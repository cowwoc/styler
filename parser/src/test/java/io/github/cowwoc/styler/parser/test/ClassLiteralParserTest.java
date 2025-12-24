package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONDITIONAL_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing class literal expressions ({@code Type.class}).
 */
public final class ClassLiteralParserTest
{
	/**
	 * Validates that a simple class literal on a reference type parses correctly.
	 * Tests the most common form: {@code String.class}.
	 */
	@Test
	public void testSimpleClassLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = String.class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(CLASS_DECLARATION, 0, 59, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 57),
			semanticNode(BLOCK, 24, 57),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CLASS_LITERAL, 41, 53),
			semanticNode(IDENTIFIER, 41, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals on fully qualified types parse correctly.
	 * Tests qualified names like {@code java.lang.String.class}.
	 */
	@Test
	public void testClassLiteralOnQualifiedType()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = java.lang.String.class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 70),
			semanticNode(CLASS_DECLARATION, 0, 69, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 67),
			semanticNode(BLOCK, 24, 67),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CLASS_LITERAL, 41, 63),
			semanticNode(FIELD_ACCESS, 41, 50),
			semanticNode(FIELD_ACCESS, 41, 57),
			semanticNode(IDENTIFIER, 41, 45));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals on nested classes parse correctly.
	 * Tests accessing class literal of inner class: {@code Outer.Inner.class}.
	 */
	@Test
	public void testClassLiteralOnNestedClass()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = Outer.Inner.class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 65),
			semanticNode(CLASS_DECLARATION, 0, 64, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 62),
			semanticNode(BLOCK, 24, 62),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CLASS_LITERAL, 41, 58),
			semanticNode(FIELD_ACCESS, 41, 52),
			semanticNode(IDENTIFIER, 41, 46));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals on primitive types parse correctly.
	 * Tests {@code int.class} which is a valid Java construct.
	 */
	@Test
	public void testIntClassLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = int.class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 57),
			semanticNode(CLASS_DECLARATION, 0, 56, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 54),
			semanticNode(BLOCK, 24, 54),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CLASS_LITERAL, 41, 50));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals on void type parse correctly.
	 * Tests {@code void.class} which represents the Void.TYPE class object.
	 */
	@Test
	public void testVoidClassLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = void.class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 58),
			semanticNode(CLASS_DECLARATION, 0, 57, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 55),
			semanticNode(BLOCK, 24, 55),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CLASS_LITERAL, 41, 51));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals on single-dimension arrays parse correctly.
	 * Tests {@code String[].class} for array type class objects.
	 */
	@Test
	public void testSingleDimensionArrayClassLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = String[].class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 62),
			semanticNode(CLASS_DECLARATION, 0, 61, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 59),
			semanticNode(BLOCK, 24, 59),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CLASS_LITERAL, 41, 55),
			semanticNode(IDENTIFIER, 41, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals on primitive arrays parse correctly.
	 * Tests {@code int[].class} for primitive array type class objects.
	 */
	@Test
	public void testPrimitiveArrayClassLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = int[].class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 59),
			semanticNode(CLASS_DECLARATION, 0, 58, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 56),
			semanticNode(BLOCK, 24, 56),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CLASS_LITERAL, 41, 52));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals on multi-dimensional arrays parse correctly.
	 * Tests {@code int[][].class} for 2D array class objects.
	 */
	@Test
	public void testMultiDimensionalArrayClassLiteral()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = int[][].class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 61),
			semanticNode(CLASS_DECLARATION, 0, 60, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 58),
			semanticNode(BLOCK, 24, 58),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CLASS_LITERAL, 41, 54));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals work correctly as method arguments.
	 * Tests passing class literals directly to methods: {@code accept(String.class)}.
	 */
	@Test
	public void testClassLiteralAsMethodArgument()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					accept(String.class);
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 55),
			semanticNode(CLASS_DECLARATION, 0, 54, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 52),
			semanticNode(BLOCK, 24, 52),
			semanticNode(METHOD_INVOCATION, 28, 48),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(IDENTIFIER, 28, 34),
			semanticNode(IDENTIFIER, 35, 41),
			semanticNode(CLASS_LITERAL, 35, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals work in return statements.
	 * Tests returning class literals from methods.
	 */
	@Test
	public void testClassLiteralInReturnStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				Class<?> m()
				{
					return Integer.class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 59),
			semanticNode(CLASS_DECLARATION, 0, 58, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 56),
			semanticNode(BLOCK, 28, 56),
			semanticNode(RETURN_STATEMENT, 32, 53),
			semanticNode(CLASS_LITERAL, 39, 52),
			semanticNode(IDENTIFIER, 39, 46));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that class literals work in ternary conditional expressions.
	 * Tests using class literals as alternatives: {@code flag ? String.class : Object.class}.
	 */
	@Test
	public void testClassLiteralInTernaryExpression()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Class<?> c = flag ? String.class : Object.class;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 82),
			semanticNode(CLASS_DECLARATION, 0, 81, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 79),
			semanticNode(BLOCK, 24, 79),
			semanticNode(QUALIFIED_NAME, 28, 33),
			semanticNode(CONDITIONAL_EXPRESSION, 41, 75),
			semanticNode(IDENTIFIER, 41, 45),
			semanticNode(CLASS_LITERAL, 48, 60),
			semanticNode(IDENTIFIER, 48, 54),
			semanticNode(CLASS_LITERAL, 63, 75),
			semanticNode(IDENTIFIER, 63, 69));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that incomplete class literal syntax fails to parse.
	 * Tests that {@code String.} without {@code class} keyword is rejected.
	 */
	@Test
	public void testMalformedClassLiteralMissingClass()
	{
		assertParseFails("""
			class Test
			{
				void m()
				{
					Class<?> c = String.;
				}
			}
			""");
	}

	/**
	 * Validates that class keyword in normal class declaration context works.
	 * Ensures class literal support does not break normal class declarations.
	 */
	@Test
	public void testClassKeywordWithoutDot()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class TestClass
			{
			}
			""");

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 20),
			semanticNode(CLASS_DECLARATION, 0, 19, "TestClass"));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
