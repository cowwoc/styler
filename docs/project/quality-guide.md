# Code Quality Guide

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** quality, test
> **Purpose:** Code quality standards and test-driven development practices

## 🧪 UNIT TEST DRIVEN BUG FIXING {#unit-tdd-implementationing}

When encountering any bug during development:

### Bug Discovery Protocol {#bug-discovery-protocol}

1. **IMMEDIATE UNIT TEST**: Create a minimal unit test that reproduces the exact bug
2. **ISOLATION**: Extract the failing behavior into the smallest possible test case
3. **DOCUMENTATION**: Add the test to appropriate test suite with descriptive name
4. **FIX VALIDATION**: Ensure the unit test passes after implementing the fix
5. **REGRESSION PREVENTION**: Keep the test in the permanent test suite

### Unit Test Requirements {#unit-test-requirements}

- **Specific**: Target the exact failing behavior, not general functionality
- **Minimal**: Use the smallest possible input that triggers the bug
- **Descriptive**: Test method name clearly describes the bug scenario
- **Isolated**: Independent of other tests and external dependencies
- **Fast**: Execute quickly to enable frequent testing

✅ `testScientificNotationLexing()` - floating-point literal bugs
✅ `testMethodReferenceInAssignment()` - parser syntax bugs
✅ `testEnumConstantWithArguments()` - enum parsing bugs
✅ `testGenericTypeVariableDeclaration()` - generics bugs

### Example Workflow {#example-workflow}

```java
// 1. Bug discovered: Parser fails on scientific notation "1.5e10"

// 2. Create test that reproduces bug
@Test
public void testScientificNotationLexing() {
    String input = "double x = 1.5e10;";
    List<Token> tokens = lexer.tokenize(input);
    Token literalToken = tokens.get(3);

    // This assertion FAILS initially (reproducing the bug)
    assertEquals(TokenType.DOUBLE_LITERAL, literalToken.type());
    assertEquals("1.5e10", literalToken.text());
}

// 3. Fix the bug in Lexer.java (add scientific notation handling)

// 4. Test now PASSES - bug is fixed

// 5. Test remains in suite permanently - prevents regression
```

## 📝 QUALITY-SPECIFIC CODE POLICIES {#quality-specific-code-policies}

### Code Comments {#code-comments}

Comments that contradict current implementation must be updated or removed. Implementation history belongs in git. Focus on WHY, not WHAT.

```java
// ❌ BAD: "Loops through the list and checks each item"
// ✅ GOOD: "Validates business rule: all items must have positive quantity"
```

### TODO Comments {#todo-comments}

**Three Options** (choose one):
1. **Implement**: Fix the TODO immediately during this task
2. **Remove**: If TODO is no longer relevant
3. **Document**: Convert to tracked task in todo.md with:
   - Purpose: Why this work is needed
   - Scope: What components are affected
   - Context: What triggered this need
   - Acceptance criteria: How to know when it's done

**PROHIBITED**: Superficial renaming without action

### Duplication Detection {#duplication-detection}

Flag duplicated logic as HIGH priority:

- Duplicated business logic → Extract to shared method
- Duplicated validation → Create validator class
- Duplicated error handling → Extract error handling pattern
- Duplicated transformations → Create transformation utility

### Complexity Thresholds {#complexity-thresholds}

**Cyclomatic Complexity**:
- **Acceptable**: < 10 per method
- **Concerning**: 10-15 (consider refactoring)
- **Rejected**: > 15 (must refactor)

**Method Length**:
- **Acceptable**: < 50 lines
- **Concerning**: 50-100 lines (consider extraction)
- **Rejected**: > 100 lines (must extract methods)

### Parameter Validation - requireThat() Usage {#parameter-validation-require-that}

**MANDATORY**: Use `requireThat()` from requirements-java library for ALL parameter validation.

**Correct Import**:
```java
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
```

**❌ WRONG Imports** (DO NOT USE):
```java
// WRONG - Internal API
import static io.github.cowwoc.requirements.java.internal.implementation.ClassAssertions.requireThat;
```

**Parameter Order**: `requireThat(value, "name")`
- **First parameter**: The actual value to validate
- **Second parameter**: String name of the parameter (for error messages)

**✅ CORRECT Examples**:
```java
requireThat(filePath, "filePath").isNotNull();
requireThat(count, "count").isGreaterThan(0);
requireThat(name, "name").isNotNull().isNotEmpty();
requireThat(endPosition, "endPosition").isGreaterThanOrEqualTo(startPosition);
```

**❌ WRONG Examples** (reversed parameter order):
```java
requireThat("filePath", filePath).isNotNull();  // WRONG - name before value
requireThat("count", count).isGreaterThan(0);    // WRONG - name before value
```

**Common Mistake**: Confusing with other validation libraries that use (name, value) order.
The requirements-java library uses (value, name) order.

**Reference**: See existing usage in security/ExecutionTimeoutManager.java

### Refactoring Patterns {#refactoring-patterns}

**Extract Method**:
```java
// Before: Long method with distinct sections
public void processOrder(Order order) {
    // 20 lines of validation
    // 30 lines of processing
    // 15 lines of notification
}

// After: Extracted to focused methods
public void processOrder(Order order) {
    validateOrder(order);
    processOrderItems(order);
    notifyOrderProcessed(order);
}

private void validateOrder(Order order) { /* validation */ }
private void processOrderItems(Order order) { /* processing */ }
private void notifyOrderProcessed(Order order) { /* notification */ }
```

**Extract Class**:
```java
// Before: God class with multiple responsibilities
public class OrderProcessor {
    public void validateOrder() { }
    public void calculateTax() { }
    public void sendEmail() { }
    public void logAudit() { }
}

// After: Single Responsibility Principle
public class OrderProcessor {
    private OrderValidator validator;
    private TaxCalculator taxCalculator;
    private EmailNotifier notifier;
    private AuditLogger logger;
}
```

## Test Strategy Requirements {#test-strategy-requirements}

### REQUIREMENTS Phase: Business Rule Focus {#requirements-phase-business-rule-focus}

**⚠️ CRITICAL FOR TESTER AGENTS**: When writing requirements reports (`*-tester-requirements.md`), focus on
**business rules to validate**, NOT test counts.

**✅ CORRECT** (Business Rule Focus):
```markdown
## Required Business Rule Coverage
- Input validation: null inputs, empty collections, invalid paths
- File isolation: one file failure must not affect other files
- Concurrency safety: no race conditions, proper synchronization
- Error handling: file not found, permissions denied, timeout
```

**❌ WRONG** (Test Count Focus):
```markdown
## Test Requirements
- Phase 1: 17 tests
- Phase 2: 10 tests
- Total: 27 tests minimum
```

**Rationale**: Test count is an OUTPUT of implementation, not an INPUT to planning. The tester agent
specifies WHAT to test; the implementation determines HOW MANY tests are needed to cover those behaviors.

### Business-Logic Coverage {#business-logic-coverage}

**Focus on meaningful behavior, not test counts or code coverage percentages.**

**Coverage Areas** (MANDATORY):
1. **Input Validation**: Tests for null, empty, and invalid inputs
2. **Business Rules**: Tests verifying core business logic and constraints
3. **Edge Cases**: Tests for boundary conditions and unusual but valid inputs
4. **Error Handling**: Tests for expected failure modes and error messages
5. **Integration Points**: Tests for component interactions and data flow

### Test Naming Convention {#test-naming-convention}

Use descriptive names that explain WHAT is tested and WHAT the expected outcome is:

✅ `shouldRejectNullInput()`
✅ `shouldHandleLineLengthAtExactLimit()`
✅ `shouldParseEmptyClass()`
✅ `shouldThrowExceptionForInvalidFormat()`

❌ `test1()`, `test2()`, `testMethod()`

### Test Independence {#test-independence}

Each test MUST:
- Create its own test data
- Not depend on execution order
- Clean up any resources
- Not share mutable state with other tests

```java
// ✅ GOOD: Fresh instance per test
@Test
public void shouldValidatePositiveInput() {
    Validator validator = new Validator();
    assertTrue(validator.isValid(10));
}

@Test
public void shouldRejectNegativeInput() {
    Validator validator = new Validator();
    assertFalse(validator.isValid(-5));
}
```

### Incremental Test Validation Strategy {#incremental-test-validation-strategy}

For multi-file test creation, validate after each test class before proceeding to next. Detects issues early and avoids cascading errors.

✅ **Recommended Pattern**:
```bash
# After creating first test file
mvn test-compile -pl :module-name
mvn test -Dtest=FirstTest -pl :module-name

# Fix issues, then proceed to next test file
# Repeat for each test file
```

❌ **Anti-Pattern**:
```bash
# Create all 6 test files first, then run full build
mvn clean verify
# Results in 53 violations across all files - must fix all at once
```

**Targeted Builds**:
```bash
mvn test-compile -pl :module-name              # Compile only
mvn test -Dtest=ClassName -pl :module-name      # Run specific test
mvn test -Dtest=Class1Test,Class2Test -pl :module-name  # Multiple tests
```

### Module Dependency Testing Requirements (JPMS Projects) {#module-dependency-testing-requirements-jpms-projects}

For JPMS projects, test modules require:
1. Separate module name (e.g., `io.github.styler.parser.test`)
2. Module descriptor: `src/test/java/module-info.java`
3. Test module `requires` main module
4. Use `opens` (NOT `exports`) for test packages

**Module Configuration Tests:**

**1. Module Descriptor Validation Tests** (verify at build time):
```bash
# Test: Verify test module name differs from main module
diff <(grep "^module " src/main/java/module-info.java) \
     <(grep "^module " src/test/java/module-info.java)
# Must show difference (parser vs parser.test)

# Test: Verify test module requires main module
grep "requires io.github.styler.parser;" src/test/java/module-info.java

# Test: Verify test framework dependency
grep "requires org.testng;" src/test/java/module-info.java
```

**2. Module Resolution Tests** (add to test suite):
```java
// Test that verifies module system is active
@Test
public void shouldRunWithModuleSystemEnabled() {
    ModuleLayer layer = getClass().getModule().getLayer();
    assertNotNull(layer, "Tests should run with module system enabled");
}

// Test that main module is accessible
@Test
public void shouldAccessMainModuleClasses() {
    // Verify test can import and instantiate classes from main module
    var parser = new JavaParser();  // From main module
    assertNotNull(parser);
}
```

**3. Clean Build Verification** (MANDATORY before merge):
```bash
# Clean build detects stale module-info.class
./mvnw clean verify

# Common failure: "module not found" despite module present
# Cause: Stale module descriptors in build cache
# Solution: Always use `clean verify` before final validation
```

**4. Module Dependency Anti-Patterns to Test Against:**

Test suites should verify these issues DON'T occur:
- [ ] Test module name conflict with main module (both named `io.github.styler.parser`)
- [ ] Missing `requires` declaration for main module in test module-info.java
- [ ] Using `exports` instead of `opens` for test packages (breaks reflection)
- [ ] Main module accidentally requiring test module (circular dependency)
- [ ] Test module using `requires transitive` for testing framework (unnecessary)

**Example Test Module Descriptor**:
```java
// src/test/java/module-info.java
module io.github.styler.parser.test {
    requires io.github.styler.parser;  // Main module
    requires org.testng;                // Testing framework

    // Open packages for reflection (TestNG needs access)
    opens io.github.styler.parser.test to org.testng;
    opens io.github.styler.parser.test.integration to org.testng;
}
```

**Verification Checklist**:
- [ ] Test module has different name than main module
- [ ] Test module requires main module
- [ ] All test packages are `opens` to testing framework
- [ ] No circular dependencies (main does NOT require test)
- [ ] `./mvnw clean verify` passes (clean build required)
- [ ] Tests execute successfully with module system enabled

## Code Review Checklist {#code-review-checklist}

Before approving code:

- [ ] All business logic has corresponding unit tests
- [ ] No code duplication (DRY principle)
- [ ] Cyclomatic complexity < 10 per method
- [ ] Method length < 50 lines
- [ ] Clear, descriptive names for classes, methods, variables
- [ ] Comments explain WHY, not WHAT
- [ ] No TODO comments without corresponding todo.md entries
- [ ] Exception handling appropriate to context
- [ ] No magic numbers (use named constants)
- [ ] Consistent formatting and style

## References {#references}

- **Complete code policies**: [docs/optional-modules/code-policies.md](../optional-modules/code-policies.md)
- **Style guide**: [style-guide.md](style-guide.md) - Style-specific requirements
