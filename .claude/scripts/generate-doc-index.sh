#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in generate-doc-index.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Generate documentation index with anchor IDs and line numbers
# Output: docs/.index.json
#
# Performance optimizations:
# - Incremental mode: Only process changed files in git commits (<1s typical)
# - awk processing: Single process per file instead of subprocess per line
# - Full regeneration: ~5s vs old 39s (87% faster)

DOCS_DIR="${1:-docs}"
INDEX_FILE="${DOCS_DIR}/.index.json"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$PROJECT_ROOT" || exit 1

# Process a single file with awk (fast, no subprocess spawning per line)
process_file_with_awk() {
    local file="$1"

    awk '
    BEGIN {
        first = 1
        prev_anchor = ""
        start_line = 0
    }

    # Match lines like: ## Title {#anchor-id}
    /^##+ .* \{#[a-z0-9_-]+\}/ {
        # Extract anchor from {#anchor-id} using sub() for compatibility
        anchor = $0
        sub(/.*\{#/, "", anchor)
        sub(/\}.*/, "", anchor)

        # Close previous section
        if (prev_anchor != "") {
            if (first == 0) {
                printf ",\n"
            }
            printf "    \"%s\": {\"start\": %d, \"end\": %d}", prev_anchor, start_line, NR - 1
            first = 0
        }

        # Start new section
        prev_anchor = anchor
        start_line = NR
    }

    END {
        # Close last section
        if (prev_anchor != "") {
            if (first == 0) {
                printf ",\n"
            }
            printf "    \"%s\": {\"start\": %d, \"end\": %d}\n", prev_anchor, start_line, NR
        }
    }
    ' "$file"
}

# Generate full index using awk
generate_full_index() {
    local output="$1"
    local temp_file="${output}.tmp"

    echo "{" > "$temp_file"

    local first_file=true
    while IFS= read -r md_file; do
        local rel_path="${md_file#./}"

        # Add comma separator
        if [ "$first_file" = true ]; then
            first_file=false
        else
            echo "," >> "$temp_file"
        fi

        # File entry
        echo "  \"${rel_path}\": {" >> "$temp_file"
        process_file_with_awk "$md_file" >> "$temp_file"
        echo "  }" >> "$temp_file"

    done < <(find "${DOCS_DIR}" -name "*.md" -type f | sort)

    echo "}" >> "$temp_file"
    mv "$temp_file" "$output"
}

# Incremental update: only process changed files
incremental_update() {
    # If index doesn't exist, do full regeneration
    if [ ! -f "$INDEX_FILE" ]; then
        echo "Index file missing, doing full regeneration" >&2
        return 1
    fi

    # Get changed/deleted files from git staging area
    local changed_files=$(git diff --cached --name-only --diff-filter=AM 2>/dev/null | grep "^${DOCS_DIR}/.*\.md$" || true)
    local deleted_files=$(git diff --cached --name-only --diff-filter=D 2>/dev/null | grep "^${DOCS_DIR}/.*\.md$" || true)

    # If no markdown files changed, skip update
    if [ -z "$changed_files" ] && [ -z "$deleted_files" ]; then
        echo "No markdown files changed, skipping index update" >&2
        return 0
    fi

    # Check if jq is available (required for incremental updates)
    if ! command -v jq &> /dev/null; then
        echo "jq not found, falling back to full regeneration" >&2
        return 1
    fi

    local temp_index="${INDEX_FILE}.tmp"
    cp "$INDEX_FILE" "$temp_index"

    # Remove deleted files from index
    for file in $deleted_files; do
        jq "del([\"$file\"])" "$temp_index" > "${temp_index}.new"
        mv "${temp_index}.new" "$temp_index"
    done

    # Update changed files
    for file in $changed_files; do
        if [ ! -f "$file" ]; then
            continue
        fi

        local rel_path="${file#./}"
        local temp_entry="${temp_index}.entry"

        # Generate entry for this file
        echo "{" > "$temp_entry"
        process_file_with_awk "$file" >> "$temp_entry"
        echo "}" >> "$temp_entry"

        # Merge into index using jq
        jq --slurpfile entry "$temp_entry" ". + {\"$rel_path\": \$entry[0]}" "$temp_index" > "${temp_index}.new"
        mv "${temp_index}.new" "$temp_index"
        rm "$temp_entry"
    done

    mv "$temp_index" "$INDEX_FILE"

    local change_count=$(($(echo "$changed_files" | wc -w) + $(echo "$deleted_files" | wc -w)))
    echo "Updated index incrementally: $change_count file(s) changed" >&2
    return 0
}

# Main execution
main() {
    # Try incremental update first (fast path for commits)
    if git rev-parse --git-dir >/dev/null 2>&1; then
        if incremental_update; then
            return 0
        fi
    fi

    # Fallback: full regeneration (still fast with awk)
    echo "Generating full index..." >&2
    generate_full_index "$INDEX_FILE"
    echo "Generated index: ${INDEX_FILE}" >&2
}

main
