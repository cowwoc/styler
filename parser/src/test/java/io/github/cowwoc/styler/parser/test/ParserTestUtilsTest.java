package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.IMPORT_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PACKAGE_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for {@link ParserTestUtils} utility methods.
 * <p>
 * Validates that the AST parsing and comparison utilities work correctly for test assertions.
 */
public class ParserTestUtilsTest
{
	/**
	 * Verifies that semantic AST comparison works correctly.
	 * The semantic comparison uses Set equality, so node order does not matter.
	 * Position information is included to distinguish nodes at different locations.
	 */
	@Test
	public void shouldCompareSemanticAst()
	{
		String source = "class Test { }";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(CLASS_DECLARATION, 0, 14, "Test"),
			semanticNode(COMPILATION_UNIT, 0, 14));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that semantic comparison correctly identifies different ASTs.
	 * Two different source files should produce different semantic node sets.
	 */
	@Test
	public void semanticAstShouldDistinguishDifferentSources()
	{
		Set<SemanticNode> ast1 = parseSemanticAst("class Foo { }");
		Set<SemanticNode> ast2 = parseSemanticAst("class Bar { }");

		// Same structure, different type names
		requireThat(ast1, "ast1").isNotEqualTo(ast2);
	}

	/**
	 * Verifies that semantic comparison is order-independent.
	 * The expected set can be constructed in any order and still match.
	 */
	@Test
	public void semanticAstShouldBeOrderIndependent()
	{
		String source = """
			import java.util.List;

			class Test
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		// Expected nodes listed in different order than they appear in source
		Set<SemanticNode> expected = Set.of(
			semanticNode(CLASS_DECLARATION, 24, 38, "Test"),
			semanticNode(IMPORT_DECLARATION, 0, 22, "java.util.List"),
			semanticNode(COMPILATION_UNIT, 0, 39));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies semantic comparison distinguishes nested classes by position.
	 * Two classes with the same name at different positions should be distinct nodes.
	 */
	@Test
	public void semanticAstShouldDistinguishNestedClasses()
	{
		String source = """
			class Outer
			{
				class Inner { }
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 33),
			semanticNode(CLASS_DECLARATION, 0, 32, "Outer"),
			semanticNode(CLASS_DECLARATION, 15, 30, "Inner"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies semantic comparison correctly handles multiple top-level types.
	 * Each type should be distinguishable by both name and position.
	 */
	@Test
	public void semanticAstShouldHandleMultipleTopLevelTypes()
	{
		String source = """
			class First { }
			class Second { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 33),
			semanticNode(CLASS_DECLARATION, 0, 15, "First"),
			semanticNode(CLASS_DECLARATION, 16, 32, "Second"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies semantic comparison distinguishes nodes with same type but different positions.
	 * Position information is critical for distinguishing structurally similar nodes.
	 */
	@Test
	public void semanticAstShouldDistinguishByPosition()
	{
		String source = """
			class Test
			{
				void foo() { }
				void bar() { }
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		// Two METHOD_DECLARATION nodes at different positions, each with its own BLOCK
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 47),
			semanticNode(CLASS_DECLARATION, 0, 46, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 28),
			semanticNode(METHOD_DECLARATION, 30, 44),
			semanticNode(BLOCK, 25, 28),
			semanticNode(BLOCK, 41, 44));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies semantic comparison with package, imports, and class together.
	 * Tests a realistic compilation unit structure with all common elements.
	 */
	@Test
	public void semanticAstShouldHandleFullCompilationUnit()
	{
		String source = """
			package com.example;

			import java.util.List;
			import java.util.Map;

			class Test { }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 83),
			semanticNode(PACKAGE_DECLARATION, 0, 20, "com.example"),
			semanticNode(QUALIFIED_NAME, 8, 19),
			semanticNode(IMPORT_DECLARATION, 22, 44, "java.util.List"),
			semanticNode(IMPORT_DECLARATION, 45, 66, "java.util.Map"),
			semanticNode(CLASS_DECLARATION, 68, 82, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies semantic nodes are truly value-based (equal by content, not identity).
	 * Two independently created nodes with same values should be equal.
	 */
	@Test
	public void semanticNodeShouldBeValueBased()
	{
		SemanticNode node1 = semanticNode(CLASS_DECLARATION, 0, 10, "Test");
		SemanticNode node2 = semanticNode(CLASS_DECLARATION, 0, 10, "Test");

		requireThat(node1, "node1").isEqualTo(node2);
		requireThat(node1.hashCode(), "node1.hashCode()").isEqualTo(node2.hashCode());
	}

	/**
	 * Verifies semantic nodes with different positions are not equal.
	 * Even with same type and name, position difference makes nodes distinct.
	 */
	@Test
	public void semanticNodeShouldDifferByPosition()
	{
		SemanticNode node1 = semanticNode(CLASS_DECLARATION, 0, 10, "Test");
		SemanticNode node2 = semanticNode(CLASS_DECLARATION, 20, 30, "Test");

		requireThat(node1, "node1").isNotEqualTo(node2);
	}

	/**
	 * Verifies semantic nodes with null attribute are handled correctly.
	 * Type-only nodes (no name/value) should be distinguishable by position.
	 */
	@Test
	public void semanticNodeShouldHandleNullAttribute()
	{
		SemanticNode node1 = semanticNode(BLOCK, 0, 10);
		SemanticNode node2 = semanticNode(BLOCK, 0, 10);
		SemanticNode node3 = semanticNode(BLOCK, 5, 15);

		requireThat(node1, "node1").isEqualTo(node2);
		requireThat(node1, "node1").isNotEqualTo(node3);
	}
}
