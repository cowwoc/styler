package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing cast expressions with lambda expression operands.
 */
public final class CastLambdaExpressionParserTest
{
	/**
	 * Validates that a cast of a no-parameter lambda parses correctly.
	 * Example: {@code (Runnable) () -> doSomething()}
	 */
	@Test
	public void shouldParseCastOfNoParamLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Runnable r = (Runnable) () -> doSomething();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();

			// Manual derivation of expected nodes:
			// Runnable r = (Runnable) () -> doSomething();
			// - QUALIFIED_NAME for variable type "Runnable" (28-36)
			// - IDENTIFIER for "doSomething" (58-69)
			// - METHOD_INVOCATION for "doSomething()" (58-71)
			// - LAMBDA_EXPRESSION for "() -> doSomething()" (52-71)
			// - CAST_EXPRESSION for "(Runnable) () -> doSomething()" (41-71)
			// - BLOCK for method body (24-75)
			// - METHOD_DECLARATION for "void m()" (14-75)
			// - CLASS_DECLARATION for "class Test" (0-77)
			// - COMPILATION_UNIT (0-78)
			// Positions verified against actual output
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 58, 69);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 58, 71);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 52, 71);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 41, 71);
			expected.allocateNode(NodeType.BLOCK, 24, 75);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 75);
			expected.allocateClassDeclaration(0, 77, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 78);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast of a single-parameter lambda parses correctly.
	 * Example: {@code (Function<String, Integer>) s -> s.length()}
	 */
	@Test
	public void shouldParseCastOfSingleParamLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object f = (Function<String, Integer>) s -> s.length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();

			// Manual derivation of expected nodes:
			// Object f = (Function<String, Integer>) s -> s.length();
			// - QUALIFIED_NAME for "Object" (28-34)
			// - QUALIFIED_NAME for "String" type argument (49-55, duplicated for allocation)
			// - QUALIFIED_NAME for "Integer" type argument (57-64, duplicated)
			// - IDENTIFIER for "s" in s.length() (72-73)
			// - FIELD_ACCESS for "s.length" (72-80)
			// - METHOD_INVOCATION for "s.length()" (72-82)
			// - LAMBDA_EXPRESSION for "s -> s.length()" (67-82)
			// - CAST_EXPRESSION for the whole cast (39-82)
			// Positions verified against actual output
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 64);
			expected.allocateNode(NodeType.IDENTIFIER, 72, 73);
			expected.allocateNode(NodeType.FIELD_ACCESS, 72, 80);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 72, 82);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 67, 82);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 82);
			expected.allocateNode(NodeType.BLOCK, 24, 86);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 86);
			expected.allocateClassDeclaration(0, 88, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 89);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a cast of a lambda with block body parses correctly.
	 * Example: {@code (Consumer<String>) s -> { process(s); }}
	 */
	@Test
	public void shouldParseCastOfBlockLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object c = (Consumer<String>) s -> { process(s); };
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();

			// Manual derivation of expected nodes:
			// Object c = (Consumer<String>) s -> { process(s); };
			// - QUALIFIED_NAME for "Object" (28-34)
			// - QUALIFIED_NAME for "String" type argument (49-55, duplicated)
			// - QUALIFIED_NAME for "process" (65-72)
			// - IDENTIFIER for "process" (65-72)
			// - IDENTIFIER for "s" argument (73-74)
			// - METHOD_INVOCATION for "process(s)" (65-75)
			// - BLOCK for "{ process(s); }" (63-78)
			// - LAMBDA_EXPRESSION for "s -> { process(s); }" (58-78)
			// - CAST_EXPRESSION for the whole cast (39-78)
			// Positions verified against actual output
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 74);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 65, 75);
			expected.allocateNode(NodeType.BLOCK, 63, 78);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 58, 78);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 39, 78);
			expected.allocateNode(NodeType.BLOCK, 24, 82);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 82);
			expected.allocateClassDeclaration(0, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that nested cast with lambda parses correctly.
	 * Example: {@code (HibernateCallback<List<T>>) session -> { ... }}
	 */
	@Test
	public void shouldParseNestedGenericCastOfLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object cb = (Callback<List<String>>) list -> list.size();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();

			// Manual derivation of expected nodes:
			// Object cb = (Callback<List<String>>) list -> list.size();
			// - QUALIFIED_NAME for "Object" (28-34)
			// - QUALIFIED_NAME for "List" type argument (50-54)
			// - QUALIFIED_NAME for "String" nested type argument (55-61, duplicated)
			// - PARAMETERIZED_TYPE for "List<String>" (50-63)
			// - QUALIFIED_NAME for the whole type argument (50-63)
			// - IDENTIFIER for "list" in list.size() (73-77)
			// - FIELD_ACCESS for "list.size" (73-82)
			// - METHOD_INVOCATION for "list.size()" (73-84)
			// - LAMBDA_EXPRESSION for "list -> list.size()" (65-84)
			// - CAST_EXPRESSION for the whole cast (40-84)
			// Positions verified against actual output
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 61);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 50, 63);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 77);
			expected.allocateNode(NodeType.FIELD_ACCESS, 73, 82);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 73, 84);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 65, 84);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 40, 84);
			expected.allocateNode(NodeType.BLOCK, 24, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that cast of lambda inside method argument parses correctly.
	 * Example: {@code nonNull((Callback<T>) session -> ...)}
	 */
	@Test
	public void shouldParseCastOfLambdaAsMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object result = nonNull((Callback<String>) s -> s.trim());
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();

			// Manual derivation of expected nodes:
			// Object result = nonNull((Callback<String>) s -> s.trim());
			// - QUALIFIED_NAME for "Object" (28-34)
			// - IDENTIFIER for "nonNull" (44-51)
			// - QUALIFIED_NAME for "String" type argument (62-68, duplicated)
			// - IDENTIFIER for "s" in s.trim() (76-77)
			// - FIELD_ACCESS for "s.trim" (76-82)
			// - METHOD_INVOCATION for "s.trim()" (76-84)
			// - LAMBDA_EXPRESSION for "s -> s.trim()" (71-84)
			// - CAST_EXPRESSION for "(Callback<String>) s -> s.trim()" (52-84)
			// - METHOD_INVOCATION for "nonNull(...)" (44-85)
			// Positions verified against actual output
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 44, 51);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.IDENTIFIER, 76, 77);
			expected.allocateNode(NodeType.FIELD_ACCESS, 76, 82);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 76, 84);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 71, 84);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 52, 84);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 44, 85);
			expected.allocateNode(NodeType.BLOCK, 24, 89);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 89);
			expected.allocateClassDeclaration(0, 91, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 92);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
