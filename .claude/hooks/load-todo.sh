#!/bin/bash
set -euo pipefail

trap 'echo "[HOOK DEBUG] load-todo.sh FAILED at line $LINENO" >&2; exit 1' ERR

echo "[HOOK DEBUG] load-todo.sh START" >&2

# Load todo.md tasks into TodoWrite list before first TodoWrite tool usage
# This script provides context to automatically initialize the TodoWrite tool

# Parse hook input - session_id and tool info passed via JSON stdin
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')

# Require session ID - fail fast if not provided
if [[ -z "$SESSION_ID" ]]; then
	echo "ERROR: session_id must be provided in hook JSON input" >&2
	exit 1
fi

STATE_FILE="/tmp/claude_todowrite_loaded_${SESSION_ID}"
# Get the directory containing this script and go up two levels to find todo.md
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TODO_FILE="$SCRIPT_DIR/../todo.md"

# Check if we've already loaded todo.md for TodoWrite in this session
if [[ -f "$STATE_FILE" ]]; then
	# Already loaded, return empty response
	echo '{}'
	exit 0
fi

# Check if this is a TodoWrite tool call
if [[ "$TOOL_NAME" == "TodoWrite" ]]; then
	# Mark that we've loaded todo.md for this session
	touch "$STATE_FILE"
	
	if [[ ! -f "$TODO_FILE" ]]; then
		echo '{
			"hookSpecificOutput": {
				"hookEventName": "PreToolUse",
				"additionalContext": "Auto-loading TODO tasks: todo.md file not found, starting with empty task list."
			}
		}'
		exit 0
	fi

	# Count tasks
	total_tasks=$(grep -c "^- \[[x ]\]" "$TODO_FILE")
	completed_tasks=$(grep -c "^- \[x\]" "$TODO_FILE")
	pending_tasks=$(grep -c "^- \[ \]" "$TODO_FILE")

	# Create context message
	context="Auto-loading TODO tasks from $TODO_FILE: Found $total_tasks tasks ($completed_tasks completed, $pending_tasks pending). IMPORTANT: You must immediately use the TodoWrite tool to load all tasks from $TODO_FILE by converting each '- [x]' task to 'completed' status and each '- [ ]' task to 'pending' status. This ensures session continuity as required by claude.md."

	echo "{
		\"hookSpecificOutput\": {
			\"hookEventName\": \"PreToolUse\",
			\"additionalContext\": \"$context\"
		}
	}"
else
	# Not a TodoWrite call, return empty response
	echo '{}'
fi

echo "[HOOK DEBUG] load-todo.sh END" >&2
