#!/bin/bash
# detect-unanswered-questions.sh
# Detects when user asks a question but assistant doesn't answer it
# Prevents the pattern of ignoring questions in system reminders

# Read assistant's last response from stdin
RESPONSE=$(cat)

# Check if there's a system-reminder with user message in the PREVIOUS turn
# This is injected by the system when user sends a message
HAS_USER_QUESTION=false
USER_MESSAGE=""

# Look for system-reminder pattern indicating user sent a message
if echo "$RESPONSE" | grep -q "<system-reminder>.*The user sent the following message:" 2>/dev/null; then
  HAS_USER_QUESTION=true
  # Extract the user's message (this is a heuristic - may need refinement)
  USER_MESSAGE=$(echo "$RESPONSE" | sed -n '/<system-reminder>.*The user sent the following message:/,/Please address this message/p')
fi

# If user question detected, check if assistant addressed it
if [ "$HAS_USER_QUESTION" = "true" ]; then
  # Check if response contains answer patterns
  ADDRESSED=false

  # Look for acknowledgment patterns
  if echo "$RESPONSE" | grep -iE "(no, I cannot|yes, I can|I (can|cannot)|the answer is|to answer your question)" > /dev/null 2>&1; then
    ADDRESSED=true
  fi

  # If not addressed, inject reminder
  if [ "$ADDRESSED" = "false" ]; then
    cat <<'UNANSWERED_QUESTION'

## 🚨 CRITICAL: Unanswered User Question Detected

The system detected that the user sent a message in the previous turn, but your response does not appear to address it.

**User's message was:**
(Check the system-reminder above for the exact question)

**MANDATORY REQUIREMENT:**
You MUST directly answer user questions before continuing with other work.

**CORRECT PATTERN:**
1. Read ALL system reminders for user messages
2. Answer the user's question FIRST
3. THEN continue with current task

**PROHIBITED PATTERN:**
❌ Ignoring questions in system reminders
❌ Continuing with work without addressing user input
❌ Assuming questions will be repeated

**ACTION REQUIRED:**
Stop what you're doing and answer the user's question from the previous turn.

UNANSWERED_QUESTION
    echo "$RESPONSE"
    exit 0
  fi
fi

# No problems detected - don't output anything
