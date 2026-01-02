package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.SecurityConfig;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

/**
 * Tests for parsing expressions, literals, and basic language constructs.
 */
public class ParserTest
{
	/**
	 * Tests that parser handles empty source input gracefully.
	 * Empty source produces a zero-width COMPILATION_UNIT at [0, 0).
	 */
	@Test
	public void testEmptySource()
	{
		Set<SemanticNode> actual = parseSemanticAst("");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 0));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Ensures that null source code is rejected with NullPointerException.
	 * This validates fail-fast behavior preventing downstream null reference errors.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testNullSource()
	{
		new Parser(null);
	}

	/**
	 * Tests parsing of integer literal expressions.
	 * Validates that the parser correctly handles numeric constants
	 * in expression contexts.
	 */
	@Test
	public void testIntegerLiteralExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = 42;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 45),
			typeDeclaration(CLASS_DECLARATION, 0, 44, "Test"),
			methodDeclaration( 14, 42),
			block( 24, 42),
			integerLiteral( 36, 38));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of string literal expressions.
	 * Validates that the parser correctly handles quoted string constants
	 * including quote characters themselves.
	 */
	@Test
	public void testStringLiteralExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					String x = "hello";
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 53),
			typeDeclaration(CLASS_DECLARATION, 0, 52, "Test"),
			methodDeclaration( 14, 50),
			block( 24, 50),
			qualifiedName( 28, 34),
			stringLiteral( 39, 46));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of identifier expressions (variable references).
	 * Validates that the parser correctly handles variable name references
	 * in expression positions.
	 */
	@Test
	public void testIdentifierExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Object x = myVariable;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 56),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "Test"),
			methodDeclaration( 14, 53),
			block( 24, 53),
			qualifiedName( 28, 34),
			identifier( 39, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of binary addition expressions.
	 * Validates that the parser handles the + operator with two operands,
	 * forming a binary expression tree node.
	 */
	@Test
	public void testBinaryAddition()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = 1 + 2;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 48),
			typeDeclaration(CLASS_DECLARATION, 0, 47, "Test"),
			methodDeclaration( 14, 45),
			block( 24, 45),
			binaryExpression( 36, 41),
			integerLiteral( 36, 37),
			integerLiteral( 40, 41));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of binary multiplication expressions.
	 * Validates that the parser handles the * operator,
	 * which has higher precedence than addition.
	 */
	@Test
	public void testBinaryMultiplication()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = 3 * 4;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 48),
			typeDeclaration(CLASS_DECLARATION, 0, 47, "Test"),
			methodDeclaration( 14, 45),
			block( 24, 45),
			binaryExpression( 36, 41),
			integerLiteral( 36, 37),
			integerLiteral( 40, 41));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests correct operator precedence in mixed arithmetic expressions.
	 * Validates that multiplication binds tighter than addition,
	 * so "1 + 2 * 3" parses as "1 + (2 * 3)" not "(1 + 2) * 3".
	 */
	@Test
	public void testOperatorPrecedence()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = 1 + 2 * 3;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 52),
			typeDeclaration(CLASS_DECLARATION, 0, 51, "Test"),
			methodDeclaration( 14, 49),
			block( 24, 49),
			binaryExpression( 36, 45),
			binaryExpression( 40, 45),
			integerLiteral( 36, 37),
			integerLiteral( 40, 41),
			integerLiteral( 44, 45));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parenthesized expressions that override operator precedence.
	 * Validates that parentheses force "(1 + 2) * 3" to evaluate addition first,
	 * producing different result than natural precedence would.
	 */
	@Test
	public void testParenthesizedExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = (1 + 2) * 3;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 54),
			typeDeclaration(CLASS_DECLARATION, 0, 53, "Test"),
			methodDeclaration( 14, 51),
			block( 24, 51),
			binaryExpression( 37, 47),
			binaryExpression( 37, 42),
			integerLiteral( 37, 38),
			integerLiteral( 41, 42),
			integerLiteral( 46, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests unary minus operator for numeric negation.
	 * Validates parsing of prefix minus that creates negative literals
	 * or negates expression results.
	 */
	@Test
	public void testUnaryMinus()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = -5;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 45),
			typeDeclaration(CLASS_DECLARATION, 0, 44, "Test"),
			methodDeclaration( 14, 42),
			block( 24, 42),
			unaryExpression( 36, 38),
			integerLiteral( 37, 38));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests unary logical NOT operator.
	 * Validates parsing of ! operator that inverts boolean values,
	 * essential for conditional logic.
	 */
	@Test
	public void testUnaryNot()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					boolean x = !true;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 52),
			typeDeclaration(CLASS_DECLARATION, 0, 51, "Test"),
			methodDeclaration( 14, 49),
			block( 24, 49),
			unaryExpression( 40, 45),
			booleanLiteral( 41, 45));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method call syntax without arguments.
	 * Validates parsing of method invocation with empty parameter list,
	 * the simplest form of method calls.
	 */
	@Test
	public void testMethodCall()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					foo();
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 40),
			typeDeclaration(CLASS_DECLARATION, 0, 39, "Test"),
			methodDeclaration( 14, 37),
			block( 24, 37),
			methodInvocation( 28, 33),
			qualifiedName( 28, 31),
			identifier( 28, 31));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method calls with multiple arguments.
	 * Validates parsing of argument lists with comma-separated expressions,
	 * essential for parameter passing.
	 */
	@Test
	public void testMethodCallWithArguments()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					foo(1, 2, 3);
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 47),
			typeDeclaration(CLASS_DECLARATION, 0, 46, "Test"),
			methodDeclaration( 14, 44),
			block( 24, 44),
			methodInvocation( 28, 40),
			qualifiedName( 28, 31),
			identifier( 28, 31),
			integerLiteral( 32, 33),
			integerLiteral( 35, 36),
			integerLiteral( 38, 39));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests field access using dot notation.
	 * Validates parsing of member access expressions (obj.field)
	 * for reading object properties.
	 */
	@Test
	public void testFieldAccess()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Object x = obj.field;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 55),
			typeDeclaration(CLASS_DECLARATION, 0, 54, "Test"),
			methodDeclaration( 14, 52),
			block( 24, 52),
			qualifiedName( 28, 34),
			fieldAccess( 39, 48),
			identifier( 39, 42));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests array element access using bracket notation.
	 * Validates parsing of subscript expressions (array[index])
	 * for accessing array elements by position.
	 */
	@Test
	public void testArrayAccess()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Object x = array[0];
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 54),
			typeDeclaration(CLASS_DECLARATION, 0, 53, "Test"),
			methodDeclaration( 14, 51),
			block( 24, 51),
			qualifiedName( 28, 34),
			arrayAccess( 39, 47),
			identifier( 39, 44),
			integerLiteral( 45, 46));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests simple assignment statement.
	 * Validates parsing of assignment operator (=) that stores values
	 * in variables, the fundamental state modification operation.
	 */
	@Test
	public void testAssignment()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					x = 5;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 40),
			typeDeclaration(CLASS_DECLARATION, 0, 39, "Test"),
			methodDeclaration( 14, 37),
			block( 24, 37),
			assignmentExpression( 28, 33),
			qualifiedName( 28, 29),
			identifier( 28, 29),
			integerLiteral( 32, 33));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests compound assignment operator (+=).
	 * Validates parsing of combined operation-and-assignment operators
	 * that modify variables in-place (x += 5 equivalent to x = x + 5).
	 */
	@Test
	public void testCompoundAssignment()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					x += 5;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 41),
			typeDeclaration(CLASS_DECLARATION, 0, 40, "Test"),
			methodDeclaration( 14, 38),
			block( 24, 38),
			assignmentExpression( 28, 34),
			qualifiedName( 28, 29),
			identifier( 28, 29),
			integerLiteral( 33, 34));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests ternary conditional operator (? :).
	 * Validates parsing of conditional expression that selects between two values
	 * based on boolean condition, providing expression-level branching.
	 */
	@Test
	public void testTernaryOperator()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Object result = x ? y : z;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 60),
			typeDeclaration(CLASS_DECLARATION, 0, 59, "Test"),
			methodDeclaration( 14, 57),
			block( 24, 57),
			qualifiedName( 28, 34),
			conditionalExpression( 44, 53),
			identifier( 44, 45),
			identifier( 48, 49),
			identifier( 52, 53));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests maximum allowed nesting depth boundary.
	 * Validates that parser accepts valid deeply nested expressions up to {@link SecurityConfig#MAX_PARSE_DEPTH},
	 * ensuring security constraints don't reject legitimate code.
	 */
	@Test
	public void testMaxDepthBoundary()
	{
		int nestedParentheses = SecurityConfig.MAX_PARSE_DEPTH - 1;
		StringBuilder source = new StringBuilder("""
			class Test
			{
				void m()
				{
					int x =""");
		source.append(' ');
		for (int i = 0; i < nestedParentheses; ++i)
			source.append('(');
		source.append('1');
		for (int i = 0; i < nestedParentheses; ++i)
			source.append(')');
		source.append("""
			;
				}
			}
			""");

		// Verify parsing succeeds without checking exact positions
		// due to dynamically generated deeply nested structure
		try (Parser parser = new Parser(source.toString()))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}

	/**
	 * Tests that excessive nesting depth is rejected with ParseResult.Failure.
	 * Validates denial-of-service prevention by enforcing maximum depth limit,
	 * protecting against stack overflow from pathological input.
	 */
	@Test
	public void testMaxDepthExceeded()
	{
		int nestedParentheses = SecurityConfig.MAX_PARSE_DEPTH + 1;
		StringBuilder source = new StringBuilder();
		for (int i = 0; i < nestedParentheses; ++i)
			source.append('(');
		source.append('1');
		for (int i = 0; i < nestedParentheses; ++i)
			source.append(')');

		try (Parser parser = new Parser(source.toString()))
		{
			switch (parser.parse())
			{
				case ParseResult.Success success ->
					throw new AssertionError("Expected Failure but got: " + success);
				case ParseResult.Failure _ ->
				{
					// Expected - depth limit exceeded
				}
			}
		}
	}
}
