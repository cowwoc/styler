#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in load-todo.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Load todo.md tasks into TodoWrite list before first TodoWrite tool usage
# This script provides context to automatically initialize the TodoWrite tool
#
# Performance optimizations:
# - Matcher-based invocation (only runs for TodoWrite tool)
# - TTL-based caching of grep results (30s cache)
# - Cache validation against todo.md modification time

# Parse hook input - session_id passed via JSON stdin
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Require session ID - fail fast if not provided
if [[ -z "$SESSION_ID" ]]; then
	echo "ERROR: session_id must be provided in hook JSON input" >&2
	exit 1
fi

STATE_FILE="/tmp/claude_todowrite_loaded_${SESSION_ID}"
CACHE_FILE="/tmp/claude_todo_cache_${SESSION_ID}"
CACHE_TTL=30  # seconds

# Get the directory containing this script and go up two levels to find todo.md
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TODO_FILE="$SCRIPT_DIR/../todo.md"

# Check if we've already loaded todo.md for TodoWrite in this session
if [[ -f "$STATE_FILE" ]]; then
	# Already loaded, return empty response
	echo '{}'
	exit 0
fi

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

	# Try to use cached counts if valid
	cache_valid=false
	if [[ -f "$CACHE_FILE" ]]; then
		# Check cache age
		cache_age=$(( $(date +%s) - $(stat -c %Y "$CACHE_FILE" 2>/dev/null || echo 0) ))

		# Check if todo.md has been modified since cache was created
		if [[ -f "$TODO_FILE" ]]; then
			todo_mtime=$(stat -c %Y "$TODO_FILE")
			cache_mtime=$(stat -c %Y "$CACHE_FILE")

			if [[ $cache_age -lt $CACHE_TTL ]] && [[ $cache_mtime -ge $todo_mtime ]]; then
				cache_valid=true
			fi
		fi
	fi

	if [[ "$cache_valid" == "true" ]]; then
		# Use cached values
		read -r total_tasks completed_tasks pending_tasks < "$CACHE_FILE"

		# Validate cache read - if any variable is empty, recompute
		if [[ -z "$total_tasks" ]] || [[ -z "$completed_tasks" ]] || [[ -z "$pending_tasks" ]]; then
			cache_valid=false
		fi
	fi

	if [[ "$cache_valid" != "true" ]]; then
		# Count tasks (grep -c returns 0 if no matches, but exit code might be 1, so use || true)
		total_tasks=$(grep -c "^- \[[x ]\]" "$TODO_FILE" || echo "0")
		completed_tasks=$(grep -c "^- \[x\]" "$TODO_FILE" || echo "0")
		pending_tasks=$(grep -c "^- \[ \]" "$TODO_FILE" || echo "0")

		# Cache the results
		echo "$total_tasks $completed_tasks $pending_tasks" > "$CACHE_FILE"
	fi

	# Create context message
	context="Auto-loading TODO tasks from $TODO_FILE: Found $total_tasks tasks ($completed_tasks completed, $pending_tasks pending). IMPORTANT: You must immediately use the TodoWrite tool to load all tasks from $TODO_FILE by converting each '- [x]' task to 'completed' status and each '- [ ]' task to 'pending' status. This ensures session continuity as required by claude.md."

echo "{
	\"hookSpecificOutput\": {
		\"hookEventName\": \"PreToolUse\",
		\"additionalContext\": \"$context\"
	}
}"
