#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in state-transition.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

# Usage: state-transition.sh TASK_NAME TO_STATE

if [ $# -lt 2 ]; then
    json_error "Usage: state-transition.sh TASK_NAME TO_STATE"
fi

TASK_NAME="$1"
TO_STATE="$2"
TASK_DIR="/workspace/tasks/$TASK_NAME"
TIMESTAMP=$(date -Iseconds)

# Verify task exists
[ -f "$TASK_DIR/task.json" ] || json_error "task.json not found: $TASK_DIR/task.json"

# Get current state
FROM_STATE=$(jq -r '.state' "$TASK_DIR/task.json")

# Get last transition timestamp for duration calculation
LAST_TRANSITION=$(jq -r '.transition_log[-1].timestamp // .created' "$TASK_DIR/task.json")
if [ -n "$LAST_TRANSITION" ] && [ "$LAST_TRANSITION" != "null" ]; then
    LAST_TS=$(date -d "$LAST_TRANSITION" +%s)
    CURRENT_TS=$(date +%s)
    DURATION_SECONDS=$((CURRENT_TS - LAST_TS))
else
    DURATION_SECONDS=0
fi

# Update state with transition record
# FIXED: Use .transition_log field (not .transitions) to match require-task-protocol.sh validation
jq --arg from "$FROM_STATE" --arg to "$TO_STATE" --arg ts "$TIMESTAMP" --arg dur "$DURATION_SECONDS" \
   '.state = $to | .transition_log += [{"from": $from, "to": $to, "timestamp": $ts, "duration_seconds": ($dur | tonumber)}]' \
   "$TASK_DIR/task.json" > "$TASK_DIR/task.json.tmp"
mv "$TASK_DIR/task.json.tmp" "$TASK_DIR/task.json"

# Verify state updated
NEW_STATE=$(jq -r '.state' "$TASK_DIR/task.json")
if [ "$NEW_STATE" != "$TO_STATE" ]; then
    json_error "State transition failed. Expected: $TO_STATE, Got: $NEW_STATE"
fi

json_success "task_name=$TASK_NAME" "from_state=$FROM_STATE" "to_state=$TO_STATE" "duration_in_previous_state=$DURATION_SECONDS"
