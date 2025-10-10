#!/bin/bash
# Pre-commit hook to detect generic/automated JavaDoc patterns
# Enforces CLAUDE.md § JavaDoc Manual Documentation Requirement

# Require Claude session ID (set by Claude Code as environment variable).
# This ensures:
# 1. Each Claude instance tracks warnings separately
# 2. Warnings show once per Claude session
# 3. After context compaction, new session allows warning again
if [ -z "$CLAUDE_SESSION_ID" ]; then
	echo "❌ ERROR: CLAUDE_SESSION_ID environment variable must be set."
	echo "   Claude Code must export CLAUDE_SESSION_ID before git commit."
	exit 1
fi
SESSION_WARNING_FILE="/tmp/claude-javadoc-warning-shown-${CLAUDE_SESSION_ID}"

# Check if we've already warned in this session
if [ -f "$SESSION_WARNING_FILE" ]; then
	# Already warned - just run checks silently
	exit 0
fi

echo "🔍 Checking for generic JavaDoc patterns..."

VIOLATIONS=""

# Pattern 1: Generic "Tests X" JavaDoc (automated pattern)
GENERIC_TESTS=$(git diff --cached --diff-filter=AM | grep -E "^\+.*\* Tests [A-Z][a-z]+" | grep -v "test.*specific\|edge case\|boundary\|regression\|verifies that\|ensures that\|validates that" || true)

if [ -n "$GENERIC_TESTS" ]; then
	echo "⚠️  SUGGESTION: Found potentially generic JavaDoc comments:"
	echo "$GENERIC_TESTS" | head -5
	echo ""
	VIOLATIONS="yes"
fi

# Pattern 2: JavaDoc that just rephrases method name
METHOD_NAME_JAVADOC=$(git diff --cached --diff-filter=AM | grep -B2 "^\+.*public.*void test" | grep -E "^\+.*\* (Test|Tests) [A-Z]" | grep -v "verifies\|ensures\|validates\|checks that" || true)

if [ -n "$METHOD_NAME_JAVADOC" ]; then
	echo "⚠️  SUGGESTION: JavaDoc appears to just rephrase method names:"
	echo "$METHOD_NAME_JAVADOC" | head -5
	echo ""
	VIOLATIONS="yes"
fi

# Pattern 3: Batch JavaDoc additions (suspicious pattern)
JAVADOC_COUNT=$(git diff --cached --diff-filter=AM | grep -c "^\+.*\* Tests" || true)

if [ "$JAVADOC_COUNT" -gt 20 ]; then
	echo "⚠️  ALERT: $JAVADOC_COUNT JavaDoc comments added in single commit"
	echo ""
	echo "🚨 Large-scale JavaDoc additions detected"
	echo "   This pattern suggests automated generation."
	echo ""
	VIOLATIONS="yes"
fi

if [ -n "$VIOLATIONS" ]; then
	echo "═══════════════════════════════════════════════════════════════"
	echo "📖 REMINDER: JavaDoc Manual Documentation Requirement"
	echo "═══════════════════════════════════════════════════════════════"
	echo ""
	echo "Please review CLAUDE.md § JavaDoc Manual Documentation Requirement:"
	echo ""
	echo "**ABSOLUTELY PROHIBITED:**"
	echo "  ❌ Using scripts to generate JavaDoc comments"
	echo "  ❌ Batch processing JavaDoc across multiple files"
	echo "  ❌ Converting method names to comments (e.g., 'testX' → 'Tests X')"
	echo ""
	echo "**REQUIRED APPROACH:**"
	echo "  ✅ Read and understand the method's purpose"
	echo "  ✅ Explain WHY the test exists, not just WHAT it tests"
	echo "  ✅ Include context about edge cases or regression prevention"
	echo ""
	echo "**EXAMPLE:**"
	echo "  ❌ WRONG:   '* Tests Valid Token.'"
	echo "  ✅ CORRECT: '* Verifies Token stores all components and calculates length.'"
	echo ""
	echo "═══════════════════════════════════════════════════════════════"
	echo ""
	echo "📝 Commit will proceed - please review your JavaDoc comments."
	echo "   (This reminder shown once per session)"
	echo ""

	# Mark that we've shown the warning in this session
	touch "$SESSION_WARNING_FILE"
fi

echo "✅ JavaDoc pattern check complete."
exit 0
