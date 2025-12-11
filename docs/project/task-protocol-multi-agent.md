# Task Protocol - Multi-Agent Implementation Workflow

> **Version:** 1.0 | **Last Updated:** 2025-12-10
> **Parent Document:** [task-protocol-core.md](task-protocol-core.md)
> **Related Documents:** [task-protocol-operations.md](task-protocol-operations.md) •
[task-protocol-agents.md](task-protocol-agents.md)

**PURPOSE**: Multi-agent parallel development model, implementation rounds, and agent coordination.

**WHEN TO READ**: During IMPLEMENTATION state when coordinating stakeholder agents.

---

## MULTI-AGENT IMPLEMENTATION WORKFLOW {#multi-agent-implementation-workflow}

### Implementation Role Boundaries - Visual Reference {#implementation-role-boundaries---visual-reference}

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
| Main agent: `Edit src/Feature.java` | Main agent writing code directly | Main agent: `Task tool (architect)` → Agent writes code |
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

### Agent-Based Parallel Development Model {#agent-based-parallel-development-model}

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
✅ Main agent invokes architect agent → architect implements in their worktree → merges to
task branch
❌ Main agent creates FormattingRule.java directly in task worktree (PROTOCOL VIOLATION)

**Agent Tier System - CLARIFICATION**:

**Tier Purpose**: Used ONLY for requirements negotiation decision deadlocks, NOT for merge ordering.

**Requirements Phase Tiers (REQUIREMENTS → SYNTHESIS)**:
- **Tier 1 (Highest)**: architect - Final say on architecture decisions
- **Tier 2**: engineer, security - Override lower tiers on domain issues
- **Tier 3**: formatter, performance, tester, usability

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

### Model Selection Strategy {#model-selection-strategy}

**COST-OPTIMIZED ARCHITECTURE**: Agent model selection is designed to maximize quality while minimizing cost.

**Model Assignments**:
- **Agents in review mode** (Opus 4.5): Deep analysis, complex decision-making, detailed requirements generation
- **Agents in implementation mode** (Haiku 4.5): Mechanical implementation following detailed specifications

**Strategic Rationale**:

The protocol uses a **two-phase quality amplification** approach:

1. **REQUIREMENTS Phase** (High-cost, high-value):
   - Agents in review mode use Opus 4.5 for comprehensive analysis
   - Generate extremely detailed, implementation-ready specifications
   - Make ALL difficult decisions (architecture, design patterns, naming, trade-offs)
   - Output must be detailed enough for simpler model to execute mechanically

2. **IMPLEMENTATION Phase** (Low-cost, high-volume):
   - Agents in implementation mode use Haiku 4.5 for mechanical code generation
   - Execute reviewer specifications without making new decisions
   - Apply exact changes using Edit/Write tools with provided strings
   - No analysis, no judgment, pure implementation execution

**Critical Requirement for Reviewers**:

Agents in review mode MUST produce specifications that a **simpler model** (Haiku) can implement **without making
difficult decisions**. See agent-specific guidance sections in agent (review mode) definitions for detailed
requirements.

**Quality Guarantee**:

- **Bad requirements + expensive model** = Expensive, potentially wrong implementation
- **Good requirements + cheap model** = Cost-effective, correct implementation
- **Investment in requirements quality** enables cost savings in implementation volume

**Cost Optimization Calculation**:

Typical task execution:
- 1x REQUIREMENTS round (agents in review mode analyze entire codebase): Use Opus 4.5
- 2-5x IMPLEMENTATION rounds (agents in implementation mode apply fixes): Use Haiku 4.5 (40% cost reduction)
- Net savings: ~30-35% on total task cost while maintaining quality

**Prohibited Patterns**:
❌ Review mode producing vague requirements ("fix the issue")
❌ Implementation mode making design decisions (naming, patterns, architecture)
❌ Assuming both modes need same model capability

**Success Criteria**:
✅ Implementation succeeds on first attempt using only review mode's specifications
✅ No clarification questions from implementation mode to review mode
✅ Implementation matches requirements without re-analysis

### Implementation Round Structure {#implementation-round-structure}

**CRITICAL**: Implementation rounds use agents in BOTH review mode (Opus) and implementation mode (Haiku) in an iterative validation pattern.

**Agent Modes in IMPLEMENTATION**:
- **Review mode** (model: opus): Deep analysis, generate detailed requirements, approve/reject with specific feedback
- **Implementation mode** (model: haiku): Mechanical implementation, apply exact specifications, merge verified changes

**Single Agent Round Pattern**:
```
1. Agent (implementation mode) implements in their worktree (/workspace/tasks/{task}/agents/{agent}/code)
2. Agent validates locally: ./mvnw verify (in their worktree)
3. Agent merges to task branch
4. Agent (review mode) reviews what was merged to task branch
5. Agent decides: APPROVED or REJECTED
   - If APPROVED: Round complete for this agent
   - If REJECTED: Agent (implementation mode) fixes issues → merge → Agent (review mode) reviews again (repeat 4-5)
6. Agent updates status.json when work is approved and complete
```

**Multi-Agent Round Flow**:
```
Round 1:
├─ architect: Implement core interfaces → merge
├─ architect: Review → REJECTED (ambiguous contracts)
├─ architect: Fix contracts → merge
├─ architect: Review → APPROVED ✓
├─ engineer: Apply refactoring → merge
├─ engineer: Review → APPROVED ✓
├─ formatter: Apply style rules → merge
├─ formatter: Review → REJECTED (12 violations)
├─ formatter: Fix violations → merge
├─ formatter: Review → APPROVED ✓
└─ All agents: Update status.json {"status": "COMPLETE", "decision": "APPROVED"}

Transition to VALIDATION (all reviewers APPROVED, all status COMPLETE)
```

**Round Completion Criteria**:
```
All conditions must be met:
- [ ] All agents in implementation mode have merged their changes
- [ ] All agents in review mode have reviewed merged changes
- [ ] All agents in review mode report APPROVED (no rejections)
- [ ] All agents update status.json with "COMPLETE"
- [ ] Task branch passes ./mvnw verify
```

**Agent Work Completion Tracking**:

Each agent MUST maintain a `status.json` file to track implementation progress and signal work completion.

**Status File Location**: `/workspace/tasks/{task-name}/agents/{agent-name}/status.json`

**Status File Format**:

For agents in review mode:
```json
{
  "agent": "architect",
  "task": "implement-feature-x",
  "status": "WORKING|COMPLETE|BLOCKED",
  "decision": "APPROVED|REJECTED|PENDING",
  "round": 3,
  "last_review_sha": "abc123def456",
  "work_remaining": "none|description of pending work",
  "feedback": "detailed feedback for agent (implementation mode) (if REJECTED)",
  "updated_at": "2025-10-15T10:30:00Z"
}
```

For agents in implementation mode:
```json
{
  "agent": "architect",
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

Agent (implementation mode) after merging:
```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}/status.json <<EOF
{
  "agent": "{AGENT}",
  "task": "{TASK}",
  "mode": "implementation",
  "status": "COMPLETE",
  "work_remaining": "none",
  "round": ${CURRENT_ROUND},
  "last_merge_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

Agent in REQUIREMENTS/VALIDATION mode after reviewing:
```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}/status.json <<EOF
{
  "agent": "{AGENT}",
  "task": "{TASK}",
  "mode": "validation",
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

Main agent checks all stakeholder agent statuses (in VALIDATION mode) before proceeding to VALIDATION state:
```bash
check_all_validation_agents_approved() {
    TASK=$1
    shift
    AGENTS=("$@")

    # Check all agents in validation mode for APPROVED status
    for agent in "${AGENTS[@]}"; do
        STATUS_FILE="/workspace/tasks/$TASK/agents/$agent/status.json"

        # Verify status file exists
        [ ! -f "$STATUS_FILE" ] && {
            echo "Agent $agent: status file missing"
            return 1
        }

        # Check agent status and decision
        STATUS=$(jq -r '.status' "$STATUS_FILE")
        DECISION=$(jq -r '.decision' "$STATUS_FILE")
        WORK=$(jq -r '.work_remaining' "$STATUS_FILE")

        [ "$STATUS" != "COMPLETE" ] && {
            echo "Agent $agent: status=$STATUS (not COMPLETE)"
            return 1
        }

        [ "$DECISION" != "APPROVED" ] && {
            echo "Agent $agent: decision=$DECISION (not APPROVED)"
            return 1
        }

        [ "$WORK" != "none" ] && {
            echo "Agent $agent: work_remaining=$WORK"
            return 1
        }
    done

    echo "All validation agents report COMPLETE with APPROVED decision"
    return 0
}

# Usage - check stakeholder agents in VALIDATION mode
VALIDATION_AGENTS=("architect" "engineer" "formatter" "tester")
if check_all_validation_agents_approved "implement-feature-x" "${VALIDATION_AGENTS[@]}"; then
    echo "✅ All validation agents approved - transitioning to VALIDATION state"
    transition_to_validation
else
    echo "⏳ Continuing implementation rounds (reviewers have feedback or work pending)"
fi
```

### Agent Non-Completion Recovery Protocol {#agent-non-completion-recovery-protocol}

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
- architect: Core implementation (src/main/java)
- tester: Test files (src/test/java)
- formatter: Style configs and fixes
- security: Security features and validation
- performance: Performance optimizations
- engineer: Refactoring and best practices
- usability: UX improvements and documentation

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

### IMPLEMENTATION → VALIDATION {#implementation-validation}
**Mandatory Conditions:**
- [ ] All implementation rounds completed
- [ ] **🚨 CRITICAL: All stakeholder agents in VALIDATION mode report APPROVED decision**
- [ ] All agents in implementation mode have merged changes to task branch
- [ ] All planned deliverables created in task branch
- [ ] Implementation follows synthesis architecture plan
- [ ] Code adheres to project conventions and patterns
- [ ] All requirements from synthesis addressed or deferred with justification
- [ ] **🚨 CRITICAL: Each agent (implementation mode) performed incremental validation during rounds (fail-fast pattern)**
- [ ] **🚨 CRITICAL: All agent (implementation mode) changes MERGED to task branch**
- [ ] **🚨 CRITICAL: Task branch passes `./mvnw verify` after final merge**

**Evidence Required:**
- All agents in review mode: status.json shows {"status": "COMPLETE", "decision": "APPROVED"}
- All agents in implementation mode: status.json shows {"status": "COMPLETE", "work_remaining": "none"}
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
- `[architect] Add FormattingRule interface hierarchy`
- `[quality] Apply factory pattern to rule instantiation`
- `[style] Implement JavaDoc requirements for public APIs`
- `[test] Add comprehensive test suite for FormattingRule`

**Main Agent Final Commit**: When main agent commits validation fixes or merges to main branch, commit message MUST list all contributing agents:

```bash
git commit -m "$(cat <<'EOF'
Implement FormattingRule system

Contributing agents:
- architect: Core interface design
- engineer: Design pattern application
- formatter: Code style compliance
- tester: Test suite implementation

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

### VALIDATION → REVIEW (Conditional Path) {#validation-review-conditional-path}

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

### REVIEW → COMPLETE (Unanimous Approval Gate) {#review-complete-unanimous-approval-gate}
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

# ⚠️ MANDATORY: Squash all task commits into single commit
# Task branch MUST contain exactly ONE commit when merged to main
# This ensures clean linear history and atomic task units

# Count commits on task branch
COMMIT_COUNT=$(git rev-list --count main..{task-name})

if [ "$COMMIT_COUNT" -gt 1 ]; then
  # Interactive rebase to squash all commits into 1
  git rebase -i main
  # In the interactive editor:
  # - Keep first commit as "pick"
  # - Change all subsequent commits to "squash" or "fixup"
  # - Save and exit
  # - Edit the combined commit message to summarize the entire task
fi

# Verify exactly 1 commit remains
FINAL_COUNT=$(git rev-list --count main..{task-name})
if [ "$FINAL_COUNT" -ne 1 ]; then
  echo "❌ VIOLATION: Task branch must have exactly 1 commit, found $FINAL_COUNT"
  exit 1
fi

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

### REVIEW → SCOPE_NEGOTIATION (Conditional Transition) {#review-scope_negotiation-conditional-transition}
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

### COMPLETE → CLEANUP {#complete-cleanup}

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

### CLEANUP State Audit Preservation (MANDATORY) {#cleanup-state-audit-preservation-mandatory}

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

# Step 5: Delete ALL agent branches
# ⚠️ MANDATORY: Agent branches MUST be deleted during CLEANUP
git branch -D {task-name} 2>/dev/null || true
git branch | grep "^  {task-name}-" | xargs -r git branch -D

# Step 6: Verify complete branch cleanup
if git branch | grep -q "{task-name}"; then
  echo "❌ VIOLATION: Task branches still exist after CLEANUP"
  git branch | grep "{task-name}"
  exit 1
fi

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

### SCOPE_NEGOTIATION → SYNTHESIS (Return Path) or SCOPE_NEGOTIATION → COMPLETE (Deferral Path) {#scope_negotiation-synthesis-return-path-or-scope_negotiation-complete-deferral-path}
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
- **security**: Absolute veto on security vulnerabilities and privacy violations
- **build**: Final authority on deployment safety and build integrity
- **architect**: Final authority on architectural completeness requirements
- **performance**: Authority on performance requirements and scalability
- **quality**: Can defer documentation/testing if basic quality maintained
- **style**: Can defer style issues if core functionality preserved
- **test**: Authority on test coverage adequacy for business logic
- **usability**: Can defer UX improvements if core functionality accessible

**Security Auditor Configuration:**
**CRITICAL: Use Project-Specific Security Model from scope.md**

For Parser Implementation Tasks:
- **Attack Model**: Single-user code formatting scenarios (see docs/project/scope.md)
- **Security Focus**: Resource exhaustion prevention, system stability
- **NOT in Scope**: Information disclosure, data exfiltration, multi-user attacks
- **Usability Priority**: Error messages should prioritize debugging assistance
- **Appropriate Limits**: Reasonable protection for legitimate code formatting use cases

**MANDATORY**: security MUST reference docs/project/scope.md "Security Model for Parser Operations"
before conducting any parser security review.

**Authority Hierarchy for Domain Conflicts:**
When agent authorities overlap or conflict:
- Security/Privacy conflicts: Joint veto power (both security AND security must approve)
- Architecture/Performance conflicts: architect decides with performance input
- Build/Architecture conflicts: build has final authority (deployment safety priority)
- Testing/Quality conflicts: test decides coverage adequacy, quality decides standards
- Style/Quality conflicts: quality decides (quality encompasses style)

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

