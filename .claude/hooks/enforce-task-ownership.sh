#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in enforce-task-ownership.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# enforce-task-ownership.sh
#
# PURPOSE: Prevent Claude instances from working on tasks owned by other sessions.
# TRIGGER: PreToolUse for Read, Edit, Write, Bash tools
#
# ADDED: 2025-12-05 after main agent attempted to work on task owned by different session
# (session 7bbc5d24-97ca-4aaa-96c5-4e467474412b tried to access task owned by 14238d5a-97a5-403b-a1bb-c6205dc20f36)
#
# PREVENTS: Multi-instance coordination violations where one Claude instance
# accidentally works on tasks initialized by another instance.

# Parse hook input from stdin
INPUT=$(cat)

# Extract session_id and tool info
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // empty')

# Only check relevant tools
case "$TOOL_NAME" in
  Read|Edit|Write|Bash)
    ;;
  *)
    # Not a tool that accesses files - allow
    exit 0
    ;;
esac

# Extract file path from tool input based on tool type
case "$TOOL_NAME" in
  Read)
    TARGET_PATH=$(echo "$INPUT" | jq -r '.tool.input.file_path // empty')
    ;;
  Edit)
    TARGET_PATH=$(echo "$INPUT" | jq -r '.tool.input.file_path // empty')
    ;;
  Write)
    TARGET_PATH=$(echo "$INPUT" | jq -r '.tool.input.file_path // empty')
    ;;
  Bash)
    # For Bash, extract paths from command - look for /workspace/tasks patterns
    COMMAND=$(echo "$INPUT" | jq -r '.tool.input.command // empty')
    # Extract task directory if command references /workspace/tasks/*/
    if [[ "$COMMAND" =~ /workspace/tasks/([a-z0-9-]+) ]]; then
      TASK_NAME="${BASH_REMATCH[1]}"
      TARGET_PATH="/workspace/tasks/$TASK_NAME"
    else
      # No task path in command - allow
      exit 0
    fi
    ;;
esac

# Check if path is within /workspace/tasks/
if [[ ! "$TARGET_PATH" =~ ^/workspace/tasks/([a-z0-9-]+) ]]; then
  # Not accessing a task directory - allow
  exit 0
fi

# Extract task name from path
TASK_NAME="${BASH_REMATCH[1]}"
TASK_JSON="/workspace/tasks/$TASK_NAME/task.json"

# Check if task.json exists
if [ ! -f "$TASK_JSON" ]; then
  # No task.json - might be new task initialization, allow
  exit 0
fi

# Read task's session_id
TASK_SESSION_ID=$(jq -r '.session_id // empty' "$TASK_JSON" 2>/dev/null || echo "")

# If task has no session_id, allow (legacy or incomplete setup)
if [ -z "$TASK_SESSION_ID" ]; then
  exit 0
fi

# If current session matches task session, allow
if [ "$SESSION_ID" = "$TASK_SESSION_ID" ]; then
  exit 0
fi

# BLOCK - Different session owns this task
cat >&2 <<EOF

🚨 TASK OWNERSHIP VIOLATION BLOCKED

**Your Session**: $SESSION_ID
**Task Owner**: $TASK_SESSION_ID
**Task**: $TASK_NAME
**Tool**: $TOOL_NAME
**Target**: $TARGET_PATH

This task is owned by a DIFFERENT Claude instance.

**What happened**:
Another Claude session initialized this task and owns the lock.
Working on it from your session would cause coordination conflicts.

**Actions**:
1. Choose a DIFFERENT task from todo.md that is not locked
2. Or wait for the other session to complete/release this task
3. If the task is abandoned, clean it up first:

   # Remove worktrees
   git worktree remove /workspace/tasks/$TASK_NAME/code --force
   git worktree remove /workspace/tasks/$TASK_NAME/agents/architect/code --force 2>/dev/null || true
   git worktree remove /workspace/tasks/$TASK_NAME/agents/tester/code --force 2>/dev/null || true
   git worktree remove /workspace/tasks/$TASK_NAME/agents/formatter/code --force 2>/dev/null || true

   # Remove task directory
   rm -rf /workspace/tasks/$TASK_NAME

   # Remove branches
   git branch -D $TASK_NAME 2>/dev/null || true
   git branch -D $TASK_NAME-architect 2>/dev/null || true
   git branch -D $TASK_NAME-tester 2>/dev/null || true
   git branch -D $TASK_NAME-formatter 2>/dev/null || true

EOF

# Return deny decision
jq -n '{
  "decision": "block",
  "reason": "Task owned by different session"
}'
exit 2
