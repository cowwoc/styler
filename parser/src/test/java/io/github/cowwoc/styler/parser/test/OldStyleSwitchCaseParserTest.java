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
 * Tests for parsing old-style switch statements with COLON case labels.
 * <p>
 * These tests verify the fix for treating COLON as a ternary operator in case labels.
 * Previously, the parser would call parseAssignment() which calls parseTernary(), and
 * parseTernary() would interpret the COLON after the case expression as a ternary operator,
 * causing "Unexpected X in expression" errors.
 */
public class OldStyleSwitchCaseParserTest
{
	/**
	 * Validates parsing of old-style switch with CASE following COLON (fallthrough).
	 */
	@Test
	public void shouldParseCaseFallthroughToAnotherCase()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
					switch (x)
					{
						case 1:
						case 2:
							break;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 57, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 72, 73);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 83, 84);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 90, 96);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 49, 100);
			expected.allocateNode(NodeType.BLOCK, 45, 103);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 103);
			expected.allocateClassDeclaration(7, 105, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 106);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of old-style switch with DEFAULT following COLON.
	 */
	@Test
	public void shouldParseCaseFallthroughToDefault()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
					switch (x)
					{
						case 1:
						default:
							break;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 57, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 72, 73);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 91, 97);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 49, 101);
			expected.allocateNode(NodeType.BLOCK, 45, 104);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 104);
			expected.allocateClassDeclaration(7, 106, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 107);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of old-style switch with BREAK following COLON.
	 */
	@Test
	public void shouldParseCaseWithBreak()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
					switch (x)
					{
						case 1:
							break;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 57, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 72, 73);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 79, 85);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 49, 89);
			expected.allocateNode(NodeType.BLOCK, 45, 92);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 92);
			expected.allocateClassDeclaration(7, 94, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 95);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of old-style switch with THROW following COLON.
	 */
	@Test
	public void shouldParseCaseWithThrow()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
					switch (x)
					{
						case 1:
							throw new RuntimeException();
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 57, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 72, 73);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 89, 105);
			expected.allocateNode(NodeType.OBJECT_CREATION, 85, 107);
			expected.allocateNode(NodeType.THROW_STATEMENT, 79, 108);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 49, 112);
			expected.allocateNode(NodeType.BLOCK, 45, 115);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 115);
			expected.allocateClassDeclaration(7, 117, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 118);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of old-style switch with WHILE following COLON.
	 */
	@Test
	public void shouldParseCaseWithWhile()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
					switch (x)
					{
						case 1:
							while (true)
								break;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 57, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 72, 73);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 86, 90);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 97, 103);
			expected.allocateNode(NodeType.WHILE_STATEMENT, 79, 103);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 49, 107);
			expected.allocateNode(NodeType.BLOCK, 45, 110);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 110);
			expected.allocateClassDeclaration(7, 112, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 113);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of old-style switch with multiple case labels falling through.
	 */
	@Test
	public void shouldParseMultipleCaseLabelsFallthrough()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
					switch (x)
					{
						case 1:
						case 2:
						case 3:
							System.out.println("small");
							break;
						default:
							System.out.println("other");
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 57, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 72, 73);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 83, 84);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 94, 95);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 101, 119);
			expected.allocateNode(NodeType.IDENTIFIER, 101, 107);
			expected.allocateNode(NodeType.FIELD_ACCESS, 101, 111);
			expected.allocateNode(NodeType.FIELD_ACCESS, 101, 119);
			expected.allocateNode(NodeType.STRING_LITERAL, 120, 127);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 101, 128);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 134, 140);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 157, 175);
			expected.allocateNode(NodeType.IDENTIFIER, 157, 163);
			expected.allocateNode(NodeType.FIELD_ACCESS, 157, 167);
			expected.allocateNode(NodeType.FIELD_ACCESS, 157, 175);
			expected.allocateNode(NodeType.STRING_LITERAL, 176, 183);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 157, 184);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 49, 189);
			expected.allocateNode(NodeType.BLOCK, 45, 192);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 192);
			expected.allocateClassDeclaration(7, 194, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 195);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of nested switch inside default with fall-through comments.
	 * <p>
	 * This pattern occurs in Spring Framework's CodeEmitter.java and similar files.
	 * The {@code /* fall through * /} comment after a statement followed by another case label
	 * was causing "Unexpected token in expression: CASE" errors.
	 */
	@Test
	public void shouldParseNestedSwitchWithFallthroughComments()
	{
		String source = """
			public class Test
			{
				int intOp;
				void swap() {}

				public void foo(int x, int mode)
				{
					switch (x)
					{
						default:
							switch (mode)
							{
								case EQ: intOp = 1; break;
								case NE: intOp = 2; break;
								case GE: swap(); /* fall through */
								case LT: intOp = 3; break;
								case LE: swap(); /* fall through */
								case GT: intOp = 4; break;
							}
					}
				}

				static final int EQ = 0, NE = 1, GE = 2, LT = 3, LE = 4, GT = 5;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Using placeholder positions - will be verified by running test
			expected.allocateNode(NodeType.FIELD_DECLARATION, 0, 0);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 0);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 0, 0);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 0, 0);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 0);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 0, 0);
			expected.allocateClassDeclaration(0, 0, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 0);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of old-style switch with qualified constant references.
	 */
	@Test
	public void shouldParseCaseWithQualifiedConstant()
	{
		String source = """
			public class Test
			{
				public void foo(Status s)
				{
					switch (s)
					{
						case Status.ACTIVE:
							break;
						case Status.INACTIVE:
							break;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 43);
			expected.allocateParameterDeclaration(37, 45, new ParameterAttribute("s", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 60, 61);
			expected.allocateNode(NodeType.IDENTIFIER, 75, 81);
			expected.allocateNode(NodeType.FIELD_ACCESS, 75, 88);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 94, 100);
			expected.allocateNode(NodeType.IDENTIFIER, 109, 115);
			expected.allocateNode(NodeType.FIELD_ACCESS, 109, 124);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 130, 136);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 52, 140);
			expected.allocateNode(NodeType.BLOCK, 48, 143);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 143);
			expected.allocateClassDeclaration(7, 145, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 146);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
