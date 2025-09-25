#!/bin/bash
set -euo pipefail
trap 'echo "at line $LINENO, exit code $? from $BASH_COMMAND" >&2; exit 1' ERR

# Check if this hook should run for the current agent
SCRIPT_DIR="$(dirname "$0")"
if ! "$SCRIPT_DIR/agent-context-filter.sh" "critical-thinking"; then
	exit 0  # Skip hook for this agent type
fi

# Critical Thinking Hook
# This hook ensures Claude applies critical thinking to all user prompts
# by challenging assumptions and providing substantive technical analysis

# The hook always triggers to enforce critical thinking requirements
# No need to check transcript history - we want this behavior consistently

cat <<'EOF'
<system-reminder>
## 🧠 CRITICAL THINKING REQUIREMENTS

**MANDATORY**: Apply evidence-based critical thinking
- FIRST gather evidence through investigation, testing, or research
- THEN identify flaws, edge cases, or counter-examples based on that evidence
- Propose alternative approaches only when evidence shows they're superior
- Avoid challenging assumptions without supporting evidence
- If user approach is sound, state specific technical reasons for agreement

**EXAMPLES OF CRITICAL ANALYSIS:**

Instead of: "That's a good approach"
Use: "That approach addresses the immediate issue. Based on testing X, I can confirm it works. However, there's a potential edge case Y that we should consider because Z."

Instead of: "You're absolutely right"
Use: "The core logic is sound. My investigation shows X evidence supporting this. However, we should also consider scenario Y where this might need adjustment."

**APPLY TO CURRENT PROMPT**: Gather evidence first, then provide critical analysis based on that evidence.
</system-reminder>
EOF

exit 0