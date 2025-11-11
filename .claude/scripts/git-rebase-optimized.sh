#!/bin/bash
set -euo pipefail

# git-rebase-optimized.sh - Atomic interactive rebase with comprehensive safety
#
# This script performs interactive rebase operations in a single atomic execution,
# reducing LLM round-trips from 8+ to 2-3 while preserving all safety checks.
#
# Usage:
#   git-rebase-optimized.sh <base_commit> <todo_file> [target_branch]
#
# Parameters:
#   base_commit    - Commit to rebase onto (e.g., "abc123" or "main")
#   todo_file      - File containing rebase todo list (pick/squash/reword lines)
#   target_branch  - Optional branch to update after rebase (default: current)

# ============================================================================
# CONFIGURATION
# ============================================================================

BASE_COMMIT="${1:?ERROR: BASE_COMMIT required (commit to rebase onto)}"
TODO_FILE="${2:?ERROR: TODO_FILE required (path to rebase todo file)}"
TARGET_BRANCH="${3:-}"

# Verify todo file exists
if [[ ! -f "$TODO_FILE" ]]; then
  echo "ERROR: Todo file not found: $TODO_FILE" >&2
  exit 1
fi

# ============================================================================
# EXECUTION RESULT TRACKING
# ============================================================================

RESULT_JSON="/tmp/git-rebase-result-$$.json"
START_TIME=$(date +%s)

# Function to output structured JSON result
output_result() {
  local status="$1"
  local message="$2"
  local end_time=$(date +%s)
  local duration=$((end_time - START_TIME))

  cat > "$RESULT_JSON" <<EOF
{
  "status": "$status",
  "message": "$message",
  "duration_seconds": $duration,
  "backup_branch": "${BACKUP_BRANCH:-none}",
  "base_commit": "$BASE_COMMIT",
  "original_head": "${ORIGINAL_HEAD:-none}",
  "final_head": "$(git rev-parse HEAD 2>/dev/null || echo 'none')",
  "working_directory": "$(pwd)",
  "timestamp": "$(date -Iseconds)"
}
EOF

  cat "$RESULT_JSON"
  [[ "$status" == "success" ]] && exit 0 || exit 1
}

# ============================================================================
# STEP 1: CREATE BACKUP
# ============================================================================

echo "Step 1: Creating backup branch..."
BACKUP_BRANCH="backup-before-rebase-$(date +%Y%m%d-%H%M%S)"

if ! git branch "$BACKUP_BRANCH"; then
  output_result "error" "Failed to create backup branch"
fi

if ! git rev-parse --verify "$BACKUP_BRANCH" >/dev/null 2>&1; then
  output_result "error" "Backup branch verification failed"
fi

echo "✅ Backup created: $BACKUP_BRANCH"
echo "   Restore command: git reset --hard $BACKUP_BRANCH"

# ============================================================================
# STEP 2: ANALYZE CURRENT STATE
# ============================================================================

echo ""
echo "Step 2: Analyzing current state..."

# Check working directory clean
if [[ -n "$(git status --porcelain)" ]]; then
  output_result "error" "Working directory not clean - commit or stash changes first"
fi
echo "✅ Working directory clean"

# Record original state
ORIGINAL_HEAD=$(git rev-parse HEAD)
ORIGINAL_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Verify base commit exists
if ! git rev-parse --verify "$BASE_COMMIT" >/dev/null 2>&1; then
  output_result "error" "Base commit $BASE_COMMIT does not exist"
fi

# Count commits to be rebased
COMMIT_COUNT=$(git rev-list --count "$BASE_COMMIT..HEAD")
echo "Commits to rebase: $COMMIT_COUNT"

if [[ "$COMMIT_COUNT" -eq 0 ]]; then
  output_result "error" "No commits to rebase - HEAD is at or behind base commit"
fi

# Show commits
echo ""
echo "Commits that will be rebased:"
git log --oneline "$BASE_COMMIT..HEAD"
echo ""

# Validate todo file
TODO_LINE_COUNT=$(grep -c "^pick\|^squash\|^reword\|^edit\|^fixup\|^drop" "$TODO_FILE" || echo "0")
if [[ "$TODO_LINE_COUNT" -eq 0 ]]; then
  output_result "error" "Todo file contains no valid rebase commands (pick/squash/reword/edit/fixup/drop)"
fi

echo "Todo file contains $TODO_LINE_COUNT rebase commands"
echo "✅ Prerequisites verified"

# ============================================================================
# STEP 3: EXECUTE INTERACTIVE REBASE
# ============================================================================

echo ""
echo "Step 3: Executing interactive rebase..."
echo "Using todo file: $TODO_FILE"
echo ""

# Use GIT_SEQUENCE_EDITOR to inject our pre-prepared todo file
if ! GIT_SEQUENCE_EDITOR="cp $TODO_FILE" git rebase -i "$BASE_COMMIT"; then
  echo "" >&2
  echo "❌ ERROR: Rebase failed or encountered conflicts!" >&2
  echo "" >&2
  echo "MANUAL RESOLUTION REQUIRED:" >&2
  echo "1. Check status: git status" >&2
  echo "2. If conflicts, resolve them:" >&2
  echo "   - Edit conflicted files" >&2
  echo "   - Stage resolved files: git add <files>" >&2
  echo "   - Continue rebase: git rebase --continue" >&2
  echo "3. Or abort: git rebase --abort && git reset --hard $BACKUP_BRANCH" >&2
  echo "" >&2
  echo "Backup available: $BACKUP_BRANCH" >&2
  output_result "error" "Rebase encountered conflicts - manual resolution required"
fi

FINAL_HEAD=$(git rev-parse HEAD)
echo "✅ Rebase completed successfully"
echo "   New HEAD: $FINAL_HEAD"

# ============================================================================
# STEP 4: VALIDATE REBASE SUCCESS
# ============================================================================

echo ""
echo "Step 4: Validating rebase result..."

# Verify HEAD moved (rebase changed something)
if [[ "$ORIGINAL_HEAD" == "$FINAL_HEAD" ]]; then
  echo "⚠️  Warning: HEAD unchanged after rebase (may be no-op)" >&2
fi

# Show new history
echo ""
echo "Rebased history (last 5 commits):"
git log --oneline --graph -5

echo ""
echo "✅ Rebase validation complete"

# ============================================================================
# STEP 5: UPDATE TARGET BRANCH
# ============================================================================

echo ""
echo "Step 5: Updating branches..."

# If we're in detached HEAD state, update the target branch
CURRENT_STATE=$(git rev-parse --abbrev-ref HEAD)
if [[ "$CURRENT_STATE" == "HEAD" ]]; then
  if [[ -n "$TARGET_BRANCH" ]]; then
    echo "Detached HEAD detected, updating target branch: $TARGET_BRANCH"
    git branch -f "$TARGET_BRANCH" HEAD
    git checkout "$TARGET_BRANCH"
    echo "✅ Updated and checked out $TARGET_BRANCH"
  elif [[ "$ORIGINAL_BRANCH" != "HEAD" ]]; then
    echo "Detached HEAD detected, updating original branch: $ORIGINAL_BRANCH"
    git branch -f "$ORIGINAL_BRANCH" HEAD
    git checkout "$ORIGINAL_BRANCH"
    echo "✅ Updated and checked out $ORIGINAL_BRANCH"
  else
    echo "⚠️  Remaining in detached HEAD state"
    echo "   To update a branch: git branch -f <branch> HEAD && git checkout <branch>"
  fi
else
  echo "✅ Already on branch: $CURRENT_STATE"
fi

# ============================================================================
# STEP 6: CLEANUP BACKUP
# ============================================================================

echo ""
echo "Step 6: Removing backup..."

if git branch -D "$BACKUP_BRANCH" 2>&1; then
  echo "✅ Backup removed after successful verification"
else
  echo "⚠️  Warning: Failed to delete backup branch $BACKUP_BRANCH" >&2
  echo "   You can manually delete it with: git branch -D $BACKUP_BRANCH" >&2
fi

# ============================================================================
# SUCCESS
# ============================================================================

COMMITS_AFTER=$(git rev-list --count "$BASE_COMMIT..HEAD")
echo ""
echo "════════════════════════════════════════"
echo "Rebase Summary:"
echo "  Base commit:   $(git log --oneline -1 "$BASE_COMMIT")"
echo "  Commits before: $COMMIT_COUNT"
echo "  Commits after:  $COMMITS_AFTER"
echo "  Original HEAD: $ORIGINAL_HEAD"
echo "  Final HEAD:    $FINAL_HEAD"
echo "════════════════════════════════════════"

output_result "success" "Rebase completed successfully: $COMMIT_COUNT commits rebased"
