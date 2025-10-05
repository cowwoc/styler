#!/bin/bash

# Consolidated Smart Documentation Prompter
# Handles all context-aware documentation prompting with session tracking
# Combines UserPromptSubmit and PreToolUse logic into one efficient hook

# Source metrics functions for documentation tracking
source /workspace/branches/main/code/.claude/hooks/metrics-capture.sh 2>/dev/null || true

HOOK_EVENT="$1"
HOOK_DATA="$2"
TOOL_ARGS="$3"
SESSION_ID="${CLAUDE_SESSION_ID:-default}"

# Detect current agent type context
CURRENT_AGENT_TYPE="main"

# Method 1: Check environment variables (if set by Task tool)
if [[ -n "${CLAUDE_AGENT_TYPE:-}" ]]; then
	CURRENT_AGENT_TYPE="$CLAUDE_AGENT_TYPE"
elif [[ -n "${CLAUDE_CURRENT_AGENT:-}" ]]; then
	CURRENT_AGENT_TYPE="$CLAUDE_CURRENT_AGENT"
fi

# Method 2: Parse from tool arguments to detect which agent is being used
if [[ "$HOOK_EVENT" == "PreToolUse" && "$HOOK_DATA" == "Task" && -n "$TOOL_ARGS" ]]; then
	if echo "$TOOL_ARGS" | grep -q 'build-validator'; then
	    CURRENT_AGENT_TYPE="build-validator"
	elif echo "$TOOL_ARGS" | grep -q 'code-quality-auditor'; then
	    CURRENT_AGENT_TYPE="code-quality-auditor"
	elif echo "$TOOL_ARGS" | grep -q 'security-auditor'; then
	    CURRENT_AGENT_TYPE="security-auditor"
	elif echo "$TOOL_ARGS" | grep -q 'performance-analyzer'; then
	    CURRENT_AGENT_TYPE="performance-analyzer"
	elif echo "$TOOL_ARGS" | grep -q 'code-tester'; then
	    CURRENT_AGENT_TYPE="code-tester"
	elif echo "$TOOL_ARGS" | grep -q 'usability-reviewer'; then
	    CURRENT_AGENT_TYPE="usability-reviewer"
	fi
fi

# Create session tracking directory
SESSION_TRACK_DIR="/tmp/claude-hooks-session-${SESSION_ID}"
mkdir -p "$SESSION_TRACK_DIR" 2>/dev/null || {
	echo "Warning: Failed to create session tracking directory" >&2
	exit 0
}

# Function to check and mark prompts per agent type to avoid duplicates
check_and_mark_prompt()
{
	local prompt_type="$1"
	local prompt_file="$SESSION_TRACK_DIR/${CURRENT_AGENT_TYPE}-${prompt_type}"
	
	if [[ -f "$prompt_file" ]]; then
	    return 1  # Already prompted this agent type for this prompt type
	fi
	
	touch "$prompt_file" 2>/dev/null || return 1
	return 0  # First time prompting this agent type for this prompt type
}

# Handle UserPromptSubmit events
handle_user_prompt_submit()
{
	local user_message="$1"
	local user_message_lower=$(echo "$user_message" | tr '[:upper:]' '[:lower:]')
	
	# Only trigger on action verbs indicating Claude should DO something
	local action_verbs="update|fix|refactor|create|implement|add|modify|change|write|edit|build|test|validate"
	
	# Only proceed if user is requesting action
	if ! echo "$user_message_lower" | grep -qE "$action_verbs"; then
	    return 0
	fi
	
	# Agent-specific filtering: Skip workflow/style prompts for specialized agents
	case "$CURRENT_AGENT_TYPE" in
	    "security-auditor"|"usability-reviewer")
	        # Read-only analysis agents - skip code style/workflow prompts
	        return 0
	        ;;
	    "statusline-setup"|"output-style-setup")
	        # Simple config agents - only need minimal docs
	        # Skip most style guides except basic ones
	        ;;
	esac
	
	# Code style work patterns (skip for simple config agents)
	if echo "$user_message_lower" | grep -qE "(update|fix|refactor).*(style|format|brace|indent|method.chain|dot)" && 
	   [[ ! "$CURRENT_AGENT_TYPE" =~ ^(statusline-setup|output-style-setup)$ ]]; then
	    if check_and_mark_prompt "style-work"; then
	        # Track traditional code style documentation access
	        track_documentation_access "docs/code-style-human.md" "reference" "style-work" 1 2>/dev/null || true
	        
	        echo "📋 STYLE WORK: Choose documentation approach:"
	        echo "  • CLAUDE: ./docs/code-style/common-claude.md + language-specific *-claude.md - Detection patterns"  
	        echo "  • HUMAN: ./docs/code-style-human.md - Hub with explanations"
	        
	        # Track the new Claude-optimized documentation as an option
	        track_documentation_access "docs/code-style/common-claude.md" "option" "style-work" 0 2>/dev/null || true
	    fi
	elif echo "$user_message_lower" | grep -qE "(add|implement|fix).*(validation|error.handling|exception|requirethat|checkif)"; then
	    if check_and_mark_prompt "validation-work"; then
	        # Track validation work
	        track_documentation_access "docs/code-style/common-claude.md" "reference" "validation-work" 1 2>/dev/null || true
	        
	        echo "🚨 VALIDATION WORK: Use consolidated approach:"
	        echo "  • CLAUDE: ./docs/code-style/common-claude.md - Validation detection patterns"
	        echo "  • HUMAN: ./docs/code-style/common-human.md - Validation explanations"
	    fi
	elif echo "$user_message_lower" | grep -qE "(update|create|modify).*(\.mts|\.ts|typescript|enum|interface)"; then
	    if check_and_mark_prompt "typescript-work"; then
	        # Track TypeScript work
	        track_documentation_access "docs/code-style/typescript-claude.md" "reference" "typescript-work" 1 2>/dev/null || true
	        
	        echo "📘 TYPESCRIPT WORK: Use consolidated approach:"
	        echo "  • CLAUDE: ./docs/code-style/typescript-claude.md - TypeScript detection patterns"
	        echo "  • HUMAN: ./docs/code-style/typescript-human.md - TypeScript explanations"
	    fi
	elif echo "$user_message_lower" | grep -qE "(update|create|modify).*(\.java|java.class)"; then
	    if check_and_mark_prompt "java-work"; then
	        # Track Java work
	        track_documentation_access "docs/code-style/java-claude.md" "reference" "java-work" 1 2>/dev/null || true
	        
	        echo "☕ JAVA WORK: Use consolidated approach:"
	        echo "  • CLAUDE: ./docs/code-style/java-claude.md - Java detection patterns"
	        echo "  • HUMAN: ./docs/code-style/java-human.md - Java explanations"
	    fi
	elif echo "$user_message_lower" | grep -qE "(create|add|update).*test|(test|testing)"; then
	    if check_and_mark_prompt "test-work"; then
	        # Track testing work
	        track_documentation_access "docs/code-style/testing-claude.md" "reference" "test-work" 1 2>/dev/null || true
	        
	        echo "🧪 TEST WORK: Use consolidated testing approach:"
	        echo "  • CLAUDE: ./docs/code-style/testing-claude.md - Testing detection patterns"
	        echo "  • HUMAN: ./docs/code-style/testing-human.md - Testing explanations and JPMS structure"
	    fi
	elif echo "$user_message_lower" | grep -qE "(update|fix|modify).*(pom\.xml|maven|build|dependency)"; then
	    if check_and_mark_prompt "build-work"; then
	        echo "🔧 BUILD WORK: Before Maven changes, read only the dependency management section from ./docs/project/build-system.md."
	    fi
	fi
	
	# Project context patterns
	if echo "$user_message_lower" | grep -qE "scope|architecture|constraint|requirement|project.goal|business.rule|parser|formatter|styler"; then
	    if check_and_mark_prompt "project-scope"; then
	        echo "🎯 PROJECT SCOPE TASK: Before proceeding, read ./docs/project/scope.md to understand project constraints, architecture, and business requirements."
	    fi
	elif echo "$user_message_lower" | grep -qE "workflow|process|stage|stakeholder|agent|task.protocol|worktree|isolation"; then
	    if check_and_mark_prompt "project-workflow"; then
	        echo "⚡ WORKFLOW TASK: Before proceeding, read ./docs/project/task-protocol.md to understand the mandatory 10-stage development process and task protocol."
	    fi
	elif echo "$user_message_lower" | grep -qE "ast|parsing|formatting|token|trivia|syntax.tree"; then
	    if check_and_mark_prompt "project-parser"; then
	        echo "🔧 PARSER TASK: Before proceeding, read ./docs/studies/java-parser-architectures.md to understand AST parsing and formatting strategies."
	    fi
	elif echo "$user_message_lower" | grep -qE "critical.rule|build.integrity|compliance|violation|mandatory"; then
	    if check_and_mark_prompt "project-critical-rules"; then
	        echo "🚨 CRITICAL RULES TASK: Before proceeding, read ./docs/project/critical-rules.md for build integrity requirements and compliance standards."
	    fi
	elif echo "$user_message_lower" | grep -qE "prohibited|forbidden|not.allowed|out.of.scope|restricted"; then
	    if check_and_mark_prompt "project-out-of-scope"; then
	        echo "❌ BAD - SCOPE LIMITATION: Before proceeding, read ./docs/project/out_of_scope.md to understand prohibited technologies and approaches."
	    fi
	fi
}

# Handle PreToolUse events
handle_pre_tool_use()
{
	local tool_name="$1"
	local tool_args="$2"
	
	case "$tool_name" in
	    "Edit"|"Write"|"MultiEdit")
	        # File-specific prompts
	        if echo "$tool_args" | grep -q '\.mts\|\.ts'; then
	            if check_and_mark_prompt "file-typescript"; then
	                echo "📘 JUST-IN-TIME [$CURRENT_AGENT_TYPE]: You're about to modify TypeScript. Quick read: ./docs/code-style/typescript-claude.md"
	            fi
	        elif echo "$tool_args" | grep -q '\.java'; then
	            if check_and_mark_prompt "file-java"; then
	                echo "☕ JUST-IN-TIME [$CURRENT_AGENT_TYPE]: You're about to modify Java. Quick read: ./docs/code-style/java-claude.md"
	            fi
	        elif echo "$tool_args" | grep -q 'pom\.xml'; then
	            if check_and_mark_prompt "file-pom"; then
	                echo "🔧 JUST-IN-TIME [$CURRENT_AGENT_TYPE]: You're about to modify pom.xml. Quick read: ./docs/project/build-system.md (Maven dependency management section only)"
	            fi
	        elif echo "$tool_args" | grep -qE 'Test\.java|test.*\.java'; then
	            if check_and_mark_prompt "file-test"; then
	                echo "🧪 JUST-IN-TIME [$CURRENT_AGENT_TYPE]: You're about to modify tests. Quick read: ./docs/code-style/testing-claude.md"
	            fi
	        fi
	        ;;
	    "Task")
	        # Agent-specific prompts
	        if echo "$tool_args" | grep -q 'code-quality-auditor'; then
	            if check_and_mark_prompt "agent-code-quality-auditor"; then
	                echo "🔍 AGENT PROMPT: Using code-quality-auditor. Quick read: ./docs/code-style/common-claude.md (validation patterns)"
	            fi
	        elif echo "$tool_args" | grep -q 'build-validator'; then
	            if check_and_mark_prompt "agent-build-validator"; then
	                echo "🏗️ AGENT PROMPT: Using build-validator. Quick read: ./docs/project/build-system.md (build commands section only)"
	            fi
	        elif echo "$tool_args" | grep -q 'security-auditor'; then
	            if check_and_mark_prompt "agent-security-auditor"; then
	                echo "🛡️ AGENT PROMPT: Using security-auditor. Quick read: ./docs/project/scope.md (security requirements section)"
	            fi
	        elif echo "$tool_args" | grep -q 'performance-analyzer'; then
	            if check_and_mark_prompt "agent-performance-analyzer"; then
	                echo "⚡ AGENT PROMPT: Using performance-analyzer. Quick read: ./docs/project/scope.md (performance requirements section)"
	            fi
	        elif echo "$tool_args" | grep -q 'code-tester'; then
	            if check_and_mark_prompt "agent-code-tester"; then
	                echo "🧪 AGENT PROMPT: Using code-tester. Quick read: ./docs/code-style/testing-claude.md (testing detection patterns)"
	            fi
	        elif echo "$tool_args" | grep -q 'technical-architect'; then
	            if check_and_mark_prompt "agent-technical-architect"; then
	                echo "🏛️ AGENT PROMPT: Using technical-architect. Quick read: ./docs/project/scope.md (architecture guidelines section)"
	            fi
	        elif echo "$tool_args" | grep -q 'usability-reviewer'; then
	            if check_and_mark_prompt "agent-usability-reviewer"; then
	                echo "👥 AGENT PROMPT: Using usability-reviewer. Quick read: ./docs/project/scope.md (user experience requirements)"
	            fi
	        fi
	        ;;
	esac
}

# Main logic dispatcher based on hook event
case "$HOOK_EVENT" in
	"UserPromptSubmit")
	    handle_user_prompt_submit "$HOOK_DATA"
	    ;;
	"PreToolUse")
	    handle_pre_tool_use "$HOOK_DATA" "$TOOL_ARGS"
	    ;;
esac

exit 0