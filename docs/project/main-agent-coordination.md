# Main Agent Coordination Guide

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** Main coordination agent only
> **Purpose:** Task protocol, lock management, approval checkpoints, and coordination patterns

**Sub-agents**: Read `task-protocol-agents.md` instead.

## 🚨 SOURCE CODE CREATION DECISION TREE {#source-code-creation}

**Critical Rule**: Main agent role boundaries determine when you can create source files.

```
Need to create/modify source files (.java/.ts/.py)?
│
├─ Current state: IMPLEMENTATION?
│  ├─ YES → ❌ STOP: Main agent CANNOT create source files
│  │         ✅ CORRECT: Delegate to stakeholder agents via Task tool
│  │         Each agent works in: /workspace/tasks/{task}/agents/{agent}/code/
│  │
│  └─ NO → Check state:
│     ├─ INIT/CLASSIFIED: ✅ Main agent can create build/config files ONLY
│     ├─ POST_IMPLEMENTATION: ✅ Main agent can merge and fix integration issues
│     └─ CLEANUP: ✅ Main agent can finalize and commit
│
└─ File type: Configuration/Documentation?
   └─ YES → ✅ Main agent can modify in any state
      (CLAUDE.md, .claude/*, docs/project/*, pom.xml, README.md)
```

**Pre-Implementation Checklist** (MANDATORY before IMPLEMENTATION state):

Before ANY implementation work, complete ALL steps in order:

**Step 1: State Tracking Infrastructure**
- [ ] task.json created at `/workspace/tasks/{task-name}/task.json` with state=IMPLEMENTATION
- [ ] Verification: `cat /workspace/tasks/{task-name}/task.json` shows correct state

**Step 2: Agent Worktree Setup**
- [ ] All stakeholder agent worktrees created: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
- [ ] Agent worktree branches exist: {task-name}-{agent-name}
- [ ] Agent worktrees verified with: `git worktree list | grep {task-name}`
- [ ] Verification: `ls -la /workspace/tasks/{task-name}/agents/` shows all required agents

**Step 3: Coordination & Boundaries**
- [ ] Task coordination plan documented in task.md
- [ ] Main agent role boundaries reviewed (NO source file creation - use Task tool to delegate)
- [ ] Agent requirements prepared for Task tool invocation

**Step 4: Pre-Flight Validation**
- [ ] Main agent has NOT used Write/Edit on any .java/.ts/.py files
- [ ] task.json exists (blocks violations if missing)
- [ ] Agent worktrees exist before invoking agents

**If ANY checkbox unchecked → STOP and complete setup before proceeding**

**Standard Implementation Workflow Template**:
```bash
# Step 1: Create task worktree
git worktree add /workspace/tasks/{task-name}/code -b {task-name}
cd /workspace/tasks/{task-name}/code

# Step 2: Initialize task.json (REQUIRED before source file work)
cat > /workspace/tasks/{task-name}/task.json <<EOF
{
  "task_name": "{task-name}",
  "state": "IMPLEMENTATION",
  "created": "$(date -Iseconds)"
}
EOF

# Step 3: Create agent worktrees
mkdir -p /workspace/tasks/{task-name}/agents
git worktree add /workspace/tasks/{task-name}/agents/architect/code \
  -b {task-name}-architect
git worktree add /workspace/tasks/{task-name}/agents/quality/code \
  -b {task-name}-quality

# Step 4: Invoke agents (NOT implement directly)
Task tool: architect
  requirements: "..."
  worktree: /workspace/tasks/{task-name}/agents/architect/code

# Step 5: Merge agent work after completion
cd /workspace/tasks/{task-name}/code
git merge {task-name}-architect
```

**Worktree Creation Helper**:
```bash
source /workspace/main/.claude/hooks/lib/worktree-manager.sh
create_agent_worktree "{task-name}" "{agent-name}"
```

## 🚨 LOCK OWNERSHIP & TASK RECOVERY {#lock-ownership}

After context compaction, `check-lock-ownership.sh` SessionStart hook checks for active tasks owned by this session. Hook enforces user approval checkpoints - if task is in SYNTHESIS or AWAITING_USER_APPROVAL state, hook displays checkpoint-specific guidance that MUST be followed.

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

### During Task Execution {#during-task-execution}

After creating worktree, IMMEDIATELY `cd` to worktree directory BEFORE other operations.

**Required Pattern**:
```bash
git worktree add /workspace/tasks/{task-name}/code -b {task-name} && cd /workspace/tasks/{task-name}/code
```

ALL subsequent work must occur inside worktree, NEVER in main branch.

### During Cleanup (CLEANUP State) {#during-cleanup-cleanup-state}

BEFORE removing worktree, MUST `cd` to main worktree.

**Required Pattern**:
```bash
cd /workspace/main && git worktree remove /workspace/tasks/{task-name}/code
```

NEVER remove a worktree while inside it - shell loses working directory.

## 🚨 IMPLEMENTATION ROLE BOUNDARIES {#implementation-role-boundaries}

During IMPLEMENTATION state, main agent and stakeholder agents have STRICTLY SEPARATED roles.

### Terminology Definitions {#terminology-definitions}

- **Implementation**: Creating new features, classes, methods, or significant logic
  - Writing FormattingRule.java, implementing algorithm logic, adding new methods
  - During IMPLEMENTATION state: ONLY stakeholder agents perform implementation

- **Coordination**: Managing agent invocations, monitoring status, updating state
  - Launching agents via Task tool, checking status.json, updating lock file
  - Main agent role during IMPLEMENTATION state

- **Minor Fixes**: Mechanical corrections to code after implementation complete
  - Adding missing imports, fixing whitespace, removing unused variables, fixing typos
  - Main agent MAY perform after VALIDATION state begins
  - NOT considered 'implementation' because no new logic or features

- **Fixes** (during IMPLEMENTATION): Corrections to agent-written code
  - Fixing bugs in newly written classes, adjusting architecture
  - During IMPLEMENTATION state: ONLY original agent fixes their own work
  - Main agent NEVER fixes during IMPLEMENTATION state

**Rule of Thumb**: If it requires domain expertise or significant changes → delegate to agents. If it's
mechanical and trivial → main agent may fix after VALIDATION state begins.

**Coordination (PERMITTED during IMPLEMENTATION)**:
✅ Launching architect agent: "Implement FormatterApi interface with transform() method"
✅ Monitoring agent status: Reading `/workspace/tasks/add-api/agents/architect/status.json`
✅ Updating lock file state: `jq '.state = "VALIDATION"' task.json`
✅ Creating task infrastructure: Writing `/workspace/tasks/add-api/task.md` with requirements

**Implementation (PROHIBITED during IMPLEMENTATION state)**:
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

Technical-architect implements new integration tests requiring Mockito → agent may add `mockito-core` with test scope in their worktree.

### Main Agent Role During IMPLEMENTATION State (COORDINATION ONLY) {#main-agent-role-during-implementation-state-coordination-only}

**PERMITTED Actions**:
✅ Launch stakeholder agents via Task tool with implementation instructions
✅ Monitor agent status.json files for completion signals
✅ Update lock file state transitions
✅ Coordinate iterative validation rounds
✅ Determine when all agents report work complete

**ABSOLUTELY PROHIBITED Actions**:
❌ Use **Write tool** to create source files (.java, .ts, .py, etc.) in task worktree
❌ Use **Edit tool** to modify source files (.java, .ts, .py, etc.) in task worktree
❌ Use **Edit tool** to modify module-info.java (JPMS module descriptors)
❌ Use **Edit tool** to modify pom.xml for dependency changes
❌ Making compilation fixes directly (add imports, fix syntax, etc.)
❌ Create implementation classes, interfaces, or modules directly
❌ "Implement then have agents review" pattern - THIS IS A VIOLATION
❌ "Quick implementation before delegating" - THIS IS A VIOLATION

## PROHIBITED ACTIONS During IMPLEMENTATION State {#prohibited-actions-during-implementation-state}

**CRITICAL VIOLATION PATTERNS** (detected by audit):

❌ **Using Edit/Write tools on ANY .java files** (source OR test)
- Violation: `Edit: FormatterAPI.java` to fix compilation error
- Correct: Delegate to quality with error description

❌ **Using Edit/Write tools on module-info.java**
- Violation: `Edit: module-info.java` to add `requires transitive`
- Correct: Delegate to architect with JPMS requirement

❌ **Using Edit/Write tools on pom.xml for dependency changes**
- Violation: `Edit: pom.xml` to add missing dependency
- Correct: Report to architect for dependency analysis

❌ **Making compilation fixes directly**
- Violation: Main agent adding `import java.util.List;` to fix compile error
- Correct: Report error back to implementing agent for fix

❌ **Creating ANY source code files directly**
- Violation: `Write: TransformationContext.java` to implement interface
- Correct: Task tool invocation to architect

**CORRECT DELEGATION PATTERN**:

✅ Delegate ALL code changes to agents in implementation mode
✅ Report compilation issues back to agents in implementation mode
✅ Monitor status.json for agent completion
✅ Verify build success after agent updates
✅ Use Task tool exclusively for implementation work

### ❌ INCORRECT: Main Agent Direct Implementation {#incorrect-main-agent-direct-implementation}

```bash
# Main agent during IMPLEMENTATION state
Edit: FormatterAPI.java           # VIOLATION!
Edit: module-info.java            # VIOLATION!
Edit: FormatterAPITest.java       # VIOLATION!
Edit: pom.xml                     # VIOLATION!
git commit -m "Fix compilation"   # Committing violations!
```

### ✅ CORRECT: Delegation Pattern {#correct-delegation-pattern}

```bash
# Main agent during IMPLEMENTATION state
Task: quality
  prompt: "Fix compilation error in FormatterAPI.java:

  Error: package io.github.cowwoc.styler.ast.core does not exist

  Requirements:
  - Add module-info.java with proper requires statement
  - Update pom.xml if dependencies missing
  - Verify build success with ./mvnw compile -pl formatter"

# Wait for agent completion
Read: /workspace/tasks/{task}/agents/quality/status.json

# Verify build after agent completes
Bash: ./mvnw clean verify -pl formatter
```

### Stakeholder Agent Role (IMPLEMENTATION ONLY) {#stakeholder-agent-role-implementation-only}

**REQUIRED Actions**:
✅ Implement domain-specific requirements in THEIR OWN worktrees
✅ Write all source code files (.java, .ts, .py, etc.)
✅ Run incremental validation (compile, test, checkstyle)
✅ Merge changes to task branch after validation
✅ Update status.json with completion state

**Working Directory**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`

### Fail-Fast Validation Pattern {#fail-fast-validation-pattern}

**RECOMMENDED**: Validate after each agent merge, not after all implementation complete.

**Why**: Validation with fresh context is 20% faster than late validation.

**Evidence**: Session 3fa4e964-9cf2-4116-99ea-7a347b498a41 (implement-formatter-api) found 22 Checkstyle violations late, requiring 11 fix iterations. Early validation would have caught issues while agent context was still fresh.

**Pattern**:

```bash
# After merging first agent (architect)
cd /workspace/tasks/{task}/code
git merge {task}-architect

# Validate immediately (fresh context)
./mvnw checkstyle:check -pl :{module-name}

# Fix any violations NOW (context still fresh)
# Fixes are 20% faster with agent work fresh in memory

# Proceed to next agent
git merge {task}-engineer

# Validate immediately again
./mvnw checkstyle:check -pl :{module-name}

# Continue pattern for all agents
```

**Benefits**:
- 20% faster fixes (fresh context)
- Earlier error detection prevents cascading issues
- Violations attributed to specific agent (easier to diagnose)
- Reduced cognitive load (fixing small batches vs large batch at end)

**Hook Support**: `task-complete.sh` hook provides automatic reminder when agent completes.

### Session Summary Documentation Requirements {#session-summary-documentation-requirements}

When presenting implementation work for protocol compliance review, session summaries MUST include:

1. **Agent Invocation Audit Trail**: List of all Task tool invocations with agent names
2. **Commit Attribution**: Commit SHAs for each agent's merge to task branch
3. **Parallel Launch Verification**: Explicit confirmation of parallel agent launch pattern used

**Compliant Session Summary**:
```markdown
## Implementation Phase Summary {#implementation-phase-summary}

### Agent Invocations (Parallel Launch): {#agent-invocations-parallel-launch}
- architect: Task tool invoked in Message 15
- quality: Task tool invoked in Message 15 (parallel)
- style: Task tool invoked in Message 15 (parallel)
- test: Task tool invoked in Message 15 (parallel)

### Agent Commits to Task Branch: {#agent-commits-to-task-branch}
- architect: abc123def (merged FormattingRule interfaces)
- quality: def456ghi (applied design patterns)
- style: ghi789jkl (code style compliance)
- test: jkl012mno (test suite implementation)

### Verification: {#verification}
✅ Parallel agent launch pattern confirmed (all agents invoked in single message)
✅ All agents merged to task branch independently
✅ Main agent performed NO source code implementation
```


### Role Verification Questions {#role-verification-questions}

**Before writing ANY .java/.ts/.py file during IMPLEMENTATION, ask yourself**:
- [ ] Am I the main coordination agent? → If YES, use Task tool to delegate
- [ ] Am I a stakeholder agent in my own worktree? → If YES, proceed with implementation
- [ ] Am I in task worktree trying to implement? → STOP - This is a VIOLATION

### Agent Role Assignment During SYNTHESIS {#agent-role-assignment-synthesis}

**⚠️ CRITICAL**: When creating implementation plans in SYNTHESIS state, you MUST assign work to the CORRECT stakeholder agents based on their defined responsibilities.

**Common Mistake**: Assigning test implementation to "engineer" agent instead of "tester" agent.

**Agent Responsibility Reference**:

| Agent | Primary Responsibility | Implementation Work |
|-------|----------------------|-------------------|
| **architect** | System architecture, interface design, integration patterns | Implements interfaces, core classes, architectural components |
| **engineer** | Code quality, refactoring, duplication removal, complexity reduction | Implements quality improvements and refactoring (NOT tests) |
| **tester** | Test coverage, test strategy, edge case identification | **Implements all test code** |
| **formatter** | Documentation, code style, JavaDoc, checkstyle/PMD compliance | Validates and fixes style violations |
| **builder** | Build systems, Maven/Gradle configuration, dependency management | Implements build configuration |
| **configurator** | Configuration systems, TOML/YAML parsing, validation | Implements configuration handling |

**MANDATORY Verification Before IMPLEMENTATION**:

When assigning implementation work in your plan:

- [ ] **Consult agent definitions**: Read `.claude/agents/{agent-name}.md` before assigning work
- [ ] **Verify PRIMARY DOMAIN**: Check agent's "PRIMARY DOMAIN" section matches work type
- [ ] **Common mistake check**:
  - ❌ WRONG: "Engineer agent implements tests"
  - ✅ CORRECT: "Tester agent implements tests"
  - ❌ WRONG: "Formatter agent implements interfaces"
  - ✅ CORRECT: "Architect agent implements interfaces"

**Agent Assignment Examples**:

✅ **CORRECT Assignments**:
```markdown
### Implementation Workflow

1. **Architect agent** (implements interfaces and core classes):
   - Create FormattingRule interface
   - Create TransformationContext interface
   - Implement SourcePosition record

2. **Tester agent** (implements all test code):
   - Create FormattingRuleTest
   - Create TransformationContextTest
   - Implement test module descriptor

3. **Formatter agent** (validates documentation and style):
   - Run checkstyle validation
   - Review JavaDoc quality
   - Report violations or approve
```

❌ **WRONG Assignments** (causes rework):
```markdown
### Implementation Workflow

1. **Architect agent** (implements interfaces):
   - Create FormattingRule interface

2. **Engineer agent** (implements tests):  ← WRONG! Engineer does refactoring, NOT tests
   - Create FormattingRuleTest
   - Create TransformationContextTest

3. **Formatter agent** (validates):
   - Run checkstyle
```

**If Unsure About Agent Assignment**:

1. **Read agent definition**: `Read: .claude/agents/{agent-name}.md`
2. **Check PRIMARY DOMAIN section**: Verify work matches agent's primary domain
3. **Look for similar tasks**: Search git history for similar work assignments
4. **When in doubt**: architect = design, tester = tests, engineer = refactoring

### Required Pattern After SYNTHESIS Approval {#required-pattern-after-synthesis-approval}

> **⚠️ MANDATORY USER APPROVAL CHECKPOINT**
>
> **BEFORE transitioning from SYNTHESIS to IMPLEMENTATION state**, you MUST:
> 1. Present the implementation plan to the user (via message output)
> 2. **Explicitly ask user for approval**: "May I proceed to IMPLEMENTATION?"
> 3. **WAIT for user response** (BLOCKED until approval received)
> 4. User must respond with explicit approval (e.g., "approved", "yes", "proceed")
> 5. Create flag file: `touch /workspace/tasks/{task-name}/user-approved-synthesis.flag`
> 6. Hook will BLOCK transition without this flag file
>
> **Required Message Pattern**:
> ```
> I've completed SYNTHESIS and created an implementation plan in task.md.
>
> **Implementation Summary**:
> - [Brief bullet points of what will be implemented]
>
> **Stakeholder Agents**:
> - [List of agents that will be invoked]
>
> May I proceed to IMPLEMENTATION state?
> ```
>
> **Enforcement**: State transition from SYNTHESIS → IMPLEMENTATION is BLOCKED unless
> `/workspace/tasks/{task-name}/user-approved-synthesis.flag` exists (checked by
> pre-state-transition.sh hook). Flag is only created AFTER receiving explicit user approval.
>
> **Note**: Do NOT use AskUserQuestion tool for approval gates - it may be skipped in
> bypass permissions mode. Use explicit user message approval + flag file instead.
>
> See [User Approval Checkpoint Enforcement](#user-approval-checkpoint-enforcement) for complete requirements.

**🚨 MANDATORY REQUIREMENT**: Agent launches MUST use parallel Task tool calls in a SINGLE message.

**VIOLATION**: Launching agents sequentially across multiple messages is a CRITICAL protocol violation that wastes 30,000-40,000 tokens per task.

**MANDATORY SESSION DOCUMENTATION**: Agent invocation record MUST be documented in task.md under "Implementation Status" section:

```markdown
## Implementation Status {#implementation-status}

### Agent Invocations {#agent-invocations}
**Round 1 - Initial Implementation** (Message 15, 2025-10-19T14:32:00Z):
- architect: Launched (parallel)
- quality: Launched (parallel)
- style: Launched (parallel)
- test: Launched (parallel)

**Round 1 - Review** (Message 22, 2025-10-19T14:45:00Z):
- architect: Launched (parallel)
- quality: Launched (parallel)
- style: Launched (parallel)
- test: Launched (parallel)

**Round 2 - Fixes** (Message 28, 2025-10-19T15:02:00Z):
- style: Re-launched for violation fixes
- architect: Re-launched for interface clarifications
```


```markdown
CORRECT SEQUENCE:
1. User approves implementation plan
2. Main agent: "Launching stakeholder agents in implementation mode for parallel implementation..."
3. Main agent launches stakeholder agents in IMPLEMENTATION mode in SINGLE MESSAGE (parallel):
   - Task tool (architect): "Implement FormattingRule interfaces per requirements..."
   - Task tool (tester): "Implement test suite per test strategy..."
   - Task tool (formatter): "Validate code style per requirements..."

   NOTE: Standard agents are architect (design), tester (tests), formatter (style).
   Add engineer only for refactoring tasks with existing code.

4. Agents in implementation mode implement in THEIR worktrees, validate locally, then merge to task branch
5. Main agent: "Implementation agents have merged. Launching stakeholder agents in validation mode for parallel review..."
6. Main agent launches stakeholder agents in VALIDATION mode in SINGLE MESSAGE (parallel):
   - Task tool (architect): "Review merged architecture on task branch..."
   - Task tool (tester): "Review merged test coverage on task branch..."
   - Task tool (formatter): "Review merged style compliance on task branch..."

   NOTE: Add engineer for code quality review if refactoring was performed.

7. Agents in validation mode analyze and report APPROVED or REJECTED with feedback
8. If any REJECTED → launch agents in implementation mode with feedback → re-review (repeat 5-7 until all APPROVED)
9. When ALL validation agents report APPROVED → main agent updates lock file: state = "VALIDATION"
10. Main agent NOW PERMITTED to fix minor issues (style violations, imports, etc.)
11. Main agent runs final build verification: ./mvnw verify
12. If build passes → proceed to REVIEW state
13. If build fails → main agent MAY fix OR re-delegate to agents
14. After REVIEW state with unanimous approval → transition to AWAITING_USER_APPROVAL state
15. **Present implementation summary and request explicit user approval** (via message output):
    ```
    Implementation complete and validated.

    **Summary**:
    - [Key deliverables]
    - All agents in review mode approved
    - Build passing

    May I proceed to COMPLETE and merge to main?
    ```
16. WAIT for user approval response (BLOCKED until received)
17. User must respond with explicit approval (e.g., "approved", "yes", "proceed")
18. After user approves, create flag: `touch /workspace/tasks/{task-name}/user-approval-obtained.flag`
19. Transition to COMPLETE state (hook will verify flag exists)
20. **Delete agent branches** (before squash to keep history clean):
    ```bash
    git branch -D {task-name}-architect {task-name}-tester {task-name}-formatter
    ```
21. **Pre-Merge Validation** (MANDATORY):

    > 🚨 **CRITICAL GIT WORKFLOW VIOLATION PATTERN**
    >
    > **NEVER merge task branch without verifying exactly 1 commit**
    >
    > **Common Mistake**: Skipping validation → merging with 12+ commits → polluted main history
    >
    > **Requirement**: Task branch MUST have EXACTLY 1 commit before merge
    >
    > **Audit Finding**: Merging without squashing is a CRITICAL protocol violation

    ```bash
    # Step 19a: ALWAYS verify task branch has exactly 1 commit
    COMMIT_COUNT=$(git rev-list --count main..<task-branch>)
    echo "Task branch has $COMMIT_COUNT commit(s)"
    # Hook .claude/hooks/enforce-commit-squashing.sh auto-validates on merge

    # Step 19b: If validation fails (>1 commit), squash ALL commits before merge:
    cd /workspace/main
    git checkout <task-branch>
    git reset --soft main
    git commit -m "Squashed task implementation"

    # Step 19c: MANDATORY re-validation to confirm squash succeeded
    COMMIT_COUNT=$(git rev-list --count main..<task-branch>)
    [ "$COMMIT_COUNT" -eq 1 ] && echo "✅ Task branch ready for merge: 1 commit"
    ```

    **VERIFICATION CHECKPOINT**:
    ```bash
    # Verify commit count BEFORE proceeding to step 20
    COMMIT_COUNT=$(git rev-list --count main..<task-branch>)
    if [ "$COMMIT_COUNT" -ne 1 ]; then
      echo "❌ VIOLATION: Cannot proceed - task branch has $COMMIT_COUNT commits (expected 1)"
      echo "📖 See git-workflow.md § Task Branch Squashing for squash procedure"
      exit 1
    fi
    echo "✅ Pre-merge validation PASSED: Exactly 1 commit on task branch"
    ```

22. **Merge to main with atomic documentation update** (MANDATORY - all changes in single commit):
    ```bash
    # Step 20a: Merge task branch to main
    cd /workspace/main
    git checkout main
    git merge <task-branch> --ff-only

    # Step 20b: Update project documentation (MANDATORY)
    # CRITICAL: These updates MUST be part of the same commit as the task
    # Update todo.md: Mark task complete, update dependencies, update current status
    # Update changelog.md: Add task completion entry with date, commit SHA, summary

    # Step 20c: Amend merge commit to include documentation updates
    git add todo.md changelog.md
    git commit --amend --no-edit

    # VERIFICATION: Single commit includes task implementation AND documentation updates
    git show HEAD --stat  # Should show task files + todo.md + changelog.md
    ```

    **CRITICAL REQUIREMENT**: The merge commit MUST include both:
    - Task implementation (from task branch)
    - Documentation updates (todo.md, changelog.md)

    **VIOLATION PATTERN** (separate commits):
    ```bash
    ❌ WRONG - Task and documentation in separate commits:
    git merge <task-branch> --ff-only          # Commit 1: e70587a (task only)
    # Update todo.md and changelog.md
    git add todo.md changelog.md
    git commit -m "Update documentation"       # Commit 2: 755532c (docs only)
    # RESULT: Two commits instead of one atomic unit
    ```

23. **Transition to CLEANUP state** (AUTOMATIC - Execute Immediately):

    > 🚨 **CRITICAL CLEANUP VIOLATION PATTERN**
    >
    > **NEVER wait or prompt user after COMPLETE - cleanup is AUTOMATIC**
    >
    > **Common Mistake**: Transitioning to COMPLETE → waiting for user prompt → user reminds about cleanup
    >
    > **Requirement**: CLEANUP MUST happen IMMEDIATELY after step 20 (merge to main)
    >
    > **Audit Finding**: Missing automatic cleanup is a CRITICAL protocol violation

    **MANDATORY CLEANUP CHECKLIST** (execute ALL steps without pausing):

    ```bash
    # Step 21a: Transition to CLEANUP state
    cd /workspace/main
    jq '.state = "CLEANUP"' /workspace/tasks/{task-name}/task.json > /tmp/task.json.tmp
    mv /tmp/task.json.tmp /workspace/tasks/{task-name}/task.json

    # Step 21b: MANDATORY - Exit to main worktree BEFORE removing task worktree
    # (Git cannot remove worktree while you are inside it)
    cd /workspace/main
    pwd | grep -q '/workspace/main$' || { echo "❌ Not in main worktree"; exit 1; }

    # Step 21c: Remove task worktree
    git worktree remove /workspace/tasks/{task-name}/code

    # Step 21d: Remove ALL agent worktrees (if they exist)
    for agent_dir in /workspace/tasks/{task-name}/agents/*/code; do
      if [ -d "$agent_dir" ]; then
        git worktree remove "$agent_dir" 2>/dev/null || true
      fi
    done

    # Step 21e: Delete ALL task-related branches
    # CRITICAL: Use -D (force delete) because squash merge makes branches unreachable
    git branch -D {task-name} 2>/dev/null || true
    git branch | grep "^  {task-name}-" | xargs -r git branch -D

    # Step 21f: Verify complete branch cleanup
    if git branch | grep -q "{task-name}"; then
      echo "❌ VIOLATION: Task branches still exist after CLEANUP"
      git branch | grep "{task-name}"
      exit 1
    fi

    # Step 21g: Garbage collect unreferenced commits from squash merge
    echo "Running garbage collection to remove orphaned commits from squash merge..."
    git gc --prune=now
    echo "✅ Orphaned commits from squash merge removed"

    # Step 21h: Remove task directory (preserves audit trail if enabled)
    rm -rf /workspace/tasks/{task-name}

    # Step 21i: VERIFICATION - Confirm complete cleanup
    echo "✅ CLEANUP complete:"
    echo "   - Task worktree removed"
    echo "   - Agent worktrees removed"
    echo "   - All task branches deleted"
    echo "   - Orphaned commits garbage collected"
    echo "   - Task directory removed"
    ```

    **CRITICAL REQUIREMENTS**:
    - ✅ Execute IMMEDIATELY after step 20 (no user prompt, no delay)
    - ✅ Execute ALL cleanup steps in sequence
    - ✅ Run garbage collection after deleting branches to remove orphaned commits
    - ✅ Verify cleanup succeeded before finishing
    - ❌ NEVER skip cleanup steps
    - ❌ NEVER wait for user confirmation to cleanup

```

**CRITICAL**: Steps 14-18 are MANDATORY and CANNOT be skipped. The sequence REVIEW → AWAITING_USER_APPROVAL → COMPLETE → CLEANUP is REQUIRED.

**Key Transition Point**: Step 9 (VALIDATION state) is when main agent permissions change from PROHIBITED to
PERMITTED for minor fixes. Steps 5-8 are iterative rounds within IMPLEMENTATION state using both reviewer and
agents in implementation mode until all reviewers approve.

**VIOLATION PATTERN** (NEVER DO THIS):
```markdown
❌ WRONG SEQUENCE:
1. User approves implementation plan
2. Main agent: "I will now implement the feature..."
3. Main agent: **Write** src/main/java/FormatterApi.java (VIOLATION!)
4. Main agent: **Edit** src/main/java/Feature.java (VIOLATION!)
5. Main agent creates files directly in task worktree (VIOLATION!)
```

### Enforcement {#enforcement}

**Hook Detection**: `.claude/hooks/detect-main-agent-implementation.sh` monitors Write/Edit tool calls during IMPLEMENTATION state and BLOCKS attempts by main agent to create source files in task worktree.

**Recovery**: If violation detected, return to SYNTHESIS state and re-launch stakeholder agents properly.

### Agent Tool Limitation Recovery Pattern {#agent-tool-limitation-recovery-pattern}

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

### Edit Tool Whitespace Mismatch Recovery {#edit-tool-whitespace-mismatch-recovery}

See CLAUDE.md § Tool Usage Best Practices for complete Edit tool whitespace handling guidance.

### State-Based Edit/Write Tool Permissions {#state-based-editwrite-tool-permissions}

Main agent Edit/Write tool permissions vary by state.

| State | Main Agent Permission | Scope |
|-------|----------------------|-------|
| **INIT** | ✅ PERMITTED | Infrastructure setup only |
| **CLASSIFIED** | ✅ PERMITTED | Documentation only |
| **REQUIREMENTS** | ✅ PERMITTED | Documentation only |
| **SYNTHESIS** | ✅ PERMITTED | Documentation only |
| **IMPLEMENTATION** | ❌ PROHIBITED | Source code files - agents implement |
| **VALIDATION** | ✅ PERMITTED | Minor fixes after agent completion |
| **REVIEW** | ✅ PERMITTED | Fix agent feedback |
| **COMPLETE** | ✅ PERMITTED | Final touches |
| **CLEANUP** | ✅ PERMITTED | Infrastructure only |

**Detailed State Permissions**:

#### INIT State {#init-state-permissions}

✅ **PERMITTED**:
- Infrastructure files: `module-info.java`, `package-info.java`
- Build configuration: `pom.xml`, `build.gradle`, `.mvn/` config
- Task coordination: `task.json`, `task.md` creation
- Git worktree setup: Task and agent worktrees
- Hook configuration updates

❌ **PROHIBITED**:
- Business logic source files (e.g., `FormattingRule.java`, `Parser.java`)
- Test implementation files (e.g., `*Test.java`, `*Tests.java`)
- Feature implementation or functionality

#### IMPLEMENTATION State {#implementation-state-permissions}

❌ **PROHIBITED** (Main Agent):
- ANY creation or editing of `.java`, `.ts`, `.py` source files
- Adding feature code to existing files
- Implementing business logic

✅ **PERMITTED** (Stakeholder Agents Only):
- Full source file creation in agent worktrees
- Test implementation
- Feature development

#### VALIDATION State {#validation-state-permissions}

✅ **PERMITTED** (Main Agent):
- Infrastructure fixes: `module-info.java`, `package-info.java`
- Minor style violations (< 5 fixes)
- Build configuration updates
- Fixing JPMS exports/requires

❌ **PROHIBITED** (Main Agent):
- Feature implementation
- Architectural changes
- High-volume style fixes (> 5 violations)
- Test logic modifications

**VALIDATION State Exit Requirements**:
1. ✅ All quality gates pass (checkstyle, PMD, build)
2. ✅ All validation fixes COMMITTED to task branch
3. ✅ Run final build verification on committed code
4. ✅ Verify `git status` shows clean working directory before REVIEW transition

**Pre-Transition Verification**:
```bash
git status  # Must show "nothing to commit, working tree clean"
```

Lock file state MUST NOT be updated to REVIEW if `git status` shows uncommitted changes.

**Quality Gate Cache Verification**:

Before transitioning VALIDATION → REVIEW, verify quality gates executed (not cached):

```bash
./mvnw verify -Dmaven.build.cache.enabled=false
```

**See Also**: task-protocol-core.md § VALIDATION → REVIEW for detailed cache detection patterns

### Post-Implementation Issue Handling Decision Tree {#post-implementation-issue-handling-decision-tree}

This decision tree applies ONLY after ALL three conditions are met:
1. ✅ ALL stakeholder agents have status.json with `{"status": "COMPLETE"}`
2. ✅ ALL agents have merged their changes to task branch
3. ✅ Lock file state updated to `"VALIDATION"`

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

**Real-World Example: Missing Module Dependency (implement-formatter-api)** {#real-world-example-missing-module-dependency}

**Situation**: Agent (architect) referenced AST types (NodeIndex, NodeArena) from ast-core module, but that module doesn't exist yet in the codebase (planned for future phase).

**Build Error**:
```
[ERROR] cannot find symbol: class NodeIndex
[ERROR] package io.github.cowwoc.styler.ast.core does not exist
```

**Main Agent Decision** (during VALIDATION state):
1. **Infrastructure Fix**: Removed `requires io.github.cowwoc.styler.ast.core;` from module-info.java ✅ (always allowed)
2. **Source File Fix**: Changed `NodeIndex` type references to `Object` as temporary placeholders ✅ (mechanical - no logic change)
3. **Rationale**: This is a build infrastructure issue caused by missing dependency, not a logic error in agent's implementation

**Why This Was Correct**:
- Fix is mechanical: Simple type substitution (NodeIndex → Object)
- No domain expertise required: Obvious workaround for missing module
- Agent's logic was sound: They correctly identified what types were needed
- Alternative (re-invoke architect): Would cause excessive rework for trivial type placeholders

**Real-World Example: Incorrect Import Path (implement-formatter-api)** {#real-world-example-incorrect-import-path}

**Situation**: Agent used incorrect package path for SecurityConfig import.

**Build Error**:
```
[ERROR] package io.github.cowwoc.styler.security.api does not exist
```

**Correct Import Path**: `io.github.cowwoc.styler.security.SecurityConfig` (no `.api` subpackage)

**Main Agent Decision** (during VALIDATION state):
- **Source File Fix**: Changed import from `io.github.cowwoc.styler.security.api.SecurityConfig` to `io.github.cowwoc.styler.security.SecurityConfig` ✅

**Why This Was Correct**:
- Fix is mechanical: Simple path correction (remove `.api`)
- No logic change: Same class, correct package path
- Error message clearly identified the problem
- Alternative (re-invoke architect): Wasteful for one-line import correction

**After ALL agents report COMPLETE status, main agent discovers issues during VALIDATION:**

```
IF (issue_type == "compilation_error"):
    IF (simple_fix like missing_import OR unused_variable):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to architect agent

ELSE IF (issue_type == "style_violation"):
    IF (count <= 5 violations total across all files AND fixes are mechanical like whitespace/imports):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to style agent

ELSE IF (issue_type == "test_failure"):
    IF (simple_fix like assertion_update):
        ✅ Main agent fixes directly
    ELSE:
        ❌ Re-delegate to test agent

ELSE IF (issue_type == "architecture_issue" OR "security_issue"):
    ❌ ALWAYS re-delegate to appropriate domain expert
```

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

### Re-delegation Workflow (When Violations Exceed Threshold) {#re-delegation-workflow-when-violations-exceed-threshold}

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
# Decision: Exceeds threshold (5), delegate to style

Task tool (style), model: haiku, prompt: "Fix 12 style violations in FormattingRule.java:
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
Task tool (architect), model: haiku, prompt: "Address architectural issues found during validation..."
```

## 🚨 TASK PROTOCOL SUMMARY {#task-protocol}

**Full Protocol**: See [task-protocol-core.md](docs/project/task-protocol-core.md) and [task-protocol-operations.md](docs/project/task-protocol-operations.md) for complete details.

**State Machine**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → AWAITING_USER_APPROVAL → COMPLETE → CLEANUP

**Critical Requirements**:
- Lock acquisition (§ Lock Ownership)
- Worktree isolation (§ Worktree Isolation)
- Implementation delegation: Main agent coordinates, stakeholder agents implement (§ Implementation Role Boundaries)
- Build verification before merge
- Unanimous stakeholder approval (REVIEW state)
- Complete all states before selecting new task

**Risk-Based Variants**:
- **HIGH-RISK** (src/\*\*, pom.xml, security/\*\*): Full protocol with all agents
- **MEDIUM-RISK** (tests, docs): Abbreviated protocol, domain-specific agents
- **LOW-RISK** (general docs): Streamlined protocol, minimal validation

## 🚨 RISK-BASED PROTOCOL SELECTION {#risk-based-protocol-selection}

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

## 🚨 STAKEHOLDER CONSENSUS ENFORCEMENT {#stakeholder-consensus-enforcement}

Phase 6 requires UNANIMOUS stakeholder approval.

**Decision Logic**:
- ALL agents must respond with "FINAL DECISION: ✅ APPROVED"
- ANY agent with "❌ REJECTED" → MANDATORY Phase 5 execution + Phase 6 re-run
- NO human override - agent decisions are ATOMIC and BINDING

## 🚨 AUTONOMOUS TASK COMPLETION REQUIREMENT {#autonomous-task-completion-requirement}

Once you begin a task (execute INIT state), you MUST complete ALL protocol states autonomously, WITH MANDATORY USER APPROVAL CHECKPOINTS.

**"Autonomous Completion" Definition**:
- Complete work WITHOUT asking for help, guidance, or direction between checkpoints
- Checkpoints are planned pauses built into protocol for user oversight
- Does NOT mean no user interaction - means no mid-protocol abandonment or "what should I do?" questions

**Expected Behavior**:
✅ Complete REQUIREMENTS, SYNTHESIS, IMPLEMENTATION, VALIDATION without asking "should I continue?"
✅ PAUSE at SYNTHESIS for PLAN APPROVAL checkpoint
✅ PAUSE at AWAITING_USER_APPROVAL for CHANGE REVIEW checkpoint

**Violations**:
❌ Stopping mid-state to ask "what should I do next?"
❌ Abandoning task due to complexity without exhausting recovery options
❌ Requesting user intervention for issues that protocol defines recovery procedures for

**Single-Session Completion**:
- Task execution occurs in ONE uninterrupted session
- TWO approval checkpoints: PLAN APPROVAL (after SYNTHESIS), CHANGE REVIEW (after REVIEW)
- NO OTHER user handoffs mid-protocol

**Interruption Type Classification**:

**EXPECTED Pauses** (built into protocol):
1. **PLAN APPROVAL**: After SYNTHESIS, before IMPLEMENTATION - wait for user approval
2. **CHANGE REVIEW**: After REVIEW, before COMPLETE - wait for user approval (state: AWAITING_USER_APPROVAL)

**PERMITTED Interruptions** (not violations):
1. **User Questions**: Answer, then resume work from same state
2. **User Commands**: Execute (/audit-session, etc.), then resume

**ACCEPTABLE Stops** (genuine blockers):
1. **External Blocker**: API unavailable, missing credentials, network failure
2. **Conflicting Requirements**: Unresolvable contradictions after exhausting resolution attempts
3. **User Stop Command**: User explicitly says "stop"

**VIOLATION Patterns**:
❌ Agent giving up: "This is too hard, let me try something simpler"
❌ Premature help-seeking: "I don't know what to do next"
❌ Mid-state handoffs: "Should I continue with IMPLEMENTATION?"

**Decision Tree: Should I Stop?**
- At approval checkpoint? → STOP ✅
- User question/command? → Handle, then CONTINUE ✅
- External blocker/unresolvable conflict/user stop? → STOP ✅
- Complexity/uncertainty? → CONTINUE, apply recovery ❌ DO NOT STOP
- Otherwise → CONTINUE ✅

## 🚨 USER APPROVAL CHECKPOINT ENFORCEMENT {#user-approval-checkpoint-enforcement}

The `enforce-user-approval.sh` hook BLOCKS transitions to COMPLETE state if user approval checkpoint not satisfied.

**Approval Marker System**:
- **File**: `/workspace/tasks/{task-name}/user-approval-obtained.flag`
- **Created**: When user provides explicit approval after REVIEW state
- **Required**: Before COMPLETE state transition
- **Removed**: During CLEANUP state

**Approval Flag Persistence**:

Approval flags are PERSISTENT across context compaction and session resumption via file-based storage.

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
## PLAN APPROVAL Checkpoint {#plan-approval-checkpoint}

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

**Context Compaction Scenarios**:
- Approval before compaction → Flag persists, proceed
- Compaction during checkpoint wait → Re-present, wait for approval
- Multiple compactions between checkpoints → Old flags ignored, wait for current checkpoint approval

**Session Resumption Behavior**:

When resuming after context compaction or crash:
1. Check current state from lock file
2. Check approval flag existence
3. If flag exists → proceed without re-requesting approval
4. If flag missing → re-present checkpoint for approval

**Flag Creation Timing**:

## Approval Persistence Pattern {#approval-persistence-pattern}

Context compaction can lose approval state. Solution: Create persistent approval flag files.

**After obtaining approval**:
```bash
touch /workspace/tasks/{task-name}/user-plan-approval-obtained.flag  # or user-approval-obtained.flag
```

**On session resumption**:
```bash
[ -f /workspace/tasks/{task-name}/user-plan-approval-obtained.flag ] || present_plan_for_approval
```

**Cleanup**: Remove flags during CLEANUP state only

**Edge Cases**:
- Flag exists but state advanced → Continue (approval already obtained)
- Flag missing but state advanced → Assume approval obtained, continue, log potential violation
- Both flags exist → Both checkpoints passed, proceed to CLEANUP
- Session crash between approval and flag creation → Re-present for approval (safer)

**Flag Cleanup**:

Flags removed ONLY during CLEANUP state:
```bash
rm -f "/workspace/tasks/${TASK_NAME}/user-plan-approval-obtained.flag"
rm -f "/workspace/tasks/${TASK_NAME}/user-approval-obtained.flag"
```

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

## 🚨 TASK UNAVAILABILITY HANDLING {#task-unavailability-handling}

**CRITICAL**: When user requests "work on the next task" or similar, verify task availability before starting
work.

### Mandatory Availability Check {#mandatory-availability-check}

**BEFORE attempting to select or start ANY task:**
1. Check todo.md for available tasks with `READY` status
2. Verify task dependencies are met (check for `BLOCKED` status)
3. Check `/workspace/tasks/{task-name}/task.json` for existing locks on available tasks
4. Confirm at least ONE task is available AND accessible

### Required Response When No Tasks Available {#required-response-when-no-tasks-available}

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

### Prohibited Patterns {#prohibited-patterns}

**NEVER do any of the following when all tasks are unavailable:**
❌ Select a blocked task and attempt to start work
❌ Try to work around missing dependencies
❌ Attempt to create new tasks not in todo.md
❌ Ask user vague questions like "What should I work on?"
❌ Proceed with any work when clear blockers exist
❌ Suggest working on tasks that violate dependency requirements

### Required Patterns {#required-patterns}

**ALWAYS do the following when all tasks are unavailable:**
✅ Stop immediately after determining no tasks available
✅ Provide detailed analysis of why each task is unavailable
✅ Explain specific conditions needed for tasks to become available
✅ Wait for user to resolve blockers or provide new instructions
✅ Check both BLOCKED status AND lock files AND dependencies

## 🎯 LONG-TERM SOLUTION PERSISTENCE {#long-term-solution-persistence}

**MANDATORY PRINCIPLE**: Prioritize optimal long-term solutions over expedient alternatives. Persistence and
thorough problem-solving are REQUIRED.

### 🚨 CRITICAL PERSISTENCE REQUIREMENTS {#critical-persistence-requirements}

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

### 🚨 PROHIBITED DOWNGRADE PATTERNS {#prohibited-downgrade-patterns}

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

### 🚨 GIVING UP DETECTION PATTERNS {#giving-up-detection-patterns}

**Hook**: `detect-giving-up.sh` detects abandonment patterns

**Response**: Return to original problem, apply systematic debugging, exhaust approaches before scope
modification

### 🧪 UNIT TEST DRIVEN BUG FIXING {#unit-tdd-implementationing}

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

### 🛡️ STAKEHOLDER AGENT PERSISTENCE ENFORCEMENT {#stakeholder-agent-persistence-enforcement}

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

### 🔧 IMPLEMENTATION PERSISTENCE PATTERNS {#implementation-persistence-patterns}

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

### 🚨 SCOPE NEGOTIATION PERSISTENCE INTEGRATION {#scope-negotiation-persistence-integration}

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

### 🎯 SUCCESS METRICS AND VALIDATION {#success-metrics-and-validation}

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

## Repository Structure {#repository-structure}

**⚠️ NEVER** initialize new repositories
**Main Repository**: `/workspace/main/` (git repository and main development branch)
**Task Worktrees**: `/workspace/tasks/{task-name}/code/` (isolated per task protocol, common merge target)
**Agent Worktrees**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/` (per-agent isolation)
**Locks**: Multi-instance coordination via lock files at `/workspace/tasks/{task-name}/task.json`

**Multi-Agent Architecture**: See § Implementation Role Boundaries for complete role definitions and workflow patterns.

## 🔧 CONTINUOUS WORKFLOW MODE {#continuous-workflow-mode}

Override system brevity for comprehensive multi-task automation via Task Protocol.

**Trigger**: `"Work on the todo list in continuous mode."`
**Auto-Detection**: "todo list", "all tasks", "continuously", "CONTINUOUS WORKFLOW MODE"
**Effects**: Detailed output, automatic task progression, full stakeholder analysis, comprehensive TodoWrite
tracking

## ⚡ PERFORMANCE OPTIMIZATION REQUIREMENTS {#performance-optimization-requirements}

**CRITICAL**: Session performance optimization through parallel execution and efficiency patterns.

### Performance Optimization Patterns (MANDATORY) {#performance-optimization-patterns-mandatory}

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

### Parallel Agent Invocation (MANDATORY) {#parallel-agent-invocation-mandatory}

**CRITICAL**: When launching multiple independent agents (reviewers or updaters), invoke ALL agents in a SINGLE message using parallel Task calls.

❌ **Anti-Pattern (Sequential Launches - wastes ~20,000 tokens)**:
```markdown
Message 1: Task(architect)
[wait for completion]
Message 2: Task(quality)
[wait for completion]
Message 3: Task(style)
[wait for completion]
```

✅ **Required Pattern (Parallel Launch in Single Message)**:
```markdown
Single Message:
  Task(architect): "Analyze architecture requirements..."
  Task(quality): "Review quality standards..."
  Task(style): "Validate style requirements..."
[all agents run concurrently, wait for all completions]
```

**Impact**: Reduces message overhead from N × 4000 tokens to 1 × 4000 tokens
- 3 agents sequential: ~12,000 tokens overhead
- 3 agents parallel: ~4,000 tokens overhead
- **Savings: ~8,000 tokens (67% reduction) per agent invocation round**

**When to Use Parallel Invocation**:
- ✅ REQUIREMENTS phase: Launch all agents in review mode simultaneously (architect, tester, formatter)
- ✅ IMPLEMENTATION phase: Launch all agents in implementation mode simultaneously when implementing parallel components
- ✅ REVIEW phase: Launch all agents in review mode simultaneously for final validation
- ❌ Do NOT parallelize agents with dependencies (e.g., architecture must complete before implementation)

**Note**: Engineer agent may be added for refactoring tasks, but standard trio is architect (design), tester (tests), formatter (style)

**Tool Call Syntax**:
All Task calls must appear in the same `<function_calls>` block to execute in parallel.

**Example - REQUIREMENTS Phase Parallelization**:

✅ **CORRECT** (Single message with 4 parallel Task calls - saves ~24,000 tokens):
```
Main agent launches all agents in review mode in ONE message:
- Task(architect): Gather architecture requirements
- Task(style): Gather style requirements
- Task(quality): Gather quality requirements
- Task(test): Gather test requirements

Result: All 4 agents execute concurrently, single round-trip
```

❌ **VIOLATION** (Sequential launches - wastes ~24,000 tokens):
```
Message 1: Task(architect)
[Wait for response...]
Message 2: Task(style)
[Wait for response...]
Message 3: Task(quality)
[Wait for response...]
Message 4: Task(test)

Result: 4 sequential round-trips, 3x more latency
```

### Fail-Fast vs Batch Collection Decision Criteria {#fail-fast-vs-batch-collection-decision-criteria}

**Use FAIL-FAST (stop on first error)**:
- Compilation errors (blocking all other checks)
- JPMS module resolution failures
- Missing dependencies (cannot proceed without)
- Architecture violations detected by build

**Use BATCH COLLECTION (collect all, fix together)**:
- Style violations (checkstyle, PMD)
- Test failures (non-blocking)
- Documentation issues
- Any violation count >5 of same type

### Protocol File Prefetching Pattern {#protocol-file-prefetching-pattern}

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
  Read /workspace/main/docs/project/task-protocol-agents.md
  Read /workspace/main/CLAUDE.md --offset=140 --limit=300  # Implementation boundaries section
Message 2: Now have all protocol context, proceed with REQUIREMENTS
```

**Files to Prefetch During INIT**:
- `task-protocol-core.md` - State definitions, transitions, requirements
- `task-protocol-operations.md` - Operational patterns, examples
- `task-protocol-agents.md` - Sub-agent coordination protocol (all tasks invoke sub-agents)
- `CLAUDE.md` sections relevant to task type (Implementation boundaries, Performance patterns)
- `code-style-human.md` - If style work expected (formatting rules, new classes)
- Task-specific domain docs if known upfront

**Impact**: Saves 5-10 round-trips × 2000 tokens each = **10,000-20,000 token savings per task**

### Incremental Validation Frequency {#incremental-validation-frequency}

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

### Parallel Execution Enforcement {#parallel-execution-enforcement}

**CRITICAL REQUIREMENT**: Parallel execution is MANDATORY, not optional. Sequential patterns waste significant tokens and violate performance requirements.

**ENFORCEMENT**:
- ❌ **VIOLATION**: Launching agents sequentially (separate Task tool calls across multiple messages)
- ✅ **REQUIRED**: Launch all independent agents in SINGLE message using parallel Task tool invocations
- **Hook**: `detect-sequential-tools.sh` warns on sequential patterns
- **Audit Check**: Protocol auditor verifies parallel launch pattern during IMPLEMENTATION → VALIDATION transition

**Examples**:

❌ **PROHIBITED (Sequential - wastes 8000+ tokens)**:
```markdown
Message 1: Task tool (architect) with implementation instructions
[Wait for response]
Message 2: Task tool (style) with review instructions
[Wait for response]
Message 3: Task tool (quality) with quality check
```

✅ **REQUIRED (Parallel - efficient)**:
```markdown
Single Message - Implementation Mode:
Task tool (architect), model: haiku, prompt: "Implement FormattingRule interfaces..."
Task tool (style), model: haiku, prompt: "Apply style guidelines to implementation..."
Task tool (quality), model: haiku, prompt: "Apply design patterns per requirements..."

Single Message - Review Mode:
Task tool (architect), model: sonnet, prompt: "Review implementation for completeness..."
Task tool (style), model: sonnet, prompt: "Review implementation for style compliance..."
Task tool (quality), model: sonnet, prompt: "Audit code quality and design patterns..."
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

### Combining Parallel and Sequential Operations {#combining-parallel-and-sequential-operations}

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
  Task (architect): "Implement FormattingRule..."
  Task (style): "Review for style compliance..."
  Task (quality): "Audit design patterns..."

Message 3 (Verification - sequential after Message 2):
  Bash: ./mvnw verify
```

**Key Principle**: Within each message, maximize parallelism. Between messages, maintain necessary sequencing.

