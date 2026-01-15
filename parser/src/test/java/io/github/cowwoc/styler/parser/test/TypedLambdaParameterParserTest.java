package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing typed lambda parameter expressions.
 */
public final class TypedLambdaParameterParserTest
{
	/**
	 * Validates that a single typed parameter lambda parses correctly.
	 */
	@Test
	public void shouldParseSingleTypedParameterLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					handle((MyEvent event) -> process(event));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 54, 61);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 67);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 54, 68);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 35, 68);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 69);
			expected.allocateNode(NodeType.BLOCK, 24, 73);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 73);
			expected.allocateClassDeclaration(0, 75, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 76);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a multi-typed parameter lambda parses correctly.
	 */
	@Test
	public void shouldParseMultiTypedParameterLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					BiFunction<String, Integer, String> f = (String a, int b) -> a + b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 63);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 69, 75);
			expected.allocateNode(NodeType.IDENTIFIER, 89, 90);
			expected.allocateNode(NodeType.IDENTIFIER, 93, 94);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 89, 94);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 68, 94);
			expected.allocateNode(NodeType.BLOCK, 24, 98);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 98);
			expected.allocateClassDeclaration(0, 100, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 101);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a typed lambda as method argument parses correctly.
	 */
	@Test
	public void shouldParseTypedLambdaAsMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					context.addListener((Event e) -> process(e));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 47);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 35);
			expected.allocateNode(NodeType.FIELD_ACCESS, 28, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 54);
			expected.allocateNode(NodeType.IDENTIFIER, 61, 68);
			expected.allocateNode(NodeType.IDENTIFIER, 69, 70);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 61, 71);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 48, 71);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 72);
			expected.allocateNode(NodeType.BLOCK, 24, 76);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 76);
			expected.allocateClassDeclaration(0, 78, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 79);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with generic type parameter parses correctly.
	 */
	@Test
	public void shouldParseLambdaWithGenericTypeParameter()
	{
		String source = """
			class Test
			{
				void m()
				{
					Function<List<String>, Integer> f = (List<String> items) -> items.size();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 37, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 58);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 58);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 59);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 69);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 70, 76);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 70, 76);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 65, 77);
			expected.allocateNode(NodeType.IDENTIFIER, 88, 93);
			expected.allocateNode(NodeType.FIELD_ACCESS, 88, 98);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 88, 100);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 64, 100);
			expected.allocateNode(NodeType.BLOCK, 24, 104);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 104);
			expected.allocateClassDeclaration(0, 106, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 107);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with array type parameter parses correctly.
	 */
	@Test
	public void shouldParseLambdaWithArrayTypeParameter()
	{
		String source = """
			class Test
			{
				void m()
				{
					ToIntFunction<String[]> f = (String[] args) -> args.length;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 50);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 51);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 75, 79);
			expected.allocateNode(NodeType.FIELD_ACCESS, 75, 86);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 56, 86);
			expected.allocateNode(NodeType.BLOCK, 24, 90);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 90);
			expected.allocateClassDeclaration(0, 92, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 93);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with final modifier parses correctly.
	 */
	@Test
	public void shouldParseLambdaWithFinalModifier()
	{
		String source = """
			class Test
			{
				void m()
				{
					ToIntFunction<String> f = (final String s) -> s.length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 61, 67);
			expected.allocateNode(NodeType.IDENTIFIER, 74, 75);
			expected.allocateNode(NodeType.FIELD_ACCESS, 74, 82);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 74, 84);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 54, 84);
			expected.allocateNode(NodeType.BLOCK, 24, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with parameter annotation parses correctly.
	 */
	@Test
	public void shouldParseLambdaWithParameterAnnotation()
	{
		String source = """
			class Test
			{
				void m()
				{
					ToIntFunction<String> f = (@NonNull String s) -> s.length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 63);
			expected.allocateNode(NodeType.ANNOTATION, 55, 63);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 70);
			expected.allocateNode(NodeType.IDENTIFIER, 77, 78);
			expected.allocateNode(NodeType.FIELD_ACCESS, 77, 85);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 77, 87);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 54, 87);
			expected.allocateNode(NodeType.BLOCK, 24, 91);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 91);
			expected.allocateClassDeclaration(0, 93, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 94);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that cast expressions still parse correctly after the lambda detection changes.
	 */
	@Test
	public void shouldParseCastExpression()
	{
		String source = """
			class Test
			{
				void m()
				{
					String s = (String) obj;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 48, 51);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that untyped lambda expressions still parse correctly.
	 */
	@Test
	public void shouldParseUntypedLambda()
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
}
