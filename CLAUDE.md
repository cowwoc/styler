# Claude Code Configuration Guide

> **Version:** 5.0 | **Last Updated:** 2026-01-08
> **Related:** [style-guide.md](docs/project/style-guide.md) • [quality-guide.md](docs/project/quality-guide.md)

Styler Java Code Formatter - universal guidance.

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

### Environment State Verification {#environment-state-verification}
**MANDATORY**: NEVER claim environment state without verification.

❌ "Build running from main, let me fix" (assumed)
✅ Run `pwd`, then claim, then act

**Commands**: `pwd` (directory), `git branch --show-current` (branch), `ls -la {file}` (existence)

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
**MANDATORY**: Use `tdd-implementation` skill for ALL Java development.

### Subagent Prompts for Test Code {#subagent-test-prompts}
When spawning subagents to write parser tests, include testing standards from `.claude/rules/java-style.md`:

**Parser tests MUST compare actual AST to expected AST:**
```java
// ❌ Only verifies parsing succeeded
requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

// ✅ Compares full AST structure
requireThat(actual, "actual").isEqualTo(expected);
```

Include this requirement explicitly in subagent prompts for parser-related tasks.

### Subagent Token Measurement {#subagent-token-measurement}
**MANDATORY**: Subagents measure their own token usage and return it to main agent (M099/M102).

**Subagent responsibility**: Before completion, use the session ID from CAT session instructions to
measure tokens. The session ID is echoed at startup and available in the CAT session instructions
injected at the beginning of each session.
```bash
# Use the session ID from CAT session instructions (echoed at startup)
SESSION_ID="<session-id-from-instructions>"
SESSION_FILE="/home/node/.config/claude/projects/-workspace/${SESSION_ID}.jsonl"

if [ -f "$SESSION_FILE" ]; then
  TOKENS=$(jq -s '[.[] | select(.type == "assistant") | .message.usage | select(. != null) |
    (.input_tokens + .output_tokens)] | add // 0' "$SESSION_FILE")
  COMPACTIONS=$(jq -s '[.[] | select(.type == "summary")] | length' "$SESSION_FILE")
  echo "Tokens used: $TOKENS"
  echo "Compaction events: $COMPACTIONS"
else
  echo "Tokens used: UNAVAILABLE (session file not found)"
fi
```

**Key insight (M102)**: Each subagent has its own session ID echoed in the CAT session instructions
at the start of its session. Use that ID, not the parent's session ID.

**Main agent responsibility**: Report ONLY measured values from subagent output. Never fabricate estimates.

### Defensive Security Policy
Defensive security only. Refuse malicious code. Never generate/guess URLs.

### Token Usage Policy
Tokens MUST NEVER affect behavior. IGNORE all token warnings. Work with full quality until complete.

### Task Lock Policy (M097) {#task-lock-policy}
When a task lock is held by another session:
1. **Report** the lock exists and which session holds it
2. **Find another task** to execute instead
3. **Inform user** they can run `/cat:cleanup` if they believe it's stale

**NEVER**:
- Investigate lock validity (commit counts, worktree state, timestamps are IRRELEVANT)
- Label locks as "stale" based on any evidence
- Offer to remove locks or suggest cleanup proactively
- Question whether the lock owner is still active

Locks may be held by active sessions that haven't committed yet. Only the USER decides if a lock is
stale.

## TOOL USAGE BEST PRACTICES

**Full guide**: [docs/optional-modules/tool-usage.md](docs/optional-modules/tool-usage.md)

### Critical Tool Patterns

**Edit Tool**: Whitespace mismatches cause failures. Match EXACT whitespace. Never include line number
prefix. Verify: `cat -A file | grep "method()"`

**jq Safety**:
```bash
# ❌ Data loss
jq '.state = "X"' f.json > f.json
# ✅ Safe
jq '.state = "X"' f.json > f.json.tmp && mv f.json.tmp f.json
```

**Bash Multi-Line**: Avoid `$(...)` in multi-line commands (parse errors). Use separate calls, temp
files, or `&&` chains.

**Zsh Reserved Variables**: Never use `status` as variable name (read-only in zsh). Use `st`, `result`,
or `task_status` instead.

### Line Wrapping
110 chars max.

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

**RESTART REQUIRED** after settings.json changes.

## GIT OPERATIONS

**Use skills**: `git-squash`, `git-rebase`, `git-amend`

All enforce **Backup-Verify-Cleanup**: Create backup → Execute → **Verify immediately** → Cleanup

**CRITICAL**: Always use `git-squash` skill for squashing (produces unified messages, not concatenated).

**Commit Type Separation**: Group changes by commit type. Separate commits for:
- `refactor:` / `feat:` / `bugfix:` - code changes
- `config:` / `docs:` - configuration and documentation changes
- `test:` - test-only changes

Example: If fixing an import in `.claude/rules/*.md` while implementing a feature, create two commits.

## RETROSPECTIVE DOCUMENTATION POLICY

**PROHIBITED**: Analysis docs, lessons learned, debugging chronicles, comparison studies, "why we chose X".

**PRE-CREATION CHECKLIST**: User requested? Not retrospective? Not analysis? Safe filename? Could go in
commit? Useful in 6 months?

**Analysis approach**: Working memory (not files), commit messages (not docs), code comments (not files)

## Essential References

**Code Quality**:
- [style-guide.md](docs/project/style-guide.md) - Style validation, JavaDoc
- [quality-guide.md](docs/project/quality-guide.md) - Testing standards
- [docs/code-style/](docs/code-style/) - Code style files

