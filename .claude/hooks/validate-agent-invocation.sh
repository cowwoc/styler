#!/bin/bash
# Validates that agent invocations occur in appropriate task states
# This hook prevents premature agent invocation (e.g., updater agents before IMPLEMENTATION)
#
# TRIGGER: PreToolUse for Task tool invocations
# CHECKS: Agent type vs current task state validity
# ACTION: Blocks invocation and displays state requirement if invalid
#
# Related Protocol Sections:
# - task-protocol-core.md § IMPLEMENTATION State
# - task-protocol-agents.md § Agent Invocation Timing

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in validate-agent-invocation.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

echo "[HOOK DEBUG] validate-agent-invocation.sh START" >&2

# Source logging helper
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"

# Read hook input from stdin
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool // empty')
AGENT_NAME=$(echo "$INPUT" | jq -r '.params.subagent_type // empty')

# Set up logging context
export HOOK_SESSION_ID="$SESSION_ID"
export HOOK_INPUT="$INPUT"

log_hook_start "validate-agent-invocation" "PreToolUse"

# Only validate Task tool invocations
if [[ "$TOOL_NAME" != "Task" ]] || [[ -z "$AGENT_NAME" ]]; then
	exit 0
fi

if [[ -z "$SESSION_ID" ]]; then
	exit 0
fi

# Find task owned by this session
TASKS_DIR="/workspace/tasks"
if [[ ! -d "$TASKS_DIR" ]]; then
	exit 0
fi

TASK_LOCK=""
TASK_NAME=""
CURRENT_STATE=""

# Find the task lock for this session
for task_dir in "$TASKS_DIR"/*; do
	if [[ ! -d "$task_dir" ]]; then
		continue
	fi

	LOCK_FILE="${task_dir}/task.json"
	if [[ ! -f "$LOCK_FILE" ]]; then
		continue
	fi

	LOCK_SESSION=$(jq -r '.session_id // ""' "$LOCK_FILE" 2>/dev/null || echo "")
	if [[ "$LOCK_SESSION" == "$SESSION_ID" ]]; then
		TASK_LOCK="$LOCK_FILE"
		TASK_NAME=$(basename "$task_dir")
		CURRENT_STATE=$(jq -r '.state // "UNKNOWN"' "$LOCK_FILE")
		break
	fi
done

# If no task found, allow (might be non-task agent invocation)
if [[ -z "$TASK_LOCK" ]]; then
	exit 0
fi

# Determine agent type from name
AGENT_TYPE="unknown"
if [[ "$AGENT_NAME" == *"-reviewer" ]]; then
	AGENT_TYPE="reviewer"
elif [[ "$AGENT_NAME" == *"-updater" ]]; then
	AGENT_TYPE="updater"
elif [[ "$AGENT_NAME" == "process-recorder" ]] || [[ "$AGENT_NAME" == "process-compliance-reviewer" ]] || [[ "$AGENT_NAME" == "process-efficiency-reviewer" ]]; then
	AGENT_TYPE="audit"
fi

# Validate agent invocation timing based on state and type
VIOLATION=""

case "$AGENT_TYPE" in
	"reviewer")
		# Reviewer agents can be invoked in REQUIREMENTS, VALIDATION, REVIEW states
		if [[ "$CURRENT_STATE" != "REQUIREMENTS" ]] && [[ "$CURRENT_STATE" != "VALIDATION" ]] && [[ "$CURRENT_STATE" != "REVIEW" ]]; then
			if [[ "$CURRENT_STATE" == "SYNTHESIS" ]] || [[ "$CURRENT_STATE" == "IMPLEMENTATION" ]]; then
				# Allow reviewer re-invocation during synthesis/implementation for clarification
				# but log as unusual
				LOG_FILE="/workspace/tasks/${TASK_NAME}/agent-invocations.log"
				echo "[$(date -Iseconds)] WARNING: Reviewer ${AGENT_NAME} invoked in ${CURRENT_STATE} state (unusual but permitted)" >> "$LOG_FILE"
			else
				VIOLATION="Reviewer agents should be invoked in REQUIREMENTS, VALIDATION, or REVIEW states. Current state: ${CURRENT_STATE}"
			fi
		fi
		;;

	"updater")
		# Updater agents MUST only be invoked in IMPLEMENTATION state
		if [[ "$CURRENT_STATE" != "IMPLEMENTATION" ]]; then
			VIOLATION="Updater agents can ONLY be invoked in IMPLEMENTATION state. Current state: ${CURRENT_STATE}"
		fi
		;;

	"audit")
		# Audit agents can be invoked in any state
		;;

	*)
		# Unknown agent type, allow but log
		LOG_FILE="/workspace/tasks/${TASK_NAME}/agent-invocations.log"
		echo "[$(date -Iseconds)] INFO: Unknown agent type invoked: ${AGENT_NAME} in ${CURRENT_STATE} state" >> "$LOG_FILE"
		;;
esac

# If violation detected, block invocation
if [[ -n "$VIOLATION" ]]; then
	log_hook_blocked "validate-agent-invocation" "PreToolUse" "Agent ${AGENT_NAME} blocked in ${CURRENT_STATE} state"
	LOG_FILE="/workspace/tasks/${TASK_NAME}/agent-invocation-violations.log"
	echo "[$(date -Iseconds)] BLOCKED: ${AGENT_NAME} invocation in ${CURRENT_STATE} state" >> "$LOG_FILE"

	MESSAGE="## 🚨 AGENT INVOCATION BLOCKED

**Task**: \`$TASK_NAME\`
**Agent**: \`$AGENT_NAME\` (${AGENT_TYPE})
**Current State**: \`$CURRENT_STATE\`
**Violation**: ${VIOLATION}

## ⚠️ AGENT INVOCATION RULES

**Reviewer Agents** (*-reviewer):
- ✅ Invoked during: REQUIREMENTS, VALIDATION, REVIEW
- ⚠️  Allowed but unusual: SYNTHESIS, IMPLEMENTATION (for clarifications)
- ❌ Invalid: INIT, CLASSIFIED, AWAITING_USER_APPROVAL, COMPLETE, CLEANUP

**Updater Agents** (*-updater):
- ✅ Invoked during: IMPLEMENTATION ONLY
- ❌ Invalid: All other states (including SYNTHESIS before user approval)

**Audit Agents** (process-*):
- ✅ Invoked during: Any state

## Required Action

**If you need to invoke \`$AGENT_NAME\`**:

1. **Check current task state**:
   \`\`\`bash
   cat /workspace/tasks/${TASK_NAME}/task.json | jq '.state'
   \`\`\`

2. **Transition to appropriate state** (if needed):
   - For reviewer agents: Ensure state is REQUIREMENTS, VALIDATION, or REVIEW
   - For updater agents: MUST be in IMPLEMENTATION state

3. **For updater agents specifically**:
   - Verify user approved implementation plan (see enforce-synthesis-checkpoint.sh)
   - Verify state is IMPLEMENTATION
   - Then invoke updater agent

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § Agent Invocation Rules

**Violation logged to**: ${LOG_FILE}"

	jq -n \
		--arg event "PreToolUse" \
		--arg context "$MESSAGE" \
		'{
			"hookSpecificOutput": {
				"hookEventName": $event,
				"additionalContext": $context
			}
		}'

	exit 1  # Block the tool invocation
fi

log_hook_success "validate-agent-invocation" "PreToolUse" "Agent ${AGENT_NAME} allowed in ${CURRENT_STATE} state"
echo "[HOOK DEBUG] validate-agent-invocation.sh END" >&2
exit 0
