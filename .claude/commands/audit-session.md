---
description: >
  Review current session for protocol violations and suggest optimizations via multi-agent audit pipeline
---

# Session Audit Command

**Task**: Execute the full multi-agent audit pipeline to review the current session for protocol compliance,
efficiency opportunities, and documentation improvements.

## Audit Pipeline Architecture

The audit pipeline consists of four sequential agents:

```
execution-tracer → protocol-auditor → efficiency-optimizer → documentation-auditor
     (facts)          (compliance)        (performance)         (clarity)
```

## Execution Sequence

### Phase 1: Fact Gathering (Neutral Data Collection)

**Agent**: execution-tracer
**Purpose**: Collect objective facts about current session execution
**Output**: JSON with task state, tool usage, worktrees, commits, agent invocations

Launch the execution-tracer agent:
```
Task tool (execution-tracer): "Collect objective facts about current session execution. Include: task state (from task.json), tool usage history, worktree structure, commits, and agent invocations. Output as structured JSON."
```

### Phase 2: Compliance Audit (Strict Binary Enforcement)

**Agent**: protocol-auditor
**Purpose**: Check execution facts against protocol rules with zero tolerance
**Input**: execution-tracer JSON output
**Output**: Violations list with verdicts (PASS/FAIL)

Launch the protocol-auditor agent:
```
Task tool (protocol-auditor): "Audit execution facts against protocol rules. Input: [execution-tracer JSON output]. Execute Check 0.1 (state verification) and Check 0.2 (IMPLEMENTATION tool usage) FIRST. Binary verdicts only - no rationalizations."
```

### Phase 3: Conditional Optimization (Performance Suggestions)

**Agent**: efficiency-optimizer
**Purpose**: Suggest efficiency improvements (ONLY if protocol-auditor passed)
**Input**: execution-tracer JSON output
**Condition**: ONLY runs if protocol-auditor verdict == "PASSED"

**Decision Logic**:
```
IF protocol-auditor.overall_verdict == "FAILED":
  → SKIP efficiency-optimizer (fix violations first)

ELSE IF protocol-auditor.overall_verdict == "PASSED":
  → Launch efficiency-optimizer agent
```

Launch the efficiency-optimizer agent (if compliant):
```
Task tool (efficiency-optimizer): "Suggest efficiency improvements based on execution patterns. Input: [execution-tracer JSON output]. Focus: parallelization, prefetching, fail-fast validation, token reduction."
```

### Phase 4: Documentation Quality Analysis

**Agent**: documentation-auditor
**Purpose**: Identify ambiguities in protocol docs that caused violations
**Input**: protocol-auditor violations (if any)
**Output**: Ambiguities, contradictions, missing guidance with proposed fixes

**Execution Logic**:
```
IF protocol-auditor.overall_verdict == "FAILED":
  → Launch documentation-auditor to find doc ambiguities that CAUSED violations
  → Focus: Root cause analysis - which doc gaps led to observed violations?

ELSE IF protocol-auditor.overall_verdict == "PASSED":
  → Launch documentation-auditor to find PREVENTIVE improvements
  → Focus: Forward-looking analysis - which doc ambiguities COULD cause future violations?
```

**Key Difference**:
- FAILED sessions: Diagnostic mode (explain past violations)
- PASSED sessions: Preventive mode (avoid future violations)

Launch the documentation-auditor agent:
```
Task tool (documentation-auditor): "Find ambiguities in protocol documentation that caused violations. Input: [protocol-auditor violations]. Identify doc gaps, contradictions, and missing edge case guidance."
```

## Report Synthesis

After all agents complete, synthesize a comprehensive report:

### Report Structure

```markdown
## Session Audit Report

### Compliance Status: [PASSED/FAILED]
- **Overall Verdict**: [protocol-auditor verdict]
- **Violations**: [count] ([severity breakdown])
- **Compliant Checks**: [passed]/[total]

### Critical Violations (if any)
[For each violation:]
- **Check ID**: [check number]
- **Severity**: [CRITICAL/HIGH/MEDIUM]
- **Rule**: [protocol rule violated]
- **Evidence**: [actual behavior from execution-tracer]
- **Recovery Options**: [numbered list from protocol-auditor]

### Efficiency Optimizations
[If PASSED:]
- **Optimization ID**: [optimization identifier]
- **Category**: [parallelization/prefetching/fail-fast/context]
- **Current Pattern**: [anti-pattern detected]
- **Recommended Pattern**: [optimal approach]
- **Impact**: [token savings or time reduction]

[If FAILED:]
- Skipped (fix violations first)

### Documentation Improvements
- **Ambiguity ID**: [identifier]
- **Severity**: [HIGH/MEDIUM/LOW]
- **File**: [CLAUDE.md or task-protocol-*.md]
- **Issue**: [description of ambiguity]
- **Related Violation**: [which check failed due to this]
- **Proposed Fix**: [specific text improvement]

### Summary
- **Token Impact**: [if optimizations available]
- **Next Steps**: [prioritized action items]
```

## Agent Configuration References

**Agent Locations**:
- `/workspace/main/.claude/agents/execution-tracer.md`
- `/workspace/main/.claude/agents/protocol-auditor.md`
- `/workspace/main/.claude/agents/efficiency-optimizer.md`
- `/workspace/main/.claude/agents/documentation-auditor.md`

**Methodology Reference**:
- `/workspace/main/docs/project/multi-agent-process-governance.md`

## Success Criteria

**Audit Execution Completed When**:
- [ ] execution-tracer produced structured JSON output
- [ ] protocol-auditor provided binary verdict (PASSED/FAILED)
- [ ] efficiency-optimizer ran (if PASSED) or skipped (if FAILED)
- [ ] documentation-auditor identified doc gaps
- [ ] Comprehensive report synthesized and presented

**Quality Gates**:
- execution-tracer output includes actual task state from task.json
- protocol-auditor executes Check 0.1 and 0.2 FIRST
- No rationalizations in protocol-auditor output
- efficiency-optimizer only runs on PASSED sessions
- Report includes actionable recovery options for violations

## Expected Output Format

Present the audit report to the user with:
1. Clear compliance status (PASSED/FAILED)
2. Detailed violation breakdown (if any)
3. Efficiency suggestions (if compliant)
4. Documentation improvements needed
5. Prioritized next steps

## Phase 5: Automatic Fix Application

**Purpose**: Automatically apply recommended fixes after audit completion
**Condition**: Apply fixes based on severity and risk assessment

**Decision Logic**:
```
IF protocol-auditor.overall_verdict == "FAILED":
  IF (auto-apply fixes available AND fixes address violation root causes):
    → Execute Phase 5 (fix violations first, then re-audit)
  ELSE:
    → SKIP Phase 5 (manual intervention required)

ELSE IF protocol-auditor.overall_verdict == "PASSED":
  IF (documentation-auditor OR efficiency-optimizer identified fixable issues):
    → Execute Phase 5 (preventive improvements)
  ELSE:
    → SKIP Phase 5 (no fixes needed)
```

**Priority Rule**: Violation fixes take precedence over efficiency improvements

### Fix Categories

**Auto-Apply (Safe Fixes)**:
1. **Build Failures**: Missing dependencies, JPMS warnings, compilation errors
2. **Style Violations**: Checkstyle/PMD issues identified in audit
3. **Documentation Updates**: Protocol clarifications, missing examples
4. **Uncommitted Changes**: Validation fixes that should have been committed

**Manual Review Required (Complex Fixes)**:
5. **Architecture Changes**: Refactorings, API modifications
6. **Breaking Changes**: Changes that affect existing integrations
7. **Test Updates**: Test modifications beyond simple assertions

### Fix Categorization Decision Criteria

**Auto-Apply Safe Fix Requirements** (ALL must be true):
1. **Isolated Impact**: Fix does not affect public APIs, contracts, or module boundaries
2. **Mechanical Nature**: Fix is deterministic (e.g., add import, fix whitespace) with no judgment calls
3. **Low Volume**: For style violations, count ≤ 5 fixes OR all fixes identical pattern
4. **Verifiable**: Can verify success with automated tooling (compile, test, checkstyle)
5. **Reversible**: Can be reverted without side effects if incorrect

**Manual Review Trigger Conditions** (ANY triggers manual review):
1. **Judgment Required**: Fix requires domain expertise or architectural decisions
2. **High Risk**: Changes affect security, performance, or data integrity
3. **High Volume**: >5 style violations OR complex refactoring patterns
4. **Test Logic Changes**: Beyond simple assertion value updates
5. **Breaking Changes**: Any modification to public contracts or integrations
6. **Uncertain Scope**: Impact radius unclear or spans multiple components

**Examples**:
- ✅ Auto-Apply: Add `import java.util.List;` to fix compilation error (isolated, mechanical, verifiable)
- ✅ Auto-Apply: Fix 3 whitespace violations in single file (low volume, mechanical)
- ❌ Manual Review: Add 15 missing JavaDoc comments (high volume, requires contextual understanding)
- ❌ Manual Review: Update method signature to fix API contract (breaking change)
- ❌ Manual Review: Refactor class hierarchy (architectural decision)

**Decision Tree Integration**:
Refer to CLAUDE.md § Post-Implementation Issue Handling Decision Tree (line 267) for state-based fix
permissions alignment

### Fix Application Process

After presenting the audit report:

1. **Categorize Fixes**:
   - List all recommended fixes from protocol-auditor and documentation-auditor
   - Classify each as auto-apply or manual-review
   - Provide rationale for classification
   - **Prioritize fixes** using this order:
     1. Build Failures (blocking all other fixes)
     2. Compilation Errors (enables other verifications)
     3. Test Failures (validates correctness)
     4. Uncommitted Changes (protocol compliance)
     5. Style Violations (quality improvements)
     6. Documentation Updates (reflects current state)
   - **Check dependencies**: If Fix B depends on Fix A, apply A first regardless of category
   - **Group by file**: When possible, apply all fixes to same file together to reduce tool calls

2. **Apply Safe Fixes Automatically**:
   ```
   For each auto-apply fix:
     - Execute the fix (Edit/Write tool)
     - Verify the fix (compile, test, validate)
     - Report success or failure
   ```

3. **Commit Applied Fixes**:
   ```bash
   git add <modified-files>
   git commit -m "Apply audit fixes: [brief description]

   Fixes applied by /audit-session:
   - [Fix 1 description]
   - [Fix 2 description]

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude <noreply@anthropic.com>"
   ```

4. **Report Manual Fixes**:
   - List fixes requiring manual review
   - Provide detailed instructions for each
   - Suggest creating tasks if appropriate

### Example Fix Scenarios

**Scenario 1: Missing `requires transitive` (Build Failure)**
- **Category**: Auto-apply (build failure)
- **Fix**: Update module-info.java with `requires transitive core` and `requires transitive io.github.cowwoc.styler.security`
- **Verification**: `./mvnw compile -pl :styler-formatter`
- **Commit**: "Fix JPMS warnings in formatter module with requires transitive"

**Scenario 2: Uncommitted Validation Fixes**
- **Category**: Auto-apply (protocol violation)
- **Detection**: git status shows uncommitted changes after VALIDATION
- **Fix**: Commit the changes before proceeding to next state
- **Commit**: "Commit validation fixes from [state] state"

**Scenario 3: Documentation Ambiguity**
- **Category**: Auto-apply (documentation update)
- **Fix**: Add explicit requirement to protocol documentation
- **Verification**: Read updated doc to confirm clarity
- **Commit**: "Clarify [protocol requirement] in [file]"

### Success Criteria

**Automatic Fix Application Complete When**:
- [ ] All auto-apply fixes attempted
- [ ] Verification passed for each applied fix
- [ ] All changes committed with descriptive messages
- [ ] Manual review fixes documented with instructions
- [ ] Final build verification passed (if build fixes applied)

## Execution Instructions

1. **Launch execution-tracer** to gather session facts
2. **Wait for completion** and capture JSON output
3. **Launch protocol-auditor** with tracer output as input
4. **Wait for compliance verdict**
5. **Conditional launch of efficiency-optimizer** (only if PASSED)
6. **Launch documentation-auditor** to identify doc gaps
7. **Synthesize report** combining all agent outputs
8. **Present report** to user with clear next steps
9. **Apply automatic fixes** (new Phase 5):
   - Categorize all recommended fixes
   - Auto-apply safe fixes (build, style, docs)
   - Verify each fix after application
   - Commit applied fixes with detailed messages
   - Report manual fixes requiring review
10. **Final verification**:
    - Run build if code fixes applied
    - Confirm all auto-fixes successful
    - Provide summary of what was fixed

Execute the full pipeline sequentially and provide comprehensive session audit results with automatic fix
application.
