package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for lambda expressions in ternary conditional expression contexts.
 */
public final class LambdaTernaryParserTest
{
	/**
	 * Validates simple lambda as ternary alternative (else-branch).
	 * Pattern: {@code condition ? value : param -> body}
	 */
	@Test
	public void simpleLambdaAsAlternative()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object r = flag ? null : x -> x + 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 43);
			expected.allocateNode(NodeType.NULL_LITERAL, 46, 50);
			expected.allocateNode(NodeType.IDENTIFIER, 58, 59);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 62, 63);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 58, 63);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 53, 63);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 63);
			expected.allocateNode(NodeType.BLOCK, 24, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates lambda as ternary consequent (then-branch).
	 * Pattern: {@code condition ? param -> body : value}
	 */
	@Test
	public void lambdaAsConsequent()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object r = flag ? x -> x * 2 : null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 51, 52);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 56);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 51, 56);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 46, 56);
			expected.allocateNode(NodeType.NULL_LITERAL, 59, 63);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 63);
			expected.allocateNode(NodeType.BLOCK, 24, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates nested ternary with lambda in outermost alternative.
	 * Pattern: {@code a ? b ? c : d : x -> x}
	 * Should parse as: {@code a ? (b ? c : d) : (x -> x)}
	 */
	@Test
	public void nestedTernaryWithLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object r = a ? b ? c : d : x -> x;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 40);
			expected.allocateNode(NodeType.IDENTIFIER, 43, 44);
			expected.allocateNode(NodeType.IDENTIFIER, 47, 48);
			expected.allocateNode(NodeType.IDENTIFIER, 51, 52);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 43, 52);
			expected.allocateNode(NodeType.IDENTIFIER, 60, 61);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 55, 61);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 61);
			expected.allocateNode(NodeType.BLOCK, 24, 65);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 65);
			expected.allocateClassDeclaration(0, 67, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates multi-parameter lambda as ternary alternative.
	 * Pattern: {@code condition ? value : (a, b) -> a + b}
	 */
	@Test
	public void multiParamLambdaAsAlternative()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object r = flag ? null : (a, b) -> a + b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 43);
			expected.allocateNode(NodeType.NULL_LITERAL, 46, 50);
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.IDENTIFIER, 67, 68);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 63, 68);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 53, 68);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 68);
			expected.allocateNode(NodeType.BLOCK, 24, 72);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 72);
			expected.allocateClassDeclaration(0, 74, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 75);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
