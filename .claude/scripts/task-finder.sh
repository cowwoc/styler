#!/bin/bash
# Task discovery helper - find task by session ID
# Source this file in hooks to simplify task discovery
#
# Usage:
#   source /workspace/.claude/scripts/task-finder.sh
#   TASK_DIR=$(find_task_by_session "$SESSION_ID")

# Find task directory owned by given session ID
# Args: session_id
# Returns: task directory path or empty string if not found
find_task_by_session() {
	local session_id="$1"
	local tasks_dir="/workspace/tasks"

	if [[ -z "$session_id" ]]; then
		return 1
	fi

	if [[ ! -d "$tasks_dir" ]]; then
		return 1
	fi

	# Check each task directory for locks owned by this session
	for task_dir in "$tasks_dir"/*; do
		if [[ ! -d "$task_dir" ]]; then
			continue
		fi

		local lock_file="${task_dir}/task.json"
		if [[ ! -f "$lock_file" ]]; then
			continue
		fi

		# Check if this task is owned by current session
		local lock_session=$(jq -r '.session_id // ""' "$lock_file" 2>/dev/null || echo "")
		if [[ "$lock_session" == "$session_id" ]]; then
			echo "$task_dir"
			return 0
		fi
	done

	return 1
}

# Get task name from task directory
# Args: task_dir
# Returns: task name (basename of directory)
get_task_name() {
	local task_dir="$1"
	basename "$task_dir"
}

# Get task state from lock file
# Args: task_dir
# Returns: task state or "UNKNOWN"
get_task_state() {
	local task_dir="$1"
	local lock_file="${task_dir}/task.json"

	if [[ ! -f "$lock_file" ]]; then
		echo "UNKNOWN"
		return 1
	fi

	jq -r '.state // "UNKNOWN"' "$lock_file" 2>/dev/null || echo "UNKNOWN"
}

# Find all tasks in given state
# Args: state
# Returns: newline-separated list of task directories
find_tasks_by_state() {
	local target_state="$1"
	local tasks_dir="/workspace/tasks"

	if [[ ! -d "$tasks_dir" ]]; then
		return 1
	fi

	for task_dir in "$tasks_dir"/*; do
		if [[ ! -d "$task_dir" ]]; then
			continue
		fi

		local lock_file="${task_dir}/task.json"
		if [[ ! -f "$lock_file" ]]; then
			continue
		fi

		local state=$(jq -r '.state // ""' "$lock_file" 2>/dev/null || echo "")
		if [[ "$state" == "$target_state" ]]; then
			echo "$task_dir"
		fi
	done
}
