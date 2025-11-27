# Claude Java Style Guide - Detection Patterns

**File Scope**: `.java` files only
**Purpose**: Systematic violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### External Source Documentation - Missing References
**Detection Pattern**: Language specification references without source comments
**Violation**: `int maxDepth = 100; // No source reference`
**Correct**: `// Based on JLS §14.4 block statement nesting limits\nint maxDepth = 100;`

### JavaDoc Paragraph Formatting - Empty Line Before &lt;p&gt;
**Detection Pattern**: `\* .*\n \*\n \* <p>` (blank JavaDoc line before `<p>` tag)
**Violation**: `/**\n * Summary line.\n *\n * <p>\n * Detailed description.`
**Correct**: `/**\n * Summary line.\n * <p>\n * Detailed description.`
**Detection Commands**:
```bash
# Find JavaDoc with blank line before <p>
grep -rn -B2 '^ \* <p>$' --include="*.java" . | grep -B1 '^ \*$'
```
**Rationale**: JavaDoc formatting convention requires `<p>` to appear immediately after summary line without blank lines. This maintains consistent paragraph separation and follows standard JavaDoc style.

### JavaDoc Exception Documentation - Missing @throws
**Detection Pattern**: `public.*throws.*Exception.*\{` without corresponding `@throws`
**Violation**: `public void process() throws ValidationException { // Missing @throws in JavaDoc`
**Correct**: `/**\n * @throws ValidationException when input validation fails\n */\npublic void process() throws ValidationException {`
**Rationale**: Exception documentation helps API consumers handle errors properly

### JavaDoc Parameter References - Missing {@code} Markup
**Detection Pattern**: `@throws.*if [a-zA-Z_][a-zA-Z0-9_]*( or [a-zA-Z_][a-zA-Z0-9_]*)* (is|are) null` without {@code}
**Violation**: `@throws IllegalArgumentException if sourceCode or parseOptions are null`
**Correct**: `@throws IllegalArgumentException if {@code sourceCode} or {@code parseOptions} are null`
**Rationale**: {@code} markup provides semantic distinction and better documentation generation

### Validation - Use requireThat() Instead of Manual Checks
**Detection Pattern**: `if \(.+ [<>!=]=.+\)\s*\{\s*throw new IllegalArgumentException`
**Violation**: `if (end < start) { throw new IllegalArgumentException("end must be >= start, got: start=" + start + ", end=" + end); }`
**Correct**: `requireThat(end, "end").isGreaterThanOrEqualTo(start, "start");`
**Detection Commands**:
```bash
# Find manual validation throws
grep -rn -A1 -E 'if \(.+ [<>!=]=' --include="*.java" . | grep 'throw new IllegalArgumentException'

# Common patterns to check
grep -rn 'if (.*<.*) {' --include="*.java" . | grep -A1 'throw new IllegalArgumentException'
grep -rn 'if (.*>.*) {' --include="*.java" . | grep -A1 'throw new IllegalArgumentException'
grep -rn 'if (.*==.*) {' --include="*.java" . | grep -A1 'throw new IllegalArgumentException'
```
**Examples**:
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

// VIOLATION - Manual range check
if (position < 0) {
    throw new IllegalArgumentException("position must be non-negative, got: " + position);
}

// CORRECT - requireThat() range check
requireThat(position, "position").isNotNegative();
```
**Rationale**: requireThat() provides consistent validation with better error messages, standardized naming,
and clearer intent. The validation library generates consistent error messages automatically and reduces
boilerplate code.

### Parameter Formatting - Multi-line Declarations and Calls
**Detection Pattern 1**: `(record|public\s+\w+).*\([^)]*\n.*,` (multi-line parameter declarations)
**Detection Pattern 2**: `(new\s+\w+|[a-zA-Z_]\w*)\([^)]*\n` (multi-line parameter lists requiring efficiency
analysis)

**Multi-Step Detection for Pattern 2**:
1. **Find multi-line parameter constructs**: `grep -n "([^)]*\n" *.java`
2. **Extract line content and measure**: Calculate actual character usage per line
3. **Apply line-filling efficiency rules**:
   - Each line should be filled to maximum capacity within 120-character limit
   - Parameters should wrap to next line only when adding them would exceed 120 chars
   - Lines with <50% utilization when combination is possible → VIOLATION
   - Empty first lines (opening paren + newline) with subsequent packed lines → VIOLATION

**Canonical Line-Filling Style**:
**Principle**: Maximize parameters per line while respecting 120-character limit. Break to next line only when
adding the next parameter would exceed the limit.

**Violation Examples**:
- `record Token(\n    TokenType type,\n    int position\n)` (unnecessary wrapping - fits on one line: ~40 chars)
- `new ParseResult(\n    astRoot, errors, comments, sourcePositions, metadata);` (underutilized first line - wasted ~25 chars)
- `format(source,\n    config, rules, options, context);` (poor distribution - could fit more on first line)

**Correct Examples**:
- `record Token(TokenType type, int position)` (single line when under 120 chars)
- `new ParseResult(astRoot, errors, comments, sourcePositions,\n    metadata);` (line-filling: max params per line within limit)
- `format(source, config, rules, options,\n    context, preserveComments);` (wrap only when next param would exceed 120)

**Detection Commands**:
```bash
# Step 1: Find all multi-line parameter constructs
grep -rn -E "\([^)]*\n" --include="*.java" . > multiline_params.txt

# Step 2: Manual analysis required for each match:
# - Measure first line length from start of construct to newline
# - Measure subsequent line lengths including indentation  
# - Apply efficiency thresholds (50%/80%/120 char rules)
# - Flag violations where redistribution would improve line usage

# Step 3: Common violation patterns to check
grep -rn -E "new \w+\(\s*\n\s+\w+.*," --include="*.java" . # Likely underutilized first line
grep -rn -E "\w+\([^,)]+,[^,)]+,[^,)]+,[^,)]+,\s*\n\s+[^,)]+\);" --include="*.java" . # Overloaded first line
```

**Rationale**: Maximize both horizontal and vertical space efficiency; avoid wasted lines and improve code
density without sacrificing readability

### Class Organization - User-Facing Members First
**Detection Pattern**: `(public|protected).*record\s+\w+.*\{.*\n.*public\s+\w+.*\(` (nested types before
public methods)
**Violation**: `public record Result(...) { ... }\npublic ReturnType publicMethod() { ... }`
**Correct**: `public ReturnType publicMethod() { ... }\npublic record Result(...) { ... }`
**Rationale**: Present API surface first to improve code readability and understanding

## TIER 2 IMPORTANT - Code Review

### Stream vs For-Loop - Inappropriate Stream Usage
**Detection Pattern**: `\.forEach\(System\.out::println\)`
**Violation**: `items.stream().forEach(System.out::println);`
**Correct**: `for (String item : items) System.out.println(item);`

### Exception Messages - Missing Business Context
**Detection Pattern**: `throw new.*Exception\("Invalid.*"\)`
**Violation**: `throw new IllegalArgumentException("Invalid amount");`
**Correct**: `throw new IllegalArgumentException("Withdrawal amount must be positive for account " + accountId);`

### Field Initialization - Avoid Redundant Default Values
**Detection Pattern**: `(private|protected|public)\s+(int|long|boolean|double|float)\s+\w+\s*=\s*0` or constructor assignments to default values
**Violation**: `private int position = 0;` or `this.position = 0;` in constructor (when position is an int)
**Correct**: `private int position;` (Java initializes to 0 automatically)
**Detection Commands**:
```bash
# Find field declarations with explicit default values
grep -rn -E '(private|protected|public)\s+int\s+\w+\s*=\s*0;' --include="*.java" .
grep -rn -E '(private|protected|public)\s+long\s+\w+\s*=\s*0L?;' --include="*.java" .
grep -rn -E '(private|protected|public)\s+boolean\s+\w+\s*=\s*false;' --include="*.java" .
grep -rn -E '(private|protected|public)\s+double\s+\w+\s*=\s*0\.0;' --include="*.java" .
grep -rn -E '(private|protected|public)\s+float\s+\w+\s*=\s*0\.0f;' --include="*.java" .

# Find constructor assignments to default values (requires manual review)
grep -rn 'this\.\w+\s*=\s*0;' --include="*.java" .
grep -rn 'this\.\w+\s*=\s*false;' --include="*.java" .
```
**Examples**:
```java
// VIOLATION - Explicit default values in field declarations
private int position = 0;
private long count = 0L;
private boolean initialized = false;
private double value = 0.0;

// CORRECT - Rely on Java's default initialization
private int position;
private long count;
private boolean initialized;
private double value;

// VIOLATION - Redundant default assignments in constructor
public Parser(String source) {
    this.arena = new NodeArena();
    this.position = 0;           // Redundant - int defaults to 0
    this.depth = 0;              // Redundant
    this.tokenCheckCounter = 0;  // Redundant
}

// CORRECT - Only assign non-default values
public Parser(String source) {
    this.arena = new NodeArena();
    // position, depth, tokenCheckCounter automatically initialized to 0
}
```
**Rationale**: Java automatically initializes primitive fields to their default values (0 for numeric types,
false for boolean, null for objects). Explicit assignment to default values is redundant and adds unnecessary
code. This reduces visual clutter and makes non-default initializations more prominent.
**Note**: This rule applies only when the assigned value equals Java's default. Non-default initializations
(e.g., `private int maxDepth = 100;`) are required and should not be removed.

### Class Declaration - Missing final Modifier
**Detection Pattern**: `public\s+class\s+\w+\s+(extends\s+\w+\s*)?\{` (non-final classes)
**Violation**: `public class ArgumentParser {` (not extended anywhere)
**Correct**: `public final class ArgumentParser {`
**Rationale**: Classes not designed for extension should be declared final to prevent unintended subclassing
and make design intent explicit
**Exception**: Abstract classes, base classes explicitly designed for extension, or classes with protected
methods intended for overriding

### Class Visibility - Prefer Non-Exported Packages Over Package-Protected
**Detection Pattern**: Package-private class declarations in exported packages
**Violation**: `final class WrapContext {` (package-private in exported package)
**Correct**: `public final class WrapContext` in `.internal` package (not exported in module-info.java)
**Detection Commands**:
```bash
# Find package-private classes (no access modifier before class)
grep -rn -E '^\s*(final\s+)?class\s+[A-Z]' --include="*.java" . | grep -v '/test/'

# Find classes in non-.internal packages that might need relocation
grep -rn -E '^\s*(final\s+)?class\s+[A-Z]' --include="*.java" src/main/java/ | grep -v '\.internal\.'

# Verify module-info.java exports (internal packages should NOT be listed)
grep -E '^\s*exports\s+.*\.internal' src/main/java/module-info.java
```
**Examples**:
```java
// VIOLATION - Package-private class in exported package
// File: src/main/java/io/github/cowwoc/styler/formatter/WrapContext.java
package io.github.cowwoc.styler.formatter;
final class WrapContext { ... }  // Encapsulation via package-private

// CORRECT - Public class in non-exported internal package
// File: src/main/java/io/github/cowwoc/styler/formatter/internal/WrapContext.java
package io.github.cowwoc.styler.formatter.internal;
public final class WrapContext { ... }  // Encapsulation via module system

// module-info.java should NOT export the internal package:
// exports io.github.cowwoc.styler.formatter;  // ✅ Public API exported
// (no export for io.github.cowwoc.styler.formatter.internal)  // ✅ Hidden
```
**Rationale**: JPMS module encapsulation is stronger than package-private visibility. Non-exported packages
provide compile-time AND runtime enforcement, better tooling support, and clearer separation between API
and implementation.
**Exception**: Very small helper classes used only within one file, or legacy non-modularized code.

### Control Flow - Multiple OR Comparisons Should Use Switch
**Detection Pattern**: `return .+ == .+ \|\|` (3 or more equality comparisons chained with OR)
**Violation**: `return type == TokenType.ASSIGN || type == TokenType.PLUSASSIGN || type == TokenType.MINUSASSIGN;`
**Correct**: `return switch (type) { case ASSIGN, PLUSASSIGN, MINUSASSIGN -> true; default -> false; };`
**Detection Commands**:
```bash
# Find methods with 3+ OR comparisons
grep -rn -E 'return .+ == .+ \|\|.+ == .+ \|\|' --include="*.java" .

# Find if statements with 3+ OR comparisons
grep -rn -E 'if \(.+ == .+ \|\|.+ == .+ \|\|' --include="*.java" .
```
**Examples**:
```java
// VIOLATION - Multiple OR comparisons (12 comparisons)
private boolean isAssignmentOperator(TokenType type) {
    return type == TokenType.ASSIGN ||
        type == TokenType.PLUSASSIGN ||
        type == TokenType.MINUSASSIGN ||
        type == TokenType.STARASSIGN ||
        type == TokenType.DIVASSIGN ||
        type == TokenType.MODASSIGN ||
        type == TokenType.BITANDASSIGN ||
        type == TokenType.BITORASSIGN ||
        type == TokenType.CARETASSIGN ||
        type == TokenType.LSHIFTASSIGN ||
        type == TokenType.RSHIFTASSIGN ||
        type == TokenType.URSHIFTASSIGN;
}

// CORRECT - Switch statement
private boolean isAssignmentOperator(TokenType type) {
    return switch (type) {
        case ASSIGN, PLUSASSIGN, MINUSASSIGN, STARASSIGN, DIVASSIGN, MODASSIGN, BITANDASSIGN, BITORASSIGN,
            CARETASSIGN, LSHIFTASSIGN, RSHIFTASSIGN, URSHIFTASSIGN -> true;
        default -> false;
    };
}

// VIOLATION - Multiple OR comparisons in if statement (6 comparisons)
if (currentToken().type() == TokenType.MINUS ||
    currentToken().type() == TokenType.PLUS ||
    currentToken().type() == TokenType.NOT ||
    currentToken().type() == TokenType.TILDE ||
    currentToken().type() == TokenType.INC ||
    currentToken().type() == TokenType.DEC) {
    // ...
}

// CORRECT - Switch-based check
TokenType type = currentToken().type();
boolean isUnaryOperator = switch (type) {
    case MINUS, PLUS, NOT, TILDE, INC, DEC -> true;
    default -> false;
};
if (isUnaryOperator) {
    // ...
}
```
**Rationale**: Switch statements with multiple case labels (comma-separated) are more concise, easier to
maintain, and less error-prone than long chains of OR comparisons. The switch pattern makes it immediately
clear that we're checking for membership in a set of values.

### JavaDoc - Thread-Safety Documentation Format and Usage
**Required Format**: Use `<b>Thread-safety</b>:` (bolded with HTML tags) followed by brief description
**Violation**: Incorrect format, documenting non-thread-safe classes, or negative assertions
**Correct**: Only document thread-safety for classes that ARE thread-safe, using proper bold HTML format

**Detection Commands**:
```bash
# Find incorrect "Thread-safe" (should be "Thread-safety")
grep -rn -E '\* Thread-safe:' --include="*.java" .

# Find missing bold tags (plain text thread-safety documentation)
grep -rn -E '\* Thread-safety:' --include="*.java" . | grep -v '<b>Thread-safety</b>'

# Find negative thread-safety documentation
grep -rn -E '\* <b>Thread-safety</b>:.*not thread-safe' --include="*.java" .
grep -rn -E '\* <b>Thread-safety</b>:.*No\.' --include="*.java" .
```

**Examples**:
```java
// ✅ CORRECT - Thread-safe class with proper bold format
/**
 * Cache for parsed configurations.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ConfigurationCache { ... }

// ✅ CORRECT - Immutable class with proper bold format
/**
 * Immutable configuration record.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public final class Config { ... }

// ✅ CORRECT - Non-thread-safe class omits thread-safety section
/**
 * Builder for Config instances.
 * <p>
 * Provides a fluent API for configuration.
 */
public final class ConfigBuilder { ... }

// ❌ VIOLATION - Not using bold format
/**
 * Cache for parsed configurations.
 * <p>
 * Thread-safety: This class is thread-safe.
 */
public final class ConfigurationCache { ... }

// ❌ VIOLATION - Using "Thread-safe" instead of "Thread-safety"
/**
 * Cache for parsed configurations.
 * <p>
 * <b>Thread-safe</b>: This class is thread-safe.
 */
public final class ConfigurationCache { ... }

// ❌ VIOLATION - Documenting non-thread-safety
/**
 * Builder for Config instances.
 * <p>
 * <b>Thread-safety</b>: This class is not thread-safe.
 */
public final class ConfigBuilder { ... }
```

**Format specification**:
- Use `<b>Thread-safety</b>:` (HTML bold tags with "Thread-safety" spelling)
- Follow with either "This class is thread-safe." or "This class is immutable."
- Do NOT use "This class is immutable and thread-safe." - immutability implies thread-safety
- For test classes, use same concise format - no verbose explanations
- Only document for classes that ARE thread-safe

**Additional Detection Patterns**:
```bash
# Find redundant "immutable and thread-safe" documentation
grep -rn '<b>Thread-safety</b>:.*immutable and thread-safe' --include="*.java" .

# Find verbose test-specific explanations
grep -rn '<b>Thread-safety</b>:.*test method.*local variables' --include="*.java" .
grep -rn '<b>Thread-safety</b>:.*try-finally cleanup' --include="*.java" .
```

**Rationale**: Thread-safety documentation should be a positive assertion with consistent formatting. Absence
of thread-safety documentation implies the class is not thread-safe.

## TIER 3 QUALITY - Best Practices

### Error Handling - Default Return Values
**Violation**: Catching exceptions and returning default/fallback values instead of propagating errors (either
directly or through error-handling methods)
**Correct**: Let exceptions propagate to allow proper error handling by callers
**Example**:
```java
// VIOLATION - Silent failure with default return
public ASTNode parseExpression(TokenStream tokens) {
    try {
        return parseComplexExpression(tokens);
    } catch (ParseException e) {
        log.error("Parsing failed", e);
        return ASTNode.EMPTY; // Hides the failure
    }
}

// CORRECT - Fail fast with meaningful error
public ASTNode parseExpression(TokenStream tokens) {
    try {
        return parseComplexExpression(tokens);
    } catch (ValidationException e) {
        throw new ParseException("Invalid token stream: " + e.getMessage(), e);
    } catch (IndexOutOfBoundsException e) {
        throw new ParseException("Unexpected end of token stream", e);
    }
    // Other exceptions propagate naturally
}
```
**Rationale**: Parser operations must fail visibly when errors occur. Returning default values (empty nodes,
null results) can hide serious parsing errors and lead to incorrect AST construction.

**Manual Analysis Required**: For Pattern 2 matches, examine the called method to verify if it returns
default/ZERO values. Methods named `handle.*Error`, `.*ErrorHandler`, `default.*` are particularly suspect.

### Error Handling - Checked Exception Wrapping with RuntimeException
**Detection Pattern**: Look for `throw new RuntimeException` wrapping checked exceptions
**Violation**: Using generic `RuntimeException` to wrap checked exceptions in lambdas or streams
**Correct**: Use `WrappedCheckedException.wrap()` for explicit checked exception wrapping
**Detection Commands**:
```bash
# Find RuntimeException wrapping checked exceptions
grep -rn -A2 'throw new RuntimeException' --include="*.java" . | grep -B2 'catch.*Exception'
grep -rn 'throw new RuntimeException.*\(' --include="*.java" .
```
**Example**:
```java
// VIOLATION - Generic RuntimeException for checked exception
return cache.computeIfAbsent(path, p -> {
    try {
        return parser.parse(p);
    } catch (ConfigurationSyntaxException e) {
        throw new RuntimeException(e);
    }
});

// CORRECT - Explicit wrapped checked exception
return cache.computeIfAbsent(path, p -> {
    try {
        return parser.parse(p);
    } catch (ConfigurationSyntaxException e) {
        throw WrappedCheckedException.wrap(e);
    }
});
```
**Rationale**: `WrappedCheckedException` makes it explicit that this is a wrapped checked exception, provides
better stack trace handling, and helps distinguish between true programming errors and wrapped I/O exceptions.
**Manual Analysis**: Review each `RuntimeException` usage to determine if it's wrapping a checked exception.
Not all `RuntimeException` uses are violations - only those wrapping checked exceptions in lambda/stream
contexts.

### Concurrency - Virtual Thread Preference
**Detection Pattern**: `CompletableFuture\.supplyAsync\(\s*\(\)\s*->\s*.*\.(read|write|process)\(`
**Violation**: Using CompletableFuture for simple I/O operations like file reading
**Correct**: Use virtual thread executor for I/O-bound operations
**Example**:
```java
// VIOLATION - CompletableFuture for simple file read
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> Files.readString(path));

// CORRECT - Virtual thread executor
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var future = executor.submit(() -> Files.readString(path));
    return future.get();
}
```

### Exception Type Selection - IllegalStateException vs AssertionError
**Detection Pattern**: Look for exception throws in defensive programming contexts
**Critical Distinction**: Choose exception type based on WHO caused the failure

**Pattern 1 - Detecting User Error (IllegalStateException)**:
```bash
# Find state validation checks (user errors)
grep -rn "throw new IllegalStateException" --include="*.java" .
```
**Violation**: `throw new AssertionError("Configuration must be initialized before use");`
**Correct**: `throw new IllegalStateException("Configuration must be initialized before use");`

**Pattern 2 - Detecting Code Bugs (AssertionError)**:
```bash
# Find defensive invariant checks (internal bugs)
grep -rn "throw new IllegalStateException.*should" --include="*.java" .
```
**Violation**: `throw new IllegalStateException("Merged configuration should be valid", e);`
**Correct**: `throw new AssertionError("Merged configuration should be valid", e);`

**Examples**:
```java
// CORRECT - User violated API contract (IllegalStateException)
public void processConfiguration(RuleConfiguration config) {
    if (!isInitialized) {
        throw new IllegalStateException("Formatter must be initialized before use");
    }
    // Process config
}

// CORRECT - Internal invariant violated (AssertionError)
public RuleConfiguration merge(RuleConfiguration override) {
    try {
        return builder().withSettings(override.getSettings()).build();
    } catch (ConfigurationException e) {
        // Merging two valid configs should always produce valid result
        throw new AssertionError("Merged configuration should be valid", e);
    }
}
```

**Rationale**: `IllegalStateException` signals recoverable API usage errors. `AssertionError` signals
programming bugs indicating broken implementation invariants.

### Method Naming - Clarity Over Brevity
**Detection Pattern**: Look for method names with unclear abbreviations
**Common Abbreviations to Flag**:
- `*Nr` → prefer `*Number` (e.g., `getLineNr()` → `getLineNumber()`)
- `*Pos` → prefer `*Position` (e.g., `getTokenPos()` → `getTokenPosition()`)
- `*Len` → prefer `*Length` (e.g., `getStrLen()` → `getStringLength()`)
- `*Cnt` → prefer `*Count` (e.g., `getItemCnt()` → `getItemCount()`)
- `*Msg` → prefer `*Message` (e.g., `getErrMsg()` → `getErrorMessage()`)
- `*Src` → prefer `*Source` (e.g., `fmtSrc()` → `formatSource()`)
- `calc*` → prefer `calculate*` (e.g., `calcDepth()` → `calculateDepth()`)
- `fmt*` → prefer `format*` (e.g., `fmtCode()` → `formatCode()`)

**Detection Commands**:
```bash
# Find methods with common unclear abbreviations
grep -rn -E '(public|protected|private)\s+\w+\s+\w+Nr\(' --include="*.java" .
grep -rn -E '(public|protected|private)\s+\w+\s+\w+Pos\(' --include="*.java" .
grep -rn -E '(public|protected|private)\s+\w+\s+\w+Len\(' --include="*.java" .
grep -rn -E '(public|protected|private)\s+\w+\s+\w+Cnt\(' --include="*.java" .
grep -rn -E '(public|protected|private)\s+\w+\s+\w+Msg\(' --include="*.java" .
grep -rn -E '(public|protected|private)\s+\w+\s+\w+Src\(' --include="*.java" .
grep -rn -E '(public|protected|private)\s+\w+\s+calc[A-Z]' --include="*.java" .
grep -rn -E '(public|protected|private)\s+\w+\s+fmt[A-Z]' --include="*.java" .

# Find field accessors with abbreviated names
grep -rn -E 'get\w+Nr\(\)' --include="*.java" .
grep -rn -E 'get\w+Pos\(\)' --include="*.java" .
grep -rn -E 'get\w+Len\(\)' --include="*.java" .
```

**Exceptions** (do NOT flag these):
- Universal acronyms: `getURL()`, `parseHTML()`, `toJSON()`, `getHTTPResponse()`
- Java conventions: `toString()`, `hashCode()`, `equals()`
- Domain terms: `parseAST()`, `getJLSReference()`, `buildDOM()`
- Math conventions: `min()`, `max()`, `abs()`

**Example Violations**:
```java
// VIOLATION
public int getLineNr() { return lineNumber; }
public void calcMaxDepth() { ... }
public String fmtSrc(SourceFile file) { ... }

// CORRECT
public int getLineNumber() { return lineNumber; }
public void calculateMaximumDepth() { ... }
public String formatSource(SourceFile file) { ... }
```

**Rationale**: Clear method names improve code comprehension. Abbreviated names require mental translation and
reduce readability, especially during code review and debugging.

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

**Usage for Style Auditor**: Run all commands in parallel, collect results, generate detailed violation
reports with file locations and context.
