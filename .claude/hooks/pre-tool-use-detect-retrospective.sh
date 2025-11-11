#!/bin/bash
# Pre-Tool-Use Hook: Block retrospective document creation
#
# PURPOSE: Prevent creation of retrospective documents by blocking Write tool
#          when filename patterns suggest retrospective content
#
# ENFORCEMENT: Runs BEFORE Write tool executes (preventive, not detective)
#
# POLICY: See CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY
#
# PATTERN: When creating .md files with retrospective indicators in name,
#          remind agent to check policy questions before proceeding
#
# Triggers: PreToolUse (Write tool, *.md files)

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in pre-tool-use-detect-retrospective.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source libraries
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")

# Only validate Write tool
if [[ "$TOOL_NAME" != "Write" ]]; then
    exit 0
fi

log_hook_start "pre-tool-use-detect-retrospective" "PreToolUse"

# Extract file path
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty' 2>/dev/null || echo "")

if [[ -z "$FILE_PATH" ]]; then
    log_hook_success "pre-tool-use-detect-retrospective" "PreToolUse" "No file path, skipping"
    exit 0
fi

# Only check markdown files
if [[ ! "$FILE_PATH" =~ \.md$ ]]; then
    log_hook_success "pre-tool-use-detect-retrospective" "PreToolUse" "Not markdown file, skipping"
    exit 0
fi

# Extract filename from path
FILENAME=$(basename "$FILE_PATH")

# Retrospective filename patterns
RETROSPECTIVE_PATTERNS=(
    "SUMMARY"
    "summary"
    "LESSONS"
    "lessons-learned"
    "lessons_learned"
    "retrospective"
    "RETROSPECTIVE"
    "postmortem"
    "POST-MORTEM"
    "analysis-report"
    "ANALYSIS"
    "implementation-summary"
    "IMPLEMENTATION-SUMMARY"
    "development-log"
    "DEVELOPMENT-LOG"
    "task-summary"
    "TASK-SUMMARY"
    "fix-summary"
    "FIX-SUMMARY"
    "optimization-summary"
    "OPTIMIZATION-SUMMARY"
)

# Check if filename matches retrospective patterns
MATCHED_PATTERN=""
for pattern in "${RETROSPECTIVE_PATTERNS[@]}"; do
    if [[ "$FILENAME" =~ $pattern ]]; then
        MATCHED_PATTERN="$pattern"
        break
    fi
done

if [[ -z "$MATCHED_PATTERN" ]]; then
    log_hook_success "pre-tool-use-detect-retrospective" "PreToolUse" "Filename does not match retrospective patterns"
    exit 0
fi

# Extract file content being written
CONTENT=$(echo "$INPUT" | jq -r '.tool_input.content // empty' 2>/dev/null || echo "")

# Check content for retrospective indicators
CONTENT_INDICATORS=(
    "What Was Implemented"
    "What was implemented"
    "Files Created"
    "Files Modified"
    "Changes Made"
    "Success Criteria.*Achieved"
    "Performance Results"
    "Implementation Summary"
    "Task Summary"
    "Lessons Learned"
    "Post-Implementation"
    "Development Process"
    "Fix Applied"
    "What We Did"
    "Completed Items"
)

CONTENT_VIOLATIONS=()
for indicator in "${CONTENT_INDICATORS[@]}"; do
    if echo "$CONTENT" | grep -qiE "$indicator"; then
        CONTENT_VIOLATIONS+=("$indicator")
    fi
done

# If both filename AND content suggest retrospective, block
if [[ ${#CONTENT_VIOLATIONS[@]} -gt 0 ]]; then
    log_hook_blocked "pre-tool-use-detect-retrospective" "PreToolUse" "Blocked retrospective document: $FILENAME"

    MESSAGE="## 🚨 RETROSPECTIVE DOCUMENT CREATION BLOCKED

**File**: \`$FILENAME\`
**Matched Pattern**: \`$MATCHED_PATTERN\`
**Content Violations**: ${#CONTENT_VIOLATIONS[@]} retrospective indicators detected

## ⚠️ CRITICAL - RETROSPECTIVE DOCUMENTATION PROHIBITED

Per CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY, do NOT create retrospective documents.

**Detected Retrospective Indicators in Content**:
$(printf '  - %s\n' "${CONTENT_VIOLATIONS[@]}")

**BEFORE creating this file, answer these questions**:

1. **Is this documenting what happened (retrospective) or what to do (forward-looking)?**
   - If \"what happened\" → Put in **commit message**, not file
   - If \"what to do\" → Proceed (but check filename)

2. **Could this go in a commit message instead?**
   - If YES → **Use commit message** (what was implemented, files created, results)
   - If NO → Verify it's truly forward-looking usage documentation

3. **Will this be useful after the fix, or only during?**
   - If \"only during\" → **Don't create file**, use commit message
   - If \"useful after\" → Verify it's not duplicating existing docs

## Retrospective Content Belongs In:

✅ **Commit Messages**:
   - What was implemented
   - Performance results achieved
   - Files created/modified
   - Success criteria met
   - Why changes prevent recurrence

✅ **Inline Comments**:
   - Pattern evolution rationale (forward-looking WHY)
   - Edge case explanations
   - Alternative approaches considered

## Forward-Looking Content Belongs In:

✅ **Documentation Files** (*.md):
   - **Usage guides**: How to use the feature
   - **API docs**: How to call the functions
   - **Architecture docs**: How components interact
   - **Reference docs**: Specifications, benchmarks

## Common Mistake Pattern:

❌ **WRONG**: Create \"OPTIMIZATION-SUMMARY.md\" with:
   - \"What Was Implemented\" (retrospective)
   - \"Files Created\" (retrospective)
   - \"Success Criteria Achieved\" (retrospective)

✅ **CORRECT**:
   - Put retrospective content in **commit message**
   - Forward-looking content likely already exists in usage guide
   - If new usage content needed, add to existing guide or create new guide with non-retrospective name

## Recommended Action:

1. **Review the content** you're about to write
2. **Separate retrospective from forward-looking**:
   - Retrospective → Draft commit message
   - Forward-looking → Check if already documented
3. **If forward-looking content is new**:
   - Choose non-retrospective filename (e.g., \"hook-usage-guide.md\" not \"hook-summary.md\")
   - Focus on HOW TO USE, not WHAT WAS DONE

## To Proceed:

If you believe this file is NOT retrospective:
1. Rename to non-retrospective filename
2. Remove retrospective content sections
3. Verify content is forward-looking (usage, how-to, reference)

If this file IS retrospective:
1. Cancel this Write operation
2. Put retrospective content in commit message
3. Add forward-looking content to existing docs (if not already there)

**See**: CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY"

    output_hook_error "PreToolUse" "$MESSAGE"
    exit 0
fi

# Filename matches but content doesn't strongly suggest retrospective
# Warn but allow (could be false positive)
if [[ -n "$MATCHED_PATTERN" ]]; then
    log_hook_warning "pre-tool-use-detect-retrospective" "PreToolUse" "Filename suggests retrospective: $FILENAME (but content unclear)"

    MESSAGE="## ⚠️ WARNING: Filename Suggests Retrospective Document

**File**: \`$FILENAME\`
**Matched Pattern**: \`$MATCHED_PATTERN\`

**Before creating this file, verify it's NOT retrospective** using these questions:

1. Is this documenting what happened (retrospective) or what to do (forward-looking)?
2. Could this go in a commit message instead?
3. Will this be useful after the fix, or only during?

**If retrospective**: Cancel Write and use commit message instead.
**If forward-looking**: Consider renaming to avoid confusion.

See: CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY"

    output_hook_warning "PreToolUse" "$MESSAGE"
    exit 0
fi

log_hook_success "pre-tool-use-detect-retrospective" "PreToolUse" "File creation allowed"
exit 0
