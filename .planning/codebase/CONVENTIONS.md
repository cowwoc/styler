# Coding Conventions

**Analysis Date:** 2026-01-08
**Language:** Java 21

## Quick Reference

| Category | Key Rules | Details |
|----------|-----------|---------|
| Naming | PascalCase types, camelCase methods | conventions/java.md |
| Formatting | 4-space, 120 chars, Allman braces | conventions/java.md |
| Documentation | JavaDoc for public API | conventions/java.md |
| Error Handling | Fail-fast, requireThat() | conventions/java.md |
| Validation | requireThat() library | conventions/java.md |
| Policies | Comments, TODOs, code lifecycle | conventions/policies.md |
| Common | Language-agnostic patterns | conventions/common.md |
| Maven | Build configuration rules | conventions/maven.md |
| Git | Commit types, branching | [below](#git-conventions) |

## Naming Patterns

**Files:** PascalCase.java matching class name
**Types:** PascalCase for classes, interfaces, records, enums
**Methods:** camelCase, no abbreviations (getLineNumber not getLineNr)
**Constants:** UPPER_SNAKE_CASE for static final fields

## Code Style

**Formatting:** 4 spaces, 120 chars, Allman braces (opening brace on own line)
**Linting:** Checkstyle + PMD

## Import Organization

**Order:** java.* → javax.* → third-party → internal
**Grouping:** Blank line between groups, static imports last

## Error Handling

**Strategy:** Fail-fast with requireThat() validation at public API entry points
**Never:** Return null/empty on invalid input, swallow exceptions silently

## Style Enforcement

**Automated Tools:**
- Checkstyle: `./mvnw checkstyle:check`
- PMD: `./mvnw pmd:check`

**Manual Rules:** See conventions/ subdirectory for TIER1-3 violations

## Git Conventions {#git-conventions}

### Commit Types (MANDATORY)

| Type | When to Use | Example |
|------|-------------|---------|
| `feature` | New functionality, endpoint, component | `feature: add user registration` |
| `bugfix` | Bug fix, error correction | `bugfix: correct email validation` |
| `test` | Test-only changes | `test: add failing test for hashing` |
| `refactor` | Code cleanup, no behavior change | `refactor: extract validation helper` |
| `performance` | Performance improvement | `performance: add database index` |
| `config` | Dependencies, config, tooling | `config: update Maven plugins` |
| `docs` | User-facing documentation | `docs: add API documentation` |

**NOT valid:** `chore`, `build`, `ci`, `style` (use appropriate types above)

### Commit Message Format

```
{type}: {concise description}

- [Key change 1]
- [Key change 2]
```

### Git Hooks

Git hooks are located in `.claude/hooks/git/`. After cloning:

```bash
git config --local core.hooksPath .claude/hooks/git
```

## Planning Conventions {#planning-conventions}

### Adding Tasks to a Release

**MANDATORY**: Before adding a CHANGE file to a release, check if the release is complete.

**Use validation script:**
```bash
.claude/scripts/add-change-to-release.sh <release-number>
```

**If release is complete (all CHANGEs have SUMMARYs):**
1. Re-open release in STATE.md: Change `✅ Complete` → `🔄 In Progress`
2. Update task count in ROADMAP.md
3. Then add new CHANGE file

**Why this matters:** Completed releases may have downstream dependencies. Re-opening makes it clear work is still in progress and prevents status confusion.

### Finding Next Change

**Use sequential scanning (not STATUS markers):**
```bash
.claude/scripts/find-next-change.sh
```

This script finds the first CHANGE.md without a SUMMARY.md by scanning releases in numerical order, regardless of STATUS markers in STATE.md.

---

*See conventions/ subdirectory for detailed rules*
