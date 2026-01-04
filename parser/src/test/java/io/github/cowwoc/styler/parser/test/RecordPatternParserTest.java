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
 * Tests for parsing record patterns in switch expressions (Java 21+).
 */
public class RecordPatternParserTest
{
	// ==================== Simple Record Patterns ====================

	/**
	 * Validates parsing of a record pattern with no components (empty parentheses).
	 * Tests the basic form: {@code case Empty() ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithEmptyComponents()
	{
		String source = """
			public class Test
			{
				public void process(Object obj)
				{
					switch (obj)
					{
						case Empty() -> System.out.println("empty");
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 83, 90);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 94, 112);
			expected.allocateNode(NodeType.IDENTIFIER, 94, 100);
			expected.allocateNode(NodeType.FIELD_ACCESS, 94, 104);
			expected.allocateNode(NodeType.FIELD_ACCESS, 94, 112);
			expected.allocateNode(NodeType.STRING_LITERAL, 113, 120);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 94, 121);
			expected.allocateNode(NodeType.BLOCK, 137, 139);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 58, 143);
			expected.allocateNode(NodeType.BLOCK, 54, 146);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 146);
			expected.allocateClassDeclaration(7, 148, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 149);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with a single component.
	 * Tests: {@code case Box(Item item) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithSingleComponent()
	{
		String source = """
			public class Test
			{
				public void process(Object obj)
				{
					switch (obj)
					{
						case Box(Item item) -> System.out.println(item);
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 83, 97);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 101, 119);
			expected.allocateNode(NodeType.IDENTIFIER, 101, 107);
			expected.allocateNode(NodeType.FIELD_ACCESS, 101, 111);
			expected.allocateNode(NodeType.FIELD_ACCESS, 101, 119);
			expected.allocateNode(NodeType.IDENTIFIER, 120, 124);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 101, 125);
			expected.allocateNode(NodeType.BLOCK, 141, 143);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 58, 147);
			expected.allocateNode(NodeType.BLOCK, 54, 150);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 150);
			expected.allocateClassDeclaration(7, 152, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 153);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with two components.
	 * Tests: {@code case Point(int x, int y) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithMultipleComponents()
	{
		String source = """
			public class Test
			{
				public void process(Object obj)
				{
					switch (obj)
					{
						case Point(int x, int y) -> System.out.println(x + ", " + y);
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 83, 102);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 106, 124);
			expected.allocateNode(NodeType.IDENTIFIER, 106, 112);
			expected.allocateNode(NodeType.FIELD_ACCESS, 106, 116);
			expected.allocateNode(NodeType.FIELD_ACCESS, 106, 124);
			expected.allocateNode(NodeType.IDENTIFIER, 125, 126);
			expected.allocateNode(NodeType.STRING_LITERAL, 129, 133);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 133);
			expected.allocateNode(NodeType.IDENTIFIER, 136, 137);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 137);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 106, 138);
			expected.allocateNode(NodeType.BLOCK, 154, 156);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 58, 160);
			expected.allocateNode(NodeType.BLOCK, 54, 163);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 163);
			expected.allocateClassDeclaration(7, 165, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 166);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with three components.
	 * Tests: {@code case RGB(int r, int g, int b) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithThreeComponents()
	{
		String source = """
			public class Test
			{
				public int process(Object obj)
				{
					return switch (obj)
					{
						case RGB(int r, int g, int b) -> r + g + b;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 72, 75);
			expected.allocateNode(NodeType.RECORD_PATTERN, 89, 113);
			expected.allocateNode(NodeType.IDENTIFIER, 117, 118);
			expected.allocateNode(NodeType.IDENTIFIER, 121, 122);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 117, 122);
			expected.allocateNode(NodeType.IDENTIFIER, 125, 126);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 117, 126);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 142, 143);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 64, 148);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 57, 149);
			expected.allocateNode(NodeType.BLOCK, 53, 152);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 152);
			expected.allocateClassDeclaration(7, 154, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 155);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with primitive type components.
	 * Tests: {@code case IntPair(int first, int second) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithPrimitiveComponents()
	{
		String source = """
			public class Test
			{
				public int sum(Object obj)
				{
					return switch (obj)
					{
						case IntPair(int first, int second) -> first + second;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 42);
			expected.allocateParameterDeclaration(36, 46, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 68, 71);
			expected.allocateNode(NodeType.RECORD_PATTERN, 85, 115);
			expected.allocateNode(NodeType.IDENTIFIER, 119, 124);
			expected.allocateNode(NodeType.IDENTIFIER, 127, 133);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 119, 133);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 149, 150);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 60, 155);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 53, 156);
			expected.allocateNode(NodeType.BLOCK, 49, 159);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 159);
			expected.allocateClassDeclaration(7, 161, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 162);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Nested Record Patterns ====================

	/**
	 * Validates parsing of a nested record pattern.
	 * Tests: {@code case Box(Point(int x, int y)) ->}.
	 */
	@Test
	public void shouldParseNestedRecordPattern()
	{
		String source = """
			public class Test
			{
				public void process(Object obj)
				{
					switch (obj)
					{
						case Box(Point(int x, int y)) -> System.out.println(x + y);
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 87, 106);
			expected.allocateNode(NodeType.RECORD_PATTERN, 83, 107);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 111, 129);
			expected.allocateNode(NodeType.IDENTIFIER, 111, 117);
			expected.allocateNode(NodeType.FIELD_ACCESS, 111, 121);
			expected.allocateNode(NodeType.FIELD_ACCESS, 111, 129);
			expected.allocateNode(NodeType.IDENTIFIER, 130, 131);
			expected.allocateNode(NodeType.IDENTIFIER, 134, 135);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 130, 135);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 111, 136);
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
	 * Validates parsing of a deeply nested record pattern (three levels).
	 * Tests: {@code case Outer(Middle(Inner(int value))) ->}.
	 */
	@Test
	public void shouldParseDeeplyNestedRecordPattern()
	{
		String source = """
			public class Test
			{
				public int extract(Object obj)
				{
					return switch (obj)
					{
						case Outer(Middle(Inner(int value))) -> value;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 72, 75);
			expected.allocateNode(NodeType.RECORD_PATTERN, 102, 118);
			expected.allocateNode(NodeType.RECORD_PATTERN, 95, 119);
			expected.allocateNode(NodeType.RECORD_PATTERN, 89, 120);
			expected.allocateNode(NodeType.IDENTIFIER, 124, 129);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 145, 146);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 64, 151);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 57, 152);
			expected.allocateNode(NodeType.BLOCK, 53, 155);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 155);
			expected.allocateClassDeclaration(7, 157, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 158);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with mixed nested and simple components.
	 * Tests: {@code case Container(String name, Box(Item item)) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithMixedNestedComponents()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case Container(String name, Box(Item item)) -> name + ": " + item;
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
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 76, 79);
			expected.allocateNode(NodeType.RECORD_PATTERN, 116, 130);
			expected.allocateNode(NodeType.RECORD_PATTERN, 93, 131);
			expected.allocateNode(NodeType.IDENTIFIER, 135, 139);
			expected.allocateNode(NodeType.STRING_LITERAL, 142, 146);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 135, 146);
			expected.allocateNode(NodeType.IDENTIFIER, 149, 153);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 135, 153);
			expected.allocateNode(NodeType.STRING_LITERAL, 169, 178);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 183);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 184);
			expected.allocateNode(NodeType.BLOCK, 57, 187);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 187);
			expected.allocateClassDeclaration(7, 189, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 190);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with multiple nested record patterns.
	 * Tests: {@code case Line(Point(int x1, int y1), Point(int x2, int y2)) ->}.
	 */
	@Test
	public void shouldParseMultipleNestedRecordPatterns()
	{
		String source = """
			public class Test
			{
				public double length(Object obj)
				{
					return switch (obj)
					{
						case Line(Point(int x1, int y1), Point(int x2, int y2)) ->
							Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
						default -> 0.0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateParameterDeclaration(42, 52, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 74, 77);
			expected.allocateNode(NodeType.RECORD_PATTERN, 96, 117);
			expected.allocateNode(NodeType.RECORD_PATTERN, 119, 140);
			expected.allocateNode(NodeType.RECORD_PATTERN, 91, 141);
			expected.allocateNode(NodeType.IDENTIFIER, 149, 153);
			expected.allocateNode(NodeType.FIELD_ACCESS, 149, 158);
			expected.allocateNode(NodeType.IDENTIFIER, 160, 162);
			expected.allocateNode(NodeType.IDENTIFIER, 165, 167);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 160, 167);
			expected.allocateNode(NodeType.IDENTIFIER, 172, 174);
			expected.allocateNode(NodeType.IDENTIFIER, 177, 179);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 172, 179);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 160, 179);
			expected.allocateNode(NodeType.IDENTIFIER, 184, 186);
			expected.allocateNode(NodeType.IDENTIFIER, 189, 191);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 184, 191);
			expected.allocateNode(NodeType.IDENTIFIER, 196, 198);
			expected.allocateNode(NodeType.IDENTIFIER, 201, 203);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 196, 203);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 184, 203);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 160, 203);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 149, 205);
			expected.allocateNode(NodeType.DOUBLE_LITERAL, 221, 224);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 66, 229);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 59, 230);
			expected.allocateNode(NodeType.BLOCK, 55, 233);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 233);
			expected.allocateClassDeclaration(7, 235, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 236);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Guard Integration ====================

	/**
	 * Validates parsing of a record pattern with a guard clause.
	 * Tests: {@code case Point(int x, int y) when x > 0 ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithGuard()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Point(int x, int y) when x > 0 -> "positive x";
						case Point(int x, int y) when x < 0 -> "negative x";
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
			expected.allocateNode(NodeType.IDENTIFIER, 120, 121);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 124, 125);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 120, 125);
			expected.allocateNode(NodeType.RECORD_PATTERN, 95, 125);
			expected.allocateNode(NodeType.STRING_LITERAL, 129, 141);
			expected.allocateNode(NodeType.IDENTIFIER, 176, 177);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 180, 181);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 176, 181);
			expected.allocateNode(NodeType.RECORD_PATTERN, 151, 181);
			expected.allocateNode(NodeType.STRING_LITERAL, 185, 197);
			expected.allocateNode(NodeType.STRING_LITERAL, 213, 220);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 225);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 226);
			expected.allocateNode(NodeType.BLOCK, 59, 229);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 229);
			expected.allocateClassDeclaration(7, 231, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 232);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a nested record pattern with a guard clause.
	 * Tests: {@code case Box(Point(int x, int y)) when x > 0 && y > 0 ->}.
	 */
	@Test
	public void shouldParseNestedRecordPatternWithGuard()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Box(Point(int x, int y)) when x > 0 && y > 0 -> "first quadrant";
						case Box(Point(int x, int y)) when x < 0 && y > 0 -> "second quadrant";
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 99, 118);
			expected.allocateNode(NodeType.IDENTIFIER, 125, 126);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 129, 130);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 130);
			expected.allocateNode(NodeType.IDENTIFIER, 134, 135);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 138, 139);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 134, 139);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 139);
			expected.allocateNode(NodeType.RECORD_PATTERN, 95, 139);
			expected.allocateNode(NodeType.STRING_LITERAL, 143, 159);
			expected.allocateNode(NodeType.RECORD_PATTERN, 173, 192);
			expected.allocateNode(NodeType.IDENTIFIER, 199, 200);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 203, 204);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 199, 204);
			expected.allocateNode(NodeType.IDENTIFIER, 208, 209);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 212, 213);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 208, 213);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 199, 213);
			expected.allocateNode(NodeType.RECORD_PATTERN, 169, 213);
			expected.allocateNode(NodeType.STRING_LITERAL, 217, 234);
			expected.allocateNode(NodeType.STRING_LITERAL, 250, 257);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 262);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 263);
			expected.allocateNode(NodeType.BLOCK, 59, 266);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 266);
			expected.allocateClassDeclaration(7, 268, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 269);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with a method call in the guard expression.
	 * Tests: {@code case Person(String name, int age) when name.startsWith("A") ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithMethodCallInGuard()
	{
		String source = """
			public class Test
			{
				public String categorize(Object obj)
				{
					return switch (obj)
					{
						case Person(String name, int age) when name.startsWith("A") -> "A-name";
						case Person(String name, int age) when age > 18 -> "adult";
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
			expected.allocateNode(NodeType.IDENTIFIER, 129, 133);
			expected.allocateNode(NodeType.FIELD_ACCESS, 129, 144);
			expected.allocateNode(NodeType.STRING_LITERAL, 145, 148);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 129, 149);
			expected.allocateNode(NodeType.RECORD_PATTERN, 95, 149);
			expected.allocateNode(NodeType.STRING_LITERAL, 153, 161);
			expected.allocateNode(NodeType.IDENTIFIER, 205, 208);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 211, 213);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 205, 213);
			expected.allocateNode(NodeType.RECORD_PATTERN, 171, 213);
			expected.allocateNode(NodeType.STRING_LITERAL, 217, 224);
			expected.allocateNode(NodeType.STRING_LITERAL, 240, 247);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 70, 252);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 253);
			expected.allocateNode(NodeType.BLOCK, 59, 256);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 256);
			expected.allocateClassDeclaration(7, 258, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 259);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Qualified Types ====================

	/**
	 * Validates parsing of a record pattern with a qualified type name.
	 * Tests: {@code case java.awt.Point(int x, int y) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithQualifiedType()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case java.awt.Point(int x, int y) -> "point at " + x + ", " + y;
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
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 76, 79);
			expected.allocateNode(NodeType.RECORD_PATTERN, 93, 121);
			expected.allocateNode(NodeType.STRING_LITERAL, 125, 136);
			expected.allocateNode(NodeType.IDENTIFIER, 139, 140);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 140);
			expected.allocateNode(NodeType.STRING_LITERAL, 143, 147);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 147);
			expected.allocateNode(NodeType.IDENTIFIER, 150, 151);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 125, 151);
			expected.allocateNode(NodeType.STRING_LITERAL, 167, 176);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 181);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 182);
			expected.allocateNode(NodeType.BLOCK, 57, 185);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 185);
			expected.allocateClassDeclaration(7, 187, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 188);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of nested record patterns with qualified types.
	 * Tests: {@code case com.example.Container(com.example.Point(int x, int y)) ->}.
	 */
	@Test
	public void shouldParseNestedRecordPatternWithQualifiedTypes()
	{
		String source = """
			public class Test
			{
				public int extract(Object obj)
				{
					return switch (obj)
					{
						case com.example.Container(com.example.Point(int x, int y)) -> x + y;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 72, 75);
			expected.allocateNode(NodeType.RECORD_PATTERN, 111, 142);
			expected.allocateNode(NodeType.RECORD_PATTERN, 89, 143);
			expected.allocateNode(NodeType.IDENTIFIER, 147, 148);
			expected.allocateNode(NodeType.IDENTIFIER, 151, 152);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 147, 152);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 168, 169);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 64, 174);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 57, 175);
			expected.allocateNode(NodeType.BLOCK, 53, 178);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 178);
			expected.allocateClassDeclaration(7, 180, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 181);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Switch Forms ====================

	/**
	 * Validates parsing of a record pattern in a switch statement with colon syntax.
	 * Tests traditional switch statement form with record patterns.
	 */
	@Test
	public void shouldParseRecordPatternInSwitchStatement()
	{
		String source = """
			public class Test
			{
				public void process(Object obj)
				{
					switch (obj)
					{
						case Point(int x, int y):
							System.out.println("Point: " + x + ", " + y);
							break;
						case Circle(int radius):
							System.out.println("Circle: " + radius);
							break;
						default:
							System.out.println("Unknown");
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 83, 102);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 108, 126);
			expected.allocateNode(NodeType.IDENTIFIER, 108, 114);
			expected.allocateNode(NodeType.FIELD_ACCESS, 108, 118);
			expected.allocateNode(NodeType.FIELD_ACCESS, 108, 126);
			expected.allocateNode(NodeType.STRING_LITERAL, 127, 136);
			expected.allocateNode(NodeType.IDENTIFIER, 139, 140);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 127, 140);
			expected.allocateNode(NodeType.STRING_LITERAL, 143, 147);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 127, 147);
			expected.allocateNode(NodeType.IDENTIFIER, 150, 151);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 127, 151);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 108, 152);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 158, 164);
			expected.allocateNode(NodeType.RECORD_PATTERN, 173, 191);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 197, 215);
			expected.allocateNode(NodeType.IDENTIFIER, 197, 203);
			expected.allocateNode(NodeType.FIELD_ACCESS, 197, 207);
			expected.allocateNode(NodeType.FIELD_ACCESS, 197, 215);
			expected.allocateNode(NodeType.STRING_LITERAL, 216, 226);
			expected.allocateNode(NodeType.IDENTIFIER, 229, 235);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 216, 235);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 197, 236);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 242, 248);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 265, 283);
			expected.allocateNode(NodeType.IDENTIFIER, 265, 271);
			expected.allocateNode(NodeType.FIELD_ACCESS, 265, 275);
			expected.allocateNode(NodeType.FIELD_ACCESS, 265, 283);
			expected.allocateNode(NodeType.STRING_LITERAL, 284, 293);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 265, 294);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 300, 306);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 58, 310);
			expected.allocateNode(NodeType.BLOCK, 54, 313);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 313);
			expected.allocateClassDeclaration(7, 315, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 316);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern in a switch expression with arrow syntax.
	 * Tests modern switch expression form with record patterns.
	 */
	@Test
	public void shouldParseRecordPatternInSwitchExpression()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case Point(int x, int y) -> "Point: " + x + ", " + y;
						case Circle(int radius) -> "Circle: " + radius;
						default -> "Unknown";
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 93, 112);
			expected.allocateNode(NodeType.STRING_LITERAL, 116, 125);
			expected.allocateNode(NodeType.IDENTIFIER, 128, 129);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 116, 129);
			expected.allocateNode(NodeType.STRING_LITERAL, 132, 136);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 116, 136);
			expected.allocateNode(NodeType.IDENTIFIER, 139, 140);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 116, 140);
			expected.allocateNode(NodeType.RECORD_PATTERN, 150, 168);
			expected.allocateNode(NodeType.STRING_LITERAL, 172, 182);
			expected.allocateNode(NodeType.IDENTIFIER, 185, 191);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 172, 191);
			expected.allocateNode(NodeType.STRING_LITERAL, 207, 216);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 221);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 222);
			expected.allocateNode(NodeType.BLOCK, 57, 225);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 225);
			expected.allocateClassDeclaration(7, 227, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 228);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Multiple Cases ====================

	/**
	 * Validates parsing of multiple record pattern case labels in sequence.
	 * Tests multiple record patterns each with their own case clause.
	 */
	@Test
	public void shouldParseMultipleRecordPatternCases()
	{
		String source = """
			public class Test
			{
				public double area(Object obj)
				{
					return switch (obj)
					{
						case Rectangle(int w, int h) -> w * h;
						case Circle(int r) -> Math.PI * r * r;
						case Triangle(int base, int height) -> 0.5 * base * height;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 72, 75);
			expected.allocateNode(NodeType.RECORD_PATTERN, 89, 112);
			expected.allocateNode(NodeType.IDENTIFIER, 116, 117);
			expected.allocateNode(NodeType.IDENTIFIER, 120, 121);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 116, 121);
			expected.allocateNode(NodeType.RECORD_PATTERN, 131, 144);
			expected.allocateNode(NodeType.IDENTIFIER, 148, 152);
			expected.allocateNode(NodeType.FIELD_ACCESS, 148, 155);
			expected.allocateNode(NodeType.IDENTIFIER, 158, 159);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 148, 159);
			expected.allocateNode(NodeType.IDENTIFIER, 162, 163);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 148, 163);
			expected.allocateNode(NodeType.RECORD_PATTERN, 173, 203);
			expected.allocateNode(NodeType.DOUBLE_LITERAL, 207, 210);
			expected.allocateNode(NodeType.IDENTIFIER, 213, 217);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 207, 217);
			expected.allocateNode(NodeType.IDENTIFIER, 220, 226);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 207, 226);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 242, 243);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 64, 248);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 57, 249);
			expected.allocateNode(NodeType.BLOCK, 53, 252);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 252);
			expected.allocateClassDeclaration(7, 254, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 255);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a mix of record patterns and type patterns in the same switch.
	 * Tests combining record pattern matching with simple type pattern matching.
	 */
	@Test
	public void shouldParseMixOfRecordAndTypePatterns()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case Point(int x, int y) -> "point at " + x + ", " + y;
						case String s -> "string: " + s;
						case Integer i -> "integer: " + i;
						case null -> "null";
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 93, 112);
			expected.allocateNode(NodeType.STRING_LITERAL, 116, 127);
			expected.allocateNode(NodeType.IDENTIFIER, 130, 131);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 116, 131);
			expected.allocateNode(NodeType.STRING_LITERAL, 134, 138);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 116, 138);
			expected.allocateNode(NodeType.IDENTIFIER, 141, 142);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 116, 142);
			expected.allocateNode(NodeType.STRING_LITERAL, 164, 174);
			expected.allocateNode(NodeType.IDENTIFIER, 177, 178);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 164, 178);
			expected.allocateNode(NodeType.STRING_LITERAL, 201, 212);
			expected.allocateNode(NodeType.IDENTIFIER, 215, 216);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 201, 216);
			expected.allocateNode(NodeType.STRING_LITERAL, 234, 240);
			expected.allocateNode(NodeType.STRING_LITERAL, 256, 263);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 268);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 269);
			expected.allocateNode(NodeType.BLOCK, 57, 272);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 272);
			expected.allocateClassDeclaration(7, 274, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 275);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Edge Cases ====================

	/**
	 * Validates parsing of a record pattern with {@code var} type inference for components.
	 * Tests: {@code case Point(var x, var y) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithVarComponent()
	{
		String source = """
			public class Test
			{
				public int sum(Object obj)
				{
					return switch (obj)
					{
						case Point(var x, var y) -> x + y;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 42);
			expected.allocateParameterDeclaration(36, 46, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 68, 71);
			expected.allocateNode(NodeType.RECORD_PATTERN, 85, 104);
			expected.allocateNode(NodeType.IDENTIFIER, 108, 109);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 113);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 108, 113);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 129, 130);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 60, 135);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 53, 136);
			expected.allocateNode(NodeType.BLOCK, 49, 139);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 139);
			expected.allocateClassDeclaration(7, 141, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 142);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with an unnamed component (underscore).
	 * Tests: {@code case Point(int x, int _) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithUnnamedComponent()
	{
		String source = """
			public class Test
			{
				public int getX(Object obj)
				{
					return switch (obj)
					{
						case Point(int x, int _) -> x;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 43);
			expected.allocateParameterDeclaration(37, 47, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 69, 72);
			expected.allocateNode(NodeType.RECORD_PATTERN, 86, 105);
			expected.allocateNode(NodeType.IDENTIFIER, 109, 110);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 126, 127);
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
	 * Validates parsing of a record pattern inside a nested switch expression.
	 * Tests record patterns in both outer and inner switch expressions.
	 */
	@Test
	public void shouldParseRecordPatternInNestedSwitch()
	{
		String source = """
			public class Test
			{
				public String describe(Object outer, Object inner)
				{
					return switch (outer)
					{
						case Point(int x, int y) -> switch (inner)
						{
							case Circle(int r) -> "point near circle of radius " + r;
							case Rectangle(int w, int h) -> "point near rect " + w + "x" + h;
							default -> "point near unknown";
						};
						default -> "not a point";
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
			expected.allocateNode(NodeType.RECORD_PATTERN, 111, 130);
			expected.allocateNode(NodeType.IDENTIFIER, 142, 147);
			expected.allocateNode(NodeType.RECORD_PATTERN, 163, 176);
			expected.allocateNode(NodeType.STRING_LITERAL, 180, 210);
			expected.allocateNode(NodeType.IDENTIFIER, 213, 214);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 180, 214);
			expected.allocateNode(NodeType.RECORD_PATTERN, 225, 248);
			expected.allocateNode(NodeType.STRING_LITERAL, 252, 270);
			expected.allocateNode(NodeType.IDENTIFIER, 273, 274);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 252, 274);
			expected.allocateNode(NodeType.STRING_LITERAL, 277, 280);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 252, 280);
			expected.allocateNode(NodeType.IDENTIFIER, 283, 284);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 252, 284);
			expected.allocateNode(NodeType.STRING_LITERAL, 301, 321);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 134, 327);
			expected.allocateNode(NodeType.STRING_LITERAL, 343, 356);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 84, 361);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 77, 362);
			expected.allocateNode(NodeType.BLOCK, 73, 365);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 365);
			expected.allocateClassDeclaration(7, 367, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 368);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Reference Types ====================

	/**
	 * Validates parsing of a record pattern with reference type (Object) components.
	 * Tests: {@code case Person(String name, Integer age) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithObjectComponents()
	{
		String source = """
			public class Test
			{
				public String describe(Object obj)
				{
					return switch (obj)
					{
						case Person(String name, Integer age) -> name + " is " + age + " years old";
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
			expected.allocateParameterDeclaration(44, 54, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 76, 79);
			expected.allocateNode(NodeType.RECORD_PATTERN, 93, 125);
			expected.allocateNode(NodeType.IDENTIFIER, 129, 133);
			expected.allocateNode(NodeType.STRING_LITERAL, 136, 142);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 129, 142);
			expected.allocateNode(NodeType.IDENTIFIER, 145, 148);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 129, 148);
			expected.allocateNode(NodeType.STRING_LITERAL, 151, 163);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 129, 163);
			expected.allocateNode(NodeType.STRING_LITERAL, 179, 188);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 68, 193);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 61, 194);
			expected.allocateNode(NodeType.BLOCK, 57, 197);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 197);
			expected.allocateClassDeclaration(7, 199, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 200);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a record pattern with array type component.
	 * Tests: {@code case Container(String[] items) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithArrayTypeComponent()
	{
		String source = """
			public class Test
			{
				public int count(Object obj)
				{
					return switch (obj)
					{
						case Container(String[] items) -> items.length;
						default -> 0;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 44);
			expected.allocateParameterDeclaration(38, 48, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 70, 73);
			expected.allocateNode(NodeType.RECORD_PATTERN, 87, 112);
			expected.allocateNode(NodeType.IDENTIFIER, 116, 121);
			expected.allocateNode(NodeType.FIELD_ACCESS, 116, 128);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 144, 145);
			expected.allocateNode(NodeType.SWITCH_EXPRESSION, 62, 150);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 55, 151);
			expected.allocateNode(NodeType.BLOCK, 51, 154);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 154);
			expected.allocateClassDeclaration(7, 156, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 157);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
