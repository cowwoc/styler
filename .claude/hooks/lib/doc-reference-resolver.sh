#!/bin/bash
# Library for resolving documentation references with anchor IDs
# Source this file in hooks that need to reference documentation

# Cache for documentation index to avoid repeated file reads
_DOC_INDEX_CACHE=""
_DOC_INDEX_FILE=""

# Resolve a documentation reference to Read command format
# Usage: resolve_doc_ref "file.md#anchor-id"
# Returns: "Read /workspace/main/docs/project/file.md lines START-END"
resolve_doc_ref() {
    local ref="$1"
    local index_file="${2:-/workspace/main/docs/.index.json}"

    # Parse reference
    if [[ $ref =~ ^([^#]+)#([a-z0-9_-]+)$ ]]; then
        local file="${BASH_REMATCH[1]}"
        local anchor="${BASH_REMATCH[2]}"
    else
        echo "ERROR: Invalid reference format: $ref" >&2
        return 1
    fi

    # Normalize file path
    if [[ ! $file =~ ^docs/ ]]; then
        if [ -f "/workspace/main/docs/project/$file" ]; then
            file="docs/project/$file"
        elif [ -f "/workspace/main/docs/$file" ]; then
            file="docs/$file"
        else
            echo "ERROR: Could not find file: $file" >&2
            return 1
        fi
    fi

    # Check if index exists
    if [ ! -f "$index_file" ]; then
        # Fallback: just return file reference without line numbers
        echo "Read /workspace/main/$file (section #$anchor)"
        return 0
    fi

    # Query index for line numbers with caching
    if command -v jq &> /dev/null; then
        # Load index into cache if not already loaded or if index file changed
        if [ -z "$_DOC_INDEX_CACHE" ] || [ "$_DOC_INDEX_FILE" != "$index_file" ]; then
            _DOC_INDEX_CACHE=$(cat "$index_file" 2>/dev/null)
            _DOC_INDEX_FILE="$index_file"
        fi

        # Parse from cache instead of re-reading file
        local result=$(echo "$_DOC_INDEX_CACHE" | jq -r ".\"$file\".\"$anchor\" // null" 2>/dev/null)

        if [ "$result" != "null" ] && [ -n "$result" ]; then
            local start=$(echo "$result" | jq -r '.start')
            local end=$(echo "$result" | jq -r '.end')
            echo "Read /workspace/main/$file lines $start-$end"
        else
            # Fallback: anchor not in index
            echo "Read /workspace/main/$file (section #$anchor)"
        fi
    else
        # Fallback: jq not available
        echo "Read /workspace/main/$file (section #$anchor)"
    fi
}

# Resolve and format for display with section title
# Usage: resolve_doc_ref_display "file.md#anchor-id" "Section Description"
resolve_doc_ref_display() {
    local ref="$1"
    local description="$2"

    local resolved=$(resolve_doc_ref "$ref")

    if [ -n "$description" ]; then
        echo "   $resolved"
        echo "   ($description)"
    else
        echo "   $resolved"
    fi
}
