#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in validate-commit-type.sh line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Validate commit message types against allowed list
# Triggered by: PreToolUse hook on Bash tool with git commit

# Read JSON input
INPUT=$(cat)

# Check if this is a git commit command
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

if [[ -z "$COMMAND" ]] || ! echo "$COMMAND" | grep -qE 'git\s+commit'; then
    exit 0
fi

# Extract commit message from -m flag or heredoc
COMMIT_MSG=""

# Try -m "message" format
if echo "$COMMAND" | grep -qE -- '-m\s+["\x27]'; then
    COMMIT_MSG=$(echo "$COMMAND" | sed -nE 's/.*-m\s+["\x27]([^"\x27]+)["\x27].*/\1/p')
fi

# Try heredoc format
if [[ -z "$COMMIT_MSG" ]] && echo "$COMMAND" | grep -qE "<<'?EOF"; then
    # Extract first line after EOF marker
    COMMIT_MSG=$(echo "$COMMAND" | sed -n '/<<.*EOF/,/EOF/p' | sed '1d;$d' | head -1)
fi

# If no message found, allow (might be interactive or amend)
if [[ -z "$COMMIT_MSG" ]]; then
    exit 0
fi

# Valid commit types (from CONVENTIONS.md)
VALID_TYPES="feature|bugfix|test|refactor|performance|config|docs"

# Extract type from message (first word before colon)
COMMIT_TYPE=$(echo "$COMMIT_MSG" | sed -nE 's/^([a-z]+):.*/\1/p')

if [[ -z "$COMMIT_TYPE" ]]; then
    # No type prefix found - allow (might be merge commit or special format)
    exit 0
fi

# Validate type
if ! echo "$COMMIT_TYPE" | grep -qE "^($VALID_TYPES)$"; then
    cat <<EOF
{
  "decision": "block",
  "reason": "Invalid commit type '$COMMIT_TYPE'. Valid types: feature, bugfix, test, refactor, performance, config, docs. See .planning/codebase/CONVENTIONS.md#git-conventions"
}
EOF
    exit 0
fi

# Type is valid
exit 0
