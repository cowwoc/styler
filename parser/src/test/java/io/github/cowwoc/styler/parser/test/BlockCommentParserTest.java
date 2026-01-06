package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing comments at block boundaries and in empty blocks.
 */
public class BlockCommentParserTest
{
	/**
	 * Verifies that comments after opening brace are correctly parsed.
	 */
	@Test
	public void shouldParseBlockWithCommentAfterOpeningBrace()
	{
		String source = """
			class Test
			{
				void m()
				{ // start of block
					return 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 26, 43);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 53, 54);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 46, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments before closing brace are correctly parsed.
	 */
	@Test
	public void shouldParseBlockWithCommentBeforeClosingBrace()
	{
		String source = """
			class Test
			{
				void m()
				{
					return 1;
					// end of block
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 35, 36);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 28, 37);
			expected.allocateNode(NodeType.LINE_COMMENT, 40, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that empty blocks containing only a comment are correctly parsed.
	 */
	@Test
	public void shouldParseEmptyBlockWithOnlyComment()
	{
		String source = """
			class Test
			{
				void m()
				{
					// empty method body
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 28, 48);
			expected.allocateNode(NodeType.BLOCK, 24, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 51);
			expected.allocateClassDeclaration(0, 53, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments in nested blocks are correctly parsed.
	 */
	@Test
	public void shouldParseNestedBlocksWithComments()
	{
		String source = """
			class Test
			{
				void m()
				{
					// outer comment
					{
						// inner comment
						return 1;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 28, 44);
			expected.allocateNode(NodeType.LINE_COMMENT, 52, 68);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 79, 80);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 72, 81);
			expected.allocateNode(NodeType.BLOCK, 47, 85);
			expected.allocateNode(NodeType.BLOCK, 24, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that block comments between member declarations are correctly parsed.
	 */
	@Test
	public void shouldParseBlockCommentBetweenMemberDeclarations()
	{
		String source = """
			class Test
			{
				int x;
				/* separator comment */
				int y;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 20);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 22, 45);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 47, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
