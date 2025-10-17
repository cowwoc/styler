# Task State Machine Protocol - Operations & Procedures

> **Version:** 2.0 | **Last Updated:** 2025-10-16
> **Related Documents:** [CLAUDE.md](../../CLAUDE.md) • [task-protocol-core.md](task-protocol-core.md)

**IMPORTANT**: This document is Part 2 of the Task Protocol. Read [task-protocol-core.md](task-protocol-core.md) first for:
- State machine architecture and definitions
- Risk-based agent selection
- Workflow variants by risk level
- Agent selection decision tree
- Complete style validation framework
- Batch processing and continuous mode
- Mandatory state transitions (INIT through SCOPE_NEGOTIATION)

This document contains:
- Completion and cleanup procedures
- Validation functions
- Agent interaction protocols
- Error handling & recovery
- Compliance verification
- Tool-specific optimizations
- Workflow execution engine

---

### COMPLETE → CLEANUP
**Mandatory Conditions:**
- [ ] All work already committed to task branch (from USER REVIEW checkpoint)
- [ ] todo.md removed from todo list and added to changelog.md
- [ ] **Dependent tasks updated in todo.md** (BLOCKED → READY if all dependencies satisfied)
- [ ] todo.md and changelog.md changes amended to existing commit
- [ ] **Build verification passes in task worktree BEFORE merge** (`./mvnw verify` in worktree)
- [ ] Task branch merged to main branch with **LINEAR COMMIT HISTORY** (fast-forward only, NO merge commits)
- [ ] Full build verification passes on main branch after merge (`./mvnw verify`)

**Evidence Required:**
- **Pre-merge build success in worktree** (`./mvnw verify` passes before merge attempt)
- Git log showing clean linear history (no merge commits)
- **todo.md modification included in final commit** (verify with `git show --stat | grep "todo.md"`)
- Main branch build success after integration (`./mvnw verify` passes)
- All deliverables preserved in main branch

**CRITICAL: Pre-Merge Build Verification Gate**
```bash
# Launch ALL agents with Task tool in ONE message
Task tool (technical-architect): {...}
Task tool (code-quality-auditor): {...}
Task tool (style-auditor): {...}
Task tool (security-auditor): {...}

# Wait for ALL responses before proceeding
```

**DON'T** (sequential):
```bash
# Message 1
Task tool (technical-architect): {...}
# Wait for response

# Message 2
Task tool (code-quality-auditor): {...}
# Wait for response

# = 3-4× overhead, 20-30 min wasted
```

---

### Pattern 2: Predictive Prefetching (INIT) - **MANDATORY**

**CRITICAL REQUIREMENT**: ALL predictable resources MUST be loaded in SINGLE message

**DO** (predict and load upfront):
```bash
# Analyze task to predict ALL dependencies
# Load ALL in single message with multiple Read/Glob calls

Read pom.xml + \
Read checkstyle.xml + \
Read pmd.xml + \
Glob "src/main/java/**/*Target*.java" + \
Glob "src/test/java/**/*Test.java" + \
Read docs/project/architecture.md

# = 1 round-trip, all resources loaded
```

**DON'T** (sequential discovery):
```bash
Read pom.xml
# Discover need for checkstyle.xml
Read checkstyle.xml
# Discover need for implementation files
Glob "src/main/java/**/*Target*.java"

# = 5-10 round-trips, 5-10 min wasted
```

---

### Pattern 3: Fail-Fast Validation (IMPLEMENTATION)

**DO** (incremental validation):
```bash
# Enable fail-fast error handling
set -e

# For each component
Edit src/main/java/Component.java

# IMMEDIATE checks (exit on failure)
./mvnw compile -q -pl :{module}
./mvnw checkstyle:check -q -pl :{module}

Edit src/test/java/ComponentTest.java

./mvnw test -Dtest=ComponentTest -q

# Commit before moving to next component
git add -A && git commit -m "Implement Component (validated)"
```

**DON'T** (late discovery):
```bash
# Implement everything
Edit Component1.java
Edit Component2.java
Edit Component3.java

# Then discover 60 style violations
./mvnw checkstyle:check  # FAILED

# Fix with stale context (30 min later)
```

---

### Pattern 4: Pre-Validation Checklist (IMPLEMENTATION Exit)

**MANDATORY GATES BEFORE EXITING IMPLEMENTATION**

Before transitioning from IMPLEMENTATION to VALIDATION, ALL criteria must be met:

```bash
# IMPLEMENTATION Exit Checklist (verify-implementation-exit.sh enforces)

# Gate 1: Clean working directory
git status --porcelain src/
# Must be empty - all changes committed

# Gate 2: All tests passing
./mvnw test
# Exit code: 0 (all tests pass)

# Gate 3: Style compliance
./mvnw checkstyle:check pmd:check
# Exit code: 0 (zero violations)

# Gate 4: Minimum test count
find src/test -name "*Test.java" -exec grep -c "@Test" {} + | awk '{sum+=$1} END {print sum}'
# Count: ≥15 tests

# Gate 5: Build verification
./mvnw verify
# Exit code: 0 (full build passes)
```

**CHECKLIST DETAILS:**

**✅ Code Compilation:**
- `./mvnw compile` passes with zero errors
- All modules compile successfully
- No missing dependencies

**✅ Test Coverage:**
- Minimum 15 tests (standard components)
- Minimum 20 tests (algorithm-heavy components)
- Required categories:
  - Null/Empty validation: 2-3 tests
  - Boundary conditions: 2-3 tests
  - Edge cases: 3-5 tests
  - Algorithm precision: 3-5 tests (if applicable)
  - Configuration validation: 2-3 tests
  - Real-world scenarios: 3-5 tests

**✅ Test Execution:**
- `./mvnw test` passes (100% success rate)
- No test failures, errors, or skipped tests
- All assertions passing

**✅ Style Compliance:**
- `./mvnw checkstyle:check` passes (zero violations)
- `./mvnw pmd:check` passes (zero violations)
- Manual style rules verified (see docs/code-style/)

**✅ JavaDoc Documentation:**
- All public methods documented
- Contextual comments (not generic templates)
- Business logic explanations included

**✅ Build Configuration:**
- pom.xml follows sibling module patterns
- Dependencies properly scoped
- Build properties configured correctly

**✅ Edge Case Handling:**
- Null validation tests present
- Empty/single-element tests present
- Boundary value tests present

**ENFORCEMENT:**

The `.claude/hooks/verify-implementation-exit.sh` hook automatically blocks VALIDATION entry if any gate fails. Recovery procedure:

```bash
# If blocked, return to IMPLEMENTATION
jq '.state = "IMPLEMENTATION"' $LOCK_FILE > /tmp/lock.json && mv /tmp/lock.json $LOCK_FILE

# Fix the specific gate failure:
# - Uncommitted changes: git add && git commit
# - Test failures: Fix implementation or tests
# - Style violations: Run checkstyle/PMD and fix
# - Insufficient tests: Add missing test categories
# - Build failures: Fix compilation/dependency issues
```

---

### Pattern 5: Batch Fixing (Implementation Rounds)

**COPY-PASTE TEMPLATE** (customize agent selection based on task):

```
I am now entering iterative validation rounds within IMPLEMENTATION state. Launching all stakeholder agents in parallel for review.

Task tool (technical-architect): Review implementation architecture and design
Task tool (code-quality-auditor): Review code quality and best practices
Task tool (style-auditor): Review complete style compliance (checkstyle + PMD + manual)
Task tool (code-tester): Review test coverage and quality
```

**CRITICAL**: All agents in SINGLE message for parallel execution

**DO** (collect all, fix once):
```bash
# Round 1: Collect ALL issues from all agents in parallel
# Agent 1: 37 checkstyle violations
# Agent 2: 18 PMD violations
# Agent 3: 5 manual rule violations
# Agent 4: 3 test failures

# Batch fix by type
Fix all 60 style violations together
./mvnw checkstyle:check pmd:check  # Verify all fixed
git commit -m "Fix all style violations (60 total)"

Fix all 3 test failures together
./mvnw test  # Verify all pass
git commit -m "Fix all test failures (3 total)"

# Round 2: Re-verify with all agents
# Expect unanimous approval
```

**DON'T** (iterative fixing):
```bash
Fix checkstyle violation 1
./mvnw checkstyle:check
Fix checkstyle violation 2
./mvnw checkstyle:check
# ... repeat 60 times ...

# = 60 verification cycles, 45-50 min wasted
```

---

## TROUBLESHOOTING

### Issue: Implementation Rounds Taking >15 Minutes

**Symptoms**:
- Multiple implementation rounds (>3)
- Agents returning ❌ REJECTED repeatedly
- Slow progress toward unanimous approval

**Root Causes**:
1. Not using batch fixing (fixing iteratively)
2. Not launching agents in parallel
3. Fail-fast validation not working (issues caught late)

**Solutions**:
```bash
# 1. Use batch fixing
# Collect ALL issues from all agents BEFORE fixing
# Fix all issues of same type together
# Verify ONCE after all fixes

# 2. Launch agents in parallel
# ALL agents in SINGLE message
Task tool (agent-1) + Task tool (agent-2) + Task tool (agent-3)

# 3. Improve fail-fast validation
# Add incremental checks during IMPLEMENTATION
./mvnw compile + ./mvnw checkstyle:check after EACH component
```

---

### Issue: High Token Usage (>65,000)

**Symptoms**:
- Conversation history exceeding 65,000 tokens
- Context compaction occurring
- Performance degradation

**Root Causes**:
1. Verbose agent responses (full code in messages)
2. Redundant file reads
3. Not using file-based diffs
4. Sequential discovery (multiple read rounds)

**Solutions**:
```bash
# 1. Agents return metadata only (not full code)
# Agent response: "Implemented 3 files. See commit abc123." (50 tokens)
# NOT: "Here's the complete implementation: [2000 lines]" (50,000 tokens)

# 2. Pre-fetch all resources in INIT
# Read once, cache in session, pass to agents
# No re-reads during SYNTHESIS or implementation rounds

# 3. Use file-based diffs where possible
# Agents write diffs to files
# Main agent reads diffs, not full code

# 4. Predict dependencies upfront
# Load ALL needed files in INIT (single message)
```

---

### Issue: Sequential Discovery Delays (>5 Round-Trips)

**Symptoms**:
- Many "Need to read FileX" messages
- 5-10+ round-trips during INIT or SYNTHESIS
- Slow resource loading

**Root Cause**:
- Not using predictive prefetching
- Reactive file discovery instead of proactive loading

**Solution**:
```bash
# Analyze task BEFORE loading resources
# Predict ALL dependencies
# Load ALL in single message

# Example for Java implementation task:
Read pom.xml + \
Read checkstyle.xml + \
Read pmd.xml + \
Glob "src/main/java/**/*{Pattern}*.java" + \
Glob "src/test/java/**/*Test.java" + \
Read docs/project/architecture.md + \
Read docs/code-style/*-claude.md

# ALL in ONE message = 1 round-trip
```

---

### Issue: Unanimous Approval Not Achieved

**Symptoms**:
- Agents returning ❌ REJECTED after multiple rounds
- Cannot proceed to VALIDATION
- Stuck in implementation rounds

**Root Causes**:
1. Issues not fully addressed
2. Scope exceeding task boundaries
3. Conflicting requirements

**Solutions**:
```bash
# 1. Verify all issues addressed
# Re-read agent rejection feedback
# Ensure EVERY issue has been fixed
# Run quality gates to confirm: ./mvnw verify

# 2. Scope negotiation (if effort > 2× original)
# If resolution effort exceeds 2× task scope
# Return to SYNTHESIS with reduced scope
# Create follow-up tasks in todo.md for deferred work

# 3. Resolve conflicting requirements
# Identify conflicts between agents
# Prioritize based on domain authority
# Document trade-off decisions
```

---

## BEST PRACTICES

### 1. Risk Assessment

```python
# Automatic risk determination
files_modified = get_modified_files(task)

if any(f.startswith("src/main/java/") for f in files_modified):
    risk = "HIGH"
elif any(f.startswith("src/test/") for f in files_modified):
    risk = "MEDIUM"
elif all(f.endswith(".md") for f in files_modified):
    risk = "LOW"
else:
    risk = "HIGH"  # Default to highest safety

# Escalation triggers
if any(keyword in task_description for keyword in
       ["security", "authentication", "build", "encryption"]):
    risk = "HIGH"  # Force escalation
```

### 2. Agent Selection

```python
# HIGH-RISK (mandatory agents)
agents = [
    "technical-architect",   # Architecture and design
    "code-quality-auditor",  # Quality standards
    "style-auditor",         # Style compliance
    "code-tester",           # Test coverage
]

# Add conditional agents
if "security" in task_description or "authentication" in task_description:
    agents.append("security-auditor")

if "performance" in task_description or "algorithm" in task_description:
    agents.append("performance-analyzer")

# MEDIUM-RISK (core only)
agents = ["technical-architect", "code-quality-auditor"]

# LOW-RISK (minimal or none)
agents = []  # or ["code-quality-auditor"] for technical docs
```

### 3. Lock State Management

```bash
# Update lock state at each transition
update_lock() {
    local TASK=$1
    local NEW_STATE=$2

    # Validate state is in allowed set
    VALID_STATES="INIT|CLASSIFIED|REQUIREMENTS|SYNTHESIS|IMPLEMENTATION|VALIDATION|REVIEW|AWAITING_USER_APPROVAL|COMPLETE|CLEANUP|SCOPE_NEGOTIATION"
    if ! echo "$NEW_STATE" | grep -qE "^($VALID_STATES)$"; then
        echo "ERROR: Invalid state '$NEW_STATE'" >&2
        return 1
    fi

    jq ".state = \"$NEW_STATE\"" /workspace/tasks/${TASK}/task.json > /tmp/lock.tmp
    mv /tmp/lock.tmp /workspace/tasks/${TASK}/task.json
}
```

**CRITICAL VALIDATION: todo.md Commit Verification**
```bash
# MANDATORY: Verify todo.md is in the commit BEFORE merging to main
git show --stat | grep "todo.md" || { echo "❌ VIOLATION: todo.md not in commit"; exit 1; }
git show --name-only | grep "todo.md" || { echo "❌ VIOLATION: todo.md not modified"; exit 1; }
```

**CRITICAL: Linear Commit History Requirement**

**Git Safety Sequence (Concurrency-Safe):**
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing
# CRITICAL: This procedure is safe for concurrent execution by multiple Claude instances
#           All operations occur in task worktree, avoiding race conditions on main worktree

# Step 1: Ensure clean working state
cd /workspace/tasks/{TASK_NAME}/code
rm -f .temp_dir
git status --porcelain | grep -E "(dist/|target/|\.temp_dir$)" | wc -l  # Must be 0

# Step 2: Commit all task deliverables (code, tests, docs)
git add <changed-files>
git commit -m "Descriptive commit message for task changes"

# Step 3: Archive completed task from todo.md to changelog.md
# CRITICAL: Follow CLAUDE.md task archival policy
# 3a. Remove completed task entry from todo.md (delete entire task section)
# 3b. Add completed task to changelog.md under today's date (## YYYY-MM-DD)
# 3c. Include completion details: solution, files modified, tests, quality gates
# Edit todo.md and changelog.md accordingly

# Step 3d: Update dependent tasks in todo.md (with file locking)
# Acquire todo.md lock to prevent concurrent modifications
TODO_LOCK="/workspace/.todo.md.lock"
exec 200>"$TODO_LOCK"
flock -x 200 || { echo "ERROR: Could not acquire todo.md lock"; exit 1; }

# For each task marked as BLOCKED:
#   - Check if the completed task was listed in its Dependencies
#   - If ALL dependencies are now complete (check changelog.md), change status from BLOCKED to READY
#   - Example: If task B1 depends on A0 and A1, and both A0 and A1 are now in changelog.md,
#             change "- [ ] **BLOCKED:** `task-name`" to "- [ ] **READY:** `task-name`"

# Edit todo.md with dependent task updates
# ... editing logic (use Edit tool or sed commands) ...

# Release lock
flock -u 200

# Step 3e: Amend the commit to include todo.md and changelog.md changes
git add todo.md changelog.md
git commit --amend --no-edit

# Step 4: Verify todo.md and changelog.md are included in the commit
git show --stat | grep "todo.md" || { echo "ERROR: todo.md not in commit"; exit 1; }
git show --stat | grep "changelog.md" || { echo "ERROR: changelog.md not in commit"; exit 1; }

# Step 5: Fetch latest main and rebase to create linear history
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git rebase origin/main

# Step 6: PRE-MERGE BUILD VERIFICATION (CRITICAL GATE)
# MANDATORY: Verify build passes in worktree BEFORE push attempt
./mvnw verify || {
    echo "❌ VIOLATION: Build fails in worktree - push BLOCKED"
    echo "Fix all checkstyle, PMD, and test failures before merging"
    exit 1
}

# Step 7: Atomic push to main worktree (TRULY CONCURRENCY-SAFE)
# Main worktree has receive.denyCurrentBranch=updateInstead configured
# Git's internal locking ensures only one push succeeds at a time
# Push automatically updates both ref and working tree atomically
git push /workspace/main HEAD:refs/heads/main || {
    echo "❌ Push failed - another instance merged first"
    echo "Solution: Fetch, rebase, and retry"
    git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
    git rebase origin/main
    # Retry after rebase
    git push /workspace/main HEAD:refs/heads/main || exit 1
}

# Step 8: Post-merge verification (from task worktree)
# Verify push succeeded
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5  # Must show our commit at HEAD
git show origin/main --stat | grep "todo.md" || { echo "ERROR: todo.md missing"; exit 1; }

# Step 9: OPTIONAL - Verify build on main (skip if Step 6 passed)
# Main worktree is automatically updated by push (receive.denyCurrentBranch=updateInstead)
# cd /workspace/main && ./mvnw verify -q
```

**Concurrency Safety Guarantees:**
- **Truly atomic**: Git's push with receive.denyCurrentBranch=updateInstead uses internal locking
- **No race conditions**: Push operations are serialized by git's index.lock
- **Automatic working tree update**: Main worktree files updated automatically on successful push
- **Automatic conflict detection**: Non-fast-forward push fails cleanly
- **Clear retry path**: Fetch + rebase + retry on conflict
- **Perfect concurrency**: Multiple instances can push simultaneously without conflicts

**Repository Configuration:**
```bash
# Main worktree must have this setting (already configured):
cd /workspace/main
git config receive.denyCurrentBranch updateInstead
```

**Example for task "refactor-line-wrapping-architecture"**:
```bash
# Step 1: Ensure clean state (in task worktree)
cd /workspace/tasks/refactor-line-wrapping-architecture/code
git status --porcelain | grep -E "(dist/|target/)" | wc -l  # Must be 0

# Step 2: Changes already committed during USER REVIEW checkpoint
# Verify commit exists
git log -1 --oneline

# Step 3: Archive completed task and update dependent tasks
# 3a-c. Remove task from todo.md, add to changelog.md with completion details
# 3d. Update dependent tasks: check each BLOCKED task, if all dependencies complete, change to READY
# 3e. Amend commit with both files
git add todo.md changelog.md
git commit --amend --no-edit

# Step 4: Verify todo.md and changelog.md included
git show --stat | grep "todo.md"
git show --stat | grep "changelog.md"

# Step 5: Fetch and rebase onto main
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git rebase origin/main

# Step 6: PRE-MERGE BUILD VERIFICATION (blocks push if fails)
./mvnw verify || {
    echo "❌ Build failed - fix violations before merging"
    exit 1
}

# Step 7: Atomic push to main worktree (concurrency-safe)
git push /workspace/main HEAD:refs/heads/main || {
    echo "❌ Push failed - another instance merged first"
    echo "Rebasing onto updated main..."
    git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
    git rebase origin/main
    # Retry push after rebase
    git push /workspace/main HEAD:refs/heads/main
}

# Step 8: Post-merge verification (from task worktree)
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -10
git show origin/main --stat | grep "todo.md"
```

**Fast-Forward Merge Validation:**
After merge, `git log --graph --oneline` should show:
```
* 7f8deba (HEAD -> main) Implement CLI security basics (SEC-001 through SEC-007)
* 6a2b1c3 Previous commit on main
* 5d9e4f2 Earlier commit
```

NOT this (merge commit creates non-linear history):
```
*   8g9h5i6 (HEAD -> main) Merge branch 'implement-cli-security-basics'  ← ❌ WRONG
|\
| * 7f8deba Implement CLI security basics
|/
* 6a2b1c3 Previous commit on main
```

### DEPENDENT TASK UPDATE PROCEDURE

**Purpose**: Automatically unblock tasks when their dependencies are satisfied, preventing tasks from remaining unnecessarily blocked.

**When to Execute**: During Step 3d of the COMPLETE → CLEANUP transition (after archiving the completed task to changelog.md).

**Implementation Logic:**
```bash
# Helper function to update dependent tasks in todo.md
update_dependent_tasks() {
    local completed_task=$1
    local todo_file="todo.md"
    local changelog_file="changelog.md"

    echo "=== UPDATING DEPENDENT TASKS ==="
    echo "Completed task: $completed_task"

    # Find all BLOCKED tasks in todo.md
    grep -n "BLOCKED:" "$todo_file" | while IFS=: read line_num task_entry; do
        # Extract task name from the entry
        task_name=$(echo "$task_entry" | grep -oP '`\K[^`]+' | head -1)

        # Find the dependencies for this task
        # Dependencies are listed on a line like: "  - **Dependencies**: A0, A1, A2"
        dependencies=$(sed -n "${line_num},/^$/p" "$todo_file" | grep "Dependencies:" | sed 's/.*Dependencies: *//' | sed 's/ *$//')

        if [ -z "$dependencies" ]; then
            continue
        fi

        echo "Checking task: $task_name"
        echo "  Dependencies: $dependencies"

        # Check if ALL dependencies are satisfied (in changelog.md)
        all_satisfied=true
        IFS=',' read -ra DEPS <<< "$dependencies"
        for dep in "${DEPS[@]}"; do
            # Trim whitespace
            dep=$(echo "$dep" | xargs)

            # Check if dependency is in changelog.md (completed)
            if ! grep -q "$dep" "$changelog_file"; then
                echo "  ❌ Dependency $dep not yet complete"
                all_satisfied=false
                break
            else
                echo "  ✅ Dependency $dep complete"
            fi
        done

        # If all dependencies satisfied, update task status from BLOCKED to READY
        if [ "$all_satisfied" = true ]; then
            echo "  🔓 All dependencies satisfied - updating to READY"
            # Use sed to change BLOCKED to READY on the specific line
            sed -i "${line_num}s/BLOCKED:/READY:/" "$todo_file"
            echo "  ✅ Task $task_name updated to READY"
        else
            echo "  ⏳ Task $task_name remains BLOCKED"
        fi
    done

    echo "=== DEPENDENT TASK UPDATE COMPLETE ==="
}

# Usage in Step 3d:
update_dependent_tasks "implement-index-overlay-parser"
```

**Example Scenario:**

Initial state in todo.md:
```markdown
- [ ] **BLOCKED:** `implement-line-length-formatter`
  - **Dependencies**: A0 (styler-formatter-api module), A1 (parser for AST)

- [ ] **BLOCKED:** `implement-import-organization`
  - **Dependencies**: A0 (styler-formatter-api module), A1 (parser for AST)
```

After completing task A1 (`implement-index-overlay-parser`):
1. Check changelog.md for A0 → Found ✅
2. Check changelog.md for A1 → Found ✅ (just completed)
3. Both dependencies satisfied → Update both tasks to READY

Result in todo.md:
```markdown
- [ ] **READY:** `implement-line-length-formatter`
  - **Dependencies**: A0 ✅ COMPLETE, A1 ✅ COMPLETE

- [ ] **READY:** `implement-import-organization`
  - **Dependencies**: A0 ✅ COMPLETE, A1 ✅ COMPLETE
```

**Validation:**
```bash
# Verify dependent tasks were updated
grep "implement-line-length-formatter" todo.md | grep -q "READY:" && echo "✅ Task unblocked" || echo "❌ Task still blocked"
```

### AUTOMATIC CONFLICT RESOLUTION VIA ATOMIC OPERATIONS

**DESIGN PRINCIPLE**: Race conditions are prevented automatically through atomic operations - no manual recovery needed.

**How Atomic Operations Prevent Conflicts:**

**Scenario 1: Both instances attempt same task simultaneously**
- **Instance A**: Executes `mkdir /workspace/tasks/task-x` → **SUCCESS** (directory created)
- **Instance B**: Executes `mkdir /workspace/tasks/task-x` → **FAILS** (directory exists)
- **Instance B**: Automatically releases lock and selects alternative task
- **Instance A**: Continues work uninterrupted

**Scenario 2: Instance A working, Instance B attempts same task**
- **Instance A**: Has lock, has worktree directory
- **Instance B**: Lock creation fails (`set -C` prevents overwrite) → **LOCK_FAILED**
- **Instance B**: Selects alternative task immediately (never attempts directory creation)
- **Instance A**: Unaffected, continues work

**Scenario 3: Lock acquired but directory creation fails**
- **Instance**: Lock created successfully
- **Instance**: Directory creation fails (another instance's directory exists)
- **Instance**: Automatically releases lock via error handler: `rm /workspace/tasks/{TASK_NAME}/task.json`
- **Instance**: Selects alternative task
- **Other instance's work**: Preserved (directory and worktree untouched)

**Key Safety Features:**

1. **Lock Creation**: `set -C` flag prevents overwriting existing locks
2. **Directory Creation**: `mkdir` (not `mkdir -p`) fails atomically if directory exists
3. **Automatic Cleanup**: Error handlers release locks when conflicts detected
4. **Work Preservation**: Existing worktrees never deleted by conflicting instance
5. **Clear Signals**: LOCK_FAILED / DIRECTORY_FAILED / WORKTREE_FAILED indicate next action

**No Manual Recovery Required:**
- ✅ Atomic operations handle all race conditions automatically
- ✅ Error handlers ensure proper cleanup on conflict
- ✅ Clear failure signals direct instance to select alternative task
- ✅ Other instance's work always preserved

---

### CLEANUP (Final State)
**Mandatory Conditions (ORDERED EXECUTION REQUIRED):**

**Phase 1: Pre-Cleanup Verification**
- [ ] Verify lock ownership (session_id matches current session)
- [ ] Verify all agents have status = COMPLETE in their status.json files
- [ ] Verify working directory is NOT inside any task worktree
- [ ] Verify main branch has task commit (git log shows task merged)

**Phase 2: Working Directory Transition**
- [ ] Change directory to main worktree: `cd /workspace/main`
- [ ] Verify pwd = /workspace/main before proceeding

**Phase 3: Worktree Removal (MUST succeed before lock removal)**
- [ ] Remove all agent worktrees (with error handling, no silent failures)
- [ ] Remove all agent branches
- [ ] Remove task worktree
- [ ] Remove task branch
- [ ] Verify removal: `git worktree list | grep {TASK}` returns empty

**Phase 4: Lock and Directory Removal (ONLY after Phase 3 succeeds)**
- [ ] Remove lock file: `rm /workspace/tasks/{TASK}/task.json`
- [ ] Remove entire task directory: `rm -rf /workspace/tasks/{TASK}`
- [ ] Verify removal: `[ ! -d /workspace/tasks/{TASK} ]`

**Phase 5: Temporary File Cleanup**
- [ ] Remove temporary files (if any)
- [ ] Archive state.json file (if needed for audit trail)

**Verification Commands (Execute BEFORE marking CLEANUP complete):**
```bash
# Verify ALL agent worktrees removed
git worktree list | grep "/workspace/tasks/{TASK_NAME}/agents" && {
    echo "❌ FAILED: Agent worktrees still exist:"
    git worktree list | grep "/workspace/tasks/{TASK_NAME}/agents"
    exit 1
} || echo "✅ All agent worktrees removed"

# Verify task worktree removed
git worktree list | grep "/workspace/tasks/{TASK_NAME}/code" && {
    echo "❌ FAILED: Task worktree still exists"
    exit 1
} || echo "✅ Task worktree removed"

# Verify lock file removed
[ -f "/workspace/tasks/{TASK_NAME}/task.json" ] && {
    echo "❌ FAILED: Lock file still exists"
    exit 1
} || echo "✅ Lock file removed"

# Verify entire task directory removed
[ -d "/workspace/tasks/{TASK_NAME}" ] && {
    echo "❌ FAILED: Task directory still exists"
    exit 1
} || echo "✅ Task directory removed"
```

**Implementation (Concurrency-Safe):**
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing
# IMPORTANT: Replace {AGENT_LIST} with space-separated list of agent names

# Step 1: Verify all agents reached COMPLETE status
AGENTS="{AGENT_LIST}"  # e.g., "technical-architect code-quality-auditor style-auditor security-auditor"
for agent in $AGENTS; do
  STATUS=$(jq -r '.status' "/workspace/tasks/{TASK_NAME}/agents/$agent/status.json" 2>/dev/null || echo "MISSING")
  if [ "$STATUS" != "COMPLETE" ]; then
    echo "❌ FATAL: Agent $agent status = $STATUS (expected: COMPLETE)"
    echo "Cannot proceed to CLEANUP until all agents reach COMPLETE status"
    exit 1
  fi
  echo "✅ Agent $agent status verified: COMPLETE"
done

# Step 2: Verify work preserved (can check from task worktree)
cd /workspace/tasks/{TASK_NAME}/code
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5 | grep "{TASK_NAME}" || {
    echo "❌ FATAL: Work not found in main branch"
    exit 1
}

# Step 3: Verify lock ownership
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/tasks/{TASK_NAME}/task.json")
if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "❌ FATAL: Cannot delete lock owned by $LOCK_OWNER"
  exit 1
fi

# Step 4: Change to main worktree (REQUIRED before removing worktrees)
[[ "$(pwd)" == "/workspace/tasks/{TASK_NAME}"* ]] && {
  echo "❌ FATAL: Cannot remove worktree while inside it (pwd=$(pwd))"
  exit 1
} || cd /workspace/main

# Step 5: Remove all agent worktrees and branches (WITH ERROR HANDLING)
for agent in $AGENTS; do
  echo "Removing agent worktree: $agent"

  # Check if worktree exists before attempting removal
  if [ -d "/workspace/tasks/{TASK_NAME}/agents/$agent/code" ]; then
    git worktree remove /workspace/tasks/{TASK_NAME}/agents/$agent/code --force || {
      echo "❌ FAILED to remove agent worktree: $agent"
      echo "Check for processes holding locks in this directory"
      exit 1
    }
    echo "  ✅ Agent worktree removed: $agent"
  else
    echo "  ℹ️  Agent worktree already removed: $agent"
  fi

  # Remove agent branch (safe to fail if already deleted)
  git branch -d {TASK_NAME}-$agent 2>/dev/null && echo "  ✅ Agent branch deleted: $agent" || echo "  ℹ️  Agent branch already deleted: $agent"
done

# Step 6: Remove task worktree and branch
git worktree remove /workspace/tasks/{TASK_NAME}/code --force
git branch -d {TASK_NAME}

# Step 7: Remove lock file (ONLY after worktrees successfully removed)
rm -f /workspace/tasks/{TASK_NAME}/task.json

# Step 8: Remove entire task directory
rm -rf /workspace/tasks/{TASK_NAME}

# Step 9: Temporary file cleanup
TEMP_DIR=$(cat .temp_dir 2>/dev/null) && [ -n "$TEMP_DIR" ] && rm -rf "$TEMP_DIR"

echo "✅ CLEANUP complete: All worktrees and branches removed"
```

**Example for task "refactor-line-wrapping-architecture" with agents "technical-architect code-quality-auditor style-auditor"**:
```bash
# Verify work in main branch (from task worktree)
cd /workspace/tasks/refactor-line-wrapping-architecture/code
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5 | grep "refactor-line-wrapping-architecture" || {
    echo "❌ FATAL: Work not found in main branch"
    exit 1
}

# Clean up task resources with session verification
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/tasks/refactor-line-wrapping-architecture/task.json")
if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "❌ FATAL: Cannot delete lock owned by $LOCK_OWNER"
  exit 1
fi

rm -f /workspace/tasks/refactor-line-wrapping-architecture/task.json

# Change to main worktree (fails if already inside task worktree being removed)
[[ "$(pwd)" == "/workspace/tasks/refactor-line-wrapping-architecture"* ]] && { echo "❌ FATAL: Cannot remove worktree while inside it (pwd=$(pwd))"; exit 1; } || cd /workspace/main

# Remove all agent worktrees and branches
# 🚨 SHELL REQUIREMENT: Execute in bash (not sh/dash)
AGENTS="technical-architect code-quality-auditor style-auditor"

# Variable $AGENTS MUST be UNQUOTED in the for loop
for agent in $AGENTS; do
  echo "Removing agent worktree: $agent"
  git worktree remove /workspace/tasks/refactor-line-wrapping-architecture/agents/$agent/code --force
  git branch -d refactor-line-wrapping-architecture-$agent
  echo "  ✅ Agent cleanup complete: $agent"
done

# Remove task worktree and branch
git worktree remove /workspace/tasks/refactor-line-wrapping-architecture/code --force
git branch -d refactor-line-wrapping-architecture

# Remove entire task directory
rm -rf /workspace/tasks/refactor-line-wrapping-architecture

echo "✅ CLEANUP complete: All worktrees and branches removed"
```

## TRANSITION VALIDATION FUNCTIONS

### Universal Validation Requirements
Every transition MUST execute these checks:

```python
def validate_transition_conditions(from_state, to_state, evidence, task_context):
    """Validate that all mandatory conditions for transition are met"""

    # Load transition requirements
    required_conditions = TRANSITION_MAP[from_state][to_state]['conditions']
    required_evidence = TRANSITION_MAP[from_state][to_state]['evidence']

    # Validate each condition
    for condition in required_conditions:
        if not validate_condition(condition, evidence, task_context):
            return False, f"Condition failed: {condition}"

    # Validate required evidence exists
    for evidence_type in required_evidence:
        if not validate_evidence(evidence_type, evidence):
            return False, f"Missing evidence: {evidence_type}"

    return True, "All transition conditions satisfied"

def validate_condition(condition, evidence, context):
    """Validate specific transition condition"""
    # Implementation specific to each condition type
    pass

def validate_evidence(evidence_type, evidence_data):
    """Validate required evidence exists and is complete"""
    # Implementation specific to each evidence type
    pass
```

### State Enforcement Functions

```bash
# Function: Update task state
update_task_state() {
    local new_state=$1
    local evidence_json=$2

    # Validate transition is legal
    current_state=$(jq -r '.current_state' state.json 2>/dev/null || echo "INIT")
    if ! validate_state_transition "$current_state" "$new_state"; then
        echo "ERROR: Invalid transition from $current_state to $new_state"
        exit 1
    fi

    # Update state file
    jq --arg state "$new_state" \
       --argjson evidence "$evidence_json" \
       --arg timestamp "$(date -Iseconds)" \
       '.current_state = $state |
        .evidence += $evidence |
        .transition_log += [{"from": .current_state, "to": $state, "timestamp": $timestamp}]' \
       state.json > state.json.tmp && mv state.json.tmp state.json
}

# Function: Validate state transition
validate_state_transition() {
    local from_state=$1
    local to_state=$2

    case "$from_state:$to_state" in
        # Standard transitions
        "INIT:CLASSIFIED"|"CLASSIFIED:REQUIREMENTS"|"REQUIREMENTS:SYNTHESIS"|"SYNTHESIS:IMPLEMENTATION"|"IMPLEMENTATION:VALIDATION"|"VALIDATION:REVIEW"|"REVIEW:COMPLETE"|"COMPLETE:CLEANUP")
            return 0 ;;
        # Scope negotiation transitions
        "REVIEW:SCOPE_NEGOTIATION"|"SCOPE_NEGOTIATION:SYNTHESIS"|"SCOPE_NEGOTIATION:COMPLETE")
            return 0 ;;
        # Conditional skip transitions for low/medium risk
        "SYNTHESIS:COMPLETE"|"CLASSIFIED:COMPLETE"|"REQUIREMENTS:COMPLETE")
            return 0 ;;
        # Resolution cycle transitions
        "SYNTHESIS:IMPLEMENTATION"|"IMPLEMENTATION:SYNTHESIS"|"VALIDATION:SYNTHESIS")
            return 0 ;;
        *)
            return 1 ;;
    esac
}
```

## AGENT INTERACTION PROTOCOLS

### Parallel Agent Invocation Pattern
```python
# Template for requirements gathering
agent_prompt_template = """
Task: {task_description}
Mode: REQUIREMENTS_ANALYSIS
Current State: REQUIREMENTS → SYNTHESIS transition pending
Your Domain: {agent_domain}
Risk Level: {risk_level}

CRITICAL SCOPE ENFORCEMENT:
Only analyze files in ../task.md scope section.
STOP IMMEDIATELY if attempting unauthorized file access.

MANDATORY PERSISTENCE REQUIREMENTS:
Apply CLAUDE.md "Long-term Solution Persistence" principles.
Reject incomplete solutions when optimal solutions are achievable.
Use "Solution Quality Hierarchy" - prioritize OPTIMAL over EXPEDIENT.

MANDATORY OUTPUT REQUIREMENT:
Provide complete {agent_domain} requirements analysis.
End with: "REQUIREMENTS COMPLETE: {agent_domain} analysis provided"

MANDATORY EVIDENCE REQUIREMENT:
Document specific {agent_domain} requirements that implementation must satisfy.
Include success criteria, constraints, and quality thresholds.
"""

# Implementation
def invoke_requirements_agents(required_agents, task_context):
    agent_calls = []
    for agent in required_agents:
        agent_calls.append({
            'subagent_type': agent,
            'description': f'{agent} requirements analysis',
            'prompt': agent_prompt_template.format(
                agent_domain=agent.replace('-', ' '),
                **task_context
            )
        })

    # Execute all agents in parallel (single message, multiple tool calls)
    return execute_parallel_agents(agent_calls)
```

### Review Agent Invocation Pattern
```python
review_prompt_template = """
Task: {task_description}
Mode: FINAL_SYSTEM_REVIEW
Current State: REVIEW → COMPLETE transition pending
Your Phase 1 Requirements: {requirements_file}
Implementation Files: {modified_files}

CRITICAL PERSISTENCE VALIDATION:
Apply CLAUDE.md "Solution Quality Indicators" checklist.
Verify solution addresses root cause, follows best practices.
Reject if merely functional when optimal was achievable within scope.

MANDATORY FINAL DECISION FORMAT:
Must end response with exactly one of:
- "FINAL DECISION: ✅ APPROVED - All {domain} requirements satisfied"
- "FINAL DECISION: ❌ REJECTED - [specific issues requiring resolution]"

NO OTHER CONCLUSION FORMAT ACCEPTED.
This decision determines workflow continuation.
"""
```

## ERROR HANDLING & RECOVERY

### Transition Failure Recovery
```python
def handle_transition_failure(current_state, attempted_state, failure_reason):
    """Handle failed state transitions with appropriate recovery"""

    recovery_actions = {
        'REQUIREMENTS': {
            'missing_agent_reports': 're_invoke_missing_agents',
            'incomplete_analysis': 'request_complete_analysis',
            'scope_violations': 'enforce_scope_boundaries'
        },
        'REVIEW': {
            'agent_rejection': 'return_to_synthesis_cycle',
            'malformed_decision': 'request_proper_format',
            'missing_decisions': 're_invoke_agents'
        },
        'VALIDATION': {
            'build_failure': 'return_to_implementation',
            'test_failure': 'fix_tests_and_retry',
            'quality_gate_failure': 'address_quality_issues'
        }
    }

    action = recovery_actions.get(current_state, {}).get(failure_reason, 'restart_from_synthesis')
    execute_recovery_action(action, current_state, attempted_state, failure_reason)
```

### Multi-Instance Coordination
```bash
# Lock conflict resolution
handle_lock_conflict() {
    local task_name=$1

    echo "Lock conflict detected for task: $task_name"
    echo "Checking for available alternative tasks..."

    # Find next available task
    available_tasks=$(grep -E "^[[:digit:]]+\." /workspace/main/todo.md |
                     head -10 |
                     while read line; do
                         task=$(echo "$line" | sed 's/^[[:digit:]]*\. *//')
                         task_slug=$(echo "$task" | tr ' ' '-' | tr '[:upper:]' '[:lower:]')
                         if [ ! -f "../../../locks/${task_slug}.json" ]; then
                             echo "$task_slug"
                             break
                         fi
                     done)

    if [ -n "$available_tasks" ]; then
        echo "Selected alternative task: $available_tasks"
        execute_task_protocol "$available_tasks"
    else
        echo "No available tasks found. Exiting."
        exit 1
    fi
}
```

## COMPLIANCE VERIFICATION

### Pre-Task Validation Checklist
```bash
# MANDATORY before ANY task execution
pre_task_validation() {
    echo "=== PRE-TASK VALIDATION ==="

    # 1. Session ID validation
    [ -n "$SESSION_ID" ] && [ "$SESSION_ID" != "REPLACE_WITH_ACTUAL_SESSION_ID" ] || {
        echo "ERROR: Invalid session ID configuration"
        exit 1
    }

    # 2. Working directory verification
    [ "$(pwd)" = "/workspace/main" ] || {
        echo "ERROR: Must execute from main branch directory"
        exit 1
    }

    # 3. Todo.md accessibility
    [ -f "todo.md" ] || {
        echo "ERROR: todo.md not accessible"
        exit 1
    }

    echo "✅ Pre-task validation complete"
}
```

### Continuous Compliance Monitoring
```bash
# Execute after each state transition
post_transition_validation() {
    local new_state=$1

    echo "=== POST-TRANSITION VALIDATION: $new_state ==="

    # Verify state file updated correctly
    current_state=$(jq -r '.current_state' state.json 2>/dev/null || echo "ERROR")
    [ "$current_state" = "$new_state" ] || {
        echo "ERROR: State file not updated correctly"
        exit 1
    }

    # Verify required evidence present
    validate_state_evidence "$new_state" || {
        echo "ERROR: Required evidence missing for state $new_state"
        exit 1
    }

    echo "✅ Post-transition validation complete"
}
```

### Final Compliance Audit
```bash
# MANDATORY before CLEANUP state
final_compliance_audit() {
    echo "=== FINAL COMPLIANCE AUDIT ==="

    # Verify all states traversed correctly
    state_sequence=$(jq -r '.transition_log[] | .to' state.json | tr '\n' ' ')

    # Valid sequences: support multiple workflow variants
    high_risk_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS IMPLEMENTATION VALIDATION REVIEW COMPLETE"
    medium_risk_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS IMPLEMENTATION VALIDATION REVIEW COMPLETE"
    low_risk_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS COMPLETE"
    scope_negotiation_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS IMPLEMENTATION VALIDATION REVIEW SCOPE_NEGOTIATION COMPLETE"
    alternate_scope_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS IMPLEMENTATION VALIDATION REVIEW SCOPE_NEGOTIATION SYNTHESIS.*COMPLETE"
    config_only_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS COMPLETE"

    # Check against all valid sequences
    valid_sequence=false
    for sequence in "$high_risk_sequence" "$medium_risk_sequence" "$low_risk_sequence" "$scope_negotiation_sequence" "$config_only_sequence"; do
        if [[ "$state_sequence" =~ $sequence ]]; then
            valid_sequence=true
            echo "✅ Valid state sequence detected: matches $sequence pattern"
            break
        fi
    done

    # Also check alternate scope negotiation pattern
    if [[ "$valid_sequence" == "false" ]] && [[ "$state_sequence" =~ $alternate_scope_sequence ]]; then
        valid_sequence=true
        echo "✅ Valid state sequence detected: matches scope negotiation with resolution cycle"
    fi

    if [[ "$valid_sequence" == "false" ]]; then
        echo "ERROR: Invalid state sequence: $state_sequence"
        exit 1
    fi

    # Verify unanimous approval achieved
    approval_count=$(jq -r '.evidence.REVIEW | to_entries[] | select(.value == "APPROVED") | .key' state.json | wc -l)
    required_count=$(jq -r '.required_agents | length' state.json)

    [ "$approval_count" -eq "$required_count" ] || {
        echo "ERROR: Missing agent approvals: $approval_count/$required_count"
        exit 1
    }

    # Verify work preserved
    git log --oneline -5 | grep -i "$(jq -r '.task_name' state.json)" || {
        echo "ERROR: Task work not preserved in git history"
        exit 1
    }

    echo "✅ Final compliance audit passed"
}
```

## VIOLATION PREVENTION PATTERNS

### Pre-Task Validation Block
**MANDATORY before ANY task execution:**
```bash
# Session ID validation
export SESSION_ID="f33c1f04-94a5-4e87-9a87-4fcbc57bc8ec" && [ -n "$SESSION_ID" ] && [ "$SESSION_ID" != "REPLACE_WITH_ACTUAL_SESSION_ID" ] && echo "SESSION_ID_VALID: $SESSION_ID" || (echo "SESSION_ID_INVALID" && exit 1)

# Working directory verification
[ "$(pwd)" = "/workspace/main" ] || (echo "ERROR: Wrong directory" && exit 1)

# Todo.md accessibility
[ -f "todo.md" ] || (echo "ERROR: todo.md not accessible" && exit 1)
```

### Mandatory task.md Creation

**CRITICAL RESPONSIBILITY CLARIFICATION**:
- **WHO**: Main coordination agent (the agent that executed INIT and CLASSIFIED states)
- **WHEN**: During CLASSIFIED state, AFTER agent selection, BEFORE invoking any stakeholder agents
- **WHERE**: `/workspace/tasks/{TASK_NAME}/task.md` (task root, NOT inside code directory)
- **WHY**: Provides scope boundary enforcement for stakeholder agents and prevents unauthorized file access

**VERIFICATION BEFORE REQUIREMENTS STATE**:
```bash
[ -f "/workspace/tasks/${TASK_NAME}/task.md" ] || {
  echo "❌ CRITICAL ERROR: task.md not created by CLASSIFIED state"
  echo "Required location: /workspace/tasks/${TASK_NAME}/task.md"
  echo "ABORT: Cannot proceed to REQUIREMENTS state without task.md"
  exit 1
}
```

**Required Structure (must be created BEFORE stakeholder agent invocation):**
```markdown
# Task Context: {task-name}

## Task Objective
{task-description}

## Scope Definition
**FILES IN SCOPE:**
- [List exact files/directories that stakeholder agents are authorized to analyze]

**FILES OUT OF SCOPE:**
- [List directories/files explicitly excluded from analysis]

## Stakeholder Agent Reports
**Requirements Phase:**
- technical-architect-requirements.md (when completed)
- [other-agent]-requirements.md (when completed)

**Implementation Reviews:**
- technical-architect-review1.md (when completed)
- [other-agent]-review1.md (when completed)

## Implementation Status
- [ ] INIT: Task initialization
- [ ] CLASSIFIED: Risk assessment
- [ ] REQUIREMENTS: Stakeholder analysis
- [ ] SYNTHESIS: Architecture planning
- [ ] IMPLEMENTATION: Code creation
- [ ] VALIDATION: Build verification
- [ ] REVIEW: Final approval
- [ ] COMPLETE: Work preservation
- [ ] CLEANUP: Resource cleanup
```

**CRITICAL LIFECYCLE NOTE**: task.md is created during CLASSIFIED state and persists through entire task execution. It is ONLY removed during CLEANUP state along with all other task artifacts.

### Atomic Lock Acquisition Pattern
```bash
# MANDATORY atomic lock acquisition
export SESSION_ID="f33c1f04-94a5-4e87-9a87-4fcbc57bc8ec" && mkdir -p ../../../locks && (set -C; echo '{"session_id": "'${SESSION_ID}'", "start_time": "'$(date '+%Y-%m-%d %H:%M:%S %Z')'"}' > ../../../locks/{task-name}.json) 2>/dev/null && echo "LOCK_SUCCESS" || echo "LOCK_FAILED"

# Violation check
if [[ "$lock_result" != *"LOCK_SUCCESS"* ]]; then
    echo "Lock acquisition failed - selecting alternative task"
    select_alternative_task
fi
```

### Temporary File Management Setup
**BEFORE IMPLEMENTATION BEGINS (Mandatory for all tasks):**
```bash
# TEMP_DIRECTORY_CREATION: Set up isolated temporary file space
TASK_NAME=$(basename $(dirname $(pwd)))
TEMP_DIR="/tmp/task-${TASK_NAME}-$(date +%s)-$$" && mkdir -p "$TEMP_DIR"
echo "$TEMP_DIR" > .temp_dir && echo "TEMP_SETUP_SUCCESS: $TEMP_DIR"

# PURPOSE: All agents use consistent temporary file location outside git repository
# USAGE: Agents read temp directory with: TEMP_DIR=$(cat .temp_dir 2>/dev/null || echo "/tmp/fallback-$$")

# VIOLATION CHECK: Output must contain "TEMP_SETUP_SUCCESS"
if [[ "$temp_result" != *"TEMP_SETUP_SUCCESS"* ]]; then
    echo "WARNING: Temp directory creation failed - using task directory as fallback"
    TEMP_DIR=$(pwd)/.temp_fallback && mkdir -p "$TEMP_DIR"
fi
```

**AGENT INTEGRATION**: All agents should use temporary directory for:
- Analysis scripts and automation tools
- Performance benchmarking artifacts
- Security testing payloads and samples
- Debug logs and intermediate processing files
- Generated test data and mock objects

### Implementation Safety Guards
**Before ANY Write/Edit/MultiEdit operation:**
```bash
# Working directory validation
pwd | grep -q "/workspace/tasks/.*/code$" || (echo "ERROR: Invalid working directory" && exit 1)

# Clean repository state
git status --porcelain | grep -E "(dist/|node_modules/|target/|\.jar$)" && (echo "ERROR: Prohibited files detected" && exit 1)
```

### Build Validation Gates
**Mandatory after implementation:**
```bash
# Full verification (build + tests + linters) before completion
./mvnw verify -q || (echo "ERROR: Build/test/linter failure - task cannot be completed" && exit 1)

# Note: 'mvnw verify' executes: compile → test → checkstyle → PMD → all quality gates
```

### Decision Parsing Enforcement
**After ANY agent invocation:**
```bash
# Extract agent decision
decision=$(echo "$agent_response" | grep "FINAL DECISION:")

if [[ -z "$decision" ]]; then
    # Retry with format clarification
    retry_agent_with_format_requirement
elif [[ "$decision" == *"❌ REJECTED"* ]]; then
    # Handle rejection appropriately
    handle_agent_rejection
elif [[ "$decision" == *"✅ APPROVED"* ]]; then
    # Continue workflow
    continue_to_next_transition
fi
```

### Protocol Violation Reporting Pattern
```
MAINTAIN THROUGHOUT TASK EXECUTION:
1. Track all format violations in TodoWrite tool as separate item
2. Record: Agent type, state, violation type, retry outcome
3. Include in final task summary for human review

VIOLATION REPORT FORMAT:
"Protocol Violations Detected:
- [Agent]: [State] - Missing decision format (resolved via retry)
- [Agent]: [State] - Malformed decision (assumed REJECTED)
- Total violations: X, Auto-resolved: Y, Manual review needed: Z"

PURPOSE:
- Identify agent prompt issues needing refinement
- Track protocol compliance trends over time
- Enable proactive protocol improvements
- Maintain audit trail for debugging

IMPLEMENTATION:
violation_tracking = {
    "agent_name": {
        "state": "REVIEW",
        "violation_type": "missing_decision_format",
        "retry_outcome": "resolved",
        "timestamp": "2024-01-01T12:00:00Z"
    }
}

# Add to TodoWrite tracking
update_todo_with_violation_report()
```

## CONTEXT PRESERVATION RULES

### Single Session Continuity
**Requirements:**
- All task execution in single Claude session
- **EXPECTED USER INTERACTION**: Wait for user approval at two mandatory checkpoints:
  1. After SYNTHESIS: Plan approval before implementation
  2. After REVIEW: Change review before finalization
- NO OTHER HANDOFFS: Complete all other phases without waiting for user input
- IF session interrupted: Restart task from beginning with new session

**User Approval Checkpoints Are NOT Violations:**
- These are expected interaction points in the workflow
- These do NOT violate the "autonomous completion" principle
- These ensure user oversight at critical decision points

### Tool Call Batching
**Optimization patterns:**
```python
# Batch related operations in single message
parallel_tool_calls = [
    {"tool": "Task", "agent": "technical-architect", "mode": "requirements"},
    {"tool": "Task", "agent": "style-auditor", "mode": "requirements"},
    {"tool": "Task", "agent": "code-quality-auditor", "mode": "requirements"}
]

# Execute all in single message to reduce context fragmentation
execute_parallel_tools(parallel_tool_calls)
```

### State Persistence Patterns
**Critical state tracking:**
- Current state in state.json file
- Session ID in environment variables
- Working directory verification
- Lock status confirmation

**Verify state before each major operation:**
```bash
# State consistency check
current_state=$(jq -r '.current_state' state.json)
expected_state="$1"
[ "$current_state" = "$expected_state" ] || (echo "ERROR: State inconsistency" && exit 1)
```

## TOOL-SPECIFIC OPTIMIZATION PATTERNS

### Bash Tool Usage
```bash
# Combine related operations with safety checks
command1 && echo "SUCCESS" || (echo "FAILED" && exit 1)

# Verify outputs before proceeding
result=$(command_with_output)
[ -n "$result" ] || (echo "ERROR: Command produced no output" && exit 1)

# Avoid interactive commands
# ❌ git rebase -i
# ✅ git rebase main
```

### Read Tool Usage
```python
# Batch reads for related content
related_files = [
    "/path/to/config.md",
    "/path/to/implementation.java",
    "/path/to/test.java"
]

# Read multiple files in sequence within single operation
for file_path in related_files:
    content = read_tool(file_path)
    analyze_content(content)
```

### Task Tool Usage
```python
# Parallel agent calls in single message
agent_calls = [
    {
        'subagent_type': 'technical-architect',
        'description': 'Architecture requirements',
        'prompt': structured_prompt_template.format(domain='architecture')
    },
    {
        'subagent_type': 'style-auditor',
        'description': 'Style requirements',
        'prompt': structured_prompt_template.format(domain='style')
    }
]

# Execute all agents simultaneously
execute_parallel_agents(agent_calls)
```

### TodoWrite Tool Usage
```python
# Frequent progress updates
update_todo_progress("requirements", "in_progress", "Gathering stakeholder requirements")

# Consistent format patterns
todo_item = {
    "content": "Implement feature X with requirements Y",
    "status": "in_progress",
    "activeForm": "Implementing feature X according to stakeholder specifications"
}

# State verification through TodoWrite
verify_workflow_position_via_todo_state()
```

## ERROR RECOVERY PROTOCOLS

### Violation Detection Triggers
**Automatic violation detection:**
- Wrong directory (pwd check fails)
- Missing locks (lock file check fails)
- Build failures (non-zero exit codes)
- Git operation failures (rebase/merge fails)
- Prohibited files detected (status check fails)

**Recovery Action**: TERMINATE current task, restart from INIT state

### Multi-Instance Conflict Resolution
**Lock conflict handling:**
```bash
handle_lock_conflict() {
    local task_name=$1
    echo "Lock conflict detected for task: $task_name"

    # Find alternative available task
    available_task=$(find_next_available_task)

    if [ -n "$available_task" ]; then
        echo "Selected alternative task: $available_task"
        execute_task_protocol "$available_task"
    else
        echo "No available tasks found. Exiting."
        exit 1
    fi
}
```

### Partial Completion Recovery
**Task interruption handling:**
```bash
if [ -f "state.json" ]; then
    session_id=$(jq -r '.session_id' state.json)
    current_session="$SESSION_ID"

    if [ "$session_id" = "$current_session" ]; then
        # Resume from last completed state
        resume_from_last_state
    else
        # Foreign session - select different task
        select_alternative_task
    fi
fi
```

## WORKFLOW EXECUTION ENGINE

### Main Task Execution Function
```bash
execute_task_protocol() {
    local task_name=$1

    # Initialize state machine
    initialize_task_state "$task_name"

    # Execute state machine with risk-based path selection
    while [ "$(get_current_state)" != "CLEANUP" ]; do
        current_state=$(get_current_state)
        risk_level=$(jq -r '.risk_level' state.json 2>/dev/null || echo "HIGH")
        change_type=$(determine_change_type)

        echo "=== STATE: $current_state (Risk: $risk_level, Type: $change_type) ==="

        case "$current_state" in
            "INIT")
                execute_init_to_classified_transition
                ;;
            "CLASSIFIED")
                execute_classified_to_requirements_transition
                ;;
            "REQUIREMENTS")
                execute_requirements_to_synthesis_transition
                ;;
            "SYNTHESIS")
                # Present implementation plan and wait for user approval
                present_implementation_plan_and_wait_for_approval

                # Conditional path selection based on risk level and change type
                if should_skip_implementation "$risk_level" "$change_type"; then
                    execute_synthesis_to_complete_transition
                else
                    execute_synthesis_to_implementation_transition
                fi
                ;;
            "IMPLEMENTATION")
                execute_implementation_to_validation_transition
                ;;
            "VALIDATION")
                execute_validation_to_review_transition
                ;;
            "REVIEW")
                execute_review_to_complete_or_scope_negotiation_transition

                # If unanimous approval achieved, present changes and wait for user review
                if all_agents_approved; then
                    present_changes_and_wait_for_user_approval
                fi
                ;;
            "SCOPE_NEGOTIATION")
                execute_scope_negotiation_to_synthesis_or_complete_transition
                ;;
            "COMPLETE")
                execute_complete_to_cleanup_transition
                ;;
            *)
                echo "ERROR: Unknown state: $current_state"
                exit 1
                ;;
        esac

        # Post-transition validation
        post_transition_validation "$(get_current_state)"
    done

# Helper function to determine if implementation should be skipped
should_skip_implementation() {
    local risk_level=$1
    local change_type=$2

    case "$risk_level" in
        "HIGH")
            return 1  # Never skip for high risk
            ;;
        "MEDIUM")
            if [[ "$change_type" == "documentation_only" || "$change_type" == "config_only" ]]; then
                return 0  # Skip for medium risk documentation/config changes
            else
                return 1  # Don't skip for other medium risk changes
            fi
            ;;
        "LOW")
            return 0  # Always skip for low risk
            ;;
        *)
            return 1  # Default to not skip if uncertain
            ;;
    esac
}

    # Final audit
    final_compliance_audit
    echo "=== TASK PROTOCOL COMPLETED SUCCESSFULLY ==="
}

# Helper functions for user approval checkpoints
present_implementation_plan_and_wait_for_approval() {
    echo "=== USER APPROVAL CHECKPOINT: PLAN REVIEW ==="
    echo "Synthesis complete. Presenting implementation plan to user..."

    # Present plan to user in clear, readable format including:
    # - Architecture approach and key design decisions
    # - Files to be created/modified
    # - Implementation sequence and dependencies
    # - Testing strategy
    # - Risk mitigation approaches

    # Wait for user approval message
    # Only proceed after receiving explicit confirmation (e.g., "yes", "approved", "proceed")
    # PROHIBITED: Assuming approval from bypass mode or lack of objection
}

present_changes_and_wait_for_user_approval() {
    echo "=== USER APPROVAL CHECKPOINT: CHANGE REVIEW ==="
    echo "All stakeholder agents have approved. Committing changes for user review..."

    # Step 1: Commit all changes to task branch
    git add <changed-files>
    git commit -m "<descriptive-commit-message>"
    COMMIT_SHA=$(git rev-parse HEAD)
    echo "Changes committed: $COMMIT_SHA"

    # Step 2: Present change summary including:
    # - Commit SHA for reference
    # - Files modified/created/deleted (git diff --stat)
    # - Key implementation decisions made
    # - Test results and quality gate status
    # - Summary of how each stakeholder requirement was addressed

    # Step 3: Ask user for approval
    # Ask: "All stakeholder agents have approved. Changes committed to task branch (SHA: $COMMIT_SHA). Please review the changes. Would you like me to proceed with finalizing (COMPLETE → CLEANUP)?"

    # Step 4: Wait for user response
    # If user requests changes:
    #   - Make requested modifications
    #   - Amend commit: git add <files> && git commit --amend --no-edit
    #   - Return to Step 2 with new commit SHA
    # If user approves:
    #   - Proceed to COMPLETE state
}
```

## MIGRATION FROM PHASE-BASED PROTOCOL

**Phase-to-State Mapping**: Phase 1=REQUIREMENTS, Phase 2=SYNTHESIS, Phase 3=IMPLEMENTATION, Phase 4=VALIDATION, Phase 5=Resolution cycles, Phase 6=REVIEW, Phase 7=CLEANUP

**Enforcement**: Mandatory transition conditions with no manual overrides or exceptions

**END OF STATE MACHINE PROTOCOL**

