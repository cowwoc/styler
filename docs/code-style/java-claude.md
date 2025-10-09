# Claude Java Style Guide - Detection Patterns

**File Scope**: `.java` files only
**Purpose**: Systematic violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### External Source Documentation - Missing References
**Detection Pattern**: Language specification references without source comments
**Violation**: `int maxDepth = 100; // No source reference`
**Correct**: `// Based on JLS §14.4 block statement nesting limits\nint maxDepth = 100;`

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

### Parameter Formatting - Multi-line Declarations and Calls
**Detection Pattern 1**: `(record|public\s+\w+).*\([^)]*\n.*,` (multi-line parameter declarations)
**Detection Pattern 2**: `(new\s+\w+|[a-zA-Z_]\w*)\([^)]*\n` (multi-line parameter lists requiring efficiency analysis)

**Multi-Step Detection for Pattern 2**:
1. **Find multi-line parameter constructs**: `grep -n "([^)]*\n" *.java`
2. **Extract line content and measure**: Calculate actual character usage per line
3. **Apply line-filling efficiency rules**:
   - Each line should be filled to maximum capacity within 120-character limit
   - Parameters should wrap to next line only when adding them would exceed 120 chars
   - Lines with <50% utilization when combination is possible → VIOLATION
   - Empty first lines (opening paren + newline) with subsequent packed lines → VIOLATION

**Canonical Line-Filling Style**:
**Principle**: Maximize parameters per line while respecting 120-character limit. Break to next line only when adding the next parameter would exceed the limit.

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

**Rationale**: Maximize both horizontal and vertical space efficiency; avoid wasted lines and improve code density without sacrificing readability

### Class Organization - User-Facing Members First
**Detection Pattern**: `(public|protected).*record\s+\w+.*\{.*\n.*public\s+\w+.*\(` (nested types before public methods)
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

### Class Declaration - Missing final Modifier
**Detection Pattern**: `public\s+class\s+\w+\s+(extends\s+\w+\s*)?\{` (non-final classes)
**Violation**: `public class ArgumentParser {` (not extended anywhere)
**Correct**: `public final class ArgumentParser {`
**Rationale**: Classes not designed for extension should be declared final to prevent unintended subclassing and make design intent explicit
**Exception**: Abstract classes, base classes explicitly designed for extension, or classes with protected methods intended for overriding

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

**Rationale**: Thread-safety documentation should be a positive assertion with consistent formatting. Absence of thread-safety documentation implies the class is not thread-safe.

## TIER 3 QUALITY - Best Practices

### Error Handling - Default Return Values
**Violation**: Catching exceptions and returning default/fallback values instead of propagating errors (either directly or through error-handling methods)
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
**Rationale**: Parser operations must fail visibly when errors occur. Returning default values (empty nodes, null results) can hide serious parsing errors and lead to incorrect AST construction.

**Manual Analysis Required**: For Pattern 2 matches, examine the called method to verify if it returns default/ZERO values. Methods named `handle.*Error`, `.*ErrorHandler`, `default.*` are particularly suspect.

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
**Rationale**: `WrappedCheckedException` makes it explicit that this is a wrapped checked exception, provides better stack trace handling, and helps distinguish between true programming errors and wrapped I/O exceptions.
**Manual Analysis**: Review each `RuntimeException` usage to determine if it's wrapping a checked exception. Not all `RuntimeException` uses are violations - only those wrapping checked exceptions in lambda/stream contexts.

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

**Rationale**: `IllegalStateException` signals recoverable API usage errors. `AssertionError` signals programming bugs indicating broken implementation invariants.

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

**Rationale**: Clear method names improve code comprehension. Abbreviated names require mental translation and reduce readability, especially during code review and debugging.

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