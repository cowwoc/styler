# Code Policies

Complete code policy guidelines for Styler Java Code Formatter project.

## 📝 CODE COMMENTS POLICY

**OUTDATED CONTENT HANDLING**:
🔄 **UPDATE PRINCIPLE**: When encountering outdated code or comments, update them to reflect current
functionality rather than removing them **only if there is long-term interest in keeping them**, even if this
requires significant effort. If they're not used/needed, remove them entirely.

**PROHIBITED COMMENT PATTERNS**:
❌ References to past changes: "Note: Removed skipping of getters/setters as tests expect them to be
documented"
❌ Implementation history: "Previously this used X, now it uses Y"
❌ Change rationale: "Updated to fix issue with Z"
❌ Refactoring notes: "Changed from approach A to approach B because..."
❌ Superficial linter avoidance: Replacing "TODO" with "Future" or "Note" to avoid checkstyle violations

**REQUIRED COMMENT PATTERNS**:
✅ Current functionality: "Skip constructors and overridden methods"
✅ Business logic: "Javadoc is complete when all parameters and return values are documented"
✅ Technical rationale: "Expression context prevents statement-level transformation"
✅ Domain constraints: "Source position tracking requires precise integer arithmetic"

**PRINCIPLE**: Comments should describe WHAT the code does and WHY it works that way, never WHAT it used to do
or HOW it changed. When comments become outdated, update them to accurately reflect current behavior.

## 🔧 TODO COMMENT HANDLING

**CRITICAL**: Never superficially modify TODO comments or use temporal language to avoid linter checks.

**PROHIBITED APPROACH**:
❌ Changing "TODO" to "Future" or "Note" to bypass checkstyle
❌ Rewording TODOs without addressing the underlying issue
❌ Using temporal terms: "For now", "Currently unused", "Temporarily", "Will be replaced"
❌ Apologetic comments: "This is a hack", "Quick fix", "Not ideal but works"

**REQUIRED APPROACH**:
✅ **Implement the TODO**: If it's critical for the current task, implement it now
✅ **Remove the comment**: If the TODO isn't needed, delete it entirely
✅ **Fix the code**: If something is "temporary", fix the underlying problem; otherwise, make it permanent or
document why it must be temporary
✅ **Move to task tracker**: Add genuine TODOs to todo.md and remove inline comment
✅ **Accept the violation**: If the TODO is legitimately needed for documentation, keep it as-is and accept the
checkstyle violation (or suppress with @SuppressWarnings if project policy allows)

**EXAMPLE - BAD**:
```java
// TODO: Extract configuration from Maven project properties
// Changed to:
// Future: Extract configuration from Maven project properties  ❌ Superficial change
// For now, use default configuration  ❌ Temporal language
// Currently unused, defaults applied  ❌ Temporal language
```

**EXAMPLE - GOOD**:
```java
// Option 1: Implement it
RuleConfiguration config = extractConfigFromMavenProperties(mavenProject);  ✅

// Option 2: Remove if not needed
return new LineLengthRuleConfiguration();  ✅ (no comment)

// Option 3: Remove unused parameter or use it
private RuleConfiguration createRuleConfiguration()  ✅ (removed unused param)

// Option 4: Move to todo.md
// Added task to todo.md: "Extract Maven config to RuleConfiguration"  ✅
return new LineLengthRuleConfiguration();

// Option 5: Keep TODO if genuinely needed for code understanding
// TODO: Extract configuration from Maven project properties when ConfigurationExtractor is implemented  ✅
```

## 📚 JAVADOC COMMENT REQUIREMENTS

**CRITICAL MANUAL PROCESS**: JavaDoc comments must NEVER be populated using automated scripts or
template-based tools.

**MANDATORY APPROACH**:
✅ Read and understand the actual business logic being implemented
✅ Write JavaDoc that explains WHAT is being validated/implemented based on context
✅ Use meaningful descriptions that add value beyond the method name
✅ Each JavaDoc must be crafted individually based on the specific code's purpose

**PROHIBITED APPROACH**:
❌ Using scripts to bulk-add generic JavaDoc like "Test method" or "Validates X"
❌ Template-based JavaDoc generation without understanding business context
❌ Copy-paste JavaDoc patterns without adapting to specific use case
❌ Generic descriptions that could apply to any method

**EXAMPLE - BAD**:
```java
/**
 * Test method.
 */
@Test
public void canHandleWithJava25ReturnsTrue()
```

**EXAMPLE - GOOD**:
```java
/**
 * Verifies that FlexibleConstructorBodiesStrategy handles constructor bodies in Java 25 with LBRACE token.
 */
@Test
public void canHandleWithJava25ReturnsTrue()
```

**RATIONALE**: JavaDoc comments require understanding the business domain and technical context. Automated
scripts cannot comprehend what a method actually does or why it matters, leading to meaningless documentation
that provides no value to developers.

## 🧪 TESTNG TEST REQUIREMENTS

**CRITICAL THREAD SAFETY**: All tests must be thread-safe to support parallel execution.

**PROHIBITED PATTERNS**:
❌ `@BeforeMethod` - Creates mutable shared state between test methods
❌ `@AfterMethod` - Cleanup methods that assume sequential execution
❌ Mutable instance fields shared across test methods
❌ `assertThatThrownBy()` - Use TestNG's native exception handling instead

**REQUIRED PATTERNS**:
✅ `@Test(expectedExceptions = ExceptionType.class)` - For exception testing
✅ Local variables within each test method
✅ Static helper methods for test data creation
✅ Each test method is completely independent and self-contained

**LOOP INCREMENT STYLE**:
✅ Use `++i` for pre-increment (standard style)
❌ Do NOT use `i++` or `i += 1`

**EXAMPLE - BAD**:
```java
public class MyTest {
    private Configuration config;  // Shared mutable state

    @BeforeMethod
    public void setUp() {
        config = new Configuration();  // Not thread-safe
    }

    @Test
    public void testFeature() {
        assertThatThrownBy(() -> config.validate())
            .isInstanceOf(ValidationException.class);
    }
}
```

**EXAMPLE - GOOD**:
```java
public class MyTest {
    @Test(expectedExceptions = ValidationException.class)
    public void testFeatureThrowsValidationException() {
        Configuration config = new Configuration();  // Local state
        config.validate();
    }

    @Test
    public void testAnotherFeature() {
        Configuration config = createTestConfig();  // Independent
        assertThat(config.isValid()).isTrue();
    }

    private static Configuration createTestConfig() {
        return new Configuration();
    }
}
```

**RATIONALE**: TestNG runs tests in parallel by default. Using `@BeforeMethod` or mutable instance fields
creates race conditions and non-deterministic test failures. Each test method must be completely independent.

## 🚨 EXCEPTION TYPE SELECTION

**CRITICAL DISTINCTION**: Choose exception types based on whether valid input reached an impossible state
(internal bug) or the user violated the API contract.

**INTERNAL BUGS** - Use `AssertionError`:
- **Definition**: Valid input leads to impossible/unreachable state - indicates bug in our implementation
- Parser bugs (missing child nodes when parsing valid Java syntax)
- Converter bugs (unexpected node types after valid parsing)
- Algorithm bugs (postcondition violations with valid inputs)
- Impossible state reached (unreachable code executed, invalid enum value)
- Invariant violations (data structure corruption with valid operations)
-  **Key principle**: If the user provided valid input and followed the API correctly, but the code reached an
  impossible state, it's our bug → AssertionError

**INVALID USER STATE** - Use `IllegalStateException`:
- **Definition**: User violated API contract by using object in wrong state/order - not an internal bug
- Object used after being closed/disposed
- Method called in wrong order (e.g., calling next() before hasNext())
- Operation attempted on object in invalid lifecycle state
- User interaction that violates object state machine
- **Key principle**: User provided valid input but called methods in wrong sequence → IllegalStateException

**INVALID USER INPUT** - Use `IllegalArgumentException`:
- **Definition**: User provided invalid parameter values - not an internal bug
- Invalid method parameters from user code
- Configuration values out of acceptable range
- User-provided data fails validation
- **Key principle**: User provided invalid data/parameters → IllegalArgumentException

**EXAMPLES - INTERNAL BUGS (AssertionError)**:
```java
// Parser bug - valid Java source reaches impossible state (missing child nodes)
if (packageName == null) {
    throw new AssertionError(
        "Parser bug: PACKAGE_DECLARATION node " + nodeId + " has no child nodes for valid package declaration");
}

// Algorithm bug - valid input produces impossible result (postcondition violated)
if (result < 0) {
    throw new AssertionError("Algorithm bug: distance calculation returned negative value for valid coordinates");
}

// Unreachable code - valid enum value reaches impossible default case
default:
    throw new AssertionError("Unreachable: all TokenType enum values handled, got: " + tokenType);
}

// Invariant violation - valid operations corrupted internal state
if (childCount != children.size()) {
    throw new AssertionError("Invariant violated: childCount=" + childCount +
        " but children.size()=" + children.size() + " after valid addChild() operation");
}
```

**EXAMPLES - INVALID USER STATE (IllegalStateException)**:
```java
// Object already closed
if (closed) {
    throw new IllegalStateException("Cannot parse: parser has been closed");
}

// Wrong method call order
if (!initialized) {
    throw new IllegalStateException("Must call initialize() before parse()");
}

// Invalid lifecycle state
if (state != State.READY) {
    throw new IllegalStateException("Cannot execute in state: " + state);
}
```

**EXAMPLES - INVALID USER INPUT (IllegalArgumentException)**:
```java
// Invalid parameter
if (maxLength <= 0) {
    throw new IllegalArgumentException("maxLength must be positive, was: " + maxLength);
}

// Invalid configuration
if (tabWidth < 1 || tabWidth > 8) {
    throw new IllegalArgumentException("tabWidth must be 1-8, was: " + tabWidth);
}
```

**PRINCIPLE**:
- **AssertionError**: Valid input + correct API usage → impossible state reached = **our bug**
-  **IllegalStateException**: Valid input + incorrect API usage (wrong method order/lifecycle) = **user's API
  misuse**
- **IllegalArgumentException**: Invalid input = **user's bad data**

This distinction helps developers immediately identify whether they need to fix their code
(IllegalStateException/IllegalArgumentException) or file a bug report against our implementation
(AssertionError).

**DECISION FLOWCHART**:
1. Did the user provide valid input/parameters?
   - NO → `IllegalArgumentException`
   - YES → Continue to 2
2. Did the user follow the API contract (correct method order, valid lifecycle state)?
   - NO → `IllegalStateException`
   - YES → Continue to 3
3. Did the code reach an impossible/unreachable state?
   - YES → `AssertionError` (this is our bug)
   - NO → Use appropriate checked/unchecked exception for the specific error case
