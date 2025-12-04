#!/bin/bash
# Post-Tool-Use Hook: Enforce cleanup after --ff-only merge to main
#
# CREATED: 2025-11-03 after implement-formatter-api left orphaned branches
# UPDATED: 2025-12-04 to work with --ff-only workflow (no GC needed)
#
# PREVENTS: Orphaned branches and worktrees after task completion
# ENFORCES: Branch and worktree cleanup after merge
#
# Triggers: PostToolUse (tool:Bash && command:*git*merge* matcher)

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in enforce-post-merge-cleanup.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")
TOOL_PARAMS=$(echo "$INPUT" | jq -r '.tool_input // {}' 2>/dev/null || echo "{}")
RESULT=$(echo "$INPUT" | jq -r '.result // empty' 2>/dev/null || echo "")

# Only validate Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
	exit 0
fi

# Extract command
COMMAND=$(echo "$TOOL_PARAMS" | jq -r '.command // empty' 2>/dev/null || echo "")

# Only validate git merge --ff-only commands
if [[ ! "$COMMAND" =~ git[[:space:]]*merge.*--ff-only ]]; then
	exit 0
fi

# Check if merge was successful (result doesn't contain error indicators)
if [[ "$RESULT" =~ (fatal:|error:|CONFLICT|Not possible to fast-forward) ]]; then
	# Merge failed or was aborted, don't cleanup
	exit 0
fi

log_hook_start "enforce-post-merge-cleanup" "PostToolUse"

# Extract branch that was merged (handles --ff-only flag before branch name)
MERGE_BRANCH=$(echo "$COMMAND" | sed -E 's/.*git[[:space:]]+merge[[:space:]]+(--[^[:space:]]+[[:space:]]+)*([^[:space:]]+).*/\2/')

# Create pending cleanup marker for verification hook
PENDING_CLEANUP_DIR="/tmp/pending-task-cleanup"
mkdir -p "$PENDING_CLEANUP_DIR"
echo "$(date -Iseconds)" > "${PENDING_CLEANUP_DIR}/${MERGE_BRANCH}.marker"

# Only cleanup task branches (not arbitrary branches)
if [[ ! "$MERGE_BRANCH" =~ ^implement- ]] && [[ ! "$MERGE_BRANCH" =~ ^refactor- ]] && [[ ! "$MERGE_BRANCH" =~ ^fix- ]]; then
	log_hook_success "enforce-post-merge-cleanup" "PostToolUse" "Not a task branch, skipping cleanup"
	exit 0
fi

# Verify we're in main worktree
CURRENT_DIR="$PWD"
if [[ "$CURRENT_DIR" != "/workspace/main" ]]; then
	log_hook_blocked "enforce-post-merge-cleanup" "PostToolUse" "Squash merge not from main worktree"
	MESSAGE="## ⚠️ SQUASH MERGE LOCATION WARNING

**Merged Branch**: \`$MERGE_BRANCH\`
**Current Directory**: \`$CURRENT_DIR\`

Squash merge should be executed from /workspace/main. Skipping automatic cleanup."

	output_hook_warning "PostToolUse" "$MESSAGE"
	exit 0
fi

# Extract task name from branch
TASK_NAME="$MERGE_BRANCH"

# Check if task directory exists (indicates cleanup hasn't happened yet)
if [[ ! -d "/workspace/tasks/${TASK_NAME}" ]]; then
	log_hook_success "enforce-post-merge-cleanup" "PostToolUse" "Task directory already removed"
	exit 0
fi

# Build reminder message for agent
MESSAGE="## 🧹 POST-MERGE CLEANUP REMINDER

**Task Branch**: \`$MERGE_BRANCH\`
**Merge**: ✅ Successful (--ff-only)

## ⚠️ MANDATORY CLEANUP REQUIRED

After --ff-only merge to main, you MUST complete CLEANUP state:

**AUTOMATIC TRANSITION TO CLEANUP STATE**:

\`\`\`bash
# Step 1: Transition to CLEANUP state
jq '.state = \"CLEANUP\"' /workspace/tasks/${TASK_NAME}/task.json > /tmp/task.json.tmp
mv /tmp/task.json.tmp /workspace/tasks/${TASK_NAME}/task.json

# Step 2: MANDATORY - Exit to main worktree BEFORE removing task worktree
cd /workspace/main
pwd | grep -q '/workspace/main\$' || { echo \"❌ Not in main worktree\"; exit 1; }

# Step 3: Remove task worktree
git worktree remove /workspace/tasks/${TASK_NAME}/code 2>/dev/null || true

# Step 4: Remove ALL agent worktrees
for agent_dir in /workspace/tasks/${TASK_NAME}/agents/*/code; do
  if [ -d \"\$agent_dir\" ]; then
    git worktree remove \"\$agent_dir\" 2>/dev/null || true
  fi
done

# Step 5: Delete ALL task-related branches (safe with --ff-only, commit is on main)
git branch -D ${TASK_NAME} 2>/dev/null || true
git branch | grep \"^  ${TASK_NAME}-\" | xargs -r git branch -D

# Step 6: Verify complete branch cleanup
if git branch | grep -q \"${TASK_NAME}\"; then
  echo \"❌ VIOLATION: Task branches still exist after CLEANUP\"
  git branch | grep \"${TASK_NAME}\"
  exit 1
fi

# Step 7: Remove task directory
rm -rf /workspace/tasks/${TASK_NAME}

# Step 8: VERIFICATION - Confirm complete cleanup
echo \"✅ CLEANUP complete:\"
echo \"   - Task worktree removed\"
echo \"   - Agent worktrees removed\"
echo \"   - All task branches deleted\"
echo \"   - Task directory removed\"
\`\`\`

## ✅ No Garbage Collection Needed

With --ff-only merge, no orphaned commits are created:
- The task branch commit becomes the main branch commit (same SHA)
- No new commit is created, just a pointer move
- All commits remain reachable through main

## Protocol Reference

See:
- task-protocol-operations.md § CLEANUP (Final State)
- git-workflow.md § Cleanup After --ff-only Merge

**CRITICAL**: CLEANUP is AUTOMATIC (execute immediately, no user prompt)"

output_hook_notification "PostToolUse" "$MESSAGE"

log_hook_success "enforce-post-merge-cleanup" "PostToolUse" "Cleanup reminder issued"
exit 0
