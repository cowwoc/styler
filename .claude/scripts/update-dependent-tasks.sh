#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in update-dependent-tasks.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

# Usage: update-dependent-tasks.sh COMPLETED_TASK

if [ $# -lt 1 ]; then
    json_error "Usage: update-dependent-tasks.sh COMPLETED_TASK"
fi

COMPLETED_TASK="$1"
TODO_FILE="/workspace/main/todo.md"
CHANGELOG_FILE="/workspace/main/changelog.md"

# Verify files exist
[ -f "$TODO_FILE" ] || json_error "todo.md not found"
[ -f "$CHANGELOG_FILE" ] || json_error "changelog.md not found"

UNBLOCKED=()
STILL_BLOCKED=()

# Find all BLOCKED tasks
while IFS= read -r line_num; do
    # Extract line content
    line=$(sed -n "${line_num}p" "$TODO_FILE")
   
    # Extract task name
    task_name=$(echo "$line" | grep -oP '`\K[^`]+' | head -1 || true)
    [ -z "$task_name" ] && continue
   
    # Find dependencies (next few lines)
    deps_line=$(sed -n "$((line_num+1)),$((line_num+5))p" "$TODO_FILE" | grep "Dependencies:" | head -1 || true)
    [ -z "$deps_line" ] && continue
   
    # Extract dependency list
    deps=$(echo "$deps_line" | sed 's/.*Dependencies: *//' | sed 's/ *$//')
   
    # Check if all dependencies satisfied
    all_satisfied=true
    IFS=',' read -ra DEP_ARRAY <<< "$deps"
    for dep in "${DEP_ARRAY[@]}"; do
        dep=$(echo "$dep" | xargs | sed 's/ .*//')  # Trim whitespace and remove descriptions
        if ! grep -q "$dep" "$CHANGELOG_FILE"; then
            all_satisfied=false
            break
        fi
    done
   
    # Update status if all satisfied
    if [ "$all_satisfied" = true ]; then
        sed -i "${line_num}s/BLOCKED:/READY:/" "$TODO_FILE"
        UNBLOCKED+=("$task_name")
    else
        STILL_BLOCKED+=("$task_name")
    fi
   
done < <(grep -n "BLOCKED:" "$TODO_FILE" | cut -d: -f1)

# Commit changes if any tasks unblocked
if [ ${#UNBLOCKED[@]} -gt 0 ]; then
    cd /workspace/main
    git add "$TODO_FILE"
    git commit -m "Update dependencies: Unblock tasks after $COMPLETED_TASK

Unblocked: ${UNBLOCKED[*]}

🤖 Generated with Claude Code
Co-Authored-By: Claude <noreply@anthropic.com>"
fi

json_success \
    "completed_task=$COMPLETED_TASK" \
    "unblocked=$(printf '%s\n' "${UNBLOCKED[@]}" | jq -R . | jq -s .)" \
    "still_blocked=$(printf '%s\n' "${STILL_BLOCKED[@]}" | jq -R . | jq -s .)"
