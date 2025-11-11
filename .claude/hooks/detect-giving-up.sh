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
#
# PATTERN EVOLUTION HISTORY:
# - Initial patterns: Explicit giving-up phrases ("too hard", "let's skip")
# - 2025-10-30: Added rationalization patterns after main agent attempted to remove
#   formatter API dependencies using "pragmatic decision" and "time constraints"
#   language to disguise giving up on JPMS compilation debugging
# - 2025-10-30: Added compilation abandonment detection after misdiagnosing
#   "empty JAR" issue as "complex JPMS problems" instead of systematically
#   debugging why security/config modules weren't compiling
# - 2025-11-03: Added token budget + file size rationalization patterns after main
#   agent avoided optimizing git-workflow.md (37KB) citing "remaining token budget
#   (70K)" despite having 2x necessary tokens. Added gerund forms ("skipping it")
#   and indirect giving up ("recommend skipping") to catch disguised avoidance.
# - 2025-11-05: Added context constraints patterns after main agent presented options
#   citing "Context Status: 126K/200K tokens used" and "Given context constraints"
#   instead of continuing work. Used "context" not "token budget" to bypass existing
#   patterns. Catches reporting context status before stopping work.

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

# Source JSON parsing library
source "/workspace/.claude/hooks/lib/json-parser.sh"

# Parse all common fields at once
parse_hook_json "$JSON_INPUT"

# Only handle UserPromptSubmit events
[[ "$HOOK_EVENT" != "UserPromptSubmit" ]] && exit 0

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

	# Early exit if no quotes present (common case optimization)
	[[ "$text" != *\"* ]] && { echo "$text"; return; }

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

	# Rationalization patterns (disguised giving-up using "pragmatic" language)
	# Added 2025-10-30 after main agent used these phrases to justify removing
	# formatter dependencies instead of debugging compilation issues
	[[ "$text_lower" == *"due to the complex"*"i need to make a pragmatic decision"* ]] && return 0
	[[ "$text_lower" == *"considering the time constraints"*"i need to"* ]] && return 0
	[[ "$text_lower" == *"considering the time constraints"*"i'll"* ]] && return 0
	[[ "$text_lower" == *"i'll simplify"*"to remove the problematic"* ]] && return 0
	[[ "$text_lower" == *"let me take a different approach"*"remove"* ]] && return 0
	[[ "$text_lower" == *"pragmatic decision"*"simplify"* ]] && return 0
	# CLAUDE.md explicitly prohibits these patterns:
	[[ "$text_lower" == *"due to complexity and token usage"* ]] && return 0
	[[ "$text_lower" == *"given token constraints"*"i'll implement a basic"* ]] && return 0
	[[ "$text_lower" == *"i'll create a solid mvp"* ]] && return 0

	# Token budget rationalization patterns (added 2025-11-03)
	# After main agent cited "file's size (37KB) and remaining token budget (70K)"
	# to justify skipping git-workflow.md optimization despite sufficient tokens
	# Updated 2025-11-03: Added "token budget remaining" + "recommend" pattern after
	# main agent presented options citing "Given the token budget remaining (69K tokens)
	# and the size of the remaining files, I recommend:" instead of continuing work
	[[ "$text_lower" == *"due to"*"size"*"token budget"* ]] && return 0
	[[ "$text_lower" == *"due to"*"size"*"remaining token"* ]] && return 0
	[[ "$text_lower" == *"file's size"*"token"*"skip"* ]] && return 0
	[[ "$text_lower" == *"given"*"size"*"token budget"* ]] && return 0
	[[ "$text_lower" == *"remaining token budget"*"skip"* ]] && return 0
	[[ "$text_lower" == *"remaining token budget"*"recommend"* ]] && return 0
	[[ "$text_lower" == *"token budget remaining"*"recommend"* ]] && return 0  # reverse word order
	[[ "$text_lower" == *"given"*"token budget remaining"* ]] && return 0  # "given the token budget remaining"
	[[ "$text_lower" == *"given"*"remaining"*"recommend"* ]] && return 0  # general "given...remaining...recommend" pattern
	[[ "$text_lower" == *"token budget"*"rather than"*"skip"* ]] && return 0
	# Context constraints rationalization patterns (added 2025-11-05)
	# After main agent presented options citing "Context Status: 126K/200K tokens used"
	# and "Given context constraints and the iterative nature of optimization, I recommend:"
	# instead of continuing work. Used "context constraints" not "token budget" to bypass
	# existing patterns. Token Usage Policy: "Token usage MUST NEVER affect behavior"
	[[ "$text_lower" == *"context status"*"tokens used"* ]] && return 0
	[[ "$text_lower" == *"context status"*"token"*"recommend"* ]] && return 0
	[[ "$text_lower" == *"given"*"context constraints"*"recommend"* ]] && return 0
	[[ "$text_lower" == *"given"*"context"*"iterative"*"recommend"* ]] && return 0
	[[ "$text_lower" == *"context"*"recommend"*"which approach"* ]] && return 0  # asking user to choose after context discussion

	# Framing shortcuts as architectural decisions to avoid debugging
	[[ "$text_lower" == *"as an architectural choice"*"remove"* ]] && return 0
	[[ "$text_lower" == *"architectural decision"*"without"* ]] && return 0
	[[ "$text_lower" == *"simplify the api"*"remove"* ]] && return 0
	[[ "$text_lower" == *"redesign to avoid"* ]] && return 0

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
	[[ "$text_lower" == *"skipping it for now"* ]] && return 0  # gerund form, added 2025-11-03
	[[ "$text_lower" == *"skipping this for now"* ]] && return 0
	[[ "$text_lower" == *"recommend skipping"* ]] && return 0  # indirect giving up
	[[ "$text_lower" == *"i recommend"*"skip"* ]] && return 0
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

# Detect compilation/build debugging abandonment patterns
# Added 2025-10-30 after main agent encountered "empty JAR" issue (mvn compile
# succeeded but produced no .class files) and attempted to remove dependencies
# instead of investigating why files weren't compiling
detect_compilation_abandonment()
{
	local text_lower=$(echo "$1" | tr '[:upper:]' '[:lower:]')

	# Pattern: Giving up on compilation errors by removing dependencies
	# Context: Agent should debug why compilation fails, not remove what fails
	[[ "$text_lower" == *"compilation error"*"remove"* ]] && return 0
	[[ "$text_lower" == *"module not found"*"remove"* ]] && return 0
	[[ "$text_lower" == *"build fails"*"simplify"* ]] && return 0
	[[ "$text_lower" == *"can't find module"*"remove dependency"* ]] && return 0

	# Pattern: Misdiagnosing as "complex JPMS issues" to justify avoidance
	# Reality: Often simple missing module-info.java or compilation error
	[[ "$text_lower" == *"complex jpms"*"simplify"* ]] && return 0
	[[ "$text_lower" == *"jpms dependency issues"*"remove"* ]] && return 0
	[[ "$text_lower" == *"module path"*"too complex"* ]] && return 0

	# Pattern: Empty JARs or missing classes but not investigating why
	# Should check: compilation errors, source files exist, javac works manually
	[[ "$text_lower" == *"empty jar"*"different approach"* ]] && return 0
	[[ "$text_lower" == *"no classes compiled"*"simplify"* ]] && return 0
	[[ "$text_lower" == *"jar contains no"*"remove"* ]] && return 0

	# Pattern: Build succeeds but produces no output - should investigate, not avoid
	[[ "$text_lower" == *"build success"*"but"*"empty"* ]] && return 0

	return 1  # No compilation abandonment detected
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

# Detect compilation abandonment and inject systematic debugging requirement
if detect_compilation_abandonment "$WORKING_TEXT"; then
	cat <<'REMINDER'
<system-reminder>
🚨 COMPILATION DEBUGGING ABANDONMENT DETECTED - SYSTEMATIC APPROACH REQUIRED

You appear to be avoiding compilation/build problems by removing dependencies or "simplifying" instead of debugging.

CRITICAL VIOLATION: When build/compilation fails, you MUST debug systematically to find and fix the root cause.

PROHIBITED PATTERNS:
❌ "Due to complex JPMS issues, I'll simplify by removing the dependency"
❌ "Module not found error - let me remove this requirement"
❌ "Empty JAR produced - I'll take a different approach"
❌ "Build succeeds but no classes - I'll redesign to avoid this"
❌ "JPMS module path too complex - I'll simplify the API"

MANDATORY SYSTEMATIC DEBUGGING APPROACH:

**Step 1: Identify Exact Error**
✅ Read full error message carefully
✅ Note exact file/line where error occurs
✅ Distinguish between: missing dependency, wrong version, compilation error, packaging issue

**Step 2: Investigate Root Cause**
For "module not found":
✅ Check if module-info.java exists in dependency
✅ Verify module name matches between requires and module declaration
✅ Check if JAR contains module-info.class: `jar tf path/to/file.jar | grep module-info`
✅ Verify dependency is in Maven reactor or installed: `mvn dependency:tree`

For "empty JAR" (build success but no .class files):
✅ Check for compilation errors: `mvn compile -X 2>&1 | grep -i error`
✅ Look for "nothing to compile" messages
✅ Verify source files exist: `find module/src -name "*.java"`
✅ Check target/classes directory: `ls -la module/target/classes/`
✅ Try manual javac to see actual errors: `javac -d /tmp module/src/main/java/File.java`

For JPMS issues:
✅ Verify transitive dependencies have module descriptors
✅ Check --add-modules or --add-reads might be needed
✅ Test compilation with explicit module-path
✅ Check for split packages across modules

**Step 3: Fix Root Cause**
✅ Add missing module-info.java files
✅ Fix module name mismatches
✅ Resolve actual compilation errors in source
✅ Add missing dependencies to POM
✅ Fix transitive JPMS requirements

**Step 4: Verify Fix**
✅ mvn clean compile succeeds
✅ JAR contains expected .class files
✅ Module dependencies resolved correctly

NEVER ACCEPTABLE:
❌ Removing dependencies because "it's too hard to make them work"
❌ Simplifying API because "JPMS is complex"
❌ Redesigning to avoid debugging
❌ Moving to "later" without fixing

ACCEPTABLE ONLY WITH EVIDENCE:
✅ "After investigation, discovered dependency X genuinely isn't needed (evidence: ...)"
✅ "Consulted stakeholder Y who confirmed this dependency should be removed"
✅ "Root cause is external API unavailable - documented blocker"

Reference: CLAUDE.md "LONG-TERM SOLUTION PERSISTENCE" - Exhaust reasonable effort before downgrading
</system-reminder>
REMINDER
fi

# Cleanup old session tracking files (7-day TTL) - runs opportunistically
find /tmp -maxdepth 1 -type d -name "claude-hooks-session-*" -mtime +7 -delete 2>/dev/null || true

echo "[HOOK DEBUG] detect-giving-up.sh END" >&2

exit 0
