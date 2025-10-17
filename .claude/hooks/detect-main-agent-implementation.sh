#!/bin/bash
# Hook: detect-main-agent-implementation.sh
# Trigger: PreToolUse (must be registered in .claude/settings.json)
# Matcher: (tool:Write || tool:Edit) && path:**/*.{java,ts,py,js,go,rs,cpp,c,h}
# Purpose: Detect and BLOCK main agent implementing source files during IMPLEMENTATION state

set -euo pipefail

# Read JSON input from stdin
INPUT=$(cat)

# Parse tool name, file path, and session ID from JSON
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
FILE_PATH=$(echo "$INPUT" | jq -r '.parameters.file_path // empty')
CURRENT_SESSION=$(echo "$INPUT" | jq -r '.session_id')

# Only check Write/Edit tools
if [[ "$TOOL_NAME" != "Write" && "$TOOL_NAME" != "Edit" ]]; then
  exit 0
fi

# file_path is required for Write/Edit tools
if [[ -z "$FILE_PATH" ]]; then
  echo "ERROR: file_path parameter is required for $TOOL_NAME tool"
  exit 1
fi

# Find active task in IMPLEMENTATION state
IMPLEMENTATION_TASKS=$(find /workspace/tasks -name "task.json" -type f 2>/dev/null | while read LOCK_FILE; do
  TASK_DIR=$(dirname "$LOCK_FILE")
  STATE=$(jq -r '.state' "$LOCK_FILE" 2>/dev/null || echo "")
  SESSION_ID=$(jq -r '.session_id' "$LOCK_FILE" 2>/dev/null || echo "")

  if [[ "$STATE" == "IMPLEMENTATION" && "$SESSION_ID" == "$CURRENT_SESSION" ]]; then
    echo "$TASK_DIR"
  fi
done)

if [ -z "$IMPLEMENTATION_TASKS" ]; then
  # No active IMPLEMENTATION tasks, allow operation
  exit 0
fi

# Check if file being written is a source file in task worktree (not agent worktree)
for TASK_DIR in $IMPLEMENTATION_TASKS; do
  TASK_WORKTREE="${TASK_DIR}/code"

  # Check if file path is within task worktree
  if [[ "$FILE_PATH" == "${TASK_WORKTREE}/"* ]]; then
    # Check if it's a source file
    if [[ "$FILE_PATH" =~ \.(java|ts|py|js|go|rs|cpp|c|h)$ ]]; then
      # Check if it's NOT in an agent worktree (agent worktrees are allowed)
      if [[ "$FILE_PATH" != *"/agents/"* ]]; then

        TASK_NAME=$(basename "$TASK_DIR")

        # ENHANCEMENT: Check if all agents are complete and state should transition
        ALL_AGENTS_COMPLETE=true
        AGENT_DIR="${TASK_DIR}/agents"

        if [[ -d "$AGENT_DIR" ]]; then
          for AGENT_STATUS in "$AGENT_DIR"/*/status.json; do
            if [[ -f "$AGENT_STATUS" ]]; then
              STATUS=$(jq -r '.status // "unknown"' "$AGENT_STATUS" 2>/dev/null)
              WORK=$(jq -r '.work_remaining // "unknown"' "$AGENT_STATUS" 2>/dev/null)

              if [[ "$STATUS" != "COMPLETE" || "$WORK" != "none" ]]; then
                ALL_AGENTS_COMPLETE=false
                break
              fi
            fi
          done
        fi

        # If all agents complete but state still IMPLEMENTATION, guide state transition
        if [[ "$ALL_AGENTS_COMPLETE" == "true" ]]; then
          echo "⚠️  WARNING: All agents complete but lock state not updated to VALIDATION"
          echo ""
          echo "REQUIRED: Update lock state first:"
          echo ""
          echo "  jq '.state = \"VALIDATION\"' ${TASK_DIR}/task.json > /tmp/lock.tmp && \\"
          echo "    mv /tmp/lock.tmp ${TASK_DIR}/task.json"
          echo ""
          echo "AFTER state update, Edit/Write will be permitted for minor fixes."
          echo ""
          exit 1
        fi

        echo "🚨🚨🚨 CRITICAL PROTOCOL VIOLATION DETECTED 🚨🚨🚨"
        echo ""
        echo "VIOLATION: Main agent attempting to create source file during IMPLEMENTATION state"
        echo ""
        echo "FILE: ${FILE_PATH}"
        echo "TASK: ${TASK_NAME}"
        echo "STATE: IMPLEMENTATION"
        echo "OPERATION: ${TOOL_NAME}"
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "PROTOCOL REQUIREMENT:"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        echo "During IMPLEMENTATION state:"
        echo "  • Main agent COORDINATES via Task tool"
        echo "  • Stakeholder agents IMPLEMENT in their worktrees"
        echo ""
        echo "Main agent MUST NOT:"
        echo "  ❌ Create source files (.java, .ts, .py, etc.)"
        echo "  ❌ Write code directly in task worktree"
        echo "  ❌ Use Write/Edit tools for implementation files"
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "REQUIRED ACTIONS:"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        echo "1. CANCEL this ${TOOL_NAME} operation"
        echo "2. Launch stakeholder agents via Task tool:"
        echo ""
        echo "   Task tool (technical-architect): Implement in /workspace/tasks/${TASK_NAME}/agents/technical-architect/code"
        echo "   Task tool (code-quality-auditor): Implement in /workspace/tasks/${TASK_NAME}/agents/code-quality-auditor/code"
        echo ""
        echo "3. Stakeholder agents will:"
        echo "   • Implement code in THEIR worktrees"
        echo "   • Run incremental validation"
        echo "   • Merge to task branch after validation"
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "REFERENCE:"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        echo "See CLAUDE.md § Implementation Role Boundaries"
        echo "See task-protocol-core.md § MULTI-AGENT IMPLEMENTATION WORKFLOW"
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""

        # BLOCK the operation
        exit 1
      fi
    fi
  fi
done

# Allow operation (not a source file in task worktree during IMPLEMENTATION)
exit 0
