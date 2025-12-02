#!/bin/bash
# Detect Batch Read Opportunities
#
# ADDED: 2025-11-28 based on temp/.claude batch-read-files pattern database
# PURPOSE: Track sequential Read operations and suggest batch-read skill when patterns detected
#
# This hook runs on PostToolUse for Read|Glob|Grep tools and tracks patterns
# to suggest more efficient batch operations.

set -euo pipefail

# Read stdin for hook context
INPUT=$(cat)

# Extract tool info
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name // empty')
FILE_PATH=$(echo "$INPUT" | jq -r '.tool.input.file_path // .tool.input.path // .tool.input.pattern // empty')
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // empty')
TIMESTAMP=$(date +%s)

# Only track Read, Glob, Grep tools
if [[ "$TOOL_NAME" != "Read" && "$TOOL_NAME" != "Glob" && "$TOOL_NAME" != "Grep" ]]; then
    exit 0
fi

# Pattern database location
PATTERN_DB="/workspace/main/.claude/pattern-database.json"
SESSION_TRACKER="/tmp/read_pattern_tracker_${SESSION_ID}"

# Create session tracker if doesn't exist
if [[ ! -f "$SESSION_TRACKER" ]]; then
    echo '{"reads": [], "last_read_time": 0}' > "$SESSION_TRACKER"
fi

# Get last read time
LAST_READ_TIME=$(jq -r '.last_read_time // 0' "$SESSION_TRACKER")
TIME_SINCE_LAST=$((TIMESTAMP - LAST_READ_TIME))

# If more than 60 seconds since last read, reset the sequence
if [[ "$TIME_SINCE_LAST" -gt 60 ]]; then
    echo '{"reads": [], "last_read_time": 0}' > "$SESSION_TRACKER"
fi

# Add current read to sequence
jq --arg path "$FILE_PATH" \
   --arg tool "$TOOL_NAME" \
   --argjson time "$TIMESTAMP" \
   '.reads += [{"path": $path, "tool": $tool, "time": $time}] | .last_read_time = $time' \
   "$SESSION_TRACKER" > "${SESSION_TRACKER}.tmp" && mv "${SESSION_TRACKER}.tmp" "$SESSION_TRACKER"

# Get current sequence length
SEQ_LENGTH=$(jq '.reads | length' "$SESSION_TRACKER")

# Only suggest if 3+ sequential reads
if [[ "$SEQ_LENGTH" -lt 3 ]]; then
    exit 0
fi

# Extract pattern signature (file extensions/directories)
PATTERN_SIG=$(jq -r '.reads | map(.path | split("/") | last | split(".") | last) | unique | sort | join(",")' "$SESSION_TRACKER")
FILE_PATHS=$(jq -r '.reads | map(.path) | join("\n")' "$SESSION_TRACKER")

# Check if we've already suggested for this pattern in this session
SUGGESTED_PATTERNS="/tmp/suggested_patterns_${SESSION_ID}"
if [[ -f "$SUGGESTED_PATTERNS" ]] && grep -q "$PATTERN_SIG" "$SUGGESTED_PATTERNS" 2>/dev/null; then
    exit 0
fi

# Calculate potential time savings (estimated 5s per avoided round-trip)
TIME_SAVED=$((SEQ_LENGTH * 5))

# Output suggestion
cat >&2 << EOF

💡 BATCH OPPORTUNITY DETECTED
═══════════════════════════════════════════════════════════════════════════════

Pattern: $SEQ_LENGTH sequential Read operations detected
Files:
$FILE_PATHS

SUGGESTION: Use batch-read skill for efficiency

Example:
    /workspace/main/.claude/scripts/batch-read.sh "pattern" --max-files $SEQ_LENGTH

Or read specific files:
    /workspace/main/.claude/scripts/batch-read.sh --files "$FILE_PATHS"

Estimated savings: ~${TIME_SAVED}s time, ~$((SEQ_LENGTH * 1500)) tokens

───────────────────────────────────────────────────────────────────────────────
Pattern signature: $PATTERN_SIG
Confidence: 0.95 (SAFE - read-only operation)
═══════════════════════════════════════════════════════════════════════════════

EOF

# Mark pattern as suggested
echo "$PATTERN_SIG" >> "$SUGGESTED_PATTERNS"

# Update pattern database
if [[ -f "$PATTERN_DB" ]]; then
    # Add pattern to database
    PATTERN_ENTRY=$(cat << PEOF
{
  "signature": "$PATTERN_SIG",
  "sequence_length": $SEQ_LENGTH,
  "last_seen": "$(date -Iseconds)",
  "occurrences": 1,
  "estimated_savings_seconds": $TIME_SAVED,
  "file_types": $(echo "$PATTERN_SIG" | jq -R 'split(",")'),
  "confidence": 0.95
}
PEOF
)

    # Check if pattern already exists and update, otherwise add
    EXISTING=$(jq --arg sig "$PATTERN_SIG" '.patterns | map(select(.signature == $sig)) | length' "$PATTERN_DB")

    if [[ "$EXISTING" -gt 0 ]]; then
        # Update existing pattern
        jq --arg sig "$PATTERN_SIG" \
           --argjson savings "$TIME_SAVED" \
           '(.patterns[] | select(.signature == $sig)) |= (
             .occurrences += 1 |
             .last_seen = now | todate |
             .estimated_savings_seconds += $savings
           ) |
           .stats.total_time_saved_seconds += $savings |
           .stats.last_updated = now | todate' \
           "$PATTERN_DB" > "${PATTERN_DB}.tmp" 2>/dev/null && \
           mv "${PATTERN_DB}.tmp" "$PATTERN_DB" || true
    else
        # Add new pattern
        jq --argjson entry "$PATTERN_ENTRY" \
           '.patterns += [$entry] |
            .stats.total_patterns_detected += 1 |
            .stats.total_batch_suggestions_made += 1 |
            .stats.total_time_saved_seconds += ($entry.estimated_savings_seconds) |
            .stats.last_updated = now | todate' \
           "$PATTERN_DB" > "${PATTERN_DB}.tmp" 2>/dev/null && \
           mv "${PATTERN_DB}.tmp" "$PATTERN_DB" || true
    fi
fi

# Reset sequence after suggestion
echo '{"reads": [], "last_read_time": 0}' > "$SESSION_TRACKER"

exit 0
