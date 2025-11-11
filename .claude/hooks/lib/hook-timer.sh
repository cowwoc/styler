#!/bin/bash
# Hook execution timing instrumentation library
# Source this file in hooks to enable performance measurement
#
# Usage:
#   source "$(dirname "$0")/lib/hook-timer.sh"
#   timer_start "hook-name"
#   # ... hook logic ...
#   timer_checkpoint "validation-complete"
#   # ... more logic ...
#   timer_end "hook-name"
#
# Timing Format: [timestamp] [hook-name] [checkpoint] [duration_ms] [cumulative_ms]
# Timing Location: /workspace/.metrics/hook-timings.log

# Global timing variables
declare -g TIMER_START_EPOCH_MS=0
declare -g TIMER_LAST_CHECKPOINT_MS=0
declare -g TIMER_HOOK_NAME=""
declare -g TIMER_METRICS_DIR="/workspace/.metrics"

# Get current time in milliseconds
_timer_now_ms() {
    echo $(($(date +%s%N) / 1000000))
}

# Initialize metrics directory
_timer_init_metrics_dir() {
    if [[ ! -d "$TIMER_METRICS_DIR" ]]; then
        mkdir -p "$TIMER_METRICS_DIR" 2>/dev/null || true
    fi
}

# Start timing a hook
# Args: hook_name
timer_start() {
    local hook_name="${1:-unknown}"
    TIMER_HOOK_NAME="$hook_name"
    TIMER_START_EPOCH_MS=$(_timer_now_ms)
    TIMER_LAST_CHECKPOINT_MS=$TIMER_START_EPOCH_MS
    _timer_init_metrics_dir
}

# Record a timing checkpoint
# Args: checkpoint_name [details]
timer_checkpoint() {
    local checkpoint="${1:-checkpoint}"
    local details="${2:-}"

    if [[ $TIMER_START_EPOCH_MS -eq 0 ]]; then
        return  # Timer not started
    fi

    local now_ms=$(_timer_now_ms)
    local duration_ms=$((now_ms - TIMER_LAST_CHECKPOINT_MS))
    local cumulative_ms=$((now_ms - TIMER_START_EPOCH_MS))

    local log_file="${TIMER_METRICS_DIR}/hook-timings.log"
    local timestamp=$(date -Iseconds)

    if [[ -n "$details" ]]; then
        echo "[$timestamp] [$TIMER_HOOK_NAME] [$checkpoint] ${duration_ms}ms (total: ${cumulative_ms}ms) - $details" >> "$log_file"
    else
        echo "[$timestamp] [$TIMER_HOOK_NAME] [$checkpoint] ${duration_ms}ms (total: ${cumulative_ms}ms)" >> "$log_file"
    fi

    TIMER_LAST_CHECKPOINT_MS=$now_ms
}

# End timing and record total duration
# Args: hook_name [result]
timer_end() {
    local hook_name="${1:-$TIMER_HOOK_NAME}"
    local result="${2:-SUCCESS}"

    if [[ $TIMER_START_EPOCH_MS -eq 0 ]]; then
        return  # Timer not started
    fi

    local now_ms=$(_timer_now_ms)
    local total_ms=$((now_ms - TIMER_START_EPOCH_MS))

    local log_file="${TIMER_METRICS_DIR}/hook-timings.log"
    local timestamp=$(date -Iseconds)

    echo "[$timestamp] [$hook_name] [COMPLETE] ${total_ms}ms [$result]" >> "$log_file"

    # Also write to summary file for easier aggregation
    local summary_file="${TIMER_METRICS_DIR}/hook-summary.jsonl"
    printf '{"timestamp":"%s","hook":"%s","duration_ms":%d,"result":"%s"}\n' \
        "$timestamp" "$hook_name" "$total_ms" "$result" >> "$summary_file"

    # Reset timer state
    TIMER_START_EPOCH_MS=0
    TIMER_LAST_CHECKPOINT_MS=0
    TIMER_HOOK_NAME=""
}

# Get timing statistics for a specific hook
# Args: hook_name
timer_stats() {
    local hook_name="${1:-}"
    local summary_file="${TIMER_METRICS_DIR}/hook-summary.jsonl"

    if [[ ! -f "$summary_file" ]]; then
        echo "No timing data available"
        return
    fi

    if [[ -n "$hook_name" ]]; then
        # Stats for specific hook
        jq -s --arg hook "$hook_name" '
            map(select(.hook == $hook)) |
            {
                hook: $hook,
                count: length,
                total_ms: (map(.duration_ms) | add),
                avg_ms: (map(.duration_ms) | add / length),
                min_ms: (map(.duration_ms) | min),
                max_ms: (map(.duration_ms) | max),
                median_ms: (map(.duration_ms) | sort | if length % 2 == 0 then .[length/2] else .[(length-1)/2] end)
            }
        ' "$summary_file"
    else
        # Stats for all hooks
        jq -s '
            group_by(.hook) |
            map({
                hook: .[0].hook,
                count: length,
                total_ms: (map(.duration_ms) | add),
                avg_ms: (map(.duration_ms) | add / length),
                min_ms: (map(.duration_ms) | min),
                max_ms: (map(.duration_ms) | max)
            }) |
            sort_by(.total_ms) | reverse
        ' "$summary_file"
    fi
}

# Clear timing data (for testing)
timer_clear() {
    rm -f "${TIMER_METRICS_DIR}/hook-timings.log"
    rm -f "${TIMER_METRICS_DIR}/hook-summary.jsonl"
}

# Export timing data as report
timer_report() {
    local report_file="${TIMER_METRICS_DIR}/timing-report.md"

    cat > "$report_file" <<'EOF'
# Hook Performance Report

## Summary Statistics

Top 10 hooks by total execution time:

```
EOF

    timer_stats | jq -r '.[:10] | .[] |
        "\(.hook): \(.total_ms)ms total, \(.avg_ms)ms avg (\(.count) executions)"' >> "$report_file"

    cat >> "$report_file" <<'EOF'
```

## Detailed Statistics

```
EOF

    timer_stats | jq '.' >> "$report_file"

    cat >> "$report_file" <<'EOF'
```

## Raw Timing Log

See: /workspace/.metrics/hook-timings.log
See: /workspace/.metrics/hook-summary.jsonl
EOF

    echo "Report written to: $report_file"
}
