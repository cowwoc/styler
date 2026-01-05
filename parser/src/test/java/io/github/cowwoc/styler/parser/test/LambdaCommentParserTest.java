package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing comments within lambda expressions.
 * <p>
 * <b>NOTE:</b> Lambdas with parameters and comments before block are not currently supported
 * by the parser. Tests for those scenarios will be added once the parser supports them.
 */
public class LambdaCommentParserTest
{
	/**
	 * Verifies that comments after the arrow in expression lambdas are correctly parsed.
	 */
	@Test
	public void shouldParseLambdaWithCommentAfterArrow()
	{
		String source = """
			class Test
			{
				void m()
				{
					Runnable r = () -> /* comment */ System.out.println();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 47, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 61, 67);
			expected.allocateNode(NodeType.FIELD_ACCESS, 61, 71);
			expected.allocateNode(NodeType.FIELD_ACCESS, 61, 79);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 61, 81);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 41, 81);
			expected.allocateNode(NodeType.BLOCK, 24, 85);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 85);
			expected.allocateClassDeclaration(0, 87, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 88);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments before lambda block are correctly parsed.
	 */
	@Test
	public void shouldParseLambdaWithCommentBeforeBlock()
	{
		String source = """
			class Test
			{
				void m()
				{
					Runnable r = () ->
						// body
						{
						};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.LINE_COMMENT, 50, 57);
			expected.allocateNode(NodeType.BLOCK, 61, 67);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 41, 67);
			expected.allocateNode(NodeType.BLOCK, 24, 71);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 71);
			expected.allocateClassDeclaration(0, 73, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 74);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
