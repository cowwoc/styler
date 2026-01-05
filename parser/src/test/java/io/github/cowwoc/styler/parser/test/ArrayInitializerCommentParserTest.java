package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing comments within array initializers.
 */
public class ArrayInitializerCommentParserTest
{
	/**
	 * Verifies that comments between array elements are correctly parsed.
	 */
	@Test
	public void shouldParseArrayInitializerWithCommentBetweenElements()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = {1, /* middle */ 2, 3};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 41, 42);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 44, 56);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 57, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 60, 61);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 40, 62);
			expected.allocateNode(NodeType.BLOCK, 24, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 66);
			expected.allocateClassDeclaration(0, 68, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 69);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments before first array element are correctly parsed.
	 */
	@Test
	public void shouldParseArrayInitializerWithCommentBeforeFirstElement()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = {
						// first element
						1, 2, 3
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 45, 61);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 65, 66);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 68, 69);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 71, 72);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 40, 76);
			expected.allocateNode(NodeType.BLOCK, 24, 80);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 80);
			expected.allocateClassDeclaration(0, 82, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 83);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments in nested array initializers are correctly parsed.
	 */
	@Test
	public void shouldParseNestedArrayInitializerWithComments()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[][] arr = {
						// row 1
						{1, 2},
						// row 2
						{3, 4}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 47, 55);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 60, 61);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 63, 64);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 59, 65);
			expected.allocateNode(NodeType.LINE_COMMENT, 70, 78);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 83, 84);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 86, 87);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 82, 88);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 42, 92);
			expected.allocateNode(NodeType.BLOCK, 24, 96);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 96);
			expected.allocateClassDeclaration(0, 98, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 99);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
