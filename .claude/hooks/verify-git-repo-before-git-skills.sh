#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in verify-git-repo-before-git-skills.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# verify-git-repo-before-git-skills.sh - Verifies agent is in a git repository before git-related skills
# Hook Type: PreToolUse (Skill tool)
# Trigger: Before Skill tool execution when skill name starts with "git-"
#
# Purpose: Prevent git-related skills (git-squash, git-rebase, git-commit, etc.) from
#   failing with "fatal: not a git repository" errors by validating the working directory
#   is inside a git repository BEFORE the skill executes.
#
# Root Cause: After context resumption, agent may be in /workspace instead of the task
#   worktree. Git skills then fail immediately with repository errors.
#
# ADDED: 2026-01-06 after learn-from-mistakes investigation for
#   task add-anonymous-inner-class-support - git-squash failed from /workspace

# Read JSON from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	# No input available, exit gracefully
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Exit if no input
if [ -z "$JSON_INPUT" ]; then
	exit 0
fi

# Extract tool and skill names
if [ -f "/workspace/.claude/hooks/lib/json-parser.sh" ]; then
	source "/workspace/.claude/hooks/lib/json-parser.sh"
	TOOL_NAME=$(extract_json_value "$JSON_INPUT" "tool_name")
	SKILL_NAME=$(extract_json_value "$JSON_INPUT" "skill")
else
	# Fallback extraction
	TOOL_NAME=$(echo "$JSON_INPUT" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
	SKILL_NAME=$(echo "$JSON_INPUT" | sed -n 's/.*"skill"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

# Only process Skill tool calls
if [ "$TOOL_NAME" != "Skill" ]; then
	exit 0
fi

# Only check git-related skills (git-squash, git-rebase, git-commit, git-amend, git-merge-linear)
if [[ ! "$SKILL_NAME" =~ ^git- ]]; then
	exit 0
fi

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
	# Not in a git repository - block with helpful error
	cat << 'EOF' >&2

================================================================================
❌ ERROR: NOT IN A GIT REPOSITORY
================================================================================

You are trying to run the git-related skill from a directory that is NOT
inside a git repository.

Current directory:
EOF
	pwd >&2
	cat << 'EOF' >&2

SOLUTION: Change to the task worktree first:

    cd /workspace/tasks/<task-name>/code

Then verify:

    pwd
    git branch --show-current

After confirming you're in the correct git repository, retry the skill.

This error commonly occurs after context resumption when the agent is in
/workspace instead of the task worktree.

================================================================================

EOF
	# Exit with non-zero to block the skill execution
	exit 1
fi

# In a git repository - allow skill to proceed
exit 0
