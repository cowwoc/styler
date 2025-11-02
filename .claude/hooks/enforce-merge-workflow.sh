#!/bin/bash
# Pre-Tool-Use Hook: Validate git merge workflow for task and agent branches
#
# ADDED: 2025-11-01 after main agent merged architect branch directly to main
# from /workspace/main during implement-formatter-api task, bypassing task branch
#
# PREVENTS: Direct agent-branch → main merges (should be agent → task → main)
# ENFORCES: Agent branches merge to task branch first, then task branch to main
#
# Triggers: PreToolUse (tool:Bash && command:*git*merge* matcher in settings.json)

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in enforce-merge-workflow.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")
TOOL_PARAMS=$(echo "$INPUT" | jq -r '.tool_input // {}' 2>/dev/null || echo "{}")

# Only validate Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
	exit 0
fi

# Extract command
COMMAND=$(echo "$TOOL_PARAMS" | jq -r '.command // empty' 2>/dev/null || echo "")

# Only validate git merge commands
if [[ ! "$COMMAND" =~ git[[:space:]]*merge ]]; then
	exit 0
fi

log_hook_start "enforce-merge-workflow" "PreToolUse"

# Extract branch being merged (after 'git merge')
MERGE_BRANCH=$(echo "$COMMAND" | sed -E 's/.*git[[:space:]]+merge[[:space:]]+([^[:space:]]+).*/\1/')

# Check if merging a task or agent branch
if [[ ! "$MERGE_BRANCH" =~ ^implement-.*-architect$ ]] && [[ ! "$MERGE_BRANCH" =~ ^implement-.*$ ]]; then
	# Not a task/agent branch, allow
	log_hook_success "enforce-merge-workflow" "PreToolUse" "Not a task/agent branch merge, allowing"
	exit 0
fi

# Determine branch type
BRANCH_TYPE="unknown"
if [[ "$MERGE_BRANCH" =~ -architect$ ]] || [[ "$MERGE_BRANCH" =~ -quality$ ]] || [[ "$MERGE_BRANCH" =~ -style$ ]] || [[ "$MERGE_BRANCH" =~ -updater$ ]]; then
	BRANCH_TYPE="agent"
elif [[ "$MERGE_BRANCH" =~ ^implement- ]]; then
	BRANCH_TYPE="task"
fi

# Get current working directory (where merge will run)
CURRENT_DIR="$PWD"

# RULE 1: Agent branches must merge from task worktree, not from /workspace/main
if [[ "$BRANCH_TYPE" == "agent" ]] && [[ "$CURRENT_DIR" == "/workspace/main" ]]; then
	# Extract task name from agent branch
	TASK_NAME=$(echo "$MERGE_BRANCH" | sed -E 's/^(implement-[^-]+(-[^-]+)*)-.*/\1/')

	log_hook_blocked "enforce-merge-workflow" "PreToolUse" "Agent branch merge blocked - must merge from task worktree"

	MESSAGE="## 🚨 MERGE WORKFLOW VIOLATION BLOCKED

**Agent Branch**: \`$MERGE_BRANCH\`
**Current Directory**: \`$CURRENT_DIR\`
**Violation**: Agent branches must merge to task branch first, then task branch to main

## ⚠️ CRITICAL - INCORRECT MERGE WORKFLOW

You attempted to merge an agent branch directly to main from /workspace/main.

**AUTOMATIC ACTION TAKEN**:
- Git merge blocked
- Violation logged

**CORRECT MERGE WORKFLOW**:

Agent branches must merge in TWO steps:
1. **Agent branch → Task branch** (from task worktree)
2. **Task branch → Main branch** (from /workspace/main)

**REQUIRED ACTION - Step 1: Merge Agent to Task Branch**:

\`\`\`bash
# 1. Change to task worktree
cd /workspace/tasks/${TASK_NAME}/code

# 2. Verify you're on the task branch
git branch --show-current
# Should show: ${TASK_NAME}

# 3. Merge agent branch to task branch
git merge ${MERGE_BRANCH} --no-edit

# 4. Verify build/tests pass
./mvnw verify

# 5. If validation passes, proceed to Step 2
\`\`\`

**Step 2: Merge Task Branch to Main** (after Step 1 succeeds):

\`\`\`bash
# 1. Change to main worktree
cd /workspace/main

# 2. Verify you're on main branch
git branch --show-current
# Should show: main

# 3. Get user approval for merge (MANDATORY CHECKPOINT)
# Present changes: git diff --stat main...${TASK_NAME}
# WAIT for user to say \"approved\", \"merge it\", or \"LGTM\"

# 4. After user approval, create approval flag
touch /workspace/tasks/${TASK_NAME}/user-approved-changes.flag

# 5. Merge task branch to main
git merge ${TASK_NAME} --no-edit
\`\`\`

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § POST_IMPLEMENTATION

**Merge Flow**:
Agent branch → Task branch (from task worktree) → Main branch (from /workspace/main)

**Why This Matters**:
- Task branch is integration point for all agent work
- Allows validation before merging to main
- Enforces user approval checkpoint before main merge"

	output_hook_error "PreToolUse" "$MESSAGE"
	exit 0
fi

# RULE 2: Task branches merging to main must have user approval
if [[ "$BRANCH_TYPE" == "task" ]] && [[ "$CURRENT_DIR" == "/workspace/main" ]]; then
	# Check if user approval flag exists
	TASK_DIR="/workspace/tasks/${MERGE_BRANCH}"
	APPROVAL_FLAG="${TASK_DIR}/user-approved-changes.flag"

	if [[ ! -f "$APPROVAL_FLAG" ]]; then
		log_hook_blocked "enforce-merge-workflow" "PreToolUse" "Task branch merge blocked - no user approval"

		MESSAGE="## 🚨 USER APPROVAL REQUIRED

**Task Branch**: \`$MERGE_BRANCH\`
**Violation**: Task branch merge to main requires user approval

## ⚠️ MANDATORY USER APPROVAL CHECKPOINT

You attempted to merge task branch to main without user approval.

**AUTOMATIC ACTION TAKEN**:
- Git merge blocked
- Violation logged

**REQUIRED ACTION**:

1. **Present changes to user**:
   \`\`\`bash
   # Show what will be merged
   git diff --stat main...${MERGE_BRANCH}
   git log --oneline main..${MERGE_BRANCH}
   \`\`\`

2. **WAIT for explicit user approval**:
   - User must say \"approved\", \"LGTM\", \"looks good\", or \"merge it\"
   - Do NOT proceed without approval

3. **After receiving approval, create approval flag**:
   \`\`\`bash
   touch /workspace/tasks/${MERGE_BRANCH}/user-approved-changes.flag
   \`\`\`

4. **Then retry merge** (this block will be removed):
   \`\`\`bash
   git merge ${MERGE_BRANCH} --no-edit
   \`\`\`

## Protocol Reference

See: /workspace/main/CLAUDE.md § MANDATORY USER APPROVAL CHECKPOINTS"

		output_hook_error "PreToolUse" "$MESSAGE"
		exit 0
	fi
fi

log_hook_success "enforce-merge-workflow" "PreToolUse" "Merge workflow validated"
exit 0
