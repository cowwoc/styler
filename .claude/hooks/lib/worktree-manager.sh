#!/bin/bash
# Worktree Manager Library
# Reusable functions for agent worktree creation and management
#
# Usage: source /workspace/main/.claude/hooks/lib/worktree-manager.sh

# Create agent worktree for task protocol
# Args:
#   $1: task-name (e.g., "implement-formatter-api")
#   $2: agent-name (e.g., "architect", "formatter")
# Returns:
#   0: Success (worktree created or already exists)
#   1: Failure (missing args, git error)
#
# Example:
#   create_agent_worktree "implement-formatter-api" "technical-architect"
create_agent_worktree() {
    local task_name="$1"
    local agent_name="$2"

    if [[ -z "$task_name" || -z "$agent_name" ]]; then
        echo "ERROR: create_agent_worktree requires task-name and agent-name" >&2
        return 1
    fi

    local worktree_path="/workspace/tasks/${task_name}/agents/${agent_name}/code"
    local branch_name="${task_name}-${agent_name}"

    # Check if worktree already exists
    if [[ -d "$worktree_path" ]]; then
        echo "Agent worktree already exists: $worktree_path" >&2
        return 0
    fi

    # Create parent directory structure
    mkdir -p "/workspace/tasks/${task_name}/agents/${agent_name}"

    # Create worktree
    if git worktree add "$worktree_path" -b "$branch_name" 2>&1; then
        echo "Created agent worktree: $worktree_path (branch: $branch_name)" >&2
        return 0
    else
        echo "ERROR: Failed to create agent worktree at $worktree_path" >&2
        return 1
    fi
}

# Remove agent worktree (cleanup)
# Args:
#   $1: task-name
#   $2: agent-name
# Returns:
#   0: Success (worktree removed or doesn't exist)
#   1: Failure (git error)
remove_agent_worktree() {
    local task_name="$1"
    local agent_name="$2"

    if [[ -z "$task_name" || -z "$agent_name" ]]; then
        echo "ERROR: remove_agent_worktree requires task-name and agent-name" >&2
        return 1
    fi

    local worktree_path="/workspace/tasks/${task_name}/agents/${agent_name}/code"
    local branch_name="${task_name}-${agent_name}"

    # Check if worktree exists
    if [[ ! -d "$worktree_path" ]]; then
        echo "Agent worktree does not exist: $worktree_path" >&2
        return 0
    fi

    # Remove worktree
    if git worktree remove "$worktree_path" 2>&1; then
        echo "Removed agent worktree: $worktree_path" >&2

        # Optionally delete branch (if exists and not checked out elsewhere)
        if git show-ref --verify --quiet "refs/heads/$branch_name"; then
            git branch -D "$branch_name" 2>&1 || true
            echo "Deleted branch: $branch_name" >&2
        fi

        return 0
    else
        echo "ERROR: Failed to remove agent worktree at $worktree_path" >&2
        return 1
    fi
}

# List all agent worktrees for a task
# Args:
#   $1: task-name
# Outputs:
#   List of agent names with active worktrees
list_agent_worktrees() {
    local task_name="$1"

    if [[ -z "$task_name" ]]; then
        echo "ERROR: list_agent_worktrees requires task-name" >&2
        return 1
    fi

    local agents_dir="/workspace/tasks/${task_name}/agents"

    if [[ ! -d "$agents_dir" ]]; then
        return 0
    fi

    find "$agents_dir" -mindepth 2 -maxdepth 2 -type d -name "code" | while read -r worktree_path; do
        local agent_name=$(basename "$(dirname "$worktree_path")")
        echo "$agent_name"
    done
}
