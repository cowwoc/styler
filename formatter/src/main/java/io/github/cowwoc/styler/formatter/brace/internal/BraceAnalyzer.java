package io.github.cowwoc.styler.formatter.brace.internal;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes source code for brace style violations.
 * <p>
 * <b>Thread-safety</b>: All methods are stateless and thread-safe.
 */
public final class BraceAnalyzer
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private BraceAnalyzer()
	{
	}

	/**
	 * Analyzes the source code for brace style violations.
	 *
	 * @param context the transformation context
	 * @param config the brace formatting configuration
	 * @return a list of formatting violations (empty if no violations)
	 */
	public static List<FormattingViolation> analyze(TransformationContext context,
		BraceFormattingConfiguration config)
	{
		List<FormattingViolation> violations = new ArrayList<>();
		String sourceCode = context.sourceCode();

		// Find all opening braces that are not inside strings, characters, or comments
		for (int i = 0; i < sourceCode.length(); ++i)
		{
			context.checkDeadline();

			if (sourceCode.charAt(i) == '{' && !isInLiteralOrComment(sourceCode, i))
			{
				// Detect current style
				BraceStyle currentStyle = detectCurrentStyle(sourceCode, i);
				BraceStyle expectedStyle = config.braceStyle();

				if (currentStyle != expectedStyle)
				{
					int lineNumber = context.getLineNumber(i);
					int columnNumber = context.getColumnNumber(i);

					String message = String.format("Brace style mismatch: expected %s but found %s",
						expectedStyle, currentStyle);

					FormattingViolation violation = new DefaultFormattingViolation("brace-style",
						ViolationSeverity.WARNING, message, context.filePath(), i, i + 1, lineNumber,
						columnNumber, List.of());

					violations.add(violation);
				}
			}
		}

		return violations;
	}

	/**
	 * Detects the current brace style at a position.
	 *
	 * @param sourceCode the source code string
	 * @param bracePosition the position of the opening brace
	 * @return the detected brace style
	 */
	private static BraceStyle detectCurrentStyle(String sourceCode, int bracePosition)
	{
		if (bracePosition <= 0)
			return BraceStyle.SAME_LINE;

		// Look back from brace to find preceding character
		int pos = bracePosition - 1;

		// Skip spaces but not newlines
		while (pos >= 0 && sourceCode.charAt(pos) == ' ')
			--pos;

		if (pos < 0)
			return BraceStyle.SAME_LINE;

		char prevChar = sourceCode.charAt(pos);

		// If preceding character is newline, it's NEW_LINE (Allman style)
		if (prevChar == '\n')
			return BraceStyle.NEW_LINE;

		// Otherwise, brace is on same line as declaration
		return BraceStyle.SAME_LINE;
	}

	/**
	 * Checks if a position is inside a string literal, character literal, or comment.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @return true if position is inside a literal or comment, false otherwise
	 */
	private static boolean isInLiteralOrComment(String sourceCode, int position)
	{
		// Scan from the beginning to the position, tracking state
		boolean inStringLiteral = false;
		boolean inCharLiteral = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;

		for (int i = 0; i < position; ++i)
		{
			char current = sourceCode.charAt(i);
			char next;
			if (i + 1 < sourceCode.length())
				next = sourceCode.charAt(i + 1);
			else
				next = '\0';

			// Handle line comments
			if (inLineComment)
			{
				if (current == '\n')
					inLineComment = false;
				continue;
			}

			// Handle block comments
			if (inBlockComment)
			{
				if (current == '*' && next == '/')
				{
					inBlockComment = false;
					// Skip the closing '/'
					++i;
				}
				continue;
			}

			// Handle string literals
			if (inStringLiteral)
			{
				if (current == '\\' && next == '"')
				{
					// Skip escaped quote
					++i;
				}
				else if (current == '"')
				{
					inStringLiteral = false;
				}
				continue;
			}

			// Handle character literals
			if (inCharLiteral)
			{
				if (current == '\\' && next == '\'')
				{
					// Skip escaped quote
					++i;
				}
				else if (current == '\'')
				{
					inCharLiteral = false;
				}
				continue;
			}

			// Check for start of literals/comments
			if (current == '"')
			{
				inStringLiteral = true;
			}
			else if (current == '\'')
			{
				inCharLiteral = true;
			}
			else if (current == '/' && next == '/')
			{
				inLineComment = true;
				// Skip second '/'
				++i;
			}
			else if (current == '/' && next == '*')
			{
				inBlockComment = true;
				// Skip '*'
				++i;
			}
		}

		// Return true if we're in any kind of literal or comment at the target position
		return inStringLiteral || inCharLiteral || inLineComment || inBlockComment;
	}
}
