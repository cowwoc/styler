#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in remind-cd-after-task-init.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# remind-cd-after-task-init.sh - Reminds agent to cd to task worktree after task-init
# Hook Type: PostToolUse (Skill tool)
# Trigger: After Skill tool execution when skill is "task-init"
#
# Purpose: Enforce the CRITICAL requirement from task-init/SKILL.md lines 139-141:
#   "After task-init, you MUST change to the task worktree before continuing."
#
# Root Cause: Agent completed task-init but forgot to cd to task worktree,
#   continuing to work from /workspace/main. This caused "not a git repository"
#   errors and potential wrong-context operations.
#
# ADDED: 2025-12-31 after learn-from-mistakes investigation for
#   task add-array-dimension-annotations

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

# Source JSON parsing library if available
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

# Check if this was task-init skill
if [ "$SKILL_NAME" != "task-init" ]; then
	exit 0
fi

# Check if the skill was successful by looking for success indicators in result
RESULT=$(echo "$JSON_INPUT" | sed -n 's/.*"result"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')

# Extract task name from result if available (looking for task directory pattern)
TASK_NAME=$(echo "$JSON_INPUT" | grep -oE 'task_name[^:]*:[^"]*"[^"]*"' | head -1 | sed 's/.*"\([^"]*\)".*/\1/')

if [ -z "$TASK_NAME" ]; then
	# Try to extract from task_dir pattern
	TASK_NAME=$(echo "$JSON_INPUT" | grep -oE '/workspace/tasks/[^/"]+' | head -1 | sed 's|.*/||')
fi

# Output reminder to stderr (visible to agent)
cat << EOF >&2

================================================================================
MANDATORY NEXT STEP: CHANGE TO TASK WORKTREE
================================================================================

Task initialization complete. You MUST now change to the task worktree:

    cd /workspace/tasks/${TASK_NAME:-<task-name>}/code

Then verify you're in the correct location:

    pwd
    # Expected: /workspace/tasks/${TASK_NAME:-<task-name>}/code

    git branch --show-current
    # Expected: ${TASK_NAME:-<task-name>}

DO NOT continue working from /workspace/main or /workspace!

All subsequent task operations MUST run from the task worktree.

Reference: task-init/SKILL.md lines 139-141
================================================================================

EOF

# Exit successfully (reminder only, not blocking)
exit 0
