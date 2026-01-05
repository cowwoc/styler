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

This codebase implements Styler, a Java code formatter that processes and transforms source code. Code clarity
and correctness are critical because:
- **Parse accuracy**: Errors in AST construction could corrupt source code or lose developer intent
- **Performance requirements**: Code formatters must process large codebases efficiently with minimal latency
- **Maintainability**: Multiple developers need to understand complex parsing and formatting logic

## 🚨 TIER 1 CRITICAL - Build Blockers

### JavaDoc Paragraph Tag - No Empty Lines Around &lt;p&gt;
**Why this matters**: The `<p>` tag should have no empty JavaDoc lines before or after it. It serves as a visual paragraph separator without requiring additional blank lines.

**Practical example**:
```java
// ✅ CORRECT - No blank lines around <p>
/**
 * Represents a potential line break location within source code.
 * <p>
 * This record is immutable and thread-safe, containing all metadata needed to
 * evaluate and apply a line break.
 */

// ❌ VIOLATION - Blank line before <p>
/**
 * Represents a potential line break location within source code.
 *
 * <p>
 * This record is immutable and thread-safe.
 */

// ❌ VIOLATION - Blank line after <p>
/**
 * Represents a potential line break location.
 * <p>
 *
 * This record is immutable and thread-safe.
 */
```

**Why consistency matters**: Following standard JavaDoc formatting makes documentation more readable and ensures proper rendering in JavaDoc HTML output and IDE documentation views.

### JavaDoc Paragraph Tag - Must Not Be Last Element
**Why this matters**: A `<p>` tag at the end of JavaDoc creates an empty paragraph that serves no purpose. Remove any trailing `<p>` tags.

**Practical example**:
```java
// ✅ CORRECT - No trailing <p>
/**
 * Processes the input file.
 * <p>
 * Returns the formatted result.
 */

// ❌ VIOLATION - Trailing <p> with no content
/**
 * Processes the input file.
 * <p>
 */
```

**Why this matters**: Empty trailing paragraphs add visual noise in generated documentation and indicate incomplete documentation.

### JavaDoc Paragraph Tag - Must Be On Own Line
**Why this matters**: The `<p>` tag in JavaDoc should appear on its own line, with the paragraph content starting on the following line. This improves readability and maintains consistent formatting.

**Practical example**:
```java
// ✅ CORRECT - <p> on its own line
/**
 * Processes the input file.
 * <p>
 * Example usage:
 * <pre>
 * processor.process(file);
 * </pre>
 */

// ❌ VIOLATION - Text immediately after <p>
/**
 * Processes the input file.
 * <p>Example usage:
 * <pre>
 * processor.process(file);
 * </pre>
 */
```

**Why this matters**: Placing `<p>` on its own line creates visual separation and makes the structure of JavaDoc comments easier to scan. It also aligns with the broader formatting style where block-level elements stand alone.

### JavaDoc - No Blank Line Before Member
**Why this matters**: JavaDoc must immediately precede the element it documents. A blank line between the closing `*/` and the declaration breaks the association and may cause documentation tools to misinterpret the relationship.

**Practical example**:
```java
// ✅ CORRECT - No blank line between JavaDoc and member
/**
 * Parses the source file.
 */
public void parse(Path path)
{
    // ...
}

// ❌ VIOLATION - Blank line breaks the association
/**
 * Parses the source file.
 */

public void parse(Path path)
{
    // ...
}
```

**Why this matters**: Many documentation generators and IDEs use the physical proximity of JavaDoc to determine which element it documents. A blank line can cause orphaned documentation or documentation attached to the wrong element.

### JavaDoc - Avoid @since Tags
**Why this matters**: Version history is tracked in git commits and changelogs, not in source code. `@since` tags become stale, create maintenance burden, and duplicate information already available in version control.

**Practical example**:
```java
// ❌ VIOLATION - @since tag in JavaDoc
/**
 * Formats source code according to style rules.
 *
 * @since 1.0
 */
public void format(SourceFile file)

// ✅ CORRECT - No @since tag (use git history instead)
/**
 * Formats source code according to style rules.
 */
public void format(SourceFile file)
```

**Why this matters**: When a method is modified after its introduction, the `@since` tag doesn't capture this evolution. Git history provides complete information about when code was added and how it changed over time.

### JavaDoc - Omit Redundant "(required)" When All Parameters Are Required
**Why this matters**: When all parameters are required (validated with `requireThat().isNotNull()`), marking each one as "(required)" adds noise without information. Only annotate nullability when it varies.

**Practical example**:
```java
// ❌ VIOLATION - Redundant "(required)" on every parameter
/**
 * Creates a new formatter.
 *
 * @param config the configuration (required)
 * @param rules the formatting rules (required)
 * @param options the processing options (required)
 */
public Formatter(Config config, Rules rules, Options options)

// ✅ CORRECT - Omit redundant markers when all are required
/**
 * Creates a new formatter.
 *
 * @param config the configuration
 * @param rules the formatting rules
 * @param options the processing options
 */
public Formatter(Config config, Rules rules, Options options)
```

**When to annotate nullability**: Use "(may be null)" for optional parameters to highlight exceptions to the default assumption that parameters are required.

### JavaDoc - @throws for Null Arguments Should Not Exclude Primitives
**Why this matters**: Primitives cannot be null, so excluding them in `@throws` documentation is redundant and confusing. Simply state "if any argument is null" - readers understand primitives are implicitly excluded.

**Practical example**:
```java
// ❌ VIOLATION - Unnecessarily excludes primitive parameter
/**
 * @param enabled whether the feature is enabled
 * @param config the configuration
 * @throws NullPointerException if any parameter except {@code enabled} is null
 */
public void configure(boolean enabled, Config config)

// ✅ CORRECT - Simple statement (primitives implicitly excluded)
/**
 * @param enabled whether the feature is enabled
 * @param config the configuration
 * @throws NullPointerException if any argument is null
 */
public void configure(boolean enabled, Config config)
```

### JavaDoc - Compact Constructors Must Have @param Tags
**Why this matters**: JavaDoc `@param` tags from record declarations are NOT automatically inherited by compact constructors. Checkstyle enforces JavaDoc on public constructors, which includes compact constructors. Each compact constructor must have its own `@param` documentation.

**Practical example**:
```java
// ❌ VIOLATION - Compact constructor without @param tags
/**
 * Configuration for type resolution.
 *
 * @param classpathEntries paths to JAR files
 * @param modulepathEntries paths to modules
 */
public record TypeResolutionConfig(List<Path> classpathEntries, List<Path> modulepathEntries)
{
    // Compact constructor WITHOUT @param tags - VIOLATION
    public TypeResolutionConfig
    {
        classpathEntries = List.copyOf(classpathEntries);
    }
}

// ✅ CORRECT - Compact constructor with its own @param tags
/**
 * Configuration for type resolution.
 *
 * @param classpathEntries paths to JAR files
 * @param modulepathEntries paths to modules
 */
public record TypeResolutionConfig(List<Path> classpathEntries, List<Path> modulepathEntries)
{
    /**
     * Creates a new type resolution configuration.
     *
     * @param classpathEntries paths to JAR files
     * @param modulepathEntries paths to modules
     * @throws NullPointerException if any argument is null
     */
    public TypeResolutionConfig
    {
        classpathEntries = List.copyOf(classpathEntries);
    }
}
```

### JavaDoc - Use "Returns" Not "Gets" for Accessor Methods
**Why this matters**: "Returns" is more precise for methods that return values. "Gets" implies retrieval from somewhere, while "Returns" accurately describes what the method does - it returns a value to the caller.

**Practical example**:
```java
// ❌ VIOLATION - Using "Gets" in method description
/**
 * Gets the file path.
 */
public Path filePath()

// ✅ CORRECT - Using "Returns" for accessor methods
/**
 * Returns the file path.
 */
public Path filePath()
```

### Inline Comments - No Duplication of JavaDoc
**Why this matters**: Inline comments that duplicate JavaDoc create maintenance burden. If JavaDoc changes, the inline comment becomes stale or contradictory. Trust the JavaDoc as the source of truth.

**Practical example**:
```java
// ❌ VIOLATION - Inline comment duplicates JavaDoc
/**
 * @param arena the node arena (may be null)
 */
public Parser(NodeArena arena)
{
    this.arena = arena;  // May be null
}

// ✅ CORRECT - No redundant inline comment
/**
 * @param arena the node arena (may be null)
 */
public Parser(NodeArena arena)
{
    this.arena = arena;
}
```

**When inline comments ARE appropriate**:
- Implementation details not in JavaDoc (algorithm notes, performance considerations)
- Non-obvious code behavior that wouldn't belong in API docs
- Temporary notes (TODO, FIXME with ticket references)

### External Source Documentation - Missing URLs
**Why this matters**: Parser implementations must be traceable to official Java Language Specification
sources. This rule ensures language compliance and makes JDK updates easier.

**Compliance requirement**: Parser logic without documented JLS sources creates correctness risk and makes
code reviews difficult.

**Implementation benefits**: Clear JLS references help developers understand parsing decisions, validate edge
cases, and maintain compatibility with language evolution.

### JavaDoc Exception Documentation - Missing @throws
**Critical for API safety**: Parser and formatter methods often throw parsing exceptions. API consumers need
to understand what exceptions to handle and why.

**Real-world scenario**: Parser methods may throw `ParseException` for malformed syntax,
`TransformationException` for formatting errors, or `LanguageException` when encountering unsupported language
features.

**Best practice**: Document all checked exceptions with specific conditions and recovery strategies.

### JavaDoc Parameter References - Missing {@code} Markup
**Why {@code} markup matters**: JavaDoc generates better documentation when parameter names are semantically
marked with `{@code}`. This creates visual distinction between parameter names and descriptive text, improving
API documentation readability.

**IDE integration benefits**: Modern IDEs recognize `{@code}` markup and provide better code completion,
cross-referencing, and navigation when browsing JavaDoc.

**Standard practice**: Using `{@code}` for parameter references follows established JavaDoc conventions used
throughout the Java ecosystem and major libraries.

**Parser API context**: AST construction and formatting APIs with complex parameter validation benefit from
clear parameter identification in exception documentation.

### Validation - Use requireThat() Instead of Manual Checks
**Why requireThat() is preferred**: The `requireThat()` validation library from cowwoc/requirements provides
consistent, expressive validation with better error messages than manual if-throw patterns. It reduces
boilerplate and improves code clarity.

**Parser validation context**: Parser components validate input ranges, token positions, and structural
constraints. Using `requireThat()` makes these validations uniform and self-documenting.

**Key benefits**:
- **Consistent error messages**: Standardized format across all validation failures
- **Clearer intent**: Method names like `.isGreaterThanOrEqualTo()` are self-documenting
- **Less boilerplate**: No need to construct error messages manually
- **Better diagnostics**: Automatic inclusion of actual values in error messages

**Practical examples**:
```java
// ✅ PREFERRED - requireThat() validation
requireThat(end, "end").isGreaterThanOrEqualTo(start, "start");
requireThat(source, "source").isNotNull();
requireThat(position, "position").isNotNegative();

// ❌ AVOID - Manual validation with if-throw
if (end < start) {
    throw new IllegalArgumentException("end must be >= start, got: start=" + start + ", end=" + end);
}
if (source == null) {
    throw new NullPointerException("source must not be null");
}
if (position < 0) {
    throw new IllegalArgumentException("position must be non-negative, got: " + position);
}
```

**When manual validation is still appropriate**: Complex business logic validation that requires custom error
messages with domain-specific context beyond simple parameter comparisons.

### FormattingConfiguration Pattern - Validation and Documentation
**What**: All record types implementing `FormattingConfiguration` must follow consistent validation patterns.

**Required pattern**:
1. Use `requireThat(ruleId, "ruleId").isNotBlank()` - not just `.isNotNull()`, because a blank ruleId is
   semantically invalid
2. Document `@throws NullPointerException` and `@throws IllegalArgumentException` in the record's JavaDoc
3. Don't chain `.isNotNull().isNotBlank()` - it's redundant since `.isNotBlank()` already checks for null

**Why this matters**: Configuration ruleId values appear in violation reports and logs. A blank ruleId would
produce confusing output like `Rule '' reported: ...`. Consistent validation and documentation across all
FormattingConfiguration implementations ensures predictable behavior and accurate API documentation.

**Example**:
```java
/**
 * @param ruleId the rule ID
 * @throws NullPointerException     if {@code ruleId} is null
 * @throws IllegalArgumentException if {@code ruleId} is blank
 */
public record WhitespaceFormattingConfiguration(String ruleId, boolean spaceAfterComma)
    implements FormattingConfiguration
{
    public WhitespaceFormattingConfiguration
    {
        requireThat(ruleId, "ruleId").isNotBlank();
    }
}
```

### Defensive Copies - Create Once in Constructor
**Why this matters**: If a collection field is never modified after construction, creating a defensive copy on every getter invocation wastes CPU and memory. Create the copy once in the constructor and return the immutable field directly in the getter.

**Practical example**:
```java
// ❌ VIOLATION - Creates defensive copy on every getter call
public List<StageResult> stageResults()
{
    return List.copyOf(stageResults);  // WRONG: Allocates new list each call
}

// ✅ CORRECT - Create defensive copy once in constructor
// Constructor
public Pipeline(List<StageResult> stageResults)
{
    this.stageResults = List.copyOf(stageResults);  // Copy once
}

// Getter
public List<StageResult> stageResults()
{
    return stageResults;  // Return already-immutable list
}
```

**Why this matters**: Defensive copying is essential for immutability, but the cost should be paid once at construction time, not on every access. This is especially important in hot paths where getters are called frequently.

### Parameter Formatting - Multi-line Declarations and Calls
**Line-filling principle**: Multi-parameter constructs (records, constructor calls, method calls) should
maximize horizontal space usage within the 120-character limit. Each line should be filled to capacity before
wrapping to the next line, avoiding unnecessary vertical bloat.

**Parsing context**: AST parsing methods like `parseExpression(TokenStream tokens, ParseContext context)` and
records like `SourceRange(int start, int end, int line)` are commonly used. Compact formatting helps when
reviewing multiple similar declarations.

**Guideline**: Keep parameter lists on same line unless they exceed reasonable line length (120+ characters).
This applies to records, constructors, and method declarations equally.

**When to use multi-line**: When parameters exceed 120 characters, fill lines efficiently by grouping
parameters that fit together rather than using one-parameter-per-line format. Example: `record Result(Type
param1, Type param2,\n Type param3, Type param4)` instead of individual lines.

**Method invocation formatting**: Apply the same line-length optimization to method calls. Use horizontal
space efficiently rather than prematurely breaking lines:

```java
// ✅ PREFERRED - Efficient horizontal space usage
return new ParseResult(astRoot, tokens, comments, errors, metadata, sourcePositions);

// ❌ AVOID - Premature line breaking when space is available
return new ParseResult(
    astRoot,
    tokens,
    comments,
    errors);
```

### Line Breaking - Break After Operators and Dots
**Why trailing operators/dots**: When a line ends with an operator or dot, it immediately signals that the
expression continues on the next line. In contrast, when a line starts with an operator/dot, you must look
back at the previous line to understand the context.

**Visual scanning benefit**: Reading code top-to-bottom, a trailing dot or operator tells you "this isn't
complete yet" before you even start the next line. A leading dot requires re-evaluating the previous line.

**Method chain readability**:
```java
// ❌ AVOID - Break before dot (common but less clear)
FileProcessingPipeline pipeline = FileProcessingPipeline.builder()
    .securityConfig(securityConfig)
    .formattingRules(rules)
    .build();

// ✅ PREFERRED - Break after dot (trailing dot signals continuation)
FileProcessingPipeline pipeline = FileProcessingPipeline.builder().
    securityConfig(securityConfig).
    formattingRules(rules).
    build();
```

**Consistency with other languages**: Many style guides prefer trailing operators for line continuation
(Python's PEP 8 historically, C++ style guides). This aligns with the principle that line endings should
signal whether the statement is complete.

**JavaDoc code examples**: Apply the same rule to code examples in JavaDoc to maintain consistency between
documentation and actual code.

### Brace Placement - Opening Brace on New Line (Allman Style)
**Why Allman style**: Opening braces on their own line creates clear visual separation between declarations
and their bodies. Each block boundary is unambiguous, and code scans vertically without brace-hunting.

**Visual alignment benefit**: With Allman style, opening and closing braces align vertically at the same
indentation level, making it trivial to match block boundaries:
```java
// ✅ PREFERRED - Allman style (braces align vertically)
public void processFile(Path path)
{
    if (path.exists())
    {
        // block content clearly delineated
    }
}

// ❌ AVOID - K&R style (closing brace misaligns from opening)
public void processFile(Path path) {
    if (path.exists()) {
        // harder to match block boundaries
    }
}
```

**Applies consistently**: Use Allman style for all blocks including:
- Class, interface, enum, record declarations
- Method and constructor bodies
- Control structures (if, else, for, while, switch)
- Try-catch-finally blocks
- Lambda expressions with block bodies
- Static and instance initializers

### Class Organization - User-Facing Members First
**Why API-first organization matters**: When reviewing parser classes, developers first want to understand the
public interface - what methods are available and what they return. Presenting the API surface before
implementation details improves code comprehension.

**Reading pattern optimization**: Code is read more often than written. Placing public methods before their
supporting nested types follows the natural reading pattern of understanding "what does this class do" before
"how does it work internally."

**Forward reference support**: Java allows methods to reference nested classes declared later in the same
compilation unit, making this organization technically feasible.

**Parser API context**: AST parsing classes often have complex result types. Seeing `parseStatement()` method
first, then `ParseResult` record below, creates a logical flow from usage to implementation.

## ⚠️ TIER 2 IMPORTANT - Code Review

### Abstract Methods - Must Have JavaDoc
**Why this matters**: Abstract methods define contracts that implementations must fulfill. Without documentation, implementers must guess the expected behavior, preconditions, and postconditions. This leads to inconsistent implementations and bugs.

**Practical example**:
```java
// ❌ VIOLATION - Abstract method without JavaDoc
public abstract NodeIndex allocateNode(NodeType type, int start, int end);

// ✅ CORRECT - Abstract method with JavaDoc explaining contract
/**
 * Allocates a new node in the arena.
 *
 * @param type  the type of node to create
 * @param start the start position in the source code
 * @param end   the end position in the source code
 * @return the index of the newly created node
 * @throws NullPointerException     if {@code type} is null
 * @throws IllegalArgumentException if positions are negative
 */
public abstract NodeIndex allocateNode(NodeType type, int start, int end);
```

**What to document**: Every abstract method should document:
- What the method does (summary line)
- Parameter requirements (@param tags)
- Return value semantics (@return tag)
- Exceptions that may be thrown (@throws tags)

### Exception Declaration - Unreachable Throws in Final Classes
**Why this matters**: When a final class declares that a method throws an exception that the implementation
cannot actually throw, it creates unnecessary burden on API consumers. They must write try-catch blocks or
declare throws clauses for exceptions that will never occur.

**Final class significance**: This rule applies specifically to final classes because they cannot be
extended. With non-final classes, a method might legitimately declare a throws clause to allow subclasses
to throw that exception, even if the base implementation doesn't. Final classes have no such consideration.

**API evolution context**: This situation often arises when:
- A method signature was designed for flexibility but the final implementation doesn't need it
- Code was refactored to handle exceptions internally but the signature wasn't updated
- A method was copied from a non-final class without adjusting the signature

**Practical examples**:
```java
// ❌ VIOLATION - Unreachable exception declaration
public final class StringConfigParser {
    public Config parse(String content) throws IOException {
        // This implementation does pure string parsing
        // IOException can never be thrown here
        return new Config(content.split(","));
    }
}

// ✅ CORRECT - No unreachable declaration
public final class StringConfigParser {
    public Config parse(String content) {
        return new Config(content.split(","));
    }
}

// ✅ CORRECT - Exception is actually reachable
public final class FileConfigParser {
    public Config parse(Path path) throws IOException {
        // Files.readString can throw IOException
        return new Config(Files.readString(path).split(","));
    }
}
```

**Caller impact**: Unreachable throws declarations force callers to write dead code:
```java
// Caller forced to handle exception that never occurs
try {
    Config config = parser.parse(jsonString);
} catch (IOException e) {
    // This catch block can never execute - dead code
    throw new RuntimeException("Impossible", e);
}
```

**When throws declarations are appropriate in final classes**: Only when the implementation actually can
throw the declared exception through direct throws statements or by calling methods that throw.

### Stream vs For-Loop - Inappropriate Stream Usage
**When to use streams**: Complex transformations, filtering, or reductions of AST node collections.
**When to use loops**: Simple iteration, I/O operations, or when performance is critical.

**Parser context**: Processing large collections of tokens or AST nodes benefits from streams, but simple
operations should use clear loops.

### Exception Messages - Missing Business Context
**Why parsing context matters**: Parse exceptions need enough context for developers to understand syntax
errors, not just generic failures.

**Good parsing exception**: "Unexpected token 'void' at position 45, expected identifier in method
declaration"

### Class Declaration - Missing final Modifier
**Why final matters for design clarity**: Classes should be explicitly marked `final` unless they are designed
for extension. This makes design intent clear and prevents accidental inheritance.

**Parser API context**: Most formatter and parser classes are implementation details that shouldn't be
extended. Marking them `final` prevents misuse and signals they are complete, standalone implementations.

**Design principle**: Inheritance requires careful design - protected methods, documented extension points,
stable contracts. Most classes don't need this complexity. Making them `final` is the safe default.

**When NOT to use final**: Abstract classes, explicit base classes with documented extension points, classes
with protected methods designed for overriding, or framework integration points requiring subclassing.

**Practical example**:
```java
// ✅ CORRECT - Implementation class not designed for extension
public final class ArgumentParser {
    // Complete, standalone implementation
}

// ✅ CORRECT - Designed for extension with protected hooks
public class BaseFormatter {
    protected void preProcess(SourceFile file) { }
}
```

### SuppressWarnings - Minimal Scope for Unchecked Casts
**Why minimal scope matters**: Applying `@SuppressWarnings("unchecked")` to an entire method hides all
unchecked cast warnings in that method, including any new ones introduced later. This defeats the purpose
of the warning system and makes code harder to audit for type safety issues.

**The single-expression rule**: Always apply `@SuppressWarnings("unchecked")` to a single local variable
assignment, not to a method or class declaration. This makes explicit exactly which cast is being suppressed.

**Benefits of minimal scope**:
- **Auditability**: Easy to search for and review each suppressed cast
- **Safety net**: New unchecked casts will still trigger warnings
- **Documentation**: The suppression acts as a marker for the specific unsafe operation
- **Code review**: Reviewers can evaluate each suppression individually

**Practical examples**:
```java
// ❌ WRONG - Suppresses warnings for entire method
@SuppressWarnings("unchecked")
public <T> T getAttribute(NodeIndex index, Class<T> type)
{
    NodeAttribute attribute = attributes.get(index);
    if (type.isInstance(attribute))
        return (T) attribute;
    return null;
}

// ✅ CORRECT - Suppresses only the single cast expression
public <T> T getAttribute(NodeIndex index, Class<T> type)
{
    NodeAttribute attribute = attributes.get(index);
    if (type.isInstance(attribute))
    {
        @SuppressWarnings("unchecked") T result = (T) attribute;
        return result;
    }
    return null;
}
```

### Thread-Safety - volatile Provides Visibility Not Atomicity
**Why this matters**: The `volatile` keyword only provides visibility (all threads see the same value), but does NOT provide atomicity. The check-then-act pattern `if (!flag) { doSomething(); flag = true; }` has a race condition even with `volatile` - two threads can both read `false`, both enter the block, then both set to `true`.

**Practical example**:
```java
// ❌ VIOLATION - volatile doesn't make check-then-act atomic
/**
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class Scanner implements AutoCloseable
{
    private volatile boolean closed;

    @Override
    public void close()
    {
        // RACE CONDITION: Two threads can both see closed=false,
        // both enter the block, both call resource.close()
        if (!closed)
        {
            resource.close();
            closed = true;
        }
    }
}

// ✅ CORRECT - Uses AtomicBoolean with compareAndSet for atomic check-then-act
/**
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class Scanner implements AutoCloseable
{
    private final AtomicBoolean closed = new AtomicBoolean();

    @Override
    public void close()
    {
        // THREAD-SAFE: Only one thread wins the compareAndSet race
        if (closed.compareAndSet(false, true))
        {
            resource.close();
        }
    }
}
```

**Key distinction**: Use `AtomicBoolean.compareAndSet()` which atomically checks AND sets in one operation.

### Field Initialization - Avoid Redundant Default Values
**Why this matters**: Java automatically initializes primitive fields to their default values (0 for numeric types, false for boolean, null for objects). Explicit assignment to default values is redundant and adds unnecessary code.

**Practical example**:
```java
// ❌ VIOLATION - Explicit default values in field declarations
private int position = 0;
private long count = 0L;
private boolean initialized = false;
private double value = 0.0;

// ✅ CORRECT - Rely on Java's default initialization
private int position;
private long count;
private boolean initialized;
private double value;

// ❌ VIOLATION - Redundant default assignments in constructor
public Parser(String source)
{
    this.arena = new NodeArena();
    this.position = 0;           // Redundant - int defaults to 0
    this.depth = 0;              // Redundant
    this.tokenCheckCounter = 0;  // Redundant
}

// ✅ CORRECT - Only assign non-default values
public Parser(String source)
{
    this.arena = new NodeArena();
    // position, depth, tokenCheckCounter automatically initialized to 0
}
```

**When explicit values are required**: Non-default initializations (e.g., `private int maxDepth = 100;`) are required and should not be removed. This rule only applies when the assigned value equals Java's default.

### Control Flow - Multiple OR Comparisons Should Use Switch
**Why this matters**: Switch statements with multiple case labels (comma-separated) are more concise, easier to maintain, and less error-prone than long chains of OR comparisons. The switch pattern makes it immediately clear that we're checking for membership in a set of values.

**Practical example**:
```java
// ❌ VIOLATION - Multiple OR comparisons
private boolean isAssignmentOperator(TokenType type)
{
    return type == TokenType.ASSIGN ||
        type == TokenType.PLUS_ASSIGN ||
        type == TokenType.MINUS_ASSIGN ||
        type == TokenType.STAR_ASSIGN ||
        type == TokenType.DIVIDE_ASSIGN ||
        type == TokenType.MODULO_ASSIGN;
}

// ✅ CORRECT - Switch statement
private boolean isAssignmentOperator(TokenType type)
{
    return switch (type)
    {
        case ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, STAR_ASSIGN, DIVIDE_ASSIGN, MODULO_ASSIGN -> true;
        default -> false;
    };
}
```

**When to apply**: Use this pattern when you have 3 or more equality comparisons against the same variable. For complex conditions (e.g., `x > 5 && x < 10`) or conditions on different variables, if-else may still be appropriate.

### Class Visibility - Prefer Non-Exported Packages Over Package-Protected
**Why this approach is preferred**: Instead of using package-private (default) visibility for implementation
classes, prefer declaring them as `public` in a package that is not exported by `module-info.java`. This
leverages JPMS (Java Platform Module System) for stronger encapsulation.

**Benefits of non-exported packages**:
- **Stronger encapsulation**: Module boundaries are enforced at compile-time AND runtime
- **Clearer visibility rules**: Package-private rules can be confusing across split packages
- **Better tooling support**: IDEs and build tools understand module boundaries better
- **Testability**: Test modules can access internal classes via `opens` directives without making them public API
- **Reflection control**: Non-exported packages prevent deep reflection by default

**Convention**: Use `.internal` suffix for implementation packages (e.g., `io.github.cowwoc.styler.formatter.internal`)

**Practical examples**:
```java
// ✅ PREFERRED - Public class in non-exported package
// In package: io.github.cowwoc.styler.formatter.internal
public final class WrapContext {
    // Implementation details hidden by module system
}

// In module-info.java:
module io.github.cowwoc.styler.formatter {
    exports io.github.cowwoc.styler.formatter;           // Public API
    // io.github.cowwoc.styler.formatter.internal NOT exported - hidden from consumers
}

// ❌ AVOID - Package-private class for encapsulation
// In package: io.github.cowwoc.styler.formatter
final class WrapContext {  // Package-private visibility
    // Relies only on Java's package-private rules
}
```

**When package-private is still acceptable**:
- Classes that are truly internal to a single package and will never be needed by tests
- Legacy code not yet modularized
- Very small helper classes used only within one file

### JavaDoc - Thread-Safety Documentation
**Document only actual thread-safe classes**: Only classes that are genuinely thread-safe should contain
thread-safety documentation in their JavaDoc. Non-thread-safe classes should omit this section entirely to
avoid confusion.

**Required Format**: Use `<b>Thread-safety</b>:` (bolded with HTML tags) followed by a brief description.

**Why this matters**: Thread-safety documentation is a positive assertion about concurrent usage guarantees.
Documenting that a class is NOT thread-safe creates noise and implies that thread-safety was even considered
as a design goal, which may not be the case for simple utility classes.

**Default assumption**: In the absence of thread-safety documentation, readers should assume a class is NOT
thread-safe unless there are obvious indicators (like using `ConcurrentHashMap` or synchronized methods).

**Practical examples**:
```java
// ✅ CORRECT - Thread-safe class with bold format
/**
 * Cache for parsed configuration files.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ConfigurationCache {
    private final ConcurrentMap<Path, Config> cache = new ConcurrentHashMap<>();
    // ...
}

// ✅ CORRECT - Immutable class with bold format
/**
 * Immutable configuration record.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public final class Config {
    private final int maxLineLength;
    // ...
}

// ✅ CORRECT - Non-thread-safe class omits thread-safety section
/**
 * Builder for constructing Config instances.
 * <p>
 * This builder provides a fluent API for setting configuration values.
 */
public final class ConfigBuilder {
    private Optional<Integer> maxLineLength = Optional.empty();
    // ...
}

// ❌ AVOID - Documenting that a class is NOT thread-safe
/**
 * Builder for constructing Config instances.
 * <p>
 * <b>Thread-safety</b>: This class is not thread-safe.
 */
public final class ConfigBuilder {
    // Unnecessary documentation noise
}

// ❌ AVOID - Not using bold format
/**
 * Cache for parsed configuration files.
 * <p>
 * Thread-safe: Yes. This class is thread-safe.
 */
public final class ConfigurationCache { ... }

// ❌ AVOID - Using "Thread-safe" instead of "Thread-safety"
/**
 * Cache for parsed configuration files.
 * <p>
 * <b>Thread-safe</b>: This class is thread-safe.
 */
public final class ConfigurationCache { ... }

// ❌ AVOID - Redundant "immutable and thread-safe"
/**
 * Configuration record.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class Config { ... }

// ❌ AVOID - Verbose test-specific explanations
/**
 * Test class for configuration loading.
 * <p>
 * <b>Thread-safety</b>: Each test method uses local variables and try-finally cleanup.
 */
public final class ConfigTest { ... }
```

**When to document thread-safety**: Only when the class is explicitly designed for concurrent access and
provides specific guarantees about thread-safe operations.

**Format specification**:
- Use `<b>Thread-safety</b>:` (note the HTML bold tags and "Thread-safety" spelling)
- Follow with either "This class is thread-safe." or "This class is immutable."
- For immutable classes, do NOT add "and thread-safe" - immutability implies thread-safety
- For test classes, use the same format as production classes - no verbose explanations
- Place after the main description and any `<p>` tags

## 💡 TIER 3 QUALITY - Best Practices

### Constants - Prefer Immutable Collections Over Arrays
**Why immutable collections matter**: Array constants (e.g., `static final String[]`) are not truly immutable -
any code with access can modify their contents. Using `List.of()`, `Set.of()`, or `Map.of()` factory methods
creates genuinely immutable collections that communicate intent and prevent accidental modification.

**Benefits of collection factory methods**:
- **True immutability**: Cannot be modified after creation (arrays only have a final reference)
- **Null-hostile**: Fail fast if null elements are passed (arrays accept nulls silently)
- **Memory efficient**: Optimized internal representations for small collections
- **Clearer intent**: Signals that data is constant and should not change

**Implementation guidance**:
```java
// ❌ AVOID - Array constant (mutable contents)
private static final String[] CONTROL_KEYWORDS = {
    "if", "else", "while", "for", "switch"
};

// ✅ PREFERRED - Immutable List
private static final List<String> CONTROL_KEYWORDS = List.of(
    "if", "else", "while", "for", "switch");

// ❌ AVOID - Arrays.asList (still backed by array)
private static final List<String> OPERATORS = Arrays.asList("+", "-", "*");

// ✅ PREFERRED - List.of()
private static final List<String> OPERATORS = List.of("+", "-", "*");

// ❌ AVOID - HashSet from Arrays.asList
private static final Set<String> RESERVED = new HashSet<>(Arrays.asList("class", "enum"));

// ✅ PREFERRED - Set.of() for unique values
private static final Set<String> RESERVED = Set.of("class", "enum");

// ❌ AVOID - Map with static block
private static final Map<String, Integer> PRECEDENCE = new HashMap<>();
static {
    PRECEDENCE.put("+", 1);
    PRECEDENCE.put("*", 2);
}

// ✅ PREFERRED - Map.of()
private static final Map<String, Integer> PRECEDENCE = Map.of("+", 1, "*", 2);
```

**Exception**: Primitive arrays (`int[]`, `char[]`, `byte[]`) - `List.of()` requires object types and
autoboxing has performance implications for large arrays. Use primitive arrays when performance matters.

### Performance - Cache Expensive Method Calls in Loops
**Why caching matters**: Methods that perform O(n) operations (string scanning, regex matching, collection
searches) can silently dominate loop performance when called redundantly with the same arguments. A loop
that appears O(n) becomes O(n²) when it calls an O(n) method on each iteration that could be cached.

**Common patterns to watch for**:
- `isInLiteralOrComment(source, position)` - scans from 0 to position each call
- `Pattern.matches()` / `String.matches()` - compiles regex each call
- `List.contains()` on large lists - O(n) linear scan
- Any method that scans/searches from the beginning

**Two refactoring approaches**:

1. **Early-exit pattern**: If you skip processing when a condition is true, code after the `continue`
   implicitly knows the condition was false:
```java
// Before: redundant checks
for (int i = 0; i < length; ++i) {
    if (isExpensive(i)) continue;
    if (x && !isExpensive(i)) doA();  // Redundant!
    if (y && !isExpensive(i)) doB();  // Redundant!
}

// After: early-exit makes result implicit
for (int i = 0; i < length; ++i) {
    if (isExpensive(i)) continue;
    // Past this point, isExpensive(i) is implicitly false
    if (x) doA();
    if (y) doB();
}
```

2. **Explicit caching**: When you need the result in multiple branches:
```java
for (int i = 0; i < length; ++i) {
    boolean cached = isExpensive(i);  // Call once
    if (cached && needsSpecialHandling(i))
        handleSpecial(i);
    else if (!cached)
        processNormal(i);
}
```

**Impact**: In a 10,000 character file, `isInLiteralOrComment()` called 4 times per character means 40,000
scans instead of 10,000 - a 4x slowdown that grows with file size.

### Performance - Cache Non-Trivial Method Return Values
**Why caching return values matters**: Methods like `toString()`, `toArray()`, and `stream()` allocate new
objects each time they're called. In loops, this creates thousands of short-lived objects that stress the
garbage collector and pollute CPU caches.

**Common offenders**:
- `StringBuilder.toString()` - creates new String each call
- `Collection.toArray()` - allocates new array each call
- `Collection.stream()` - creates stream pipeline objects
- `list.size()` in loop condition - harmless for ArrayList, expensive for LinkedList

**Safe caching pattern for mutable data**:
```java
// When StringBuilder is modified during iteration, cache at iteration start
for (int i = result.length() - 1; i >= 0; --i) {
    String str = result.toString();  // Cache once per iteration
    // Use str for all read-only operations
    if (someCheck(str, i)) {
        result.insert(i, ' ');  // Modification invalidates cache
        continue;               // Next iteration gets fresh cache
    }
}
```

**Impact**: In a formatter processing 10,000 characters with 10 method calls per iteration, caching reduces
100,000 String allocations to 10,000 - a 10x reduction in GC pressure.

### Error Handling - Default Return Values
**Parser system reliability**: Returning default values when parsing fails can mask serious errors in code
formatters. Parse operations must fail visibly so developers can identify and resolve syntax issues before
they affect code transformation.

**Silent failure risks**:
- Returning `ASTNode.EMPTY` on parse errors could result in incorrect code transformations
- Returning empty collections hides data retrieval failures
- Null returns without clear error indication leave callers uncertain about system state

**Fail-fast approach**: Let parse exceptions propagate to calling systems where appropriate error handling,
logging, and user notification can occur. This ensures errors are handled at the right abstraction level.

### Error Handling - Checked Exception Wrapping
**Use WrappedCheckedException instead of RuntimeException**: When wrapping checked exceptions in unchecked
exceptions (e.g., in lambdas or stream operations), use `WrappedCheckedException.wrap()` instead of generic
`RuntimeException`.

**Why this matters**: `WrappedCheckedException` provides better stack trace handling and makes it explicit
that this is a wrapped checked exception, not a genuine runtime failure. It helps distinguish between true
programming errors and wrapped I/O or other checked exceptions.

**Implementation guidance**:
```java
// ✅ PREFERRED - Explicit checked exception wrapping
return cache.computeIfAbsent(path, p -> {
    try {
        return parser.parse(p);
    } catch (ConfigurationSyntaxException e) {
        throw WrappedCheckedException.wrap(e);
    }
});

// ❌ AVOID - Generic RuntimeException for checked exceptions
return cache.computeIfAbsent(path, p -> {
    try {
        return parser.parse(p);
    } catch (ConfigurationSyntaxException e) {
        throw new RuntimeException(e);
    }
});
```

**When to use**: Lambda expressions, stream operations, or any context where checked exceptions cannot be
declared but need to be propagated.

### Concurrency - Virtual Thread Preference
**Java 25 optimization**: Styler targets Java 25 and should leverage virtual threads for I/O-bound operations
instead of CompletableFuture for simple cases.

**Why virtual threads for Styler**: File processing involves extensive file I/O operations. Virtual threads
provide better resource utilization and simpler code patterns for these blocking operations.

**Implementation guidance**:
```java
// ✅ PREFERRED - Virtual threads for I/O operations
public FormattingResult formatFile(Path filePath) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        var future = executor.submit(() -> processFile(filePath));
        return future.get();
    }
}

// ❌ AVOID - CompletableFuture for simple I/O
public CompletableFuture<FormattingResult> formatFile(Path filePath) {
    return CompletableFuture.supplyAsync(() -> processFile(filePath));
}
```

**When CompletableFuture is still appropriate**: Complex async coordination (combining multiple file
operations, timeout handling with sophisticated retry logic), CPU-bound parsing operations, or when
integrating with existing async APIs.

### Exception Type Selection - IllegalStateException vs AssertionError
**Critical semantic distinction**: The choice between these exception types communicates WHO made the mistake.

**IllegalStateException - User/Caller Error**:
- API contract violation by calling code
- Object used incorrectly (wrong state, wrong order)
- Invalid method call sequences
- Precondition failures caused by external usage

**Why this matters for Styler**: Parser and formatter APIs have strict usage contracts. Calling `apply()`
before `validate()`, or using a formatter after it's been closed, are user errors that should throw
`IllegalStateException`.

**AssertionError - Implementation Bug**:
- Internal invariant violated
- "This should be impossible" scenarios
- Defensive programming checks that should never fail
- Post-condition violations in correct code

**Why this matters for Styler**: When merging two valid configurations produces an invalid result, that's a
bug in the merge logic itself - not a user error. The implementation violated its own invariants.

**Practical examples**:
```java
// ✅ CORRECT - User violated API contract (IllegalStateException)
public void apply(FormattingContext context) {
    if (!isInitialized) {
        throw new IllegalStateException("Formatter must be initialized before applying rules");
    }
    // Apply formatting
}

// ✅ CORRECT - Implementation bug (AssertionError)
public RuleConfiguration merge(RuleConfiguration override) {
    try {
        return builder().withSettings(override.getSettings()).build();
    } catch (ConfigurationException e) {
        // If two valid configs can't merge, our merge logic is broken
        throw new AssertionError("Merged configuration should be valid", e);
    }
}
```

**Detection heuristic**: If the error message contains "should be" or "must be valid", it's likely describing
an invariant (use `AssertionError`). If it describes incorrect usage or state, use `IllegalStateException`.

### Method Naming - Clarity Over Brevity
**Why descriptive names matter**: Method names are read far more often than they are typed. A clear,
unambiguous method name improves code comprehension and reduces cognitive load when understanding parser and
formatter logic.

**Prefer full words over abbreviations**: Use complete words unless the abbreviation is universally understood
(like URL, HTML, HTTP). This makes code self-documenting and reduces the mental translation required when
reading.

**Parser and formatter context**: Configuration, parsing, and formatting APIs benefit from clear method names.
When debugging complex AST transformations, explicit method names like `getLineNumber()` are more immediately
understood than abbreviated forms like `getLineNr()`.

**Practical examples**:
```java
// ✅ PREFERRED - Clear, self-documenting names
public int getLineNumber() { return lineNumber; }
public void calculateMaximumDepth() { ... }
public String formatSourceCode(SourceFile file) { ... }
public Position getCurrentTokenPosition() { ... }

// ❌ AVOID - Unclear abbreviations requiring mental translation
public int getLineNr() { return lineNumber; }
public void calcMaxDepth() { ... }
public String fmtSrc(SourceFile file) { ... }
public Position getCurTokPos() { ... }
```

**When abbreviations are acceptable**:
- **Universal acronyms**: URL, HTML, HTTP, JSON, XML, UTF
- **Established Java conventions**: `toString()`, `hashCode()`, `equals()`
- **Domain-specific terms**: AST (Abstract Syntax Tree), JLS (Java Language Specification)
- **Mathematical conventions**: `min`, `max`, `abs`

**Reading vs Writing balance**: Modern IDEs provide excellent autocompletion. The slight typing overhead of
`getLineNumber()` versus `getLineNr()` is negligible compared to the readability improvement when reviewing
code months later.

**Codebase consistency**: When refactoring existing methods, prefer clarity. If a method name seems unclear
during code review, it's a good candidate for renaming to a more descriptive alternative.

### Class Naming - CamelCase for All Java Files
**Why this matters**: Java class names must match file names, and class names follow CamelCase convention. This applies to ALL Java files including test fixtures and resources. Non-CamelCase file names cause compilation errors or require workarounds that obscure intent.

**Practical example**:
```java
// ❌ VIOLATION - Non-CamelCase file names
malformed-missing-brace.java   // Kebab-case
valid_simple.java              // Snake_case
testinput.java                 // All lowercase

// ✅ CORRECT - CamelCase file names
MalformedMissingBrace.java
ValidSimple.java
TestInput.java
```

**Why this matters for test resources**: Even Java files used as test input (not compiled) should follow CamelCase. This maintains consistency across the codebase and avoids confusion about which naming conventions apply where.

### Naming - Acronyms as CamelCase Not ALL-CAPS
**Why this matters**: Treating acronyms as regular words in CamelCase improves readability and consistency. When acronyms are ALL-CAPS, word boundaries become ambiguous (is it `XMLHTTP` or `XML` + `HTTP`?) and the visual rhythm of CamelCase is broken.

**Practical example**:
```java
// ❌ VIOLATION - ALL-CAPS acronyms
public interface AIOutputFormatter { }
public class HTTPClientFactory { }
public String formatForAI() { }
public void parseJSON() { }

// ✅ CORRECT - CamelCase acronyms
public interface AiOutputFormatter { }
public class HttpClientFactory { }
public String formatForAi() { }
public void parseJson() { }
```

**This applies to**: Both type names (classes, interfaces, enums, records) and method names. The goal is consistent visual rhythm where each "word" in the name starts with exactly one capital letter.

### JavaDoc/Comments/Errors - Use "empty" Not "blank" for String Validation

**Why "empty" over "blank"**: When code uses `isNotBlank()` or similar methods that check for both null and
whitespace-only strings, documentation should use "empty" rather than "blank" in `@throws` clauses, error
messages, and comments. The term "empty" is more universally understood by users to mean "contains no
meaningful content."

**Scope**: This applies to JavaDoc `@throws` clauses, exception messages shown to users, and explanatory
comments.

**Example**:
```java
// ❌ WRONG - Uses "blank" in documentation
/**
 * @param name the user's name
 * @throws IllegalArgumentException if {@code name} is blank
 */
public void setName(String name)
{
    requireThat(name, "name").isNotBlank();
}

// ✅ CORRECT - Uses "empty" for user-facing documentation
/**
 * @param name the user's name
 * @throws IllegalArgumentException if {@code name} is empty
 */
public void setName(String name)
{
    requireThat(name, "name").isNotBlank();
}
```

**Note**: The method call `isNotBlank()` remains unchanged - only the documentation terminology changes.

## 📚 Navigation

### Related Documentation
- **[Common Practices](common-human.md)**: Universal principles applying to all languages
- **[TypeScript Style Guide](typescript-human.md)**: Type-safe frontend development
- **[Testing Conventions](testing-human.md)**: Testing patterns, parallel execution, and JPMS structure
- **[Master Style Guide](../code-style-human.md)**: Complete overview and philosophy

### Claude Detection Patterns
- **[Java Detection Patterns](java-claude.md)**: Automated rule detection patterns

This human guide provides the conceptual foundation. For specific violation patterns and systematic checking,
Claude uses the companion detection file. Together, they ensure both understanding and consistent enforcement
of parser code quality standards.
