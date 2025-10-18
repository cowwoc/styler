# Multi-Agent Process Governance Design

> **Version:** 2.0 | **Created:** 2025-10-16
> **Purpose:** Separation of concerns for process correctness, efficiency, and documentation quality

## Problem Statement

**Current Flaw**: Single `process-optimizer` agent has conflicting responsibilities:
- **Audit correctness** (adversarial, strict, binary)
- **Optimize efficiency** (collaborative, flexible, continuous)

**Result**: Agent rationalizes violations instead of flagging them (observed 5+ times)

## Proposed Multi-Agent Architecture

### Agent Pipeline

```
User Request: "Review session for violations"
    ↓
[1] execution-tracer → Collects neutral facts
    ↓
[2] protocol-auditor → Binary violation detection (PASS/FAIL)
    ↓
[3] efficiency-optimizer → Suggests improvements (assumes correctness)
    ↓
[4] documentation-auditor → Finds ambiguities/contradictions
    ↓
Final Report: Violations + Optimizations + Doc Fixes
```

---

## Agent 1: execution-tracer

**Role**: Neutral fact gatherer (no judgments)

**Responsibility**:
- Collect objective facts about session execution
- No interpretation, no recommendations
- Pure data extraction

**Output Format**:
```json
{
  "task_state_actual": "IMPLEMENTATION",
  "task_state_todowrite": "VALIDATION",
  "tool_usage": [
    {"tool": "Edit", "target": "FormattingViolation.java", "timestamp": "...", "actor": "main"},
    {"tool": "Task", "agent": "code-quality-auditor", "timestamp": "...", "actor": "main"}
  ],
  "worktrees_created": [
    "/workspace/tasks/implement-formatter-api/code",
    "/workspace/tasks/implement-formatter-api/agents/technical-architect/code"
  ],
  "commits": [
    {"sha": "abc123", "message": "...", "branch": "implement-formatter-api"}
  ],
  "agents_invoked": [
    {"agent": "technical-architect", "state_when_invoked": "IMPLEMENTATION"},
    {"agent": "code-quality-auditor", "state_when_invoked": "IMPLEMENTATION"}
  ]
}
```

**Key Characteristics**:
- ✅ Objective data collection
- ✅ No interpretation
- ✅ Machine-readable output
- ❌ No recommendations
- ❌ No violation flagging

**Tools**: Read, Grep, Bash (ls, git log)

---

## Agent 2: protocol-auditor

**Role**: Adversarial compliance checker (strict, binary)

**Responsibility**:
- Check facts from execution-tracer against protocol rules
- Binary output: VIOLATION or COMPLIANT (no gray area)
- No rationalizations, no "this would be OK if..."

**Input**: execution-tracer JSON output

**Output Format**:
```json
{
  "violations": [
    {
      "check_id": "0.2",
      "severity": "CRITICAL",
      "rule": "Main agent MUST NOT use Write/Edit on source files during IMPLEMENTATION state",
      "actual": "Main agent used Edit tool on FormattingViolation.java while task.json state == IMPLEMENTATION",
      "evidence": {
        "task_state": "IMPLEMENTATION",
        "tool_used": "Edit",
        "target_file": "FormattingViolation.java",
        "actor": "main"
      },
      "verdict": "VIOLATION",
      "recovery_options": [
        "Option 1: Revert changes and return to start of IMPLEMENTATION",
        "Option 2: Update task.json to VALIDATION if implementation complete",
        "Option 3: Re-launch code-quality-auditor to fix violations properly"
      ]
    }
  ],
  "compliant_checks": [
    {"check_id": "1.1", "rule": "Main agent must coordinate stakeholders", "verdict": "COMPLIANT"},
    {"check_id": "2.1", "rule": "Worktree structure must exist", "verdict": "COMPLIANT"}
  ],
  "summary": {
    "total_checks": 25,
    "violations": 1,
    "compliant": 24,
    "overall_verdict": "FAILED"
  }
}
```

**Key Characteristics**:
- ✅ Strict rule enforcement
- ✅ Binary verdicts (no rationalization)
- ✅ Evidence-based (uses execution-tracer data)
- ✅ Clear recovery options
- ❌ No efficiency suggestions
- ❌ No documentation fixes
- ❌ No "this would be OK in different state" rationalizations

**Audit Checklist** (from process-optimization-methodology.md):
```
Category 0: MANDATORY STATE VERIFICATION
  [0.1] Task state verification
  [0.2] Main agent tool usage in IMPLEMENTATION state

Category 1: Implementation Actor Verification
  [1.1] Main agent direct implementation detection
  [1.2] Stakeholder agent invocation pattern
  [1.3] Role clarity verification

Category 2: Worktree Activity Analysis
  [2.1] Worktree structure verification
  [2.2] Working directory history

Category 3: Multi-Agent Architecture Compliance
  [3.1] Parallel agent execution
  [3.2] Iterative validation rounds
  [3.3] Agent worktree integration

... (all 25 checks)
```

**Tools**: Read (methodology, protocol docs, execution-tracer output)

**CRITICAL RULE**: If execution-tracer shows `task_state_actual == "IMPLEMENTATION"` and `tool_usage` contains `Edit` by `main` on `.java` file, **IMMEDIATE VIOLATION FLAG**. Do NOT check "what state should it be" - flag the violation in the ACTUAL state.

---

## Agent 3: efficiency-optimizer

**Role**: Collaborative performance advisor (helpful, flexible)

**Responsibility**:
- **ONLY runs if protocol-auditor verdict == "COMPLIANT"**
- Suggests efficiency improvements
- Identifies parallelization opportunities
- Recommends prefetching patterns
- Calculates token savings

**Input**: execution-tracer JSON output

**Output Format**:
```json
{
  "optimizations": [
    {
      "id": "E1",
      "category": "parallelization",
      "current_pattern": "Sequential agent launches (3 messages)",
      "recommended_pattern": "Single message with 3 Task tool calls",
      "token_savings": 8000,
      "implementation": "Launch all agents in single message using parallel Task calls"
    },
    {
      "id": "E2",
      "category": "fail_fast",
      "current_pattern": "Validate after all components created",
      "recommended_pattern": "Validate after each component",
      "time_savings": "15%",
      "implementation": "Add './mvnw checkstyle:check' after each file creation"
    }
  ],
  "token_impact": {
    "current_usage": 95000,
    "optimized_usage": 72000,
    "savings": 23000,
    "roi": "24% reduction"
  }
}
```

**Key Characteristics**:
- ✅ Assumes correctness already verified
- ✅ Collaborative suggestions
- ✅ Quantified improvements
- ❌ Never touches protocol violations
- ❌ Only runs on COMPLIANT sessions

**Optimization Categories**:
1. Parallelization (batch tool calls)
2. Prefetching (load early)
3. Fail-fast validation (incremental checks)
4. Context reduction (smaller responses)

**Tools**: Read (execution-tracer output, protocol docs)

---

## Agent 4: documentation-auditor

**Role**: Technical writer quality checker (clarity, consistency)

**Responsibility**:
- Find ambiguities in protocol documentation
- Detect contradictions between documents
- Identify missing edge case guidance
- Propose clarity improvements

**Input**:
- protocol-auditor violations (to find doc gaps)
- CLAUDE.md, task-protocol-*.md files

**Output Format**:
```json
{
  "ambiguities": [
    {
      "id": "D1",
      "severity": "HIGH",
      "file": "CLAUDE.md",
      "line": 150,
      "issue": "Section says 'main agent coordinates' but doesn't specify which agents can implement vs review-only",
      "confusion_caused": "Main agent unclear if fixing violations violates protocol",
      "proposed_fix": "Add 'Stakeholder Agent Capabilities Matrix' section clearly listing implementation vs review-only agents",
      "related_violation": "protocol-auditor check 0.2 failed due to this ambiguity"
    }
  ],
  "contradictions": [
    {
      "id": "D2",
      "severity": "MEDIUM",
      "files": ["CLAUDE.md:175", "task-protocol-operations.md:231"],
      "contradiction": "CLAUDE.md shows 'coordination only' but operations doc template uses 'review' language",
      "proposed_resolution": "Align both documents to use 'implement' language for initial rounds, 'review' for iterative rounds"
    }
  ],
  "missing_guidance": [
    {
      "id": "D3",
      "severity": "HIGH",
      "scenario": "Agent reports tool access limitations",
      "current_guidance": "None found",
      "needed_guidance": "Recovery workflow when agents can't access Edit tool",
      "proposed_addition": "Add 'Agent Tool Limitation Recovery Pattern' to CLAUDE.md"
    }
  ]
}
```

**Key Characteristics**:
- ✅ Finds root causes of violations (ambiguous docs)
- ✅ Proposes specific text improvements
- ✅ Cross-references violations to doc gaps
- ❌ Doesn't detect violations (that's protocol-auditor's job)

**Tools**: Read, Grep, Edit (for proposing fixes)

---

## Sequential Pipeline Usage

### Step 1: User Invocation
```
User: "Review session for violations and optimize"
```

### Step 2: Main Agent Orchestrates Pipeline

```bash
# Phase 1: Fact Gathering (neutral)
Task tool (execution-tracer): "Collect facts about current session execution"

# Phase 2: Compliance Audit (adversarial)
Task tool (protocol-auditor): "Audit facts against protocol rules. Input: execution-tracer output"

# Phase 3: Conditional Optimization (collaborative)
IF protocol-auditor verdict == "COMPLIANT":
  Task tool (efficiency-optimizer): "Suggest optimizations. Input: execution-tracer output"
ELSE:
  Skip optimization (fix violations first)

# Phase 4: Documentation Improvement (quality)
Task tool (documentation-auditor): "Find doc ambiguities that caused violations. Input: protocol-auditor violations"
```

### Step 3: Main Agent Synthesizes Report

```markdown
## Session Review Report

### Compliance Status: FAILED
- **Violations**: 1 CRITICAL
- **Compliant Checks**: 24/25

### Critical Violation
- **Check 0.2**: Main agent used Edit tool during IMPLEMENTATION state
- **Evidence**: task.json state == IMPLEMENTATION, Edit tool on FormattingViolation.java
- **Recovery Options**: (see protocol-auditor output)

### Efficiency Optimizations
- Skipped (fix violations first)

### Documentation Improvements
- **D1**: Add agent capabilities matrix to CLAUDE.md
- **D3**: Add tool limitation recovery workflow
```

---

## Agent Configuration Files

### execution-tracer.md
```yaml
---
name: execution-tracer
description: Neutral fact gatherer for session execution analysis
tools: [Read, Grep, Bash, LS]
model: sonnet-4-5
color: gray
---

**Role**: Collect objective facts about session execution (no interpretation)

**Output**: JSON with task state, tool usage, worktrees, commits, agent invocations

**CRITICAL**: Do NOT make judgments or recommendations - just collect data
```

### protocol-auditor.md
```yaml
---
name: protocol-auditor
description: Adversarial compliance checker for protocol violations
tools: [Read]
model: sonnet-4-5
color: red
---

**Role**: Check execution-tracer facts against protocol rules (strict, binary)

**CRITICAL RULES**:
1. Check task.json state FIRST (Check 0.1)
2. If state == IMPLEMENTATION and main agent used Write/Edit on source files, FLAG IMMEDIATELY (Check 0.2)
3. Do NOT rationalize violations
4. Binary verdicts: VIOLATION or COMPLIANT (no gray area)
5. Provide evidence and recovery options

**Input**: execution-tracer JSON output
**Output**: Violations list with verdicts
```

### efficiency-optimizer.md
```yaml
---
name: efficiency-optimizer
description: Collaborative performance advisor for process optimization
tools: [Read]
model: sonnet-4-5
color: green
---

**Role**: Suggest efficiency improvements (helpful, quantified)

**CRITICAL RULE**: ONLY runs if protocol-auditor verdict == "COMPLIANT"

**Focus Areas**:
- Parallelization opportunities
- Prefetching patterns
- Fail-fast validation
- Token usage reduction

**Output**: Optimization suggestions with token savings
```

### documentation-auditor.md
```yaml
---
name: documentation-auditor
description: Technical writer quality checker for protocol documentation
tools: [Read, Grep, Edit]
model: sonnet-4-5
color: blue
---

**Role**: Find ambiguities and contradictions in protocol docs

**Input**: protocol-auditor violations (to identify doc gaps)

**Output**: Ambiguities, contradictions, missing guidance with proposed fixes
```

---

## Benefits of Multi-Agent Architecture

### 1. Separation of Concerns
- **execution-tracer**: Facts only (no bias)
- **protocol-auditor**: Strict enforcement (no rationalization)
- **efficiency-optimizer**: Helpful suggestions (no violation detection)
- **documentation-auditor**: Clarity improvements (no compliance checking)

### 2. No Conflicting Responsibilities
- Auditor can't rationalize violations (not its job)
- Optimizer can't flag violations (assumes correctness)
- Each agent has single, clear purpose

### 3. Sequential Dependencies
- Tracer runs first (provides facts)
- Auditor uses tracer facts (objective audit)
- Optimizer only runs if compliant (no wasted effort)
- Documentation auditor identifies root causes (improves protocol)

### 4. Failure Isolation
- If auditor finds violations, optimizer skips (fix first)
- If tracer fails, whole pipeline stops (need facts)
- Each agent can fail independently

### 5. Accountability
- Clear attribution: "protocol-auditor flagged violation"
- No ambiguity about who found what
- Easier to debug agent failures

---

## Migration Path

### Phase 1: Deprecate process-optimizer
- Mark as deprecated in agent config
- Document replacement: "Use execution-tracer → protocol-auditor → efficiency-optimizer pipeline"

### Phase 2: Create New Agents
1. Create execution-tracer.md (simplest, facts only)
2. Create protocol-auditor.md (copy checks from methodology, make strict)
3. Create efficiency-optimizer.md (extract efficiency checks from methodology)
4. Create documentation-auditor.md (new functionality)

### Phase 3: Update Methodology
- Split process-optimization-methodology.md into:
  - protocol-audit-checklist.md (for protocol-auditor)
  - efficiency-patterns.md (for efficiency-optimizer)
  - documentation-quality-standards.md (for documentation-auditor)

### Phase 4: Update CLAUDE.md
- Replace references to "process-optimizer"
- Document multi-agent pipeline
- Provide usage examples

---

## Testing the New Architecture

### Test Case: Current Session Violation

**Input**: Current implement-formatter-api session

**Expected execution-tracer output**:
```json
{
  "task_state_actual": "IMPLEMENTATION",
  "task_state_todowrite": "VALIDATION",
  "tool_usage": [
    {"tool": "Edit", "target": "FormattingViolation.java", "actor": "main", "timestamp": "..."}
  ]
}
```

**Expected protocol-auditor output**:
```json
{
  "violations": [
    {
      "check_id": "0.2",
      "severity": "CRITICAL",
      "rule": "Main agent MUST NOT use Write/Edit on source files during IMPLEMENTATION state",
      "verdict": "VIOLATION",
      "evidence": {
        "task_state": "IMPLEMENTATION",
        "tool_used": "Edit",
        "target_file": "FormattingViolation.java"
      }
    }
  ],
  "overall_verdict": "FAILED"
}
```

**Expected efficiency-optimizer output**:
```
SKIPPED (violations must be fixed first)
```

**Expected documentation-auditor output**:
```json
{
  "ambiguities": [
    {
      "id": "D1",
      "issue": "CLAUDE.md doesn't specify when main agent can fix violations",
      "proposed_fix": "Add workflow matrix: IMPLEMENTATION = delegate, VALIDATION = main agent can fix"
    }
  ]
}
```

**Result**: Clear violation detection with no rationalization

---

## Success Metrics

- **Accuracy**: 0 false negatives (all violations caught)
- **Clarity**: Binary verdicts (no ambiguity)
- **Efficiency**: Skip optimization when violations exist
- **Improvement**: Documentation fixes prevent future violations

---

**END OF MULTI-AGENT PROCESS GOVERNANCE DESIGN**
