#!/bin/bash
# Validates git working directory is clean before state transitions
# This hook prevents state transitions when uncommitted changes exist
#
# TRIGGER: UserPromptSubmit (via .claude/settings.json)
# CHECKS: Git status in task worktree before allowing state updates
# ACTION: Blocks state transition and displays commit requirement if dirty
#
# Related Protocol Sections:
# - git-workflow.md § Git Working Directory Requirements
# - main-agent-coordination.md § VALIDATION State Exit Requirements

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in validate-git-status-on-transition.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source git status checking library
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/lib/git-status-check.sh"
source "${SCRIPT_DIR}/hook-logger.sh"

# Read session ID from stdin JSON
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Set up logging context
export HOOK_SESSION_ID="$SESSION_ID"
export HOOK_INPUT="$INPUT"

log_hook_start "validate-git-status-on-transition" "UserPromptSubmit"

if [[ -z "$SESSION_ID" ]]; then
	log_hook_success "validate-git-status-on-transition" "UserPromptSubmit" "No session ID - skipping"
	exit 0
fi

# Find task owned by this session
TASKS_DIR="/workspace/tasks"
if [[ ! -d "$TASKS_DIR" ]]; then
	log_hook_success "validate-git-status-on-transition" "UserPromptSubmit" "No tasks directory"
	exit 0
fi

# Track if we found a violation
VIOLATION_FOUND=0

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

	# This task is owned by us - check git status before any state changes
	TASK_NAME=$(basename "$task_dir")
	CURRENT_STATE=$(jq -r '.state // "UNKNOWN"' "$LOCK_FILE")
	WORKING_DIR="${task_dir}/code"

	# Skip check if no working directory exists yet (INIT state)
	if [[ ! -d "$WORKING_DIR" ]]; then
		continue
	fi

	# Skip check for INIT and CLASSIFIED states (no code changes expected yet)
	if [[ "$CURRENT_STATE" == "INIT" ]] || [[ "$CURRENT_STATE" == "CLASSIFIED" ]]; then
		continue
	fi

	# Check if git status is clean
	if ! check_git_status_clean "$WORKING_DIR" 2>&1 | grep -q "nothing to commit"; then
		# Git status is NOT clean - check if we're about to transition
		# by looking at previous state
		STATE_TRACKER="${task_dir}/.last-validated-state"
		if [[ -f "$STATE_TRACKER" ]]; then
			PREVIOUS_STATE=$(cat "$STATE_TRACKER")
		else
			PREVIOUS_STATE="UNKNOWN"
		fi

		# If state changed, this is a transition with uncommitted changes
		if [[ "$PREVIOUS_STATE" != "$CURRENT_STATE" ]]; then
			VIOLATION_FOUND=1
			log_hook_blocked "validate-git-status-on-transition" "UserPromptSubmit" "Task $TASK_NAME: State transition $PREVIOUS_STATE → $CURRENT_STATE with uncommitted changes"

			# Revert state back to previous
			if [[ "$PREVIOUS_STATE" != "UNKNOWN" ]]; then
				jq --arg state "$PREVIOUS_STATE" '.state = $state' "$LOCK_FILE" > /tmp/lock-revert.tmp
				mv /tmp/lock-revert.tmp "$LOCK_FILE"
			fi

			# Get changes summary
			CHANGES_SUMMARY=$(get_uncommitted_changes_summary "$WORKING_DIR")

			# Display violation message
			MESSAGE="## 🚨 STATE TRANSITION BLOCKED - UNCOMMITTED CHANGES

**Task**: \`$TASK_NAME\`
**Attempted Transition**: $PREVIOUS_STATE → $CURRENT_STATE
**Violation**: Working directory has uncommitted changes

## ⚠️ MANDATORY REQUIREMENT

Working directory MUST be clean (all changes committed) before any state transition.

**AUTOMATIC ACTION TAKEN**:
- State reverted to: $PREVIOUS_STATE
- Transition blocked

## 📋 Uncommitted Changes

$CHANGES_SUMMARY

## ✅ REQUIRED ACTION

1. **Commit all changes**:
   \`\`\`bash
   cd $WORKING_DIR
   git add <files>
   git commit -m 'Description of changes'
   \`\`\`

2. **Verify clean status**:
   \`\`\`bash
   git status  # Should show 'nothing to commit, working tree clean'
   \`\`\`

3. **Retry state transition** after working directory is clean

## 📖 Protocol Reference

See: /workspace/main/docs/project/git-workflow.md § Git Working Directory Requirements
See: /workspace/main/docs/project/main-agent-coordination.md § VALIDATION State Exit Requirements

**WHY THIS MATTERS**:
- Ensures all work is preserved in git history
- Prevents loss of changes during state transitions
- Maintains audit trail integrity
- Enables clean rollback if needed"

			jq -n \
				--arg event "UserPromptSubmit" \
				--arg context "$MESSAGE" \
				'{
					"hookSpecificOutput": {
						"hookEventName": $event,
						"additionalContext": $context
					}
				}'

			# Don't exit yet - check other tasks too
		fi
	fi

	# Update state tracker for next check
	echo "$CURRENT_STATE" > "$STATE_TRACKER"

done

if [[ $VIOLATION_FOUND -eq 1 ]]; then
	exit 0  # Already sent violation message
fi

log_hook_success "validate-git-status-on-transition" "UserPromptSubmit" "No violations detected"
exit 0
