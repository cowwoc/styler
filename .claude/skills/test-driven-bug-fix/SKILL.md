---
name: test-driven-bug-fix
description: Systematic bug fix workflow with unit test first, then fix, prevents regression
allowed-tools: Bash, Read, Write, Edit
---

# Test-Driven Bug Fix Skill

**Purpose**: Systematically fix bugs by creating unit test first, then implementing fix, ensuring regression prevention.

**Performance**: Prevents bug recurrence, builds regression test suite, improves code quality

## When to Use This Skill

### ✅ Use test-driven-bug-fix When:

- Bug discovered during development or testing
- Unexpected behavior found
- Edge case identified
- Need to ensure bug doesn't recur
- Want systematic bug fix approach

### ❌ Do NOT Use When:

- Not a bug (feature request)
- Issue is compilation error (not behavioral bug)
- Test already exists for the bug
- Bug fix is trivial (typo, missing semicolon)

## What This Skill Does

### 1. Analyzes Bug

```bash
# Identifies:
- Exact failing behavior
- Minimal input that triggers bug
- Expected vs actual behavior
- Root cause (if known)
```

### 2. Creates Minimal Reproducing Test

```java
@Test
public void testBugScenario() {
    // Minimal input triggering bug
    String input = "edge case";

    // Execute code
    Result result = methodUnderTest(input);

    // This assertion FAILS initially (reproduces bug)
    assertEquals(expectedBehavior, result);
}
```

### 3. Verifies Test Fails

```bash
# Run test to confirm it reproduces bug
mvn test -Dtest=BugTest#testBugScenario

# Expected: Test FAILS (proves bug exists)
```

### 4. Implements Fix

```bash
# Fix the code causing the bug
Edit: src/main/java/BuggyClass.java
  - Fix root cause
  - Handle edge case
  - Add validation if needed
```

### 5. Verifies Test Passes

```bash
# Run test again after fix
mvn test -Dtest=BugTest#testBugScenario

# Expected: Test PASSES (proves bug fixed)
```

### 6. Adds to Permanent Suite

```bash
# Commit test with fix
git add src/test/java/BugTest.java
git add src/main/java/BuggyClass.java
git commit -m "Fix: Handle edge case in validation

Added regression test: testBugScenario()

🤖 Generated with Claude Code
Co-Authored-By: Claude <noreply@anthropic.com>"
```

## Usage

### Basic Bug Fix

```bash
# After discovering bug
BUG_DESCRIPTION="Parser fails on scientific notation '1.5e10'"
TEST_CLASS="LexerTest"
BUGGY_CLASS="Lexer"

/workspace/main/.claude/scripts/test-driven-bug-fix.sh \
  --bug-description "$BUG_DESCRIPTION" \
  --test-class "$TEST_CLASS" \
  --buggy-class "$BUGGY_CLASS"
```

### With Minimal Input

```bash
# Provide minimal reproducing input
MINIMAL_INPUT="double x = 1.5e10;"
EXPECTED="TokenType.DOUBLE_LITERAL"
ACTUAL="ParseException"

/workspace/main/.claude/scripts/test-driven-bug-fix.sh \
  --input "$MINIMAL_INPUT" \
  --expected "$EXPECTED" \
  --actual "$ACTUAL"
```

## Bug Discovery Protocol

### Step 1: Immediate Unit Test

**DO NOT fix bug before creating test**

```java
// ❌ WRONG: Fix immediately
if (input.contains("e")) {
    // Handle scientific notation
}

// ✅ CORRECT: Test first
@Test
public void testScientificNotationLexing() {
    // Reproduces bug, will fail initially
}
// THEN fix after test fails
```

### Step 2: Isolation

**Extract smallest possible test case**

```java
// ❌ TOO BROAD: Full program
@Test
public void testComplexProgram() {
    String program = """
        public class Test {
            double x = 1.5e10;
            int y = 42;
            String z = "hello";
        }
        """;
    parser.parse(program);
}

// ✅ MINIMAL: Just the failing part
@Test
public void testScientificNotationLexing() {
    String input = "1.5e10";
    Token token = lexer.nextToken(input);
    assertEquals(TokenType.DOUBLE_LITERAL, token.type());
}
```

### Step 3: Documentation

**Descriptive test name**

```java
// ❌ VAGUE
@Test public void test1() { }
@Test public void testLexer() { }
@Test public void testBug() { }

// ✅ DESCRIPTIVE
@Test public void testScientificNotationLexing() { }
@Test public void testMethodReferenceInAssignment() { }
@Test public void testEnumConstantWithArguments() { }
@Test public void testGenericTypeVariableDeclaration() { }
```

### Step 4: Fix Validation

**Ensure test passes after fix**

```bash
# Before fix: Test FAILS
mvn test -Dtest=LexerTest#testScientificNotationLexing
# [ERROR] Tests run: 1, Failures: 1

# After fix: Test PASSES
mvn test -Dtest=LexerTest#testScientificNotationLexing
# [INFO] Tests run: 1, Failures: 0
```

### Step 5: Regression Prevention

**Keep test permanently**

```bash
# ❌ WRONG: Delete test after bug fixed
git add src/main/java/Lexer.java
git commit -m "Fix scientific notation bug"
# Test not committed - regression possible

# ✅ CORRECT: Commit test with fix
git add src/test/java/LexerTest.java
git add src/main/java/Lexer.java
git commit -m "Fix scientific notation bug

Added regression test: testScientificNotationLexing()

🤖 Generated with Claude Code"
```

## Unit Test Requirements

### Specific

**Target exact failing behavior**

```java
// ❌ GENERAL: Tests overall functionality
@Test
public void testLexer() {
    // Tests many things, not specific bug
}

// ✅ SPECIFIC: Tests only the bug
@Test
public void testScientificNotationLexing() {
    // Tests ONLY scientific notation handling
}
```

### Minimal

**Smallest input triggering bug**

```java
// ❌ LARGE INPUT
String input = "public class Foo { double x = 1.5e10; int y = 42; }";

// ✅ MINIMAL INPUT
String input = "1.5e10";
```

### Isolated

**Independent of other tests**

```java
// ❌ DEPENDENT
@Test
public void test1() { setupDatabase(); }

@Test
public void test2() {
    // Depends on test1 running first
    queryDatabase();
}

// ✅ ISOLATED
@Test
public void testScientificNotation() {
    // Self-contained, no external dependencies
    String input = "1.5e10";
    Token token = Lexer.tokenize(input);
    assertEquals(TokenType.DOUBLE_LITERAL, token.type());
}
```

### Fast

**Execute quickly**

```java
// ❌ SLOW: External dependencies
@Test
public void testParser() {
    server.start();
    client.connect();
    // ... slow setup
}

// ✅ FAST: Direct testing
@Test
public void testScientificNotation() {
    assertEquals(TokenType.DOUBLE_LITERAL,
                 Lexer.tokenize("1.5e10").type());
}
```

## Example Workflow

### Example 1: Parser Bug

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

// 3. Run test - FAILS (confirms bug exists)
// mvn test -Dtest=LexerTest#testScientificNotationLexing
// Expected: DOUBLE_LITERAL, Actual: ParseException

// 4. Fix the bug in Lexer.java
private Token lexNumber() {
    StringBuilder sb = new StringBuilder();

    // Add existing decimal logic...

    // NEW: Handle scientific notation
    if (peek() == 'e' || peek() == 'E') {
        sb.append(consume());
        if (peek() == '+' || peek() == '-') {
            sb.append(consume());
        }
        while (Character.isDigit(peek())) {
            sb.append(consume());
        }
    }

    return new Token(TokenType.DOUBLE_LITERAL, sb.toString());
}

// 5. Run test - PASSES (confirms bug fixed)
// mvn test -Dtest=LexerTest#testScientificNotationLexing
// Tests run: 1, Failures: 0

// 6. Commit test + fix together
// git add src/test/java/LexerTest.java src/main/java/Lexer.java
// git commit -m "Fix: Add scientific notation support to lexer"
```

### Example 2: Validation Bug

```java
// 1. Bug: Validator incorrectly rejects valid input

// 2. Create test
@Test
public void testNullInputValidation() {
    // Currently throws NPE, should return validation error
    ValidationResult result = validator.validate(null);

    assertFalse(result.isValid());
    assertEquals("Input cannot be null", result.getError());
}

// 3. Test FAILS with NPE (reproduces bug)

// 4. Fix validator
public ValidationResult validate(String input) {
    // NEW: Null check
    if (input == null) {
        return ValidationResult.error("Input cannot be null");
    }

    // Existing validation logic...
}

// 5. Test PASSES (bug fixed)

// 6. Commit both files
```

## Workflow Integration

### Bug Discovery Flow

```markdown
Development or Testing
  ↓
Bug discovered
  ↓
[test-driven-bug-fix skill] ← THIS SKILL
  ↓
Create minimal reproducing test
  ↓
Verify test FAILS (reproduces bug)
  ↓
Implement fix
  ↓
Verify test PASSES (bug fixed)
  ↓
Commit test + fix together
  ↓
Bug fixed with regression prevention
```

## Output Format

Script returns JSON:

```json
{
  "status": "success",
  "message": "Bug fixed with regression test",
  "bug_description": "Parser fails on scientific notation",
  "test_class": "LexerTest",
  "test_method": "testScientificNotationLexing",
  "buggy_class": "Lexer",
  "test_failed_initially": true,
  "test_passed_after_fix": true,
  "files_modified": [
    "src/test/java/LexerTest.java",
    "src/main/java/Lexer.java"
  ],
  "commit_sha": "abc123",
  "timestamp": "2025-11-11T12:34:56-05:00"
}
```

## Related Skills

- **reinvoke-agent-fixes**: May use test-driven approach for fixes
- **archive-task**: Commits test + fix together

## Troubleshooting

### Error: "Test passes before fix"

```bash
# Test should FAIL initially to prove it reproduces bug
# If test passes before fix:
1. Bug may already be fixed
2. Test doesn't actually reproduce bug
3. Test logic incorrect

# Fix: Verify test actually triggers bug condition
```

### Error: "Test still fails after fix"

```bash
# Fix didn't resolve the bug
# Options:
1. Fix incorrect (try different approach)
2. Multiple bugs (fix addressed one, another remains)
3. Test expectations wrong (update test)

# Debug: Add logging to understand actual behavior
```

### Test Too Broad

```bash
# Test covers too much, not minimal
# Refine by:
1. Remove unrelated setup
2. Use smallest possible input
3. Test one specific behavior
4. Extract to helper method if needed
```

## Best Practices

### Always Test First

```bash
# ✅ CORRECT SEQUENCE
1. Create test (fails)
2. Implement fix
3. Test passes
4. Commit both

# ❌ WRONG SEQUENCE
1. Implement fix
2. Create test (passes)
# Can't verify test reproduces bug
```

### One Bug, One Test

```bash
# ✅ ONE TEST PER BUG
@Test testScientificNotation() { }
@Test testHexLiterals() { }
@Test testMethodReferences() { }

# ❌ MULTIPLE BUGS IN ONE TEST
@Test testAllBugs() {
    // Tests 5 different bugs
}
```

### Keep Tests Simple

```java
// ✅ SIMPLE
@Test
public void testEdgeCase() {
    assertEquals(expected, actual);
}

// ❌ COMPLEX
@Test
public void testEdgeCase() {
    // 50 lines of setup
    // Multiple assertions
    // Complex logic
}
```

## Implementation Notes

The test-driven-bug-fix script performs:

1. **Analysis Phase**
   - Parse bug description
   - Identify failing behavior
   - Determine minimal input
   - Extract expected vs actual

2. **Test Creation Phase**
   - Generate test method name
   - Create minimal reproducing test
   - Write to appropriate test class
   - Format with proper annotations

3. **Verification Phase**
   - Compile test
   - Run test
   - Verify test FAILS (proves reproduces bug)
   - Capture failure output

4. **Fix Implementation Phase**
   - Identify buggy code location
   - Implement fix (Edit tool)
   - Preserve existing behavior
   - Handle edge case

5. **Validation Phase**
   - Compile fixed code
   - Run test again
   - Verify test PASSES (proves fix works)
   - Run full test suite (no regressions)

6. **Commit Phase**
   - Stage test file
   - Stage fixed code file
   - Commit both together atomically
   - Include test name in message
