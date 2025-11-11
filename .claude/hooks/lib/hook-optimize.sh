#!/bin/bash
# Hook optimization utilities library
# Provides optimized common operations for hooks
#
# Usage:
#   source "$(dirname "$0")/lib/hook-optimize.sh"
#
# Features:
#   - Task context detection with caching
#   - Optimized file operations
#   - Reduced subshell usage
#   - Batch operations where possible

# Source dependencies
SCRIPT_DIR_OPT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR_OPT}/json-cache.sh"

# Cache for task context
declare -gA TASK_CONTEXT_CACHE

# Find task context from current directory (optimized)
# Returns: task_name or empty string
find_task_from_pwd() {
    local cache_key="pwd::$PWD"

    # Check cache
    if [[ -v TASK_CONTEXT_CACHE["$cache_key"] ]]; then
        echo "${TASK_CONTEXT_CACHE[$cache_key]}"
        return 0
    fi

    local task_name=""

    # Pattern matching without subshell
    if [[ "$PWD" =~ /workspace/tasks/([^/]+) ]]; then
        task_name="${BASH_REMATCH[1]}"
    fi

    # Cache result
    TASK_CONTEXT_CACHE["$cache_key"]="$task_name"
    echo "$task_name"
}

# Find task owned by session (optimized with caching)
# Args: session_id
# Returns: task_name or empty string
find_task_by_session() {
    local session_id="${1:-}"

    if [[ -z "$session_id" ]]; then
        return 1
    fi

    local cache_key="session::$session_id"

    # Check cache (valid for 5 seconds to handle rapid hook executions)
    if [[ -v TASK_CONTEXT_CACHE["$cache_key"] ]]; then
        local cached_time=${TASK_CONTEXT_CACHE["${cache_key}_time"]:-0}
        local now=$(date +%s)
        if (( now - cached_time < 5 )); then
            echo "${TASK_CONTEXT_CACHE[$cache_key]}"
            return 0
        fi
    fi

    local tasks_dir="/workspace/tasks"
    if [[ ! -d "$tasks_dir" ]]; then
        return 1
    fi

    local task_name=""

    # Use built-in loop instead of find for better performance
    for task_dir in "$tasks_dir"/*; do
        if [[ ! -d "$task_dir" ]]; then
            continue
        fi

        local lock_file="${task_dir}/task.json"
        if [[ ! -f "$lock_file" ]]; then
            continue
        fi

        # Use cached JSON parsing
        if ! json_cache_exists "task::$(basename "$task_dir")"; then
            json_cache_load_file "task::$(basename "$task_dir")" "$lock_file"
        fi

        local lock_session=$(json_cache_get "task::$(basename "$task_dir")" ".session_id" "")

        if [[ "$lock_session" == "$session_id" ]]; then
            task_name=$(basename "$task_dir")
            break
        fi
    done

    # Cache result
    TASK_CONTEXT_CACHE["$cache_key"]="$task_name"
    TASK_CONTEXT_CACHE["${cache_key}_time"]=$(date +%s)

    echo "$task_name"
}

# Check if running in task context (optimized)
# Returns: 0 if in task context, 1 otherwise
is_task_context() {
    local task=$(find_task_from_pwd)
    [[ -n "$task" ]]
}

# Get task.json path for current context
# Args: [task_name]
# Returns: path to task.json or empty
get_task_json_path() {
    local task_name="${1:-}"

    if [[ -z "$task_name" ]]; then
        task_name=$(find_task_from_pwd)
    fi

    if [[ -z "$task_name" ]]; then
        return 1
    fi

    local task_json="/workspace/tasks/$task_name/task.json"
    if [[ -f "$task_json" ]]; then
        echo "$task_json"
        return 0
    fi

    return 1
}

# Read task state (optimized with caching)
# Args: task_name
# Returns: state or "UNKNOWN"
get_task_state() {
    local task_name="${1:-}"

    if [[ -z "$task_name" ]]; then
        echo "UNKNOWN"
        return 1
    fi

    local cache_key="task::$task_name"

    if ! json_cache_exists "$cache_key"; then
        local task_json="/workspace/tasks/$task_name/task.json"
        if [[ -f "$task_json" ]]; then
            json_cache_load_task "$task_json"
        else
            echo "UNKNOWN"
            return 1
        fi
    fi

    json_cache_get "$cache_key" ".state" "UNKNOWN"
}

# Batch check multiple tasks for a pattern
# Args: pattern task1 task2 ...
# Returns: list of matching tasks
filter_tasks_by_pattern() {
    local pattern="${1:-}"
    shift

    local matching=()

    for task in "$@"; do
        if [[ "$task" =~ $pattern ]]; then
            matching+=("$task")
        fi
    done

    printf '%s\n' "${matching[@]}"
}

# Optimized file existence check for multiple files
# Args: file1 file2 file3 ...
# Returns: 0 if all exist, 1 otherwise
all_files_exist() {
    for file in "$@"; do
        if [[ ! -f "$file" ]]; then
            return 1
        fi
    done
    return 0
}

# Optimized file existence check (any)
# Args: file1 file2 file3 ...
# Returns: 0 if any exist, 1 otherwise
any_file_exists() {
    for file in "$@"; do
        if [[ -f "$file" ]]; then
            return 0
        fi
    done
    return 1
}

# Clear optimization caches (for testing)
clear_opt_caches() {
    TASK_CONTEXT_CACHE=()
    json_cache_clear_all
}
