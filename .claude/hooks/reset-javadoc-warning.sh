#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in reset-javadoc-warning.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Session start hook to reset JavaDoc warning marker after context compaction
# Allows detect-generic-javadoc.sh to show warning again in resumed session

# Read session ID from stdin JSON
INPUT=$(cat)
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')

# Require session ID - fail fast if not provided
if [ -z "$SESSION_ID" ]; then
    echo "ERROR: session_id must be provided in hook JSON input" >&2
    exit 1
fi

# Check if marker file exists and remove it
MARKER_FILE="/tmp/claude-javadoc-warning-shown-${SESSION_ID}"
if [ -f "$MARKER_FILE" ]; then
	rm -f "$MARKER_FILE"
	echo "🔄 Reset JavaDoc warning marker for session ${SESSION_ID}"
fi

exit 0
