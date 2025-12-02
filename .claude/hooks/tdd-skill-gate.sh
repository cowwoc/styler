#!/bin/bash
# TDD Skill Gate - Blocks production Java code edits without active TDD mode
#
# ADDED: 2025-11-28 based on temp/.claude tdd-implementation skill
# PURPOSE: Enforce TDD workflow by physically blocking production code edits
#          unless TDD mode is active with verified state transitions
#
# This hook implements the gatekeeper pattern from temp/.claude/skills/tdd-implementation
# which proved more effective than passive documentation.

set -euo pipefail
trap 'echo "ERROR in tdd-skill-gate.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read stdin for hook context
INPUT=$(cat)

# Extract tool name and file path
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // empty')
FILE_PATH=$(echo "$INPUT" | jq -r '.tool.input.file_path // .tool.input.path // empty')
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Only check Write and Edit tools
if [[ "$TOOL_NAME" != "Write" && "$TOOL_NAME" != "Edit" ]]; then
    exit 0
fi

# Only check Java production code files
if [[ -z "$FILE_PATH" ]]; then
    exit 0
fi

# Check if this is a production Java file (src/main/java)
if [[ ! "$FILE_PATH" =~ src/main/java/.*\.java$ ]]; then
    exit 0
fi

# Skip infrastructure files that don't need TDD
if [[ "$FILE_PATH" =~ (module-info|package-info)\.java$ ]]; then
    exit 0
fi

# Check for active TDD mode file
TDD_MODE_FILE="/tmp/tdd_skill_active_${SESSION_ID}"

if [[ ! -f "$TDD_MODE_FILE" ]]; then
    # No TDD mode active - provide helpful error
    cat >&2 << EOF

❌ TDD SKILL GATE - BLOCKED ❌
═══════════════════════════════════════════════════════════════════════════════

Production Java code edit attempted without active TDD mode.

File: $FILE_PATH
Tool: $TOOL_NAME

To proceed, you MUST invoke the test-driven-bug-fix skill first:

    Skill: test-driven-bug-fix

This ensures:
1. Test is written FIRST (fails initially)
2. Implementation makes test pass
3. No regressions introduced

─────────────────────────────────────────────────────────────────────────────────
To initialize TDD mode manually (for the current session):

    cat > /tmp/tdd_skill_active_${SESSION_ID} << 'TDDEOF'
{
  "phase": "RED",
  "session_id": "${SESSION_ID}",
  "target_class": "YOUR_CLASS_NAME",
  "test_class": "YOUR_CLASS_NAMETest",
  "test_failed": false,
  "test_passed": false,
  "started_at": "$(date -Iseconds)"
}
TDDEOF

─────────────────────────────────────────────────────────────────────────────────

Session ID: ${SESSION_ID:-UNKNOWN}
Required file: $TDD_MODE_FILE

═══════════════════════════════════════════════════════════════════════════════
EOF

    # Return deny decision
    echo '{"decision": "block", "reason": "TDD mode not active. Invoke test-driven-bug-fix skill first."}'
    exit 2
fi

# TDD mode file exists - verify session ID matches
FILE_SESSION_ID=$(jq -r '.session_id // empty' "$TDD_MODE_FILE" 2>/dev/null || echo "")

if [[ "$FILE_SESSION_ID" != "$SESSION_ID" && -n "$SESSION_ID" ]]; then
    cat >&2 << EOF

❌ TDD SESSION MISMATCH - BLOCKED ❌
═══════════════════════════════════════════════════════════════════════════════

TDD mode file exists but belongs to a different session.

Current session: $SESSION_ID
File session: $FILE_SESSION_ID

This happens when a previous session's TDD mode wasn't cleaned up.

To fix, remove stale file and reinitialize:

    rm -f /tmp/tdd_skill_active_*
    Skill: test-driven-bug-fix

═══════════════════════════════════════════════════════════════════════════════
EOF

    echo '{"decision": "block", "reason": "TDD mode session mismatch. Clean up stale files."}'
    exit 2
fi

# Check TDD phase - must be GREEN or REFACTOR to edit production code
PHASE=$(jq -r '.phase // empty' "$TDD_MODE_FILE" 2>/dev/null || echo "")
TEST_FAILED=$(jq -r '.test_failed // false' "$TDD_MODE_FILE" 2>/dev/null || echo "false")

if [[ "$PHASE" == "RED" ]]; then
    if [[ "$TEST_FAILED" != "true" ]]; then
        cat >&2 << EOF

⚠️ TDD RED PHASE - TEST NOT YET FAILED ⚠️
═══════════════════════════════════════════════════════════════════════════════

You're in RED phase but haven't verified the test fails yet.

Before editing production code, you MUST:
1. Write the failing test
2. Run the test to verify it FAILS
3. Update TDD mode file with test_failed: true

Run your test:
    cd /workspace/main && mvn test -Dtest=YourTestClass#testMethod

Then update TDD mode:
    jq '.test_failed = true | .phase = "GREEN"' $TDD_MODE_FILE > ${TDD_MODE_FILE}.tmp && mv ${TDD_MODE_FILE}.tmp $TDD_MODE_FILE

═══════════════════════════════════════════════════════════════════════════════
EOF

        echo '{"decision": "block", "reason": "RED phase: Test must fail before implementing fix."}'
        exit 2
    fi
fi

# All checks passed - allow the edit
exit 0
