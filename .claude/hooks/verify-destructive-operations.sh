#!/bin/bash
# Claude Code Hook: Post-Destructive Operation Verification
# Reminds Claude to verify no important details were lost after destructive operations

# List of destructive operations that trigger verification
DESTRUCTIVE_KEYWORDS=(
	"git rebase"
	"git reset" 
	"git checkout"
	"squash"
	"consolidate"
	"merge"
	"remove duplicate"
	"cleanup"
	"reorganize"
	"refactor"
	"delete"
	"rm "
)

# Get the last user message (simplified detection)
LAST_MESSAGE="$1"

# Check if any destructive keywords are present
for keyword in "${DESTRUCTIVE_KEYWORDS[@]}"; do
	if echo "$LAST_MESSAGE" | grep -qi "$keyword"; then
	    echo "🚨 DESTRUCTIVE OPERATION DETECTED: '$keyword'"
	    echo ""
	    echo "⚠️  MANDATORY VERIFICATION REQUIRED:"
	    echo "After completing this operation, you MUST:"
	    echo "1. Double-check that no important details were unintentionally removed"
	    echo "2. Verify that all essential information has been preserved" 
	    echo "3. Compare before/after to ensure completeness"
	    echo "4. If consolidating/reorganizing, confirm all original content is retained"
	    echo ""
	    echo "🔍 This verification step is REQUIRED before considering the task complete."
	    exit 0
	fi
done