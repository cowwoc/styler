package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.PackageAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 8, 19);
			expected.allocatePackageDeclaration(0, 20, new PackageAttribute("com.example"));
			expected.allocateClassDeclaration(21, 34, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 35);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 8, 50);
			expected.allocatePackageDeclaration(0, 51,
				new PackageAttribute("io.github.cowwoc.styler.formatter.internal"));
			expected.allocateClassDeclaration(52, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 8, 13);
			expected.allocatePackageDeclaration(0, 14, new PackageAttribute("utils"));
			expected.allocateClassDeclaration(15, 28, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 29);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateClassDeclaration(0, 13, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 14);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
