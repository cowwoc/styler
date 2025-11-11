#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in task-invoke-post.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Post-tool-use hook: Auto-create agent worktrees after Task tool invocation
# Prevents VIOLATION #2: Missing Agent Worktrees

# Parse stdin JSON for tool result details
TOOL_RESULT=$(cat)

# Extract tool name and check if it was a Task invocation
TOOL_NAME=$(echo "$TOOL_RESULT" | jq -r '.tool // empty')

if [[ "$TOOL_NAME" != "Task" ]]; then
    exit 0
fi

# Extract subagent type from Task tool parameters
SUBAGENT_TYPE=$(echo "$TOOL_RESULT" | jq -r '.parameters.subagent_type // empty')

# If no subagent type, skip
if [[ -z "$SUBAGENT_TYPE" ]]; then
    exit 0
fi

# Derive agent name from subagent_type (e.g., "style-reviewer" -> "style-reviewer")
AGENT_NAME="$SUBAGENT_TYPE"

# Infer task name from current working directory or task context
TASK_NAME=""
if [[ "$PWD" == /workspace/tasks/*/code ]] || [[ "$PWD" == /workspace/tasks/*/agents/*/code ]]; then
    # Extract from current directory
    TASK_NAME=$(echo "$PWD" | sed -E 's|/workspace/tasks/([^/]+)/.*|\1|')
fi

# If still no task name, check all task directories for this session's lock
if [[ -z "$TASK_NAME" ]]; then
    SESSION_ID=$(echo "$TOOL_RESULT" | jq -r '.session_id // empty')
    if [[ -n "$SESSION_ID" ]]; then
        for task_dir in /workspace/tasks/*; do
            if [[ -f "$task_dir/task.json" ]]; then
                LOCK_SESSION=$(jq -r '.session_id // ""' "$task_dir/task.json" 2>/dev/null || echo "")
                if [[ "$LOCK_SESSION" == "$SESSION_ID" ]]; then
                    TASK_NAME=$(basename "$task_dir")
                    break
                fi
            fi
        done
    fi
fi

# If we still can't determine task name, skip auto-creation
if [[ -z "$TASK_NAME" ]]; then
    exit 0
fi

# Define paths
TASK_ROOT="/workspace/tasks/$TASK_NAME"
AGENT_WORKTREE="$TASK_ROOT/agents/$AGENT_NAME/code"
TASK_BRANCH="$TASK_NAME"
AGENT_BRANCH="$TASK_NAME-$AGENT_NAME"

# Check if agent worktree already exists
if [[ -d "$AGENT_WORKTREE" ]]; then
    # Worktree exists, nothing to do
    exit 0
fi

# Check if task root exists
if [[ ! -d "$TASK_ROOT" ]]; then
    # Task not initialized yet, skip auto-creation
    exit 0
fi

# Create agent worktree directory structure
mkdir -p "$TASK_ROOT/agents/$AGENT_NAME"

# Navigate to main repo to create worktree
cd /workspace/main

# Check if agent branch exists
if git show-ref --verify --quiet "refs/heads/$AGENT_BRANCH"; then
    # Branch exists, add worktree from existing branch
    git worktree add "$AGENT_WORKTREE" "$AGENT_BRANCH" 2>/dev/null || true
else
    # Branch doesn't exist, create from task branch
    if git show-ref --verify --quiet "refs/heads/$TASK_BRANCH"; then
        git worktree add "$AGENT_WORKTREE" -b "$AGENT_BRANCH" "$TASK_BRANCH" 2>/dev/null || true
    else
        # Task branch doesn't exist yet, create from main
        git worktree add "$AGENT_WORKTREE" -b "$AGENT_BRANCH" main 2>/dev/null || true
    fi
fi

# Verify worktree creation
if [[ -d "$AGENT_WORKTREE/.git" ]]; then
    echo "✅ Auto-created agent worktree: $AGENT_WORKTREE" >&2
else
    # Worktree creation failed silently, log but don't block
    echo "⚠️  Could not auto-create agent worktree for $AGENT_NAME (may need manual creation)" >&2
fi

exit 0
