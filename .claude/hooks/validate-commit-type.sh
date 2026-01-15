#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in validate-commit-type.sh line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Validate commit message uses valid CAT commit types
# PreToolUse hook for Bash commands containing "git commit"

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

# Only check git commit commands
if [[ ! "$COMMAND" =~ git[[:space:]]+commit ]]; then
    exit 0
fi

# Valid commit types per commit-types.md
VALID_TYPES="feature|bugfix|test|refactor|performance|docs|style|config|planning"

# Invalid types (common conventional commits that are NOT valid here)
INVALID_TYPES="fix|feat|chore|build|ci|perf"

# Extract the commit message type from -m flag
# Handles: git commit -m "type: message" and HEREDOC patterns
if [[ "$COMMAND" =~ -m[[:space:]]+[\"\']?([a-z]+): ]]; then
    COMMIT_TYPE="${BASH_REMATCH[1]}"
elif [[ "$COMMAND" =~ ^([a-z]+):[[:space:]] ]]; then
    COMMIT_TYPE="${BASH_REMATCH[1]}"
else
    # Can't extract type, let it pass
    exit 0
fi

# Check if it's an explicitly invalid type
if [[ "$COMMIT_TYPE" =~ ^($INVALID_TYPES)$ ]]; then
    echo "BLOCKED: Invalid commit type '$COMMIT_TYPE:'" >&2
    echo "" >&2
    echo "Per commit-types.md, these abbreviated forms are NOT valid:" >&2
    echo "  feat, fix, chore, build, ci, perf" >&2
    echo "" >&2
    echo "Use the full standard types instead:" >&2
    echo "  feature, bugfix, test, refactor, performance, docs, style, config, planning" >&2
    echo "" >&2
    echo "Example: Use 'bugfix:' instead of 'fix:'" >&2
    exit 1
fi

exit 0
