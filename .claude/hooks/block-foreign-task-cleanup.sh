#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-foreign-task-cleanup.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper for proper hook blocking
source /workspace/.claude/scripts/json-output.sh

# block-foreign-task-cleanup.sh
#
# PURPOSE: Prevent Claude instances from cleaning up tasks owned by other sessions
# without first verifying ownership via verify-task-ownership skill.
# TRIGGER: PreToolUse for Bash tool
#
# ADDED: 2025-12-29 after main agent deleted another session's tasks during SessionStart
# cleanup without using verify-task-ownership skill.
#
# ROOT CAUSE: SessionStart hook detected incomplete tasks with different session_ids
# and warned "Use verify-task-ownership skill if unsure", but agent rationalized
# "These are ABANDONED tasks... safe to clean up" and deleted without verification.
#
# TRIGGERING THOUGHT:
# "These are ABANDONED tasks, not completed ones. Since the sessions are no longer active...
# these are safe to clean up."
#
# PREVENTS: Cleanup of tasks owned by other sessions without verification

# Parse hook input from stdin
INPUT=$(cat)

# Extract tool info and session
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // empty')
CURRENT_SESSION=$(echo "$INPUT" | jq -r '.session_id // empty')

# Only check Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
  exit 0
fi

# Extract command
COMMAND=$(echo "$INPUT" | jq -r '.tool.input.command // empty')
if [[ -z "$COMMAND" ]]; then
  exit 0
fi

# Check if command targets a task directory
# Patterns: /workspace/tasks/{task-name}, /workspace/tasks/{task-name}/*
TASK_PATTERN='/workspace/tasks/([a-z0-9-]+)'
if [[ ! "$COMMAND" =~ $TASK_PATTERN ]]; then
  # Not targeting a task directory
  exit 0
fi

TASK_NAME="${BASH_REMATCH[1]}"
TASK_JSON="/workspace/tasks/$TASK_NAME/task.json"

# Check if this is a destructive/cleanup command
IS_CLEANUP=false
CLEANUP_TYPE=""

# Pattern 1: git worktree remove
if [[ "$COMMAND" =~ git[[:space:]]+worktree[[:space:]]+remove ]]; then
  IS_CLEANUP=true
  CLEANUP_TYPE="git worktree remove"
fi

# Pattern 2: rm -rf (with force)
if [[ "$COMMAND" =~ rm[[:space:]]+-rf[[:space:]] ]] || [[ "$COMMAND" =~ rm[[:space:]]+-r[[:space:]]+-f[[:space:]] ]]; then
  IS_CLEANUP=true
  CLEANUP_TYPE="rm -rf"
fi

# Pattern 3: git branch -D (force delete)
if [[ "$COMMAND" =~ git[[:space:]]+branch[[:space:]]+-[dD] ]]; then
  IS_CLEANUP=true
  CLEANUP_TYPE="git branch delete"
fi

# Pattern 4: rmdir
if [[ "$COMMAND" =~ rmdir ]]; then
  IS_CLEANUP=true
  CLEANUP_TYPE="rmdir"
fi

# Not a cleanup command
if [[ "$IS_CLEANUP" != "true" ]]; then
  exit 0
fi

# Check if task.json exists
if [[ ! -f "$TASK_JSON" ]]; then
  # No task.json - can't verify ownership, allow cleanup
  # (task may be partially created or corrupted)
  exit 0
fi

# Read task's session_id
TASK_SESSION_ID=$(jq -r '.session_id // empty' "$TASK_JSON" 2>/dev/null || echo "")

# If task has no session_id, allow (legacy or incomplete setup)
if [[ -z "$TASK_SESSION_ID" ]]; then
  exit 0
fi

# If current session matches task session, allow cleanup
if [[ "$CURRENT_SESSION" == "$TASK_SESSION_ID" ]]; then
  exit 0
fi

# BLOCK - Trying to clean up a task owned by different session
cat >&2 <<EOF

================================================================================
🚫 BLOCKED: FOREIGN TASK CLEANUP WITHOUT VERIFICATION

You're attempting to clean up a task owned by a DIFFERENT Claude session
without first verifying that session is no longer active.

================================================================================

**Command:** $CLEANUP_TYPE
**Task:** $TASK_NAME
**Your Session:** $CURRENT_SESSION
**Task Owner:** $TASK_SESSION_ID

**Why this is blocked:**
The SessionStart hook warned you to "Use verify-task-ownership skill if unsure"
before cleaning up tasks with different session_ids. You cannot assume tasks
are abandoned just because they appear incomplete.

**MANDATORY VERIFICATION STEPS:**

1. **First, invoke the verify-task-ownership skill:**
   \`\`\`
   Skill: verify-task-ownership
   Args: $TASK_NAME
   \`\`\`

2. The skill will check:
   - If task is truly abandoned -> Provides safe cleanup commands
   - If task is owned by active session -> Do NOT clean up
   - If uncertain -> Ask user before proceeding

3. **Only AFTER verification confirms abandonment**, run cleanup commands.

**PROHIBITED RATIONALIZATIONS:**
- ❌ "These are ABANDONED tasks... safe to clean up" (without verification)
- ❌ "The sessions are no longer active" (how do you know?)
- ❌ "Tasks in SYNTHESIS with agents not_started are abandoned" (speculation)

**The verify-task-ownership skill exists precisely for this scenario.**

================================================================================
Protocol Reference: CLAUDE.md § Multi-Instance Coordination
Skill Reference: .claude/skills/verify-task-ownership/SKILL.md
================================================================================

EOF

output_hook_block "Blocked: Cannot cleanup task '$TASK_NAME' owned by session $TASK_SESSION_ID. Use verify-task-ownership skill first." ""
exit 0
