package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(13, 18, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(20, 25, new ParameterAttribute("y", false, false, false));
			expected.allocateRecordDeclaration(0, 30, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 31);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 14, 15);
			expected.allocateParameterDeclaration(14, 21, new ParameterAttribute("value", false, false, false));
			expected.allocateRecordDeclaration(0, 26, new TypeDeclarationAttribute("Box"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 27);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(13, 18, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(20, 25, new ParameterAttribute("y", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 54);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 38, 55);
			expected.allocateRecordDeclaration(0, 59, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(13, 18, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(20, 25, new ParameterAttribute("y", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 67, 71);
			expected.allocateNode(NodeType.FIELD_ACCESS, 67, 76);
			expected.allocateNode(NodeType.IDENTIFIER, 77, 78);
			expected.allocateNode(NodeType.IDENTIFIER, 81, 82);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 77, 82);
			expected.allocateNode(NodeType.IDENTIFIER, 85, 86);
			expected.allocateNode(NodeType.IDENTIFIER, 89, 90);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 85, 90);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 77, 90);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 67, 91);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 60, 92);
			expected.allocateNode(NodeType.BLOCK, 56, 95);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 30, 95);
			expected.allocateRecordDeclaration(0, 97, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 98);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 44);
			expected.allocateClassDeclaration(7, 48, new TypeDeclarationAttribute("Shape"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 49);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 48);
			expected.allocateInterfaceDeclaration(7, 52, new TypeDeclarationAttribute("Shape"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 53);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 37);
			expected.allocateClassDeclaration(11, 41, new TypeDeclarationAttribute("Square"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 57);
			expected.allocateNode(NodeType.BLOCK, 43, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 61);
			expected.allocateClassDeclaration(7, 63, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateClassDeclaration(7, 66, new TypeDeclarationAttribute("Shape"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 14, 20);
			expected.allocateParameterDeclaration(14, 25, new ParameterAttribute("name", false, false, false));
			expected.allocateParameterDeclaration(27, 34, new ParameterAttribute("age", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 42);
			expected.allocateParameterDeclaration(36, 48, new ParameterAttribute("email", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 56);
			expected.allocateParameterDeclaration(50, 62, new ParameterAttribute("phone", false, false, false));
			expected.allocateRecordDeclaration(0, 67, new TypeDeclarationAttribute("Person"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(39, 48, new ParameterAttribute("value", false, false, false));
			expected.allocateRecordDeclaration(26, 53, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(7, 55, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(20, 25, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(27, 32, new ParameterAttribute("y", false, false, false));
			expected.allocateRecordDeclaration(7, 37, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 38);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 68);
			expected.allocateClassDeclaration(7, 72, new TypeDeclarationAttribute("Circle"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateRecordDeclaration(0, 18, new TypeDeclarationAttribute("Empty"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 19);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
