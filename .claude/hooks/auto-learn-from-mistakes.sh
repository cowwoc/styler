#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in auto-learn-from-mistakes.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Auto-invoke learn-from-mistakes skill when mistakes are detected
#
# This hook monitors tool results for error patterns and automatically
# triggers the learn-from-mistakes skill for root cause analysis.
#
# Triggered on: PostToolUse (all tools)
# Detects: Build failures, test failures, validation errors, protocol violations,
#          skill step failures, git operation failures, merge conflicts, edit failures
#
# PATTERN EVOLUTION:
#   - 2025-11-05: Added Pattern 6 (skill step failures) and Pattern 7 (git operation failures)

# Read input from stdin (hook context JSON for PostToolUse)
HOOK_CONTEXT=$(cat)

# Extract tool name and result from context
TOOL_NAME=$(echo "$HOOK_CONTEXT" | jq -r '.tool.name // "unknown"')
TOOL_RESULT=$(echo "$HOOK_CONTEXT" | jq -r '.result.output // .result.content // ""')

# Initialize mistake detection
MISTAKE_TYPE=""
MISTAKE_DETAILS=""

# Pattern 1: Build failures (CRITICAL)
if echo "$TOOL_RESULT" | grep -qi "BUILD FAILURE\|COMPILATION ERROR\|compilation failure"; then
  MISTAKE_TYPE="build_failure"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -A5 -i "error\|failure" | head -20)
fi

# Pattern 2: Test failures (CRITICAL)
if echo "$TOOL_RESULT" | grep -qE "Tests run:.*Failures: [1-9]|test.*failed"; then
  MISTAKE_TYPE="test_failure"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -A5 -iE "test.*failed|failure" | head -20)
fi

# Pattern 3: Protocol violations (CRITICAL)
if echo "$TOOL_RESULT" | grep -qE "PROTOCOL VIOLATION|🚨.*VIOLATION"; then
  MISTAKE_TYPE="protocol_violation"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -B2 -A5 -i "violation" | head -20)
fi

# Pattern 4: Merge conflicts (HIGH)
if echo "$TOOL_RESULT" | grep -qi "CONFLICT\|merge conflict"; then
  MISTAKE_TYPE="merge_conflict"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -A3 -i "conflict" | head -10)
fi

# Pattern 5: Edit tool failures (old_string not found)
if echo "$TOOL_RESULT" | grep -qi "String to replace not found\|old_string not found"; then
  MISTAKE_TYPE="edit_failure"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -B5 -A2 -i "string to replace not found\|old_string not found" | head -15)
fi

# Pattern 6: Skill step failures (HIGH)
if [[ "$TOOL_NAME" == "Skill" ]] && echo "$TOOL_RESULT" | grep -qiE "\bERROR\b|\bFAILED\b|failed to|step.*(failed|failure)|operation.*(failed|failure)|could not|unable to"; then
  MISTAKE_TYPE="skill_step_failure"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -B3 -A5 -iE "error|failed|could not|unable to" | head -20)
fi

# Pattern 7: Git operation failures (HIGH)
if echo "$TOOL_RESULT" | grep -qiE "fatal:|error:|git.*failed|rebase.*failed|merge.*failed"; then
  MISTAKE_TYPE="git_operation_failure"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -B2 -A3 -iE "fatal:|error:|failed" | head -15)
fi

# If no mistake detected, pass through
if [[ -z "$MISTAKE_TYPE" ]]; then
  echo "$HOOK_CONTEXT"
  exit 0
fi

# Rate limiting: Check if we've triggered recently
MISTAKE_LOG="/tmp/mistake-detection-log.json"
touch "$MISTAKE_LOG"

# Initialize log if empty
if [[ ! -s "$MISTAKE_LOG" ]]; then
  echo "[]" > "$MISTAKE_LOG"
fi

# Check for recent mistakes (within last 5 minutes)
FIVE_MINUTES_AGO=$(date -d '5 minutes ago' -Iseconds 2>/dev/null || date -v-5M -Iseconds 2>/dev/null || echo "")
if [[ -n "$FIVE_MINUTES_AGO" ]]; then
  RECENT_MISTAKES=$(jq --arg cutoff "$FIVE_MINUTES_AGO" '[.[] | select(.timestamp > $cutoff)]' "$MISTAKE_LOG")
  RECENT_COUNT=$(echo "$RECENT_MISTAKES" | jq 'length')

  if [[ "$RECENT_COUNT" -gt 0 ]]; then
    # Already prompted recently, just log and pass through
    TIMESTAMP=$(date -Iseconds)
    jq --arg type "$MISTAKE_TYPE" \
       --arg tool "$TOOL_NAME" \
       --arg timestamp "$TIMESTAMP" \
       --arg details "$MISTAKE_DETAILS" \
       '. += [{type: $type, tool: $tool, timestamp: $timestamp, details: $details}]' \
       "$MISTAKE_LOG" > "${MISTAKE_LOG}.tmp" && mv "${MISTAKE_LOG}.tmp" "$MISTAKE_LOG"

    echo "$HOOK_CONTEXT"
    exit 0
  fi
fi

# Log this mistake
TIMESTAMP=$(date -Iseconds)
jq --arg type "$MISTAKE_TYPE" \
   --arg tool "$TOOL_NAME" \
   --arg timestamp "$TIMESTAMP" \
   --arg details "$MISTAKE_DETAILS" \
   '. += [{type: $type, tool: $tool, timestamp: $timestamp, details: $details}]' \
   "$MISTAKE_LOG" > "${MISTAKE_LOG}.tmp" && mv "${MISTAKE_LOG}.tmp" "$MISTAKE_LOG"

# Output the original hook context
echo "$HOOK_CONTEXT"

# Output recommendation to invoke learn-from-mistakes
cat >&2 << EOF

📚 MISTAKE DETECTED: $MISTAKE_TYPE

A significant mistake was detected in the $TOOL_NAME tool result.

**Recommendation**: Invoke the learn-from-mistakes skill:

\`\`\`
Skill: learn-from-mistakes

Context: Detected $MISTAKE_TYPE during $TOOL_NAME execution.

Details: $MISTAKE_DETAILS

Please analyze the root cause and recommend prevention measures.
\`\`\`

This analysis will help prevent similar mistakes in future sessions.

EOF

exit 0
