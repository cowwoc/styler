#!/bin/bash
# Post-Tool-Use Hook: Enforce cleanup after squash merge to main
#
# CREATED: 2025-11-03 after implement-formatter-api left orphaned commits
# (5f95180, b393ce3) and unreferenced branches after squash merge to main
#
# PREVENTS: Orphaned commits from squash merges polluting repository
# ENFORCES: Automatic garbage collection and branch cleanup after squash merge
#
# Triggers: PostToolUse (tool:Bash && command:*git*merge*--squash* matcher)

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

# Only validate git merge --squash commands
if [[ ! "$COMMAND" =~ git[[:space:]]*merge.*--squash ]]; then
	exit 0
fi

# Check if merge was successful (result contains success indicators)
if [[ ! "$RESULT" =~ (Squash commit|Automatic merge went well) ]]; then
	# Merge failed or was aborted, don't cleanup
	exit 0
fi

log_hook_start "enforce-post-merge-cleanup" "PostToolUse"

# Extract branch that was merged (after 'git merge')
MERGE_BRANCH=$(echo "$COMMAND" | sed -E 's/.*git[[:space:]]+merge[[:space:]]+--squash[[:space:]]+([^[:space:]]+).*/\1/')

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
**Squash Merge**: ✅ Successful

## ⚠️ MANDATORY CLEANUP REQUIRED

After squash merge to main, you MUST complete CLEANUP state:

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

# Step 5: Delete ALL task-related branches (use -D, squash makes them unreachable)
git branch -D ${TASK_NAME} 2>/dev/null || true
git branch | grep \"^  ${TASK_NAME}-\" | xargs -r git branch -D

# Step 6: MANDATORY - Garbage collect orphaned commits from squash merge
# Why: git merge --squash creates new commit on main, leaving original commits unreferenced
# Without gc, these orphaned commits persist indefinitely
echo \"Running garbage collection to remove orphaned commits...\"
git gc --prune=now
echo \"✅ Orphaned commits removed\"

# Step 7: Verify complete branch cleanup
if git branch | grep -q \"${TASK_NAME}\"; then
  echo \"❌ VIOLATION: Task branches still exist after CLEANUP\"
  git branch | grep \"${TASK_NAME}\"
  exit 1
fi

# Step 8: Remove task directory
rm -rf /workspace/tasks/${TASK_NAME}

# Step 9: VERIFICATION - Confirm complete cleanup
echo \"✅ CLEANUP complete:\"
echo \"   - Task worktree removed\"
echo \"   - Agent worktrees removed\"
echo \"   - All task branches deleted\"
echo \"   - Orphaned commits garbage collected\"
echo \"   - Task directory removed\"
\`\`\`

## 🚨 WHY GARBAGE COLLECTION IS CRITICAL

**Squash Merge Creates Orphaned Commits**:
- \`git merge --squash\` creates a NEW commit on main with all changes
- Original task branch commits (on \`${TASK_NAME}\`) become unreferenced
- Without \`git gc --prune=now\`, these orphaned commits persist forever
- This clutters the repository and wastes disk space

**Example from implement-formatter-api**:
- Task branch had commits: 5f95180, b393ce3
- Squash merge created: db1339b on main
- Original commits 5f95180, b393ce3 became unreferenced orphans
- User reported: \"Task didn't remove commits 5f95180, b393ce3\"
- Root cause: Missing garbage collection in CLEANUP

## Protocol Reference

See:
- task-protocol-operations.md § CLEANUP (Final State) - Phase 4: Garbage Collection
- main-agent-coordination.md § Step 21g: Garbage collect orphaned commits
- git-workflow.md § Cleanup After Squash Merge

**CRITICAL**: CLEANUP is AUTOMATIC (execute immediately, no user prompt)"

output_hook_notification "PostToolUse" "$MESSAGE"

log_hook_success "enforce-post-merge-cleanup" "PostToolUse" "Cleanup reminder issued"
exit 0
