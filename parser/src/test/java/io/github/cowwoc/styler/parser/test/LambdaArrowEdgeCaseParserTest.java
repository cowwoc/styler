package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for lambda expressions as assignment operands.
 */
public final class LambdaArrowEdgeCaseParserTest
{
	/**
	 * Validates that a lambda as RHS of field assignment parses correctly.
	 * This was the original failing pattern from Spring Framework.
	 */
	@Test
	public void shouldParseLambdaAsAssignmentRhs()
	{
		String source = """
			class Test
			{
				void init()
				{
					this.sessionManager = exchange -> Mono.just(session);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.THIS_EXPRESSION, 31, 35);
			expected.allocateNode(NodeType.FIELD_ACCESS, 31, 50);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 69);
			expected.allocateNode(NodeType.FIELD_ACCESS, 65, 74);
			expected.allocateNode(NodeType.IDENTIFIER, 75, 82);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 65, 83);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 53, 83);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 31, 83);
			expected.allocateNode(NodeType.BLOCK, 27, 87);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 87);
			expected.allocateClassDeclaration(0, 89, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 90);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a no-param lambda as RHS of field assignment parses correctly.
	 */
	@Test
	public void shouldParseNoParamLambdaAsAssignmentRhs()
	{
		String source = """
			class Test
			{
				void init()
				{
					this.supplier = () -> "hello";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.THIS_EXPRESSION, 31, 35);
			expected.allocateNode(NodeType.FIELD_ACCESS, 31, 44);
			expected.allocateNode(NodeType.STRING_LITERAL, 53, 60);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 47, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 31, 60);
			expected.allocateNode(NodeType.BLOCK, 27, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 64);
			expected.allocateClassDeclaration(0, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates lambda in nested assignment (a = b = x -> expr).
	 */
	@Test
	public void shouldParseLambdaInNestedAssignment()
	{
		String source = """
			class Test
			{
				void init()
				{
					a = b = x -> x + 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 31, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 35, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 44, 45);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 48, 49);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 44, 49);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 39, 49);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 35, 49);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 31, 49);
			expected.allocateNode(NodeType.BLOCK, 27, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates lambda after method reference with trailing comments.
	 * <p>
	 * This pattern occurs in Spring Framework's DatabasePopulator.java:
	 * {@code Mono.usingWhen(source, this::populate, connection -> release(connection))}
	 * The trailing comments and line breaks were causing "Expected RIGHT_PARENTHESIS but found ARROW".
	 * <p>
	 * <b>TDD RED:</b> This test reproduces a production bug and is disabled until the fix is implemented.
	 *
	 * @see <a href=".claude/cat/v0/v0.5/task/fix-lambda-arrow-in-parenthesized-context">Task to fix this</a>
	 */
	@Test
	public void shouldParseLambdaAfterMethodReferenceWithTrailingComments()
	{
		String source = """
			class Test
			{
				void foo()
				{
					Mono.usingWhen(getConnection(), //
						this::populate, //
						connection -> release(connection));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 44);
			expected.allocateNode(NodeType.IDENTIFIER, 30, 34);
			expected.allocateNode(NodeType.FIELD_ACCESS, 30, 44);
			expected.allocateNode(NodeType.IDENTIFIER, 45, 58);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 45, 60);
			expected.allocateNode(NodeType.LINE_COMMENT, 62, 64);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 68, 72);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 68, 82);
			expected.allocateNode(NodeType.LINE_COMMENT, 84, 86);
			expected.allocateNode(NodeType.IDENTIFIER, 104, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 122);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 104, 123);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 90, 123);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 30, 124);
			expected.allocateNode(NodeType.BLOCK, 26, 128);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 128);
			expected.allocateClassDeclaration(0, 130, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 131);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
