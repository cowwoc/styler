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

# POSITIVE VERIFICATION: Type MUST be in valid list (M098)
# This catches ALL invalid types, not just known-bad ones
if [[ ! "$COMMIT_TYPE" =~ ^($VALID_TYPES)$ ]]; then
    echo "BLOCKED: Invalid commit type '$COMMIT_TYPE:'" >&2
    echo "" >&2
    echo "Valid commit types (per git-commit skill):" >&2
    echo "  feature, bugfix, test, refactor, performance, docs, style, config, planning" >&2
    echo "" >&2
    if [[ "$COMMIT_TYPE" =~ ^($INVALID_TYPES)$ ]]; then
        echo "Note: '$COMMIT_TYPE' is a common conventional commit type but not valid here." >&2
        echo "Use the full name instead (e.g., 'bugfix:' not 'fix:', 'feature:' not 'feat:')" >&2
    else
        echo "Type '$COMMIT_TYPE' is not recognized. Check git-commit skill for guidance." >&2
    fi
    exit 1
fi

exit 0
