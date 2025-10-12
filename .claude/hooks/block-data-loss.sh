#!/bin/sh

# Git Init Blocker Hook - Fixed version for both PreToolUse and UserPromptSubmit
# Prevents git repository creation per task-protocol.md
# Handles both actual command execution blocking and user prompt warnings

# Read JSON data from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	# No input available, exit gracefully
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Ensure debug log directory exists
mkdir -p /tmp 2>/dev/null || true

# Debug: Log what we received (with timestamp)
echo "$(date): Consolidated hook received: $JSON_INPUT" >> /tmp/consolidated-debug.log 2>/dev/null || true

# Simple JSON value extraction without jq dependency
extract_json_value()
{
	local json="$1"
	local key="$2"
	# Use grep and sed for basic JSON parsing
	echo "$json" | grep -o "\"$key\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | sed "s/\"$key\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\"/\1/"
}

handle_pre_tool_use()
{
	# Extract tool name and command
	TOOL_NAME=$(extract_json_value "$JSON_INPUT" "tool_name")
	COMMAND=$(extract_json_value "$JSON_INPUT" "command")
	
	# If extraction failed, try alternative method
	if [ -z "$TOOL_NAME" ]; then
	    TOOL_NAME=$(echo "$JSON_INPUT" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
	fi
	
	if [ -z "$COMMAND" ]; then
	    COMMAND=$(echo "$JSON_INPUT" | sed -n 's/.*"command"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
	fi

	# Debug logging
	echo "$(date): Tool: '$TOOL_NAME', Command: '$COMMAND'" >> /tmp/consolidated-debug.log 2>/dev/null || true

	# Only check Bash tool calls
	if [ "$TOOL_NAME" != "Bash" ] && [ "$TOOL_NAME" != "bash" ]; then
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
	        echo "⛔ BLOCKED: git init not allowed at workspace root (see task-protocol.md line 931)" >&2
	        echo "🚨 REPOSITORY CREATION BLOCKED: Per task-protocol.md, NEVER create new git repositories at workspace root" >&2
	        echo "📍 EXISTING REPOSITORY: Use 'cd /workspace/branches/main/code' for git operations" >&2
	        echo "📋 FOR COMMITS: Use the existing repository in /workspace/branches/main/code/" >&2
	        echo "" >&2
	        echo "🔧 CORRECT USAGE:" >&2
	        echo "   cd /workspace/branches/main/code" >&2
	        echo "   git status" >&2
	        echo "   git add ." >&2
	        echo "   git commit -m 'your message'" >&2
	        echo "" >&2
	        echo "❌ **VIOLATION** - Workspace directory MUST remain non-git to prevent worktree isolation conflicts" >&2
	        exit 2
	        ;;
	    *"rm -rf /workspace/branches/main"*|*"rm /workspace/branches/main"*|*"rmdir /workspace/branches/main"*)
	        echo "⛔ BLOCKED: Deletion of /workspace/branches/main is not allowed to prevent data loss" >&2
	        echo "🚨 DATA PROTECTION: The main workspace directory must not be deleted" >&2
	        echo "❌ **VIOLATION** - This operation would destroy the entire workspace" >&2
	        exit 2
	        ;;
	    *"rm -rf /workspace/branches/main/code"*|*"rm /workspace/branches/main/code"*)
	        echo "⛔ BLOCKED: Deletion of /workspace/branches/main/code is not allowed to prevent data loss" >&2
	        echo "🚨 DATA PROTECTION: The code directory must not be deleted" >&2
	        echo "❌ **VIOLATION** - This operation would destroy the entire project" >&2
	        exit 2
	        ;;
	    *"rm -rf /workspace/branches/main/.git"|*"rm /workspace/branches/main/.git"|*"rmdir /workspace/branches/main/.git"*)
	        echo "⛔ BLOCKED: Deletion of .git directory or its contents is not allowed to prevent data loss" >&2
	        echo "🚨 REPOSITORY PROTECTION: The .git directory must not be modified or deleted" >&2
	        echo "❌ **VIOLATION** - This operation would destroy version control history" >&2
	        exit 2
	        ;;
	    *"rm -rf /workspace/branches/main/code/.git"|*"rm /workspace/branches/main/code/.git"|*"rmdir /workspace/branches/main/code/.git"*)
	        echo "⛔ BLOCKED: Deletion of .git directory or its contents is not allowed to prevent data loss" >&2
	        echo "🚨 REPOSITORY PROTECTION: The .git directory must not be modified or deleted" >&2
	        echo "❌ **VIOLATION** - This operation would destroy version control history" >&2
	        exit 2
	        ;;
	    *"mv /workspace/branches/main "*|*"mv /workspace/branches/main/.git "*)
	        echo "⛔ BLOCKED: Moving/renaming protected directories is not allowed to prevent data loss" >&2
	        echo "🚨 DATA PROTECTION: The main workspace and .git directories must not be moved" >&2
	        echo "❌ **VIOLATION** - This operation could break the workspace structure" >&2
	        exit 2
	        ;;
	    *"ln -s /workspace/branches/main"*|*"ln -s /workspace/branches/main/.git"*)
	        echo "⛔ BLOCKED: Creating symlinks to protected directories is not allowed to prevent data loss" >&2
	        echo "🚨 SYMLINK ATTACK PREVENTION: Symlinks could be used to bypass directory protection" >&2
	        echo "❌ **VIOLATION** - This operation could enable indirect deletion of protected directories" >&2
	        exit 2
	        ;;
	esac

	# Allow all other commands
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
	    echo "⛔ PROMPT WARNING: git repository creation not allowed at workspace root (see task-protocol.md line 931)" >&2
	    echo "🚨 REPOSITORY CREATION BLOCKED: Per task-protocol.md, NEVER create new git repositories at workspace root" >&2
	    echo "📍 EXISTING REPOSITORY: Use 'cd /workspace/branches/main/code' for git operations" >&2
	    echo "📋 FOR COMMITS: Use the existing repository in /workspace/branches/main/code/" >&2
	    echo "" >&2
	    echo "🔧 CORRECT USAGE:" >&2
	    echo "   cd /workspace/branches/main/code" >&2
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
	if echo "$USER_PROMPT_LOWER" | grep -q "delete.*workspace/branches/main\|remove.*workspace/branches/main\|rm.*workspace/branches/main"; then
	    echo "⛔ PROMPT WARNING: Deletion of /workspace/branches/main is not allowed to prevent data loss" >&2
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
	    if [ ! -d ".git" ] && [ ! -d "/workspace/branches/main/code/.git" ]; then
	        echo "📍 GIT OPERATIONS GUIDE:" >&2
	        echo "   Repository location: /workspace/branches/main/code/" >&2
	        echo "   Command: cd /workspace/branches/main/code && git status" >&2
	        echo "🚨 REMINDER: Never use 'git init' - violates task-protocol.md" >&2
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
	    exit 0
	    ;;
esac
