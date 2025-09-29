# Java Style Guide - Human Understanding

**Purpose**: Conceptual understanding and rationale for Java coding standards
**Companion**: See `java-claude.md` for optimized detection patterns and commands

## 📋 Table of Contents

- [🧠 Why These Rules Matter](#why-these-rules-matter)
- [🚨 TIER 1 CRITICAL - Build Blockers](#tier-1-critical---build-blockers)
- [⚠️ TIER 2 IMPORTANT - Code Review](#tier-2-important---code-review)
- [💡 TIER 3 QUALITY - Best Practices](#tier-3-quality---best-practices)
- [📚 Integration with Detection](#integration-with-detection)

---

## 🧠 Why These Rules Matter

This codebase implements Styler, a Java code formatter that processes and transforms source code. Code clarity and correctness are critical because:
- **Parse accuracy**: Errors in AST construction could corrupt source code or lose developer intent
- **Performance requirements**: Code formatters must process large codebases efficiently with minimal latency
- **Maintainability**: Multiple developers need to understand complex parsing and formatting logic

## 🚨 TIER 1 CRITICAL - Build Blockers

### External Source Documentation - Missing URLs
**Why this matters**: Parser implementations must be traceable to official Java Language Specification sources. This rule ensures language compliance and makes JDK updates easier.

**Compliance requirement**: Parser logic without documented JLS sources creates correctness risk and makes code reviews difficult.

**Implementation benefits**: Clear JLS references help developers understand parsing decisions, validate edge cases, and maintain compatibility with language evolution.

### JavaDoc Exception Documentation - Missing @throws
**Critical for API safety**: Parser and formatter methods often throw parsing exceptions. API consumers need to understand what exceptions to handle and why.

**Real-world scenario**: Parser methods may throw `ParseException` for malformed syntax, `TransformationException` for formatting errors, or `LanguageException` when encountering unsupported language features.

**Best practice**: Document all checked exceptions with specific conditions and recovery strategies.

### JavaDoc Parameter References - Missing {@code} Markup
**Why {@code} markup matters**: JavaDoc generates better documentation when parameter names are semantically marked with `{@code}`. This creates visual distinction between parameter names and descriptive text, improving API documentation readability.

**IDE integration benefits**: Modern IDEs recognize `{@code}` markup and provide better code completion, cross-referencing, and navigation when browsing JavaDoc.

**Standard practice**: Using `{@code}` for parameter references follows established JavaDoc conventions used throughout the Java ecosystem and major libraries.

**Parser API context**: AST construction and formatting APIs with complex parameter validation benefit from clear parameter identification in exception documentation.

### Parameter Formatting - Multi-line Declarations and Calls
**Line-filling principle**: Multi-parameter constructs (records, constructor calls, method calls) should maximize horizontal space usage within the 120-character limit. Each line should be filled to capacity before wrapping to the next line, avoiding unnecessary vertical bloat.

**Parsing context**: AST parsing methods like `parseExpression(TokenStream tokens, ParseContext context)` and records like `SourceRange(int start, int end, int line)` are commonly used. Compact formatting helps when reviewing multiple similar declarations.

**Guideline**: Keep parameter lists on same line unless they exceed reasonable line length (120+ characters). This applies to records, constructors, and method declarations equally.

**When to use multi-line**: When parameters exceed 120 characters, fill lines efficiently by grouping parameters that fit together rather than using one-parameter-per-line format. Example: `record Result(Type param1, Type param2,\n    Type param3, Type param4)` instead of individual lines.

**Method invocation formatting**: Apply the same line-length optimization to method calls. Use horizontal space efficiently rather than prematurely breaking lines:

```java
// ✅ PREFERRED - Efficient horizontal space usage
return new TaxResult(contextId, timestamp, federalTax, provincialTax, credits, deductions);

// ❌ AVOID - Premature line breaking when space is available  
return new TaxResult(
    contextId,
    timestamp,
    federalTax,
    provincialTax);
```

### Class Organization - User-Facing Members First
**Why API-first organization matters**: When reviewing parser classes, developers first want to understand the public interface - what methods are available and what they return. Presenting the API surface before implementation details improves code comprehension.

**Reading pattern optimization**: Code is read more often than written. Placing public methods before their supporting nested types follows the natural reading pattern of understanding "what does this class do" before "how does it work internally."

**Forward reference support**: Java allows methods to reference nested classes declared later in the same compilation unit, making this organization technically feasible.

**Parser API context**: AST parsing classes often have complex result types. Seeing `parseStatement()` method first, then `ParseResult` record below, creates a logical flow from usage to implementation.

## ⚠️ TIER 2 IMPORTANT - Code Review

### Stream vs For-Loop - Inappropriate Stream Usage
**When to use streams**: Complex transformations, filtering, or reductions of AST node collections.
**When to use loops**: Simple iteration, I/O operations, or when performance is critical.

**Parser context**: Processing large collections of tokens or AST nodes benefits from streams, but simple operations should use clear loops.

### Exception Messages - Missing Business Context
**Why parsing context matters**: Parse exceptions need enough context for developers to understand syntax errors, not just generic failures.

**Good parsing exception**: "Unexpected token 'void' at position 45, expected identifier in method declaration"

## 💡 TIER 3 QUALITY - Best Practices

### Error Handling - Default Return Values
**Parser system reliability**: Returning default values when parsing fails can mask serious errors in code formatters. Parse operations must fail visibly so developers can identify and resolve syntax issues before they affect code transformation.

**Silent failure risks**: 
- Returning `ASTNode.EMPTY` on parse errors could result in incorrect code transformations
- Returning empty collections hides data retrieval failures 
- Null returns without clear error indication leave callers uncertain about system state

**Fail-fast approach**: Let parse exceptions propagate to calling systems where appropriate error handling, logging, and user notification can occur. This ensures errors are handled at the right abstraction level.

### Concurrency - Virtual Thread Preference
**Java 24 optimization**: Betty targets Java 24 and should leverage virtual threads for I/O-bound operations instead of CompletableFuture for simple cases.

**Why virtual threads for Betty**: Claude API integration involves extensive HTTP communication and file I/O operations. Virtual threads provide better resource utilization and simpler code patterns for these blocking operations.

**Implementation guidance**:
```java
// ✅ PREFERRED - Virtual threads for I/O operations
public ClaudeResponse sendMessage(ClaudeRequest request) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        var future = executor.submit(() -> httpClient.send(request));
        return future.get();
    }
}

// ❌ AVOID - CompletableFuture for simple I/O  
public CompletableFuture<ClaudeResponse> sendMessage(ClaudeRequest request) {
    return CompletableFuture.supplyAsync(() -> httpClient.send(request));
}
```

**When CompletableFuture is still appropriate**: Complex async coordination (combining multiple API calls, timeout handling with sophisticated retry logic), CPU-bound operations, or when integrating with existing async APIs.

## 📚 Navigation

### Related Documentation
- **[Common Practices](common-human.md)**: Universal principles applying to all languages
- **[TypeScript Style Guide](typescript-human.md)**: Type-safe frontend development
- **[Testing Conventions](testing-human.md)**: Testing patterns, parallel execution, and JPMS structure
- **[Master Style Guide](../code-style-human.md)**: Complete overview and philosophy

### Claude Detection Patterns
- **[Java Detection Patterns](java-claude.md)**: Automated rule detection patterns

This human guide provides the conceptual foundation. For specific violation patterns and systematic checking, Claude uses the companion detection file. Together, they ensure both understanding and consistent enforcement of parser code quality standards.