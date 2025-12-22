#!/bin/bash
# PreToolUse hook: Remind to use git-squash skill for squashing operations
#
# ADDED: 2025-12-22 after using ad-hoc git rebase -i instead of git-squash skill
# which produced concatenated commit messages requiring amendment.
#
# PREVENTION HIERARCHY:
# - Original: Documentation in CLAUDE.md (ignored)
# - Improvement 1: PostToolUse detection (warns after damage)
# - Improvement 2: PreToolUse reminder (warns BEFORE execution)
#
# This hook reminds the agent to use the git-squash skill when interactive
# rebase is detected. The skill enforces unified commit messages.

set -eo pipefail
trap 'echo "ERROR in remind-git-squash-skill.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read JSON context from stdin
INPUT=$(cat)

# Only process Bash tool
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
if [[ "$TOOL_NAME" != "Bash" ]]; then
	exit 0
fi

# Get the command
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

# Detect interactive rebase commands
if [[ "$COMMAND" =~ git[[:space:]]+(rebase[[:space:]]+-i|rebase[[:space:]]+--interactive) ]]; then
	# Rate limit: Only remind once per 10 minutes to avoid spam
	RATE_LIMIT_FILE="/tmp/git-squash-reminder-$(date +%Y%m%d%H)"
	RATE_LIMIT_MINUTE=$(($(date +%M) / 10))
	RATE_LIMIT_KEY="${RATE_LIMIT_FILE}-${RATE_LIMIT_MINUTE}"

	if [[ -f "$RATE_LIMIT_KEY" ]]; then
		# Already reminded recently, skip
		exit 0
	fi
	touch "$RATE_LIMIT_KEY"

	echo "" >&2
	echo "================================================================" >&2
	echo "  REMINDER: git-squash skill for squashing operations" >&2
	echo "================================================================" >&2
	echo "" >&2
	echo "You're using 'git rebase -i'. If your goal is to SQUASH commits:" >&2
	echo "" >&2
	echo "  - Ad-hoc squash produces CONCATENATED messages (all messages joined)" >&2
	echo "  - git-squash skill enforces UNIFIED messages (describes final result)" >&2
	echo "" >&2
	echo "RECOMMENDATION:" >&2
	echo "  Skill: git-squash" >&2
	echo "" >&2
	echo "If you're reordering commits (not squashing), proceed with rebase." >&2
	echo "================================================================" >&2
	echo "" >&2
fi

# Allow command to proceed (reminder only, not blocking)
exit 0
