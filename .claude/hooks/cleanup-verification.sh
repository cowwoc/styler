#!/bin/bash
# Enforces complete agent branch cleanup during CLEANUP state
# This hook validates that ALL agent branches are deleted before allowing state transition
#
# TRIGGER: UserPromptSubmit (via .claude/settings.json)
# CHECKS: When transitioning from CLEANUP to any other state, verify no task-related branches remain
# ACTION: Blocks completion and displays cleanup requirements if branches remain
#
# Related Protocol Sections:
# - task-protocol-core.md § COMPLETE → CLEANUP {#complete-cleanup}
# - CLAUDE.md § Protocol Violation #2: Incomplete CLEANUP

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in cleanup-verification.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source logging helper
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"

# Read session ID from stdin JSON
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Set up logging context
export HOOK_SESSION_ID="$SESSION_ID"
export HOOK_INPUT="$INPUT"

log_hook_start "cleanup-verification" "UserPromptSubmit"

if [[ -z "$SESSION_ID" ]]; then
	exit 0
fi

# Find task owned by this session
TASKS_DIR="/workspace/tasks"
if [[ ! -d "$TASKS_DIR" ]]; then
	exit 0
fi

# Check each task directory for locks owned by this session
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

	# Only validate when transitioning OUT of CLEANUP state
	STATE_TRACKER="${task_dir}/.last-validated-state"
	if [[ -f "$STATE_TRACKER" ]]; then
		PREVIOUS_STATE=$(cat "$STATE_TRACKER")
	else
		PREVIOUS_STATE="UNKNOWN"
	fi

	# If we're in CLEANUP state or just transitioned from it, verify branch cleanup
	if [[ "$CURRENT_STATE" == "CLEANUP" ]] || [[ "$PREVIOUS_STATE" == "CLEANUP" && "$CURRENT_STATE" != "CLEANUP" ]]; then

		# Check for remaining task-related branches
		REMAINING_BRANCHES=$(git branch | grep -E "${TASK_NAME}" || true)

		if [[ -n "$REMAINING_BRANCHES" ]]; then
			# Log the violation
			log_hook_blocked "cleanup-verification" "UserPromptSubmit" "Task $TASK_NAME: Agent branches still exist after CLEANUP"
			LOG_FILE="${task_dir}/cleanup-violations.log"
			echo "[$(date -Iseconds)] CRITICAL: Agent branches remain after CLEANUP attempt" >> "$LOG_FILE"

			# Count remaining branches
			BRANCH_COUNT=$(echo "$REMAINING_BRANCHES" | wc -l)

			# Display cleanup requirement
			MESSAGE="## 🚨 CLEANUP VIOLATION DETECTED AND BLOCKED

**Task**: \`$TASK_NAME\`
**Violation**: ${BRANCH_COUNT} task-related branches still exist
**State**: CLEANUP incomplete

## ⚠️ CRITICAL - MANDATORY BRANCH CLEANUP REQUIREMENT

You attempted to complete CLEANUP state but ${BRANCH_COUNT} branches remain.
This violates the task protocol cleanup requirement.

**REMAINING BRANCHES**:
\`\`\`
${REMAINING_BRANCHES}
\`\`\`

**AUTOMATIC ACTION TAKEN**:
- CLEANUP completion blocked
- Violation logged to: ${task_dir}/cleanup-violations.log

**REQUIRED ACTION**:

Delete ALL task-related branches before completing CLEANUP:

\`\`\`bash
# Delete main task branch
git branch -D ${TASK_NAME} 2>/dev/null || true

# Delete all agent branches for this task
git branch | grep \"^  ${TASK_NAME}-\" | xargs -r git branch -D

# Verify complete cleanup
if git branch | grep -q \"${TASK_NAME}\"; then
  echo \"❌ VIOLATION: Branches still exist\"
  git branch | grep \"${TASK_NAME}\"
else
  echo \"✅ All task branches deleted\"
fi
\`\`\`

**VERIFICATION**:
Run this command to verify no task branches remain:
\`\`\`bash
git branch | grep \"${TASK_NAME}\"
\`\`\`
Should return empty (no results).

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § COMPLETE → CLEANUP {#complete-cleanup}

**MANDATORY CLEANUP CHECKLIST**:
- [ ] Delete task branch: \`git branch -D ${TASK_NAME}\`
- [ ] Delete all agent branches: \`git branch | grep \"^  ${TASK_NAME}-\" | xargs -r git branch -D\`
- [ ] Remove task worktree: \`git worktree remove /workspace/tasks/${TASK_NAME}/code\`
- [ ] Verify no branches remain: \`git branch | grep \"${TASK_NAME}\"\` (should be empty)

**PROHIBITED PATTERNS**:
❌ Leaving agent branches after CLEANUP
❌ Completing CLEANUP with task branches remaining
❌ Assuming branches will be cleaned up later
❌ Manual cleanup instead of following protocol procedure"

			jq -n \
				--arg event "UserPromptSubmit" \
				--arg context "$MESSAGE" \
				'{
					"hookSpecificOutput": {
						"hookEventName": $event,
						"additionalContext": $context
					}
				}'

			exit 0
		fi
	fi

	# Update tracker to record current state
	echo "$CURRENT_STATE" > "$STATE_TRACKER"
	log_hook_success "cleanup-verification" "UserPromptSubmit" "Task $TASK_NAME: No branch cleanup violations detected"

done

log_hook_success "cleanup-verification" "UserPromptSubmit" "No violations detected"
exit 0
