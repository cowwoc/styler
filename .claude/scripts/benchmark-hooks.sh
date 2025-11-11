#!/bin/bash
# Hook performance benchmarking script
# Compares performance between original and optimized hooks
#
# Usage:
#   ./benchmark-hooks.sh [hook_name] [iterations]
#
# Examples:
#   ./benchmark-hooks.sh enforce-checkpoints 100
#   ./benchmark-hooks.sh all 50

set -euo pipefail

# Configuration
ITERATIONS="${2:-100}"
HOOK_NAME="${1:-all}"
HOOKS_DIR="/workspace/.claude/hooks"
METRICS_DIR="/workspace/.metrics"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Create metrics directory
mkdir -p "$METRICS_DIR"

# Generate sample hook input for testing
generate_sample_input() {
    local trigger="${1:-UserPromptSubmit}"

    case "$trigger" in
        UserPromptSubmit)
            cat <<EOF
{
  "session_id": "test-session-$(date +%s)",
  "prompt": "test prompt",
  "timestamp": "$(date -Iseconds)"
}
EOF
            ;;
        PreToolUse)
            cat <<EOF
{
  "tool_name": "Bash",
  "tool_input": {
    "command": "git status"
  },
  "eventName": "PreToolUse"
}
EOF
            ;;
        *)
            echo "{}"
            ;;
    esac
}

# Benchmark a single hook
benchmark_hook() {
    local hook_path="$1"
    local hook_name=$(basename "$hook_path" .sh)
    local iterations="$2"
    local input="$3"

    local total_time=0
    local min_time=999999
    local max_time=0

    echo -ne "${BLUE}Benchmarking $hook_name${NC} ($iterations iterations)... "

    for ((i=1; i<=iterations; i++)); do
        local start=$(date +%s%N)

        # Run hook with input
        echo "$input" | "$hook_path" > /dev/null 2>&1 || true

        local end=$(date +%s%N)
        local duration=$(( (end - start) / 1000000 ))  # Convert to milliseconds

        total_time=$((total_time + duration))

        if ((duration < min_time)); then
            min_time=$duration
        fi

        if ((duration > max_time)); then
            max_time=$duration
        fi
    done

    local avg_time=$((total_time / iterations))

    echo -e "${GREEN}Done${NC}"
    printf "  Avg: %4dms | Min: %4dms | Max: %4dms | Total: %6dms\n" \
        "$avg_time" "$min_time" "$max_time" "$total_time"

    # Return average for comparison
    echo "$avg_time"
}

# Compare original vs optimized
compare_hooks() {
    local original="$1"
    local optimized="$2"
    local iterations="$3"
    local input="$4"

    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo -e "${YELLOW}Comparing: $(basename "$original" .sh)${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    echo ""
    echo "Original version:"
    local original_avg=$(benchmark_hook "$original" "$iterations" "$input")

    echo ""
    echo "Optimized version:"
    local optimized_avg=$(benchmark_hook "$optimized" "$iterations" "$input")

    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Calculate improvement
    if ((original_avg > 0)); then
        local improvement=$(( (original_avg - optimized_avg) * 100 / original_avg ))
        local speedup=$(awk "BEGIN {printf \"%.2f\", $original_avg / $optimized_avg}")

        if ((improvement > 0)); then
            echo -e "${GREEN}Performance Improvement: $improvement% (${speedup}x speedup)${NC}"
        else
            local regression=$((improvement * -1))
            echo -e "${RED}Performance Regression: $regression%${NC}"
        fi
    fi

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
}

# Main execution
main() {
    echo ""
    echo "═══════════════════════════════════════════════════════"
    echo "           Hook Performance Benchmark"
    echo "═══════════════════════════════════════════════════════"
    echo ""
    echo "Iterations per test: $ITERATIONS"
    echo ""

    # Generate sample inputs
    local user_input=$(generate_sample_input "UserPromptSubmit")
    local tool_input=$(generate_sample_input "PreToolUse")

    if [[ "$HOOK_NAME" == "all" ]]; then
        # Benchmark all hooks that have optimized versions
        for original in "$HOOKS_DIR"/*.sh; do
            local base=$(basename "$original" .sh)
            local optimized="$HOOKS_DIR/${base}-optimized.sh"

            if [[ -f "$optimized" ]]; then
                # Determine appropriate input type
                local input="$user_input"
                if [[ "$base" == *"tool"* ]]; then
                    input="$tool_input"
                fi

                compare_hooks "$original" "$optimized" "$ITERATIONS" "$input"
            fi
        done
    else
        # Benchmark specific hook
        local original="$HOOKS_DIR/${HOOK_NAME}.sh"
        local optimized="$HOOKS_DIR/${HOOK_NAME}-optimized.sh"

        if [[ ! -f "$original" ]]; then
            echo -e "${RED}Error: Original hook not found: $original${NC}"
            exit 1
        fi

        if [[ ! -f "$optimized" ]]; then
            echo -e "${RED}Error: Optimized hook not found: $optimized${NC}"
            exit 1
        fi

        # Determine appropriate input type
        local input="$user_input"
        if [[ "$HOOK_NAME" == *"tool"* ]]; then
            input="$tool_input"
        fi

        compare_hooks "$original" "$optimized" "$ITERATIONS" "$input"
    fi

    echo ""
    echo "═══════════════════════════════════════════════════════"
    echo "           Benchmark Complete"
    echo "═══════════════════════════════════════════════════════"
    echo ""

    # Show timing report if available
    if [[ -f "$METRICS_DIR/hook-summary.jsonl" ]]; then
        echo "Detailed timing metrics available at:"
        echo "  $METRICS_DIR/hook-summary.jsonl"
        echo "  $METRICS_DIR/hook-timings.log"
        echo ""
    fi
}

# Run main
main
