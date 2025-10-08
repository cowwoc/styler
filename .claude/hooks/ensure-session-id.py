#!/usr/bin/env python3
# Based on https://github.com/anthropics/claude-code/issues/1990#issuecomment-3208077056
import json
import os
import sys

# This script handles both SessionStart and UserPromptSubmit events
# to ensure the session ID is always in context.

def create_context_output(event_name, context_message):
	"""Creates the JSON payload to inject context back into Claude Code."""
	return {
	    "hookSpecificOutput": {
	        "hookEventName": event_name,
	        "additionalContext": context_message
	    }
	}

def main():
	try:
	    # Debug: Log that the hook was called
	    print(f"DEBUG: ensure-session-id.py called from {os.getcwd()}", file=sys.stderr)
	    
	    # Check if stdin has data
	    if sys.stdin.isatty():
	        print("DEBUG: No stdin data (TTY mode)", file=sys.stderr)
	        sys.exit(0)
	    
	    stdin_content = sys.stdin.read()
	    if not stdin_content.strip():
	        print("DEBUG: Empty stdin", file=sys.stderr)
	        sys.exit(0)
	        
	    data = json.loads(stdin_content)
	    hook_event = data.get("hook_event_name")
	    session_id = data.get("session_id")

	    if not session_id:
	        sys.exit(0) # Nothing to do if we don't have a session_id

	    context_message = f"System Note: The current session ID is {session_id}. I must use this ID for session-specific tasks."

	    if hook_event == "SessionStart":
	        # On session start, always inject the context.
	        print(json.dumps(create_context_output("SessionStart", context_message)))

	    elif hook_event == "UserPromptSubmit":
	        # On prompt submit, only inject if the context was lost (e.g., by /compact).
	        transcript_path = data.get("transcript_path")
	        
	        if not transcript_path or not os.path.exists(transcript_path):
	            # No transcript path or file doesn't exist, inject context to be safe
	            print(json.dumps(create_context_output("UserPromptSubmit", context_message)))
	            return
	        
	        try:
	            # Read the last ~20 lines of the transcript to check if the ID is still there.
	            # This is an efficient way to avoid re-injecting on every single prompt.
	            with open(transcript_path, 'r') as f:
	                recent_lines = f.readlines()[-200:]
	            
	            recent_transcript = "".join(recent_lines)

	            if session_id not in recent_transcript:
	                # The session ID is missing from recent history, so re-inject it!
	                print(json.dumps(create_context_output("UserPromptSubmit", context_message)))
	        except (IOError, OSError):
	            # File read error, inject context to be safe
	            print(json.dumps(create_context_output("UserPromptSubmit", context_message)))

	except Exception as e:
	    # Log errors to stderr to avoid breaking the hook's JSON output
	    print(f"Error in ensure-session-id.py: {e}", file=sys.stderr)
	    sys.exit(1)

if __name__ == "__main__":
	main()
