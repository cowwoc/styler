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
- ✅ **Required**: Create fresh instances in each test method

```java
// ❌ BAD
@BeforeMethod
public void setup() {
    parser = new Parser();  // Shared state
}

// ✅ GOOD
@Test
public void testParse() {
    Parser parser = new Parser();  // Fresh instance per test
    // ...
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

## References {#references}

- **Complete style rules**: [docs/code-style-human.md](../code-style-human.md) - Human-readable explanations
- **Automated patterns**: [docs/code-style/*.md](../code-style/) - Detection patterns for reviewers
- **Task protocol**: [task-protocol-agents.md](task-protocol-agents.md) - Agent coordination
- **Main agent guide**: [main-agent-coordination.md](main-agent-coordination.md) - Coordination patterns
