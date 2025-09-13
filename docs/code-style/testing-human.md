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
public class BadTaxCalculatorTest
{
	// ❌ Shared instance causes race conditions
	private TaxCalculator calculator;
	
	@BeforeMethod  // ❌ Creates shared state between parallel tests
	public void setUp()
	{
		calculator = new TaxCalculator();
	}
}

// ❌ NEVER use static fields that are modified by tests
public class BadTest
{
	// ❌ Shared mutable state
	private static List<String> testData = new ArrayList<>();
}
```

#### ✅ **REQUIRED Pattern - Isolated Test Setup:**
```java
public class TaxCalculatorTest
{
	@Test
	public void calculateTax_withValidInput_returnsExpectedResult()
	{
		// ✅ Create all required objects inside each test method
		TaxCalculator calculator = new TaxCalculator();
		double income = 50_000.0;
		Province province = Province.ONTARIO;
		
		// Act
		double result = calculator.calculateTax(income, province);
		
		// Assert
		requireThat(result, "result").isGreaterThan(0.0);
		requireThat(result, "result").isLessThan(income);
	}
	
	@Test
	public void calculateTax_withHighIncome_appliesCorrectBracket()
	{
		// ✅ Each test creates its own isolated instances
		TaxCalculator calculator = new TaxCalculator();
		double highIncome = 200_000.0;
		Province province = Province.ONTARIO;
		
		double result = calculator.calculateTax(highIncome, province);
		
		requireThat(result, "result").isGreaterThan(50_000.0);
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
public void calculateFederalTax_withNegativeIncome_throwsIllegalArgumentException()
{
}

@Test  
public void calculateFederalTax_withValidIncome_returnsCorrectAmount()
{
}

@Test
public void calculateOAS_atAge65WithFullResidency_returnsMaximumAmount()
{
}
```

### Test Categories and Organization

```java
// ✅ Use TestNG groups for test categorization - each test remains isolated
public class TaxCalculationTest
{
	@Test(groups = {"unit", "fast"})
	public void calculateBasicTax_withValidInput_returnsCorrectAmount() 
	{
		// ✅ Instance created per test
		TaxCalculator calculator = new TaxCalculator();
		// Test implementation...
	}
	
	@Test(groups = {"integration", "slow"}) 
	public void calculateTaxWithDatabase_withRealData_matchesExpectedResults() 
	{
		// ✅ Isolated instance
		TaxCalculator calculator = new TaxCalculator();
		// Test implementation...
	}
	
	@Test(groups = {"performance"})
	public void calculateTax_withLargeDataset_completesWithinTimeLimit() 
	{
		// ✅ Isolated instance
		TaxCalculator calculator = new TaxCalculator();
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
public void calculateTax_withHighIncome_appliesCorrectBracket()
{
	// ✅ Each test method creates its own calculator and data
	TaxCalculator calculator = new TaxCalculator();
	Person person = new PersonTestBuilder().
		withAge(45).
		withSalary(150_000.0).
		build();
		
	double tax = calculator.calculateTax(person);
	
	requireThat(tax, "tax").isGreaterThan(30_000.0);
}

@Test 
public void calculateTax_withRetirementAge_appliesAgeCredit()
{
	// ✅ Completely isolated from other tests
	TaxCalculator calculator = new TaxCalculator();
	Person person = new PersonTestBuilder().
		withAge(TestConstants.RETIREMENT_AGE).
		withSalary(TestConstants.TYPICAL_SALARY).
		build();
		
	double tax = calculator.calculateTax(person);
	
	requireThat(tax, "tax").isLessThan(15_000.0);
}
```

### Parallel-Safe Test Utilities and Helpers

```java
// ✅ Thread-safe utilities (stateless methods only)
public class TestUtils
{
	// ✅ Stateless factory methods are parallel-safe
	public static Person createTypicalPerson()
	{
		// ✅ Creates new instance each time
		return new PersonTestBuilder().build();
	}
	
	// ✅ Pure functions with no shared state are parallel-safe
	public static void assertTaxCalculationValid(double income, double tax)
	{
		requireThat(tax, "tax").isNotNegative();
		requireThat(tax, "tax").isLessThan(income);
		// Reasonable max rate
		requireThat(tax / income, "tax rate").isLessThan(0.5);
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
public void calculateTax_withTestDataFromFile_matchesExpectedResults() throws IOException
{
	// ✅ Each test creates its own calculator instance
	TaxCalculator calculator = new TaxCalculator();
	
	// ✅ Loading read-only resources is parallel-safe
	List<TaxTestCase> testCases = loadTestCasesFromResource("/tax-test-cases.json");
	
	for (TaxTestCase testCase : testCases)
	{
		double actualTax = calculator.calculateTax(testCase.getIncome(), testCase.getProvince());
		double expectedTax = testCase.getExpectedTax();
		
		// ✅ Stateless utility methods are parallel-safe
		TestUtils.assertWithinTolerance(expectedTax, actualTax, 0.01);
	}
}

// ✅ Example of parallel-safe parameterized-style testing
@Test
public void calculateTax_withVariousIncomes_appliesCorrectBrackets()
{
	// ✅ Create test data as local variables (no shared state)
	TaxCalculator calculator = new TaxCalculator();
	
	Map<Double, Double> incomeToExpectedTax = Map.of(
		30_000.0, 4_500.0,
		75_000.0, 15_000.0,
		150_000.0, 35_000.0
	);
	
	// ✅ Each iteration uses isolated data
	for (Map.Entry<Double, Double> testCase : incomeToExpectedTax.entrySet())
	{
		double income = testCase.getKey();
		double expectedTax = testCase.getValue();
		
		double actualTax = calculator.calculateTax(income, Province.ONTARIO);
		
		TestUtils.assertWithinTolerance(expectedTax, actualTax, 1_000.0);
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