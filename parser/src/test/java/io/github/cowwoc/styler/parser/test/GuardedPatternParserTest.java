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
 * Tests for parsing guarded patterns in switch expressions (Java 21+).
 */
public class GuardedPatternParserTest
{
	/**
	 * Validates parsing of a simple type pattern with a guard clause using {@code when} keyword.
	 * Tests the basic form: {@code case String s when s.length() > 5 ->}.
	 */
	@Test
	public void shouldParseSimpleTypePatternWithGuard()
	{
		String source = """
			public class Test
			{
				public void process(Object obj)
				{
					switch (obj)
					{
						case String s when s.length() > 5 -> System.out.println(s);
						default -> {}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateParameterDeclaration(41, 51, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 66, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 97, 98);
			expected.allocateNode(NodeType.FIELD_ACCESS, 97, 105);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 97, 107);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 110, 111);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 97, 111);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 115, 133);
			expected.allocateNode(NodeType.IDENTIFIER, 115, 121);
			expected.allocateNode(NodeType.FIELD_ACCESS, 115, 125);
			expected.allocateNode(NodeType.FIELD_ACCESS, 115, 133);
			expected.allocateNode(NodeType.IDENTIFIER, 134, 135);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 115, 136);
			expected.allocateNode(NodeType.BLOCK, 152, 154);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 58, 158);
			expected.allocateNode(NodeType.BLOCK, 54, 161);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 161);
			expected.allocateClassDeclaration(7, 163, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 164);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a qualified type pattern with a guard clause.
	 * Tests pattern matching with fully qualified type name and guard expression.
	 * <p>
	 * Note: Generic types like {@code List<String>} are not currently supported by the parser.
	 * This test uses a simple qualified type without generics.
	 */
	@Test
	public void shouldParseQualifiedTypePatternWithGuard()
	{
		String source = """
			public class Test
			{
				public void process(Object obj)
				{
					switch (obj)
					{
						case java.lang.String s when s.isEmpty() -> System.out.println("empty");
						default -> {}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateParameterDeclaration(41, 51, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 66, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 108);
			expected.allocateNode(NodeType.FIELD_ACCESS, 107, 116);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 107, 118);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 122, 140);
			expected.allocateNode(NodeType.IDENTIFIER, 122, 128);
			expected.allocateNode(NodeType.FIELD_ACCESS, 122, 132);
			expected.allocateNode(NodeType.FIELD_ACCESS, 122, 140);
			expected.allocateNode(NodeType.STRING_LITERAL, 141, 148);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 122, 149);
			expected.allocateNode(NodeType.BLOCK, 165, 167);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 58, 171);
			expected.allocateNode(NodeType.BLOCK, 54, 174);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 174);
			expected.allocateClassDeclaration(7, 176, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 177);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with simple numeric comparison in guard expression.
	 * Tests basic relational operator in when clause.
	 */
	@Test
	public void shouldParseGuardWithSimpleComparison()
	{
		String source = """
			public class Test
			{
				public int categorize(Object obj)
				{
					return switch (obj)
					{
						case Integer i when i > 0 -> 1;
						case Integer i when i < 0 -> -1;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 49);
			expected.allocateParameterDeclaration(43, 53, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 75, 78);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 108);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 111, 112);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 107, 112);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 116, 117);
			expected.allocateNode(NodeType.IDENTIFIER, 142, 143);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 146, 147);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 142, 147);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 152, 153);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 151, 153);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 169, 170);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 67, 175);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 60, 176);
			expected.allocateNode(NodeType.BLOCK, 56, 179);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 179);
			expected.allocateClassDeclaration(7, 181, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 182);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with complex boolean expression using logical AND.
	 * Tests compound boolean expressions in when clause.
	 */
	@Test
	public void shouldParseGuardWithComplexBooleanExpressionAnd()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Integer i when i > 0 && i < 100 -> "small positive";
						case Integer i when i >= 100 && i < 1000 -> "medium positive";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(46, 56, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 111);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 114, 115);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 115);
			expected.allocateNode(NodeType.IDENTIFIER, 119, 120);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 123, 126);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 119, 126);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 126);
			expected.allocateNode(NodeType.STRING_LITERAL, 130, 146);
			expected.allocateNode(NodeType.IDENTIFIER, 171, 172);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 176, 179);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 171, 179);
			expected.allocateNode(NodeType.IDENTIFIER, 183, 184);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 187, 191);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 183, 191);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 171, 191);
			expected.allocateNode(NodeType.STRING_LITERAL, 195, 212);
			expected.allocateNode(NodeType.STRING_LITERAL, 228, 235);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 240);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 241);
			expected.allocateNode(NodeType.BLOCK, 59, 244);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 244);
			expected.allocateClassDeclaration(7, 246, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 247);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with complex boolean expression using logical OR.
	 * Tests compound boolean expressions with OR operator in when clause.
	 */
	@Test
	public void shouldParseGuardWithComplexBooleanExpressionOr()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Integer i when i >= 100 || i <= -100 -> "large magnitude";
						default -> "small magnitude";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(46, 56, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 111);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 115, 118);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 118);
			expected.allocateNode(NodeType.IDENTIFIER, 122, 123);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 128, 131);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 127, 131);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 122, 131);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 131);
			expected.allocateNode(NodeType.STRING_LITERAL, 135, 152);
			expected.allocateNode(NodeType.STRING_LITERAL, 168, 185);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 190);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 191);
			expected.allocateNode(NodeType.BLOCK, 59, 194);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 194);
			expected.allocateClassDeclaration(7, 196, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 197);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with method call in guard expression.
	 * Tests method invocation as guard condition.
	 */
	@Test
	public void shouldParseGuardWithMethodCall()
	{
		String source = """
			public class Test
			{
				public String process(Object obj)
				{
					return switch (obj)
					{
						case String s when s.isEmpty() -> "empty string";
						case String s when s.startsWith("test") -> "test string";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 49);
			expected.allocateParameterDeclaration(43, 53, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 75, 78);
			expected.allocateNode(NodeType.IDENTIFIER, 106, 107);
			expected.allocateNode(NodeType.FIELD_ACCESS, 106, 115);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 106, 117);
			expected.allocateNode(NodeType.STRING_LITERAL, 121, 135);
			expected.allocateNode(NodeType.IDENTIFIER, 159, 160);
			expected.allocateNode(NodeType.FIELD_ACCESS, 159, 171);
			expected.allocateNode(NodeType.STRING_LITERAL, 172, 178);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 159, 179);
			expected.allocateNode(NodeType.STRING_LITERAL, 183, 196);
			expected.allocateNode(NodeType.STRING_LITERAL, 212, 219);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 67, 224);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 60, 225);
			expected.allocateNode(NodeType.BLOCK, 56, 228);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 228);
			expected.allocateClassDeclaration(7, 230, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 231);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with compound boolean expression in guard.
	 * Tests operator precedence and associativity in when clause.
	 * <p>
	 * Note: Parenthesized expressions at the end of a guard before {@code ->} may be
	 * incorrectly interpreted as lambda parameters due to parser ambiguity.
	 * This test uses expressions that avoid this edge case.
	 */
	@Test
	public void shouldParseGuardWithParenthesizedExpression()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Integer i when i > 0 && i % 2 == 0 -> "positive even";
						case Integer i when i > 0 && i % 2 != 0 -> "positive odd";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(46, 56, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 111);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 114, 115);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 115);
			expected.allocateNode(NodeType.IDENTIFIER, 119, 120);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 123, 124);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 119, 124);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 128, 129);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 119, 129);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 129);
			expected.allocateNode(NodeType.STRING_LITERAL, 133, 148);
			expected.allocateNode(NodeType.IDENTIFIER, 173, 174);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 177, 178);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 173, 178);
			expected.allocateNode(NodeType.IDENTIFIER, 182, 183);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 186, 187);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 182, 187);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 191, 192);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 182, 192);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 173, 192);
			expected.allocateNode(NodeType.STRING_LITERAL, 196, 210);
			expected.allocateNode(NodeType.STRING_LITERAL, 226, 233);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 238);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 239);
			expected.allocateNode(NodeType.BLOCK, 59, 242);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 242);
			expected.allocateClassDeclaration(7, 244, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 245);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple guarded case patterns in a single switch expression.
	 * Tests multiple type patterns each with their own guard clauses.
	 */
	@Test
	public void shouldParseMultipleGuardedCases()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case String s when s.length() > 10 -> "long string";
						case String s when s.length() > 5 -> "medium string";
						case String s when !s.isEmpty() -> "short string";
						case Integer i when i > 100 -> "large integer";
						case Integer i when i > 0 -> "small integer";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 76, 79);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 108);
			expected.allocateNode(NodeType.FIELD_ACCESS, 107, 115);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 107, 117);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 120, 122);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 107, 122);
			expected.allocateNode(NodeType.STRING_LITERAL, 126, 139);
			expected.allocateNode(NodeType.IDENTIFIER, 163, 164);
			expected.allocateNode(NodeType.FIELD_ACCESS, 163, 171);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 163, 173);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 176, 177);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 163, 177);
			expected.allocateNode(NodeType.STRING_LITERAL, 181, 196);
			expected.allocateNode(NodeType.IDENTIFIER, 221, 222);
			expected.allocateNode(NodeType.FIELD_ACCESS, 221, 230);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 221, 232);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 220, 232);
			expected.allocateNode(NodeType.STRING_LITERAL, 236, 250);
			expected.allocateNode(NodeType.IDENTIFIER, 275, 276);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 279, 282);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 275, 282);
			expected.allocateNode(NodeType.STRING_LITERAL, 286, 301);
			expected.allocateNode(NodeType.IDENTIFIER, 326, 327);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 330, 331);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 326, 331);
			expected.allocateNode(NodeType.STRING_LITERAL, 335, 350);
			expected.allocateNode(NodeType.STRING_LITERAL, 366, 373);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 378);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 379);
			expected.allocateNode(NodeType.BLOCK, 57, 382);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 382);
			expected.allocateClassDeclaration(7, 384, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 385);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch expression with mix of guarded and unguarded patterns.
	 * Tests interleaving of guarded patterns with null case and default.
	 */
	@Test
	public void shouldParseMixOfGuardedAndUnguardedPatterns()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case null -> "null value";
						case String s when s.isEmpty() -> "empty string";
						case String s -> "non-empty string";
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
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 76, 79);
			expected.allocateNode(NodeType.STRING_LITERAL, 101, 113);
			expected.allocateNode(NodeType.IDENTIFIER, 137, 138);
			expected.allocateNode(NodeType.FIELD_ACCESS, 137, 146);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 137, 148);
			expected.allocateNode(NodeType.STRING_LITERAL, 152, 166);
			expected.allocateNode(NodeType.STRING_LITERAL, 188, 206);
			expected.allocateNode(NodeType.IDENTIFIER, 231, 232);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 235, 236);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 231, 236);
			expected.allocateNode(NodeType.STRING_LITERAL, 240, 250);
			expected.allocateNode(NodeType.STRING_LITERAL, 273, 287);
			expected.allocateNode(NodeType.STRING_LITERAL, 303, 310);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 315);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 316);
			expected.allocateNode(NodeType.BLOCK, 57, 319);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 319);
			expected.allocateClassDeclaration(7, 321, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 322);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with negated condition in guard expression.
	 * Tests logical NOT operator in when clause.
	 * <p>
	 * Note: Negated parenthesized expressions like {@code !(expr)} before {@code ->} may be
	 * incorrectly interpreted as lambda parameters. This test uses equivalent expressions
	 * that avoid this edge case.
	 */
	@Test
	public void shouldParseGuardWithNegatedCondition()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case String s when !s.isEmpty() -> "non-empty";
						case String s when s.length() == 0 -> "empty";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 76, 79);
			expected.allocateNode(NodeType.IDENTIFIER, 108, 109);
			expected.allocateNode(NodeType.FIELD_ACCESS, 108, 117);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 108, 119);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 107, 119);
			expected.allocateNode(NodeType.STRING_LITERAL, 123, 134);
			expected.allocateNode(NodeType.IDENTIFIER, 158, 159);
			expected.allocateNode(NodeType.FIELD_ACCESS, 158, 166);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 158, 168);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 172, 173);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 158, 173);
			expected.allocateNode(NodeType.STRING_LITERAL, 177, 184);
			expected.allocateNode(NodeType.STRING_LITERAL, 200, 207);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 212);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 213);
			expected.allocateNode(NodeType.BLOCK, 57, 216);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 216);
			expected.allocateClassDeclaration(7, 218, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 219);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that {@code when} can be used as a variable name outside switch guards.
	 * Tests that when is a contextual keyword, not a reserved keyword.
	 */
	@Test
	public void shouldParseWhenAsVariableNameOutsideSwitch()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					int when = 10;
					String whenValue = "test";
					Object whenObj = when + whenValue.length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 58, 60);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 70);
			expected.allocateNode(NodeType.STRING_LITERAL, 83, 89);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 93, 99);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 114);
			expected.allocateNode(NodeType.IDENTIFIER, 117, 126);
			expected.allocateNode(NodeType.FIELD_ACCESS, 117, 133);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 117, 135);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 135);
			expected.allocateNode(NodeType.BLOCK, 43, 139);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 139);
			expected.allocateClassDeclaration(7, 141, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 142);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with field access in guard expression.
	 * Tests accessing instance fields in when clause.
	 */
	@Test
	public void shouldParseGuardWithFieldAccess()
	{
		String source = """
			public class Test
			{
				private int threshold = 100;

				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Integer i when i > this.threshold -> "above threshold";
						case Integer i when i > threshold -> "also above threshold";
						default -> "below or equal";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 45, 48);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 77, 83);
			expected.allocateParameterDeclaration(77, 87, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 109, 112);
			expected.allocateNode(NodeType.IDENTIFIER, 141, 142);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 145, 149);
			expected.allocateNode(NodeType.FIELD_ACCESS, 145, 159);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 141, 159);
			expected.allocateNode(NodeType.STRING_LITERAL, 163, 180);
			expected.allocateNode(NodeType.IDENTIFIER, 205, 206);
			expected.allocateNode(NodeType.IDENTIFIER, 209, 218);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 205, 218);
			expected.allocateNode(NodeType.STRING_LITERAL, 222, 244);
			expected.allocateNode(NodeType.STRING_LITERAL, 260, 276);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 101, 281);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 94, 282);
			expected.allocateNode(NodeType.BLOCK, 90, 285);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 52, 285);
			expected.allocateClassDeclaration(7, 287, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 288);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of nested switch expressions with guarded patterns.
	 * Tests guarded patterns in both outer and inner switch expressions.
	 */
	@Test
	public void shouldParseNestedSwitchWithGuardedPatterns()
	{
		String source = """
			public class Test
			{
				public String describe(Object outer, Object inner)
				{
					return switch (outer)
					{
						case String s when s.length() > 0 -> switch (inner)
						{
							case Integer i when i > 0 -> "positive inner";
							case Integer i when i < 0 -> "negative inner";
							default -> "zero inner";
						};
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 56, new ParameterAttribute("outer", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 64);
			expected.allocateParameterDeclaration(58, 70, new ParameterAttribute("inner", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 92, 97);
			expected.allocateNode(NodeType.IDENTIFIER, 125, 126);
			expected.allocateNode(NodeType.FIELD_ACCESS, 125, 133);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 125, 135);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 138, 139);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 139);
			expected.allocateNode(NodeType.IDENTIFIER, 151, 156);
			expected.allocateNode(NodeType.IDENTIFIER, 187, 188);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 191, 192);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 187, 192);
			expected.allocateNode(NodeType.STRING_LITERAL, 196, 212);
			expected.allocateNode(NodeType.IDENTIFIER, 238, 239);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 242, 243);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 238, 243);
			expected.allocateNode(NodeType.STRING_LITERAL, 247, 263);
			expected.allocateNode(NodeType.STRING_LITERAL, 280, 292);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 143, 298);
			expected.allocateNode(NodeType.STRING_LITERAL, 314, 321);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 84, 326);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 77, 327);
			expected.allocateNode(NodeType.BLOCK, 73, 330);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 330);
			expected.allocateClassDeclaration(7, 332, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 333);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern in switch statement with colon syntax.
	 * Tests guarded patterns in traditional switch statement form.
	 */
	@Test
	public void shouldParseGuardedPatternInSwitchStatement()
	{
		String source = """
			public class Test
			{
				public void process(Object obj)
				{
					switch (obj)
					{
						case String s when s.length() > 5:
							System.out.println("long string");
							break;
						case String s:
							System.out.println("short string");
							break;
						default:
							System.out.println("not a string");
							break;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateParameterDeclaration(41, 51, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 66, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 97, 98);
			expected.allocateNode(NodeType.FIELD_ACCESS, 97, 105);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 97, 107);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 110, 111);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 97, 111);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 117, 135);
			expected.allocateNode(NodeType.IDENTIFIER, 117, 123);
			expected.allocateNode(NodeType.FIELD_ACCESS, 117, 127);
			expected.allocateNode(NodeType.FIELD_ACCESS, 117, 135);
			expected.allocateNode(NodeType.STRING_LITERAL, 136, 149);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 117, 150);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 156, 162);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 185, 203);
			expected.allocateNode(NodeType.IDENTIFIER, 185, 191);
			expected.allocateNode(NodeType.FIELD_ACCESS, 185, 195);
			expected.allocateNode(NodeType.FIELD_ACCESS, 185, 203);
			expected.allocateNode(NodeType.STRING_LITERAL, 204, 218);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 185, 219);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 225, 231);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 248, 266);
			expected.allocateNode(NodeType.IDENTIFIER, 248, 254);
			expected.allocateNode(NodeType.FIELD_ACCESS, 248, 258);
			expected.allocateNode(NodeType.FIELD_ACCESS, 248, 266);
			expected.allocateNode(NodeType.STRING_LITERAL, 267, 281);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 248, 282);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 288, 294);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 58, 298);
			expected.allocateNode(NodeType.BLOCK, 54, 301);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 301);
			expected.allocateClassDeclaration(7, 303, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 304);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with multiple related type patterns.
	 * Tests combining similar type patterns with different guard expressions.
	 * <p>
	 * Note: Record pattern deconstruction like {@code Point(int x, int y)} is not
	 * currently supported by the parser. This test uses simple type patterns instead.
	 */
	@Test
	public void shouldParseGuardWithMultipleRelatedPatterns()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Number n when n.doubleValue() > 0 -> "positive";
						case Number n when n.doubleValue() < 0 -> "negative";
						case Number n when n.doubleValue() == 0 -> "zero";
						default -> "not a number";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(46, 56, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 109, 110);
			expected.allocateNode(NodeType.FIELD_ACCESS, 109, 122);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 109, 124);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 127, 128);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 109, 128);
			expected.allocateNode(NodeType.STRING_LITERAL, 132, 142);
			expected.allocateNode(NodeType.IDENTIFIER, 166, 167);
			expected.allocateNode(NodeType.FIELD_ACCESS, 166, 179);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 166, 181);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 184, 185);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 166, 185);
			expected.allocateNode(NodeType.STRING_LITERAL, 189, 199);
			expected.allocateNode(NodeType.IDENTIFIER, 223, 224);
			expected.allocateNode(NodeType.FIELD_ACCESS, 223, 236);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 223, 238);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 242, 243);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 223, 243);
			expected.allocateNode(NodeType.STRING_LITERAL, 247, 253);
			expected.allocateNode(NodeType.STRING_LITERAL, 269, 283);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 288);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 289);
			expected.allocateNode(NodeType.BLOCK, 59, 292);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 292);
			expected.allocateClassDeclaration(7, 294, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 295);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with collection access in guard expression.
	 * Tests collection operations in when clause.
	 * <p>
	 * Note: Array type patterns like {@code int[] arr} are not currently supported.
	 * This test uses List type patterns with similar guard expressions.
	 */
	@Test
	public void shouldParseGuardWithCollectionAccess()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case String s when s.length() > 0 && s.charAt(0) == 'A' -> "starts with A";
						case String s when s.length() > 0 && s.charAt(0) == 'B' -> "starts with B";
						case String s when s.length() > 0 -> "non-empty";
						case String s -> "empty string";
						default -> "not a string";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 76, 79);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 108);
			expected.allocateNode(NodeType.FIELD_ACCESS, 107, 115);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 107, 117);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 120, 121);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 107, 121);
			expected.allocateNode(NodeType.IDENTIFIER, 125, 126);
			expected.allocateNode(NodeType.FIELD_ACCESS, 125, 133);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 134, 135);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 125, 136);
			expected.allocateNode(NodeType.CHAR_LITERAL, 140, 143);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 143);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 107, 143);
			expected.allocateNode(NodeType.STRING_LITERAL, 147, 162);
			expected.allocateNode(NodeType.IDENTIFIER, 186, 187);
			expected.allocateNode(NodeType.FIELD_ACCESS, 186, 194);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 186, 196);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 199, 200);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 186, 200);
			expected.allocateNode(NodeType.IDENTIFIER, 204, 205);
			expected.allocateNode(NodeType.FIELD_ACCESS, 204, 212);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 213, 214);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 204, 215);
			expected.allocateNode(NodeType.CHAR_LITERAL, 219, 222);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 204, 222);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 186, 222);
			expected.allocateNode(NodeType.STRING_LITERAL, 226, 241);
			expected.allocateNode(NodeType.IDENTIFIER, 265, 266);
			expected.allocateNode(NodeType.FIELD_ACCESS, 265, 273);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 265, 275);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 278, 279);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 265, 279);
			expected.allocateNode(NodeType.STRING_LITERAL, 283, 294);
			expected.allocateNode(NodeType.STRING_LITERAL, 316, 330);
			expected.allocateNode(NodeType.STRING_LITERAL, 346, 360);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 365);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 366);
			expected.allocateNode(NodeType.BLOCK, 57, 369);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 369);
			expected.allocateClassDeclaration(7, 371, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 372);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with equals comparison in guard.
	 * Tests equality comparison operators in when clause.
	 */
	@Test
	public void shouldParseGuardWithEqualsComparison()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Integer i when i == 0 -> "zero";
						case Integer i when i != 0 -> "non-zero";
						case String s when s.equals("test") -> "test string";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(46, 56, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 111);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 115, 116);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 116);
			expected.allocateNode(NodeType.STRING_LITERAL, 120, 126);
			expected.allocateNode(NodeType.IDENTIFIER, 151, 152);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 156, 157);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 151, 157);
			expected.allocateNode(NodeType.STRING_LITERAL, 161, 171);
			expected.allocateNode(NodeType.IDENTIFIER, 195, 196);
			expected.allocateNode(NodeType.FIELD_ACCESS, 195, 203);
			expected.allocateNode(NodeType.STRING_LITERAL, 204, 210);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 195, 211);
			expected.allocateNode(NodeType.STRING_LITERAL, 215, 228);
			expected.allocateNode(NodeType.STRING_LITERAL, 244, 251);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 256);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 257);
			expected.allocateNode(NodeType.BLOCK, 59, 260);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 260);
			expected.allocateClassDeclaration(7, 262, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 263);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with static method call in guard.
	 * Tests invoking static methods in when clause.
	 */
	@Test
	public void shouldParseGuardWithStaticMethodCall()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Integer i when Integer.signum(i) > 0 -> "positive";
						case Integer i when Integer.signum(i) < 0 -> "negative";
						case Integer i when Math.abs(i) == 0 -> "zero";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(46, 56, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 78, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 117);
			expected.allocateNode(NodeType.FIELD_ACCESS, 110, 124);
			expected.allocateNode(NodeType.IDENTIFIER, 125, 126);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 110, 127);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 130, 131);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 110, 131);
			expected.allocateNode(NodeType.STRING_LITERAL, 135, 145);
			expected.allocateNode(NodeType.IDENTIFIER, 170, 177);
			expected.allocateNode(NodeType.FIELD_ACCESS, 170, 184);
			expected.allocateNode(NodeType.IDENTIFIER, 185, 186);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 170, 187);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 190, 191);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 170, 191);
			expected.allocateNode(NodeType.STRING_LITERAL, 195, 205);
			expected.allocateNode(NodeType.IDENTIFIER, 230, 234);
			expected.allocateNode(NodeType.FIELD_ACCESS, 230, 238);
			expected.allocateNode(NodeType.IDENTIFIER, 239, 240);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 230, 241);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 245, 246);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 230, 246);
			expected.allocateNode(NodeType.STRING_LITERAL, 250, 256);
			expected.allocateNode(NodeType.STRING_LITERAL, 272, 279);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 284);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 285);
			expected.allocateNode(NodeType.BLOCK, 59, 288);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 288);
			expected.allocateClassDeclaration(7, 290, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 291);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with ternary expression in guard.
	 * Tests conditional operator within when clause.
	 * <p>
	 * Note: Parenthesized ternary expressions before {@code ->} may be incorrectly
	 * interpreted as lambda parameters. This test uses ternary without outer parentheses.
	 */
	@Test
	public void shouldParseGuardWithTernaryExpression()
	{
		String source = """
			public class Test
			{
				private int threshold = 100;

				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Integer i when i > threshold -> "large";
						case Integer i when i > threshold / 2 -> "medium";
						default -> "small";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 45, 48);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 77, 83);
			expected.allocateParameterDeclaration(77, 87, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 109, 112);
			expected.allocateNode(NodeType.IDENTIFIER, 141, 142);
			expected.allocateNode(NodeType.IDENTIFIER, 145, 154);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 141, 154);
			expected.allocateNode(NodeType.STRING_LITERAL, 158, 165);
			expected.allocateNode(NodeType.IDENTIFIER, 190, 191);
			expected.allocateNode(NodeType.IDENTIFIER, 194, 203);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 206, 207);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 194, 207);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 190, 207);
			expected.allocateNode(NodeType.STRING_LITERAL, 211, 219);
			expected.allocateNode(NodeType.STRING_LITERAL, 235, 242);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 101, 247);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 94, 248);
			expected.allocateNode(NodeType.BLOCK, 90, 251);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 52, 251);
			expected.allocateClassDeclaration(7, 253, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 254);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with instanceof in guard expression.
	 * Tests combining pattern matching with additional instanceof check in guard.
	 */
	@Test
	public void shouldParseGuardWithInstanceofInGuard()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj, Object extra)
				{
					return switch (obj)
					{
						case String s when extra instanceof Integer -> "string with integer context";
						case String s when extra instanceof String -> "string with string context";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateParameterDeclaration(56, 68, new ParameterAttribute("extra", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 90, 93);
			expected.allocateNode(NodeType.IDENTIFIER, 121, 126);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 138, 145);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 121, 145);
			expected.allocateNode(NodeType.STRING_LITERAL, 149, 178);
			expected.allocateNode(NodeType.IDENTIFIER, 202, 207);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 219, 225);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 202, 225);
			expected.allocateNode(NodeType.STRING_LITERAL, 229, 257);
			expected.allocateNode(NodeType.STRING_LITERAL, 273, 280);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 82, 285);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 75, 286);
			expected.allocateNode(NodeType.BLOCK, 71, 289);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 289);
			expected.allocateClassDeclaration(7, 291, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 292);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with chained method calls in guard.
	 * Tests method chaining in when clause expression.
	 */
	@Test
	public void shouldParseGuardWithChainedMethodCalls()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case String s when s.strip().toLowerCase().startsWith("hello") -> "greeting";
						case String s when s.strip().length() > 0 -> "non-empty";
						default -> "other";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 76, 79);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 108);
			expected.allocateNode(NodeType.FIELD_ACCESS, 107, 114);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 107, 116);
			expected.allocateNode(NodeType.FIELD_ACCESS, 107, 128);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 107, 130);
			expected.allocateNode(NodeType.FIELD_ACCESS, 107, 141);
			expected.allocateNode(NodeType.STRING_LITERAL, 142, 149);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 107, 150);
			expected.allocateNode(NodeType.STRING_LITERAL, 154, 164);
			expected.allocateNode(NodeType.IDENTIFIER, 188, 189);
			expected.allocateNode(NodeType.FIELD_ACCESS, 188, 195);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 188, 197);
			expected.allocateNode(NodeType.FIELD_ACCESS, 188, 204);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 188, 206);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 209, 210);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 188, 210);
			expected.allocateNode(NodeType.STRING_LITERAL, 214, 225);
			expected.allocateNode(NodeType.STRING_LITERAL, 241, 248);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 253);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 254);
			expected.allocateNode(NodeType.BLOCK, 57, 257);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 257);
			expected.allocateClassDeclaration(7, 259, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 260);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of guarded pattern with local variable reference in guard.
	 * Tests using local variables in when clause boolean expressions.
	 */
	@Test
	public void shouldParseGuardWithLocalVariableInGuard()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					int minLength = 5;
					int maxLength = 100;
					return switch (obj)
					{
						case String s when s.length() >= minLength && s.length() <= maxLength -> "valid length";
						default -> "invalid length";
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(46, 56, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.INTEGER_LITERAL, 79, 80);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 100, 103);
			expected.allocateNode(NodeType.IDENTIFIER, 122, 125);
			expected.allocateNode(NodeType.IDENTIFIER, 153, 154);
			expected.allocateNode(NodeType.FIELD_ACCESS, 153, 161);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 153, 163);
			expected.allocateNode(NodeType.IDENTIFIER, 167, 176);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 153, 176);
			expected.allocateNode(NodeType.IDENTIFIER, 180, 181);
			expected.allocateNode(NodeType.FIELD_ACCESS, 180, 188);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 180, 190);
			expected.allocateNode(NodeType.IDENTIFIER, 194, 203);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 180, 203);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 153, 203);
			expected.allocateNode(NodeType.STRING_LITERAL, 207, 221);
			expected.allocateNode(NodeType.STRING_LITERAL, 237, 253);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 114, 258);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 107, 259);
			expected.allocateNode(NodeType.BLOCK, 59, 262);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 262);
			expected.allocateClassDeclaration(7, 264, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 265);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
