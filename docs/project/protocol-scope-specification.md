# Task Protocol Scope Specification

> **Version:** 1.0 | **Last Updated:** 2025-10-25
> **Purpose:** Define what work requires task protocol vs what can happen outside protocol
> **Enforcement:** Via `require-task-protocol.sh` PreToolUse hook

## Overview

Not all work requires the full task protocol. This document categorizes all work types to determine when task.json and the full protocol state machine are required.

## Category A: Configuration & Documentation Work {#non-protocol-work}

Work that does NOT require task.json or the full protocol state machine, but MUST be committed on the task
branch in a **separate commit before** the task implementation commit when done during a task.

**Commit Structure on Task Branch** (before user approval):
1. **Config/docs commit** (first): `.claude/`, `docs/project/`, `CLAUDE.md` changes
2. **Implementation commit** (second): Source code, tests, changelog, todo changes

**Squashing Rule**: During pre-presentation cleanup, squash into TWO commits (not one):
- Squash all config/docs changes into first commit
- Squash all implementation changes into second commit

### Documentation & Configuration

**Files**:
- `/workspace/main/CLAUDE.md`
- `/workspace/main/README.md`
- `/workspace/main/changelog.md`
- `/workspace/main/todo.md`
- `/workspace/main/docs/**/*.md` (all documentation)
- `/workspace/main/.editorconfig`
- `/workspace/main/.gitignore`
- `/workspace/main/.gitattributes`

### Hook Scripts & Automation

**Files**:
- `/workspace/main/.claude/hooks/**/*`
- `/workspace/main/.claude/settings.json`
- `/workspace/main/.claude/commands/**/*`
- `/workspace/main/.claude/agents/**/*`
- `/workspace/main/.claude/scripts/**/*`

### Build Infrastructure (Root Level)

**Files**:
- `/workspace/main/pom.xml` (root only, NOT module pom.xml)
- `/workspace/main/mvnw`, `/workspace/main/mvnw.cmd`
- `/workspace/main/.mvn/**/*`
- `/workspace/main/config/checkstyle.xml`
- `/workspace/main/config/pmd.xml`
- `/workspace/main/config/pmd-main.xml`

### Git Operations

**Operations**:
- `git commit`, `git push`, `git pull`
- `git branch`, `git checkout`, `git merge`
- `.git/` directory operations

### IDE & Personal Settings

**Files**:
- `/workspace/main/.idea/**/*`
- `/workspace/main/**/*.iml`
- Any IDE-specific configuration

### Audit & Meta-Protocol Activities

**Activities**:
- Running `/audit-session` slash command
- Applying protocol improvements from audit results
- Fixing protocol violations detected by audits
- Hook testing and debugging

### Research Documentation (Exploratory)

**Files**:
- `/workspace/main/docs/studies/**/*.md` (research only, see Category B for implementation)

**CRITICAL**: If study leads to implementation task, the IMPLEMENTATION requires task protocol.

## Category B: Protocol-Required Work (task.json REQUIRED) {#protocol-required-work}

Work that MUST use full task protocol with task.json, state transitions, and agent isolation.

### Source Code Implementation

**Files**:
- All `.java`, `.ts`, `.py`, `.js`, `.go`, `.rs`, `.cpp`, `.c`, `.h` files
- Module directories: `ast/`, `cli/`, `config/`, `parser/`, `security/`, `formatter/`, etc.
- ANY file under `*/src/main/java/**`
- ANY file under `*/src/test/java/**`

### Test Files & Fixtures

**Files**:
- `*/src/test/**` (all test code)
- `/workspace/main/tests/integration/**`
- Test fixtures, test data files

### Module Build Files

**Files**:
- `*/pom.xml` (module pom.xml, NOT root)
- `*/module-info.java`
- Module-specific build configuration

### Module Structure Changes

**Operations**:
- Creating new modules
- Restructuring module hierarchies
- Adding/removing module dependencies
- Refactoring module boundaries

### Tasks from todo.md

**Scope**:
- ANY task explicitly listed in `/workspace/main/todo.md`
- Implementing features from roadmap
- Fixing bugs from task list
- Refactoring work from technical debt list

### Studies for Implementation Planning

**Files**:
- `/workspace/main/docs/studies/**/*.md` when created FOR a todo.md task
- Research documents that include implementation requirements

**Distinction from Category A**:
- Category A: Exploratory research (understanding existing code, investigating libraries)
- Category B: Implementation planning (requirements gathering, design decisions for upcoming tasks)

**Decision Rule**: If study will be consumed by a todo.md task, it requires task protocol

### Bug Fixes (All Sizes)

**Scope**:
- One-line fixes
- Typo corrections in code
- Quick patches
- Emergency hotfixes

**Note**: Fast-track protocol available for emergencies (see § Emergency Protocol)

### Refactoring Work

**Decision Rule**: Protocol required ONLY if refactoring is a todo.md task

**Category A** (Non-Protocol):
- Opportunistic refactoring while working on other tasks
- Small refactors (rename variable, extract method)
- Code cleanup during reviews
- ANY refactoring NOT listed in todo.md

**Category B** (Protocol-Required):
- Refactoring explicitly listed in todo.md
- Planned architectural refactoring tasks
- Technical debt items from task list

### Performance Work

**Category A** (Non-Protocol):
- Running existing benchmarks (read-only analysis)
- Performance profiling and measurement
- Analyzing benchmark results

**Category B** (Protocol-Required):
- Creating NEW JMH benchmarks
- Modifying benchmark code
- Performance optimization implementations (new code/logic)

### Dependency Updates

**Decision**: Non-Protocol (Category A)

**Files**:
- Root `pom.xml` `<dependencyManagement>` version updates
- Module `pom.xml` dependency version changes
- Adding/removing dependencies in any pom.xml

**Note**: If dependency update breaks build, fix with non-protocol work. If dependency update requires code changes (API migration), those code changes are Category B.

### Generated Code Modifications

**Category A** (Non-Protocol):
- Running code generators (read-only operation)
- Regenerating code from templates
- Updating generator configuration

**Category B** (Protocol-Required):
- Manually editing generated code (makes it non-generated)
- Modifying code generator templates (affects future generation)
- Creating new code generators

## Category C: Resolved Ambiguous Cases {#resolved-cases}

### Build System Debugging → Category A

**Decision**: Non-protocol

**Exception**: If debug reveals need for module code changes → Category B

### Quick Bug Fixes → Category B

**Decision**: Protocol-required

### Checkstyle/PMD Config → Category A

**Decision**: Non-protocol

**Files**: `config/checkstyle.xml`, `config/pmd.xml`

### Studies/Research → Category B (for implementation)

**Decision**: Protocol-required for implementation planning, Non-protocol for exploration

**Distinction**:
- **Category A**: "How does library X work?" (exploration)
- **Category B**: "Requirements for implementing feature Y using library X" (planning)

**Enforcement**: If study filename matches todo.md task name → Category B

### Emergency Fixes → Category B (Fast-Track)

**Decision**: Protocol-required, but with fast-track state machine

**Fast-Track Protocol**:
1. Create task.json with `state: "EMERGENCY"`
2. Skip CLASSIFIED, REQUIREMENTS, SYNTHESIS states
3. Direct transition: EMERGENCY → IMPLEMENTATION → VALIDATION → CLEANUP
4. Stakeholder agents still required (quality doesn't decrease for urgency)
5. Parallel review during implementation (not blocking)

**Use Case**: Critical production bugs, security vulnerabilities

## Enforcement Rules {#enforcement-rules}

### Detection Logic

Hook: `require-task-protocol.sh` (PreToolUse)

```bash
# Check 1: Are we in /workspace/tasks/*/code/ directory?
if [[ "$CWD" == /workspace/tasks/*/code* ]]; then
  # Check 2: Does task.json exist in parent directory?
  if [[ ! -f "../task.json" ]]; then
    # Check 3: Is this file Category A (allowed without protocol)?
    if is_category_a_file "$FILE_PATH"; then
      allow()
    else:
      block("Category B work requires task protocol. Run: create task.json first")
    fi
  fi
fi

# Check 4: Are we on main branch working on Category B files?
if [[ "$BRANCH" == "main" ]] && is_category_b_file "$FILE_PATH"; then
  block("Category B work must use task protocol. Cannot modify source code on main branch directly.")
fi
```

### File Pattern Matching

**Category A Patterns** (glob):
- `CLAUDE.md`
- `README.md`
- `changelog.md`
- `todo.md`
- `docs/**/*.md` (except implementation planning studies)
- `.claude/**/*`
- `**/*.xml` (all pom.xml, checkstyle.xml, pmd.xml)
- `.editorconfig`, `.gitignore`, `.gitattributes`
- `.idea/**/*`, `**/*.iml`

**Category B Patterns** (glob - ONLY if in todo.md):
- `**/*.java` (source code files)
- `**/*.ts`, `**/*.py`, `**/*.js` (source code files)
- `**/*.go`, `**/*.rs`, `**/*.cpp`, `**/*.c`, `**/*.h` (source code files)
- `*/src/**/*.java` (module source files)
- `*/module-info.java` (module descriptors)
- `tests/integration/**/*` (integration tests)
- `docs/studies/*` (if study name matches todo.md task)

**Special Logic Required**:
- **Refactoring**: Requires checking if work is from todo.md
- **Source Files**: Always Category B ONLY if:
  1. Working in `/workspace/tasks/*/code/` (protocol workflow)
  2. OR modifying on main branch (blocked)
- **Studies**: Check if filename matches any todo.md task name

### Special Cases

**Root pom.xml**: Category A (build infrastructure)
**Module pom.xml**: Category B (module configuration)

**Detection**:
```bash
if [[ "$FILE_PATH" == "/workspace/main/pom.xml" ]]; then
  # Root pom.xml - Category A
elif [[ "$FILE_PATH" == */pom.xml ]]; then
  # Module pom.xml - Category B
fi
```

**Studies Detection**:
```bash
if [[ "$FILE_PATH" == docs/studies/*.md ]]; then
  # Check if filename matches any todo.md task
  STUDY_NAME=$(basename "$FILE_PATH" .md)
  if grep -q "$STUDY_NAME" /workspace/main/todo.md; then
    # Category B - implementation planning
  else:
    # Category A - exploratory research
  fi
fi
```

## Hook Consolidation Plan {#consolidation}

### Hooks to Consolidate

**Redundant Hooks** (providing similar guidance):
1. `task-protocol-reminder.sh` (SessionStart - protocol reminder)
2. `session-start-role-reminder.sh` (SessionStart - role boundaries)

**Consolidated Into**: `protocol-and-role-reminder.sh`
- Shows both protocol requirements AND role boundaries
- Single SessionStart hook instead of two

**Redundant Hooks** (protocol enforcement):
1. `detect-main-agent-implementation.sh` (PreToolUse - blocks source file creation)
2. New logic needed for protocol bypass detection

**Consolidated Into**: `require-task-protocol.sh`
- Enforces Category A/B distinction
- Blocks protocol bypass
- Blocks main agent source file creation
- Single PreToolUse hook for all protocol enforcement

### Hooks to Keep (Specialized)

- `check-lock-ownership.sh` - Lock file validation (different concern)
- `state-transition-detector.sh` - State change detection (different concern)
- `enforce-synthesis-checkpoint.sh` - User approval enforcement (different concern)
- `validate-git-status-on-transition.sh` - Git state validation (different concern)
- `detect-worktree-violation.sh` - Worktree boundary enforcement (different concern)

### New Hooks to Create

1. `protocol-and-role-reminder.sh` - Consolidates two SessionStart hooks
2. `require-task-protocol.sh` - Consolidates protocol enforcement, implements Category A/B logic

## Error Messages {#error-messages}

### Category B File Without task.json

```
🚨 TASK PROTOCOL REQUIRED

FILE: /workspace/tasks/my-task/code/src/main/java/Feature.java
OPERATION: Write
CATEGORY: Category B (Source Code Implementation)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PROTOCOL REQUIREMENT:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Source code implementation requires task protocol isolation.

REQUIRED ACTIONS:

1. Initialize task protocol:

   cd /workspace/tasks/my-task/code
   cat > ../task.json <<EOF
   {
     "session_id": "$SESSION_ID",
     "task_name": "my-task",
     "state": "INIT",
     "created_at": "$(date -Iseconds)"
   }
   EOF

2. Follow task protocol state machine (INIT → CLASSIFIED → IMPLEMENTATION)

3. Use stakeholder agents for source code implementation

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
REFERENCE:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

See: docs/project/protocol-scope-specification.md
See: docs/project/task-protocol-core.md
```

### Category B File on Main Branch

```
🚨 PROTOCOL VIOLATION: Source Code on Main Branch

FILE: /workspace/main/formatter/src/main/java/Feature.java
BRANCH: main
OPERATION: Write
CATEGORY: Category B (Source Code Implementation)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
VIOLATION:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Source code CANNOT be modified directly on main branch.

REQUIRED ACTIONS:

1. Create task for this work (if not in todo.md already)
2. Use task protocol with isolated worktree:

   git worktree add /workspace/tasks/my-task/code -b my-task
   cd /workspace/tasks/my-task/code
   # Create task.json and follow protocol

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
REFERENCE:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

See: docs/project/protocol-scope-specification.md § Category B
```

## Examples {#examples}

### ✅ Valid Category A Work (During Task)

```bash
# When working on a task, Category A files go in SEPARATE commit on task branch
cd /workspace/tasks/my-task/code

# Improving documentation (separate commit from implementation)
vim CLAUDE.md  # Category A - commit separately

# Fixing hook (separate commit from implementation)
vim .claude/hooks/my-hook.sh  # Category A - commit separately

# Updating root build config
vim pom.xml  # Category A (root level) - commit separately

# Exploratory research
vim docs/studies/explore-library-x.md  # Category A - commit separately

# Final task branch structure:
# Commit 1: "Update hook for X handling" (all Category A changes)
# Commit 2: "Implement feature Y" (all implementation changes)
```

### ❌ Invalid Non-Protocol Work

```bash
# Implementing feature
cd /workspace/main
vim formatter/src/main/java/NewFeature.java  # Category B - BLOCKED

# Quick bug fix
cd /workspace/main
vim parser/src/main/java/Parser.java  # Category B - BLOCKED

# Test creation
vim ast/src/test/java/NewTest.java  # Category B - BLOCKED

# Implementation planning
vim docs/studies/implement-feature-y.md  # Category B (matches todo.md) - BLOCKED
```

### ✅ Valid Protocol Work

```bash
# Create task for feature implementation
git worktree add /workspace/tasks/my-feature/code -b my-feature
cd /workspace/tasks/my-feature/code

# Initialize protocol
cat > ../task.json <<EOF
{
  "session_id": "$SESSION_ID",
  "task_name": "my-feature",
  "state": "INIT",
  "created_at": "$(date -Iseconds)"
}
EOF

# Now source code work is allowed
vim formatter/src/main/java/NewFeature.java  # Category B with protocol - allowed
```

## Migration Path {#migration}

For existing work started without protocol:

1. **If work is in progress**:
   - Create task.json retroactively
   - Set state based on current progress
   - Continue with protocol going forward

2. **If work is complete**:
   - Document as protocol violation in audit
   - Apply protocol improvements to prevent recurrence
   - Keep completed work (if quality is acceptable)

3. **If work is planned**:
   - Initialize task.json BEFORE starting
   - Follow protocol from beginning

## FAQ {#faq}

**Q: Can I fix a typo in a comment without task protocol?**
A: No. Even comment changes are source code modifications (Category B).

**Q: Can I update library version in root pom.xml without protocol?**
A: Yes. Root pom.xml dependency management is Category A.

**Q: Can I update library version in module pom.xml without protocol?**
A: No. Module pom.xml changes are Category B.

**Q: Can I create a study document for exploration without protocol?**
A: Yes, IF it's purely exploratory. If it's implementation planning for a todo.md task, it's Category B.

**Q: What about documentation for new features?**
A: Documenting HOW to use a feature (README, user guide) is Category A. Implementation planning (requirements, design) is Category B.

**Q: Can I run benchmarks without protocol?**
A: Yes. Running existing benchmarks is read-only (Category A). Creating/modifying benchmark code is Category B.

**Q: What about emergency production fixes?**
A: Category B (protocol required) but use Fast-Track Protocol (§ Emergency Fixes).

---

**See Also**:
- [task-protocol-core.md](task-protocol-core.md) - Complete protocol state machine
- [main-agent-coordination.md](main-agent-coordination.md) - Main agent role boundaries
