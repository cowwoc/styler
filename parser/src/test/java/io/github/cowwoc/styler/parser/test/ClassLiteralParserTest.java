package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing class literal expressions ({@code Type.class}).
 */
public final class ClassLiteralParserTest
{
	/**
	 * Validates that a simple class literal on a reference type parses correctly.
	 * Tests the most common form: {@code String.class}.
	 */
	@Test
	public void testSimpleClassLiteral()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = String.class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 41, 47);
			expected.allocateNode(NodeType.CLASS_LITERAL, 41, 53);
			expected.allocateNode(NodeType.BLOCK, 24, 57);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals on fully qualified types parse correctly.
	 * Tests qualified names like {@code java.lang.String.class}.
	 */
	@Test
	public void testClassLiteralOnQualifiedType()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = java.lang.String.class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 41, 45);
			expected.allocateNode(NodeType.FIELD_ACCESS, 41, 50);
			expected.allocateNode(NodeType.FIELD_ACCESS, 41, 57);
			expected.allocateNode(NodeType.CLASS_LITERAL, 41, 63);
			expected.allocateNode(NodeType.BLOCK, 24, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals on nested classes parse correctly.
	 * Tests accessing class literal of inner class: {@code Outer.Inner.class}.
	 */
	@Test
	public void testClassLiteralOnNestedClass()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = Outer.Inner.class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 41, 46);
			expected.allocateNode(NodeType.FIELD_ACCESS, 41, 52);
			expected.allocateNode(NodeType.CLASS_LITERAL, 41, 58);
			expected.allocateNode(NodeType.BLOCK, 24, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals on primitive types parse correctly.
	 * Tests {@code int.class} which is a valid Java construct.
	 */
	@Test
	public void testIntClassLiteral()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = int.class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.CLASS_LITERAL, 41, 50);
			expected.allocateNode(NodeType.BLOCK, 24, 54);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 54);
			expected.allocateClassDeclaration(0, 56, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals on void type parse correctly.
	 * Tests {@code void.class} which represents the Void.TYPE class object.
	 */
	@Test
	public void testVoidClassLiteral()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = void.class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.CLASS_LITERAL, 41, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals on single-dimension arrays parse correctly.
	 * Tests {@code String[].class} for array type class objects.
	 */
	@Test
	public void testSingleDimensionArrayClassLiteral()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = String[].class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 41, 47);
			expected.allocateNode(NodeType.CLASS_LITERAL, 41, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals on primitive arrays parse correctly.
	 * Tests {@code int[].class} for primitive array type class objects.
	 */
	@Test
	public void testPrimitiveArrayClassLiteral()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = int[].class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.CLASS_LITERAL, 41, 52);
			expected.allocateNode(NodeType.BLOCK, 24, 56);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 56);
			expected.allocateClassDeclaration(0, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals on multi-dimensional arrays parse correctly.
	 * Tests {@code int[][].class} for 2D array class objects.
	 */
	@Test
	public void testMultiDimensionalArrayClassLiteral()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = int[][].class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.CLASS_LITERAL, 41, 54);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals work correctly as method arguments.
	 * Tests passing class literals directly to methods: {@code accept(String.class)}.
	 */
	@Test
	public void testClassLiteralAsMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					accept(String.class);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 35, 41);
			expected.allocateNode(NodeType.CLASS_LITERAL, 35, 47);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 48);
			expected.allocateNode(NodeType.BLOCK, 24, 52);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 52);
			expected.allocateClassDeclaration(0, 54, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals work in return statements.
	 * Tests returning class literals from methods.
	 */
	@Test
	public void testClassLiteralInReturnStatement()
	{
		String source = """
			class Test
			{
				Class<?> m()
				{
					return Integer.class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 20, 21);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 46);
			expected.allocateNode(NodeType.CLASS_LITERAL, 39, 52);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 32, 53);
			expected.allocateNode(NodeType.BLOCK, 28, 56);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 56);
			expected.allocateClassDeclaration(0, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class literals work in ternary conditional expressions.
	 * Tests using class literals as alternatives: {@code flag ? String.class : Object.class}.
	 */
	@Test
	public void testClassLiteralInTernaryExpression()
	{
		String source = """
			class Test
			{
				void m()
				{
					Class<?> c = flag ? String.class : Object.class;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 33);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 41, 45);
			expected.allocateNode(NodeType.IDENTIFIER, 48, 54);
			expected.allocateNode(NodeType.CLASS_LITERAL, 48, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 63, 69);
			expected.allocateNode(NodeType.CLASS_LITERAL, 63, 75);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 41, 75);
			expected.allocateNode(NodeType.BLOCK, 24, 79);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 79);
			expected.allocateClassDeclaration(0, 81, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 82);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that incomplete class literal syntax fails to parse.
	 * Tests that {@code String.} without {@code class} keyword is rejected.
	 */
	@Test
	public void testMalformedClassLiteralMissingClass()
	{
		assertParseFails("""
			class Test
			{
				void m()
				{
					Class<?> c = String.;
				}
			}
			""");
	}

	/**
	 * Validates that class keyword in normal class declaration context works.
	 * Ensures class literal support does not break normal class declarations.
	 */
	@Test
	public void testClassKeywordWithoutDot()
	{
		String source = """
			class TestClass
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 19, new TypeDeclarationAttribute("TestClass"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 20);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
