#!/bin/bash
# Per-Agent Performance Tracker
#
# ADDED: 2025-12-01
# PURPOSE: Track agent execution metrics including:
#          - Execution time per agent type
#          - Tool call counts
#          - Efficiency patterns
#
# Trigger: PostToolUse on Task tool
# Output: /tmp/agent_performance_${SESSION_ID}.json

set -euo pipefail
trap 'echo "ERROR in agent-performance-tracker.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read stdin
INPUT=$(cat)

# Extract info
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // .tool.name // empty' 2>/dev/null || echo "")

# Only track Task tool
if [[ "$TOOL_NAME" != "Task" ]]; then
    exit 0
fi

SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty' 2>/dev/null || echo "")
AGENT_TYPE=$(echo "$INPUT" | jq -r '.tool_input.subagent_type // .tool.input.subagent_type // "unknown"' 2>/dev/null || echo "unknown")
DURATION_MS=$(echo "$INPUT" | jq -r '.tool_result.durationMs // .durationMs // 0' 2>/dev/null || echo "0")
RESULT_LENGTH=$(echo "$INPUT" | jq -r '.tool_result.output // .result // "" | length' 2>/dev/null || echo "0")

TIMESTAMP=$(date -Iseconds)
PERF_FILE="/tmp/agent_performance_${SESSION_ID}.json"

# Initialize if needed
if [[ ! -f "$PERF_FILE" ]]; then
    echo '{
  "session_id": "'$SESSION_ID'",
  "started": "'$TIMESTAMP'",
  "agents": {},
  "invocations": []
}' > "$PERF_FILE"
fi

# Calculate duration in seconds
DURATION_SEC=$((DURATION_MS / 1000))

# Add invocation record
jq --arg agent "$AGENT_TYPE" \
   --arg ts "$TIMESTAMP" \
   --arg duration "$DURATION_SEC" \
   --arg result_len "$RESULT_LENGTH" \
   '.invocations += [{
     "agent": $agent,
     "timestamp": $ts,
     "duration_sec": ($duration | tonumber),
     "result_length": ($result_len | tonumber)
   }]' "$PERF_FILE" > "${PERF_FILE}.tmp" && mv "${PERF_FILE}.tmp" "$PERF_FILE"

# Update agent aggregate stats
jq --arg agent "$AGENT_TYPE" \
   --arg duration "$DURATION_SEC" \
   '
   .agents[$agent] = (
     .agents[$agent] // {
       "invocation_count": 0,
       "total_duration_sec": 0,
       "avg_duration_sec": 0,
       "max_duration_sec": 0,
       "min_duration_sec": 999999
     }
   ) |
   .agents[$agent].invocation_count += 1 |
   .agents[$agent].total_duration_sec += ($duration | tonumber) |
   .agents[$agent].avg_duration_sec = (.agents[$agent].total_duration_sec / .agents[$agent].invocation_count) |
   .agents[$agent].max_duration_sec = ([.agents[$agent].max_duration_sec, ($duration | tonumber)] | max) |
   .agents[$agent].min_duration_sec = ([.agents[$agent].min_duration_sec, ($duration | tonumber)] | min)
   ' "$PERF_FILE" > "${PERF_FILE}.tmp" && mv "${PERF_FILE}.tmp" "$PERF_FILE"

# Alert if agent took too long (> 3 minutes)
if [[ $DURATION_SEC -gt 180 ]]; then
    MINUTES=$((DURATION_SEC / 60))
    SECONDS=$((DURATION_SEC % 60))

    cat >&2 << EOF

⚠️ AGENT PERFORMANCE ALERT
───────────────────────────────────────────────────────────────────────────────

Agent: $AGENT_TYPE
Duration: ${MINUTES}m ${SECONDS}s (${DURATION_SEC}s)
Threshold: 180s exceeded

POTENTIAL ISSUES:
  • Sequential operations that could be parallelized
  • Excessive tool calls
  • Repeated reads of same data
  • Missing batch operations

METRICS FILE: $PERF_FILE

───────────────────────────────────────────────────────────────────────────────

EOF
fi

# Show summary every 5 invocations
TOTAL_INVOCATIONS=$(jq '.invocations | length' "$PERF_FILE")
if [[ $((TOTAL_INVOCATIONS % 5)) -eq 0 && $TOTAL_INVOCATIONS -gt 0 ]]; then
    cat >&2 << EOF

📊 AGENT PERFORMANCE SUMMARY (Session: ${SESSION_ID:0:8}...)
───────────────────────────────────────────────────────────────────────────────
$(jq -r '.agents | to_entries | map("\(.key): \(.value.invocation_count) calls, avg \(.value.avg_duration_sec | floor)s, total \(.value.total_duration_sec)s") | .[]' "$PERF_FILE" 2>/dev/null || echo "No data")
───────────────────────────────────────────────────────────────────────────────

EOF
fi

exit 0
