#!/bin/bash
# Validates pre-review cleanup before transitioning to AWAITING_USER_APPROVAL
#
# TRIGGER: PostToolUse on Bash commands that update task.json state
# CHECKS: When transitioning to AWAITING_USER_APPROVAL, verifies:
#   1. Task branch has exactly 1 commit relative to main (squashed)
#   2. No agent branches exist for this task (cleaned up)
# ACTION: Outputs warning with required cleanup steps if checks fail
#
# ADDED: After mistake where main agent presented 6 unsquashed commits
#        and 3 agent branches for review (session ec47cea5)
# PREVENTS: Presenting messy commit history for user review
#
# Related Protocol Sections:
# - git-workflow.md § Task Branch Squashing: "MUST have exactly 1 squashed commit before merge"
# - main-agent-coordination.md line 555: "Delete agent branches (before squash)"

set -euo pipefail

# Error handler
trap 'echo "ERROR in validate-pre-review-cleanup.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read hook input from stdin
INPUT=$(cat)
STDOUT=$(echo "$INPUT" | jq -r '.stdout // ""')
COMMAND=$(echo "$INPUT" | jq -r '.command // ""')

# Only check commands that modify task.json state
if [[ ! "$COMMAND" =~ task\.json ]] || [[ ! "$COMMAND" =~ jq ]]; then
	exit 0
fi

# Check if transitioning to AWAITING_USER_APPROVAL
if [[ ! "$STDOUT" =~ AWAITING_USER_APPROVAL ]] && [[ ! "$COMMAND" =~ AWAITING_USER_APPROVAL ]]; then
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
	# Can't determine task name, skip validation
	exit 0
fi

# Find task worktree
TASK_WORKTREE="/workspace/tasks/$TASK_NAME/code"
if [[ ! -d "$TASK_WORKTREE" ]]; then
	# No task worktree found, skip
	exit 0
fi

WARNINGS=""

# Check 1: Verify task branch has exactly 1 commit
cd "$TASK_WORKTREE" 2>/dev/null || exit 0
COMMIT_COUNT=$(git rev-list --count main..HEAD 2>/dev/null || echo "0")

if [[ "$COMMIT_COUNT" -ne 1 ]]; then
	WARNINGS+="
## ⚠️ COMMIT SQUASHING REQUIRED

**Task branch has $COMMIT_COUNT commits** (should be exactly 1)

Before presenting for user review, you MUST squash all commits:

\`\`\`bash
cd $TASK_WORKTREE

# Squash all commits into one
git reset --soft \$(git merge-base main HEAD)
git commit -m \"Implement $TASK_NAME

[Comprehensive description of all changes]

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>\"

# Verify exactly 1 commit
git rev-list --count main..HEAD  # Should output: 1
\`\`\`
"
fi

# Check 2: Verify no agent branches exist
AGENT_BRANCHES=$(git branch --list "*${TASK_NAME}-*" 2>/dev/null | grep -v "^\\*" | tr -d ' ' || true)

if [[ -n "$AGENT_BRANCHES" ]]; then
	WARNINGS+="
## ⚠️ AGENT BRANCHES MUST BE DELETED

The following agent branches still exist:
\`\`\`
$AGENT_BRANCHES
\`\`\`

Before presenting for user review, delete these branches:

\`\`\`bash
cd /workspace/main  # Must be in main worktree to delete

# Delete agent branches
$(echo "$AGENT_BRANCHES" | while read branch; do echo "git branch -D $branch"; done)
\`\`\`
"
fi

# Output warnings if any issues found
if [[ -n "$WARNINGS" ]]; then
	MESSAGE="## 🧹 PRE-REVIEW CLEANUP REQUIRED

You are transitioning to AWAITING_USER_APPROVAL but cleanup is incomplete.
$WARNINGS

## Required Sequence

1. Delete agent branches (from /workspace/main)
2. Squash all commits into one
3. THEN transition to AWAITING_USER_APPROVAL
4. Present clean, single commit for user review

## Protocol Reference

- git-workflow.md line 141: \"Task branches MUST have exactly 1 squashed commit before merge\"
- main-agent-coordination.md line 555: \"Delete agent branches (before squash to keep history clean)\""

	jq -n \
		--arg event "PostToolUse" \
		--arg context "$MESSAGE" \
		'{
			"hookSpecificOutput": {
				"hookEventName": $event,
				"additionalContext": $context
			}
		}'
fi

exit 0
