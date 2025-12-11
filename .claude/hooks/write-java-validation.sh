#!/bin/bash
# Post-Write hook: Validate Java files for common mistakes
#
# ADDED: 2025-11-04 after engineer agent used wrong requireThat() import and
# parameter order during implement-formatter-api task, causing 20+ compilation errors
# PREVENTS: Wrong requireThat() imports and parameter order violations
#
# UPDATED: 2025-12-11 - Added .trim() detection after agents used trim() instead of
# strip() in implement-indentation-formatting task despite style guide rule existing
# PREVENTS: Using .trim() instead of .strip() (Unicode whitespace handling)

set -euo pipefail
trap 'echo "ERROR in write-java-validation.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Get the file path from Write tool context (passed as environment variable)
FILE_PATH="${CLAUDE_TOOL_FILE_PATH:-}"

# Only validate Java files
if [[ ! "$FILE_PATH" =~ \.java$ ]]; then
  exit 0
fi

# Check if file exists
if [ ! -f "$FILE_PATH" ]; then
  exit 0
fi

# Pattern 1: Wrong requireThat import (internal API)
if grep -q "import static io.github.cowwoc.requirements.java.internal.implementation" "$FILE_PATH" 2>/dev/null; then
  echo "⚠️  WARNING: Wrong requireThat() import detected in $FILE_PATH" >&2
  echo "" >&2
  echo "Found: import static io.github.cowwoc.requirements.java.internal.implementation.*" >&2
  echo "Should be: import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;" >&2
  echo "" >&2
  echo "See: docs/project/quality-guide.md § Parameter Validation" >&2
  # Don't fail - just warn
fi

# Pattern 2: Wrong parameter order - requireThat("name", value) instead of requireThat(value, "name")
# This is a heuristic - check for requireThat with string literal as first parameter
if grep -E 'requireThat\s*\(\s*"[^"]+"\s*,' "$FILE_PATH" 2>/dev/null | head -1; then
  echo "⚠️  WARNING: Possible wrong requireThat() parameter order in $FILE_PATH" >&2
  echo "" >&2
  echo "Pattern detected: requireThat(\"name\", value)" >&2
  echo "Correct pattern: requireThat(value, \"name\")" >&2
  echo "" >&2
  echo "First parameter should be the VALUE, second parameter should be the NAME" >&2
  echo "See: docs/project/quality-guide.md § Parameter Validation" >&2
  echo "" >&2
  grep -n -E 'requireThat\s*\(\s*"[^"]+"\s*,' "$FILE_PATH" | head -5 >&2
  # Don't fail - just warn
fi

# Pattern 3: Using .trim() instead of .strip() (Unicode whitespace)
# Added: 2025-12-11 - style guide rule existed but was violated
if grep -E '\.trim\s*\(\s*\)' "$FILE_PATH" 2>/dev/null | head -1 > /dev/null; then
  echo "⚠️  WARNING: .trim() usage detected in $FILE_PATH" >&2
  echo "" >&2
  echo "Style guide requires .strip() instead of .trim()" >&2
  echo "Reason: strip() handles Unicode whitespace, trim() only handles ASCII" >&2
  echo "" >&2
  echo "See: .claude/rules/java-style.md § Code Patterns" >&2
  echo "" >&2
  echo "Occurrences:" >&2
  grep -n -E '\.trim\s*\(\s*\)' "$FILE_PATH" | head -5 >&2
  # Don't fail - just warn
fi

exit 0
