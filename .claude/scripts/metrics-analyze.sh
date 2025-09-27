#!/bin/bash
# Simplified Workflow Metrics Analysis
# Focus on timing, success rates, and behavioral patterns

# Configuration
METRICS_ROOT="/workspace/.claude/metrics"
METRICS_FILE="${1:-$METRICS_ROOT/workflow_metrics.jsonl}"

# Ensure metrics file exists
if [ ! -f "$METRICS_FILE" ]; then
    echo "No metrics data found at: $METRICS_FILE"
    exit 1
fi

# Workflow timing analysis
analyze_timing() {
    echo "=== Workflow Timing Analysis ==="
    
    # Session duration by test environment
    echo "Average session duration by test type:"
    jq -r 'select(.event=="session_start") | "\(.data.worktree) \(.timestamp)"' "$METRICS_FILE" | \
    awk '{worktree[$1]++; if(worktree[$1]==1) start_time[$1]=$2} END {for(w in worktree) print w": "worktree[w]" sessions"}'
    
    # Task completion patterns
    echo -e "\nTask completion events:"
    jq -r 'select(.event=="task_timing") | "\(.data.event) \(.data.task_identifier) \(.worktree)"' "$METRICS_FILE" | \
    sort | uniq -c
    
    # Workflow outcomes
    echo -e "\nWorkflow success rates:"
    jq -r 'select(.event=="workflow_outcome") | "\(.data.outcome) \(.worktree)"' "$METRICS_FILE" | \
    sort | uniq -c
}

# Protocol performance comparison
analyze_protocol_performance() {
    echo "=== Protocol Performance Comparison ==="
    
    # Protocol usage statistics
    echo "Protocol execution counts:"
    jq -r 'select(.event=="protocol_execution") | "\(.data.protocol_version)"' "$METRICS_FILE" | \
    sort | uniq -c | sort -nr
    
    # Average agent selection metrics by protocol
    echo -e "\nAgent selection efficiency by protocol:"
    for protocol in V2; do
        if jq -e "select(.event==\"protocol_execution\" and .data.protocol_version==\"$protocol\")" "$METRICS_FILE" >/dev/null; then
            avg_agents=$(jq -r "select(.event==\"protocol_execution\" and .data.protocol_version==\"$protocol\" and .data.phase==\"agent_selection\") | .data.agent_count" "$METRICS_FILE" | awk '{sum+=$1; count++} END {if(count>0) print sum/count; else print "N/A"}')
            avg_selection_time=$(jq -r "select(.event==\"protocol_execution\" and .data.protocol_version==\"$protocol\" and .data.phase==\"agent_selection\") | .data.selection_time_seconds" "$METRICS_FILE" | awk '{if($1!="null") {sum+=$1; count++}} END {if(count>0) print sum/count; else print "N/A"}')
            echo "$protocol: ${avg_agents} avg agents, ${avg_selection_time}s avg selection time"
        fi
    done
    
    # Agent selection accuracy comparison
    echo -e "\nAgent selection accuracy by protocol:"
    for protocol in V2; do
        if jq -e "select(.event==\"agent_selection_accuracy\" and .data.protocol_version==\"$protocol\")" "$METRICS_FILE" >/dev/null; then
            avg_precision=$(jq -r "select(.event==\"agent_selection_accuracy\" and .data.protocol_version==\"$protocol\") | .data.precision" "$METRICS_FILE" | awk '{if($1!="null") {sum+=$1; count++}} END {if(count>0) printf "%.1f%%", (sum/count)*100; else print "N/A"}')
            avg_recall=$(jq -r "select(.event==\"agent_selection_accuracy\" and .data.protocol_version==\"$protocol\") | .data.recall" "$METRICS_FILE" | awk '{if($1!="null") {sum+=$1; count++}} END {if(count>0) printf "%.1f%%", (sum/count)*100; else print "N/A"}')
            echo "$protocol: ${avg_precision} precision, ${avg_recall} recall"
        fi
    done
}

# Test environment comparison
analyze_test_environments() {
    echo "=== Test Environment Comparison ==="
    
    # Test type distribution
    echo "Test environments detected:"
    jq -r 'select(.event=="test_environment") | "\(.data.test_type) \(.worktree)"' "$METRICS_FILE" | \
    sort | uniq -c
    
    # Session count by worktree
    echo -e "\nSession activity by worktree:"
    jq -r 'select(.event=="session_start") | .worktree' "$METRICS_FILE" | \
    sort | uniq -c | sort -nr
    
    # Most recent test activity
    echo -e "\nMost recent test sessions:"
    jq -r 'select(.event=="session_start" and (.worktree | test("test-"))) | "\(.timestamp) \(.worktree) \(.data.git_branch)"' "$METRICS_FILE" | \
    tail -10
}

# Behavioral pattern analysis  
analyze_behavior() {
    echo "=== User Behavior Analysis ==="
    
    # Prompt patterns
    echo "User prompt analysis:"
    jq -r 'select(.event=="user_prompt") | "\(.data.contains_task_protocol) \(.data.contains_todo_reference) \(.worktree)"' "$METRICS_FILE" | \
    awk '{if($1=="true") task_protocol++; if($2=="true") todo_ref++; total++} END {
        printf "Task protocol references: %d/%d (%.1f%%)\n", task_protocol, total, task_protocol/total*100
        printf "TODO references: %d/%d (%.1f%%)\n", todo_ref, total, todo_ref/total*100
    }'
    
    # Navigation patterns
    echo -e "\nNavigation events:"
    jq -r 'select(.event=="navigation") | .data.navigation_type' "$METRICS_FILE" | \
    sort | uniq -c
}

# Build and validation analysis
analyze_builds() {
    echo "=== Build & Validation Analysis ==="
    
    # Build events
    local build_events=$(jq -r 'select(.event=="build_event")' "$METRICS_FILE" | wc -l)
    if [ "$build_events" -gt 0 ]; then
        echo "Build events detected:"
        jq -r 'select(.event=="build_event") | "\(.data.build_event) \(.data.build_tool) \(.data.error_count) \(.worktree)"' "$METRICS_FILE"
    else
        echo "No build events captured"
    fi
}

# Performance comparison between test environments
compare_performance() {
    echo "=== Performance Comparison ==="
    
    # Compare session counts
    echo "Test execution comparison:"
    local monolithic_sessions=$(jq -r 'select(.event=="session_start" and (.worktree | test("monolithic")))' "$METRICS_FILE" | wc -l)
    local modular_sessions=$(jq -r 'select(.event=="session_start" and (.worktree | test("modular")))' "$METRICS_FILE" | wc -l)
    
    echo "Monolithic approach sessions: $monolithic_sessions"
    echo "Modular approach sessions: $modular_sessions"
    
    # Compare task timing if available
    local monolithic_completions=$(jq -r 'select(.event=="task_timing" and .data.event=="complete" and (.worktree | test("monolithic")))' "$METRICS_FILE" | wc -l)
    local modular_completions=$(jq -r 'select(.event=="task_timing" and .data.event=="complete" and (.worktree | test("modular")))' "$METRICS_FILE" | wc -l)
    
    echo "Task completions - Monolithic: $monolithic_completions, Modular: $modular_completions"
    
    # Success rate comparison
    local monolithic_success=$(jq -r 'select(.event=="workflow_outcome" and .data.outcome=="success" and (.worktree | test("monolithic")))' "$METRICS_FILE" | wc -l)
    local modular_success=$(jq -r 'select(.event=="workflow_outcome" and .data.outcome=="success" and (.worktree | test("modular")))' "$METRICS_FILE" | wc -l)
    
    echo "Successful workflows - Monolithic: $monolithic_success, Modular: $modular_success"
}

# Generate comprehensive report
generate_workflow_report() {
    echo "=== Simplified Workflow Metrics Report ==="
    echo "Generated: $(date)"
    echo "Metrics file: $METRICS_FILE"
    echo "Total events: $(wc -l < "$METRICS_FILE" 2>/dev/null || echo "0")"
    echo ""
    
    analyze_test_environments
    echo ""
    analyze_timing
    echo ""
    analyze_protocol_performance
    echo ""
    compare_performance
    echo ""
    analyze_behavior
    echo ""
    analyze_builds
}

# Export functions  
export -f analyze_timing
export -f analyze_test_environments
export -f analyze_behavior
export -f compare_performance
export -f generate_workflow_report

# Main execution
case "${2:-report}" in
    "report") generate_workflow_report ;;
    "timing") analyze_timing ;;
    "environments") analyze_test_environments ;;
    "protocols") analyze_protocol_performance ;;
    "behavior") analyze_behavior ;;
    "performance") compare_performance ;;
    "builds") analyze_builds ;;
    *) echo "Usage: $0 [metrics_file] [report|timing|environments|protocols|behavior|performance|builds]" ;;
esac