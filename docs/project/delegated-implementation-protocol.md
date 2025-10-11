# Delegated Implementation Protocol

## Overview

Agent-based implementation via **emergent dependency discovery**. All agents attempt implementation each round; ordering emerges from readiness.

**KEY**: Dependencies discovered during implementation, not predicted. Agents self-organize into rounds.

## Emergent Dependency Discovery

### Why Static Planning Fails
Main agent cannot predict: Domain expertise gap (helper classes/interfaces/utilities) | Emergent dependencies (discovered during implementation) | Cross-agent coordination (interface extensions) | Circular dependencies (both agents need each other)

### Self-Organizing Round Structure

Rounds emerge from agent readiness:
1. **Round 1**: All agents attempt → COMPLETE (no deps) | PARTIAL (implement available) | BLOCKED (cannot proceed)
2. **Integration**: Apply COMPLETE + PARTIAL diffs
3. **Round N**: Non-COMPLETE agents retry → Continue until all COMPLETE

**Key**: PARTIAL implementations prevent deadlocks by ensuring progress despite missing dependencies.

### Benefits of Partial Implementation

**Deadlock Prevention**: Agents rarely BLOCKED; circular dependencies resolve incrementally.
**Faster Convergence**: PARTIAL enables parallelism. Example: BLOCKED approach 3 rounds (sequential) vs PARTIAL 2 rounds (parallel agents).
**Better Dependency Discovery**: Agents discover needs during implementation (not guesses).
**Reduced Wasted Effort**: No idle agents; earlier integration catches mismatches sooner.

### Execution Pattern

```python
def autonomous_implementation_with_discovery():
    """All agents participate each round. Rounds emerge from agent readiness."""
    round_num = 1
    all_agents = get_required_agents()
    completed_agents = set()

    while len(completed_agents) < len(all_agents):
        remaining_agents = [a for a in all_agents if a not in completed_agents]
        if not remaining_agents:
            break

        results = launch_agents_with_dependency_check(remaining_agents, round_num)
        ready_agents, blocked_agents = [], []

        for agent_name, response in results.items():
            if "PARTIAL:" in response or "COMPLETE:" in response:
                ready_agents.append({
                    "agent": agent_name,
                    "diff": get_agent_diff_file(agent_name, round_num),
                    "status": "PARTIAL" if "PARTIAL:" in response else "COMPLETE",
                    "completed_work": parse_completed_work(response),
                    "missing_deps": parse_missing_dependencies(response) if "PARTIAL:" in response else []
                })
                if "COMPLETE:" in response:
                    completed_agents.add(agent_name)
            elif "BLOCKED:" in response:
                blocked_agents.append({
                    "agent": agent_name,
                    "missing": parse_missing_dependencies(response),
                    "reason": parse_blocker_reason(response)
                })

        if not ready_agents:
            handle_deadlock(blocked_agents, round_num)
            break

        for agent_result in ready_agents:
            diff_file = agent_result["diff"]
            if validate_diff_applies(diff_file):
                apply_diff(diff_file)
                if agent_result["status"] == "COMPLETE":
                    completed_agents.add(agent_result["agent"])

        if ready_agents:
            git_commit(f"Round {round_num}: {', '.join([a['agent'] for a in ready_agents])}")
        round_num += 1

    return len(completed_agents) == len(all_agents)
```

## Protocol Detection

**Automatic Detection**: `context.md` exists in task root (`/workspace/branches/{task-name}/context.md`) | Contains `## Agent Work Assignments` section | Lock file state: CONTEXT, AUTONOMOUS_IMPLEMENTATION, or CONVERGENCE

**When Detected**: Direct Write/Edit for implementation files **PROHIBITED** | Implementation via delegated agents only | Enforced by `enforce-delegated-implementation.sh` hook

## State Machine (Delegated Protocol)

```
USER_APPROVAL (plan approved)
    ↓
CONTEXT (generate context.md with agent assignments)
    ↓
AUTONOMOUS_IMPLEMENTATION (iterative rounds until completion)
    ↓
CONVERGENCE (final integration, conflict resolution)
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

**Objective**: Generate implementation context WITHOUT predicting dependency order

**Actions**: Create `context.md` with agent assignments (NO round numbers) | Specify: agent type, task description, deliverables, known dependencies (may be incomplete) | Update lock state to AUTONOMOUS_IMPLEMENTATION

**context.md Structure** (NO static rounds):
```markdown
## Agent Work Assignments

### technical-architect
**Task**: Create core interfaces and data structures
**Deliverables**:
- FormattingRule.java interface
- Violation.java record
**Known Dependencies**: None
**Notes**: Foundation interfaces for other agents

### code-quality-auditor
**Task**: Implement concrete formatter classes
**Deliverables**:
- LineWrapperFormatter.java
**Known Dependencies**: FormattingRule.java (may discover more during implementation)
**Notes**: Will use interfaces created by technical-architect

### security-auditor
**Task**: Implement input validation
**Deliverables**:
- InputValidator.java
**Known Dependencies**: Unknown until implementation (may need validation interfaces)

### style-auditor
**Task**: Add JavaDoc and fix style violations
**Deliverables**: JavaDoc comments, style fixes
**Known Dependencies**: All implementation files (runs after other agents)

### code-tester
**Task**: Create comprehensive test suite
**Deliverables**: Test classes
**Known Dependencies**: All implementation files (runs after other agents)
```

**Lock Update**:
```bash
jq '.state = "AUTONOMOUS_IMPLEMENTATION"' /workspace/locks/{task}.json > /tmp/lock.json
mv /tmp/lock.json /workspace/locks/{task}.json
```

### Phase 2: AUTONOMOUS_IMPLEMENTATION State

**Objective**: Execute emergent dependency discovery rounds

**MANDATORY EXECUTION PATTERN** (preserved verbatim - execution-critical bash script):

```bash
#!/bin/bash

ROUND=1
ALL_AGENTS=("technical-architect" "code-quality-auditor" "security-auditor" "style-auditor" "code-tester")
COMPLETED_AGENTS=()

while [ ${#COMPLETED_AGENTS[@]} -lt ${#ALL_AGENTS[@]} ]; do
    echo "=== ROUND $ROUND: Dependency Discovery ==="

    # Get remaining agents
    REMAINING_AGENTS=()
    for agent in "${ALL_AGENTS[@]}"; do
        if [[ ! " ${COMPLETED_AGENTS[@]} " =~ " ${agent} " ]]; then
            REMAINING_AGENTS+=("$agent")
        fi
    done

    if [ ${#REMAINING_AGENTS[@]} -eq 0 ]; then
        echo "✅ All agents completed"
        break
    fi

    echo "Attempting: ${REMAINING_AGENTS[@]}"

    # Launch all remaining agents in parallel
    # (Use single message with multiple Task tool invocations)
    # Each agent gets dependency-check-enabled prompt

    # Collect results
    READY_AGENTS=()
    BLOCKED_AGENTS=()

    for agent in "${REMAINING_AGENTS[@]}"; do
        RESPONSE=$(cat "../round${ROUND}-${agent}-response.txt")

        if grep -q "COMPLETE:" <<< "$RESPONSE"; then
            READY_AGENTS+=("$agent")
            COMPLETED_AGENTS+=("$agent")  # Fully done, won't retry
            echo "   ✅ COMPLETE: $agent"
        elif grep -q "PARTIAL:" <<< "$RESPONSE"; then
            READY_AGENTS+=("$agent")
            # Note: NOT added to COMPLETED_AGENTS - will retry next round
            echo "   ⚙️  PARTIAL: $agent (has deferred work)"
        elif grep -q "BLOCKED:" <<< "$RESPONSE"; then
            BLOCKED_AGENTS+=("$agent")
            echo "   ⏸️  BLOCKED: $agent"
            grep "BLOCKED:" <<< "$RESPONSE"
        fi
    done

    # Deadlock detection
    if [ ${#READY_AGENTS[@]} -eq 0 ]; then
        echo "❌ DEADLOCK: No agents made progress in round $ROUND"
        echo "Blocked agents: ${BLOCKED_AGENTS[@]}"
        # Call deadlock resolution function
        resolve_deadlock "${BLOCKED_AGENTS[@]}"
        exit 1
    fi

    # Validate and apply diffs for ready agents
    for agent in "${READY_AGENTS[@]}"; do
        DIFF_FILE="../round${ROUND}-${agent}.diff"

        # Validate
        if ! git apply --check "$DIFF_FILE"; then
            echo "⚠️  Diff validation failed for $agent - skipping"
            continue
        fi

        # Apply
        git apply "$DIFF_FILE"
        COMPLETED_AGENTS+=("$agent")
        echo "   ✅ Integrated: $agent"
    done

    # Commit integration
    if [ ${#READY_AGENTS[@]} -gt 0 ]; then
        git commit -am "Round $ROUND: ${READY_AGENTS[*]}"
    fi

    ROUND=$((ROUND + 1))
done

if [ ${#COMPLETED_AGENTS[@]} -eq ${#ALL_AGENTS[@]} ]; then
    echo "✅ All agents completed successfully in $((ROUND - 1)) rounds"
else
    INCOMPLETE=()
    for agent in "${ALL_AGENTS[@]}"; do
        if [[ ! " ${COMPLETED_AGENTS[@]} " =~ " ${agent} " ]]; then
            INCOMPLETE+=("$agent")
        fi
    done
    echo "❌ Incomplete: ${INCOMPLETE[@]}"
fi
```

### Agent Prompt Template (Dependency-Check-Enabled)

**CRITICAL**: Every agent prompt includes early bailout instructions (template preserved verbatim - execution-critical)

```
ROUND {round_number} IMPLEMENTATION ATTEMPT

Your task: {agent_specific_task_description}

Expected deliverables:
{list_of_files_to_create_or_modify}

Known dependencies (may be incomplete):
{dependencies_from_context_md}

═══════════════════════════════════════════════════════════════
DEPENDENCY CHECK PROTOCOL - READ THIS FIRST
═══════════════════════════════════════════════════════════════

BEFORE implementing, verify dependencies and implement as much as possible:

1. Read context.md to understand what other agents are creating
2. Identify all files you need to import/reference
3. Use Read tool to check which dependencies exist

4. Based on available dependencies, choose ONE response:

   **OPTION A: COMPLETE Implementation**
   All dependencies available → Implement everything

   Response format:
   "COMPLETE: Implemented [full summary]

   Created/Modified:
   - File1.java: [description]
   - File2.java: [description]

   Diff written to: ../round{round_number}-{agent_type}.diff"

   **OPTION B: PARTIAL Implementation**
   Some dependencies missing → Implement what you can

   Response format:
   "PARTIAL: Implemented [what you completed]

   Completed:
   - File1.java: [description - doesn't need missing deps]
   - File2.java: [description - standalone utility]

   Deferred (missing dependencies):
   - File3.java: BLOCKED by missing DependencyX.java
   - File4.java: BLOCKED by missing DependencyY.java

   Missing dependencies:
   - DependencyX.java: Reason: [why needed], Created by: [agent]
   - DependencyY.java: Reason: [why needed], Created by: [agent]

   Diff written to: ../round{round_number}-{agent_type}.diff
   Will retry deferred items in next round."

   **OPTION C: BLOCKED (No Progress Possible)**
   All your work depends on missing dependencies

   Response format:
   "BLOCKED: Cannot proceed

   All implementation requires:
   - DependencyX.java: Reason: [why needed], Created by: [agent]
   - DependencyY.java: Reason: [why needed], Created by: [agent]

   I cannot implement anything until these dependencies are satisfied."

   IMPORTANT: Prefer PARTIAL over BLOCKED whenever possible.
   Even implementing 10% is better than blocking completely.

═══════════════════════════════════════════════════════════════
IMPORTANT: You may discover NEW dependencies during implementation
═══════════════════════════════════════════════════════════════

If you realize mid-implementation that you need something not listed above:
1. STOP implementation immediately
2. Respond: "BLOCKED: Missing [newly discovered file]"
3. Explain what you need and why
4. We will ensure it's created in the next round

═══════════════════════════════════════════════════════════════

Implementation requirements:
{detailed_requirements_for_this_agent}

Current repository state (Round {round_number}):
{output_of_find_src_-name_*.java}

Begin dependency check now.
```

### Agent Response Examples

**Example 1: COMPLETE - All dependencies available (Round 1)**
```
technical-architect response:

Dependency check: No dependencies required (creating foundation interfaces).

COMPLETE: Implemented FormattingRule.java and Violation.java

Created:
- styler-formatter-api/src/main/java/FormattingRule.java (interface)
- styler-formatter-api/src/main/java/Violation.java (record)

Diff written to: ../round1-technical-architect.diff
```

**Example 2: PARTIAL - Some dependencies missing (Round 1)**
```
code-quality-auditor response:

Dependency check:
- FormattingRule.java: NOT FOUND
- FormattingContext.java: NOT FOUND

PARTIAL: Implemented standalone formatter utilities

Completed:
- FormatterUtils.java: String manipulation utilities (no dependencies)
- ConfigParser.java: Configuration file parser (standalone)

Deferred (missing dependencies):
- LineWrapperFormatter.java: BLOCKED by missing FormattingRule.java
- ContextAwareFormatter.java: BLOCKED by missing FormattingContext.java

Missing dependencies:
- FormattingRule.java: Needed for formatter implementations, Created by: technical-architect
- FormattingContext.java: Needed for context-aware formatting, Created by: technical-architect

Diff written to: ../round1-code-quality-auditor.diff
Will retry deferred items in next round.
```

**Example 3: COMPLETE after PARTIAL (Round 2)**
```
code-quality-auditor response (continuing from Round 1 PARTIAL):

Dependency check:
- FormattingRule.java: FOUND ✅
- FormattingContext.java: FOUND ✅

COMPLETE: Implemented remaining formatter classes

Created:
- LineWrapperFormatter.java: Implements FormattingRule interface
- ContextAwareFormatter.java: Uses FormattingContext for smart formatting

Note: FormatterUtils.java and ConfigParser.java were completed in Round 1.

Diff written to: ../round2-code-quality-auditor.diff
```

**Example 4: BLOCKED - All work needs dependencies (Round 1)**
```
style-auditor response:

Dependency check:
- No Java implementation files found in repository

BLOCKED: Cannot proceed

All my work (adding JavaDoc and style fixes) requires:
- Implementation files to document
- Cannot add documentation to non-existent code

I cannot implement anything until other agents create implementation files.
Will retry in next round after implementations exist.
```

**Example 5: PARTIAL with mid-implementation discovery (Round 2)**
```
security-auditor response:

Dependency check:
- Initial dependencies verified ✅

Started implementation...

PARTIAL: Implemented basic validation, discovered new dependency

Completed:
- InputSanitizer.java: Basic input sanitization (standalone)
- SecurityUtils.java: Security utility functions (no dependencies)

Deferred (discovered during implementation):
- InputValidator.java: BLOCKED by missing ValidationRule.java

Missing dependencies (discovered):
- ValidationRule.java: Discovered while implementing InputValidator
  Reason: Need interface to define extensible validation contracts
  Created by: technical-architect
  Suggested location: styler-formatter-api/src/main/java/ValidationRule.java

Diff written to: ../round2-security-auditor.diff
Will complete InputValidator.java in next round.
```

### DIFF VALIDATION GATE (MANDATORY)

Before accepting any agent diff:

```bash
validate_agent_diff() {
    local diff_file=$1
    local agent_name=$2

    # Check file exists
    if [ ! -f "$diff_file" ]; then
        echo "❌ No diff file generated by $agent_name"
        return 1
    fi

    # Validate diff applies cleanly
    if ! git apply --check "$diff_file" 2>/dev/null; then
        echo "❌ Diff validation failed for $agent_name"
        echo "Common causes:"
        echo "  - Agent generated diff against hypothetical files (context mismatch)"
        echo "  - Agent used placeholder git index values (not real commit hashes)"
        echo "  - File changed between agent read and diff generation"
        echo ""
        echo "Resolution options:"
        echo "  1. Re-invoke agent with instruction to read actual files first"
        echo "  2. Request full file contents instead of diff"
        echo "  3. Check if agent has circular dependency issue"
        return 1
    fi

    echo "✅ Diff validation passed for $agent_name"
    return 0
}

# Usage
if validate_agent_diff "../round2-code-quality-auditor.diff" "code-quality-auditor"; then
    git apply "../round2-code-quality-auditor.diff"
else
    # Retry agent or use alternative approach
    handle_invalid_diff "code-quality-auditor"
fi
```

### Alternative to Diffs: Full File Contents

If diff validation repeatedly fails, agents can return complete file contents:

**Agent prompt modification**:
```
If you cannot generate a valid unified diff, return complete file contents in JSON format:

{
  "implementation_method": "full_files",
  "files": [
    {
      "path": "relative/path/to/File.java",
      "content": "complete file contents here",
      "action": "create" | "modify"
    }
  ],
  "summary": "Brief description of implementation"
}
```

**Main agent integration**:
```bash
if grep -q '"implementation_method": "full_files"' "../round2-agent-response.json"; then
    # Extract files and write them
    jq -r '.files[] | @json' "../round2-agent-response.json" | while read file_json; do
        path=$(echo "$file_json" | jq -r '.path')
        content=$(echo "$file_json" | jq -r '.content')
        action=$(echo "$file_json" | jq -r '.action')

        if [ "$action" = "create" ]; then
            mkdir -p "$(dirname "$path")"
        fi

        echo "$content" > "$path"
        echo "✅ Written: $path"
    done
fi
```

## Deadlock Detection and Resolution

### Deadlock Likelihood with PARTIAL Implementations

**True deadlocks rare** - agents implement standalone components while waiting.

**True Deadlock** (all must be true): ALL agents BLOCKED (no PARTIAL or COMPLETE) | Every agent's entire scope needs missing dependencies | No agent can progress

### Deadlock Patterns (Rare)

**Circular Dependency** (mostly prevented): Agent A BLOCKED needs Agent B's interface, Agent B BLOCKED needs Agent A's interface. Reality: Both PARTIAL (create utilities), Round 2 both COMPLETE.

**Missing Foundation** (usually resolved): All agents need BaseFormatter.java, no agent assigned to create it. Reality: Agents A+B PARTIAL (utilities/parsers), Agent C BLOCKED. Partial progress → dependency obvious.

**Transitive Blocking**: Nearly impossible with PARTIAL (agents implement independent components while waiting).

### Deadlock Resolution Function

```python
def handle_deadlock(blocked_agents, round_num):
    """Break deadlocks by coordinating agent work."""
    dependency_graph = {
        agent_info["agent"]: agent_info["missing"]
        for agent_info in blocked_agents
    }

    if has_circular_dependency(dependency_graph):
        resolve_circular_dependency(blocked_agents, round_num)
        return

    all_needed_files = set()
    for deps in dependency_graph.values():
        all_needed_files.update(deps)

    creating_agents = get_agents_that_create_files(blocked_agents)
    missing_foundation = all_needed_files - creating_agents.keys()

    if missing_foundation:
        resolve_missing_foundation(missing_foundation, round_num)
        return

    print("❌ UNKNOWN DEADLOCK TYPE - check agent assignments in context.md")

def resolve_circular_dependency(blocked_agents, round_num):
    """Create interfaces first, then implementations."""
    coord_prompt_template = """
    COORDINATION ROUND - Interface Definitions Only

    Circular dependency detected. Create ONLY interface/abstract class definitions:
    - Define method signatures
    - Add minimal JavaDoc
    - NO implementations (empty/abstract methods only)

    Your interface definitions: {list_of_interfaces_this_agent_should_define}
    Output to: ../round{round_num}-{agent_type}-interfaces.diff
    """

    # Launch coordination round, then retry full implementation
    # (Launch agents with coordination prompt, then re-launch with original prompts)

def resolve_missing_foundation(missing_files, round_num):
    """Assign missing files to appropriate agent."""
    for file in missing_files:
        if "Rule" in file or "Interface" in file:
            assigned_agent = "technical-architect"
        elif "Test" in file:
            assigned_agent = "code-tester"
        elif "pom.xml" in file:
            assigned_agent = "build-validator"
        else:
            assigned_agent = "technical-architect"

        foundation_prompt = f"""
        FOUNDATION FILE CREATION - Round {round_num}

        Multiple agents blocked waiting for: {file}
        Create this file because: {explain_why_this_agent_should_create_file(file, assigned_agent)}
        Requirements: {gather_requirements_from_blocked_agents(file)}
        Output to: ../round{round_num}-{assigned_agent}-foundation.diff
        """
        # (Invoke assigned_agent with foundation_prompt)
```

### Deadlock Resolution Examples

**Example 1: Circular Dependency**

```
Round 3 deadlock:
- security-auditor: BLOCKED - needs ValidationRule.java (from technical-architect)
- technical-architect: BLOCKED - needs SecurityContext.java (from security-auditor)

Resolution:
Round 3a (Coordination):
- technical-architect: Creates ValidationRule.java interface only (no impl)
- security-auditor: Creates SecurityContext.java interface only (no impl)

Round 3b (Implementation):
- technical-architect: Implements full ValidationRule.java (uses SecurityContext)
- security-auditor: Implements full SecurityContext.java (uses ValidationRule)
```

**Example 2: Missing Foundation**

```
Round 2 deadlock:
- code-quality-auditor: BLOCKED - needs FormattingContext.java
- performance-analyzer: BLOCKED - needs FormattingContext.java
- security-auditor: BLOCKED - needs FormattingContext.java

None of the agents claim to create FormattingContext.java

Resolution:
Round 2 (retry with foundation fix):
- Assign FormattingContext.java to technical-architect
- Launch technical-architect with targeted prompt to create FormattingContext
- After integration, retry all blocked agents
```

## Phase 3: CONVERGENCE State

**Objective**: Final integration and conflict resolution

**Actions**: Verify all agents completed | Resolve conflicts between agent outputs | Ensure code compiles and integrates | Update lock state to VALIDATION

**Allowed Tools**: Write/Edit ONLY for: Resolving merge conflicts | Integration glue code (minimal, documented)

**STRICTLY PROHIBITED**: ❌ New feature implementations | ❌ Business logic not from agents | ❌ Functionality agents should have created

**IF AGENTS FAILED**: Return to AUTONOMOUS_IMPLEMENTATION with refined prompts

## Phase 4: VALIDATION State

**Objective**: Build passes and quality gates met

**Actions**: Run `./mvnw verify -Dmaven.build.cache.enabled=false` | Fix build failures | Ensure quality gates pass (checkstyle, PMD, tests) | Update lock state to REVIEW

## Phase 5: REVIEW State

**Objective**: Unanimous stakeholder approval

**Actions**: Invoke review agents (same types as requirements phase) | Collect approval/rejection decisions | ANY rejection → Return to CONVERGENCE or AUTONOMOUS_IMPLEMENTATION | ALL approve → Move to USER_APPROVAL checkpoint

## Post-Compaction Recovery

**Detection**: Lock file with your session_id | context.md has "## Agent Work Assignments" | State: CONTEXT, AUTONOMOUS_IMPLEMENTATION, or CONVERGENCE

**Recovery Pattern** (preserved verbatim - execution-critical bash script):
```bash
# 1. Check lock state
LOCK_STATE=$(jq -r '.state' /workspace/locks/{task}.json)

# 2. Read context.md
cat ../context.md

# 3. Check for agent output files
ls -la ../ | grep -E "(\.diff|metadata\.json)"

# 4. Determine recovery action based on state
case "$LOCK_STATE" in
    CONTEXT)
        echo "Starting AUTONOMOUS_IMPLEMENTATION phase"
        # Begin round 1
        ;;
    AUTONOMOUS_IMPLEMENTATION)
        echo "Resuming AUTONOMOUS_IMPLEMENTATION"
        # Determine which round we're on
        LAST_ROUND=$(ls ../ | grep -oP 'round\K[0-9]+' | sort -n | tail -1)
        NEXT_ROUND=$((LAST_ROUND + 1))
        # Continue from next round
        ;;
    CONVERGENCE)
        echo "Resuming CONVERGENCE phase"
        # Apply any remaining diffs and resolve conflicts
        ;;
esac
```

## Performance Characteristics

### Speedup Comparison

**Without PARTIAL**: Flat deps 6×, Linear deps 1× (no benefit), Typical 2-3×
**With PARTIAL**: Flat deps 6×, Linear deps 3-4×, Typical 4-5×

**PARTIAL improves performance**: More agents active per round | Fewer sequential rounds | Earlier integration

**Example**: BLOCKED 7min (4 rounds, 1-3 agents/round) vs PARTIAL 4min (2 rounds, 4-6 agents/round) = 43% faster

## Context Efficiency: Early Bailout and Partial Work

**Token costs**: BLOCKED ~100-200 | PARTIAL ~2,000-5,000 | COMPLETE ~5,000-10,000

**Example Round 1**: ~14,250 tokens (4 PARTIAL + 1 COMPLETE + 2 BLOCKED) vs BLOCKED-only ~9,750 (2 agents) vs All COMPLETE ~36,000

**Tradeoff**: More tokens than pure BLOCKED (14k vs 10k), but 4 agents progress vs 2 (2× throughput) with 60% savings vs forcing all complete.

## Best Practices

**PARTIAL over BLOCKED** - Agents always try to implement something | **Launch all agents each round** - Self-select by readiness | **Trust incremental progress** - PARTIAL valuable, not failures | **Update lock state** before phase transitions | **Early bailout prompts** - Include dependency check and PARTIAL guidance | **Validate diffs** - Use `git apply --check` before applying | **Detect deadlocks quickly** - No progress = analyze dependencies | **Document partial work** - Track deferred items and reasons | **Integrate frequently** - Apply diffs after each round

## Common Mistakes

❌ Agent BLOCKED when PARTIAL possible → ✅ Implement standalone, defer dependent
❌ PARTIAL as failure → ✅ PARTIAL is incremental progress
❌ Predetermine round structure in context.md → ✅ Let rounds emerge from readiness
❌ Force complete implementation with missing deps → ✅ Accept PARTIAL, complete next round
❌ Manual code when agents report PARTIAL → ✅ Let agents finish deferred work
❌ Declare deadlock with PARTIAL → ✅ Deadlock only when ALL BLOCKED
❌ Skip dependency check in prompts → ✅ Include COMPLETE/PARTIAL/BLOCKED options
❌ Apply diffs without validation → ✅ Use `git apply --check` first
❌ Give up after missing dependencies → ✅ Continue rounds, dependencies satisfied by other agents
