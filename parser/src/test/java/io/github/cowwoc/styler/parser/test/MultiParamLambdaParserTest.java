package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing multi-parameter lambda expressions.
 */
public final class MultiParamLambdaParserTest
{
	/**
	 * Validates that a two-parameter lambda with expression body parses correctly.
	 * Example: {@code (a, b) -> a + b}
	 */
	@Test
	public void shouldParseTwoParamLambdaWithExpressionBody()
	{
		String source = """
			class Test
			{
				void m()
				{
					BinaryOperator<Integer> op = (a, b) -> a + b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 50);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 50);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 51);
			expected.allocateNode(NodeType.IDENTIFIER, 67, 68);
			expected.allocateNode(NodeType.IDENTIFIER, 71, 72);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 67, 72);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 57, 72);
			expected.allocateNode(NodeType.BLOCK, 24, 76);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 76);
			expected.allocateClassDeclaration(0, 78, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 79);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a two-parameter lambda with block body parses correctly.
	 * Example: {@code (a, b) -> { return a + b; }}
	 */
	@Test
	public void shouldParseTwoParamLambdaWithBlockBody()
	{
		String source = """
			class Test
			{
				void m()
				{
					BinaryOperator<Integer> op = (a, b) -> { return a + b; };
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 50);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 50);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 51);
			expected.allocateNode(NodeType.IDENTIFIER, 76, 77);
			expected.allocateNode(NodeType.IDENTIFIER, 80, 81);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 76, 81);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 69, 82);
			expected.allocateNode(NodeType.BLOCK, 67, 84);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 57, 84);
			expected.allocateNode(NodeType.BLOCK, 24, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a three-parameter lambda parses correctly.
	 * Example: {@code (a, b, c) -> a + b + c}
	 */
	@Test
	public void shouldParseThreeParamLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					TernaryOperator<Integer> op = (a, b, c) -> a + b + c;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 43);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 51);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 51);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 52);
			expected.allocateNode(NodeType.IDENTIFIER, 71, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 75, 76);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 71, 76);
			expected.allocateNode(NodeType.IDENTIFIER, 79, 80);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 71, 80);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 58, 80);
			expected.allocateNode(NodeType.BLOCK, 24, 84);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 84);
			expected.allocateClassDeclaration(0, 86, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 87);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda as method argument parses correctly.
	 * Example: {@code method((a, b) -> process(a, b))}
	 */
	@Test
	public void shouldParseLambdaAsMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					method((a, b) -> process(a, b));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 45, 52);
			expected.allocateNode(NodeType.IDENTIFIER, 53, 54);
			expected.allocateNode(NodeType.IDENTIFIER, 56, 57);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 45, 58);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 35, 58);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 59);
			expected.allocateNode(NodeType.BLOCK, 24, 63);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 63);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
