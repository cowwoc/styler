#!/bin/bash
set -euo pipefail

# Check if this is a merge commit being created
if git rev-parse -q --verify MERGE_HEAD > /dev/null 2>&1; then
    echo "⚠️  WARNING: Creating a merge commit"
    echo ""
    echo "This project uses linear history (--ff-only merges)."
    echo "If main has diverged from your branch, use:"
    echo "  /cat:git-rebase to rebase your branch onto main first"
    echo ""
    echo "Then merge with: git merge --ff-only <branch>"
    echo ""
    # Allow to continue but warn (don't block)
fi
