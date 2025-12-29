#!/bin/bash
# Pre-Tool-Use Hook: Block git branch -f main for task branches
#
# ADDED: 2025-12-11 after session 317ae3b3 bypassed archival enforcement by using
# "git branch -f main HEAD" instead of "git merge --ff-only", which circumvented
# the enforce-atomic-archival.sh hook
#
# PREVENTS: Bypassing merge workflow by directly moving main branch pointer
# ENFORCES: All task merges must go through git merge --ff-only
# EXCEPTION: Allows during squash/rebase when backup branch exists (backup-before-squash-*, backup-before-rebase-*)
#
# Triggers: PreToolUse (tool:Bash matcher in settings.json)

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-branch-force-update.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")
TOOL_PARAMS=$(echo "$INPUT" | jq -r '.tool_input // {}' 2>/dev/null || echo "{}")

# Only validate Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
	exit 0
fi

# Extract command
COMMAND=$(echo "$TOOL_PARAMS" | jq -r '.command // empty' 2>/dev/null || echo "")

# Skip multi-line commands (likely scripts or searches, not actual git commands)
if [[ $(echo "$COMMAND" | wc -l) -gt 1 ]]; then
	exit 0
fi

# Check for git branch -f main (force update main branch)
# Patterns: "git branch -f main", "git branch --force main"
if [[ ! "$COMMAND" =~ git[[:space:]]+branch[[:space:]]+(-f|--force)[[:space:]]+main ]]; then
	exit 0
fi

log_hook_start "block-branch-force-update" "PreToolUse"

# Allow during legitimate squash/rebase operations
# Detection: backup branch exists with squash/rebase pattern
BACKUP_BRANCHES=$(git branch --list 'backup-before-squash-*' 'backup-before-rebase-*' 2>/dev/null || true)
if [[ -n "$BACKUP_BRANCHES" ]]; then
	log_hook_end "block-branch-force-update" "PreToolUse" "Allowed: squash/rebase backup branch detected"
	exit 0
fi

# Extract what HEAD/commit is being used
# This catches: git branch -f main HEAD, git branch -f main <commit>
log_hook_blocked "block-branch-force-update" "PreToolUse" "Attempted to force-update main branch"

MESSAGE="## 🚨 BRANCH FORCE UPDATE BLOCKED

**Command**: \`$COMMAND\`
**Violation**: Direct manipulation of main branch pointer is prohibited

## ⚠️ WHY THIS IS BLOCKED

Using \`git branch -f main\` bypasses the merge workflow and archival enforcement:
- Skips atomic archival verification (todo.md + changelog.md)
- Circumvents pre-merge hooks
- Creates inconsistent repository state

## ✅ CORRECT APPROACH

**Use git merge --ff-only instead:**

\`\`\`bash
cd /workspace/main
git merge --ff-only <task-branch>
\`\`\`

This ensures:
1. Archival files (todo.md, changelog.md) are verified in branch
2. Pre-merge hooks can validate the merge
3. Post-merge cleanup is triggered
4. Audit trail is maintained

## If You Need to Update Main

If you legitimately need to update main to a specific commit:
1. Create a branch at that commit
2. Merge the branch with --ff-only
3. This triggers all validation hooks

## Protocol Reference

See:
- git-workflow.md § Branch Force Update Prohibition
- task-protocol-core.md § AWAITING_USER_APPROVAL → COMPLETE Transition
- enforce-atomic-archival.sh (archival enforcement)"

output_hook_block "Blocked: git branch -f main prohibited. Use git merge --ff-only to properly merge task branches." "$MESSAGE"
exit 0
