package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

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
			compilationUnit( 0, 61),
			typeDeclaration(CLASS_DECLARATION, 0, 60, "Test"),
			methodDeclaration( 14, 58),
			block( 24, 58),
			binaryExpression( 36, 54),
			integerLiteral( 36, 37),
			lineComment( 40, 50),
			integerLiteral( 53, 54));
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
			compilationUnit( 0, 62),
			typeDeclaration(CLASS_DECLARATION, 0, 61, "Test"),
			methodDeclaration( 14, 59),
			block( 24, 59),
			binaryExpression( 36, 55),
			integerLiteral( 36, 37),
			blockComment( 40, 53),
			integerLiteral( 54, 55));
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
			compilationUnit( 0, 58),
			typeDeclaration(CLASS_DECLARATION, 0, 57, "Test"),
			methodDeclaration( 14, 55),
			block( 24, 55),
			lineComment( 36, 46),
			integerLiteral( 49, 51));
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
			compilationUnit( 0, 64),
			typeDeclaration(CLASS_DECLARATION, 0, 63, "Test"),
			methodDeclaration( 14, 61),
			block( 24, 61),
			methodInvocation( 28, 57),
			qualifiedName( 28, 32),
			identifier( 28, 32),
			identifier( 33, 37),
			lineComment( 39, 49),
			identifier( 52, 56));
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
			compilationUnit( 0, 62),
			typeDeclaration(CLASS_DECLARATION, 0, 61, "Test"),
			methodDeclaration( 14, 59),
			block( 24, 59),
			assignmentExpression( 28, 55),
			fieldAccess( 28, 51),
			identifier( 28, 31),
			qualifiedName( 28, 32),
			lineComment( 33, 43),
			integerLiteral( 54, 55));
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
			compilationUnit( 0, 65),
			typeDeclaration(CLASS_DECLARATION, 0, 64, "Test"),
			methodDeclaration( 14, 62),
			block( 24, 62),
			arrayAccess( 36, 58),
			identifier( 36, 41),
			blockComment( 42, 55),
			integerLiteral( 56, 57));
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
			compilationUnit( 0, 59),
			typeDeclaration(CLASS_DECLARATION, 0, 58, "Test"),
			methodDeclaration( 14, 56),
			block( 24, 56),
			blockComment( 36, 49),
			unaryExpression( 50, 52),
			integerLiteral( 51, 52));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
