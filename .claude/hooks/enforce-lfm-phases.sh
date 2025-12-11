#!/bin/bash
# Learn-from-Mistakes Phase Enforcement Hook
#
# ADDED: 2025-12-01
# PURPOSE: Enforce mandatory 3-phase workflow for mistake handling:
#          INVESTIGATE → PREVENT → FIX
#
# Key insight: Fixes without prevention lead to recurring mistakes.
# This hook blocks fixes until investigation and prevention are complete.
#
# Trigger: PreToolUse on Write|Edit
# State: /tmp/lfm_phase_${SESSION_ID}

set -euo pipefail
trap 'echo "ERROR in enforce-lfm-phases.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper for proper hook blocking
source /workspace/.claude/scripts/json-output.sh

# Read stdin
INPUT=$(cat)

# Extract tool and session info
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // .tool.name // empty' 2>/dev/null || echo "")
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty' 2>/dev/null || echo "")
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool.input.file_path // empty' 2>/dev/null || echo "")

# Only check Write and Edit tools
if [[ "$TOOL_NAME" != "Write" && "$TOOL_NAME" != "Edit" ]]; then
    exit 0
fi

# Check if learn-from-mistakes workflow is active
LFM_STATE_FILE="/tmp/lfm_state_${SESSION_ID}.json"

if [[ ! -f "$LFM_STATE_FILE" ]]; then
    # No active LFM workflow - allow
    exit 0
fi

# Read current phase
PHASE=$(jq -r '.phase // "NONE"' "$LFM_STATE_FILE" 2>/dev/null || echo "NONE")
MISTAKE_ID=$(jq -r '.mistake_id // "unknown"' "$LFM_STATE_FILE" 2>/dev/null || echo "unknown")
TARGET_FILE=$(jq -r '.target_file // ""' "$LFM_STATE_FILE" 2>/dev/null || echo "")

# If no active phase, allow
if [[ "$PHASE" == "NONE" || "$PHASE" == "COMPLETE" ]]; then
    exit 0
fi

# Check if this edit is to the target file that needs fixing
# (Allow edits to hooks, configs, docs - only block source file fixes)
if [[ -n "$TARGET_FILE" && "$FILE_PATH" == *"$TARGET_FILE"* ]]; then
    # This is an edit to the file that needs fixing

    if [[ "$PHASE" == "INVESTIGATE" ]]; then
        cat >&2 << EOF

❌ LEARN-FROM-MISTAKES PHASE VIOLATION ❌
═══════════════════════════════════════════════════════════════════════════════

You're trying to fix the source file before completing investigation.

Current Phase: INVESTIGATE
Mistake ID: $MISTAKE_ID
Target File: $TARGET_FILE

MANDATORY WORKFLOW:
  1. INVESTIGATE → Gather evidence, identify root cause, find triggering thought
  2. PREVENT     → Implement prevention mechanism (hook, validation, code fix)
  3. FIX         → Only then fix the immediate issue

To proceed:
  1. Complete investigation (read conversation logs, identify root cause)
  2. Update phase: echo '{"phase":"PREVENT","mistake_id":"$MISTAKE_ID","target_file":"$TARGET_FILE"}' > $LFM_STATE_FILE
  3. Implement prevention mechanism
  4. Update phase to FIX
  5. Then fix the source file

═══════════════════════════════════════════════════════════════════════════════
EOF
        # Use proper permission system
        output_hook_block "Blocked: LFM phase violation - must complete INVESTIGATE phase before fixing. Read conversation logs first." ""
        exit 0
    fi

    if [[ "$PHASE" == "PREVENT" ]]; then
        cat >&2 << EOF

❌ LEARN-FROM-MISTAKES PHASE VIOLATION ❌
═══════════════════════════════════════════════════════════════════════════════

You're trying to fix the source file before implementing prevention.

Current Phase: PREVENT
Mistake ID: $MISTAKE_ID
Target File: $TARGET_FILE

Prevention must be implemented BEFORE fixing. This ensures the same mistake
cannot recur.

PREVENTION HIERARCHY (choose highest applicable):
  1. code_fix   - Fix broken tool/code (HIGHEST priority)
  2. hook       - Automatic enforcement before/after execution
  3. validation - Automatic detection after execution
  4. config     - Documentation (LAST RESORT - must justify why above won't work)

To proceed:
  1. Implement prevention mechanism
  2. Update phase: echo '{"phase":"FIX","mistake_id":"$MISTAKE_ID","target_file":"$TARGET_FILE"}' > $LFM_STATE_FILE
  3. Then fix the source file

═══════════════════════════════════════════════════════════════════════════════
EOF
        # Use proper permission system
        output_hook_block "Blocked: LFM phase violation - must implement PREVENT phase before fixing. Create prevention mechanism first." ""
        exit 0
    fi
fi

# Allow edits to prevention files (hooks, configs, docs) in any phase
# Allow edits to target file only in FIX phase
exit 0
