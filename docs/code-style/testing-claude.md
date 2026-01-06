# Testing Code Style Detection Patterns

**Claude Code optimization patterns for testing validation and detection**

This document contains testing patterns and detection rules optimized for Claude Code analysis and validation.

---

## 🔍 PARSER TEST PATTERNS {#parser-test-patterns}

### Unit Tests vs Integration Tests

Parser tests have two distinct utilities with DIFFERENT purposes:

| Utility | Purpose | Use Case |
|---------|---------|----------|
| `parseSemanticAst()` | Parse + return AST nodes | Unit tests that validate node types |
| `assertParseSucceeds()` | Parse only, verify no crash | Integration/smoke tests on real files |

### When to Use Each

**`parseSemanticAst()` + AST comparison** (REQUIRED for ALL feature tests):
- Testing a new parser feature (e.g., class literals, generics)
- Verifying specific node types are created
- Testing node positions/attributes
- **Any test validating parser correctness**

**`assertParseSucceeds()` - Limited Value** (consider avoiding):
- ONLY legitimate use: Smoke tests on real project files where maintaining expected AST is impractical
- **Does NOT validate semantic correctness** - only that parser doesn't crash
- **Prefer `parseSemanticAst()` for all new tests** - it validates both success AND correctness

### 🚨 CRITICAL: Deriving Expected Values Manually {#derive-expected-values-manually}

**NEVER generate expected values by running the test and copying actual output.**

Expected values MUST be derived by manually analyzing the source code string:

```java
// Source to test:
String source = """
    void main()
    {
    }
    """;

// ❌ WRONG: Run test, see actual output, copy it as expected
// This approach:
// 1. Tests nothing - you're comparing parser output to itself
// 2. Masks bugs - if parser is wrong, you'll enshrine the bug in expected
// 3. Provides no verification - expected should be independent truth

// ✅ CORRECT: Manually analyze source string to derive expected
// Step 1: Count character positions in source
//   "void main()" = positions 0-10
//   "\n" = position 11
//   "{" = position 12 (start of BLOCK)
//   "\n" = position 13
//   "}" = position 14 (end of BLOCK content)
//   "\n" = position 15 (end position is exclusive, so BLOCK ends at 15)
//
// Step 2: Identify expected node types and their spans
//   - BLOCK: starts at "{" (12), ends after "}" (15)
//   - METHOD_DECLARATION: starts at "void" (0), ends after block (15)
//   - IMPLICIT_CLASS_DECLARATION: wraps the method (0, 15)
//   - COMPILATION_UNIT: entire source (0, 16)
//
// Step 3: Write expected values based on YOUR analysis
expected.allocateNode(NodeType.BLOCK, 12, 15);
expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 15);
expected.allocateImplicitClassDeclaration(0, 15);
expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 16);
```

**Why manual derivation matters:**
- Expected values are the **specification** - what the parser SHOULD produce
- If you copy actual output, you're testing that "parser produces what parser produces"
- Manual analysis catches parser bugs because your expectation is independent
- When test fails, the diff shows whether parser or expectation is wrong

**Process for writing parser test expectations:**
1. Write the source code string
2. Count character positions manually (or use a simple counter)
3. Identify what AST nodes SHOULD be created based on Java grammar
4. Determine start/end positions for each node from character counting
5. Write expected allocations in the order nodes are created
6. Run test - if it fails, analyze WHETHER parser or your calculation is wrong

### Practical Technique: Verify Position Calculations {#verify-position-calculations}

**Manual counting is error-prone** (especially with tabs, newlines, multi-line strings). Use this technique
to verify your calculations:

**Step 1: Write test with placeholder positions first**
```java
@Test
public void shouldParseBlockComment()
{
    String source = """
        class Foo
        {
            /* comment */
            int x;
        }
        """;

    // First pass: use placeholder values (0, 0) to see actual positions
    NodeArena expected = new NodeArena();
    expected.allocateNode(NodeType.BLOCK_COMMENT, 0, 0);  // Placeholder
    expected.allocateNode(NodeType.FIELD_DECLARATION, 0, 0);  // Placeholder
    // ... other nodes
}
```

**Step 2: Run the test to see actual positions in error output**
```
Expected: [BLOCK_COMMENT(0, 0), ...]
Actual:   [BLOCK_COMMENT(16, 31), ...]
```

**Step 3: Verify the actual positions make sense by checking source string**
- Position 16 should be the `/*` in the source
- Position 31 should be just after `*/`
- Count manually to VERIFY the parser is correct, don't just copy

**Step 4: Update expected values after verification**
```java
// Verified: "/* comment */" spans positions 16-31
expected.allocateNode(NodeType.BLOCK_COMMENT, 16, 31);
```

**⚠️ CRITICAL**: The goal is NOT to blindly copy actual values. The goal is to:
1. See what the parser produces
2. VERIFY those positions match your understanding of where tokens should be
3. Only then update expected values

**Common position calculation errors:**
- **Off-by-one on text block indentation**: Text blocks strip leading whitespace differently than expected
- **Newline counting**: `\n` is 1 character, `\r\n` is 2 characters
- **Tab counting**: Tab is 1 character regardless of visual width
- **End position semantics**: End positions are typically exclusive (point AFTER the last character)

### 🚨 CRITICAL: Unit Test Requirements

**Parser unit tests MUST validate AST structure**, not just parsing success:

```java
// ❌ WRONG - Only verifies parsing doesn't crash
@Test
public void classLiteralInExpression()
{
    String source = "String s = String.class.getName();";
    assertParseSucceeds(source);  // Tests NOTHING about CLASS_LITERAL node!
}

// ❌ WRONG - isNotEmpty() is a WEAK assertion (equally bad as assertParseSucceeds)
@Test
public void recordPatternInSwitch()
{
    String source = "case Point(int x, int y) -> ...";
    Set<SemanticNode> actual = parseSemanticAst(source);
    requireThat(actual, "actual").isNotEmpty();  // Tests NOTHING about RECORD_PATTERN node!
}

// ❌ WRONG - isNotNull() on arena only verifies parsing succeeded, NOT AST correctness
@Test
public void primitiveTypePatternInSwitch()
{
    String source = "case int i -> ...";
    try (Parser parser = parse(source))
    {
        requireThat(parser.getArena(), "arena").isNotNull();  // Tests NOTHING about new node types!
    }
}

// ✅ CORRECT - Validates correct AST nodes are created
@Test
public void classLiteralInExpression()
{
    String source = "String s = String.class.getName();";
    Set<SemanticNode> actual = parseSemanticAst(source);
    Set<SemanticNode> expected = Set.of(
        semanticNode(COMPILATION_UNIT, 0, 60),
        semanticNode(CLASS_DECLARATION, 0, 59, "Test"),
        semanticNode(CLASS_LITERAL, 41, 53),  // Verify the new node type!
        // ... other expected nodes
    );
    requireThat(actual, "actual").isEqualTo(expected);
}
```

**Why `isNotEmpty()` / `isNotNull()` is WRONG for parser tests:**
- `isNotEmpty()`: Non-empty only means SOME nodes were produced - NOT that the CORRECT nodes exist
- `isNotNull()` on arena: Only verifies parsing completed - NOT that the AST structure is correct
- The new feature (e.g., `RECORD_PATTERN`, `PRIMITIVE_TYPE_PATTERN`) might not be created at all
- Test passes even if parser produces completely wrong AST structure
- Use `isEqualTo(expected)` with explicit expected nodes including the NEW node type

### Detection Patterns

- ✅ `parseSemanticAst\(.*\).*isEqualTo\(expected\)` - Correct AST validation
- ✅ `parser\.getArena\(\).*isEqualTo\(expected\)` - Correct arena comparison
- ❌ `parseSemanticAst\(.*\).*isNotEmpty\(\)` - Weak assertion, tests nothing about new feature
- ❌ `parser\.getArena\(\).*isNotNull\(\)` - Weak assertion, only verifies parsing succeeded
- ❌ `assertParseSucceeds\(` in `*ParserTest.java` - Wrong utility for unit tests
- ✅ `assertParseSucceeds\(` in `IntegrationTest.java` - Correct for integration tests

### Expected Value Derivation Anti-Patterns

- ❌ Copying `actual` output from failed test directly into `expected` - Tests nothing
- ❌ Comments like `// Based on actual parser output` - Indicates copied values
- ❌ Multiple identical node positions without manual verification
- ✅ Comments showing character position calculation - Evidence of manual derivation
- ✅ Step-by-step position comments matching source string analysis

---

## 🔍 DETECTION PATTERNS

### JPMS-Compliant Test Structure
- `module.*\.test\s*\{`
- `src/test/java/module-info\.java`
- `io\.github\.styler\..*\.test`

### Parallel Test Execution Requirements
- `@BeforeTest|@BeforeMethod|@BeforeClass` (violations)
- `@AfterTest|@AfterMethod|@AfterClass` (violations)
- `private\s+(?:static\s+)?(?!final)\w+.*=` (mutable fields)
- `private\s+\w+.*calculator.*=` (shared instances)
- `private\s+(?:ConfigSearchPath|ErrorReporter|ConfigDiscovery)\s+\w+;` (shared test fixtures)
- `Files\.createTempDirectory\("(?!.*UUID\.randomUUID)` (temp files without UUID isolation)

### Parallel Test Safety Rules
- `@Test.*public\s+void.*\{[^}]*new\s+\w+Calculator\(\)`
- `static\s+final\s+` (immutable constants)
- Test method creating instances locally

### Test Class Declaration
- ✅ `public\s+final\s+class\s+\w+Test` - Correct test class declaration
- ❌ `^class\s+\w+Test` - Missing public final (package-private)
- ❌ `public\s+class\s+\w+Test[^a-zA-Z]` - Missing final modifier

### Test Naming and Structure
- `@Test\s*public\s+void\s+[a-z][a-zA-Z0-9]*\(\)`
- `parseExpressionWithInvalidTokenThrowsParseException`
- `methodConditionExpectedResult` pattern

### Test Categories and Organization
- `@Test\s*\(.*groups\s*=\s*\{.*"(unit|integration|performance|fast|slow)".*\}`
- Test categorization patterns

### Parallel-Safe Test Data Creation
- `public\s+class\s+\w+TestBuilder\s*\{`
- `public\s+\w+TestBuilder\s+with\w+\(`
- `.build\(\)` method calls

### Parallel-Safe Test Utilities and Helpers
- `public\s+static\s+void\s+assert\w+\(`
- `TestUtils\.assert\w+\(`
- Stateless utility methods

### Parallel-Safe Test Data Management
- `loadTestCasesFromResource\(.*\.json.*\)`
- Read-only test data patterns

### Thread Safety Checklist
- `// ✅|❌` indicators
- `requireThat\(.*\)\.` assertions
- Instance creation within test methods

### Risk-Based Test Coverage Philosophy
- `// RISK:.*→` (business risk documentation)
- `// IMPACT:.*-` (impact assessment)
- `// BUSINESS RULE:` (business rule validation)
- `// USER IMPACT:` (user workflow impact)
- `// BUG RISK:` (bug probability assessment)

### Priority 1: Business Logic Scenarios
- `mergeConfigs[A-Z][a-zA-Z0-9]*\(\)` (config merge business logic)
- `parseTomlWithInvalidBusinessRuleRejectsClearly`
- Business rule validation tests
- User workflow validation

### Priority 2: Happy Path Validation
- `parseTomlWithValidConfigReturnsCorrectObject`
- Standard use case validation
- Core feature validation

### Priority 3: Edge Cases
- `parseTomlWithMaxBoundaryValue[A-Z]`
- `loadConfigWithConcurrentAccess[A-Z]`
- Boundary value tests
- Concurrency tests

### Priority 4: Error Handling
- `parseTomlWithMalformedSyntaxProvidesActionableError`
- `try\s*\{[^}]*parser\.parse\([^)]*\);[^}]*fail\(.*Expected`
- Error message validation
- Actionable error patterns

### Metric-Driven Testing Anti-Patterns
- `testGetterReturnsField\(\)` (coverage-only test)
- `testPrivateHelperMethodLogic\(\)` (implementation detail test)
- Tests without business logic validation

### Test Value Anti-Patterns (Meaningless Tests)
- `shouldHave\w+Value\(\)` (compile-time guarantee tests)
- `shouldHaveExactly\w+Values\(\)` (implementation detail tests)
- `shouldContainAllExpected\w+\(\)` (enum/collection structure tests)
- `\.values\(\)\)\.hasSize\(` (enum count assertions)
- `\.values\(\)\)\.containsExactly\(` (enum structure assertions)
- `assertThat\(\w+\.\w+\)\.isNotNull\(\)` without business logic (trivial null checks)
- `shouldReturn\w+List\(\)` testing collection type (implementation detail)
- Tests verifying Java language guarantees (record fields, enum values exist)

### Risk-Driven Testing Best Practices
- `discoverConfigWithGitBoundaryStopsAtRepositoryRoot`
- Repository boundary validation
- Critical business rule tests
- User workflow impact tests

---

## 📋 VALIDATION RULES

**Critical Requirements:**
1. JPMS Compliance - Test module descriptors required
2. Parallel Safety - No shared mutable state
3. Isolation - Instance creation per test method
4. Thread Safety - Stateless utilities only
5. Risk Documentation - Business risk, impact, and bug risk comments
6. Test Framework - TestNG only (NOT JUnit)
7. Test Value - No compile-time guarantee or implementation detail tests

### Test Dependency Detection
- ✅ `org\.testng` - Correct test framework
- ❌ `org\.junit` - Wrong framework, use TestNG
- ✅ `io\.github\.cowwoc\.requirements12` - Correct assertion library (Requirements API)
- ❌ `org\.assertj` - Wrong assertion library, use Requirements API
- ✅ `logback-classic` - Required for test logging
- ✅ `logback-test\.xml` - Required configuration file

### Assertion Pattern Detection
- ✅ `requireThat\(` - Correct assertion pattern (Requirements API)
- ❌ `assertThat\(` - Wrong assertion pattern, use requireThat

**Standard Requirements:**
1. Test Class Declaration - `public final class *Test`
2. Naming Convention - `methodConditionExpectedResult` (camelCase, no underscores)
3. Test Organization - TestNG groups for categorization
4. Builder Pattern - For complex test data creation
5. Resource Management - Read-only external data
6. Test Prioritization - Business logic > Happy path > Edge cases > Error handling

**Risk-Based Testing Requirements:**
1. Business Logic First - Critical workflows validated before edge cases
2. Risk Documentation - All high-priority tests document RISK, IMPACT, BUSINESS RULE
3. Bug Prevention Focus - Tests target real user-impacting bugs, not coverage metrics
4. Regression Tests - Every discovered bug gets a dedicated regression test
5. Actionable Errors - Error handling tests validate error message usefulness

**Detection Priority:**
- 🔴 CRITICAL: Parallel test violations, JPMS structure, missing business logic tests, meaningless tests
- 🟡 STANDARD: Naming conventions, organization patterns, risk documentation
- 🟢 OPTIONAL: Advanced patterns, optimizations

---

**Related Documentation:**
- `testing-human.md` - Human-readable testing explanations
- `java-claude.md` - Java-specific Claude patterns
- `common-claude.md` - Universal code style patterns