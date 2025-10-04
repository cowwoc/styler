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
	 * <p>This method uses single-scan extraction for O(n) performance where n is the
	 * position of the target line. This is significantly more efficient than splitting
	 * the entire file into lines when only one line is needed.
	 *
	 * @param sourceText the source text, never {@code null}
	 * @param lineNumber the line number (1-based), must be positive
	 * @return the line text, or empty string if line number is out of bounds
	 * @throws NullPointerException if {@code sourceText} is {@code null}
	 */
	public static String extractLine(String sourceText, int lineNumber)
	{
		if (lineNumber < 1)
		{
			return "";
		}

		int currentLine = 1;
		int lineStart = 0;

		for (int i = 0; i < sourceText.length(); ++i)
		{
			char c = sourceText.charAt(i);

			if (c == LF || c == CR)
			{
				// Check if we're on the target line
				if (currentLine == lineNumber)
				{
					return sourceText.substring(lineStart, i);
				}

				// Handle CRLF as single line terminator
				if (c == CR && i + 1 < sourceText.length() && sourceText.charAt(i + 1) == LF)
				{
					++i;
				}

				++currentLine;
				lineStart = i + 1;
			}
		}

		// Handle last line (no terminator at end of file)
		if (currentLine == lineNumber)
		{
			return sourceText.substring(lineStart);
		}

		return "";
	}

	/**
	 * Converts a character offset to line and column position (both 1-based).
	 *
	 * <p>This method scans the source text from the beginning to the specified offset,
	 * counting lines and columns. Handles all three line terminator types per JLS §3.4.
	 *
	 * @param sourceText the source text, never {@code null}
	 * @param offset the character offset (0-based), must be {@code >= 0} and {@code <= sourceText.length()}
	 * @return array where [0] is line number (1-based) and [1] is column number (1-based)
	 * @throws NullPointerException if {@code sourceText} is {@code null}
	 * @throws IllegalArgumentException if {@code offset} is negative or exceeds text length
	 */
	public static int[] offsetToPosition(String sourceText, int offset)
	{
		if (offset < 0 || offset > sourceText.length())
		{
			throw new IllegalArgumentException(
				"Offset " + offset + " out of bounds for text length " + sourceText.length());
		}

		int line = 1;
		int column = 1;

		for (int i = 0; i < offset; ++i)
		{
			char c = sourceText.charAt(i);
			if (c == LF)
			{
				++line;
				column = 1;
			}
			else if (c == CR)
			{
				++line;
				column = 1;
				if (i + 1 < sourceText.length() && sourceText.charAt(i + 1) == LF)
				{
					++i;
				}
			}
			else
			{
				++column;
			}
		}

		return new int[]{line, column};
	}

	/**
	 * Calculates the indentation level of a line (number of leading spaces/tabs).
	 *
	 * <p>This method counts leading whitespace, expanding tabs to their equivalent space count.
	 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.2">JLS §3.2</a>,
	 * whitespace includes space (SP) and horizontal tab (HT) characters.
	 *
	 * @param line the source line, never {@code null}
	 * @param tabWidth the number of spaces equivalent to one tab character, must be positive
	 * @return indentation count (spaces, with tabs counted as tabWidth)
	 * @throws NullPointerException if {@code line} is {@code null}
	 * @throws IllegalArgumentException if {@code tabWidth} is not positive
	 */
	public static int getIndentationLevel(String line, int tabWidth)
	{
		if (line == null)
		{
			throw new NullPointerException("line cannot be null");
		}
		if (tabWidth <= 0)
		{
			throw new IllegalArgumentException("tabWidth must be positive, got: " + tabWidth);
		}

		int indent = 0;
		for (int i = 0; i < line.length(); ++i)
		{
			char c = line.charAt(i);
			if (c == ' ')
			{
				++indent;
			}
			else if (c == '\t')
			{
				indent += tabWidth;
			}
			else
			{
				break;
			}
		}
		return indent;
	}

	/**
	 * Converts line and column position (both 1-based) to character offset (0-based).
	 *
	 * <p>This method scans the source text line by line until reaching the specified position.
	 * Handles all three line terminator types per JLS §3.4.
	 *
	 * @param sourceText the source text, never {@code null}
	 * @param line the line number (1-based), must be positive
	 * @param column the column number (1-based), must be positive
	 * @return the character offset (0-based)
	 * @throws NullPointerException if {@code sourceText} is {@code null}
	 * @throws IllegalArgumentException if line or column is invalid
	 */
	public static int positionToOffset(String sourceText, int line, int column)
	{
		if (line < 1)
		{
			throw new IllegalArgumentException("Line must be positive, got: " + line);
		}
		if (column < 1)
		{
			throw new IllegalArgumentException("Column must be positive, got: " + column);
		}

		int currentLine = 1;
		int currentColumn = 1;
		int offset = 0;

		while (offset < sourceText.length())
		{
			if (currentLine == line && currentColumn == column)
			{
				return offset;
			}

			char c = sourceText.charAt(offset);
			if (c == LF)
			{
				++currentLine;
				currentColumn = 1;
				++offset;
			}
			else if (c == CR)
			{
				++currentLine;
				currentColumn = 1;
				++offset;
				if (offset < sourceText.length() && sourceText.charAt(offset) == LF)
				{
					++offset;
				}
			}
			else
			{
				++currentColumn;
				++offset;
			}
		}

		if (currentLine == line && currentColumn == column)
		{
			return offset;
		}

		throw new IllegalArgumentException(
			"Position (" + line + ", " + column + ") not found in source text");
	}
}
