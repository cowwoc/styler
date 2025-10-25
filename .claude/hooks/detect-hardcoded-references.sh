#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in detect-hardcoded-references.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Hook: Warn about hard-coded line number references in newly added/modified files
# Runs on: UserPromptSubmit, tool results
# Purpose: Encourage use of anchor-based reference system

# Only check files that are being modified
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM 2>/dev/null | grep -E '\.(sh|md)$' || true)

if [ -z "$STAGED_FILES" ]; then
    exit 0
fi

FOUND=0

while IFS= read -r file; do
    # Skip this script itself and documentation about the system
    if [[ "$file" == *"detect-hardcoded-references.sh"* ]] || \
       [[ "$file" == *"documentation-references.md"* ]] || \
       [[ "$file" == *"find-hardcoded-references.sh"* ]]; then
        continue
    fi

    # Check for hard-coded line number patterns in staged changes
    if git diff --cached "$file" | grep -E "^\+" | grep -qE "(Read .*/docs/[^ ]+\.md lines [0-9]+-[0-9]+|Read .*/docs/[^ ]+\.md lines [0-9]+)"; then
        if [ $FOUND -eq 0 ]; then
            echo "" >&2
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
            echo "⚠️  DOCUMENTATION REFERENCE PATTERN DETECTED" >&2
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
            echo "" >&2
        fi
        FOUND=$((FOUND + 1))
        echo "📄 $file contains hard-coded line number reference" >&2
        echo "" >&2
    fi
done <<< "$STAGED_FILES"

if [ $FOUND -gt 0 ]; then
    echo "Hard-coded line numbers become stale when documentation changes." >&2
    echo "" >&2
    echo "💡 Consider using anchor-based references instead:" >&2
    echo "   source .claude/hooks/lib/doc-reference-resolver.sh" >&2
    echo "   DOC_REF=\$(resolve_doc_ref \"file.md#anchor-id\")" >&2
    echo "" >&2
    echo "📖 Guide: docs/project/documentation-references.md" >&2
    echo "" >&2
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
    echo "" >&2

    # Warning only, don't block commit
    # Allow override with: git commit --no-verify
fi

exit 0
