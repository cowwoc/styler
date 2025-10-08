#!/bin/bash

# Task Protocol enforcement reminder for session start
# Ensures Claude follows the mandatory TASK PROTOCOL for ALL tasks

TASK_PROTOCOL_CORE="$(dirname "$0")/../../docs/project/task-protocol-core.md"
TASK_PROTOCOL_OPS="$(dirname "$0")/../../docs/project/task-protocol-operations.md"

if [[ ! -f "$TASK_PROTOCOL_CORE" ]] || [[ ! -f "$TASK_PROTOCOL_OPS" ]]; then
	echo '{
	    "hookSpecificOutput": {
	        "hookEventName": "SessionStart",
	        "additionalContext": "🚨 CRITICAL TASK PROTOCOL ENFORCEMENT: task-protocol files not found! Cannot enforce mandatory TASK PROTOCOL."
	    }
	}'
	exit 0
fi

echo '{
	"hookSpecificOutput": {
	    "hookEventName": "SessionStart",
	    "additionalContext": "🚨 MANDATORY TASK PROTOCOL ENFORCEMENT: Before executing ANY task (whether from todo list OR any user request), you MUST FIRST read docs/project/task-protocol-core.md and task-protocol-operations.md completely and apply their mandatory TASK PROTOCOL procedures. The TASK PROTOCOL defines required steps for task isolation, stakeholder consultation, atomic locking, and worktree management. ANY task execution without following the TASK PROTOCOL procedures is a CRITICAL VIOLATION requiring immediate task restart. Read both protocol files NOW before proceeding with any task execution."
	}
}'
