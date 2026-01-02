package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.annotation;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.arrayInitializer;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.compilationUnit;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.lineComment;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.packageNode;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.qualifiedName;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.stringLiteral;

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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 33),
			annotation(0, 11),
			qualifiedName(1, 11),
			packageNode(0, 32, "com.example"),
			qualifiedName(20, 31));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 52),
			annotation(0, 30),
			qualifiedName(1, 17),
			stringLiteral(18, 29),
			packageNode(0, 51, "com.example"),
			qualifiedName(39, 50));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 83),
			annotation(0, 11),
			qualifiedName(1, 11),
			annotation(12, 42),
			qualifiedName(13, 29),
			stringLiteral(30, 41),
			annotation(43, 61),
			qualifiedName(44, 53),
			stringLiteral(54, 60),
			packageNode(0, 82, "com.example"),
			qualifiedName(70, 81));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 57),
			annotation(0, 35),
			qualifiedName(1, 27),
			stringLiteral(28, 34),
			packageNode(0, 56, "com.example"),
			qualifiedName(44, 55));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 66),
			annotation(0, 44),
			qualifiedName(1, 17),
			arrayInitializer(18, 43),
			stringLiteral(19, 30),
			stringLiteral(32, 42),
			packageNode(0, 65, "com.example"),
			qualifiedName(53, 64));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 105),
			annotation(0, 11),
			qualifiedName(1, 11),
			lineComment(12, 52),
			annotation(53, 83),
			qualifiedName(54, 70),
			stringLiteral(71, 82),
			packageNode(0, 104, "com.example"),
			qualifiedName(92, 103));

		requireThat(actual, "actual").isEqualTo(expected);
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
