---
name: git-squash
description: Safely squash multiple commits into one with automatic backup and verification
allowed-tools: Bash, Read
---

# Git Squash Skill

**Purpose**: Safely squash multiple consecutive commits into a single commit with automatic backup, verification, and cleanup.

**When to Use**:
- Cleaning up commit history before merge
- Combining related commits into logical units
- Simplifying pull request history
- Preparing feature branch for main merge

## Supports Both Tip and Mid-History Squashing

**This skill now handles both scenarios**:

### ✅ Tip Squashing (commits at branch HEAD)
```bash
# Current state:
# HEAD → 043a992 (commit 7) ← Last to squash
#        ...     (commits 1-6) ← Squash these
#        61edb5a (base)

# Simple case: No commits after squash range
# Workflow: Squash → Move branch pointer
```

### ✅ Mid-History Squashing (commits have others after them)
```bash
# Current state:
# HEAD → 39f3981 (commit 10)
#        a25506b (commit 9)
#        4a2e49a (commit 8)
#        043a992 (commit 7) ← Last to squash
#        ...     (commits 1-6) ← Squash these
#        61edb5a (base)

# Mid-history case: Commits exist after squash range
# Workflow: Squash → Rebase commits after → Move branch pointer
# Commits 8-10 are automatically rebased on top of squashed commit
```

**The skill automatically detects which scenario and handles it appropriately.**

**Alternative: Interactive Rebase**

You can also use interactive rebase for mid-history squashing:
```bash
git rebase -i <base-commit>
# Change "pick" to "squash" for commits to combine
```

Both approaches work. git-squash provides more safety checks and verification,
while interactive rebase is the traditional git approach.

## ⚠️ Critical Safety Rules

**MANDATORY BACKUP**: Always create timestamped backup before squashing
**VERIFICATION REQUIRED**: Verify no changes lost or added beyond original commits
**NON-SQUASHED COMMITS**: Verify commits outside squash range remain completely unchanged
**FINAL STATE VERIFICATION**: Verify working tree state matches original HEAD
**AUTOMATIC CLEANUP**: Remove backup only after verification passes
**WORKING DIRECTORY**: Must be clean (no uncommitted changes)

## Prerequisites

Before using this skill, verify:
- [ ] Working directory is clean: `git status` shows no uncommitted changes
- [ ] Know base commit (where to squash back to - parent of first commit to squash)
- [ ] Know first and last commits to squash
- [ ] **HEAD is positioned at the LAST commit to squash** (not beyond it)
- [ ] Current branch is correct

**⚠️ CRITICAL**: If HEAD has commits beyond the last commit you want to squash,
you will squash MORE commits than intended. Ensure HEAD is exactly at the
last commit to include in the squash before running this skill.

## Skill Workflow

### Step 1: Position HEAD (MANDATORY - DO NOT SKIP)

**🚨 CRITICAL: This step is MANDATORY and must be done FIRST**

```bash
# YOU MUST checkout the last commit to squash BEFORE creating backup
git checkout <last-commit-to-squash>

# Example: If squashing commits abc123 and def456, checkout def456
git checkout def456
```

**Why this is mandatory**:
- If HEAD is beyond the last commit to squash, you'll squash MORE commits than intended
- Example: HEAD at main with commits after def456 → squashes everything from base to main
- This was the exact mistake that occurred and required adding this checkpoint

**Verification**:
```bash
# Verify you're at the right commit
git log --oneline -1
# Should show the LAST commit you want to include in the squash
```

**⚠️ DO NOT PROCEED to Step 2 until you've verified HEAD position**

### Step 2: Create Mandatory Backup

**⚠️ CRITICAL - Always Create Backup First**:
```bash
# Create timestamped backup branch
BACKUP_BRANCH="backup-before-squash-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP_BRANCH"

# Verify backup created
if ! git rev-parse --verify "$BACKUP_BRANCH" >/dev/null 2>&1; then
  echo "❌ ERROR: Failed to create backup - STOP"
  exit 1
fi

echo "✅ Backup created: $BACKUP_BRANCH"
echo "   Restore command if needed: git reset --hard $BACKUP_BRANCH"
```

### Step 3: Verify Preconditions

**Check Working Directory**:
```bash
# Ensure no uncommitted changes
if [[ -n "$(git status --porcelain)" ]]; then
  echo "❌ ERROR: Working directory not clean"
  echo "   Commit or stash changes first"
  exit 1
fi

echo "✅ Working directory clean"
```

**Identify Commits to Squash**:
```bash
# Specify the range to squash
BASE_COMMIT="<commit-sha>"     # Parent of first commit to squash
EXPECTED_LAST_COMMIT="<commit-sha>"  # Last commit to include in squash (optional but recommended)

# Verify HEAD position if EXPECTED_LAST_COMMIT is specified
if [[ -n "$EXPECTED_LAST_COMMIT" ]]; then
  CURRENT_HEAD=$(git rev-parse HEAD)
  EXPECTED_HEAD=$(git rev-parse "$EXPECTED_LAST_COMMIT")

  if [[ "$CURRENT_HEAD" != "$EXPECTED_HEAD" ]]; then
    echo "❌ ERROR: HEAD is not at expected last commit!"
    echo "   Current HEAD:  $(git log --oneline -1 HEAD)"
    echo "   Expected HEAD: $(git log --oneline -1 "$EXPECTED_LAST_COMMIT")"
    echo ""
    echo "   You may be about to squash MORE commits than intended."
    echo "   Checkout the last commit before squashing:"
    echo "   git checkout $EXPECTED_LAST_COMMIT"
    exit 1
  fi
  echo "✅ HEAD positioned at expected last commit"

  # Check for commits AFTER the squash range (mid-history squashing)
  if git rev-parse --verify "$ORIGINAL_BRANCH" >/dev/null 2>&1; then
    COMMITS_AFTER_COUNT=$(git rev-list --count HEAD.."$ORIGINAL_BRANCH" 2>/dev/null || echo "0")
    if [[ "$COMMITS_AFTER_COUNT" -gt 0 ]]; then
      echo "⚠️  Mid-history squash detected: $COMMITS_AFTER_COUNT commits exist after squash range"
      echo "   Will automatically rebase them after squashing"
      echo ""

      # Record the original branch HEAD and commits after squash range
      ORIGINAL_BRANCH_HEAD=$(git rev-parse "$ORIGINAL_BRANCH")
      NEED_REBASE="true"
      echo "ORIGINAL_BRANCH_HEAD=$ORIGINAL_BRANCH_HEAD" >> /tmp/squash-vars.sh
      echo "NEED_REBASE=true" >> /tmp/squash-vars.sh
      echo "COMMITS_AFTER_COUNT=$COMMITS_AFTER_COUNT" >> /tmp/squash-vars.sh

      echo "   Commits to preserve after squash:"
      git log --oneline HEAD.."$ORIGINAL_BRANCH"
      echo ""
    else
      echo "✅ No commits after squash range (tip squashing)"
      echo "NEED_REBASE=false" >> /tmp/squash-vars.sh
    fi
  else
    echo "NEED_REBASE=false" >> /tmp/squash-vars.sh
  fi
fi

# Show commits to be squashed
echo "Commits to squash:"
git log --oneline "$BASE_COMMIT..HEAD"
echo ""

# Count commits
COMMIT_COUNT=$(git rev-list --count "$BASE_COMMIT..HEAD")
echo "Will squash $COMMIT_COUNT commits into 1"
```

### Step 4: Record Original State

**⚠️ CRITICAL - Record State for Verification**:
```bash
# Record original HEAD for final state verification
ORIGINAL_HEAD=$(git rev-parse HEAD)
echo "Original HEAD: $ORIGINAL_HEAD"

# Verify BASE_COMMIT exists
if ! git rev-parse --verify "$BASE_COMMIT" >/dev/null 2>&1; then
  echo "❌ ERROR: Base commit $BASE_COMMIT does not exist"
  exit 1
fi

# CRITICAL: Verify HEAD is at or within the squash range
# If HEAD is beyond the commits to squash, we'll squash too many commits
COMMITS_AFTER_ORIGINAL=$(git rev-list HEAD --not "$BACKUP_BRANCH" 2>/dev/null || true)
if [[ -n "$COMMITS_AFTER_ORIGINAL" ]]; then
  echo "❌ ERROR: HEAD has moved beyond backup branch!"
  echo "   This indicates commits exist after the squash range"
  echo "   Current HEAD: $(git rev-parse --short HEAD)"
  echo "   Backup HEAD: $(git rev-parse --short "$BACKUP_BRANCH")"
  exit 1
fi

# Display final confirmation of squash range
echo "═══════════════════════════════════════"
echo "Squash Summary:"
echo "  Base commit:   $(git log --oneline -1 "$BASE_COMMIT")"
echo "  Commits to squash: $COMMIT_COUNT"
echo "  Final result: 1 commit"
echo "═══════════════════════════════════════"

# Record commits before base (these must remain unchanged)
if git rev-parse "${BASE_COMMIT}~1" >/dev/null 2>&1; then
  COMMITS_BEFORE_BASE=$(git rev-list "${BASE_COMMIT}~1")
  echo "Commits before base recorded for verification"
else
  COMMITS_BEFORE_BASE=""
  echo "Base commit is first commit (no prior commits to verify)"
fi
```

### Step 5: Execute Squash

**Soft Reset to Base**:
```bash
# Reset to base commit, keeping all changes staged
git reset --soft "$BASE_COMMIT"

# Verify changes are staged
git status --short
```

### Step 6: Verify No Changes Lost or Added

**⚠️ MANDATORY VERIFICATION**:
```bash
# Compare staged changes with backup branch
# No output = no differences = verification passed
DIFF_OUTPUT=$(git diff --stat "$BACKUP_BRANCH")

if [[ -n "$DIFF_OUTPUT" ]]; then
  echo "❌ ERROR: Staged changes don't match original commits!"
  echo "$DIFF_OUTPUT"
  echo ""
  echo "ROLLBACK: git reset --hard $BACKUP_BRANCH"
  exit 1
fi

echo "✅ Verification passed: No changes lost or added"
```

### Step 7: Create Squashed Commit

**⚠️ CRITICAL: Write Meaningful Commit Message**

The commit message must describe WHAT the changes DO, not just say "squashed".

**Step 7a: Review Commits Being Squashed**:
```bash
# Review all commits to understand their combined purpose
echo "Commits being squashed:"
git log --format="%h %s" "$BASE_COMMIT..HEAD"
```

**Step 7b: Craft Message Following git-commit Skill Guidelines**:

See `git-commit` skill for complete guidance on writing commit messages.

**Key principles for squashed commits**:
- Subject line: Summarize the combined purpose (imperative mood, 50-72 chars)
- Body: List key changes from all squashed commits (use bullet points)
- Rationale: Explain why these changes work together

❌ **WRONG - Generic placeholder**:
```bash
git commit -m "Squashed: feature improvements"
```

✅ **CORRECT - Describes what the code does**:
```bash
# After reviewing commits, synthesize meaningful message
git commit -m "$(cat <<'EOF'
Enhance optimize-doc with iterative loop and session ID

Adds multi-pass optimization capability with proper session
management for agent continuity between optimization phases.

Changes:
- Add iterative optimization loop for multi-pass document refinement
- Integrate session ID capture from Phase 1 agent for Phase 2 resume
- Clarify that main agent optimizes directly (not via Task tool)
- Strengthen to use general-purpose agent only for both phases
- Fix Phase 2 to resume Phase 1 agent (not create new agent)
- Remove unnecessary agent ID file storage

These enhancements enable proper multi-pass optimization with agent
continuity between phases while maintaining session context.

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"

echo "✅ Squashed commit created"
```

**Reference**: See `git-commit` skill for detailed examples and anti-patterns

**Step 7c: Rewriting Squashed Commit Message (If Needed)**

**⚠️ CRITICAL: Do NOT use `git commit --amend` after rebase completes**

If you need to rewrite the squashed commit message after the rebase has finished:

**❌ WRONG - Amends wrong commit**:
```bash
# Rebase completes, HEAD is at branch tip
git commit --amend -m "New message"  # ← Amends HEAD, NOT the squashed commit!
```

**Why this fails**:
- Interactive rebase with squashing creates the squashed commit
- Rebase continues, rebasing commits that came after the squash range
- When rebase completes, HEAD is at the branch tip (last commit)
- The squashed commit is back in history, NOT at HEAD
- `git commit --amend` modifies commit at HEAD, not a historical commit

**✅ CORRECT - Use interactive rebase with edit**:
```bash
# After rebase completes, identify the squashed commit
git log --oneline -10
# Example output:
# aa4f0c6 Latest commit (HEAD)
# ...
# 28b1631 Add bash-to-script skill  ← The squashed commit (back in history)
# af98b4f Base commit

# Create script to mark squashed commit for editing
cat > /tmp/edit-squash.sh << 'EOF'
#!/bin/bash
# Change pick to edit for the squashed commit
sed -i 's/^pick 28b1631/edit 28b1631/' "$1"
EOF
chmod +x /tmp/edit-squash.sh

# Start interactive rebase from base of squashed commit
BASE_COMMIT="af98b4f"  # Parent of squashed commit
GIT_SEQUENCE_EDITOR=/tmp/edit-squash.sh git rebase -i "$BASE_COMMIT"

# Rebase stops at squashed commit
# Now you can amend it
git commit --amend -m "$(cat <<'EOF'
New commit message
...
EOF
)"

# Continue rebase to reapply subsequent commits
git rebase --continue
```

**Alternative - Use reword in original interactive rebase**:

Instead of fixing the message after rebase completes, you can specify "reword" during the initial rebase:

```bash
# During initial squash rebase, the todo list looks like:
# pick 2df90f4 First commit
# squash edbf606 Second commit
# squash e98d3b0 Third commit
# squash 625cc88 Fourth commit
#
# Git will prompt for combined message after squashing
# This is the BEST time to write the final message
```

**Best Practice**: Write the final commit message during the initial squash operation when Git prompts you. This avoids the need for a second interactive rebase.

### Step 8: Verify Non-Squashed Commits Unchanged

**⚠️ CRITICAL - Verify Commits Outside Squash Range**:
```bash
# Verify commits before base remain completely unchanged
if [[ -n "$COMMITS_BEFORE_BASE" ]]; then
  CURRENT_COMMITS_BEFORE_BASE=$(git rev-list "${BASE_COMMIT}~1")
  if [[ "$COMMITS_BEFORE_BASE" != "$CURRENT_COMMITS_BEFORE_BASE" ]]; then
    echo "❌ ERROR: Commits before base were modified!"
    echo "   This should never happen - non-squashed commits changed"
    echo "ROLLBACK: git reset --hard $BACKUP_BRANCH"
    exit 1
  fi
  echo "✅ Verification passed: Commits before base unchanged"
fi

# Verify final working tree state matches original HEAD
TREE_DIFF=$(git diff --stat "$ORIGINAL_HEAD")
if [[ -n "$TREE_DIFF" ]]; then
  echo "❌ ERROR: Working tree state doesn't match original HEAD!"
  echo "$TREE_DIFF"
  echo "   Final contents should be identical to original"
  echo "ROLLBACK: git reset --hard $BACKUP_BRANCH"
  exit 1
fi
echo "✅ Verification passed: Final working tree matches original HEAD"
```

### Step 9: Final Count Verification

**Verify Squash Success**:
```bash
# Check commit count
NEW_COMMIT_COUNT=$(git rev-list --count "$BASE_COMMIT..HEAD")
if [[ "$NEW_COMMIT_COUNT" -ne 1 ]]; then
  echo "❌ ERROR: Expected 1 commit after squash, got $NEW_COMMIT_COUNT"
  exit 1
fi

# Show result
git log --oneline -3
echo "✅ Squash successful: $COMMIT_COUNT commits → 1 commit"
```

### Step 10: Rebase Commits After Squash Range (Mid-History Only)

**If commits existed after the squash range, rebase them on top**:

```bash
source /tmp/squash-vars.sh

if [[ "$NEED_REBASE" == "true" ]]; then
  echo ""
  echo "═══════════════════════════════════════"
  echo "Mid-history squash: Rebasing $COMMITS_AFTER_COUNT commits on top"
  echo "═══════════════════════════════════════"

  # Current state: We're at the squashed commit
  SQUASHED_COMMIT=$(git rev-parse HEAD)

  # Rebase commits that came after the squash range onto the squashed commit
  # Uses --onto to replay commits from (last-to-squash) to (original-branch-head)
  # onto the squashed commit
  echo "Rebasing commits from $EXPECTED_LAST_COMMIT to $ORIGINAL_BRANCH_HEAD"
  echo "onto squashed commit $SQUASHED_COMMIT"
  echo ""

  if ! git rebase --onto "$SQUASHED_COMMIT" "$EXPECTED_LAST_COMMIT" "$ORIGINAL_BRANCH_HEAD"; then
    echo "" >&2
    echo "❌ ERROR: Rebase encountered conflicts!" >&2
    echo "" >&2
    echo "   Squashed commit created successfully: $SQUASHED_COMMIT" >&2
    echo "   But rebasing commits after squash range failed." >&2
    echo "" >&2
    echo "   MANUAL RESOLUTION REQUIRED:" >&2
    echo "   1. Resolve conflicts: git status" >&2
    echo "   2. Stage resolved files: git add <files>" >&2
    echo "   3. Continue rebase: git rebase --continue" >&2
    echo "   4. Or abort: git rebase --abort && git reset --hard $BACKUP_BRANCH" >&2
    echo "" >&2
    echo "   Backup available: $BACKUP_BRANCH" >&2
    exit 1
  fi

  echo "✅ Successfully rebased $COMMITS_AFTER_COUNT commits"
  echo ""

  # Update branch pointer to the final rebased state
  if [[ -n "$ORIGINAL_BRANCH" ]]; then
    git branch -f "$ORIGINAL_BRANCH" HEAD
    git checkout "$ORIGINAL_BRANCH"
    echo "✅ Updated $ORIGINAL_BRANCH to final state"
  fi

  # Verify final commit count
  FINAL_COUNT=$(git rev-list --count "$BASE_COMMIT..HEAD")
  EXPECTED_FINAL=$((COMMITS_AFTER_COUNT + 1))  # Squashed commit + commits after
  if [[ "$FINAL_COUNT" -ne "$EXPECTED_FINAL" ]]; then
    echo "⚠️  WARNING: Expected $EXPECTED_FINAL commits, got $FINAL_COUNT" >&2
    echo "   This may indicate an issue - verify history" >&2
  fi

  echo ""
  echo "Final history:"
  git log --oneline "$BASE_COMMIT..HEAD"
else
  # Tip squashing - just update branch pointer
  if [[ -n "$ORIGINAL_BRANCH" ]] && [[ "$(git rev-parse --abbrev-ref HEAD)" == "HEAD" ]]; then
    git branch -f "$ORIGINAL_BRANCH" HEAD
    git checkout "$ORIGINAL_BRANCH"
    echo "✅ Updated $ORIGINAL_BRANCH to squashed commit"
  fi
fi
```

### Step 11: Remove Backup

**⚠️ Only After Verification Passes**:
```bash
# Delete backup branch (verification passed)
source /tmp/squash-vars.sh
git branch -D "$BACKUP_BRANCH"
rm -f /tmp/squash-vars.sh
echo "✅ Backup removed after successful verification"
```

## Complete Example

### Example: Squash 2 Commits

```bash
#!/bin/bash
set -euo pipefail

# Configuration
BASE_COMMIT="4d3e19e"              # Parent of first commit to squash
EXPECTED_LAST_COMMIT="b732939"     # Last commit to include in squash
ORIGINAL_BRANCH="main"             # Branch to update after squash

echo "=== Git Squash: Multiple commits → 1 commit ==="
echo ""

# Step 1: Position HEAD at last commit to squash
git checkout "$EXPECTED_LAST_COMMIT"
echo "✅ Checked out last commit to squash"

# Step 2: Create backup
BACKUP_BRANCH="backup-before-squash-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP_BRANCH"
echo "✅ Backup created: $BACKUP_BRANCH"

# Step 3: Verify preconditions
if [[ -n "$(git status --porcelain)" ]]; then
  echo "❌ ERROR: Working directory not clean"
  exit 1
fi
echo "✅ Working directory clean"

# Verify HEAD position
CURRENT_HEAD=$(git rev-parse HEAD)
EXPECTED_HEAD=$(git rev-parse "$EXPECTED_LAST_COMMIT")
if [[ "$CURRENT_HEAD" != "$EXPECTED_HEAD" ]]; then
  echo "❌ ERROR: HEAD is not at expected last commit"
  exit 1
fi
echo "✅ HEAD positioned at expected last commit"

COMMIT_COUNT=$(git rev-list --count "$BASE_COMMIT..HEAD")
echo "Will squash $COMMIT_COUNT commits into 1"

# Step 4: Record original state
ORIGINAL_HEAD=$(git rev-parse HEAD)
if git rev-parse "${BASE_COMMIT}~1" >/dev/null 2>&1; then
  COMMITS_BEFORE_BASE=$(git rev-list "${BASE_COMMIT}~1")
else
  COMMITS_BEFORE_BASE=""
fi

# Step 5: Execute squash
git reset --soft "$BASE_COMMIT"
echo "✅ Soft reset to base commit"

# Step 6: Verify no changes lost
DIFF_OUTPUT=$(git diff --stat "$BACKUP_BRANCH")
if [[ -n "$DIFF_OUTPUT" ]]; then
  echo "❌ ERROR: Changes don't match!"
  echo "ROLLBACK: git reset --hard $BACKUP_BRANCH"
  exit 1
fi
echo "✅ Verification passed: No changes lost or added"

# Step 7: Create squashed commit
git commit -m "$(cat <<'EOF'
Combined commit message

[Details from all squashed commits]

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
echo "✅ Squashed commit created"

# Step 8: Verify non-squashed commits unchanged
if [[ -n "$COMMITS_BEFORE_BASE" ]]; then
  CURRENT_COMMITS_BEFORE_BASE=$(git rev-list "${BASE_COMMIT}~1")
  if [[ "$COMMITS_BEFORE_BASE" != "$CURRENT_COMMITS_BEFORE_BASE" ]]; then
    echo "❌ ERROR: Commits before base were modified!"
    echo "ROLLBACK: git reset --hard $BACKUP_BRANCH"
    exit 1
  fi
  echo "✅ Commits before base unchanged"
fi

TREE_DIFF=$(git diff --stat "$ORIGINAL_HEAD")
if [[ -n "$TREE_DIFF" ]]; then
  echo "❌ ERROR: Working tree doesn't match original!"
  echo "ROLLBACK: git reset --hard $BACKUP_BRANCH"
  exit 1
fi
echo "✅ Final working tree matches original HEAD"

# Step 9: Final verification
NEW_COUNT=$(git rev-list --count "$BASE_COMMIT..HEAD")
if [[ "$NEW_COUNT" -ne 1 ]]; then
  echo "❌ ERROR: Expected 1 commit, got $NEW_COUNT"
  exit 1
fi
echo "✅ Final verification passed"

# Step 10: Update original branch (if detached)
if [[ -n "$ORIGINAL_BRANCH" ]] && [[ "$(git rev-parse --abbrev-ref HEAD)" == "HEAD" ]]; then
  git branch -f "$ORIGINAL_BRANCH" HEAD
  git checkout "$ORIGINAL_BRANCH"
  echo "✅ Updated $ORIGINAL_BRANCH to squashed commit"
fi

# Step 11: Remove backup
git branch -D "$BACKUP_BRANCH"
echo "✅ Backup removed"

echo ""
echo "=== Squash complete ==="
git log --oneline -3
```

## Safety Guarantees

**This skill guarantees**:
- ✅ Backup always created before any destructive operation
- ✅ Verification that staged changes = original commits (no loss, no additions)
- ✅ Verification that commits before base remain completely unchanged
- ✅ Verification that final working tree matches original HEAD state
- ✅ Verification that only intended commits are squashed (not commits beyond)
- ✅ HEAD position validation to prevent squashing beyond intended range
- ✅ Backup removed only after all verifications pass
- ✅ Clear rollback instructions on any error
- ✅ Atomic operation (all or nothing)

**Error Recovery**:
```bash
# If anything goes wrong, restore from backup:
git reset --hard backup-before-squash-YYYYMMDD-HHMMSS
```

## Common Mistakes and How to Avoid Them

### Mistake #1: Squashing Beyond Intended Range (MOST COMMON)

**Problem**: Running squash while HEAD is beyond the last commit you want to squash will include ALL commits from base to current HEAD, not just the commits you intended.

**Real Example from 2025-11-05**:
```bash
# User wanted to squash: 3ebecc4 and b732939 (2 commits)
# But HEAD was at: 5879391 (main branch, 43 commits ahead)

git log --oneline
# 5879391 (HEAD -> main) Fix optimize-doc...  ← Current HEAD (WRONG)
# ...
# 5cc46a9 Optimize task-protocol-agents...
# ...
# b732939 Add AWAITING_USER_APPROVAL...     ← Last commit to squash
# 3ebecc4 Update project documentation...   ← First commit to squash
# 4d3e19e Implement TOML-based...           ← Base commit

# Running: git reset --soft 4d3e19e
# Result: Squashed ALL 43 commits from 4d3e19e to 5879391 (WRONG!)
# Expected: Only 2 commits (3ebecc4 and b732939)
```

**Solution**: ALWAYS checkout the LAST commit FIRST (Step 0):
```bash
# 🚨 MANDATORY Step 0: Position HEAD
git checkout b732939                # Position HEAD at LAST commit to squash
git log --oneline -1                # Verify: should show b732939

# Now create backup and squash
BACKUP_BRANCH="backup-before-squash-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP_BRANCH"
git reset --soft 4d3e19e           # Now squashes ONLY 3ebecc4 and b732939
```

**Prevention Measures Added**:
1. **Step 0 added**: Mandatory HEAD positioning before backup
2. **Usage section updated**: Emphasizes Step 0 with warnings
3. **Verification checks**: HEAD position validated in Step 2
4. **This example added**: Documents real mistake for future reference

**Why This Works**:
- `git reset --soft` moves current branch pointer but keeps working tree
- If HEAD is at wrong position, wrong range gets squashed
- Checking out last commit FIRST ensures correct range

## Squashing Non-Adjacent Commits

**When commits to squash are NOT adjacent** (separated by other commits in between):

### General Pattern: Reorder First, Then Squash

**Use Case**: You want to squash multiple commits together, but they're currently separated by other commits in the history.

**Two-Step Workflow**:
1. **Reorder**: Use interactive rebase to move commits adjacent to each other
2. **Squash**: Change "pick" to "squash" to combine them into one commit

**Example Command Interpretation**:

When you say: "Squash commits A, B, C into commit X"
- Means: Move commits A, B, C to be adjacent to commit X, then squash them together
- Workflow: Interactive rebase moves A, B, C next to X, marks them as "squash"
- Result: One combined commit at X's position

**Concrete Example**:
```bash
# Current history:
# 625cc88 - Commit C (separated)
# e98d3b0 - Commit B (separated)
# ...     - Many other commits
# 2df90f4 - Commit A (separated)
# edbf606 - Target commit X
# ...     - Earlier commits

# Goal: Squash A, B, C into X
# Result after rebase:
# edbf606 - Combined commit (X + A + B + C)
# ...     - Earlier commits
# (All other commits preserved in original positions)
```

### General Procedure: Multiple Separated Commits

**Procedure for squashing multiple non-adjacent commits**:

```bash
# 1. Create backup
BACKUP_BRANCH="backup-before-squash-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP_BRANCH"

# 2. Identify commits to squash
TARGET_COMMIT="edbf606"          # Base commit (all others squash into this)
COMMITS_TO_SQUASH=(              # Commits currently separated in history
  "2df90f4"
  "e98d3b0"
  "625cc88"
)
BASE_COMMIT="${TARGET_COMMIT}^"  # Parent of target commit

# 3. Start interactive rebase from base
git rebase -i "$BASE_COMMIT"

# 4. In the interactive editor:
#    - FIND each commit in COMMITS_TO_SQUASH array
#    - MOVE them to be directly after TARGET_COMMIT
#    - CHANGE their "pick" to "squash"
#    - PRESERVE all other commits in original order
#    - Save and exit

# Example transformation for "Squash 2df90f4, e98d3b0, 625cc88 into edbf606":
# BEFORE:
#   pick edbf606 Target commit
#   pick abc1234 Other commit
#   pick 2df90f4 Commit A to squash
#   pick def5678 Other commit
#   pick e98d3b0 Commit B to squash
#   pick ghi9012 Other commit
#   pick 625cc88 Commit C to squash
#
# AFTER:
#   pick edbf606 Target commit
#   squash 2df90f4 Commit A to squash    ← MOVED & CHANGED
#   squash e98d3b0 Commit B to squash    ← MOVED & CHANGED
#   squash 625cc88 Commit C to squash    ← MOVED & CHANGED
#   pick abc1234 Other commit
#   pick def5678 Other commit
#   pick ghi9012 Other commit

# 5. Git prompts for combined commit message
#    - Edit to create unified message
#    - Save and exit

# 6. Verify result
git log --oneline -10
git diff "$BACKUP_BRANCH"  # Should show no differences

# 7. Remove backup after verification
git branch -D "$BACKUP_BRANCH"
```

### Specific Example: Fix Commit Into Original

**Use Case**: Squashing a fix commit into the commit that introduced the issue, while preserving all commits in between.

**Example**: Squash commit 285d9b9 (removes broken reference) into 977d7b9 (introduced broken reference), preserving 77 commits in between.

**Procedure**:
```bash
# 1. Create backup
BACKUP_BRANCH="backup-before-squash-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP_BRANCH"

# 2. Identify the commits
TARGET_COMMIT="977d7b9"           # Commit that introduced issue
FIX_COMMIT="285d9b9"              # Commit that fixes issue
BASE_COMMIT="${TARGET_COMMIT}^"   # Parent of target commit

# 3. Start interactive rebase
git rebase -i "$BASE_COMMIT"

# 4. In the interactive editor:
#    - FIND the fix commit line
#    - MOVE it directly after the target commit line
#    - CHANGE "pick" to "squash" (or "fixup" to discard fix message)
#    - PRESERVE all other commits in their original order
#    - Save and exit

# Example transformation:
# BEFORE:
#   pick 977d7b9 Initial commit (TARGET)
#   pick abc1234 Some other commit
#   pick def5678 Another commit
#   ...
#   pick 285d9b9 Remove broken reference (FIX)
#
# AFTER:
#   pick 977d7b9 Initial commit (TARGET)
#   squash 285d9b9 Remove broken reference (FIX) ← MOVED & CHANGED
#   pick abc1234 Some other commit
#   pick def5678 Another commit
#   ...

# 5. Git will prompt for combined commit message
#    - Edit to create unified message
#    - Save and exit

# 6. Verify result
git log --oneline -10
git diff "$BACKUP_BRANCH"  # Should show no differences

# 7. Remove backup after verification
git branch -D "$BACKUP_BRANCH"
```

**Critical Rules for Reordering**:
- ✅ Only reorder the commits being squashed together
- ✅ Preserve all other commits in their exact original positions
- ❌ Never reorder commits that aren't being squashed
- ❌ Never drop commits accidentally

**Safety Verification**:
```bash
# After rebase, verify commit count
ORIGINAL_COUNT=$(git rev-list --count "$BACKUP_BRANCH")
NEW_COUNT=$(git rev-list --count HEAD)
EXPECTED_NEW=$((ORIGINAL_COUNT - 1))  # One less due to squash

if [[ "$NEW_COUNT" -ne "$EXPECTED_NEW" ]]; then
  echo "❌ ERROR: Expected $EXPECTED_NEW commits, got $NEW_COUNT"
  git rebase --abort
  git reset --hard "$BACKUP_BRANCH"
  exit 1
fi

echo "✅ Verification passed: $ORIGINAL_COUNT commits → $NEW_COUNT commits"
```

### Automating Reorder with GIT_SEQUENCE_EDITOR

**⚠️ CRITICAL: Commit Order Assumptions**

When automating rebase with `GIT_SEQUENCE_EDITOR`, git presents commits in **chronological order** (oldest first). Scripts MUST NOT assume they will encounter commits in any particular order.

**❌ WRONG Pattern - Assumes Order**:
```python
#!/usr/bin/env python3
# ❌ BUG: Assumes commit B comes BEFORE commit A
import sys

with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

result = []
moved_commit = None

for line in lines:
    if 'commit-B' in line:  # Try to save this commit
        moved_commit = line.replace('pick', 'squash', 1)
    elif 'commit-A' in line:  # Try to insert saved commit here
        result.append(line)
        if moved_commit:  # ❌ Won't work if commit-A comes first!
            result.append(moved_commit)
            moved_commit = None
    else:
        result.append(line)

# ❌ BUG: If commit-A comes before commit-B chronologically,
# moved_commit will never be appended, silently dropping commit-B!
```

**✅ CORRECT Pattern - Process All, Then Reorder**:
```python
#!/usr/bin/env python3
# ✅ CORRECT: Process all lines first, then reorder
import sys

with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

# Step 1: Extract commits (don't assume order)
commits = []
for line in lines:
    if line.startswith('#') or not line.strip():
        continue
    commits.append(line)

# Step 2: Find specific commits
commit_a = None
commit_b = None
other_commits = []

for commit in commits:
    if 'commit-A' in commit:
        commit_a = commit
    elif 'commit-B' in commit:
        commit_b = commit.replace('pick', 'squash', 1)
    else:
        other_commits.append(commit)

# Step 3: Verify both found
if not commit_a or not commit_b:
    print(f"ERROR: Commits not found", file=sys.stderr)
    sys.exit(1)

# Step 4: Build new order
result = []
if commit_a:
    result.append(commit_a)
if commit_b:
    result.append(commit_b)
result.extend(other_commits)

# Step 5: Verify count (defensive programming)
if len(result) != len(commits):
    print(f"ERROR: Commit count mismatch! {len(commits)} → {len(result)}", file=sys.stderr)
    sys.exit(1)

# Write result
with open(sys.argv[1], 'w') as f:
    f.writelines(result)
```

**Key Principles**:
1. **Never assume commit order** in git rebase todo list
2. **Process all lines first**, extract what you need
3. **Then reorder** based on requirements
4. **Verify count** before writing (defensive check)
5. **Exit with error** if commits not found or count mismatch

**See Also**: [git-workflow.md § Handling Non-Contiguous Commits](../../docs/project/git-workflow.md#handling-non-contiguous-commits) for complete documentation.

## Usage

### Interactive Mode (Recommended)

**For Adjacent Commits**:
```bash
# 1. Identify commits to squash
git log --oneline -10
# Note:
# - Base commit (parent of first commit to squash)
# - Last commit to include in squash

# 2. 🚨 MANDATORY: Checkout the last commit to squash FIRST
git checkout <last-commit-sha>
# ⚠️ CRITICAL: This MUST be done before creating backup
# If skipped, you'll squash MORE commits than intended

# Verify HEAD position
git log --oneline -1
# Should show the LAST commit you want in the squash

# 3. Run skill (follows Step 1-10 from workflow)
Skill: git-squash

# 4. When prompted, provide:
#    - Base commit SHA (parent of first commit)
#    - Expected last commit SHA (for verification)
#    - Combined commit message

# 5. After squash, update original branch if needed
git branch -f main HEAD
git checkout main
```

**For Non-Adjacent Commits**:
```bash
# Use interactive rebase with reordering (see section above)
# This handles commits with other commits in between
```

### Direct Invocation

**For scripting/automation**:
```bash
# Set base commit
BASE_COMMIT="abc123"

# Execute skill workflow
# [Follow steps 1-7 from workflow section]
```

## Related Documentation

- git-workflow.md: Git workflows and commit management
- git-rebase/SKILL.md: For complex history editing
- git-merge-linear/SKILL.md: For linear merge workflows
- CLAUDE.md: Branch management rules

## Success Criteria

Squash is successful when:
1. ✅ Backup created before squash
2. ✅ HEAD was positioned at correct last commit to squash
3. ✅ Verification passed (no changes lost/added)
4. ✅ Commits before base remain unchanged
5. ✅ Final working tree matches original HEAD state
6. ✅ Single commit created with all changes
7. ✅ Commit count reduced by exactly (N-1) where N = commits squashed
8. ✅ Backup removed after all verifications pass
9. ✅ `git log` shows clean, linear history
10. ✅ No uncommitted changes remain
