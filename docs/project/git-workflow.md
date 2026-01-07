# Git Workflow Guidelines

This document outlines git workflows and best practices for the Styler project.

## Commit Message Format {#commit-message-format}

**All commit messages MUST follow the `type: description` format.**

### Commit Types

**feature:** - New feature or functionality
- Adding new classes, methods, or capabilities
- Implementing new user-facing features
- Example: `feature: Add code formatting API`

**bugfix:** - Bug fixes
- Correcting errors in existing code
- Fixing broken functionality
- Example: `bugfix: Resolve NPE in FormattingRule validation`

**docs:** - Documentation changes
- README, JavaDoc, markdown files
- Code comments (when substantial)
- Example: `docs: Add architecture documentation for formatter module`

**refactor:** - Code restructuring without changing behavior
- Improving code structure or organization
- Renaming for clarity
- Extracting methods or classes
- Example: `refactor: Extract validation logic to separate class`

**test:** - Adding or modifying tests
- New test cases
- Test infrastructure changes
- Example: `test: Add integration tests for formatter API`

**config:** - Configuration, maintenance, and code style tasks
- Build configuration updates (pom.xml, module-info.java)
- Dependency updates
- Tool configuration (.checkstyle, PMD rules, .claude/)
- Project configuration (CLAUDE.md, todo.md, changelog.md)
- IDE configuration (.idea/)
- CI/CD pipeline changes
- Code style/formatting changes (whitespace, formatting, applying style rules)
- Example: `config: Update checkstyle to version 10.12.5`
- Example: `config: Apply checkstyle formatting rules`

**perf:** - Performance improvements
- Optimization changes
- Performance-related refactoring
- Example: `perf: Optimize rule matching with indexed lookup`

**revert:** - Reverting previous commits
- Rolling back changes
- Example: `revert: Revert "Add experimental caching feature"`

### Commit Message Structure

```
type: Short description (50 chars or less)

Optional detailed explanation of what and why (not how).
Wrap at 72 characters.

- Bullet points for multiple changes
- Each point describes a discrete change

```

### Examples

```
feature: Implement FormattingRule interface for style checks

- Created FormattingRule base interface
- Added FormattingViolation data class
- Implemented NoFixAvailable singleton pattern
- Added comprehensive JavaDoc
```

```
config: Update Maven build configuration

- Upgraded checkstyle-maven-plugin to 3.3.1
- Added SpotBugs exclusion patterns
- Configured japicmp for API compatibility checks
```

```
bugfix: Handle null values in FormattingConfiguration

Added null checks and validation to prevent NPE when
configuration values are missing. Falls back to defaults.
```

## Branch Reference Resolution {#branch-reference-resolution}

**When user says "rebase on X" or "merge X"**: Use LOCAL branch `X` first, not `origin/X`.

**Resolution Order**:
1. **Local branch exists**: Use `X` (e.g., `git rebase main`)
2. **No local, remote exists**: Use `origin/X` (e.g., `git rebase origin/main`)
3. **Neither exists**: Error - branch not found

**Examples**:
```bash
# User says: "rebase on main"
# ✅ CORRECT - Use local main
git rebase main

# ❌ WRONG - Don't default to origin/main when local exists
git rebase origin/main
```

**Rationale**: Local branches represent the developer's working state. Remote branches may be stale or
different from local. User intent is typically to work with local state unless explicitly requesting remote.

**Exception**: If user explicitly says "origin/main" or "remote main", use the remote reference.

---

## Git Working Directory Requirements {#git-working-directory-requirements}

**MANDATORY RULE**: Working directory MUST be clean (all changes committed) before any state transition.

**Verification Command**:
```bash
git status  # Must show "nothing to commit, working tree clean"
```

**BLOCKED Transitions**:
- Lock file state MUST NOT be updated if `git status` shows uncommitted changes
- State transition hooks SHOULD validate git status before allowing transitions
- Commit all work-in-progress before transitioning states

**See Also**: main-agent-coordination.md § VALIDATION State Exit Requirements

## Push Workflow {#push-workflow}

### Branch Push Policy

**Only the `main` branch should be pushed to remote origin.** Task and agent branches are local workspaces.

| Branch Type | Push to Origin | Rationale |
|-------------|----------------|-----------|
| `main` | ✅ Allowed | Primary development branch |
| `v{N}` (version branches) | ✅ Allowed | Release markers |
| Task branches (e.g., `implement-api`) | ❌ Blocked | Temporary local workspace |
| Agent branches (e.g., `implement-api-architect`) | ❌ Blocked | Agent-specific workspace |

### Enforcement

The hook `.claude/hooks/block-task-branch-push.sh` automatically blocks pushing branches that match task
patterns:
- Prefixes: `implement-`, `add-`, `fix-`, `refactor-`, `update-`, `create-`
- Suffixes: `-architect`, `-engineer`, `-formatter`, `-tester`, `-optimizer`, `-hacker`

### Correct Workflow

```bash
# 1. Complete work on task branch (LOCAL)
cd /workspace/tasks/implement-api/code
# ... make changes, commit locally ...

# 2. Squash commits into single commit
git rebase -i main

# 3. Merge to main with --ff-only
cd /workspace/main
git merge --ff-only implement-api

# 4. Push main to origin
git push origin main

# 5. Cleanup: Delete local task branches
git branch -D implement-api implement-api-architect implement-api-formatter
```

### Bypass (When Necessary)

If you explicitly need to push a task branch (e.g., for backup or collaboration):

```bash
# Explicitly bypass the check
git push origin implement-api --force
```

**Note**: This should be rare. Task branches are designed as temporary local workspaces.

## Commit Squashing Procedures {#commit-squashing-procedures}

### Overview {#overview}

When you need to combine (squash) commits in your git history, there are two main approaches:

1. **Interactive Rebase** (PRIMARY METHOD) - Reliable, efficient, and handles complex scenarios
2. **Cherry-pick Method** - Advanced manual approach for exceptional cases only

**ALWAYS use Interactive Rebase unless specifically required otherwise.** Both workflows support squashing
non-adjacent commits while preserving intermediate commits, useful for consolidating related functionality
without losing unrelated work.

### Task Branch Squashing (Task Protocol) {#task-branch-squashing-task-protocol}

> ⚠️ **CRITICAL VIOLATION PATTERN**: Merging task branch with multiple commits
>
> **REQUIREMENT**: Task branches MUST have exactly 1 squashed commit before merge
> **ENFORCEMENT**: Pre-merge hook `.claude/hooks/enforce-commit-squashing.sh` validates commit count
> **CORRECT**: `git rev-list --count main..<task-branch>` returns 1
> **INCORRECT**: Merging with 2+ commits creates unclear history and violates protocol

**MANDATORY REQUIREMENT**: Before merging a task branch to main, ALL commits on the task branch MUST be squashed into a single commit.

**Procedure** (from task worktree, before merge to main):

```bash
# Step 1: Ensure you're in the task worktree AND on the correct branch
cd /workspace/tasks/{task-name}/code

# ⚠️ CRITICAL: Verify you're on the task branch, NOT an agent branch
CURRENT_BRANCH=$(git branch --show-current)
if [[ "$CURRENT_BRANCH" != "{task-name}" ]]; then
  echo "❌ ERROR: On branch '$CURRENT_BRANCH', expected '{task-name}'"
  echo "   Common mistake: In agent worktree instead of task worktree"
  echo "   Fix: cd /workspace/tasks/{task-name}/code (not agents/{agent}/code)"
  exit 1
fi
echo "✅ On correct branch: $CURRENT_BRANCH"

# Step 2: Count commits on task branch
COMMIT_COUNT=$(git rev-list --count main..{task-name})
echo "Task branch has $COMMIT_COUNT commits"

# Step 3: If more than 1 commit, squash all into 1
if [ "$COMMIT_COUNT" -gt 1 ]; then
  # Interactive rebase to squash all commits
  git rebase -i main

  # In the interactive editor that opens:
  # - Line 1: Keep as "pick" (this will be the final commit)
  # - Lines 2-N: Change ALL to "squash" (or just "s")
  # - Save and close editor
  #
  # Git will then open another editor for the combined commit message:
  # - Edit to create a single comprehensive message describing the entire task
  # - Remove redundant individual commit messages
  # - Keep the task summary and key changes
  # - Save and close editor
fi

# Step 4: Verify exactly 1 commit remains
FINAL_COUNT=$(git rev-list --count main..{task-name})
if [ "$FINAL_COUNT" -ne 1 ]; then
  echo "❌ VIOLATION: Task branch must have exactly 1 commit, found $FINAL_COUNT"
  exit 1
fi

echo "✅ Task branch ready for merge: 1 commit"
```

**Example Interactive Rebase Editor**:

```
# Before (4 commits to squash):
pick a1b2c3d Initial implementation of formatter API
pick e4f5g6h Fix checkstyle violations in FormattingConfiguration
pick i7j8k9l Add missing JavaDoc to public methods
pick m0n1o2p Fix PMD violations in TransformationContext

# Change to (squash all into first):
pick a1b2c3d Initial implementation of formatter API
squash e4f5g6h Fix checkstyle violations in FormattingConfiguration
squash i7j8k9l Add missing JavaDoc to public methods
squash m0n1o2p Fix PMD violations in TransformationContext
```

**Combined Commit Message Template**:

```
feature: Implement formatter API for code transformation

- Created core interfaces: FormattingConfiguration, TransformationContext
- Implemented Fix abstraction with NoFixAvailable singleton
- Added comprehensive test coverage
- All checkstyle, PMD, and SpotBugs validations passing
```

**Enforcement**: Pre-merge hook will block `git merge --ff-only {task-name}` if task branch contains more than 1 commit.

**See Also**: task-protocol-core.md § MANDATORY PRE-MERGE VERIFICATION for complete merge procedure.

### Squash Enforcement Workflow {#squash-enforcement-workflow}

**Pre-Merge Validation** (automated):

```bash
# Validation hook checks commit count before merge (automatic via PreToolUse)
# Hook: .claude/hooks/enforce-commit-squashing.sh

# If validation fails (>1 commit), squash required:
cd /workspace/main
git checkout <task-branch>
git reset --soft main
git commit -m "Combined commit message describing all changes"
```

**Manual Verification**:

```bash
# Count commits ahead of main
git rev-list --count main..<task-branch>
# Expected output: 1

# If output is >1, squash before merge
```

**Post-Squash Merge**:

```bash
# After squashing to 1 commit
git checkout main
git merge <task-branch> --ff-only
# Fast-forward merge preserves linear history
```

**Cleanup After --ff-only Merge** (Task Protocol):

With `--ff-only` merge, no orphaned commits are created (the task branch commit becomes the main branch
commit). Cleanup is simpler:

```bash
# After: git merge --ff-only task-branch
# Result: main pointer moves to task-branch commit (same SHA, no orphans)

# Cleanup steps:
git branch -D task-branch              # Delete branch (safe, commit is on main)
git branch | grep "task-branch-" | xargs -r git branch -D  # Delete agent branches

# No git gc needed! No orphaned commits with --ff-only
```

See: [task-protocol-operations.md § CLEANUP](task-protocol-operations.md#cleanup-final-state) for complete task protocol cleanup procedure.

### Branch Force Update Prohibition {#branch-force-update-prohibition}

**CRITICAL**: Direct manipulation of the `main` branch pointer is PROHIBITED.

**Blocked Operations**:
```bash
# ❌ PROHIBITED - Bypasses merge workflow
git branch -f main HEAD
git branch -f main <commit>
git branch --force main <any-ref>
```

**Why This Is Blocked**:
1. **Bypasses archival verification** - todo.md and changelog.md updates not validated
2. **Circumvents pre-merge hooks** - No enforcement of commit squashing or approval flags
3. **Creates inconsistent state** - Post-merge cleanup hooks never triggered
4. **Breaks audit trail** - Task completion not properly recorded

**Correct Approach**:
```bash
# ✅ CORRECT - Use merge workflow
cd /workspace/main
git merge --ff-only <task-branch>
```

**If You Need to Update Main to Specific Commit**:
1. Create a branch at that commit: `git checkout -b temp-branch <commit>`
2. Merge with validation: `git checkout main && git merge --ff-only temp-branch`
3. This triggers all validation hooks

**Enforcement**: `.claude/hooks/block-branch-force-update.sh` blocks these operations at PreToolUse.

## Interactive Rebase (PRIMARY METHOD) {#interactive-rebase-primary-method}

**RECOMMENDED APPROACH**: Use interactive rebase for all squash operations unless specific circumstances
require the cherry-pick fallback method.

### Interactive Rebase Procedure {#interactive-rebase-procedure}

#### 1. Create Backup Branch {#1-create-backup-branch}

Always create a backup branch before performing any git history operations:

```bash
git checkout -b backup-before-squash-$(date +%Y%m%d-%H%M%S)
git checkout main  # or your working branch
```

#### 2. Create Chronological Rebase Plan {#2-create-chronological-rebase-plan}

**CRITICAL**: Before starting rebase, create and validate the chronological plan to prevent commit reordering.

**Step 2a: Document Original Commit Order**
```bash
# Document the EXACT original chronological order
git log --oneline <base-commit>..HEAD > original-commit-order.txt
cat original-commit-order.txt
```

**Step 2b: Create Preservation Plan**
Identify which commits to preserve in their exact original positions:
```bash
# Example: If squashing commits 098c953, 709c52a, 31efd60, 1311b64, d4ca920, d1edc60
# And preserving 0ed377d, f40ab63, c29c828, d345c52, c2b4a7e (in that order)

echo "SQUASH TARGETS: 098c953, 709c52a, 31efd60, 1311b64, d4ca920, d1edc60" > rebase-plan.txt
echo "PRESERVE ORDER: 0ed377d, f40ab63, c29c828, d345c52, c2b4a7e" >> rebase-plan.txt
echo "PRESERVE AFTER: [list all commits after the newest target]" >> rebase-plan.txt
```

**Step 2c: Validate Chronological Consistency**
```bash
# MANDATORY: Verify no chronological violations in your plan
# Check that preserved commits maintain their relative order
git log --oneline --grep="0ed377d\|f40ab63\|c29c828\|d345c52\|c2b4a7e" <base-commit>..HEAD
```

**Step 2d: Start Interactive Rebase**
```bash
git rebase -i <base-commit>  # e.g., git rebase -i eee773804f
```

**⚠️ AUTOMATED ENVIRONMENT SAFEGUARDS**

Interactive rebase requires manual editor intervention. In automated environments (CLI tools, scripts),
implement these safeguards:

**Safeguard 1: Verify Editor Interaction**
```bash
# Before starting rebase, verify git editor is configured
git config --get core.editor
# If empty or problematic, set explicit editor:
export GIT_EDITOR="nano"  # or vim, emacs, etc.
```

**Safeguard 2: Never Use Destructive Fallbacks**
```bash
# ❌ PROHIBITED: If interactive rebase seems to "fail" (auto-closes, no changes):
# git reset --soft <commit>   # CATASTROPHIC - eliminates timeline segments

# ✅ CORRECT: If interactive rebase doesn't work:
# 1. Abort the rebase safely
git rebase --abort
# 2. Investigate the editor/environment issue
# 3. Fix the root cause, then retry
# 4. If unsolvable, seek manual intervention - NEVER use destructive methods
```

**Safeguard 3: Validate Rebase Completion**
```bash
# After rebase completes, immediately verify all expected commits remain:
git log --oneline -20 > post-rebase-order.txt
# Compare against original-commit-order.txt to detect lost commits
diff -u original-commit-order.txt post-rebase-order.txt
```

**Base Commit Selection**:
- Use the commit BEFORE the oldest commit you want to modify
- For example, to squash commits A, B, C, use the parent of A as base-commit
- Use `git log --oneline -20` to identify the correct base commit

#### 3. Configure Squash Operations with Chronological Validation {#3-configure-squash-operations-with-chronological-validation}

**🚨 CRITICAL CHRONOLOGICAL PRESERVATION REQUIREMENT:**
Git presents commits in chronological order (oldest first), and you MUST preserve this order for non-squashed
commits to prevent historical reordering.

**Step 3a: Review Git's Default Plan**
Git will present commits in chronological order:
```
pick eee7738 Restructure todo.md with proper architecture
pick 098c953 Add empirical data requirement for analysis tasks
pick 0ed377d Complete study-claude-cli-help empirical analysis  
pick f40ab63 Complete study-claude-core-tools comprehensive analysis
pick c29c828 Mark study-claude-startup-errors as completed
pick 709c52a Enhance task protocol with scope negotiation
pick d345c52 Complete study-claude-session-basics analysis task
pick c2b4a7e Add custom code style rules and enhance checkstyle rules
pick 31efd60 Enhance task protocol to enforce empirical validation
```

**Step 3b: Apply Squash Commands WITHOUT Reordering**
Mark targets for squashing while preserving chronological sequence:
```
pick eee7738 Restructure todo.md with proper architecture
squash 098c953 Add empirical data requirement for analysis tasks
pick 0ed377d Complete study-claude-cli-help empirical analysis  
pick f40ab63 Complete study-claude-core-tools comprehensive analysis
pick c29c828 Mark study-claude-startup-errors as completed
squash 709c52a Enhance task protocol with scope negotiation
pick d345c52 Complete study-claude-session-basics analysis task
pick c2b4a7e Add custom code style rules and enhance checkstyle rules
squash 31efd60 Enhance task protocol to enforce empirical validation
```

**🚨 MANDATORY CHRONOLOGICAL VALIDATION:**
Before saving the rebase plan, verify:
1. **Non-squashed commits appear in SAME ORDER as original history**
2. **No preserved commits have been moved relative to each other**  
3. **Only squash/fixup commands applied to target commits**

**Key Commands**:
- `pick`: Keep commit as-is (default for all commits)
- `squash` (or `s`): Combine with previous picked commit, keep both commit messages
- `fixup` (or `f`): Combine with previous picked commit, discard this commit's message
- `drop` (or `d`): Remove commit entirely from history
- `reword` (or `r`): Keep commit but edit the commit message
- `edit` (or `e`): Pause rebase to make changes to this commit

**CRITICAL SQUASHING RULES**:
1. **Cannot squash the first commit** - it needs a target to squash into
2. **Squash/fixup always targets the previous picked commit** in the list
3. **Order matters** - commits are applied top-to-bottom (chronologically)
4. **🚨 NEW: NEVER reorder preserved commits** - maintain exact original sequence

**⚠️ COMMON MISTAKE: Squashing Newer into Older**

❌ **WRONG**: Trying to squash HEAD into an earlier commit using rebase
```bash
# Current state: A - B - C - D - E (HEAD)
# Goal: Squash E into B

git rebase -i B^  # ❌ This reorders history, doesn't squash E into B
# Git shows: pick B, pick C, pick D, pick E
# Changing E to "fixup" combines E into D, NOT B!
```

**Root Cause**: Interactive rebase rebases FROM a point TO HEAD. Commits are listed chronologically (oldest first). Marking a newer commit as "squash" combines it with the **previous** commit in the list, not with an arbitrary earlier commit.

✅ **CORRECT**: Use manual method for non-adjacent squashing
```bash
# Method 1: Reset and amend (if E is HEAD)
git reset --soft B
git commit --amend  # E's changes now in B

# Method 2: Cherry-pick specific changes
git checkout B
git cherry-pick E
git commit --amend  # Combine E into B
git cherry-pick C..HEAD  # Reapply remaining commits

# Method 3: Manual edit + amend
git checkout B
# Manually apply E's changes to files
git add -u
git commit --amend  # Combine changes into B
git cherry-pick C..original-HEAD  # Reapply remaining
```

**When to use each method**:
- **Adjacent commits**: Interactive rebase with squash/fixup
- **Non-adjacent commits**: Manual method (reset/cherry-pick/edit)
- **Complex reordering**: Break into smaller adjacent squashes

#### 4. Handle Conflicts During Rebase {#4-handle-conflicts-during-rebase}

**When conflicts occur**, git will pause the rebase and display conflict information:

```bash
# Git will show something like:
CONFLICT (content): Merge conflict in docs/file.md
error: could not apply abc1234... commit message
Resolve all conflicts manually, mark them as resolved with
"git add/rm <conflicted_files>", then run "git rebase --continue".
```

**Systematic Conflict Resolution Process**:

1. **Identify conflicted files**:
   ```bash
   git status  # Shows files with conflicts
   ```

2. **Analyze each conflict**:
   ```bash
   # Open conflicted file in editor
   # Look for conflict markers:
   # <<<<<<< HEAD (current state)
   # content from target commit
   # =======
   # content from commit being applied  
   # >>>>>>> abc1234 (commit being applied)
   ```

3. **Apply conflict resolution strategy** (see below)

4. **Mark conflict as resolved**:
   ```bash
   git add <resolved-file>  # Stage the resolved file
   ```

5. **Continue rebase**:
   ```bash
   git rebase --continue
   ```

#### 5. Advanced Conflict Resolution Strategies {#5-advanced-conflict-resolution-strategies}

**Strategy 1: State Tracking Conflicts** (todo.md, documentation status, etc.)

```bash
# Common pattern: Task completion status conflicts
# <<<<<<< HEAD
# - [ ] incomplete task
# =======  
# - [x] completed task
# >>>>>>> commit-hash

# RESOLUTION STRATEGY: Choose the more advanced state
# If work was actually completed, choose [x]
# If work was reverted, choose [ ]
# Principle: Preserve actual work progress
```

**Strategy 2: Code Logic Conflicts**

```bash
# RESOLUTION STRATEGY: Analyze both implementations
# 1. Understand what each change accomplishes
# 2. Determine if changes are:
#    - Competing (choose one)
#    - Complementary (merge both)
#    - Sequential (apply in logical order)
# 3. Test the merged result if possible

# Example approach:
git show HEAD -- conflicted-file.java        # See current version
git show <commit-hash> -- conflicted-file.java  # See incoming version
# Make informed decision based on code analysis
```

**Strategy 3: Documentation and Configuration Conflicts**

```bash
# RESOLUTION STRATEGY: Preserve comprehensive information
# 1. Generally prefer the more complete/detailed version
# 2. Merge improvements from both sides when possible
# 3. Maintain consistency with project conventions
# 4. Ensure all referenced files/sections still exist
```

**Strategy 4: Dependency and Build Configuration Conflicts**

```bash
# RESOLUTION STRATEGY: Maintain build integrity
# 1. Choose version that maintains successful builds
# 2. If both versions build, prefer newer dependencies
# 3. Check for compatibility between conflicted components
# 4. Test build after resolution: mvn clean install
```

#### 6. Advanced Conflict Handling Scenarios {#6-advanced-conflict-handling-scenarios}

**Scenario A: Multiple Files with Related Conflicts**

When conflicts span multiple related files, resolve them as a group:

```bash
# Resolve all related files before continuing
git add file1.java file2.java config.xml
git rebase --continue
```

**Scenario B: Complex Three-Way Conflicts**

When squashing creates conflicts that involve multiple commits:

```bash
# Use git mergetool for complex conflicts
git mergetool
# or examine the full conflict history
git log --merge --oneline -p <conflicted-file>
```

**Scenario C: Conflicting Commit Messages During Squash**

When squashing commits, git will prompt for the combined commit message:

```bash
# Git opens editor with both messages:
# This is a combination of 2 commits.
# The first commit's message is:
# Original commit message 1
# 
# This is the 2nd commit message:
# Original commit message 2

# EDIT TO CREATE: Single coherent commit message
# Combine functionality descriptions
# Remove redundant information
# Follow project commit message conventions
```

#### 7. Error Recovery During Rebase {#7-error-recovery-during-rebase}

**If conflicts become too complex or you make mistakes:**

```bash
# ABORT entire rebase and return to original state
git rebase --abort

# Return to backup branch if needed
git reset --hard backup-before-squash-YYYYMMDD-HHMMSS
```

**If you accidentally continue with unresolved conflicts:**

```bash
# Check if build/tests still work
mvn clean install

# If problems exist, reset to backup and restart
git reset --hard backup-before-squash-YYYYMMDD-HHMMSS
```

**If you need to skip a problematic commit:**

```bash
# Skip current commit (use carefully - causes data loss)
git rebase --skip

# Better alternative: edit the rebase plan
# 1. git rebase --abort  
# 2. git rebase -i <base-commit>
# 3. Change 'pick' to 'drop' for problematic commit
```

#### 8. Verify Results with Chronological Validation {#8-verify-results-with-chronological-validation}

After successful rebase, perform comprehensive verification:

**Step 8a: Commit History Verification**
```bash
git log --oneline -15                    # Check commit history
git log --stat -5                        # Review file changes
```

**Step 8b: 🚨 MANDATORY Chronological Order Validation**
```bash
# Compare preserved commit order against original
git log --oneline backup-before-squash-YYYYMMDD-HHMMSS > original-order.txt
git log --oneline HEAD > new-order.txt

# CRITICAL: Verify non-squashed commits maintain relative order
echo "=== CHRONOLOGICAL VALIDATION ==="
echo "Check that preserved commits appear in same relative order:"
diff -u original-order.txt new-order.txt
```

**Step 8c: Content and Build Verification**
```bash
git diff backup-before-squash-YYYYMMDD-HHMMSS  # Verify changes are correct  
mvn clean install                        # Verify build integrity
```

**Success Criteria**:
- ✅ Commit count reduced by number of squashed commits
- ✅ All functionality preserved
- ✅ Build and tests pass
- ✅ Commit messages are coherent and informative
- ✅ **🚨 NEW: Preserved commits maintain chronological order**

**🚨 FAILURE CRITERIA - Abort and Restart if:**
- Preserved commits appear in different relative order than original
- Any non-target commits have been accidentally moved
- Chronological sequence has been disrupted

**Recovery from Chronological Violations:**
```bash
# If chronological order violated, abort and restart
git reset --hard backup-before-squash-YYYYMMDD-HHMMSS
echo "CHRONOLOGICAL VIOLATION DETECTED - Restarting with corrected plan"
# Return to Step 2 and create proper chronological plan
```

#### 9. Error Recovery {#9-error-recovery}

**Interactive Rebase Issues**:

```bash
# If interactive rebase doesn't complete, debug environment before switching methods

# Debug steps:
git config --get core.editor
echo $EDITOR
git rebase --abort  # Reset to clean state
# Fix editor configuration, then retry

# ❌ PROHIBITED: Switching to git reset or other destructive methods
# ✅ CORRECT: Fix environment issue, retry same method
```

**Emergency Abort and Recovery**:
```bash
# ABORT entire rebase and return to original state
git rebase --abort
# Return to backup branch if needed
git reset --hard backup-before-squash-YYYYMMDD-HHMMSS
```

#### 10. Finalization {#10-finalization}

If results are satisfactory:

```bash
# Optional: Update commit messages if needed
git commit --amend  # for most recent commit only

# The rebase is complete - no branch switching needed
# Original branch now contains the squashed history
```

#### 10. Cleanup Backup Branch {#10-cleanup-backup-branch}

Only after confirming everything works correctly:

```bash
git branch -D backup-before-squash-YYYYMMDD-HHMMSS
```

### Critical Success Factors for Complex Squash Operations {#critical-success-factors-for-complex-squash-operations}

**🚨 MANDATORY CHRONOLOGICAL PRESERVATION PROTOCOL:**
1. **Pre-rebase Planning**: Document original commit order before starting
2. **Rebase Plan Validation**: Verify preserved commits maintain sequence  
3. **Post-rebase Verification**: Systematically validate chronological integrity
4. **Failure Recovery**: Immediate rollback if chronological violations detected

**Common Failure Patterns to Avoid:**
- ❌ Applying squash commands without verifying preserved commit order
- ❌ Assuming git automatically maintains chronological sequence
- ❌ Skipping post-rebase chronological validation
- ❌ Accepting chronological violations as "acceptable side effects"

### Handling Non-Contiguous Commits {#handling-non-contiguous-commits}

**CRITICAL DECISION POINT**: When commits to be squashed are not adjacent in git history, you must choose the
appropriate strategy based on your specific situation.

#### Decision Matrix for Non-Contiguous Commits {#decision-matrix-for-non-contiguous-commits}

**Scenario A: Historical Reorganization** (commits already exist, not adjacent)
- **User Request Pattern**: "Squash commits A, B, C into commit D" where commits are scattered
- **Required Approach**: Interactive rebase with careful reordering
- **Safety Requirements**: Must preserve intermediate unrelated commits

**Scenario B: Future Development Workflow** (adding fixes to existing commits)  
- **User Request Pattern**: "Fix this issue in commit X" where X is several commits back
- **Preferred Approach**: Fixup commits + autosquash
- **Benefits**: Maintains timeline until final squashing

#### Strategy 1: Interactive Rebase for Non-Contiguous Historical Commits {#strategy-1-interactive-rebase-for-non-contiguous-historical-commits}

**When to Use**: Historical reorganization of existing commits that are not adjacent

**MANDATORY SAFETY PROTOCOL**:
```bash
# 1. Document ALL commits in the range to prevent data loss
git log --oneline <oldest-target>^..HEAD > commit-inventory.txt

# 2. Identify EXACTLY which commits to squash vs. preserve
echo "SQUASH TARGETS:" > rebase-plan-detailed.txt
echo "- eb6925d Refactor JavadocParameterAlignment" >> rebase-plan-detailed.txt
echo "- e6477b0 Add checkstyle tests" >> rebase-plan-detailed.txt
echo "- 95375a5 Apply code formatting updates" >> rebase-plan-detailed.txt
echo "" >> rebase-plan-detailed.txt
echo "PRESERVE (maintain exact order):" >> rebase-plan-detailed.txt
echo "- f3824d3 Complete study-claude-streaming-behavior" >> rebase-plan-detailed.txt
echo "- d7fbd9a Complete study-claude-tool-orchestration" >> rebase-plan-detailed.txt

# 3. Start interactive rebase with careful reordering
git rebase -i <base-commit>
```

**CRITICAL REORDERING PROCEDURE**:
1. **Move related commits together** in the interactive rebase editor
2. **Preserve chronological order** for all non-target commits
3. **Apply squash commands** only after grouping related commits

**Example Interactive Rebase Plan**:
```
# Original git presents (chronologically oldest to newest):
pick f888a4c Add custom code style rules
pick 95375a5 Apply code formatting updates          # TARGET 1
pick f3824d3 Complete study-claude-streaming        # PRESERVE - keep position
pick e6477b0 Add checkstyle tests                   # TARGET 2  
pick eb6925d Refactor JavadocParameterAlignment     # TARGET 3

# MODIFY TO (group targets together, preserve others):
pick f888a4c Add custom code style rules
squash 95375a5 Apply code formatting updates        # Move and squash into f888a4c
squash e6477b0 Add checkstyle tests                 # Move and squash into f888a4c
squash eb6925d Refactor JavadocParameterAlignment   # Move and squash into f888a4c  
pick f3824d3 Complete study-claude-streaming        # Preserve original position
```

**VIOLATION PREVENTION**:
- ❌ **Never reorder preserved commits** relative to each other
- ❌ **Never drop commits** that weren't in the squash request  
- ✅ **Only reorder commits that are being squashed together**
- ✅ **Maintain all other commits in their original positions**

#### Strategy 2: Fixup Workflow for Future Development {#strategy-2-fixup-workflow-for-future-development}

**When to Use**: When you need to make fixes to older commits during active development

**Procedure**:
```bash
# 1. Create fixup commit targeting specific older commit
git add <files-to-fix>
git commit --fixup <target-commit-hash>

# 2. Continue development normally (creates timeline preservation)
git commit -m "New feature work"  
git commit -m "More development" 

# 3. Later, squash fixups automatically when ready
git rebase -i --autosquash <base-commit>
```

**Advantages of Fixup Approach**:
- Preserves development timeline until final squash
- Clear intent marking (fixup! commits are obvious)  
- Atomic safety (each step is a working commit)
- Git handles the arrangement logic automatically

**Fixup Limitations**:
- Only works for amendments to existing commits
- Cannot reorganize completely unrelated commits
- Still requires interactive rebase for final application

#### Critical Success Factors for Non-Contiguous Squashing {#critical-success-factors-for-non-contiguous-squashing}

**MANDATORY VERIFICATION AFTER REORDERING**:
```bash
# 1. Verify ALL expected commits are present
git log --oneline -20 > post-rebase-commits.txt
wc -l post-rebase-commits.txt  # Should be original count minus squashed count

# 2. Verify preserved commits maintain their relative order
# Check that non-squashed commits appear in same sequence as original

# 3. Content verification
git diff backup-branch..HEAD --stat  # Should show only intended changes
```

**EMERGENCY RECOVERY**:
```bash
# If commit order is violated or commits are dropped:
git rebase --abort
git reset --hard backup-branch
echo "REORDER VIOLATION - Restarting with corrected plan"
```

### When Interactive Rebase May Not Be Suitable {#when-interactive-rebase-may-not-be-suitable}

**Use Cherry-pick Method Instead When**:
- Working with commits from different branches/remotes
- Need very fine-grained control over individual commit application
- Interactive rebase repeatedly fails due to complex conflicts (>50 commits with extensive conflicts)
- Working with a corrupted or unusual git history structure

**Note on Chronological Preservation**: Interactive rebase is fully capable of maintaining chronological order
in all scenarios when proper planning and verification procedures are followed. The complexity lies in human
planning, not in git's technical capabilities.

### When to Abandon Recovery and Start Fresh {#when-to-abandon-recovery}

**Sometimes the fastest path forward is to abandon a diverged branch and re-apply changes cleanly.**

**Consider Starting Fresh When**:
- Branch has diverged significantly from main (>10 conflicts during rebase)
- Multiple failed rebase/cherry-pick attempts with no clear resolution
- Changes are well-understood and can be re-applied manually
- Time spent on recovery exceeds time to re-do the work

**Fresh Start Procedure**:
```bash
# 1. Save your current work (document what was changed)
git diff main > /tmp/my-changes.patch  # Optional: save as patch

# 2. Create fresh branch from main
git checkout main
git pull origin main  # Ensure up-to-date
git checkout -b my-task-fresh

# 3. Re-apply changes manually
# For bulk renames: Re-run the rename script with correct mappings
# For code changes: Re-implement using the patch as reference

# 4. Run tests to verify
./mvnw verify -Dmaven.build.cache.enabled=false
```

**REAL-WORLD EXAMPLE**: A bulk TokenType rename task diverged after a failed rebase. After two recovery
attempts (rebase abort, cherry-pick with conflicts), resetting to main and re-applying the rename script
took 10 minutes vs. 30+ minutes of failed recovery attempts.

## Cherry-pick Method (Advanced/Fallback Approach) {#cherry-pick-method-advancedfallback-approach}

**⚠️ NOTE**: This is a complex manual approach. Use Interactive Rebase (see above) for most cases.

### ⚠️ CRITICAL: User Request Validation {#critical-user-request-validation}

**BEFORE STARTING**: Parse the user's squash request EXACTLY to avoid data loss.

**USER REQUEST PATTERN**: "Squash commits A, B, C, D, E, F into commit G"

**MANDATORY VALIDATION**:
1. ✅ Verify each requested commit hash exists: `git show A`, `git show B`, etc.
2. ✅ Identify the BASE commit G (target for squashing)
3. ✅ Identify ALL commits between base G and HEAD that are NOT in the user's list
4. ✅ These non-listed commits MUST be preserved exactly as they are

**COMMON ERROR**: Assuming "squash into G" means "squash everything from G to HEAD"
**CORRECT INTERPRETATION**: "squash ONLY the specified commits A,B,C,D,E,F into G"

### Procedure {#procedure}

#### 1. Create Backup Branch {#1-create-backup-branch}

Always create a backup branch before performing complex git operations:

```bash
git checkout -b backup-before-squash-$(date +%Y%m%d-%H%M%S)
git checkout main  # or your working branch
```

#### 2. Analyze Commit Structure and Validate User Request {#2-analyze-commit-structure-and-validate-user-request}

**🚨 CRITICAL**: Parse the user's EXACT request to identify specific commits to squash:

```bash
# If user specifies: "Squash d1edc60f7, d4ca9202a, 1311b64bf, 31efd60a1, 709c52ab1, 098c95394 into eee773804f"
# MANDATORY: Validate each commit hash exists and identify the EXACT range
git show --oneline d1edc60f7  # Must exist and be target commit
git show --oneline 098c95394  # Must exist and be target commit  
git show --oneline eee773804f # Must exist and be base commit
```

**🚨 COMMIT RANGE VALIDATION**: Identify ALL commits in the range - those to be squashed, intermediate commits,
AND commits after the targets:

```bash
git log --oneline -25  # Use broader range to see all affected commits
# MANUALLY verify each commit in the user's request exists in this list
```

Example structure:
```
xyz4567 (commit AFTER targets - MUST PRESERVE)
wvu8901 (commit AFTER targets - MUST PRESERVE)
abc1234 (commit AFTER targets - MUST PRESERVE)
def5678 (commit to squash - newer TARGET)
ghi9012 (intermediate commit to preserve)  
jkl3456 (intermediate commit to preserve)
mno7890 (commit to squash - older TARGET)
pqr1234 (base commit)
```

**⚠️ CRITICAL ERROR TO AVOID**: Forgetting commits that come AFTER the target commits will result in permanent
data loss.

#### 3. Create Working Branch {#3-create-working-branch}

Create a new branch with "squashed" in the name for the squash operation:

```bash
git checkout -b main-squashed  # Keep original branch name + "squashed"
# or for feature branches: git checkout -b feature-name-squashed
```

**CRITICAL**: The original branch keeps its name, the new branch gets "squashed" suffix.

#### 4. Perform Targeted Soft Reset {#4-perform-targeted-soft-reset}

**🚨 CRITICAL ERROR PREVENTION**: Only reset to capture the EXACT commits requested for squashing:

```bash
# WRONG: git reset --soft base-commit  # This captures ALL commits from base to HEAD
# RIGHT: Use interactive rebase or cherry-pick approach for non-adjacent commits

# For user request "Squash A, B, C into D":
# Step 1: Create new branch from base commit D
git checkout -b temp-squash D

# Step 2: Cherry-pick each target commit to create combined diff
git cherry-pick --no-commit A
git cherry-pick --no-commit B  
git cherry-pick --no-commit C
# Now staging area contains ONLY the changes from A, B, C
```

**ALTERNATIVE SAFE APPROACH** for non-adjacent commits:
```bash
# Reset to base, then apply only target commits
git reset --soft D  # base commit specified by user
# DANGER: This stages ALL commits from D to HEAD
# MUST manually exclude non-target commits (see next section)
```

#### 5. Handle Staging Area for Exact Target Commits {#5-handle-staging-area-for-exact-target-commits}

**🚨 IF USING SOFT RESET APPROACH**: Remove changes from non-target commits before committing:

```bash
# After git reset --soft base-commit, staging area contains ALL commits from base to HEAD
# MUST unstage files that belong to non-target commits

# Identify files changed by non-target commits:
git diff --name-only intermediate-commit-1^..intermediate-commit-1
git diff --name-only commit-after-target-1^..commit-after-target-1

# Unstage those files and restore them to their non-target commit state:
git restore --staged path/to/file/from/non-target-commit
git restore --source=intermediate-commit-1 path/to/file/from/non-target-commit

# WARNING: This approach is error-prone. Cherry-pick method (step 4) is safer.
```

#### 6. Create Squashed Commit {#6-create-squashed-commit}

Create a new commit that combines the functionality of ONLY the target commits:

```bash
git commit -m "feature: Combined functionality: descriptive message

- Feature from commit mno7890
- Enhancement from commit def5678
- Additional changes from intermediate commits
- Comprehensive description of combined functionality

"
```

#### 7. Restore ALL Non-Target Commits   {#7-restore-all-non-target-commits}

**🚨 CRITICAL**: Cherry-pick ALL commits that were NOT in the user's squash request, in chronological order:

```bash
# Cherry-pick intermediate commits first (between targets)
git cherry-pick jkl3456  # intermediate commit 1 (older)
git cherry-pick ghi9012  # intermediate commit 2 (newer)

# Cherry-pick ALL commits that came AFTER the target commits
git cherry-pick abc1234  # commit after targets 1
git cherry-pick wvu8901  # commit after targets 2  
git cherry-pick xyz4567  # commit after targets 3
# Continue for ALL commits that came after the newest target
```

**⚠️ MANDATORY**: Every commit that existed in the original history (except the two being squashed) MUST be
preserved.

#### 8. Verify Result {#8-verify-result}

Check the final commit structure:

```bash
git log --oneline -10
```

Expected result:
```
xyz4567 (commit after targets 3 - cherry-picked)
wvu8901 (commit after targets 2 - cherry-picked)
abc1234 (commit after targets 1 - cherry-picked)
ghi9012 (intermediate commit 2 - cherry-picked)
jkl3456 (intermediate commit 1 - cherry-picked)  
stu1098 (squashed commit with combined functionality)
pqr1234 (base commit)
```

**✅ SUCCESS CRITERIA**: The new history contains the same number of commits as the original, minus one
(because two commits were squashed into one).

#### 9. Test Build Integrity {#9-test-build-integrity}

Verify that the changes work correctly:

```bash
mvn clean install
# Run relevant tests
```

#### 10. User Approval {#10-user-approval}

Present the results to the user and get approval for the changes.

#### 11. Replace Original Branch (if approved) {#11-replace-original-branch-if-approved}

If the user approves the result, replace the original branch:

```bash
git checkout main  # switch to original branch
git reset --hard main-squashed  # replace with squashed version
git branch -D main-squashed  # delete the squashed branch
```

**WORKFLOW**: Original branch is replaced, squashed branch is deleted and renamed to original name.

#### 12. Cleanup Backup Branch (if approved) {#12-cleanup-backup-branch-if-approved}

Remove the backup branch only after user confirmation:

```bash
git branch -D backup-before-squash-YYYYMMDD-HHMMSS
```

**NOTE**: Only delete backup branch after confirming squashed branch works correctly.

### Important Notes {#important-notes}

- **Always create backups** before complex git operations
- **Test build integrity** after any history rewriting
- **Get user approval** before removing backup branches
- **Preserve commit messages** that provide valuable context
- **Maintain logical commit grouping** when possible

### 🚨 CRITICAL DATA LOSS PREVENTION {#critical-data-loss-prevention}

**MOST COMMON ERROR**: Dropping commits that come AFTER the target commits.

**MANDATORY CHECKLIST** before declaring squash complete:
1. ✅ Count commits in original: `git rev-list --count backup-branch`
2. ✅ Count commits in new branch: `git rev-list --count new-branch`  
3. ✅ Verify: new count = original count - 1 (exactly one less due to squashing)
4. ✅ If counts don't match: **STOP** and identify missing commits

**RECOVERY WHEN COMMITS ARE DROPPED**:
```bash
# Compare what was lost
git log --oneline backup-branch --not new-branch

# Cherry-pick each missing commit
git cherry-pick <missing-commit-hash>
```

### When to Use This Procedure {#when-to-use-this-procedure}

Use this squashing procedure when:
- Combining related functionality scattered across the commit history
- Consolidating rule development or feature work
- Creating cleaner release branches
- Preparing commits for code review

### When NOT to Use {#when-not-to-use}

Avoid this procedure when:
- Simple adjacent commit squashing is sufficient
- The intermediate commits are closely related to the squashed commits
- The commit history is complex with many merge commits
- Working on shared branches with multiple contributors

## File Extraction from Commits - Stale Base Danger {#file-extraction-stale-base}

### The Problem

When extracting files from commits using `git show <commit>:<file>` or `git checkout <commit> -- <file>`,
the extracted file contains the **ENTIRE file content** from that commit - not just the changes made in
that commit.

**If the source commit was based on an older version of main**, the extracted file will contain:
1. The base code from the OLD main (missing newer changes)
2. Plus the changes made in that commit

This silently removes any code that was added to main AFTER the source commit's base.

### Real-World Example

```
main:        A --- B --- C --- D  (current main, D has new switch-parsing code)
                    \
feature:             E --- F --- G  (feature branch, based on B)
```

If you extract `Parser.java` from commit G:
- You get Parser.java as it existed at G
- This is based on B's version of Parser.java
- **Missing**: All changes from C and D (60 lines of switch-parsing code)

**Result**: If you use this extracted file, you silently DELETE the switch-parsing code.

### Detection

The `warn-file-extraction.sh` hook detects these patterns and warns:
- `git show <commit>:<file>`
- `git checkout <commit> -- <file>`
- `git restore --source=<commit> -- <file>`

### Safe Alternatives

**Alternative 1: Apply only the delta changes (RECOMMENDED)**:
```bash
# Find the base of the source commit
BASE_COMMIT=$(git merge-base main <source-commit>)

# Extract ONLY the changes made between base and source
git diff $BASE_COMMIT..<source-commit> -- <file> | git apply
```

**Alternative 2: Rebase the source branch onto current main first**:
```bash
# Rebase feature branch to current main
git checkout feature-branch
git rebase main

# Now files in feature-branch have current main as base
```

**Alternative 3: Manual merge of specific changes**:
```bash
# Compare the file between commits
git diff main:<file> <source-commit>:<file>

# Manually apply only the NEW changes from source-commit
# while keeping the current main base
```

### Verification Before File Extraction

**MANDATORY checks before extracting files from commits**:

```bash
# 1. Find what main looked like when source commit was created
MERGE_BASE=$(git merge-base main <source-commit>)
echo "Source commit was based on: $MERGE_BASE"

# 2. Check if main has moved forward since then
git log --oneline $MERGE_BASE..main
# If there are commits here, main has NEW code not in source commit

# 3. Compare the specific file
git diff main:<file> <source-commit>:<file>
# Lines starting with '-' are code that WOULD BE REMOVED by extraction
```

**If main has moved forward**: Do NOT use file extraction. Use the delta approach instead.

## Git History Rewriting Safety {#git-history-rewriting-safety}

### Overview

Git history rewriting commands (`git filter-branch`, `git rebase`, `git reset`) are powerful but dangerous.
Incorrect usage can permanently destroy commits across multiple branches, including protected version branches.

### Protected Branch Pattern

**Version branches MUST NEVER be affected by history rewriting operations.**

```bash
# Pattern: v followed by digits (v1, v13, v21, etc.)
PATTERN="^v[0-9]+$"

# Check for version branches before any history operation
git branch | grep -E "^  v[0-9]+"
```

### Prohibited Patterns

**NEVER use these flags with history-rewriting commands:**

```bash
# ❌ PROHIBITED - Affects ALL branches including version branches
git filter-branch --all ...
git filter-branch --branches ...
git rebase -i HEAD~10 --all

# ❌ PROHIBITED - Global operations without branch targeting
git filter-branch -- --all
git filter-branch --index-filter 'command' -- --all
```

### Required Safety Procedures

**Before ANY history-rewriting operation:**

```bash
# Step 1: Document current branch state
git branch -v > /tmp/branch-state-before.txt

# Step 2: Identify protected version branches
VERSION_BRANCHES=$(git branch | grep -E "^  v[0-9]+" | wc -l)
echo "Found $VERSION_BRANCHES protected version branches"

# Step 3: Verify target is specific branch, not --all
# ✅ CORRECT: Target specific branch
git filter-branch --tree-filter 'command' main

# ❌ WRONG: Target all branches
git filter-branch --tree-filter 'command' --all
```

### Safe History Rewriting Examples

**Filter-branch (remove file from history):**

```bash
# ✅ CORRECT - Targets only main branch
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch path/to/sensitive-file' \
  --prune-empty -- main

# ❌ WRONG - Would affect all branches including v1, v13, v21
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch path/to/sensitive-file' \
  --prune-empty -- --all
```

**Rebase (squash commits):**

```bash
# ✅ CORRECT - Interactive rebase on current branch only
git checkout feature-branch
git rebase -i main

# ❌ WRONG - Never rebase version branches
git checkout v21
git rebase -i main  # PROHIBITED - corrupts version marker
```

### Pre-Operation Checklist

Before executing any history-rewriting command:

1. **Verify current branch**: `git branch --show-current`
2. **List all branches**: `git branch -a`
3. **Identify version branches**: `git branch | grep -E "^  v[0-9]+"`
4. **Confirm command targets specific branch** (not `--all` or `--branches`)
5. **Create backup**: `git checkout -b backup-$(date +%Y%m%d-%H%M%S)`

### Recovery Procedures

**If version branches are accidentally modified:**

```bash
# Check reflog for original commit
git reflog show v21

# Restore version branch to original commit
git branch -f v21 <original-commit-sha>

# Verify restoration
git log --oneline v21 -1
```

### Enforcement

The hook `.claude/hooks/validate-git-filter-branch.sh` blocks commands containing `--all` or `--branches`
flags to prevent accidental corruption of version branches.

---

**⚠️ Warning**: Git history rewriting is a destructive operation. Always ensure you have backups and user
approval before proceeding with complex squash operations.
