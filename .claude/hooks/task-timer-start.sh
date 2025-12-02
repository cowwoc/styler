#!/bin/bash
#
# Task Timer Start (PreToolUse Hook)
#
# Records start time when Task tool begins execution.
# Paired with task-performance-monitor.sh (PostToolUse) which calculates duration.
#
# Purpose: Track actual Task tool execution time for performance monitoring

set -euo pipefail
trap 'echo "ERROR in task-timer-start.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read stdin (JSON with tool_name, tool_input)
STDIN=$(cat)

# Extract tool name
TOOL_NAME=$(echo "$STDIN" | jq -r '.tool_name // empty' 2>/dev/null)

# Only track Task tool invocations
if [[ "$TOOL_NAME" != "Task" ]]; then
    exit 0
fi

# Extract subagent type
SUBAGENT_TYPE=$(echo "$STDIN" | jq -r '.tool_input.subagent_type // "unknown"' 2>/dev/null)

# Record start timestamp (seconds since epoch)
START_TIME=$(date +%s)

# Write timestamp and subagent type to temp file
# Use START_TIME as part of filename to handle multiple concurrent tasks
TIMER_FILE="/tmp/task_timer_${START_TIME}.tmp"
echo "START_TIME=$START_TIME" > "$TIMER_FILE"
echo "SUBAGENT_TYPE=$SUBAGENT_TYPE" >> "$TIMER_FILE"

# Non-blocking (allow task to proceed)
exit 0
