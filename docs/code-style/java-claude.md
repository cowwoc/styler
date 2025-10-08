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