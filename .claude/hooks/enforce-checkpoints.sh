#!/bin/bash
# Consolidated checkpoint enforcement for task protocol
# Enforces mandatory user approval at critical state transitions
#
# CHECKPOINTS ENFORCED:
# 1. SYNTHESIS → IMPLEMENTATION (after plan presentation)
# 2. AWAITING_USER_APPROVAL → COMPLETE (after REVIEW with unanimous approval)
# 3. POST_IMPLEMENTATION merge (after agent implementations complete)
#
# TRIGGERS: UserPromptSubmit, PreToolUse (via .claude/settings.json)
# CHECKS: State transitions and git merge operations requiring user approval
# ACTION: Blocks operations and displays checkpoint requirements if approval missing
#
# Related Protocol Sections:
# - task-protocol-core.md § SYNTHESIS → IMPLEMENTATION
# - task-protocol-core.md § POST_IMPLEMENTATION
# - CLAUDE.md § CRITICAL PROTOCOL VIOLATIONS

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in enforce-checkpoints.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source logging and helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/session-helper.sh
source /workspace/.claude/scripts/json-output.sh
source /workspace/.claude/scripts/task-finder.sh

# Read input from stdin
INPUT=$(cat)

# Determine trigger type and extract relevant data
TRIGGER_TYPE=""
SESSION_ID=""
TOOL_NAME=""
TOOL_PARAMS=""

# Try to parse as UserPromptSubmit hook (has session_id)
SESSION_ID=$(get_session_id "$INPUT")
if [[ -n "$SESSION_ID" ]]; then
	TRIGGER_TYPE="UserPromptSubmit"
else
	# Try to parse as PreToolUse hook (has tool_name and tool_input)
	TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")
	if [[ -n "$TOOL_NAME" ]]; then
		TRIGGER_TYPE="PreToolUse"
		TOOL_PARAMS=$(echo "$INPUT" | jq -r '.tool_input // {}' 2>/dev/null || echo "{}")
	fi
fi

# If we can't determine trigger type, exit
if [[ -z "$TRIGGER_TYPE" ]]; then
	exit 0
fi

log_hook_start "enforce-checkpoints" "$TRIGGER_TYPE"

# =============================================================================
# CHECKPOINTS 1 & 3: User Approval Validation (UserPromptSubmit trigger)
# Consolidated: SYNTHESIS → IMPLEMENTATION and AWAITING_USER_APPROVAL → COMPLETE
# =============================================================================
if [[ "$TRIGGER_TYPE" == "UserPromptSubmit" ]]; then
	if [[ -z "$SESSION_ID" ]]; then
		exit 0
	fi

	# Find task owned by this session
	TASKS_DIR="/workspace/tasks"
	if [[ ! -d "$TASKS_DIR" ]]; then
		exit 0
	fi

	# Check each task directory for locks owned by this session (consolidated loop)
	for task_dir in "$TASKS_DIR"/*; do
		if [[ ! -d "$task_dir" ]]; then
			continue
		fi

		LOCK_FILE="${task_dir}/task.json"
		if [[ ! -f "$LOCK_FILE" ]]; then
			continue
		fi

		# Check if this task is owned by current session
		LOCK_SESSION=$(jq -r '.session_id // ""' "$LOCK_FILE" 2>/dev/null || echo "")
		if [[ "$LOCK_SESSION" != "$SESSION_ID" ]]; then
			continue
		fi

		# This task is owned by us - check state
		TASK_NAME=$(basename "$task_dir")
		CURRENT_STATE=$(jq -r '.state // "UNKNOWN"' "$LOCK_FILE")

		# Check previous state from tracker
		STATE_TRACKER="${task_dir}/.last-validated-state"
		if [[ -f "$STATE_TRACKER" ]]; then
			PREVIOUS_STATE=$(cat "$STATE_TRACKER")
		else
			PREVIOUS_STATE="UNKNOWN"
		fi

		# Handle checkpoint validation based on current state
		case "$CURRENT_STATE" in
			IMPLEMENTATION)
				# Checkpoint 1: SYNTHESIS → IMPLEMENTATION
				APPROVAL_FLAG="${task_dir}/user-approved-synthesis.flag"

				# If we just transitioned to IMPLEMENTATION without approval flag file, BLOCK
				if [[ "$PREVIOUS_STATE" == "IMPLEMENTATION" ]] || [[ -f "$APPROVAL_FLAG" ]]; then
					# Already validated or has approval
					echo "IMPLEMENTATION" > "$STATE_TRACKER"
					log_hook_success "enforce-checkpoints" "UserPromptSubmit" "Task $TASK_NAME: IMPLEMENTATION state validated with user approval"
					continue
				fi

				# Log the violation
				log_hook_blocked "enforce-checkpoints" "UserPromptSubmit" "Task $TASK_NAME: SYNTHESIS → IMPLEMENTATION without user approval"
				LOG_FILE="${task_dir}/checkpoint-violations.log"
				echo "[$(date -Iseconds)] CRITICAL: SYNTHESIS → IMPLEMENTATION transition without user approval" >> "$LOG_FILE"

				# Revert state to SYNTHESIS
				jq '.state = "SYNTHESIS"' "$LOCK_FILE" > /tmp/lock-revert.tmp
				mv /tmp/lock-revert.tmp "$LOCK_FILE"

				# Display checkpoint requirement
				MESSAGE="## 🚨 CHECKPOINT VIOLATION DETECTED AND BLOCKED

**Task**: \`$TASK_NAME\`
**Attempted Transition**: SYNTHESIS → IMPLEMENTATION
**Violation**: User approval not obtained

## ⚠️ CRITICAL - MANDATORY USER APPROVAL CHECKPOINT

You attempted to transition from SYNTHESIS to IMPLEMENTATION state without user approval.
This violates the task protocol checkpoint requirement.

**AUTOMATIC ACTION TAKEN**:
- State reverted to SYNTHESIS
- Violation logged to: ${task_dir}/checkpoint-violations.log

**REQUIRED ACTION**:

1. **Present the implementation plan** to the user:
   \`\`\`
   Implementation plan complete. Location: /workspace/tasks/${TASK_NAME}/task.md

   Please review the implementation approach and confirm approval.
   Type 'approved', 'LGTM', 'looks good', or 'proceed' to continue to IMPLEMENTATION state.
   \`\`\`

2. **WAIT for explicit user approval** (do not proceed automatically)

3. **After receiving approval**, create approval flag file:
   \`\`\`bash
   touch /workspace/tasks/${TASK_NAME}/user-approved-synthesis.flag
   \`\`\`

4. **Then transition to IMPLEMENTATION**:
   \`\`\`bash
   jq '.state = \"IMPLEMENTATION\"' /workspace/tasks/${TASK_NAME}/task.json > /tmp/lock.tmp
   mv /tmp/lock.tmp /workspace/tasks/${TASK_NAME}/task.json
   \`\`\`

**Note**: Flag file approach used instead of lock file field to work correctly in bypass permissions mode.

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § SYNTHESIS → IMPLEMENTATION

**PROHIBITED PATTERNS**:
❌ Automatic transition from SYNTHESIS → IMPLEMENTATION
❌ Assuming \"continue\" or \"proceed\" from earlier messages means plan approval
❌ Treating bypass mode as approval checkpoint override
❌ Proceeding because requirements are clear or plan is straightforward"

				output_hook_error "UserPromptSubmit" "$MESSAGE"
				exit 0
				;;

			COMPLETE)
				# Checkpoint 3: AWAITING_USER_APPROVAL → COMPLETE
				APPROVAL_FLAG="${task_dir}/user-approved-changes.flag"

				# If we just transitioned to COMPLETE without approval flag file, BLOCK
				if [[ "$PREVIOUS_STATE" == "COMPLETE" ]] || [[ -f "$APPROVAL_FLAG" ]]; then
					# Already validated or has approval
					echo "COMPLETE" > "$STATE_TRACKER"
					log_hook_success "enforce-checkpoints" "UserPromptSubmit" "Task $TASK_NAME: COMPLETE state validated with user approval"
					continue
				fi

				# Log the violation
				log_hook_blocked "enforce-checkpoints" "UserPromptSubmit" "Task $TASK_NAME: AWAITING_USER_APPROVAL → COMPLETE without user approval"
				LOG_FILE="${task_dir}/checkpoint-violations.log"
				echo "[$(date -Iseconds)] CRITICAL: AWAITING_USER_APPROVAL → COMPLETE transition without user approval" >> "$LOG_FILE"

				# Revert state to AWAITING_USER_APPROVAL
				jq '.state = "AWAITING_USER_APPROVAL"' "$LOCK_FILE" > /tmp/lock-revert.tmp
				mv /tmp/lock-revert.tmp "$LOCK_FILE"

				# Display checkpoint requirement
				MESSAGE="## 🚨 CHECKPOINT VIOLATION DETECTED AND BLOCKED

**Task**: \`$TASK_NAME\`
**Attempted Transition**: AWAITING_USER_APPROVAL → COMPLETE
**Violation**: User approval not obtained

## ⚠️ CRITICAL - MANDATORY CHANGE REVIEW CHECKPOINT

You attempted to transition from AWAITING_USER_APPROVAL to COMPLETE state without user approval.
This violates the task protocol checkpoint requirement.

**AUTOMATIC ACTION TAKEN**:
- State reverted to AWAITING_USER_APPROVAL
- Violation logged to: ${task_dir}/checkpoint-violations.log

**REQUIRED ACTION**:

1. **Show the changes to the user**:
   \`\`\`bash
   git log -1 --stat
   git diff --stat main...HEAD
   \`\`\`

2. **WAIT for explicit user approval** (do not merge automatically)

3. **After receiving approval**, create approval flag file:
   \`\`\`bash
   touch /workspace/tasks/${TASK_NAME}/user-approved-changes.flag
   \`\`\`

4. **Then transition to COMPLETE**:
   \`\`\`bash
   jq '.state = \"COMPLETE\"' /workspace/tasks/${TASK_NAME}/task.json > /tmp/lock.tmp
   mv /tmp/lock.tmp /workspace/tasks/${TASK_NAME}/task.json
   \`\`\`

**Note**: Unanimous agent approval does NOT substitute for user approval.

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § CHANGE REVIEW checkpoint

**PROHIBITED PATTERNS**:
❌ Automatic transition from AWAITING_USER_APPROVAL → COMPLETE
❌ Assuming unanimous agent approval means user approved
❌ Merging to main without showing user what changed
❌ Proceeding because \"changes look correct\" without user confirmation"

				output_hook_error "UserPromptSubmit" "$MESSAGE"
				exit 0
				;;

			*)
				# Other states don't require checkpoint validation
				continue
				;;
		esac

	done
fi

# =============================================================================
# CHECKPOINT 2: POST_IMPLEMENTATION merge approval (PreToolUse trigger)
# =============================================================================
if [[ "$TRIGGER_TYPE" == "PreToolUse" ]]; then
	# Only enforce on Bash tool with git merge commands
	if [[ "$TOOL_NAME" != "Bash" ]]; then
		exit 0
	fi

	COMMAND=$(echo "$TOOL_PARAMS" | jq -r '.command // empty')
	if [[ -z "$COMMAND" ]]; then
		exit 0
	fi

	# Check for git merge commands (with flexible whitespace)
	if [[ ! "$COMMAND" =~ git[[:space:]]*merge ]]; then
		exit 0
	fi

	# Check if we're in a task context
	TASK_NAME=""
	if [[ "$PWD" == /workspace/tasks/*/code ]] || [[ "$PWD" == /workspace/tasks/*/agents/*/code ]]; then
		# Extract task name from current directory
		TASK_NAME=$(echo "$PWD" | sed -E 's|/workspace/tasks/([^/]+)/.*|\1|')
	elif [[ "$COMMAND" =~ /workspace/tasks/[^/]+/code ]]; then
		# Extract task name from command
		TASK_NAME=$(echo "$COMMAND" | grep -oP '/workspace/tasks/\K[^/]+(?=/code)')
	fi

	if [[ -z "$TASK_NAME" ]]; then
		exit 0
	fi

	TASK_JSON="/workspace/tasks/$TASK_NAME/task.json"
	if [[ ! -f "$TASK_JSON" ]]; then
		exit 0
	fi

	STATE=$(jq -r '.state // empty' "$TASK_JSON")

	# Require approval for merges in IMPLEMENTATION state
	if [[ "$STATE" == "IMPLEMENTATION" ]]; then
		APPROVAL_FLAG="/workspace/tasks/$TASK_NAME/user-approved-merge.flag"

		if [[ ! -f "$APPROVAL_FLAG" ]]; then
			log_hook_blocked "enforce-checkpoints" "PreToolUse" "Task $TASK_NAME: git merge without user approval"

			echo "⚠️  CHECKPOINT REQUIRED: Agent implementation merge" >&2
			echo "" >&2
			echo "Before merging agent implementations, you MUST:" >&2
			echo "1. Review agent outputs for quality and completeness" >&2
			echo "2. Obtain user approval for merge" >&2
			echo "3. Create approval flag file" >&2
			echo "" >&2
			echo "To approve merge after user approval:" >&2
			echo "  touch /workspace/tasks/$TASK_NAME/user-approved-merge.flag" >&2
			echo "" >&2
			echo "See: /workspace/main/CLAUDE.md § POST_IMPLEMENTATION Checkpoint" >&2
			exit 2
		fi
	fi

	log_hook_success "enforce-checkpoints" "PreToolUse" "Task $TASK_NAME: git merge approved"
	exit 0
fi

# Unknown trigger type - shouldn't happen but exit cleanly
exit 0
