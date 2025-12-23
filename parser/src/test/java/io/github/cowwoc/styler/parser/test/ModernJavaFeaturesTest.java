package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.INTERFACE_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RECORD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing modern Java features (JDK 16+): records, sealed classes, and pattern matching.
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
		String source = """
			record Point(int x, int y) { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 31),
			semanticNode(RECORD_DECLARATION, 0, 30, "Point"));
		requireThat(actual, "actual").isEqualTo(expected);
	}


	/**
	 * Tests record declarations with generic type parameters.
	 * Validates that the parser correctly handles type parameters in record headers,
	 * which is essential for type-safe container records.
	 */
	@Test
	public void testRecordWithGenericTypes()
	{
		String source = """
			record Box<T>(T value) { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 27),
			semanticNode(RECORD_DECLARATION, 0, 26, "Box"),
			semanticNode(QUALIFIED_NAME, 14, 15));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates record declarations with implements clauses.
	 * Records can implement interfaces, and this tests that the parser correctly
	 * handles the implements clause with generic type arguments.
	 */
	@Test
	public void testRecordWithImplements()
	{
		String source = """
			record Point(int x, int y) implements Comparable<Point> { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(RECORD_DECLARATION, 0, 59, "Point"),
			semanticNode(QUALIFIED_NAME, 38, 48),
			semanticNode(QUALIFIED_NAME, 49, 54));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests records with custom method declarations.
	 * Records can contain methods beyond the automatically generated accessors,
	 * and this validates parsing of method bodies within record declarations.
	 */
	@Test
	public void testRecordWithMethods()
	{
		String source = """
			record Point(int x, int y)
			{
				public double distance()
				{
					return Math.sqrt(x * x + y * y);
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 98),
			semanticNode(RECORD_DECLARATION, 0, 97, "Point"),
			semanticNode(METHOD_DECLARATION, 30, 95),
			semanticNode(RETURN_STATEMENT, 60, 92),
			semanticNode(METHOD_INVOCATION, 67, 91),
			semanticNode(FIELD_ACCESS, 67, 76),
			semanticNode(IDENTIFIER, 67, 71),
			semanticNode(IDENTIFIER, 77, 78),
			semanticNode(IDENTIFIER, 81, 82),
			semanticNode(IDENTIFIER, 85, 86),
			semanticNode(IDENTIFIER, 89, 90),
			semanticNode(BLOCK, 56, 95),
			semanticNode(BINARY_EXPRESSION, 77, 82),
			semanticNode(BINARY_EXPRESSION, 85, 90),
			semanticNode(BINARY_EXPRESSION, 77, 90));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates sealed class syntax introduced in Java 17.
	 * Tests the sealed modifier with permits clause that restricts which classes
	 * can extend this class, enabling exhaustive pattern matching.
	 */
	@Test
	public void testSealedClass()
	{
		String source = """
			sealed class Shape permits Circle, Rectangle { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 49),
			semanticNode(CLASS_DECLARATION, 7, 48, "Shape"),
			semanticNode(QUALIFIED_NAME, 27, 33),
			semanticNode(QUALIFIED_NAME, 35, 44));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests sealed interfaces with permits clauses.
	 * Sealed interfaces work similarly to sealed classes, restricting which types
	 * can implement the interface for exhaustiveness checking.
	 */
	@Test
	public void testSealedInterface()
	{
		String source = """
			sealed interface Shape permits Circle, Rectangle { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 53),
			semanticNode(INTERFACE_DECLARATION, 7, 52, "Shape"),
			semanticNode(QUALIFIED_NAME, 31, 37),
			semanticNode(QUALIFIED_NAME, 39, 48));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates non-sealed modifier for classes in sealed hierarchies.
	 * A non-sealed class is a permitted subclass that reopens the hierarchy,
	 * allowing unrestricted subclassing from that point onward.
	 */
	@Test
	public void testNonSealedClass()
	{
		String source = """
			non-sealed class Square extends Shape { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 42),
			semanticNode(CLASS_DECLARATION, 11, 41, "Square"),
			semanticNode(QUALIFIED_NAME, 32, 37));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests var keyword for local variable type inference (Java 10+).
	 * Validates that the parser handles var declarations where the type
	 * is inferred from the initializer expression.
	 */
	@Test
	public void testVarKeyword()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					var x = 10;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 64),
			semanticNode(CLASS_DECLARATION, 7, 63, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 61),
			semanticNode(BLOCK, 43, 61),
			semanticNode(INTEGER_LITERAL, 55, 57));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests sealed classes with multiple permitted subclasses.
	 * Validates that the parser correctly handles comma-separated lists in permits clauses,
	 * which is common in sealed hierarchies with several variants.
	 */
	@Test
	public void testMultipleSealedPermits()
	{
		String source = """
			sealed class Shape permits Circle, Rectangle, Triangle, Square { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 67),
			semanticNode(CLASS_DECLARATION, 7, 66, "Shape"),
			semanticNode(QUALIFIED_NAME, 27, 33),
			semanticNode(QUALIFIED_NAME, 35, 44),
			semanticNode(QUALIFIED_NAME, 46, 54),
			semanticNode(QUALIFIED_NAME, 56, 62));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests records with multiple components.
	 * Validates parsing of record headers with several parameters, ensuring
	 * proper handling of comma-separated component lists.
	 */
	@Test
	public void testRecordWithMultipleComponents()
	{
		String source = """
			record Person(String name, int age, String email, String phone) { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 68),
			semanticNode(RECORD_DECLARATION, 0, 67, "Person"),
			semanticNode(QUALIFIED_NAME, 14, 20),
			semanticNode(QUALIFIED_NAME, 36, 42),
			semanticNode(QUALIFIED_NAME, 50, 56));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates nested record declarations inside classes.
	 * Records can be declared as nested types, and this tests that the parser
	 * correctly handles record declarations within class bodies.
	 */
	@Test
	public void testNestedRecord()
	{
		String source = """
			public class Container
			{
				record Inner(int value) { }
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(CLASS_DECLARATION, 7, 55, "Container"),
			semanticNode(RECORD_DECLARATION, 26, 53, "Inner"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests records with public access modifier.
	 * Validates that the parser correctly handles access modifiers on record declarations,
	 * which is necessary for public API records.
	 */
	@Test
	public void testPublicRecord()
	{
		String source = """
			public record Point(int x, int y) { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 38),
			semanticNode(RECORD_DECLARATION, 7, 37, "Point"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests sealed classes that both extend a parent and have permits clauses.
	 * Validates parsing of sealed class declarations with both inheritance and
	 * subclass restrictions, a common pattern in sealed hierarchies.
	 */
	@Test
	public void testSealedClassWithExtends()
	{
		String source = """
			sealed class Circle extends Shape permits FilledCircle, HollowCircle { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 73),
			semanticNode(CLASS_DECLARATION, 7, 72, "Circle"),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(QUALIFIED_NAME, 42, 54),
			semanticNode(QUALIFIED_NAME, 56, 68));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests records with no components (empty parameter list).
	 * Validates that the parser handles the edge case of marker records
	 * that carry no data but serve as type tags.
	 */
	@Test
	public void testRecordWithEmptyBody()
	{
		String source = """
			record Empty() { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 19),
			semanticNode(RECORD_DECLARATION, 0, 18, "Empty"));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
