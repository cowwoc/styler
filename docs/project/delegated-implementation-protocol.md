# Delegated Implementation Protocol

## Overview

The delegated implementation protocol enables agent-based implementation for complex tasks through round-based execution. Agents are organized into dependency rounds and execute sequentially or in parallel based on their dependencies.

**KEY PRINCIPLE**: Agents must execute in dependency order - dependent agents cannot run until their dependencies complete and their changes are integrated.

## Round-Based Execution Model

### Why Rounds Are Required

**The Problem**: Agents cannot see each other's work when running in parallel. If Agent B depends on Agent A's output (e.g., an interface Agent A creates), Agent B will fail if launched before Agent A's changes are integrated into the codebase.

**The Solution**: Organize agents into **dependency rounds**:
1. **Round 1**: Foundation agents (no dependencies) - execute in parallel
2. **Integration**: Apply all Round 1 diffs to codebase
3. **Round 2**: Implementation agents (depend on Round 1) - execute in parallel
4. **Integration**: Apply all Round 2 diffs to codebase
5. **Round N**: Subsequent rounds following same pattern
6. **Final Round**: Validation agents (read-only verification)

### Round Execution Pattern

```python
for round_num, agents in rounds:
    # 1. Launch all agents in this round in parallel
    results = launch_agents_parallel(agents)

    # 2. Validate all diffs before proceeding
    for agent in agents:
        validate_diff_applies(agent.diff_file)

    # 3. Integrate all diffs into codebase
    for agent in agents:
        apply_diff(agent.diff_file)

    # 4. Verify build still compiles
    run_build_check()

    # 5. Proceed to next round
```

### Typical Round Structure

**Round 1 - Foundation**:
- `technical-architect`: Create core interfaces, data structures
- `build-validator`: Create/update Maven POMs, module descriptors
- **Parallelism**: ✅ Yes (no dependencies between these agents)

**Round 2 - Implementation**:
- `code-quality-auditor`: Implement concrete classes using Round 1 interfaces
- `performance-analyzer`: Implement algorithms, optimization logic
- **Parallelism**: ✅ Yes (both depend on Round 1, but not on each other)
- **Dependencies**: Requires Round 1 complete (interfaces must exist)

**Round 3 - Enhancement**:
- `style-auditor`: Add JavaDoc, fix style violations
- **Parallelism**: N/A (single agent)
- **Dependencies**: Requires Round 2 complete (classes must exist to document)

**Round 4 - Testing**:
- `code-tester`: Create comprehensive test suite
- **Parallelism**: N/A (single agent)
- **Dependencies**: Requires Round 3 complete (implementation must be complete)

**Round 5 - Validation** (Read-Only):
- `build-validator`: Run `mvn verify`, check quality gates
- `security-auditor`: Scan for vulnerabilities
- **Parallelism**: ✅ Yes (both are read-only)
- **Dependencies**: Requires Round 4 complete (tests must exist)

### Performance Implications

**Actual Speedup** = (Rounds with >1 agent) × (agents per round)

**Example**:
- Sequential execution: 6 agents × 2 min = **12 minutes**
- Round-based execution: 5 rounds × 2 min (max agent time per round) = **10 minutes**
- Improvement: ~17% (not 4× as originally claimed)

**Reality**: Most tasks have 3-5 dependency rounds, limiting parallelism to 2-3× speedup maximum.

## Protocol Detection

**Automatic Detection Criteria**:
1. `context.md` exists in task root (`/workspace/branches/{task-name}/context.md`)
2. File contains section: `## Agent Work Assignments`
3. Lock file state is one of: CONTEXT, AUTONOMOUS_IMPLEMENTATION, CONVERGENCE

**When Detected**:
- Direct Write/Edit usage for implementation files is **PROHIBITED**
- Implementation must be performed by delegated agents
- Enforcement via `enforce-delegated-implementation.sh` hook

## State Machine (Delegated Protocol)

```
USER_APPROVAL (plan approved)
    ↓
CONTEXT (generate/verify context.md with agent assignments)
    ↓
AUTONOMOUS_IMPLEMENTATION (invoke agents in parallel)
    ↓
CONVERGENCE (integrate agent outputs, resolve conflicts)
    ↓
VALIDATION (build verification)
    ↓
REVIEW (stakeholder approval)
    ↓
USER_APPROVAL (final approval)
    ↓
COMPLETE (merge to main)
    ↓
CLEANUP (remove worktree)
```

## Implementation Phases

### Phase 1: CONTEXT State

**Objective**: Generate or verify implementation context

**Actions**:
1. Verify `context.md` exists with complete agent work assignments
2. Each agent assignment must specify:
   - Agent type (e.g., technical-architect, security-auditor)
   - Specific files to create/modify
   - Implementation requirements
3. Update lock state to AUTONOMOUS_IMPLEMENTATION

**Lock Update**:
```bash
jq '.state = "AUTONOMOUS_IMPLEMENTATION"' /workspace/locks/{task}.json > /tmp/lock.json
mv /tmp/lock.json /workspace/locks/{task}.json
```

### Phase 2: AUTONOMOUS_IMPLEMENTATION State

**Objective**: Invoke delegated agents in dependency rounds to perform implementation

**MANDATORY ROUND-BASED EXECUTION**:

**Step 1: Parse Round Assignments from context.md**
```bash
# context.md contains round assignments:
# Round 1: [technical-architect, build-validator]
# Round 2: [code-quality-auditor, performance-analyzer]
# Round 3: [style-auditor]
# Round 4: [code-tester]
# Round 5: [build-validator (validation)]
```

**Step 2: Execute Each Round**
```bash
for ROUND in 1 2 3 4 5; do
    echo "=== Executing Round $ROUND ==="

    # 2a. Launch all agents in round (parallel Task tool invocations)
    # Example: Round 1 launches technical-architect AND build-validator in single message

    # 2b. Wait for all round agents to complete

    # 2c. Validate all diffs (MANDATORY)
    for AGENT_DIFF in ../round${ROUND}-*.diff; do
        git apply --check "$AGENT_DIFF" || {
            echo "ERROR: $AGENT_DIFF does not apply cleanly"
            # Retry agent with error details OR use alternative integration
            exit 1
        }
    done

    # 2d. Apply all diffs
    for AGENT_DIFF in ../round${ROUND}-*.diff; do
        git apply "$AGENT_DIFF"
    done

    # 2e. Verify build compiles (except Round 5 which IS the build verification)
    if [ $ROUND -lt 5 ]; then
        mvn compile -q || {
            echo "ERROR: Build broken after Round $ROUND"
            exit 1
        }
    fi

    echo "✅ Round $ROUND complete"
done
```

**Example Round 1 Agent Invocation** (both agents in single message):
```
Task tool invocation 1:
- subagent_type: "technical-architect"
- prompt: "ROUND 1 of 5. Create FormattingRule interface and Violation record. Read context.md section '## Round 1: technical-architect'. Output diff to ../round1-technical-architect.diff"

Task tool invocation 2 (same message):
- subagent_type: "build-validator"
- prompt: "ROUND 1 of 5. Create Maven POM files for formatter modules. Read context.md section '## Round 1: build-validator'. Output diff to ../round1-build-validator.diff"
```

**Expected Outputs Per Round**:
- `../round{N}-{agent-type}.diff` - Code changes for round N
- `../round{N}-{agent-type}-metadata.json` - Metadata (files changed, summary)

**DIFF VALIDATION GATE** (MANDATORY):

Before accepting any agent diff, MUST verify it applies cleanly:
```bash
git apply --check /path/to/agent.diff
if [ $? -ne 0 ]; then
    echo "❌ VALIDATION FAILED: Diff does not apply cleanly"
    echo "Common causes:"
    echo "  - Agent generated diff against hypothetical files (context mismatch)"
    echo "  - Agent used placeholder git index values (not real commit hashes)"
    echo "  - File changed between agent read and diff generation"
    echo ""
    echo "Resolution: Re-invoke agent with instruction to read actual files first"
    # DO NOT PROCEED - fix the diff or retry agent
    exit 1
fi
```

**Alternative to Diffs** (if validation repeatedly fails):

Agents can return complete file contents instead of diffs:
```json
{
  "implementation_method": "full_files",
  "files": [
    {
      "path": "styler-formatter-api/pom.xml",
      "content": "<?xml version=\"1.0\"...",
      "action": "create"
    }
  ]
}
```

**CRITICAL**: Do NOT use Write/Edit tools during this phase. All implementation must come from agents.

### Phase 3: CONVERGENCE State

**Objective**: Integrate agent outputs and resolve conflicts

**Actions**:
1. Read all agent output files from `../`
2. Apply diffs in dependency order
3. Resolve any conflicts between agent outputs
4. Validate integration completeness
5. Update lock state to VALIDATION

**Allowed Tools**: Write/Edit permitted ONLY for:
- Applying agent diffs (files generated by agents)
- Resolving merge conflicts between agent outputs
- Integration code (minimal glue between agent outputs)

**STRICTLY PROHIBITED**:
❌ Creating new implementation files not generated by agents
❌ Writing business logic code not provided by agents
❌ Implementing features yourself instead of applying agent diffs
❌ "Helping" agents by writing code they should have created

**IF AGENTS FAILED**: Do NOT implement code in CONVERGENCE. Return to AUTONOMOUS_IMPLEMENTATION and retry agents per Failure Recovery section.

### Phase 4: VALIDATION State

**Objective**: Ensure build passes and quality gates met

**Actions**:
1. Run `./mvnw verify`
2. Fix any build failures
3. Ensure all quality gates pass (checkstyle, PMD, tests)
4. Update lock state to REVIEW

### Phase 5: REVIEW State

**Objective**: Get unanimous stakeholder approval

**Actions**:
1. Invoke review agents (same types as requirements phase)
2. Collect approval/rejection decisions
3. If ANY rejection: Return to CONVERGENCE or AUTONOMOUS_IMPLEMENTATION
4. If ALL approve: Move to USER_APPROVAL checkpoint

## Post-Compaction Recovery

**Detection Indicators**:
- Lock file exists with your session_id
- context.md has "## Agent Work Assignments" section
- Current state is CONTEXT, AUTONOMOUS_IMPLEMENTATION, or CONVERGENCE

**Recovery Pattern**:
```bash
# 1. Check lock state
cat /workspace/locks/{task}.json

# 2. Read context.md
grep -A 50 "## Agent Work Assignments" ../context.md

# 3. Determine protocol phase
# 4. Follow state-specific actions above
```

**State-Specific Recovery**:

**If in CONTEXT**:
- Verify context.md has complete agent assignments
- Update lock to AUTONOMOUS_IMPLEMENTATION
- Invoke implementation agents

**If in AUTONOMOUS_IMPLEMENTATION**:
- Check for agent output files in ../
- If agents completed: Move to CONVERGENCE
- If agents failed/incomplete: Document failure, decide recovery strategy
- DO NOT implement directly

**If in CONVERGENCE**:
- Read agent outputs, apply diffs, resolve conflicts
- Allowed to use Write/Edit for integration only
- Update lock to VALIDATION when complete

## Violation Prevention

**Hook Enforcement**:
- `enforce-delegated-implementation.sh`: Blocks Write/Edit during AUTONOMOUS_IMPLEMENTATION
- `check-lock-ownership.sh`: Provides recovery guidance after compaction

**Manual Verification**:
```bash
# Check if delegated protocol is active
grep -q "## Agent Work Assignments" ../context.md && echo "Delegated protocol active"

# Verify current state allows direct implementation
LOCK_STATE=$(jq -r '.state' /workspace/locks/{task}.json)
case "$LOCK_STATE" in
    CONTEXT|AUTONOMOUS_IMPLEMENTATION)
        echo "❌ Direct implementation prohibited - use agents"
        ;;
    CONVERGENCE)
        echo "⚠️ Direct implementation only for integration/conflict resolution"
        ;;
    *)
        echo "✅ Direct implementation allowed"
        ;;
esac
```

## Failure Recovery

**CRITICAL**: Main agent must NEVER implement code as first response to agent failure.

**Mandatory Recovery Sequence**:

### Attempt 1: Refine Agent Instructions (REQUIRED)

1. **Analyze failure**:
   ```bash
   # Read agent output to understand what went wrong
   cat ../technical-architect-metadata.json
   ```

2. **DO NOT update state yet** - remain in AUTONOMOUS_IMPLEMENTATION

3. **Re-invoke agent with refined instructions**:
   - Add explicit file paths to create
   - Include code examples or patterns to follow
   - Reference similar implementations in codebase
   - Specify exact output format expected

4. **Verify agent output** before proceeding

### Attempt 2: Different Agent Type (if Attempt 1 fails)

1. Try alternative agent type that might handle task better
2. Provide even more detailed context and examples
3. Verify output completeness

### Attempt 3: Manual Implementation Fallback (ONLY if Attempts 1-2 fail)

**Justification Required**: Manual fallback is permitted ONLY when:
- Agent has been retried with refined instructions (2+ attempts)
- Technical limitation prevents agent from completing task
- Specific reason documented explaining why agents cannot succeed

**IF justified, proceed with manual fallback**:

1. Document failure in context.md:
   ```markdown
   ## Implementation Status
   - [x] AUTONOMOUS_IMPLEMENTATION: FAILED after 2 retry attempts
   - Agent returned: [describe what agent provided]
   - Failure reason: [specific technical limitation]
   - Retry attempt 1: [what was tried, why it failed]
   - Retry attempt 2: [what was tried, why it failed]
   - Fallback decision: Manual implementation justified because [reason]
   ```

2. Update state to CONVERGENCE:
   ```bash
   jq '.state = "CONVERGENCE"' /workspace/locks/{task}.json > /tmp/lock.json
   mv /tmp/lock.json /workspace/locks/{task}.json
   ```

3. Implement directly with full documentation:
   ```markdown
   ## Convergence Notes
   Agent delegation failed after 2 retry attempts due to [specific technical reason].
   Manual implementation approach:
   - Created X files manually
   - Followed patterns from [reference implementation]
   - Validated against requirements in context.md
   - REVIEW agents will validate direct implementation
   ```

4. Ensure REVIEW agents validate the direct implementation

**PROHIBITED PATTERNS**:
❌ Implementing directly after single agent failure without retry
❌ Updating state to CONVERGENCE before retry attempts
❌ Manual implementation without documenting retry attempts
❌ Assuming agent "won't work" without testing refined instructions

## Best Practices

1. **Always update lock state** before transitioning phases
2. **Read context.md first** after compaction to determine protocol type
3. **Verify agent outputs** before moving to CONVERGENCE
4. **Document deviations** when forced to bypass agent implementation
5. **Trust the hooks** - they prevent common protocol violations

## Common Mistakes

❌ **Mistake**: Starting direct implementation without checking context.md
✅ **Correct**: Read context.md, verify protocol type, follow state machine

❌ **Mistake**: Implementing directly during AUTONOMOUS_IMPLEMENTATION
✅ **Correct**: Invoke agents via Task tool, wait for completion

❌ **Mistake**: Implementing code manually after first agent failure
✅ **Correct**: Retry agent with refined instructions (minimum 2 attempts) before manual fallback

❌ **Mistake**: Main agent writing implementation code in CONVERGENCE phase
✅ **Correct**: CONVERGENCE is for applying agent diffs, not creating new implementation

❌ **Mistake**: Forgetting to update lock state
✅ **Correct**: Update lock after each state transition

❌ **Mistake**: Bypassing failed agent implementation without documentation
✅ **Correct**: Document all retry attempts, failure reasons, and justification for manual fallback
