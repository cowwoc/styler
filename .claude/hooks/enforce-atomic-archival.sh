#!/bin/bash
# Pre-Tool-Use Hook: Enforce atomic archival in task branch commits before --ff-only merge
#
# ADDED: 2025-12-04 after implement-file-processing-pipeline created separate
# commits for code merge and documentation archival instead of atomic commit
#
# UPDATED: 2025-12-04 to work with --ff-only workflow (validates task branch
# commit includes archival BEFORE merge, not during git commit on main)
#
# BUG FIX: 2025-12-06 - Fixed directory detection for compound commands
# When command is "cd /workspace/main && git merge ...", $PWD is pre-cd directory
# Now parses command string to detect effective directory after cd
#
# PREVENTS: Merging task branches that don't include todo.md and changelog.md updates
# ENFORCES: Atomic commits that include task + todo.md + changelog.md together
#
# Triggers: PreToolUse (tool:Bash matcher in settings.json)

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in enforce-atomic-archival.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/hook-logger.sh"
source /workspace/.claude/scripts/json-output.sh

# Read input from stdin
INPUT=$(cat)

# Extract tool information
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty' 2>/dev/null || echo "")
TOOL_PARAMS=$(echo "$INPUT" | jq -r '.tool_input // {}' 2>/dev/null || echo "{}")

# Only validate Bash tool
if [[ "$TOOL_NAME" != "Bash" ]]; then
	exit 0
fi

# Extract command
COMMAND=$(echo "$TOOL_PARAMS" | jq -r '.command // empty' 2>/dev/null || echo "")

# Only validate git merge commands
if [[ ! "$COMMAND" =~ git[[:space:]]+merge ]]; then
	exit 0
fi

# Determine effective directory for merge
# Bug fix: When command includes "cd /workspace/main &&", $PWD is pre-cd directory
# ADDED: 2025-12-06 after implement-ai-violation-output merged without archival
# because hook saw $PWD=/workspace/tasks/... but command changed to /workspace/main
EFFECTIVE_DIR="$PWD"
if [[ "$COMMAND" =~ cd[[:space:]]+(/workspace/main|/workspace/main/)[[:space:]]*(\&\&|;) ]]; then
	EFFECTIVE_DIR="/workspace/main"
fi

# Only enforce in /workspace/main
if [[ "$EFFECTIVE_DIR" != "/workspace/main" ]]; then
	exit 0
fi

# Extract branch being merged (handles flags like --ff-only before branch name)
MERGE_BRANCH=$(echo "$COMMAND" | sed -E 's/.*git[[:space:]]+merge[[:space:]]+(--[^[:space:]]+[[:space:]]+)*([^[:space:]]+).*/\2/')

# Only check task branches (implement-*, refactor-*, fix-*)
if [[ ! "$MERGE_BRANCH" =~ ^(implement|refactor|fix)- ]]; then
	exit 0
fi

# Verify branch exists
if ! git rev-parse --verify "$MERGE_BRANCH" >/dev/null 2>&1; then
	# Branch doesn't exist, let git merge fail naturally
	exit 0
fi

log_hook_start "enforce-atomic-archival" "PreToolUse"

# Get the files changed in the task branch commit (relative to main)
# With --ff-only workflow, task branch should have exactly 1 commit with all changes
CHANGED_FILES=$(git diff --name-only main..."$MERGE_BRANCH" 2>/dev/null || echo "")

if [[ -z "$CHANGED_FILES" ]]; then
	log_hook_success "enforce-atomic-archival" "PreToolUse" "No changes in branch, allowing merge"
	exit 0
fi

# Check if implementation files are present (indicates this is a real task, not just docs)
HAS_IMPL_FILES=false
if echo "$CHANGED_FILES" | grep -qE '\.(java|kt|ts|py)$'; then
	HAS_IMPL_FILES=true
fi

# If no implementation files, this isn't a code task - allow without archival check
if [[ "$HAS_IMPL_FILES" == "false" ]]; then
	log_hook_success "enforce-atomic-archival" "PreToolUse" "No implementation files, allowing merge"
	exit 0
fi

# This is a code task - check for archival files
HAS_TODO=false
HAS_CHANGELOG=false

if echo "$CHANGED_FILES" | grep -q '^todo\.md$'; then
	HAS_TODO=true
fi

if echo "$CHANGED_FILES" | grep -q '^changelog\.md$'; then
	HAS_CHANGELOG=true
fi

# If both archival files are present, allow
if [[ "$HAS_TODO" == "true" && "$HAS_CHANGELOG" == "true" ]]; then
	log_hook_success "enforce-atomic-archival" "PreToolUse" "Atomic archival verified: todo.md and changelog.md in branch"
	exit 0
fi

# Missing archival files - block and explain
MISSING=""
if [[ "$HAS_TODO" == "false" ]]; then
	MISSING="todo.md"
fi
if [[ "$HAS_CHANGELOG" == "false" ]]; then
	if [[ -n "$MISSING" ]]; then
		MISSING="$MISSING and changelog.md"
	else
		MISSING="changelog.md"
	fi
fi

log_hook_blocked "enforce-atomic-archival" "PreToolUse" "Task branch missing archival: $MISSING"

MESSAGE="## 🚨 ATOMIC ARCHIVAL VIOLATION BLOCKED

**Task Branch**: \`$MERGE_BRANCH\`
**Missing Files**: \`$MISSING\`
**Violation**: Task branch commit MUST include todo.md and changelog.md updates

## ⚠️ MANDATORY ATOMIC ARCHIVAL REQUIREMENT

You attempted to merge a task branch that doesn't include documentation updates.

**AUTOMATIC ACTION TAKEN**:
- Git merge blocked
- Violation logged

**PROTOCOL REQUIREMENT** (task-protocol-core.md line 918):
> \"Atomic commit: task + todo.md + changelog.md merged to main\"

**WHY THIS MATTERS**:
- Main branch commits must be atomic units of work
- Each task commit must include its documentation updates
- With --ff-only, archival must be in the task branch commit BEFORE merge

**REQUIRED ACTION**:

1. **Switch to task branch**:
   \`\`\`bash
   cd /workspace/tasks/${MERGE_BRANCH}/code
   git checkout ${MERGE_BRANCH}
   \`\`\`

2. **Update todo.md** - Mark task complete:
   \`\`\`markdown
   - [x] **COMPLETE:** \`${MERGE_BRANCH}\` - Description ($(date +%Y-%m-%d))
   \`\`\`

3. **Update changelog.md** - Document changes:
   \`\`\`markdown
   ## $(date +%Y-%m-%d)

   ### ${MERGE_BRANCH}

   - Added feature X
   - Implemented Y
   - Fixed Z
   \`\`\`

4. **Amend the squashed commit** to include archival:
   \`\`\`bash
   git add todo.md changelog.md
   git commit --amend --no-edit
   \`\`\`

5. **Return to main and retry merge**:
   \`\`\`bash
   cd /workspace/main
   git merge --ff-only ${MERGE_BRANCH}
   \`\`\`

## Alternative: Use archive-task Skill

Before squashing, use the archive-task skill to handle this:
\`\`\`
Skill(skill=\"archive-task\")
\`\`\`

## Protocol Reference

See:
- task-protocol-core.md § COMPLETE → CLEANUP (line 918)
- CLAUDE.md § Checkpoint 2: AWAITING_USER_APPROVAL → COMPLETE
- git-workflow.md § Task Branch Squashing"

# Use proper permission system: JSON to stdout, message to stderr, exit 0
output_hook_block "Blocked: Task branch missing archival files (todo.md and/or changelog.md). Add archival entries before merging." "$MESSAGE"
exit 0
