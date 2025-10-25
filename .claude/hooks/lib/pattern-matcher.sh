#!/bin/bash
# Pattern Matcher Library
# Reusable functions for Category A/B file pattern matching
#
# Usage: source /workspace/main/.claude/hooks/lib/pattern-matcher.sh

# Match Category A patterns (non-protocol work)
# Args:
#   $1: file path
# Returns:
#   0: Matches Category A (allow without protocol)
#   1: Does not match Category A
match_category_a() {
	local path="$1"
	local filename="${path##*/}"

	# Documentation (except task.md)
	if [[ "$path" =~ \.md$ ]] && [[ "$filename" != "task.md" ]]; then
		# Exception: studies matching todo.md tasks require protocol
		if [[ "$path" =~ docs/studies/ ]]; then
			if is_study_for_implementation "$path"; then
				return 1  # Not Category A
			fi
		fi
		return 0  # Category A
	fi

	# Hooks
	if [[ "$path" =~ \.claude/hooks/ ]]; then
		return 0
	fi

	# Scripts
	if [[ "$path" =~ scripts/ ]]; then
		return 0
	fi

	# Build files (ALL pom.xml per user clarification)
	if [[ "$filename" == "pom.xml" ]]; then
		return 0
	fi

	if [[ "$path" =~ build\. ]] || [[ "$path" =~ \.mvn/ ]] || [[ "$path" =~ mvnw ]]; then
		return 0
	fi

	# Git configs
	if [[ "$filename" =~ ^\.git(ignore|attributes)$ ]]; then
		return 0
	fi

	return 1  # Not Category A
}

# Match Category B patterns (protocol-required)
# Args:
#   $1: file path
# Returns:
#   0: Matches Category B (requires protocol)
#   1: Does not match Category B
match_category_b() {
	local path="$1"
	local filename="${path##*/}"

	# task.md always requires protocol
	if [[ "$filename" == "task.md" ]]; then
		return 0
	fi

	# Source files
	if [[ "$path" =~ \.(java|ts|py|js|go|rs|cpp|c|h|hpp)$ ]]; then
		return 0
	fi

	# Test directories (be specific: src/test, not just any /test/ in path)
	if [[ "$path" =~ /src/test/ ]] || [[ "$path" =~ /test/.*\.(java|ts|py|js|go|rs|cpp|c|h|hpp)$ ]]; then
		return 0
	fi

	# Source directories (must be /src/ followed by main/ or contain source files)
	if [[ "$path" =~ /src/main/ ]] || [[ "$path" =~ /src/.*\.(java|ts|py|js|go|rs|cpp|c|h|hpp)$ ]]; then
		return 0
	fi

	# Module descriptors
	if [[ "$filename" == "module-info.java" ]]; then
		return 0
	fi

	# Implementation studies
	if [[ "$path" =~ docs/studies/ ]]; then
		if is_study_for_implementation "$path"; then
			return 0
		fi
	fi

	return 1  # Not Category B
}

# Match source code patterns (for VIOLATION #1 detection)
# Args:
#   $1: file path
# Returns:
#   0: Matches source code pattern
#   1: Does not match
match_source_pattern() {
	local path="$1"

	if [[ "$path" =~ \.(java|ts|py|js|go|rs|cpp|c|h|hpp)$ ]]; then
		return 0
	fi

	return 1
}

# Check if study is for implementation planning (matches todo.md task)
# Args:
#   $1: study file path
# Returns:
#   0: Study matches todo.md task (requires protocol)
#   1: Exploratory study (no protocol required)
is_study_for_implementation() {
	local study_path="$1"
	local filename="${study_path##*/}"
	local study_name="${filename%.md}"

	# If no todo.md, assume exploratory
	if [[ ! -f "/workspace/main/todo.md" ]]; then
		return 1
	fi

	# Check if any todo.md task contains study topic
	# Match pattern: - [ ] taskname or - [x] taskname
	# Use case-insensitive matching and word boundaries
	if grep -qiE "\- \[[ x]\].*${study_name}" /workspace/main/todo.md; then
		return 0  # Study matches task, requires protocol
	fi

	return 1  # Exploratory study, no protocol required
}

# Get category name for display purposes
# Args:
#   $1: file path
# Returns:
#   Category name string (echoed to stdout)
get_category_name() {
	local path="$1"

	if match_category_a "$path"; then
		echo "Category A (non-protocol)"
		return 0
	fi

	if match_category_b "$path"; then
		echo "Category B (protocol-required)"
		return 0
	fi

	echo "Unknown (unrecognized pattern)"
	return 0
}

# Get pattern reason for error messages
# Args:
#   $1: file path
# Returns:
#   Reason string (echoed to stdout)
get_category_reason() {
	local path="$1"
	local filename="${path##*/}"

	# Category B reasons
	if [[ "$filename" == "task.md" ]]; then
		echo "Task requirements document"
		return 0
	fi

	if [[ "$path" =~ \.(java|ts|py|js|go|rs|cpp|c|h|hpp)$ ]]; then
		echo "Source code file"
		return 0
	fi

	if [[ "$path" =~ /test/ ]]; then
		echo "Test code"
		return 0
	fi

	if [[ "$path" =~ /src/ ]]; then
		echo "Source directory content"
		return 0
	fi

	if [[ "$filename" == "module-info.java" ]]; then
		echo "Module descriptor"
		return 0
	fi

	if [[ "$path" =~ docs/studies/ ]] && is_study_for_implementation "$path"; then
		echo "Implementation planning study (matches todo.md task)"
		return 0
	fi

	# Category A reasons
	if [[ "$path" =~ \.md$ ]]; then
		echo "Documentation"
		return 0
	fi

	if [[ "$path" =~ \.claude/hooks/ ]]; then
		echo "Hook script"
		return 0
	fi

	if [[ "$path" =~ scripts/ ]]; then
		echo "Utility script"
		return 0
	fi

	if [[ "$filename" == "pom.xml" ]]; then
		echo "Build file"
		return 0
	fi

	echo "Unrecognized pattern"
	return 0
}
