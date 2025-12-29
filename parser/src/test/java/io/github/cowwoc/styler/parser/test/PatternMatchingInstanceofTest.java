package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETERIZED_TYPE;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 61, 64),
			semanticNode(NodeType.QUALIFIED_NAME, 76, 82),
			semanticNode(NodeType.BINARY_EXPRESSION, 61, 84),
			semanticNode(NodeType.QUALIFIED_NAME, 93, 111),
			semanticNode(NodeType.IDENTIFIER, 93, 99),
			semanticNode(NodeType.FIELD_ACCESS, 93, 103),
			semanticNode(NodeType.FIELD_ACCESS, 93, 111),
			semanticNode(NodeType.IDENTIFIER, 112, 113),
			semanticNode(NodeType.METHOD_INVOCATION, 93, 114),
			semanticNode(NodeType.BLOCK, 88, 119),
			semanticNode(NodeType.IF_STATEMENT, 57, 119),
			semanticNode(NodeType.BLOCK, 53, 122),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 122),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 124, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 125),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 61, 64),
			semanticNode(PARAMETERIZED_TYPE, 76, 98),
			semanticNode(NodeType.QUALIFIED_NAME, 76, 90),
			semanticNode(NodeType.QUALIFIED_NAME, 91, 97),
			semanticNode(NodeType.BINARY_EXPRESSION, 61, 103),
			semanticNode(NodeType.QUALIFIED_NAME, 112, 130),
			semanticNode(NodeType.IDENTIFIER, 112, 118),
			semanticNode(NodeType.FIELD_ACCESS, 112, 122),
			semanticNode(NodeType.FIELD_ACCESS, 112, 130),
			semanticNode(NodeType.IDENTIFIER, 131, 135),
			semanticNode(NodeType.FIELD_ACCESS, 131, 140),
			semanticNode(NodeType.METHOD_INVOCATION, 131, 142),
			semanticNode(NodeType.METHOD_INVOCATION, 112, 143),
			semanticNode(NodeType.BLOCK, 107, 148),
			semanticNode(NodeType.IF_STATEMENT, 57, 148),
			semanticNode(NodeType.BLOCK, 53, 151),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 151),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 153, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 154),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 61, 64),
			semanticNode(NodeType.QUALIFIED_NAME, 76, 95),
			semanticNode(NodeType.BINARY_EXPRESSION, 61, 101),
			semanticNode(NodeType.QUALIFIED_NAME, 110, 128),
			semanticNode(NodeType.IDENTIFIER, 110, 116),
			semanticNode(NodeType.FIELD_ACCESS, 110, 120),
			semanticNode(NodeType.FIELD_ACCESS, 110, 128),
			semanticNode(NodeType.IDENTIFIER, 129, 134),
			semanticNode(NodeType.FIELD_ACCESS, 129, 141),
			semanticNode(NodeType.METHOD_INVOCATION, 129, 143),
			semanticNode(NodeType.METHOD_INVOCATION, 110, 144),
			semanticNode(NodeType.BLOCK, 105, 149),
			semanticNode(NodeType.IF_STATEMENT, 57, 149),
			semanticNode(NodeType.BLOCK, 53, 152),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 152),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 154, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 155),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 61, 64),
			semanticNode(NodeType.QUALIFIED_NAME, 76, 83),
			semanticNode(NodeType.BINARY_EXPRESSION, 61, 85),
			semanticNode(NodeType.QUALIFIED_NAME, 94, 112),
			semanticNode(NodeType.IDENTIFIER, 94, 100),
			semanticNode(NodeType.FIELD_ACCESS, 94, 104),
			semanticNode(NodeType.FIELD_ACCESS, 94, 112),
			semanticNode(NodeType.IDENTIFIER, 113, 114),
			semanticNode(NodeType.INTEGER_LITERAL, 117, 118),
			semanticNode(NodeType.BINARY_EXPRESSION, 113, 118),
			semanticNode(NodeType.METHOD_INVOCATION, 94, 119),
			semanticNode(NodeType.BLOCK, 89, 124),
			semanticNode(NodeType.IF_STATEMENT, 57, 124),
			semanticNode(NodeType.BLOCK, 53, 127),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 127),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 129, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 130),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 61, 64),
			semanticNode(NodeType.QUALIFIED_NAME, 76, 82),
			semanticNode(NodeType.BINARY_EXPRESSION, 61, 84),
			semanticNode(NodeType.IDENTIFIER, 88, 89),
			semanticNode(NodeType.FIELD_ACCESS, 88, 96),
			semanticNode(NodeType.METHOD_INVOCATION, 88, 98),
			semanticNode(NodeType.INTEGER_LITERAL, 101, 102),
			semanticNode(NodeType.BINARY_EXPRESSION, 88, 102),
			semanticNode(NodeType.BINARY_EXPRESSION, 61, 102),
			semanticNode(NodeType.QUALIFIED_NAME, 111, 129),
			semanticNode(NodeType.IDENTIFIER, 111, 117),
			semanticNode(NodeType.FIELD_ACCESS, 111, 121),
			semanticNode(NodeType.FIELD_ACCESS, 111, 129),
			semanticNode(NodeType.IDENTIFIER, 130, 131),
			semanticNode(NodeType.METHOD_INVOCATION, 111, 132),
			semanticNode(NodeType.BLOCK, 106, 137),
			semanticNode(NodeType.IF_STATEMENT, 57, 137),
			semanticNode(NodeType.BLOCK, 53, 140),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 140),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 142, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 143),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 61, 64),
			semanticNode(NodeType.QUALIFIED_NAME, 76, 82),
			semanticNode(NodeType.BINARY_EXPRESSION, 61, 88),
			semanticNode(NodeType.QUALIFIED_NAME, 97, 115),
			semanticNode(NodeType.IDENTIFIER, 97, 103),
			semanticNode(NodeType.FIELD_ACCESS, 97, 107),
			semanticNode(NodeType.FIELD_ACCESS, 97, 115),
			semanticNode(NodeType.IDENTIFIER, 116, 119),
			semanticNode(NodeType.FIELD_ACCESS, 116, 126),
			semanticNode(NodeType.METHOD_INVOCATION, 97, 127),
			semanticNode(NodeType.BLOCK, 92, 132),
			semanticNode(NodeType.IF_STATEMENT, 57, 132),
			semanticNode(NodeType.BLOCK, 53, 135),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 135),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 137, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 138),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 63, 66),
			semanticNode(NodeType.QUALIFIED_NAME, 78, 84),
			semanticNode(NodeType.BINARY_EXPRESSION, 63, 86),
			semanticNode(NodeType.UNARY_EXPRESSION, 61, 86),
			semanticNode(NodeType.QUALIFIED_NAME, 96, 114),
			semanticNode(NodeType.IDENTIFIER, 96, 102),
			semanticNode(NodeType.FIELD_ACCESS, 96, 106),
			semanticNode(NodeType.FIELD_ACCESS, 96, 114),
			semanticNode(NodeType.STRING_LITERAL, 115, 129),
			semanticNode(NodeType.METHOD_INVOCATION, 96, 130),
			semanticNode(NodeType.BLOCK, 91, 135),
			semanticNode(NodeType.IF_STATEMENT, 57, 135),
			semanticNode(NodeType.BLOCK, 53, 138),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 138),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 140, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 141),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.QUALIFIED_NAME, 57, 63),
			semanticNode(NodeType.IDENTIFIER, 73, 76),
			semanticNode(NodeType.QUALIFIED_NAME, 88, 94),
			semanticNode(NodeType.BINARY_EXPRESSION, 73, 96),
			semanticNode(NodeType.IDENTIFIER, 99, 100),
			semanticNode(NodeType.STRING_LITERAL, 103, 112),
			semanticNode(NodeType.CONDITIONAL_EXPRESSION, 73, 112),
			semanticNode(NodeType.QUALIFIED_NAME, 116, 134),
			semanticNode(NodeType.IDENTIFIER, 116, 122),
			semanticNode(NodeType.FIELD_ACCESS, 116, 126),
			semanticNode(NodeType.FIELD_ACCESS, 116, 134),
			semanticNode(NodeType.IDENTIFIER, 135, 141),
			semanticNode(NodeType.METHOD_INVOCATION, 116, 142),
			semanticNode(NodeType.BLOCK, 53, 146),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 146),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 148, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 149),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.QUALIFIED_NAME, 53, 59),
			semanticNode(NodeType.IDENTIFIER, 75, 79),
			semanticNode(NodeType.QUALIFIED_NAME, 91, 97),
			semanticNode(NodeType.BINARY_EXPRESSION, 75, 99),
			semanticNode(NodeType.IDENTIFIER, 103, 107),
			semanticNode(NodeType.QUALIFIED_NAME, 119, 126),
			semanticNode(NodeType.BINARY_EXPRESSION, 103, 128),
			semanticNode(NodeType.BINARY_EXPRESSION, 75, 128),
			semanticNode(NodeType.QUALIFIED_NAME, 137, 155),
			semanticNode(NodeType.IDENTIFIER, 137, 143),
			semanticNode(NodeType.FIELD_ACCESS, 137, 147),
			semanticNode(NodeType.FIELD_ACCESS, 137, 155),
			semanticNode(NodeType.IDENTIFIER, 156, 157),
			semanticNode(NodeType.IDENTIFIER, 160, 161),
			semanticNode(NodeType.BINARY_EXPRESSION, 156, 161),
			semanticNode(NodeType.METHOD_INVOCATION, 137, 162),
			semanticNode(NodeType.BLOCK, 132, 167),
			semanticNode(NodeType.IF_STATEMENT, 71, 167),
			semanticNode(NodeType.BLOCK, 67, 170),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 170),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 172, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 173),
			semanticNode(PARAMETER_DECLARATION, 40, 51, "obj1"),
			semanticNode(PARAMETER_DECLARATION, 53, 64, "obj2"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 40, 46),
			semanticNode(NodeType.IDENTIFIER, 61, 64),
			semanticNode(NodeType.QUALIFIED_NAME, 76, 82),
			semanticNode(NodeType.BINARY_EXPRESSION, 61, 82),
			semanticNode(NodeType.QUALIFIED_NAME, 91, 109),
			semanticNode(NodeType.IDENTIFIER, 91, 97),
			semanticNode(NodeType.FIELD_ACCESS, 91, 101),
			semanticNode(NodeType.FIELD_ACCESS, 91, 109),
			semanticNode(NodeType.STRING_LITERAL, 110, 125),
			semanticNode(NodeType.METHOD_INVOCATION, 91, 126),
			semanticNode(NodeType.BLOCK, 86, 131),
			semanticNode(NodeType.IF_STATEMENT, 57, 131),
			semanticNode(NodeType.BLOCK, 53, 134),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 134),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 136, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 137),
			semanticNode(PARAMETER_DECLARATION, 40, 50, "obj"));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.QUALIFIED_NAME, 47, 53),
			semanticNode(NodeType.IDENTIFIER, 60, 69),
			semanticNode(NodeType.METHOD_INVOCATION, 60, 71),
			semanticNode(NodeType.IDENTIFIER, 82, 85),
			semanticNode(NodeType.QUALIFIED_NAME, 97, 103),
			semanticNode(NodeType.BINARY_EXPRESSION, 82, 105),
			semanticNode(NodeType.QUALIFIED_NAME, 114, 132),
			semanticNode(NodeType.IDENTIFIER, 114, 120),
			semanticNode(NodeType.FIELD_ACCESS, 114, 124),
			semanticNode(NodeType.FIELD_ACCESS, 114, 132),
			semanticNode(NodeType.IDENTIFIER, 133, 134),
			semanticNode(NodeType.METHOD_INVOCATION, 114, 135),
			semanticNode(NodeType.QUALIFIED_NAME, 140, 143),
			semanticNode(NodeType.IDENTIFIER, 140, 143),
			semanticNode(NodeType.IDENTIFIER, 146, 155),
			semanticNode(NodeType.METHOD_INVOCATION, 146, 157),
			semanticNode(NodeType.ASSIGNMENT_EXPRESSION, 140, 157),
			semanticNode(NodeType.BLOCK, 109, 162),
			semanticNode(NodeType.WHILE_STATEMENT, 75, 162),
			semanticNode(NodeType.BLOCK, 43, 165),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 165),
			semanticNode(NodeType.NULL_LITERAL, 206, 210),
			semanticNode(NodeType.RETURN_STATEMENT, 199, 211),
			semanticNode(NodeType.BLOCK, 195, 214),
			semanticNode(NodeType.METHOD_DECLARATION, 167, 214),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 216, "Test"),
			semanticNode(NodeType.COMPILATION_UNIT, 0, 217));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
