#!/bin/bash
set -euo pipefail

# Error handler
trap 'echo "ERROR in warn-git-aliases.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Warn About Git Aliases Hook
# Warns when bash commands use common git aliases instead of full commands
#
# TRIGGER: PreToolUse (Bash)
#
# WARNED PATTERNS:
# - git st (should be: git status)
# - git co (should be: git checkout)
# - git br (should be: git branch)
# - git ci (should be: git commit)
# - git df (should be: git diff)
#
# RATIONALE:
# Git aliases are user-specific configurations. Commands using aliases
# fail in environments where they're not configured.
#
# ADDED: 2026-01-16 after M106 - used `git st` which isn't a standard command

# Read JSON from stdin
JSON_INPUT=""
if [ -t 0 ]; then
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Source JSON parsing library
source "${CLAUDE_PROJECT_DIR}/.claude/hooks/lib/json-parser.sh"

# Extract tool name and command
TOOL_NAME=$(extract_json_value "$JSON_INPUT" "tool_name")
COMMAND=$(extract_json_value "$JSON_INPUT" "command")

# Fallback extraction
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

# Map of common git aliases to full commands
declare -A GIT_ALIASES=(
	["st"]="status"
	["co"]="checkout"
	["br"]="branch"
	["ci"]="commit"
	["df"]="diff"
	["lg"]="log --oneline --graph"
	["cp"]="cherry-pick"
	["rb"]="rebase"
	["rs"]="reset"
	["sw"]="switch"
)

FOUND_ALIASES=()
SUGGESTED_FIXES=()

# Check for git alias patterns
for alias in "${!GIT_ALIASES[@]}"; do
	# Match: git alias, git alias args, but not git alias-something
	if echo "$COMMAND" | grep -qE "(^|[;&|[:space:]])git[[:space:]]+${alias}([[:space:]]|$|[;&|])"; then
		FOUND_ALIASES+=("$alias")
		SUGGESTED_FIXES+=("git ${GIT_ALIASES[$alias]}")
	fi
done

# If aliases found, warn
if [ ${#FOUND_ALIASES[@]} -gt 0 ]; then
	cat << EOF >&2

⚠️  GIT ALIAS WARNING
═══════════════════════════════════════════════════════════════

⚠️  Found git alias(es) that may not exist: ${FOUND_ALIASES[*]}

Git aliases are user-specific. Use full commands instead:
EOF

	for i in "${!FOUND_ALIASES[@]}"; do
		echo "  ❌ git ${FOUND_ALIASES[$i]} → ✅ ${SUGGESTED_FIXES[$i]}" >&2
	done

	cat << EOF >&2

📖 See: git help for standard subcommands

═══════════════════════════════════════════════════════════════
⚠️  WARNING - Command will proceed but may fail
EOF
fi

# Allow command to proceed (warning only)
echo '{}'
exit 0
