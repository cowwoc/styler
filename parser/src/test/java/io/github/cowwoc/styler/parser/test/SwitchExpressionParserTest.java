package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.BOOLEAN_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.CHAR_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.SWITCH_EXPRESSION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 139),
			semanticNode(CLASS_DECLARATION, 7, 138, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 136),
			semanticNode(BLOCK, 50, 136),
			semanticNode(RETURN_STATEMENT, 54, 133),
			semanticNode(SWITCH_EXPRESSION, 61, 132),
			semanticNode(IDENTIFIER, 69, 71),
			semanticNode(CHAR_LITERAL, 85, 88),
			semanticNode(CHAR_LITERAL, 90, 93),
			semanticNode(CHAR_LITERAL, 95, 98),
			semanticNode(BOOLEAN_LITERAL, 102, 106),
			semanticNode(BOOLEAN_LITERAL, 122, 127));
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
			semanticNode(COMPILATION_UNIT, 0, 131),
			semanticNode(CLASS_DECLARATION, 7, 130, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 128),
			semanticNode(BLOCK, 44, 128),
			semanticNode(RETURN_STATEMENT, 48, 125),
			semanticNode(SWITCH_EXPRESSION, 55, 124),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(INTEGER_LITERAL, 83, 85),
			semanticNode(INTEGER_LITERAL, 95, 96),
			semanticNode(INTEGER_LITERAL, 100, 102),
			semanticNode(INTEGER_LITERAL, 118, 119));
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
			semanticNode(COMPILATION_UNIT, 0, 146),
			semanticNode(CLASS_DECLARATION, 7, 145, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 143),
			semanticNode(BLOCK, 47, 143),
			semanticNode(RETURN_STATEMENT, 51, 140),
			semanticNode(SWITCH_EXPRESSION, 58, 139),
			semanticNode(IDENTIFIER, 66, 67),
			semanticNode(INTEGER_LITERAL, 81, 82),
			semanticNode(STRING_LITERAL, 86, 91),
			semanticNode(INTEGER_LITERAL, 101, 102),
			semanticNode(STRING_LITERAL, 106, 111),
			semanticNode(STRING_LITERAL, 127, 134));
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
			semanticNode(COMPILATION_UNIT, 0, 97),
			semanticNode(CLASS_DECLARATION, 7, 96, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 94),
			semanticNode(BLOCK, 44, 94),
			semanticNode(RETURN_STATEMENT, 48, 91),
			semanticNode(SWITCH_EXPRESSION, 55, 90),
			semanticNode(IDENTIFIER, 63, 64),
			semanticNode(INTEGER_LITERAL, 84, 85));
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
			semanticNode(COMPILATION_UNIT, 0, 133),
			semanticNode(CLASS_DECLARATION, 7, 132, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 130),
			semanticNode(BLOCK, 51, 130),
			semanticNode(RETURN_STATEMENT, 55, 127),
			semanticNode(SWITCH_EXPRESSION, 62, 126),
			semanticNode(IDENTIFIER, 70, 71),
			semanticNode(BOOLEAN_LITERAL, 85, 89),
			semanticNode(STRING_LITERAL, 93, 98),
			semanticNode(BOOLEAN_LITERAL, 108, 113),
			semanticNode(STRING_LITERAL, 117, 121));
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
			semanticNode(COMPILATION_UNIT, 0, 176),
			semanticNode(CLASS_DECLARATION, 7, 175, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 173),
			semanticNode(BLOCK, 51, 173),
			semanticNode(RETURN_STATEMENT, 55, 170),
			semanticNode(SWITCH_EXPRESSION, 62, 169),
			semanticNode(IDENTIFIER, 70, 71),
			semanticNode(INTEGER_LITERAL, 85, 86),
			semanticNode(SWITCH_EXPRESSION, 90, 147),
			semanticNode(IDENTIFIER, 98, 99),
			semanticNode(INTEGER_LITERAL, 115, 116),
			semanticNode(INTEGER_LITERAL, 120, 122),
			semanticNode(INTEGER_LITERAL, 139, 141),
			semanticNode(INTEGER_LITERAL, 163, 164));
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
			semanticNode(COMPILATION_UNIT, 0, 122),
			semanticNode(CLASS_DECLARATION, 7, 121, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 119),
			semanticNode(BLOCK, 45, 119),
			semanticNode(SWITCH_EXPRESSION, 62, 115),
			semanticNode(IDENTIFIER, 70, 71),
			semanticNode(INTEGER_LITERAL, 85, 86),
			semanticNode(INTEGER_LITERAL, 90, 93),
			semanticNode(INTEGER_LITERAL, 109, 110));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
