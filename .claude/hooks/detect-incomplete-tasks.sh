#!/bin/bash
# SessionStart Hook: Detect tasks that have been merged to main but not cleaned up
#
# CREATED: 2025-12-02 after implement-line-length-formatter was merged but left
# with AWAITING_USER_APPROVAL state and orphaned worktree/branch
#
# PREVENTS: Tasks remaining in incomplete state after merge to main
# DETECTS: Tasks where content is on main but cleanup never happened

set -euo pipefail

trap 'echo "ERROR in detect-incomplete-tasks.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

log_hook_start "detect-incomplete-tasks" "SessionStart"

# Check if /workspace/tasks directory exists
if [[ ! -d "/workspace/tasks" ]]; then
	log_hook_success "detect-incomplete-tasks" "SessionStart" "No tasks directory"
	exit 0
fi

# Track if any incomplete tasks found
FOUND_INCOMPLETE=false
MESSAGE=""

# Iterate through task directories
for task_dir in /workspace/tasks/*/; do
	if [[ ! -d "$task_dir" ]]; then
		continue
	fi

	TASK_NAME=$(basename "$task_dir")
	TASK_JSON="${task_dir}task.json"

	# Skip if no task.json
	if [[ ! -f "$TASK_JSON" ]]; then
		continue
	fi

	# Get task state
	STATE=$(jq -r '.state // "UNKNOWN"' "$TASK_JSON" 2>/dev/null || echo "UNKNOWN")

	# Check if task is in a non-final state (not CLEANUP or after)
	case "$STATE" in
		CLEANUP|ARCHIVED)
			# These are expected final states
			continue
			;;
		*)
			# Check if task was merged to main
			# Look for a commit on main that mentions the task name
			cd /workspace/main

			MERGED_COMMIT=$(git log --oneline --grep="$TASK_NAME" -1 2>/dev/null || echo "")

			if [[ -n "$MERGED_COMMIT" ]]; then
				# Task appears to be merged but not cleaned up
				FOUND_INCOMPLETE=true
				WORKTREE_EXISTS="No"
				BRANCH_EXISTS="No"

				# Check for orphaned worktree
				if [[ -d "/workspace/tasks/${TASK_NAME}/code" ]]; then
					if git worktree list 2>/dev/null | grep -q "${TASK_NAME}/code"; then
						WORKTREE_EXISTS="Yes"
					fi
				fi

				# Check for orphaned branch
				if git branch 2>/dev/null | grep -q "^\s*${TASK_NAME}$"; then
					BRANCH_EXISTS="Yes"
				fi

				MESSAGE+="
## ⚠️ INCOMPLETE TASK DETECTED: \`${TASK_NAME}\`

**Current State**: \`${STATE}\` (expected: CLEANUP)
**Merged Commit**: \`${MERGED_COMMIT}\`
**Orphaned Worktree**: ${WORKTREE_EXISTS}
**Orphaned Branch**: ${BRANCH_EXISTS}

**Action Required**: Complete CLEANUP for this task before starting new work.

\`\`\`bash
# Execute cleanup
cd /workspace/main

# Update task state
jq '.state = \"CLEANUP\"' /workspace/tasks/${TASK_NAME}/task.json > /tmp/task.json.tmp
mv /tmp/task.json.tmp /workspace/tasks/${TASK_NAME}/task.json

# Remove worktree if exists
git worktree remove /workspace/tasks/${TASK_NAME}/code --force 2>/dev/null || true

# Delete branch if exists
git branch -D ${TASK_NAME} 2>/dev/null || true

# Garbage collect
git gc --prune=now

# Verify
git worktree list
git branch | grep ${TASK_NAME} || echo \"Branch cleaned\"
\`\`\`
"
			fi
			;;
	esac
done

if [[ "$FOUND_INCOMPLETE" == "true" ]]; then
	HEADER="# 🚨 INCOMPLETE TASK CLEANUP REQUIRED

Previous session(s) merged task(s) to main but did not complete cleanup.
**You MUST complete cleanup before starting new work.**
$MESSAGE"

	output_hook_warning "SessionStart" "$HEADER"
	log_hook_blocked "detect-incomplete-tasks" "SessionStart" "Found incomplete tasks needing cleanup"
else
	log_hook_success "detect-incomplete-tasks" "SessionStart" "No incomplete tasks"
fi

exit 0
