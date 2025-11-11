---
description: >
  Review current session for protocol violations and suggest optimizations via multi-agent audit pipeline
---

# Session Audit Command

**Task**: Execute the full multi-agent audit pipeline to review the current session for protocol compliance,
efficiency opportunities, and documentation improvements.

## Audit Pipeline Architecture

The audit pipeline consists of multiple agents and skills with conditional branching:

```
parse-conversation-timeline skill → audit-protocol-compliance skill → CONDITIONAL BRANCH:
    (facts)         (compliance + violations)       ├─ [ANY violations] → learn-from-mistakes
                                                    │   (root cause analysis + prevention)
                                                    │
                                                    └─ [PASSED] → audit-protocol-efficiency skill
                                                        (optimizations + recommendations)
                    ↓
                config → Apply fixes (immediate + preventive)
                (apply changes)
```

**Key Innovation**: learn-from-mistakes skill provides deep root cause analysis for ALL violations
(any severity), enabling systematic prevention rather than just fixing individual instances.

## Execution Sequence

**IMPORTANT**: Skills execute synchronously, NOT in the background. When you invoke a skill with `Skill: skill-name`, it runs to completion before returning control. Do not treat skills as asynchronous background processes.

### Phase 1: Conversation Parsing (Timeline Generation)

**Skill**: parse-conversation-timeline
**Execution**: Synchronous - runs to completion before proceeding
**Purpose**: Transform 6MB raw conversation into 100-300KB structured timeline for reviewer queries
**Output**: Comprehensive structured timeline JSON with chronological events, git status, task state, statistics

**Design Philosophy**: parse-conversation-timeline skill provides comprehensive timeline that ANY reviewer can query. It doesn't pre-filter based on specific compliance checks - reviewers extract what they need.

**Architecture Benefits**:
- **Token Efficient**: Parses 6MB conversation ONCE, multiple reviewers share 300KB timeline
- **Extensible**: Adding new reviewers doesn't require updating parse-conversation-timeline skill
- **Complete**: Timeline contains ALL events - no missing data for unexpected queries
- **Independent**: Main agent cannot filter data before parse-conversation-timeline skill accesses it

**Invocation Pattern** (synchronous execution):

```
# Step 1: Invoke get-history skill (executes synchronously)
Skill: get-history
# Skill runs to completion and returns before continuing

# Step 2: Invoke parse-conversation-timeline skill (executes synchronously)
Skill: parse-conversation-timeline
# Skill runs to completion and returns before continuing
# Note: Skills execute code directly, not via Task tool

The skill will parse conversation into structured timeline. Timeline must include:

- session_metadata: session ID, task name, timestamps, conversation file location
- timeline: chronological array of ALL events (user messages, tool uses, tool results, state transitions)
- git_status: branches, commits, worktrees, merge status
- task_state: task.json contents, module existence in main, agent output files
- statistics: event counts, tool usage, approval checkpoints

For each timeline event, include:
- timestamp, sequence number, type (user_message/tool_use/tool_result/state_transition)
- actor (user/main/agent)
- context (working directory, git branch at time of event)
- file_classification (for Edit/Write: source_file/test_file/infrastructure, worktree_type)

Use methods from get-history skill to access conversation.jsonl. Output comprehensive structured timeline JSON that reviewers can query for ANY compliance or efficiency check."
```

**Timeline Schema** (see parse-conversation-timeline skill.md for full spec):
- 100-300KB structured JSON
- Chronological events with full context
- Pre-computed statistics and aggregations
- Current git and task state snapshot

### Phase 2: Compliance Review (Always Runs)

**Skill**: audit-protocol-compliance
**Execution**: Synchronous - runs to completion before proceeding
**Purpose**: Query structured timeline for protocol violations and recommend preventive changes
**Input**: Structured timeline from parse-conversation-timeline skill (already completed)
**Output**: Violations list with recommended protocol changes

**ALWAYS RUNS** - compliance review happens for every audit

**Review Process**: Compliance reviewer queries the structured timeline for specific checks. Timeline is comprehensive - reviewer extracts compliance-relevant data.

**Query Examples** (executed by audit-protocol-compliance skill):

```bash
# Check 0.0: User Approval Checkpoints
jq '.statistics.approval_checkpoints' timeline.json
→ Required: true, Found: false → VIOLATION

# Check 0.1: Task Merge to Main
jq '.task_state.task_json.state, .git_status.branches[] | select(.task_complete_but_not_merged == true)' timeline.json
→ State = COMPLETE, merged_to_main = false → VIOLATION

# Check 0.2: Main Agent Source File Creation
jq '.timeline[] | select(.actor == "main" and .file_classification.type == "source_file")' timeline.json
→ Found main agent editing source files → Check state and worktree

# Check 0.3: Working Directory Violations
jq '.timeline[] | select(.actor == "main" and .file_classification.worktree_type == "agent_worktree")' timeline.json
→ Found main agent in agent worktree → VIOLATION

# Check 0.5: Complete REQUIREMENTS Phase
jq '.task_state.agent_outputs' timeline.json
→ Missing architect output → VIOLATION
```

**Compliance Checks** (audit-protocol-compliance skill queries timeline for each):

**Check 0.0**: User approval checkpoints (query statistics.approval_checkpoints)
**Check 0.1**: Task merge to main (query git_status.branches, task_state.module_in_main)
**Check 0.2**: Main agent source file creation (query timeline for Edit/Write by main on source_file)
**Check 0.3**: Working directory violations (query timeline for main agent in agent_worktree)
**Check 0.4**: Agent worktree creation (query timeline for worktree creation before Task calls)
**Check 0.5**: Complete REQUIREMENTS phase (query task_state.agent_outputs)
**Check 1.0-3.0**: Additional protocol checks (query timeline as needed)

Invoke the audit-protocol-compliance skill (synchronous):
```
Skill: audit-protocol-compliance
# Skill executes synchronously and returns compliance results

The skill will query structured timeline from parse-conversation-timeline to execute MANDATORY compliance checks. Timeline contains comprehensive event data - extract what you need for each check.

CRITICAL checks to query:
- Check 0.0: Query statistics.approval_checkpoints for user approval after SYNTHESIS/REVIEW
- Check 0.1: Query git_status and task_state for merge-to-main before COMPLETE
- Check 0.2: Query timeline for main agent Edit/Write on source files
- Check 0.3: Query timeline for main agent operations in agent worktrees
- Check 0.5: Query task_state.agent_outputs for complete REQUIREMENTS phase

For each violation found, provide severity, evidence from timeline queries, recovery options, and recommended protocol changes. Output structured JSON with violations list and overall verdict."
```

### Phase 3: Efficiency Review (Conditional)

**Skill**: audit-protocol-efficiency
**Execution**: Synchronous - runs to completion before proceeding (if invoked)
**Purpose**: Query structured timeline for efficiency opportunities and recommend improvements
**Input**: Structured timeline from parse-conversation-timeline skill + audit-protocol-compliance skill results (both already completed)
**Condition**: ONLY runs if NO violations detected (session fully compliant)

**Decision Logic**:
```
IF audit-protocol-compliance skill contains ANY violations:
  → SKIP audit-protocol-efficiency skill (fix violations first via learn-from-mistakes)

ELSE:
  → Launch audit-protocol-efficiency skill (session compliant, optimize for efficiency)
```

**Review Process**: Efficiency reviewer queries the same structured timeline for different patterns - parallelization opportunities, redundant operations, token usage optimization.

**Query Examples** (executed by audit-protocol-efficiency skill):

```bash
# Find sequential Task calls that could be parallel
jq '.timeline[] | select(.type == "tool_use" and .tool.name == "Task") | {timestamp, agent: .tool.input.subagent_type}' timeline.json
→ 3 Task calls with <1 second apart → Could parallelize

# Find duplicate Read operations
jq '.timeline[] | select(.type == "tool_use" and .tool.name == "Read") | .tool.input.file_path' timeline.json | sort | uniq -c
→ Same file read 4 times → Cache opportunity

# Count messages/round-trips
jq '.statistics.user_messages' timeline.json
→ 12 user prompts → High round-trip count

# Find large tool outputs
jq '.timeline[] | select(.type == "tool_result" and .estimated_tokens > 5000)' timeline.json
→ Verbose outputs consuming tokens → Summarization opportunity
```

Invoke the audit-protocol-efficiency skill (if no violations - synchronous):
```
Skill: audit-protocol-efficiency
# Skill executes synchronously and returns efficiency recommendations

The skill will query structured timeline from parse-conversation-timeline to identify protocol efficiency opportunities. Timeline contains comprehensive event data - extract patterns for efficiency analysis.

Focus areas to query:
- Execution time: Query timeline for sequential Task calls that could be parallel
- Token usage: Query timeline for duplicate Read operations, verbose tool outputs
- Quality: Query timeline for missing validation, late error detection patterns

Quantify impact of each opportunity (token savings, time reduction). Recommend specific protocol changes. Output structured JSON with efficiency recommendations."
```

### Phase 4: Apply Recommendations

**Agent**: config
**Purpose**: Apply recommended changes from both reviewers
**Input**: Aggregated recommendations from audit-protocol-compliance skill + audit-protocol-efficiency skill (if ran)
**Output**: Applied changes with verification status

**Aggregation Logic**:
```
recommendations = audit-protocol-compliance skill.recommended_changes
IF audit-protocol-efficiency skill ran:
  recommendations += audit-protocol-efficiency skill.recommended_changes
```

Launch the config agent:
```
Task tool (config): "Apply Claude Code configuration changes. Input: [aggregated recommendations from reviewers]. For each recommendation: read current state, apply using Edit tool, verify by reading updated file."
```

### Phase 4a: Root Cause Analysis (MANDATORY - For ANY Violations)

**Skill**: learn-from-mistakes
**Execution**: Synchronous - runs to completion before proceeding
**Purpose**: Deep root cause analysis and systematic prevention for violations
**Input**: Violation details from audit-protocol-compliance skill (already completed)
**Condition**: MANDATORY for ANY violations (all severities: CRITICAL, HIGH, MEDIUM, LOW)

**Decision Logic**:
```
IF audit-protocol-compliance skill contains ANY violations (any severity):
  → Launch learn-from-mistakes skill with violation details
  → Get deeper root cause analysis
  → Update protocol/hooks to prevent recurrence systematically

ELSE:
  → SKIP Phase 4a (no violations found - session fully compliant)
```

**Invocation Pattern**:
```
Skill: learn-from-mistakes

Context: During /audit-session, detected [count] violations:
- [Violation 1: Brief description with severity]
- [Violation 2: Brief description with severity]

Please perform root cause analysis and recommend systematic prevention measures.
```

**Rationale**: ALL violations indicate opportunities for systematic improvement.
Even LOW severity violations reveal gaps in documentation, examples, or validation
that should be fixed to prevent recurrence. Systematic prevention is mandatory
for continuous improvement.

**Integration with Phase 5**:
- learn-from-mistakes identifies root causes and prevention strategies
- Phase 5 applies BOTH immediate fixes AND systematic prevention updates
- Prevents not just this instance, but entire category of mistakes

## Report Synthesis

After all agents complete, synthesize a comprehensive report:

### Report Structure

```markdown
## Session Audit Report

### Compliance Status: [PASSED/FAILED]
- **Overall Verdict**: [audit-protocol-compliance skill verdict]
- **Violations**: [count] ([severity breakdown])
- **Compliant Checks**: [passed]/[total]

### Critical Violations (if any)
[For each violation:]
- **Check ID**: [check number]
- **Severity**: [CRITICAL/HIGH/MEDIUM]
- **Rule**: [protocol rule violated]
- **Evidence**: [actual behavior from parse-conversation-timeline skill]
- **Recovery Options**: [numbered list from audit-protocol-compliance skill]

### Root Cause Analysis (if CRITICAL/HIGH violations)
[From learn-from-mistakes skill:]
- **Root Cause**: [underlying reason for violation]
- **Contributing Factors**: [environmental, protocol, or tooling issues]
- **Similar Incidents**: [related violations or patterns]
- **Prevention Strategy**: [systematic changes to prevent recurrence]
- **Protocol Updates**: [specific hooks, docs, or configurations to update]
- **Verification**: [how to confirm prevention measures work]

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
- `/workspace/main/.claude/agents/parse-conversation-timeline skill.md`
- `/workspace/main/.claude/agents/audit-protocol-compliance skill.md`
- `/workspace/main/.claude/agents/audit-protocol-efficiency skill.md`
- `/workspace/main/.claude/agents/config.md`
- `/workspace/main/.claude/agents/config.md`

**Methodology Reference**:
- `/workspace/main/docs/project/multi-agent-process-governance.md`

## Success Criteria

**Audit Execution Completed When**:
- [ ] parse-conversation-timeline skill produced structured JSON output
- [ ] audit-protocol-compliance skill provided binary verdict (PASSED/FAILED)
- [ ] learn-from-mistakes ran (if ANY violations) or skipped (if PASSED)
- [ ] audit-protocol-efficiency skill ran (if PASSED) or skipped (if violations found)
- [ ] config identified configuration gaps
- [ ] config applied proposed fixes
- [ ] Comprehensive report synthesized and presented

**Quality Gates**:
- parse-conversation-timeline skill output includes actual task state from task.json
- audit-protocol-compliance skill executes Check 0.1 and 0.2 FIRST
- No rationalizations in audit-protocol-compliance skill output
- learn-from-mistakes invoked for ANY violations (not just CRITICAL/HIGH)
- audit-protocol-efficiency skill only runs on fully compliant sessions (zero violations)
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
IF audit-protocol-compliance skill.overall_verdict == "FAILED":
  IF (auto-apply fixes available AND fixes address violation root causes):
    → Execute Phase 5 (fix violations first, then re-audit)
  ELSE:
    → SKIP Phase 5 (manual intervention required)

ELSE IF audit-protocol-compliance skill.overall_verdict == "PASSED":
  IF (config OR audit-protocol-efficiency skill identified fixable issues):
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
   - List all recommended fixes from audit-protocol-compliance skill and config
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

**CRITICAL**: Skills execute synchronously, NOT in background. Each skill invocation blocks until completion.

1. **Invoke parse-conversation-timeline skill** (synchronous - waits for completion)
   ```
   Skill: parse-conversation-timeline
   ```
2. **Invoke audit-protocol-compliance skill** (synchronous - waits for compliance verdict)
   ```
   Skill: audit-protocol-compliance
   ```
3. **Conditional invocation based on verdict**:
   - **If ANY violations found (any severity)**: Invoke learn-from-mistakes skill (synchronous)
     ```
     Skill: learn-from-mistakes
     ```
   - **If PASSED (zero violations)**: Invoke audit-protocol-efficiency skill (synchronous)
     ```
     Skill: audit-protocol-efficiency
     ```
   - **NOTE**: learn-from-mistakes is MANDATORY for ALL violations, not just critical ones
4. **Synthesize report** combining all skill outputs (all skills have completed by this point)
5. **Present report** to user with clear next steps
10. **Apply automatic fixes** (Phase 5):
    - Categorize all recommended fixes (including learn-from-mistakes prevention measures)
    - Auto-apply safe fixes (build, style, docs, prevention updates)
    - Verify each fix after application
    - Commit applied fixes with detailed messages
    - Report manual fixes requiring review
11. **Final verification**:
    - Run build if code fixes applied
    - Confirm all auto-fixes successful
    - Provide summary of what was fixed and prevented

Execute the full pipeline sequentially (each skill invocation is synchronous) and provide comprehensive session audit results with automatic fix application and systematic violation prevention.

**Execution Model**: Skills are synchronous, not background processes. When you invoke a skill, it runs to completion before you continue. Do not attempt to "wait for" or "check status of" skills - they complete before returning control to you.
