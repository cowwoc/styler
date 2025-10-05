#!/bin/bash
set -euo pipefail
# Fail gracefully without blocking Claude Code
trap 'echo "Warning: detect-giving-up.sh encountered error" >&2; exit 0' ERR

# Giving Up Pattern Detection Hook
# Detects phrases indicating abandonment of complex problems
# Triggers: UserPromptSubmit event
# Action: Inject persistence reminder if patterns detected

# Get hook data from command-line arguments (NOT stdin)
HOOK_EVENT="$1"
USER_PROMPT="$2"

# Input validation: limit prompt length to prevent resource exhaustion
MAX_PROMPT_LENGTH=100000  # 100KB maximum
if [[ ${#USER_PROMPT} -gt $MAX_PROMPT_LENGTH ]]; then
    USER_PROMPT="${USER_PROMPT:0:$MAX_PROMPT_LENGTH}"
fi

# Sanitize session ID to prevent directory traversal attacks
SESSION_ID_RAW="${CLAUDE_SESSION_ID:-default}"
SESSION_ID=$(echo "$SESSION_ID_RAW" | tr -cd 'a-zA-Z0-9_-')
[[ -z "$SESSION_ID" ]] && SESSION_ID="default"

# Session tracking directory with restrictive permissions
SESSION_TRACK_DIR="/tmp/claude-hooks-session-${SESSION_ID}"
if [[ ! -d "$SESSION_TRACK_DIR" ]]; then
    umask 077  # Restrictive permissions for security
    mkdir -m 700 "$SESSION_TRACK_DIR" 2>/dev/null || {
        echo "Warning: Session tracking unavailable (disk full?)" >&2
        exit 0  # Graceful degradation
    }
fi

# Rate limiting: prevent hook execution spam (max 1 execution per second)
RATE_LIMIT_FILE="$SESSION_TRACK_DIR/.rate_limit_giving_up"
CURRENT_TIME=$(date +%s)
if [[ -f "$RATE_LIMIT_FILE" ]]; then
    LAST_EXECUTION=$(cat "$RATE_LIMIT_FILE" 2>/dev/null || echo "0")
    TIME_DIFF=$((CURRENT_TIME - LAST_EXECUTION))
    if [[ $TIME_DIFF -lt 1 ]]; then
        exit 0  # Less than 1 second since last execution, skip
    fi
fi
echo "$CURRENT_TIME" > "$RATE_LIMIT_FILE"

# Skip if no prompt provided
[[ -z "$USER_PROMPT" ]] && exit 0

# Atomic check-and-mark for session tracking (prevents race conditions)
check_and_mark_prompt()
{
    local prompt_type="$1"
    local prompt_file="$SESSION_TRACK_DIR/main-${prompt_type}"

    # Use noclobber (set -C) for atomic test-and-set
    ( set -C; echo "$$" > "$prompt_file" ) 2>/dev/null
    return $?
}

# Only notify once per session (prevents notification spam)
if ! check_and_mark_prompt "giving-up-pattern"; then
    exit 0  # Already notified this session
fi

# Remove quoted text to prevent false positives (handle balanced quotes only)
remove_quoted_sections()
{
    local text="$1"
    local quote_count=$(echo "$text" | tr -cd '"' | wc -c)

    # Only remove quotes if balanced (even number)
    if [[ $((quote_count % 2)) -eq 0 ]]; then
        echo "$text" | sed 's/"[^"]*"//g'
    else
        echo "$text"  # Unbalanced quotes, return as-is
    fi
}

WORKING_TEXT=$(remove_quoted_sections "$USER_PROMPT")

# Pattern detection using literal string matching (prevents ReDoS attacks)
# No regex alternation, just simple bash string matching for security
detect_giving_up_pattern()
{
    local text_lower=$(echo "$1" | tr '[:upper:]' '[:lower:]')

    # Short-circuit on first match for performance
    [[ "$text_lower" == *"given the complexity of properly implementing"* ]] && return 0
    [[ "$text_lower" == *"given the evidence that this requires significant changes"* ]] && return 0
    [[ "$text_lower" == *"let me focus on completing the task protocol instead"* ]] && return 0
    [[ "$text_lower" == *"let me focus on features that provide more immediate value"* ]] && return 0
    [[ "$text_lower" == *"this would require significant architectural changes"* ]] && return 0
    [[ "$text_lower" == *"rather than diving deeper into this complex issue"* ]] && return 0
    [[ "$text_lower" == *"instead of implementing the full solution"* ]] && return 0
    [[ "$text_lower" == *"due to the complexity, i'll defer this to"* ]] && return 0
    [[ "$text_lower" == *"this appears to be beyond the current scope"* ]] && return 0
    [[ "$text_lower" == *"let me move on to easier tasks"* ]] && return 0

    return 1  # No pattern detected
}

# Detect giving up patterns and inject persistence reminder
if detect_giving_up_pattern "$WORKING_TEXT"; then
    cat <<'REMINDER'
<system-reminder>
🚨 GIVING UP PATTERN DETECTED - PERSISTENCE REQUIRED

MANDATORY RESPONSE:
✅ IMMEDIATELY return to the original technical problem
✅ Apply systematic debugging and decomposition approach
✅ Continue working on the exact issue that triggered this pattern
✅ Use incremental progress rather than abandoning the work
✅ Exhaust all reasonable technical approaches before scope modification

PROHIBITED: Abandoning complex problems for simpler alternatives without technical justification.
</system-reminder>
REMINDER
fi

# Cleanup old session tracking files (7-day TTL) - runs opportunistically
find /tmp -maxdepth 1 -type d -name "claude-hooks-session-*" -mtime +7 -delete 2>/dev/null || true

exit 0
