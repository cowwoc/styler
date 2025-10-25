#!/bin/bash
# Library for validating state transitions
# Sourced by hooks that need to validate state machine transitions

# Validate a state transition according to task protocol rules
# Usage: validate_state_transition <current_state> <target_state>
# Returns: 0 if valid, 1 if invalid (with error message to stderr)
validate_state_transition() {
    local CURRENT_STATE="$1"
    local TARGET_STATE="$2"

    # CRITICAL VALIDATION: Block REVIEW → COMPLETE direct transition
    if [[ "$CURRENT_STATE" == "REVIEW" && "$TARGET_STATE" == "COMPLETE" ]]; then
        echo "" >&2
        echo "🚨 STATE TRANSITION VIOLATION DETECTED 🚨" >&2
        echo "" >&2
        echo "PROHIBITED: Direct transition from REVIEW to COMPLETE" >&2
        echo "REQUIRED SEQUENCE: REVIEW → AWAITING_USER_APPROVAL → COMPLETE" >&2
        echo "" >&2
        echo "The AWAITING_USER_APPROVAL state is MANDATORY and CANNOT be bypassed." >&2
        echo "It serves as the formal handoff point where:" >&2
        echo "  - Main agent presents complete implementation for approval" >&2
        echo "  - User reviews and decides: approve, reject, or request changes" >&2
        echo "  - Explicit approval is required before transition to COMPLETE" >&2
        echo "" >&2
        echo "CORRECT WORKFLOW:" >&2
        echo "  1. REVIEW state: Stakeholder reviews complete, no violations" >&2
        echo "  2. Main agent transitions to AWAITING_USER_APPROVAL" >&2
        echo "  3. Main agent presents implementation summary to user" >&2
        echo "  4. User approves (or requests changes)" >&2
        echo "  5. Main agent transitions to COMPLETE" >&2
        echo "" >&2
        echo "See: docs/project/task-protocol-core.md § AWAITING_USER_APPROVAL" >&2
        echo "" >&2
        return 1
    fi

    # VALIDATION: Block other invalid direct transitions that skip required states
    local INVALID_TRANSITIONS=(
        "INIT:REVIEW"
        "INIT:AWAITING_USER_APPROVAL"
        "INIT:COMPLETE"
        "CLASSIFIED:REVIEW"
        "CLASSIFIED:AWAITING_USER_APPROVAL"
        "CLASSIFIED:COMPLETE"
        "REQUIREMENTS:AWAITING_USER_APPROVAL"
        "REQUIREMENTS:COMPLETE"
        "SYNTHESIS:AWAITING_USER_APPROVAL"
        "SYNTHESIS:COMPLETE"
        "IMPLEMENTATION:AWAITING_USER_APPROVAL"
        "IMPLEMENTATION:COMPLETE"
    )

    local TRANSITION="$CURRENT_STATE:$TARGET_STATE"

    for INVALID in "${INVALID_TRANSITIONS[@]}"; do
        if [[ "$TRANSITION" == "$INVALID" ]]; then
            echo "" >&2
            echo "🚨 INVALID STATE TRANSITION: $CURRENT_STATE → $TARGET_STATE 🚨" >&2
            echo "" >&2
            echo "This transition skips required intermediate states." >&2
            echo "See: docs/project/task-protocol-core.md for valid state sequences" >&2
            echo "" >&2
            return 1
        fi
    done

    # All validations passed
    return 0
}
