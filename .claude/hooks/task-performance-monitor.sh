#!/bin/bash
#
# Task Performance Monitor (PostToolUse Hook)
#
# Monitors Task tool execution time and alerts when threshold exceeded.
# Paired with task-timer-start.sh (PreToolUse) which records start time.
#
# Performance optimizations:
# - Uses parameter expansion instead of jq where possible
# - Minimal file operations
# - Early exit for non-Task tools

set -euo pipefail
trap 'echo "ERROR in task-performance-monitor.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read stdin
STDIN=$(cat)

# Quick check: skip if not Task tool (string match before JSON parse)
[[ "$STDIN" != *'"tool_name":"Task"'* && "$STDIN" != *'"tool_name": "Task"'* ]] && exit 0

# Extract subagent type using grep (faster than jq for simple extraction)
SUBAGENT_TYPE=$(echo "$STDIN" | grep -oP '"subagent_type"\s*:\s*"\K[^"]+' 2>/dev/null || echo "unknown")

# Find most recent timer file (handle case where no files exist)
TIMER_FILE=""
if compgen -G "/tmp/task_timer_*.tmp" > /dev/null 2>&1; then
    TIMER_FILE=$(ls -t /tmp/task_timer_*.tmp 2>/dev/null | head -1)
fi

if [[ -n "$TIMER_FILE" ]]; then
    # Read start time from timer file
    source "$TIMER_FILE"
    rm -f "$TIMER_FILE"

    # Calculate duration
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
else
    # No timer file - try to extract from JSON (fallback)
    DURATION_MS=$(echo "$STDIN" | grep -oP '"totalDurationMs"\s*:\s*\K[0-9]+' 2>/dev/null || echo "0")
    DURATION=$((DURATION_MS / 1000))
fi

# Threshold: 180 seconds (3 minutes)
[[ $DURATION -lt 180 ]] && exit 0

# Calculate human-readable format
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))
OVERAGE=$((DURATION - 180))

# Alert: Task exceeded threshold
cat >&2 << EOF

======================================================================
⚠️  PERFORMANCE ALERT: Task Exceeded 3-Minute Threshold
======================================================================

Task: $SUBAGENT_TYPE
Duration: ${MINUTES}m ${SECONDS}s (${DURATION}s)
Threshold: 180s | Overage: ${OVERAGE}s

ANALYZE FOR:
  ❌ For-loops instead of batch operations
  ❌ Sequential operations that could be parallel
  ❌ Repeated reads of same data
  ❌ Excessive tool calls

REPORT TO USER:
  "The ${SUBAGENT_TYPE} agent completed in ${MINUTES}m ${SECONDS}s."

======================================================================

EOF

exit 0
