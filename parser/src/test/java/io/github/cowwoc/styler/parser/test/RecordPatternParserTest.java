package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			qualifiedName( 41, 47),
			identifier( 66, 69),
			recordPattern( 83, 90),
			qualifiedName( 94, 112),
			identifier( 94, 100),
			fieldAccess( 94, 104),
			fieldAccess( 94, 112),
			stringLiteral( 113, 120),
			methodInvocation( 94, 121),
			block( 137, 139),
			switchStatement( 58, 143),
			block( 54, 146),
			methodDeclaration( 21, 146),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 148, "Test"),
			compilationUnit( 0, 149),
			parameterNode( 41, 51, "obj"));
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
			qualifiedName( 41, 47),
			identifier( 66, 69),
			recordPattern( 83, 97),
			qualifiedName( 101, 119),
			identifier( 101, 107),
			fieldAccess( 101, 111),
			fieldAccess( 101, 119),
			identifier( 120, 124),
			methodInvocation( 101, 125),
			block( 141, 143),
			switchStatement( 58, 147),
			block( 54, 150),
			methodDeclaration( 21, 150),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 152, "Test"),
			compilationUnit( 0, 153),
			parameterNode( 41, 51, "obj"));
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
			qualifiedName( 41, 47),
			identifier( 66, 69),
			recordPattern( 83, 102),
			qualifiedName( 106, 124),
			identifier( 106, 112),
			fieldAccess( 106, 116),
			fieldAccess( 106, 124),
			identifier( 125, 126),
			stringLiteral( 129, 133),
			binaryExpression( 125, 133),
			identifier( 136, 137),
			binaryExpression( 125, 137),
			methodInvocation( 106, 138),
			block( 154, 156),
			switchStatement( 58, 160),
			block( 54, 163),
			methodDeclaration( 21, 163),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 165, "Test"),
			compilationUnit( 0, 166),
			parameterNode( 41, 51, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 72, 75),
			recordPattern( 89, 113),
			identifier( 117, 118),
			identifier( 121, 122),
			binaryExpression( 117, 122),
			identifier( 125, 126),
			binaryExpression( 117, 126),
			integerLiteral( 142, 143),
			switchExpression( 64, 148),
			returnStatement( 57, 149),
			block( 53, 152),
			methodDeclaration( 21, 152),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 154, "Test"),
			compilationUnit( 0, 155),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 36, 42),
			identifier( 68, 71),
			recordPattern( 85, 115),
			identifier( 119, 124),
			identifier( 127, 133),
			binaryExpression( 119, 133),
			integerLiteral( 149, 150),
			switchExpression( 60, 155),
			returnStatement( 53, 156),
			block( 49, 159),
			methodDeclaration( 21, 159),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 161, "Test"),
			compilationUnit( 0, 162),
			parameterNode( 36, 46, "obj"));
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
			qualifiedName( 41, 47),
			identifier( 66, 69),
			recordPattern( 87, 106),
			recordPattern( 83, 107),
			qualifiedName( 111, 129),
			identifier( 111, 117),
			fieldAccess( 111, 121),
			fieldAccess( 111, 129),
			identifier( 130, 131),
			identifier( 134, 135),
			binaryExpression( 130, 135),
			methodInvocation( 111, 136),
			block( 152, 154),
			switchStatement( 58, 158),
			block( 54, 161),
			methodDeclaration( 21, 161),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 163, "Test"),
			compilationUnit( 0, 164),
			parameterNode( 41, 51, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 72, 75),
			recordPattern( 102, 118),
			recordPattern( 95, 119),
			recordPattern( 89, 120),
			identifier( 124, 129),
			integerLiteral( 145, 146),
			switchExpression( 64, 151),
			returnStatement( 57, 152),
			block( 53, 155),
			methodDeclaration( 21, 155),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 157, "Test"),
			compilationUnit( 0, 158),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 44, 50),
			identifier( 76, 79),
			recordPattern( 116, 130),
			recordPattern( 93, 131),
			identifier( 135, 139),
			stringLiteral( 142, 146),
			binaryExpression( 135, 146),
			identifier( 149, 153),
			binaryExpression( 135, 153),
			stringLiteral( 169, 178),
			switchExpression( 68, 183),
			returnStatement( 61, 184),
			block( 57, 187),
			methodDeclaration( 21, 187),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 189, "Test"),
			compilationUnit( 0, 190),
			parameterNode( 44, 54, "obj"));
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
			qualifiedName( 42, 48),
			identifier( 74, 77),
			recordPattern( 96, 117),
			recordPattern( 119, 140),
			recordPattern( 91, 141),
			identifier( 149, 153),
			fieldAccess( 149, 158),
			identifier( 160, 162),
			identifier( 165, 167),
			binaryExpression( 160, 167),
			identifier( 172, 174),
			identifier( 177, 179),
			binaryExpression( 172, 179),
			binaryExpression( 160, 179),
			identifier( 184, 186),
			identifier( 189, 191),
			binaryExpression( 184, 191),
			identifier( 196, 198),
			identifier( 201, 203),
			binaryExpression( 196, 203),
			binaryExpression( 184, 203),
			binaryExpression( 160, 203),
			methodInvocation( 149, 205),
			doubleLiteral( 221, 224),
			switchExpression( 66, 229),
			returnStatement( 59, 230),
			block( 55, 233),
			methodDeclaration( 21, 233),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 235, "Test"),
			compilationUnit( 0, 236),
			parameterNode( 42, 52, "obj"));
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
			qualifiedName( 46, 52),
			identifier( 78, 81),
			identifier( 120, 121),
			integerLiteral( 124, 125),
			binaryExpression( 120, 125),
			recordPattern( 95, 125),
			stringLiteral( 129, 141),
			identifier( 176, 177),
			integerLiteral( 180, 181),
			binaryExpression( 176, 181),
			recordPattern( 151, 181),
			stringLiteral( 185, 197),
			stringLiteral( 213, 220),
			switchExpression( 70, 225),
			returnStatement( 63, 226),
			block( 59, 229),
			methodDeclaration( 21, 229),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 231, "Test"),
			compilationUnit( 0, 232),
			parameterNode( 46, 56, "obj"));
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
			qualifiedName( 46, 52),
			identifier( 78, 81),
			recordPattern( 99, 118),
			identifier( 125, 126),
			integerLiteral( 129, 130),
			binaryExpression( 125, 130),
			identifier( 134, 135),
			integerLiteral( 138, 139),
			binaryExpression( 134, 139),
			binaryExpression( 125, 139),
			recordPattern( 95, 139),
			stringLiteral( 143, 159),
			recordPattern( 173, 192),
			identifier( 199, 200),
			integerLiteral( 203, 204),
			binaryExpression( 199, 204),
			identifier( 208, 209),
			integerLiteral( 212, 213),
			binaryExpression( 208, 213),
			binaryExpression( 199, 213),
			recordPattern( 169, 213),
			stringLiteral( 217, 234),
			stringLiteral( 250, 257),
			switchExpression( 70, 262),
			returnStatement( 63, 263),
			block( 59, 266),
			methodDeclaration( 21, 266),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 268, "Test"),
			compilationUnit( 0, 269),
			parameterNode( 46, 56, "obj"));
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
			qualifiedName( 46, 52),
			identifier( 78, 81),
			identifier( 129, 133),
			fieldAccess( 129, 144),
			stringLiteral( 145, 148),
			methodInvocation( 129, 149),
			recordPattern( 95, 149),
			stringLiteral( 153, 161),
			identifier( 205, 208),
			integerLiteral( 211, 213),
			binaryExpression( 205, 213),
			recordPattern( 171, 213),
			stringLiteral( 217, 224),
			stringLiteral( 240, 247),
			switchExpression( 70, 252),
			returnStatement( 63, 253),
			block( 59, 256),
			methodDeclaration( 21, 256),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 258, "Test"),
			compilationUnit( 0, 259),
			parameterNode( 46, 56, "obj"));
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
			qualifiedName( 44, 50),
			identifier( 76, 79),
			recordPattern( 93, 121),
			stringLiteral( 125, 136),
			identifier( 139, 140),
			binaryExpression( 125, 140),
			stringLiteral( 143, 147),
			binaryExpression( 125, 147),
			identifier( 150, 151),
			binaryExpression( 125, 151),
			stringLiteral( 167, 176),
			switchExpression( 68, 181),
			returnStatement( 61, 182),
			block( 57, 185),
			methodDeclaration( 21, 185),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 187, "Test"),
			compilationUnit( 0, 188),
			parameterNode( 44, 54, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 72, 75),
			recordPattern( 111, 142),
			recordPattern( 89, 143),
			identifier( 147, 148),
			identifier( 151, 152),
			binaryExpression( 147, 152),
			integerLiteral( 168, 169),
			switchExpression( 64, 174),
			returnStatement( 57, 175),
			block( 53, 178),
			methodDeclaration( 21, 178),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 180, "Test"),
			compilationUnit( 0, 181),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 41, 47),
			identifier( 66, 69),
			recordPattern( 83, 102),
			qualifiedName( 108, 126),
			identifier( 108, 114),
			fieldAccess( 108, 118),
			fieldAccess( 108, 126),
			stringLiteral( 127, 136),
			identifier( 139, 140),
			binaryExpression( 127, 140),
			stringLiteral( 143, 147),
			binaryExpression( 127, 147),
			identifier( 150, 151),
			binaryExpression( 127, 151),
			methodInvocation( 108, 152),
			breakStatement( 158, 164),
			recordPattern( 173, 191),
			qualifiedName( 197, 215),
			identifier( 197, 203),
			fieldAccess( 197, 207),
			fieldAccess( 197, 215),
			stringLiteral( 216, 226),
			identifier( 229, 235),
			binaryExpression( 216, 235),
			methodInvocation( 197, 236),
			breakStatement( 242, 248),
			qualifiedName( 265, 283),
			identifier( 265, 271),
			fieldAccess( 265, 275),
			fieldAccess( 265, 283),
			stringLiteral( 284, 293),
			methodInvocation( 265, 294),
			breakStatement( 300, 306),
			switchStatement( 58, 310),
			block( 54, 313),
			methodDeclaration( 21, 313),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 315, "Test"),
			compilationUnit( 0, 316),
			parameterNode( 41, 51, "obj"));
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
			qualifiedName( 44, 50),
			identifier( 76, 79),
			recordPattern( 93, 112),
			stringLiteral( 116, 125),
			identifier( 128, 129),
			binaryExpression( 116, 129),
			stringLiteral( 132, 136),
			binaryExpression( 116, 136),
			identifier( 139, 140),
			binaryExpression( 116, 140),
			recordPattern( 150, 168),
			stringLiteral( 172, 182),
			identifier( 185, 191),
			binaryExpression( 172, 191),
			stringLiteral( 207, 216),
			switchExpression( 68, 221),
			returnStatement( 61, 222),
			block( 57, 225),
			methodDeclaration( 21, 225),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 227, "Test"),
			compilationUnit( 0, 228),
			parameterNode( 44, 54, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 72, 75),
			recordPattern( 89, 112),
			identifier( 116, 117),
			identifier( 120, 121),
			binaryExpression( 116, 121),
			recordPattern( 131, 144),
			identifier( 148, 152),
			fieldAccess( 148, 155),
			identifier( 158, 159),
			binaryExpression( 148, 159),
			identifier( 162, 163),
			binaryExpression( 148, 163),
			recordPattern( 173, 203),
			doubleLiteral( 207, 210),
			identifier( 213, 217),
			binaryExpression( 207, 217),
			identifier( 220, 226),
			binaryExpression( 207, 226),
			integerLiteral( 242, 243),
			switchExpression( 64, 248),
			returnStatement( 57, 249),
			block( 53, 252),
			methodDeclaration( 21, 252),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 254, "Test"),
			compilationUnit( 0, 255),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 44, 50),
			identifier( 76, 79),
			recordPattern( 93, 112),
			stringLiteral( 116, 127),
			identifier( 130, 131),
			binaryExpression( 116, 131),
			stringLiteral( 134, 138),
			binaryExpression( 116, 138),
			identifier( 141, 142),
			binaryExpression( 116, 142),
			stringLiteral( 164, 174),
			identifier( 177, 178),
			binaryExpression( 164, 178),
			stringLiteral( 201, 212),
			identifier( 215, 216),
			binaryExpression( 201, 216),
			stringLiteral( 234, 240),
			stringLiteral( 256, 263),
			switchExpression( 68, 268),
			returnStatement( 61, 269),
			block( 57, 272),
			methodDeclaration( 21, 272),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 274, "Test"),
			compilationUnit( 0, 275),
			parameterNode( 44, 54, "obj"));
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
			qualifiedName( 36, 42),
			identifier( 68, 71),
			recordPattern( 85, 104),
			identifier( 108, 109),
			identifier( 112, 113),
			binaryExpression( 108, 113),
			integerLiteral( 129, 130),
			switchExpression( 60, 135),
			returnStatement( 53, 136),
			block( 49, 139),
			methodDeclaration( 21, 139),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 141, "Test"),
			compilationUnit( 0, 142),
			parameterNode( 36, 46, "obj"));
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
			qualifiedName( 37, 43),
			identifier( 69, 72),
			recordPattern( 86, 105),
			identifier( 109, 110),
			integerLiteral( 126, 127),
			switchExpression( 61, 132),
			returnStatement( 54, 133),
			block( 50, 136),
			methodDeclaration( 21, 136),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 138, "Test"),
			compilationUnit( 0, 139),
			parameterNode( 37, 47, "obj"));
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
			qualifiedName( 44, 50),
			qualifiedName( 58, 64),
			identifier( 92, 97),
			recordPattern( 111, 130),
			identifier( 142, 147),
			recordPattern( 163, 176),
			stringLiteral( 180, 210),
			identifier( 213, 214),
			binaryExpression( 180, 214),
			recordPattern( 225, 248),
			stringLiteral( 252, 270),
			identifier( 273, 274),
			binaryExpression( 252, 274),
			stringLiteral( 277, 280),
			binaryExpression( 252, 280),
			identifier( 283, 284),
			binaryExpression( 252, 284),
			stringLiteral( 301, 321),
			switchExpression( 134, 327),
			stringLiteral( 343, 356),
			switchExpression( 84, 361),
			returnStatement( 77, 362),
			block( 73, 365),
			methodDeclaration( 21, 365),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 367, "Test"),
			compilationUnit( 0, 368),
			parameterNode( 44, 56, "outer"),
			parameterNode( 58, 70, "inner"));
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
			qualifiedName( 44, 50),
			identifier( 76, 79),
			recordPattern( 93, 125),
			identifier( 129, 133),
			stringLiteral( 136, 142),
			binaryExpression( 129, 142),
			identifier( 145, 148),
			binaryExpression( 129, 148),
			stringLiteral( 151, 163),
			binaryExpression( 129, 163),
			stringLiteral( 179, 188),
			switchExpression( 68, 193),
			returnStatement( 61, 194),
			block( 57, 197),
			methodDeclaration( 21, 197),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 199, "Test"),
			compilationUnit( 0, 200),
			parameterNode( 44, 54, "obj"));
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
			qualifiedName( 38, 44),
			identifier( 70, 73),
			recordPattern( 87, 112),
			identifier( 116, 121),
			fieldAccess( 116, 128),
			integerLiteral( 144, 145),
			switchExpression( 62, 150),
			returnStatement( 55, 151),
			block( 51, 154),
			methodDeclaration( 21, 154),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 156, "Test"),
			compilationUnit( 0, 157),
			parameterNode( 38, 48, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
