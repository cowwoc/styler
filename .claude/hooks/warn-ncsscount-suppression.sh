#!/bin/bash
# Hook: warn-ncsscount-suppression.sh
# Trigger: PostToolUse (Edit)
# Purpose: BLOCK NcssCount suppression unless decomposition was actually attempted
#
# ADDED: 2025-12-31 after engineer agent added NcssCount suppression to parsePostfix()
# without attempting decomposition first.
# UPDATED: 2026-01-05 - Made blocking (exit 2) instead of warning-only.
# Hook was not registered previously, allowing violations to slip through.
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
    echo "" >&2
    echo "🚨 NCSSCOUNT SUPPRESSION BLOCKED" >&2
    echo "" >&2
    echo "You added @SuppressWarnings(\"PMD.NcssCount\")." >&2
    echo "" >&2
    echo "Per java-style.md: NEVER suppress NcssCount without ACTUALLY attempting decomposition." >&2
    echo "" >&2
    echo "MANDATORY STEPS (must be completed BEFORE suppression):" >&2
    echo "" >&2
    echo "  1. Actually EXTRACT at least one helper method or helper class" >&2
    echo "  2. Run the build to verify extraction works" >&2
    echo "  3. Compare readability of extracted vs original" >&2
    echo "  4. ONLY if extraction genuinely harms readability, add suppression" >&2
    echo "" >&2
    echo "INVALID JUSTIFICATIONS (these are NOT acceptable):" >&2
    echo "  ❌ 'Shared state would need to be exposed' - use inner classes" >&2
    echo "  ❌ 'Would need to pass many parameters' - create parameter objects" >&2
    echo "  ❌ 'Class is inherently large' - extract to helper classes" >&2
    echo "" >&2
    echo "ACTION REQUIRED:" >&2
    echo "  1. Remove the @SuppressWarnings annotation" >&2
    echo "  2. Extract module parsing methods to ModuleParser helper class" >&2
    echo "  3. OR extract other logical blocks to helper methods" >&2
    echo "  4. Re-run PMD to verify class is under threshold" >&2
    echo "" >&2
    echo "Reference: .claude/rules/java-style.md § PMD Suppression" >&2
    echo "" >&2
    # Exit 2 signals hook rejection (blocks the edit conceptually, though PostToolUse
    # can't actually roll back - it serves as a strong signal to undo)
    exit 2
fi

exit 0
