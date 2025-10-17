# Claude Code Configuration Guide

> **Version:** 2.0 | **Last Updated:** 2025-10-16
> **Related Documents:** [task-protocol-core.md](docs/project/task-protocol-core.md) • [task-protocol-operations.md](docs/project/task-protocol-operations.md)

Styler Java Code Formatter project configuration and workflow guidance.

## 🚨 MANDATORY COMPLIANCE

**CRITICAL WORKFLOW**: Task Protocol ([core](docs/project/task-protocol-core.md) + [operations](docs/project/task-protocol-operations.md)) - MANDATORY risk-based protocol selection - Apply appropriate workflow based on file risk classification.
**CRITICAL LOCK OWNERSHIP**: See [§ Lock Ownership & Task Recovery](#-lock-ownership--task-recovery) for complete lock file requirements and ownership rules.
**CRITICAL WORKTREE ISOLATION**: See [§ Worktree Isolation & Cleanup](#-worktree-isolation--cleanup) for complete worktree management requirements.
**CRITICAL STYLE**: Complete style validation = checkstyle + PMD + manual rules - See [§ Complete Style Validation](#complete-style-validation) and task-protocol-core.md
**CRITICAL PERSISTENCE**: [Long-term solution persistence](#-long-term-solution-persistence) - MANDATORY prioritization of optimal solutions over expedient alternatives.
**CRITICAL TASK COMPLETION**: Tasks are NOT complete until ALL protocol states are finished (INIT through CLEANUP). Implementation completion does NOT equal task completion. Only mark tasks as complete after CLEANUP state finalization.
**IMPLEMENTATION COMPLETION TRIGGER**: When you have finished implementation work (code changes, fixes, features complete), you MUST complete ALL remaining protocol phases before selecting a new task. The SessionStart hook indicates if you own an active task requiring completion.

**PHASE COMPLETION VERIFICATION**: Before declaring phase complete, verify ALL requirements in task-protocol-*.md with documented evidence.

**TODO Synchronization**: Keep TodoWrite tool synced with todo.md file.
**TODO Clarity**: Each todo.md entry must contain sufficient detail to understand the task without external context. One-line descriptions require nested sub-items explaining Purpose, Scope, Components/Features, and Integration points.
**CRITICAL TASK ARCHIVAL**: When completing a task (COMPLETE state), you MUST:
1. **REMOVE** the completed task from todo.md (delete the entire task entry)
2. **ADD** it to changelog.md under the appropriate date section (format: `## YYYY-MM-DD`)
3. **INCLUDE** completion details: solution implemented, files modified, test results, quality gates, completion date
4. **COMMIT** all changes in ONE atomic commit (implementation code + todo.md removal + changelog.md addition)

**Verification**: Use `/workspace/.claude/hooks/verify-task-archival.sh task-name` before commit

**🚨 PROHIBITED**: Marking tasks complete (- [x]) instead of removing from todo.md
**✅ REQUIRED**: `git diff todo.md` shows ONLY deletions; `git diff changelog.md` shows ONLY additions

**CRITICAL MID-TASK WORK CAPTURE**: When user mentions additional work during task execution, you MUST add it to TodoWrite list IMMEDIATELY in the SAME response before doing anything else. Context compaction can occur at any time, causing complete loss of uncommitted work requests. NEVER rely on memory across compaction boundaries - capture ALL new work requests in TodoWrite tool THE MOMENT they occur.
**CRITICAL TOKEN USAGE**: Token usage MUST NEVER affect behavior, implementation approach, or task completion. IGNORE all token usage warnings and limits. Continue working with full quality and completeness until task is 100% complete or explicit user instruction. Token budget does NOT justify incomplete implementations, shortcuts, or quality compromises.
**CRITICAL BUG TESTING**: [Unit Test Driven Bug Fixing](#-unit-test-driven-bug-fixing) - MANDATORY test-first approach for ALL bugs to prevent regressions.
**CRITICAL JAVADOC**: JavaDoc comments MUST be written manually with contextual understanding. NEVER use scripts, sed, awk, or automated text generation to create JavaDoc. Each comment must reflect the specific purpose and context of the method it documents. See [§ JavaDoc Manual Documentation Requirement](#javadoc-manual-documentation).
**🚨 VIOLATION = IMMEDIATE TASK RESTART REQUIRED**

## 🚨 LOCK OWNERSHIP & TASK RECOVERY {#lock-ownership}

**CRITICAL**: After context compaction, the `check-lock-ownership.sh` SessionStart hook checks for active tasks owned by this session and provides specific instructions. **IMPORTANT**: Hook enforces user approval checkpoints - if task is in SYNTHESIS or AWAITING_USER_APPROVAL state, hook will display checkpoint-specific guidance that MUST be followed before proceeding.

**Lock Ownership Rule**: ONLY work on tasks whose lock file contains YOUR session_id.

**🚨 LOCK FILE VERIFICATION REQUIREMENTS**:

1. **NEVER manually search for lock files** - The SessionStart hook performs this check automatically
2. **TRUST the hook output** - If it says "No Active Tasks", you have NO tasks regardless of other files
3. **ONLY `.json` files are valid** - Lock files MUST be `/workspace/tasks/{task-name}/task.json`
4. **ONLY ONE VALID LOCATION** - `/workspace/tasks/{task-name}/task.json` (no other directories)
5. **Invalid patterns are NEVER valid**:
   - ❌ `/workspace/locks/task-name.lock` - Wrong directory
   - ❌ `/workspace/locks/task-name.txt` - Wrong directory and extension
   - ❌ `/workspace/tasks/{task-name}/task.lock` - Wrong extension
   - ❌ Any file with extension other than `.json`
6. **If you see your session_id in a non-.json file**: Delete it immediately, it's incorrect
7. **NEVER remove lock files unless you own them** - session_id must match
8. **If lock acquisition fails** - Select alternative task, do NOT delete the lock

**Lock File Format**:
```json
{
  "session_id": "unique-session-identifier",
  "task_name": "task-name-matching-filename",
  "state": "INIT",
  "created_at": "2025-10-16T14:32:00Z"
}
```

**Valid state values**: INIT, CLASSIFIED, REQUIREMENTS, SYNTHESIS, IMPLEMENTATION, VALIDATION, REVIEW, AWAITING_USER_APPROVAL, COMPLETE, CLEANUP

## 🚨 WORKTREE ISOLATION & CLEANUP {#worktree-isolation}

### During Task Execution

**CRITICAL WORKTREE ISOLATION**: After creating worktree, IMMEDIATELY `cd` to worktree directory BEFORE any other operations.

**Required Pattern**:
```bash
git worktree add /workspace/tasks/{task-name}/code -b {task-name} && cd /workspace/tasks/{task-name}/code
```

**ALL subsequent work must occur inside worktree, NEVER in main branch.**

**Verification Before Proceeding**:
```bash
pwd | grep -q "/workspace/tasks/{task-name}/code$" && echo "✅ In worktree" || echo "❌ ERROR: Not in worktree!"
```

### During Cleanup (CLEANUP State)

**CRITICAL WORKTREE CLEANUP**: BEFORE removing worktree, MUST `cd` to main worktree.

**Required Pattern**:
```bash
cd /workspace/main && git worktree remove /workspace/tasks/{task-name}/code
```

**NEVER remove a worktree while inside it** - shell loses working directory. This is MANDATORY in CLEANUP state.

## 🚨 IMPLEMENTATION ROLE BOUNDARIES {#implementation-role-boundaries}

**CRITICAL**: During IMPLEMENTATION state, main agent and stakeholder agents have STRICTLY SEPARATED roles.

### Terminology Definitions

**CRITICAL TERMINOLOGY** (used throughout this protocol):

- **Implementation**: Creating new features, classes, methods, or significant logic
  - Examples: Writing FormattingRule.java, implementing algorithm logic, adding new methods
  - During IMPLEMENTATION state: ONLY stakeholder agents perform implementation

- **Coordination**: Managing agent invocations, monitoring status, updating state
  - Examples: Launching agents via Task tool, checking status.json, updating lock file
  - Main agent role during IMPLEMENTATION state

- **Minor Fixes**: Mechanical corrections to code after implementation complete
  - Examples: Adding missing imports, fixing whitespace, removing unused variables, fixing typos
  - Main agent MAY perform after VALIDATION state begins
  - NOT considered 'implementation' because no new logic or features

- **Fixes** (during IMPLEMENTATION): Corrections to agent-written code
  - Examples: Fixing bugs in newly written classes, adjusting architecture
  - During IMPLEMENTATION state: ONLY original agent fixes their own work
  - Main agent NEVER fixes during IMPLEMENTATION state

**Rule of Thumb**: If it requires domain expertise or significant changes → delegate to agents. If it's mechanical and trivial → main agent may fix after VALIDATION state begins.

**Coordination Examples (PERMITTED during IMPLEMENTATION)**:
✅ Launching technical-architect agent: "Implement FormatterApi interface with transform() method"
✅ Monitoring agent status: Reading `/workspace/tasks/add-api/agents/technical-architect/status.json`
✅ Updating lock file state: `jq '.state = "VALIDATION"' task.json`
✅ Creating task infrastructure: Writing `/workspace/tasks/add-api/task.md` with requirements

**Implementation Examples (PROHIBITED during IMPLEMENTATION state)**:
❌ Creating source files: `Write tool → src/main/java/FormatterApi.java`
❌ Implementing methods: Adding `public Token getToken()` to source file
❌ Fixing compilation: Adding `import java.util.List;` to new implementation code
❌ Writing tests: Creating `TestFormatterApi.java` test class

**Configuration File Boundary Cases**:
✅ PERMITTED: Task infrastructure (`.claude/task-context.json`, `task.md`)
❌ PROHIBITED: Project configuration (`pom.xml` dependencies, `application.properties`)

### Main Agent Role During IMPLEMENTATION State (COORDINATION ONLY)

**PERMITTED Actions**:
✅ Launch stakeholder agents via Task tool with implementation instructions
✅ Monitor agent status.json files for completion signals
✅ Update lock file state transitions
✅ Coordinate iterative validation rounds
✅ Determine when all agents report work complete

**ABSOLUTELY PROHIBITED Actions**:
❌ Use **Write tool** to create source files (.java, .ts, .py, etc.) in task worktree
❌ Use **Edit tool** to modify source files (.java, .ts, .py, etc.) in task worktree
❌ Create implementation classes, interfaces, or modules directly
❌ "Implement then have agents review" pattern - THIS IS A VIOLATION
❌ "Quick implementation before delegating" - THIS IS A VIOLATION

### Stakeholder Agent Role (IMPLEMENTATION ONLY)

**REQUIRED Actions**:
✅ Implement domain-specific requirements in THEIR OWN worktrees
✅ Write all source code files (.java, .ts, .py, etc.)
✅ Run incremental validation (compile, test, checkstyle)
✅ Merge changes to task branch after validation
✅ Update status.json with completion state

**Working Directory**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`

### Role Verification Questions

**Before writing ANY .java/.ts/.py file during IMPLEMENTATION, ask yourself**:
- [ ] Am I the main coordination agent? → If YES, use Task tool to delegate
- [ ] Am I a stakeholder agent in my own worktree? → If YES, proceed with implementation
- [ ] Am I in task worktree trying to implement? → STOP - This is a VIOLATION

### Required Pattern After SYNTHESIS Approval

```markdown
CORRECT SEQUENCE:
1. User approves implementation plan
2. Main agent: "Launching stakeholder agents for parallel implementation..."
3. Main agent: Task tool (technical-architect) with implementation instructions
4. Main agent: Task tool (code-quality-auditor) with implementation instructions
5. Main agent: Task tool (style-auditor) with implementation instructions
6. Stakeholder agents implement in THEIR worktrees
7. Main agent monitors status.json files for completion
8. ALL agents report COMPLETE status in their status.json files
9. Main agent updates lock file: state = "VALIDATION"
10. Main agent NOW PERMITTED to fix minor issues (style violations, imports, etc.)
11. Main agent runs final build verification: ./mvnw verify
12. If build passes → proceed to REVIEW state
13. If build fails → main agent MAY fix OR re-delegate to agents
```

**Key Transition Point**: Step 9 (VALIDATION state) is when main agent permissions change from PROHIBITED to PERMITTED for minor fixes.

**VIOLATION PATTERN** (NEVER DO THIS):
```markdown
❌ WRONG SEQUENCE:
1. User approves implementation plan
2. Main agent: "I will now implement the feature..."
3. Main agent: **Write** src/main/java/FormatterApi.java (VIOLATION!)
4. Main agent: **Edit** src/main/java/Feature.java (VIOLATION!)
5. Main agent creates files directly in task worktree (VIOLATION!)
```

### Enforcement

**Hook Detection**: `.claude/hooks/detect-main-agent-implementation.sh` monitors Write/Edit tool calls during IMPLEMENTATION state and BLOCKS attempts by main agent to create source files in task worktree.

**🚨 CRITICAL REQUIREMENT**: This hook MUST be registered in `.claude/settings.json` under `PreToolUse` triggers to function. If hook is not registered, **NO PROTECTION EXISTS**.

**Hook Registration Verification**:
```bash
# Verify hook is registered and active
jq '.hooks.PreToolUse[] | select(.hooks[].command | contains("detect-main-agent-implementation"))' /workspace/.claude/settings.json

# Expected output: Hook configuration object with matcher
# If empty: CRITICAL - Hook NOT registered, no protection active
```

**If Hook NOT Registered (Verification Fails)**:
1. **STOP IMMEDIATELY** - Do not proceed with any IMPLEMENTATION work
2. Alert user: "CRITICAL: Implementation protection hook not registered"
3. Provide registration instructions (show required settings.json configuration below)
4. Wait for user to confirm hook registration
5. Re-run verification before continuing

**NEVER** proceed with IMPLEMENTATION state if hook verification fails - no protection exists.

**Required Registration** (in `.claude/settings.json`):
```json
{
  "matcher": "(tool:Write || tool:Edit) && path:**/*.{java,ts,py,js,go,rs,cpp,c,h}",
  "hooks": [{
    "type": "command",
    "command": "/workspace/.claude/hooks/detect-main-agent-implementation.sh"
  }]
}
```

**Recovery**: If violation detected, return to SYNTHESIS state and re-launch stakeholder agents properly.

### Agent Tool Limitation Recovery Pattern

**Scenario**: Stakeholder agent reports tool limitations, file size constraints, or inability to complete assigned work.

**CORRECT RECOVERY SEQUENCE**:
1. **NEVER** bypass agent by implementing directly - this violates protocol regardless of reason
2. Reduce agent scope: Re-launch agent with smaller file subset or reduced requirements
3. Split work: Launch multiple agent instances with divided responsibilities
4. If persistent: Transition to REVIEW state → document limitation → request user guidance
5. Only after VALIDATION state begins: Main agent may fix if scope is truly mechanical (imports, whitespace)

**PROHIBITED PATTERNS**:
❌ Agent says "file too large" → Main agent implements directly
❌ Agent reports Edit tool limitation → Main agent takes over
❌ Agent cannot complete → Main agent "helps" by implementing in task worktree

**Recovery Rule**: Agent limitations change SCOPE or APPROACH, NEVER change WHO implements.

### State-Based Edit/Write Tool Permissions

**CRITICAL**: Main agent Edit/Write tool permissions vary by state.

| State | Main Agent Edit/Write Permission | Rationale |
|-------|----------------------------------|------------|
| **INIT** | ✅ PERMITTED - Infrastructure setup only | Create worktrees, lock files, task.md skeleton |
| **CLASSIFIED** | ✅ PERMITTED - Documentation only | Update task.md with risk classification |
| **REQUIREMENTS** | ✅ PERMITTED - Documentation only | Update task.md with consolidated requirements |
| **SYNTHESIS** | ✅ PERMITTED - Documentation only | Update task.md with implementation plan |
| **IMPLEMENTATION** | ❌ PROHIBITED - Source code files | Stakeholder agents implement in their worktrees |
| **VALIDATION** | ✅ PERMITTED - Minor fixes only | Fix style violations, compilation errors after agent completion |
| **REVIEW** | ✅ PERMITTED - Fix agent feedback | Address stakeholder agent review comments |
| **COMPLETE** | ✅ PERMITTED - Final touches | Amend commits if user requests changes |
| **CLEANUP** | ✅ PERMITTED - Infrastructure only | Remove worktrees, update todo.md/changelog.md |

**IMPLEMENTATION State Clarification**:
- Main agent MUST NOT create or modify source code files (.java, .ts, .py, etc.)
- Main agent MAY update documentation files (task.md, status files)
- ALL source code implementation delegated via Task tool to stakeholder agents

**VALIDATION State Clarification**:
- Main agent MAY fix minor issues discovered after agents complete
- Examples: style violations, missing imports, compilation errors
- Rationale: Efficiency - avoid re-launching agents for trivial fixes
- Constraint: Only after all agents report COMPLETE status

**VALIDATION State Exit Requirements** (CRITICAL):
1. ✅ All quality gates pass (checkstyle, PMD, build)
2. ✅ **All validation fixes COMMITTED to task branch** before proceeding
3. ✅ Run final build verification on committed code
4. ✅ Verify `git status` shows clean working directory (no uncommitted changes)

**Rationale**: Uncommitted validation fixes will NOT be included in merge, causing build failures on main branch.

**Verification Command**:
```bash
git status | grep "nothing to commit" || echo "ERROR: Uncommitted changes exist"
```

**Audit Integration**: If `/audit-session` detects uncommitted changes during VALIDATION state, this indicates a protocol violation (Check 0.3 equivalent). Phase 5 automatic fix application will commit these changes, but this represents a recovery action - the violation still occurred. Future prevention requires discipline during VALIDATION → REVIEW transition.

**See Also**: `.claude/commands/audit-session.md` § Phase 5: Automatic Fix Application for audit-time detection and automatic remediation

**REVIEW State Clarification**:
- Main agent MAY implement fixes requested by stakeholder agents
- Only for issues identified during agent review phase
- Alternative: Re-delegate to appropriate stakeholder agent if changes are complex

### Post-Implementation Issue Handling Decision Tree

**CRITICAL TIMING REQUIREMENT**: This decision tree applies ONLY after ALL three conditions are met:
1. ✅ ALL stakeholder agents have status.json with `{"status": "COMPLETE"}`
2. ✅ ALL agents have merged their changes to task branch
3. ✅ Lock file state updated to `"VALIDATION"`

**Before these conditions**: Main agent MUST NOT fix anything - use Task tool to request agent fixes.
**After these conditions**: Main agent MAY apply decision tree below for minor fixes.

**After ALL agents report COMPLETE status, main agent discovers issues during VALIDATION:**

```
IF (issue_type == "compilation_error"):
    IF (simple_fix like missing_import OR unused_variable):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to technical-architect agent

ELSE IF (issue_type == "style_violation"):
    IF (count <= 5 violations total across all files AND fixes are mechanical like whitespace/imports):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to style-auditor agent

ELSE IF (issue_type == "test_failure"):
    IF (simple_fix like assertion_update):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to code-tester agent

ELSE IF (issue_type == "architecture_issue" OR "security_issue"):
    ❌ ALWAYS re-delegate to appropriate domain expert
```

**Efficiency Rationale**: Re-launching agents for trivial fixes wastes 50-100 messages per round. Direct fixes for mechanical issues preserve protocol safety while maintaining efficiency.

**Threshold Rationale**: The '5 violation' threshold represents message cost efficiency:
- Auto-fixing 5 violations: ~2-3 tool calls = 3-5 messages
- Agent delegation: ~50-100 messages per round
- Breakeven: When manual fixing approaches agent launch cost

**Counting Rule**: Total violations across ALL files, ALL types combined.

**Clarification Examples**:
- File A: 3 missing imports, File B: 2 whitespace → Total 5 → Main agent fixes ✅
- File A: 3 missing imports, File B: 3 whitespace → Total 6 → Delegate to agent ❌
- Files A-J: 50 violations, all same missing import statement → Treat as 1 pattern → Main agent fixes ✅

**Override Rule**: If ALL violations are identical pattern (e.g., 10 missing imports, all same import statement), treat as mechanical regardless of count.

**Safety Constraint**: Only apply after unanimous agent completion. During IMPLEMENTATION state, ALL fixes must go through agents.

## 🚨 TASK PROTOCOL SUMMARY {#task-protocol}

**Full Protocol Details**: See [task-protocol-core.md](docs/project/task-protocol-core.md) and [task-protocol-operations.md](docs/project/task-protocol-operations.md) for complete state machine and transition requirements.

**State Machine**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → AWAITING_USER_APPROVAL → COMPLETE → CLEANUP

**Critical Requirements for All Tasks**:
- Lock acquisition (see [§ Lock Ownership](#lock-ownership))
- Worktree isolation (see [§ Worktree Isolation](#worktree-isolation))
- **🚨 IMPLEMENTATION DELEGATION**: Main agent COORDINATES via Task tool - stakeholder agents IMPLEMENT in their worktrees (see [§ Implementation Role Boundaries](#implementation-role-boundaries))
- Build verification before merge
- Unanimous stakeholder approval (REVIEW state)
- Complete all states before selecting new task

**Risk-Based Variants**:
- **HIGH-RISK** (src/\*\*, pom.xml, security/\*\*): Full protocol with all agents
- **MEDIUM-RISK** (tests, docs): Abbreviated protocol, domain-specific agents
- **LOW-RISK** (general docs): Streamlined protocol, minimal validation

**Post-Compaction Note**: This summary plus lock ownership and worktree isolation rules provide EMERGENCY FALLBACK guidance when task-protocol files are not accessible. For complete protocol compliance, the full protocol files (task-protocol-core.md and task-protocol-operations.md) are REQUIRED and should be re-loaded as soon as possible.

## 🚨 RISK-BASED PROTOCOL SELECTION

**PROTOCOL SELECTION BASED ON FILE RISK:**
- **HIGH-RISK**: Full protocol (src/\*\*, pom.xml, .github/\*\*, security/\*\*, CLAUDE.md)
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

**CRITICAL**: Once you begin a task (execute INIT state), you MUST complete ALL protocol states autonomously, WITH MANDATORY USER APPROVAL CHECKPOINTS.

**MANDATORY SINGLE-SESSION COMPLETION**:
- Task execution occurs in ONE uninterrupted session
- **EXPECTED USER APPROVAL CHECKPOINTS** (these are NOT violations):
  1. **PLAN APPROVAL**: After SYNTHESIS, user approves implementation plan before IMPLEMENTATION begins
  2. **CHANGE REVIEW**: After REVIEW (unanimous approval), enter AWAITING_USER_APPROVAL state until user confirms
- NO OTHER HANDOFFS to user mid-protocol
- Complete all other states autonomously

**When to Ask User**:
✅ **BEFORE** starting task: "Task X has ambiguous requirements. Clarify before I begin?"
✅ **AFTER SYNTHESIS**: Present plan for approval before IMPLEMENTATION
✅ **AFTER REVIEW**: Present changes, wait for approval before COMPLETE
✅ **NEVER** at other points: Complete other states autonomously once INIT begins

**Plan Presentation Format** (after SYNTHESIS):
- Present implementation plan in clear, readable format (use markdown)
- Include: architecture approach, files to modify, implementation sequence, testing strategy
- Wait for explicit user approval before proceeding to IMPLEMENTATION

**Only Stop Mid-Protocol If**:
1. **Genuine External Blocker**: API unavailable, missing credentials, network failure
2. **Ambiguous Conflicting Requirements**: No resolution path exists
3. **User Explicit Interruption**: User says "stop"

**Enforcement**: The `detect-giving-up.sh` hook detects mid-protocol abandonment patterns and injects completion reminders.

## 🚨 USER APPROVAL CHECKPOINT ENFORCEMENT

**HOOK-ENFORCED REQUIREMENT**: The `enforce-user-approval.sh` hook will BLOCK transitions to COMPLETE state if user approval checkpoint is not satisfied.

**Approval Marker System**:
- **File**: `/workspace/tasks/{task-name}/user-approval-obtained.flag`
- **Created**: When user provides explicit approval after REVIEW state
- **Required**: Before COMPLETE state transition is allowed
- **Removed**: Automatically during CLEANUP state

**Automatic Approval Detection** (hook recognizes these patterns):
✅ User message contains approval keywords: "yes", "approved", "approve", "proceed", "looks good", "LGTM"
✅ AND message references review context: "review", "changes", "commit", "finalize"

**User Instructions DO NOT Override Checkpoints**:
❌ "Continue without asking questions" - DOES NOT skip checkpoint
❌ "Just finish the task" - DOES NOT skip checkpoint
❌ "I trust you, proceed" - DOES NOT skip checkpoint (unless includes review context)
❌ Bypass mode enabled - DOES NOT skip checkpoint
❌ Automation mode active - DOES NOT skip checkpoint

**How Enforcement Works**:
1. Hook monitors all UserPromptSubmit events
2. When task is in REVIEW state and user says "continue/proceed/finalize"
3. Hook checks for approval marker file existence
4. If marker missing AND message is not explicit approval → **BLOCKED with checkpoint reminder**
5. If message IS explicit approval → Create marker, allow continuation
6. If marker exists → Allow COMPLETE state transition

**Required Pattern for Approval**:
✅ "All changes look good, please proceed with finalization"
✅ "Yes, approved - you can merge this"
✅ "LGTM, proceed with cleanup"

**Checkpoint Rejection Recovery**:

**If user rejects PLAN APPROVAL** (after SYNTHESIS):
1. Return to REQUIREMENTS state if requirements need clarification
2. Return to SYNTHESIS if only plan needs revision
3. Update task.md with revised requirements or plan
4. Re-present for approval before IMPLEMENTATION

**If user rejects CHANGE REVIEW** (during AWAITING_USER_APPROVAL):
1. Identify specific issues or requested changes
2. Return to IMPLEMENTATION if code changes needed
3. Return to REVIEW after fixes for agent re-validation
4. Only proceed to AWAITING_USER_APPROVAL after unanimous agent approval again

**Lock state updates** for rejections:
```bash
# Return to appropriate state
jq '.state = "SYNTHESIS"' /workspace/tasks/{task-name}/task.json > /tmp/lock.tmp
mv /tmp/lock.tmp /workspace/tasks/{task-name}/task.json
```

## 🚨 TASK UNAVAILABILITY HANDLING

**CRITICAL**: When user requests "work on the next task" or similar, verify task availability before starting work.

### Mandatory Availability Check

**BEFORE attempting to select or start ANY task:**
1. Check todo.md for available tasks with `READY` status
2. Verify task dependencies are met (check for `BLOCKED` status)
3. Check `/workspace/tasks/{task-name}/task.json` for existing locks on available tasks
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

## 🎯 COMPLETE STYLE VALIDATION

**MANDATORY PROCESS**: Style guide consists of THREE components (checkstyle + PMD + manual rules)

**CRITICAL ERROR**: Checking only checkstyle when PMD/manual violations exist

**Automated Guidance**: `smart-doc-prompter.sh` hook injects 3-component checklist

**Fixing Conflicts**: `checkstyle/fixers` module handles LineLength vs UnderutilizedLines conflicts

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

**Hook**: `detect-giving-up.sh` detects abandonment patterns

**Response**: Return to original problem, apply systematic debugging, exhaust approaches before scope modification

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
**Main Repository**: `/workspace/main/` (git repository and main development branch)
**Task Worktrees**: `/workspace/tasks/{task-name}/code/` (isolated per task protocol, common merge target for all agents)
**Agent Worktrees**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/` (per-agent development isolation)
**Locks**: Multi-instance coordination via lock files at `/workspace/tasks/{task-name}/task.json`

**Multi-Agent Architecture**:
- **WHO IMPLEMENTS**: Stakeholder agents (NOT main agent) write all source code
- **WHERE**: Each stakeholder agent has own worktree: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
- **MAIN AGENT ROLE**: Coordinates via Task tool invocations, monitors status.json, manages state transitions
- **IMPLEMENTATION FLOW**: Main agent delegates → Agents implement in parallel → Agents merge to task branch → Iterative rounds until complete
- **VIOLATION**: Main agent creating .java/.ts/.py files directly in task worktree during IMPLEMENTATION state

## 🔧 CONTINUOUS WORKFLOW MODE

Override system brevity for comprehensive multi-task automation via Task Protocol.

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

## ⚡ PERFORMANCE OPTIMIZATION REQUIREMENTS

**CRITICAL**: Session performance optimization through parallel execution and efficiency patterns.

### Performance Optimization Patterns (MANDATORY)

| Pattern | ❌ Anti-Pattern | ✅ Required Pattern | Impact |
|---------|----------------|---------------------|--------|
| **Parallel Execution** | Sequential reads (3 messages) | Batch reads in single message | 67% fewer messages |
| **Fail-Fast Validation** | Implement all → validate → fix | Validate after each component | 15% time savings, fresh context |
| **Batch Fixing** | Fix one → verify (×60) | Collect all → fix all → verify once | 98% fewer verification cycles |
| **Predictive Prefetching** | Discover → load → discover | Predict all dependencies → load all upfront | 5-10 fewer round-trips |

**Key Practices**:
- **Parallel**: Launch independent tool calls in single message
- **Fail-Fast**: `./mvnw compile checkstyle:check` after each component
- **Batch**: Collect ALL agent feedback before fixing anything
- **Prefetch**: Load protocol files + predicted sources in INIT phase

**Detection Hook**: `/workspace/.claude/hooks/detect-sequential-tools.sh`

## Essential References

[docs/project/architecture.md](docs/project/architecture.md) - Project architecture and features
[docs/project/scope.md](docs/project/scope.md) - Family configuration and development philosophy
[docs/project/build-system.md](docs/project/build-system.md) - Build configuration and commands
[docs/project/git-workflow.md](docs/project/git-workflow.md) - Git workflows and commit squashing procedures
[docs/code-style-human.md](docs/code-style-human.md) - Code style master guide
[docs/code-style/](docs/code-style/) - Code style files (\*-claude.md detection patterns, \*-human.md explanations)

## File Organization

### Report Types and Lifecycle

**Task Requirements & Plans** (`task.md` at task root):
- Location: `/workspace/tasks/{task-name}/task.md`
- Contains all agent requirements and implementation plans
- **Created**: During CLASSIFIED state (by main agent, BEFORE stakeholder agent invocation)
- **Updated**: During REQUIREMENTS (agent reports added), SYNTHESIS (implementation plans added)
- **Lifecycle**: Persists through entire task execution, removed during CLEANUP state

**Stakeholder Reports** (at task root, one level up from code directory):
- Temporary workflow artifacts for task protocol
- Examples: `{task-name}-technical-architect-requirements.md`, `{task-name}-style-auditor-review.md`
- **Lifecycle**: Created during task execution, cleaned up with worktrees in CLEANUP state
- **Purpose**: Process documentation for protocol compliance
- **Location**: `/workspace/tasks/{task-name}/` (task root, accessible to all agents)

**Empirical Studies** (`docs/studies/{topic}.md`):
- Temporary research cache for pending implementation tasks
- Examples: `docs/studies/claude-cli-interface.md`, `docs/studies/claude-startup-sequence.md`
- **Lifecycle**: Persist until ALL dependent todo.md tasks consume them as input
- **Purpose**: Behavioral analysis and research studies based on empirical testing
- **Cleanup Rule**: Remove after all dependent tasks complete implementation

**Project Code**: Task code directory (`src/`, `pom.xml`, etc.)

### Report File Naming Convention
See **"MANDATORY OUTPUT REQUIREMENT"** patterns in [task-protocol-core.md](docs/project/task-protocol-core.md) and [task-protocol-operations.md](docs/project/task-protocol-operations.md) for exact agent report naming conventions by phase.

**Note**: Reports are written to `/workspace/tasks/{task-name}/` (task root), not inside the code directory.

## 📝 RETROSPECTIVE DOCUMENTATION POLICY

**CRITICAL**: Do NOT create retrospective documentation files that chronicle fixes, problems, or development process.

**PROHIBITED DOCUMENTATION PATTERNS**:
❌ Post-implementation analysis reports (e.g., `protocol-violation-prevention.md`)
❌ "Lessons learned" documents chronicling what went wrong and how it was fixed
❌ Debugging chronicles or problem-solving narratives
❌ Development process retrospectives or meta-documentation
❌ Fix documentation that duplicates information already in code/commits

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
