package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.THIS_EXPRESSION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 52),
			semanticNode(CLASS_DECLARATION, 0, 51, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 49),
			semanticNode(BLOCK, 24, 49),
			semanticNode(OBJECT_CREATION, 28, 45),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(IDENTIFIER, 28, 33),
			semanticNode(QUALIFIED_NAME, 38, 43));
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
			semanticNode(COMPILATION_UNIT, 0, 63),
			semanticNode(CLASS_DECLARATION, 0, 62, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 60),
			semanticNode(BLOCK, 24, 60),
			semanticNode(METHOD_INVOCATION, 28, 56),
			semanticNode(OBJECT_CREATION, 28, 45),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(IDENTIFIER, 28, 33),
			semanticNode(QUALIFIED_NAME, 38, 43),
			semanticNode(FIELD_ACCESS, 28, 54));
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
			semanticNode(COMPILATION_UNIT, 0, 57),
			semanticNode(CLASS_DECLARATION, 0, 56, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 54),
			semanticNode(BLOCK, 24, 54),
			semanticNode(OBJECT_CREATION, 28, 50),
			semanticNode(METHOD_INVOCATION, 28, 38),
			semanticNode(QUALIFIED_NAME, 28, 36),
			semanticNode(IDENTIFIER, 28, 36),
			semanticNode(QUALIFIED_NAME, 43, 48));
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
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(CLASS_DECLARATION, 0, 55, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 53),
			semanticNode(BLOCK, 24, 53),
			semanticNode(OBJECT_CREATION, 28, 49),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(IDENTIFIER, 28, 33),
			semanticNode(QUALIFIED_NAME, 38, 43),
			semanticNode(INTEGER_LITERAL, 44, 45),
			semanticNode(INTEGER_LITERAL, 47, 48));
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
			semanticNode(COMPILATION_UNIT, 0, 57),
			semanticNode(CLASS_DECLARATION, 0, 56, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 54),
			semanticNode(BLOCK, 24, 54),
			semanticNode(OBJECT_CREATION, 28, 50),
			semanticNode(THIS_EXPRESSION, 28, 38),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(IDENTIFIER, 28, 33),
			semanticNode(QUALIFIED_NAME, 43, 48));
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
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(CLASS_DECLARATION, 0, 59, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 57),
			semanticNode(BLOCK, 24, 57),
			semanticNode(OBJECT_CREATION, 28, 53),
			semanticNode(QUALIFIED_NAME, 28, 34),
			semanticNode(IDENTIFIER, 28, 33),
			semanticNode(QUALIFIED_NAME, 38, 43));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
