#!/bin/bash
# find-next-change.sh - Find the next CHANGE.md without a corresponding SUMMARY.md
# This script scans releases in numerical order, not relying on STATUS markers
#
# Usage: .claude/scripts/find-next-change.sh
# Output: Path to next CHANGE.md to execute, or message if all complete

set -euo pipefail

RELEASES_DIR=".planning/releases"

if [[ ! -d "$RELEASES_DIR" ]]; then
    echo "ERROR: $RELEASES_DIR not found" >&2
    exit 1
fi

# Get all release directories sorted numerically
# Handles both integer (01-name) and decimal (01.1-name) formats
release_dirs=$(ls -d "$RELEASES_DIR"/*/ 2>/dev/null | sort -t'-' -k1 -V)

for release_dir in $release_dirs; do
    # Get all CHANGE files in this release
    change_files=$(ls "$release_dir"*-CHANGE.md 2>/dev/null | sort || true)

    for change_file in $change_files; do
        [[ -z "$change_file" ]] && continue

        # Derive expected SUMMARY path
        summary_file="${change_file%-CHANGE.md}-SUMMARY.md"

        if [[ ! -f "$summary_file" ]]; then
            echo "$change_file"
            exit 0
        fi
    done
done

echo "All changes complete - no pending CHANGE.md files found"
exit 0
