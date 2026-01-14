package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for handling comments in type reference contexts (implements, extends clauses).
 */
public class CommentInTypeContextTest
{
	/**
	 * Tests that line comments between comma and type name in implements clause are handled.
	 * Reproduces: "Expected identifier but found LINE_COMMENT" error.
	 */
	@Test
	public void testCommentBetweenImplementedTypes()
	{
		String source = """
			class Test implements Serializable, // comment
			    Comparable
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 34);
			expected.allocateNode(NodeType.LINE_COMMENT, 36, 46);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 61);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests that comments between comma and type in extends clause are handled.
	 */
	@Test
	public void testCommentBetweenExtendedInterfaces()
	{
		String source = """
			interface Test extends Runnable, // comment
			    Serializable
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 31);
			expected.allocateNode(NodeType.LINE_COMMENT, 33, 43);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 60);
			expected.allocateInterfaceDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
