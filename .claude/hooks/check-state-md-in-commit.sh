#!/bin/bash
set -euo pipefail

# Error handler
trap 'echo "ERROR in check-state-md-in-commit.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# PostToolUse hook: Warn if commit on task branch doesn't include STATE.md
# Detects M076/M119 violations: STATE.md must be in same commit as implementation

HOOK_CONTEXT=$(cat)
TOOL_NAME=$(echo "$HOOK_CONTEXT" | jq -r '.tool_name // "unknown"')

# Only check Bash tool (commits)
if [[ "$TOOL_NAME" != "Bash" ]]; then
    exit 0
fi

TOOL_INPUT=$(echo "$HOOK_CONTEXT" | jq -r '.tool_input.command // ""')
TOOL_STDOUT=$(echo "$HOOK_CONTEXT" | jq -r '.tool_response.stdout // ""')
TOOL_EXIT=$(echo "$HOOK_CONTEXT" | jq -r '.tool_response.exit_code // 0')

# Only check successful git commits
if [[ "$TOOL_EXIT" != "0" ]]; then
    exit 0
fi

if ! echo "$TOOL_INPUT" | grep -q "git commit"; then
    exit 0
fi

# Check if we're on a task branch (format: X.Y-task-name)
BRANCH=$(git branch --show-current 2>/dev/null || echo "")
if [[ -z "$BRANCH" ]] || ! echo "$BRANCH" | grep -qE "^[0-9]+\.[0-9]+-"; then
    exit 0  # Not a task branch
fi

# Check if this was an implementation commit (bugfix, feature, refactor, test)
if ! echo "$TOOL_INPUT" | grep -qE "(bugfix|feature|refactor|test):"; then
    exit 0  # Config/docs commit, STATE.md not required
fi

# Check if STATE.md was included in the commit
LAST_COMMIT_FILES=$(git diff-tree --no-commit-id --name-only -r HEAD 2>/dev/null || echo "")
if ! echo "$LAST_COMMIT_FILES" | grep -q "STATE.md"; then
    # STATE.md missing from implementation commit
    ADDITIONAL_CONTEXT=$(cat << 'EOF'
⚠️ M076/M119 VIOLATION: STATE.md not included in implementation commit

Task branches require STATE.md to be updated IN THE SAME COMMIT as implementation.

**Fix:** Amend the commit to include STATE.md:
```bash
# Update STATE.md to status: completed
git add .claude/cat/v*/v*.*/task/*/STATE.md
git commit --amend --no-edit
```
EOF
)

    ESCAPED_CONTEXT=$(echo "$ADDITIONAL_CONTEXT" | jq -Rs .)

    cat << EOF
{
  "hookSpecificOutput": {
    "hookEventName": "PostToolUse",
    "additionalContext": ${ESCAPED_CONTEXT}
  }
}
EOF
fi

exit 0
