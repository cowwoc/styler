# Task State Machine Protocol

**CRITICAL**: This protocol applies to ALL tasks that create, modify, or delete files, using MANDATORY STATE TRANSITIONS with zero-tolerance enforcement

**TARGET AUDIENCE**: Claude AI instances executing tasks
**ARCHITECTURE**: State machine with atomic transitions and verifiable conditions
**ENFORCEMENT**: No manual overrides - all transitions require documented evidence

## STATE MACHINE ARCHITECTURE

### Core States
```
INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → COMPLETE → CLEANUP
                                      ↑                                      ↓
                                      └─────── SCOPE_NEGOTIATION ←──────────┘
```

### State Definitions
- **INIT**: Task selected, locks acquired, session validated
- **CLASSIFIED**: Risk level determined, agents selected, isolation established
- **REQUIREMENTS**: All stakeholder requirements collected and validated
- **SYNTHESIS**: Requirements consolidated into unified architecture plan
- **IMPLEMENTATION**: Code and tests created according to synthesis plan
- **VALIDATION**: Build verification and automated quality gates passed
- **REVIEW**: All stakeholder agents provide unanimous approval
- **SCOPE_NEGOTIATION**: Determine what work can be deferred when agents reject due to scope concerns (ONLY when resolution effort > 2x task scope AND agent consensus permits deferral - NEVER ask user for permission)
- **COMPLETE**: Work preserved to main branch, todo.md updated
- **CLEANUP**: Worktrees removed, locks released, temporary files cleaned

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

**Evidence Required:**
- Synthesis document exists with all sections completed
- Each agent requirement mapped to implementation approach
- Trade-off decisions documented with rationale
- Success criteria defined for each domain (architecture, security, performance, etc.)

### IMPLEMENTATION → VALIDATION
**Mandatory Conditions:**
- [ ] All planned deliverables created
- [ ] Implementation follows synthesis architecture plan
- [ ] Code adheres to project conventions and patterns
- [ ] All requirements from synthesis addressed or deferred with justification

**Evidence Required:**
- Git diff showing all implemented changes
- File creation/modification timestamps
- Implementation matches synthesis plan
- Any requirement deferrals properly documented in todo.md

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

### COMPLETE → CLEANUP
**Mandatory Conditions:**
- [ ] All work committed to task branch with descriptive commit message
- [ ] **Build verification passes in task worktree BEFORE merge** (`./mvnw verify` in worktree)
- [ ] Task branch merged to main branch with **LINEAR COMMIT HISTORY** (fast-forward only, NO merge commits)
- [ ] todo.md updated to mark task complete (in same commit as deliverables)
- [ ] Full build verification passes on main branch after merge (`./mvnw verify`)

**Evidence Required:**
- **Pre-merge build success in worktree** (`./mvnw verify` passes before merge attempt)
- Git log showing clean linear history (no merge commits)
- **todo.md modification included in final commit** (verify with `git show --stat | grep "todo.md"`)
- Main branch build success after integration (`./mvnw verify` passes)
- All deliverables preserved in main branch

**CRITICAL: Pre-Merge Build Verification Gate**
```bash
# MANDATORY: Verify build passes in worktree BEFORE attempting merge
cd /workspace/branches/{TASK_NAME}/code
./mvnw verify || {
    echo "❌ VIOLATION: Build fails in worktree - merge BLOCKED"
    echo "Fix all checkstyle, PMD, and test failures before merging"
    exit 1
}
```

**Rationale:**
- Prevents contamination of main branch with style violations or build failures
- Catches linter violations (checkstyle, PMD) before they reach main
- Ensures all tests pass in isolation before integration
- Eliminates need for cleanup commits on main branch after merge
- Maintains main branch in continuously deployable state

**CRITICAL VALIDATION: todo.md Commit Verification**
```bash
# MANDATORY: Verify todo.md is in the commit BEFORE merging to main
git show --stat | grep "todo.md" || { echo "❌ VIOLATION: todo.md not in commit"; exit 1; }
git show --name-only | grep "todo.md" || { echo "❌ VIOLATION: todo.md not modified"; exit 1; }
```

**CRITICAL: Linear Commit History Requirement**

**Why Linear History?**
- Preserves clean, readable commit history for code archaeology
- Enables precise git bisect for regression identification
- Simplifies revert operations and cherry-picking
- Avoids visual clutter from merge commits in git log
- Maintains chronological order of changes

**Git Safety Sequence (Concurrency-Safe):**
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing
# CRITICAL: This procedure is safe for concurrent execution by multiple Claude instances
#           All operations occur in task worktree, avoiding race conditions on main worktree

# Step 1: Ensure clean working state
cd /workspace/branches/{TASK_NAME}/code
rm -f .temp_dir
git status --porcelain | grep -E "(dist/|target/|\.temp_dir$)" | wc -l  # Must be 0

# Step 2: Commit all task deliverables (code, tests, docs)
git add <changed-files>
git commit -m "Descriptive commit message for task changes"

# Step 3: Update todo.md to mark task complete
# Edit todo.md to change task status from [ ] to [x] and add completion details
# Then amend the commit to include todo.md changes
git add todo.md
git commit --amend --no-edit

# Step 4: Verify todo.md is included in the commit
git show --stat | grep "todo.md" || { echo "ERROR: todo.md not in commit"; exit 1; }

# Step 5: Fetch latest main and rebase to create linear history
git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
git rebase origin/main

# Step 6: PRE-MERGE BUILD VERIFICATION (CRITICAL GATE)
# MANDATORY: Verify build passes in worktree BEFORE push attempt
./mvnw verify || {
    echo "❌ VIOLATION: Build fails in worktree - push BLOCKED"
    echo "Fix all checkstyle, PMD, and test failures before merging"
    exit 1
}

# Step 7: Atomic push to main worktree (TRULY CONCURRENCY-SAFE)
# Main worktree has receive.denyCurrentBranch=updateInstead configured
# Git's internal locking ensures only one push succeeds at a time
# Push automatically updates both ref and working tree atomically
git push /workspace/branches/main/code HEAD:refs/heads/main || {
    echo "❌ Push failed - another instance merged first"
    echo "Solution: Fetch, rebase, and retry"
    git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
    git rebase origin/main
    # Retry after rebase
    git push /workspace/branches/main/code HEAD:refs/heads/main || exit 1
}

# Step 8: Post-merge verification (from task worktree)
# Verify push succeeded
git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5  # Must show our commit at HEAD
git show origin/main --stat | grep "todo.md" || { echo "ERROR: todo.md missing"; exit 1; }

# Step 9: OPTIONAL - Verify build on main (skip if Step 6 passed)
# Main worktree is automatically updated by push (receive.denyCurrentBranch=updateInstead)
# cd /workspace/branches/main/code && ./mvnw verify -q
```

**Concurrency Safety Guarantees:**
- **Truly atomic**: Git's push with receive.denyCurrentBranch=updateInstead uses internal locking
- **No race conditions**: Push operations are serialized by git's index.lock
- **Automatic working tree update**: Main worktree files updated automatically on successful push
- **Automatic conflict detection**: Non-fast-forward push fails cleanly
- **Clear retry path**: Fetch + rebase + retry on conflict
- **Perfect concurrency**: Multiple instances can push simultaneously without conflicts

**Repository Configuration:**
```bash
# Main worktree must have this setting (already configured):
cd /workspace/branches/main/code
git config receive.denyCurrentBranch updateInstead
```

**Example for task "refactor-line-wrapping-architecture"**:
```bash
# Step 1: Ensure clean state (in task worktree)
cd /workspace/branches/refactor-line-wrapping-architecture/code
git status --porcelain | grep -E "(dist/|target/)" | wc -l  # Must be 0

# Step 2: Commit task changes
git add src/ pom.xml
git commit -m "refactor: restructure line-wrapping architecture for reusability"

# Step 3: Update todo.md and amend commit
# (Edit todo.md to mark task complete)
git add todo.md
git commit --amend --no-edit

# Step 4: Verify todo.md included
git show --stat | grep "todo.md"

# Step 5: Fetch and rebase onto main
git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
git rebase origin/main

# Step 6: PRE-MERGE BUILD VERIFICATION (blocks push if fails)
./mvnw verify || {
    echo "❌ Build failed - fix violations before merging"
    exit 1
}

# Step 7: Atomic push to main worktree (concurrency-safe)
git push /workspace/branches/main/code HEAD:refs/heads/main || {
    echo "❌ Push failed - another instance merged first"
    echo "Rebasing onto updated main..."
    git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
    git rebase origin/main
    # Retry push after rebase
    git push /workspace/branches/main/code HEAD:refs/heads/main
}

# Step 8: Post-merge verification (from task worktree)
git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -10
git show origin/main --stat | grep "todo.md"
```

**Fast-Forward Merge Validation:**
After merge, `git log --graph --oneline` should show:
```
* 7f8deba (HEAD -> main) Implement CLI security basics (SEC-001 through SEC-007)
* 6a2b1c3 Previous commit on main
* 5d9e4f2 Earlier commit
```

NOT this (merge commit creates non-linear history):
```
*   8g9h5i6 (HEAD -> main) Merge branch 'implement-cli-security-basics'  ← ❌ WRONG
|\
| * 7f8deba Implement CLI security basics
|/
* 6a2b1c3 Previous commit on main
```

### AUTOMATIC CONFLICT RESOLUTION VIA ATOMIC OPERATIONS

**DESIGN PRINCIPLE**: Race conditions are prevented automatically through atomic operations - no manual recovery needed.

**How Atomic Operations Prevent Conflicts:**

**Scenario 1: Both instances attempt same task simultaneously**
- **Instance A**: Executes `mkdir /workspace/branches/task-x` → **SUCCESS** (directory created)
- **Instance B**: Executes `mkdir /workspace/branches/task-x` → **FAILS** (directory exists)
- **Instance B**: Automatically releases lock and selects alternative task
- **Instance A**: Continues work uninterrupted

**Scenario 2: Instance A working, Instance B attempts same task**
- **Instance A**: Has lock, has worktree directory
- **Instance B**: Lock creation fails (`set -C` prevents overwrite) → **LOCK_FAILED**
- **Instance B**: Selects alternative task immediately (never attempts directory creation)
- **Instance A**: Unaffected, continues work

**Scenario 3: Lock acquired but directory creation fails**
- **Instance**: Lock created successfully
- **Instance**: Directory creation fails (another instance's directory exists)
- **Instance**: Automatically releases lock via error handler: `rm /workspace/locks/{TASK_NAME}.json`
- **Instance**: Selects alternative task
- **Other instance's work**: Preserved (directory and worktree untouched)

**Key Safety Features:**

1. **Lock Creation**: `set -C` flag prevents overwriting existing locks
2. **Directory Creation**: `mkdir` (not `mkdir -p`) fails atomically if directory exists
3. **Automatic Cleanup**: Error handlers release locks when conflicts detected
4. **Work Preservation**: Existing worktrees never deleted by conflicting instance
5. **Clear Signals**: LOCK_FAILED / DIRECTORY_FAILED / WORKTREE_FAILED indicate next action

**No Manual Recovery Required:**
- ✅ Atomic operations handle all race conditions automatically
- ✅ Error handlers ensure proper cleanup on conflict
- ✅ Clear failure signals direct instance to select alternative task
- ✅ Other instance's work always preserved

---

### CLEANUP (Final State)
**Mandatory Conditions:**
- [ ] Task lock released
- [ ] Worktree removed safely
- [ ] Task branch deleted
- [ ] Temporary files cleaned
- [ ] `state.json` file archived

**Implementation (Concurrency-Safe):**
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing

# Step 1: Verify work preserved (can check from task worktree)
cd /workspace/branches/{TASK_NAME}/code
git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5 | grep "{TASK_NAME}" || {
    echo "❌ FATAL: Work not found in main branch"
    exit 1
}

# Safe cleanup sequence with session verification
# CRITICAL: Verify lock ownership before deletion
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/locks/{TASK_NAME}.json")
if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "❌ FATAL: Cannot delete lock owned by $LOCK_OWNER"
  exit 1
fi

# Delete lock file (only if ownership verified)
rm -f /workspace/locks/{TASK_NAME}.json

# ╔═══════════════════════════════════════════════════════════════════════╗
# ║ CRITICAL: MUST CHANGE TO MAIN WORKTREE BEFORE REMOVING TASK WORKTREE ║
# ║ The command below will FAIL if you are inside the worktree           ║
# ╚═══════════════════════════════════════════════════════════════════════╝

# Change to main worktree (fails if already inside task worktree being removed)
[[ "$(pwd)" == "/workspace/branches/{TASK_NAME}/code"* ]] && { echo "❌ FATAL: Cannot remove worktree while inside it (pwd=$(pwd))"; exit 1; } || cd /workspace/branches/main/code

# Remove worktree and branch
git worktree remove /workspace/branches/{TASK_NAME}/code --force
git branch -d {TASK_NAME}
rm -rf /workspace/branches/{TASK_NAME}

# Temporary file cleanup
TEMP_DIR=$(cat .temp_dir 2>/dev/null) && [ -n "$TEMP_DIR" ] && rm -rf "$TEMP_DIR"
```

**Example for task "refactor-line-wrapping-architecture"**:
```bash
# Verify work in main branch (from task worktree)
cd /workspace/branches/refactor-line-wrapping-architecture/code
git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5 | grep "refactor-line-wrapping-architecture" || {
    echo "❌ FATAL: Work not found in main branch"
    exit 1
}

# Clean up task resources with session verification
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/locks/refactor-line-wrapping-architecture.json")
if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "❌ FATAL: Cannot delete lock owned by $LOCK_OWNER"
  exit 1
fi

rm -f /workspace/locks/refactor-line-wrapping-architecture.json

# Change to main worktree (fails if already inside task worktree being removed)
[[ "$(pwd)" == "/workspace/branches/refactor-line-wrapping-architecture/code"* ]] && { echo "❌ FATAL: Cannot remove worktree while inside it (pwd=$(pwd))"; exit 1; } || cd /workspace/branches/main/code

# Remove worktree and branch
git worktree remove /workspace/branches/refactor-line-wrapping-architecture/code --force
git branch -d refactor-line-wrapping-architecture
rm -rf /workspace/branches/refactor-line-wrapping-architecture
```

## TRANSITION VALIDATION FUNCTIONS

### Universal Validation Requirements
Every transition MUST execute these checks:

```python
def validate_transition_conditions(from_state, to_state, evidence, task_context):
    """Validate that all mandatory conditions for transition are met"""

    # Load transition requirements
    required_conditions = TRANSITION_MAP[from_state][to_state]['conditions']
    required_evidence = TRANSITION_MAP[from_state][to_state]['evidence']

    # Validate each condition
    for condition in required_conditions:
        if not validate_condition(condition, evidence, task_context):
            return False, f"Condition failed: {condition}"

    # Validate required evidence exists
    for evidence_type in required_evidence:
        if not validate_evidence(evidence_type, evidence):
            return False, f"Missing evidence: {evidence_type}"

    return True, "All transition conditions satisfied"

def validate_condition(condition, evidence, context):
    """Validate specific transition condition"""
    # Implementation specific to each condition type
    pass

def validate_evidence(evidence_type, evidence_data):
    """Validate required evidence exists and is complete"""
    # Implementation specific to each evidence type
    pass
```

### State Enforcement Functions

```bash
# Function: Update task state
update_task_state() {
    local new_state=$1
    local evidence_json=$2

    # Validate transition is legal
    current_state=$(jq -r '.current_state' state.json 2>/dev/null || echo "INIT")
    if ! validate_state_transition "$current_state" "$new_state"; then
        echo "ERROR: Invalid transition from $current_state to $new_state"
        exit 1
    fi

    # Update state file
    jq --arg state "$new_state" \
       --argjson evidence "$evidence_json" \
       --arg timestamp "$(date -Iseconds)" \
       '.current_state = $state |
        .evidence += $evidence |
        .transition_log += [{"from": .current_state, "to": $state, "timestamp": $timestamp}]' \
       state.json > state.json.tmp && mv state.json.tmp state.json
}

# Function: Validate state transition
validate_state_transition() {
    local from_state=$1
    local to_state=$2

    case "$from_state:$to_state" in
        # Standard transitions
        "INIT:CLASSIFIED"|"CLASSIFIED:REQUIREMENTS"|"REQUIREMENTS:SYNTHESIS"|"SYNTHESIS:IMPLEMENTATION"|"IMPLEMENTATION:VALIDATION"|"VALIDATION:REVIEW"|"REVIEW:COMPLETE"|"COMPLETE:CLEANUP")
            return 0 ;;
        # Scope negotiation transitions
        "REVIEW:SCOPE_NEGOTIATION"|"SCOPE_NEGOTIATION:SYNTHESIS"|"SCOPE_NEGOTIATION:COMPLETE")
            return 0 ;;
        # Conditional skip transitions for low/medium risk
        "SYNTHESIS:COMPLETE"|"CLASSIFIED:COMPLETE"|"REQUIREMENTS:COMPLETE")
            return 0 ;;
        # Resolution cycle transitions
        "SYNTHESIS:IMPLEMENTATION"|"IMPLEMENTATION:SYNTHESIS"|"VALIDATION:SYNTHESIS")
            return 0 ;;
        *)
            return 1 ;;
    esac
}
```

## AGENT INTERACTION PROTOCOLS

### Parallel Agent Invocation Pattern
```python
# Template for requirements gathering
agent_prompt_template = """
Task: {task_description}
Mode: REQUIREMENTS_ANALYSIS
Current State: REQUIREMENTS → SYNTHESIS transition pending
Your Domain: {agent_domain}
Risk Level: {risk_level}

CRITICAL SCOPE ENFORCEMENT:
Only analyze files in ../context.md scope section.
STOP IMMEDIATELY if attempting unauthorized file access.

MANDATORY PERSISTENCE REQUIREMENTS:
Apply CLAUDE.md "Long-term Solution Persistence" principles.
Reject incomplete solutions when optimal solutions are achievable.
Use "Solution Quality Hierarchy" - prioritize OPTIMAL over EXPEDIENT.

MANDATORY OUTPUT REQUIREMENT:
Provide complete {agent_domain} requirements analysis.
End with: "REQUIREMENTS COMPLETE: {agent_domain} analysis provided"

MANDATORY EVIDENCE REQUIREMENT:
Document specific {agent_domain} requirements that implementation must satisfy.
Include success criteria, constraints, and quality thresholds.
"""

# Implementation
def invoke_requirements_agents(required_agents, task_context):
    agent_calls = []
    for agent in required_agents:
        agent_calls.append({
            'subagent_type': agent,
            'description': f'{agent} requirements analysis',
            'prompt': agent_prompt_template.format(
                agent_domain=agent.replace('-', ' '),
                **task_context
            )
        })

    # Execute all agents in parallel (single message, multiple tool calls)
    return execute_parallel_agents(agent_calls)
```

### Review Agent Invocation Pattern
```python
review_prompt_template = """
Task: {task_description}
Mode: FINAL_SYSTEM_REVIEW
Current State: REVIEW → COMPLETE transition pending
Your Phase 1 Requirements: {requirements_file}
Implementation Files: {modified_files}

CRITICAL PERSISTENCE VALIDATION:
Apply CLAUDE.md "Solution Quality Indicators" checklist.
Verify solution addresses root cause, follows best practices.
Reject if merely functional when optimal was achievable within scope.

MANDATORY FINAL DECISION FORMAT:
Must end response with exactly one of:
- "FINAL DECISION: ✅ APPROVED - All {domain} requirements satisfied"
- "FINAL DECISION: ❌ REJECTED - [specific issues requiring resolution]"

NO OTHER CONCLUSION FORMAT ACCEPTED.
This decision determines workflow continuation.
"""
```

## ERROR HANDLING & RECOVERY

### Transition Failure Recovery
```python
def handle_transition_failure(current_state, attempted_state, failure_reason):
    """Handle failed state transitions with appropriate recovery"""

    recovery_actions = {
        'REQUIREMENTS': {
            'missing_agent_reports': 're_invoke_missing_agents',
            'incomplete_analysis': 'request_complete_analysis',
            'scope_violations': 'enforce_scope_boundaries'
        },
        'REVIEW': {
            'agent_rejection': 'return_to_synthesis_cycle',
            'malformed_decision': 'request_proper_format',
            'missing_decisions': 're_invoke_agents'
        },
        'VALIDATION': {
            'build_failure': 'return_to_implementation',
            'test_failure': 'fix_tests_and_retry',
            'quality_gate_failure': 'address_quality_issues'
        }
    }

    action = recovery_actions.get(current_state, {}).get(failure_reason, 'restart_from_synthesis')
    execute_recovery_action(action, current_state, attempted_state, failure_reason)
```

### Multi-Instance Coordination
```bash
# Lock conflict resolution
handle_lock_conflict() {
    local task_name=$1

    echo "Lock conflict detected for task: $task_name"
    echo "Checking for available alternative tasks..."

    # Find next available task
    available_tasks=$(grep -E "^[[:digit:]]+\." /workspace/branches/main/code/todo.md |
                     head -10 |
                     while read line; do
                         task=$(echo "$line" | sed 's/^[[:digit:]]*\. *//')
                         task_slug=$(echo "$task" | tr ' ' '-' | tr '[:upper:]' '[:lower:]')
                         if [ ! -f "../../../locks/${task_slug}.json" ]; then
                             echo "$task_slug"
                             break
                         fi
                     done)

    if [ -n "$available_tasks" ]; then
        echo "Selected alternative task: $available_tasks"
        execute_task_protocol "$available_tasks"
    else
        echo "No available tasks found. Exiting."
        exit 1
    fi
}
```

## COMPLIANCE VERIFICATION

### Pre-Task Validation Checklist
```bash
# MANDATORY before ANY task execution
pre_task_validation() {
    echo "=== PRE-TASK VALIDATION ==="

    # 1. Session ID validation
    [ -n "$SESSION_ID" ] && [ "$SESSION_ID" != "REPLACE_WITH_ACTUAL_SESSION_ID" ] || {
        echo "ERROR: Invalid session ID configuration"
        exit 1
    }

    # 2. Working directory verification
    [ "$(pwd)" = "/workspace/branches/main/code" ] || {
        echo "ERROR: Must execute from main branch directory"
        exit 1
    }

    # 3. Todo.md accessibility
    [ -f "todo.md" ] || {
        echo "ERROR: todo.md not accessible"
        exit 1
    }

    echo "✅ Pre-task validation complete"
}
```

### Continuous Compliance Monitoring
```bash
# Execute after each state transition
post_transition_validation() {
    local new_state=$1

    echo "=== POST-TRANSITION VALIDATION: $new_state ==="

    # Verify state file updated correctly
    current_state=$(jq -r '.current_state' state.json 2>/dev/null || echo "ERROR")
    [ "$current_state" = "$new_state" ] || {
        echo "ERROR: State file not updated correctly"
        exit 1
    }

    # Verify required evidence present
    validate_state_evidence "$new_state" || {
        echo "ERROR: Required evidence missing for state $new_state"
        exit 1
    }

    echo "✅ Post-transition validation complete"
}
```

### Final Compliance Audit
```bash
# MANDATORY before CLEANUP state
final_compliance_audit() {
    echo "=== FINAL COMPLIANCE AUDIT ==="

    # Verify all states traversed correctly
    state_sequence=$(jq -r '.transition_log[] | .to' state.json | tr '\n' ' ')

    # Valid sequences: support multiple workflow variants
    high_risk_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS IMPLEMENTATION VALIDATION REVIEW COMPLETE"
    medium_risk_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS IMPLEMENTATION VALIDATION REVIEW COMPLETE"
    low_risk_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS COMPLETE"
    scope_negotiation_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS IMPLEMENTATION VALIDATION REVIEW SCOPE_NEGOTIATION COMPLETE"
    alternate_scope_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS IMPLEMENTATION VALIDATION REVIEW SCOPE_NEGOTIATION SYNTHESIS.*COMPLETE"
    config_only_sequence="CLASSIFIED REQUIREMENTS SYNTHESIS COMPLETE"

    # Check against all valid sequences
    valid_sequence=false
    for sequence in "$high_risk_sequence" "$medium_risk_sequence" "$low_risk_sequence" "$scope_negotiation_sequence" "$config_only_sequence"; do
        if [[ "$state_sequence" =~ $sequence ]]; then
            valid_sequence=true
            echo "✅ Valid state sequence detected: matches $sequence pattern"
            break
        fi
    done

    # Also check alternate scope negotiation pattern
    if [[ "$valid_sequence" == "false" ]] && [[ "$state_sequence" =~ $alternate_scope_sequence ]]; then
        valid_sequence=true
        echo "✅ Valid state sequence detected: matches scope negotiation with resolution cycle"
    fi

    if [[ "$valid_sequence" == "false" ]]; then
        echo "ERROR: Invalid state sequence: $state_sequence"
        exit 1
    fi

    # Verify unanimous approval achieved
    approval_count=$(jq -r '.evidence.REVIEW | to_entries[] | select(.value == "APPROVED") | .key' state.json | wc -l)
    required_count=$(jq -r '.required_agents | length' state.json)

    [ "$approval_count" -eq "$required_count" ] || {
        echo "ERROR: Missing agent approvals: $approval_count/$required_count"
        exit 1
    }

    # Verify work preserved
    git log --oneline -5 | grep -i "$(jq -r '.task_name' state.json)" || {
        echo "ERROR: Task work not preserved in git history"
        exit 1
    }

    echo "✅ Final compliance audit passed"
}
```

## VIOLATION PREVENTION PATTERNS

### Pre-Task Validation Block
**MANDATORY before ANY task execution:**
```bash
# Session ID validation
export SESSION_ID="f33c1f04-94a5-4e87-9a87-4fcbc57bc8ec" && [ -n "$SESSION_ID" ] && [ "$SESSION_ID" != "REPLACE_WITH_ACTUAL_SESSION_ID" ] && echo "SESSION_ID_VALID: $SESSION_ID" || (echo "SESSION_ID_INVALID" && exit 1)

# Working directory verification
[ "$(pwd)" = "/workspace/branches/main/code" ] || (echo "ERROR: Wrong directory" && exit 1)

# Todo.md accessibility
[ -f "todo.md" ] || (echo "ERROR: todo.md not accessible" && exit 1)
```

### Mandatory Context.md Creation
**Required BEFORE any stakeholder consultation:**
```markdown
# Task Context: {task-name}

## Task Objective
{task-description}

## Scope Definition
**FILES IN SCOPE:**
- [List exact files/directories that stakeholder agents are authorized to analyze]

**FILES OUT OF SCOPE:**
- [List directories/files explicitly excluded from analysis]

## Stakeholder Agent Reports
**Requirements Phase:**
- technical-architect-requirements.md (when completed)
- [other-agent]-requirements.md (when completed)

**Implementation Reviews:**
- technical-architect-review1.md (when completed)
- [other-agent]-review1.md (when completed)

## Implementation Status
- [ ] INIT: Task initialization
- [ ] CLASSIFIED: Risk assessment
- [ ] REQUIREMENTS: Stakeholder analysis
- [ ] SYNTHESIS: Architecture planning
- [ ] IMPLEMENTATION: Code creation
- [ ] VALIDATION: Build verification
- [ ] REVIEW: Final approval
- [ ] COMPLETE: Work preservation
- [ ] CLEANUP: Resource cleanup
```

### Atomic Lock Acquisition Pattern
```bash
# MANDATORY atomic lock acquisition
export SESSION_ID="f33c1f04-94a5-4e87-9a87-4fcbc57bc8ec" && mkdir -p ../../../locks && (set -C; echo '{"session_id": "'${SESSION_ID}'", "start_time": "'$(date '+%Y-%m-%d %H:%M:%S %Z')'"}' > ../../../locks/{task-name}.json) 2>/dev/null && echo "LOCK_SUCCESS" || echo "LOCK_FAILED"

# Violation check
if [[ "$lock_result" != *"LOCK_SUCCESS"* ]]; then
    echo "Lock acquisition failed - selecting alternative task"
    select_alternative_task
fi
```

### Temporary File Management Setup
**BEFORE IMPLEMENTATION BEGINS (Mandatory for all tasks):**
```bash
# TEMP_DIRECTORY_CREATION: Set up isolated temporary file space
TASK_NAME=$(basename $(dirname $(pwd)))
TEMP_DIR="/tmp/task-${TASK_NAME}-$(date +%s)-$$" && mkdir -p "$TEMP_DIR"
echo "$TEMP_DIR" > .temp_dir && echo "TEMP_SETUP_SUCCESS: $TEMP_DIR"

# PURPOSE: All agents use consistent temporary file location outside git repository
# USAGE: Agents read temp directory with: TEMP_DIR=$(cat .temp_dir 2>/dev/null || echo "/tmp/fallback-$$")

# VIOLATION CHECK: Output must contain "TEMP_SETUP_SUCCESS"
if [[ "$temp_result" != *"TEMP_SETUP_SUCCESS"* ]]; then
    echo "WARNING: Temp directory creation failed - using task directory as fallback"
    TEMP_DIR=$(pwd)/.temp_fallback && mkdir -p "$TEMP_DIR"
fi
```

**AGENT INTEGRATION**: All agents should use temporary directory for:
- Analysis scripts and automation tools
- Performance benchmarking artifacts
- Security testing payloads and samples
- Debug logs and intermediate processing files
- Generated test data and mock objects

### Implementation Safety Guards
**Before ANY Write/Edit/MultiEdit operation:**
```bash
# Working directory validation
pwd | grep -q "/workspace/branches/.*/code$" || (echo "ERROR: Invalid working directory" && exit 1)

# Clean repository state
git status --porcelain | grep -E "(dist/|node_modules/|target/|\.jar$)" && (echo "ERROR: Prohibited files detected" && exit 1)
```

### Build Validation Gates
**Mandatory after implementation:**
```bash
# Full verification (build + tests + linters) before completion
./mvnw verify -q || (echo "ERROR: Build/test/linter failure - task cannot be completed" && exit 1)

# Note: 'mvnw verify' executes: compile → test → checkstyle → PMD → all quality gates
```

### Decision Parsing Enforcement
**After ANY agent invocation:**
```bash
# Extract agent decision
decision=$(echo "$agent_response" | grep "FINAL DECISION:")

if [[ -z "$decision" ]]; then
    # Retry with format clarification
    retry_agent_with_format_requirement
elif [[ "$decision" == *"❌ REJECTED"* ]]; then
    # Handle rejection appropriately
    handle_agent_rejection
elif [[ "$decision" == *"✅ APPROVED"* ]]; then
    # Continue workflow
    continue_to_next_transition
fi
```

### Protocol Violation Reporting Pattern
```
MAINTAIN THROUGHOUT TASK EXECUTION:
1. Track all format violations in TodoWrite tool as separate item
2. Record: Agent type, state, violation type, retry outcome
3. Include in final task summary for human review

VIOLATION REPORT FORMAT:
"Protocol Violations Detected:
- [Agent]: [State] - Missing decision format (resolved via retry)
- [Agent]: [State] - Malformed decision (assumed REJECTED)
- Total violations: X, Auto-resolved: Y, Manual review needed: Z"

PURPOSE:
- Identify agent prompt issues needing refinement
- Track protocol compliance trends over time
- Enable proactive protocol improvements
- Maintain audit trail for debugging

IMPLEMENTATION:
violation_tracking = {
    "agent_name": {
        "state": "REVIEW",
        "violation_type": "missing_decision_format",
        "retry_outcome": "resolved",
        "timestamp": "2024-01-01T12:00:00Z"
    }
}

# Add to TodoWrite tracking
update_todo_with_violation_report()
```

## CONTEXT PRESERVATION RULES

### Single Session Continuity
**Requirements:**
- All task execution in single Claude session
- NO HANDOFFS: Complete task without waiting for user input
- IF session interrupted: Restart task from beginning with new session

### Tool Call Batching
**Optimization patterns:**
```python
# Batch related operations in single message
parallel_tool_calls = [
    {"tool": "Task", "agent": "technical-architect", "mode": "requirements"},
    {"tool": "Task", "agent": "style-auditor", "mode": "requirements"},
    {"tool": "Task", "agent": "code-quality-auditor", "mode": "requirements"}
]

# Execute all in single message to reduce context fragmentation
execute_parallel_tools(parallel_tool_calls)
```

### State Persistence Patterns
**Critical state tracking:**
- Current state in state.json file
- Session ID in environment variables
- Working directory verification
- Lock status confirmation

**Verify state before each major operation:**
```bash
# State consistency check
current_state=$(jq -r '.current_state' state.json)
expected_state="$1"
[ "$current_state" = "$expected_state" ] || (echo "ERROR: State inconsistency" && exit 1)
```

## TOOL-SPECIFIC OPTIMIZATION PATTERNS

### Bash Tool Usage
```bash
# Combine related operations with safety checks
command1 && echo "SUCCESS" || (echo "FAILED" && exit 1)

# Verify outputs before proceeding
result=$(command_with_output)
[ -n "$result" ] || (echo "ERROR: Command produced no output" && exit 1)

# Avoid interactive commands
# ❌ git rebase -i
# ✅ git rebase main
```

### Read Tool Usage
```python
# Batch reads for related content
related_files = [
    "/path/to/config.md",
    "/path/to/implementation.java",
    "/path/to/test.java"
]

# Read multiple files in sequence within single operation
for file_path in related_files:
    content = read_tool(file_path)
    analyze_content(content)
```

### Task Tool Usage
```python
# Parallel agent calls in single message
agent_calls = [
    {
        'subagent_type': 'technical-architect',
        'description': 'Architecture requirements',
        'prompt': structured_prompt_template.format(domain='architecture')
    },
    {
        'subagent_type': 'style-auditor',
        'description': 'Style requirements',
        'prompt': structured_prompt_template.format(domain='style')
    }
]

# Execute all agents simultaneously
execute_parallel_agents(agent_calls)
```

### TodoWrite Tool Usage
```python
# Frequent progress updates
update_todo_progress("requirements", "in_progress", "Gathering stakeholder requirements")

# Consistent format patterns
todo_item = {
    "content": "Implement feature X with requirements Y",
    "status": "in_progress",
    "activeForm": "Implementing feature X according to stakeholder specifications"
}

# State verification through TodoWrite
verify_workflow_position_via_todo_state()
```

## ERROR RECOVERY PROTOCOLS

### Violation Detection Triggers
**Automatic violation detection:**
- Wrong directory (pwd check fails)
- Missing locks (lock file check fails)
- Build failures (non-zero exit codes)
- Git operation failures (rebase/merge fails)
- Prohibited files detected (status check fails)

**Recovery Action**: TERMINATE current task, restart from INIT state

### Multi-Instance Conflict Resolution
**Lock conflict handling:**
```bash
handle_lock_conflict() {
    local task_name=$1
    echo "Lock conflict detected for task: $task_name"

    # Find alternative available task
    available_task=$(find_next_available_task)

    if [ -n "$available_task" ]; then
        echo "Selected alternative task: $available_task"
        execute_task_protocol "$available_task"
    else
        echo "No available tasks found. Exiting."
        exit 1
    fi
}
```

### Partial Completion Recovery
**Task interruption handling:**
```bash
if [ -f "state.json" ]; then
    session_id=$(jq -r '.session_id' state.json)
    current_session="$SESSION_ID"

    if [ "$session_id" = "$current_session" ]; then
        # Resume from last completed state
        resume_from_last_state
    else
        # Foreign session - select different task
        select_alternative_task
    fi
fi
```

## WORKFLOW EXECUTION ENGINE

### Main Task Execution Function
```bash
execute_task_protocol() {
    local task_name=$1

    # Initialize state machine
    initialize_task_state "$task_name"

    # Execute state machine with risk-based path selection
    while [ "$(get_current_state)" != "CLEANUP" ]; do
        current_state=$(get_current_state)
        risk_level=$(jq -r '.risk_level' state.json 2>/dev/null || echo "HIGH")
        change_type=$(determine_change_type)

        echo "=== STATE: $current_state (Risk: $risk_level, Type: $change_type) ==="

        case "$current_state" in
            "INIT")
                execute_init_to_classified_transition
                ;;
            "CLASSIFIED")
                execute_classified_to_requirements_transition
                ;;
            "REQUIREMENTS")
                execute_requirements_to_synthesis_transition
                ;;
            "SYNTHESIS")
                # Conditional path selection based on risk level and change type
                if should_skip_implementation "$risk_level" "$change_type"; then
                    execute_synthesis_to_complete_transition
                else
                    execute_synthesis_to_implementation_transition
                fi
                ;;
            "IMPLEMENTATION")
                execute_implementation_to_validation_transition
                ;;
            "VALIDATION")
                execute_validation_to_review_transition
                ;;
            "REVIEW")
                execute_review_to_complete_or_scope_negotiation_transition
                ;;
            "SCOPE_NEGOTIATION")
                execute_scope_negotiation_to_synthesis_or_complete_transition
                ;;
            "COMPLETE")
                execute_complete_to_cleanup_transition
                ;;
            *)
                echo "ERROR: Unknown state: $current_state"
                exit 1
                ;;
        esac

        # Post-transition validation
        post_transition_validation "$(get_current_state)"
    done

# Helper function to determine if implementation should be skipped
should_skip_implementation() {
    local risk_level=$1
    local change_type=$2

    case "$risk_level" in
        "HIGH")
            return 1  # Never skip for high risk
            ;;
        "MEDIUM")
            if [[ "$change_type" == "documentation_only" || "$change_type" == "config_only" ]]; then
                return 0  # Skip for medium risk documentation/config changes
            else
                return 1  # Don't skip for other medium risk changes
            fi
            ;;
        "LOW")
            return 0  # Always skip for low risk
            ;;
        *)
            return 1  # Default to not skip if uncertain
            ;;
    esac
}

    # Final audit
    final_compliance_audit
    echo "=== TASK PROTOCOL COMPLETED SUCCESSFULLY ==="
}
```

## MIGRATION FROM PHASE-BASED PROTOCOL

### Compatibility Layer
All existing CLAUDE.md references to "phases" map to states as follows:
- Phase 1 = REQUIREMENTS state
- Phase 2 = SYNTHESIS state
- Phase 3 = IMPLEMENTATION state
- Phase 4 = Part of VALIDATION state
- Phase 5 = Resolution cycle (SYNTHESIS ↔ IMPLEMENTATION ↔ VALIDATION)
- Phase 6 = REVIEW state
- Phase 7 = CLEANUP state

### Transition Period Support
During migration, both terminologies are recognized:
- "Execute Phase 1" → "Transition to REQUIREMENTS state"
- "Phase 6 rejection" → "REVIEW state rejection, return to SYNTHESIS"
- "Phase guards" → "Transition conditions"

### Zero Tolerance Enforcement
Unlike the previous phase-based system with optional "phase guards," this state machine implements **mandatory transition conditions**. There are no manual overrides, no "reasonable approximation" exceptions, and no human discretion in bypassing required validations.

**END OF STATE MACHINE PROTOCOL**

