package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for SourceSnippetExtractor functionality.
 * Validates code snippet extraction and formatting capabilities.
 */
public class SourceSnippetExtractorTest
{
	private static final String SAMPLE_CODE =
		"package com.example;\n" +
		"\n" +
		"public class Example {\n" +
		"    public void method() {\n" +
		"        System.out.println(\"Hello, World!\");\n" +
		"    }\n" +
		"}";

	@Test
	public void testExtractSnippetBasic()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(4, 5),
			new SourcePosition(4, 10)
		);

		String snippet = SourceSnippetExtractor.extractSnippet(SAMPLE_CODE, errorRange);

		assertNotNull(snippet);
		assertFalse(snippet.trim().isEmpty());
		assertTrue(snippet.contains("public void method()"));
		assertTrue(snippet.contains("System.out.println"));
		assertTrue(snippet.contains("^")); // Error indicator
	}

	@Test
	public void testExtractSnippetWithContext()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(5, 9),
			new SourcePosition(5, 15)
		);

		String snippet = SourceSnippetExtractor.extractSnippet(SAMPLE_CODE, errorRange, 1);

		assertNotNull(snippet);
		assertTrue(snippet.contains("public void method()"));
		assertTrue(snippet.contains("System.out.println"));
		assertTrue(snippet.contains("}"));
	}

	@Test
	public void testExtractInlineSnippet()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(5, 9),
			new SourcePosition(5, 15)
		);

		String snippet = SourceSnippetExtractor.extractInlineSnippet(SAMPLE_CODE, errorRange);

		assertNotNull(snippet);
		assertTrue(snippet.contains("System.out.println"));
		assertFalse(snippet.contains("^")); // No error indicator for inline
	}

	@Test
	public void testExtractSnippetWithTabs()
	{
		String codeWithTabs = "public class Test {\n\tpublic void method() {\n\t\tint x = 1;\n\t}\n}";
		SourceRange errorRange = new SourceRange(
			new SourcePosition(3, 3),
			new SourcePosition(3, 6)
		);

		String snippet = SourceSnippetExtractor.extractSnippet(codeWithTabs, errorRange);

		assertNotNull(snippet);
		// Tabs should be expanded to spaces
		assertTrue(snippet.contains("    int x = 1")); // Tab expanded to 4 spaces
	}

	@Test
	public void testExtractSnippetEmptyFile()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 1)
		);

		String snippet = SourceSnippetExtractor.extractSnippet("", errorRange);

		assertEquals(snippet.trim(), "(empty file)");
	}

	@Test
	public void testExtractSnippetLineOutOfBounds()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(100, 1),
			new SourcePosition(100, 5)
		);

		String snippet = SourceSnippetExtractor.extractInlineSnippet(SAMPLE_CODE, errorRange);

		assertTrue(snippet.contains("line 100 not found"));
	}

	@Test
	public void testExtractExtendedSnippet()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(4, 5),
			new SourcePosition(5, 10)
		);

		String snippet = SourceSnippetExtractor.extractExtendedSnippet(SAMPLE_CODE, errorRange, 1, 1);

		assertNotNull(snippet);
		assertTrue(snippet.contains("public class Example"));
		assertTrue(snippet.contains("public void method()"));
		assertTrue(snippet.contains("System.out.println"));
		assertTrue(snippet.contains("*")); // Error line marker
	}

	@Test
	public void testLongLinesTruncation()
	{
		String longLine = "a".repeat(150); // Line longer than MAX_LINE_LENGTH
		String codeWithLongLine = "public class Test {\n" + longLine + "\n}";

		SourceRange errorRange = new SourceRange(
			new SourcePosition(2, 1),
			new SourcePosition(2, 5)
		);

		String snippet = SourceSnippetExtractor.extractSnippet(codeWithLongLine, errorRange);

		assertNotNull(snippet);
		assertTrue(snippet.contains("...")); // Truncation indicator
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullSourceTextThrows()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 5)
		);

		SourceSnippetExtractor.extractSnippet(null, errorRange);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullErrorRangeThrows()
	{
		SourceSnippetExtractor.extractSnippet(SAMPLE_CODE, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeContextLinesThrows()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 5)
		);

		SourceSnippetExtractor.extractSnippet(SAMPLE_CODE, errorRange, -1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeBeforeLinesThrows()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 5)
		);

		SourceSnippetExtractor.extractExtendedSnippet(SAMPLE_CODE, errorRange, -1, 1);
	}
}