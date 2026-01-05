#!/bin/bash
# validate-rebase-deletions.sh - Validate no unexpected deletions after rebase
#
# ADDED: 2026-01-01 after data loss during add-flexible-constructor-bodies task.
# Agent worktree was based on old main (59fa6eb), but main had been updated with
# add-module-import-declarations task (fe0dc90). When agent work was rebased onto
# new main, it silently reverted the newer changes (deleted ModuleImportAttribute.java,
# ModuleImportParserTest.java).
#
# This hook runs PostToolUse after Bash commands and checks for file deletions
# relative to main after rebase operations.
#
# PREVENTS: Silent data loss when rebasing stale branches onto updated main.

set -euo pipefail
trap 'echo "ERROR in validate-rebase-deletions.sh line $LINENO: $BASH_COMMAND" >&2; exit 0' ERR

# Read hook input from stdin
INPUT=$(cat)

# Extract tool name
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // empty' 2>/dev/null || echo "")

# Only check after Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
  exit 0
fi

# Extract command that was executed
COMMAND=$(echo "$INPUT" | jq -r '.tool.input.command // empty' 2>/dev/null || echo "")

# Only check after rebase commands
if [[ ! "$COMMAND" =~ git.*rebase|git.*cherry-pick|git.*reset.*--hard ]]; then
  exit 0
fi

# Extract stdout (command output)
STDOUT=$(echo "$INPUT" | jq -r '.result.stdout // empty' 2>/dev/null || echo "")

# Skip if rebase failed or was aborted
if [[ "$STDOUT" =~ "CONFLICT"|"Aborting"|"failed"|"error:" ]]; then
  exit 0
fi

# Skip if not in a git repository
if ! git rev-parse --git-dir >/dev/null 2>&1; then
  exit 0
fi

# Skip if we don't have a main branch reference
if ! git rev-parse main >/dev/null 2>&1; then
  exit 0
fi

# Check for deletions relative to main
DELETIONS=$(git diff --name-status main..HEAD 2>/dev/null | grep '^D' | cut -f2 || true)

if [[ -n "$DELETIONS" ]]; then
  DELETION_COUNT=$(echo "$DELETIONS" | wc -l)

  # Only warn if there are deletions (might be intentional)
  echo "" >&2
  echo "============================================================" >&2
  echo "⚠️  POST-REBASE DELETION CHECK" >&2
  echo "============================================================" >&2
  echo "" >&2
  echo "Found $DELETION_COUNT file(s) DELETED relative to main:" >&2
  echo "" >&2
  echo "$DELETIONS" | head -20 >&2
  if [[ $DELETION_COUNT -gt 20 ]]; then
    echo "... and $((DELETION_COUNT - 20)) more" >&2
  fi
  echo "" >&2
  echo "CRITICAL: Review each deletion to confirm it's intentional." >&2
  echo "" >&2
  echo "If ANY deletion is unexpected, this may indicate:" >&2
  echo "  - Stale worktree (branch based on old main)" >&2
  echo "  - Rebase silently reverted changes from newer main" >&2
  echo "" >&2
  echo "To investigate a deleted file:" >&2
  echo "  git log --all --oneline -- <deleted-file>" >&2
  echo "" >&2
  echo "If deletion is UNINTENTIONAL:" >&2
  echo "  1. STOP - Do not proceed with merge" >&2
  echo "  2. git reset --hard <backup-branch>" >&2
  echo "  3. Rebase agent branch onto current main first" >&2
  echo "  4. Retry the operation" >&2
  echo "" >&2
  echo "COMMON CAUSE: File extraction from commits based on stale main." >&2
  echo "See: docs/project/git-workflow.md § File Extraction from Commits" >&2
  echo "============================================================" >&2
  echo "" >&2
fi

exit 0
