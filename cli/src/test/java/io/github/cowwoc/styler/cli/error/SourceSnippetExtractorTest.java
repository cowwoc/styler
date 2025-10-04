package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

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

	/**
	 * Verifies that extractSnippet() extracts a code snippet with error indicator for a basic error range.
	 */
	@Test
	public void extractSnippetBasic()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(4, 5),
			new SourcePosition(4, 10));

		String snippet = SourceSnippetExtractor.extractSnippet(SAMPLE_CODE, errorRange);

		requireThat(snippet, "snippet").isNotNull();
		requireThat(snippet.isBlank(), "snippetIsBlank").isFalse();
		requireThat(snippet.contains("public void method()"), "snippetContainsMethod").isTrue();
		requireThat(snippet.contains("System.out.println"), "snippetContainsPrintln").isTrue();
		requireThat(snippet.contains("^"), "snippetContainsErrorIndicator").isTrue();
	}

	/**
	 * Verifies that extractSnippet() includes surrounding context lines when requested.
	 */
	@Test
	public void extractSnippetWithContext()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(5, 9),
			new SourcePosition(5, 15));

		String snippet = SourceSnippetExtractor.extractSnippet(SAMPLE_CODE, errorRange, 1);

		requireThat(snippet, "snippet").isNotNull();
		requireThat(snippet.contains("public void method()"), "snippetContainsMethod").isTrue();
		requireThat(snippet.contains("System.out.println"), "snippetContainsPrintln").isTrue();
		requireThat(snippet.contains("}"), "snippetContainsBrace").isTrue();
	}

	/**
	 * Verifies that extractInlineSnippet() extracts code without error indicator markers.
	 */
	@Test
	public void extractInlineSnippet()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(5, 9),
			new SourcePosition(5, 15));

		String snippet = SourceSnippetExtractor.extractInlineSnippet(SAMPLE_CODE, errorRange);

		requireThat(snippet, "snippet").isNotNull();
		requireThat(snippet.contains("System.out.println"), "snippetContainsPrintln").isTrue();
		requireThat(snippet.contains("^"), "snippetContainsErrorIndicator").isFalse();
	}

	/**
	 * Verifies that extractSnippet() correctly expands tabs to spaces in the extracted snippet.
	 */
	@Test
	public void extractSnippetWithTabs()
	{
		String codeWithTabs = "public class Test {\n\tpublic void method() {\n\t\tint x = 1;\n\t}\n}";
		SourceRange errorRange = new SourceRange(
			new SourcePosition(3, 3),
			new SourcePosition(3, 6));

		String snippet = SourceSnippetExtractor.extractSnippet(codeWithTabs, errorRange);

		requireThat(snippet, "snippet").isNotNull();
		// Tabs should be expanded to spaces
		requireThat(snippet.contains("    int x = 1"), "snippetContainsExpandedTab").isTrue();
	}

	/**
	 * Verifies that extractSnippet() handles empty source files by returning an "(empty file)" indicator.
	 */
	@Test
	public void extractSnippetEmptyFile()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 1));

		String snippet = SourceSnippetExtractor.extractSnippet("", errorRange);

		requireThat(snippet.trim(), "snippetTrimmed").isEqualTo("(empty file)");
	}

	/**
	 * Verifies that extractInlineSnippet() returns a "not found" message when the error line is out of bounds.
	 */
	@Test
	public void extractSnippetLineOutOfBounds()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(100, 1),
			new SourcePosition(100, 5));

		String snippet = SourceSnippetExtractor.extractInlineSnippet(SAMPLE_CODE, errorRange);

		requireThat(snippet.contains("line 100 not found"), "snippetContainsNotFound").isTrue();
	}

	/**
	 * Verifies that extractExtendedSnippet() extracts multi-line snippets with configurable context lines.
	 */
	@Test
	public void extractExtendedSnippet()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(4, 5),
			new SourcePosition(5, 10));

		String snippet = SourceSnippetExtractor.extractExtendedSnippet(SAMPLE_CODE, errorRange, 1, 1);

		requireThat(snippet, "snippet").isNotNull();
		requireThat(snippet.contains("public class Example"), "snippetContainsClass").isTrue();
		requireThat(snippet.contains("public void method()"), "snippetContainsMethod").isTrue();
		requireThat(snippet.contains("System.out.println"), "snippetContainsPrintln").isTrue();
		requireThat(snippet.contains("*"), "snippetContainsErrorMarker").isTrue();
	}

	/**
	 * Verifies that extractSnippet() truncates excessively long lines with a truncation indicator.
	 */
	@Test
	public void longLinesTruncation()
	{
		String longLine = "a".repeat(150); // Line longer than MAX_LINE_LENGTH
		String codeWithLongLine = "public class Test {\n" + longLine + "\n}";

		SourceRange errorRange = new SourceRange(
			new SourcePosition(2, 1),
			new SourcePosition(2, 5));

		String snippet = SourceSnippetExtractor.extractSnippet(codeWithLongLine, errorRange);

		requireThat(snippet, "snippet").isNotNull();
		requireThat(snippet.contains("..."), "snippetContainsTruncation").isTrue();
	}

	/**
	 * Verifies that extractSnippet() throws NullPointerException when source text is null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullSourceTextThrows()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 5));

		SourceSnippetExtractor.extractSnippet(null, errorRange);
	}

	/**
	 * Verifies that extractSnippet() throws NullPointerException when error range is null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullErrorRangeThrows()
	{
		SourceSnippetExtractor.extractSnippet(SAMPLE_CODE, null);
	}

	/**
	 * Verifies that extractSnippet() throws IllegalArgumentException when context lines is negative.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void negativeContextLinesThrows()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 5));

		SourceSnippetExtractor.extractSnippet(SAMPLE_CODE, errorRange, -1);
	}

	/**
	 * Verifies that extractExtendedSnippet() throws IllegalArgumentException when before lines is negative.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void negativeBeforeLinesThrows()
	{
		SourceRange errorRange = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 5));

		SourceSnippetExtractor.extractExtendedSnippet(SAMPLE_CODE, errorRange, -1, 1);
	}
}