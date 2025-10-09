#!/bin/bash

# Pre-commit hook to detect retrospective documentation and meta-commentary
# Purpose: Catch documentation that chronicles what happened to the codebase in the past
# Aligns with RETROSPECTIVE DOCUMENTATION POLICY in CLAUDE.md

echo "🔍 Checking for retrospective documentation patterns..."

POTENTIAL_ISSUES=""

# Check for retrospective chronicle patterns (exclude test files - regression tests document past bugs)
RETROSPECTIVE=$(git diff --cached | grep -v "^diff.*Test\|^diff.*test/" | grep -E "^\+.*(How we (fixed|debugged|solved|resolved)|What went wrong|Lessons learned|Previous (implementation|approach|version)|This (fixes|resolves|addresses) (the|an?) (issue|problem|bug) where|We (discovered|found|realized) that|After (investigating|debugging|testing)|The (original|old|previous) (code|implementation|approach))" -i | head -5 || true)

if [ -n "$RETROSPECTIVE" ]; then
	echo "⚠️  BLOCKED: Found retrospective documentation patterns:"
	echo "$RETROSPECTIVE"
	echo ""
	echo "❌ PROHIBITED: Documentation that chronicles past problems or fixes"
	echo "   - 'How we fixed X' → Remove, git history records this"
	echo "   - 'Lessons learned from Y' → Remove, not user-facing value"
	echo "   - 'After debugging, we found Z' → Remove, implementation detail"
	echo ""
	echo "✅ PERMITTED: Forward-looking documentation describing current state"
	echo "   - 'System uses X to handle Y' → Describes current functionality"
	echo "   - 'API provides Z capability' → User-facing documentation"
	echo ""
	echo "✅ EXCEPTION: Regression tests documenting past bugs"
	echo "   - Test files may reference historical bugs to prevent recurrence"
	echo ""
	POTENTIAL_ISSUES="yes"
fi

# Check for debugging chronicle patterns (exclude test files - regression test context is allowed)
DEBUG_CHRONICLES=$(git diff --cached | grep -v "^diff.*Test\|^diff.*test/" | grep -E "^\+.*(debugging|investigation|troubleshooting) (process|chronicle|narrative|story|journey|steps)" -i | head -3 || true)

if [ -n "$DEBUG_CHRONICLES" ]; then
	echo "⚠️  BLOCKED: Found debugging chronicle documentation:"
	echo "$DEBUG_CHRONICLES"
	echo ""
	echo "❌ PROHIBITED: Step-by-step debugging narratives belong in git history"
	echo "✅ PERMITTED: Technical design docs for upcoming features only"
	echo "✅ EXCEPTION: Test documentation explaining regression test context"
	echo ""
	POTENTIAL_ISSUES="yes"
fi

# Check for post-implementation analysis file patterns (exclude test files)
POST_IMPL_FILES=$(git diff --cached --name-only | grep -v "Test\|test/" | grep -E "(retrospective|lessons-learned|post-mortem|analysis-of|chronicle-of|fix-documentation|problem-solving|debugging-.*\.(md|txt))" -i || true)

if [ -n "$POST_IMPL_FILES" ]; then
	echo "⚠️  BLOCKED: Found retrospective documentation files:"
	echo "$POST_IMPL_FILES"
	echo ""
	echo "❌ PROHIBITED file types:"
	echo "   - Post-implementation analysis reports"
	echo "   - Lessons learned documents"
	echo "   - Debugging chronicles"
	echo "   - Development process retrospectives"
	echo ""
	echo "✅ PERMITTED file types (with explicit requirement):"
	echo "   - Architecture documentation (forward-looking)"
	echo "   - API/user documentation"
	echo "   - Technical design for upcoming features"
	echo "   - Regression test files documenting past bugs"
	echo ""
	POTENTIAL_ISSUES="yes"
fi

# Check for change chronicle markers (exclude test files)
CHANGE_CHRONICLES=$(git diff --cached | grep -v "^diff.*Test\|^diff.*test/" | grep -E "^\+.*##.*Changes|^\+.*##.*(Fix|Problem|Issue) (History|Chronicle)|^\+.*##.*(What|Why) (We|This) (Changed|Fixed)" -i | head -3 || true)

if [ -n "$CHANGE_CHRONICLES" ]; then
	echo "⚠️  BLOCKED: Found change chronicle section headers:"
	echo "$CHANGE_CHRONICLES"
	echo ""
	echo "❌ PROHIBITED: Sections documenting what changed and why"
	echo "✅ ALTERNATIVE: Git commit messages and history serve this purpose"
	echo "✅ EXCEPTION: Test documentation may explain bug history for context"
	echo ""
	POTENTIAL_ISSUES="yes"
fi

if [ -n "$POTENTIAL_ISSUES" ]; then
	echo "🚨 RETROSPECTIVE DOCUMENTATION DETECTED"
	echo ""
	echo "Documentation should serve future users/developers, not chronicle past problems."
	echo "Code and git history are the primary record of changes and debugging."
	echo ""
	echo "Review staged changes and remove retrospective content before committing."
	echo ""
	exit 1
fi

echo "✅ No retrospective documentation detected. Commit proceeding."
exit 0