# Claude Code Configuration Guide

> **Version:** 3.0 | **Last Updated:** 2025-10-18
> **Related Documents:** [main-agent-coordination.md](docs/project/main-agent-coordination.md) •
[task-protocol-agents.md](docs/project/task-protocol-agents.md) •
[style-guide.md](docs/project/style-guide.md) • [quality-guide.md](docs/project/quality-guide.md)

Styler Java Code Formatter project configuration and universal guidance for all agents.

## 🚨 MANDATORY STARTUP PROTOCOL

**MAIN AGENT**: Task protocol uses just-in-time guidance via hooks. You do NOT need to read protocol
files upfront. Phase-specific instructions provided automatically as you transition states. Reference docs
available for troubleshooting: main-agent-coordination.md, task-protocol-core.md

### ⚠️ MANDATORY USER APPROVAL CHECKPOINTS

**CRITICAL**: Two checkpoints require EXPLICIT user approval before proceeding. User approval checkpoints
are tied to task.json state transitions. You MUST progress through the state machine - hooks only enforce
protocol when states are used correctly.

**State Workflow**:
```
INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → [USER APPROVAL] → IMPLEMENTATION → VALIDATION →
AWAITING_USER_APPROVAL → [USER APPROVAL] → COMPLETE
         ↓              ↓              ↓                              ↓                             ↓
    Create task.md  Gather reqs   Plan impl      Invoke agents    Build/test              Merge to main
```

**CRITICAL: REQUIREMENTS Phase is MANDATORY**

❌ **WRONG**: Main agent writes implementation plan directly from todo.md
✅ **CORRECT**: Invoke stakeholder agents → Read reports → Synthesize plan

**Required stakeholder agents** (invoke in parallel for requirements gathering):
- `architect` - Analyzes dependencies, design patterns, integration points
- `tester` - Analyzes test coverage needs, test strategy, edge cases, business logic validation
  - **⚠️ CRITICAL**: Tester requirements MUST focus on **business rules to validate**, NOT test counts.
    Specify WHAT behaviors/scenarios to test, not HOW MANY tests. Test count is an output of implementation,
    not an input to planning.
- `formatter` - Specifies documentation requirements, code style standards

**⚠️ COMMON MISTAKE**: Do NOT invoke "engineer" for testing requirements (engineer = code
quality/refactoring; tester = test strategy/coverage)

**Workflow**:
1. CLASSIFIED state: **MANDATORY** - Invoke ALL stakeholder agents in REQUIREMENTS mode with parallel
   coordination
   - **PREFERRED** (Best Practice - True Parallel): Single message with 3 Task tool calls
   - **ACCEPTABLE** (Achieves Coordination Goal): Rapid succession within same conversation turn (all
     within <30 seconds), no user messages between invocations. Evidence: Session 3fa4e964 had 17-31s
     spreads, marked COMPLIANT
   - **VIOLATION** (Prevents Coordination): Sequential calls across multiple user interactions; user
     interruption between agent invocations
   - **Clarification on Timing**: Focus on avoiding user interruptions, not microsecond timing. 6-30
     second gaps within same turn = ACCEPTABLE (achieves coordination). >30 second delays suggest
     sequential work.
   - **CRITICAL**: Prompts MUST specify output file `/workspace/tasks/{task}/{task}-{agent}-requirements.md`
     and emphasize "REQUIREMENTS mode. ONLY write requirements report. DO NOT implement code."
2. Wait for completion: Each writes `{task-name}-{agent}-requirements.md`
3. **VERIFY reports exist**: `ls -la /workspace/tasks/{task}/*-requirements.md` (Must show all 3 files)
4. READ all reports: Main agent synthesizes into unified plan
5. SYNTHESIS state: Write implementation plan to task.md
6. Get user approval
7. IMPLEMENTATION state: **MANDATORY** - Invoke ALL stakeholder agents in IMPLEMENTATION mode with
   parallel coordination (PREFERRED: single message, ACCEPTABLE: rapid succession <10s within same turn,
   no user interruption)

**Checkpoint 1: SYNTHESIS → IMPLEMENTATION** (Plan Approval)
   - SYNTHESIS state: Create implementation plan in task.md
   - **STOP and PRESENT**: Show plan to user
   - **WAIT for user approval**: User must say "approved", "proceed", "looks good"
   - **ONLY THEN**: Create `/workspace/tasks/{task}/user-approved-synthesis.flag`
   - **Transition to IMPLEMENTATION**: Use `state-transition` skill (see pattern in § State Transition
     Pattern below)
   - Hook will BLOCK Task tool invocations from INIT state and IMPLEMENTATION transition without approval
     flag

**Checkpoint 2: AWAITING_USER_APPROVAL → COMPLETE** (Change Review)
   - AWAITING_USER_APPROVAL state: After validation passes
   - **⚠️ MANDATORY**: Use `pre-presentation-cleanup` skill before showing changes to user
   - **STOP and PRESENT**: Show commit SHA and `git diff --stat main...task-branch`
   - **WAIT for user approval**: User must say "approved", "merge it", "LGTM"
   - **⚠️ DISTINGUISH**: Git manipulation requests ("squash the commits", "rebase", "amend") are
     preprocessing instructions, NOT approval. Execute them and re-present for review.
   - **ONLY THEN**: Create `/workspace/tasks/{task}/user-approved-changes.flag`
   - **⚠️ MANDATORY: Update archival files BEFORE merge**:
     - Update todo.md: Mark task complete with date
     - Update changelog.md: Add entry describing changes
     - Amend task branch commit to include archival files
     - Use `archive-task` skill for atomic update
   - **Transition to COMPLETE**: Use `state-transition` skill (or see pattern below)
   - **Merge with --ff-only**: `git merge --ff-only {task-branch}` (linear history, no merge commits)
   - Hook will BLOCK merges without approval flag, from wrong directory, or missing archival files

**Final Step: COMPLETE → CLEANUP** (Clean Repository)
   - After merge to main: Use `state-transition` skill to transition to CLEANUP (or see pattern below)
   - Delete all task branches: `git branch -D {task-name} {task-name}-architect {task-name}-engineer
     {task-name}-formatter`
   - Remove all worktrees: `git worktree remove /workspace/tasks/{task-name}/code` and agent worktrees
   - Verify cleanup: `git branch | grep {task-name}` should return nothing
   - **Delete task directory**: `rm -rf /workspace/tasks/{task-name}` (audit trail in git history)
   - Reference: [task-protocol-core.md § COMPLETE → CLEANUP
     Transition](docs/project/task-protocol-core.md#complete-cleanup-transition)

**⚠️ MANDATORY: State Transition Pattern** {#state-transition-pattern}

**NEVER update just `.state`** - ALL state transitions MUST also update `.transition_log`:

```bash
# ❌ WRONG - Missing transition_log (causes audit trail gaps)
jq '.state = "COMPLETE"' task.json > tmp.json && mv tmp.json task.json

# ✅ CORRECT - Full transition with audit trail
jq --arg old "AWAITING_USER_APPROVAL" --arg new "COMPLETE" \
   --arg ts "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
   '.state = $new | .transition_log += [{"from": $old, "to": $new, "timestamp": $ts}]' \
   task.json > tmp.json && mv tmp.json task.json
```

**Recommended**: Use `state-transition` skill for safe transitions with automatic validation.

**NEVER**:
- Skip state progression (staying in INIT throughout task)
- Invoke Task tool before transitioning to CLASSIFIED/IMPLEMENTATION
- Proceed to IMPLEMENTATION without presenting plan
- Merge to main without presenting changes
- Assume silence or bypass mode means approval
- Use `git merge` without `--ff-only` when merging task to main

**Enforcement**:
- `task-invoke-pre.sh` blocks Task tool from INIT state and SYNTHESIS/IMPLEMENTATION states without
  requirements reports
- `enforce-merge-workflow.sh` validates merge location and approval flag
- `enforce-commit-squashing.sh` validates single commit and `--ff-only` flag
- `enforce-atomic-archival.sh` validates task branch includes todo.md and changelog.md
- `enforce-checkpoints.sh` validates state transitions and approval flags

**SUB-AGENTS**: If you are a stakeholder agent (architect, engineer, formatter), this file contains
universal guidance only. You MUST also read `/workspace/main/docs/project/task-protocol-agents.md`

**Domain-Specific Agents**:
- **Formatter agents**: Read `/workspace/main/docs/project/style-guide.md`
- **Engineer agents**: Read `/workspace/main/docs/project/quality-guide.md`

## Universal Guidance

### Professional Objectivity

Prioritize technical accuracy over validating user beliefs. Provide direct, objective information without
superlatives, praise, or emotional validation. Apply rigorous standards to all ideas and disagree when
necessary. When uncertain, investigate first rather than confirming user beliefs.

### Tone and Style

- Output displays on CLI. Keep responses short and concise. Use Github-flavored markdown.
- Output text to communicate with user; all text outside tool use is displayed. Never use Bash or code
  comments to communicate.
- NEVER create files unless absolutely necessary. ALWAYS prefer editing existing files.

### Self-Validation Before Decisions

**MANDATORY**: Verify logical consistency before presenting decisions, especially when applying thresholds or
conditional logic.

**Self-Check Pattern**:
1. State the value/score: "Score = 0.809"
2. Identify the threshold range: "0.809 is in range 0.75-0.84"
3. Apply decision logic: "0.809 < 0.85, therefore ITERATE"
4. **Verify consistency**: Does stated range match the comparison? Does decision match the logic?

**Common Mistake**: ❌ "Score 0.809, range 0.75-0.84, above floor 0.85" (contradiction); ✅ "Score 0.809,
range 0.75-0.84, below 0.85 threshold, iterate"

**Anti-Pattern Detection**: Stating X < Y then X > Y; decision contradicts threshold; range membership
contradicts comparison

**Tool-Val**: Mandatory tool=INVOKE. ❌Manual checklist when skill requires /compare-docs

### System-Reminder Instructions

**MANDATORY**: After tool results, ALWAYS check for `<system-reminder>` tags containing user instructions.

**⚠️ CRITICAL PATTERN**: User instructions can appear in system-reminders DURING your response (embedded in
tool results). These are NOT optional suggestions - they are user requests that MUST be addressed.

**Common Mistake**:
- ❌ Complete first task, ignore system-reminder with second instruction
- ❌ Treat system-reminder instructions as "handled later"
- ✅ Process ALL user instructions from system-reminders before responding

**Detection Pattern**: After each tool result, scan for:
```
<system-reminder>
...user sent the following message...
</system-reminder>
```

**Action Required**: If found, address the instruction IMMEDIATELY before continuing with other work.

### Environment State Verification

**MANDATORY**: NEVER claim or act on environment state (directory, branch, file existence) without
verification first.

**⚠️ DANGEROUS PATTERN**: Making assertions about state without evidence:
- ❌ "The build is running from main, not the task worktree. Let me fix:"
- ❌ "We're on the wrong branch, switching to..."
- ❌ "The file doesn't exist, creating..."

**CORRECT PATTERN**: Verify THEN claim THEN act:
```bash
# ✅ CORRECT - Verify before claiming
pwd                                    # Verify first
# Output: /workspace/tasks/my-task/code
# "Current directory is /workspace/tasks/my-task/code, running build..."

# ❌ WRONG - Claim without verification
# "The build is running from main..." (assumed without pwd)
```

**Verification Commands**:
- Directory: `pwd`
- Branch: `git branch --show-current`
- File existence: `ls -la {file}` or `test -f {file}`
- Worktree status: `git worktree list`

**Why This Matters**: False claims about environment state lead to:
1. Operations in wrong directory/branch
2. Unnecessary "fixes" that break things
3. Confusion about actual system state
4. Potential data loss or corruption

### Code Lifecycle Policy

**NO DEPRECATION - Remove Outright**

When code, configuration, or documentation becomes obsolete:
- ✅ DELETE immediately - Remove files, hooks, configurations completely
- ✅ UPDATE references - Clean up changelog, documentation, and dependent code
- ❌ DO NOT deprecate - No "deprecated" markers, backup files, or dormant code
- ❌ DO NOT keep "for reference" - Git history preserves old versions

**When removing**: (1) Delete all obsolete files/code (2) Update changelog with "Removed" section (3)
Update documentation (4) Verify no broken references

**NO STUBBING - Complete or Simplify**

When implementing features, complete them fully or simplify the API to match what you can deliver:
- ✅ Complete implementation matches API surface (all fields/methods work)
- ✅ Simplify API if feature is too complex (remove unused fields, use simpler design)
- ❌ DO NOT create "for future use" fields/parameters that aren't implemented
- ❌ DO NOT add comments like "for now", "a full implementation would", "TODO"

**Detection patterns** (indicate stubbing violations):
- Record/class fields that are never read
- Configuration parameters that have no effect
- Comments suggesting incomplete implementation
- API promising more than implementation delivers

**Example violation**:
```java
// ❌ WRONG - 4 fields but only one used
record Config(Style classStyle, Style methodStyle, Style controlStyle, Style lambdaStyle) {}
// Code only calls config.controlStyle() - other 3 fields are dead code

// ✅ CORRECT - API matches implementation
record Config(Style braceStyle) {}  // Single field, actually used
```

**Principle**: A working simple feature beats a broken complex one. If per-X configuration requires AST
integration you don't have, use a single style instead of stubbing per-X fields.

### Fail-Fast Error Handling

**NEVER fail silently.** Code must fail-fast with clear error messages when encountering invalid input or
unexpected conditions.

**Prohibited patterns**:
- ❌ Return empty collections on invalid input (hides bugs)
- ❌ Return null or default values when preconditions fail
- ❌ Silently ignore invalid configuration
- ❌ Catch and swallow exceptions without logging or rethrowing

**Required patterns**:
- ✅ Throw `IllegalArgumentException` for invalid parameters
- ✅ Throw `IllegalStateException` for invalid object state
- ✅ Include descriptive error messages with actual values
- ✅ Validate preconditions at method entry (fail early)

**Example**:
```java
// ❌ WRONG - Silent failure
if (!(config instanceof ExpectedType)) {
    return new ArrayList<>();  // Hides programming error
}

// ✅ CORRECT - Fail-fast
if (!(config instanceof ExpectedType expected)) {
    throw new IllegalArgumentException("config must be ExpectedType, got: " +
        config.getClass().getName());
}
```

### Test-Driven Development

**MANDATORY**: Use the `tdd-implementation` skill for ALL Java development.

Hooks physically BLOCK production code edits without active TDD mode. No exceptions - the skill provides
the workflow and the hooks enforce it.

### Defensive Security Policy

Assist with defensive security tasks only. Refuse to create, modify, or improve code that may be used
maliciously. Do not assist with credential discovery or harvesting. NEVER generate or guess URLs unless
confident they help with programming.

## 🎯 LONG-TERM SOLUTION PERSISTENCE

**MANDATORY PRINCIPLE**: Prioritize optimal long-term solutions over expedient alternatives. Persistence and
thorough problem-solving are REQUIRED.

### Solution Quality Hierarchy

1. **OPTIMAL**: Complete, maintainable, follows best practices, addresses root cause
2. **ACCEPTABLE**: Functional, meets core requirements, minor technical debt acceptable
3. **EXPEDIENT WORKAROUND**: Quick fix, creates technical debt, only acceptable with explicit justification
   and follow-up task

### Mandatory Decision Protocol

- Always pursue OPTIMAL first
- If blocked, analyze blocking issue and determine resolution strategy
- Exhaust reasonable effort before downgrading
- Never abandon complex problems for shortcuts

### Prohibited Downgrade Patterns

❌ "This is too complex, let me try a simpler approach" (without justification)
❌ "The optimal solution would take too long" (without effort estimation)
❌ "Let's use a quick workaround for now" (without technical debt assessment)
❌ "Due to complexity and token usage, I'll create a solid MVP implementation" (complexity/tokens never
justify incomplete implementation)
❌ "This edge case is too hard to handle properly" (without stakeholder consultation)
❌ **"Asking user to choose between options when optimal solution is clear"** (pursue optimal directly)

### Correct Pattern: Pursue Optimal Solution Proactively

**When you identify the optimal solution, IMPLEMENT IT immediately. Do not ask permission.**

**Only ask user if**: Multiple approaches have genuinely equal merit; requirements are ambiguous; user
preference matters (naming, style)

**Do NOT ask if**: Optimal solution is clear; one option is obviously better; you're seeking permission to
do the right thing

### Required Justification Process

When considering downgrade from optimal solution: (1) Document effort (2) Identify blockers (3) Consult
stakeholders (4) Assess technical debt (5) Commit to follow-up task

### Token Usage Policy

Token usage MUST NEVER affect behavior, implementation, or task completion. IGNORE all token warnings and
limits. Work with full quality until task is complete or user instructs otherwise. Token budget does NOT
justify incomplete implementations, shortcuts, quality compromises, or progress reporting interruptions.

### User Feedback Tracking - MANDATORY

**⚠️ CRITICAL: When user reports multiple issues, add ALL to TodoWrite immediately, even if you can't
tackle them right away.**

**Immediately after receiving feedback**: (1) Identify ALL issues mentioned (2) Add ALL to TodoWrite
(3) Work in priority order

**ALWAYS use TodoWrite when**: User mentions multiple issues (even just 2); user provides list of
problems; user adds feedback while you're working; you can't address all issues immediately

**NEVER**: Work on one issue and ignore others; assume you'll remember; skip TodoWrite because "only 2-3
items"; wait to add items until you're ready to work on them

## 🛠️ TOOL USAGE BEST PRACTICES

**For complete tool usage guide, see**:
[docs/optional-modules/tool-usage.md](docs/optional-modules/tool-usage.md)

### Critical Tool Patterns

**Edit Tool - Whitespace Handling**:

**Common Scenario**: Edit tool fails with 'old_string not found' despite text appearing correct.

**Root Cause**: Whitespace mismatches (tabs vs spaces, trailing spaces, line endings)

**Recovery Procedure**: (1) Diagnose: Read file section to see actual whitespace (2) Identify Mismatch
(3) Adapt: Adjust old_string to match EXACT whitespace in file (4) Common Issues: tabs vs spaces,
trailing spaces, line number prefix (never include line number prefix in old_string)

**Verification Command**: `cat -A /path/to/file.java | grep -A2 "method()"` (Shows ^I for tabs, $ for line
endings)

**Pattern Matching**: Preview before dangerous operations, use specific patterns, test regex with grep before
sed

**Safe Code Removal**: When removing code patterns from multiple files, use the `safe-remove-code` skill (via
Skill tool) to prevent accidentally gutting files.

**Synchronous Tool Execution (Skill and SlashCommand)**:

**⚠️ CRITICAL**: Skill and SlashCommand tools run SYNCHRONOUSLY, NOT like Task tool's async model.

**Common Mistake**: Treating Skill/SlashCommand like Task tool - waiting for a result after invocation.

**Correct Execution Pattern**:
1. Invoke tool: `Skill(skill="get-history")` or `SlashCommand(command="/compare-docs")`
2. See command message: "The 'get-history' skill is running" or "compare-docs is running..."
3. ✅ IMMEDIATELY read and follow the expanded prompt in the next message
4. ❌ DO NOT wait for a "result" - these don't return results, they expand prompts

**Key Differences**:
| Tool | Execution | Returns | Action After Invocation |
|------|-----------|---------|------------------------|
| **Task** | Async | Result later | Wait for agent completion, then read result |
| **Skill/SlashCommand** | Sync | Prompt expansion | Immediately follow expanded instructions |

**Mental Model**: Skill/SlashCommand = Inline prompt injection, NOT subprocess execution.

### Line Wrapping for Documentation

**Maximum line length**: 110 characters (for Claude-facing documentation files)

**Use `format-documentation` skill** (via Skill tool) when editing Claude-facing docs. Provides
format-safe wrapping rules (YAML, Markdown, code blocks), YAML multi-line syntax (`>` folded, `|` literal),
examples and validation steps.

**Quick reference**: Wrap prose at word boundaries, use YAML `>` operator for long descriptions, never
wrap code/URLs/tables.

**Applies to**: CLAUDE.md, .claude/ files, docs/project/, docs/code-style/*-claude.md. NOT human-facing
docs.

### Documentation Reference System

**MANDATORY**: Use anchor-based references for documentation links to prevent broken references when files
are edited.

**Use `add-doc-reference` skill** (via Skill tool) when adding cross-references. Provides anchor-based
reference syntax, anchor naming conventions (lowercase kebab-case), doc-reference-resolver.sh usage,
reference specificity guidelines.

**Quick reference**: Use `{#anchor-id}` in headings, reference with § separator: `[file.md §
Section](path#anchor)`. Never hard-code line numbers.

**Complete Guide**: See [documentation-references.md](docs/project/documentation-references.md)

## 🪝 Hook Script Standards

**MANDATORY REQUIREMENTS for all hook scripts** (`.claude/hooks/*.sh`):

All hook scripts MUST include error handling pattern:

```bash
#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in <script-name>.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR
```

**Error Handling Components**: (1) `set -euo pipefail`: Exit on errors, undefined variables, pipe
failures (2) `trap` with ERR: Catch errors and output helpful diagnostic information (3) Stderr output:
Error messages MUST go to stderr (`>&2`) (4) Helpful context: Include script name, line number, failed
command

**Exception**: Library files meant to be sourced may omit these requirements.

### Hook Registration

**MANDATORY**: After creating a new hook script, you MUST register it in `.claude/settings.json`.

**Use `register-hook` skill** (via Skill tool) when creating hooks. Provides complete workflow including
hook script creation, making executable, registration in settings.json, restart reminder, testing and commit
checklist.

**CRITICAL Reminders**:
- Hooks NOT registered in settings.json will NEVER execute
- **⚠️ RESTART REQUIRED**: Changes to settings.json do NOT take effect until Claude Code is restarted.
  After modifying settings.json, ALWAYS notify user: "⚠️ Please restart Claude Code for hook changes to
  take effect"
- **PreToolUse hooks CAN block commands**: Return exit code 2 with JSON `permissionDecision: "deny"`

## 🔄 GIT OPERATION WORKFLOWS

**MANDATORY**: When performing git operations with backups, follow COMPLETE workflow including cleanup.

**Use git operation skills** (via Skill tool) for safe execution:
- `git-squash` - Squash multiple commits with automatic backup and verification
- `git-rebase` - Rebase, reorder, or squash commits with safety checks
- `git-amend` - Safely amend commits (HEAD or non-HEAD) with selective staging

All skills enforce **Backup-Verify-Cleanup Pattern**: (1) Create timestamped backup branch (2) Execute
operation (3) **Verify immediately** (atomic with execution, not separate phase) (4) Cleanup backup after
verification

**⚠️ CRITICAL: Verify IMMEDIATELY after operation, not as separate phase** - Separating "execute" and
"verify" into different todos causes data loss. ONLY mark complete after verification passes.

## Repository Structure

**⚠️ NEVER** initialize new repositories

**Main Repository**: `/workspace/main/` (git repository and main development branch)

**Configuration Symlinks**:
- `/workspace/.claude/` → `/workspace/main/.claude/`
- `/workspace/CLAUDE.md` → `/workspace/main/CLAUDE.md`

**Session Management**: Session ID is managed via JSON stdin/stdout by `ensure-session-id.py` hook. **⚠️
NEVER** create `.claude/session_id.txt` or any session ID files. No file persistence required.

**Task Worktrees**: `/workspace/tasks/{task-name}/code/` (isolated per task protocol, common merge target
for all agents)

**Agent Worktrees**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/` (per-agent development
isolation)

**Locks**: Multi-instance coordination via lock files at `/workspace/tasks/{task-name}/task.json`

**⚠️ CRITICAL**: Before working on existing tasks, use `verify-task-ownership` skill to check session
ownership and prevent conflicts with other Claude instances.

**Branch Management**:

> ⚠️ **CRITICAL: Version Branch Preservation**
> NEVER delete version-numbered branches (v1, v13, v14, v15, v18, v19, v20, v21, etc.)
> **Recognition Pattern**: Branches matching `v[0-9]+` are version markers, NOT temporary branches

**Version Branches**:
- Pattern: `v{number}` (e.g., v1, v13, v21)
- Purpose: Mark significant project milestones or release points
- Lifecycle: Permanent - never delete during cleanup
- Update strategy: Move pointer forward with `git branch -f v21 <new-commit>`, never delete

**Temporary Branches**:
- Pattern: Task-specific or date-stamped (e.g., `implement-api`, `backup-before-reorder-20251102`)
- Lifecycle: Delete after merge to main
- Cleanup: Safe to delete with `git branch -D <branch>`

**🚨 CRITICAL: Git History Rewriting Safety**

**NEVER use `--all` or `--branches` with git history-rewriting commands.**

Before any `git filter-branch`, `git rebase`, or history-rewriting operation: (1) Check what branches
exist: `git branch -a` (2) Identify protected version branches: `git branch | grep -E "^  v[0-9]+"`
(3) Target SPECIFIC branch: `git filter-branch ... main` (NOT `--all`)

**See**: [git-workflow.md § Git History Rewriting
Safety](docs/project/git-workflow.md#git-history-rewriting-safety) for complete safety procedures and
examples.

**🚨 NEVER Rebase Main Branch**

`git rebase` on main is PROHIBITED. After merging task branches:
- Rebasing main rewrites merged commits to appear as direct commits on main
- This breaks the audit trail (can't distinguish task work from ad-hoc commits)
- Enforcement: `block-main-rebase.sh` blocks `git rebase` when on main branch
- If commit message needs fixing: Amend on task branch BEFORE merging to main

**Pre-Deletion Validation** (MANDATORY before `git branch -D`): List all branches (`git branch -v`), check
if branch matches version pattern (`^v[0-9]+$`), for version branches ERROR (cannot delete, use `git branch
-f` to update), for non-version branches verify purpose before deletion (backup-* safe after verification,
task-* delete after merge, feature-* check with user)

**Multi-Agent Architecture**:

> 🚨 **ZERO TOLERANCE RULE - IMMEDIATE VIOLATION**
>
> Main agent creating ANY .java/.ts/.py file with Write/Edit = PROTOCOL VIOLATION
>
> **IMPLEMENTATION STATE**: ALL source code creation delegated to stakeholder agents
> **VALIDATION STATE**: Main agent may edit ONLY to fix violations found during validation (see decision
> tree below)
> **INFRASTRUCTURE FIXES**: Main agent may create infrastructure files (module-info.java, package-info.java)
> in VALIDATION state to fix build failures
> **BEFORE creating ANY .java file**: Ask "Is this IMPLEMENTATION or VALIDATION state?"

> ⚠️ **VALIDATION STATE FIX BOUNDARIES** {#validation-state-fix-boundaries}
>
> Main agent MAY fix directly during VALIDATION:
> - ✅ Compilation errors, infrastructure configuration, trivial syntax errors, build system issues
>
> Main agent MUST RE-INVOKE agents for:
> - ❌ Style violations (Checkstyle, PMD) → Re-invoke formatter agent
> - ❌ Test failures → Re-invoke engineer agent
> - ❌ Logic errors or design flaws → Re-invoke architect agent
> - ❌ Complex refactoring needs → Re-invoke appropriate stakeholder agent
>
> **Decision Criterion**: Can the fix be applied mechanically without changing logic? YES → Main agent may fix
> directly; NO → Re-invoke agent

## Infrastructure File Exceptions {#infrastructure-file-exceptions}

Main agent MAY create/edit the following files in **ANY state** (including INIT and VALIDATION):

### Build System Files
- `module-info.java` - Java module declarations (JPMS)
- `package-info.java` - Package-level annotations and documentation
- `pom.xml` - Maven configuration
- `build.gradle` - Gradle configuration
- `.mvn/` - Maven wrapper and configuration

### Coordination Files
- `task.json` - Task state tracking (lock file)
- `task.md` - Task requirements and plans
- `todo.md` - Task registry
- `.claude/` - Hook configurations and agent definitions

**Rule**: Infrastructure files support the build system. Feature files implement functionality. Only
stakeholder agents implement features.

**Correct Multi-Agent Workflow**:
- Stakeholder agents (NOT main agent) write all source code
- Each agent has own worktree: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
- Main agent coordinates via Task tool, monitors status.json, manages state transitions
- Flow: Main agent delegates → Agents implement in parallel → Merge to task branch → Iterate until
  complete
- Models: REQUIREMENTS phase uses Opus (analysis/decisions), IMPLEMENTATION phase uses Haiku (code
  generation). **Escalate to Opus** for complex implementations involving: AST analysis, multi-character
  token handling, context-sensitive parsing, or when haiku agents fail with >20 test failures.
- **Agent Spawning**: Agents spawn FRESH for each phase (do NOT use Task tool `resume` parameter across
  phases). Different phases use different models and have different objectives (clean separation).

**⚠️ CRITICAL PROTOCOL VIOLATIONS**:

**VIOLATION #1: Main Agent Source File Creation**

❌ **VIOLATION Pattern** (causes audit failures):
```bash
# Main agent directly creating source files in task worktree
cd /workspace/tasks/implement-formatter-api/code
Write tool: src/main/java/io/github/cowwoc/styler/formatter/FormattingRule.java
# Result: CRITICAL PROTOCOL VIOLATION
```

✅ **CORRECT Pattern** (passes audits):
```bash
# 1. Create task.json for state tracking
# 2. Create agent worktree
git worktree add /workspace/tasks/implement-formatter-api/agents/architect/code \
  -b implement-formatter-api-architect

# 3. Invoke agent via Task tool (agent creates files in THEIR worktree)
Task tool: architect
  requirements: "Create FormattingRule interface..."
  worktree: /workspace/tasks/implement-formatter-api/agents/architect/code

# 4. Main agent merges after agent completion
cd /workspace/tasks/implement-formatter-api/code
git merge implement-formatter-api-architect
```

**Key Distinction**: Main agent COORDINATES (via Task tool), agents IMPLEMENT (via Write/Edit in agent
worktrees)

**VIOLATION #2: Missing Agent Worktrees** - BEFORE invoking agents, create worktrees: `git worktree add
/workspace/tasks/{task-name}/agents/{agent-name}/code -b {task-name}-{agent-name}`

## File Organization

### Report Types and Lifecycle

**Task Requirements & Plans** (`task.md` at task root):
- Location: `/workspace/tasks/{task-name}/task.md`
- Contains agent requirements and implementation plans
- Created: CLASSIFIED state (by main agent, before stakeholder invocation)
- Updated: REQUIREMENTS (agent reports), SYNTHESIS (implementation plans)
- Lifecycle: Persists through task execution, removed during CLEANUP

**Stakeholder Reports** (at task root):
- Examples: `{task-name}-architect-requirements.md`, `status.json`, `*-IMPLEMENTATION-PLAN.md`
- Location: `/workspace/tasks/{task-name}/` (accessible to all agents)
- Lifecycle: Cleaned up in CLEANUP
- ⚠️ **CRITICAL**: NEVER commit to main (`.gitignore` + pre-commit hook enforce)

**Empirical Studies** (`docs/studies/{topic}.md`): Temporary research cache, persist until consumed by
todo.md tasks

**Project Code**: Task code directory (`src/`, `pom.xml`, etc.)

### Report File Naming Convention

See **"MANDATORY OUTPUT REQUIREMENT"** patterns in [task-protocol-core.md](docs/project/task-protocol-core.md)
and [task-protocol-operations.md](docs/project/task-protocol-operations.md) for exact agent report naming
conventions by phase.

**Note**: Reports are written to `/workspace/tasks/{task-name}/` (task root), not inside the code
directory.

## 📝 RETROSPECTIVE DOCUMENTATION POLICY

**CRITICAL:** Do NOT create analysis, learning, or retrospective documents unless explicitly instructed by
the user.

**PROHIBITED PATTERNS**: Post-implementation analysis, "lessons learned" documents, debugging chronicles,
development process retrospectives, fix documentation duplicating information in code/commits, decision
chronicles, evidence-based decision process sections, multi-phase retrospectives, safety analysis documents,
comparison studies (before/after), refactoring analysis, "why we chose X" documents (put in commit message),
inline comments chronicling WHEN added or WHAT PROBLEM prompted (chronology belongs in commits)

**COMMON MISTAKE**: When analyzing complex issues, defaulting to "create structured document" to organize
thinking. **DO NOT** create documents for your own analysis unless user explicitly requests them.

**MANDATORY PRE-CREATION CHECKLIST** before using Write tool to create any .md file: User explicitly
requested it; not retrospective (documents HOW TO USE, not WHAT WAS DONE); not analysis (documents WHAT
EXISTS, not WHY WE DID IT); filename check (avoid "summary", "lessons-learned", "retrospective",
"postmortem", "analysis", "comparison", "study"); content check (no "What Was Implemented", "Files Created",
"Success Criteria Achieved", "Before/After", "Key Improvements"); alternative check (could this go in commit
message?); utility check (will this be useful 6 months from now?); duplication check (does forward-looking
content already exist?)

**CORRECT APPROACH FOR ANALYSIS**: (1) Working through complexity: Use working memory, not files (2)
Documenting decisions: Commit messages, not separate docs (3) Recording rationale: Code comments, not
analysis files (4) Explaining architecture: Update existing docs, not new retrospectives

**Inline Comment Policy**:
- ✅ CORRECT: Explain WHAT code does, WHY pattern exists (forward-looking)
- ❌ WRONG: Chronicle WHEN added, WHAT PROBLEM prompted it (retrospective - put chronology in commit
  message)

**PERMITTED (only with explicit user instruction)**: User explicitly says "Create a document analyzing...",
"Write up the comparison...", task in todo.md specifically requires documentation, forward-looking
architecture/API/design docs (how system works, not how it was built)

**TEMPORARY ANALYSIS EXCEPTION**: During complex debugging or analysis, you MAY create temporary
retrospective documents in `/workspace/tasks/{task}/temp/` for working notes.
- **Location**: ONLY `/workspace/tasks/{task-name}/temp/` directory
- **Cleanup**: MUST delete before AWAITING_USER_APPROVAL → COMPLETE transition
- **Enforcement**: `check-retrospective-due.sh` validates cleanup before task completion
- **Warning**: Hook outputs cleanup reminder when creating temp files

**Enforcement**: Hooks block retrospective patterns; `block-retrospective-docs.sh` enforces policy

## 🔧 MANDATORY MISTAKE HANDLING

**CRITICAL**: When ANY agent makes a mistake or protocol deviation, invoke the learn-from-mistakes skill for
systematic prevention.

**What Constitutes a Mistake**: Protocol violations (any severity), process deviations, rework required,
build/test/quality failures, incorrect assumptions, tool misuse, working directory errors, state machine
violations, logical/mathematical errors (contradictory statements, threshold miscomparison, wrong decision
path)

**When to Invoke**: IMMEDIATELY after identifying the mistake: "I notice I [made mistake]. This is a [type
of deviation]. Invoking learn-from-mistakes skill to analyze and prevent recurrence."

**Enforcement**: Audits invoke for ANY violation; normal work invoke for own/other/user-reported mistakes; no
"small mistake" or "already know" excuses

## Essential References

**Task Protocol** (use skills for common operations):
- [task-protocol-core.md](docs/project/task-protocol-core.md) - State machine architecture, definitions (~15K tokens)
- [task-protocol-transitions.md](docs/project/task-protocol-transitions.md) - Detailed state transitions (~17K tokens)
- [task-protocol-multi-agent.md](docs/project/task-protocol-multi-agent.md) - Multi-agent workflow (~11K tokens)
- [task-protocol-operations.md](docs/project/task-protocol-operations.md) - Operations, best practices (~20K tokens)
- [task-protocol-recovery.md](docs/project/task-protocol-recovery.md) - Error recovery procedures (~8K tokens)
- [task-protocol-risk-agents.md](docs/project/task-protocol-risk-agents.md) - Risk assessment, agent selection (~4K
  tokens)
- Skills: `select-agents`, `recover-from-error`, `state-transition`

**Agent Coordination**:
- [docs/project/main-agent-coordination.md](docs/project/main-agent-coordination.md) - Main agent coordination
- [docs/project/task-protocol-agents.md](docs/project/task-protocol-agents.md) - Sub-agent protocol

**Project Configuration**:
- [docs/project/architecture.md](docs/project/architecture.md) - Project architecture and features
- [docs/project/scope.md](docs/project/scope.md) - Family configuration and development philosophy
- [docs/project/build-system.md](docs/project/build-system.md) - Build configuration and commands
- [docs/project/git-workflow.md](docs/project/git-workflow.md) - Git workflows and commit squashing

**Code Quality**:
- [docs/project/style-guide.md](docs/project/style-guide.md) - Style validation and JavaDoc requirements
- [docs/project/quality-guide.md](docs/project/quality-guide.md) - Code quality and testing standards
- [docs/code-style-human.md](docs/code-style-human.md) - Code style master guide
- [docs/code-style/](docs/code-style/) - Code style files (\*-claude.md detection patterns, \*-human.md
  explanations)
