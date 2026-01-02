package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

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
			compilationUnit( 0, 60),
			typeDeclaration(CLASS_DECLARATION, 0, 59, "Test"),
			methodDeclaration( 14, 57),
			block( 24, 57),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			classLiteral( 41, 53),
			identifier( 41, 47));
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
			compilationUnit( 0, 70),
			typeDeclaration(CLASS_DECLARATION, 0, 69, "Test"),
			methodDeclaration( 14, 67),
			block( 24, 67),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			classLiteral( 41, 63),
			fieldAccess( 41, 50),
			fieldAccess( 41, 57),
			identifier( 41, 45));
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
			compilationUnit( 0, 65),
			typeDeclaration(CLASS_DECLARATION, 0, 64, "Test"),
			methodDeclaration( 14, 62),
			block( 24, 62),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			classLiteral( 41, 58),
			fieldAccess( 41, 52),
			identifier( 41, 46));
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
			compilationUnit( 0, 57),
			typeDeclaration(CLASS_DECLARATION, 0, 56, "Test"),
			methodDeclaration( 14, 54),
			block( 24, 54),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			classLiteral( 41, 50));
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
			compilationUnit( 0, 58),
			typeDeclaration(CLASS_DECLARATION, 0, 57, "Test"),
			methodDeclaration( 14, 55),
			block( 24, 55),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			classLiteral( 41, 51));
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
			compilationUnit( 0, 62),
			typeDeclaration(CLASS_DECLARATION, 0, 61, "Test"),
			methodDeclaration( 14, 59),
			block( 24, 59),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			classLiteral( 41, 55),
			identifier( 41, 47));
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
			compilationUnit( 0, 59),
			typeDeclaration(CLASS_DECLARATION, 0, 58, "Test"),
			methodDeclaration( 14, 56),
			block( 24, 56),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			classLiteral( 41, 52));
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
			compilationUnit( 0, 61),
			typeDeclaration(CLASS_DECLARATION, 0, 60, "Test"),
			methodDeclaration( 14, 58),
			block( 24, 58),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			classLiteral( 41, 54));
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
			compilationUnit( 0, 55),
			typeDeclaration(CLASS_DECLARATION, 0, 54, "Test"),
			methodDeclaration( 14, 52),
			block( 24, 52),
			methodInvocation( 28, 48),
			qualifiedName( 28, 34),
			identifier( 28, 34),
			identifier( 35, 41),
			classLiteral( 35, 47));
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
			compilationUnit( 0, 59),
			typeDeclaration(CLASS_DECLARATION, 0, 58, "Test"),
			methodDeclaration( 14, 56),
			wildcardType( 20, 21),
			block( 28, 56),
			returnStatement( 32, 53),
			classLiteral( 39, 52),
			identifier( 39, 46));
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
			compilationUnit( 0, 82),
			typeDeclaration(CLASS_DECLARATION, 0, 81, "Test"),
			methodDeclaration( 14, 79),
			block( 24, 79),
			parameterizedType( 28, 36),
			qualifiedName( 28, 33),
			wildcardType( 34, 35),
			conditionalExpression( 41, 75),
			identifier( 41, 45),
			classLiteral( 48, 60),
			identifier( 48, 54),
			classLiteral( 63, 75),
			identifier( 63, 69));
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
			compilationUnit( 0, 20),
			typeDeclaration(CLASS_DECLARATION, 0, 19, "TestClass"));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
