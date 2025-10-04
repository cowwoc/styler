package io.github.cowwoc.styler.formatter.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for source text processing.
 * <p>
 * This class provides common operations for analyzing and manipulating source code text,
 * including line extraction and splitting operations that properly handle all line
 * terminator types defined in the Java Language Specification.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.4">JLS §3.4</a>,
 * line terminators are defined as CR, LF, or CRLF sequences.
 * <p>
 * <strong>Note:</strong> This class is made public to enable comprehensive unit testing
 * of line handling logic, despite being an implementation detail.
 */
public final class SourceTextUtil
{
	private static final char LF = '\n';
	private static final char CR = '\r';

	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private SourceTextUtil()
	{
		throw new AssertionError("Utility class should not be instantiated");
	}

	/**
	 * Splits source text into individual lines, preserving line structure.
	 * <p>
	 * This method handles all three line terminator types defined in the JLS:
	 * LF (\n), CR (\r), and CRLF (\r\n). Line terminators are removed from
	 * the returned strings.
	 *
	 * @param text the text to split into lines, never {@code null}
	 * @return array of lines without line terminators, never {@code null}
	 * @throws NullPointerException if {@code text} is {@code null}
	 */
	public static String[] splitIntoLines(String text)
	{
		List<String> lines = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder();

		for (int i = 0; i < text.length(); i += 1)
		{
			char c = text.charAt(i);

			if (c == LF)
			{
				lines.add(currentLine.toString());
				currentLine = new StringBuilder();
			}
			else if (c == CR)
			{
				lines.add(currentLine.toString());
				currentLine = new StringBuilder();

				if (i + 1 < text.length() && text.charAt(i + 1) == LF)
				{
					i += 1;
				}
			}
			else
			{
				currentLine.append(c);
			}
		}

		if (!currentLine.isEmpty() || text.isEmpty())
		{
			lines.add(currentLine.toString());
		}

		return lines.toArray(new String[0]);
	}

	/**
	 * Extracts a single line from source text by line number (1-based).
	 *
	 * @param sourceText the source text, never {@code null}
	 * @param lineNumber the line number (1-based), must be positive
	 * @return the line text, or empty string if line number is out of bounds
	 * @throws NullPointerException if {@code sourceText} is {@code null}
	 */
	public static String extractLine(String sourceText, int lineNumber)
	{
		String[] lines = splitIntoLines(sourceText);
		int lineIndex = lineNumber - 1;
		if (lineIndex >= 0 && lineIndex < lines.length)
		{
			return lines[lineIndex];
		}
		return "";
	}
}
