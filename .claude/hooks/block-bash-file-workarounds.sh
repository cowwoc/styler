#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in block-bash-file-workarounds.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Block Bash File Creation Workarounds Hook
# Prevents using Bash to bypass Write/Edit tool blocking
#
# TRIGGER: PreToolUse (Bash)
#
# BLOCKED OPERATIONS:
# 1. echo/printf redirection to task/agent worktree paths
# 2. cat heredoc to task/agent worktree paths
# 3. tee to task/agent worktree paths
# 4. Any file creation workaround targeting task paths
#
# RATIONALE:
# When Write/Edit hooks block operations (due to protocol violations),
# agents should return errors to main agent, NOT use Bash workarounds.
# Workarounds mask protocol violations and bypass safety checks.
#
# ADDED: 2025-12-28 after discovering agents used Bash to create files
# when Write tool was blocked.

# Read JSON from stdin with timeout to prevent hanging
JSON_INPUT=""
if [ -t 0 ]; then
	exit 0
else
	JSON_INPUT="$(timeout 5s cat 2>/dev/null)" || JSON_INPUT=""
fi

# Source JSON parsing library
source "$CLAUDE_PROJECT_DIR/.claude/hooks/lib/json-parser.sh"

# Source helper for proper hook blocking
source /workspace/.claude/scripts/json-output.sh

# Extract tool name and command from JSON
TOOL_NAME=$(extract_json_value "$JSON_INPUT" "tool_name")
COMMAND=$(extract_json_value "$JSON_INPUT" "command")

# Fallback extraction if primary method fails
if [ -z "$TOOL_NAME" ]; then
	TOOL_NAME=$(echo "$JSON_INPUT" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

if [ -z "$COMMAND" ]; then
	COMMAND=$(echo "$JSON_INPUT" | sed -n 's/.*"command"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

# Only process Bash tool calls
if [ "$TOOL_NAME" != "Bash" ] && [ "$TOOL_NAME" != "bash" ]; then
	echo '{}'
	exit 0
fi

# Define path patterns to protect
# These are paths where file creation should go through Write/Edit tools
PROTECTED_PATTERNS=(
	"/workspace/main/.*\\.java"
	"/workspace/main/.*\\.ts"
	"/workspace/main/.*\\.py"
)

# Check for file creation commands with redirection to protected paths
# Pattern 1: echo/printf with > redirection
# Pattern 2: cat with heredoc (<<)
# Pattern 3: tee command
# Pattern 4: cp/mv creating new files

FILE_CREATION_DETECTED=false
DETECTED_PATH=""

# Check for redirect patterns (>, >>) to protected paths
for pattern in "${PROTECTED_PATTERNS[@]}"; do
	# Check echo > file, printf > file
	if echo "$COMMAND" | grep -qE "(echo|printf)[^>]*>[[:space:]]*['\"]?${pattern}"; then
		FILE_CREATION_DETECTED=true
		DETECTED_PATH=$(echo "$COMMAND" | grep -oE "['\"]?${pattern}['\"]?" | head -1)
		break
	fi

	# Check cat > file (heredoc pattern)
	if echo "$COMMAND" | grep -qE "cat[^>]*>[[:space:]]*['\"]?${pattern}"; then
		FILE_CREATION_DETECTED=true
		DETECTED_PATH=$(echo "$COMMAND" | grep -oE "['\"]?${pattern}['\"]?" | head -1)
		break
	fi

	# Check tee (with or without -a)
	if echo "$COMMAND" | grep -qE "\\|[[:space:]]*tee[[:space:]].*${pattern}"; then
		FILE_CREATION_DETECTED=true
		DETECTED_PATH=$(echo "$COMMAND" | grep -oE "${pattern}" | head -1)
		break
	fi
done

# Also check for explicit path patterns in the command with any redirection
if [ "$FILE_CREATION_DETECTED" = "false" ]; then
	# Look for any > to /workspace/main/...java|ts|py
	if echo "$COMMAND" | grep -qE ">[[:space:]]*['\"]?/workspace/main/.*\\.(java|ts|py)"; then
		FILE_CREATION_DETECTED=true
		DETECTED_PATH=$(echo "$COMMAND" | grep -oE "/workspace/main/[^[:space:]'\"]*\\.(java|ts|py)" | head -1)
	fi
fi

# If file creation workaround detected, block it
if [ "$FILE_CREATION_DETECTED" = "true" ]; then
	cat << EOF >&2

🚨 BASH FILE CREATION WORKAROUND BLOCKED
═══════════════════════════════════════════════════════════════

❌ Attempted: Bash file creation to bypass Write/Edit blocking
   Path: ${DETECTED_PATH:-[detected but path unclear]}

WHY THIS IS BLOCKED:
• Write/Edit hooks blocked for a reason (protocol violation)
• Using Bash to bypass masks the underlying issue
• Main agent never learns about the protocol problem
• Bypasses safety checks designed to prevent invalid states

WHAT TO DO INSTEAD:
1. Check the error message from the Write/Edit block
2. Update your status.json to BLOCKED status
3. Return error details to main agent immediately
4. Let main agent fix the protocol issue

CORRECT RESPONSE TO HOOK BLOCKING:
{
  "agent": "your-agent-name",
  "task": "task-name",
  "status": "BLOCKED",
  "blocked_by": "hook_protocol_violation",
  "details": "Hook error: [paste exact error message]",
  "updated_at": "..."
}

📖 See: CLAUDE.md § Tool Usage Best Practices

═══════════════════════════════════════════════════════════════
❌ COMMAND BLOCKED - Use Write/Edit tools, fix protocol issues
EOF
	output_hook_block "Blocked: Bash file creation to task/agent paths bypasses Write/Edit hooks. Return error to main agent instead."
	exit 0
fi

# Allow non-file-creation Bash commands
echo '{}'
exit 0
