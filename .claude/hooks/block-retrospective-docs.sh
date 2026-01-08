#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-retrospective-docs.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Block retrospective documentation creation
#
# PURPOSE: Prevent creation of retrospective documents that chronicle past
#          decisions, fixes, or development processes
#
# TRIGGER: PreToolUse (Write, Edit)
#
# POLICY: See CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY
#
# DETECTION STRATEGY:
# 1. Check file PATH for prohibited directories (mistakes/, lessons-learned/, etc.)
# 2. Check FILENAME for retrospective patterns (summary, analysis, etc.)
# 3. Check CONTENT for retrospective indicators
# 4. BLOCK if: path violation OR (filename + content violation)
# 5. WARN if: filename violation without content confirmation
#
# EXCEPTIONS:
# 1. README.md, changelog.md, todo.md (allowed files)
# 2. .claude/retrospectives/*.json (structured skill output)
# 3. .claude/skills/retrospective/ and learn-from-mistakes/ (skill files)
# 4. User explicitly requests documentation creation
#
# BLOCKED PATTERNS:
# - mistakes/, lessons-learned/, postmortem/, decision-log/ directories
# - Filenames: summary, analysis, retrospective, postmortem, lessons-learned
# - Content: "What Was Implemented", "Lessons Learned", "Root Cause Analysis"

# Source JSON output helper
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")

# Only validate Write and Edit tools
if [[ "$TOOL_NAME" != "Write" && "$TOOL_NAME" != "Edit" ]]; then
    exit 0
fi

# Extract file path - try multiple possible JSON keys
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .file_path // .path // .parameters.file_path // ""' 2>/dev/null || echo "")

if [[ -z "$FILE_PATH" ]]; then
    exit 0
fi

# Only check markdown files
if [[ ! "$FILE_PATH" =~ \.md$ ]]; then
    exit 0
fi

# Skip allowed files
if [[ "$FILE_PATH" =~ README\.md$ ]] || \
   [[ "$FILE_PATH" =~ changelog\.md$ ]] || \
   [[ "$FILE_PATH" =~ CHANGELOG\.md$ ]] || \
   [[ "$FILE_PATH" =~ todo\.md$ ]]; then
    exit 0
fi

# =============================================================================
# RETROSPECTIVE SKILL OUTPUT EXCEPTION
# =============================================================================
# Allow structured outputs from the retrospective skill process:
# - .claude/retrospectives/*.json (mistakes.json, retrospectives.json)
# - Action item tracking files
#
# This distinction per user request:
# - BLOCK: Ad-hoc retrospective prose, lessons-learned docs, post-mortems
# - ALLOW: Structured machine-readable tracking from retrospective workflow

if [[ "$FILE_PATH" =~ \.claude/retrospectives/.*\.json$ ]]; then
    exit 0
fi

# Allow retrospective skill to update its own skill file
if [[ "$FILE_PATH" =~ \.claude/skills/retrospective/ ]] || \
   [[ "$FILE_PATH" =~ \.claude/skills/learn-from-mistakes/ ]]; then
    exit 0
fi

# Extract filename and content
FILENAME=$(basename "$FILE_PATH")
CONTENT=$(echo "$INPUT" | jq -r '.tool_input.content // .content // .parameters.content // ""' 2>/dev/null || echo "")

# =============================================================================
# DETECTION PHASE 1: Prohibited directory paths (ALWAYS BLOCK)
# =============================================================================
# NOTE: .claude/retrospectives/ is ALLOWED (handled above) for structured JSON
#       tracking. These patterns block ad-hoc prose directories elsewhere.

PROHIBITED_PATHS=(
    "mistakes/"
    "lessons-learned"
    "post-mortem"
    "postmortem"
    "decision-log"
    "decision-logs"
)
# NOTE: "retrospective" pattern removed - .claude/retrospectives/ is allowed
#       for structured skill output. Other retrospective directories still blocked
#       by filename/content analysis below.

for pattern in "${PROHIBITED_PATHS[@]}"; do
    if [[ "$FILE_PATH" =~ $pattern ]]; then
        MESSAGE="## 🚨 RETROSPECTIVE DOCUMENTATION VIOLATION DETECTED AND BLOCKED

**File**: \`$FILE_PATH\`
**Violation**: Path contains prohibited directory pattern: \`$pattern\`
**Tool**: $TOOL_NAME

## ⚠️ CRITICAL - RETROSPECTIVE DOCUMENTATION PROHIBITED

Per CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY:

**PROHIBITED PATTERNS**:
❌ Post-implementation analysis reports
❌ \"Lessons learned\" documents
❌ Debugging chronicles or problem-solving narratives
❌ Development process retrospectives
❌ Fix documentation duplicating information in code/commits
❌ Decision chronicles documenting past decision-making phases

**AUTOMATIC ACTION TAKEN**:
- File creation blocked
- No retrospective document created

**CORRECT APPROACH**:

✅ **Inline Comments in Hook/Config Files**:
\`\`\`bash
# PATTERN EVOLUTION HISTORY:
# - 2025-10-31: Added pattern X after mistake Y occurred
# - PREVENTS: Specific mistake pattern Z
\`\`\`

✅ **Git Commit Messages**:
\`\`\`
Fix: Add validation to prevent mistake X

Root cause: Configuration gap Y
Prevention: Hook now validates Z
Testing: Attempted to reproduce - correctly blocked
\`\`\`

✅ **Code Comments for Rationale**:
\`\`\`java
// Using Arena API instead of traditional approach because:
// - 3x performance improvement (benchmarked)
// - Meets 512MB target with 96.9% safety margin
\`\`\`

## WHERE TO DOCUMENT

- **Rationale**: Git commit message with the change
- **Why this approach**: Code comments inline with implementation
- **Context**: Inline comments in hook/config files explaining pattern evolution
- **Lessons learned**: Inline comments showing pattern evolution history

## Protocol Reference

See: /workspace/main/CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY

**WHY THIS MATTERS**:
- Retrospectives duplicate information already in git history
- They become stale and misleading over time
- Inline documentation stays synchronized with code
- Git commit messages provide chronological context"

        output_hook_error "PreToolUse" "$MESSAGE"
        exit 0
    fi
done

# =============================================================================
# DETECTION PHASE 2: Retrospective filename patterns
# =============================================================================

RETROSPECTIVE_FILENAME_PATTERNS=(
    "SUMMARY"
    "summary"
    "LESSONS"
    "lessons-learned"
    "lessons_learned"
    "retrospective"
    "RETROSPECTIVE"
    "postmortem"
    "POST-MORTEM"
    "post-mortem"
    "analysis-report"
    "ANALYSIS"
    "analysis"
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

MATCHED_FILENAME_PATTERN=""
for pattern in "${RETROSPECTIVE_FILENAME_PATTERNS[@]}"; do
    if [[ "$FILENAME" =~ $pattern ]]; then
        MATCHED_FILENAME_PATTERN="$pattern"
        break
    fi
done

# =============================================================================
# DETECTION PHASE 3: Retrospective content patterns
# =============================================================================

RETROSPECTIVE_CONTENT_PATTERNS=(
    # Pattern evolution and analysis
    "Mistake (Summary|Analysis)"
    "Root Cause Analysis"
    "Evidence-Based Decision Process"
    "Decision Process"
    "Phase [0-9]+:.*Requirements"
    "Lessons [Ll]earned"
    "What [Ww]e [Ll]earned"
    "Post-[Mm]ortem"
    "What [Ww]ent [Ww]rong"
    "Prevention [Ss]trategy"
    "Historical Context"
    "Final.*Decision"
    "^# (Fix|Mistake|Error|Problem).*Analysis"
    # Implementation retrospectives
    "What Was Implemented"
    "What was implemented"
    "Files Created"
    "Files Modified"
    "Changes Made"
    "Success Criteria.*Achieved"
    "Performance Results"
    "Implementation Summary"
    "Task Summary"
    "Post-Implementation"
    "Development Process"
    "Development Process Retrospective"
    "Fix Applied"
    "What We Did"
    "Completed Items"
)

MATCHED_CONTENT_PATTERNS=()
for pattern in "${RETROSPECTIVE_CONTENT_PATTERNS[@]}"; do
    if echo "$CONTENT" | grep -qiE "$pattern"; then
        MATCHED_CONTENT_PATTERNS+=("$pattern")
    fi
done

# =============================================================================
# ENFORCEMENT DECISION TREE
# =============================================================================

# BLOCK: Filename AND content both match retrospective patterns
if [[ -n "$MATCHED_FILENAME_PATTERN" ]] && [[ ${#MATCHED_CONTENT_PATTERNS[@]} -gt 0 ]]; then
    MESSAGE="## 🚨 RETROSPECTIVE DOCUMENT CREATION BLOCKED

**File**: \`$FILENAME\`
**Filename Pattern**: \`$MATCHED_FILENAME_PATTERN\`
**Content Violations**: ${#MATCHED_CONTENT_PATTERNS[@]} retrospective indicators detected

## ⚠️ CRITICAL - RETROSPECTIVE DOCUMENTATION PROHIBITED

Per CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY, do NOT create retrospective documents.

**Detected Retrospective Indicators in Content**:
$(printf '  - %s\n' "${MATCHED_CONTENT_PATTERNS[@]}")

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

# BLOCK: Content matches retrospective patterns strongly (even without suspicious filename)
if [[ ${#MATCHED_CONTENT_PATTERNS[@]} -ge 3 ]]; then
    MESSAGE="## 🚨 RETROSPECTIVE CONTENT DETECTED AND BLOCKED

**File**: \`$FILE_PATH\`
**Content Violations**: ${#MATCHED_CONTENT_PATTERNS[@]} retrospective patterns detected

## ⚠️ CONTENT ANALYSIS VIOLATION

The content of this markdown file contains multiple retrospective patterns that
chronicle past mistakes, decisions, or development processes.

**Detected Retrospective Patterns**:
$(printf '  - %s\n' "${MATCHED_CONTENT_PATTERNS[@]}")

**AUTOMATIC ACTION TAKEN**:
- File creation blocked
- Retrospective content prevented

**CORRECT DOCUMENTATION APPROACH**:

1. **For Mistake Prevention** → Inline comments in hooks:
   \`\`\`bash
   # Added 2025-10-31 after X mistake
   # PREVENTS: Pattern Y from recurring
   \`\`\`

2. **For Design Rationale** → Code comments:
   \`\`\`java
   // Using approach X because: [brief rationale]
   \`\`\`

3. **For Change Context** → Git commit message:
   \`\`\`
   Add feature X to prevent mistake Y

   Context: [what happened]
   Fix: [what changed]
   Testing: [verification performed]
   \`\`\`

## Protocol Reference

See: /workspace/main/CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY"

    output_hook_error "PreToolUse" "$MESSAGE"
    exit 0
fi

# WARN: Filename suggests retrospective but content is unclear
if [[ -n "$MATCHED_FILENAME_PATTERN" ]]; then
    MESSAGE="## ⚠️ WARNING: Filename Suggests Retrospective Document

**File**: \`$FILENAME\`
**Matched Pattern**: \`$MATCHED_FILENAME_PATTERN\`

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

# ALLOW: No violations detected
exit 0
