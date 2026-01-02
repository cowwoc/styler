package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.compilationUnit;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.packageNode;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.qualifiedName;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

/**
 * Tests for package declaration attribute population during parsing.
 */
public class ParserPackageAttributeTest
{
	/**
	 * Verifies that a simple package declaration has its name attribute populated.
	 */
	@Test
	public void shouldPopulatePackageNameAttribute()
	{
		String source = """
			package com.example; class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 35),
			packageNode(0, 20, "com.example"),
			qualifiedName(8, 19),
			typeDeclaration(CLASS_DECLARATION, 21, 34, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a deeply nested package has its full name attribute populated.
	 */
	@Test
	public void shouldPopulatePackageNameForDeeplyNestedPackage()
	{
		String source = """
			package io.github.cowwoc.styler.formatter.internal; class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 66),
			packageNode(0, 51, "io.github.cowwoc.styler.formatter.internal"),
			qualifiedName(8, 50),
			typeDeclaration(CLASS_DECLARATION, 52, 65, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a single-segment package has its name attribute populated.
	 */
	@Test
	public void shouldPopulatePackageNameForSingleSegment()
	{
		String source = """
			package utils; class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 29),
			packageNode(0, 14, "utils"),
			qualifiedName(8, 13),
			typeDeclaration(CLASS_DECLARATION, 15, 28, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that the default package (no package declaration) has no PACKAGE_DECLARATION node.
	 */
	@Test
	public void shouldHandleDefaultPackage()
	{
		String source = """
			class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 14),
			typeDeclaration(CLASS_DECLARATION, 0, 13, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
