#!/bin/bash
set -euo pipefail

# Hook: measure-phase-metrics.sh
# Purpose: Track metrics for each protocol phase by monitoring tool usage and state transitions
# Trigger: ToolUse events (monitors Edit tool for lock file state updates)
#
# BEHAVIOR:
# - Detects lock file state transitions via Edit tool on /workspace/locks/*.json
# - Records phase entry timestamp, tool usage count, and duration
# - Generates phase summary report on CLEANUP state
# - Enables data-driven protocol optimization analysis

# Parse hook input
INPUT=$(cat)
EVENT_NAME=$(echo "$INPUT" | jq -r '.eventName // empty')

# Only process ToolUse events
if [ "$EVENT_NAME" != "ToolUse" ]; then
  exit 0
fi

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // empty')
TOOL_PARAMS=$(echo "$INPUT" | jq -r '.tool.parameters // {}')

# Check if this is an Edit on a lock file
if [ "$TOOL_NAME" != "Edit" ]; then
  exit 0
fi

FILE_PATH=$(echo "$TOOL_PARAMS" | jq -r '.file_path // empty')

# Only track lock file edits in /workspace/locks/*.json
if [[ ! "$FILE_PATH" =~ ^/workspace/locks/.*\.json$ ]]; then
  exit 0
fi

# Extract task name from lock file path
TASK_NAME=$(basename "$FILE_PATH" .json)

# Parse new state from edit content
NEW_STRING=$(echo "$TOOL_PARAMS" | jq -r '.new_string // empty')
NEW_STATE=$(echo "$NEW_STRING" | jq -r '.state // empty' 2>/dev/null)

# If no state change detected, exit
if [ -z "$NEW_STATE" ]; then
  exit 0
fi

# Metrics directory setup
METRICS_DIR="/workspace/.protocol-metrics"
mkdir -p "$METRICS_DIR"
METRICS_FILE="$METRICS_DIR/${TASK_NAME}-phases.jsonl"

# Record phase transition
TIMESTAMP=$(date -u +%Y-%m-%dT%H:%M:%SZ)
TIMESTAMP_EPOCH=$(date +%s)

# Append phase entry
jq -n \
  --arg task "$TASK_NAME" \
  --arg phase "$NEW_STATE" \
  --arg timestamp "$TIMESTAMP" \
  --arg timestamp_epoch "$TIMESTAMP_EPOCH" \
  --arg session "$SESSION_ID" \
  '{
    "task": $task,
    "phase": $phase,
    "timestamp": $timestamp,
    "timestamp_epoch": ($timestamp_epoch | tonumber),
    "session_id": $session
  }' >> "$METRICS_FILE"

# If transitioning to CLEANUP, calculate phase summary
if [ "$NEW_STATE" = "CLEANUP" ]; then
  # Calculate phase durations
  SUMMARY_FILE="$METRICS_DIR/${TASK_NAME}-summary.json"

  # Read all phase entries and calculate durations
  jq -s '
    group_by(.phase) |
    map({
      phase: .[0].phase,
      start_time: .[0].timestamp,
      start_epoch: .[0].timestamp_epoch,
      end_time: .[-1].timestamp,
      end_epoch: .[-1].timestamp_epoch,
      duration_seconds: (.[-1].timestamp_epoch - .[0].timestamp_epoch),
      entry_count: length
    }) |
    {
      task: (.[0] | if length > 0 then .[0].task else "unknown" end),
      session_id: (.[0] | if length > 0 then .[0].session_id else "unknown" end),
      total_phases: length,
      phases: .,
      total_duration_seconds: (map(.duration_seconds) | add),
      measured_at: (now | strftime("%Y-%m-%dT%H:%M:%SZ"))
    }
  ' "$METRICS_FILE" > "$SUMMARY_FILE"

  # Generate summary message
  TOTAL_DURATION=$(jq -r '.total_duration_seconds // 0' "$SUMMARY_FILE")
  TOTAL_PHASES=$(jq -r '.total_phases // 0' "$SUMMARY_FILE")

  # Format duration (seconds to minutes:seconds)
  DURATION_MIN=$((TOTAL_DURATION / 60))
  DURATION_SEC=$((TOTAL_DURATION % 60))

  MESSAGE="## 📊 PHASE METRICS SUMMARY

**Task**: $TASK_NAME
**Total Phases**: $TOTAL_PHASES
**Total Duration**: ${DURATION_MIN}m ${DURATION_SEC}s

**Phase Breakdown**:
$(jq -r '.phases[] | "  - \(.phase): \(.duration_seconds)s (\(.entry_count) transitions)"' "$SUMMARY_FILE")

**Metrics Files**:
- Phase log: $METRICS_FILE
- Summary: $SUMMARY_FILE

**Analysis Available**:
\`\`\`bash
# View complete phase timeline
jq '.' $METRICS_FILE

# View summary statistics
jq '.' $SUMMARY_FILE

# Compare to baseline (1,381 messages efficient session)
# Identify bottleneck phases
jq '.phases | sort_by(.duration_seconds) | reverse | .[0:3]' $SUMMARY_FILE
\`\`\`"

  # Output metrics summary
  jq -n \
    --arg event "ToolUse" \
    --arg context "$MESSAGE" \
    '{
      "hookSpecificOutput": {
        "hookEventName": $event,
        "additionalContext": $context
      }
    }'
fi

exit 0
