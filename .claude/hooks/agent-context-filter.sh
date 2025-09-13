#!/bin/bash

# Agent Context Filter for Hook Relevance
# Determines which hooks should run for which agents to reduce context consumption

# Get current agent context
CURRENT_AGENT_TYPE="${CLAUDE_AGENT_TYPE:-main}"

# If running from another agent context, try to detect it
if [[ -n "${CLAUDE_CURRENT_AGENT:-}" ]]; then
	CURRENT_AGENT_TYPE="$CLAUDE_CURRENT_AGENT"
fi

# Parse from task arguments if available
if [[ -n "${TASK_ARGS:-}" ]]; then
	if echo "$TASK_ARGS" | grep -q 'subagent_type.*build-validator'; then
	    CURRENT_AGENT_TYPE="build-validator"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*tax-auditor'; then
	    CURRENT_AGENT_TYPE="tax-auditor"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*privacy-auditor'; then
	    CURRENT_AGENT_TYPE="privacy-auditor"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*security-auditor'; then
	    CURRENT_AGENT_TYPE="security-auditor"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*statusline-setup'; then
	    CURRENT_AGENT_TYPE="statusline-setup"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*output-style-setup'; then
	    CURRENT_AGENT_TYPE="output-style-setup"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*performance-analyzer'; then
	    CURRENT_AGENT_TYPE="performance-analyzer"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*code-tester'; then
	    CURRENT_AGENT_TYPE="code-tester"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*usability-reviewer'; then
	    CURRENT_AGENT_TYPE="usability-reviewer"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*technical-architect'; then
	    CURRENT_AGENT_TYPE="technical-architect"
	elif echo "$TASK_ARGS" | grep -q 'subagent_type.*code-quality-auditor'; then
	    CURRENT_AGENT_TYPE="code-quality-auditor"
	fi
fi

# Function to check if a hook should run for the current agent
should_run_hook()
{
	local hook_name="$1"
	
	case "$hook_name" in
	    "task-protocol-reminder")
	        # Only main and general-purpose agents need full workflow compliance
	        case "$CURRENT_AGENT_TYPE" in
	            "main"|"general-purpose") return 0 ;;
	            *) return 1 ;;
	        esac
	        ;;
	        
	    "todo-context-reminder")
	        # Only agents that work with todo lists need this
	        case "$CURRENT_AGENT_TYPE" in
	            "main"|"general-purpose") return 0 ;;
	            *) return 1 ;;
	        esac
	        ;;
	        
	    "load-todo")
	        # Only main workflow agents need todo loading
	        case "$CURRENT_AGENT_TYPE" in
	            "main"|"general-purpose") return 0 ;;
	            *) return 1 ;;
	        esac
	        ;;
	        
	    "smart-doc-prompter")
	        # Most agents need documentation, but with different scopes
	        # This hook has its own internal filtering logic
	        return 0
	        ;;
	        
	    "block-data-loss"|"verify-destructive-operations")
	        # All agents that can modify files need these safety checks
	        case "$CURRENT_AGENT_TYPE" in
	            "statusline-setup"|"output-style-setup"|"main"|"general-purpose") return 0 ;;
	            "tax-auditor"|"privacy-auditor"|"usability-reviewer"|"technical-architect") return 1 ;; # Read-only agents
	            *) return 0 ;; # Default to safe side
	        esac
	        ;;
	        
	    "critical-thinking")
	        # Only main agents need broad critical thinking
	        case "$CURRENT_AGENT_TYPE" in
	            "main"|"general-purpose") return 0 ;;
	            *) return 1 ;;
	        esac
	        ;;

	    "checkstyle-guidance")
	        # Java style guidance for code modification agents
	        case "$CURRENT_AGENT_TYPE" in
	            "main"|"general-purpose"|"style-auditor"|"code-quality-auditor") return 0 ;;
	            *) return 1 ;;
	        esac
	        ;;

	    *)
	        # Unknown hook - default to running (safe side)
	        return 0
	        ;;
	esac
}

# Export functions and variables for use by other scripts
export CURRENT_AGENT_TYPE
export -f should_run_hook

# If called directly with hook name, check if it should run
if [[ $# -eq 1 ]]; then
	if should_run_hook "$1"; then
	    exit 0  # Should run
	else
	    exit 1  # Should not run
	fi
fi