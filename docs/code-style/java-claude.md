# Claude Java Style Guide - Detection Patterns

**File Scope**: `.java` files only
**Purpose**: Systematic violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### External Source Documentation - Missing References
**Pattern**: Language spec references without source comments | **Violation**: `int maxDepth = 100;` | **Correct**: `// Based on JLS §14.4 block statement nesting limits\nint maxDepth = 100;`

### JavaDoc Exception Documentation - Missing @throws
**Pattern**: `public.*throws.*Exception.*\{` without `@throws` | **Violation**: `public void process() throws ValidationException {` | **Correct**: `/**\n * @throws ValidationException when input validation fails\n */` | **Rationale**: Helps API consumers handle errors

### JavaDoc Parameter References - Missing {@code} Markup
**Pattern**: `@throws.*if [a-zA-Z_][a-zA-Z0-9_]*( or [a-zA-Z_][a-zA-Z0-9_]*)* (is|are) null` without {@code} | **Violation**: `@throws IllegalArgumentException if sourceCode or parseOptions are null` | **Correct**: `@throws IllegalArgumentException if {@code sourceCode} or {@code parseOptions} are null` | **Rationale**: Semantic distinction, better documentation generation

### Validation - Use requireThat() Instead of Manual Checks
**Pattern**: `if \(.+ [<>!=]=.+\)\s*\{\s*throw new IllegalArgumentException` | **Violation**: `if (end < start) { throw new IllegalArgumentException(...); }` | **Correct**: `requireThat(end, "end").isGreaterThanOrEqualTo(start, "start");`
**Detection Commands**: `grep -rn -A1 -E 'if \(.+ [<>!=]=' --include="*.java" . | grep 'throw new IllegalArgumentException'`
**Examples** (preserved verbatim - illustrate pattern):
```java
// VIOLATION - Manual validation
if (end < start) {
    throw new IllegalArgumentException("end must be >= start, got: start=" + start + ", end=" + end);
}

// CORRECT - requireThat() validation
requireThat(end, "end").isGreaterThanOrEqualTo(start, "start");

// VIOLATION - Manual null check
if (source == null) {
    throw new NullPointerException("source must not be null");
}

// CORRECT - requireThat() null check
requireThat(source, "source").isNotNull();
```
**Rationale**: Consistent validation, better error messages, standardized naming, clearer intent, reduces boilerplate

### Parameter Formatting - Multi-line Declarations and Calls
**Pattern 1**: `(record|public\s+\w+).*\([^)]*\n.*,` | **Pattern 2**: `(new\s+\w+|[a-zA-Z_]\w*)\([^)]*\n` (efficiency analysis required)
**Detection**: Find multi-line constructs `grep -n "([^)]*\n" *.java` | Measure character usage per line | Apply line-filling efficiency (fill to 120 chars, wrap only when next param exceeds limit)
**Principle**: Maximize params per line within 120-char limit
**Violations**: `record Token(\n    TokenType type,\n    int position\n)` (fits on one line ~40 chars) | `new ParseResult(\n    astRoot, errors, ...)` (underutilized first line)
**Correct**: `record Token(TokenType type, int position)` (single line <120) | `new ParseResult(astRoot, errors, comments, sourcePositions,\n    metadata);` (line-filling)
**Rationale**: Maximize horizontal/vertical space efficiency without sacrificing readability

### Class Organization - User-Facing Members First
**Pattern**: `(public|protected).*record\s+\w+.*\{.*\n.*public\s+\w+.*\(` | **Violation**: `public record Result(...) {...}\npublic ReturnType method() {...}` | **Correct**: `public ReturnType method() {...}\npublic record Result(...) {...}` | **Rationale**: Present API surface first

## TIER 2 IMPORTANT - Code Review

### Stream vs For-Loop - Inappropriate Stream Usage
**Pattern**: `\.forEach\(System\.out::println\)` | **Violation**: `items.stream().forEach(System.out::println);` | **Correct**: `for (String item : items) System.out.println(item);`

### Exception Messages - Missing Business Context
**Pattern**: `throw new.*Exception\("Invalid.*"\)` | **Violation**: `throw new IllegalArgumentException("Invalid amount");` | **Correct**: `throw new IllegalArgumentException("Withdrawal amount must be positive for account " + accountId);`

### Field Initialization - Avoid Redundant Default Values
**Pattern**: `(private|protected|public)\s+(int|long|boolean|double|float)\s+\w+\s*=\s*0` or constructor assignments to defaults | **Violation**: `private int position = 0;` or `this.position = 0;` in constructor | **Correct**: `private int position;` (Java auto-initializes to 0)
**Detection Commands**: `grep -rn -E '(private|protected|public)\s+(int|long|boolean|double|float)\s+\w+\s*=\s*0' --include="*.java" .`
**Examples** (preserved - illustrate pattern):
```java
// VIOLATION - Explicit defaults
private int position = 0;
private boolean initialized = false;

// CORRECT - Rely on Java defaults
private int position;
private boolean initialized;
```
**Rationale**: Java auto-initializes primitives (0/false/null). Explicit defaults redundant, reduces clutter
**Note**: Only applies when value equals Java's default. Non-defaults (e.g., `private int maxDepth = 100;`) required

### Class Declaration - Missing final Modifier
**Pattern**: `public\s+class\s+\w+\s+(extends\s+\w+\s*)?\{` | **Violation**: `public class ArgumentParser {` (not extended) | **Correct**: `public final class ArgumentParser {` | **Rationale**: Prevent unintended subclassing, make intent explicit | **Exception**: Abstract classes, base classes for extension, classes with protected methods for overriding

### Control Flow - Multiple OR Comparisons Should Use Switch
**Pattern**: `return .+ == .+ \|\|` (3+ comparisons) | **Violation**: `return type == A || type == B || type == C;` | **Correct**: `return switch (type) { case A, B, C -> true; default -> false; };`
**Detection**: `grep -rn -E 'return .+ == .+ \|\|.+ == .+ \|\|' --include="*.java" .`
**Examples** (preserved - illustrate switch pattern):
```java
// VIOLATION - Multiple OR comparisons
return type == TokenType.ASSIGN || type == TokenType.PLUSASSIGN || type == TokenType.MINUSASSIGN;

// CORRECT - Switch statement
return switch (type) {
    case ASSIGN, PLUSASSIGN, MINUSASSIGN -> true;
    default -> false;
};
```
**Rationale**: Switch with comma-separated cases more concise, maintainable, less error-prone. Makes set membership check immediately clear.

### JavaDoc - Thread-Safety Documentation Format and Usage
**Required Format**: `<b>Thread-safety</b>:` (bold HTML) + "This class is thread-safe." or "This class is immutable."
**Violation**: Incorrect format | Documenting non-thread-safe classes | Negative assertions | "Thread-safe" (wrong spelling) | Plain text (no bold) | "immutable and thread-safe" (redundant)
**Correct**: Only document for thread-safe classes with proper format
**Detection**: `grep -rn -E '\* Thread-safe:' --include="*.java" .` (wrong spelling) | `grep -rn -E '\* Thread-safety:' --include="*.java" . | grep -v '<b>Thread-safety</b>'` (missing bold)
**Examples** (preserved - show format):
```java
// ✅ CORRECT
/**
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ConfigurationCache { ... }

// ❌ VIOLATION - Not using bold
/**
 * Thread-safety: This class is thread-safe.
 */

// ❌ VIOLATION - Documenting non-thread-safety
/**
 * <b>Thread-safety</b>: This class is not thread-safe.
 */
```
**Format Rules**: `<b>Thread-safety</b>:` (HTML bold, "Thread-safety" spelling) | "thread-safe" OR "immutable" (NOT both) | Only for thread-safe classes | Absence implies not thread-safe
**Rationale**: Positive assertion with consistent formatting

## TIER 3 QUALITY - Best Practices

### Error Handling - Default Return Values
**Violation**: Catching exceptions and returning default/fallback values | **Correct**: Propagate errors to allow proper caller handling
**Example** (preserved - illustrate pattern):
```java
// VIOLATION - Silent failure
return ASTNode.EMPTY; // Hides failure

// CORRECT - Fail fast
throw new ParseException("Invalid token stream: " + e.getMessage(), e);
```
**Rationale**: Parser operations must fail visibly. Defaults (empty nodes, null) hide serious errors, lead to incorrect AST construction
**Manual Analysis**: Examine called methods for default/ZERO returns. Methods named `handle.*Error`, `.*ErrorHandler`, `default.*` particularly suspect

### Error Handling - Checked Exception Wrapping with RuntimeException
**Pattern**: `throw new RuntimeException` wrapping checked exceptions | **Violation**: Generic `RuntimeException` in lambdas/streams | **Correct**: `WrappedCheckedException.wrap()`
**Detection**: `grep -rn -A2 'throw new RuntimeException' --include="*.java" . | grep -B2 'catch.*Exception'`
**Example** (preserved):
```java
// VIOLATION
throw new RuntimeException(e);

// CORRECT
throw WrappedCheckedException.wrap(e);
```
**Rationale**: Explicit wrapped checked exception, better stack traces, distinguishes programming errors from wrapped I/O exceptions
**Manual Analysis**: Not all `RuntimeException` uses are violations - only those wrapping checked exceptions in lambda/stream contexts

### Concurrency - Virtual Thread Preference
**Pattern**: `CompletableFuture\.supplyAsync\(\s*\(\)\s*->\s*.*\.(read|write|process)\(` | **Violation**: CompletableFuture for simple I/O | **Correct**: Virtual thread executor for I/O-bound operations
**Example**: `try (var executor = Executors.newVirtualThreadPerTaskExecutor()) { var future = executor.submit(() -> Files.readString(path)); return future.get(); }`

### Exception Type Selection - IllegalStateException vs AssertionError
**Critical Distinction**: Choose based on WHO caused failure
**Pattern 1 - User Error**: `IllegalStateException` for API contract violations (user misuse) | Detection: `grep -rn "throw new IllegalStateException" --include="*.java" .`
**Pattern 2 - Code Bugs**: `AssertionError` for internal invariants (implementation bugs) | Detection: `grep -rn "throw new IllegalStateException.*should" --include="*.java" .`
**Examples** (preserved - illustrate distinction):
```java
// CORRECT - User violated API (IllegalStateException)
if (!isInitialized) {
    throw new IllegalStateException("Formatter must be initialized before use");
}

// CORRECT - Internal invariant (AssertionError)
throw new AssertionError("Merged configuration should be valid", e);
```
**Rationale**: IllegalStateException = recoverable API usage errors | AssertionError = programming bugs, broken invariants

### Method Naming - Clarity Over Brevity
**Abbreviations to Flag**: `*Nr` → `*Number` | `*Pos` → `*Position` | `*Len` → `*Length` | `*Cnt` → `*Count` | `*Msg` → `*Message` | `*Src` → `*Source` | `calc*` → `calculate*` | `fmt*` → `format*`
**Detection**: `grep -rn -E '(public|protected|private)\s+\w+\s+\w+(Nr|Pos|Len|Cnt|Msg|Src)\(' --include="*.java" .`
**Exceptions (OK)**: Universal acronyms (URL, HTML, JSON, HTTP) | Java conventions (toString, hashCode, equals) | Domain terms (AST, JLS, DOM) | Math (min, max, abs)
**Examples**: `getLineNr()` → `getLineNumber()` | `calcDepth()` → `calculateDepth()` | `fmtSrc()` → `formatSource()`
**Rationale**: Clear names improve comprehension. Abbreviations require mental translation, reduce readability

## Optimized Detection Commands

**Performance Strategy**: Batch similar patterns, use parallel execution, generate complete violation reports.

```bash
# TIER 1 CRITICAL - Batch execution for speed
echo "=== TIER 1 CRITICAL VIOLATIONS ==="

# External source documentation missing
echo "External Documentation:"
grep -rn "new BigDecimal" --include="*.java" . | grep -v '// .*http'

# JavaDoc missing @throws
echo "JavaDoc @throws:"
grep -rn -A5 -E 'public.*throws.*Exception' --include="*.java" . | grep -B5 -v '@throws'

# TIER 2 IMPORTANT
echo "=== TIER 2 IMPORTANT VIOLATIONS ==="

# Stream inappropriate usage
echo "Stream Usage:"
grep -rn '\.forEach(System\.out::println)' --include="*.java" .

# Exception messages missing context
echo "Exception Messages:"
grep -rn -E 'throw new.*Exception\("Invalid.*"\)' --include="*.java" .

# TIER 3 QUALITY
echo "=== TIER 3 QUALITY VIOLATIONS ==="

```

**Usage for Style Auditor**: Run all commands in parallel, collect results, generate detailed violation reports with file locations and context.