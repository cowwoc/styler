#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in detect-retrospective-creation.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Hook: Warn when creating documentation files that might be retrospective
# Runs on: UserPromptSubmit, ToolResult (Write tool)
# Purpose: Prevent creation of retrospective documentation

# Check for newly created markdown files in docs/
STAGED_FILES=$(git diff --cached --name-only --diff-filter=A 2>/dev/null | grep -E '^docs/.*\.md$' || true)

if [ -z "$STAGED_FILES" ]; then
    exit 0
fi

SUSPICIOUS=0

# Retrospective indicators
RETROSPECTIVE_PATTERNS=(
    "Evidence-Based Decision Process"
    "Decision Process"
    "Post-mortem"
    "Lessons Learned"
    "What Went Wrong"
    "What We Learned"
    "Historical Context"
    "Development Process Retrospective"
    "Final.*Decision"
)

# Check newly added files
while IFS= read -r file; do
    # Skip certain allowed patterns
    if [[ "$file" =~ README\.md$ ]] || \
       [[ "$file" =~ changelog\.md$ ]] || \
       [[ "$file" =~ todo\.md$ ]]; then
        continue
    fi

    # Check if file contains retrospective patterns
    for pattern in "${RETROSPECTIVE_PATTERNS[@]}"; do
        if git diff --cached "$file" | grep -q "$pattern" 2>/dev/null; then
            if [ $SUSPICIOUS -eq 0 ]; then
                echo "" >&2
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
                echo "⚠️  POTENTIAL RETROSPECTIVE DOCUMENTATION" >&2
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
                echo "" >&2
            fi
            SUSPICIOUS=$((SUSPICIOUS + 1))

            echo "📄 $file" >&2
            echo "   Contains: '$pattern'" >&2
            echo "" >&2
            break
        fi
    done
done <<< "$STAGED_FILES"

if [ $SUSPICIOUS -gt 0 ]; then
    echo "This appears to be retrospective documentation (chronicling past" >&2
    echo "decisions/processes rather than providing forward-looking guidance)." >&2
    echo "" >&2
    echo "Per CLAUDE.md § Retrospective Documentation Policy:" >&2
    echo "" >&2
    echo "❌ PROHIBITED:" >&2
    echo "   - Post-implementation analysis reports" >&2
    echo "   - Decision chronicles documenting past phases" >&2
    echo "   - \"Lessons learned\" documents" >&2
    echo "   - Debugging chronicles" >&2
    echo "" >&2
    echo "✅ PERMITTED:" >&2
    echo "   - Forward-looking architecture documentation" >&2
    echo "   - API documentation and user guides" >&2
    echo "   - Technical design documents for upcoming features" >&2
    echo "" >&2
    echo "💡 Better alternatives:" >&2
    echo "   - Put rationale in git commit message" >&2
    echo "   - Add code comments explaining 'why'" >&2
    echo "   - Update architecture.md with forward-looking design" >&2
    echo "" >&2
    echo "To proceed anyway: git commit --no-verify" >&2
    echo "" >&2
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2

    # Warning only, don't block
    exit 0
fi

exit 0
