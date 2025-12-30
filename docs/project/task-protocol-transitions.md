# Task Protocol - State Transitions

> **Version:** 1.0 | **Last Updated:** 2025-12-10
> **Parent Document:** [task-protocol-core.md](task-protocol-core.md)
> **Related Documents:** [task-protocol-operations.md](task-protocol-operations.md)

**PURPOSE**: Detailed state transition procedures, prerequisites, and verification steps.

**RECOMMENDED**: Use `state-transition` skill instead of reading this document.

---

## QUICK NAVIGATION

| Transition | Section |
|------------|---------|
| INIT -> CLASSIFIED | [INIT -> CLASSIFIED](#init-classified) |
| CLASSIFIED -> REQUIREMENTS | [CLASSIFIED -> REQUIREMENTS](#classified-requirements) |
| REQUIREMENTS -> SYNTHESIS | [REQUIREMENTS -> SYNTHESIS](#requirements-synthesis) |
| SYNTHESIS -> IMPLEMENTATION | [SYNTHESIS -> IMPLEMENTATION](#synthesis-implementation) |
| IMPLEMENTATION -> VALIDATION | [IMPLEMENTATION -> VALIDATION](#implementation-validation) |
| VALIDATION -> REVIEW | [VALIDATION -> REVIEW](#validation-review-conditional-path) |
| REVIEW -> COMPLETE | [REVIEW -> COMPLETE](#review-complete-unanimous-approval-gate) |
| COMPLETE -> CLEANUP | [COMPLETE -> CLEANUP](#complete-cleanup) |
| Lock Management | [Main Worktree Operations Lock](#main-worktree-operations-lock-requirement) |

---

## MANDATORY STATE TRANSITIONS {#mandatory-state-transitions}

### state.json File Management {#statejson-file-management}

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

## PRE-INIT VERIFICATION: Task Selection and Lock Validation {#pre-init-verification-task-selection-and-lock-validation}

**CRITICAL**: Before executing INIT phase, you MUST verify that the selected task is available for work by
checking existing locks and worktrees, AND verify you can complete the task autonomously.

### Autonomous Completion Feasibility Check {#autonomous-completion-feasibility-check}

Before acquiring lock and starting INIT, verify the task can be completed without user intervention:

**MANDATORY PRE-TASK CHECKLIST**:
```bash
# 1. Task has clear deliverables
grep -A 20 "TASK.*${TASK_NAME}" /workspace/main/todo.md
# Verify: Purpose, Scope, Components are defined

# 2. No external blockers exist
# PROCEED if: All dependencies available, no missing APIs, no ambiguous requirements
# SELECT ALTERNATIVE if: Requires unavailable external API, missing credentials, undefined requirements

# 3. Implementation is within capabilities
# PROCEED if: Technical approach is clear, no user design decisions needed
# SELECT ALTERNATIVE if: Requires user to choose between architectural options
```

**PROHIBITED STOPPING REASONS** (these are NOT blockers):
- "This might take a long time" - TIME ESTIMATES ARE NOT BLOCKERS
- "This is complex" - COMPLEXITY IS NOT A BLOCKER unless genuinely impossible
- "Token usage is high" - TOKENS NEVER JUSTIFY STOPPING
- "Should I ask the user first?" - NO, protocol is AUTONOMOUS

**LEGITIMATE STOPPING REASONS** (these ARE blockers):
- Genuine technical blocker (missing external API, undefined requirement)
- Ambiguity requiring user clarification (conflicting requirements with no resolution)
- Task scope changed externally (user modified todo.md indicating change)

**COMMITMENT**: If all checks pass, you MUST complete entire protocol (States 0-8) without asking user
permission.

### Decision Logic: Can I Work on This Task? {#decision-logic-can-i-work-on-this-task}

**STEP 1: Check for existing lock file**
```bash
# Replace {TASK_NAME} with actual task name
cat /workspace/tasks/{TASK_NAME}/task.json 2>/dev/null
```

**STEP 2: Analyze lock ownership**
```bash
LOCK_SESSION=$(jq -r '.session_id' /workspace/tasks/{TASK_NAME}/task.json 2>/dev/null)
CURRENT_SESSION="{SESSION_ID}"  # From system environment

if [ "$LOCK_SESSION" = "$CURRENT_SESSION" ]; then
    echo "Lock owned by current session - can resume task"
elif [ -n "$LOCK_SESSION" ]; then
    echo "Lock owned by different session ($LOCK_SESSION) - SELECT ALTERNATIVE TASK"
else
    echo "No lock exists - can acquire lock and start task"
fi
```

**STEP 3: Check for existing worktree**
```bash
ls -d /workspace/tasks/{TASK_NAME} 2>/dev/null && echo "Worktree exists" || echo "No worktree"
```

### Task Selection Decision Matrix {#task-selection-decision-matrix}

| Lock Exists? | Lock Owner | Worktree Exists? | Action |
|--------------|------------|------------------|--------|
| YES | Current session | YES | **RESUME**: Skip INIT, verify current state, continue from last phase |
| YES | Current session | NO | **ERROR**: Inconsistent state - lock exists but worktree missing - ask user |
| YES | Different session | YES | **SELECT ALTERNATIVE TASK**: Another instance is working on this task |
| YES | Different session | NO | **SELECT ALTERNATIVE TASK**: Lock exists without worktree - may be initializing |
| NO | N/A | YES | **ASK USER**: Worktree exists without lock - crashed session or manual intervention |
| NO | N/A | NO | **PROCEED WITH INIT**: Task is available - execute normal INIT phase |

### Prohibited Actions {#prohibited-actions}

- **NEVER** delete or override a lock file owned by a different session
- **NEVER** assume an existing worktree without a lock is abandoned
- **NEVER** proceed with INIT if a lock exists for a different session
- **NEVER** skip lock verification when user says "continue with next task"

### Required Actions {#required-actions}

- **ALWAYS** check `/workspace/tasks/{TASK_NAME}/task.json` before starting work
- **ALWAYS** compare lock session_id with current session_id
- **ALWAYS** check for existing worktree at `/workspace/tasks/{TASK_NAME}`
- **ALWAYS** select an alternative task if lock is owned by different session
- **ALWAYS** ask user for guidance if worktree exists without a lock

### Recovery from Crashed Sessions {#recovery-from-crashed-sessions}

**Scenario**: Worktree exists at `/workspace/tasks/{TASK_NAME}` but no lock file exists.

**Possible Causes**:
- Previous Claude instance crashed before cleanup
- Manual worktree creation outside protocol
- Lock file manually deleted

**Required Action**: Ask user for guidance:
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

## MAIN WORKTREE OPERATIONS LOCK REQUIREMENT {#main-worktree-operations-lock-requirement}

**CRITICAL**: Any operations executed directly on the main worktree (`/workspace/main/`) require acquiring a
special lock at `/workspace/tasks/main/task.json`.

### Operations Requiring Main Worktree Lock {#operations-requiring-main-worktree-lock}

**MANDATORY LOCK ACQUISITION** for:
- Modifying files directly in main worktree working directory
- Running git operations on main branch (e.g., `git checkout`, `git merge`, `git pull`)
- Updating main branch configuration files
- Any direct edits to main worktree files outside of task-specific worktrees
- Emergency fixes or hotfixes applied directly to main

### Main Worktree Lock Format {#main-worktree-lock-format}

```json
{
  "session_id": "unique-session-identifier",
  "task_name": "main-worktree-operation",
  "operation_type": "git-operation|file-modification|configuration-update",
  "state": "IN_PROGRESS",
  "created_at": "ISO-8601-timestamp"
}
```

### Acquiring Main Worktree Lock {#acquiring-main-worktree-lock}

**STEP 1: Attempt atomic lock creation**
```bash
export SESSION_ID="{SESSION_ID}" && mkdir -p /workspace/locks && (set -C; echo "{\"session_id\": \"${SESSION_ID}\", \"task_name\": \"main-worktree-operation\", \"operation_type\": \"{OPERATION_TYPE}\", \"state\": \"IN_PROGRESS\", \"created_at\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}" > /workspace/tasks/main/task.json) && echo "MAIN_LOCK_SUCCESS" || echo "MAIN_LOCK_FAILED"
```

**STEP 2: Check lock ownership if MAIN_LOCK_FAILED**
```bash
LOCK_OWNER=$(jq -r '.session_id' /workspace/tasks/main/task.json 2>/dev/null)
CURRENT_SESSION="{SESSION_ID}"

if [ "$LOCK_OWNER" = "$CURRENT_SESSION" ]; then
    echo "Main lock already owned by current session"
elif [ -n "$LOCK_OWNER" ]; then
    echo "Main worktree locked by different session ($LOCK_OWNER)"
    echo "ABORT: Cannot perform main worktree operations - wait or select different task"
    exit 1
else
    echo "No main lock exists - retry lock acquisition"
fi
```

### Releasing Main Worktree Lock {#releasing-main-worktree-lock}

**After completing main worktree operations:**
```bash
# CRITICAL: Verify lock ownership before deletion
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/tasks/main/task.json")

if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
  echo "FATAL: Cannot delete main lock owned by $LOCK_OWNER"
  exit 1
fi

# Delete main lock file (only if ownership verified)
rm -f /workspace/tasks/main/task.json
echo "Main worktree lock released"
```

### Main Worktree Lock vs Task-Specific Locks {#main-worktree-lock-vs-task-specific-locks}

**Key Differences:**
- **Task locks** (`/workspace/tasks/{task-name}/task.json`): Used for task-specific worktrees during normal
  protocol execution
- **Main lock** (`/workspace/tasks/main/task.json`): Used ONLY for direct operations on main worktree

**When to use each:**
- **Normal task work**: Use task-specific lock and work in task worktree (`/workspace/tasks/{task-name}/code/`)
- **Direct main operations**: Use main lock when working directly in main worktree (`/workspace/main/`)

**Prohibited Patterns:**
- Modifying main worktree files without acquiring main lock
- Running git operations on main branch without main lock
- Assuming main worktree is always available

**Required Patterns:**
- Acquire main lock before ANY main worktree operations
- Release main lock immediately after operations complete
- Verify lock ownership before release
- Wait or select alternative task if main lock is owned by different session

---

## INIT -> CLASSIFIED {#init-classified}

**Mandatory Conditions (Prerequisites):**
1. Session ID validated and unique
2. Atomic lock acquired for task (LOCK_SUCCESS received) at `/workspace/tasks/{task-name}/task.json`
3. Task exists in todo.md
4. Task worktree created at `/workspace/tasks/{task-name}/code`
5. Agent worktrees created at `/workspace/tasks/{task-name}/agents/{agent-name}/code` for all selected agents
6. **CRITICAL: Changed directory to task worktree**
7. **CRITICAL: Verified pwd shows task directory**

**Evidence Required:**
- Lock file creation timestamp at `/workspace/tasks/{task-name}/task.json`
- Session ID validation output
- Task worktree creation confirmation
- All agent worktrees creation confirmation
- **pwd verification showing `/workspace/tasks/{task-name}/code`**

**MANDATORY TOOL USAGE PATTERN**:
- Task requirements loaded in SINGLE parallel read: `Read todo.md + Glob locks/*.json`
- Configuration files loaded in SINGLE parallel read (if applicable): `Read pom.xml + Read checkstyle.xml + Read pmd.xml`
- Architecture documentation loaded with implementation files (if applicable): `Read docs/project/architecture.md + Glob src/main/java/**/*Pattern*.java`

**NOTE**: Protocol guidance is provided just-in-time via hooks. You do NOT need to read
task-protocol-core.md or task-protocol-operations.md upfront.

**VERIFICATION**:
```bash
# Each message should have >=2 tool calls during INIT
# Average tools per message should be >=2.0
```

**INIT is NOT complete until you have:**
- Created task directory and acquired atomic lock
- Created task and agent worktrees
- Changed to task worktree directory
- Verified pwd shows correct task directory

### Lock State Update Helper {#lock-state-update-helper}

**Purpose**: Update lock file state as task progresses through protocol phases

```bash
update_lock_state() {
  local TASK_NAME=$1
  local NEW_STATE=$2
  local LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"

  # Verify lock exists and ownership
  if [ ! -f "$LOCK_FILE" ]; then
    echo "ERROR: Lock file not found: $LOCK_FILE"
    return 1
  fi

  local LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "$LOCK_FILE")
  local SESSION_ID="${CURRENT_SESSION_ID}"

  if [ "$LOCK_OWNER" != "$SESSION_ID" ]; then
    echo "FATAL: Cannot update lock owned by $LOCK_OWNER"
    return 1
  fi

  # Update state field
  sed -i "s/\"state\":\s*\"[^\"]*\"/\"state\": \"$NEW_STATE\"/" "$LOCK_FILE"
  echo "Lock state updated: $NEW_STATE"
}
```

**Valid State Values**: INIT, CLASSIFIED, REQUIREMENTS, SYNTHESIS, IMPLEMENTATION, VALIDATION, REVIEW, AWAITING_USER_APPROVAL, SCOPE_NEGOTIATION, COMPLETE, CLEANUP

**IMPORTANT**: Update lock state at the START of each phase transition to maintain accurate recovery state.

### Enhanced Lock File Format with Checkpoint Tracking {#enhanced-lock-file-format-with-checkpoint-tracking}

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
- **Validation**: Verify no states were skipped (INIT->CLASSIFIED->REQUIREMENTS->SYNTHESIS->IMPLEMENTATION)

**Updating transition_log**:
```bash
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
# After REVIEW state with unanimous approval, push branch and update lock
TASK_NAME="your-task-name"
LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"

# STEP 1: Push task branch to origin for review
git push origin "$TASK_NAME"

# STEP 2: Get commit SHA after push
COMMIT_SHA=$(git rev-parse HEAD)

# STEP 3: Update lock to AWAITING_USER_APPROVAL state with checkpoint data
jq --arg sha "$COMMIT_SHA" \
   --arg timestamp "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
   '.state = "AWAITING_USER_APPROVAL" |
    .checkpoint = {
      "type": "USER_APPROVAL_POST_REVIEW",
      "commit_sha": $sha,
      "presented_at": $timestamp,
      "approved": false
    }' "$LOCK_FILE" > "${LOCK_FILE}.tmp" && mv "${LOCK_FILE}.tmp" "$LOCK_FILE"

# STEP 4: Present changes to user with commit SHA and remote branch
echo "Task branch pushed to origin for review"
echo "Commit: $COMMIT_SHA"
echo "Remote: origin/$TASK_NAME"
```

**Marking Checkpoint as Approved**:
```bash
# After user provides explicit approval
LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"
jq '.checkpoint.approved = true' "$LOCK_FILE" > "${LOCK_FILE}.tmp" && mv "${LOCK_FILE}.tmp" "$LOCK_FILE"
```

**Checking Checkpoint Status Before COMPLETE (MANDATORY)**:
```bash
# Mandatory verification before COMPLETE state transition
CHECKPOINT_APPROVED=$(jq -r '.checkpoint.approved // false' "$LOCK_FILE")

if [ "$CHECKPOINT_APPROVED" != "true" ]; then
    echo "CHECKPOINT VIOLATION: User approval not obtained"
    echo "Cannot proceed to COMPLETE state"
    exit 1
fi
```

### Backward Transition: AWAITING_USER_APPROVAL -> IMPLEMENTATION {#backward-transition-awaiting-implementation}

**When user requests changes during AWAITING_USER_APPROVAL**, transition back to IMPLEMENTATION immediately.

**No Permission Needed**: The user's change request IS the instruction to do the work. Do NOT present options
or ask for confirmation.

**PROHIBITED Patterns**:
- "Would you like me to: 1) transition back, 2) wait, 3) create follow-up task?"
- "The hook blocked agent invocation. Options: ..."
- "Should I transition back to IMPLEMENTATION to make these changes?"

**REQUIRED Pattern**:
```bash
# User requests changes -> Immediately transition back
jq '.state = "IMPLEMENTATION" | .transitions += [{"from": "AWAITING_USER_APPROVAL", "to": "IMPLEMENTATION",
    "timestamp": "'$(date -Iseconds)'", "reason": "User requested changes"}]' task.json > task.json.tmp && \
    mv task.json.tmp task.json

# Then invoke agents to implement the requested changes
```

**See also**: [CLAUDE.md - User Change Requests During AWAITING_USER_APPROVAL](../../CLAUDE.md#user-change-requests-awaiting-approval)

### Dynamic Agent Addition Mid-Task {#dynamic-agent-addition-mid-task}

**CRITICAL GUIDANCE**: Agent selection is typically finalized during CLASSIFIED state, but circumstances may require adding agents after initial selection.

**PERMITTED Scenarios**:
1. **During REQUIREMENTS**: Initial requirements reveal need for additional domain expertise
   - Example: Security requirements reveal need for security (if not initially selected)
   - Example: Performance constraints discovered, need performance
2. **During SYNTHESIS**: Requirements synthesis identifies gaps in domain coverage
   - Example: Agent conflicts need additional tier-1 authority (architect)
   - Example: Complex integration needs usability input
3. **During IMPLEMENTATION**: Implementation complexity exceeds initial agent capabilities
   - Example: Realize need for test for complex business logic
   - Example: Build integration issues need build expertise

**PROHIBITED Scenarios**:
1. **During VALIDATION/REVIEW**: Too late - implementation complete, adding agents is rework
2. **To bypass rejections**: Cannot add "friendly" agents to override existing agent rejections
3. **For convenience**: Cannot add agents to avoid fixing issues raised by existing agents

**Dynamic Agent Addition Process**:

**STEP 1: Identify Gap and Justification**
```markdown
Document why additional agent is needed:
- What domain expertise is missing?
- What specific requirement cannot be met without this agent?
- Why wasn't this agent selected initially?
- What evidence justifies adding them now?

Example:
"During REQUIREMENTS, security identified authentication requirements.
 Initial risk assessment classified task as MEDIUM-RISK (test files only),
 but authentication features require HIGH-RISK security validation.
 Adding security to requirements gathering."
```

**STEP 2: Create Agent Worktree**
```bash
TASK_NAME="your-task-name"
NEW_AGENT="security"
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

**STEP 3: Bring Agent Up to Current State**

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

**STEP 4: Update Task Documentation**
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
    "architect",
    "quality",
    "style",
    "security"
  ],
  "agent_additions": [
    {
      "agent": "security",
      "added_at_state": "REQUIREMENTS",
      "timestamp": "2025-10-18T14:30:00Z",
      "justification": "Authentication requirements discovered during requirements gathering"
    }
  ]
}
```

**State-Specific Addition Constraints**:

**REQUIREMENTS State**:
- Can add any agent type
- Agent participates fully from this point
- No state reversal needed
- May extend REQUIREMENTS duration (more agents to complete)

**SYNTHESIS State**:
- Can add if synthesis reveals gaps
- Requires re-synthesis with new agent input
- User must re-approve revised plan
- Cannot skip agent's requirements input (must review existing reports)

**IMPLEMENTATION State**:
- Use sparingly - indicates planning failure
- Permitted for genuine implementation gaps
- Agent must catch up by reviewing task branch
- May require additional implementation rounds
- Cannot add to bypass existing agent rejections

**VALIDATION/REVIEW States**:
- Too late - implementation complete
- Adding agents now requires returning to REQUIREMENTS
- IF CRITICAL: Return to REQUIREMENTS, add agent, re-execute full protocol

**Example: Adding Security Auditor During REQUIREMENTS**

```markdown
Scenario: Task classified as MEDIUM-RISK (test file modifications).
During REQUIREMENTS, architect identifies authentication logic in tests.

Decision:
- Authentication = security concern
- Original classification missed security implications
- security needed for requirements gathering

Process:
1. Document justification: "Authentication logic requires security validation"
2. Create security worktree
3. Update lock file: required_agents += ["security"]
4. Invoke security with REQUIREMENTS mode
5. Wait for security requirements report
6. Proceed to SYNTHESIS with 4 agents (was 3)

Result:
- Task now has security validation coverage
- Synthesis incorporates security requirements
- Implementation includes security best practices
```

**Example: Attempting to Add Agent During REVIEW (PROHIBITED)**

```markdown
Scenario: During REVIEW, tester rejects due to insufficient test coverage.
Main agent considers adding additional tester agent.

Decision: PROHIBITED

Reason:
- REVIEW state means implementation complete
- Cannot add agents to bypass rejection
- Proper response: Return to IMPLEMENTATION, improve test coverage
- test re-reviews improved tests in next REVIEW round

Correct Recovery:
1. Do NOT add new agent
2. Return to IMPLEMENTATION state
3. Address test's test coverage concerns
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

---

## CLASSIFIED -> REQUIREMENTS {#classified-requirements}

**Mandatory Conditions (Prerequisites):**
1. Risk level determined (HIGH/MEDIUM/LOW)
2. Agent set selected based on risk classification
3. **CRITICAL: Verified currently in task worktree (pwd check)**
4. Agent worktrees created for all selected agents
5. **CRITICAL: Main agent MUST create task.md at `/workspace/tasks/{TASK_NAME}/task.md` BEFORE
   transitioning to REQUIREMENTS state**

**task.md Creation Responsibility**:
- **WHO**: Main coordination agent (the agent that executed INIT and CLASSIFIED states)
- **WHEN**: After agent selection, BEFORE invoking any stakeholder agents
- **WHERE**: `/workspace/tasks/{TASK_NAME}/task.md` (task root, NOT inside code directory)
- **WHAT**: Initial task.md with Task Objective, Scope Definition sections, and empty Stakeholder Agent
  Reports placeholders

**Verification Command**:
```bash
[ -f "/workspace/tasks/${TASK_NAME}/task.md" ] || {
  echo "CRITICAL ERROR: task.md not created by CLASSIFIED state"
  echo "Required location: /workspace/tasks/${TASK_NAME}/task.md"
  echo "ABORT: Cannot proceed to REQUIREMENTS state without task.md"
  exit 1
}
echo "task.md verification PASSED"
```

**Evidence Required:**
- Risk classification reasoning documented
- **MANDATORY: pwd verification showing task directory**
- Agent selection justification based on file patterns
- task.md exists with mandatory sections
- All agent worktrees created and accessible

**CRITICAL PRE-REQUIREMENTS VERIFICATION**:
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing
# MANDATORY: Verify working directory BEFORE invoking ANY agents
pwd | grep -q "/workspace/tasks/{TASK_NAME}/code$" || {
  echo "CRITICAL ERROR: Not in task worktree!"
  echo "Current directory: $(pwd)"
  echo "Required directory: /workspace/tasks/{TASK_NAME}/code"
  echo "ABORT: Cannot proceed to REQUIREMENTS state"
  exit 1
}
echo "Directory verification PASSED: $(pwd)"

# MANDATORY: Verify all agent worktrees exist
for agent in architect quality style; do
  [ -d "/workspace/tasks/{TASK_NAME}/agents/$agent/code" ] || {
    echo "CRITICAL ERROR: Agent worktree missing: $agent"
    exit 1
  }
done
echo "All agent worktrees verified"
```

**PROHIBITED PATTERN**:
- Invoking stakeholder agents while in main branch
- Proceeding to State 2 without pwd verification
- Assuming you're in the correct directory without checking

**REQUIRED PATTERN**:
- Execute pwd verification command
- Confirm output matches task worktree path
- Only then invoke stakeholder agents

### Agent Invocation Interruption Handling {#agent-invocation-interruption-handling}

**CRITICAL GUIDANCE**: When agent invocations are initiated but interrupted by user commands or directives, the following recovery pattern applies.

**Common Interruption Scenarios**:
1. **Audit Commands**: User runs `/audit-session` during REQUIREMENTS state while agents are being invoked
2. **User Questions**: User asks questions or requests status updates mid-invocation
3. **Priority Changes**: User requests different tasks or changes task priorities
4. **System Commands**: User runs other slash commands that interrupt normal flow

**Recovery Pattern After Interruption**:

**STEP 1: COMPLETE INTERRUPTING COMMAND**
- Process user's immediate request fully (audit, questions, status, etc.)
- Do NOT abandon the interrupted agent invocations
- Do NOT transition to different state without completing interruption

**STEP 2: VERIFY STATE CONSISTENCY**
- Check lock file state matches expected state before interruption
- Verify worktrees remain intact
- Confirm no state corruption occurred during interruption

**STEP 3: ASSESS AGENT COMPLETION STATUS**
- Check which agents completed before interruption (via status.json files)
- Identify which agents need to be invoked or re-invoked
- Preserve any partial agent work (requirements reports, status updates)

**STEP 4: RESUME AGENT INVOCATIONS**
- Re-invoke incomplete agents in parallel (same as original invocation)
- Do NOT skip any required agents
- Wait for all agents to complete before state transition

**State Preservation Requirements During Interruption**:
- Lock file state remains unchanged (e.g., stays in REQUIREMENTS)
- Task worktree and agent worktrees remain intact
- Partial agent completions preserved (check status.json, requirements reports)
- Protocol state machine position maintained

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
- **PROHIBITED**: Skipping agent invocations because of interruption
- **PROHIBITED**: Transitioning to next state with incomplete agent work
- **PROHIBITED**: Assuming interruption invalidates prior agent completions
- **REQUIRED**: Resume and complete all agent invocations after interruption
- **REQUIRED**: Preserve partial progress from agents that completed before interruption
- **REQUIRED**: Verify state consistency before and after interruption

**Interruption Recovery Examples**:

**Example 1: All Agents Not Yet Invoked**
```markdown
Scenario: User runs `/audit-session` immediately after CLASSIFIED -> REQUIREMENTS transition

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
   - architect: COMPLETE (has requirements.md)
   - engineer: COMPLETE (has requirements.md)
   - formatter: COMPLETE (has requirements.md)
   - builder: NOT STARTED
   - tester: NOT STARTED
   - security: NOT STARTED
   - performance: NOT STARTED
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

---

## REQUIREMENTS -> SYNTHESIS {#requirements-synthesis}

**Mandatory Conditions (Prerequisites):**
1. ALL required agents invoked in parallel
2. ALL agents provided complete requirement reports
3. ALL agent reports written to `../{agent-name}-requirements.md`
4. NO agent failures or incomplete responses
5. Requirements synthesis document created
6. Architecture plan addresses all stakeholder requirements
7. Conflict resolution documented for competing requirements
8. Implementation strategy defined with clear success criteria

### Partial Agent Completion Handling (REQUIREMENTS State) {#partial-agent-completion-handling-requirements-state}

**CRITICAL CLARIFICATION**: REQUIREMENTS -> SYNTHESIS transition requires ALL agents to complete successfully.
Partial completion is NOT acceptable for state transition.

**Agent Completion Criteria** (BOTH conditions must be met):
1. **status.json exists** with `{"status": "COMPLETE"}` in `/workspace/tasks/{task-name}/agents/{agent-name}/status.json`
2. **Requirements report exists** at `/workspace/tasks/{task-name}/{task-name}-{agent-name}-requirements.md`

**Verification Function**:
```bash
verify_agent_completion() {
    local TASK_NAME=$1
    local AGENT=$2
    local STATUS_FILE="/workspace/tasks/${TASK_NAME}/agents/${AGENT}/status.json"
    local REPORT_FILE="/workspace/tasks/${TASK_NAME}/${TASK_NAME}-${AGENT}-requirements.md"

    # Check status.json
    if [ ! -f "$STATUS_FILE" ]; then
        echo "$AGENT: status.json missing"
        return 1
    fi

    local STATUS=$(jq -r '.status' "$STATUS_FILE" 2>/dev/null)
    if [ "$STATUS" != "COMPLETE" ]; then
        echo "$AGENT: status=$STATUS (expected: COMPLETE)"
        return 1
    fi

    # Check requirements report
    if [ ! -f "$REPORT_FILE" ]; then
        echo "$AGENT: requirements report missing"
        return 1
    fi

    # Verify report is non-empty
    if [ ! -s "$REPORT_FILE" ]; then
        echo "$AGENT: requirements report is empty"
        return 1
    fi

    echo "$AGENT: COMPLETE (status + report verified)"
    return 0
}

# Usage: Verify all agents before SYNTHESIS transition
ALL_COMPLETE=true
for agent in architect engineer formatter builder tester security performance; do
    if ! verify_agent_completion "${TASK_NAME}" "$agent"; then
        ALL_COMPLETE=false
    fi
done

if [ "$ALL_COMPLETE" = "true" ]; then
    echo "All agents COMPLETE - ready for SYNTHESIS transition"
else
    echo "Some agents incomplete - cannot transition to SYNTHESIS"
fi
```

**Handling Incomplete Agents During REQUIREMENTS**:

**Scenario 1: Agent Reports ERROR Status**
```markdown
Recovery:
1. Read error_message from status.json
2. Classify error type:
   - Tool limitation (file too large, complexity) -> Re-invoke with reduced scope
   - Blocker (missing dependency, unclear requirements) -> Return to CLASSIFIED state, clarify requirements
   - Implementation error (agent bug) -> Re-invoke with same requirements
3. After fix, re-invoke agent
4. Wait for agent to reach COMPLETE status
5. Only then proceed to SYNTHESIS
```

**Scenario 2: Agent Status is WORKING/IN_PROGRESS (Timeout)**
```markdown
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
```markdown
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
```markdown
Recovery:
1. Assess pattern:
   - All agents incomplete -> Systematic issue (requirements unclear, worktree problem)
     -> Return to CLASSIFIED state, fix root cause
   - Specific agent type incomplete -> Domain-specific issue
     -> Re-invoke those agents with clarified requirements
   - Random subset incomplete -> Individual agent issues
     -> Re-invoke incomplete agents individually
2. Maximum retry attempts: 2 per agent
3. After 2 failures: Escalate to user for guidance
```

**CRITICAL TRANSITION GATE**:
```bash
# MANDATORY gate check
echo "=== REQUIREMENTS -> SYNTHESIS TRANSITION GATE ==="

# Count completed agents
COMPLETED_COUNT=0
REQUIRED_COUNT=${#REQUIRED_AGENTS[@]}

for agent in "${REQUIRED_AGENTS[@]}"; do
    if verify_agent_completion "${TASK_NAME}" "$agent"; then
        ((COMPLETED_COUNT++))
    fi
done

if [ $COMPLETED_COUNT -eq $REQUIRED_COUNT ]; then
    echo "GATE PASSED: All $REQUIRED_COUNT agents COMPLETE"
    echo "Proceeding to SYNTHESIS state"
    jq '.state = "SYNTHESIS"' task.json > task.json.tmp
    mv task.json.tmp task.json
else
    echo "GATE FAILED: $COMPLETED_COUNT/$REQUIRED_COUNT agents complete"
    echo "Missing agents require completion before SYNTHESIS"
    exit 1
fi
```

**PROHIBITED Transition Patterns**:
- Proceeding to SYNTHESIS with 6/7 agents complete ("one agent not critical")
- Skipping failed agent's requirements ("we can work without security review")
- Accepting ERROR status without recovery ("agent provided partial feedback")
- Assuming empty requirements report means "no requirements" (it's a failure)

**REQUIRED Patterns**:
- Wait for ALL agents to reach COMPLETE status
- Re-invoke failed agents until successful completion
- Escalate to user only after retry attempts exhausted
- Verify both status.json AND requirements report before accepting completion

### REQUIREMENTS State Exit Verification Procedure (9-Point Checklist) {#requirements-state-exit-verification-procedure}

**MANDATORY**: Before transitioning from REQUIREMENTS to SYNTHESIS, execute this comprehensive verification.

```bash
#!/bin/bash
# verify-requirements-exit.sh - Gate enforcement before REQUIREMENTS -> SYNTHESIS

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
    echo "FAILED: Lock state is '$CURRENT_STATE', expected 'REQUIREMENTS'"
    exit 1
fi
echo "PASSED: Lock state = REQUIREMENTS"

# =============================================================================
# CHECK 2: Session Ownership Verification
# =============================================================================
echo "[2/9] Verifying session ownership..."

LOCK_SESSION=$(jq -r '.session_id' "$LOCK_FILE")
if [ "$LOCK_SESSION" != "$CURRENT_SESSION_ID" ]; then
    echo "FAILED: Lock owned by different session ($LOCK_SESSION)"
    exit 1
fi
echo "PASSED: Lock owned by current session"

# =============================================================================
# CHECK 3: Working Directory Verification
# =============================================================================
echo "[3/9] Verifying working directory..."

CURRENT_DIR=$(pwd)
EXPECTED_DIR="${TASK_DIR}/code"
if [ "$CURRENT_DIR" != "$EXPECTED_DIR" ]; then
    echo "FAILED: Wrong directory"
    echo "   Current: $CURRENT_DIR"
    echo "   Expected: $EXPECTED_DIR"
    exit 1
fi
echo "PASSED: In task worktree directory"

# =============================================================================
# CHECK 4: Task.md Structure Verification
# =============================================================================
echo "[4/9] Verifying task.md structure..."

if [ ! -f "$TASK_MD" ]; then
    echo "FAILED: task.md not found at $TASK_MD"
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
        echo "FAILED: task.md missing section: $section"
        exit 1
    fi
done
echo "PASSED: task.md structure valid"

# =============================================================================
# CHECK 5: Agent Worktree Existence
# =============================================================================
echo "[5/9] Verifying agent worktrees exist..."

# Get required agents from lock file
REQUIRED_AGENTS=($(jq -r '.required_agents[]' "$LOCK_FILE" 2>/dev/null))
if [ ${#REQUIRED_AGENTS[@]} -eq 0 ]; then
    echo "FAILED: No required_agents in lock file"
    exit 1
fi

for agent in "${REQUIRED_AGENTS[@]}"; do
    AGENT_DIR="${TASK_DIR}/agents/${agent}/code"
    if [ ! -d "$AGENT_DIR" ]; then
        echo "FAILED: Agent worktree missing: $agent"
        echo "   Expected at: $AGENT_DIR"
        exit 1
    fi
done
echo "PASSED: All ${#REQUIRED_AGENTS[@]} agent worktrees exist"

# =============================================================================
# CHECK 6: Agent Completion Status
# =============================================================================
echo "[6/9] Verifying agent completion status..."

INCOMPLETE_AGENTS=()
for agent in "${REQUIRED_AGENTS[@]}"; do
    STATUS_FILE="${TASK_DIR}/agents/${agent}/status.json"

    # Check status.json exists
    if [ ! -f "$STATUS_FILE" ]; then
        echo "$agent: status.json missing"
        INCOMPLETE_AGENTS+=("$agent")
        continue
    fi

    # Check status is COMPLETE
    STATUS=$(jq -r '.status' "$STATUS_FILE" 2>/dev/null)
    if [ "$STATUS" != "COMPLETE" ]; then
        echo "$agent: status=$STATUS (expected: COMPLETE)"
        INCOMPLETE_AGENTS+=("$agent")
        continue
    fi

    echo "$agent: COMPLETE"
done

if [ ${#INCOMPLETE_AGENTS[@]} -gt 0 ]; then
    echo "FAILED: ${#INCOMPLETE_AGENTS[@]} agents incomplete"
    echo "   Incomplete: ${INCOMPLETE_AGENTS[*]}"
    exit 1
fi
echo "PASSED: All agents status = COMPLETE"

# =============================================================================
# CHECK 7: Requirements Reports Existence and Validity
# =============================================================================
echo "[7/9] Verifying requirements reports..."

MISSING_REPORTS=()
for agent in "${REQUIRED_AGENTS[@]}"; do
    REPORT_FILE="${TASK_DIR}/${TASK_NAME}-${agent}-requirements.md"

    # Check report exists
    if [ ! -f "$REPORT_FILE" ]; then
        echo "$agent: requirements report missing"
        MISSING_REPORTS+=("$agent")
        continue
    fi

    # Check report is non-empty
    if [ ! -s "$REPORT_FILE" ]; then
        echo "$agent: requirements report is empty"
        MISSING_REPORTS+=("$agent")
        continue
    fi

    # Check report has minimum content (at least 100 characters)
    REPORT_SIZE=$(wc -c < "$REPORT_FILE")
    if [ $REPORT_SIZE -lt 100 ]; then
        echo "$agent: requirements report too small ($REPORT_SIZE bytes)"
        MISSING_REPORTS+=("$agent")
        continue
    fi

    echo "$agent: requirements report valid ($REPORT_SIZE bytes)"
done

if [ ${#MISSING_REPORTS[@]} -gt 0 ]; then
    echo "FAILED: ${#MISSING_REPORTS[@]} reports missing/invalid"
    echo "   Missing: ${MISSING_REPORTS[*]}"
    exit 1
fi
echo "PASSED: All requirements reports valid"

# =============================================================================
# CHECK 8: State Transition History
# =============================================================================
echo "[8/9] Verifying state transition history..."

# Verify transition log exists and contains expected sequence
TRANSITION_LOG=$(jq -r '.transition_log' "$LOCK_FILE" 2>/dev/null)
if [ "$TRANSITION_LOG" = "null" ]; then
    echo "FAILED: transition_log missing from lock file"
    exit 1
fi

# Check INIT -> CLASSIFIED transition exists
if ! jq -e '.transition_log[] | select(.from == "INIT" and .to == "CLASSIFIED")' "$LOCK_FILE" >/dev/null 2>&1; then
    echo "FAILED: Missing INIT -> CLASSIFIED transition"
    exit 1
fi

# Check CLASSIFIED -> REQUIREMENTS transition exists
if ! jq -e '.transition_log[] | select(.from == "CLASSIFIED" and .to == "REQUIREMENTS")' "$LOCK_FILE" >/dev/null 2>&1; then
    echo "FAILED: Missing CLASSIFIED -> REQUIREMENTS transition"
    exit 1
fi

echo "PASSED: State transition history valid"

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
        echo "$agent: ERROR status - $ERROR_MSG"
        ERROR_AGENTS+=("$agent")
    fi
done

if [ ${#ERROR_AGENTS[@]} -gt 0 ]; then
    echo "FAILED: ${#ERROR_AGENTS[@]} agents in ERROR state"
    echo "   Errors: ${ERROR_AGENTS[*]}"
    exit 1
fi
echo "PASSED: No agents in ERROR state"

# =============================================================================
# FINAL VERDICT
# =============================================================================
echo ""
echo "=============================================="
echo "ALL 9 CHECKS PASSED"
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
    echo "REQUIREMENTS exit verification FAILED"
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
   - Check 5: Missing agent worktrees -> return to CLASSIFIED
   - Check 6-7: Incomplete agents -> use partial completion recovery procedures
   - Check 8: State sequence violated -> protocol error, escalate
   - Check 9: Agent errors -> re-invoke failed agents
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
   - Compare architect vs performance requirements
   - Resolve style vs quality conflicts
   - Balance security vs usability trade-offs

2. ARCHITECTURE PLANNING: Design approach satisfying all constraints
   - Document chosen architectural patterns
   - Specify integration points between components
   - Define interface contracts and data flows

3. REQUIREMENT MAPPING: Document how each stakeholder requirement is addressed
   - Map each architect requirement to implementation component
   - Map each security requirement to security control
   - Map each performance requirement to optimization strategy

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

CRITICAL COMMIT PATTERN VIOLATION: Never create separate commits for todo.md updates
MANDATORY: todo.md task completion update MUST be committed with task deliverables in single atomic commit
ANTI-PATTERN: git commit deliverables -> git commit todo.md (VIOLATES PROTOCOL)
CORRECT PATTERN: git add deliverables todo.md -> git commit (single atomic commit)
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

---

## IMPLEMENTATION State Entry Guards (7-Guard Verification) {#implementation-state-entry-guards-critical-enforcement}

**PURPOSE**: Prevent protocol violations by verifying ALL prerequisite artifacts exist before entering
IMPLEMENTATION state.

**MANDATORY VERIFICATION SCRIPT**:
```bash
#!/bin/bash
# verify-implementation-entry.sh - Gate enforcement before SYNTHESIS -> IMPLEMENTATION

TASK_NAME="${1}"
TASK_DIR="/workspace/tasks/${TASK_NAME}"
TASK_MD="${TASK_DIR}/task.md"
LOCK_FILE="${TASK_DIR}/task.json"

echo "=== IMPLEMENTATION STATE ENTRY VERIFICATION ==="

# Guard 1: Verify task.md exists
if [ ! -f "$TASK_MD" ]; then
  echo "ENTRY GUARD FAILED: task.md not found"
  echo "   Expected: ${TASK_MD}"
  echo "   CAUSE: CLASSIFIED state did not create task.md"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "Guard 1 PASSED: task.md exists"

# Guard 2: Verify task.md contains Stakeholder Agent Reports section
if ! grep -q "## Stakeholder Agent Reports" "$TASK_MD"; then
  echo "ENTRY GUARD FAILED: task.md missing Stakeholder Agent Reports section"
  echo "   CAUSE: task.md created with incomplete structure"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "Guard 2 PASSED: task.md has Stakeholder Agent Reports section"

# Guard 3: Verify at least one requirements report exists
REQUIREMENTS_REPORTS=$(find "${TASK_DIR}" -maxdepth 1 -name "*-requirements.md" 2>/dev/null | wc -l)
if [ "$REQUIREMENTS_REPORTS" -eq 0 ]; then
  echo "ENTRY GUARD FAILED: No stakeholder requirements reports found"
  echo "   Expected: At least one ${TASK_DIR}/*-requirements.md file"
  echo "   CAUSE: REQUIREMENTS state did not produce any agent reports"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "Guard 3 PASSED: Found ${REQUIREMENTS_REPORTS} stakeholder requirements reports"

# Guard 4: Verify task.md contains implementation plans section
if ! grep -q -i "implementation plan" "$TASK_MD"; then
  echo "ENTRY GUARD FAILED: task.md missing implementation plans"
  echo "   CAUSE: SYNTHESIS state did not append implementation plans to task.md"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "Guard 4 PASSED: task.md contains implementation plans"

# Guard 5: Verify user approval checkpoint flag exists
USER_APPROVAL_FLAG="${TASK_DIR}/user-plan-approval-obtained.flag"
if [ ! -f "$USER_APPROVAL_FLAG" ]; then
  echo "ENTRY GUARD FAILED: User plan approval not obtained"
  echo "   Expected: ${USER_APPROVAL_FLAG}"
  echo "   CAUSE: SYNTHESIS state did not wait for user approval of implementation plan"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state without user approval"
  exit 1
fi
echo "Guard 5 PASSED: User plan approval obtained"

# Guard 6: Verify lock file shows complete state progression
CURRENT_STATE=$(jq -r '.state' "$LOCK_FILE" 2>/dev/null)
if [ "$CURRENT_STATE" != "SYNTHESIS" ]; then
  echo "ENTRY GUARD FAILED: Lock state is '${CURRENT_STATE}', expected 'SYNTHESIS'"
  echo "   CAUSE: Attempting to skip states or transition out of order"
  echo "   ABORT: Cannot proceed to IMPLEMENTATION state"
  exit 1
fi
echo "Guard 6 PASSED: Lock file shows SYNTHESIS state (ready for IMPLEMENTATION)"

echo ""
echo "=============================================="
echo "ALL ENTRY GUARDS PASSED"
echo "=============================================="
echo ""

# Guard 7: Verify main agent understanding of delegation pattern
echo "Guard 7: IMPLEMENTATION STATE DELEGATION VERIFICATION"
echo ""
echo "CRITICAL REMINDER: Main coordination agent role during IMPLEMENTATION state"
echo ""
echo "**BEFORE ANY ACTION in IMPLEMENTATION state**:"
echo "- [ ] Read this reminder: 'I will NOT use Edit/Write on .java files'"
echo "- [ ] Read this reminder: 'I will NOT use Edit/Write on pom.xml'"
echo "- [ ] Read this reminder: 'I will NOT use Edit/Write on module-info.java'"
echo "- [ ] Verify: All implementation will be via Task tool invocations"
echo "- [ ] Verify: My role is coordination ONLY during this state"
echo ""
echo "YOUR ROLE:"
echo "   - Invoke stakeholder agents via Task tool"
echo "   - Monitor agent status.json files"
echo "   - Coordinate merge ordering and conflict resolution"
echo "   - Determine when all agents complete their work"
echo ""
echo "PROHIBITED:"
echo "   - Creating implementation files directly in task worktree"
echo "   - Writing code yourself instead of delegating to agents"
echo "   - Modifying src/main/java or src/test/java directly"
echo "   - Editing pom.xml for dependency changes"
echo "   - Editing module-info.java for JPMS configuration"
echo "   - Fixing compilation errors directly"
echo ""
echo "CORRECT WORKFLOW:"
echo "1. Invoke agents in parallel via Task tool"
echo "2. Wait for all agents to complete Round 1"
echo "3. Check status.json files for completion"
echo "4. If work remains -> invoke agents for Round 2"
echo "5. If all complete -> transition to VALIDATION"
echo ""

echo "Guard 7 PASSED: Delegation pattern verification complete"
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
1. **Automatically**: By pre-transition hooks before SYNTHESIS -> IMPLEMENTATION
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

### IMPLEMENTATION State Audit Trail Requirements (MANDATORY) {#implementation-state-audit-trail-requirements-mandatory}

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
      "agent_name": "architect",
      "agent_mode": "requirements",
      "working_dir": "/workspace/tasks/{task-name}/code/",
      "invocation_timestamp": "2025-10-19T10:05:00Z",
      "completion_timestamp": "2025-10-19T10:10:00Z",
      "report_location": "/workspace/tasks/{task-name}/task-name-architect-requirements.md",
      "status": "completed"
    }
  ],
  "tool_usage": [
    {
      "tool": "Task",
      "agent_name": "architect",
      "timestamp": "2025-10-19T10:15:00Z",
      "working_dir": "/workspace/tasks/{task-name}/agents/architect/code/"
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
      "agent": "architect",
      "branch": "task-branch",
      "timestamp": "2025-10-19T10:18:00Z",
      "files_created": ["FormattingRule.java"],
      "files_modified": [],
      "message": "[agent:architect] Add FormattingRule interface"
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

---

## SYNTHESIS -> IMPLEMENTATION {#synthesis-implementation}

**REFERENCE**: See [Detailed Implementation Plan Requirements](task-protocol-core.md#detailed-implementation-plan-requirements)
for the complete specification of what implementation plans must contain.

**Mandatory Conditions (Prerequisites):**
1. Requirements synthesis document created (all agents contributed to task.md)
2. Architecture plan addresses all stakeholder requirements
3. Conflict resolution documented for competing requirements (architect makes final decisions based on tier hierarchy)
4. **DETAILED PLAN**: Implementation plan contains ALL required components:
   - File manifest (all files to create/modify with paths)
   - API specifications (exact method signatures with JavaDoc)
   - Behavioral specifications (scenario -> behavior tables)
   - Test specifications (exact test cases to implement)
   - Implementation sequence (ordered phases with dependencies)
   - Decision log (all design decisions already made)
5. **USER APPROVAL: Detailed plan presented to user for approval**
6. **USER CONFIRMATION: User has approved the specific implementation approach**

### MANDATORY USER APPROVAL CHECKPOINT

**PROHIBITION**: Main agent MUST NOT automatically transition from SYNTHESIS to IMPLEMENTATION state.

**PLAN DETAIL REQUIREMENT**: The implementation plan must be detailed enough that:
- User knows EXACTLY what files will be created/modified
- User knows EXACTLY what method signatures will exist
- User knows EXACTLY how the code will behave in each scenario
- User knows EXACTLY what tests will be written
- NO significant design decisions remain for implementation phase

**REQUIRED BEHAVIOR**:
1. After completing SYNTHESIS (detailed implementation plan written to task.md), main agent MUST HALT
2. Main agent MUST present plan summary using the format below
3. Main agent MUST WAIT for explicit user approval before transitioning to IMPLEMENTATION
4. ONLY transition to IMPLEMENTATION after receiving approval keywords ("approved", "proceed")

**EXAMPLE - CORRECT DETAILED APPROVAL REQUEST**:
```markdown
## Implementation Plan Complete

**Task**: implement-multi-config-architecture
**Risk Level**: MEDIUM
**Estimated Changes**: 2 files modified, 1 method added, ~30 lines

### What Will Be Built
Multi-configuration architecture allowing each formatting rule to receive all configurations and find
its own type via a shared helper method. This fixes CLI failures where a single config was passed to
all rules.

### Files to Modify
| File | Changes |
|------|---------|
| `formatter/.../FormattingConfiguration.java` | Add static `findConfig()` helper method |
| `formatter/.../FormattingRule.java` | Change `analyze()` and `format()` to accept `List<FormattingConfiguration>` |
| `formatter/.../BraceFormattingRule.java` | Use `findConfig()` to get BraceFormattingConfiguration |
| `formatter/.../LineLengthFormattingRule.java` | Use `findConfig()` to get LineLengthConfiguration |
| (3 more rules) | Same pattern |
| `pipeline/.../FormatStage.java` | Pass config list to rules |
| `cli/.../CliMain.java` | Create `List<FormattingConfiguration>` |
| (19 test files) | Update to use `List.of(config)` |

### Key API Signature
```java
static <T extends FormattingConfiguration> T findConfig(
    List<FormattingConfiguration> configs,
    Class<T> configType,
    Supplier<T> defaultSupplier)
```

### Behavioral Summary
| Scenario | Behavior |
|----------|----------|
| Matching config in list | Returns that config |
| No matching config | Returns default from supplier |
| Multiple same type | Throws IllegalArgumentException |
| Null or empty list | Returns default from supplier |

### Implementation Phases
1. **Phase 1** (architect): Add `findConfig()` to FormattingConfiguration
2. **Phase 2** (engineer): Update FormattingRule interface signatures
3. **Phase 3** (engineer): Update all 5 rule implementations
4. **Phase 4** (engineer): Update pipeline FormatStage
5. **Phase 5** (engineer): Update CLI to create config list
6. **Phase 6** (tester): Update all 19 test files

---

**Full implementation details**: `/workspace/tasks/implement-multi-config-architecture/task.md`

**Please review and respond with:**
- "approved" / "proceed" to begin implementation
- Or provide feedback for plan revisions
```

**PROHIBITED PATTERNS**:
- Automatic transition from SYNTHESIS -> IMPLEMENTATION without user interaction
- Assuming "continue" or "proceed" from earlier messages means plan approval
- Treating bypass mode as approval checkpoint override
- Proceeding because requirements are clear or plan is straightforward

**ENFORCEMENT**: The check-lock-ownership.sh hook enforces this checkpoint at SessionStart.

**Implementation Plan Clarity Requirements**:

When writing implementation plans in task.md during SYNTHESIS state, use EXPLICIT DELEGATION language:

**WHO IMPLEMENTS**:
- CORRECT: "architect agent implements core interfaces in /workspace/tasks/{task}/agents/architect/code"
- CORRECT: "Main agent invokes architect via Task tool -> architect implements Phase 1-2"
- CORRECT: "Stakeholder agents implement in parallel (main agent coordinates via Task tool)"
- AMBIGUOUS: "architect implements Phase 1-2" (who invokes? where does implementation occur?)
- PROHIBITED: "Main agent implements Phase 1-2 following architect requirements"
- PROHIBITED: "I will implement the feature based on the approved plan"

**CRITICAL RULE**: Any implementation activity means:
1. Main agent uses Task tool to invoke stakeholder agent
2. Stakeholder agent implements in `/workspace/tasks/{task}/agents/{agent}/code/`
3. Stakeholder agent validates, then merges to task branch

**NEXT ACTION AFTER USER APPROVAL**:

The ONLY acceptable next action after user approves SYNTHESIS is:

```markdown
"I am now entering IMPLEMENTATION state. Launching stakeholder agents in parallel for domain-specific implementation.

Task tool (architect), model: haiku, prompt: "Implement core architecture per requirements"
Task tool (quality), model: haiku, prompt: "Apply design patterns per requirements"
Task tool (style), model: haiku, prompt: "Ensure code follows style guidelines per requirements"
```

**VIOLATION PATTERNS** (never say these after SYNTHESIS approval):
- "I will now implement the feature..."
- "Let me create the core classes..."
- "I'll start by implementing..."
- "First, I'll write the implementation..."

**If you catch yourself saying "I will implement"**, STOP and rephrase to "I will coordinate stakeholder
agents to implement".
