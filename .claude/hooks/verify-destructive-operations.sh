#!/bin/bash
set -euo pipefail

# Identify script path for error messages
SCRIPT_PATH="${BASH_SOURCE[0]}"

# Fail gracefully without blocking Claude Code
trap 'echo "[HOOK DEBUG] verify-destructive-operations.sh FAILED at line $LINENO" >&2; echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Unexpected error at line $LINENO" >&2; exit 0' ERR

echo "[HOOK DEBUG] verify-destructive-operations.sh START" >&2

# Claude Code Hook: Post-Destructive Operation Verification
# Reminds Claude to verify no important details were lost after destructive operations

# Read JSON data from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	# No input available - this is a configuration error
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Expected JSON on stdin but none provided" >&2
	echo "   This hook must receive JSON data via stdin from Claude Code" >&2
	exit 0  # Non-blocking exit
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Check if JSON input is empty
if [[ -z "$JSON_INPUT" ]]; then
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Empty JSON input received" >&2
	echo "   Expected JSON structure with hook_event_name and message fields" >&2
	exit 0  # Non-blocking exit
fi

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

# Check if hook event was extracted
if [[ -z "$HOOK_EVENT" ]]; then
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Could not extract hook_event_name from JSON" >&2
	echo "   JSON received: ${JSON_INPUT:0:200}..." >&2
	exit 0  # Non-blocking exit
fi

# Only handle UserPromptSubmit events
if [[ "$HOOK_EVENT" != "UserPromptSubmit" ]]; then
	# Silently ignore other events (this is expected)
	exit 0
fi

# Extract user message - try multiple possible fields
LAST_MESSAGE=$(extract_json_value "$JSON_INPUT" "message")

if [[ -z "$LAST_MESSAGE" ]]; then
	LAST_MESSAGE=$(extract_json_value "$JSON_INPUT" "user_message")
fi

if [[ -z "$LAST_MESSAGE" ]]; then
	LAST_MESSAGE=$(extract_json_value "$JSON_INPUT" "prompt")
fi

# Check if message was extracted
if [[ -z "$LAST_MESSAGE" ]]; then
	echo "⚠️  HOOK WARNING [$SCRIPT_PATH]: Could not extract user message from UserPromptSubmit JSON" >&2
	exit 0  # Non-blocking exit
fi

# List of destructive operations that trigger verification
DESTRUCTIVE_KEYWORDS=(
	"git rebase"
	"git reset"
	"git checkout"
	"squash"
	"consolidate"
	"merge"
	"remove duplicate"
	"cleanup"
	"reorganize"
	"refactor"
	"delete"
	"rm "
)

# Check if any destructive keywords are present
for keyword in "${DESTRUCTIVE_KEYWORDS[@]}"; do
	if echo "$LAST_MESSAGE" | grep -qi "$keyword"; then
		echo "🚨 DESTRUCTIVE OPERATION DETECTED: '$keyword'"
		echo ""
		echo "⚠️  MANDATORY VERIFICATION REQUIRED:"
		echo "After completing this operation, you MUST:"
		echo "1. Double-check that no important details were unintentionally removed"
		echo "2. Verify that all essential information has been preserved"
		echo "3. Compare before/after to ensure completeness"
		echo "4. If consolidating/reorganizing, confirm all original content is retained"
		echo ""
		echo "🔍 This verification step is REQUIRED before considering the task complete."
		exit 0
	fi
done

echo "[HOOK DEBUG] verify-destructive-operations.sh END" >&2

exit 0
