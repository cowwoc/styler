#!/bin/bash
# Simplified Task Protocol Metrics - Focus on Workflow Efficiency  
# Measures what we can reliably track: timing, success rates, behavior patterns
# DISABLED: Metrics collection disabled per user request

# DISABLED: Return immediately to disable metrics collection
exit 0

# Configuration
METRICS_ROOT="/workspace/.claude/metrics"
SESSION_ID="${SESSION_ID:-$(uuidgen 2>/dev/null || echo "session_$(date +%s)")}"
METRICS_FILE="$METRICS_ROOT/workflow_metrics.jsonl"
LOCK_FILE="$METRICS_ROOT/.metrics.lock"

# Working directory context for test differentiation
WORK_DIR_CONTEXT=$(basename "$(pwd)" | sed 's/[^a-zA-Z0-9-]/_/g')

# Ensure metrics directory exists
mkdir -p "$METRICS_ROOT"
chmod 755 "$METRICS_ROOT"

# Atomic write function with file locking
atomic_write_metric() {
    local metric_json="$1"
    local temp_file="$METRICS_FILE.tmp.$$"
    
    (
        flock -x 200
        echo "$metric_json" >> "$temp_file"
        mv "$temp_file" "$METRICS_FILE.partial.$$"
        cat "$METRICS_FILE.partial.$$" >> "$METRICS_FILE"
        rm -f "$METRICS_FILE.partial.$$"
    ) 200>"$LOCK_FILE"
}

# Enhanced logging with structured JSON data
log_metric() {
    local event_type="$1"
    local data="$2"
    
    local timestamp=$(date -u -Iseconds 2>/dev/null || date -u '+%Y-%m-%dT%H:%M:%SZ')
    local metric_json=$(cat <<EOF
{"timestamp":"$timestamp","session_id":"$SESSION_ID","worktree":"$WORK_DIR_CONTEXT","pid":$$,"event":"$event_type","data":$data}
EOF
)
    
    atomic_write_metric "$metric_json"
}

# Track user prompts and task initiation patterns
track_user_prompt() {
    local prompt_type="$1"
    local contains_task_protocol="${2:-false}"
    local contains_todo_reference="${3:-false}"
    local prompt_length="${4:-0}"
    
    local data=$(cat <<EOF
{"prompt_type":"$prompt_type","contains_task_protocol":$contains_task_protocol,"contains_todo_reference":$contains_todo_reference,"prompt_length":$prompt_length,"working_dir":"$(pwd)"}
EOF
)
    
    log_metric "user_prompt" "$data"
}

# Track task workflow timing (start/complete/fail)
track_task_timing() {
    local event="$1"  # start, complete, fail
    local task_identifier="${2:-unknown}"
    local duration_seconds="${3:-null}"
    local error_details="${4:-null}"
    
    local data=$(cat <<EOF
{"event":"$event","task_identifier":"$task_identifier","duration_seconds":$duration_seconds,"error_details":"$error_details","timestamp_unix":$(date +%s)}
EOF
)
    
    log_metric "task_timing" "$data"
}

# Track workflow success patterns
track_workflow_outcome() {
    local outcome="$1"  # success, partial_success, failure, abandoned
    local task_type="${2:-unknown}"
    local completion_percentage="${3:-0}"
    local final_state="${4:-unknown}"
    
    local data=$(cat <<EOF
{"outcome":"$outcome","task_type":"$task_type","completion_percentage":$completion_percentage,"final_state":"$final_state"}
EOF
)
    
    log_metric "workflow_outcome" "$data"
}

# Track protocol comparison metrics
track_protocol_execution() {
    local protocol_version="$1"  # V2
    local phase="$2"  # start, agent_selection, implementation, complete
    local task_name="${3:-unknown}"
    local agent_count="${4:-0}"
    local selected_agents="${5:-[]}"
    local confidence_scores="${6:-{}}"
    local selection_time="${7:-null}"
    
    local data=$(cat <<EOF
{"protocol_version":"$protocol_version","phase":"$phase","task_name":"$task_name","agent_count":$agent_count,"selected_agents":$selected_agents,"confidence_scores":$confidence_scores,"selection_time_seconds":$selection_time,"timestamp_unix":$(date +%s)}
EOF
)
    
    log_metric "protocol_execution" "$data"
}

# Track agent selection accuracy
track_agent_selection_accuracy() {
    local protocol_version="$1"
    local task_name="$2"
    local true_positives="${3:-0}"
    local false_positives="${4:-0}" 
    local false_negatives="${5:-0}"
    local precision="${6:-null}"
    local recall="${7:-null}"
    local selection_rationale="${8:-}"
    
    local data=$(cat <<EOF
{"protocol_version":"$protocol_version","task_name":"$task_name","true_positives":$true_positives,"false_positives":$false_positives,"false_negatives":$false_negatives,"precision":$precision,"recall":$recall,"selection_rationale":"$selection_rationale"}
EOF
)
    
    log_metric "agent_selection_accuracy" "$data"
}

# Track agent performance metrics (self-reported)
track_agent_performance() {
    local agent_type="$1"
    local task_complexity="${2:-unknown}"  # simple, moderate, complex
    local execution_mode="${3:-standard}"  # quick, standard, comprehensive
    local tokens_generated="${4:-null}"
    local processing_time_seconds="${5:-null}"
    local confidence_score="${6:-null}"
    local findings_count="${7:-0}"
    local recommendations_count="${8:-0}"
    
    local data=$(cat <<EOF
{"agent_type":"$agent_type","task_complexity":"$task_complexity","execution_mode":"$execution_mode","tokens_generated":$tokens_generated,"processing_time_seconds":$processing_time_seconds,"confidence_score":$confidence_score,"findings_count":$findings_count,"recommendations_count":$recommendations_count,"timestamp_unix":$(date +%s)}
EOF
)
    
    log_metric "agent_performance" "$data"
}

# Track Claude-specific consumption metrics
track_claude_consumption() {
    local agent_type="$1"
    local output_format="${2:-human}"  # human, claude, hybrid
    local structural_complexity="${3:-null}"  # tokens in structured sections
    local narrative_complexity="${4:-null}"   # tokens in narrative sections
    local actionable_items="${5:-0}"
    local follow_up_required="${6:-false}"
    
    local data=$(cat <<EOF
{"agent_type":"$agent_type","output_format":"$output_format","structural_complexity":$structural_complexity,"narrative_complexity":$narrative_complexity,"actionable_items":$actionable_items,"follow_up_required":"$follow_up_required"}
EOF
)
    
    log_metric "claude_consumption" "$data"
}

# Track build and validation events (when they occur)
track_build_event() {
    local build_event="$1"  # start, success, failure
    local build_tool="${2:-unknown}"
    local error_count="${3:-0}"
    local duration_seconds="${4:-null}"
    
    local data=$(cat <<EOF
{"build_event":"$build_event","build_tool":"$build_tool","error_count":$error_count,"duration_seconds":$duration_seconds}
EOF
)
    
    log_metric "build_event" "$data"
}

# Track directory changes and navigation patterns
track_navigation() {
    local from_dir="${PWD}"
    local navigation_type="$1"  # cd, worktree_switch, task_start
    local target_dir="${2:-unknown}"
    
    local data=$(cat <<EOF
{"from_dir":"$from_dir","navigation_type":"$navigation_type","target_dir":"$target_dir"}
EOF
)
    
    log_metric "navigation" "$data"
}

# Auto-detect task protocol patterns in user prompts
detect_task_protocol_usage() {
    local prompt_text="${1:-}"
    
    # Check for task protocol indicators
    local has_task_protocol=false
    local has_todo_reference=false
    
    if echo "$prompt_text" | grep -qi -E "(task.protocol|todo.list|apply.*style|continuous.*mode)"; then
        has_task_protocol=true
    fi
    
    if echo "$prompt_text" | grep -qi -E "(todo|task.*list|pending.*task)"; then
        has_todo_reference=true
    fi
    
    local prompt_length=${#prompt_text}
    track_user_prompt "auto_detected" "$has_task_protocol" "$has_todo_reference" "$prompt_length"
}

# Session initialization with environment context
initialize_session() {
    local init_data=$(cat <<EOF
{"pid":$$,"working_dir":"$(pwd)","worktree":"$WORK_DIR_CONTEXT","git_branch":"$(git branch --show-current 2>/dev/null || echo 'unknown')","start_time_unix":$(date +%s)}
EOF
)
    
    log_metric "session_start" "$init_data"
    
    # Detect test environment
    if [[ "$PWD" == *"test-"* ]]; then
        local test_type="unknown"
        if [[ "$PWD" == *"monolithic"* ]]; then
            test_type="monolithic"
        elif [[ "$PWD" == *"modular"* ]]; then
            test_type="modular"
        fi
        
        log_metric "test_environment" '{"test_type":"'"$test_type"'","environment_ready":true}'
    fi
}

# Track documentation access patterns for optimization analysis
track_documentation_access() {
	local doc_file="$1"
	local access_type="${2:-read}"  # read, search, reference
	local context="${3:-unknown}"  # task_type, agent_type, etc.
	local tool_call_count="${4:-1}"
	
	local data=$(cat <<EOF
{"doc_file":"$doc_file","access_type":"$access_type","context":"$context","tool_call_count":$tool_call_count,"timestamp_unix":$(date +%s)}
EOF
)
	
	log_metric "doc_access" "$data"
}

# Track task protocol A/B testing metrics
track_protocol_version_usage() {
	local version="${1:-unknown}"  # original, claude-optimized
	local task_type="${2:-unknown}"
	local access_trigger="${3:-manual}"  # manual, automatic, hook
	local context="${4:-unknown}"
	
	local data=$(cat <<EOF
{"protocol_version":"$version","task_type":"$task_type","access_trigger":"$access_trigger","context":"$context","timestamp_unix":$(date +%s)}
EOF
)
	
	log_metric "protocol_version_usage" "$data"
}

# Track protocol effectiveness (adherence to steps, completion rate)
track_protocol_effectiveness() {
	local version="${1:-unknown}"
	local phases_completed="${2:-0}"
	local total_phases="${3:-6}"
	local success="${4:-false}"
	local task_name="${5:-unknown}"
	local execution_time="${6:-null}"
	
	local completion_rate=$(echo "scale=2; $phases_completed / $total_phases * 100" | bc 2>/dev/null || echo "0")
	
	local data=$(cat <<EOF
{"protocol_version":"$version","phases_completed":$phases_completed,"total_phases":$total_phases,"completion_rate":$completion_rate,"success":"$success","task_name":"$task_name","execution_time_seconds":$execution_time}
EOF
)
	
	log_metric "protocol_effectiveness" "$data"
}

# Track documentation efficiency metrics
track_documentation_efficiency() {
	local task_type="$1"
	local docs_accessed="${2:-[]}"  # JSON array of doc files
	local total_reads="${3:-0}"
	local task_completion_time="${4:-null}"
	local success="${5:-unknown}"
	
	local data=$(cat <<EOF
{"task_type":"$task_type","docs_accessed":$docs_accessed,"total_reads":$total_reads,"completion_time_seconds":$task_completion_time,"success":"$success"}
EOF
)
	
	log_metric "doc_efficiency" "$data"
}

# Track cross-reference patterns that break Claude context
track_context_breaks() {
	local source_doc="$1"
	local target_doc="$2"
	local break_type="${3:-external_link}"  # external_link, see_also, reference
	local context="${4:-unknown}"
	
	local data=$(cat <<EOF
{"source_doc":"$source_doc","target_doc":"$target_doc","break_type":"$break_type","context":"$context"}
EOF
)
	
	log_metric "context_break" "$data"
}

# Export functions for hook usage
export -f track_user_prompt
export -f track_task_timing
export -f track_workflow_outcome
export -f track_protocol_execution
export -f track_agent_selection_accuracy
export -f track_agent_performance
export -f track_claude_consumption
export -f track_build_event
export -f track_navigation
export -f detect_task_protocol_usage
export -f track_documentation_access
export -f track_documentation_efficiency
export -f track_context_breaks
export -f track_protocol_version_usage
export -f track_protocol_effectiveness
export -f log_metric

# Initialize session on script execution
initialize_session

# Simple manual tracking interface
case "${1:-}" in
    "task-start")
        track_task_timing "start" "$2" null null
        echo "Task timing started for: $2"
        ;;
    "task-complete")
        track_task_timing "complete" "$2" "$3" null
        echo "Task completion tracked for: $2 (duration: ${3}s)"
        ;;
    "workflow-success")
        track_workflow_outcome "success" "$2" "100" "completed"
        echo "Workflow success tracked for: $2"
        ;;
    "build-success")
        track_build_event "success" "$2" "0" "$3"
        echo "Build success tracked: $2 (duration: ${3}s)"
        ;;
    "test")
        echo "Testing simplified metrics capture for worktree: $WORK_DIR_CONTEXT"
        track_task_timing "start" "test-task" null null
        track_workflow_outcome "success" "test" "100" "completed"
        echo "Test metrics logged to: $METRICS_FILE"
        ;;
    *)
        # Default hook behavior - just track session
        ;;
esac