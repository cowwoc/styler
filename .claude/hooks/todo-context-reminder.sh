#!/bin/bash
set -euo pipefail

# Hook to remind Claude about todo list synchronization

# Determine the root directory of the current git repository/branch
if git rev-parse --git-dir > /dev/null 2>&1; then
    # We're in a git repository - use the repository root
    REPO_ROOT="$(git rev-parse --show-toplevel)"
    TODO_PATH="$REPO_ROOT/todo.md"
else
    # Fallback: use current working directory, or main branch if we're outside branches structure
    if [[ "$(pwd)" == /workspace/branches/*/code* ]]; then
        # We're in a branch directory structure - use current location
        TODO_PATH="$(pwd)/todo.md"
    else
        # We're outside branch structure - default to main branch
        TODO_PATH="/workspace/main/todo.md"
    fi
fi

echo "📋 TODO LIST SYNC REMINDER: When the user mentions 'todo list', they refer to BOTH:"
echo "   1. Project tasks in $TODO_PATH (main list)"  
echo "   2. TodoWrite tool (workflow execution tracking)"
echo "   🔄 KEEP BOTH LISTS IN SYNC - Update todo.md when completing TodoWrite tasks"