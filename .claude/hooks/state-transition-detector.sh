#!/bin/bash
# Detects state transitions and invokes phase-specific guidance
# Triggered on every UserPromptSubmit via .claude/settings.json
# Performance: ~3ms (quick lock file check + conditional hook invocation)

set -euo pipefail

# Find all active tasks owned by this session
SESSION_ID_FILE="/workspace/tasks/session-id.txt"

# Get current session ID
if [[ -f "$SESSION_ID_FILE" ]]; then
	SESSION_ID=$(cat "$SESSION_ID_FILE")
else
	# No session tracking yet, exit silently
	exit 0
fi

# Find the hook script directory (works from any location)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Find all lock files for this session
TASKS_DIR="/workspace/tasks"
if [[ ! -d "$TASKS_DIR" ]]; then
	exit 0
fi

# Check each task directory for locks owned by this session
for task_dir in "$TASKS_DIR"/*; do
	if [[ ! -d "$task_dir" ]]; then
		continue
	fi

	LOCK_FILE="${task_dir}/task.json"
	if [[ ! -f "$LOCK_FILE" ]]; then
		continue
	fi

	# Check if this task is owned by current session
	LOCK_SESSION=$(jq -r '.session_id // ""' "$LOCK_FILE" 2>/dev/null || echo "")
	if [[ "$LOCK_SESSION" != "$SESSION_ID" ]]; then
		continue
	fi

	# This task is owned by us - check for state transition
	TASK_NAME=$(basename "$task_dir")
	CURRENT_STATE=$(jq -r '.state // "UNKNOWN"' "$LOCK_FILE")

	# Check previous state from tracker file
	STATE_TRACKER="${task_dir}/.last-notified-state"
	if [[ -f "$STATE_TRACKER" ]]; then
		PREVIOUS_STATE=$(cat "$STATE_TRACKER")
	else
		PREVIOUS_STATE="NONE"
	fi

	# If state changed, invoke phase guidance
	if [[ "$CURRENT_STATE" != "$PREVIOUS_STATE" ]]; then
		# Update tracker
		echo "$CURRENT_STATE" > "$STATE_TRACKER"

		# Invoke phase-specific guidance
		HOOK_SCRIPT="${SCRIPT_DIR}/phase-transition-guide.sh"
		if [[ -x "$HOOK_SCRIPT" ]]; then
			"$HOOK_SCRIPT" "$TASK_NAME" "$CURRENT_STATE"
		fi
	fi
done
