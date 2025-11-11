#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in task-complete.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# This hook runs after Task tool completes to provide efficiency guidance
# Purpose: Remind main agent to validate incrementally (fail-fast pattern)

# Extract tool input from stdin
TOOL_INPUT=$(cat)

# Check if an agent just completed work
if [[ -d /workspace/tasks ]]; then
  for TASK_DIR in /workspace/tasks/*/; do
    [[ -d "$TASK_DIR" ]] || continue

    TASK_JSON="$TASK_DIR/task.json"
    [[ -f "$TASK_JSON" ]] || continue

    TASK_STATE=$(jq -r '.state // "UNKNOWN"' "$TASK_JSON" 2>/dev/null || echo "UNKNOWN")

    # Only provide reminder during IMPLEMENTATION state
    if [[ "$TASK_STATE" == "IMPLEMENTATION" ]]; then
      # Check for agent status files indicating completion
      for STATUS_FILE in "$TASK_DIR"*-status.json; do
        [[ -f "$STATUS_FILE" ]] || continue

        STATUS=$(jq -r '.status // "unknown"' "$STATUS_FILE" 2>/dev/null || echo "unknown")
        AGENT=$(basename "$STATUS_FILE" | sed 's/-status.json$//')

        if [[ "$STATUS" == "complete" ]]; then
          TASK_NAME=$(basename "$TASK_DIR")

          echo "
💡 EFFICIENCY TIP: Validate NOW before merging next agent

   cd /workspace/tasks/$TASK_NAME/code
   ./mvnw checkstyle:check -pl :\${module-name}

   Why: Fresh context enables 20% faster fixes than late validation

   Evidence: Session 3fa4e964 (implement-formatter-api) found 22 violations
   late, requiring 11 fix iterations. Early validation catches issues while
   agent context is still fresh in memory.

   Pattern: Merge agent → Validate → Fix → Next agent
" >&2

          # Only show reminder once per task
          break 2
        fi
      done
    fi
  done
fi
