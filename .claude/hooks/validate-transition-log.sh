#!/bin/bash
# PostToolUse hook: Validate transition_log is consistent with state in task.json
#
# ADDED: 2025-12-03 after main agent updated state without updating transition_log
# during implement-import-organization task. Final transitions (AWAITING_USER_APPROVAL
# → COMPLETE → CLEANUP) only updated .state field, leaving transition_log incomplete.
#
# PREVENTS: Incomplete audit trail in task.json when state changes
# TRIGGERING THOUGHT: Agent thought "Update task state to COMPLETE" and only
# changed .state field without using full transition pattern or state-transition skill.

set -euo pipefail

# Only runs for Bash tool after task.json modifications
# Read tool context from stdin
CONTEXT=$(cat)

# FIXED: Use correct field names (tool_name, not tool.name)
TOOL_NAME=$(echo "$CONTEXT" | jq -r '.tool_name // empty' 2>/dev/null)
TOOL_INPUT=$(echo "$CONTEXT" | jq -r '.tool_input.command // empty' 2>/dev/null)
TOOL_OUTPUT=$(echo "$CONTEXT" | jq -r '.tool_result.stdout // empty' 2>/dev/null)

# Only check Bash commands that touched task.json
if [[ "$TOOL_NAME" != "Bash" ]]; then
    exit 0
fi

if [[ "$TOOL_INPUT" != *"task.json"* ]] && [[ "$TOOL_INPUT" != *".state"* ]]; then
    exit 0
fi

# Skip read-only commands
if [[ "$TOOL_INPUT" == *"cat "* ]] || [[ "$TOOL_INPUT" == *"jq -r"* ]] || [[ "$TOOL_INPUT" == *"ls "* ]]; then
    # Check if it's just reading (no redirect/pipe to file)
    if [[ "$TOOL_INPUT" != *"> "* ]] && [[ "$TOOL_INPUT" != *"| mv"* ]] && [[ "$TOOL_INPUT" != *"&& mv"* ]]; then
        exit 0
    fi
fi

# Find task.json files that might have been modified
for TASK_DIR in /workspace/tasks/*/; do
    TASK_JSON="${TASK_DIR}task.json"
    if [[ ! -f "$TASK_JSON" ]]; then
        continue
    fi

    # Get current state
    CURRENT_STATE=$(jq -r '.state // empty' "$TASK_JSON" 2>/dev/null)
    if [[ -z "$CURRENT_STATE" ]]; then
        continue
    fi

    # Get last transition in log
    LAST_TRANSITION_TO=$(jq -r '.transition_log[-1].to // empty' "$TASK_JSON" 2>/dev/null)

    # If transition_log is empty but state is not INIT, that's a problem
    if [[ -z "$LAST_TRANSITION_TO" ]] && [[ "$CURRENT_STATE" != "INIT" ]]; then
        echo "" >&2
        echo "⚠️ WARNING: State transition without transition_log update detected!" >&2
        echo "" >&2
        echo "Task: $(basename "$TASK_DIR")" >&2
        echo "Current state: $CURRENT_STATE" >&2
        echo "Last transition_log entry: (none)" >&2
        echo "" >&2
        echo "FIX REQUIRED: Update task.json with proper transition:" >&2
        echo "" >&2
        echo "  jq --arg old \"PREVIOUS_STATE\" --arg new \"$CURRENT_STATE\" \\" >&2
        echo "     --arg ts \"\$(date -u +%Y-%m-%dT%H:%M:%SZ)\" \\" >&2
        echo "     '.transition_log += [{\"from\": \$old, \"to\": \$new, \"timestamp\": \$ts}]' \\" >&2
        echo "     $TASK_JSON > tmp.json && mv tmp.json $TASK_JSON" >&2
        echo "" >&2
        echo "Or use the state-transition skill for safe transitions." >&2
        echo "" >&2
        continue
    fi

    # Check if current state matches last transition
    if [[ -n "$LAST_TRANSITION_TO" ]] && [[ "$CURRENT_STATE" != "$LAST_TRANSITION_TO" ]]; then
        echo "" >&2
        echo "⚠️ WARNING: State/transition_log mismatch detected!" >&2
        echo "" >&2
        echo "Task: $(basename "$TASK_DIR")" >&2
        echo "Current state: $CURRENT_STATE" >&2
        echo "Last transition_log entry: → $LAST_TRANSITION_TO" >&2
        echo "" >&2
        echo "This means state was changed without updating transition_log." >&2
        echo "" >&2
        echo "FIX REQUIRED: Add missing transition(s) to transition_log:" >&2
        echo "" >&2
        echo "  jq --arg old \"$LAST_TRANSITION_TO\" --arg new \"$CURRENT_STATE\" \\" >&2
        echo "     --arg ts \"\$(date -u +%Y-%m-%dT%H:%M:%SZ)\" \\" >&2
        echo "     '.transition_log += [{\"from\": \$old, \"to\": \$new, \"timestamp\": \$ts}]' \\" >&2
        echo "     $TASK_JSON > tmp.json && mv tmp.json $TASK_JSON" >&2
        echo "" >&2
        echo "For future transitions, use the state-transition skill:" >&2
        echo "  Skill: state-transition" >&2
        echo "" >&2
    fi
done

exit 0
