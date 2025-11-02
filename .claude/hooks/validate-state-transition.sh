#!/bin/bash
# Pre-Tool-Use Hook: Validate state transitions require proper artifacts
#
# ADDED: 2025-11-02 to prevent state transitions without required outputs
#
# PREVENTS: State transitions without creating required artifacts (task.md, flags)
# ENFORCES: Each state transition requires its mandatory outputs before proceeding
#
# Triggers: PreToolUse (tool:Edit && path:**/task.json matcher in settings.json)

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in validate-state-transition.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")
TOOL_PARAMS=$(echo "$INPUT" | jq -r '.tool_input // {}' 2>/dev/null || echo "{}")

# Only validate Edit tool on task.json files
if [[ "$TOOL_NAME" != "Edit" ]]; then
	exit 0
fi

FILE_PATH=$(echo "$TOOL_PARAMS" | jq -r '.file_path // empty' 2>/dev/null || echo "")
if [[ ! "$FILE_PATH" =~ task\.json$ ]]; then
	exit 0
fi

# Extract new state from new_string parameter
NEW_STRING=$(echo "$TOOL_PARAMS" | jq -r '.new_string // empty' 2>/dev/null || echo "")
if [[ -z "$NEW_STRING" ]]; then
	exit 0
fi

# Parse new state from replacement string (format: "state": "VALUE")
# For Edit tool, new_string is the replacement text, not full JSON
if [[ "$NEW_STRING" =~ \"state\":[[:space:]]*\"([^\"]+)\" ]]; then
	NEW_STATE="${BASH_REMATCH[1]}"
else
	# Not a state field edit, allow
	exit 0
fi

log_hook_start "validate-state-transition" "PreToolUse"

# Get current state from file
CURRENT_STATE=$(jq -r '.state // "UNKNOWN"' "$FILE_PATH" 2>/dev/null || echo "UNKNOWN")

# If state isn't changing, allow
if [[ "$NEW_STATE" == "$CURRENT_STATE" ]]; then
	log_hook_success "validate-state-transition" "PreToolUse" "State not changing, allowing"
	exit 0
fi

# Get task directory and name
TASK_DIR=$(dirname "$FILE_PATH")
TASK_NAME=$(basename "$TASK_DIR")

# Validate state-specific requirements
VIOLATION_FOUND=false
VIOLATION_MESSAGE=""

case "$NEW_STATE" in
	CLASSIFIED)
		# CLASSIFIED requires task.md exists
		TASK_MD="${TASK_DIR}/task.md"
		if [[ ! -f "$TASK_MD" ]]; then
			VIOLATION_FOUND=true
			VIOLATION_MESSAGE="## 🚨 STATE TRANSITION BLOCKED

**Task**: \`$TASK_NAME\`
**Attempted Transition**: \`$CURRENT_STATE\` → \`$NEW_STATE\`
**Violation**: CLASSIFIED state requires task.md with stakeholder requirements

## ⚠️ CRITICAL - MISSING REQUIRED ARTIFACT

You attempted to transition to CLASSIFIED state without creating task.md.

**AUTOMATIC ACTION TAKEN**:
- State transition blocked
- Violation logged

**REQUIRED ACTION - Create task.md**:

\`\`\`bash
cat > ${TASK_DIR}/task.md <<EOF_INNER
# Task: ${TASK_NAME}

## Requirements

[Define what stakeholder agents should implement]

## Acceptance Criteria

[Define success criteria for this task]
EOF_INNER
\`\`\`

**After creating task.md, retry state transition**:
\`\`\`bash
jq '.state = \"CLASSIFIED\"' ${FILE_PATH} > /tmp/task.tmp
mv /tmp/task.tmp ${FILE_PATH}
\`\`\`

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § INIT → CLASSIFIED

**Why This Matters**:
- task.md communicates requirements to stakeholder agents
- Ensures requirements are documented before agent invocation
- Prevents ad-hoc requirement passing via Task tool prompts"
		fi
		;;

	IMPLEMENTATION)
		# IMPLEMENTATION requires user-approved-synthesis.flag
		APPROVAL_FLAG="${TASK_DIR}/user-approved-synthesis.flag"
		if [[ ! -f "$APPROVAL_FLAG" ]]; then
			VIOLATION_FOUND=true
			VIOLATION_MESSAGE="## 🚨 STATE TRANSITION BLOCKED

**Task**: \`$TASK_NAME\`
**Attempted Transition**: \`$CURRENT_STATE\` → \`$NEW_STATE\`
**Violation**: IMPLEMENTATION state requires user approval of synthesis plan

## ⚠️ CRITICAL - MISSING USER APPROVAL CHECKPOINT

You attempted to transition to IMPLEMENTATION without user approval.

**AUTOMATIC ACTION TAKEN**:
- State transition blocked
- Violation logged

**REQUIRED ACTION - Get User Approval**:

1. **Present implementation plan to user**:
   - Show task.md synthesis plan
   - Explain approach and architecture

2. **WAIT for explicit user approval**:
   - User must say \"approved\", \"proceed\", \"looks good\", or \"LGTM\"
   - Do NOT proceed without approval

3. **After receiving approval, create approval flag**:
   \`\`\`bash
   touch ${APPROVAL_FLAG}
   \`\`\`

4. **Then retry state transition**:
   \`\`\`bash
   jq '.state = \"IMPLEMENTATION\"' ${FILE_PATH} > /tmp/task.tmp
   mv /tmp/task.tmp ${FILE_PATH}
   \`\`\`

## Protocol Reference

See: /workspace/main/CLAUDE.md § MANDATORY USER APPROVAL CHECKPOINTS

**Why This Matters**:
- User approval checkpoints ensure alignment before implementation
- Prevents wasted effort on wrong approach
- Required by task protocol state machine"
		fi
		;;

	COMPLETE)
		# COMPLETE requires user-approved-changes.flag
		APPROVAL_FLAG="${TASK_DIR}/user-approved-changes.flag"
		if [[ ! -f "$APPROVAL_FLAG" ]]; then
			VIOLATION_FOUND=true
			VIOLATION_MESSAGE="## 🚨 STATE TRANSITION BLOCKED

**Task**: \`$TASK_NAME\`
**Attempted Transition**: \`$CURRENT_STATE\` → \`$NEW_STATE\`
**Violation**: COMPLETE state requires user approval of changes

## ⚠️ CRITICAL - MISSING USER APPROVAL CHECKPOINT

You attempted to transition to COMPLETE without user approval.

**AUTOMATIC ACTION TAKEN**:
- State transition blocked
- Violation logged

**REQUIRED ACTION - Get User Approval**:

1. **Present changes to user**:
   \`\`\`bash
   git diff --stat main...${TASK_NAME}
   git log --oneline main..${TASK_NAME}
   \`\`\`

2. **WAIT for explicit user approval**:
   - User must say \"approved\", \"merge it\", \"LGTM\", or \"looks good\"
   - Do NOT proceed without approval

3. **After receiving approval, create approval flag**:
   \`\`\`bash
   touch ${APPROVAL_FLAG}
   \`\`\`

4. **Then retry state transition**:
   \`\`\`bash
   jq '.state = \"COMPLETE\"' ${FILE_PATH} > /tmp/task.tmp
   mv /tmp/task.tmp ${FILE_PATH}
   \`\`\`

## Protocol Reference

See: /workspace/main/CLAUDE.md § MANDATORY USER APPROVAL CHECKPOINTS

**Why This Matters**:
- User approval ensures changes meet expectations before merge
- Prevents unwanted changes reaching main branch
- Required by task protocol state machine"
		fi
		;;
esac

if [[ "$VIOLATION_FOUND" == true ]]; then
	log_hook_blocked "validate-state-transition" "PreToolUse" "State transition blocked - missing required artifact"
	output_hook_error "PreToolUse" "$VIOLATION_MESSAGE"
	exit 0
fi

log_hook_success "validate-state-transition" "PreToolUse" "State transition validated"
exit 0
