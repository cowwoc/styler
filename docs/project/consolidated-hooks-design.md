# Consolidated Hooks Architecture Design

> **Purpose**: Design for consolidated protocol enforcement hooks based on [protocol-scope-specification.md](protocol-scope-specification.md)

## Overview

This design consolidates 6 existing hooks into 2 streamlined hooks:

**Consolidation Targets**:
- `session-start-role-reminder.sh` (NEW, added in protocol improvements)
- `task-protocol-reminder.sh` (existing SessionStart hook)
- `detect-main-agent-implementation.sh` (existing PreToolUse hook)
- `validate-lock-location.sh` (existing PreToolUse hook for task.json)
- Partial: `state-transition-detector.sh` (only protocol-scope enforcement logic)
- Partial: `validate-agent-invocation.sh` (only scope validation)

**New Consolidated Hooks**:
1. `protocol-and-role-reminder.sh` - SessionStart guidance
2. `require-task-protocol.sh` - PreToolUse enforcement

## Hook 1: protocol-and-role-reminder.sh

**Trigger**: SessionStart

**Purpose**: Provide unified protocol and role guidance on every session start/resume

**Consolidates**:
- `session-start-role-reminder.sh` (role boundaries)
- `task-protocol-reminder.sh` (protocol requirements)

**Output Format**:
```
╔═══════════════════════════════════════════════════════════════╗
║  TASK PROTOCOL & MAIN AGENT ROLE                              ║
╟───────────────────────────────────────────────────────────────╢
║  PROTOCOL SCOPE                                               ║
║  • Todo.md tasks → MUST use task protocol                     ║
║  • Ad-hoc work → Protocol NOT required                        ║
║  • See: protocol-scope-specification.md for full rules        ║
╟───────────────────────────────────────────────────────────────╢
║  MAIN AGENT ROLE BOUNDARIES                                   ║
║  ✅ CAN: Coordinate, configure, document, git operations      ║
║  ❌ CANNOT: Create .java/.ts/.py in IMPLEMENTATION state      ║
║           → Delegate to stakeholder agents instead            ║
╚═══════════════════════════════════════════════════════════════╝
```

**Implementation Strategy**:
```bash
#!/bin/bash
set -euo pipefail

# Unified guidance message
cat << 'EOF'
╔═══════════════════════════════════════════════════════════════╗
║  TASK PROTOCOL & MAIN AGENT ROLE                              ║
╟───────────────────────────────────────────────────────────────╢
║  PROTOCOL SCOPE                                               ║
║  • Todo.md tasks → MUST use task protocol                     ║
║  • Ad-hoc work → Protocol NOT required                        ║
║  • See: protocol-scope-specification.md for full rules        ║
╟───────────────────────────────────────────────────────────────╢
║  MAIN AGENT ROLE BOUNDARIES                                   ║
║  ✅ CAN: Coordinate, configure, document, git operations      ║
║  ❌ CANNOT: Create .java/.ts/.py in IMPLEMENTATION state      ║
║           → Delegate to stakeholder agents instead            ║
╚═══════════════════════════════════════════════════════════════╝
EOF

exit 0
```

**Registration** (in `.claude/settings.json`):
```json
"SessionStart": [
  {
    "hooks": [
      {
        "type": "command",
        "command": "/workspace/.claude/hooks/protocol-and-role-reminder.sh"
      }
    ]
  }
]
```

**Replaces**:
- `/workspace/.claude/hooks/session-start-role-reminder.sh` (DELETE)
- `/workspace/.claude/hooks/task-protocol-reminder.sh` (DELETE)

## Hook 2: require-task-protocol.sh

**Trigger**: PreToolUse

**Purpose**: Enforce protocol-scope-specification.md rules - prevent Category B work without task protocol initialization

**Consolidates**:
- `detect-main-agent-implementation.sh` (main agent source file creation detection)
- `validate-lock-location.sh` (task.json location validation)
- Scope enforcement logic from other hooks

**Matcher**:
```json
{
  "matcher": "(tool:Write || tool:Edit) && path:/workspace/tasks/**"
}
```

**Enforcement Logic Flow**:

```
1. Extract file path and operation details
   ↓
2. Determine if path is under /workspace/tasks/
   ↓ NO → Allow (root workspace edits don't require protocol)
   ↓ YES
   ↓
3. Extract task name from path
   ↓
4. Check if task.json exists at /workspace/tasks/{task-name}/task.json
   ↓ YES → Protocol initialized, continue to Category validation
   ↓ NO → Check file category
   ↓
5. Apply Category A/B rules:
   ├─ Category A (Allowed WITHOUT protocol):
   │  • Documentation: *.md (except task.md)
   │  • Hooks: .claude/hooks/**
   │  • Scripts: scripts/**
   │  • Build: **/pom.xml, build.*, .mvn/**, mvnw*
   │  • Git: .gitignore, .gitattributes
   │  ↓ MATCH → Allow
   │
   ├─ Category B (REQUIRES protocol):
   │  • Source code: **/*.{java,ts,py,js,go,rs,cpp,c,h,hpp}
   │  • Tests: **/test/**/*.{java,ts,py,js}
   │  • Module structure: src/**, module-info.java
   │  • Implementation studies: docs/studies/*.md matching todo.md tasks
   │  ↓ MATCH → Block with error message
   │
   └─ Unknown/Edge Case → Allow with warning
   ↓
6. If protocol initialized (task.json exists):
   ├─ Read task.json state
   ├─ If state == IMPLEMENTATION && agent == main && file matches source patterns:
   │  └─ Block: VIOLATION #1 (main agent source creation)
   ├─ If writing task.json at non-standard location:
   │  └─ Block: task.json must be at /workspace/tasks/{task-name}/task.json
   └─ Otherwise allow
```

**File Pattern Matching**:

```bash
# Category A patterns (non-protocol)
CATEGORY_A_PATTERNS=(
  "*.md"                          # Documentation (except task.md, handled specially)
  ".claude/hooks/**"              # Hook scripts
  "scripts/**"                    # Utility scripts
  "**/pom.xml"                    # Build files (ALL pom.xml per user clarification)
  "build.*"                       # Build configs
  ".mvn/**"                       # Maven wrapper
  "mvnw*"                         # Maven wrapper scripts
  ".gitignore"                    # Git configs
  ".gitattributes"
)

# Category B patterns (protocol-required)
CATEGORY_B_PATTERNS=(
  "**/*.java"
  "**/*.ts"
  "**/*.py"
  "**/*.js"
  "**/*.go"
  "**/*.rs"
  "**/*.cpp"
  "**/*.c"
  "**/*.h"
  "**/*.hpp"
  "**/test/**"                    # Test directories
  "src/**"                        # Source directories
  "module-info.java"
)

# Special case: task.md ALWAYS requires protocol
if [[ "$filename" == "task.md" ]]; then
  CATEGORY_B=true
fi

# Special case: studies require protocol if they match todo.md task names
if [[ "$path" == docs/studies/*.md ]]; then
  # Extract study topic and check against todo.md tasks
  # Implementation in next section
fi
```

**Study Validation Logic**:

```bash
# Check if study filename matches any todo.md task
is_study_for_implementation() {
  local study_path="$1"
  local study_name=$(basename "$study_path" .md)

  # Read todo.md and extract task names
  if [[ ! -f "/workspace/main/todo.md" ]]; then
    return 1  # No todo.md, allow study (exploratory)
  fi

  # Check if any todo.md task contains study topic
  # Match pattern: - [ ] taskname or - [x] taskname
  if grep -qE "^- \[[ x]\] .*${study_name}" /workspace/main/todo.md; then
    return 0  # Study matches task, requires protocol
  fi

  return 1  # Exploratory study, no protocol required
}
```

**Error Messages**:

```bash
# Category B work without protocol
cat << EOF
❌ PROTOCOL REQUIRED

You are attempting to modify:
  $file_path

This is Category B work (requires task protocol):
  Pattern: $matched_pattern
  Reason: $category_reason

✅ REQUIRED ACTIONS:
1. Initialize task protocol with task.json
2. Follow state machine (INIT → CLASSIFIED → ...)
3. Create agent worktrees for stakeholder agents

📖 See: protocol-scope-specification.md for complete rules
📖 See: main-agent-coordination.md for state machine

🔧 Initialize protocol:
   Write task.json to: /workspace/tasks/{task-name}/task.json
EOF
```

```bash
# Main agent source file creation (VIOLATION #1)
cat << EOF
❌ CRITICAL VIOLATION: Main Agent Source File Creation

Current state: IMPLEMENTATION
Agent: main
File: $file_path
Operation: $tool

🚫 VIOLATION #1: Main agent CANNOT create source files in IMPLEMENTATION state

✅ CORRECT APPROACH:
1. Delegate to stakeholder agents via Task tool
2. Agents work in isolated worktrees: /workspace/tasks/{task}/agents/{agent}/code
3. Merge agent branches to task branch
4. Main agent coordinates, does NOT implement

📖 See: CLAUDE.md § Critical Protocol Violations
📖 See: main-agent-coordination.md § Source Code Creation Decision Tree
EOF
```

**Implementation Pseudocode**:

```bash
#!/bin/bash
set -euo pipefail

trap 'echo "ERROR in require-task-protocol.sh at line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper library
source /workspace/main/.claude/hooks/lib/pattern-matcher.sh

# Extract tool use details from stdin/env
FILE_PATH="${TOOL_PATH:-}"
TOOL_NAME="${TOOL_NAME:-}"

# Only enforce for /workspace/tasks/** paths
if [[ ! "$FILE_PATH" =~ ^/workspace/tasks/ ]]; then
  exit 0  # Allow root workspace edits
fi

# Extract task name
TASK_NAME=$(echo "$FILE_PATH" | sed -n 's|^/workspace/tasks/\([^/]*\)/.*|\1|p')

# Check protocol initialization
TASK_JSON="/workspace/tasks/$TASK_NAME/task.json"

if [[ ! -f "$TASK_JSON" ]]; then
  # No protocol initialized - apply Category A/B rules

  # Check Category A (allowed)
  if match_category_a "$FILE_PATH"; then
    exit 0  # Allow
  fi

  # Check Category B (blocked)
  if match_category_b "$FILE_PATH"; then
    echo "❌ PROTOCOL REQUIRED" >&2
    show_category_b_error "$FILE_PATH"
    exit 1  # Block
  fi

  # Unknown - allow with warning
  echo "⚠️ WARNING: Unrecognized file pattern: $FILE_PATH" >&2
  exit 0
fi

# Protocol initialized - check for VIOLATION #1
STATE=$(jq -r '.state' "$TASK_JSON")
AGENT_TYPE="${CURRENT_AGENT_TYPE:-main}"

if [[ "$STATE" == "IMPLEMENTATION" ]] && [[ "$AGENT_TYPE" == "main" ]]; then
  if match_source_pattern "$FILE_PATH"; then
    echo "❌ CRITICAL VIOLATION: Main Agent Source File Creation" >&2
    show_violation_1_error "$FILE_PATH" "$TOOL_NAME"
    exit 1  # Block
  fi
fi

# Check task.json location (must be at task root)
if [[ "$FILE_PATH" =~ task\.json$ ]] && [[ "$FILE_PATH" != "$TASK_JSON" ]]; then
  echo "❌ Invalid task.json location" >&2
  echo "Expected: $TASK_JSON" >&2
  echo "Actual: $FILE_PATH" >&2
  exit 1  # Block
fi

# All checks passed
exit 0
```

**Helper Library: pattern-matcher.sh**:

```bash
# /workspace/main/.claude/hooks/lib/pattern-matcher.sh

# Match Category A patterns (non-protocol work)
match_category_a() {
  local path="$1"
  local filename=$(basename "$path")

  # Documentation (except task.md)
  if [[ "$path" =~ \.md$ ]] && [[ "$filename" != "task.md" ]]; then
    # Exception: studies matching todo.md tasks require protocol
    if [[ "$path" =~ ^docs/studies/ ]]; then
      if is_study_for_implementation "$path"; then
        return 1  # Not Category A
      fi
    fi
    return 0  # Category A
  fi

  # Hooks
  if [[ "$path" =~ \.claude/hooks/ ]]; then
    return 0
  fi

  # Scripts
  if [[ "$path" =~ ^scripts/ ]]; then
    return 0
  fi

  # Build files (ALL pom.xml per user clarification)
  if [[ "$filename" == "pom.xml" ]]; then
    return 0
  fi

  if [[ "$path" =~ ^(build\.|\.mvn/|mvnw) ]]; then
    return 0
  fi

  # Git configs
  if [[ "$filename" =~ ^\.git(ignore|attributes)$ ]]; then
    return 0
  fi

  return 1  # Not Category A
}

# Match Category B patterns (protocol-required)
match_category_b() {
  local path="$1"
  local filename=$(basename "$path")

  # task.md always requires protocol
  if [[ "$filename" == "task.md" ]]; then
    return 0
  fi

  # Source files
  if [[ "$path" =~ \.(java|ts|py|js|go|rs|cpp|c|h|hpp)$ ]]; then
    return 0
  fi

  # Test directories
  if [[ "$path" =~ /test/ ]]; then
    return 0
  fi

  # Source directories
  if [[ "$path" =~ /src/ ]]; then
    return 0
  fi

  # Module descriptors
  if [[ "$filename" == "module-info.java" ]]; then
    return 0
  fi

  # Implementation studies
  if [[ "$path" =~ ^docs/studies/ ]]; then
    if is_study_for_implementation "$path"; then
      return 0
    fi
  fi

  return 1  # Not Category B
}

# Match source code patterns (for VIOLATION #1 detection)
match_source_pattern() {
  local path="$1"

  if [[ "$path" =~ \.(java|ts|py|js|go|rs|cpp|c|h|hpp)$ ]]; then
    return 0
  fi

  return 1
}

# Check if study is for implementation planning
is_study_for_implementation() {
  local study_path="$1"
  local study_name=$(basename "$study_path" .md)

  if [[ ! -f "/workspace/main/todo.md" ]]; then
    return 1
  fi

  # Match todo.md task patterns
  if grep -qE "^- \[[ x]\] .*${study_name}" /workspace/main/todo.md; then
    return 0
  fi

  return 1
}
```

**Registration** (in `.claude/settings.json`):

```json
"PreToolUse": [
  {
    "matcher": "(tool:Write || tool:Edit) && path:/workspace/tasks/**",
    "hooks": [
      {
        "type": "command",
        "command": "/workspace/.claude/hooks/require-task-protocol.sh"
      }
    ]
  }
]
```

**Replaces**:
- `/workspace/.claude/hooks/detect-main-agent-implementation.sh` (DELETE)
- `/workspace/.claude/hooks/validate-lock-location.sh` (DELETE)
- Scope enforcement portions of other hooks (UPDATE to remove duplicated logic)

## Hook Registration Plan

### Settings.json Changes

**REMOVE from SessionStart**:
```json
{
  "type": "command",
  "command": "/workspace/.claude/hooks/session-start-role-reminder.sh"
},
{
  "type": "command",
  "command": "/workspace/.claude/hooks/task-protocol-reminder.sh"
}
```

**ADD to SessionStart**:
```json
{
  "type": "command",
  "command": "/workspace/.claude/hooks/protocol-and-role-reminder.sh"
}
```

**REMOVE from PreToolUse**:
```json
{
  "matcher": "(tool:Write || tool:Edit) && path:**/*.{java,ts,py,js,go,rs,cpp,c,h}",
  "hooks": [
    {
      "type": "command",
      "command": "/workspace/.claude/hooks/detect-main-agent-implementation.sh"
    }
  ]
},
{
  "matcher": "tool:Write && path:**/task.json",
  "hooks": [
    {
      "type": "command",
      "command": "/workspace/.claude/hooks/validate-lock-location.sh"
    }
  ]
}
```

**ADD to PreToolUse**:
```json
{
  "matcher": "(tool:Write || tool:Edit) && path:/workspace/tasks/**",
  "hooks": [
    {
      "type": "command",
      "command": "/workspace/.claude/hooks/require-task-protocol.sh"
    }
  ]
}
```

### File Deletion Plan

After consolidation complete and tested:

```bash
# Delete consolidated hooks
rm /workspace/main/.claude/hooks/session-start-role-reminder.sh
rm /workspace/main/.claude/hooks/task-protocol-reminder.sh
rm /workspace/main/.claude/hooks/detect-main-agent-implementation.sh
rm /workspace/main/.claude/hooks/validate-lock-location.sh
```

## Testing Plan

### Test Cases for protocol-and-role-reminder.sh

1. **SessionStart trigger**: Verify message displays on session start
2. **Message format**: Verify box formatting renders correctly
3. **No errors**: Hook exits 0, doesn't block session

### Test Cases for require-task-protocol.sh

**Category A (Should Allow)**:
1. Edit `/workspace/tasks/test-task/code/README.md` - Documentation
2. Edit `/workspace/tasks/test-task/code/.claude/hooks/test.sh` - Hook script
3. Edit `/workspace/tasks/test-task/code/pom.xml` - Build file (root)
4. Edit `/workspace/tasks/test-task/code/formatter/pom.xml` - Build file (module)
5. Edit `/workspace/tasks/test-task/code/.gitignore` - Git config
6. Write `/workspace/tasks/test-task/code/docs/studies/exploration.md` - Exploratory study (not in todo.md)

**Category B (Should Block WITHOUT task.json)**:
1. Write `/workspace/tasks/test-task/code/src/main/java/Test.java` - Source code
2. Write `/workspace/tasks/test-task/code/src/test/java/TestTest.java` - Test code
3. Write `/workspace/tasks/test-task/code/module-info.java` - Module descriptor
4. Write `/workspace/tasks/test-task/task.md` - Task requirements
5. Write `/workspace/tasks/test-task/code/docs/studies/implement-api.md` - Implementation study (if "implement-api" in todo.md)

**Category B (Should Allow WITH task.json, state != IMPLEMENTATION)**:
1. Create `/workspace/tasks/test-task/task.json` with state: CLASSIFIED
2. Write `/workspace/tasks/test-task/code/src/main/java/Test.java` - Should allow (main agent can create files in non-IMPLEMENTATION states)

**VIOLATION #1 Detection (Should Block)**:
1. Create task.json with state: IMPLEMENTATION
2. Set CURRENT_AGENT_TYPE=main
3. Attempt Write `/workspace/tasks/test-task/code/src/main/java/Test.java`
4. Should block with VIOLATION #1 error

**task.json Location Validation (Should Block)**:
1. Write `/workspace/tasks/test-task/code/task.json` - Wrong location
2. Should block, require `/workspace/tasks/test-task/task.json`

**Root Workspace (Should Always Allow)**:
1. Write `/workspace/main/docs/project/test.md` - Not under /workspace/tasks/
2. Should allow regardless of protocol

## Implementation Steps

1. ✅ Create design document (this file)
2. Create helper library: `lib/pattern-matcher.sh`
3. Create hook: `protocol-and-role-reminder.sh`
4. Create hook: `require-task-protocol.sh`
5. Update `.claude/settings.json` with new registrations
6. Test all test cases
7. Remove old hooks after validation
8. Commit consolidated hooks

## Migration Notes

**Backward Compatibility**: The new hooks are MORE permissive than old hooks in some cases:
- Old: `detect-main-agent-implementation.sh` only checked if task.json existed (if not, allowed all)
- New: `require-task-protocol.sh` blocks Category B work even without task.json

**Breaking Change**: After deployment, main agent CANNOT create source files in `/workspace/tasks/**/code/` without task.json, even outside IMPLEMENTATION state. This is CORRECT per protocol-scope-specification.md.

**Rollback Plan**: If issues arise:
1. Restore old hook registrations in settings.json
2. Keep old hook files until confidence in new system
3. Use git revert to restore previous state

## Performance Considerations

**Pattern Matching**: Bash pattern matching with case statements is fast for small pattern sets
**File I/O**: Only reads task.json when path is under /workspace/tasks/
**Grep Performance**: todo.md grep only runs for docs/studies/*.md paths

**Expected Hook Execution Time**: <50ms per tool use

## Future Enhancements

1. **Cache todo.md parsing**: Read once per session instead of per-hook-call
2. **Structured logging**: JSON log output for audit trails
3. **Hook metrics**: Track block rate, allow rate, pattern match performance
4. **Auto-correction suggestions**: "Did you mean to initialize task protocol? Run: [command]"

---

**Design Status**: ✅ COMPLETE - Ready for implementation
**Next Step**: Implement `lib/pattern-matcher.sh` helper library
