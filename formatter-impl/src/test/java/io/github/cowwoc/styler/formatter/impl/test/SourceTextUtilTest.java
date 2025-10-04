package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.formatter.impl.SourceTextUtil;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SourceTextUtil} line handling functionality.
 * <p>
 * These tests verify JLS §3.4 compliance for line terminator handling (LF, CR, CRLF)
 * and ensure correct line extraction behavior.
 */
public final class SourceTextUtilTest
{
	/**
	 * Verifies splitIntoLines() correctly handles Unix-style LF (\n) line terminators
	 * per JLS §3.4.
	 */
	@Test
	public void splitIntoLinesHandlesLFTerminators()
	{
		String source = "line1\nline2\nline3";

		String[] lines = SourceTextUtil.splitIntoLines(source);

		assertThat(lines).containsExactly("line1", "line2", "line3");
	}

	/**
	 * Verifies splitIntoLines() correctly handles old Mac-style CR (\r) line terminators
	 * per JLS §3.4.
	 */
	@Test
	public void splitIntoLinesHandlesCRTerminators()
	{
		String source = "line1\rline2\rline3";

		String[] lines = SourceTextUtil.splitIntoLines(source);

		assertThat(lines).containsExactly("line1", "line2", "line3");
	}

	/**
	 * Verifies splitIntoLines() correctly handles Windows-style CRLF (\r\n) line terminators
	 * per JLS §3.4, treating CR+LF as a single line terminator.
	 */
	@Test
	public void splitIntoLinesHandlesCRLFTerminators()
	{
		String source = "line1\r\nline2\r\nline3";

		String[] lines = SourceTextUtil.splitIntoLines(source);

		assertThat(lines).containsExactly("line1", "line2", "line3");
	}

	/**
	 * Verifies splitIntoLines() correctly handles mixed line terminators in the same source text.
	 */
	@Test
	public void splitIntoLinesHandlesMixedTerminators()
	{
		String source = "line1\nline2\r\nline3\rline4";

		String[] lines = SourceTextUtil.splitIntoLines(source);

		assertThat(lines).containsExactly("line1", "line2", "line3", "line4");
	}

	/**
	 * Verifies splitIntoLines() handles empty source text as a single empty line.
	 */
	@Test
	public void splitIntoLinesWithEmptyText()
	{
		String source = "";

		String[] lines = SourceTextUtil.splitIntoLines(source);

		assertThat(lines).containsExactly("");
	}

	/**
	 * Verifies splitIntoLines() does not include an empty trailing line when source
	 * text ends with a line terminator (standard text processing behavior).
	 */
	@Test
	public void splitIntoLinesWithTrailingTerminator()
	{
		String source = "line1\nline2\n";

		String[] lines = SourceTextUtil.splitIntoLines(source);

		// Trailing terminator ends the last line, no empty line follows
		assertThat(lines).containsExactly("line1", "line2");
	}

	/**
	 * Verifies extractLine() returns the correct line using 1-based indexing
	 * as required by the formatting rule contract.
	 */
	@Test
	public void extractLineWithValidLineNumber()
	{
		String source = "first\nsecond\nthird";

		String line2 = SourceTextUtil.extractLine(source, 2);

		assertThat(line2).isEqualTo("second");
	}

	/**
	 * Verifies extractLine() returns an empty string when the line number is out of bounds
	 * rather than throwing an exception.
	 */
	@Test
	public void extractLineWithInvalidLineNumber()
	{
		String source = "first\nsecond";

		String line10 = SourceTextUtil.extractLine(source, 10);

		assertThat(line10).isEmpty();
	}
}
