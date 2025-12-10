---
paths:
  - "**/*.java"
---

# Java Code Style (Quick Reference)

**Full guide**: [docs/project/style-guide.md](../../docs/project/style-guide.md)

## Validation Requirements

Style validation requires **THREE components** - checking only one is a CRITICAL ERROR:

```bash
./mvnw checkstyle:check pmd:check   # Automated tools
# + Manual review of docs/code-style-human.md (TIER1/TIER2/TIER3 rules)
```

## Critical Rules

### JavaDoc
- Manual authoring required (no scripts/automation)
- Explain WHY, not just WHAT
- Use `{@code}` for all identifiers
- No `@since` tags (use git history)
- Thread-safety notes go LAST in class JavaDoc

### TestNG
- No `@BeforeMethod`/`@AfterMethod` (creates shared state)
- No `@Test(enabled = false)` (stub tests prohibited)
- Use `requireThat()` for assertions, not manual if-throw
- Don't chain redundant validators (`isNotEmpty()` implies `isNotNull()`)

### Code Patterns
- `strip()` over `trim()` (Unicode whitespace)
- `List.copyOf()` over `Collections.unmodifiableList()` (true immutable)
- `stream().toList()` over `collect(Collectors.toUnmodifiableList())`
- Import types, never use FQNs in code
- `Optional` only as return type, never as parameter
- Explicit types preferred over `var` (use `var` only for long generics)
- Pre-increment `++i` over post-increment `i++` (Checkstyle enforces)
- Pre-increment `++i` over `i += 1` (consistency with codebase)
- Comments above the line they describe, not trailing:
  ```java
  // ✅ CORRECT - Comment above
  // Skip the closing character
  ++i;

  // ❌ WRONG - Trailing comment
  ++i; // Skip the closing character
  ```

### Validation Chaining
```java
// ❌ Separate calls
requireThat(x, "x").isPositive();
requireThat(x, "x").isLessThan(100);

// ✅ Single chain
requireThat(x, "x").isPositive().isLessThan(100);
```

### PMD Suppression
Only suppress with documented legitimate reason. "Too much work" is NOT valid.
