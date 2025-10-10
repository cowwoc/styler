#!/bin/bash

# Pre-commit hook to suggest improvements for potential meta-commentary
# This suggests cleaner documentation focused on current state rather than changes

# More specific patterns that are more likely to indicate meta-commentary
POTENTIAL_ISSUES=""

# Check for explicit change documentation patterns and meta-commentary markers
CHANGE_DOCS=$(git diff --cached | grep -E "(^\+.*Last Updated|^\+.*UPDATED:|^\+.*REPLACED:|^\+.*\*\*.*UPDATED.*\*\*|^\+.*\*\*.*REPLACED.*\*\*|^\+.*\*\*.*SIMPLIFIED.*\*\*|^\+.*\*\*.*ENHANCED.*\*\*|^\+.*\*\*.*IMPROVED.*\*\*|^\+.*\*\*.*CHANGED.*\*\*|^\+.*\*\*.*MODIFIED.*\*\*)" || true)

if [ -n "$CHANGE_DOCS" ]; then
	echo "⚠️  SUGGESTION: Found potential meta-commentary patterns in staged changes:"
	echo "$CHANGE_DOCS" | head -5
	echo ""
	echo "💡 Consider focusing on current state rather than change descriptions:"
	echo "   - Instead of 'Updated X to do Y' → 'X does Y'"
	echo "   - Instead of 'Replaced A with B' → 'Uses B for [purpose]'"
	echo "   - Remove 'Last Updated' timestamps in favor of git history"
	echo ""
	POTENTIAL_ISSUES="yes"
fi

# Check for procedural language in commit message
COMMIT_MSG=$(git log --format=%B -n 1 HEAD 2>/dev/null || echo "")
PROCEDURAL=$(echo "$COMMIT_MSG" | grep -iE "(updated|modified|changed|replaced)" | head -3 || true)

if [ -n "$PROCEDURAL" ]; then
	echo "💡 COMMIT MESSAGE SUGGESTION: The diff shows what changed - describe the result instead:"
	echo "   Current: $(echo "$PROCEDURAL" | head -1)"
	echo "   Better:  Omit 'updated/modified/changed' - state what the code/docs DO now"
	echo "   Example: 'Protocol uses delegated agents' not 'Updated protocol to use delegated agents'"
	echo ""
	POTENTIAL_ISSUES="yes"
fi

# Check for obvious meta-commentary in documentation files
META_COMMENTS=$(git diff --cached | grep -E "^\+.*what.*changed|^\+.*what.*modified|^\+.*this.*replaces|^\+.*now.*instead.*of|^\+.*instead.*of.*listing|^\+.*approach.*instead|^\+.*simplified.*approach" -i | head -3 || true)

if [ -n "$META_COMMENTS" ]; then
	echo "💡 DOCUMENTATION SUGGESTION: Consider removing change explanations:"
	echo "$META_COMMENTS"
	echo "   → Focus on describing current functionality and purpose"
	echo ""
	POTENTIAL_ISSUES="yes"
fi

# Check for procedural change descriptions
PROCEDURAL_CHANGES=$(git diff --cached | grep -E "^\+.*Instead of.*Claude must|^\+.*Rather than.*Claude|^\+.*Now Claude|^\+.*Claude must now" | head -3 || true)

if [ -n "$PROCEDURAL_CHANGES" ]; then
	echo "💡 PROCEDURAL SUGGESTION: Found change-focused instructions:"
	echo "$PROCEDURAL_CHANGES"
	echo "   → Consider stating requirements directly without referencing changes"
	echo "   → Example: 'Claude must X' instead of 'Instead of Y, Claude must X'"
	echo ""
	POTENTIAL_ISSUES="yes"
fi

if [ -n "$POTENTIAL_ISSUES" ]; then
	echo "📝 These are suggestions, not blocks. Commit will proceed."
	echo "   Review if these patterns represent meta-commentary about changes"
	echo "   vs. legitimate functional descriptions."
	echo ""
fi

# No output if no issues detected
exit 0