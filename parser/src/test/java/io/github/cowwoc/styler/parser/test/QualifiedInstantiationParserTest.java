package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

/**
 * Tests for parsing qualified class instantiation expressions ({@code outer.new Inner()}).
 */
public final class QualifiedInstantiationParserTest
{
	/**
	 * Validates that a simple qualified instantiation parses correctly.
	 * Tests the form {@code outer.new Inner()} used to create inner class instances.
	 */
	@Test
	public void testSimpleQualifiedInstantiation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					outer.new Inner();
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 52),
			typeDeclaration(CLASS_DECLARATION, 0, 51, "Test"),
			methodDeclaration( 14, 49),
			block( 24, 49),
			objectCreation( 28, 45),
			qualifiedName( 28, 34),
			identifier( 28, 33),
			qualifiedName( 38, 43));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a chained method call after qualified instantiation parses correctly.
	 * Tests the form {@code outer.new Inner().method()}.
	 */
	@Test
	public void testChainedQualifiedInstantiation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					outer.new Inner().getValue();
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 63),
			typeDeclaration(CLASS_DECLARATION, 0, 62, "Test"),
			methodDeclaration( 14, 60),
			block( 24, 60),
			methodInvocation( 28, 56),
			objectCreation( 28, 45),
			qualifiedName( 28, 34),
			identifier( 28, 33),
			qualifiedName( 38, 43),
			fieldAccess( 28, 54));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified instantiation with an expression qualifier parses correctly.
	 * Tests the form {@code getOuter().new Inner()}.
	 */
	@Test
	public void testExpressionQualifierInstantiation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					getOuter().new Inner();
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 57),
			typeDeclaration(CLASS_DECLARATION, 0, 56, "Test"),
			methodDeclaration( 14, 54),
			block( 24, 54),
			objectCreation( 28, 50),
			methodInvocation( 28, 38),
			qualifiedName( 28, 36),
			identifier( 28, 36),
			qualifiedName( 43, 48));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified instantiation with constructor arguments parses correctly.
	 * Tests the form {@code outer.new Inner(arg1, arg2)}.
	 */
	@Test
	public void testQualifiedInstantiationWithArguments()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					outer.new Inner(1, 2);
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 56),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "Test"),
			methodDeclaration( 14, 53),
			block( 24, 53),
			objectCreation( 28, 49),
			qualifiedName( 28, 34),
			identifier( 28, 33),
			qualifiedName( 38, 43),
			integerLiteral( 44, 45),
			integerLiteral( 47, 48));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that nested qualified instantiation parses correctly.
	 * Tests creating an inner class instance using {@code Outer.this.new Inner()}.
	 */
	@Test
	public void testNestedQualifiedInstantiation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					Outer.this.new Inner();
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 57),
			typeDeclaration(CLASS_DECLARATION, 0, 56, "Test"),
			methodDeclaration( 14, 54),
			block( 24, 54),
			objectCreation( 28, 50),
			thisExpression( 28, 38),
			qualifiedName( 28, 34),
			identifier( 28, 33),
			qualifiedName( 43, 48));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified instantiation with anonymous class body parses correctly.
	 * Tests the form {@code outer.new Inner() { ... }}.
	 */
	@Test
	public void testQualifiedInstantiationWithAnonymousClass()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Test
			{
				void m()
				{
					outer.new Inner()
					{
					};
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 60),
			typeDeclaration(CLASS_DECLARATION, 0, 59, "Test"),
			methodDeclaration( 14, 57),
			block( 24, 57),
			objectCreation( 28, 53),
			qualifiedName( 28, 34),
			identifier( 28, 33),
			qualifiedName( 38, 43));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
