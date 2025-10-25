#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in validate-state-transition.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Validate state transitions to prevent protocol violations
# Specifically prevents REVIEW → COMPLETE bypass of AWAITING_USER_APPROVAL

# Source validation library
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/state-transition-validator.sh"

TASK_DIR="${1:-}"

if [[ -z "$TASK_DIR" ]]; then
    echo "ERROR: validate-state-transition.sh requires task directory argument" >&2
    exit 1
fi

TASK_JSON="$TASK_DIR/task.json"

if [[ ! -f "$TASK_JSON" ]]; then
    # No task.json - not a state transition scenario
    exit 0
fi

# Read current state and target state from task.json
CURRENT_STATE=$(jq -r '.state // "UNKNOWN"' "$TASK_JSON")
TARGET_STATE=$(jq -r '.target_state // "NONE"' "$TASK_JSON")

# If no target state is being set, this is not a transition
if [[ "$TARGET_STATE" == "NONE" || "$TARGET_STATE" == "null" ]]; then
    exit 0
fi

# Validate the transition
if ! validate_state_transition "$CURRENT_STATE" "$TARGET_STATE"; then
    exit 1
fi

# All validations passed
exit 0
