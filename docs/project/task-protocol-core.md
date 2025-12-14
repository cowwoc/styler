# Task State Machine Protocol

> **Version:** 2.2 | **Last Updated:** 2025-12-10
> **Related Documents:** [CLAUDE.md](../../CLAUDE.md) •
[task-protocol-transitions.md](task-protocol-transitions.md) •
[task-protocol-multi-agent.md](task-protocol-multi-agent.md) •
[task-protocol-operations.md](task-protocol-operations.md) •
[task-protocol-recovery.md](task-protocol-recovery.md) •
[task-protocol-risk-agents.md](task-protocol-risk-agents.md)

**CRITICAL**: This protocol applies to ALL tasks that create, modify, or delete files, using MANDATORY STATE
TRANSITIONS with zero-tolerance enforcement

**TARGET AUDIENCE**: Claude AI instances executing tasks
**ARCHITECTURE**: State machine with atomic transitions and verifiable conditions
**ENFORCEMENT**: No manual overrides - all transitions require documented evidence

---

## 🗺️ QUICK NAVIGATION INDEX {#quick-navigation-index}

**Use skills for common operations** (invoke via Skill tool):
- `select-agents` - Agent selection during CLASSIFIED state
- `recover-from-error` - Error diagnosis and recovery procedures
- `state-transition` - Safe state transitions with validation

**Jump to section by current state:**

| Current State | Read File/Section |
|---------------|-------------------|
| **Starting task** | [State Machine Architecture](#state-machine-architecture) (this file) |
| **CLASSIFIED** | [task-protocol-risk-agents.md](task-protocol-risk-agents.md) (agent selection) |
| **REQUIREMENTS** | [task-protocol-transitions.md § CLASSIFIED → REQUIREMENTS](task-protocol-transitions.md#classified-requirements) |
| **SYNTHESIS** | [task-protocol-transitions.md § REQUIREMENTS → SYNTHESIS](task-protocol-transitions.md#requirements-synthesis) |
| **IMPLEMENTATION** | [task-protocol-multi-agent.md](task-protocol-multi-agent.md) (multi-agent workflow) |
| **VALIDATION** | [task-protocol-transitions.md § IMPLEMENTATION → VALIDATION](task-protocol-transitions.md#implementation-validation) |
| **Error/Recovery** | [task-protocol-recovery.md](task-protocol-recovery.md) or use `recover-from-error` skill |

**Jump to section by need:**

| Need | File/Section |
|------|--------------|
| State definitions | [State Definitions](#state-definitions) (this file) |
| User approval checkpoints | [User Approval Checkpoints](#user-approval-checkpoints---mandatory-regardless-of-bypass-mode) (this file) |
| Lock file management | [task-protocol-transitions.md § Main Worktree Lock](task-protocol-transitions.md#main-worktree-operations-lock-requirement) |
| State transition details | [task-protocol-transitions.md](task-protocol-transitions.md) |
| Compliance audit | [Automated Protocol Compliance Audit](#automated-protocol-compliance-audit) (this file) |
| Best practices | [task-protocol-operations.md](task-protocol-operations.md) |

---

## 📖 DOCUMENTATION STRUCTURE GUIDE {#documentation-structure-guide}

This document contains BOTH ideal workflow documentation AND recovery procedures. Understanding when to reference each is critical.

### Ideal State vs Recovery Documentation {#ideal-state-vs-recovery-documentation}

This protocol document is organized into two logical layers:

**Layer 1: IDEAL STATE DOCUMENTATION** (Primary workflow - read this first):
- Describes the EXPECTED execution path when everything works correctly
- Shows standard state transitions without interruptions or failures
- Example sections:
  - "STATE MACHINE ARCHITECTURE" - Core workflow
  - "State Definitions" - What each state does in ideal case
  - "INIT → CLASSIFIED" - Standard transition procedure
  - "REQUIREMENTS → SYNTHESIS" - Normal progression

**Layer 2: RECOVERY DOCUMENTATION** (Exception handling - read when issues occur):
- Describes HOW TO RECOVER when things don't go as planned
- Shows exception handling, interruptions, and failure recovery
- Example sections:
  - "Agent Invocation Interruption Handling" - Handling user interruptions
  - "Partial Agent Completion Handling" - Some agents fail
  - "Recovery from Crashed Sessions" - Session interruption recovery
  - "Violation Recovery Patterns" - Protocol violation fixes

### When to Read Each Layer {#when-to-read-each-layer}

**During Normal Task Execution** (no issues):
✅ Read IDEAL STATE sections to understand standard workflow
✅ Follow state transition procedures as documented
✅ Skip recovery sections unless you encounter issues
✅ Focus on "happy path" execution

**When Issues Occur** (interruptions, failures, violations):
✅ First identify the issue type (agent failure, build error, interruption)
✅ Then reference the appropriate RECOVERY section
✅ Follow recovery procedures to return to ideal state
✅ Resume normal execution after recovery

### Navigation Patterns {#navigation-patterns}

**First-Time Reading** (learning the protocol):
```
Step 1: Read "STATE MACHINE ARCHITECTURE" (ideal state)
Step 2: Read "State Definitions" (ideal state)
Step 3: Read "State Transitions" sections (ideal state)
Step 4: SKIP recovery sections on first read
Step 5: Return to recovery sections when needed
```

**Executing a Task** (applying the protocol):
```
Step 1: Follow ideal state transitions in order
Step 2: If interruption occurs → Jump to "Interruption Handling" (recovery)
Step 3: If agent fails → Jump to "Partial Agent Completion" (recovery)
Step 4: If build fails → Jump to "Violation Recovery" (recovery)
Step 5: After recovery → Return to ideal state workflow
```

**Debugging an Issue** (troubleshooting):
```
Step 1: Identify current state from lock file
Step 2: Identify issue type (interruption, failure, violation)
Step 3: Jump directly to relevant RECOVERY section
Step 4: Apply recovery procedure
Step 5: Verify return to ideal state
```

### Section Type Indicators {#section-type-indicators}

Throughout this document, section headers indicate their type:

**Ideal State Sections** (standard workflow):
- Headers describe state transitions: "INIT → CLASSIFIED"
- Content focuses on: requirements, procedures, success criteria
- Reading order: Sequential from INIT through CLEANUP

**Recovery Sections** (exception handling):
- Headers mention recovery/handling: "Agent Invocation Interruption Handling"
- Content focuses on: problems, detection, recovery actions
- Reading order: As needed when specific issues occur

### Quick Reference: Section Classification {#quick-reference-section-classification}

| Section Type | Purpose | When to Read |
|--------------|---------|--------------|
| **State Machine Architecture** | Ideal workflow | Always (first read) |
| **State Definitions** | Ideal behavior | Always (first read) |
| **State Transitions** | Standard procedures | During execution |
| **Interruption Handling** | Recovery from user interruptions | Only when interrupted |
| **Partial Completion** | Recovery from agent failures | Only when agents fail |
| **Violation Recovery** | Recovery from protocol violations | Only when violations detected |
| **Checkpoint Enforcement** | Ideal checkpoint behavior | During checkpoint wait |

---

## STATE MACHINE ARCHITECTURE {#state-machine-architecture}

### Core States {#core-states}
```
INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → [PLAN APPROVAL] → IMPLEMENTATION (iterative rounds) → VALIDATION → REVIEW → AWAITING_USER_APPROVAL → COMPLETE → CLEANUP
                                      ↑                                ↓                                                    ↓
                                      └────────────────────────────────┴──────────── SCOPE_NEGOTIATION ←──────────────────┘
```

**User Approval Checkpoints:**
-  **[PLAN APPROVAL]**: After SYNTHESIS, before IMPLEMENTATION - User reviews and approves implementation plan
  (all agents' plans in task.md)
-  **[CHANGE REVIEW]**: After REVIEW (unanimous stakeholder approval) - Transition to AWAITING_USER_APPROVAL
  state

### State Definitions {#state-definitions}
- **INIT**: Task selected, locks acquired, session validated, task worktree and agent worktrees created
- **CLASSIFIED**: Risk level determined, agents selected, isolation established
-  **REQUIREMENTS**: All stakeholder agents contribute requirements to task.md, negotiate conflicts, finalize
  task.md
-  **SYNTHESIS**: Create detailed implementation plan in task.md with file-level specificity, exact method
  signatures, and complete test specifications. **USER APPROVAL CHECKPOINT: Present comprehensive plan with
  all implementation details - user approves the WHAT and HOW before any code is written**
-  **IMPLEMENTATION**: Iterative rounds where each agent implements domain requirements in parallel, rebases
  on task branch, merges changes (continues until all agents report no more work)
- **VALIDATION**: Final build verification after all implementation rounds complete
-  **REVIEW**: All stakeholder agents review task branch against requirements, accept (no violations) or
  reject (violations found) - If ANY reject → back to IMPLEMENTATION rounds. **After unanimous approval, MUST
  transition to AWAITING_USER_APPROVAL state**
-  **AWAITING_USER_APPROVAL**: **MANDATORY CHECKPOINT STATE - All agents accepted, changes committed to task
  branch, waiting for user review and approval before finalizing. Main agent MUST present changes, ask for
  approval, wait for user response, create approval flag, then transition to COMPLETE. This state CANNOT be
  skipped.**
-  **SCOPE_NEGOTIATION**: Determine what work can be deferred when agents reject due to scope concerns (ONLY
  when resolution effort > 2x task scope AND agent consensus permits deferral - escalate based on agent tiers,
  architect makes final decision)
-  **COMPLETE**: Work merged to main branch with atomic documentation update (task implementation + todo.md +
  changelog.md in single commit), dependent tasks unblocked (only after user approves changes)
- **CLEANUP**: All agent worktrees removed, task worktree removed, locks released, temporary files cleaned

### Detailed Implementation Plan Requirements (SYNTHESIS State) {#detailed-implementation-plan-requirements}

**MANDATORY**: The implementation plan created during SYNTHESIS must be detailed enough that implementation
becomes mechanical - NO significant decisions should remain for the implementation phase.

**🎯 Goal**: When a user approves an implementation plan, they know EXACTLY what will be created, where,
and how. No surprises during implementation.

#### Required Plan Components {#required-plan-components}

**1. FILE MANIFEST** - Complete list of all files to be created or modified:
```markdown
## Files to Create
| File Path | Purpose | Lines (est.) |
|-----------|---------|--------------|
| `formatter/src/main/java/.../FormattingRule.java` | Core interface | ~50 |
| `formatter/src/main/java/.../FormattingConfiguration.java` | Config base | ~30 |
| `formatter/src/test/java/.../FormattingRuleTest.java` | Unit tests | ~150 |

## Files to Modify
| File Path | Changes |
|-----------|---------|
| `pipeline/src/main/java/.../FormatStage.java` | Update format() signature |
| `cli/src/main/java/.../CliMain.java` | Create config list |
```

**2. API SPECIFICATIONS** - Exact method signatures with JavaDoc summaries:
```markdown
## API Design

### FormattingRule Interface
```java
public interface FormattingRule {
    /**
     * Analyzes source code for formatting violations.
     * @param context transformation context with source and metadata
     * @param configs list of all configurations (rule finds its own)
     * @return list of violations found, empty if compliant
     */
    List<FormattingViolation> analyze(TransformationContext context,
                                       List<FormattingConfiguration> configs);

    /**
     * Formats source code according to rule's style.
     * @param context transformation context with source
     * @param configs list of all configurations
     * @return formatted source code
     */
    String format(TransformationContext context,
                  List<FormattingConfiguration> configs);
}
```

### FormattingConfiguration.findConfig() Helper
```java
/**
 * Finds configuration of specified type from config list.
 * @param configs all configurations passed to rule
 * @param configType the configuration class to find
 * @param defaultSupplier provides default if not found
 * @return matching config or default
 * @throws IllegalArgumentException if multiple configs of same type
 */
static <T extends FormattingConfiguration> T findConfig(
    List<FormattingConfiguration> configs,
    Class<T> configType,
    Supplier<T> defaultSupplier);
```
```

**3. BEHAVIORAL SPECIFICATIONS** - How the code should behave in specific scenarios:
```markdown
## Behavior Specifications

### Config Lookup Behavior
| Scenario | Input | Expected Behavior |
|----------|-------|-------------------|
| Matching config exists | `[LineConfig, BraceConfig]` for BraceRule | Returns BraceConfig |
| No matching config | `[LineConfig]` for BraceRule | Returns default from supplier |
| Multiple same type | `[BraceConfig, BraceConfig]` | Throws IllegalArgumentException |
| Null config list | `null` | Returns default from supplier |
| Empty config list | `[]` | Returns default from supplier |

### Rule Format Behavior
| Input | Config | Expected Output |
|-------|--------|-----------------|
| `class Test{` | BraceStyle.SAME_LINE | `class Test {` |
| `class Test{` | BraceStyle.NEW_LINE | `class Test\n{` |
```

**4. TEST SPECIFICATIONS** - Exact test cases to implement:
```markdown
## Test Cases

### FormattingConfiguration.findConfig() Tests
| Test Name | Input | Expected |
|-----------|-------|----------|
| `shouldReturnMatchingConfig` | List with matching type | Returns that config |
| `shouldReturnDefaultWhenNoMatch` | List without matching type | Returns default |
| `shouldThrowOnDuplicateConfig` | List with 2 same type | IllegalArgumentException |
| `shouldHandleNullList` | null | Returns default |
| `shouldHandleEmptyList` | [] | Returns default |

### BraceFormattingRule.analyze() Tests
| Test Name | Source | Config | Expected Violations |
|-----------|--------|--------|---------------------|
| `shouldDetectMissingSpaceBeforeBrace` | `class Test{` | SAME_LINE | 1 violation at col 10 |
| `shouldAcceptCorrectSameLineStyle` | `class Test {` | SAME_LINE | 0 violations |
```

**5. IMPLEMENTATION SEQUENCE** - Ordered steps with dependencies:
```markdown
## Implementation Sequence

### Phase 1: Core Interfaces (architect agent)
1. Create `FormattingConfiguration` base record with `findConfig()` helper
2. Update `FormattingRule` interface with new `List<>` signatures
3. Compile and verify no existing code breaks

### Phase 2: Rule Updates (engineer agent, depends on Phase 1)
1. Update `BraceFormattingRule` to use `findConfig()`
2. Update `LineLengthFormattingRule` to use `findConfig()`
3. Update remaining 3 rules
4. Verify all rules compile

### Phase 3: Pipeline Integration (engineer agent, depends on Phase 2)
1. Update `FormatStage.format()` to pass config list
2. Update `ProcessingContext` if needed
3. Verify pipeline compiles

### Phase 4: CLI Integration (engineer agent, depends on Phase 3)
1. Update `CliMain` to create `List<FormattingConfiguration>`
2. Pass config list through pipeline
3. Verify end-to-end flow

### Phase 5: Test Updates (tester agent, depends on Phases 1-4)
1. Update all existing tests to use `List.of(config)`
2. Add new tests for `findConfig()` behavior
3. Add tests for duplicate config detection
4. Verify 100% test pass rate
```

**6. DECISION LOG** - Decisions already made (NOT for implementation phase):
```markdown
## Decisions Made During Requirements/Synthesis

| Decision | Options Considered | Chosen | Rationale |
|----------|-------------------|--------|-----------|
| Config lookup method | Instance method vs static | Static helper | Reusable across all rules |
| Missing config behavior | Throw vs default | Return default | Backward compatible |
| Duplicate config behavior | Ignore vs throw | Throw exception | Fail-fast, prevent bugs |
| Method location | FormattingRule vs Config | FormattingConfiguration | Closer to config domain |
```

#### Plan Presentation Format for User Approval {#plan-presentation-format}

When presenting the plan to the user, use this comprehensive format:

```markdown
## Implementation Plan Summary

**Task**: [Task name from todo.md]
**Risk Level**: [HIGH/MEDIUM/LOW]
**Stakeholder Agents**: [List of agents consulted, e.g., architect, formatter, tester]

### Problem Statement
[2-3 sentences describing the problem being solved and why it matters]

### Proposed Solution
[2-3 sentences describing the approach at a high level]

### Scope Summary
**Files to create**: [X files, ~Y lines]
**Files to modify**: [A files, ~B lines changed]
**Test coverage**: [N new tests, M existing tests affected]

### Key Design Decisions
| Decision | Choice | Rationale |
|----------|--------|-----------|
| [Design decision 1] | [Choice made] | [Why this choice] |
| [Design decision 2] | [Choice made] | [Why this choice] |

### Files to Create/Modify
| File Path | Purpose | Changes |
|-----------|---------|---------|
| `path/to/file.java` | [Purpose] | [Create/Modify: description] |

### Key API Signatures
[Most important 2-3 method signatures that define the feature]
```java
// Example signature with brief description
```

### Behavioral Summary
| Scenario | Input | Expected Behavior |
|----------|-------|-------------------|
| [Scenario 1] | [Input] | [Expected result] |

### Implementation Phases
1. **Phase 1: [Name]** - [Description] (Agent: [agent name])
2. **Phase 2: [Name]** - [Description] (Agent: [agent name])
[Continue for all phases]

### Success Criteria
- [ ] [Criterion 1 - what must be true for implementation to be complete]
- [ ] [Criterion 2]
- [ ] Build passes with zero test failures
- [ ] Checkstyle and PMD pass with zero violations

### Risks and Mitigations
| Risk | Likelihood | Mitigation |
|------|------------|------------|
| [Risk 1] | [High/Medium/Low] | [How we'll address it] |

---

**Full implementation details**: See `/workspace/tasks/{task-name}/task.md`

**Please review and respond with:**
- "approved" / "proceed" to begin implementation
- Or provide feedback for plan revisions
```

**CRITICAL**: The summary must provide enough context that the user can make an informed decision
WITHOUT reading the full task.md file. Include all major decisions and their rationale.

#### What the User is Approving {#what-user-approves}

When the user approves a SYNTHESIS plan, they are agreeing to:

✅ **THESE ARE FIXED** (user approved, no changes without re-approval):
- File paths and names
- Method signatures and return types
- Core behavioral logic (what the code does)
- Test case specifications
- Implementation sequence and phases

⚠️ **THESE MAY VARY** (implementation details within approved scope):
- Internal variable names
- Loop vs stream implementation
- Exact line counts
- Helper method decomposition
- Import ordering

❌ **THESE REQUIRE RE-APPROVAL** (deviation from plan):
- Adding/removing files not in manifest
- Changing method signatures
- Changing behavior (different edge case handling)
- Skipping planned test cases
- Changing implementation sequence

### User Approval Checkpoints - MANDATORY REGARDLESS OF BYPASS MODE {#user-approval-checkpoints---mandatory-regardless-of-bypass-mode}

**🚨 BYPASS MODE DOES NOT BYPASS USER APPROVAL CHECKPOINTS**

**AUTONOMY vs CHECKPOINTS CLARIFICATION**:

The Task Protocol requires both:
1. **Autonomous completion** of work between checkpoints
2. **Mandatory user approval** at two specific checkpoints

These are NOT contradictory. Understanding the distinction:

**"Autonomous Completion" Definition**:
- Agent completes work WITHOUT asking for help, guidance, or direction BETWEEN checkpoints
- Agent solves problems, debugs issues, and makes implementation decisions independently
- Agent does NOT stop mid-protocol to ask "what should I do next?"
- Agent does NOT abandon tasks due to complexity without exhausting recovery procedures

**"Checkpoints" Definition**:
- Planned pauses BUILT INTO the protocol state machine for user oversight
- Occur at exactly TWO transition points: SYNTHESIS → IMPLEMENTATION and REVIEW → COMPLETE
- Allow user to validate direction before major work (plan approval) and review results after completion (change approval)
- These are EXPECTED parts of the workflow, NOT violations of autonomous completion

**Examples: Autonomous Completion WITH Checkpoints**:

✅ **CORRECT Autonomous Behavior**:
```
REQUIREMENTS: Agent gathers all requirements autonomously (no questions)
SYNTHESIS: Agent drafts complete implementation plan autonomously (no questions)
[PLAN APPROVAL]: Agent presents plan, waits for approval ← CHECKPOINT (expected)
IMPLEMENTATION: Agent implements according to plan autonomously (no questions)
VALIDATION: Agent fixes issues autonomously (no questions)
REVIEW: Agent addresses agent feedback autonomously (no questions)
[CHANGE REVIEW]: Agent presents changes, waits for approval ← CHECKPOINT (expected)
COMPLETE: Agent merges work autonomously (no questions)
```

❌ **WRONG: Mid-Protocol Handoffs** (these violate autonomous completion):
```
REQUIREMENTS: Agent stops to ask "should I gather more requirements?"
SYNTHESIS: Agent stops to ask "what architecture should I use?"
IMPLEMENTATION: Agent stops to ask "how should I handle this error?"
VALIDATION: Agent stops to ask "should I fix this or delegate it?"
```

**Checkpoint 1: [PLAN APPROVAL] - After SYNTHESIS, Before IMPLEMENTATION**
- MANDATORY: Present implementation plan to user in clear, readable format
- MANDATORY: Wait for explicit user approval message
- PROHIBITED: Assuming user approval from bypass mode or lack of response
-  PROHIBITED: Proceeding to IMPLEMENTATION without clear "yes", "approved", "proceed", or equivalent
  confirmation
- **NOT A VIOLATION**: This checkpoint is EXPECTED and part of autonomous completion protocol

**Checkpoint 2: [CHANGE REVIEW] - After REVIEW, Before COMPLETE**
- MANDATORY: Present completed changes with commit SHA to user
- MANDATORY: Wait for explicit user review approval
- PROHIBITED: Assuming user approval from unanimous agent approval alone
- PROHIBITED: Proceeding to COMPLETE without clear user confirmation
- **NOT A VIOLATION**: This checkpoint is EXPECTED and part of autonomous completion protocol

**Verification Questions Before Proceeding:**
Before SYNTHESIS → IMPLEMENTATION:
- [ ] Did I present the complete implementation plan to the user?
- [ ] Did the user explicitly approve the plan with words like "yes", "approved", "proceed", "looks good"?
- [ ] Did I assume approval from bypass mode? (VIOLATION if yes)

Before REVIEW → COMPLETE:
- [ ] Did I present the completed changes with commit SHA?
- [ ] Did the user explicitly approve proceeding to finalization?
- [ ] Did I assume approval from agent consensus alone? (VIOLATION if yes)

### Checkpoint Wait Behavior {#checkpoint-wait-behavior}

**CRITICAL GUIDANCE**: While waiting at user approval checkpoints, the main agent must follow specific behavior patterns to maintain protocol integrity while remaining responsive to user needs.

**Two Checkpoint Types**:
1. **PLAN APPROVAL** (after SYNTHESIS, before IMPLEMENTATION)
   - Lock state: SYNTHESIS
   - Waiting for: User to approve implementation plan in task.md
   - Typical wait time: Minutes to hours (user reviews plan)

2. **CHANGE REVIEW** (after REVIEW, before COMPLETE)
   - Lock state: AWAITING_USER_APPROVAL
   - Waiting for: User to approve implemented changes
   - Typical wait time: Minutes to hours (user reviews commit)

**Permitted Activities During Checkpoint Wait**:

✅ **ALWAYS PERMITTED** (maintains protocol state):
- Answer user questions about the plan/changes
- Provide clarifications about implementation approach
- Explain technical decisions made during prior states
- Show file diffs or commit details on request
- Respond to audit commands (`/audit-session`)

✅ **PERMITTED IF RELATED TO CURRENT CHECKPOINT**:
- Make minor corrections to plan presentation (formatting, typos in task.md)
- Add clarifying details to implementation plan if user requests
- Generate additional documentation about proposed changes
- Provide estimates or risk assessments for proposed approach

❌ **PROHIBITED** (violates checkpoint semantics):
- Starting IMPLEMENTATION work before plan approval received
- Modifying implementation files while awaiting change review
- Transitioning to next state without explicit approval
- Working on different tasks (violates single-task protocol)
- Assuming user will approve and pre-executing next state work

**Handling User Requests During Checkpoint Wait**:

**Scenario 1: User Requests Plan/Change Modifications**
```markdown
User at PLAN APPROVAL: "Can you add error handling details to the plan?"

Permitted Response:
1. Update task.md with additional error handling section
2. Re-present updated plan to user
3. Continue waiting for approval of revised plan
4. Do NOT proceed to IMPLEMENTATION until approval received
```

**Scenario 2: User Asks Clarifying Questions**
```markdown
User at CHANGE REVIEW: "How did you handle the edge case for empty input?"

Permitted Response:
1. Answer question with reference to implementation
2. Show relevant code sections if helpful
3. Remain in AWAITING_USER_APPROVAL state
4. Do NOT assume question indicates approval
5. Wait for explicit "proceed" or "approved" message
```

**Scenario 3: User Runs Audit Command**
```markdown
User at PLAN APPROVAL: "/audit-session"

Permitted Response:
1. Execute full audit pipeline
2. Report audit results to user
3. Return to PLAN APPROVAL checkpoint wait
4. Lock state remains SYNTHESIS throughout
5. Checkpoint approval still required after audit
```

**Scenario 4: User Requests Unrelated Work**
```markdown
User at CHANGE REVIEW: "While I review, can you update the README?"

PROHIBITED Response (violates single-task protocol):
❌ "Sure, I'll update the README while you review"
❌ Starting work on README changes

REQUIRED Response:
✅ "I'm currently waiting for your approval of the task branch changes (commit SHA: abc123).
    Due to task protocol isolation requirements, I cannot work on other files until
    this task completes CLEANUP state. Would you like me to:
    1. Continue waiting for your review approval, OR
    2. Abandon current task and start README update task (will lose checkpoint progress)"
```

**Wait Timeout Behavior**:

**NO AUTOMATIC TIMEOUT**: Agent must wait indefinitely for user approval at checkpoints.

User may need extended time to:
- Review complex implementation plans thoroughly
- Test changes locally before approving
- Consult with team members
- Review security implications
- Validate against requirements

**User Responsibilities During Wait**:
- Review presented plan/changes
- Ask clarifying questions if needed
- Provide explicit approval or request changes
- If abandoning task, explicitly instruct agent to stop

**Context Compaction Handling**:

If context compaction occurs while waiting at checkpoint:
1. SessionStart hook detects checkpoint state
2. Hook reminds agent of checkpoint requirements
3. Agent re-presents plan/changes to user
4. Wait for approval resumes from where interrupted

**Checkpoint State Recovery After Compaction**:
```bash
# SessionStart hook detects checkpoint
LOCK_STATE=$(jq -r '.state' /workspace/tasks/{TASK_NAME}/task.json)

if [ "$LOCK_STATE" = "SYNTHESIS" ]; then
    # Check for plan approval flag
    if [ ! -f "/workspace/tasks/{TASK_NAME}/user-plan-approval-obtained.flag" ]; then
        echo "📋 CHECKPOINT RECOVERY: Task waiting for PLAN APPROVAL"
        echo "Action: Re-present implementation plan from task.md"
        echo "Wait for explicit user approval before IMPLEMENTATION"
    fi
elif [ "$LOCK_STATE" = "AWAITING_USER_APPROVAL" ]; then
    # Check for change review approval flag
    if [ ! -f "/workspace/tasks/{TASK_NAME}/user-approval-obtained.flag" ]; then
        COMMIT_SHA=$(jq -r '.checkpoint.commit_sha' /workspace/tasks/{TASK_NAME}/task.json)
        echo "📋 CHECKPOINT RECOVERY: Task waiting for CHANGE REVIEW"
        echo "Action: Re-present changes at commit $COMMIT_SHA"
        echo "Wait for explicit user approval before COMPLETE"
    fi
fi
```

**Approval Signal Detection**:

Agent must recognize these approval patterns:

**PLAN APPROVAL Signals**:
- ✅ "Yes, proceed with implementation"
- ✅ "Approved, go ahead"
- ✅ "Looks good, please continue"
- ✅ "LGTM" (Let's Get This Made)
- ✅ "Proceed"
- ❌ "Interesting approach" (NOT approval, just commentary)
- ❌ "continue" alone (ambiguous, could mean continue waiting)

**CHANGE REVIEW Approval Signals**:
- ✅ "Changes look good, please finalize"
- ✅ "Approved, you can merge"
- ✅ "LGTM, proceed with cleanup"
- ✅ "Yes, approved"
- ❌ "Nice work" (NOT approval, just praise)
- ❌ "Okay" alone (ambiguous without review context)

**Ambiguous Message Handling**:
```markdown
User: "continue"

Agent Response:
"I'm currently waiting for your approval of [plan/changes]. To confirm:
 - If you approve: Please say 'yes, approved' or 'proceed with [implementation/finalization]'
 - If you need changes: Please specify what to modify
 - If you want me to continue waiting: I will wait for your review"
```

**CRITICAL**: When in doubt, ask for clarification rather than assuming approval.

### Automated Checkpoint Enforcement {#automated-checkpoint-enforcement}

**Approval Marker File**: `/workspace/tasks/{task-name}/user-approval-obtained.flag`
- Required for COMPLETE state transition
- Automatically managed by enforcement system

**Required Approval Patterns**:
- User message contains approval keywords: "yes", "approved", "approve", "proceed", "looks good", "LGTM"
- AND message references review context: "review", "changes", "commit", "finalize"

### State Transitions {#state-transitions}
Each transition requires **ALL** specified conditions to be met. **NO EXCEPTIONS.**

## TERMINOLOGY GLOSSARY {#terminology-glossary}

**State**: One of 10 formal states in the task protocol state machine (INIT, CLASSIFIED, REQUIREMENTS,
SYNTHESIS, IMPLEMENTATION, VALIDATION, REVIEW, AWAITING_USER_APPROVAL, COMPLETE, CLEANUP)

**State Transition**: Movement from one state to another after meeting all transition conditions

**Implementation Round**: Iterative cycle within IMPLEMENTATION state where agents develop, validate, and
refine code until all agents report no more work

**Checkpoint**: MANDATORY pause in protocol requiring explicit user approval before continuing (PLAN APPROVAL
after SYNTHESIS and CHANGE REVIEW after REVIEW)

**Risk Level**: Classification of file modification impact (HIGH/MEDIUM/LOW) determining workflow variant

**Agent**: Stakeholder specialist (architect, style, etc.) providing domain-specific
validation

**Worktree**: Git worktree providing isolated workspace for task development without affecting main branch

**Lock File**: JSON file at `/workspace/tasks/{task-name}/task.json` ensuring single-session task ownership

**Scope Negotiation**: State for determining what work can be deferred when resolution effort exceeds 2x task
scope

**Unanimous Approval**: Required condition where ALL agents must respond with "✅ APPROVED" before proceeding

## RISK-BASED AGENT SELECTION ENGINE {#risk-based-agent-selection-engine}

### Agent Classification {#agent-classification}

**STAKEHOLDER AGENTS** (for task implementation):
- `architect`, `architect`
- `style`, `style`
- `quality`, `quality`
- `test`, `test`
- `build`, `build`
- `security`, `security`
- `performance`, `performance`
- `usability`, `usability`

**NON-STAKEHOLDER AGENTS** (excluded from task classification):
- `config`, `config` - Claude Code configuration management only
- `parse-conversation-timeline skill`, `audit-protocol-compliance skill`, `audit-protocol-efficiency skill` - Audit pipeline only

**CRITICAL RULE**: When performing CLASSIFIED state agent selection, ONLY select from stakeholder agents. NEVER include config-* or process-* agents in task worktree agent selection. These agents serve meta-purposes (configuration management, process auditing) and are NOT involved in task implementation.

### Automatic Risk Classification {#automatic-risk-classification}
**Input**: File paths from modification request
**Process**: Pattern matching → Escalation trigger analysis → Agent set determination
**Output**: Risk level (HIGH/MEDIUM/LOW) + Required agent set from STAKEHOLDER AGENTS only

### HIGH-RISK FILES (Complete Validation Required) {#high-risk-files-complete-validation-required}
**Patterns:**
- `src/**/*.java` (core implementation)
- `pom.xml`, `**/pom.xml` (build configuration)
- `.github/**` (CI/CD workflows)
- `**/security/**` (security components)
- `checkstyle.xml`, `**/checkstyle*.xml` (style enforcement)
- `CLAUDE.md` (critical configuration)
- `docs/project/task-protocol.md` (protocol configuration)
- `docs/project/critical-rules.md` (safety rules)

**Required Agents**: architect, style, quality, build
**Additional Agents**: security (if security-related), performance (if performance-critical),
test (if new functionality), usability (if user-facing)

### MEDIUM-RISK FILES (Domain Validation Required) {#medium-risk-files-domain-validation-required}
**Patterns:**
- `src/test/**/*.java` (test files)
- `docs/code-style/**` (style documentation)
- `**/resources/**/*.properties` (configuration)
- `**/*Test.java`, `**/*Tests.java` (test classes)

**Required Agents**: architect, quality
**Additional Agents**: style (if style files), security (if config files),
performance (if benchmarks)

### LOW-RISK FILES (Minimal Validation Required) {#low-risk-files-minimal-validation-required}
**Patterns:**
- `*.md` (except CLAUDE.md, task-protocol.md, critical-rules.md)
- `docs/**/*.md` (general documentation)
- `todo.md` (task tracking)
- `*.txt`, `*.log` (text files)
- `**/README*` (readme files)

**Required Agents**: None (unless escalation triggered)

### Escalation Triggers {#escalation-triggers}
**Keywords**: "security", "architecture", "breaking", "performance", "concurrent", "database", "api", "state",
"dependency"
**Content Analysis**: Cross-module dependencies, security implications, architectural changes
**Action**: Force escalation to next higher risk level

### Manual Overrides {#manual-overrides}
**Force Full Protocol**: `--force-full-protocol` flag for critical changes
**Explicit Risk Level**: `--risk-level=HIGH|MEDIUM|LOW` to override classification
**Escalation Keywords**: "security", "architecture", "breaking" in task description

### Risk Assessment Audit Trail {#risk-assessment-audit-trail}
**Required Logging:**
- Risk level selected (HIGH/MEDIUM/LOW)
- Classification method (pattern match, keyword trigger, manual override)
- Escalation triggers activated (if any)
- Workflow variant executed (FULL/ABBREVIATED/STREAMLINED)
- Agent set selected for review
- Final outcome (approved/rejected/deferred)

**Implementation**: Log in state.json file and commit messages for audit purposes

## WORKFLOW VARIANTS BY RISK LEVEL {#workflow-variants-by-risk-level}

### HIGH_RISK_WORKFLOW (Complete Validation) {#high_risk_workflow-complete-validation}
**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW →
COMPLETE → CLEANUP
**Stakeholder Agents**: All agents based on task requirements
**Isolation**: Mandatory worktree isolation
**Review**: Complete stakeholder validation
**Use Case**: Core implementation, build configuration, security, CI/CD
**Conditional Skips**: None - all validation required

### MEDIUM_RISK_WORKFLOW (Domain Validation) {#medium_risk_workflow-domain-validation}
**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW →
COMPLETE → CLEANUP
**Stakeholder Agents**: Based on change characteristics
- Base: architect (always required)
- +formatter: If style/formatting files modified
- +security: If any configuration or resource files modified
- +performance: If test performance or benchmarks affected
- +engineer: Always included for code quality validation
**Isolation**: Worktree isolation for multi-file changes
**Review**: Domain-appropriate stakeholder validation
**Use Case**: Test files, style documentation, configuration files
**Conditional Skips**: May skip IMPLEMENTATION/VALIDATION states if only documentation changes

### LOW_RISK_WORKFLOW (Streamlined Validation) {#low_risk_workflow-streamlined-validation}
**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → COMPLETE → CLEANUP
**Stakeholder Agents**: None (unless escalation triggered)
**Isolation**: Required for multi-file changes, optional for single documentation file
**Review**: Evidence-based validation and automated checks
**Safety Gates**:
- Verify no cross-references to modified files in src/
- Confirm no build configuration impact
- Validate no security-sensitive content changes
**Use Case**: Documentation updates, todo.md, README files
**Conditional Skips**: Skip IMPLEMENTATION, VALIDATION, REVIEW states entirely

### Conditional State Transition Logic {#conditional-state-transition-logic}
```python
def determine_state_path(risk_level, change_type):
    """Determine which states to execute based on risk and change type"""

    base_states = ["INIT", "CLASSIFIED", "REQUIREMENTS", "SYNTHESIS"]

    if risk_level == "HIGH":
        return base_states + ["IMPLEMENTATION", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]

    elif risk_level == "MEDIUM":
        if change_type in ["documentation_only", "config_only"]:
            return base_states + ["COMPLETE", "CLEANUP"]
        else:
            return base_states + ["IMPLEMENTATION", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]

    elif risk_level == "LOW":
        return base_states + ["COMPLETE", "CLEANUP"]

    else:
        # Default to HIGH risk if uncertain
        return base_states + ["IMPLEMENTATION", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]
```

### Skip Condition Examples {#skip-condition-examples}
**IMPLEMENTATION/VALIDATION State Skip Conditions:**
- Maven dependency additions (configuration only)
- Build plugin configuration changes
- Documentation updates without code references
- Property file modifications
- Version bumps without code changes
- README and markdown file updates
- Todo.md task tracking updates

**Full Validation Required Conditions:**
- Any source code modifications (*.java, *.js, *.py, etc.)
- Runtime behavior changes expected
- Security-sensitive configuration changes
- Build system modifications affecting compilation
- API contract modifications

## AGENT SELECTION DECISION TREE {#agent-selection-decision-tree}

### Comprehensive Agent Selection Framework {#comprehensive-agent-selection-framework}
**Input**: Task description and file modification patterns
**Available Agents**: architect, usability, performance, security,
formatter, engineer, tester, builder

**Processing Logic:**

**🚨 CORE AGENTS (Always Required):**
- **architect**: MANDATORY for ALL file modification tasks (provides implementation requirements)

**🔍 FUNCTIONAL AGENTS (Code Implementation):**
- IF NEW CODE created: add formatter, engineer, builder
- IF IMPLEMENTATION (not just config): add tester
- IF MAJOR FEATURES completed: add usability (MANDATORY after completion)

**🛡️ SECURITY AGENTS (Actual Security Concerns):**
- IF AUTHENTICATION/AUTHORIZATION changes: add security
- IF EXTERNAL API/DATA integration: add security
- IF ENCRYPTION/CRYPTOGRAPHIC operations: add security
- IF INPUT VALIDATION/SANITIZATION: add security

**⚡ PERFORMANCE AGENTS (Performance Critical):**
- IF ALGORITHM optimization tasks: add performance
- IF DATABASE/QUERY optimization: add performance
- IF MEMORY/CPU intensive operations: add performance

**🔧 FORMATTING AGENTS (Code Quality):**
- IF PARSER LOGIC modified: add performance, security
- IF AST TRANSFORMATION changed: add engineer, tester
- IF FORMATTING RULES affected: add formatter

**❌ AGENTS NOT NEEDED FOR SIMPLE OPERATIONS:**
- Maven module renames: NO performance
- Configuration file updates: NO security unless changing auth
- Directory/file renames: NO performance
- Documentation updates: Usually only architect

**📊 ANALYSIS AGENTS (Research/Study Tasks):**
- IF ARCHITECTURAL ANALYSIS: add architect
- IF PERFORMANCE ANALYSIS: add performance
- IF UX/INTERFACE ANALYSIS: add usability
- IF SECURITY ANALYSIS: add security
- IF CODE QUALITY REVIEW: add engineer
- IF PARSER/FORMATTER PERFORMANCE ANALYSIS: add performance

**Agent Selection Verification Checklist:**
- [ ] NEW CODE task → formatter included?
- [ ] Source files created/modified → builder included?
- [ ] Performance-critical code → performance included?
- [ ] Security-sensitive features → security included?
- [ ] User-facing interfaces → usability included?
- [ ] Post-implementation refactoring → engineer included?
- [ ] AST parsing/code formatting → performance included?

**Special Agent Usage Patterns:**
-  **formatter**: Apply ALL manual style guide rules from docs/code-style/ (Java, common, and
  language-specific patterns)
-  **builder**: For style/formatting tasks, triggers linters (checkstyle, PMD, ESLint) through builder
  system
-  **builder**: Use alongside formatter to ensure comprehensive validation (automated + manual
  rules)
- **engineer**: Post-implementation refactoring and best practices enforcement
- **tester**: Business logic validation and comprehensive test creation
- **security**: Data handling and storage compliance review
- **performance**: Algorithmic efficiency and resource optimization
- **usability**: User experience design and interface evaluation
- **architect**: System architecture and implementation guidance

## COMPLETE STYLE VALIDATION FRAMEWORK {#complete-style-validation-framework}

### Three-Component Style Validation {#three-component-style-validation}
**MANDATORY PROCESS**: When style validation is required, ALL THREE components must pass:

1. **Automated Linters** (via build):
   - `checkstyle`: Java coding conventions and formatting
   - `PMD`: Code quality and best practices
   - `ESLint`: JavaScript/TypeScript style (if applicable)

2. **Manual Style Rules** (via formatter):
   - Apply ALL detection patterns from `docs/code-style/*-claude.md`
   - Java-specific patterns (naming, structure, comments)
   - Common patterns (cross-language consistency)
   - Language-specific patterns as applicable

3. **Build Integration** (via builder):
   - Automated fixing when conflicts detected (LineLength vs UnderutilizedLines)
   - Use `checkstyle/fixers` module for AST-based consolidate-then-split strategy
   - Comprehensive testing validates fixing logic before application

### Complete Style Validation Gate Pattern {#complete-style-validation-gate-pattern}
```bash
# MANDATORY: Never assume checkstyle-only validation
# CRITICAL ERROR PATTERN: Checking only checkstyle and declaring "no violations found" when PMD/manual violations exist

validate_complete_style_compliance() {
    echo "=== COMPLETE STYLE VALIDATION GATE ==="

    # Component 1: Automated linters via build system
    echo "Validating automated linters..."
    ./mvnw checkstyle:check || return 1
    ./mvnw pmd:check || return 1

    # Component 2: Manual style rules via formatter agent
    echo "Validating manual style rules..."
    invoke_style_auditor_with_manual_detection_patterns || return 1

    # Component 3: Automated fixing integration if conflicts
    echo "Checking for LineLength vs UnderutilizedLines conflicts..."
    if detect_style_conflicts; then
        echo "Applying automated AST-based fixes..."
        apply_automated_style_fixes || return 1
        # Re-validate after automated fixes
        validate_complete_style_compliance
    fi

    echo "✅ Complete style validation passed: checkstyle + PMD + manual rules"
    return 0
}
```


## BATCH PROCESSING AND CONTINUOUS MODE {#batch-processing-and-continuous-mode}

### Batch Processing Restrictions {#batch-processing-restrictions}
**PROHIBITED PATTERNS:**
- Processing multiple tasks sequentially without individual protocol execution
- "Work on all Phase 1 tasks until done" - Must select ONE specific task
- "Complete these 5 tasks" - Each requires separate lock acquisition and worktree
- Assuming research tasks can bypass protocol because they create "only" study files

**MANDATORY SINGLE-TASK PROCESSING:**
1. Select ONE specific task from todo.md
2. Acquire atomic lock for THAT specific task only
3. Create isolated worktree for THAT task only
4. Execute full state machine protocol for THAT task only
5. Complete CLEANUP state before starting any other task

### Automatic Continuous Mode Translation {#automatic-continuous-mode-translation}
**When users request batch operations:**

**AUTOMATIC TRANSLATION PROTOCOL:**
1. **ACKNOWLEDGE**: "I understand you want to work on multiple tasks efficiently..."
2.  **AUTO-TRANSLATE**: "I'll interpret this as a request to work on the todo list in continuous mode,
   processing each task with full protocol isolation..."
3. **EXECUTE**: Automatically trigger continuous workflow mode without requiring user to rephrase

**Batch Request Patterns to Auto-Translate:**
- "Work on all [phase/type] tasks"
- "Complete these tasks until done"
- "Process the todo list"
- "Work on multiple tasks"
- Any request mentioning multiple specific tasks

**Task Filtering for Continuous Mode:**
When batch requests specify subsets:
1. **Phase-based filtering**: Process only tasks in specified phases
2. **Type-based filtering**: Process only tasks matching specified types
3. **Name-based filtering**: Process only specifically mentioned task names
4. **Default behavior**: Process all available tasks if no filter mentioned

## 🧠 PROTOCOL INTERPRETATION MODE {#protocol-interpretation-mode}

**ENHANCED ANALYTICAL RIGOR**: Parent agent must apply deeper analysis when interpreting and following the
task protocol workflow. Rather than surface-level interpretations, carefully analyze what the protocol truly
requires for the specific task context.

**Critical Thinking Requirements:**
- Question assumptions about task scope and complexity
- Verify all transition conditions are genuinely met
- Apply evidence-based validation rather than procedural compliance
- Consider edge cases and alternative approaches
- Maintain skeptical evaluation of "good enough" solutions

## AUTOMATED PROTOCOL COMPLIANCE AUDIT {#automated-protocol-compliance-audit}

**MANDATORY**: After EVERY state transition completion, the main agent MUST invoke the protocol compliance
audit pipeline to verify correct protocol execution.

### 4-Agent Protocol Audit Pipeline {#4-agent-protocol-audit-pipeline}

Detect and prevent protocol violations through systematic, automated checking after each phase.

**Pipeline Architecture**:
```
parse-conversation-timeline skill → audit-protocol-compliance skill → efficiency-optimizer → documentation-auditor
    (facts)           (enforcement)        (performance)        (root causes)
```

### When to Run Protocol Audit {#when-to-run-protocol-audit}

**MANDATORY Audit Triggers**:
- **After each state transition** (INIT→CLASSIFIED, CLASSIFIED→REQUIREMENTS, etc.)
- **Before critical transitions** (especially before IMPLEMENTATION and before COMPLETE)
- **After agent completion** (when all stakeholder agents report COMPLETE)
- **On violation detection** (immediate re-audit after fixing violations)

### Protocol Audit Execution Pattern {#protocol-audit-execution-pattern}

```bash
# Step 1: Collect session facts
process_recorder_output=$(invoke_agent parse-conversation-timeline skill \
  --task-name "${TASK_NAME}" \
  --session-id "${SESSION_ID}")

# Step 2: Check protocol compliance
protocol_audit_result=$(invoke_agent audit-protocol-compliance skill \
  --execution-trace "${process_recorder_output}")

# Step 3: Handle violations
if [[ $(echo "$protocol_audit_result" | jq -r '.overall_verdict') == "FAILED" ]]; then
  echo "❌ Protocol violations detected"

  # Extract violation details
  violations=$(echo "$protocol_audit_result" | jq -r '.violations')

  # Fix violations based on type
  for violation in $violations; do
    check_id=$(echo "$violation" | jq -r '.check_id')

    case "$check_id" in
      "0.2")
        # Main agent implemented during IMPLEMENTATION state
        echo "CRITICAL: Reverting to proper delegation pattern"
        # Return to last safe state and re-execute with proper delegation
        ;;
      *)
        # Other violations - fix according to protocol
        ;;
    esac
  done

  # Re-run audit after fixes
  # Repeat until overall_verdict == "PASSED"

else
  echo "✅ Protocol compliance verified"

  # Step 4: Optimize efficiency (only if compliant)
  efficiency_report=$(invoke_agent efficiency-optimizer \
    --audit-result "${protocol_audit_result}")

  # Step 5: Identify doc gaps (only if needed)
  if [[ $(echo "$efficiency_report" | jq -r '.status') == "COMPLETED" ]]; then
    doc_audit=$(invoke_agent documentation-auditor \
      --violation-context "${protocol_audit_result}")
  fi
fi
```

### Violation Detection and Retry Logic {#violation-detection-and-retry-logic}

**Critical Violation Response Pattern**:

```markdown
IF (audit-protocol-compliance skill returns FAILED):
  1. **STOP immediately** - Do not proceed to next state
  2. **IDENTIFY violation** - Read audit-protocol-compliance skill output for specific check IDs
  3. **APPLY fix** - Based on violation type:
     - Check 1.2 (main agent implementation): Revert changes, re-delegate to agents
     - State sequence violation: Return to skipped state
     - Missing artifacts: Create required files/reports
     - Lock state mismatch: Update lock file to match actual state
  4. **RE-RUN protocol audit** - Verify fix resolved violation
  5. **REPEAT until PASSED** - Cannot proceed until audit-protocol-compliance skill returns PASSED
  6. **ONLY THEN proceed** - After PASSED verdict, continue to next state
```

### Example: IMPLEMENTATION State Transition with Audit {#example-implementation-state-transition-with-audit}

```markdown
## After SYNTHESIS state complete and user approval received: {#after-synthesis-state-complete-and-user-approval-received}

1. Update lock state to IMPLEMENTATION:
   ```bash
   jq '.state = "IMPLEMENTATION"' task.json > task.json.tmp
   mv task.json.tmp task.json
   ```

2. **MANDATORY PROTOCOL AUDIT** before launching agents:
   ```bash
   # Audit current state
   audit-protocol-compliance skill --check-state-transition "SYNTHESIS→IMPLEMENTATION"
   ```

3. **IF AUDIT PASSES**:
   - Launch stakeholder agents in parallel
   - Monitor agent status.json files
   - Continue IMPLEMENTATION rounds

4. **IF AUDIT FAILS**:
   - STOP - do not launch agents
   - Fix violations identified by audit-protocol-compliance skill
   - Re-run audit
   - Only proceed after PASSED verdict

5. **After all agents report COMPLETE**:
   - **MANDATORY PROTOCOL AUDIT** before transitioning to VALIDATION:
     ```bash
     audit-protocol-compliance skill --check-implementation-complete
     ```
   - Verify main agent did not create source files in task worktree
   - Verify all agents merged to task branch
   - Verify lock state matches actual work completed
```

### Protocol Audit Integration Points {#protocol-audit-integration-points}

**State-by-State Audit Requirements**:

| State Transition | Audit Focus | Critical Checks |
|-----------------|-------------|-----------------|
| **INIT → CLASSIFIED** | Infrastructure setup | Lock acquired, worktrees created, pwd verified |
| **CLASSIFIED → REQUIREMENTS** | Agent selection | Risk classified, task.md created, agents selected |
| **REQUIREMENTS → SYNTHESIS** | Requirements complete | All agent reports received, conflicts resolved |
| **SYNTHESIS → IMPLEMENTATION** | Plan approved | User approval obtained, implementation plans in task.md |
| **IMPLEMENTATION → VALIDATION** | Delegation verified | Main agent coordinated (not implemented), agents merged |
| **VALIDATION → REVIEW** | Build passed | Quality gates executed (not cached), tests passed |
| **REVIEW → AWAITING_USER_APPROVAL** | Unanimous approval | All agents approved, no violations found |
| **AWAITING_USER_APPROVAL → COMPLETE** | User approved | User approval flag exists, archival complete, merged to main |
| **COMPLETE → CLEANUP** | Merge complete | Atomic commit: task + todo.md + changelog.md merged to main |

### AWAITING_USER_APPROVAL → COMPLETE Transition {#awaiting-user-approval-complete-transition}

**Trigger**: User explicitly approves the changes (says "approved", "LGTM", "merge it", etc.)

**⚠️ CRITICAL: Archival BEFORE Merge**

The todo.md and changelog.md updates MUST be part of the task branch commit BEFORE merging to main.
This ensures the atomic commit includes both implementation AND archival.

**Transition Steps**:

1. **Create approval flag**:
   ```bash
   touch /workspace/tasks/{task-name}/user-approved-changes.flag
   ```

2. **Use `archive-task` skill** to atomically update archival files:
   ```bash
   # The archive-task skill performs:
   # - Updates todo.md (marks task complete)
   # - Updates changelog.md (adds completion entry)
   # - Commits both changes atomically to task branch
   Skill: archive-task
   ```

3. **Squash task branch commits** (if multiple):
   ```bash
   # Use git-squash skill for safe squashing
   Skill: git-squash
   ```

4. **Merge to main with --ff-only**:
   ```bash
   cd /workspace/main
   git merge --ff-only {task-branch}
   ```

5. **Transition state to COMPLETE**:
   ```bash
   jq --arg old "AWAITING_USER_APPROVAL" --arg new "COMPLETE" \
      --arg ts "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
      '.state = $new | .transition_log += [{"from": $old, "to": $new, "timestamp": $ts}]' \
      task.json > tmp.json && mv tmp.json task.json
   ```

**Common Mistake**:
❌ Merging to main BEFORE updating todo.md and changelog.md (causes orphaned archival)

**Correct Sequence**:
```
User approves → Create flag → archive-task skill → Squash → Merge → Transition to COMPLETE
```

### COMPLETE → CLEANUP Transition {#complete-cleanup-transition}

**Trigger**: After successfully merging task to main branch with --ff-only merge

**Timing**: Execute CLEANUP immediately after COMPLETE state merge is verified on main

**Transition Criteria**:
- ✅ Task branch commits squashed into ONE commit (includes todo.md + changelog.md)
- ✅ Task branch merged to main (`git merge --ff-only`)
- ✅ Atomic commit on main (task + todo.md + changelog.md)
- ✅ Build verification passed on main branch
- ✅ Module exists in main branch (for implementation tasks)

**CLEANUP Execution Sequence**:

```bash
# Step 1: Verify merge to main completed
cd /workspace/main
git log -1 --oneline  # Should show task merge commit

# Step 2: Transition task state to CLEANUP
jq '.state = "CLEANUP" | .cleanup_timestamp = now | toISO8601' \
  /workspace/tasks/{task-name}/task.json > /tmp/task.json.tmp
mv /tmp/task.json.tmp /workspace/tasks/{task-name}/task.json

# Step 3: Remove all worktrees
git worktree remove /workspace/tasks/{task-name}/code
for agent_dir in /workspace/tasks/{task-name}/agents/*/code; do
  [ -d "$agent_dir" ] && git worktree remove --force "$agent_dir"
done

# Step 4: Delete ALL task branches
git branch -D {task-name}
git branch | grep "^  {task-name}-" | xargs -r git branch -D

# Step 5: Verify cleanup complete
git branch | grep "{task-name}" || echo "✅ All task branches deleted"
git worktree list | grep "{task-name}" || echo "✅ All worktrees removed"

# Step 6: Create archival marker (if not exists)
[ -f /workspace/tasks/{task-name}/archival-complete.flag ] || \
  touch /workspace/tasks/{task-name}/archival-complete.flag
```

**What Gets Deleted**:
- Task branch (`{task-name}`)
- All agent branches (`{task-name}-architect`, `{task-name}-engineer`, `{task-name}-formatter`)
- Task worktree (`/workspace/tasks/{task-name}/code/`)
- All agent worktrees (`/workspace/tasks/{task-name}/agents/*/code/`)

**What Gets Preserved**:
- Task directory (`/workspace/tasks/{task-name}/`) with audit files
- `task.json` (with CLEANUP state)
- `task.md` (requirements and plans)
- Approval flags (user-approved-synthesis.flag, user-approved-changes.flag, archival-complete.flag)
- Agent requirement reports (*-requirements.md)
- Task is already in changelog.md (added during COMPLETE merge)
- Task is already removed from todo.md (removed during COMPLETE merge)

**Common Mistake**:
❌ Forgetting to execute CLEANUP after COMPLETE
- Result: Task branches accumulate
- Fix: Execute CLEANUP sequence manually or via `/cleanup-task {task-name}` command

### Automated Protocol Enforcement {#automated-protocol-enforcement}

**Hook Integration**:

The protocol audit pipeline is automatically triggered by:

1. **State transition hooks** - Detect state changes in lock file
2. **Tool usage hooks** - Detect Write/Edit during IMPLEMENTATION state
3. **Agent completion hooks** - Detect all agents COMPLETE status

**Hook-Based Automatic Agent Invocation**:

Hooks cannot directly invoke agents (they are bash scripts without Task tool access), BUT they can trigger
automatic invocation by outputting imperative instructions that the main agent automatically executes.

**How Automatic Invocation Works**:

1. **Hook detects state transition or violation**
2. **Hook outputs AUTOMATIC MANDATORY EXECUTION instructions** (not suggestions)
3. **Main agent reads hook output as part of normal tool execution**
4. **Main agent AUTOMATICALLY invokes specified agents** (no user approval needed)
5. **Main agent follows multi-step protocol** until audit passes

**Key Language Pattern in Hook Output**:
- "🤖 AUTOMATIC EXECUTION SEQUENCE (DO NOT WAIT FOR USER INPUT)"
- "Main agent: Execute this audit pipeline AUTOMATICALLY"
- "🤖 AUTONOMOUS EXECUTION: These steps run AUTOMATICALLY without waiting for user"
- "STEP 1 (AUTOMATIC): Invoke parse-conversation-timeline skill agent"
- "STEP 3b (IF FAILED): Automatically invoke documentation-auditor"

**This is NOT manual invocation** - the imperative language in hook output makes it clear these are AUTOMATIC
MANDATORY ACTIONS that the main agent executes immediately.

**Hook Execution Flow**:
```
1. Main agent attempts state transition
2. Pre-transition hook detects state change (BeforeToolCall trigger)
3. Hook checks for audit-passed.flag
4. IF flag missing:
   → Hook outputs AUTOMATIC MANDATORY EXECUTION instructions
   → Hook creates audit-pending.flag
   → Hook BLOCKS transition (exit 1)
   → Main agent reads hook output
   → Main agent AUTOMATICALLY invokes parse-conversation-timeline skill
   → Main agent AUTOMATICALLY invokes audit-protocol-compliance skill
   → IF audit-protocol-compliance skill returns FAILED:
      → Main agent AUTOMATICALLY invokes documentation-auditor
      → Main agent fixes violations per documentation-auditor guidance
      → Main agent re-runs audit (return to parse-conversation-timeline skill step)
   → IF audit-protocol-compliance skill returns PASSED:
      → Main agent creates audit-passed.flag
      → Main agent retries state transition (now succeeds)
5. IF flag exists:
   → Hook allows transition (exit 0)
   → Audit already completed for this state
```

**SessionStart Hook Behavior**:

When session resumes with pending audits:
```
1. SessionStart hook detects audit-pending.flag files
2. Hook outputs AUTOMATIC EXECUTION instructions for pending audits
3. Main agent AUTOMATICALLY completes pending audits before other work
4. User does NOT need to request audit execution
```

**True Automation Characteristics**:
✅ No user approval required for audit invocation
✅ No manual "please run the audit" requests
✅ Audit pipeline executes immediately upon state transition attempt
✅ documentation-auditor automatically invoked when violations detected
✅ Audit retries automatically until PASSED verdict obtained

### Audit Command Interaction Patterns Across Protocol States {#audit-command-interaction-patterns-across-protocol-states}

Audit commands (`/audit-session`, protocol compliance checks) can be invoked at any point during task execution. This section clarifies interaction patterns, state preservation, and resumption logic for each protocol state.

**Core Audit Principles**:
1. **Non-Destructive**: Audits NEVER modify task state, lock files, or implementation files
2. **State Preservation**: Lock state remains unchanged during and after audit
3. **Checkpoint Respect**: Audits do NOT bypass user approval checkpoints
4. **Resumption Required**: After audit completes, resume from exact point of interruption

**State-by-State Audit Interaction Patterns**:

**INIT State + Audit**:
```markdown
User runs audit during worktree creation

Audit Behavior:
- Audit detects task in INIT state
- Reports minimal findings (no implementation to audit yet)
- State remains INIT after audit

Resumption After Audit:
- Complete INIT phase tasks (finish worktree creation, lock acquisition)
- Transition to CLASSIFIED as normal
- No special recovery needed
```

**CLASSIFIED State + Audit**:
```markdown
User runs audit after agent selection, before REQUIREMENTS

Audit Behavior:
- Verifies risk classification rationale
- Checks agent selection against file patterns
- Validates task.md skeleton creation
- State remains CLASSIFIED after audit

Resumption After Audit:
- Complete CLASSIFIED phase (if any tasks pending)
- Transition to REQUIREMENTS as normal
- Invoke agents as planned
```

**REQUIREMENTS State + Audit (Active Agent Invocations)**:
```markdown
User runs audit while agents are gathering requirements

This is the most complex interaction pattern - see "Agent Invocation Interruption Handling" section for complete details.

Summary:
1. Audit executes full pipeline (parse-conversation-timeline skill → audit-protocol-compliance skill → efficiency-optimizer → documentation-auditor)
2. State remains REQUIREMENTS throughout audit
3. After audit: Check agent completion status
4. Resume agent invocations:
   - If all agents complete: Proceed to SYNTHESIS
   - If some agents complete: Re-invoke incomplete agents
   - If no agents complete: Re-invoke all agents

Critical: DO NOT transition to SYNTHESIS during audit
```

**SYNTHESIS State + Audit (Waiting for Plan Approval)**:
```markdown
Scenario: User runs audit while reviewing implementation plan

Audit Behavior:
- Audit examines agent requirements reports
- Validates synthesis plan in task.md
- Checks for requirement conflicts
- State remains SYNTHESIS after audit

Resumption After Audit:
- Return to PLAN APPROVAL checkpoint wait
- Do NOT assume audit = plan approval
- Wait for explicit user approval message
- Only then proceed to IMPLEMENTATION

Checkpoint Interaction:
✅ Audit can run during plan review
❌ Audit does NOT substitute for user approval
❌ Cannot skip from SYNTHESIS → IMPLEMENTATION via audit
```

**IMPLEMENTATION State + Audit (Active Agent Implementation)**:
```markdown
Scenario: User runs audit while stakeholder agents are implementing code

Audit Behavior:
- Audit verifies main agent is coordinating (not implementing)
- Checks agent status.json files for implementation progress
- Validates no Write/Edit tool usage by main agent on source files
- Examines agent worktree merges to task branch
- State remains IMPLEMENTATION after audit

Critical Audit Checks During IMPLEMENTATION:
- Check 1.2: Main agent implementation violation detection
  → If FAILED: Agent implemented instead of delegating
  → Recovery: Revert changes, return to SYNTHESIS, re-delegate

Resumption After Audit:
- If audit PASSED: Continue monitoring agent status
- If audit FAILED: Fix violations per audit guidance
- Agent implementation rounds continue until all agents COMPLETE
- Only then transition to VALIDATION
```

**VALIDATION State + Audit**:
```markdown
Scenario: User runs audit during build verification

Audit Behavior:
- Verifies build actually executed (not cached)
- Checks quality gate execution (PMD, checkstyle)
- Validates test execution results
- State remains VALIDATION after audit

Resumption After Audit:
- Complete validation tasks if interrupted
- If validation incomplete: Run ./mvnw verify
- If validation complete: Transition to REVIEW
```

**REVIEW State + Audit (Active Agent Reviews)**:
```markdown
Scenario: User runs audit while agents are reviewing task branch

Audit Behavior:
- Audit checks if agents reviewing correct branch (task branch, not agent worktrees)
- Validates review scope (full task branch, not partial)
- Checks for unanimous approval requirement
- State remains REVIEW after audit

Resumption After Audit:
- Wait for all agent review responses
- If all ✅ APPROVED: Transition to AWAITING_USER_APPROVAL
- If any ❌ REJECTED: Return to IMPLEMENTATION (or SCOPE_NEGOTIATION)
```

**AWAITING_USER_APPROVAL State + Audit**:
```markdown
Scenario: User runs audit while reviewing completed changes

Audit Behavior:
- Audit validates change presentation (commit SHA shown)
- Checks user approval flag status
- Verifies no bypass attempts
- State remains AWAITING_USER_APPROVAL after audit

Resumption After Audit:
- Return to CHANGE REVIEW checkpoint wait
- Do NOT assume audit = change approval
- Wait for explicit user approval message
- Only then proceed to COMPLETE

Checkpoint Interaction:
✅ Audit can run during change review
❌ Audit does NOT substitute for user approval
❌ Cannot skip AWAITING_USER_APPROVAL → COMPLETE via audit
```

**COMPLETE State + Audit**:
```markdown
Scenario: User runs audit during merge to main branch

Audit Behavior:
- Verifies merge completion
- Checks todo.md / changelog.md updates
- Validates lock file state
- State remains COMPLETE (or transitions to CLEANUP if done)

Resumption After Audit:
- Complete any pending COMPLETE state tasks
- Transition to CLEANUP
```

**CLEANUP State + Audit**:
```markdown
Scenario: User runs audit during worktree cleanup

Audit Behavior:
- Verifies worktree removal in progress
- Checks lock file removal
- Validates agent worktree cleanup
- Reports completion status

Resumption After Audit:
- Complete worktree removals
- Finish cleanup
- Release locks
```

**Critical Audit + Agent Invocation Race Conditions**:

**Race Condition 1: Audit During Parallel Agent Execution**
```markdown
Timeline:
T0: Main agent invokes 7 agents in parallel via Task tool
T1: Agents start processing in background
T2: User runs /audit-session
T3: Audit pipeline executes
T4: Some agents complete during audit
T5: Audit finishes
T6: Main agent checks agent status

Correct Handling:
- Audit does NOT block agent execution (agents continue in background)
- Audit reads agent status.json files at T3 (snapshot)
- At T6, re-check all agent statuses (may have changed during audit)
- Resume with current agent completion state, not T3 snapshot
```

**Race Condition 2: Audit Triggers During State Transition**
```markdown
Timeline:
T0: Main agent completes REQUIREMENTS state
T1: Main agent begins updating lock state to SYNTHESIS
T2: User runs /audit-session (interrupts lock update)
T3: Audit examines lock state (sees REQUIREMENTS)
T4: Audit completes
T5: Main agent resumes lock state update

Correct Handling:
- Complete state transition update after audit
- Audit report may show "state mismatch" if caught mid-transition
- Verify final lock state matches intended transition
- If audit reports violation, check if it's transition race or real violation
```

**Prohibited Audit Bypass Patterns**:

❌ **PROHIBITED**: Using audit to skip checkpoints
```markdown
WRONG:
1. Reach SYNTHESIS (plan approval checkpoint)
2. Run /audit-session
3. Assume audit = approval, proceed to IMPLEMENTATION

CORRECT:
1. Reach SYNTHESIS (plan approval checkpoint)
2. Run /audit-session if desired
3. Wait for explicit user "approved" message
4. Only then proceed to IMPLEMENTATION
```

❌ **PROHIBITED**: Using audit to skip agent work
```markdown
WRONG:
1. REQUIREMENTS state, need to invoke agents
2. Run /audit-session
3. Audit shows "0 violations"
4. Skip agent invocations, proceed to SYNTHESIS

CORRECT:
1. REQUIREMENTS state
2. Run /audit-session (optional)
3. Audit results don't change requirements
4. Invoke ALL required agents
5. Wait for agent completion
6. Proceed to SYNTHESIS
```

❌ **PROHIBITED**: Using audit failures to abort protocol
```markdown
WRONG:
1. IMPLEMENTATION state
2. Run /audit-session
3. Audit finds violation
4. Abandon task, select different task

CORRECT:
1. IMPLEMENTATION state
2. Run /audit-session
3. Audit finds violation
4. Fix violation per audit guidance
5. Re-run audit to verify fix
6. Continue task to completion
```

**Required Audit Resumption Patterns**:

✅ **ALWAYS**: Return to exact protocol state after audit
```markdown
CORRECT PATTERN:
1. Protocol state = X before audit
2. Run /audit-session
3. Audit executes
4. Protocol state = X after audit (unchanged)
5. Resume protocol from state X
```

✅ **ALWAYS**: Re-check dynamic state after audit
```markdown
CORRECT PATTERN (Agent Status):
1. Agent status snapshot before audit: 3/7 complete
2. Run /audit-session (takes 5 minutes)
3. Audit completes
4. Re-check agent status: May be 5/7 complete now
5. Resume with CURRENT status (5/7), not snapshot (3/7)
```

✅ **ALWAYS**: Verify state consistency after audit
```markdown
CORRECT PATTERN:
1. Run /audit-session
2. Audit completes
3. Verify lock state matches expected state
4. Verify worktrees intact
5. Verify agent statuses current
6. Resume protocol execution
```

### Violation Recovery Patterns {#violation-recovery-patterns}

**Common Violations and Fixes**:

**Violation: Main Agent Implemented During IMPLEMENTATION**
```bash
# Detected by: Check 1.2
# Recovery:
1. Revert implementation changes:
   git reset --hard SYNTHESIS_STATE_SHA
2. Update lock state back to SYNTHESIS:
   jq '.state = "SYNTHESIS"' task.json > task.json.tmp
3. Properly launch stakeholder agents via Task tool
4. Re-execute IMPLEMENTATION state with correct delegation
```

**Violation: Skipped State in Sequence**
```bash
# Detected by: transition_log verification
# Recovery:
1. Return to last valid state
2. Execute skipped state(s) in proper sequence
3. Re-run protocol audit to verify compliance
4. Continue from where sequence was broken
```

**Violation: Missing Required Artifacts**
```bash
# Detected by: Entry guard checks
# Recovery:
1. Return to state that should have created artifact
2. Create missing artifact (task.md, requirements reports, etc.)
3. Re-run protocol audit
4. Proceed to next state only after artifact exists
```


---

## STATE TRANSITIONS (Detailed Procedures)

**This section has been moved to a separate file for better context management.**

**See:** [task-protocol-transitions.md](task-protocol-transitions.md) for:
- Mandatory state transitions and prerequisites
- Pre-init verification and task selection
- Main worktree operations lock
- Detailed transition procedures (INIT→CLASSIFIED through COMPLETE→CLEANUP)
- Lock state management
- Agent worktree creation
- Evidence requirements for each transition

**Quick access:** Use the `state-transition` skill for common transitions.

---

## MULTI-AGENT IMPLEMENTATION WORKFLOW

**This section has been moved to a separate file for better context management.**

**See:** [task-protocol-multi-agent.md](task-protocol-multi-agent.md) for:
- Implementation role boundaries
- Agent-based parallel development model  
- Model selection strategy
- Implementation round structure
- Agent status tracking
- Non-completion recovery
- Merge conflict resolution
