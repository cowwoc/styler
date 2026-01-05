#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in archive-task.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

# Configuration
MAX_CHANGELOG_LINES=500
TODO_FILE="/workspace/main/todo.md"
CHANGELOG_FILE="/workspace/main/changelog.md"
DATE=$(date +%Y-%m-%d)
YEAR=$(date +%Y)
ARCHIVE_FILE="/workspace/main/changelog-${YEAR}.md"

# Function to archive changelog entries to yearly archive
# Returns 0 if archived, 1 if no archival needed
archive_changelog_entries() {
    local line_count
    line_count=$(wc -l < "$CHANGELOG_FILE")

    if [ "$line_count" -le "$MAX_CHANGELOG_LINES" ]; then
        return 1  # No archival needed
    fi

    echo "Changelog exceeds $MAX_CHANGELOG_LINES lines ($line_count), archiving to $ARCHIVE_FILE"

    # Extract entries (everything after the first line "# Changelog")
    local entries
    entries=$(tail -n +2 "$CHANGELOG_FILE")

    # Create or prepend to archive file
    if [ -f "$ARCHIVE_FILE" ]; then
        # Prepend new entries to existing archive (after archive header)
        local existing_content
        existing_content=$(tail -n +2 "$ARCHIVE_FILE")
        {
            echo "# Changelog Archive - $YEAR"
            echo "$entries"
            echo "$existing_content"
        } > "$ARCHIVE_FILE.tmp"
        mv "$ARCHIVE_FILE.tmp" "$ARCHIVE_FILE"
    else
        # Create new archive file
        {
            echo "# Changelog Archive - $YEAR"
            echo "$entries"
        } > "$ARCHIVE_FILE"
    fi

    # Reset changelog.md to header only
    echo "# Changelog" > "$CHANGELOG_FILE"

    # Stage archive file for commit
    git add "$ARCHIVE_FILE"
}

# Handle --archive-now flag for manual archival
if [ "${1:-}" = "--archive-now" ]; then
    if archive_changelog_entries; then
        echo "Archived to $ARCHIVE_FILE"
    else
        echo "No archival needed (under $MAX_CHANGELOG_LINES lines)"
    fi
    exit 0
fi

# Usage: archive-task.sh TASK_NAME "Change description line 1\nChange description line 2"

if [ $# -lt 2 ]; then
    json_error "Usage: archive-task.sh TASK_NAME CHANGES"
fi

TASK_NAME="$1"
CHANGES="$2"

# Verify files exist
[ -f "$TODO_FILE" ] || json_error "todo.md not found"
[ -f "$CHANGELOG_FILE" ] || json_error "changelog.md not found"

# Update todo.md: REMOVE task entry completely (NOT mark with [x])
# Per task archival policy: completed tasks are DELETED from todo.md
if ! grep -q "$TASK_NAME" "$TODO_FILE"; then
    json_error "Task not found in todo.md: $TASK_NAME"
fi

# Delete the task entry line and any following indented lines (sub-items)
# Pattern: line starting with "- [ ]" or "- [x]" containing task name,
# plus any immediately following lines that are indented (task details)
sed -i "/^- \[.\] .*$TASK_NAME/,/^[^ ]/{/^- \[.\] .*$TASK_NAME/d; /^  /d}" "$TODO_FILE"
# Also clean up any standalone task name line
sed -i "/^- \[.\] $TASK_NAME$/d" "$TODO_FILE"

# Update changelog.md: Add entry at top (after header)
CHANGELOG_ENTRY="## [$DATE] - $TASK_NAME

$CHANGES
"

# Insert after first line (header)
sed -i "1 a\\
\\
$CHANGELOG_ENTRY" "$CHANGELOG_FILE"

# Check if archival is needed after adding entry
ARCHIVED="false"
archive_changelog_entries && ARCHIVED="true"

# Commit atomically (includes archive file if created)
cd /workspace/main
git add "$TODO_FILE" "$CHANGELOG_FILE"

COMMIT_MSG="Archive task: $TASK_NAME (remove from todo.md)

Added changelog entry:
$CHANGES
"

if [ "$ARCHIVED" = "true" ]; then
    COMMIT_MSG="${COMMIT_MSG}
Archived changelog entries to $ARCHIVE_FILE (exceeded $MAX_CHANGELOG_LINES lines)
"
fi

git commit -m "$COMMIT_MSG"

COMMIT_SHA=$(git rev-parse HEAD)

json_success "task_name=$TASK_NAME" "todo_updated=true" "changelog_updated=true" "archived=$ARCHIVED" "commit_sha=$COMMIT_SHA"
