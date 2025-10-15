#!/bin/bash
set -euo pipefail

# Session start hook to reset JavaDoc warning marker after context compaction
# Allows detect-generic-javadoc.sh to show warning again in resumed session

# Require session ID - fail fast if not provided
if [ -z "${CLAUDE_SESSION_ID:-}" ]; then
    echo "ERROR: CLAUDE_SESSION_ID environment variable is required" >&2
    exit 1
fi

# Check if marker file exists and remove it
if [ -n "$CLAUDE_SESSION_ID" ]; then
	MARKER_FILE="/tmp/claude-javadoc-warning-shown-${CLAUDE_SESSION_ID}"
	if [ -f "$MARKER_FILE" ]; then
		rm -f "$MARKER_FILE"
		echo "🔄 Reset JavaDoc warning marker for session ${CLAUDE_SESSION_ID}"
	fi
fi

exit 0
