#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in require-task-protocol.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Require Task Protocol Hook
# Trigger: PreToolUse
# Matcher: (tool:Write || tool:Edit) && path:/workspace/tasks/**
# Purpose: Enforce protocol-scope-specification.md rules
# Consolidates: detect-main-agent-implementation.sh, validate-lock-location.sh

# Source helper library
source /workspace/main/.claude/hooks/lib/pattern-matcher.sh

# Extract tool use details from environment variables
FILE_PATH="${TOOL_PATH:-}"
TOOL_NAME="${TOOL_NAME:-unknown}"

# Debug logging (uncomment for troubleshooting)
# echo "DEBUG: FILE_PATH=$FILE_PATH" >&2
# echo "DEBUG: TOOL_NAME=$TOOL_NAME" >&2

# Only enforce for /workspace/tasks/** paths
if [[ ! "$FILE_PATH" =~ ^/workspace/tasks/ ]]; then
	exit 0  # Allow root workspace edits
fi

# Extract task name from path
# Pattern: /workspace/tasks/{task-name}/...
TASK_NAME=$(echo "$FILE_PATH" | sed -n 's|^/workspace/tasks/\([^/]*\)/.*|\1|p')

if [[ -z "$TASK_NAME" ]]; then
	# Path is exactly /workspace/tasks/ with no task subdirectory
	exit 0
fi

# Check protocol initialization
TASK_JSON="/workspace/tasks/$TASK_NAME/task.json"

# ═══════════════════════════════════════════════════════════════
# Phase 1: No Protocol Initialized (task.json doesn't exist)
# ═══════════════════════════════════════════════════════════════
if [[ ! -f "$TASK_JSON" ]]; then
	# Apply Category A/B rules

	# Check Category A (allowed without protocol)
	if match_category_a "$FILE_PATH"; then
		exit 0  # Allow
	fi

	# Check Category B (requires protocol)
	if match_category_b "$FILE_PATH"; then
		# Block with error message
		CATEGORY_REASON=$(get_category_reason "$FILE_PATH")

		cat << EOF >&2
❌ PROTOCOL REQUIRED

You are attempting to modify:
  $FILE_PATH

This is Category B work (requires task protocol):
  Type: $CATEGORY_REASON
  Category: Protocol-required work

✅ REQUIRED ACTIONS:
1. Initialize task protocol with task.json
2. Follow state machine (INIT → CLASSIFIED → REQUIREMENTS → ...)
3. Create agent worktrees for stakeholder agents

📖 See: protocol-scope-specification.md for complete rules
📖 See: main-agent-coordination.md for state machine guidance

🔧 Initialize protocol:
   Create task.json at: /workspace/tasks/$TASK_NAME/task.json

💡 Alternatively, if this is NOT todo.md work:
   - Documentation, hooks, scripts → Move to /workspace/main/
   - Build files → Category A, should have been allowed
   - Contact user if you believe this is incorrectly blocked
EOF
		exit 1  # Block
	fi

	# Unknown/edge case - allow with warning
	echo "⚠️ WARNING: Unrecognized file pattern: $FILE_PATH" >&2
	echo "   Allowing operation. See protocol-scope-specification.md for categories." >&2
	exit 0
fi

# ═══════════════════════════════════════════════════════════════
# Phase 2: Protocol Initialized (task.json exists)
# ═══════════════════════════════════════════════════════════════

# Read task.json state
if ! STATE=$(jq -r '.state' "$TASK_JSON" 2>/dev/null); then
	echo "⚠️ WARNING: Cannot read state from task.json: $TASK_JSON" >&2
	echo "   Allowing operation. Ensure task.json is valid JSON." >&2
	exit 0
fi

# Get current agent type (default to main)
AGENT_TYPE="${CURRENT_AGENT_TYPE:-main}"

# Check VIOLATION #1: Main agent creating source files in IMPLEMENTATION state
if [[ "$STATE" == "IMPLEMENTATION" ]] && [[ "$AGENT_TYPE" == "main" ]]; then
	if match_source_pattern "$FILE_PATH"; then
		cat << EOF >&2
❌ CRITICAL VIOLATION: Main Agent Source File Creation

Current state: IMPLEMENTATION
Agent: main
File: $FILE_PATH
Operation: $TOOL_NAME

🚫 VIOLATION #1: Main agent CANNOT create source files in IMPLEMENTATION state

✅ CORRECT APPROACH:
1. Delegate to stakeholder agents via Task tool
2. Agents work in isolated worktrees: /workspace/tasks/$TASK_NAME/agents/{agent}/code
3. Merge agent branches to task branch
4. Main agent coordinates, does NOT implement

📖 See: CLAUDE.md § Critical Protocol Violations
📖 See: main-agent-coordination.md § Source Code Creation Decision Tree

🔧 Required steps:
   1. Create agent worktree:
      git worktree add /workspace/tasks/$TASK_NAME/agents/{agent-name}/code -b $TASK_NAME-{agent-name}

   2. Invoke stakeholder agent:
      Use Task tool with appropriate agent type

   3. Merge agent work:
      git merge $TASK_NAME-{agent-name}
EOF
		exit 1  # Block
	fi
fi

# Check task.json location validation
if [[ "$FILE_PATH" =~ task\.json$ ]] && [[ "$FILE_PATH" != "$TASK_JSON" ]]; then
	cat << EOF >&2
❌ Invalid task.json Location

Expected location: $TASK_JSON
Actual location:   $FILE_PATH

Task lock files MUST be at the task root:
  /workspace/tasks/{task-name}/task.json

NOT inside subdirectories like:
  /workspace/tasks/{task-name}/code/task.json
  /workspace/tasks/{task-name}/agents/{agent}/task.json

📖 See: CLAUDE.md § Locks
EOF
	exit 1  # Block
fi

# All checks passed
exit 0
