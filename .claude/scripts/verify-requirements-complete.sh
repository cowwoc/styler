#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in verify-requirements-complete.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

# Usage: verify-requirements-complete.sh TASK_NAME

if [ $# -lt 1 ]; then
    json_error "Usage: verify-requirements-complete.sh TASK_NAME"
fi

TASK_NAME="$1"
TASK_DIR="/workspace/tasks/$TASK_NAME"

# Check all 3 requirement reports exist
ARCH_REPORT="$TASK_DIR/$TASK_NAME-architect-requirements.md"
TEST_REPORT="$TASK_DIR/$TASK_NAME-tester-requirements.md"
FMT_REPORT="$TASK_DIR/$TASK_NAME-formatter-requirements.md"

MISSING=()
[ -f "$ARCH_REPORT" ] || MISSING+=("architect-requirements.md")
[ -f "$TEST_REPORT" ] || MISSING+=("tester-requirements.md")
[ -f "$FMT_REPORT" ] || MISSING+=("formatter-requirements.md")

if [ ${#MISSING[@]} -gt 0 ]; then
    json_error "Missing reports: ${MISSING[*]}"
fi

# Check reports are non-empty
for report in "$ARCH_REPORT" "$TEST_REPORT" "$FMT_REPORT"; do
    SIZE=$(wc -c < "$report")
    if [ "$SIZE" -lt 100 ]; then
        json_error "Report too small (likely placeholder): $report"
    fi
done

json_success "task_name=$TASK_NAME" "ready_for_synthesis=true"
