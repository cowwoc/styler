package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ARRAY_TYPE;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONDITIONAL_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.LAMBDA_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_REFERENCE;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 39),
			semanticNode(CLASS_DECLARATION, 0, 38, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 36),
			semanticNode(METHOD_REFERENCE, 25, 35),
			semanticNode(ARRAY_TYPE, 25, 30));

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
			semanticNode(COMPILATION_UNIT, 0, 42),
			semanticNode(CLASS_DECLARATION, 0, 41, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 39),
			semanticNode(METHOD_REFERENCE, 25, 38),
			semanticNode(ARRAY_TYPE, 25, 33));

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
			semanticNode(COMPILATION_UNIT, 0, 43),
			semanticNode(CLASS_DECLARATION, 0, 42, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 40),
			semanticNode(METHOD_REFERENCE, 25, 39),
			semanticNode(ARRAY_TYPE, 25, 34));

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
			semanticNode(COMPILATION_UNIT, 0, 41),
			semanticNode(CLASS_DECLARATION, 0, 40, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 38),
			semanticNode(METHOD_REFERENCE, 25, 37),
			semanticNode(ARRAY_TYPE, 25, 32));

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
			semanticNode(COMPILATION_UNIT, 0, 43),
			semanticNode(CLASS_DECLARATION, 0, 42, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 40),
			semanticNode(METHOD_REFERENCE, 25, 39),
			semanticNode(ARRAY_TYPE, 25, 34));

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
			semanticNode(COMPILATION_UNIT, 0, 42),
			semanticNode(CLASS_DECLARATION, 0, 41, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 39),
			semanticNode(METHOD_REFERENCE, 25, 38),
			semanticNode(ARRAY_TYPE, 25, 33),
			semanticNode(IDENTIFIER, 25, 31));

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
			semanticNode(COMPILATION_UNIT, 0, 42),
			semanticNode(CLASS_DECLARATION, 0, 41, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 39),
			semanticNode(METHOD_REFERENCE, 25, 38),
			semanticNode(ARRAY_TYPE, 25, 33),
			semanticNode(IDENTIFIER, 25, 31));

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
			semanticNode(COMPILATION_UNIT, 0, 44),
			semanticNode(CLASS_DECLARATION, 0, 43, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 41),
			semanticNode(METHOD_REFERENCE, 25, 40),
			semanticNode(ARRAY_TYPE, 25, 35),
			semanticNode(IDENTIFIER, 25, 31));

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
			semanticNode(COMPILATION_UNIT, 0, 50),
			semanticNode(CLASS_DECLARATION, 0, 49, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 47),
			semanticNode(METHOD_REFERENCE, 25, 46),
			semanticNode(ARRAY_TYPE, 25, 41),
			semanticNode(FIELD_ACCESS, 25, 34),
			semanticNode(FIELD_ACCESS, 25, 39),
			semanticNode(IDENTIFIER, 25, 29));

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
			semanticNode(COMPILATION_UNIT, 0, 61),
			semanticNode(CLASS_DECLARATION, 0, 60, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 58),
			semanticNode(BLOCK, 29, 58),
			semanticNode(METHOD_INVOCATION, 33, 54),
			semanticNode(QUALIFIED_NAME, 33, 39),
			semanticNode(IDENTIFIER, 33, 39),
			semanticNode(METHOD_REFERENCE, 40, 53),
			semanticNode(ARRAY_TYPE, 40, 48),
			semanticNode(IDENTIFIER, 40, 46));

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
			semanticNode(COMPILATION_UNIT, 0, 59),
			semanticNode(CLASS_DECLARATION, 0, 58, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 56),
			semanticNode(BLOCK, 31, 56),
			semanticNode(RETURN_STATEMENT, 35, 53),
			semanticNode(METHOD_REFERENCE, 42, 52),
			semanticNode(ARRAY_TYPE, 42, 47));

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
			semanticNode(COMPILATION_UNIT, 0, 94),
			semanticNode(CLASS_DECLARATION, 0, 93, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 91),
			semanticNode(PARAMETER_DECLARATION, 26, 38, "flag"),
			semanticNode(BLOCK, 41, 91),
			semanticNode(QUALIFIED_NAME, 45, 51),
			semanticNode(CONDITIONAL_EXPRESSION, 56, 87),
			semanticNode(IDENTIFIER, 56, 60),
			semanticNode(METHOD_REFERENCE, 63, 73),
			semanticNode(ARRAY_TYPE, 63, 68),
			semanticNode(METHOD_REFERENCE, 76, 87),
			semanticNode(ARRAY_TYPE, 76, 82));

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
			semanticNode(COMPILATION_UNIT, 0, 97),
			semanticNode(CLASS_DECLARATION, 0, 96, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 50),
			semanticNode(METHOD_REFERENCE, 39, 49),
			semanticNode(ARRAY_TYPE, 39, 44),
			semanticNode(FIELD_DECLARATION, 52, 94),
			semanticNode(METHOD_REFERENCE, 80, 93),
			semanticNode(ARRAY_TYPE, 80, 88),
			semanticNode(IDENTIFIER, 80, 86));

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
			semanticNode(COMPILATION_UNIT, 0, 45),
			semanticNode(CLASS_DECLARATION, 0, 44, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 42),
			semanticNode(LAMBDA_EXPRESSION, 25, 41),
			semanticNode(METHOD_REFERENCE, 31, 41),
			semanticNode(ARRAY_TYPE, 31, 36));

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
