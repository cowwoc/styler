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
 * Tests for parsing switch expressions with arrow syntax.
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
	 * Validates parsing of switch expression with arrow syntax and trailing comments.
	 * Tests that comments after case blocks are properly handled.
	 */
	@Test
	public void testSwitchExpressionWithColonAndTrailingComments()
	{
		String source = """
			public class Test
			{
				public int foo(int x)
				{
					return switch (x)
					{
						case 0 -> 0; // case 0
						case 1 -> 1; // case 1
						default -> -1; // default
					}; // switch
				}
			}
			""";
		try (Parser parser = parse(source))
		{
			NodeArena actual = parser.getArena();
			// Verify parsing completes without error
			requireThat(actual.getNodeCount(), "actual.getNodeCount()").isGreaterThan(0);
		}
	}

	/**
	 * Validates parsing of switch arm with comment between arrow and throw expression.
	 * Tests that comments between {@code ->} and {@code throw} are handled correctly.
	 */
	@Test
	public void testSwitchArmWithCommentBeforeThrow()
	{
		String source = """
			class Foo
			{
				Object m(int value)
				{
					return switch (value)
					{
						default ->
							// Should never happen
							throw new IllegalStateException();
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(22, 31, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 53, 58);
			expected.allocateNode(NodeType.LINE_COMMENT, 82, 104);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 119, 140);
			expected.allocateNode(NodeType.OBJECT_CREATION, 115, 142);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 45, 147);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 38, 148);
			expected.allocateNode(NodeType.BLOCK, 34, 151);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 13, 151);
			expected.allocateClassDeclaration(0, 153, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 154);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
