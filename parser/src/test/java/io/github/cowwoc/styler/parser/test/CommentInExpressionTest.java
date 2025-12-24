package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ARRAY_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK_COMMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.LINE_COMMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.UNARY_EXPRESSION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = 1 + // comment
					2;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 61),
			semanticNode(CLASS_DECLARATION, 0, 60, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 58),
			semanticNode(BLOCK, 24, 58),
			semanticNode(BINARY_EXPRESSION, 36, 54),
			semanticNode(INTEGER_LITERAL, 36, 37),
			semanticNode(LINE_COMMENT, 40, 50),
			semanticNode(INTEGER_LITERAL, 53, 54));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that block comments between binary operators are correctly handled.
	 * The parser should create proper AST nodes including the comment.
	 */
	@Test
	public void shouldParseBlockCommentBetweenBinaryOperators()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = 1 + /* comment */ 2;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 62),
			semanticNode(CLASS_DECLARATION, 0, 61, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 59),
			semanticNode(BLOCK, 24, 59),
			semanticNode(BINARY_EXPRESSION, 36, 55),
			semanticNode(INTEGER_LITERAL, 36, 37),
			semanticNode(BLOCK_COMMENT, 40, 53),
			semanticNode(INTEGER_LITERAL, 54, 55));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that line comments before a primary expression are correctly handled.
	 * The parser should skip the comment and parse the following literal.
	 */
	@Test
	public void shouldParseCommentBeforePrimaryExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = // comment
					42;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 58),
			semanticNode(CLASS_DECLARATION, 0, 57, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 55),
			semanticNode(BLOCK, 24, 55),
			semanticNode(LINE_COMMENT, 36, 46),
			semanticNode(INTEGER_LITERAL, 49, 51));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that comments within method arguments are correctly handled.
	 * The parser should create proper AST nodes including the comment.
	 */
	@Test
	public void shouldParseCommentInMethodArguments()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					call(arg1, // comment
					arg2);
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 64),
			semanticNode(CLASS_DECLARATION, 0, 63, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 61),
			semanticNode(BLOCK, 24, 61),
			semanticNode(METHOD_INVOCATION, 28, 57),
			semanticNode(QUALIFIED_NAME, 28, 32),
			semanticNode(IDENTIFIER, 28, 32),
			semanticNode(IDENTIFIER, 33, 37),
			semanticNode(LINE_COMMENT, 39, 49),
			semanticNode(IDENTIFIER, 52, 56));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that comments after the dot operator in field access are correctly handled.
	 * The parser should skip the comment and parse the field name.
	 */
	@Test
	public void shouldParseCommentAfterDotOperator()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					obj. // comment
					field = 1;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 62),
			semanticNode(CLASS_DECLARATION, 0, 61, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 59),
			semanticNode(BLOCK, 24, 59),
			semanticNode(ASSIGNMENT_EXPRESSION, 28, 55),
			semanticNode(FIELD_ACCESS, 28, 51),
			semanticNode(IDENTIFIER, 28, 31),
			semanticNode(QUALIFIED_NAME, 28, 32),
			semanticNode(LINE_COMMENT, 33, 43),
			semanticNode(INTEGER_LITERAL, 54, 55));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that block comments within array access brackets are correctly handled.
	 * The parser should create proper AST nodes including the comment.
	 */
	@Test
	public void shouldParseCommentInArrayAccess()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = array[/* comment */ 0];
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 65),
			semanticNode(CLASS_DECLARATION, 0, 64, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 62),
			semanticNode(BLOCK, 24, 62),
			semanticNode(ARRAY_ACCESS, 36, 58),
			semanticNode(IDENTIFIER, 36, 41),
			semanticNode(BLOCK_COMMENT, 42, 55),
			semanticNode(INTEGER_LITERAL, 56, 57));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that block comments before a unary operator are correctly handled.
	 * The parser should skip the comment and parse the unary expression.
	 */
	@Test
	public void shouldParseCommentBeforeUnaryOperator()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					int x = /* comment */ -5;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 59),
			semanticNode(CLASS_DECLARATION, 0, 58, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 56),
			semanticNode(BLOCK, 24, 56),
			semanticNode(BLOCK_COMMENT, 36, 49),
			semanticNode(UNARY_EXPRESSION, 50, 52),
			semanticNode(INTEGER_LITERAL, 51, 52));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
