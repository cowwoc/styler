package io.github.cowwoc.styler.formatter.indentation.internal;

import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationType;
import io.github.cowwoc.styler.formatter.internal.SourceCodeUtils;

/**
 * Applies indentation formatting fixes to source code.
 * <p>
 * Replaces incorrect leading whitespace with correct indentation based on brace depth and
 * configuration.
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
	 * @param config the indentation formatting configuration
	 * @return the formatted source code
	 */
	public static String format(TransformationContext context, IndentationFormattingConfiguration config)
	{
		String sourceCode = context.sourceCode();
		String[] lines = sourceCode.split("\n", -1);
		StringBuilder result = new StringBuilder();

		int depth = 0;
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
				String trimmedLine = line.stripLeading();
				int originalIndentLength = line.length() - trimmedLine.length();
				String originalIndent = line.substring(0, originalIndentLength);

				// Determine if line starts with closing brace
				boolean startsWithCloseBrace = trimmedLine.startsWith("}");

				// Adjust depth for closing brace on current line
				int effectiveDepth = depth;
				if (startsWithCloseBrace)
					effectiveDepth = Math.max(0, depth - 1);

				// Determine if this is a continuation line
				boolean isContinuationLine = prevLineWasContinuation;

				// Build correct leading whitespace based on brace depth or original indentation
				String correctIndentation;
				if (depth > 0 || startsWithCloseBrace || isContinuationLine)
				{
					// Use brace-based indentation for lines with brace context
					correctIndentation = calculateIndentation(effectiveDepth, isContinuationLine, config);
				}
				else
				{
					// For lines without brace context, convert the original indentation
					correctIndentation = convertIndentation(originalIndent, config);
				}

				// Append correctly indented line
				result.append(correctIndentation).append(trimmedLine);

				// Update depth based on braces in this line
				int lineStartPosition = getLineStartPosition(lines, lineIndex);
				depth = calculateDepthAfterLine(sourceCode, lineStartPosition, line, depth);

				// Determine if next line will be a continuation
				prevLineWasContinuation = isContinuationLine(trimmedLine);
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
	 * Converts existing indentation from one format to another.
	 *
	 * @param originalIndent the original indentation string (tabs and/or spaces)
	 * @param config the indentation configuration
	 * @return the converted indentation string
	 */
	private static String convertIndentation(String originalIndent,
		IndentationFormattingConfiguration config)
	{
		// Count the equivalent indent level from original whitespace
		int indentLevel = 0;
		for (char ch : originalIndent.toCharArray())
		{
			if (ch == '\t')
			{
				// Each tab counts as one indent level
				++indentLevel;
			}
			else if (ch == ' ')
			{
				// Spaces accumulate to form indent levels
				// We need to count spaces based on config.indentSize()
				// For simplicity, treat each indentSize spaces as one level
			}
		}

		// For spaces, count how many complete indent levels exist
		int spaceCount = 0;
		for (char ch : originalIndent.toCharArray())
		{
			if (ch == ' ')
				++spaceCount;
		}
		int spaceLevels = spaceCount / config.indentSize();

		// Use the maximum of tab-based or space-based levels
		int totalLevels = Math.max(indentLevel, spaceLevels);

		// Generate the correct indentation in the target format
		if (config.indentationType() == IndentationType.TABS)
		{
			return "\t".repeat(totalLevels);
		}

		return " ".repeat(totalLevels * config.indentSize());
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
		{
			// +1 for the newline character
			position += lines[i].length() + 1;
		}

		return position;
	}

	/**
	 * Calculates the indentation depth after processing braces in the line.
	 *
	 * @param sourceCode the full source code
	 * @param lineStartPosition the position where the line starts
	 * @param line the line content
	 * @param currentDepth the current depth
	 * @return the depth after processing this line
	 */
	private static int calculateDepthAfterLine(String sourceCode, int lineStartPosition, String line,
		int currentDepth)
	{
		int depth = currentDepth;

		for (int i = 0; i < line.length(); ++i)
		{
			int position = lineStartPosition + i;

			// Skip content in literals or comments
			if (SourceCodeUtils.isInLiteralOrComment(sourceCode, position))
				continue;

			char ch = line.charAt(i);
			if (ch == '{')
				++depth;
			else if (ch == '}')
				depth = Math.max(0, depth - 1);
		}

		return depth;
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
