package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

/**
 * Tests for parsing labeled statements.
 */
public final class LabeledStatementParserTest
{
	/**
	 * Validates parsing of a basic labeled for loop.
	 */
	@Test
	public void labeledForLoop()
	{
		String source = """
			public class Test
			{
				void foo()
				{
					outer: for (int i = 0; i < 10; ++i)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 86),
			typeDeclaration(CLASS_DECLARATION, 7, 85, "Test"),
			methodDeclaration( 21, 83),
			block( 33, 83),
			labeledStatement( 9, 80),
			forStatement( 44, 80),
			integerLiteral( 57, 58),
			binaryExpression( 60, 66),
			identifier( 60, 61),
			integerLiteral( 64, 66),
			unaryExpression( 68, 71),
			identifier( 70, 71),
			block( 75, 80));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a labeled while loop.
	 */
	@Test
	public void labeledWhileLoop()
	{
		String source = """
			public class Test
			{
				void foo()
				{
					retry: while (x > 0)
					{
						--x;
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 79),
			typeDeclaration(CLASS_DECLARATION, 7, 78, "Test"),
			methodDeclaration( 21, 76),
			block( 33, 76),
			labeledStatement( 9, 73),
			whileStatement( 44, 73),
			binaryExpression( 51, 56),
			identifier( 51, 52),
			integerLiteral( 55, 56),
			block( 60, 73),
			unaryExpression( 65, 68),
			identifier( 67, 68));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of nested labels on a single statement.
	 */
	@Test
	public void nestedLabels()
	{
		String source = """
			public class Test
			{
				void foo()
				{
					a: b: for (int i = 0; i < 10; ++i)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 85),
			typeDeclaration(CLASS_DECLARATION, 7, 84, "Test"),
			methodDeclaration( 21, 82),
			block( 33, 82),
			labeledStatement( 9, 79),
			labeledStatement( 11, 79),
			forStatement( 43, 79),
			integerLiteral( 56, 57),
			binaryExpression( 59, 65),
			identifier( 59, 60),
			integerLiteral( 63, 65),
			unaryExpression( 67, 70),
			identifier( 69, 70),
			block( 74, 79));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
