#!/bin/bash
set -euo pipefail

# Identify script path for error messages
SCRIPT_PATH="${BASH_SOURCE[0]}"

# Fail gracefully without blocking Claude Code
trap 'echo "[HOOK DEBUG] smart-doc-prompter.sh FAILED at line $LINENO" >&2; echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Unexpected error at line $LINENO" >&2; exit 0' ERR

echo "[HOOK DEBUG] smart-doc-prompter.sh START" >&2

# Consolidated Smart Documentation Prompter
# Handles all context-aware documentation prompting with session tracking
# Combines UserPromptSubmit and PreToolUse logic into one efficient hook

# Read JSON data from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	# No input available - this is a configuration error
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Expected JSON on stdin but none provided" >&2
	echo "   This hook must receive JSON data via stdin from Claude Code" >&2
	exit 0  # Non-blocking exit
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Check if JSON input is empty
if [[ -z "$JSON_INPUT" ]]; then
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Empty JSON input received" >&2
	echo "   Expected JSON structure with hook_event_name field" >&2
	exit 0  # Non-blocking exit
fi

# Simple JSON value extraction without jq dependency
extract_json_value()
{
	local json="$1"
	local key="$2"
	# Use grep and sed for basic JSON parsing (allow grep to fail without triggering set -e)
	echo "$json" | grep -o "\"$key\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | sed "s/\"$key\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\"/\1/" || true
}

# Extract hook event type
HOOK_EVENT=$(extract_json_value "$JSON_INPUT" "hook_event_name")

# Check if hook event was extracted
if [[ -z "$HOOK_EVENT" ]]; then
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: Could not extract hook_event_name from JSON" >&2
	echo "   JSON received: ${JSON_INPUT:0:200}..." >&2
	exit 0  # Non-blocking exit
fi

# Extract session ID from JSON input
SESSION_ID=$(extract_json_value "$JSON_INPUT" "session_id")

# Require session ID - fail fast if not provided
if [ -z "$SESSION_ID" ]; then
	echo "⚠️  HOOK ERROR [$SCRIPT_PATH]: session_id must be provided in hook JSON input" >&2
	echo "   This hook requires a session ID for tracking" >&2
	exit 0  # Non-blocking exit
fi

# Detect current agent type context
CURRENT_AGENT_TYPE="main"

# Method 1: Check environment variables (if set by Task tool)
if [[ -n "${CLAUDE_AGENT_TYPE:-}" ]]; then
	CURRENT_AGENT_TYPE="$CLAUDE_AGENT_TYPE"
elif [[ -n "${CLAUDE_CURRENT_AGENT:-}" ]]; then
	CURRENT_AGENT_TYPE="$CLAUDE_CURRENT_AGENT"
fi

# Create session tracking directory
SESSION_TRACK_DIR="/tmp/claude-hooks-session-${SESSION_ID}"
mkdir -p "$SESSION_TRACK_DIR" 2>/dev/null || {
	echo "⚠️  HOOK WARNING [$SCRIPT_PATH]: Failed to create session tracking directory" >&2
	exit 0  # Non-blocking exit
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
	# Extract user message - try multiple possible fields
	local user_message=$(extract_json_value "$JSON_INPUT" "message")

	if [[ -z "$user_message" ]]; then
		user_message=$(extract_json_value "$JSON_INPUT" "user_message")
	fi

	if [[ -z "$user_message" ]]; then
		user_message=$(extract_json_value "$JSON_INPUT" "prompt")
	fi

	if [[ -z "$user_message" ]]; then
		echo "⚠️  HOOK WARNING [$SCRIPT_PATH]: Could not extract user message from UserPromptSubmit JSON" >&2
		return 0
	fi

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
			echo "📋 STYLE WORK: Choose documentation approach:"
			echo "  • CLAUDE: ./docs/code-style/common-claude.md + language-specific *-claude.md - Detection patterns"
			echo "  • HUMAN: ./docs/code-style-human.md - Hub with explanations"
		fi
	elif echo "$user_message_lower" | grep -qE "(add|implement|fix).*(validation|error.handling|exception|requirethat|checkif)"; then
		if check_and_mark_prompt "validation-work"; then
			echo "🚨 VALIDATION WORK: Use consolidated approach:"
			echo "  • CLAUDE: ./docs/code-style/common-claude.md - Validation detection patterns"
			echo "  • HUMAN: ./docs/code-style/common-human.md - Validation explanations"
		fi
	elif echo "$user_message_lower" | grep -qE "(update|create|modify).*(\.mts|\.ts|typescript|enum|interface)"; then
		if check_and_mark_prompt "typescript-work"; then
			echo "📘 TYPESCRIPT WORK: Use consolidated approach:"
			echo "  • CLAUDE: ./docs/code-style/typescript-claude.md - TypeScript detection patterns"
			echo "  • HUMAN: ./docs/code-style/typescript-human.md - TypeScript explanations"
		fi
	elif echo "$user_message_lower" | grep -qE "(update|create|modify).*(\.java|java.class)"; then
		if check_and_mark_prompt "java-work"; then
			echo "☕ JAVA WORK: Use consolidated approach:"
			echo "  • CLAUDE: ./docs/code-style/java-claude.md - Java detection patterns"
			echo "  • HUMAN: ./docs/code-style/java-human.md - Java explanations"
		fi
	elif echo "$user_message_lower" | grep -qE "(apply|check|validate|verify).*(style|format|guide|compliance)"; then
		if check_and_mark_prompt "complete-style-validation"; then
			echo "🎯 COMPLETE STYLE VALIDATION: ALL THREE components required:"
			echo "  1. checkstyle: ./mvnw checkstyle:check"
			echo "  2. PMD: ./mvnw pmd:check"
			echo "  3. Manual rules: docs/code-style/*-claude.md detection patterns"
			echo ""
			echo "📋 CRITICAL: Never assume checkstyle-only. See task-protocol-core.md 'Complete Style Validation Gate'"
		fi
	elif echo "$user_message_lower" | grep -qE "(create|add|update).*test|(test|testing)"; then
		if check_and_mark_prompt "test-work"; then
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
			echo "⚡ WORKFLOW TASK: Before proceeding, read ./docs/project/task-protocol-core.md to understand the mandatory 10-stage development process and task protocol."
		fi
	elif echo "$user_message_lower" | grep -qE "ast|parsing|formatting|token|trivia|syntax.tree"; then
		if check_and_mark_prompt "project-parser"; then
			echo "🔧 PARSER TASK: Before proceeding, read ./docs/studies/java-parser-architectures.md to understand AST parsing and formatting strategies."
		fi
	elif echo "$user_message_lower" | grep -qE "critical.rule|build.integrity|compliance|violation|mandatory"; then
		if check_and_mark_prompt "project-critical-rules"; then
			echo "🚨 CRITICAL RULES TASK: Before proceeding, read CLAUDE.md (§ Lock Ownership, § Worktree Isolation, § Task Protocol Summary) for build integrity requirements and compliance standards."
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
	# Extract tool name
	local tool_name=$(extract_json_value "$JSON_INPUT" "tool_name")

	if [[ -z "$tool_name" ]]; then
		echo "⚠️  HOOK WARNING [$SCRIPT_PATH]: Could not extract tool_name from PreToolUse JSON" >&2
		return 0
	fi

	case "$tool_name" in
		"Edit"|"Write"|"MultiEdit")
			# Extract file path from JSON
			local file_path=$(extract_json_value "$JSON_INPUT" "file_path")
			local path=$(extract_json_value "$JSON_INPUT" "path")

			# Use whichever field is populated
			[[ -z "$file_path" ]] && file_path="$path"
			[[ -z "$file_path" ]] && return 0

			# File-specific prompts
			if echo "$file_path" | grep -q '\.mts\|\.ts'; then
				if check_and_mark_prompt "file-typescript"; then
					echo "📘 JUST-IN-TIME [$CURRENT_AGENT_TYPE]: You're about to modify TypeScript. Quick read: ./docs/code-style/typescript-claude.md"
				fi
			elif echo "$file_path" | grep -q '\.java'; then
				if check_and_mark_prompt "file-java"; then
					echo "☕ JUST-IN-TIME [$CURRENT_AGENT_TYPE]: You're about to modify Java. Quick read: ./docs/code-style/java-claude.md"
				fi
			elif echo "$file_path" | grep -q 'pom\.xml'; then
				if check_and_mark_prompt "file-pom"; then
					echo "🔧 JUST-IN-TIME [$CURRENT_AGENT_TYPE]: You're about to modify pom.xml. Quick read: ./docs/project/build-system.md (Maven dependency management section only)"
				fi
			elif echo "$file_path" | grep -qE 'Test\.java|test.*\.java'; then
				if check_and_mark_prompt "file-test"; then
					echo "🧪 JUST-IN-TIME [$CURRENT_AGENT_TYPE]: You're about to modify tests. Quick read: ./docs/code-style/testing-claude.md"
				fi
			fi
			;;
		"Task")
			# Check for agent type in tool arguments by parsing JSON
			# Try to detect subagent_type field
			local subagent_type=$(extract_json_value "$JSON_INPUT" "subagent_type")

			if [[ -n "$subagent_type" ]]; then
				case "$subagent_type" in
					*"code-quality-auditor"*)
						if check_and_mark_prompt "agent-code-quality-auditor"; then
							echo "🔍 AGENT PROMPT: Using code-quality-auditor. Quick read: ./docs/code-style/common-claude.md (validation patterns)"
						fi
						;;
					*"build-validator"*)
						if check_and_mark_prompt "agent-build-validator"; then
							echo "🏗️ AGENT PROMPT: Using build-validator. Quick read: ./docs/project/build-system.md (build commands section only)"
						fi
						;;
					*"security-auditor"*)
						if check_and_mark_prompt "agent-security-auditor"; then
							echo "🛡️ AGENT PROMPT: Using security-auditor. Quick read: ./docs/project/scope.md (security requirements section)"
						fi
						;;
					*"performance-analyzer"*)
						if check_and_mark_prompt "agent-performance-analyzer"; then
							echo "⚡ AGENT PROMPT: Using performance-analyzer. Quick read: ./docs/project/scope.md (performance requirements section)"
						fi
						;;
					*"code-tester"*)
						if check_and_mark_prompt "agent-code-tester"; then
							echo "🧪 AGENT PROMPT: Using code-tester. Quick read: ./docs/code-style/testing-claude.md (testing detection patterns)"
						fi
						;;
					*"technical-architect"*)
						if check_and_mark_prompt "agent-technical-architect"; then
							echo "🏛️ AGENT PROMPT: Using technical-architect. Quick read: ./docs/project/scope.md (architecture guidelines section)"
						fi
						;;
					*"usability-reviewer"*)
						if check_and_mark_prompt "agent-usability-reviewer"; then
							echo "👥 AGENT PROMPT: Using usability-reviewer. Quick read: ./docs/project/scope.md (user experience requirements)"
						fi
						;;
				esac
			fi
			;;
	esac
}

# Main logic dispatcher based on hook event type
case "$HOOK_EVENT" in
	"UserPromptSubmit")
		handle_user_prompt_submit
		;;
	"PreToolUse")
		handle_pre_tool_use
		;;
	*)
		echo "⚠️  HOOK WARNING [$SCRIPT_PATH]: Unknown hook event type: $HOOK_EVENT" >&2
		;;
esac

echo "[HOOK DEBUG] smart-doc-prompter.sh END" >&2

exit 0
