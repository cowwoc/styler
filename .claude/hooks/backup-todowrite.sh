#!/bin/bash
# TodoWrite Backup Hook
#
# ADDED: 2025-12-01
# PURPOSE: Backup TodoWrite state before context compaction to preserve
#          task tracking across sessions.
#
# Trigger: PreCompact
# Output: Saves state to .claude/backups/todowrite/

set -euo pipefail
trap 'echo "ERROR in backup-todowrite.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read stdin (compact context)
INPUT=$(cat)

SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty' 2>/dev/null || echo "unknown")

# Create backup directory
BACKUP_DIR="/workspace/main/.claude/backups/todowrite"
mkdir -p "$BACKUP_DIR"

# Check for existing TodoWrite state in temp
TODOWRITE_STATE="/tmp/todowrite_state_${SESSION_ID}.json"

if [[ -f "$TODOWRITE_STATE" ]]; then
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    BACKUP_FILE="${BACKUP_DIR}/todowrite_${SESSION_ID}_${TIMESTAMP}.json"

    cp "$TODOWRITE_STATE" "$BACKUP_FILE"

    # Keep only last 10 backups per session
    ls -t "${BACKUP_DIR}/todowrite_${SESSION_ID}_"*.json 2>/dev/null | tail -n +11 | xargs -r rm -f

    echo "TodoWrite state backed up to: $BACKUP_FILE" >&2
fi

# Also try to extract TodoWrite from the compact context if available
TODOS=$(echo "$INPUT" | jq -r '.todos // empty' 2>/dev/null || echo "")

if [[ -n "$TODOS" && "$TODOS" != "null" ]]; then
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    BACKUP_FILE="${BACKUP_DIR}/todowrite_context_${SESSION_ID}_${TIMESTAMP}.json"

    echo "$TODOS" > "$BACKUP_FILE"
    echo "TodoWrite context backed up to: $BACKUP_FILE" >&2
fi

exit 0
