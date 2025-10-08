# Claude Code Configuration Guide

Styler Java Code Formatter project configuration and workflow guidance.

## 🚨 MANDATORY COMPLIANCE

**CRITICAL WORKFLOW**: [docs/project/task-protocol.md](docs/project/task-protocol.md) - MANDATORY risk-based protocol selection - Apply appropriate workflow based on file risk classification.
**CRITICAL LOCK OWNERSHIP**: See [§ Lock Ownership & Task Recovery](#-lock-ownership--task-recovery) for complete lock file requirements and ownership rules.
**CRITICAL WORKTREE ISOLATION**: See [§ Worktree Isolation & Cleanup](#-worktree-isolation--cleanup) for complete worktree management requirements.
**CRITICAL STYLE**: Complete style validation = checkstyle + PMD + manual rules - See task-protocol.md
**CRITICAL PERSISTENCE**: [Long-term solution persistence](#-long-term-solution-persistence) - MANDATORY prioritization of optimal solutions over expedient alternatives.
**CRITICAL TASK COMPLETION**: Tasks are NOT complete until ALL 7 phases of task protocol are finished. Implementation completion does NOT equal task completion. Only mark tasks as complete after Phase 7 cleanup and finalization.
**IMPLEMENTATION COMPLETION TRIGGER**: When you have finished implementation work (code changes, fixes, features complete), you MUST complete ALL remaining protocol phases before selecting a new task. The SessionStart hook indicates if you own an active task requiring completion.

**Prohibited Pattern**: ❌ "Implementation is done, let me look for what to work on next" (while protocol incomplete)
**Required Pattern**: ✅ "Implementation is done. Continuing with Phase 6 reviews and Phase 7 cleanup."

**PHASE COMPLETION VERIFICATION**: Before declaring ANY phase complete, you MUST:
1. **READ**: Execute `grep -A 20 "^## State [N]:" docs/project/task-protocol.md` to read ACTUAL phase requirements
2. **CHECKLIST**: Create explicit checklist of all phase requirements from task-protocol.md
3. **VERIFY**: Confirm EACH requirement is met with evidence (command output, file checks, etc.)
4. **DECLARE**: Only after ALL requirements verified, declare phase complete

**Prohibited Pattern**: ❌ "Phase N is complete" (based on assumption)
**Required Pattern**: ✅ "Reading Phase N requirements... [shows grep output]... Requirements: [list from doc]... Executing: [commands with output]... Verified: [checklist with evidence]... Phase N complete"

**Example:**
❌ BAD: "Phase 7 complete! I committed changes and marked task as done in todo.md"
✅ GOOD: "Reading Phase 7 requirements... Requirements: commit ALL changes (code + todo.md removal + changelog.md addition) in single atomic commit, merge to main, remove worktree, release lock... Executing: [shows single commit with all changes]... Verified: [checklist with evidence]... Phase 7 complete"

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

**CRITICAL MID-TASK WORK CAPTURE**: When user mentions additional work during task execution, IMMEDIATELY add it to TodoWrite list to prevent loss due to context compaction. NEVER rely on memory across compaction boundaries - capture ALL new work requests in TodoWrite tool as they occur.
**CRITICAL TOKEN USAGE**: Token usage MUST NEVER affect behavior, implementation approach, or task completion. IGNORE all token usage warnings and limits. Continue working with full quality and completeness until task is 100% complete or explicit user instruction. Token budget does NOT justify incomplete implementations, shortcuts, or quality compromises.
**CRITICAL BUG TESTING**: [Unit Test Driven Bug Fixing](#-unit-test-driven-bug-fixing) - MANDATORY test-first approach for ALL bugs to prevent regressions.
**🚨 VIOLATION = IMMEDIATE TASK RESTART REQUIRED**

## 🚨 LOCK OWNERSHIP & TASK RECOVERY {#lock-ownership}

**CRITICAL**: After context compaction, the `check-lock-ownership.sh` SessionStart hook automatically checks for active tasks owned by this session and provides specific instructions.

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

**CRITICAL WORKTREE ISOLATION**: After creating worktree, IMMEDIATELY `cd` to worktree directory BEFORE any other operations.

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

**CRITICAL WORKTREE CLEANUP**: BEFORE removing worktree, MUST `cd` to main worktree first.

**Required Pattern**:
```bash
cd /workspace/branches/main/code && git worktree remove /workspace/branches/{task}/code
```

**NEVER remove a worktree while inside it** - shell loses working directory. This is MANDATORY in Phase 8 (CLEANUP).

**Prohibited Pattern**: ❌ Being inside `/workspace/branches/{task}/code` and running `git worktree remove /workspace/branches/{task}/code`
**Required Pattern**: ✅ Change to main worktree first, then remove task worktree

## 🚨 TASK PROTOCOL SUMMARY {#task-protocol}

**Full Protocol Details**: See [task-protocol.md](docs/project/task-protocol.md) for complete state machine and transition requirements.

**State Machine**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → COMPLETE → CLEANUP

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

**Post-Compaction Note**: This summary plus lock ownership and worktree isolation rules above are sufficient for basic protocol compliance when task-protocol.md is not accessible.

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

**PROHIBITED PATTERNS**:
❌ "Considering the MVP nature, I'll proceed despite rejections"
❌ "Privacy issues are enhancement-level, not blocking"
❌ "Since critical security is fixed, I'll finalize the task"

**REQUIRED PATTERN**:
✅ "Agent X returned ❌ REJECTED, executing Phase 5 resolution cycle"
✅ "Re-running Phase 6 after addressing all stakeholder concerns"
✅ "Continuing until ALL agents return ✅ APPROVED"

## 🚨 AUTONOMOUS TASK COMPLETION REQUIREMENT

**CRITICAL**: Once you begin a task (execute INIT state), you MUST complete ALL protocol states (0-8) autonomously, WITH TWO MANDATORY USER APPROVAL CHECKPOINTS.

**MANDATORY SINGLE-SESSION COMPLETION**:
- Task execution occurs in ONE uninterrupted session
- **EXPECTED USER APPROVAL CHECKPOINTS** (these are NOT violations):
  1. **After SYNTHESIS**: Present implementation plan via ExitPlanMode, wait for user approval
  2. **After REVIEW**: Present completed changes, wait for user approval to finalize
- NO OTHER HANDOFFS to user mid-protocol
- Complete all other states autonomously

**Prohibited Mid-Task Questions** (other than the two mandatory checkpoints):
❌ "Would you like me to continue with implementation?" (after user approved plan)
❌ "This will take 2-3 days, should I proceed?" (ask before INIT, not during)
❌ "Select a different task or continue this one?" (not during execution)
❌ "Requires extended work session - continue?" (not during execution)

**When to Ask User**:
✅ **BEFORE** starting task: "Task X has ambiguous requirements. Clarify before I begin?"
✅ **AFTER SYNTHESIS**: Present plan via ExitPlanMode, wait for approval before IMPLEMENTATION
✅ **AFTER REVIEW**: Present changes, wait for approval before COMPLETE
✅ **NEVER** at other points: Complete other states autonomously once INIT begins

**Time Estimates Are NOT Blockers**:
- "2-3 days" = **effort estimation**, NOT permission gate
- You MUST complete implementation regardless of estimated duration
- Token budget NEVER justifies incomplete work (per CRITICAL TOKEN USAGE above)
- Complexity NEVER justifies asking permission mid-task
- "Multi-day effort" is NORMAL for implementation tasks

**Only Stop Mid-Protocol If**:
1. **Genuine External Blocker**: API unavailable, missing credentials, network failure
2. **Ambiguous Conflicting Requirements**: No resolution path exists between stakeholder requirements
3. **User Explicit Interruption**: User says "stop" or modifies todo.md mid-execution

**NOT Legitimate Stopping Reasons**:
❌ Task is complex
❌ Task takes time
❌ Token usage high
❌ "This might be too much work"
❌ "Should I ask the user first?"

**Enforcement**: The `detect-giving-up.sh` hook detects mid-protocol abandonment patterns and injects completion reminders.

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
1. **STOP immediately** - Do NOT attempt to start any work
2. **EXPLAIN clearly** why no tasks are available
3. **PROVIDE specifics** about what's blocking progress

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

**ALWAYS do the following when all tasks are unavailable:**
✅ Stop immediately after determining no tasks available
✅ Provide detailed analysis of why each task is unavailable
✅ Explain specific conditions needed for tasks to become available
✅ Wait for user to resolve blockers or provide new instructions
✅ Check both BLOCKED status AND lock files AND dependencies

### Example Scenarios

**Scenario 1: All tasks blocked by dependencies**
```
I cannot proceed with any tasks because:

**Task Availability Analysis:**
- Total tasks in todo.md: 15
- Tasks with READY status: 0
- All remaining tasks: BLOCKED

**Specific Blockers:**
1. A1-A4: BLOCKED - Dependencies: A0 (setup-maven-multi-module-build) must complete first
2. B1-B5: BLOCKED - Dependencies: All Phase A tasks must complete first
3. C1-C6: BLOCKED - Dependencies: Phase B tasks must complete first

**Next Steps:**
- Task A0 (setup-maven-multi-module-build) must be completed before any other tasks can begin
- Once A0 completes, tasks A1-A4 will become available

I will stop here and await further instructions.
```

**Scenario 2: All tasks locked by other sessions**
```
I cannot proceed with any tasks because:

**Task Availability Analysis:**
- Total tasks in todo.md: 5
- Tasks with READY status: 5
- Tasks currently locked by other sessions: 5

**Specific Blockers:**
1. implement-parser: LOCKED - Owned by session abc-123-def
2. implement-config: LOCKED - Owned by session xyz-789-ghi
3. implement-cli: LOCKED - Owned by session jkl-456-mno
4. implement-security: LOCKED - Owned by session pqr-012-stu
5. implement-formatter: LOCKED - Owned by session vwx-345-yza

**Next Steps:**
- Wait for other sessions to complete their work and release locks
- Check back periodically to see if any tasks become available

I will stop here and await further instructions.
```

**Scenario 3: No tasks remaining**
```
I cannot proceed with any tasks because:

**Task Availability Analysis:**
- Total tasks in todo.md: 0
- All tasks have been completed and archived to changelog.md

**Conclusion:**
All planned work has been completed. The todo list is empty.

**Next Steps:**
- Review completed work in changelog.md
- User may add new tasks to todo.md if additional work is needed

I will stop here and await further instructions.
```

### Enforcement

The behavior is MANDATORY and will be monitored through:
- Hook validation of task selection patterns
- Verification that blocked/locked tasks are never started
- Confirmation that clear explanations are provided when stopping

## 🎯 COMPLETE STYLE VALIDATION

**AUTOMATED GUIDANCE**: The `smart-doc-prompter.sh` hook automatically injects the 3-component checklist when style work is detected.

**MANDATORY PROCESS**: When user requests "apply style guide" or similar:

1. **NEVER assume checkstyle-only** - Style guide consists of THREE components
2. **FOLLOW PROTOCOL**: task-protocol.md "Complete Style Validation Gate" pattern
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

**ANTI-PATTERNS - ABSOLUTELY FORBIDDEN**:
❌ "This is too complex, let me try a simpler approach" (without justification)
❌ "The optimal solution would take too long" (without effort estimation)
❌ "Let's use a quick workaround for now" (without technical debt assessment)
❌ "I'll implement the minimum viable solution" (when requirements specify comprehensive solution)
❌ "Due to complexity and token usage, I'll create a solid MVP implementation" (complexity/tokens never justify incomplete implementation)
❌ "Given token constraints, I'll implement a basic version" (token budget does not override quality requirements)
❌ "This edge case is too hard to handle properly" (without stakeholder consultation)
❌ "The existing pattern is suboptimal but I'll follow it" (without improvement attempt)

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

**EXAMPLES**:
✅ `testScientificNotationLexing()` - for floating-point literal bugs
✅ `testMethodReferenceInAssignment()` - for parser syntax bugs
✅ `testEnumConstantWithArguments()` - for enum parsing bugs
✅ `testGenericTypeVariableDeclaration()` - for generics bugs

**INTEGRATION**: Unit tests become part of the development workflow, not separate documentation

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

**ENHANCED SCOPE ASSESSMENT** (extends task-protocol.md Phase 5):
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
**Task Worktrees**: `/workspace/branches/{task-name}/code/` (isolated per task-protocol.md)
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

## 📝 CODE POLICIES

**For complete code policies, see**: [docs/optional-modules/code-policies.md](docs/optional-modules/code-policies.md)

**Quick Reference**:
- **Code Comments**: Update outdated comments, avoid implementation history
- **TODO Comments**: Implement, remove, or document - never superficially rename
- **JavaDoc**: Manual process required, no automated scripts
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

**Stakeholder Reports** (`../` from code directory):
- Temporary workflow artifacts for 7-phase task protocol
- Examples: `{task-name}-technical-architect-requirements.md`, `{task-name}-style-auditor-review.md`
- **Lifecycle**: Created during task execution, cleaned up with worktree in Phase 7
- **Purpose**: Process documentation for protocol compliance

**Empirical Studies** (`docs/studies/{topic}.md`):
- Temporary research cache for pending implementation tasks
- Examples: `docs/studies/claude-cli-interface.md`, `docs/studies/claude-startup-sequence.md`
- **Lifecycle**: Persist until ALL dependent todo.md tasks consume them as input
- **Purpose**: Behavioral analysis and research studies based on empirical testing
- **Cleanup Rule**: Remove after all dependent tasks complete implementation

**Project Code**: Task code directory (`src/`, `pom.xml`, etc.)

### Report File Naming Convention
See **"MANDATORY OUTPUT REQUIREMENT"** patterns in [docs/project/task-protocol.md](docs/project/task-protocol.md) for exact agent report naming conventions by phase.

**Note**: The `../` path writes reports to `/workspace/branches/{task-name}/` (task root), not inside the code directory.

## 📝 RETROSPECTIVE DOCUMENTATION POLICY

**CRITICAL**: Do NOT create retrospective documentation files that chronicle fixes, problems, or development process.

**PROHIBITED DOCUMENTATION PATTERNS**:
❌ Post-implementation analysis reports (e.g., `protocol-violation-prevention.md`)
❌ "Lessons learned" documents chronicling what went wrong and how it was fixed
❌ Debugging chronicles or problem-solving narratives
❌ Development process retrospectives or meta-documentation
❌ Fix documentation that duplicates information already in code/commits

**RATIONALE**:
- Code and commit messages are the primary record of changes
- Git history provides the full development timeline
- Retrospective documents create maintenance burden without user value
- Documentation should serve future developers, not chronicle past problems

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
