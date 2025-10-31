# Automatic Learn-From-Mistakes Invocation

**Purpose**: Automatically detect mistakes during execution and prompt the agent to invoke the learn-from-mistakes skill for root cause analysis.

**Location**: `.claude/hooks/auto-learn-from-mistakes.sh`

**Trigger**: PostToolUse (all tools)

## Overview

The auto-learn-from-mistakes hook monitors all tool results for error patterns and automatically suggests invoking the learn-from-mistakes skill when significant mistakes are detected. This creates a proactive learning loop that helps prevent recurring mistakes.

## Detected Mistake Patterns

### 1. Build Failures (CRITICAL)
**Pattern**: `BUILD FAILURE`, `COMPILATION ERROR`, `compilation failure`

**Example**:
```
[ERROR] COMPILATION ERROR:
[ERROR] /path/to/File.java:[42,5] cannot find symbol
```

**Action**: Immediately suggests learn-from-mistakes invocation to analyze:
- Why did the build fail?
- Was this a missing dependency, syntax error, or API misuse?
- How can we prevent similar failures?

### 2. Test Failures (CRITICAL)
**Pattern**: `Tests run: X Failures: Y`, `test.*failed`

**Example**:
```
Tests run: 15, Failures: 3, Errors: 0, Skipped: 0
```

**Action**: Suggests root cause analysis:
- What assumptions were incorrect?
- Are tests properly isolated?
- Is this a regression or new failure?

### 3. Protocol Violations (CRITICAL)
**Pattern**: `PROTOCOL VIOLATION`, `🚨.*VIOLATION`

**Example**:
```
🚨 PROTOCOL VIOLATION: Main agent created source file during IMPLEMENTATION state
```

**Action**: Triggers deep analysis:
- Why was the protocol violated?
- Was the guidance unclear?
- Should hooks be enhanced?

### 4. Merge Conflicts (HIGH)
**Pattern**: `CONFLICT`, `merge conflict`

**Example**:
```
CONFLICT (content): Merge conflict in src/main/java/Foo.java
```

**Action**: Analyzes conflict causes:
- Were branches properly synchronized?
- Should merge strategy be different?
- Can conflicts be prevented?

### 5. High-Volume Quality Violations (MEDIUM)
**Pattern**: More than 5 checkstyle or PMD violations

**Example**:
```
You have 12 Checkstyle violations
```

**Action**: When violations exceed threshold, suggests pattern analysis:
- Are these new or legacy violations?
- Is there a systemic style issue?
- Should auto-formatters be configured?

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

## Integration with learn-from-mistakes Skill

The hook complements the learn-from-mistakes skill:

- **Hook**: Detects mistakes automatically during execution
- **Skill**: Performs deep root cause analysis and prevention planning
- **Together**: Creates a continuous learning loop

### Manual Invocation Still Supported

You can still manually invoke learn-from-mistakes at any time:

```
Skill: learn-from-mistakes

Context: [Describe the mistake or pattern observed]

Please perform root cause analysis.
```

The automatic detection is supplemental, not a replacement for manual analysis.

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

### When to Invoke learn-from-mistakes

**DO invoke when**:
- Multiple similar mistakes occur (pattern detected)
- Critical mistakes block progress
- Protocol violations happen despite hooks
- Root cause is unclear

**DON'T invoke when**:
- Mistake is a simple typo (fix directly)
- Root cause is obvious (fix and move on)
- Already analyzing a recent mistake (avoid parallel analyses)

### Leveraging Automatic Detection

1. **Pay attention to prompts**: When the hook suggests invocation, consider doing it
2. **Review the log periodically**: Look for patterns in `/tmp/mistake-detection-log.json`
3. **Adjust thresholds**: If you get too many/few prompts, tune the rate limit
4. **Enhance patterns**: Add project-specific error patterns to the hook

## Example Workflow

### Scenario: Build Failure Detected

1. **Developer runs build**:
   ```bash
   ./mvnw clean compile
   ```

2. **Build fails** with compilation error

3. **Hook detects failure** and outputs:
   ```
   📚 MISTAKE DETECTED: build_failure

   Recommendation: Invoke learn-from-mistakes skill...
   ```

4. **Agent invokes skill**:
   ```
   Skill: learn-from-mistakes

   Context: Build failed with "cannot find symbol: NodeIndex"

   Please analyze root cause.
   ```

5. **Skill analyzes**:
   - Root cause: AST core module-info.java missing
   - Contributing factor: Module wasn't exporting package
   - Prevention: Add pre-commit hook to verify module exports

6. **Prevention measures applied**:
   - Create module-info.java for AST core
   - Update build to validate module structure
   - Document JPMS requirements

7. **Future builds**: Similar mistakes prevented

## See Also

- [learn-from-mistakes skill](../../.claude/skills/learn-from-mistakes/SKILL.md)
- [Multi-Agent Process Governance](multi-agent-process-governance.md)
- [Task Protocol](task-protocol-core.md)
