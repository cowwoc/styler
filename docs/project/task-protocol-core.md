# Task State Machine Protocol

> **Version:** 2.0 | **Last Updated:** 2025-10-16
> **Related Documents:** [CLAUDE.md](../../CLAUDE.md) •
[task-protocol-operations.md](task-protocol-operations.md)

**CRITICAL**: This protocol applies to ALL tasks that create, modify, or delete files, using MANDATORY STATE
TRANSITIONS with zero-tolerance enforcement

**TARGET AUDIENCE**: Claude AI instances executing tasks
**ARCHITECTURE**: State machine with atomic transitions and verifiable conditions
**ENFORCEMENT**: No manual overrides - all transitions require documented evidence

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
  reject (violations found) - If ANY reject → back to IMPLEMENTATION rounds
-  **AWAITING_USER_APPROVAL**: **CHECKPOINT STATE - All agents accepted, changes committed to task branch,
  presented to user, waiting for explicit approval before COMPLETE**
-  **SCOPE_NEGOTIATION**: Determine what work can be deferred when agents reject due to scope concerns (ONLY
  when resolution effort > 2x task scope AND agent consensus permits deferral - escalate based on agent tiers,
  technical-architect makes final decision)
-  **COMPLETE**: Work merged to main branch, todo.md updated, dependent tasks unblocked (only after user
  approves changes)
- **CLEANUP**: All agent worktrees removed, task worktree removed, locks released, temporary files cleaned

### User Approval Checkpoints - MANDATORY REGARDLESS OF BYPASS MODE

**CRITICAL**: The two user approval checkpoints are MANDATORY and MUST be respected REGARDLESS of whether the
user is in "bypass permissions on" mode or any other automation mode.

**🚨 BYPASS MODE DOES NOT BYPASS USER APPROVAL CHECKPOINTS**

**Checkpoint 1: [PLAN APPROVAL] - After SYNTHESIS, Before IMPLEMENTATION**
- MANDATORY: Present implementation plan to user in clear, readable format
- MANDATORY: Wait for explicit user approval message
- PROHIBITED: Assuming user approval from bypass mode or lack of response
-  PROHIBITED: Proceeding to IMPLEMENTATION without clear "yes", "approved", "proceed", or equivalent
  confirmation

**Checkpoint 2: [CHANGE REVIEW] - After REVIEW, Before COMPLETE**
- MANDATORY: Present completed changes with commit SHA to user
- MANDATORY: Wait for explicit user review approval
- PROHIBITED: Assuming user approval from unanimous agent approval alone
- PROHIBITED: Proceeding to COMPLETE without clear user confirmation

**Verification Questions Before Proceeding:**
Before SYNTHESIS → IMPLEMENTATION:
- [ ] Did I present the complete implementation plan to the user?
- [ ] Did the user explicitly approve the plan with words like "yes", "approved", "proceed", "looks good"?
- [ ] Did I assume approval from bypass mode? (VIOLATION if yes)

Before REVIEW → COMPLETE:
- [ ] Did I present the completed changes with commit SHA?
- [ ] Did the user explicitly approve proceeding to finalization?
- [ ] Did I assume approval from agent consensus alone? (VIOLATION if yes)

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

**Agent**: Stakeholder specialist (technical-architect, style-auditor, etc.) providing domain-specific
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

**Required Agents**: technical-architect, style-auditor, code-quality-auditor, build-validator
**Additional Agents**: security-auditor (if security-related), performance-analyzer (if performance-critical),
code-tester (if new functionality), usability-reviewer (if user-facing)

### MEDIUM-RISK FILES (Domain Validation Required)
**Patterns:**
- `src/test/**/*.java` (test files)
- `docs/code-style/**` (style documentation)
- `**/resources/**/*.properties` (configuration)
- `**/*Test.java`, `**/*Tests.java` (test classes)

**Required Agents**: technical-architect, code-quality-auditor
**Additional Agents**: style-auditor (if style files), security-auditor (if config files),
performance-analyzer (if benchmarks)

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
- Base: technical-architect (always required)
- +style-auditor: If style/formatting files modified
- +security-auditor: If any configuration or resource files modified
- +performance-analyzer: If test performance or benchmarks affected
- +code-quality-auditor: Always included for code quality validation
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
**Available Agents**: technical-architect, usability-reviewer, performance-analyzer, security-auditor,
style-auditor, code-quality-auditor, code-tester, build-validator

**Processing Logic:**

**🚨 CORE AGENTS (Always Required):**
- **technical-architect**: MANDATORY for ALL file modification tasks (provides implementation requirements)

**🔍 FUNCTIONAL AGENTS (Code Implementation):**
- IF NEW CODE created: add style-auditor, code-quality-auditor, build-validator
- IF IMPLEMENTATION (not just config): add code-tester
- IF MAJOR FEATURES completed: add usability-reviewer (MANDATORY after completion)

**🛡️ SECURITY AGENTS (Actual Security Concerns):**
- IF AUTHENTICATION/AUTHORIZATION changes: add security-auditor
- IF EXTERNAL API/DATA integration: add security-auditor
- IF ENCRYPTION/CRYPTOGRAPHIC operations: add security-auditor
- IF INPUT VALIDATION/SANITIZATION: add security-auditor

**⚡ PERFORMANCE AGENTS (Performance Critical):**
- IF ALGORITHM optimization tasks: add performance-analyzer
- IF DATABASE/QUERY optimization: add performance-analyzer
- IF MEMORY/CPU intensive operations: add performance-analyzer

**🔧 FORMATTING AGENTS (Code Quality):**
- IF PARSER LOGIC modified: add performance-analyzer, security-auditor
- IF AST TRANSFORMATION changed: add code-quality-auditor, code-tester
- IF FORMATTING RULES affected: add style-auditor

**❌ AGENTS NOT NEEDED FOR SIMPLE OPERATIONS:**
- Maven module renames: NO performance-analyzer
- Configuration file updates: NO security-auditor unless changing auth
- Directory/file renames: NO performance-analyzer
- Documentation updates: Usually only technical-architect

**📊 ANALYSIS AGENTS (Research/Study Tasks):**
- IF ARCHITECTURAL ANALYSIS: add technical-architect
- IF PERFORMANCE ANALYSIS: add performance-analyzer
- IF UX/INTERFACE ANALYSIS: add usability-reviewer
- IF SECURITY ANALYSIS: add security-auditor
- IF CODE QUALITY REVIEW: add code-quality-auditor
- IF PARSER/FORMATTER PERFORMANCE ANALYSIS: add performance-analyzer

**Agent Selection Verification Checklist:**
- [ ] NEW CODE task → style-auditor included?
- [ ] Source files created/modified → build-validator included?
- [ ] Performance-critical code → performance-analyzer included?
- [ ] Security-sensitive features → security-auditor included?
- [ ] User-facing interfaces → usability-reviewer included?
- [ ] Post-implementation refactoring → code-quality-auditor included?
- [ ] AST parsing/code formatting → performance-analyzer included?

**Special Agent Usage Patterns:**
-  **style-auditor**: Apply ALL manual style guide rules from docs/code-style/ (Java, common, and
  language-specific patterns)
-  **build-validator**: For style/formatting tasks, triggers linters (checkstyle, PMD, ESLint) through build
  system
-  **build-validator**: Use alongside style-auditor to ensure comprehensive validation (automated + manual
  rules)
- **code-quality-auditor**: Post-implementation refactoring and best practices enforcement
- **code-tester**: Business logic validation and comprehensive test creation
- **security-auditor**: Data handling and storage compliance review
- **performance-analyzer**: Algorithmic efficiency and resource optimization
- **usability-reviewer**: User experience design and interface evaluation
- **technical-architect**: System architecture and implementation guidance

## COMPLETE STYLE VALIDATION FRAMEWORK

### Three-Component Style Validation
**MANDATORY PROCESS**: When style validation is required, ALL THREE components must pass:

1. **Automated Linters** (via build-validator):
   - `checkstyle`: Java coding conventions and formatting
   - `PMD`: Code quality and best practices
   - `ESLint`: JavaScript/TypeScript style (if applicable)

2. **Manual Style Rules** (via style-auditor):
   - Apply ALL detection patterns from `docs/code-style/*-claude.md`
   - Java-specific patterns (naming, structure, comments)
   - Common patterns (cross-language consistency)
   - Language-specific patterns as applicable

3. **Build Integration** (via build-validator):
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

    # Component 2: Manual style rules via style-auditor agent
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
execution-tracer → protocol-auditor → efficiency-optimizer → documentation-auditor
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
execution_tracer_output=$(invoke_agent execution-tracer \
  --task-name "${TASK_NAME}" \
  --session-id "${SESSION_ID}")

# Step 2: Check protocol compliance
protocol_audit_result=$(invoke_agent protocol-auditor \
  --execution-trace "${execution_tracer_output}")

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
IF (protocol-auditor returns FAILED):
  1. **STOP immediately** - Do not proceed to next state
  2. **IDENTIFY violation** - Read protocol-auditor output for specific check IDs
  3. **APPLY fix** - Based on violation type:
     - Check 0.2 (main agent implementation): Revert changes, re-delegate to agents
     - State sequence violation: Return to skipped state
     - Missing artifacts: Create required files/reports
     - Lock state mismatch: Update lock file to match actual state
  4. **RE-RUN protocol audit** - Verify fix resolved violation
  5. **REPEAT until PASSED** - Cannot proceed until protocol-auditor returns PASSED
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
   protocol-auditor --check-state-transition "SYNTHESIS→IMPLEMENTATION"
   ```

3. **IF AUDIT PASSES**:
   - Launch stakeholder agents in parallel
   - Monitor agent status.json files
   - Continue IMPLEMENTATION rounds

4. **IF AUDIT FAILS**:
   - STOP - do not launch agents
   - Fix violations identified by protocol-auditor
   - Re-run audit
   - Only proceed after PASSED verdict

5. **After all agents report COMPLETE**:
   - **MANDATORY PROTOCOL AUDIT** before transitioning to VALIDATION:
     ```bash
     protocol-auditor --check-implementation-complete
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
- "STEP 1 (AUTOMATIC): Invoke execution-tracer agent"
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
   → Main agent AUTOMATICALLY invokes execution-tracer
   → Main agent AUTOMATICALLY invokes protocol-auditor
   → IF protocol-auditor returns FAILED:
      → Main agent AUTOMATICALLY invokes documentation-auditor
      → Main agent fixes violations per documentation-auditor guidance
      → Main agent re-runs audit (return to execution-tracer step)
   → IF protocol-auditor returns PASSED:
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
- [ ] Protocol files loaded in SINGLE parallel read:
      `Read task-protocol-core.md + Read task-protocol-operations.md`
- [ ] Task requirements loaded in SINGLE parallel read:
      `Read todo.md + Glob locks/*.json`
- [ ] Configuration files loaded in SINGLE parallel read (if applicable):
      `Read pom.xml + Read checkstyle.xml + Read pmd.xml`
- [ ] Architecture documentation loaded with implementation files:
      `Read docs/project/architecture.md + Glob src/main/java/**/*Pattern*.java`

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
for agent in technical-architect code-quality-auditor style-auditor; do
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
   - Compare technical-architect vs performance-analyzer requirements
   - Resolve style-auditor vs code-quality-auditor conflicts
   - Balance security-auditor vs usability-reviewer trade-offs

2. ARCHITECTURE PLANNING: Design approach satisfying all constraints
   - Document chosen architectural patterns
   - Specify integration points between components
   - Define interface contracts and data flows

3. REQUIREMENT MAPPING: Document how each stakeholder requirement is addressed
   - Map each technical-architect requirement to implementation component
   - Map each security-auditor requirement to security control
   - Map each performance-analyzer requirement to optimization strategy

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

### SYNTHESIS → IMPLEMENTATION
**Mandatory Conditions:**
- [ ] Requirements synthesis document created (all agents contributed to task.md)
- [ ] Architecture plan addresses all stakeholder requirements
-  [ ] Conflict resolution documented for competing requirements (technical-architect makes final decisions
  based on tier hierarchy)
- [ ] Implementation strategy defined with clear success criteria
- [ ] Each agent's implementation plan appended to task.md
- [ ] **USER APPROVAL: All implementation plans presented to user in task.md**
- [ ] **USER CONFIRMATION: User has approved all proposed implementation approaches**

**Implementation Plan Clarity Requirements**:

When writing implementation plans in task.md during SYNTHESIS state, use EXPLICIT DELEGATION language:

**WHO IMPLEMENTS**:
✅ CORRECT: "technical-architect agent implements core interfaces in
/workspace/tasks/{task}/agents/technical-architect/code"
✅ CORRECT: "Main agent invokes technical-architect via Task tool → technical-architect implements Phase 1-2"
✅ CORRECT: "Stakeholder agents implement in parallel (main agent coordinates via Task tool)"
❌ AMBIGUOUS: "technical-architect implements Phase 1-2" (who invokes? where does implementation occur?)
❌ PROHIBITED: "Main agent implements Phase 1-2 following technical-architect requirements"
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

Task tool (technical-architect): {...}
Task tool (code-quality-auditor): {...}
Task tool (style-auditor): {...}"
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
| Main agent: `Edit src/Feature.java` | Main agent writing code directly | Main agent: `Task tool (technical-architect)` → Agent writes code |
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
✅ Main agent invokes technical-architect agent → technical-architect implements in their worktree → merges to
task branch
❌ Main agent creates FormattingRule.java directly in task worktree (PROTOCOL VIOLATION)

**Agent Tier System - CLARIFICATION**:

**Tier Purpose**: Used ONLY for requirements negotiation decision deadlocks, NOT for merge ordering.

**Requirements Phase Tiers (REQUIREMENTS → SYNTHESIS)**:
- **Tier 1 (Highest)**: technical-architect - Final say on architecture decisions
- **Tier 2**: code-quality-auditor, security-auditor - Override lower tiers on domain issues
- **Tier 3**: style-auditor, performance-analyzer, code-tester, usability-reviewer

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

### Implementation Round Structure

**Round Flow**:
```
1. All agents implement domain requirements in parallel (in their own worktrees)
2. Each agent runs `mvn verify` in their worktree before merging
3. Each agent rebases their branch on task branch (if task branch changed since last merge)
4. Each agent merges changes to task branch (as ready, parallel not sequential)
5. Each agent runs `mvn verify` in task worktree after merging
6. Each agent updates status.json with work completion status
7. If any agent has more work → repeat round
8. If all agents report "no more work in my domain" via status.json → proceed to VALIDATION
```

**Agent Work Completion Tracking**:

Each agent MUST maintain a `status.json` file to track implementation progress and signal work completion.

**Status File Location**: `/workspace/tasks/{task-name}/agents/{agent-name}/status.json`

**Status File Format**:
```json
{
  "agent": "technical-architect",
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

After each implementation round, agent must update status.json:
```bash
# Agent finishes round, updates status
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}/status.json <<EOF
{
  "agent": "{AGENT}",
  "task": "{TASK}",
  "status": "COMPLETE",
  "work_remaining": "none",
  "round": ${CURRENT_ROUND},
  "last_merge_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

**Validation Transition Trigger**:

Main agent checks all agent statuses before proceeding to VALIDATION:
```bash
check_all_agents_complete() {
    TASK=$1
    AGENTS="${@:2}"

    for agent in $AGENTS; do
        STATUS_FILE="/workspace/tasks/$TASK/agents/$agent/status.json"

        # Verify status file exists
        [ ! -f "$STATUS_FILE" ] && {
            echo "Agent $agent: status file missing"
            return 1
        }

        # Check agent status
        STATUS=$(jq -r '.status' "$STATUS_FILE")
        WORK=$(jq -r '.work_remaining' "$STATUS_FILE")

        [ "$STATUS" != "COMPLETE" ] && {
            echo "Agent $agent: status=$STATUS (not COMPLETE)"
            return 1
        }

        [ "$WORK" != "none" ] && {
            echo "Agent $agent: work_remaining=$WORK"
            return 1
        }
    done

    echo "All agents report COMPLETE with no remaining work"
    return 0
}

# Usage
if check_all_agents_complete "implement-feature-x" "technical-architect" "code-tester" "style-auditor"; then
    echo "Transitioning to VALIDATION state"
    transition_to_validation
else
    echo "Continuing implementation rounds (agents still working)"
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
- technical-architect: Core implementation (src/main/java)
- code-tester: Test files (src/test/java)
- style-auditor: Style configs and fixes
- security-auditor: Security features and validation
- performance-analyzer: Performance optimizations
- code-quality-auditor: Refactoring and best practices
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
- [ ] All implementation rounds completed (all agents report "DOMAIN_WORK_COMPLETE")
- [ ] All planned deliverables created in task branch
- [ ] Implementation follows synthesis architecture plan
- [ ] Code adheres to project conventions and patterns
- [ ] All requirements from synthesis addressed or deferred with justification
- [ ] **🚨 CRITICAL: Each agent performed incremental validation during rounds (fail-fast pattern)**
- [ ] **🚨 CRITICAL: All agent changes MERGED to task branch**
- [ ] **🚨 CRITICAL: Task branch passes `./mvnw verify` after final merge**

**Evidence Required:**
- All agents reported "DOMAIN_WORK_COMPLETE: No more {domain} work needed"
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

### VALIDATION → REVIEW (Conditional Path)
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
MANDATORY AFTER UNANIMOUS STAKEHOLDER APPROVAL:
1. Stop workflow after receiving ALL ✅ APPROVED responses from all agents
2. Task branch already contains all agent implementations (via merge rounds)
3. Present change summary to user including:
   - Task branch HEAD SHA for review
   - Files modified/created/deleted in task branch (git diff --stat main..task-branch)
   - Key implementation decisions made by each agent
   - Test results and quality gate status
   - Summary of how each stakeholder requirement was addressed
   - Agent implementation history (git log showing agent merges)
4. Ask user: "All stakeholder agents have approved the task branch. Changes available for review at task branch HEAD (SHA: <commit-sha>). Please review the integrated implementation. Would you like me to proceed with finalizing (COMPLETE → CLEANUP)?"
5. Wait for user approval response
6. Only proceed to COMPLETE state after user confirms approval

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
- **security-auditor**: Absolute veto on security vulnerabilities and privacy violations
- **build-validator**: Final authority on deployment safety and build integrity
- **technical-architect**: Final authority on architectural completeness requirements
- **performance-analyzer**: Authority on performance requirements and scalability
- **code-quality-auditor**: Can defer documentation/testing if basic quality maintained
- **style-auditor**: Can defer style issues if core functionality preserved
- **code-tester**: Authority on test coverage adequacy for business logic
- **usability-reviewer**: Can defer UX improvements if core functionality accessible

**Security Auditor Configuration:**
**CRITICAL: Use Project-Specific Security Model from scope.md**

For Parser Implementation Tasks:
- **Attack Model**: Single-user code formatting scenarios (see docs/project/scope.md)
- **Security Focus**: Resource exhaustion prevention, system stability
- **NOT in Scope**: Information disclosure, data exfiltration, multi-user attacks
- **Usability Priority**: Error messages should prioritize debugging assistance
- **Appropriate Limits**: Reasonable protection for legitimate code formatting use cases

**MANDATORY**: security-auditor MUST reference docs/project/scope.md "Security Model for Parser Operations"
before conducting any parser security review.

**Authority Hierarchy for Domain Conflicts:**
When agent authorities overlap or conflict:
- Security/Privacy conflicts: Joint veto power (both security-auditor AND security-auditor must approve)
- Architecture/Performance conflicts: technical-architect decides with performance-analyzer input
- Build/Architecture conflicts: build-validator has final authority (deployment safety priority)
- Testing/Quality conflicts: code-tester decides coverage adequacy, code-quality-auditor decides standards
- Style/Quality conflicts: code-quality-auditor decides (quality encompasses style)

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

