#!/bin/bash

# Claude Code statusline script
# Displays git worktree, model name, and session duration

# Ensure UTF-8 encoding for proper emoji rendering
export LC_ALL=C.UTF-8
export LANG=C.UTF-8


# Extract git worktree name (directory name of the worktree root)
GIT_WORKTREE=$(basename "$(git rev-parse --show-toplevel 2>/dev/null)" 2>/dev/null || echo "N/A")

# Read JSON input from stdin and extract needed values with simple parsing
input=$(cat)

# Extract duration using simple string manipulation (faster than jq)
DURATION_MS=0
if [[ "$input" =~ \"total_duration_ms\"[[:space:]]*:[[:space:]]*([0-9]+) ]]; then
	DURATION_MS="${BASH_REMATCH[1]}"
fi
DURATION_SECONDS=$((DURATION_MS / 1000))
HOURS=$((DURATION_SECONDS / 3600))
MINUTES=$(((DURATION_SECONDS % 3600) / 60))

# Extract model name using simple string manipulation
MODEL_NAME="Claude"
if [[ "$input" =~ \"display_name\"[[:space:]]*:[[:space:]]*\"([^\"]+)\" ]]; then
	MODEL_NAME="${BASH_REMATCH[1]}"
fi

# Extract session ID using simple string manipulation
SESSION_ID="N/A"
if [[ "$input" =~ \"session_id\"[[:space:]]*:[[:space:]]*\"([^\"]+)\" ]]; then
	SESSION_ID="${BASH_REMATCH[1]}"
fi


# Colors for statusline components
WORKTREE_COLOR="\033[38;2;255;255;255m"  # Bright White (excellent readability)
MODEL_COLOR="\033[38;2;220;150;9m"     # Warm Gold
TIME_COLOR="\033[38;2;255;127;80m"     # Coral
SESSION_COLOR="\033[38;2;147;112;219m" # Medium Purple
RESET="\033[0m"

# Set Windows Terminal tab title to git worktree name
# Set custom title if we have a valid git worktree, otherwise reset to default
if [[ "$GIT_WORKTREE" != "N/A" && -n "$GIT_WORKTREE" ]]; then
	printf "\033]0;%s\007" "$GIT_WORKTREE"
else
	# Reset tab title to empty/default when no valid git worktree
	printf "\033]0;\007"
fi

# Generate and output the statusline
printf '🌿 %b%s%b 🤖 %b%s%b ⏰ %b%02d:%02d%b 🆔 %b%s%b\n' \
    "$WORKTREE_COLOR" "$GIT_WORKTREE" "$RESET" \
    "$MODEL_COLOR" "$MODEL_NAME" "$RESET" \
    "$TIME_COLOR" "$HOURS" "$MINUTES" "$RESET" \
    "$SESSION_COLOR" "$SESSION_ID" "$RESET"
