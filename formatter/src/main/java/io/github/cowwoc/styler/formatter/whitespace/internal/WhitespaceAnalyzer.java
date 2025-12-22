package io.github.cowwoc.styler.formatter.whitespace.internal;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Analyzes source code for whitespace formatting violations.
 * <p>
 * <b>Thread-safety</b>: All methods are stateless and thread-safe.
 */
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
	 * @param config  the whitespace formatting configuration
	 * @return a list of formatting violations (empty if no violations)
	 */
	public static List<FormattingViolation> analyze(TransformationContext context,
		WhitespaceFormattingConfiguration config)
	{
		List<FormattingViolation> violations = new ArrayList<>();
		String sourceCode = context.sourceCode();

		// Build exclusion set from AST (text and comments)
		BitSet textAndComments = context.positionIndex().getTextAndCommentPositions();

		for (int i = 0; i < sourceCode.length(); ++i)
		{
			context.checkDeadline();

			// Skip if inside text or comment
			if (textAndComments.get(i))
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
		if (!config.spaceAroundBinaryOperator() || !isBinaryOperator(sourceCode, position))
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
		if (!config.spaceAfterControlKeyword() || !isControlKeyword(sourceCode, position))
			return;

		int keywordStart = getKeywordStart(sourceCode, position);
		int keywordEnd = getKeywordEnd(sourceCode, keywordStart);

		// Check for opening parenthesis
		int nextNonSpace = keywordEnd;
		while (nextNonSpace < sourceCode.length() && sourceCode.charAt(nextNonSpace) == ' ')
			++nextNonSpace;

		// Must be followed by '(' and must have exactly one space between keyword and '('
		boolean hasOpenParen = nextNonSpace < sourceCode.length() && sourceCode.charAt(nextNonSpace) == '(';
		boolean hasWrongSpacing = nextNonSpace == keywordEnd || nextNonSpace - keywordEnd != 1;

		if (hasOpenParen && hasWrongSpacing)
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
			checkOpeningParenthesis(sourceCode, position, context, violations);
		else if (current == ')')
			checkClosingParenthesis(sourceCode, position, context, violations);
	}

	/**
	 * Checks for space after opening parenthesis.
	 *
	 * @param sourceCode the source code string
	 * @param position the position of the opening parenthesis
	 * @param context the transformation context
	 * @param violations the list to add violations to
	 */
	private static void checkOpeningParenthesis(String sourceCode, int position,
		TransformationContext context, List<FormattingViolation> violations)
	{
		if (position + 1 >= sourceCode.length() || sourceCode.charAt(position + 1) != ' ')
			return;

		int lineNumber = context.getLineNumber(position);
		int columnNumber = context.getColumnNumber(position);

		String message = "Unexpected space after opening parenthesis";
		violations.add(new DefaultFormattingViolation("whitespace-paren",
			ViolationSeverity.WARNING, message, context.filePath(), position, position + 2,
			lineNumber, columnNumber, List.of()));
	}

	/**
	 * Checks for space before closing parenthesis.
	 *
	 * @param sourceCode the source code string
	 * @param position the position of the closing parenthesis
	 * @param context the transformation context
	 * @param violations the list to add violations to
	 */
	private static void checkClosingParenthesis(String sourceCode, int position,
		TransformationContext context, List<FormattingViolation> violations)
	{
		if (position <= 0 || sourceCode.charAt(position - 1) != ' ')
			return;

		// Check if previous non-space is opening paren (empty parens with space is ok)
		int j = position - 2;
		while (j >= 0 && sourceCode.charAt(j) == ' ')
			--j;

		if (j < 0 || sourceCode.charAt(j) == '(')
			return;

		int lineNumber = context.getLineNumber(position);
		int columnNumber = context.getColumnNumber(position);

		String message = "Unexpected space before closing parenthesis";
		violations.add(new DefaultFormattingViolation("whitespace-paren",
			ViolationSeverity.WARNING, message, context.filePath(), position - 1, position,
			lineNumber, columnNumber, List.of()));
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
		if (!isSingleCharOperator(current))
			return false;

		// Make sure it's not part of a comment
		return position <= 0 || !isPartOfComment(current, sourceCode.charAt(position - 1));
	}

	/**
	 * Checks if a character is a single-character binary operator.
	 *
	 * @param ch the character to check
	 * @return true if it's a single-character operator
	 */
	private static boolean isSingleCharOperator(char ch)
	{
		return switch (ch)
		{
			case '+', '-', '*', '/', '%', '&', '|', '^', '<', '>', '=' -> true;
			default -> false;
		};
	}

	/**
	 * Checks if the current character is part of a comment delimiter.
	 *
	 * @param current the current character
	 * @param prev the previous character
	 * @return true if this is part of a comment (// or end of block comment)
	 */
	private static boolean isPartOfComment(char current, char prev)
	{
		// "//" is a line comment, "*/" ends a block comment
		return (current == '/' && prev == '/') || (current == '/' && prev == '*');
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
			if (matchesKeywordAt(sourceCode, position, keyword))
				return true;
		}
		return false;
	}

	/**
	 * Checks if a keyword matches at the given position with word boundaries.
	 *
	 * @param sourceCode the source code string
	 * @param position the position to check
	 * @param keyword the keyword to match
	 * @return true if the keyword matches at this position
	 */
	private static boolean matchesKeywordAt(String sourceCode, int position, String keyword)
	{
		if (position + keyword.length() > sourceCode.length())
			return false;

		String candidate = sourceCode.substring(position, position + keyword.length());
		if (!candidate.equals(keyword))
			return false;

		// Check word boundary after keyword
		if (position + keyword.length() < sourceCode.length())
		{
			char next = sourceCode.charAt(position + keyword.length());
			if (Character.isLetterOrDigit(next) || next == '_')
				return false;
		}

		// Check word boundary before keyword
		if (position > 0)
		{
			char prev = sourceCode.charAt(position - 1);
			if (Character.isLetterOrDigit(prev) || prev == '_')
				return false;
		}

		return true;
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
