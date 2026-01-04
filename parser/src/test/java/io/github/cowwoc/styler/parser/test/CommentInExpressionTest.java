package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing expressions containing comments.
 * <p>
 * Validates that the parser correctly handles comments appearing within expressions,
 * creating proper AST nodes for both the expression structure and comment nodes.
 */
public final class CommentInExpressionTest
{
	/**
	 * Verifies that line comments between binary operators are correctly handled.
	 * The parser should create proper AST nodes including the comment.
	 */
	@Test
	public void shouldParseLineCommentBetweenBinaryOperators()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = 1 + // comment
					2;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 36, 37);
			expected.allocateNode(NodeType.LINE_COMMENT, 40, 50);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 53, 54);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 36, 54);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that block comments between binary operators are correctly handled.
	 * The parser should create proper AST nodes including the comment.
	 */
	@Test
	public void shouldParseBlockCommentBetweenBinaryOperators()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = 1 + /* comment */ 2;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 36, 37);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 40, 53);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 54, 55);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 36, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that line comments before a primary expression are correctly handled.
	 * The parser should skip the comment and parse the following literal.
	 */
	@Test
	public void shouldParseCommentBeforePrimaryExpression()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = // comment
					42;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 36, 46);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 49, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments within method arguments are correctly handled.
	 * The parser should create proper AST nodes including the comment.
	 */
	@Test
	public void shouldParseCommentInMethodArguments()
	{
		String source = """
			class Test
			{
				void m()
				{
					call(arg1, // comment
					arg2);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 33, 37);
			expected.allocateNode(NodeType.LINE_COMMENT, 39, 49);
			expected.allocateNode(NodeType.IDENTIFIER, 52, 56);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 57);
			expected.allocateNode(NodeType.BLOCK, 24, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 61);
			expected.allocateClassDeclaration(0, 63, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments after the dot operator in field access are correctly handled.
	 * The parser should skip the comment and parse the field name.
	 */
	@Test
	public void shouldParseCommentAfterDotOperator()
	{
		String source = """
			class Test
			{
				void m()
				{
					obj. // comment
					field = 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 31);
			expected.allocateNode(NodeType.LINE_COMMENT, 33, 43);
			expected.allocateNode(NodeType.FIELD_ACCESS, 28, 51);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 54, 55);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 28, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that block comments within array access brackets are correctly handled.
	 * The parser should create proper AST nodes including the comment.
	 */
	@Test
	public void shouldParseCommentInArrayAccess()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = array[/* comment */ 0];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 36, 41);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 42, 55);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 56, 57);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 36, 58);
			expected.allocateNode(NodeType.BLOCK, 24, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that block comments before a unary operator are correctly handled.
	 * The parser should skip the comment and parse the unary expression.
	 */
	@Test
	public void shouldParseCommentBeforeUnaryOperator()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = /* comment */ -5;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 36, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 51, 52);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 50, 52);
			expected.allocateNode(NodeType.BLOCK, 24, 56);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 56);
			expected.allocateClassDeclaration(0, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
