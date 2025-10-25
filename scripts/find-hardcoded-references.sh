#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in find-hardcoded-references.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Find hard-coded line number references in documentation and scripts
# These should be converted to anchor-based references

echo "🔍 Scanning for hard-coded documentation references..."
echo ""

# Change to main directory
cd "$(dirname "$0")/.." || exit 1

FOUND=0

# Pattern 1: "Read /path/to/file.md lines X-Y"
# Pattern 2: "Read /path/to/file.md lines X"
# Pattern 3: "file.md lines X-Y" in documentation

while IFS=: read -r file line_num content; do
    if [ -n "$file" ]; then
        # Skip comment lines (lines starting with # after whitespace)
        if [[ $content =~ ^[[:space:]]*# ]]; then
            continue
        fi

        FOUND=$((FOUND + 1))
        echo "📄 $file:$line_num"
        echo "   $content"
        echo ""
    fi
done < <(grep -rn \
    --include="*.sh" \
    --include="*.md" \
    --include="*.txt" \
    --exclude-dir=".git" \
    --exclude="find-hardcoded-references.sh" \
    --exclude="documentation-references.md" \
    -E "(Read .*/docs/[^ ]+\.md lines [0-9]+-[0-9]+|Read .*/docs/[^ ]+\.md lines [0-9]+|[a-z-]+\.md lines [0-9]+-[0-9]+)" \
    . 2>/dev/null || true)

if [ $FOUND -eq 0 ]; then
    echo "✅ No hard-coded references found!"
    echo ""
    echo "All documentation references appear to use the anchor-based system."
else
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "⚠️  Found $FOUND hard-coded reference(s)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "These should be converted to anchor-based references."
    echo "See: docs/project/documentation-references.md"
    echo ""
    echo "Migration steps:"
    echo "1. Identify the section being referenced"
    echo "2. Find or add an anchor ID {#anchor-id}"
    echo "3. Replace with: resolve_doc_ref \"file.md#anchor-id\""
    echo ""
    exit 1
fi
