# Task Protocol for Sub-Agents

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** All sub-agents (reviewers and updaters)
> **Purpose:** Agent coordination protocol, worktree structure, and status tracking

## 🚨 MANDATORY: Read This Document at Startup {#mandatory-read-this-document-at-startup}

ALL sub-agents MUST read this document BEFORE performing any work to ensure proper coordination, worktree usage, status tracking, and iterative collaboration.

## Worktree Structure {#worktree-structure}

### Directory Layout {#directory-layout}

```
/workspace/
├── main/                           # Main git repository and branch
│   ├── src/                        # Production code
│   ├── docs/                       # Documentation
│   └── pom.xml                     # Build configuration
│
└── tasks/
    └── {task-name}/                # Task root directory
        ├── task.json               # Lock file with task state
        ├── task.md                 # Requirements and plans
        ├── code/                   # Task branch worktree (main agent creates)
        │   ├── src/                # Task implementation
        │   └── pom.xml             # Modified build config
        │
        └── agents/
            ├── architect/
            │   └── status.json     # Reviewer status tracking
            ├── architect/
            │   ├── code/           # Updater's isolated worktree
            │   │   ├── src/        # Implementation work
            │   │   └── pom.xml
            │   └── status.json     # Updater status tracking
            ├── quality/
            │   └── status.json
            └── quality/
                ├── code/
                └── status.json
```

### Critical Path Distinctions {#critical-path-distinctions}

**Stakeholder Agents in IMPLEMENTATION Mode**:
- **YOUR worktree**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
- **Work here**: Implement changes in isolation
- **Validate here**: Run `./mvnw verify` in YOUR worktree
- **Merge to**: Task branch at `/workspace/tasks/{task-name}/code/`

**Stakeholder Agents in VALIDATION Mode**:
- **Review location**: `/workspace/tasks/{task-name}/code/` (task branch, NOT agent worktrees)
- **What to review**: Changes that have been merged to task branch by agents in implementation mode
- **DO NOT review**: Individual agent worktrees (those are WIP)

**Main Agent**:
- **Task worktree**: `/workspace/tasks/{task-name}/code/`
- **Monitors**: All agent status.json files in `/workspace/tasks/{task-name}/agents/*/status.json`
- **Coordinates**: Agent invocations, state transitions, final merge to main branch

## Status Tracking Protocol {#status-tracking-protocol}

### Status File Location {#status-file-location}

**Every agent** (reviewer and updater) MUST maintain a `status.json` file:

```
/workspace/tasks/{task-name}/agents/{agent-name}/status.json
```

### Status File Formats {#status-file-formats}

**For Stakeholder Agents in REQUIREMENTS/VALIDATION Mode**:

```json
{
  "agent": "architect",
  "task": "implement-feature-x",
  "status": "WORKING|COMPLETE|BLOCKED",
  "decision": "APPROVED|REJECTED|PENDING",
  "round": 3,
  "last_review_sha": "abc123def456",
  "work_remaining": "none|description of pending work",
  "feedback": "detailed feedback for agent (implementation mode) (if REJECTED)",
  "updated_at": "2025-10-15T10:30:00Z"
}
```

**Required fields for REQUIREMENTS/VALIDATION mode**:
- `decision`: Main agent checks this to determine completion (APPROVED/REJECTED/PENDING)
- `feedback`: Detailed feedback for implementation agent when REJECTED

**For Stakeholder Agents in IMPLEMENTATION Mode**:

```json
{
  "agent": "architect",
  "task": "implement-feature-x",
  "status": "WORKING|COMPLETE|BLOCKED",
  "round": 3,
  "last_merge_sha": "abc123def456",
  "work_remaining": "none|description of pending work",
  "blocked_by": null,
  "updated_at": "2025-10-15T10:30:00Z"
}
```

**Required updater fields**:
- `last_merge_sha`: Git SHA of last merge to task branch
- `work_remaining`: "none" when complete

## Agent Response Verbosity Guidelines {#agent-response-verbosity-guidelines}

Return metadata-only status reports, NOT full code listings.

**✅ CORRECT**:
```
Implementation complete.
- Files created: FormattingRule.java, TransformationContext.java, FormattingViolation.java
- Commit SHA: abc1234
- Validation: PASSED (22/22 tests, 0 violations)
```

**❌ VIOLATION**:
```
Implementation complete. Here is the code:

[Full code listing of 3 files × 200 lines each = 600 lines]
```

### Status Update Commands {#status-update-commands}

**Agent (implementation mode) after merging**:

```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}/status.json <<EOF
{
  "agent": "{AGENT}",
  "task": "{TASK}",
  "mode": "implementation",
  "status": "COMPLETE",
  "work_remaining": "none",
  "round": ${CURRENT_ROUND},
  "last_merge_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

**Stakeholder Agents in REQUIREMENTS/VALIDATION Mode after reviewing**:

```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}/status.json <<EOF
{
  "agent": "{AGENT}",
  "task": "{TASK}",
  "mode": "validation",
  "status": "COMPLETE",
  "decision": "APPROVED",
  "work_remaining": "none",
  "round": ${CURRENT_ROUND},
  "last_review_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "feedback": ""
}
EOF
```

**If reviewer rejects changes**:

```bash
cat > /workspace/tasks/{TASK}/agents/{AGENT}-reviewer/status.json <<EOF
{
  "agent": "{AGENT}-reviewer",
  "task": "{TASK}",
  "status": "COMPLETE",
  "decision": "REJECTED",
  "work_remaining": "updater must address feedback",
  "round": ${CURRENT_ROUND},
  "last_review_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "feedback": "Detailed list of issues:\n1. Interface contracts are ambiguous\n2. Missing null validation\n3. Insufficient error handling"
}
EOF
```

## Iterative Workflow Pattern {#iterative-workflow-pattern}

### Single Agent Round Pattern {#single-agent-round-pattern}

```
1. Agent (implementation mode) implements in their worktree (/workspace/tasks/{task}/agents/{agent}-updater/code)
2. Agent (implementation mode) validates locally: ./mvnw verify (in their worktree)
3. Agent (implementation mode) merges to task branch (/workspace/tasks/{task}/code)
4. Agent (implementation mode) updates status.json with COMPLETE
5. Agent (review mode) reviews what was merged to task branch
6. Agent (review mode) decides: APPROVED or REJECTED
   - If APPROVED: Round complete, update status.json with decision=APPROVED
   - If REJECTED: Update status.json with decision=REJECTED and feedback
7. If REJECTED: Agent (implementation mode) reads feedback → fixes issues → merge → repeat from step 5
8. Round complete when reviewer decision=APPROVED
```

### Multi-Agent Round Flow Example {#multi-agent-round-flow-example}

```
Round 1 - Initial Implementation:
├─ architect: Implement core interfaces → merge to task branch
├─ quality: Apply refactoring → merge to task branch
├─ style: Apply style rules → merge to task branch
├─ test: Implement test suite → merge to task branch
└─ All updaters: Update status.json with COMPLETE

Round 1 - Review Merged Changes:
├─ architect: Review task branch → REJECTED (ambiguous contracts)
├─ quality: Review task branch → APPROVED ✓
├─ style: Review task branch → REJECTED (12 violations)
├─ test: Review task branch → APPROVED ✓
└─ Reviewers: Update status.json with decision

Round 2 - Apply Reviewer Feedback:
├─ architect: Fix contracts per feedback → merge
├─ style: Fix 12 violations per feedback → merge
└─ Updaters: Update status.json

Round 2 - Re-review Fixes:
├─ architect: Re-review → APPROVED ✓
├─ style: Re-review → APPROVED ✓
└─ All reviewers now APPROVED: Round complete

Main agent checks all reviewer status.json files:
- All have decision=APPROVED → Transition to VALIDATION state
```

### Agent Deliverable Verification {#agent-deliverable-verification}

**MANDATORY**: Agents in IMPLEMENTATION mode MUST verify their deliverables before reporting completion.

**Compilation Requirement**:
- **MUST compile code after creating each file**: `./mvnw test-compile -pl :module-name`
- **MUST fix compilation errors immediately** before proceeding to next file
- **Prevents cascading errors** across multiple files
- **Ensures fresh context** for error fixes (not stale context)

**Pattern** (Incremental Validation):
```bash
# After creating/editing each source file
./mvnw test-compile -pl :module-name

# If compilation fails:
#   1. Fix the error immediately
#   2. Retry compilation
#   3. Proceed only when clean

# After all files created
./mvnw clean verify -pl :module-name
```

**Example** (Engineer Agent):
```bash
# Create FormattingViolation.java
Write: FormattingViolation.java

# Compile immediately
./mvnw test-compile -pl :styler-formatter
# → Detects wrong requireThat() import immediately
# → Fix now with fresh context

# Create FileMetadata.java
Write: FileMetadata.java

# Compile immediately
./mvnw test-compile -pl :styler-formatter
# → Catches any new errors early
```

### ⚠️ CRITICAL: Commit Partial Progress {#commit-partial-progress}

**ALWAYS commit your work, even if tests fail.** Partial progress is better than no progress.

**Why This Matters**:
- Agent work is NOT persisted unless committed
- If you don't commit, ALL your work is lost when the session ends
- Main agent cannot merge or review uncommitted changes
- Re-invocation starts from scratch without your progress

**Commit Pattern**:
```bash
# Even with test failures, COMMIT your work
git add -A
git commit -m "[WIP] description of work done

Tests: X passing, Y failing
Remaining: list of known issues"

# Main agent can then:
# - Merge partial progress
# - Re-invoke you to fix specific issues
# - Have another agent continue from your progress
```

**Example** (from real session):
```
❌ WRONG: Agent completed implementation but didn't commit due to 29 test failures
   Result: ALL work lost, had to start over

✅ CORRECT: Agent commits with test failures
   Result: Progress preserved, targeted fix invocation reduced failures from 29 → 12 → 0
```

**When to Commit**:
- After creating skeleton classes (even empty)
- After implementing core logic (even if tests fail)
- Before reporting any error or blocker
- At regular intervals during long implementations

### Round Completion Criteria {#round-completion-criteria}

Main agent checks these conditions before transitioning to next state:

```
- [ ] All agents in implementation mode have merged their changes to task branch
- [ ] All agents in review mode have reviewed merged changes on task branch
- [ ] All agents in review mode report decision=APPROVED (no REJECTED)
- [ ] All agents have status=COMPLETE
- [ ] Task branch passes ./mvnw verify
```

## Implementation Mode Design Decision Protocol {#implementation-mode-design-decision-protocol}

When agents in implementation mode encounter unexpected design problems not specified in the implementation plan, they MUST re-invoke themselves in review mode (model: opus) or invoke other agents in review mode rather than making architectural decisions themselves in implementation mode.

### Role Separation Principle {#role-separation-principle}

**Review Mode (model: opus)**: Make architectural decisions, analyze requirements, create plans, resolve ambiguities

**Implementation Mode (model: haiku)**: Execute mechanical implementation per plan, apply fixes from review mode, report blockers, DO NOT make architectural decisions independently

### When to Consult Reviewers {#when-to-consult-reviewers}

Agents in implementation mode MUST report to reviewers when encountering:

1. **Unexpected Design Problems**: Planned approach not viable, multiple valid approaches exist, design pattern choice needed, ambiguous contracts
2. **Scope Ambiguities**: Unspecified edge case handling, unclear component ownership, conflicting requirements
3. **Technical Blockers**: Missing dependencies, platform limitations, performance constraints

### Consultation Process {#consultation-process}

```
1. Agent (implementation mode) detects unexpected design problem
2. Agent (implementation mode) updates status.json:
   {
     "status": "BLOCKED",
     "blocked_by": "design_decision_required",
     "details": "Specific description of problem and why plan is insufficient"
   }
3. Agent (implementation mode) returns with summary explaining blocker
4. Main agent invokes agent (review mode) with:
   - Description of problem
   - Why current plan doesn't address it
   - Request for design guidance
5. Agent (review mode) provides updated plan section with decision
6. Main agent provides updated plan to agent (implementation mode)
7. Agent (implementation mode) implements per updated plan
```

**Example**:

❌ **INCORRECT** (Updater makes architectural decision):
```
Problem: Plan says "validate inputs" but doesn't specify validation framework
Updater Action: Independently chooses to use Bean Validation annotations
Result: Violates separation of concerns, may contradict project patterns
```

✅ **CORRECT** (Updater consults reviewer):
```
Problem: Plan says "validate inputs" but doesn't specify validation framework
Updater Action:
  - Updates status.json with BLOCKED status
  - Returns: "Blocked: Plan requires input validation but doesn't specify framework.
             Project uses both requirements-java and Bean Validation. Need reviewer
             guidance on which to use for consistency."
Main Agent: Invokes architect with problem description
Reviewer: Analyzes project patterns, specifies "Use requirements-java requireThat()
          for consistency with existing security module"
Updater: Implements using requirements-java per reviewer guidance
```

## Git Workflow for Agents {#git-workflow-for-agents}

### Commit Signature Requirements (MANDATORY) {#commit-signature-requirements-mandatory}

All agent commits MUST include agent type signature in commit message prefix for post-completion audit verification.

**Format**:

```
[{agent-type}] Commit subject line

Detailed commit body (optional).

Co-Authored-By: {agent-type} <noreply@anthropic.com>
```

**Examples**:

```bash
# ✅ CORRECT - Architecture updater commit
[architect] Implement FormattingRule and TransformationContext interfaces

Created core API interfaces with proper contract definitions.
Validated with SecurityConfig integration.

Co-Authored-By: architect <noreply@anthropic.com>

# ✅ CORRECT - Quality updater commit
[quality] Add comprehensive test suite for FormattingViolation

Implemented 11 unit tests covering validation, immutability, equals/hashCode.
All tests passing with 100% coverage.

Co-Authored-By: quality <noreply@anthropic.com>

# ❌ VIOLATION - Missing agent signature
Implement FormattingRule interface

# ❌ VIOLATION - Generic signature not specific to agent
[feat] Add new feature
```

### For Stakeholder Agents in IMPLEMENTATION Mode: Merging to Task Branch {#for-implementation-agents-merging-to-task-branch}

**Step 1: Verify your worktree**

```bash
# Ensure you're in YOUR worktree
pwd
# Expected: /workspace/tasks/{task-name}/agents/{agent-name}/code

# Verify git status clean
git status
```

**Step 2: Validate locally before merging**

```bash
# Run full validation in YOUR worktree
./mvnw verify

# If validation fails, fix issues before merging
# Never merge failing code to task branch
```

**Step 3: Merge to task branch**

```bash
# Change to task branch worktree
cd /workspace/tasks/{TASK}/code

# Merge changes from your worktree
# CRITICAL: Include [{AGENT}-updater] signature prefix for audit compliance
git merge --no-ff /workspace/tasks/{TASK}/agents/{AGENT}-updater/code -m "[{AGENT}-updater] {description}

Implemented in round ${ROUND}.

Co-Authored-By: {AGENT}-updater <noreply@anthropic.com>"

# Verify merge succeeded
git status
```

**Step 4: Update status.json**

```bash
# Record merge SHA
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)

# Update your status file
cat > /workspace/tasks/{TASK}/agents/{AGENT}-updater/status.json <<EOF
{
  "agent": "{AGENT}-updater",
  "task": "{TASK}",
  "status": "COMPLETE",
  "work_remaining": "none",
  "round": ${ROUND},
  "last_merge_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

### For Stakeholder Agents in VALIDATION Mode: Reviewing Task Branch {#for-validation-agents-reviewing-task-branch}

**Step 1: Review the task branch (NOT implementation agent worktrees)**

```bash
# Change to task branch worktree
cd /workspace/tasks/{TASK}/code

# Review merged code here
# DO NOT review individual updater worktrees
```

**Step 2: Analyze changes**

```bash
# See what was merged since last review
git log --oneline -10

# Review specific files
Read /workspace/tasks/{TASK}/code/src/main/java/...

# Run analysis tools if applicable
./mvnw checkstyle:check pmd:check  # For style/quality reviewers
./mvnw test                         # For test reviewers
```

**Step 3: Make decision (APPROVED or REJECTED)**

```bash
# If changes meet requirements
DECISION="APPROVED"
FEEDBACK=""

# If changes need fixes
DECISION="REJECTED"
FEEDBACK="Detailed issues:\n1. Interface contracts ambiguous\n2. Missing validation"
```

**Step 4: Update status.json with decision**

```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)

cat > /workspace/tasks/{TASK}/agents/{AGENT}-reviewer/status.json <<EOF
{
  "agent": "{AGENT}-reviewer",
  "task": "{TASK}",
  "status": "COMPLETE",
  "decision": "${DECISION}",
  "work_remaining": "$([ "$DECISION" = "APPROVED" ] && echo "none" || echo "updater must address feedback")",
  "round": ${ROUND},
  "last_review_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "feedback": "${FEEDBACK}"
}
EOF
```

## Validation Commands {#validation-commands}

### For Stakeholder Agents in IMPLEMENTATION Mode {#for-implementation-agents-validation}

**Local validation in YOUR worktree** (before merging):

```bash
cd /workspace/tasks/{task-name}/agents/{agent-name}/code

# Full validation
./mvnw verify

# Incremental validation (faster during development)
./mvnw compile                 # After structural changes
./mvnw test                    # After logic changes
./mvnw checkstyle:check        # After style changes
```

**Validation of task branch** (after merging):

```bash
cd /workspace/tasks/{task-name}/code

# Verify merge didn't break anything
./mvnw verify
```

### For Stakeholder Agents in VALIDATION Mode {#for-validation-agents}

**Review task branch** (where implementation agents have merged):

```bash
cd /workspace/tasks/{task-name}/code

# Run domain-specific validation
./mvnw verify                          # All reviewers
./mvnw checkstyle:check pmd:check      # Style/quality reviewers
./mvnw test                            # Test reviewer
./mvnw compile                         # Architecture/build reviewers
```

**Report Format Best Practices**:

Reviewer reports should be **concise and structured** to minimize token usage:

✅ **RECOMMENDED** (Concise):
```markdown
## Missing Components
- File: `FormattingRule.java`
- Add: `priority()` method returning `int`
- Rationale: Support rule ordering
- Reference: See style-guide.md § Rule Priority
```

❌ **AVOID** (Verbose):
```markdown
## Missing Components

The FormattingRule interface needs a priority method. Here's the complete interface:

\```java
// Full 40-line interface definition with all existing methods...
public interface FormattingRule {
  // ... extensive code block ...
}
\```
```

**Guidelines**:
- Use file references instead of full code blocks
- Include code snippets ONLY for highlighting specific issues (max 5-10 lines)
- Prefer structured lists over prose explanations

## File Locations {#file-locations}

### Input Files (Where to Read) {#input-files-where-to-read}

**Reviewer reports** (generated by agents in review mode, consumed by agents in implementation mode):

```
/workspace/tasks/{task-name}/{domain}-reviewer-report.json
/workspace/tasks/{task-name}/{domain}-reviewer-report.md

Examples:
/workspace/tasks/add-api/architect-requirements.md
/workspace/tasks/add-api/style-violations.json
/workspace/tasks/add-api/quality-refactoring.json
```

**Task requirements** (generated by main agent):

```
/workspace/tasks/{task-name}/task.md
```

**Project documentation**:

```
/workspace/main/docs/project/          # Protocol and architecture docs
/workspace/main/docs/code-style/       # Style guides
/workspace/main/docs/                  # General documentation
```

### Output Files (Where to Write) {#output-files-where-to-write}

**Status tracking** (MANDATORY):

```
/workspace/tasks/{task-name}/agents/{agent-name}/status.json
```

**Reviewer reports** (for updater consumption):

```
/workspace/tasks/{task-name}/{agent-name}-report.json
/workspace/tasks/{task-name}/{agent-name}-report.md
```

**Temporary files** (use temporary directory):

```bash
# Get temporary directory (set up by task protocol)
TEMP_DIR=$(cat .temp_dir 2>/dev/null || echo "/tmp/agent-$$")

# Use for temporary artifacts
"$TEMP_DIR/analysis-results.json"
"$TEMP_DIR/test-data.txt"
```

**NEVER write temporary files to**:
- ❌ Git repository directories
- ❌ Project source directories
- ❌ Main worktree

## Common Patterns {#common-patterns}

### Pattern: Incremental Validation {#pattern-incremental-validation}

Full `./mvnw verify` is slow (30-60 seconds). Validate incrementally during development:

```bash
# After creating new class
./mvnw compile

# After modifying logic
./mvnw test -Dtest=YourTestClass

# After style fixes
./mvnw checkstyle:check pmd:check

# Final validation before merge
./mvnw verify
```

**Build Optimization**: Use incremental builds to save time:

✅ **RECOMMENDED** (Incremental - ~15 seconds):
```bash
./mvnw verify   # Uses cached compilation for unchanged files
```

❌ **AVOID** (Full rebuild - ~45 seconds):
```bash
./mvnw clean verify   # Recompiles everything unnecessarily
```

**When to use `clean`**:
- Previous build had compilation errors
- Dependency changes (pom.xml modified)
- Build state corruption suspected
- Fresh verification needed after major changes

**Default**: Use `./mvnw verify` for normal validation.

### Pattern: Reading Reviewer Feedback {#pattern-reading-reviewer-feedback}

**Agents in implementation mode** must read reviewer feedback after REJECTED decision:

```bash
# Check reviewer decision
DECISION=$(jq -r '.decision' /workspace/tasks/{TASK}/agents/{REVIEWER}/status.json)

if [ "$DECISION" = "REJECTED" ]; then
    # Read feedback
    FEEDBACK=$(jq -r '.feedback' /workspace/tasks/{TASK}/agents/{REVIEWER}/status.json)

    echo "Reviewer rejected changes:"
    echo "$FEEDBACK"

    # Address each issue in feedback
    # Fix issues in your worktree
    # Re-merge to task branch
    # Reviewer will re-review
fi
```

### Pattern: Error Handling {#pattern-error-handling}

**If implementation cannot proceed**:

```bash
# Update status as BLOCKED
cat > /workspace/tasks/{TASK}/agents/{AGENT}/status.json <<EOF
{
  "agent": "{AGENT}",
  "task": "{TASK}",
  "status": "BLOCKED",
  "work_remaining": "Cannot proceed: missing dependency",
  "blocked_by": "Requires FormatterApi interface from architect",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

### Pattern: Multi-Round Collaboration {#pattern-multi-round-collaboration}

Architecture change affects multiple agents:

```
Round 1:
- architect: Create new FormatterApi interface
- architect: Merge to task branch
- quality: BLOCKED (waiting for FormatterApi)
- style: BLOCKED (waiting for FormatterApi)

Round 2 (after architect completes):
- quality: Implement FormatterApiImpl using new interface
- style: Apply style rules to FormatterApi
- Both merge to task branch

Round 3 (review):
- architect: Review FormatterApi → APPROVED
- quality: Review FormatterApiImpl → APPROVED
- style: Review style → APPROVED
```

## Summary: Agent Checklist {#summary-agent-checklist}

### Stakeholder Agent Checklist (IMPLEMENTATION Mode) {#implementation-agent-checklist}

Before starting work:
- [ ] Read task-protocol-agents.md (this document)
- [ ] Read requirements/specifications from REQUIREMENTS phase
- [ ] Verify worktree location: `/workspace/tasks/{task}/agents/{agent}/code`

During implementation:
- [ ] Implement in YOUR worktree
- [ ] Validate incrementally during development
- [ ] **MANDATORY**: Run clean build before completion: `cd /workspace/tasks/{task}/agents/{agent}/code && ../../../../../../main/mvnw clean verify`
- [ ] Verify exit code 0 (build passes)

When merging:
- [ ] Ensure clean build passes in YOUR worktree (see above)
- [ ] Merge to task branch at `/workspace/tasks/{task}/code`
- [ ] Update status.json with COMPLETE and last_merge_sha
- [ ] Verify task branch still passes validation after merge

If reviewer rejects:
- [ ] Read feedback from reviewer's status.json
- [ ] Fix issues in YOUR worktree
- [ ] Re-merge to task branch
- [ ] Update status.json

### Stakeholder Agent Checklist (VALIDATION Mode) {#validation-agent-checklist}

Before starting work:
- [ ] Read task-protocol-agents.md (this document)
- [ ] Verify review location: `/workspace/tasks/{task}/code` (task branch)

During review:
- [ ] Review task branch (NOT individual implementation agent worktrees)
- [ ] Analyze merged changes
- [ ] Run domain-specific validation tools
- [ ] Make decision: APPROVED or REJECTED

When complete:
- [ ] Update status.json with decision
- [ ] If REJECTED: Provide detailed feedback
- [ ] If APPROVED: Set work_remaining to "none"

## References {#references}

- **Main protocol**: [task-protocol-core.md](task-protocol-core.md) - Complete state machine (for main agent)
- **Operations guide**: [task-protocol-operations.md](task-protocol-operations.md) - Patterns and examples (for main agent)
- **Architecture**: [architecture.md](architecture.md) - Project structure and design
- **Build system**: [build-system.md](build-system.md) - Maven commands and configuration
