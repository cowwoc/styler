package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			compilationUnit( 0, 134),
			typeDeclaration(CLASS_DECLARATION, 7, 133, "Test"),
			methodDeclaration( 21, 131),
			parameterNode( 36, 41, "x"),
			block( 44, 131),
			returnStatement( 48, 128),
			switchExpression( 55, 127),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			block( 86, 106),
			yieldStatement( 92, 101),
			integerLiteral( 98, 100),
			integerLiteral( 121, 122));
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
			compilationUnit( 0, 143),
			typeDeclaration(CLASS_DECLARATION, 7, 142, "Test"),
			methodDeclaration( 21, 140),
			parameterNode( 39, 44, "x"),
			block( 47, 140),
			returnStatement( 51, 137),
			switchExpression( 58, 136),
			identifier( 66, 67),
			integerLiteral( 81, 82),
			block( 89, 114),
			yieldStatement( 95, 109),
			stringLiteral( 101, 108),
			stringLiteral( 129, 131));
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
			compilationUnit( 0, 141),
			typeDeclaration(CLASS_DECLARATION, 7, 140, "Test"),
			methodDeclaration( 21, 138),
			parameterNode( 36, 41, "x"),
			block( 44, 138),
			returnStatement( 48, 135),
			switchExpression( 55, 134),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			block( 86, 113),
			yieldStatement( 92, 108),
			identifier( 98, 105),
			methodInvocation( 98, 107),
			integerLiteral( 128, 129));
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
			compilationUnit( 0, 158),
			typeDeclaration(CLASS_DECLARATION, 7, 157, "Test"),
			methodDeclaration( 21, 155),
			parameterNode( 36, 41, "x"),
			block( 44, 155),
			returnStatement( 48, 152),
			switchExpression( 55, 151),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			block( 86, 130),
			integerLiteral( 103, 104),
			yieldStatement( 110, 125),
			binaryExpression( 116, 124),
			identifier( 116, 120),
			integerLiteral( 123, 124),
			integerLiteral( 145, 146));
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
			compilationUnit( 0, 140),
			typeDeclaration(CLASS_DECLARATION, 7, 139, "Test"),
			methodDeclaration( 21, 137),
			parameterNode( 39, 44, "x"),
			block( 47, 137),
			returnStatement( 51, 134),
			switchExpression( 58, 133),
			identifier( 66, 67),
			integerLiteral( 81, 82),
			block( 89, 111),
			yieldStatement( 95, 106),
			nullLiteral( 101, 105),
			stringLiteral( 126, 128));
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
			compilationUnit( 0, 160),
			typeDeclaration(CLASS_DECLARATION, 7, 159, "Test"),
			methodDeclaration( 21, 157),
			parameterNode( 36, 41, "x"),
			parameterNode( 43, 55, "flag"),
			block( 58, 157),
			returnStatement( 62, 154),
			switchExpression( 69, 153),
			identifier( 77, 78),
			integerLiteral( 92, 93),
			block( 100, 132),
			yieldStatement( 106, 127),
			conditionalExpression( 112, 126),
			identifier( 112, 116),
			integerLiteral( 119, 122),
			integerLiteral( 125, 126),
			integerLiteral( 147, 148));
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
			compilationUnit( 0, 150),
			typeDeclaration(CLASS_DECLARATION, 7, 149, "Test"),
			methodDeclaration( 21, 147),
			parameterNode( 39, 44, "x"),
			block( 47, 147),
			returnStatement( 51, 144),
			switchExpression( 58, 143),
			identifier( 66, 67),
			integerLiteral( 81, 82),
			block( 89, 119),
			yieldStatement( 95, 114),
			qualifiedName( 105, 111),
			objectCreation( 101, 113),
			nullLiteral( 134, 138));
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
			compilationUnit( 0, 186),
			typeDeclaration(CLASS_DECLARATION, 7, 185, "Test"),
			methodDeclaration( 21, 183),
			parameterNode( 36, 41, "x"),
			block( 44, 183),
			returnStatement( 48, 180),
			switchExpression( 55, 179),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			block( 86, 158),
			integerLiteral( 100, 101),
			integerLiteral( 115, 116),
			binaryExpression( 132, 137),
			identifier( 132, 133),
			identifier( 136, 137),
			yieldStatement( 143, 153),
			identifier( 149, 152),
			integerLiteral( 173, 174));
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
			compilationUnit( 0, 199),
			typeDeclaration(CLASS_DECLARATION, 7, 198, "Test"),
			methodDeclaration( 21, 196),
			parameterNode( 36, 41, "x"),
			parameterNode( 43, 48, "y"),
			block( 51, 196),
			returnStatement( 55, 193),
			switchExpression( 62, 192),
			identifier( 70, 71),
			integerLiteral( 85, 86),
			switchExpression( 90, 170),
			identifier( 98, 99),
			integerLiteral( 115, 116),
			block( 124, 146),
			yieldStatement( 131, 140),
			integerLiteral( 137, 139),
			integerLiteral( 162, 164),
			integerLiteral( 186, 187));
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
			compilationUnit( 0, 140),
			typeDeclaration(CLASS_DECLARATION, 7, 139, "Test"),
			methodDeclaration( 21, 137),
			parameterNode( 36, 41, "x"),
			block( 44, 137),
			returnStatement( 48, 134),
			switchExpression( 55, 133),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			block( 84, 104),
			yieldStatement( 90, 99),
			integerLiteral( 96, 98),
			yieldStatement( 121, 129),
			integerLiteral( 127, 128));
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
			compilationUnit( 0, 160),
			typeDeclaration(CLASS_DECLARATION, 7, 159, "Test"),
			methodDeclaration( 21, 157),
			parameterNode( 36, 41, "x"),
			block( 44, 157),
			returnStatement( 48, 154),
			switchExpression( 55, 153),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			block( 86, 132),
			blockComment( 92, 105),
			yieldStatement( 106, 127),
			blockComment( 112, 123),
			integerLiteral( 124, 126),
			integerLiteral( 147, 148));
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
			compilationUnit( 0, 228),
			typeDeclaration(CLASS_DECLARATION, 7, 227, "Test"),
			methodDeclaration( 21, 225),
			parameterNode( 36, 41, "x"),
			block( 44, 225),
			returnStatement( 48, 222),
			switchExpression( 55, 221),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			block( 86, 106),
			yieldStatement( 92, 101),
			integerLiteral( 98, 100),
			integerLiteral( 115, 116),
			block( 123, 143),
			yieldStatement( 129, 138),
			integerLiteral( 135, 137),
			integerLiteral( 152, 153),
			block( 160, 180),
			yieldStatement( 166, 175),
			integerLiteral( 172, 174),
			block( 198, 217),
			yieldStatement( 204, 212),
			integerLiteral( 210, 211));
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
			compilationUnit( 0, 134),
			typeDeclaration(CLASS_DECLARATION, 7, 133, "Test"),
			methodDeclaration( 21, 131),
			parameterNode( 36, 41, "x"),
			block( 44, 131),
			returnStatement( 48, 128),
			switchExpression( 55, 127),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			integerLiteral( 83, 85),
			block( 104, 123),
			yieldStatement( 110, 118),
			integerLiteral( 116, 117));
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
			compilationUnit( 0, 141),
			typeDeclaration(CLASS_DECLARATION, 7, 140, "Test"),
			methodDeclaration( 21, 138),
			parameterNode( 37, 42, "x"),
			block( 45, 138),
			switchExpression( 62, 134),
			identifier( 70, 71),
			integerLiteral( 85, 86),
			block( 93, 113),
			yieldStatement( 99, 108),
			integerLiteral( 105, 107),
			integerLiteral( 128, 129));
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
			compilationUnit( 0, 179),
			typeDeclaration(CLASS_DECLARATION, 7, 178, "Test"),
			methodDeclaration( 21, 176),
			qualifiedName( 56, 63),
			parameterNode( 69, 74, "x"),
			block( 77, 176),
			returnStatement( 81, 173),
			switchExpression( 88, 172),
			identifier( 96, 97),
			integerLiteral( 111, 112),
			block( 119, 145),
			yieldStatement( 125, 140),
			lambdaExpression( 131, 139),
			integerLiteral( 137, 139),
			lambdaExpression( 160, 167),
			integerLiteral( 166, 167));
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
			compilationUnit( 0, 150),
			typeDeclaration(CLASS_DECLARATION, 7, 149, "Test"),
			methodDeclaration( 21, 147),
			parameterNode( 36, 45, "arr"),
			parameterNode( 47, 52, "x"),
			block( 55, 147),
			returnStatement( 59, 144),
			switchExpression( 66, 143),
			identifier( 74, 75),
			integerLiteral( 89, 90),
			block( 97, 121),
			yieldStatement( 103, 116),
			arrayAccess( 109, 115),
			identifier( 109, 112),
			integerLiteral( 113, 114),
			unaryExpression( 136, 138),
			integerLiteral( 137, 138));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
