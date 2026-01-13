package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing else-if chains with comments between blocks.
 */
public class ElseIfCommentParserTest
{
	/**
	 * Validates parsing of if-else with line comment between closing brace and else.
	 */
	@Test
	public void testLineCommentBeforeElse()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					if (x > 0)
					{
						y = 1;
					}
					// Comment before else
					else
					{
						y = 0;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 67);
			expected.allocateNode(NodeType.BLOCK, 57, 72);
			expected.allocateNode(NodeType.LINE_COMMENT, 75, 97);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 112, 113);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 113);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 116, 117);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 112, 117);
			expected.allocateNode(NodeType.BLOCK, 107, 122);
			expected.allocateNode(NodeType.IF_STATEMENT, 44, 122);
			expected.allocateNode(NodeType.BLOCK, 40, 125);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 125);
			expected.allocateClassDeclaration(7, 127, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 128);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of if-else if chain with comments between blocks.
	 */
	@Test
	public void testLineCommentBeforeElseIf()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					if (x > 0)
					{
						y = 1;
					}
					// First comment
					else if (x < 0)
					{
						y = -1;
					}
					// Second comment
					else
					{
						y = 0;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 67);
			expected.allocateNode(NodeType.BLOCK, 57, 72);
			expected.allocateNode(NodeType.LINE_COMMENT, 75, 91);
			expected.allocateNode(NodeType.IDENTIFIER, 103, 104);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 107, 108);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 103, 108);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 117, 118);
			expected.allocateNode(NodeType.IDENTIFIER, 117, 118);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 122, 123);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 121, 123);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 117, 123);
			expected.allocateNode(NodeType.BLOCK, 112, 128);
			expected.allocateNode(NodeType.LINE_COMMENT, 131, 148);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 163, 164);
			expected.allocateNode(NodeType.IDENTIFIER, 163, 164);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 167, 168);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 163, 168);
			expected.allocateNode(NodeType.BLOCK, 158, 173);
			expected.allocateNode(NodeType.IF_STATEMENT, 99, 173);
			expected.allocateNode(NodeType.IF_STATEMENT, 44, 173);
			expected.allocateNode(NodeType.BLOCK, 40, 176);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 176);
			expected.allocateClassDeclaration(7, 178, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 179);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of if-else with block comment between closing brace and else.
	 */
	@Test
	public void testBlockCommentBeforeElse()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					if (x > 0)
					{
						y = 1;
					}
					/* Block comment */
					else
					{
						y = 0;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 67);
			expected.allocateNode(NodeType.BLOCK, 57, 72);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 75, 94);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 109, 110);
			expected.allocateNode(NodeType.IDENTIFIER, 109, 110);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 113, 114);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 109, 114);
			expected.allocateNode(NodeType.BLOCK, 104, 119);
			expected.allocateNode(NodeType.IF_STATEMENT, 44, 119);
			expected.allocateNode(NodeType.BLOCK, 40, 122);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 122);
			expected.allocateClassDeclaration(7, 124, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 125);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
