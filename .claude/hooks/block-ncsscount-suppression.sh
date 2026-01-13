#!/bin/bash
# Hook: block-ncsscount-suppression.sh
# Trigger: PreToolUse (Write|Edit)
# Purpose: BLOCK NcssCount suppression - this is NEVER allowed per java-style.md
#
# ADDED: 2025-12-31 after engineer agent added NcssCount suppression to parsePostfix()
# without attempting decomposition first.
# UPDATED: 2026-01-05 - Made blocking (exit 2) instead of warning-only.
# UPDATED: 2026-01-13 - Moved to PreToolUse to actually BLOCK (PostToolUse can't block).
# PREVENTS: NcssCount suppressions which are NEVER acceptable.

set -euo pipefail
trap 'echo "ERROR in block-ncsscount-suppression.sh line $LINENO: $BASH_COMMAND" >&2; exit 0' ERR

# Read tool input from stdin
INPUT=$(cat)

# Get the new_string (Edit) or content (Write) that will be inserted
NEW_STRING=$(echo "$INPUT" | jq -r '.tool_input.new_string // .tool_input.content // ""')

# Check if it contains NcssCount suppression
if echo "$NEW_STRING" | grep -q 'SuppressWarnings.*PMD.NcssCount\|SuppressWarnings.*NcssCount'; then
    echo "" >&2
    echo "🚨 NCSSCOUNT SUPPRESSION BLOCKED" >&2
    echo "" >&2
    echo "You attempted to add @SuppressWarnings(\"PMD.NcssCount\")." >&2
    echo "" >&2
    echo "Per java-style.md: NcssCount suppressions are NEVER allowed." >&2
    echo "" >&2
    echo "REQUIRED ACTION: Refactor the code to reduce complexity." >&2
    echo "" >&2
    echo "  Strategies:" >&2
    echo "  - Extract helper methods for logical blocks" >&2
    echo "  - Extract helper classes (e.g., ModuleParser for module-info.java)" >&2
    echo "  - Use composition instead of monolithic classes" >&2
    echo "  - Group switch cases into separate methods" >&2
    echo "" >&2
    echo "Reference: .claude/rules/java-style.md § PMD Suppression" >&2
    echo "" >&2
    # Exit 2 signals hook rejection - BLOCKS the edit
    exit 2
fi

exit 0
