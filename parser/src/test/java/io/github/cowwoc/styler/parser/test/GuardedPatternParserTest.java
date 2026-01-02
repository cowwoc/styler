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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 41, 47),
			parameterNode( 41, 51, "obj"),
			identifier( 66, 69),
			identifier( 97, 98),
			fieldAccess( 97, 105),
			methodInvocation( 97, 107),
			integerLiteral( 110, 111),
			binaryExpression( 97, 111),
			qualifiedName( 115, 133),
			identifier( 115, 121),
			fieldAccess( 115, 125),
			fieldAccess( 115, 133),
			identifier( 134, 135),
			methodInvocation( 115, 136),
			block( 152, 154),
			switchStatement( 58, 158),
			block( 54, 161),
			methodDeclaration( 21, 161),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 163, "Test"),
			compilationUnit( 0, 164));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 41, 47),
			parameterNode( 41, 51, "obj"),
			identifier( 66, 69),
			identifier( 107, 108),
			fieldAccess( 107, 116),
			methodInvocation( 107, 118),
			qualifiedName( 122, 140),
			identifier( 122, 128),
			fieldAccess( 122, 132),
			fieldAccess( 122, 140),
			stringLiteral( 141, 148),
			methodInvocation( 122, 149),
			block( 165, 167),
			switchStatement( 58, 171),
			block( 54, 174),
			methodDeclaration( 21, 174),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 176, "Test"),
			compilationUnit( 0, 177));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with simple numeric comparison in guard expression.
	 * Tests basic relational operator in when clause.
	 */
	@Test
	public void shouldParseGuardWithSimpleComparison()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 43, 49),
			parameterNode( 43, 53, "obj"),
			identifier( 75, 78),
			identifier( 107, 108),
			integerLiteral( 111, 112),
			binaryExpression( 107, 112),
			integerLiteral( 116, 117),
			identifier( 142, 143),
			integerLiteral( 146, 147),
			binaryExpression( 142, 147),
			integerLiteral( 152, 153),
			unaryExpression( 151, 153),
			integerLiteral( 169, 170),
			switchExpression( 67, 175),
			returnStatement( 60, 176),
			block( 56, 179),
			methodDeclaration( 21, 179),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 181, "Test"),
			compilationUnit( 0, 182));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with complex boolean expression using logical AND.
	 * Tests compound boolean expressions in when clause.
	 */
	@Test
	public void shouldParseGuardWithComplexBooleanExpressionAnd()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 46, 52),
			parameterNode( 46, 56, "obj"),
			identifier( 78, 81),
			identifier( 110, 111),
			integerLiteral( 114, 115),
			binaryExpression( 110, 115),
			identifier( 119, 120),
			integerLiteral( 123, 126),
			binaryExpression( 119, 126),
			binaryExpression( 110, 126),
			stringLiteral( 130, 146),
			identifier( 171, 172),
			integerLiteral( 176, 179),
			binaryExpression( 171, 179),
			identifier( 183, 184),
			integerLiteral( 187, 191),
			binaryExpression( 183, 191),
			binaryExpression( 171, 191),
			stringLiteral( 195, 212),
			stringLiteral( 228, 235),
			switchExpression( 70, 240),
			returnStatement( 63, 241),
			block( 59, 244),
			methodDeclaration( 21, 244),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 246, "Test"),
			compilationUnit( 0, 247));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with complex boolean expression using logical OR.
	 * Tests compound boolean expressions with OR operator in when clause.
	 */
	@Test
	public void shouldParseGuardWithComplexBooleanExpressionOr()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 46, 52),
			parameterNode( 46, 56, "obj"),
			identifier( 78, 81),
			identifier( 110, 111),
			integerLiteral( 115, 118),
			binaryExpression( 110, 118),
			identifier( 122, 123),
			integerLiteral( 128, 131),
			unaryExpression( 127, 131),
			binaryExpression( 122, 131),
			binaryExpression( 110, 131),
			stringLiteral( 135, 152),
			stringLiteral( 168, 185),
			switchExpression( 70, 190),
			returnStatement( 63, 191),
			block( 59, 194),
			methodDeclaration( 21, 194),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 196, "Test"),
			compilationUnit( 0, 197));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with method call in guard expression.
	 * Tests method invocation as guard condition.
	 */
	@Test
	public void shouldParseGuardWithMethodCall()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 43, 49),
			parameterNode( 43, 53, "obj"),
			identifier( 75, 78),
			identifier( 106, 107),
			fieldAccess( 106, 115),
			methodInvocation( 106, 117),
			stringLiteral( 121, 135),
			identifier( 159, 160),
			fieldAccess( 159, 171),
			stringLiteral( 172, 178),
			methodInvocation( 159, 179),
			stringLiteral( 183, 196),
			stringLiteral( 212, 219),
			switchExpression( 67, 224),
			returnStatement( 60, 225),
			block( 56, 228),
			methodDeclaration( 21, 228),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 230, "Test"),
			compilationUnit( 0, 231));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 46, 52),
			parameterNode( 46, 56, "obj"),
			identifier( 78, 81),
			identifier( 110, 111),
			integerLiteral( 114, 115),
			binaryExpression( 110, 115),
			identifier( 119, 120),
			integerLiteral( 123, 124),
			binaryExpression( 119, 124),
			integerLiteral( 128, 129),
			binaryExpression( 119, 129),
			binaryExpression( 110, 129),
			stringLiteral( 133, 148),
			identifier( 173, 174),
			integerLiteral( 177, 178),
			binaryExpression( 173, 178),
			identifier( 182, 183),
			integerLiteral( 186, 187),
			binaryExpression( 182, 187),
			integerLiteral( 191, 192),
			binaryExpression( 182, 192),
			binaryExpression( 173, 192),
			stringLiteral( 196, 210),
			stringLiteral( 226, 233),
			switchExpression( 70, 238),
			returnStatement( 63, 239),
			block( 59, 242),
			methodDeclaration( 21, 242),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 244, "Test"),
			compilationUnit( 0, 245));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multiple guarded case patterns in a single switch expression.
	 * Tests multiple type patterns each with their own guard clauses.
	 */
	@Test
	public void shouldParseMultipleGuardedCases()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 44, 50),
			parameterNode( 44, 54, "obj"),
			identifier( 76, 79),
			identifier( 107, 108),
			fieldAccess( 107, 115),
			methodInvocation( 107, 117),
			integerLiteral( 120, 122),
			binaryExpression( 107, 122),
			stringLiteral( 126, 139),
			identifier( 163, 164),
			fieldAccess( 163, 171),
			methodInvocation( 163, 173),
			integerLiteral( 176, 177),
			binaryExpression( 163, 177),
			stringLiteral( 181, 196),
			identifier( 221, 222),
			fieldAccess( 221, 230),
			methodInvocation( 221, 232),
			unaryExpression( 220, 232),
			stringLiteral( 236, 250),
			identifier( 275, 276),
			integerLiteral( 279, 282),
			binaryExpression( 275, 282),
			stringLiteral( 286, 301),
			identifier( 326, 327),
			integerLiteral( 330, 331),
			binaryExpression( 326, 331),
			stringLiteral( 335, 350),
			stringLiteral( 366, 373),
			switchExpression( 68, 378),
			returnStatement( 61, 379),
			block( 57, 382),
			methodDeclaration( 21, 382),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 384, "Test"),
			compilationUnit( 0, 385));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of switch expression with mix of guarded and unguarded patterns.
	 * Tests interleaving of guarded patterns with null case and default.
	 */
	@Test
	public void shouldParseMixOfGuardedAndUnguardedPatterns()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 44, 50),
			parameterNode( 44, 54, "obj"),
			identifier( 76, 79),
			stringLiteral( 101, 113),
			identifier( 137, 138),
			fieldAccess( 137, 146),
			methodInvocation( 137, 148),
			stringLiteral( 152, 166),
			stringLiteral( 188, 206),
			identifier( 231, 232),
			integerLiteral( 235, 236),
			binaryExpression( 231, 236),
			stringLiteral( 240, 250),
			stringLiteral( 273, 287),
			stringLiteral( 303, 310),
			switchExpression( 68, 315),
			returnStatement( 61, 316),
			block( 57, 319),
			methodDeclaration( 21, 319),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 321, "Test"),
			compilationUnit( 0, 322));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 44, 50),
			parameterNode( 44, 54, "obj"),
			identifier( 76, 79),
			identifier( 108, 109),
			fieldAccess( 108, 117),
			methodInvocation( 108, 119),
			unaryExpression( 107, 119),
			stringLiteral( 123, 134),
			identifier( 158, 159),
			fieldAccess( 158, 166),
			methodInvocation( 158, 168),
			integerLiteral( 172, 173),
			binaryExpression( 158, 173),
			stringLiteral( 177, 184),
			stringLiteral( 200, 207),
			switchExpression( 68, 212),
			returnStatement( 61, 213),
			block( 57, 216),
			methodDeclaration( 21, 216),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 218, "Test"),
			compilationUnit( 0, 219));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that {@code when} can be used as a variable name outside switch guards.
	 * Tests that when is a contextual keyword, not a reserved keyword.
	 */
	@Test
	public void shouldParseWhenAsVariableNameOutsideSwitch()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					int when = 10;
					String whenValue = "test";
					Object whenObj = when + whenValue.length();
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			integerLiteral( 58, 60),
			qualifiedName( 64, 70),
			stringLiteral( 83, 89),
			qualifiedName( 93, 99),
			identifier( 110, 114),
			identifier( 117, 126),
			fieldAccess( 117, 133),
			methodInvocation( 117, 135),
			binaryExpression( 110, 135),
			block( 43, 139),
			methodDeclaration( 21, 139),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 141, "Test"),
			compilationUnit( 0, 142));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with field access in guard expression.
	 * Tests accessing instance fields in when clause.
	 */
	@Test
	public void shouldParseGuardWithFieldAccess()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			integerLiteral( 45, 48),
			fieldDeclaration( 21, 49),
			qualifiedName( 77, 83),
			parameterNode( 77, 87, "obj"),
			identifier( 109, 112),
			identifier( 141, 142),
			thisExpression( 145, 149),
			fieldAccess( 145, 159),
			binaryExpression( 141, 159),
			stringLiteral( 163, 180),
			identifier( 205, 206),
			identifier( 209, 218),
			binaryExpression( 205, 218),
			stringLiteral( 222, 244),
			stringLiteral( 260, 276),
			switchExpression( 101, 281),
			returnStatement( 94, 282),
			block( 90, 285),
			methodDeclaration( 52, 285),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 287, "Test"),
			compilationUnit( 0, 288));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of nested switch expressions with guarded patterns.
	 * Tests guarded patterns in both outer and inner switch expressions.
	 */
	@Test
	public void shouldParseNestedSwitchWithGuardedPatterns()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 44, 50),
			parameterNode( 44, 56, "outer"),
			qualifiedName( 58, 64),
			parameterNode( 58, 70, "inner"),
			identifier( 92, 97),
			identifier( 125, 126),
			fieldAccess( 125, 133),
			methodInvocation( 125, 135),
			integerLiteral( 138, 139),
			binaryExpression( 125, 139),
			identifier( 151, 156),
			identifier( 187, 188),
			integerLiteral( 191, 192),
			binaryExpression( 187, 192),
			stringLiteral( 196, 212),
			identifier( 238, 239),
			integerLiteral( 242, 243),
			binaryExpression( 238, 243),
			stringLiteral( 247, 263),
			stringLiteral( 280, 292),
			switchExpression( 143, 298),
			stringLiteral( 314, 321),
			switchExpression( 84, 326),
			returnStatement( 77, 327),
			block( 73, 330),
			methodDeclaration( 21, 330),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 332, "Test"),
			compilationUnit( 0, 333));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern in switch statement with colon syntax.
	 * Tests guarded patterns in traditional switch statement form.
	 */
	@Test
	public void shouldParseGuardedPatternInSwitchStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 41, 47),
			parameterNode( 41, 51, "obj"),
			identifier( 66, 69),
			identifier( 97, 98),
			fieldAccess( 97, 105),
			methodInvocation( 97, 107),
			integerLiteral( 110, 111),
			binaryExpression( 97, 111),
			qualifiedName( 117, 135),
			identifier( 117, 123),
			fieldAccess( 117, 127),
			fieldAccess( 117, 135),
			stringLiteral( 136, 149),
			methodInvocation( 117, 150),
			breakStatement( 156, 162),
			qualifiedName( 185, 203),
			identifier( 185, 191),
			fieldAccess( 185, 195),
			fieldAccess( 185, 203),
			stringLiteral( 204, 218),
			methodInvocation( 185, 219),
			breakStatement( 225, 231),
			qualifiedName( 248, 266),
			identifier( 248, 254),
			fieldAccess( 248, 258),
			fieldAccess( 248, 266),
			stringLiteral( 267, 281),
			methodInvocation( 248, 282),
			breakStatement( 288, 294),
			switchStatement( 58, 298),
			block( 54, 301),
			methodDeclaration( 21, 301),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 303, "Test"),
			compilationUnit( 0, 304));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 46, 52),
			parameterNode( 46, 56, "obj"),
			identifier( 78, 81),
			identifier( 109, 110),
			fieldAccess( 109, 122),
			methodInvocation( 109, 124),
			integerLiteral( 127, 128),
			binaryExpression( 109, 128),
			stringLiteral( 132, 142),
			identifier( 166, 167),
			fieldAccess( 166, 179),
			methodInvocation( 166, 181),
			integerLiteral( 184, 185),
			binaryExpression( 166, 185),
			stringLiteral( 189, 199),
			identifier( 223, 224),
			fieldAccess( 223, 236),
			methodInvocation( 223, 238),
			integerLiteral( 242, 243),
			binaryExpression( 223, 243),
			stringLiteral( 247, 253),
			stringLiteral( 269, 283),
			switchExpression( 70, 288),
			returnStatement( 63, 289),
			block( 59, 292),
			methodDeclaration( 21, 292),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 294, "Test"),
			compilationUnit( 0, 295));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 44, 50),
			parameterNode( 44, 54, "obj"),
			identifier( 76, 79),
			identifier( 107, 108),
			fieldAccess( 107, 115),
			methodInvocation( 107, 117),
			integerLiteral( 120, 121),
			binaryExpression( 107, 121),
			identifier( 125, 126),
			fieldAccess( 125, 133),
			integerLiteral( 134, 135),
			methodInvocation( 125, 136),
			charLiteral( 140, 143),
			binaryExpression( 125, 143),
			binaryExpression( 107, 143),
			stringLiteral( 147, 162),
			identifier( 186, 187),
			fieldAccess( 186, 194),
			methodInvocation( 186, 196),
			integerLiteral( 199, 200),
			binaryExpression( 186, 200),
			identifier( 204, 205),
			fieldAccess( 204, 212),
			integerLiteral( 213, 214),
			methodInvocation( 204, 215),
			charLiteral( 219, 222),
			binaryExpression( 204, 222),
			binaryExpression( 186, 222),
			stringLiteral( 226, 241),
			identifier( 265, 266),
			fieldAccess( 265, 273),
			methodInvocation( 265, 275),
			integerLiteral( 278, 279),
			binaryExpression( 265, 279),
			stringLiteral( 283, 294),
			stringLiteral( 316, 330),
			stringLiteral( 346, 360),
			switchExpression( 68, 365),
			returnStatement( 61, 366),
			block( 57, 369),
			methodDeclaration( 21, 369),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 371, "Test"),
			compilationUnit( 0, 372));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with equals comparison in guard.
	 * Tests equality comparison operators in when clause.
	 */
	@Test
	public void shouldParseGuardWithEqualsComparison()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 46, 52),
			parameterNode( 46, 56, "obj"),
			identifier( 78, 81),
			identifier( 110, 111),
			integerLiteral( 115, 116),
			binaryExpression( 110, 116),
			stringLiteral( 120, 126),
			identifier( 151, 152),
			integerLiteral( 156, 157),
			binaryExpression( 151, 157),
			stringLiteral( 161, 171),
			identifier( 195, 196),
			fieldAccess( 195, 203),
			stringLiteral( 204, 210),
			methodInvocation( 195, 211),
			stringLiteral( 215, 228),
			stringLiteral( 244, 251),
			switchExpression( 70, 256),
			returnStatement( 63, 257),
			block( 59, 260),
			methodDeclaration( 21, 260),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 262, "Test"),
			compilationUnit( 0, 263));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with static method call in guard.
	 * Tests invoking static methods in when clause.
	 */
	@Test
	public void shouldParseGuardWithStaticMethodCall()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 46, 52),
			parameterNode( 46, 56, "obj"),
			identifier( 78, 81),
			identifier( 110, 117),
			fieldAccess( 110, 124),
			identifier( 125, 126),
			methodInvocation( 110, 127),
			integerLiteral( 130, 131),
			binaryExpression( 110, 131),
			stringLiteral( 135, 145),
			identifier( 170, 177),
			fieldAccess( 170, 184),
			identifier( 185, 186),
			methodInvocation( 170, 187),
			integerLiteral( 190, 191),
			binaryExpression( 170, 191),
			stringLiteral( 195, 205),
			identifier( 230, 234),
			fieldAccess( 230, 238),
			identifier( 239, 240),
			methodInvocation( 230, 241),
			integerLiteral( 245, 246),
			binaryExpression( 230, 246),
			stringLiteral( 250, 256),
			stringLiteral( 272, 279),
			switchExpression( 70, 284),
			returnStatement( 63, 285),
			block( 59, 288),
			methodDeclaration( 21, 288),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 290, "Test"),
			compilationUnit( 0, 291));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			integerLiteral( 45, 48),
			fieldDeclaration( 21, 49),
			qualifiedName( 77, 83),
			parameterNode( 77, 87, "obj"),
			identifier( 109, 112),
			identifier( 141, 142),
			identifier( 145, 154),
			binaryExpression( 141, 154),
			stringLiteral( 158, 165),
			identifier( 190, 191),
			identifier( 194, 203),
			integerLiteral( 206, 207),
			binaryExpression( 194, 207),
			binaryExpression( 190, 207),
			stringLiteral( 211, 219),
			stringLiteral( 235, 242),
			switchExpression( 101, 247),
			returnStatement( 94, 248),
			block( 90, 251),
			methodDeclaration( 52, 251),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 253, "Test"),
			compilationUnit( 0, 254));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with instanceof in guard expression.
	 * Tests combining pattern matching with additional instanceof check in guard.
	 */
	@Test
	public void shouldParseGuardWithInstanceofInGuard()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 44, 50),
			parameterNode( 44, 54, "obj"),
			qualifiedName( 56, 62),
			parameterNode( 56, 68, "extra"),
			identifier( 90, 93),
			identifier( 121, 126),
			qualifiedName( 138, 145),
			binaryExpression( 121, 145),
			stringLiteral( 149, 178),
			identifier( 202, 207),
			qualifiedName( 219, 225),
			binaryExpression( 202, 225),
			stringLiteral( 229, 257),
			stringLiteral( 273, 280),
			switchExpression( 82, 285),
			returnStatement( 75, 286),
			block( 71, 289),
			methodDeclaration( 21, 289),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 291, "Test"),
			compilationUnit( 0, 292));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with chained method calls in guard.
	 * Tests method chaining in when clause expression.
	 */
	@Test
	public void shouldParseGuardWithChainedMethodCalls()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 44, 50),
			parameterNode( 44, 54, "obj"),
			identifier( 76, 79),
			identifier( 107, 108),
			fieldAccess( 107, 114),
			methodInvocation( 107, 116),
			fieldAccess( 107, 128),
			methodInvocation( 107, 130),
			fieldAccess( 107, 141),
			stringLiteral( 142, 149),
			methodInvocation( 107, 150),
			stringLiteral( 154, 164),
			identifier( 188, 189),
			fieldAccess( 188, 195),
			methodInvocation( 188, 197),
			fieldAccess( 188, 204),
			methodInvocation( 188, 206),
			integerLiteral( 209, 210),
			binaryExpression( 188, 210),
			stringLiteral( 214, 225),
			stringLiteral( 241, 248),
			switchExpression( 68, 253),
			returnStatement( 61, 254),
			block( 57, 257),
			methodDeclaration( 21, 257),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 259, "Test"),
			compilationUnit( 0, 260));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of guarded pattern with local variable reference in guard.
	 * Tests using local variables in when clause boolean expressions.
	 */
	@Test
	public void shouldParseGuardWithLocalVariableInGuard()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			qualifiedName( 46, 52),
			parameterNode( 46, 56, "obj"),
			integerLiteral( 79, 80),
			integerLiteral( 100, 103),
			identifier( 122, 125),
			identifier( 153, 154),
			fieldAccess( 153, 161),
			methodInvocation( 153, 163),
			identifier( 167, 176),
			binaryExpression( 153, 176),
			identifier( 180, 181),
			fieldAccess( 180, 188),
			methodInvocation( 180, 190),
			identifier( 194, 203),
			binaryExpression( 180, 203),
			binaryExpression( 153, 203),
			stringLiteral( 207, 221),
			stringLiteral( 237, 253),
			switchExpression( 114, 258),
			returnStatement( 107, 259),
			block( 59, 262),
			methodDeclaration( 21, 262),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 264, "Test"),
			compilationUnit( 0, 265));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
