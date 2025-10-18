# Code Quality Guide

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** quality-reviewer, quality-updater, test-reviewer, test-updater
> **Purpose:** Code quality standards and test-driven development practices

## 🧪 UNIT TEST DRIVEN BUG FIXING

**MANDATORY PROCESS**: When encountering any bug during development:

### Bug Discovery Protocol

1. **IMMEDIATE UNIT TEST**: Create a minimal unit test that reproduces the exact bug
2. **ISOLATION**: Extract the failing behavior into the smallest possible test case
3. **DOCUMENTATION**: Add the test to appropriate test suite with descriptive name
4. **FIX VALIDATION**: Ensure the unit test passes after implementing the fix
5. **REGRESSION PREVENTION**: Keep the test in the permanent test suite

### Unit Test Requirements

- **Specific**: Target the exact failing behavior, not general functionality
- **Minimal**: Use the smallest possible input that triggers the bug
- **Descriptive**: Test method name clearly describes the bug scenario
- **Isolated**: Independent of other tests and external dependencies
- **Fast**: Execute quickly to enable frequent testing

### Examples

✅ `testScientificNotationLexing()` - for floating-point literal bugs
✅ `testMethodReferenceInAssignment()` - for parser syntax bugs
✅ `testEnumConstantWithArguments()` - for enum parsing bugs
✅ `testGenericTypeVariableDeclaration()` - for generics bugs

### Example Workflow

```java
// 1. Bug discovered: Parser fails on scientific notation "1.5e10"

// 2. Create test that reproduces bug
@Test
public void testScientificNotationLexing() {
    String input = "double x = 1.5e10;";
    List<Token> tokens = lexer.tokenize(input);

    // This assertion FAILS initially (reproducing the bug)
    assertEquals(TokenType.DOUBLE_LITERAL, tokens.get(3).type());
    assertEquals("1.5e10", tokens.get(3).text());
}

// 3. Fix the bug in Lexer.java (add scientific notation handling)

// 4. Test now PASSES - bug is fixed

// 5. Test remains in suite permanently - prevents regression
```

## 📝 QUALITY-SPECIFIC CODE POLICIES

### Code Comments

**Update Outdated Comments**:
- Comments that contradict current implementation must be updated or removed
- Implementation history belongs in git, not code comments

**Focus on WHY, not WHAT**:
```java
// ❌ BAD: "Loops through the list and checks each item"
// ✅ GOOD: "Validates business rule: all items must have positive quantity"
```

### TODO Comments

**Three Options** (choose one):
1. **Implement**: Fix the TODO immediately during this task
2. **Remove**: If TODO is no longer relevant
3. **Document**: Convert to tracked task in todo.md with:
   - Purpose: Why this work is needed
   - Scope: What components are affected
   - Context: What triggered this need
   - Acceptance criteria: How to know when it's done

**PROHIBITED**: Superficial renaming without action

### Duplication Detection

**When reviewing code, flag duplicated logic as HIGH priority**:

- Duplicated business logic → Extract to shared method
- Duplicated validation → Create validator class
- Duplicated error handling → Extract error handling pattern
- Duplicated transformations → Create transformation utility

### Complexity Thresholds

**Cyclomatic Complexity**:
- **Acceptable**: < 10 per method
- **Concerning**: 10-15 (consider refactoring)
- **Rejected**: > 15 (must refactor)

**Method Length**:
- **Acceptable**: < 50 lines
- **Concerning**: 50-100 lines (consider extraction)
- **Rejected**: > 100 lines (must extract methods)

### Refactoring Patterns

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

## Test Strategy Requirements

### Minimum Test Coverage

**Categories** (MANDATORY):
1. **Null/Empty Validation**: 2-3 tests - Verify null/empty input handling
2. **Boundary Conditions**: 2-3 tests - Test edge values (0, max, min+1, max-1)
3. **Edge Cases**: 3-5 tests - Unusual but valid inputs
4. **Happy Path**: 1-2 tests - Standard expected usage
5. **Error Conditions**: 2-3 tests - Invalid inputs and expected failures

### Test Naming Convention

Use descriptive names that explain WHAT is tested and WHAT the expected outcome is:

✅ `shouldRejectNullInput()`
✅ `shouldHandleLineLengthAtExactLimit()`
✅ `shouldParseEmptyClass()`
✅ `shouldThrowExceptionForInvalidFormat()`

❌ `test1()`, `test2()`, `testMethod()`

### Test Independence

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

### Module Dependency Testing Requirements (JPMS Projects)

For projects using Java Platform Module System (JPMS), test modules have additional requirements:

**Test Module Structure:**
1. **Separate module name**: Test module MUST use different name (e.g., `io.github.styler.parser.test`)
2. **Module descriptor**: `src/test/java/module-info.java` required
3. **Dependency declaration**: Test module `requires` main module
4. **Reflection access**: Use `opens` (NOT `exports`) for test packages to testing framework

**Required Tests for Module Configuration:**

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
# CRITICAL: Clean build detects stale module-info.class
./mvnw clean verify

# Common failure mode: build cache has stale module descriptors
# Symptom: "module not found" despite module being present
# Root cause: Previous `./mvnw compile` without package phase
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

## Code Review Checklist

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

## References

- **Complete code policies**: [docs/optional-modules/code-policies.md](../optional-modules/code-policies.md)
- **Style guide**: [style-guide.md](style-guide.md) - Style-specific requirements
- **Task protocol**: [task-protocol-agents.md](task-protocol-agents.md) - Agent coordination
- **Main agent guide**: [main-agent-coordination.md](main-agent-coordination.md) - Coordination patterns
