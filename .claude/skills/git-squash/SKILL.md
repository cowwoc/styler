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

## ⚠️ Critical Safety Rules

**MANDATORY BACKUP**: Always create timestamped backup before squashing
**VERIFICATION REQUIRED**: Verify no changes lost or added beyond original commits
**AUTOMATIC CLEANUP**: Remove backup only after verification passes
**WORKING DIRECTORY**: Must be clean (no uncommitted changes)

## Prerequisites

Before using this skill, verify:
- [ ] Working directory is clean: `git status` shows no uncommitted changes
- [ ] Know base commit (where to squash back to)
- [ ] Know which commits to squash
- [ ] Current branch is correct

## Skill Workflow

### Step 1: Create Mandatory Backup

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

### Step 2: Verify Preconditions

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
# Show commits to be squashed
BASE_COMMIT="<commit-sha>"  # The commit to squash back to
git log --oneline "$BASE_COMMIT..HEAD"

# Count commits
COMMIT_COUNT=$(git rev-list --count "$BASE_COMMIT..HEAD")
echo "Will squash $COMMIT_COUNT commits"
```

### Step 3: Execute Squash

**Soft Reset to Base**:
```bash
# Reset to base commit, keeping all changes staged
git reset --soft "$BASE_COMMIT"

# Verify changes are staged
git status --short
```

### Step 4: Verify No Changes Lost or Added

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

### Step 5: Create Squashed Commit

**Commit with Combined Message**:
```bash
# Create new commit with all squashed changes
git commit -m "$(cat <<'EOF'
<Combined commit message>

[Include relevant details from all squashed commits]

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"

echo "✅ Squashed commit created"
```

### Step 6: Final Verification

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

### Step 7: Remove Backup

**⚠️ Only After Verification Passes**:
```bash
# Delete backup branch (verification passed)
git branch -D "$BACKUP_BRANCH"
echo "✅ Backup removed after successful verification"
```

## Complete Example

### Example: Squash 6 Commits

```bash
#!/bin/bash
set -euo pipefail

# Configuration
BASE_COMMIT="60356be"  # Commit to squash back to
COMMITS_TO_SQUASH=6

echo "=== Git Squash: $COMMITS_TO_SQUASH commits → 1 commit ==="
echo ""

# Step 1: Create backup
BACKUP_BRANCH="backup-before-squash-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP_BRANCH"
echo "✅ Backup created: $BACKUP_BRANCH"

# Step 2: Verify preconditions
if [[ -n "$(git status --porcelain)" ]]; then
  echo "❌ ERROR: Working directory not clean"
  exit 1
fi
echo "✅ Working directory clean"

# Step 3: Execute squash
git reset --soft "$BASE_COMMIT"
echo "✅ Soft reset to base commit"

# Step 4: Verify no changes lost
DIFF_OUTPUT=$(git diff --stat "$BACKUP_BRANCH")
if [[ -n "$DIFF_OUTPUT" ]]; then
  echo "❌ ERROR: Changes don't match!"
  echo "ROLLBACK: git reset --hard $BACKUP_BRANCH"
  exit 1
fi
echo "✅ Verification passed: No changes lost or added"

# Step 5: Create squashed commit
git commit -m "$(cat <<'EOF'
Combined commit message

[Details from all squashed commits]

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
echo "✅ Squashed commit created"

# Step 6: Final verification
NEW_COUNT=$(git rev-list --count "$BASE_COMMIT..HEAD")
if [[ "$NEW_COUNT" -ne 1 ]]; then
  echo "❌ ERROR: Expected 1 commit, got $NEW_COUNT"
  exit 1
fi
echo "✅ Final verification passed"

# Step 7: Remove backup
git branch -D "$BACKUP_BRANCH"
echo "✅ Backup removed"

echo ""
echo "=== Squash complete ===$()"
git log --oneline -3
```

## Safety Guarantees

**This skill guarantees**:
- ✅ Backup always created before any destructive operation
- ✅ Verification that staged changes = original commits (no loss, no additions)
- ✅ Backup removed only after verification passes
- ✅ Clear rollback instructions on any error
- ✅ Atomic operation (all or nothing)

**Error Recovery**:
```bash
# If anything goes wrong, restore from backup:
git reset --hard backup-before-squash-YYYYMMDD-HHMMSS
```

## Usage

### Interactive Mode (Recommended)

**Step-by-step with prompts**:
```bash
# 1. Identify base commit
git log --oneline -10
# Pick the commit to squash back to

# 2. Run skill
Skill: git-squash

# 3. When prompted, provide:
#    - Base commit SHA
#    - Combined commit message
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
2. ✅ Verification passed (no changes lost/added)
3. ✅ Single commit created with all changes
4. ✅ Backup removed after verification
5. ✅ `git log` shows clean, linear history
6. ✅ No uncommitted changes remain
