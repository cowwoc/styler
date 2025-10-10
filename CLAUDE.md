# Claude Code Configuration Guide

Styler Java Code Formatter project configuration and workflow guidance.

## 🚨 MANDATORY COMPLIANCE

**CRITICAL WORKFLOW**: Task Protocol ([core](docs/project/task-protocol-core.md) + [operations](docs/project/task-protocol-operations.md) + [delegated](docs/project/delegated-implementation-protocol.md)) - MANDATORY risk-based protocol selection - Apply appropriate workflow based on file risk classification.
**CRITICAL LOCK OWNERSHIP**: See [§ Lock Ownership & Task Recovery](#-lock-ownership--task-recovery) for complete lock file requirements and ownership rules.
**CRITICAL WORKTREE ISOLATION**: See [§ Worktree Isolation & Cleanup](#-worktree-isolation--cleanup) for complete worktree management requirements.
**CRITICAL GIT HISTORY VERIFICATION**: See [§ Git History Rewriting Verification](#-git-history-verification) for MANDATORY verification after ANY history rewriting operation.
**CRITICAL STYLE**: Complete style validation = checkstyle + PMD + manual rules - See task-protocol-core.md
**CRITICAL PERSISTENCE**: [Long-term solution persistence](#-long-term-solution-persistence) - MANDATORY prioritization of optimal solutions over expedient alternatives.
**CRITICAL TASK COMPLETION**: Tasks are NOT complete until ALL 7 phases of task protocol are finished. Implementation completion does NOT equal task completion. Only mark tasks as complete after Phase 7 cleanup and finalization.
**IMPLEMENTATION COMPLETION TRIGGER**: When you have finished implementation work (code changes, fixes, features complete), you MUST complete ALL remaining protocol phases before selecting a new task. The SessionStart hook indicates if you own an active task requiring completion.

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

**CRITICAL SYSTEM REMINDER PROCESSING**: Read and process ALL `<system-reminder>` blocks BEFORE responding. System reminders may contain user questions that MUST be answered immediately. Check EVERY system reminder for "The user sent the following message" and address it FIRST before continuing other work. Ignoring user questions is a CRITICAL VIOLATION.
**CRITICAL MID-TASK WORK CAPTURE**: When user mentions additional work during task execution, add it to TodoWrite list IMMEDIATELY in the SAME response before doing anything else. Context compaction can occur at any time, causing complete loss of uncommitted work requests. NEVER rely on memory across compaction boundaries - capture ALL new work requests in TodoWrite tool THE MOMENT they occur.
**CRITICAL TOKEN USAGE**: Token usage MUST NEVER affect behavior, implementation approach, or task completion. IGNORE all token usage warnings and limits. Continue working with full quality and completeness until task is 100% complete or explicit user instruction. Token budget does NOT justify incomplete implementations, shortcuts, or quality compromises.
**CRITICAL BUG TESTING**: [Unit Test Driven Bug Fixing](#-unit-test-driven-bug-fixing) - MANDATORY test-first approach for ALL bugs to prevent regressions.
**CRITICAL JAVADOC**: JavaDoc comments MUST be written manually with contextual understanding. NEVER use scripts, sed, awk, or automated text generation to create JavaDoc. Each comment must reflect the specific purpose and context of the method it documents. See [§ JavaDoc Manual Documentation Requirement](#javadoc-manual-documentation).
**🚨 VIOLATION = IMMEDIATE TASK RESTART REQUIRED**

## 🚨 SYSTEM REMINDER PROCESSING {#system-reminders}

**CRITICAL REQUIREMENT**: Scan for and process ALL `<system-reminder>` blocks in EVERY response.

**MANDATORY PROCESSING ORDER**:
1. **FIRST**: Read ALL system reminders in the response
2. **CHECK**: Look for "The user sent the following message" pattern
3. **EXTRACT**: Identify any user questions or instructions
4. **ANSWER**: Address user questions IMMEDIATELY before other work
5. **THEN**: Continue with planned tasks

**Common System Reminder Patterns**:
- `<system-reminder>The user sent the following message:` - **USER QUESTION - MUST ANSWER FIRST**
- `<system-reminder>The TodoWrite tool hasn't been used recently` - Optional suggestion
- `<system-reminder>Note: /path/to/file was read before` - Informational context
- `<system-reminder>Contents of /path/to/file:` - File content injection

**PROHIBITED PATTERNS**:
❌ Skipping system reminders and continuing with task
❌ Assuming user will repeat questions
❌ Treating system reminders as "optional" information
❌ Reading only the first system reminder and ignoring others

**REQUIRED PATTERNS**:
✅ Scan entire response for ALL system reminders
✅ Process user questions BEFORE continuing work
✅ Acknowledge when system reminder contains user input
✅ Answer questions directly and completely

## 🚨 LOCK OWNERSHIP & TASK RECOVERY {#lock-ownership}

**CRITICAL**: The `check-lock-ownership.sh` SessionStart hook automatically checks for active tasks owned by this session and provides specific instructions.

**Lock Ownership Rule**: ONLY work on tasks whose lock file contains YOUR session_id.

**🚨 LOCK FILE VERIFICATION REQUIREMENTS**:

1. **NEVER manually search for lock files** - The SessionStart hook performs this check automatically
2. **TRUST the hook output** - If it says "No Active Tasks", you have NO tasks regardless of other files
3. **ONLY `.json` files are valid** - Lock files MUST be `/workspace/locks/{task-name}.json`
4. **Invalid extensions are NEVER valid**:
   - ❌ `/workspace/locks/task-name.lock` - INVALID, will be ignored
   - ❌ `/workspace/locks/task-name.txt` - INVALID, will be ignored
   - ❌ Any extension other than `.json` - INVALID, will be ignored
5. **If you see your session_id in a non-.json file**: Delete it immediately, it's incorrect
6. **NEVER remove lock files unless you own them** - session_id must match
7. **If lock acquisition fails** - Select alternative task, do NOT delete the lock

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

**CRITICAL WORKTREE ISOLATION**: After creating worktree, IMMEDIATELY `cd` to worktree directory before any other operations.

**Required Pattern**:
```bash
git worktree add /workspace/branches/{task}/code -b {task} && cd /workspace/branches/{task}/code
```

**ALL subsequent work must occur inside worktree, NEVER in main branch.**

**Verification Before Proceeding**:
```bash
pwd | grep -q "/workspace/branches/{task}/code$" && echo "✅ In worktree" || echo "❌ ERROR: Not in worktree!"
```

### During Cleanup (Phase 7/8)

**CRITICAL WORKTREE CLEANUP**: Before removing worktree, MUST `cd` to main worktree first.

**Required Pattern**:
```bash
cd /workspace/branches/main/code && git worktree remove /workspace/branches/{task}/code
```

**NEVER remove a worktree while inside it** - shell loses working directory. This is MANDATORY in Phase 8 (CLEANUP).

## 🚨 GIT HISTORY REWRITING VERIFICATION {#git-history-verification}

**CRITICAL**: ANY git history rewriting operation MUST be followed by mandatory verification to ensure no commits or changes were dropped.

**History Rewriting Operations Include**:
- `git rebase` (interactive or non-interactive)
- `git rebase --onto`
- `git commit --amend`
- `git reset --hard` followed by new commits
- `git filter-branch`
- `git cherry-pick` with history modification
- Any operation that changes commit SHAs

**MANDATORY VERIFICATION PROCEDURE**:

After ANY history rewriting operation, you MUST execute ALL of the following verification steps:

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

**PROHIBITED PATTERNS**:
❌ Completing rebase without verification
❌ Assuming "no conflicts" means "no data loss"
❌ Trusting git rebase output without manual inspection
❌ Skipping verification because operation "seemed simple"
❌ Verifying only commit count without checking content

**REQUIRED PATTERNS**:
✅ Save original SHA before rewriting: `ORIG_SHA=$(git rev-parse HEAD)`
✅ Compare before/after states explicitly
✅ Check reflog for unexpected operations
✅ Run build after history modifications
✅ Document verification results before proceeding

**ENFORCEMENT**:
- Any history rewriting without verification is a CRITICAL VIOLATION
- Failed verification requires immediate rollback to pre-rewriting state
- If verification reveals data loss, recover via reflog: `git reset --hard <original-sha>`

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

**CRITICAL PROTOCOL VIOLATION PREVENTION**: Phase 6 requires UNANIMOUS stakeholder approval

**MANDATORY DECISION LOGIC**:
- ALL agents must respond with "FINAL DECISION: ✅ APPROVED"
- ANY agent with "❌ REJECTED" → MANDATORY Phase 5 execution + Phase 6 re-run
- NO human override permitted - agent decisions are ATOMIC and BINDING
- NO subjective "MVP scope" or "enhancement-level" assessments allowed

## 🚨 AUTONOMOUS TASK COMPLETION REQUIREMENT

**CRITICAL**: Once you begin a task (execute INIT state), you MUST complete ALL protocol states (0-8) autonomously, WITH TWO MANDATORY USER APPROVAL CHECKPOINTS.

**MANDATORY SINGLE-SESSION COMPLETION**:
- Task execution occurs in ONE uninterrupted session
- **EXPECTED USER APPROVAL CHECKPOINTS** (these are NOT violations):
  1. **After SYNTHESIS**: Present implementation plan via ExitPlanMode, wait for user approval
  2. **After REVIEW**: Present completed changes, wait for user approval to finalize
- NO OTHER HANDOFFS to user mid-protocol
- Complete all other states autonomously

**When to Ask User**:
✅ **BEFORE** starting task: "Task X has ambiguous requirements. Clarify before I begin?"
✅ **AFTER SYNTHESIS**: Present plan via ExitPlanMode, wait for approval before IMPLEMENTATION
✅ **AFTER REVIEW**: Present changes, wait for approval before COMPLETE
✅ **NEVER** at other points: Complete other states autonomously once INIT begins

**🚨 PROHIBITED MID-PROTOCOL STOPPING PATTERNS**:
❌ **NEVER** stop after INIT state to provide progress summary - Continue immediately to CLASSIFIED
❌ **NEVER** stop after CLASSIFIED state to report status - Continue immediately to REQUIREMENTS
❌ **NEVER** provide "Summary of progress" mid-protocol - Work continues until checkpoint
❌ **NEVER** say "Ready to continue with [next state]" and wait - Just continue to next state
❌ **NEVER** inform user of current state completion - Only stop at mandatory checkpoints

**CORRECT PATTERN - Continuous Execution**:
```
INIT (complete) → IMMEDIATELY proceed to CLASSIFIED
CLASSIFIED (complete) → IMMEDIATELY proceed to REQUIREMENTS
REQUIREMENTS (complete) → IMMEDIATELY proceed to SYNTHESIS
SYNTHESIS (complete) → STOP - Present plan, wait for approval
[After approval] → CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE → VALIDATION → REVIEW (all continuous)
REVIEW (complete) → STOP - Present changes, wait for approval
```

**Only Stop Mid-Protocol If**:
1. **Genuine External Blocker**: API unavailable, missing credentials, network failure
2. **Ambiguous Conflicting Requirements**: No resolution path exists
3. **User Explicit Interruption**: User says "stop"

**Enforcement**: The `detect-giving-up.sh` hook detects mid-protocol abandonment patterns and injects completion reminders.

## 🚨 USER APPROVAL CHECKPOINT ENFORCEMENT

**CRITICAL**: There are TWO MANDATORY user approval checkpoints enforced by `enforce-user-approval.sh` hook using **lock file state tracking**:

### Checkpoint 1: After SYNTHESIS (Before Implementation)

**WHEN**: After consolidating stakeholder requirements into unified implementation plan

**MANDATORY STATE TRANSITIONS**:
1. Complete SYNTHESIS (consolidate requirements)
2. Present implementation plan to user
3. **Update lock file state to `SYNTHESIS_AWAITING_APPROVAL`**
4. **STOP and wait for user response**
5. User approves → Update state to `CONTEXT` and proceed
6. User rejects → Update state back to `SYNTHESIS` and revise

**CORRECT WORKFLOW**:
```bash
# 1. Present plan to user (text summary or ExitPlanMode in bypass mode)
# 2. Update lock file state
jq '.state = "SYNTHESIS_AWAITING_APPROVAL"' /workspace/locks/implement-security-controls.json > /tmp/lock.json && mv /tmp/lock.json /workspace/locks/implement-security-controls.json

# 3. STOP - await user approval
```

**PROHIBITED PATTERNS**:
❌ "Proceeding to CONTEXT phase..." (without state = CONTEXT)
❌ "Starting implementation..." (without state = CONTEXT)
❌ Continuing any work after completing synthesis
❌ Forgetting to update lock file state
❌ **CRITICAL**: Manually implementing code yourself instead of using delegated protocol

**HOOK ENFORCEMENT**:
- Detects "synthesis complete" phrases
- Checks lock file state
- BLOCKS if state != SYNTHESIS_AWAITING_APPROVAL
- BLOCKS proceeding to implementation if state != CONTEXT

**AFTER USER APPROVES - MANDATORY NEXT STEPS**:
1. ✅ User says "approved" or "I approve" → Update lock to `CONTEXT`
2. ✅ Execute CONTEXT state: Generate context.md via `python3 .claude/protocol/generate-context.py`
3. ✅ Execute AUTONOMOUS_IMPLEMENTATION: Launch parallel implementation agents via Task tool
4. ✅ Execute CONVERGENCE: Agents implement code, you integrate diffs until unanimous approval
5. ❌ **NEVER**: Manually write code yourself - delegated implementation agents do this
6. ❌ **NEVER**: Skip CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE states

**Why Delegated Implementation**:
- 67% token savings through file-based diffs
- Parallel agent execution (4x faster)
- Unanimous approval through iterative convergence
- Main agent NEVER writes code - only coordinates integration

### Checkpoint 2: After REVIEW (Before Merge)

**WHEN**: After achieving unanimous stakeholder approval in Phase 6

**MANDATORY STATE TRANSITIONS**:
1. Achieve unanimous stakeholder approval
2. Create git commit with all implementation changes
3. Show commit summary and diff to user
4. **Update lock file state to `REVIEW_AWAITING_APPROVAL`**
5. **STOP and wait for user response**
6. User approves → Update state to `COMPLETE` and proceed
7. User rejects → Update state back to `CONVERGENCE` and revise

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

**PROHIBITED PATTERNS**:
❌ "Proceeding to COMPLETE..." (without state = COMPLETE)
❌ "Merging to main..." (without state = COMPLETE)
❌ Forgetting to create commit before asking for approval
❌ Forgetting to update lock file state

**HOOK ENFORCEMENT**:
- Detects "unanimous approval" phrases
- Checks lock file state
- BLOCKS if state != REVIEW_AWAITING_APPROVAL after approval
- BLOCKS proceeding to COMPLETE if state != COMPLETE

**User Approval Process**:
When user responds with approval keywords ("approved", "LGTM", "yes"), they should also:
- Update lock state: `jq '.state = "CONTEXT"'` (for SYNTHESIS checkpoint)
- Update lock state: `jq '.state = "COMPLETE"'` (for REVIEW checkpoint)

## 🚨 TASK UNAVAILABILITY HANDLING

**CRITICAL**: When user requests "work on the next task" or similar, you MUST verify task availability before attempting to start any work.

### Mandatory Availability Check

**BEFORE attempting to select or start ANY task:**
1. Check todo.md for available tasks with `READY` status
2. Verify task dependencies are met (check for `BLOCKED` status)
3. Check `/workspace/locks/` for existing locks on available tasks
4. Confirm at least ONE task is available AND accessible

### Required Response When No Tasks Available

**If ALL tasks are unavailable, you MUST:**
1. **STOP** - Do not attempt any work
2. **RESPOND** with task availability analysis in required format below
3. **WAIT** for user instructions to resolve blockers

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

### Prohibited Patterns

**NEVER do any of the following when all tasks are unavailable:**
❌ Select a blocked task and attempt to start work
❌ Try to work around missing dependencies
❌ Attempt to create new tasks not in todo.md
❌ Ask user vague questions like "What should I work on?"
❌ Proceed with any work when clear blockers exist
❌ Suggest working on tasks that violate dependency requirements

### Required Patterns

**When all tasks are unavailable:**
✅ Stop immediately after determining no tasks available
✅ Provide detailed analysis of why each task is unavailable
✅ Explain specific conditions needed for tasks to become available
✅ Wait for user to resolve blockers or provide new instructions
✅ Check both BLOCKED status AND lock files AND dependencies


## 🎯 COMPLETE STYLE VALIDATION

**AUTOMATED GUIDANCE**: The `smart-doc-prompter.sh` hook automatically injects the 3-component checklist when style work is detected.

**MANDATORY PROCESS**: When user requests "apply style guide" or similar:

1. **NEVER assume checkstyle-only** - Style guide consists of THREE components
2. **FOLLOW PROTOCOL**: task-protocol-core.md "Complete Style Validation Gate" pattern
3. **MANUAL VERIFICATION**: Check docs/code-style/\*-claude.md detection patterns
4. **ALL THREE REQUIRED**: checkstyle + PMD + manual rules must ALL pass

**CRITICAL ERROR PATTERN**: Checking only checkstyle and declaring "no violations found" when PMD/manual violations exist

**AUTOMATED FIXING INTEGRATION**: When LineLength vs UnderutilizedLines conflicts are detected:
1. **Use Java-Based Fixer**: `checkstyle/fixers` module implements AST-based consolidate-then-split strategy
2. **Guidance Hook**: Automatically suggests fixer when Java files are modified
3. **Comprehensive Testing**: Test suite validates fixing logic before application
4. **Manual Verification**: Always verify automated fixes meet business logic requirements

## 🎯 LONG-TERM SOLUTION PERSISTENCE

**MANDATORY PRINCIPLE**: Prioritize optimal long-term solutions over expedient alternatives. Persistence and thorough problem-solving are REQUIRED.

### 🚨 CRITICAL PERSISTENCE REQUIREMENTS

**SOLUTION QUALITY HIERARCHY**:
1. **OPTIMAL SOLUTION**: Complete, maintainable, follows best practices, addresses root cause
2. **ACCEPTABLE SOLUTION**: Functional, meets core requirements, minor technical debt acceptable
3. **EXPEDIENT WORKAROUND**: Quick fix, creates technical debt, only acceptable with explicit justification and follow-up task

**MANDATORY DECISION PROTOCOL**:
- **FIRST ATTEMPT**: Always pursue the OPTIMAL SOLUTION approach
- **IF BLOCKED**: Analyze the blocking issue and determine resolution strategy
- **BEFORE DOWNGRADING**: Must exhaust reasonable effort toward optimal solution
- **NEVER ABANDON**: Complex problems require persistence, not shortcuts

### 🚨 PROHIBITED DOWNGRADE PATTERNS

**ANTI-PATTERNS - CATEGORICALLY FORBIDDEN**:
These statements are PROHIBITED regardless of circumstances or justification:
❌ "This is too complex, let me try a simpler approach"
❌ "The optimal solution would take too long"
❌ "Let's use a quick workaround for now"
❌ "I'll implement the minimum viable solution" (when requirements specify comprehensive solution)
❌ "Due to complexity and token usage, I'll create a solid MVP implementation"
❌ "Given token constraints, I'll implement a basic version"
❌ "This edge case is too hard to handle properly"
❌ "The existing pattern is suboptimal but I'll follow it"

### 🚨 GIVING UP DETECTION PATTERNS

**AUTOMATED ENFORCEMENT**: Runtime detection via `/workspace/.claude/hooks/detect-giving-up.sh`

**MANDATORY RESPONSE TO GIVING UP PATTERNS**:
✅ IMMEDIATELY return to the original technical problem
✅ Apply systematic debugging and decomposition approach
✅ Continue working on the exact issue that triggered the pattern
✅ Use incremental progress rather than abandoning the work
✅ Exhaust all reasonable technical approaches before any scope modification
✅ Document specific technical blockers if genuine limitations exist

### 🧪 UNIT TEST DRIVEN BUG FIXING

**MANDATORY PROCESS**: When encountering any bug during development:

**BUG DISCOVERY PROTOCOL**:
1. **IMMEDIATE UNIT TEST**: Create a minimal unit test that reproduces the exact bug
2. **ISOLATION**: Extract the failing behavior into the smallest possible test case
3. **DOCUMENTATION**: Add the test to appropriate test suite with descriptive name
4. **FIX VALIDATION**: Ensure the unit test passes after implementing the fix
5. **REGRESSION PREVENTION**: Keep the test in the permanent test suite

**UNIT TEST REQUIREMENTS**:
- **Specific**: Target the exact failing behavior, not general functionality
- **Minimal**: Use the smallest possible input that triggers the bug
- **Descriptive**: Test method name clearly describes the bug scenario
- **Isolated**: Independent of other tests and external dependencies
- **Fast**: Execute quickly to enable frequent testing

**REQUIRED JUSTIFICATION PROCESS** (when considering downgrade):
1. **DOCUMENT EFFORT**: "Attempted optimal solution for X hours/attempts"
2. **IDENTIFY BLOCKERS**: "Specific technical obstacles: [list]"
3. **STAKEHOLDER CONSULTATION**: "Consulting domain authorities for guidance"
4. **TECHNICAL DEBT ASSESSMENT**: "Proposed workaround creates debt in areas: [list]"
5. **FOLLOW-UP COMMITMENT**: "Created todo.md task for proper solution: [task-name]"

### 🛡️ STAKEHOLDER AGENT PERSISTENCE ENFORCEMENT

**AGENT DECISION STANDARDS**:
- **TECHNICAL-ARCHITECT**: Must validate architectural completeness, not just basic functionality
- **CODE-QUALITY-AUDITOR**: Must enforce best practices, not accept "good enough" code
- **SECURITY-AUDITOR**: Must ensure comprehensive security, not just absence of obvious vulnerabilities
- **PERFORMANCE-ANALYZER**: Must validate efficiency, not just absence of performance regressions
- **STYLE-AUDITOR**: Must enforce complete style compliance, not just major violation fixes

**MANDATORY REJECTION CRITERIA** (agents must reject if present):
❌ Incomplete implementation with "TODO: finish later" comments
❌ Known edge cases left unhandled without explicit deferral justification
❌ Suboptimal algorithms when better solutions are feasible
❌ Technical debt introduction without compelling business justification
❌ Partial compliance with requirements when full compliance is achievable

### 🔧 IMPLEMENTATION PERSISTENCE PATTERNS

**WHEN ENCOUNTERING COMPLEX PROBLEMS**:
1. **DECOMPOSITION**: Break complex problems into manageable sub-problems
2. **RESEARCH**: Investigate existing patterns, libraries, and best practices
3. **INCREMENTAL PROGRESS**: Make steady progress rather than abandoning for easier alternatives
4. **ITERATIVE REFINEMENT**: Improve solution quality through multiple passes
5. **STAKEHOLDER COLLABORATION**: Leverage agent expertise for guidance and validation

**PERSISTENCE CHECKPOINTS**:
- Before every major architectural decision: "Is this the best long-term approach?"
- Before accepting technical debt: "Have I exhausted reasonable alternatives?"
- Before deferring complex work: "Is this truly beyond current task scope?"
- Before implementing workarounds: "Will this create maintainability problems?"

**EFFORT ESCALATION PROTOCOL**:
1. **STANDARD EFFORT**: Normal problem-solving approach (default)
2. **ENHANCED EFFORT**: Additional research, alternative approaches, stakeholder consultation
3. **COLLABORATIVE EFFORT**: Multi-agent coordination for complex architectural challenges
4. **DOCUMENTED DEFERRAL**: Only after stakeholder consensus that effort exceeds reasonable scope

### 🚨 SCOPE NEGOTIATION PERSISTENCE INTEGRATION

**ENHANCED SCOPE ASSESSMENT** (extends task-protocol-core.md Phase 5):
When evaluating whether to defer work via scope negotiation:

**MANDATORY PERSISTENCE EVALUATION**:
1. **COMPLEXITY ANALYSIS**: "Is this genuinely complex or just requiring more effort?"
2. **LEARNING CURVE ASSESSMENT**: "Would investment in learning create long-term capability?"
3. **TECHNICAL DEBT COST**: "What maintenance burden does deferral create?"
4. **STAKEHOLDER VALUE**: "Does optimal solution provide significantly more value?"

**DEFERRAL JUSTIFICATION REQUIREMENTS**:
- **SCOPE MISMATCH**: Work genuinely extends beyond original task boundaries
- **EXPERTISE GAP**: Requires domain knowledge not available to current session
- **DEPENDENCY BLOCKING**: Blocked by external factors beyond current control
- **RESOURCE CONSTRAINTS**: Genuinely exceeds reasonable time/effort allocation

**PROHIBITED DEFERRAL REASONS**:
❌ "This is harder than I expected"
❌ "The easy solution works fine"
❌ "Perfect is the enemy of good"
❌ "We can improve this later"
❌ "This level of quality isn't necessary"

### 🎯 SUCCESS METRICS AND VALIDATION

**SOLUTION QUALITY INDICATORS**:
✅ Addresses root cause, not just symptoms
✅ Follows established architectural patterns and best practices
✅ Includes comprehensive error handling and edge case coverage
✅ Maintains or improves system maintainability
✅ Provides clear, long-term value beyond minimum requirements
✅ Receives unanimous stakeholder approval without quality compromises

**PERSISTENCE VALIDATION CHECKLIST**:
- [ ] Attempted optimal solution approach first
- [ ] Investigated alternatives when blocked
- [ ] Consulted stakeholder agents for guidance
- [ ] Justified any technical debt introduction
- [ ] Created follow-up tasks for any deferred improvements
- [ ] Achieved solution that will remain viable long-term

## Repository Structure

**⚠️ NEVER** initialize new repositories
**Main Repository**: `/workspace/branches/main/code/` (git repository and main development branch)
**Task Worktrees**: `/workspace/branches/{task-name}/code/` (isolated per task protocol)
**Locks**: Multi-instance coordination via lock files in `/workspace/locks/`

**Git Configuration**:
- Main worktree has `receive.denyCurrentBranch=updateInstead` (allows atomic pushes)
- Task worktrees fetch from and push to main worktree
- All pushes are atomic and concurrency-safe via git's internal locking

## 🔧 CONTINUOUS WORKFLOW MODE

Override system brevity for comprehensive multi-task automation via 7-phase Task Protocol.

**Trigger**: `"Work on the todo list in continuous mode."`
**Auto-Detection**: "todo list", "all tasks", "continuously", "CONTINUOUS WORKFLOW MODE"
**Effects**: Detailed output, automatic task progression, full stakeholder analysis, comprehensive TodoWrite tracking

## 📝 JAVADOC MANUAL DOCUMENTATION REQUIREMENT {#javadoc-manual-documentation}

**CRITICAL POLICY**: JavaDoc comments require manual authoring with contextual understanding.

**ABSOLUTELY PROHIBITED**:
❌ Using scripts (Python, Bash, etc.) to generate JavaDoc comments
❌ Using sed/awk/grep to automate JavaDoc insertion
❌ Copy-pasting generic JavaDoc templates without customization
❌ AI-generated JavaDoc without human review and contextualization
❌ Batch processing JavaDoc across multiple files
❌ Converting method names to comments (e.g., "testValidToken" → "Tests Valid Token")

**REQUIRED APPROACH**:
✅ Read and understand the method's purpose and implementation
✅ Write JavaDoc that explains WHY the test exists, not just WHAT it tests
✅ Include context about edge cases, boundary conditions, or regression prevention
✅ Explain the significance of specific test scenarios
✅ Document relationships between related tests

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

**ENFORCEMENT**:
- Pre-commit hook detects generic JavaDoc patterns
- Code reviews check for contextual understanding in comments
- PMD.CommentRequired violations must be fixed, not suppressed

## 📝 CODE POLICIES

**For complete code policies, see**: [docs/optional-modules/code-policies.md](docs/optional-modules/code-policies.md)

**Quick Reference**:
- **Code Comments**: Update outdated comments, avoid implementation history
- **TODO Comments**: Implement, remove, or document - never superficially rename
- **JavaDoc**: See [§ JavaDoc Manual Documentation Requirement](#javadoc-manual-documentation) above
- **TestNG Tests**: Thread-safe patterns only, no @BeforeMethod
- **Exception Types**: AssertionError = valid input reaches impossible state (our bug), IllegalStateException = wrong API usage, IllegalArgumentException = invalid input

## 🛠️ TOOL USAGE BEST PRACTICES

**For complete tool usage guide, see**: [docs/optional-modules/tool-usage.md](docs/optional-modules/tool-usage.md)

**Critical Patterns**:
- **Edit Tool**: Verify whitespace before editing (tabs vs spaces)
- **Bash Tool**: Use absolute paths or combine `cd` with command
- **Pattern Matching**: Preview before replacing, use specific patterns

## Essential References

[docs/project/architecture.md](docs/project/architecture.md) - Project architecture and features
[docs/project/scope.md](docs/project/scope.md) - Family configuration and development philosophy
[docs/project/build-system.md](docs/project/build-system.md) - Build configuration and commands
[docs/project/git-workflow.md](docs/project/git-workflow.md) - Git workflows and commit squashing procedures
[docs/code-style-human.md](docs/code-style-human.md) - Code style master guide
[docs/code-style/](docs/code-style/) - Code style files (\*-claude.md detection patterns, \*-human.md explanations)

## File Organization

### Report Types and Lifecycle

**Stakeholder Agent Analysis**:
- Agents return analysis as TEXT in their response messages (NOT files)
- Protocol explicitly states: "Do NOT write requirements to files - return analysis as response text"
- **No report files created** during REQUIREMENTS or REVIEW phases
- See task-protocol-operations.md "MANDATORY OUTPUT REQUIREMENT" for details

**Empirical Studies** (`docs/studies/{topic}.md`):
- Temporary research cache for pending implementation tasks
- Examples: `docs/studies/claude-cli-interface.md`, `docs/studies/claude-startup-sequence.md`
- **Lifecycle**: Persist until ALL dependent todo.md tasks consume them as input
- **Purpose**: Behavioral analysis and research studies based on empirical testing
- **Cleanup Rule**: Remove after all dependent tasks complete implementation

**Project Code**: Task code directory (`src/`, `pom.xml`, etc.)

### Agent Output Format
**IMPORTANT**: Stakeholder agents do NOT write report files. See [task-protocol-operations.md](docs/project/task-protocol-operations.md) "MANDATORY OUTPUT REQUIREMENT" section:

> "Provide complete {agent_domain} requirements analysis IN YOUR RESPONSE.
> **Do NOT write requirements to files** - return analysis as response text."

Agents return their analysis directly in response messages for consolidation during SYNTHESIS phase.

## 📝 RETROSPECTIVE DOCUMENTATION POLICY

**CRITICAL**: Do NOT create documentation that explains past work. Documentation must serve FUTURE needs.

**Decision Criterion**:
- ❌ **Retrospective** (forbidden): Explains how past decisions were made, problems were solved, or bugs were fixed
- ✅ **Forward-looking** (permitted): Explains how to use features, understand architecture, or extend functionality

**PROHIBITED DOCUMENTATION PATTERNS**:
❌ Post-implementation analysis reports (e.g., `protocol-violation-prevention.md`)
❌ "Lessons learned" documents chronicling what went wrong and how it was fixed
❌ Debugging chronicles or problem-solving narratives
❌ Development process retrospectives or meta-documentation
❌ Fix documentation that duplicates information already in code/commits
❌ Analysis documents chronicling work performed (e.g., `CLAUDE-optimization-analysis.md`)

**PERMITTED DOCUMENTATION** (only when explicitly required):
✅ Task explicitly requires documentation creation
✅ User explicitly requests specific documentation
✅ Forward-looking architecture documentation
✅ API documentation and user guides
✅ Technical design documents for upcoming features

**EXAMPLES**:

**PROHIBITED**:
```
docs/project/protocol-violation-prevention.md - "Analysis of violations and fixes"
docs/debugging/parallel-processing-issues.md - "How we debugged concurrency"
docs/lessons/picocli-reflection-removal.md - "Story of migrating to programmatic API"
```

**PERMITTED** (with explicit requirement):
```
docs/project/architecture.md - Forward-looking system design
docs/api/file-processor.md - API documentation for users
README.md - User-facing project documentation
```

**ENFORCEMENT**: Before creating any `.md` file in `/docs/`, verify it serves future users/developers rather than documenting the past.
