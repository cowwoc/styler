#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in detect-retrospective-docs.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Detect retrospective documentation that chronicles past decisions/processes
# rather than providing forward-looking guidance

echo "🔍 Scanning for retrospective documentation..."
echo ""

cd "$(dirname "$0")/.." || exit 1

VIOLATIONS=0

# Retrospective indicators (strong signals)
RETROSPECTIVE_PATTERNS=(
    "Evidence-Based Decision Process"
    "Decision Process"
    "Post-mortem"
    "Lessons Learned"
    "What Went Wrong"
    "What We Learned"
    "Historical Context"
    "Development Process Retrospective"
)

# Phase-based decision chronicles (strong signal when combined with decision language)
DECISION_CHRONICLE_PATTERNS=(
    "Phase 1:.*Stakeholder"
    "Phase 2:.*JMH Benchmark Evidence"
    "Phase 3:.*Memory Target Validation"
    "Phase 4:.*Stakeholder Validation"
)

# Check each markdown file
while IFS= read -r file; do
    # Skip excluded patterns
    if [[ "$file" =~ todo\.md$ ]] || \
       [[ "$file" =~ changelog\.md$ ]] || \
       [[ "$file" =~ CLAUDE\.md$ ]] || \
       [[ "$file" =~ detect-retrospective-docs\.sh ]] || \
       [[ "$file" =~ README\.md$ ]]; then
        continue
    fi

    MATCHED=0
    MATCH_REASON=""

    # Check for retrospective patterns
    for pattern in "${RETROSPECTIVE_PATTERNS[@]}"; do
        if grep -q "$pattern" "$file" 2>/dev/null; then
            MATCHED=1
            MATCH_REASON="Contains retrospective pattern: '$pattern'"
            break
        fi
    done

    # Check for decision chronicle patterns (multiple phases documenting past decision process)
    if [ $MATCHED -eq 0 ]; then
        PHASE_COUNT=0
        for pattern in "${DECISION_CHRONICLE_PATTERNS[@]}"; do
            if grep -qE "$pattern" "$file" 2>/dev/null; then
                PHASE_COUNT=$((PHASE_COUNT + 1))
            fi
        done

        if [ $PHASE_COUNT -ge 2 ]; then
            MATCHED=1
            MATCH_REASON="Decision chronicle detected (multiple decision phases documented)"
        fi
    fi

    # Check for combination of "DECISION:" and "RATIONALE:" (decision record pattern)
    if [ $MATCHED -eq 0 ]; then
        if grep -q "^##.*DECISION:" "$file" 2>/dev/null && \
           grep -q "RATIONALE:" "$file" 2>/dev/null && \
           grep -qE "Phase [0-9]:" "$file" 2>/dev/null; then
            MATCHED=1
            MATCH_REASON="Formal decision record pattern (DECISION + RATIONALE + phases)"
        fi
    fi

    if [ $MATCHED -eq 1 ]; then
        VIOLATIONS=$((VIOLATIONS + 1))

        if [ $VIOLATIONS -eq 1 ]; then
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "⚠️  RETROSPECTIVE DOCUMENTATION DETECTED"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo ""
        fi

        echo "📄 $file"
        echo "   Reason: $MATCH_REASON"
        echo ""

        # Show first few matching lines as evidence
        echo "   Evidence:"
        for pattern in "${RETROSPECTIVE_PATTERNS[@]}"; do
            if grep -n "$pattern" "$file" 2>/dev/null | head -1; then
                break
            fi
        done | sed 's/^/   /'
        echo ""
    fi
done < <(find docs -name "*.md" -type f)

if [ $VIOLATIONS -eq 0 ]; then
    echo "✅ No retrospective documentation found!"
    echo ""
    echo "All documentation appears to be forward-looking."
else
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "⚠️  Found $VIOLATIONS retrospective document(s)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Per CLAUDE.md § Retrospective Documentation Policy:"
    echo "❌ Post-implementation analysis reports"
    echo "❌ \"Lessons learned\" documents"
    echo "❌ Debugging chronicles or problem-solving narratives"
    echo "❌ Development process retrospectives"
    echo ""
    echo "These documents should be removed. Decisions and rationale should be"
    echo "captured in:"
    echo "  - Code comments (inline with the code)"
    echo "  - Git commit messages (with the changes)"
    echo "  - Architecture docs (forward-looking design)"
    echo ""
    echo "See: docs/project/documentation-references.md for guidance"
    echo ""
    exit 1
fi
