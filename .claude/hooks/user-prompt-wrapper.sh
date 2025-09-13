#!/bin/bash

# Wrapper for UserPromptSubmit events
# Calls the consolidated smart-doc-prompter with the correct event type

USER_MESSAGE="$1"

# Get the directory containing this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Call smart-doc-prompter with absolute path
"$SCRIPT_DIR/smart-doc-prompter.sh" "UserPromptSubmit" "$USER_MESSAGE"