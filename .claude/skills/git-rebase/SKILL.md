---
name: git-rebase
description: Safely rebase, reorder, or squash commits with automatic backup and verification
allowed-tools: Bash, Read, Edit
---

# Git Rebase Skill

<!-- PATTERN EVOLUTION:
     - 2025-11-02: Fixed hardcoded v21 bug (line 117), enforced backup creation, added inline safety reminders
-->

**Purpose**: Safely perform git rebases to reorder commits, squash history, or update branch base, with automatic backup and validation.

**When to Use**:
- Squashing multiple commits into one before merge
- Reordering commits to improve chronology
- Updating branch to latest main (rebasing onto main)
- Separating interleaved concerns into sequential commits
- Cleaning up commit history before push

## ⚠️ Critical Safety Rules

**MANDATORY BACKUP**: Always create backup branch before rebasing
**BRANCH PRESERVATION**: NEVER delete version branches (v1, v13, v21, etc.) - update them with `git branch -f`
**VALIDATION**: Verify branch patterns before any deletion
**WORKING DIRECTORY**: Must be clean (no uncommitted changes)

## Prerequisites

Before using this skill, verify:
- [ ] Working directory is clean: `git status` shows no uncommitted changes
- [ ] Know which commits to rebase: `git log --oneline -10`
- [ ] Have clear goal: squash/reorder/update base
- [ ] Understand branch purpose (version vs temporary)

## Common Rebase Scenarios

### Scenario 1: Squash All Task Commits

**Goal**: Combine multiple commits on task branch into single commit

**Use Case**: Before merging task branch to main

### Scenario 2: Reorder Commits

**Goal**: Change commit order while preserving history

**Use Case**: Documentation commit should come before implementation

### Scenario 3: Update Branch Base

**Goal**: Rebase feature branch onto latest main

**Use Case**: Main has moved ahead, need to update feature branch

## Skill Workflow

### Step 1: Backup Current State

**⚠️ MANDATORY - Create Safety Backup BEFORE ANY git reset --hard**:
```bash
# CRITICAL: Create timestamped backup FIRST - rebase destroys commits!
# DO NOT skip this step or jump to Step 3
BACKUP_BRANCH="backup-before-rebase-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP_BRANCH"

# Verify backup created
if ! git rev-parse --verify "$BACKUP_BRANCH" >/dev/null 2>&1; then
  echo "❌ ERROR: Failed to create backup - STOP"
  exit 1
fi
echo "✅ Backup created: $BACKUP_BRANCH"
echo "   Restore command if needed: git reset --hard $BACKUP_BRANCH"
```

### Step 2: Analyze Current State

**Understand What You're Rebasing**:
```bash
# Count commits ahead of main
COMMIT_COUNT=$(git rev-list --count main..<branch>)

# List commits with dates
git log --oneline --graph --date=short --format="%h %ad %s" <base>..<branch>
```

### Step 3: Execute Rebase

**Method A: Interactive Rebase**:
```bash
git rebase -i <base-commit>
# Editor opens - change "pick" to "squash" or reorder lines
```

**Method B: Reset and Squash**:
```bash
git reset --soft <base-commit>
git commit -m "Combined commit message"
```

**Method C: Cherry-Pick Reorder**:
```bash
git reset --hard <base-commit>
git cherry-pick <commit-1>  # Pick in desired order
git cherry-pick <commit-2>
```

### Step 4: Validate Rebase Success

```bash
# Verify commit count
git rev-list --count <base>..<branch>

# Check build
./mvnw clean compile test
```

### Step 5: Update Related Branches

**⚠️ CRITICAL: Version Branch Management**

```bash
# SAFETY: Always check if branch is version marker before deleting
BRANCH_NAME="v21"  # Example - replace with actual branch

# Check pattern
if [[ "$BRANCH_NAME" =~ ^v[0-9]+$ ]]; then
  echo "⚠️  VERSION BRANCH - UPDATE, don't delete"
  # BUG FIX (2025-11-02): Use $BRANCH_NAME variable, not hardcoded v21
  git branch -f "$BRANCH_NAME" HEAD
else
  echo "Temporary branch - safe to delete"
  git branch -D "$BRANCH_NAME"
fi
```

### Step 6: Cleanup

```bash
# Delete backup after verification
git branch -D "$BACKUP_BRANCH"

# Garbage collect
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```

## Complete Examples

### Example: Reorder Commits (Docs Before Implementation)

```bash
# Current: base → formatter → docs
# Want: base → docs → formatter

# Step 1: Backup
git branch backup-reorder-$(date +%Y%m%d-%H%M%S)

# Step 2: Reset to base
git reset --hard <base>

# Step 3: Cherry-pick in order
git cherry-pick <docs-commit>
git cherry-pick <formatter-commit>

# Step 4: Update version branch (NOT delete!)
git branch -f v21 HEAD

# Step 5: Validate
git log --oneline --graph -3
```

## Safety Rules Summary

**DO**:
- ✅ Create backup before every rebase
- ✅ Update version branches with `git branch -f`
- ✅ Delete only temporary branches
- ✅ Run tests after rebase

**DON'T**:
- ❌ Rebase without backup
- ❌ Delete version branches (v1, v13, v21, etc.)
- ❌ Skip validation
- ❌ Force push to shared branches

## Related Documentation

- git-workflow.md: Git workflows and squashing
- main-agent-coordination.md: Merge requirements
- CLAUDE.md: Branch management rules
