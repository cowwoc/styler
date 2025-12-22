#!/bin/bash
# TDD Bash Guard - Blocks Bash commands that modify production Java files without TDD mode
#
# ADDED: 2025-12-01 to close TDD enforcement gap where hooks blocking only Write/Edit
#        can be bypassed via Bash commands like `cat > file.java`, `sed -i`, etc.
#
# PURPOSE: Close the TDD enforcement gap for CLI workarounds
#
# DETECTION: Checks for patterns that could modify production Java files:
#   - Redirections: cat > file.java, echo >> file.java
#   - In-place edits: sed -i, awk -i
#   - Move/copy: mv *.java, cp *.java
#   - Direct writes: tee file.java
#
# SKIP CONDITIONS:
#   - Test files (src/test/)
#   - Infrastructure files (module-info.java, package-info.java)
#   - TDD mode is active
#   - Git conflict resolution (rebase/merge in progress)
#   - Git conflict resolution commands (checkout --ours/--theirs, add)

set -euo pipefail
trap 'echo "ERROR in tdd-bash-guard.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper for proper hook blocking
source /workspace/.claude/scripts/json-output.sh

# Read stdin for hook context
INPUT=$(cat)

# Extract tool name
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // .tool.name // empty' 2>/dev/null || echo "")

# Only check Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
    exit 0
fi

# Extract command
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // .tool.input.command // empty' 2>/dev/null || echo "")
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty' 2>/dev/null || echo "")

if [[ -z "$COMMAND" ]]; then
    exit 0
fi

# Skip if command doesn't involve Java files
if [[ ! "$COMMAND" =~ \.java ]]; then
    exit 0
fi

# Skip if only targeting test files
if [[ "$COMMAND" =~ src/test/ ]] && [[ ! "$COMMAND" =~ src/main/ ]]; then
    exit 0
fi

# Skip if only targeting infrastructure files
if [[ "$COMMAND" =~ (module-info|package-info)\.java ]] && \
   [[ ! "$COMMAND" =~ [^-]info\.java ]]; then
    exit 0
fi

# Skip if in git rebase/merge state (conflict resolution)
GIT_DIR=$(git rev-parse --git-dir 2>/dev/null || echo "")
if [[ -n "$GIT_DIR" ]]; then
    if [[ -d "$GIT_DIR/rebase-merge" ]] || [[ -d "$GIT_DIR/rebase-apply" ]] || [[ -f "$GIT_DIR/MERGE_HEAD" ]]; then
        exit 0
    fi
fi

# Skip git conflict resolution commands
if [[ "$COMMAND" =~ git\ (checkout|add|restore|reset)\ .*(--ours|--theirs|\.java) ]]; then
    exit 0
fi

# Skip read-only operations (grep, cat for viewing, diff)
if [[ "$COMMAND" =~ ^(grep|cat|diff|head|tail|less|more)\ .*\.java ]]; then
    exit 0
fi

# Patterns that could modify Java files
MODIFY_PATTERNS=(
    '>\s*[^>]*\.java'           # Redirect to .java file
    '>>\s*[^>]*\.java'          # Append to .java file
    'sed\s+-i.*\.java'          # sed in-place edit
    'awk\s+-i.*\.java'          # awk in-place
    'mv\s+.*\.java'             # move .java files
    'cp\s+.*\.java'             # copy to .java files (destination)
    'tee\s+.*\.java'            # tee to .java file
    'cat\s*<<.*\.java'          # heredoc to .java
    'echo\s+.*>\s*.*\.java'     # echo redirect to .java
    'printf\s+.*>\s*.*\.java'   # printf redirect to .java
)

# Check if command matches any modify pattern
for pattern in "${MODIFY_PATTERNS[@]}"; do
    if echo "$COMMAND" | grep -qE "$pattern"; then
        # Check if targeting production code (src/main/java)
        if [[ "$COMMAND" =~ src/main/java ]]; then
            # Check for TDD mode - each session has isolated TDD state
            TDD_MODE_FILE="/tmp/tdd_skill_active_${SESSION_ID}"

            if [[ ! -f "$TDD_MODE_FILE" ]]; then
                cat >&2 << EOF

❌ TDD BASH GUARD - BLOCKED ❌
═══════════════════════════════════════════════════════════════════════════════

Bash command attempts to modify production Java file without TDD mode.

Command: $COMMAND
Pattern: $pattern

This would bypass the TDD enforcement gate. Production Java files MUST be
modified via Write/Edit tools with active TDD mode.

To proceed, you MUST invoke the tdd-implementation skill first:

    Skill: tdd-implementation

═══════════════════════════════════════════════════════════════════════════════
EOF
                # Use proper permission system
                output_hook_block "Blocked: TDD mode required for modifying production Java via Bash. Invoke tdd-implementation skill first." ""
                exit 0
            fi

            # TDD mode active but verify phase
            PHASE=$(jq -r '.phase // empty' "$TDD_MODE_FILE" 2>/dev/null || echo "")
            TEST_FAILED=$(jq -r '.test_failed // false' "$TDD_MODE_FILE" 2>/dev/null || echo "false")

            if [[ "$PHASE" == "RED" && "$TEST_FAILED" != "true" ]]; then
                cat >&2 << EOF

⚠️ TDD BASH GUARD - RED PHASE NOT VERIFIED ⚠️
═══════════════════════════════════════════════════════════════════════════════

TDD mode is active but you're in RED phase without verified test failure.

Before modifying production code, you MUST:
1. Write the failing test
2. Run the test to verify it FAILS
3. Update TDD mode to GREEN phase

═══════════════════════════════════════════════════════════════════════════════
EOF
                # Use proper permission system
                output_hook_block "Blocked: TDD RED phase - test must fail before modifying production code. Verify test failure first." ""
                exit 0
            fi
        fi
        break
    fi
done

# All checks passed
exit 0
