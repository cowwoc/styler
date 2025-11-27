#!/bin/bash
# Test suite for json-output.sh functions
#
# Tests json_error() and json_success() functions added in commit 0857c68
# ADDED: 2025-11-26 to verify JSON output helpers work correctly
# PREVENTS: Malformed JSON output, incorrect exit codes, lost additional fields

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JSON_OUTPUT_SCRIPT="/workspace/main/.claude/scripts/json-output.sh"

# Source the script once at the beginning
source "$JSON_OUTPUT_SCRIPT"

# Test counters
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test result tracking
fail_test() {
	local test_name="$1"
	local reason="$2"
	echo -e "${RED}❌ FAIL${NC}: $test_name"
	echo "   Reason: $reason"
	((TESTS_FAILED++)) || true
}

pass_test() {
	local test_name="$1"
	echo -e "${GREEN}✅ PASS${NC}: $test_name"
	((TESTS_PASSED++)) || true
}

run_test() {
	((TESTS_RUN++)) || true
}

echo "🧪 Testing json-output.sh functions"
echo "===================================="
echo ""

# Test 1: json_error() outputs correct JSON structure
run_test
TEST_NAME="json_error() outputs valid JSON with status=error"
set +e
OUTPUT=$(json_error 'Test error message' 2>&1)
EXIT_CODE=$?
set -e

# Verify it's valid JSON
if ! echo "$OUTPUT" | jq . >/dev/null 2>&1; then
	fail_test "$TEST_NAME" "Output is not valid JSON: $OUTPUT"
elif [[ $EXIT_CODE -ne 1 ]]; then
	fail_test "$TEST_NAME" "Expected exit code 1, got $EXIT_CODE"
else
	# Check JSON structure
	STATUS=$(echo "$OUTPUT" | jq -r '.status')
	MESSAGE=$(echo "$OUTPUT" | jq -r '.message')

	if [[ "$STATUS" != "error" ]]; then
		fail_test "$TEST_NAME" "Expected status='error', got '$STATUS'"
	elif [[ "$MESSAGE" != "Test error message" ]]; then
		fail_test "$TEST_NAME" "Expected message='Test error message', got '$MESSAGE'"
	else
		pass_test "$TEST_NAME"
	fi
fi

# Test 2: json_error() handles special characters
run_test
TEST_NAME="json_error() handles special characters in message"
SPECIAL_MSG='Error with "quotes" and $variables and newlines\nand tabs\t'
set +e
OUTPUT=$(json_error "$SPECIAL_MSG" 2>&1)
set -e

if ! echo "$OUTPUT" | jq . >/dev/null 2>&1; then
	fail_test "$TEST_NAME" "Output is not valid JSON with special chars"
else
	MESSAGE=$(echo "$OUTPUT" | jq -r '.message')
	if [[ "$MESSAGE" != "$SPECIAL_MSG" ]]; then
		fail_test "$TEST_NAME" "Message not preserved: expected '$SPECIAL_MSG', got '$MESSAGE'"
	else
		pass_test "$TEST_NAME"
	fi
fi

# Test 3: json_success() outputs correct JSON with message only
run_test
TEST_NAME="json_success() outputs valid JSON with status=success (message only)"
OUTPUT=$(json_success 'Operation completed')

if ! echo "$OUTPUT" | jq . >/dev/null 2>&1; then
	fail_test "$TEST_NAME" "Output is not valid JSON: $OUTPUT"
else
	STATUS=$(echo "$OUTPUT" | jq -r '.status')
	MESSAGE=$(echo "$OUTPUT" | jq -r '.message')

	if [[ "$STATUS" != "success" ]]; then
		fail_test "$TEST_NAME" "Expected status='success', got '$STATUS'"
	elif [[ "$MESSAGE" != "Operation completed" ]]; then
		fail_test "$TEST_NAME" "Expected message='Operation completed', got '$MESSAGE'"
	else
		pass_test "$TEST_NAME"
	fi
fi

# Test 4: json_success() merges additional fields
run_test
TEST_NAME="json_success() merges additional JSON fields"
ADDITIONAL_JSON='{"task_name":"test-task","duration":42,"files_changed":5}'
OUTPUT=$(json_success 'Task completed' "$ADDITIONAL_JSON")

if ! echo "$OUTPUT" | jq . >/dev/null 2>&1; then
	fail_test "$TEST_NAME" "Output is not valid JSON: $OUTPUT"
else
	STATUS=$(echo "$OUTPUT" | jq -r '.status')
	MESSAGE=$(echo "$OUTPUT" | jq -r '.message')
	TASK_NAME=$(echo "$OUTPUT" | jq -r '.task_name')
	DURATION=$(echo "$OUTPUT" | jq -r '.duration')
	FILES_CHANGED=$(echo "$OUTPUT" | jq -r '.files_changed')

	if [[ "$STATUS" != "success" ]]; then
		fail_test "$TEST_NAME" "Expected status='success', got '$STATUS'"
	elif [[ "$MESSAGE" != "Task completed" ]]; then
		fail_test "$TEST_NAME" "Expected message='Task completed', got '$MESSAGE'"
	elif [[ "$TASK_NAME" != "test-task" ]]; then
		fail_test "$TEST_NAME" "Expected task_name='test-task', got '$TASK_NAME'"
	elif [[ "$DURATION" != "42" ]]; then
		fail_test "$TEST_NAME" "Expected duration=42, got '$DURATION'"
	elif [[ "$FILES_CHANGED" != "5" ]]; then
		fail_test "$TEST_NAME" "Expected files_changed=5, got '$FILES_CHANGED'"
	else
		pass_test "$TEST_NAME"
	fi
fi

# Test 5: json_success() with empty additional fields defaults to {}
run_test
TEST_NAME="json_success() defaults additional fields to {}"
OUTPUT=$(json_success 'Simple success')

if ! echo "$OUTPUT" | jq . >/dev/null 2>&1; then
	fail_test "$TEST_NAME" "Output is not valid JSON: $OUTPUT"
else
	# Should only have status and message fields
	FIELD_COUNT=$(echo "$OUTPUT" | jq 'keys | length')

	if [[ "$FIELD_COUNT" != "2" ]]; then
		fail_test "$TEST_NAME" "Expected 2 fields (status, message), got $FIELD_COUNT: $(echo "$OUTPUT" | jq 'keys')"
	else
		pass_test "$TEST_NAME"
	fi
fi

# Test 6: json_success() doesn't exit with error
run_test
TEST_NAME="json_success() exits with code 0"
json_success 'Test success' >/dev/null 2>&1
EXIT_CODE=$?

if [[ $EXIT_CODE -ne 0 ]]; then
	fail_test "$TEST_NAME" "Expected exit code 0, got $EXIT_CODE"
else
	pass_test "$TEST_NAME"
fi

# Test 7: json_error() always exits even when called in subshell
run_test
TEST_NAME="json_error() exits (doesn't continue execution)"
# This test verifies that json_error() actually exits, not just returns
OUTPUT=$( (json_error 'Fatal error'; echo 'SHOULD NOT PRINT') 2>&1 || true)

if echo "$OUTPUT" | grep -q "SHOULD NOT PRINT"; then
	fail_test "$TEST_NAME" "json_error() did not exit - execution continued"
else
	pass_test "$TEST_NAME"
fi

# Test 8: json_success() handles nested JSON in additional fields
run_test
TEST_NAME="json_success() handles nested JSON structures"
NESTED_JSON='{"result":{"files":["a.txt","b.txt"],"stats":{"total":2,"success":2}}}'
OUTPUT=$(json_success 'Complex result' "$NESTED_JSON")

if ! echo "$OUTPUT" | jq . >/dev/null 2>&1; then
	fail_test "$TEST_NAME" "Output is not valid JSON: $OUTPUT"
else
	# Verify nested structure is preserved
	FILES=$(echo "$OUTPUT" | jq -r '.result.files | length')
	TOTAL=$(echo "$OUTPUT" | jq -r '.result.stats.total')

	if [[ "$FILES" != "2" ]]; then
		fail_test "$TEST_NAME" "Nested array not preserved correctly"
	elif [[ "$TOTAL" != "2" ]]; then
		fail_test "$TEST_NAME" "Nested object not preserved correctly"
	else
		pass_test "$TEST_NAME"
	fi
fi

# Summary
echo ""
echo "===================================="
echo "Test Summary"
echo "===================================="
echo "Tests run:    $TESTS_RUN"
echo -e "Tests passed: ${GREEN}$TESTS_PASSED${NC}"
if [[ $TESTS_FAILED -gt 0 ]]; then
	echo -e "Tests failed: ${RED}$TESTS_FAILED${NC}"
else
	echo -e "Tests failed: $TESTS_FAILED"
fi
echo ""

if [[ $TESTS_FAILED -eq 0 ]]; then
	echo -e "${GREEN}✅ All tests passed!${NC}"
	exit 0
else
	echo -e "${RED}❌ Some tests failed${NC}"
	exit 1
fi
