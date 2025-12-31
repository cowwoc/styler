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
- No `@throws AssertionError` - programming errors are unexpected and unrecoverable (not for callers to handle)

### TestNG
- No `@BeforeMethod`/`@AfterMethod` (creates shared state)
- No `@Test(enabled = false)` (stub tests prohibited)
- Use `@Test(expectedExceptions = X.class)` for exception tests, NOT `assertThrows()`:
  ```java
  // ❌ WRONG - JUnit style, not TestNG
  @Test
  public void shouldRejectNull()
  {
      assertThrows(IllegalArgumentException.class, () -> method(null));
  }

  // ✅ CORRECT - TestNG native exception testing
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldRejectNull()
  {
      method(null);
  }
  ```
- Use `requireThat()` for assertions, not manual if-throw
- Don't chain redundant validators (`isNotEmpty()` implies `isNotNull()`)
- Trust natural NPE from method calls (don't check null before calling methods that would throw NPE anyway)
- Don't validate before calling methods that already validate the same parameter:
  ```java
  // ❌ WRONG - validateIndex() already checks isNotNull()
  requireThat(index, "index").isNotNull();
  validateIndex(index);

  // ✅ CORRECT - Let validateIndex() handle validation
  validateIndex(index);
  ```
- Add JavaDoc comments to test classes/methods instead of `@SuppressWarnings("PMD.CommentRequired")`
- Test class JavaDoc should describe the **category of tests** (what functionality is being tested), not
  boilerplate about thread safety or validation patterns:
  ```java
  // ❌ WRONG - Boilerplate about thread safety and validation style
  /**
   * Thread-safe tests for Parser class declarations.
   * <p>
   * Each test validates both successful parsing AND correct AST structure using the two-step
   * parse-then-compare pattern.
   */
  public class ClassParserTest { }

  // ✅ CORRECT - Describes what category of tests the class contains
  /**
   * Tests for parsing class, interface, and enum declarations.
   */
  public class ClassParserTest { }
  ```
- Parser tests MUST use `isEqualTo(expected)` NOT `isNotEmpty()` - see [testing-claude.md](../../docs/code-style/testing-claude.md#parser-test-patterns)
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
- Test source strings: MUST use text blocks with natural formatting (one statement per line):
  ```java
  // ❌ WRONG - Escape sequences instead of text block
  String source = "public class Test\n{\n\tvoid foo()\n\t{\n\t}\n}\n";

  // ❌ WRONG - Redundant comment duplicating source content
  // source: public class Test\n{\n\tvoid foo()...
  String source = "...";

  // ❌ WRONG - Text block but compacted (multiple statements on same line)
  String source = """
      import java.util.*;
      class Test { void foo() { int x = 1; } }
      """;

  // ✅ CORRECT - Text block with natural formatting
  String source = """
      import java.util.*;

      class Test
      {
          void foo()
          {
              int x = 1;
          }
      }
      """;
  ```
  **Key requirements:**
  - ALWAYS use text blocks (`"""`) for multi-line source strings
  - NEVER use escape sequences (`\n`, `\t`) for source code
  - NEVER add comments that duplicate the source content (text blocks are self-documenting)
  - Format code naturally with one statement per line
- File cleanup: Use `TestFileFactory.deleteFilesQuietly()` instead of duplicating cleanup code:
  ```java
  // ❌ WRONG - Duplicate cleanup pattern
  finally
  {
      for (Path file : files)
      {
          try
          {
              Files.deleteIfExists(file);
          }
          catch (IOException _)
          {
              // Intentionally ignored
          }
      }
  }

  // ✅ CORRECT - Use shared utility
  finally
  {
      TestFileFactory.deleteFilesQuietly(files);
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
- Import types, never use FQNs in code or JavaDoc:
  ```java
  // ❌ WRONG - FQN in record/class declaration
  private record Helper() implements com.example.SomeInterface {}

  // ❌ WRONG - FQN in JavaDoc @throws
  /** @throws java.io.IOException if file read fails */

  // ✅ CORRECT - Import at top, use simple name everywhere
  import com.example.SomeInterface;
  import java.io.IOException;
  // ...
  private record Helper() implements SomeInterface {}
  /** @throws IOException if file read fails */
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
- Prefer switch over if-else-if chains with 3+ branches (when the condition tests the same variable):
  ```java
  // ❌ WRONG - Long if-else-if chain
  if (type == TokenType.STRING)
      handleString();
  else if (type == TokenType.NUMBER)
      handleNumber();
  else if (type == TokenType.IDENTIFIER)
      handleIdentifier();
  else
      handleOther();

  // ✅ CORRECT - Switch statement
  switch (type)
  {
      case STRING -> handleString();
      case NUMBER -> handleNumber();
      case IDENTIFIER -> handleIdentifier();
      default -> handleOther();
  }
  ```
  **When NOT to convert**: Complex conditions (e.g., `x > 5 && x < 10`), conditions on different variables,
  or when the switch would require pattern matching not available in Java 21.
- Comments above the line they describe, not trailing:
  ```java
  // ✅ CORRECT - Comment above
  // Skip the closing character
  ++i;

  // ❌ WRONG - Trailing comment
  ++i; // Skip the closing character
  ```
- Comments must not reference past implementations or "original behavior":
  ```java
  // ❌ WRONG - References past implementation
  int adjusted = position - 1;  // -1 to match original behavior (endPosition is inclusive)

  // ✅ CORRECT - Explains current state
  // -1 because endPosition is inclusive, pointing to semicolon
  int adjusted = position - 1;
  ```
- Extract repeated element access to local variable (but **only for 2+ uses**):
  ```java
  // ❌ WRONG - Repeated collection access (3 uses, needs variable)
  requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.STRING);
  requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("hello");
  requireThat(tokens.get(0).start(), "tokens.get(0).start()").isEqualTo(0);

  // ✅ CORRECT - Extract to local variable (used 3 times)
  Token token = tokens.get(0);
  requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING);
  requireThat(token.text(), "token.text()").isEqualTo("hello");
  requireThat(token.start(), "token.start()").isEqualTo(0);

  // ❌ WRONG - Single use, no need for variable
  Token boundToken = previousToken();
  return arena.allocateNode(..., boundToken.end());

  // ✅ CORRECT - Single use, call directly inline
  return arena.allocateNode(..., previousToken().end());
  ```
  **⚠️ CRITICAL**: Before extracting, trace execution paths - if index variable can change between accesses
  (e.g., `position` modified in conditional block), the accesses may refer to DIFFERENT elements. See
  [style-guide.md § Trace Execution Paths](../../docs/project/style-guide.md#extract-trace-execution-paths).
- **Check for existing helper methods first**: Before extracting to local variable, search for existing
  helper methods (e.g., if `currentToken()` exists, create `previousToken()` for `tokens.get(position - 1)`).
  See [style-guide.md § Check Existing Helper Methods](../../docs/project/style-guide.md#check-existing-helper-methods).
  - 2-3 occurrences in same method → Local variable
  - 3+ occurrences across multiple methods → Helper method
  - Existing helper pattern → Follow the pattern

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

### Contextual Validation Errors
When validating derived values or method results, the parameter name alone may not provide enough context
for debugging. Use `withContext()` to add diagnostic information:
```java
// ❌ WRONG - Unhelpful error: "root.isValid() must be true"
requireThat(success.rootNode().isValid(), "root.isValid()").isTrue();

// ✅ CORRECT - Includes diagnostic context
requireThat(success.rootNode().isValid(), "root.isValid()").
    withContext(success.rootNode(), "rootNode").
    isTrue();
```

**When to use `withContext()`:**
- Validating method return values (e.g., `x.isValid()`, `list.isEmpty()`)
- Validating computed/derived values
- When the parameter name alone won't help diagnose failures

**What to include in context:**
- The object being validated (for inspecting its state)
- Related values that help understand the failure
- Source code or input that led to the value

### PMD Suppression
Only suppress with documented legitimate reason. "Too much work" is NOT valid.

**NcssCount violations**: ALWAYS attempt to fix by breaking down the method into smaller helper methods.
Suppression is a LAST RESORT, acceptable only when decomposition would genuinely harm readability (e.g.,
tightly coupled state machine logic where extraction creates unclear control flow). Before suppressing:
1. Identify logical blocks that can become helper methods
2. Extract repetitive patterns into shared utilities
3. Consider if the method is doing too much (Single Responsibility Principle)

### @SuppressWarnings("unchecked") - Minimal Scope
Never apply to entire method. Always apply to single expression:
```java
// ❌ WRONG - Method-level suppression
@SuppressWarnings("unchecked")
public <T> T getAttribute(Class<T> type) { ... }

// ✅ CORRECT - Expression-level suppression
public <T> T getAttribute(Class<T> type)
{
    if (type.isInstance(attribute))
    {
        @SuppressWarnings("unchecked") T result = (T) attribute;
        return result;
    }
    return null;
}
```

### JavaDoc/Comments/Errors - Use "empty" Not "blank"
When documenting string validation that checks for null/whitespace-only values, use "empty" in user-facing text:
```java
// ❌ WRONG - Uses "blank" in documentation
/**
 * @throws IllegalArgumentException if {@code name} is blank
 */

// ✅ CORRECT - Uses "empty" for user-facing documentation
/**
 * @throws IllegalArgumentException if {@code name} is empty
 */
```

**Note**: The method call `isNotBlank()` remains unchanged - only the documentation terminology changes.
