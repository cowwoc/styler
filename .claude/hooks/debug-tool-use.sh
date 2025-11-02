#!/bin/bash
# Debug hook to log actual JSON structure from Claude Code
set -euo pipefail

INPUT=$(cat)
{
  echo "===== PreToolUse Hook Triggered: $(date -Iseconds) ====="
  echo "Tool matcher hit: $0"
  echo "JSON received:"
  echo "$INPUT" | jq '.' 2>&1 || echo "$INPUT"
  echo "=========================================="
  echo ""
} >> /tmp/hook-debug.log

exit 0
