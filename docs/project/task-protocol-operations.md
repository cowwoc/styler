# Task State Machine Protocol - Operations & Procedures

> **Version:** 2.0 | **Last Updated:** 2025-10-16
> **Related Documents:** [CLAUDE.md](../../CLAUDE.md) • [task-protocol-core.md](task-protocol-core.md)

**IMPORTANT**: This document is Part 2 of the Task Protocol. Read
[task-protocol-core.md](task-protocol-core.md) first for:
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

### COMPLETE → CLEANUP {#complete-cleanup}
**Mandatory Conditions:**
- [ ] All work already committed to task branch (from USER REVIEW checkpoint)
- [ ] todo.md removed from todo list and added to changelog.md
- [ ] **Dependent tasks updated in todo.md** (BLOCKED → READY if all dependencies satisfied)
- [ ] todo.md and changelog.md changes amended to existing commit
- [ ] **Build verification passes in task worktree BEFORE merge** (`./mvnw clean verify` in worktree - clean build prevents stale module-info.class issues)
- [ ] Task branch merged to main branch with **LINEAR COMMIT HISTORY** (fast-forward only, NO merge commits)
- [ ] Full build verification passes on main branch after merge (`./mvnw verify`)

**Post-Merge Build Failure Recovery Protocol:**

If `./mvnw verify` fails on main branch after merge (despite passing in worktree):

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

The main branch must NEVER contain broken commits. If build failures occur post-merge, the merge itself was premature and must be undone. All fixes must be applied in the task worktree and re-verified before attempting merge again.

**Evidence Required:**
- **Pre-merge build success in worktree** (`./mvnw clean verify` passes before merge attempt)
- Git log showing clean linear history (no merge commits)
- **todo.md modification included in final commit** (verify with `git show --stat | grep "todo.md"`)
- Main branch build success after integration (`./mvnw verify` passes)
- All deliverables preserved in main branch

**CRITICAL: Pre-Merge Build Verification Gate**
```bash
# Launch ALL agents with Task tool in ONE message
Task tool (architect), model: sonnet, prompt: "Review architecture requirements for {task}"
Task tool (quality), model: sonnet, prompt: "Review code quality requirements for {task}"
Task tool (style), model: sonnet, prompt: "Review style requirements for {task}"
Task tool (security), model: sonnet, prompt: "Review security requirements for {task}"

# Wait for ALL responses before proceeding
```

**DON'T** (sequential):
```bash
# Message 1
Task tool (architect), model: sonnet, prompt: "..."
# Wait for response

# Message 2
Task tool (quality), model: sonnet, prompt: "..."
# Wait for response

# = 3-4× overhead, 20-30 min wasted
```

---

### Predictive Prefetching (INIT) - **MANDATORY** {#predictive-prefetching-init---mandatory}

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

### Fail-Fast Validation (IMPLEMENTATION) {#fail-fast-validation-implementation}

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

### Pre-Validation Checklist (IMPLEMENTATION Exit) {#pre-validation-checklist-implementation-exit}

**MANDATORY GATES BEFORE EXITING IMPLEMENTATION**

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

# Gate 4: Build verification
./mvnw clean verify
# Exit code: 0 (full build passes with clean compilation)
# Clean build prevents stale module-info.class from compile-only builds
```

**CHECKLIST DETAILS:**

**✅ Code Compilation:**
- `./mvnw compile` passes with zero errors
- All modules compile successfully
- No missing dependencies

**✅ Business-Logic Coverage:**
- Tests cover all significant business logic paths
- Focus on meaningful behavior, not test counts
- Required coverage areas:
  - Input validation (null, empty, invalid values)
  - Business rules and constraints
  - Edge cases and boundary conditions
  - Error handling paths
  - Integration points between components

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

**✅ JPMS Module Compilation Verification:**
- **Test module descriptor exists**: `src/test/java/module-info.java` present
- **Test module name correct**: Uses `.test` suffix (e.g., `io.github.styler.parser.test`)
- **Test module requires main module**: `requires io.github.styler.parser;` present
- **Test framework dependency**: `requires org.testng;` present (not `requires transitive`)
- **Package opens for reflection**: All test packages have `opens X to org.testng;` (NOT `exports`)
- **No circular dependencies**: Test module requires main, main does NOT require test
- **Clean build verification**: `./mvnw clean verify` passes (detects stale module-info.class)
- **Module resolution**: No "module not found" errors in compiler output
- **Test execution**: Tests run successfully with module system enabled

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

**✅ Edge Case Handling:**
- Null validation tests present
- Empty/single-element tests present
- Boundary value tests present

**ENFORCEMENT:**

The `.claude/hooks/verify-implementation-exit.sh` hook automatically blocks VALIDATION entry if any gate
fails. Recovery procedure:

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

### Implementation Rounds (Review/Implementation Iteration) {#implementation-rounds-reviewimplementation-iteration}

**CRITICAL PATTERN**: Implementation uses agents in BOTH review mode (Sonnet) and implementation mode (Haiku) in iterative cycles.

**Round 1 - Initial Implementation:**
```
I am now entering IMPLEMENTATION state. Launching agents in implementation mode for parallel implementation.

Task tool (architect), model: haiku, prompt: "Implement core FormattingRule interfaces per requirements"
Task tool (quality), model: haiku, prompt: "Apply design patterns and refactoring per requirements"
Task tool (style), model: haiku, prompt: "Ensure all code follows project style guidelines"
Task tool (test), model: haiku, prompt: "Implement comprehensive test suite per test strategy"
```

**Round 1 - Review Merged Changes:**
```
Agents have merged to task branch. Launching agents in review mode for parallel validation.

Task tool (architect), model: sonnet, prompt: "Review merged architecture on task branch for completeness"
Task tool (engineer), model: sonnet, prompt: "Review merged code quality on task branch"
Task tool (formatter), model: sonnet, prompt: "Review merged style compliance on task branch"
Task tool (tester), model: sonnet, prompt: "Review merged test coverage and quality on task branch"
```

**Round 2 - Apply Review Feedback (if rejections):**
```
Reviews identified issues. Launching agents in implementation mode to fix.

Task tool (formatter), model: haiku, prompt: "Fix 12 style violations identified in review"
Task tool (architect), model: haiku, prompt: "Clarify interface contracts per review feedback"
```

**Round 2 - Re-review Fixes:**
```
Agents have merged fixes. Re-launching agents in review mode to verify.

Task tool (formatter), model: sonnet, prompt: "Re-review style compliance on task branch"
Task tool (architect), model: sonnet, prompt: "Re-review architecture fixes on task branch"
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

### Multi-Agent Implementation Commit History Examples {#multi-agent-implementation-commit-history-examples}

Demonstrate CORRECT vs INCORRECT commit history patterns that show multi-agent implementation.

**INCORRECT Pattern - Single Commit (Protocol Violation)**:
```bash
$ git log --oneline task-branch
abc123d Implement FormattingRule system (Claude)
```

**Analysis**: Single commit suggests main agent implemented directly, violating protocol. No evidence of multiple agents.

**CORRECT Pattern - Multiple Agent Commits**:
```bash
$ git log --oneline task-branch
jkl012m [test] Add comprehensive test suite for FormattingRule
ghi789j [style] Implement JavaDoc requirements for public APIs
def456g [quality] Apply factory pattern to rule instantiation
abc123d [architect] Add FormattingRule interface hierarchy
```

**Analysis**: Multiple commits with agent attribution prove multi-agent implementation protocol followed.

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

**Verification Commands**:
```bash
# Count agent commits to verify multi-agent implementation
git log --oneline task-branch | grep -E '\[(architect|engineer|formatter|tester|builder)\]' | wc -l
# Expected: 3+ (at least 3 different agents contributed)

# List all contributing agents
git log --format='%s' task-branch | grep -oP '\[\K[^]]+' | sort -u
# Expected output:
# architect
# quality
# style
# test

# Verify parallel implementation (commits should have similar timestamps)
git log --format='%h %ai %s' task-branch
# Expected: Commits within minutes of each other, not hours apart
```

**Main Agent Final Commit Example**:
```bash
$ git log --format='%h %s%n%b' main --max-count=1
abc123z Implement FormattingRule system

Contributing agents:
- architect: Core interface design
- quality: Design pattern application
- style: Code style compliance
- test: Test suite implementation

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Red Flags - Potential Violations**:
- Only 1 commit in task branch history
- All commits by "main agent" or generic "Claude" attribution
- Commits hours apart (suggests sequential, not parallel)
- No agent names in commit messages
- Commit messages don't identify which domain (architecture/engineer/formatter/tester)

**Green Flags - Compliant Implementation**:
- 3+ commits with agent-specific attribution
- Agent names in commit messages: `[agent-name]`
- Commits within minutes (parallel execution)
- Clear domain separation in commit subjects
- Main agent final commit lists all contributors

---

## TROUBLESHOOTING {#troubleshooting}

### Issue: Implementation Rounds Taking >15 Minutes {#issue-implementation-rounds-taking-15-minutes}

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

### Issue: High Token Usage (>65,000) {#issue-high-token-usage-65000}

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

### Issue: Sequential Discovery Delays (>5 Round-Trips) {#issue-sequential-discovery-delays-5-round-trips}

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

### Issue: Unanimous Approval Not Achieved {#issue-unanimous-approval-not-achieved}

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

## BEST PRACTICES {#best-practices}

### 1. Risk Assessment {#1-risk-assessment}

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

### 2. Agent Selection {#2-agent-selection}

```python
# HIGH-RISK (mandatory agents)
agents = [
    "architect",   # Architecture and design
    "quality",  # Quality standards
    "style",         # Style compliance
    "test",           # Test coverage
]

# Add conditional agents
if "security" in task_description or "authentication" in task_description:
    agents.append("security")

if "performance" in task_description or "algorithm" in task_description:
    agents.append("performance")

# MEDIUM-RISK (core only)
agents = ["architect", "quality"]

# LOW-RISK (minimal or none)
agents = []  # or ["quality"] for technical docs
```

### 3. Lock State Management {#3-lock-state-management}

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

# Step 2: Archive completed task from todo.md to changelog.md
# CRITICAL: Follow CLAUDE.md task archival policy
# CRITICAL: These changes MUST be in the same commit as task deliverables
# 2a. Remove completed task entry from todo.md (delete entire task section)
# 2b. Add completed task to changelog.md under today's date (## YYYY-MM-DD)
# 2c. Include completion details: solution, files modified, tests, quality gates

# Step 2d: Update dependent tasks in todo.md (with file locking)
# Acquire todo.md lock to prevent concurrent modifications
TODO_LOCK="/workspace/.todo.md.lock"
exec 200>"$TODO_LOCK"
flock -x 200 || { echo "ERROR: Could not acquire todo.md lock"; exit 1; }

# For each task marked as BLOCKED:
#   - Check if the completed task was listed in its Dependencies
#   - If ALL dependencies are now complete (check changelog.md), change status from BLOCKED to READY
#   - Example: If task B1 depends on A0 and A1, and both A0 and A1 are now in changelog.md,
#             change "- [ ] **BLOCKED:** `task-name`" to "- [ ] **READY:** `task-name`"

# Edit todo.md and changelog.md with all changes
# ... editing logic (use Edit tool or sed commands) ...

# Release lock
flock -u 200

# Step 3: Commit ALL changes together (implementation + todo.md + changelog.md)
# CRITICAL: Single atomic commit containing all task deliverables and project status updates
git add <changed-files> todo.md changelog.md
git commit -m "Descriptive commit message for task changes"

# Step 4: Verify todo.md and changelog.md are included in the commit
git show --stat | grep "todo.md" || { echo "ERROR: todo.md not in commit"; exit 1; }
git show --stat | grep "changelog.md" || { echo "ERROR: changelog.md not in commit"; exit 1; }

# Step 5: Fetch latest main and rebase to create linear history
git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
git rebase origin/main

# Step 6: PRE-MERGE BUILD VERIFICATION (CRITICAL GATE)
# MANDATORY: Verify build passes in worktree BEFORE merge attempt
./mvnw verify || {
    echo "❌ VIOLATION: Build fails in worktree - merge BLOCKED"
    echo "Fix all checkstyle, PMD, and test failures before merging"
    exit 1
}

# Step 7: Linear merge to main branch (fast-forward only)
# CRITICAL: Must use --ff-only to enforce linear history
# CRITICAL: No merge commits allowed
cd /workspace/main
git merge --ff-only {TASK_NAME} || {
    echo "❌ Fast-forward merge failed - non-linear history detected"
    echo "Solution: Return to task worktree, fetch latest main, rebase, and retry"
    cd /workspace/tasks/{TASK_NAME}/code
    git fetch /workspace/main refs/heads/main:refs/remotes/origin/main
    git rebase origin/main
    ./mvnw verify || { echo "❌ Build failed after rebase"; exit 1; }
    # Retry merge
    cd /workspace/main
    git merge --ff-only {TASK_NAME} || exit 1
}

# Step 8: Post-merge verification (from main worktree)
# Verify merge succeeded and todo.md/changelog.md are present
git log --oneline -5  # Must show task commit at HEAD
git show HEAD --stat | grep "todo.md" || { echo "ERROR: todo.md missing in merged commit"; exit 1; }
git show HEAD --stat | grep "changelog.md" || { echo "ERROR: changelog.md missing in merged commit"; exit 1; }

# Step 9: Verify build on main (MANDATORY after merge)
./mvnw verify || {
    echo "❌ CRITICAL: Build fails on main after merge"
    echo "This should not happen if Step 6 passed - investigate immediately"
    exit 1
}
```

**Concurrency Safety Guarantees:**
- **Linear history enforced**: --ff-only flag prevents merge commits
- **Automatic conflict detection**: Non-fast-forward merge fails cleanly
- **Clear retry path**: Fetch + rebase + retry on conflict
- **Build verification**: Tests run before and after merge
- **Atomic commit**: All changes (code + todo.md + changelog.md) in single commit

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

### DEPENDENT TASK UPDATE PROCEDURE {#dependent-task-update-procedure}

**Purpose**: Automatically unblock tasks when their dependencies are satisfied, preventing tasks from
remaining unnecessarily blocked.

**When to Execute**: During Step 3d of the COMPLETE → CLEANUP transition (after archiving the completed task
to changelog.md).

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

### AUTOMATIC CONFLICT RESOLUTION VIA ATOMIC OPERATIONS {#automatic-conflict-resolution-via-atomic-operations}

**DESIGN PRINCIPLE**: Race conditions are prevented automatically through atomic operations - no manual
recovery needed.

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

### CLEANUP (Final State) {#cleanup-final-state}
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
AGENTS="{AGENT_LIST}"  # e.g., "architect engineer formatter security"
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
git branch -D {TASK_NAME}  # Use -D: squash merge makes branch unreachable from main

# Step 7: MANDATORY - Garbage collect orphaned commits from squash merge
# Why: git merge --squash creates new commit on main, leaving original task commits unreferenced
# Without gc, these orphaned commits persist indefinitely
echo "Running garbage collection to remove orphaned commits..."
git gc --prune=now
echo "✅ Orphaned commits removed"

# Step 8: Remove lock file (ONLY after worktrees successfully removed)
rm -f /workspace/tasks/{TASK_NAME}/task.json

# Step 9: Remove entire task directory
rm -rf /workspace/tasks/{TASK_NAME}

# Step 10: Temporary file cleanup
TEMP_DIR=$(cat .temp_dir 2>/dev/null) && [ -n "$TEMP_DIR" ] && rm -rf "$TEMP_DIR"

echo "✅ CLEANUP complete: All worktrees, branches, and orphaned commits removed"
```

**Example for task "refactor-line-wrapping-architecture" with agents "architect quality
style"**:
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
AGENTS="architect engineer formatter"

# Variable $AGENTS MUST be UNQUOTED in the for loop
for agent in $AGENTS; do
  echo "Removing agent worktree: $agent"
  git worktree remove /workspace/tasks/refactor-line-wrapping-architecture/agents/$agent/code --force
  git branch -d refactor-line-wrapping-architecture-$agent
  echo "  ✅ Agent cleanup complete: $agent"
done

# Remove task worktree and branch
git worktree remove /workspace/tasks/refactor-line-wrapping-architecture/code --force
git branch -D refactor-line-wrapping-architecture  # Use -D: squash makes branch unreachable

# MANDATORY: Garbage collect orphaned commits from squash merge
echo "Running garbage collection to remove orphaned commits..."
git gc --prune=now
echo "✅ Orphaned commits removed"

# Remove entire task directory
rm -rf /workspace/tasks/refactor-line-wrapping-architecture

echo "✅ CLEANUP complete: All worktrees, branches, and orphaned commits removed"
```

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

### State Transition Reversibility Table {#state-transition-reversibility-table}

**CRITICAL REFERENCE**: This table documents which state transitions are reversible (can go back) vs irreversible (forward-only).

| From State | To State | Reversibility | Type | Rationale |
|-----------|----------|---------------|------|-----------|
| **INIT** | CLASSIFIED | ❌ **IRREVERSIBLE** | Standard forward | Task initialization complete, agents selected |
| **CLASSIFIED** | REQUIREMENTS | ❌ **IRREVERSIBLE** | Standard forward | Risk classified, moving to requirements gathering |
| **CLASSIFIED** | COMPLETE | ❌ **IRREVERSIBLE** | Conditional skip | LOW-RISK only: skip to completion |
| **REQUIREMENTS** | SYNTHESIS | ❌ **IRREVERSIBLE** | Standard forward | Requirements gathered, moving to planning |
| **REQUIREMENTS** | COMPLETE | ❌ **IRREVERSIBLE** | Conditional skip | MEDIUM-RISK docs: skip to completion |
| **SYNTHESIS** | IMPLEMENTATION | ❌ **IRREVERSIBLE** | Standard forward | Plan approved, moving to implementation |
| **SYNTHESIS** | COMPLETE | ❌ **IRREVERSIBLE** | Conditional skip | Config-only changes: skip implementation |
| **IMPLEMENTATION** | SYNTHESIS | ✅ **REVERSIBLE** | Resolution cycle | Agent rejection → revise plan |
| **IMPLEMENTATION** | VALIDATION | ❌ **IRREVERSIBLE** | Standard forward | Implementation complete, moving to validation |
| **VALIDATION** | SYNTHESIS | ✅ **REVERSIBLE** | Resolution cycle | Build failure → revise plan |
| **VALIDATION** | REVIEW | ❌ **IRREVERSIBLE** | Standard forward | Validation passed, moving to review |
| **REVIEW** | SCOPE_NEGOTIATION | ✅ **REVERSIBLE** | Scope adjustment | Scope too large → negotiate scope |
| **REVIEW** | COMPLETE | ❌ **IRREVERSIBLE** | Standard forward | All agents approved, moving to completion |
| **SCOPE_NEGOTIATION** | SYNTHESIS | ✅ **REVERSIBLE** | Resolution cycle | Scope reduced → re-plan |
| **SCOPE_NEGOTIATION** | COMPLETE | ❌ **IRREVERSIBLE** | Scope resolution | Work deferred → complete remaining scope |
| **COMPLETE** | CLEANUP | ❌ **IRREVERSIBLE** | Standard forward | Work merged, moving to cleanup |

**Transition Type Definitions**:

1. **Standard Forward** (forward-only, irreversible):
   - Normal progression through protocol states
   - Cannot return to previous state once transitioned
   - Evidence of completion required before transition
   - Examples: INIT → CLASSIFIED, REQUIREMENTS → SYNTHESIS

2. **Conditional Skip** (forward-only, irreversible):
   - Bypasses intermediate states based on risk level or change type
   - Only available for specific risk levels (LOW, MEDIUM) or change types (config-only, docs)
   - Once skipped, cannot return to skipped states
   - Examples: CLASSIFIED → COMPLETE (LOW-RISK), SYNTHESIS → COMPLETE (config-only)

3. **Resolution Cycle** (reversible, backward transition):
   - Returns to earlier state to address issues discovered during later states
   - Allows iterative refinement of plan or implementation
   - Can transition forward again after resolving issues
   - Examples: IMPLEMENTATION → SYNTHESIS (agent rejection), VALIDATION → SYNTHESIS (build failure)

4. **Scope Adjustment** (reversible, lateral transition):
   - Temporarily transitions to SCOPE_NEGOTIATION to reduce scope
   - Returns to SYNTHESIS to re-plan with reduced scope
   - Alternative path to COMPLETE if work deferred
   - Example: REVIEW → SCOPE_NEGOTIATION → SYNTHESIS

**Reversibility Rules**:

**IRREVERSIBLE Transitions** (once crossed, cannot go back):
- **INIT → CLASSIFIED**: Task initialization creates worktrees and locks
- **CLASSIFIED → REQUIREMENTS**: Agents selected and invoked
- **REQUIREMENTS → SYNTHESIS**: Requirements finalized in task.md
- **SYNTHESIS → IMPLEMENTATION** (after user approval): Plan approved, implementation begins
- **IMPLEMENTATION → VALIDATION**: All implementation rounds complete
- **VALIDATION → REVIEW**: Build verification passed
- **REVIEW → COMPLETE** (after agent approval): All agents approved changes
- **COMPLETE → CLEANUP**: Work merged to main branch

**REVERSIBLE Transitions** (can return to earlier state):
- **IMPLEMENTATION ⇄ SYNTHESIS**: Agent rejection triggers plan revision
- **VALIDATION → SYNTHESIS**: Build failure triggers plan revision
- **REVIEW → SCOPE_NEGOTIATION**: Scope too large, need to negotiate
- **SCOPE_NEGOTIATION → SYNTHESIS**: Scope reduced, need to re-plan

**Decision Logic: Can I Go Back?**

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

**Examples: Reversible vs Irreversible**

**Example 1: IMPLEMENTATION → SYNTHESIS (Reversible)**
```
State: IMPLEMENTATION
Action: architect agent rejects implementation
Decision: FINAL DECISION: ❌ REJECTED - Architecture violates SOLID principles
Result: Transition back to SYNTHESIS
Reason: Plan needs revision based on agent feedback
Outcome: Revise plan in task.md, re-present for approval, retry IMPLEMENTATION
```

**Example 2: VALIDATION → REVIEW (Irreversible)**
```
State: VALIDATION
Action: Build verification passes (./mvnw verify succeeds)
Decision: All quality gates passed
Result: Transition forward to REVIEW (CANNOT go back to VALIDATION)
Reason: Validation successful, moving to final review phase
Outcome: Launch review agents for final approval
```

**Example 3: REVIEW → SCOPE_NEGOTIATION (Reversible)**
```
State: REVIEW
Action: All agents reject due to scope creep
Decision: Unanimous agent recommendation to reduce scope
Result: Transition to SCOPE_NEGOTIATION
Reason: Scope too large for single task
Outcome: Defer some work to follow-up tasks, transition back to SYNTHESIS with reduced scope
```

**Example 4: COMPLETE → CLEANUP (Irreversible)**
```
State: COMPLETE
Action: Work merged to main branch via git push
Decision: Merge successful, task archived in changelog.md
Result: Transition forward to CLEANUP (CANNOT go back to COMPLETE)
Reason: Work permanently integrated, only cleanup remains
Outcome: Remove worktrees, remove locks, finalize task
```

**Audit Trail Implications**:

**IRREVERSIBLE transitions must leave evidence**:
- Lock file transition_log records transition
- Git history shows commits (for COMPLETE → CLEANUP)
- Agent status.json files show completion (for state transitions)
- Task.md shows requirements/plans (for early state transitions)

**REVERSIBLE transitions must record reason**:
- Why did we go back? (agent rejection, build failure, scope issue)
- What changed? (revised plan, reduced scope, fixed implementation)
- Evidence of resolution attempt (commit showing fixes, updated task.md)
```

## AGENT INTERACTION PROTOCOLS {#agent-interaction-protocols}

### Parallel Agent Invocation Pattern {#parallel-agent-invocation-pattern}
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

### Review Agent Invocation Pattern {#review-agent-invocation-pattern}
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

## ERROR HANDLING & RECOVERY {#error-handling-recovery}

### Transition Failure Recovery {#transition-failure-recovery}
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

### Multi-Instance Coordination {#multi-instance-coordination}
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

## COMPLIANCE VERIFICATION {#compliance-verification}

### Pre-Task Validation Checklist {#pre-task-validation-checklist}
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

### Continuous Compliance Monitoring {#continuous-compliance-monitoring}
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

### Final Compliance Audit {#final-compliance-audit}
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

## VIOLATION PREVENTION PATTERNS {#violation-prevention-patterns}

### Pre-Task Validation Block {#pre-task-validation-block}
**MANDATORY before ANY task execution:**
```bash
# Session ID validation
export SESSION_ID="f33c1f04-94a5-4e87-9a87-4fcbc57bc8ec" && [ -n "$SESSION_ID" ] && [ "$SESSION_ID" != "REPLACE_WITH_ACTUAL_SESSION_ID" ] && echo "SESSION_ID_VALID: $SESSION_ID" || (echo "SESSION_ID_INVALID" && exit 1)

# Working directory verification
[ "$(pwd)" = "/workspace/main" ] || (echo "ERROR: Wrong directory" && exit 1)

# Todo.md accessibility
[ -f "todo.md" ] || (echo "ERROR: todo.md not accessible" && exit 1)
```

### Mandatory task.md Creation {#mandatory-taskmd-creation}

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

**CRITICAL LIFECYCLE NOTE**: task.md is created during CLASSIFIED state and persists through entire task
execution. It is ONLY removed during CLEANUP state along with all other task artifacts.

### Atomic Lock Acquisition Pattern {#atomic-lock-acquisition-pattern}
```bash
# MANDATORY atomic lock acquisition
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

### Implementation Safety Guards {#implementation-safety-guards}
**Before ANY Write/Edit/MultiEdit operation:**
```bash
# Working directory validation
pwd | grep -q "/workspace/tasks/.*/code$" || (echo "ERROR: Invalid working directory" && exit 1)

# Clean repository state
git status --porcelain | grep -E "(dist/|node_modules/|target/|\.jar$)" && (echo "ERROR: Prohibited files detected" && exit 1)
```

### Build Validation Gates {#build-validation-gates}
**Mandatory after implementation:**
```bash
# Full verification (build + tests + linters) before completion
./mvnw verify -q || (echo "ERROR: Build/test/linter failure - task cannot be completed" && exit 1)

# Note: 'mvnw verify' executes: compile → test → checkstyle → PMD → all quality gates
```

### Decision Parsing Enforcement {#decision-parsing-enforcement}
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

### Protocol Violation Reporting Pattern {#protocol-violation-reporting-pattern}
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
**Optimization patterns:**
```python
# Batch related operations in single message
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
# State consistency check
current_state=$(jq -r '.current_state' state.json)
expected_state="$1"
[ "$current_state" = "$expected_state" ] || (echo "ERROR: State inconsistency" && exit 1)
```

## TOOL-SPECIFIC OPTIMIZATION PATTERNS {#tool-specific-optimization-patterns}

### Bash Tool Usage {#bash-tool-usage}
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

## ERROR RECOVERY PROTOCOLS {#error-recovery-protocols}

### Violation Detection Triggers {#violation-detection-triggers}
**Automatic violation detection:**
- Wrong directory (pwd check fails)
- Missing locks (lock file check fails)
- Build failures (non-zero exit codes)
- Git operation failures (rebase/merge fails)
- Prohibited files detected (status check fails)

**Recovery Action**: TERMINATE current task, restart from INIT state

### Multi-Instance Conflict Resolution {#multi-instance-conflict-resolution}
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

### Partial Completion Recovery {#partial-completion-recovery}

**COMPREHENSIVE RECOVERY PROCEDURES**: This section expands on partial completion scenarios with detailed recovery workflows.

**Scenario Classification**:
1. **Session Interruption**: Task interrupted mid-state (context compact, crash, user stop)
2. **Agent Partial Completion**: Some agents complete, others fail or timeout
3. **Build Failure**: Compilation/test failures after partial implementation
4. **State Corruption**: Lock file or worktree inconsistencies

#### Recovery Procedure 1: Session Interruption Recovery {#recovery-procedure-1-session-interruption-recovery}

**Task interruption handling:**
```bash
# SessionStart hook detects active task for current session
TASK_NAME=$(jq -r --arg sid "$SESSION_ID" '.task_name | select(.session_id == $sid)' /workspace/tasks/*/task.json 2>/dev/null)

if [ -n "$TASK_NAME" ]; then
    CURRENT_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")
    echo "=== RESUMING TASK: $TASK_NAME (State: $CURRENT_STATE) ==="

    case "$CURRENT_STATE" in
        "INIT"|"CLASSIFIED")
            # Early states - safe to restart from beginning
            echo "Early state detected - resuming from current state"
            ;;

        "REQUIREMENTS")
            # Check agent completion status
            echo "Checking requirements agent completion status..."
            # Use agent completion verification from task-protocol-core.md
            verify_agent_completion_status
            # Resume agent invocations if needed
            ;;

        "SYNTHESIS")
            # Check if plan approval obtained
            if [ -f "/workspace/tasks/$TASK_NAME/user-plan-approval-obtained.flag" ]; then
                echo "Plan approval obtained - proceeding to IMPLEMENTATION"
            else
                echo "Waiting for plan approval - re-presenting plan to user"
            fi
            ;;

        "IMPLEMENTATION")
            # Check agent implementation status
            echo "Checking implementation agent completion status..."
            verify_implementation_rounds_status
            ;;

        "VALIDATION")
            # Re-run validation if interrupted
            echo "Re-running validation checks..."
            ;;

        "REVIEW")
            # Check agent review status
            echo "Checking review agent completion status..."
            verify_review_agent_status
            ;;

        "AWAITING_USER_APPROVAL")
            # Re-present changes for approval
            COMMIT_SHA=$(jq -r '.checkpoint.commit_sha' "/workspace/tasks/$TASK_NAME/task.json")
            echo "Re-presenting changes (commit $COMMIT_SHA) for user approval..."
            ;;

        "COMPLETE"|"CLEANUP")
            # Resume completion/cleanup procedures
            echo "Resuming ${CURRENT_STATE} state procedures..."
            ;;

        *)
            echo "Unknown state: $CURRENT_STATE - manual intervention required"
            ;;
    esac
else
    echo "No active tasks for current session"
fi
```

#### Recovery Procedure 2: Agent Partial Completion {#recovery-procedure-2-agent-partial-completion}

**Scenario**: During REQUIREMENTS/IMPLEMENTATION, some agents complete successfully while others fail, timeout, or return errors.

**Detection**:
```bash
detect_partial_agent_completion() {
    local TASK_NAME=$1
    local STATE=$2  # "REQUIREMENTS" or "IMPLEMENTATION"
    local AGENTS=($(jq -r '.required_agents[]' "/workspace/tasks/$TASK_NAME/task.json"))

    local COMPLETE_COUNT=0
    local ERROR_COUNT=0
    local TIMEOUT_COUNT=0
    local MISSING_COUNT=0

    for agent in "${AGENTS[@]}"; do
        STATUS_FILE="/workspace/tasks/$TASK_NAME/agents/$agent/status.json"

        if [ ! -f "$STATUS_FILE" ]; then
            ((MISSING_COUNT++))
            echo "❌ $agent: status.json missing (not invoked or invocation failed)"
            continue
        fi

        STATUS=$(jq -r '.status' "$STATUS_FILE" 2>/dev/null)
        case "$STATUS" in
            "COMPLETE")
                ((COMPLETE_COUNT++))
                echo "✅ $agent: COMPLETE"
                ;;
            "ERROR")
                ((ERROR_COUNT++))
                ERROR_MSG=$(jq -r '.error_message // "Unknown"' "$STATUS_FILE")
                echo "❌ $agent: ERROR - $ERROR_MSG"
                ;;
            "WORKING"|"IN_PROGRESS")
                # Check timestamp to detect timeouts
                LAST_UPDATE=$(jq -r '.updated_at' "$STATUS_FILE")
                AGE_MINUTES=$(( ($(date +%s) - $(date -d "$LAST_UPDATE" +%s)) / 60 ))
                if [ $AGE_MINUTES -gt 60 ]; then
                    ((TIMEOUT_COUNT++))
                    echo "⏱️ $agent: TIMEOUT (stuck $AGE_MINUTES min)"
                else
                    echo "⏳ $agent: WORKING (${AGE_MINUTES}min elapsed)"
                fi
                ;;
            *)
                echo "⚠️ $agent: Unknown status '$STATUS'"
                ;;
        esac
    done

    echo "Summary: ${COMPLETE_COUNT}/${#AGENTS[@]} complete, $ERROR_COUNT errors, $TIMEOUT_COUNT timeouts, $MISSING_COUNT missing"

    # Return status indicating recovery action needed
    if [ ${#AGENTS[@]} -eq $COMPLETE_COUNT ]; then
        return 0  # All complete
    else
        return 1  # Partial completion
    fi
}
```

**Recovery Actions**:
```bash
recover_partial_agent_completion() {
    local TASK_NAME=$1
    local STATE=$2
    local AGENTS=($(jq -r '.required_agents[]' "/workspace/tasks/$TASK_NAME/task.json"))

    echo "=== PARTIAL AGENT COMPLETION RECOVERY ==="

    # Strategy 1: Re-invoke incomplete agents
    INCOMPLETE_AGENTS=()
    for agent in "${AGENTS[@]}"; do
        STATUS_FILE="/workspace/tasks/$TASK_NAME/agents/$agent/status.json"

        if [ ! -f "$STATUS_FILE" ]; then
            INCOMPLETE_AGENTS+=("$agent")
            continue
        fi

        STATUS=$(jq -r '.status' "$STATUS_FILE")
        if [ "$STATUS" != "COMPLETE" ]; then
            INCOMPLETE_AGENTS+=("$agent")
        fi
    done

    if [ ${#INCOMPLETE_AGENTS[@]} -eq 0 ]; then
        echo "✅ All agents complete - no recovery needed"
        return 0
    fi

    echo "Incomplete agents: ${INCOMPLETE_AGENTS[*]}"
    echo "Recovery action: Re-invoking incomplete agents..."

    # Re-invoke incomplete agents with same requirements
    for agent in "${INCOMPLETE_AGENTS[@]}"; do
        echo "Re-invoking: $agent"

        # Check retry count to prevent infinite loops
        RETRY_COUNT=$(jq -r '.retry_count // 0' "/workspace/tasks/$TASK_NAME/agents/$agent/status.json" 2>/dev/null)
        if [ "$RETRY_COUNT" -ge 3 ]; then
            echo "⚠️ $agent: Max retries exceeded (3) - escalating to user"
            escalate_agent_failure_to_user "$agent"
            continue
        fi

        # Update retry count
        jq --arg count "$((RETRY_COUNT + 1))" '.retry_count = ($count | tonumber)' \
           "/workspace/tasks/$TASK_NAME/agents/$agent/status.json" > "/tmp/status.tmp" 2>/dev/null
        mv "/tmp/status.tmp" "/workspace/tasks/$TASK_NAME/agents/$agent/status.json" 2>/dev/null

        # Re-invoke agent via Task tool
        # (Main agent should use Task tool here with appropriate prompt)
    done

    # Wait for completion and verify
    wait_for_agent_completion "${INCOMPLETE_AGENTS[@]}"
}
```

#### Recovery Procedure 3: Build Failure After Partial Implementation {#recovery-procedure-3-build-failure-after-partial-implementation}

**Scenario**: Some agents merged changes to task branch, but final build verification fails.

**Detection & Recovery**:
```bash
recover_build_failure_partial_implementation() {
    local TASK_NAME=$1

    echo "=== BUILD FAILURE RECOVERY ==="

    # Step 1: Identify which merges succeeded
    echo "Analyzing git log for merged agent work..."
    git log --oneline -20 | grep -E "(architect|engineer|formatter)"

    # Step 2: Run build to get specific error details
    echo "Running build to identify specific failures..."
    BUILD_OUTPUT=$(./mvnw verify 2>&1 | tee /tmp/build-failure.log)

    # Step 3: Classify failure type
    if echo "$BUILD_OUTPUT" | grep -q "COMPILATION ERROR"; then
        echo "Type: Compilation errors"
        RECOVERY_ACTION="fix_compilation_errors"
    elif echo "$BUILD_OUTPUT" | grep -q "There are test failures"; then
        echo "Type: Test failures"
        RECOVERY_ACTION="fix_test_failures"
    elif echo "$BUILD_OUTPUT" | grep -q "Checkstyle violations"; then
        echo "Type: Style violations"
        RECOVERY_ACTION="fix_style_violations"
    else
        echo "Type: Unknown build failure"
        RECOVERY_ACTION="investigate_manually"
    fi

    # Step 4: Execute recovery action
    case "$RECOVERY_ACTION" in
        "fix_compilation_errors")
            # Extract compilation errors
            ERRORS=$(echo "$BUILD_OUTPUT" | grep -A5 "COMPILATION ERROR")
            echo "Compilation errors found:"
            echo "$ERRORS"

            # Decision: Main agent fixes if simple (missing imports)
            # Otherwise, re-delegate to architect
            if echo "$ERRORS" | grep -q "cannot find symbol"; then
                echo "Likely missing imports - main agent can fix"
                # Fix missing imports
            else
                echo "Complex compilation errors - re-delegating to architect"
                # Re-invoke architect to fix
            fi
            ;;

        "fix_test_failures")
            # Show test failures
            echo "$BUILD_OUTPUT" | grep -E "Tests run:|Failures:"

            # Re-delegate to test for test fixes
            echo "Re-delegating test failures to test agent"
            ;;

        "fix_style_violations")
            # Count violations
            VIOLATION_COUNT=$(echo "$BUILD_OUTPUT" | grep -oP '\[\d+\] violations' | grep -oP '\d+' | head -1)

            if [ "$VIOLATION_COUNT" -le 5 ]; then
                echo "Small number of violations ($VIOLATION_COUNT) - main agent fixes"
                # Fix style violations directly
            else
                echo "Many violations ($VIOLATION_COUNT) - re-delegating to style"
                # Re-invoke style
            fi
            ;;

        "investigate_manually")
            echo "Build failure requires manual investigation"
            echo "Build log saved to /tmp/build-failure.log"
            # Escalate to user
            ;;
    esac

    # Step 5: Re-run build after fixes
    echo "Re-running build after fixes..."
    ./mvnw verify || {
        echo "Build still failing - may need additional recovery iterations"
        return 1
    }

    echo "✅ Build recovered successfully"
    return 0
}
```

#### Recovery Procedure 4: State Corruption Recovery {#recovery-procedure-4-state-corruption-recovery}

**Scenario**: Lock file state doesn't match actual work completed, or worktrees have issues.

**Detection**:
```bash
detect_state_corruption() {
    local TASK_NAME=$1

    echo "=== STATE CORRUPTION DETECTION ==="

    ISSUES_FOUND=false

    # Check 1: Lock file exists and is valid JSON
    if [ ! -f "/workspace/tasks/$TASK_NAME/task.json" ]; then
        echo "❌ Lock file missing"
        ISSUES_FOUND=true
    elif ! jq empty "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null; then
        echo "❌ Lock file corrupted (invalid JSON)"
        ISSUES_FOUND=true
    fi

    # Check 2: Session ID matches
    LOCK_SESSION=$(jq -r '.session_id' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null)
    if [ "$LOCK_SESSION" != "$SESSION_ID" ]; then
        echo "⚠️ Session ID mismatch: lock=$LOCK_SESSION, current=$SESSION_ID"
        ISSUES_FOUND=true
    fi

    # Check 3: Worktrees exist
    if [ ! -d "/workspace/tasks/$TASK_NAME/code" ]; then
        echo "❌ Task worktree missing"
        ISSUES_FOUND=true
    fi

    # Check 4: Agent worktrees match required_agents
    REQUIRED_AGENTS=($(jq -r '.required_agents[]' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null))
    for agent in "${REQUIRED_AGENTS[@]}"; do
        if [ ! -d "/workspace/tasks/$TASK_NAME/agents/$agent/code" ]; then
            echo "❌ Agent worktree missing: $agent"
            ISSUES_FOUND=true
        fi
    done

    # Check 5: State transition log is consistent
    TRANSITION_LOG=$(jq -r '.transition_log' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null)
    if [ "$TRANSITION_LOG" = "null" ] || [ -z "$TRANSITION_LOG" ]; then
        echo "❌ Transition log missing or empty"
        ISSUES_FOUND=true
    fi

    if $ISSUES_FOUND; then
        return 1  # Corruption detected
    else
        echo "✅ No state corruption detected"
        return 0  # State is clean
    fi
}

recover_state_corruption() {
    local TASK_NAME=$1

    echo "=== STATE CORRUPTION RECOVERY ==="

    # Recovery strategy depends on severity
    detect_state_corruption "$TASK_NAME"
    SEVERITY=$?

    if [ $SEVERITY -eq 0 ]; then
        echo "No recovery needed - state is valid"
        return 0
    fi

    # Attempt automatic recovery
    echo "Attempting automatic state recovery..."

    # Fix 1: Recreate missing worktrees (if lock and branch exist)
    CURRENT_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null)

    if [ ! -d "/workspace/tasks/$TASK_NAME/code" ] && git show-ref --verify --quiet "refs/heads/$TASK_NAME"; then
        echo "Recreating task worktree from existing branch..."
        git worktree add "/workspace/tasks/$TASK_NAME/code" "$TASK_NAME"
    fi

    # Fix 2: Recreate agent worktrees (if needed)
    REQUIRED_AGENTS=($(jq -r '.required_agents[]' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null))
    for agent in "${REQUIRED_AGENTS[@]}"; do
        AGENT_BRANCH="${TASK_NAME}-${agent}"
        if [ ! -d "/workspace/tasks/$TASK_NAME/agents/$agent/code" ] && git show-ref --verify --quiet "refs/heads/$AGENT_BRANCH"; then
            echo "Recreating agent worktree: $agent"
            mkdir -p "/workspace/tasks/$TASK_NAME/agents/$agent"
            git worktree add "/workspace/tasks/$TASK_NAME/agents/$agent/code" "$AGENT_BRANCH"
        fi
    done

    # Fix 3: Repair lock file if corrupted
    if ! jq empty "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null; then
        echo "Lock file corrupted - manual intervention required"
        echo "Options:"
        echo "1. Delete task and restart from scratch"
        echo "2. Reconstruct lock file from git history"
        echo "3. Escalate to user"
        return 1
    fi

    # Verify recovery
    detect_state_corruption "$TASK_NAME"
    if [ $? -eq 0 ]; then
        echo "✅ State corruption recovered successfully"
        return 0
    else
        echo "❌ Automatic recovery failed - manual intervention required"
        return 1
    fi
}
```

#### Recovery Decision Tree {#recovery-decision-tree}

**When to Apply Each Recovery Procedure**:

```
IF (session interrupted during task execution):
    → Apply Recovery Procedure 1 (Session Interruption Recovery)

ELSE IF (agents not all COMPLETE during REQUIREMENTS or IMPLEMENTATION):
    → Apply Recovery Procedure 2 (Agent Partial Completion)

ELSE IF (build fails after agent merges):
    → Apply Recovery Procedure 3 (Build Failure After Partial Implementation)

ELSE IF (lock file or worktrees corrupted):
    → Apply Recovery Procedure 4 (State Corruption Recovery)
```

#### Recovery Escalation Policy {#recovery-escalation-policy}

**When to escalate to user**:
1. Agent failure after 3 retries
2. Build failure with unknown error type
3. State corruption that cannot be automatically recovered
4. Conflicting agent requirements with no resolution path
5. External dependency blocking progress

**Escalation Message Template**:
```
"Task '$TASK_NAME' encountered a recovery scenario requiring user guidance:

**Issue**: [Description of the problem]
**Current State**: [Lock file state]
**Recovery Attempts**: [What was tried]
**Impact**: [What work is at risk]

**Options**:
1. [Option 1 with pros/cons]
2. [Option 2 with pros/cons]
3. Abandon task and select alternative

Please advise on how to proceed."
```

**Foreign Session Handling**:
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

### Multiple Interruption Handling {#multiple-interruption-handling}

**CRITICAL REQUIREMENT**: When user interrupts multiple times before resuming work, maintain correct state tracking and resume from the original pre-interruption state.

#### Scenario: Successive Interruptions {#scenario-successive-interruptions}

**Common Pattern**:
1. Agent working in IMPLEMENTATION state
2. User asks question → Agent answers → Still interrupted
3. User runs /audit-session → Agent audits → Still interrupted
4. User asks clarification → Agent answers → Still interrupted
5. User says "continue working" → NOW resume IMPLEMENTATION

**Problem**: Agent must resume from IMPLEMENTATION (original state), NOT from whatever activity occurred during interruptions.

#### State Preservation During Interruptions {#state-preservation-during-interruptions}

**Core Principle**: Lock file state NEVER changes during interruption handling.

```bash
# Example: Agent in IMPLEMENTATION state when interrupted

# Check 1: Before handling ANY user command, record current state
ACTIVE_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")
echo "Active task state: $ACTIVE_STATE (preserved during interruption handling)"

# Check 2: Handle user command (question, audit, clarification, etc.)
# ... process user request ...

# Check 3: After handling, state should be UNCHANGED
CURRENT_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")
if [ "$CURRENT_STATE" != "$ACTIVE_STATE" ]; then
    echo "❌ ERROR: State changed during interruption handling"
    echo "Expected: $ACTIVE_STATE, Got: $CURRENT_STATE"
    exit 1
fi

# Check 4: Only when user says "continue/resume/proceed" → resume active state
```

#### Interruption Type Classification {#interruption-type-classification}

**Non-Resuming Interruptions** (do NOT change state, do NOT trigger resumption):
- User questions about code/architecture/implementation
- User requests for clarification
- User runs /audit-session or other diagnostic commands
- User requests status updates
- User provides feedback/observations during implementation

**Resumption Triggers** (signal to return to active state):
- User says "continue"
- User says "resume work"
- User says "proceed"
- User says "keep going"
- User says "continue with the task"

#### Multi-Interruption Handling Logic {#multi-interruption-handling-logic}

```bash
handle_user_command_during_task() {
    local TASK_NAME=$1
    local USER_COMMAND=$2

    # Retrieve active state (preserved from before interruption)
    ACTIVE_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")

    echo "=== HANDLING USER COMMAND ==="
    echo "Active task: $TASK_NAME"
    echo "Active state: $ACTIVE_STATE (preserved)"

    # Classify user command
    if [[ "$USER_COMMAND" =~ (continue|resume|proceed|keep.*going) ]]; then
        COMMAND_TYPE="RESUMPTION"
    elif [[ "$USER_COMMAND" =~ (audit|/audit-session) ]]; then
        COMMAND_TYPE="AUDIT"
    elif [[ "$USER_COMMAND" =~ (what|how|why|explain|clarify) ]]; then
        COMMAND_TYPE="QUESTION"
    elif [[ "$USER_COMMAND" =~ (status|progress|where.*are.*we) ]]; then
        COMMAND_TYPE="STATUS_REQUEST"
    else
        COMMAND_TYPE="OTHER"
    fi

    echo "Command type: $COMMAND_TYPE"

    case "$COMMAND_TYPE" in
        "RESUMPTION")
            echo "Resumption trigger detected - returning to active state: $ACTIVE_STATE"
            resume_task_from_active_state "$TASK_NAME" "$ACTIVE_STATE"
            ;;

        "AUDIT")
            echo "Running audit - state will be preserved"
            run_audit_command "$TASK_NAME"
            echo "Audit complete - state remains: $ACTIVE_STATE"
            echo "Awaiting resumption trigger or additional commands"
            ;;

        "QUESTION")
            echo "Answering user question - state will be preserved"
            answer_user_question "$USER_COMMAND"
            echo "Question answered - state remains: $ACTIVE_STATE"
            echo "Awaiting resumption trigger or additional commands"
            ;;

        "STATUS_REQUEST")
            echo "Providing status update - state will be preserved"
            provide_status_update "$TASK_NAME" "$ACTIVE_STATE"
            echo "Status provided - state remains: $ACTIVE_STATE"
            echo "Awaiting resumption trigger or additional commands"
            ;;

        "OTHER")
            echo "Processing command - state will be preserved"
            process_other_command "$USER_COMMAND"
            echo "Command processed - state remains: $ACTIVE_STATE"
            echo "Awaiting resumption trigger or additional commands"
            ;;
    esac

    # Verify state unchanged (except for RESUMPTION type)
    if [ "$COMMAND_TYPE" != "RESUMPTION" ]; then
        VERIFY_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")
        if [ "$VERIFY_STATE" != "$ACTIVE_STATE" ]; then
            echo "❌ CRITICAL ERROR: State changed during interruption handling"
            echo "This violates state preservation principle"
            exit 1
        fi
    fi
}
```

#### Resumption Logic After Multiple Interruptions {#resumption-logic-after-multiple-interruptions}

```bash
resume_task_from_active_state() {
    local TASK_NAME=$1
    local ACTIVE_STATE=$2

    echo "=== RESUMING TASK: $TASK_NAME ==="
    echo "Resuming from state: $ACTIVE_STATE"

    case "$ACTIVE_STATE" in
        "REQUIREMENTS")
            echo "Resuming requirements gathering..."
            # Check which agents completed before interruption
            check_requirements_agent_status "$TASK_NAME"
            # Resume incomplete agent invocations
            resume_incomplete_agents "$TASK_NAME" "REQUIREMENTS"
            ;;

        "IMPLEMENTATION")
            echo "Resuming implementation work..."
            # Check which agents completed implementation
            check_implementation_agent_status "$TASK_NAME"
            # Resume incomplete implementation rounds
            resume_implementation_rounds "$TASK_NAME"
            ;;

        "VALIDATION")
            echo "Resuming validation checks..."
            # Re-run validation from where it was interrupted
            run_validation_checks "$TASK_NAME"
            ;;

        "REVIEW")
            echo "Resuming review process..."
            # Check which review agents completed
            check_review_agent_status "$TASK_NAME"
            # Resume incomplete reviews
            resume_incomplete_reviews "$TASK_NAME"
            ;;

        "AWAITING_USER_APPROVAL")
            echo "Returning to user approval checkpoint..."
            # Re-present changes for approval
            COMMIT_SHA=$(jq -r '.checkpoint.commit_sha' "/workspace/tasks/$TASK_NAME/task.json")
            present_changes_for_approval "$TASK_NAME" "$COMMIT_SHA"
            ;;

        *)
            echo "Resuming from state: $ACTIVE_STATE"
            # Continue with state-specific work
            ;;
    esac
}
```

#### Examples: Multi-Interruption Scenarios {#examples-multi-interruption-scenarios}

**Example 1: Question → Audit → Question → Resume**

```
Initial State: IMPLEMENTATION (architect working)

[User interrupts with question]
User: "What architecture pattern are you using?"
Agent: [Answers question, lock state remains IMPLEMENTATION]

[User interrupts again with audit]
User: "/audit-session"
Agent: [Runs audit, lock state remains IMPLEMENTATION]

[User interrupts again with clarification]
User: "Can you explain why you chose that pattern?"
Agent: [Explains, lock state remains IMPLEMENTATION]

[User triggers resumption]
User: "Continue working"
Agent: [Resumes IMPLEMENTATION state - architect continues work]
```

**Example 2: Status → Audit → Resume**

```
Initial State: REVIEW (waiting for agent decisions)

[User interrupts with status request]
User: "Where are we in the process?"
Agent: [Provides status, lock state remains REVIEW]

[User interrupts with audit]
User: "Run audit to verify everything is correct"
Agent: [Runs audit, lock state remains REVIEW]

[User triggers resumption]
User: "Proceed with review"
Agent: [Resumes REVIEW state - continues waiting for agent decisions or processes received decisions]
```

**Example 3: Multiple Questions Before Resume**

```
Initial State: SYNTHESIS (drafting implementation plan)

[Interruption 1]
User: "How will you handle error cases?"
Agent: [Answers, lock state remains SYNTHESIS]

[Interruption 2]
User: "What about thread safety?"
Agent: [Answers, lock state remains SYNTHESIS]

[Interruption 3]
User: "Will this work with existing code?"
Agent: [Answers, lock state remains SYNTHESIS]

[Interruption 4]
User: "Can you explain the testing strategy?"
Agent: [Answers, lock state remains SYNTHESIS]

[User triggers resumption]
User: "Continue with the implementation plan"
Agent: [Resumes SYNTHESIS state - completes plan and presents for user approval]
```

#### Anti-Patterns: What NOT To Do {#anti-patterns-what-not-to-do}

**❌ WRONG: Treating each interruption as a state change**
```bash
# VIOLATION: Lock state changes with every command
User: "What's the status?"
Agent: Updates lock state to "STATUS_CHECK"  # ❌ WRONG

User: "Run audit"
Agent: Updates lock state to "AUDITING"  # ❌ WRONG

User: "Continue"
Agent: Updates lock state to "COMPLETE"  # ❌ WRONG - lost original state!
```

**❌ WRONG: Auto-resuming after interruption without explicit trigger**
```bash
# VIOLATION: Resuming without user signal
User: "What's happening now?"
Agent: [Answers question]
Agent: [Immediately continues IMPLEMENTATION work]  # ❌ WRONG - user didn't say continue
```

**❌ WRONG: Losing track of original state**
```bash
# VIOLATION: Not preserving original state
Initial: IMPLEMENTATION
User: "Run audit"
Agent: [Runs audit, forgets it was in IMPLEMENTATION]
User: "Continue"
Agent: "Continue with what?"  # ❌ WRONG - should remember IMPLEMENTATION
```

**✅ CORRECT: State preservation across all interruptions**
```bash
# CORRECT: Lock state never changes during interruptions
Initial: IMPLEMENTATION (lock file shows state=IMPLEMENTATION)

User: "What's the status?" → lock still shows IMPLEMENTATION
User: "Run audit" → lock still shows IMPLEMENTATION
User: "Explain your approach" → lock still shows IMPLEMENTATION
User: "Continue" → Resume IMPLEMENTATION (state was preserved)
```

#### Interruption Context Tracking {#interruption-context-tracking}

**Optional Enhancement**: Track interruption history for context (NOT required, but useful for debugging)

```bash
# Store interruption log in lock file (optional)
record_interruption() {
    local TASK_NAME=$1
    local COMMAND_TYPE=$2

    jq --arg cmd "$COMMAND_TYPE" \
       --arg ts "$(date -Iseconds)" \
       '.interruptions += [{"type": $cmd, "timestamp": $ts}]' \
       "/workspace/tasks/$TASK_NAME/task.json" > /tmp/lock.tmp
    mv /tmp/lock.tmp "/workspace/tasks/$TASK_NAME/task.json"
}

# Retrieve interruption count
get_interruption_count() {
    local TASK_NAME=$1
    jq '.interruptions | length' "/workspace/tasks/$TASK_NAME/task.json"
}
```

#### Checkpoint Interaction: Interruptions During User Approval Wait {#checkpoint-interaction-interruptions-during-user-approval-wait}

**Special Case**: User asks questions WHILE at a checkpoint (SYNTHESIS or AWAITING_USER_APPROVAL)

```bash
# Scenario: Agent waiting at PLAN APPROVAL checkpoint
CURRENT_STATE="SYNTHESIS"
echo "Waiting for user plan approval..."

# User asks question INSTEAD of approving
User: "Can you explain the architecture decision?"
Agent: [Answers question]
# State remains SYNTHESIS - still waiting for approval

# User asks another question
User: "What's the performance impact?"
Agent: [Answers question]
# State remains SYNTHESIS - still waiting for approval

# User finally approves
User: "Looks good, proceed with implementation"
Agent: [Recognizes approval, transitions SYNTHESIS → IMPLEMENTATION]
```

**Key Insight**: At checkpoints, interruptions (questions) are STILL non-resuming. Only approval keywords trigger state transition.

## WORKFLOW EXECUTION ENGINE {#workflow-execution-engine}

### Main Task Execution Function {#main-task-execution-function}
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

## MIGRATION FROM PHASE-BASED PROTOCOL {#migration-from-phase-based-protocol}

**Phase-to-State Mapping**: Phase 1=REQUIREMENTS, Phase 2=SYNTHESIS, Phase 3=IMPLEMENTATION, Phase
4=VALIDATION, Phase 5=Resolution cycles, Phase 6=REVIEW, Phase 7=CLEANUP

**Enforcement**: Mandatory transition conditions with no manual overrides or exceptions

**END OF STATE MACHINE PROTOCOL**

