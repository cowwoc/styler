package io.github.cowwoc.styler.formatter.brace.internal;

import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies brace style transformations to source code.
 * <p>
 * <b>Thread-safety</b>: All methods are stateless and thread-safe.
 */
public final class BraceFixer
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private BraceFixer()
	{
	}

	/**
	 * Formats the source code by applying brace style fixes.
	 *
	 * @param context the transformation context
	 * @param config the brace formatting configuration
	 * @return the formatted source code
	 */
	public static String format(TransformationContext context, BraceFormattingConfiguration config)
	{
		String sourceCode = context.sourceCode();
		List<Integer> bracePositions = new ArrayList<>();

		// Find all braces that are not inside strings, characters, or comments
		for (int i = 0; i < sourceCode.length(); ++i)
		{
			context.checkDeadline();

			if (sourceCode.charAt(i) == '{' && !isInLiteralOrComment(sourceCode, i))
			{
				BraceStyle currentStyle = detectCurrentStyle(sourceCode, i);
				BraceStyle expectedStyle = config.braceStyle();

				if (currentStyle != expectedStyle)
					bracePositions.add(i);
			}
		}

		// If no changes needed, return original
		if (bracePositions.isEmpty())
			return sourceCode;

		// Process braces in reverse order to avoid offset issues
		StringBuilder result = new StringBuilder(sourceCode);
		for (int j = bracePositions.size() - 1; j >= 0; --j)
		{
			context.checkDeadline();

			int bracePos = bracePositions.get(j);
			BraceStyle targetStyle = config.braceStyle();

			result = fixBracePosition(result, bracePos, targetStyle);
		}

		return result.toString();
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

		int pos = bracePosition - 1;

		while (pos >= 0 && sourceCode.charAt(pos) == ' ')
			--pos;

		if (pos < 0)
			return BraceStyle.SAME_LINE;

		char prevChar = sourceCode.charAt(pos);

		if (prevChar == '\n')
			return BraceStyle.NEW_LINE;

		return BraceStyle.SAME_LINE;
	}

	/**
	 * Fixes the brace position in the source code.
	 *
	 * @param source the source code string builder
	 * @param bracePos the position of the brace to fix
	 * @param targetStyle the target brace style
	 * @return the modified source code string builder
	 */
	private static StringBuilder fixBracePosition(StringBuilder source, int bracePos,
		BraceStyle targetStyle)
	{
		// Find the position to start removing whitespace
		int removeStart = bracePos - 1;

		// Skip trailing spaces
		while (removeStart >= 0 && source.charAt(removeStart) == ' ')
			--removeStart;

		// Determine if we need to remove a newline before the brace
		if (removeStart >= 0 && source.charAt(removeStart) == '\n')
		{
			if (targetStyle == BraceStyle.SAME_LINE)
			{
				// When converting to SAME_LINE, we need to remove the newline too
				--removeStart;
				// Also skip any indentation before the newline
				while (removeStart >= 0 && source.charAt(removeStart) == ' ')
					--removeStart;
			}
			else
			{
				// For NEW_LINE styles, keep the newline and just adjust removeStart
				++removeStart;
			}
		}

		// Remove whitespace between declaration and brace
		if (removeStart < bracePos - 1)
		{
			source.delete(removeStart + 1, bracePos);
			bracePos = removeStart + 1;
		}

		// Determine what to insert
		String insertStr = switch (targetStyle)
		{
			case SAME_LINE -> " ";
			case NEW_LINE -> "\n";
		};

		source.insert(bracePos, insertStr);

		return source;
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
