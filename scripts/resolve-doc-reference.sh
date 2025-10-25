#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in resolve-doc-reference.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Resolve documentation reference to line numbers
# Usage: resolve-doc-reference.sh <reference>
# Example: resolve-doc-reference.sh "task-protocol-core.md#agent-coordination"
# Output: Read /workspace/main/docs/project/task-protocol-core.md lines 145-203

REFERENCE="${1:-}"
INDEX_FILE="${2:-docs/.index.json}"

if [ -z "$REFERENCE" ]; then
    echo "Usage: resolve-doc-reference.sh <reference> [index-file]" >&2
    echo "Example: resolve-doc-reference.sh 'task-protocol-core.md#init-classified'" >&2
    exit 1
fi

# Change to main directory
cd "$(dirname "$0")/.." || exit 1

# Check if index exists
if [ ! -f "$INDEX_FILE" ]; then
    echo "ERROR: Index file not found: $INDEX_FILE" >&2
    echo "Run: ./scripts/generate-doc-index.sh" >&2
    exit 1
fi

# Parse reference
if [[ $REFERENCE =~ ^([^#]+)#([a-z0-9_-]+)$ ]]; then
    file="${BASH_REMATCH[1]}"
    anchor="${BASH_REMATCH[2]}"
else
    echo "ERROR: Invalid reference format: $REFERENCE" >&2
    echo "Expected format: file.md#anchor-id" >&2
    exit 1
fi

# Normalize file path (add docs/project/ if not present)
if [[ ! $file =~ ^docs/ ]]; then
    if [ -f "docs/project/$file" ]; then
        file="docs/project/$file"
    elif [ -f "docs/$file" ]; then
        file="docs/$file"
    else
        echo "ERROR: Could not find file: $file" >&2
        exit 1
    fi
fi

# Look up in index using jq
if ! command -v jq &> /dev/null; then
    echo "ERROR: jq is required but not installed" >&2
    exit 1
fi

# Query index
result=$(jq -r ".\"$file\".\"$anchor\" // null" "$INDEX_FILE")

if [ "$result" = "null" ]; then
    echo "ERROR: Reference not found in index: $file#$anchor" >&2
    echo "Available anchors in $file:" >&2
    jq -r ".\"$file\" | keys[]" "$INDEX_FILE" 2>/dev/null || echo "  (file not in index)" >&2
    exit 1
fi

# Extract line numbers
start=$(echo "$result" | jq -r '.start')
end=$(echo "$result" | jq -r '.end')

# Get absolute path
abs_path="$(pwd)/$file"

# Output Read command format
echo "Read $abs_path lines $start-$end"

# Also output helpful summary to stderr (not captured by scripts)
echo "# Section '$anchor' in $file (lines $start-$end)" >&2
echo "# Usage: \$(resolve_doc_ref \"$(basename "$file")#$anchor\")" >&2
