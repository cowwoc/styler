#!/bin/bash
# Hook: detect-weak-parser-tests.sh
# Trigger: PostToolUse (Write|Edit)
# Purpose: Detect weak parser tests that only check parsing success
#          instead of validating AST structure
#
# ADDED: 2025-12-24 after tester agent created CommentInExpressionTest with
# weak assertions (isInstanceOf(ParseResult.Success.class)) instead of proper
# AST validation (parseSemanticAst + semanticNode comparison).
# PREVENTS: Parser tests that pass when parsing succeeds but fail to catch
# incorrect AST structure.

set -euo pipefail

# Read tool input from stdin
INPUT=$(cat)

# Extract file path from tool input
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty' 2>/dev/null)

# Exit if no file path or not a Java file
if [[ -z "$FILE_PATH" || ! "$FILE_PATH" =~ \.java$ ]]; then
    exit 0
fi

# Only check parser test files
if [[ ! "$FILE_PATH" =~ parser/src/test/.*Test\.java$ ]]; then
    exit 0
fi

# Skip ParserTestUtils itself and integration tests
if [[ "$FILE_PATH" =~ ParserTestUtils\.java$ ]] || \
   [[ "$FILE_PATH" =~ IntegrationTest\.java$ ]] || \
   [[ "$FILE_PATH" =~ SecurityTest\.java$ ]]; then
    exit 0
fi

# Check if file exists
if [[ ! -f "$FILE_PATH" ]]; then
    exit 0
fi

# Pattern detection: weak parser test assertions
WEAK_PATTERNS=(
    'isInstanceOf.*ParseResult\.Success'
    'assertParseSucceeds'
    '\.parse\(\).*instanceof.*Success'
)

ISSUES_FOUND=false
ISSUES=""

for pattern in "${WEAK_PATTERNS[@]}"; do
    if grep -qE "$pattern" "$FILE_PATH" 2>/dev/null; then
        ISSUES_FOUND=true
        # Count occurrences
        COUNT=$(grep -cE "$pattern" "$FILE_PATH" 2>/dev/null || echo "0")
        ISSUES="${ISSUES}\n  - Pattern '$pattern': $COUNT occurrences"
    fi
done

if $ISSUES_FOUND; then
    cat >&2 << 'EOF'

⚠️  WEAK PARSER TEST PATTERN DETECTED

Parser unit tests MUST validate AST structure, not just parsing success.
See: docs/code-style/testing-claude.md § PARSER TEST PATTERNS

❌ DETECTED PATTERNS:
EOF
    echo -e "$ISSUES" >&2

    cat >&2 << 'EOF'

❌ WRONG - Only verifies parsing doesn't crash:
    ParseResult result = parser.parse();
    requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

✅ CORRECT - Validates AST nodes are created:
    Set<SemanticNode> actual = ParserTestUtils.parseSemanticAst(source);
    Set<SemanticNode> expected = Set.of(
        semanticNode("EXPRESSION", ..., List.of(
            semanticNode("LITERAL", ..., List.of())
        ))
    );
    requireThat(actual, "actual").isEqualTo(expected);

📖 Reference: docs/code-style/testing-claude.md lines 33-63

EOF
fi

# Don't block - just warn. Tests should still compile, validation happens during review.
exit 0
