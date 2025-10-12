#!/bin/bash
# detect-worktree-violation.sh - BLOCKS directory changes outside task worktree
# Hook Type: PreToolUse (Bash tool with cd command)
# Trigger: Before Bash tool execution when command contains 'cd'
#
# Purpose: Enforce CRITICAL WORKTREE ISOLATION rule during task execution
# Policy: NEVER allow 'cd' commands that leave the task worktree during States 0-7
# Exception: State 8 (CLEANUP) allows cd to main worktree for safe worktree removal

# Read JSON from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	# No input available, exit gracefully
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Simple JSON value extraction without jq dependency
extract_json_value()
{
	local json="$1"
	local key="$2"
	# Use grep and sed for basic JSON parsing
	echo "$json" | grep -o "\"$key\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | sed "s/\"$key\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\"/\1/"
}

# Extract tool name and command from JSON
TOOL_NAME=$(extract_json_value "$JSON_INPUT" "tool_name")
BASH_COMMAND=$(extract_json_value "$JSON_INPUT" "command")

# Fallback extraction if primary method fails
if [ -z "$TOOL_NAME" ]; then
    TOOL_NAME=$(echo "$JSON_INPUT" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

if [ -z "$BASH_COMMAND" ]; then
    BASH_COMMAND=$(echo "$JSON_INPUT" | sed -n 's/.*"command"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

# Only process Bash tool calls
if [ "$TOOL_NAME" != "Bash" ]; then
	exit 0
fi

# Check if we have an active task
LOCK_FILE=$(ls /workspace/locks/*.json 2>/dev/null | grep -v ".gitkeep" | head -1)
if [ -z "$LOCK_FILE" ]; then
    exit 0  # No active task, no restriction
fi

# Extract task name from lock file
TASK_NAME=$(basename "$LOCK_FILE" .json)
EXPECTED_WORKTREE="/workspace/branches/${TASK_NAME}/code"

# Detect 'cd' commands that might leave task worktree
# Use timeout to prevent ReDoS attacks - limit regex execution to 1 second
# Pattern: "cd path", "; cd path", "&& cd path", "|| cd path"
if timeout 1s grep -qE "(^|[;&|]) {0,10}cd +[^ ]" <<< "$BASH_COMMAND" 2>/dev/null; then
    # Extract the target directory from the cd command
    # Handle both quoted and unquoted paths
    # Use timeout to prevent ReDoS - limit to 1 second
    # First try to extract quoted paths (handles spaces), then unquoted
    TARGET=$(timeout 1s grep -oE "cd +['\"][^'\"]+['\"]|cd +[^ ;]+" <<< "$BASH_COMMAND" 2>/dev/null | head -1 | sed "s/cd *//" | sed "s/['\"]//g")

    # Initialize VIOLATION variable
    VIOLATION=false

    # If extraction failed (timeout or other), default to blocking
    if [ -z "$TARGET" ]; then
        VIOLATION=true
    else
        # EXCEPTION: Allow cd to main worktree during cleanup (State 8)
        # Pattern: "cd /workspace/branches/main/code" in preparation for worktree removal
        MAIN_WORKTREE="/workspace/branches/main/code"
        if [[ "$TARGET" == "$MAIN_WORKTREE" ]] && echo "$BASH_COMMAND" | grep -q "git worktree remove"; then
            # This is the cleanup phase pattern: cd main && git worktree remove task
            exit 0  # Allow this specific pattern
        fi

        # Resolve relative paths to absolute (simplified check)
        if [[ "$TARGET" == "." ]]; then
            # "cd ." is a no-op, allow it
            exit 0
        elif [[ "$TARGET" == /* ]]; then
            # Absolute path - canonicalize and check if it's within task worktree
            # Use realpath for symlink resolution (with fallback if not available)
            if command -v realpath >/dev/null 2>&1; then
                RESOLVED_TARGET=$(realpath -m "$TARGET" 2>/dev/null || echo "$TARGET")
            else
                RESOLVED_TARGET="$TARGET"
            fi

            if [[ "$RESOLVED_TARGET" != "$EXPECTED_WORKTREE"* ]]; then
                VIOLATION=true
            fi
        elif [[ "$TARGET" == ".."* ]] || [[ "$TARGET" == ~* ]]; then
            # Relative paths going up (..) or home (~) are violations
            VIOLATION=true
        fi
    fi

    # If violation detected, block the command
    if [ "$VIOLATION" = true ]; then
        cat << EOF

🚨 WORKTREE VIOLATION BLOCKED
═══════════════════════════════════════════════════════════════

❌ Attempted: cd "$TARGET"
✅ Required:  Stay in "$EXPECTED_WORKTREE"

ALTERNATIVE APPROACHES:

1. Use subshell (auto-returns to original directory):
   (cd "$TARGET" && command)

2. Use git -C flag (no directory change):
   git -C "$TARGET" command

3. Use Read tool for file access:
   Read "$TARGET/file.ext"

4. Use absolute paths with current tools:
   Most git commands work from any worktree

5. Use tool-specific directory flags:
   find "$TARGET" -name "*.java"

WHY THIS MATTERS:
• Bash tool maintains working directory between invocations
• One 'cd' affects ALL subsequent commands
• There is NO legitimate reason to leave task worktree
• All objectives can be accomplished from within it

RATIONALE:
During task execution (States 0-7), all work MUST occur in the
task worktree. This enforces isolation and prevents commands from
running in the wrong location.

═══════════════════════════════════════════════════════════════
❌ COMMAND BLOCKED - Use alternative approach above
EOF
        exit 1  # Block the command
    fi
fi

exit 0  # Allow the command
