#!/bin/bash
set -euo pipefail

# Throttle Agent Bash Calls Hook
#
# Trigger: PostToolUse on Bash tool (agent executions only)
# Purpose: Prevent agents from making excessive Bash calls
#
# Root Cause (M006): Accountant agent made 71 Bash calls when 15-20 maximum needed
# Performance Impact: Wasted 716 seconds (93% waste) due to excessive overhead
#
# Prevention: Block agents after 30 Bash calls, warn at 20
#
# Adopted from temp/.claude 2025-11-28

# Error handler
trap 'echo "ERROR in throttle-agent-bash-calls.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read hook context from stdin
HOOK_CONTEXT=$(cat)

# Extract tool name
TOOL_NAME=$(echo "$HOOK_CONTEXT" | jq -r '.tool.name // "unknown"')

# Only apply to Bash tool calls
if [[ "$TOOL_NAME" != "Bash" ]]; then
    exit 0
fi

# Check if this is a sidechain (agent) execution
IS_SIDECHAIN=$(echo "$HOOK_CONTEXT" | jq -r '.is_sidechain // false')

# Only apply to agent (sidechain) executions
if [[ "$IS_SIDECHAIN" != "true" ]]; then
    exit 0
fi

# Get agent ID
AGENT_ID=$(echo "$HOOK_CONTEXT" | jq -r '.agent_id // ""')

if [[ -z "$AGENT_ID" ]]; then
    exit 0
fi

# Count Bash calls for this agent execution using temp file
COUNTER_FILE="/tmp/agent-bash-count-${AGENT_ID}.txt"

# Initialize or increment counter
if [[ -f "$COUNTER_FILE" ]]; then
    BASH_COUNT=$(cat "$COUNTER_FILE")
    BASH_COUNT=$((BASH_COUNT + 1))
else
    BASH_COUNT=1
fi

# Save updated count
echo "$BASH_COUNT" > "$COUNTER_FILE"

# Thresholds
WARN_THRESHOLD=20
BLOCK_THRESHOLD=30

if [[ $BASH_COUNT -ge $BLOCK_THRESHOLD ]]; then
    cat >&2 <<EOF

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❌ BLOCKED: Excessive Bash Calls (Agent Performance Policy Violation)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Agent has made $BASH_COUNT Bash calls - EXCEEDS limit of $BLOCK_THRESHOLD

Performance Impact:
- Each Bash call has overhead (process startup, I/O)
- $BASH_COUNT calls indicates inefficient approach
- Expected maximum: 15-20 Bash calls for typical operations

Root Cause (M006, Session dff54f8e, 2025-11-20):
Agent made 71 Bash calls (should be 15-20):
- 33 parsing commands (cat/grep/awk) → Should use 1 script
- 5 save+verify cycles → Should batch operations
- Result: 766s execution (93% waste)

Required Actions:
1. Use bulk read operations (read once, cache results)
2. Parse data with SINGLE script (not 30+ individual commands)
3. Batch all modifications together
4. Verify ONCE at end

Policy Reference: CLAUDE.md "Tool Usage Best Practices"

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

EOF
    exit 1
fi

if [[ $BASH_COUNT -ge $WARN_THRESHOLD ]]; then
    cat >&2 <<EOF

⚠️  WARNING: High Bash Call Count (Performance Concern)

Agent has made $BASH_COUNT Bash calls (approaching limit of $BLOCK_THRESHOLD)

Performance Reminder:
- Use bulk reads (read once, cache results)
- Parse cached data with single script (not 30+ commands)
- Batch all operations, verify once at end
- Target: <20 Bash calls for typical operations

This is a WARNING. Execution continues, but optimize your approach.

EOF
    # Don't block, just warn
    exit 0
fi

# Under threshold - allow
exit 0
