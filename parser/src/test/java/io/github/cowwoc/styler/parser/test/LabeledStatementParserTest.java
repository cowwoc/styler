package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.INTEGER_LITERAL, 57, 58);
			expected.allocateNode(NodeType.IDENTIFIER, 60, 61);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 64, 66);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 60, 66);
			expected.allocateNode(NodeType.IDENTIFIER, 70, 71);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 68, 71);
			expected.allocateNode(NodeType.BLOCK, 75, 80);
			expected.allocateNode(NodeType.FOR_STATEMENT, 44, 80);
			expected.allocateNode(NodeType.LABELED_STATEMENT, 9, 80);
			expected.allocateNode(NodeType.BLOCK, 33, 83);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 83);
			expected.allocateClassDeclaration(7, 85, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 86);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.IDENTIFIER, 51, 52);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 56);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 51, 56);
			expected.allocateNode(NodeType.IDENTIFIER, 67, 68);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 65, 68);
			expected.allocateNode(NodeType.BLOCK, 60, 73);
			expected.allocateNode(NodeType.WHILE_STATEMENT, 44, 73);
			expected.allocateNode(NodeType.LABELED_STATEMENT, 9, 73);
			expected.allocateNode(NodeType.BLOCK, 33, 76);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 76);
			expected.allocateClassDeclaration(7, 78, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 79);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.INTEGER_LITERAL, 56, 57);
			expected.allocateNode(NodeType.IDENTIFIER, 59, 60);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 63, 65);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 59, 65);
			expected.allocateNode(NodeType.IDENTIFIER, 69, 70);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 67, 70);
			expected.allocateNode(NodeType.BLOCK, 74, 79);
			expected.allocateNode(NodeType.FOR_STATEMENT, 43, 79);
			expected.allocateNode(NodeType.LABELED_STATEMENT, 11, 79);
			expected.allocateNode(NodeType.LABELED_STATEMENT, 9, 79);
			expected.allocateNode(NodeType.BLOCK, 33, 82);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 82);
			expected.allocateClassDeclaration(7, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
