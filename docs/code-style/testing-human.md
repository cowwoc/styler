# Testing Style and Conventions

🧪 **Testing patterns, JPMS-compliant structure, and test organization standards**

This document contains testing conventions, test structure requirements, and testing patterns for Java
projects in Styler Java Code Formatter.

---

## Table of Contents
- [🚨 Critical Requirements](#-critical-requirements)
- [⚠️ Standard Conventions](#️-standard-conventions)  
- [💡 Advanced Patterns](#-advanced-patterns)
- [Related Documentation](#related-documentation)

---

## 🚨 CRITICAL REQUIREMENTS

### Test Framework Dependencies

**Use TestNG exclusively** (NOT JUnit). Required test dependencies in each module's `pom.xml`:

```xml
<dependencies>
	<dependency>
		<groupId>org.testng</groupId>
		<artifactId>testng</artifactId>
		<scope>test</scope>
	</dependency>
	<dependency>
		<groupId>io.github.cowwoc.requirements</groupId>
		<artifactId>requirements-java</artifactId>
		<scope>test</scope>
	</dependency>
	<dependency>
		<groupId>ch.qos.logback</groupId>
		<artifactId>logback-classic</artifactId>
		<scope>test</scope>
	</dependency>
</dependencies>
```

**Use Requirements API for assertions** (NOT AssertJ):
```java
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

// ✅ CORRECT - Requirements API
requireThat(result, "result").isNotNull();
requireThat(result.hasErrors(), "hasErrors").isFalse();

// ❌ WRONG - AssertJ
assertThat(result).isNotNull();
assertThat(result.hasErrors()).isFalse();
```

Each module with tests MUST have `src/test/resources/logback-test.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
	<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
		<encoder>
			<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
		</encoder>
	</appender>

	<root level="WARN">
		<appender-ref ref="CONSOLE"/>
	</root>
</configuration>
```

### JPMS-Compliant Test Structure

**This project uses Java Platform Module System (JPMS).** Tests MUST follow documented structure:

1. **Separate Test Packages**:
   - Main: `io.github.styler.parser`
   - Test: `io.github.styler.parser.test`

2. **Test Module Descriptor** - Required `src/test/java/module-info.java`:
   ```java
   module io.github.styler.parser.test
   {
	   requires io.github.styler.parser;
	   requires org.testng;

	   opens io.github.styler.parser.test to org.testng;
	   opens io.github.styler.parser.test.ast to org.testng;
   }
   ```

### Parallel Test Execution Requirements

**ALL tests MUST be able to run in parallel.** This is non-negotiable for performance and CI/CD reliability.

#### ❌ **PROHIBITED Patterns - Do NOT Use:**
```java
// ❌ NEVER use @BeforeTest, @BeforeMethod, @BeforeClass for shared state
public class BadJavaParserTest
{
	// ❌ Shared instance causes race conditions
	private JavaParser parser;

	@BeforeMethod  // ❌ Creates shared state between parallel tests
	public void setUp()
	{
		parser = new JavaParser();
	}
}

// ❌ NEVER use static fields that are modified by tests
public class BadTest
{
	// ❌ Shared mutable state
	private static List<String> testData = new ArrayList<>();
}

// ❌ Real violation example from refactoring: Shared test fixtures
public class BadConfigSearchPathTest
{
	// ❌ Shared mutable instance field
	private ConfigSearchPath searchPath;

	@BeforeMethod  // ❌ Creates shared state
	public void setUp()
	{
		searchPath = new ConfigSearchPath();
	}

	@Test
	public void test1() { /* Uses shared searchPath */ }

	@Test
	public void test2() { /* Uses shared searchPath - race condition! */ }
}

// ❌ Real violation: Temp files without UUID isolation
public class BadTestWithTempFiles
{
	@Test
	public void testWithTempDir() throws IOException
	{
		// ❌ Multiple parallel tests create conflicting temp directories
		Path tempDir = Files.createTempDirectory("test-prefix");
		// Tests can interfere with each other!
	}
}
```

#### ✅ **REQUIRED Pattern - Isolated Test Setup:**
```java
public class JavaParserTest
{
	@Test
	public void parseClassWithValidInputReturnsExpectedAST()
	{
		// ✅ Create all required objects inside each test method
		JavaParser parser = new JavaParser();
		String sourceCode = "public class Test {}";
		ParseOptions options = ParseOptions.standard();

		// Act
		ParseResult result = parser.parseClass(sourceCode, options);

		// Assert
		requireThat(result, "result").isNotNull();
		requireThat(result.hasErrors(), "hasErrors").isFalse();
	}

	@Test
	public void parseMethodWithComplexSyntaxHandlesCorrectly()
	{
		// ✅ Each test creates its own isolated instances
		JavaParser parser = new JavaParser();
		String complexSource = "public <T> Optional<T> getValue() { return Optional.empty(); }";
		ParseOptions options = ParseOptions.standard();

		ParseResult result = parser.parseMethod(complexSource, options);

		requireThat(result.getAst(), "ast").isNotNull();
	}
}

// ✅ Real solution from refactoring: Helper class for test fixtures
public class ConfigSearchPathTest
{
	/**
	 * Helper class for test fixtures (no @BeforeMethod needed).
	 */
	private static class TestFixtures
	{
		final ConfigSearchPath searchPath;

		TestFixtures()
		{
			this.searchPath = new ConfigSearchPath();
		}
	}

	@Test
	public void buildSearchPathFromSingleDirectory() throws IOException
	{
		// ✅ Each test creates its own fixtures instance
		TestFixtures fixtures = new TestFixtures();
		Path tempDir = Files.createTempDirectory("test-" + UUID.randomUUID());

		List<Path> result = fixtures.searchPath.buildSearchPath(tempDir);

		requireThat(result, "result").isNotEmpty();
	}

	@Test
	public void buildSearchPathTraversesParents() throws IOException
	{
		// ✅ Completely isolated from other test
		TestFixtures fixtures = new TestFixtures();
		Path tempDir = Files.createTempDirectory("test-" + UUID.randomUUID());

		// Test implementation...
	}
}

// ✅ Real solution: UUID-based temp file isolation
public class ConfigDiscoveryTest
{
	@Test
	public void discoverConfigWithTomlFile() throws IOException
	{
		// ✅ UUID ensures unique temp directory per test execution
		Path tempProjectDir = Files.createTempDirectory("styler-config-test-" + UUID.randomUUID());
		ConfigDiscovery configDiscovery = new ConfigDiscovery();

		// Test can run in parallel without conflicts
		Path configFile = tempProjectDir.resolve(".styler.toml");
		Files.writeString(configFile, "maxLineLength = 120");

		ConfigDiscovery.DiscoveryResult result = configDiscovery.discoverWithLocations(tempProjectDir, null);

		requireThat(result.getConfiguration(), "configuration").isNotNull();
	}
}
```

### Parallel Test Safety Rules

1. **NO SHARED MUTABLE STATE**: Never use instance fields or static fields that are modified by tests
2. **NO SETUP/TEARDOWN**: Avoid `@BeforeTest`, `@BeforeMethod`, `@BeforeClass`, `@AfterTest`, `@AfterMethod`, `@AfterClass`
3. **ISOLATED DATA**: Create all test data inside individual test methods
4. **IMMUTABLE CONSTANTS**: Only use `static final` fields for truly immutable constants
5. **THREAD-SAFE UTILITIES**: Ensure any shared utility methods are stateless and thread-safe

```java
// ✅ PREFERRED: Immutable constants are safe for parallel execution
public class TestConstants
{
	// ✅ Immutable constant
	public static final double TYPICAL_SALARY = 75_000.0;
	// ✅ Immutable constant
	public static final int RETIREMENT_AGE = 65;
	// ✅ Immutable constant
	public static final Province DEFAULT_PROVINCE = Province.ONTARIO;
}

// ❌ AVOID: Mutable shared state
public class BadTestConstants
{
	// ❌ Mutable shared state
	public static List<Person> testPersons = new ArrayList<>();
	// ❌ Mutable shared state
	public static int testCounter = 0;
}
```

### Test Value Requirements

**Tests must verify runtime behavior and business logic, not compile-time guarantees or implementation details.**

#### ❌ **PROHIBITED - Meaningless Tests (Compile-Time Guarantees):**
```java
// ❌ Tests compile-time guarantee - enum always has these values
@Test
public void shouldHaveAiValue()
{
	assertThat(Audience.AI).isNotNull();
}

@Test
public void shouldHaveHumanValue()
{
	assertThat(Audience.HUMAN).isNotNull();
}

// ❌ Tests that a record has fields - compiler guarantees this
@Test
public void shouldHaveDescriptionField()
{
	FixSuggestion fix = new FixSuggestion("desc", List.of("step"));
	assertThat(fix.description()).isEqualTo("desc");
}
```

#### ❌ **PROHIBITED - Implementation Detail Tests:**
```java
// ❌ Tests implementation detail - business logic doesn't care about enum count
@Test
public void shouldHaveExactlyTwoValues()
{
	Audience[] values = Audience.values();
	assertThat(values).hasSize(2);
}

// ❌ Tests internal structure - if a third value is added, test fails but nothing broke
@Test
public void shouldContainAllExpectedValues()
{
	assertThat(Audience.values()).containsExactly(Audience.AI, Audience.HUMAN);
}

// ❌ Tests implementation choice - business doesn't care about internal collection type
@Test
public void stepsShouldReturnImmutableList()
{
	FixSuggestion fix = new FixSuggestion("desc", List.of("step"));
	assertThatThrownBy(() -> fix.steps().add("new"))
		.isInstanceOf(UnsupportedOperationException.class);
}
```

#### ✅ **REQUIRED - Tests with Value (Business Logic):**
```java
// ✅ Tests runtime behavior - actual logic that could fail
@Test
public void detectReturnsAiWhenClaudeSessionIdSet()
{
	// Tests environment detection logic - this is business behavior
}

@Test
public void formatForAiProducesValidJson()
{
	// Tests actual formatting logic - this is user-visible behavior
}

@Test
public void constructorRejectsEmptyDescription()
{
	// Tests validation logic - prevents invalid data
	assertThatThrownBy(() -> new FixSuggestion("", List.of("step")))
		.isInstanceOf(IllegalArgumentException.class);
}

// ✅ Tests business rule - merge precedence affects user experience
@Test
public void mergeConfigsProjectOverridesUser()
{
	// Tests that nearest config wins - this is documented behavior
}
```

#### Decision Criteria

**Ask these questions before writing a test:**

1. **Does this test business logic?** If the code changes but behavior is identical, would the test still pass?
2. **Would a bug here affect users?** If this logic fails, does something user-visible break?
3. **Is this a documented contract?** Is the tested behavior part of the API contract or just an implementation choice?

**Rules:**
- If removing the test would not reduce confidence in the code's correctness, the test has no value.
- If the test would break when refactoring without changing behavior, it tests implementation details.
- Tests should verify WHAT the code does, not HOW it does it internally.

### Parser Test Patterns

Parser tests have two distinct utilities with different purposes:

| Utility | Purpose | Use Case |
|---------|---------|----------|
| `parseSemanticAst()` | Parse + return AST nodes | Unit tests that validate node types |
| `assertParseSucceeds()` | Parse only, verify no crash | Integration/smoke tests on real files |

#### When to Use Each

**`parseSemanticAst()` + AST comparison** (REQUIRED for ALL feature tests):
- Testing a new parser feature (e.g., class literals, generics)
- Verifying specific node types are created
- Testing node positions/attributes
- **Any test validating parser correctness**

**`assertParseSucceeds()` - Limited Value** (consider avoiding):
- ONLY legitimate use: Smoke tests on real project files where maintaining expected AST is impractical
- **Does NOT validate semantic correctness** - only that parser doesn't crash
- **Prefer `parseSemanticAst()` for all new tests** - it validates both success AND correctness

#### Critical: Deriving Expected Values Manually

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

#### Practical Technique: Verify Position Calculations

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

#### Parser Unit Test Requirements

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

**Why `isNotEmpty()` is WRONG for parser tests:**
- Non-empty only means SOME nodes were produced - NOT that the CORRECT nodes exist
- The new feature (e.g., `RECORD_PATTERN`, `GUARDED_PATTERN`) might not be created at all
- Test passes even if parser produces completely wrong AST structure
- Use `isEqualTo(expected)` with explicit expected nodes including the NEW node type

---

## ⚠️ STANDARD CONVENTIONS

### Test Class Declaration

**All test classes should be `public final`:**

```java
// ✅ CORRECT - Test class is public final
public final class JavaParserTest
{
	@Test
	public void parseExpressionWithValidSyntaxReturnsCorrectAST()
	{
		// Test implementation
	}
}

// ❌ WRONG - Package-private or non-final test class
class JavaParserTest  // Missing public final
{
}
```

**Rationale:**
- `public` - Required by some test frameworks for reflection access
- `final` - Test classes should not be extended; each test class is self-contained

### Test Naming and Structure

```java
// ✅ Descriptive test method names using pattern: methodConditionExpectedResult
@Test
public void parseExpressionWithInvalidTokenThrowsParseException()
{
}

@Test
public void parseExpressionWithValidSyntaxReturnsCorrectAST()
{
}

@Test
public void formatCodeWithMaxLineLengthBreaksAtCorrectPosition()
{
}
```

### Test Categories and Organization

```java
// ✅ Use TestNG groups for test categorization - each test remains isolated
public class JavaParserTest
{
	@Test(groups = {"unit", "fast"})
	public void parseBasicExpressionWithValidInputReturnsCorrectAST()
	{
		// ✅ Instance created per test
		JavaParser parser = new JavaParser();
		// Test implementation...
	}

	@Test(groups = {"integration", "slow"})
	public void parseComplexFileWithRealCodeMatchesExpectedStructure()
	{
		// ✅ Isolated instance
		JavaParser parser = new JavaParser();
		// Test implementation...
	}

	@Test(groups = {"performance"})
	public void parseFileWithLargeCodebaseCompletesWithinTimeLimit()
	{
		// ✅ Isolated instance
		JavaParser parser = new JavaParser();
		// Test implementation...
	}
}
```

---

## 💡 ADVANCED PATTERNS

### Parallel-Safe Test Data Creation

```java
// ✅ Test data builders are parallel-safe (each test creates its own builder instance)
public class PersonTestBuilder
{
	private int age = 35;
	// ✅ Uses immutable constant
	private double salary = TestConstants.TYPICAL_SALARY;
	private Province province = TestConstants.DEFAULT_PROVINCE;
	
	public PersonTestBuilder withAge(int age)
	{
		this.age = age;
		return this;
	}
	
	public PersonTestBuilder withSalary(double salary)
	{
		this.salary = salary;
		return this;
	}
	
	public Person build()
	{
		return new Person(age, salary, province);
	}
}

// ✅ Parallel-safe usage: Each test creates its own instances
@Test
public void parseClassWithComplexStructureBuildsCorrectAST()
{
	// ✅ Each test method creates its own parser and data
	JavaParser parser = new JavaParser();
	SourceCode source = new SourceCodeTestBuilder().
		withClassName("ComplexClass").
		withMethodCount(10).
		build();

	ASTNode ast = parser.parse(source);

	requireThat(ast.getChildCount(), "childCount").isGreaterThan(5);
}

@Test
public void formatCodeWithLongLinesBreaksAtSemanticBoundaries()
{
	// ✅ Completely isolated from other tests
	CodeFormatter formatter = new CodeFormatter();
	SourceCode source = new SourceCodeTestBuilder().
		withLineLength(TestConstants.MAX_LINE_LENGTH).
		withContent(TestConstants.LONG_METHOD).
		build();

	String formatted = formatter.format(source);

	requireThat(formatted.lines().count(), "lineCount").isGreaterThan(1);
}
```

### Parallel-Safe Test Utilities and Helpers

```java
// ✅ Thread-safe utilities (stateless methods only)
public class TestUtils
{
	// ✅ Stateless factory methods are parallel-safe
	public static SourceCode createTypicalClass()
	{
		// ✅ Creates new instance each time
		return new SourceCodeTestBuilder().build();
	}
	
	// ✅ Pure functions with no shared state are parallel-safe
	public static void assertParseResultValid(String sourceCode, ParseResult result)
	{
		requireThat(result, "result").isNotNull();
		requireThat(result.hasErrors(), "hasErrors").isFalse();
		// Reasonable token count
		requireThat(result.getTokenCount(), "tokenCount").isGreaterThan(0);
	}
	
	// ✅ Pure mathematical functions are parallel-safe
	public static void assertWithinTolerance(double expected, double actual, double tolerance)
	{
		double difference = Math.abs(expected - actual);
		requireThat(difference, "difference").isLessThan(tolerance);
	}
}
```

### Parallel-Safe Test Data Management

```java
// ✅ Resource files are parallel-safe (read-only operations)
@Test
public void parseClassWithTestDataFromFileMatchesExpectedResults() throws IOException
{
	// ✅ Each test creates its own parser instance
	JavaParser parser = new JavaParser();

	// ✅ Loading read-only resources is parallel-safe
	List<ParseTestCase> testCases = loadTestCasesFromResource("/parse-test-cases.json");

	for (ParseTestCase testCase : testCases)
	{
		ParseResult actualResult = parser.parseClass(testCase.getSourceCode(), testCase.getOptions());
		ParseResult expectedResult = testCase.getExpectedResult();

		// ✅ Stateless utility methods are parallel-safe
		TestUtils.assertParseResultValid(testCase.getSourceCode(), actualResult);
	}
}

// ✅ Example of parallel-safe parameterized-style testing
@Test
public void parseExpressionWithVariousSyntaxParsesCorrectly()
{
	// ✅ Create test data as local variables (no shared state)
	JavaParser parser = new JavaParser();

	Map<String, Integer> sourceToExpectedTokens = Map.of(
		"a + b", 3,
		"method(arg1, arg2)", 6,
		"obj.field.getValue()", 7
	);

	// ✅ Each iteration uses isolated data
	for (Map.Entry<String, Integer> testCase : sourceToExpectedTokens.entrySet())
	{
		String sourceCode = testCase.getKey();
		int expectedTokens = testCase.getValue();

		ParseResult actualResult = parser.parseExpression(sourceCode, ParseOptions.standard());

		TestUtils.assertWithinTolerance(expectedTokens, actualResult.getTokenCount(), 1);
	}
}
```

### Thread Safety Checklist

Before submitting any test class, verify:

- [ ] **No instance fields** that store mutable objects
- [ ] **No static fields** that are modified by tests  
- [ ] **No `@Before*` or `@After*` annotations** that modify shared state
- [ ] **All test data created** inside individual test methods
- [ ] **All utility methods** are stateless (pure functions)
- [ ] **All shared constants** are truly immutable (`static final` primitives/strings)
- [ ] **Resource loading** uses only read-only operations
- [ ] **External dependencies** (databases, files) use unique identifiers per test if needed

---

## 🎯 RISK-BASED TEST COVERAGE PHILOSOPHY

### Red-Blue Team Testing Approach

**CRITICAL PRINCIPLE**: Prioritize **business risk coverage** over metric-driven coverage targets (e.g., "95%
line coverage"). The goal is **bug prevention effectiveness**, not coverage percentages.

**Test Priority Hierarchy** (inspired by security Red-Blue team methodology):

#### Priority 1: BUSINESS LOGIC SCENARIOS (Red Team - Attack Surface)
**Focus**: What breaks critical user workflows and business rules?

```java
// ✅ HIGH PRIORITY: Tests critical business logic
@Test
public void mergeConfigsProjectOverridesUserNearestConfigWins()
{
	// RISK: Wrong precedence → user sets global config but project ignores it
	// IMPACT: High - violates user's mental model, breaks configuration hierarchy
	// BUSINESS RULE: Nearest config file always wins for conflicting fields

	Config userConfig = parseConfig("~/.styler.toml", "maxLineLength = 100");
	Config projectConfig = parseConfig("project/.styler.toml", "maxLineLength = 120");

	Config merged = merger.merge(userConfig, projectConfig);

	requireThat(merged.getMaxLineLength(), "maxLineLength").isEqualTo(120);
}

@Test
public void parseTomlWithInvalidBusinessRuleRejectsClearly()
{
	// RISK: Silent acceptance of invalid config → user confusion when formatter behaves unexpectedly
	// IMPACT: High - data corruption, incorrect formatting, user trust damage
	// BUSINESS RULE: maxLineLength must be positive (1-500 range)

	try
	{
		parser.parse("maxLineLength = -1");
		fail("Expected IllegalArgumentException for negative maxLineLength");
	}
	catch (IllegalArgumentException e)
	{
		// Verify error message guides user to fix
		requireThat(e.getMessage(), "errorMessage").contains("maxLineLength");
		requireThat(e.getMessage(), "errorMessage").contains("must be positive");
		requireThat(e.getMessage(), "errorMessage").contains("Received: -1");
	}
}
```

#### Priority 2: HAPPY PATH VALIDATION
**Focus**: Core features work correctly for typical usage.

```java
// ✅ MEDIUM PRIORITY: Validates standard workflows
@Test
public void parseTomlWithValidConfigReturnsCorrectObject()
{
	// Standard use case: user provides valid TOML config
	String toml = "maxLineLength = 120\nindentSize = 4";

	Config result = parser.parse(toml);

	requireThat(result.getMaxLineLength(), "maxLineLength").isEqualTo(120);
	requireThat(result.getIndentSize(), "indentSize").isEqualTo(4);
}
```

#### Priority 3: EDGE CASES (Blue Team - Defense)
**Focus**: Boundary values, unusual inputs, concurrent access.

```java
// ✅ LOWER PRIORITY: Edge case validation (after business logic covered)
@Test
public void parseTomlWithMaxBoundaryValueAccepts()
{
	// Edge case: maximum allowed line length
	Config result = parser.parse("maxLineLength = 500");
	requireThat(result.getMaxLineLength(), "maxLineLength").isEqualTo(500);
}

@Test
public void loadConfigWithConcurrentAccessMaintainsConsistency() throws Exception
{
	// Concurrency edge case: multiple threads loading same config
	ExecutorService executor = Executors.newFixedThreadPool(100);
	List<Future<Config>> futures = new ArrayList<>();

	for (int i = 0; i < 100; i++)
	{
		futures.add(executor.submit(() -> configLoader.load(configPath)));
	}

	// All threads should get consistent results
	Config expected = futures.get(0).get();
	for (Future<Config> future : futures)
	{
		requireThat(future.get(), "config").isEqualTo(expected);
	}
}
```

#### Priority 4: ERROR HANDLING
**Focus**: Fail-fast validation and actionable error messages.

```java
// ✅ ERROR PATH: After business logic tests
@Test
public void parseTomlWithMalformedSyntaxProvidesActionableError()
{
	String invalidToml = "maxLineLength = \n";  // Incomplete assignment

	try
	{
		parser.parse(invalidToml);
		fail("Expected TomlParseException");
	}
	catch (TomlParseException e)
	{
		// Error must help user fix the problem
		requireThat(e.getMessage(), "errorMessage").contains("line 1");
		requireThat(e.getMessage(), "errorMessage").matches(".*column \\d+.*");
		requireThat(e.getMessage(), "errorMessage").contains("incomplete assignment");
	}
}
```

### Metrics De-Emphasis

**De-Prioritized Metrics** (outcome, not goal):
- ❌ "Must achieve 95% line coverage" - Coverage is a **side effect** of comprehensive business logic testing
- ❌ "Need 67+ test methods" - Test **count** is irrelevant; **business risk coverage** matters
- ❌ "Every branch must be tested" - Focus on **business-critical branches** first

**Prioritized Metrics** (actual goals):
- ✅ **Business risk coverage**: All critical workflows and business rules validated
- ✅ **Bug prevention effectiveness**: Tests catch real bugs that would impact users
- ✅ **Regression prevention**: Every discovered bug gets a test to prevent recurrence

### Test Design Questions

**Before writing any test, ask:**
1. **What business rule does this validate?** (If none, de-prioritize)
2. **What user workflow breaks if this fails?** (High impact = high priority)
3. **How likely is this scenario to cause bugs?** (Common paths > rare edge cases)
4. **Does this test prevent real bugs or just increase coverage?** (Bug prevention > metrics)

### Anti-Pattern: Metric-Driven Testing

```java
// ❌ ANTI-PATTERN: Writing tests just to hit coverage targets
@Test
public void testGetterReturnsField()
{
	// This adds line coverage but provides zero bug prevention value
	Config config = new Config(120, 4);
	requireThat(config.getMaxLineLength(), "maxLineLength").isEqualTo(120);
}

// ❌ ANTI-PATTERN: Testing implementation details instead of business logic
@Test
public void testPrivateHelperMethodLogic()
{
	// Tests internal implementation, not user-visible behavior
	// Breaks if refactoring changes implementation (even if behavior unchanged)
}
```

### Best Practice: Risk-Driven Testing

```java
// ✅ BEST PRACTICE: Test critical business logic that users depend on
@Test
public void discoverConfigWithGitBoundaryStopsAtRepositoryRoot()
{
	// BUSINESS RULE: Config discovery must not cross repository boundaries
	// USER IMPACT: Parent repo config should not affect nested projects
	// BUG RISK: High - easy to accidentally traverse past .git directory

	// Setup: root/.styler.toml, root/project/.git/, root/project/src/
	Path rootConfig = createTempConfig(rootDir, "maxLineLength = 80");
	Path gitDir = rootDir.resolve("project/.git");
	Files.createDirectories(gitDir);
	Path workingDir = rootDir.resolve("project/src");
	Files.createDirectories(workingDir);

	// Act: Search from working dir
	Optional<Path> found = discovery.discover(workingDir);

	// Assert: Should NOT find root config (stopped at .git boundary)
	requireThat(found.isEmpty(), "foundConfig").isTrue();
}
```

### Success Criteria

**Implementation is adequately tested when:**
- ✅ All **critical business rules** have dedicated tests
- ✅ All **major user workflows** are validated end-to-end
- ✅ All **discovered bugs** have regression tests
- ✅ **Error messages** are validated for actionability
- ✅ **Common failure scenarios** are tested

**NOT when:**
- ❌ Coverage percentage reaches arbitrary threshold
- ❌ Test count exceeds some number
- ❌ Every single branch is tested (regardless of business criticality)

---

## Related Documentation

- **Language-Specific Implementation**: 
  - [`java-human.md`](java-human.md) - Java patterns and best practices
  - [`common-human.md`](common-human.md) - Universal coding principles

- **Claude Detection Patterns**: 
  - [`testing-claude.md`](testing-claude.md) - Testing validation patterns for Claude Code

- **Project Context**:
  -  [`../../project/build-system.md`](../../project/build-system.md) - Maven dependency management and build
    configuration
  
**Business Context**: These testing conventions ensure that Java parser and formatter components are reliable,
maintainable, and can be validated efficiently during the rapid iteration cycles required for language feature
support and performance optimization.
