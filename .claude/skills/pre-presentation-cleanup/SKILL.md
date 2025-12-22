---
name: pre-presentation-cleanup
description: Complete mandatory cleanup before presenting changes for user approval in AWAITING_USER_APPROVAL state
allowed-tools: Bash, Read
---

# Pre-Presentation Cleanup Skill

**Purpose**: Ensure all cleanup is completed BEFORE presenting changes to user for final approval
in AWAITING_USER_APPROVAL state.

**Why This Matters**: Presenting changes with unsquashed commits or lingering agent branches
requires rework and creates a poor user experience. Complete cleanup first, then present a clean
single commit for review.

## When to Use This Skill

### Use pre-presentation-cleanup When:

- Task is in AWAITING_USER_APPROVAL state (after validation passes)
- About to present changes to user for final approval
- Before showing commit SHA and `git diff --stat`

### Do NOT Use When:

- Still in IMPLEMENTATION or VALIDATION state
- Already presented and awaiting user response
- Task cleanup already completed

## Mandatory Cleanup Steps

Execute ALL steps before presenting changes to user:

### Step 1: Remove Agent Worktrees

```bash
TASK_NAME="{task-name}"

# Remove all agent worktrees
git worktree remove /workspace/tasks/$TASK_NAME/agents/architect/code --force 2>/dev/null || true
git worktree remove /workspace/tasks/$TASK_NAME/agents/tester/code --force 2>/dev/null || true
git worktree remove /workspace/tasks/$TASK_NAME/agents/formatter/code --force 2>/dev/null || true
```

### Step 2: Delete Subagent Branches

```bash
# Delete agent branches (task branch preserved)
git branch -D ${TASK_NAME}-architect 2>/dev/null || true
git branch -D ${TASK_NAME}-tester 2>/dev/null || true
git branch -D ${TASK_NAME}-formatter 2>/dev/null || true
```

### Step 3: Update Archival Files (MANDATORY)

**⚠️ CRITICAL**: The task branch commit MUST include todo.md and changelog.md updates.
This ensures the atomic commit includes both implementation AND archival.

```bash
TASK_NAME="{task-name}"
cd /workspace/tasks/$TASK_NAME/code

# Update todo.md: Mark task complete (change status from IN_PROGRESS to DONE)
# Update changelog.md: Add task completion entry with date and summary

# Stage archival files
git add todo.md changelog.md
```

**Archival Content Requirements**:

**todo.md update**:
- Change task status from `IN_PROGRESS` to `DONE`
- Add completion date
- Keep task entry for reference (will be cleaned in CLEANUP)

**changelog.md update**:
- Add entry under current date section
- Format: `- [task-name] Brief description of what was accomplished`
- Include key deliverables

### Step 4: Squash Commits (Including Archival)

Use the `git-squash` skill to squash ALL task branch commits into ONE:

```bash
# From task worktree
cd /workspace/tasks/$TASK_NAME/code

# IMPORTANT: Ensure todo.md and changelog.md are staged BEFORE squashing
git status  # Should show todo.md and changelog.md as staged

# Use git-squash skill (provides backup, verification, cleanup)
# Target: single commit with all changes since branching from main
# The squashed commit MUST include: implementation + todo.md + changelog.md
```

**Post-Squash Verification**:
```bash
# Verify archival files are in the commit
git show --stat | grep "todo.md" || echo "❌ ERROR: todo.md not in commit"
git show --stat | grep "changelog.md" || echo "❌ ERROR: changelog.md not in commit"
```

### Step 5: Verify Cleanup

```bash
# Verify only task branch remains (no agent branches)
git branch | grep $TASK_NAME
# Expected output: ONLY "{task-name}" (no -architect, -tester, -formatter suffixes)
```

### Step 6: Verify Single Commit with Archival

```bash
# Count commits ahead of main
git rev-list --count main..$TASK_NAME
# Expected output: 1

# MANDATORY: Verify archival files are included
git show --stat | grep -E "todo.md|changelog.md"
# Expected: Both todo.md and changelog.md appear in commit
```

## Complete Cleanup Script

```bash
#!/bin/bash
set -euo pipefail

TASK_NAME="${1:?Task name required}"
TASK_DIR="/workspace/tasks/$TASK_NAME"

echo "=== Pre-Presentation Cleanup for $TASK_NAME ==="

# Step 1: Remove agent worktrees
echo "Step 1: Removing agent worktrees..."
for agent in architect tester formatter; do
  WORKTREE="$TASK_DIR/agents/$agent/code"
  if [ -d "$WORKTREE" ]; then
    git worktree remove "$WORKTREE" --force 2>/dev/null && echo "  Removed $agent worktree" || true
  fi
done

# Step 2: Delete agent branches
echo "Step 2: Deleting agent branches..."
for agent in architect tester formatter; do
  BRANCH="${TASK_NAME}-${agent}"
  if git branch --list "$BRANCH" | grep -q .; then
    git branch -D "$BRANCH" && echo "  Deleted $BRANCH" || true
  fi
done

# Step 3: Squash commits (use git-squash skill or manual)
echo "Step 3: Squash commits using git-squash skill..."
echo "  → Use git-squash skill to squash all commits into one"

# Step 4: Verify cleanup
echo "Step 4: Verifying cleanup..."
REMAINING=$(git branch | grep "$TASK_NAME" | grep -v "^  $TASK_NAME$" || true)
if [ -n "$REMAINING" ]; then
  echo "  ❌ ERROR: Found remaining agent branches:"
  echo "$REMAINING"
  exit 1
else
  echo "  ✅ Only task branch remains"
fi

# Step 5: Verify single commit
echo "Step 5: Verifying single commit..."
COMMIT_COUNT=$(git rev-list --count main..$TASK_NAME)
if [ "$COMMIT_COUNT" -eq 1 ]; then
  echo "  ✅ Single commit verified"
else
  echo "  ❌ ERROR: Found $COMMIT_COUNT commits (expected 1)"
  echo "  Use git-squash skill to squash commits"
  exit 1
fi

echo ""
echo "=== Cleanup Complete ==="
echo "Ready to present changes for user approval"
```

## Common Mistakes

### Mistake: Presenting Unsquashed Commits

```
❌ WRONG:
User, here are the changes:
  commit abc123 - Architect implementation
  commit def456 - Tester additions
  commit ghi789 - Formatter fixes
  commit jkl012 - Merge agent work
```

```
✅ CORRECT:
[Complete all cleanup steps first]
User, here is the change:
  commit xyz789 - Implement feature X with tests and formatting
```

### Mistake: Subagent Branches Still Visible

```bash
# ❌ WRONG: Agent branches still exist
$ git branch | grep my-task
  my-task
  my-task-architect
  my-task-formatter
  my-task-tester

# ✅ CORRECT: Only task branch remains
$ git branch | grep my-task
  my-task
```

### Mistake: Missing Archival Files in Commit

```bash
# ❌ WRONG: Presenting without archival
$ git show --stat
  Parser.java   | 10 ++++
  Test.java     | 50 +++++++++++
  2 files changed, 60 insertions(+)
# Missing: todo.md and changelog.md!

# ✅ CORRECT: Commit includes archival files
$ git show --stat
  Parser.java   | 10 ++++
  Test.java     | 50 +++++++++++
  todo.md       |  2 +-
  changelog.md  |  3 +++
  4 files changed, 63 insertions(+), 1 deletion(-)
```

**Why This Matters**: The merge commit MUST be atomic - including both implementation
AND archival. Presenting without archival requires rework after user approval.

## Workflow Integration

```
[VALIDATION state: All tests pass]
        ↓
[Transition to AWAITING_USER_APPROVAL]
        ↓
[Invoke pre-presentation-cleanup skill] ← THIS SKILL
        ↓
Step 1: Remove agent worktrees
Step 2: Delete agent branches
Step 3: Update archival files (todo.md + changelog.md) ← CRITICAL
Step 4: Squash commits (including archival)
Step 5: Verify only task branch
Step 6: Verify single commit with archival
        ↓
[Present clean commit to user]
        ↓
[Wait for user approval]
```

## Related Skills

- **git-squash**: Used in Step 4 to squash commits
- **archive-task**: Alternative skill for archival (updates todo.md + changelog.md atomically)
- **task-cleanup**: Used AFTER merge to main (removes task branch and worktree)
- **state-transition**: Manages state machine transitions

## Verification Checklist

Before presenting to user, confirm:

- [ ] All agent worktrees removed
- [ ] All agent branches deleted
- [ ] **todo.md updated** (task status changed to DONE)
- [ ] **changelog.md updated** (task completion entry added)
- [ ] Commits squashed to single commit (including archival files)
- [ ] `git branch | grep {task}` shows ONLY task branch
- [ ] `git rev-list --count main..{task}` returns `1`
- [ ] `git show --stat` shows todo.md AND changelog.md in commit
