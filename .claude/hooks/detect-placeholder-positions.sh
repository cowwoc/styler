#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in detect-placeholder-positions.sh line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Hook: Detect placeholder positions in parser tests
# Blocks edits that contain (0, 0) position placeholders

# Only check Edit tool on parser test files
if [[ "${TOOL_NAME:-}" != "Edit" ]]; then
  exit 0
fi

FILE_PATH="${TOOL_PARAM_file_path:-}"
if [[ ! "$FILE_PATH" =~ ParserTest\.java$ ]] && [[ ! "$FILE_PATH" =~ parser/src/test/.*/test/.*Test\.java$ ]]; then
  exit 0
fi

NEW_STRING="${TOOL_PARAM_new_string:-}"

# Check for placeholder position patterns
if echo "$NEW_STRING" | grep -qE '\(0,\s*0\)'; then
  echo "BLOCKED: Detected placeholder positions (0, 0) in parser test."
  echo ""
  echo "Parser test expected values MUST be manually calculated, not:"
  echo "  1. Written with placeholders"
  echo "  2. Derived from running the test and copying actual output"
  echo ""
  echo "Calculate positions by counting bytes in the source string."
  echo "See: .claude/rules/java-style.md § parser test position values"
  exit 1
fi

exit 0
