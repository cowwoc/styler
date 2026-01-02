package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 39),
			typeDeclaration(CLASS_DECLARATION, 0, 38, "Test"),
			fieldDeclaration( 14, 36),
			methodReference( 25, 35),
			arrayType( 25, 30));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 42),
			typeDeclaration(CLASS_DECLARATION, 0, 41, "Test"),
			fieldDeclaration( 14, 39),
			methodReference( 25, 38),
			arrayType( 25, 33));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 43),
			typeDeclaration(CLASS_DECLARATION, 0, 42, "Test"),
			fieldDeclaration( 14, 40),
			methodReference( 25, 39),
			arrayType( 25, 34));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 41),
			typeDeclaration(CLASS_DECLARATION, 0, 40, "Test"),
			fieldDeclaration( 14, 38),
			methodReference( 25, 37),
			arrayType( 25, 32));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 43),
			typeDeclaration(CLASS_DECLARATION, 0, 42, "Test"),
			fieldDeclaration( 14, 40),
			methodReference( 25, 39),
			arrayType( 25, 34));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 42),
			typeDeclaration(CLASS_DECLARATION, 0, 41, "Test"),
			fieldDeclaration( 14, 39),
			methodReference( 25, 38),
			arrayType( 25, 33),
			identifier( 25, 31));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 42),
			typeDeclaration(CLASS_DECLARATION, 0, 41, "Test"),
			fieldDeclaration( 14, 39),
			methodReference( 25, 38),
			arrayType( 25, 33),
			identifier( 25, 31));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 44),
			typeDeclaration(CLASS_DECLARATION, 0, 43, "Test"),
			fieldDeclaration( 14, 41),
			methodReference( 25, 40),
			arrayType( 25, 35),
			identifier( 25, 31));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 50),
			typeDeclaration(CLASS_DECLARATION, 0, 49, "Test"),
			fieldDeclaration( 14, 47),
			methodReference( 25, 46),
			arrayType( 25, 41),
			fieldAccess( 25, 34),
			fieldAccess( 25, 39),
			identifier( 25, 29));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 61),
			typeDeclaration(CLASS_DECLARATION, 0, 60, "Test"),
			methodDeclaration( 14, 58),
			block( 29, 58),
			methodInvocation( 33, 54),
			qualifiedName( 33, 39),
			identifier( 33, 39),
			methodReference( 40, 53),
			arrayType( 40, 48),
			identifier( 40, 46));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 59),
			typeDeclaration(CLASS_DECLARATION, 0, 58, "Test"),
			methodDeclaration( 14, 56),
			block( 31, 56),
			returnStatement( 35, 53),
			methodReference( 42, 52),
			arrayType( 42, 47));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 94),
			typeDeclaration(CLASS_DECLARATION, 0, 93, "Test"),
			methodDeclaration( 14, 91),
			parameterNode( 26, 38, "flag"),
			block( 41, 91),
			qualifiedName( 45, 51),
			conditionalExpression( 56, 87),
			identifier( 56, 60),
			methodReference( 63, 73),
			arrayType( 63, 68),
			methodReference( 76, 87),
			arrayType( 76, 82));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 97),
			typeDeclaration(CLASS_DECLARATION, 0, 96, "Test"),
			fieldDeclaration( 14, 50),
			methodReference( 39, 49),
			arrayType( 39, 44),
			fieldDeclaration( 52, 94),
			methodReference( 80, 93),
			arrayType( 80, 88),
			identifier( 80, 86));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 45),
			typeDeclaration(CLASS_DECLARATION, 0, 44, "Test"),
			fieldDeclaration( 14, 42),
			lambdaExpression( 25, 41),
			methodReference( 31, 41),
			arrayType( 31, 36));

		requireThat(actual, "actual").isEqualTo(expected);
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
