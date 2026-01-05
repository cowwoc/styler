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
 * Tests for parsing primitive type patterns in switch expressions (JEP 507).
 */
public class PrimitiveTypePatternParserTest
{
	/**
	 * Validates parsing of a primitive type pattern with {@code int} type.
	 * Tests: {@code case int i ->}.
	 */
	@Test
	public void shouldParsePrimitiveTypePatternWithInt()
	{
		String source = """
			public class Test
			{
			    public void process(Object obj)
			    {
			        switch (obj)
			        {
			            case int i -> System.out.println(i);
			            default -> {}
			        }
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 119, 137);
			expected.allocateNode(NodeType.IDENTIFIER, 119, 125);
			expected.allocateNode(NodeType.FIELD_ACCESS, 119, 129);
			expected.allocateNode(NodeType.FIELD_ACCESS, 119, 137);
			expected.allocateNode(NodeType.IDENTIFIER, 138, 139);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 119, 140);
			expected.allocateNode(NodeType.BLOCK, 165, 167);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 70, 177);
			expected.allocateNode(NodeType.BLOCK, 60, 183);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 183);
			expected.allocateClassDeclaration(7, 185, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 186);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a primitive type pattern with {@code long} type.
	 * Tests: {@code case long l ->}.
	 */
	@Test
	public void shouldParsePrimitiveTypePatternWithLong()
	{
		String source = """
			public class Test
			{
			    public void process(Object obj)
			    {
			        switch (obj)
			        {
			            case long l -> System.out.println(l);
			            default -> {}
			        }
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 120, 138);
			expected.allocateNode(NodeType.IDENTIFIER, 120, 126);
			expected.allocateNode(NodeType.FIELD_ACCESS, 120, 130);
			expected.allocateNode(NodeType.FIELD_ACCESS, 120, 138);
			expected.allocateNode(NodeType.IDENTIFIER, 139, 140);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 120, 141);
			expected.allocateNode(NodeType.BLOCK, 166, 168);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 70, 178);
			expected.allocateNode(NodeType.BLOCK, 60, 184);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 184);
			expected.allocateClassDeclaration(7, 186, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 187);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a primitive type pattern with {@code double} type.
	 * Tests: {@code case double d ->}.
	 */
	@Test
	public void shouldParsePrimitiveTypePatternWithDouble()
	{
		String source = """
			public class Test
			{
			    public void process(Object obj)
			    {
			        switch (obj)
			        {
			            case double d -> System.out.println(d);
			            default -> {}
			        }
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 122, 140);
			expected.allocateNode(NodeType.IDENTIFIER, 122, 128);
			expected.allocateNode(NodeType.FIELD_ACCESS, 122, 132);
			expected.allocateNode(NodeType.FIELD_ACCESS, 122, 140);
			expected.allocateNode(NodeType.IDENTIFIER, 141, 142);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 122, 143);
			expected.allocateNode(NodeType.BLOCK, 168, 170);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 70, 180);
			expected.allocateNode(NodeType.BLOCK, 60, 186);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 186);
			expected.allocateClassDeclaration(7, 188, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 189);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a primitive type pattern with guard expression.
	 * Tests: {@code case int i when i > 0 ->}.
	 */
	@Test
	public void shouldParsePrimitiveTypePatternWithGuard()
	{
		String source = """
			public class Test
			{
			    public void process(Object obj)
			    {
			        switch (obj)
			        {
			            case int i when i > 0 -> System.out.println("positive");
			            default -> {}
			        }
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			// Guard expression: "i > 0"
			expected.allocateNode(NodeType.IDENTIFIER, 121, 122);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 125, 126);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 121, 126);
			// System.out.println("positive")
			expected.allocateNode(NodeType.QUALIFIED_NAME, 130, 148);
			expected.allocateNode(NodeType.IDENTIFIER, 130, 136);
			expected.allocateNode(NodeType.FIELD_ACCESS, 130, 140);
			expected.allocateNode(NodeType.FIELD_ACCESS, 130, 148);
			expected.allocateNode(NodeType.STRING_LITERAL, 149, 159);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 130, 160);
			expected.allocateNode(NodeType.BLOCK, 185, 187);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 70, 197);
			expected.allocateNode(NodeType.BLOCK, 60, 203);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 203);
			expected.allocateClassDeclaration(7, 205, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 206);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of primitive type pattern in instanceof expression.
	 * Tests: {@code obj instanceof int i}.
	 */
	@Test
	public void shouldParsePrimitiveTypePatternInInstanceof()
	{
		String source = """
			public class Test
			{
			    public void process(Object obj)
			    {
			        if (obj instanceof int i)
			        {
			            System.out.println(i);
			        }
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			// if (obj instanceof int i)
			expected.allocateNode(NodeType.IDENTIFIER, 74, 77);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 74, 94);
			// System.out.println(i) inside if block
			expected.allocateNode(NodeType.QUALIFIED_NAME, 118, 136);
			expected.allocateNode(NodeType.IDENTIFIER, 118, 124);
			expected.allocateNode(NodeType.FIELD_ACCESS, 118, 128);
			expected.allocateNode(NodeType.FIELD_ACCESS, 118, 136);
			expected.allocateNode(NodeType.IDENTIFIER, 137, 138);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 118, 139);
			expected.allocateNode(NodeType.BLOCK, 104, 150);
			expected.allocateNode(NodeType.IF_STATEMENT, 70, 150);
			expected.allocateNode(NodeType.BLOCK, 60, 156);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 156);
			expected.allocateClassDeclaration(7, 158, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 159);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of primitive type pattern with unnamed pattern variable.
	 * Tests: {@code case int _ ->}.
	 */
	@Test
	public void shouldParsePrimitiveTypePatternWithUnnamedVariable()
	{
		String source = """
			public class Test
			{
			    public void process(Object obj)
			    {
			        switch (obj)
			        {
			            case int _ -> System.out.println("int");
			            default -> {}
			        }
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 119, 137);
			expected.allocateNode(NodeType.IDENTIFIER, 119, 125);
			expected.allocateNode(NodeType.FIELD_ACCESS, 119, 129);
			expected.allocateNode(NodeType.FIELD_ACCESS, 119, 137);
			expected.allocateNode(NodeType.STRING_LITERAL, 138, 143);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 119, 144);
			expected.allocateNode(NodeType.BLOCK, 169, 171);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 70, 181);
			expected.allocateNode(NodeType.BLOCK, 60, 187);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 187);
			expected.allocateClassDeclaration(7, 189, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 190);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of all primitive types as patterns.
	 * Tests: {@code case boolean b}, {@code case byte b}, {@code case char c},
	 * {@code case short s}, {@code case float f}.
	 */
	@Test
	public void shouldParseAllPrimitiveTypePatterns()
	{
		String source = """
			public class Test
			{
			    public void process(Object obj)
			    {
			        switch (obj)
			        {
			            case boolean b -> {}
			            case byte by -> {}
			            case char c -> {}
			            case short s -> {}
			            case float f -> {}
			            default -> {}
			        }
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			// Each case has an empty block {}
			expected.allocateNode(NodeType.BLOCK, 123, 125);
			expected.allocateNode(NodeType.BLOCK, 154, 156);
			expected.allocateNode(NodeType.BLOCK, 184, 186);
			expected.allocateNode(NodeType.BLOCK, 215, 217);
			expected.allocateNode(NodeType.BLOCK, 246, 248);
			expected.allocateNode(NodeType.BLOCK, 272, 274);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 70, 284);
			expected.allocateNode(NodeType.BLOCK, 60, 290);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 290);
			expected.allocateClassDeclaration(7, 292, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 293);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of primitive type pattern with complex guard expression.
	 * Tests: {@code case double d when d > 0.0 && d < 100.0 ->}.
	 */
	@Test
	public void shouldParsePrimitiveTypePatternWithComplexGuard()
	{
		String source = """
			public class Test
			{
			    public void process(Object obj)
			    {
			        switch (obj)
			        {
			            case double d when d > 0.0 && d < 100.0 -> System.out.println("valid range");
			            default -> {}
			        }
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			// Guard: d > 0.0 && d < 100.0
			expected.allocateNode(NodeType.IDENTIFIER, 124, 125);
			expected.allocateNode(NodeType.DOUBLE_LITERAL, 128, 131);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 124, 131);
			expected.allocateNode(NodeType.IDENTIFIER, 135, 136);
			expected.allocateNode(NodeType.DOUBLE_LITERAL, 139, 144);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 135, 144);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 124, 144);
			// System.out.println("valid range")
			expected.allocateNode(NodeType.QUALIFIED_NAME, 148, 166);
			expected.allocateNode(NodeType.IDENTIFIER, 148, 154);
			expected.allocateNode(NodeType.FIELD_ACCESS, 148, 158);
			expected.allocateNode(NodeType.FIELD_ACCESS, 148, 166);
			expected.allocateNode(NodeType.STRING_LITERAL, 167, 180);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 148, 181);
			expected.allocateNode(NodeType.BLOCK, 206, 208);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 70, 218);
			expected.allocateNode(NodeType.BLOCK, 60, 224);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 224);
			expected.allocateClassDeclaration(7, 226, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 227);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
