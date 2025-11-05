# Claude Code Configuration Guide

> **Version:** 3.0 | **Last Updated:** 2025-10-18
> **Related Documents:** [main-agent-coordination.md](docs/project/main-agent-coordination.md) •
[task-protocol-agents.md](docs/project/task-protocol-agents.md) •
[style-guide.md](docs/project/style-guide.md) • [quality-guide.md](docs/project/quality-guide.md)

Styler Java Code Formatter project configuration and universal guidance for all agents.

## 🚨 MANDATORY STARTUP PROTOCOL

**MAIN AGENT**: Task protocol uses just-in-time guidance via hooks. You do NOT need to read protocol files upfront.
- Phase-specific instructions provided automatically as you transition states
- Hooks will direct you to read specific sections of documentation when needed
- Start tasks by following hook guidance, not by reading complete protocol docs
- Reference docs available for troubleshooting: main-agent-coordination.md, task-protocol-core.md

### ⚠️ MANDATORY USER APPROVAL CHECKPOINTS

**CRITICAL**: Two checkpoints require EXPLICIT user approval before proceeding.

**🚨 STATE MACHINE PROGRESSION IS MANDATORY**

User approval checkpoints are tied to task.json state transitions. You MUST progress through the state machine - hooks only enforce protocol when states are used correctly.

**State Workflow**:
```
INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → [USER APPROVAL] → IMPLEMENTATION → VALIDATION → AWAITING_USER_APPROVAL → [USER APPROVAL] → COMPLETE
         ↓              ↓              ↓                              ↓                             ↓
    Create task.md  Gather reqs   Plan impl      Invoke agents    Build/test              Merge to main
```

**CRITICAL: REQUIREMENTS Phase is MANDATORY**

❌ **WRONG**: Main agent writes implementation plan directly from todo.md
✅ **CORRECT**: Invoke stakeholder agents → Read reports → Synthesize plan

**Required stakeholder agents** (invoke in parallel for requirements gathering):
- `architect` - Analyzes dependencies, design patterns, integration points
- `tester` - Analyzes test coverage needs, test strategy, edge cases, business logic validation
- `formatter` - Specifies documentation requirements, code style standards

**⚠️ COMMON MISTAKE**: Do NOT invoke "engineer" for testing requirements
- `engineer` = Code quality, refactoring, duplication (NOT testing)
- `tester` = Test strategy, test coverage, edge cases (use this for testing)

**Workflow**:
1. CLASSIFIED state: **MANDATORY** - Invoke ALL stakeholder agents in REQUIREMENTS mode with parallel coordination

   **⚠️ CLARIFICATION: "Parallel" Agent Invocation Patterns**

   **PREFERRED** (Best Practice - True Parallel):
   - Single message with 3 Task tool calls (architect, tester, formatter)
   - Example: One assistant message containing three `<invoke name="Task">` blocks
   - This is the optimal pattern for maximum parallelization

   **ACCEPTABLE** (Achieves Coordination Goal):
   - Rapid succession within same conversation turn (all within <30 seconds)
   - No user messages between invocations
   - Example: Three consecutive Task calls at 17:00:14, 17:00:23, 17:00:32 (18-second spread)
   - **Key criterion**: All within same conversation turn, no user interruption

   **VIOLATION** (Prevents Coordination):
   - Sequential calls across multiple user interactions
   - User interruption between agent invocations
   - Example: Task call, user response, Task call, user response

   **Clarification on Timing**:
   - Focus on avoiding user interruptions, not microsecond timing
   - 6-30 second gaps within same turn = ACCEPTABLE (achieves coordination)
   - >30 second delays suggest sequential work rather than parallel coordination
   - Evidence: Session 3fa4e964 had 17-31 second spreads, marked COMPLIANT

   **Rationale**: The goal is rapid concurrent requirements gathering to avoid multi-hour sequential delays. Both PREFERRED and ACCEPTABLE patterns achieve this outcome by ensuring all agents start work concurrently.

   - **CRITICAL**: Prompts MUST specify output file: `/workspace/tasks/{task}/{task}-{agent}-requirements.md`
   - **CRITICAL**: Prompts MUST emphasize: "You are in REQUIREMENTS mode. ONLY write requirements report. DO NOT implement code."
2. Wait for completion: Each writes `{task-name}-{agent}-requirements.md`
3. **VERIFY reports exist** (MANDATORY before SYNTHESIS):
   ```bash
   ls -la /workspace/tasks/{task}/*-requirements.md
   # Must show all 3 files: architect-requirements.md, tester-requirements.md, formatter-requirements.md
   ```
4. READ all reports: Main agent synthesizes into unified plan
5. SYNTHESIS state: Write implementation plan to task.md
6. Get user approval
7. IMPLEMENTATION state: **MANDATORY** - Invoke ALL stakeholder agents in IMPLEMENTATION mode with parallel coordination
   - Follow same patterns as REQUIREMENTS phase (PREFERRED: single message, ACCEPTABLE: rapid succession <10s)
   - All agents working concurrently reduces implementation time from days to hours

**Checkpoint 1: SYNTHESIS → IMPLEMENTATION** (Plan Approval)
   - **SYNTHESIS state**: Create implementation plan in task.md
   - **STOP and PRESENT**: Show plan to user
   - **WAIT for user approval**: User must say "approved", "proceed", "looks good"
   - **ONLY THEN**: Create `/workspace/tasks/{task}/user-approved-synthesis.flag`
   - **Transition to IMPLEMENTATION**: `jq '.state = "IMPLEMENTATION"' task.json`
   - **NOW you can invoke Task tool**: Agents work in IMPLEMENTATION state
   - Hook will BLOCK Task tool invocations from INIT state
   - Hook will BLOCK IMPLEMENTATION transition without approval flag

**Checkpoint 2: AWAITING_USER_APPROVAL → COMPLETE** (Change Review)
   - **AWAITING_USER_APPROVAL state**: After validation passes
   - **STOP and PRESENT**: Show commit SHA and `git diff --stat main...task-branch`
   - **WAIT for user approval**: User must say "approved", "merge it", "LGTM"
   - **ONLY THEN**: Create `/workspace/tasks/{task}/user-approved-changes.flag`
   - **Transition to COMPLETE**: `jq '.state = "COMPLETE"' task.json`
   - **NOW squash and merge to main**:
     ```bash
     cd /workspace/main
     git merge --squash {task-branch}
     # Update todo.md and changelog.md
     git add -A
     git commit -m "Task implementation

     - Implementation details
     - Updated todo.md: Mark task complete
     - Updated changelog.md: Document changes

     🤖 Generated with Claude Code
     Co-Authored-By: Claude <noreply@anthropic.com>"
     ```
   - **CRITICAL**: Use `--squash` to create ONE commit (not multiple)
   - Hook will BLOCK merges to main without approval flag
   - Hook will BLOCK merges from wrong working directory

**Final Step: COMPLETE → CLEANUP** (Clean Repository)
   - **After merge to main**: Execute CLEANUP to delete branches and worktrees
   - **Transition to CLEANUP**: `jq '.state = "CLEANUP"' task.json`
   - **Delete all task branches**:
     ```bash
     cd /workspace/main
     git branch -D {task-name} {task-name}-architect {task-name}-engineer {task-name}-formatter
     ```
   - **Remove all worktrees**:
     ```bash
     git worktree remove /workspace/tasks/{task-name}/code
     git worktree remove --force /workspace/tasks/{task-name}/agents/*/code
     ```
   - **Verify cleanup**: `git branch | grep {task-name}` should return nothing
   - **Preserves**: Task directory with audit files (task.json, task.md, approval flags)
   - **Why**: Prevents branch accumulation, reclaims disk space, maintains clean repository
   - **Reference**: See [task-protocol-core.md § COMPLETE → CLEANUP Transition](docs/project/task-protocol-core.md#complete-cleanup-transition)

**NEVER**:
- ❌ Skip state progression (staying in INIT throughout task)
- ❌ Invoke Task tool before transitioning to CLASSIFIED/IMPLEMENTATION
- ❌ Proceed to IMPLEMENTATION without presenting plan
- ❌ Merge to main without presenting changes
- ❌ Assume silence or bypass mode means approval
- ❌ Skip checkpoints because "plan is straightforward"
- ❌ Use `git merge` without `--squash` when merging task to main (creates multiple commits instead of one)

**Enforcement**:
- `pre-tool-use-task-invoke.sh` blocks Task tool from INIT state
- `pre-tool-use-task-invoke.sh` blocks SYNTHESIS/IMPLEMENTATION states without requirements reports
- `enforce-merge-workflow.sh` validates merge location, approval, and squash requirement
- `enforce-checkpoints.sh` validates state transitions and approval flags

**Why State Machine Matters**:
- Hooks validate based on task.json state
- Bypassing states disables protocol enforcement
- User approval checkpoints are state-transition-dependent

**SUB-AGENTS**: If you are a stakeholder agent (architect, engineer, formatter), this file contains universal guidance only. You MUST also read `/workspace/main/docs/project/task-protocol-agents.md`

**Domain-Specific Agents**: Additionally read domain-specific guides:
- **Formatter agents** (formatter): `Read /workspace/main/docs/project/style-guide.md`
- **Engineer agents** (engineer, tester): `Read /workspace/main/docs/project/quality-guide.md`

## Universal Guidance

This file contains guidance applicable to **ALL agents** (main agent and all sub-agents).

### Professional Objectivity

Prioritize technical accuracy over validating user beliefs. Provide direct, objective information without superlatives, praise, or emotional validation. Apply rigorous standards to all ideas and disagree when necessary. Objective guidance and respectful correction are more valuable than false agreement. When uncertain, investigate first rather than confirming user beliefs.

### Tone and Style

- Output displays on CLI. Keep responses short and concise. Use Github-flavored markdown (CommonMark specification).
- Output text to communicate with user; all text outside tool use is displayed. Use tools only to complete tasks. Never use Bash or code comments to communicate.
- NEVER create files unless absolutely necessary. ALWAYS prefer editing existing files.

### Defensive Security Policy

**IMPORTANT**: Assist with defensive security tasks only. Refuse to create, modify, or improve code that may be used maliciously. Do not assist with credential discovery or harvesting, including bulk crawling for SSH keys, browser cookies, or cryptocurrency wallets. Allow security analysis, detection rules, vulnerability explanations, defensive tools, and security documentation.

**IMPORTANT**: NEVER generate or guess URLs unless confident they help with programming. Use only URLs provided by user in messages or local files.

## 🎯 LONG-TERM SOLUTION PERSISTENCE

**MANDATORY PRINCIPLE**: Prioritize optimal long-term solutions over expedient alternatives. Persistence and thorough problem-solving are REQUIRED.

### Solution Quality Hierarchy

1. **OPTIMAL**: Complete, maintainable, follows best practices, addresses root cause
2. **ACCEPTABLE**: Functional, meets core requirements, minor technical debt acceptable
3. **EXPEDIENT WORKAROUND**: Quick fix, creates technical debt, only acceptable with explicit justification and follow-up task

### Mandatory Decision Protocol

- Always pursue OPTIMAL first
- If blocked, analyze blocking issue and determine resolution strategy
- Exhaust reasonable effort before downgrading
- Never abandon complex problems for shortcuts

### Prohibited Downgrade Patterns

❌ "This is too complex, let me try a simpler approach" (without justification)
❌ "The optimal solution would take too long" (without effort estimation)
❌ "Let's use a quick workaround for now" (without technical debt assessment)
❌ "I'll implement the minimum viable solution" (when requirements specify comprehensive solution)
❌ "Due to complexity and token usage, I'll create a solid MVP implementation" (complexity/tokens never justify incomplete implementation)
❌ "Given token constraints, I'll implement a basic version" (token budget does not override quality requirements)
❌ "This edge case is too hard to handle properly" (without stakeholder consultation)
❌ "The existing pattern is suboptimal but I'll follow it" (without improvement attempt)
❌ **"Asking user to choose between options when optimal solution is clear"** (pursue optimal directly)
❌ **"Presenting Option A (optimal), Option B (suboptimal), Option C (workaround) - which do you prefer?"** (just do Option A)

### Correct Pattern: Pursue Optimal Solution Proactively

**When you identify the optimal solution, IMPLEMENT IT immediately. Do not ask permission.**

✅ **CORRECT**:
```
Tests need rework to use actual types from ast-core. Fixing them now...

[Proceeds to fix tests with Edit tool]
```

❌ **WRONG**:
```
Tests need rework. How would you like to proceed?
- Option A: Fix the tests now
- Option B: Merge as-is
- Option C: Remove failing tests
```

**Only ask user if**:
- Multiple approaches have genuinely equal merit (architectural decision)
- Requirements are ambiguous and you need clarification
- User preference matters (e.g., naming conventions, style choices)

**Do NOT ask if**:
- Optimal solution is clear (just implement it)
- One option is obviously better than others
- You're seeking permission to do the right thing

### Required Justification Process

When considering downgrade from optimal solution:

1. Document effort: "Attempted optimal solution for X hours/attempts"
2. Identify blockers: "Specific technical obstacles: [list]"
3. Consult stakeholders: "Consulting domain authorities for guidance"
4. Assess technical debt: "Proposed workaround creates debt in areas: [list]"
5. Commit to follow-up: "Created todo.md task for proper solution: [task-name]"

### Token Usage Policy

Token usage MUST NEVER affect behavior, implementation, or task completion. IGNORE all token warnings and limits. Work with full quality until task is complete or user instructs otherwise. Token budget does NOT justify incomplete implementations, shortcuts, quality compromises, or progress reporting interruptions.

## 🛠️ TOOL USAGE BEST PRACTICES

**For complete tool usage guide, see**:
[docs/optional-modules/tool-usage.md](docs/optional-modules/tool-usage.md)

### Critical Tool Patterns

**Edit Tool - Whitespace Handling**:

**Common Scenario**: Edit tool fails with 'old_string not found' despite text appearing correct.

**Root Cause**: Whitespace mismatches (tabs vs spaces, trailing spaces, line endings)

**Recovery Procedure**:
1. **Diagnose**: Read file section to see actual whitespace characters
2. **Identify Mismatch**: Compare visible text vs actual indentation
3. **Adapt**: Adjust old_string to match EXACT whitespace in file
4. **Common Issues**:
   - Tabs vs spaces: Check file uses consistent indentation
   - Trailing spaces: Include or exclude from old_string to match
   - Line number prefix in Read output: Never include line number prefix in old_string

**Prevention**: When reading file before Edit, note indentation style (tabs vs spaces) and preserve it exactly in old_string.

**Example Failure and Fix**:
```bash
# Read output shows (note the tab character after line number):
15→	public void method() {

# ❌ FAILS: old_string uses spaces but file has tab
Edit: old_string="    public void method()" # 4 spaces - doesn't match

# ✅ CORRECT: Use tab to match file
Edit: old_string="	public void method()" # 1 tab character - matches file

# Note: Line number "15→" and tab separator are NOT part of file content
# Only text AFTER the separator tab is the actual file content
```

**Verification Command**:
```bash
# If Edit fails, verify exact whitespace in file
cat -A /path/to/file.java | grep -A2 "method()"
# Shows ^I for tabs, $ for line endings, · for spaces
```

**Bash Tool - Path Handling**:

- Always quote file paths that contain spaces with double quotes
- Use absolute paths or combine `cd` with command (e.g., `cd /path && command`)
- Try to maintain your current working directory throughout the session by using absolute paths and avoiding usage of `cd`

**Pattern Matching**:
- Preview before replacing with dangerous operations
- Use specific patterns to avoid unintended matches
- Test regex patterns with grep before using in sed

### Documentation Reference System

**MANDATORY**: Use anchor-based references for documentation links to prevent broken references when files are edited.

**✅ CORRECT - Anchor-Based References**:
```bash
# In hooks and scripts
source .claude/hooks/lib/doc-reference-resolver.sh
DOC_REF=$(resolve_doc_ref "task-protocol-core.md#init-classified")
echo "📖 Read: $DOC_REF"
# Output: Read /workspace/main/docs/project/task-protocol-core.md lines 1590-1634
```

**❌ INCORRECT - Hard-Coded Line Numbers**:
```bash
# NEVER do this - breaks when documentation changes
echo "Read /workspace/main/docs/project/task-protocol-core.md lines 1583-1626"
```

**Adding Anchors to Documentation**:
```markdown
## Section Title {#anchor-id}
```

**Anchor Naming**: Use lowercase kebab-case that matches heading semantics: `{#lock-ownership}`, `{#init-classified}`

**Reference Specificity**: Always reference specific documentation sections with anchors instead of vague file-level references.
- ✅ CORRECT: "See [main-agent-coordination.md § Post-Implementation Issue Handling](docs/project/main-agent-coordination.md#post-implementation-issue-handling-decision-tree)"
- ❌ INCORRECT: "Refer to CLAUDE.md for state-based fix permissions" (too vague, no specific section)
- ❌ INCORRECT: "See main-agent-coordination.md" (file-level only, no section specified)

**System Maintenance**:
- Index auto-regenerates on commit (pre-commit hook)
- Manual: `./.claude/scripts/generate-doc-index.sh`

**Complete Guide**: See [documentation-references.md](docs/project/documentation-references.md)

## 🪝 Hook Script Standards

**MANDATORY REQUIREMENTS for all hook scripts** (`.claude/hooks/*.sh`):

All hook scripts MUST include the following error handling pattern:

```bash
#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in <script-name>.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Rest of script...
```

**Error Handling Components**:

1. **`set -euo pipefail`**: Exit on errors, undefined variables, and pipe failures
2. **`trap` with ERR**: Catch errors and output helpful diagnostic information
3. **Stderr output**: Error messages MUST go to stderr (`>&2`) for proper hook error reporting
4. **Helpful context**: Include script name, line number, and failed command in error messages

**Exception**: Library files meant to be sourced (not executed directly) may omit these requirements.

### Hook Registration

**MANDATORY**: After creating a new hook script, you MUST register it in `.claude/settings.json`.

**Registration Checklist**:
1. ✅ Create hook script in `.claude/hooks/`
2. ✅ Make hook executable: `chmod +x .claude/hooks/my-hook.sh`
3. ✅ **Register in `.claude/settings.json`** under appropriate trigger event
4. ✅ Test hook triggers correctly
5. ✅ Commit both hook script AND settings.json update

**Common Trigger Events**:
- `SessionStart` - Runs when session starts or resumes after compaction
- `UserPromptSubmit` - Runs when user submits a prompt
- `PreToolUse` - Runs before tool execution (supports matchers)
- `PostToolUse` - Runs after tool execution (supports matchers)
- `PreCompact` - Runs before context compaction

**Example Registration**:
```json
"UserPromptSubmit": [
  {
    "hooks": [
      {
        "type": "command",
        "command": "/workspace/.claude/hooks/my-new-hook.sh"
      }
    ]
  }
]
```

**CRITICAL**: Hooks NOT registered in settings.json will NEVER execute, even if the script exists and is executable.

## Repository Structure

**⚠️ NEVER** initialize new repositories

**Main Repository**: `/workspace/main/` (git repository and main development branch)

**Configuration Symlinks**:
- `/workspace/.claude/` → `/workspace/main/.claude/` (shared hook and agent configurations)
- `/workspace/CLAUDE.md` → `/workspace/main/CLAUDE.md` (shared project instructions)

**Session Management**:
- Session ID is managed via JSON stdin/stdout by `ensure-session-id.py` hook
- **⚠️ NEVER** create `.claude/session_id.txt` or any session ID files
- Session ID flows: Claude Code → hook stdin → hook stdout → context injection
- No file persistence required for session ID tracking

**Task Worktrees**: `/workspace/tasks/{task-name}/code/` (isolated per task protocol, common merge target for all agents)

**Agent Worktrees**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/` (per-agent development isolation)

**Locks**: Multi-instance coordination via lock files at `/workspace/tasks/{task-name}/task.json`

**Branch Management**:

> ⚠️ **CRITICAL: Version Branch Preservation**
>
> **NEVER delete version-numbered branches (v1, v13, v14, v15, v18, v19, v20, v21, etc.)**
>
> **Common Mistake**: Deleting v-branches during git cleanup operations
>
> **Recognition Pattern**: Branches matching `v[0-9]+` are version markers, NOT temporary branches

**Version Branches**:
- Pattern: `v{number}` (e.g., v1, v13, v21)
- Purpose: Mark significant project milestones or release points
- Lifecycle: Permanent - never delete during cleanup
- Update strategy: Move pointer forward with `git branch -f v21 <new-commit>`, never delete

**Temporary Branches**:
- Pattern: Task-specific or date-stamped (e.g., `implement-api`, `backup-before-reorder-20251102`)
- Purpose: Isolate development work
- Lifecycle: Delete after merge to main
- Cleanup: Safe to delete with `git branch -D <branch>`

**Pre-Deletion Validation** (MANDATORY before `git branch -D`):
```bash
# Step 1: List all branches to identify patterns
git branch -v

# Step 2: Check if branch matches version pattern
if [[ "$BRANCH_NAME" =~ ^v[0-9]+$ ]]; then
  echo "❌ ERROR: Cannot delete version branch $BRANCH_NAME"
  echo "Version branches are permanent project markers"
  echo "To update: git branch -f $BRANCH_NAME <new-commit>"
  exit 1
fi

# Step 3: For non-version branches, verify purpose before deletion
# - backup-* branches: Temporary, safe to delete after verification
# - task-* branches: Delete only after merge to main
# - feature-* branches: Check with user before deletion
```

**Examples**:

✅ **CORRECT - Updating Version Branch**:
```bash
# After rebase/squash, update version branch to new commit
git branch -f v21 HEAD
# Version branch now points to clean history
```

❌ **WRONG - Deleting Version Branch**:
```bash
# DON'T delete version branches during cleanup
git branch -D v21  # ← MISTAKE: Lost project milestone marker
```

✅ **CORRECT - Deleting Temporary Branch**:
```bash
# Temporary backup branch can be deleted after verification
git branch -D backup-before-reorder-20251102-001057
```

**Multi-Agent Architecture**:

> 🚨 **ZERO TOLERANCE RULE - IMMEDIATE VIOLATION**
>
> Main agent creating ANY .java/.ts/.py file with Write/Edit = PROTOCOL VIOLATION
>
> **IMPLEMENTATION STATE**: ALL source code creation delegated to stakeholder agents
> **VALIDATION STATE**: Main agent may edit ONLY to fix violations found during validation (see decision tree below)
> **INFRASTRUCTURE FIXES**: Main agent may create infrastructure files (module-info.java, package-info.java) in VALIDATION state to fix build failures
> **BEFORE creating ANY .java file**: Ask "Is this IMPLEMENTATION or VALIDATION state?"

> ⚠️ **VALIDATION STATE FIX BOUNDARIES** {#validation-state-fix-boundaries}
>
> Main agent MAY fix directly during VALIDATION:
> - ✅ **Compilation errors**: Missing imports, incorrect package paths, type resolution failures
> - ✅ **Infrastructure configuration**: module-info.java, pom.xml, build.gradle
> - ✅ **Trivial syntax errors**: Missing semicolons, typos in identifiers
> - ✅ **Build system issues**: Missing dependencies, incorrect artifact versions
>
> Main agent MUST RE-INVOKE agents for:
> - ❌ **Style violations** (Checkstyle, PMD) → Re-invoke formatter agent
> - ❌ **Test failures** → Re-invoke engineer agent
> - ❌ **Logic errors or design flaws** → Re-invoke architect agent
> - ❌ **Complex refactoring needs** → Re-invoke appropriate stakeholder agent
>
> **Decision Criterion**: Can the fix be applied mechanically without changing logic?
> - YES → Main agent may fix directly
> - NO → Re-invoke agent

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

### IMPORTANT DISTINCTION

✅ **CORRECT**: Infrastructure fix during VALIDATION
```bash
# Build fails due to missing module export
Edit: formatter/src/main/java/module-info.java
  Add: exports io.github.cowwoc.styler.formatter;
```

❌ **VIOLATION**: Feature implementation during IMPLEMENTATION
```bash
# Creating business logic - WRONG STATE
Write: formatter/src/main/java/FormattingRule.java
  [Implements feature logic - should be done by architect]
```

✅ **CORRECT**: Infrastructure setup during INIT
```bash
# Preparing module structure before agent invocation
Write: formatter/src/main/java/module-info.java
  [Declares module, requires, exports]
```

**Rule**: Infrastructure files support the build system. Feature files implement functionality. Only stakeholder agents implement features.

**Correct Multi-Agent Workflow**:
- Stakeholder agents (NOT main agent) write all source code
- Each agent has own worktree: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
- Main agent coordinates via Task tool, monitors status.json, manages state transitions
- Flow: Main agent delegates → Agents implement in parallel → Merge to task branch → Iterate until complete
- Models: REQUIREMENTS phase uses Sonnet 4.5 (analysis/decisions), IMPLEMENTATION phase uses Haiku 4.5 (code generation)
- **Agent Spawning**: Agents spawn FRESH for each phase (do NOT use Task tool `resume` parameter across phases)
  - REQUIREMENTS phase: New agent instances for requirements gathering
  - IMPLEMENTATION phase: New agent instances for implementation
  - Rationale: Different phases use different models and have different objectives (clean separation)

**⚠️ CRITICAL PROTOCOL VIOLATIONS**:

**VIOLATION #1: Main Agent Source File Creation**

❌ **VIOLATION Pattern** (causes audit failures):
```bash
# Main agent directly creating source files in task worktree - WRONG
cd /workspace/tasks/implement-formatter-api/code
Write tool: src/main/java/io/github/cowwoc/styler/formatter/FormattingRule.java
# Result: CRITICAL PROTOCOL VIOLATION
```

✅ **CORRECT Pattern** (passes audits):
```bash
# 1. Create task.json for state tracking
cat > /workspace/tasks/implement-formatter-api/task.json <<EOF
{
  "task_name": "implement-formatter-api",
  "state": "IMPLEMENTATION",
  "created": "$(date -Iseconds)"
}
EOF

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

**Key Distinction**: Main agent COORDINATES (via Task tool), agents IMPLEMENT (via Write/Edit in agent worktrees)

**VIOLATION #2: Missing Agent Worktrees**
- Requirement: BEFORE invoking stakeholder agents, main agent MUST create agent worktrees
- Command: `git worktree add /workspace/tasks/{task-name}/agents/{agent-name}/code -b {task-name}-{agent-name}`
- Enforcement: Pre-tool-use hook blocks source file creation without task.json

## Essential References

[docs/project/main-agent-coordination.md](docs/project/main-agent-coordination.md) - Main agent task protocol and coordination
[docs/project/task-protocol-agents.md](docs/project/task-protocol-agents.md) - Sub-agent coordination protocol
[docs/project/task-protocol-core.md](docs/project/task-protocol-core.md) - Complete state machine (main agent)
[docs/project/task-protocol-operations.md](docs/project/task-protocol-operations.md) - Operational patterns (main agent)
[docs/project/architecture.md](docs/project/architecture.md) - Project architecture and features
[docs/project/scope.md](docs/project/scope.md) - Family configuration and development philosophy
[docs/project/build-system.md](docs/project/build-system.md) - Build configuration and commands
[docs/project/git-workflow.md](docs/project/git-workflow.md) - Git workflows and commit squashing procedures
[docs/project/style-guide.md](docs/project/style-guide.md) - Style validation and JavaDoc requirements
[docs/project/quality-guide.md](docs/project/quality-guide.md) - Code quality and testing standards
[docs/code-style-human.md](docs/code-style-human.md) - Code style master guide
[docs/code-style/](docs/code-style/) - Code style files (\*-claude.md detection patterns, \*-human.md explanations)

## File Organization

### Report Types and Lifecycle

**Task Requirements & Plans** (`task.md` at task root):
- Location: `/workspace/tasks/{task-name}/task.md`
- Contains agent requirements and implementation plans
- Created: CLASSIFIED state (by main agent, before stakeholder invocation)
- Updated: REQUIREMENTS (agent reports), SYNTHESIS (implementation plans)
- Lifecycle: Persists through task execution, removed during CLEANUP

**Stakeholder Reports** (at task root, one level up from code directory):
- Temporary workflow artifacts for task protocol
- Examples: `{task-name}-architect-requirements.md`, `{task-name}-formatter-violations.json`, `status.json`, `*-IMPLEMENTATION-PLAN.md`
- Lifecycle: Created during execution, cleaned up with worktrees in CLEANUP
- Location: `/workspace/tasks/{task-name}/` (accessible to all agents)
- ⚠️ **CRITICAL**: Task artifacts must NEVER be committed to main repository
  - `.gitignore` excludes these files automatically
  - Pre-commit hook blocks accidental commits
  - Keep artifacts in `/workspace/tasks/{task}/` NOT `/workspace/main/`

**Empirical Studies** (`docs/studies/{topic}.md`):
- Temporary research cache for pending implementation tasks
- Examples: `docs/studies/claude-cli-interface.md`, `docs/studies/claude-startup-sequence.md`
- Lifecycle: Persist until all dependent todo.md tasks consume them, then remove

**Project Code**: Task code directory (`src/`, `pom.xml`, etc.)

### Report File Naming Convention

See **"MANDATORY OUTPUT REQUIREMENT"** patterns in [task-protocol-core.md](docs/project/task-protocol-core.md) and [task-protocol-operations.md](docs/project/task-protocol-operations.md) for exact agent report naming conventions by phase.

**Note**: Reports are written to `/workspace/tasks/{task-name}/` (task root), not inside the code directory.

## 📝 RETROSPECTIVE DOCUMENTATION POLICY

Do NOT create retrospective documentation chronicling fixes, problems, or development process.

**PROHIBITED PATTERNS**:
❌ Post-implementation analysis reports
❌ "Lessons learned" documents
❌ Debugging chronicles or problem-solving narratives
❌ Development process retrospectives
❌ Fix documentation duplicating information in code/commits
❌ **Decision chronicles** documenting past decision-making phases
❌ Documents with "Evidence-Based Decision Process" sections
❌ Multi-phase retrospectives ("Phase 1: Requirements", "Phase 2: Evidence", etc.)
❌ **Safety analysis documents** chronicling what went wrong and how to fix it

**COMMON MISTAKE PATTERN**:
When analyzing complex issues, you may default to "create structured document" to organize thinking.
STOP and ask BEFORE creating any .md file:
1. "Is this documenting what happened (retrospective) or what to do (forward-looking)?"
2. "Could this go in a commit message instead?" (yes → put it there, not in file)
3. "Could this go in inline comments instead?" (yes → put it there, not in file)
4. "Will this be useful after the fix, or only during?" (only during → don't create file)

**Example of Mistake** (SAFETY-ANALYSIS.md created 2025-11-02):
- **Trigger**: Thought "Given the complexity and length of fixing both skills completely, let me create a summary document"
- **Root cause**: Defaulted to "create structured document" when task felt complex
- **Violation**: Used complexity as excuse to avoid doing work directly (violates Token Usage Policy)
- Documented "what went wrong" in git skills instead of just fixing them
- Should have gone in: commit message + inline comments in skills
- File was deleted immediately after fixes applied - proof it was retrospective
- **Lesson**: Complexity NEVER justifies creating intermediate retrospective documents

**SPECIFIC ANTI-PATTERNS**:
```markdown
❌ BAD - Retrospective Decision Chronicle:
# Final Decision: Arena API Adoption

## Evidence-Based Decision Process
### Phase 1: Stakeholder Requirements
- Technical-Architect initially recommended...
### Phase 2: JMH Benchmark Evidence
Successfully executed benchmarks revealing...
### Phase 3: Stakeholder Validation
- Technical-Architect: ✅ APPROVED...
```

```markdown
✅ GOOD - Forward-Looking Architecture:
# Parser Memory Management

## Design Choice: Arena API

**Rationale**: Provides 3x performance improvement and meets
512MB target with 96.9% safety margin (benchmarked on JDK 25).

**Implementation**:
[Shows HOW to use it going forward]
```

**WHERE TO DOCUMENT**:
- Rationale: Git commit message with the change
- Why this approach: Code comments inline with implementation
- Benchmark results: Reference in architecture.md design section
- Alternatives considered: Brief note in code comment
- **Lessons learned**: Inline comments in hook/config files explaining pattern evolution

**PERMITTED** (only when explicitly required):
✅ Task or user explicitly requires specific documentation
✅ Forward-looking architecture documentation
✅ API documentation and user guides
✅ Technical design documents for upcoming features

**ENFORCEMENT**:
- Pre-commit hook blocks retrospective file creation
- Pre-tool-use hook detects retrospective content patterns

## 🔧 MANDATORY MISTAKE HANDLING

**CRITICAL**: When ANY agent (including main agent) makes a mistake or protocol deviation, you MUST invoke the learn-from-mistakes skill for systematic prevention.

### What Constitutes a Mistake

**Mistakes include** (invoke learn-from-mistakes for ANY of these):
- Protocol violations (any severity: CRITICAL, HIGH, MEDIUM, or LOW)
- Process deviations (incorrect commit patterns, missing steps)
- Rework required (had to redo work due to error)
- Build failures (compilation, test, or quality gate failures)
- Incorrect assumptions (led to wrong implementation)
- Tool misuse (wrong tool, incorrect parameters, validation gaps)
- Working directory errors (wrong worktree, incorrect paths)
- State machine violations (skipped states, wrong sequence)

**Examples**:
- ❌ Split commits (todo.md separate from implementation) → INVOKE learn-from-mistakes
- ❌ Main agent creates source files during IMPLEMENTATION → INVOKE learn-from-mistakes
- ❌ Checkstyle violations found late → INVOKE learn-from-mistakes
- ❌ Agent worked in wrong worktree → INVOKE learn-from-mistakes
- ❌ Missing user approval checkpoint → INVOKE learn-from-mistakes

### When to Invoke

**IMMEDIATELY after identifying the mistake**:

```markdown
# ❌ WRONG Pattern:
"I notice I split the commits incorrectly. Let me just note that for next time."

# ✅ CORRECT Pattern:
"I notice I split the commits incorrectly. This is a protocol deviation.
Invoking learn-from-mistakes skill to analyze and prevent recurrence."

Skill: learn-from-mistakes
Context: Split commit pattern - todo.md updated in separate commit instead
of atomic commit with implementation.
```

### Why This is Mandatory

**Prevents recurrence**:
- One-time acknowledgment → Likely to repeat
- Systematic analysis → Configuration updates prevent recurrence
- Root cause fixes → Entire category of mistakes prevented

**Examples of systematic prevention**:
- Hook added → Blocks mistake automatically
- Documentation clarified → Clear guidance prevents confusion
- Examples added → Shows correct pattern explicitly
- Validation enhanced → Catches mistake earlier

### Enforcement

**During audits**:
- Audit identifies protocol deviations
- Audit MUST invoke learn-from-mistakes for ANY violation
- No exceptions for "minor" or "low severity" violations

**During normal work**:
- Agent recognizes own mistake → MUST invoke learn-from-mistakes
- Agent recognizes other agent's mistake → MUST invoke learn-from-mistakes
- User points out mistake → MUST invoke learn-from-mistakes

**Bypass NOT permitted**:
- ❌ "This is a small mistake, not worth invoking skill"
- ❌ "I already know what went wrong, no need for analysis"
- ❌ "This mistake is obvious, I'll just fix it"
- ✅ "Mistake identified → Invoke learn-from-mistakes → Systematic prevention"

### Integration with Audit

**Audit pipeline automatically invokes learn-from-mistakes**:
- Protocol compliance audit detects violations
- IF any violations found (any severity) → Invoke learn-from-mistakes
- Root cause analysis identifies systemic issues
- Configuration updates prevent recurrence

**Manual invocation required**:
- When mistake discovered outside audit
- When recognizing pattern of repeated mistakes
- When user points out deviation

---

**For agent-specific guidance, see the documents listed in the Mandatory Startup Protocol section above.**
