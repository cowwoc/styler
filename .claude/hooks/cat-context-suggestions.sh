#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in cat-context-suggestions.sh line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Session start hook: Provide context-aware CAT suggestions
# Only runs if CAT is initialized (cat-config.json exists)

CAT_DIR=".claude/cat"
CONFIG_FILE="${CAT_DIR}/cat-config.json"

# Exit silently if CAT not initialized
if [ ! -f "$CONFIG_FILE" ]; then
    exit 0
fi

# Count pending tasks (exclude backups directory)
# Supports both formats: "status: pending" and "**Status:** pending"
count_pending_tasks() {
    find "$CAT_DIR" -path "*/task/*" -name "STATE.md" -type f ! -path "*/backups/*" 2>/dev/null \
        -exec grep -liE "(status:.*pending|\*\*Status:\*\*.*pending)" {} \; 2>/dev/null | wc -l
}

# Count in-progress tasks (exclude backups directory)
count_in_progress_tasks() {
    find "$CAT_DIR" -path "*/task/*" -name "STATE.md" -type f ! -path "*/backups/*" 2>/dev/null \
        -exec grep -liE "(status:.*in.?progress|\*\*Status:\*\*.*in.?progress)" {} \; 2>/dev/null | wc -l
}

# Count completed tasks (exclude backups directory)
count_completed_tasks() {
    find "$CAT_DIR" -path "*/task/*" -name "STATE.md" -type f ! -path "*/backups/*" 2>/dev/null \
        -exec grep -liE "(status:.*completed|\*\*Status:\*\*.*completed)" {} \; 2>/dev/null | wc -l
}

# Check if any tasks exist (exclude backups)
has_any_tasks() {
    local count
    count=$(find "$CAT_DIR" -path "*/task/*" -name "STATE.md" -type f ! -path "*/backups/*" 2>/dev/null | wc -l)
    [ "$count" -gt 0 ]
}

# Get current minor version with pending work (exclude backups)
get_current_minor() {
    find "$CAT_DIR" -path "*/v*/v*.*" -type d ! -path "*/backups/*" 2>/dev/null | sort -V | while read -r minor_dir; do
        if find "$minor_dir" -path "*/task/*" -name "STATE.md" -exec grep -liE "(status:.*(pending|in.?progress)|\*\*Status:\*\*.*(pending|in.?progress))" {} \; 2>/dev/null | head -1 | grep -q .; then
            basename "$minor_dir"
            break
        fi
    done
}

PENDING=$(count_pending_tasks)
IN_PROGRESS=$(count_in_progress_tasks)
COMPLETED=$(count_completed_tasks)

# Determine context and provide suggestion
if ! has_any_tasks; then
    # CAT initialized but no tasks
    echo ""
    echo "📋 CAT initialized but no tasks yet."
    echo "   → /cat:add     Create your first task"
    echo "   → /cat:status  See project structure"
    echo ""
elif [ "$IN_PROGRESS" -gt 0 ]; then
    # Work in progress
    CURRENT=$(get_current_minor)
    echo ""
    echo "🔄 ${IN_PROGRESS} task(s) in progress."
    echo "   → /cat:work    Continue working"
    echo "   → /cat:status  See current state"
    echo ""
elif [ "$PENDING" -gt 0 ]; then
    # Tasks available
    CURRENT=$(get_current_minor)
    echo ""
    echo "📋 ${PENDING} task(s) available${CURRENT:+ in $CURRENT}."
    echo "   → /cat:work    Start next task"
    echo "   → /cat:status  See full overview"
    echo ""
elif [ "$COMPLETED" -gt 0 ] && [ "$PENDING" -eq 0 ]; then
    # All tasks complete
    echo ""
    echo "✅ All current tasks complete! (${COMPLETED} done)"
    echo "   → /cat:status  See what's next"
    echo "   → /cat:add     Add more work"
    echo ""
fi

exit 0
