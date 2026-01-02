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
			compilationUnit( 0, 58),
			typeDeclaration(CLASS_DECLARATION, 0, 57, "Test"),
			methodDeclaration( 14, 55),
			block( 24, 55),
			castExpression( 36, 51),
			identifier( 42, 51));
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
			compilationUnit( 0, 63),
			typeDeclaration(CLASS_DECLARATION, 0, 62, "Test"),
			methodDeclaration( 14, 60),
			block( 24, 60),
			castExpression( 39, 56),
			identifier( 48, 56));
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
			compilationUnit( 0, 58),
			typeDeclaration(CLASS_DECLARATION, 0, 57, "Test"),
			methodDeclaration( 14, 55),
			block( 24, 55),
			qualifiedName( 28, 34),
			castExpression( 39, 51),
			identifier( 48, 51));
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
			compilationUnit( 0, 68),
			typeDeclaration(CLASS_DECLARATION, 0, 67, "Test"),
			methodDeclaration( 14, 65),
			block( 24, 65),
			qualifiedName( 28, 34),
			castExpression( 39, 61),
			identifier( 58, 61));
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
			compilationUnit( 0, 73),
			typeDeclaration(CLASS_DECLARATION, 0, 72, "Test"),
			methodDeclaration( 14, 70),
			block( 24, 70),
			qualifiedName( 28, 32),
			qualifiedName( 33, 39),
			parameterizedType( 28, 40),
			castExpression( 48, 66),
			qualifiedName( 54, 60),
			identifier( 63, 66));
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
			compilationUnit( 0, 82),
			typeDeclaration(CLASS_DECLARATION, 0, 81, "Test"),
			methodDeclaration( 14, 79),
			block( 24, 79),
			qualifiedName( 28, 34),
			castExpression( 39, 75),
			wildcardType( 66, 67),
			identifier( 70, 75));
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
			compilationUnit( 0, 94),
			typeDeclaration(CLASS_DECLARATION, 0, 93, "Test"),
			methodDeclaration( 14, 91),
			block( 24, 91),
			qualifiedName( 28, 34),
			castExpression( 39, 87),
			wildcardType( 66, 67),
			identifier( 82, 87));
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
			compilationUnit( 0, 100),
			typeDeclaration(CLASS_DECLARATION, 0, 99, "Test"),
			methodDeclaration( 14, 97),
			block( 24, 97),
			qualifiedName( 28, 34),
			castExpression( 39, 93),
			wildcardType( 84, 85),
			identifier( 88, 93));
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
			compilationUnit( 0, 66),
			typeDeclaration(CLASS_DECLARATION, 0, 65, "Test"),
			methodDeclaration( 14, 63),
			block( 24, 63),
			qualifiedName( 28, 34),
			castExpression( 43, 59),
			identifier( 54, 59));
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
			compilationUnit( 0, 67),
			typeDeclaration(CLASS_DECLARATION, 0, 66, "Test"),
			methodDeclaration( 14, 64),
			block( 24, 64),
			castExpression( 45, 60),
			identifier( 55, 60));
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
			compilationUnit( 0, 58),
			typeDeclaration(CLASS_DECLARATION, 0, 57, "Test"),
			methodDeclaration( 14, 55),
			block( 24, 55),
			castExpression( 40, 51),
			identifier( 48, 51));
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
			compilationUnit( 0, 55),
			typeDeclaration(CLASS_DECLARATION, 0, 54, "Test"),
			methodDeclaration( 14, 52),
			block( 24, 52),
			binaryExpression( 42, 48),
			identifier( 42, 43),
			identifier( 47, 48));
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
			compilationUnit( 0, 51),
			typeDeclaration(CLASS_DECLARATION, 0, 50, "Test"),
			methodDeclaration( 14, 48),
			block( 24, 48),
			castExpression( 36, 44),
			unaryExpression( 42, 44),
			identifier( 43, 44));
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
			compilationUnit( 0, 55),
			typeDeclaration(CLASS_DECLARATION, 0, 54, "Test"),
			methodDeclaration( 14, 52),
			block( 24, 52),
			castExpression( 36, 48),
			unaryExpression( 42, 48),
			identifier( 43, 48));
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
			compilationUnit( 0, 65),
			typeDeclaration(CLASS_DECLARATION, 0, 64, "Test"),
			methodDeclaration( 14, 62),
			block( 24, 62),
			qualifiedName( 28, 34),
			castExpression( 39, 58),
			methodInvocation( 48, 58),
			identifier( 48, 56));
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
			compilationUnit( 0, 58),
			typeDeclaration(CLASS_DECLARATION, 0, 57, "Test"),
			methodDeclaration( 14, 55),
			block( 24, 55),
			castExpression( 36, 51),
			fieldAccess( 42, 51),
			identifier( 42, 45));
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
			compilationUnit( 0, 69),
			typeDeclaration(CLASS_DECLARATION, 0, 68, "Test"),
			methodDeclaration( 14, 66),
			block( 24, 66),
			qualifiedName( 28, 34),
			castExpression( 39, 62),
			castExpression( 48, 62),
			identifier( 57, 62));
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
			compilationUnit( 0, 68),
			typeDeclaration(CLASS_DECLARATION, 0, 67, "Test"),
			methodDeclaration( 14, 65),
			block( 24, 65),
			methodInvocation( 39, 61),
			fieldAccess( 39, 59),
			castExpression( 39, 51),
			identifier( 48, 51));
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
			compilationUnit( 0, 56),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "Test"),
			methodDeclaration( 14, 53),
			block( 24, 53),
			methodInvocation( 28, 49),
			qualifiedName( 28, 35),
			identifier( 28, 35),
			castExpression( 36, 48),
			identifier( 45, 48));
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
			compilationUnit( 0, 56),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "Test"),
			methodDeclaration( 14, 53),
			block( 26, 53),
			returnStatement( 30, 50),
			castExpression( 37, 49),
			identifier( 46, 49));
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
			compilationUnit( 0, 76),
			typeDeclaration(CLASS_DECLARATION, 0, 75, "Test"),
			methodDeclaration( 14, 73),
			block( 24, 73),
			qualifiedName( 28, 34),
			conditionalExpression( 39, 69),
			identifier( 39, 43),
			castExpression( 46, 56),
			identifier( 55, 56),
			castExpression( 59, 69),
			identifier( 68, 69));
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
			compilationUnit( 0, 65),
			typeDeclaration(CLASS_DECLARATION, 0, 64, "Test"),
			methodDeclaration( 14, 62),
			block( 24, 62),
			binaryExpression( 41, 58),
			castExpression( 41, 48),
			identifier( 47, 48),
			castExpression( 51, 58),
			identifier( 57, 58));
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
			compilationUnit( 0, 67),
			typeDeclaration(CLASS_DECLARATION, 0, 66, "Test"),
			methodDeclaration( 14, 64),
			block( 24, 64),
			qualifiedName( 28, 34),
			arrayAccess( 40, 60),
			castExpression( 40, 56),
			identifier( 51, 56),
			integerLiteral( 58, 59));
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
			compilationUnit( 0, 57),
			typeDeclaration(CLASS_DECLARATION, 0, 56, "Test"),
			methodDeclaration( 14, 54),
			block( 24, 54),
			castExpression( 39, 50),
			integerLiteral( 48, 50));
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
			compilationUnit( 0, 54),
			typeDeclaration(CLASS_DECLARATION, 0, 53, "Test"),
			methodDeclaration( 14, 51),
			block( 24, 51),
			qualifiedName( 28, 34),
			identifier( 40, 46));
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
