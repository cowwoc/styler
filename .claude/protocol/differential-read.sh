#!/bin/bash
#
# Differential Reading System for File-Based Agent Communication
#
# Implements read-once + diff-based updates pattern to minimize context usage.
# Agents read files once during initial analysis, then work with diffs in
# subsequent convergence rounds.
#
# Usage:
#   ./differential-read.sh --mode initial|diff --file FILE --baseline-sha SHA

set -euo pipefail

MODE=""
FILE=""
BASELINE_SHA=""
TASK_DIR=""

usage() {
    cat <<EOF
Usage: $0 --mode MODE --file FILE [OPTIONS]

Modes:
    initial     Read complete file (first read)
    diff        Read only changes since baseline

Options:
    --file FILE             File to read
    --baseline-sha SHA      SHA256 hash of baseline version (for diff mode)
    --task-dir DIR          Task directory (default: current directory)
    --help                  Show this help

Examples:
    # Initial read (full file)
    $0 --mode initial --file src/main/java/Token.java

    # Differential read (only changes)
    $0 --mode diff --file src/main/java/Token.java --baseline-sha abc123...

EOF
    exit 1
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --mode)
            MODE="$2"
            shift 2
            ;;
        --file)
            FILE="$2"
            shift 2
            ;;
        --baseline-sha)
            BASELINE_SHA="$2"
            shift 2
            ;;
        --task-dir)
            TASK_DIR="$2"
            shift 2
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
if [ -z "$MODE" ] || [ -z "$FILE" ]; then
    echo "Error: --mode and --file are required"
    usage
fi

if [ "$MODE" != "initial" ] && [ "$MODE" != "diff" ]; then
    echo "Error: mode must be 'initial' or 'diff'"
    usage
fi

if [ "$MODE" = "diff" ] && [ -z "$BASELINE_SHA" ]; then
    echo "Error: --baseline-sha required for diff mode"
    usage
fi

# Set task directory
if [ -z "$TASK_DIR" ]; then
    TASK_DIR=$(pwd)
fi

CODE_DIR="${TASK_DIR}/code"
if [ ! -d "$CODE_DIR" ]; then
    echo "Error: Code directory not found: $CODE_DIR"
    exit 1
fi

FILE_PATH="${CODE_DIR}/${FILE}"
if [ ! -f "$FILE_PATH" ]; then
    echo "Error: File not found: $FILE_PATH"
    exit 1
fi

# Execute based on mode
if [ "$MODE" = "initial" ]; then
    # Initial read: return full file content
    echo "=== DIFFERENTIAL READ: INITIAL (full file) ==="
    echo "File: $FILE"
    echo "Lines: $(wc -l < "$FILE_PATH")"
    echo "SHA256: $(sha256sum "$FILE_PATH" | cut -d' ' -f1)"
    echo ""
    echo "=== FILE CONTENT ==="
    cat -n "$FILE_PATH"
    echo ""
    echo "=== END FILE CONTENT ==="

    # Save baseline hash for future diff reads
    BASELINE_FILE="${TASK_DIR}/.baselines/${FILE}.sha256"
    mkdir -p "$(dirname "$BASELINE_FILE")"
    sha256sum "$FILE_PATH" | cut -d' ' -f1 > "$BASELINE_FILE"

elif [ "$MODE" = "diff" ]; then
    # Differential read: return only changes since baseline
    echo "=== DIFFERENTIAL READ: DIFF (changes only) ==="
    echo "File: $FILE"
    echo "Baseline SHA: $BASELINE_SHA"

    # Get current hash
    CURRENT_SHA=$(sha256sum "$FILE_PATH" | cut -d' ' -f1)
    echo "Current SHA: $CURRENT_SHA"

    if [ "$CURRENT_SHA" = "$BASELINE_SHA" ]; then
        echo "Status: UNCHANGED"
        echo "No diff needed - file unchanged since baseline"
        exit 0
    fi

    echo "Status: MODIFIED"
    echo ""
    echo "=== DIFF FROM BASELINE ==="

    # Try to get diff from git
    cd "$CODE_DIR"
    if git rev-parse HEAD >/dev/null 2>&1; then
        # Git repository - use git diff
        # Find commit with matching file hash
        git diff HEAD -- "$FILE" || echo "(git diff unavailable, showing full file)"
    else
        # Not a git repo or can't find baseline - show full file
        echo "(Baseline not in git, showing full file)"
        cat -n "$FILE_PATH"
    fi

    echo ""
    echo "=== END DIFF ==="

    # Update baseline
    BASELINE_FILE="${TASK_DIR}/.baselines/${FILE}.sha256"
    mkdir -p "$(dirname "$BASELINE_FILE")"
    echo "$CURRENT_SHA" > "$BASELINE_FILE"
fi

echo ""
echo "✅ Differential read complete"
