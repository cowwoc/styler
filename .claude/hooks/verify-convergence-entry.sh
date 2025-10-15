#!/bin/bash
# Verify clean state before CONVERGENCE entry (uncommitted changes, test pass, style, test count)

set -euo pipefail

# Find the task lock file based on current directory
CURRENT_DIR="$(pwd)"
TASK_NAME="$(basename "$(dirname "$CURRENT_DIR")")"

# Try multiple lock file patterns
LOCK_FILE=""
if [ -f "/workspace/locks/${TASK_NAME}.json" ]; then
    LOCK_FILE="/workspace/locks/${TASK_NAME}.json"
elif [ -f "/workspace/locks/$(basename "$CURRENT_DIR").json" ]; then
    LOCK_FILE="/workspace/locks/$(basename "$CURRENT_DIR").json"
fi

# No lock file = no enforcement
[ -z "$LOCK_FILE" ] && exit 0
[ ! -f "$LOCK_FILE" ] && exit 0

# Read lock state
STATE=$(jq -r '.state // "UNKNOWN"' "$LOCK_FILE" 2>/dev/null || echo "UNKNOWN")

# Detect transition TO convergence
if [[ "$STATE" == "CONVERGENCE" ]]; then
    # Gate 1: Check for uncommitted implementation changes
    if ! git diff --exit-code --quiet src/ 2>/dev/null; then
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        echo "❌ CONVERGENCE ENTRY VIOLATION - Uncommitted Changes" >&2
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        echo "" >&2
        echo "Uncommitted changes detected in src/" >&2
        echo "" >&2
        git diff --stat src/ 2>/dev/null || true >&2
        echo "" >&2
        echo "CONVERGENCE requires clean working directory." >&2
        echo "Main agent MUST NOT implement fixes directly." >&2
        echo "" >&2
        echo "RECOVERY:" >&2
        echo "  git diff src/ > /tmp/unauthorized-changes.patch" >&2
        echo "  git reset --hard HEAD" >&2
        echo "  # Return to IMPLEMENTATION and delegate fixes to technical-architect" >&2
        echo "  jq '.state = \"IMPLEMENTATION\"' $LOCK_FILE > /tmp/lock.json && mv /tmp/lock.json $LOCK_FILE" >&2
        echo "" >&2
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        exit 1
    fi

    # Gate 2: Check tests pass
    if ! ./mvnw test -q 2>/dev/null; then
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        echo "❌ CONVERGENCE ENTRY VIOLATION - Test Failures" >&2
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        echo "" >&2
        echo "Tests failing - CONVERGENCE requires ALL tests passing" >&2
        echo "Main agent MUST NOT implement fixes directly." >&2
        echo "" >&2
        echo "RECOVERY:" >&2
        echo "  ./mvnw test  # Identify failures" >&2
        echo "  # Return to IMPLEMENTATION and delegate fixes to technical-architect" >&2
        echo "  jq '.state = \"IMPLEMENTATION\"' $LOCK_FILE > /tmp/lock.json && mv /tmp/lock.json $LOCK_FILE" >&2
        echo "" >&2
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        exit 1
    fi

    # Gate 3: Check style compliance
    if ! ./mvnw checkstyle:check pmd:check -q 2>/dev/null; then
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        echo "❌ CONVERGENCE ENTRY VIOLATION - Style Violations" >&2
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        echo "" >&2
        echo "Style violations detected - CONVERGENCE requires checkstyle/PMD clean" >&2
        echo "Main agent MUST NOT implement fixes directly." >&2
        echo "" >&2
        echo "RECOVERY:" >&2
        echo "  ./mvnw checkstyle:check pmd:check  # View violations" >&2
        echo "  # Return to IMPLEMENTATION and delegate fixes to technical-architect" >&2
        echo "  jq '.state = \"IMPLEMENTATION\"' $LOCK_FILE > /tmp/lock.json && mv /tmp/lock.json $LOCK_FILE" >&2
        echo "" >&2
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        exit 1
    fi

    # Gate 4: Check minimum test count (≥15 tests)
    TEST_COUNT=$(find src/test -name "*Test.java" -exec grep -c "@Test" {} + 2>/dev/null | awk '{sum+=$1} END {print sum}')
    if [ -z "$TEST_COUNT" ]; then
        TEST_COUNT=0
    fi

    if [ "$TEST_COUNT" -lt 15 ]; then
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        echo "❌ CONVERGENCE ENTRY VIOLATION - Insufficient Test Coverage" >&2
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        echo "" >&2
        echo "Test count: $TEST_COUNT (minimum: 15)" >&2
        echo "" >&2
        echo "CONVERGENCE requires comprehensive test coverage:" >&2
        echo "  - Null/Empty validation: 2-3 tests" >&2
        echo "  - Boundary conditions: 2-3 tests" >&2
        echo "  - Edge cases: 3-5 tests" >&2
        echo "  - Algorithm precision: 3-5 tests (if applicable)" >&2
        echo "  - Configuration validation: 2-3 tests" >&2
        echo "Main agent MUST NOT implement fixes directly." >&2
        echo "" >&2
        echo "RECOVERY:" >&2
        echo "  # Return to IMPLEMENTATION and delegate to code-tester agent" >&2
        echo "  jq '.state = \"IMPLEMENTATION\"' $LOCK_FILE > /tmp/lock.json && mv /tmp/lock.json $LOCK_FILE" >&2
        echo "" >&2
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" >&2
        exit 1
    fi
fi

exit 0
