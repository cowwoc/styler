#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-main-rebase.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Block Rebase on Main Branch Hook
# Prevents git rebase commands when on main branch
#
# TRIGGER: PreToolUse (Bash)
#
# BLOCKED OPERATIONS:
# 1. git rebase (any form) when on main branch
#
# RATIONALE:
# After merging task branches to main, rebasing main rewrites history,
# making merged commits appear as direct commits on main. This breaks
# the audit trail and violates task protocol.
#
# ALLOWED:
# - Rebasing on task branches (for squashing before merge)
# - Rebasing on agent branches
# - Any non-rebase git operations on main

# Read JSON from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Source JSON parsing library
source "/workspace/.claude/hooks/lib/json-parser.sh"

# Source helper for proper hook blocking
source /workspace/.claude/scripts/json-output.sh

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

# Check if command contains git rebase
COMMAND_LOWER=$(echo "$COMMAND" | tr '[:upper:]' '[:lower:]')
if ! echo "$COMMAND_LOWER" | grep -qE "(^|[;&|])[[:space:]]*git[[:space:]]+rebase"; then
	echo '{}'
	exit 0
fi

# Get current branch (try multiple methods)
CURRENT_BRANCH=""

# Method 1: Check if command changes to main first
if echo "$COMMAND" | grep -qE "cd[[:space:]]+(/workspace/main|['\"]*/workspace/main['\"]*)"; then
	CURRENT_BRANCH="main"
fi

# Method 2: Check current working directory's branch
# BUG FIX: 2025-12-15 - Was checking /workspace/main's branch instead of $PWD's branch
# This caused false positives when rebasing task branches from their worktrees
# because it would see main's branch is "main" and block, even though
# the actual command runs in a task worktree on a different branch
if [ -z "$CURRENT_BRANCH" ]; then
	CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")
fi

# Method 3: Check if we're in a task worktree (not main)
if [ -z "$CURRENT_BRANCH" ]; then
	# Look for task.json to identify if in task context
	if ls /workspace/tasks/*/task.json 2>/dev/null | head -1 | grep -q .; then
		# In task context, likely not main
		echo '{}'
		exit 0
	fi
fi

# If on main branch, block the rebase
if [ "$CURRENT_BRANCH" = "main" ]; then
	cat << EOF >&2

🚨 REBASE ON MAIN BLOCKED
═══════════════════════════════════════════════════════════════

❌ Attempted: git rebase on main branch
✅ Correct:   Main branch should never be rebased

WHY THIS IS BLOCKED:
• Rebasing main rewrites commit history
• Merged task commits get recreated as direct commits
• This breaks the audit trail (commits appear on main, not task branch)
• Violates task protocol's linear history requirement

WHAT TO DO INSTEAD:
• If you need to fix a commit message: Use git commit --amend on the task
  branch BEFORE merging to main
• If already merged: The history is final. Accept it or create a new commit
• For commit reordering: Do this on task branches, not main

CORRECT WORKFLOW:
1. All changes on task branch
2. Squash/rebase on task branch (before merge)
3. Merge to main with --ff-only
4. Never rebase main

═══════════════════════════════════════════════════════════════
❌ COMMAND BLOCKED - Rebase on main is prohibited
EOF
	output_hook_block "Blocked: git rebase on main branch is prohibited. Rebase rewrites history and breaks audit trail."
	exit 0
fi

# Allow rebase on non-main branches
echo '{}'
exit 0
