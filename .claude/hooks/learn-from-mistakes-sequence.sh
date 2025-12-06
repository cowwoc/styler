#!/bin/bash
# Enforces learn-from-mistakes sequence: analyze → prevent → THEN fix
#
# TRIGGER: UserPromptSubmit when user mentions "learn from mistakes"
# PURPOSE: Reminds agent to analyze and implement prevention BEFORE fixing
# ADDED: After mistake where agent immediately fixed problem without learning (session ec47cea5)
# PREVENTS: Agents jumping straight to fixes without implementing preventative measures

set -euo pipefail

trap 'echo "ERROR in learn-from-mistakes-sequence.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read hook input from stdin
INPUT=$(cat)
USER_PROMPT=$(echo "$INPUT" | jq -r '.userPrompt // ""' | tr '[:upper:]' '[:lower:]')

# Check if user is invoking learn-from-mistakes pattern
if [[ "$USER_PROMPT" =~ learn.*from.*mistake ]] || [[ "$USER_PROMPT" =~ mistake.*learn ]]; then
	MESSAGE="## 🎓 LEARN-FROM-MISTAKES SEQUENCE REMINDER

**MANDATORY**: When learning from a mistake, you MUST follow this sequence:

1. **ANALYZE** - Identify root cause of the mistake
2. **PREVENT** - Implement preventative measures (hooks, documentation, configuration)
3. **COMMIT** - Commit the prevention changes with descriptive message
4. **THEN FIX** - Only after prevention is in place, fix the immediate problem

❌ **WRONG**: Immediately jump to fixing the problem
✅ **CORRECT**: Analyze → Prevent → Commit prevention → Then fix

**Anti-Pattern Detection**:
- Using Edit/Write/Bash to fix the original issue BEFORE creating prevention hook
- Skipping root cause analysis
- Treating symptom without addressing systemic cause

**Your first action should be**: Analyze what caused the mistake and what system change would prevent it in the future."

	jq -n \
		--arg event "UserPromptSubmit" \
		--arg context "$MESSAGE" \
		'{
			"hookSpecificOutput": {
				"hookEventName": $event,
				"additionalContext": $context
			}
		}'
fi

exit 0
