---
paths:
  - "**/*.java"
---

# Java Code Style (Quick Reference)

**Full guide**: [docs/project/style-guide.md](../../docs/project/style-guide.md)
**Validation API**: [requirements-api.md](requirements-api.md) - requireThat/assert patterns

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
- Parser tests MUST use `isEqualTo(expected)` NOT `isNotEmpty()` or `isNotNull()` on arena - see [testing-claude.md](../../docs/code-style/testing-claude.md#parser-test-patterns)
  - `isNotEmpty()` on nodes: Tests nothing about specific node types
  - `isNotNull()` on arena: Only verifies parsing succeeded, NOT AST correctness
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

### Brace Omission for Single-Line Statements
Omit braces for `if`, `else`, `for`, `while`, and `do-while` when the body is a single line:
```java
// ✅ CORRECT - Omit braces for single-line body
if (condition)
    return;

for (int i = 0; i < count; ++i)
    process(i);

while (hasNext())
    advance();

// ❌ WRONG - Unnecessary braces for single-line body
if (condition)
{
    return;
}

for (int i = 0; i < count; ++i)
{
    process(i);
}
// ✅ CORRECT - Nested single-line structures also omit braces
if (condition)
    for (int i = 0; i < count; ++i)
        process(i);
```
**When braces ARE required:**
- Multi-statement bodies (2+ statements)
- Multi-line statements (single statement spanning multiple visual lines)
- Bodies containing comments

**⚠️ CRITICAL**: "Single-line" means ONE VISUAL LINE, not "one statement". A throw/return/method call
that spans multiple lines requires braces:
```java
// ❌ WRONG - Multi-line statement WITHOUT braces
if (Files.isDirectory(filePath))
    throw new IllegalArgumentException(
        "Directory not supported: " + filePath);

// ✅ CORRECT - Multi-line statement WITH braces
if (Files.isDirectory(filePath))
{
    throw new IllegalArgumentException(
        "Directory not supported: " + filePath);
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
- Magic numbers: Use named constants or inline comments to explain non-obvious numeric values:
  ```java
  // ❌ WRONG - Magic number with no explanation
  position += 7;

  // ✅ CORRECT - Named constant explains the value
  private static final String NON_SEALED_SUFFIX = "-sealed";
  position += NON_SEALED_SUFFIX.length();

  // ✅ ALSO CORRECT - Inline comment when constant would be overkill
  // Skip "*/"
  position += 2;
  ```
  **When to use which:**
  - Named constant: Value used multiple times OR represents domain concept (e.g., `MAX_RETRIES`)
  - Inline comment: Value used once AND meaning is clear from context (e.g., skipping known tokens)
  - No explanation needed: Obvious values like `0`, `1`, `-1` in common idioms
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

### PMD Suppression
Only suppress with documented legitimate reason. "Too much work" is NOT valid.

**NcssCount violations**: **NEVER suppress without actually attempting decomposition first.**

⚠️ **CRITICAL**: Reasoning about why decomposition "would be hard" is NOT the same as attempting it.

**MANDATORY before ANY NcssCount suppression**:
1. **Actually extract** at least one helper method or helper class
2. **Run the build** to verify extraction works
3. **Compare readability** of extracted vs original code
4. **Only if extraction genuinely harms readability**, add suppression with a comment explaining:
   - What decomposition was attempted
   - Why it harmed readability (be specific)

**Valid reasons for suppression** (after attempting decomposition):
- State machine logic where extraction breaks control flow clarity
- Switch statements with many cases that cannot be reasonably grouped
- Parser methods with tightly coupled position/token state

**INVALID reasons** (do NOT suppress):
- "Shared state would need to be exposed" - use inner classes or pass state
- "Would need to pass many parameters" - create parameter objects or extract to helper class
- "Class is inherently large" - extract to multiple classes (e.g., ModuleParser helper)

**Example - Parser too large**:
```java
// ❌ WRONG - Suppressing without extraction
// "Parser needs shared state so extraction is hard"
@SuppressWarnings("PMD.NcssCount")
public class Parser { /* 1600+ lines */ }

// ✅ CORRECT - Extract to helper class
public class Parser {
    private final ModuleParser moduleParser;  // Handles module-info.java
    // ... now under threshold
}
```

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

