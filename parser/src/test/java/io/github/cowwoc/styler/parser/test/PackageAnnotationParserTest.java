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
 * Tests for parsing package annotations in package-info.java files.
 */
public class PackageAnnotationParserTest
{
	/**
	 * Verifies that a single marker annotation before the package declaration is parsed correctly.
	 */
	@Test
	public void shouldParseSingleMarkerAnnotationBeforePackage()
	{
		String source = """
			@Deprecated
			package com.example;
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			List<NodeIndex> annotations = findNodesOfType(arena, NodeType.ANNOTATION);
			requireThat(annotations.size(), "annotations.size()").isEqualTo(1);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").isEqualTo("com.example");
		}
	}

	/**
	 * Verifies that an annotation with a value before the package declaration is parsed correctly.
	 */
	@Test
	public void shouldParseSingleAnnotationWithValueBeforePackage()
	{
		String source = """
			@SuppressWarnings("unchecked")
			package com.example;
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			List<NodeIndex> annotations = findNodesOfType(arena, NodeType.ANNOTATION);
			requireThat(annotations.size(), "annotations.size()").isEqualTo(1);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").isEqualTo("com.example");
		}
	}

	/**
	 * Verifies that multiple annotations before the package declaration are all parsed correctly.
	 */
	@Test
	public void shouldParseMultipleAnnotationsBeforePackage()
	{
		String source = """
			@Deprecated
			@SuppressWarnings("unchecked")
			@Generated("test")
			package com.example;
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			List<NodeIndex> annotations = findNodesOfType(arena, NodeType.ANNOTATION);
			requireThat(annotations.size(), "annotations.size()").isEqualTo(3);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").isEqualTo("com.example");
		}
	}

	/**
	 * Verifies that an annotation with a fully qualified name is parsed correctly.
	 */
	@Test
	public void shouldParseAnnotationWithQualifiedNameBeforePackage()
	{
		String source = """
			@javax.annotation.Generated("test")
			package com.example;
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			List<NodeIndex> annotations = findNodesOfType(arena, NodeType.ANNOTATION);
			requireThat(annotations.size(), "annotations.size()").isEqualTo(1);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").isEqualTo("com.example");
		}
	}

	/**
	 * Verifies that an annotation with an array value is parsed correctly.
	 */
	@Test
	public void shouldParseAnnotationWithArrayValueBeforePackage()
	{
		String source = """
			@SuppressWarnings({"unchecked", "rawtypes"})
			package com.example;
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			List<NodeIndex> annotations = findNodesOfType(arena, NodeType.ANNOTATION);
			requireThat(annotations.size(), "annotations.size()").isEqualTo(1);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").isEqualTo("com.example");
		}
	}

	/**
	 * Verifies that comments between annotations are handled correctly.
	 */
	@Test
	public void shouldParseCommentBetweenAnnotations()
	{
		String source = """
			@Deprecated
			// This is a comment between annotations
			@SuppressWarnings("unchecked")
			package com.example;
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> packages = findNodesOfType(arena, NodeType.PACKAGE_DECLARATION);
			requireThat(packages.size(), "packages.size()").isEqualTo(1);

			List<NodeIndex> annotations = findNodesOfType(arena, NodeType.ANNOTATION);
			requireThat(annotations.size(), "annotations.size()").isEqualTo(2);

			PackageAttribute attribute = arena.getPackageAttribute(packages.get(0));
			requireThat(attribute.packageName(), "packageName").isEqualTo("com.example");
		}
	}

	/**
	 * Verifies that annotations followed by import (not package) result in a parse failure.
	 * <p>
	 * This tests the case where annotations appear at file start but are followed by an import
	 * statement instead of a package declaration, which is invalid Java syntax.
	 */
	@Test
	public void shouldRejectAnnotationsWithoutPackageDeclaration()
	{
		String source = """
			@Deprecated
			import java.util.List;
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Failure.class);
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
