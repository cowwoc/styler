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

**CRITICAL**: Two checkpoints require EXPLICIT user approval before proceeding:

1. **SYNTHESIS → IMPLEMENTATION** (Plan Approval)
   - **STOP after SYNTHESIS**: Present implementation plan in task.md
   - **WAIT for user approval**: User must say "approved", "proceed", "looks good"
   - **ONLY THEN**: Create approval flag and transition to IMPLEMENTATION
   - Hook will BLOCK transitions without approval flag

2. **AWAITING_USER_APPROVAL → COMPLETE** (Change Review)
   - **STOP after REVIEW**: Present commit SHA and changes (git diff --stat)
   - **WAIT for user approval**: User must say "approved", "merge it", "LGTM"
   - **ONLY THEN**: Create approval flag and transition to COMPLETE
   - Hook will BLOCK transitions without approval flag

**NEVER**:
- ❌ Proceed to IMPLEMENTATION without presenting plan
- ❌ Merge to main without presenting changes
- ❌ Assume silence or bypass mode means approval
- ❌ Skip checkpoints because "plan is straightforward"

**Enforcement**: `enforce-checkpoints.sh` hook automatically reverts state and blocks violations

**SUB-AGENTS**: If you are a sub-agent (reviewer or updater), this file contains universal guidance only. You MUST also read `/workspace/main/docs/project/task-protocol-agents.md`

**Domain-Specific Agents**: Additionally read domain-specific guides:
- **Style agents** (style-reviewer, style-updater): `Read /workspace/main/docs/project/style-guide.md`
- **Quality agents** (quality-reviewer, quality-updater, test-reviewer, test-updater): `Read /workspace/main/docs/project/quality-guide.md`

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
- Manual: `./scripts/generate-doc-index.sh`
- Validate: `./scripts/find-hardcoded-references.sh`
- Migration help: `./scripts/suggest-anchor-migration.sh`

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

**Multi-Agent Architecture**:

> 🚨 **ZERO TOLERANCE RULE - IMMEDIATE VIOLATION**
>
> Main agent creating ANY .java/.ts/.py file with Write/Edit = PROTOCOL VIOLATION
>
> **IMPLEMENTATION STATE**: ALL source code creation delegated to stakeholder agents
> **VALIDATION STATE**: Main agent may edit ONLY to fix violations found during validation
> **INFRASTRUCTURE FIXES**: Main agent may create infrastructure files (module-info.java, package-info.java) in VALIDATION state to fix build failures
> **BEFORE creating ANY .java file**: Ask "Is this IMPLEMENTATION or VALIDATION state?"

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
  [Implements feature logic - should be done by architecture-updater]
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
- Models: Reviewers use Sonnet 4.5 (analysis/decisions), updaters use Haiku 4.5 (implementation)

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
git worktree add /workspace/tasks/implement-formatter-api/agents/architecture-updater/code \
  -b implement-formatter-api-architecture-updater

# 3. Invoke agent via Task tool (agent creates files in THEIR worktree)
Task tool: architecture-updater
  requirements: "Create FormattingRule interface..."
  worktree: /workspace/tasks/implement-formatter-api/agents/architecture-updater/code

# 4. Main agent merges after agent completion
cd /workspace/tasks/implement-formatter-api/code
git merge implement-formatter-api-architecture-updater
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
- Examples: `{task-name}-architecture-reviewer-requirements.md`, `{task-name}-style-reviewer-violations.json`
- Lifecycle: Created during execution, cleaned up with worktrees in CLEANUP
- Location: `/workspace/tasks/{task-name}/` (accessible to all agents)

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
- Pre-commit hook warns about retrospective patterns
- Detection script: `./scripts/detect-retrospective-docs.sh`
- Manual scan before creating `.md` files in `/docs/`

---

**For agent-specific guidance, see the documents listed in the Mandatory Startup Protocol section above.**
