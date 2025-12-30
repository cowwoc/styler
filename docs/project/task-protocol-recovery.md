# Task Protocol - Error Recovery Procedures

> **Version:** 1.0 | **Parent:** [task-protocol-operations.md](task-protocol-operations.md)

**PURPOSE**: Error recovery for session interruptions, agent failures, build errors, state corruption.

**RECOMMENDED**: Use `recover-from-error` skill for quick diagnosis.

---

## Quick Navigation

| Error Type | Section |
|------------|---------|
| Session Interrupted | [Session Interruption](#recovery-procedure-1-session-interruption-recovery) |
| Agent Failed/Timeout | [Agent Partial Completion](#recovery-procedure-2-agent-partial-completion) |
| Build Failed | [Build Failure](#recovery-procedure-3-build-failure-after-partial-implementation) |
| State Corruption | [State Corruption](#recovery-procedure-4-state-corruption-recovery) |
| Multiple Interruptions | [Multiple Interruption](#multiple-interruption-handling) |
| Lock Conflicts | [Multi-Instance Conflict](#multi-instance-conflict-resolution) |

---

## Error Recovery Protocols {#error-recovery-protocols}

### Violation Detection Triggers {#violation-detection-triggers}

**Automatic violation detection:**
- Wrong directory (pwd check fails)
- Missing locks (lock file check fails)
- Build failures (non-zero exit codes)
- Git operation failures (rebase/merge fails)
- Prohibited files detected (status check fails)

**Recovery Action**: TERMINATE current task, restart from INIT state

### Multi-Instance Conflict Resolution {#multi-instance-conflict-resolution}

```bash
handle_lock_conflict() {
    local task_name=$1
    echo "Lock conflict detected for task: $task_name"

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

### Partial Completion Recovery {#partial-completion-recovery}

**Scenario Classification**:
1. **Session Interruption**: Task interrupted mid-state (context compact, crash, user stop)
2. **Agent Partial Completion**: Some agents complete, others fail or timeout
3. **Build Failure**: Compilation/test failures after partial implementation
4. **State Corruption**: Lock file or worktree inconsistencies

#### Recovery Procedure 1: Session Interruption Recovery {#recovery-procedure-1-session-interruption-recovery}

**State-specific recovery actions**:

| State | Recovery Action |
|-------|-----------------|
| INIT, CLASSIFIED | Safe to restart from current state |
| REQUIREMENTS | Check agent completion status via `verify_agent_completion_status`, resume agent invocations if needed |
| SYNTHESIS | Check for `user-plan-approval-obtained.flag`: if exists, proceed to IMPLEMENTATION; else re-present plan |
| IMPLEMENTATION | Check agent status via `verify_implementation_rounds_status`, resume incomplete work |
| VALIDATION | Re-run validation checks |
| REVIEW | Check review agent status via `verify_review_agent_status`, resume if needed |
| AWAITING_USER_APPROVAL | Re-present changes for approval using checkpoint commit_sha from task.json |
| COMPLETE, CLEANUP | Resume completion/cleanup procedures |
| Unknown | Manual intervention required |

```bash
# SessionStart hook detects active task for current session
TASK_NAME=$(jq -r --arg sid "$SESSION_ID" '.task_name | select(.session_id == $sid)' /workspace/tasks/*/task.json 2>/dev/null)

if [ -n "$TASK_NAME" ]; then
    CURRENT_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")
    echo "=== RESUMING TASK: $TASK_NAME (State: $CURRENT_STATE) ==="

    case "$CURRENT_STATE" in
        "INIT"|"CLASSIFIED")
            echo "Early state detected - resuming from current state"
            ;;
        "REQUIREMENTS")
            echo "Checking requirements agent completion status..."
            verify_agent_completion_status
            ;;
        "SYNTHESIS")
            if [ -f "/workspace/tasks/$TASK_NAME/user-plan-approval-obtained.flag" ]; then
                echo "Plan approval obtained - proceeding to IMPLEMENTATION"
            else
                echo "Waiting for plan approval - re-presenting plan to user"
            fi
            ;;
        "IMPLEMENTATION")
            echo "Checking implementation agent completion status..."
            verify_implementation_rounds_status
            ;;
        "VALIDATION")
            echo "Re-running validation checks..."
            ;;
        "REVIEW")
            echo "Checking review agent completion status..."
            verify_review_agent_status
            ;;
        "AWAITING_USER_APPROVAL")
            COMMIT_SHA=$(jq -r '.checkpoint.commit_sha' "/workspace/tasks/$TASK_NAME/task.json")
            echo "Re-presenting changes (commit $COMMIT_SHA) for user approval..."
            ;;
        "COMPLETE"|"CLEANUP")
            echo "Resuming ${CURRENT_STATE} state procedures..."
            ;;
        *)
            echo "Unknown state: $CURRENT_STATE - manual intervention required"
            ;;
    esac
else
    echo "No active tasks for current session"
fi
```

#### Recovery Procedure 2: Agent Partial Completion {#recovery-procedure-2-agent-partial-completion}

**Scenario**: During REQUIREMENTS/IMPLEMENTATION, some agents complete successfully while others fail, timeout, or return errors.

**Detection - Check status.json for each required agent**:

| Status | Output | Action |
|--------|--------|--------|
| Missing status.json | `"[agent]: status.json missing (not invoked or invocation failed)"` | Re-invoke |
| COMPLETE | `"[agent]: COMPLETE"` | No action |
| ERROR | `"[agent]: ERROR - [error_message]"` | Re-invoke |
| WORKING/IN_PROGRESS with age >60 min | `"[agent]: TIMEOUT (stuck [age] min)"` | Re-invoke |
| WORKING/IN_PROGRESS with age <=60 min | `"[agent]: WORKING ([age]min elapsed)"` | Wait |

**Detection Script**:
```bash
detect_partial_agent_completion() {
    local TASK_NAME=$1
    local STATE=$2  # "REQUIREMENTS" or "IMPLEMENTATION"
    local AGENTS=($(jq -r '.required_agents[]' "/workspace/tasks/$TASK_NAME/task.json"))

    local COMPLETE_COUNT=0
    local ERROR_COUNT=0
    local TIMEOUT_COUNT=0
    local MISSING_COUNT=0

    for agent in "${AGENTS[@]}"; do
        STATUS_FILE="/workspace/tasks/$TASK_NAME/agents/$agent/status.json"

        if [ ! -f "$STATUS_FILE" ]; then
            ((MISSING_COUNT++))
            echo "[agent]: status.json missing (not invoked or invocation failed)"
            continue
        fi

        STATUS=$(jq -r '.status' "$STATUS_FILE" 2>/dev/null)
        case "$STATUS" in
            "COMPLETE")
                ((COMPLETE_COUNT++))
                echo "[agent]: COMPLETE"
                ;;
            "ERROR")
                ((ERROR_COUNT++))
                ERROR_MSG=$(jq -r '.error_message // "Unknown"' "$STATUS_FILE")
                echo "[agent]: ERROR - $ERROR_MSG"
                ;;
            "WORKING"|"IN_PROGRESS")
                LAST_UPDATE=$(jq -r '.updated_at' "$STATUS_FILE")
                AGE_MINUTES=$(( ($(date +%s) - $(date -d "$LAST_UPDATE" +%s)) / 60 ))
                if [ $AGE_MINUTES -gt 60 ]; then
                    ((TIMEOUT_COUNT++))
                    echo "[agent]: TIMEOUT (stuck $AGE_MINUTES min)"
                else
                    echo "[agent]: WORKING (${AGE_MINUTES}min elapsed)"
                fi
                ;;
            *)
                echo "[agent]: Unknown status '$STATUS'"
                ;;
        esac
    done

    echo "Summary: ${COMPLETE_COUNT}/${#AGENTS[@]} complete, $ERROR_COUNT errors, $TIMEOUT_COUNT timeouts, $MISSING_COUNT missing"

    if [ ${#AGENTS[@]} -eq $COMPLETE_COUNT ]; then
        return 0  # All complete
    else
        return 1  # Partial completion
    fi
}
```

**Recovery Steps** (7-step sequence):
1. **Collect required agents**: Read `required_agents` from task.json
2. **Identify incomplete agents**: Status missing, non-COMPLETE, or timeout (>60 min since last update)
3. **For each incomplete agent, check retry count**: Read `retry_count` from status.json (default 0)
4. **If retry_count >= 3**: Call `escalate_agent_failure_to_user "$agent"`, skip to next agent
5. **Otherwise**: Increment retry_count in status.json
6. **Re-invoke agent via Task tool**: Main agent uses Task tool with appropriate prompt
7. **After all re-invocations**: Call `wait_for_agent_completion "${INCOMPLETE_AGENTS[@]}"`

```bash
recover_partial_agent_completion() {
    local TASK_NAME=$1
    local STATE=$2
    local AGENTS=($(jq -r '.required_agents[]' "/workspace/tasks/$TASK_NAME/task.json"))

    echo "=== PARTIAL AGENT COMPLETION RECOVERY ==="

    INCOMPLETE_AGENTS=()
    for agent in "${AGENTS[@]}"; do
        STATUS_FILE="/workspace/tasks/$TASK_NAME/agents/$agent/status.json"

        if [ ! -f "$STATUS_FILE" ]; then
            INCOMPLETE_AGENTS+=("$agent")
            continue
        fi

        STATUS=$(jq -r '.status' "$STATUS_FILE")
        if [ "$STATUS" != "COMPLETE" ]; then
            INCOMPLETE_AGENTS+=("$agent")
        fi
    done

    if [ ${#INCOMPLETE_AGENTS[@]} -eq 0 ]; then
        echo "All agents complete - no recovery needed"
        return 0
    fi

    echo "Incomplete agents: ${INCOMPLETE_AGENTS[*]}"
    echo "Recovery action: Re-invoking incomplete agents..."

    for agent in "${INCOMPLETE_AGENTS[@]}"; do
        echo "Re-invoking: $agent"

        RETRY_COUNT=$(jq -r '.retry_count // 0' "/workspace/tasks/$TASK_NAME/agents/$agent/status.json" 2>/dev/null)
        if [ "$RETRY_COUNT" -ge 3 ]; then
            echo "$agent: Max retries exceeded (3) - escalating to user"
            escalate_agent_failure_to_user "$agent"
            continue
        fi

        jq --arg count "$((RETRY_COUNT + 1))" '.retry_count = ($count | tonumber)' \
           "/workspace/tasks/$TASK_NAME/agents/$agent/status.json" > "/tmp/status.tmp" 2>/dev/null
        mv "/tmp/status.tmp" "/workspace/tasks/$TASK_NAME/agents/$agent/status.json" 2>/dev/null

        # Re-invoke agent via Task tool (main agent should use Task tool here)
    done

    wait_for_agent_completion "${INCOMPLETE_AGENTS[@]}"
}
```

#### Recovery Procedure 3: Build Failure After Partial Implementation {#recovery-procedure-3-build-failure-after-partial-implementation}

**Scenario**: Some agents merged changes to task branch, but final build verification fails.

**Recovery Steps** (7-step sequence):
1. **Identify merged work**: `git log --oneline -20 | grep -E "(architect|engineer|formatter)"`
2. **Run build to capture errors**: `BUILD_OUTPUT=$(./mvnw verify 2>&1 | tee /tmp/build-failure.log)`
3. **Classify failure type** by pattern matching (see table below)
4. **Determine recovery action** based on classification
5. **Execute recovery action** (see decision matrix)
6. **Re-run build after fixes**: `./mvnw verify`
7. **If still failing**: Return 1 for additional recovery iterations

**Failure Classification**:

| Failure Type | Detection Pattern | Recovery Action |
|-------------|-------------------|-----------------|
| Compilation | `"COMPILATION ERROR"` in output | Check for `"cannot find symbol"`: YES = main agent fixes (missing imports); NO = re-delegate to architect |
| Test | `"There are test failures"` in output | Re-delegate to test agent |
| Style | `"Checkstyle violations"` in output | Count violations: <=5 main agent fixes; >5 re-delegate to style agent |
| Unknown | None of the above | Log to /tmp/build-failure.log, escalate to user |

```bash
recover_build_failure_partial_implementation() {
    local TASK_NAME=$1

    echo "=== BUILD FAILURE RECOVERY ==="

    # Step 1: Identify which merges succeeded
    echo "Analyzing git log for merged agent work..."
    git log --oneline -20 | grep -E "(architect|engineer|formatter)"

    # Step 2: Run build to get specific error details
    echo "Running build to identify specific failures..."
    BUILD_OUTPUT=$(./mvnw verify 2>&1 | tee /tmp/build-failure.log)

    # Step 3: Classify failure type
    if echo "$BUILD_OUTPUT" | grep -q "COMPILATION ERROR"; then
        echo "Type: Compilation errors"
        RECOVERY_ACTION="fix_compilation_errors"
    elif echo "$BUILD_OUTPUT" | grep -q "There are test failures"; then
        echo "Type: Test failures"
        RECOVERY_ACTION="fix_test_failures"
    elif echo "$BUILD_OUTPUT" | grep -q "Checkstyle violations"; then
        echo "Type: Style violations"
        RECOVERY_ACTION="fix_style_violations"
    else
        echo "Type: Unknown build failure"
        RECOVERY_ACTION="investigate_manually"
    fi

    # Step 4: Execute recovery action
    case "$RECOVERY_ACTION" in
        "fix_compilation_errors")
            ERRORS=$(echo "$BUILD_OUTPUT" | grep -A5 "COMPILATION ERROR")
            echo "Compilation errors found:"
            echo "$ERRORS"

            if echo "$ERRORS" | grep -q "cannot find symbol"; then
                echo "Likely missing imports - main agent can fix"
            else
                echo "Complex compilation errors - re-delegating to architect"
            fi
            ;;

        "fix_test_failures")
            echo "$BUILD_OUTPUT" | grep -E "Tests run:|Failures:"
            echo "Re-delegating test failures to test agent"
            ;;

        "fix_style_violations")
            VIOLATION_COUNT=$(echo "$BUILD_OUTPUT" | grep -oP '\[\d+\] violations' | grep -oP '\d+' | head -1)

            if [ "$VIOLATION_COUNT" -le 5 ]; then
                echo "Small number of violations ($VIOLATION_COUNT) - main agent fixes"
            else
                echo "Many violations ($VIOLATION_COUNT) - re-delegating to style"
            fi
            ;;

        "investigate_manually")
            echo "Build failure requires manual investigation"
            echo "Build log saved to /tmp/build-failure.log"
            ;;
    esac

    # Step 5: Re-run build after fixes
    echo "Re-running build after fixes..."
    ./mvnw verify || {
        echo "Build still failing - may need additional recovery iterations"
        return 1
    }

    echo "Build recovered successfully"
    return 0
}
```

#### Recovery Procedure 4: State Corruption Recovery {#recovery-procedure-4-state-corruption-recovery}

**Scenario**: Lock file state doesn't match actual work completed, or worktrees have issues.

**Detection Checks** (6 checks, all must pass):
1. Lock file exists: `/workspace/tasks/$TASK_NAME/task.json`
2. Lock file is valid JSON: `jq empty` succeeds
3. Session ID matches: lock `session_id == $SESSION_ID`
4. Task worktree exists: `/workspace/tasks/$TASK_NAME/code`
5. Agent worktrees exist for all `required_agents`: `/workspace/tasks/$TASK_NAME/agents/$agent/code`
6. Transition log exists and not empty

```bash
detect_state_corruption() {
    local TASK_NAME=$1

    echo "=== STATE CORRUPTION DETECTION ==="

    ISSUES_FOUND=false

    # Check 1: Lock file exists and is valid JSON
    if [ ! -f "/workspace/tasks/$TASK_NAME/task.json" ]; then
        echo "Lock file missing"
        ISSUES_FOUND=true
    elif ! jq empty "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null; then
        echo "Lock file corrupted (invalid JSON)"
        ISSUES_FOUND=true
    fi

    # Check 2: Session ID matches
    LOCK_SESSION=$(jq -r '.session_id' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null)
    if [ "$LOCK_SESSION" != "$SESSION_ID" ]; then
        echo "Session ID mismatch: lock=$LOCK_SESSION, current=$SESSION_ID"
        ISSUES_FOUND=true
    fi

    # Check 3: Worktrees exist
    if [ ! -d "/workspace/tasks/$TASK_NAME/code" ]; then
        echo "Task worktree missing"
        ISSUES_FOUND=true
    fi

    # Check 4: Agent worktrees match required_agents
    REQUIRED_AGENTS=($(jq -r '.required_agents[]' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null))
    for agent in "${REQUIRED_AGENTS[@]}"; do
        if [ ! -d "/workspace/tasks/$TASK_NAME/agents/$agent/code" ]; then
            echo "Agent worktree missing: $agent"
            ISSUES_FOUND=true
        fi
    done

    # Check 5: State transition log is consistent
    TRANSITION_LOG=$(jq -r '.transition_log' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null)
    if [ "$TRANSITION_LOG" = "null" ] || [ -z "$TRANSITION_LOG" ]; then
        echo "Transition log missing or empty"
        ISSUES_FOUND=true
    fi

    if $ISSUES_FOUND; then
        return 1  # Corruption detected
    else
        echo "No state corruption detected"
        return 0  # State is clean
    fi
}
```

**Recovery Steps** (7-step sequence):
1. **Run detection checks**: Call `detect_state_corruption "$TASK_NAME"`
2. **If no corruption**: Return 0 (no recovery needed)
3. **Recreate task worktree** if missing but branch exists:
   ```bash
   if [ ! -d "/workspace/tasks/$TASK_NAME/code" ] && git show-ref --verify --quiet "refs/heads/$TASK_NAME"; then
       echo "Recreating task worktree from existing branch..."
       git worktree add "/workspace/tasks/$TASK_NAME/code" "$TASK_NAME"
   fi
   ```
4. **Recreate agent worktrees** for each required agent if missing but branch exists:
   ```bash
   AGENT_BRANCH="${TASK_NAME}-${agent}"
   if [ ! -d "/workspace/tasks/$TASK_NAME/agents/$agent/code" ] && git show-ref --verify --quiet "refs/heads/$AGENT_BRANCH"; then
       echo "Recreating agent worktree: $agent"
       mkdir -p "/workspace/tasks/$TASK_NAME/agents/$agent"
       git worktree add "/workspace/tasks/$TASK_NAME/agents/$agent/code" "$AGENT_BRANCH"
   fi
   ```
5. **Handle corrupted lock file**: Manual intervention required with options:
   - Option 1: Delete task and restart from scratch
   - Option 2: Reconstruct lock file from git history
   - Option 3: Escalate to user
6. **Verify recovery**: Re-run `detect_state_corruption "$TASK_NAME"`
7. **If still failing**: Return 1 (manual intervention required)

```bash
recover_state_corruption() {
    local TASK_NAME=$1

    echo "=== STATE CORRUPTION RECOVERY ==="

    detect_state_corruption "$TASK_NAME"
    SEVERITY=$?

    if [ $SEVERITY -eq 0 ]; then
        echo "No recovery needed - state is valid"
        return 0
    fi

    echo "Attempting automatic state recovery..."

    CURRENT_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null)

    if [ ! -d "/workspace/tasks/$TASK_NAME/code" ] && git show-ref --verify --quiet "refs/heads/$TASK_NAME"; then
        echo "Recreating task worktree from existing branch..."
        git worktree add "/workspace/tasks/$TASK_NAME/code" "$TASK_NAME"
    fi

    REQUIRED_AGENTS=($(jq -r '.required_agents[]' "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null))
    for agent in "${REQUIRED_AGENTS[@]}"; do
        AGENT_BRANCH="${TASK_NAME}-${agent}"
        if [ ! -d "/workspace/tasks/$TASK_NAME/agents/$agent/code" ] && git show-ref --verify --quiet "refs/heads/$AGENT_BRANCH"; then
            echo "Recreating agent worktree: $agent"
            mkdir -p "/workspace/tasks/$TASK_NAME/agents/$agent"
            git worktree add "/workspace/tasks/$TASK_NAME/agents/$agent/code" "$AGENT_BRANCH"
        fi
    done

    if ! jq empty "/workspace/tasks/$TASK_NAME/task.json" 2>/dev/null; then
        echo "Lock file corrupted - manual intervention required"
        echo "Options:"
        echo "1. Delete task and restart from scratch"
        echo "2. Reconstruct lock file from git history"
        echo "3. Escalate to user"
        return 1
    fi

    detect_state_corruption "$TASK_NAME"
    if [ $? -eq 0 ]; then
        echo "State corruption recovered successfully"
        return 0
    else
        echo "Automatic recovery failed - manual intervention required"
        return 1
    fi
}
```

#### Recovery Decision Tree {#recovery-decision-tree}

**Decision sequence** (evaluate in order, THEN apply):
```
IF session interrupted during task execution
    → Apply Recovery Procedure 1 (Session Interruption)

ELSE IF agents not all COMPLETE during REQUIREMENTS or IMPLEMENTATION
    → Apply Recovery Procedure 2 (Agent Partial Completion)

ELSE IF build fails after agent merges
    → Apply Recovery Procedure 3 (Build Failure)

ELSE IF lock file or worktrees corrupted
    → Apply Recovery Procedure 4 (State Corruption)
```

#### Recovery Escalation Policy {#recovery-escalation-policy}

**Escalate to user when** (any condition):
1. Agent failure after 3 retries
2. Build failure with unknown error type
3. State corruption that cannot be automatically recovered
4. Conflicting agent requirements with no resolution path
5. External dependency blocking progress

**Escalation Message Template**:
```
"Task '$TASK_NAME' encountered a recovery scenario requiring user guidance:

**Issue**: [Description of the problem]
**Current State**: [Lock file state]
**Recovery Attempts**: [What was tried]
**Impact**: [What work is at risk]

**Options**:
1. [Option 1 with pros/cons]
2. [Option 2 with pros/cons]
3. Abandon task and select alternative

Please advise on how to proceed."
```

**Foreign Session Handling**:
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

---

### Multiple Interruption Handling {#multiple-interruption-handling}

**CRITICAL REQUIREMENT**: When user interrupts multiple times before resuming work, maintain correct state tracking and resume from the original pre-interruption state.

#### Scenario: Successive Interruptions {#scenario-successive-interruptions}

**Common Pattern**:
1. Agent working in IMPLEMENTATION state
2. User asks question -> Agent answers -> Still interrupted
3. User runs /audit-session -> Agent audits -> Still interrupted
4. User asks clarification -> Agent answers -> Still interrupted
5. User says "continue working" -> NOW resume IMPLEMENTATION

**Problem**: Agent must resume from IMPLEMENTATION (original state), NOT from whatever activity occurred during interruptions.

#### State Preservation During Interruptions {#state-preservation-during-interruptions}

**Core Principle**: Lock file state NEVER changes during interruption handling.

**Verification sequence** (4 steps, temporal order critical):
1. **BEFORE** handling ANY user command: Record `ACTIVE_STATE` from task.json
2. **HANDLE** user command (question, audit, clarification, etc.)
3. **AFTER** handling: Verify state UNCHANGED (if changed, ERROR and exit)
4. **ONLY** when user says "continue/resume/proceed" -> resume active state

```bash
# Check 1: Before handling ANY user command, record current state
ACTIVE_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")
echo "Active task state: $ACTIVE_STATE (preserved during interruption handling)"

# Check 2: Handle user command (question, audit, clarification, etc.)
# ... process user request ...

# Check 3: After handling, state should be UNCHANGED
CURRENT_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")
if [ "$CURRENT_STATE" != "$ACTIVE_STATE" ]; then
    echo "ERROR: State changed during interruption handling"
    echo "Expected: $ACTIVE_STATE, Got: $CURRENT_STATE"
    exit 1
fi

# Check 4: Only when user says "continue/resume/proceed" -> resume active state
```

#### Interruption Type Classification {#interruption-type-classification}

**Non-Resuming Interruptions** (do NOT change state, do NOT trigger resumption):
- User questions about code/architecture/implementation
- User requests for clarification
- User runs /audit-session or other diagnostic commands
- User requests status updates
- User provides feedback/observations during implementation

**Resumption Triggers** (signal to return to active state):
- User says "continue"
- User says "resume work"
- User says "proceed"
- User says "keep going"
- User says "continue with the task"

#### Multi-Interruption Handling Logic {#multi-interruption-handling-logic}

```bash
handle_user_command_during_task() {
    local TASK_NAME=$1
    local USER_COMMAND=$2

    # Retrieve active state (preserved from before interruption)
    ACTIVE_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")

    echo "=== HANDLING USER COMMAND ==="
    echo "Active task: $TASK_NAME"
    echo "Active state: $ACTIVE_STATE (preserved)"

    # Classify user command
    if [[ "$USER_COMMAND" =~ (continue|resume|proceed|keep.*going) ]]; then
        COMMAND_TYPE="RESUMPTION"
    elif [[ "$USER_COMMAND" =~ (audit|/audit-session) ]]; then
        COMMAND_TYPE="AUDIT"
    elif [[ "$USER_COMMAND" =~ (what|how|why|explain|clarify) ]]; then
        COMMAND_TYPE="QUESTION"
    elif [[ "$USER_COMMAND" =~ (status|progress|where.*are.*we) ]]; then
        COMMAND_TYPE="STATUS_REQUEST"
    else
        COMMAND_TYPE="OTHER"
    fi

    echo "Command type: $COMMAND_TYPE"

    case "$COMMAND_TYPE" in
        "RESUMPTION")
            echo "Resumption trigger detected - returning to active state: $ACTIVE_STATE"
            resume_task_from_active_state "$TASK_NAME" "$ACTIVE_STATE"
            ;;

        "AUDIT")
            echo "Running audit - state will be preserved"
            run_audit_command "$TASK_NAME"
            echo "Audit complete - state remains: $ACTIVE_STATE"
            echo "Awaiting resumption trigger or additional commands"
            ;;

        "QUESTION")
            echo "Answering user question - state will be preserved"
            answer_user_question "$USER_COMMAND"
            echo "Question answered - state remains: $ACTIVE_STATE"
            echo "Awaiting resumption trigger or additional commands"
            ;;

        "STATUS_REQUEST")
            echo "Providing status update - state will be preserved"
            provide_status_update "$TASK_NAME" "$ACTIVE_STATE"
            echo "Status provided - state remains: $ACTIVE_STATE"
            echo "Awaiting resumption trigger or additional commands"
            ;;

        "OTHER")
            echo "Processing command - state will be preserved"
            process_other_command "$USER_COMMAND"
            echo "Command processed - state remains: $ACTIVE_STATE"
            echo "Awaiting resumption trigger or additional commands"
            ;;
    esac

    # Verify state unchanged (except for RESUMPTION type)
    if [ "$COMMAND_TYPE" != "RESUMPTION" ]; then
        VERIFY_STATE=$(jq -r '.state' "/workspace/tasks/$TASK_NAME/task.json")
        if [ "$VERIFY_STATE" != "$ACTIVE_STATE" ]; then
            echo "CRITICAL ERROR: State changed during interruption handling"
            echo "This violates state preservation principle"
            exit 1
        fi
    fi
}
```

#### Resumption Logic After Multiple Interruptions {#resumption-logic-after-multiple-interruptions}

**State-specific resumption actions**:

| State | Resumption Action |
|-------|-------------------|
| REQUIREMENTS | Check `check_requirements_agent_status`, resume via `resume_incomplete_agents` |
| IMPLEMENTATION | Check `check_implementation_agent_status`, resume via `resume_implementation_rounds` |
| VALIDATION | Re-run via `run_validation_checks` |
| REVIEW | Check `check_review_agent_status`, resume via `resume_incomplete_reviews` |
| AWAITING_USER_APPROVAL | Re-present changes using `checkpoint.commit_sha` from task.json |

```bash
resume_task_from_active_state() {
    local TASK_NAME=$1
    local ACTIVE_STATE=$2

    echo "=== RESUMING TASK: $TASK_NAME ==="
    echo "Resuming from state: $ACTIVE_STATE"

    case "$ACTIVE_STATE" in
        "REQUIREMENTS")
            echo "Resuming requirements gathering..."
            check_requirements_agent_status "$TASK_NAME"
            resume_incomplete_agents "$TASK_NAME" "REQUIREMENTS"
            ;;

        "IMPLEMENTATION")
            echo "Resuming implementation work..."
            check_implementation_agent_status "$TASK_NAME"
            resume_implementation_rounds "$TASK_NAME"
            ;;

        "VALIDATION")
            echo "Resuming validation checks..."
            run_validation_checks "$TASK_NAME"
            ;;

        "REVIEW")
            echo "Resuming review process..."
            check_review_agent_status "$TASK_NAME"
            resume_incomplete_reviews "$TASK_NAME"
            ;;

        "AWAITING_USER_APPROVAL")
            echo "Returning to user approval checkpoint..."
            COMMIT_SHA=$(jq -r '.checkpoint.commit_sha' "/workspace/tasks/$TASK_NAME/task.json")
            present_changes_for_approval "$TASK_NAME" "$COMMIT_SHA"
            ;;

        *)
            echo "Resuming from state: $ACTIVE_STATE"
            # Continue with state-specific work
            ;;
    esac
}
```

#### Examples: Multi-Interruption Scenarios {#examples-multi-interruption-scenarios}

**Example 1: Question -> Audit -> Question -> Resume**

```
Initial State: IMPLEMENTATION (architect working)

[User interrupts with question]
User: "What architecture pattern are you using?"
Agent: [Answers question, lock state remains IMPLEMENTATION]

[User interrupts again with audit]
User: "/audit-session"
Agent: [Runs audit, lock state remains IMPLEMENTATION]

[User interrupts again with clarification]
User: "Can you explain why you chose that pattern?"
Agent: [Explains, lock state remains IMPLEMENTATION]

[User triggers resumption]
User: "Continue working"
Agent: [Resumes IMPLEMENTATION state - architect continues work]
```

**Example 2: Status -> Audit -> Resume**

```
Initial State: REVIEW (waiting for agent decisions)

[User interrupts with status request]
User: "Where are we in the process?"
Agent: [Provides status, lock state remains REVIEW]

[User interrupts with audit]
User: "Run audit to verify everything is correct"
Agent: [Runs audit, lock state remains REVIEW]

[User triggers resumption]
User: "Proceed with review"
Agent: [Resumes REVIEW state - continues waiting for agent decisions or processes received decisions]
```

**Example 3: Multiple Questions Before Resume (4 interruptions)**

```
Initial State: SYNTHESIS (drafting implementation plan)

[Interruption 1]
User: "How will you handle error cases?"
Agent: [Answers, lock state remains SYNTHESIS]

[Interruption 2]
User: "What about thread safety?"
Agent: [Answers, lock state remains SYNTHESIS]

[Interruption 3]
User: "Will this work with existing code?"
Agent: [Answers, lock state remains SYNTHESIS]

[Interruption 4]
User: "Can you explain the testing strategy?"
Agent: [Answers, lock state remains SYNTHESIS]

[User triggers resumption]
User: "Continue with the implementation plan"
Agent: [Resumes SYNTHESIS state - completes plan and presents for user approval]
```

#### Anti-Patterns: What NOT To Do {#anti-patterns-what-not-to-do}

**WRONG: Treating each interruption as a state change**
```bash
# VIOLATION: Lock state changes with every command
User: "What's the status?"
Agent: Updates lock state to "STATUS_CHECK"  # WRONG

User: "Run audit"
Agent: Updates lock state to "AUDITING"  # WRONG

User: "Continue"
Agent: Updates lock state to "COMPLETE"  # WRONG - lost original state!
```

**WRONG: Auto-resuming after interruption without explicit trigger**
```bash
# VIOLATION: Resuming without user signal
User: "What's happening now?"
Agent: [Answers question]
Agent: [Immediately continues IMPLEMENTATION work]  # WRONG - user didn't say continue
```

**WRONG: Losing track of original state**
```bash
# VIOLATION: Not preserving original state
Initial: IMPLEMENTATION
User: "Run audit"
Agent: [Runs audit, forgets it was in IMPLEMENTATION]
User: "Continue"
Agent: "Continue with what?"  # WRONG - should remember IMPLEMENTATION
```

**CORRECT: State preservation across all interruptions**
```bash
# CORRECT: Lock state never changes during interruptions
Initial: IMPLEMENTATION (lock file shows state=IMPLEMENTATION)

User: "What's the status?" -> lock still shows IMPLEMENTATION
User: "Run audit" -> lock still shows IMPLEMENTATION
User: "Explain your approach" -> lock still shows IMPLEMENTATION
User: "Continue" -> Resume IMPLEMENTATION (state was preserved)
```

#### Interruption Context Tracking {#interruption-context-tracking}

**Optional Enhancement**: Track interruption history for context (NOT required, but useful for debugging)

```bash
# Store interruption log in lock file (optional)
record_interruption() {
    local TASK_NAME=$1
    local COMMAND_TYPE=$2

    jq --arg cmd "$COMMAND_TYPE" \
       --arg ts "$(date -Iseconds)" \
       '.interruptions += [{"type": $cmd, "timestamp": $ts}]' \
       "/workspace/tasks/$TASK_NAME/task.json" > /tmp/lock.tmp
    mv /tmp/lock.tmp "/workspace/tasks/$TASK_NAME/task.json"
}

# Retrieve interruption count
get_interruption_count() {
    local TASK_NAME=$1
    jq '.interruptions | length' "/workspace/tasks/$TASK_NAME/task.json"
}
```

#### Checkpoint Interaction: Interruptions During User Approval Wait {#checkpoint-interaction-interruptions-during-user-approval-wait}

**Special Case**: User asks questions WHILE at a checkpoint (SYNTHESIS or AWAITING_USER_APPROVAL)

```bash
# Scenario: Agent waiting at PLAN APPROVAL checkpoint
CURRENT_STATE="SYNTHESIS"
echo "Waiting for user plan approval..."

# User asks question INSTEAD of approving
User: "Can you explain the architecture decision?"
Agent: [Answers question]
# State remains SYNTHESIS - still waiting for approval

# User asks another question
User: "What's the performance impact?"
Agent: [Answers question]
# State remains SYNTHESIS - still waiting for approval

# User finally approves
User: "Looks good, proceed with implementation"
Agent: [Recognizes approval, transitions SYNTHESIS -> IMPLEMENTATION]
```

**Key Insight**: At checkpoints, interruptions (questions) are STILL non-resuming. Only approval keywords trigger state transition.
