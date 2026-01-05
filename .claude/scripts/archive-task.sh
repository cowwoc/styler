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

# Function to archive changelog entries to yearly archive files (by entry date)
# Returns 0 if archived, 1 if no archival needed
archive_changelog_entries() {
    local line_count
    line_count=$(wc -l < "$CHANGELOG_FILE")

    if [ "$line_count" -le "$MAX_CHANGELOG_LINES" ]; then
        return 1  # No archival needed
    fi

    echo "Changelog exceeds $MAX_CHANGELOG_LINES lines ($line_count), archiving by entry date"

    # Use Python to split entries by year, keeping recent entries in changelog.md
    python3 << 'PYTHON_SCRIPT'
import re
import os

changelog_file = "/workspace/main/changelog.md"
max_lines = 500

with open(changelog_file, 'r') as f:
    content = f.read()

# Split into sections by date headers (## YYYY-MM-DD)
sections = re.split(r'(^## \d{4}-\d{2}-\d{2})', content, flags=re.MULTILINE)

# Parse entries in order (newest first)
entries = []
i = 1  # Skip header
while i < len(sections):
    if re.match(r'^## (\d{4})', sections[i]):
        year = sections[i][3:7]
        header = sections[i]
        body = sections[i+1] if i+1 < len(sections) else ""
        entry = header + body
        entries.append((year, entry))
        i += 2
    else:
        i += 1

# Determine how many entries to keep in changelog.md (stay under max_lines)
keep_entries = []
keep_lines = 1  # Start with header line
for year, entry in entries:
    entry_lines = entry.count('\n') + 1
    if keep_lines + entry_lines <= max_lines:
        keep_entries.append((year, entry))
        keep_lines += entry_lines
    else:
        break  # Stop adding to keep, rest goes to archive

# Entries to archive (the rest)
archive_entries = entries[len(keep_entries):]

if not archive_entries:
    print("No entries to archive")
    exit(0)

# Group archive entries by year
entries_by_year = {}
for year, entry in archive_entries:
    if year not in entries_by_year:
        entries_by_year[year] = []
    entries_by_year[year].append(entry)

# Write each year's entries to its archive file
for year, year_entries in entries_by_year.items():
    archive_file = f"/workspace/main/changelog-{year}.md"
    archive_header = f"# Changelog Archive - {year}\n"
    new_content = "".join(year_entries)

    if os.path.exists(archive_file):
        # Prepend new entries after header
        with open(archive_file, 'r') as f:
            existing = f.read()
        existing_entries = existing.split('\n', 1)[1] if '\n' in existing else ""
        with open(archive_file, 'w') as f:
            f.write(archive_header + new_content + existing_entries)
    else:
        with open(archive_file, 'w') as f:
            f.write(archive_header + new_content)

    print(f"Archived {len(year_entries)} entries to changelog-{year}.md")

# Rewrite changelog.md with kept entries only
with open(changelog_file, 'w') as f:
    f.write("# Changelog\n")
    for year, entry in keep_entries:
        f.write(entry)

print(f"Kept {len(keep_entries)} entries in changelog.md ({keep_lines} lines)")
PYTHON_SCRIPT

    # Stage all archive files for commit
    for archive in /workspace/main/changelog-[0-9][0-9][0-9][0-9].md; do
        [ -f "$archive" ] && git add "$archive"
    done
}

# Handle --archive-now flag for manual archival
if [ "${1:-}" = "--archive-now" ]; then
    if archive_changelog_entries; then
        echo "Archival complete (entries split by date)"
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
Archived changelog entries by date (exceeded $MAX_CHANGELOG_LINES lines)
"
fi

git commit -m "$COMMIT_MSG"

COMMIT_SHA=$(git rev-parse HEAD)

json_success "task_name=$TASK_NAME" "todo_updated=true" "changelog_updated=true" "archived=$ARCHIVED" "commit_sha=$COMMIT_SHA"
