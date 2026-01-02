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
			compilationUnit( 0, 108),
			typeDeclaration(CLASS_DECLARATION, 7, 107, "Test"),
			methodDeclaration( 21, 105),
			block( 40, 105),
			tryStatement( 44, 102),
			block( 50, 55),
			catchClause( 58, 102),
			unionType( 65, 91),
			qualifiedName( 65, 76),
			qualifiedName( 79, 91),
			parameterNode( 65, 93, "e"),
			block( 97, 102));
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
			compilationUnit( 0, 127),
			typeDeclaration(CLASS_DECLARATION, 7, 126, "Test"),
			methodDeclaration( 21, 124),
			block( 40, 124),
			tryStatement( 44, 121),
			block( 50, 55),
			catchClause( 58, 121),
			unionType( 65, 110),
			qualifiedName( 65, 76),
			qualifiedName( 79, 91),
			qualifiedName( 94, 110),
			parameterNode( 65, 112, "e"),
			block( 116, 121));
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
			compilationUnit( 0, 111),
			typeDeclaration(CLASS_DECLARATION, 7, 110, "Test"),
			methodDeclaration( 21, 108),
			block( 40, 108),
			tryStatement( 44, 105),
			block( 50, 65),
			qualifiedName( 55, 56),
			identifier( 55, 56),
			integerLiteral( 59, 60),
			assignmentExpression( 55, 60),
			catchClause( 68, 105),
			qualifiedName( 75, 84),
			parameterNode( 75, 86, "e"),
			block( 90, 105),
			qualifiedName( 95, 96),
			identifier( 95, 96),
			integerLiteral( 99, 100),
			assignmentExpression( 95, 100));
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
			compilationUnit( 0, 114),
			typeDeclaration(CLASS_DECLARATION, 7, 113, "Test"),
			methodDeclaration( 21, 111),
			block( 40, 111),
			tryStatement( 44, 108),
			block( 50, 55),
			catchClause( 58, 108),
			unionType( 71, 97),
			qualifiedName( 71, 82),
			qualifiedName( 85, 97),
			parameterNode( 65, 99, "e"),
			block( 103, 108));
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
			compilationUnit( 0, 125),
			typeDeclaration(CLASS_DECLARATION, 7, 124, "Test"),
			methodDeclaration( 21, 122),
			block( 40, 122),
			tryStatement( 44, 119),
			block( 50, 55),
			catchClause( 58, 119),
			unionType( 65, 108),
			qualifiedName( 65, 84),
			qualifiedName( 87, 108),
			parameterNode( 65, 110, "e"),
			block( 114, 119));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
