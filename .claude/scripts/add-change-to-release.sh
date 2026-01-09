#!/bin/bash
# add-change-to-release.sh - Validate and prepare for adding a CHANGE to a release
#
# This script enforces the protocol: if adding tasks to a completed release,
# the release must be re-opened first.
#
# Usage: .claude/scripts/add-change-to-release.sh <release-number>
# Example: .claude/scripts/add-change-to-release.sh 5
#
# Checks:
# 1. Release directory exists
# 2. If release has all CHANGEs with SUMMARYs (complete), warns that it needs re-opening
# 3. Outputs instructions for adding new CHANGE and updating STATE.md/ROADMAP.md

set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <release-number>"
    echo "Example: $0 5"
    exit 1
fi

RELEASE_NUM="$1"
RELEASES_DIR=".planning/releases"
STATE_FILE=".planning/STATE.md"
ROADMAP_FILE=".planning/ROADMAP.md"

# Find release directory (handles both 05-name and 5-name formats)
RELEASE_DIR=$(ls -d "$RELEASES_DIR"/*/ 2>/dev/null | grep -E "/${RELEASE_NUM}-|/0${RELEASE_NUM}-" | head -1 || true)

if [[ -z "$RELEASE_DIR" ]]; then
    echo "ERROR: Release $RELEASE_NUM directory not found in $RELEASES_DIR" >&2
    exit 1
fi

RELEASE_NAME=$(basename "$RELEASE_DIR")
echo "Found release directory: $RELEASE_DIR"

# Count CHANGEs and SUMMARYs
CHANGE_COUNT=$(ls "$RELEASE_DIR"*-CHANGE.md 2>/dev/null | wc -l || echo 0)
SUMMARY_COUNT=$(ls "$RELEASE_DIR"*-SUMMARY.md 2>/dev/null | wc -l || echo 0)

echo "Current state: $CHANGE_COUNT CHANGEs, $SUMMARY_COUNT SUMMARYs"

# Check if release appears complete
if [[ "$CHANGE_COUNT" -gt 0 && "$CHANGE_COUNT" -eq "$SUMMARY_COUNT" ]]; then
    echo ""
    echo "⚠️  WARNING: Release $RELEASE_NUM appears COMPLETE (all CHANGEs have SUMMARYs)"
    echo ""
    echo "REQUIRED ACTIONS before adding new CHANGE:"
    echo ""
    echo "1. Re-open release in STATE.md:"
    echo "   Change: | $RELEASE_NUM | ... | ✅ Complete |"
    echo "   To:     | $RELEASE_NUM | ... | 🔄 In Progress |"
    echo ""
    echo "2. Update task count in ROADMAP.md:"
    echo "   Change: Release $RELEASE_NUM: ... (N tasks)"
    echo "   To:     Release $RELEASE_NUM: ... (N+1 tasks)"
    echo ""
    echo "3. Then add new CHANGE file"
    echo ""
else
    echo ""
    echo "✓ Release $RELEASE_NUM is not complete - safe to add new CHANGE"
    echo ""
fi

# Calculate next change number
LAST_CHANGE=$(ls "$RELEASE_DIR"*-CHANGE.md 2>/dev/null | sort | tail -1 | grep -oE '[0-9]+-[0-9]+' | cut -d'-' -f2 || echo "00")
# Remove leading zeros for arithmetic
LAST_CHANGE_NUM=$((10#$LAST_CHANGE))
NEXT_CHANGE_NUM=$(printf "%02d" $((LAST_CHANGE_NUM + 1)))

echo "Next change number: $RELEASE_NUM-$NEXT_CHANGE_NUM"
echo "New file pattern: ${RELEASE_DIR}${RELEASE_NUM}-${NEXT_CHANGE_NUM}-<slug>-CHANGE.md"
