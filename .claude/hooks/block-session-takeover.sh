#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-session-takeover.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper for proper hook blocking
source /workspace/.claude/scripts/json-output.sh

# block-session-takeover.sh
#
# PURPOSE: Prevent Claude instances from modifying session_id in task.json to take ownership
# TRIGGER: PreToolUse for Bash tool
#
# ADDED: 2025-12-29 after main agent modified task.json session_id to "take ownership"
# of a task owned by a different session, bypassing the verify-task-ownership skill
#
# ROOT CAUSE: Agent saw different session_id but rationalized "since this is my task
# to work on, I should take ownership" instead of using verify-task-ownership skill.
#
# TRIGGERING THOUGHT:
# "Wait - the session_id is different, but since this is my task to work on, I should take ownership."
#
# PREVENTS: Session hijacking where agent modifies task.json to change ownership
# without proper verification that the original session is abandoned.

# Parse hook input from stdin
INPUT=$(cat)

# Extract tool info
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // empty')

# Only check Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
  exit 0
fi

# Extract command
COMMAND=$(echo "$INPUT" | jq -r '.tool.input.command // empty')
if [[ -z "$COMMAND" ]]; then
  exit 0
fi

# Pattern 1: jq with session_id modification targeting task.json
# Matches: jq '.session_id = "..."' task.json
#          jq '.session_id = ...' /workspace/tasks/*/task.json
if [[ "$COMMAND" =~ jq.*session_id.*task\.json ]]; then
  cat >&2 <<'EOF'

================================================================================
BLOCKED: TASK OWNERSHIP TAKEOVER ATTEMPT DETECTED

You're trying to modify session_id in a task.json file directly.
This is a PROHIBITED action that bypasses multi-instance coordination.

================================================================================

**What you tried to do:**
Modify task.json to change the session_id and "take ownership" of a task.

**Why this is blocked:**
1. Tasks are owned by the session that initialized them
2. Directly modifying session_id bypasses ownership verification
3. The original session may still be working on this task
4. This violates multi-instance coordination protocol

**CORRECT APPROACH - Use verify-task-ownership skill:**

1. Invoke the verify-task-ownership skill:
   ```
   Skill: verify-task-ownership
   Args: <task-name>
   ```

2. The skill will check:
   - If task is owned by your session -> Safe to proceed
   - If task is owned by different session -> Choose different task
   - If task appears abandoned -> Provides safe cleanup commands

**If the task is truly abandoned:**
Clean up the abandoned task first, then re-initialize with your session:

   # Clean up abandoned task
   git worktree remove /workspace/tasks/<task>/code --force
   rm -rf /workspace/tasks/<task>
   git branch -D <task> 2>/dev/null || true

   # Re-initialize with task-init skill
   Skill: task-init
   Args: <task-name> <session-id>

**NEVER:**
- Modify session_id directly in task.json
- Assume "taking ownership" is the right action when session_id differs
- Override multi-instance coordination for convenience

================================================================================
Protocol Reference: CLAUDE.md § Multi-Instance Coordination
Skill Reference: .claude/skills/verify-task-ownership/SKILL.md
================================================================================

EOF

  output_hook_block "Blocked: Cannot modify session_id in task.json directly. Use verify-task-ownership skill instead." ""
  exit 0
fi

# Pattern 2: sed/awk modifying session_id in task.json
if [[ "$COMMAND" =~ (sed|awk).*session_id.*task\.json ]]; then
  cat >&2 <<'EOF'

================================================================================
BLOCKED: TASK OWNERSHIP TAKEOVER ATTEMPT DETECTED

You're trying to modify session_id in task.json using sed/awk.
This is a PROHIBITED action. Use verify-task-ownership skill instead.
================================================================================

EOF

  output_hook_block "Blocked: Cannot modify session_id in task.json directly. Use verify-task-ownership skill instead." ""
  exit 0
fi

# Pattern 3: echo/cat redirecting session_id content to task.json
if [[ "$COMMAND" =~ session_id.*\>.*task\.json ]]; then
  cat >&2 <<'EOF'

================================================================================
BLOCKED: TASK OWNERSHIP TAKEOVER ATTEMPT DETECTED

You're trying to overwrite task.json with modified session_id.
This is a PROHIBITED action. Use verify-task-ownership skill instead.
================================================================================

EOF

  output_hook_block "Blocked: Cannot modify session_id in task.json directly. Use verify-task-ownership skill instead." ""
  exit 0
fi

# Allow other commands
exit 0
