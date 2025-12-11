#!/bin/bash
# Validates that agent invocations occur in appropriate task states
# This hook prevents premature agent invocation (e.g., agents in IMPLEMENTATION mode before IMPLEMENTATION state)
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


# Source logging helper
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"

# Read hook input from stdin
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
AGENT_NAME=$(echo "$INPUT" | jq -r '.tool_input.subagent_type // empty')

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

# Stakeholder agents (can operate in REQUIREMENTS/VALIDATION or IMPLEMENTATION modes)
# Note: The mode is determined by state + model parameter, not agent name
STAKEHOLDER_AGENTS=("architect" "engineer" "formatter" "tester" "builder" "designer" "optimizer" "hacker" "configurator")
for stakeholder in "${STAKEHOLDER_AGENTS[@]}"; do
	if [[ "$AGENT_NAME" == "$stakeholder" ]]; then
		# Determine mode based on model parameter if available, otherwise allow all states
		MODEL=$(echo "$INPUT" | jq -r '.tool_input.model // empty')
		if [[ "$MODEL" == "haiku" ]] || [[ "$MODEL" == *"haiku"* ]]; then
			AGENT_TYPE="updater"  # Implementation mode (Haiku)
		elif [[ "$MODEL" == "opus" ]] || [[ "$MODEL" == *"opus"* ]]; then
			AGENT_TYPE="reviewer"  # Requirements/Validation mode (Opus)
		else
			# No model specified, infer from state
			if [[ "$CURRENT_STATE" == "REQUIREMENTS" ]] || [[ "$CURRENT_STATE" == "VALIDATION" ]] || [[ "$CURRENT_STATE" == "REVIEW" ]]; then
				AGENT_TYPE="reviewer"
			elif [[ "$CURRENT_STATE" == "IMPLEMENTATION" ]]; then
				AGENT_TYPE="updater"
			fi
		fi
		break
	fi
done

# Legacy pattern support (for backward compatibility with old tests)
if [[ "$AGENT_TYPE" == "unknown" ]]; then
	if [[ "$AGENT_NAME" == *"-reviewer" ]]; then
		AGENT_TYPE="reviewer"
	elif [[ "$AGENT_NAME" == *"-updater" ]]; then
		AGENT_TYPE="updater"
	fi
fi

# Audit agents
if [[ "$AGENT_NAME" == "process-recorder" ]] || [[ "$AGENT_NAME" == "process-compliance-reviewer" ]] || [[ "$AGENT_NAME" == "process-efficiency-reviewer" ]]; then
	AGENT_TYPE="audit"
fi

# Validate agent invocation timing based on state and type
VIOLATION=""

case "$AGENT_TYPE" in
	"reviewer")
		# Stakeholder agents in REQUIREMENTS/VALIDATION mode can be invoked in REQUIREMENTS, VALIDATION, REVIEW states
		if [[ "$CURRENT_STATE" != "REQUIREMENTS" ]] && [[ "$CURRENT_STATE" != "VALIDATION" ]] && [[ "$CURRENT_STATE" != "REVIEW" ]]; then
			if [[ "$CURRENT_STATE" == "SYNTHESIS" ]] || [[ "$CURRENT_STATE" == "IMPLEMENTATION" ]]; then
				# Allow re-invocation during synthesis/implementation for clarification
				# but log as unusual
				LOG_FILE="/workspace/tasks/${TASK_NAME}/agent-invocations.log"
				echo "[$(date -Iseconds)] WARNING: Agent ${AGENT_NAME} in REQUIREMENTS/VALIDATION mode invoked in ${CURRENT_STATE} state (unusual but permitted)" >> "$LOG_FILE"
			else
				VIOLATION="Stakeholder agents in REQUIREMENTS/VALIDATION mode should be invoked in REQUIREMENTS, VALIDATION, or REVIEW states. Current state: ${CURRENT_STATE}"
			fi
		fi
		;;

	"updater")
		# Stakeholder agents in IMPLEMENTATION mode MUST only be invoked in IMPLEMENTATION state
		if [[ "$CURRENT_STATE" != "IMPLEMENTATION" ]]; then
			VIOLATION="Stakeholder agents in IMPLEMENTATION mode can ONLY be invoked in IMPLEMENTATION state. Current state: ${CURRENT_STATE}"
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

**Stakeholder Agents in REQUIREMENTS/VALIDATION Mode** (*-reviewer):
- ✅ Invoked during: REQUIREMENTS, VALIDATION, REVIEW
- ⚠️  Allowed but unusual: SYNTHESIS, IMPLEMENTATION (for clarifications)
- ❌ Invalid: INIT, CLASSIFIED, AWAITING_USER_APPROVAL, COMPLETE, CLEANUP

**Stakeholder Agents in IMPLEMENTATION Mode** (*-updater):
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
   - For stakeholder agents in REQUIREMENTS/VALIDATION mode: Ensure state is REQUIREMENTS, VALIDATION, or REVIEW
   - For stakeholder agents in IMPLEMENTATION mode: MUST be in IMPLEMENTATION state

3. **For stakeholder agents in IMPLEMENTATION mode specifically**:
   - Verify user approved implementation plan (see enforce-synthesis-checkpoint.sh)
   - Verify state is IMPLEMENTATION
   - Then invoke agent

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

	exit 2  # Block the tool invocation
fi

log_hook_success "validate-agent-invocation" "PreToolUse" "Agent ${AGENT_NAME} allowed in ${CURRENT_STATE} state"
exit 0
