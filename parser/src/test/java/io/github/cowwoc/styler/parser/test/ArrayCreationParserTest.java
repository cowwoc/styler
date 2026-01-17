package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing array creation expressions ({@code new Type[size]} and {@code new Type[]{...}} syntax).
 */
public final class ArrayCreationParserTest
{
	/**
	 * Parses a single-dimension array creation with a size expression.
	 * Validates that {@code new int[5]} produces correct AST with ARRAY_CREATION node.
	 */
	@Test
	public void shouldParseSingleDimensionArrayCreation()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = new int[5];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 48, 49);
			expected.allocateNode(NodeType.ARRAY_CREATION, 40, 50);
			expected.allocateNode(NodeType.BLOCK, 24, 54);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 54);
			expected.allocateClassDeclaration(0, 56, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses an array creation with an initializer list.
	 * Validates that {@code new int[]{1, 2, 3}} produces correct AST with initializer values
	 * as children of ARRAY_CREATION.
	 */
	@Test
	public void shouldParseArrayInitializer()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = new int[]{1, 2, 3};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 51);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 53, 54);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 56, 57);
			expected.allocateNode(NodeType.ARRAY_CREATION, 40, 58);
			expected.allocateNode(NodeType.BLOCK, 24, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses a reference type array creation with a size expression.
	 * Validates that {@code new String[10]} parses correctly.
	 */
	@Test
	public void shouldParseReferenceTypeArray()
	{
		String source = """
			class Test
			{
				void m()
				{
					String[] arr = new String[10];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 53);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 54, 56);
			expected.allocateNode(NodeType.ARRAY_CREATION, 43, 57);
			expected.allocateNode(NodeType.BLOCK, 24, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 61);
			expected.allocateClassDeclaration(0, 63, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses a reference type array creation with an initializer.
	 * Validates that {@code new String[]{"a", "b"}} parses correctly.
	 */
	@Test
	public void shouldParseReferenceTypeInitializer()
	{
		String source = """
			class Test
			{
				void m()
				{
					String[] arr = new String[]{"a", "b"};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 53);
			expected.allocateNode(NodeType.STRING_LITERAL, 56, 59);
			expected.allocateNode(NodeType.STRING_LITERAL, 61, 64);
			expected.allocateNode(NodeType.ARRAY_CREATION, 43, 65);
			expected.allocateNode(NodeType.BLOCK, 24, 69);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 69);
			expected.allocateClassDeclaration(0, 71, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 72);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses a multi-dimensional array creation with multiple size expressions.
	 * Validates that {@code new int[2][3]} parses correctly.
	 */
	@Test
	public void shouldParseMultiDimensionalArray()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[][] arr = new int[2][3];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 51);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 53, 54);
			expected.allocateNode(NodeType.ARRAY_CREATION, 42, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses a partially specified multi-dimensional array.
	 * Validates that {@code new int[2][]} parses correctly with first dimension
	 * having a size expression and second dimension empty.
	 */
	@Test
	public void shouldParsePartialDimensions()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[][] arr = new int[2][];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 51);
			expected.allocateNode(NodeType.ARRAY_CREATION, 42, 54);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation followed by immediate array access.
	 * Validates that {@code new Object[]{a, b}[0]} parses correctly.
	 */
	@Test
	public void shouldParseArrayCreationWithAccess()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object x = new Object[]{a, b}[0];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 49);
			expected.allocateNode(NodeType.IDENTIFIER, 52, 53);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 56);
			expected.allocateNode(NodeType.ARRAY_CREATION, 39, 57);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 58, 59);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 39, 60);
			expected.allocateNode(NodeType.BLOCK, 24, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 64);
			expected.allocateClassDeclaration(0, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation with a method call as the dimension expression.
	 * Validates that {@code new int[computeSize()]} parses correctly.
	 */
	@Test
	public void shouldParseArrayWithMethodCallDimension()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = new int[computeSize()];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 59);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 48, 61);
			expected.allocateNode(NodeType.ARRAY_CREATION, 40, 62);
			expected.allocateNode(NodeType.BLOCK, 24, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 66);
			expected.allocateClassDeclaration(0, 68, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 69);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation with a binary expression as the dimension.
	 * Validates that {@code new int[a + b]} parses correctly.
	 */
	@Test
	public void shouldParseArrayWithBinaryExpression()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = new int[a + b];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.IDENTIFIER, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.ARRAY_CREATION, 40, 54);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation with an empty initializer.
	 * Validates that {@code new int[]{}} parses correctly.
	 */
	@Test
	public void shouldParseEmptyInitializer()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = new int[]{};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_CREATION, 40, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation with nested initializers for 2D arrays.
	 * Validates that {@code new int[][]{{1}, {2}}} parses correctly.
	 */
	@Test
	public void shouldParseNestedInitializers()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[][] arr = new int[][]{{1}, {2}};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 56);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 54, 57);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 60, 61);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 59, 62);
			expected.allocateNode(NodeType.ARRAY_CREATION, 42, 63);
			expected.allocateNode(NodeType.BLOCK, 24, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation within an annotation value context.
	 * Tests real-world Spring Framework pattern.
	 */
	@Test
	public void shouldParseArrayCreationInAnnotationValue()
	{
		String source = """
			@SuppressWarnings(value = new String[]{"unchecked"})
			class Test
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 17);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 17);
			expected.allocateNode(NodeType.IDENTIFIER, 18, 23);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.STRING_LITERAL, 39, 50);
			expected.allocateNode(NodeType.ARRAY_CREATION, 26, 51);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 18, 51);
			expected.allocateNode(NodeType.ANNOTATION, 0, 52);
			expected.allocateClassDeclaration(53, 67, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation in a return statement context.
	 * Tests common pattern for factory methods.
	 */
	@Test
	public void shouldParseArrayCreationInReturn()
	{
		String source = """
			class Test
			{
				int[] getNumbers()
				{
					return new int[]{1, 2, 3};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 56);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 58, 59);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 61, 62);
			expected.allocateNode(NodeType.ARRAY_CREATION, 45, 63);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 38, 64);
			expected.allocateNode(NodeType.BLOCK, 34, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation as a method argument.
	 * Tests common TestNG/JUnit pattern: {@code assertEquals(new int[]{1}, result)}.
	 */
	@Test
	public void shouldParseArrayCreationAsMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					assertEquals(new int[]{1}, result);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 40);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 40);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 51, 52);
			expected.allocateNode(NodeType.ARRAY_CREATION, 41, 53);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 61);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 62);
			expected.allocateNode(NodeType.BLOCK, 24, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 66);
			expected.allocateClassDeclaration(0, 68, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 69);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation for a nested class type.
	 * Tests {@code new Outer.Inner[5]} syntax.
	 */
	@Test
	public void shouldParseNestedClassArrayCreation()
	{
		String source = """
			class Test
			{
				void m()
				{
					Outer.Inner[] arr = new Outer.Inner[5];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 39);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 52, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 64, 65);
			expected.allocateNode(NodeType.ARRAY_CREATION, 48, 66);
			expected.allocateNode(NodeType.BLOCK, 24, 70);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 70);
			expected.allocateClassDeclaration(0, 72, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation with generic type (raw array, not parameterized).
	 * Tests {@code new List[10]} syntax.
	 */
	@Test
	public void shouldParseGenericTypeArray()
	{
		String source = """
			class Test
			{
				void m()
				{
					List[] arr = new List[10];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 45, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 52);
			expected.allocateNode(NodeType.ARRAY_CREATION, 41, 53);
			expected.allocateNode(NodeType.BLOCK, 24, 57);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Parses array creation used in a ternary expression.
	 * Tests {@code condition ? new int[]{1} : new int[]{2}} syntax.
	 */
	@Test
	public void shouldParseArrayCreationInTernary()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = flag ? new int[]{1} : new int[]{2};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 40, 44);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 57, 58);
			expected.allocateNode(NodeType.ARRAY_CREATION, 47, 59);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 72, 73);
			expected.allocateNode(NodeType.ARRAY_CREATION, 62, 74);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 40, 74);
			expected.allocateNode(NodeType.BLOCK, 24, 78);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 78);
			expected.allocateClassDeclaration(0, 80, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 81);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies start and end positions for an array creation node.
	 * Tests that {@code new int[5]} spans from 'n' in new to ']' after 5.
	 */
	@Test
	public void shouldTrackArrayCreationPositions()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = new int[5];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 48, 49);
			expected.allocateNode(NodeType.ARRAY_CREATION, 40, 50);
			expected.allocateNode(NodeType.BLOCK, 24, 54);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 54);
			expected.allocateClassDeclaration(0, 56, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies positions for dimension expressions in multi-dimensional arrays.
	 * Tests that each dimension expression has correct position tracking.
	 */
	@Test
	public void shouldTrackDimensionExpressionPositions()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[][] arr = new int[2][3];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 51);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 53, 54);
			expected.allocateNode(NodeType.ARRAY_CREATION, 42, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies positions for initializer elements.
	 * Tests that each element in the initializer has correct position tracking.
	 */
	@Test
	public void shouldTrackInitializerElementPositions()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = new int[]{1, 2, 3};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 51);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 53, 54);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 56, 57);
			expected.allocateNode(NodeType.ARRAY_CREATION, 40, 58);
			expected.allocateNode(NodeType.BLOCK, 24, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that array creation with trailing comma and comment before closing brace parses correctly.
	 * Tests real-world Spring Framework pattern with comment after trailing comma.
	 */
	@Test
	public void shouldParseArrayCreationWithTrailingCommaAndComment()
	{
		String source = """
			class Test
			{
				Class<?>[] arr = new Class<?>[] {
					String.class,
					Integer.class,
					// More types
				};
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 20, 21);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 40);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 41, 42);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 35, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 50, 56);
			expected.allocateNode(NodeType.CLASS_LITERAL, 50, 62);
			expected.allocateNode(NodeType.IDENTIFIER, 66, 73);
			expected.allocateNode(NodeType.CLASS_LITERAL, 66, 79);
			expected.allocateNode(NodeType.LINE_COMMENT, 83, 96);
			expected.allocateNode(NodeType.ARRAY_CREATION, 31, 99);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 100);
			expected.allocateClassDeclaration(0, 102, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 103);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
