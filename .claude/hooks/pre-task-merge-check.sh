#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in pre-task-merge-check.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Pre-Task Merge Validation Hook
# Purpose: Verify task branch has exactly 1 commit before merging to main
# Triggered: Before git merge operations in CLEANUP state

TASK_BRANCH="${1:-}"
BASE_BRANCH="${2:-main}"

if [[ -z "$TASK_BRANCH" ]]; then
    echo "ERROR: Task branch not specified" >&2
    echo "Usage: pre-task-merge-check.sh <task-branch> [base-branch]" >&2
    exit 1
fi

# Change to main repository directory
cd /workspace/main

# Verify we're in a git repository
if ! git rev-parse --git-dir >/dev/null 2>&1; then
    echo "ERROR: Not in a git repository" >&2
    exit 1
fi

# Verify task branch exists
if ! git rev-parse --verify "$TASK_BRANCH" >/dev/null 2>&1; then
    echo "ERROR: Task branch '$TASK_BRANCH' does not exist" >&2
    exit 1
fi

# Verify base branch exists
if ! git rev-parse --verify "$BASE_BRANCH" >/dev/null 2>&1; then
    echo "ERROR: Base branch '$BASE_BRANCH' does not exist" >&2
    exit 1
fi

# Count commits in task branch that are not in base branch
COMMIT_COUNT=$(git rev-list --count "$BASE_BRANCH..$TASK_BRANCH")

echo "Validating task branch: $TASK_BRANCH"
echo "Commits ahead of $BASE_BRANCH: $COMMIT_COUNT"

if [[ "$COMMIT_COUNT" -eq 0 ]]; then
    echo "WARNING: Task branch has no new commits" >&2
    exit 1
elif [[ "$COMMIT_COUNT" -eq 1 ]]; then
    echo "✅ PASS: Task branch has exactly 1 commit (squashed correctly)"
    exit 0
else
    echo "" >&2
    echo "❌ FAIL: Task branch has $COMMIT_COUNT commits (expected 1)" >&2
    echo "" >&2
    echo "REQUIRED ACTION: Squash commits before merge" >&2
    echo "" >&2
    echo "Run the following commands:" >&2
    echo "  cd /workspace/main" >&2
    echo "  git checkout $TASK_BRANCH" >&2
    echo "  git reset --soft $BASE_BRANCH" >&2
    echo "  git commit -m \"<combined-commit-message>\"" >&2
    echo "" >&2
    echo "See docs/project/git-workflow.md § Squash and Merge Protocol" >&2
    exit 1
fi
