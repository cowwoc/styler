#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in task-cleanup.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source JSON output helper
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

#
# Task Cleanup Script
#
# Purpose: Safely remove all task branches and worktrees after successful merge to main,
#          preserving audit trail while reclaiming resources.
#
# Usage: task-cleanup.sh TASK_NAME
#

if [ $# -lt 1 ]; then
    json_error "Usage: task-cleanup.sh TASK_NAME"
fi

TASK_NAME="$1"
TASK_DIR="/workspace/tasks/$TASK_NAME"
TIMESTAMP=$(date -Iseconds)

# Verify task exists
if [ ! -d "$TASK_DIR" ]; then
    json_error "Task not found: $TASK_DIR"
fi

# Verify task.json exists
if [ ! -f "$TASK_DIR/task.json" ]; then
    json_error "task.json not found: $TASK_DIR/task.json"
fi

# Check task state is COMPLETE
STATE=$(jq -r '.state' "$TASK_DIR/task.json")
if [ "$STATE" != "COMPLETE" ]; then
    json_error "Task not in COMPLETE state (current: $STATE)"
fi

# Verify we're in main repository
cd /workspace/main || json_error "Cannot change to /workspace/main"

# Track removed resources
REMOVED_WORKTREES=()
DELETED_BRANCHES=()

# Remove worktrees (best-effort, continue on errors)
echo "Removing worktrees..." >&2

for worktree in "$TASK_DIR/code" "$TASK_DIR/agents/architect/code" "$TASK_DIR/agents/tester/code" "$TASK_DIR/agents/formatter/code"; do
    if [ -d "$worktree" ]; then
        if git worktree remove --force "$worktree" 2>/dev/null; then
            REMOVED_WORKTREES+=("$worktree")
        else
            echo "Warning: Failed to remove worktree: $worktree" >&2
        fi
    fi
done

# Delete branches (best-effort, continue on errors)
echo "Deleting branches..." >&2

for branch in "$TASK_NAME" "$TASK_NAME-architect" "$TASK_NAME-tester" "$TASK_NAME-formatter"; do
    # Skip version branches (v1, v13, v21, etc.)
    if [[ "$branch" =~ ^v[0-9]+$ ]]; then
        echo "Skipping version branch: $branch" >&2
        continue
    fi

    if git show-ref --verify --quiet "refs/heads/$branch"; then
        if git branch -D "$branch" 2>/dev/null; then
            DELETED_BRANCHES+=("$branch")
        else
            echo "Warning: Failed to delete branch: $branch" >&2
        fi
    fi
done

# Update task.json to CLEANUP state
jq --arg timestamp "$TIMESTAMP" '.state = "CLEANUP" | .cleaned_at = $timestamp' "$TASK_DIR/task.json" > "$TASK_DIR/task.json.tmp"
mv "$TASK_DIR/task.json.tmp" "$TASK_DIR/task.json"

# Verify cleanup
echo "Verifying cleanup..." >&2

# Check no worktrees remain
REMAINING_WORKTREES=$(git worktree list | grep -c "$TASK_NAME" || true)
if [ "$REMAINING_WORKTREES" -gt 0 ]; then
    echo "Warning: Some worktrees may remain: $REMAINING_WORKTREES" >&2
fi

# Check no branches remain (except version branches)
REMAINING_BRANCHES=$(git branch | grep "$TASK_NAME" | grep -v -E "^  v[0-9]+$" || true)
if [ -n "$REMAINING_BRANCHES" ]; then
    echo "Warning: Some branches may remain: $REMAINING_BRANCHES" >&2
fi

# Success - return JSON
json_success \
    "task_name=$TASK_NAME" \
    "removed_worktrees=$(printf '%s\n' "${REMOVED_WORKTREES[@]}" | jq -R . | jq -s .)" \
    "deleted_branches=$(printf '%s\n' "${DELETED_BRANCHES[@]}" | jq -R . | jq -s .)" \
    "preserved_artifacts=$TASK_DIR"
