#!/bin/bash
# Standardized JSON output helper for hooks
# Source this file in hooks to simplify hook output generation
#
# Usage:
#   source /workspace/.claude/scripts/json-output.sh
#   output_hook_message "EventName" "Message content here"

# Output standardized hook message in JSON format
# Args: event_name message_content
output_hook_message() {
	local event="$1"
	local message="$2"

	jq -n \
		--arg event "$event" \
		--arg context "$message" \
		'{
			"hookSpecificOutput": {
				"hookEventName": $event,
				"additionalContext": $context
			}
		}'
}

# Output hook warning message
# Args: event_name message_content
output_hook_warning() {
	local event="$1"
	local message="$2"
	output_hook_message "$event" "$message"
}

# Output hook error message
# Args: event_name message_content
output_hook_error() {
	local event="$1"
	local message="$2"
	output_hook_message "$event" "$message"
}
