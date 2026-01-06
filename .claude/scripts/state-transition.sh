#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in state-transition.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

# Usage: state-transition.sh TASK_NAME TO_STATE
#
# ENHANCED: 2026-01-06 to validate AWAITING_USER_APPROVAL prerequisites
# PREVENTS: Transition to AWAITING_USER_APPROVAL without pre-presentation cleanup
# ROOT CAUSE: fix-classpath-scanner-per-file-overhead mistake - agent transitioned
# without removing agent worktrees/branches, violating pre-presentation cleanup requirement

if [ $# -lt 2 ]; then
    json_error "Usage: state-transition.sh TASK_NAME TO_STATE"
fi

TASK_NAME="$1"
TO_STATE="$2"
TASK_DIR="/workspace/tasks/$TASK_NAME"
TIMESTAMP=$(date -Iseconds)

# Verify task exists
[ -f "$TASK_DIR/task.json" ] || json_error "task.json not found: $TASK_DIR/task.json"

# Get current state
FROM_STATE=$(jq -r '.state' "$TASK_DIR/task.json")

# Validate AWAITING_USER_APPROVAL prerequisites (per pre-presentation-cleanup skill)
if [ "$TO_STATE" = "AWAITING_USER_APPROVAL" ]; then
    TASK_WORKTREE="${TASK_DIR}/code"
    CLEANUP_ISSUES=""

    if [ -d "$TASK_WORKTREE" ]; then
        # Check commit count (should be 1 after squashing)
        COMMIT_COUNT=$(cd "$TASK_WORKTREE" && git rev-list --count main..HEAD 2>/dev/null || echo "0")
        if [ "$COMMIT_COUNT" -ne 1 ]; then
            CLEANUP_ISSUES="$CLEANUP_ISSUES\n- Task branch has $COMMIT_COUNT commits (should be 1 - use git-squash skill)"
        fi

        # Check for agent branches (should be deleted)
        AGENT_BRANCHES=$(cd "$TASK_WORKTREE" && git branch --list "*${TASK_NAME}-*" 2>/dev/null | tr -d ' ' || true)
        if [ -n "$AGENT_BRANCHES" ]; then
            CLEANUP_ISSUES="$CLEANUP_ISSUES\n- Agent branches still exist: $(echo "$AGENT_BRANCHES" | tr '\n' ' ')"
        fi

        # Check for agent worktrees (should be deleted)
        for agent in architect tester formatter engineer builder; do
            AGENT_WORKTREE="${TASK_DIR}/agents/${agent}/code"
            if [ -d "$AGENT_WORKTREE" ]; then
                CLEANUP_ISSUES="$CLEANUP_ISSUES\n- Agent worktree still exists: ${agent}"
            fi
        done

        if [ -n "$CLEANUP_ISSUES" ]; then
            echo "🚨 STATE TRANSITION BLOCKED - PRE-APPROVAL CLEANUP REQUIRED" >&2
            echo "" >&2
            echo "Task: $TASK_NAME" >&2
            echo "Attempted Transition: $FROM_STATE → $TO_STATE" >&2
            echo "" >&2
            echo "Cleanup issues found:" >&2
            echo -e "$CLEANUP_ISSUES" >&2
            echo "" >&2
            echo "✅ REQUIRED: Run pre-presentation-cleanup skill or follow these steps:" >&2
            echo "" >&2
            echo "1. Remove agent worktrees:" >&2
            echo "   for agent in architect tester formatter engineer builder; do" >&2
            echo "     git worktree remove ${TASK_DIR}/agents/\${agent}/code --force 2>/dev/null || true" >&2
            echo "   done" >&2
            echo "" >&2
            echo "2. Delete agent branches:" >&2
            echo "   cd /workspace/main" >&2
            echo "   for branch in \$(git branch --list '*${TASK_NAME}-*'); do" >&2
            echo "     git branch -D \"\$branch\" 2>/dev/null || true" >&2
            echo "   done" >&2
            echo "" >&2
            echo "3. Squash commits (use git-squash skill):" >&2
            echo "   cd ${TASK_WORKTREE}" >&2
            echo "" >&2
            echo "4. Verify cleanup:" >&2
            echo "   git rev-list --count main..HEAD  # Should output: 1" >&2
            echo "   git branch | grep '${TASK_NAME}'   # Should show ONLY task branch" >&2
            json_error "Cleanup required before AWAITING_USER_APPROVAL transition"
        fi
    fi
fi

# Get last transition timestamp for duration calculation
LAST_TRANSITION=$(jq -r '.transition_log[-1].timestamp // .created' "$TASK_DIR/task.json")
if [ -n "$LAST_TRANSITION" ] && [ "$LAST_TRANSITION" != "null" ]; then
    LAST_TS=$(date -d "$LAST_TRANSITION" +%s)
    CURRENT_TS=$(date +%s)
    DURATION_SECONDS=$((CURRENT_TS - LAST_TS))
else
    DURATION_SECONDS=0
fi

# Update state with transition record
# FIXED: Use .transition_log field (not .transitions) to match require-task-protocol.sh validation
jq --arg from "$FROM_STATE" --arg to "$TO_STATE" --arg ts "$TIMESTAMP" --arg dur "$DURATION_SECONDS" \
   '.state = $to | .transition_log += [{"from": $from, "to": $to, "timestamp": $ts, "duration_seconds": ($dur | tonumber)}]' \
   "$TASK_DIR/task.json" > "$TASK_DIR/task.json.tmp"
mv "$TASK_DIR/task.json.tmp" "$TASK_DIR/task.json"

# Verify state updated
NEW_STATE=$(jq -r '.state' "$TASK_DIR/task.json")
if [ "$NEW_STATE" != "$TO_STATE" ]; then
    json_error "State transition failed. Expected: $TO_STATE, Got: $NEW_STATE"
fi

json_success "State transition successful" "$(jq -n --arg task "$TASK_NAME" --arg from "$FROM_STATE" --arg to "$TO_STATE" --arg dur "$DURATION_SECONDS" '{task_name: $task, from_state: $from, to_state: $to, duration_in_previous_state: ($dur | tonumber)}')"
