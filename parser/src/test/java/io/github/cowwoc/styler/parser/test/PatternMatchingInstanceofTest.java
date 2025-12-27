package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;

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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of pattern matching instanceof with generic type.
	 * Tests handling of parameterized types in instanceof patterns.
	 */
	@Test
	public void shouldParsePatternMatchingWithGenerics()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of pattern matching instanceof with nested type reference.
	 * Tests qualified type names in instanceof patterns.
	 */
	@Test
	public void shouldParsePatternMatchingWithNestedType()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of pattern matching instanceof in if condition.
	 * Tests the typical usage pattern in conditional statements.
	 */
	@Test
	public void shouldParsePatternMatchingInIfStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of pattern matching instanceof in logical AND expression.
	 * Tests pattern variable usage in compound boolean expressions.
	 */
	@Test
	public void shouldParsePatternMatchingInLogicalExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of pattern matching instanceof with array type.
	 * Tests pattern matching with array type references.
	 */
	@Test
	public void shouldParsePatternMatchingWithArrayType()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of negated pattern matching instanceof.
	 * Tests pattern matching in negated conditions.
	 */
	@Test
	public void shouldParseNegatedPatternMatching()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of pattern matching instanceof in ternary expression.
	 * Tests pattern matching as part of conditional expressions.
	 */
	@Test
	public void shouldParsePatternMatchingInTernary()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method(Object obj)
				{
					String result = obj instanceof String s ? s : "default";
					System.out.println(result);
				}
			}
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of multiple pattern matching instanceof in same method.
	 * Tests handling of multiple instanceof patterns with different variables.
	 */
	@Test
	public void shouldParseMultiplePatternMatchingInstanceof()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of traditional instanceof without pattern variable.
	 * Tests backward compatibility with pre-Java 16 instanceof syntax.
	 */
	@Test
	public void shouldParseTraditionalInstanceof()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}

	/**
	 * Validates parsing of pattern matching instanceof in while loop.
	 * Tests pattern matching in loop conditions.
	 */
	@Test
	public void shouldParsePatternMatchingInWhileLoop()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		requireThat(actual, "actual").isNotEmpty();
	}
}
