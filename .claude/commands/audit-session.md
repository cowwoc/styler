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
process-recorder → process-compliance-reviewer → process-efficiency-reviewer → config-updater
    (facts)         (compliance + recommendations)  (efficiency + recommendations)   (apply changes)
```

## Execution Sequence

### Phase 1: Fact Gathering (Neutral Data Collection)

**Agent**: process-recorder
**Purpose**: Collect objective facts about current session execution
**Output**: JSON with conversation history, tool usage, worktrees, commits, agent invocations

Launch the process-recorder agent:
```
Task tool (process-recorder): "Collect objective facts about current session execution. Include conversation history, tool usage, worktree structure, commits, and agent invocations. Output as structured JSON."
```

### Phase 2: Compliance Review (Always Runs)

**Agent**: process-compliance-reviewer
**Purpose**: Review conversation history for protocol violations and recommend preventive changes
**Input**: process-recorder conversation history
**Output**: Violations list with recommended protocol changes

**ALWAYS RUNS** - compliance review happens for every audit

Launch the process-compliance-reviewer agent:
```
Task tool (process-compliance-reviewer): "Review conversation history for task protocol compliance violations. Input: [process-recorder output]. For each violation, recommend specific protocol changes to prevent future occurrences."
```

### Phase 3: Efficiency Review (Conditional)

**Agent**: process-efficiency-reviewer
**Purpose**: Review conversation history for efficiency opportunities and recommend improvements
**Input**: process-recorder conversation history + process-compliance-reviewer results
**Condition**: ONLY runs if no CRITICAL or HIGH severity violations detected

**Decision Logic**:
```
IF process-compliance-reviewer contains CRITICAL or HIGH severity violations:
  → SKIP process-efficiency-reviewer (fix major violations first)

ELSE:
  → Launch process-efficiency-reviewer
```

Launch the process-efficiency-reviewer agent (if no major violations):
```
Task tool (process-efficiency-reviewer): "Review conversation history for protocol efficiency opportunities. Input: [process-recorder output]. Focus: reducing execution time, reducing token usage, increasing quality. Recommend specific protocol changes."
```

### Phase 4: Apply Recommendations

**Agent**: config-updater
**Purpose**: Apply recommended changes from both reviewers
**Input**: Aggregated recommendations from process-compliance-reviewer + process-efficiency-reviewer (if ran)
**Output**: Applied changes with verification status

**Aggregation Logic**:
```
recommendations = process-compliance-reviewer.recommended_changes
IF process-efficiency-reviewer ran:
  recommendations += process-efficiency-reviewer.recommended_changes
```

Launch the config-updater agent:
```
Task tool (config-updater): "Apply Claude Code configuration changes. Input: [aggregated recommendations from reviewers]. For each recommendation: read current state, apply using Edit tool, verify by reading updated file."
```

## Report Synthesis

After all agents complete, synthesize a comprehensive report:

### Report Structure

```markdown
## Session Audit Report

### Compliance Status: [PASSED/FAILED]
- **Overall Verdict**: [process-compliance-reviewer verdict]
- **Violations**: [count] ([severity breakdown])
- **Compliant Checks**: [passed]/[total]

### Critical Violations (if any)
[For each violation:]
- **Check ID**: [check number]
- **Severity**: [CRITICAL/HIGH/MEDIUM]
- **Rule**: [protocol rule violated]
- **Evidence**: [actual behavior from process-recorder]
- **Recovery Options**: [numbered list from process-compliance-reviewer]

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
- `/workspace/main/.claude/agents/process-recorder.md`
- `/workspace/main/.claude/agents/process-compliance-reviewer.md`
- `/workspace/main/.claude/agents/process-efficiency-reviewer.md`
- `/workspace/main/.claude/agents/config-reviewer.md`
- `/workspace/main/.claude/agents/config-updater.md`

**Methodology Reference**:
- `/workspace/main/docs/project/multi-agent-process-governance.md`

## Success Criteria

**Audit Execution Completed When**:
- [ ] process-recorder produced structured JSON output
- [ ] process-compliance-reviewer provided binary verdict (PASSED/FAILED)
- [ ] process-efficiency-reviewer ran (if PASSED) or skipped (if FAILED)
- [ ] config-reviewer identified configuration gaps
- [ ] config-updater applied proposed fixes
- [ ] Comprehensive report synthesized and presented

**Quality Gates**:
- process-recorder output includes actual task state from task.json
- process-compliance-reviewer executes Check 0.1 and 0.2 FIRST
- No rationalizations in process-compliance-reviewer output
- process-efficiency-reviewer only runs on PASSED sessions
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
IF process-compliance-reviewer.overall_verdict == "FAILED":
  IF (auto-apply fixes available AND fixes address violation root causes):
    → Execute Phase 5 (fix violations first, then re-audit)
  ELSE:
    → SKIP Phase 5 (manual intervention required)

ELSE IF process-compliance-reviewer.overall_verdict == "PASSED":
  IF (config-reviewer OR process-efficiency-reviewer identified fixable issues):
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
Refer to [main-agent-coordination.md § Post-Implementation Issue Handling Decision Tree](../../../docs/project/main-agent-coordination.md#post-implementation-issue-handling-decision-tree) for state-based fix permissions alignment

### Fix Application Process

After presenting the audit report:

1. **Categorize Fixes**:
   - List all recommended fixes from process-compliance-reviewer and config-reviewer
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

1. **Launch process-recorder** to gather session facts
2. **Wait for completion** and capture JSON output
3. **Launch process-compliance-reviewer** with tracer output as input
4. **Wait for compliance verdict**
5. **Conditional launch of process-efficiency-reviewer** (only if PASSED)
6. **Launch config-reviewer** to identify configuration gaps
7. **Launch config-updater** to apply proposed fixes
8. **Synthesize report** combining all agent outputs
9. **Present report** to user with clear next steps
10. **Apply automatic fixes** (Phase 5):
    - Categorize all recommended fixes
    - Auto-apply safe fixes (build, style, docs)
    - Verify each fix after application
    - Commit applied fixes with detailed messages
    - Report manual fixes requiring review
11. **Final verification**:
    - Run build if code fixes applied
    - Confirm all auto-fixes successful
    - Provide summary of what was fixed

Execute the full pipeline sequentially and provide comprehensive session audit results with automatic fix
application.
