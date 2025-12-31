#!/bin/bash
# Hook: warn-ncsscount-suppression.sh
# Trigger: PostToolUse (Edit)
# Purpose: Warn when NcssCount suppression is added without documented justification
#
# ADDED: 2025-12-31 after engineer agent added NcssCount suppression to parsePostfix()
# without attempting decomposition first.
# PREVENTS: Lazy suppression shortcuts that violate the style guide.

set -euo pipefail
trap 'echo "ERROR in warn-ncsscount-suppression.sh line $LINENO: $BASH_COMMAND" >&2; exit 0' ERR

# Read tool result from stdin
INPUT=$(cat)

# Only check Edit tool results
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_result.name // .tool.name // ""')
if [[ "$TOOL_NAME" != "Edit" ]]; then
    exit 0
fi

# Get the new_string that was inserted
NEW_STRING=$(echo "$INPUT" | jq -r '.tool_input.new_string // ""')

# Check if it contains NcssCount suppression
if echo "$NEW_STRING" | grep -q 'SuppressWarnings.*PMD.NcssCount\|SuppressWarnings.*NcssCount'; then
    # Check if there's a justification comment
    if ! echo "$NEW_STRING" | grep -qiE '(decomposition|extract|helper|state.machine|control.flow|switch.cases)'; then
        echo "" >&2
        echo "⚠️ NCSSCOUNT SUPPRESSION DETECTED" >&2
        echo "" >&2
        echo "You added @SuppressWarnings(\"PMD.NcssCount\")." >&2
        echo "" >&2
        echo "MANDATORY CHECKLIST (from java-style.md):" >&2
        echo "" >&2
        echo "  1. ☐ Identified logical blocks that could become helper methods" >&2
        echo "  2. ☐ Checked for existing helper method patterns in the class" >&2
        echo "  3. ☐ Actually ATTEMPTED to extract at least ONE helper method" >&2
        echo "  4. ☐ Documented WHY decomposition failed (if suppression needed)" >&2
        echo "" >&2
        echo "If you haven't completed this checklist:" >&2
        echo "  - Remove the suppression" >&2
        echo "  - Extract helper methods to reduce method size" >&2
        echo "  - Only add suppression if decomposition genuinely harms readability" >&2
        echo "" >&2
        echo "Reference: .claude/rules/java-style.md § PMD Suppression" >&2
        echo "" >&2
    fi
fi

exit 0
