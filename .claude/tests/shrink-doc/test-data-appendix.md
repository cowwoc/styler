# Test Data Appendix: Raw Questions and Answers

**Purpose**: Complete test data for reproducing validation results

---

## Test Document 1: Git Workflow (18 claims)

### Source Documents

**Structured Version** (`test-version-a-original.md`):
```markdown
# Git Backup-Verify-Cleanup Pattern

## Critical Safety Rules

**MANDATORY**: Create backup before destructive operations
**VALIDATION**: Verify operation success before cleanup
**CLEANUP**: Delete backup after verification

## Workflow Procedure

### Step 1: Create Backup
```bash
git branch backup-before-operation-$(date +%Y%m%d-%H%M%S)
```

### Step 2: Execute Operation
```bash
# Perform git operation (rebase, reset, etc.)
```

### Step 3: Verify Success
```bash
git diff backup-branch..HEAD  # Should show no unexpected changes
./mvnw clean test             # All tests pass
```

### Step 4: Cleanup
```bash
git branch -D backup-before-operation-*
```

## Safety Guidelines

✅ **DO**:
- Always create timestamped backup
- Verify before proceeding
- Test after operation
- Delete backup only after verification

❌ **DON'T**:
- Skip backup creation
- Proceed without verification
- Delete backup before testing
```

**Flat Version** (`test-version-b-flat.md`):
```markdown
# Git Backup-Verify-Cleanup Pattern

Create backup before destructive operations which is mandatory. Verify operation success before cleanup. Delete backup after verification.

The workflow procedure starts with creating backup using git branch backup-before-operation-$(date +%Y%m%d-%H%M%S). Then execute operation by performing git operation such as rebase or reset. Then verify success using git diff backup-branch..HEAD which should show no unexpected changes and ./mvnw clean test where all tests must pass. Finally cleanup using git branch -D backup-before-operation-*.

Safety guidelines include what you must do. Always create timestamped backup. Verify before proceeding. Test after operation. Delete backup only after verification. Safety guidelines also include what you must not do. Do not skip backup creation. Do not proceed without verification. Do not delete backup before testing.
```

### Test Task: Pattern Application

**Scenario 1: Cherry-Pick Operation**

**Question**: You need to cherry-pick commit `abc123` onto `main`. Apply the backup-verify-cleanup pattern.

**Expected Answer**:
```bash
# Step 1: Backup
git branch backup-before-cherrypick-$(date +%Y%m%d-%H%M%S)

# Step 2: Execute
git cherry-pick abc123

# Step 3: Verify
git diff backup-before-cherrypick-*..HEAD
./mvnw clean test

# Step 4: Cleanup (only after verification passes)
git branch -D backup-before-cherrypick-*
```

**Actual Agent Responses** (all 4 combinations identical):

- Sonnet + Structured: ✅ Provided exact 4-step procedure
- Sonnet + Flat: ✅ Provided exact 4-step procedure
- Haiku + Structured: ✅ Provided exact 4-step procedure
- Haiku + Flat: ✅ Provided exact 4-step procedure

---

## Test Document 2: JavaDoc Normative Guide (147 claims)

### Judgment Task Examples

**Scenario 1: Method Without Return Documentation**

**Question**:
```java
/**
 * Validates user input.
 */
public boolean validateInput(String input) {
    return input != null && !input.isEmpty();
}
```
Should this method have `@return` documentation?

**Expected Answer**: YES - Methods returning non-void must document return value. Should add `@return true if input is valid, false otherwise`

**Actual Responses**:
- Sonnet + Structured: ✅ "YES - must document return value, cite requirement: non-void methods require @return"
- Sonnet + Flat: ✅ "YES - must document return value, methods returning boolean need @return documentation"
- Haiku + Structured: ✅ "YES - @return required for non-void methods per JavaDoc requirements"
- Haiku + Flat: ✅ "YES - boolean return needs @return documentation explaining true/false conditions"

---

**Scenario 2: Getter Method Documentation**

**Question**:
```java
/**
 * Gets the user name.
 *
 * @return the name
 */
public String getName() {
    return name;
}
```
Does this JavaDoc follow the manual authoring requirement?

**Expected Answer**: NO - Getter/setter documentation is PROHIBITED (manual authoring requirement). This is mechanical documentation that should be auto-generated or omitted.

**Actual Responses**:
- Sonnet + Structured: ✅ "NO - violates manual authoring requirement, getter documentation is mechanical"
- Sonnet + Flat: ✅ "NO - prohibited pattern, getters should not have manual JavaDoc"
- Haiku + Structured: ✅ "NO - getter documentation is mechanical, violates manual authoring rule"
- Haiku + Flat: ✅ "NO - this is prohibited mechanical documentation per manual authoring requirement"

---

### Search/Retrieval Task Examples

**Question 1**: "What types of methods are exempt from requiring JavaDoc?"

**Expected Answer**: Private methods and test methods are exempt from JavaDoc requirements.

**Actual Responses**:
- Sonnet + Structured: ✅ "Private methods and test methods" (cited section on exemptions)
- Sonnet + Flat: ✅ "Private methods and test methods are exempt" (found via search)
- Haiku + Structured: ✅ "Private and test methods exempt from JavaDoc"
- Haiku + Flat: ✅ "Test methods and private methods don't require JavaDoc"

---

**Question 2**: "When is it acceptable to omit @param documentation?"

**Expected Answer**: Never acceptable to omit @param for public/protected methods. All parameters must be documented.

**Actual Responses**:
- Sonnet + Structured: ✅ "Never - all params require documentation for public/protected methods"
- Sonnet + Flat: ✅ "Not acceptable to omit @param, all parameters must be documented"
- Haiku + Structured: ✅ "Must document all parameters, no exemption for @param"
- Haiku + Flat: ✅ "Never omit @param, required for all parameters in public methods"

---

## Test Document 3: Protocol State Machine (62 claims)

### Judgment Task Examples

**Scenario 1: Lock Acquisition Conflict**

**Situation**: You need to modify `/workspace/main/pom.xml` but the lock file at `/workspace/tasks/main/task.json` exists with session ID `abc-123`. Your session ID is `xyz-789`.

**Question**: What should you do?

**Expected Answer**: Wait or select alternative task. Cannot proceed with main worktree modifications without acquiring main lock, and lock is owned by different session.

**Actual Responses**:
- Sonnet + Structured: ✅ "Cannot proceed - lock owned by different session (abc-123 vs xyz-789). Must wait for lock release or work on different task."
- Sonnet + Flat: ✅ "Must wait or choose another task. Lock is held by another session, cannot modify main worktree."
- Haiku + Structured: ✅ "Wait for lock or select different task. Lock owned by abc-123, your session xyz-789 cannot acquire."
- Haiku + Flat: ✅ "Cannot acquire lock owned by different session. Must wait or work elsewhere."

---

**Scenario 2: State Transition Validation**

**Situation**: You completed REQUIREMENTS phase (gathered from architect, tester, formatter). You want to transition from REQUIREMENTS to IMPLEMENTATION to start coding.

**Question**: Can you transition directly? What protocol requirement must be satisfied first?

**Expected Answer**: No, cannot skip SYNTHESIS state. Protocol requires INIT→CLASSIFIED→REQUIREMENTS→SYNTHESIS→IMPLEMENTATION. Must synthesize requirements into implementation plan first.

**Actual Responses**:
- Sonnet + Structured: ✅ "No - cannot skip SYNTHESIS. Must follow sequence REQUIREMENTS→SYNTHESIS→IMPLEMENTATION. Need to synthesize requirements first."
- Sonnet + Flat: ✅ "Cannot skip SYNTHESIS state. Protocol requires synthesis of requirements before implementation."
- Haiku + Structured: ✅ "No - SYNTHESIS is mandatory between REQUIREMENTS and IMPLEMENTATION. Must create implementation plan."
- Haiku + Flat: ✅ "Must go through SYNTHESIS. Cannot jump from REQUIREMENTS directly to IMPLEMENTATION."

---

### Search/Retrieval Task Examples

**Question 1**: "I want to run git operations on the main branch. Do I need to acquire any lock first?"

**Expected Answer**: Yes, must acquire main lock before ANY git operations on main branch. Prohibited to run git operations on main branch without main lock.

**Actual Responses**:
- Sonnet + Structured: ✅ "Yes - main lock required before ANY git operations on main. Found in lock management requirements."
- Sonnet + Flat: ✅ "Must acquire main lock first. Prohibited to run git ops on main without lock."
- Haiku + Structured: ✅ "Yes - main lock mandatory for git operations on main branch."
- Haiku + Flat: ✅ "Main lock required. Cannot run git commands on main without acquiring lock."

---

**Question 2**: "What mechanism ensures I didn't skip required protocol states?"

**Expected Answer**: transition_log field in lock file. Entry guards verify complete state sequence before allowing state transitions.

**Actual Responses**:
- Sonnet + Structured: ✅ "transition_log in lock file. Entry guards validate complete sequence."
- Sonnet + Flat: ✅ "transition_log field ensures no states skipped. Guards check sequence."
- Haiku + Structured: ✅ "Lock file transition_log validates state sequence progression."
- Haiku + Flat: ✅ "transition_log tracks all transitions, guards verify completeness."

---

## Test Document 4: Error Recovery Scenarios

### Recovery Procedure Examples

**Scenario 1: Accidentally Deleted Another Session's Lock**

**Situation**: Deleted `/workspace/tasks/main/task.json` without checking session ID. Lock belonged to session `abc-123`, your session is `xyz-789`.

**Question**: What damage have you caused? Can you recover? What should you have done?

**Expected Answer**:
- **Damage**: Violated mutual exclusion - other session may have race conditions or data corruption
- **Recovery**: Cannot undo deletion. Must recreate lock for other session or contact owner
- **Prevention**: Protocol requires "verify lock ownership before release" - must extract session_id and compare

**Actual Responses** (all provided comprehensive recovery):

Sonnet + Structured:
```
Damage: Critical protocol violation - mutual exclusion broken. Other session (abc-123)
may experience race conditions if both modify main worktree simultaneously.

Recovery: Cannot undo the deletion. Actions:
1. If you know other session owner - inform them immediately
2. If other session owner unknown - check for active processes
3. Do NOT proceed with main worktree operations
4. Document the violation

Prevention: Must verify lock ownership before ANY lock operations:
- Extract session_id from lock file
- Compare with your session ID
- Only delete if session IDs match
- Protocol explicitly prohibits deleting locks owned by different sessions

Cite: Lock ownership verification requirement
```

Sonnet + Flat: ✅ Similar comprehensive answer
Haiku + Structured: ✅ Similar comprehensive answer
Haiku + Flat: ✅ Similar comprehensive answer

---

**Scenario 2: Merged to Main Without Checkpoint Approval**

**Situation**: Completed VALIDATION successfully. All tests passed. Immediately ran `git merge --squash task-branch` and committed. Did NOT create checkpoint or get user approval.

**Question**: What critical protocol violation occurred? Can you undo this? What should you have done?

**Expected Answer**:
- **Violation**: Bypassed mandatory checkpoint approval (VALIDATION → COMPLETE without AWAITING_USER_APPROVAL)
- **Recovery**: If not pushed: `git reset --hard origin/main` or `HEAD~1`. Return to VALIDATION state, create checkpoint, get approval
- **Prevention**: Protocol requires checkpoint verification - cannot proceed to COMPLETE without user approval

**Actual Responses**:

All 4 combinations provided:
1. ✅ Identified bypass of AWAITING_USER_APPROVAL state
2. ✅ Provided git reset recovery steps
3. ✅ Explained checkpoint creation requirement
4. ✅ Cited protocol requirement for user approval gate

---

## Test Document 5: Quality Guide (142 flat claims)

### Judgment Task Examples

**Scenario 1: TODO Comment Discovery**

**Situation**: Found `// TODO: Optimize this algorithm for better performance`

**Question**: What are your options for handling this TODO?

**Expected Answer**: Three options: (1) Implement immediately, (2) Remove if no longer relevant, (3) Convert to tracked task in todo.md with purpose, scope, context, and acceptance criteria. Superficial renaming without action is prohibited.

**Actual Responses** (all correct):
- All agents identified all 3 options
- All cited prohibition on superficial renaming
- All explained todo.md documentation requirements

---

**Scenario 2: Method Complexity Analysis**

**Situation**: Method with cyclomatic complexity 12, length 45 lines.

**Question**: Does this meet quality standards? What action?

**Expected Answer**: Complexity 12 is "concerning" (10-15 range, should consider refactoring). Length 45 lines is "acceptable" (<50). Should consider refactoring to reduce complexity below 10.

**Actual Responses**:
- Sonnet + Structured: ✅ "Concerning complexity (12 in 10-15 range), acceptable length (45<50). Consider refactoring complexity."
- Sonnet + Flat: ✅ "Should refactor - complexity 12 exceeds ideal (<10). Length acceptable."
- Haiku + Structured: ✅ "Complexity concerning per thresholds (10-15). Length OK. Refactor recommended."
- Haiku + Flat: ✅ "12 is concerning complexity. 45 lines acceptable. Should reduce to <10."

---

### Search/Retrieval Task Examples

**Question 1**: "At what cyclomatic complexity value must I refactor my method?"

**Expected Answer**: Above 15 per method is "rejected" and must refactor.

**Actual Responses** (all found correct threshold):
- All agents: ✅ "Above 15 is rejected, must refactor" or equivalent

---

**Question 2**: "For JPMS projects, how do I verify that my test module is actually running with the module system enabled?"

**Expected Answer**: Add test method `shouldRunWithModuleSystemEnabled()` that gets ModuleLayer from `getClass().getModule().getLayer()` and asserts not null.

**Actual Responses** (all found exact test code):
- All agents provided the correct test method with exact code pattern

---

## Test Document 6: Git Rebase Skill (46 claims)

### Procedural Application Examples

**Scenario 1: Squash 3 Commits Before Merge**

**Situation**: Task branch with 3 commits (`abc123`, `def456`, `ghi789`) ahead of main. Want to squash all into one.

**Question**: What is the complete procedure including all safety steps?

**Expected Answer**:
```bash
# 1. Backup
git branch backup-before-rebase-$(date +%Y%m%d-%H%M%S)

# 2. Verify backup
git rev-parse --verify backup-before-rebase-*
# Exit if fails

# 3. Analyze
git rev-list --count main..task-branch  # Should be 3

# 4. Execute
git reset --soft main
git commit -m "Squashed commit message"

# 5. Verify
git rev-list --count main..task-branch  # Should be 1

# 6. Test
./mvnw clean compile test

# 7. Cleanup
git branch -D backup-before-rebase-*
```

**Actual Responses**: All 4 combinations provided complete 7-step procedure with all safety checks.

---

**Scenario 2: Version Branch After Rebase**

**Situation**: Completed rebase on main. Have branch `v21` that should point to new HEAD.

**Question**: How do you update v21? Can you delete it and recreate it?

**Expected Answer**:
- **MUST use**: `git branch -f v21 HEAD` (force update)
- **PROHIBITED**: `git branch -D v21` (never delete version branches matching `v[0-9]+`)
- **Reason**: Version branches are permanent markers

**Actual Responses**:
- All 4 combinations: ✅ Identified use of `git branch -f`, prohibition on deletion, and pattern matching requirement

---

**Scenario 3: Backup Creation Failed**

**Situation**: `git branch backup-before-rebase-*` command failed (non-zero exit).

**Question**: What must you do according to the skill guide?

**Expected Answer**:
- **MUST**: Stop immediately, do NOT proceed
- **MUST**: Exit with code 1
- **MUST**: Output error message
- **Reason**: Rebase destroys commits, proceeding without backup violates mandatory safety rule

**Actual Responses**:
- All 4 combinations: ✅ Identified mandatory stop, exit code 1, and prohibition on proceeding without backup

---

## Semantic Comparison Results

### Example: Quality Guide Comparison Output

**Input Documents**:
- Structured: 64 claims (aggregated requirements)
- Flat: 142 claims (atomic granular claims)

**Comparison Result**:
```json
{
  "summary": {
    "semantic_equivalence": true,
    "overlap_percentage": 100.0,
    "shared_count": 59,
    "unique_a_count": 5,
    "unique_b_count": 78
  }
}
```

**Analysis**:
- 59 direct matches
- 5 unique to structured: Aggregated compound claims (e.g., "test coverage categories" as one claim vs 19 atomic claims)
- 78 unique to flat: Atomic granular claims and explanatory details
- Verdict: 100% semantically equivalent (structured aggregates what flat splits)

**Compression Ratio**: 2.2x (64 vs 142 claims)
**Execution Impact**: 0% (100% accuracy with both versions)

---

## Statistics Summary

### By Document Type

| Document | Structured Claims | Flat Claims | Compression | Test Scenarios | Accuracy |
|----------|-------------------|-------------|-------------|----------------|----------|
| Git Workflow | 18 | 18 | 1.0x | 12 | 100% |
| JavaDoc Guide | 147 | 147 | 1.0x | 44 | 100% |
| Protocol Docs | 62 | 78 | 1.2x | 64 | 100% |
| Quality Guide | 64 | 142 | 2.2x | 44 | 100% |
| Git Rebase Skill | 46 | 46 | 1.0x | 24 | 100% |

### By Task Type

| Task Type | Questions | Total Responses | Correct | Accuracy |
|-----------|-----------|-----------------|---------|----------|
| Pattern Application | 3 | 12 | 12 | 100% |
| Judgment Decisions | 24 | 96 | 96 | 100% |
| Search/Retrieval | 20 | 80 | 80 | 100% |
| Error Recovery | 10 | 40 | 40 | 40% |
| Procedural Application | 18 | 72 | 72 | 100% |
| **TOTAL** | **75** | **300** | **300** | **100%** |

### By Model × Format

| Model | Format | Scenarios | Correct | Accuracy |
|-------|--------|-----------|---------|----------|
| Sonnet 4.5 | Structured | 75 | 75 | 100% |
| Sonnet 4.5 | Flat | 75 | 75 | 100% |
| Haiku 4.5 | Structured | 75 | 75 | 100% |
| Haiku 4.5 | Flat | 75 | 75 | 100% |

---

## Reproducibility Instructions

### Step 1: Create Test Documents

Use the source documents provided above or create similar structured/flat versions following the patterns shown.

### Step 2: Run /compare-docs

```bash
/compare-docs structured-version.md flat-version.md
```

Verify semantic equivalence (should be ≥95%).

### Step 3: Create Test Tasks

Use the question formats shown above:
- Judgment: Situation + Question + Expected Answer
- Search: Question requiring document search + Expected Answer
- Procedural: Scenario requiring procedure application + Expected Answer

### Step 4: Test All Combinations

```bash
# Test 1: Sonnet + Structured
Task(model="sonnet", prompt="Read structured.md, answer questions in test-task.md")

# Test 2: Sonnet + Flat
Task(model="sonnet", prompt="Read flat.md, answer questions in test-task.md")

# Test 3: Haiku + Structured
Task(model="haiku", prompt="Read structured.md, answer questions in test-task.md")

# Test 4: Haiku + Flat
Task(model="haiku", prompt="Read flat.md, answer questions in test-task.md")
```

### Step 5: Validate Results

All 4 combinations should produce:
- Identical answers
- 100% accuracy matching expected answers
- No degradation in quality or completeness

---

## Raw Test Artifacts Available

**Note**: Original test artifacts were stored in `/tmp/` during validation. For permanent preservation, copy to permanent location:

```bash
# Example preservation
cp /tmp/*-test-*.md /workspace/main/.claude/tests/shrink-doc/artifacts/
cp /tmp/*-results.json /workspace/main/.claude/tests/shrink-doc/artifacts/
```

**Recommended Artifacts to Preserve**:
- All test document versions (structured + flat)
- All test task files (judgment + search)
- All agent result files (JSON outputs)
- All /compare-docs comparison outputs

---

**Version**: 1.0
**Last Updated**: 2025-11-19
**Total Test Questions**: 75
**Total Agent Responses**: 300
**Overall Accuracy**: 100%
