#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in task-init.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source JSON output helper
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

#
# Task Initialization Script
#
# Purpose: Atomically initialize complete task structure with proper state tracking,
#          worktrees, branches, and directory organization.
#
# Usage: task-init.sh TASK_NAME [DESCRIPTION]
#
# Example: task-init.sh implement-formatter-api "Add public API for formatting rules"
#

# Validate arguments
if [ $# -lt 1 ]; then
    json_error "Usage: task-init.sh TASK_NAME [DESCRIPTION]"
fi

TASK_NAME="$1"
DESCRIPTION="${2:-Task description}"
SESSION_ID="${3:-${CLAUDE_SESSION_ID:-unknown}}"  # Session ID for ownership tracking
TASK_DIR="/workspace/tasks/$TASK_NAME"
TIMESTAMP=$(date -Iseconds)
START_TIME=$(date +%s)

# Validate session ID is provided
if [ "$SESSION_ID" = "unknown" ]; then
    echo "⚠️ WARNING: No session ID provided. Task ownership cannot be tracked." >&2
    echo "   Pass session ID as 3rd argument or set CLAUDE_SESSION_ID env var." >&2
fi

# Validate task name format (kebab-case)
if ! [[ "$TASK_NAME" =~ ^[a-z0-9]+(-[a-z0-9]+)*$ ]]; then
    json_error "Invalid task name format. Must be kebab-case: $TASK_NAME"
fi

# Check if task already exists
if [ -d "$TASK_DIR" ]; then
    json_error "Task already exists: $TASK_DIR"
fi

# Verify we're in main repository
if [ ! -d "/workspace/main/.git" ]; then
    json_error "Not in main repository: /workspace/main"
fi

cd /workspace/main

# Check no uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
    json_error "Uncommitted changes in main. Commit or stash before initializing task."
fi

# Track created resources for rollback on failure
CREATED_WORKTREES=()
CREATED_BRANCHES=()
CREATED_DIRS=()

# Cleanup function for rollback
cleanup_on_error() {
    echo "Error occurred. Rolling back changes..." >&2

    # Remove worktrees
    for worktree in "${CREATED_WORKTREES[@]}"; do
        if [ -d "$worktree" ]; then
            git worktree remove --force "$worktree" 2>/dev/null || true
        fi
    done

    # Delete branches
    for branch in "${CREATED_BRANCHES[@]}"; do
        git branch -D "$branch" 2>/dev/null || true
    done

    # Remove directories
    for dir in "${CREATED_DIRS[@]}"; do
        if [ -d "$dir" ]; then
            rm -rf "$dir"
        fi
    done

    echo "Rollback complete." >&2
}

trap cleanup_on_error ERR

# Create task directory structure
mkdir -p "$TASK_DIR"
CREATED_DIRS+=("$TASK_DIR")

mkdir -p "$TASK_DIR/agents/architect"
mkdir -p "$TASK_DIR/agents/tester"
mkdir -p "$TASK_DIR/agents/formatter"

# Create branches
echo "Creating branches..." >&2
git branch "$TASK_NAME" main
CREATED_BRANCHES+=("$TASK_NAME")

git branch "$TASK_NAME-architect" main
CREATED_BRANCHES+=("$TASK_NAME-architect")

git branch "$TASK_NAME-tester" main
CREATED_BRANCHES+=("$TASK_NAME-tester")

git branch "$TASK_NAME-formatter" main
CREATED_BRANCHES+=("$TASK_NAME-formatter")

# Create worktrees
echo "Creating worktrees..." >&2
git worktree add "$TASK_DIR/code" "$TASK_NAME"
CREATED_WORKTREES+=("$TASK_DIR/code")

git worktree add "$TASK_DIR/agents/architect/code" "$TASK_NAME-architect"
CREATED_WORKTREES+=("$TASK_DIR/agents/architect/code")

git worktree add "$TASK_DIR/agents/tester/code" "$TASK_NAME-tester"
CREATED_WORKTREES+=("$TASK_DIR/agents/tester/code")

git worktree add "$TASK_DIR/agents/formatter/code" "$TASK_NAME-formatter"
CREATED_WORKTREES+=("$TASK_DIR/agents/formatter/code")

# Create task.json with session_id for ownership tracking
cat > "$TASK_DIR/task.json" <<EOF
{
  "task_name": "$TASK_NAME",
  "session_id": "$SESSION_ID",
  "state": "INIT",
  "created": "$TIMESTAMP",
  "phase": "initialization",
  "agents": {
    "architect": {"status": "not_started"},
    "tester": {"status": "not_started"},
    "formatter": {"status": "not_started"}
  },
  "transition_log": [
    {"from": null, "to": "INIT", "timestamp": "$TIMESTAMP"}
  ]
}
EOF

# Create task.md template
cat > "$TASK_DIR/task.md" <<EOF
# Task: $TASK_NAME

## Status: INIT

## Description

$DESCRIPTION

## Requirements

[To be filled by stakeholder agents in REQUIREMENTS phase]

## Implementation Plan

[To be synthesized in SYNTHESIS phase]
EOF

# Verify all components created
echo "Verifying initialization..." >&2

# Verify task.json
if [ ! -f "$TASK_DIR/task.json" ]; then
    json_error "task.json not created"
fi

# Verify task.md
if [ ! -f "$TASK_DIR/task.md" ]; then
    json_error "task.md not created"
fi

# Verify all worktrees
for worktree in "${CREATED_WORKTREES[@]}"; do
    if [ ! -d "$worktree" ]; then
        json_error "Worktree not created: $worktree"
    fi
done

# Verify all branches
for branch in "${CREATED_BRANCHES[@]}"; do
    if ! git show-ref --verify --quiet "refs/heads/$branch"; then
        json_error "Branch not created: $branch"
    fi
done

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

# Build worktrees and branches as JSON arrays
WORKTREES_JSON=$(printf '%s\n' "${CREATED_WORKTREES[@]}" | jq -R . | jq -s .)
BRANCHES_JSON=$(printf '%s\n' "${CREATED_BRANCHES[@]}" | jq -R . | jq -s .)

# Build complete JSON object for json_success
ADDITIONAL_JSON=$(jq -n \
    --arg task_name "$TASK_NAME" \
    --arg task_dir "$TASK_DIR" \
    --argjson worktrees "$WORKTREES_JSON" \
    --argjson branches "$BRANCHES_JSON" \
    --arg state_file "$TASK_DIR/task.json" \
    --arg timestamp "$TIMESTAMP" \
    --argjson duration "$DURATION" \
    '{
        task_name: $task_name,
        task_dir: $task_dir,
        worktrees: $worktrees,
        branches: $branches,
        state_file: $state_file,
        timestamp: $timestamp,
        duration_seconds: $duration
    }')

# Success - return JSON with properly constructed object
json_success "Task initialized successfully" "$ADDITIONAL_JSON"
