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
fi

# Determine base directory (handle both worktree and main execution)
if [ -f "/workspace/branches/main/code/todo.md" ]; then
    BASE_DIR="/workspace/branches/main/code"
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

# Check 1: Task REMOVED from todo.md
if grep -q "$TASK_NAME" "$BASE_DIR/todo.md"; then
    FOUND_AT=$(grep -n "$TASK_NAME" "$BASE_DIR/todo.md" | head -3 | sed 's/^/  Line /')

    MESSAGE="## ❌ TASK ARCHIVAL VIOLATION

**Task**: \`$TASK_NAME\`
**Problem**: Task still exists in todo.md

**Requirement**: Tasks must be DELETED from todo.md, not marked complete

**Found at**:
$FOUND_AT

**Correct Archival Process**:
1. DELETE entire task entry from todo.md
2. ADD task to changelog.md under ## $(date +%Y-%m-%d)
3. Commit both changes together

See CLAUDE.md 'CRITICAL TASK ARCHIVAL' section for complete requirements."

    echo "{
      \"hookSpecificOutput\": {
        \"hookEventName\": \"PostToolUse\",
        \"additionalContext\": $(echo "$MESSAGE" | jq -Rs .)
      }
    }"
    exit 1
fi

# Check 2: Task ADDED to changelog.md
if [ ! -f "$BASE_DIR/changelog.md" ]; then
    echo "# Changelog" > "$BASE_DIR/changelog.md"
    echo "" >> "$BASE_DIR/changelog.md"
    echo "## $(date +%Y-%m-%d)" >> "$BASE_DIR/changelog.md"
fi

if ! grep -q "$TASK_NAME" "$BASE_DIR/changelog.md"; then
    MESSAGE="## ❌ TASK ARCHIVAL VIOLATION

**Task**: \`$TASK_NAME\`
**Problem**: Task not found in changelog.md

**Requirement**: Tasks must be ADDED to changelog.md under today's date

**Expected format**:
\`\`\`markdown
## $(date +%Y-%m-%d)
- **\`$TASK_NAME\`** - Task description ✅ COMPLETED
  - Solution: Brief description of what was implemented
  - Files: List of key files modified
  - Tests: Test results summary
\`\`\`

**Correct Archival Process**:
1. DELETE entire task entry from todo.md
2. ADD task to changelog.md under ## $(date +%Y-%m-%d)
3. Commit both changes together

See CLAUDE.md 'CRITICAL TASK ARCHIVAL' section for complete requirements."

    echo "{
      \"hookSpecificOutput\": {
        \"hookEventName\": \"PostToolUse\",
        \"additionalContext\": $(echo "$MESSAGE" | jq -Rs .)
      }
    }"
    exit 1
fi

# Check 3: Both files staged in git (if in git repo)
if [ -d "$BASE_DIR/.git" ] || git rev-parse --git-dir > /dev/null 2>&1; then
    WARNINGS=""

    if ! git diff --cached --name-only | grep -q "todo.md"; then
        WARNINGS="${WARNINGS}\n⚠️  todo.md not staged - did you forget to git add it?"
    fi

    if ! git diff --cached --name-only | grep -q "changelog.md"; then
        WARNINGS="${WARNINGS}\n⚠️  changelog.md not staged - did you forget to git add it?"
    fi

    if [ -n "$WARNINGS" ]; then
        MESSAGE="## ⚠️  TASK ARCHIVAL WARNING

**Task**: \`$TASK_NAME\`
**Problem**: Files not staged for commit
$WARNINGS

**Action Required**:
\`\`\`bash
git add todo.md changelog.md
git commit -m \"Complete task: $TASK_NAME\"
\`\`\`

**Both files must be committed together** to maintain archival integrity."

        echo "{
          \"hookSpecificOutput\": {
            \"hookEventName\": \"PostToolUse\",
            \"additionalContext\": $(echo "$MESSAGE" | jq -Rs .)
          }
        }"
        # Don't exit with error - this is just a warning
    fi
fi

# Success - exit silently
exit 0
