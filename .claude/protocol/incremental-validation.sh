#!/bin/bash
#
# Incremental Validation System
#
# Validates only changed files rather than entire codebase to improve efficiency.
# Runs checkstyle, PMD, and tests on modified files only.
#
# Usage:
#   ./incremental-validation.sh --task-dir TASK_DIR [--round ROUND]

set -euo pipefail

TASK_DIR=""
ROUND=1
VERBOSE=false

usage() {
    cat <<EOF
Usage: $0 --task-dir TASK_DIR [OPTIONS]

Options:
    --task-dir DIR          Task directory
    --round NUM             Convergence round number (default: 1)
    --verbose               Verbose output
    --help                  Show this help

Description:
    Runs validation only on files modified in the current convergence round.
    Includes checkstyle, PMD, and targeted unit tests.

Examples:
    # Validate round 1 changes
    $0 --task-dir /workspace/branches/my-task

    # Validate round 2 with verbose output
    $0 --task-dir /workspace/branches/my-task --round 2 --verbose

EOF
    exit 1
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --task-dir)
            TASK_DIR="$2"
            shift 2
            ;;
        --round)
            ROUND="$2"
            shift 2
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        --help)
            usage
            ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done

# Validate required arguments
if [ -z "$TASK_DIR" ]; then
    echo "Error: --task-dir is required"
    usage
fi

CODE_DIR="${TASK_DIR}/code"
if [ ! -d "$CODE_DIR" ]; then
    echo "Error: Code directory not found: $CODE_DIR"
    exit 1
fi

echo "=== INCREMENTAL VALIDATION ==="
echo "Task: $(basename "$TASK_DIR")"
echo "Round: $ROUND"
echo "Directory: $CODE_DIR"
echo ""

# Get list of changed files
cd "$CODE_DIR"

echo "📋 Identifying changed files..."
CHANGED_FILES=$(git diff --name-only HEAD~1 2>/dev/null || echo "")

if [ -z "$CHANGED_FILES" ]; then
    echo "⚠️  No changed files detected - falling back to full validation"
    # Run full validation
    if [ -f "mvnw" ]; then
        ./mvnw verify
    else
        mvn verify
    fi
    exit $?
fi

echo "Changed files:"
echo "$CHANGED_FILES" | while read -r file; do
    echo "  - $file"
done
echo ""

# Filter to Java files only
JAVA_FILES=$(echo "$CHANGED_FILES" | grep '\.java$' || true)

if [ -z "$JAVA_FILES" ]; then
    echo "✅ No Java files changed - skipping Java validation"
    exit 0
fi

echo "Java files to validate:"
echo "$JAVA_FILES" | while read -r file; do
    echo "  - $file"
done
echo ""

# Run checkstyle on changed files
echo "🔍 Running checkstyle on changed files..."
CHECKSTYLE_FAILED=false

echo "$JAVA_FILES" | while read -r file; do
    if [ -f "mvnw" ]; then
        ./mvnw checkstyle:check -Dcheckstyle.includes="**/${file}" || CHECKSTYLE_FAILED=true
    else
        mvn checkstyle:check -Dcheckstyle.includes="**/${file}" || CHECKSTYLE_FAILED=true
    fi
done

if [ "$CHECKSTYLE_FAILED" = true ]; then
    echo "❌ Checkstyle validation failed"
    exit 1
fi

echo "✅ Checkstyle passed for changed files"
echo ""

# Run PMD on changed files
echo "🔍 Running PMD on changed files..."
PMD_FAILED=false

# PMD requires full module check, but we can filter results
if [ -f "mvnw" ]; then
    ./mvnw pmd:check || PMD_FAILED=true
else
    mvn pmd:check || PMD_FAILED=true
fi

if [ "$PMD_FAILED" = true ]; then
    echo "❌ PMD validation failed"
    exit 1
fi

echo "✅ PMD passed"
echo ""

# Run tests for changed classes
echo "🧪 Running tests for changed classes..."
TEST_FAILED=false

echo "$JAVA_FILES" | while read -r file; do
    # Convert src/main/java/Foo.java -> src/test/java/FooTest.java
    if [[ "$file" == src/main/java/* ]]; then
        test_file=$(echo "$file" | sed 's|src/main/java/|src/test/java/|' | sed 's|\.java$|Test.java|')

        if [ -f "$test_file" ]; then
            test_class=$(basename "$test_file" .java)
            echo "  Running test: $test_class"

            if [ -f "mvnw" ]; then
                ./mvnw test -Dtest="$test_class" || TEST_FAILED=true
            else
                mvn test -Dtest="$test_class" || TEST_FAILED=true
            fi
        else
            echo "  ⚠️  No test found for $file (expected: $test_file)"
        fi
    fi
done

if [ "$TEST_FAILED" = true ]; then
    echo "❌ Tests failed"
    exit 1
fi

echo "✅ Tests passed for changed files"
echo ""

# Summary
echo "=== VALIDATION SUMMARY ==="
echo "✅ Checkstyle: PASSED"
echo "✅ PMD: PASSED"
echo "✅ Tests: PASSED"
echo ""
echo "✅ Incremental validation complete"
