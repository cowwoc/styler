# Claude Java Style Guide - Detection Patterns

**File Scope**: `.java` files only
**Purpose**: Systematic violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### External Source Documentation - Missing URLs
**Detection Pattern**: Tax calculations without source comments
**Violation**: `BigDecimal federalRate = new BigDecimal("0.15"); // No source`
**Correct**: `// Based on 2024 CRA tax brackets: https://canada.ca/tax-2024\nBigDecimal federalRate = new BigDecimal("0.15");`

### JavaDoc Exception Documentation - Missing @throws
**Detection Pattern**: `public.*throws.*Exception.*\{` without corresponding `@throws`
**Violation**: `public void process() throws ValidationException { // Missing @throws in JavaDoc`
**Correct**: `/**\n * @throws ValidationException when input validation fails\n */\npublic void process() throws ValidationException {`
**Rationale**: Exception documentation helps API consumers handle errors properly

### JavaDoc Parameter References - Missing {@code} Markup
**Detection Pattern**: `@throws.*if [a-zA-Z_][a-zA-Z0-9_]*( or [a-zA-Z_][a-zA-Z0-9_]*)* (is|are) null` without {@code}
**Violation**: `@throws IllegalArgumentException if taxPayer or taxYear are null`
**Correct**: `@throws IllegalArgumentException if {@code taxPayer} or {@code taxYear} are null`
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
- `record Person(\n    String name,\n    int age\n)` (unnecessary wrapping - fits on one line: ~35 chars)
- `new CorporateTaxResult(\n    federalTax, quebecTax, erdtohBalance, nerdtohBalance, gripBalance, lripBalance);` (underutilized first line - wasted ~25 chars)
- `method(param1,\n    param2, param3, param4, param5);` (poor distribution - could fit more on first line)

**Correct Examples**:
- `record Person(String name, int age)` (single line when under 120 chars)
- `new CorporateTaxResult(federalTax, quebecTax, erdtohBalance, nerdtohBalance, gripBalance,\n    lripBalance);` (line-filling: max params per line within limit)
- `calculateComplexTax(taxpayerId, corporationId, taxYear, federalRate,\n    quebecRate, exemptions, credits);` (wrap only when next param would exceed 120)

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
public TaxAmount calculateTax(TaxContext context) {
    try {
        return performComplexCalculation(context);
    } catch (Exception e) {
        log.error("Calculation failed", e);
        return TaxAmount.ZERO_CAD; // Hides the failure
    }
}

// CORRECT - Fail fast with meaningful error
public TaxAmount calculateTax(TaxContext context) {
    try {
        return performComplexCalculation(context);
    } catch (ValidationException e) {
        throw new TaxCalculationException("Invalid context: " + e.getMessage(), e);
    } catch (ArithmeticException e) {
        throw new TaxCalculationException("Mathematical error in calculation", e);
    }
    // Other exceptions propagate naturally
}
```
**Rationale**: Financial calculations must fail visibly when errors occur. Returning default values (zero tax, empty results) can hide serious calculation errors and lead to incorrect financial decisions.

**Manual Analysis Required**: For Pattern 2 matches, examine the called method to verify if it returns default/ZERO values. Methods named `handle.*Error`, `.*ErrorHandler`, `default.*` are particularly suspect.

### Concurrency - Virtual Thread Preference
**Detection Pattern**: `CompletableFuture\.supplyAsync\(\s*\(\)\s*->\s*.*\.send\(`
**Violation**: Using CompletableFuture for simple I/O operations like HTTP requests
**Correct**: Use virtual thread executor for I/O-bound operations
**Example**:
```java
// VIOLATION - CompletableFuture for simple HTTP call
CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> client.send(request));

// CORRECT - Virtual thread executor
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var future = executor.submit(() -> client.send(request));
    return future.get();
}
```

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