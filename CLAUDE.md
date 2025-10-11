# Claude Code Configuration Guide

Styler Java Code Formatter project configuration and workflow guidance.

## 🚨 MANDATORY COMPLIANCE

**CRITICAL WORKFLOW**: Task Protocol ([core](docs/project/task-protocol-core.md) + [operations](docs/project/task-protocol-operations.md) + [delegated](docs/project/delegated-implementation-protocol.md)) - Apply risk-based protocol selection per file risk classification.
**CRITICAL LOCK OWNERSHIP**: See [§ Lock Ownership](#lock-ownership) for lock file requirements and ownership rules.
**CRITICAL WORKTREE ISOLATION**: See [§ Worktree Isolation](#worktree-isolation) for worktree management requirements.
**CRITICAL GIT HISTORY VERIFICATION**: See [§ Git History Verification](#git-history-verification) for verification after history rewriting operations.
**CRITICAL STYLE**: Complete style validation = checkstyle + PMD + manual rules (see task-protocol-core.md)
**CRITICAL PERSISTENCE**: [Long-term solution persistence](#-long-term-solution-persistence) - Prioritize optimal solutions over expedient alternatives.
**CRITICAL TASK COMPLETION**: Tasks complete after ALL 7 protocol phases. Implementation ≠ task completion. Mark complete only after Phase 7.
**IMPLEMENTATION COMPLETION TRIGGER**: After implementation work (code changes, fixes, features), complete ALL remaining protocol phases before selecting new task. SessionStart hook indicates active task ownership.

**PHASE COMPLETION VERIFICATION**: Before declaring ANY phase complete, you MUST:
1. **READ**: Execute `grep -A 20 "^## State [N]:" docs/project/task-protocol-*.md` to read ACTUAL phase requirements
2. **CHECKLIST**: Create explicit checklist of all phase requirements from task-protocol files
3. **VERIFY**: Confirm EACH requirement is met with evidence (command output, file checks, etc.)
4. **DECLARE**: Only after ALL requirements verified, declare phase complete

**Detailed Archival Example:**

❌ **WRONG** (marking task complete in todo.md):
```bash
# Modified todo.md: changed - [ ] to - [x], added completion details
vim todo.md  # Line 547: - [x] **TASK:** debug-maven-build-cache...

git add todo.md pom.xml docs/project/build-system.md
git commit -m "Fix Maven build cache..."
# Result: Task still in todo.md ❌, not in changelog.md ❌
```

✅ **CORRECT** (moving task from todo.md to changelog.md):
```bash
# Step 1: DELETE task from todo.md (remove entire entry, lines 547-567)
vim todo.md  # Delete all lines for debug-maven-build-cache-stale-instances

# Step 2: ADD task to changelog.md under ## 2025-10-08
vim changelog.md  # Add completion entry with full details

# Step 3: Verify archival
grep "debug-maven-build-cache" todo.md && echo "❌ ERROR: Still in todo.md!"
grep "debug-maven-build-cache" changelog.md || echo "❌ ERROR: Not in changelog.md!"

# Step 4: Commit both files together
git add todo.md changelog.md pom.xml docs/project/build-system.md
git commit -m "Fix Maven build cache..."  # Single atomic commit
# Result: Task removed from todo.md ✅, added to changelog.md ✅
```

**TODO Synchronization**: Keep TodoWrite tool synced with todo.md file.
**TODO Clarity**: Each todo.md entry must contain sufficient detail to understand the task without external context. One-line descriptions require nested sub-items explaining Purpose, Scope, Components/Features, and Integration points.
**CRITICAL TASK ARCHIVAL**: When completing a task (Phase 7/COMPLETE state), you MUST:
1. **REMOVE** the completed task from todo.md (delete the entire task entry)
2. **ADD** it to changelog.md under the appropriate date section (format: `## YYYY-MM-DD`)
3. **INCLUDE** completion details: solution implemented, files modified, test results, quality gates, completion date
4. **COMMIT** all changes in ONE atomic commit (implementation code + todo.md removal + changelog.md addition)
5. **FORMAT** changelog entries to match existing style (see changelog.md for examples)

**🚨 PROHIBITED ARCHIVAL PATTERNS**:
❌ Changing `- [ ]` to `- [x]` in todo.md (tasks must be REMOVED, not marked complete)
❌ Adding completion details to todo.md entry (details go to changelog.md, not todo.md)
❌ Committing todo.md changes without corresponding changelog.md addition
❌ Leaving completed tasks in todo.md for "historical reference"

**✅ REQUIRED ARCHIVAL PATTERNS**:
✅ `git diff todo.md` shows ONLY deletions (lines removed, no modifications)
✅ `git diff changelog.md` shows ONLY additions under today's date
✅ Both files modified in SAME commit
✅ Task completely absent from todo.md after commit

**ARCHIVAL VERIFICATION CHECKLIST** (run before Phase 7 commit):
```bash
# Automated verification script (recommended)
bash .claude/hooks/verify-task-archival.sh task-name

# OR manual verification:
# 1. Verify task REMOVED from todo.md (not just marked complete)
grep -q "task-name" todo.md && {
    echo "❌ VIOLATION: Task still in todo.md - must be REMOVED"
    exit 1
}

# 2. Verify task ADDED to changelog.md
grep -q "task-name" changelog.md || {
    echo "❌ VIOLATION: Task not in changelog.md - must be ADDED"
    exit 1
}

# 3. Verify both files staged
git diff --cached --name-only | grep -q "todo.md" || echo "⚠️ todo.md not staged"
git diff --cached --name-only | grep -q "changelog.md" || echo "⚠️ changelog.md not staged"
```

**CRITICAL SYSTEM REMINDER PROCESSING**: Read ALL `<system-reminder>` blocks before responding. System reminders may contain user questions requiring immediate answers. Check each for "The user sent the following message" and address FIRST.
**CRITICAL MID-TASK WORK CAPTURE**: When user mentions additional work during task execution, add to TodoWrite list IMMEDIATELY in SAME response. Context compaction causes complete loss of uncommitted requests - capture ALL new work requests instantly.
**CRITICAL TOKEN USAGE**: Token usage MUST NOT affect behavior, implementation approach, or task completion. IGNORE token warnings and limits. Continue with full quality until 100% complete or explicit user instruction. Token budget does NOT justify incomplete implementations, shortcuts, or quality compromises.
**CRITICAL BUG TESTING**: [Unit Test Driven Bug Fixing](#-unit-test-driven-bug-fixing) - Test-first approach for ALL bugs.
**CRITICAL JAVADOC**: Write JavaDoc manually with contextual understanding. NEVER use scripts, sed, awk, or automated generation. Each comment must reflect specific method purpose and context. See [§ JavaDoc Manual Documentation](#javadoc-manual-documentation).
**🚨 VIOLATION = IMMEDIATE TASK RESTART REQUIRED**

## 🚨 SYSTEM REMINDER PROCESSING {#system-reminders}

**MANDATORY PROCESSING ORDER**:
1. Read ALL system reminders in response
2. Check for "The user sent the following message" pattern
3. Identify user questions or instructions
4. Answer user questions IMMEDIATELY before other work
5. Continue with planned tasks

**Common Patterns**:
- `<system-reminder>The user sent the following message:` - **USER QUESTION - ANSWER FIRST**
- `<system-reminder>The TodoWrite tool hasn't been used recently` - Optional suggestion
- `<system-reminder>Note: /path/to/file was read before` - Informational
- `<system-reminder>Contents of /path/to/file:` - File content injection

**PROHIBITED**: Skipping reminders, assuming user will repeat questions, treating reminders as optional, reading only first reminder
**REQUIRED**: Scan all reminders, process user questions first, acknowledge user input, answer completely

## 🚨 LOCK OWNERSHIP & TASK RECOVERY {#lock-ownership}

**Lock Ownership Rule**: ONLY work on tasks whose lock file contains YOUR session_id.

**SessionStart Hook**: `check-lock-ownership.sh` automatically checks for active tasks and provides instructions.

**LOCK FILE REQUIREMENTS**:
1. NEVER manually search lock files - SessionStart hook performs check automatically
2. Trust hook output - "No Active Tasks" means NO tasks regardless of other files
3. ONLY `.json` files valid - Lock files MUST be `/workspace/locks/{task-name}.json`
4. Invalid extensions (`.lock`, `.txt`, any non-`.json`) - Delete immediately if containing your session_id
5. NEVER remove lock files unless you own them (session_id must match)
6. If lock acquisition fails - Select alternative task, do NOT delete lock

**Lock File Format**:
```json
{
  "session_id": "unique-session-identifier",
  "task_name": "task-name-matching-filename",
  "state": "current-protocol-phase",
  "created_at": "ISO-8601-timestamp"
}
```

## 🚨 WORKTREE ISOLATION & CLEANUP {#worktree-isolation}

### During Task Execution

**After creating worktree, IMMEDIATELY `cd` to worktree directory before other operations.**

**Required Pattern**:
```bash
git worktree add /workspace/branches/{task}/code -b {task} && cd /workspace/branches/{task}/code
```

**ALL subsequent work inside worktree, NEVER in main branch.**

**Verification**:
```bash
pwd | grep -q "/workspace/branches/{task}/code$" && echo "✅ In worktree" || echo "❌ ERROR: Not in worktree!"
```

### During Cleanup (Phase 7/8)

**Before removing worktree, MUST `cd` to main worktree first. NEVER remove worktree while inside it (shell loses working directory).**

**Required Pattern**:
```bash
cd /workspace/branches/main/code && git worktree remove /workspace/branches/{task}/code
```

## 🚨 GIT HISTORY REWRITING VERIFICATION {#git-history-verification}

**ANY git history rewriting requires mandatory verification to ensure no commits or changes dropped.**

**History Rewriting Operations**: `git rebase`, `git rebase --onto`, `git commit --amend`, `git reset --hard` + new commits, `git filter-branch`, `git cherry-pick` with history modification, any operation changing commit SHAs

**MANDATORY VERIFICATION PROCEDURE** (execute ALL steps after history rewriting):

```bash
# 1. Verify commit count matches expectations
# Before rewriting: git rev-list --count HEAD
# After rewriting: git rev-list --count HEAD
# Compare counts - should match unless you explicitly intended to drop commits

# 2. Verify all expected commits are present
git log --oneline --all --graph
# Manually verify each expected commit appears in the history

# 3. Verify no changes were lost - compare working tree states
# Save state before rewriting: git diff-tree -r --no-commit-id --name-only <old-sha> | sort > /tmp/before-files.txt
# Check state after rewriting: git diff-tree -r --no-commit-id --name-only HEAD | sort > /tmp/after-files.txt
# Compare: diff /tmp/before-files.txt /tmp/after-files.txt

# 4. Verify file contents match expectations
git diff <old-sha> HEAD
# Should show ONLY intentional changes, no accidental deletions or modifications

# 5. For interactive rebase: verify no "pick" lines were accidentally deleted
# Check reflog to see what was dropped: git reflog
# Compare original and rebased histories
```

**VERIFICATION CHECKLIST** (ALL must pass):
- [ ] Commit count matches expectations (or intentional delta explained)
- [ ] All expected commit messages present in `git log`
- [ ] No unexpected file additions/deletions in `git diff <before> <after>`
- [ ] No unexpected content changes in modified files
- [ ] Reflog shows expected operations only
- [ ] Build still passes after rewriting (`mvn verify`)
- [ ] All tests still pass after rewriting

**PROHIBITED**: Completing rebase without verification, assuming "no conflicts" = "no data loss", trusting git output without inspection, skipping verification for "simple" operations, verifying only commit count

**REQUIRED**: Save original SHA (`ORIG_SHA=$(git rev-parse HEAD)`), compare before/after states, check reflog, run build after modifications, document verification results

**ENFORCEMENT**: History rewriting without verification = CRITICAL VIOLATION. Failed verification requires immediate rollback. Data loss recovery: `git reset --hard <original-sha>`

## 🚨 TASK PROTOCOL SUMMARY {#task-protocol}

**Full Protocol Details**: See [task-protocol-core.md](docs/project/task-protocol-core.md) and [task-protocol-operations.md](docs/project/task-protocol-operations.md) for complete state machine and transition requirements.

**State Machine**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE → VALIDATION → REVIEW → COMPLETE → CLEANUP

**Critical Requirements for All Tasks**:
- Lock acquisition (see [§ Lock Ownership](#lock-ownership))
- Worktree isolation (see [§ Worktree Isolation](#worktree-isolation))
- Build verification before merge
- Unanimous stakeholder approval (Phase 6/REVIEW)
- Complete all phases before selecting new task

**Risk-Based Variants**:
- **HIGH-RISK** (src/\*\*, pom.xml, security/\*\*): Full 7-phase protocol with all agents
- **MEDIUM-RISK** (tests, docs): Abbreviated protocol, domain-specific agents
- **LOW-RISK** (general docs): Streamlined protocol, minimal validation

**Post-Compaction Note**: This summary plus lock ownership and worktree isolation rules above are sufficient for basic protocol compliance when task-protocol files are not accessible.

## 🚨 RISK-BASED PROTOCOL SELECTION

**PROTOCOL SELECTION BASED ON FILE RISK:**
- **HIGH-RISK**: Full 7-phase protocol (src/\*\*, pom.xml, .github/\*\*, security/\*\*, CLAUDE.md)
- **MEDIUM-RISK**: Abbreviated protocol (test files, code-style docs, configuration)
- **LOW-RISK**: Streamlined protocol (general docs, todo.md, README files)

**AUTOMATIC RISK ASSESSMENT:**
✅ Pattern-based file classification determines workflow variant
✅ Escalation triggers force higher risk levels when needed
✅ Manual overrides available for edge cases
✅ Default to full protocol when risk unclear

**EFFICIENCY IMPROVEMENTS:**
✅ Documentation updates: 99%+ faster (5min → 0.2s)
✅ Safety preserved: Critical files always get full review
✅ Backward compatible: Existing workflows unchanged

**BATCH PROCESSING - AUTOMATIC CONTINUOUS MODE:**
✅ "Work on multiple tasks until done" - Auto-translates to continuous workflow mode
✅ "Complete all Phase 1 tasks" - Auto-translates to continuous mode with phase filtering
✅ "Work on study-claude-cli-interface task" - Single task with proper isolation
❌ Manual batch processing within single protocol execution - PROHIBITED

## 🚨 STAKEHOLDER CONSENSUS ENFORCEMENT

**Phase 6 requires UNANIMOUS stakeholder approval.**

**DECISION LOGIC**:
- ALL agents: "FINAL DECISION: ✅ APPROVED"
- ANY "❌ REJECTED" → Execute Phase 5 + re-run Phase 6
- NO human override - agent decisions ATOMIC and BINDING
- NO subjective "MVP scope" or "enhancement-level" assessments

## 🚨 AUTONOMOUS TASK COMPLETION REQUIREMENT

**After INIT state, complete ALL protocol states (0-8) autonomously with TWO MANDATORY USER APPROVAL CHECKPOINTS.**

**SINGLE-SESSION COMPLETION**:
- Task execution in ONE uninterrupted session
- **USER APPROVAL CHECKPOINTS** (NOT violations):
  1. **After SYNTHESIS**: Present plan via ExitPlanMode, wait for approval
  2. **After REVIEW**: Present changes, wait for approval to finalize
- NO OTHER HANDOFFS mid-protocol
- Complete all other states autonomously

**When to Ask User**:
✅ BEFORE starting: "Task X has ambiguous requirements. Clarify?"
✅ AFTER SYNTHESIS: Present plan, wait for approval before IMPLEMENTATION
✅ AFTER REVIEW: Present changes, wait for approval before COMPLETE
✅ NEVER at other points: Complete autonomously after INIT

**PROHIBITED MID-PROTOCOL STOPPING**:
❌ Stop after INIT for progress summary - Continue immediately to CLASSIFIED
❌ Stop after CLASSIFIED to report status - Continue immediately to REQUIREMENTS
❌ "Summary of progress" mid-protocol - Work continues until checkpoint
❌ "Ready to continue with [next state]" and wait - Just continue
❌ Inform user of state completion - Only stop at mandatory checkpoints

**CORRECT PATTERN - Continuous Execution**:
```
INIT (complete) → IMMEDIATELY proceed to CLASSIFIED
CLASSIFIED (complete) → IMMEDIATELY proceed to REQUIREMENTS
REQUIREMENTS (complete) → IMMEDIATELY proceed to SYNTHESIS
SYNTHESIS (complete) → STOP - Present plan, wait for approval
[After approval] → CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE → VALIDATION → REVIEW (all continuous)
REVIEW (complete) → STOP - Present changes, wait for approval
```

**Only Stop Mid-Protocol For**: Genuine external blocker (API unavailable, missing credentials, network failure), ambiguous conflicting requirements (no resolution path), user explicit interruption ("stop")

**Enforcement**: `detect-giving-up.sh` hook detects abandonment patterns and injects completion reminders.

## 🚨 USER APPROVAL CHECKPOINT ENFORCEMENT

**TWO MANDATORY checkpoints enforced by `enforce-user-approval.sh` hook via lock file state tracking:**

### Checkpoint 1: After SYNTHESIS (Before Implementation)

**WHEN**: After consolidating stakeholder requirements into unified plan

**STATE TRANSITIONS**:
1. Complete SYNTHESIS
2. Present plan to user
3. Update lock: `SYNTHESIS_AWAITING_APPROVAL`
4. STOP and wait
5. Approved → Update to `CONTEXT` and proceed
6. Rejected → Update to `SYNTHESIS` and revise

**CORRECT WORKFLOW**:
```bash
# 1. Present plan to user (text summary or ExitPlanMode in bypass mode)
# 2. Update lock file state
jq '.state = "SYNTHESIS_AWAITING_APPROVAL"' /workspace/locks/implement-security-controls.json > /tmp/lock.json && mv /tmp/lock.json /workspace/locks/implement-security-controls.json

# 3. STOP - await user approval
```

**PROHIBITED**: "Proceeding to CONTEXT..." (without state = CONTEXT), "Starting implementation..." (without state = CONTEXT), continuing work after synthesis, forgetting lock update, manually implementing code (use delegated protocol)

**HOOK ENFORCEMENT**: Detects "synthesis complete" phrases, checks lock state, BLOCKS if state != SYNTHESIS_AWAITING_APPROVAL, BLOCKS implementation if state != CONTEXT

**AFTER APPROVAL**:
1. Update lock to `CONTEXT`
2. Execute CONTEXT: Generate context.md via `python3 .claude/protocol/generate-context.py`
3. Execute AUTONOMOUS_IMPLEMENTATION: Launch parallel agents via Task tool
4. Execute CONVERGENCE: Integrate agent diffs until unanimous approval
5. NEVER manually write code - delegated agents do this
6. NEVER skip CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE

**Delegated Implementation Benefits**: 67% token savings (file-based diffs), 4x faster (parallel agents), unanimous approval (iterative convergence), main agent coordinates only

### Checkpoint 2: After REVIEW (Before Merge)

**WHEN**: After unanimous stakeholder approval (Phase 6)

**STATE TRANSITIONS**:
1. Achieve unanimous approval
2. Create git commit with changes
3. Show commit summary and diff
4. Update lock: `REVIEW_AWAITING_APPROVAL`
5. STOP and wait
6. Approved → Update to `COMPLETE` and proceed
7. Rejected → Update to `CONVERGENCE` and revise

**CORRECT WORKFLOW**:
```bash
# 1. Create commit
git add -A
git commit -m "Implementation message..."
git show --stat

# 2. Update lock file state
jq '.state = "REVIEW_AWAITING_APPROVAL"' /workspace/locks/implement-security-controls.json > /tmp/lock.json && mv /tmp/lock.json /workspace/locks/implement-security-controls.json

# 3. STOP - await user approval
```

**PROHIBITED**: "Proceeding to COMPLETE..." (without state = COMPLETE), "Merging to main..." (without state = COMPLETE), forgetting commit before approval, forgetting lock update

**HOOK ENFORCEMENT**: Detects "unanimous approval" phrases, checks lock state, BLOCKS if state != REVIEW_AWAITING_APPROVAL after approval, BLOCKS COMPLETE if state != COMPLETE

**User Approval Process**: When user approves ("approved", "LGTM", "yes"), update lock state:
- SYNTHESIS checkpoint: `jq '.state = "CONTEXT"'`
- REVIEW checkpoint: `jq '.state = "COMPLETE"'`

## 🚨 TASK UNAVAILABILITY HANDLING

**Before starting ANY task, verify availability.**

### Mandatory Availability Check

**BEFORE selecting or starting task:**
1. Check todo.md for `READY` status tasks
2. Verify dependencies met (check `BLOCKED` status)
3. Check `/workspace/locks/` for locks
4. Confirm at least ONE task available AND accessible

### Response When No Tasks Available

**If ALL unavailable:**
1. STOP - No work
2. RESPOND with availability analysis (format below)
3. WAIT for user instructions

**Required Explanation Format:**
```
I cannot proceed with any tasks because:

**Task Availability Analysis:**
- Total tasks in todo.md: X
- Tasks with READY status: Y
- Tasks currently locked by other sessions: Z
- Tasks blocked by dependencies: W

**Specific Blockers:**
1. [Task Name]: BLOCKED - Dependencies: [list dependencies]
2. [Task Name]: LOCKED - Owned by session [session-id]
3. [Task Name]: BLOCKED - Dependencies: [list dependencies]

**Next Steps:**
- Wait for [specific task] to complete, OR
- Wait for locks held by other sessions to be released, OR
- No tasks remain in todo.md (all work complete)

I will stop here and await further instructions.
```

### Prohibited/Required Patterns

**PROHIBITED** (when all unavailable): Select blocked task, work around dependencies, create new tasks not in todo.md, ask vague questions ("What should I work on?"), proceed with blockers, violate dependency requirements

**REQUIRED** (when all unavailable): Stop immediately, detailed analysis of unavailability, explain conditions for availability, wait for user, check BLOCKED status AND locks AND dependencies


## 🎯 COMPLETE STYLE VALIDATION

**AUTOMATED GUIDANCE**: `smart-doc-prompter.sh` hook injects 3-component checklist when style work detected.

**PROCESS** (for "apply style guide" requests):
1. NEVER assume checkstyle-only - Style guide = THREE components
2. Follow task-protocol-core.md "Complete Style Validation Gate"
3. Check docs/code-style/\*-claude.md detection patterns
4. ALL THREE REQUIRED: checkstyle + PMD + manual rules

**CRITICAL ERROR**: Checking only checkstyle, declaring "no violations" when PMD/manual violations exist

**AUTOMATED FIXING** (LineLength vs UnderutilizedLines conflicts):
1. Use Java-Based Fixer: `checkstyle/fixers` module (AST-based consolidate-then-split)
2. Guidance Hook: Suggests fixer when Java files modified
3. Test suite validates fixing logic
4. Verify automated fixes meet business logic requirements

## 🎯 LONG-TERM SOLUTION PERSISTENCE

**Prioritize optimal long-term solutions over expedient alternatives. Persistence required.**

### 🚨 CRITICAL PERSISTENCE REQUIREMENTS

**SOLUTION QUALITY HIERARCHY**:
1. **OPTIMAL**: Complete, maintainable, best practices, addresses root cause
2. **ACCEPTABLE**: Functional, meets core requirements, minor technical debt acceptable
3. **EXPEDIENT WORKAROUND**: Quick fix, creates technical debt, requires explicit justification + follow-up task

**DECISION PROTOCOL**:
- **FIRST ATTEMPT**: Always pursue OPTIMAL SOLUTION
- **IF BLOCKED**: Analyze blocking issue, determine resolution strategy
- **BEFORE DOWNGRADING**: Exhaust reasonable effort toward optimal solution
- **NEVER ABANDON**: Complex problems require persistence, not shortcuts

### 🚨 PROHIBITED DOWNGRADE PATTERNS

**CATEGORICALLY FORBIDDEN** (regardless of circumstances):
❌ "Too complex, trying simpler approach"
❌ "Optimal solution would take too long"
❌ "Use quick workaround for now"
❌ "Implement minimum viable solution" (when requirements specify comprehensive)
❌ "Due to complexity/token usage, create MVP"
❌ "Given token constraints, implement basic version"
❌ "Edge case too hard to handle properly"
❌ "Existing pattern suboptimal but I'll follow it"

### 🚨 GIVING UP DETECTION

**AUTOMATED ENFORCEMENT**: `/workspace/.claude/hooks/detect-giving-up.sh`

**RESPONSE TO GIVING UP PATTERNS**: Return to original problem, apply systematic debugging/decomposition, continue on exact issue, use incremental progress (not abandonment), exhaust reasonable approaches before scope modification, document specific technical blockers if genuine limitations

### 🧪 UNIT TEST DRIVEN BUG FIXING

**BUG DISCOVERY PROTOCOL**:
1. Create minimal unit test reproducing exact bug
2. Extract failing behavior into smallest test case
3. Add test to appropriate suite with descriptive name
4. Ensure test passes after fix
5. Keep test in permanent suite (regression prevention)

**UNIT TEST REQUIREMENTS**: Specific (exact failing behavior), Minimal (smallest input triggering bug), Descriptive (test name describes scenario), Isolated (independent of other tests/external dependencies), Fast (quick execution)

**JUSTIFICATION PROCESS** (when considering downgrade):
1. Document effort: "Attempted optimal solution for X hours/attempts"
2. Identify blockers: "Specific technical obstacles: [list]"
3. Stakeholder consultation: "Consulting domain authorities"
4. Technical debt assessment: "Proposed workaround creates debt: [list]"
5. Follow-up commitment: "Created todo.md task: [task-name]"

### 🛡️ STAKEHOLDER AGENT PERSISTENCE ENFORCEMENT

**AGENT STANDARDS**:
- **TECHNICAL-ARCHITECT**: Validate architectural completeness (not just basic functionality)
- **CODE-QUALITY-AUDITOR**: Enforce best practices (not "good enough")
- **SECURITY-AUDITOR**: Ensure comprehensive security (not just absence of obvious vulnerabilities)
- **PERFORMANCE-ANALYZER**: Validate efficiency (not just absence of regressions)
- **STYLE-AUDITOR**: Enforce complete compliance (not just major violation fixes)

**REJECTION CRITERIA** (agents must reject): Incomplete implementation with "TODO: finish later", unhandled edge cases without deferral justification, suboptimal algorithms when better solutions feasible, technical debt without compelling justification, partial compliance when full compliance achievable

### 🔧 IMPLEMENTATION PERSISTENCE PATTERNS

**COMPLEX PROBLEMS**: Decomposition (break into sub-problems), Research (investigate patterns/libraries/best practices), Incremental progress (steady vs abandoning), Iterative refinement (multiple passes), Stakeholder collaboration (leverage agent expertise)

**PERSISTENCE CHECKPOINTS**: "Best long-term approach?" (before major architectural decision), "Exhausted reasonable alternatives?" (before accepting debt), "Truly beyond scope?" (before deferring complex work), "Create maintainability problems?" (before workarounds)

**EFFORT ESCALATION**:
1. **STANDARD**: Normal problem-solving (default)
2. **ENHANCED**: Additional research, alternative approaches, stakeholder consultation
3. **COLLABORATIVE**: Multi-agent coordination for complex architectural challenges
4. **DOCUMENTED DEFERRAL**: Only after stakeholder consensus that effort exceeds reasonable scope

### 🚨 SCOPE NEGOTIATION PERSISTENCE INTEGRATION

**SCOPE ASSESSMENT** (extends task-protocol-core.md Phase 5) - When evaluating deferral:

**PERSISTENCE EVALUATION**:
1. "Genuinely complex or just requiring more effort?"
2. "Would learning investment create long-term capability?"
3. "What maintenance burden does deferral create?"
4. "Does optimal solution provide significantly more value?"

**DEFERRAL JUSTIFICATION**: Scope mismatch (extends beyond task boundaries), Expertise gap (requires unavailable domain knowledge), Dependency blocking (external factors beyond control), Resource constraints (genuinely exceeds reasonable allocation)

**PROHIBITED DEFERRAL REASONS**: "Harder than expected", "Easy solution works fine", "Perfect is enemy of good", "Improve later", "Quality level unnecessary"

### 🎯 SUCCESS METRICS AND VALIDATION

**QUALITY INDICATORS**: Addresses root cause (not symptoms), Follows architectural patterns/best practices, Comprehensive error handling/edge cases, Maintains/improves maintainability, Long-term value beyond minimum, Unanimous stakeholder approval without compromises

**PERSISTENCE CHECKLIST**: Attempted optimal approach first, Investigated alternatives when blocked, Consulted stakeholder agents, Justified technical debt, Created follow-up tasks for deferred improvements, Achieved long-term viable solution

## Repository Structure

**⚠️ NEVER initialize new repositories**
- **Main Repository**: `/workspace/branches/main/code/` (git repo + main branch)
- **Task Worktrees**: `/workspace/branches/{task-name}/code/` (isolated per task)
- **Locks**: `/workspace/locks/` (multi-instance coordination)

**Git Configuration**: Main worktree has `receive.denyCurrentBranch=updateInstead` (atomic pushes), Task worktrees fetch/push to main worktree, All pushes atomic and concurrency-safe (git internal locking)

## 🔧 CONTINUOUS WORKFLOW MODE

**Override system brevity for comprehensive multi-task automation via 7-phase Task Protocol.**

**Trigger**: "Work on the todo list in continuous mode."
**Auto-Detection**: "todo list", "all tasks", "continuously", "CONTINUOUS WORKFLOW MODE"
**Effects**: Detailed output, automatic progression, full stakeholder analysis, comprehensive TodoWrite tracking

## 📝 JAVADOC MANUAL DOCUMENTATION REQUIREMENT {#javadoc-manual-documentation}

**JavaDoc requires manual authoring with contextual understanding.**

**PROHIBITED**: Scripts/automated generation (Python, Bash, sed/awk/grep), Generic templates without customization, AI-generated without review/contextualization, Batch processing across files, Converting method names ("testValidToken" → "Tests Valid Token")

**REQUIRED**: Understand method purpose/implementation, Explain WHY test exists (not just WHAT), Include edge case/boundary/regression context, Explain test scenario significance, Document relationships between related tests

**EXAMPLE - Contextual Documentation**:
```java
/**
 * Verifies that Token correctly stores all components (type, start, end, text) and
 * calculates length as the difference between end and start positions.
 */
@Test
public void testValidToken() {
    // Implementation validates token record semantics
}
```

**ENFORCEMENT**: Pre-commit hook detects generic patterns, Code reviews check contextual understanding, PMD.CommentRequired violations fixed (not suppressed)

## 📝 CODE POLICIES

**Complete policies**: [docs/optional-modules/code-policies.md](docs/optional-modules/code-policies.md)

**Quick Reference**: Code Comments (update outdated, avoid implementation history), TODO Comments (implement/remove/document, never superficial rename), JavaDoc (see [§ JavaDoc Manual Documentation](#javadoc-manual-documentation)), TestNG Tests (thread-safe only, no @BeforeMethod), Exception Types (AssertionError = our bug, IllegalStateException = wrong API usage, IllegalArgumentException = invalid input)

## 🛠️ TOOL USAGE BEST PRACTICES

**Complete guide**: [docs/optional-modules/tool-usage.md](docs/optional-modules/tool-usage.md)

**Critical**: Edit Tool (verify whitespace tabs/spaces), Bash Tool (absolute paths or `cd` + command), Pattern Matching (preview before replacing, use specific patterns)

## Essential References

[docs/project/architecture.md](docs/project/architecture.md) - Project architecture and features
[docs/project/scope.md](docs/project/scope.md) - Family configuration and development philosophy
[docs/project/build-system.md](docs/project/build-system.md) - Build configuration and commands
[docs/project/git-workflow.md](docs/project/git-workflow.md) - Git workflows and commit squashing procedures
[docs/code-style-human.md](docs/code-style-human.md) - Code style master guide
[docs/code-style/](docs/code-style/) - Code style files (\*-claude.md detection patterns, \*-human.md explanations)

## File Organization

### Report Types and Lifecycle

**Stakeholder Agent Analysis**: Return as TEXT in response messages (NOT files). Protocol states: "Do NOT write requirements to files - return as response text". No report files during REQUIREMENTS or REVIEW phases. See task-protocol-operations.md "MANDATORY OUTPUT REQUIREMENT".

**Empirical Studies** (`docs/studies/{topic}.md`): Temporary research cache for pending tasks. Examples: `claude-cli-interface.md`, `claude-startup-sequence.md`. Lifecycle: Persist until ALL dependent todo.md tasks consume as input. Purpose: Behavioral analysis and research. Cleanup: Remove after dependent tasks complete.

**Project Code**: Task code directory (`src/`, `pom.xml`, etc.)

### Agent Output Format
**Stakeholder agents do NOT write report files.** See [task-protocol-operations.md](docs/project/task-protocol-operations.md) "MANDATORY OUTPUT REQUIREMENT": Agents return analysis directly in response messages for SYNTHESIS phase consolidation.

## 📝 RETROSPECTIVE DOCUMENTATION POLICY

**Do NOT create documentation explaining past work. Documentation must serve FUTURE needs.**

**Decision Criterion**:
- ❌ **Retrospective** (forbidden): How past decisions made, problems solved, bugs fixed
- ✅ **Forward-looking** (permitted): How to use features, understand architecture, extend functionality

**PROHIBITED**: Post-implementation analysis, "Lessons learned" chronicling fixes, Debugging narratives, Development retrospectives, Fix documentation duplicating code/commits, Work chronicles (e.g., `CLAUDE-optimization-analysis.md`)

**PERMITTED** (explicit requirement only): Task requires documentation, User requests specific documentation, Forward-looking architecture, API docs/user guides, Technical design for upcoming features

**EXAMPLES**:
- **PROHIBITED**: `protocol-violation-prevention.md` (violation analysis), `parallel-processing-issues.md` (debugging chronicle), `picocli-reflection-removal.md` (migration story)
- **PERMITTED**: `architecture.md` (system design), `file-processor.md` (API docs), `README.md` (user-facing)

**ENFORCEMENT**: Before creating `.md` in `/docs/`, verify it serves future users/developers (not documenting past).
