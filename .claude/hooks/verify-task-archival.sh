#!/bin/bash
# Verify task archival requirements before Phase 7 commit
#
# Usage: bash .claude/hooks/verify-task-archival.sh [task-name]
#
# If task-name is not provided, auto-detects from current worktree path.
#
# This script verifies that a completed task has been properly archived:
# 1. Task REMOVED from todo.md (entire entry deleted)
# 2. Task ADDED to changelog.md (under today's date)
# 3. Both files staged for commit

set -e

# Auto-detect task name from worktree path if not provided
TASK_NAME="$1"

if [ -z "$TASK_NAME" ]; then
    # Extract task name from /workspace/branches/{task-name}/code path
    TASK_NAME=$(pwd | grep -oP '/workspace/branches/\K[^/]+(?=/code)' || echo "")

    if [ -z "$TASK_NAME" ] || [ "$TASK_NAME" = "main" ]; then
        # Not in a task worktree - this is likely not a task archival operation
        # Exit silently (status 0) to avoid blocking non-archival edits to todo.md/changelog.md
        exit 0
    fi

    echo "📍 Auto-detected task name from worktree: $TASK_NAME"
    echo ""
fi

echo "🔍 Verifying archival for task: $TASK_NAME"
echo ""

# Determine base directory (handle both worktree and main execution)
if [ -f "/workspace/branches/main/code/todo.md" ]; then
    BASE_DIR="/workspace/branches/main/code"
elif [ -f "todo.md" ]; then
    BASE_DIR="."
else
    echo "❌ ERROR: Cannot find todo.md - run from project root"
    exit 1
fi

# Check 1: Task REMOVED from todo.md
echo "Check 1: Verifying task removed from todo.md..."
if grep -q "$TASK_NAME" "$BASE_DIR/todo.md"; then
    echo "❌ VIOLATION: Task '$TASK_NAME' still exists in todo.md"
    echo "   Requirement: Task must be DELETED from todo.md, not marked complete"
    echo "   Found at:"
    grep -n "$TASK_NAME" "$BASE_DIR/todo.md" | head -3
    exit 1
fi
echo "✅ Task removed from todo.md"
echo ""

# Check 2: Task ADDED to changelog.md
echo "Check 2: Verifying task added to changelog.md..."
if [ ! -f "$BASE_DIR/changelog.md" ]; then
    echo "⚠️  WARNING: changelog.md not found - creating it"
    echo "# Changelog" > "$BASE_DIR/changelog.md"
    echo "" >> "$BASE_DIR/changelog.md"
    echo "## $(date +%Y-%m-%d)" >> "$BASE_DIR/changelog.md"
fi

if ! grep -q "$TASK_NAME" "$BASE_DIR/changelog.md"; then
    echo "❌ VIOLATION: Task '$TASK_NAME' not found in changelog.md"
    echo "   Requirement: Task must be ADDED to changelog.md under today's date"
    echo ""
    echo "   Expected format:"
    echo "   ## $(date +%Y-%m-%d)"
    echo "   - **\`$TASK_NAME\`** - Task description ✅ COMPLETED"
    exit 1
fi
echo "✅ Task added to changelog.md"
echo ""

# Check 3: Both files staged in git (if in git repo)
if [ -d "$BASE_DIR/.git" ] || git rev-parse --git-dir > /dev/null 2>&1; then
    echo "Check 3: Verifying files staged for commit..."

    if ! git diff --cached --name-only | grep -q "todo.md"; then
        echo "⚠️  WARNING: todo.md not staged - did you forget to commit the deletion?"
    else
        echo "✅ todo.md staged"
    fi

    if ! git diff --cached --name-only | grep -q "changelog.md"; then
        echo "⚠️  WARNING: changelog.md not staged - did you forget to commit the addition?"
    else
        echo "✅ changelog.md staged"
    fi
    echo ""
fi

echo "✅ Task archival verification PASSED for: $TASK_NAME"
echo ""
echo "Ready to commit with both todo.md and changelog.md changes."
exit 0
