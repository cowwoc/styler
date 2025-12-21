package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.PackageAttribute;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for package declaration attribute population during parsing.
 * <p>
 * Verifies that the parser correctly populates the {@code packageName} attribute
 * for package declarations.
 * <p>
 * <b>Thread-safety</b>: Thread-safe - all instances are created inside {@code @Test} methods.
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").isEqualTo("com.example");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").
				isEqualTo("io.github.cowwoc.styler.formatter.internal");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").isEqualTo("utils");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(0);
		}
	}

	/**
	 * Finds all nodes of the specified type in the arena.
	 *
	 * @param arena the arena to search
	 * @param type  the node type to find
	 * @return list of node indices matching the type
	 */
	private List<NodeIndex> findNodesOfType(NodeArena arena, NodeType type)
	{
		List<NodeIndex> result = new ArrayList<>();
		for (int i = 0; i < arena.getNodeCount(); ++i)
		{
			NodeIndex index = new NodeIndex(i);
			if (arena.getType(index) == type)
			{
				result.add(index);
			}
		}
		return result;
	}
}
