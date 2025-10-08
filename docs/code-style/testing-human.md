# Testing Style and Conventions

🧪 **Testing patterns, JPMS-compliant structure, and test organization standards**

This document contains testing conventions, test structure requirements, and testing patterns for Java projects in Styler Java Code Formatter.

---

## Table of Contents
- [🚨 Critical Requirements](#-critical-requirements)
- [⚠️ Standard Conventions](#️-standard-conventions)  
- [💡 Advanced Patterns](#-advanced-patterns)
- [Related Documentation](#related-documentation)

---

## 🚨 CRITICAL REQUIREMENTS

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
	public void parseClass_withValidInput_returnsExpectedAST()
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
	public void parseMethod_withComplexSyntax_handlesCorrectly()
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

---

## ⚠️ STANDARD CONVENTIONS

### Test Naming and Structure

```java
// ✅ Descriptive test method names using pattern: method_condition_expectedResult
@Test
public void parseExpression_withInvalidToken_throwsParseException()
{
}

@Test
public void parseExpression_withValidSyntax_returnsCorrectAST()
{
}

@Test
public void formatCode_withMaxLineLength_breaksAtCorrectPosition()
{
}
```

### Test Categories and Organization

```java
// ✅ Use TestNG groups for test categorization - each test remains isolated
public class JavaParserTest
{
	@Test(groups = {"unit", "fast"})
	public void parseBasicExpression_withValidInput_returnsCorrectAST()
	{
		// ✅ Instance created per test
		JavaParser parser = new JavaParser();
		// Test implementation...
	}

	@Test(groups = {"integration", "slow"})
	public void parseComplexFile_withRealCode_matchesExpectedStructure()
	{
		// ✅ Isolated instance
		JavaParser parser = new JavaParser();
		// Test implementation...
	}

	@Test(groups = {"performance"})
	public void parseFile_withLargeCodebase_completesWithinTimeLimit()
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
public void parseClass_withComplexStructure_buildsCorrectAST()
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
public void formatCode_withLongLines_breaksAtSemanticBoundaries()
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
public void parseClass_withTestDataFromFile_matchesExpectedResults() throws IOException
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
public void parseExpression_withVariousSyntax_parsesCorrectly()
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

## Related Documentation

- **Language-Specific Implementation**: 
  - [`java-human.md`](java-human.md) - Java patterns and best practices
  - [`common-human.md`](common-human.md) - Universal coding principles

- **Claude Detection Patterns**: 
  - [`testing-claude.md`](testing-claude.md) - Testing validation patterns for Claude Code

- **Project Context**:
  - [`../../project/build-system.md`](../../project/build-system.md) - Maven dependency management and build configuration
  
**Business Context**: These testing conventions ensure that Java parser and formatter components are reliable, maintainable, and can be validated efficiently during the rapid iteration cycles required for language feature support and performance optimization.