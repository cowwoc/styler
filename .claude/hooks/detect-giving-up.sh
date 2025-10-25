#!/bin/bash
set -euo pipefail

# Identify script path for error messages
SCRIPT_PATH="${BASH_SOURCE[0]}"

# Fail gracefully without blocking Claude Code
trap 'echo "[HOOK DEBUG] detect-giving-up.sh FAILED at line $LINENO" >&2; echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Unexpected error at line $LINENO" >&2; exit 0' ERR

echo "[HOOK DEBUG] detect-giving-up.sh START" >&2

# Giving Up Pattern Detection Hook
# Detects phrases indicating abandonment of complex problems
# Triggers: UserPromptSubmit event
# Action: Inject persistence reminder if patterns detected

# Read JSON data from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	# No input available, exit gracefully
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Exit if no JSON input
[[ -z "$JSON_INPUT" ]] && exit 0

# Simple JSON value extraction without jq dependency
extract_json_value()
{
	local json="$1"
	local key="$2"
	# Use grep and sed for basic JSON parsing (allow grep to fail without triggering set -e)
	echo "$json" | grep -o "\"$key\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | sed "s/\"$key\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\"/\1/" || true
}

# Extract hook event type
HOOK_EVENT=$(extract_json_value "$JSON_INPUT" "hook_event_name")

# Only handle UserPromptSubmit events
[[ "$HOOK_EVENT" != "UserPromptSubmit" ]] && exit 0

# Extract user message - try multiple possible fields
USER_PROMPT=$(extract_json_value "$JSON_INPUT" "message")

if [[ -z "$USER_PROMPT" ]]; then
	USER_PROMPT=$(extract_json_value "$JSON_INPUT" "user_message")
fi

if [[ -z "$USER_PROMPT" ]]; then
	USER_PROMPT=$(extract_json_value "$JSON_INPUT" "prompt")
fi

# Skip if no prompt provided
[[ -z "$USER_PROMPT" ]] && exit 0

# Input validation: limit prompt length to prevent resource exhaustion
MAX_PROMPT_LENGTH=100000  # 100KB maximum
if [[ ${#USER_PROMPT} -gt $MAX_PROMPT_LENGTH ]]; then
	USER_PROMPT="${USER_PROMPT:0:$MAX_PROMPT_LENGTH}"
fi

# Extract session ID from JSON input
SESSION_ID_RAW=$(extract_json_value "$JSON_INPUT" "session_id")

# Require session ID - fail fast if not provided
if [[ -z "$SESSION_ID_RAW" ]]; then
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: session_id must be provided in hook JSON input" >&2
	exit 0  # Non-blocking
fi

# Sanitize session ID to prevent directory traversal attacks
SESSION_ID=$(echo "$SESSION_ID_RAW" | tr -cd 'a-zA-Z0-9_-')
if [[ -z "$SESSION_ID" ]]; then
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Invalid session_id (contains no valid characters)" >&2
	exit 0  # Non-blocking
fi

# Session tracking directory with restrictive permissions
SESSION_TRACK_DIR="/tmp/claude-hooks-session-${SESSION_ID}"
if [[ ! -d "$SESSION_TRACK_DIR" ]]; then
	umask 077  # Restrictive permissions for security
	mkdir -m 700 "$SESSION_TRACK_DIR" 2>/dev/null || {
		echo "⚠️  HOOK WARNING [$SCRIPT_PATH]: Session tracking unavailable (disk full?)" >&2
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
	[[ "$text_lower" == *"given the large number"*"i'll continue with the next"* ]] && return 0
	[[ "$text_lower" == *"given the large number"*"let me continue with the next"* ]] && return 0
	[[ "$text_lower" == *"given the volume"*"i'll process them"* ]] && return 0

	return 1  # No pattern detected
}

# Detect code disabling/removal instead of debugging
detect_code_disabling_pattern()
{
	local text_lower=$(echo "$1" | tr '[:upper:]' '[:lower:]')

	# Pattern: Disabling/removing broken code
	[[ "$text_lower" == *"broken"*"remove"* ]] && return 0
	[[ "$text_lower" == *"broken"*"disable"* ]] && return 0
	[[ "$text_lower" == *"broken"*"skip"* ]] && return 0
	[[ "$text_lower" == *"broken"*"comment out"* ]] && return 0

	# Pattern: Simplifying to avoid debugging
	[[ "$text_lower" == *"simplifying the implementation"*"remove"* ]] && return 0
	[[ "$text_lower" == *"simpler approach"*"remove"* ]] && return 0
	[[ "$text_lower" == *"simplify by removing"* ]] && return 0

	# Pattern: Temporary disabling
	[[ "$text_lower" == *"temporarily disable"* ]] && return 0
	[[ "$text_lower" == *"disable for now"* ]] && return 0
	[[ "$text_lower" == *"skip for now"* ]] && return 0
	[[ "$text_lower" == *"comment out"*"temporarily"* ]] && return 0

	# Pattern: Removing exception handlers to "fix" compilation
	[[ "$text_lower" == *"removing the broad exception handler"* ]] && return 0
	[[ "$text_lower" == *"remove the exception handler"* ]] && return 0
	[[ "$text_lower" == *"removing the try-catch"* ]] && return 0

	# Pattern: Test passes without code = remove the code
	[[ "$text_lower" == *"test passes without"*"remove"* ]] && return 0
	[[ "$text_lower" == *"works without"*"disable"* ]] && return 0
	[[ "$text_lower" == *"passes without"*"skip"* ]] && return 0

	return 1  # No code disabling pattern detected
}

# Detect mid-protocol abandonment patterns
detect_protocol_abandonment()
{
	local text_lower=$(echo "$1" | tr '[:upper:]' '[:lower:]')

	# Check for asking permission mid-protocol
	if [[ "$text_lower" == *"would you like me to"* ]]; then
		# Check if we have active locks (indicating task in progress)
		if ls /workspace/tasks/*/task.json &>/dev/null; then
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

# Detect code disabling/removal patterns and inject debugging requirement
if detect_code_disabling_pattern "$WORKING_TEXT"; then
	cat <<'REMINDER'
<system-reminder>
🚨 CODE DISABLING ANTI-PATTERN DETECTED - DEBUGGING REQUIRED

You appear to be disabling, removing, or skipping broken code instead of debugging it.

CRITICAL VIOLATION: When code is broken or failing, you MUST debug and fix the root cause, NOT remove/disable the code.

PROHIBITED PATTERNS:
❌ "The test passes without the custom deserializer, so let me remove it"
❌ "Let me simplify by removing the broken code"
❌ "I'll disable this for now"
❌ "Let me skip this broken feature"
❌ "Comment out the failing code temporarily"
❌ "Remove the exception handler to fix compilation"
❌ "Try a simpler approach" (when debugging should continue)

MANDATORY RESPONSE:
✅ IMMEDIATELY debug the broken code to find the root cause
✅ Apply systematic troubleshooting approach (add logging, test isolation, step-by-step analysis)
✅ Fix the underlying problem, don't remove functionality
✅ If code appears unused, verify with evidence before removal
✅ Use incremental debugging rather than wholesale removal

ACCEPTABLE PATTERNS:
✅ "Let me add debug logging to understand why this fails"
✅ "I'll create a minimal test case to isolate the issue"
✅ "Let me verify the API contract to ensure correct usage"
✅ "I'll check the compilation error details to find the exact problem"

WHY THIS MATTERS:
- Removing broken code hides problems instead of solving them
- "Simplifying" often means abandoning requirements
- Features exist for a reason - debug first, remove only with justification
- Test passing without code suggests the code may be working but test is wrong

CORRECT APPROACH:
1. Identify the specific error/failure
2. Add targeted debug output to understand behavior
3. Form hypothesis about root cause
4. Test hypothesis with minimal changes
5. Fix the actual problem
6. Verify fix with tests

Reference: CLAUDE.md "LONG-TERM SOLUTION PERSISTENCE" and "GIVING UP DETECTION PATTERNS"
</system-reminder>
REMINDER
fi

# Cleanup old session tracking files (7-day TTL) - runs opportunistically
find /tmp -maxdepth 1 -type d -name "claude-hooks-session-*" -mtime +7 -delete 2>/dev/null || true

echo "[HOOK DEBUG] detect-giving-up.sh END" >&2

exit 0
