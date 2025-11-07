#!/bin/bash
# Hook Integrity Validator
# Detects hooks that have been accidentally gutted (only shebang + set command)
#
# ADDED: 2025-11-07 after mistakenly removing all functional code from 7 hooks
# during timing instrumentation removal
# PREVENTS: Accidental removal of functional code, catching gutted hooks early

set -euo pipefail

HOOKS_DIR="/workspace/.claude/hooks"
MIN_LINES=10  # Hooks with <10 lines are suspicious (unless explicitly marked as minimal)
EXIT_CODE=0

echo "🔍 Validating hook integrity..."
echo ""

# List of hooks that are intentionally minimal (just pass-through or disabled)
MINIMAL_HOOKS=(
  "debug-tool-use.sh"  # Intentionally minimal when disabled
)

is_minimal_hook() {
  local hook="$1"
  for minimal in "${MINIMAL_HOOKS[@]}"; do
    if [[ "$hook" == "$minimal" ]]; then
      return 0
    fi
  done
  return 1
}

# Check each executable hook
for hook in "$HOOKS_DIR"/*.sh; do
  if [[ ! -f "$hook" ]] || [[ ! -x "$hook" ]]; then
    continue
  fi

  hook_name=$(basename "$hook")

  # Skip lib directory
  if [[ "$hook_name" == "lib/"* ]]; then
    continue
  fi

  # Count non-empty, non-comment lines
  functional_lines=$(grep -v '^\s*#' "$hook" | grep -v '^\s*$' | wc -l)

  # Check if hook is suspiciously small
  if [[ $functional_lines -lt $MIN_LINES ]]; then
    # Check if it's intentionally minimal
    if is_minimal_hook "$hook_name"; then
      echo "✅ $hook_name: Intentionally minimal ($functional_lines functional lines)"
    else
      echo "❌ $hook_name: SUSPICIOUSLY SMALL ($functional_lines functional lines)"
      echo "   Expected: At least $MIN_LINES functional lines"
      echo "   Check if functional code was accidentally removed"
      EXIT_CODE=1
    fi
  else
    echo "✅ $hook_name: $functional_lines functional lines"
  fi
done

echo ""
if [[ $EXIT_CODE -eq 0 ]]; then
  echo "✅ All hooks pass integrity check"
else
  echo "❌ Some hooks failed integrity check - may have been gutted"
fi

exit $EXIT_CODE
