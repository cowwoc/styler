#!/bin/bash
# User Approval Checkpoint Enforcement Hook
# Runs on UserPromptSubmit to detect and enforce mandatory user approval checkpoints
# Prevents transitioning to COMPLETE state without user approval

# Parse hook input
INPUT=$(cat)
USER_PROMPT=$(echo "$INPUT" | jq -r '.user_prompt // empty')
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Check if we're in a task worktree
CURRENT_DIR=$(pwd)
if [[ ! "$CURRENT_DIR" =~ /workspace/branches/[^/]+/code$ ]]; then
  exit 0
fi

TASK_NAME=$(basename $(dirname "$CURRENT_DIR"))

# Check if lock file exists and extract state
LOCK_FILE="/workspace/locks/${TASK_NAME}.json"
if [ ! -f "$LOCK_FILE" ]; then
  exit 0
fi

TASK_STATE=$(grep -oP '"state":\s*"\K[^"]+' "$LOCK_FILE")

# Check for user approval marker file
APPROVAL_MARKER="/workspace/branches/${TASK_NAME}/user-approval-obtained.flag"

# CRITICAL CHECK: Detect attempt to proceed to COMPLETE without user approval
if [[ "$TASK_STATE" == "REVIEW" ]] && [[ ! -f "$APPROVAL_MARKER" ]]; then
  # Check if user prompt contains finalization keywords
  LOWER_PROMPT=$(echo "$USER_PROMPT" | tr '[:upper:]' '[:lower:]')

  if [[ "$LOWER_PROMPT" =~ (continue|proceed|complete|finalize|merge|finish|cleanup) ]]; then

    # Check if this IS the approval message
    if [[ "$LOWER_PROMPT" =~ (yes|approved|approve|proceed|looks good|lgtm) ]] && \
       [[ "$LOWER_PROMPT" =~ (review|changes|commit|finali) ]]; then
      # User is providing approval - create approval marker
      touch "$APPROVAL_MARKER"
      exit 0
    fi

    MESSAGE="## 🚨 MANDATORY USER APPROVAL CHECKPOINT - BLOCKED

**Current State**: REVIEW (unanimous stakeholder approval obtained)
**Attempted Action**: Transition to COMPLETE state
**VIOLATION DETECTED**: User approval checkpoint not satisfied

---

## Protocol Requirement (task-protocol-core.md:46-50)

**Checkpoint 2: [CHANGE REVIEW] - After REVIEW, Before COMPLETE**
- ✅ **MANDATORY**: Present completed changes with commit SHA to user
- ✅ **MANDATORY**: Wait for explicit user review approval
- ❌ **PROHIBITED**: Assuming user approval from unanimous agent approval alone
- ❌ **PROHIBITED**: Proceeding to COMPLETE without clear user confirmation

## 🔒 CHECKPOINT IS MANDATORY REGARDLESS OF:
- User instructions to \"continue without asking\"
- Bypass mode settings
- Automation mode
- Session continuity
- Time pressure

**From protocol line 36**:
> The two user approval checkpoints are MANDATORY and MUST be respected
> REGARDLESS of whether the user is in \"bypass permissions on\" mode or
> any other automation mode.

---

## Required Action Before COMPLETE:

\`\`\`bash
# 1. Ensure all changes are committed
git status

# 2. Get commit SHA for user review
git rev-parse HEAD

# 3. Present changes to user
git show --stat HEAD
\`\`\`

## Then present to user:

\"All stakeholder agents have approved the implementation.

**Commit SHA**: [commit-sha-here]
**Files changed**: [list from git show --stat]
**Key implementation decisions**: [summary]
**Test results**: [summary]
**Quality gates**: [checkstyle/PMD/build status]

Please review the changes. Would you like me to proceed with finalizing (COMPLETE → CLEANUP)?\"

## Wait for explicit user approval before proceeding to COMPLETE state.

**Approval markers**: \"yes\", \"approved\", \"proceed\", \"looks good\", \"LGTM\"

---

**🚫 TRANSITION TO COMPLETE STATE IS BLOCKED UNTIL USER APPROVAL IS OBTAINED**"

    jq -n \
      --arg event "UserPromptSubmit" \
      --arg context "$MESSAGE" \
      '{
        "hookSpecificOutput": {
          "hookEventName": $event,
          "additionalContext": $context
        }
      }'
    exit 0
  fi
fi

# If we're past REVIEW and approval marker exists, allow COMPLETE
if [[ "$TASK_STATE" == "COMPLETE" ]] || [[ "$TASK_STATE" == "CLEANUP" ]]; then
  # Clean up approval marker during cleanup
  if [[ "$TASK_STATE" == "CLEANUP" ]]; then
    rm -f "$APPROVAL_MARKER" 2>/dev/null
  fi
fi

exit 0
