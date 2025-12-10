package io.github.cowwoc.styler.formatter.whitespace.internal;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes source code for whitespace formatting violations.
 * <p>
 * <b>Thread-safety</b>: All methods are stateless and thread-safe.
 */
@SuppressWarnings(
	{
		"PMD.AvoidDeeplyNestedIfStmts",
		"PMD.CollapsibleIfStatements",
		"PMD.NcssCount"
	})
public final class WhitespaceAnalyzer
{
	// Multi-character operators to check before single-character ones
	private static final List<String> MULTI_CHAR_OPERATORS = List.of(
		">>>", ">>=", "<<=",
		">>", "<<",
		"==", "!=", "<=", ">=",
		"&&", "||",
		"+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=",
		"::");

	// Keywords that must have space before opening parenthesis
	private static final List<String> CONTROL_KEYWORDS = List.of(
		"if", "else", "while", "for", "switch", "synchronized", "try", "catch", "do");

	/**
	 * Private constructor to prevent instantiation.
	 */
	private WhitespaceAnalyzer()
	{
	}

	/**
	 * Analyzes the source code for whitespace formatting violations.
	 *
	 * @param context the transformation context
	 * @param config the whitespace formatting configuration
	 * @return a list of formatting violations (empty if no violations)
	 */
	public static List<FormattingViolation> analyze(TransformationContext context,
		WhitespaceFormattingConfiguration config)
	{
		List<FormattingViolation> violations = new ArrayList<>();
		String sourceCode = context.sourceCode();

		for (int i = 0; i < sourceCode.length(); ++i)
		{
			context.checkDeadline();

			// Skip if inside literal or comment
			if (isInLiteralOrComment(sourceCode, i))
				continue;

			char current = sourceCode.charAt(i);

			checkBinaryOperator(sourceCode, i, config, context, violations);
			checkControlKeyword(sourceCode, i, config, context, violations);
			checkComma(sourceCode, i, current, config, context, violations);
			checkParentheses(sourceCode, i, current, context, violations);
		}

		return violations;
	}

	/**
	 * Checks for binary operator spacing violations at the given position.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @param config the whitespace formatting configuration
	 * @param context the transformation context
	 * @param violations the list to add violations to
	 */
	private static void checkBinaryOperator(String sourceCode, int position,
		WhitespaceFormattingConfiguration config, TransformationContext context,
		List<FormattingViolation> violations)
	{
		if (!config.spaceAroundBinaryOperators() || !isBinaryOperator(sourceCode, position))
			return;

		int opStart = getOperatorStart(sourceCode, position);
		int opEnd = opStart + getOperatorLength(sourceCode, opStart);

		boolean spaceBefore = opStart > 0 && sourceCode.charAt(opStart - 1) == ' ';
		boolean spaceAfter = opEnd < sourceCode.length() && sourceCode.charAt(opEnd) == ' ';

		if (!spaceBefore || !spaceAfter)
		{
			String op = sourceCode.substring(opStart, opEnd);
			int lineNumber = context.getLineNumber(opStart);
			int columnNumber = context.getColumnNumber(opStart);

			String message = String.format("Missing space around operator: %s", op);
			violations.add(new DefaultFormattingViolation("whitespace-operator",
				ViolationSeverity.WARNING, message, context.filePath(), opStart, opEnd,
				lineNumber, columnNumber, List.of()));
		}
	}

	/**
	 * Checks for control keyword spacing violations at the given position.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @param config the whitespace formatting configuration
	 * @param context the transformation context
	 * @param violations the list to add violations to
	 */
	private static void checkControlKeyword(String sourceCode, int position,
		WhitespaceFormattingConfiguration config, TransformationContext context,
		List<FormattingViolation> violations)
	{
		if (!config.spaceAfterControlKeywords() || !isControlKeyword(sourceCode, position))
			return;

		int keywordStart = getKeywordStart(sourceCode, position);
		int keywordEnd = getKeywordEnd(sourceCode, keywordStart);

		// Check for opening parenthesis
		int nextNonSpace = keywordEnd;
		while (nextNonSpace < sourceCode.length() && sourceCode.charAt(nextNonSpace) == ' ')
			++nextNonSpace;

		if (nextNonSpace < sourceCode.length() && sourceCode.charAt(nextNonSpace) == '(')
		{
			if (nextNonSpace == keywordEnd || nextNonSpace - keywordEnd != 1)
			{
				String keyword = sourceCode.substring(keywordStart, keywordEnd);
				int lineNumber = context.getLineNumber(keywordStart);
				int columnNumber = context.getColumnNumber(keywordStart);

				String message = String.format("Missing space after keyword: %s", keyword);
				violations.add(new DefaultFormattingViolation("whitespace-keyword",
					ViolationSeverity.WARNING, message, context.filePath(), keywordStart,
					keywordEnd, lineNumber, columnNumber, List.of()));
			}
		}
	}

	/**
	 * Checks for comma spacing violations at the given position.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @param current the character at the position
	 * @param config the whitespace formatting configuration
	 * @param context the transformation context
	 * @param violations the list to add violations to
	 */
	private static void checkComma(String sourceCode, int position, char current,
		WhitespaceFormattingConfiguration config, TransformationContext context,
		List<FormattingViolation> violations)
	{
		if (!config.spaceAfterComma() || current != ',')
			return;

		if (position + 1 < sourceCode.length() && sourceCode.charAt(position + 1) != ' ' &&
			sourceCode.charAt(position + 1) != '\n')
		{
			int lineNumber = context.getLineNumber(position);
			int columnNumber = context.getColumnNumber(position);

			String message = "Missing space after comma";
			violations.add(new DefaultFormattingViolation("whitespace-comma",
				ViolationSeverity.WARNING, message, context.filePath(), position, position + 1,
				lineNumber, columnNumber, List.of()));
		}
	}

	/**
	 * Checks for parentheses spacing violations at the given position.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @param current the character at the position
	 * @param context the transformation context
	 * @param violations the list to add violations to
	 */
	private static void checkParentheses(String sourceCode, int position, char current,
		TransformationContext context, List<FormattingViolation> violations)
	{
		if (current == '(')
		{
			if (position + 1 < sourceCode.length() && sourceCode.charAt(position + 1) == ' ')
			{
				int lineNumber = context.getLineNumber(position);
				int columnNumber = context.getColumnNumber(position);

				String message = "Unexpected space after opening parenthesis";
				violations.add(new DefaultFormattingViolation("whitespace-paren",
					ViolationSeverity.WARNING, message, context.filePath(), position, position + 2,
					lineNumber, columnNumber, List.of()));
			}
		}
		else if (current == ')')
		{
			if (position > 0 && sourceCode.charAt(position - 1) == ' ')
			{
				// Check if previous non-space is opening paren
				int j = position - 2;
				while (j >= 0 && sourceCode.charAt(j) == ' ')
					--j;

				if (j >= 0 && sourceCode.charAt(j) != '(')
				{
					int lineNumber = context.getLineNumber(position);
					int columnNumber = context.getColumnNumber(position);

					String message = "Unexpected space before closing parenthesis";
					violations.add(new DefaultFormattingViolation("whitespace-paren",
						ViolationSeverity.WARNING, message, context.filePath(), position - 1, position,
						lineNumber, columnNumber, List.of()));
				}
			}
		}
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
				{
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

	/**
	 * Checks if the position starts a binary operator.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @return true if position starts a binary operator
	 */
	private static boolean isBinaryOperator(String sourceCode, int position)
	{
		// Check multi-character operators first
		for (String op : MULTI_CHAR_OPERATORS)
		{
			if (position + op.length() <= sourceCode.length() &&
				sourceCode.substring(position, position + op.length()).equals(op))
			{
				// Special case: :: is method reference, not binary operator
				return !op.equals("::");
			}
		}

		// Check single-character operators
		char current = sourceCode.charAt(position);
		if (current == '+' || current == '-' || current == '*' || current == '/' ||
			current == '%' || current == '&' || current == '|' || current == '^' ||
			current == '<' || current == '>' || current == '=')
		{
			// Make sure it's not a unary operator or part of a comment
			if (position > 0)
			{
				char prev = sourceCode.charAt(position - 1);
				if (current == '/' && prev == '/')
					return false;
				if (current == '/' && prev == '*')
					return false;
			}

			return true;
		}

		return false;
	}

	/**
	 * Gets the start position of an operator.
	 *
	 * @param sourceCode the source code string
	 * @param position the position within the operator
	 * @return the start position of the operator
	 */
	private static int getOperatorStart(String sourceCode, int position)
	{
		// For multi-character operators, we need to find the exact start
		for (String op : MULTI_CHAR_OPERATORS)
		{
			if (position >= op.length() - 1)
			{
				int checkPos = position - (op.length() - 1);
				if (checkPos >= 0 && checkPos + op.length() <= sourceCode.length() &&
					sourceCode.substring(checkPos, checkPos + op.length()).equals(op))
				{
					return checkPos;
				}
			}
		}

		return position;
	}

	/**
	 * Gets the length of the operator at the position.
	 *
	 * @param sourceCode the source code string
	 * @param position the start position of the operator
	 * @return the length of the operator
	 */
	private static int getOperatorLength(String sourceCode, int position)
	{
		for (String op : MULTI_CHAR_OPERATORS)
		{
			if (position + op.length() <= sourceCode.length() &&
				sourceCode.substring(position, position + op.length()).equals(op))
			{
				return op.length();
			}
		}

		return 1;
	}

	/**
	 * Checks if position starts a control keyword.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @return true if position starts a control keyword
	 */
	private static boolean isControlKeyword(String sourceCode, int position)
	{
		for (String keyword : CONTROL_KEYWORDS)
		{
			if (position + keyword.length() <= sourceCode.length())
			{
				String candidate = sourceCode.substring(position, position + keyword.length());
				if (candidate.equals(keyword))
				{
					// Make sure it's a word boundary (not part of longer identifier)
					if (position + keyword.length() < sourceCode.length())
					{
						char next = sourceCode.charAt(position + keyword.length());
						if (Character.isLetterOrDigit(next) || next == '_')
							continue;
					}

					if (position > 0)
					{
						char prev = sourceCode.charAt(position - 1);
						if (Character.isLetterOrDigit(prev) || prev == '_')
							continue;
					}

					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Gets the start position of a keyword.
	 *
	 * @param sourceCode the source code string
	 * @param position the position within the keyword
	 * @return the start position of the keyword
	 */
	private static int getKeywordStart(String sourceCode, int position)
	{
		// Find the start of the word
		int start = position;
		while (start > 0 && (Character.isLetterOrDigit(sourceCode.charAt(start - 1)) ||
			sourceCode.charAt(start - 1) == '_'))
		{
			--start;
		}

		return start;
	}

	/**
	 * Gets the end position of a keyword.
	 *
	 * @param sourceCode the source code string
	 * @param start the start position of the keyword
	 * @return the end position (exclusive) of the keyword
	 */
	private static int getKeywordEnd(String sourceCode, int start)
	{
		int end = start;
		while (end < sourceCode.length() &&
			(Character.isLetterOrDigit(sourceCode.charAt(end)) || sourceCode.charAt(end) == '_'))
		{
			++end;
		}

		return end;
	}
}
