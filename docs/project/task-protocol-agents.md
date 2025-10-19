# Task Protocol for Sub-Agents

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** All sub-agents (reviewers and updaters)
> **Purpose:** Agent coordination protocol, worktree structure, and status tracking

This document contains the **essential task protocol information** that all sub-agents must understand to coordinate properly with the main agent and other sub-agents during task execution.

## 🚨 MANDATORY: Read This Document at Startup

**ALL sub-agents MUST read this document BEFORE performing any work.** This ensures proper coordination, worktree usage, status tracking, and iterative collaboration.

## Worktree Structure

### Directory Layout

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
            ├── architecture-reviewer/
            │   └── status.json     # Reviewer status tracking
            ├── architecture-updater/
            │   ├── code/           # Updater's isolated worktree
            │   │   ├── src/        # Implementation work
            │   │   └── pom.xml
            │   └── status.json     # Updater status tracking
            ├── quality-reviewer/
            │   └── status.json
            └── quality-updater/
                ├── code/
                └── status.json
```

### Critical Path Distinctions

**Updater Agents**:
- **YOUR worktree**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
- **Work here**: Implement changes in isolation
- **Validate here**: Run `./mvnw verify` in YOUR worktree
- **Merge to**: Task branch at `/workspace/tasks/{task-name}/code/`

**Reviewer Agents**:
- **Review location**: `/workspace/tasks/{task-name}/code/` (task branch, NOT updater worktrees)
- **What to review**: Changes that have been merged to task branch by updater agents
- **DO NOT review**: Individual updater worktrees (those are WIP)

**Main Agent**:
- **Task worktree**: `/workspace/tasks/{task-name}/code/`
- **Monitors**: All agent status.json files in `/workspace/tasks/{task-name}/agents/*/status.json`
- **Coordinates**: Agent invocations, state transitions, final merge to main branch

## Status Tracking Protocol

### Status File Location

**Every agent** (reviewer and updater) MUST maintain a `status.json` file:

```
/workspace/tasks/{task-name}/agents/{agent-name}/status.json
```

### Status File Formats

**For Reviewer Agents**:

```json
{
  "agent": "architecture-reviewer",
  "task": "implement-feature-x",
  "status": "WORKING|COMPLETE|BLOCKED",
  "decision": "APPROVED|REJECTED|PENDING",
  "round": 3,
  "last_review_sha": "abc123def456",
  "work_remaining": "none|description of pending work",
  "feedback": "detailed feedback for updater agent (if REJECTED)",
  "updated_at": "2025-10-15T10:30:00Z"
}
```

**Required fields for reviewers**:
- `decision`: **CRITICAL** - Main agent checks this field to determine if work is complete
  - `APPROVED`: Changes meet requirements, round complete
  - `REJECTED`: Changes need fixes, provide feedback
  - `PENDING`: Review in progress
- `feedback`: Detailed feedback for updater agent when decision is REJECTED

**For Updater Agents**:

```json
{
  "agent": "architecture-updater",
  "task": "implement-feature-x",
  "status": "WORKING|COMPLETE|BLOCKED",
  "round": 3,
  "last_merge_sha": "abc123def456",
  "work_remaining": "none|description of pending work",
  "blocked_by": null,
  "updated_at": "2025-10-15T10:30:00Z"
}
```

**Required fields for updaters**:
- `last_merge_sha`: Git SHA of last merge to task branch
- `work_remaining`: "none" when implementation complete

## Agent Response Verbosity Guidelines

**MANDATORY**: Return metadata-only status reports, NOT full code listings.

**✅ CORRECT (50 tokens)**:
```
Implementation complete.
- Files created: FormattingRule.java, TransformationContext.java, FormattingViolation.java
- Commit SHA: abc1234
- Validation: PASSED (22/22 tests, 0 violations)
```

**❌ VIOLATION (5,000+ tokens)**:
```
Implementation complete. Here is the code:

[Full code listing of 3 files × 200 lines each = 600 lines]
```

**Token Impact**: Returning full code for 4 agents × 2 rounds = 40,000-60,000 wasted tokens.

### Status Update Commands

**Updater agent after merging**:

```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}-updater/status.json <<EOF
{
  "agent": "{AGENT}-updater",
  "task": "{TASK}",
  "status": "COMPLETE",
  "work_remaining": "none",
  "round": ${CURRENT_ROUND},
  "last_merge_sha": "${TASK_SHA}",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

**Reviewer agent after reviewing**:

```bash
TASK_SHA=$(git -C /workspace/tasks/{TASK}/code rev-parse HEAD)
cat > /workspace/tasks/{TASK}/agents/{AGENT}-reviewer/status.json <<EOF
{
  "agent": "{AGENT}-reviewer",
  "task": "{TASK}",
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

## Iterative Workflow Pattern

### Single Agent Round Pattern

```
1. Updater agent implements in their worktree (/workspace/tasks/{task}/agents/{agent}-updater/code)
2. Updater agent validates locally: ./mvnw verify (in their worktree)
3. Updater agent merges to task branch (/workspace/tasks/{task}/code)
4. Updater agent updates status.json with COMPLETE
5. Reviewer agent reviews what was merged to task branch
6. Reviewer agent decides: APPROVED or REJECTED
   - If APPROVED: Round complete, update status.json with decision=APPROVED
   - If REJECTED: Update status.json with decision=REJECTED and feedback
7. If REJECTED: Updater agent reads feedback → fixes issues → merge → repeat from step 5
8. Round complete when reviewer decision=APPROVED
```

### Multi-Agent Round Flow Example

```
Round 1 - Initial Implementation:
├─ architecture-updater: Implement core interfaces → merge to task branch
├─ quality-updater: Apply refactoring → merge to task branch
├─ style-updater: Apply style rules → merge to task branch
├─ test-updater: Implement test suite → merge to task branch
└─ All updaters: Update status.json with COMPLETE

Round 1 - Review Merged Changes:
├─ architecture-reviewer: Review task branch → REJECTED (ambiguous contracts)
├─ quality-reviewer: Review task branch → APPROVED ✓
├─ style-reviewer: Review task branch → REJECTED (12 violations)
├─ test-reviewer: Review task branch → APPROVED ✓
└─ Reviewers: Update status.json with decision

Round 2 - Apply Reviewer Feedback:
├─ architecture-updater: Fix contracts per feedback → merge
├─ style-updater: Fix 12 violations per feedback → merge
└─ Updaters: Update status.json

Round 2 - Re-review Fixes:
├─ architecture-reviewer: Re-review → APPROVED ✓
├─ style-reviewer: Re-review → APPROVED ✓
└─ All reviewers now APPROVED: Round complete

Main agent checks all reviewer status.json files:
- All have decision=APPROVED → Transition to VALIDATION state
```

### Round Completion Criteria

Main agent checks these conditions before transitioning to next state:

```
- [ ] All updater agents have merged their changes to task branch
- [ ] All reviewer agents have reviewed merged changes on task branch
- [ ] All reviewer agents report decision=APPROVED (no REJECTED)
- [ ] All agents have status=COMPLETE
- [ ] Task branch passes ./mvnw verify
```

## Git Workflow for Agents

### For Updater Agents: Merging to Task Branch

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
git merge --no-ff /workspace/tasks/{TASK}/agents/{AGENT}-updater/code -m "feat: {description}

Implemented by {AGENT}-updater in round ${ROUND}.

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

### For Reviewer Agents: Reviewing Task Branch

**Step 1: Review the task branch (NOT updater worktrees)**

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

## Validation Commands

### For Updater Agents

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

### For Reviewer Agents

**Review task branch** (where updaters have merged):

```bash
cd /workspace/tasks/{task-name}/code

# Run domain-specific validation
./mvnw verify                          # All reviewers
./mvnw checkstyle:check pmd:check      # Style/quality reviewers
./mvnw test                            # Test reviewer
./mvnw compile                         # Architecture/build reviewers
```

## File Locations

### Input Files (Where to Read)

**Reviewer reports** (generated by reviewer agents, consumed by updater agents):

```
/workspace/tasks/{task-name}/{domain}-reviewer-report.json
/workspace/tasks/{task-name}/{domain}-reviewer-report.md

Examples:
/workspace/tasks/add-api/architecture-reviewer-requirements.md
/workspace/tasks/add-api/style-reviewer-violations.json
/workspace/tasks/add-api/quality-reviewer-refactoring.json
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

### Output Files (Where to Write)

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

## Common Patterns

### Pattern: Incremental Validation

**Problem**: Full `./mvnw verify` is slow (30-60 seconds)

**Solution**: Validate incrementally during development

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

### Pattern: Reading Reviewer Feedback

**Updater agents** must read reviewer feedback after REJECTED decision:

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

### Pattern: Error Handling

**If implementation cannot proceed**:

```bash
# Update status as BLOCKED
cat > /workspace/tasks/{TASK}/agents/{AGENT}/status.json <<EOF
{
  "agent": "{AGENT}",
  "task": "{TASK}",
  "status": "BLOCKED",
  "work_remaining": "Cannot proceed: missing dependency",
  "blocked_by": "Requires FormatterApi interface from architecture-updater",
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

**Main agent will detect BLOCKED status and resolve dependency.**

### Pattern: Multi-Round Collaboration

**Scenario**: Architecture change affects multiple agents

```
Round 1:
- architecture-updater: Create new FormatterApi interface
- architecture-updater: Merge to task branch
- quality-updater: BLOCKED (waiting for FormatterApi)
- style-updater: BLOCKED (waiting for FormatterApi)

Round 2 (after architecture-updater completes):
- quality-updater: Implement FormatterApiImpl using new interface
- style-updater: Apply style rules to FormatterApi
- Both merge to task branch

Round 3 (review):
- architecture-reviewer: Review FormatterApi → APPROVED
- quality-reviewer: Review FormatterApiImpl → APPROVED
- style-reviewer: Review style → APPROVED
```

## Summary: Agent Checklist

### Updater Agent Checklist

Before starting work:
- [ ] Read task-protocol-agents.md (this document)
- [ ] Read reviewer report/specifications
- [ ] Verify worktree location: `/workspace/tasks/{task}/agents/{agent}/code`

During implementation:
- [ ] Implement in YOUR worktree
- [ ] Validate incrementally during development
- [ ] Run `./mvnw verify` before merging

When merging:
- [ ] Ensure validation passes in YOUR worktree
- [ ] Merge to task branch at `/workspace/tasks/{task}/code`
- [ ] Update status.json with COMPLETE and last_merge_sha
- [ ] Verify task branch still passes validation after merge

If reviewer rejects:
- [ ] Read feedback from reviewer's status.json
- [ ] Fix issues in YOUR worktree
- [ ] Re-merge to task branch
- [ ] Update status.json

### Reviewer Agent Checklist

Before starting work:
- [ ] Read task-protocol-agents.md (this document)
- [ ] Verify review location: `/workspace/tasks/{task}/code` (task branch)

During review:
- [ ] Review task branch (NOT individual updater worktrees)
- [ ] Analyze merged changes
- [ ] Run domain-specific validation tools
- [ ] Make decision: APPROVED or REJECTED

When complete:
- [ ] Update status.json with decision
- [ ] If REJECTED: Provide detailed feedback
- [ ] If APPROVED: Set work_remaining to "none"

## References

- **Main protocol**: [task-protocol-core.md](task-protocol-core.md) - Complete state machine (for main agent)
- **Operations guide**: [task-protocol-operations.md](task-protocol-operations.md) - Patterns and examples (for main agent)
- **Architecture**: [architecture.md](architecture.md) - Project structure and design
- **Build system**: [build-system.md](build-system.md) - Maven commands and configuration
