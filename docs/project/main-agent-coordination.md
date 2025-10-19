# Main Agent Coordination Guide

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** Main coordination agent only
> **Purpose:** Task protocol, lock management, approval checkpoints, and coordination patterns

This document contains **main-agent-specific** content extracted from CLAUDE.md. It covers task protocol coordination, lock ownership, worktree management, agent invocation patterns, approval checkpoints, and performance optimization.

**Sub-agents**: You should read `task-protocol-agents.md` instead - this file is for main agent coordination only.

## 🚨 LOCK OWNERSHIP & TASK RECOVERY {#lock-ownership}

**CRITICAL**: After context compaction, the `check-lock-ownership.sh` SessionStart hook checks for active
tasks owned by this session and provides specific instructions. **IMPORTANT**: Hook enforces user approval
checkpoints - if task is in SYNTHESIS or AWAITING_USER_APPROVAL state, hook will display checkpoint-specific
guidance that MUST be followed before proceeding.

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

**Valid state values**: INIT, CLASSIFIED, REQUIREMENTS, SYNTHESIS, IMPLEMENTATION, VALIDATION, REVIEW,
AWAITING_USER_APPROVAL, COMPLETE, CLEANUP

## 🚨 WORKTREE ISOLATION & CLEANUP {#worktree-isolation}

### During Task Execution

**CRITICAL WORKTREE ISOLATION**: After creating worktree, IMMEDIATELY `cd` to worktree directory BEFORE any
other operations.

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

**NEVER remove a worktree while inside it** - shell loses working directory. This is MANDATORY in CLEANUP
state.

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

**Rule of Thumb**: If it requires domain expertise or significant changes → delegate to agents. If it's
mechanical and trivial → main agent may fix after VALIDATION state begins.

**Coordination Examples (PERMITTED during IMPLEMENTATION)**:
✅ Launching architecture-reviewer agent: "Implement FormatterApi interface with transform() method"
✅ Monitoring agent status: Reading `/workspace/tasks/add-api/agents/architecture-updater/status.json`
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

**Special Case: Test Dependencies**:
- ❌ PROHIBITED (Main Agent): Adding dependencies to pom.xml during IMPLEMENTATION
- ✅ PERMITTED (Technical-Architect Agent): Adding test-scoped dependencies when implementing new test infrastructure, ONLY if:
  1. Dependencies are test-scoped (`<scope>test</scope>`)
  2. Agent documents dependency in their requirements/plan
  3. Agent validates no conflicts with existing dependencies
- ✅ PERMITTED (Main Agent): Adding test dependencies during VALIDATION if agents request them

**Example**: Technical-architect implements new integration tests requiring Mockito → agent may add `mockito-core` with test scope in their worktree.

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

### Session Summary Documentation Requirements

**MANDATORY SESSION SUMMARY REQUIREMENT**: When presenting implementation work for protocol compliance review, session summaries MUST include:

1. **Agent Invocation Audit Trail**: List of all Task tool invocations with agent names
2. **Commit Attribution**: Commit SHAs for each agent's merge to task branch
3. **Parallel Launch Verification**: Explicit confirmation of parallel agent launch pattern used

**Example Compliant Session Summary**:
```markdown
## Implementation Phase Summary

### Agent Invocations (Parallel Launch):
- architecture-updater: Task tool invoked in Message 15
- quality-updater: Task tool invoked in Message 15 (parallel)
- style-updater: Task tool invoked in Message 15 (parallel)
- test-updater: Task tool invoked in Message 15 (parallel)

### Agent Commits to Task Branch:
- architecture-updater: abc123def (merged FormattingRule interfaces)
- quality-updater: def456ghi (applied design patterns)
- style-updater: ghi789jkl (code style compliance)
- test-updater: jkl012mno (test suite implementation)

### Verification:
✅ Parallel agent launch pattern confirmed (all agents invoked in single message)
✅ All agents merged to task branch independently
✅ Main agent performed NO source code implementation
```

**Rationale**: These audit trail elements provide verifiable evidence that the multi-agent implementation protocol was followed correctly, preventing protocol violations where main agent implements directly.

### Role Verification Questions

**Before writing ANY .java/.ts/.py file during IMPLEMENTATION, ask yourself**:
- [ ] Am I the main coordination agent? → If YES, use Task tool to delegate
- [ ] Am I a stakeholder agent in my own worktree? → If YES, proceed with implementation
- [ ] Am I in task worktree trying to implement? → STOP - This is a VIOLATION

### Required Pattern After SYNTHESIS Approval

**🚨 MANDATORY REQUIREMENT**: Agent launches MUST use parallel Task tool calls in a SINGLE message.

**VIOLATION**: Launching agents sequentially across multiple messages is a CRITICAL protocol violation that wastes 30,000-40,000 tokens per task.

**MANDATORY SESSION DOCUMENTATION**: Agent invocation record MUST be documented in task.md under "Implementation Status" section:

```markdown
## Implementation Status

### Agent Invocations
**Round 1 - Initial Implementation** (Message 15, 2025-10-19T14:32:00Z):
- architecture-updater: Launched (parallel)
- quality-updater: Launched (parallel)
- style-updater: Launched (parallel)
- test-updater: Launched (parallel)

**Round 1 - Review** (Message 22, 2025-10-19T14:45:00Z):
- architecture-reviewer: Launched (parallel)
- quality-reviewer: Launched (parallel)
- style-reviewer: Launched (parallel)
- test-reviewer: Launched (parallel)

**Round 2 - Fixes** (Message 28, 2025-10-19T15:02:00Z):
- style-updater: Re-launched for violation fixes
- architecture-updater: Re-launched for interface clarifications
```

**Rationale**: This record provides audit trail showing parallel launch pattern was used, not sequential violations.

```markdown
CORRECT SEQUENCE:
1. User approves implementation plan
2. Main agent: "Launching updater agents for parallel implementation..."
3. Main agent launches UPDATER agents in SINGLE MESSAGE (parallel):
   - Task tool (architecture-updater): "Implement FormattingRule interfaces per requirements..."
   - Task tool (quality-updater): "Apply refactoring and design patterns per requirements..."
   - Task tool (style-updater): "Implement code following project style guidelines..."
   - Task tool (test-updater): "Implement test suite per test strategy..."
4. Updater agents implement in THEIR worktrees, validate locally, then merge to task branch
5. Main agent: "Updaters have merged. Launching reviewer agents for parallel review..."
6. Main agent launches REVIEWER agents in SINGLE MESSAGE (parallel):
   - Task tool (architecture-reviewer): "Review merged architecture on task branch..."
   - Task tool (quality-reviewer): "Review merged code quality on task branch..."
   - Task tool (style-reviewer): "Review merged style compliance on task branch..."
   - Task tool (test-reviewer): "Review merged test coverage on task branch..."
7. Reviewer agents analyze and report APPROVED or REJECTED with feedback
8. If any REJECTED → launch updater agents with feedback → re-review (repeat 5-7 until all APPROVED)
9. When ALL reviewers report APPROVED → main agent updates lock file: state = "VALIDATION"
10. Main agent NOW PERMITTED to fix minor issues (style violations, imports, etc.)
11. Main agent runs final build verification: ./mvnw verify
12. If build passes → proceed to REVIEW state
13. If build fails → main agent MAY fix OR re-delegate to agents
14. After REVIEW state with unanimous approval → transition to AWAITING_USER_APPROVAL state
15. Present changes to user with commit SHA and ask: "May I proceed to merge to main?"
16. Wait for user approval response
17. Create user-approval-obtained.flag after approval received
18. Transition to COMPLETE state
19. Merge to main branch
20. Transition to CLEANUP state
```

**CRITICAL**: Steps 14-18 are MANDATORY and CANNOT be skipped. The sequence REVIEW → AWAITING_USER_APPROVAL → COMPLETE → CLEANUP is REQUIRED.

**Key Transition Point**: Step 9 (VALIDATION state) is when main agent permissions change from PROHIBITED to
PERMITTED for minor fixes. Steps 5-8 are iterative rounds within IMPLEMENTATION state using both reviewer and
updater agents until all reviewers approve.

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

**Hook Detection**: `.claude/hooks/detect-main-agent-implementation.sh` monitors Write/Edit tool calls during
IMPLEMENTATION state and BLOCKS attempts by main agent to create source files in task worktree.

**🚨 CRITICAL REQUIREMENT**: This hook MUST be registered in `.claude/settings.json` under `PreToolUse`
triggers to function. If hook is not registered, **NO PROTECTION EXISTS**.

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

**Scenario**: Stakeholder agent reports tool limitations, file size constraints, or inability to complete
assigned work.

**CORRECT RECOVERY SEQUENCE**:
1. **NEVER** bypass agent by implementing directly - this violates protocol regardless of reason
2. Reduce agent scope: Re-launch agent with smaller file subset or reduced requirements
3. Split work: Launch multiple agent instances with divided responsibilities
4. If persistent and unresolvable: Complete remaining IMPLEMENTATION rounds → transition to VALIDATION → then REVIEW → document limitation in agent reports → request user guidance during AWAITING_USER_APPROVAL checkpoint
5. Only after VALIDATION state begins: Main agent may fix if scope is truly mechanical (imports, whitespace)

**PROHIBITED PATTERNS**:
❌ Agent says "file too large" → Main agent implements directly
❌ Agent reports Edit tool limitation → Main agent takes over
❌ Agent cannot complete → Main agent "helps" by implementing in task worktree

**Recovery Rule**: Agent limitations change SCOPE or APPROACH, NEVER change WHO implements.

### Edit Tool Whitespace Mismatch Recovery

**Common Scenario**: Edit tool fails with 'old_string not found' despite text appearing correct.

**Root Cause**: Whitespace mismatches (tabs vs spaces, trailing spaces, line endings)

**Recovery Procedure**:
1. **Diagnose**: Read file section to see actual whitespace characters
2. **Identify Mismatch**: Compare visible text vs actual indentation
3. **Adapt**: Adjust old_string to match EXACT whitespace in file
4. **Common Issues**:
   - Tabs vs spaces: Check file uses consistent indentation
   - Trailing spaces: Include or exclude from old_string to match
   - Line number prefix in Read output: Never include line number prefix in old_string

**Prevention**: When reading file before Edit, note indentation style (tabs vs spaces) and preserve it exactly in old_string.

**Example Failure and Fix**:
```bash
# Read output shows (note the tab character after line number):
15→	public void method() {

# ❌ FAILS: old_string uses spaces but file has tab
Edit: old_string="    public void method()" # 4 spaces - doesn't match

# ✅ CORRECT: Use tab to match file
Edit: old_string="	public void method()" # 1 tab character - matches file

# Note: Line number "15→" and tab separator are NOT part of file content
# Only text AFTER the separator tab is the actual file content
```

**Verification Command**:
```bash
# If Edit fails, verify exact whitespace in file
cat -A /path/to/file.java | grep -A2 "method()"
# Shows ^I for tabs, $ for line endings, · for spaces
```

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
4. ✅ **Verify `git status` shows clean working directory before REVIEW transition**

**Rationale**: Uncommitted validation fixes will NOT be included in merge, causing build failures on main
branch.

**MANDATORY Pre-Transition Verification Checkpoint**:
```bash
# REQUIRED before transitioning VALIDATION → REVIEW
git status

# Expected output: "nothing to commit, working tree clean"
# If uncommitted changes exist → BLOCK transition, commit changes first
```

**State Transition Blocker**: Lock file state MUST NOT be updated to REVIEW if `git status` shows uncommitted changes.

**Verification Command**:
```bash
# Automated verification before state transition
if git status | grep -q "nothing to commit"; then
    echo "✅ Clean working directory - safe to transition to REVIEW"
    jq '.state = "REVIEW"' task.json > task.json.tmp && mv task.json.tmp task.json
else
    echo "❌ ERROR: Uncommitted changes exist - CANNOT transition to REVIEW"
    echo "Commit all validation fixes before proceeding"
    exit 1
fi
```

**Commit Timing During VALIDATION**:
- **RECOMMENDED**: Commit after each logical fix group (e.g., all style violations → commit, all missing imports → commit)
- **MINIMUM REQUIREMENT**: All fixes committed BEFORE running final `./mvnw verify` (step 3)
- **VERIFICATION**: `git status` must show clean working directory before transitioning to REVIEW

**Rationale**: Incremental commits prevent loss of work and make final verification easier to debug if failures occur.

**Example Workflow**:
```bash
# Fix style violations
Edit: FormattingRule.java (fix 5 violations)
git add FormattingRule.java
git commit -m "Fix checkstyle violations in FormattingRule"

# Fix missing imports
Edit: TransformationContext.java (add imports)
git add TransformationContext.java
git commit -m "Add missing imports to TransformationContext"

# Final verification on committed code
./mvnw verify

# Verify clean state before REVIEW
git status  # Should show "nothing to commit, working tree clean"
```

**Quality Gate Cache Verification**:

Before transitioning from VALIDATION → REVIEW, verify quality gates ACTUALLY EXECUTED (not cached):

```bash
# Option A: Disable cache explicitly
./mvnw verify -Dmaven.build.cache.enabled=false

# Option B: Verify build output shows execution
./mvnw verify 2>&1 | tee build.log
grep -q "Skipping plugin execution (cached)" build.log && {
  echo "❌ ERROR: Quality gates were cached, not executed"
  exit 1
}

# Option C: Check for actual analysis output
grep -q "PMD Failure\|Checkstyle violations" build.log || {
  echo "✅ Quality gates executed (found analysis output)"
}
```

**Cache Indicators to Watch For**:
- ❌ "Skipping plugin execution (cached)" → Quality gate NOT executed
- ❌ "Restored from cache" → Results may be stale
- ✅ "Running PMD analysis" → Fresh execution
- ✅ "Checking checkstyle" → Fresh execution

**See Also**: task-protocol-core.md § VALIDATION → REVIEW for detailed cache detection patterns

**Audit Integration**: If `/audit-session` detects uncommitted changes during VALIDATION state, this indicates
a protocol violation (Check 0.3 equivalent). Phase 5 automatic fix application will commit these changes, but
this represents a recovery action - the violation still occurred. Future prevention requires discipline during
VALIDATION → REVIEW transition.

**See Also**: `.claude/commands/audit-session.md` § Phase 5: Automatic Fix Application for audit-time
detection and automatic remediation

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

**'Mechanical' Fix Definition** (objective criteria):

A fix is 'mechanical' if ALL criteria met:
1. ✅ No domain expertise required (any competent programmer could do it)
2. ✅ No algorithmic or architectural decisions involved
3. ✅ Change is fully determined by error message or tool output
4. ✅ No testing logic changes (assertion updates are NOT mechanical)
5. ✅ IDE could auto-fix it (e.g., 'Add import', 'Remove unused variable')

**Examples**:
- ✅ Mechanical: Add missing `import java.util.List;` (determined by compilation error)
- ✅ Mechanical: Remove unused variable `int x = 5;` (checkstyle/PMD flagged it)
- ✅ Mechanical: Fix whitespace/indentation (style tool flagged exact location)
- ❌ NOT Mechanical: Fix failing test assertion (requires understanding test intent)
- ❌ NOT Mechanical: Resolve checkstyle violation by refactoring method (design decision)
- ❌ NOT Mechanical: Add null check to prevent NPE (requires understanding control flow)

**After ALL agents report COMPLETE status, main agent discovers issues during VALIDATION:**

```
IF (issue_type == "compilation_error"):
    IF (simple_fix like missing_import OR unused_variable):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to architecture-updater agent

ELSE IF (issue_type == "style_violation"):
    IF (count <= 5 violations total across all files AND fixes are mechanical like whitespace/imports):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to style-updater agent

ELSE IF (issue_type == "test_failure"):
    IF (simple_fix like assertion_update):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to test-updater agent

ELSE IF (issue_type == "architecture_issue" OR "security_issue"):
    ❌ ALWAYS re-delegate to appropriate domain expert
```

**Efficiency Rationale**: Re-launching agents for trivial fixes wastes 50-100 messages per round. Direct fixes
for mechanical issues preserve protocol safety while maintaining efficiency.

**Threshold Rationale**: The '5 violation' threshold represents message cost efficiency:
- Auto-fixing 5 violations: ~2-3 tool calls = 3-5 messages
- Agent delegation: ~50-100 messages per round
- Breakeven: When manual fixing approaches agent launch cost

**Counting Rule**: Total violations across ALL files, ALL types combined.

**Counting Rule Emphasis**: The threshold is 5 violations TOTAL across the ENTIRE task, NOT 5 per file. Even if violations are spread across 10 files (1 violation each for 10 files = 10 total), this EXCEEDS threshold and requires delegation.

**Clarification Examples**:
- File A: 3 missing imports, File B: 2 whitespace → Total 5 → Main agent fixes ✅
- File A: 3 missing imports, File B: 3 whitespace → Total 6 → Delegate to agent ❌
- Files A-J: 50 violations, all same missing import statement → Treat as 1 pattern → Main agent fixes ✅

**Override Rule**: If ALL violations are identical pattern (e.g., 10 missing imports, all same import
statement), treat as mechanical regardless of count.

**Safety Constraint**: Only apply after unanimous agent completion. During IMPLEMENTATION state, ALL fixes
must go through agents.

### Re-delegation Workflow (When Violations Exceed Threshold)

When main agent determines fixes require agent delegation (count > 5, complexity, or domain expertise):

**Option A: Iterative Round Pattern** (PREFERRED for < 20 violations):
1. Remain in VALIDATION state
2. Launch specific agent with targeted fix request via Task tool
3. Agent fixes in their worktree, merges to task branch
4. Main agent re-runs `./mvnw verify` to confirm fixes
5. If new issues discovered, repeat decision tree
6. Proceed to REVIEW when all quality gates pass

**Option B: Return to IMPLEMENTATION** (for extensive issues > 20 violations or architectural problems):
1. Update lock state back to IMPLEMENTATION
2. Document issues found in task.md
3. Launch agents for full re-implementation round
4. Follow standard IMPLEMENTATION → VALIDATION flow
5. Proceed to REVIEW after validation passes

**Decision Criteria**:
- Fixes are isolated and mechanical → Option A (iterative)
- Fixes require architectural changes or affect multiple files extensively → Option B (return to IMPLEMENTATION)

**Example - Option A (Iterative)**:
```markdown
# During VALIDATION, found 12 style violations
# Decision: Exceeds threshold (5), delegate to style-updater

Task tool (style-updater): "Fix 12 style violations in FormattingRule.java:
- Lines 45-52: Missing JavaDoc on public methods
- Lines 78-83: SeparatorWrap violations (closing parentheses)
- Line 102: Unused import statement

Please fix in your worktree, validate with checkstyle, and merge to task branch."

# Wait for agent completion, then verify
./mvnw verify
```

**Example - Option B (Return to IMPLEMENTATION)**:
```markdown
# During VALIDATION, found architectural issues:
# - FormattingRule violates single responsibility
# - Missing security integration in 3 classes
# - Test coverage < 80%

# Decision: Extensive architectural changes needed

# Update lock state
jq '.state = "IMPLEMENTATION"' /workspace/tasks/{task-name}/task.json

# Document in task.md and launch full round
Task tool (architecture-updater): "Address architectural issues found during validation..."
```

## 🚨 TASK PROTOCOL SUMMARY {#task-protocol}

**Full Protocol Details**: See [task-protocol-core.md](docs/project/task-protocol-core.md) and
[task-protocol-operations.md](docs/project/task-protocol-operations.md) for complete state machine and
transition requirements.

**State Machine**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW →
AWAITING_USER_APPROVAL → COMPLETE → CLEANUP

**Critical Requirements for All Tasks**:
- Lock acquisition (see [§ Lock Ownership](#lock-ownership))
- Worktree isolation (see [§ Worktree Isolation](#worktree-isolation))
-  **🚨 IMPLEMENTATION DELEGATION**: Main agent COORDINATES via Task tool - stakeholder agents IMPLEMENT in
  their worktrees (see [§ Implementation Role Boundaries](#implementation-role-boundaries))
- Build verification before merge
- Unanimous stakeholder approval (REVIEW state)
- Complete all states before selecting new task

**Risk-Based Variants**:
- **HIGH-RISK** (src/\*\*, pom.xml, security/\*\*): Full protocol with all agents
- **MEDIUM-RISK** (tests, docs): Abbreviated protocol, domain-specific agents
- **LOW-RISK** (general docs): Streamlined protocol, minimal validation

**Post-Compaction Note**: This summary plus lock ownership and worktree isolation rules provide EMERGENCY
FALLBACK guidance when task-protocol files are not accessible. For complete protocol compliance, the full
protocol files (task-protocol-core.md and task-protocol-operations.md) are REQUIRED and should be re-loaded as
soon as possible.

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

**CRITICAL**: Once you begin a task (execute INIT state), you MUST complete ALL protocol states autonomously,
WITH MANDATORY USER APPROVAL CHECKPOINTS.

**TERMINOLOGY CLARIFICATION - "Autonomous Completion"**:
- **"Autonomous"** means: Agent completes work WITHOUT asking for help, guidance, or direction between checkpoints
- **"Checkpoints"** are: Planned pauses built INTO the protocol state machine for user oversight
- **"Autonomous" DOES NOT mean**: No user interaction whatsoever
- **"Autonomous" DOES mean**: No mid-protocol abandonment, no "what should I do?" questions, no giving up

**What Autonomous Completion Requires**:
✅ Complete REQUIREMENTS without asking "should I continue?"
✅ Complete SYNTHESIS without asking "how should I plan this?"
✅ Complete IMPLEMENTATION without asking "what should I do about this error?"
✅ Complete VALIDATION without asking "should I fix this or delegate?"
✅ **PAUSE at SYNTHESIS for PLAN APPROVAL checkpoint** (this is EXPECTED, not a violation)
✅ **PAUSE at REVIEW for CHANGE REVIEW checkpoint** (this is EXPECTED, not a violation)

**What Violates Autonomous Completion**:
❌ Stopping mid-state to ask "what should I do next?"
❌ Abandoning task due to complexity without exhausting recovery options
❌ Asking user to make implementation decisions that agents should make
❌ Giving up on problems that can be solved through systematic debugging
❌ Requesting user intervention for issues that protocol already defines recovery procedures for

**MANDATORY SINGLE-SESSION COMPLETION**:
- Task execution occurs in ONE uninterrupted session
- **EXPECTED USER APPROVAL CHECKPOINTS** (these are NOT violations of autonomous completion):
  1. **PLAN APPROVAL**: After SYNTHESIS, user approves implementation plan before IMPLEMENTATION begins
  2.  **CHANGE REVIEW**: After REVIEW (unanimous approval), enter AWAITING_USER_APPROVAL state until user
     confirms
- NO OTHER HANDOFFS to user mid-protocol
- Complete all other states autonomously (without asking for help)

**When to Ask User**:
✅ **BEFORE** starting task: "Task X has ambiguous requirements. Clarify before I begin?"
✅ **AFTER SYNTHESIS**: Present plan for approval before IMPLEMENTATION
✅ **AFTER REVIEW**: Present changes, wait for approval before COMPLETE
✅ **NEVER** at other points: Complete other states autonomously once INIT begins

**Plan Presentation Format** (after SYNTHESIS):
- Present implementation plan in clear, readable format (use markdown)
- Include: architecture approach, files to modify, implementation sequence, testing strategy
- Wait for explicit user approval before proceeding to IMPLEMENTATION

**Interruption Type Classification**:

Understanding when to stop, when to continue, and what constitutes a violation:

**EXPECTED Pauses** (built into protocol, NOT violations):
1. **PLAN APPROVAL Checkpoint**: After SYNTHESIS, before IMPLEMENTATION
   - Agent presents complete implementation plan
   - Agent waits for user approval
   - Lock state remains SYNTHESIS
   - This is EXPECTED protocol behavior

2. **CHANGE REVIEW Checkpoint**: After REVIEW, before COMPLETE
   - Agent presents committed changes with SHA
   - Agent waits for user approval
   - Lock state: AWAITING_USER_APPROVAL
   - This is EXPECTED protocol behavior

**PERMITTED Interruptions** (agent handles, continues work):
1. **User Questions During Work**: User asks for status, clarification, or explanation
   - Agent answers the question
   - Agent resumes work from same state
   - Lock state preserved throughout
   - NOT a violation of autonomous completion

2. **User Commands During Work**: User runs /audit-session or other diagnostic commands
   - Agent executes the command
   - Agent returns to active work state
   - Lock state preserved throughout
   - NOT a violation of autonomous completion

**ACCEPTABLE Stops** (genuine blockers requiring stop):
1. **Genuine External Blocker**: API unavailable, missing credentials, network failure
   - External dependency cannot be resolved by agent
   - Work cannot proceed without external resource
   - Agent must stop and escalate to user

2. **Ambiguous Conflicting Requirements**: No resolution path exists
   - Requirements contain unresolvable contradictions
   - Agent exhausted all conflict resolution attempts
   - User clarification required before continuing

3. **User Explicit Stop Command**: User says "stop" or "halt"
   - User explicitly commands task termination
   - Agent must stop immediately
   - Task may need to be resumed or abandoned

**VIOLATION Patterns** (prohibited stops):
❌ **Agent Giving Up**: "This is too hard, let me try something simpler"
❌ **Premature Help-Seeking**: "I don't know what to do next"
❌ **Abandonment**: "I'll leave this for the user to decide"
❌ **Complexity Avoidance**: "Due to complexity, I'll skip this"
❌ **Mid-State Handoffs**: "Should I continue with IMPLEMENTATION?"

**Decision Tree: Should I Stop?**
```
IF (at SYNTHESIS checkpoint OR at AWAITING_USER_APPROVAL checkpoint):
    → STOP and wait for approval ✅ EXPECTED

ELSE IF (user asked question OR user ran command):
    → Answer/execute, then CONTINUE from same state ✅ PERMITTED

ELSE IF (genuine external blocker OR unresolvable conflict):
    → STOP and escalate to user ✅ ACCEPTABLE

ELSE IF (user said "stop" explicitly):
    → STOP immediately ✅ ACCEPTABLE

ELSE IF (complexity/uncertainty/difficulty):
    → CONTINUE, apply recovery procedures ❌ DO NOT STOP

ELSE:
    → CONTINUE autonomously ✅ DEFAULT
```

**Enforcement**: The `detect-giving-up.sh` hook detects mid-protocol abandonment patterns and injects
completion reminders.

## 🚨 USER APPROVAL CHECKPOINT ENFORCEMENT

**HOOK-ENFORCED REQUIREMENT**: The `enforce-user-approval.sh` hook will BLOCK transitions to COMPLETE state if
user approval checkpoint is not satisfied.

**Approval Marker System**:
- **File**: `/workspace/tasks/{task-name}/user-approval-obtained.flag`
- **Created**: When user provides explicit approval after REVIEW state
- **Required**: Before COMPLETE state transition is allowed
- **Removed**: Automatically during CLEANUP state

**Approval Flag Persistence Across Context Boundaries**:

**CRITICAL**: Approval flags are PERSISTENT across context compaction and session resumption.

**Persistence Guarantees**:
1. **File-Based Storage**: Flags stored as files in `/workspace/tasks/{task-name}/` directory
2. **Filesystem Persistence**: Files persist across context compaction (memory cleared, filesystem unchanged)
3. **Session Independence**: Flags remain valid across session boundaries
4. **Checkpoint Isolation**: Each checkpoint has separate flag file

**Flag Locations**:
- **PLAN APPROVAL**: `/workspace/tasks/{task-name}/user-plan-approval-obtained.flag`
- **CHANGE REVIEW**: `/workspace/tasks/{task-name}/user-approval-obtained.flag`

**Session Summary Documentation Requirements for Approval Checkpoints**:

When documenting approval checkpoints in session summaries, MUST include:

1. **Checkpoint Identification**: Which checkpoint was reached (PLAN APPROVAL or CHANGE REVIEW)
2. **Presentation Evidence**: Message number where checkpoint content was presented to user
3. **User Response**: Exact user message that constituted approval
4. **Flag Creation Confirmation**: Verification that approval flag file was created
5. **State After Approval**: Lock file state after approval obtained

**Example Session Summary Entry**:
```markdown
## PLAN APPROVAL Checkpoint

**Checkpoint Reached**: Message 25 (after SYNTHESIS state)
**Plan Presented**: Implementation plan shown to user with:
- Architecture approach
- Files to modify
- Implementation sequence
- Testing strategy

**User Response**: Message 26 - "Looks good, please proceed with implementation"
**Approval Flag**: Created at /workspace/tasks/my-task/user-plan-approval-obtained.flag
**State Transition**: SYNTHESIS → IMPLEMENTATION (lock file updated)
**Timestamp**: 2025-10-19T14:32:00Z
```

**Rationale**: Documented approval checkpoints provide audit trail showing protocol compliance with mandatory user oversight requirements.

**Context Compaction Scenarios**:

**Scenario 1: Approval Before Compaction**
```
Time T1: User approves plan → Flag created
Time T2: Context compaction occurs
Time T3: Session resumes → Flag STILL EXISTS
Result: ✅ Approval preserved, proceed to IMPLEMENTATION
```

**Scenario 2: Compaction During Checkpoint Wait**
```
Time T1: Agent waiting at PLAN APPROVAL checkpoint
Time T2: Context compaction occurs
Time T3: Session resumes → Lock state = SYNTHESIS, no flag
Result: ✅ Agent re-presents plan, waits for approval
```

**Scenario 3: Multiple Compactions Between Checkpoints**
```
Time T1: User approves plan → Flag created
Time T2-T10: Multiple context compactions during IMPLEMENTATION
Time T11: Agent reaches CHANGE REVIEW checkpoint
Result: ✅ PLAN APPROVAL flag ignored (different checkpoint), wait for CHANGE REVIEW approval
```

**Session Resumption Behavior**:

When resuming after context compaction or crash:

**Check 1: Verify Current State**
```bash
CURRENT_STATE=$(jq -r '.state' /workspace/tasks/{task-name}/task.json)
```

**Check 2: Check Approval Flags**
```bash
PLAN_APPROVAL_FLAG="/workspace/tasks/{task-name}/user-plan-approval-obtained.flag"
CHANGE_APPROVAL_FLAG="/workspace/tasks/{task-name}/user-approval-obtained.flag"

if [ "$CURRENT_STATE" = "SYNTHESIS" ]; then
    if [ -f "$PLAN_APPROVAL_FLAG" ]; then
        echo "✅ PLAN APPROVAL already obtained - proceeding to IMPLEMENTATION"
        # Do NOT re-present plan
        # Do NOT wait for approval again
    else
        echo "⏳ PLAN APPROVAL pending - re-presenting plan to user"
        # Re-present plan
        # Wait for approval
    fi
elif [ "$CURRENT_STATE" = "AWAITING_USER_APPROVAL" ]; then
    if [ -f "$CHANGE_APPROVAL_FLAG" ]; then
        echo "✅ CHANGE REVIEW approval already obtained - proceeding to COMPLETE"
        # Do NOT re-present changes
        # Do NOT wait for approval again
    else
        echo "⏳ CHANGE REVIEW pending - re-presenting changes to user"
        # Re-present changes
        # Wait for approval
    fi
fi
```

**Flag Creation Timing**:

## Approval Persistence Pattern

**Problem**: Context compaction can lose approval state, causing duplicate approval requests.

**Solution**: Create persistent approval flag files

**After obtaining approval**:
```bash
# Detect approval keywords
if [[ "$USER_MESSAGE" =~ (yes|approved|proceed|LGTM|go ahead) ]]; then
  touch /workspace/tasks/{task-name}/user-plan-approval-obtained.flag
fi
```

**On session resumption** (check before re-requesting approval):
```bash
if [ -f /workspace/tasks/{task-name}/user-plan-approval-obtained.flag ]; then
  echo "Approval already obtained - proceeding without re-request"
else
  echo "Present plan and wait for approval"
fi
```

**Cleanup**: Remove flags during CLEANUP state only

**PLAN APPROVAL Flag**:
- **Created**: Immediately after user provides plan approval message
- **Created By**: Main agent (after detecting approval keywords)
- **Location**: `/workspace/tasks/{task-name}/user-plan-approval-obtained.flag`
- **Content**: Empty file (existence is the signal)

**CHANGE REVIEW Flag**:
- **Created**: Immediately after user provides change review approval message
- **Created By**: Main agent (after detecting approval keywords)
- **Location**: `/workspace/tasks/{task-name}/user-approval-obtained.flag`
- **Content**: Empty file (existence is the signal)

**Flag Validation**:
```bash
# Verify flag was created after approval
create_approval_flag() {
    local FLAG_PATH=$1
    local CHECKPOINT_NAME=$2

    # Create flag file
    touch "$FLAG_PATH"

    # Verify creation
    if [ -f "$FLAG_PATH" ]; then
        echo "✅ $CHECKPOINT_NAME approval flag created successfully"
        return 0
    else
        echo "❌ CRITICAL: Failed to create $CHECKPOINT_NAME approval flag"
        return 1
    fi
}

# Example usage
create_approval_flag "/workspace/tasks/my-task/user-plan-approval-obtained.flag" "PLAN APPROVAL"
```

**Edge Cases**:

**Edge Case 1: Flag Exists But Lock State Doesn't Match**
```
Lock state: IMPLEMENTATION
Plan approval flag: EXISTS

Interpretation: Approval was obtained, agent has progressed past SYNTHESIS
Action: Continue with IMPLEMENTATION (flag no longer relevant)
```

**Edge Case 2: Flag Missing But State Advanced**
```
Lock state: IMPLEMENTATION
Plan approval flag: MISSING

Interpretation: Either (1) flag was manually deleted, or (2) protocol violation
Action: Assume approval was obtained (state already advanced), continue work
Warning: Log potential protocol violation for audit
```

**Edge Case 3: Both Flags Exist**
```
Plan approval flag: EXISTS
Change review flag: EXISTS
Lock state: COMPLETE

Interpretation: Both checkpoints passed, task completing
Action: Proceed with COMPLETE → CLEANUP transition
```

**Edge Case 4: Session Crash Between Approval and Flag Creation**
```
Time T1: User says "approved"
Time T2: Session crashes BEFORE flag created
Time T3: Session resumes → No flag exists

Interpretation: Approval occurred but not recorded
Action: Re-present checkpoint for approval (safer to re-ask)
Rationale: Cannot verify approval without flag
```

**Flag Cleanup**:

**CRITICAL**: Flags are ONLY removed during CLEANUP state, never during task execution.

**Cleanup Procedure**:
```bash
# During CLEANUP state only
TASK_NAME="my-task"
rm -f "/workspace/tasks/${TASK_NAME}/user-plan-approval-obtained.flag"
rm -f "/workspace/tasks/${TASK_NAME}/user-approval-obtained.flag"

# Verify removal
[ ! -f "/workspace/tasks/${TASK_NAME}/user-plan-approval-obtained.flag" ] || echo "ERROR: Flag not removed"
[ ! -f "/workspace/tasks/${TASK_NAME}/user-approval-obtained.flag" ] || echo "ERROR: Flag not removed"
```

**Why Flags Never Expire**:
1. **No Time Limit**: Approval valid indefinitely until task completes
2. **User Intent**: User approved specific plan/changes, intent doesn't expire
3. **Session Independence**: Approval not tied to specific session
4. **Resumption Safety**: Allows safe resumption after long delays

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

**CRITICAL**: When user requests "work on the next task" or similar, verify task availability before starting
work.

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

## 🎯 LONG-TERM SOLUTION PERSISTENCE

**MANDATORY PRINCIPLE**: Prioritize optimal long-term solutions over expedient alternatives. Persistence and
thorough problem-solving are REQUIRED.

### 🚨 CRITICAL PERSISTENCE REQUIREMENTS

**SOLUTION QUALITY HIERARCHY**:
1. **OPTIMAL SOLUTION**: Complete, maintainable, follows best practices, addresses root cause
2. **ACCEPTABLE SOLUTION**: Functional, meets core requirements, minor technical debt acceptable
3.  **EXPEDIENT WORKAROUND**: Quick fix, creates technical debt, only acceptable with explicit justification
   and follow-up task

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
❌ "Due to complexity and token usage, I'll create a solid MVP implementation" (complexity/tokens never justify
incomplete implementation)
❌ "Given token constraints, I'll implement a basic version" (token budget does not override quality
requirements)
❌ "This edge case is too hard to handle properly" (without stakeholder consultation)
❌ "The existing pattern is suboptimal but I'll follow it" (without improvement attempt)

### 🚨 GIVING UP DETECTION PATTERNS

**Hook**: `detect-giving-up.sh` detects abandonment patterns

**Response**: Return to original problem, apply systematic debugging, exhaust approaches before scope
modification

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
**Task Worktrees**: `/workspace/tasks/{task-name}/code/` (isolated per task protocol, common merge target for
all agents)
**Agent Worktrees**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/` (per-agent development
isolation)
**Locks**: Multi-instance coordination via lock files at `/workspace/tasks/{task-name}/task.json`

**Multi-Agent Architecture**:
- **WHO IMPLEMENTS**: Stakeholder agents (NOT main agent) write all source code
- **WHERE**: Each stakeholder agent has own worktree: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
- **MAIN AGENT ROLE**: Coordinates via Task tool invocations, monitors status.json, manages state transitions
-  **IMPLEMENTATION FLOW**: Main agent delegates → Agents implement in parallel → Agents merge to task branch
  → Iterative rounds until complete
- **VIOLATION**: Main agent creating .java/.ts/.py files directly in task worktree during IMPLEMENTATION state

## 🔧 CONTINUOUS WORKFLOW MODE

Override system brevity for comprehensive multi-task automation via Task Protocol.

**Trigger**: `"Work on the todo list in continuous mode."`
**Auto-Detection**: "todo list", "all tasks", "continuously", "CONTINUOUS WORKFLOW MODE"
**Effects**: Detailed output, automatic task progression, full stakeholder analysis, comprehensive TodoWrite
tracking

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
- **Batch**: Collect ALL agent feedback from current round before fixing anything (avoid fix-one-verify-one within same round)
- **Prefetch**: Load protocol files + predicted sources in INIT phase

**Batch Fixing Scope Clarification**: "Batch fixing" means collect all feedback from the CURRENT implementation round, fix all issues together, then verify once. It does NOT mean waiting for ALL rounds to complete before fixing anything. Multiple iterative rounds (each with batch fixing) are acceptable.

**Detection Hook**: `/workspace/.claude/hooks/detect-sequential-tools.sh`

### Fail-Fast vs Batch Collection Decision Criteria

**Use FAIL-FAST (stop on first error)**:
- Compilation errors (blocking all other checks)
- JPMS module resolution failures
- Missing dependencies (cannot proceed without)
- Architecture violations detected by build-reviewer

**Use BATCH COLLECTION (collect all, fix together)**:
- Style violations (checkstyle, PMD)
- Test failures (non-blocking)
- Documentation issues
- Any violation count >5 of same type

### Protocol File Prefetching Pattern

**Optimization**: Load all protocol files during INIT phase instead of discovering need mid-task.

❌ **Anti-Pattern (Discovered Late - wastes 10,000-20,000 tokens)**:
```markdown
Message 1 (INIT): Create worktrees
Message 10 (REQUIREMENTS): Oh, I need task-protocol-core.md
Message 11: Read task-protocol-core.md
Message 25 (SYNTHESIS): Oh, I need task-protocol-operations.md
Message 26: Read task-protocol-operations.md
```

✅ **Required Pattern (Predictive Prefetch)**:
```markdown
Message 1 (INIT - parallel prefetch):
  Bash: git worktree add /workspace/tasks/my-task/code -b my-task
  Read /workspace/main/docs/project/task-protocol-core.md
  Read /workspace/main/docs/project/task-protocol-operations.md
  Read /workspace/main/CLAUDE.md --offset=140 --limit=300  # Implementation boundaries section
Message 2: Now have all protocol context, proceed with REQUIREMENTS
```

**Files to Prefetch During INIT**:
- `task-protocol-core.md` - State definitions, transitions, requirements
- `task-protocol-operations.md` - Operational patterns, examples
- `CLAUDE.md` sections relevant to task type (Implementation boundaries, Performance patterns)
- `code-style-human.md` - If style work expected (formatting rules, new classes)
- Task-specific domain docs if known upfront

**Impact**: Saves 5-10 round-trips × 2000 tokens each = **10,000-20,000 token savings per task**

### Incremental Validation Frequency

**Component Boundaries** (validate at these points):
- After each complete interface/class (NOT after each method)
- After each test class with full test coverage
- After each logical module (group of related classes)

**Validation Commands**:
```bash
./mvnw compile -q              # Quick syntax check
./mvnw checkstyle:check -q     # Style validation
./mvnw test -Dtest=ClassName   # Single test class
```

**Avoid Over-Validation**: Do NOT validate after each method (too granular, wastes time)
**Avoid Under-Validation**: Do NOT wait until all 23 files complete (late error detection)

**Component Boundary Definition** (for Fail-Fast Validation):

**Component Boundaries** (validate after each):
- ✅ Each new source file created → validate compilation
- ✅ Each new test class added → validate tests pass
- ✅ Each interface/API contract defined → validate compiles, no conflicts
- ✅ Each dependency added to pom.xml → validate resolves

**NOT Component Boundaries** (don't validate after each):
- ❌ Each method added within same class (wait for class complete)
- ❌ Each import statement (wait for file complete)
- ❌ Each line of code (excessive)

**Example Incremental Validation**:
```bash
# Agent creates FormatterApi.java
cd /workspace/tasks/my-task/code
./mvnw compile  # Validate: compiles

# Agent creates FormatterApiImpl.java
./mvnw compile  # Validate: still compiles, no conflicts

# Agent creates TestFormatterApi.java
./mvnw test     # Validate: tests pass

# All components done
./mvnw verify   # Final validation: all quality gates
```

**Rationale**: Validate at logical completion points to catch integration issues early while avoiding excessive build cycles.

### Parallel Execution Enforcement

**CRITICAL REQUIREMENT**: Parallel execution is MANDATORY, not optional. Sequential patterns waste significant tokens and violate performance requirements.

**ENFORCEMENT**:
- ❌ **VIOLATION**: Launching agents sequentially (separate Task tool calls across multiple messages)
- ✅ **REQUIRED**: Launch all independent agents in SINGLE message using parallel Task tool invocations
- **Hook**: `detect-sequential-tools.sh` warns on sequential patterns
- **Audit Check**: Protocol auditor verifies parallel launch pattern during IMPLEMENTATION → VALIDATION transition

**Examples**:

❌ **PROHIBITED (Sequential - wastes 8000+ tokens)**:
```markdown
Message 1: Task tool (architecture-updater) with implementation instructions
[Wait for response]
Message 2: Task tool (style-updater) with review instructions
[Wait for response]
Message 3: Task tool (quality-updater) with quality check
```

✅ **REQUIRED (Parallel - efficient)**:
```markdown
Single Message:
Task tool (architecture-updater): "Implement FormattingRule interfaces..."
Task tool (style-updater): "Review implementation for style compliance..."
Task tool (quality-updater): "Audit code quality and design patterns..."
```

**When Parallel Execution is NOT Appropriate**:

❌ **Dependent Operations** (MUST be sequential):
- Git operations: commit THEN push (cannot parallel)
- File operations: Read file THEN Edit based on content
- Build verification: Implement THEN compile THEN test
- State transitions: Complete current state THEN transition to next

❌ **Stateful Tools** (require sequential ordering):
- Bash with directory changes: `cd /path && command` (cannot split across messages)
- Edit: Multiple edits to same file (race conditions possible)
- Write: Creating file THEN editing it

✅ **Safe for Parallel**:
- Reading multiple unrelated files
- Launching independent stakeholder agents
- Fetching multiple web pages
- Searching multiple directories
- Grepping multiple file patterns

**Rule of Thumb**: If operation B depends on result of operation A, use sequential. If operations are independent, use parallel.

### Combining Parallel and Sequential Operations

**Pattern**: When workflow has both parallel and sequential components, maximize parallelism within each message while maintaining necessary sequencing between messages.

**Pattern A: Sequential Messages with Parallel Within**
```markdown
Message 1 (Parallel reads):
  Read file1.java
  Read file2.java
  Read file3.java

Message 2 (Sequential - process results from Message 1):
  Edit file1.java based on content

Message 3 (Parallel again - independent fixes):
  Edit file2.java
  Edit file3.java
```

**Pattern B: Maximize Parallelism Per Message**
```markdown
Message 1 (Setup phase):
  Bash: cd /workspace/tasks/my-task/code
  Read pom.xml
  Read README.md

Message 2 (Parallel implementation):
  Task (architecture-updater): "Implement FormattingRule..."
  Task (style-updater): "Review for style compliance..."
  Task (quality-updater): "Audit design patterns..."

Message 3 (Verification - sequential after Message 2):
  Bash: ./mvnw verify
```

**Key Principle**: Within each message, maximize parallelism. Between messages, maintain necessary sequencing.

