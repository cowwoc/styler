#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in warn-zsh-reserved-vars.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Warn About zsh Reserved Variables Hook
# Warns when bash commands use variable names that are read-only in zsh
#
# TRIGGER: PreToolUse (Bash)
#
# WARNED PATTERNS:
# 1. status= (assignment to read-only zsh variable)
# 2. Other common zsh read-only variables
#
# RATIONALE:
# Claude Code runs in zsh on many systems. Using reserved variable names
# causes cryptic errors like "(eval):1: read-only variable: status"
#
# ADDED: 2026-01-16 after M105 - used status= in loop, broke task scanning

# Read JSON from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Source JSON parsing library
source "${CLAUDE_PROJECT_DIR}/.claude/hooks/lib/json-parser.sh"

# Extract tool name and command from JSON
TOOL_NAME=$(extract_json_value "$JSON_INPUT" "tool_name")
COMMAND=$(extract_json_value "$JSON_INPUT" "command")

# Fallback extraction if primary method fails
if [ -z "$TOOL_NAME" ]; then
	TOOL_NAME=$(echo "$JSON_INPUT" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

if [ -z "$COMMAND" ]; then
	COMMAND=$(echo "$JSON_INPUT" | sed -n 's/.*"command"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

# Only process Bash tool calls
if [ "$TOOL_NAME" != "Bash" ] && [ "$TOOL_NAME" != "bash" ]; then
	echo '{}'
	exit 0
fi

# List of zsh read-only variables to check
# See: https://zsh.sourceforge.io/Doc/Release/Parameters.html
ZSH_READONLY_VARS=(
	"status"      # Most common - exit status of last command
	"pipestatus"  # Exit statuses of pipeline components
	"ERRNO"       # System error number
	"GID"         # Real group ID
	"EGID"        # Effective group ID
	"UID"         # Real user ID
	"EUID"        # Effective user ID
	"USERNAME"    # Login name
	"HOST"        # Hostname
	"LINENO"      # Current line number
)

FOUND_VARS=()

# Convert JSON-escaped newlines to actual newlines for pattern matching
# JSON encodes newlines as \n (literal backslash-n), which grep won't match as whitespace
COMMAND_NORMALIZED=$(echo "$COMMAND" | sed 's/\\n/\n/g')

# Check for assignment patterns: var= or var=value
for var in "${ZSH_READONLY_VARS[@]}"; do
	# Match: var=, var="...", var='...', var=$(...), etc.
	# But NOT: $var, ${var}, some_other_var=
	if echo "$COMMAND_NORMALIZED" | grep -qE "(^|[;&|[:space:]])${var}="; then
		FOUND_VARS+=("$var")
	fi
done

# If reserved variables found, BLOCK the command
if [ ${#FOUND_VARS[@]} -gt 0 ]; then
	VARS_LIST=$(printf ", %s" "${FOUND_VARS[@]}")
	VARS_LIST=${VARS_LIST:2}  # Remove leading ", "

	# Output blocking JSON to stdout (what Claude Code reads)
	cat << EOF
{
  "decision": "block",
  "reason": "Command uses zsh read-only variable(s): ${VARS_LIST}. Use alternatives: status→st, pipestatus→pipe_results, ERRNO→err_num. See CLAUDE.md § Zsh Reserved Variables."
}
EOF
	exit 0
fi

# Allow command to proceed if no reserved variables found
echo '{}'
exit 0
