package io.github.cowwoc.styler.parser.test;

import org.testng.annotations.Test;

import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseSucceeds;

/**
 * Thread-safe tests for modern Java features (JDK 16-25).
 * Tests records, sealed classes, pattern matching, and other modern constructs.
 */
public class ModernJavaFeaturesTest
{
	/**
	 * Validates parsing of basic record syntax introduced in Java 16.
	 * Tests the simplest form: record name with component list and empty body.
	 */
	@Test
	public void testSimpleRecord()
	{
		assertParseSucceeds("record Point(int x, int y) { }");
	}

	/**
	 * Tests record declarations with generic type parameters.
	 * Validates that the parser correctly handles type parameters in record headers,
	 * which is essential for type-safe container records.
	 */
	@Test
	public void testRecordWithGenericTypes()
	{
		assertParseSucceeds("record Box<T>(T value) { }");
	}

	/**
	 * Validates record declarations with implements clauses.
	 * Records can implement interfaces, and this tests that the parser correctly
	 * handles the implements clause with generic type arguments.
	 */
	@Test
	public void testRecordWithImplements()
	{
		assertParseSucceeds("record Point(int x, int y) implements Comparable<Point> { }");
	}

	/**
	 * Tests records with custom method declarations.
	 * Records can contain methods beyond the automatically generated accessors,
	 * and this validates parsing of method bodies within record declarations.
	 */
	@Test
	public void testRecordWithMethods()
	{
		assertParseSucceeds("""
			record Point(int x, int y) {
				public double distance() {
					return Math.sqrt(x * x + y * y);
				}
			}
			""");
	}

	/**
	 * Validates sealed class syntax introduced in Java 17.
	 * Tests the sealed modifier with permits clause that restricts which classes
	 * can extend this class, enabling exhaustive pattern matching.
	 */
	@Test
	public void testSealedClass()
	{
		assertParseSucceeds("sealed class Shape permits Circle, Rectangle { }");
	}

	/**
	 * Tests sealed interfaces with permits clauses.
	 * Sealed interfaces work similarly to sealed classes, restricting which types
	 * can implement the interface for exhaustiveness checking.
	 */
	@Test
	public void testSealedInterface()
	{
		assertParseSucceeds("sealed interface Shape permits Circle, Rectangle { }");
	}

	/**
	 * Validates non-sealed modifier for classes in sealed hierarchies.
	 * A non-sealed class is a permitted subclass that reopens the hierarchy,
	 * allowing unrestricted subclassing from that point onward.
	 */
	@Test
	public void testNonSealedClass()
	{
		assertParseSucceeds("non-sealed class Square extends Shape { }");
	}

	/**
	 * Tests var keyword for local variable type inference (Java 10+).
	 * Validates that the parser handles var declarations where the type
	 * is inferred from the initializer expression.
	 */
	@Test
	public void testVarKeyword()
	{
		assertParseSucceeds("""
			public class Test {
				public void method() {
					var x = 10;
				}
			}
			""");
	}

	/**
	 * Tests sealed classes with multiple permitted subclasses.
	 * Validates that the parser correctly handles comma-separated lists in permits clauses,
	 * which is common in sealed hierarchies with several variants.
	 */
	@Test
	public void testMultipleSealedPermits()
	{
		assertParseSucceeds("sealed class Shape permits Circle, Rectangle, Triangle, Square { }");
	}

	/**
	 * Tests records with multiple components.
	 * Validates parsing of record headers with several parameters, ensuring
	 * proper handling of comma-separated component lists.
	 */
	@Test
	public void testRecordWithMultipleComponents()
	{
		assertParseSucceeds("record Person(String name, int age, String email, String phone) { }");
	}

	/**
	 * Validates nested record declarations inside classes.
	 * Records can be declared as nested types, and this tests that the parser
	 * correctly handles record declarations within class bodies.
	 */
	@Test
	public void testNestedRecord()
	{
		assertParseSucceeds("""
			public class Container {
				record Inner(int value) { }
			}
			""");
	}

	/**
	 * Tests records with public access modifier.
	 * Validates that the parser correctly handles access modifiers on record declarations,
	 * which is necessary for public API records.
	 */
	@Test
	public void testPublicRecord()
	{
		assertParseSucceeds("public record Point(int x, int y) { }");
	}

	/**
	 * Tests sealed classes that both extend a parent and have permits clauses.
	 * Validates parsing of sealed class declarations with both inheritance and
	 * subclass restrictions, a common pattern in sealed hierarchies.
	 */
	@Test
	public void testSealedClassWithExtends()
	{
		assertParseSucceeds("sealed class Circle extends Shape permits FilledCircle, HollowCircle { }");
	}

	/**
	 * Tests records with no components (empty parameter list).
	 * Validates that the parser handles the edge case of marker records
	 * that carry no data but serve as type tags.
	 */
	@Test
	public void testRecordWithEmptyBody()
	{
		assertParseSucceeds("record Empty() { }");
	}
}
