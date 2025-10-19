#!/bin/bash
set -euo pipefail

# Task Protocol enforcement reminder for session start
# Ensures Claude follows the mandatory TASK PROTOCOL for ALL tasks

# Parse hook input
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

TASK_PROTOCOL_CORE="$(dirname "$0")/../../docs/project/task-protocol-core.md"
TASK_PROTOCOL_OPS="$(dirname "$0")/../../docs/project/task-protocol-operations.md"
TASK_PROTOCOL_DELEGATED="$(dirname "$0")/../../docs/project/delegated-implementation-protocol.md"

if [[ ! -f "$TASK_PROTOCOL_CORE" ]] || [[ ! -f "$TASK_PROTOCOL_OPS" ]]; then
	echo '{
	    "hookSpecificOutput": {
	        "hookEventName": "SessionStart",
	        "additionalContext": "🚨 CRITICAL TASK PROTOCOL ENFORCEMENT: task-protocol files not found! Cannot enforce mandatory TASK PROTOCOL."
	    }
	}'
	exit 0
fi

# Build base protocol reminder message
BASE_MESSAGE="🚨 MANDATORY TASK PROTOCOL ENFORCEMENT: Before executing ANY task (whether from todo list OR any user request), you MUST follow the TASK PROTOCOL procedures for task isolation, stakeholder consultation, atomic locking, and worktree management. The protocol uses just-in-time guidance - phase-specific instructions will be provided automatically via hooks as you transition between states. You do NOT need to read protocol files upfront. The state-transition-detector hook will provide targeted reading instructions for each phase as needed. Start by understanding the task requirements, then follow the phase-specific guidance provided by the hooks."

# Check if this session has an active task with delegated protocol
if [ -n "$SESSION_ID" ]; then
	LOCK_FILE=$(find /workspace/locks -name "*.json" -type f -exec grep -lE "\"session_id\":\s*\"$SESSION_ID\"" {} \; 2>/dev/null | head -1)

	if [ -n "$LOCK_FILE" ]; then
		TASK_NAME=$(basename "$LOCK_FILE" .json)
		TASK_STATE=$(grep -oP '"state":\s*"\K[^"]+' "$LOCK_FILE" 2>/dev/null)
		CONTEXT_FILE="/workspace/branches/$TASK_NAME/context.md"

		# Check for delegated implementation protocol
		if [ -f "$CONTEXT_FILE" ] && grep -q "## Agent Work Assignments" "$CONTEXT_FILE" 2>/dev/null; then
			# Add delegated protocol specific guidance
			case "$TASK_STATE" in
				CONTEXT|AUTONOMOUS_IMPLEMENTATION|CONVERGENCE)
					DELEGATED_MESSAGE="

## 📋 DELEGATED IMPLEMENTATION PROTOCOL DETECTED

**Task**: \`$TASK_NAME\`
**Current State**: \`$TASK_STATE\`
**Protocol Type**: Agent-based parallel implementation

⚠️  **CRITICAL**: This task uses DELEGATED IMPLEMENTATION PROTOCOL.

**You MUST read**: \`docs/project/delegated-implementation-protocol.md\`

**Key Requirements for State \`$TASK_STATE\`**:"

					case "$TASK_STATE" in
						CONTEXT)
							DELEGATED_MESSAGE="$DELEGATED_MESSAGE
- Verify context.md has complete agent work assignments
- Update lock state to AUTONOMOUS_IMPLEMENTATION
- Invoke implementation agents using Task tool
- **DO NOT** implement directly with Write/Edit"
							;;
						AUTONOMOUS_IMPLEMENTATION)
							DELEGATED_MESSAGE="$DELEGATED_MESSAGE
- Invoke implementation agents in parallel
- Wait for agent completion
- **PROHIBITED**: Direct Write/Edit for implementation files
- \`enforce-delegated-implementation.sh\` will BLOCK violations"
							;;
						CONVERGENCE)
							DELEGATED_MESSAGE="$DELEGATED_MESSAGE
- Integrate agent outputs from ../ directory
- Resolve conflicts between agents
- **ALLOWED**: Write/Edit for integration/conflicts only
- **PROHIBITED**: New implementation files"
							;;
					esac

					echo "$DELEGATED_MESSAGE

See: docs/project/delegated-implementation-protocol.md for complete state-specific guidance.

---

$BASE_MESSAGE" | jq -Rs '{
	    "hookSpecificOutput": {
	        "hookEventName": "SessionStart",
	        "additionalContext": .
	    }
	}'
					exit 0
					;;
			esac
		fi
	fi
fi

# No delegated protocol active, show base message
echo "$BASE_MESSAGE" | jq -Rs '{
	"hookSpecificOutput": {
	    "hookEventName": "SessionStart",
	    "additionalContext": .
	}
}'
