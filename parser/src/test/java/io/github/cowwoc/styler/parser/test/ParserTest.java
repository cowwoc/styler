package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.SecurityConfig;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		String source = "";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 0);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					int x = 42;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 36, 38);
			expected.allocateNode(NodeType.BLOCK, 24, 42);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 42);
			expected.allocateClassDeclaration(0, 44, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					String x = "hello";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.STRING_LITERAL, 39, 46);
			expected.allocateNode(NodeType.BLOCK, 24, 50);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 50);
			expected.allocateClassDeclaration(0, 52, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 53);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					Object x = myVariable;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 49);
			expected.allocateNode(NodeType.BLOCK, 24, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					int x = 1 + 2;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 36, 37);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 40, 41);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 36, 41);
			expected.allocateNode(NodeType.BLOCK, 24, 45);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 45);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					int x = 3 * 4;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 36, 37);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 40, 41);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 36, 41);
			expected.allocateNode(NodeType.BLOCK, 24, 45);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 45);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					int x = 1 + 2 * 3;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 36, 37);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 40, 41);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 44, 45);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 40, 45);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 36, 45);
			expected.allocateNode(NodeType.BLOCK, 24, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 49);
			expected.allocateClassDeclaration(0, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					int x = (1 + 2) * 3;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 37, 38);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 41, 42);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 37, 42);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 46, 47);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 37, 47);
			expected.allocateNode(NodeType.BLOCK, 24, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 51);
			expected.allocateClassDeclaration(0, 53, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					int x = -5;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 37, 38);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 36, 38);
			expected.allocateNode(NodeType.BLOCK, 24, 42);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 42);
			expected.allocateClassDeclaration(0, 44, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					boolean x = !true;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 41, 45);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 40, 45);
			expected.allocateNode(NodeType.BLOCK, 24, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 49);
			expected.allocateClassDeclaration(0, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					foo();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 31);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 31);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 33);
			expected.allocateNode(NodeType.BLOCK, 24, 37);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 37);
			expected.allocateClassDeclaration(0, 39, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					foo(1, 2, 3);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 31);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 31);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 32, 33);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 35, 36);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 38, 39);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 40);
			expected.allocateNode(NodeType.BLOCK, 24, 44);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 44);
			expected.allocateClassDeclaration(0, 46, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					Object x = obj.field;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 42);
			expected.allocateNode(NodeType.FIELD_ACCESS, 39, 48);
			expected.allocateNode(NodeType.BLOCK, 24, 52);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 52);
			expected.allocateClassDeclaration(0, 54, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					Object x = array[0];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 44);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 45, 46);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 39, 47);
			expected.allocateNode(NodeType.BLOCK, 24, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 51);
			expected.allocateClassDeclaration(0, 53, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					x = 5;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 29);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 29);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 32, 33);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 28, 33);
			expected.allocateNode(NodeType.BLOCK, 24, 37);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 37);
			expected.allocateClassDeclaration(0, 39, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					x += 5;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 29);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 29);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 33, 34);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 28, 34);
			expected.allocateNode(NodeType.BLOCK, 24, 38);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 38);
			expected.allocateClassDeclaration(0, 40, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 41);
			requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void m()
				{
					Object result = x ? y : z;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 44, 45);
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.IDENTIFIER, 52, 53);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 44, 53);
			expected.allocateNode(NodeType.BLOCK, 24, 57);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests maximum allowed nesting depth boundary.
	 * Validates that parser accepts valid deeply nested expressions up to {@link SecurityConfig#MAX_NODE_DEPTH},
	 * ensuring security constraints don't reject legitimate code.
	 */
	@Test
	public void testMaxDepthBoundary()
	{
		int nestedParentheses = SecurityConfig.MAX_NODE_DEPTH - 1;
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
		int nestedParentheses = SecurityConfig.MAX_NODE_DEPTH + 1;
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
