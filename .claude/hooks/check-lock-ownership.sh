#!/bin/bash
# Check for active tasks owned by this session
# Runs on SessionStart to detect tasks that need resuming after context compaction

# Parse hook input
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Exit if no session ID
if [ -z "$SESSION_ID" ]; then
  exit 0
fi

# Check for lock files owned by this session
OWNED_LOCK=$(grep -l "\"session_id\":\s*\"$SESSION_ID\"" /workspace/locks/*.json 2>/dev/null | head -1)

if [ -n "$OWNED_LOCK" ]; then
  TASK_NAME=$(basename "$OWNED_LOCK" .json)
  TASK_STATE=$(grep -oP '"state":\s*"\K[^"]+' "$OWNED_LOCK")

  # Create warning message for Claude
  MESSAGE="## ⚠️ ACTIVE TASK DETECTED

**Task**: \`$TASK_NAME\`
**Current State**: \`$TASK_STATE\`
**Lock File**: \`/workspace/locks/$TASK_NAME.json\`
**Worktree**: \`/workspace/branches/$TASK_NAME/code\`

🚨 **YOU MUST RESUME THIS TASK** before starting any new work.

**Action Required**:
1. \`cd /workspace/branches/$TASK_NAME/code\`
2. Continue from state: \`$TASK_STATE\`
3. Complete all remaining protocol phases
4. Only after Phase 7 (CLEANUP) may you select a new task

**DO NOT**:
- ❌ Select a new task from todo.md
- ❌ Work on any other task
- ❌ Delete or modify the lock file
- ❌ Assume this task is abandoned"

else
  # No active task - provide guidance
  MESSAGE="## ✅ No Active Tasks

**Session ID**: \`${SESSION_ID:0:12}...\`
**Lock Status**: No locks owned by this session

You may select a new task from \`todo.md\` and begin the task protocol."
fi

# Output as hook response
jq -n \
  --arg event "SessionStart" \
  --arg context "$MESSAGE" \
  '{
    "hookSpecificOutput": {
      "hookEventName": $event,
      "additionalContext": $context
    }
  }'
