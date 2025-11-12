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
#          skill step failures, git operation failures, merge conflicts, edit failures,
#          missing cleanup steps, self-acknowledged mistakes, restore from backup,
#          critical self-acknowledgments ("CRITICAL DISASTER", "catastrophic", etc.)
#
# PATTERN EVOLUTION:
#   - 2025-11-05: Added Pattern 6 (skill step failures) and Pattern 7 (git operation failures)
#   - 2025-11-06: Added Pattern 8 (missing cleanup), Pattern 9 (self-acknowledged mistakes),
#                 and Pattern 10 (restore from backup)
#   - 2025-11-07: Added Pattern 11 (critical self-acknowledgments) for detecting phrases
#                 like "CRITICAL DISASTER", "CRITICAL MISTAKE", "CRITICAL ERROR"
#   - 2025-11-12: Enhanced Pattern 11 to check assistant messages via conversation log
#                 (inspired by detect-assistant-giving-up.sh approach)
#                 Improved to track last checked line number, only parsing new messages
#                 (eliminates missed messages and removes need for rate limiting)

# Read input from stdin (hook context JSON for PostToolUse)
HOOK_CONTEXT=$(cat)

# Extract tool name and result from context
TOOL_NAME=$(echo "$HOOK_CONTEXT" | jq -r '.tool.name // "unknown"')
TOOL_RESULT=$(echo "$HOOK_CONTEXT" | jq -r '.result.output // .result.content // ""')

# Extract session ID for conversation log access
SESSION_ID=$(echo "$HOOK_CONTEXT" | jq -r '.session_id // ""')

# Initialize assistant message content for Pattern 11 enhancement
LAST_ASSISTANT_MESSAGE=""

# Track last checked line number to only parse new messages
# This prevents missing messages and is more efficient than rate limiting
if [[ -n "$SESSION_ID" ]]; then
  CONVERSATION_LOG="/home/node/.config/projects/-workspace/${SESSION_ID}.jsonl"
  LAST_LINE_FILE="/tmp/auto-learn-mistakes-lastline-${SESSION_ID}.txt"

  if [[ -f "$CONVERSATION_LOG" ]]; then
    # Get current line count
    CURRENT_LINE_COUNT=$(wc -l < "$CONVERSATION_LOG")

    # Get last checked line number (default to 0 if file doesn't exist)
    LAST_LINE_CHECKED=0
    if [[ -f "$LAST_LINE_FILE" ]]; then
      LAST_LINE_CHECKED=$(cat "$LAST_LINE_FILE" 2>/dev/null || echo "0")
    fi

    # Only check if there are new lines
    if [[ $CURRENT_LINE_COUNT -gt $LAST_LINE_CHECKED ]]; then
      # Extract only new assistant messages since last check
      LINES_TO_CHECK=$((CURRENT_LINE_COUNT - LAST_LINE_CHECKED))
      LAST_ASSISTANT_MESSAGE=$(tail -n "$LINES_TO_CHECK" "$CONVERSATION_LOG" | grep -F '"role":"assistant"' || echo "")

      # Update last checked line number
      echo "$CURRENT_LINE_COUNT" > "$LAST_LINE_FILE"
    fi
  fi
fi

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

# Pattern 8: Missing cleanup after successful operations (MEDIUM)
# Detects when user asks "Why didn't you [cleanup action]?"
if echo "$TOOL_RESULT" | grep -qiE "why didn't you (remove|delete|clean|cleanup)|didn't you (remove|delete|clean)"; then
  MISTAKE_TYPE="missing_cleanup"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -B5 -A2 -iE "why didn't you|didn't you" | head -15)
fi

# Pattern 9: Self-acknowledged mistakes (HIGH)
# Detects when assistant acknowledges "You're right - I should have..."
if echo "$TOOL_RESULT" | grep -qiE "(you're|you are) (right|correct|absolutely right).*(should have|should've|ought to have)|I should have.*instead"; then
  MISTAKE_TYPE="self_acknowledged_mistake"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -B2 -A5 -iE "(you're|you are) right|should have|ought to" | head -20)
fi

# Pattern 10: Restore from backup (HIGH)
# Detects when assistant says "let me restore from backup" indicating failed operation
if echo "$TOOL_RESULT" | grep -qiE "(let me|I'll|I will|going to) (restore|reset).*(from|using|to).{0,10}backup|restoring from.{0,10}backup"; then
  MISTAKE_TYPE="restore_from_backup"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -B5 -A3 -iE "restore|reset|backup" | head -20)
fi

# Pattern 11: Critical self-acknowledgments (CRITICAL)
# Detects when assistant uses phrases like "CRITICAL DISASTER", "CRITICAL MISTAKE", "CRITICAL ERROR"
# indicating awareness of a major problem
#
# ENHANCED 2025-11-12: Now also checks assistant text messages via conversation log
# Searches both tool results AND recent assistant messages for error acknowledgments
if echo "$TOOL_RESULT" | grep -qiE "CRITICAL (DISASTER|MISTAKE|ERROR|FAILURE|BUG|PROBLEM|ISSUE)|catastrophic|devastating"; then
  MISTAKE_TYPE="critical_self_acknowledgment"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | grep -B3 -A5 -iE "CRITICAL|catastrophic|devastating" | head -20)
fi

# Also check last assistant message from conversation log (if rate limit allows)
if [[ -z "$MISTAKE_TYPE" ]] && [[ -n "$LAST_ASSISTANT_MESSAGE" ]]; then
  if echo "$LAST_ASSISTANT_MESSAGE" | grep -qiE "I made a critical (error|mistake)|CRITICAL (DISASTER|MISTAKE|ERROR|FAILURE)|catastrophic failure|devastating (error|failure|mistake)"; then
    MISTAKE_TYPE="critical_self_acknowledgment"
    MISTAKE_DETAILS=$(echo "$LAST_ASSISTANT_MESSAGE" | grep -iE "I made a critical|CRITICAL|catastrophic|devastating" | head -5)
  fi
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
