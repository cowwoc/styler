#!/bin/bash
set -euo pipefail

# Documentation Synchronization Verification Script
# Ensures Claude and human documentation files maintain synchronized rule titles

trap 'echo "ERROR in verify-doc-sync.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Resolve absolute path to docs directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)" || {
    echo "❌ ERROR: Failed to resolve script directory" >&2
    exit 1
}

DOCS_DIR=""
if [[ -d "$SCRIPT_DIR/../docs" ]]; then
    DOCS_DIR="$(cd "$SCRIPT_DIR/../docs" && pwd)"
elif [[ -d "$SCRIPT_DIR/../../docs" ]]; then
    DOCS_DIR="$(cd "$SCRIPT_DIR/../../docs" && pwd)"
else
    echo "❌ ERROR: Cannot find docs directory relative to script location: $SCRIPT_DIR" >&2
    echo "Tried: $SCRIPT_DIR/../docs and $SCRIPT_DIR/../../docs" >&2
    exit 1
fi

# Debug information
DEBUG="${DEBUG:-false}"
if [[ "$DEBUG" == "true" ]]; then
    echo "🔍 DEBUG: Script directory: $SCRIPT_DIR"
    echo "🔍 DEBUG: Docs directory: $DOCS_DIR"
    echo ""
fi

# Verify docs directory exists
if [[ ! -d "$DOCS_DIR" ]]; then
    echo "❌ ERROR: Documentation directory not found: $DOCS_DIR"
    echo "   Script location: $SCRIPT_DIR"
    echo "   Expected structure: /workspace/main/docs/"
    echo ""
    echo "💡 FIX: Run this script from the correct location:"
    echo "   cd /workspace/main"
    echo "   ./.claude/hooks/verify-doc-sync.sh"
    echo ""
    exit 1
fi
SCRIPT_NAME=$(basename "$0")
EXIT_CODE=0

echo "=== DOCUMENTATION SYNCHRONIZATION VERIFICATION ===" >&2
echo "Checking rule title consistency between Claude and human files" >&2
echo "" >&2

# Function to extract rule titles from a file
extract_rule_titles() {
    local file="$1"
    if [[ ! -f "$file" ]]; then
        echo "ERROR: File not found: $file" >&2
        return 1
    fi
    
    # Extract only actual rules, excluding navigation/structural sections
    local rules
    rules=$(grep "^### " "$file" 2>/dev/null | \
        grep -v "Language-Specific Implementation" | \
        grep -v "Related Documentation" | \
        grep -v "Navigation" | \
        grep -v "Claude Detection Patterns" | \
        grep -v "Integration with" | \
        sed 's/^### //' | sed 's/\s*$//' || true)
    
    if [[ -z "$rules" ]]; then
        echo "WARNING: No rules found in file: $(basename "$file")" >&2
        return 1
    fi
    
    echo "$rules"
}

# Function to compare rule titles between two files
compare_rule_titles() {
    local claude_file="$1"
    local human_file="$2"
    local category="$3"
    
    echo "🔍 CHECKING: $category"
    echo "Claude file: $(basename "$claude_file")"
    echo "Human file:  $(basename "$human_file")"
    
    # Check if both files exist
    if [[ ! -f "$claude_file" ]]; then
        echo "❌ ERROR: Claude file not found: $claude_file"
        EXIT_CODE=1
        return 1
    fi
    
    if [[ ! -f "$human_file" ]]; then
        echo "❌ ERROR: Human file not found: $human_file"
        EXIT_CODE=1
        return 1
    fi
    
    # Extract titles from both files
    local claude_titles
    local human_titles
    
    claude_titles=$(extract_rule_titles "$claude_file")
    local claude_exit_code=$?
    
    human_titles=$(extract_rule_titles "$human_file")
    local human_exit_code=$?
    
    # Check if extraction failed
    if [[ $claude_exit_code -ne 0 ]]; then
        echo "❌ ERROR: Failed to extract rules from Claude file: $(basename "$claude_file")"
        echo "   File path: $claude_file"
        echo "   💡 FIX: Ensure file has ### headers for rule sections"
        echo ""
        EXIT_CODE=1
        return 1
    fi
    
    if [[ $human_exit_code -ne 0 ]]; then
        echo "❌ ERROR: Failed to extract rules from human file: $(basename "$human_file")"
        echo "   File path: $human_file"
        echo "   💡 FIX: Ensure file has ### headers for rule sections"
        echo ""
        EXIT_CODE=1
        return 1
    fi
    
    # Count titles (use xargs to trim whitespace from wc output)
    local claude_count=$(echo "$claude_titles" | wc -l | xargs)
    local human_count=$(echo "$human_titles" | wc -l | xargs)
    
    echo "Claude rules: $claude_count"
    echo "Human rules:  $human_count"
    
    # Check if counts match
    if [[ "$claude_count" != "$human_count" ]]; then
        echo "❌ ERROR: Rule count mismatch for $category"
        echo "Claude has $claude_count rules, Human has $human_count rules"
        echo ""
        echo "📋 Claude rules found:"
        echo "$claude_titles" | sed 's/^/  - /'
        echo ""
        echo "📋 Human rules found:"
        echo "$human_titles" | sed 's/^/  - /'
        echo ""
        echo "💡 FIX: Ensure both files have the same number of ### headers (excluding Navigation sections)"
        echo ""
        EXIT_CODE=1
        return 1
    fi
    
    # Compare titles line by line
    local differences=$(diff <(echo "$claude_titles") <(echo "$human_titles") 2>/dev/null || true)
    
    if [[ -n "$differences" ]]; then
        echo "❌ ERROR: Rule title mismatch for $category"
        echo ""
        echo "📋 Side-by-side comparison:"
        echo "$(printf "%-40s | %s" "Claude File" "Human File")"
        echo "$(printf "%-40s | %s" "$(printf '%*s' 40 '' | tr ' ' '-')" "$(printf '%*s' 40 '' | tr ' ' '-')")"
        
        # Create detailed side-by-side comparison
        local claude_array=()
        local human_array=()
        
        while IFS= read -r line; do
            claude_array+=("$line")
        done <<< "$claude_titles"
        
        while IFS= read -r line; do
            human_array+=("$line")
        done <<< "$human_titles"
        
        local max_count=$((claude_count > human_count ? claude_count : human_count))
        
        for ((i=0; i<max_count; i++)); do
            local claude_rule="${claude_array[i]:-<MISSING>}"
            local human_rule="${human_array[i]:-<MISSING>}"
            
            local status=""
            if [[ "$claude_rule" != "$human_rule" ]]; then
                status=" ❌"
            else
                status=" ✅"
            fi
            
            printf "%-40s | %-40s%s\n" "$claude_rule" "$human_rule" "$status"
        done
        
        echo ""
        echo "💡 FIX: Ensure rule titles match exactly between Claude and human files"
        echo "   Common issues:"
        echo "   - Missing/extra spaces or punctuation"
        echo "   - Different capitalization"
        echo "   - Missing rules in one file"
        echo ""
        EXIT_CODE=1
        return 1
    fi
    
    echo "✅ SUCCESS: $category rules are synchronized ($claude_count rules match)"
    echo "   Claude file: $(basename "$claude_file")"
    echo "   Human file:  $(basename "$human_file")" 
    echo ""
    return 0
}

# Function to check file pairs exist and verify synchronization
verify_file_pair() {
    local claude_pattern="$1"
    local human_pattern="$2"
    local category="$3"
    
    local claude_file="$DOCS_DIR/$claude_pattern"
    local human_file="$DOCS_DIR/$human_pattern"
    
    compare_rule_titles "$claude_file" "$human_file" "$category"
}

# Main verification process
main() {
    echo "Starting synchronization check at $(date)" >&2
    echo "" >&2

    # Verify common rules (|| true prevents set -e from exiting on mismatch)
    verify_file_pair "code-style/common-claude.md" "code-style/common-human.md" "COMMON RULES" || true

    # Verify Java rules
    verify_file_pair "code-style/java-claude.md" "code-style/java-human.md" "JAVA RULES" || true

    # Verify TypeScript rules
    verify_file_pair "code-style/typescript-claude.md" "code-style/typescript-human.md" "TYPESCRIPT RULES" || true

    # Verify Testing rules
    verify_file_pair "code-style/testing-claude.md" "code-style/testing-human.md" "TESTING RULES" || true
    
    echo "=== VERIFICATION SUMMARY ===" >&2
    if [[ $EXIT_CODE -eq 0 ]]; then
        echo "✅ ALL DOCUMENTATION FILES ARE SYNCHRONIZED" >&2
        echo "All rule titles match between Claude and human files." >&2
        echo "Safe to proceed with documentation updates." >&2
    else
        echo "❌ SYNCHRONIZATION ERRORS DETECTED" >&2
        echo "Rule titles do not match between some Claude and human files." >&2
        echo "Fix synchronization issues before proceeding." >&2
    fi

    echo "" >&2
    echo "Verification completed at $(date)" >&2

    # PostToolUse hooks are advisory - always exit 0 to not block
    # The warnings above inform the user of any issues
    exit 0
}

# Function to show usage information
show_usage() {
    echo "Usage: $SCRIPT_NAME [options]"
    echo ""
    echo "Verifies that Claude and human documentation files maintain synchronized rule titles."
    echo ""
    echo "Options:"
    echo "  -h, --help     Show this help message"
    echo ""
    echo "File Pairs Checked:"
    echo "  code-style/common-claude.md      <-> code-style/common-human.md"
    echo "  code-style/java-claude.md        <-> code-style/java-human.md"
    echo "  code-style/typescript-claude.md  <-> code-style/typescript-human.md"
    echo "  code-style/testing-claude.md     <-> code-style/testing-human.md"
    echo ""
    echo "Exit Codes:"
    echo "  0 - All files are synchronized"
    echo "  1 - Synchronization errors detected"
}

# Parse command line arguments
case "${1:-}" in
    -h|--help)
        show_usage
        exit 0
        ;;
    "")
        # No arguments, run main verification
        main
        ;;
    *)
        echo "Error: Unknown option '$1'"
        echo ""
        show_usage
        exit 1
        ;;
esac