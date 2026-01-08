# Style Guide for Code Formatting

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** style, main agent (during VALIDATION)
> **Purpose:** Style validation requirements and JavaDoc documentation standards

## 🎯 COMPLETE STYLE VALIDATION {#complete-style-validation}

Style validation requires THREE components: checkstyle + PMD + manual rules.

Checking only checkstyle when PMD/manual violations exist is a CRITICAL ERROR.

`smart-doc-prompter.sh` hook injects 3-component checklist. `checkstyle/fixers` module handles LineLength vs UnderutilizedLines conflicts.

### Validation Components {#validation-components}

1. **Checkstyle** (automated tool):
   - Line length, indentation, naming conventions
   - JavaDoc requirements
   - Import organization
   - Whitespace rules

2. **PMD** (automated tool):
   - Code quality patterns
   - Unused code detection
   - Complexity metrics
   - Best practice enforcement

3. **Manual Rules** (documented in code-style-human.md):
   - TIER1 violations: Critical style issues
   - TIER2 violations: Important style issues
   - TIER3 violations: Minor style issues

### Validation Commands {#validation-commands}

```bash
# Full style validation (all three components)
./mvnw checkstyle:check pmd:check

# Individual components
./mvnw checkstyle:check
./mvnw pmd:check

# Review manual rules
cat docs/code-style-human.md
```

### Complete Validation Checklist {#complete-validation-checklist}

Before declaring style validation complete:

- [ ] Checkstyle passes: `./mvnw checkstyle:check`
- [ ] PMD passes: `./mvnw pmd:check`
- [ ] Manual rules reviewed from code-style-human.md
- [ ] All TIER1 violations fixed
- [ ] All TIER2 violations fixed
- [ ] All TIER3 violations fixed or documented

### PMD Suppression Policy {#pmd-suppression-policy}

**Only suppress PMD rules when there is a documented, legitimate reason.**

```java
// ❌ BAD: Lazy suppression without justification
@SuppressWarnings("PMD.UseEnumCollections")
Map<MyEnum, String> map = new HashMap<>();  // Should use EnumMap

// ✅ GOOD: Fix the issue instead of suppressing
Map<MyEnum, String> map = new EnumMap<>(MyEnum.class);

// ✅ ACCEPTABLE: Suppression with legitimate reason documented
@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")  // Magic number is array index, clear in context
if (args.length > 0) { ... }
```

**Valid suppression reasons:**
- False positive (PMD doesn't understand the context)
- Performance-critical code where PMD suggestion would hurt performance
- Interop with external API that requires specific pattern

**Invalid suppression reasons:**
- "Too much work to fix"
- "I don't understand why PMD flagged this"
- No reason provided

## 📝 JAVADOC MANUAL DOCUMENTATION REQUIREMENT {#javadoc-manual-documentation-requirement}

JavaDoc comments require manual authoring with contextual understanding.

### Absolutely Prohibited {#absolutely-prohibited}

❌ Scripts/automation (Python, Bash, sed/awk/grep) to generate JavaDoc
❌ Generic templates without customization
❌ AI-generated JavaDoc without human review and contextualization
❌ Batch processing JavaDoc across multiple files
❌ Converting method names to comments (e.g., "testValidToken" → "Tests Valid Token")

### Required Approach {#required-approach}

✅ Read and understand method's purpose and implementation
✅ Explain WHY the test exists, not just WHAT it tests
✅ Include context about edge cases, boundary conditions, or regression prevention
✅ Document relationships between related tests

### Contextual Documentation {#contextual-documentation}

**BAD** (Generic):
```java
/**
 * Tests valid token.
 */
@Test
public void testValidToken() {
    // ...
}
```

**GOOD** (Contextual, explains purpose):
```java
/**
 * Verifies that Token correctly stores all components (type, start, end, text) and
 * calculates length as the difference between end and start positions.
 *
 * This test validates the fundamental Token record semantics that other components
 * rely on for parsing and formatting operations.
 */
@Test
public void testValidToken() {
    // ...
}
```

### JavaDoc Formatting Rules {#javadoc-formatting-rules}

**Avoid @since Tags**: Do not use `@since` tags in JavaDoc. Version history is tracked in git commits and
changelogs, not source code. These tags become stale and duplicate information from version control.

**Paragraph Tags**: No empty line before `<p>` tags. The `<p>` tag should appear on the same line as the
preceding content's closing.

```java
// ❌ BAD: Empty line before <p>
/**
 * First paragraph.
 *
 * <p>
 * Second paragraph.
 */

// ✅ GOOD: No empty line before <p>
/**
 * First paragraph.
 * <p>
 * Second paragraph.
 */
```

**Thread-safety Documentation**: Thread-safety notes must appear at the END of the class JavaDoc, after all
other content including performance characteristics.

```java
// ❌ BAD: Thread-safety in the middle
/**
 * Description.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 * <p>
 * <b>Performance</b>: O(n) lookup.
 */

// ✅ GOOD: Thread-safety at the end
/**
 * Description.
 * <p>
 * <b>Performance</b>: O(n) lookup.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
```

**Identifier References**: Always use `{@code}` for identifiers in JavaDoc, including class names, method
names, parameter names, field names, and literals.

```java
// ❌ BAD: Plain text identifiers
/**
 * @throws NullPointerException if outputFormat or processingDuration is null
 * @param config the Parser configuration
 * @return the Token list
 */

// ✅ GOOD: {@code} wrapped identifiers
/**
 * @throws NullPointerException if {@code outputFormat} or {@code processingDuration} is {@code null}
 * @param config the {@code Parser} configuration
 * @return the {@code Token} list
 */
```

**Simplified @throws for All-Non-Null Parameters**: When all parameters must be non-null, use a single
concise statement instead of enumerating each parameter:

```java
// ❌ BAD: Enumerating when all are non-null
/**
 * @throws NullPointerException if any of {@code ruleId}, {@code severity}, {@code message},
 *                              {@code filePath}, or {@code suggestedFixes} is {@code null}
 */

// ✅ GOOD: Concise when all parameters are non-null
/**
 * @throws NullPointerException if any of the arguments are {@code null}
 */
```

Use enumeration only when some parameters allow null (to clarify which ones don't).

**Clear Terminology in @throws**: Use simple, understandable descriptions instead of technical jargon:

```java
// ❌ BAD: Technical jargon
/**
 * @throws IllegalArgumentException if {@code name} is empty or not stripped
 */

// ✅ GOOD: Clear terminology
/**
 * @throws IllegalArgumentException if {@code name} is empty or contains leading/trailing whitespace
 */
```

**Multiple Conditions in @throws**: When an exception has multiple conditions, use `<ul>` and `<li>`:

```java
// ❌ BAD: Run-on conditions
/**
 * @throws IllegalArgumentException if {@code ruleId} is empty, if {@code startPosition} is negative,
 *                                  if {@code endPosition} is less than {@code startPosition},
 *                                  or if {@code lineNumber} is not positive
 */

// ✅ GOOD: Structured list
/**
 * @throws IllegalArgumentException <ul>
 *                                    <li>if {@code ruleId} or {@code message} are empty</li>
 *                                    <li>if {@code startPosition} is negative</li>
 *                                    <li>if {@code endPosition} is less than {@code startPosition}</li>
 *                                    <li>if {@code lineNumber} is not positive</li>
 *                                  </ul>
 */
```

### Enforcement {#enforcement}

Pre-commit hook detects generic JavaDoc patterns. PMD.CommentRequired violations must be fixed, not suppressed.

## 📝 STYLE-SPECIFIC CODE POLICIES {#style-specific-code-policies}

### Code Comments {#code-comments}

Comments that contradict current implementation must be updated or removed. Implementation history belongs in git, not code comments.

```java
// ❌ BAD: "This used to use ArrayList but was changed to LinkedList for performance"
// ✅ GOOD: "Uses LinkedList for efficient insertions at arbitrary positions"
```

**Avoid obvious comments.** Only add comments that explain complex logic or explain WHY code does something.
Do not add comments that merely restate what self-explanatory code is doing.

```java
// ❌ BAD: Comments restate what the code obviously does
// Validate ruleId
requireThat(ruleId, "ruleId").isNotBlank();

// Validate groupOrder
requireThat(groupOrder, "groupOrder").isNotNull().isNotEmpty();

// Defensive copy
groupOrder = List.copyOf(groupOrder);

// ✅ GOOD: No comments needed - the code is self-explanatory
requireThat(ruleId, "ruleId").isNotBlank();
requireThat(groupOrder, "groupOrder").isNotNull().isNotEmpty();
groupOrder = List.copyOf(groupOrder);

// ✅ GOOD: Comment explains WHY, not WHAT
// Process in reverse order to preserve insertion positions during replacement
for (int i = matches.size() - 1; i >= 0; i--)
{
    // ...
}
```

### TODO Comments {#todo-comments}

**Three Options** (choose one):
1. **Implement**: Fix the TODO immediately
2. **Remove**: If TODO is no longer relevant
3. **Document**: Convert to tracked task in todo.md with full context

**PROHIBITED**: Superficial renaming without action
```java
// ❌ BAD: Renamed "TODO" to "FIXME" or "FUTURE" without addressing
// ✅ GOOD: Created todo.md task: "Optimize parse() method for large inputs"
```

### TestNG Tests {#testng-tests}

**Thread-Safe Patterns Only**:
- ❌ **Prohibited**: `@BeforeMethod` (creates shared mutable state)
- ❌ **Prohibited**: `@AfterMethod` (implies shared state needing cleanup)
- ❌ **Prohibited**: `@Test(singleThreaded = true)` (masks thread-safety issues)
- ✅ **Required**: Create fresh instances in each test method
- ✅ **Required**: Use try-finally for resource cleanup within each test

**No Stub Tests (All Forms Prohibited)**:
- ❌ **Prohibited**: `@Test(enabled = false)` - Disabled test is a stub
- ❌ **Prohibited**: Comments like `// This test will be implemented when...` - Placeholder comment is a stub
- ❌ **Prohibited**: Tests that only verify `isNotNull()` without testing actual behavior - Empty shell is a stub
- ✅ **Required**: Either implement the test fully or don't create it
- Tests waiting for "future functionality" ARE stubs - delete them entirely

```java
// ❌ BAD - Shared state
@BeforeMethod
public void setup() {
    parser = new Parser();  // Shared state
}

// ❌ BAD - Disabled test stub
@Test(enabled = false)
public void testFutureFeature() {
    // TODO: implement when feature is ready
}

// ❌ BAD - Placeholder comment stub
@Test
public void outputHandlerFormatsSuccessResultProperly() {
    OutputHandler handler = new OutputHandler();
    // This test will be implemented when OutputHandler is available
    requireThat(handler, "handler").isNotNull();  // Only verifies creation, not behavior
}

// ❌ BAD - Empty shell stub (only tests object creation)
@Test
public void testFormatting() {
    Formatter formatter = new Formatter();
    requireThat(formatter, "formatter").isNotNull();  // No actual behavior tested
}

// ✅ GOOD - Fresh instance, no disabled tests
@Test
public void testParse() {
    Parser parser = new Parser();  // Fresh instance per test
    // ...
}
```

**Test Assertions - Use requireThat()**:
- ❌ **Prohibited**: Manual if-throw patterns for assertions
- ✅ **Required**: Use `requireThat()` for all test assertions

`requireThat()` throws `IllegalArgumentException` instead of `AssertionError`, which is acceptable for test
assertions. The benefits of consistent validation syntax and descriptive failure messages outweigh the
semantic difference in exception type. TestNG reports both exception types as test failures.

```java
// ❌ BAD - Manual null check
if (message == null) {
    throw new AssertionError("Message should not be null");
}

// ❌ BAD - Manual value check
if (!result.contains("expected")) {
    throw new AssertionError("Result should contain expected");
}

// ✅ GOOD - Use requireThat()
requireThat(message, "message").isNotNull();
requireThat(result, "result").contains("expected");
```

**Avoid Redundant Validators** - Stronger validators imply weaker ones:
- ❌ `isNotNull().isNotEmpty()` - `isNotEmpty()` implies `isNotNull()`
- ❌ `isNotNull()` then `contains()` - `contains()` implies `isNotNull()`
- ✅ Use only the strongest validator needed

```java
// ❌ BAD - Redundant: isNotEmpty() already checks for null
requireThat(message, "message").isNotNull().isNotEmpty();

// ❌ BAD - Redundant: contains() already checks for null
requireThat(message, "message").isNotNull();
requireThat(message, "message").contains("expected");

// ✅ GOOD - Single strongest validator
requireThat(message, "message").isNotEmpty();
requireThat(message, "message").contains("expected");
```

**Trust Natural NPE from Method Calls** - Don't add explicit null checks when subsequent operations will naturally throw NPE:

The JVM provides clear NPE messages when calling methods on null objects. Explicit null checks before method calls that would fail anyway are redundant and add noise.

```java
// ❌ BAD - Redundant: qualifiedName() will throw NPE if attribute is null
requireThat(attribute, "attribute").isNotNull();
requireThat(attribute.qualifiedName(), "qualifiedName").isEqualTo("java.util.*");

// ✅ GOOD - Let natural NPE occur with clear stack trace
requireThat(attribute.qualifiedName(), "qualifiedName").isEqualTo("java.util.*");

// ❌ BAD - Redundant: isEmpty() will throw NPE if list is null
requireThat(list, "list").isNotNull();
requireThat(list.isEmpty(), "isEmpty").isFalse();

// ✅ GOOD - Natural NPE is clearer than explicit check
requireThat(list.isEmpty(), "isEmpty").isFalse();
```

**Don't Validate Before Calling Methods That Already Validate** - If you pass a parameter to a method that
validates it internally, don't validate the same parameter before the call:

```java
// Given a helper method that validates its parameter:
private void validateIndex(NodeIndex index)
{
    requireThat(index, "index").isNotNull();
    requireThat(index.index(), "index.index()").isLessThan(nodeCount);
}

// ❌ BAD - Redundant: validateIndex() already checks isNotNull()
public ImportAttribute getImportAttribute(NodeIndex index)
{
    requireThat(index, "index").isNotNull();  // REDUNDANT
    validateIndex(index);
    // ...
}

// ✅ GOOD - Let validateIndex() handle the validation
public ImportAttribute getImportAttribute(NodeIndex index)
{
    validateIndex(index);  // Already validates null
    // ...
}
```

**When copying patterns from existing methods**, check what the called helper methods validate to avoid
duplicating their validation.

**When explicit null checks ARE needed**:
- Method parameter validation in public APIs
- Checking fields that might legitimately be null
- Scenarios where NPE would be confusing (nested calls, complex expressions)

**Avoid Testing Compile-Time Constants** - Don't test what the compiler guarantees:
- ❌ Testing that an enum value equals its defined constant (tautology)
- ❌ Testing that a constant is positive/negative when defined that way
- ✅ Test relationships between values that could accidentally break
- ✅ Test uniqueness constraints (no accidental duplicates)
- ✅ Test integration with external systems that depend on specific values

```java
// ❌ BAD - Tautology: tests that 1 > 0
@Test
public void errorCodesArePositive() {
    requireThat(ExitCode.VIOLATIONS_FOUND.code(), "code").isGreaterThan(0); // Always true
}

// ❌ BAD - Duplicates enum definition
@Test
public void successExitCodeEqualsZero() {
    requireThat(ExitCode.SUCCESS.code(), "code").isEqualTo(0); // Enum already defines this
}

// ✅ GOOD - Tests intentional relationship that could break
@Test
public void successAndHelpShareSameCode() {
    requireThat(ExitCode.SUCCESS.code(), "SUCCESS").isEqualTo(ExitCode.HELP.code());
}

// ✅ GOOD - Tests uniqueness constraint
@Test
public void errorCodesAreUnique() {
    Set<Integer> codes = Arrays.stream(ExitCode.values())
        .filter(e -> e != ExitCode.HELP) // HELP intentionally duplicates SUCCESS
        .map(ExitCode::code)
        .collect(Collectors.toSet());
    requireThat(codes.size(), "unique codes").isEqualTo(ExitCode.values().length - 1);
}
```

### Exception Types {#exception-types}

Choose exception type based on cause:

- **AssertionError**: Valid input reaches impossible state (our bug/invariant violation)
  ```java
  if (state == State.READY && !isInitialized()) {
      throw new AssertionError("READY state but not initialized");
  }
  ```

  **Important**: Do NOT document `AssertionError` in JavaDoc `@throws` tags. Internal invariant violations
  indicate bugs in our code, not conditions callers should handle. Only document exceptions that represent
  expected failure modes from invalid input or state.

  ```java
  // ❌ BAD - Documents internal invariant violation
  /**
   * @throws AssertionError if attribute is missing
   */
  public void processNode(NodeIndex node) {
      ImportAttribute attr = arena.getImportAttribute(node);  // May throw AssertionError
  }

  // ✅ GOOD - No @throws for AssertionError
  /**
   * Processes the import node.
   */
  public void processNode(NodeIndex node) {
      ImportAttribute attr = arena.getImportAttribute(node);  // AssertionError not documented
  }
  ```

- **IllegalStateException**: Wrong API usage (user called method at wrong time)
  ```java
  if (!isStarted()) {
      throw new IllegalStateException("Must call start() before parse()");
  }
  ```

- **IllegalArgumentException**: Invalid input parameter
  ```java
  if (input == null) {
      throw new IllegalArgumentException("Input cannot be null");
  }
  ```

### Validation Method Chaining {#validation-method-chaining}

Combine `requireThat()` invocations for the same parameter into a single chained call:

```java
// ❌ BAD: Separate calls for same parameter
requireThat(tabWidth, "tabWidth").isGreaterThanOrEqualTo(MIN_TAB_WIDTH);
requireThat(tabWidth, "tabWidth").isLessThanOrEqualTo(MAX_TAB_WIDTH);

// ✅ GOOD: Combined into single chain
requireThat(tabWidth, "tabWidth").isGreaterThanOrEqualTo(MIN_TAB_WIDTH).isLessThanOrEqualTo(MAX_TAB_WIDTH);
```

This reduces redundancy and makes the validation constraints clearer at a glance.

**Avoid Redundant Validation Calls**: Some validation methods imply others. Don't chain methods where
one already covers another:

```java
// ❌ BAD: isNotBlank() already implies isNotEmpty()
requireThat(name, "name").isNotEmpty().isNotBlank();

// ✅ GOOD: isNotBlank() is sufficient
requireThat(name, "name").isNotBlank();
```

**Method implications**:
- `isNotBlank()` → implies `isNotEmpty()` (non-blank strings cannot be empty)
- `size().isEqualTo(n)` where n > 0 → implies `isNotEmpty()` (positive size means not empty)

### Extract Repeated Element Access {#extract-repeated-element-access}

**Extract repeated collection/array element access to a local variable.** When the same element is accessed
multiple times, extract it to a named local variable.

```java
// ❌ BAD: Repeated collection access
requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.STRING);
requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("hello");
requireThat(tokens.get(0).start(), "tokens.get(0).start()").isEqualTo(0);

// ✅ GOOD: Extract to local variable
Token token = tokens.get(0);
requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING);
requireThat(token.text(), "token.text()").isEqualTo("hello");
requireThat(token.start(), "token.start()").isEqualTo(0);
```

**Why this matters**:
- Improves readability by giving the accessed element a meaningful name
- Reduces visual noise from repeated index expressions
- Makes the code easier to modify (change index in one place)
- Can improve performance by avoiding redundant lookups (though this is usually minor)

**When to extract**: If the same element is accessed 2 or more times, extract it to a local variable.

**When NOT to extract**: Do NOT create local variables for single-use expressions. If an expression is
evaluated only once, call it directly inline.

```java
// ❌ WRONG - single use, no need for variable
Token boundToken = previousToken();
return arena.allocateNode(..., boundToken.end());

// ✅ CORRECT - single use, call directly
return arena.allocateNode(..., previousToken().end());

// ✅ CORRECT - used twice, variable is needed
Token wildcardToken = previousToken();
int start = wildcardToken.start();  // first use
...
return arena.allocateNode(..., wildcardToken.end());  // second use
```

**⚠️ CRITICAL: Trace Execution Paths First** {#extract-trace-execution-paths}

Before applying this rule, **trace all execution paths** to verify the index expression refers to the same
element in each path. Beware of:
- Index variables modified inside conditional blocks
- Loop iterations that change index state
- Method calls that mutate position/index state

```java
// ❌ INCOMPLETE FIX: Fails to recognize execution path differences
Token wildcardToken = tokens.get(position - 1);
int start = wildcardToken.start();

if (match(TokenType.EXTENDS) || match(TokenType.SUPER))
{
    parseType();  // ⚠️ This modifies `position`!
}
Token lastToken = tokens.get(position - 1);  // Different token if branch taken!
int end = lastToken.end();
return arena.allocateNode(NodeType.WILDCARD_TYPE, start, end);

// ✅ CORRECT: Return early in each branch to properly reuse variables
Token wildcardToken = tokens.get(position - 1);
int start = wildcardToken.start();

if (match(TokenType.EXTENDS) || match(TokenType.SUPER))
{
    parseType();  // position changes
    Token boundToken = tokens.get(position - 1);  // Different token
    return arena.allocateNode(NodeType.WILDCARD_TYPE, start, boundToken.end());
}
// Unbounded: reuse wildcardToken since position hasn't changed
return arena.allocateNode(NodeType.WILDCARD_TYPE, start, wildcardToken.end());
```

**Execution Path Verification Checklist**:
1. Identify all uses of the index expression (e.g., `tokens.get(position - 1)`)
2. For each use, trace backward: Can any code between uses modify the index variable?
3. If YES: The accesses refer to DIFFERENT elements - do NOT share a variable across paths
4. If NO: Safe to extract to a shared variable

**⚠️ CRITICAL: Check for Existing Helper Methods First** {#check-existing-helper-methods}

Before extracting to a local variable, **check if the class already has a helper method** for similar access
patterns. Follow existing patterns rather than creating ad-hoc solutions.

```java
// ❌ BAD: Ignoring existing pattern
// Class already has currentToken() returning tokens.get(position)
// But agent uses tokens.get(position - 1) repeatedly without checking for helpers
Token prevToken = tokens.get(position - 1);  // 50+ occurrences across class!

// ✅ GOOD: Check for existing helpers, follow the pattern
// 1. Search class for existing helpers: "private.*Token.*tokens.get"
// 2. Found: currentToken() returns tokens.get(position)
// 3. Create parallel helper: previousToken() returns tokens.get(position - 1)
private Token previousToken()
{
    return tokens.get(position - 1);
}
```

**Pre-Fix Checklist** (MANDATORY before applying extraction):
1. **Search for existing helpers**: Look for methods that access `tokens`, `list`, `array` etc.
2. **Identify naming patterns**: If `currentX()` exists, expect `previousX()` or `nextX()` pattern
3. **Count occurrences**: Use grep to count how many times the expression appears in the class/file
4. **Choose abstraction level**:
   - 2-3 occurrences in same method → Local variable
   - 3+ occurrences across multiple methods → Helper method
   - Existing helper pattern → Follow the pattern (create parallel helper)

**Why this matters**: Agents have repeatedly extracted to local variables while missing the opportunity to
create helper methods that follow existing patterns, leading to multiple fix iterations.

### Local Variable Type Inference (var) {#var-usage}

**`var` should be the exception, not the rule.** Only use `var` when it **significantly reduces verbosity**
by avoiding long generic type declarations, AND the type is clear from explicit types in the same or outer
block.

**The justification for `var` is reducing verbosity, not just "obvious" types.** If the explicit type is
short and readable, use it even when the type would be obvious with `var`.

```java
// ✅ GOOD: var significantly reduces verbosity, type clear from declaration
List<Map<Integer, String>> list = new ArrayList<>();
for (var element : list)
{
    // element is clearly Map<Integer, String> from list declaration
    // Writing Map<Integer, String> element would be redundant
}

// ❌ BAD: Type is obvious but var doesn't improve readability
var matcher = PATTERN.matcher(input);
// Use: Matcher matcher = PATTERN.matcher(input);
// "Matcher" is short - no verbosity benefit from var

// ❌ BAD: Type not immediately clear
var result = processData(input);  // What type is result?

// ❌ BAD: Simple types should be explicit
var count = 0;        // Use: int count = 0;
var name = "test";    // Use: String name = "test";
```

**When in doubt, use explicit types.** Explicit types improve code readability and make refactoring safer.

### String Whitespace Trimming {#string-whitespace-trimming}

**Use `strip()` instead of `trim()` when the string may contain Unicode whitespace.**

`String.trim()` only removes ASCII whitespace (characters ≤ U+0020), while `String.strip()` (Java 11+)
removes all Unicode whitespace characters including non-breaking spaces, ideographic spaces, and other
Unicode space separators.

```java
// ❌ BAD: trim() misses Unicode whitespace
String input = "\u00A0text\u2003";  // Non-breaking space + ideographic space
input.trim();  // Returns "\u00A0text\u2003" - unchanged!

// ✅ GOOD: strip() handles all Unicode whitespace
input.strip();  // Returns "text"
```

**When to use each**:
- `strip()` - Default choice for user input, file content, external data
- `trim()` - Only when specifically handling ASCII-only content (rare)

### Immutable Collections {#immutable-collections}

**Prefer `List.copyOf()` and `Map.copyOf()` over `Collections.unmodifiableList()` and
`Collections.unmodifiableMap()`.**

`Collections.unmodifiableX()` creates a view that prevents modification through the wrapper, but the
underlying collection can still be modified by the original reference. `List.copyOf()` and `Map.copyOf()`
create true immutable copies.

```java
// ❌ BAD: Unmodifiable wrapper - original list can still be modified
List<String> original = new ArrayList<>();
List<String> wrapped = Collections.unmodifiableList(original);
original.add("surprise");  // wrapped now contains "surprise"

// ✅ GOOD: True immutable copy - completely independent
List<String> original = new ArrayList<>();
List<String> copy = List.copyOf(original);
original.add("surprise");  // copy is unaffected
```

**When to use each**:
- `List.copyOf()` / `Map.copyOf()` - Default choice for defensive copies and immutable fields
- `Collections.unmodifiableX()` - Only when you intentionally want a live view (rare)

### Stream toList() {#stream-tolist}

**Use `toList()` instead of `collect(Collectors.toUnmodifiableList())`.**

Java 16+ provides `Stream.toList()` which returns an unmodifiable list. It's more concise and readable.

```java
// ❌ BAD: Verbose collector
List<String> names = items.stream()
    .map(Item::getName)
    .collect(Collectors.toUnmodifiableList());

// ✅ GOOD: Built-in terminal operation
List<String> names = items.stream()
    .map(Item::getName)
    .toList();
```

**Note**: `toList()` returns an unmodifiable list (like `List.of()`). If you need a mutable list, use
`collect(Collectors.toList())` or `toCollection(ArrayList::new)`.

### Import Types Instead of Using FQNs {#import-types}

**Always import types instead of using fully-qualified names (FQNs) in code.**

FQNs reduce readability and create visual clutter. Import statements exist to avoid repeating package names.

```java
// ❌ BAD: FQN in code
private java.util.Map<String, java.util.List<String>> data = new java.util.HashMap<>();

// ✅ GOOD: Proper imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// ...
private Map<String, List<String>> data = new HashMap<>();
```

**Exception**: Name conflicts where two classes have the same simple name (rare). In this case, import the
more frequently used one and use the FQN for the other.

### Optional Usage {#optional-usage}

**Use `Optional` only as a method return type, never as a method parameter.**

`Optional` was designed to represent "a value that may or may not be present" in return types. Using it as
a parameter creates unnecessary complexity and ambiguity.

```java
// ❌ BAD: Optional as parameter
public void processUser(Optional<String> nickname) {
    // Caller must wrap: processUser(Optional.of("John"))
    // Or pass empty: processUser(Optional.empty())
}

// ❌ BAD: Nullable parameter
public void processUser(String name, String nickname) {
    // Caller passes null: processUser("John", null)
    // Unclear contract, null checks scattered throughout
}

// ✅ GOOD: Method overloading with optional arguments last
public void processUser(String name) {
    processUser(name, generateDefaultNickname(name));
}

public void processUser(String name, String nickname) {
    requireThat(name, "name").isNotNull();
    requireThat(nickname, "nickname").isNotNull();  // Non-null when provided
    // process with both values
}

// ✅ GOOD: Optional as return type
public Optional<User> findUserById(long id) {
    // Clearly signals the result may be absent
    return Optional.ofNullable(userMap.get(id));
}
```

**Pattern for optional arguments**:
- Place optional arguments at the end of the method signature
- Provide overloaded methods that omit optional arguments
- The full-signature method must validate all arguments as non-null
- Shorter overloads delegate to the full signature with default values

**When the default value is null**: Use a private method that accepts nullable parameters:

```java
// Public API - all arguments validated
public void process(String name) {
    processInternal(name, null);  // null is the default
}

public void process(String name, String nickname) {
    requireThat(nickname, "nickname").isNotNull();
    processInternal(name, nickname);
}

// Private implementation accepts nullable
private void processInternal(String name, String nickname) {
    requireThat(name, "name").isNotNull();
    if (nickname != null) {
        // use nickname
    }
}
```

**For constructors**: When optional arguments have no non-null default value, each constructor should
set fields directly (no chaining). The constructor accepting the optional argument must validate it's
non-null:

```java
public class User {
    private final String name;
    private final String nickname;  // nullable

    // Short constructor - sets nickname to null directly
    public User(String name) {
        requireThat(name, "name").isNotNull();
        this.name = name;
        this.nickname = null;
    }

    // Full constructor - validates nickname is non-null
    public User(String name, String nickname) {
        requireThat(name, "name").isNotNull();
        requireThat(nickname, "nickname").isNotNull();
        this.name = name;
        this.nickname = nickname;
    }
}
```

When optional arguments have a non-null default, use constructor chaining:

```java
public class Config {
    private final int timeout;
    private final int retries;

    public Config(int timeout) {
        this(timeout, 3);  // Chain with default retries=3
    }

    public Config(int timeout, int retries) {
        this.timeout = timeout;
        this.retries = retries;
    }
}
```

**Records and Optional**: Do not use `Optional` as a record component when implementing an interface
that returns `Optional`. Records auto-generate accessors matching component types, so an
`Optional<T>` component creates a public accessor that callers must use `Optional.empty()` to construct.
Use a regular class instead to control accessor visibility.

```java
// ❌ BAD: Record with Optional component
public record Violation(String message, Optional<NodeIndex> nodeIndex)
    implements FormattingViolation
{
    // Auto-generated: public Optional<NodeIndex> nodeIndex() - forces callers to pass Optional
}

// ✅ GOOD: Class with controlled accessors
public final class Violation implements FormattingViolation {
    private final String message;
    private final NodeIndex nodeIndex;  // nullable

    // Constructor without optional parameter - sets null directly
    public Violation(String message) {
        this.message = requireThat(message, "message").isNotNull().getValue();
        this.nodeIndex = null;
    }

    // Constructor with optional parameter - enforces non-null
    public Violation(String message, NodeIndex nodeIndex) {
        this.message = requireThat(message, "message").isNotNull().getValue();
        this.nodeIndex = requireThat(nodeIndex, "nodeIndex").isNotNull().getValue();
    }

    @Override
    public Optional<NodeIndex> nodeIndex() {
        return Optional.ofNullable(nodeIndex);  // Interface return type is Optional
    }
}
```

**Constructors vs Factory Methods**: Prefer constructors for simple instance creation. Use static factory
methods only when they provide meaningful benefits:
- Named construction with different semantics (e.g., `fromJson()`, `empty()`, `copyOf()`)
- Returning cached instances or subclasses
- Complex initialization logic that benefits from a descriptive name

When all factory methods would just be `create(...)` with simple `new` calls, use constructors instead.

## References {#references}

- **Complete style rules**: [docs/code-style-human.md](../code-style-human.md) - Human-readable explanations
- **Automated patterns**: [docs/code-style/*.md](../code-style/) - Detection patterns for reviewers
