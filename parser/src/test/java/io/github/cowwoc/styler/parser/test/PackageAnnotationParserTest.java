package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.PackageAttribute;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 11);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 11);
			expected.allocateNode(NodeType.ANNOTATION, 0, 11);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 20, 31);
			expected.allocatePackageDeclaration(0, 32, new PackageAttribute("com.example"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 33);
			requireThat(actual, "actual").isEqualTo(expected);
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 17);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 17);
			expected.allocateNode(NodeType.STRING_LITERAL, 18, 29);
			expected.allocateNode(NodeType.ANNOTATION, 0, 30);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 50);
			expected.allocatePackageDeclaration(0, 51, new PackageAttribute("com.example"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			// Qualified names first pass
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 11);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 13, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 53);
			// @Deprecated
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 11);
			expected.allocateNode(NodeType.ANNOTATION, 0, 11);
			// @SuppressWarnings("unchecked")
			expected.allocateNode(NodeType.QUALIFIED_NAME, 13, 29);
			expected.allocateNode(NodeType.STRING_LITERAL, 30, 41);
			expected.allocateNode(NodeType.ANNOTATION, 12, 42);
			// @Generated("test")
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 53);
			expected.allocateNode(NodeType.STRING_LITERAL, 54, 60);
			expected.allocateNode(NodeType.ANNOTATION, 43, 61);
			// package com.example
			expected.allocateNode(NodeType.QUALIFIED_NAME, 70, 81);
			expected.allocatePackageDeclaration(0, 82, new PackageAttribute("com.example"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 83);
			requireThat(actual, "actual").isEqualTo(expected);
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 27);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 27);
			expected.allocateNode(NodeType.STRING_LITERAL, 28, 34);
			expected.allocateNode(NodeType.ANNOTATION, 0, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 55);
			expected.allocatePackageDeclaration(0, 56, new PackageAttribute("com.example"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 17);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 17);
			expected.allocateNode(NodeType.STRING_LITERAL, 19, 30);
			expected.allocateNode(NodeType.STRING_LITERAL, 32, 42);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 18, 43);
			expected.allocateNode(NodeType.ANNOTATION, 0, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 64);
			expected.allocatePackageDeclaration(0, 65, new PackageAttribute("com.example"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			// Qualified names first pass
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 11);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 70);
			// @Deprecated
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 11);
			expected.allocateNode(NodeType.ANNOTATION, 0, 11);
			// // comment
			expected.allocateNode(NodeType.LINE_COMMENT, 12, 52);
			// @SuppressWarnings("unchecked")
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 70);
			expected.allocateNode(NodeType.STRING_LITERAL, 71, 82);
			expected.allocateNode(NodeType.ANNOTATION, 53, 83);
			// package com.example
			expected.allocateNode(NodeType.QUALIFIED_NAME, 92, 103);
			expected.allocatePackageDeclaration(0, 104, new PackageAttribute("com.example"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 105);
			requireThat(actual, "actual").isEqualTo(expected);
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
}
