# Task State Machine Protocol

**CRITICAL**: This protocol applies to ALL tasks that create, modify, or delete files, using MANDATORY STATE TRANSITIONS with zero-tolerance enforcement

**TARGET AUDIENCE**: Claude AI instances executing tasks
**ARCHITECTURE**: State machine with atomic transitions and verifiable conditions
**ENFORCEMENT**: No manual overrides - all transitions require documented evidence

## STATE MACHINE ARCHITECTURE

### Core States
```
INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → [PLAN APPROVAL] → IMPLEMENTATION → VALIDATION → REVIEW → [CHANGE REVIEW] → COMPLETE → CLEANUP
                                      ↑                                                          ↓
                                      └─────────────────── SCOPE_NEGOTIATION ←──────────────────┘
```

**User Approval Checkpoints:**
- **[PLAN APPROVAL]**: After SYNTHESIS, before IMPLEMENTATION - User reviews and approves implementation plan
- **[CHANGE REVIEW]**: After REVIEW (unanimous stakeholder approval), before COMPLETE - User reviews actual changes

### State Definitions
- **INIT**: Task selected, locks acquired, session validated
- **CLASSIFIED**: Risk level determined, agents selected, isolation established
- **REQUIREMENTS**: All stakeholder requirements collected and validated
- **SYNTHESIS**: Requirements consolidated into unified architecture plan, **USER APPROVAL CHECKPOINT: Present plan to user, wait for explicit user approval**
- **IMPLEMENTATION**: Code and tests created according to user-approved synthesis plan
- **VALIDATION**: Build verification and automated quality gates passed
- **REVIEW**: All stakeholder agents provide unanimous approval, **USER REVIEW CHECKPOINT: Present changes to user, wait for review approval before proceeding**
- **SCOPE_NEGOTIATION**: Determine what work can be deferred when agents reject due to scope concerns (ONLY when resolution effort > 2x task scope AND agent consensus permits deferral - NEVER ask user for permission)
- **COMPLETE**: Work preserved to main branch, todo.md updated (only after user approves changes)
- **CLEANUP**: Worktrees removed, locks released, temporary files cleaned

### User Approval Checkpoints - MANDATORY REGARDLESS OF BYPASS MODE

**CRITICAL**: The two user approval checkpoints are MANDATORY and MUST be respected REGARDLESS of whether the user is in "bypass permissions on" mode or any other automation mode.

**🚨 BYPASS MODE DOES NOT BYPASS USER APPROVAL CHECKPOINTS**

**Checkpoint 1: [PLAN APPROVAL] - After SYNTHESIS, Before IMPLEMENTATION**
- MANDATORY: Present implementation plan to user in clear, readable format
- MANDATORY: Wait for explicit user approval message
- PROHIBITED: Assuming user approval from bypass mode or lack of response
- PROHIBITED: Proceeding to IMPLEMENTATION without clear "yes", "approved", "proceed", or equivalent confirmation

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

### State Transitions
Each transition requires **ALL** specified conditions to be met. **NO EXCEPTIONS.**

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
**Additional Agents**: security-auditor (if security-related), performance-analyzer (if performance-critical), code-tester (if new functionality), usability-reviewer (if user-facing)

### MEDIUM-RISK FILES (Domain Validation Required)
**Patterns:**
- `src/test/**/*.java` (test files)
- `docs/code-style/**` (style documentation)
- `**/resources/**/*.properties` (configuration)
- `**/*Test.java`, `**/*Tests.java` (test classes)

**Required Agents**: technical-architect, code-quality-auditor
**Additional Agents**: style-auditor (if style files), security-auditor (if config files), performance-analyzer (if benchmarks)

### LOW-RISK FILES (Minimal Validation Required)
**Patterns:**
- `*.md` (except CLAUDE.md, task-protocol.md, critical-rules.md)
- `docs/**/*.md` (general documentation)
- `todo.md` (task tracking)
- `*.txt`, `*.log` (text files)
- `**/README*` (readme files)

**Required Agents**: None (unless escalation triggered)

### Escalation Triggers
**Keywords**: "security", "architecture", "breaking", "performance", "concurrent", "database", "api", "state", "dependency"
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
**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → COMPLETE → CLEANUP
**Stakeholder Agents**: All agents based on task requirements
**Isolation**: Mandatory worktree isolation
**Review**: Complete stakeholder validation
**Use Case**: Core implementation, build configuration, security, CI/CD
**Conditional Skips**: None - all validation required

### MEDIUM_RISK_WORKFLOW (Domain Validation)
**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → COMPLETE → CLEANUP
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
**Available Agents**: technical-architect, usability-reviewer, performance-analyzer, security-auditor, style-auditor, code-quality-auditor, code-tester, build-validator

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
- **style-auditor**: Apply ALL manual style guide rules from docs/code-style/ (Java, common, and language-specific patterns)
- **build-validator**: For style/formatting tasks, triggers linters (checkstyle, PMD, ESLint) through build system
- **build-validator**: Use alongside style-auditor to ensure comprehensive validation (automated + manual rules)
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

### Style Validation Integration Points
- **VALIDATION State**: Complete style validation before transitioning to REVIEW
- **build-validator Agent**: Triggers automated linters and reports results
- **style-auditor Agent**: Validates manual detection patterns from docs/code-style/
- **Conflict Resolution**: Automatic AST-based fixes when linter rules conflict
- **Evidence Requirement**: All three validation components must pass for ✅ APPROVED

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
2. **AUTO-TRANSLATE**: "I'll interpret this as a request to work on the todo list in continuous mode, processing each task with full protocol isolation..."
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

**ENHANCED ANALYTICAL RIGOR**: Parent agent must apply deeper analysis when interpreting and following the task protocol workflow. Rather than surface-level interpretations, carefully analyze what the protocol truly requires for the specific task context.

**Critical Thinking Requirements:**
- Question assumptions about task scope and complexity
- Verify all transition conditions are genuinely met
- Apply evidence-based validation rather than procedural compliance
- Consider edge cases and alternative approaches
- Maintain skeptical evaluation of "good enough" solutions

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

**CRITICAL**: Before executing INIT phase, you MUST verify that the selected task is available for work by checking existing locks and worktrees, AND verify you can complete the task autonomously.

### Autonomous Completion Feasibility Check

Before acquiring lock and starting INIT, verify the task can be completed without user intervention:

**MANDATORY PRE-TASK CHECKLIST**:

```bash
# 1. Task has clear deliverables
grep -A 20 "TASK.*${TASK_NAME}" /workspace/branches/main/code/todo.md
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

**COMMITMENT**: If all checks pass, you MUST complete entire protocol (States 0-8) without asking user permission.

### Decision Logic: Can I Work on This Task?

**Step 1: Check for existing lock file**
```bash
# Replace {TASK_NAME} with actual task name
# Check if another instance has locked this task
cat /workspace/locks/{TASK_NAME}.json 2>/dev/null
```

**Step 2: Analyze lock ownership**
```bash
# If lock file exists, compare session IDs
LOCK_SESSION=$(jq -r '.session_id' /workspace/locks/{TASK_NAME}.json 2>/dev/null)
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
ls -d /workspace/branches/{TASK_NAME} 2>/dev/null && echo "Worktree exists" || echo "No worktree"
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

✅ **ALWAYS** check `/workspace/locks/{TASK_NAME}.json` before starting work
✅ **ALWAYS** compare lock session_id with current session_id
✅ **ALWAYS** check for existing worktree at `/workspace/branches/{TASK_NAME}`
✅ **ALWAYS** select an alternative task if lock is owned by different session
✅ **ALWAYS** ask user for guidance if worktree exists without a lock

### Example: Proper Task Selection Verification

```bash
# User: "continue with next task"

# Step 1: Identify next task from todo.md
TASK_NAME="implement-indentation-formatter"

# Step 2: MANDATORY - Check for existing lock
cat /workspace/locks/${TASK_NAME}.json 2>/dev/null
# Output: {"session_id": "abc-123-def-456", "start_time": "2025-10-04 10:30:00 UTC", "task_name": "implement-indentation-formatter"}

# Step 3: Compare with current session ID
CURRENT_SESSION="xyz-789-ghi-012"  # From system environment
LOCK_SESSION=$(jq -r '.session_id' /workspace/locks/${TASK_NAME}.json 2>/dev/null)
# LOCK_SESSION = "abc-123-def-456"

# Step 4: Decision
if [ "$LOCK_SESSION" != "$CURRENT_SESSION" ]; then
    echo "Task '${TASK_NAME}' is locked by another instance (session ${LOCK_SESSION})"
    echo "Selecting alternative task from todo.md..."
    # Find next available task without lock
fi

# Step 5: Check next available task and repeat verification
```

### Recovery from Crashed Sessions

**Scenario**: Worktree exists at `/workspace/branches/{TASK_NAME}` but no lock file exists.

**Possible Causes**:
- Previous Claude instance crashed before cleanup
- Manual worktree creation outside protocol
- Lock file manually deleted

**Required Action**: Ask user for guidance
```
"I found an existing worktree for task '{TASK_NAME}' at /workspace/branches/{TASK_NAME}
but no lock file exists at /workspace/locks/{TASK_NAME}.json. This may indicate a
crashed session. Should I:
1. Clean up the abandoned worktree and start fresh
2. Resume work in the existing worktree
3. Select a different task"
```

**DO NOT** make this decision autonomously - user knows if another instance is running.

---

## MAIN WORKTREE OPERATIONS LOCK REQUIREMENT

**CRITICAL**: Any operations executed directly on the main worktree (`/workspace/branches/main/code/`) require acquiring a special lock at `/workspace/locks/main.json`.

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

export SESSION_ID="{SESSION_ID}" && mkdir -p /workspace/locks && (set -C; echo "{\"session_id\": \"${SESSION_ID}\", \"task_name\": \"main-worktree-operation\", \"operation_type\": \"{OPERATION_TYPE}\", \"state\": \"IN_PROGRESS\", \"created_at\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}" > /workspace/locks/main.json) && echo "MAIN_LOCK_SUCCESS" || echo "MAIN_LOCK_FAILED"
```

**Step 2: Check lock ownership if MAIN_LOCK_FAILED**
```bash
# If lock acquisition failed, check who owns it
LOCK_OWNER=$(jq -r '.session_id' /workspace/locks/main.json 2>/dev/null)
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
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/locks/main.json")

if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "❌ FATAL: Cannot delete main lock owned by $LOCK_OWNER"
  exit 1
fi

# Delete main lock file (only if ownership verified)
rm -f /workspace/locks/main.json
echo "✅ Main worktree lock released"
```

### Main Worktree Lock vs Task-Specific Locks

**Key Differences:**
- **Task locks** (`/workspace/locks/{task-name}.json`): Used for task-specific worktrees during normal protocol execution
- **Main lock** (`/workspace/locks/main.json`): Used ONLY for direct operations on main worktree

**When to use each:**
- **Normal task work**: Use task-specific lock (`{task-name}.json`) and work in task worktree (`/workspace/branches/{task-name}/code/`)
- **Direct main operations**: Use main lock (`main.json`) when working directly in main worktree (`/workspace/branches/main/code/`)

**Prohibited Patterns:**
❌ Modifying main worktree files without acquiring main lock
❌ Running git operations on main branch without main lock
❌ Assuming main worktree is always available

**Required Patterns:**
✅ Acquire main lock before ANY main worktree operations
✅ Release main lock immediately after operations complete
✅ Verify lock ownership before release
✅ Wait or select alternative task if main lock is owned by different session

### Example: Git Operation on Main Branch

```bash
# User requests: "Checkout main branch to verify something"

# Step 1: Acquire main worktree lock
export SESSION_ID="057ceaa9-533b-4beb-80c3-30d510971bdc" && mkdir -p /workspace/locks && (set -C; echo "{\"session_id\": \"${SESSION_ID}\", \"task_name\": \"main-worktree-operation\", \"operation_type\": \"git-operation\", \"state\": \"IN_PROGRESS\", \"created_at\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}" > /workspace/locks/main.json) && echo "MAIN_LOCK_SUCCESS" || echo "MAIN_LOCK_FAILED"

# If MAIN_LOCK_FAILED, check ownership and abort if owned by different session

# Step 2: Perform git operation on main
cd /workspace/branches/main/code
git checkout main
# ... perform operation ...

# Step 3: Release main lock
SESSION_ID="057ceaa9-533b-4beb-80c3-30d510971bdc"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/locks/main.json")
if [ "$LOCK_OWNER" = "$SESSION_ID" ]; then
    rm -f /workspace/locks/main.json
    echo "✅ Main worktree lock released"
else
    echo "❌ FATAL: Cannot delete main lock owned by $LOCK_OWNER"
    exit 1
fi
```

---

### INIT → CLASSIFIED
**Mandatory Conditions:**
- [ ] Session ID validated and unique
- [ ] Atomic lock acquired for task (LOCK_SUCCESS received)
- [ ] Task exists in todo.md
- [ ] Worktree created for task isolation
- [ ] **CRITICAL: Changed directory to task worktree**
- [ ] **CRITICAL: Verified pwd shows task directory**

**Evidence Required:**
- Lock file creation timestamp
- Session ID validation output
- Worktree creation confirmation
- **pwd verification showing `/workspace/branches/{task-name}/code`**

**Implementation:**
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name (e.g., "refactor-line-wrapping-architecture")
# IMPORTANT: Replace {SESSION_ID} with current session ID (from system environment)

# Step 1: ATOMIC lock creation - WILL NOT overwrite existing locks
export SESSION_ID="{SESSION_ID}" && mkdir -p /workspace/locks && (set -C; echo "{\"session_id\": \"${SESSION_ID}\", \"task_name\": \"{TASK_NAME}\", \"state\": \"INIT\", \"created_at\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}" > /workspace/locks/{TASK_NAME}.json) && echo "LOCK_SUCCESS" || echo "LOCK_FAILED"

# 🚨 CRITICAL LOCK FILE FORMAT REQUIREMENTS:
# - Extension MUST be .json (NOT .lock or any other extension)
# - Files with .lock, .txt, or any other extension are COMPLETELY INVALID and have NEVER been valid
# - ALL protocol scripts ONLY check *.json files - other extensions are silently ignored
# - Field names MUST be: "session_id", "task_name", "state", "created_at" (NOT "phase", "acquired_at", or variations)
# - created_at MUST be actual ISO-8601 timestamp, NOT literal bash command string
# - NEVER manually create lock files - ALWAYS use the command above with command substitution
# - NEVER manually search for locks - SessionStart hook does this automatically
# - NEVER use echo with single quotes around the JSON - use double quotes to enable variable/command substitution
# ❌ WRONG: echo '{"created_at": "$(date ...)"}' → Creates literal string "$(date ...)"
# ✅ CORRECT: echo "{\"created_at\": \"$(date ...)\"}" → Creates actual timestamp "2025-10-05T21:07:00Z"

# CRITICAL: If LOCK_FAILED, STOP IMMEDIATELY - another instance owns this task
# MANDATORY: Select alternative task, DO NOT proceed to Step 2

# Step 2: ATOMIC worktree directory creation - fails if directory exists
cd /workspace/branches/main/code && mkdir /workspace/branches/{TASK_NAME} && echo "DIRECTORY_CREATED" || { echo "DIRECTORY_FAILED"; rm /workspace/locks/{TASK_NAME}.json 2>/dev/null; echo "SELECT ALTERNATIVE TASK"; exit 1; }

# CRITICAL: If DIRECTORY_FAILED, lock is released and alternative task must be selected
# Directory creation failure indicates another instance is working on this task

# Step 3: Create git worktree in the atomically created directory
git worktree add /workspace/branches/{TASK_NAME}/code -b {TASK_NAME} && echo "WORKTREE_SUCCESS" || { echo "WORKTREE_FAILED"; rmdir /workspace/branches/{TASK_NAME} 2>/dev/null; rm /workspace/locks/{TASK_NAME}.json 2>/dev/null; echo "SELECT ALTERNATIVE TASK"; exit 1; }

# Step 4: CRITICAL - Change to task worktree (separate execution)
cd /workspace/branches/{TASK_NAME}/code

# Step 5: MANDATORY - Verify working directory (separate execution)
pwd | grep -q "/workspace/branches/{TASK_NAME}/code$" && echo "✅ INIT complete - Working in: $(pwd)" || echo "❌ ERROR: Not in task worktree! Currently in: $(pwd)"
```

**Example for task "refactor-line-wrapping-architecture" with session ID "6cca10f2-3c44-49ba-8c90-6dfaeda8f20f"**:
```bash
# Step 1: ATOMIC lock creation (replace session ID with your actual session ID)
export SESSION_ID="6cca10f2-3c44-49ba-8c90-6dfaeda8f20f" && mkdir -p /workspace/locks && (set -C; echo "{\"session_id\": \"${SESSION_ID}\", \"task_name\": \"refactor-line-wrapping-architecture\", \"state\": \"INIT\", \"created_at\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}" > /workspace/locks/refactor-line-wrapping-architecture.json) && echo "LOCK_SUCCESS" || echo "LOCK_FAILED"

# VERIFY: Output must be "LOCK_SUCCESS" to proceed
# If "LOCK_FAILED", another instance owns this task - select alternative task immediately

# Step 2: ATOMIC directory creation - fails if directory exists
cd /workspace/branches/main/code && mkdir /workspace/branches/refactor-line-wrapping-architecture && echo "DIRECTORY_CREATED" || { echo "DIRECTORY_FAILED"; rm /workspace/locks/refactor-line-wrapping-architecture.json 2>/dev/null; echo "SELECT ALTERNATIVE TASK"; exit 1; }

# CRITICAL: If DIRECTORY_FAILED, directory already exists (another instance working)
# Lock is automatically released and alternative task must be selected

# Step 3: Create git worktree in the atomically created directory
git worktree add /workspace/branches/refactor-line-wrapping-architecture/code -b refactor-line-wrapping-architecture && echo "WORKTREE_SUCCESS" || { echo "WORKTREE_FAILED"; rmdir /workspace/branches/refactor-line-wrapping-architecture 2>/dev/null; rm /workspace/locks/refactor-line-wrapping-architecture.json 2>/dev/null; echo "SELECT ALTERNATIVE TASK"; exit 1; }

# Step 4: Change to task worktree
cd /workspace/branches/refactor-line-wrapping-architecture/code

# Step 5: Verify working directory
pwd | grep -q "/workspace/branches/refactor-line-wrapping-architecture/code$" && echo "✅ INIT complete - Working in: $(pwd)" || echo "❌ ERROR: Not in task worktree! Currently in: $(pwd)"
```

**🚨 CRITICAL**: State 0 is NOT complete until you have executed `cd` to the task worktree and verified `pwd` shows the correct directory. NEVER proceed to State 1 while still in main branch.

### Lock State Update Helper

**Purpose**: Update lock file state as task progresses through protocol phases

**Function**:
```bash
update_lock_state() {
  local TASK_NAME=$1
  local NEW_STATE=$2
  local LOCK_FILE="/workspace/locks/${TASK_NAME}.json"

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

# Usage example - update when transitioning to REQUIREMENTS phase:
update_lock_state "create-maven-plugin" "REQUIREMENTS"

# Usage example - update when transitioning to SYNTHESIS phase:
update_lock_state "create-maven-plugin" "SYNTHESIS"
```

**Valid State Values**:
- `INIT` - Initial lock creation
- `CLASSIFIED` - Risk classification complete
- `REQUIREMENTS` - Gathering stakeholder requirements
- `SYNTHESIS` - Consolidating requirements into architecture
- `IMPLEMENTATION` - Writing code
- `VALIDATION` - Build verification
- `REVIEW` - Stakeholder reviews
- `SCOPE_NEGOTIATION` - Resolving scope issues
- `COMPLETE` - Merging to main
- `CLEANUP` - Final cleanup (lock will be deleted)

**IMPORTANT**: Update lock state at the START of each phase transition to maintain accurate recovery state.

### CLASSIFIED → REQUIREMENTS
**Mandatory Conditions:**
- [ ] Risk level determined (HIGH/MEDIUM/LOW)
- [ ] Agent set selected based on risk classification
- [ ] **CRITICAL: Verified currently in task worktree (pwd check)**
- [ ] Context.md created with explicit scope boundaries

**Evidence Required:**
- Risk classification reasoning documented
- **MANDATORY: pwd verification showing task directory**
- Agent selection justification based on file patterns
- Context.md exists with mandatory sections

**🚨 CRITICAL PRE-REQUIREMENTS VERIFICATION**:
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing
# MANDATORY: Verify working directory BEFORE invoking ANY agents
pwd | grep -q "/workspace/branches/{TASK_NAME}/code$" || {
  echo "❌ CRITICAL ERROR: Not in task worktree!"
  echo "Current directory: $(pwd)"
  echo "Required directory: /workspace/branches/{TASK_NAME}/code"
  echo "ABORT: Cannot proceed to REQUIREMENTS state"
  exit 1
}
echo "✅ Directory verification PASSED: $(pwd)"
```

**Example for task "refactor-line-wrapping-architecture"**:
```bash
pwd | grep -q "/workspace/branches/refactor-line-wrapping-architecture/code$" || {
  echo "❌ CRITICAL ERROR: Not in task worktree!"
  echo "Current directory: $(pwd)"
  echo "Required directory: /workspace/branches/refactor-line-wrapping-architecture/code"
  echo "ABORT: Cannot proceed to REQUIREMENTS state"
  exit 1
}
echo "✅ Directory verification PASSED: $(pwd)"
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

### SYNTHESIS → IMPLEMENTATION
**Mandatory Conditions:**
- [ ] Requirements synthesis document created
- [ ] Architecture plan addresses all stakeholder requirements
- [ ] Conflict resolution documented for competing requirements
- [ ] Implementation strategy defined with clear success criteria
- [ ] **USER APPROVAL: Implementation plan presented to user**
- [ ] **USER CONFIRMATION: User has approved the proposed implementation approach**

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
- [ ] All planned deliverables created
- [ ] Implementation follows synthesis architecture plan
- [ ] Code adheres to project conventions and patterns
- [ ] All requirements from synthesis addressed or deferred with justification
- [ ] **🚨 CRITICAL: All implementation changes COMMITTED to task branch before validation**

**Evidence Required:**
- Git commit showing all implemented changes (with commit SHA)
- Implementation matches synthesis plan
- Any requirement deferrals properly documented in todo.md

**COMMIT REQUIREMENT**:
```bash
# Commit all implementation changes BEFORE running validation
git add [implementation files]
git commit -m "Implementation message with Claude attribution"
# Record commit SHA for user review
git rev-parse HEAD
```

**RATIONALE**: Stakeholder agents in REVIEW state review COMMITTED code, not working directory changes. User approval checkpoint requires commit SHA for review. Committing after agent approval creates confusion about what was reviewed.

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

**Evidence Required (Skip Path):**
- Documentation that no runtime behavior changes
- Verification that only configuration/documentation modified
- Build system confirms no code compilation required

### REVIEW → COMPLETE (Unanimous Approval Gate)
**Mandatory Conditions:**
- [ ] ALL required agents invoked for final system review
- [ ] ALL agents return exactly: "FINAL DECISION: ✅ APPROVED - [reason]"
- [ ] NO agent returns: "FINAL DECISION: ❌ REJECTED - [issues]"
- [ ] Review evidence documented in `state.json` file
- [ ] **Build verification passes in worktree** (`./mvnw verify` must pass before merge attempt)
- [ ] **COMMIT CHANGES: BEFORE presenting to user, create review commit with all changes**
  - **Rationale**: User reviews commits, not working directory. Commit SHA enables git-based review.
  - **Timing**: This commit happens AFTER stakeholder approval, BEFORE user approval checkpoint.
- [ ] **USER REVIEW: Changes presented to user for review (with commit SHA)**
- [ ] **USER APPROVAL: User has approved the implemented changes**

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
- **NO HUMAN OVERRIDE**: Agent decisions are atomic and binding - Claude MUST NOT ask user for permission to implement or defer
- **MANDATORY RESOLUTION**: ANY ❌ REJECTED triggers either resolution cycle OR scope negotiation
- **AUTOMATIC IMPLEMENTATION**: If stakeholder requirements are technically feasible, implement them immediately without asking user
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
1. Stop workflow after receiving ALL ✅ APPROVED responses
2. Commit all changes to task branch:
   - Stage all modified/created files
   - Create commit with descriptive message
   - Record commit SHA for user reference
3. Present change summary to user including:
   - Commit SHA for review
   - Files modified/created/deleted (git diff --stat)
   - Key implementation decisions made
   - Test results and quality gate status
   - Summary of how each stakeholder requirement was addressed
4. Ask user: "All stakeholder agents have approved. Changes committed to task branch (SHA: <commit-sha>). Please review the changes. Would you like me to proceed with finalizing (COMPLETE → CLEANUP)?"
5. Wait for user approval response
6. Only proceed to COMPLETE state after user confirms approval

COMMIT FORMAT BEFORE USER REVIEW:
- Use descriptive commit message summarizing changes
- Do NOT include todo.md in this commit (will be amended during COMPLETE state)
- Commit message pattern: "<type>: <description>" (e.g., "feat: implement security validation framework")

HANDLING USER-REQUESTED CHANGES:
If user requests changes during review:
1. Make requested modifications
2. Stage changes: git add <modified-files>
3. Amend the existing commit: git commit --amend --no-edit (or with updated message)
4. Present updated changes with new commit SHA
5. Wait for user approval again
6. Repeat until user approves

PROHIBITED:
❌ Automatically proceeding to COMPLETE after stakeholder approval
❌ Skipping user review for "minor" changes
❌ Assuming user approval without explicit confirmation
❌ Proceeding to CLEANUP without user review
❌ Asking user to review uncommitted changes
❌ Creating multiple commits instead of amending (maintain single commit for task)
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

**MANDATORY**: security-auditor MUST reference docs/project/scope.md "Security Model for Parser Operations" before conducting any parser security review.

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

