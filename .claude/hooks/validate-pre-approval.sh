#!/bin/bash
# Pre-approval validation - ensures cleanup completed before approval gate
# Called by main agent before presenting AskUserQuestion for merge approval

set -euo pipefail

TASK_BRANCH="${1:-}"

if [[ -z "$TASK_BRANCH" ]]; then
    echo "Usage: validate-pre-approval.sh <task-branch>"
    exit 1
fi

# Check for orphaned subagent branches
SUBAGENT_BRANCHES=$(git branch --list "${TASK_BRANCH}-sub-*" 2>/dev/null || true)
if [[ -n "$SUBAGENT_BRANCHES" ]]; then
    echo "ERROR: Subagent branches still exist (must cleanup BEFORE approval gate):"
    echo "$SUBAGENT_BRANCHES"
    echo ""
    echo "Run these commands to cleanup:"
    for branch in $SUBAGENT_BRANCHES; do
        echo "  git worktree remove .worktrees/$branch --force 2>/dev/null || true"
        echo "  git branch -d $branch"
    done
    exit 1
fi

# Check for orphaned subagent worktrees
SUBAGENT_WORKTREES=$(git worktree list | grep "${TASK_BRANCH}-sub-" || true)
if [[ -n "$SUBAGENT_WORKTREES" ]]; then
    echo "ERROR: Subagent worktrees still exist (must cleanup BEFORE approval gate):"
    echo "$SUBAGENT_WORKTREES"
    exit 1
fi

echo "✓ Pre-approval validation passed: no orphaned subagent resources"
exit 0
