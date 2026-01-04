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
 * Tests for parsing pattern matching instanceof expressions introduced in Java 16.
 */
public class PatternMatchingInstanceofTest
{
	/**
	 * Validates parsing of basic pattern matching instanceof with simple type and variable.
	 * Tests the most common case of pattern matching in if statements.
	 */
	@Test
	public void shouldParseBasicPatternMatchingInstanceof()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof String s)
					{
						System.out.println(s);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 76, 82);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 84);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 93, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 93, 99);
			expected.allocateNode(NodeType.FIELD_ACCESS, 93, 103);
			expected.allocateNode(NodeType.FIELD_ACCESS, 93, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 113);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 93, 114);
			expected.allocateNode(NodeType.BLOCK, 88, 119);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 119);
			expected.allocateNode(NodeType.BLOCK, 53, 122);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 122);
			expected.allocateClassDeclaration(7, 124, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 125);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of pattern matching instanceof with generic type.
	 * Tests handling of parameterized types in instanceof patterns.
	 */
	@Test
	public void shouldParsePatternMatchingWithGenerics()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof java.util.List<String> list)
					{
						System.out.println(list.size());
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 76, 90);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 97);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 97);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 76, 98);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 103);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 112, 130);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 118);
			expected.allocateNode(NodeType.FIELD_ACCESS, 112, 122);
			expected.allocateNode(NodeType.FIELD_ACCESS, 112, 130);
			expected.allocateNode(NodeType.IDENTIFIER, 131, 135);
			expected.allocateNode(NodeType.FIELD_ACCESS, 131, 140);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 131, 142);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 112, 143);
			expected.allocateNode(NodeType.BLOCK, 107, 148);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 148);
			expected.allocateNode(NodeType.BLOCK, 53, 151);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 151);
			expected.allocateClassDeclaration(7, 153, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 154);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of pattern matching instanceof with nested type reference.
	 * Tests qualified type names in instanceof patterns.
	 */
	@Test
	public void shouldParsePatternMatchingWithNestedType()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof java.util.Map.Entry entry)
					{
						System.out.println(entry.getKey());
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 76, 95);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 101);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 110, 128);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 116);
			expected.allocateNode(NodeType.FIELD_ACCESS, 110, 120);
			expected.allocateNode(NodeType.FIELD_ACCESS, 110, 128);
			expected.allocateNode(NodeType.IDENTIFIER, 129, 134);
			expected.allocateNode(NodeType.FIELD_ACCESS, 129, 141);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 129, 143);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 110, 144);
			expected.allocateNode(NodeType.BLOCK, 105, 149);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 149);
			expected.allocateNode(NodeType.BLOCK, 53, 152);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 152);
			expected.allocateClassDeclaration(7, 154, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 155);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of pattern matching instanceof in if condition.
	 * Tests the typical usage pattern in conditional statements.
	 */
	@Test
	public void shouldParsePatternMatchingInIfStatement()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof Integer i)
					{
						System.out.println(i * 2);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 76, 83);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 85);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 94, 112);
			expected.allocateNode(NodeType.IDENTIFIER, 94, 100);
			expected.allocateNode(NodeType.FIELD_ACCESS, 94, 104);
			expected.allocateNode(NodeType.FIELD_ACCESS, 94, 112);
			expected.allocateNode(NodeType.IDENTIFIER, 113, 114);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 117, 118);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 113, 118);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 94, 119);
			expected.allocateNode(NodeType.BLOCK, 89, 124);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 124);
			expected.allocateNode(NodeType.BLOCK, 53, 127);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 127);
			expected.allocateClassDeclaration(7, 129, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 130);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of pattern matching instanceof in logical AND expression.
	 * Tests pattern variable usage in compound boolean expressions.
	 */
	@Test
	public void shouldParsePatternMatchingInLogicalExpression()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof String s && s.length() > 0)
					{
						System.out.println(s);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 76, 82);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 84);
			expected.allocateNode(NodeType.IDENTIFIER, 88, 89);
			expected.allocateNode(NodeType.FIELD_ACCESS, 88, 96);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 88, 98);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 101, 102);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 88, 102);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 102);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 111, 129);
			expected.allocateNode(NodeType.IDENTIFIER, 111, 117);
			expected.allocateNode(NodeType.FIELD_ACCESS, 111, 121);
			expected.allocateNode(NodeType.FIELD_ACCESS, 111, 129);
			expected.allocateNode(NodeType.IDENTIFIER, 130, 131);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 111, 132);
			expected.allocateNode(NodeType.BLOCK, 106, 137);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 137);
			expected.allocateNode(NodeType.BLOCK, 53, 140);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 140);
			expected.allocateClassDeclaration(7, 142, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 143);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of pattern matching instanceof with array type.
	 * Tests pattern matching with array type references.
	 */
	@Test
	public void shouldParsePatternMatchingWithArrayType()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof String[] arr)
					{
						System.out.println(arr.length);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 76, 82);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 88);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 97, 115);
			expected.allocateNode(NodeType.IDENTIFIER, 97, 103);
			expected.allocateNode(NodeType.FIELD_ACCESS, 97, 107);
			expected.allocateNode(NodeType.FIELD_ACCESS, 97, 115);
			expected.allocateNode(NodeType.IDENTIFIER, 116, 119);
			expected.allocateNode(NodeType.FIELD_ACCESS, 116, 126);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 97, 127);
			expected.allocateNode(NodeType.BLOCK, 92, 132);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 132);
			expected.allocateNode(NodeType.BLOCK, 53, 135);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 135);
			expected.allocateClassDeclaration(7, 137, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 138);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of negated pattern matching instanceof.
	 * Tests pattern matching in negated conditions.
	 */
	@Test
	public void shouldParseNegatedPatternMatching()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (!(obj instanceof String s))
					{
						System.out.println("Not a string");
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 63, 66);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 78, 84);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 63, 86);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 61, 86);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 96, 114);
			expected.allocateNode(NodeType.IDENTIFIER, 96, 102);
			expected.allocateNode(NodeType.FIELD_ACCESS, 96, 106);
			expected.allocateNode(NodeType.FIELD_ACCESS, 96, 114);
			expected.allocateNode(NodeType.STRING_LITERAL, 115, 129);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 96, 130);
			expected.allocateNode(NodeType.BLOCK, 91, 135);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 135);
			expected.allocateNode(NodeType.BLOCK, 53, 138);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 138);
			expected.allocateClassDeclaration(7, 140, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 141);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of pattern matching instanceof in ternary expression.
	 * Tests pattern matching as part of conditional expressions.
	 */
	@Test
	public void shouldParsePatternMatchingInTernary()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					String result = obj instanceof String s ? s : "default";
					System.out.println(result);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 76);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 88, 94);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 73, 96);
			expected.allocateNode(NodeType.IDENTIFIER, 99, 100);
			expected.allocateNode(NodeType.STRING_LITERAL, 103, 112);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 73, 112);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 116, 134);
			expected.allocateNode(NodeType.IDENTIFIER, 116, 122);
			expected.allocateNode(NodeType.FIELD_ACCESS, 116, 126);
			expected.allocateNode(NodeType.FIELD_ACCESS, 116, 134);
			expected.allocateNode(NodeType.IDENTIFIER, 135, 141);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 116, 142);
			expected.allocateNode(NodeType.BLOCK, 53, 146);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 146);
			expected.allocateClassDeclaration(7, 148, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 149);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple pattern matching instanceof in same method.
	 * Tests handling of multiple instanceof patterns with different variables.
	 */
	@Test
	public void shouldParseMultiplePatternMatchingInstanceof()
	{
		String source = """
			public class Test
			{
				public void method(Object obj1, Object obj2)
				{
					if (obj1 instanceof String s && obj2 instanceof Integer i)
					{
						System.out.println(s + i);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 51, new ParameterAttribute("obj1", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 59);
			expected.allocateParameterDeclaration(53, 64, new ParameterAttribute("obj2", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 75, 79);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 97);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 75, 99);
			expected.allocateNode(NodeType.IDENTIFIER, 103, 107);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 119, 126);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 103, 128);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 75, 128);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 137, 155);
			expected.allocateNode(NodeType.IDENTIFIER, 137, 143);
			expected.allocateNode(NodeType.FIELD_ACCESS, 137, 147);
			expected.allocateNode(NodeType.FIELD_ACCESS, 137, 155);
			expected.allocateNode(NodeType.IDENTIFIER, 156, 157);
			expected.allocateNode(NodeType.IDENTIFIER, 160, 161);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 156, 161);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 137, 162);
			expected.allocateNode(NodeType.BLOCK, 132, 167);
			expected.allocateNode(NodeType.IF_STATEMENT, 71, 167);
			expected.allocateNode(NodeType.BLOCK, 67, 170);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 170);
			expected.allocateClassDeclaration(7, 172, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 173);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of traditional instanceof without pattern variable.
	 * Tests backward compatibility with pre-Java 16 instanceof syntax.
	 */
	@Test
	public void shouldParseTraditionalInstanceof()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof String)
					{
						System.out.println("It's a string");
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 76, 82);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 82);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 109);
			expected.allocateNode(NodeType.IDENTIFIER, 91, 97);
			expected.allocateNode(NodeType.FIELD_ACCESS, 91, 101);
			expected.allocateNode(NodeType.FIELD_ACCESS, 91, 109);
			expected.allocateNode(NodeType.STRING_LITERAL, 110, 125);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 91, 126);
			expected.allocateNode(NodeType.BLOCK, 86, 131);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 131);
			expected.allocateNode(NodeType.BLOCK, 53, 134);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 134);
			expected.allocateClassDeclaration(7, 136, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 137);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of pattern matching instanceof in while loop.
	 * Tests pattern matching in loop conditions.
	 */
	@Test
	public void shouldParsePatternMatchingInWhileLoop()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					Object obj = getObject();
					while (obj instanceof String s)
					{
						System.out.println(s);
						obj = getObject();
					}
				}
				private Object getObject()
				{
					return null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 53);
			expected.allocateNode(NodeType.IDENTIFIER, 60, 69);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 60, 71);
			expected.allocateNode(NodeType.IDENTIFIER, 82, 85);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 97, 103);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 82, 105);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 114, 132);
			expected.allocateNode(NodeType.IDENTIFIER, 114, 120);
			expected.allocateNode(NodeType.FIELD_ACCESS, 114, 124);
			expected.allocateNode(NodeType.FIELD_ACCESS, 114, 132);
			expected.allocateNode(NodeType.IDENTIFIER, 133, 134);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 114, 135);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 140, 143);
			expected.allocateNode(NodeType.IDENTIFIER, 140, 143);
			expected.allocateNode(NodeType.IDENTIFIER, 146, 155);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 146, 157);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 140, 157);
			expected.allocateNode(NodeType.BLOCK, 109, 162);
			expected.allocateNode(NodeType.WHILE_STATEMENT, 75, 162);
			expected.allocateNode(NodeType.BLOCK, 43, 165);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 165);
			expected.allocateNode(NodeType.NULL_LITERAL, 206, 210);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 199, 211);
			expected.allocateNode(NodeType.BLOCK, 195, 214);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 167, 214);
			expected.allocateClassDeclaration(7, 216, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 217);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
