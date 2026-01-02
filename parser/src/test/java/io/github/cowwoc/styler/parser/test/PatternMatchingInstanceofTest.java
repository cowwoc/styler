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
			qualifiedName( 40, 46),
			identifier( 61, 64),
			qualifiedName( 76, 82),
			binaryExpression( 61, 84),
			qualifiedName( 93, 111),
			identifier( 93, 99),
			fieldAccess( 93, 103),
			fieldAccess( 93, 111),
			identifier( 112, 113),
			methodInvocation( 93, 114),
			block( 88, 119),
			ifStatement( 57, 119),
			block( 53, 122),
			methodDeclaration( 21, 122),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 124, "Test"),
			compilationUnit( 0, 125),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 61, 64),
			parameterizedType( 76, 98),
			qualifiedName( 76, 90),
			qualifiedName( 91, 97),
			binaryExpression( 61, 103),
			qualifiedName( 112, 130),
			identifier( 112, 118),
			fieldAccess( 112, 122),
			fieldAccess( 112, 130),
			identifier( 131, 135),
			fieldAccess( 131, 140),
			methodInvocation( 131, 142),
			methodInvocation( 112, 143),
			block( 107, 148),
			ifStatement( 57, 148),
			block( 53, 151),
			methodDeclaration( 21, 151),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 153, "Test"),
			compilationUnit( 0, 154),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 61, 64),
			qualifiedName( 76, 95),
			binaryExpression( 61, 101),
			qualifiedName( 110, 128),
			identifier( 110, 116),
			fieldAccess( 110, 120),
			fieldAccess( 110, 128),
			identifier( 129, 134),
			fieldAccess( 129, 141),
			methodInvocation( 129, 143),
			methodInvocation( 110, 144),
			block( 105, 149),
			ifStatement( 57, 149),
			block( 53, 152),
			methodDeclaration( 21, 152),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 154, "Test"),
			compilationUnit( 0, 155),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 61, 64),
			qualifiedName( 76, 83),
			binaryExpression( 61, 85),
			qualifiedName( 94, 112),
			identifier( 94, 100),
			fieldAccess( 94, 104),
			fieldAccess( 94, 112),
			identifier( 113, 114),
			integerLiteral( 117, 118),
			binaryExpression( 113, 118),
			methodInvocation( 94, 119),
			block( 89, 124),
			ifStatement( 57, 124),
			block( 53, 127),
			methodDeclaration( 21, 127),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 129, "Test"),
			compilationUnit( 0, 130),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 61, 64),
			qualifiedName( 76, 82),
			binaryExpression( 61, 84),
			identifier( 88, 89),
			fieldAccess( 88, 96),
			methodInvocation( 88, 98),
			integerLiteral( 101, 102),
			binaryExpression( 88, 102),
			binaryExpression( 61, 102),
			qualifiedName( 111, 129),
			identifier( 111, 117),
			fieldAccess( 111, 121),
			fieldAccess( 111, 129),
			identifier( 130, 131),
			methodInvocation( 111, 132),
			block( 106, 137),
			ifStatement( 57, 137),
			block( 53, 140),
			methodDeclaration( 21, 140),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 142, "Test"),
			compilationUnit( 0, 143),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 61, 64),
			qualifiedName( 76, 82),
			binaryExpression( 61, 88),
			qualifiedName( 97, 115),
			identifier( 97, 103),
			fieldAccess( 97, 107),
			fieldAccess( 97, 115),
			identifier( 116, 119),
			fieldAccess( 116, 126),
			methodInvocation( 97, 127),
			block( 92, 132),
			ifStatement( 57, 132),
			block( 53, 135),
			methodDeclaration( 21, 135),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 137, "Test"),
			compilationUnit( 0, 138),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 40, 46),
			identifier( 63, 66),
			qualifiedName( 78, 84),
			binaryExpression( 63, 86),
			unaryExpression( 61, 86),
			qualifiedName( 96, 114),
			identifier( 96, 102),
			fieldAccess( 96, 106),
			fieldAccess( 96, 114),
			stringLiteral( 115, 129),
			methodInvocation( 96, 130),
			block( 91, 135),
			ifStatement( 57, 135),
			block( 53, 138),
			methodDeclaration( 21, 138),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 140, "Test"),
			compilationUnit( 0, 141),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 40, 46),
			qualifiedName( 57, 63),
			identifier( 73, 76),
			qualifiedName( 88, 94),
			binaryExpression( 73, 96),
			identifier( 99, 100),
			stringLiteral( 103, 112),
			conditionalExpression( 73, 112),
			qualifiedName( 116, 134),
			identifier( 116, 122),
			fieldAccess( 116, 126),
			fieldAccess( 116, 134),
			identifier( 135, 141),
			methodInvocation( 116, 142),
			block( 53, 146),
			methodDeclaration( 21, 146),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 148, "Test"),
			compilationUnit( 0, 149),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 40, 46),
			qualifiedName( 53, 59),
			identifier( 75, 79),
			qualifiedName( 91, 97),
			binaryExpression( 75, 99),
			identifier( 103, 107),
			qualifiedName( 119, 126),
			binaryExpression( 103, 128),
			binaryExpression( 75, 128),
			qualifiedName( 137, 155),
			identifier( 137, 143),
			fieldAccess( 137, 147),
			fieldAccess( 137, 155),
			identifier( 156, 157),
			identifier( 160, 161),
			binaryExpression( 156, 161),
			methodInvocation( 137, 162),
			block( 132, 167),
			ifStatement( 71, 167),
			block( 67, 170),
			methodDeclaration( 21, 170),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 172, "Test"),
			compilationUnit( 0, 173),
			parameterNode( 40, 51, "obj1"),
			parameterNode( 53, 64, "obj2"));
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
			qualifiedName( 40, 46),
			identifier( 61, 64),
			qualifiedName( 76, 82),
			binaryExpression( 61, 82),
			qualifiedName( 91, 109),
			identifier( 91, 97),
			fieldAccess( 91, 101),
			fieldAccess( 91, 109),
			stringLiteral( 110, 125),
			methodInvocation( 91, 126),
			block( 86, 131),
			ifStatement( 57, 131),
			block( 53, 134),
			methodDeclaration( 21, 134),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 136, "Test"),
			compilationUnit( 0, 137),
			parameterNode( 40, 50, "obj"));
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
			qualifiedName( 47, 53),
			identifier( 60, 69),
			methodInvocation( 60, 71),
			identifier( 82, 85),
			qualifiedName( 97, 103),
			binaryExpression( 82, 105),
			qualifiedName( 114, 132),
			identifier( 114, 120),
			fieldAccess( 114, 124),
			fieldAccess( 114, 132),
			identifier( 133, 134),
			methodInvocation( 114, 135),
			qualifiedName( 140, 143),
			identifier( 140, 143),
			identifier( 146, 155),
			methodInvocation( 146, 157),
			assignmentExpression( 140, 157),
			block( 109, 162),
			whileStatement( 75, 162),
			block( 43, 165),
			methodDeclaration( 21, 165),
			nullLiteral( 206, 210),
			returnStatement( 199, 211),
			block( 195, 214),
			methodDeclaration( 167, 214),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 216, "Test"),
			compilationUnit( 0, 217));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
