#!/bin/bash
# PostToolUse Hook: Verify cleanup actually completed after squash merge
#
# CREATED: 2025-12-02 after implement-line-length-formatter merged to main
# but cleanup failed silently because:
# 1. Branch deletion attempted before worktree removal (git blocks this)
# 2. Error masked by `|| echo "already deleted"` pattern
# 3. No verification that cleanup succeeded
#
# PREVENTS: Incomplete cleanup after merge (orphaned worktrees/branches)
# ENFORCES: Verification that cleanup commands actually worked
#
# Triggers: PostToolUse (Bash) - checks if pending cleanup exists and verifies completion

set -euo pipefail

trap 'echo "ERROR in verify-cleanup-complete.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Only check Bash tool
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")
if [[ "$TOOL_NAME" != "Bash" ]]; then
	exit 0
fi

# Check for pending cleanup marker file
PENDING_CLEANUP_DIR="/tmp/pending-task-cleanup"
if [[ ! -d "$PENDING_CLEANUP_DIR" ]]; then
	exit 0
fi

# Process each pending cleanup
shopt -s nullglob
for marker_file in "$PENDING_CLEANUP_DIR"/*.marker; do
	if [[ ! -f "$marker_file" ]]; then
		continue
	fi

	TASK_NAME=$(basename "$marker_file" .marker)
	MERGE_TIME=$(cat "$marker_file" 2>/dev/null || echo "unknown")

	log_hook_start "verify-cleanup-complete" "PostToolUse"

	# Check if cleanup is actually complete
	WORKTREE_EXISTS=false
	BRANCH_EXISTS=false
	TASK_DIR_EXISTS=false

	# Check worktree
	if git -C /workspace/main worktree list 2>/dev/null | grep -q "${TASK_NAME}/code"; then
		WORKTREE_EXISTS=true
	fi

	# Check branch
	if git -C /workspace/main branch 2>/dev/null | grep -q "^\s*${TASK_NAME}$"; then
		BRANCH_EXISTS=true
	fi

	# Check task directory with code subdirectory
	if [[ -d "/workspace/tasks/${TASK_NAME}/code" ]]; then
		TASK_DIR_EXISTS=true
	fi

	# If all cleaned up, remove the marker
	if [[ "$WORKTREE_EXISTS" == "false" && "$BRANCH_EXISTS" == "false" ]]; then
		rm -f "$marker_file"
		log_hook_success "verify-cleanup-complete" "PostToolUse" "Cleanup verified for $TASK_NAME"
		continue
	fi

	# Cleanup incomplete - warn agent
	ISSUES=""
	if [[ "$WORKTREE_EXISTS" == "true" ]]; then
		ISSUES+="- ❌ Worktree still exists: /workspace/tasks/${TASK_NAME}/code\n"
	fi
	if [[ "$BRANCH_EXISTS" == "true" ]]; then
		ISSUES+="- ❌ Branch still exists: ${TASK_NAME}\n"
	fi

	MESSAGE="## 🚨 CLEANUP INCOMPLETE: \`${TASK_NAME}\`

**Merge completed at**: ${MERGE_TIME}
**Cleanup status**: FAILED

### Issues Found:
$(echo -e "$ISSUES")

### Why This Happened:
Git will NOT delete a branch that has an active worktree. You MUST remove worktrees FIRST.

### Required Fix (execute in order):

\`\`\`bash
# Step 1: MUST be in main worktree
cd /workspace/main

# Step 2: Remove worktree FIRST (required before branch deletion)
git worktree remove /workspace/tasks/${TASK_NAME}/code --force

# Step 3: NOW delete the branch (will work after worktree removed)
git branch -D ${TASK_NAME}

# Step 4: Verify cleanup succeeded
git worktree list | grep -q '${TASK_NAME}' && echo '❌ WORKTREE STILL EXISTS' || echo '✅ Worktree removed'
git branch | grep -q '${TASK_NAME}' && echo '❌ BRANCH STILL EXISTS' || echo '✅ Branch deleted'

# Step 5: Garbage collect orphaned commits
git gc --prune=now
\`\`\`

### ⚠️ DO NOT use \`|| echo \"already deleted\"\` patterns - they hide real failures!

**This message will repeat until cleanup is verified complete.**"

	output_hook_warning "PostToolUse" "$MESSAGE"
	log_hook_blocked "verify-cleanup-complete" "PostToolUse" "Cleanup incomplete for $TASK_NAME"
done

exit 0
