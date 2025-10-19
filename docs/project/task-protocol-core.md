# Task State Machine Protocol

> **Version:** 2.0 | **Last Updated:** 2025-10-16
> **Related Documents:** [CLAUDE.md](../../CLAUDE.md) •
[task-protocol-operations.md](task-protocol-operations.md)

**CRITICAL**: This protocol applies to ALL tasks that create, modify, or delete files, using MANDATORY STATE
TRANSITIONS with zero-tolerance enforcement

**TARGET AUDIENCE**: Claude AI instances executing tasks
**ARCHITECTURE**: State machine with atomic transitions and verifiable conditions
**ENFORCEMENT**: No manual overrides - all transitions require documented evidence

---

## 📖 DOCUMENTATION STRUCTURE GUIDE

**IMPORTANT**: This document contains BOTH ideal workflow documentation AND recovery procedures. Understanding when to reference each is critical.

### Ideal State vs Recovery Documentation

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

### When to Read Each Layer

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

### Navigation Patterns

**Pattern 1: First-Time Reading** (learning the protocol):
```
Step 1: Read "STATE MACHINE ARCHITECTURE" (ideal state)
Step 2: Read "State Definitions" (ideal state)
Step 3: Read "State Transitions" sections (ideal state)
Step 4: SKIP recovery sections on first read
Step 5: Return to recovery sections when needed
```

**Pattern 2: Executing a Task** (applying the protocol):
```
Step 1: Follow ideal state transitions in order
Step 2: If interruption occurs → Jump to "Interruption Handling" (recovery)
Step 3: If agent fails → Jump to "Partial Agent Completion" (recovery)
Step 4: If build fails → Jump to "Violation Recovery" (recovery)
Step 5: After recovery → Return to ideal state workflow
```

**Pattern 3: Debugging an Issue** (troubleshooting):
```
Step 1: Identify current state from lock file
Step 2: Identify issue type (interruption, failure, violation)
Step 3: Jump directly to relevant RECOVERY section
Step 4: Apply recovery procedure
Step 5: Verify return to ideal state
```

### Section Type Indicators

Throughout this document, section headers indicate their type:

**Ideal State Sections** (standard workflow):
- Headers describe state transitions: "INIT → CLASSIFIED"
- Content focuses on: requirements, procedures, success criteria
- Reading order: Sequential from INIT through CLEANUP

**Recovery Sections** (exception handling):
- Headers mention recovery/handling: "Agent Invocation Interruption Handling"
- Content focuses on: problems, detection, recovery actions
- Reading order: As needed when specific issues occur

### Why This Structure?

**Benefits of Combined Documentation**:
1. **Complete Picture**: Both ideal and recovery in one authoritative source
2. **Context Awareness**: Recovery procedures reference ideal state sections
3. **Efficiency**: No need to search multiple documents during emergencies
4. **Consistency**: Single source of truth for all protocol behavior

**Avoiding Confusion**:
1. **Clear Indicators**: Section titles clearly signal ideal vs recovery
2. **Separate Navigation**: Different reading patterns for each layer
3. **Explicit References**: Recovery sections explicitly link to ideal sections
4. **Visual Separation**: Recovery sections use distinct formatting

### Quick Reference: Section Classification

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

## STATE MACHINE ARCHITECTURE

### Core States
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

### State Definitions
- **INIT**: Task selected, locks acquired, session validated, task worktree and agent worktrees created
- **CLASSIFIED**: Risk level determined, agents selected, isolation established
-  **REQUIREMENTS**: All stakeholder agents contribute requirements to task.md, negotiate conflicts, finalize
  task.md
-  **SYNTHESIS**: Each agent appends implementation plan to task.md, **USER APPROVAL CHECKPOINT: Present all
  plans to user, wait for explicit user approval**
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
  architecture-reviewer makes final decision)
-  **COMPLETE**: Work merged to main branch, todo.md updated, dependent tasks unblocked (only after user
  approves changes)
- **CLEANUP**: All agent worktrees removed, task worktree removed, locks released, temporary files cleaned

### User Approval Checkpoints - MANDATORY REGARDLESS OF BYPASS MODE

**CRITICAL**: The two user approval checkpoints are MANDATORY and MUST be respected REGARDLESS of whether the
user is in "bypass permissions on" mode or any other automation mode.

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

**Why Checkpoints Do NOT Violate Autonomous Completion**:
- Checkpoints are at DEFINED state transitions (not random mid-protocol stops)
- Checkpoints serve OVERSIGHT function (not "help me decide what to do" function)
- Agent arrives at checkpoint WITH COMPLETED WORK (plan drafted, implementation finished)
- Agent does NOT need help to continue - only needs approval to proceed

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

### Checkpoint Wait Behavior

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

**Rationale**: User may need extended time to:
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

### Automated Checkpoint Enforcement

**Approval Marker File**: `/workspace/tasks/{task-name}/user-approval-obtained.flag`
- Required for COMPLETE state transition
- Automatically managed by enforcement system

**Required Approval Patterns**:
- User message contains approval keywords: "yes", "approved", "approve", "proceed", "looks good", "LGTM"
- AND message references review context: "review", "changes", "commit", "finalize"

**Why User Instructions Don't Override**: Protocol line 36: "MANDATORY REGARDLESS of bypass mode or automation
mode". Checkpoints are quality gates, not permission gates.

### State Transitions
Each transition requires **ALL** specified conditions to be met. **NO EXCEPTIONS.**

## TERMINOLOGY GLOSSARY

**State**: One of 10 formal states in the task protocol state machine (INIT, CLASSIFIED, REQUIREMENTS,
SYNTHESIS, IMPLEMENTATION, VALIDATION, REVIEW, AWAITING_USER_APPROVAL, COMPLETE, CLEANUP)

**State Transition**: Movement from one state to another after meeting all transition conditions

**Implementation Round**: Iterative cycle within IMPLEMENTATION state where agents develop, validate, and
refine code until all agents report no more work

**Checkpoint**: MANDATORY pause in protocol requiring explicit user approval before continuing (PLAN APPROVAL
after SYNTHESIS and CHANGE REVIEW after REVIEW)

**Risk Level**: Classification of file modification impact (HIGH/MEDIUM/LOW) determining workflow variant

**Agent**: Stakeholder specialist (architecture-reviewer, style-reviewer, etc.) providing domain-specific
validation

**Worktree**: Git worktree providing isolated workspace for task development without affecting main branch

**Lock File**: JSON file at `/workspace/tasks/{task-name}/task.json` ensuring single-session task ownership

**Scope Negotiation**: State for determining what work can be deferred when resolution effort exceeds 2x task
scope

**Unanimous Approval**: Required condition where ALL agents must respond with "✅ APPROVED" before proceeding

## RISK-BASED AGENT SELECTION ENGINE

### Automatic Risk Classification
**Input**: File paths from modification request
**Process**: Pattern matching → Escalation trigger analysis → Agent set determination
**Output**: Risk level (HIGH/MEDIUM/LOW) + Required agent set

### HIGH-RISK FILES (Complete Validation Required)
**Patterns:**
- `src/**/*.java` (core implementation)
- `pom.xml`, `**/pom.xml` (build configuration)
- `.github/**` (CI/CD workflows)
- `**/security/**` (security components)
- `checkstyle.xml`, `**/checkstyle*.xml` (style enforcement)
- `CLAUDE.md` (critical configuration)
- `docs/project/task-protocol.md` (protocol configuration)
- `docs/project/critical-rules.md` (safety rules)

**Required Agents**: architecture-reviewer, style-reviewer, quality-reviewer, build-reviewer
**Additional Agents**: security-reviewer (if security-related), performance-reviewer (if performance-critical),
test-reviewer (if new functionality), usability-reviewer (if user-facing)

### MEDIUM-RISK FILES (Domain Validation Required)
**Patterns:**
- `src/test/**/*.java` (test files)
- `docs/code-style/**` (style documentation)
- `**/resources/**/*.properties` (configuration)
- `**/*Test.java`, `**/*Tests.java` (test classes)

**Required Agents**: architecture-reviewer, quality-reviewer
**Additional Agents**: style-reviewer (if style files), security-reviewer (if config files),
performance-reviewer (if benchmarks)

### LOW-RISK FILES (Minimal Validation Required)
**Patterns:**
- `*.md` (except CLAUDE.md, task-protocol.md, critical-rules.md)
- `docs/**/*.md` (general documentation)
- `todo.md` (task tracking)
- `*.txt`, `*.log` (text files)
- `**/README*` (readme files)

**Required Agents**: None (unless escalation triggered)

### Escalation Triggers
**Keywords**: "security", "architecture", "breaking", "performance", "concurrent", "database", "api", "state",
"dependency"
**Content Analysis**: Cross-module dependencies, security implications, architectural changes
**Action**: Force escalation to next higher risk level

### Manual Overrides
**Force Full Protocol**: `--force-full-protocol` flag for critical changes
**Explicit Risk Level**: `--risk-level=HIGH|MEDIUM|LOW` to override classification
**Escalation Keywords**: "security", "architecture", "breaking" in task description

### Risk Assessment Audit Trail
**Required Logging:**
- Risk level selected (HIGH/MEDIUM/LOW)
- Classification method (pattern match, keyword trigger, manual override)
- Escalation triggers activated (if any)
- Workflow variant executed (FULL/ABBREVIATED/STREAMLINED)
- Agent set selected for review
- Final outcome (approved/rejected/deferred)

**Implementation**: Log in state.json file and commit messages for audit purposes

## WORKFLOW VARIANTS BY RISK LEVEL

### HIGH_RISK_WORKFLOW (Complete Validation)
**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW →
COMPLETE → CLEANUP
**Stakeholder Agents**: All agents based on task requirements
**Isolation**: Mandatory worktree isolation
**Review**: Complete stakeholder validation
**Use Case**: Core implementation, build configuration, security, CI/CD
**Conditional Skips**: None - all validation required

### MEDIUM_RISK_WORKFLOW (Domain Validation)
**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW →
COMPLETE → CLEANUP
**Stakeholder Agents**: Based on change characteristics
- Base: architecture-reviewer (always required)
- +style-reviewer: If style/formatting files modified
- +security-reviewer: If any configuration or resource files modified
- +performance-reviewer: If test performance or benchmarks affected
- +quality-reviewer: Always included for code quality validation
**Isolation**: Worktree isolation for multi-file changes
**Review**: Domain-appropriate stakeholder validation
**Use Case**: Test files, style documentation, configuration files
**Conditional Skips**: May skip IMPLEMENTATION/VALIDATION states if only documentation changes

### LOW_RISK_WORKFLOW (Streamlined Validation)
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

### Conditional State Transition Logic
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

### Skip Condition Examples
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

## AGENT SELECTION DECISION TREE

### Comprehensive Agent Selection Framework
**Input**: Task description and file modification patterns
**Available Agents**: architecture-reviewer, usability-reviewer, performance-reviewer, security-reviewer,
style-reviewer, quality-reviewer, test-reviewer, build-reviewer

**Processing Logic:**

**🚨 CORE AGENTS (Always Required):**
- **architecture-reviewer**: MANDATORY for ALL file modification tasks (provides implementation requirements)

**🔍 FUNCTIONAL AGENTS (Code Implementation):**
- IF NEW CODE created: add style-reviewer, quality-reviewer, build-reviewer
- IF IMPLEMENTATION (not just config): add test-reviewer
- IF MAJOR FEATURES completed: add usability-reviewer (MANDATORY after completion)

**🛡️ SECURITY AGENTS (Actual Security Concerns):**
- IF AUTHENTICATION/AUTHORIZATION changes: add security-reviewer
- IF EXTERNAL API/DATA integration: add security-reviewer
- IF ENCRYPTION/CRYPTOGRAPHIC operations: add security-reviewer
- IF INPUT VALIDATION/SANITIZATION: add security-reviewer

**⚡ PERFORMANCE AGENTS (Performance Critical):**
- IF ALGORITHM optimization tasks: add performance-reviewer
- IF DATABASE/QUERY optimization: add performance-reviewer
- IF MEMORY/CPU intensive operations: add performance-reviewer

**🔧 FORMATTING AGENTS (Code Quality):**
- IF PARSER LOGIC modified: add performance-reviewer, security-reviewer
- IF AST TRANSFORMATION changed: add quality-reviewer, test-reviewer
- IF FORMATTING RULES affected: add style-reviewer

**❌ AGENTS NOT NEEDED FOR SIMPLE OPERATIONS:**
- Maven module renames: NO performance-reviewer
- Configuration file updates: NO security-reviewer unless changing auth
- Directory/file renames: NO performance-reviewer
- Documentation updates: Usually only architecture-reviewer

**📊 ANALYSIS AGENTS (Research/Study Tasks):**
- IF ARCHITECTURAL ANALYSIS: add architecture-reviewer
- IF PERFORMANCE ANALYSIS: add performance-reviewer
- IF UX/INTERFACE ANALYSIS: add usability-reviewer
- IF SECURITY ANALYSIS: add security-reviewer
- IF CODE QUALITY REVIEW: add quality-reviewer
- IF PARSER/FORMATTER PERFORMANCE ANALYSIS: add performance-reviewer

**Agent Selection Verification Checklist:**
- [ ] NEW CODE task → style-reviewer included?
- [ ] Source files created/modified → build-reviewer included?
- [ ] Performance-critical code → performance-reviewer included?
- [ ] Security-sensitive features → security-reviewer included?
- [ ] User-facing interfaces → usability-reviewer included?
- [ ] Post-implementation refactoring → quality-reviewer included?
- [ ] AST parsing/code formatting → performance-reviewer included?

**Special Agent Usage Patterns:**
-  **style-reviewer**: Apply ALL manual style guide rules from docs/code-style/ (Java, common, and
  language-specific patterns)
-  **build-reviewer**: For style/formatting tasks, triggers linters (checkstyle, PMD, ESLint) through build
  system
-  **build-reviewer**: Use alongside style-reviewer to ensure comprehensive validation (automated + manual
  rules)
- **quality-reviewer**: Post-implementation refactoring and best practices enforcement
- **test-reviewer**: Business logic validation and comprehensive test creation
- **security-reviewer**: Data handling and storage compliance review
- **performance-reviewer**: Algorithmic efficiency and resource optimization
- **usability-reviewer**: User experience design and interface evaluation
- **architecture-reviewer**: System architecture and implementation guidance

## COMPLETE STYLE VALIDATION FRAMEWORK

### Three-Component Style Validation
**MANDATORY PROCESS**: When style validation is required, ALL THREE components must pass:

1. **Automated Linters** (via build-reviewer):
   - `checkstyle`: Java coding conventions and formatting
   - `PMD`: Code quality and best practices
   - `ESLint`: JavaScript/TypeScript style (if applicable)

2. **Manual Style Rules** (via style-reviewer):
   - Apply ALL detection patterns from `docs/code-style/*-claude.md`
   - Java-specific patterns (naming, structure, comments)
   - Common patterns (cross-language consistency)
   - Language-specific patterns as applicable

3. **Build Integration** (via build-reviewer):
   - Automated fixing when conflicts detected (LineLength vs UnderutilizedLines)
   - Use `checkstyle/fixers` module for AST-based consolidate-then-split strategy
   - Comprehensive testing validates fixing logic before application

### Complete Style Validation Gate Pattern
```bash
# MANDATORY: Never assume checkstyle-only validation
# CRITICAL ERROR PATTERN: Checking only checkstyle and declaring "no violations found" when PMD/manual violations exist

validate_complete_style_compliance() {
    echo "=== COMPLETE STYLE VALIDATION GATE ==="

    # Component 1: Automated linters via build system
    echo "Validating automated linters..."
    ./mvnw checkstyle:check || return 1
    ./mvnw pmd:check || return 1

    # Component 2: Manual style rules via style-reviewer agent
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


## BATCH PROCESSING AND CONTINUOUS MODE

### Batch Processing Restrictions
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

### Automatic Continuous Mode Translation
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

## 🧠 PROTOCOL INTERPRETATION MODE

**ENHANCED ANALYTICAL RIGOR**: Parent agent must apply deeper analysis when interpreting and following the
task protocol workflow. Rather than surface-level interpretations, carefully analyze what the protocol truly
requires for the specific task context.

**Critical Thinking Requirements:**
- Question assumptions about task scope and complexity
- Verify all transition conditions are genuinely met
- Apply evidence-based validation rather than procedural compliance
- Consider edge cases and alternative approaches
- Maintain skeptical evaluation of "good enough" solutions

## AUTOMATED PROTOCOL COMPLIANCE AUDIT

**MANDATORY**: After EVERY state transition completion, the main agent MUST invoke the protocol compliance
audit pipeline to verify correct protocol execution.

### 4-Agent Protocol Audit Pipeline

**Purpose**: Detect and prevent protocol violations through systematic, automated checking after each phase.

**Pipeline Architecture**:
```
process-recorder → process-compliance-reviewer → efficiency-optimizer → documentation-auditor
    (facts)           (enforcement)        (performance)        (root causes)
```

### When to Run Protocol Audit

**MANDATORY Audit Triggers**:
- **After each state transition** (INIT→CLASSIFIED, CLASSIFIED→REQUIREMENTS, etc.)
- **Before critical transitions** (especially before IMPLEMENTATION and before COMPLETE)
- **After agent completion** (when all stakeholder agents report COMPLETE)
- **On violation detection** (immediate re-audit after fixing violations)

### Protocol Audit Execution Pattern

```bash
# Step 1: Collect session facts
process_recorder_output=$(invoke_agent process-recorder \
  --task-name "${TASK_NAME}" \
  --session-id "${SESSION_ID}")

# Step 2: Check protocol compliance
protocol_audit_result=$(invoke_agent process-compliance-reviewer \
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

### Violation Detection and Retry Logic

**Critical Violation Response Pattern**:

```markdown
IF (process-compliance-reviewer returns FAILED):
  1. **STOP immediately** - Do not proceed to next state
  2. **IDENTIFY violation** - Read process-compliance-reviewer output for specific check IDs
  3. **APPLY fix** - Based on violation type:
     - Check 0.2 (main agent implementation): Revert changes, re-delegate to agents
     - State sequence violation: Return to skipped state
     - Missing artifacts: Create required files/reports
     - Lock state mismatch: Update lock file to match actual state
  4. **RE-RUN protocol audit** - Verify fix resolved violation
  5. **REPEAT until PASSED** - Cannot proceed until process-compliance-reviewer returns PASSED
  6. **ONLY THEN proceed** - After PASSED verdict, continue to next state
```

### Example: IMPLEMENTATION State Transition with Audit

```markdown
## After SYNTHESIS state complete and user approval received:

1. Update lock state to IMPLEMENTATION:
   ```bash
   jq '.state = "IMPLEMENTATION"' task.json > task.json.tmp
   mv task.json.tmp task.json
   ```

2. **MANDATORY PROTOCOL AUDIT** before launching agents:
   ```bash
   # Audit current state
   process-compliance-reviewer --check-state-transition "SYNTHESIS→IMPLEMENTATION"
   ```

3. **IF AUDIT PASSES**:
   - Launch stakeholder agents in parallel
   - Monitor agent status.json files
   - Continue IMPLEMENTATION rounds

4. **IF AUDIT FAILS**:
   - STOP - do not launch agents
   - Fix violations identified by process-compliance-reviewer
   - Re-run audit
   - Only proceed after PASSED verdict

5. **After all agents report COMPLETE**:
   - **MANDATORY PROTOCOL AUDIT** before transitioning to VALIDATION:
     ```bash
     process-compliance-reviewer --check-implementation-complete
     ```
   - Verify main agent did not create source files in task worktree
   - Verify all agents merged to task branch
   - Verify lock state matches actual work completed
```

### Protocol Audit Integration Points

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
| **AWAITING_USER_APPROVAL → COMPLETE** | User approved | User approval flag exists, ready to merge |
| **COMPLETE → CLEANUP** | Merge complete | Changes merged to main, todo.md updated |

### Automated Protocol Enforcement

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
- "STEP 1 (AUTOMATIC): Invoke process-recorder agent"
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
   → Main agent AUTOMATICALLY invokes process-recorder
   → Main agent AUTOMATICALLY invokes process-compliance-reviewer
   → IF process-compliance-reviewer returns FAILED:
      → Main agent AUTOMATICALLY invokes documentation-auditor
      → Main agent fixes violations per documentation-auditor guidance
      → Main agent re-runs audit (return to process-recorder step)
   → IF process-compliance-reviewer returns PASSED:
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

### Audit Command Interaction Patterns Across Protocol States

**CRITICAL GUIDANCE**: Audit commands (`/audit-session`, protocol compliance checks) can be invoked at any point during task execution. This section clarifies interaction patterns, state preservation, and resumption logic for each protocol state.

**Core Audit Principles**:
1. **Non-Destructive**: Audits NEVER modify task state, lock files, or implementation files
2. **State Preservation**: Lock state remains unchanged during and after audit
3. **Checkpoint Respect**: Audits do NOT bypass user approval checkpoints
4. **Resumption Required**: After audit completes, resume from exact point of interruption

**State-by-State Audit Interaction Patterns**:

**INIT State + Audit**:
```markdown
Scenario: User runs audit during worktree creation

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
Scenario: User runs audit after agent selection, before REQUIREMENTS

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
Scenario: User runs audit while agents are gathering requirements

This is the most complex interaction pattern - see "Agent Invocation Interruption Handling" section for complete details.

Summary:
1. Audit executes full pipeline (process-recorder → process-compliance-reviewer → efficiency-optimizer → documentation-auditor)
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
- Check 0.2: Main agent implementation violation detection
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

### Violation Recovery Patterns

**Common Violations and Fixes**:

**Violation: Main Agent Implemented During IMPLEMENTATION**
```bash
# Detected by: Check 0.2
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

## MANDATORY STATE TRANSITIONS

### state.json File Management
Every task MUST maintain a `state.json` file in the task directory containing:
```json
{
  "current_state": "STATE_NAME",
  "session_id": "session_uuid",
  "task_name": "task-name",
  "risk_level": "HIGH|MEDIUM|LOW",
  "required_agents": ["agent1", "agent2"],
  "evidence": {
    "REQUIREMENTS": ["file1.md", "file2.md"],
    "IMPLEMENTATION": ["sha256hash"],
    "VALIDATION": ["build_success_timestamp"],
    "REVIEW": {"agent1": "APPROVED", "agent2": "APPROVED"}
  },
  "transition_log": [
    {"from": "INIT", "to": "CLASSIFIED", "timestamp": "ISO8601", "evidence": "classification_reasoning"}
  ]
}
```

## PRE-INIT VERIFICATION: Task Selection and Lock Validation

**CRITICAL**: Before executing INIT phase, you MUST verify that the selected task is available for work by
checking existing locks and worktrees, AND verify you can complete the task autonomously.

### Autonomous Completion Feasibility Check

Before acquiring lock and starting INIT, verify the task can be completed without user intervention:

**MANDATORY PRE-TASK CHECKLIST**:

```bash
# 1. Task has clear deliverables
grep -A 20 "TASK.*${TASK_NAME}" /workspace/main/todo.md
# Verify: Purpose, Scope, Components are defined

# 2. No external blockers exist
# ✅ PROCEED if: All dependencies available, no missing APIs, no ambiguous requirements
# ❌ SELECT ALTERNATIVE if: Requires unavailable external API, missing credentials, undefined requirements

# 3. Implementation is within capabilities
# ✅ PROCEED if: Technical approach is clear, no user design decisions needed
# ❌ SELECT ALTERNATIVE if: Requires user to choose between architectural options
```

**PROHIBITED STOPPING REASONS** (these are NOT blockers):
❌ "This might take a long time" - TIME ESTIMATES ARE NOT BLOCKERS
❌ "This is complex" - COMPLEXITY IS NOT A BLOCKER unless genuinely impossible
❌ "Token usage is high" - TOKENS NEVER JUSTIFY STOPPING
❌ "Should I ask the user first?" - NO, protocol is AUTONOMOUS

**LEGITIMATE STOPPING REASONS** (these ARE blockers):
✅ Genuine technical blocker (missing external API, undefined requirement)
✅ Ambiguity requiring user clarification (conflicting requirements with no resolution)
✅ Task scope changed externally (user modified todo.md indicating change)

**COMMITMENT**: If all checks pass, you MUST complete entire protocol (States 0-8) without asking user
permission.

### Decision Logic: Can I Work on This Task?

**Step 1: Check for existing lock file**
```bash
# Replace {TASK_NAME} with actual task name
# Check if another instance has locked this task
cat /workspace/tasks/{TASK_NAME}/task.json 2>/dev/null
```

**Step 2: Analyze lock ownership**
```bash
# If lock file exists, compare session IDs
LOCK_SESSION=$(jq -r '.session_id' /workspace/tasks/{TASK_NAME}/task.json 2>/dev/null)
CURRENT_SESSION="{SESSION_ID}"  # From system environment

if [ "$LOCK_SESSION" = "$CURRENT_SESSION" ]; then
    echo "✅ Lock owned by current session - can resume task"
elif [ -n "$LOCK_SESSION" ]; then
    echo "❌ Lock owned by different session ($LOCK_SESSION) - SELECT ALTERNATIVE TASK"
else
    echo "✅ No lock exists - can acquire lock and start task"
fi
```

**Step 3: Check for existing worktree**
```bash
# Verify if worktree already exists
ls -d /workspace/tasks/{TASK_NAME} 2>/dev/null && echo "Worktree exists" || echo "No worktree"
```

### Task Selection Decision Matrix

| Lock Exists? | Lock Owner | Worktree Exists? | Action |
|--------------|------------|------------------|--------|
| ✅ YES | Current session | ✅ YES | **RESUME**: Skip INIT, verify current state, continue from last phase |
| ✅ YES | Current session | ❌ NO | **ERROR**: Inconsistent state - lock exists but worktree missing - ask user |
| ✅ YES | Different session | ✅ YES | **SELECT ALTERNATIVE TASK**: Another instance is working on this task |
| ✅ YES | Different session | ❌ NO | **SELECT ALTERNATIVE TASK**: Lock exists without worktree - may be initializing |
| ❌ NO | N/A | ✅ YES | **ASK USER**: Worktree exists without lock - crashed session or manual intervention |
| ❌ NO | N/A | ❌ NO | **PROCEED WITH INIT**: Task is available - execute normal INIT phase |

### Prohibited Actions

❌ **NEVER** delete or override a lock file owned by a different session
❌ **NEVER** assume an existing worktree without a lock is abandoned
❌ **NEVER** proceed with INIT if a lock exists for a different session
❌ **NEVER** skip lock verification when user says "continue with next task"

### Required Actions

✅ **ALWAYS** check `/workspace/tasks/{TASK_NAME}/task.json` before starting work
✅ **ALWAYS** compare lock session_id with current session_id
✅ **ALWAYS** check for existing worktree at `/workspace/tasks/{TASK_NAME}`
✅ **ALWAYS** select an alternative task if lock is owned by different session
✅ **ALWAYS** ask user for guidance if worktree exists without a lock


### Recovery from Crashed Sessions

**Scenario**: Worktree exists at `/workspace/tasks/{TASK_NAME}` but no lock file exists.

**Possible Causes**:
- Previous Claude instance crashed before cleanup
- Manual worktree creation outside protocol
- Lock file manually deleted

**Required Action**: Ask user for guidance
```
"I found an existing worktree for task '{TASK_NAME}' at /workspace/tasks/{TASK_NAME}
but no lock file exists at /workspace/tasks/{TASK_NAME}/task.json. This may indicate a
crashed session. Should I:
1. Clean up the abandoned worktree and start fresh
2. Resume work in the existing worktree
3. Select a different task"
```

**DO NOT** make this decision autonomously - user knows if another instance is running.

---

## MAIN WORKTREE OPERATIONS LOCK REQUIREMENT

**CRITICAL**: Any operations executed directly on the main worktree (`/workspace/main/`) require acquiring a
special lock at `/workspace/tasks/main/task.json`.

### Operations Requiring Main Worktree Lock

**MANDATORY LOCK ACQUISITION** for:
- Modifying files directly in main worktree working directory
- Running git operations on main branch (e.g., `git checkout`, `git merge`, `git pull`)
- Updating main branch configuration files
- Any direct edits to main worktree files outside of task-specific worktrees
- Emergency fixes or hotfixes applied directly to main

### Main Worktree Lock Format

```json
{
  "session_id": "unique-session-identifier",
  "task_name": "main-worktree-operation",
  "operation_type": "git-operation|file-modification|configuration-update",
  "state": "IN_PROGRESS",
  "created_at": "ISO-8601-timestamp"
}
```

### Acquiring Main Worktree Lock

**Step 1: Attempt atomic lock creation**
```bash
# Replace {SESSION_ID} with current session ID
# Replace {OPERATION_TYPE} with one of: git-operation, file-modification, configuration-update

export SESSION_ID="{SESSION_ID}" && mkdir -p /workspace/locks && (set -C; echo "{\"session_id\": \"${SESSION_ID}\", \"task_name\": \"main-worktree-operation\", \"operation_type\": \"{OPERATION_TYPE}\", \"state\": \"IN_PROGRESS\", \"created_at\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}" > /workspace/tasks/main/task.json) && echo "MAIN_LOCK_SUCCESS" || echo "MAIN_LOCK_FAILED"
```

**Step 2: Check lock ownership if MAIN_LOCK_FAILED**
```bash
# If lock acquisition failed, check who owns it
LOCK_OWNER=$(jq -r '.session_id' /workspace/tasks/main/task.json 2>/dev/null)
CURRENT_SESSION="{SESSION_ID}"

if [ "$LOCK_OWNER" = "$CURRENT_SESSION" ]; then
    echo "✅ Main lock already owned by current session"
elif [ -n "$LOCK_OWNER" ]; then
    echo "❌ Main worktree locked by different session ($LOCK_OWNER)"
    echo "ABORT: Cannot perform main worktree operations - wait or select different task"
    exit 1
else
    echo "✅ No main lock exists - retry lock acquisition"
fi
```

### Releasing Main Worktree Lock

**After completing main worktree operations:**
```bash
# CRITICAL: Verify lock ownership before deletion
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/tasks/main/task.json")

if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "❌ FATAL: Cannot delete main lock owned by $LOCK_OWNER"
  exit 1
fi

# Delete main lock file (only if ownership verified)
rm -f /workspace/tasks/main/task.json
echo "✅ Main worktree lock released"
```

### Main Worktree Lock vs Task-Specific Locks

**Key Differences:**
-  **Task locks** (`/workspace/tasks/{task-name}/task.json`): Used for task-specific worktrees during normal
  protocol execution
- **Main lock** (`/workspace/tasks/main/task.json`): Used ONLY for direct operations on main worktree

**When to use each:**
-  **Normal task work**: Use task-specific lock (`{task-name}.json`) and work in task worktree
  (`/workspace/tasks/{task-name}/code/`)
-  **Direct main operations**: Use main lock (`main.json`) when working directly in main worktree
  (`/workspace/main/`)

**Prohibited Patterns:**
❌ Modifying main worktree files without acquiring main lock
❌ Running git operations on main branch without main lock
❌ Assuming main worktree is always available

**Required Patterns:**
✅ Acquire main lock before ANY main worktree operations
✅ Release main lock immediately after operations complete
✅ Verify lock ownership before release
✅ Wait or select alternative task if main lock is owned by different session

### INIT → CLASSIFIED
**Mandatory Conditions:**
- [ ] Session ID validated and unique
- [ ] Atomic lock acquired for task (LOCK_SUCCESS received) at `/workspace/tasks/{task-name}/task.json`
- [ ] Task exists in todo.md
- [ ] Task worktree created at `/workspace/tasks/{task-name}/code`
-  [ ] Agent worktrees created at `/workspace/tasks/{task-name}/agents/{agent-name}/code` for all selected
  agents
- [ ] **CRITICAL: Changed directory to task worktree**
- [ ] **CRITICAL: Verified pwd shows task directory**

**Evidence Required:**
- Lock file creation timestamp at `/workspace/tasks/{task-name}/task.json`
- Session ID validation output
- Task worktree creation confirmation
- All agent worktrees creation confirmation
- **pwd verification showing `/workspace/tasks/{task-name}/code`**

**MANDATORY TOOL USAGE PATTERN**:
- [ ] Task requirements loaded in SINGLE parallel read:
      `Read todo.md + Glob locks/*.json`
- [ ] Configuration files loaded in SINGLE parallel read (if applicable):
      `Read pom.xml + Read checkstyle.xml + Read pmd.xml`
- [ ] Architecture documentation loaded with implementation files (if applicable):
      `Read docs/project/architecture.md + Glob src/main/java/**/*Pattern*.java`

**NOTE**: Protocol guidance is provided just-in-time via hooks. You do NOT need to read
task-protocol-core.md or task-protocol-operations.md upfront. The phase-transition-guide hook
will direct you to specific sections as needed for each state.

**VERIFICATION**:
```bash
# Each message should have ≥2 tool calls during INIT
# Average tools per message should be ≥2.0
```

**Implementation:** Phase-specific guidance provided by hooks (state-transition-detector.sh →
phase-transition-guide.sh)

**🚨 CRITICAL**: INIT is NOT complete until you have:
- Created task directory and acquired atomic lock
- Created task and agent worktrees
- Changed to task worktree directory
- Verified pwd shows correct task directory

### Lock State Update Helper

**Purpose**: Update lock file state as task progresses through protocol phases

**Function**:
```bash
update_lock_state() {
  local TASK_NAME=$1
  local NEW_STATE=$2
  local LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"

  # Verify lock exists and ownership
  if [ ! -f "$LOCK_FILE" ]; then
    echo "❌ ERROR: Lock file not found: $LOCK_FILE"
    return 1
  fi

  local LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "$LOCK_FILE")
  local SESSION_ID="${CURRENT_SESSION_ID}"

  if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
    echo "❌ FATAL: Cannot update lock owned by $LOCK_OWNER"
    return 1
  fi

  # Update state field
  sed -i "s/\"state\":\s*\"[^\"]*\"/\"state\": \"$NEW_STATE\"/" "$LOCK_FILE"
  echo "✅ Lock state updated: $NEW_STATE"
}
```

**Valid State Values**:
- `INIT` - Initial lock creation
- `CLASSIFIED` - Risk classification complete
- `REQUIREMENTS` - Gathering stakeholder requirements
- `SYNTHESIS` - Consolidating requirements into architecture
- `IMPLEMENTATION` - Writing code
- `VALIDATION` - Build verification
- `REVIEW` - Stakeholder reviews
- `AWAITING_USER_APPROVAL` - Checkpoint state waiting for user approval
- `SCOPE_NEGOTIATION` - Resolving scope issues
- `COMPLETE` - Merging to main
- `CLEANUP` - Final cleanup (lock will be deleted)

**IMPORTANT**: Update lock state at the START of each phase transition to maintain accurate recovery state.

### Enhanced Lock File Format with Checkpoint Tracking

**Standard Lock File** (basic task execution):
```json
{
  "session_id": "unique-session-identifier",
  "task_name": "task-name-matching-filename",
  "state": "current-protocol-phase",
  "created_at": "ISO-8601-timestamp",
  "transition_log": [
    {"from": "INIT", "to": "CLASSIFIED", "timestamp": "2025-10-16T14:33:00Z"},
    {"from": "CLASSIFIED", "to": "REQUIREMENTS", "timestamp": "2025-10-16T14:35:00Z"}
  ]
}
```

**transition_log Field**:
- **Purpose**: Track complete state transition history for violation detection
- **Format**: Array of transition objects with from/to states and timestamps
- **Usage**: Entry guards verify complete state sequence before allowing IMPLEMENTATION
- **Required**: MUST be updated with EVERY state transition
- **Validation**: Verify no states were skipped (INIT→CLASSIFIED→REQUIREMENTS→SYNTHESIS→IMPLEMENTATION)

**Updating transition_log**:
```bash
# Add new transition to log when updating state
TASK_NAME="your-task-name"
LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"
OLD_STATE=$(jq -r '.state' "$LOCK_FILE")
NEW_STATE="REQUIREMENTS"

jq --arg old "$OLD_STATE" \
   --arg new "$NEW_STATE" \
   --arg timestamp "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
   '.state = $new |
    .transition_log += [{"from": $old, "to": $new, "timestamp": $timestamp}]' \
   "$LOCK_FILE" > "${LOCK_FILE}.tmp" && mv "${LOCK_FILE}.tmp" "$LOCK_FILE"
```

**Lock File with Active Checkpoint** (when presenting for user approval):
```json
{
  "session_id": "unique-session-identifier",
  "task_name": "task-name-matching-filename",
  "state": "AWAITING_USER_APPROVAL",
  "created_at": "ISO-8601-timestamp",
  "checkpoint": {
    "type": "USER_APPROVAL_POST_REVIEW",
    "commit_sha": "abc123def456",
    "presented_at": "ISO-8601-timestamp",
    "approved": false
  }
}
```

**Checkpoint Field Structure**:
- `type`: Always "USER_APPROVAL_POST_REVIEW" for Checkpoint 2
- `commit_sha`: The commit hash presented to user for review
- `presented_at`: ISO-8601 timestamp when changes were presented
- `approved`: Boolean flag (false = pending, true = approved)

**Creating Checkpoint State**:
```bash
# After REVIEW state with unanimous approval, create commit and update lock
COMMIT_SHA=$(git rev-parse HEAD)
TASK_NAME="your-task-name"
LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"

# Update lock to AWAITING_USER_APPROVAL state with checkpoint data
jq --arg sha "$COMMIT_SHA" \
   --arg timestamp "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
   '.state = "AWAITING_USER_APPROVAL" |
    .checkpoint = {
      "type": "USER_APPROVAL_POST_REVIEW",
      "commit_sha": $sha,
      "presented_at": $timestamp,
      "approved": false
    }' "$LOCK_FILE" > "${LOCK_FILE}.tmp" && mv "${LOCK_FILE}.tmp" "$LOCK_FILE"
```

**Marking Checkpoint as Approved**:
```bash
# After user provides explicit approval
LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"
jq '.checkpoint.approved = true' "$LOCK_FILE" > "${LOCK_FILE}.tmp" && mv "${LOCK_FILE}.tmp" "$LOCK_FILE"
```

**Checking Checkpoint Status Before Phase 7**:
```bash
# Mandatory verification before COMPLETE state transition
CHECKPOINT_APPROVED=$(jq -r '.checkpoint.approved // false' "$LOCK_FILE")

if [ "$CHECKPOINT_APPROVED" != "true" ]; then
    echo "❌ CHECKPOINT VIOLATION: User approval not obtained"
    echo "Cannot proceed to COMPLETE state"
    exit 1
fi
```

### Dynamic Agent Addition Mid-Task

**CRITICAL GUIDANCE**: Agent selection is typically finalized during CLASSIFIED state, but circumstances may require adding agents after initial selection. This section clarifies when and how to add agents dynamically.

**When Dynamic Agent Addition is Permitted**:

✅ **PERMITTED Scenarios**:
1. **During REQUIREMENTS**: Initial requirements reveal need for additional domain expertise
   - Example: Security requirements reveal need for security-reviewer (if not initially selected)
   - Example: Performance constraints discovered, need performance-reviewer

2. **During SYNTHESIS**: Requirements synthesis identifies gaps in domain coverage
   - Example: Agent conflicts need additional tier-1 authority (architecture-reviewer)
   - Example: Complex integration needs usability-reviewer input

3. **During IMPLEMENTATION**: Implementation complexity exceeds initial agent capabilities
   - Example: Realize need for test-reviewer for complex business logic
   - Example: Build integration issues need build-reviewer expertise

❌ **PROHIBITED Scenarios**:
1. **During VALIDATION/REVIEW**: Too late - implementation complete, adding agents is rework
2. **To bypass rejections**: Cannot add "friendly" agents to override existing agent rejections
3. **For convenience**: Cannot add agents to avoid fixing issues raised by existing agents

**Dynamic Agent Addition Process**:

**Step 1: Identify Gap and Justification**
```markdown
Document why additional agent is needed:
- What domain expertise is missing?
- What specific requirement cannot be met without this agent?
- Why wasn't this agent selected initially?
- What evidence justifies adding them now?

Example:
"During REQUIREMENTS, security-reviewer identified authentication requirements.
 Initial risk assessment classified task as MEDIUM-RISK (test files only),
 but authentication features require HIGH-RISK security validation.
 Adding security-reviewer to requirements gathering."
```

**Step 2: Create Agent Worktree**
```bash
TASK_NAME="your-task-name"
NEW_AGENT="security-reviewer"
TASK_DIR="/workspace/tasks/${TASK_NAME}"

# Create agent worktree
git worktree add "${TASK_DIR}/agents/${NEW_AGENT}/code" -b "task-${TASK_NAME}-agent-${NEW_AGENT}"

# Verify creation
ls -ld "${TASK_DIR}/agents/${NEW_AGENT}/code" || {
    echo "Failed to create agent worktree"
    exit 1
}

# Update lock file with new agent
jq --arg agent "$NEW_AGENT" '.required_agents += [$agent]' \
   "${TASK_DIR}/task.json" > "${TASK_DIR}/task.json.tmp"
mv "${TASK_DIR}/task.json.tmp" "${TASK_DIR}/task.json"
```

**Step 3: Bring Agent Up to Current State**

The catch-up process depends on when the agent is added:

**Adding During REQUIREMENTS**:
```markdown
1. Invoke new agent with same prompt as other agents
2. Agent provides requirements report
3. Continue REQUIREMENTS state with expanded agent set
4. Proceed to SYNTHESIS only after ALL agents (including new) complete
```

**Adding During SYNTHESIS**:
```markdown
1. New agent must review ALL existing requirements reports
2. Invoke agent with synthesis mode: "Review requirements from 6 agents, provide synthesis input"
3. Agent identifies conflicts, provides additional requirements
4. Update synthesis plan to incorporate new agent's input
5. Re-present plan to user (now includes new agent's perspective)
6. User approval required for revised plan
```

**Adding During IMPLEMENTATION**:
```markdown
1. New agent must review existing implementation in task branch
2. Invoke agent with implementation mode: "Review current implementation, identify gaps in [domain]"
3. Agent implements additional components in their worktree
4. Agent merges to task branch (normal implementation rounds)
5. Continue rounds until ALL agents (including new) report COMPLETE
```

**Step 4: Update Task Documentation**

```bash
# Add agent to task.md stakeholder list
# Add section for new agent's requirements/implementation plan
# Document why agent was added mid-task
```

**Lock File Required Changes**:

```json
{
  "session_id": "...",
  "task_name": "...",
  "state": "REQUIREMENTS",
  "required_agents": [
    "architecture-reviewer",
    "quality-reviewer",
    "style-reviewer",
    "security-reviewer"  // <- Newly added
  ],
  "agent_additions": [
    {
      "agent": "security-reviewer",
      "added_at_state": "REQUIREMENTS",
      "timestamp": "2025-10-18T14:30:00Z",
      "justification": "Authentication requirements discovered during requirements gathering"
    }
  ]
}
```

**State-Specific Addition Constraints**:

**REQUIREMENTS State**:
- ✅ Can add any agent type
- ✅ Agent participates fully from this point
- ✅ No state reversal needed
- ⚠️ May extend REQUIREMENTS duration (more agents to complete)

**SYNTHESIS State**:
- ✅ Can add if synthesis reveals gaps
- ⚠️ Requires re-synthesis with new agent input
- ⚠️ User must re-approve revised plan
- ❌ Cannot skip agent's requirements input (must review existing reports)

**IMPLEMENTATION State**:
- ⚠️ Use sparingly - indicates planning failure
- ✅ Permitted for genuine implementation gaps
- ⚠️ Agent must catch up by reviewing task branch
- ⚠️ May require additional implementation rounds
- ❌ Cannot add to bypass existing agent rejections

**VALIDATION/REVIEW States**:
- ❌ Too late - implementation complete
- ❌ Adding agents now requires returning to REQUIREMENTS
- ✅ IF CRITICAL: Return to REQUIREMENTS, add agent, re-execute full protocol

**Example: Adding Security Auditor During REQUIREMENTS**

```markdown
Scenario: Task classified as MEDIUM-RISK (test file modifications).
During REQUIREMENTS, architecture-reviewer identifies authentication logic in tests.

Decision:
- Authentication = security concern
- Original classification missed security implications
- security-reviewer needed for requirements gathering

Process:
1. Document justification: "Authentication logic requires security validation"
2. Create security-reviewer worktree
3. Update lock file: required_agents += ["security-reviewer"]
4. Invoke security-reviewer with REQUIREMENTS mode
5. Wait for security-reviewer requirements report
6. Proceed to SYNTHESIS with 4 agents (was 3)

Result:
- Task now has security validation coverage
- Synthesis incorporates security requirements
- Implementation includes security best practices
```

**Example: Attempting to Add Agent During REVIEW (PROHIBITED)**

```markdown
Scenario: During REVIEW, test-reviewer rejects due to insufficient test coverage.
Main agent considers adding additional test-reviewer agent.

Decision: PROHIBITED

Reason:
- REVIEW state means implementation complete
- Cannot add agents to bypass rejection
- Proper response: Return to IMPLEMENTATION, improve test coverage
- test-reviewer re-reviews improved tests in next REVIEW round

Correct Recovery:
1. Do NOT add new agent
2. Return to IMPLEMENTATION state
3. Address test-reviewer's test coverage concerns
4. Re-run REVIEW with original agent set
```

**Critical Rules for Dynamic Agent Addition**:

1. **Justify Addition**: Document why agent wasn't selected initially and why needed now
2. **Update Lock File**: Add agent to required_agents array + document in agent_additions
3. **Create Worktree**: Agent needs isolated workspace like all agents
4. **Catch-Up Required**: Agent must review all prior work before contributing
5. **No Bypass**: Cannot add agents to override existing agent decisions
6. **State Constraints**: Later states have stricter addition requirements
7. **User Approval**: If adding during SYNTHESIS, user must re-approve revised plan

**Audit Detection**:

Protocol audits check for improper dynamic agent additions:
- Agents added without justification
- Agents added to bypass rejections
- Agents added during VALIDATION/REVIEW without returning to earlier state
- Agents missing required_agents or agent_additions in lock file

### CLASSIFIED → REQUIREMENTS
**Mandatory Conditions:**
- [ ] Risk level determined (HIGH/MEDIUM/LOW)
- [ ] Agent set selected based on risk classification
- [ ] **CRITICAL: Verified currently in task worktree (pwd check)**
- [ ] Agent worktrees created for all selected agents
-  [ ] **CRITICAL: Main agent MUST create task.md at `/workspace/tasks/{TASK_NAME}/task.md` BEFORE
  transitioning to REQUIREMENTS state**

**task.md Creation Responsibility**:
- **WHO**: Main coordination agent (the agent that executed INIT and CLASSIFIED states)
- **WHEN**: After agent selection, BEFORE invoking any stakeholder agents
- **WHERE**: `/workspace/tasks/{TASK_NAME}/task.md` (task root, NOT inside code directory)
-  **WHAT**: Initial task.md with Task Objective, Scope Definition sections, and empty Stakeholder Agent
  Reports placeholders

**Verification Command**:
```bash
[ -f "/workspace/tasks/${TASK_NAME}/task.md" ] || {
  echo "❌ CRITICAL ERROR: task.md not created by CLASSIFIED state"
  echo "Required location: /workspace/tasks/${TASK_NAME}/task.md"
  echo "ABORT: Cannot proceed to REQUIREMENTS state without task.md"
  exit 1
}
echo "✅ task.md verification PASSED"
```

**Evidence Required:**
- Risk classification reasoning documented
- **MANDATORY: pwd verification showing task directory**
- Agent selection justification based on file patterns
- task.md exists with mandatory sections
- All agent worktrees created and accessible

**🚨 CRITICAL PRE-REQUIREMENTS VERIFICATION**:
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing
# MANDATORY: Verify working directory BEFORE invoking ANY agents
pwd | grep -q "/workspace/tasks/{TASK_NAME}/code$" || {
  echo "❌ CRITICAL ERROR: Not in task worktree!"
  echo "Current directory: $(pwd)"
  echo "Required directory: /workspace/tasks/{TASK_NAME}/code"
  echo "ABORT: Cannot proceed to REQUIREMENTS state"
  exit 1
}
echo "✅ Directory verification PASSED: $(pwd)"

# MANDATORY: Verify all agent worktrees exist
for agent in architecture-reviewer quality-reviewer style-reviewer; do
  [ -d "/workspace/tasks/{TASK_NAME}/agents/$agent/code" ] || {
    echo "❌ CRITICAL ERROR: Agent worktree missing: $agent"
    exit 1
  }
done
echo "✅ All agent worktrees verified"
```

**PROHIBITED PATTERN**:
❌ Invoking stakeholder agents while in main branch
❌ Proceeding to State 2 without pwd verification
❌ Assuming you're in the correct directory without checking

**REQUIRED PATTERN**:
✅ Execute pwd verification command
✅ Confirm output matches task worktree path
✅ Only then invoke stakeholder agents

### Agent Invocation Interruption Handling

**CRITICAL GUIDANCE**: When agent invocations are initiated but interrupted by user commands or directives, the following recovery pattern applies.

**Common Interruption Scenarios**:
1. **Audit Commands**: User runs `/audit-session` during REQUIREMENTS state while agents are being invoked
2. **User Questions**: User asks questions or requests status updates mid-invocation
3. **Priority Changes**: User requests different tasks or changes task priorities
4. **System Commands**: User runs other slash commands that interrupt normal flow

**Recovery Pattern After Interruption**:

```markdown
STEP 1: COMPLETE INTERRUPTING COMMAND
- Process user's immediate request fully (audit, questions, status, etc.)
- Do NOT abandon the interrupted agent invocations
- Do NOT transition to different state without completing interruption

STEP 2: VERIFY STATE CONSISTENCY
- Check lock file state matches expected state before interruption
- Verify worktrees remain intact
- Confirm no state corruption occurred during interruption

STEP 3: ASSESS AGENT COMPLETION STATUS
- Check which agents completed before interruption (via status.json files)
- Identify which agents need to be invoked or re-invoked
- Preserve any partial agent work (requirements reports, status updates)

STEP 4: RESUME AGENT INVOCATIONS
- Re-invoke incomplete agents in parallel (same as original invocation)
- Do NOT skip any required agents
- Wait for all agents to complete before state transition
```

**State Preservation Requirements During Interruption**:
- ✅ Lock file state remains unchanged (e.g., stays in REQUIREMENTS)
- ✅ Task worktree and agent worktrees remain intact
- ✅ Partial agent completions preserved (check status.json, requirements reports)
- ✅ Protocol state machine position maintained

**Verification Commands Before Resuming**:
```bash
# Check current lock state - should match state before interruption
CURRENT_STATE=$(jq -r '.state' /workspace/tasks/{TASK_NAME}/task.json)
echo "Current state: $CURRENT_STATE"

# Check which agents completed before interruption
echo "Agent completion status:"
for agent in {AGENT_LIST}; do
  if [ -f "/workspace/tasks/{TASK_NAME}/agents/$agent/status.json" ]; then
    STATUS=$(jq -r '.status' "/workspace/tasks/{TASK_NAME}/agents/$agent/status.json")
    echo "  $agent: $STATUS"
  else
    echo "  $agent: Not started (needs invocation)"
  fi
done

# Check for requirements reports from completed agents
echo "Requirements reports:"
ls -1 /workspace/tasks/{TASK_NAME}/*-requirements.md 2>/dev/null || echo "  None yet"
```

**Re-Invocation Decision Logic**:
```python
def determine_resumption_action(task_name, required_agents):
    """
    Determine what action to take after interruption based on agent completion status.
    """
    completed_agents = []
    incomplete_agents = []

    for agent in required_agents:
        status_file = f"/workspace/tasks/{task_name}/agents/{agent}/status.json"
        requirements_file = f"/workspace/tasks/{task_name}/{task_name}-{agent}-requirements.md"

        # Agent considered complete if both status.json AND requirements report exist
        if os.path.exists(status_file) and os.path.exists(requirements_file):
            completed_agents.append(agent)
        else:
            incomplete_agents.append(agent)

    if len(incomplete_agents) == 0:
        return "TRANSITION_TO_SYNTHESIS", "All agents completed before interruption"
    elif len(completed_agents) == 0:
        return "INVOKE_ALL_AGENTS", "No agents completed, re-invoke all"
    else:
        return "INVOKE_INCOMPLETE", f"Re-invoke: {incomplete_agents}"
```

**CRITICAL PROTOCOL REQUIREMENTS**:
- ❌ **PROHIBITED**: Skipping agent invocations because of interruption
- ❌ **PROHIBITED**: Transitioning to next state with incomplete agent work
- ❌ **PROHIBITED**: Assuming interruption invalidates prior agent completions
- ✅ **REQUIRED**: Resume and complete all agent invocations after interruption
- ✅ **REQUIRED**: Preserve partial progress from agents that completed before interruption
- ✅ **REQUIRED**: Verify state consistency before and after interruption

**Interruption Recovery Examples**:

**Example 1: All Agents Not Yet Invoked**
```markdown
Scenario: User runs `/audit-session` immediately after CLASSIFIED → REQUIREMENTS transition

Recovery:
1. Complete audit command execution
2. Verify lock state = "REQUIREMENTS"
3. Check agent status: None started
4. Action: Invoke all required agents in parallel (original plan)
5. Wait for all agents to complete
6. Proceed to SYNTHESIS after unanimous completion
```

**Example 2: Partial Agent Completion**
```markdown
Scenario: User asks question mid-REQUIREMENTS, 3/7 agents completed

Recovery:
1. Answer user's question
2. Verify lock state = "REQUIREMENTS"
3. Check agent status:
   - architecture-reviewer: COMPLETE (has requirements.md)
   - quality-reviewer: COMPLETE (has requirements.md)
   - style-reviewer: COMPLETE (has requirements.md)
   - build-reviewer: NOT STARTED
   - test-reviewer: NOT STARTED
   - security-reviewer: NOT STARTED
   - performance-reviewer: NOT STARTED
4. Action: Invoke 4 incomplete agents in parallel
5. Wait for remaining agents to complete
6. Proceed to SYNTHESIS after all 7 agents complete
```

**Example 3: All Agents Completed Before Interruption**
```markdown
Scenario: User runs `/audit-session` after all agent invocations finished

Recovery:
1. Complete audit command execution
2. Verify lock state = "REQUIREMENTS"
3. Check agent status: All 7 agents COMPLETE with requirements.md files
4. Action: No re-invocation needed
5. Proceed directly to SYNTHESIS state transition
```

**Integration with Audit Commands**:

When `/audit-session` or similar audit commands interrupt agent invocations:
1. **Audit takes precedence**: Complete full audit pipeline first
2. **State remains REQUIREMENTS**: Lock file not updated during audit
3. **Resume after audit**: Return to agent invocation resumption logic
4. **No state skip**: Cannot jump from REQUIREMENTS to VALIDATION via audit

### REQUIREMENTS → SYNTHESIS
**Mandatory Conditions:**
- [ ] ALL required agents invoked in parallel
- [ ] ALL agents provided complete requirement reports
- [ ] ALL agent reports written to `../{agent-name}-requirements.md`
- [ ] NO agent failures or incomplete responses
- [ ] Requirements synthesis document created
- [ ] Architecture plan addresses all stakeholder requirements
- [ ] Conflict resolution documented for competing requirements
- [ ] Implementation strategy defined with clear success criteria

### Partial Agent Completion Handling (REQUIREMENTS State)

**CRITICAL CLARIFICATION**: REQUIREMENTS → SYNTHESIS transition requires ALL agents to complete successfully. Partial completion is NOT acceptable for state transition.

**Agent Completion Criteria**:

An agent is considered COMPLETE during REQUIREMENTS state when BOTH conditions are met:
1. ✅ **status.json exists** with `{"status": "COMPLETE"}` in `/workspace/tasks/{task-name}/agents/{agent-name}/status.json`
2. ✅ **Requirements report exists** at `/workspace/tasks/{task-name}/{task-name}-{agent-name}-requirements.md`

**Verification Function**:
```bash
verify_agent_completion() {
    local TASK_NAME=$1
    local AGENT=$2
    local STATUS_FILE="/workspace/tasks/${TASK_NAME}/agents/${AGENT}/status.json"
    local REPORT_FILE="/workspace/tasks/${TASK_NAME}/${TASK_NAME}-${AGENT}-requirements.md"

    # Check status.json
    if [ ! -f "$STATUS_FILE" ]; then
        echo "❌ $AGENT: status.json missing"
        return 1
    fi

    local STATUS=$(jq -r '.status' "$STATUS_FILE" 2>/dev/null)
    if [ "$STATUS" != "COMPLETE" ]; then
        echo "❌ $AGENT: status=$STATUS (expected: COMPLETE)"
        return 1
    fi

    # Check requirements report
    if [ ! -f "$REPORT_FILE" ]; then
        echo "❌ $AGENT: requirements report missing"
        return 1
    fi

    # Verify report is non-empty
    if [ ! -s "$REPORT_FILE" ]; then
        echo "❌ $AGENT: requirements report is empty"
        return 1
    fi

    echo "✅ $AGENT: COMPLETE (status + report verified)"
    return 0
}

# Usage: Verify all agents before SYNTHESIS transition
ALL_COMPLETE=true
for agent in architecture-reviewer quality-reviewer style-reviewer build-reviewer test-reviewer security-reviewer performance-reviewer; do
    if ! verify_agent_completion "${TASK_NAME}" "$agent"; then
        ALL_COMPLETE=false
    fi
done

if [ "$ALL_COMPLETE" = "true" ]; then
    echo "✅ All agents COMPLETE - ready for SYNTHESIS transition"
    # Proceed to SYNTHESIS state
else
    echo "❌ Some agents incomplete - cannot transition to SYNTHESIS"
    # Handle incomplete agents (see recovery procedures below)
fi
```

**Handling Incomplete Agents During REQUIREMENTS**:

**Scenario 1: Agent Reports ERROR Status**
```bash
# Agent status.json shows: {"status": "ERROR", "error_message": "..."}

Recovery:
1. Read error_message from status.json
2. Classify error type:
   - Tool limitation (file too large, complexity)
     → Re-invoke with reduced scope
   - Blocker (missing dependency, unclear requirements)
     → Return to CLASSIFIED state, clarify requirements
   - Implementation error (agent bug)
     → Re-invoke with same requirements
3. After fix, re-invoke agent
4. Wait for agent to reach COMPLETE status
5. Only then proceed to SYNTHESIS
```

**Scenario 2: Agent Status is WORKING/IN_PROGRESS (Timeout)**
```bash
# Agent invoked but status stuck at WORKING for extended period

Recovery:
1. Check agent's last update timestamp
2. If timestamp old (>30 minutes):
   - Assume agent encountered issue
   - Re-invoke agent with same requirements
3. If timestamp recent:
   - Wait longer (agent still processing)
   - Monitor for status change
4. Maximum wait: 60 minutes per agent
5. After timeout: Re-invoke or escalate to user
```

**Scenario 3: Agent Status Missing (Invocation Failed)**
```bash
# Task tool invocation completed but no status.json created

Recovery:
1. Verify agent worktree exists at /workspace/tasks/{task}/agents/{agent}/code
2. If worktree missing:
   - Critical error - worktree creation failed
   - Return to CLASSIFIED state
   - Re-create agent worktrees
3. If worktree exists:
   - Agent invocation may have failed silently
   - Re-invoke agent
4. Verify status.json creation within 5 minutes of invocation
```

**Scenario 4: Multiple Agents Incomplete**
```bash
# Several agents haven't completed after reasonable wait time

Recovery:
1. Assess pattern:
   - All agents incomplete → Systematic issue (requirements unclear, worktree problem)
     → Return to CLASSIFIED state, fix root cause
   - Specific agent type incomplete → Domain-specific issue
     → Re-invoke those agents with clarified requirements
   - Random subset incomplete → Individual agent issues
     → Re-invoke incomplete agents individually
2. Maximum retry attempts: 2 per agent
3. After 2 failures: Escalate to user for guidance
```

**CRITICAL TRANSITION GATE**:

Before updating lock state from REQUIREMENTS to SYNTHESIS:
```bash
# MANDATORY gate check
echo "=== REQUIREMENTS → SYNTHESIS TRANSITION GATE ==="

# Count completed agents
COMPLETED_COUNT=0
REQUIRED_COUNT=${#REQUIRED_AGENTS[@]}

for agent in "${REQUIRED_AGENTS[@]}"; do
    if verify_agent_completion "${TASK_NAME}" "$agent"; then
        ((COMPLETED_COUNT++))
    fi
done

if [ $COMPLETED_COUNT -eq $REQUIRED_COUNT ]; then
    echo "✅ GATE PASSED: All $REQUIRED_COUNT agents COMPLETE"
    echo "Proceeding to SYNTHESIS state"
    jq '.state = "SYNTHESIS"' task.json > task.json.tmp
    mv task.json.tmp task.json
else
    echo "❌ GATE FAILED: $COMPLETED_COUNT/$REQUIRED_COUNT agents complete"
    echo "Missing agents require completion before SYNTHESIS"
    exit 1
fi
```

**PROHIBITED Transition Patterns**:
- ❌ Proceeding to SYNTHESIS with 6/7 agents complete ("one agent not critical")
- ❌ Skipping failed agent's requirements ("we can work without security review")
- ❌ Accepting ERROR status without recovery ("agent provided partial feedback")
- ❌ Assuming empty requirements report means "no requirements" (it's a failure)

**REQUIRED Patterns**:
- ✅ Wait for ALL agents to reach COMPLETE status
- ✅ Re-invoke failed agents until successful completion
- ✅ Escalate to user only after retry attempts exhausted
- ✅ Verify both status.json AND requirements report before accepting completion

### REQUIREMENTS State Exit Verification Procedure

**MANDATORY**: Before transitioning from REQUIREMENTS to SYNTHESIS, execute this comprehensive verification procedure to ensure protocol compliance.

**Exit Verification Checklist**:

```bash
#!/bin/bash
# File: verify-requirements-exit.sh
# Purpose: Comprehensive gate enforcement before REQUIREMENTS → SYNTHESIS transition

TASK_NAME="${1}"
TASK_DIR="/workspace/tasks/${TASK_NAME}"
LOCK_FILE="${TASK_DIR}/task.json"
TASK_MD="${TASK_DIR}/task.md"

echo "=== REQUIREMENTS STATE EXIT VERIFICATION ==="
echo "Task: ${TASK_NAME}"
echo ""

# =============================================================================
# CHECK 1: Lock State Verification
# =============================================================================
echo "[1/9] Verifying lock state..."

CURRENT_STATE=$(jq -r '.state' "$LOCK_FILE" 2>/dev/null)
if [ "$CURRENT_STATE" != "REQUIREMENTS" ]; then
    echo "❌ FAILED: Lock state is '$CURRENT_STATE', expected 'REQUIREMENTS'"
    exit 1
fi
echo "✅ PASSED: Lock state = REQUIREMENTS"

# =============================================================================
# CHECK 2: Session Ownership Verification
# =============================================================================
echo "[2/9] Verifying session ownership..."

LOCK_SESSION=$(jq -r '.session_id' "$LOCK_FILE")
if [ "$LOCK_SESSION" != "$CURRENT_SESSION_ID" ]; then
    echo "❌ FAILED: Lock owned by different session ($LOCK_SESSION)"
    exit 1
fi
echo "✅ PASSED: Lock owned by current session"

# =============================================================================
# CHECK 3: Working Directory Verification
# =============================================================================
echo "[3/9] Verifying working directory..."

CURRENT_DIR=$(pwd)
EXPECTED_DIR="${TASK_DIR}/code"
if [ "$CURRENT_DIR" != "$EXPECTED_DIR" ]; then
    echo "❌ FAILED: Wrong directory"
    echo "   Current: $CURRENT_DIR"
    echo "   Expected: $EXPECTED_DIR"
    exit 1
fi
echo "✅ PASSED: In task worktree directory"

# =============================================================================
# CHECK 4: Task.md Structure Verification
# =============================================================================
echo "[4/9] Verifying task.md structure..."

if [ ! -f "$TASK_MD" ]; then
    echo "❌ FAILED: task.md not found at $TASK_MD"
    exit 1
fi

# Verify required sections exist
REQUIRED_SECTIONS=(
    "Task Objective"
    "Scope Definition"
    "Stakeholder Agent Reports"
)

for section in "${REQUIRED_SECTIONS[@]}"; do
    if ! grep -q "## $section" "$TASK_MD"; then
        echo "❌ FAILED: task.md missing section: $section"
        exit 1
    fi
done
echo "✅ PASSED: task.md structure valid"

# =============================================================================
# CHECK 5: Agent Worktree Existence
# =============================================================================
echo "[5/9] Verifying agent worktrees exist..."

# Get required agents from lock file
REQUIRED_AGENTS=($(jq -r '.required_agents[]' "$LOCK_FILE" 2>/dev/null))
if [ ${#REQUIRED_AGENTS[@]} -eq 0 ]; then
    echo "❌ FAILED: No required_agents in lock file"
    exit 1
fi

for agent in "${REQUIRED_AGENTS[@]}"; do
    AGENT_DIR="${TASK_DIR}/agents/${agent}/code"
    if [ ! -d "$AGENT_DIR" ]; then
        echo "❌ FAILED: Agent worktree missing: $agent"
        echo "   Expected at: $AGENT_DIR"
        exit 1
    fi
done
echo "✅ PASSED: All ${#REQUIRED_AGENTS[@]} agent worktrees exist"

# =============================================================================
# CHECK 6: Agent Completion Status
# =============================================================================
echo "[6/9] Verifying agent completion status..."

INCOMPLETE_AGENTS=()
for agent in "${REQUIRED_AGENTS[@]}"; do
    STATUS_FILE="${TASK_DIR}/agents/${agent}/status.json"

    # Check status.json exists
    if [ ! -f "$STATUS_FILE" ]; then
        echo "❌ $agent: status.json missing"
        INCOMPLETE_AGENTS+=("$agent")
        continue
    fi

    # Check status is COMPLETE
    STATUS=$(jq -r '.status' "$STATUS_FILE" 2>/dev/null)
    if [ "$STATUS" != "COMPLETE" ]; then
        echo "❌ $agent: status=$STATUS (expected: COMPLETE)"
        INCOMPLETE_AGENTS+=("$agent")
        continue
    fi

    echo "✅ $agent: COMPLETE"
done

if [ ${#INCOMPLETE_AGENTS[@]} -gt 0 ]; then
    echo "❌ FAILED: ${#INCOMPLETE_AGENTS[@]} agents incomplete"
    echo "   Incomplete: ${INCOMPLETE_AGENTS[*]}"
    exit 1
fi
echo "✅ PASSED: All agents status = COMPLETE"

# =============================================================================
# CHECK 7: Requirements Reports Existence and Validity
# =============================================================================
echo "[7/9] Verifying requirements reports..."

MISSING_REPORTS=()
for agent in "${REQUIRED_AGENTS[@]}"; do
    REPORT_FILE="${TASK_DIR}/${TASK_NAME}-${agent}-requirements.md"

    # Check report exists
    if [ ! -f "$REPORT_FILE" ]; then
        echo "❌ $agent: requirements report missing"
        MISSING_REPORTS+=("$agent")
        continue
    fi

    # Check report is non-empty
    if [ ! -s "$REPORT_FILE" ]; then
        echo "❌ $agent: requirements report is empty"
        MISSING_REPORTS+=("$agent")
        continue
    fi

    # Check report has minimum content (at least 100 characters)
    REPORT_SIZE=$(wc -c < "$REPORT_FILE")
    if [ $REPORT_SIZE -lt 100 ]; then
        echo "❌ $agent: requirements report too small ($REPORT_SIZE bytes)"
        MISSING_REPORTS+=("$agent")
        continue
    fi

    echo "✅ $agent: requirements report valid ($REPORT_SIZE bytes)"
done

if [ ${#MISSING_REPORTS[@]} -gt 0 ]; then
    echo "❌ FAILED: ${#MISSING_REPORTS[@]} reports missing/invalid"
    echo "   Missing: ${MISSING_REPORTS[*]}"
    exit 1
fi
echo "✅ PASSED: All requirements reports valid"

# =============================================================================
# CHECK 8: State Transition History
# =============================================================================
echo "[8/9] Verifying state transition history..."

# Verify transition log exists and contains expected sequence
TRANSITION_LOG=$(jq -r '.transition_log' "$LOCK_FILE" 2>/dev/null)
if [ "$TRANSITION_LOG" = "null" ]; then
    echo "❌ FAILED: transition_log missing from lock file"
    exit 1
fi

# Check INIT → CLASSIFIED transition exists
if ! jq -e '.transition_log[] | select(.from == "INIT" and .to == "CLASSIFIED")' "$LOCK_FILE" >/dev/null 2>&1; then
    echo "❌ FAILED: Missing INIT → CLASSIFIED transition"
    exit 1
fi

# Check CLASSIFIED → REQUIREMENTS transition exists
if ! jq -e '.transition_log[] | select(.from == "CLASSIFIED" and .to == "REQUIREMENTS")' "$LOCK_FILE" >/dev/null 2>&1; then
    echo "❌ FAILED: Missing CLASSIFIED → REQUIREMENTS transition"
    exit 1
fi

echo "✅ PASSED: State transition history valid"

# =============================================================================
# CHECK 9: No Error Status in Any Agent
# =============================================================================
echo "[9/9] Verifying no agents in ERROR state..."

ERROR_AGENTS=()
for agent in "${REQUIRED_AGENTS[@]}"; do
    STATUS_FILE="${TASK_DIR}/agents/${agent}/status.json"
    STATUS=$(jq -r '.status' "$STATUS_FILE" 2>/dev/null)

    if [ "$STATUS" = "ERROR" ]; then
        ERROR_MSG=$(jq -r '.error_message // "Unknown error"' "$STATUS_FILE")
        echo "❌ $agent: ERROR status - $ERROR_MSG"
        ERROR_AGENTS+=("$agent")
    fi
done

if [ ${#ERROR_AGENTS[@]} -gt 0 ]; then
    echo "❌ FAILED: ${#ERROR_AGENTS[@]} agents in ERROR state"
    echo "   Errors: ${ERROR_AGENTS[*]}"
    exit 1
fi
echo "✅ PASSED: No agents in ERROR state"

# =============================================================================
# FINAL VERDICT
# =============================================================================
echo ""
echo "=============================================="
echo "✅✅✅ ALL CHECKS PASSED ✅✅✅"
echo "=============================================="
echo ""
echo "REQUIREMENTS state exit verification COMPLETE"
echo "Ready to transition to SYNTHESIS state"
echo ""

exit 0
```

**Usage Pattern**:

```bash
# Before updating lock state to SYNTHESIS
TASK_NAME="your-task-name"
export CURRENT_SESSION_ID="your-session-id"

# Run verification script
/workspace/.claude/hooks/verify-requirements-exit.sh "${TASK_NAME}" || {
    echo "❌ REQUIREMENTS exit verification FAILED"
    echo "Cannot transition to SYNTHESIS - resolve issues above"
    exit 1
}

# Only proceed if verification passes
echo "Verification passed - transitioning to SYNTHESIS"
jq --arg timestamp "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
   '.state = "SYNTHESIS" |
    .transition_log += [{"from": "REQUIREMENTS", "to": "SYNTHESIS", "timestamp": $timestamp}]' \
   task.json > task.json.tmp && mv task.json.tmp task.json
```

**Integration Points**:

This verification procedure should be invoked:
1. **Manually**: By main agent before SYNTHESIS transition
2. **Automatically**: By pre-transition hooks (if configured)
3. **Recovery**: When resuming crashed tasks in REQUIREMENTS state
4. **Audit**: During protocol compliance audits

**Failure Recovery**:

If verification fails at any check:
1. **DO NOT** proceed to SYNTHESIS state
2. **READ** the specific check failure message
3. **FIX** the identified issue:
   - Check 1-3: Infrastructure issues (locks, directories)
   - Check 4: task.md structure problems
   - Check 5: Missing agent worktrees → return to CLASSIFIED
   - Check 6-7: Incomplete agents → use partial completion recovery procedures
   - Check 8: State sequence violated → protocol error, escalate
   - Check 9: Agent errors → re-invoke failed agents
4. **RE-RUN** verification after fix
5. **PROCEED** only after all checks pass

**Evidence Required:**
- Agent report files exist and contain complete analysis
- Each agent response includes domain-specific requirements
- All conflicts between agent requirements identified
- Synthesis document exists with all sections completed
- Each agent requirement mapped to implementation approach
- Trade-off decisions documented with rationale
- Success criteria defined for each domain (architecture, security, performance, etc.)

**Detailed Synthesis Process Pattern:**
```
MANDATORY AFTER REQUIREMENTS completion:
1. CONFLICT RESOLUTION: Identify competing requirements between domains
   - Compare architecture-reviewer vs performance-reviewer requirements
   - Resolve style-reviewer vs quality-reviewer conflicts
   - Balance security-reviewer vs usability-reviewer trade-offs

2. ARCHITECTURE PLANNING: Design approach satisfying all constraints
   - Document chosen architectural patterns
   - Specify integration points between components
   - Define interface contracts and data flows

3. REQUIREMENT MAPPING: Document how each stakeholder requirement is addressed
   - Map each architecture-reviewer requirement to implementation component
   - Map each security-reviewer requirement to security control
   - Map each performance-reviewer requirement to optimization strategy

4. TRADE-OFF ANALYSIS: Record decisions and compromises
   - Document rejected alternatives and reasons
   - Record acceptable compromises with risk assessment
   - Define success criteria for each domain

5. IMPLEMENTATION EXECUTION PLANNING:
   - Write complete, functional code addressing all requirements
   - Document design decisions and rationale
   - Follow established project patterns
   - Edit: todo.md (mark task complete) - MUST be included in same commit as task deliverables

VIOLATION CHECK: All REQUIREMENTS state agent feedback addressed or exceptions documented

⚠️ CRITICAL COMMIT PATTERN VIOLATION: Never create separate commits for todo.md updates
MANDATORY: todo.md task completion update MUST be committed with task deliverables in single atomic commit
ANTI-PATTERN: git commit deliverables → git commit todo.md (VIOLATES PROTOCOL)
CORRECT PATTERN: git add deliverables todo.md → git commit (single atomic commit)
```

**Validation Function:**
```python
def validate_requirements_complete(required_agents, task_dir):
    for agent in required_agents:
        report_file = f"{task_dir}/../{agent}-requirements.md"
        if not os.path.exists(report_file):
            return False, f"Missing report: {report_file}"
        # Additional validation: file is non-empty, contains analysis
    return True, "All requirements complete"
```

### IMPLEMENTATION State Entry Guards (CRITICAL ENFORCEMENT)

**PURPOSE**: Prevent protocol violations by verifying ALL prerequisite artifacts exist before entering
IMPLEMENTATION state.

**MANDATORY VERIFICATION SCRIPT**:
```bash
#!/bin/bash
# File: verify-implementation-entry.sh
# Purpose: Gate enforcement before SYNTHESIS → IMPLEMENTATION transition

TASK_NAME="${1}"
TASK_DIR="/workspace/tasks/${TASK_NAME}"
TASK_MD="${TASK_DIR}/task.md"
LOCK_FILE="${TASK_DIR}/task.json"

echo "=== IMPLEMENTATION STATE ENTRY VERIFICATION ==="

# Guard 1: Verify task.md exists
if [ ! -f "$TASK_MD" ]; then
  echo "❌ ENTRY GUARD FAILED: task.md not found"
  echo "   Expected: ${TASK_MD}"
  echo "   CAUSE: CLASSIFIED state did not create task.md"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "✅ Guard 1 PASSED: task.md exists"

# Guard 2: Verify task.md contains Stakeholder Agent Reports section
if ! grep -q "## Stakeholder Agent Reports" "$TASK_MD"; then
  echo "❌ ENTRY GUARD FAILED: task.md missing Stakeholder Agent Reports section"
  echo "   CAUSE: task.md created with incomplete structure"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "✅ Guard 2 PASSED: task.md has Stakeholder Agent Reports section"

# Guard 3: Verify at least one requirements report exists
REQUIREMENTS_REPORTS=$(find "${TASK_DIR}" -maxdepth 1 -name "*-requirements.md" 2>/dev/null | wc -l)
if [ "$REQUIREMENTS_REPORTS" -eq 0 ]; then
  echo "❌ ENTRY GUARD FAILED: No stakeholder requirements reports found"
  echo "   Expected: At least one ${TASK_DIR}/*-requirements.md file"
  echo "   CAUSE: REQUIREMENTS state did not produce any agent reports"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "✅ Guard 3 PASSED: Found ${REQUIREMENTS_REPORTS} stakeholder requirements reports"

# Guard 4: Verify task.md contains implementation plans section
if ! grep -q -i "implementation plan" "$TASK_MD"; then
  echo "❌ ENTRY GUARD FAILED: task.md missing implementation plans"
  echo "   CAUSE: SYNTHESIS state did not append implementation plans to task.md"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "✅ Guard 4 PASSED: task.md contains implementation plans"

# Guard 5: Verify user approval checkpoint flag exists
USER_APPROVAL_FLAG="${TASK_DIR}/user-plan-approval-obtained.flag"
if [ ! -f "$USER_APPROVAL_FLAG" ]; then
  echo "❌ ENTRY GUARD FAILED: User plan approval not obtained"
  echo "   Expected: ${USER_APPROVAL_FLAG}"
  echo "   CAUSE: SYNTHESIS state did not wait for user approval of implementation plan"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state without user approval"
  exit 1
fi
echo "✅ Guard 5 PASSED: User plan approval obtained"

# Guard 6: Verify lock file shows complete state progression
CURRENT_STATE=$(jq -r '.state' "$LOCK_FILE" 2>/dev/null)
if [ "$CURRENT_STATE" != "SYNTHESIS" ]; then
  echo "❌ ENTRY GUARD FAILED: Lock state is '${CURRENT_STATE}', expected 'SYNTHESIS'"
  echo "   CAUSE: Attempting to skip states or transition out of order"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "✅ Guard 6 PASSED: Lock file shows SYNTHESIS state (ready for IMPLEMENTATION)"

echo ""
echo "✅✅✅ ALL ENTRY GUARDS PASSED ✅✅✅"

# Guard 7: Verify main agent understanding of delegation pattern
echo "=== IMPLEMENTATION STATE DELEGATION VERIFICATION ==="
echo ""
echo "CRITICAL REMINDER: Main coordination agent role during IMPLEMENTATION state"
echo ""
echo "✅ YOUR ROLE:"
echo "   - Invoke stakeholder agents via Task tool"
echo "   - Monitor agent status.json files"
echo "   - Coordinate merge ordering and conflict resolution"
echo "   - Determine when all agents complete their work"
echo ""
echo "❌ PROHIBITED:"
echo "   - Creating implementation files directly in task worktree"
echo "   - Writing code yourself instead of delegating to agents"
echo "   - Modifying src/main/java or src/test/java directly"
echo ""
echo "CORRECT WORKFLOW:"
echo "1. Invoke agents in parallel via Task tool"
echo "2. Wait for all agents to complete Round 1"
echo "3. Check status.json files for completion"
echo "4. If work remains → invoke agents for Round 2"
echo "5. If all complete → transition to VALIDATION"
echo ""

echo "✅ Guard 7 PASSED: Delegation pattern verification complete"
echo ""
echo "IMPLEMENTATION state transition APPROVED"
exit 0
```

**USAGE PATTERN**:
```bash
# Before updating lock state to IMPLEMENTATION
TASK_NAME="your-task-name"
/workspace/.claude/hooks/verify-implementation-entry.sh "${TASK_NAME}" || {
  echo "IMPLEMENTATION entry guards failed - aborting transition"
  echo "Review protocol documentation and fix prerequisite states"
  exit 1
}

# Only proceed if verification passes
update_lock_state "${TASK_NAME}" "IMPLEMENTATION"
```

**ENFORCEMENT MECHANISM**:

This guard script should be invoked:
1. **Automatically**: By pre-transition hooks before SYNTHESIS → IMPLEMENTATION
2. **Manually**: By main agent before proceeding to IMPLEMENTATION
3. **Recovery**: When resuming crashed tasks in SYNTHESIS state

**VIOLATION PREVENTION STRATEGY**:

The entry guards enforce the following protocol requirements:
- **task.md creation**: Prevents "skipped CLASSIFIED state" violations
- **Requirements reports**: Prevents "skipped REQUIREMENTS state" violations
- **Implementation plans**: Prevents "skipped SYNTHESIS state" violations
- **User approval**: Prevents "skipped PLAN APPROVAL checkpoint" violations
- **State sequence**: Prevents "jumped directly to IMPLEMENTATION" violations

**CRITICAL NOTE**: These guards are DEFENSIVE measures. Proper protocol compliance means these guards should
ALWAYS pass. If a guard fails, it indicates a CRITICAL VIOLATION in a previous state that MUST be corrected
before proceeding.

### IMPLEMENTATION State Audit Trail Requirements (MANDATORY)

**PURPOSE**: Enable post-completion compliance verification by maintaining persistent audit trail throughout
task execution.

**AUDIT TRAIL LOCATION**: `/workspace/tasks/{task-name}/audit-trail.json`

**MANDATORY**: Main agent MUST maintain audit-trail.json starting from INIT state through CLEANUP state.

**Audit Trail Schema**:
```json
{
  "task_name": "task-name",
  "session_id": "uuid",
  "started_at": "2025-10-19T10:00:00Z",
  "state_transitions": [
    {
      "from_state": "INIT",
      "to_state": "CLASSIFIED",
      "timestamp": "2025-10-19T10:01:00Z",
      "verification_passed": true
    }
  ],
  "agent_invocations": [
    {
      "agent_name": "architecture-reviewer",
      "agent_type": "reviewer",
      "working_dir": "/workspace/tasks/{task-name}/code/",
      "invocation_timestamp": "2025-10-19T10:05:00Z",
      "completion_timestamp": "2025-10-19T10:10:00Z",
      "report_location": "/workspace/tasks/{task-name}/task-name-architecture-reviewer-requirements.md",
      "status": "completed"
    }
  ],
  "tool_usage": [
    {
      "tool": "Task",
      "agent_name": "architecture-updater",
      "timestamp": "2025-10-19T10:15:00Z",
      "working_dir": "/workspace/tasks/{task-name}/agents/architecture-updater/code/"
    },
    {
      "tool": "Edit",
      "file_path": "todo.md",
      "timestamp": "2025-10-19T10:20:00Z",
      "note": "Main agent coordination activity (non-implementation)"
    }
  ],
  "commits": [
    {
      "sha": "abc123def",
      "agent": "architecture-updater",
      "branch": "task-branch",
      "timestamp": "2025-10-19T10:18:00Z",
      "files_created": ["FormattingRule.java"],
      "files_modified": [],
      "message": "[agent:architecture-updater] Add FormattingRule interface"
    }
  ],
  "build_verifications": [
    {
      "timestamp": "2025-10-19T10:25:00Z",
      "command": "./mvnw clean verify -pl formatter",
      "result": "SUCCESS",
      "tests_run": 34,
      "tests_passed": 34,
      "tests_failed": 0
    }
  ]
}
```

**LOGGING REQUIREMENTS**:

1. **State Transitions**: Log every state change with timestamp and verification status
2. **Agent Invocations**: Log every Task() tool call with agent name, working dir, and completion time
3. **Tool Usage**: Log Write/Edit tool usage to distinguish coordination from implementation
4. **Commits**: Associate git commits with agents via commit message tags or worktree analysis
5. **Build Verifications**: Log all Maven/build commands and results

**MAIN AGENT RESPONSIBILITIES**:

```bash
# After each state transition
log_state_transition() {
  local from_state="$1"
  local to_state="$2"

  jq --arg from "$from_state" \
     --arg to "$to_state" \
     --arg ts "$(date -Iseconds)" \
     '.state_transitions += [{
       "from_state": $from,
       "to_state": $to,
       "timestamp": $ts,
       "verification_passed": true
     }]' audit-trail.json > audit-trail.tmp && mv audit-trail.tmp audit-trail.json
}

# When invoking agent via Task tool
log_agent_invocation() {
  local agent_name="$1"
  local working_dir="$2"

  jq --arg agent "$agent_name" \
     --arg dir "$working_dir" \
     --arg ts "$(date -Iseconds)" \
     '.agent_invocations += [{
       "agent_name": $agent,
       "working_dir": $dir,
       "invocation_timestamp": $ts,
       "status": "running"
     }]' audit-trail.json > audit-trail.tmp && mv audit-trail.tmp audit-trail.json
}

# When agent completes
log_agent_completion() {
  local agent_name="$1"
  local report_location="$2"

  jq --arg agent "$agent_name" \
     --arg report "$report_location" \
     --arg ts "$(date -Iseconds)" \
     '.agent_invocations |= map(
       if .agent_name == $agent and .status == "running"
       then . + {"completion_timestamp": $ts, "report_location": $report, "status": "completed"}
       else .
       end
     )' audit-trail.json > audit-trail.tmp && mv audit-trail.tmp audit-trail.json
}
```

**VERIFICATION CHECKS**:

Before transitioning to CLEANUP state, verify audit trail completeness:
```bash
# Check all required sections exist
jq -e '.state_transitions | length > 0' audit-trail.json || echo "ERROR: No state transitions logged"
jq -e '.agent_invocations | length > 0' audit-trail.json || echo "ERROR: No agent invocations logged"
jq -e '.commits | length > 0' audit-trail.json || echo "ERROR: No commits logged"

# Check all agents completed
INCOMPLETE=$(jq '[.agent_invocations[] | select(.status != "completed")] | length' audit-trail.json)
if [ "$INCOMPLETE" -gt 0 ]; then
  echo "ERROR: $INCOMPLETE agents did not complete"
  jq '.agent_invocations[] | select(.status != "completed")' audit-trail.json
fi
```

**CRITICAL**: Audit trail MUST be preserved during CLEANUP state (see CLEANUP State Audit Preservation section).

### SYNTHESIS → IMPLEMENTATION
**Mandatory Conditions:**
- [ ] Requirements synthesis document created (all agents contributed to task.md)
- [ ] Architecture plan addresses all stakeholder requirements
-  [ ] Conflict resolution documented for competing requirements (architecture-reviewer makes final decisions
  based on tier hierarchy)
- [ ] Implementation strategy defined with clear success criteria
- [ ] Each agent's implementation plan appended to task.md
- [ ] **USER APPROVAL: All implementation plans presented to user in task.md**
- [ ] **USER CONFIRMATION: User has approved all proposed implementation approaches**

**Implementation Plan Clarity Requirements**:

When writing implementation plans in task.md during SYNTHESIS state, use EXPLICIT DELEGATION language:

**WHO IMPLEMENTS**:
✅ CORRECT: "architecture-updater agent implements core interfaces in
/workspace/tasks/{task}/agents/architecture-updater/code"
✅ CORRECT: "Main agent invokes architecture-reviewer via Task tool → architecture-updater implements Phase 1-2"
✅ CORRECT: "Stakeholder agents implement in parallel (main agent coordinates via Task tool)"
❌ AMBIGUOUS: "architecture-updater implements Phase 1-2" (who invokes? where does implementation occur?)
❌ PROHIBITED: "Main agent implements Phase 1-2 following architecture-reviewer requirements"
❌ PROHIBITED: "I will implement the feature based on the approved plan"

**CRITICAL RULE**: Any implementation activity means:
1. Main agent uses Task tool to invoke stakeholder agent
2. Stakeholder agent implements in `/workspace/tasks/{task}/agents/{agent}/code/`
3. Stakeholder agent validates, then merges to task branch

**NEXT ACTION AFTER USER APPROVAL**:

The ONLY acceptable next action after user approves SYNTHESIS is:

```markdown
✅ CORRECT:
"I am now entering IMPLEMENTATION state. Launching stakeholder agents in parallel for domain-specific implementation.

Task tool (architecture-updater): {...}
Task tool (quality-updater): {...}
Task tool (style-updater): {...}"
```

**VIOLATION PATTERNS** (never say these after SYNTHESIS approval):
```markdown
❌ "I will now implement the feature..."
❌ "Let me create the core classes..."
❌ "I'll start by implementing..."
❌ "First, I'll write the implementation..."
```

**If you catch yourself saying "I will implement"**, STOP and rephrase to "I will coordinate stakeholder
agents to implement".

## MULTI-AGENT IMPLEMENTATION WORKFLOW

### Implementation Role Boundaries - Visual Reference

| Aspect | Main Coordination Agent | Stakeholder Agent |
|--------|------------------------|-------------------|
| **Primary Role** | COORDINATE implementation | IMPLEMENT code |
| **Tool Used** | Task tool (invoke agents) | Write/Edit tools (create files) |
| **Working Directory** | `/workspace/tasks/{task}/code` (READ ONLY during IMPLEMENTATION) | `/workspace/tasks/{task}/agents/{agent}/code` (WRITE) |
| **Permitted File Operations** | Read source files for monitoring | Write/Edit ALL source files |
| **Status Updates** | Update lock state transitions | Update agent status.json |
| **Build Verification** | Monitor final task branch build | Run incremental builds in agent worktree |
| **Merge Authority** | NONE - agents merge their own work | Merge agent branch to task branch after validation |
| **Code Creation** | ❌ PROHIBITED | ✅ REQUIRED |
| **Agent Invocation** | ✅ REQUIRED (via Task tool) | ❌ NOT APPLICABLE |

**CRITICAL ANTI-PATTERNS** (NEVER DO THESE):

| Violation Pattern | Why It's Wrong | Correct Pattern |
|-------------------|----------------|-----------------|
| Main agent: `Edit src/Feature.java` | Main agent writing code directly | Main agent: `Task tool (architecture-reviewer)` → Agent writes code |
| "I will implement then have agents review" | Implementation before delegation | Launch agents first → Agents implement → Agents validate |
| Main agent creates files in task worktree | Wrong working directory | Agents create files in `/agents/{agent}/code/` |
| "Quick skeleton implementation" | Any main agent code creation violates protocol | Launch agents for ALL code creation |
| Main agent merges agent branches | Violates agent autonomy | Agents merge their own branches after validation |

**DECISION FLOWCHART**:

```
                    Need to create source file?
                              |
                              v
                    Are you main agent? ──YES──> Use Task tool to invoke stakeholder agent
                              |                            |
                              NO                           v
                              |                    Agent implements in agent worktree
                              v
              Are you in agent worktree? ──NO──> STOP - Wrong directory
                              |
                             YES
                              |
                              v
                    Proceed with Write/Edit
```

### Agent-Based Parallel Development Model

**Core Principle**: Each stakeholder agent operates as an autonomous developer with their own worktree,
implementing domain-specific requirements and merging changes to the common task branch via rebase workflow.

**Main Coordination Agent Role**:
- **PROHIBITED**: Main agent implementing code directly in task worktree
- **REQUIRED**: Main agent coordinates stakeholder agents via Subagent tool invocations
- **REQUIRED**: Main agent monitors agent status.json files for completion
- **REQUIRED**: Main agent determines when to proceed to VALIDATION based on unanimous agent completion

**CRITICAL VIOLATION**: Main agent creating implementation files (*.java, *.ts, *.py) directly in task
worktree during IMPLEMENTATION state. Implementation MUST be performed by stakeholder agents in their own
worktrees.

**Correct Pattern**:
✅ Main agent invokes architecture-reviewer agent → architecture-updater implements in their worktree → merges to
task branch
❌ Main agent creates FormattingRule.java directly in task worktree (PROTOCOL VIOLATION)

**Agent Tier System - CLARIFICATION**:

**Tier Purpose**: Used ONLY for requirements negotiation decision deadlocks, NOT for merge ordering.

**Requirements Phase Tiers (REQUIREMENTS → SYNTHESIS)**:
- **Tier 1 (Highest)**: architecture-reviewer - Final say on architecture decisions
- **Tier 2**: quality-reviewer, security-reviewer - Override lower tiers on domain issues
- **Tier 3**: style-reviewer, performance-reviewer, test-reviewer, usability-reviewer

**Tier Usage**: Tiers break decision deadlocks when agents disagree during requirements negotiation. Explicit
escalation path: Tier 3 → Tier 2 → Tier 1.

**Implementation Phase Merge Ordering (SYNTHESIS → VALIDATION)**:
- **NO tier-based ordering**: Agents merge AS SOON AS READY (parallel, not sequential)
- **Natural conflict resolution**: Git handles merge races via push rejection → rebase → retry
- **Conflict ownership**: Agent doing rebase resolves conflicts in their domain files
- **Cross-domain conflicts**: Escalate via tier system if conflicts span multiple domains

**PROHIBITED Interpretations**:
❌ "Tier 1 agents must merge before Tier 2 agents" (defeats parallelism)
❌ "Wait for all Tier N agents before Tier N+1 can merge" (unnecessary blocking)
❌ "Lower-tier agents cannot merge if higher-tier still working" (incorrect)

### Model Selection Strategy

**COST-OPTIMIZED ARCHITECTURE**: Agent model selection is designed to maximize quality while minimizing cost.

**Model Assignments**:
- **Reviewer agents** (Sonnet 4.5): Deep analysis, complex decision-making, detailed requirements generation
- **Updater agents** (Haiku 4.5): Mechanical implementation following detailed specifications

**Strategic Rationale**:

The protocol uses a **two-phase quality amplification** approach:

1. **REQUIREMENTS Phase** (High-cost, high-value):
   - Reviewer agents use Sonnet 4.5 for comprehensive analysis
   - Generate extremely detailed, implementation-ready specifications
   - Make ALL difficult decisions (architecture, design patterns, naming, trade-offs)
   - Output must be detailed enough for simpler model to execute mechanically

2. **IMPLEMENTATION Phase** (Low-cost, high-volume):
   - Updater agents use Haiku 4.5 for mechanical code generation
   - Execute reviewer specifications without making new decisions
   - Apply exact changes using Edit/Write tools with provided strings
   - No analysis, no judgment, pure implementation execution

**Critical Requirement for Reviewers**:

Reviewer agents MUST produce specifications that a **simpler model** (Haiku) can implement **without making
difficult decisions**. See agent-specific guidance sections in reviewer agent definitions for detailed
requirements.

**Quality Guarantee**:

- **Bad requirements + expensive model** = Expensive, potentially wrong implementation
- **Good requirements + cheap model** = Cost-effective, correct implementation
- **Investment in requirements quality** enables cost savings in implementation volume

**Cost Optimization Calculation**:

Typical task execution:
- 1x REQUIREMENTS round (reviewer agents analyze entire codebase): Use Sonnet 4.5
- 2-5x IMPLEMENTATION rounds (updater agents apply fixes): Use Haiku 4.5 (40% cost reduction)
- Net savings: ~30-35% on total task cost while maintaining quality

**Prohibited Patterns**:
❌ Reviewer producing vague requirements ("fix the issue")
❌ Updater making design decisions (naming, patterns, architecture)
❌ Assuming both agents need same model capability

**Success Criteria**:
✅ Updater succeeds on first attempt using only reviewer's specifications
✅ No clarification questions from updater to reviewer
✅ Implementation matches requirements without re-analysis

### Implementation Round Structure

**CRITICAL**: Implementation rounds use BOTH reviewer and updater agents in an iterative validation pattern.

**Agent Roles in IMPLEMENTATION**:
- **Reviewer agents** (Sonnet 4.5): Deep analysis, generate detailed requirements, approve/reject with specific feedback
- **Updater agents** (Haiku 4.5): Mechanical implementation, apply exact specifications, merge verified changes

**Single Agent Round Pattern**:
```
1. Updater agent implements in their worktree (/workspace/tasks/{task}/agents/{agent}-updater/code)
2. Updater agent validates locally: ./mvnw verify (in their worktree)
3. Updater agent merges to task branch
4. Reviewer agent reviews what was merged to task branch
5. Reviewer agent decides: APPROVED or REJECTED
   - If APPROVED: Round complete for this agent
   - If REJECTED: Updater agent fixes issues → merge → reviewer reviews again (repeat 4-5)
6. Both agents update status.json when work is approved and complete
```

**Multi-Agent Round Flow**:
```
Round 1:
├─ architecture-updater: Implement core interfaces → merge
├─ architecture-reviewer: Review → REJECTED (ambiguous contracts)
├─ architecture-updater: Fix contracts → merge
├─ architecture-reviewer: Review → APPROVED ✓
├─ quality-updater: Apply refactoring → merge
├─ quality-reviewer: Review → APPROVED ✓
├─ style-updater: Apply style rules → merge
├─ style-reviewer: Review → REJECTED (12 violations)
├─ style-updater: Fix violations → merge
├─ style-reviewer: Review → APPROVED ✓
└─ All agents: Update status.json {"status": "COMPLETE", "decision": "APPROVED"}

Transition to VALIDATION (all reviewers APPROVED, all status COMPLETE)
```

**Round Completion Criteria**:
```
All conditions must be met:
- [ ] All updater agents have merged their changes
- [ ] All reviewer agents have reviewed merged changes
- [ ] All reviewer agents report APPROVED (no rejections)
- [ ] All agents update status.json with "COMPLETE"
- [ ] Task branch passes ./mvnw verify
```

**Agent Work Completion Tracking**:

Each agent MUST maintain a `status.json` file to track implementation progress and signal work completion.

**Status File Location**: `/workspace/tasks/{task-name}/agents/{agent-name}/status.json`

**Status File Format**:

For reviewer agents:
```json
{
  "agent": "architecture-reviewer",
  "task": "implement-feature-x",
  "status": "WORKING|COMPLETE|BLOCKED",
  "decision": "APPROVED|REJECTED|PENDING",
  "round": 3,
  "last_review_sha": "abc123def456",
  "work_remaining": "none|description of pending work",
  "feedback": "detailed feedback for updater agent (if REJECTED)",
  "updated_at": "2025-10-15T10:30:00Z"
}
```

For updater agents:
```json
{
  "agent": "architecture-updater",
  "task": "implement-feature-x",
  "status": "WORKING|COMPLETE|BLOCKED",
  "round": 3,
  "last_merge_sha": "abc123def456",
  "work_remaining": "none|description of pending work",
  "blocked_by": null,
  "updated_at": "2025-10-15T10:30:00Z"
}
```

**Status Update Requirements**:

Updater agent after merging:
```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}-updater/status.json <<EOF
{
  "agent": "{AGENT}-updater",
  "task": "{TASK}",
  "status": "COMPLETE",
  "work_remaining": "none",
  "round": ${CURRENT_ROUND},
  "last_merge_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

Reviewer agent after reviewing:
```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}-reviewer/status.json <<EOF
{
  "agent": "{AGENT}-reviewer",
  "task": "{TASK}",
  "status": "COMPLETE",
  "decision": "APPROVED",
  "work_remaining": "none",
  "round": ${CURRENT_ROUND},
  "last_review_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

**Validation Transition Trigger**:

Main agent checks all REVIEWER agent statuses before proceeding to VALIDATION:
```bash
check_all_reviewers_approved() {
    TASK=$1
    shift
    REVIEWERS=("$@")

    # Check all reviewer agents for APPROVED status
    for reviewer in "${REVIEWERS[@]}"; do
        STATUS_FILE="/workspace/tasks/$TASK/agents/$reviewer/status.json"

        # Verify status file exists
        [ ! -f "$STATUS_FILE" ] && {
            echo "Reviewer $reviewer: status file missing"
            return 1
        }

        # Check reviewer status and decision
        STATUS=$(jq -r '.status' "$STATUS_FILE")
        DECISION=$(jq -r '.decision' "$STATUS_FILE")
        WORK=$(jq -r '.work_remaining' "$STATUS_FILE")

        [ "$STATUS" != "COMPLETE" ] && {
            echo "Reviewer $reviewer: status=$STATUS (not COMPLETE)"
            return 1
        }

        [ "$DECISION" != "APPROVED" ] && {
            echo "Reviewer $reviewer: decision=$DECISION (not APPROVED)"
            return 1
        }

        [ "$WORK" != "none" ] && {
            echo "Reviewer $reviewer: work_remaining=$WORK"
            return 1
        }
    done

    echo "All reviewers report COMPLETE with APPROVED decision"
    return 0
}

# Usage - check REVIEWER agents (not updaters)
REVIEWERS=("architecture-reviewer" "quality-reviewer" "style-reviewer" "test-reviewer")
if check_all_reviewers_approved "implement-feature-x" "${REVIEWERS[@]}"; then
    echo "✅ All reviewers approved - transitioning to VALIDATION state"
    transition_to_validation
else
    echo "⏳ Continuing implementation rounds (reviewers have feedback or work pending)"
fi
```

### Agent Non-Completion Recovery Protocol

**MANDATORY STATUS VERIFICATION** (CRITICAL COMPLIANCE REQUIREMENT):

**BEFORE VALIDATION state transition:**
```bash
check_all_agents_complete {TASK_NAME} {AGENT_LIST} || {
    echo "❌ BLOCKED: Cannot proceed to VALIDATION - agents have incomplete work"
    # Re-launch agents with incomplete work
    exit 1
}
```

**BEFORE CLEANUP state transition:**
```bash
# Verify all agents reached COMPLETE status
for agent in $AGENTS; do
    STATUS=$(jq -r '.status' "/workspace/tasks/{TASK_NAME}/agents/$agent/status.json")
    [ "$STATUS" != "COMPLETE" ] && {
        echo "❌ PROTOCOL VIOLATION: Agent $agent status = $STATUS (expected: COMPLETE)"
        echo "Cannot proceed to CLEANUP until all agents reach COMPLETE status"
        exit 1
    }
done
```

**Agent Status Lifecycle Rule:**
- Agents MUST update status.json to COMPLETE before exiting implementation work
- Status values: WORKING → IN_PROGRESS → MERGING → COMPLETE
- WAITING/BLOCKED/ERROR are INVALID terminal states
- Main agent MUST NOT proceed to CLEANUP if any agent has non-COMPLETE status

**Valid Agent Status Values:**
- `WORKING`: Agent actively implementing
- `IN_PROGRESS`: Agent has partial work merged
- `MERGING`: Agent merging changes to task branch
- `COMPLETE`: Agent finished all domain work (TERMINAL STATE)
- `ERROR`: Agent encountered unrecoverable error
- `WAITING`: Agent blocked by dependency (INVALID if dependencies resolved)

**Recovery Workflows:**

**Scenario 1: Agent status = WAITING**
```bash
# Investigate why agent is waiting
cat /workspace/tasks/{TASK}/agents/{AGENT}/status.json

# If agent waiting for other agents to complete:
#   - Allow more time for parallel work
#   - Check if blocking agent made progress

# If agent stuck in WAITING state inappropriately:
#   - Re-launch agent with explicit instructions
#   - Update status.json manually if agent completed but forgot to update
```

**Scenario 2: Agent status = ERROR**
```bash
# Review agent error logs
cat /workspace/tasks/{TASK}/agents/{AGENT}/status.json | jq '.error_message'

# Determine if error is recoverable:
#   - Tool limitations → Re-launch with adjusted scope
#   - Merge conflicts → Manually resolve and update status
#   - Implementation blocker → Escalate to user or defer via SCOPE_NEGOTIATION

# After resolution, agent MUST update status to COMPLETE
```

**Scenario 3: Agent status = IN_PROGRESS (during CLEANUP attempt)**
```bash
# PROTOCOL VIOLATION: Should not reach CLEANUP with IN_PROGRESS agents
echo "❌ VIOLATION: Agent {AGENT} status = IN_PROGRESS during CLEANUP"

# Required action:
#   1. Return to IMPLEMENTATION state
#   2. Allow agent to complete work
#   3. Verify COMPLETE status before VALIDATION
#   4. Do NOT proceed to CLEANUP until all agents COMPLETE
```

**MANDATORY RULE**: Main agent MUST verify all agents have status = COMPLETE before entering CLEANUP state. No
exceptions.

**Agent Re-Invocation Logic**:

Check if agent needs re-invocation after cross-domain changes:
```bash
check_agent_needs_reinvocation() {
    AGENT=$1
    TASK=$2
    STATUS_FILE="/workspace/tasks/$TASK/agents/$AGENT/status.json"

    LAST_MERGE=$(jq -r '.last_merge_sha' "$STATUS_FILE")
    TASK_HEAD=$(git -C /workspace/tasks/$TASK/code rev-parse HEAD)

    if [ "$LAST_MERGE" != "$TASK_HEAD" ]; then
        echo "Task branch changed since $AGENT's last merge (was: $LAST_MERGE, now: $TASK_HEAD)"
        echo "Re-invoking $AGENT to review changes"
        return 0  # Needs re-invocation
    fi
    return 1  # No re-invocation needed
}
```

**Agent Worktree Locations**:
- Task branch: `/workspace/tasks/{task-name}/code` (common merge target)
- Agent worktrees: `/workspace/tasks/{task-name}/agents/{agent-name}/code`

**Agent Implementation Scope**:
- Each agent implements ONLY their domain requirements
- architecture-reviewer: Core implementation (src/main/java)
- test-reviewer: Test files (src/test/java)
- style-reviewer: Style configs and fixes
- security-reviewer: Security features and validation
- performance-reviewer: Performance optimizations
- quality-reviewer: Refactoring and best practices
- usability-reviewer: UX improvements and documentation

**Merge Conflict Resolution**:
- Agents autonomously resolve conflicts in their own branches
- Natural git behavior: detect push rejection → rebase → retry
- No explicit locking for merges (git handles race conditions)

**Last-Merge SHA Tracking**:
- Each agent tracks SHA of task branch at their last successful merge
- Agent only rebases if task branch SHA changed since last merge
- Optimization: avoid unnecessary rebases when task branch unchanged

**Build Verification Requirements**:
- BEFORE merge: `./mvnw verify` must pass in agent worktree
- AFTER merge: `./mvnw verify` must pass in task worktree
- Agents cannot merge broken builds to task branch
- If merge breaks build: agent fixes in next round before other merges

**Round Completion Criteria**:
- ALL agents report "DOMAIN_WORK_COMPLETE: No more {domain} work needed"
- Task branch passes `./mvnw verify`
- All domain requirements addressed in task branch

**Transition to VALIDATION**:
- Only after ALL agents complete all implementation rounds
- Final verification: `./mvnw verify` on task branch before VALIDATION state

**Evidence Required:**
- Synthesis document exists with all sections completed
- Each agent requirement mapped to implementation approach
- Trade-off decisions documented with rationale
- Success criteria defined for each domain (architecture, security, performance, etc.)
- Implementation plan presented to user in clear, readable format
- User approval message received

**Plan Presentation Requirements:**
```markdown
MANDATORY BEFORE IMPLEMENTATION:
1. After SYNTHESIS complete, stop and present implementation plan to user
2. Plan must include:
   - Architecture approach and key design decisions
   - Files to be created/modified
   - Implementation sequence and dependencies
   - Testing strategy
   - Risk mitigation approaches
3. Wait for explicit user approval before proceeding to IMPLEMENTATION
4. Only proceed to IMPLEMENTATION after receiving clear user confirmation (e.g., "yes", "approved", "proceed")

PROHIBITED:
❌ Starting implementation without user plan approval
❌ Skipping plan presentation for "simple" tasks
❌ Assuming user approval without explicit confirmation
❌ Assuming approval from bypass mode or lack of objection
```

### IMPLEMENTATION → VALIDATION
**Mandatory Conditions:**
- [ ] All implementation rounds completed
- [ ] **🚨 CRITICAL: All REVIEWER agents report APPROVED decision**
- [ ] All updater agents have merged changes to task branch
- [ ] All planned deliverables created in task branch
- [ ] Implementation follows synthesis architecture plan
- [ ] Code adheres to project conventions and patterns
- [ ] All requirements from synthesis addressed or deferred with justification
- [ ] **🚨 CRITICAL: Each updater agent performed incremental validation during rounds (fail-fast pattern)**
- [ ] **🚨 CRITICAL: All updater agent changes MERGED to task branch**
- [ ] **🚨 CRITICAL: Task branch passes `./mvnw verify` after final merge**

**Evidence Required:**
- All reviewer agents: status.json shows {"status": "COMPLETE", "decision": "APPROVED"}
- All updater agents: status.json shows {"status": "COMPLETE", "work_remaining": "none"}
- Task branch contains all agent implementations (via git log)
- Implementation matches synthesis plan
- Any requirement deferrals properly documented in todo.md
- Incremental validation checkpoints passed during all rounds
- Final task branch build verification passed

**🚨 MANDATORY FAIL-FAST VALIDATION PATTERN**:

**ANTI-PATTERN** (Late-stage failure discovery - 10-15% overhead):
```
1. Implement entire feature (all components)
2. Run full validation → discover 60 violations
3. Fix violations with stale context
# Result: Excessive rework, 100-150 extra messages
```

**REQUIRED PATTERN** (Incremental validation):
```bash
# After each component implementation:
./mvnw compile -q -pl :{module}  # Verify compilation
./mvnw checkstyle:check -q -pl :{module}  # Check style
# Fix violations immediately while context is fresh
git add component-files && git commit -m "Add Component A"

# After each module completion:
./mvnw test -Dtest=ModuleTest* -q  # Run module tests
# Fix test failures before next module

# Before final VALIDATION state:
./mvnw verify -Dmaven.build.cache.enabled=false  # Full validation
```

**Incremental Validation Checkpoints:**
1. **After each component**: Compile + style check for that component only
2. **After each test class**: Run test class immediately, fix failures
3. **After each module**: Run module-specific tests
4. **Before VALIDATION state**: Full build with all quality gates

**COMMIT REQUIREMENT**:
```bash
# Commit implementation changes BEFORE running final validation
git add [implementation files]
git commit -m "Implementation message with Claude attribution"
# Record commit SHA for user review
git rev-parse HEAD
```

**MANDATORY COMMIT MESSAGE CONVENTION**:

Each stakeholder agent MUST include their agent name in commit message when merging to task branch:

**Agent Commit Format**: `[agent-name] Implementation summary`

**Examples**:
- `[architecture-updater] Add FormattingRule interface hierarchy`
- `[quality-updater] Apply factory pattern to rule instantiation`
- `[style-updater] Implement JavaDoc requirements for public APIs`
- `[test-updater] Add comprehensive test suite for FormattingRule`

**Main Agent Final Commit**: When main agent commits validation fixes or merges to main branch, commit message MUST list all contributing agents:

```bash
git commit -m "$(cat <<'EOF'
Implement FormattingRule system

Contributing agents:
- architecture-updater: Core interface design
- quality-updater: Design pattern application
- style-updater: Code style compliance
- test-updater: Test suite implementation

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

**Rationale**: Agent-attributed commits provide verifiable audit trail that multi-agent protocol was followed, not main-agent direct implementation.

### VALIDATION → REVIEW (Conditional Path)

**🚨 MANDATORY PATTERN**: Batch Collection and Fixing
1. Run ALL quality gates (checkstyle, PMD, tests)
2. Collect ALL violations from current round
3. Fix ALL issues of same type together
4. Verify ONCE after all fixes complete

**VIOLATION**: Fix-verify-fix-verify cycles waste 98% of verification time. For 60 violations, sequential fixing wastes 29 minutes and 29,000 tokens.

**Path Selection Logic:**
```
IF (source_code_modified OR runtime_behavior_changed):
    EXECUTE full validation sequence
    REQUIRED CONDITIONS:
    - [ ] Full build validation passes (./mvnw verify)
    - [ ] All tests pass
    - [ ] All automated quality gates pass (checkstyle, PMD, etc.)
    - [ ] Performance benchmarks within acceptable ranges (if applicable)
    - [ ] Security scanning clean (if applicable)
ELSE:
    SKIP to REVIEW with documentation-only validation
```

**Evidence Required (Full Path):**
- Build success output with zero exit code from `./mvnw verify`
- Quality gate results (checkstyle, PMD, etc.)
- Test execution results showing all tests pass
- Performance baseline comparison (if performance-critical changes)

**🚨 CRITICAL BUILD CACHE VERIFICATION REQUIREMENTS:**

Maven Build Cache can create false positives by restoring cached quality gate results without actually
executing analysis on modified code. This can allow violations to slip through VALIDATION phase undetected.

**MANDATORY CACHE-BYPASSING PROCEDURES:**

When running validation after code modifications:

```bash
# OPTION 1: Disable cache for critical validation (RECOMMENDED for final validation)
./mvnw verify -Dmaven.build.cache.enabled=false

# OPTION 2: If using default verify, verify quality gates actually executed
./mvnw verify
# Then verify PMD/checkstyle actually ran (not cached):
grep -q "Skipping plugin execution (cached): pmd:check" && {
  echo "❌ WARNING: PMD results were cached, not executed!"
  echo "Re-running with cache disabled..."
  ./mvnw verify -Dmaven.build.cache.enabled=false
} || echo "✅ PMD executed fresh analysis"
```

**QUALITY GATE EXECUTION VERIFICATION CHECKLIST:**

Before transitioning from VALIDATION → REVIEW:
- [ ] Build executed with `-Dmaven.build.cache.enabled=false`, OR
- [ ] Verified build output does NOT contain "Skipping plugin execution (cached)" for:
  - `pmd:check`
  - `checkstyle:check`
- [ ] **MANDATORY: Verify `git status` shows clean working directory (no uncommitted changes)**

**MANDATORY STATE TRANSITION BLOCKER - Clean Working Directory**:

```bash
# REQUIRED before transitioning VALIDATION → REVIEW
git status

# Expected output: "nothing to commit, working tree clean"
# If uncommitted changes exist → BLOCK transition
```

**State Transition Rule**: Lock file state MUST NOT be updated to REVIEW if uncommitted changes exist.

**Automated Verification**:
```bash
# Check working directory before state transition
if git status | grep -q "nothing to commit"; then
    echo "✅ Clean working directory - proceeding to REVIEW state"
    jq '.state = "REVIEW"' task.json > task.json.tmp && mv task.json.tmp task.json
else
    echo "❌ CRITICAL: Uncommitted changes detected"
    echo "All validation fixes MUST be committed before REVIEW transition"
    git status
    exit 1
fi
```

**Rationale**: Uncommitted validation fixes will NOT be included in task branch merge to main, causing build failures after integration despite passing in VALIDATION state.
  - Other quality gates (PMD, checkstyle, etc.)
- [ ] All quality gates show actual execution timestamps (not cache restoration)
- [ ] Build output explicitly shows analysis results (not "restored from cache")

**CACHE-RELATED VIOLATION PATTERNS:**

❌ **PROHIBITED**: Running `./mvnw verify` once and trusting cached results
❌ **PROHIBITED**: Assuming "BUILD SUCCESS" means quality gates actually executed
❌ **PROHIBITED**: Ignoring "Skipping plugin execution (cached)" messages in build output

✅ **REQUIRED**: Verify quality gates executed fresh analysis on modified code
✅ **REQUIRED**: Disable cache for final validation before REVIEW phase
✅ **REQUIRED**: Check build logs for cache skip messages

**Prevention**: The `-Dmaven.build.cache.enabled=false` flag forces fresh execution of all plugins, ensuring
modified code is actually analyzed by quality gates.

**Evidence Required (Skip Path):**
- Documentation that no runtime behavior changes
- Verification that only configuration/documentation modified
- Build system confirms no code compilation required

### REVIEW → COMPLETE (Unanimous Approval Gate)
**Mandatory Conditions:**
- [ ] ALL required agents invoked to review task branch (not agent worktrees)
- [ ] ALL agents return exactly: "FINAL DECISION: ✅ APPROVED - [reason]"
- [ ] NO agent returns: "FINAL DECISION: ❌ REJECTED - [issues]"
- [ ] Review evidence documented in `state.json` file
- [ ] **Build verification passes in task worktree** (`./mvnw verify` on task branch)
- [ ] **Task branch ready for user review** (all agent changes integrated)
- [ ] **USER REVIEW: Changes presented to user for review (task branch HEAD SHA)**
- [ ] **USER APPROVAL: User has approved the implemented changes**

**MANDATORY PRE-MERGE VERIFICATION**:
```bash
# In task worktree BEFORE merge attempt
cd /workspace/tasks/{task-name}/code
./mvnw clean verify  # MANDATORY clean build (detects cache issues)

# ONLY proceed to merge if exit code 0
cd /workspace/main
git merge --ff-only {task-name}
```

**🚨 CRITICAL for JPMS projects**: `./mvnw verify` alone misses stale module-info.class files. `clean verify` prevents 90% of post-merge build failures.

**CRITICAL REJECTION HANDLING:**
- **IF ANY agent returns ❌ REJECTED** → Return to IMPLEMENTATION state
- Agents perform additional implementation rounds to address rejections
- Continue rounds until all agents accept task branch
- **NO BYPASSING**: Cannot proceed without unanimous approval

**Enforcement Logic:**
```python
def validate_unanimous_approval(agent_responses):
    for agent, response in agent_responses.items():
        if "FINAL DECISION: ✅ APPROVED" not in response:
            if "FINAL DECISION: ❌ REJECTED" in response:
                return False, f"Agent {agent} rejected with specific issues"
            else:
                return False, f"Agent {agent} provided malformed decision format"
    return True, "Unanimous approval achieved"
```

**CRITICAL ENFORCEMENT RULES:**
-  **NO HUMAN OVERRIDE**: Agent decisions are atomic and binding - Claude MUST NOT ask user for permission to
  implement or defer
- **MANDATORY RESOLUTION**: ANY ❌ REJECTED triggers either resolution cycle OR scope negotiation
-  **AUTOMATIC IMPLEMENTATION**: If stakeholder requirements are technically feasible, implement them
  immediately without asking user
- **AGENT AUTHORITY**: Only agents (not users) decide what is BLOCKING vs DEFERRABLE during scope negotiation

**Scope Assessment Decision Logic:**
```python
def handle_agent_rejections(rejecting_agents, rejection_feedback):
    # Estimate resolution effort complexity
    if estimated_resolution_effort > (2 * original_task_scope):
        # Trigger scope negotiation
        return transition_to_scope_negotiation(rejecting_agents, rejection_feedback)
    else:
        # Standard resolution cycle
        return transition_to_synthesis_cycle()
```

**Prohibited Bypass Patterns:**
❌ "Proceeding despite minor rejections"
❌ "Issues are enhancement-level, not blocking"
❌ "Critical functionality complete, finalizing"
❌ "Would you like me to implement these tests or defer this work?" (NEVER ask user - agents decide)
❌ "This seems complex, should I proceed or skip?" (If feasible, implement - don't ask)

**Required Response to ❌ REJECTED:**
✅ "Agent [name] returned ❌ REJECTED, analyzing resolution scope..."
✅ IF (resolution effort ≤ 2x task scope): "Returning to SYNTHESIS state to address: [specific issues]"
✅ IF (resolution effort > 2x task scope): "Transitioning to SCOPE_NEGOTIATION state for scope assessment"
✅ "Cannot advance to COMPLETE until ALL agents return ✅ APPROVED"

**User Review After Unanimous Approval:**
```markdown
CRITICAL STATE TRANSITION REQUIREMENT:

After REVIEW state with unanimous agent approval, main agent MUST:

1. **TRANSITION TO AWAITING_USER_APPROVAL STATE** (update lock file: state = "AWAITING_USER_APPROVAL")
2. **PRESENT CHANGES TO USER** with:
   - Task branch HEAD commit SHA
   - Files modified/created/deleted (git diff --stat main..task-branch)
   - Key implementation decisions from each agent
   - Test results and quality gate status
   - Summary of how each stakeholder requirement was addressed
   - Agent implementation history (git log showing agent merges)
3. **ASK FOR APPROVAL**: "All stakeholder agents have approved the task branch. Changes available for review at task branch HEAD (SHA: <commit-sha>). May I proceed to merge to main?"
4. **WAIT FOR USER RESPONSE** - Do NOT proceed without explicit approval
5. **CREATE APPROVAL FLAG**: touch /workspace/tasks/{task-name}/user-approval-obtained.flag
6. **VERIFY FLAG CREATION**: Confirm flag exists before state transition
7. **TRANSITION TO COMPLETE STATE** (only after steps 1-6)

PROHIBITED PATTERNS:
❌ Transitioning directly from REVIEW → COMPLETE (CRITICAL VIOLATION)
❌ Transitioning from REVIEW → CLEANUP (skips both AWAITING_USER_APPROVAL and COMPLETE)
❌ Merging to main without AWAITING_USER_APPROVAL state execution
❌ Assuming user approval from agent consensus alone
❌ Skipping user review for "straightforward" changes

TASK BRANCH REVIEW:
- Task branch contains all agent implementations merged linearly
- Each agent's work visible in git log with agent branch merges
- Final state already build-verified (passed VALIDATION)
- User reviews complete integrated result

HANDLING USER-REQUESTED CHANGES:
If user requests changes during review:
1. Return to IMPLEMENTATION state
2. Direct relevant agent(s) to make requested modifications
3. Agent(s) implement changes in their worktrees
4. Agent(s) rebase and merge to task branch
5. Re-run VALIDATION
6. Re-run REVIEW (all agents review updated task branch)
7. Return to user review checkpoint
8. Repeat until user approves

PROHIBITED:
❌ Automatically proceeding to COMPLETE after stakeholder approval
❌ Skipping user review for "minor" changes
❌ Assuming user approval without explicit confirmation
❌ Proceeding to CLEANUP without user review
❌ Making changes directly in task branch (agents must use their worktrees)
```

### REVIEW → SCOPE_NEGOTIATION (Conditional Transition)
**Trigger Conditions:**
- [ ] Multiple agents returned ❌ REJECTED with extensive scope concerns
- [ ] Estimated resolution effort exceeds 2x original task scope
- [ ] Issues span multiple domains requiring significant rework

**Mandatory Conditions:**
- [ ] All rejecting agents re-invoked with SCOPE_NEGOTIATION mode
- [ ] Each agent classifies rejected items as BLOCKING vs DEFERRABLE
- [ ] Domain authority assignments respected for final decisions
- [ ] Scope negotiation results documented in `state.json` file

**Evidence Required:**
- Scope assessment from each rejecting agent
- Classification of issues (BLOCKING/DEFERRABLE) with justification
- Domain authority decisions documented
- Follow-up tasks created in todo.md for deferred work

### COMPLETE → CLEANUP

**MANDATORY SEQUENCING**: Always `cd /workspace/main` BEFORE worktree removal

**WHY**: Git cannot remove a worktree while you are inside it.

**FAILURE MODE**:
```bash
# ❌ VIOLATION: Attempting removal from inside worktree
cd /workspace/tasks/{task-name}/code  # Inside worktree
git worktree remove /workspace/tasks/{task-name}/code
# ERROR: Cannot remove worktree while inside it
```

**CORRECT SEQUENCE**:
```bash
# ✅ Step 1: Exit to main worktree FIRST
cd /workspace/main

# ✅ Step 2: Verify location
pwd | grep -q '/workspace/main$'

# ✅ Step 3: Now safe to remove
git worktree remove /workspace/tasks/{task-name}/code
```

### CLEANUP State Audit Preservation (MANDATORY)

**PURPOSE**: Preserve audit trail for post-completion compliance verification

**CRITICAL**: Audit files MUST NOT be deleted during CLEANUP. They provide the only persistent evidence of proper protocol execution.

**Files to Preserve**:
1. `/workspace/tasks/{task-name}/audit-trail.json` - Complete execution log
2. `/workspace/tasks/{task-name}/audit-snapshot.json` - Final state summary

**Preservation Procedure**:

Before removing task worktrees, create audit snapshot:
```bash
# Step 1: Create final audit snapshot
cat > /workspace/tasks/{task-name}/audit-snapshot.json <<'EOF'
{
  "task_name": "{task-name}",
  "completion_timestamp": "$(date -Iseconds)",
  "final_state": "COMPLETE",
  "total_state_transitions": $(jq '.state_transitions | length' audit-trail.json),
  "total_agent_invocations": $(jq '.agent_invocations | length' audit-trail.json),
  "agents_used": $(jq '[.agent_invocations[].agent_type] | unique' audit-trail.json),
  "total_commits": $(jq '.commits | length' audit-trail.json),
  "implementation_commits": $(jq '[.commits[] | select(.agent != "main")] | length' audit-trail.json),
  "coordination_commits": $(jq '[.commits[] | select(.agent == "main")] | length' audit-trail.json),
  "build_verifications": $(jq '.build_verifications | length' audit-trail.json),
  "verification_summary": {
    "all_builds_passed": $(jq '[.build_verifications[].success] | all' audit-trail.json),
    "stakeholder_agents_used": $(jq '[.agent_invocations[] | select(.agent_type | endswith("-updater") or endswith("-reviewer"))] | length > 0' audit-trail.json)
  }
}
EOF

# Step 2: Verify audit completeness
if [ ! -f /workspace/tasks/{task-name}/audit-trail.json ]; then
  echo "❌ VIOLATION: audit-trail.json missing before CLEANUP"
  exit 1
fi

# Step 3: Verify minimum audit requirements
total_agents=$(jq '.agent_invocations | length' /workspace/tasks/{task-name}/audit-trail.json)
if [ "$total_agents" -eq 0 ]; then
  echo "⚠️  WARNING: No agent invocations logged (possible protocol bypass)"
fi

# Step 4: Remove worktrees but PRESERVE audit files
cd /workspace/main
git worktree remove /workspace/tasks/{task-name}/code
# Remove agent worktrees if they exist
for agent_dir in /workspace/tasks/{task-name}/agents/*/code; do
  if [ -d "$agent_dir" ]; then
    git worktree remove "$agent_dir"
  fi
done

# ✅ CRITICAL: Do NOT remove audit files
# Leave audit-trail.json and audit-snapshot.json in /workspace/tasks/{task-name}/
```

**Post-CLEANUP Audit Access**:

After CLEANUP, audit files remain accessible at:
- `/workspace/tasks/{task-name}/audit-trail.json` - Full execution log
- `/workspace/tasks/{task-name}/audit-snapshot.json` - Summary metrics

**Audit Verification Commands**:
```bash
# Verify task used stakeholder agents (not main agent implementation)
jq '.agent_invocations[] | select(.agent_type | endswith("-updater"))' \
  /workspace/tasks/{task-name}/audit-trail.json

# Check for suspicious single-commit pattern
total_commits=$(jq '.commits | length' /workspace/tasks/{task-name}/audit-trail.json)
implementation_commits=$(jq '[.commits[] | select(.agent != "main")] | length' \
  /workspace/tasks/{task-name}/audit-trail.json)

if [ "$total_commits" -eq 1 ] && [ "$implementation_commits" -eq 0 ]; then
  echo "⚠️  SUSPICIOUS: Single commit with no agent implementation commits"
fi

# Verify all state transitions were logged
jq '.state_transitions[] | "\(.from_state) → \(.to_state)"' \
  /workspace/tasks/{task-name}/audit-trail.json
```

### SCOPE_NEGOTIATION → SYNTHESIS (Return Path) or SCOPE_NEGOTIATION → COMPLETE (Deferral Path)
**Decision Logic:**
```python
def process_scope_negotiation_results(agent_responses):
    blocking_issues = []
    deferrable_issues = []

    for agent, response in agent_responses.items():
        if "SCOPE_DECISION: RESOLVE_NOW" in response:
            blocking_issues.extend(extract_blocking_issues(response))
        elif "SCOPE_DECISION: DEFER" in response:
            deferrable_issues.extend(extract_deferrable_issues(response))

    if blocking_issues:
        return "TRANSITION_TO_SYNTHESIS", blocking_issues
    else:
        return "TRANSITION_TO_COMPLETE", deferrable_issues
```

**Transition to SYNTHESIS (Full Resolution Path):**
- ANY agent determines issues are BLOCKING and must be resolved
- Return to SYNTHESIS state with reduced scope focusing on blocking issues
- Deferred issues added to todo.md as follow-up tasks

**Transition to COMPLETE (Deferral Path):**
- ALL agents agree issues are DEFERRABLE beyond current task scope
- Core functionality preserved, enhancements deferred
- All deferred work properly documented in todo.md with specific task entries

**Domain Authority Framework:**
- **security-reviewer**: Absolute veto on security vulnerabilities and privacy violations
- **build-reviewer**: Final authority on deployment safety and build integrity
- **architecture-reviewer**: Final authority on architectural completeness requirements
- **performance-reviewer**: Authority on performance requirements and scalability
- **quality-reviewer**: Can defer documentation/testing if basic quality maintained
- **style-reviewer**: Can defer style issues if core functionality preserved
- **test-reviewer**: Authority on test coverage adequacy for business logic
- **usability-reviewer**: Can defer UX improvements if core functionality accessible

**Security Auditor Configuration:**
**CRITICAL: Use Project-Specific Security Model from scope.md**

For Parser Implementation Tasks:
- **Attack Model**: Single-user code formatting scenarios (see docs/project/scope.md)
- **Security Focus**: Resource exhaustion prevention, system stability
- **NOT in Scope**: Information disclosure, data exfiltration, multi-user attacks
- **Usability Priority**: Error messages should prioritize debugging assistance
- **Appropriate Limits**: Reasonable protection for legitimate code formatting use cases

**MANDATORY**: security-reviewer MUST reference docs/project/scope.md "Security Model for Parser Operations"
before conducting any parser security review.

**Authority Hierarchy for Domain Conflicts:**
When agent authorities overlap or conflict:
- Security/Privacy conflicts: Joint veto power (both security-reviewer AND security-reviewer must approve)
- Architecture/Performance conflicts: architecture-reviewer decides with performance-reviewer input
- Build/Architecture conflicts: build-reviewer has final authority (deployment safety priority)
- Testing/Quality conflicts: test-reviewer decides coverage adequacy, quality-reviewer decides standards
- Style/Quality conflicts: quality-reviewer decides (quality encompasses style)

**Scope Negotiation Agent Prompt Template:**
```python
scope_negotiation_prompt = """
Task: {task_description}
Mode: SCOPE_NEGOTIATION
Current State: SCOPE_NEGOTIATION → SYNTHESIS or COMPLETE pending
Your Previous Rejection: {rejection_feedback}
Core Task Scope: {original_task_definition}

CRITICAL PERSISTENCE REQUIREMENTS:
Apply CLAUDE.md "Deferral Justification Requirements" and "Prohibited Deferral Reasons".
Only defer if work genuinely extends beyond task boundaries, requires unavailable expertise,
is blocked by external factors, or genuinely exceeds reasonable allocation.

NEVER defer because:
❌ "This is harder than expected"
❌ "The easy solution works fine"
❌ "Perfect is the enemy of good"

SCOPE DECISION REQUEST:
Classify your rejected items based on original task scope:

BLOCKING ISSUES (must resolve now):
- Issues that prevent core functionality
- Issues that compromise long-term solution quality within achievable scope
- Issues within your domain authority that cannot be safely deferred

DEFERRABLE ISSUES (can add to todo.md):
- Issues that enhance but don't block core task
- Issues that genuinely exceed reasonable task boundaries
- Issues that can become standalone follow-up tasks

MANDATORY RESPONSE FORMAT:
BLOCKING ISSUES: [list specific items that absolutely must be resolved]
DEFERRABLE ISSUES: [list items that can become follow-up tasks]
PERSISTENCE JUSTIFICATION: [explain why deferrable items genuinely exceed scope vs. requiring more effort]
SCOPE_DECISION: DEFER or RESOLVE_NOW

If DEFER: Provide exact todo.md task entries for deferred work
If RESOLVE_NOW: Confirm all issues must be resolved in current task
"""
```

