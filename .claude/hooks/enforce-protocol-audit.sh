#!/bin/bash
# Hook: enforce-protocol-audit.sh
# Trigger: SessionStart, BeforeToolCall (Write to task.json)
# Purpose: Enforce protocol audit completion before state transitions

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in enforce-protocol-audit.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read JSON input from stdin (for BeforeToolCall trigger)
if [ -t 0 ]; then
  # SessionStart trigger - check for pending audits
  TRIGGER_TYPE="SessionStart"
else
  # BeforeToolCall trigger
  INPUT=$(cat)
  TRIGGER_TYPE="BeforeToolCall"
  TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
  FILE_PATH=$(echo "$INPUT" | jq -r '.parameters.file_path // empty')
fi

# Function to check if task has pending audit
check_pending_audit() {
  local TASK_DIR=$1
  local AUDIT_PENDING="${TASK_DIR}/protocol-audit-pending.flag"
  local AUDIT_PASSED="${TASK_DIR}/protocol-audit-passed.flag"

  if [[ -f "$AUDIT_PENDING" && ! -f "$AUDIT_PASSED" ]]; then
    return 0  # Audit pending
  fi
  return 1  # No pending audit
}

# Function to get required state from pending marker
get_pending_state() {
  local TASK_DIR=$1
  local AUDIT_PENDING="${TASK_DIR}/protocol-audit-pending.flag"

  if [[ -f "$AUDIT_PENDING" ]]; then
    cat "$AUDIT_PENDING"
  fi
}

# SessionStart: Inform main agent of pending audits
if [[ "$TRIGGER_TYPE" == "SessionStart" ]]; then
  PENDING_AUDITS=()

  # Check all tasks for pending audits
  for TASK_DIR in /workspace/tasks/*/; do
    if [[ -d "$TASK_DIR" && -f "${TASK_DIR}/task.json" ]]; then
      TASK_NAME=$(basename "$TASK_DIR")

      if check_pending_audit "$TASK_DIR"; then
        PENDING_STATE=$(get_pending_state "$TASK_DIR")
        PENDING_AUDITS+=("$TASK_NAME:$PENDING_STATE")
      fi
    fi
  done

  # Report pending audits
  if [[ ${#PENDING_AUDITS[@]} -gt 0 ]]; then
    echo ""
    echo "🤖 AUTOMATIC PROTOCOL AUDIT EXECUTION REQUIRED 🤖"
    echo ""
    echo "The following tasks have pending protocol audits that MUST be executed AUTOMATICALLY:"
    echo ""

    for AUDIT_INFO in "${PENDING_AUDITS[@]}"; do
      TASK_NAME="${AUDIT_INFO%%:*}"
      STATE="${AUDIT_INFO##*:}"

      echo "  Task: ${TASK_NAME}"
      echo "  State Transition: ${STATE}"
      echo ""
      echo "  🤖 AUTOMATIC EXECUTION SEQUENCE (DO NOT WAIT FOR USER INPUT):"
      echo ""
      echo "    STEP 1: Automatically invoke parse-conversation-timeline skill"
      echo "            Skill: parse-conversation-timeline"
      echo ""
      echo "    STEP 2: Automatically invoke audit-protocol-compliance skill with timeline output"
      echo "            Skill: audit-protocol-compliance"
      echo ""
      echo "    STEP 3a: IF audit-protocol-compliance verdict == PASSED:"
      echo "             touch /workspace/tasks/${TASK_NAME}/protocol-audit-passed.flag"
      echo "             rm -f /workspace/tasks/${TASK_NAME}/protocol-audit-pending.flag"
      echo "             → Audit complete, proceed with normal work"
      echo ""
      echo "    STEP 3b: IF audit-protocol-compliance verdict == FAILED:"
      echo "             → Automatically invoke documentation-auditor"
      echo "             Task tool (documentation-auditor) --violations \"{violations from step 2}\""
      echo "             → Read documentation-auditor recommendations"
      echo "             → Fix violations according to guidance"
      echo "             → Automatically re-run audit (return to STEP 1)"
      echo "             → Repeat until PASSED verdict obtained"
      echo ""
      echo "  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
      echo ""
    done

    echo "🚨 MANDATORY AUTONOMOUS EXECUTION: These steps execute AUTOMATICALLY without user approval."
    echo "   This is not a suggestion or reminder - it is an AUTOMATIC MANDATORY WORKFLOW."
    echo "   Main agent: Execute audit pipeline immediately before proceeding with any other work."
    echo ""
  fi

  exit 0
fi

# BeforeToolCall: Enforce audit before state transitions
if [[ "$TOOL_NAME" == "Write" && "$FILE_PATH" == *"/task.json" ]]; then
  # Extract task directory from file path
  TASK_DIR=$(dirname "$FILE_PATH")
  TASK_NAME=$(basename "$TASK_DIR")

  # Check if this is a state transition (new state in write content)
  NEW_STATE=$(echo "$INPUT" | jq -r '.parameters.content // empty' | jq -r '.state // empty' 2>/dev/null)

  if [[ -n "$NEW_STATE" ]]; then
    # Get current state
    CURRENT_STATE=$(jq -r '.state // "unknown"' "$FILE_PATH" 2>/dev/null)

    # If state is changing, check if audit completed for current state
    if [[ "$CURRENT_STATE" != "$NEW_STATE" && "$CURRENT_STATE" != "unknown" ]]; then
      AUDIT_PASSED="${TASK_DIR}/protocol-audit-passed.flag"

      if [[ ! -f "$AUDIT_PASSED" ]]; then
        echo ""
        echo "🚨🚨🚨 STATE TRANSITION BLOCKED - AUTOMATIC AUDIT REQUIRED 🚨🚨🚨"
        echo ""
        echo "BLOCKED: State transition requires completed protocol audit"
        echo ""
        echo "Current State: ${CURRENT_STATE}"
        echo "Attempted State: ${NEW_STATE}"
        echo "Task: ${TASK_NAME}"
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "🤖 AUTOMATIC MANDATORY EXECUTION BEFORE RETRY:"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        echo "Main agent: Execute this audit pipeline AUTOMATICALLY (no user approval needed):"
        echo ""
        echo "STEP 1 (AUTOMATIC): Invoke parse-conversation-timeline skill"
        echo "        Skill: parse-conversation-timeline"
        echo "        Purpose: Collect objective facts about session execution"
        echo ""
        echo "STEP 2 (AUTOMATIC): Invoke audit-protocol-compliance skill with timeline output"
        echo "        Skill: audit-protocol-compliance"
        echo "        Purpose: Check protocol compliance with zero-tolerance enforcement"
        echo ""
        echo "STEP 3a (IF PASSED): Create audit-passed marker and retry transition"
        echo "        touch /workspace/tasks/${TASK_NAME}/protocol-audit-passed.flag"
        echo "        rm -f /workspace/tasks/${TASK_NAME}/protocol-audit-pending.flag"
        echo "        # Automatically retry the state transition (will now succeed)"
        echo ""
        echo "STEP 3b (IF FAILED): Automatic violation resolution workflow"
        echo "        → Automatically invoke documentation-auditor:"
        echo "          Task tool (documentation-auditor) --violations \"{violations array}\""
        echo "        → Read documentation-auditor root cause analysis"
        echo "        → Fix each violation according to check_id guidance"
        echo "        → Automatically re-run entire audit (return to STEP 1)"
        echo "        → Continue until PASSED verdict obtained"
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "ENFORCEMENT MECHANISM:"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        echo "• This Write tool call is BLOCKED (exit 1)"
        echo "• Audit-pending marker created: /workspace/tasks/${TASK_NAME}/protocol-audit-pending.flag"
        echo "• Main agent: Execute audit pipeline automatically, then retry Write tool"
        echo "• State transition will succeed after audit-passed flag exists"
        echo ""
        echo "🤖 AUTONOMOUS EXECUTION: These steps run AUTOMATICALLY without waiting for user."
        echo "   Main agent: Begin audit pipeline execution immediately."
        echo ""
        echo "Reference: docs/project/task-protocol-core.md § AUTOMATED PROTOCOL COMPLIANCE AUDIT"
        echo ""

        # Create pending audit marker
        echo "$CURRENT_STATE → $NEW_STATE" > "${TASK_DIR}/protocol-audit-pending.flag"

        exit 2
      fi

      # Audit passed - allow transition and clear flags
      rm -f "${TASK_DIR}/protocol-audit-passed.flag"
      rm -f "${TASK_DIR}/protocol-audit-pending.flag"

      # Create new pending audit for the new state
      # (will need audit before transitioning FROM this new state)
      echo "$NEW_STATE → ?" > "${TASK_DIR}/protocol-audit-pending.flag"
    fi
  fi
fi

# Allow operation
exit 0
