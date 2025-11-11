#!/bin/bash
set -euo pipefail

# write-and-commit.sh - Atomic file creation with git commit
#
# This script creates a file and commits it in a single atomic operation,
# reducing LLM round-trips from 4-5 to 2-3 (60-75% faster).
#
# Usage:
#   write-and-commit.sh <file_path> <content_file> <commit_message_file> [--executable]
#
# Parameters:
#   file_path            - Path to file to create (absolute or relative)
#   content_file         - File containing content to write
#   commit_message_file  - File containing commit message
#   --executable         - Optional flag to make file executable (chmod +x)

# ============================================================================
# CONFIGURATION
# ============================================================================

FILE_PATH="${1:?ERROR: FILE_PATH required (path to file to create)}"
CONTENT_FILE="${2:?ERROR: CONTENT_FILE required (file containing content)}"
COMMIT_MSG_FILE="${3:?ERROR: COMMIT_MESSAGE_FILE required (file containing commit message)}"
EXECUTABLE_FLAG="${4:-}"

# Verify content file exists
if [[ ! -f "$CONTENT_FILE" ]]; then
  echo "ERROR: Content file not found: $CONTENT_FILE" >&2
  exit 1
fi

# Verify commit message file exists
if [[ ! -f "$COMMIT_MSG_FILE" ]]; then
  echo "ERROR: Commit message file not found: $COMMIT_MSG_FILE" >&2
  exit 1
fi

# ============================================================================
# EXECUTION RESULT TRACKING
# ============================================================================

RESULT_JSON="/tmp/write-and-commit-result-$$.json"
START_TIME=$(date +%s)

# Function to output structured JSON result
output_result() {
  local status="$1"
  local message="$2"
  local end_time=$(date +%s)
  local duration=$((end_time - START_TIME))

  cat > "$RESULT_JSON" <<EOF
{
  "status": "$status",
  "message": "$message",
  "duration_seconds": $duration,
  "file_path": "$FILE_PATH",
  "executable": $([ "$EXECUTABLE_FLAG" = "--executable" ] && echo "true" || echo "false"),
  "commit_sha": "${COMMIT_SHA:-none}",
  "working_directory": "$(pwd)",
  "timestamp": "$(date -Iseconds)"
}
EOF

  cat "$RESULT_JSON"
  [[ "$status" == "success" ]] && exit 0 || exit 1
}

# ============================================================================
# STEP 1: VALIDATE PRECONDITIONS
# ============================================================================

echo "Step 1: Validating preconditions..."

# Check if file already exists
if [[ -f "$FILE_PATH" ]]; then
  echo "⚠️  Warning: File already exists: $FILE_PATH"
  echo "   This will overwrite the existing file."
fi

# Check if we're in a git repository
if ! git rev-parse --git-dir >/dev/null 2>&1; then
  output_result "error" "Not in a git repository"
fi

echo "✅ Preconditions validated"

# ============================================================================
# STEP 2: CREATE FILE
# ============================================================================

echo ""
echo "Step 2: Creating file..."

# Create parent directory if it doesn't exist
FILE_DIR=$(dirname "$FILE_PATH")
if [[ ! -d "$FILE_DIR" ]]; then
  echo "Creating directory: $FILE_DIR"
  if ! mkdir -p "$FILE_DIR"; then
    output_result "error" "Failed to create directory: $FILE_DIR"
  fi
fi

# Write content to file
if ! cp "$CONTENT_FILE" "$FILE_PATH"; then
  output_result "error" "Failed to write file: $FILE_PATH"
fi

echo "✅ File created: $FILE_PATH"

# Show file size
FILE_SIZE=$(wc -c < "$FILE_PATH")
echo "   Size: $FILE_SIZE bytes"

# ============================================================================
# STEP 3: SET PERMISSIONS (OPTIONAL)
# ============================================================================

if [[ "$EXECUTABLE_FLAG" == "--executable" ]]; then
  echo ""
  echo "Step 3: Setting executable permissions..."

  if ! chmod +x "$FILE_PATH"; then
    output_result "error" "Failed to set executable permissions on: $FILE_PATH"
  fi

  echo "✅ File is now executable"
else
  echo ""
  echo "Step 3: Skipping executable permissions (not requested)"
fi

# ============================================================================
# STEP 4: GIT ADD
# ============================================================================

echo ""
echo "Step 4: Staging file with git..."

if ! git add "$FILE_PATH"; then
  output_result "error" "Failed to stage file with git add: $FILE_PATH"
fi

echo "✅ File staged for commit"

# ============================================================================
# STEP 5: GIT COMMIT
# ============================================================================

echo ""
echo "Step 5: Committing file..."

if ! git commit -F "$COMMIT_MSG_FILE"; then
  # Check if it failed because nothing to commit
  if git diff --cached --quiet; then
    output_result "error" "Nothing to commit - file unchanged"
  else
    output_result "error" "Failed to commit file"
  fi
fi

COMMIT_SHA=$(git rev-parse HEAD)
echo "✅ File committed: $COMMIT_SHA"

# Show the commit
echo ""
git log --oneline -1

# ============================================================================
# SUCCESS
# ============================================================================

echo ""
echo "════════════════════════════════════════"
echo "File Creation Summary:"
echo "  File:       $FILE_PATH"
echo "  Size:       $FILE_SIZE bytes"
echo "  Executable: $([ "$EXECUTABLE_FLAG" = "--executable" ] && echo "Yes" || echo "No")"
echo "  Commit:     $COMMIT_SHA"
echo "════════════════════════════════════════"

output_result "success" "File created and committed successfully"
