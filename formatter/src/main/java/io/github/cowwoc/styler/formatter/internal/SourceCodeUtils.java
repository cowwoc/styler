package io.github.cowwoc.styler.formatter.internal;

/**
 * Utility methods for analyzing source code structure.
 * <p>
 * <b>Thread-safety</b>: All methods are stateless and thread-safe.
 */
public final class SourceCodeUtils
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private SourceCodeUtils()
	{
	}

	/**
	 * Checks if a position is inside a string literal, character literal, or comment.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @return true if position is inside a literal or comment, false otherwise
	 */
	// Suppressed: State machine with 5 interacting boolean flags; decomposition would obscure control flow
	@SuppressWarnings("PMD.NcssCount")
	public static boolean isInLiteralOrComment(String sourceCode, int position)
	{
		boolean inStringLiteral = false;
		boolean inCharLiteral = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;
		boolean inTextBlock = false;

		for (int i = 0; i < position; ++i)
		{
			char current = sourceCode.charAt(i);
			char next;
			if (i + 1 < sourceCode.length())
				next = sourceCode.charAt(i + 1);
			else
				next = '\0';
			char nextNext;
			if (i + 2 < sourceCode.length())
				nextNext = sourceCode.charAt(i + 2);
			else
				nextNext = '\0';

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
					++i;
				}
				continue;
			}

			// Handle text blocks
			if (inTextBlock)
			{
				if (current == '"' && next == '"' && nextNext == '"')
				{
					inTextBlock = false;
					i += 2;
				}
				continue;
			}

			// Handle string literals
			if (inStringLiteral)
			{
				if (current == '\\' && next == '"')
					++i;
				else if (current == '"')
					inStringLiteral = false;
				continue;
			}

			// Handle character literals
			if (inCharLiteral)
			{
				if (current == '\\' && next == '\'')
					++i;
				else if (current == '\'')
					inCharLiteral = false;
				continue;
			}

			// Check for start of literals/comments
			if (current == '"')
			{
				// Check for text block
				if (next == '"' && nextNext == '"')
				{
					inTextBlock = true;
					i += 2;
				}
				else
				{
					inStringLiteral = true;
				}
			}
			else if (current == '\'')
			{
				inCharLiteral = true;
			}
			else if (current == '/' && next == '/')
			{
				inLineComment = true;
				++i;
			}
			else if (current == '/' && next == '*')
			{
				inBlockComment = true;
				++i;
			}
		}

		return inStringLiteral || inCharLiteral || inLineComment || inBlockComment || inTextBlock;
	}
}
