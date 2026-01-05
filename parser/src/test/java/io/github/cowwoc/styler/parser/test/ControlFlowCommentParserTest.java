package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing comments within control flow statements.
 * <p>
 * <b>NOTE:</b> Some control flow comment scenarios are not currently supported by the parser:
 * <ul>
 *     <li>Comments before else clause</li>
 *     <li>Comments before case in switch</li>
 *     <li>Comments before catch clause</li>
 *     <li>Comments before finally clause</li>
 * </ul>
 * Tests for those scenarios will be added once the parser supports them.
 */
public class ControlFlowCommentParserTest
{
	/**
	 * Verifies that comments before if condition are correctly parsed.
	 */
	@Test
	public void shouldParseIfStatementWithCommentBeforeCondition()
	{
		String source = """
			class Test
			{
				void m()
				{
					if (/* condition */ true)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 32, 47);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 48, 52);
			expected.allocateNode(NodeType.BLOCK, 56, 61);
			expected.allocateNode(NodeType.IF_STATEMENT, 28, 61);
			expected.allocateNode(NodeType.BLOCK, 24, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 64);
			expected.allocateClassDeclaration(0, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments in for loop header are correctly parsed.
	 */
	@Test
	public void shouldParseForLoopWithCommentInHeader()
	{
		String source = """
			class Test
			{
				void m()
				{
					for (int i = 0; /* loop condition */ i < 10; i++)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 41, 42);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 44, 64);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 66);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 69, 71);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 65, 71);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 74);
			expected.allocateNode(NodeType.POSTFIX_EXPRESSION, 73, 76);
			expected.allocateNode(NodeType.BLOCK, 80, 85);
			expected.allocateNode(NodeType.FOR_STATEMENT, 28, 85);
			expected.allocateNode(NodeType.BLOCK, 24, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments after colon in enhanced for are correctly parsed.
	 */
	@Test
	public void shouldParseEnhancedForWithCommentAfterColon()
	{
		String source = """
			class Test
			{
				void m()
				{
					for (int x : /* items */ items)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 41, 52);
			expected.allocateNode(NodeType.IDENTIFIER, 53, 58);
			expected.allocateNode(NodeType.BLOCK, 62, 67);
			expected.allocateNode(NodeType.ENHANCED_FOR_STATEMENT, 28, 67);
			expected.allocateNode(NodeType.BLOCK, 24, 70);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 70);
			expected.allocateClassDeclaration(0, 72, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments within while condition are correctly parsed.
	 */
	@Test
	public void shouldParseWhileWithCommentInCondition()
	{
		String source = """
			class Test
			{
				void m()
				{
					while (/* check */ true)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 35, 46);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 47, 51);
			expected.allocateNode(NodeType.BLOCK, 55, 60);
			expected.allocateNode(NodeType.WHILE_STATEMENT, 28, 60);
			expected.allocateNode(NodeType.BLOCK, 24, 63);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 63);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
