#!/bin/bash
# Hook: enforce-awaiting-user-approval.sh
# Trigger: PreToolUse on lock file state updates
# Purpose: Block REVIEW → COMPLETE transitions without AWAITING_USER_APPROVAL

TASK_JSON="$1"
NEW_STATE=$(jq -r '.state' "$TASK_JSON" 2>/dev/null)
OLD_STATE=$(git show HEAD:"$TASK_JSON" 2>/dev/null | jq -r '.state' 2>/dev/null || echo "")

# Detect REVIEW → COMPLETE transition
if [ "$OLD_STATE" = "REVIEW" ] && [ "$NEW_STATE" = "COMPLETE" ]; then
    echo "❌ CRITICAL VIOLATION: Cannot transition REVIEW → COMPLETE"
    echo "REQUIRED: REVIEW → AWAITING_USER_APPROVAL → COMPLETE"
    echo "Missing AWAITING_USER_APPROVAL state execution"
    exit 1
fi

# Detect REVIEW → CLEANUP transition
if [ "$OLD_STATE" = "REVIEW" ] && [ "$NEW_STATE" = "CLEANUP" ]; then
    echo "❌ CRITICAL VIOLATION: Cannot transition REVIEW → CLEANUP"
    echo "REQUIRED: REVIEW → AWAITING_USER_APPROVAL → COMPLETE → CLEANUP"
    echo "Missing AWAITING_USER_APPROVAL and COMPLETE states"
    exit 1
fi

# Verify approval flag before COMPLETE transition
if [ "$OLD_STATE" = "AWAITING_USER_APPROVAL" ] && [ "$NEW_STATE" = "COMPLETE" ]; then
    TASK_NAME=$(jq -r '.task_name' "$TASK_JSON")
    APPROVAL_FLAG="/workspace/tasks/${TASK_NAME}/user-approval-obtained.flag"
    if [ ! -f "$APPROVAL_FLAG" ]; then
        echo "❌ CRITICAL VIOLATION: User approval flag missing"
        echo "Expected: $APPROVAL_FLAG"
        echo "Cannot transition to COMPLETE without user approval"
        exit 1
    fi
fi
