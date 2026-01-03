#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in archive-task.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

# Usage: archive-task.sh TASK_NAME "Change description line 1\nChange description line 2"

if [ $# -lt 2 ]; then
    json_error "Usage: archive-task.sh TASK_NAME CHANGES"
fi

TASK_NAME="$1"
CHANGES="$2"
TODO_FILE="/workspace/main/todo.md"
CHANGELOG_FILE="/workspace/main/changelog.md"
DATE=$(date +%Y-%m-%d)

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

# Commit both atomically
cd /workspace/main
git add "$TODO_FILE" "$CHANGELOG_FILE"
git commit -m "Archive task: $TASK_NAME (remove from todo.md)

Added changelog entry:
$CHANGES

"

COMMIT_SHA=$(git rev-parse HEAD)

json_success "task_name=$TASK_NAME" "todo_updated=true" "changelog_updated=true" "commit_sha=$COMMIT_SHA"
