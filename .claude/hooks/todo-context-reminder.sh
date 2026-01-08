#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in todo-context-reminder.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Hook to remind Claude about todo list synchronization

# Determine the root directory of the current git repository
if git rev-parse --git-dir > /dev/null 2>&1; then
    # We're in a git repository - use the repository root
    REPO_ROOT="$(git rev-parse --show-toplevel)"
    TODO_PATH="$REPO_ROOT/todo.md"
else
    # Fallback: default to main
    TODO_PATH="/workspace/main/todo.md"
fi

echo "📋 TODO LIST SYNC REMINDER: When the user mentions 'todo list', they refer to BOTH:"
echo "   1. Project tasks in $TODO_PATH (main list)"
echo "   2. TodoWrite tool (workflow execution tracking)"
echo "   🔄 KEEP BOTH LISTS IN SYNC - Update todo.md when completing TodoWrite tasks"