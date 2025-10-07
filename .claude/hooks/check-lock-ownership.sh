#!/bin/bash
# Check for active tasks owned by this session
# Runs on SessionStart to detect tasks that need resuming after context compaction
#
# BEHAVIOR:
# - Only triggers when working directory is in a task worktree (/workspace/branches/{task}/code)
# - Checks if the corresponding lock file exists and is owned by this session
# - Shows "ACTIVE TASK DETECTED" warning only when both conditions are met
# - Exits silently when in main branch or outside worktree directories

# Parse hook input
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Exit if no session ID
if [ -z "$SESSION_ID" ]; then
  exit 0
fi

# STEP 1: Check if we're in a task worktree (not main branch)
# Only check for active tasks if we're in a task worktree directory
CURRENT_DIR=$(pwd)

# Check if we're in a worktree (pattern: /workspace/branches/*/code but NOT /workspace/branches/main/code)
if [[ ! "$CURRENT_DIR" =~ ^/workspace/branches/[^/]+/code ]]; then
  # Not in a worktree at all - no need to check for active tasks
  exit 0
fi

if [[ "$CURRENT_DIR" =~ ^/workspace/branches/main/code ]]; then
  # We're in the main branch - no active task
  exit 0
fi

# Extract task name from current directory path
TASK_NAME_FROM_PATH=$(echo "$CURRENT_DIR" | sed -n 's|^/workspace/branches/\([^/]*\)/code.*|\1|p')

# STEP 2: Check for invalid lock files (non-.json extensions) containing this session_id
INVALID_LOCKS=$(find /workspace/locks -type f ! -name "*.json" -exec grep -l "\"session_id\":\s*\"$SESSION_ID\"" {} \; 2>/dev/null)

if [ -n "$INVALID_LOCKS" ]; then
  MESSAGE="## 🚨 CRITICAL: INVALID LOCK FILE DETECTED

**Your session_id found in INCORRECTLY-NAMED lock file(s)**:
$INVALID_LOCKS

**Problem**: Lock files MUST use \`.json\` extension (per task-protocol.md)

**⚠️ THIS FILE IS INVALID AND WILL BE IGNORED BY ALL PROTOCOL SCRIPTS**

**Action Required**:
1. You do NOT own any valid task locks
2. Remove the invalid file: \`rm $INVALID_LOCKS\`
3. If you need to claim this task, follow proper protocol in task-protocol.md

**Why this matters**:
- The \`check-lock-ownership.sh\` hook ONLY checks \`*.json\` files
- No protocol script will recognize ownership from non-.json files
- Working on tasks without proper \`.json\` lock causes race conditions"

  jq -n \
    --arg event "SessionStart" \
    --arg context "$MESSAGE" \
    '{
      "hookSpecificOutput": {
        "hookEventName": $event,
        "additionalContext": $context
      }
    }'
  exit 0
fi

# STEP 3: Check if lock file exists for this task worktree and is owned by this session
LOCK_FILE="/workspace/locks/$TASK_NAME_FROM_PATH.json"

if [ ! -f "$LOCK_FILE" ]; then
  # No lock file exists for this worktree - this is unusual but not an error
  # User might be in an old worktree that was already cleaned up
  exit 0
fi

# Check if the lock is owned by this session
LOCK_SESSION_ID=$(grep -oP '"session_id":\s*"\K[^"]+' "$LOCK_FILE" 2>/dev/null)

if [ "$LOCK_SESSION_ID" != "$SESSION_ID" ]; then
  # Lock exists but owned by different session - exit silently
  # This means another Claude instance is working on this task
  exit 0
fi

# Lock exists and is owned by this session - show active task warning
TASK_STATE=$(grep -oP '"state":\s*"\K[^"]+' "$LOCK_FILE")

MESSAGE="## ⚠️ ACTIVE TASK DETECTED

**Task**: \`$TASK_NAME_FROM_PATH\`
**Current State**: \`$TASK_STATE\`
**Lock File**: \`$LOCK_FILE\`
**Worktree**: \`$CURRENT_DIR\`

🚨 **YOU MUST RESUME THIS TASK** before starting any new work.

**Action Required**:
1. Continue from state: \`$TASK_STATE\`
2. Complete all remaining protocol phases
3. Only after Phase 7 (CLEANUP) may you select a new task

**CRITICAL - AUTONOMOUS COMPLETION REQUIREMENT**:
⚠️  **DO NOT** pause mid-protocol to provide status updates
⚠️  **DO NOT** ask user for permission to continue
⚠️  **DO NOT** summarize progress instead of working

**REQUIRED BEHAVIOR**:
✅ Use tools IMMEDIATELY to make progress
✅ Continue working autonomously through all phases
✅ Only stop for genuine external blockers (API down, missing credentials)

**DO NOT**:
- ❌ Select a new task from todo.md
- ❌ Work on any other task
- ❌ Delete or modify the lock file
- ❌ Assume this task is abandoned
- ❌ Provide status summaries without tool calls
- ❌ Ask \"would you like me to continue?\"
- ❌ Use task complexity/duration as excuse to pause"

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
