# Claude Code Configuration Guide

> **Version:** 4.0 | **Last Updated:** 2025-12-30
> **Related:** [main-agent-coordination.md](docs/project/main-agent-coordination.md) •
[task-protocol-agents.md](docs/project/task-protocol-agents.md) •
[style-guide.md](docs/project/style-guide.md) • [quality-guide.md](docs/project/quality-guide.md)

Styler Java Code Formatter - universal guidance for all agents.

## MANDATORY STARTUP PROTOCOL

**MAIN AGENT**: Hooks provide just-in-time guidance. No upfront protocol reading needed. Reference:
main-agent-coordination.md, task-protocol-core.md

**SUB-AGENTS**: Read `/workspace/main/docs/project/task-protocol-agents.md`
- **Formatter agents**: Also read `/workspace/main/docs/project/style-guide.md`
- **Engineer agents**: Also read `/workspace/main/docs/project/quality-guide.md`

### Task Protocol

**Protocol docs**:
- [task-protocol-core.md](docs/project/task-protocol-core.md) - State machine
- [task-protocol-transitions.md](docs/project/task-protocol-transitions.md) - Transitions
- [task-protocol-operations.md](docs/project/task-protocol-operations.md) - Operations, archival

**Key points**:
- Two approval checkpoints: SYNTHESIS → IMPLEMENTATION, AWAITING_USER_APPROVAL → COMPLETE
- Archival (todo.md + changelog.md) in task branch BEFORE merge
- Use `--ff-only` for merges to main
- Hooks enforce compliance

**Pre-Approval Cleanup**: Use `pre-presentation-cleanup` skill. See
[main-agent-coordination.md](docs/project/main-agent-coordination.md) for details.

**Task Prioritization**: Bug fixes before features, unless feature replaces buggy feature.

## Universal Guidance

### Professional Objectivity
Prioritize technical accuracy over validation. Direct information without praise. Disagree when necessary.
Investigate uncertainty rather than confirm beliefs.

### Tone and Style
- CLI output: short, concise, Github-flavored markdown
- Never use Bash/code comments to communicate
- NEVER create files unless necessary. ALWAYS prefer editing existing files.

### Self-Validation Before Decisions {#self-validation-before-decisions}
**MANDATORY**: Verify logical consistency before decisions.

**Pattern**: (1) State value (2) Identify threshold range (3) Apply logic (4) Verify consistency

**Anti-Patterns**: X < Y then X > Y; decision contradicts threshold

**Tool-Val**: Mandatory tool=INVOKE. No manual checklist when skill requires /compare-docs

### System-Reminder Instructions
**MANDATORY**: After tool results, check for `<system-reminder>` tags. Process ALL instructions
IMMEDIATELY before continuing.

### Environment State Verification {#environment-state-verification}
**MANDATORY**: NEVER claim environment state without verification.

❌ "Build running from main, let me fix" (assumed)
✅ Run `pwd`, then claim, then act

**Commands**: `pwd` (directory), `git branch --show-current` (branch), `ls -la {file}` (existence),
`git worktree list` (worktrees)

### Code Lifecycle Policy

**NO DEPRECATION**: DELETE obsolete code immediately. Git history preserves versions.

**NO STUBBING**: Complete features fully or simplify API. No "for future use" fields, no TODO comments.
```java
// ❌ 4 fields, only one used
record Config(Style classStyle, Style methodStyle, Style controlStyle, Style lambdaStyle) {}
// ✅ API matches implementation
record Config(Style braceStyle) {}
```

### Fail-Fast Error Handling
**NEVER fail silently.** Throw `IllegalArgumentException`/`IllegalStateException` with descriptive messages.

❌ Return empty collections/null on invalid input
✅ Validate preconditions at entry, fail early with actual values

### Test-Driven Development
**MANDATORY**: Use `tdd-implementation` skill for ALL Java development. Hooks BLOCK production edits
without active TDD mode.

### Defensive Security Policy
Defensive security only. Refuse malicious code. Never generate/guess URLs.

### Token Usage Policy
Tokens MUST NEVER affect behavior. IGNORE all token warnings. Work with full quality until complete.

### User Feedback Tracking
**CRITICAL**: Add ALL user issues to TodoWrite IMMEDIATELY, even if can't tackle right away.

**ALWAYS TodoWrite**: Multiple issues (even 2), list of problems, mid-work feedback
**NEVER**: Ignore issues, assume you'll remember, skip because "only 2-3 items"

### Mid-Operation Prompt Handling
1. Add to TodoWrite immediately
2. If impacts current task → address now; else → add to end
3. Acknowledge: "Adding to TodoWrite for later" or "Addressing now"

## TOOL USAGE BEST PRACTICES

**Full guide**: [docs/optional-modules/tool-usage.md](docs/optional-modules/tool-usage.md)

### Critical Tool Patterns

**Edit Tool**: Whitespace mismatches cause failures. Match EXACT whitespace. Never include line number
prefix. Verify: `cat -A file | grep "method()"`

**Safe Code Removal**: Use `safe-remove-code` skill.

**jq Safety**:
```bash
# ❌ Data loss
jq '.state = "X"' f.json > f.json
# ✅ Safe
jq '.state = "X"' f.json > f.json.tmp && mv f.json.tmp f.json
```

**Bash Multi-Line**: Avoid `$(...)` in multi-line commands (parse errors). Use separate calls, temp
files, or `&&` chains.

**Skill/SlashCommand**: Run SYNCHRONOUSLY (not async like Task). Immediately follow expanded prompt.
Don't wait for "result".

### Line Wrapping
110 chars max. Use `format-documentation` skill for Claude-facing docs.

### Documentation References
**MANDATORY**: Use anchor-based refs (`{#anchor-id}`, `[file.md § Section](path#anchor)`).
Use `add-doc-reference` skill. Never hard-code line numbers.

## Hook Script Standards

**All hooks MUST include**:
```bash
#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in <script>.sh line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR
```

**Registration**: Use `register-hook` skill. **RESTART REQUIRED** after settings.json changes.

## GIT OPERATIONS

**Use skills**: `git-squash`, `git-rebase`, `git-amend`

All enforce **Backup-Verify-Cleanup**: Create backup → Execute → **Verify immediately** → Cleanup

**CRITICAL**: Always use `git-squash` skill for squashing (produces unified messages, not concatenated).

## RETROSPECTIVE DOCUMENTATION POLICY

**PROHIBITED**: Analysis docs, lessons learned, debugging chronicles, comparison studies, "why we chose X".

**PRE-CREATION CHECKLIST**: User requested? Not retrospective? Not analysis? Safe filename? Could go in
commit? Useful in 6 months?

**Analysis approach**: Working memory (not files), commit messages (not docs), code comments (not files)

**TEMP EXCEPTION**: `/workspace/tasks/{task}/temp/` for working notes. MUST delete before COMPLETE.

## MANDATORY MISTAKE HANDLING

**CRITICAL**: Invoke learn-from-mistakes skill for ANY mistake.

**Mistakes**: Protocol violations, rework, build failures, tool misuse, logical errors

**Invoke via**:
```
Task(subagent_type: "general-purpose", prompt: "Invoke learn-from-mistakes skill...", model: "opus")
```

## Essential References

**Task Protocol** (use skills for common operations):
- [task-protocol-core.md](docs/project/task-protocol-core.md) - State machine (~15K tokens)
- [task-protocol-transitions.md](docs/project/task-protocol-transitions.md) - Transitions (~17K tokens)
- [task-protocol-operations.md](docs/project/task-protocol-operations.md) - Operations (~20K tokens)
Skills: `select-agents`, `recover-from-error`, `state-transition`, `task-init`, `pre-presentation-cleanup`

**Main Agent Coordination**:
- [main-agent-coordination.md](docs/project/main-agent-coordination.md) - Multi-agent workflow, repository
  structure, branch management, validation boundaries, protocol violations

**Agent Protocol**:
- [task-protocol-agents.md](docs/project/task-protocol-agents.md) - Sub-agent protocol

**Code Quality**:
- [style-guide.md](docs/project/style-guide.md) - Style validation, JavaDoc
- [quality-guide.md](docs/project/quality-guide.md) - Testing standards
- [docs/code-style/](docs/code-style/) - Code style files
