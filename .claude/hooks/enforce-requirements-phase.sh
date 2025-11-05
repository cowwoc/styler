#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in enforce-requirements-phase.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Enforce REQUIREMENTS phase completion before SYNTHESIS state transition
#
# This hook ensures that stakeholder reviewers have been invoked and have
# produced requirement reports before the main agent creates an implementation plan.
#
# Triggered by: PreToolUse (when tool is TodoWrite or Bash with jq updating task.json)
# Purpose: Prevent skipping REQUIREMENTS phase

# Read stdin JSON from Claude Code
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // empty')
TOOL_INPUT=$(echo "$INPUT" | jq -r '.tool.input // empty')

# Only check when transitioning task state
if [ "$TOOL_NAME" != "Bash" ] && [ "$TOOL_NAME" != "Write" ]; then
  echo "$INPUT"
  exit 0
fi

# Check if this is a state transition to SYNTHESIS
# Pattern 1: jq command updating task.json state to SYNTHESIS
# Pattern 2: Write tool creating/updating task.json with SYNTHESIS state
IS_SYNTHESIS_TRANSITION=false

if [ "$TOOL_NAME" = "Bash" ]; then
  COMMAND=$(echo "$TOOL_INPUT" | jq -r '.command // empty')
  if echo "$COMMAND" | grep -qE "(jq.*\.state.*SYNTHESIS|task\.json.*SYNTHESIS)"; then
    IS_SYNTHESIS_TRANSITION=true
  fi
elif [ "$TOOL_NAME" = "Write" ]; then
  FILE_PATH=$(echo "$TOOL_INPUT" | jq -r '.file_path // empty')
  CONTENT=$(echo "$TOOL_INPUT" | jq -r '.content // empty')
  if [[ "$FILE_PATH" == */task.json ]] && echo "$CONTENT" | grep -q "SYNTHESIS"; then
    IS_SYNTHESIS_TRANSITION=true
  fi
fi

if [ "$IS_SYNTHESIS_TRANSITION" = false ]; then
  echo "$INPUT"
  exit 0
fi

# Extract task name from current working directory or task.json path
CWD=$(echo "$INPUT" | jq -r '.cwd // "/workspace/main"')
TASK_NAME=""

# Try to extract task name from cwd
if [[ "$CWD" =~ /workspace/tasks/([^/]+) ]]; then
  TASK_NAME="${BASH_REMATCH[1]}"
fi

# If not found in cwd, try to find task.json in current directory
if [ -z "$TASK_NAME" ] && [ -f "$CWD/task.json" ]; then
  TASK_NAME=$(jq -r '.task_name // empty' "$CWD/task.json")
fi

# If still not found, try parent directory
if [ -z "$TASK_NAME" ] && [ -f "$CWD/../task.json" ]; then
  TASK_NAME=$(jq -r '.task_name // empty' "$CWD/../task.json")
fi

if [ -z "$TASK_NAME" ]; then
  # Cannot determine task name, allow transition but warn
  echo "WARNING: Cannot determine task name to verify REQUIREMENTS phase completion" >&2
  echo "$INPUT"
  exit 0
fi

# Check for stakeholder requirement reports
TASK_DIR="/workspace/tasks/${TASK_NAME}"
if [ ! -d "$TASK_DIR" ]; then
  # Task directory doesn't exist yet, probably in INIT state
  echo "$INPUT"
  exit 0
fi

# Look for *-requirements.md files (stakeholder reports)
REQUIREMENT_REPORTS=$(find "$TASK_DIR" -maxdepth 1 -name "*-requirements.md" 2>/dev/null | wc -l)

if [ "$REQUIREMENT_REPORTS" -eq 0 ]; then
  # No stakeholder requirement reports found - BLOCK transition
  cat >&2 <<EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🚨 REQUIREMENTS PHASE VIOLATION - Transition Blocked
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CRITICAL: Attempted to transition to SYNTHESIS state without completing
REQUIREMENTS phase.

Current State: CLASSIFIED (inferred)
Attempted Transition: CLASSIFIED → SYNTHESIS
Task: ${TASK_NAME}

❌ MISSING: Stakeholder requirement reports

Expected files in ${TASK_DIR}/:
  - ${TASK_NAME}-architect-requirements.md
  - ${TASK_NAME}-engineer-requirements.md
  - ${TASK_NAME}-formatter-requirements.md

REQUIRED ACTIONS:

1. Stay in CLASSIFIED state
2. Invoke stakeholder agents in REQUIREMENTS mode IN PARALLEL:

   Task tool (architect): "Review task requirements from
   architectural perspective. Analyze dependencies, design patterns,
   integration points. Output: ${TASK_NAME}-architect-requirements.md"

   Task tool (engineer): "Review task requirements from quality
   perspective. Define testing strategy, quality metrics, validation criteria.
   Output: ${TASK_NAME}-engineer-requirements.md"

   Task tool (formatter): "Review task requirements from style perspective.
   Specify documentation requirements, code style standards, naming conventions.
   Output: ${TASK_NAME}-formatter-requirements.md"

3. After ALL reviewers complete, READ their reports
4. SYNTHESIZE unified implementation plan in task.md
5. THEN transition to SYNTHESIS state

📖 Protocol Reference:
   - CLAUDE.md § Mandatory Startup Protocol
   - docs/project/task-protocol-core.md § REQUIREMENTS Phase
   - docs/project/task-protocol-operations.md § Multi-Stakeholder Review

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
  exit 1
fi

# REQUIREMENTS phase complete - allow transition
echo "✅ REQUIREMENTS phase verified: Found $REQUIREMENT_REPORTS stakeholder report(s)" >&2
echo "$INPUT"
exit 0
