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
source /workspace/.claude/scripts/json-output.sh

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

# Determine agent mode based on model parameter
# Mode determines which states the agent can be invoked in:
#   - requirements: REQUIREMENTS, VALIDATION, REVIEW states (uses Opus model)
#   - implementation: IMPLEMENTATION state only (uses Haiku model)
#   - audit: Any state (special audit agents)
AGENT_MODE="unknown"

# Stakeholder agents - mode is determined by model parameter
STAKEHOLDER_AGENTS=("architect" "engineer" "formatter" "tester" "builder" "designer" "optimizer" "hacker" "configurator")
for stakeholder in "${STAKEHOLDER_AGENTS[@]}"; do
	if [[ "$AGENT_NAME" == "$stakeholder" ]]; then
		MODEL=$(echo "$INPUT" | jq -r '.tool_input.model // empty')
		if [[ "$MODEL" == "haiku" ]] || [[ "$MODEL" == *"haiku"* ]]; then
			AGENT_MODE="implementation"
		elif [[ "$MODEL" == "opus" ]] || [[ "$MODEL" == *"opus"* ]]; then
			AGENT_MODE="requirements"
		else
			# No model specified, infer from current state
			if [[ "$CURRENT_STATE" == "REQUIREMENTS" ]] || [[ "$CURRENT_STATE" == "VALIDATION" ]] || [[ "$CURRENT_STATE" == "REVIEW" ]]; then
				AGENT_MODE="requirements"
			elif [[ "$CURRENT_STATE" == "IMPLEMENTATION" ]]; then
				AGENT_MODE="implementation"
			fi
		fi
		break
	fi
done


# Validate agent invocation timing based on mode
VIOLATION=""

case "$AGENT_MODE" in
	"requirements")
		# Requirements mode agents can be invoked in REQUIREMENTS, VALIDATION, REVIEW states
		if [[ "$CURRENT_STATE" != "REQUIREMENTS" ]] && [[ "$CURRENT_STATE" != "VALIDATION" ]] && [[ "$CURRENT_STATE" != "REVIEW" ]]; then
			if [[ "$CURRENT_STATE" == "SYNTHESIS" ]] || [[ "$CURRENT_STATE" == "IMPLEMENTATION" ]]; then
				# Allow re-invocation during synthesis/implementation for clarification
				LOG_FILE="/workspace/tasks/${TASK_NAME}/agent-invocations.log"
				echo "[$(date -Iseconds)] WARNING: Agent ${AGENT_NAME} in requirements mode invoked in ${CURRENT_STATE} state (unusual but permitted)" >> "$LOG_FILE"
			else
				VIOLATION="Agents in requirements mode (model=opus) should be invoked in REQUIREMENTS, VALIDATION, or REVIEW states. Current state: ${CURRENT_STATE}"
			fi
		fi
		;;

	"implementation")
		# Implementation mode agents MUST only be invoked in IMPLEMENTATION state
		if [[ "$CURRENT_STATE" != "IMPLEMENTATION" ]]; then
			VIOLATION="Agents in implementation mode (model=haiku) can ONLY be invoked in IMPLEMENTATION state. Current state: ${CURRENT_STATE}"
		fi
		;;

	*)
		# Unknown agent mode, allow but log
		LOG_FILE="/workspace/tasks/${TASK_NAME}/agent-invocations.log"
		echo "[$(date -Iseconds)] INFO: Unknown agent mode for: ${AGENT_NAME} in ${CURRENT_STATE} state" >> "$LOG_FILE"
		;;
esac

# If violation detected, block invocation
if [[ -n "$VIOLATION" ]]; then
	log_hook_blocked "validate-agent-invocation" "PreToolUse" "Agent ${AGENT_NAME} blocked in ${CURRENT_STATE} state"
	LOG_FILE="/workspace/tasks/${TASK_NAME}/agent-invocation-violations.log"
	echo "[$(date -Iseconds)] BLOCKED: ${AGENT_NAME} invocation in ${CURRENT_STATE} state (mode: ${AGENT_MODE})" >> "$LOG_FILE"

	MESSAGE="## 🚨 AGENT INVOCATION BLOCKED

**Task**: \`$TASK_NAME\`
**Agent**: \`$AGENT_NAME\` (mode: ${AGENT_MODE})
**Current State**: \`$CURRENT_STATE\`
**Violation**: ${VIOLATION}

## ⚠️ AGENT MODE RULES

**Requirements Mode** (model=opus):
- ✅ Invoked during: REQUIREMENTS, VALIDATION, REVIEW
- ⚠️ Allowed but unusual: SYNTHESIS, IMPLEMENTATION (for clarifications)
- ❌ Invalid: INIT, CLASSIFIED, AWAITING_USER_APPROVAL, COMPLETE, CLEANUP

**Implementation Mode** (model=haiku):
- ✅ Invoked during: IMPLEMENTATION ONLY
- ❌ Invalid: All other states (including SYNTHESIS before user approval)

## Required Action

**To invoke \`$AGENT_NAME\` in the correct mode**:

1. **Check current task state**:
   \`\`\`bash
   cat /workspace/tasks/${TASK_NAME}/task.json | jq '.state'
   \`\`\`

2. **Choose the correct model for your state**:
   - In REQUIREMENTS/VALIDATION/REVIEW → use \`model: opus\`
   - In IMPLEMENTATION → use \`model: haiku\`

3. **For implementation mode specifically**:
   - Verify user approved implementation plan (see enforce-synthesis-checkpoint.sh)
   - Verify state is IMPLEMENTATION
   - Then invoke agent with \`model: haiku\`

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § Agent Invocation Rules

**Violation logged to**: ${LOG_FILE}"

	# Output detailed message to stderr for user
	echo "$MESSAGE" >&2

	# Use proper permission system
	output_hook_block "Blocked: Agent invocation not allowed in $CURRENT_STATE state. Progress through protocol states first." ""
	exit 0  # Exit 0 for JSON processing
fi

log_hook_success "validate-agent-invocation" "PreToolUse" "Agent ${AGENT_NAME} allowed in ${CURRENT_STATE} state"
exit 0
