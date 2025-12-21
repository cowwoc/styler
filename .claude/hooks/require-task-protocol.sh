#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in require-task-protocol.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Require Task Protocol Hook
# Trigger: PreToolUse
# Matcher: (tool:Write || tool:Edit) && path:/workspace/tasks/**
# Purpose: Enforce protocol-scope-specification.md rules
# Consolidates: detect-main-agent-implementation.sh, validate-lock-location.sh

# Source helper libraries
source /workspace/main/.claude/hooks/lib/pattern-matcher.sh
source /workspace/.claude/scripts/json-output.sh

# Parse stdin JSON for tool invocation details
TOOL_INPUT=$(cat)

# Extract tool name and target file path
# Note: Claude Code uses .tool_name and .tool_input, not .tool and .parameters
TOOL_NAME=$(echo "$TOOL_INPUT" | jq -r '.tool_name // empty')
FILE_PATH=$(echo "$TOOL_INPUT" | jq -r '.tool_input.file_path // empty')

# Debug logging (uncomment for troubleshooting)
# echo "DEBUG require-task-protocol.sh: FILE_PATH=$FILE_PATH" >&2
# echo "DEBUG require-task-protocol.sh: TOOL_NAME=$TOOL_NAME" >&2
# echo "DEBUG require-task-protocol.sh: TOOL_INPUT=$TOOL_INPUT" >&2

# Only enforce for Write and Edit tools
if [[ "$TOOL_NAME" != "Write" && "$TOOL_NAME" != "Edit" ]]; then
	exit 0
fi

# ═══════════════════════════════════════════════════════════════
# Check 0: Block /workspace/main/ source edits when task is active
# ═══════════════════════════════════════════════════════════════
# ADDED: 2025-12-16 after main agent edited NodeArena.java, Parser.java,
# NodeArenaTest.java directly in /workspace/main/ while migrate-formatters-to-ast
# task was active in SYNTHESIS state. This violated protocol-scope-specification.md
# which requires ALL source code edits go through task worktrees, not directly to main.
if [[ "$FILE_PATH" =~ ^/workspace/main/ ]]; then
	# Check if this is Category B (source code requiring protocol)
	if match_category_b "$FILE_PATH"; then
		# Check for active tasks
		TERMINAL_STATES=("COMPLETE" "ABANDONED" "CLEANUP")
		for task_dir in /workspace/tasks/*/; do
			[[ -d "$task_dir" ]] || continue
			task_json="${task_dir}task.json"
			[[ -f "$task_json" ]] || continue

			state=$(jq -r '.state // "UNKNOWN"' "$task_json" 2>/dev/null || echo "UNKNOWN")
			task_name=$(basename "$task_dir")

			# Check if state is NOT terminal
			is_terminal=false
			for terminal in "${TERMINAL_STATES[@]}"; do
				if [[ "$state" == "$terminal" ]]; then
					is_terminal=true
					break
				fi
			done

			if [[ "$is_terminal" == "false" && "$state" != "UNKNOWN" ]]; then
				# Active task found - block the edit
				CATEGORY_REASON=$(get_category_reason "$FILE_PATH")
				cat << EOF >&2
❌ BLOCKED: Source Code Edit to Main Branch During Active Task

You are attempting to edit:
  $FILE_PATH

This is Category B work (requires task protocol):
  Type: $CATEGORY_REASON

⚠️  ACTIVE TASK DETECTED: $task_name (state: $state)

🚫 VIOLATION: When a task is active, source code changes MUST go through
   the task worktree, not directly to /workspace/main/.

✅ CORRECT APPROACH:
1. Switch to task worktree: cd /workspace/tasks/$task_name/code/
2. Make your edits there
3. Edits will be merged to main when task completes

📖 See: protocol-scope-specification.md § Category B: Protocol-Required Work

🔧 Quick fix - edit the file in task worktree instead:
   Edit: /workspace/tasks/$task_name/code/${FILE_PATH#/workspace/main/}
EOF
				output_hook_block "Blocked: Source code edits to /workspace/main/ not allowed while task '$task_name' is active. Use task worktree instead."
				exit 0
			fi
		done
	fi
	exit 0  # Category A files (docs, hooks, config) are allowed on main
fi

# Only enforce for /workspace/tasks/** paths (from here on)
if [[ ! "$FILE_PATH" =~ ^/workspace/tasks/ ]]; then
	exit 0  # Other paths not covered
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
		# Use proper permission system
		output_hook_block "Blocked: Protocol required for Category B work. Initialize task.json first."
		exit 0
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

# ═══════════════════════════════════════════════════════════════
# State Sequence Validation - Check for skipped required states
# ═══════════════════════════════════════════════════════════════
# ADDED: 2025-10-31 after main agent created task.json with state="IMPLEMENTATION"
# directly, bypassing INIT, CLASSIFIED, REQUIREMENTS, SYNTHESIS states entirely.
# This caused enforce-checkpoints.sh to never trigger since checkpoints only
# enforce transitions FROM required states (not detecting when states are skipped).

# Required state sequence for complete protocol
REQUIRED_SEQUENCE=("INIT" "CLASSIFIED" "REQUIREMENTS" "SYNTHESIS" "IMPLEMENTATION")

# Check if current state requires earlier states to have been completed
case "$STATE" in
	"IMPLEMENTATION")
		# Verify all earlier states were completed
		if ! jq -e '.transition_log' "$TASK_JSON" > /dev/null 2>&1; then
			# No transition_log - this is a protocol violation
			cat << EOF >&2
❌ PROTOCOL VIOLATION: State Sequence Skipped

Current state: $STATE (in task.json)
Task: $TASK_NAME

🚫 VIOLATION: Task.json created with state="IMPLEMENTATION" without going through required states.

Required state sequence:
  INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → [PLAN APPROVAL] → IMPLEMENTATION

You skipped: INIT, CLASSIFIED, REQUIREMENTS, SYNTHESIS states entirely.

⚠️  CRITICAL: SYNTHESIS state includes MANDATORY user approval checkpoint.
   By skipping SYNTHESIS, you bypassed the plan approval requirement.

✅ CORRECT WORKFLOW:
1. Create task.json with state="INIT"
2. Progress through: INIT → CLASSIFIED → REQUIREMENTS
3. SYNTHESIS: Gather requirements, create implementation plan
4. **STOP**: Present plan to user, wait for approval
5. Create approval flag after user approval
6. ONLY THEN transition to IMPLEMENTATION

📖 See: task-protocol-core.md § State Machine Architecture
📖 See: CLAUDE.md § MANDATORY USER APPROVAL CHECKPOINTS

🔧 FIX: Delete task.json and restart with proper state sequence:
   rm $TASK_JSON
   # Then follow protocol from INIT state
EOF
			# Use proper permission system
			output_hook_block "Blocked: State sequence skipped. task.json created with IMPLEMENTATION without going through required states."
			exit 0
		fi

		# Verify transition log contains all required states
		TRANSITION_LOG=$(jq -r '.transition_log[]?.to // empty' "$TASK_JSON" 2>/dev/null || echo "")

		# Check each required state appears in transition history
		for required_state in "${REQUIRED_SEQUENCE[@]}"; do
			if [[ "$required_state" == "IMPLEMENTATION" ]]; then
				break  # Don't check for current state
			fi

			if ! echo "$TRANSITION_LOG" | grep -q "^${required_state}$"; then
				cat << EOF >&2
❌ PROTOCOL VIOLATION: Skipped Required State

Current state: $STATE
Task: $TASK_NAME
Missing state: $required_state

Required sequence: ${REQUIRED_SEQUENCE[*]}
Your sequence: $(echo "$TRANSITION_LOG" | tr '\n' ' ')

🚫 VIOLATION: You skipped $required_state in the state progression.

Each state serves a critical purpose:
- INIT: Setup worktrees, acquire locks
- CLASSIFIED: Risk assessment, agent selection
- REQUIREMENTS: Stakeholder requirements gathering
- SYNTHESIS: Implementation planning + USER APPROVAL CHECKPOINT
- IMPLEMENTATION: Actual implementation by stakeholder agents

✅ CORRECT APPROACH: Follow complete state sequence without skipping

📖 See: task-protocol-core.md § State Definitions
EOF
				# Use proper permission system
				output_hook_block "Blocked: Skipped required state $required_state in state progression."
				exit 0
			fi
		done
		;;

	"SYNTHESIS"|"REQUIREMENTS"|"CLASSIFIED")
		# These states also require earlier states, but IMPLEMENTATION is most critical
		# Can add similar checks here if needed
		;;
esac

# Get current agent type (default to main)
AGENT_TYPE="${CURRENT_AGENT_TYPE:-main}"

# Check VIOLATION #1: Main agent creating source files in IMPLEMENTATION state
if [[ "$STATE" == "IMPLEMENTATION" ]] && [[ "$AGENT_TYPE" == "main" ]]; then
	if match_source_pattern "$FILE_PATH"; then
		# Exception: Allow agent worktree paths (agents are allowed to create source files)
		# Pattern: /workspace/tasks/{task}/agents/{agent}/code/...
		if [[ "$FILE_PATH" =~ /workspace/tasks/$TASK_NAME/agents/[^/]+/code/ ]]; then
			exit 0  # Allow - this is an agent working in their isolated worktree
		fi

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
		# Use proper permission system
		output_hook_block "Blocked: Main agent cannot create source files in IMPLEMENTATION state. Delegate to stakeholder agents."
		exit 0
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
	# Use proper permission system
	output_hook_block "Blocked: Invalid task.json location. Must be at task root: /workspace/tasks/{task-name}/task.json"
	exit 0
fi

# All checks passed
exit 0
