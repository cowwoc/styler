package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing enum constants with anonymous body containing comments.
 */
public class EnumConstantBodyParserTest
{
	/**
	 * Verifies that enum constant with anonymous body containing only a comment is correctly parsed.
	 */
	@Test
	public void shouldParseEnumConstantBodyWithOnlyComment()
	{
		String source = """
			public enum Status
			{
				ACTIVE
				{
					// Empty body for now
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 34, 55);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 22, 58);
			expected.allocateEnumDeclaration(7, 60, new TypeDeclarationAttribute("Status"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that enum constant with anonymous body containing block comment before method is parsed.
	 */
	@Test
	public void shouldParseEnumConstantBodyWithBlockCommentBeforeMethod()
	{
		String source = """
			public enum Type
			{
				VALUE
				{
					/* Method description */
					public void run()
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 31, 55);
			expected.allocateNode(NodeType.BLOCK, 78, 83);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 58, 83);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 20, 86);
			expected.allocateEnumDeclaration(7, 88, new TypeDeclarationAttribute("Type"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 89);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that multiple enum constants with anonymous bodies containing comments are parsed.
	 */
	@Test
	public void shouldParseMultipleEnumConstantsWithBodiesAndComments()
	{
		String source = """
			public enum Mode
			{
				A
				{
					// First constant
				},
				B
				{
					// Second constant
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 27, 44);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 20, 47);
			expected.allocateNode(NodeType.LINE_COMMENT, 57, 75);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 50, 78);
			expected.allocateEnumDeclaration(7, 80, new TypeDeclarationAttribute("Mode"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 81);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
