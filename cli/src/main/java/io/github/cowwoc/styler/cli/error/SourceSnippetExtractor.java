package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Extracts code snippets with context around error locations for display in error messages.
 * <p>
 * This class provides formatted code context that helps developers quickly identify
 * and understand the source of errors. It handles line extraction, formatting,
 * visual indicators, and tab expansion for clear presentation.
 */
public final class SourceSnippetExtractor
{
	private static final int DEFAULT_CONTEXT_LINES = 2;
	private static final int TAB_WIDTH = 4;
	private static final int MAX_LINE_LENGTH = 120;

	/**
	 * Extracts a formatted code snippet showing the error location with surrounding context.
	 *
	 * @param sourceText the complete source text, never null
	 * @param errorRange the location of the error, never null
	 * @param contextLines the number of lines to show before and after the error
	 * @throws IllegalArgumentException if {@code sourceText} or {@code errorRange} is null,
	 *                                 or if {@code contextLines} is negative
	 * @return a formatted code snippet with line numbers and error indicators, never null
	 */
	public static String extractSnippet(String sourceText, SourceRange errorRange, int contextLines)
	{
		if (sourceText == null)
		{
			throw new IllegalArgumentException("Source text cannot be null");
		}
		if (errorRange == null)
		{
			throw new IllegalArgumentException("Error range cannot be null");
		}
		if (contextLines < 0)
		{
			throw new IllegalArgumentException("Context lines cannot be negative: " + contextLines);
		}

		String[] lines = sourceText.split("\\r?\\n");
		if (lines.length == 0 || sourceText.trim().isEmpty())
		{
			return "(empty file)";
		}

		int errorLine = errorRange.start().line();
		int startLine = Math.max(1, errorLine - contextLines);
		int endLine = Math.min(lines.length, errorLine + contextLines);

		StringBuilder snippet = new StringBuilder();
		int maxLineNumberWidth = String.valueOf(endLine).length();

		for (int lineNumber = startLine; lineNumber <= endLine; lineNumber++)
		{
			String lineContent = getLineContent(lines, lineNumber);
			String expandedContent = expandTabs(lineContent);
			String truncatedContent = truncateLine(expandedContent);

			// Format line number with consistent width
			String lineNumberStr = String.format("%" + maxLineNumberWidth + "d", lineNumber);
			snippet.append(" ").append(lineNumberStr).append(" | ");
			snippet.append(truncatedContent).append("\n");

			// Add error indicator for the error line
			if (lineNumber == errorLine)
			{
				snippet.append(createErrorIndicator(
					maxLineNumberWidth, errorRange, expandedContent));
			}
		}

		return snippet.toString();
	}

	/**
	 * Extracts a code snippet with default context (2 lines before and after).
	 *
	 * @param sourceText the complete source text, never null
	 * @param errorRange the location of the error, never null
	 * @throws IllegalArgumentException if {@code sourceText} or {@code errorRange} is null
	 * @return a formatted code snippet with line numbers and error indicators, never null
	 */
	public static String extractSnippet(String sourceText, SourceRange errorRange)
	{
		return extractSnippet(sourceText, errorRange, DEFAULT_CONTEXT_LINES);
	}

	/**
	 * Creates a compact single-line snippet for inline error display.
	 *
	 * @param sourceText the complete source text, never null
	 * @param errorRange the location of the error, never null
	 * @throws IllegalArgumentException if {@code sourceText} or {@code errorRange} is null
	 * @return a single-line snippet showing just the error line, never null
	 */
	public static String extractInlineSnippet(String sourceText, SourceRange errorRange)
	{
		if (sourceText == null)
		{
			throw new IllegalArgumentException("Source text cannot be null");
		}
		if (errorRange == null)
		{
			throw new IllegalArgumentException("Error range cannot be null");
		}

		String[] lines = sourceText.split("\\r?\\n");
		int errorLine = errorRange.start().line();

		if (errorLine < 1 || errorLine > lines.length)
		{
			return "(line " + errorLine + " not found)";
		}

		String lineContent = lines[errorLine - 1];
		String expandedContent = expandTabs(lineContent);
		return truncateLine(expandedContent.trim());
	}

	/**
	 * Gets the content of a specific line from the source text lines array.
	 */
	private static String getLineContent(String[] lines, int lineNumber)
	{
		if (lineNumber < 1 || lineNumber > lines.length)
		{
			return "";
		}
		return lines[lineNumber - 1];
	}

	/**
	 * Expands tab characters to spaces for consistent display.
	 */
	private static String expandTabs(String line)
	{
		StringBuilder expanded = new StringBuilder();
		for (int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			if (c == '\t')
			{
				int spacesToAdd = TAB_WIDTH - (expanded.length() % TAB_WIDTH);
				expanded.append(" ".repeat(spacesToAdd));
			}
			else
			{
				expanded.append(c);
			}
		}
		return expanded.toString();
	}

	/**
	 * Truncates long lines for readable display.
	 */
	private static String truncateLine(String line)
	{
		if (line.length() <= MAX_LINE_LENGTH)
		{
			return line;
		}
		return line.substring(0, MAX_LINE_LENGTH - 3) + "...";
	}

	/**
	 * Creates visual indicators pointing to the error location.
	 */
	private static String createErrorIndicator(int lineNumberWidth, SourceRange errorRange,
	                                          String expandedLineContent)
	{
		StringBuilder indicator = new StringBuilder();

		// Add spacing to align with line content
		indicator.append(" ".repeat(lineNumberWidth + 3));

		int startColumn = Math.max(1, errorRange.start().column());
		int endColumn = errorRange.end().column();

		// Handle single-line ranges
		if (errorRange.start().line() == errorRange.end().line())
		{
			// Add spaces before the error position
			for (int i = 1; i < startColumn; i++)
			{
				if (i <= expandedLineContent.length() && expandedLineContent.charAt(i - 1) == '\t')
				{
					indicator.append(" ".repeat(TAB_WIDTH));
				}
				else
				{
					indicator.append(" ");
				}
			}

			// Add error indicators
			int indicatorLength = Math.max(1, endColumn - startColumn);
			indicatorLength = Math.min(indicatorLength, MAX_LINE_LENGTH - startColumn + 1);
			indicator.append("^".repeat(indicatorLength));
		}
		else
		{
			// Multi-line range - just point to start
			for (int i = 1; i < startColumn; i++)
			{
				indicator.append(" ");
			}
			indicator.append("^--- error starts here");
		}

		return indicator.append("\n").toString();
	}

	/**
	 * Extracts multiple lines of context for complex error ranges.
	 *
	 * @param sourceText the complete source text, never null
	 * @param errorRange the location of the error, never null
	 * @param beforeLines number of lines to show before the error
	 * @param afterLines number of lines to show after the error
	 * @throws IllegalArgumentException if {@code sourceText} or {@code errorRange} is null,
	 *                                 or if line counts are negative
	 * @return a formatted multi-line snippet with context, never null
	 */
	public static String extractExtendedSnippet(String sourceText, SourceRange errorRange,
	                                           int beforeLines, int afterLines)
	{
		if (sourceText == null)
		{
			throw new IllegalArgumentException("Source text cannot be null");
		}
		if (errorRange == null)
		{
			throw new IllegalArgumentException("Error range cannot be null");
		}
		if (beforeLines < 0)
		{
			throw new IllegalArgumentException("Before lines cannot be negative: " + beforeLines);
		}
		if (afterLines < 0)
		{
			throw new IllegalArgumentException("After lines cannot be negative: " + afterLines);
		}

		String[] lines = sourceText.split("\\r?\\n");
		if (lines.length == 0)
		{
			return "(empty file)";
		}

		int errorStartLine = errorRange.start().line();
		int errorEndLine = errorRange.end().line();
		int startLine = Math.max(1, errorStartLine - beforeLines);
		int endLine = Math.min(lines.length, errorEndLine + afterLines);

		StringBuilder snippet = new StringBuilder();
		int maxLineNumberWidth = String.valueOf(endLine).length();

		for (int lineNumber = startLine; lineNumber <= endLine; lineNumber++)
		{
			String lineContent = getLineContent(lines, lineNumber);
			String expandedContent = expandTabs(lineContent);
			String truncatedContent = truncateLine(expandedContent);

			// Mark error lines with asterisk
			String marker = (lineNumber >= errorStartLine && lineNumber <= errorEndLine) ? "*" : " ";
			String lineNumberStr = String.format("%" + maxLineNumberWidth + "d", lineNumber);
			snippet.append(marker).append(lineNumberStr).append(" | ");
			snippet.append(truncatedContent).append("\n");
		}

		return snippet.toString();
	}
}