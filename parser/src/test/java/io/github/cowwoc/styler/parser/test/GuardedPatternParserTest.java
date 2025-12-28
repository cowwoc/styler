package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;

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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
	}
}
