#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in generate-doc-index.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Generate documentation index with anchor IDs and line numbers
# Output: docs/.index.json

DOCS_DIR="${1:-docs}"
OUTPUT_FILE="${DOCS_DIR}/.index.json"

# Change to main directory
cd "$(dirname "$0")/.." || exit 1

# Start JSON output
echo "{" > "$OUTPUT_FILE"

first_file=true

# Find all markdown files
while IFS= read -r md_file; do
    # Get relative path from docs/
    rel_path="${md_file#./}"

    # Add comma for subsequent files
    if [ "$first_file" = true ]; then
        first_file=false
    else
        echo "," >> "$OUTPUT_FILE"
    fi

    # Start file entry
    echo "  \"${rel_path}\": {" >> "$OUTPUT_FILE"

    first_anchor=true
    current_section_start=0
    prev_anchor=""

    # Process file line by line
    line_num=0
    while IFS= read -r line || [ -n "$line" ]; do
        line_num=$((line_num + 1))

        # Check if line contains anchor ID
        if echo "$line" | grep -qE '^##+ .* \{#[a-z0-9_-]+\}'; then
            # Extract anchor ID
            anchor=$(echo "$line" | sed -E 's/.*\{#([a-z0-9_-]+)\}.*/\1/')

            # Close previous section if exists
            if [ "$current_section_start" -gt 0 ] && [ -n "$prev_anchor" ]; then
                end_line=$((line_num - 1))
                if [ "$first_anchor" = true ]; then
                    first_anchor=false
                else
                    echo "," >> "$OUTPUT_FILE"
                fi
                echo "    \"${prev_anchor}\": {\"start\": ${current_section_start}, \"end\": ${end_line}}" >> "$OUTPUT_FILE"
            fi

            # Start new section
            current_section_start=$line_num
            prev_anchor="$anchor"
        fi
    done < "$md_file"

    # Close last section
    if [ "$current_section_start" -gt 0 ] && [ -n "$prev_anchor" ]; then
        end_line=$line_num
        if [ "$first_anchor" = true ]; then
            first_anchor=false
        else
            echo "," >> "$OUTPUT_FILE"
        fi
        echo "    \"${prev_anchor}\": {\"start\": ${current_section_start}, \"end\": ${end_line}}" >> "$OUTPUT_FILE"
    fi

    # Close file entry
    echo "  }" >> "$OUTPUT_FILE"

done < <(find "${DOCS_DIR}" -name "*.md" -type f | sort)

# Close JSON
echo "}" >> "$OUTPUT_FILE"

echo "Generated index: ${OUTPUT_FILE}" >&2
