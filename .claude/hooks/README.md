# Claude Code Hooks Registry

**Last Updated**: 2025-10-16
**Purpose**: Central registry of all safety and automation hooks with registration requirements

---

## 🚨 CRITICAL: Hook Registration Requirement

**Hooks MUST be registered in `.claude/settings.json` to function.**

Simply placing a hook file in `/workspace/.claude/hooks/` does NOT activate it. Each hook requires explicit
registration under the appropriate trigger event in `settings.json`.

**Verification Command**:
```bash
# List all registered hooks
jq '.hooks' /workspace/.claude/settings.json
```

---

## Hook Trigger Events

| Event Type | When It Fires | Use Cases |
|------------|---------------|-----------|
| **SessionStart** | Once at session initialization | Context loading, lock checking, protocol reminders |
| **UserPromptSubmit** | Before processing user message | Input validation, state checks, reminders |
| **PreToolUse** | Before ANY tool execution | Safety checks, permission verification, blocking |
| **PostToolUse** | After tool completes | Verification, cleanup, documentation sync |
| **PreCompact** | Before context compaction | Critical state persistence |

---

## Registered Hooks

### SessionStart Hooks

#### ensure-session-id.py
- **Purpose**: Generate and inject unique session ID
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO
- **Status**: ✅ REGISTERED

#### check-lock-ownership.sh
- **Purpose**: Detect active tasks owned by current session after compaction
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO (informational)
- **Status**: ✅ REGISTERED

#### task-protocol-reminder.sh
- **Purpose**: Inject task protocol file paths into context
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO
- **Status**: ✅ REGISTERED

#### todo-context-reminder.sh
- **Purpose**: Inject TODO list synchronization reminders
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO
- **Status**: ✅ REGISTERED

#### critical-thinking.sh
- **Purpose**: Inject evidence-based critical thinking requirements
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO
- **Status**: ✅ REGISTERED

---

### UserPromptSubmit Hooks

#### detect-giving-up.sh
- **Purpose**: Detect mid-protocol abandonment patterns
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO (reminder only)
- **Status**: ✅ REGISTERED

#### verify-destructive-operations.sh
- **Purpose**: Verify user intent for destructive operations
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO
- **Status**: ✅ REGISTERED

#### smart-doc-prompter.sh
- **Purpose**: Inject style validation checklist
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO
- **Status**: ✅ REGISTERED

---

### PreToolUse Hooks (Safety-Critical)

#### detect-main-agent-implementation.sh
- **Purpose**: **BLOCK** Write/Edit of source files during IMPLEMENTATION state
- **Trigger**: PreToolUse
- **Matcher**: `(tool:Write || tool:Edit) && path:**/*.{java,ts,py,js,go,rs,cpp,c,h}`
- **Registration**: ✅ REQUIRED
- **Blocking**: **YES** (returns exit 1 to prevent operation)
- **Status**: ✅ REGISTERED
- **Verification**:
  ```bash
  jq '.hooks.PreToolUse[] | select(.hooks[].command | contains("detect-main-agent-implementation"))' /workspace/.claude/settings.json
  ```
- **Test Case**:
  ```bash
  # Simulate Write tool during IMPLEMENTATION state
  echo '{"tool_name": "Write", "parameters": {"file_path": "/workspace/tasks/test/code/src/Test.java"}}' | \
    .claude/hooks/detect-main-agent-implementation.sh
  # Expected: Exit 1 with violation message (if task in IMPLEMENTATION state)
  ```

#### block-data-loss.sh
- **Purpose**: **BLOCK** destructive git/rm operations without verification
- **Trigger**: PreToolUse
- **Matcher**: `tool:Bash && (command:*git* || command:*rm*)`
- **Registration**: ✅ REQUIRED
- **Blocking**: **YES**
- **Status**: ✅ REGISTERED

#### detect-worktree-violation.sh
- **Purpose**: **BLOCK** cd commands that violate worktree isolation
- **Trigger**: PreToolUse
- **Matcher**: `tool:Bash && command:*cd*`
- **Registration**: ✅ REQUIRED
- **Blocking**: **YES**
- **Status**: ✅ REGISTERED

#### load-todo.sh
- **Purpose**: Load TODO list context before tool execution
- **Trigger**: PreToolUse
- **Matcher**: (unconditional)
- **Registration**: ✅ REQUIRED
- **Blocking**: NO
- **Status**: ✅ REGISTERED

---

### PostToolUse Hooks

#### verify-doc-sync.sh
- **Purpose**: Verify documentation synchronization after doc edits
- **Trigger**: PostToolUse
- **Matcher**: `(tool:Write || tool:Edit || tool:MultiEdit) && path:docs/`
- **Registration**: ✅ REQUIRED
- **Blocking**: NO (verification only)
- **Status**: ✅ REGISTERED

#### detect-meta-commentary.sh
- **Purpose**: Detect prohibited meta-commentary in documentation
- **Trigger**: PostToolUse
- **Matcher**: `(tool:Write || tool:Edit || tool:MultiEdit) && path:docs/`
- **Registration**: ✅ REQUIRED
- **Blocking**: NO
- **Status**: ✅ REGISTERED

#### verify-task-archival.sh
- **Purpose**: Verify task archival follows CLAUDE.md requirements
- **Trigger**: PostToolUse
- **Matcher**: `(tool:Edit && path:todo.md) || (tool:Edit && path:changelog.md)`
- **Registration**: ✅ REQUIRED
- **Blocking**: NO
- **Status**: ✅ REGISTERED

---

### PreCompact Hooks

#### critical-thinking.sh
- **Purpose**: Re-inject critical thinking requirements before context loss
- **Registration**: REQUIRED (unconditional)
- **Blocking**: NO
- **Status**: ✅ REGISTERED

---

## Hook Development Guidelines

### Creating a New Hook

1. **Choose Trigger Event**:
   - PreToolUse: Safety-critical blocking operations
   - PostToolUse: Verification after tool completes
   - SessionStart: One-time context loading
   - UserPromptSubmit: Input validation
   - PreCompact: Pre-compaction persistence

2. **Write Hook Script**:
   ```bash
   #!/bin/bash
   # Hook: my-hook.sh
   # Trigger: PreToolUse (must be registered in .claude/settings.json)
   # Matcher: tool:Write && path:**/*.java
   # Purpose: Block unauthorized Java file writes

   set -euo pipefail

   # Read JSON input
   INPUT=$(cat)

   # Parse parameters
   TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
   FILE_PATH=$(echo "$INPUT" | jq -r '.parameters.file_path // empty')

   # Implement logic
   if [[ condition ]]; then
     echo "ERROR: Violation detected"
     exit 1  # BLOCK operation
   fi

   exit 0  # Allow operation
   ```

3. **Register in settings.json**:
   ```json
   {
     "hooks": {
       "PreToolUse": [
         {
           "matcher": "tool:Write && path:**/*.java",
           "hooks": [{
             "type": "command",
             "command": "/workspace/.claude/hooks/my-hook.sh"
           }]
         }
       ]
     }
   }
   ```

4. **Test Hook**:
   ```bash
   # Simulate tool invocation
   echo '{"tool_name": "Write", "parameters": {"file_path": "/test.java"}}' | \
     .claude/hooks/my-hook.sh

   # Verify exit code
   echo $?  # 0 = allow, 1 = block
   ```

5. **Document in README.md**:
   - Add entry to appropriate section
   - Include purpose, matcher, blocking status
   - Provide test case

### Exit Code Conventions

- **0**: Allow operation to proceed
- **1**: Block operation (PreToolUse hooks only)
- **Non-zero**: Error in hook execution (operation blocked)

### JSON Input Format

**PreToolUse/PostToolUse**:
```json
{
  "tool_name": "Write",
  "parameters": {
    "file_path": "/workspace/test.java",
    "content": "..."
  }
}
```

**SessionStart/UserPromptSubmit/PreCompact**:
```json
{
  "message": "user message text",
  "context": {...}
}
```

---

## Verification Checklist

Before starting any HIGH-RISK task, verify critical hooks are registered:

```bash
# 1. Verify main agent implementation hook
jq '.hooks.PreToolUse[] | select(.hooks[].command | contains("detect-main-agent-implementation"))' \
  /workspace/.claude/settings.json

# Expected: Hook configuration object
# If empty: CRITICAL - NO PROTECTION ACTIVE

# 2. Verify data loss protection
jq '.hooks.PreToolUse[] | select(.hooks[].command | contains("block-data-loss"))' \
  /workspace/.claude/settings.json

# 3. Verify worktree isolation
jq '.hooks.PreToolUse[] | select(.hooks[].command | contains("detect-worktree-violation"))' \
  /workspace/.claude/settings.json
```

**All three hooks MUST return configuration objects.**

---

## Troubleshooting

### Hook Not Firing

**Symptom**: Expected hook behavior not occurring

**Diagnosis**:
```bash
# Check if hook is registered
jq '.hooks' /workspace/.claude/settings.json | grep -A5 "my-hook.sh"

# Check hook file exists and is executable
ls -la /workspace/.claude/hooks/my-hook.sh
chmod +x /workspace/.claude/hooks/my-hook.sh

# Check matcher syntax
jq '.hooks.PreToolUse[] | .matcher' /workspace/.claude/settings.json
```

**Common Issues**:
- Hook file not in `/workspace/.claude/hooks/`
- Hook not registered in settings.json
- Matcher pattern doesn't match tool/path
- Hook file not executable
- Syntax error in hook script

### Hook Blocking Legitimate Operations

**Symptom**: Hook blocking operations that should be allowed

**Diagnosis**:
```bash
# Test hook with legitimate input
echo '{"tool_name": "Write", "parameters": {"file_path": "/workspace/test.java"}}' | \
  /workspace/.claude/hooks/my-hook.sh

# Check exit code
echo $?  # Should be 0 for legitimate operations
```

**Solution**:
- Review hook logic for false positives
- Adjust matcher to be more specific
- Add exception handling for edge cases

---

## References

- **CLAUDE.md § Implementation Role Boundaries**: Hook requirements for protocol enforcement
- **task-protocol-core.md**: Task protocol state requirements
- **settings.json**: Hook registration configuration

---

**Maintenance**: Update this registry when adding, removing, or modifying hooks. Keep verification commands
current.
