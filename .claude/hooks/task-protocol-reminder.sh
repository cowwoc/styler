#!/bin/bash

# Task Protocol enforcement reminder for session start
# Ensures Claude follows the mandatory TASK PROTOCOL for ALL tasks
# Only runs for agents that need full workflow compliance

# Check if this hook should run for the current agent
SCRIPT_DIR="$(dirname "$0")"
if ! "$SCRIPT_DIR/agent-context-filter.sh" "task-protocol-reminder"; then
	exit 0  # Skip hook for this agent type
fi

TASK_PROTOCOL_FILE="$(dirname "$0")/../../docs/project/task-protocol.md"

if [[ ! -f "$TASK_PROTOCOL_FILE" ]]; then
	echo '{
	    "hookSpecificOutput": {
	        "hookEventName": "SessionStart", 
	        "additionalContext": "🚨 CRITICAL TASK PROTOCOL ENFORCEMENT: task-protocol.md file not found! Cannot enforce mandatory TASK PROTOCOL."
	    }
	}'
	exit 0
fi

echo '{
	"hookSpecificOutput": {
	    "hookEventName": "SessionStart", 
	    "additionalContext": "🚨 MANDATORY TASK PROTOCOL ENFORCEMENT: Before executing ANY task (whether from todo list OR any user request), you MUST FIRST read docs/project/task-protocol.md completely and apply its mandatory TASK PROTOCOL procedures. The TASK PROTOCOL defines required steps for task isolation, stakeholder consultation, atomic locking, and worktree management. ANY task execution without following the TASK PROTOCOL procedures is a CRITICAL VIOLATION requiring immediate task restart. Read task-protocol.md NOW before proceeding with any task execution."
	}
}'
