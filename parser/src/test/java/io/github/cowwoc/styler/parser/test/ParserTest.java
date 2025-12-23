package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ARRAY_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.BOOLEAN_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONDITIONAL_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.UNARY_EXPRESSION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseSucceeds;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 0));
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
			semanticNode(COMPILATION_UNIT, 0, 45),
			semanticNode(CLASS_DECLARATION, 0, 44, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 42),
			semanticNode(BLOCK, 24, 42),
			semanticNode(INTEGER_LITERAL, 36, 38));
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
			semanticNode(COMPILATION_UNIT, 0, 53),
			semanticNode(CLASS_DECLARATION, 0, 52, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 50),
			semanticNode(BLOCK, 24, 50),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(STRING_LITERAL, 39, 46));
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
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(CLASS_DECLARATION, 0, 55, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 53),
			semanticNode(BLOCK, 24, 53),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(IDENTIFIER, 39, 49));
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
			semanticNode(COMPILATION_UNIT, 0, 48),
			semanticNode(CLASS_DECLARATION, 0, 47, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 45),
			semanticNode(BLOCK, 24, 45),
			semanticNode(BINARY_EXPRESSION, 36, 41),
			semanticNode(INTEGER_LITERAL, 36, 37),
			semanticNode(INTEGER_LITERAL, 40, 41));
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
			semanticNode(COMPILATION_UNIT, 0, 48),
			semanticNode(CLASS_DECLARATION, 0, 47, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 45),
			semanticNode(BLOCK, 24, 45),
			semanticNode(BINARY_EXPRESSION, 36, 41),
			semanticNode(INTEGER_LITERAL, 36, 37),
			semanticNode(INTEGER_LITERAL, 40, 41));
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
			semanticNode(COMPILATION_UNIT, 0, 52),
			semanticNode(CLASS_DECLARATION, 0, 51, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 49),
			semanticNode(BLOCK, 24, 49),
			semanticNode(BINARY_EXPRESSION, 36, 45),
			semanticNode(BINARY_EXPRESSION, 40, 45),
			semanticNode(INTEGER_LITERAL, 36, 37),
			semanticNode(INTEGER_LITERAL, 40, 41),
			semanticNode(INTEGER_LITERAL, 44, 45));
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
			semanticNode(COMPILATION_UNIT, 0, 54),
			semanticNode(CLASS_DECLARATION, 0, 53, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 51),
			semanticNode(BLOCK, 24, 51),
			semanticNode(BINARY_EXPRESSION, 37, 47),
			semanticNode(BINARY_EXPRESSION, 37, 42),
			semanticNode(INTEGER_LITERAL, 37, 38),
			semanticNode(INTEGER_LITERAL, 41, 42),
			semanticNode(INTEGER_LITERAL, 46, 47));
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
			semanticNode(COMPILATION_UNIT, 0, 45),
			semanticNode(CLASS_DECLARATION, 0, 44, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 42),
			semanticNode(BLOCK, 24, 42),
			semanticNode(UNARY_EXPRESSION, 36, 38),
			semanticNode(INTEGER_LITERAL, 37, 38));
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
			semanticNode(COMPILATION_UNIT, 0, 52),
			semanticNode(CLASS_DECLARATION, 0, 51, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 49),
			semanticNode(BLOCK, 24, 49),
			semanticNode(UNARY_EXPRESSION, 40, 45),
			semanticNode(BOOLEAN_LITERAL, 41, 45));
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
			semanticNode(COMPILATION_UNIT, 0, 40),
			semanticNode(CLASS_DECLARATION, 0, 39, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 37),
			semanticNode(BLOCK, 24, 37),
			semanticNode(METHOD_INVOCATION, 28, 33),
			semanticNode(QUALIFIED_NAME, 28, 31),
			semanticNode(IDENTIFIER, 28, 31));
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
			semanticNode(COMPILATION_UNIT, 0, 47),
			semanticNode(CLASS_DECLARATION, 0, 46, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 44),
			semanticNode(BLOCK, 24, 44),
			semanticNode(METHOD_INVOCATION, 28, 40),
			semanticNode(QUALIFIED_NAME, 28, 31),
			semanticNode(IDENTIFIER, 28, 31),
			semanticNode(INTEGER_LITERAL, 32, 33),
			semanticNode(INTEGER_LITERAL, 35, 36),
			semanticNode(INTEGER_LITERAL, 38, 39));
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
			semanticNode(COMPILATION_UNIT, 0, 55),
			semanticNode(CLASS_DECLARATION, 0, 54, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 52),
			semanticNode(BLOCK, 24, 52),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(FIELD_ACCESS, 39, 48),
			semanticNode(IDENTIFIER, 39, 42));
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
			semanticNode(COMPILATION_UNIT, 0, 54),
			semanticNode(CLASS_DECLARATION, 0, 53, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 51),
			semanticNode(BLOCK, 24, 51),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(ARRAY_ACCESS, 39, 47),
			semanticNode(IDENTIFIER, 39, 44),
			semanticNode(INTEGER_LITERAL, 45, 46));
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
			semanticNode(COMPILATION_UNIT, 0, 40),
			semanticNode(CLASS_DECLARATION, 0, 39, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 37),
			semanticNode(BLOCK, 24, 37),
			semanticNode(ASSIGNMENT_EXPRESSION, 28, 33),
			semanticNode(QUALIFIED_NAME, 28, 29),
			semanticNode(IDENTIFIER, 28, 29),
			semanticNode(INTEGER_LITERAL, 32, 33));
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
			semanticNode(COMPILATION_UNIT, 0, 41),
			semanticNode(CLASS_DECLARATION, 0, 40, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 38),
			semanticNode(BLOCK, 24, 38),
			semanticNode(ASSIGNMENT_EXPRESSION, 28, 34),
			semanticNode(QUALIFIED_NAME, 28, 29),
			semanticNode(IDENTIFIER, 28, 29),
			semanticNode(INTEGER_LITERAL, 33, 34));
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
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(CLASS_DECLARATION, 0, 59, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 57),
			semanticNode(BLOCK, 24, 57),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(CONDITIONAL_EXPRESSION, 44, 53),
			semanticNode(IDENTIFIER, 44, 45),
			semanticNode(IDENTIFIER, 48, 49),
			semanticNode(IDENTIFIER, 52, 53));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests maximum allowed nesting depth boundary.
	 * Validates that parser accepts valid deeply nested expressions up to depth 200,
	 * ensuring security constraints don't reject legitimate code.
	 * This test creates 199 nested parentheses (depth 200 with inner literal).
	 */
	@Test
	public void testMaxDepthBoundary()
	{
		StringBuilder source = new StringBuilder("""
			class Test
			{
				void m()
				{
					int x =""");
		source.append(' ');
		for (int i = 0; i < 199; ++i)
			source.append('(');
		source.append('1');
		for (int i = 0; i < 199; ++i)
			source.append(')');
		source.append("""
			;
				}
			}
			""");

		// Verify parsing succeeds without checking exact positions
		// due to dynamically generated deeply nested structure
		assertParseSucceeds(source.toString());
	}

	/**
	 * Tests that excessive nesting depth is rejected with ParseResult.Failure.
	 * Validates denial-of-service prevention by enforcing maximum depth limit,
	 * protecting against stack overflow from pathological input.
	 * This test creates 201 nested parentheses exceeding the 200 depth limit.
	 */
	@Test
	public void testMaxDepthExceeded()
	{
		StringBuilder source = new StringBuilder();
		for (int i = 0; i < 201; ++i)
			source.append('(');
		source.append('1');
		for (int i = 0; i < 201; ++i)
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
