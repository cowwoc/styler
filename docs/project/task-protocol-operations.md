# Task State Machine Protocol - Operations & Procedures

> **Version:** 2.1 | **Last Updated:** 2025-12-10
> **Related Documents:** [CLAUDE.md](../../CLAUDE.md) • [task-protocol-core.md](task-protocol-core.md) •
[task-protocol-risk-agents.md](task-protocol-risk-agents.md)

**IMPORTANT**: Read [task-protocol-core.md](task-protocol-core.md) first for state machine architecture.

---

## Quick Navigation {#quick-navigation-index}

**Skills** (invoke via Skill tool): `recover-from-error`, `state-transition`, `task-cleanup`

| Need | Section |
|------|---------|
| Optimization | [Predictive Prefetching](#predictive-prefetching-init---mandatory), [Fail-Fast Validation](#fail-fast-validation-implementation) |
| Troubleshooting | [Troubleshooting](#troubleshooting) |
| Error Recovery | [Error Recovery Protocols](#error-handling-recovery) |
| Multi-Instance | [Multi-Instance Coordination](#multi-instance-coordination) |
| Cleanup | [CLEANUP State](#cleanup-final-state) |

---

## COMPLETE to CLEANUP Transition {#complete-cleanup}

**Mandatory Conditions (ALL must be satisfied BEFORE transition):**
1. All work already committed to task branch (from USER REVIEW checkpoint)
2. todo.md removed from todo list AND added to changelog.md
3. Dependent tasks updated in todo.md (BLOCKED -> READY if all dependencies satisfied)
4. todo.md and changelog.md changes amended to existing commit
5. Build verification passes in task worktree BEFORE merge (`./mvnw clean verify` - clean build prevents stale module-info.class)
6. Task branch merged to main with `--ff-only` (linear history, NO merge commits)
7. Full build verification passes on main branch AFTER merge (`./mvnw verify`)

### Post-Merge Build Failure Recovery Protocol {#post-merge-build-failure-recovery}

If `./mvnw verify` fails on main AFTER merge (despite passing in worktree), execute these 7 steps:

1. **DO NOT create new commits** on main branch to fix build issues
2. **Immediately revert the merge**:
   ```bash
   cd /workspace/main
   git reset --hard HEAD~1  # Undo the merge commit
   ```
3. **Return to task worktree** to investigate:
   ```bash
   cd /workspace/tasks/{task-name}/code
   ./mvnw clean verify  # Use clean build to detect stale cache issues
   ```
4. **Root cause analysis**:
   - Maven build cache corruption (stale module-info.class from compile-only builds)
   - Missing module dependencies in test module-info.java
   - Circular module dependencies not caught in worktree
   - Test module name conflicts with main module
5. **Fix in worktree**, then amend the commit:
   ```bash
   # After fixing the issue
   git add -u
   git commit --amend --no-edit
   ```
6. **Re-verify before retry**:
   ```bash
   ./mvnw clean verify  # Must pass with clean build
   ```
7. **Retry merge to main** only after worktree clean build passes

**CRITICAL**: Main branch must NEVER contain broken commits. If build failures occur post-merge, the merge was premature and must be undone.

**Evidence Required:**
- Pre-merge build success in worktree (`./mvnw clean verify` passes BEFORE merge)
- Git log showing clean linear history (no merge commits)
- todo.md modification in final commit (verify: `git show --stat | grep "todo.md"`)
- Main branch build success AFTER integration (`./mvnw verify` passes)
- All deliverables preserved in main branch

### Pre-Merge Verification Pattern (Parallel Agent Launch) {#pre-merge-verification-parallel}

**CRITICAL: Pre-Merge Build Verification Gate**
```bash
# Launch ALL agents with Task tool in ONE message
Task tool (architect), model: opus, prompt: "Review architecture requirements for {task}"
Task tool (quality), model: opus, prompt: "Review code quality requirements for {task}"
Task tool (style), model: opus, prompt: "Review style requirements for {task}"
Task tool (security), model: opus, prompt: "Review security requirements for {task}"

# Wait for ALL responses before proceeding
```

**DON'T** (sequential):
```bash
# Message 1
Task tool (architect), model: opus, prompt: "..."
# Wait for response

# Message 2
Task tool (quality), model: opus, prompt: "..."
# Wait for response

# = 3-4x overhead, 20-30 min wasted
```

---

## Predictive Prefetching (INIT) - MANDATORY {#predictive-prefetching-init---mandatory}

**DO** (single round-trip - predict and load upfront):
```bash
Read pom.xml + Read checkstyle.xml + Read pmd.xml + \
Glob "src/main/java/**/*Target*.java" + \
Glob "src/test/java/**/*Test.java" + \
Read docs/project/architecture.md
# = 1 round-trip, all resources loaded
```

**DON'T** (sequential discovery - 5-10 round-trips, 5-10 min wasted):
```bash
Read pom.xml
# Discover need for checkstyle.xml
Read checkstyle.xml
# Discover need for implementation files...
```

---

## Fail-Fast Validation (IMPLEMENTATION) {#fail-fast-validation-implementation}

**DO** (incremental validation after each component):
```bash
set -e  # Exit on failure

Edit src/main/java/Component.java
./mvnw compile -q -pl :{module}
./mvnw checkstyle:check -q -pl :{module}

Edit src/test/java/ComponentTest.java
./mvnw test -Dtest=ComponentTest -q

git add -A && git commit -m "Implement Component (validated)"
```

**DON'T** (late discovery - fix with stale context 30 min later):
```bash
Edit Component1.java
Edit Component2.java
Edit Component3.java
./mvnw checkstyle:check  # FAILED - 60 violations
```

---

## Pre-Validation Checklist (IMPLEMENTATION Exit) {#pre-validation-checklist-implementation-exit}

**MANDATORY GATES** (verify-implementation-exit.sh enforces):

```bash
# Gate 1: Clean working directory
git status --porcelain src/
# Must be empty - all changes committed

# Gate 2: All tests passing
./mvnw test
# Exit code: 0 (all tests pass)

# Gate 3: Style compliance
./mvnw checkstyle:check pmd:check
# Exit code: 0 (zero violations)

# Gate 4: Build verification (clean build prevents stale module-info.class)
./mvnw clean verify
# Exit code: 0 (full build passes with clean compilation)
```

**Required Coverage Areas:**
- Input validation (null, empty, invalid values)
- Business rules and constraints
- Edge cases and boundary conditions
- Error handling paths
- Integration points between components

**JPMS Module Compilation Verification:**
- Test module descriptor exists: `src/test/java/module-info.java`
- Test module name uses `.test` suffix (e.g., `io.github.styler.parser.test`)
- Test module requires main module: `requires io.github.styler.parser;`
- Test framework dependency: `requires org.testng;` (not `requires transitive`)
- Package opens for reflection: `opens X to org.testng;` (NOT `exports`)
- No circular dependencies: Test requires main, main does NOT require test
- Clean build verification: `./mvnw clean verify` (detects stale module-info.class)

**Common JPMS Issues Checklist:**
- [ ] Test module name conflicts with main module (both named `io.github.styler.parser`)
- [ ] Missing `opens` declarations for test packages (TestNG needs reflection access)
- [ ] Using `exports` instead of `opens` for test packages (tests fail at runtime)
- [ ] Test module-info.java missing `requires org.testng;`
- [ ] Stale module-info.class from previous `./mvnw compile` without package phase
- [ ] Circular dependency: main module accidentally requires test module

**Verification Commands:**
```bash
# Verify test module descriptor
cat src/test/java/module-info.java

# Check for module resolution errors
./mvnw clean compile 2>&1 | grep -i "module not found"

# Verify tests run with modules
./mvnw clean test -X 2>&1 | grep "module-info"
```

**Recovery when blocked:**
```bash
jq '.state = "IMPLEMENTATION"' $LOCK_FILE > /tmp/lock.json && mv /tmp/lock.json $LOCK_FILE
# Fix: Uncommitted changes (git add && git commit), Test failures, Style violations, Build failures
```

---

## Implementation Rounds {#implementation-rounds-reviewimplementation-iteration}

**CRITICAL PATTERN**: Agents in BOTH implementation mode (Haiku) and review mode (Opus) in iterative cycles.

**Round 1 - Initial Implementation (Haiku, all agents in SINGLE message):**
```
Task tool (architect), model: haiku, prompt: "Implement core interfaces per requirements"
Task tool (quality), model: haiku, prompt: "Apply design patterns per requirements"
Task tool (style), model: haiku, prompt: "Ensure code follows style guidelines"
Task tool (test), model: haiku, prompt: "Implement test suite per strategy"
```

**Round 1 - Review Merged Changes (Opus, all agents in SINGLE message):**
```
Task tool (architect), model: opus, prompt: "Review merged architecture on task branch"
Task tool (engineer), model: opus, prompt: "Review merged code quality on task branch"
Task tool (formatter), model: opus, prompt: "Review merged style compliance on task branch"
Task tool (tester), model: opus, prompt: "Review merged test coverage on task branch"
```

**Round 2 - Apply Review Feedback (if rejections, Haiku):**
```
Task tool (formatter), model: haiku, prompt: "Fix 12 style violations from review"
Task tool (architect), model: haiku, prompt: "Clarify interface contracts per feedback"
```

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

**DON'T** (iterative fixing - 60 verification cycles = 45-50 min wasted):
```bash
Fix checkstyle violation 1
./mvnw checkstyle:check
Fix checkstyle violation 2
./mvnw checkstyle:check
# ... repeat 60 times ...
```

### Multi-Agent Commit History {#multi-agent-implementation-commit-history-examples}

**INCORRECT Pattern - Single Commit (Protocol Violation)**:
```bash
$ git log --oneline task-branch
abc123d Implement FormattingRule system (Claude)
```
**Analysis**: Single commit suggests main agent implemented directly, violating protocol.

**CORRECT Pattern - Multiple Agent Commits**:
```bash
$ git log --oneline task-branch
jkl012m [test] Add comprehensive test suite for FormattingRule
ghi789j [style] Implement JavaDoc requirements for public APIs
def456g [quality] Apply factory pattern to rule instantiation
abc123d [architect] Add FormattingRule interface hierarchy
```
**Analysis**: Multiple commits with agent attribution prove multi-agent implementation.

**CORRECT Pattern - Detailed View with Attribution**:
```bash
$ git log --format='%h %s' task-branch
jkl012m [test] Add comprehensive test suite for FormattingRule
    - Unit tests for all FormattingRule implementations
    - Integration tests for rule composition
    - Test coverage: 95%+

ghi789j [style] Implement JavaDoc requirements for public APIs
    - Added JavaDoc to all public methods
    - Fixed checkstyle violations
    - Code style compliance verified

def456g [quality] Apply factory pattern to rule instantiation
    - Created RuleFactory for centralized creation
    - Applied builder pattern for complex rules
    - Reduced cyclomatic complexity

abc123d [architect] Add FormattingRule interface hierarchy
    - Created FormattingRule interface
    - Implemented concrete rule classes
    - Defined rule composition API
```

**Verification Commands:**
```bash
# Count agent commits (expected: 3+ different agents)
git log --oneline task-branch | grep -E '\[(architect|engineer|formatter|tester|builder)\]' | wc -l
# Expected: 3+ (at least 3 different agents contributed)

# List all contributing agents
git log --format='%s' task-branch | grep -oP '\[\K[^]]+' | sort -u
# Expected output:
# architect
# quality
# style
# test

# Verify parallel execution (commits within minutes, not hours)
git log --format='%h %ai %s' task-branch
# Expected: Commits within minutes of each other, not hours apart
```

**Main Agent Final Commit Example:**
```bash
$ git log --format='%h %s%n%b' main --max-count=1
abc123z Implement FormattingRule system

Contributing agents:
- architect: Core interface design
- quality: Design pattern application
- style: Code style compliance
- test: Test suite implementation

Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Red Flags (Protocol Violations):**
- Only 1 commit in task branch (main agent implemented directly)
- All commits by generic "Claude" (no agent names)
- Commits hours apart (sequential, not parallel)
- No agent names in commit messages
- Commit messages don't identify domain (architecture/engineer/formatter/tester)

**Green Flags (Compliant):**
- 3+ commits with agent-specific attribution
- Agent names in commit messages: `[agent-name]`
- Commits within minutes (parallel execution)
- Clear domain separation in commit subjects
- Main agent final commit lists all contributors

---

## TROUBLESHOOTING {#troubleshooting}

### Implementation Rounds Taking >15 Minutes
**Root Causes:** (1) Not using batch fixing (2) Not launching agents in parallel (3) Fail-fast not working

**Solutions:**
1. Collect ALL issues from all agents BEFORE fixing
2. Fix all issues of same type together
3. Launch ALL agents in SINGLE message
4. Add incremental checks during IMPLEMENTATION

### High Token Usage (>65,000)
**Root Causes:** (1) Verbose agent responses (full code) (2) Redundant file reads (3) Sequential discovery

**Solutions:**
1. Agents return metadata only ("Implemented 3 files. See commit abc123.")
2. Pre-fetch all resources in INIT (single message)
3. Predict dependencies upfront, load ALL in INIT

### Unanimous Approval Not Achieved
**Root Causes:** (1) Issues not fully addressed (2) Scope exceeding boundaries (3) Conflicting requirements

**Solutions:**
1. Verify all issues addressed, run `./mvnw verify`
2. If effort >2x scope: return to SYNTHESIS, create follow-up tasks in todo.md
3. Resolve conflicts by domain authority priority

---

## BEST PRACTICES {#best-practices}

### Risk Assessment {#1-risk-assessment}
```python
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

### Agent Selection {#2-agent-selection}

```python
# HIGH-RISK (mandatory agents)
agents = ["architect", "quality", "style", "test"]

# Add conditional agents based on task description
if "security" in task_description or "authentication" in task_description:
    agents.append("security")

if "performance" in task_description or "algorithm" in task_description:
    agents.append("performance")

# MEDIUM-RISK (core only)
agents = ["architect", "quality"]

# LOW-RISK (minimal or none)
agents = []  # or ["quality"] for technical docs
```

### Lock State Management {#3-lock-state-management}
```bash
update_lock() {
    local TASK=$1
    local NEW_STATE=$2
    VALID_STATES="INIT|CLASSIFIED|REQUIREMENTS|SYNTHESIS|IMPLEMENTATION|VALIDATION|REVIEW|AWAITING_USER_APPROVAL|COMPLETE|CLEANUP|SCOPE_NEGOTIATION"
    if ! echo "$NEW_STATE" | grep -qE "^($VALID_STATES)$"; then
        echo "ERROR: Invalid state '$NEW_STATE'" >&2
        return 1
    fi
    jq ".state = \"$NEW_STATE\"" /workspace/tasks/${TASK}/task.json > /tmp/lock.tmp
    mv /tmp/lock.tmp /workspace/tasks/${TASK}/task.json
}
```

**CRITICAL: todo.md Commit Verification (MANDATORY BEFORE merging to main):**
```bash
git show --stat | grep "todo.md" || { echo "VIOLATION: todo.md not in commit"; exit 1; }
git show --name-only | grep "todo.md" || { echo "VIOLATION: todo.md not modified"; exit 1; }
```

---

## Git Safety Sequence (Concurrency-Safe) {#git-safety-sequence}

**IMPORTANT**: Replace {TASK_NAME} with actual task name. Safe for concurrent execution.

```bash
# Step 1: Ensure clean working state
cd /workspace/tasks/{TASK_NAME}/code
rm -f .temp_dir
git status --porcelain | grep -E "(dist/|target/|\.temp_dir$)" | wc -l  # Must be 0

# Step 2: Archive completed task from todo.md to changelog.md
# CRITICAL: Changes MUST be in same commit as deliverables
# 2a. Remove completed task entry from todo.md (delete entire task section)
# 2b. Add completed task to changelog.md under today's date (## YYYY-MM-DD)
# 2c. Include completion details: solution, files modified, tests, quality gates

# Step 2d: Update dependent tasks in todo.md (with file locking)
TODO_LOCK="/workspace/.todo.md.lock"
exec 200>"$TODO_LOCK"
flock -x 200 || { echo "ERROR: Could not acquire todo.md lock"; exit 1; }
# For each task marked as BLOCKED:
#   - Check if completed task was listed in Dependencies
#   - If ALL dependencies complete (check changelog.md), change BLOCKED to READY
flock -u 200

# Step 3: Commit ALL changes together (implementation + todo.md + changelog.md)
git add <changed-files> todo.md changelog.md
git commit -m "Descriptive commit message for task changes"

# Step 4: Verify todo.md and changelog.md are included in the commit
git show --stat | grep "todo.md" || { echo "ERROR: todo.md not in commit"; exit 1; }
git show --stat | grep "changelog.md" || { echo "ERROR: changelog.md not in commit"; exit 1; }

# Step 5: Fetch latest main and rebase to create linear history
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git rebase origin/main

# Step 6: PRE-MERGE BUILD VERIFICATION (CRITICAL GATE)
./mvnw verify || {
    echo "VIOLATION: Build fails in worktree - merge BLOCKED"
    echo "Fix all checkstyle, PMD, and test failures before merging"
    exit 1
}

# Step 7: Linear merge to main branch (fast-forward only)
cd /workspace/main
git merge --ff-only {TASK_NAME} || {
    echo "Fast-forward merge failed - non-linear history detected"
    echo "Solution: Return to task worktree, fetch latest main, rebase, and retry"
    cd /workspace/tasks/{TASK_NAME}/code
    git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
    git rebase origin/main
    ./mvnw verify || { echo "Build failed after rebase"; exit 1; }
    cd /workspace/main
    git merge --ff-only {TASK_NAME} || exit 1
}

# Step 8: Post-merge verification (from main worktree)
git log --oneline -5  # Must show task commit at HEAD
git show HEAD --stat | grep "todo.md" || { echo "ERROR: todo.md missing in merged commit"; exit 1; }
git show HEAD --stat | grep "changelog.md" || { echo "ERROR: changelog.md missing in merged commit"; exit 1; }

# Step 9: Verify build on main (MANDATORY after merge)
./mvnw verify || {
    echo "CRITICAL: Build fails on main after merge"
    echo "This should not happen if Step 6 passed - investigate immediately"
    exit 1
}
```

**Concurrency Safety Guarantees:**
- Linear history: `--ff-only` prevents merge commits
- Automatic conflict detection: Non-fast-forward fails cleanly
- Clear retry: Fetch + rebase + retry on conflict
- Build verification: Tests run BEFORE and AFTER merge
- Atomic commit: All changes (code + todo.md + changelog.md) in single commit

**Example for task "implement-formatter-api"**:
```bash
# Step 1: Ensure clean state (in task worktree)
cd /workspace/tasks/implement-formatter-api/code
git status --porcelain | grep -E "(dist/|target/)" | wc -l  # Must be 0

# Step 2: Archive task and update dependencies BEFORE committing
# 2a-c. Remove task from todo.md, add to changelog.md with completion details
# 2d. Update dependent tasks: check each BLOCKED task, if all dependencies complete, change to READY
# (Use Edit tool to update todo.md and changelog.md)

# Step 3: Commit ALL changes together (implementation already staged from VALIDATION)
git add . todo.md changelog.md
git commit -m "Implement FormattingRule API with core interfaces"

# Step 4: Verify todo.md and changelog.md included
git show --stat | grep "todo.md"
git show --stat | grep "changelog.md"

# Step 5: Fetch and rebase onto main
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git rebase origin/main

# Step 6: PRE-MERGE BUILD VERIFICATION (blocks merge if fails)
./mvnw verify || {
    echo "❌ Build failed - fix violations before merging"
    exit 1
}

# Step 7: Linear merge to main branch (fast-forward only)
cd /workspace/main
git merge --ff-only implement-formatter-api || {
    echo "❌ Fast-forward merge failed - rebasing..."
    cd /workspace/tasks/implement-formatter-api/code
    git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
    git rebase origin/main
    ./mvnw verify || { echo "❌ Build failed after rebase"; exit 1; }
    # Retry merge
    cd /workspace/main
    git merge --ff-only implement-formatter-api
}

# Step 8: Post-merge verification (from main worktree)
git log --oneline -5
git show HEAD --stat | grep "todo.md"
git show HEAD --stat | grep "changelog.md"

# Step 9: Verify build on main
./mvnw verify
```

**Fast-Forward Validation - CORRECT:**
```
* 7f8deba (HEAD -> main) Implement CLI security basics (SEC-001 through SEC-007)
* 6a2b1c3 Previous commit on main
* 5d9e4f2 Earlier commit
```

**WRONG (merge commit = non-linear history):**
```
*   8g9h5i6 (HEAD -> main) Merge branch 'implement-cli-security-basics'
|\
| * 7f8deba Implement CLI security basics
|/
* 6a2b1c3 Previous commit on main
```

---

## Dependent Task Update Procedure {#dependent-task-update-procedure}

**Purpose**: Automatically unblock tasks when dependencies satisfied.
**When**: Step 2d of COMPLETE->CLEANUP (AFTER archiving to changelog.md).

```bash
update_dependent_tasks() {
    local completed_task=$1
    local todo_file="todo.md"
    local changelog_file="changelog.md"

    echo "=== UPDATING DEPENDENT TASKS ==="
    echo "Completed task: $completed_task"

    # Find all BLOCKED tasks in todo.md
    grep -n "BLOCKED:" "$todo_file" | while IFS=: read line_num task_entry; do
        task_name=$(echo "$task_entry" | grep -oP '`\K[^`]+' | head -1)
        dependencies=$(sed -n "${line_num},/^$/p" "$todo_file" | grep "Dependencies:" | sed 's/.*Dependencies: *//' | sed 's/ *$//')

        if [ -z "$dependencies" ]; then
            continue
        fi

        echo "Checking task: $task_name"
        echo "  Dependencies: $dependencies"

        all_satisfied=true
        IFS=',' read -ra DEPS <<< "$dependencies"
        for dep in "${DEPS[@]}"; do
            dep=$(echo "$dep" | xargs)
            if ! grep -q "$dep" "$changelog_file"; then
                echo "  Dependency $dep not yet complete"
                all_satisfied=false
                break
            else
                echo "  Dependency $dep complete"
            fi
        done

        if [ "$all_satisfied" = true ]; then
            echo "  All dependencies satisfied - updating to READY"
            sed -i "${line_num}s/BLOCKED:/READY:/" "$todo_file"
            echo "  Task $task_name updated to READY"
        else
            echo "  Task $task_name remains BLOCKED"
        fi
    done
    echo "=== DEPENDENT TASK UPDATE COMPLETE ==="
}

# Usage in Step 2d:
update_dependent_tasks "implement-index-overlay-parser"
```

**Validation:**
```bash
# Verify dependent tasks were updated
grep "implement-line-length-formatter" todo.md | grep -q "READY:" && echo "Task unblocked" || echo "Task still blocked"
```

---

## Automatic Conflict Resolution {#automatic-conflict-resolution-via-atomic-operations}

**DESIGN PRINCIPLE**: Race conditions prevented automatically through atomic operations.

**Scenario 1: Both instances attempt same task simultaneously**
- Instance A: `mkdir /workspace/tasks/task-x` -> SUCCESS (directory created)
- Instance B: `mkdir /workspace/tasks/task-x` -> FAILS (directory exists)
- Instance B: Automatically releases lock and selects alternative task
- Instance A: Continues work uninterrupted

**Scenario 2: Instance A working, Instance B attempts same task**
- Instance A: Has lock, has worktree directory
- Instance B: Lock creation fails (`set -C` prevents overwrite) -> LOCK_FAILED
- Instance B: Selects alternative task immediately (never attempts directory creation)
- Instance A: Unaffected, continues work

**Scenario 3: Lock acquired but directory creation fails**
- Instance: Lock created successfully
- Instance: Directory creation fails (another instance's directory exists)
- Instance: Automatically releases lock via error handler: `rm /workspace/tasks/{TASK_NAME}/task.json`
- Instance: Selects alternative task
- Other instance's work: Preserved (directory and worktree untouched)

**Key Safety Features:**
1. Lock Creation: `set -C` prevents overwriting existing locks
2. Directory Creation: `mkdir` (not `mkdir -p`) fails atomically if exists
3. Automatic Cleanup: Error handlers release locks on conflict
4. Work Preservation: Existing worktrees never deleted by conflicting instance
5. Clear Signals: LOCK_FAILED / DIRECTORY_FAILED / WORKTREE_FAILED indicate next action

**No Manual Recovery Required:**
- Atomic operations handle all race conditions automatically
- Error handlers ensure proper cleanup on conflict
- Clear failure signals direct instance to select alternative task
- Other instance's work always preserved

---

## CLEANUP (Final State) {#cleanup-final-state}

**Mandatory Phases (ORDERED EXECUTION REQUIRED):**

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
- [ ] Remove all agent branches (use -D force delete, squash makes them unreachable)
- [ ] Remove task worktree
- [ ] Remove task branch (use -D force delete)
- [ ] Verify removal: `git worktree list | grep {TASK}` returns empty

**Phase 4: Garbage Collection (MANDATORY after squash merge)**
- [ ] Run `git gc --prune=now` to remove orphaned commits from squash merge
- [ ] Rationale: Squash merge creates new commit on main, original task commits become unreferenced
- [ ] Without garbage collection, orphaned commits persist indefinitely

**Phase 5: Lock and Directory Removal (ONLY after Phases 3-4 succeed)**
- [ ] Remove lock file: `rm /workspace/tasks/{TASK}/task.json`
- [ ] Remove entire task directory: `rm -rf /workspace/tasks/{TASK}`
- [ ] Verify removal: `[ ! -d /workspace/tasks/{TASK} ]`

**Phase 6: Temporary File Cleanup**
- [ ] Remove temporary files (if any)
- [ ] Archive state.json file (if needed for audit trail)

**Verification Commands (Execute BEFORE marking CLEANUP complete):**
```bash
# Verify ALL agent worktrees removed
git worktree list | grep "/workspace/tasks/{TASK_NAME}/agents" && {
    echo "FAILED: Agent worktrees still exist:"
    git worktree list | grep "/workspace/tasks/{TASK_NAME}/agents"
    exit 1
} || echo "All agent worktrees removed"

# Verify task worktree removed
git worktree list | grep "/workspace/tasks/{TASK_NAME}/code" && {
    echo "FAILED: Task worktree still exists"
    exit 1
} || echo "Task worktree removed"

# Verify lock file removed
[ -f "/workspace/tasks/{TASK_NAME}/task.json" ] && {
    echo "FAILED: Lock file still exists"
    exit 1
} || echo "Lock file removed"

# Verify entire task directory removed
[ -d "/workspace/tasks/{TASK_NAME}" ] && {
    echo "FAILED: Task directory still exists"
    exit 1
} || echo "Task directory removed"
```

**Implementation (Concurrency-Safe):**
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name
# IMPORTANT: Replace {AGENT_LIST} with space-separated agent names

# Step 1: Verify all agents reached COMPLETE status
AGENTS="{AGENT_LIST}"  # e.g., "architect engineer formatter security"
for agent in $AGENTS; do
  STATUS=$(jq -r '.status' "/workspace/tasks/{TASK_NAME}/agents/$agent/status.json" 2>/dev/null || echo "MISSING")
  if [ "$STATUS" != "COMPLETE" ]; then
    echo "FATAL: Agent $agent status = $STATUS (expected: COMPLETE)"
    echo "Cannot proceed to CLEANUP until all agents reach COMPLETE status"
    exit 1
  fi
  echo "Agent $agent status verified: COMPLETE"
done

# Step 2: Verify work preserved (can check from task worktree)
cd /workspace/tasks/{TASK_NAME}/code
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5 | grep "{TASK_NAME}" || {
    echo "FATAL: Work not found in main branch"
    exit 1
}

# Step 3: Verify lock ownership
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/tasks/{TASK_NAME}/task.json")
if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "FATAL: Cannot delete lock owned by $LOCK_OWNER"
  exit 1
fi

# Step 4: Change to main worktree (REQUIRED before removing worktrees)
[[ "$(pwd)" == "/workspace/tasks/{TASK_NAME}"* ]] && {
  echo "FATAL: Cannot remove worktree while inside it (pwd=$(pwd))"
  exit 1
} || cd /workspace/main

# Step 5: Remove all agent worktrees and branches (WITH ERROR HANDLING)
for agent in $AGENTS; do
  echo "Removing agent worktree: $agent"
  if [ -d "/workspace/tasks/{TASK_NAME}/agents/$agent/code" ]; then
    git worktree remove /workspace/tasks/{TASK_NAME}/agents/$agent/code --force || {
      echo "FAILED to remove agent worktree: $agent"
      echo "Check for processes holding locks in this directory"
      exit 1
    }
    echo "  Agent worktree removed: $agent"
  else
    echo "  Agent worktree already removed: $agent"
  fi
  git branch -D {TASK_NAME}-$agent 2>/dev/null && echo "  Agent branch deleted: $agent" || echo "  Agent branch already deleted: $agent"
done

# Step 6: Remove task worktree and branch
git worktree remove /workspace/tasks/{TASK_NAME}/code --force
git branch -D {TASK_NAME}  # Use -D: squash merge makes branch unreachable from main

# Step 7: MANDATORY - Garbage collect orphaned commits from squash merge
echo "Running garbage collection to remove orphaned commits..."
git gc --prune=now
echo "Orphaned commits removed"

# Step 8: Remove lock file (ONLY after worktrees successfully removed)
rm -f /workspace/tasks/{TASK_NAME}/task.json

# Step 9: Remove entire task directory
rm -rf /workspace/tasks/{TASK_NAME}

# Step 10: Temporary file cleanup
TEMP_DIR=$(cat .temp_dir 2>/dev/null) && [ -n "$TEMP_DIR" ] && rm -rf "$TEMP_DIR"

echo "CLEANUP complete: All worktrees, branches, and orphaned commits removed"
```

**Example for task "refactor-line-wrapping-architecture" with agents "architect quality style"**:
```bash
# Verify work in main branch (from task worktree)
cd /workspace/tasks/refactor-line-wrapping-architecture/code
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5 | grep "refactor-line-wrapping-architecture" || {
    echo "FATAL: Work not found in main branch"
    exit 1
}

# Clean up task resources with session verification
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/tasks/refactor-line-wrapping-architecture/task.json")
if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "FATAL: Cannot delete lock owned by $LOCK_OWNER"
  exit 1
fi

rm -f /workspace/tasks/refactor-line-wrapping-architecture/task.json

# Change to main worktree (fails if already inside task worktree being removed)
[[ "$(pwd)" == "/workspace/tasks/refactor-line-wrapping-architecture"* ]] && { echo "FATAL: Cannot remove worktree while inside it (pwd=$(pwd))"; exit 1; } || cd /workspace/main

# Remove all agent worktrees and branches
AGENTS="architect engineer formatter"
for agent in $AGENTS; do
  echo "Removing agent worktree: $agent"
  git worktree remove /workspace/tasks/refactor-line-wrapping-architecture/agents/$agent/code --force
  git branch -D refactor-line-wrapping-architecture-$agent
  echo "  Agent cleanup complete: $agent"
done

# Remove task worktree and branch
git worktree remove /workspace/tasks/refactor-line-wrapping-architecture/code --force
git branch -D refactor-line-wrapping-architecture  # Use -D: squash makes branch unreachable

# MANDATORY: Garbage collect orphaned commits from squash merge
echo "Running garbage collection to remove orphaned commits..."
git gc --prune=now
echo "Orphaned commits removed"

# Remove entire task directory
rm -rf /workspace/tasks/refactor-line-wrapping-architecture

echo "CLEANUP complete: All worktrees, branches, and orphaned commits removed"
```

---

## TRANSITION VALIDATION FUNCTIONS {#transition-validation-functions}

### Universal Validation Requirements {#universal-validation-requirements}
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

### State Enforcement Functions {#state-enforcement-functions}

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
        # Standard forward transitions
        "INIT:CLASSIFIED"|"CLASSIFIED:REQUIREMENTS"|"REQUIREMENTS:SYNTHESIS"|"SYNTHESIS:IMPLEMENTATION"|"IMPLEMENTATION:VALIDATION"|"VALIDATION:REVIEW"|"REVIEW:COMPLETE"|"COMPLETE:CLEANUP")
            return 0 ;;
        # Scope negotiation transitions
        "REVIEW:SCOPE_NEGOTIATION"|"SCOPE_NEGOTIATION:SYNTHESIS"|"SCOPE_NEGOTIATION:COMPLETE")
            return 0 ;;
        # Conditional skip transitions for low/medium risk
        "SYNTHESIS:COMPLETE"|"CLASSIFIED:COMPLETE"|"REQUIREMENTS:COMPLETE")
            return 0 ;;
        # Resolution cycle transitions
        "IMPLEMENTATION:SYNTHESIS"|"VALIDATION:SYNTHESIS")
            return 0 ;;
        *)
            return 1 ;;
    esac
}
```

### State Transition Reversibility Table {#state-transition-reversibility-table}

| From State | To State | Reversibility | Type | Rationale |
|-----------|----------|---------------|------|-----------|
| **INIT** | CLASSIFIED | IRREVERSIBLE | Standard forward | Task initialization complete, agents selected |
| **CLASSIFIED** | REQUIREMENTS | IRREVERSIBLE | Standard forward | Risk classified, moving to requirements gathering |
| **CLASSIFIED** | COMPLETE | IRREVERSIBLE | Conditional skip | LOW-RISK only: skip to completion |
| **REQUIREMENTS** | SYNTHESIS | IRREVERSIBLE | Standard forward | Requirements gathered, moving to planning |
| **REQUIREMENTS** | COMPLETE | IRREVERSIBLE | Conditional skip | MEDIUM-RISK docs: skip to completion |
| **SYNTHESIS** | IMPLEMENTATION | IRREVERSIBLE | Standard forward | Plan approved, moving to implementation |
| **SYNTHESIS** | COMPLETE | IRREVERSIBLE | Conditional skip | Config-only changes: skip implementation |
| **IMPLEMENTATION** | SYNTHESIS | REVERSIBLE | Resolution cycle | Agent rejection -> revise plan |
| **IMPLEMENTATION** | VALIDATION | IRREVERSIBLE | Standard forward | Implementation complete, moving to validation |
| **VALIDATION** | SYNTHESIS | REVERSIBLE | Resolution cycle | Build failure -> revise plan |
| **VALIDATION** | REVIEW | IRREVERSIBLE | Standard forward | Validation passed, moving to review |
| **REVIEW** | SCOPE_NEGOTIATION | REVERSIBLE | Scope adjustment | Scope too large -> negotiate scope |
| **REVIEW** | COMPLETE | IRREVERSIBLE | Standard forward | All agents approved, moving to completion |
| **SCOPE_NEGOTIATION** | SYNTHESIS | REVERSIBLE | Resolution cycle | Scope reduced -> re-plan |
| **SCOPE_NEGOTIATION** | COMPLETE | IRREVERSIBLE | Scope resolution | Work deferred -> complete remaining scope |
| **COMPLETE** | CLEANUP | IRREVERSIBLE | Standard forward | Work merged, moving to cleanup |

**Transition Type Definitions:**
1. **Standard Forward** (forward-only, irreversible): Normal progression, cannot return once transitioned
2. **Conditional Skip** (forward-only, irreversible): Bypass states based on risk/change type, cannot return to skipped states
3. **Resolution Cycle** (reversible, backward): Return to earlier state to address issues, can forward again after resolving
4. **Scope Adjustment** (reversible, lateral): Temporarily transition to SCOPE_NEGOTIATION, return to SYNTHESIS or proceed to COMPLETE

**IRREVERSIBLE Transitions** (once crossed, cannot go back):
- INIT -> CLASSIFIED: Task initialization creates worktrees and locks
- CLASSIFIED -> REQUIREMENTS: Agents selected and invoked
- REQUIREMENTS -> SYNTHESIS: Requirements finalized in task.md
- SYNTHESIS -> IMPLEMENTATION (after user approval): Plan approved, implementation begins
- IMPLEMENTATION -> VALIDATION: All implementation rounds complete
- VALIDATION -> REVIEW: Build verification passed
- REVIEW -> COMPLETE (after agent approval): All agents approved changes
- COMPLETE -> CLEANUP: Work merged to main branch

**REVERSIBLE Transitions** (can return to earlier state):
- IMPLEMENTATION <-> SYNTHESIS: Agent rejection triggers plan revision
- VALIDATION -> SYNTHESIS: Build failure triggers plan revision
- REVIEW -> SCOPE_NEGOTIATION: Scope too large, need to negotiate
- SCOPE_NEGOTIATION -> SYNTHESIS: Scope reduced, need to re-plan

**Decision Logic:**
```bash
can_transition_back() {
    local current_state=$1
    local target_state=$2
    case "$current_state:$target_state" in
        "IMPLEMENTATION:SYNTHESIS")
            # Only if agent rejection occurred
            if agent_rejection_detected; then
                return 0  # Can go back
            fi
            ;;
        "VALIDATION:SYNTHESIS")
            # Only if build failure occurred
            if build_failure_detected; then
                return 0  # Can go back
            fi
            ;;
        "SCOPE_NEGOTIATION:SYNTHESIS")
            # Only if scope reduction agreed
            if scope_reduction_agreed; then
                return 0  # Can go back
            fi
            ;;
        *)
            # All other backward transitions prohibited
            return 1  # Cannot go back
            ;;
    esac
}
```

### Reversibility Worked Examples {#reversibility-worked-examples}

**Example 1: IMPLEMENTATION -> SYNTHESIS (Reversible)**
```
State: IMPLEMENTATION
Action: architect agent rejects implementation
Decision: FINAL DECISION: ❌ REJECTED - Architecture violates SOLID principles
Result: Transition back to SYNTHESIS
Reason: Plan needs revision based on agent feedback
Outcome: Revise plan in task.md, re-present for approval, retry IMPLEMENTATION
```

**Example 2: VALIDATION -> REVIEW (Irreversible)**
```
State: VALIDATION
Action: Build verification passes (./mvnw verify succeeds)
Decision: All quality gates passed
Result: Transition forward to REVIEW (CANNOT go back to VALIDATION)
Reason: Validation successful, moving to final review phase
Outcome: Launch review agents for final approval
```

**Example 3: REVIEW -> SCOPE_NEGOTIATION (Reversible)**
```
State: REVIEW
Action: All agents reject due to scope creep
Decision: Unanimous agent recommendation to reduce scope
Result: Transition to SCOPE_NEGOTIATION
Reason: Scope too large for single task
Outcome: Defer some work to follow-up tasks, transition back to SYNTHESIS with reduced scope
```

**Example 4: COMPLETE -> CLEANUP (Irreversible)**
```
State: COMPLETE
Action: Work merged to main branch via git push
Decision: Merge successful, task archived in changelog.md
Result: Transition forward to CLEANUP (CANNOT go back to COMPLETE)
Reason: Work permanently integrated, only cleanup remains
Outcome: Remove worktrees, remove locks, finalize task
```

**Audit Trail Implications:**
- IRREVERSIBLE transitions must leave evidence (lock file transition_log, git history, status.json, task.md)
- REVERSIBLE transitions must record reason (why back, what changed, evidence of resolution attempt)

---

## AGENT INTERACTION PROTOCOLS {#agent-interaction-protocols}

### Requirements Agent Template {#parallel-agent-invocation-pattern}
```
Task: {task_description}
Mode: REQUIREMENTS_ANALYSIS
Current State: REQUIREMENTS -> SYNTHESIS transition pending
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
```

### Review Agent Template {#review-agent-invocation-pattern}
```
Task: {task_description}
Mode: FINAL_SYSTEM_REVIEW
Current State: REVIEW -> COMPLETE transition pending
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
```

---

## ERROR HANDLING & RECOVERY {#error-handling-recovery}

**See:** [task-protocol-recovery.md](task-protocol-recovery.md) for detailed procedures.
**Quick access:** Use `recover-from-error` skill.

### Transition Failure Recovery {#transition-failure-recovery}
```python
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
```

### Multi-Instance Coordination {#multi-instance-coordination}

Use `verify-task-ownership` skill BEFORE working on or cleaning up foreign tasks.

**Lock Conflict Handling:**
```bash
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

**PROHIBITED: Task Ownership Takeover**

**The Mistake Pattern:**
```bash
# Agent reads task.json, sees different session_id
# Thinking: "the session_id is different, but since this is my task to work on, I should take ownership"
# WRONG: Modifies session_id to their own
jq '.session_id = "my-session-id"' task.json > tmp && mv tmp task.json
```

**Why This Is Prohibited:**
- The original session may still be actively working on the task
- Session_id is the coordination mechanism - modifying it breaks multi-instance coordination
- The task may already be completed and merged (wasted effort)

**Correct Approach:** Use `verify-task-ownership` skill which:
- Checks if task is truly abandoned vs actively in progress
- Provides safe cleanup commands if abandoned
- Guides you to choose a different task if owned by active session

**PROHIBITED: Cleanup of Foreign Tasks Without Verification**

**The Mistake Pattern:**
```bash
# SessionStart hook shows tasks owned by different sessions
# Thinking: "These are ABANDONED tasks... safe to clean up"
# WRONG: Runs cleanup commands without verification
git worktree remove --force /workspace/tasks/task-name/code
rm -rf /workspace/tasks/task-name
```

**Why This Is Prohibited:**
- You cannot determine if a session is "no longer active" without verification
- The hook `block-foreign-task-cleanup.sh` will BLOCK these commands
- Tasks in SYNTHESIS state with agents "not_started" are NOT necessarily abandoned

**Correct Approach:**
1. When SessionStart detects foreign tasks, use the skill first:
   ```
   Skill: verify-task-ownership
   Args: {task-name}
   ```
2. Only cleanup after the skill confirms the task is truly abandoned
3. The skill provides safe cleanup commands in its output

**PROHIBITED Rationalizations:**
- "These are ABANDONED tasks... safe to clean up"
- "The sessions are no longer active"
- "Tasks in SYNTHESIS with agents not_started are abandoned"

---

## COMPLIANCE VERIFICATION {#compliance-verification}

### Pre-Task Validation (MANDATORY) {#pre-task-validation-checklist}
```bash
# Session ID validation
[ -n "$SESSION_ID" ] && [ "$SESSION_ID" != "REPLACE_WITH_ACTUAL_SESSION_ID" ] && echo "SESSION_ID_VALID: $SESSION_ID" || (echo "SESSION_ID_INVALID" && exit 1)

# Working directory verification
[ "$(pwd)" = "/workspace/main" ] || (echo "ERROR: Wrong directory" && exit 1)

# Todo.md accessibility
[ -f "todo.md" ] || (echo "ERROR: todo.md not accessible" && exit 1)
```

### Post-Transition Validation {#continuous-compliance-monitoring}
```bash
post_transition_validation() {
    local new_state=$1
    echo "=== POST-TRANSITION VALIDATION: $new_state ==="
    current_state=$(jq -r '.current_state' state.json 2>/dev/null || echo "ERROR")
    [ "$current_state" = "$new_state" ] || { echo "ERROR: State file not updated correctly"; exit 1; }
    validate_state_evidence "$new_state" || { echo "ERROR: Required evidence missing for state $new_state"; exit 1; }
    echo "Post-transition validation complete"
}
```

### Final Compliance Audit (MANDATORY before CLEANUP) {#final-compliance-audit}
```bash
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
            echo "Valid state sequence detected: matches $sequence pattern"
            break
        fi
    done

    # Also check alternate scope negotiation pattern
    if [[ "$valid_sequence" == "false" ]] && [[ "$state_sequence" =~ $alternate_scope_sequence ]]; then
        valid_sequence=true
        echo "Valid state sequence detected: matches scope negotiation with resolution cycle"
    fi

    if [[ "$valid_sequence" == "false" ]]; then
        echo "ERROR: Invalid state sequence: $state_sequence"
        exit 1
    fi

    # Verify unanimous approval achieved
    approval_count=$(jq -r '.evidence.REVIEW | to_entries[] | select(.value == "APPROVED") | .key' state.json | wc -l)
    required_count=$(jq -r '.required_agents | length' state.json)
    [ "$approval_count" -eq "$required_count" ] || { echo "ERROR: Missing agent approvals: $approval_count/$required_count"; exit 1; }

    # Verify work preserved
    git log --oneline -5 | grep -i "$(jq -r '.task_name' state.json)" || { echo "ERROR: Task work not preserved in git history"; exit 1; }

    echo "Final compliance audit passed"
}
```

---

## VIOLATION PREVENTION PATTERNS {#violation-prevention-patterns}

### Mandatory task.md Creation {#mandatory-taskmd-creation}

**CRITICAL RESPONSIBILITY CLARIFICATION:**
- **WHO**: Main coordination agent (the agent that executed INIT and CLASSIFIED states)
- **WHEN**: During CLASSIFIED state, AFTER agent selection, BEFORE invoking any stakeholder agents
- **WHERE**: `/workspace/tasks/{TASK_NAME}/task.md` (task root, NOT inside code directory)
- **WHY**: Provides scope boundary enforcement for stakeholder agents and prevents unauthorized file access

**Verification BEFORE REQUIREMENTS state:**
```bash
[ -f "/workspace/tasks/${TASK_NAME}/task.md" ] || {
    echo "CRITICAL ERROR: task.md not created by CLASSIFIED state"
    echo "Required location: /workspace/tasks/${TASK_NAME}/task.md"
    echo "ABORT: Cannot proceed to REQUIREMENTS state without task.md"
    exit 1
}
```

**Required Structure (must be created BEFORE stakeholder agent invocation):**
```markdown
# Task Context: {task-name}

## Task Objective {#task-objective}
{task-description}

## Scope Definition {#scope-definition}
**FILES IN SCOPE:**
- [List exact files/directories that stakeholder agents are authorized to analyze]

**FILES OUT OF SCOPE:**
- [List directories/files explicitly excluded from analysis]

## Stakeholder Agent Reports {#stakeholder-agent-reports}
**Requirements Phase:**
- architect-requirements.md (when completed)
- [other-agent]-requirements.md (when completed)

**Implementation Reviews:**
- architect-review1.md (when completed)
- [other-agent]-review1.md (when completed)

## Implementation Status {#implementation-status}
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

### Atomic Lock Acquisition Pattern {#atomic-lock-acquisition-pattern}
```bash
export SESSION_ID="f33c1f04-94a5-4e87-9a87-4fcbc57bc8ec" && mkdir -p ../../../locks && (set -C; echo '{"session_id": "'${SESSION_ID}'", "start_time": "'$(date '+%Y-%m-%d %H:%M:%S %Z')'"}' > ../../../locks/{task-name}.json) 2>/dev/null && echo "LOCK_SUCCESS" || echo "LOCK_FAILED"

# Violation check
if [[ "$lock_result" != *"LOCK_SUCCESS"* ]]; then
    echo "Lock acquisition failed - selecting alternative task"
    select_alternative_task
fi
```

### Temporary File Management Setup {#temporary-file-management-setup}
**BEFORE IMPLEMENTATION BEGINS (Mandatory for all tasks):**
```bash
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

### Implementation Safety Guards {#implementation-safety-guards}
```bash
# Working directory validation
pwd | grep -q "/workspace/tasks/.*/code$" || (echo "ERROR: Invalid working directory" && exit 1)

# Clean repository state
git status --porcelain | grep -E "(dist/|node_modules/|target/|\.jar$)" && (echo "ERROR: Prohibited files detected" && exit 1)
```

### Build Validation Gates {#build-validation-gates}
```bash
./mvnw verify -q || (echo "ERROR: Build/test/linter failure - task cannot be completed" && exit 1)
# Note: 'mvnw verify' executes: compile -> test -> checkstyle -> PMD -> all quality gates
```

### Decision Parsing Enforcement {#decision-parsing-enforcement}
```bash
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

### Protocol Violation Reporting Pattern {#protocol-violation-reporting-pattern}

**MAINTAIN THROUGHOUT TASK EXECUTION:**
1. Track all format violations in TodoWrite tool as separate item
2. Record: Agent type, state, violation type, retry outcome
3. Include in final task summary for human review

**VIOLATION REPORT FORMAT:**
```
"Protocol Violations Detected:
- [Agent]: [State] - Missing decision format (resolved via retry)
- [Agent]: [State] - Malformed decision (assumed REJECTED)
- Total violations: X, Auto-resolved: Y, Manual review needed: Z"
```

**PURPOSE:**
- Identify agent prompt issues needing refinement
- Track protocol compliance trends over time
- Enable proactive protocol improvements
- Maintain audit trail for debugging

**IMPLEMENTATION:**
```python
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

---

## CONTEXT PRESERVATION RULES {#context-preservation-rules}

### Single Session Continuity {#single-session-continuity}
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

### Tool Call Batching {#tool-call-batching}
```python
parallel_tool_calls = [
    {"tool": "Task", "agent": "architect", "mode": "requirements"},
    {"tool": "Task", "agent": "style", "mode": "requirements"},
    {"tool": "Task", "agent": "quality", "mode": "requirements"}
]
# Execute all in single message to reduce context fragmentation
execute_parallel_tools(parallel_tool_calls)
```

### State Persistence Patterns {#state-persistence-patterns}
**Critical state tracking:**
- Current state in state.json file
- Session ID in environment variables
- Working directory verification
- Lock status confirmation

**Verify state before each major operation:**
```bash
current_state=$(jq -r '.current_state' state.json)
expected_state="$1"
[ "$current_state" = "$expected_state" ] || (echo "ERROR: State inconsistency" && exit 1)
```

---

## TOOL-SPECIFIC OPTIMIZATION PATTERNS {#tool-specific-optimization-patterns}

### Bash Tool Usage {#bash-tool-usage}
```bash
# Combine related operations with safety checks
command1 && echo "SUCCESS" || (echo "FAILED" && exit 1)

# Verify outputs before proceeding
result=$(command_with_output)
[ -n "$result" ] || (echo "ERROR: Command produced no output" && exit 1)

# Avoid interactive commands
# BAD: git rebase -i
# GOOD: git rebase main
```

### Read Tool Usage {#read-tool-usage}
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

### Task Tool Usage {#task-tool-usage}
```python
# Parallel agent calls in single message
agent_calls = [
    {
        'subagent_type': 'architect',
        'description': 'Architecture requirements',
        'prompt': structured_prompt_template.format(domain='architecture')
    },
    {
        'subagent_type': 'style',
        'description': 'Style requirements',
        'prompt': structured_prompt_template.format(domain='style')
    }
]

# Execute all agents simultaneously
execute_parallel_agents(agent_calls)
```

### TodoWrite Tool Usage {#todowrite-tool-usage}
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

---

## WORKFLOW EXECUTION ENGINE {#workflow-execution-engine}

### Main Task Execution {#main-task-execution-function}
```bash
execute_task_protocol() {
    local task_name=$1
    initialize_task_state "$task_name"

    while [ "$(get_current_state)" != "CLEANUP" ]; do
        current_state=$(get_current_state)
        risk_level=$(jq -r '.risk_level' state.json 2>/dev/null || echo "HIGH")
        change_type=$(determine_change_type)

        echo "=== STATE: $current_state (Risk: $risk_level, Type: $change_type) ==="

        case "$current_state" in
            "INIT")
                execute_init_to_classified_transition ;;
            "CLASSIFIED")
                execute_classified_to_requirements_transition ;;
            "REQUIREMENTS")
                execute_requirements_to_synthesis_transition ;;
            "SYNTHESIS")
                present_implementation_plan_and_wait_for_approval  # USER CHECKPOINT 1
                if should_skip_implementation "$risk_level" "$change_type"; then
                    execute_synthesis_to_complete_transition
                else
                    execute_synthesis_to_implementation_transition
                fi ;;
            "IMPLEMENTATION")
                execute_implementation_to_validation_transition ;;
            "VALIDATION")
                execute_validation_to_review_transition ;;
            "REVIEW")
                execute_review_to_complete_or_scope_negotiation_transition
                if all_agents_approved; then
                    present_changes_and_wait_for_user_approval  # USER CHECKPOINT 2
                fi ;;
            "SCOPE_NEGOTIATION")
                execute_scope_negotiation_to_synthesis_or_complete_transition ;;
            "COMPLETE")
                execute_complete_to_cleanup_transition ;;
            *)
                echo "ERROR: Unknown state: $current_state"; exit 1 ;;
        esac
        post_transition_validation "$(get_current_state)"
    done
    final_compliance_audit
    echo "=== TASK PROTOCOL COMPLETED SUCCESSFULLY ==="
}

should_skip_implementation() {
    local risk_level=$1
    local change_type=$2
    case "$risk_level" in
        "HIGH") return 1 ;;  # Never skip for high risk
        "MEDIUM")
            if [[ "$change_type" == "documentation_only" || "$change_type" == "config_only" ]]; then
                return 0  # Skip for medium risk documentation/config changes
            else
                return 1  # Don't skip for other medium risk changes
            fi ;;
        "LOW") return 0 ;;  # Always skip for low risk
        *) return 1 ;;  # Default to not skip if uncertain
    esac
}
```

### User Approval Checkpoints {#user-approval-checkpoints}

**Checkpoint 1 - Plan Review (After SYNTHESIS):**
```bash
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
```

**Checkpoint 2 - Change Review (After REVIEW):**
```bash
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
    # Ask: "All stakeholder agents have approved. Changes committed to task branch (SHA: $COMMIT_SHA). Please review the changes. Would you like me to proceed with finalizing (COMPLETE -> CLEANUP)?"

    # Step 4: Wait for user response
    # If user requests changes:
    #   - Make requested modifications
    #   - Amend commit: git add <files> && git commit --amend --no-edit
    #   - Return to Step 2 with new commit SHA
    # If user approves:
    #   - Proceed to COMPLETE state
}
```

---

## MIGRATION FROM PHASE-BASED PROTOCOL {#migration-from-phase-based-protocol}

**Phase-to-State Mapping:**
- Phase 1 = REQUIREMENTS
- Phase 2 = SYNTHESIS
- Phase 3 = IMPLEMENTATION
- Phase 4 = VALIDATION
- Phase 5 = Resolution cycles
- Phase 6 = REVIEW
- Phase 7 = CLEANUP

**Enforcement**: Mandatory transition conditions with no manual overrides or exceptions.

**END OF STATE MACHINE PROTOCOL**
