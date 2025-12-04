#!/bin/bash
# Blocks pushing task/agent branches to remote origin
#
# TRIGGER: PreToolUse on Bash commands containing "git push"
# PURPOSE: Task branches are local workspaces - only main gets pushed
# ADDED: After mistake where task+agent branches were pushed to origin (session ec47cea5)
# PREVENTS: Polluting remote with temporary task branches

set -euo pipefail

trap 'echo "ERROR in block-task-branch-push.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read hook input from stdin
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // ""')
TOOL_INPUT=$(echo "$INPUT" | jq -r '.tool_input // ""')

# Only process Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
    exit 0
fi

# Get command from tool input
COMMAND=$(echo "$TOOL_INPUT" | jq -r '.command // ""')

# Only check git push commands
if [[ ! "$COMMAND" =~ git[[:space:]]+push ]]; then
    exit 0
fi

# Check if pushing to origin (default remote)
# Allow explicit pushes to other remotes
if [[ "$COMMAND" =~ git[[:space:]]+push[[:space:]]+[a-zA-Z] ]] && [[ ! "$COMMAND" =~ git[[:space:]]+push[[:space:]]+(origin|--) ]]; then
    exit 0
fi

# Check if any task-related branch patterns in the push
# Task branches follow pattern: {task-name} or {task-name}-{agent}
# Look for common task indicators in branch names being pushed

# Get current branch if no branch specified
CURRENT_BRANCH=$(cd /workspace/main 2>/dev/null && git branch --show-current 2>/dev/null || echo "")

# Detect task branch patterns:
# 1. Branch name contains common task prefixes
# 2. Branch ends with -architect, -engineer, -formatter, -tester, etc.
# 3. We're in a task worktree

TASK_PATTERN="implement-|add-|fix-|refactor-|update-|create-|-architect$|-engineer$|-formatter$|-tester$|-optimizer$|-hacker$"

# Check if current branch matches task pattern
if [[ -n "$CURRENT_BRANCH" ]] && [[ "$CURRENT_BRANCH" =~ $TASK_PATTERN ]]; then
    REASON="Push blocked: '$CURRENT_BRANCH' appears to be a task/agent branch.

Task branches should remain LOCAL. Only main branch gets pushed to origin.

Workflow:
1. Complete work on task branch (local)
2. Squash commits into single commit (git rebase -i)
3. Merge to main with --ff-only
4. Push main to origin

If you need to push this branch for a specific reason, use:
  git push origin $CURRENT_BRANCH --force  # Explicitly bypass check"

    jq -n \
        --arg reason "$REASON" \
        '{
            "decision": "block",
            "reason": $reason
        }'
    exit 0
fi

# Check if command explicitly names a task branch
for WORD in $COMMAND; do
    if [[ "$WORD" =~ $TASK_PATTERN ]]; then
        REASON="Push blocked: '$WORD' appears to be a task/agent branch name.

Task branches should remain LOCAL. Only main branch gets pushed."

        jq -n \
            --arg reason "$REASON" \
            '{
                "decision": "block",
                "reason": $reason
            }'
        exit 0
    fi
done

exit 0
