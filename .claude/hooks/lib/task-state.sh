#!/bin/bash
# Task State Library - Centralized task.json access patterns
#
# Usage: source /workspace/main/.claude/hooks/lib/task-state.sh
#
# This library provides common functions for interacting with task.json files,
# reducing code duplication across hooks.

# ============================================================================
# TASK DISCOVERY
# ============================================================================

# Find task.json for a given session ID
# Args: session_id
# Returns: Path to task.json or empty string
# Usage: TASK_JSON=$(find_task_for_session "$SESSION_ID")
find_task_for_session() {
    local session_id="$1"

    if [[ -z "$session_id" ]]; then
        return 1
    fi

    for task_json in /workspace/tasks/*/task.json; do
        [[ -f "$task_json" ]] || continue
        local lock_session
        lock_session=$(jq -r '.session_id // ""' "$task_json" 2>/dev/null) || continue
        if [[ "$lock_session" == "$session_id" ]]; then
            echo "$task_json"
            return 0
        fi
    done

    return 1
}

# Find task directory for a given session ID
# Args: session_id
# Returns: Path to task directory or empty string
find_task_dir_for_session() {
    local session_id="$1"
    local task_json

    task_json=$(find_task_for_session "$session_id") || return 1
    dirname "$task_json"
}

# Get task name from task.json path or task directory
# Args: task_json_path OR task_dir
# Returns: Task name
get_task_name() {
    local path="$1"

    if [[ "$path" == */task.json ]]; then
        path=$(dirname "$path")
    fi

    basename "$path"
}

# List all active tasks (have task.json)
# Returns: Space-separated list of task directories
list_active_tasks() {
    for task_dir in /workspace/tasks/*/; do
        [[ -f "${task_dir}task.json" ]] && echo "$task_dir"
    done
}

# ============================================================================
# STATE READING
# ============================================================================

# Get current state from task.json
# Args: task_json_path
# Returns: State string or "UNKNOWN"
get_task_state() {
    local task_json="$1"

    if [[ ! -f "$task_json" ]]; then
        echo "UNKNOWN"
        return 1
    fi

    jq -r '.state // "UNKNOWN"' "$task_json" 2>/dev/null || echo "UNKNOWN"
}

# Get session_id from task.json
# Args: task_json_path
# Returns: Session ID or empty string
get_task_session() {
    local task_json="$1"

    if [[ ! -f "$task_json" ]]; then
        return 1
    fi

    jq -r '.session_id // ""' "$task_json" 2>/dev/null
}

# Get task_name from task.json
# Args: task_json_path
# Returns: Task name from JSON or directory name as fallback
get_task_name_from_json() {
    local task_json="$1"

    if [[ ! -f "$task_json" ]]; then
        return 1
    fi

    local name
    name=$(jq -r '.task_name // ""' "$task_json" 2>/dev/null)

    if [[ -z "$name" ]]; then
        # Fallback to directory name
        get_task_name "$task_json"
    else
        echo "$name"
    fi
}

# Check if task is in a specific state
# Args: task_json_path state
# Returns: 0 if match, 1 otherwise
is_task_in_state() {
    local task_json="$1"
    local expected_state="$2"

    local current_state
    current_state=$(get_task_state "$task_json")

    [[ "$current_state" == "$expected_state" ]]
}

# Check if task is owned by a session
# Args: task_json_path session_id
# Returns: 0 if owned, 1 otherwise
is_task_owned_by() {
    local task_json="$1"
    local session_id="$2"

    local owner
    owner=$(get_task_session "$task_json")

    [[ "$owner" == "$session_id" ]]
}

# ============================================================================
# STATE MODIFICATION
# ============================================================================

# Update task state with transition log entry
# Args: task_json_path new_state
# Returns: 0 on success, 1 on failure
update_task_state() {
    local task_json="$1"
    local new_state="$2"

    if [[ ! -f "$task_json" ]]; then
        echo "ERROR: task.json not found: $task_json" >&2
        return 1
    fi

    local old_state
    old_state=$(get_task_state "$task_json")
    local timestamp
    timestamp=$(date -u +%Y-%m-%dT%H:%M:%SZ)

    local tmp_file="${task_json}.tmp.$$"

    if jq --arg old "$old_state" --arg new "$new_state" --arg ts "$timestamp" \
       '.state = $new | .transition_log += [{"from": $old, "to": $new, "timestamp": $ts}]' \
       "$task_json" > "$tmp_file" 2>/dev/null; then
        mv "$tmp_file" "$task_json"
        return 0
    else
        rm -f "$tmp_file"
        return 1
    fi
}

# Set a simple field in task.json
# Args: task_json_path field_name value
set_task_field() {
    local task_json="$1"
    local field="$2"
    local value="$3"

    if [[ ! -f "$task_json" ]]; then
        return 1
    fi

    local tmp_file="${task_json}.tmp.$$"

    if jq --arg val "$value" ".${field} = \$val" "$task_json" > "$tmp_file" 2>/dev/null; then
        mv "$tmp_file" "$task_json"
        return 0
    else
        rm -f "$tmp_file"
        return 1
    fi
}

# ============================================================================
# VALIDATION HELPERS
# ============================================================================

# Valid state transitions map
declare -A VALID_TRANSITIONS=(
    ["INIT"]="CLASSIFIED"
    ["CLASSIFIED"]="REQUIREMENTS"
    ["REQUIREMENTS"]="SYNTHESIS"
    ["SYNTHESIS"]="IMPLEMENTATION"
    ["IMPLEMENTATION"]="VALIDATION"
    ["VALIDATION"]="AWAITING_USER_APPROVAL"
    ["AWAITING_USER_APPROVAL"]="COMPLETE"
    ["COMPLETE"]="CLEANUP"
)

# Check if a state transition is valid
# Args: from_state to_state
# Returns: 0 if valid, 1 otherwise
is_valid_transition() {
    local from_state="$1"
    local to_state="$2"

    local expected="${VALID_TRANSITIONS[$from_state]:-}"

    [[ "$expected" == "$to_state" ]]
}

# Get the next valid state
# Args: current_state
# Returns: Next state or empty string
get_next_state() {
    local current_state="$1"
    echo "${VALID_TRANSITIONS[$current_state]:-}"
}

# ============================================================================
# SESSION ID EXTRACTION
# ============================================================================

# Extract session_id from hook input JSON
# Args: json_input
# Returns: Session ID
extract_session_id() {
    local input="$1"
    echo "$input" | jq -r '.session_id // empty' 2>/dev/null
}

# ============================================================================
# APPROVAL FLAGS
# ============================================================================

# Check if synthesis approval flag exists
# Args: task_dir
has_synthesis_approval() {
    local task_dir="$1"
    [[ -f "${task_dir}/user-approved-synthesis.flag" ]]
}

# Check if changes approval flag exists
# Args: task_dir
has_changes_approval() {
    local task_dir="$1"
    [[ -f "${task_dir}/user-approved-changes.flag" ]]
}

# Create approval flag
# Args: task_dir flag_name
create_approval_flag() {
    local task_dir="$1"
    local flag_name="$2"
    touch "${task_dir}/${flag_name}.flag"
}

# ============================================================================
# REQUIREMENTS REPORTS
# ============================================================================

# Check if all required reports exist
# Args: task_dir task_name
# Returns: 0 if all exist, 1 otherwise
has_all_requirements_reports() {
    local task_dir="$1"
    local task_name="$2"

    local required_agents=("architect" "tester" "formatter")

    for agent in "${required_agents[@]}"; do
        if [[ ! -f "${task_dir}/${task_name}-${agent}-requirements.md" ]]; then
            return 1
        fi
    done

    return 0
}

# List missing requirements reports
# Args: task_dir task_name
# Returns: Space-separated list of missing agent names
list_missing_reports() {
    local task_dir="$1"
    local task_name="$2"

    local required_agents=("architect" "tester" "formatter")
    local missing=()

    for agent in "${required_agents[@]}"; do
        if [[ ! -f "${task_dir}/${task_name}-${agent}-requirements.md" ]]; then
            missing+=("$agent")
        fi
    done

    echo "${missing[*]}"
}
