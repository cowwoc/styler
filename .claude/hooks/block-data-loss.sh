#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-data-loss.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Data Loss Prevention Hook
# Prevents destructive operations that could corrupt the workspace
#
# TRIGGER: PreToolUse (Bash), UserPromptSubmit
#
# BLOCKED OPERATIONS:
# 1. git init - Repository creation at workspace root
# 2. rm -rf /workspace - Code directory deletion
# 3. rm -rf /workspace/.git - Git repository corruption
# 4. mv /workspace - Moving protected directories
# 5. ln -s to protected directories - Symlink attacks that bypass path checks
#
# PROTECTED DIRECTORIES:
# - /workspace/ - Primary code repository
# - /workspace/.git - Version control data
#
# See: CLAUDE.md § Repository Structure

# Read JSON data from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	# No input available, exit gracefully
	echo '{}'
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Ensure debug log directory exists
mkdir -p /tmp 2>/dev/null || true

# Debug: Log what we received (with timestamp)
echo "$(date): Consolidated hook received: $JSON_INPUT" >> /tmp/consolidated-debug.log 2>/dev/null || true

# Source JSON parsing library
source "${CLAUDE_PROJECT_DIR}/.claude/hooks/lib/json-parser.sh"

# Source helper for proper hook blocking
source /workspace/.claude/scripts/json-output.sh

handle_pre_tool_use()
{
	# Extract tool name and command
	# CRITICAL: Claude Code sends command at .tool_input.command, NOT at root .command
	# Using jq with correct path - extract_json_value only handles root-level keys (M121)
	TOOL_NAME=$(echo "$JSON_INPUT" | jq -r '.tool_name // empty' 2>/dev/null) || TOOL_NAME=""
	COMMAND=$(echo "$JSON_INPUT" | jq -r '.tool_input.command // empty' 2>/dev/null) || COMMAND=""

	# Fallback extraction if jq fails
	if [ -z "$TOOL_NAME" ]; then
	    TOOL_NAME=$(extract_json_value "$JSON_INPUT" "tool_name")
	fi

	if [ -z "$COMMAND" ]; then
	    COMMAND=$(extract_json_value "$JSON_INPUT" "command")
	fi

	# Debug logging
	echo "$(date): Tool: '$TOOL_NAME', Command: '$COMMAND'" >> /tmp/consolidated-debug.log 2>/dev/null || true

	# Only check Bash tool calls
	if [ "$TOOL_NAME" != "Bash" ] && [ "$TOOL_NAME" != "bash" ]; then
	    echo '{}'
	    exit 0
	fi

	# Check for dangerous operations (case insensitive)
	# But exclude syntax checking commands like "bash -n" that reference files containing "git"
	COMMAND_LOWER=$(echo "$COMMAND" | tr '[:upper:]' '[:lower:]')
	
	case "$COMMAND_LOWER" in
	    *"bash -n"*|*"python3 -m py_compile"*)
	        # Allow syntax checking commands
	        ;;
	    *"git init"*|*"git-init"*)
	        # Allow git init in /tmp directories (for tests)
	        if echo "$COMMAND" | grep -qE "cd /tmp|/tmp/"; then
	            echo '{}'
	            exit 0
	        fi
	        echo "⛔ BLOCKED: git init not allowed at workspace root (see CLAUDE.md § Repository Structure)" >&2
	        echo "🚨 REPOSITORY CREATION BLOCKED: NEVER create new git repositories at workspace root" >&2
	        echo "📍 EXISTING REPOSITORY: Use 'cd /workspace' for git operations" >&2
	        echo "📋 FOR COMMITS: Use the existing repository in /workspace/" >&2
	        echo "" >&2
	        echo "🔧 CORRECT USAGE:" >&2
	        echo "   cd /workspace" >&2
	        echo "   git status" >&2
	        echo "   git add ." >&2
	        echo "   git commit -m 'your message'" >&2
	        echo "" >&2
	        echo "❌ **VIOLATION** - Workspace directory MUST remain non-git to prevent worktree isolation conflicts" >&2
	        # Use proper permission system
	        output_hook_block "Blocked: git init not allowed. Use existing repository in /workspace/"
	        exit 0
	        ;;
	    *"rm -rf /workspace"|*"rm -rf /workspace "*|*"rm -rf /workspace/ "*|*"rm -rf /workspace/&&"*|*"rm -rf /workspace/;"*|*"rm /workspace"|*"rm /workspace "*)
	        # Skip if this is a heredoc (the rm pattern is just text content, not a command)
	        if echo "$COMMAND" | grep -q '<<'; then
	            echo '{}'
	            exit 0
	        fi
	        # Skip if this is an echo/printf/cat string (the rm pattern is quoted text)
	        if echo "$COMMAND" | grep -qE '^(echo|printf|cat\s*>)'; then
	            echo '{}'
	            exit 0
	        fi
	        echo "⛔ BLOCKED: Deletion of /workspace directory is not allowed to prevent data loss" >&2
	        echo "🚨 DATA PROTECTION: The code directory must not be deleted" >&2
	        echo "❌ **VIOLATION** - This operation would destroy the entire project" >&2
	        echo "ℹ️  NOTE: To delete files WITHIN /workspace/, use full path: rm /workspace/filename" >&2
	        # Use proper permission system
	        output_hook_block "Blocked: Deletion of /workspace not allowed to prevent data loss."
	        exit 0
	        ;;
	    *"rm -rf /workspace/.git"|*"rm /workspace/.git"|*"rmdir /workspace/.git"*)
	        echo "⛔ BLOCKED: Deletion of .git directory or its contents is not allowed to prevent data loss" >&2
	        echo "🚨 REPOSITORY PROTECTION: The .git directory must not be modified or deleted" >&2
	        echo "❌ **VIOLATION** - This operation would destroy version control history" >&2
	        # Use proper permission system
	        output_hook_block "Blocked: Deletion of /workspace/.git not allowed to prevent data loss."
	        exit 0
	        ;;
	    *"mv /workspace "*|*"mv /workspace/.git "*)
	        echo "⛔ BLOCKED: Moving/renaming protected directories is not allowed to prevent data loss" >&2
	        echo "🚨 DATA PROTECTION: The main workspace and .git directories must not be moved" >&2
	        echo "❌ **VIOLATION** - This operation could break the workspace structure" >&2
	        # Use proper permission system
	        output_hook_block "Blocked: Moving protected directories not allowed to prevent data loss."
	        exit 0
	        ;;
	    *"ln -s /workspace"*|*"ln -s /workspace/.git"*)
	        echo "⛔ BLOCKED: Creating symlinks to protected directories is not allowed to prevent data loss" >&2
	        echo "🚨 SYMLINK ATTACK PREVENTION: Symlinks could be used to bypass directory protection" >&2
	        echo "❌ **VIOLATION** - This operation could enable indirect deletion of protected directories" >&2
	        # Use proper permission system
	        output_hook_block "Blocked: Creating symlinks to protected directories not allowed."
	        exit 0
	        ;;
	esac

	# Allow all other commands
	echo '{}'
	exit 0
}

handle_user_prompt_submit()
{
	# Extract user message from JSON - try multiple possible fields
	USER_PROMPT=$(extract_json_value "$JSON_INPUT" "message")
	
	if [ -z "$USER_PROMPT" ]; then
	    USER_PROMPT=$(extract_json_value "$JSON_INPUT" "user_message")
	fi
	
	if [ -z "$USER_PROMPT" ]; then
	    USER_PROMPT=$(extract_json_value "$JSON_INPUT" "prompt")
	fi
	
	# If still empty, try to extract any text content
	if [ -z "$USER_PROMPT" ]; then
	    USER_PROMPT=$(echo "$JSON_INPUT" | sed -n 's/.*"[^"]*"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)
	fi

	# Debug logging
	echo "$(date): User prompt: '$USER_PROMPT'" >> /tmp/consolidated-debug.log 2>/dev/null || true

	# Convert to lowercase for case-insensitive matching
	USER_PROMPT_LOWER=$(echo "$USER_PROMPT" | tr '[:upper:]' '[:lower:]')

	# Check for prohibited operations - git init and data loss operations
	if echo "$USER_PROMPT_LOWER" | grep -q "git init\|initialize git\|create git repository\|create new repository\|create repository\|new git repo\|git-init"; then
	    echo "⛔ PROMPT WARNING: git repository creation not allowed at workspace root (see CLAUDE.md § Repository Structure)" >&2
	    echo "🚨 REPOSITORY CREATION BLOCKED: NEVER create new git repositories at workspace root" >&2
	    echo "📍 EXISTING REPOSITORY: Use 'cd /workspace' for git operations" >&2
	    echo "📋 FOR COMMITS: Use the existing repository in /workspace/" >&2
	    echo "" >&2
	    echo "🔧 CORRECT USAGE:" >&2
	    echo "   cd /workspace" >&2
	    echo "   git status" >&2
	    echo "   git add ." >&2
	    echo "   git commit -m 'your message'" >&2
	    echo "" >&2
	    echo "❌ **VIOLATION** - Workspace directory MUST remain non-git to prevent worktree isolation conflicts" >&2

	    # For UserPromptSubmit, we warn but don't block (exit 0)
	    # Change to exit 2 if you want to block the prompt entirely
	    exit 0
	fi

	# Check for dangerous deletion operations
	if echo "$USER_PROMPT_LOWER" | grep -q "delete.*workspace/main\|remove.*workspace/main\|rm.*workspace/main"; then
	    echo "⛔ PROMPT WARNING: Deletion of /workspace is not allowed to prevent data loss" >&2
	    echo "🚨 DATA PROTECTION: The main workspace directory must not be deleted" >&2
	    echo "❌ **VIOLATION** - This operation would destroy the entire workspace" >&2
	    exit 0
	fi

	if echo "$USER_PROMPT_LOWER" | grep -q "delete.*\.git\|remove.*\.git\|rm.*\.git"; then
	    echo "⛔ PROMPT WARNING: Deletion of .git directory or its contents is not allowed to prevent data loss" >&2
	    echo "🚨 REPOSITORY PROTECTION: The .git directory must not be modified or deleted" >&2
	    echo "❌ **VIOLATION** - This operation would destroy version control history" >&2
	    exit 0
	fi

	# Check if user is asking to commit and we're not in a git repository
	if echo "$USER_PROMPT_LOWER" | grep -q "commit\|git.*add\|git.*status"; then
	    if [ ! -d ".git" ] && [ ! -d "/workspace/.git" ]; then
	        echo "📍 GIT OPERATIONS GUIDE:" >&2
	        echo "   Repository location: /workspace/" >&2
	        echo "   Command: cd /workspace && git status" >&2
	        echo "🚨 REMINDER: Never use 'git init' at workspace root" >&2
	    fi
	fi

	# Allow all other prompts
	exit 0
}

# Extract hook event type - try multiple methods
HOOK_EVENT=""

# Method 1: Direct extraction
HOOK_EVENT=$(extract_json_value "$JSON_INPUT" "hook_event_name")

# Method 2: Alternative field names
if [ -z "$HOOK_EVENT" ]; then
	HOOK_EVENT=$(extract_json_value "$JSON_INPUT" "event")
fi

if [ -z "$HOOK_EVENT" ]; then
	HOOK_EVENT=$(extract_json_value "$JSON_INPUT" "event_name")
fi

# Method 3: Pattern matching
if [ -z "$HOOK_EVENT" ]; then
	if echo "$JSON_INPUT" | grep -q "PreToolUse"; then
	    HOOK_EVENT="PreToolUse"
	elif echo "$JSON_INPUT" | grep -q "UserPromptSubmit"; then
	    HOOK_EVENT="UserPromptSubmit"
	fi
fi

# Debug logging
echo "$(date): Hook event: '$HOOK_EVENT'" >> /tmp/consolidated-debug.log 2>/dev/null || true

# Dispatch based on hook event type
case "$HOOK_EVENT" in
	"PreToolUse")
	    handle_pre_tool_use
	    ;;
	"UserPromptSubmit")
	    handle_user_prompt_submit
	    ;;
	*)
	    # Unknown event or no event detected, allow it
	    echo "$(date): Unknown or missing hook event: '$HOOK_EVENT'" >> /tmp/consolidated-debug.log 2>/dev/null || true
	    echo '{}'
	    exit 0
	    ;;
esac
