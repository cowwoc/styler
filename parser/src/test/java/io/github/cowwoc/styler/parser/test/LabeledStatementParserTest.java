package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.FOR_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.LABELED_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.UNARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.WHILE_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 86),
			semanticNode(CLASS_DECLARATION, 7, 85, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 83),
			semanticNode(BLOCK, 33, 83),
			semanticNode(LABELED_STATEMENT, 9, 80),
			semanticNode(FOR_STATEMENT, 44, 80),
			semanticNode(INTEGER_LITERAL, 57, 58),
			semanticNode(BINARY_EXPRESSION, 60, 66),
			semanticNode(IDENTIFIER, 60, 61),
			semanticNode(INTEGER_LITERAL, 64, 66),
			semanticNode(UNARY_EXPRESSION, 68, 71),
			semanticNode(IDENTIFIER, 70, 71),
			semanticNode(BLOCK, 75, 80));
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
			semanticNode(COMPILATION_UNIT, 0, 79),
			semanticNode(CLASS_DECLARATION, 7, 78, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 76),
			semanticNode(BLOCK, 33, 76),
			semanticNode(LABELED_STATEMENT, 9, 73),
			semanticNode(WHILE_STATEMENT, 44, 73),
			semanticNode(BINARY_EXPRESSION, 51, 56),
			semanticNode(IDENTIFIER, 51, 52),
			semanticNode(INTEGER_LITERAL, 55, 56),
			semanticNode(BLOCK, 60, 73),
			semanticNode(UNARY_EXPRESSION, 65, 68),
			semanticNode(IDENTIFIER, 67, 68));
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
			semanticNode(COMPILATION_UNIT, 0, 85),
			semanticNode(CLASS_DECLARATION, 7, 84, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 82),
			semanticNode(BLOCK, 33, 82),
			semanticNode(LABELED_STATEMENT, 9, 79),
			semanticNode(LABELED_STATEMENT, 11, 79),
			semanticNode(FOR_STATEMENT, 43, 79),
			semanticNode(INTEGER_LITERAL, 56, 57),
			semanticNode(BINARY_EXPRESSION, 59, 65),
			semanticNode(IDENTIFIER, 59, 60),
			semanticNode(INTEGER_LITERAL, 63, 65),
			semanticNode(UNARY_EXPRESSION, 67, 70),
			semanticNode(IDENTIFIER, 69, 70),
			semanticNode(BLOCK, 74, 79));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
