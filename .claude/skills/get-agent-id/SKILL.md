---
name: get-agent-id
description: Get the most recent agent ID from a Task tool invocation for use with resume parameter
allowed-tools: Bash
---

# Get Agent ID Skill

**Purpose**: Retrieve the agent ID from a Task tool invocation for use with the Task tool's `resume` parameter.

## Version-Specific Methods

### Preferred Method: SubagentStop Hook (v2.0.42+)

**As of Claude Code v2.0.42**, the `SubagentStop` hook provides `agent_id` directly, eliminating the need for conversation log parsing.

**Setup**: Register a SubagentStop hook in `.claude/settings.json`:

```json
"SubagentStop": [
  {
    "hooks": [
      {
        "type": "command",
        "command": "/workspace/.claude/hooks/save-agent-id.sh"
      }
    ]
  }
]
```

**Hook Implementation** (`save-agent-id.sh`):
```bash
#!/bin/bash
# Automatically save agent_id when subagent stops

# Read JSON from stdin
JSON_INPUT=$(cat)

# Extract agent_id and agent_transcript_path (available in v2.0.42+)
AGENT_ID=$(echo "$JSON_INPUT" | jq -r '.agent_id // empty')
TRANSCRIPT_PATH=$(echo "$JSON_INPUT" | jq -r '.agent_transcript_path // empty')

if [[ -n "$AGENT_ID" ]]; then
  # Store in standard location
  echo "$AGENT_ID" > /tmp/last-agent-id.txt
  echo "📋 Saved agent ID: $AGENT_ID" >&2

  # Optionally store transcript path for debugging
  if [[ -n "$TRANSCRIPT_PATH" ]]; then
    echo "$TRANSCRIPT_PATH" > /tmp/last-agent-transcript.txt
  fi
fi
```

**Usage**:
```bash
# Agent ID is automatically saved when subagent completes
# Simply read it when needed
AGENT_ID=$(cat /tmp/last-agent-id.txt)

# Use with Task tool resume
Task tool: general-purpose
Resume: $AGENT_ID
Prompt: "Continue from previous analysis..."
```

### Fallback Method: Conversation Log Parsing (Pre-v2.0.42)

> **Note**: This method is a workaround for [Claude Code issue #10864](https://github.com/anthropics/claude-code/issues/10864). Use the SubagentStop hook method for v2.0.42+.

**When to Use**:
- Claude Code version < 2.0.42
- SubagentStop hook not configured
- Need to retrieve agent ID from older session

**What It Does**:
1. Reads the current session's conversation log
2. Extracts the most recent `agentId` from `toolUseResult` entries
3. Stores the agent ID in a specified file
4. Outputs the agent ID for verification

#### Execution Instructions (Fallback Method)

**Input Required**:
- Session ID (automatically available from SessionStart hook context)
- Output file path (where to store the extracted agent ID)

**Output**:
- Agent ID stored in specified file
- Agent ID echoed to stdout for verification

After invoking a Task tool, extract the agent ID with this bash command:

```bash
# Extract most recent agent ID from conversation log
# Replace SESSION_ID with actual value from context (e.g., "2ccd9677-8b8e-4048-a052-16b52b989c7b")
# Replace OUTPUT_FILE with desired storage path (e.g., "/tmp/my-agent-id.txt")

bash -c 'SESSION_ID="<session-id-from-context>" && OUTPUT_FILE="<output-file-path>" && AGENT_ID=$(jq -r "select(.toolUseResult.agentId) | .toolUseResult.agentId" /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl 2>/dev/null | tail -1) && echo "$AGENT_ID" > "$OUTPUT_FILE" && echo "Stored agent ID: $AGENT_ID"'
```

**Parameter Substitution**:
- `<session-id-from-context>`: Replace with session ID from SessionStart system reminder
- `<output-file-path>`: Replace with path where agent ID should be stored

**Example**:
```bash
bash -c 'SESSION_ID="2ccd9677-8b8e-4048-a052-16b52b989c7b" && OUTPUT_FILE="/tmp/validator-agent-id.txt" && AGENT_ID=$(jq -r "select(.toolUseResult.agentId) | .toolUseResult.agentId" /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl 2>/dev/null | tail -1) && echo "$AGENT_ID" > "$OUTPUT_FILE" && echo "Stored agent ID: $AGENT_ID"'
```

## Using the Extracted Agent ID

After extraction, read the agent ID from the file for use with Task tool resume:

```bash
# Read stored agent ID
AGENT_ID=$(cat /tmp/validator-agent-id.txt)

# Use with Task tool resume parameter
Task tool: general-purpose
Resume: $AGENT_ID
Prompt: "Continue from previous analysis..."
```

## How It Works

**Step 1: Locate Conversation Log**

Conversation logs are stored at `/home/node/.config/projects/-workspace/{session-id}.jsonl` where each line is a JSON object representing a conversation event.

**Step 2: Filter for Agent ID Entries**

The `jq` command filters for entries where:
- Entry has `toolUseResult.agentId` field (Task tool invocations)
- Returns the `agentId` value

**Step 3: Get Most Recent**

Using `tail -1` ensures we get the most recent agent ID (last line after filtering).

**Step 4: Store and Verify**

- Agent ID is written to specified output file
- Agent ID is echoed for visual confirmation

## Error Handling

**If extraction fails** (empty agent ID):
- Verify session ID is correct (check SessionStart system reminder)
- Ensure Task tool was invoked recently in this session
- Check conversation log exists: `ls -la /home/node/.config/projects/-workspace/{session-id}.jsonl`

**If jq errors occur**:
- Ensure jq is installed: `which jq`
- Check conversation log is valid JSON: `tail -1 /home/node/.config/projects/-workspace/{session-id}.jsonl | jq`

## Multi-Agent Workflows

For workflows with multiple agents (e.g., Phase 1 validator, Phase 2 validator):

```bash
# Phase 1: Extract first agent ID
bash -c 'SESSION_ID="..." && OUTPUT_FILE="/tmp/phase1-agent.txt" && AGENT_ID=$(jq -r "select(.toolUseResult.agentId) | .toolUseResult.agentId" /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl 2>/dev/null | tail -1) && echo "$AGENT_ID" > "$OUTPUT_FILE" && echo "Phase 1 agent ID: $AGENT_ID"'

# ... Phase 1 work ...

# Phase 2: Extract second agent ID (after invoking new agent)
bash -c 'SESSION_ID="..." && OUTPUT_FILE="/tmp/phase2-agent.txt" && AGENT_ID=$(jq -r "select(.toolUseResult.agentId) | .toolUseResult.agentId" /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl 2>/dev/null | tail -1) && echo "$AGENT_ID" > "$OUTPUT_FILE" && echo "Phase 2 agent ID: $AGENT_ID"'
```

**Important**: Always extract immediately after Task invocation to ensure you capture the correct agent ID.

## Which Method Should I Use?

**Decision Tree**:

1. **Check Claude Code version**: Run `claude --version` or check system reminders for version info
2. **If v2.0.42+**:
   - ✅ Use SubagentStop hook method (preferred)
   - Cleaner, automatic, no manual extraction needed
   - Requires one-time hook setup in settings.json
3. **If pre-v2.0.42**:
   - Use conversation log parsing method (fallback)
   - More manual but works on all versions

**Migration Path**:
- Existing projects using conversation log parsing: Continue working, optional upgrade to SubagentStop hook
- New projects on v2.0.42+: Use SubagentStop hook from the start

## Version History

- **v2.0.42** (2024): Added `agent_id` and `agent_transcript_path` fields to SubagentStop hooks ([changelog](https://github.com/anthropics/claude-code/blob/main/CHANGELOG.md))
- **Pre-v2.0.42**: Manual extraction from conversation logs required

## Related Skills

- **get-session-id**: Provides session ID needed for conversation log parsing method
- **get-history**: Read full conversation log for debugging

## Common Use Cases

### Use Case 1: Two-Phase Validation

```bash
# Phase 1: Invoke validator agent
Task tool: general-purpose
Prompt: "Analyze document..."

# Extract Phase 1 agent ID
bash -c 'SESSION_ID="abc123..." && OUTPUT_FILE="/tmp/validator-phase1.txt" && AGENT_ID=$(jq -r "select(.toolUseResult.agentId) | .toolUseResult.agentId" /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl 2>/dev/null | tail -1) && echo "$AGENT_ID" > "$OUTPUT_FILE" && echo "Stored agent ID: $AGENT_ID"'

# Phase 2: Resume same validator agent
AGENT_ID=$(cat /tmp/validator-phase1.txt)
Task tool: general-purpose
Resume: $AGENT_ID
Prompt: "Now compare with baseline..."
```

### Use Case 2: Iterative Refinement

```bash
# Initial analysis agent
Task tool: general-purpose
Prompt: "Review code for issues..."

# Extract agent ID
bash -c '...'  # extraction command

# Resume for follow-up questions
AGENT_ID=$(cat /tmp/analyzer-agent.txt)
Task tool: general-purpose
Resume: $AGENT_ID
Prompt: "Focus on security issues specifically..."
```

## Technical Details

**Conversation Log Format**:

Each Task tool invocation creates entries like:
```json
{
  "type": "toolResult",
  "toolUseResult": {
    "agentId": "a16346ea",
    "output": "...",
    ...
  },
  ...
}
```

**jq Filter Explanation**:
- `select(.toolUseResult.agentId)`: Only entries with agentId field
- `.toolUseResult.agentId`: Extract the agentId value
- `2>/dev/null`: Suppress jq errors on malformed entries
- `tail -1`: Get last (most recent) match
