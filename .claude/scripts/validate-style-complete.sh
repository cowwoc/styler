#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in validate-style-complete.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/json-output.sh"

# Usage: validate-style-complete.sh [MODULE]

cd /workspace/main

# Module-specific or full validation
MODULE_FLAG=""
if [ $# -ge 1 ]; then
    MODULE_FLAG="-pl :$1"
fi

# Run Checkstyle
echo "Running Checkstyle..." >&2
if ./mvnw checkstyle:check $MODULE_FLAG -q 2>&1; then
    CHECKSTYLE_PASS=true
    CHECKSTYLE_VIOLATIONS=0
else
    CHECKSTYLE_PASS=false
    CHECKSTYLE_VIOLATIONS=$(./mvnw checkstyle:check $MODULE_FLAG 2>&1 | grep -c "Checkstyle violations" || echo "unknown")
fi

# Run PMD
echo "Running PMD..." >&2
if ./mvnw pmd:check $MODULE_FLAG -q 2>&1; then
    PMD_PASS=true
    PMD_VIOLATIONS=0
else
    PMD_PASS=false
    PMD_VIOLATIONS=$(./mvnw pmd:check $MODULE_FLAG 2>&1 | grep -c "PMD Failure" || echo "unknown")
fi

# Overall status
if [ "$CHECKSTYLE_PASS" = true ] && [ "$PMD_PASS" = true ]; then
    json_success "checkstyle_pass=$CHECKSTYLE_PASS" "pmd_pass=$PMD_PASS" "overall=PASSED"
else
    json_error "Style validation failed: Checkstyle=$CHECKSTYLE_PASS, PMD=$PMD_PASS"
fi
