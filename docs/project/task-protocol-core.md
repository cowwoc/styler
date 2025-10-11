# Task State Machine Protocol

**CRITICAL**: This protocol applies to ALL file operations using MANDATORY STATE TRANSITIONS with zero-tolerance enforcement.

**State machine: atomic transitions, verifiable conditions, no manual overrides.**

## STATE MACHINE ARCHITECTURE

### Core States
```
INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → [PLAN APPROVAL] → CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE → VALIDATION → REVIEW → AWAITING_USER_APPROVAL → COMPLETE → CLEANUP
                                                                                                              ↑                                    ↓
                                                                                                              └────────── SCOPE_NEGOTIATION ←──────┘
```

**User Approval Checkpoints:**
- **[PLAN APPROVAL]**: After SYNTHESIS, before CONTEXT - User reviews and approves implementation plan
- **[CHANGE REVIEW]**: After REVIEW (unanimous stakeholder approval) - Transition to AWAITING_USER_APPROVAL state

### State Definitions
- **INIT**: Task selected, locks acquired, session validated
- **CLASSIFIED**: Risk level determined, agents selected, isolation established
- **REQUIREMENTS**: All stakeholder requirements collected and validated
- **SYNTHESIS**: Requirements consolidated into unified architecture plan, **USER APPROVAL CHECKPOINT: Present plan to user, wait for explicit user approval**
- **CONTEXT**: Generate context.md for autonomous agent implementation
- **AUTONOMOUS_IMPLEMENTATION**: Agents implement changes autonomously in parallel
- **CONVERGENCE**: Iterative integration until unanimous approval
- **VALIDATION**: Build verification and automated quality gates passed
- **REVIEW**: All stakeholder agents provide unanimous approval
- **AWAITING_USER_APPROVAL**: **CHECKPOINT STATE - Implementation commit created, changes presented to user, waiting for explicit approval before COMPLETE**
- **SCOPE_NEGOTIATION**: Determine what work can be deferred when agents reject due to scope concerns (ONLY when resolution effort > 2x task scope AND agent consensus permits deferral - NEVER ask user for permission)
- **COMPLETE**: Work preserved to main branch, todo.md updated (only after user approves changes)
- **CLEANUP**: Worktrees removed, locks released, temporary files cleaned

### User Approval Checkpoints - MANDATORY REGARDLESS OF BYPASS MODE

**🚨 BYPASS MODE DOES NOT BYPASS USER APPROVAL CHECKPOINTS**

**Checkpoint 1: [PLAN APPROVAL] - After SYNTHESIS, Before CONTEXT**
- Present implementation plan in clear format
- Wait for explicit approval message
- PROHIBITED: Assuming approval from bypass mode, lack of response, or proceeding without "yes"/"approved"/"proceed"

**Checkpoint 2: [CHANGE REVIEW] - After REVIEW, Before COMPLETE**
- Present completed changes with commit SHA
- Wait for explicit review approval
- PROHIBITED: Assuming approval from agent consensus alone or proceeding without clear confirmation

**Verification Before Proceeding:**
SYNTHESIS → CONTEXT: Presented complete plan? User explicitly approved? Assumed approval from bypass? (VIOLATION if yes)
REVIEW → COMPLETE: Presented changes with SHA? User explicitly approved finalization? Assumed approval from agents alone? (VIOLATION if yes)

### Automated Checkpoint Enforcement

**HOOK**: `enforce-user-approval.sh` blocks COMPLETE without user approval

**Approval Marker**: `/workspace/branches/{task-name}/user-approval-obtained.flag` (created on approval, required for COMPLETE, removed during CLEANUP)

**Approval Keywords**: "yes", "approved", "approve", "proceed", "looks good", "LGTM" + review context

**Hook Process**: Monitor UserPromptSubmit events → Check marker during REVIEW → Block if missing and no explicit approval → Create marker on approval, allow COMPLETE

### State Transitions
Each transition requires **ALL** specified conditions to be met. **NO EXCEPTIONS.**

## RISK-BASED AGENT SELECTION ENGINE

**Risk Classification**: File patterns → Escalation triggers → Risk level (HIGH/MEDIUM/LOW) + Agent set

### HIGH-RISK FILES (Complete Validation Required)
**Patterns**: `src/**/*.java`, `pom.xml`, `**/pom.xml`, `.github/**`, `**/security/**`, `checkstyle.xml`, `**/checkstyle*.xml`, `CLAUDE.md`, `docs/project/task-protocol.md`, `docs/project/critical-rules.md`

**Required**: technical-architect, style-auditor, code-quality-auditor, build-validator
**Additional**: security-auditor (security-related), performance-analyzer (performance-critical), code-tester (new functionality), usability-reviewer (user-facing)

### MEDIUM-RISK FILES (Domain Validation Required)
**Patterns**: `src/test/**/*.java`, `docs/code-style/**`, `**/resources/**/*.properties`, `**/*Test.java`, `**/*Tests.java`

**Required**: technical-architect, code-quality-auditor
**Additional**: style-auditor (style files), security-auditor (config files), performance-analyzer (benchmarks)

### LOW-RISK FILES (Minimal Validation Required)
**Patterns**: `*.md` (except CLAUDE.md, task-protocol.md, critical-rules.md), `docs/**/*.md`, `todo.md`, `*.txt`, `*.log`, `**/README*`

**Required**: None (unless escalation triggered)

### Escalation Triggers
**Keywords**: "security", "architecture", "breaking", "performance", "concurrent", "database", "api", "state", "dependency" → Escalate to next higher risk level

### Manual Overrides
`--force-full-protocol` (critical changes), `--risk-level=HIGH|MEDIUM|LOW` (override classification), Escalation keywords in task description

### Risk Assessment Audit Trail
Log in state.json: Risk level, classification method, escalation triggers, workflow variant, agent set, outcome

## WORKFLOW VARIANTS BY RISK LEVEL

### HIGH_RISK_WORKFLOW (Complete Validation)
**States**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE → VALIDATION → REVIEW → COMPLETE → CLEANUP
**Agents**: All based on task requirements | **Isolation**: Mandatory worktree | **Review**: Complete stakeholder validation
**Use Case**: Core implementation, build configuration, security, CI/CD | **Skips**: None (all validation required)

### MEDIUM_RISK_WORKFLOW (Domain Validation)
**States**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE → VALIDATION → REVIEW → COMPLETE → CLEANUP
**Agents**: technical-architect (always), +style-auditor (style/formatting files), +security-auditor (config/resource files), +performance-analyzer (test performance/benchmarks), +code-quality-auditor (always)
**Isolation**: Worktree for multi-file changes | **Review**: Domain-appropriate stakeholder validation
**Use Case**: Test files, style documentation, configuration files | **Skips**: May skip CONTEXT/AUTONOMOUS_IMPLEMENTATION/CONVERGENCE/VALIDATION if documentation-only

### LOW_RISK_WORKFLOW (Streamlined Validation)
**States**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → COMPLETE → CLEANUP
**Agents**: None (unless escalation) | **Isolation**: Required for multi-file, optional for single doc | **Review**: Evidence-based validation, automated checks
**Safety Gates**: Verify no src/ cross-references, no build config impact, no security-sensitive content changes
**Use Case**: Documentation updates, todo.md, README files | **Skips**: CONTEXT, AUTONOMOUS_IMPLEMENTATION, CONVERGENCE, VALIDATION, REVIEW (all skipped)

### Conditional State Transition Logic
```python
def determine_state_path(risk_level, change_type):
    """Determine which states to execute based on risk and change type"""

    base_states = ["INIT", "CLASSIFIED", "REQUIREMENTS", "SYNTHESIS"]

    if risk_level == "HIGH":
        return base_states + ["CONTEXT", "AUTONOMOUS_IMPLEMENTATION", "CONVERGENCE", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]

    elif risk_level == "MEDIUM":
        if change_type in ["documentation_only", "config_only"]:
            return base_states + ["COMPLETE", "CLEANUP"]
        else:
            return base_states + ["CONTEXT", "AUTONOMOUS_IMPLEMENTATION", "CONVERGENCE", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]

    elif risk_level == "LOW":
        return base_states + ["COMPLETE", "CLEANUP"]

    else:
        # Default to HIGH risk if uncertain
        return base_states + ["CONTEXT", "AUTONOMOUS_IMPLEMENTATION", "CONVERGENCE", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]
```

### Skip Condition Examples
**Skip CONTEXT/AUTONOMOUS_IMPLEMENTATION/CONVERGENCE/VALIDATION**: Maven dependency additions, Build plugin config changes, Documentation updates (no code references), Property file modifications, Version bumps (no code changes), README/markdown updates, todo.md tracking updates

**Require Full Validation**: Source code modifications (*.java, *.js, *.py), Runtime behavior changes, Security-sensitive config changes, Build system modifications (affecting compilation), API contract modifications

## AGENT SELECTION DECISION TREE

### Comprehensive Agent Selection Framework
**Input**: Task description + file modification patterns
**Available**: technical-architect, usability-reviewer, performance-analyzer, security-auditor, style-auditor, code-quality-auditor, code-tester, build-validator

**🚨 CORE**: technical-architect (MANDATORY for ALL file modifications)

**🔍 FUNCTIONAL** (Code Implementation): NEW CODE → add style-auditor, code-quality-auditor, build-validator | CODE CHANGES (not config) → add code-tester | MAJOR FEATURES completed → add usability-reviewer (MANDATORY)

**🛡️ SECURITY** (Actual Security Concerns): AUTH changes, EXTERNAL API/DATA integration, ENCRYPTION/CRYPTO operations, INPUT VALIDATION/SANITIZATION → add security-auditor

**⚡ PERFORMANCE** (Performance Critical): ALGORITHM optimization, DATABASE/QUERY optimization, MEMORY/CPU intensive operations → add performance-analyzer

**🔧 FORMATTING** (Code Quality): PARSER LOGIC → add performance-analyzer, security-auditor | AST TRANSFORMATION → add code-quality-auditor, code-tester | FORMATTING RULES → add style-auditor

**❌ NOT NEEDED**: Maven module renames (NO performance-analyzer), Config file updates (NO security-auditor unless auth), Directory/file renames (NO performance-analyzer), Documentation updates (usually only technical-architect)

**📊 ANALYSIS** (Research/Study): ARCHITECTURAL → technical-architect | PERFORMANCE → performance-analyzer | UX/INTERFACE → usability-reviewer | SECURITY → security-auditor | CODE QUALITY → code-quality-auditor | PARSER/FORMATTER PERFORMANCE → performance-analyzer

**Verification Checklist**: NEW CODE → style-auditor? | Source files created/modified → build-validator? | Performance-critical → performance-analyzer? | Security-sensitive → security-auditor? | User-facing → usability-reviewer? | Post-implementation refactoring → code-quality-auditor? | AST parsing/formatting → performance-analyzer?

**Special Usage Patterns**: style-auditor (apply ALL manual rules from docs/code-style/), build-validator (triggers linters for style/formatting, use with style-auditor for comprehensive validation), code-quality-auditor (post-implementation refactoring, best practices), code-tester (business logic validation, comprehensive tests), security-auditor (data handling/storage compliance), performance-analyzer (algorithmic efficiency, resource optimization), usability-reviewer (UX design, interface evaluation), technical-architect (system architecture, implementation guidance)

## COMPLETE STYLE VALIDATION FRAMEWORK

### Three-Component Style Validation
**When style validation required, ALL THREE must pass:**

1. **Automated Linters** (build-validator): checkstyle (Java conventions/formatting), PMD (code quality/best practices), ESLint (JavaScript/TypeScript if applicable)

2. **Manual Style Rules** (style-auditor): Apply ALL detection patterns from `docs/code-style/*-claude.md` (Java-specific, Common patterns, Language-specific)

3. **Build Integration** (build-validator): Automated fixing for conflicts (LineLength vs UnderutilizedLines), `checkstyle/fixers` module (AST-based consolidate-then-split), Testing validates fixing logic before application

### Complete Style Validation Gate Pattern
```bash
# NEVER assume checkstyle-only validation
# CRITICAL ERROR: Checking only checkstyle, declaring "no violations" when PMD/manual violations exist

validate_complete_style_compliance() {
    echo "=== COMPLETE STYLE VALIDATION GATE ==="
    ./mvnw checkstyle:check || return 1  # Component 1: Automated linters
    ./mvnw pmd:check || return 1
    invoke_style_auditor_with_manual_detection_patterns || return 1  # Component 2: Manual rules
    if detect_style_conflicts; then  # Component 3: Automated fixing if conflicts
        apply_automated_style_fixes || return 1
        validate_complete_style_compliance  # Re-validate
    fi
    echo "✅ Complete: checkstyle + PMD + manual rules"
}
```

### Style Validation Integration Points
**VALIDATION State**: Complete style validation before REVIEW | **build-validator**: Triggers automated linters, reports results | **style-auditor**: Validates manual patterns from docs/code-style/ | **Conflict Resolution**: Automatic AST-based fixes when linter rules conflict | **Evidence**: All three components must pass for ✅ APPROVED

## DELEGATED IMPLEMENTATION PROTOCOL

**Standard Workflow**: Autonomous agent implementation with 67% token reduction. Stakeholder agents implement in parallel, iterate until consensus via file-based diffs.

**Use For**: HIGH/MEDIUM-RISK tasks, multiple stakeholder domains, parallel implementation

**Infrastructure**: `.claude/protocol/` (`generate-context.py`, `convergence.py`, `differential-read.sh`, `incremental-validation.sh`)

### State Flow

**Delegated Protocol (Standard):**
```
SYNTHESIS → CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE → VALIDATION → REVIEW
```

### New State Definitions

**CONTEXT**: Generate context.md for autonomous agents (task requirements from todo.md, agent work assignments, scope boundaries, integration points, file-based communication protocol)

**AUTONOMOUS_IMPLEMENTATION**: Agents implement in parallel, write diffs to files (../agent-type.diff), return metadata only (10-50 tokens)

**CONVERGENCE**: Iterative integration until unanimous approval, selective review (changed files only), max 10 rounds (extendable)

### SYNTHESIS → CONTEXT Transition

**Conditions**: User approval obtained, Task requirements defined in synthesis, Agent assignments determined, Context infrastructure available

**Implementation**:
```bash
python3 .claude/protocol/generate-context.py --task-name ${TASK_NAME} --task-dir /workspace/branches/${TASK_NAME}
test -f ../context.md && echo "✅ CONTEXT state complete"
```

**Context.md Sections**: Task Requirements Summary, Technical Constraints, Security Requirements, Code Quality Standards, File Structure/Expected Changes, Agent Work Assignments, File-Based Communication Protocol

### CONTEXT → AUTONOMOUS_IMPLEMENTATION Transition

**Conditions**: Context.md exists with all sections, Agent assignments defined, Implementation agents ready (technical-architect, security-auditor, code-quality-auditor, performance-analyzer, code-tester), Review-only agents identified (style-auditor, usability-reviewer, build-validator)

**Implementation**: Launch agents in parallel → each reads context.md, implements changes, writes ../agent-type.diff → Return: `{"summary": "...", "files_changed": [...], "diff_file": "...", "diff_size_lines": N}`

**Efficiency**: Diffs in files (not context), metadata only (10-50 tokens), parallel execution (4x speedup)

### AUTONOMOUS_IMPLEMENTATION → CONVERGENCE Transition

**Mandatory Conditions:**
- [ ] ALL implementation agents completed their work
- [ ] ALL agent diffs written to files
- [ ] NO agent implementation failures
- [ ] Ready for integration and review

**Implementation:**
```bash
# Round 1: Initial Integration
python3 .claude/protocol/convergence.py \
  --task-dir /workspace/branches/${TASK_NAME} \
  --integrate \
  --round 1

# Main agent integrates diffs using git apply
# Detects conflicts, resolves or escalates
# Commits integrated state
# Notifies agents of changes
```

### CONVERGENCE Workflow

**Round-Based Iteration (Max 10 rounds):**

**Round 1:**
1. Main agent integrates all agent diffs
2. Detects conflicts (overlapping line ranges)
3. Resolves conflicts or escalates
4. Commits integrated state
5. ALL agents review integrated changes:
   - Receive diffs → reconstruct current state (baseline + diffs)
   - Review for their domain concerns
   - Read specific lines if more context needed
   - Return APPROVED / REVISE / CONFLICT

**Round 2-10:**
1. Main agent applies revision diffs
2. Selective agent review:
   - If your files unchanged → Implicit approval
   - If your files changed → Review modifications
   - Always review other agents' changes for domain issues
3. Iterate until unanimous APPROVED

**Unanimous Approval:**
- ALL agents return APPROVED
- Proceed to VALIDATION state

**Round Limit Exceeded:**
- Escalate to user review checkpoint
- User reviews partial consensus
- If user requests changes → Extend limit by 10 rounds from current
- If user approves → Override unanimous requirement

**User Checkpoint Reset Example:**
```
Round 3: No consensus reached
Main agent: "Escalating to user checkpoint..."
User: "Please address security concern X"
Result: max_rounds = 3 + 10 = 13 (agents have until round 13)
```

### Context Reconstruction Pattern

1. Initial: Agent reads full files → baseline
2. Convergence: Agent receives diffs → reconstructs mentally (baseline + diff)
3. If needed: Read specific lines (not entire file)
4. Re-read full file only if context lost

**Token Efficiency**: 67% reduction (10,600 vs 32,500 tokens) via baseline + diffs

### CONVERGENCE → VALIDATION Transition

**Mandatory Conditions:**
- [ ] Unanimous agent approval achieved
- [ ] All changes integrated to task branch
- [ ] Implementation commit created
- [ ] Ready for build validation

**User Experience (Option A: Summary-Only):**

Console shows summary only, no diff clutter:
```
Phase 5 Round 1 Complete:
  ✅ security-auditor: Implemented validation (120 lines in 2 files)
  ✅ technical-architect: Implemented Token record (150 lines in 2 files)
  ✅ code-quality-auditor: Refactored builder pattern (95 lines in 1 file)
  ✅ code-tester: Added comprehensive tests (300 lines in 2 files)

All changes integrated to working tree.
Run 'git diff' to review changes.
Waiting for agent convergence...
```

**User reviews manually:**
```bash
git diff
git diff --stat
# Review with familiar git tools, syntax highlighting
```

### Process Memory Separation

1. **Python (convergence.py)**: Loads diffs, parses conflicts, outputs metadata
2. **Git**: Applies diffs via `git apply`
3. **Main Agent**: Receives metadata only (10-50 tokens, never sees raw diffs)

### Delegated Protocol Benefits

**Context**: 67% token reduction, diffs in files, metadata only, selective review
**Speed**: 4x parallel, 5-10x selective validation, fewer round trips
**Quality**: Unanimous approval, cross-domain review, iterative convergence
**UX**: Git-based review, syntax highlighting, summary-only console

### Infrastructure Reference

**Documentation**: `.claude/protocol/README.md`
**Scripts**: `generate-context.py`, `convergence.py`, `differential-read.sh`, `incremental-validation.sh`
**Agents**: All 8 support delegated workflows
**Requirements**: Agents return text (not files), main agent consolidates during SYNTHESIS

## BATCH PROCESSING AND CONTINUOUS MODE

### Batch Processing Restrictions
**PROHIBITED**: Processing multiple tasks sequentially without individual protocol, "Work on all Phase 1 tasks" (must select ONE), "Complete these 5 tasks" (each needs separate lock/worktree), Assuming research tasks bypass protocol

**MANDATORY SINGLE-TASK**: Select ONE task from todo.md → Acquire atomic lock for THAT task → Create isolated worktree for THAT task → Execute full protocol for THAT task → Complete CLEANUP before starting another

### Automatic Continuous Mode Translation
**Auto-Translation Protocol**: ACKNOWLEDGE ("I understand...") → AUTO-TRANSLATE ("I'll interpret as continuous mode request...") → EXECUTE (trigger continuous workflow automatically)

**Batch Patterns to Auto-Translate**: "Work on all [phase/type] tasks", "Complete these tasks until done", "Process the todo list", "Work on multiple tasks", Any request mentioning multiple specific tasks

**Task Filtering**: Phase-based (specified phases only), Type-based (matching types only), Name-based (mentioned names only), Default (all available if no filter)

## 🧠 PROTOCOL INTERPRETATION MODE

Apply deeper analysis when interpreting protocol. Verify transition conditions genuinely met, use evidence-based validation, consider edge cases, evaluate "good enough" solutions skeptically.

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
    "CONTEXT": ["context.md"],
    "AUTONOMOUS_IMPLEMENTATION": ["agent-diffs.json"],
    "CONVERGENCE": ["convergence-metadata.json"],
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

**Pre-Task Checklist**: Clear deliverables (Purpose, Scope, Components in todo.md), No external blockers (dependencies available, no missing APIs), Implementation within capabilities (technical approach clear)

**Not Blockers**: Time estimates, complexity, token usage
**Actual Blockers**: Missing external API, undefined requirements, conflicting requirements
**Commitment**: If checks pass, complete States 0-8 autonomously

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

### Prohibited/Required Actions

**PROHIBITED**: Delete/override lock file owned by different session, Assume existing worktree without lock is abandoned, Proceed with INIT if lock exists for different session, Skip lock verification for "continue with next task"

**REQUIRED**: Check `/workspace/locks/{TASK_NAME}.json` before starting, Compare lock session_id with current session_id, Check for existing worktree at `/workspace/branches/{TASK_NAME}`, Select alternative task if lock owned by different session, Ask user if worktree exists without lock

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

**Scenario**: Worktree exists but no lock file

**Action**: Ask user:
```
Found worktree at /workspace/branches/{TASK_NAME} but no lock file. Options:
1. Clean up abandoned worktree and start fresh
2. Resume work in existing worktree
3. Select different task
```

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

**Task locks** (`/workspace/locks/{task-name}.json`): Task worktrees during protocol
**Main lock** (`/workspace/locks/main.json`): Direct operations on main worktree only

**Required**: Acquire main lock before main worktree operations, release after, verify ownership before release

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
  "created_at": "ISO-8601-timestamp"
}
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
LOCK_FILE="/workspace/locks/${TASK_NAME}.json"

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
LOCK_FILE="/workspace/locks/${TASK_NAME}.json"
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
- [ ] ALL agents provided complete requirement analysis in their responses
- [ ] NO agent failures or incomplete responses
- [ ] Requirements synthesis consolidates all stakeholder input
- [ ] Architecture plan addresses all stakeholder requirements
- [ ] Conflict resolution documented for competing requirements
- [ ] Implementation strategy defined with clear success criteria

**Evidence Required:**
- Each agent response includes domain-specific requirements
- All conflicts between agent requirements identified
- Synthesis consolidates requirements from all agents
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

5. AGENT WORK ASSIGNMENT PLANNING:
   - Determine which agents will implement which components
   - Define integration points and dependencies
   - Specify file-based communication protocol
   - Edit: todo.md (mark task complete) - MUST be included in same commit as task deliverables

VIOLATION CHECK: All REQUIREMENTS state agent feedback addressed or exceptions documented

⚠️ CRITICAL COMMIT PATTERN VIOLATION: Never create separate commits for todo.md updates
MANDATORY: todo.md task completion update MUST be committed with task deliverables in single atomic commit
ANTI-PATTERN: git commit deliverables → git commit todo.md (VIOLATES PROTOCOL)
CORRECT PATTERN: git add deliverables todo.md → git commit (single atomic commit)
```

**Validation Function:**
```python
def validate_requirements_complete(agent_responses):
    for agent, response in agent_responses.items():
        if not response or len(response) < 100:
            return False, f"Incomplete response from {agent}"
        if "REQUIREMENTS COMPLETE" not in response:
            return False, f"Agent {agent} did not complete requirements analysis"
    return True, "All requirements complete"
```

### SYNTHESIS → CONTEXT
**Mandatory Conditions:**
- [ ] Requirements synthesis document created
- [ ] Architecture plan addresses all stakeholder requirements
- [ ] Conflict resolution documented for competing requirements
- [ ] Implementation strategy defined with clear success criteria
- [ ] **USER APPROVAL: Implementation plan presented to user**
- [ ] **USER CONFIRMATION: User has approved the proposed implementation approach**
- [ ] Agent work assignments determined

**Evidence Required:**
- Synthesis consolidates all stakeholder requirements
- Each agent requirement mapped to implementation approach
- Trade-off decisions documented with rationale
- Success criteria defined for each domain (architecture, security, performance, etc.)
- Implementation plan presented to user in clear, readable format
- User approval message received
- Agent work assignments determined

**Plan Presentation Requirements:**
```markdown
MANDATORY BEFORE CONTEXT GENERATION:
1. After SYNTHESIS complete, stop and present implementation plan to user
2. Plan must include:
   - Architecture approach and key design decisions
   - Files to be created/modified
   - Agent work assignments
   - Integration strategy
   - Testing strategy
   - Risk mitigation approaches
3. Wait for explicit user approval before proceeding to CONTEXT
4. Only proceed to CONTEXT after receiving clear user confirmation (e.g., "yes", "approved", "proceed")

PROHIBITED:
❌ Starting context generation without user plan approval
❌ Skipping plan presentation for "simple" tasks
❌ Assuming user approval without explicit confirmation
❌ Assuming approval from bypass mode or lack of objection
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

**🚨 CRITICAL BUILD CACHE VERIFICATION:**

Maven cache can restore cached quality gate results without analyzing modified code, allowing violations through.

**Validation Procedure**:
```bash
# RECOMMENDED: Disable cache for final validation
./mvnw verify -Dmaven.build.cache.enabled=false
```

**Checklist Before VALIDATION → REVIEW**:
- [ ] Build executed with `-Dmaven.build.cache.enabled=false`, OR
- [ ] Verified no "Skipping plugin execution (cached)" for pmd:check, checkstyle:check

**Required**: Verify quality gates executed fresh analysis, disable cache for final validation

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
Parser tasks: Use attack model from docs/project/scope.md (single-user code formatting, resource exhaustion focus, NOT information disclosure/exfiltration)

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

