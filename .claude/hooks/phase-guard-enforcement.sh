#!/bin/bash
set -euo pipefail

# Phase Guard Enforcement Hook
# Runs on every user prompt to check for "continue" commands and enforce task protocol phase progression
# Prevents Claude from skipping protocol phases when continuing work

# Get the user prompt from environment or stdin
USER_PROMPT="${CLAUDE_USER_PROMPT:-$(cat 2>/dev/null || echo '')}"

# Patterns that indicate continuing work
CONTINUE_PATTERNS=(
    "continue"
    "keep going"
    "proceed"
    "next"
    "move on"
    "go ahead"
    "carry on"
    "resume"
    "finish"
    "complete"
    "implement"
    "work on"
    "start"
    "begin"
)

# Check if prompt matches continue patterns (case insensitive)
is_continue_command() {
    local prompt="$1"
    local lower_prompt=$(echo "$prompt" | tr '[:upper:]' '[:lower:]')

    for pattern in "${CONTINUE_PATTERNS[@]}"; do
        if [[ "$lower_prompt" =~ $pattern ]]; then
            return 0
        fi
    done
    return 1
}

# Check if we're in a task-specific worktree (indicates task protocol in progress)
is_task_protocol_active() {
    local current_dir=$(pwd)
    # Check if we're in a task-specific branch/worktree
    if [[ "$current_dir" =~ /workspace/branches/[^/]+/code$ ]]; then
        return 0
    fi
    # Check for task protocol markers
    if [[ -f "../context.md" ]] || ls ../*-requirements.md >/dev/null 2>&1; then
        return 0
    fi
    return 1
}

# Get current task protocol phase based on existing files
get_current_protocol_phase() {
    local phase=0

    # Phase 1: Requirements Analysis - check for requirements files
    if ls ../*-requirements.md >/dev/null 2>&1; then
        phase=1
    fi

    # Phase 2: Requirements Synthesis - check for synthesis file
    if [[ -f "../requirements-synthesis.md" ]]; then
        phase=2
    fi

    # Phase 3: Technical Design - check for technical design files
    if ls ../*-technical-design*.md >/dev/null 2>&1 || [[ -f "../technical-design.md" ]]; then
        phase=3
    fi

    # Phase 4: Implementation Planning - check for implementation plan
    if [[ -f "../implementation-plan.md" ]] || ls ../*-implementation-plan*.md >/dev/null 2>&1; then
        phase=4
    fi

    # Phase 5: Implementation - check for actual code changes (only src/main, not tests)
    if git status --porcelain 2>/dev/null | grep -q "^[AM]" || [[ -d "src/main" && $(find src/main -name "*.java" -newer ../context.md 2>/dev/null | wc -l) -gt 0 ]]; then
        phase=5
    fi

    # Phase 6: Stakeholder Review - check for review files
    if ls ../*-review*.md >/dev/null 2>&1; then
        phase=6
    fi

    # Phase 7: Finalization - check for completion markers
    if [[ -f "../task-complete.md" ]] || grep -q "COMPLETED" ../context.md 2>/dev/null; then
        phase=7
    fi

    echo $phase
}

# Get next required phase based on current phase
get_next_required_phase() {
    local current_phase=$1
    case $current_phase in
        0) echo "1" ;;
        1) echo "2" ;;
        2) echo "3" ;;
        3) echo "4" ;;
        4) echo "5" ;;
        5) echo "6" ;;
        6) echo "7" ;;
        7) echo "complete" ;;
        *) echo "1" ;;
    esac
}

# Generate phase completion requirements
get_phase_requirements() {
    local phase=$1
    case $phase in
        1) echo "Phase 1: Requirements Analysis - Create stakeholder requirements files (technical-architect-requirements.md, security-auditor-requirements.md, etc.)" ;;
        2) echo "Phase 2: Requirements Synthesis - Create requirements-synthesis.md combining all stakeholder inputs" ;;
        3) echo "Phase 3: Technical Design - Create detailed technical design specification" ;;
        4) echo "Phase 4: Implementation Planning - Create implementation-plan.md with task breakdown and timeline" ;;
        5) echo "Phase 5: Implementation - Write actual code, tests, and implementation files" ;;
        6) echo "Phase 6: Stakeholder Review - Run stakeholder agents for final approval (technical-architect, security-auditor, etc.)" ;;
        7) echo "Phase 7: Finalization - Clean up worktree, update main todo.md, create completion documentation" ;;
        *) echo "Phase 1: Requirements Analysis - Start with stakeholder requirements gathering" ;;
    esac
}

# Main logic
main() {
    # Only run if this looks like a continue command
    if ! is_continue_command "$USER_PROMPT"; then
        exit 0
    fi

    # Only run if task protocol appears to be active
    if ! is_task_protocol_active; then
        exit 0
    fi

    local current_phase=$(get_current_protocol_phase)
    local next_phase=$(get_next_required_phase $current_phase)
    local current_requirements=$(get_phase_requirements $current_phase)
    local next_requirements=$(get_phase_requirements $next_phase)

    # Generate enforcement message
    echo '{
        "hookSpecificOutput": {
            "hookEventName": "UserPromptSubmit",
            "additionalContext": "🚨 TASK PROTOCOL PHASE GUARD ENFORCEMENT: Detected continue command while task protocol is active. MANDATORY PHASE VALIDATION REQUIRED:\n\n📍 CURRENT PHASE: Phase '$current_phase' - '$current_requirements'\n\n🔍 BEFORE CONTINUING: You MUST verify current phase is complete by checking for required files and deliverables.\n\n➡️ NEXT REQUIRED PHASE: '$next_phase' - '$next_requirements'\n\n⚠️ CRITICAL: Do NOT skip phases or proceed to implementation without completing all prior phases. Check docs/project/task-protocol-core.md and task-protocol-operations.md for complete phase requirements.\n\n🔒 PHASE GUARD: Only proceed if current phase deliverables exist and are complete. If unsure, review existing files in ../directory to verify phase completion status."
        }
    }'
}

main "$@"