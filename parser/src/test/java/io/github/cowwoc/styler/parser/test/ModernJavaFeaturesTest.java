package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.INTERFACE_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.RECORD_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			compilationUnit( 0, 31),
			typeDeclaration(RECORD_DECLARATION, 0, 30, "Point"),
			parameterNode( 13, 18, "x"),
			parameterNode( 20, 25, "y"));
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
			compilationUnit( 0, 27),
			typeDeclaration(RECORD_DECLARATION, 0, 26, "Box"),
			qualifiedName( 14, 15),
			parameterNode( 14, 21, "value"));
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
			compilationUnit( 0, 60),
			typeDeclaration(RECORD_DECLARATION, 0, 59, "Point"),
			parameterizedType( 38, 55),
			qualifiedName( 38, 48),
			qualifiedName( 49, 54),
			parameterNode( 13, 18, "x"),
			parameterNode( 20, 25, "y"));
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
			compilationUnit( 0, 98),
			typeDeclaration(RECORD_DECLARATION, 0, 97, "Point"),
			methodDeclaration( 30, 95),
			returnStatement( 60, 92),
			methodInvocation( 67, 91),
			fieldAccess( 67, 76),
			identifier( 67, 71),
			identifier( 77, 78),
			identifier( 81, 82),
			identifier( 85, 86),
			identifier( 89, 90),
			block( 56, 95),
			binaryExpression( 77, 82),
			binaryExpression( 85, 90),
			binaryExpression( 77, 90),
			parameterNode( 13, 18, "x"),
			parameterNode( 20, 25, "y"));
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
			compilationUnit( 0, 49),
			typeDeclaration(CLASS_DECLARATION, 7, 48, "Shape"),
			qualifiedName( 27, 33),
			qualifiedName( 35, 44));
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
			compilationUnit( 0, 53),
			typeDeclaration(INTERFACE_DECLARATION, 7, 52, "Shape"),
			qualifiedName( 31, 37),
			qualifiedName( 39, 48));
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
			compilationUnit( 0, 42),
			typeDeclaration(CLASS_DECLARATION, 11, 41, "Square"),
			qualifiedName( 32, 37));
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
			compilationUnit( 0, 64),
			typeDeclaration(CLASS_DECLARATION, 7, 63, "Test"),
			methodDeclaration( 21, 61),
			block( 43, 61),
			integerLiteral( 55, 57));
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
			compilationUnit( 0, 67),
			typeDeclaration(CLASS_DECLARATION, 7, 66, "Shape"),
			qualifiedName( 27, 33),
			qualifiedName( 35, 44),
			qualifiedName( 46, 54),
			qualifiedName( 56, 62));
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
			compilationUnit( 0, 68),
			typeDeclaration(RECORD_DECLARATION, 0, 67, "Person"),
			qualifiedName( 14, 20),
			qualifiedName( 36, 42),
			qualifiedName( 50, 56),
			parameterNode( 14, 25, "name"),
			parameterNode( 27, 34, "age"),
			parameterNode( 36, 48, "email"),
			parameterNode( 50, 62, "phone"));
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
			compilationUnit( 0, 56),
			typeDeclaration(CLASS_DECLARATION, 7, 55, "Container"),
			typeDeclaration(RECORD_DECLARATION, 26, 53, "Inner"),
			parameterNode( 39, 48, "value"));
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
			compilationUnit( 0, 38),
			typeDeclaration(RECORD_DECLARATION, 7, 37, "Point"),
			parameterNode( 20, 25, "x"),
			parameterNode( 27, 32, "y"));
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
			compilationUnit( 0, 73),
			typeDeclaration(CLASS_DECLARATION, 7, 72, "Circle"),
			qualifiedName( 28, 33),
			qualifiedName( 42, 54),
			qualifiedName( 56, 68));
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
			compilationUnit( 0, 19),
			typeDeclaration(RECORD_DECLARATION, 0, 18, "Empty"));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
