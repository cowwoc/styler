package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ARRAY_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CAST_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONDITIONAL_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETERIZED_TYPE;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.UNARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.WILDCARD_TYPE;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = (int) longValue;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 58),
			semanticNode(CLASS_DECLARATION, 0, 57, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 55),
			semanticNode(BLOCK, 24, 55),
			semanticNode(CAST_EXPRESSION, 36, 51),
			semanticNode(IDENTIFIER, 42, 51));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a primitive double cast parses correctly.
	 */
	@Test
	public void shouldParsePrimitiveDoubleCast()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					double d = (double) intValue;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 63),
			semanticNode(CLASS_DECLARATION, 0, 62, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 60),
			semanticNode(BLOCK, 24, 60),
			semanticNode(CAST_EXPRESSION, 39, 56),
			semanticNode(IDENTIFIER, 48, 56));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a reference type cast parses correctly.
	 */
	@Test
	public void shouldParseReferenceTypeCast()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					String s = (String) obj;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 58),
			semanticNode(CLASS_DECLARATION, 0, 57, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 55),
			semanticNode(BLOCK, 24, 55),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CAST_EXPRESSION, 39, 51),
			semanticNode(IDENTIFIER, 48, 51));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a qualified type cast parses correctly.
	 */
	@Test
	public void shouldParseQualifiedTypeCast()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					String s = (java.lang.String) obj;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 68),
			semanticNode(CLASS_DECLARATION, 0, 67, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 65),
			semanticNode(BLOCK, 24, 65),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CAST_EXPRESSION, 39, 61),
			semanticNode(IDENTIFIER, 58, 61));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a generic type cast parses correctly.
	 */
	@Test
	public void shouldParseGenericTypeCast()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					List<String> list = (List<String>) obj;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 73),
			semanticNode(CLASS_DECLARATION, 0, 72, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 70),
			semanticNode(BLOCK, 24, 70),
			semanticNode(QUALIFIED_NAME, 28, 32),
			semanticNode(QUALIFIED_NAME, 33, 39),
			semanticNode(PARAMETERIZED_TYPE, 28, 40),
			semanticNode(CAST_EXPRESSION, 48, 66),
			semanticNode(QUALIFIED_NAME, 54, 60),
			semanticNode(IDENTIFIER, 63, 66));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========== Intersection Casts (3 tests) ==========

	/**
	 * Validates that an intersection cast with two types parses correctly.
	 */
	@Test
	public void shouldParseIntersectionCastTwoTypes()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Object o = (Serializable & Comparable<?>) value;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 82),
			semanticNode(CLASS_DECLARATION, 0, 81, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 79),
			semanticNode(BLOCK, 24, 79),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CAST_EXPRESSION, 39, 75),
			semanticNode(WILDCARD_TYPE, 66, 67),
			semanticNode(IDENTIFIER, 70, 75));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that an intersection cast with three types parses correctly.
	 */
	@Test
	public void shouldParseIntersectionCastThreeTypes()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Object o = (Serializable & Comparable<?> & Cloneable) value;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 94),
			semanticNode(CLASS_DECLARATION, 0, 93, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 91),
			semanticNode(BLOCK, 24, 91),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CAST_EXPRESSION, 39, 87),
			semanticNode(WILDCARD_TYPE, 66, 67),
			semanticNode(IDENTIFIER, 82, 87));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that an intersection cast with qualified types parses correctly.
	 */
	@Test
	public void shouldParseIntersectionCastQualifiedTypes()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Object o = (java.io.Serializable & java.lang.Comparable<?>) value;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 100),
			semanticNode(CLASS_DECLARATION, 0, 99, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 97),
			semanticNode(BLOCK, 24, 97),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CAST_EXPRESSION, 39, 93),
			semanticNode(WILDCARD_TYPE, 84, 85),
			semanticNode(IDENTIFIER, 88, 93));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========== Array Casts (3 tests) ==========

	/**
	 * Validates that a single-dimension array cast parses correctly.
	 */
	@Test
	public void shouldParseSingleDimensionArrayCast()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					String[] arr = (String[]) array;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 66),
			semanticNode(CLASS_DECLARATION, 0, 65, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 63),
			semanticNode(BLOCK, 24, 63),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CAST_EXPRESSION, 43, 59),
			semanticNode(IDENTIFIER, 54, 59));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a multi-dimension array cast parses correctly.
	 */
	@Test
	public void shouldParseMultiDimensionArrayCast()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int[][] matrix = (int[][]) array;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 67),
			semanticNode(CLASS_DECLARATION, 0, 66, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 64),
			semanticNode(BLOCK, 24, 64),
			semanticNode(CAST_EXPRESSION, 45, 60),
			semanticNode(IDENTIFIER, 55, 60));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a primitive array cast parses correctly.
	 */
	@Test
	public void shouldParsePrimitiveArrayCast()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int[] arr = (int[]) obj;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 58),
			semanticNode(CLASS_DECLARATION, 0, 57, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 55),
			semanticNode(BLOCK, 24, 55),
			semanticNode(CAST_EXPRESSION, 40, 51),
			semanticNode(IDENTIFIER, 48, 51));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int result = (a) + b;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 55),
			semanticNode(CLASS_DECLARATION, 0, 54, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 52),
			semanticNode(BLOCK, 24, 52),
			semanticNode(BINARY_EXPRESSION, 42, 48),
			semanticNode(IDENTIFIER, 42, 43),
			semanticNode(IDENTIFIER, 47, 48));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a primitive cast of unary plus parses correctly.
	 * The expression {@code (int) +b} is valid because primitive casts can be followed by any unary
	 * expression.
	 */
	@Test
	public void shouldParsePrimitiveCastOfUnaryPlus()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int i = (int) +b;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 51),
			semanticNode(CLASS_DECLARATION, 0, 50, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 48),
			semanticNode(BLOCK, 24, 48),
			semanticNode(CAST_EXPRESSION, 36, 44),
			semanticNode(UNARY_EXPRESSION, 42, 44),
			semanticNode(IDENTIFIER, 43, 44));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a primitive cast of unary minus parses correctly.
	 * The expression {@code (int) -value} is valid because primitive casts can be followed by any unary
	 * expression.
	 */
	@Test
	public void shouldParsePrimitiveCastOfUnaryMinus()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int d = (int) -value;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 55),
			semanticNode(CLASS_DECLARATION, 0, 54, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 52),
			semanticNode(BLOCK, 24, 52),
			semanticNode(CAST_EXPRESSION, 36, 48),
			semanticNode(UNARY_EXPRESSION, 42, 48),
			semanticNode(IDENTIFIER, 43, 48));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a cast of method call parses correctly.
	 */
	@Test
	public void shouldParseCastOfMethodCall()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					String s = (String) getValue();
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 65),
			semanticNode(CLASS_DECLARATION, 0, 64, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 62),
			semanticNode(BLOCK, 24, 62),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CAST_EXPRESSION, 39, 58),
			semanticNode(METHOD_INVOCATION, 48, 58),
			semanticNode(IDENTIFIER, 48, 56));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a cast of field access parses correctly.
	 */
	@Test
	public void shouldParseCastOfFieldAccess()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = (int) obj.field;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 58),
			semanticNode(CLASS_DECLARATION, 0, 57, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 55),
			semanticNode(BLOCK, 24, 55),
			semanticNode(CAST_EXPRESSION, 36, 51),
			semanticNode(FIELD_ACCESS, 42, 51),
			semanticNode(IDENTIFIER, 42, 45));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========== Chained/Nested (4 tests) ==========

	/**
	 * Validates that chained casts parse correctly.
	 */
	@Test
	public void shouldParseChainedCasts()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Object o = (Object) (String) value;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 69),
			semanticNode(CLASS_DECLARATION, 0, 68, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 66),
			semanticNode(BLOCK, 24, 66),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CAST_EXPRESSION, 39, 62),
			semanticNode(CAST_EXPRESSION, 48, 62),
			semanticNode(IDENTIFIER, 57, 62));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a cast with method call on result parses correctly.
	 */
	@Test
	public void shouldParseCastWithMethodCallOnResult()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int len = ((String) obj).length();
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 68),
			semanticNode(CLASS_DECLARATION, 0, 67, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 65),
			semanticNode(BLOCK, 24, 65),
			semanticNode(METHOD_INVOCATION, 39, 61),
			semanticNode(FIELD_ACCESS, 39, 59),
			semanticNode(CAST_EXPRESSION, 39, 51),
			semanticNode(IDENTIFIER, 48, 51));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a cast as method argument parses correctly.
	 */
	@Test
	public void shouldParseCastAsMethodArgument()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					process((String) obj);
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(CLASS_DECLARATION, 0, 55, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 53),
			semanticNode(BLOCK, 24, 53),
			semanticNode(METHOD_INVOCATION, 28, 49),
			semanticNode(QUALIFIED_NAME, 28, 35),
			semanticNode(IDENTIFIER, 28, 35),
			semanticNode(CAST_EXPRESSION, 36, 48),
			semanticNode(IDENTIFIER, 45, 48));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a cast in return statement parses correctly.
	 */
	@Test
	public void shouldParseCastInReturnStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				String m()
				{
					return (String) obj;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(CLASS_DECLARATION, 0, 55, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 53),
			semanticNode(BLOCK, 26, 53),
			semanticNode(RETURN_STATEMENT, 30, 50),
			semanticNode(CAST_EXPRESSION, 37, 49),
			semanticNode(IDENTIFIER, 46, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========== Expression Contexts (4 tests) ==========

	/**
	 * Validates that casts in ternary conditional expression parse correctly.
	 */
	@Test
	public void shouldParseCastsInTernaryExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					String s = flag ? (String) a : (String) b;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 76),
			semanticNode(CLASS_DECLARATION, 0, 75, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 73),
			semanticNode(BLOCK, 24, 73),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CONDITIONAL_EXPRESSION, 39, 69),
			semanticNode(IDENTIFIER, 39, 43),
			semanticNode(CAST_EXPRESSION, 46, 56),
			semanticNode(IDENTIFIER, 55, 56),
			semanticNode(CAST_EXPRESSION, 59, 69),
			semanticNode(IDENTIFIER, 68, 69));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that casts in binary expression parse correctly.
	 */
	@Test
	public void shouldParseCastsInBinaryExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int result = (int) a + (int) b;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 65),
			semanticNode(CLASS_DECLARATION, 0, 64, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 62),
			semanticNode(BLOCK, 24, 62),
			semanticNode(BINARY_EXPRESSION, 41, 58),
			semanticNode(CAST_EXPRESSION, 41, 48),
			semanticNode(IDENTIFIER, 47, 48),
			semanticNode(CAST_EXPRESSION, 51, 58),
			semanticNode(IDENTIFIER, 57, 58));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a cast with array access parses correctly.
	 */
	@Test
	public void shouldParseCastWithArrayAccess()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					String s = ((String[]) array)[0];
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 67),
			semanticNode(CLASS_DECLARATION, 0, 66, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 64),
			semanticNode(BLOCK, 24, 64),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(ARRAY_ACCESS, 40, 60),
			semanticNode(CAST_EXPRESSION, 40, 56),
			semanticNode(IDENTIFIER, 51, 56),
			semanticNode(INTEGER_LITERAL, 58, 59));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a cast of integer literal parses correctly.
	 */
	@Test
	public void shouldParseCastOfLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					double d = (double) 42;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 57),
			semanticNode(CLASS_DECLARATION, 0, 56, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 54),
			semanticNode(BLOCK, 24, 54),
			semanticNode(CAST_EXPRESSION, 39, 50),
			semanticNode(INTEGER_LITERAL, 48, 50));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					String s = (String);
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 54),
			semanticNode(CLASS_DECLARATION, 0, 53, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 51),
			semanticNode(BLOCK, 24, 51),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(IDENTIFIER, 40, 46));
		requireThat(actual, "actual").isEqualTo(expected);
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
}
