package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing cast expressions.
 */
public final class CastExpressionParserTest
{
	// ========== Core Casts (5 tests) ==========

	/**
	 * Validates that a primitive int cast parses correctly.
	 */
	@Test
	public void shouldParsePrimitiveIntCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = (int) longValue;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 42, 51);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 36, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a primitive double cast parses correctly.
	 */
	@Test
	public void shouldParsePrimitiveDoubleCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					double d = (double) intValue;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 56);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 56);
			expected.allocateNode(NodeType.BLOCK, 24, 60);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 60);
			expected.allocateClassDeclaration(0, 62, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 63);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a reference type cast parses correctly.
	 */
	@Test
	public void shouldParseReferenceTypeCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					String s = (String) obj;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 48, 51);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a qualified type cast parses correctly.
	 */
	@Test
	public void shouldParseQualifiedTypeCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					String s = (java.lang.String) obj;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 58, 61);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 61);
			expected.allocateNode(NodeType.BLOCK, 24, 65);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 65);
			expected.allocateClassDeclaration(0, 67, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a generic type cast parses correctly.
	 */
	@Test
	public void shouldParseGenericTypeCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					List<String> list = (List<String>) obj;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 60);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 63, 66);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 48, 66);
			expected.allocateNode(NodeType.BLOCK, 24, 70);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 70);
			expected.allocateClassDeclaration(0, 72, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Intersection Casts (3 tests) ==========

	/**
	 * Validates that an intersection cast with two types parses correctly.
	 */
	@Test
	public void shouldParseIntersectionCastTwoTypes()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object o = (Serializable & Comparable<?>) value;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 66, 67);
			expected.allocateNode(NodeType.IDENTIFIER, 70, 75);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 75);
			expected.allocateNode(NodeType.BLOCK, 24, 79);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 79);
			expected.allocateClassDeclaration(0, 81, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 82);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an intersection cast with three types parses correctly.
	 */
	@Test
	public void shouldParseIntersectionCastThreeTypes()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object o = (Serializable & Comparable<?> & Cloneable) value;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 66, 67);
			expected.allocateNode(NodeType.IDENTIFIER, 82, 87);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 87);
			expected.allocateNode(NodeType.BLOCK, 24, 91);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 91);
			expected.allocateClassDeclaration(0, 93, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 94);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an intersection cast with qualified types parses correctly.
	 */
	@Test
	public void shouldParseIntersectionCastQualifiedTypes()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object o = (java.io.Serializable & java.lang.Comparable<?>) value;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 84, 85);
			expected.allocateNode(NodeType.IDENTIFIER, 88, 93);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 93);
			expected.allocateNode(NodeType.BLOCK, 24, 97);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 97);
			expected.allocateClassDeclaration(0, 99, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 100);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Array Casts (3 tests) ==========

	/**
	 * Validates that a single-dimension array cast parses correctly.
	 */
	@Test
	public void shouldParseSingleDimensionArrayCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					String[] arr = (String[]) array;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 54, 59);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 43, 59);
			expected.allocateNode(NodeType.BLOCK, 24, 63);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 63);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a multi-dimension array cast parses correctly.
	 */
	@Test
	public void shouldParseMultiDimensionArrayCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[][] matrix = (int[][]) array;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 55, 60);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 45, 60);
			expected.allocateNode(NodeType.BLOCK, 24, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 64);
			expected.allocateClassDeclaration(0, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a primitive array cast parses correctly.
	 */
	@Test
	public void shouldParsePrimitiveArrayCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = (int[]) obj;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 51);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 40, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Disambiguation (5 tests) ==========

	/**
	 * Validates that parenthesized expression with binary plus is NOT a cast.
	 * The expression {@code (a) + b} should parse as binary expression (not as cast).
	 * <p>
	 * Note: The parser does not produce explicit PARENTHESIZED_EXPRESSION nodes; it returns the inner
	 * expression directly.
	 */
	@Test
	public void shouldDisambiguateParenthesizedExpressionFromCast()
	{
		String source = """
			class Test
			{
				void m()
				{
					int result = (a) + b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 42, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 47, 48);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 42, 48);
			expected.allocateNode(NodeType.BLOCK, 24, 52);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 52);
			expected.allocateClassDeclaration(0, 54, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a primitive cast of unary plus parses correctly.
	 * The expression {@code (int) +b} is valid because primitive casts can be followed by any unary
	 * expression.
	 */
	@Test
	public void shouldParsePrimitiveCastOfUnaryPlus()
	{
		String source = """
			class Test
			{
				void m()
				{
					int i = (int) +b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 43, 44);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 42, 44);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 36, 44);
			expected.allocateNode(NodeType.BLOCK, 24, 48);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 48);
			expected.allocateClassDeclaration(0, 50, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 51);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a primitive cast of unary minus parses correctly.
	 * The expression {@code (int) -value} is valid because primitive casts can be followed by any unary
	 * expression.
	 */
	@Test
	public void shouldParsePrimitiveCastOfUnaryMinus()
	{
		String source = """
			class Test
			{
				void m()
				{
					int d = (int) -value;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 43, 48);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 42, 48);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 36, 48);
			expected.allocateNode(NodeType.BLOCK, 24, 52);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 52);
			expected.allocateClassDeclaration(0, 54, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast of method call parses correctly.
	 */
	@Test
	public void shouldParseCastOfMethodCall()
	{
		String source = """
			class Test
			{
				void m()
				{
					String s = (String) getValue();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 48, 56);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 48, 58);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 58);
			expected.allocateNode(NodeType.BLOCK, 24, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast of field access parses correctly.
	 */
	@Test
	public void shouldParseCastOfFieldAccess()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = (int) obj.field;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 42, 45);
			expected.allocateNode(NodeType.FIELD_ACCESS, 42, 51);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 36, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Chained/Nested (4 tests) ==========

	/**
	 * Validates that chained casts parse correctly.
	 */
	@Test
	public void shouldParseChainedCasts()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object o = (Object) (String) value;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 57, 62);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 48, 62);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 62);
			expected.allocateNode(NodeType.BLOCK, 24, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 66);
			expected.allocateClassDeclaration(0, 68, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 69);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast with method call on result parses correctly.
	 */
	@Test
	public void shouldParseCastWithMethodCallOnResult()
	{
		String source = """
			class Test
			{
				void m()
				{
					int len = ((String) obj).length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 51);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 51);
			expected.allocateNode(NodeType.FIELD_ACCESS, 39, 59);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 39, 61);
			expected.allocateNode(NodeType.BLOCK, 24, 65);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 65);
			expected.allocateClassDeclaration(0, 67, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast as method argument parses correctly.
	 */
	@Test
	public void shouldParseCastAsMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					process((String) obj);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 35);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 35);
			expected.allocateNode(NodeType.IDENTIFIER, 45, 48);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 36, 48);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 49);
			expected.allocateNode(NodeType.BLOCK, 24, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast in return statement parses correctly.
	 */
	@Test
	public void shouldParseCastInReturnStatement()
	{
		String source = """
			class Test
			{
				String m()
				{
					return (String) obj;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 46, 49);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 37, 49);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 30, 50);
			expected.allocateNode(NodeType.BLOCK, 26, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Expression Contexts (4 tests) ==========

	/**
	 * Validates that casts in ternary conditional expression parse correctly.
	 */
	@Test
	public void shouldParseCastsInTernaryExpression()
	{
		String source = """
			class Test
			{
				void m()
				{
					String s = flag ? (String) a : (String) b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 56);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 46, 56);
			expected.allocateNode(NodeType.IDENTIFIER, 68, 69);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 59, 69);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 69);
			expected.allocateNode(NodeType.BLOCK, 24, 73);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 73);
			expected.allocateClassDeclaration(0, 75, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 76);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that casts in binary expression parse correctly.
	 */
	@Test
	public void shouldParseCastsInBinaryExpression()
	{
		String source = """
			class Test
			{
				void m()
				{
					int result = (int) a + (int) b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 47, 48);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 41, 48);
			expected.allocateNode(NodeType.IDENTIFIER, 57, 58);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 51, 58);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 41, 58);
			expected.allocateNode(NodeType.BLOCK, 24, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast with array access parses correctly.
	 */
	@Test
	public void shouldParseCastWithArrayAccess()
	{
		String source = """
			class Test
			{
				void m()
				{
					String s = ((String[]) array)[0];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 51, 56);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 40, 56);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 58, 59);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 40, 60);
			expected.allocateNode(NodeType.BLOCK, 24, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 64);
			expected.allocateClassDeclaration(0, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast of integer literal parses correctly.
	 */
	@Test
	public void shouldParseCastOfLiteral()
	{
		String source = """
			class Test
			{
				void m()
				{
					double d = (double) 42;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 48, 50);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 50);
			expected.allocateNode(NodeType.BLOCK, 24, 54);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 54);
			expected.allocateClassDeclaration(0, 56, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Error Cases (3 tests) ==========

	/**
	 * Validates that a parenthesized type name without a cast operand is parsed as a variable declaration
	 * with a parenthesized expression initializer.
	 * <p>
	 * The expression {@code String s = (String);} is syntactically valid: the parser interprets
	 * {@code (String)} as a parenthesized identifier expression (treating {@code String} as a variable name,
	 * not a type). The semantic error (using a type name where a value is expected) would only be caught
	 * during type checking, not parsing.
	 */
	@Test
	public void shouldParseParenthesizedTypeAsExpression()
	{
		String source = """
			class Test
			{
				void m()
				{
					String s = (String);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 40, 46);
			expected.allocateNode(NodeType.BLOCK, 24, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 51);
			expected.allocateClassDeclaration(0, 53, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that malformed intersection cast fails to parse.
	 */
	@Test
	public void shouldRejectMalformedIntersectionCast()
	{
		assertParseFails("""
			class Test
			{
				void m()
				{
					Object o = (A &) value;
				}
			}
			""");
	}

	/**
	 * Validates that empty intersection cast fails to parse.
	 */
	@Test
	public void shouldRejectEmptyIntersectionCast()
	{
		assertParseFails("""
			class Test
			{
				void m()
				{
					Object o = (&Comparable) value;
				}
			}
			""");
	}

	// ========== Cast Lambda Expression Tests (5 tests) ==========

	/**
	 * Validates that a cast of a no-parameter lambda parses correctly.
	 * Example: {@code (Runnable) () -> doSomething()}
	 */
	@Test
	public void shouldParseCastOfNoParamLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Runnable r = (Runnable) () -> doSomething();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 58, 69);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 58, 71);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 52, 71);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 41, 71);
			expected.allocateNode(NodeType.BLOCK, 24, 75);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 75);
			expected.allocateClassDeclaration(0, 77, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 78);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast of a single-parameter lambda parses correctly.
	 * Example: {@code (Function<String, Integer>) s -> s.length()}
	 */
	@Test
	public void shouldParseCastOfSingleParamLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object f = (Function<String, Integer>) s -> s.length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 64);
			expected.allocateNode(NodeType.IDENTIFIER, 72, 73);
			expected.allocateNode(NodeType.FIELD_ACCESS, 72, 80);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 72, 82);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 67, 82);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 82);
			expected.allocateNode(NodeType.BLOCK, 24, 86);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 86);
			expected.allocateClassDeclaration(0, 88, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 89);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast of a lambda with block body parses correctly.
	 * Example: {@code (Consumer<String>) s -> { process(s); }}
	 */
	@Test
	public void shouldParseCastOfBlockLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object c = (Consumer<String>) s -> { process(s); };
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 74);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 65, 75);
			expected.allocateNode(NodeType.BLOCK, 63, 78);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 58, 78);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 78);
			expected.allocateNode(NodeType.BLOCK, 24, 82);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 82);
			expected.allocateClassDeclaration(0, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that nested cast with lambda parses correctly.
	 * Example: {@code (HibernateCallback<List<T>>) session -> { ... }}
	 */
	@Test
	public void shouldParseNestedGenericCastOfLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object cb = (Callback<List<String>>) list -> list.size();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 61);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 50, 63);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 77);
			expected.allocateNode(NodeType.FIELD_ACCESS, 73, 82);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 73, 84);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 65, 84);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 40, 84);
			expected.allocateNode(NodeType.BLOCK, 24, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that cast of lambda inside method argument parses correctly.
	 * Example: {@code nonNull((Callback<T>) session -> ...)}
	 */
	@Test
	public void shouldParseCastOfLambdaAsMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object result = nonNull((Callback<String>) s -> s.trim());
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 44, 51);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.IDENTIFIER, 76, 77);
			expected.allocateNode(NodeType.FIELD_ACCESS, 76, 82);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 76, 84);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 71, 84);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 52, 84);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 44, 85);
			expected.allocateNode(NodeType.BLOCK, 24, 89);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 89);
			expected.allocateClassDeclaration(0, 91, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 92);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
