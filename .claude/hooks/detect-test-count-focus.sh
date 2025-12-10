#!/bin/bash
# detect-test-count-focus.sh - Validates tester requirements focus on business rules, not test counts
#
# ADDED: 2025-12-10 after tester agent mandated "34 tests" instead of business rule coverage
# PREVENTS: Test reports focusing on quantity over quality
#
# Triggers: PostToolUse for Write tool writing to *-tester-requirements.md

set -euo pipefail

# Read tool context from stdin
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // .tool.name // ""' 2>/dev/null || echo "")
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .input.file_path // ""' 2>/dev/null || echo "")

# Only check Write tool to tester requirements files
if [[ "$TOOL_NAME" != "Write" ]] || [[ ! "$FILE_PATH" =~ -tester-requirements\.md$ ]]; then
    exit 0
fi

# Check if file exists
if [[ ! -f "$FILE_PATH" ]]; then
    exit 0
fi

# Patterns that indicate test-count focus (anti-patterns)
VIOLATIONS=""

# Check for minimum test count mandates
if grep -qiE "(minimum|required|must have|at least).*(test count|[0-9]+ tests)" "$FILE_PATH"; then
    VIOLATIONS="$VIOLATIONS\n- Mandates specific test count instead of business rule coverage"
fi

# Check for "Total Tests: N" or "## Total: N tests"
if grep -qiE "(total|grand total):?\s*[0-9]+ tests?" "$FILE_PATH"; then
    VIOLATIONS="$VIOLATIONS\n- Focuses on total test count"
fi

# Check for "MINIMUM TEST COUNT: N"
if grep -qiE "minimum test count" "$FILE_PATH"; then
    VIOLATIONS="$VIOLATIONS\n- Contains 'MINIMUM TEST COUNT' - should focus on business rules instead"
fi

# Check for numbered requirements like "Phase 1: 17 tests"
if grep -qiE "phase [0-9]:?\s*[0-9]+ tests" "$FILE_PATH"; then
    VIOLATIONS="$VIOLATIONS\n- Specifies test counts per phase instead of coverage goals"
fi

if [[ -n "$VIOLATIONS" ]]; then
    echo "" >&2
    echo "⚠️ TEST REQUIREMENTS ANTI-PATTERN DETECTED" >&2
    echo "File: $FILE_PATH" >&2
    echo "" >&2
    echo "Issues found:" >&2
    echo -e "$VIOLATIONS" >&2
    echo "" >&2
    echo "📚 GUIDANCE (from quality-guide.md):" >&2
    echo "   'Focus on meaningful behavior, not test counts or code coverage percentages.'" >&2
    echo "" >&2
    echo "✅ CORRECT APPROACH:" >&2
    echo "   - List business rules that must be tested" >&2
    echo "   - Define edge cases and error scenarios" >&2
    echo "   - Specify validation criteria" >&2
    echo "   - Test count is an OUTPUT, not an INPUT" >&2
    echo "" >&2
    echo "❌ WRONG APPROACH:" >&2
    echo "   - 'Minimum 27 tests required'" >&2
    echo "   - 'Total: 34 tests'" >&2
    echo "   - 'Phase 1: 17 tests'" >&2
    echo "" >&2
    # Warning only - don't block (file already written)
    exit 0
fi

exit 0
