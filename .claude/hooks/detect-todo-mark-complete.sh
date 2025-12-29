#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in detect-todo-mark-complete.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Detect when agent marks todo.md task with [x] instead of deleting
#
# TRIGGER: PostToolUse on Edit tool for todo.md
#
# This hook detects a common archival mistake where agents mark tasks
# with [x] **DONE:** or [x] **COMPLETE:** instead of deleting the entry.
#
# The correct archival behavior is:
# 1. DELETE the entire task entry from todo.md
# 2. ADD the task to changelog.md
#
# This hook runs after Edit tool to detect the mistake pattern.

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

# Extract new_string from parameters to check what was written
NEW_STRING=$(echo "$INPUT" | jq -r '.tool.parameters.new_string // ""' 2>/dev/null || echo "")

# Check for the mistake patterns: [x] followed by DONE or COMPLETE
# This catches: - [x] **DONE:**  or  - [x] **COMPLETE:**
if echo "$NEW_STRING" | grep -qE '\[x\].*\*\*(DONE|COMPLETE):\*\*'; then
    # Detected the mistake pattern
    MESSAGE="## ⚠️ TODO.MD ARCHIVAL MISTAKE DETECTED

**Problem**: You marked a task with \`[x] **DONE:**\` or \`[x] **COMPLETE:**\`
**This is WRONG**: Tasks should be DELETED from todo.md, NOT marked with \`[x]\`

**Correct Archival Process**:
1. **DELETE** the entire task entry from todo.md (use Edit to remove the section)
2. **ADD** the task to changelog.md under today's date

**Immediate Fix Required**:
\`\`\`bash
# Remove the incorrectly marked entry and delete the entire task section
\`\`\`

**Reference**: task-protocol-operations.md § COMPLETE → CLEANUP:
> todo.md removed from todo list and added to changelog.md
> Remove completed task entry from todo.md (delete entire task section)

**Note**: The \`[x]\` checkbox pattern is for personal todo lists, NOT for this project's
task protocol. Completed tasks are archived to changelog.md and removed from todo.md."

    echo "{
      \"hookSpecificOutput\": {
        \"hookEventName\": \"PostToolUse\",
        \"additionalContext\": $(echo "$MESSAGE" | jq -Rs .)
      }
    }" >&2

    # Exit with code 0 - this is a warning, not a blocking error
    # The mistake already happened, we're informing the agent to fix it
    exit 0
fi

# No mistake detected
exit 0
