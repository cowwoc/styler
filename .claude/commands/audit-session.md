---
description: >
  Review current session for protocol violations and suggest optimizations via multi-agent audit pipeline
---

# Session Audit Command

**Task**: Execute the full multi-agent audit pipeline to review the current session for protocol compliance,
efficiency opportunities, and documentation improvements.

## Audit Pipeline Architecture

The audit pipeline consists of five sequential agents:

```
process-recorder → process-reviewer → process-updater → documentation-reviewer → documentation-updater
    (facts)         (compliance)      (performance)        (identify)              (apply)
```

## Execution Sequence

### Phase 1: Fact Gathering (Neutral Data Collection)

**Agent**: process-recorder
**Purpose**: Collect objective facts about current session execution
**Output**: JSON with task state, tool usage, worktrees, commits, agent invocations

Launch the process-recorder agent:
```
Task tool (process-recorder): "Collect objective facts about current session execution. Include: task state (from task.json), tool usage history, worktree structure, commits, and agent invocations. Output as structured JSON."
```

### Phase 2: Compliance Audit (Strict Binary Enforcement)

**Agent**: process-reviewer
**Purpose**: Check execution facts against protocol rules with zero tolerance
**Input**: process-recorder JSON output
**Output**: Violations list with verdicts (PASS/FAIL)

Launch the process-reviewer agent:
```
Task tool (process-reviewer): "Audit execution facts against protocol rules. Input: [process-recorder JSON output]. Execute Check 0.1 (state verification) and Check 0.2 (IMPLEMENTATION tool usage) FIRST. Binary verdicts only - no rationalizations."
```

### Phase 3: Conditional Optimization (Performance Suggestions)

**Agent**: process-updater
**Purpose**: Suggest efficiency improvements (ONLY if process-reviewer passed)
**Input**: process-recorder JSON output
**Condition**: ONLY runs if process-reviewer verdict == "PASSED"

**Decision Logic**:
```
IF process-reviewer.overall_verdict == "FAILED":
  → SKIP process-updater (fix violations first)

ELSE IF process-reviewer.overall_verdict == "PASSED":
  → Launch process-updater agent
```

Launch the process-updater agent (if compliant):
```
Task tool (process-updater): "Suggest efficiency improvements based on execution patterns. Input: [process-recorder JSON output]. Focus: parallelization, prefetching, fail-fast validation, token reduction."
```

### Phase 4a: Documentation Quality Analysis (Identification)

**Agent**: documentation-reviewer
**Purpose**: Identify ambiguities in protocol docs that caused violations
**Input**: process-reviewer violations (if any)
**Output**: Proposed fixes JSON

**Execution Logic**:
```
IF process-reviewer.overall_verdict == "FAILED":
  → Launch documentation-reviewer to find doc ambiguities that CAUSED violations
  → Focus: Root cause analysis - which doc gaps led to observed violations?

ELSE IF process-reviewer.overall_verdict == "PASSED":
  → Launch documentation-reviewer to find PREVENTIVE improvements
  → Focus: Forward-looking analysis - which doc ambiguities COULD cause future violations?
```

**Key Difference**:
- FAILED sessions: Diagnostic mode (explain past violations)
- PASSED sessions: Preventive mode (avoid future violations)

Launch the documentation-reviewer agent:
```
Task tool (documentation-reviewer): "Find ambiguities in protocol documentation that caused violations. Input: [process-reviewer violations]. Identify doc gaps, contradictions, and missing edge case guidance. Output proposed fixes as JSON."
```

### Phase 4b: Documentation Fix Application

**Agent**: documentation-updater
**Purpose**: Apply proposed fixes from documentation-reviewer
**Input**: documentation-reviewer proposed fixes JSON
**Output**: Applied fixes with verification status

Launch the documentation-updater agent:
```
Task tool (documentation-updater): "Apply documentation fixes. Input: [documentation-reviewer proposed fixes]. For each fix: read current state, apply using Edit tool, verify by reading updated file. Output applied fixes with verification status."
```

## Report Synthesis

After all agents complete, synthesize a comprehensive report:

### Report Structure

```markdown
## Session Audit Report

### Compliance Status: [PASSED/FAILED]
- **Overall Verdict**: [process-reviewer verdict]
- **Violations**: [count] ([severity breakdown])
- **Compliant Checks**: [passed]/[total]

### Critical Violations (if any)
[For each violation:]
- **Check ID**: [check number]
- **Severity**: [CRITICAL/HIGH/MEDIUM]
- **Rule**: [protocol rule violated]
- **Evidence**: [actual behavior from process-recorder]
- **Recovery Options**: [numbered list from process-reviewer]

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
- `/workspace/main/.claude/agents/process-reviewer.md`
- `/workspace/main/.claude/agents/process-updater.md`
- `/workspace/main/.claude/agents/documentation-reviewer.md`
- `/workspace/main/.claude/agents/documentation-updater.md`

**Methodology Reference**:
- `/workspace/main/docs/project/multi-agent-process-governance.md`

## Success Criteria

**Audit Execution Completed When**:
- [ ] process-recorder produced structured JSON output
- [ ] process-reviewer provided binary verdict (PASSED/FAILED)
- [ ] process-updater ran (if PASSED) or skipped (if FAILED)
- [ ] documentation-reviewer identified doc gaps
- [ ] documentation-updater applied proposed fixes
- [ ] Comprehensive report synthesized and presented

**Quality Gates**:
- process-recorder output includes actual task state from task.json
- process-reviewer executes Check 0.1 and 0.2 FIRST
- No rationalizations in process-reviewer output
- process-updater only runs on PASSED sessions
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
IF process-reviewer.overall_verdict == "FAILED":
  IF (auto-apply fixes available AND fixes address violation root causes):
    → Execute Phase 5 (fix violations first, then re-audit)
  ELSE:
    → SKIP Phase 5 (manual intervention required)

ELSE IF process-reviewer.overall_verdict == "PASSED":
  IF (documentation-reviewer OR process-updater identified fixable issues):
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
   - List all recommended fixes from process-reviewer and documentation-reviewer
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
3. **Launch process-reviewer** with tracer output as input
4. **Wait for compliance verdict**
5. **Conditional launch of process-updater** (only if PASSED)
6. **Launch documentation-reviewer** to identify doc gaps
7. **Launch documentation-updater** to apply proposed fixes
8. **Synthesize report** combining all agent outputs
9. **Present report** to user with clear next steps
10. **Apply automatic fixes** (Phase 5):
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
