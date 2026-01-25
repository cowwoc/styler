#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in require-collect-results.sh line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Hook: Require collect-results before approval gate
# Trigger: PreToolUse on AskUserQuestion
# Purpose: Ensure token metrics are collected before presenting approval gate (M142)

INPUT=$(cat)

# Check if this looks like an approval gate question
if ! echo "$INPUT" | jq -e '.tool_input.questions[]?.question | test("approve|approval|ready.*merge|checkpoint"; "i")' >/dev/null 2>&1; then
  exit 0  # Not an approval gate, allow
fi

# Get session file path
SESSION_ID="${CLAUDE_SESSION_ID:-}"
if [[ -z "$SESSION_ID" ]]; then
  exit 0  # Can't check without session ID
fi

SESSION_FILE="/home/node/.config/claude/projects/-workspace/${SESSION_ID}.jsonl"
if [[ ! -f "$SESSION_FILE" ]]; then
  exit 0  # Session file not found, can't verify
fi

# Check if collect-results was invoked in this session
if jq -e 'select(.type == "assistant") | .message.content[]? | select(.type == "tool_use") |
  select(.name == "Skill") | .input.skill | test("collect-results"; "i")' "$SESSION_FILE" >/dev/null 2>&1; then
  exit 0  # collect-results was invoked, allow
fi

# Check if approval gate mentions "NOT MEASURED" - this indicates missing collection
if echo "$INPUT" | jq -e '.tool_input.questions[]?.options[]?.description | test("NOT MEASURED"; "i")' >/dev/null 2>&1; then
  echo "BLOCKED: Approval gate with 'NOT MEASURED' token usage detected."
  echo ""
  echo "Before presenting an approval gate, invoke /cat:collect-results to get"
  echo "authoritative token metrics from the subagent session."
  echo ""
  echo "See: M142, M146 in mistakes.json"
  exit 1
fi

exit 0
