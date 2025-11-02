#!/bin/bash
# Pre-Tool-Use Hook: Validate Task tool invocations require proper task state
#
# ADDED: 2025-11-01 after main agent invoked architect from INIT state during
# implement-formatter-api task, bypassing state machine and user approval checkpoints
#
# PREVENTS: Task tool invocations before proper state machine progression
# ENFORCES: Task tool requires state=CLASSIFIED or IMPLEMENTATION
#
# Triggers: PreToolUse (tool:Task matcher in settings.json)

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in pre-tool-use-task-invoke.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")

# Only validate Task tool
if [[ "$TOOL_NAME" != "Task" ]]; then
	exit 0
fi

log_hook_start "pre-tool-use-task-invoke" "PreToolUse"

# Check if we're in a task context
TASKS_DIR="/workspace/tasks"
if [[ ! -d "$TASKS_DIR" ]]; then
	log_hook_success "pre-tool-use-task-invoke" "PreToolUse" "No tasks directory, allowing Task invocation"
	exit 0
fi

# Find any task.json files in tasks directory
TASK_JSON_FILES=$(find "$TASKS_DIR" -maxdepth 2 -name "task.json" 2>/dev/null || true)

if [[ -z "$TASK_JSON_FILES" ]]; then
	log_hook_success "pre-tool-use-task-invoke" "PreToolUse" "No task.json found, allowing Task invocation"
	exit 0
fi

# Check each task.json for state
VIOLATION_FOUND=false
VIOLATING_TASK=""
CURRENT_STATE=""

while IFS= read -r TASK_JSON; do
	if [[ ! -f "$TASK_JSON" ]]; then
		continue
	fi

	STATE=$(jq -r '.state // "UNKNOWN"' "$TASK_JSON" 2>/dev/null || echo "UNKNOWN")
	TASK_NAME=$(basename "$(dirname "$TASK_JSON")")

	# Task tool is only allowed in CLASSIFIED or IMPLEMENTATION states
	# INIT state requires state machine progression first
	if [[ "$STATE" == "INIT" ]]; then
		VIOLATION_FOUND=true
		VIOLATING_TASK="$TASK_NAME"
		CURRENT_STATE="$STATE"
		break
	fi
done <<< "$TASK_JSON_FILES"

if [[ "$VIOLATION_FOUND" == true ]]; then
	log_hook_blocked "pre-tool-use-task-invoke" "PreToolUse" "Task tool invocation blocked - state=INIT (requires CLASSIFIED or IMPLEMENTATION)"

	MESSAGE="## 🚨 TASK TOOL INVOCATION BLOCKED

**Task**: \`$VIOLATING_TASK\`
**Current State**: \`$CURRENT_STATE\`
**Violation**: Task tool requires proper state machine progression

## ⚠️ CRITICAL - STATE MACHINE PROGRESSION REQUIRED

You attempted to invoke the Task tool while task.json state is INIT.
This bypasses the task protocol state machine and user approval checkpoints.

**AUTOMATIC ACTION TAKEN**:
- Task tool invocation blocked
- Violation logged

**REQUIRED ACTION - Follow Task Protocol State Machine**:

1. **Transition to CLASSIFIED state**:
   \`\`\`bash
   jq '.state = \"CLASSIFIED\"' /workspace/tasks/${VIOLATING_TASK}/task.json > /tmp/task.tmp
   mv /tmp/task.tmp /workspace/tasks/${VIOLATING_TASK}/task.json
   \`\`\`

2. **Create task.md with stakeholder requirements**:
   \`\`\`bash
   cat > /workspace/tasks/${VIOLATING_TASK}/task.md <<EOF
   # Task: ${VIOLATING_TASK}

   ## Requirements
   [Define what stakeholder agents should implement]

   ## Acceptance Criteria
   [Define success criteria]
   EOF
   \`\`\`

3. **Transition to SYNTHESIS state and create implementation plan**:
   \`\`\`bash
   jq '.state = \"SYNTHESIS\"' /workspace/tasks/${VIOLATING_TASK}/task.json > /tmp/task.tmp
   mv /tmp/task.tmp /workspace/tasks/${VIOLATING_TASK}/task.json

   # Add implementation plan to task.md
   \`\`\`

4. **PRESENT PLAN TO USER and WAIT for approval**:
   - Show implementation approach
   - Wait for user to say \"approved\", \"proceed\", or \"looks good\"

5. **After user approval, create approval flag**:
   \`\`\`bash
   touch /workspace/tasks/${VIOLATING_TASK}/user-approved-synthesis.flag
   \`\`\`

6. **Transition to IMPLEMENTATION state**:
   \`\`\`bash
   jq '.state = \"IMPLEMENTATION\"' /workspace/tasks/${VIOLATING_TASK}/task.json > /tmp/task.tmp
   mv /tmp/task.tmp /workspace/tasks/${VIOLATING_TASK}/task.json
   \`\`\`

7. **NOW you can invoke Task tool** (this block will be removed)

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § State Machine

**State Progression**:
INIT → CLASSIFIED (create task.md) → SYNTHESIS (plan) → user approval → IMPLEMENTATION (invoke agents)

**Why This Matters**:
- User approval checkpoints are tied to state transitions
- Hooks only enforce protocol when state machine is used
- Bypassing states disables all protocol enforcement"

	output_hook_error "PreToolUse" "$MESSAGE"
	exit 0
fi

log_hook_success "pre-tool-use-task-invoke" "PreToolUse" "Task tool invocation allowed - proper state"
exit 0
