#!/bin/bash
# Hook execution logging helper
# Source this file in hooks to enable standardized logging
#
# Usage:
#   source "$(dirname "$0")/hook-logger.sh"
#   log_hook_execution "hook-name" "trigger" "result" "details"
#
# Log Format: [timestamp] [hook-name] [trigger] [result] [details]
# Log Location: /workspace/tasks/{task-name}/hook-execution.log (if task context)
#              /tmp/global-hook-execution.log (if no task context)

# Find hook log file location based on session context
find_hook_log_file() {
	local SESSION_ID="${1:-}"

	if [[ -z "$SESSION_ID" ]]; then
		echo "/tmp/global-hook-execution.log"
		return
	fi

	# Find task owned by this session
	local TASKS_DIR="/workspace/tasks"
	if [[ ! -d "$TASKS_DIR" ]]; then
		echo "/tmp/global-hook-execution.log"
		return
	fi

	for task_dir in "$TASKS_DIR"/*; do
		if [[ ! -d "$task_dir" ]]; then
			continue
		fi

		local LOCK_FILE="${task_dir}/task.json"
		if [[ ! -f "$LOCK_FILE" ]]; then
			continue
		fi

		local LOCK_SESSION=$(jq -r '.session_id // ""' "$LOCK_FILE" 2>/dev/null || echo "")
		if [[ "$LOCK_SESSION" == "$SESSION_ID" ]]; then
			echo "${task_dir}/hook-execution.log"
			return
		fi
	done

	echo "/tmp/global-hook-execution.log"
}

# Log hook execution
# Args: hook_name trigger result [details]
log_hook_execution() {
	local HOOK_NAME="${1:-unknown}"
	local TRIGGER="${2:-unknown}"
	local RESULT="${3:-unknown}"
	local DETAILS="${4:-}"

	# Read session ID from environment or stdin
	local SESSION_ID="${HOOK_SESSION_ID:-}"
	if [[ -z "$SESSION_ID" ]] && [[ -n "${HOOK_INPUT:-}" ]]; then
		SESSION_ID=$(echo "$HOOK_INPUT" | jq -r '.session_id // empty' 2>/dev/null || echo "")
	fi

	local LOG_FILE=$(find_hook_log_file "$SESSION_ID")
	local TIMESTAMP=$(date -Iseconds)

	# Create log directory if needed
	mkdir -p "$(dirname "$LOG_FILE")"

	# Write log entry
	if [[ -n "$DETAILS" ]]; then
		echo "[$TIMESTAMP] [$HOOK_NAME] [$TRIGGER] [$RESULT] $DETAILS" >> "$LOG_FILE"
	else
		echo "[$TIMESTAMP] [$HOOK_NAME] [$TRIGGER] [$RESULT]" >> "$LOG_FILE"
	fi
}

# Log hook start
log_hook_start() {
	local HOOK_NAME="${1:-unknown}"
	local TRIGGER="${2:-unknown}"
	log_hook_execution "$HOOK_NAME" "$TRIGGER" "START"
}

# Log hook success
log_hook_success() {
	local HOOK_NAME="${1:-unknown}"
	local TRIGGER="${2:-unknown}"
	local DETAILS="${3:-}"
	log_hook_execution "$HOOK_NAME" "$TRIGGER" "SUCCESS" "$DETAILS"
}

# Log hook failure
log_hook_failure() {
	local HOOK_NAME="${1:-unknown}"
	local TRIGGER="${2:-unknown}"
	local DETAILS="${3:-}"
	log_hook_execution "$HOOK_NAME" "$TRIGGER" "FAILURE" "$DETAILS"
}

# Log hook blocked action
log_hook_blocked() {
	local HOOK_NAME="${1:-unknown}"
	local TRIGGER="${2:-unknown}"
	local DETAILS="${3:-}"
	log_hook_execution "$HOOK_NAME" "$TRIGGER" "BLOCKED" "$DETAILS"
}

# Log hook warning
log_hook_warning() {
	local HOOK_NAME="${1:-unknown}"
	local TRIGGER="${2:-unknown}"
	local DETAILS="${3:-}"
	log_hook_execution "$HOOK_NAME" "$TRIGGER" "WARNING" "$DETAILS"
}
