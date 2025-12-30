#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-main-task-branch-checkout.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# block-main-task-branch-checkout.sh - BLOCKS git checkout/switch of task branches in /workspace/main
# Hook Type: PreToolUse (Bash tool with git checkout/switch command)
# Trigger: Before Bash tool execution when command contains 'git checkout' or 'git switch'
#
# Purpose: Enforce task worktree isolation by preventing checkout of task branches in main repo
#
# ADDED: 2025-12-30 after main agent worked in /workspace/main on add-qualified-this-super branch
# instead of using the task worktree at /workspace/tasks/add-qualified-this-super/code/
# This violated task protocol isolation - all task work should happen in task worktrees.
#
# The mistake pattern:
# 1. Task was initialized with task-init (creates worktrees)
# 2. Instead of cd'ing to task worktree, agent ran 'git checkout add-qualified-this-super' in /workspace/main
# 3. All subsequent work happened in /workspace/main on the task branch
# 4. /workspace/main is now on a task branch instead of main

# Read JSON from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
    # No input available, exit gracefully
    exit 0
else
    JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Source JSON parsing library
source "/workspace/.claude/hooks/lib/json-parser.sh"

# Extract tool name and command from JSON
TOOL_NAME=$(extract_json_value "$JSON_INPUT" "tool_name")
GIT_COMMAND=$(extract_json_value "$JSON_INPUT" "command")

# Fallback extraction if primary method fails
if [ -z "$TOOL_NAME" ]; then
    TOOL_NAME=$(echo "$JSON_INPUT" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

if [ -z "$GIT_COMMAND" ]; then
    GIT_COMMAND=$(echo "$JSON_INPUT" | sed -n 's/.*"command"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

# Only process Bash tool calls
if [ "$TOOL_NAME" != "Bash" ]; then
    exit 0
fi

# Check if command contains git checkout or git switch
if ! echo "$GIT_COMMAND" | grep -qE "(git[[:space:]]+(checkout|switch))"; then
    exit 0  # Not a checkout/switch command
fi

# Check if we're in /workspace/main or if pwd would put us there
# Extract any cd command that precedes the git command
if echo "$GIT_COMMAND" | grep -qE "(^|[;&|])[[:space:]]*cd[[:space:]]+(//workspace/main|/workspace/main)"; then
    # Command explicitly changes to /workspace/main before checkout
    WORKING_DIR="/workspace/main"
elif [[ "$(pwd)" == "/workspace/main" ]]; then
    # Current directory is /workspace/main
    WORKING_DIR="/workspace/main"
else
    # Not in /workspace/main, allow the command
    exit 0
fi

# Extract the branch name being checked out
# Handle: git checkout <branch>, git switch <branch>, git checkout -b <branch>
BRANCH_NAME=""
if echo "$GIT_COMMAND" | grep -qE "checkout[[:space:]]+-b[[:space:]]+"; then
    # Creating new branch - extract name after -b
    BRANCH_NAME=$(echo "$GIT_COMMAND" | sed -n 's/.*checkout[[:space:]]*-b[[:space:]]*\([^[:space:];|&]*\).*/\1/p')
elif echo "$GIT_COMMAND" | grep -qE "checkout[[:space:]]+"; then
    # Regular checkout - extract branch name (skip flags like -f, -q)
    BRANCH_NAME=$(echo "$GIT_COMMAND" | sed -n 's/.*checkout[[:space:]]*\(-[a-z]*[[:space:]]*\)*\([^[:space:];|&-][^[:space:];|&]*\).*/\2/p')
elif echo "$GIT_COMMAND" | grep -qE "switch[[:space:]]+-c[[:space:]]+"; then
    # Creating new branch with switch -c
    BRANCH_NAME=$(echo "$GIT_COMMAND" | sed -n 's/.*switch[[:space:]]*-c[[:space:]]*\([^[:space:];|&]*\).*/\1/p')
elif echo "$GIT_COMMAND" | grep -qE "switch[[:space:]]+"; then
    # Regular switch
    BRANCH_NAME=$(echo "$GIT_COMMAND" | sed -n 's/.*switch[[:space:]]*\(-[a-z]*[[:space:]]*\)*\([^[:space:];|&-][^[:space:];|&]*\).*/\2/p')
fi

# If we couldn't extract a branch name, allow the command
if [ -z "$BRANCH_NAME" ]; then
    exit 0
fi

# Check if the branch name corresponds to an active task
# Task branches follow pattern: {task-name} or {task-name}-{agent}
TASK_NAME=""
for task_dir in /workspace/tasks/*/; do
    [ -d "$task_dir" ] || continue
    task=$(basename "$task_dir")

    # Check if branch name matches task or task-agent pattern
    if [[ "$BRANCH_NAME" == "$task" ]] || [[ "$BRANCH_NAME" == "$task"-* ]]; then
        # Found matching task - check if it's active
        TASK_JSON="${task_dir}task.json"
        if [ -f "$TASK_JSON" ]; then
            STATE=$(jq -r '.state // "UNKNOWN"' "$TASK_JSON" 2>/dev/null || echo "UNKNOWN")
            # Check if state is non-terminal
            if [[ "$STATE" != "COMPLETE" && "$STATE" != "ABANDONED" && "$STATE" != "CLEANUP" && "$STATE" != "UNKNOWN" ]]; then
                TASK_NAME="$task"
                break
            fi
        fi
    fi
done

# If no active task matches the branch, allow the command
if [ -z "$TASK_NAME" ]; then
    exit 0
fi

# VIOLATION DETECTED: Attempting to checkout task branch in /workspace/main
cat << EOF

============================================================================
    BLOCKED: Task Branch Checkout in Main Repository
============================================================================

COMMAND: $GIT_COMMAND
BRANCH:  $BRANCH_NAME
TASK:    $TASK_NAME (state: $STATE)

VIOLATION: You are attempting to check out a task branch in /workspace/main
           Task work MUST happen in the task worktree, not the main repository.

WHY THIS IS WRONG:
  - /workspace/main should ALWAYS stay on the 'main' branch
  - Task work happens in isolated worktrees to prevent conflicts
  - Checking out task branches in main creates confusion and violates isolation

CORRECT APPROACH:
  1. Work in the task worktree:
     cd /workspace/tasks/$TASK_NAME/code

  2. The task worktree is ALREADY on the task branch

  3. For agent work, use agent worktrees:
     /workspace/tasks/$TASK_NAME/agents/{agent}/code

VERIFY WORKTREE STATUS:
  git worktree list

TASK WORKTREE LOCATION:
  /workspace/tasks/$TASK_NAME/code

REFERENCE:
  - CLAUDE.md Task Worktrees section
  - task-init/SKILL.md Complete Task Startup Sequence

============================================================================
    COMMAND BLOCKED - Use task worktree instead
============================================================================
EOF

# Output JSON for permission system
echo '{"permissionDecision":"deny","reason":"Task branch checkout in main repository blocked. Use task worktree at /workspace/tasks/'"$TASK_NAME"'/code instead."}'
exit 2  # Exit code 2 signals permission denial
