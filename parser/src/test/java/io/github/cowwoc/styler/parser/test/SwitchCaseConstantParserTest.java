package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing switch case labels with constant references (not literals).
 */
public class SwitchCaseConstantParserTest
{
	/**
	 * Validates parsing of switch expression with constant references in case labels.
	 * Tests the pattern: case CONSTANT_NAME -> expression;
	 */
	@Test
	public void shouldParseCaseWithConstantReference()
	{
		String source = """
			public class Test
			{
				public static final int A = 1;
				public static final int B = 2;

				public String foo(int x)
				{
					return switch (x)
					{
						case A -> "a";
						case B -> "b";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 49, 50);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 51);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 81, 82);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 53, 83);
			expected.allocateParameterDeclaration(104, 109, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 131, 132);
			expected.allocateNode(NodeType.IDENTIFIER, 146, 147);
			expected.allocateNode(NodeType.STRING_LITERAL, 151, 154);
			expected.allocateNode(NodeType.IDENTIFIER, 164, 165);
			expected.allocateNode(NodeType.STRING_LITERAL, 169, 172);
			expected.allocateNode(NodeType.STRING_LITERAL, 188, 195);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 123, 200);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 116, 201);
			expected.allocateNode(NodeType.BLOCK, 112, 204);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 86, 204);
			expected.allocateClassDeclaration(7, 206, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 207);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch with qualified constant references.
	 * Tests the pattern: case Enum.VALUE -> expression;
	 */
	@Test
	public void shouldParseCaseWithQualifiedConstant()
	{
		String source = """
			public class Test
			{
				public String describe(Status status)
				{
					return switch (status)
					{
						case Status.ACTIVE -> "active";
						case Status.INACTIVE -> "inactive";
						default -> "unknown";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 57, new ParameterAttribute("status", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 79, 85);
			expected.allocateNode(NodeType.IDENTIFIER, 99, 105);
			expected.allocateNode(NodeType.FIELD_ACCESS, 99, 112);
			expected.allocateNode(NodeType.STRING_LITERAL, 116, 124);
			expected.allocateNode(NodeType.IDENTIFIER, 134, 140);
			expected.allocateNode(NodeType.FIELD_ACCESS, 134, 149);
			expected.allocateNode(NodeType.STRING_LITERAL, 153, 163);
			expected.allocateNode(NodeType.STRING_LITERAL, 179, 188);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 71, 193);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 64, 194);
			expected.allocateNode(NodeType.BLOCK, 60, 197);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 197);
			expected.allocateClassDeclaration(7, 199, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 200);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with mixed literals and constants.
	 */
	@Test
	public void shouldParseCaseWithMixedLabels()
	{
		String source = """
			public class Test
			{
				public static final int SPECIAL = 99;

				public String categorize(int value)
				{
					return switch (value)
					{
						case 0 -> "zero";
						case 1, 2, 3 -> "small";
						case SPECIAL -> "special";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 57);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 58);
			expected.allocateParameterDeclaration(86, 95, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 117, 122);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 136, 137);
			expected.allocateNode(NodeType.STRING_LITERAL, 141, 147);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 157, 158);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 160, 161);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 163, 164);
			expected.allocateNode(NodeType.STRING_LITERAL, 168, 175);
			expected.allocateNode(NodeType.IDENTIFIER, 185, 192);
			expected.allocateNode(NodeType.STRING_LITERAL, 196, 205);
			expected.allocateNode(NodeType.STRING_LITERAL, 221, 228);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 109, 233);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 102, 234);
			expected.allocateNode(NodeType.BLOCK, 98, 237);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 61, 237);
			expected.allocateClassDeclaration(7, 239, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 240);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that regular lambda expressions still work outside switch cases.
	 * Ensures the fix doesn't break normal lambda parsing.
	 */
	@Test
	public void shouldStillParseLambdaExpressions()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					Runnable r = () -> System.out.println("hello");
					Function<String, Integer> f = s -> s.length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 52);
			expected.allocateNode(NodeType.IDENTIFIER, 63, 69);
			expected.allocateNode(NodeType.FIELD_ACCESS, 63, 73);
			expected.allocateNode(NodeType.FIELD_ACCESS, 63, 81);
			expected.allocateNode(NodeType.STRING_LITERAL, 82, 89);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 63, 90);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 57, 90);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 94, 102);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 103, 109);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 103, 109);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 111, 118);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 111, 118);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 94, 119);
			expected.allocateNode(NodeType.IDENTIFIER, 129, 130);
			expected.allocateNode(NodeType.FIELD_ACCESS, 129, 137);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 129, 139);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 124, 139);
			expected.allocateNode(NodeType.BLOCK, 40, 143);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 143);
			expected.allocateClassDeclaration(7, 145, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 146);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
