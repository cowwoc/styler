package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing array type constructor references ({@code int[]::new}, {@code String[][]::new}).
 * Array type constructor references create array instances through functional interfaces like
 * {@code IntFunction<int[]>} for primitive arrays or {@code Function<Integer, String[]>} for reference arrays.
 */
public class ArrayTypeMethodReferenceParserTest
{
	// ========================================
	// Primitive Array Constructor References (5 tests)
	// ========================================

	/**
	 * Tests single-dimension primitive array constructor reference.
	 * The form {@code int[]::new} creates an {@code IntFunction<int[]>} that allocates arrays.
	 */
	@Test
	public void shouldParseIntArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = int[]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 30);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 35);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 36);
			expected.allocateClassDeclaration(0, 38, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 39);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests double array constructor reference for primitive type.
	 * The form {@code double[]::new} creates arrays of primitive double values.
	 */
	@Test
	public void shouldParseDoubleArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = double[]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 33);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 38);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 39);
			expected.allocateClassDeclaration(0, 41, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests boolean array constructor reference.
	 * All primitive types support array constructor references.
	 */
	@Test
	public void shouldParseBooleanArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = boolean[]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 34);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 39);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 40);
			expected.allocateClassDeclaration(0, 42, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 43);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests two-dimensional primitive array constructor reference.
	 * Multi-dimensional arrays require the outer dimension to be created.
	 */
	@Test
	public void shouldParseTwoDimensionalIntArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = int[][]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 32);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 37);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 38);
			expected.allocateClassDeclaration(0, 40, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 41);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests three-dimensional primitive array constructor reference.
	 * Tests the parser handles arbitrary dimensions correctly.
	 */
	@Test
	public void shouldParseThreeDimensionalIntArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = int[][][]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 34);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 39);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 40);
			expected.allocateClassDeclaration(0, 42, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 43);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Reference Array Constructor References (4 tests)
	// ========================================

	/**
	 * Tests String array constructor reference.
	 * Reference type arrays work the same as primitive arrays for constructor references.
	 */
	@Test
	public void shouldParseStringArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = String[]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 31);
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 33);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 38);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 39);
			expected.allocateClassDeclaration(0, 41, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests Object array constructor reference.
	 * The root reference type also supports array constructor references.
	 */
	@Test
	public void shouldParseObjectArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = Object[]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 31);
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 33);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 38);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 39);
			expected.allocateClassDeclaration(0, 41, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests two-dimensional String array constructor reference.
	 * Reference type multi-dimensional arrays also work.
	 */
	@Test
	public void shouldParseTwoDimensionalStringArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = String[][]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 31);
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 35);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 40);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 41);
			expected.allocateClassDeclaration(0, 43, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 44);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests qualified type array constructor reference.
	 * Fully qualified types like {@code java.util.List[]::new} are valid.
	 */
	@Test
	public void shouldParseQualifiedTypeArrayConstructorReference()
	{
		String source = """
			class Test
			{
				Object f = java.util.List[]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 29);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 34);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 39);
			expected.allocateNode(NodeType.ARRAY_TYPE, 25, 41);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 46);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 47);
			expected.allocateClassDeclaration(0, 49, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 50);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Expression Contexts (5 tests)
	// ========================================

	/**
	 * Tests array constructor reference as method argument.
	 * Common usage with {@code Stream.toArray(String[]::new)}.
	 */
	@Test
	public void shouldParseArrayConstructorReferenceAsMethodArgument()
	{
		String source = """
			class Test
			{
				void method()
				{
					accept(String[]::new);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateNode(NodeType.IDENTIFIER, 33, 39);
			expected.allocateNode(NodeType.IDENTIFIER, 40, 46);
			expected.allocateNode(NodeType.ARRAY_TYPE, 40, 48);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 40, 53);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 33, 54);
			expected.allocateNode(NodeType.BLOCK, 29, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests array constructor reference in return statement.
	 * Array constructor references can be returned from methods.
	 */
	@Test
	public void shouldParseArrayConstructorReferenceInReturnStatement()
	{
		String source = """
			class Test
			{
				Object method()
				{
					return int[]::new;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_TYPE, 42, 47);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 42, 52);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 35, 53);
			expected.allocateNode(NodeType.BLOCK, 31, 56);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 56);
			expected.allocateClassDeclaration(0, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests array constructor reference in ternary conditional.
	 * Array constructor references can appear in conditional branches.
	 */
	@Test
	public void shouldParseArrayConstructorReferenceInTernary()
	{
		String source = """
			class Test
			{
				void method(boolean flag)
				{
					Object f = flag ? int[]::new : long[]::new;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(26, 38, new ParameterAttribute("flag", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 45, 51);
			expected.allocateNode(NodeType.IDENTIFIER, 56, 60);
			expected.allocateNode(NodeType.ARRAY_TYPE, 63, 68);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 63, 73);
			expected.allocateNode(NodeType.ARRAY_TYPE, 76, 82);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 76, 87);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 56, 87);
			expected.allocateNode(NodeType.BLOCK, 41, 91);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 91);
			expected.allocateClassDeclaration(0, 93, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 94);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests array constructor reference in field initialization.
	 * Array constructor references can initialize fields.
	 */
	@Test
	public void shouldParseArrayConstructorReferenceInFieldInitializer()
	{
		String source = """
			class Test
			{
				Object intArrayFactory = int[]::new;
				Object stringArrayFactory = String[]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_TYPE, 39, 44);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 39, 49);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 50);
			expected.allocateNode(NodeType.IDENTIFIER, 80, 86);
			expected.allocateNode(NodeType.ARRAY_TYPE, 80, 88);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 80, 93);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 52, 94);
			expected.allocateClassDeclaration(0, 96, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 97);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests array constructor reference in lambda body.
	 * Array constructor references can be returned from lambdas.
	 */
	@Test
	public void shouldParseArrayConstructorReferenceInLambdaBody()
	{
		String source = """
			class Test
			{
				Object f = () -> int[]::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_TYPE, 31, 36);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 31, 41);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 25, 41);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 42);
			expected.allocateClassDeclaration(0, 44, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Error Cases (3 tests)
	// ========================================

	/**
	 * Tests that primitive type without array dimensions fails for constructor reference.
	 * The syntax {@code int::new} is invalid because primitives are not instantiable.
	 */
	@Test
	public void shouldRejectPrimitiveWithoutArrayDimensions()
	{
		assertParseFails("""
			class Test
			{
				Object x = int::new;
			}
			""");
	}

	/**
	 * Tests that array constructor reference with extra colon fails.
	 * The syntax {@code int[]:::new} is malformed.
	 */
	@Test
	public void shouldRejectArrayConstructorReferenceWithExtraColon()
	{
		assertParseFails("""
			class Test
			{
				Object x = int[]:::new;
			}
			""");
	}

	/**
	 * Tests that array constructor reference with missing new fails.
	 * The syntax {@code int[]::} is incomplete.
	 */
	@Test
	public void shouldRejectArrayConstructorReferenceWithMissingNew()
	{
		assertParseFails("""
			class Test
			{
				Object x = int[]::;
			}
			""");
	}
}
