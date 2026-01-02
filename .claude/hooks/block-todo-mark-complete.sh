#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-todo-mark-complete.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Block attempts to mark todo.md task with [x] instead of deleting
#
# TRIGGER: PreToolUse on Edit tool for todo.md
#
# This hook BLOCKS the common archival mistake where agents try to mark tasks
# with [x] **DONE:** or [x] **COMPLETE:** instead of deleting the entry.
#
# The correct archival behavior is:
# 1. DELETE the entire task entry from todo.md
# 2. ADD the task to changelog.md
#
# ADDED: 2026-01-02 after learn-from-mistakes investigation found conflicting
# documentation caused this recurring mistake pattern.

# Read tool invocation from stdin
INPUT=$(cat)

# Extract tool name
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // ""' 2>/dev/null || echo "")

# Only check Edit tool
if [[ "$TOOL_NAME" != "Edit" ]]; then
    exit 0
fi

# Extract file path from parameters
FILE_PATH=$(echo "$INPUT" | jq -r '.tool.parameters.file_path // ""' 2>/dev/null || echo "")

# Only check todo.md
if [[ ! "$FILE_PATH" =~ todo\.md$ ]]; then
    exit 0
fi

# Extract new_string from parameters to check what would be written
NEW_STRING=$(echo "$INPUT" | jq -r '.tool.parameters.new_string // ""' 2>/dev/null || echo "")

# Check for the mistake patterns: [x] followed by DONE or COMPLETE
# This catches: - [x] **DONE:**  or  - [x] **COMPLETE:**
if echo "$NEW_STRING" | grep -qE '\[x\].*\*\*(DONE|COMPLETE):\*\*'; then
    # Block the operation
    cat >&2 <<'EOF'
## ❌ BLOCKED: TODO.MD ARCHIVAL MISTAKE

**Problem**: You're trying to mark a task with `[x] **DONE:**` or `[x] **COMPLETE:**`
**This is WRONG**: Tasks must be DELETED from todo.md, NOT marked with `[x]`

**Correct Archival Process**:
1. **DELETE** the entire task entry from todo.md (remove the section completely)
2. **ADD** the task to changelog.md under today's date

**How to Fix**:
Instead of changing `[ ]` to `[x]`, use Edit to DELETE the entire task entry:
- Remove the task line: `- [ ] task-name`
- Remove all sub-items (Dependencies, Blocks, etc.)

**Reference**: todo.md line 27:
> Completed tasks are REMOVED from todo.md (entire entry deleted)

**Reference**: archive-task/SKILL.md:
> CRITICAL: Tasks are REMOVED from todo.md, NOT marked with `[x]`
EOF
    exit 2
fi

# Also check for just adding [x] without the status text
if echo "$NEW_STRING" | grep -qE '^\s*-\s*\[x\]'; then
    # Check if old_string has [ ] (meaning they're changing from unchecked to checked)
    OLD_STRING=$(echo "$INPUT" | jq -r '.tool.parameters.old_string // ""' 2>/dev/null || echo "")
    if echo "$OLD_STRING" | grep -qE '^\s*-\s*\[ \]'; then
        cat >&2 <<'EOF'
## ❌ BLOCKED: TODO.MD ARCHIVAL MISTAKE

**Problem**: You're trying to change `[ ]` to `[x]` in todo.md
**This is WRONG**: Tasks must be DELETED from todo.md, NOT marked with `[x]`

**Correct Archival Process**:
1. **DELETE** the entire task entry from todo.md (remove the section completely)
2. **ADD** the task to changelog.md under today's date

**Reference**: todo.md line 27:
> Completed tasks are REMOVED from todo.md (entire entry deleted)
EOF
        exit 2
    fi
fi

# No mistake detected - allow the operation
exit 0
