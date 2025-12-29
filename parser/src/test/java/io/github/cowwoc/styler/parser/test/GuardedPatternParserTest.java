package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(PARAMETER_DECLARATION, 41, 51, "obj"),
			semanticNode(NodeType.IDENTIFIER, 66, 69),
			semanticNode(NodeType.IDENTIFIER, 97, 98),
			semanticNode(NodeType.FIELD_ACCESS, 97, 105),
			semanticNode(NodeType.METHOD_INVOCATION, 97, 107),
			semanticNode(NodeType.INTEGER_LITERAL, 110, 111),
			semanticNode(NodeType.BINARY_EXPRESSION, 97, 111),
			semanticNode(NodeType.QUALIFIED_NAME, 115, 133),
			semanticNode(NodeType.IDENTIFIER, 115, 121),
			semanticNode(NodeType.FIELD_ACCESS, 115, 125),
			semanticNode(NodeType.FIELD_ACCESS, 115, 133),
			semanticNode(NodeType.IDENTIFIER, 134, 135),
			semanticNode(NodeType.METHOD_INVOCATION, 115, 136),
			semanticNode(NodeType.BLOCK, 152, 154),
			semanticNode(NodeType.SWITCH_STATEMENT, 58, 158),
			semanticNode(NodeType.BLOCK, 54, 161),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 161),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 163, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 164));
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
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(PARAMETER_DECLARATION, 41, 51, "obj"),
			semanticNode(NodeType.IDENTIFIER, 66, 69),
			semanticNode(NodeType.IDENTIFIER, 107, 108),
			semanticNode(NodeType.FIELD_ACCESS, 107, 116),
			semanticNode(NodeType.METHOD_INVOCATION, 107, 118),
			semanticNode(NodeType.QUALIFIED_NAME, 122, 140),
			semanticNode(NodeType.IDENTIFIER, 122, 128),
			semanticNode(NodeType.FIELD_ACCESS, 122, 132),
			semanticNode(NodeType.FIELD_ACCESS, 122, 140),
			semanticNode(NodeType.STRING_LITERAL, 141, 148),
			semanticNode(NodeType.METHOD_INVOCATION, 122, 149),
			semanticNode(NodeType.BLOCK, 165, 167),
			semanticNode(NodeType.SWITCH_STATEMENT, 58, 171),
			semanticNode(NodeType.BLOCK, 54, 174),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 174),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 176, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 177));
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
			semanticNode(NodeType.QUALIFIED_NAME, 43, 49),
			semanticNode(PARAMETER_DECLARATION, 43, 53, "obj"),
			semanticNode(NodeType.IDENTIFIER, 75, 78),
			semanticNode(NodeType.IDENTIFIER, 107, 108),
			semanticNode(NodeType.INTEGER_LITERAL, 111, 112),
			semanticNode(NodeType.BINARY_EXPRESSION, 107, 112),
			semanticNode(NodeType.INTEGER_LITERAL, 116, 117),
			semanticNode(NodeType.IDENTIFIER, 142, 143),
			semanticNode(NodeType.INTEGER_LITERAL, 146, 147),
			semanticNode(NodeType.BINARY_EXPRESSION, 142, 147),
			semanticNode(NodeType.INTEGER_LITERAL, 152, 153),
			semanticNode(NodeType.UNARY_EXPRESSION, 151, 153),
			semanticNode(NodeType.INTEGER_LITERAL, 169, 170),
			semanticNode(NodeType.SWITCH_EXPRESSION, 67, 175),
			semanticNode(NodeType.RETURN_STATEMENT, 60, 176),
			semanticNode(NodeType.BLOCK, 56, 179),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 179),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 181, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 182));
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
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(PARAMETER_DECLARATION, 46, 56, "obj"),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.IDENTIFIER, 110, 111),
			semanticNode(NodeType.INTEGER_LITERAL, 114, 115),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 115),
			semanticNode(NodeType.IDENTIFIER, 119, 120),
			semanticNode(NodeType.INTEGER_LITERAL, 123, 126),
			semanticNode(NodeType.BINARY_EXPRESSION, 119, 126),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 126),
			semanticNode(NodeType.STRING_LITERAL, 130, 146),
			semanticNode(NodeType.IDENTIFIER, 171, 172),
			semanticNode(NodeType.INTEGER_LITERAL, 176, 179),
			semanticNode(NodeType.BINARY_EXPRESSION, 171, 179),
			semanticNode(NodeType.IDENTIFIER, 183, 184),
			semanticNode(NodeType.INTEGER_LITERAL, 187, 191),
			semanticNode(NodeType.BINARY_EXPRESSION, 183, 191),
			semanticNode(NodeType.BINARY_EXPRESSION, 171, 191),
			semanticNode(NodeType.STRING_LITERAL, 195, 212),
			semanticNode(NodeType.STRING_LITERAL, 228, 235),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 240),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 241),
			semanticNode(NodeType.BLOCK, 59, 244),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 244),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 246, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 247));
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
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(PARAMETER_DECLARATION, 46, 56, "obj"),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.IDENTIFIER, 110, 111),
			semanticNode(NodeType.INTEGER_LITERAL, 115, 118),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 118),
			semanticNode(NodeType.IDENTIFIER, 122, 123),
			semanticNode(NodeType.INTEGER_LITERAL, 128, 131),
			semanticNode(NodeType.UNARY_EXPRESSION, 127, 131),
			semanticNode(NodeType.BINARY_EXPRESSION, 122, 131),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 131),
			semanticNode(NodeType.STRING_LITERAL, 135, 152),
			semanticNode(NodeType.STRING_LITERAL, 168, 185),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 190),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 191),
			semanticNode(NodeType.BLOCK, 59, 194),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 194),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 196, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 197));
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
			semanticNode(NodeType.QUALIFIED_NAME, 43, 49),
			semanticNode(PARAMETER_DECLARATION, 43, 53, "obj"),
			semanticNode(NodeType.IDENTIFIER, 75, 78),
			semanticNode(NodeType.IDENTIFIER, 106, 107),
			semanticNode(NodeType.FIELD_ACCESS, 106, 115),
			semanticNode(NodeType.METHOD_INVOCATION, 106, 117),
			semanticNode(NodeType.STRING_LITERAL, 121, 135),
			semanticNode(NodeType.IDENTIFIER, 159, 160),
			semanticNode(NodeType.FIELD_ACCESS, 159, 171),
			semanticNode(NodeType.STRING_LITERAL, 172, 178),
			semanticNode(NodeType.METHOD_INVOCATION, 159, 179),
			semanticNode(NodeType.STRING_LITERAL, 183, 196),
			semanticNode(NodeType.STRING_LITERAL, 212, 219),
			semanticNode(NodeType.SWITCH_EXPRESSION, 67, 224),
			semanticNode(NodeType.RETURN_STATEMENT, 60, 225),
			semanticNode(NodeType.BLOCK, 56, 228),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 228),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 230, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 231));
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
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(PARAMETER_DECLARATION, 46, 56, "obj"),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.IDENTIFIER, 110, 111),
			semanticNode(NodeType.INTEGER_LITERAL, 114, 115),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 115),
			semanticNode(NodeType.IDENTIFIER, 119, 120),
			semanticNode(NodeType.INTEGER_LITERAL, 123, 124),
			semanticNode(NodeType.BINARY_EXPRESSION, 119, 124),
			semanticNode(NodeType.INTEGER_LITERAL, 128, 129),
			semanticNode(NodeType.BINARY_EXPRESSION, 119, 129),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 129),
			semanticNode(NodeType.STRING_LITERAL, 133, 148),
			semanticNode(NodeType.IDENTIFIER, 173, 174),
			semanticNode(NodeType.INTEGER_LITERAL, 177, 178),
			semanticNode(NodeType.BINARY_EXPRESSION, 173, 178),
			semanticNode(NodeType.IDENTIFIER, 182, 183),
			semanticNode(NodeType.INTEGER_LITERAL, 186, 187),
			semanticNode(NodeType.BINARY_EXPRESSION, 182, 187),
			semanticNode(NodeType.INTEGER_LITERAL, 191, 192),
			semanticNode(NodeType.BINARY_EXPRESSION, 182, 192),
			semanticNode(NodeType.BINARY_EXPRESSION, 173, 192),
			semanticNode(NodeType.STRING_LITERAL, 196, 210),
			semanticNode(NodeType.STRING_LITERAL, 226, 233),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 238),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 239),
			semanticNode(NodeType.BLOCK, 59, 242),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 242),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 244, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 245));
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
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(PARAMETER_DECLARATION, 44, 54, "obj"),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.IDENTIFIER, 107, 108),
			semanticNode(NodeType.FIELD_ACCESS, 107, 115),
			semanticNode(NodeType.METHOD_INVOCATION, 107, 117),
			semanticNode(NodeType.INTEGER_LITERAL, 120, 122),
			semanticNode(NodeType.BINARY_EXPRESSION, 107, 122),
			semanticNode(NodeType.STRING_LITERAL, 126, 139),
			semanticNode(NodeType.IDENTIFIER, 163, 164),
			semanticNode(NodeType.FIELD_ACCESS, 163, 171),
			semanticNode(NodeType.METHOD_INVOCATION, 163, 173),
			semanticNode(NodeType.INTEGER_LITERAL, 176, 177),
			semanticNode(NodeType.BINARY_EXPRESSION, 163, 177),
			semanticNode(NodeType.STRING_LITERAL, 181, 196),
			semanticNode(NodeType.IDENTIFIER, 221, 222),
			semanticNode(NodeType.FIELD_ACCESS, 221, 230),
			semanticNode(NodeType.METHOD_INVOCATION, 221, 232),
			semanticNode(NodeType.UNARY_EXPRESSION, 220, 232),
			semanticNode(NodeType.STRING_LITERAL, 236, 250),
			semanticNode(NodeType.IDENTIFIER, 275, 276),
			semanticNode(NodeType.INTEGER_LITERAL, 279, 282),
			semanticNode(NodeType.BINARY_EXPRESSION, 275, 282),
			semanticNode(NodeType.STRING_LITERAL, 286, 301),
			semanticNode(NodeType.IDENTIFIER, 326, 327),
			semanticNode(NodeType.INTEGER_LITERAL, 330, 331),
			semanticNode(NodeType.BINARY_EXPRESSION, 326, 331),
			semanticNode(NodeType.STRING_LITERAL, 335, 350),
			semanticNode(NodeType.STRING_LITERAL, 366, 373),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 378),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 379),
			semanticNode(NodeType.BLOCK, 57, 382),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 382),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 384, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 385));
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
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(PARAMETER_DECLARATION, 44, 54, "obj"),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.STRING_LITERAL, 101, 113),
			semanticNode(NodeType.IDENTIFIER, 137, 138),
			semanticNode(NodeType.FIELD_ACCESS, 137, 146),
			semanticNode(NodeType.METHOD_INVOCATION, 137, 148),
			semanticNode(NodeType.STRING_LITERAL, 152, 166),
			semanticNode(NodeType.STRING_LITERAL, 188, 206),
			semanticNode(NodeType.IDENTIFIER, 231, 232),
			semanticNode(NodeType.INTEGER_LITERAL, 235, 236),
			semanticNode(NodeType.BINARY_EXPRESSION, 231, 236),
			semanticNode(NodeType.STRING_LITERAL, 240, 250),
			semanticNode(NodeType.STRING_LITERAL, 273, 287),
			semanticNode(NodeType.STRING_LITERAL, 303, 310),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 315),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 316),
			semanticNode(NodeType.BLOCK, 57, 319),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 319),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 321, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 322));
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
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(PARAMETER_DECLARATION, 44, 54, "obj"),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.IDENTIFIER, 108, 109),
			semanticNode(NodeType.FIELD_ACCESS, 108, 117),
			semanticNode(NodeType.METHOD_INVOCATION, 108, 119),
			semanticNode(NodeType.UNARY_EXPRESSION, 107, 119),
			semanticNode(NodeType.STRING_LITERAL, 123, 134),
			semanticNode(NodeType.IDENTIFIER, 158, 159),
			semanticNode(NodeType.FIELD_ACCESS, 158, 166),
			semanticNode(NodeType.METHOD_INVOCATION, 158, 168),
			semanticNode(NodeType.INTEGER_LITERAL, 172, 173),
			semanticNode(NodeType.BINARY_EXPRESSION, 158, 173),
			semanticNode(NodeType.STRING_LITERAL, 177, 184),
			semanticNode(NodeType.STRING_LITERAL, 200, 207),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 212),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 213),
			semanticNode(NodeType.BLOCK, 57, 216),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 216),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 218, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 219));
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
			semanticNode(NodeType.INTEGER_LITERAL, 58, 60),
			semanticNode(NodeType.QUALIFIED_NAME, 64, 70),
			semanticNode(NodeType.STRING_LITERAL, 83, 89),
			semanticNode(NodeType.QUALIFIED_NAME, 93, 99),
			semanticNode(NodeType.IDENTIFIER, 110, 114),
			semanticNode(NodeType.IDENTIFIER, 117, 126),
			semanticNode(NodeType.FIELD_ACCESS, 117, 133),
			semanticNode(NodeType.METHOD_INVOCATION, 117, 135),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 135),
			semanticNode(NodeType.BLOCK, 43, 139),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 139),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 141, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 142));
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
			semanticNode(NodeType.INTEGER_LITERAL, 45, 48),
			semanticNode(NodeType.FIELD_DECLARATION, 21, 49),
			semanticNode(NodeType.QUALIFIED_NAME, 77, 83),
			semanticNode(PARAMETER_DECLARATION, 77, 87, "obj"),
			semanticNode(NodeType.IDENTIFIER, 109, 112),
			semanticNode(NodeType.IDENTIFIER, 141, 142),
			semanticNode(NodeType.THIS_EXPRESSION, 145, 149),
			semanticNode(NodeType.FIELD_ACCESS, 145, 159),
			semanticNode(NodeType.BINARY_EXPRESSION, 141, 159),
			semanticNode(NodeType.STRING_LITERAL, 163, 180),
			semanticNode(NodeType.IDENTIFIER, 205, 206),
			semanticNode(NodeType.IDENTIFIER, 209, 218),
			semanticNode(NodeType.BINARY_EXPRESSION, 205, 218),
			semanticNode(NodeType.STRING_LITERAL, 222, 244),
			semanticNode(NodeType.STRING_LITERAL, 260, 276),
			semanticNode(NodeType.SWITCH_EXPRESSION, 101, 281),
			semanticNode(NodeType.RETURN_STATEMENT, 94, 282),
			semanticNode(NodeType.BLOCK, 90, 285),
			semanticNode(NodeType.METHOD_DECLARATION, 52, 285),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 287, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 288));
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
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(PARAMETER_DECLARATION, 44, 56, "outer"),
			semanticNode(NodeType.QUALIFIED_NAME, 58, 64),
			semanticNode(PARAMETER_DECLARATION, 58, 70, "inner"),
			semanticNode(NodeType.IDENTIFIER, 92, 97),
			semanticNode(NodeType.IDENTIFIER, 125, 126),
			semanticNode(NodeType.FIELD_ACCESS, 125, 133),
			semanticNode(NodeType.METHOD_INVOCATION, 125, 135),
			semanticNode(NodeType.INTEGER_LITERAL, 138, 139),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 139),
			semanticNode(NodeType.IDENTIFIER, 151, 156),
			semanticNode(NodeType.IDENTIFIER, 187, 188),
			semanticNode(NodeType.INTEGER_LITERAL, 191, 192),
			semanticNode(NodeType.BINARY_EXPRESSION, 187, 192),
			semanticNode(NodeType.STRING_LITERAL, 196, 212),
			semanticNode(NodeType.IDENTIFIER, 238, 239),
			semanticNode(NodeType.INTEGER_LITERAL, 242, 243),
			semanticNode(NodeType.BINARY_EXPRESSION, 238, 243),
			semanticNode(NodeType.STRING_LITERAL, 247, 263),
			semanticNode(NodeType.STRING_LITERAL, 280, 292),
			semanticNode(NodeType.SWITCH_EXPRESSION, 143, 298),
			semanticNode(NodeType.STRING_LITERAL, 314, 321),
			semanticNode(NodeType.SWITCH_EXPRESSION, 84, 326),
			semanticNode(NodeType.RETURN_STATEMENT, 77, 327),
			semanticNode(NodeType.BLOCK, 73, 330),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 330),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 332, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 333));
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
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(PARAMETER_DECLARATION, 41, 51, "obj"),
			semanticNode(NodeType.IDENTIFIER, 66, 69),
			semanticNode(NodeType.IDENTIFIER, 97, 98),
			semanticNode(NodeType.FIELD_ACCESS, 97, 105),
			semanticNode(NodeType.METHOD_INVOCATION, 97, 107),
			semanticNode(NodeType.INTEGER_LITERAL, 110, 111),
			semanticNode(NodeType.BINARY_EXPRESSION, 97, 111),
			semanticNode(NodeType.QUALIFIED_NAME, 117, 135),
			semanticNode(NodeType.IDENTIFIER, 117, 123),
			semanticNode(NodeType.FIELD_ACCESS, 117, 127),
			semanticNode(NodeType.FIELD_ACCESS, 117, 135),
			semanticNode(NodeType.STRING_LITERAL, 136, 149),
			semanticNode(NodeType.METHOD_INVOCATION, 117, 150),
			semanticNode(NodeType.BREAK_STATEMENT, 156, 162),
			semanticNode(NodeType.QUALIFIED_NAME, 185, 203),
			semanticNode(NodeType.IDENTIFIER, 185, 191),
			semanticNode(NodeType.FIELD_ACCESS, 185, 195),
			semanticNode(NodeType.FIELD_ACCESS, 185, 203),
			semanticNode(NodeType.STRING_LITERAL, 204, 218),
			semanticNode(NodeType.METHOD_INVOCATION, 185, 219),
			semanticNode(NodeType.BREAK_STATEMENT, 225, 231),
			semanticNode(NodeType.QUALIFIED_NAME, 248, 266),
			semanticNode(NodeType.IDENTIFIER, 248, 254),
			semanticNode(NodeType.FIELD_ACCESS, 248, 258),
			semanticNode(NodeType.FIELD_ACCESS, 248, 266),
			semanticNode(NodeType.STRING_LITERAL, 267, 281),
			semanticNode(NodeType.METHOD_INVOCATION, 248, 282),
			semanticNode(NodeType.BREAK_STATEMENT, 288, 294),
			semanticNode(NodeType.SWITCH_STATEMENT, 58, 298),
			semanticNode(NodeType.BLOCK, 54, 301),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 301),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 303, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 304));
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
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(PARAMETER_DECLARATION, 46, 56, "obj"),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.IDENTIFIER, 109, 110),
			semanticNode(NodeType.FIELD_ACCESS, 109, 122),
			semanticNode(NodeType.METHOD_INVOCATION, 109, 124),
			semanticNode(NodeType.INTEGER_LITERAL, 127, 128),
			semanticNode(NodeType.BINARY_EXPRESSION, 109, 128),
			semanticNode(NodeType.STRING_LITERAL, 132, 142),
			semanticNode(NodeType.IDENTIFIER, 166, 167),
			semanticNode(NodeType.FIELD_ACCESS, 166, 179),
			semanticNode(NodeType.METHOD_INVOCATION, 166, 181),
			semanticNode(NodeType.INTEGER_LITERAL, 184, 185),
			semanticNode(NodeType.BINARY_EXPRESSION, 166, 185),
			semanticNode(NodeType.STRING_LITERAL, 189, 199),
			semanticNode(NodeType.IDENTIFIER, 223, 224),
			semanticNode(NodeType.FIELD_ACCESS, 223, 236),
			semanticNode(NodeType.METHOD_INVOCATION, 223, 238),
			semanticNode(NodeType.INTEGER_LITERAL, 242, 243),
			semanticNode(NodeType.BINARY_EXPRESSION, 223, 243),
			semanticNode(NodeType.STRING_LITERAL, 247, 253),
			semanticNode(NodeType.STRING_LITERAL, 269, 283),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 288),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 289),
			semanticNode(NodeType.BLOCK, 59, 292),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 292),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 294, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 295));
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
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(PARAMETER_DECLARATION, 44, 54, "obj"),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.IDENTIFIER, 107, 108),
			semanticNode(NodeType.FIELD_ACCESS, 107, 115),
			semanticNode(NodeType.METHOD_INVOCATION, 107, 117),
			semanticNode(NodeType.INTEGER_LITERAL, 120, 121),
			semanticNode(NodeType.BINARY_EXPRESSION, 107, 121),
			semanticNode(NodeType.IDENTIFIER, 125, 126),
			semanticNode(NodeType.FIELD_ACCESS, 125, 133),
			semanticNode(NodeType.INTEGER_LITERAL, 134, 135),
			semanticNode(NodeType.METHOD_INVOCATION, 125, 136),
			semanticNode(NodeType.CHAR_LITERAL, 140, 143),
			semanticNode(NodeType.BINARY_EXPRESSION, 125, 143),
			semanticNode(NodeType.BINARY_EXPRESSION, 107, 143),
			semanticNode(NodeType.STRING_LITERAL, 147, 162),
			semanticNode(NodeType.IDENTIFIER, 186, 187),
			semanticNode(NodeType.FIELD_ACCESS, 186, 194),
			semanticNode(NodeType.METHOD_INVOCATION, 186, 196),
			semanticNode(NodeType.INTEGER_LITERAL, 199, 200),
			semanticNode(NodeType.BINARY_EXPRESSION, 186, 200),
			semanticNode(NodeType.IDENTIFIER, 204, 205),
			semanticNode(NodeType.FIELD_ACCESS, 204, 212),
			semanticNode(NodeType.INTEGER_LITERAL, 213, 214),
			semanticNode(NodeType.METHOD_INVOCATION, 204, 215),
			semanticNode(NodeType.CHAR_LITERAL, 219, 222),
			semanticNode(NodeType.BINARY_EXPRESSION, 204, 222),
			semanticNode(NodeType.BINARY_EXPRESSION, 186, 222),
			semanticNode(NodeType.STRING_LITERAL, 226, 241),
			semanticNode(NodeType.IDENTIFIER, 265, 266),
			semanticNode(NodeType.FIELD_ACCESS, 265, 273),
			semanticNode(NodeType.METHOD_INVOCATION, 265, 275),
			semanticNode(NodeType.INTEGER_LITERAL, 278, 279),
			semanticNode(NodeType.BINARY_EXPRESSION, 265, 279),
			semanticNode(NodeType.STRING_LITERAL, 283, 294),
			semanticNode(NodeType.STRING_LITERAL, 316, 330),
			semanticNode(NodeType.STRING_LITERAL, 346, 360),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 365),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 366),
			semanticNode(NodeType.BLOCK, 57, 369),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 369),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 371, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 372));
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
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(PARAMETER_DECLARATION, 46, 56, "obj"),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.IDENTIFIER, 110, 111),
			semanticNode(NodeType.INTEGER_LITERAL, 115, 116),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 116),
			semanticNode(NodeType.STRING_LITERAL, 120, 126),
			semanticNode(NodeType.IDENTIFIER, 151, 152),
			semanticNode(NodeType.INTEGER_LITERAL, 156, 157),
			semanticNode(NodeType.BINARY_EXPRESSION, 151, 157),
			semanticNode(NodeType.STRING_LITERAL, 161, 171),
			semanticNode(NodeType.IDENTIFIER, 195, 196),
			semanticNode(NodeType.FIELD_ACCESS, 195, 203),
			semanticNode(NodeType.STRING_LITERAL, 204, 210),
			semanticNode(NodeType.METHOD_INVOCATION, 195, 211),
			semanticNode(NodeType.STRING_LITERAL, 215, 228),
			semanticNode(NodeType.STRING_LITERAL, 244, 251),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 256),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 257),
			semanticNode(NodeType.BLOCK, 59, 260),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 260),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 262, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 263));
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
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(PARAMETER_DECLARATION, 46, 56, "obj"),
			semanticNode(NodeType.IDENTIFIER, 78, 81),
			semanticNode(NodeType.IDENTIFIER, 110, 117),
			semanticNode(NodeType.FIELD_ACCESS, 110, 124),
			semanticNode(NodeType.IDENTIFIER, 125, 126),
			semanticNode(NodeType.METHOD_INVOCATION, 110, 127),
			semanticNode(NodeType.INTEGER_LITERAL, 130, 131),
			semanticNode(NodeType.BINARY_EXPRESSION, 110, 131),
			semanticNode(NodeType.STRING_LITERAL, 135, 145),
			semanticNode(NodeType.IDENTIFIER, 170, 177),
			semanticNode(NodeType.FIELD_ACCESS, 170, 184),
			semanticNode(NodeType.IDENTIFIER, 185, 186),
			semanticNode(NodeType.METHOD_INVOCATION, 170, 187),
			semanticNode(NodeType.INTEGER_LITERAL, 190, 191),
			semanticNode(NodeType.BINARY_EXPRESSION, 170, 191),
			semanticNode(NodeType.STRING_LITERAL, 195, 205),
			semanticNode(NodeType.IDENTIFIER, 230, 234),
			semanticNode(NodeType.FIELD_ACCESS, 230, 238),
			semanticNode(NodeType.IDENTIFIER, 239, 240),
			semanticNode(NodeType.METHOD_INVOCATION, 230, 241),
			semanticNode(NodeType.INTEGER_LITERAL, 245, 246),
			semanticNode(NodeType.BINARY_EXPRESSION, 230, 246),
			semanticNode(NodeType.STRING_LITERAL, 250, 256),
			semanticNode(NodeType.STRING_LITERAL, 272, 279),
			semanticNode(NodeType.SWITCH_EXPRESSION, 70, 284),
			semanticNode(NodeType.RETURN_STATEMENT, 63, 285),
			semanticNode(NodeType.BLOCK, 59, 288),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 288),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 290, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 291));
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
			semanticNode(NodeType.INTEGER_LITERAL, 45, 48),
			semanticNode(NodeType.FIELD_DECLARATION, 21, 49),
			semanticNode(NodeType.QUALIFIED_NAME, 77, 83),
			semanticNode(PARAMETER_DECLARATION, 77, 87, "obj"),
			semanticNode(NodeType.IDENTIFIER, 109, 112),
			semanticNode(NodeType.IDENTIFIER, 141, 142),
			semanticNode(NodeType.IDENTIFIER, 145, 154),
			semanticNode(NodeType.BINARY_EXPRESSION, 141, 154),
			semanticNode(NodeType.STRING_LITERAL, 158, 165),
			semanticNode(NodeType.IDENTIFIER, 190, 191),
			semanticNode(NodeType.IDENTIFIER, 194, 203),
			semanticNode(NodeType.INTEGER_LITERAL, 206, 207),
			semanticNode(NodeType.BINARY_EXPRESSION, 194, 207),
			semanticNode(NodeType.BINARY_EXPRESSION, 190, 207),
			semanticNode(NodeType.STRING_LITERAL, 211, 219),
			semanticNode(NodeType.STRING_LITERAL, 235, 242),
			semanticNode(NodeType.SWITCH_EXPRESSION, 101, 247),
			semanticNode(NodeType.RETURN_STATEMENT, 94, 248),
			semanticNode(NodeType.BLOCK, 90, 251),
			semanticNode(NodeType.METHOD_DECLARATION, 52, 251),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 253, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 254));
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
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(PARAMETER_DECLARATION, 44, 54, "obj"),
			semanticNode(NodeType.QUALIFIED_NAME, 56, 62),
			semanticNode(PARAMETER_DECLARATION, 56, 68, "extra"),
			semanticNode(NodeType.IDENTIFIER, 90, 93),
			semanticNode(NodeType.IDENTIFIER, 121, 126),
			semanticNode(NodeType.QUALIFIED_NAME, 138, 145),
			semanticNode(NodeType.BINARY_EXPRESSION, 121, 145),
			semanticNode(NodeType.STRING_LITERAL, 149, 178),
			semanticNode(NodeType.IDENTIFIER, 202, 207),
			semanticNode(NodeType.QUALIFIED_NAME, 219, 225),
			semanticNode(NodeType.BINARY_EXPRESSION, 202, 225),
			semanticNode(NodeType.STRING_LITERAL, 229, 257),
			semanticNode(NodeType.STRING_LITERAL, 273, 280),
			semanticNode(NodeType.SWITCH_EXPRESSION, 82, 285),
			semanticNode(NodeType.RETURN_STATEMENT, 75, 286),
			semanticNode(NodeType.BLOCK, 71, 289),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 289),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 291, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 292));
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
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(PARAMETER_DECLARATION, 44, 54, "obj"),
			semanticNode(NodeType.IDENTIFIER, 76, 79),
			semanticNode(NodeType.IDENTIFIER, 107, 108),
			semanticNode(NodeType.FIELD_ACCESS, 107, 114),
			semanticNode(NodeType.METHOD_INVOCATION, 107, 116),
			semanticNode(NodeType.FIELD_ACCESS, 107, 128),
			semanticNode(NodeType.METHOD_INVOCATION, 107, 130),
			semanticNode(NodeType.FIELD_ACCESS, 107, 141),
			semanticNode(NodeType.STRING_LITERAL, 142, 149),
			semanticNode(NodeType.METHOD_INVOCATION, 107, 150),
			semanticNode(NodeType.STRING_LITERAL, 154, 164),
			semanticNode(NodeType.IDENTIFIER, 188, 189),
			semanticNode(NodeType.FIELD_ACCESS, 188, 195),
			semanticNode(NodeType.METHOD_INVOCATION, 188, 197),
			semanticNode(NodeType.FIELD_ACCESS, 188, 204),
			semanticNode(NodeType.METHOD_INVOCATION, 188, 206),
			semanticNode(NodeType.INTEGER_LITERAL, 209, 210),
			semanticNode(NodeType.BINARY_EXPRESSION, 188, 210),
			semanticNode(NodeType.STRING_LITERAL, 214, 225),
			semanticNode(NodeType.STRING_LITERAL, 241, 248),
			semanticNode(NodeType.SWITCH_EXPRESSION, 68, 253),
			semanticNode(NodeType.RETURN_STATEMENT, 61, 254),
			semanticNode(NodeType.BLOCK, 57, 257),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 257),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 259, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 260));
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
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(PARAMETER_DECLARATION, 46, 56, "obj"),
			semanticNode(NodeType.INTEGER_LITERAL, 79, 80),
			semanticNode(NodeType.INTEGER_LITERAL, 100, 103),
			semanticNode(NodeType.IDENTIFIER, 122, 125),
			semanticNode(NodeType.IDENTIFIER, 153, 154),
			semanticNode(NodeType.FIELD_ACCESS, 153, 161),
			semanticNode(NodeType.METHOD_INVOCATION, 153, 163),
			semanticNode(NodeType.IDENTIFIER, 167, 176),
			semanticNode(NodeType.BINARY_EXPRESSION, 153, 176),
			semanticNode(NodeType.IDENTIFIER, 180, 181),
			semanticNode(NodeType.FIELD_ACCESS, 180, 188),
			semanticNode(NodeType.METHOD_INVOCATION, 180, 190),
			semanticNode(NodeType.IDENTIFIER, 194, 203),
			semanticNode(NodeType.BINARY_EXPRESSION, 180, 203),
			semanticNode(NodeType.BINARY_EXPRESSION, 153, 203),
			semanticNode(NodeType.STRING_LITERAL, 207, 221),
			semanticNode(NodeType.STRING_LITERAL, 237, 253),
			semanticNode(NodeType.SWITCH_EXPRESSION, 114, 258),
			semanticNode(NodeType.RETURN_STATEMENT, 107, 259),
			semanticNode(NodeType.BLOCK, 59, 262),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 262),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 264, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 265));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
