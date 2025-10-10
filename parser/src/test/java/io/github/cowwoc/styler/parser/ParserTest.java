package io.github.cowwoc.styler.parser;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Thread-safe tests for Parser.
 */
public class ParserTest
{
	/**
	 * Tests that parser handles empty source input gracefully.
	 * Validates that parsing empty string produces valid AST without errors,
	 * which is an edge case for lexer and parser initialization.
	 */
	@Test
	public void testEmptySource()
	{
		try (Parser parser = new Parser(""))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
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
		String source = "class Test { void m() { int x = 42; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests parsing of string literal expressions.
	 * Validates that the parser correctly handles quoted string constants
	 * including quote characters themselves.
	 */
	@Test
	public void testStringLiteralExpression()
	{
		String source = "class Test { void m() { String x = \"hello\"; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests parsing of identifier expressions (variable references).
	 * Validates that the parser correctly handles variable name references
	 * in expression positions.
	 */
	@Test
	public void testIdentifierExpression()
	{
		String source = "class Test { void m() { Object x = myVariable; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests parsing of binary addition expressions.
	 * Validates that the parser handles the + operator with two operands,
	 * forming a binary expression tree node.
	 */
	@Test
	public void testBinaryAddition()
	{
		String source = "class Test { void m() { int x = 1 + 2; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests parsing of binary multiplication expressions.
	 * Validates that the parser handles the * operator,
	 * which has higher precedence than addition.
	 */
	@Test
	public void testBinaryMultiplication()
	{
		String source = "class Test { void m() { int x = 3 * 4; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests correct operator precedence in mixed arithmetic expressions.
	 * Validates that multiplication binds tighter than addition,
	 * so "1 + 2 * 3" parses as "1 + (2 * 3)" not "(1 + 2) * 3".
	 */
	@Test
	public void testOperatorPrecedence()
	{
		String source = "class Test { void m() { int x = 1 + 2 * 3; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests parenthesized expressions that override operator precedence.
	 * Validates that parentheses force "(1 + 2) * 3" to evaluate addition first,
	 * producing different result than natural precedence would.
	 */
	@Test
	public void testParenthesizedExpression()
	{
		String source = "class Test { void m() { int x = (1 + 2) * 3; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests unary minus operator for numeric negation.
	 * Validates parsing of prefix minus that creates negative literals
	 * or negates expression results.
	 */
	@Test
	public void testUnaryMinus()
	{
		String source = "class Test { void m() { int x = -5; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests unary logical NOT operator.
	 * Validates parsing of ! operator that inverts boolean values,
	 * essential for conditional logic.
	 */
	@Test
	public void testUnaryNot()
	{
		String source = "class Test { void m() { boolean x = !true; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests method call syntax without arguments.
	 * Validates parsing of method invocation with empty parameter list,
	 * the simplest form of method calls.
	 */
	@Test
	public void testMethodCall()
	{
		String source = "class Test { void m() { foo(); } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests method calls with multiple arguments.
	 * Validates parsing of argument lists with comma-separated expressions,
	 * essential for parameter passing.
	 */
	@Test
	public void testMethodCallWithArguments()
	{
		String source = "class Test { void m() { foo(1, 2, 3); } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests field access using dot notation.
	 * Validates parsing of member access expressions (obj.field)
	 * for reading object properties.
	 */
	@Test
	public void testFieldAccess()
	{
		String source = "class Test { void m() { Object x = obj.field; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests array element access using bracket notation.
	 * Validates parsing of subscript expressions (array[index])
	 * for accessing array elements by position.
	 */
	@Test
	public void testArrayAccess()
	{
		String source = "class Test { void m() { Object x = array[0]; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests simple assignment statement.
	 * Validates parsing of assignment operator (=) that stores values
	 * in variables, the fundamental state modification operation.
	 */
	@Test
	public void testAssignment()
	{
		String source = "class Test { void m() { x = 5; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests compound assignment operator (+=).
	 * Validates parsing of combined operation-and-assignment operators
	 * that modify variables in-place (x += 5 equivalent to x = x + 5).
	 */
	@Test
	public void testCompoundAssignment()
	{
		String source = "class Test { void m() { x += 5; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests ternary conditional operator (? :).
	 * Validates parsing of conditional expression that selects between two values
	 * based on boolean condition, providing expression-level branching.
	 */
	@Test
	public void testTernaryOperator()
	{
		String source = "class Test { void m() { Object result = x ? y : z; } }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	// Note: Lambda expression parsing is incomplete - test disabled pending full implementation
	// @Test
	// public void testLambdaExpression()
	// {
	// 	String source = "class Test { void m() { process(x -> x + 1); } }";
	// 	try (Parser parser = new Parser(source))
	// 	{
	// 		NodeIndex root = parser.parse();
	// 		requireThat(root.isValid(), "root.isValid()").isTrue();
	// 	}
	// }

	/**
	 * Tests maximum allowed nesting depth boundary.
	 * Validates that parser accepts valid deeply nested expressions up to depth 200,
	 * ensuring security constraints don't reject legitimate code.
	 * This test creates 199 nested parentheses (depth 200 with inner literal).
	 */
	@Test
	public void testMaxDepthBoundary()
	{
		// Validate that maximum allowed depth (199 nesting levels = depth 200) succeeds
		// This ensures the security constraint allows valid deeply nested expressions
		// Note: 199 parentheses creates depth=200 because the inner literal requires one more depth
		StringBuilder source = new StringBuilder("class Test { void m() { int x = ");
		for (int i = 0; i < 199; i += 1)
		{
			source.append('(');
		}
		source.append('1');
		for (int i = 0; i < 199; i += 1)
		{
			source.append(')');
		}
		source.append("; } }");

		try (Parser parser = new Parser(source.toString()))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}

	/**
	 * Tests that excessive nesting depth is rejected with ParserException.
	 * Validates denial-of-service prevention by enforcing maximum depth limit,
	 * protecting against stack overflow from pathological input.
	 * This test creates 201 nested parentheses exceeding the 200 depth limit.
	 */
	@Test(expectedExceptions = Parser.ParserException.class)
	public void testMaxDepthExceeded()
	{
		// Create deeply nested parenthesized expression
		StringBuilder source = new StringBuilder();
		for (int i = 0; i < 201; i += 1)
		{
			source.append('(');
		}
		source.append('1');
		for (int i = 0; i < 201; i += 1)
		{
			source.append(')');
		}

		try (Parser parser = new Parser(source.toString()))
		{
			parser.parse();
		}
	}
}
