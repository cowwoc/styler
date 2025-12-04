#!/bin/bash
# Detector Framework Library - Unified pattern detection dispatch
#
# Usage: source /workspace/main/.claude/hooks/lib/detector-framework.sh
#
# This library provides a framework for detection hooks to reduce boilerplate.
# Each detector only needs to define its pattern matching and action logic.

# ============================================================================
# CONFIGURATION
# ============================================================================

# Array to hold registered detectors
declare -a REGISTERED_DETECTORS=()

# Detector state tracking (per-session)
declare -A DETECTOR_TRIGGERED=()
declare -A DETECTOR_COOLDOWN=()

# Default cooldown period (seconds) - prevents spam
DEFAULT_COOLDOWN=300

# ============================================================================
# DETECTOR REGISTRATION
# ============================================================================

# Register a detector function
# Args: detector_name cooldown_seconds
# The detector function must be named: detect_<detector_name>
# Returns: 0 on success
register_detector() {
    local name="$1"
    local cooldown="${2:-$DEFAULT_COOLDOWN}"

    REGISTERED_DETECTORS+=("$name")
    DETECTOR_COOLDOWN["$name"]="$cooldown"
}

# ============================================================================
# EXECUTION FRAMEWORK
# ============================================================================

# Check if detector is in cooldown period
# Args: detector_name session_id
# Returns: 0 if in cooldown (should skip), 1 if can run
is_detector_in_cooldown() {
    local name="$1"
    local session_id="$2"

    local marker_file="/tmp/detector_${name}_${session_id}"

    if [[ ! -f "$marker_file" ]]; then
        return 1  # Not in cooldown
    fi

    local last_run
    last_run=$(cat "$marker_file" 2>/dev/null || echo "0")
    local now
    now=$(date +%s)
    local cooldown="${DETECTOR_COOLDOWN[$name]:-$DEFAULT_COOLDOWN}"

    if (( now - last_run < cooldown )); then
        return 0  # In cooldown
    fi

    return 1  # Cooldown expired
}

# Mark detector as triggered
# Args: detector_name session_id
mark_detector_triggered() {
    local name="$1"
    local session_id="$2"

    local marker_file="/tmp/detector_${name}_${session_id}"
    date +%s > "$marker_file"
}

# Run all registered detectors
# Args: input_json
# Calls each detect_<name> function with the input
run_all_detectors() {
    local input="$1"

    local session_id
    session_id=$(echo "$input" | jq -r '.session_id // empty' 2>/dev/null)

    for detector in "${REGISTERED_DETECTORS[@]}"; do
        # Skip if in cooldown
        if is_detector_in_cooldown "$detector" "$session_id"; then
            continue
        fi

        # Call the detector function
        local func_name="detect_${detector}"
        if declare -f "$func_name" > /dev/null 2>&1; then
            if "$func_name" "$input"; then
                mark_detector_triggered "$detector" "$session_id"
            fi
        fi
    done
}

# ============================================================================
# OUTPUT HELPERS
# ============================================================================

# Standard detection banner
# Args: title
print_detection_banner() {
    local title="$1"

    cat >&2 << EOF

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️  $title
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

EOF
}

# Standard suggestion block
# Args: suggestion_text
print_suggestion() {
    local text="$1"

    cat >&2 << EOF
💡 SUGGESTION:
$text

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

EOF
}

# Standard warning block
# Args: warning_text
print_warning() {
    local text="$1"

    cat >&2 << EOF
⚠️  WARNING:
$text

EOF
}

# Standard error block (for blocking hooks)
# Args: error_text
print_blocking_error() {
    local text="$1"

    cat >&2 << EOF
🚨 BLOCKED:
$text

EOF
}

# ============================================================================
# PATTERN MATCHING HELPERS
# ============================================================================

# Check if text contains any of the given patterns
# Args: text pattern1 [pattern2 ...]
# Returns: 0 if match found, 1 otherwise
matches_any_pattern() {
    local text="$1"
    shift

    for pattern in "$@"; do
        if echo "$text" | grep -qiE "$pattern"; then
            return 0
        fi
    done

    return 1
}

# Check if text contains all of the given patterns
# Args: text pattern1 [pattern2 ...]
# Returns: 0 if all match, 1 otherwise
matches_all_patterns() {
    local text="$1"
    shift

    for pattern in "$@"; do
        if ! echo "$text" | grep -qiE "$pattern"; then
            return 1
        fi
    done

    return 0
}

# Count pattern matches in text
# Args: text pattern
# Returns: Count of matches (via stdout)
count_pattern_matches() {
    local text="$1"
    local pattern="$2"

    echo "$text" | grep -ciE "$pattern" 2>/dev/null || echo "0"
}

# ============================================================================
# TOOL INPUT EXTRACTION
# ============================================================================

# Extract tool name from hook input
# Args: input_json
get_tool_name() {
    local input="$1"
    echo "$input" | jq -r '.tool.name // .tool_name // empty' 2>/dev/null
}

# Extract tool input/parameters
# Args: input_json
get_tool_input() {
    local input="$1"
    echo "$input" | jq -r '.tool.input // .tool.parameters // {}' 2>/dev/null
}

# Extract file path from tool input
# Args: input_json
get_tool_file_path() {
    local input="$1"
    echo "$input" | jq -r '.tool.input.file_path // .tool.input.path // empty' 2>/dev/null
}

# Extract command from Bash tool input
# Args: input_json
get_bash_command() {
    local input="$1"
    echo "$input" | jq -r '.tool.input.command // empty' 2>/dev/null
}

# ============================================================================
# CONVERSATION CONTEXT
# ============================================================================

# Get recent conversation log path
# Args: session_id
get_conversation_log() {
    local session_id="$1"

    # Check standard Claude Code location
    local log_dir="${HOME}/.claude/conversations"
    if [[ -d "$log_dir" ]]; then
        # Find most recent conversation file
        find "$log_dir" -name "*.json" -type f -mmin -60 -print 2>/dev/null | head -1
    fi
}

# Extract assistant messages from conversation
# Args: conversation_json_path
extract_assistant_messages() {
    local conv_path="$1"

    if [[ -f "$conv_path" ]]; then
        jq -r '.messages[] | select(.role == "assistant") | .content' "$conv_path" 2>/dev/null
    fi
}

# ============================================================================
# INITIALIZATION
# ============================================================================

# Initialize detector framework
# Call this at the start of your hook
init_detector_framework() {
    # Clean up old cooldown markers (older than 1 hour)
    find /tmp -name "detector_*" -type f -mmin +60 -delete 2>/dev/null || true
}
