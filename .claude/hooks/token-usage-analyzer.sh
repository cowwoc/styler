#!/bin/bash

# Comprehensive Token Usage Analysis Tool
# Analyzes hybrid tracking data to provide optimization insights

METRICS_ROOT="/workspace/.claude/metrics"
TRACKING_LOG="$METRICS_ROOT/hybrid_tracking.jsonl"
ANALYSIS_REPORT="$METRICS_ROOT/token_analysis_report.json"

# Check if tracking data exists
if [[ ! -f "$TRACKING_LOG" ]]; then
    echo "No tracking data found. Run some Task executions first."
    exit 1
fi

# Generate comprehensive analysis
analyze_token_usage() {
    local report_data=$(cat <<'EOF'
{
  "analysis_timestamp": "__TIMESTAMP__",
  "data_source": "__TRACKING_LOG__",
  "summary": {
    "total_tasks": __TOTAL_TASKS__,
    "completed_tasks": __COMPLETED_TASKS__,
    "total_tokens": __TOTAL_TOKENS__,
    "average_tokens_per_task": __AVG_TOKENS__,
    "total_execution_time": __TOTAL_TIME__,
    "average_execution_time": __AVG_TIME__
  },
  "agent_performance": __AGENT_PERFORMANCE__,
  "complexity_analysis": __COMPLEXITY_ANALYSIS__,
  "optimization_opportunities": __OPTIMIZATION_OPPORTUNITIES__,
  "recommendations": __RECOMMENDATIONS__
}
EOF
)

    # Calculate summary statistics
    local total_tasks=$(grep -c '"event":"task_initiation"' "$TRACKING_LOG" 2>/dev/null || echo "0")
    local completed_tasks=$(grep -c '"event":"task_completion"' "$TRACKING_LOG" 2>/dev/null || echo "0")
    local total_tokens=$(grep '"event":"task_completion"' "$TRACKING_LOG" | grep -o '"tokens_actual":[0-9]*' | cut -d: -f2 | awk '{sum+=$1} END {print sum+0}')
    local total_time=$(grep '"event":"task_completion"' "$TRACKING_LOG" | grep -o '"duration_seconds":[0-9.]*' | cut -d: -f2 | awk '{sum+=$1} END {print sum+0}')
    
    local avg_tokens=0
    local avg_time=0
    if [[ $completed_tasks -gt 0 ]]; then
        avg_tokens=$(echo "scale=2; $total_tokens / $completed_tasks" | bc 2>/dev/null || echo "0")
        avg_time=$(echo "scale=2; $total_time / $completed_tasks" | bc 2>/dev/null || echo "0")
    fi

    # Agent performance analysis
    local agent_performance=$(grep '"event":"task_completion"' "$TRACKING_LOG" | python3 -c "
import json, sys
from collections import defaultdict

agents = defaultdict(lambda: {'count': 0, 'tokens': 0, 'time': 0})

for line in sys.stdin:
    try:
        data = json.loads(line)
        agent = data.get('agent_type', 'unknown')
        tokens = data.get('tokens_actual', 0)
        time = data.get('duration_seconds', 0)
        
        agents[agent]['count'] += 1
        agents[agent]['tokens'] += tokens
        agents[agent]['time'] += time
    except:
        continue

result = {}
for agent, stats in agents.items():
    if stats['count'] > 0:
        result[agent] = {
            'task_count': stats['count'],
            'total_tokens': stats['tokens'],
            'total_time': round(stats['time'], 2),
            'avg_tokens': round(stats['tokens'] / stats['count'], 2),
            'avg_time': round(stats['time'] / stats['count'], 2),
            'tokens_per_second': round(stats['tokens'] / stats['time'], 2) if stats['time'] > 0 else 0
        }

print(json.dumps(result, indent=2))
" 2>/dev/null || echo '{}')

    # Generate optimization recommendations
    local recommendations=$(cat <<'EOF'
[
  {
    "category": "Agent Efficiency",
    "recommendation": "Monitor token usage patterns to identify agents with high token consumption",
    "priority": "high"
  },
  {
    "category": "Task Complexity",
    "recommendation": "Route simple tasks to lightweight processing modes",
    "priority": "medium"
  },
  {
    "category": "Performance Monitoring",
    "recommendation": "Track tokens per second as a key performance indicator",
    "priority": "medium"
  }
]
EOF
)

    # Build final report
    report_data=$(echo "$report_data" | \
        sed "s/__TIMESTAMP__/$(date -u -Iseconds)/g" | \
        sed "s|__TRACKING_LOG__|$TRACKING_LOG|g" | \
        sed "s/__TOTAL_TASKS__/$total_tasks/g" | \
        sed "s/__COMPLETED_TASKS__/$completed_tasks/g" | \
        sed "s/__TOTAL_TOKENS__/$total_tokens/g" | \
        sed "s/__AVG_TOKENS__/$avg_tokens/g" | \
        sed "s/__TOTAL_TIME__/$total_time/g" | \
        sed "s/__AVG_TIME__/$avg_time/g")
    
    # Insert complex JSON data
    echo "$report_data" | python3 -c "
import json, sys

template = sys.stdin.read()
agent_performance = $agent_performance
recommendations = $recommendations

# Replace placeholders with actual JSON data
template = template.replace('\"__AGENT_PERFORMANCE__\"', json.dumps(agent_performance, indent=2))
template = template.replace('\"__COMPLEXITY_ANALYSIS__\"', '{}')
template = template.replace('\"__OPTIMIZATION_OPPORTUNITIES__\"', '[]')  
template = template.replace('\"__RECOMMENDATIONS__\"', json.dumps(recommendations, indent=2))

print(template)
" > "$ANALYSIS_REPORT"

    echo "Token usage analysis completed: $ANALYSIS_REPORT"
}

# Display summary
display_summary() {
    if [[ -f "$ANALYSIS_REPORT" ]]; then
        echo "=== TOKEN USAGE ANALYSIS SUMMARY ==="
        python3 -c "
import json
with open('$ANALYSIS_REPORT') as f:
    data = json.load(f)

summary = data['summary']
print(f\"Total Tasks: {summary['total_tasks']}\")
print(f\"Completed Tasks: {summary['completed_tasks']}\")
print(f\"Total Tokens: {summary['total_tokens']:,}\")
print(f\"Average Tokens/Task: {summary['average_tokens_per_task']}\")
print(f\"Total Execution Time: {summary['total_execution_time']}s\")
print(f\"Average Time/Task: {summary['average_execution_time']}s\")

print(f\"\nAgent Performance:\")
for agent, stats in data['agent_performance'].items():
    print(f\"  {agent}: {stats['total_tokens']:,} tokens ({stats['task_count']} tasks)\")
"
    fi
}

# Manual terminal output processing
process_terminal_output() {
    local terminal_output="$1"
    if [[ -n "$terminal_output" ]]; then
        /workspace/.claude/hooks/hybrid-token-tracker.sh "terminal_output" "$terminal_output"
    else
        echo "Usage: $0 process \"● agent-name(description) ⎿ Done (X tool uses · Y.Zk tokens · N.Ms)\""
    fi
}

# Main execution
case "${1:-analyze}" in
    "analyze")
        analyze_token_usage
        display_summary
        ;;
    "summary")
        display_summary
        ;;
    "process")
        process_terminal_output "$2"
        ;;
    *)
        echo "Usage: $0 {analyze|summary|process}"
        echo "  analyze  - Generate comprehensive token usage analysis"
        echo "  summary  - Display current analysis summary"
        echo "  process  - Process terminal output line for correlation"
        ;;
esac