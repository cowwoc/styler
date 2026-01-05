#!/bin/bash
# warn-file-extraction.sh - PreToolUse hook to warn about file extraction from commits
#
# ADDED: 2026-01-05 after cherry-picking files from commit based on stale main,
# causing 60 lines of switch-parsing code to be incorrectly removed.
#
# When extracting files from commits (git show/checkout), the files contain the
# ENTIRE content from that commit - not just the changes. If the commit was based
# on an older version of main, the extracted files will have stale base code.
#
# This hook detects file extraction patterns and warns about base version verification.
#
# PREVENTS: Silent code loss when extracting files from commits based on stale main.

set -eo pipefail
trap 'echo "ERROR in warn-file-extraction.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 0' ERR

# Read JSON context from stdin
INPUT=$(cat)

# Only process Bash tool
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
if [[ "$TOOL_NAME" != "Bash" ]]; then
	exit 0
fi

# Get the command
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

# Patterns that extract file content from specific commits:
# - git show <commit>:<file>     - Shows file content from commit
# - git checkout <commit> -- <file>  - Extracts file from commit to working directory
# - git restore --source=<commit> -- <file>  - Same as checkout

# Check for git show with file path (commit:path pattern)
if [[ "$COMMAND" =~ git[[:space:]]+show[[:space:]].*:[^[:space:]]+ ]]; then
	FILE_EXTRACTION=true
	EXTRACTION_TYPE="git show"
fi

# Check for git checkout with commit and file path
if [[ "$COMMAND" =~ git[[:space:]]+checkout[[:space:]]+[^[:space:]]+[[:space:]]+--[[:space:]] ]]; then
	FILE_EXTRACTION=true
	EXTRACTION_TYPE="git checkout"
fi

# Check for git restore --source
if [[ "$COMMAND" =~ git[[:space:]]+restore[[:space:]]+--source ]]; then
	FILE_EXTRACTION=true
	EXTRACTION_TYPE="git restore --source"
fi

# If file extraction detected, warn about base version verification
if [[ "${FILE_EXTRACTION:-false}" == "true" ]]; then
	# Rate limit: Only warn once per 5 minutes to avoid spam
	RATE_LIMIT_FILE="/tmp/file-extraction-warning-$(date +%Y%m%d%H)"
	RATE_LIMIT_MINUTE=$(($(date +%M) / 5))
	RATE_LIMIT_KEY="${RATE_LIMIT_FILE}-${RATE_LIMIT_MINUTE}"

	if [[ -f "$RATE_LIMIT_KEY" ]]; then
		# Already warned recently, skip
		exit 0
	fi
	touch "$RATE_LIMIT_KEY"

	echo "" >&2
	echo "================================================================" >&2
	echo "  WARNING: File Extraction from Commit Detected" >&2
	echo "================================================================" >&2
	echo "" >&2
	echo "You're using '$EXTRACTION_TYPE' to extract file content from a commit." >&2
	echo "" >&2
	echo "CRITICAL: Extracted files contain the ENTIRE file content from that" >&2
	echo "commit - not just the changes. If the source commit was based on an" >&2
	echo "older version of main, the extracted files will have STALE BASE CODE." >&2
	echo "" >&2
	echo "VERIFICATION REQUIRED:" >&2
	echo "  1. Identify what commit main was at when source commit was created:" >&2
	echo "     git log --oneline --graph main <source-commit> | head -20" >&2
	echo "" >&2
	echo "  2. Compare extracted file against current main:" >&2
	echo "     git diff main:<file> <source-commit>:<file>" >&2
	echo "" >&2
	echo "  3. If main has changes not in source commit, DO NOT use file extraction." >&2
	echo "     Instead, use git diff/patch to apply only the delta changes." >&2
	echo "" >&2
	echo "SAFE ALTERNATIVE: Apply only the changes, not the whole file:" >&2
	echo "  git diff <base-commit>..<source-commit> -- <file> | git apply" >&2
	echo "================================================================" >&2
	echo "" >&2
fi

# Allow command to proceed (warning only, not blocking)
exit 0
