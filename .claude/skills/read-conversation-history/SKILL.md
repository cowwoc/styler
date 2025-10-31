---
name: read-conversation-history
description: Access raw conversation history from Claude Code session storage for audit and analysis
---

# Read Conversation History Skill

**Purpose**: Provide direct access to raw, unfiltered conversation history stored by Claude Code for audit trails, compliance verification, and conversation analysis.

**When to Use**:
- During `/audit-session` to provide raw conversation data to process-recorder
- When investigating protocol violations or agent behavior
- To verify user approval checkpoints in conversation
- To analyze tool call sequences and working directory context
- When main agent's filtered summaries are insufficient

## Skill Capabilities

### 1. Access Current Session Conversation

**Location**: `/home/node/.config/projects/-workspace/{session-id}.jsonl`

**Session ID Detection**:
```bash
# Method 1: From environment if available
echo $CLAUDE_SESSION_ID

# Method 2: From recent files (current session is largest/newest)
ls -lhS /home/node/.config/projects/-workspace/*.jsonl | head -1 | awk '{print $NF}'

# Method 3: Use the currently active session
CURRENT_SESSION=$(ls -t /home/node/.config/projects/-workspace/*.jsonl | head -1)
```

### 2. Parse Conversation Structure

**Entry Types**:
- `type: "summary"` - High-level conversation summary
- `type: "message"` - User or assistant messages
- `type: "tool_use"` - Tool invocations
- `type: "tool_result"` - Tool outputs

**Extract Messages**:
```bash
# Get all user messages
jq 'select(.type == "message" and .role == "user") | .content' conversation.jsonl

# Get all assistant messages
jq 'select(.type == "message" and .role == "assistant") | .content' conversation.jsonl

# Get all tool uses with names
jq 'select(.type == "tool_use") | {name: .name, input: .input}' conversation.jsonl
```

### 3. Verify User Approval Checkpoints

**Search Patterns**:
```bash
# Look for approval-related user messages
jq -r 'select(.type == "message" and .role == "user") | .content' conversation.jsonl | \
  grep -iE "(approve|proceed|continue|go ahead|yes|looks good|lgtm)"

# Look for agent waiting for approval
jq -r 'select(.type == "message" and .role == "assistant") | .content' conversation.jsonl | \
  grep -iE "(waiting for approval|user approval required|proceed\?|continue\?)"
```

### 4. Extract Tool Call Context

**Get Working Directory for Each Tool Call**:
```bash
# For each Edit/Write operation, check if pwd or cd preceded it
jq -r 'select(.type == "tool_use" and (.name == "Edit" or .name == "Write")) |
  {tool: .name, file: .input.file_path, timestamp: .timestamp}' conversation.jsonl

# Look for Bash calls that changed directory before Edit/Write
jq -r 'select(.type == "tool_use" and .name == "Bash" and
  (.input.command | contains("cd "))) | .input.command' conversation.jsonl
```

### 5. Agent Invocation Analysis

**Extract All Task Tool Calls**:
```bash
# Get all agent invocations with prompts
jq 'select(.type == "tool_use" and .name == "Task") |
  {agent: .input.subagent_type, description: .input.description,
   prompt: .input.prompt[0:200]}' conversation.jsonl
```

## Usage in /audit-session

### Phase 1: Process-Recorder Integration

**Replace filtered data with raw conversation access**:

```markdown
**CRITICAL**: Main agent MUST NOT provide filtered summaries. process-recorder uses
read-conversation-history skill to access raw conversation independently.

**Mandatory Actions**:
1. Use read-conversation-history skill to get current session conversation file
2. Parse conversation for:
   - User messages (approval checkpoints)
   - Tool uses (Edit/Write with working directory context)
   - Task invocations (agent delegation sequence)
   - Bash commands (directory changes, git operations)
3. Extract objective facts WITHOUT main agent interpretation
4. Output raw data to process-compliance-reviewer
```

### Phase 2: Compliance Review Integration

**Use raw conversation data for verification**:

```markdown
**Check 0.0: User Approval Checkpoints**
- Input: Raw user messages from conversation
- Search: Messages containing approval after SYNTHESIS/REVIEW states
- Verdict: PASS if approval found, FAIL if proceeded without approval

**Check 0.3: Working Directory Violations**
- Input: Bash cd commands + Edit/Write file paths
- Correlate: Did main agent cd to agent worktree before Edit/Write?
- Verdict: FAIL if any Edit/Write in /workspace/tasks/{task}/agents/{agent}/code
```

## Conversation File Structure

### Main Session File
**Path**: `/home/node/.config/projects/-workspace/{session-id}.jsonl`

**Format**: JSON Lines (one JSON object per line)

**Example Entry Structure**:
```json
{"type":"message","role":"user","content":"Work on implement-formatter-api","timestamp":1730331600000}
{"type":"message","role":"assistant","content":"I'll help with that task...","timestamp":1730331610000}
{"type":"tool_use","name":"Task","input":{"subagent_type":"architecture-reviewer","prompt":"..."}}
{"type":"tool_result","tool_use_id":"xyz","content":"Agent output..."}
```

### Historical Data
**Path**: `/home/node/.config/history.jsonl`

**Format**: High-level history entries with display summaries

## Error Handling

**If session ID unknown**:
```bash
# Fallback: Use most recent/largest file (current session)
CURRENT_SESSION=$(ls -lhS /home/node/.config/projects/-workspace/*.jsonl | \
  head -1 | awk '{print $NF}')
```

**If file not readable**:
- Check permissions: `ls -la /home/node/.config/projects/-workspace/`
- Verify path: `realpath ~/.claude/projects/`
- Fallback: Use conversation context from current Claude Code API if available

## Security Considerations

- Conversation files may contain sensitive data (code, credentials mentions, paths)
- Only process-recorder and audit agents should access raw conversation
- Do not log or store conversation content outside audit workflow
- Verify conversation file belongs to current project before parsing

## Output Format for Audits

**Structured JSON Output**:
```json
{
  "session_id": "fa3f1ca8-903e-4253-baf8-30416279a7e0",
  "conversation_file": "/home/node/.config/projects/-workspace/fa3f1ca8-903e-4253-baf8-30416279a7e0.jsonl",
  "entry_count": 1543,
  "user_messages": 27,
  "assistant_messages": 26,
  "tool_uses": {
    "Task": 11,
    "Edit": 26,
    "Write": 62,
    "Bash": 221,
    "Read": 52
  },
  "user_approvals": [
    {
      "timestamp": 1730331800000,
      "content": "yes, proceed",
      "context": "after SYNTHESIS state"
    }
  ],
  "working_directory_violations": [
    {
      "timestamp": 1730335200000,
      "tool": "Edit",
      "file": "FormattingViolationTest.java",
      "pwd": "/workspace/tasks/implement-formatter-api/agents/architecture-updater/code",
      "violation": "Main agent editing in agent worktree"
    }
  ],
  "agent_invocations": [
    {
      "timestamp": 1730331900000,
      "agent": "architecture-reviewer",
      "state": "CLASSIFIED",
      "output_verified": true
    }
  ]
}
```

## Integration with Existing Skills

**Complements learn-from-mistakes**:
- read-conversation-history: Provides raw conversation data
- learn-from-mistakes: Analyzes mistakes and recommends fixes
- Used together: Complete audit and improvement cycle

**Enables Independent Verification**:
- Main agent cannot filter/sanitize conversation
- Audit agents get unbiased, complete conversation history
- Prevents false negatives in compliance audits
