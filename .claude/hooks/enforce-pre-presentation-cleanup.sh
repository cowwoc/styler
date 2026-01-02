#!/bin/bash
# Enforce pre-presentation cleanup before AWAITING_USER_APPROVAL transition
#
# ADDED: 2026-01-01 after repeated mistakes where main agent skipped squashing
#        commits and deleting agent branches before presenting for user approval.
#
# IMPROVED: 2026-01-02 with enhanced messaging after refactor-parser-depth-limiting
#           showed that agent bypassed state-transition PreToolUse hook by using
#           state-transition skill instead of Edit tool. This hook now catches
#           Bash-based state transitions that bypass the Edit-tool validation hook.
#
# ROOT CAUSE OF ORIGINAL MISTAKE:
# - Agent used state-transition skill to transition to AWAITING_USER_APPROVAL
# - This bypassed validate-state-transition.sh hook (which only checks Edit tool)
# - This hook (enforce-pre-presentation-cleanup) should have caught it via Bash
# - But may not have been fully integrated at the time of mistake
#
# TRIGGER: PreToolUse on Bash commands
# CHECKS: When transitioning to AWAITING_USER_APPROVAL, BLOCKS unless:
#   1. Task branch has 1-2 commits relative to main (squashed)
#   2. No agent branches exist for this task
#   3. No agent worktrees exist
# ACTION: Outputs blocking error if checks fail (exit 2)
#
# PREVENTS: Presenting messy commit history or lingering agent branches for review
#
# Related:
# - main-agent-coordination.md § Lines 602-623: MANDATORY pre-presentation-cleanup (enhanced 2026-01-02)
# - pre-presentation-cleanup skill: Complete cleanup procedure
# - validate-state-transition.sh: PreToolUse hook for Edit tool state transitions
# - task-protocol-core.md § VALIDATION→AWAITING_USER_APPROVAL: State machine definition

set -euo pipefail

# Error handler
trap 'echo "ERROR in enforce-pre-presentation-cleanup.sh at line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Read hook input from stdin
INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // ""' 2>/dev/null || echo "")

# Only check commands that transition to AWAITING_USER_APPROVAL
if [[ ! "$COMMAND" =~ AWAITING_USER_APPROVAL ]]; then
	exit 0
fi

# Must also be modifying task state
if [[ ! "$COMMAND" =~ task\.json ]] && [[ ! "$COMMAND" =~ \.state ]]; then
	exit 0
fi

# Extract task name from current directory or command
TASK_NAME=""
if [[ "$PWD" =~ /workspace/tasks/([^/]+) ]]; then
	TASK_NAME="${BASH_REMATCH[1]}"
elif [[ "$COMMAND" =~ /workspace/tasks/([^/]+) ]]; then
	TASK_NAME=$(echo "$COMMAND" | grep -oP '/workspace/tasks/\K[^/]+' | head -1)
fi

if [[ -z "$TASK_NAME" ]]; then
	# Can't determine task name, allow but warn
	exit 0
fi

# Find task worktree
TASK_WORKTREE="/workspace/tasks/$TASK_NAME/code"
if [[ ! -d "$TASK_WORKTREE" ]]; then
	# No task worktree found, allow
	exit 0
fi

ISSUES=""

# Check 1: Verify task branch has 1-2 commits (squashed)
cd "$TASK_WORKTREE" 2>/dev/null || exit 0
COMMIT_COUNT=$(git rev-list --count main..HEAD 2>/dev/null || echo "0")

if [[ "$COMMIT_COUNT" -eq 0 ]]; then
	ISSUES+="- Task branch has NO commits relative to main (nothing to review)\n"
elif [[ "$COMMIT_COUNT" -gt 2 ]]; then
	ISSUES+="- Task branch has $COMMIT_COUNT commits (should be 1-2, use git-squash skill)\n"
fi

# Check 2: Verify no agent branches exist
AGENT_BRANCHES=$(git branch --list "*${TASK_NAME}-*" 2>/dev/null | grep -v "^\*" | tr -d ' ' || true)
if [[ -n "$AGENT_BRANCHES" ]]; then
	BRANCH_LIST=$(echo "$AGENT_BRANCHES" | tr '\n' ', ' | sed 's/,$//')
	ISSUES+="- Agent branches still exist: $BRANCH_LIST\n"
fi

# Check 3: Verify no agent worktrees exist
TASK_DIR="/workspace/tasks/$TASK_NAME"
for agent in architect tester formatter engineer builder quality style; do
	AGENT_WORKTREE="${TASK_DIR}/agents/${agent}/code"
	if [[ -d "$AGENT_WORKTREE" ]]; then
		ISSUES+="- Agent worktree still exists: ${agent}\n"
	fi
done

# If any issues found, BLOCK the transition
if [[ -n "$ISSUES" ]]; then
	# Source the json output helper
	source /workspace/.claude/scripts/json-output.sh 2>/dev/null || true

	MESSAGE="## 🚨 STATE TRANSITION BLOCKED - PRE-PRESENTATION CLEANUP REQUIRED

**Task**: \`$TASK_NAME\`
**Attempted Transition**: → \`AWAITING_USER_APPROVAL\`
**Violation**: Cleanup not completed before presenting changes for review

## ⚠️ ISSUES FOUND

$(echo -e "$ISSUES")

## 🧹 REQUIRED CLEANUP - INVOKE SKILL FIRST

Before transitioning to AWAITING_USER_APPROVAL, you MUST complete cleanup.

### Option 1: Use pre-presentation-cleanup skill (RECOMMENDED)

\`\`\`
Skill: pre-presentation-cleanup
\`\`\`

### Option 2: Manual Cleanup Steps

**Step 1: Remove Agent Worktrees**
\`\`\`bash
for agent in architect tester formatter engineer builder quality style; do
  git worktree remove ${TASK_DIR}/agents/\${agent}/code --force 2>/dev/null || true
done
\`\`\`

**Step 2: Delete Agent Branches**
\`\`\`bash
cd /workspace/main
for branch in \$(git branch --list '*${TASK_NAME}-*' | tr -d ' '); do
  git branch -D \"\$branch\" 2>/dev/null || true
done
\`\`\`

**Step 3: Squash Commits (use git-squash skill)**
\`\`\`bash
cd ${TASK_WORKTREE}
# Use git-squash skill for safe squashing with backup
Skill: git-squash
\`\`\`

**Step 4: Verify Cleanup**
\`\`\`bash
git rev-list --count main..HEAD  # Should output: 1 or 2
git branch | grep '${TASK_NAME}'  # Should show ONLY task branch
\`\`\`

## ✅ AFTER CLEANUP PASSES

Retry the state transition:
\`\`\`bash
jq '.state = \"AWAITING_USER_APPROVAL\"' /workspace/tasks/${TASK_NAME}/task.json > /tmp/task.tmp
mv /tmp/task.tmp /workspace/tasks/${TASK_NAME}/task.json
\`\`\`

## Protocol Reference

- main-agent-coordination.md § Lines 595-599: MANDATORY pre-presentation-cleanup
- pre-presentation-cleanup skill: Complete cleanup procedure"

	# Output blocking error using json format if available
	if type output_hook_error &>/dev/null; then
		output_hook_error "PreToolUse" "$MESSAGE"
	else
		echo "$MESSAGE" >&2
	fi

	# Exit with code 2 to BLOCK the command
	exit 2
fi

# All checks passed, allow transition
exit 0
