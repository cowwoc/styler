---
name: archive-task
description: Atomically update todo.md and changelog.md when completing a task
allowed-tools: Bash, Read, Edit, Write
---

# Archive Task Skill

**Purpose**: Atomically update todo.md (mark task complete) and changelog.md (add changes) in a single commit when task is complete.

**Performance**: Prevents split commits, ensures atomic task archival

## When to Use This Skill

### ✅ Use archive-task When:

- User has approved changes (AWAITING_USER_APPROVAL state)
- Ready to mark task complete in todo.md
- Need to add entry to changelog.md
- Want atomic commit of both updates
- **BEFORE merging to main** (archival must be part of task branch commit)

### ❌ Do NOT Use When:

- User has not yet approved changes
- Still in IMPLEMENTATION or VALIDATION state
- Todo.md already updated for this task
- Working on multiple tasks simultaneously

## What This Skill Does

### 1. Validates Task Complete

```bash
# Checks:
- Task in COMPLETE state (task.json)
- Task merged to main (git log check)
- User approval flag exists
```

### 2. Updates todo.md

```bash
# REMOVES task entry completely (do NOT mark with [x]):
BEFORE: - [ ] implement-formatter-api
        - **Dependencies**: None
        - **Blocks**: None
        ...
AFTER:  (entire entry deleted)
```

**CRITICAL**: Tasks are REMOVED from todo.md, NOT marked with `[x]`. The `[x]` pattern is a common mistake
that violates the archival protocol.

### 3. Updates changelog.md

```bash
# Adds entry with changes:
## [YYYY-MM-DD] - implement-formatter-api

- Added FormattingRule interface
- Implemented RuleEngine class
- Added comprehensive tests
- Documented public API
```

### 4. Commits Atomically

```bash
# Single commit with both files:
git add todo.md changelog.md
git commit -m "Archive task: implement-formatter-api

Removed task entry from todo.md.
Added changelog entry:
- Added FormattingRule interface
- Implemented RuleEngine class
- Added comprehensive tests
- Documented public API

"
```

## Usage

### Basic Task Archival

```bash
# After task merged to main
TASK_NAME="implement-formatter-api"
CHANGES="Added FormattingRule interface
Implemented RuleEngine class
Added comprehensive tests
Documented public API"

/workspace/main/.claude/scripts/archive-task.sh \
  --task "$TASK_NAME" \
  --changes "$CHANGES"
```

### Extract Changes from Git

```bash
# Automatically extract changes from merge commit
TASK_NAME="implement-formatter-api"

/workspace/main/.claude/scripts/archive-task.sh \
  --task "$TASK_NAME" \
  --auto-extract-changes true
```

## Atomic Commit Pattern

### ✅ CORRECT Pattern (Atomic)

```bash
# Single commit updates both files
Edit: todo.md (mark task complete)
Edit: changelog.md (add entry)
git add todo.md changelog.md
git commit -m "Update todo.md: Mark task complete

Added changelog entry: ...

"
```

**Result**: One commit, both files updated together

### ❌ WRONG Pattern (Split Commits)

```bash
# First commit: todo.md only
Edit: todo.md
git add todo.md
git commit -m "Mark task complete"

# Second commit: changelog.md
Edit: changelog.md
git add changelog.md
git commit -m "Update changelog"
```

**Problem**: Two commits instead of atomic update (protocol violation)

## Changelog Entry Format

### Standard Format

```markdown
## [YYYY-MM-DD] - {task-name}

- Change description line 1
- Change description line 2
- Change description line 3
```

### With Categories

```markdown
## [YYYY-MM-DD] - {task-name}

### Added
- New feature X
- New API Y

### Changed
- Improved performance of Z

### Fixed
- Bug in edge case handling
```

### With References

```markdown
## [YYYY-MM-DD] - {task-name}

- Implemented FormattingRule interface (architect)
- Added comprehensive test suite (tester)
- Documented public API with JavaDoc (formatter)

Commit: abc123
```

## Workflow Integration

### Complete Task Archival Workflow

```markdown
User approves changes (AWAITING_USER_APPROVAL)
  ↓
[archive-task skill] ← THIS SKILL (BEFORE merge)
  ↓
Update todo.md (mark complete)
  ↓
Update changelog.md (add entry)
  ↓
Amend task branch commit to include archival
  ↓
Merge to main with --ff-only
  ↓
[update-dependent-tasks skill: Unblock dependent tasks] ← MANDATORY
  ↓
[task-cleanup skill: Remove branches/worktrees]
```

⚠️ **CRITICAL: After archiving, you MUST invoke update-dependent-tasks skill**

When a task is marked complete, other tasks that depend on it may become unblocked.
Failure to update dependent tasks leaves todo.md in an inconsistent state where
tasks remain marked BLOCKED even though their dependencies are satisfied.

## Output Format

Script returns JSON:

```json
{
  "status": "success",
  "message": "Task archived successfully",
  "task_name": "implement-formatter-api",
  "todo_updated": true,
  "changelog_updated": true,
  "commit_sha": "abc123def456",
  "files_changed": ["todo.md", "changelog.md"],
  "atomic_commit": true,
  "timestamp": "2025-11-11T12:34:56-05:00"
}
```

## Change Extraction Strategies

### Strategy 1: From Merge Commit Message

```bash
# Extract from squash merge commit message
git log --format=%B -n 1 HEAD | grep "^-"
```

### Strategy 2: From Git Diff Stats

```bash
# Summarize file changes
git diff --stat main~1..main
```

### Strategy 3: From Task Requirements

```bash
# Use original task description from todo.md
# and agent requirement reports to summarize
```

### Strategy 4: User Provides

```bash
# User explicitly provides change summary
--changes "Added feature X, Fixed bug Y"
```

## Safety Features

### Precondition Validation

- ✅ Verifies task exists
- ✅ Checks task in COMPLETE state
- ✅ Confirms task merged to main
- ✅ Validates approval flag exists

### Atomic Commit Enforcement

- ✅ Stages both files together
- ✅ Single commit command
- ✅ Aborts if either file fails to update
- ✅ Rollback on commit failure

### Error Handling

On any error:
- Unstages any staged changes
- Does not create partial commit
- Returns JSON with error details
- Leaves repository in clean state

**Recovery**: Safe to retry after fixing issue

## Todo.md Update Patterns

**CRITICAL**: Tasks are REMOVED completely from todo.md, NOT marked with `[x]`.
The `[x]` checkbox pattern is WRONG and violates the archival protocol.

### Simple Entry

```markdown
BEFORE:
- [ ] implement-formatter-api

AFTER:
(entire entry deleted from todo.md)
```

### With Sub-tasks

```markdown
BEFORE:
- [ ] implement-formatter-api
  - Dependencies: None
  - Blocks: Other tasks
  - Estimated Effort: 2 days
  - Purpose: Description here

AFTER:
(entire entry including all sub-items deleted from todo.md)
```

### Common Mistake to AVOID

```markdown
❌ WRONG - Do NOT mark with [x]:
- [x] implement-formatter-api

✅ CORRECT - DELETE the entire entry:
(entry no longer exists in todo.md)
```

## Changelog.md Update Patterns

### Append to Top (Recommended)

```markdown
# Changelog

## [2025-11-11] - implement-formatter-api ← NEW ENTRY
- Added FormattingRule interface
...

## [2025-11-10] - previous-task
...
```

**Rationale**: Latest changes at top, easy to see recent work

## Changelog Archive Management {#changelog-archive-management}

### Size Threshold and Yearly Archives

To keep `changelog.md` readable for both humans and Claude, the file uses a **size threshold with yearly
archive rotation**:

- **Working file**: `changelog.md` (≤500 lines) - Claude reads this
- **Archive files**: `changelog-YYYY.md` (append-only) - Full history by year

### Archive Trigger

When `changelog.md` exceeds **500 lines**, the archive-task script:
1. Keeps recent entries in `changelog.md` (up to 500 lines)
2. Parses older entries' dates from `## YYYY-MM-DD` headers
3. Groups older entries by year
4. **Appends** each year's entries to `changelog-{year}.md` (creates if needed)

**Key**: Recent history stays visible; only older entries are archived by their date.

### Archive File Format

```markdown
# Changelog Archive - 2025

## [2025-12-31] - task-name
- Changes from this task

## [2025-12-30] - earlier-task
- Changes from earlier task
...
```

**Key points**:
- Archive files are **append-only** (new archives prepend to existing content)
- One archive file per year (`changelog-2025.md`, `changelog-2026.md`)
- Within each year, entries remain in reverse chronological order (newest first)
- Users can search any year's archive for historical changes

### File Locations

```
/workspace/main/
├── changelog.md           # Active working file (Claude reads)
├── changelog-2026.md      # 2026 archived entries
├── changelog-2025.md      # 2025 archived entries
└── ...
```

### Manual Archive Trigger

To manually trigger archival (regardless of size):

```bash
/workspace/main/.claude/scripts/archive-task.sh --archive-now
```

## Related Skills

- **update-dependent-tasks**: **MANDATORY** - Follows archive-task (unblocks dependent tasks)
- **task-cleanup**: Follows update-dependent-tasks (cleans branches/worktrees)
- **checkpoint-approval**: Precedes archive-task (gets user approval)
- **git-merge-linear**: Merges task to main before archival

## Troubleshooting

### Error: "Task not in COMPLETE state"

```bash
# Check current state
jq -r '.state' /workspace/tasks/{task-name}/task.json

# If not COMPLETE:
# 1. Ensure validation passed
# 2. Get user approval
# 3. Merge to main
# 4. Transition to COMPLETE
# 5. Retry archival
```

### Error: "Task not merged to main"

```bash
# Check if task merged
git log --oneline --all --grep="{task-name}"

# If not merged:
# 1. Complete VALIDATION phase
# 2. Get user approval
# 3. Squash merge to main
# 4. Retry archival
```

### Error: "Todo.md entry not found"

```bash
# Task name doesn't match todo.md entry
# Options:
1. Verify exact task name in todo.md
2. Check for typos or different casing
3. Manually locate entry and provide line number
4. Retry with correct task name
```

### Changelog Entry Already Exists

```bash
# Task already archived (duplicate)
# Options:
1. Skip changelog update (only update todo.md)
2. Update existing entry if changes incomplete
3. Do nothing if already complete
```

## Common Archival Patterns

### Pattern 1: Standard Task

```bash
# Simple feature implementation
archive-task \
  --task "implement-api" \
  --changes "Added public API for feature X"
```

### Pattern 2: Bug Fix

```bash
# Bug fix task
archive-task \
  --task "fix-edge-case-bug" \
  --changes "Fixed edge case in validation logic
Affected: Parser.validate() method
Impact: Prevents NPE on null input"
```

### Pattern 3: Multi-Component Task

```bash
# Task touching multiple areas
archive-task \
  --task "refactor-validation" \
  --changes "Refactored validation logic:
- Extracted ValidationRule interface
- Implemented DefaultValidator
- Added ValidationContext for error reporting
- Updated 15 call sites"
```

## Implementation Notes

The archive-task script performs:

1. **Validation Phase**
   - Check task exists
   - Verify task in COMPLETE state
   - Confirm merged to main
   - Validate approval flag

2. **Change Extraction Phase**
   - Extract from merge commit (if auto)
   - Parse provided changes
   - Format for changelog
   - Validate non-empty

3. **Todo Update Phase**
   - Read todo.md
   - Locate task entry
   - DELETE entire task entry (NOT mark with [x])
   - Remove task line and all sub-items

4. **Changelog Update Phase**
   - Read changelog.md
   - Format new entry
   - Insert at appropriate location
   - Add date and task name

5. **Commit Phase**
   - Stage both files
   - Create commit with formatted message
   - Include change summary
   - Add co-author footer

6. **Verification Phase**
   - Confirm commit created
   - Verify both files updated
   - Validate atomic commit
   - Return success status
