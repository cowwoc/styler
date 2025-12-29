package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CATCH_CLAUSE;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.TRY_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.UNION_TYPE;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing multi-catch (union type) exception handling (JDK 7+).
 */
public final class MultiCatchParserTest
{
	/**
	 * Validates parsing of basic two-exception union type in catch clause.
	 * Tests the simplest multi-catch form: {@code catch (IOException | SQLException e)}.
	 */
	@Test
	public void shouldParseTwoExceptionUnionType()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (IOException | SQLException e)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 108),
			semanticNode(CLASS_DECLARATION, 7, 107, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 105),
			semanticNode(BLOCK, 40, 105),
			semanticNode(TRY_STATEMENT, 44, 102),
			semanticNode(BLOCK, 50, 55),
			semanticNode(CATCH_CLAUSE, 58, 102),
			semanticNode(UNION_TYPE, 65, 91),
			semanticNode(QUALIFIED_NAME, 65, 76),
			semanticNode(QUALIFIED_NAME, 79, 91),
			semanticNode(PARAMETER_DECLARATION, 65, 93, "e"),
			semanticNode(BLOCK, 97, 102));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of three-exception union type in catch clause.
	 * Tests common pattern with three exception types joined by {@code |} operators.
	 */
	@Test
	public void shouldParseThreeExceptionUnionType()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (IOException | SQLException | TimeoutException e)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 127),
			semanticNode(CLASS_DECLARATION, 7, 126, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 124),
			semanticNode(BLOCK, 40, 124),
			semanticNode(TRY_STATEMENT, 44, 121),
			semanticNode(BLOCK, 50, 55),
			semanticNode(CATCH_CLAUSE, 58, 121),
			semanticNode(UNION_TYPE, 65, 110),
			semanticNode(QUALIFIED_NAME, 65, 76),
			semanticNode(QUALIFIED_NAME, 79, 91),
			semanticNode(QUALIFIED_NAME, 94, 110),
			semanticNode(PARAMETER_DECLARATION, 65, 112, "e"),
			semanticNode(BLOCK, 116, 121));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that simple catch clause without union type still works correctly.
	 * Regression test ensuring multi-catch support does not break single-exception catch.
	 */
	@Test
	public void shouldContinueToParseSimpleCatch()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
						x = 1;
					}
					catch (Exception e)
					{
						x = 0;
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 111),
			semanticNode(CLASS_DECLARATION, 7, 110, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 108),
			semanticNode(BLOCK, 40, 108),
			semanticNode(TRY_STATEMENT, 44, 105),
			semanticNode(BLOCK, 50, 65),
			semanticNode(QUALIFIED_NAME, 55, 56),
			semanticNode(IDENTIFIER, 55, 56),
			semanticNode(INTEGER_LITERAL, 59, 60),
			semanticNode(ASSIGNMENT_EXPRESSION, 55, 60),
			semanticNode(CATCH_CLAUSE, 68, 105),
			semanticNode(QUALIFIED_NAME, 75, 84),
			semanticNode(PARAMETER_DECLARATION, 75, 86, "e"),
			semanticNode(BLOCK, 90, 105),
			semanticNode(QUALIFIED_NAME, 95, 96),
			semanticNode(IDENTIFIER, 95, 96),
			semanticNode(INTEGER_LITERAL, 99, 100),
			semanticNode(ASSIGNMENT_EXPRESSION, 95, 100));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multi-catch with final modifier on parameter.
	 * Tests: {@code catch (final IOException | SQLException e)}.
	 */
	@Test
	public void shouldParseFinalMultiCatchParameter()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (final IOException | SQLException e)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 114),
			semanticNode(CLASS_DECLARATION, 7, 113, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 111),
			semanticNode(BLOCK, 40, 111),
			semanticNode(TRY_STATEMENT, 44, 108),
			semanticNode(BLOCK, 50, 55),
			semanticNode(CATCH_CLAUSE, 58, 108),
			semanticNode(UNION_TYPE, 71, 97),
			semanticNode(QUALIFIED_NAME, 71, 82),
			semanticNode(QUALIFIED_NAME, 85, 97),
			semanticNode(PARAMETER_DECLARATION, 65, 99, "e"),
			semanticNode(BLOCK, 103, 108));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multi-catch with fully qualified exception type names.
	 * Tests: {@code catch (java.io.IOException | java.sql.SQLException e)}.
	 */
	@Test
	public void shouldParseFullyQualifiedExceptionUnion()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (java.io.IOException | java.sql.SQLException e)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 125),
			semanticNode(CLASS_DECLARATION, 7, 124, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 122),
			semanticNode(BLOCK, 40, 122),
			semanticNode(TRY_STATEMENT, 44, 119),
			semanticNode(BLOCK, 50, 55),
			semanticNode(CATCH_CLAUSE, 58, 119),
			semanticNode(UNION_TYPE, 65, 108),
			semanticNode(QUALIFIED_NAME, 65, 84),
			semanticNode(QUALIFIED_NAME, 87, 108),
			semanticNode(PARAMETER_DECLARATION, 65, 110, "e"),
			semanticNode(BLOCK, 114, 119));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
