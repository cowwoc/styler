#!/bin/bash
# Validates task/agent branch pushes to remote origin
#
# TRIGGER: PreToolUse on Bash commands containing "git push"
# PURPOSE: Allow task branches to be pushed for review (PRs)
# BLOCKS: Agent branches (task-architect, task-tester, etc.) - these should never be pushed
# UPDATED: 2025-12-25 - Allow task branch pushes for review purposes

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

# Get current branch if no branch specified
CURRENT_BRANCH=$(cd /workspace/main 2>/dev/null && git branch --show-current 2>/dev/null || echo "")

# Agent branch pattern - these should NEVER be pushed
# Agent branches end with -architect, -engineer, -formatter, -tester, etc.
AGENT_PATTERN="-architect$|-engineer$|-formatter$|-tester$|-optimizer$|-hacker$|-builder$|-designer$"

# Block agent branches (internal implementation detail, not for review)
if [[ -n "$CURRENT_BRANCH" ]] && [[ "$CURRENT_BRANCH" =~ $AGENT_PATTERN ]]; then
    REASON="Push blocked: '$CURRENT_BRANCH' is an agent branch.

Agent branches are internal implementation details and should not be pushed.
Only task branches (for review) and main (after merge) should be pushed.

Workflow:
1. Merge agent work to task branch
2. Push task branch for review (allowed)
3. After approval, merge to main and push main"

    jq -n \
        --arg reason "$REASON" \
        '{
            "decision": "block",
            "reason": $reason
        }'
    exit 0
fi

# Check if command explicitly names an agent branch
for WORD in $COMMAND; do
    if [[ "$WORD" =~ $AGENT_PATTERN ]]; then
        REASON="Push blocked: '$WORD' appears to be an agent branch name.

Agent branches should not be pushed to origin."

        jq -n \
            --arg reason "$REASON" \
            '{
                "decision": "block",
                "reason": $reason
            }'
        exit 0
    fi
done

# Task branches (fix-, implement-, add-, etc.) are now ALLOWED for review purposes
exit 0
