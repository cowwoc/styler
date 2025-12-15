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
		# IMPLEMENTATION requires user-approved-synthesis.flag AND detailed plan
		APPROVAL_FLAG="${TASK_DIR}/user-approved-synthesis.flag"
		TASK_MD="${TASK_DIR}/task.md"

		# First check: Plan quality validation (before user approval check)
		if [[ -f "$TASK_MD" ]]; then
			PLAN_ISSUES=""

			# Required sections per task-protocol-core.md § Required Plan Components
			if ! grep -qiE "^#+ .*file.*manifest|^#+ .*files to (create|modify)" "$TASK_MD"; then
				PLAN_ISSUES="$PLAN_ISSUES\n- Missing FILE MANIFEST section"
			fi
			if ! grep -qiE "^#+ .*api.*specification|^#+ .*api.*design|^#+ .*interface|^#+ .*method signature" "$TASK_MD"; then
				PLAN_ISSUES="$PLAN_ISSUES\n- Missing API SPECIFICATIONS section"
			fi
			if ! grep -qiE "^#+ .*behavior|^#+ .*scenario" "$TASK_MD"; then
				PLAN_ISSUES="$PLAN_ISSUES\n- Missing BEHAVIORAL SPECIFICATIONS section"
			fi
			if ! grep -qiE "^#+ .*test (specification|case|scenario)" "$TASK_MD"; then
				PLAN_ISSUES="$PLAN_ISSUES\n- Missing TEST SPECIFICATIONS section"
			fi
			if ! grep -qiE "^#+ .*decision" "$TASK_MD"; then
				PLAN_ISSUES="$PLAN_ISSUES\n- Missing DECISION LOG section"
			fi

			# Anti-pattern: Test counts instead of specific test cases
			if grep -qiE "(minimum|required|must have|at least).*(test count|[0-9]+ tests)" "$TASK_MD" || \
			   grep -qiE "(total|grand total):?\s*[0-9]+ tests?" "$TASK_MD" || \
			   grep -qiE "\b[0-9]+ tests?\b.*(minimum|required|mandatory)" "$TASK_MD"; then
				PLAN_ISSUES="$PLAN_ISSUES\n- Contains TEST COUNTS instead of specific test cases (prohibited)"
			fi

			# Test cases should have specific names and expected outputs
			if grep -qiE "^#+ .*test (specification|case)" "$TASK_MD"; then
				# Check if there are test case tables with Name|Input|Expected format
				if ! grep -qE "\|.*test.*name.*\||\|.*input.*\|.*expected|\|.*scenario.*\|" "$TASK_MD"; then
					# Also check for bullet-list format test cases
					if ! grep -qE "^\s*[-*]\s*\`?[a-zA-Z]+.*\`?:?\s*(input|when|given|should|returns|expects)" "$TASK_MD"; then
						PLAN_ISSUES="$PLAN_ISSUES\n- TEST SPECIFICATIONS lacks specific test cases (need Name|Input|Expected format)"
					fi
				fi
			fi

			if [[ -n "$PLAN_ISSUES" ]]; then
				VIOLATION_FOUND=true
				VIOLATION_MESSAGE="## 🚨 STATE TRANSITION BLOCKED - INSUFFICIENT PLAN DETAIL

**Task**: \`$TASK_NAME\`
**Attempted Transition**: \`$CURRENT_STATE\` → \`$NEW_STATE\`
**Violation**: Implementation plan lacks required detail for mechanical implementation

## ⚠️ PLAN QUALITY VALIDATION FAILED

Issues found in task.md:
$PLAN_ISSUES

## 📋 REQUIRED PLAN COMPONENTS (task-protocol-core.md § Required Plan Components)

1. **FILE MANIFEST** - Complete list of files to create/modify with paths
2. **API SPECIFICATIONS** - Exact method signatures with JavaDoc summaries
3. **BEHAVIORAL SPECIFICATIONS** - How code behaves in specific scenarios (tables)
4. **TEST SPECIFICATIONS** - Specific test cases with Name|Input|Expected format
5. **DECISION LOG** - Decisions already made (NOT for implementation phase)

## ❌ PROHIBITED PATTERNS

- \"\${BOLD}36 tests required\${NORMAL}\" - Mandates counts instead of coverage
- \"\${BOLD}Minimum 31 mandatory tests\${NORMAL}\" - Focuses on quantity over quality
- Missing specific test case definitions

## ✅ REQUIRED TEST SPECIFICATION FORMAT

\`\`\`markdown
| Test Name | Input | Expected |
|-----------|-------|----------|
| \`shouldReturnEmptyForNullInput\` | null | Empty list |
| \`shouldFindExistingClass\` | \"java.util.List\" | true |
\`\`\`

## 🎯 GOAL

Implementation must be MECHANICAL - NO significant decisions should remain.
User approves the WHAT and HOW before any code is written.

**ACTION**: Update task.md to include missing sections with sufficient detail,
then retry state transition.

## Protocol Reference

See: /workspace/main/docs/project/task-protocol-core.md § Detailed Implementation Plan Requirements"
			fi
		fi

		# Second check: User approval flag (only if plan passes quality check)
		if [[ "$VIOLATION_FOUND" != "true" ]] && [[ ! -f "$APPROVAL_FLAG" ]]; then
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
