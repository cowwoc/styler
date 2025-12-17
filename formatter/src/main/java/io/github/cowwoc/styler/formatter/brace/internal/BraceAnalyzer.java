package io.github.cowwoc.styler.formatter.brace.internal;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceStyle;

import java.util.ArrayList;
import java.util.BitSet;
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
	 * @param config  the brace formatting configuration
	 * @return a list of formatting violations (empty if no violations)
	 */
	public static List<FormattingViolation> analyze(TransformationContext context,
		BraceFormattingConfiguration config)
	{
		List<FormattingViolation> violations = new ArrayList<>();
		String sourceCode = context.sourceCode();

		// Get positions inside text and comments from AST
		BitSet textAndComments = context.positionIndex().getTextAndCommentPositions();

		// Scan for braces, skipping text and comments
		for (int i = 0; i < sourceCode.length(); ++i)
		{
			context.checkDeadline();

			if (sourceCode.charAt(i) == '{' && !textAndComments.get(i))
			{
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
}
