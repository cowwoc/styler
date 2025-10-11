# Task State Machine Protocol - Operations & Procedures

**IMPORTANT**: This document is Part 2 of the Task Protocol. Read [task-protocol-core.md](task-protocol-core.md) first for:
- State machine architecture and definitions
- Risk-based agent selection
- Workflow variants by risk level
- Agent selection decision tree
- Complete style validation framework
- Batch processing and continuous mode
- Mandatory state transitions (INIT through SCOPE_NEGOTIATION)

This document contains:
- Completion and cleanup procedures
- Validation functions
- Agent interaction protocols
- Error handling & recovery
- Compliance verification
- Tool-specific optimizations
- Workflow execution engine

---

### COMPLETE → CLEANUP
**Conditions**: All work committed (from USER REVIEW checkpoint) | todo.md removed + added to changelog.md | Dependent tasks updated (BLOCKED → READY if deps satisfied) | todo.md/changelog.md changes amended to commit | Build passes in worktree before merge (`./mvnw verify`) | Task branch merged to main with LINEAR HISTORY (fast-forward only, NO merge commits) | Full build passes on main

**Evidence**: Pre-merge build success | Git log shows linear history (no merge commits) | todo.md in commit (`git show --stat | grep "todo.md"`) | Main branch build success | All deliverables preserved

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


**CRITICAL VALIDATION: todo.md Commit Verification**
```bash
# MANDATORY: Verify todo.md is in the commit BEFORE merging to main
git show --stat | grep "todo.md" || { echo "❌ VIOLATION: todo.md not in commit"; exit 1; }
git show --name-only | grep "todo.md" || { echo "❌ VIOLATION: todo.md not modified"; exit 1; }
```

**Git Safety Sequence (Concurrency-Safe, Linear History):**
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

# Step 3: Archive task (CLAUDE.md policy)
# 3a-c: Remove from todo.md, add to changelog.md (## YYYY-MM-DD) with completion details
# 3d: Update dependent tasks: BLOCKED → READY if ALL dependencies in changelog.md
# 3e: Amend commit with todo.md and changelog.md
git add todo.md changelog.md
git commit --amend --no-edit

# Step 4: Verify todo.md and changelog.md are included in the commit
git show --stat | grep "todo.md" || { echo "ERROR: todo.md not in commit"; exit 1; }
git show --stat | grep "changelog.md" || { echo "ERROR: changelog.md not in commit"; exit 1; }

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

# Step 7: Atomic push (CONCURRENCY-SAFE via git internal locking)
# Main worktree: receive.denyCurrentBranch=updateInstead
# Push updates ref + working tree atomically
git push /workspace/branches/main/code HEAD:refs/heads/main || {
    echo "❌ Push failed - another instance merged first"
    echo "Solution: Fetch, rebase, and retry"
    git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
    git rebase origin/main
    # Retry after rebase
    git push /workspace/branches/main/code HEAD:refs/heads/main || exit 1
}

# Step 8: Post-merge verification
git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5  # Verify our commit at HEAD
git show origin/main --stat | grep "todo.md" || { echo "ERROR: todo.md missing"; exit 1; }

# Step 9: OPTIONAL - Verify build on main (skip if Step 6 passed, main worktree auto-updated)
```

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

# Step 2: Changes already committed during USER REVIEW checkpoint
# Verify commit exists
git log -1 --oneline

# Step 3: Archive task + update dependent tasks (todo.md → changelog.md, BLOCKED → READY if deps met)
git add todo.md changelog.md
git commit --amend --no-edit

# Step 4: Verify todo.md and changelog.md included
git show --stat | grep "todo.md"
git show --stat | grep "changelog.md"

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

**Fast-Forward Merge Validation** (`git log --graph --oneline`):
✅ **CORRECT** (linear): `* 7f8deba (HEAD -> main) Implement CLI security` then `* 6a2b1c3 Previous commit`
❌ **WRONG** (merge commit): `*   8g9h5i6 Merge branch 'task'` with `|\` graph showing non-linear history

### DEPENDENT TASK UPDATE PROCEDURE
**When**: Step 3d of COMPLETE → CLEANUP (after archiving to changelog.md)
```bash
# Helper function to update dependent tasks in todo.md
update_dependent_tasks() {
    local completed_task=$1
    local todo_file="todo.md"
    local changelog_file="changelog.md"

    echo "=== UPDATING DEPENDENT TASKS ==="
    echo "Completed task: $completed_task"

    # Find all BLOCKED tasks in todo.md
    grep -n "BLOCKED:" "$todo_file" | while IFS=: read line_num task_entry; do
        # Extract task name from the entry
        task_name=$(echo "$task_entry" | grep -oP '`\K[^`]+' | head -1)

        # Find the dependencies for this task
        # Dependencies are listed on a line like: "  - **Dependencies**: A0, A1, A2"
        dependencies=$(sed -n "${line_num},/^$/p" "$todo_file" | grep "Dependencies:" | sed 's/.*Dependencies: *//' | sed 's/ *$//')

        if [ -z "$dependencies" ]; then
            continue
        fi

        echo "Checking task: $task_name"
        echo "  Dependencies: $dependencies"

        # Check if ALL dependencies are satisfied (in changelog.md)
        all_satisfied=true
        IFS=',' read -ra DEPS <<< "$dependencies"
        for dep in "${DEPS[@]}"; do
            # Trim whitespace
            dep=$(echo "$dep" | xargs)

            # Check if dependency is in changelog.md (completed)
            if ! grep -q "$dep" "$changelog_file"; then
                echo "  ❌ Dependency $dep not yet complete"
                all_satisfied=false
                break
            else
                echo "  ✅ Dependency $dep complete"
            fi
        done

        # If all dependencies satisfied, update task status from BLOCKED to READY
        if [ "$all_satisfied" = true ]; then
            echo "  🔓 All dependencies satisfied - updating to READY"
            # Use sed to change BLOCKED to READY on the specific line
            sed -i "${line_num}s/BLOCKED:/READY:/" "$todo_file"
            echo "  ✅ Task $task_name updated to READY"
        else
            echo "  ⏳ Task $task_name remains BLOCKED"
        fi
    done

    echo "=== DEPENDENT TASK UPDATE COMPLETE ==="
}

# Usage in Step 3d:
update_dependent_tasks "implement-index-overlay-parser"
```

**Example**: After completing A1, check changelog.md for A0 ✅ and A1 ✅ → Both satisfied → Update `BLOCKED:` tasks to `READY:`

**Validation:**
```bash
# Verify dependent tasks were updated
grep "implement-line-length-formatter" todo.md | grep -q "READY:" && echo "✅ Task unblocked" || echo "❌ Task still blocked"
```

### AUTOMATIC CONFLICT RESOLUTION VIA ATOMIC OPERATIONS
**Scenarios**: Both attempt same task (`mkdir` fails for 2nd → lock release, select alternative) | Lock conflict (`set -C` prevents overwrite → select alternative) | Directory exists (lock created but `mkdir` fails → cleanup, select alternative)
**Safety**: `set -C` prevents overwriting locks | `mkdir` (not `-p`) fails atomically | Error handlers release locks | Existing worktrees never deleted

---

### CLEANUP (Final State)
**Conditions**: Task lock released | Worktree removed safely | Task branch deleted | Temporary files cleaned | `state.json` archived
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing

# Step 1: Verify work preserved (can check from task worktree)
cd /workspace/branches/{TASK_NAME}/code
git fetch /workspace/branches/main/code refs/heads/main:refs/remotes/origin/main
git log origin/main --oneline -5 | grep "{TASK_NAME}" || {
    echo "❌ FATAL: Work not found in main branch"
    exit 1
}

# Cleanup with session verification
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/locks/{TASK_NAME}.json")
[ "$LOCK_OWNER" = "$SESSION_ID" ] || { echo "❌ FATAL: Lock owned by $LOCK_OWNER"; exit 1; }
rm -f /workspace/locks/{TASK_NAME}.json

# CRITICAL: cd to main BEFORE removing worktree (fails if inside worktree being removed)
[[ "$(pwd)" == "/workspace/branches/{TASK_NAME}/code"* ]] && { echo "❌ FATAL: Inside worktree"; exit 1; } || cd /workspace/branches/main/code

git worktree remove /workspace/branches/{TASK_NAME}/code --force
git branch -d {TASK_NAME}
rm -rf /workspace/branches/{TASK_NAME}

# Cleanup temp files
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

# Cleanup with verification
SESSION_ID="${CURRENT_SESSION_ID}"
LOCK_OWNER=$(grep -oP '"session_id":\s*"\K[^"]+' "/workspace/locks/refactor-line-wrapping-architecture.json")
[ "$LOCK_OWNER" = "$SESSION_ID" ] || { echo "❌ FATAL: Lock owned by $LOCK_OWNER"; exit 1; }
rm -f /workspace/locks/refactor-line-wrapping-architecture.json

[[ "$(pwd)" == "/workspace/branches/refactor-line-wrapping-architecture/code"* ]] && { echo "❌ FATAL: Inside worktree"; exit 1; } || cd /workspace/branches/main/code

git worktree remove /workspace/branches/refactor-line-wrapping-architecture/code --force
git branch -d refactor-line-wrapping-architecture
rm -rf /workspace/branches/refactor-line-wrapping-architecture
```

## TRANSITION VALIDATION FUNCTIONS
### Universal Validation Requirements (ALL transitions)

```python
def validate_transition_conditions(from_state, to_state, evidence, task_context):
    """Validate mandatory conditions and evidence for transition"""
    required_conditions = TRANSITION_MAP[from_state][to_state]['conditions']
    required_evidence = TRANSITION_MAP[from_state][to_state]['evidence']

    for condition in required_conditions:
        if not validate_condition(condition, evidence, task_context):
            return False, f"Condition failed: {condition}"

    for evidence_type in required_evidence:
        if not validate_evidence(evidence_type, evidence):
            return False, f"Missing evidence: {evidence_type}"

    return True, "All transition conditions satisfied"
```

### State Enforcement Functions

```bash
update_task_state() {
    local new_state=$1 evidence_json=$2
    current_state=$(jq -r '.current_state' state.json 2>/dev/null || echo "INIT")
    validate_state_transition "$current_state" "$new_state" || { echo "ERROR: Invalid transition $current_state→$new_state"; exit 1; }

    jq --arg state "$new_state" --argjson evidence "$evidence_json" --arg timestamp "$(date -Iseconds)" \
       '.current_state = $state | .evidence += $evidence | .transition_log += [{"from": .current_state, "to": $state, "timestamp": $timestamp}]' \
       state.json > state.json.tmp && mv state.json.tmp state.json
}

validate_state_transition() {
    case "$1:$2" in
        # Core: INIT→CLASSIFIED→REQUIREMENTS→SYNTHESIS→CONTEXT→AUTONOMOUS_IMPLEMENTATION→CONVERGENCE→VALIDATION→REVIEW→COMPLETE→CLEANUP
        "INIT:CLASSIFIED"|"CLASSIFIED:REQUIREMENTS"|"REQUIREMENTS:SYNTHESIS"|"SYNTHESIS:CONTEXT"|"CONTEXT:AUTONOMOUS_IMPLEMENTATION"|"AUTONOMOUS_IMPLEMENTATION:CONVERGENCE"|"CONVERGENCE:VALIDATION"|"VALIDATION:REVIEW"|"REVIEW:COMPLETE"|"COMPLETE:CLEANUP") return 0 ;;
        # Scope negotiation: REVIEW→SCOPE_NEGOTIATION→SYNTHESIS/COMPLETE
        "REVIEW:SCOPE_NEGOTIATION"|"SCOPE_NEGOTIATION:SYNTHESIS"|"SCOPE_NEGOTIATION:COMPLETE") return 0 ;;
        # Skip for low/medium risk
        "SYNTHESIS:COMPLETE"|"CLASSIFIED:COMPLETE"|"REQUIREMENTS:COMPLETE") return 0 ;;
        # Resolution cycle
        "VALIDATION:SYNTHESIS") return 0 ;;
        *) return 1 ;;
    esac
}
```

## AGENT INTERACTION PROTOCOLS

### Parallel Agent Invocation (Requirements)
```python
agent_prompt_template = """
Task: {task_description} | Mode: REQUIREMENTS_ANALYSIS | State: REQUIREMENTS → SYNTHESIS pending
Domain: {agent_domain} | Risk: {risk_level}

SCOPE ENFORCEMENT: Only ../context.md scope files. STOP on unauthorized access.
PERSISTENCE: Apply CLAUDE.md "Solution Quality Hierarchy" - OPTIMAL over EXPEDIENT.
OUTPUT: Complete requirements IN RESPONSE (not files). End: "REQUIREMENTS COMPLETE: {agent_domain}"
EVIDENCE: Document requirements, success criteria, constraints, quality thresholds.
"""

def invoke_requirements_agents(required_agents, task_context):
    return execute_parallel_agents([{
        'subagent_type': agent,
        'description': f'{agent} requirements',
        'prompt': agent_prompt_template.format(agent_domain=agent.replace('-', ' '), **task_context)
    } for agent in required_agents])
```

### Review Agent Invocation
```python
review_prompt_template = """
Task: {task_description} | Mode: FINAL_SYSTEM_REVIEW | State: REVIEW → COMPLETE pending
Requirements: {requirements_file} | Implementation: {modified_files}

PERSISTENCE VALIDATION: CLAUDE.md "Solution Quality Indicators". Reject if functional but not optimal (when achievable).
DECISION FORMAT (required):
  "FINAL DECISION: ✅ APPROVED - All {domain} requirements satisfied"
  OR "FINAL DECISION: ❌ REJECTED - [specific issues]"
NO OTHER FORMAT ACCEPTED. Determines workflow continuation.
"""
```

## ERROR HANDLING & RECOVERY

### Transition Failure Recovery
```python
def handle_transition_failure(current_state, attempted_state, failure_reason):
    """Handle failed transitions with recovery actions"""
    recovery_actions = {
        'REQUIREMENTS': {'missing_agent_reports': 're_invoke_missing_agents', 'incomplete_analysis': 'request_complete_analysis', 'scope_violations': 'enforce_scope_boundaries'},
        'REVIEW': {'agent_rejection': 'return_to_synthesis_cycle', 'malformed_decision': 'request_proper_format', 'missing_decisions': 're_invoke_agents'},
        'VALIDATION': {'build_failure': 'return_to_implementation', 'test_failure': 'fix_tests_and_retry', 'quality_gate_failure': 'address_quality_issues'}
    }
    action = recovery_actions.get(current_state, {}).get(failure_reason, 'restart_from_synthesis')
    execute_recovery_action(action, current_state, attempted_state, failure_reason)
```

### Multi-Instance Coordination
```bash
handle_lock_conflict() {
    local task_name=$1
    echo "Lock conflict: $task_name. Checking alternatives..."

    available_tasks=$(grep -E "^[[:digit:]]+\." /workspace/branches/main/code/todo.md | head -10 | while read line; do
        task=$(echo "$line" | sed 's/^[[:digit:]]*\. *//')
        task_slug=$(echo "$task" | tr ' ' '-' | tr '[:upper:]' '[:lower:]')
        [ ! -f "../../../locks/${task_slug}.json" ] && { echo "$task_slug"; break; }
    done)

    [ -n "$available_tasks" ] && { echo "Selected: $available_tasks"; execute_task_protocol "$available_tasks"; } || { echo "No tasks available"; exit 1; }
}
```

## COMPLIANCE VERIFICATION

### Pre-Task Validation (MANDATORY before ANY task)
```bash
pre_task_validation() {
    echo "=== PRE-TASK VALIDATION ==="
    [ -n "$SESSION_ID" ] && [ "$SESSION_ID" != "REPLACE_WITH_ACTUAL_SESSION_ID" ] || { echo "ERROR: Invalid session ID"; exit 1; }
    [ "$(pwd)" = "/workspace/branches/main/code" ] || { echo "ERROR: Wrong directory"; exit 1; }
    [ -f "todo.md" ] || { echo "ERROR: todo.md not accessible"; exit 1; }
    echo "✅ Pre-task validation complete"
}
```

### Continuous Compliance Monitoring (after each transition)
```bash
post_transition_validation() {
    local new_state=$1
    echo "=== POST-TRANSITION VALIDATION: $new_state ==="
    current_state=$(jq -r '.current_state' state.json 2>/dev/null || echo "ERROR")
    [ "$current_state" = "$new_state" ] || { echo "ERROR: State file not updated"; exit 1; }
    validate_state_evidence "$new_state" || { echo "ERROR: Missing evidence for $new_state"; exit 1; }
    echo "✅ Post-transition validation complete"
}
```

### Final Compliance Audit (MANDATORY before CLEANUP)
```bash
final_compliance_audit() {
    echo "=== FINAL COMPLIANCE AUDIT ==="
    state_sequence=$(jq -r '.transition_log[] | .to' state.json | tr '\n' ' ')

    # Valid sequences: high/medium (full), low (streamlined), scope negotiation (with resolution)
    valid_patterns=("CLASSIFIED REQUIREMENTS SYNTHESIS CONTEXT AUTONOMOUS_IMPLEMENTATION CONVERGENCE VALIDATION REVIEW COMPLETE" \
                    "CLASSIFIED REQUIREMENTS SYNTHESIS COMPLETE" \
                    "CLASSIFIED REQUIREMENTS SYNTHESIS.*SCOPE_NEGOTIATION.*COMPLETE")

    valid_sequence=false
    for pattern in "${valid_patterns[@]}"; do
        [[ "$state_sequence" =~ $pattern ]] && { valid_sequence=true; echo "✅ Valid: $pattern"; break; }
    done
    [ "$valid_sequence" = "true" ] || { echo "ERROR: Invalid sequence: $state_sequence"; exit 1; }

    # Verify unanimous approval
    approval_count=$(jq -r '.evidence.REVIEW | to_entries[] | select(.value == "APPROVED") | .key' state.json | wc -l)
    required_count=$(jq -r '.required_agents | length' state.json)
    [ "$approval_count" -eq "$required_count" ] || { echo "ERROR: Approvals: $approval_count/$required_count"; exit 1; }

    # Verify work preserved
    git log --oneline -5 | grep -i "$(jq -r '.task_name' state.json)" || { echo "ERROR: Work not in git"; exit 1; }
    echo "✅ Final compliance audit passed"
}
```

## VIOLATION PREVENTION PATTERNS

### Pre-Task Validation Block (MANDATORY before ANY task)
```bash
export SESSION_ID="..." && [ -n "$SESSION_ID" ] && [ "$SESSION_ID" != "REPLACE_WITH_ACTUAL_SESSION_ID" ] && echo "SESSION_ID_VALID: $SESSION_ID" || (echo "SESSION_ID_INVALID" && exit 1)
[ "$(pwd)" = "/workspace/branches/main/code" ] || (echo "ERROR: Wrong directory" && exit 1)
[ -f "todo.md" ] || (echo "ERROR: todo.md not accessible" && exit 1)
```

### Mandatory Context.md Creation (BEFORE stakeholder consultation)
```markdown
# Task Context: {task-name}
## Task Objective: {task-description}
## Scope Definition
**IN SCOPE**: [exact files/dirs for stakeholder analysis]
**OUT OF SCOPE**: [excluded files/dirs]
## Stakeholder Agent Reports
**Requirements**: technical-architect-requirements.md, [other-agent]-requirements.md (when completed)
**Reviews**: technical-architect-review1.md, [other-agent]-review1.md (when completed)
## Implementation Status
- [ ] INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE → VALIDATION → REVIEW → COMPLETE → CLEANUP
```

### Atomic Lock Acquisition
```bash
export SESSION_ID="..." && mkdir -p ../../../locks && (set -C; echo '{"session_id": "'${SESSION_ID}'", "start_time": "'$(date '+%Y-%m-%d %H:%M:%S %Z')'"}' > ../../../locks/{task-name}.json) 2>/dev/null && echo "LOCK_SUCCESS" || echo "LOCK_FAILED"
[[ "$lock_result" != *"LOCK_SUCCESS"* ]] && { echo "Lock failed - selecting alternative"; select_alternative_task; }
```

### Temporary File Management (BEFORE AUTONOMOUS IMPLEMENTATION, all tasks)
```bash
TASK_NAME=$(basename $(dirname $(pwd)))
TEMP_DIR="/tmp/task-${TASK_NAME}-$(date +%s)-$$" && mkdir -p "$TEMP_DIR"
echo "$TEMP_DIR" > .temp_dir && echo "TEMP_SETUP_SUCCESS: $TEMP_DIR"

# PURPOSE: Agents use consistent temp location outside git (read: TEMP_DIR=$(cat .temp_dir 2>/dev/null || echo "/tmp/fallback-$$"))
# AGENT USAGE: Analysis scripts, benchmarking, security testing payloads, debug logs, test data, mock objects
[[ "$temp_result" != *"TEMP_SETUP_SUCCESS"* ]] && { echo "WARNING: Fallback"; TEMP_DIR=$(pwd)/.temp_fallback && mkdir -p "$TEMP_DIR"; }
```

### Implementation Safety Guards (BEFORE Write/Edit/MultiEdit)
```bash
pwd | grep -q "/workspace/branches/.*/code$" || (echo "ERROR: Invalid working directory" && exit 1)
git status --porcelain | grep -E "(dist/|node_modules/|target/|\.jar$)" && (echo "ERROR: Prohibited files" && exit 1)
```

### Build Validation Gates (MANDATORY after implementation)
```bash
./mvnw verify -q || (echo "ERROR: Build/test/linter failure" && exit 1)  # Runs: compile → test → checkstyle → PMD
```

### Decision Parsing Enforcement (AFTER ANY agent invocation)
```bash
decision=$(echo "$agent_response" | grep "FINAL DECISION:")
[[ -z "$decision" ]] && retry_agent_with_format_requirement
[[ "$decision" == *"❌ REJECTED"* ]] && handle_agent_rejection
[[ "$decision" == *"✅ APPROVED"* ]] && continue_to_next_transition
```

### Protocol Violation Reporting
**MAINTAIN THROUGHOUT EXECUTION**: Track format violations in TodoWrite (agent type, state, violation type, retry outcome) → Include in final task summary

**REPORT FORMAT**: `Protocol Violations: [Agent]:[State] - Missing decision format (resolved) | Total: X, Auto-resolved: Y, Manual review: Z`

**PURPOSE**: Identify agent prompt issues, track compliance trends, enable improvements, audit trail

**IMPLEMENTATION**: `violation_tracking = {"agent_name": {"state": "REVIEW", "violation_type": "missing_decision_format", "retry_outcome": "resolved", "timestamp": "..."}}`

## CONTEXT PRESERVATION RULES

### Single Session Continuity
**Requirements**: All execution in single session | Two mandatory checkpoints (After SYNTHESIS: plan approval, After REVIEW: change review) | Complete other phases without waiting | Session interruption → Restart with new session

### Tool Call Batching
```python
parallel_tool_calls = [{"tool": "Task", "agent": "technical-architect", "mode": "requirements"},
                        {"tool": "Task", "agent": "style-auditor", "mode": "requirements"},
                        {"tool": "Task", "agent": "code-quality-auditor", "mode": "requirements"}]
execute_parallel_tools(parallel_tool_calls)  # Single message reduces context fragmentation
```

### State Persistence
**Track**: state.json, SESSION_ID, working directory, lock status
```bash
current_state=$(jq -r '.current_state' state.json)
[ "$current_state" = "$1" ] || (echo "ERROR: State inconsistency" && exit 1)
```

## TOOL-SPECIFIC OPTIMIZATION PATTERNS

### Bash Tool
```bash
command1 && echo "SUCCESS" || (echo "FAILED" && exit 1)
result=$(command_with_output); [ -n "$result" ] || (echo "ERROR: No output" && exit 1)
# ❌ git rebase -i (interactive) | ✅ git rebase main (non-interactive)
```

### Read Tool
```python
related_files = ["/path/to/config.md", "/path/to/implementation.java", "/path/to/test.java"]
for file_path in related_files:
    analyze_content(read_tool(file_path))  # Batch reads in sequence
```

### Task Tool
```python
agent_calls = [{'subagent_type': 'technical-architect', 'description': 'Architecture requirements', 'prompt': structured_prompt_template.format(domain='architecture')},
               {'subagent_type': 'style-auditor', 'description': 'Style requirements', 'prompt': structured_prompt_template.format(domain='style')}]
execute_parallel_agents(agent_calls)  # Parallel execution in single message
```

### TodoWrite Tool
```python
update_todo_progress("requirements", "in_progress", "Gathering stakeholder requirements")
todo_item = {"content": "Implement feature X with requirements Y", "status": "in_progress", "activeForm": "Implementing feature X..."}
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
                # Present implementation plan and wait for user approval
                present_implementation_plan_and_wait_for_approval

                # Conditional path selection based on risk level and change type
                if should_skip_implementation "$risk_level" "$change_type"; then
                    execute_synthesis_to_complete_transition
                else
                    execute_synthesis_to_context_transition
                fi
                ;;
            "CONTEXT")
                # Delegated Protocol: Generate context for autonomous implementation
                execute_context_to_autonomous_implementation_transition
                ;;
            "AUTONOMOUS_IMPLEMENTATION")
                # Delegated Protocol: Agents implement in parallel
                execute_autonomous_implementation_to_convergence_transition
                ;;
            "CONVERGENCE")
                # Delegated Protocol: Iterative integration until consensus
                execute_convergence_to_validation_transition
                ;;
            "VALIDATION")
                execute_validation_to_review_transition
                ;;
            "REVIEW")
                execute_review_to_complete_or_scope_negotiation_transition

                # If unanimous approval achieved, present changes and wait for user review
                if all_agents_approved; then
                    present_changes_and_wait_for_user_approval
                fi
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

# Helper functions for user approval checkpoints
present_implementation_plan_and_wait_for_approval() {
    echo "=== USER APPROVAL CHECKPOINT: PLAN REVIEW ==="
    echo "Synthesis complete. Presenting implementation plan to user..."

    # Present plan to user in clear, readable format including:
    # - Architecture approach and key design decisions
    # - Files to be created/modified
    # - Implementation sequence and dependencies
    # - Testing strategy
    # - Risk mitigation approaches

    # Wait for user approval message
    # Only proceed after receiving explicit confirmation (e.g., "yes", "approved", "proceed")
    # PROHIBITED: Assuming approval from bypass mode or lack of objection
}

present_changes_and_wait_for_user_approval() {
    echo "=== USER APPROVAL CHECKPOINT: CHANGE REVIEW ==="
    echo "All stakeholder agents have approved. Committing changes for user review..."

    # Step 1: Commit all changes to task branch
    git add <changed-files>
    git commit -m "<descriptive-commit-message>"
    COMMIT_SHA=$(git rev-parse HEAD)
    echo "Changes committed: $COMMIT_SHA"

    # Step 2: Present change summary including:
    # - Commit SHA for reference
    # - Files modified/created/deleted (git diff --stat)
    # - Key implementation decisions made
    # - Test results and quality gate status
    # - Summary of how each stakeholder requirement was addressed

    # Step 3: Ask user for approval
    # Ask: "All stakeholder agents have approved. Changes committed to task branch (SHA: $COMMIT_SHA). Please review the changes. Would you like me to proceed with finalizing (COMPLETE → CLEANUP)?"

    # Step 4: Wait for user response
    # If user requests changes:
    #   - Make requested modifications
    #   - Amend commit: git add <files> && git commit --amend --no-edit
    #   - Return to Step 2 with new commit SHA
    # If user approves:
    #   - Proceed to COMPLETE state
}
```

## MIGRATION FROM PHASE-BASED PROTOCOL

### Compatibility Layer
All existing CLAUDE.md references to "phases" map to states as follows:
- Phase 1 = REQUIREMENTS state
- Phase 2 = SYNTHESIS state
- Phase 3 = CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE states
- Phase 4 = Part of VALIDATION state
- Phase 5 = Resolution cycle (SYNTHESIS ↔ CONTEXT ↔ AUTONOMOUS_IMPLEMENTATION ↔ CONVERGENCE ↔ VALIDATION)
- Phase 6 = REVIEW state
- Phase 7 = CLEANUP state

### Transition Period Support
During migration, both terminologies are recognized:
- "Execute Phase 1" → "Transition to REQUIREMENTS state"
- "Phase 3" → "Transition to CONTEXT → AUTONOMOUS_IMPLEMENTATION → CONVERGENCE states"
- "Phase 6 rejection" → "REVIEW state rejection, return to SYNTHESIS"
- "Phase guards" → "Transition conditions"

### Zero Tolerance Enforcement
This state machine implements **mandatory transition conditions**. There are no manual overrides, no "reasonable approximation" exceptions, and no human discretion in bypassing required validations.

**END OF STATE MACHINE PROTOCOL**

