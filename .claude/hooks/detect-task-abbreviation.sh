#!/bin/bash
# PostToolUse hook: Detect abbreviation in Task tool results
#
# ADDED: 2025-11-18 after general-purpose agent abbreviated classification output
# despite explicit "DO NOT abbreviate or summarize" instruction during /shrink-doc
# execution (session d8135b08-5dca-4620-9afc-ac8ccf9b52b9).
#
# PREVENTS: Agents ignoring "DO NOT abbreviate" instructions by detecting
# abbreviation patterns in Task tool results and alerting the main agent.
#
# TRIGGERING THOUGHT: "Due to the length and complexity of this document (2633 lines),
# I'll provide a summary of the remaining high-confidence candidates rather than
# exhaustive detail for all 86:"

set -euo pipefail

trap 'echo "ERROR in detect-task-abbreviation.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read JSON input from stdin
JSON_INPUT=$(cat)

# Extract tool name
TOOL_NAME=$(echo "$JSON_INPUT" | python3 -c "import sys, json; data = json.load(sys.stdin); print(data.get('tool', {}).get('name', ''))" 2>/dev/null || echo "")

# Only check Task tool results
if [[ "$TOOL_NAME" != "Task" ]]; then
  exit 0
fi

# Extract tool input (the prompt given to the agent)
TOOL_PROMPT=$(echo "$JSON_INPUT" | python3 -c "
import sys, json
data = json.load(sys.stdin)
tool_input = data.get('tool', {}).get('input', {})
print(tool_input.get('prompt', ''))
" 2>/dev/null || echo "")

# Extract tool result (the agent's response)
TOOL_RESULT=$(echo "$JSON_INPUT" | python3 -c "
import sys, json
data = json.load(sys.stdin)
result = data.get('result', '')
if isinstance(result, list):
  # Handle array of content blocks
  for item in result:
    if isinstance(item, dict) and item.get('type') == 'text':
      print(item.get('text', ''))
elif isinstance(result, dict):
  print(result.get('content', ''))
else:
  print(result)
" 2>/dev/null || echo "")

# Check if prompt contains "DO NOT abbreviate" instruction
if ! echo "$TOOL_PROMPT" | grep -qi "DO NOT abbreviate\|DO NOT.*summarize\|COMPLETE.*for ALL"; then
  # No explicit instruction against abbreviation - allow
  exit 0
fi

# Prompt prohibits abbreviation - check if result contains abbreviation patterns
ABBREVIATION_DETECTED=false
DETECTED_PATTERN=""

# Pattern 1: Explicit statements of abbreviation
if echo "$TOOL_RESULT" | grep -qi "I'll provide a summary\|provide a summary\|summarizing the remaining\|summary of the remaining"; then
  ABBREVIATION_DETECTED=true
  DETECTED_PATTERN="Explicit abbreviation statement"
fi

# Pattern 2: "Due to length/complexity" justifications
if echo "$TOOL_RESULT" | grep -qi "Due to.*length\|Due to.*complexity\|Due to.*size\|Due to.*token"; then
  ABBREVIATION_DETECTED=true
  DETECTED_PATTERN="Length/complexity justification"
fi

# Pattern 3: References to "remaining candidates/items" with abbreviation
if echo "$TOOL_RESULT" | grep -qi "remaining.*rather than.*detail\|remaining.*instead of.*complete\|exhaustive detail for all"; then
  ABBREVIATION_DETECTED=true
  DETECTED_PATTERN="Partial output with reference to remaining items"
fi

# Pattern 4: Explicit "abbreviated" or "summarized" statements
if echo "$TOOL_RESULT" | grep -qi "\babbreviated\b\|\bsummarized\b\|for brevity\|to save space"; then
  ABBREVIATION_DETECTED=true
  DETECTED_PATTERN="Explicit mention of abbreviation/summary"
fi

if [[ "$ABBREVIATION_DETECTED" = true ]]; then
  echo "⚠️ WARNING: Task tool result appears to violate 'DO NOT abbreviate' instruction" >&2
  echo "" >&2
  echo "Pattern detected: $DETECTED_PATTERN" >&2
  echo "" >&2
  echo "The agent prompt contained an explicit instruction against abbreviation," >&2
  echo "but the agent's response contains patterns suggesting output was abbreviated." >&2
  echo "" >&2
  echo "Common abbreviation patterns detected:" >&2
  echo "  - 'I'll provide a summary...'" >&2
  echo "  - 'Due to length/complexity...'" >&2
  echo "  - 'remaining candidates rather than exhaustive detail...'" >&2
  echo "" >&2
  echo "⚠️ RECOMMENDED ACTION:" >&2
  echo "  1. Review the agent's output for completeness" >&2
  echo "  2. If incomplete, re-invoke with stronger emphasis:" >&2
  echo "     - Add ⚠️ CRITICAL markers to the instruction" >&2
  echo "     - Explain WHY complete output is required" >&2
  echo "     - Explicitly prohibit using length/complexity as justification" >&2
  echo "  3. Consider breaking the task into smaller chunks if truly needed" >&2
  echo "" >&2

  # NOTE: We output to stderr but don't block (exit 0) because the agent may have
  # valid reasons and the main agent needs to make the final decision.
  # This hook serves as an alert mechanism, not hard enforcement.
fi

exit 0
