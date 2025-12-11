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
- Add JavaDoc comments to test classes/methods instead of `@SuppressWarnings("PMD.CommentRequired")`

### Code Patterns
- `strip()` over `trim()` (Unicode whitespace)
- `List.of()`/`Set.of()`/`Map.of()` over array literals for constants (truly immutable)
- `List.copyOf()` over `Collections.unmodifiableList()` (true immutable)
- `stream().toList()` over `collect(Collectors.toUnmodifiableList())`
- Import types, never use FQNs in code
- `Optional` only as return type, never as parameter
- Explicit types preferred over `var` (use `var` only for long generics)
- Pre-increment `++i` over post-increment `i++` (Checkstyle enforces)
- Pre-increment `++i` over `i += 1` (consistency with codebase)
- Combine consecutive conditionals with same body (unless it harms readability):
  ```java
  // ❌ WRONG - Separate statements with same body
  if (position + 1 >= sourceCode.length())
      return;
  if (sourceCode.charAt(position + 1) != ' ')
      return;

  // ✅ CORRECT - Combined with ||
  if (position + 1 >= sourceCode.length() || sourceCode.charAt(position + 1) != ' ')
      return;
  ```
- Prefer switch expression over long `||` chains when checking value against many constants:
  ```java
  // ❌ WRONG - Long || chain
  return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%';

  // ✅ CORRECT - Switch expression
  return switch (ch)
  {
      case '+', '-', '*', '/', '%' -> true;
      default -> false;
  };
  ```
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

**NcssCount violations**: Prefer breaking down the method into smaller helper methods over suppressing.
Suppression is acceptable only when decomposition would harm readability (e.g., tightly coupled state
machine logic where extraction creates unclear control flow).
