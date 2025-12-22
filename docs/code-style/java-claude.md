# Claude Java Style Guide - Detection Patterns

**File Scope**: `.java` files only
**Purpose**: Systematic violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### External Source Documentation - Missing References
**Detection Pattern**: Language specification references without source comments
**Violation**: `int maxDepth = 100; // No source reference`
**Correct**: `// Based on JLS §14.4 block statement nesting limits\nint maxDepth = 100;`

### JavaDoc Paragraph Tag - No Empty Lines Around &lt;p&gt;
**Detection Pattern 1**: `\* .*\n \*\n \* <p>` (blank line BEFORE `<p>` tag)
**Detection Pattern 2**: `<p>\n \*\n` (blank line AFTER `<p>` tag)
**Violation**: `/**\n * Summary line.\n *\n * <p>\n * Text.` or ` * <p>\n *\n * Text.`
**Correct**: `/**\n * Summary line.\n * <p>\n * Text.`
**Detection Commands**:
```bash
# Find blank line before <p>
grep -rn -B2 '^ \* <p>$' --include="*.java" . | grep -B1 '^ \*$'
# Find blank line after <p>
grep -rn -A1 '^ \* <p>$' --include="*.java" . | grep -A1 '^ \*$'
```
**Rationale**: The `<p>` tag should have no empty lines before or after it. It serves as a visual separator between paragraphs without requiring additional blank lines.

### JavaDoc Paragraph Tag - Must Not Be Last Element
**Detection Pattern**: `<p>\n \*/` (paragraph tag as final JavaDoc element)
**Violation**: ` * Some text.\n * <p>\n */`
**Correct**: ` * Some text.\n */` (remove trailing `<p>`)
**Detection Commands**:
```bash
# Find <p> as last element before closing */
grep -rn -A1 '^ \* <p>$' --include="*.java" . | grep ' \*/'
```
**Rationale**: A `<p>` tag at the end of JavaDoc serves no purpose - it creates an empty paragraph. Remove trailing `<p>` tags.

### JavaDoc Paragraph Tag - Must Be On Own Line
**Detection Pattern**: `<p>[A-Za-z]` (opening `<p>` tag immediately followed by text)
**Violation**: ` * <p>Example usage:`
**Correct**: ` * <p>\n * Example usage:`
**Detection Commands**:
```bash
# Find <p> tags with inline text
grep -rn '<p>[A-Za-z]' --include="*.java" .
```
**Rationale**: The `<p>` tag should appear on its own line for consistency and readability. Text content begins on the following line.

### JavaDoc - No Blank Line Before Member
**Detection Pattern**: ` \*/\n\n(public|private|protected|class|interface|enum|record)`
**Violation**: ` */\n\npublic class Foo`
**Correct**: ` */\npublic class Foo`
**Detection Commands**:
```bash
# Find blank line between JavaDoc and member (multiline grep)
grep -Pzo '\*/\n\n(public|private|protected)' --include="*.java" -r .
```
**Rationale**: JavaDoc must immediately precede the element it documents. A blank line breaks the association and may cause documentation tools to misinterpret the relationship.

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

### Inline Comments - No Duplication of JavaDoc
**Detection Pattern**: Inline comment repeating information from method/parameter JavaDoc
**Violation**: `this.arena = arena;  // May be null` (when JavaDoc says `@param arena ... (may be null)`)
**Correct**: `this.arena = arena;` (JavaDoc already documents nullability)
**Rationale**: Inline comments that duplicate JavaDoc create maintenance burden - if JavaDoc changes, the inline comment becomes stale or contradictory. Trust the JavaDoc as the source of truth.

**When inline comments ARE appropriate**:
- Implementation details not in JavaDoc (algorithm notes, performance considerations)
- Non-obvious code behavior that wouldn't belong in API docs
- Temporary notes (TODO, FIXME with ticket references)

### JavaDoc - Avoid @since Tags
**Detection Pattern**: `@since\s+`
**Violation**: `* @since 1.0`
**Correct**: (remove the @since tag entirely)
**Detection Commands**:
```bash
# Find @since tags in JavaDoc
grep -rn '@since' --include="*.java" .
```
**Rationale**: Version history is tracked in git commits and changelogs, not in source code. @since tags become stale, create maintenance burden, and duplicate information already available in version control.

### JavaDoc - Omit Redundant "(required)" When All Parameters Are Required
**Detection Pattern**: `@param.*\(required\)` when all non-primitive parameters have `(required)`
**Violation**: `@param filePath the file path (required)` when every parameter is required
**Correct**: `@param filePath the file path` (omit redundant marker)
**Rationale**: When all parameters are required (validated with `requireThat().isNotNull()`), marking each
one as "(required)" adds noise without information. Only annotate nullability when it varies - use
"(may be null)" for optional parameters to highlight exceptions to the default.

### JavaDoc - @throws for Null Arguments Should Not Exclude Primitives
**Detection Pattern**: `@throws.*if any.*except.*is null` mentioning primitive parameters
**Violation**: `@throws NullPointerException if any parameter except {@code enabled} is null` (where
`enabled` is a primitive boolean)
**Correct**: `@throws NullPointerException if any argument is null`
**Rationale**: Primitives cannot be null, so excluding them in `@throws` documentation is redundant and
confusing. Simply state "if any argument is null" - readers understand primitives are implicitly excluded.

### JavaDoc - Compact Constructors Must Have @param Tags
**Detection Pattern**: Record with compact constructor missing `@param` tags on the constructor itself
**Violation**:
```java
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
```
**Correct**:
```java
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
**Rationale**: JavaDoc `@param` tags from record declarations are NOT automatically inherited by compact
constructors. Checkstyle enforces JavaDoc on public constructors, which includes compact constructors.
Each compact constructor must have its own `@param` documentation.

### JavaDoc - Use "Returns" Not "Gets" for Accessor Methods
**Detection Pattern**: `^\s+\* Gets the` or `^\s+\* Gets a`
**Violation**: `/** Gets the file path. */`
**Correct**: `/** Returns the file path. */`
**Detection Commands**:
```bash
# Find "Gets the/a" at start of JavaDoc description
grep -rn '^\s*\* Gets [ta]' --include="*.java" .
```
**Rationale**: "Returns" is more precise for methods that return values. "Gets" implies retrieval from
somewhere, while "Returns" accurately describes what the method does - it returns a value to the caller.

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

// VIOLATION - Manual null + empty check (two separate checks)
requireThat(message, "message").isNotNull();
if (message.isEmpty()) {
    throw new IllegalArgumentException("message cannot be empty");
}

// CORRECT - requireThat() combined null + empty check
requireThat(message, "message").isNotEmpty();
```
**Rationale**: requireThat() provides consistent validation with better error messages, standardized naming,
and clearer intent. The validation library generates consistent error messages automatically and reduces
boilerplate code. Use `isNotEmpty()` for strings that must be non-null AND non-empty - it handles both in
one call.

### Requirements Library - Exception Types for @throws Documentation
**Detection Pattern**: `@throws IllegalArgumentException if.*is null` (when using `requireThat()`)
**Violation**: `@throws IllegalArgumentException if config is null`
**Correct**: `@throws NullPointerException if config is null`
**Key Rule**: Most `requireThat()` methods throw `NullPointerException` on null values.
**Exception Types**:
- **NullPointerException** (on null): All methods EXCEPT those listed below
- **IllegalArgumentException** (if validation fails): `isEqualTo()`, `isNotEqualTo()`, `isInstanceOf()`,
  `isNotInstanceOf()` - these methods accept null as valid input; they only throw if validation fails
- **IllegalArgumentException** (on invalid non-null value): Empty strings, out-of-range values, etc.
**Examples**:
```java
// VIOLATION - Wrong exception type
/**
 * @throws IllegalArgumentException if config is null
 */
public void configure(Config config)
{
    requireThat(config, "config").isNotNull();  // Actually throws NullPointerException!
}

// CORRECT - Matching exception type
/**
 * @throws NullPointerException if config is null
 */
public void configure(Config config)
{
    requireThat(config, "config").isNotNull();
}

// CORRECT - Multiple validations with different exception types
/**
 * @throws NullPointerException if message is null
 * @throws IllegalArgumentException if message is empty
 */
public Failure(String message)
{
    requireThat(message, "message").isNotNull();  // NullPointerException
    if (message.isEmpty())
    {
        throw new IllegalArgumentException("message cannot be empty");  // IllegalArgumentException
    }
}
```
**Rationale**: JavaDoc `@throws` must accurately document the actual exception type thrown. Mismatched
exception types mislead API consumers who may catch the wrong exception type.

### FormattingConfiguration Pattern - Validation and Documentation
**Detection Pattern**: Record implementing `FormattingConfiguration` with compact constructor
**Applies to**: All `*Configuration` records that implement `FormattingConfiguration`
**Required Pattern**:
1. Use `requireThat(ruleId, "ruleId").isNotBlank()` (NOT `.isNotNull()` - blank ruleId is invalid)
2. Document `@throws NullPointerException` and `@throws IllegalArgumentException` in record JavaDoc
3. Do NOT chain `.isNotNull().isNotBlank()` (redundant - `isNotBlank()` implies `isNotNull()`)
**Detection Commands**:
```bash
# Find FormattingConfiguration implementations with weak validation
grep -rn -A5 'implements FormattingConfiguration' --include="*.java" . | \
  grep -E 'isNotNull\(\)' | grep -v 'isNotBlank'

# Find missing @throws documentation
grep -rn -B20 'implements FormattingConfiguration' --include="*.java" . | \
  grep -L '@throws'
```
**Examples**:
```java
// VIOLATION - isNotNull() allows blank ruleId
public WhitespaceFormattingConfiguration
{
    requireThat(ruleId, "ruleId").isNotNull();
}

// VIOLATION - Redundant chain
public BraceFormattingConfiguration
{
    requireThat(ruleId, "ruleId").isNotNull().isNotBlank();  // isNotNull() is redundant
}

// VIOLATION - Missing @throws documentation
/**
 * @param ruleId the rule ID
 */
public record MyConfiguration(String ruleId) implements FormattingConfiguration { }

// CORRECT - Proper validation and documentation
/**
 * @param ruleId the rule ID
 * @throws NullPointerException     if {@code ruleId} is null
 * @throws IllegalArgumentException if {@code ruleId} is blank
 */
public record WhitespaceFormattingConfiguration(String ruleId, ...)
    implements FormattingConfiguration
{
    public WhitespaceFormattingConfiguration
    {
        requireThat(ruleId, "ruleId").isNotBlank();
    }
}
```
**Rationale**: Configuration ruleId values are used for identification and reporting. A blank ruleId is
semantically meaningless and would cause confusing output. The validation and documentation patterns must
be consistent across all FormattingConfiguration implementations.

### Defensive Copies - Create Once in Constructor
**Detection Pattern**: `return List.copyOf\(` or `return Collections.unmodifiable` in getter methods
**Violation**: Creating defensive copy on every getter call for immutable fields
```java
public List<StageResult> stageResults()
{
    return List.copyOf(stageResults);  // WRONG: Creates copy on every call
}
```
**Correct**: Create defensive copy once in constructor, return field directly in getter
```java
// Constructor
this.stageResults = List.copyOf(stageResults);  // Copy once

// Getter
public List<StageResult> stageResults()
{
    return stageResults;  // Return already-immutable list
}
```
**Rationale**: If a collection field is never modified after construction, creating a defensive copy on
every getter invocation wastes CPU and memory. Create the copy once in the constructor.

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

### Line Breaking - Break After Operators and Dots
**Detection Pattern**: `\n\s*\.(method|field)` (line starting with dot) or `\n\s*[+\-*/|&]` (line starting
with operator)
**Violation**: Line break BEFORE operator/dot
**Correct**: Line break AFTER operator/dot
**Detection Commands**:
```bash
# Find lines starting with dot (method chains breaking before dot)
grep -rn '^\s*\.' --include="*.java" .

# Find lines starting with binary operators
grep -rn -E '^\s*[+\-*/|&]{1,2}\s' --include="*.java" .
```
**Examples**:
```java
// VIOLATION - Break before dot
FileProcessingPipeline pipeline = FileProcessingPipeline.builder()
    .securityConfig(securityConfig)
    .formattingRules(rules)
    .build();

// CORRECT - Break after dot
FileProcessingPipeline pipeline = FileProcessingPipeline.builder().
    securityConfig(securityConfig).
    formattingRules(rules).
    build();

// VIOLATION - Break before operator
String result = longExpression
    + anotherExpression;

// CORRECT - Break after operator
String result = longExpression +
    anotherExpression;
```
**Rationale**: Breaking after operators/dots makes it immediately clear that the line continues. The trailing
operator/dot signals continuation; a leading operator/dot requires looking at the previous line for context.
**Applies to**: Source code AND JavaDoc code examples.

### Brace Placement - Opening Brace on New Line (Allman Style)
**Detection Pattern**: `\)\s*\{$` or `(class|interface|enum|record)\s+\w+.*\{$` (opening brace at end of line)
**Violation**: Opening brace at end of line (K&R/OTBS style)
**Correct**: Opening brace on its own line (Allman style)
**Detection Commands**:
```bash
# Find opening braces at end of line
grep -rn -E '\)\s*\{$' --include="*.java" .
grep -rn -E '(class|interface|enum|record)\s+\w+[^{]*\{$' --include="*.java" .
```
**Examples**:
```java
// VIOLATION - K&R style (brace at end of line)
public void processFile(Path path) {
    if (path.exists()) {
        // ...
    }
}

// CORRECT - Allman style (brace on new line)
public void processFile(Path path)
{
    if (path.exists())
    {
        // ...
    }
}

// VIOLATION - Class declaration
public class FileProcessor {

// CORRECT - Class declaration
public class FileProcessor
{
```
**Rationale**: Allman style provides clear visual separation between declarations and body. Each brace gets
its own line, making block boundaries unambiguous and improving vertical scanning.
**Applies to**: All blocks - classes, interfaces, methods, control structures, lambdas, initializers.

### Class Organization - User-Facing Members First
**Detection Pattern**: `(public|protected).*record\s+\w+.*\{.*\n.*public\s+\w+.*\(` (nested types before
public methods)
**Violation**: `public record Result(...) { ... }\npublic ReturnType publicMethod() { ... }`
**Correct**: `public ReturnType publicMethod() { ... }\npublic record Result(...) { ... }`
**Rationale**: Present API surface first to improve code readability and understanding

## TIER 2 IMPORTANT - Code Review

### Exception Declaration - Unreachable Throws in Final Classes
**Detection Pattern**: `public final class.*\{.*public.*throws.*Exception` where implementation cannot throw
**Violation**: Method in final class declares `throws` for exceptions the implementation never throws
**Correct**: Remove unreachable exception declarations from method signatures
**Detection Commands**:
```bash
# Find final classes with methods declaring throws
grep -rn -A20 'public final class' --include="*.java" . | grep 'throws'

# Manual review required: Check if implementation actually throws declared exceptions
# Look for methods that wrap checked exceptions or have evolved to not need them
```
**Examples**:
```java
// VIOLATION - Method declares IOException but implementation cannot throw it
public final class ConfigParser {
    public Config parse(String content) throws IOException {
        // Implementation only does string parsing, never touches I/O
        return new Config(content.split(","));
    }
}

// CORRECT - No unreachable throws declaration
public final class ConfigParser {
    public Config parse(String content) {
        return new Config(content.split(","));
    }
}

// CORRECT - Method actually throws the declared exception
public final class FileConfigParser {
    public Config parse(Path path) throws IOException {
        return new Config(Files.readString(path).split(","));
    }
}
```
**Rationale**: Unreachable exception declarations in final classes force callers to handle exceptions that can
never occur. Since final classes cannot be extended, there's no future subclass that might need the exception.
This creates unnecessary boilerplate and misleads API consumers about actual error conditions.
**Note**: This rule applies specifically to final classes. Non-final classes may legitimately declare
exceptions for subclass implementations even if the base implementation doesn't throw.

### Stream vs For-Loop - Inappropriate Stream Usage
**Detection Pattern**: `\.forEach\(System\.out::println\)`
**Violation**: `items.stream().forEach(System.out::println);`
**Correct**: `for (String item : items) System.out.println(item);`

### Exception Messages - Missing Business Context
**Detection Pattern**: `throw new.*Exception\("Invalid.*"\)`
**Violation**: `throw new IllegalArgumentException("Invalid amount");`
**Correct**: `throw new IllegalArgumentException("Withdrawal amount must be positive for account " + accountId);`

### Thread-Safety - Mutable Fields in Thread-Safe Classes
**Detection Pattern**: Class JavaDoc claims "thread-safe" but has mutable boolean fields (even `volatile`)
**Violation**: JavaDoc says "This class is thread-safe" but uses `private boolean closed;` or even
`private volatile boolean closed;` with check-then-act pattern
**Correct**: Use `private final AtomicBoolean closed = new AtomicBoolean();` with `compareAndSet()`
**Detection Commands**:
```bash
# Find classes claiming thread-safety
grep -rn -l 'thread-safe\|Thread-safety' --include="*.java" .

# For each, check for boolean fields (volatile or not) - both are violations for check-then-act
grep -A50 'thread-safe' <file> | grep 'private.*boolean.*;'
```
**Examples**:
```java
// VIOLATION - Claims thread-safety but uses plain boolean
/**
 * Scanner for classpath entries.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class Scanner implements AutoCloseable
{
    private boolean closed;  // NOT thread-safe!
}

// VIOLATION - volatile doesn't make check-then-act atomic
/**
 * Scanner for classpath entries.
 * <p>
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

// CORRECT - Uses AtomicBoolean with compareAndSet for atomic check-then-act
/**
 * Scanner for classpath entries.
 * <p>
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
**Rationale**: `volatile` only provides visibility (all threads see the same value), but does NOT provide
atomicity. The check-then-act pattern `if (!flag) { doSomething(); flag = true; }` has a race condition
even with `volatile` - two threads can both read `false`, both enter the block, then both set to `true`.
Use `AtomicBoolean.compareAndSet()` which atomically checks AND sets in one operation.

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

### SuppressWarnings - Minimal Scope for Unchecked Casts
**Detection Pattern**: `@SuppressWarnings("unchecked")` applied to method or class declarations
**Violation**: `@SuppressWarnings("unchecked")\npublic void process() {`
**Correct**: Apply to single local variable assignment: `@SuppressWarnings("unchecked") T result = (T) map.get(key);`
**Detection Commands**:
```bash
# Find @SuppressWarnings("unchecked") on methods or classes
grep -rn -B1 '@SuppressWarnings.*unchecked' --include="*.java" . | grep -E '(public|private|protected|void|class)'
```
**Examples**:
```java
// VIOLATION - Suppresses warnings for entire method
@SuppressWarnings("unchecked")
public <T> T getAttribute(NodeIndex index, Class<T> type)
{
    NodeAttribute attribute = attributes.get(index);
    if (type.isInstance(attribute))
        return (T) attribute;
    return null;
}

// CORRECT - Suppresses only the single cast expression
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
**Rationale**: Applying `@SuppressWarnings("unchecked")` to an entire method hides potentially unsafe casts
throughout the method body. Applying it to a single expression makes explicit exactly which cast is being
suppressed, improves auditability, and ensures new unchecked operations added later will still trigger warnings.

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

### Constants - Prefer Immutable Collections Over Arrays
**Detection Pattern**: `(private|public)\s+static\s+final\s+\w+\[\]\s+\w+\s*=\s*\{`
**Violation**: Using array literals for constant collections of objects
**Correct**: Use `List.of()`, `Set.of()`, or `Map.of()` for immutable constant collections
**Detection Commands**:
```bash
# Find array constant declarations (object types)
grep -rn -E 'static\s+final\s+String\[\]' --include="*.java" .
grep -rn -E 'static\s+final\s+\w+\[\]\s+\w+\s*=\s*\{' --include="*.java" .

# Find Arrays.asList() that could be List.of()
grep -rn 'Arrays\.asList(' --include="*.java" .

# Find new HashSet<>(Arrays.asList(...)) that could be Set.of()
grep -rn 'new HashSet.*Arrays\.asList' --include="*.java" .
```
**Examples**:
```java
// VIOLATION - Array constant
private static final String[] CONTROL_KEYWORDS = {
    "if", "else", "while", "for", "switch", "synchronized", "try", "catch", "do"
};

// CORRECT - Immutable List
private static final List<String> CONTROL_KEYWORDS = List.of(
    "if", "else", "while", "for", "switch", "synchronized", "try", "catch", "do");

// VIOLATION - Arrays.asList for constant
private static final List<String> OPERATORS = Arrays.asList("+", "-", "*", "/");

// CORRECT - List.of()
private static final List<String> OPERATORS = List.of("+", "-", "*", "/");

// VIOLATION - HashSet with Arrays.asList for constant unique values
private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList("class", "interface", "enum"));

// CORRECT - Set.of()
private static final Set<String> RESERVED_WORDS = Set.of("class", "interface", "enum");

// VIOLATION - Map constant with put() calls
private static final Map<String, Integer> PRECEDENCE = new HashMap<>();
static {
    PRECEDENCE.put("+", 1);
    PRECEDENCE.put("*", 2);
}

// CORRECT - Map.of()
private static final Map<String, Integer> PRECEDENCE = Map.of("+", 1, "*", 2);
```
**Exception**: Primitive arrays (`int[]`, `char[]`, `byte[]`, etc.) - `List.of()` requires object types and
autoboxing has performance implications for large arrays. Use primitive arrays when performance matters.
**Rationale**: Immutable collections from factory methods are:
- Truly immutable (arrays can be modified even when `final`)
- More expressive of intent (constant data shouldn't change)
- Null-hostile (fail fast on null elements)
- More memory efficient (optimized internal representations)

### Performance - Cache Expensive Method Calls in Loops
**Detection Pattern**: Same expensive method called multiple times with identical arguments in a loop
**Violation**: Redundant calls to expensive methods (O(n) or worse) when result is already known
**Correct**: Call once at loop start, cache result, reuse cached value
**Detection Commands**:
```bash
# Find methods called multiple times in same loop body (manual review required)
grep -rn -A30 'for.*{' --include="*.java" . | grep -E '(\w+)\([^)]*\).*\1\([^)]*\)'

# Common expensive patterns to check
grep -rn 'isIn.*(' --include="*.java" . | grep -B5 -A5 'for\s*('
grep -rn 'contains.*(' --include="*.java" . | grep -B5 -A5 'for\s*('
```
**Examples**:
```java
// VIOLATION - isInLiteralOrComment() called 4 times per iteration with same arguments
for (int i = 0; i < sourceCode.length(); ++i) {
    if (isInLiteralOrComment(sourceCode, i))  // O(n) - scans from 0 to i
        continue;
    if (current == ',' && !isInLiteralOrComment(sourceCode, i))  // Redundant!
        checkComma(i);
    if (current == '(' && !isInLiteralOrComment(sourceCode, i))  // Redundant!
        checkParen(i);
}

// CORRECT - Call once, use result (or structure code so early-exit handles it)
for (int i = 0; i < sourceCode.length(); ++i) {
    if (isInLiteralOrComment(sourceCode, i))
        continue;  // If we get past this, we know we're NOT in literal/comment
    char current = sourceCode.charAt(i);
    checkComma(sourceCode, i, current);      // No redundant check needed
    checkParentheses(sourceCode, i, current); // No redundant check needed
}

// ALTERNATIVE - Cache when result needed multiple times in different branches
for (int i = 0; i < sourceCode.length(); ++i) {
    boolean inLiteralOrComment = isInLiteralOrComment(sourceCode, i);
    if (inLiteralOrComment && needsSpecialHandling(i))
        handleSpecial(i);
    else if (!inLiteralOrComment)
        processNormal(i);
}
```
**Rationale**: Expensive methods (string scanning, regex matching, collection searches) can dominate loop
performance when called redundantly. Structure code to call once and reuse, or use early-exit patterns that
make the result implicitly known.

### Performance - Cache Non-Trivial Method Return Values
**Detection Pattern**: Same method called multiple times with identical arguments when result doesn't change
**Violation**: Repeated calls to methods that allocate or compute (e.g., `toString()`, `toArray()`, `stream()`)
**Correct**: Assign result to variable once, reuse the variable
**Detection Commands**:
```bash
# Find repeated toString() calls (often in same method)
grep -rn '\.toString()' --include="*.java" . | awk -F: '{print $1}' | sort | uniq -c | sort -rn | head -20

# Find multiple StringBuilder.toString() in loops
grep -rn -A20 'for.*{' --include="*.java" . | grep 'result\.toString()'
```
**Examples**:
```java
// VIOLATION - 10 calls to result.toString() per iteration
for (int i = result.length() - 1; i >= 0; --i) {
    if (isStartOfComment(result.toString(), i)) continue;
    if (isEndOfComment(result.toString(), i)) continue;
    if (isInLiteral(result.toString(), i)) continue;
    if (isBinaryOperator(result.toString(), i)) { ... }
    if (isKeyword(result.toString(), i)) { ... }
    // Each toString() creates a new String object!
}

// CORRECT - Cache once per iteration, reuse
for (int i = result.length() - 1; i >= 0; --i) {
    String str = result.toString();  // One allocation per iteration
    if (isStartOfComment(str, i)) continue;
    if (isEndOfComment(str, i)) continue;
    if (isInLiteral(str, i)) continue;
    if (isBinaryOperator(str, i)) { ... }
    if (isKeyword(str, i)) { ... }
}

// VIOLATION - Repeated method call in tight loop
for (int i = 0; i < list.size(); ++i) {  // size() called N times
    process(list.get(i));
}

// CORRECT - Cache size
for (int i = 0, size = list.size(); i < size; ++i) {
    process(list.get(i));
}
```
**When caching is safe**: When the underlying data doesn't change between calls. If modifications occur
(e.g., `StringBuilder.insert()`), cache at iteration start and let loop iteration invalidate it naturally.
**Rationale**: Methods like `toString()`, `toArray()`, `stream()` allocate new objects. In loops, this can
create thousands of short-lived objects causing GC pressure and cache pollution.

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

### Class Naming - CamelCase for All Java Files Including Test Resources
**Detection Pattern**: Java files with non-CamelCase names (kebab-case, snake_case, lowercase)
**Violation**: `malformed-missing-brace.java`, `valid_simple.java`, `testinput.java`
**Correct**: `MalformedMissingBrace.java`, `ValidSimple.java`, `TestInput.java`
**Detection Commands**:
```bash
# Find Java files not starting with uppercase letter
find . -name "*.java" | grep -E '/[a-z].*\.java$'

# Find Java files with hyphens or underscores
find . -name "*.java" | grep -E '[-_].*\.java$'
```
**Rationale**: Java class names must match file names, and class names follow CamelCase convention. This
applies to ALL Java files including test fixtures and resources. Non-CamelCase file names cause compilation
errors or require workarounds that obscure intent.

### Naming - Acronyms as CamelCase Not ALL-CAPS
**Detection Pattern**: Class/interface/enum/record/method names with consecutive uppercase letters (2+ caps)
**Violation**: `AIOutputFormatter`, `HTTPClient`, `formatForAI()`, `parseJSON()`
**Correct**: `AiOutputFormatter`, `HttpClient`, `formatForAi()`, `parseJson()`
**Detection Commands**:
```bash
# Find class declarations with consecutive uppercase letters (acronyms)
grep -rn -E '(class|interface|enum|record)\s+[A-Z]+[A-Z][a-z]' --include="*.java" .

# Find class declarations starting with 2+ uppercase letters
grep -rn -E '(class|interface|enum|record)\s+[A-Z]{2,}' --include="*.java" .
```
**Examples**:
```java
// VIOLATION - ALL-CAPS acronyms
public interface AIOutputFormatter { }
public class HTTPClientFactory { }
public String formatForAI() { }
public void parseJSON() { }

// CORRECT - CamelCase acronyms
public interface AiOutputFormatter { }
public class HttpClientFactory { }
public String formatForAi() { }
public void parseJson() { }
```
**Rationale**: Treating acronyms as regular words in CamelCase improves readability and consistency. When
acronyms are ALL-CAPS, word boundaries become ambiguous (is it `XMLHTTP` or `XML` + `HTTP`?) and the visual
rhythm of CamelCase is broken. This applies to both type names and method names.

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
- Java conventions: `toString()`, `hashCode()`, `equals()`
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

### JavaDoc/Comments/Errors - Use "empty" Not "blank" for String Validation

**Detection Pattern**: `@throws.*if.*is blank|".*is blank"`

**What to Look For**: Documentation and error messages that say "is blank" when describing string validation
that rejects null/whitespace-only values.

```java
// VIOLATION - Uses "blank" in documentation
/**
 * @throws IllegalArgumentException if {@code name} is blank
 */

// CORRECT - Uses "empty" for user-facing documentation
/**
 * @throws IllegalArgumentException if {@code name} is empty
 */
```

**Rationale**: "Empty" is more universally understood to mean "contains no meaningful content" and is
preferred in user-facing documentation and error messages.

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
