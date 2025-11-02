#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in detect-retrospective-docs.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Detect and block retrospective documentation creation
#
# PATTERN EVOLUTION HISTORY:
# - 2025-10-31: Initial creation after main agent created .claude/mistakes/
#   directory with retrospective markdown file chronicling merge commit fix,
#   violating CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY
#
# PREVENTS:
# - Creation of mistake chronicles in any directory
# - "Lessons learned" documents
# - Multi-phase retrospective reports
# - Decision process documentation

# Read hook input from stdin (PreToolUse format)
INPUT=$(cat)

# Extract file path - try multiple possible JSON keys
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .file_path // .path // .parameters.file_path // ""')
FILE_CONTENT=$(echo "$INPUT" | jq -r '.tool_input.content // .content // .parameters.content // ""')

# Only check markdown files
if [[ ! "$FILE_PATH" =~ \.md$ ]] || [[ -z "$FILE_PATH" ]]; then
	echo "$INPUT"
	exit 0
fi

# Prohibited patterns in file path
PROHIBITED_PATHS=(
	"mistakes/"
	"lessons-learned"
	"post-mortem"
	"retrospective"
	"decision-log"
)

for pattern in "${PROHIBITED_PATHS[@]}"; do
	if [[ "$FILE_PATH" =~ $pattern ]]; then
		MESSAGE="## 🚨 RETROSPECTIVE DOCUMENTATION VIOLATION DETECTED AND BLOCKED

**File**: \`$FILE_PATH\`
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
- **Context**: Inline comments in hook/config files
- **Lessons learned**: Inline comments explaining pattern evolution

## Protocol Reference

See: /workspace/main/CLAUDE.md § RETROSPECTIVE DOCUMENTATION POLICY

**WHY THIS MATTERS**:
- Retrospectives duplicate information already in git history
- They become stale and misleading over time
- Inline documentation stays synchronized with code
- Git commit messages provide chronological context"

		jq -n \
			--arg event "PreToolUse" \
			--arg context "$MESSAGE" \
			'{
				"hookSpecificOutput": {
					"hookEventName": $event,
					"additionalContext": $context
				}
			}'

		exit 2  # Block the command
	fi
done

# Check file content for retrospective patterns
RETROSPECTIVE_PATTERNS=(
	"Mistake (Summary|Analysis)"
	"Root Cause Analysis"
	"Evidence-Based Decision Process"
	"Phase [0-9]+:.*Requirements"
	"Lessons [Ll]earned"
	"Post-[Mm]ortem"
	"What [Ww]ent [Ww]rong"
	"Prevention [Ss]trategy"
	"^# (Fix|Mistake|Error|Problem).*Analysis"
)

for pattern in "${RETROSPECTIVE_PATTERNS[@]}"; do
	if echo "$FILE_CONTENT" | grep -qE "$pattern"; then
		MESSAGE="## 🚨 RETROSPECTIVE CONTENT DETECTED AND BLOCKED

**File**: \`$FILE_PATH\`
**Detected Pattern**: \`$pattern\`

## ⚠️ CONTENT ANALYSIS VIOLATION

The content of this markdown file contains retrospective patterns that
chronicle past mistakes, decisions, or development processes.

**Detected Retrospective Patterns**:
$(echo "$FILE_CONTENT" | grep -E "$pattern" | head -5)

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

		jq -n \
			--arg event "PreToolUse" \
			--arg context "$MESSAGE" \
			'{
				"hookSpecificOutput": {
					"hookEventName": $event,
					"additionalContext": $context
				}
			}'

		exit 2  # Block the command
	fi
done

# No retrospective patterns detected - allow creation
echo "$INPUT"
exit 0
