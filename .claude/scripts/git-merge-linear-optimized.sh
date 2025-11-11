#!/bin/bash
set -euo pipefail

# git-merge-linear-optimized.sh - Atomic linear merge with verification
#
# This script performs fast-forward merge with comprehensive validation,
# reducing LLM round-trips from 6+ to 2-3 while preserving all safety checks.
#
# Usage:
#   git-merge-linear-optimized.sh <task_branch> [--cleanup]
#
# Parameters:
#   task_branch  - Name of task branch to merge (e.g., "implement-feature")
#   --cleanup    - Optional flag to delete branch and worktree after merge

# ============================================================================
# CONFIGURATION
# ============================================================================

TASK_BRANCH="${1:?ERROR: TASK_BRANCH required (branch to merge to main)}"
CLEANUP_MODE="${2:-}"

# ============================================================================
# EXECUTION RESULT TRACKING
# ============================================================================

RESULT_JSON="/tmp/git-merge-linear-result-$$.json"
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
  "task_branch": "$TASK_BRANCH",
  "merged_commit": "${MERGED_COMMIT:-none}",
  "working_directory": "$(pwd)",
  "cleanup_performed": ${CLEANUP_PERFORMED:-false},
  "timestamp": "$(date -Iseconds)"
}
EOF

  cat "$RESULT_JSON"
  [[ "$status" == "success" ]] && exit 0 || exit 1
}

# ============================================================================
# STEP 1: VALIDATION
# ============================================================================

echo "Step 1: Validating prerequisites..."

# Verify we're on main branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$CURRENT_BRANCH" != "main" ]]; then
  output_result "error" "Must be on main branch (currently on: $CURRENT_BRANCH)"
fi
echo "✅ On main branch"

# Verify task branch exists
if ! git rev-parse --verify "$TASK_BRANCH" >/dev/null 2>&1; then
  output_result "error" "Task branch '$TASK_BRANCH' not found"
fi
echo "✅ Task branch exists"

# Check working directory clean
if [[ -n "$(git status --porcelain)" ]]; then
  output_result "error" "Working directory not clean - commit or stash changes first"
fi
echo "✅ Working directory clean"

# Count commits on task branch
COMMIT_COUNT=$(git rev-list --count main.."$TASK_BRANCH")
echo "Task branch has $COMMIT_COUNT commit(s)"

if [[ "$COMMIT_COUNT" -ne 1 ]]; then
  output_result "error" "Task branch must have exactly 1 commit (found: $COMMIT_COUNT). Squash commits first."
fi
echo "✅ Task branch has exactly 1 commit"

# Show the commit to be merged
echo ""
echo "Commit to merge:"
git log --oneline -1 "$TASK_BRANCH"
echo ""

# ============================================================================
# STEP 2: FAST-FORWARD MERGE
# ============================================================================

echo "Step 2: Executing fast-forward merge..."

if ! git merge --ff-only "$TASK_BRANCH" 2>&1; then
  echo "" >&2
  echo "ERROR: Fast-forward merge failed!" >&2
  echo "This usually means main has moved ahead since task branch was created." >&2
  echo "" >&2
  echo "SOLUTION: Rebase task branch onto latest main:" >&2
  echo "  git merge --abort" >&2
  echo "  git checkout $TASK_BRANCH" >&2
  echo "  git rebase main" >&2
  echo "  git checkout main" >&2
  echo "  git merge --ff-only $TASK_BRANCH" >&2
  output_result "error" "Fast-forward merge failed - main has diverged"
fi

MERGED_COMMIT=$(git rev-parse HEAD)
echo "✅ Linear merge successful: $MERGED_COMMIT"

# ============================================================================
# STEP 3: VERIFICATION
# ============================================================================

echo ""
echo "Step 3: Verifying linear history..."

# Verify the task commit is now on main
LATEST_COMMIT=$(git log -1 --format=%s)
echo "Latest commit on main: $LATEST_COMMIT"

# Confirm no merge commit created (linear history preserved)
if git log -1 --format=%p | grep -q " "; then
  output_result "error" "Merge commit detected - history is not linear (should not happen with --ff-only)"
fi
echo "✅ Linear history verified"

# Show result
echo ""
echo "History (last 5 commits):"
git log --oneline --graph -5

# ============================================================================
# STEP 4: CLEANUP (OPTIONAL)
# ============================================================================

CLEANUP_PERFORMED=false

if [[ "$CLEANUP_MODE" == "--cleanup" ]]; then
  echo ""
  echo "Step 4: Cleaning up task branch..."

  # Delete task branch
  if git branch -d "$TASK_BRANCH" 2>&1; then
    echo "✅ Task branch deleted: $TASK_BRANCH"
    CLEANUP_PERFORMED=true
  else
    echo "⚠️  Warning: Failed to delete task branch $TASK_BRANCH" >&2
    echo "   You can manually delete it with: git branch -D $TASK_BRANCH" >&2
  fi

  # If task worktree exists, remove it
  TASK_WORKTREE="/workspace/tasks/$TASK_BRANCH/code"
  if [[ -d "$TASK_WORKTREE" ]]; then
    if git worktree remove "$TASK_WORKTREE" 2>&1; then
      echo "✅ Task worktree removed: $TASK_WORKTREE"
    else
      echo "⚠️  Warning: Failed to remove worktree $TASK_WORKTREE" >&2
    fi

    # Remove task directory if empty
    TASK_DIR="/workspace/tasks/$TASK_BRANCH"
    if [[ -d "$TASK_DIR" ]]; then
      if rmdir "$TASK_DIR" 2>/dev/null; then
        echo "✅ Task directory removed: $TASK_DIR"
      else
        echo "⚠️  Task directory not empty: $TASK_DIR (manual cleanup required)" >&2
      fi
    fi
  fi
else
  echo ""
  echo "Step 4: Skipping cleanup (use --cleanup flag to auto-delete branch)"
  echo "   To manually cleanup later:"
  echo "   git branch -d $TASK_BRANCH"
  echo "   git worktree remove /workspace/tasks/$TASK_BRANCH/code"
fi

# ============================================================================
# SUCCESS
# ============================================================================

output_result "success" "Linear merge completed successfully: $TASK_BRANCH → main"
