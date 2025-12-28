package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(NodeType.IDENTIFIER, 66, 69),
			semanticNode(NodeType.RECORD_PATTERN, 83, 90),
			semanticNode(NodeType.QUALIFIED_NAME, 94, 112),
			semanticNode(NodeType.IDENTIFIER, 94, 100),
			semanticNode(NodeType.FIELD_ACCESS, 94, 104),
			semanticNode(NodeType.FIELD_ACCESS, 94, 112),
			semanticNode(NodeType.STRING_LITERAL, 113, 120),
			semanticNode(NodeType.METHOD_INVOCATION, 94, 121),
			semanticNode(NodeType.BLOCK, 137, 139),
			semanticNode(NodeType.SWITCH_STATEMENT, 58, 143),
			semanticNode(NodeType.BLOCK, 54, 146),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 146),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 148, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 149));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with a single component.
	 * Tests: {@code case Box(Item item) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithSingleComponent()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(NodeType.IDENTIFIER, 66, 69),
			semanticNode(NodeType.RECORD_PATTERN, 83, 97),
			semanticNode(NodeType.QUALIFIED_NAME, 101, 119),
			semanticNode(NodeType.IDENTIFIER, 101, 107),
			semanticNode(NodeType.FIELD_ACCESS, 101, 111),
			semanticNode(NodeType.FIELD_ACCESS, 101, 119),
			semanticNode(NodeType.IDENTIFIER, 120, 124),
			semanticNode(NodeType.METHOD_INVOCATION, 101, 125),
			semanticNode(NodeType.BLOCK, 141, 143),
			semanticNode(NodeType.SWITCH_STATEMENT, 58, 147),
			semanticNode(NodeType.BLOCK, 54, 150),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 150),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 152, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 153));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with two components.
	 * Tests: {@code case Point(int x, int y) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithMultipleComponents()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(NodeType.IDENTIFIER, 66, 69),
			semanticNode(NodeType.RECORD_PATTERN, 83, 102),
			semanticNode(NodeType.QUALIFIED_NAME, 106, 124),
			semanticNode(NodeType.IDENTIFIER, 106, 112),
			semanticNode(NodeType.FIELD_ACCESS, 106, 116),
			semanticNode(NodeType.FIELD_ACCESS, 106, 124),
			semanticNode(NodeType.IDENTIFIER, 125, 126),
			semanticNode(NodeType.STRING_LITERAL, 129, 133),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 133),
			semanticNode(NodeType.IDENTIFIER, 136, 137),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 137),
			semanticNode(NodeType.METHOD_INVOCATION, 106, 138),
			semanticNode(NodeType.BLOCK, 154, 156),
			semanticNode(NodeType.SWITCH_STATEMENT, 58, 160),
			semanticNode(NodeType.BLOCK, 54, 163),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 163),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 165, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 166));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with three components.
	 * Tests: {@code case RGB(int r, int g, int b) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithThreeComponents()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 72, 75),
			semanticNode(NodeType.RECORD_PATTERN, 89, 113),
			semanticNode(NodeType.IDENTIFIER, 117, 118),
			semanticNode(NodeType.IDENTIFIER, 121, 122),
			semanticNode(NodeType.BINARY_EXPRESSION, 117, 122),
			semanticNode(NodeType.IDENTIFIER, 125, 126),
			semanticNode(NodeType.BINARY_EXPRESSION, 117, 126),
			semanticNode(NodeType.INTEGER_LITERAL, 142, 143),
			semanticNode(NodeType.SWITCH_EXPRESSION, 64, 148),
			semanticNode(NodeType.RETURN_STATEMENT, 57, 149),
			semanticNode(NodeType.BLOCK, 53, 152),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 152),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 154, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 155));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with primitive type components.
	 * Tests: {@code case IntPair(int first, int second) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithPrimitiveComponents()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 36, 42),
			semanticNode(NodeType.IDENTIFIER, 68, 71),
			semanticNode(NodeType.RECORD_PATTERN, 85, 115),
			semanticNode(NodeType.IDENTIFIER, 119, 124),
			semanticNode(NodeType.IDENTIFIER, 127, 133),
			semanticNode(NodeType.BINARY_EXPRESSION, 119, 133),
			semanticNode(NodeType.INTEGER_LITERAL, 149, 150),
			semanticNode(NodeType.SWITCH_EXPRESSION, 60, 155),
			semanticNode(NodeType.RETURN_STATEMENT, 53, 156),
			semanticNode(NodeType.BLOCK, 49, 159),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 159),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 161, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 162));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Nested Record Patterns ====================

	/**
	 * Validates parsing of a nested record pattern.
	 * Tests: {@code case Box(Point(int x, int y)) ->}.
	 */
	@Test
	public void shouldParseNestedRecordPattern()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(NodeType.IDENTIFIER, 66, 69),
			semanticNode(NodeType.RECORD_PATTERN, 87, 106),
			semanticNode(NodeType.RECORD_PATTERN, 83, 107),
			semanticNode(NodeType.QUALIFIED_NAME, 111, 129),
			semanticNode(NodeType.IDENTIFIER, 111, 117),
			semanticNode(NodeType.FIELD_ACCESS, 111, 121),
			semanticNode(NodeType.FIELD_ACCESS, 111, 129),
			semanticNode(NodeType.IDENTIFIER, 130, 131),
			semanticNode(NodeType.IDENTIFIER, 134, 135),
			semanticNode(NodeType.BINARY_EXPRESSION, 130, 135),
			semanticNode(NodeType.METHOD_INVOCATION, 111, 136),
			semanticNode(NodeType.BLOCK, 152, 154),
			semanticNode(NodeType.SWITCH_STATEMENT, 58, 158),
			semanticNode(NodeType.BLOCK, 54, 161),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 161),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 163, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 164));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a deeply nested record pattern (three levels).
	 * Tests: {@code case Outer(Middle(Inner(int value))) ->}.
	 */
	@Test
	public void shouldParseDeeplyNestedRecordPattern()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 72, 75),
			semanticNode(NodeType.RECORD_PATTERN, 102, 118),
			semanticNode(NodeType.RECORD_PATTERN, 95, 119),
			semanticNode(NodeType.RECORD_PATTERN, 89, 120),
			semanticNode(NodeType.IDENTIFIER, 124, 129),
			semanticNode(NodeType.INTEGER_LITERAL, 145, 146),
			semanticNode(NodeType.SWITCH_EXPRESSION, 64, 151),
			semanticNode(NodeType.RETURN_STATEMENT, 57, 152),
			semanticNode(NodeType.BLOCK, 53, 155),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 155),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 157, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 158));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with mixed nested and simple components.
	 * Tests: {@code case Container(String name, Box(Item item)) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithMixedNestedComponents()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.RECORD_PATTERN, 116, 130),
			semanticNode(NodeType.RECORD_PATTERN, 93, 131),
			semanticNode(NodeType.IDENTIFIER, 135, 139),
			semanticNode(NodeType.STRING_LITERAL, 142, 146),
			semanticNode(NodeType.BINARY_EXPRESSION, 135, 146),
			semanticNode(NodeType.IDENTIFIER, 149, 153),
			semanticNode(NodeType.BINARY_EXPRESSION, 135, 153),
			semanticNode(NodeType.STRING_LITERAL, 169, 178),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 183),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 184),
			semanticNode(NodeType.BLOCK, 57, 187),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 187),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 189, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 190));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with multiple nested record patterns.
	 * Tests: {@code case Line(Point(int x1, int y1), Point(int x2, int y2)) ->}.
	 */
	@Test
	public void shouldParseMultipleNestedRecordPatterns()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 42, 48),
			semanticNode(NodeType.IDENTIFIER, 74, 77),
			semanticNode(NodeType.RECORD_PATTERN, 96, 117),
			semanticNode(NodeType.RECORD_PATTERN, 119, 140),
			semanticNode(NodeType.RECORD_PATTERN, 91, 141),
			semanticNode(NodeType.IDENTIFIER, 149, 153),
			semanticNode(NodeType.FIELD_ACCESS, 149, 158),
			semanticNode(NodeType.IDENTIFIER, 160, 162),
			semanticNode(NodeType.IDENTIFIER, 165, 167),
			semanticNode(NodeType.BINARY_EXPRESSION, 160, 167),
			semanticNode(NodeType.IDENTIFIER, 172, 174),
			semanticNode(NodeType.IDENTIFIER, 177, 179),
			semanticNode(NodeType.BINARY_EXPRESSION, 172, 179),
			semanticNode(NodeType.BINARY_EXPRESSION, 160, 179),
			semanticNode(NodeType.IDENTIFIER, 184, 186),
			semanticNode(NodeType.IDENTIFIER, 189, 191),
			semanticNode(NodeType.BINARY_EXPRESSION, 184, 191),
			semanticNode(NodeType.IDENTIFIER, 196, 198),
			semanticNode(NodeType.IDENTIFIER, 201, 203),
			semanticNode(NodeType.BINARY_EXPRESSION, 196, 203),
			semanticNode(NodeType.BINARY_EXPRESSION, 184, 203),
			semanticNode(NodeType.BINARY_EXPRESSION, 160, 203),
			semanticNode(NodeType.METHOD_INVOCATION, 149, 205),
			semanticNode(NodeType.DOUBLE_LITERAL, 221, 224),
			semanticNode(NodeType.SWITCH_EXPRESSION, 66, 229),
			semanticNode(NodeType.RETURN_STATEMENT, 59, 230),
			semanticNode(NodeType.BLOCK, 55, 233),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 233),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 235, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 236));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Guard Integration ====================

	/**
	 * Validates parsing of a record pattern with a guard clause.
	 * Tests: {@code case Point(int x, int y) when x > 0 ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithGuard()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.IDENTIFIER, 120, 121),
			semanticNode(NodeType.INTEGER_LITERAL, 124, 125),
			semanticNode(NodeType.BINARY_EXPRESSION, 120, 125),
			semanticNode(NodeType.RECORD_PATTERN, 95, 125),
			semanticNode(NodeType.STRING_LITERAL, 129, 141),
			semanticNode(NodeType.IDENTIFIER, 176, 177),
			semanticNode(NodeType.INTEGER_LITERAL, 180, 181),
			semanticNode(NodeType.BINARY_EXPRESSION, 176, 181),
			semanticNode(NodeType.RECORD_PATTERN, 151, 181),
			semanticNode(NodeType.STRING_LITERAL, 185, 197),
			semanticNode(NodeType.STRING_LITERAL, 213, 220),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 225),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 226),
			semanticNode(NodeType.BLOCK, 59, 229),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 229),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 231, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 232));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a nested record pattern with a guard clause.
	 * Tests: {@code case Box(Point(int x, int y)) when x > 0 && y > 0 ->}.
	 */
	@Test
	public void shouldParseNestedRecordPatternWithGuard()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.RECORD_PATTERN, 99, 118),
			semanticNode(NodeType.IDENTIFIER, 125, 126),
			semanticNode(NodeType.INTEGER_LITERAL, 129, 130),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 130),
			semanticNode(NodeType.IDENTIFIER, 134, 135),
			semanticNode(NodeType.INTEGER_LITERAL, 138, 139),
			semanticNode(NodeType.BINARY_EXPRESSION, 134, 139),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 139),
			semanticNode(NodeType.RECORD_PATTERN, 95, 139),
			semanticNode(NodeType.STRING_LITERAL, 143, 159),
			semanticNode(NodeType.RECORD_PATTERN, 173, 192),
			semanticNode(NodeType.IDENTIFIER, 199, 200),
			semanticNode(NodeType.INTEGER_LITERAL, 203, 204),
			semanticNode(NodeType.BINARY_EXPRESSION, 199, 204),
			semanticNode(NodeType.IDENTIFIER, 208, 209),
			semanticNode(NodeType.INTEGER_LITERAL, 212, 213),
			semanticNode(NodeType.BINARY_EXPRESSION, 208, 213),
			semanticNode(NodeType.BINARY_EXPRESSION, 199, 213),
			semanticNode(NodeType.RECORD_PATTERN, 169, 213),
			semanticNode(NodeType.STRING_LITERAL, 217, 234),
			semanticNode(NodeType.STRING_LITERAL, 250, 257),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 262),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 263),
			semanticNode(NodeType.BLOCK, 59, 266),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 266),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 268, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 269));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with a method call in the guard expression.
	 * Tests: {@code case Person(String name, int age) when name.startsWith("A") ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithMethodCallInGuard()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.IDENTIFIER, 129, 133),
			semanticNode(NodeType.FIELD_ACCESS, 129, 144),
			semanticNode(NodeType.STRING_LITERAL, 145, 148),
			semanticNode(NodeType.METHOD_INVOCATION, 129, 149),
			semanticNode(NodeType.RECORD_PATTERN, 95, 149),
			semanticNode(NodeType.STRING_LITERAL, 153, 161),
			semanticNode(NodeType.IDENTIFIER, 205, 208),
			semanticNode(NodeType.INTEGER_LITERAL, 211, 213),
			semanticNode(NodeType.BINARY_EXPRESSION, 205, 213),
			semanticNode(NodeType.RECORD_PATTERN, 171, 213),
			semanticNode(NodeType.STRING_LITERAL, 217, 224),
			semanticNode(NodeType.STRING_LITERAL, 240, 247),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 252),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 253),
			semanticNode(NodeType.BLOCK, 59, 256),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 256),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 258, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 259));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Qualified Types ====================

	/**
	 * Validates parsing of a record pattern with a qualified type name.
	 * Tests: {@code case java.awt.Point(int x, int y) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithQualifiedType()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.RECORD_PATTERN, 93, 121),
			semanticNode(NodeType.STRING_LITERAL, 125, 136),
			semanticNode(NodeType.IDENTIFIER, 139, 140),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 140),
			semanticNode(NodeType.STRING_LITERAL, 143, 147),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 147),
			semanticNode(NodeType.IDENTIFIER, 150, 151),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 151),
			semanticNode(NodeType.STRING_LITERAL, 167, 176),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 181),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 182),
			semanticNode(NodeType.BLOCK, 57, 185),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 185),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 187, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 188));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of nested record patterns with qualified types.
	 * Tests: {@code case com.example.Container(com.example.Point(int x, int y)) ->}.
	 */
	@Test
	public void shouldParseNestedRecordPatternWithQualifiedTypes()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 72, 75),
			semanticNode(NodeType.RECORD_PATTERN, 111, 142),
			semanticNode(NodeType.RECORD_PATTERN, 89, 143),
			semanticNode(NodeType.IDENTIFIER, 147, 148),
			semanticNode(NodeType.IDENTIFIER, 151, 152),
			semanticNode(NodeType.BINARY_EXPRESSION, 147, 152),
			semanticNode(NodeType.INTEGER_LITERAL, 168, 169),
			semanticNode(NodeType.SWITCH_EXPRESSION, 64, 174),
			semanticNode(NodeType.RETURN_STATEMENT, 57, 175),
			semanticNode(NodeType.BLOCK, 53, 178),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 178),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 180, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 181));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Switch Forms ====================

	/**
	 * Validates parsing of a record pattern in a switch statement with colon syntax.
	 * Tests traditional switch statement form with record patterns.
	 */
	@Test
	public void shouldParseRecordPatternInSwitchStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(NodeType.IDENTIFIER, 66, 69),
			semanticNode(NodeType.RECORD_PATTERN, 83, 102),
			semanticNode(NodeType.QUALIFIED_NAME, 108, 126),
			semanticNode(NodeType.IDENTIFIER, 108, 114),
			semanticNode(NodeType.FIELD_ACCESS, 108, 118),
			semanticNode(NodeType.FIELD_ACCESS, 108, 126),
			semanticNode(NodeType.STRING_LITERAL, 127, 136),
			semanticNode(NodeType.IDENTIFIER, 139, 140),
			semanticNode(NodeType.BINARY_EXPRESSION, 127, 140),
			semanticNode(NodeType.STRING_LITERAL, 143, 147),
			semanticNode(NodeType.BINARY_EXPRESSION, 127, 147),
			semanticNode(NodeType.IDENTIFIER, 150, 151),
			semanticNode(NodeType.BINARY_EXPRESSION, 127, 151),
			semanticNode(NodeType.METHOD_INVOCATION, 108, 152),
			semanticNode(NodeType.BREAK_STATEMENT, 158, 164),
			semanticNode(NodeType.RECORD_PATTERN, 173, 191),
			semanticNode(NodeType.QUALIFIED_NAME, 197, 215),
			semanticNode(NodeType.IDENTIFIER, 197, 203),
			semanticNode(NodeType.FIELD_ACCESS, 197, 207),
			semanticNode(NodeType.FIELD_ACCESS, 197, 215),
			semanticNode(NodeType.STRING_LITERAL, 216, 226),
			semanticNode(NodeType.IDENTIFIER, 229, 235),
			semanticNode(NodeType.BINARY_EXPRESSION, 216, 235),
			semanticNode(NodeType.METHOD_INVOCATION, 197, 236),
			semanticNode(NodeType.BREAK_STATEMENT, 242, 248),
			semanticNode(NodeType.QUALIFIED_NAME, 265, 283),
			semanticNode(NodeType.IDENTIFIER, 265, 271),
			semanticNode(NodeType.FIELD_ACCESS, 265, 275),
			semanticNode(NodeType.FIELD_ACCESS, 265, 283),
			semanticNode(NodeType.STRING_LITERAL, 284, 293),
			semanticNode(NodeType.METHOD_INVOCATION, 265, 294),
			semanticNode(NodeType.BREAK_STATEMENT, 300, 306),
			semanticNode(NodeType.SWITCH_STATEMENT, 58, 310),
			semanticNode(NodeType.BLOCK, 54, 313),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 313),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 315, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 316));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern in a switch expression with arrow syntax.
	 * Tests modern switch expression form with record patterns.
	 */
	@Test
	public void shouldParseRecordPatternInSwitchExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.RECORD_PATTERN, 93, 112),
			semanticNode(NodeType.STRING_LITERAL, 116, 125),
			semanticNode(NodeType.IDENTIFIER, 128, 129),
			semanticNode(NodeType.BINARY_EXPRESSION, 116, 129),
			semanticNode(NodeType.STRING_LITERAL, 132, 136),
			semanticNode(NodeType.BINARY_EXPRESSION, 116, 136),
			semanticNode(NodeType.IDENTIFIER, 139, 140),
			semanticNode(NodeType.BINARY_EXPRESSION, 116, 140),
			semanticNode(NodeType.RECORD_PATTERN, 150, 168),
			semanticNode(NodeType.STRING_LITERAL, 172, 182),
			semanticNode(NodeType.IDENTIFIER, 185, 191),
			semanticNode(NodeType.BINARY_EXPRESSION, 172, 191),
			semanticNode(NodeType.STRING_LITERAL, 207, 216),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 221),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 222),
			semanticNode(NodeType.BLOCK, 57, 225),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 225),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 227, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 228));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Multiple Cases ====================

	/**
	 * Validates parsing of multiple record pattern case labels in sequence.
	 * Tests multiple record patterns each with their own case clause.
	 */
	@Test
	public void shouldParseMultipleRecordPatternCases()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 72, 75),
			semanticNode(NodeType.RECORD_PATTERN, 89, 112),
			semanticNode(NodeType.IDENTIFIER, 116, 117),
			semanticNode(NodeType.IDENTIFIER, 120, 121),
			semanticNode(NodeType.BINARY_EXPRESSION, 116, 121),
			semanticNode(NodeType.RECORD_PATTERN, 131, 144),
			semanticNode(NodeType.IDENTIFIER, 148, 152),
			semanticNode(NodeType.FIELD_ACCESS, 148, 155),
			semanticNode(NodeType.IDENTIFIER, 158, 159),
			semanticNode(NodeType.BINARY_EXPRESSION, 148, 159),
			semanticNode(NodeType.IDENTIFIER, 162, 163),
			semanticNode(NodeType.BINARY_EXPRESSION, 148, 163),
			semanticNode(NodeType.RECORD_PATTERN, 173, 203),
			semanticNode(NodeType.DOUBLE_LITERAL, 207, 210),
			semanticNode(NodeType.IDENTIFIER, 213, 217),
			semanticNode(NodeType.BINARY_EXPRESSION, 207, 217),
			semanticNode(NodeType.IDENTIFIER, 220, 226),
			semanticNode(NodeType.BINARY_EXPRESSION, 207, 226),
			semanticNode(NodeType.INTEGER_LITERAL, 242, 243),
			semanticNode(NodeType.SWITCH_EXPRESSION, 64, 248),
			semanticNode(NodeType.RETURN_STATEMENT, 57, 249),
			semanticNode(NodeType.BLOCK, 53, 252),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 252),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 254, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 255));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a mix of record patterns and type patterns in the same switch.
	 * Tests combining record pattern matching with simple type pattern matching.
	 */
	@Test
	public void shouldParseMixOfRecordAndTypePatterns()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.RECORD_PATTERN, 93, 112),
			semanticNode(NodeType.STRING_LITERAL, 116, 127),
			semanticNode(NodeType.IDENTIFIER, 130, 131),
			semanticNode(NodeType.BINARY_EXPRESSION, 116, 131),
			semanticNode(NodeType.STRING_LITERAL, 134, 138),
			semanticNode(NodeType.BINARY_EXPRESSION, 116, 138),
			semanticNode(NodeType.IDENTIFIER, 141, 142),
			semanticNode(NodeType.BINARY_EXPRESSION, 116, 142),
			semanticNode(NodeType.STRING_LITERAL, 164, 174),
			semanticNode(NodeType.IDENTIFIER, 177, 178),
			semanticNode(NodeType.BINARY_EXPRESSION, 164, 178),
			semanticNode(NodeType.STRING_LITERAL, 201, 212),
			semanticNode(NodeType.IDENTIFIER, 215, 216),
			semanticNode(NodeType.BINARY_EXPRESSION, 201, 216),
			semanticNode(NodeType.STRING_LITERAL, 234, 240),
			semanticNode(NodeType.STRING_LITERAL, 256, 263),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 268),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 269),
			semanticNode(NodeType.BLOCK, 57, 272),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 272),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 274, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 275));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Edge Cases ====================

	/**
	 * Validates parsing of a record pattern with {@code var} type inference for components.
	 * Tests: {@code case Point(var x, var y) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithVarComponent()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 36, 42),
			semanticNode(NodeType.IDENTIFIER, 68, 71),
			semanticNode(NodeType.RECORD_PATTERN, 85, 104),
			semanticNode(NodeType.IDENTIFIER, 108, 109),
			semanticNode(NodeType.IDENTIFIER, 112, 113),
			semanticNode(NodeType.BINARY_EXPRESSION, 108, 113),
			semanticNode(NodeType.INTEGER_LITERAL, 129, 130),
			semanticNode(NodeType.SWITCH_EXPRESSION, 60, 135),
			semanticNode(NodeType.RETURN_STATEMENT, 53, 136),
			semanticNode(NodeType.BLOCK, 49, 139),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 139),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 141, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 142));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with an unnamed component (underscore).
	 * Tests: {@code case Point(int x, int _) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithUnnamedComponent()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 37, 43),
			semanticNode(NodeType.IDENTIFIER, 69, 72),
			semanticNode(NodeType.RECORD_PATTERN, 86, 105),
			semanticNode(NodeType.IDENTIFIER, 109, 110),
			semanticNode(NodeType.INTEGER_LITERAL, 126, 127),
			semanticNode(NodeType.SWITCH_EXPRESSION, 61, 132),
			semanticNode(NodeType.RETURN_STATEMENT, 54, 133),
			semanticNode(NodeType.BLOCK, 50, 136),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 136),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 138, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 139));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern inside a nested switch expression.
	 * Tests record patterns in both outer and inner switch expressions.
	 */
	@Test
	public void shouldParseRecordPatternInNestedSwitch()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(NodeType.QUALIFIED_NAME, 58, 64),
			semanticNode(NodeType.IDENTIFIER, 92, 97),
			semanticNode(NodeType.RECORD_PATTERN, 111, 130),
			semanticNode(NodeType.IDENTIFIER, 142, 147),
			semanticNode(NodeType.RECORD_PATTERN, 163, 176),
			semanticNode(NodeType.STRING_LITERAL, 180, 210),
			semanticNode(NodeType.IDENTIFIER, 213, 214),
			semanticNode(NodeType.BINARY_EXPRESSION, 180, 214),
			semanticNode(NodeType.RECORD_PATTERN, 225, 248),
			semanticNode(NodeType.STRING_LITERAL, 252, 270),
			semanticNode(NodeType.IDENTIFIER, 273, 274),
			semanticNode(NodeType.BINARY_EXPRESSION, 252, 274),
			semanticNode(NodeType.STRING_LITERAL, 277, 280),
			semanticNode(NodeType.BINARY_EXPRESSION, 252, 280),
			semanticNode(NodeType.IDENTIFIER, 283, 284),
			semanticNode(NodeType.BINARY_EXPRESSION, 252, 284),
			semanticNode(NodeType.STRING_LITERAL, 301, 321),
			semanticNode(NodeType.SWITCH_EXPRESSION, 134, 327),
			semanticNode(NodeType.STRING_LITERAL, 343, 356),
			semanticNode(NodeType.SWITCH_EXPRESSION, 84, 361),
			semanticNode(NodeType.RETURN_STATEMENT, 77, 362),
			semanticNode(NodeType.BLOCK, 73, 365),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 365),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 367, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 368));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Reference Types ====================

	/**
	 * Validates parsing of a record pattern with reference type (Object) components.
	 * Tests: {@code case Person(String name, Integer age) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithObjectComponents()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.RECORD_PATTERN, 93, 125),
			semanticNode(NodeType.IDENTIFIER, 129, 133),
			semanticNode(NodeType.STRING_LITERAL, 136, 142),
			semanticNode(NodeType.BINARY_EXPRESSION, 129, 142),
			semanticNode(NodeType.IDENTIFIER, 145, 148),
			semanticNode(NodeType.BINARY_EXPRESSION, 129, 148),
			semanticNode(NodeType.STRING_LITERAL, 151, 163),
			semanticNode(NodeType.BINARY_EXPRESSION, 129, 163),
			semanticNode(NodeType.STRING_LITERAL, 179, 188),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 193),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 194),
			semanticNode(NodeType.BLOCK, 57, 197),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 197),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 199, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 200));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a record pattern with array type component.
	 * Tests: {@code case Container(String[] items) ->}.
	 */
	@Test
	public void shouldParseRecordPatternWithArrayTypeComponent()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 38, 44),
			semanticNode(NodeType.IDENTIFIER, 70, 73),
			semanticNode(NodeType.RECORD_PATTERN, 87, 112),
			semanticNode(NodeType.IDENTIFIER, 116, 121),
			semanticNode(NodeType.FIELD_ACCESS, 116, 128),
			semanticNode(NodeType.INTEGER_LITERAL, 144, 145),
			semanticNode(NodeType.SWITCH_EXPRESSION, 62, 150),
			semanticNode(NodeType.RETURN_STATEMENT, 55, 151),
			semanticNode(NodeType.BLOCK, 51, 154),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 154),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 156, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 157));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
