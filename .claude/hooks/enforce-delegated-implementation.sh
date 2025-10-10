#!/usr/bin/env bash
# Hook: EnforceDelegatedImplementation
# Trigger: UserPromptSubmit
# Purpose: Prevent direct implementation when delegated protocol is active
#
# This hook detects when context.md indicates delegated implementation protocol
# and blocks direct Write/Edit tool usage for implementation files

# Parse hook input
INPUT=$(cat)
USER_PROMPT=$(echo "$INPUT" | jq -r '.user_prompt // empty')

# Get current working directory to find task root
CURRENT_DIR=$(pwd)

# Only run if we're in a task worktree
if [[ ! "$CURRENT_DIR" =~ /workspace/branches/[^/]+/code ]]; then
    exit 0
fi

# Extract task name from path
TASK_NAME=$(echo "$CURRENT_DIR" | sed 's|.*/workspace/branches/\([^/]*\)/code.*|\1|')
LOCK_FILE="/workspace/locks/${TASK_NAME}.json"
CONTEXT_FILE="../context.md"

# Only run if we have an active task
if [[ -z "$TASK_NAME" ]] || [[ ! -f "$LOCK_FILE" ]]; then
    exit 0
fi

# Check if context.md exists and indicates delegated implementation
if [[ ! -f "$CONTEXT_FILE" ]]; then
    exit 0
fi

# Check if context.md has "Agent Work Assignments" section (indicates delegated protocol)
if ! grep -q "## Agent Work Assignments" "$CONTEXT_FILE" 2>/dev/null; then
    exit 0
fi

# Check current lock state
CURRENT_STATE=$(jq -r '.state // "UNKNOWN"' "$LOCK_FILE" 2>/dev/null)

# States where delegated implementation should be active
case "$CURRENT_STATE" in
    AUTONOMOUS_IMPLEMENTATION)
        # Check if user prompt contains Write/Edit tool usage for src files
        if echo "$USER_PROMPT" | grep -qE 'Write.*src/(main|test)/java|Edit.*src/(main|test)/java'; then
            MESSAGE="## 🚨 DELEGATED IMPLEMENTATION PROTOCOL VIOLATION DETECTED

**Current State**: $CURRENT_STATE
**Lock File**: $LOCK_FILE
**Context File**: $CONTEXT_FILE

**VIOLATION**: Direct Write/Edit usage detected for implementation files.

**Expected Behavior**:
During AUTONOMOUS_IMPLEMENTATION state, implementation MUST be performed by
delegated agents, not direct tool usage.

**Required Actions**:
1. READ context.md to understand agent work assignments:
   \`\`\`bash
   grep -A 50 \"## Agent Work Assignments\" $CONTEXT_FILE
   \`\`\`

2. INVOKE implementation agents in parallel using Task tool

3. WAIT for agent completion

4. MOVE to CONVERGENCE state after agents complete

**Correct Pattern (Agent Invocation)**:
\`\`\`
Use Task tool with:
- subagent_type: \"technical-architect\" (or other assigned agent)
- prompt: \"Implement [component] according to context.md work assignment\"
\`\`\`

**Recovery if Agent Implementation Failed**:
If delegated protocol is broken/not working:
1. Document the failure in context.md
2. Update state to CONVERGENCE:
   \`jq '.state = \"CONVERGENCE\"' $LOCK_FILE > /tmp/lock.json && mv /tmp/lock.json $LOCK_FILE\`
3. Implement directly with full documentation of why delegation failed
4. Ensure REVIEW agents validate the direct implementation

See: docs/project/delegated-implementation-protocol.md for complete guidance"

            jq -n \
              --arg event "UserPromptSubmit" \
              --arg context "$MESSAGE" \
              '{
                "hookSpecificOutput": {
                  "hookEventName": $event,
                  "additionalContext": $context
                }
              }'
            exit 1
        fi
        ;;
esac

exit 0
