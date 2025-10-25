#!/bin/bash
# Git Status Validation Library
#
# Provides functions for validating git working directory cleanliness
# before state transitions and other critical operations
#
# Usage:
#   source /workspace/main/.claude/hooks/lib/git-status-check.sh
#   check_git_status_clean /workspace/tasks/my-task/code || handle_error

# Check if a git working directory is clean (no uncommitted changes)
# Args:
#   $1: Working directory path
# Returns:
#   0 if clean, 1 if uncommitted changes exist
# Output:
#   Prints status message to stdout
check_git_status_clean() {
	local working_dir="$1"

	if [[ -z "$working_dir" ]]; then
		echo "ERROR: check_git_status_clean requires working directory argument" >&2
		return 1
	fi

	if [[ ! -d "$working_dir" ]]; then
		echo "ERROR: Working directory does not exist: $working_dir" >&2
		return 1
	fi

	# Change to working directory for git status check
	pushd "$working_dir" > /dev/null || return 1

	# Check for uncommitted changes
	if ! git diff-index --quiet HEAD -- 2>/dev/null; then
		echo "UNCOMMITTED CHANGES DETECTED in $working_dir"
		echo ""
		echo "Working directory has uncommitted changes:"
		git status --short
		popd > /dev/null
		return 1
	fi

	# Check for untracked files that would be significant
	local untracked_count
	untracked_count=$(git ls-files --others --exclude-standard | wc -l)
	if [[ "$untracked_count" -gt 0 ]]; then
		echo "UNTRACKED FILES DETECTED in $working_dir"
		echo ""
		echo "Working directory has $untracked_count untracked files:"
		git ls-files --others --exclude-standard | head -10
		popd > /dev/null
		return 1
	fi

	popd > /dev/null
	return 0
}

# Validate git status before state transition
# Args:
#   $1: Task name
#   $2: Current state
#   $3: Target state
# Returns:
#   0 if clean, 1 if uncommitted changes exist
# Output:
#   Prints detailed error message if validation fails
validate_git_status_for_transition() {
	local task_name="$1"
	local current_state="$2"
	local target_state="$3"
	local working_dir="/workspace/tasks/${task_name}/code"

	if [[ ! -d "$working_dir" ]]; then
		# No working directory exists yet, validation passes
		return 0
	fi

	if check_git_status_clean "$working_dir"; then
		echo "✅ Git status clean - transition allowed: $current_state → $target_state"
		return 0
	else
		echo ""
		echo "❌ TRANSITION BLOCKED: $current_state → $target_state"
		echo ""
		echo "**MANDATORY REQUIREMENT**: Working directory MUST be clean before state transitions"
		echo ""
		echo "**REQUIRED ACTION**:"
		echo "1. Commit all changes:"
		echo "   cd $working_dir"
		echo "   git add <files>"
		echo "   git commit -m 'Description of changes'"
		echo ""
		echo "2. Verify clean status:"
		echo "   git status  # Should show 'nothing to commit, working tree clean'"
		echo ""
		echo "3. Retry state transition after committing"
		echo ""
		echo "**Protocol Reference**: See git-workflow.md § Git Working Directory Requirements"
		return 1
	fi
}

# Get summary of uncommitted changes
# Args:
#   $1: Working directory path
# Returns:
#   0 always
# Output:
#   Prints change summary
get_uncommitted_changes_summary() {
	local working_dir="$1"

	if [[ ! -d "$working_dir" ]]; then
		echo "No working directory at $working_dir"
		return 0
	fi

	pushd "$working_dir" > /dev/null || return 0

	echo "## Uncommitted Changes Summary"
	echo ""
	echo "**Modified files**:"
	git diff --name-only | sed 's/^/  - /'
	echo ""
	echo "**Staged files**:"
	git diff --cached --name-only | sed 's/^/  - /'
	echo ""
	echo "**Untracked files**:"
	git ls-files --others --exclude-standard | sed 's/^/  - /'

	popd > /dev/null
	return 0
}
