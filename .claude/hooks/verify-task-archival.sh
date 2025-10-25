#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in verify-task-archival.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Verify task archival requirements after COMPLETE state
#
# Usage: bash .claude/hooks/verify-task-archival.sh [task-name]
#
# This script runs ONCE after COMPLETE state if archival was skipped.
# It verifies that a completed task has been properly archived:
# 1. Task REMOVED from todo.md (entire entry deleted)
# 2. Task ADDED to changelog.md (under today's date)
# 3. Both files staged for commit
#
# TRIGGER CONDITIONS:
# - Lock file exists with state="CLEANUP" (transitioning from COMPLETE)
# - Archival marker file does NOT exist
# - Task still present in todo.md (archival was skipped)

# Auto-detect task name from worktree path if not provided
TASK_NAME="${1:-}"

if [ -z "$TASK_NAME" ]; then
    # Extract task name from /workspace/tasks/{task-name}/code path
    TASK_NAME=$(pwd | grep -oP '/workspace/tasks/\K[^/]+(?=/code)' || echo "")

    if [ -z "$TASK_NAME" ]; then
        # Not in a task worktree path - check if any tasks are in CLEANUP state
        # This handles hook invocation from /workspace or other directories
        CLEANUP_LOCKS=""
        if [ -d "/workspace/tasks" ]; then
            CLEANUP_LOCKS=$(find /workspace/tasks -name "task.json" -type f 2>/dev/null | while read -r lock; do
                state=$(jq -r '.state // ""' "$lock" 2>/dev/null)
                if [ "$state" = "CLEANUP" ]; then
                    basename "$lock" .json
                fi
            done)
        fi

        if [ -z "$CLEANUP_LOCKS" ]; then
            # No tasks in CLEANUP state - archival check not applicable
            exit 0
        fi

        # Found task(s) in CLEANUP state - cannot determine which one to verify
        echo "ERROR: Cannot determine task name - no argument provided and not in task worktree" >&2
        echo "Usage: $0 <task-name>" >&2
        echo "Or run from within /workspace/tasks/{task-name}/code/" >&2
        exit 1
    fi

    if [ "$TASK_NAME" = "main" ]; then
        # In main worktree - this is not a task archival operation
        # Exit silently (status 0) to avoid blocking non-archival edits to todo.md/changelog.md
        exit 0
    fi
fi

# Check if archival marker exists - if so, verification already ran
MARKER_FILE="/tmp/.archival-verified-${TASK_NAME}"
if [ -f "$MARKER_FILE" ]; then
    # Already verified for this task - exit silently
    exit 0
fi

# Check if we're in CLEANUP state (after COMPLETE)
LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"
if [ ! -f "$LOCK_FILE" ]; then
    # No lock file - task may have already completed cleanup
    # Exit silently - archival check not needed
    exit 0
fi

# Extract current state from lock file
LOCK_STATE=$(jq -r '.state // ""' "$LOCK_FILE" 2>/dev/null || echo "")

# Only run verification if in CLEANUP state (after COMPLETE)
if [ "$LOCK_STATE" != "CLEANUP" ]; then
    # Not in CLEANUP state yet - archival happens during COMPLETE→CLEANUP transition
    # Exit silently - archival check not needed yet
    exit 0
fi

# Determine base directory (handle both worktree and main execution)
if [ -f "/workspace/main/todo.md" ]; then
    BASE_DIR="/workspace/main"
elif [ -f "todo.md" ]; then
    BASE_DIR="."
else
    MESSAGE="## ❌ ERROR: Cannot find todo.md

**Action Required**: Run this script from the project root directory"

    echo "{
      \"hookSpecificOutput\": {
        \"hookEventName\": \"PostToolUse\",
        \"additionalContext\": $(echo "$MESSAGE" | jq -Rs .)
      }
    }"
    exit 1
fi

# Check if task still exists in todo.md (archival was skipped)
if ! grep -qF "$TASK_NAME" "$BASE_DIR/todo.md"; then
    # Task already removed from todo.md - archival was completed
    # Create marker to prevent re-running and exit silently
    touch "$MARKER_FILE"
    exit 0
fi

# ARCHIVAL WAS SKIPPED - Run verification checks
FOUND_AT=$(grep -nF "$TASK_NAME" "$BASE_DIR/todo.md" | head -3 | sed 's/^/  Line /')

MESSAGE="## ❌ TASK ARCHIVAL VIOLATION

**Task**: \`$TASK_NAME\`
**State**: CLEANUP (archival should have occurred during COMPLETE state)
**Problem**: Task still exists in todo.md - archival was skipped

**Requirement**: Tasks must be DELETED from todo.md and ADDED to changelog.md during COMPLETE state

**Found at**:
$FOUND_AT

**Correct Archival Process (COMPLETE State)**:
1. DELETE entire task entry from todo.md
2. ADD task to changelog.md under ## $(date +%Y-%m-%d)
3. Commit both changes together with implementation in single atomic commit

**Recovery Steps**:
\`\`\`bash
# 1. Remove task from todo.md (delete entire entry)
# 2. Add task to changelog.md with completion details
# 3. Stage both files
git add todo.md changelog.md

# 4. Amend the COMPLETE commit to include archival
git commit --amend --no-edit
\`\`\`

See CLAUDE.md 'CRITICAL TASK ARCHIVAL' section for complete requirements.

**Note**: This verification runs once during CLEANUP state to catch missed archival."

echo "{
  \"hookSpecificOutput\": {
    \"hookEventName\": \"PostToolUse\",
    \"additionalContext\": $(echo "$MESSAGE" | jq -Rs .)
  }
}"

# Create marker to prevent re-running this verification
touch "$MARKER_FILE"

exit 1
