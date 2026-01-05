package io.github.cowwoc.styler.formatter.indentation.internal;

import io.github.cowwoc.styler.formatter.AstPositionIndex;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationType;

/**
 * Applies indentation formatting fixes to source code.
 * <p>
 * Replaces incorrect leading whitespace with correct indentation based on nesting depth.
 * <p>
 * <b>Thread-safety</b>: All methods are stateless and thread-safe.
 */
public final class IndentationFixer
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private IndentationFixer()
	{
	}

	/**
	 * Formats the source code by applying indentation fixes.
	 *
	 * @param context the transformation context
	 * @param config  the indentation formatting configuration
	 * @return the formatted source code
	 */
	public static String format(TransformationContext context, IndentationFormattingConfiguration config)
	{
		String[] lines = context.sourceCode().split("\n", -1);
		StringBuilder result = new StringBuilder();
		AstPositionIndex positionIndex = context.positionIndex();

		boolean prevLineWasContinuation = false;

		for (int lineIndex = 0; lineIndex < lines.length; ++lineIndex)
		{
			context.checkDeadline();

			String line = lines[lineIndex];

			// For empty lines, preserve them as-is
			if (line.isBlank())
			{
				result.append(line);
				prevLineWasContinuation = false;
			}
			else
			{
				// Extract existing leading whitespace and content
				String strippedLine = line.stripLeading();
				int originalIndentLength = line.length() - strippedLine.length();

				int lineStartPosition = getLineStartPosition(lines, lineIndex);
				int codePosition = lineStartPosition + originalIndentLength;

				int depth = positionIndex.getDepth(codePosition);

				boolean isContinuationLine = prevLineWasContinuation;
				String correctIndentation = calculateIndentation(depth, isContinuationLine, config);

				// Append correctly indented line
				result.append(correctIndentation).append(strippedLine);

				// Determine if next line will be a continuation
				prevLineWasContinuation = isContinuationLine(strippedLine);
			}

			// Add newline after each line except the last
			if (lineIndex < lines.length - 1)
				result.append('\n');
		}

		return result.toString();
	}

	/**
	 * Calculates the indentation string for a given depth.
	 *
	 * @param depth the indentation depth
	 * @param isContinuation whether this is a continuation line
	 * @param config the indentation configuration
	 * @return the indentation string
	 */
	private static String calculateIndentation(int depth, boolean isContinuation,
		IndentationFormattingConfiguration config)
	{
		int totalIndent = depth;
		if (isContinuation)
			++totalIndent;

		if (config.indentationType() == IndentationType.TABS)
		{
			return "\t".repeat(totalIndent);
		}

		int spaces = totalIndent * config.indentSize();
		return " ".repeat(spaces);
	}

	/**
	 * Gets the position in the source code where a line starts.
	 *
	 * @param lines all lines in the source code
	 * @param lineIndex the index of the line
	 * @return the position in the source code
	 */
	private static int getLineStartPosition(String[] lines, int lineIndex)
	{
		int position = 0;
		for (int i = 0; i < lineIndex; ++i)
			// +1 for the newline character
			position += lines[i].length() + 1;

		return position;
	}

	/**
	 * Determines if a line is a continuation line by checking if it ends with certain patterns.
	 *
	 * @param line the trimmed line to check
	 * @return true if the next line should be treated as a continuation
	 */
	private static boolean isContinuationLine(String line)
	{
		// Empty lines don't create continuations
		if (line.isEmpty())
			return false;

		// Lines ending with these characters suggest continuation
		char lastChar = line.charAt(line.length() - 1);
		return switch (lastChar)
		{
			case ',', '(', '+', '-', '*', '/', '%', '&', '|', '^', '=', '<', '>' -> true;
			default -> false;
		};
	}
}
