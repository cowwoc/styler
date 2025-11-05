# Automatic Learn-From-Mistakes Invocation

**Purpose**: Automatically detect mistakes during execution and prompt the agent to invoke the learn-from-mistakes skill for root cause analysis.

**Location**: `.claude/hooks/auto-learn-from-mistakes.sh`

**Trigger**: PostToolUse (all tools)

## Detected Mistake Patterns

### 1. Build Failures (CRITICAL)
**Pattern**: `BUILD FAILURE`, `COMPILATION ERROR`, `compilation failure`

**Example**:
```
[ERROR] COMPILATION ERROR:
[ERROR] /path/to/File.java:[42,5] cannot find symbol
```

### 2. Test Failures (CRITICAL)
**Pattern**: `Tests run: X Failures: Y`, `test.*failed`

**Example**:
```
Tests run: 15, Failures: 3, Errors: 0, Skipped: 0
```

### 3. Protocol Violations (CRITICAL)
**Pattern**: `PROTOCOL VIOLATION`, `🚨.*VIOLATION`

**Example**:
```
🚨 PROTOCOL VIOLATION: Main agent created source file during IMPLEMENTATION state
```

### 4. Merge Conflicts (HIGH)
**Pattern**: `CONFLICT`, `merge conflict`

**Example**:
```
CONFLICT (content): Merge conflict in src/main/java/Foo.java
```

### 5. High-Volume Quality Violations (MEDIUM)
**Pattern**: More than 5 checkstyle or PMD violations

**Example**:
```
You have 12 Checkstyle violations
```

## Rate Limiting

To avoid spam and alert fatigue, the hook implements rate limiting:

- **Threshold**: Maximum 1 trigger per 5 minutes
- **Tracking**: Logs mistakes to `/tmp/mistake-detection-log.json`
- **Filtering**: Only CRITICAL mistakes (build/test/protocol) trigger automatically
- **Cooldown**: Subsequent mistakes within 5 minutes are logged but don't trigger prompts

## Hook Behavior

### On Mistake Detection

When a significant mistake is detected, the hook:

1. **Logs the mistake** to `/tmp/mistake-detection-log.json`:
   ```json
   {
     "type": "build_failure",
     "tool": "Bash",
     "timestamp": "2025-10-31T01:00:00Z",
     "details": "BUILD FAILURE: cannot find symbol..."
   }
   ```

2. **Outputs the original tool result** (passes through unchanged)

3. **Displays a reminder** (to stderr) with suggested invocation:
   ```
   📚 MISTAKE DETECTED: build_failure

   A significant mistake was detected in the Bash tool result.

   **Recommendation**: Invoke the learn-from-mistakes skill:

   Skill: learn-from-mistakes

   Context: Detected build_failure during Bash execution.

   Details: BUILD FAILURE: cannot find symbol...

   Please analyze the root cause and recommend prevention measures.
   ```

### When No Mistake Detected

- Tool result passes through unchanged
- No logging or output
- Zero performance overhead

## Manual Invocation

Manual invocation supported anytime:

```
Skill: learn-from-mistakes

Context: [Describe the mistake or pattern observed]

Please perform root cause analysis.
```

## Configuration

### Adjusting Rate Limit

Edit `/workspace/main/.claude/hooks/auto-learn-from-mistakes.sh`:

```bash
# Change from 5 minutes to 10 minutes
RECENT_MISTAKES=$(jq --arg cutoff "$(date -d '10 minutes ago' -Iseconds)" ...)
```

### Adding Custom Patterns

Add new detection patterns in the hook script:

```bash
# Pattern 6: Custom error pattern
if echo "$TOOL_RESULT" | jq -r '.output' | grep -qi "YOUR_PATTERN"; then
  MISTAKE_TYPE="custom_error"
  MISTAKE_DETAILS=$(echo "$TOOL_RESULT" | jq -r '.output' | grep -A5 "YOUR_PATTERN")
fi
```

### Disabling Auto-Detection

Comment out or remove the hook registration in `.claude/settings.json`:

```json
"PostToolUse": [
  {
    "hooks": [
      // {
      //   "type": "command",
      //   "command": "/workspace/.claude/hooks/auto-learn-from-mistakes.sh"
      // }
    ]
  },
  ...
]
```

## Mistake Log Analysis

The mistake log at `/tmp/mistake-detection-log.json` can be analyzed for patterns:

### View Recent Mistakes
```bash
jq '.' /tmp/mistake-detection-log.json
```

### Count by Type
```bash
jq 'group_by(.type) | map({type: .[0].type, count: length})' /tmp/mistake-detection-log.json
```

### Mistakes in Last Hour
```bash
jq --arg cutoff "$(date -d '1 hour ago' -Iseconds)" \
  '[.[] | select(.timestamp > $cutoff)]' /tmp/mistake-detection-log.json
```

## Best Practices

**Invoke when**:
- Multiple similar mistakes (pattern detected)
- Critical mistakes block progress
- Protocol violations despite hooks
- Root cause unclear

**Skip when**:
- Simple typo (fix directly)
- Obvious root cause (fix and move on)
- Already analyzing recent mistake (avoid parallel analyses)

**Leverage automatic detection**:
1. Consider prompts when hook suggests invocation
2. Review log periodically: `/tmp/mistake-detection-log.json`
3. Adjust rate limit if too many/few prompts
4. Add project-specific error patterns

## See Also

- [learn-from-mistakes skill](../../.claude/skills/learn-from-mistakes/SKILL.md)
- [Multi-Agent Process Governance](multi-agent-process-governance.md)
- [Task Protocol](task-protocol-core.md)
