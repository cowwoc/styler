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

---

*See conventions/ subdirectory for detailed rules*
