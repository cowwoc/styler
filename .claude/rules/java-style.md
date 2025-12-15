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
- Compact constructors MUST have their own `@param` tags (not inherited from record declaration)

### TestNG
- No `@BeforeMethod`/`@AfterMethod` (creates shared state)
- No `@Test(enabled = false)` (stub tests prohibited)
- Use `requireThat()` for assertions, not manual if-throw
- Don't chain redundant validators (`isNotEmpty()` implies `isNotNull()`)
- Add JavaDoc comments to test classes/methods instead of `@SuppressWarnings("PMD.CommentRequired")`
- No meaningless assertions - `assertTrue(true, ...)` always passes and tests nothing:
  ```java
  // ❌ WRONG - Useless assertion that always passes
  @Test
  public void shouldCompleteWithoutThrowing()
  {
      doWork();
      assertTrue(true, "Should complete without throwing");  // Tests nothing!
  }

  // ✅ CORRECT - No assertion needed; test passes if no exception thrown
  @Test
  public void shouldCompleteWithoutThrowing()
  {
      doWork();
      // Test passes if we reach here without exception
  }
  ```
- Use `@Test(timeOut = X)` for deadlock protection, not post-completion time checks:
  ```java
  // ❌ WRONG - Only verifies timing after completion (doesn't prevent hangs)
  @Test
  public void shouldCompleteWithoutDeadlock()
  {
      Instant start = Instant.now();
      doWork();
      Duration elapsed = Duration.between(start, Instant.now());
      requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(30));
  }

  // ✅ CORRECT - TestNG kills the test if it hangs
  @Test(timeOut = 30_000)
  public void shouldCompleteWithoutDeadlock()
  {
      doWork();
  }
  ```

### Code Patterns
- `strip()` over `trim()` (Unicode whitespace)
- `List.of()`/`Set.of()`/`Map.of()` over array literals for constants (truly immutable)
- `List.copyOf()` over `Collections.unmodifiableList()` (true immutable)
- `stream().toList()` over `collect(Collectors.toUnmodifiableList())`
- Defensive copying for List parameters: Use `List.copyOf()` in constructors and setters to prevent external
  mutation:
  ```java
  // ❌ WRONG - Caller can mutate the list after passing it
  public MyClass(List<Item> items)
  {
      this.items = items;
  }
  public void setItems(List<Item> items)
  {
      this.items = items;
  }

  // ✅ CORRECT - Defensive copy ensures immutability
  public MyClass(List<Item> items)
  {
      this.items = List.copyOf(items);
  }
  public void setItems(List<Item> items)
  {
      this.items = List.copyOf(items);
  }
  ```
- Import types, never use FQNs in code (including in nested types):
  ```java
  // ❌ WRONG - FQN in record/class declaration
  private record Helper() implements com.example.SomeInterface {}

  // ✅ CORRECT - Import at top, use simple name
  import com.example.SomeInterface;
  // ...
  private record Helper() implements SomeInterface {}
  ```
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

### Time Calculations
Use `Instant` and `Duration` instead of `long` for time measurements:
```java
// ❌ WRONG - Using long for time
long startTime = System.currentTimeMillis();
doWork();
long elapsed = System.currentTimeMillis() - startTime;
assertTrue(elapsed < 5000, "Took too long: " + elapsed);

// ✅ CORRECT - Using Instant/Duration
Instant startTime = Instant.now();
doWork();
Duration elapsed = Duration.between(startTime, Instant.now());
requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(5));
```

**Key points:**
- Use `Instant.now()` to capture timestamps
- Use `Duration.between(start, end)` to calculate elapsed time
- Use `requireThat()` for Duration comparisons (better error messages)
- Use `Duration.ofSeconds()`, `Duration.ofMillis()` etc. for thresholds

### Thread-Safety
If JavaDoc claims thread-safety, implementation MUST match:
- Use `AtomicBoolean` for boolean state (not `volatile boolean`)
- `volatile` only provides visibility, NOT atomicity for check-then-act patterns
- The pattern `if (!closed) { doSomething(); closed = true; }` is NOT thread-safe even with `volatile`
```java
// ❌ WRONG - Plain boolean, not thread-safe
private boolean closed;

// ❌ WRONG - Volatile doesn't make check-then-act atomic
private volatile boolean closed;
// if (!closed) { x.close(); closed = true; }  // Race condition!

// ✅ CORRECT - AtomicBoolean with compareAndSet
private final AtomicBoolean closed = new AtomicBoolean();
// if (closed.compareAndSet(false, true)) { x.close(); }  // Thread-safe
```

### Validation: Public vs Internal Types
```java
// Public API - use requireThat() (always validates, throws on failure)
public void process(String input)
{
    requireThat(input, "input").isNotNull();
}

// Internal types - use assert that() (validates only with -ea flag)
record InternalData(String value)
{
    InternalData
    {
        assert that(value, "value").isNotNull().elseThrow();
    }
}
```

**When to use which:**
- `requireThat()` - Public constructors, public methods, API boundaries
- `assert that().elseThrow()` - Internal records, package-private types, invariants

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
