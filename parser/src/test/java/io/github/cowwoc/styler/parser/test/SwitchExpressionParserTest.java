package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing switch expressions including arrow syntax, complex contexts, edge cases,
 * and real-world patterns.
 */
public class SwitchExpressionParserTest
{
	/**
	 * Validates parsing of multi-label case with arrow syntax in switch expression.
	 * Tests enhanced switch with multiple case labels using arrow operator for expressions.
	 */
	@Test
	public void testMultiLabelCaseWithArrow()
	{
		String source = """
			public class Test
			{
				public boolean foo(char ch)
				{
					return switch (ch)
					{
						case 'a', 'b', 'c' -> true;
						default -> false;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(40, 47, new ParameterAttribute("ch", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 69, 71);
			expected.allocateNode(NodeType.CHAR_LITERAL, 85, 88);
			expected.allocateNode(NodeType.CHAR_LITERAL, 90, 93);
			expected.allocateNode(NodeType.CHAR_LITERAL, 95, 98);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 102, 106);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 122, 127);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 61, 132);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 54, 133);
			expected.allocateNode(NodeType.BLOCK, 50, 136);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 136);
			expected.allocateClassDeclaration(7, 138, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 139);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with single case label and arrow syntax.
	 * Tests basic switch expression form with one case per branch.
	 */
	@Test
	public void testSingleCaseWithArrow()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 -> 10;
						case 2 -> 20;
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
			expected.allocateNode(NodeType.INTEGER_LITERAL, 83, 85);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 95, 96);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 100, 102);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 118, 119);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 124);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 125);
			expected.allocateNode(NodeType.BLOCK, 44, 128);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 128);
			expected.allocateClassDeclaration(7, 130, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 131);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression returning string values.
	 * Tests switch expression with string literal results.
	 */
	@Test
	public void testSwitchExpressionReturningStrings()
	{
		String source = """
			public class Test
			{
				public String foo(int x)
				{
					return switch (x)
					{
						case 1 -> "one";
						case 2 -> "two";
						default -> "other";
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
			expected.allocateNode(NodeType.STRING_LITERAL, 86, 91);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 101, 102);
			expected.allocateNode(NodeType.STRING_LITERAL, 106, 111);
			expected.allocateNode(NodeType.STRING_LITERAL, 127, 134);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 58, 139);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 51, 140);
			expected.allocateNode(NodeType.BLOCK, 47, 143);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 143);
			expected.allocateClassDeclaration(7, 145, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 146);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with only default case.
	 * Tests minimal switch expression that always returns the default.
	 */
	@Test
	public void testSwitchExpressionDefaultOnly()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
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
			expected.allocateNode(NodeType.INTEGER_LITERAL, 84, 85);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 90);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 91);
			expected.allocateNode(NodeType.BLOCK, 44, 94);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 94);
			expected.allocateClassDeclaration(7, 96, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 97);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression using boolean selector.
	 * Tests switch expression over boolean type values.
	 */
	@Test
	public void testSwitchExpressionWithBooleanSelector()
	{
		String source = """
			public class Test
			{
				public String foo(boolean b)
				{
					return switch (b)
					{
						case true -> "yes";
						case false -> "no";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(39, 48, new ParameterAttribute("b", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 70, 71);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 85, 89);
			expected.allocateNode(NodeType.STRING_LITERAL, 93, 98);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 108, 113);
			expected.allocateNode(NodeType.STRING_LITERAL, 117, 121);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 62, 126);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 55, 127);
			expected.allocateNode(NodeType.BLOCK, 51, 130);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 130);
			expected.allocateClassDeclaration(7, 132, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 133);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of nested switch expressions.
	 * Tests a switch expression containing another switch expression as a result value.
	 */
	@Test
	public void testNestedSwitchExpression()
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
							case 1 -> 11;
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
			expected.allocateNode(NodeType.INTEGER_LITERAL, 120, 122);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 139, 141);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 90, 147);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 163, 164);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 62, 169);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 55, 170);
			expected.allocateNode(NodeType.BLOCK, 51, 173);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 173);
			expected.allocateClassDeclaration(7, 175, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 176);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression in variable assignment.
	 * Tests switch expression used to initialize a variable.
	 */
	@Test
	public void testSwitchExpressionInVariableAssignment()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
					int result = switch (x)
					{
						case 1 -> 100;
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
			expected.allocateNode(NodeType.INTEGER_LITERAL, 90, 93);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 109, 110);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 62, 115);
			expected.allocateNode(NodeType.BLOCK, 45, 119);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 119);
			expected.allocateClassDeclaration(7, 121, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 122);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
	/**
	 * Validates parsing of switch expression used as a method argument.
	 * Tests that CASE tokens do not trigger errors when nested in method argument context.
	 */
	@Test
	public void testSwitchExpressionAsMethodArgument()
	{
		String source = """
			public class Test
			{
				public void process(String value)
				{
				}

				public void foo(int x)
				{
					process(switch (x)
					{
						case 1 -> "one";
						default -> "other";
					});
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateParameterDeclaration(41, 53, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 56, 60);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 60);
			expected.allocateParameterDeclaration(79, 84, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 98);
			expected.allocateNode(NodeType.IDENTIFIER, 91, 98);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 108);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 122, 123);
			expected.allocateNode(NodeType.STRING_LITERAL, 127, 132);
			expected.allocateNode(NodeType.STRING_LITERAL, 148, 155);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 99, 160);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 91, 161);
			expected.allocateNode(NodeType.BLOCK, 87, 165);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 63, 165);
			expected.allocateClassDeclaration(7, 167, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 168);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression used in a ternary conditional.
	 * Tests that ELSE tokens do not leak into expression context when parsing ternary.
	 */
	@Test
	public void testSwitchExpressionInTernaryCondition()
	{
		String source = """
			public class Test
			{
				public String foo(boolean flag, int x)
				{
					return flag ? switch (x)
					{
						case 1 -> "first";
						default -> "other";
					} : "fallback";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(39, 51, new ParameterAttribute("flag", false, false, false));
			expected.allocateParameterDeclaration(53, 58, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 72, 76);
			expected.allocateNode(NodeType.IDENTIFIER, 87, 88);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 102, 103);
			expected.allocateNode(NodeType.STRING_LITERAL, 107, 114);
			expected.allocateNode(NodeType.STRING_LITERAL, 130, 137);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 79, 142);
			expected.allocateNode(NodeType.STRING_LITERAL, 145, 155);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 72, 155);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 65, 156);
			expected.allocateNode(NodeType.BLOCK, 61, 159);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 159);
			expected.allocateClassDeclaration(7, 161, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 162);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with method chain on result.
	 * Tests that method invocation after switch expression works correctly.
	 */
	@Test
	public void testSwitchExpressionWithMethodChain()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 -> "first";
						default -> "other";
					}.length();
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
			expected.allocateNode(NodeType.STRING_LITERAL, 83, 90);
			expected.allocateNode(NodeType.STRING_LITERAL, 106, 113);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 118);
			expected.allocateNode(NodeType.FIELD_ACCESS, 55, 125);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 55, 127);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 128);
			expected.allocateNode(NodeType.BLOCK, 44, 131);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 131);
			expected.allocateClassDeclaration(7, 133, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 134);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression used as operand in binary operation.
	 * Tests that switch expression can be used as left operand of string concatenation.
	 */
	@Test
	public void testSwitchExpressionInBinaryOperation()
	{
		String source = """
			public class Test
			{
				public String foo(int x)
				{
					return switch (x)
					{
						case 1 -> "one";
						default -> "other";
					} + " suffix";
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
			expected.allocateNode(NodeType.STRING_LITERAL, 86, 91);
			expected.allocateNode(NodeType.STRING_LITERAL, 107, 114);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 58, 119);
			expected.allocateNode(NodeType.STRING_LITERAL, 122, 131);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 58, 131);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 51, 132);
			expected.allocateNode(NodeType.BLOCK, 47, 135);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 135);
			expected.allocateClassDeclaration(7, 137, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 138);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
	/**
	 * Validates parsing of case body containing a ternary expression.
	 * Tests that conditional expressions within case arrows parse correctly.
	 */
	@Test
	public void testCaseBodyWithTernaryExpression()
	{
		String source = """
			public class Test
			{
				public String foo(int x, boolean flag)
				{
					return switch (x)
					{
						case 1 -> flag ? "yes" : "no";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(39, 44, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(46, 58, new ParameterAttribute("flag", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 80, 81);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 95, 96);
			expected.allocateNode(NodeType.IDENTIFIER, 100, 104);
			expected.allocateNode(NodeType.STRING_LITERAL, 107, 112);
			expected.allocateNode(NodeType.STRING_LITERAL, 115, 119);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 100, 119);
			expected.allocateNode(NodeType.STRING_LITERAL, 135, 142);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 72, 147);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 65, 148);
			expected.allocateNode(NodeType.BLOCK, 61, 151);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 151);
			expected.allocateClassDeclaration(7, 153, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 154);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of case body containing a method chain.
	 * Tests that chained method invocations in case arrows are correctly parsed.
	 */
	@Test
	public void testCaseBodyWithMethodChain()
	{
		String source = """
			public class Test
			{
				public String foo(String s)
				{
					return switch (s.length())
					{
						case 0 -> "";
						default -> s.strip().toLowerCase();
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 45);
			expected.allocateParameterDeclaration(39, 47, new ParameterAttribute("s", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 69, 70);
			expected.allocateNode(NodeType.FIELD_ACCESS, 69, 77);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 69, 79);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 93, 94);
			expected.allocateNode(NodeType.STRING_LITERAL, 98, 100);
			expected.allocateNode(NodeType.IDENTIFIER, 116, 117);
			expected.allocateNode(NodeType.FIELD_ACCESS, 116, 123);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 116, 125);
			expected.allocateNode(NodeType.FIELD_ACCESS, 116, 137);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 116, 139);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 61, 144);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 54, 145);
			expected.allocateNode(NodeType.BLOCK, 50, 148);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 148);
			expected.allocateClassDeclaration(7, 150, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 151);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of arrow case with if-else inside block.
	 * Tests that ELSE in case block does not leak into parent context.
	 */
	@Test
	public void testArrowCaseWithIfElseInBlock()
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
							if (x > 0)
								yield 100;
							else
								yield 200;
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
			expected.allocateNode(NodeType.IDENTIFIER, 96, 97);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 100, 101);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 96, 101);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 114, 117);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 108, 118);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 139, 142);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 133, 143);
			expected.allocateNode(NodeType.IF_STATEMENT, 92, 143);
			expected.allocateNode(NodeType.BLOCK, 86, 148);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 163, 164);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 169);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 170);
			expected.allocateNode(NodeType.BLOCK, 44, 173);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 173);
			expected.allocateClassDeclaration(7, 175, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 176);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of nested switch expressions at two levels.
	 * Tests that inner switch CASE tokens do not conflict with outer context.
	 */
	@Test
	public void testNestedSwitchExpressions()
	{
		String source = """
			public class Test
			{
				public int foo(int x, int y)
				{
					return switch (x)
					{
						case 0 -> switch (y)
						{
							case 0 -> 0;
							case 1 -> 1;
							default -> 2;
						};
						case 1 -> 10;
						default -> 20;
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
			expected.allocateNode(NodeType.INTEGER_LITERAL, 120, 121);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 132, 133);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 137, 138);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 155, 156);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 90, 162);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 172, 173);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 177, 179);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 195, 197);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 62, 202);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 55, 203);
			expected.allocateNode(NodeType.BLOCK, 51, 206);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 206);
			expected.allocateClassDeclaration(7, 208, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 209);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple switch expressions in sequence in same method.
	 * Tests that back-to-back switch expressions parse independently.
	 */
	@Test
	public void testMultipleSwitchExpressionsInSequence()
	{
		String source = """
			public class Test
			{
				public int foo(int x, int y)
				{
					int a = switch (x)
					{
						case 1 -> 10;
						default -> 0;
					};
					int b = switch (y)
					{
						case 1 -> 20;
						default -> 0;
					};
					return a + b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(43, 48, new ParameterAttribute("y", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 71, 72);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 86, 87);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 91, 93);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 109, 110);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 63, 115);
			expected.allocateNode(NodeType.IDENTIFIER, 135, 136);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 150, 151);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 155, 157);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 173, 174);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 127, 179);
			expected.allocateNode(NodeType.IDENTIFIER, 190, 191);
			expected.allocateNode(NodeType.IDENTIFIER, 194, 195);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 190, 195);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 183, 196);
			expected.allocateNode(NodeType.BLOCK, 51, 199);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 199);
			expected.allocateClassDeclaration(7, 201, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 202);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with lambda in case body.
	 * Tests that lambda arrow syntax does not conflict with case arrow.
	 */
	@Test
	public void testSwitchWithLambdaInCaseBody()
	{
		String source = """
			public class Test
			{
				public java.util.function.IntSupplier foo(int x)
				{
					return switch (x)
					{
						case 1 -> () -> 42;
						default -> () -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(63, 68, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 90, 91);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 105, 106);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 116, 118);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 110, 118);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 140, 141);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 134, 141);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 82, 146);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 75, 147);
			expected.allocateNode(NodeType.BLOCK, 71, 150);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 150);
			expected.allocateClassDeclaration(7, 152, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 153);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of three levels of nested switch expressions.
	 * Tests that deeply nested CASE tokens are correctly scoped.
	 */
	@Test
	public void testTriplyNestedSwitchExpression()
	{
		String source = """
			public class Test
			{
				public int foo(int a, int b, int c)
				{
					return switch (a)
					{
						case 1 -> switch (b)
						{
							case 1 -> switch (c)
							{
								case 1 -> 111;
								default -> 110;
							};
							default -> 100;
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
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("a", false, false, false));
			expected.allocateParameterDeclaration(43, 48, new ParameterAttribute("b", false, false, false));
			expected.allocateParameterDeclaration(50, 55, new ParameterAttribute("c", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 77, 78);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 92, 93);
			expected.allocateNode(NodeType.IDENTIFIER, 105, 106);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 122, 123);
			expected.allocateNode(NodeType.IDENTIFIER, 135, 136);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 154, 155);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 159, 162);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 180, 183);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 127, 190);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 207, 210);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 97, 216);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 232, 233);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 69, 238);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 62, 239);
			expected.allocateNode(NodeType.BLOCK, 58, 242);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 242);
			expected.allocateClassDeclaration(7, 244, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 245);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with JDK 21 pattern matching features.
	 * Tests type patterns with guards using {@code when} keyword.
	 */
	@Test
	public void testSwitchWithAllJDK21Patterns()
	{
		String source = """
			public class Test
			{
				public String foo(Object obj)
				{
					return switch (obj)
					{
						case null -> "null";
						case String s when s.isEmpty() -> "empty string";
						case String s -> "string: " + s;
						case Integer i when i > 0 -> "positive";
						case Integer i -> "non-positive";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 45);
			expected.allocateParameterDeclaration(39, 49, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 71, 74);
			expected.allocateNode(NodeType.STRING_LITERAL, 96, 102);
			expected.allocateNode(NodeType.IDENTIFIER, 126, 127);
			expected.allocateNode(NodeType.FIELD_ACCESS, 126, 135);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 126, 137);
			expected.allocateNode(NodeType.STRING_LITERAL, 141, 155);
			expected.allocateNode(NodeType.STRING_LITERAL, 177, 187);
			expected.allocateNode(NodeType.IDENTIFIER, 190, 191);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 177, 191);
			expected.allocateNode(NodeType.IDENTIFIER, 216, 217);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 220, 221);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 216, 221);
			expected.allocateNode(NodeType.STRING_LITERAL, 225, 235);
			expected.allocateNode(NodeType.STRING_LITERAL, 258, 272);
			expected.allocateNode(NodeType.STRING_LITERAL, 288, 295);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 63, 300);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 56, 301);
			expected.allocateNode(NodeType.BLOCK, 52, 304);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 304);
			expected.allocateClassDeclaration(7, 306, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 307);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression inside lambda expression body.
	 * Tests that switch in lambda context parses without token conflicts.
	 */
	@Test
	public void testSwitchInLambdaBody()
	{
		String source = """
			public class Test
			{
				public java.util.function.Function<Integer, String> foo()
				{
					return x -> switch (x)
					{
						case 1 -> "one";
						default -> "other";
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
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 71);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 71);
			expected.allocateNode(NodeType.IDENTIFIER, 104, 105);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 119, 120);
			expected.allocateNode(NodeType.STRING_LITERAL, 124, 129);
			expected.allocateNode(NodeType.STRING_LITERAL, 145, 152);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 96, 157);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 91, 157);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 84, 158);
			expected.allocateNode(NodeType.BLOCK, 80, 161);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 161);
			expected.allocateClassDeclaration(7, 163, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 164);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of case body with array access expression.
	 * Tests that bracket syntax in case body does not conflict with block parsing.
	 */
	@Test
	public void testSwitchWithArrayAccessInCaseBody()
	{
		String source = """
			public class Test
			{
				public int foo(int x, int[] arr)
				{
					return switch (x)
					{
						case 0 -> arr[0];
						case 1 -> arr[1];
						default -> arr[arr.length - 1];
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(36, 41, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(43, 52, new ParameterAttribute("arr", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 74, 75);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 89, 90);
			expected.allocateNode(NodeType.IDENTIFIER, 94, 97);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 98, 99);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 94, 100);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 110, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 115, 118);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 119, 120);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 115, 121);
			expected.allocateNode(NodeType.IDENTIFIER, 137, 140);
			expected.allocateNode(NodeType.IDENTIFIER, 141, 144);
			expected.allocateNode(NodeType.FIELD_ACCESS, 141, 151);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 154, 155);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 141, 155);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 137, 156);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 66, 161);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 59, 162);
			expected.allocateNode(NodeType.BLOCK, 55, 165);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 165);
			expected.allocateClassDeclaration(7, 167, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 168);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of if-else statement inside switch case block.
	 * Tests that ELSE token inside case block does not affect parent switch.
	 */
	@Test
	public void testIfElseInsideSwitchCaseBlock()
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
							if (flag)
								yield 10;
							else
								yield 20;
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
			expected.allocateNode(NodeType.IDENTIFIER, 110, 114);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 127, 129);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 121, 130);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 151, 153);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 145, 154);
			expected.allocateNode(NodeType.IF_STATEMENT, 106, 154);
			expected.allocateNode(NodeType.BLOCK, 100, 159);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 174, 175);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 69, 180);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 62, 181);
			expected.allocateNode(NodeType.BLOCK, 58, 184);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 184);
			expected.allocateClassDeclaration(7, 186, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 187);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of case bodies with throw expressions.
	 * Tests that throw in case arrow is correctly parsed as expression.
	 */
	@Test
	public void testSwitchWithThrowExpressions()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1 -> 10;
						case 2 -> throw new IllegalStateException("invalid");
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
			expected.allocateNode(NodeType.INTEGER_LITERAL, 83, 85);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 95, 96);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 110, 131);
			expected.allocateNode(NodeType.STRING_LITERAL, 132, 141);
			expected.allocateNode(NodeType.OBJECT_CREATION, 106, 142);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 158, 159);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 164);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 165);
			expected.allocateNode(NodeType.BLOCK, 44, 168);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 168);
			expected.allocateClassDeclaration(7, 170, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 171);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of complex binary expressions in case bodies.
	 * Tests operator precedence within case arrow expressions.
	 */
	@Test
	public void testComplexBinaryExpressionsInCaseBodies()
	{
		String source = """
			public class Test
			{
				public int foo(int x, int y)
				{
					return switch (x)
					{
						case 1 -> y * 2 + 3;
						case 2 -> y << 1 | 0xFF;
						default -> y % 2 == 0 ? y : y + 1;
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
			expected.allocateNode(NodeType.IDENTIFIER, 90, 91);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 94, 95);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 90, 95);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 98, 99);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 90, 99);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 109, 110);
			expected.allocateNode(NodeType.IDENTIFIER, 114, 115);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 119, 120);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 114, 120);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 123, 127);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 114, 127);
			expected.allocateNode(NodeType.IDENTIFIER, 143, 144);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 147, 148);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 143, 148);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 152, 153);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 143, 153);
			expected.allocateNode(NodeType.IDENTIFIER, 156, 157);
			expected.allocateNode(NodeType.IDENTIFIER, 160, 161);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 164, 165);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 160, 165);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 143, 165);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 62, 170);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 55, 171);
			expected.allocateNode(NodeType.BLOCK, 51, 174);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 174);
			expected.allocateClassDeclaration(7, 176, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 177);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of factory pattern using switch expression.
	 * Tests switch expression returning new object instances.
	 */
	@Test
	public void testFactoryPatternSwitchExpression()
	{
		String source = """
			public class Test
			{
				public Object create(String type)
				{
					return switch (type)
					{
						case "list" -> new java.util.ArrayList<>();
						case "set" -> new java.util.HashSet<>();
						default -> new Object();
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateParameterDeclaration(42, 53, new ParameterAttribute("type", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 75, 79);
			expected.allocateNode(NodeType.STRING_LITERAL, 93, 99);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 107, 126);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 107, 128);
			expected.allocateNode(NodeType.OBJECT_CREATION, 103, 130);
			expected.allocateNode(NodeType.STRING_LITERAL, 140, 145);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 153, 170);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 153, 172);
			expected.allocateNode(NodeType.OBJECT_CREATION, 149, 174);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 194, 200);
			expected.allocateNode(NodeType.OBJECT_CREATION, 190, 202);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 67, 207);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 60, 208);
			expected.allocateNode(NodeType.BLOCK, 56, 211);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 211);
			expected.allocateClassDeclaration(7, 213, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 214);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of state machine pattern using switch expression.
	 * Tests String-based switch returning next state with ternary expressions.
	 */
	@Test
	public void testStateMachineSwitchExpression()
	{
		String source = """
			public class Test
			{
				public String nextState(String current, String event)
				{
					return switch (current)
					{
						case "IDLE" -> event.equals("start") ? "RUNNING" : current;
						case "RUNNING" -> event.equals("stop") ? "STOPPED" : current;
						default -> current;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 45, 51);
			expected.allocateParameterDeclaration(45, 59, new ParameterAttribute("current", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 61, 67);
			expected.allocateParameterDeclaration(61, 73, new ParameterAttribute("event", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 95, 102);
			expected.allocateNode(NodeType.STRING_LITERAL, 116, 122);
			expected.allocateNode(NodeType.IDENTIFIER, 126, 131);
			expected.allocateNode(NodeType.FIELD_ACCESS, 126, 138);
			expected.allocateNode(NodeType.STRING_LITERAL, 139, 146);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 126, 147);
			expected.allocateNode(NodeType.STRING_LITERAL, 150, 159);
			expected.allocateNode(NodeType.IDENTIFIER, 162, 169);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 126, 169);
			expected.allocateNode(NodeType.STRING_LITERAL, 179, 188);
			expected.allocateNode(NodeType.IDENTIFIER, 192, 197);
			expected.allocateNode(NodeType.FIELD_ACCESS, 192, 204);
			expected.allocateNode(NodeType.STRING_LITERAL, 205, 211);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 192, 212);
			expected.allocateNode(NodeType.STRING_LITERAL, 215, 224);
			expected.allocateNode(NodeType.IDENTIFIER, 227, 234);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 192, 234);
			expected.allocateNode(NodeType.IDENTIFIER, 250, 257);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 87, 262);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 80, 263);
			expected.allocateNode(NodeType.BLOCK, 76, 266);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 266);
			expected.allocateClassDeclaration(7, 268, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 269);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with enum declaration in same class.
	 * Tests that enum declaration followed by method with switch expression works correctly.
	 */
	@Test
	public void testSwitchWithEnumInSameClass()
	{
		String source = """
			public class Test
			{
				enum Priority
				{
					LOW,
					MEDIUM,
					HIGH
				}

				public String describe(String level)
				{
					return switch (level)
					{
						case "low" -> "low priority";
						case "high" -> "high priority";
						default -> "unknown";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ENUM_CONSTANT, 40, 43);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 47, 53);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 57, 61);
			expected.allocateEnumDeclaration(21, 64, new TypeDeclarationAttribute("Priority"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 90, 96);
			expected.allocateParameterDeclaration(90, 102, new ParameterAttribute("level", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 124, 129);
			expected.allocateNode(NodeType.STRING_LITERAL, 143, 148);
			expected.allocateNode(NodeType.STRING_LITERAL, 152, 166);
			expected.allocateNode(NodeType.STRING_LITERAL, 176, 182);
			expected.allocateNode(NodeType.STRING_LITERAL, 186, 201);
			expected.allocateNode(NodeType.STRING_LITERAL, 217, 226);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 116, 231);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 109, 232);
			expected.allocateNode(NodeType.BLOCK, 105, 235);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 67, 235);
			expected.allocateClassDeclaration(7, 237, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 238);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with block comment before switch keyword.
	 * Tests that comments before switch are parsed correctly.
	 */
	@Test
	public void testCommentsInSwitchPositions()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return /* before switch */ switch (x)
					{
						case 1 -> 10;
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
			expected.allocateNode(NodeType.BLOCK_COMMENT, 55, 74);
			expected.allocateNode(NodeType.IDENTIFIER, 83, 84);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 98, 99);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 103, 105);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 121, 122);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 75, 127);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 128);
			expected.allocateNode(NodeType.BLOCK, 44, 131);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 131);
			expected.allocateClassDeclaration(7, 133, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 134);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch combining arrow and colon syntax.
	 * This tests the colon-style cases within a switch expression.
	 */
	@Test
	public void testMixedArrowColonSyntax()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 1:
						case 2:
							yield 100;
						case 3:
							yield 200;
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
			expected.allocateNode(NodeType.INTEGER_LITERAL, 89, 90);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 102, 105);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 96, 106);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 115, 116);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 128, 131);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 122, 132);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 155, 156);
			expected.allocateNode(NodeType.YIELD_STATEMENT, 149, 157);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 55, 161);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 48, 162);
			expected.allocateNode(NodeType.BLOCK, 44, 165);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 165);
			expected.allocateClassDeclaration(7, 167, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 168);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression used in return with method call.
	 * Tests switch expression combined with string formatting.
	 */
	@Test
	public void testSwitchExpressionWithStringFormat()
	{
		String source = """
			public class Test
			{
				public String foo(int code)
				{
					return String.format("Result: %s", switch (code)
					{
						case 200 -> "OK";
						case 404 -> "Not Found";
						default -> "Unknown";
					});
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(39, 47, new ParameterAttribute("code", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 67);
			expected.allocateNode(NodeType.FIELD_ACCESS, 61, 74);
			expected.allocateNode(NodeType.STRING_LITERAL, 75, 87);
			expected.allocateNode(NodeType.IDENTIFIER, 97, 101);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 115, 118);
			expected.allocateNode(NodeType.STRING_LITERAL, 122, 126);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 136, 139);
			expected.allocateNode(NodeType.STRING_LITERAL, 143, 154);
			expected.allocateNode(NodeType.STRING_LITERAL, 170, 179);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 89, 184);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 61, 185);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 54, 186);
			expected.allocateNode(NodeType.BLOCK, 50, 189);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 189);
			expected.allocateClassDeclaration(7, 191, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 192);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression assigning to final variable.
	 * Tests common pattern of using switch for conditional initialization.
	 */
	@Test
	public void testSwitchExpressionAssignedToFinalVariable()
	{
		String source = """
			public class Test
			{
				public void foo(int mode)
				{
					final int value = switch (mode)
					{
						case 0 -> 100;
						case 1 -> 200;
						default -> 0;
					};
					System.out.println(value);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 45, new ParameterAttribute("mode", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 82);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 96, 97);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 101, 104);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 114, 115);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 119, 122);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 138, 139);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 144);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 148, 166);
			expected.allocateNode(NodeType.IDENTIFIER, 148, 154);
			expected.allocateNode(NodeType.FIELD_ACCESS, 148, 158);
			expected.allocateNode(NodeType.FIELD_ACCESS, 148, 166);
			expected.allocateNode(NodeType.IDENTIFIER, 167, 172);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 148, 173);
			expected.allocateNode(NodeType.BLOCK, 48, 177);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 177);
			expected.allocateClassDeclaration(7, 179, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 180);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with null case.
	 * Tests switch expression with null case label and default.
	 */
	@Test
	public void testSwitchExpressionWithNullCase()
	{
		String source = """
			public class Test
			{
				public int length(String str)
				{
					return switch (str)
					{
						case null -> 0;
						case "empty" -> 0;
						default -> str.length();
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 45);
			expected.allocateParameterDeclaration(39, 49, new ParameterAttribute("str", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 71, 74);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 96, 97);
			expected.allocateNode(NodeType.STRING_LITERAL, 107, 114);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 118, 119);
			expected.allocateNode(NodeType.IDENTIFIER, 135, 138);
			expected.allocateNode(NodeType.FIELD_ACCESS, 135, 145);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 135, 147);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 63, 152);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 56, 153);
			expected.allocateNode(NodeType.BLOCK, 52, 156);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 156);
			expected.allocateClassDeclaration(7, 158, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 159);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
