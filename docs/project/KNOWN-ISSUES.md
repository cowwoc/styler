# Known Issues

## Hook System Not Blocking Tool Invocations

**Status**: Open  
**Severity**: Critical  
**Discovered**: 2025-10-31

### Issue

PreToolUse hooks are logging violations but not blocking tool execution:
- `validate-agent-invocation.sh` logs violations to agent-invocation-violations.log
- Hook exits with code 2 (block)
- Tool invocation proceeds anyway

### Evidence

```bash
# Violation logged at 20:40
$ cat /workspace/tasks/implement-formatter-api/agent-invocation-violations.log
[2025-10-31T20:40:11-04:00] BLOCKED: architect invocation in INIT state

# But Task tool invocation succeeded despite hook exit code 2
```

### Impact

All safety hooks are ineffective:
- Protocol phase enforcement not working
- Agent invocation validation not blocking
- State transition guards not preventing violations

### Root Cause

Unknown - requires investigation:
- Are PreToolUse hooks receiving correct JSON?
- Is Claude Code honoring exit code 2?
- Are matchers working correctly?

### Next Steps

1. Register debug hook to capture actual JSON from Claude Code
2. Verify hook exit codes are being respected
3. Test with simpler hook that always blocks
4. Contact Claude Code support if infrastructure issue
