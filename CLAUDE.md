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

**SUB-AGENTS**: If you are a sub-agent (reviewer or updater), this file contains universal guidance only. You MUST also read:
```
Read /workspace/main/docs/project/task-protocol-agents.md
```

**Domain-Specific Agents**: Additionally read domain-specific guides:
- **Style agents** (style-reviewer, style-updater): `Read /workspace/main/docs/project/style-guide.md`
- **Quality agents** (quality-reviewer, quality-updater, test-reviewer, test-updater): `Read /workspace/main/docs/project/quality-guide.md`

## Universal Guidance

This file contains guidance applicable to **ALL agents** (main agent and all sub-agents).

### Professional Objectivity

Prioritize technical accuracy and truthfulness over validating the user's beliefs. Focus on facts and problem-solving, providing direct, objective technical info without any unnecessary superlatives, praise, or emotional validation. It is best for the user if Claude honestly applies the same rigorous standards to all ideas and disagrees when necessary, even if it may not be what the user wants to hear. Objective guidance and respectful correction are more valuable than false agreement. Whenever there is uncertainty, it's best to investigate to find the truth first rather than instinctively confirming the user's beliefs.

### Tone and Style

- Only use emojis if the user explicitly requests it. Avoid using emojis in all communication unless asked.
- Your output will be displayed on a command line interface. Your responses should be short and concise. You can use Github-flavored markdown for formatting, and will be rendered in a monospace font using the CommonMark specification.
- Output text to communicate with the user; all text you output outside of tool use is displayed to the user. Only use tools to complete tasks. Never use tools like Bash or code comments as means to communicate with the user during the session.
- NEVER create files unless they're absolutely necessary for achieving your goal. ALWAYS prefer editing an existing file to creating a new one. This includes markdown files.

### Defensive Security Policy

**IMPORTANT**: Assist with defensive security tasks only. Refuse to create, modify, or improve code that may be used maliciously. Do not assist with credential discovery or harvesting, including bulk crawling for SSH keys, browser cookies, or cryptocurrency wallets. Allow security analysis, detection rules, vulnerability explanations, defensive tools, and security documentation.

**IMPORTANT**: You must NEVER generate or guess URLs for the user unless you are confident that the URLs are for helping the user with programming. You may use URLs provided by the user in their messages or local files.

## 🎯 LONG-TERM SOLUTION PERSISTENCE

**MANDATORY PRINCIPLE**: Prioritize optimal long-term solutions over expedient alternatives. Persistence and thorough problem-solving are REQUIRED.

### Solution Quality Hierarchy

1. **OPTIMAL SOLUTION**: Complete, maintainable, follows best practices, addresses root cause
2. **ACCEPTABLE SOLUTION**: Functional, meets core requirements, minor technical debt acceptable
3. **EXPEDIENT WORKAROUND**: Quick fix, creates technical debt, only acceptable with explicit justification and follow-up task

### Mandatory Decision Protocol

- **FIRST ATTEMPT**: Always pursue the OPTIMAL SOLUTION approach
- **IF BLOCKED**: Analyze the blocking issue and determine resolution strategy
- **BEFORE DOWNGRADING**: Must exhaust reasonable effort toward optimal solution
- **NEVER ABANDON**: Complex problems require persistence, not shortcuts

### Prohibited Downgrade Patterns

**ANTI-PATTERNS - ABSOLUTELY FORBIDDEN**:
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

1. **DOCUMENT EFFORT**: "Attempted optimal solution for X hours/attempts"
2. **IDENTIFY BLOCKERS**: "Specific technical obstacles: [list]"
3. **STAKEHOLDER CONSULTATION**: "Consulting domain authorities for guidance"
4. **TECHNICAL DEBT ASSESSMENT**: "Proposed workaround creates debt in areas: [list]"
5. **FOLLOW-UP COMMITMENT**: "Created todo.md task for proper solution: [task-name]"

### Token Usage Policy

**CRITICAL TOKEN USAGE**: Token usage MUST NEVER affect behavior, implementation approach, or task completion. IGNORE all token usage warnings and limits. Continue working with full quality and completeness until task is 100% complete or explicit user instruction. Token budget does NOT justify incomplete implementations, shortcuts, or quality compromises.

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

## Repository Structure

**⚠️ NEVER** initialize new repositories

**Main Repository**: `/workspace/main/` (git repository and main development branch)

**Task Worktrees**: `/workspace/tasks/{task-name}/code/` (isolated per task protocol, common merge target for all agents)

**Agent Worktrees**: `/workspace/tasks/{task-name}/agents/{agent-name}/code/` (per-agent development isolation)

**Locks**: Multi-instance coordination via lock files at `/workspace/tasks/{task-name}/task.json`

**Multi-Agent Architecture**:
- **WHO IMPLEMENTS**: Stakeholder agents (NOT main agent) write all source code
- **WHERE**: Each stakeholder agent has own worktree: `/workspace/tasks/{task-name}/agents/{agent-name}/code/`
- **MAIN AGENT ROLE**: Coordinates via Task tool invocations, monitors status.json, manages state transitions
- **IMPLEMENTATION FLOW**: Main agent delegates → Agents implement in parallel → Agents merge to task branch → Iterative rounds until complete
- **VIOLATION**: Main agent creating .java/.ts/.py files directly in task worktree during IMPLEMENTATION state
- **MODEL STRATEGY**: Reviewer agents use Sonnet 4.5 (analysis/decisions), updater agents use Haiku 4.5 (mechanical implementation) - see task-protocol-core.md "Model Selection Strategy" for rationale

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
- Contains all agent requirements and implementation plans
- **Created**: During CLASSIFIED state (by main agent, BEFORE stakeholder agent invocation)
- **Updated**: During REQUIREMENTS (agent reports added), SYNTHESIS (implementation plans added)
- **Lifecycle**: Persists through entire task execution, removed during CLEANUP state

**Stakeholder Reports** (at task root, one level up from code directory):
- Temporary workflow artifacts for task protocol
- Examples: `{task-name}-architecture-reviewer-requirements.md`, `{task-name}-style-reviewer-violations.json`
- **Lifecycle**: Created during task execution, cleaned up with worktrees in CLEANUP state
- **Purpose**: Process documentation for protocol compliance
- **Location**: `/workspace/tasks/{task-name}/` (task root, accessible to all agents)

**Empirical Studies** (`docs/studies/{topic}.md`):
- Temporary research cache for pending implementation tasks
- Examples: `docs/studies/claude-cli-interface.md`, `docs/studies/claude-startup-sequence.md`
- **Lifecycle**: Persist until ALL dependent todo.md tasks consume them as input
- **Purpose**: Behavioral analysis and research studies based on empirical testing
- **Cleanup Rule**: Remove after all dependent tasks complete implementation

**Project Code**: Task code directory (`src/`, `pom.xml`, etc.)

### Report File Naming Convention

See **"MANDATORY OUTPUT REQUIREMENT"** patterns in [task-protocol-core.md](docs/project/task-protocol-core.md) and [task-protocol-operations.md](docs/project/task-protocol-operations.md) for exact agent report naming conventions by phase.

**Note**: Reports are written to `/workspace/tasks/{task-name}/` (task root), not inside the code directory.

## 📝 RETROSPECTIVE DOCUMENTATION POLICY

**CRITICAL**: Do NOT create retrospective documentation files that chronicle fixes, problems, or development process.

**PROHIBITED DOCUMENTATION PATTERNS**:
❌ Post-implementation analysis reports (e.g., `protocol-violation-prevention.md`)
❌ "Lessons learned" documents chronicling what went wrong and how it was fixed
❌ Debugging chronicles or problem-solving narratives
❌ Development process retrospectives or meta-documentation
❌ Fix documentation that duplicates information already in code/commits

**PERMITTED DOCUMENTATION** (only when explicitly required):
✅ Task explicitly requires documentation creation
✅ User explicitly requests specific documentation
✅ Forward-looking architecture documentation
✅ API documentation and user guides
✅ Technical design documents for upcoming features

**EXAMPLES**:

**PROHIBITED**:
```
docs/project/protocol-violation-prevention.md - "Analysis of violations and fixes"
docs/debugging/parallel-processing-issues.md - "How we debugged concurrency"
docs/lessons/picocli-reflection-removal.md - "Story of migrating to programmatic API"
```

**PERMITTED** (with explicit requirement):
```
docs/project/architecture.md - Forward-looking system design
docs/api/file-processor.md - API documentation for users
README.md - User-facing project documentation
```

**ENFORCEMENT**: Before creating any `.md` file in `/docs/`, verify it serves future users/developers rather than documenting the past.

---

**For agent-specific guidance, see the documents listed in the Mandatory Startup Protocol section above.**
