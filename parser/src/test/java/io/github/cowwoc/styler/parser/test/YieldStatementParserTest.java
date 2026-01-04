package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing yield statements in switch expressions.
 */
public final class YieldStatementParserTest
{
	/**
	 * Validates parsing of yield statement with integer literal.
	 */
	@Test
	public void testSimpleYieldWithIntegerLiteral()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield 42;
						}
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 98, 100);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 92, 101);
			expected.allocateNode(NodeType.BLOCK, 86, 106);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 121, 122);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 127);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 128);
			expected.allocateNode(NodeType.BLOCK, 44, 131);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 131);
			expected.allocateClassDeclaration(7, 133, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 134);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield statement with string literal.
	 */
	@Test
	public void testYieldWithStringLiteral()
	{
		String source = """
			public class Test
			{
				public String foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield "hello";
						}
						default -> "";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(39, 44, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 66, 67);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 81, 82);
			expected.allocateNode(NodeType.STRING_LITERAL, 101, 108);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 95, 109);
			expected.allocateNode(NodeType.BLOCK, 89, 114);
			expected.allocateNode(NodeType.STRING_LITERAL, 129, 131);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 58, 136);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 51, 137);
			expected.allocateNode(NodeType.BLOCK, 47, 140);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 140);
			expected.allocateClassDeclaration(7, 142, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 143);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield with method invocation.
	 */
	@Test
	public void testYieldWithMethodInvocation()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield compute();
						}
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.IDENTIFIER, 98, 105);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 98, 107);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 92, 108);
			expected.allocateNode(NodeType.BLOCK, 86, 113);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 128, 129);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 134);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 135);
			expected.allocateNode(NodeType.BLOCK, 44, 138);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 138);
			expected.allocateClassDeclaration(7, 140, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 141);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield with binary expression.
	 */
	@Test
	public void testYieldWithBinaryExpression()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							int temp = 5;
							yield temp * 2;
						}
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 103, 104);
			expected.allocateNode(NodeType.IDENTIFIER, 116, 120);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 123, 124);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 116, 124);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 110, 125);
			expected.allocateNode(NodeType.BLOCK, 86, 130);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 145, 146);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 151);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 152);
			expected.allocateNode(NodeType.BLOCK, 44, 155);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 155);
			expected.allocateClassDeclaration(7, 157, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 158);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield with null literal.
	 */
	@Test
	public void testYieldWithNullLiteral()
	{
		String source = """
			public class Test
			{
				public Object foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield null;
						}
						default -> "";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(39, 44, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 66, 67);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 81, 82);
			expected.allocateNode(NodeType.NULL_LITERAL, 101, 105);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 95, 106);
			expected.allocateNode(NodeType.BLOCK, 89, 111);
			expected.allocateNode(NodeType.STRING_LITERAL, 126, 128);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 58, 133);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 51, 134);
			expected.allocateNode(NodeType.BLOCK, 47, 137);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 137);
			expected.allocateClassDeclaration(7, 139, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 140);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield with ternary conditional expression.
	 */
	@Test
	public void testYieldWithTernaryExpression()
	{
		String source = """
			public class Test
			{
				public int foo(int x, boolean flag)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield flag ? 100 : 0;
						}
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(43, 55, new ParameterAttribute("flag", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 77, 78);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 92, 93);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 116);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 119, 122);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 125, 126);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 112, 126);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 106, 127);
			expected.allocateNode(NodeType.BLOCK, 100, 132);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 147, 148);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 69, 153);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 62, 154);
			expected.allocateNode(NodeType.BLOCK, 58, 157);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 157);
			expected.allocateClassDeclaration(7, 159, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 160);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield with object creation.
	 */
	@Test
	public void testYieldWithObjectCreation()
	{
		String source = """
			public class Test
			{
				public Object foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield new Result();
						}
						default -> null;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(39, 44, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 66, 67);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 81, 82);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 105, 111);
			expected.allocateNode(NodeType.OBJECT_CREATION, 101, 113);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 95, 114);
			expected.allocateNode(NodeType.BLOCK, 89, 119);
			expected.allocateNode(NodeType.NULL_LITERAL, 134, 138);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 58, 143);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 51, 144);
			expected.allocateNode(NodeType.BLOCK, 47, 147);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 147);
			expected.allocateClassDeclaration(7, 149, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 150);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield after multiple statements in block.
	 */
	@Test
	public void testYieldAfterMultipleStatements()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							int a = 1;
							int b = 2;
							int sum = a + b;
							yield sum;
						}
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 100, 101);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 115, 116);
			expected.allocateNode(NodeType.IDENTIFIER, 132, 133);
			expected.allocateNode(NodeType.IDENTIFIER, 136, 137);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 132, 137);
			expected.allocateNode(NodeType.IDENTIFIER, 149, 152);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 143, 153);
			expected.allocateNode(NodeType.BLOCK, 86, 158);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 173, 174);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 179);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 180);
			expected.allocateNode(NodeType.BLOCK, 44, 183);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 183);
			expected.allocateClassDeclaration(7, 185, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 186);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of nested switch expression with yield.
	 */
	@Test
	public void testNestedSwitchExpressionWithYield()
	{
		String source = """
			public class Test
			{
				public int foo(int x, int y)
				{
					return switch (x)
					{
						case 1 -> switch (y)
						{
							case 1 ->
							{
								yield 11;
							}
							default -> 10;
						};
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(43, 48, new ParameterAttribute("y", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 70, 71);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 85, 86);
			expected.allocateNode(NodeType.IDENTIFIER, 98, 99);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 115, 116);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 137, 139);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 131, 140);
			expected.allocateNode(NodeType.BLOCK, 124, 146);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 162, 164);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 90, 170);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 186, 187);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 62, 192);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 55, 193);
			expected.allocateNode(NodeType.BLOCK, 51, 196);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 196);
			expected.allocateClassDeclaration(7, 198, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 199);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield in colon-style switch block.
	 */
	@Test
	public void testYieldInColonStyleSwitchBlock()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1:
						{
							yield 42;
						}
						default:
							yield 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 96, 98);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 90, 99);
			expected.allocateNode(NodeType.BLOCK, 84, 104);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 127, 128);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 121, 129);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 133);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 134);
			expected.allocateNode(NodeType.BLOCK, 44, 137);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 137);
			expected.allocateClassDeclaration(7, 139, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 140);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield with comments.
	 */
	@Test
	public void testYieldWithComment()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							/* comment */ yield /* value */ 42;
						}
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 92, 105);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 112, 123);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 124, 126);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 106, 127);
			expected.allocateNode(NodeType.BLOCK, 86, 132);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 147, 148);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 153);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 154);
			expected.allocateNode(NodeType.BLOCK, 44, 157);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 157);
			expected.allocateClassDeclaration(7, 159, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 160);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple cases with yield.
	 */
	@Test
	public void testMultipleCasesWithYield()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield 10;
						}
						case 2 ->
						{
							yield 20;
						}
						case 3 ->
						{
							yield 30;
						}
						default ->
						{
							yield 0;
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 98, 100);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 92, 101);
			expected.allocateNode(NodeType.BLOCK, 86, 106);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 115, 116);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 135, 137);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 129, 138);
			expected.allocateNode(NodeType.BLOCK, 123, 143);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 152, 153);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 172, 174);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 166, 175);
			expected.allocateNode(NodeType.BLOCK, 160, 180);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 210, 211);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 204, 212);
			expected.allocateNode(NodeType.BLOCK, 198, 217);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 221);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 222);
			expected.allocateNode(NodeType.BLOCK, 44, 225);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 225);
			expected.allocateClassDeclaration(7, 227, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 228);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield in default case.
	 */
	@Test
	public void testYieldInDefaultCase()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 -> 42;
						default ->
						{
							yield 0;
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 83, 85);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 116, 117);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 110, 118);
			expected.allocateNode(NodeType.BLOCK, 104, 123);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 127);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 128);
			expected.allocateNode(NodeType.BLOCK, 44, 131);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 131);
			expected.allocateClassDeclaration(7, 133, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 134);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield in variable assignment context.
	 */
	@Test
	public void testYieldInVariableAssignment()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
					int result = switch (x)
					{
						case 1 ->
						{
							yield 42;
						}
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 70, 71);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 85, 86);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 105, 107);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 99, 108);
			expected.allocateNode(NodeType.BLOCK, 93, 113);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 128, 129);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 62, 134);
			expected.allocateNode(NodeType.BLOCK, 45, 138);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 138);
			expected.allocateClassDeclaration(7, 140, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 141);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield returning a lambda expression.
	 */
	@Test
	public void testYieldWithLambdaExpression()
	{
		String source = """
			public class Test
			{
				public java.util.function.Supplier<Integer> foo(int x)
				{
					return switch (x)
					{
						case 1 ->
						{
							yield () -> 42;
						}
						default -> () -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 63);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 63);
			expected.allocateParameterDeclaration(69, 74, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 96, 97);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 111, 112);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 137, 139);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 131, 139);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 125, 140);
			expected.allocateNode(NodeType.BLOCK, 119, 145);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 166, 167);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 160, 167);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 88, 172);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 81, 173);
			expected.allocateNode(NodeType.BLOCK, 77, 176);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 176);
			expected.allocateClassDeclaration(7, 178, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 179);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of yield with array access expression.
	 */
	@Test
	public void testYieldWithArrayAccess()
	{
		String source = """
			public class Test
			{
				public int foo(int[] arr, int x)
				{
					return switch (x)
					{
						case 0 ->
						{
							yield arr[0];
						}
						default -> -1;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 45, new ParameterAttribute("arr", false, false, false));
			expected.allocateParameterDeclaration(47, 52, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 74, 75);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 89, 90);
			expected.allocateNode(NodeType.IDENTIFIER, 109, 112);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 113, 114);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 109, 115);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 103, 116);
			expected.allocateNode(NodeType.BLOCK, 97, 121);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 137, 138);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 136, 138);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 66, 143);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 59, 144);
			expected.allocateNode(NodeType.BLOCK, 55, 147);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 147);
			expected.allocateClassDeclaration(7, 149, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 150);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
