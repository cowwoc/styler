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

# Detect mid-protocol abandonment patterns
detect_protocol_abandonment()
{
    local text_lower=$(echo "$1" | tr '[:upper:]' '[:lower:]')

    # Check for asking permission mid-protocol
    if [[ "$text_lower" == *"would you like me to"* ]]; then
        # Check if we have active locks (indicating task in progress)
        if ls /workspace/locks/*.json &>/dev/null; then
            # Pattern: asking what to do mid-task
            [[ "$text_lower" == *"continue with implementation"* ]] && return 0
            [[ "$text_lower" == *"select a different task"* ]] && return 0
            [[ "$text_lower" == *"proceed with"* ]] && return 0
            [[ "$text_lower" == *"or would you"* ]] && return 0
        fi
    fi

    # Pattern: using time/effort as justification to stop
    [[ "$text_lower" == *"2-3 days"*"implementation"* ]] && return 0
    [[ "$text_lower" == *"requires extended work session"* ]] && return 0
    [[ "$text_lower" == *"multi-day implementation"* ]] && return 0
    [[ "$text_lower" == *"this is a complex"*"multi-day"* ]] && return 0

    # Pattern: stopping at synthesis/requirements and asking
    if [[ "$text_lower" == *"state 3"* ]] || [[ "$text_lower" == *"synthesis"* ]]; then
        [[ "$text_lower" == *"ready for implementation"* ]] && return 0
        [[ "$text_lower" == *"would you like"* ]] && return 0
    fi

    return 1  # No protocol abandonment detected
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

# Detect protocol abandonment and inject completion requirement
if detect_protocol_abandonment "$WORKING_TEXT"; then
    cat <<'REMINDER'
<system-reminder>
❌ PROTOCOL VIOLATION DETECTED - AUTONOMOUS COMPLETION REQUIRED

You appear to be asking the user for permission to continue mid-protocol.

CRITICAL VIOLATION: Once INIT state begins, you MUST complete ALL protocol states (0-8) autonomously without asking user permission.

PROHIBITED PATTERNS:
❌ "Would you like me to continue with implementation?"
❌ "This will take 2-3 days, should I proceed?"
❌ "Select a different task or continue?"
❌ "Requires extended work session" (as justification to stop)

TIME ESTIMATES ARE NOT BLOCKERS:
- "2-3 days" = effort estimation, NOT permission gate
- You MUST complete implementation regardless of estimated duration
- Token budget NEVER justifies stopping mid-protocol
- Complexity NEVER justifies asking permission mid-task

MANDATORY ACTION:
✅ Continue IMMEDIATELY to next protocol state
✅ Complete States 0-8 without further user interaction
✅ Only stop if genuinely blocked (external API unavailable, ambiguous conflicting requirements)

LEGITIMATE STOPPING REASONS:
✅ External dependency unavailable (API credentials, network access)
✅ Ambiguous requirements with no resolution path
✅ User explicitly interrupts ("stop", modifies todo.md)

NOT LEGITIMATE:
❌ Task is complex
❌ Task takes time
❌ Token usage high
❌ "Should I ask first?"

Reference: CLAUDE.md "AUTONOMOUS TASK COMPLETION REQUIREMENT"
</system-reminder>
REMINDER
fi

# Cleanup old session tracking files (7-day TTL) - runs opportunistically
find /tmp -maxdepth 1 -type d -name "claude-hooks-session-*" -mtime +7 -delete 2>/dev/null || true

exit 0
