#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in pre-tool-use-implementation-guard.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Pre-tool-use hook: Block main agent from creating source files during IMPLEMENTATION state
# Prevents VIOLATION #1: Main Agent Source File Creation

# Parse stdin JSON for tool invocation details
TOOL_INPUT=$(cat)

# Extract tool name and target file path
TOOL_NAME=$(echo "$TOOL_INPUT" | jq -r '.tool // empty')
TARGET_PATH=$(echo "$TOOL_INPUT" | jq -r '.parameters.file_path // empty')

# Only guard Write and Edit tools
if [[ "$TOOL_NAME" != "Write" && "$TOOL_NAME" != "Edit" ]]; then
    exit 0
fi

# Check if we're in a task context
TASK_NAME=""
if [[ "$PWD" == /workspace/tasks/*/code ]] || [[ "$PWD" == /workspace/tasks/*/agents/*/code ]]; then
    # Extract task name from path
    TASK_NAME=$(echo "$PWD" | sed -E 's|/workspace/tasks/([^/]+)/.*|\1|')
elif [[ -n "$TARGET_PATH" && "$TARGET_PATH" == /workspace/tasks/*/code/* ]]; then
    # Extract from target path
    TASK_NAME=$(echo "$TARGET_PATH" | sed -E 's|/workspace/tasks/([^/]+)/.*|\1|')
fi

# If no task context, allow operation
if [[ -z "$TASK_NAME" ]]; then
    exit 0
fi

# Read task state
TASK_JSON="/workspace/tasks/$TASK_NAME/task.json"
if [[ ! -f "$TASK_JSON" ]]; then
    exit 0
fi

STATE=$(jq -r '.state // empty' "$TASK_JSON")

# Only enforce during IMPLEMENTATION state
if [[ "$STATE" != "IMPLEMENTATION" ]]; then
    exit 0
fi

# Check if target is a source file
if [[ -z "$TARGET_PATH" ]]; then
    exit 0
fi

# Source file extensions to guard
if [[ "$TARGET_PATH" =~ \.(java|ts|py|js|jsx|tsx|cpp|c|h|hpp|rs|go)$ ]]; then
    # Check if this is an agent worktree path (should be allowed)
    # Use regex pattern matching instead of glob
    if [[ "$TARGET_PATH" =~ /workspace/tasks/$TASK_NAME/agents/[^/]+/code/ ]]; then
        exit 0
    fi

    # Check if target is in task worktree (not agent worktree)
    if [[ "$TARGET_PATH" == /workspace/tasks/$TASK_NAME/code/* ]]; then
        # BLOCK: Main agent attempting to create source file during IMPLEMENTATION
        echo "❌ PROTOCOL VIOLATION: Main agent cannot create source files during IMPLEMENTATION state" >&2
        echo "" >&2
        echo "Tool: $TOOL_NAME" >&2
        echo "Target: $TARGET_PATH" >&2
        echo "State: $STATE" >&2
        echo "" >&2
        echo "CORRECT APPROACH: Use Task tool to delegate implementation to stakeholder agents." >&2
        echo "Stakeholder agents create files in agent worktrees, then merge to task branch." >&2
        echo "" >&2
        echo "See: /workspace/main/CLAUDE.md § CRITICAL PROTOCOL VIOLATIONS #1" >&2
        exit 1
    fi
fi

# Allow operation
exit 0
