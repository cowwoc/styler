#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in suggest-anchor-migration.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Suggest anchor-based replacements for hard-coded line number references
# Usage: ./scripts/suggest-anchor-migration.sh [file-pattern]

# Change to main directory
cd "$(dirname "$0")/.." || exit 1

FILE_PATTERN="${1:-.}"

echo "🔄 Analyzing hard-coded references and suggesting migrations..."
echo ""

INDEX_FILE="docs/.index.json"

if [ ! -f "$INDEX_FILE" ]; then
    echo "ERROR: Index file not found. Run: ./scripts/generate-doc-index.sh" >&2
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "ERROR: jq is required but not installed" >&2
    exit 1
fi

SUGGESTIONS=0

# Find hard-coded references
while IFS=: read -r file line_num content; do
    if [ -z "$file" ]; then
        continue
    fi

    # Extract file and line range from the reference
    if [[ $content =~ (Read[[:space:]]+)([^[:space:]]+/docs/[^[:space:]]+\.md)[[:space:]]+lines[[:space:]]+([0-9]+)-([0-9]+) ]]; then
        ref_file="${BASH_REMATCH[2]}"
        start_line="${BASH_REMATCH[3]}"
        end_line="${BASH_REMATCH[4]}"

        # Normalize path
        ref_file_rel="${ref_file#*/docs/}"
        ref_file_rel="docs/$ref_file_rel"

        # Find matching anchor in index
        matching_anchors=$(jq -r "
            .\"$ref_file_rel\" // {} |
            to_entries |
            map(select(.value.start == $start_line or
                      (.value.start >= $start_line - 10 and .value.start <= $start_line + 10))) |
            .[0].key // \"\"
        " "$INDEX_FILE" 2>/dev/null || echo "")

        SUGGESTIONS=$((SUGGESTIONS + 1))

        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "📄 $file:$line_num"
        echo ""
        echo "Current (hard-coded):"
        echo "  $content"
        echo ""

        if [ -n "$matching_anchors" ]; then
            # Extract just the filename from the full path
            filename=$(basename "$ref_file_rel")

            echo "✅ Suggested replacement:"
            echo "  DOC_REF=\$(resolve_doc_ref \"$filename#$matching_anchors\")"
            echo "  echo \"\$DOC_REF\""
            echo ""
            echo "Or in documentation:"
            echo "  See [$matching_anchors]($filename#$matching_anchors)"
        else
            echo "⚠️  No matching anchor found in index"
            echo ""
            echo "Manual steps:"
            echo "1. Open $ref_file_rel"
            echo "2. Find section starting around line $start_line"
            echo "3. Add anchor: ## Section Title {#anchor-id}"
            echo "4. Regenerate index: ./scripts/generate-doc-index.sh"
            echo "5. Replace with: resolve_doc_ref \"filename#anchor-id\""
        fi
        echo ""

    fi
done < <(grep -rn \
    --include="*.sh" \
    --include="*.md" \
    --exclude-dir=".git" \
    --exclude="suggest-anchor-migration.sh" \
    --exclude="find-hardcoded-references.sh" \
    -E "Read .*/docs/[^ ]+\.md lines [0-9]+-[0-9]+" \
    "$FILE_PATTERN" 2>/dev/null || true)

if [ $SUGGESTIONS -eq 0 ]; then
    echo "✅ No hard-coded references found to migrate!"
else
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📊 Summary: $SUGGESTIONS reference(s) found"
    echo ""
    echo "💡 After making changes:"
    echo "   1. Test: ./scripts/resolve-doc-reference.sh \"file.md#anchor-id\""
    echo "   2. Verify: ./scripts/find-hardcoded-references.sh"
    echo ""
fi
