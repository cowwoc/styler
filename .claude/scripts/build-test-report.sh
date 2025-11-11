#!/bin/bash
set -euo pipefail

# build-test-report.sh - Atomic build and test with structured reporting
#
# This script runs Maven build and tests in a single operation,
# reducing LLM round-trips from 5-7 to 2-3 (60-75% faster overhead).
#
# Usage:
#   build-test-report.sh [--skip-tests] [--profile <name>] [--module <module>]
#
# Parameters:
#   --skip-tests       - Skip test execution (compile only)
#   --profile <name>   - Use specific Maven profile
#   --module <module>  - Build specific module only

# ============================================================================
# CONFIGURATION
# ============================================================================

SKIP_TESTS=false
MAVEN_PROFILE=""
MODULE=""

# Parse arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-tests)
      SKIP_TESTS=true
      shift
      ;;
    --profile)
      MAVEN_PROFILE="$2"
      shift 2
      ;;
    --module)
      MODULE="$2"
      shift 2
      ;;
    *)
      echo "ERROR: Unknown option: $1" >&2
      echo "Usage: build-test-report.sh [--skip-tests] [--profile <name>] [--module <module>]" >&2
      exit 1
      ;;
  esac
done

# ============================================================================
# EXECUTION RESULT TRACKING
# ============================================================================

RESULT_JSON="/tmp/build-test-result-$$.json"
START_TIME=$(date +%s)

# Initialize result variables
BUILD_STATUS="not_run"
TEST_STATUS="not_run"
BUILD_OUTPUT=""
TEST_OUTPUT=""
BUILD_ERRORS=""
TEST_ERRORS=""
BUILD_DURATION=0
TEST_DURATION=0

# Function to output structured JSON result
output_result() {
  local status="$1"
  local message="$2"
  local end_time=$(date +%s)
  local total_duration=$((end_time - START_TIME))

  cat > "$RESULT_JSON" <<EOF
{
  "status": "$status",
  "message": "$message",
  "total_duration_seconds": $total_duration,
  "build": {
    "status": "$BUILD_STATUS",
    "duration_seconds": $BUILD_DURATION,
    "errors": "$BUILD_ERRORS"
  },
  "tests": {
    "status": "$TEST_STATUS",
    "duration_seconds": $TEST_DURATION,
    "tests_run": ${TESTS_RUN:-0},
    "tests_failed": ${TESTS_FAILED:-0},
    "tests_errors": ${TESTS_ERRORS:-0},
    "tests_skipped": ${TESTS_SKIPPED:-0},
    "errors": "$TEST_ERRORS"
  },
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

# Check if Maven wrapper exists
if [[ ! -f "./mvnw" ]]; then
  output_result "error" "Maven wrapper (mvnw) not found in current directory"
fi

# Check if pom.xml exists
if [[ ! -f "./pom.xml" ]]; then
  output_result "error" "pom.xml not found in current directory"
fi

echo "✅ Maven project detected"

# ============================================================================
# STEP 2: RUN BUILD
# ============================================================================

echo ""
echo "Step 2: Running Maven build..."

# Construct Maven command
MAVEN_CMD="./mvnw clean compile"

if [[ -n "$MAVEN_PROFILE" ]]; then
  MAVEN_CMD="$MAVEN_CMD -P$MAVEN_PROFILE"
  echo "Using Maven profile: $MAVEN_PROFILE"
fi

if [[ -n "$MODULE" ]]; then
  MAVEN_CMD="$MAVEN_CMD -pl $MODULE -am"
  echo "Building module: $MODULE"
fi

echo "Command: $MAVEN_CMD"
echo ""

# Run build and capture output
BUILD_START=$(date +%s)
if BUILD_OUTPUT=$(eval "$MAVEN_CMD" 2>&1); then
  BUILD_STATUS="success"
  echo "✅ Build succeeded"
else
  BUILD_STATUS="failed"
  BUILD_ERRORS=$(echo "$BUILD_OUTPUT" | grep -A 10 "\[ERROR\]" || echo "Build failed - see output")

  echo "❌ Build failed"
  echo ""
  echo "Build errors:"
  echo "$BUILD_ERRORS"

  BUILD_END=$(date +%s)
  BUILD_DURATION=$((BUILD_END - BUILD_START))

  output_result "error" "Build failed - compilation errors"
fi

BUILD_END=$(date +%s)
BUILD_DURATION=$((BUILD_END - BUILD_START))

echo "Build duration: ${BUILD_DURATION}s"

# ============================================================================
# STEP 3: RUN TESTS (OPTIONAL)
# ============================================================================

if [[ "$SKIP_TESTS" == "true" ]]; then
  echo ""
  echo "Step 3: Skipping tests (--skip-tests flag provided)"
  TEST_STATUS="skipped"
else
  echo ""
  echo "Step 3: Running Maven tests..."

  # Construct test command
  TEST_CMD="./mvnw test"

  if [[ -n "$MAVEN_PROFILE" ]]; then
    TEST_CMD="$TEST_CMD -P$MAVEN_PROFILE"
  fi

  if [[ -n "$MODULE" ]]; then
    TEST_CMD="$TEST_CMD -pl $MODULE"
  fi

  echo "Command: $TEST_CMD"
  echo ""

  # Run tests and capture output
  TEST_START=$(date +%s)
  if TEST_OUTPUT=$(eval "$TEST_CMD" 2>&1); then
    TEST_STATUS="success"
    echo "✅ All tests passed"
  else
    TEST_STATUS="failed"
    TEST_ERRORS=$(echo "$TEST_OUTPUT" | grep -A 10 "FAILURE\|ERROR" || echo "Tests failed - see output")

    echo "❌ Tests failed"
  fi

  TEST_END=$(date +%s)
  TEST_DURATION=$((TEST_END - TEST_START))

  # Parse test results
  if echo "$TEST_OUTPUT" | grep -q "Tests run:"; then
    TEST_SUMMARY=$(echo "$TEST_OUTPUT" | grep "Tests run:" | tail -1)
    TESTS_RUN=$(echo "$TEST_SUMMARY" | sed -n 's/.*Tests run: \([0-9]*\).*/\1/p')
    TESTS_FAILED=$(echo "$TEST_SUMMARY" | sed -n 's/.*Failures: \([0-9]*\).*/\1/p')
    TESTS_ERRORS=$(echo "$TEST_SUMMARY" | sed -n 's/.*Errors: \([0-9]*\).*/\1/p')
    TESTS_SKIPPED=$(echo "$TEST_SUMMARY" | sed -n 's/.*Skipped: \([0-9]*\).*/\1/p')

    echo ""
    echo "Test Results:"
    echo "  Tests run:    ${TESTS_RUN:-0}"
    echo "  Failures:     ${TESTS_FAILED:-0}"
    echo "  Errors:       ${TESTS_ERRORS:-0}"
    echo "  Skipped:      ${TESTS_SKIPPED:-0}"
  fi

  echo "Test duration: ${TEST_DURATION}s"

  # If tests failed, output errors and exit
  if [[ "$TEST_STATUS" == "failed" ]]; then
    echo ""
    echo "Test failures/errors:"
    echo "$TEST_ERRORS"

    output_result "error" "Tests failed - ${TESTS_FAILED:-0} failures, ${TESTS_ERRORS:-0} errors"
  fi
fi

# ============================================================================
# STEP 4: SUMMARY
# ============================================================================

echo ""
echo "════════════════════════════════════════"
echo "Build and Test Summary:"
echo "  Build:        $BUILD_STATUS (${BUILD_DURATION}s)"
echo "  Tests:        $TEST_STATUS (${TEST_DURATION}s)"
if [[ "$TEST_STATUS" != "skipped" ]]; then
  echo "  Tests Run:    ${TESTS_RUN:-0}"
  echo "  Tests Failed: ${TESTS_FAILED:-0}"
fi
echo "════════════════════════════════════════"

# ============================================================================
# SUCCESS
# ============================================================================

if [[ "$SKIP_TESTS" == "true" ]]; then
  output_result "success" "Build completed successfully (tests skipped)"
else
  output_result "success" "Build and tests completed successfully"
fi
