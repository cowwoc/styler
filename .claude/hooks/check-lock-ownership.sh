#!/bin/bash
# Check for active tasks owned by this session
# Runs on SessionStart to detect tasks that need resuming after context compaction
#
# BEHAVIOR:
# - Checks ALL lock files for tasks owned by this session
# - Shows "ACTIVE TASK DETECTED" warning with EXACT commands to navigate to task worktree
# - Triggers regardless of current working directory
# - Prevents working on main when task worktree exists

# Parse hook input
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Exit if no session ID
if [ -z "$SESSION_ID" ]; then
  exit 0
fi

CURRENT_DIR=$(pwd)

# STEP 2: Check for invalid lock files (non-.json extensions) containing this session_id
INVALID_LOCKS=$(find /workspace/locks -type f ! -name "*.json" -exec grep -l "\"session_id\":\s*\"$SESSION_ID\"" {} \; 2>/dev/null)

if [ -n "$INVALID_LOCKS" ]; then
  MESSAGE="## 🚨 CRITICAL: INVALID LOCK FILE DETECTED

**Your session_id found in INCORRECTLY-NAMED lock file(s)**:
$INVALID_LOCKS

**Problem**: Lock files MUST use \`.json\` extension (per task-protocol-core.md)

**⚠️ THIS FILE IS INVALID AND WILL BE IGNORED BY ALL PROTOCOL SCRIPTS**

**Action Required**:
1. You do NOT own any valid task locks
2. Remove the invalid file: \`rm $INVALID_LOCKS\`
3. If you need to claim this task, follow proper protocol in task-protocol-core.md

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

# STEP 3: Search ALL lock files for this session_id
LOCK_FILE=$(find /workspace/locks -name "*.json" -type f -exec grep -l "\"session_id\":\s*\"$SESSION_ID\"" {} \; 2>/dev/null | head -1)

if [ -z "$LOCK_FILE" ]; then
  # No active task for this session
  exit 0
fi

# Extract task details from lock file
TASK_NAME=$(basename "$LOCK_FILE" .json)
TASK_STATE=$(grep -oP '"state":\s*"\K[^"]+' "$LOCK_FILE")
TASK_WORKTREE="/workspace/branches/$TASK_NAME/code"

# Check for user approval marker if in REVIEW state
APPROVAL_STATUS=""
APPROVAL_MARKER="/workspace/branches/$TASK_NAME/user-approval-obtained.flag"
if [[ "$TASK_STATE" == "REVIEW" ]]; then
  if [ -f "$APPROVAL_MARKER" ]; then
    APPROVAL_STATUS="✅ User Approval: OBTAINED"
  else
    APPROVAL_STATUS="❌ User Approval: NOT OBTAINED - CHECKPOINT REQUIRED"
  fi
fi

# Check if worktree exists
if [ ! -d "$TASK_WORKTREE" ]; then
  MESSAGE="## 🚨 ERROR: Lock exists but worktree missing

**Task**: \`$TASK_NAME\`
**Lock File**: \`$LOCK_FILE\`
**Expected Worktree**: \`$TASK_WORKTREE\` (NOT FOUND)

**Action Required**:
1. The worktree was probably removed incorrectly
2. Release the lock: \`rm $LOCK_FILE\`
3. Select a new task from todo.md"

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

MESSAGE="## ⚠️ ACTIVE TASK DETECTED

**Task**: \`$TASK_NAME\`
**Current State**: \`$TASK_STATE\`
**Lock File**: \`$LOCK_FILE\`
**Worktree**: \`$TASK_WORKTREE\`
${APPROVAL_STATUS:+**Checkpoint Status**: \`$APPROVAL_STATUS\`}

🚨 **YOU MUST RESUME THIS TASK** before starting any new work.

## 🔴 FIRST: Navigate to Task Worktree

**Your current location**: \`$CURRENT_DIR\`

**YOU MUST RUN THIS COMMAND FIRST** (before ANY other operations):

\`\`\`bash
cd $TASK_WORKTREE
\`\`\`

**THEN verify your location**:

\`\`\`bash
pwd  # Should show: $TASK_WORKTREE
\`\`\`

**THEN check task status**:

\`\`\`bash
git status
\`\`\`

## Action Required After Navigation:

1. Continue from state: \`$TASK_STATE\`
2. Complete all remaining protocol phases
3. Only after Phase 8 (CLEANUP) may you select a new task

${APPROVAL_STATUS:+${APPROVAL_STATUS/NOT OBTAINED/## 🛑 MANDATORY USER APPROVAL CHECKPOINT #2

**YOU MUST STOP AFTER REVIEW** and present changes for user approval.

**CRITICAL**: Proceeding to COMPLETE state without user approval is a PROTOCOL VIOLATION.

**REQUIRED STEPS BEFORE COMPLETE**:
1. ✅ Commit all changes to task branch (if not already committed)
2. ✅ Record commit SHA: \`git rev-parse HEAD\`
3. ✅ Present to user:
   - Commit SHA for review
   - Files changed: \`git show --stat HEAD\`
   - Key implementation decisions
   - Test results (all passing)
   - Quality gates (checkstyle: PASS, PMD: PASS, build: SUCCESS)
4. ✅ Ask: \"Please review these changes. Would you like me to proceed with finalizing (COMPLETE → CLEANUP)?\"
5. ✅ WAIT for explicit approval keywords: \"yes\", \"approved\", \"LGTM\", \"proceed\", \"looks good\"

**ADD TODOWRITE CHECKPOINT TASK**:
\`\`\`
✅ Execute REVIEW phase - Stakeholder approval (completed)
⏸️ **Wait for user review and approval of changes** (in_progress) ← ADD THIS
⏸️ Execute COMPLETE phase - Merge to main (pending)
\`\`\`

**PROHIBITED ASSUMPTIONS**:
❌ \"Continue without asking\" instructions - DOES NOT skip checkpoint
❌ Bypass mode settings - DOES NOT skip checkpoint
❌ Stakeholder approval alone - DOES NOT equal user approval
❌ Silence or lack of objection - DOES NOT mean approval

**ENFORCEMENT**: \`enforce-user-approval.sh\` hook will BLOCK COMPLETE transition without approval marker}}

## CRITICAL - AUTONOMOUS COMPLETION REQUIREMENT:

⚠️  **DO NOT** pause mid-protocol to provide status updates
⚠️  **DO NOT** ask user for permission to continue
⚠️  **DO NOT** summarize progress instead of working

## REQUIRED BEHAVIOR:

✅ Use tools IMMEDIATELY to make progress
✅ Continue working autonomously through all phases
✅ Only stop for genuine external blockers (API down, missing credentials)

## PROHIBITED PATTERNS:

❌ Working on main branch while task worktree exists
❌ Checking \`git status\` on main when you have an active task
❌ Performing ANY operations before navigating to task worktree
❌ Select a new task from todo.md
❌ Delete or modify the lock file
❌ Assume this task is abandoned
❌ Ask \"would you like me to continue?\"
❌ Use task complexity/duration as excuse to pause"

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
