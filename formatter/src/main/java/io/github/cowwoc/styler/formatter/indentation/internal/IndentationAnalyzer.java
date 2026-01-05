package io.github.cowwoc.styler.formatter.indentation.internal;

import io.github.cowwoc.styler.formatter.AstPositionIndex;
import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationType;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * Analyzes source code for indentation violations.
 * <p>
 * Determines expected indentation level based on nesting depth and compares with actual leading
 * whitespace on each line.
 * <p>
 * <b>Thread-safety</b>: All methods are stateless and thread-safe.
 */
public final class IndentationAnalyzer
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private IndentationAnalyzer()
	{
	}

	/**
	 * Analyzes the source code for indentation violations.
	 *
	 * @param context the transformation context
	 * @param config  the indentation formatting configuration
	 * @return a list of formatting violations (empty if no violations)
	 */
	public static List<FormattingViolation> analyze(TransformationContext context,
		IndentationFormattingConfiguration config)
	{
		List<FormattingViolation> violations = new ArrayList<>();
		String[] lines = context.sourceCode().split("\n", -1);
		AstPositionIndex positionIndex = context.positionIndex();

		boolean prevLineWasContinuation = false;

		for (int lineIndex = 0; lineIndex < lines.length; ++lineIndex)
		{
			context.checkDeadline();

			String line = lines[lineIndex];

			// Skip empty lines
			if (line.isBlank())
			{
				prevLineWasContinuation = false;
				continue;
			}

			int lineStartPosition = getLineStartPosition(lines, lineIndex);

			// Extract leading whitespace
			int leadingWhitespaceEnd = findLeadingWhitespaceEnd(line);
			String leadingWhitespace = line.substring(0, leadingWhitespaceEnd);

			assert that(leadingWhitespaceEnd, "leadingWhitespaceEnd").isLessThan(line.length()).elseThrow();

			int codePosition = lineStartPosition + leadingWhitespaceEnd;
			int depth = positionIndex.getDepth(codePosition);

			// Determine if this is a continuation line
			boolean isContinuationLine = prevLineWasContinuation;

			// Calculate expected indentation
			String expectedIndentation = calculateExpectedIndentation(depth, isContinuationLine, config);

			// Check for mixed tabs and spaces
			boolean hasTabs = leadingWhitespace.contains("\t");
			boolean hasSpaces = leadingWhitespace.contains(" ");
			if (hasTabs && hasSpaces)
			{
				int lineNumber = context.getLineNumber(lineStartPosition);
				int columnNumber = context.getColumnNumber(lineStartPosition);

				String message = "Mixed tabs and spaces in leading whitespace";
				violations.add(new DefaultFormattingViolation("indentation",
					ViolationSeverity.WARNING, message, context.filePath(), lineStartPosition,
					lineStartPosition + leadingWhitespaceEnd, lineNumber, columnNumber, List.of()));
			}
			// Check for incorrect indentation
			else if (!leadingWhitespace.equals(expectedIndentation))
			{
				int lineNumber = context.getLineNumber(lineStartPosition);
				int columnNumber = context.getColumnNumber(lineStartPosition);

				String unitName;
				if (config.indentationType() == IndentationType.TABS)
					unitName = "tab(s)";
				else
					unitName = "spaces";

				String message = String.format(
					"Incorrect indentation: expected %d %s but found %d characters",
					depth,
					unitName,
					leadingWhitespace.length());

				violations.add(new DefaultFormattingViolation("indentation",
					ViolationSeverity.WARNING, message, context.filePath(), lineStartPosition,
					lineStartPosition + leadingWhitespaceEnd, lineNumber, columnNumber, List.of()));
			}

			// Determine if next line will be a continuation
			prevLineWasContinuation = isContinuationLine(line);
		}

		return violations;
	}

	/**
	 * Finds the position after leading whitespace ends.
	 *
	 * @param line the line to check
	 * @return the index of the first non-whitespace character
	 */
	private static int findLeadingWhitespaceEnd(String line)
	{
		int i = 0;
		while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t'))
			++i;
		return i;
	}

	/**
	 * Calculates the expected indentation string for a given depth.
	 *
	 * @param depth the indentation depth
	 * @param isContinuation whether this is a continuation line
	 * @param config the indentation configuration
	 * @return the expected leading whitespace string
	 */
	private static String calculateExpectedIndentation(int depth, boolean isContinuation,
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
	 * @param line the line to check
	 * @return true if the next line should be treated as a continuation
	 */
	private static boolean isContinuationLine(String line)
	{
		String stripped = line.strip();

		// Empty lines don't create continuations
		if (stripped.isEmpty())
			return false;

		// Lines ending with these characters suggest continuation
		char lastChar = stripped.charAt(stripped.length() - 1);
		return switch (lastChar)
		{
			case ',', '(', '+', '-', '*', '/', '%', '&', '|', '^', '=', '<', '>' -> true;
			default -> false;
		};
	}
}
