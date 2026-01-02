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
 * Tests for parsing switch expressions with arrow syntax.
 */
public class SwitchExpressionParserTest
{
	/**
	 * Validates parsing of multi-label case with arrow syntax in switch expression.
	 * Tests enhanced switch with multiple case labels using arrow operator for expressions.
	 */
	@Test
	public void testMultiLabelCaseWithArrow()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public boolean foo(char ch)
				{
					return switch (ch)
					{
						case 'a', 'b', 'c' -> true;
						default -> false;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 139),
			typeDeclaration(CLASS_DECLARATION, 7, 138, "Test"),
			methodDeclaration( 21, 136),
			parameterNode( 40, 47, "ch"),
			block( 50, 136),
			returnStatement( 54, 133),
			switchExpression( 61, 132),
			identifier( 69, 71),
			charLiteral( 85, 88),
			charLiteral( 90, 93),
			charLiteral( 95, 98),
			booleanLiteral( 102, 106),
			booleanLiteral( 122, 127));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of switch expression with single case label and arrow syntax.
	 * Tests basic switch expression form with one case per branch.
	 */
	@Test
	public void testSingleCaseWithArrow()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 -> 10;
						case 2 -> 20;
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 131),
			typeDeclaration(CLASS_DECLARATION, 7, 130, "Test"),
			methodDeclaration( 21, 128),
			parameterNode( 36, 41, "x"),
			block( 44, 128),
			returnStatement( 48, 125),
			switchExpression( 55, 124),
			identifier( 63, 64),
			integerLiteral( 78, 79),
			integerLiteral( 83, 85),
			integerLiteral( 95, 96),
			integerLiteral( 100, 102),
			integerLiteral( 118, 119));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of switch expression returning string values.
	 * Tests switch expression with string literal results.
	 */
	@Test
	public void testSwitchExpressionReturningStrings()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public String foo(int x)
				{
					return switch (x)
					{
						case 1 -> "one";
						case 2 -> "two";
						default -> "other";
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 146),
			typeDeclaration(CLASS_DECLARATION, 7, 145, "Test"),
			methodDeclaration( 21, 143),
			parameterNode( 39, 44, "x"),
			block( 47, 143),
			returnStatement( 51, 140),
			switchExpression( 58, 139),
			identifier( 66, 67),
			integerLiteral( 81, 82),
			stringLiteral( 86, 91),
			integerLiteral( 101, 102),
			stringLiteral( 106, 111),
			stringLiteral( 127, 134));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of switch expression with only default case.
	 * Tests minimal switch expression that always returns the default.
	 */
	@Test
	public void testSwitchExpressionDefaultOnly()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 97),
			typeDeclaration(CLASS_DECLARATION, 7, 96, "Test"),
			methodDeclaration( 21, 94),
			parameterNode( 36, 41, "x"),
			block( 44, 94),
			returnStatement( 48, 91),
			switchExpression( 55, 90),
			identifier( 63, 64),
			integerLiteral( 84, 85));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of switch expression using boolean selector.
	 * Tests switch expression over boolean type values.
	 */
	@Test
	public void testSwitchExpressionWithBooleanSelector()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public String foo(boolean b)
				{
					return switch (b)
					{
						case true -> "yes";
						case false -> "no";
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 133),
			typeDeclaration(CLASS_DECLARATION, 7, 132, "Test"),
			methodDeclaration( 21, 130),
			parameterNode( 39, 48, "b"),
			block( 51, 130),
			returnStatement( 55, 127),
			switchExpression( 62, 126),
			identifier( 70, 71),
			booleanLiteral( 85, 89),
			stringLiteral( 93, 98),
			booleanLiteral( 108, 113),
			stringLiteral( 117, 121));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of nested switch expressions.
	 * Tests a switch expression containing another switch expression as a result value.
	 */
	@Test
	public void testNestedSwitchExpression()
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
							case 1 -> 11;
							default -> 10;
						};
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 176),
			typeDeclaration(CLASS_DECLARATION, 7, 175, "Test"),
			methodDeclaration( 21, 173),
			parameterNode( 36, 41, "x"),
			parameterNode( 43, 48, "y"),
			block( 51, 173),
			returnStatement( 55, 170),
			switchExpression( 62, 169),
			identifier( 70, 71),
			integerLiteral( 85, 86),
			switchExpression( 90, 147),
			identifier( 98, 99),
			integerLiteral( 115, 116),
			integerLiteral( 120, 122),
			integerLiteral( 139, 141),
			integerLiteral( 163, 164));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of switch expression in variable assignment.
	 * Tests switch expression used to initialize a variable.
	 */
	@Test
	public void testSwitchExpressionInVariableAssignment()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo(int x)
				{
					int result = switch (x)
					{
						case 1 -> 100;
						default -> 0;
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 122),
			typeDeclaration(CLASS_DECLARATION, 7, 121, "Test"),
			methodDeclaration( 21, 119),
			parameterNode( 37, 42, "x"),
			block( 45, 119),
			switchExpression( 62, 115),
			identifier( 70, 71),
			integerLiteral( 85, 86),
			integerLiteral( 90, 93),
			integerLiteral( 109, 110));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
