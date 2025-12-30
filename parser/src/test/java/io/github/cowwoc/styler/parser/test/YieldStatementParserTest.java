package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ARRAY_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK_COMMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONDITIONAL_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.LAMBDA_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.NULL_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.SWITCH_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.UNARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.YIELD_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing yield statements in switch expressions.
 */
public final class YieldStatementParserTest
{
	/**
	 * Validates parsing of yield statement with integer literal.
	 */
	@Test
	public void testSimpleYieldWithIntegerLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield 42;
						}
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 134),
			semanticNode(CLASS_DECLARATION, 7, 133, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 131),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(BLOCK, 44, 131),
			semanticNode(RETURN_STATEMENT, 48, 128),
			semanticNode(SWITCH_EXPRESSION, 55, 127),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(BLOCK, 86, 106),
			semanticNode(YIELD_STATEMENT, 92, 101),
			semanticNode(INTEGER_LITERAL, 98, 100),
			semanticNode(INTEGER_LITERAL, 121, 122));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield statement with string literal.
	 */
	@Test
	public void testYieldWithStringLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public String foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield "hello";
						}
						default -> "";
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 143),
			semanticNode(CLASS_DECLARATION, 7, 142, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 140),
			semanticNode(PARAMETER_DECLARATION, 39, 44, "x"),
			semanticNode(BLOCK, 47, 140),
			semanticNode(RETURN_STATEMENT, 51, 137),
			semanticNode(SWITCH_EXPRESSION, 58, 136),
			semanticNode(IDENTIFIER, 66, 67),
			semanticNode(INTEGER_LITERAL, 81, 82),
			semanticNode(BLOCK, 89, 114),
			semanticNode(YIELD_STATEMENT, 95, 109),
			semanticNode(STRING_LITERAL, 101, 108),
			semanticNode(STRING_LITERAL, 129, 131));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield with method invocation.
	 */
	@Test
	public void testYieldWithMethodInvocation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield compute();
						}
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 141),
			semanticNode(CLASS_DECLARATION, 7, 140, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 138),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(BLOCK, 44, 138),
			semanticNode(RETURN_STATEMENT, 48, 135),
			semanticNode(SWITCH_EXPRESSION, 55, 134),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(BLOCK, 86, 113),
			semanticNode(YIELD_STATEMENT, 92, 108),
			semanticNode(IDENTIFIER, 98, 105),
			semanticNode(METHOD_INVOCATION, 98, 107),
			semanticNode(INTEGER_LITERAL, 128, 129));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield with binary expression.
	 */
	@Test
	public void testYieldWithBinaryExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							int temp = 5;
							yield temp * 2;
						}
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 158),
			semanticNode(CLASS_DECLARATION, 7, 157, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 155),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(BLOCK, 44, 155),
			semanticNode(RETURN_STATEMENT, 48, 152),
			semanticNode(SWITCH_EXPRESSION, 55, 151),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(BLOCK, 86, 130),
			semanticNode(INTEGER_LITERAL, 103, 104),
			semanticNode(YIELD_STATEMENT, 110, 125),
			semanticNode(BINARY_EXPRESSION, 116, 124),
			semanticNode(IDENTIFIER, 116, 120),
			semanticNode(INTEGER_LITERAL, 123, 124),
			semanticNode(INTEGER_LITERAL, 145, 146));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield with null literal.
	 */
	@Test
	public void testYieldWithNullLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public Object foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield null;
						}
						default -> "";
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 140),
			semanticNode(CLASS_DECLARATION, 7, 139, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 137),
			semanticNode(PARAMETER_DECLARATION, 39, 44, "x"),
			semanticNode(BLOCK, 47, 137),
			semanticNode(RETURN_STATEMENT, 51, 134),
			semanticNode(SWITCH_EXPRESSION, 58, 133),
			semanticNode(IDENTIFIER, 66, 67),
			semanticNode(INTEGER_LITERAL, 81, 82),
			semanticNode(BLOCK, 89, 111),
			semanticNode(YIELD_STATEMENT, 95, 106),
			semanticNode(NULL_LITERAL, 101, 105),
			semanticNode(STRING_LITERAL, 126, 128));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield with ternary conditional expression.
	 */
	@Test
	public void testYieldWithTernaryExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x, boolean flag)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield flag ? 100 : 0;
						}
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 160),
			semanticNode(CLASS_DECLARATION, 7, 159, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 157),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(PARAMETER_DECLARATION, 43, 55, "flag"),
			semanticNode(BLOCK, 58, 157),
			semanticNode(RETURN_STATEMENT, 62, 154),
			semanticNode(SWITCH_EXPRESSION, 69, 153),
			semanticNode(IDENTIFIER, 77, 78),
			semanticNode(INTEGER_LITERAL, 92, 93),
			semanticNode(BLOCK, 100, 132),
			semanticNode(YIELD_STATEMENT, 106, 127),
			semanticNode(CONDITIONAL_EXPRESSION, 112, 126),
			semanticNode(IDENTIFIER, 112, 116),
			semanticNode(INTEGER_LITERAL, 119, 122),
			semanticNode(INTEGER_LITERAL, 125, 126),
			semanticNode(INTEGER_LITERAL, 147, 148));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield with object creation.
	 */
	@Test
	public void testYieldWithObjectCreation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public Object foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield new Result();
						}
						default -> null;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 150),
			semanticNode(CLASS_DECLARATION, 7, 149, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 147),
			semanticNode(PARAMETER_DECLARATION, 39, 44, "x"),
			semanticNode(BLOCK, 47, 147),
			semanticNode(RETURN_STATEMENT, 51, 144),
			semanticNode(SWITCH_EXPRESSION, 58, 143),
			semanticNode(IDENTIFIER, 66, 67),
			semanticNode(INTEGER_LITERAL, 81, 82),
			semanticNode(BLOCK, 89, 119),
			semanticNode(YIELD_STATEMENT, 95, 114),
			semanticNode(QUALIFIED_NAME, 105, 111),
			semanticNode(OBJECT_CREATION, 101, 113),
			semanticNode(NULL_LITERAL, 134, 138));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield after multiple statements in block.
	 */
	@Test
	public void testYieldAfterMultipleStatements()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							int a = 1;
							int b = 2;
							int sum = a + b;
							yield sum;
						}
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 186),
			semanticNode(CLASS_DECLARATION, 7, 185, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 183),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(BLOCK, 44, 183),
			semanticNode(RETURN_STATEMENT, 48, 180),
			semanticNode(SWITCH_EXPRESSION, 55, 179),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(BLOCK, 86, 158),
			semanticNode(INTEGER_LITERAL, 100, 101),
			semanticNode(INTEGER_LITERAL, 115, 116),
			semanticNode(BINARY_EXPRESSION, 132, 137),
			semanticNode(IDENTIFIER, 132, 133),
			semanticNode(IDENTIFIER, 136, 137),
			semanticNode(YIELD_STATEMENT, 143, 153),
			semanticNode(IDENTIFIER, 149, 152),
			semanticNode(INTEGER_LITERAL, 173, 174));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of nested switch expression with yield.
	 */
	@Test
	public void testNestedSwitchExpressionWithYield()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x, int y)
				{
					return switch (x)
					{
						case 1 -> switch (y)
						{
							case 1 ->
							{
								yield 11;
							}
							default -> 10;
						};
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 199),
			semanticNode(CLASS_DECLARATION, 7, 198, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 196),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(PARAMETER_DECLARATION, 43, 48, "y"),
			semanticNode(BLOCK, 51, 196),
			semanticNode(RETURN_STATEMENT, 55, 193),
			semanticNode(SWITCH_EXPRESSION, 62, 192),
			semanticNode(IDENTIFIER, 70, 71),
			semanticNode(INTEGER_LITERAL, 85, 86),
			semanticNode(SWITCH_EXPRESSION, 90, 170),
			semanticNode(IDENTIFIER, 98, 99),
			semanticNode(INTEGER_LITERAL, 115, 116),
			semanticNode(BLOCK, 124, 146),
			semanticNode(YIELD_STATEMENT, 131, 140),
			semanticNode(INTEGER_LITERAL, 137, 139),
			semanticNode(INTEGER_LITERAL, 162, 164),
			semanticNode(INTEGER_LITERAL, 186, 187));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield in colon-style switch block.
	 */
	@Test
	public void testYieldInColonStyleSwitchBlock()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1:
						{
							yield 42;
						}
						default:
							yield 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 140),
			semanticNode(CLASS_DECLARATION, 7, 139, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 137),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(BLOCK, 44, 137),
			semanticNode(RETURN_STATEMENT, 48, 134),
			semanticNode(SWITCH_EXPRESSION, 55, 133),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(BLOCK, 84, 104),
			semanticNode(YIELD_STATEMENT, 90, 99),
			semanticNode(INTEGER_LITERAL, 96, 98),
			semanticNode(YIELD_STATEMENT, 121, 129),
			semanticNode(INTEGER_LITERAL, 127, 128));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield with comments.
	 */
	@Test
	public void testYieldWithComment()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							/* comment */ yield /* value */ 42;
						}
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 160),
			semanticNode(CLASS_DECLARATION, 7, 159, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 157),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(BLOCK, 44, 157),
			semanticNode(RETURN_STATEMENT, 48, 154),
			semanticNode(SWITCH_EXPRESSION, 55, 153),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(BLOCK, 86, 132),
			semanticNode(BLOCK_COMMENT, 92, 105),
			semanticNode(YIELD_STATEMENT, 106, 127),
			semanticNode(BLOCK_COMMENT, 112, 123),
			semanticNode(INTEGER_LITERAL, 124, 126),
			semanticNode(INTEGER_LITERAL, 147, 148));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multiple cases with yield.
	 */
	@Test
	public void testMultipleCasesWithYield()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield 10;
						}
						case 2 ->
						{
							yield 20;
						}
						case 3 ->
						{
							yield 30;
						}
						default ->
						{
							yield 0;
						}
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 228),
			semanticNode(CLASS_DECLARATION, 7, 227, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 225),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(BLOCK, 44, 225),
			semanticNode(RETURN_STATEMENT, 48, 222),
			semanticNode(SWITCH_EXPRESSION, 55, 221),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(BLOCK, 86, 106),
			semanticNode(YIELD_STATEMENT, 92, 101),
			semanticNode(INTEGER_LITERAL, 98, 100),
			semanticNode(INTEGER_LITERAL, 115, 116),
			semanticNode(BLOCK, 123, 143),
			semanticNode(YIELD_STATEMENT, 129, 138),
			semanticNode(INTEGER_LITERAL, 135, 137),
			semanticNode(INTEGER_LITERAL, 152, 153),
			semanticNode(BLOCK, 160, 180),
			semanticNode(YIELD_STATEMENT, 166, 175),
			semanticNode(INTEGER_LITERAL, 172, 174),
			semanticNode(BLOCK, 198, 217),
			semanticNode(YIELD_STATEMENT, 204, 212),
			semanticNode(INTEGER_LITERAL, 210, 211));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield in default case.
	 */
	@Test
	public void testYieldInDefaultCase()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 -> 42;
						default ->
						{
							yield 0;
						}
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 134),
			semanticNode(CLASS_DECLARATION, 7, 133, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 131),
			semanticNode(PARAMETER_DECLARATION, 36, 41, "x"),
			semanticNode(BLOCK, 44, 131),
			semanticNode(RETURN_STATEMENT, 48, 128),
			semanticNode(SWITCH_EXPRESSION, 55, 127),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(INTEGER_LITERAL, 83, 85),
			semanticNode(BLOCK, 104, 123),
			semanticNode(YIELD_STATEMENT, 110, 118),
			semanticNode(INTEGER_LITERAL, 116, 117));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield in variable assignment context.
	 */
	@Test
	public void testYieldInVariableAssignment()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo(int x)
				{
					int result = switch (x)
					{
						case 1 ->
						{
							yield 42;
						}
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 141),
			semanticNode(CLASS_DECLARATION, 7, 140, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 138),
			semanticNode(PARAMETER_DECLARATION, 37, 42, "x"),
			semanticNode(BLOCK, 45, 138),
			semanticNode(SWITCH_EXPRESSION, 62, 134),
			semanticNode(IDENTIFIER, 70, 71),
			semanticNode(INTEGER_LITERAL, 85, 86),
			semanticNode(BLOCK, 93, 113),
			semanticNode(YIELD_STATEMENT, 99, 108),
			semanticNode(INTEGER_LITERAL, 105, 107),
			semanticNode(INTEGER_LITERAL, 128, 129));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield returning a lambda expression.
	 */
	@Test
	public void testYieldWithLambdaExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public java.util.function.Supplier<Integer> foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield () -> 42;
						}
						default -> () -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 179),
			semanticNode(CLASS_DECLARATION, 7, 178, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 176),
			semanticNode(QUALIFIED_NAME, 56, 63),
			semanticNode(PARAMETER_DECLARATION, 69, 74, "x"),
			semanticNode(BLOCK, 77, 176),
			semanticNode(RETURN_STATEMENT, 81, 173),
			semanticNode(SWITCH_EXPRESSION, 88, 172),
			semanticNode(IDENTIFIER, 96, 97),
			semanticNode(INTEGER_LITERAL, 111, 112),
			semanticNode(BLOCK, 119, 145),
			semanticNode(YIELD_STATEMENT, 125, 140),
			semanticNode(LAMBDA_EXPRESSION, 131, 139),
			semanticNode(INTEGER_LITERAL, 137, 139),
			semanticNode(LAMBDA_EXPRESSION, 160, 167),
			semanticNode(INTEGER_LITERAL, 166, 167));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of yield with array access expression.
	 */
	@Test
	public void testYieldWithArrayAccess()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int[] arr, int x)
				{
					return switch (x)
					{
						case 0 ->
						{
							yield arr[0];
						}
						default -> -1;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 150),
			semanticNode(CLASS_DECLARATION, 7, 149, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 147),
			semanticNode(PARAMETER_DECLARATION, 36, 45, "arr"),
			semanticNode(PARAMETER_DECLARATION, 47, 52, "x"),
			semanticNode(BLOCK, 55, 147),
			semanticNode(RETURN_STATEMENT, 59, 144),
			semanticNode(SWITCH_EXPRESSION, 66, 143),
			semanticNode(IDENTIFIER, 74, 75),
			semanticNode(INTEGER_LITERAL, 89, 90),
			semanticNode(BLOCK, 97, 121),
			semanticNode(YIELD_STATEMENT, 103, 116),
			semanticNode(ARRAY_ACCESS, 109, 115),
			semanticNode(IDENTIFIER, 109, 112),
			semanticNode(INTEGER_LITERAL, 113, 114),
			semanticNode(UNARY_EXPRESSION, 136, 138),
			semanticNode(INTEGER_LITERAL, 137, 138));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
