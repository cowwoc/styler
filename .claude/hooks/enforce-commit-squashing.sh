#!/bin/bash
# Enforces mandatory commit squashing before merging task branches to main
# This hook validates that task branches contain exactly ONE commit when merged
#
# TRIGGER: PreToolUse on Bash commands containing "git merge" (via .claude/settings.json)
# CHECKS: When merging task branch to main, verifies branch has exactly 1 commit
# ACTION: Blocks merge and displays squashing instructions if multiple commits detected
#
# Related Protocol Sections:
# - task-protocol-core.md § REVIEW → COMPLETE {#review-complete-unanimous-approval-gate}
# - git-workflow.md § Task Branch Squashing (Task Protocol)
# - CLAUDE.md § Protocol Violation #3: Missing Commit Squashing

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in enforce-commit-squashing.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read hook input from stdin
INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.command // ""')

# Only check git merge commands
if [[ ! "$COMMAND" =~ git[[:space:]]+merge ]]; then
	exit 0
fi

# Extract branch name from merge command
# Handles: git merge <branch>, git merge --ff-only <branch>, etc.
BRANCH_NAME=$(echo "$COMMAND" | grep -oP 'git\s+merge\s+(?:--\S+\s+)*\K\S+' || echo "")

if [[ -z "$BRANCH_NAME" ]]; then
	# Can't parse branch name, allow command
	exit 0
fi

# Only check merges to main branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
if [[ "$CURRENT_BRANCH" != "main" ]]; then
	# Not merging to main, allow command
	exit 0
fi

# Check if this is a task branch (format: {task-name} or {task-name}-{agent})
if [[ ! "$BRANCH_NAME" =~ ^[a-z0-9-]+$ ]]; then
	# Not a task branch pattern, allow command
	exit 0
fi

# Validate merge command uses --ff-only flag for task branches
if [[ ! "$COMMAND" =~ --ff-only ]]; then
	# VIOLATION: Task branch merge without --ff-only flag
	MESSAGE="## 🚨 LINEAR HISTORY VIOLATION DETECTED AND BLOCKED

**Branch**: \`$BRANCH_NAME\`
**Attempted command**: \`$COMMAND\`
**Required flag**: \`--ff-only\`

## ⚠️ CRITICAL - MANDATORY LINEAR HISTORY REQUIREMENT

You attempted to merge a task branch without the \`--ff-only\` flag.
This creates merge commits and violates the linear history requirement.

**AUTOMATIC ACTION TAKEN**:
- Merge command blocked
- Task branch unchanged

**REQUIRED COMMAND**:

\`\`\`bash
git merge --ff-only $BRANCH_NAME
\`\`\`

**WHY --ff-only IS MANDATORY**:
- Prevents accidental merge commits
- Enforces linear git history on main branch
- Makes git bisect and history navigation simpler
- Ensures each commit on main is an atomic task unit

**IF FAST-FORWARD FAILS**:
If git reports \"fatal: Not possible to fast-forward\", this means:
1. Main branch has moved ahead since task branch was created
2. You need to rebase the task branch onto latest main:

\`\`\`bash
# Step 1: Update main
git checkout main
git pull

# Step 2: Rebase task branch
git checkout $BRANCH_NAME
git rebase main

# Step 3: Merge with fast-forward
git checkout main
git merge --ff-only $BRANCH_NAME
\`\`\`

## Protocol Reference

See:
- /workspace/main/docs/project/git-workflow.md § Task Branch Squashing
- /workspace/main/docs/project/task-protocol-core.md (line 3886)

**PROHIBITED PATTERNS**:
❌ \`git merge <branch>\` (default, may create merge commit)
❌ \`git merge --no-ff <branch>\` (explicitly creates merge commit)
❌ \`git merge --squash <branch>\` (loses commit history)

**REQUIRED PATTERN**:
✅ \`git merge --ff-only <branch>\` (enforces linear history)"

	jq -n \
		--arg event "PreToolUse" \
		--arg context "$MESSAGE" \
		'{
			"hookSpecificOutput": {
				"hookEventName": $event,
				"additionalContext": $context
			}
		}'

	exit 2  # Block the command
fi

# Check if branch exists
if ! git rev-parse --verify "$BRANCH_NAME" >/dev/null 2>&1; then
	# Branch doesn't exist, let git merge fail naturally
	exit 0
fi

# Count commits on the branch relative to main
COMMIT_COUNT=$(git rev-list --count main.."$BRANCH_NAME" 2>/dev/null || echo "0")

if [[ "$COMMIT_COUNT" -gt 1 ]]; then
	# VIOLATION: Multiple commits on task branch
	MESSAGE="## 🚨 COMMIT SQUASHING VIOLATION DETECTED AND BLOCKED

**Branch**: \`$BRANCH_NAME\`
**Commits on branch**: $COMMIT_COUNT
**Required**: Exactly 1 commit
**Attempted command**: \`$COMMAND\`

## ⚠️ CRITICAL - MANDATORY COMMIT SQUASHING REQUIREMENT

You attempted to merge a task branch with $COMMIT_COUNT commits to main.
This violates the task protocol commit squashing requirement.

**AUTOMATIC ACTION TAKEN**:
- Merge command blocked
- Task branch unchanged

**REQUIRED ACTION**:

Squash all commits on the task branch into a single commit before merging:

\`\`\`bash
# Step 1: Switch to the task branch
git checkout $BRANCH_NAME

# Step 2: Count commits (verification)
COMMIT_COUNT=\$(git rev-list --count main..$BRANCH_NAME)
echo \"Task branch has \$COMMIT_COUNT commits\"

# Step 3: Interactive rebase to squash commits
git rebase -i main

# In the interactive editor that opens:
# - Line 1: Keep as \"pick\" (first commit)
# - Lines 2-N: Change ALL to \"squash\" (or \"s\")
# - Save and exit
#
# Then edit the combined commit message:
# - Create single comprehensive message for the entire task
# - Remove redundant individual commit messages
# - Save and exit

# Step 4: Verify exactly 1 commit remains
FINAL_COUNT=\$(git rev-list --count main..$BRANCH_NAME)
if [ \"\$FINAL_COUNT\" -ne 1 ]; then
  echo \"❌ Still have \$FINAL_COUNT commits, need exactly 1\"
  exit 1
fi

echo \"✅ Task branch ready for merge: 1 commit\"

# Step 5: Return to main and merge
git checkout main
git merge --ff-only $BRANCH_NAME
\`\`\`

**ALTERNATIVE - Automated Squashing**:

\`\`\`bash
# Checkout task branch
git checkout $BRANCH_NAME

# Reset to create squashed commit
git reset --soft \$(git merge-base main $BRANCH_NAME)
git commit -m \"[Comprehensive task message summarizing all changes]

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
[List all contributing agents]\"

# Verify and merge
git checkout main
git merge --ff-only $BRANCH_NAME
\`\`\`

## Protocol Reference

See:
- /workspace/main/docs/project/task-protocol-core.md § REVIEW → COMPLETE {#review-complete-unanimous-approval-gate}
- /workspace/main/docs/project/git-workflow.md § Task Branch Squashing

**WHY THIS REQUIREMENT**:
- Maintains atomic task units in main branch history
- Each commit on main represents one complete task
- Simplifies history navigation and git bisect
- Prevents main branch pollution with implementation details

**PROHIBITED PATTERNS**:
❌ Merging task branch with multiple commits
❌ Using merge commits instead of fast-forward merges
❌ Assuming \"clean history\" doesn't matter
❌ Bypassing squashing for \"small\" changes"

	jq -n \
		--arg event "PreToolUse" \
		--arg context "$MESSAGE" \
		'{
			"hookSpecificOutput": {
				"hookEventName": $event,
				"additionalContext": $context
			}
		}'

	exit 2  # Block the command
fi

# Exactly 1 commit or 0 commits - allow merge
exit 0
