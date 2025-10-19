# Process Optimization Methodology

> **Version:** 1.0 | **Last Updated:** 2025-10-16
> **Purpose:** Systematic checklist for process-optimizer agent to ensure protocol correctness and efficiency

## Overview

This methodology defines checks the process-optimizer agent MUST execute to:
1. **Prevent protocol violations** (correctness)
2. **Optimize context usage** (efficiency)

Each check is numbered for TodoWrite tracking. Execute ALL checks before reporting.

---

## Category 0: MANDATORY STATE VERIFICATION

**Purpose:** Verify ACTUAL task state before analyzing any behavior (prevents post-hoc rationalization)

### Check 0.1: Task State Verification (MANDATORY FIRST CHECK)

**Pattern:** Read task.json to determine ACTUAL current state before analyzing any session behavior

**CRITICAL**: This check MUST execute FIRST before any other analysis. Do NOT skip.

**Verification Steps:**
1. **MANDATORY**: Read `/workspace/tasks/{task-name}/task.json`
2. Extract actual `state` field value
3. Document actual state vs TodoWrite state (if different)
4. Use ACTUAL state for all subsequent violation detection

**Expected States:**
-  INIT, CLASSIFIED, REQUIREMENTS, SYNTHESIS, IMPLEMENTATION, VALIDATION, REVIEW, AWAITING_USER_APPROVAL,
  COMPLETE, CLEANUP

**CRITICAL RULE**: TodoWrite state does NOT override task.json state
- Main agent may update TodoWrite to say "VALIDATION"
- But if task.json says "IMPLEMENTATION", that is the ACTUAL state
- All protocol rules apply based on task.json state, not TodoWrite state

**Fix**: If state mismatch detected, flag as separate issue and use task.json state for all checks

---

### Check 0.2: Main Agent Tool Usage in IMPLEMENTATION State (MANDATORY)

**Pattern:** If task.json state == "IMPLEMENTATION", detect if main agent used Write/Edit tools on source
files

**CRITICAL**: This is a PROTOCOL VIOLATION, not a "workflow variation"

**Verification Steps:**
1. Verify Check 0.1 completed (task.json state known)
2. IF state == "IMPLEMENTATION":
   - Search conversation for Write/Edit tool usage by main agent
   - Check tool targets: .java, .ts, .py, .jsx, .tsx files
   - Verify working directory was task worktree (not agent worktree)
3. IF Write/Edit found during IMPLEMENTATION state:
   - **IMMEDIATE CRITICAL VIOLATION FLAG**
   - Do NOT create "Workflow B" or rationalize behavior
   - Do NOT suggest "this would be OK in VALIDATION"
   - Flag as: "Main agent implemented code in IMPLEMENTATION state"

**Expected Pattern:**
- Main agent uses ONLY Task tool during IMPLEMENTATION
- All Write/Edit tools used by stakeholder agents in agent worktrees
- Main agent coordinates, does not implement

**Violation Pattern (CRITICAL):**
- Main agent uses Edit tool on FormattingViolation.java while task.json state == "IMPLEMENTATION"
- Main agent uses Write tool on any .java file while state == "IMPLEMENTATION"
- Main agent implements code directly instead of delegating to agents

**Fix:**
```
CRITICAL VIOLATION DETECTED:
- Main agent used [Write/Edit] tool on [file] while task.json state == "IMPLEMENTATION"
- This violates CLAUDE.md § Implementation Role Boundaries
- Correct approach:
  Option 1: Return to start of IMPLEMENTATION and delegate properly
  Option 2: Update task.json state to VALIDATION if implementation truly complete
  Option 3: Revert changes and re-launch code-quality-auditor to fix violations
- DO NOT rationalize as "Workflow B" or "permitted in VALIDATION" when ACTUALLY in IMPLEMENTATION
```

**Priority**: CRITICAL - Prevents protocol violations from being rationalized

---

### Check 0.3: Git History Pattern Analysis (MANDATORY)

**Pattern:** Detect suspicious single-commit patterns that suggest main agent implementation bypass

**CRITICAL**: Git history provides empirical evidence of WHO implemented code, independent of conversation content

**Verification Steps:**
1. Navigate to task branch: `cd /workspace/tasks/{task-name}/code`
2. Analyze commit history: `git log --oneline --graph task-{task-name}`
3. Count total commits in task implementation
4. Analyze commit authorship and messages
5. Check for stakeholder agent commit signatures

**Expected Pattern (Multi-Agent Implementation):**
```bash
# Multiple commits from different agents
* abc1234 [architecture-updater] Implement core FormattingRule interfaces
* abc1235 [style-updater] Apply JavaDoc and formatting standards
* abc1236 [quality-updater] Add comprehensive test suite
* abc1237 [main] Merge agent implementations to task branch
* abc1238 [main] Fix test API mismatches
```

**Violation Pattern (Main Agent Bypass):**
```bash
# Single monolithic commit
* abc1234 Implement styler-formatter-api module

# OR: Main agent commits with no agent attribution
* abc1234 Add FormattingRule.java, FormattingViolation.java, tests
* abc1235 Fix compilation errors
* abc1236 Update module-info.java
```

**Detection Algorithm:**
```bash
# Step 1: Count commits in task branch
cd /workspace/tasks/{task-name}/code
total_commits=$(git log --oneline task-{task-name} --not main | wc -l)

# Step 2: Check for agent commit signatures
agent_commits=$(git log task-{task-name} --not main --grep '\[.*-updater\]' | wc -l)
implementation_commits=$(git log task-{task-name} --not main \
  --grep '\[architecture-updater\]\|\[quality-updater\]\|\[style-updater\]' | wc -l)

# Step 3: Detect suspicious patterns
if [ "$total_commits" -eq 1 ] && [ "$agent_commits" -eq 0 ]; then
  echo "🚨 CRITICAL: Single commit with no agent signatures (main agent bypass suspected)"
elif [ "$implementation_commits" -eq 0 ] && [ "$total_commits" -gt 0 ]; then
  echo "⚠️  WARNING: No stakeholder agent implementation commits detected"
elif [ "$total_commits" -lt 3 ]; then
  echo "⚠️  WARNING: Low commit count ($total_commits) for multi-component task"
fi
```

**Violation Indicators:**
- **Single commit**: Entire module implemented in one commit (suggests main agent batch implementation)
- **No agent signatures**: Commits lack `[agent-name]` prefixes in messages
- **Low commit count**: 3+ components but <3 commits (suggests batched main agent work)
- **Main-only attribution**: All commits attributed to "main" agent with no stakeholder differentiation
- **No merge commits**: Missing merge commits from agent worktrees to task branch
- **Temporal clustering**: All commits within single timestamp (suggests single-agent batch work)

**Cross-Reference with Audit Trail** (if available):
```bash
# Verify git history matches audit-trail.json
if [ -f /workspace/tasks/{task-name}/audit-trail.json ]; then
  logged_agents=$(jq '[.agent_invocations[] | select(.agent_type | endswith("-updater"))] | length' \
    /workspace/tasks/{task-name}/audit-trail.json)

  if [ "$logged_agents" -gt 0 ] && [ "$agent_commits" -eq 0 ]; then
    echo "🚨 AUDIT MISMATCH: audit-trail.json shows agent invocations but git shows no agent commits"
  fi
fi
```

**Fix:**
```
CRITICAL VIOLATION DETECTED - Suspicious Git History Pattern:
- Total commits: [N]
- Agent implementation commits: 0
- Pattern: [Single commit / No agent signatures / Low commit count]

EVIDENCE:
$ git log --oneline task-{task-name} --not main
[paste actual output]

DIAGNOSIS:
This pattern indicates main agent implemented code directly instead of coordinating stakeholder agents.
Expected pattern: Multiple commits with agent signatures ([architecture-updater], [style-updater], etc.)

CORRECTIVE ACTION REQUIRED:
Option 1: Revert task branch to start of IMPLEMENTATION
  - git reset --hard main
  - Restart IMPLEMENTATION with proper stakeholder agent coordination
  - Ensure agents commit from their worktrees with proper signatures

Option 2: Audit and document bypass justification
  - Create audit report explaining why single-commit was necessary
  - Document as technical debt for future protocol improvement
  - Add compensating controls for quality assurance

Option 3: Post-hoc agent validation
  - Launch all stakeholder agents to review existing implementation
  - Collect comprehensive validation reports
  - Document validation results in task.md

RECOMMENDED: Option 1 (proper protocol adherence)
```

**Priority**: CRITICAL - Git history is forensic evidence that cannot be post-hoc rationalized

---

## Category 1: Implementation Actor Verification

**Purpose:** Verify WHO is implementing code matches protocol requirements

### Check 1.1: Main Agent Direct Implementation Detection

**Pattern:** Detect if main agent implemented code directly in task worktree

**Verification Steps:**
1. Read task.json to get current state
2. If state is IMPLEMENTATION or later:
   - Check git log for commits in task branch
   - Verify commit authors match expected pattern
   - Search task.md for language indicating main agent implemented directly
   - Patterns to detect:
     - "I will implement..."
     - "I have created the following files..."
     - "I implemented X, Y, Z..."
     - "Created [file] with following content..."

**Expected Pattern:**
- Main agent coordinates stakeholder agents
- Each stakeholder agent implements in their own worktree
- Commits come from agent worktrees, not task worktree

**Violation Indicators:**
- Main agent messages contain implementation language
- Git history shows commits directly to task branch before agent coordination
- Task.md contains main agent implementation details instead of coordination logs

**Fix:** If detected in IMPLEMENTATION state, recommend returning to start of IMPLEMENTATION with proper agent
coordination

---

### Check 1.2: Stakeholder Agent Invocation Pattern Analysis

**Pattern:** Verify main agent invoked stakeholder agents for implementation

**Verification Steps:**
1. Search conversation history for Task tool invocations
2. Verify agents were invoked with "implement" instructions
3. Check that agents were launched in parallel (single message)
4. Verify agents were NOT just invoked for validation/review

**Expected Pattern:**
```
Main agent message contains:
- Multiple Task tool calls in single message
- Prompt includes "implement", "create", "write code"
- Agent types: technical-architect, code-tester, code-quality-auditor, style-auditor, build-validator
```

**Violation Indicators:**
- No Task tool invocations found before code creation
- Task tool invocations only for "review" or "validate" (not "implement")
- Sequential agent launches instead of parallel
- Agents invoked after code already exists

**Fix:** Recommend restarting IMPLEMENTATION with proper agent coordination pattern

---

### Check 1.3: Role Clarity Verification

**Pattern:** Ensure main agent understands role boundaries

**Verification Steps:**
1. Check for role confusion indicators in conversation
2. Verify main agent delegated domain-specific work
3. Look for main agent doing agent-specific tasks:
   - Writing code (should delegate to technical-architect)
   - Writing tests (should delegate to code-tester)
   - Style fixes (should delegate to style-auditor)

**Expected Pattern:**
- Main agent: Coordinates, integrates, merges
- Stakeholder agents: Implement domain-specific code

**Violation Indicators:**
- Main agent messages contain domain implementation details
- No evidence of delegation to specialized agents
- Main agent doing work described in agent descriptions

**Fix:** Clarify role boundaries, restart with proper delegation

---

## Category 2: Worktree Activity Analysis

**Purpose:** Verify WHERE implementation occurs matches protocol requirements

### Check 2.1: Worktree Structure Verification

**Pattern:** Verify proper worktree structure exists

**Verification Steps:**
1. Check if task worktree exists: `/workspace/tasks/{task-name}/code/`
2. Check if agent worktrees exist: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
3. Verify at least one agent worktree created during IMPLEMENTATION

**Expected Pattern:**
```
/workspace/tasks/{task-name}/
├── code/ (task worktree, integration point)
└── agents/
    ├── technical-architect/code/
    ├── code-tester/code/
    ├── style-auditor/code/
    └── build-validator/code/
```

**Violation Indicators:**
- Agent worktrees don't exist
- Only task worktree exists
- Implementation happened without agent worktrees

**Fix:** Create agent worktrees, restart agent implementation

---

### Check 2.2: Working Directory History Analysis

**Pattern:** Verify main agent worked in correct directories

**Verification Steps:**
1. Search conversation for `pwd` commands or directory references
2. Check if main agent changed directories during implementation
3. Verify main agent didn't write files while in task worktree

**Expected Pattern:**
- Main agent stays in task worktree for coordination
- Agent implementations happen in agent worktrees
- Main agent only merges from agent worktrees

**Violation Indicators:**
- Main agent wrote files while in `/workspace/tasks/{task-name}/code/`
- No evidence of `cd` commands to agent worktrees
- File creation without worktree isolation

**Fix:** Recommend proper worktree isolation, restart with correct directory usage

---

## Category 3: Multi-Agent Architecture Compliance

**Purpose:** Verify multi-agent coordination pattern was followed

### Check 3.1: Parallel Agent Execution

**Pattern:** Verify agents were launched in parallel

**Verification Steps:**
1. Search for Task tool invocations
2. Count tool calls in each message
3. Verify multiple agents launched in single message

**Expected Pattern:**
- Single message with 5 Task tool calls (one per stakeholder agent)
- All agents receive similar "implement X" instructions
- Parallel execution, not sequential

**Violation Indicators:**
- Sequential agent launches (one per message)
- No parallel Task tool invocations
- Agents launched at different times

**Fix:** Recommend using single message with multiple Task calls for parallel execution

---

### Check 3.2: Iterative Validation Rounds

**Pattern:** Verify iterative agent validation occurred

**Verification Steps:**
1. Count number of agent validation rounds
2. Verify agents were invoked multiple times until unanimous "no more work"
3. Check for batch fixing pattern (collect all feedback → fix all → re-validate)

**Expected Pattern:**
- Round 1: Agents identify issues
- Batch fix all issues
- Round 2: Agents verify fixes
- Continue until all agents report "FINAL DECISION: ✅ APPROVED"

**Violation Indicators:**
- Only one agent validation round
- No iterative refinement
- Agents not re-invoked after fixes

**Fix:** Execute remaining validation rounds until unanimous approval

---

### Check 3.3: Agent Worktree Integration

**Pattern:** Verify agent changes were merged to task branch

**Verification Steps:**
1. Check git log for merge commits
2. Verify each agent worktree was rebased and merged
3. Confirm task branch contains all agent implementations

**Expected Pattern:**
```bash
git log --oneline task-branch shows:
- Merge agents/technical-architect
- Merge agents/code-tester
- Merge agents/style-auditor
```

**Violation Indicators:**
- No merge commits from agent worktrees
- Task branch has direct commits instead of merges
- Agent implementations not integrated

**Fix:** Rebase and merge each agent worktree to task branch

---

## Category 4: Task.md Authorship Verification

**Purpose:** Verify task.md is authored by main agent, not stakeholder agents implementing directly

### Check 4.1: Task.md Creation Timing

**Pattern:** Verify task.md created during CLASSIFIED state

**Verification Steps:**
1. Check task.json transition log for CLASSIFIED state timestamp
2. Verify task.md was created during or immediately after CLASSIFIED
3. Confirm task.md existed BEFORE agent invocations

**Expected Pattern:**
- CLASSIFIED state completes
- Main agent creates task.md
- Main agent invokes agents with "read task.md for requirements"

**Violation Indicators:**
- Task.md created during IMPLEMENTATION state
- Task.md created after code files
- No task.md at REQUIREMENTS state

**Fix:** Task.md should be created earlier; recommend protocol restart if critical

---

### Check 4.2: Task.md Content Analysis

**Pattern:** Verify task.md contains requirements, not implementation details

**Verification Steps:**
1. Read task.md content
2. Check for implementation language vs. requirements language
3. Verify format matches protocol specification

**Expected Content:**
- Task Objective (requirements)
- Stakeholder Requirements (from agent reports)
- Implementation Plan (architecture, not code)

**Violation Content:**
- Detailed code implementations
- Specific file contents
- Implementation notes from main agent

**Fix:** Refactor task.md to contain requirements only

---

## Category 5: Protocol Interpretation Validation

**Purpose:** Ensure protocol sections are correctly interpreted

### Check 5.1: Multi-Agent Architecture Section Compliance

**Pattern:** Verify understanding of multi-agent architecture section in CLAUDE.md

**Verification Steps:**
1. Read CLAUDE.md section: "Multi-Agent Architecture"
2. Compare expected behavior to actual behavior
3. Identify misinterpretations

**Expected Understanding:**
```
"Each stakeholder agent has own worktree for domain-specific implementation"
→ Agents implement, not main agent

"Agents implement in parallel, then rebase and merge to task branch"
→ Parallel execution, then integration

"Task branch is the common integration point"
→ Main agent merges, doesn't implement
```

**Violation Indicators:**
- Protocol section quoted but not followed
- Misinterpretation of "common integration point" as "main agent implements here"
- Ignoring "own worktree" requirement

**Fix:** Re-read protocol sections, restart with correct interpretation

---

### Check 5.2: IMPLEMENTATION State Requirements Validation

**Pattern:** Verify IMPLEMENTATION state requirements from task-protocol-core.md

**Verification Steps:**
1. Read task-protocol-core.md IMPLEMENTATION state section
2. Verify all requirements met:
   - Agent coordination
   - Parallel implementation
   - Iterative validation
   - Test creation
   - Pre-validation checklist

**Expected Requirements:**
- Main agent coordinates stakeholder agents
- Each agent implements in own worktree
- Tests created DURING implementation (not separate phase)
- Minimum 15 tests before state exit

**Violation Indicators:**
- Main agent implemented instead of coordinating
- No agent worktrees created
- Tests not created
- Pre-validation checklist not executed

**Fix:** Restart IMPLEMENTATION with full requirements compliance

---

## Category 6: Early Detection Triggers

**Purpose:** Catch violations early before significant work is done

### Check 6.1: Post-SYNTHESIS Early Warning

**Pattern:** After SYNTHESIS state approval, verify preparation for proper IMPLEMENTATION

**Verification Steps:**
1. Check if SYNTHESIS state just completed
2. Verify next message shows agent coordination preparation
3. Look for red flags in first IMPLEMENTATION message

**Early Warning Signals:**
- Main agent says "I will implement..."
- Main agent starts creating files
- No mention of agent coordination
- No Task tool preparation

**Expected Pattern:**
- "I will coordinate with stakeholder agents..."
-  "Launching technical-architect, code-tester, style-auditor, build-validator, code-quality-auditor in
  parallel..."
- Task tool invocations in first IMPLEMENTATION message

**Fix:** Interrupt immediately, correct approach before implementation begins

---

### Check 6.2: First File Creation Detection

**Pattern:** Detect first file creation and verify it's from agent, not main

**Verification Steps:**
1. Search for first Write or Edit tool usage after IMPLEMENTATION begins
2. Verify tool call came from stakeholder agent, not main agent
3. Check working directory context

**Expected Pattern:**
- First file creation is inside agent worktree
- Created by stakeholder agent (technical-architect, code-tester)
- Main agent only creates coordination files (task.md updates)

**Violation Pattern:**
- Main agent creates source files directly
- Files created in task worktree root
- No agent worktree context

**Fix:** Immediate interruption, restart with agent delegation

---

## Category 7: Test Coverage Validation

**Purpose:** Ensure test requirements are met during IMPLEMENTATION

### Check 7.1: Test Count Verification

**Pattern:** Verify minimum test count before IMPLEMENTATION exit

**Verification Steps:**
1. Count test files created
2. Count @Test methods across all test files
3. Verify against minimum requirement (15 tests)

**Expected Pattern:**
- At least 15 @Test methods
- Multiple test classes covering different components
- Tests created DURING IMPLEMENTATION, not after

**Violation Indicators:**
- 0 tests created
- Tests deferred to "next phase"
- Test count below minimum threshold

**Fix:** Create remaining tests before exiting IMPLEMENTATION

---

### Check 7.2: Test-First Bug Fixing

**Pattern:** Verify bugs discovered during implementation have corresponding tests

**Verification Steps:**
1. Search conversation for bug mentions
2. Verify each bug has unit test created
3. Check test names describe bug scenarios

**Expected Pattern:**
```java
@Test
public void testSpecificBugScenario() {
    // Test reproducing exact bug
}
```

**Violation Indicators:**
- Bugs mentioned but no tests created
- Generic tests instead of bug-specific tests
- Bug fixes without regression prevention

**Fix:** Create unit tests for each bug discovered

---

## Category 8: Efficiency Optimization

**Purpose:** Reduce context usage and improve session performance

### Check 8.1: Sequential Tool Call Detection

**Pattern:** Identify opportunities for parallel tool execution

**Verification Steps:**
1. Search for multiple Read tool calls in separate messages
2. Identify independent tool calls that could be parallelized
3. Calculate message savings from parallelization

**Expected Pattern:**
- Independent reads in single message
- Parallel agent launches in single message
- Batch operations where possible

**Violation Pattern:**
- Read file 1 → next message → Read file 2
- Launch agent 1 → next message → Launch agent 2
- Sequential operations for independent tasks

**Fix:** Recommend batching tool calls in single message
**Impact:** 50-70% message reduction

---

### Check 8.2: Predictive Prefetching

**Pattern:** Verify protocol files loaded early in session

**Verification Steps:**
1. Check when task-protocol-core.md was read
2. Check when task-protocol-operations.md was read
3. Verify loaded during INIT or CLASSIFIED, not later

**Expected Pattern:**
- Protocol files read during INIT phase
- Dependencies predicted and loaded upfront
- No discover → load → discover cycles

**Violation Pattern:**
- Protocol files loaded during IMPLEMENTATION
- Late discovery of requirements
- Multiple rounds of file loading

**Fix:** Load protocol files early in INIT phase
**Impact:** 5-10 fewer round-trips

---

### Check 8.3: Fail-Fast Validation

**Pattern:** Verify compilation/style checks after each component

**Verification Steps:**
1. Check for build verification frequency
2. Verify validation happens after each major component
3. Look for "implement everything then validate" anti-pattern

**Expected Pattern:**
- Create Component A → ./mvnw compile checkstyle:check
- Create Component B → ./mvnw compile checkstyle:check
- Catch issues with fresh context

**Violation Pattern:**
- Create all components → single validation at end
- Multiple files changed before validation
- Late detection of issues

**Fix:** Add validation after each component
**Impact:** 15% time savings from fresh context

---

## Execution Guidance

### For Process-Optimizer Agent

1. **Load this methodology** at start of analysis
2. **Create TodoWrite checklist** with ALL checks (format: "Check 1.1: [Description]")
3. **Execute sequentially**, marking each completed
4. **Document violations** with exact file:line references
5. **Propose fixes** with exact text replacements
6. **Generate final report** with prioritized recommendations

### Priority Levels

- **CRITICAL**: Protocol violations that invalidate task completion
- **HIGH**: Violations that create significant rework
- **MEDIUM**: Efficiency issues causing context waste
- **LOW**: Minor improvements with small impact

### Issue Severity Matrix

| Category | Violation Type | Severity |
|----------|---------------|----------|
| 0.1 | State verification not performed | CRITICAL |
| 0.2 | Main agent implements during IMPLEMENTATION state | CRITICAL |
| 0.3 | Suspicious git history pattern (single commit/no agent signatures) | CRITICAL |
| 1.x | Main agent implements directly | CRITICAL |
| 2.x | Wrong worktree usage | CRITICAL |
| 3.x | No multi-agent coordination | CRITICAL |
| 4.x | Task.md authorship issues | HIGH |
| 5.x | Protocol misinterpretation | HIGH |
| 6.x | Late detection of violations | HIGH |
| 7.x | Missing tests | CRITICAL |
| 8.x | Efficiency issues | MEDIUM-LOW |

---

## Measurement and Reporting

### Token Impact Calculation

For efficiency fixes, calculate token savings:
- **Parallelization**: (messages_before - messages_after) × avg_tokens_per_message
- **Prefetching**: avoided_rounds × (avg_read_tokens + avg_response_tokens)
- **Fail-fast**: estimated_rework_tokens × 0.15

### Success Metrics

- **Protocol Correctness**: 0 CRITICAL violations
- **Efficiency Score**: Context usage vs. theoretical minimum
- **Early Detection**: Violations caught before significant work

---

**END OF METHODOLOGY**

**Version History:**
- 1.0 (2025-10-16): Initial version with 8 core check categories
