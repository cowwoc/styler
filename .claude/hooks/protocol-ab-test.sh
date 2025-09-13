#!/bin/bash
# Task Protocol A/B Testing Framework
# Helps Claude choose between original and optimized versions for comparison testing

ORIGINAL_PROTOCOL="docs/project/task-protocol.md"
OPTIMIZED_PROTOCOL="docs/project/task-protocol-claude-optimized.md"

# Source metrics functions
source /workspace/branches/main/code/.claude/hooks/metrics-capture.sh 2>/dev/null || true

# Get current session ID
SESSION_ID="${CLAUDE_SESSION_ID:-$(whoami)-$$}"

# Determine which version to use based on session ID hash
get_protocol_version() {
    local task_type="${1:-general}"
    local force_version="${2:-auto}"
    
    if [[ "$force_version" == "original" ]]; then
        echo "original"
        return
    elif [[ "$force_version" == "optimized" ]]; then
        echo "optimized"
        return
    fi
    
    # Auto-selection based on session hash for A/B testing
    local hash=$(echo "$SESSION_ID" | md5sum 2>/dev/null | cut -d' ' -f1 || echo "$SESSION_ID")
    local last_digit="${hash: -1}"
    
    case "$last_digit" in
        [0-4]) echo "original" ;;
        [5-9a-f]) echo "optimized" ;;
    esac
}

# Get the appropriate protocol file path
get_protocol_file() {
    local version=$(get_protocol_version "$1" "$2")
    
    if [[ "$version" == "optimized" ]]; then
        echo "$OPTIMIZED_PROTOCOL"
    else
        echo "$ORIGINAL_PROTOCOL"
    fi
}

# Track which version is being used
track_version_selection() {
    local version="${1:-unknown}"
    local task_type="${2:-general}"
    local trigger="${3:-automatic}"
    
    # Track the selection for metrics
    track_protocol_version_usage "$version" "$task_type" "$trigger" "ab_test_selection" 2>/dev/null || true
}

# Provide recommendation to Claude
recommend_protocol_version() {
    local task_type="${1:-general}"
    local context="${2:-workflow}"
    
    local selected_version=$(get_protocol_version "$task_type" "auto")
    local protocol_file=$(get_protocol_file "$task_type" "auto")
    
    echo "🧪 A/B TEST PROTOCOL SELECTION"
    echo "Session ID: $SESSION_ID"
    echo "Selected Version: $selected_version"
    echo "Protocol File: $protocol_file"
    echo
    
    if [[ "$selected_version" == "optimized" ]]; then
        echo "📋 USING: Claude-Optimized Task Protocol"
        echo "Features: Self-contained sections, reduced cross-references, quick-start guide"
        echo "Expected Benefits: Faster navigation, reduced context switching, clearer decision trees"
    else
        echo "📋 USING: Original Task Protocol"
        echo "Features: Comprehensive detail, extensive cross-references, full documentation"
        echo "Expected Benefits: Complete information, established patterns, proven workflow"
    fi
    
    echo
    echo "💡 RECOMMENDATION: Read the specified protocol file to follow the task workflow."
    echo "📊 METRICS: Your usage patterns will be tracked for A/B comparison analysis."
    
    # Track this recommendation
    track_version_selection "$selected_version" "$task_type" "recommendation"
}

# Manual version forcing for testing
force_protocol_version() {
    local version="${1:-auto}"
    local task_type="${2:-general}"
    
    local protocol_file=$(get_protocol_file "$task_type" "$version")
    
    echo "🎯 FORCED PROTOCOL VERSION: $version"
    echo "Protocol File: $protocol_file"
    
    track_version_selection "$version" "$task_type" "manual_override"
}

# Generate A/B test status report
show_ab_test_status() {
    echo "🧪 TASK PROTOCOL A/B TEST STATUS"
    echo "================================"
    echo "Session ID: $SESSION_ID"
    echo "Original Protocol: $ORIGINAL_PROTOCOL"
    echo "Optimized Protocol: $OPTIMIZED_PROTOCOL"
    echo
    
    if [[ -f "/workspace/branches/main/code/$ORIGINAL_PROTOCOL" ]] && [[ -f "/workspace/branches/main/code/$OPTIMIZED_PROTOCOL" ]]; then
        echo "✅ Both protocol versions available for testing"
    else
        echo "⚠️  One or both protocol versions missing"
        [[ ! -f "/workspace/branches/main/code/$ORIGINAL_PROTOCOL" ]] && echo "   Missing: $ORIGINAL_PROTOCOL"
        [[ ! -f "/workspace/branches/main/code/$OPTIMIZED_PROTOCOL" ]] && echo "   Missing: $OPTIMIZED_PROTOCOL"
    fi
    
    echo
    echo "📊 Current session would use: $(get_protocol_version)"
    echo "🔄 To analyze results: .claude/hooks/doc-optimization-analyzer.sh"
}

# Main command dispatcher
case "${1:-recommend}" in
    "recommend")
        recommend_protocol_version "$2" "$3"
        ;;
    "force")
        force_protocol_version "$2" "$3"
        ;;
    "status")
        show_ab_test_status
        ;;
    "version")
        get_protocol_version "$2" "$3"
        ;;
    "file")
        get_protocol_file "$2" "$3"
        ;;
    *)
        echo "Usage: $0 [recommend|force|status|version|file] [task_type] [version]"
        echo
        echo "Commands:"
        echo "  recommend [task_type] [context] - Get A/B test recommendation for task"
        echo "  force [version] [task_type]     - Force specific version (original|optimized)"
        echo "  status                          - Show A/B test configuration status"
        echo "  version [task_type]             - Get selected version name only"
        echo "  file [task_type]                - Get selected protocol file path only"
        echo
        echo "Examples:"
        echo "  $0 recommend feature workflow"
        echo "  $0 force optimized bug-fix"
        echo "  $0 status"
        ;;
esac