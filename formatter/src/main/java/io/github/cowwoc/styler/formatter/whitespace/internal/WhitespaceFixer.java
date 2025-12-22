package io.github.cowwoc.styler.formatter.whitespace.internal;

import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;

import java.util.BitSet;
import java.util.List;

/**
 * Applies whitespace formatting fixes to source code.
 * <p>
 * <b>Thread-safety</b>: All methods are stateless and thread-safe.
 */
@SuppressWarnings(
	{
		"PMD.AvoidDeeplyNestedIfStmts",
		"PMD.CollapsibleIfStatements"
	})
public final class WhitespaceFixer
{
	// Binary operators that require spaces on both sides
	private static final List<String> BINARY_OPERATORS = List.of(
		">>>", ">>=", "<<=",
		">>", "<<",
		"==", "!=", "<=", ">=",
		"&&", "||",
		"+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=",
		"->");

	// Operators that should NEVER have spaces added (unary/compound)
	private static final List<String> NO_SPACE_OPERATORS = List.of("::", "++", "--");

	private static final List<String> CONTROL_KEYWORDS = List.of(
		"if", "else", "while", "for", "switch", "synchronized", "try", "catch", "do");


	/**
	 * Private constructor to prevent instantiation.
	 */
	private WhitespaceFixer()
	{
	}

	/**
	 * Formats the source code by applying whitespace fixes.
	 *
	 * @param context the transformation context
	 * @param config the whitespace formatting configuration
	 * @return the formatted source code
	 */
	public static String format(TransformationContext context,
		WhitespaceFormattingConfiguration config)
	{
		String sourceCode = context.sourceCode();
		StringBuilder result = new StringBuilder(sourceCode);
		BitSet textAndComments = context.positionIndex().getTextAndCommentPositions();

		// Process from end to beginning to preserve offsets
		for (int i = result.length() - 1; i >= 0; --i)
		{
			context.checkDeadline();

			// Cache string representation for this iteration (invalidated by continue after modifications)
			String str = result.toString();
			char current = str.charAt(i);

			// Skip positions inside text or comments
			if (textAndComments.get(i))
				continue;

			// Fix increment/decrement operator spacing (must be before binary operators)
			if ((current == '+' || current == '-') && i + 1 < str.length() && str.charAt(i + 1) == current)
			{
				fixIncrementDecrementSpacing(result, i);
				continue;
			}

			// Fix logical NOT spacing (no space after !)
			if (current == '!')
			{
				fixLogicalNotSpacing(result, i);
				continue;
			}

			// Fix binary operator spacing (skip generic angle brackets)
			if (isBinaryOperatorStart(str, i))
			{
				// Check if this is a generic angle bracket, not a comparison operator
				if ((current == '<' || current == '>') && isGenericBracket(str, i))
					continue;

				// For = operator, check assignment operator config
				if (current == '=' && !config.spaceAroundAssignmentOperators())
					continue;

				// For other operators, check binary operators config
				if (current != '=' && !config.spaceAroundBinaryOperator())
					continue;

				fixBinaryOperatorSpacing(result, i, config);
				continue;
			}

			// Fix control keyword spacing
			if (config.spaceAfterControlKeyword() && isControlKeywordStart(str, i))
				fixControlKeywordSpacing(result, i);

			// Fix "else if" - ensure space between them
			if (isElseIfSequence(str, i))
				fixElseIfSpacing(result, i);

			// Fix "new" keyword spacing - ensure exactly one space after
			if (isNewKeywordStart(str, i))
				fixNewKeywordSpacing(result, i);

			// Fix comma spacing (remove space before, add space after)
			if (current == ',')
			{
				// Always remove space before comma
				int originalLength = result.length();
				fixSpaceBeforeComma(result, i);
				int removedSpaces = originalLength - result.length();
				// Adjust position for removed spaces
				i -= removedSpaces;

				// Add space after comma if config says so
				if (config.spaceAfterComma())
					fixCommaSpacing(result, i);
			}

			// Fix parentheses spacing
			if (current == '(' || current == ')')
				fixParenthesesSpacing(result, i);

			// Fix bracket spacing
			if (current == '[' || current == ']')
				fixBracketSpacing(result, i);

			// Fix method reference spacing
			if (config.noSpaceAroundMethodReference() && current == ':' &&
				i + 1 < result.length() && result.charAt(i + 1) == ':')
			{
				fixMethodReferenceSpacing(result, i);
			}

			// Fix semicolon spacing
			if (current == ';')
				fixSemicolonSpacing(result, i);
		}

		return result.toString();
	}

	/**
	 * Fixes spacing around binary operators.
	 *
	 * @param source the source code string builder
	 * @param position the position of the operator
	 * @param config the configuration
	 */
	private static void fixBinaryOperatorSpacing(StringBuilder source, int position,
		WhitespaceFormattingConfiguration config)
	{
		int opStart = getOperatorStart(source.toString(), position);
		int opEnd = opStart + getOperatorLength(source.toString(), opStart);

		String op = source.substring(opStart, opEnd);

		// Special handling for specific operators
		if (op.equals("::"))
		{
			// Method reference - remove spaces
			if (config.noSpaceAroundMethodReference())
			{
				removeSpacesAround(source, opStart, opEnd);
			}

			return;
		}

		if (op.equals("->"))
		{
			// Lambda arrow
			if (config.spaceAroundArrowInLambda())
			{
				normalizeSpacesAround(source, opStart, opEnd, " ", " ");
			}

			return;
		}

		if (op.equals(":"))
		{
			// Check if this is a switch case colon - should have no space before
			if (isSwitchCaseColon(source.toString(), opStart))
			{
				// Remove space before the colon in switch case
				while (opStart > 0 && source.charAt(opStart - 1) == ' ')
				{
					source.deleteCharAt(opStart - 1);
					--opStart;
				}
				return;
			}

			// For ternary or enhanced for, add spaces around
			normalizeSpacesAround(source, opStart, opEnd, " ", " ");

			return;
		}

		if (op.equals("?"))
		{
			// Ternary question mark
			normalizeSpacesAround(source, opStart, opEnd, " ", " ");

			return;
		}

		// Standard binary operators
		normalizeSpacesAround(source, opStart, opEnd, " ", " ");
	}

	/**
	 * Fixes spacing after control keywords.
	 *
	 * @param source the source code string builder
	 * @param position the position of the keyword
	 */
	private static void fixControlKeywordSpacing(StringBuilder source, int position)
	{
		int keywordStart = getKeywordStart(source.toString(), position);
		int keywordEnd = getKeywordEnd(source.toString(), keywordStart);

		// Find next non-space character
		int nextNonSpace = keywordEnd;
		while (nextNonSpace < source.length() && source.charAt(nextNonSpace) == ' ')
			++nextNonSpace;

		// Only normalize spacing if followed by '(' (control keyword before parenthesis)
		if (nextNonSpace < source.length() && source.charAt(nextNonSpace) == '(')
		{
			// Remove all spaces between keyword and paren
			while (keywordEnd < source.length() && source.charAt(keywordEnd) == ' ')
				source.deleteCharAt(keywordEnd);
			// Add exactly one space
			source.insert(keywordEnd, ' ');
		}
	}

	/**
	 * Fixes spacing after commas.
	 *
	 * @param source the source code string builder
	 * @param position the position of the comma
	 */
	private static void fixCommaSpacing(StringBuilder source, int position)
	{
		// Check if already has space after
		if (position + 1 >= source.length())
			return;

		char after = source.charAt(position + 1);

		if (after == ' ')
		{
			// Already has space, normalize to single space
			int extraSpaces = 1;
			while (position + 1 + extraSpaces < source.length() &&
				source.charAt(position + 1 + extraSpaces) == ' ')
			{
				source.deleteCharAt(position + 1);
			}
		}
		else if (after != '\n' && after != '\r')
		{
			// No space, add one
			source.insert(position + 1, ' ');
		}
	}

	/**
	 * Fixes spacing around parentheses.
	 *
	 * @param source the source code string builder
	 * @param position the position of the parenthesis
	 */
	private static void fixParenthesesSpacing(StringBuilder source, int position)
	{
		if (source.charAt(position) == '(')
		{
			// Remove space after opening paren
			if (position + 1 < source.length() && source.charAt(position + 1) == ' ')
			{
				source.deleteCharAt(position + 1);
			}

			// Remove space before opening paren for method calls (not control keywords)
			if (position > 0 && source.charAt(position - 1) == ' ')
			{
				// Find the first non-space character before this position
				int prev = position - 1;
				while (prev > 0 && source.charAt(prev) == ' ')
					--prev;

				if (prev >= 0)
				{
					// Check if this is a control keyword - those should keep space
					if (!isControlKeywordBefore(source.toString(), position))
					{
						// It's a method call - remove space before paren
						while (position > 0 && source.charAt(position - 1) == ' ')
						{
							source.deleteCharAt(position - 1);
							--position;
						}
					}
				}
			}
		}
		else if (source.charAt(position) == ')')
		{
			// Remove space before closing paren
			if (position > 0 && source.charAt(position - 1) == ' ')
			{
				int prevNonSpace = position - 2;
				while (prevNonSpace >= 0 && source.charAt(prevNonSpace) == ' ')
					--prevNonSpace;

				if (prevNonSpace >= 0 && source.charAt(prevNonSpace) != '(')
				{
					source.deleteCharAt(position - 1);
				}
			}
		}
	}

	/**
	 * Fixes spacing around brackets.
	 *
	 * @param source the source code string builder
	 * @param position the position of the bracket
	 */
	private static void fixBracketSpacing(StringBuilder source, int position)
	{
		if (source.charAt(position) == '[')
		{
			// Remove space after opening bracket
			if (position + 1 < source.length() && source.charAt(position + 1) == ' ')
			{
				source.deleteCharAt(position + 1);
			}
		}
		else if (source.charAt(position) == ']')
		{
			// Remove space before closing bracket
			if (position > 0 && source.charAt(position - 1) == ' ')
			{
				int prevNonSpace = position - 2;
				while (prevNonSpace >= 0 && source.charAt(prevNonSpace) == ' ')
					--prevNonSpace;

				if (prevNonSpace >= 0 && source.charAt(prevNonSpace) != '[')
				{
					source.deleteCharAt(position - 1);
				}
			}
		}
	}

	/**
	 * Fixes spacing around method reference operator.
	 *
	 * @param source the source code string builder
	 * @param position the position of the first colon
	 */
	private static void fixMethodReferenceSpacing(StringBuilder source, int position)
	{
		// Remove spaces around ::
		// First, remove space after ::
		int end = position + 2;
		while (end < source.length() && source.charAt(end) == ' ')
		{
			source.deleteCharAt(end);
		}

		// Then, remove space before ::
		int start = position;
		while (start > 0 && source.charAt(start - 1) == ' ')
		{
			source.deleteCharAt(start - 1);
			--start;
		}
	}

	/**
	 * Fixes spacing before semicolon.
	 *
	 * @param source the source code string builder
	 * @param position the position of the semicolon
	 */
	private static void fixSemicolonSpacing(StringBuilder source, int position)
	{
		// Remove space before semicolon
		if (position > 0 && source.charAt(position - 1) == ' ')
		{
			source.deleteCharAt(position - 1);
		}
	}

	/**
	 * Fixes spacing around increment/decrement operators ({@code ++}, {@code --}).
	 * These should have no space between the operator and operand.
	 *
	 * @param source the source code string builder
	 * @param position the position of the first character of the operator
	 */
	private static void fixIncrementDecrementSpacing(StringBuilder source, int position)
	{
		// Position points to first + or -
		// Remove space after the operator (between ++ and operand for prefix)
		int afterOp = position + 2;
		while (afterOp < source.length() && source.charAt(afterOp) == ' ')
		{
			source.deleteCharAt(afterOp);
		}

		// Remove space before the operator (between operand and ++ for postfix)
		while (position > 0 && source.charAt(position - 1) == ' ')
		{
			source.deleteCharAt(position - 1);
			--position;
		}
	}

	/**
	 * Fixes spacing after logical NOT operator ({@code !}).
	 * There should be no space between ! and its operand.
	 *
	 * @param source the source code string builder
	 * @param position the position of the ! character
	 */
	private static void fixLogicalNotSpacing(StringBuilder source, int position)
	{
		// Check it's not part of !=
		if (position + 1 < source.length() && source.charAt(position + 1) == '=')
			return;

		// Remove space after !
		int afterBang = position + 1;
		while (afterBang < source.length() && source.charAt(afterBang) == ' ')
		{
			source.deleteCharAt(afterBang);
		}
	}

	/**
	 * Normalizes spaces around an operator to the specified amounts.
	 *
	 * @param source the source code
	 * @param opStart the start of the operator
	 * @param opEnd the end of the operator
	 * @param spaceBefore the spaces to add before
	 * @param spaceAfter the spaces to add after
	 */
	private static void normalizeSpacesAround(StringBuilder source, int opStart, int opEnd,
		String spaceBefore, String spaceAfter)
	{
		// Remove spaces before operator
		while (opStart > 0 && source.charAt(opStart - 1) == ' ')
		{
			source.deleteCharAt(opStart - 1);
			--opStart;
			--opEnd;
		}

		// Remove spaces after operator
		while (opEnd < source.length() && source.charAt(opEnd) == ' ')
		{
			source.deleteCharAt(opEnd);
		}

		// Add proper spacing
		source.insert(opEnd, spaceAfter);
		source.insert(opStart, spaceBefore);
	}

	/**
	 * Removes all spaces around an operator.
	 *
	 * @param source the source code
	 * @param opStart the start of the operator
	 * @param opEnd the end of the operator
	 */
	private static void removeSpacesAround(StringBuilder source, int opStart, int opEnd)
	{
		// Remove spaces before
		while (opStart > 0 && source.charAt(opStart - 1) == ' ')
		{
			source.deleteCharAt(opStart - 1);
			--opStart;
			--opEnd;
		}

		// Remove spaces after
		while (opEnd < source.length() && source.charAt(opEnd) == ' ')
		{
			source.deleteCharAt(opEnd);
		}
	}

	/**
	 * Checks if position is part of a no-space operator (like {@code ++}, {@code --}, {@code ::}).
	 *
	 * @param sourceCode the source code
	 * @param position the position
	 * @return true if position is part of a no-space operator
	 */
	private static boolean isNoSpaceOperator(String sourceCode, int position)
	{
		for (String op : NO_SPACE_OPERATORS)
		{
			// Check if this position starts the operator
			if (position + op.length() <= sourceCode.length() &&
				sourceCode.substring(position, position + op.length()).equals(op))
			{
				return true;
			}

			// Check if this position is within the operator (second char)
			if (position > 0 && position + op.length() - 1 <= sourceCode.length())
			{
				int startPos = position - 1;
				if (sourceCode.substring(startPos, startPos + op.length()).equals(op))
					return true;
			}
		}

		return false;
	}

	/**
	 * Checks if position starts a binary operator.
	 *
	 * @param sourceCode the source code
	 * @param position the position
	 * @return true if position starts a binary operator
	 */
	private static boolean isBinaryOperatorStart(String sourceCode, int position)
	{
		// First check if this is a no-space operator - those should not be treated as binary operators
		if (isNoSpaceOperator(sourceCode, position))
			return false;

		for (String op : BINARY_OPERATORS)
		{
			if (position + op.length() <= sourceCode.length() &&
				sourceCode.substring(position, position + op.length()).equals(op))
			{
				return true;
			}
		}

		char current = sourceCode.charAt(position);

		// For single-char operators, check they're not part of multi-char tokens
		if (current == '+' || current == '-')
		{
			// Don't treat as binary if part of ++ or --
			if (position + 1 < sourceCode.length())
			{
				char next = sourceCode.charAt(position + 1);
				if (next == current)
					return false;
			}

			if (position > 0)
			{
				char prev = sourceCode.charAt(position - 1);
				if (prev == current)
					return false;
			}
		}

		if (current == ':')
		{
			// Don't treat as binary if part of ::
			if (position + 1 < sourceCode.length() && sourceCode.charAt(position + 1) == ':')
				return false;

			if (position > 0 && sourceCode.charAt(position - 1) == ':')
				return false;
		}

		return switch (current)
		{
			case '+', '-', '*', '/', '%', '&', '|', '^', '<', '>', '=', ':', '?' -> true;
			default -> false;
		};
	}

	/**
	 * Gets the start of an operator.
	 *
	 * @param sourceCode the source code
	 * @param position the position within the operator
	 * @return the start position
	 */
	private static int getOperatorStart(String sourceCode, int position)
	{
		// Check no-space operators first
		for (String op : NO_SPACE_OPERATORS)
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

		for (String op : BINARY_OPERATORS)
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
	 * Gets the length of an operator.
	 *
	 * @param sourceCode the source code
	 * @param position the start position
	 * @return the operator length
	 */
	private static int getOperatorLength(String sourceCode, int position)
	{
		// Check no-space operators first
		for (String op : NO_SPACE_OPERATORS)
		{
			if (position + op.length() <= sourceCode.length() &&
				sourceCode.substring(position, position + op.length()).equals(op))
			{
				return op.length();
			}
		}

		for (String op : BINARY_OPERATORS)
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
	 * @param sourceCode the source code
	 * @param position the position
	 * @return true if position starts a control keyword
	 */
	private static boolean isControlKeywordStart(String sourceCode, int position)
	{
		for (String keyword : CONTROL_KEYWORDS)
		{
			if (position + keyword.length() <= sourceCode.length())
			{
				String candidate = sourceCode.substring(position, position + keyword.length());
				if (candidate.equals(keyword))
				{
					// Check word boundaries
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
	 * Checks if a control keyword is before the given position.
	 *
	 * @param sourceCode the source code
	 * @param position the position
	 * @return true if there's a control keyword before this position
	 */
	private static boolean isControlKeywordBefore(String sourceCode, int position)
	{
		// Look backward for control keyword
		int searchStart = Math.max(0, position - 20);
		for (int i = searchStart; i <= position; ++i)
		{
			if (isControlKeywordStart(sourceCode, i))
				return true;
		}

		return false;
	}

	/**
	 * Gets the start of a keyword.
	 *
	 * @param sourceCode the source code
	 * @param position the position within the keyword
	 * @return the start position
	 */
	private static int getKeywordStart(String sourceCode, int position)
	{
		int start = position;
		while (start > 0 && (Character.isLetterOrDigit(sourceCode.charAt(start - 1)) ||
			sourceCode.charAt(start - 1) == '_'))
		{
			--start;
		}

		return start;
	}

	/**
	 * Gets the end of a keyword.
	 *
	 * @param sourceCode the source code
	 * @param start the start position
	 * @return the end position
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

	/**
	 * Checks if an angle bracket is used as a generic type bracket (not a comparison operator).
	 *
	 * @param sourceCode the source code
	 * @param position the position of the angle bracket
	 * @return true if this is likely a generic bracket
	 */
	private static boolean isGenericBracket(String sourceCode, int position)
	{
		char c = sourceCode.charAt(position);
		if (c == '<')
			return isGenericOpenBracket(sourceCode, position);
		else if (c == '>')
			return isGenericCloseBracket(sourceCode, position);
		return false;
	}

	/**
	 * Checks if an opening angle bracket is used for generics.
	 *
	 * @param sourceCode the source code
	 * @param position the position of the angle bracket
	 * @return true if this is a generic opening bracket
	 */
	private static boolean isGenericOpenBracket(String sourceCode, int position)
	{
		int next = skipSpacesForward(sourceCode, position + 1);
		if (next >= sourceCode.length())
			return false;

		char afterChar = sourceCode.charAt(next);

		// Diamond operator <>
		if (afterChar == '>')
			return true;

		// Check if followed by a type name (starts with uppercase) or ?
		if (Character.isUpperCase(afterChar) || afterChar == '?')
		{
			int prev = skipSpacesBackward(sourceCode, position - 1);
			if (prev >= 0 && Character.isJavaIdentifierPart(sourceCode.charAt(prev)))
				return true;
		}

		// Type parameter declaration <T extends ...>
		return isTypeParameterDeclaration(sourceCode, next);
	}

	/**
	 * Checks if a closing angle bracket is used for generics.
	 *
	 * @param sourceCode the source code
	 * @param position the position of the angle bracket
	 * @return true if this is a generic closing bracket
	 */
	private static boolean isGenericCloseBracket(String sourceCode, int position)
	{
		// Check for diamond operator (<>) or matching generic open bracket
		boolean isDiamondOperator = position > 0 && sourceCode.charAt(position - 1) == '<';
		return isDiamondOperator || hasMatchingGenericOpenBracket(sourceCode, position);
	}

	/**
	 * Checks if position looks like a type parameter declaration.
	 *
	 * @param sourceCode the source code
	 * @param identStart the start position of the identifier
	 * @return true if this looks like a type parameter declaration
	 */
	private static boolean isTypeParameterDeclaration(String sourceCode, int identStart)
	{
		if (!Character.isUpperCase(sourceCode.charAt(identStart)))
			return false;

		int identEnd = identStart;
		while (identEnd < sourceCode.length() && Character.isJavaIdentifierPart(sourceCode.charAt(identEnd)))
			++identEnd;

		if (identEnd >= sourceCode.length())
			return false;

		int afterIdent = skipSpacesForward(sourceCode, identEnd);
		if (afterIdent >= sourceCode.length())
			return false;

		char nextChar = sourceCode.charAt(afterIdent);
		return nextChar == '>' || nextChar == ',' ||
			sourceCode.substring(afterIdent).startsWith("extends") ||
			sourceCode.substring(afterIdent).startsWith("super");
	}

	/**
	 * Looks for a matching {@code <} that suggests generics.
	 *
	 * @param sourceCode the source code
	 * @param position the position of the closing bracket
	 * @return true if a matching generic open bracket is found
	 */
	private static boolean hasMatchingGenericOpenBracket(String sourceCode, int position)
	{
		int depth = 1;
		for (int i = position - 1; i >= 0 && depth > 0; --i)
		{
			char ch = sourceCode.charAt(i);
			if (ch == '>')
				++depth;
			else if (ch == '<')
			{
				--depth;
				if (depth == 0 && isMatchedBracketGeneric(sourceCode, i, position))
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a matched bracket pair is generic.
	 *
	 * @param sourceCode the source code
	 * @param openPos the position of the opening bracket
	 * @param closePos the position of the closing bracket
	 * @return true if this matched bracket pair appears to be for generics
	 */
	private static boolean isMatchedBracketGeneric(String sourceCode, int openPos, int closePos)
	{
		int prev = skipSpacesBackward(sourceCode, openPos - 1);
		if (prev >= 0 && Character.isJavaIdentifierPart(sourceCode.charAt(prev)))
		{
			int afterLt = skipSpacesForward(sourceCode, openPos + 1);
			if (afterLt < closePos)
			{
				char firstAfterLt = sourceCode.charAt(afterLt);
				if (Character.isUpperCase(firstAfterLt) || firstAfterLt == '?')
					return true;
			}
		}
		// Standalone <T extends ...>
		if (prev < 0 || !Character.isJavaIdentifierPart(sourceCode.charAt(prev)))
		{
			int next = skipSpacesForward(sourceCode, openPos + 1);
			if (next < closePos && Character.isUpperCase(sourceCode.charAt(next)))
				return true;
		}
		return false;
	}

	/**
	 * Skips spaces forward from a position.
	 *
	 * @param sourceCode the source code
	 * @param position the starting position
	 * @return the position of the first non-space character
	 */
	private static int skipSpacesForward(String sourceCode, int position)
	{
		while (position < sourceCode.length() && sourceCode.charAt(position) == ' ')
			++position;
		return position;
	}

	/**
	 * Skips spaces backward from a position.
	 *
	 * @param sourceCode the source code
	 * @param position the starting position
	 * @return the position of the first non-space character going backward
	 */
	private static int skipSpacesBackward(String sourceCode, int position)
	{
		while (position >= 0 && sourceCode.charAt(position) == ' ')
			--position;
		return position;
	}

	/**
	 * Checks if a colon is part of a switch case statement.
	 *
	 * @param sourceCode the source code
	 * @param colonPosition the position of the colon
	 * @return true if this is a switch case colon
	 */
	private static boolean isSwitchCaseColon(String sourceCode, int colonPosition)
	{
		// Look backward for "case" or "default" keyword
		int searchStart = Math.max(0, colonPosition - 50);
		String before = sourceCode.substring(searchStart, colonPosition);
		// Find the last occurrence of "case" or "default"
		int caseIdx = before.lastIndexOf("case");
		int defaultIdx = before.lastIndexOf("default");
		int idx = Math.max(caseIdx, defaultIdx);
		if (idx >= 0)
		{
			// Ensure no other colons between keyword and this position
			String betweenText = before.substring(idx);
			// Check if there's a ':' already (would be from ternary)
			if (!betweenText.contains(":"))
				return true;
		}
		return false;
	}

	/**
	 * Removes space before a comma.
	 *
	 * @param source the source code string builder
	 * @param commaPosition the position of the comma
	 */
	private static void fixSpaceBeforeComma(StringBuilder source, int commaPosition)
	{
		while (commaPosition > 0 && source.charAt(commaPosition - 1) == ' ')
		{
			source.deleteCharAt(commaPosition - 1);
			--commaPosition;
		}
	}

	/**
	 * Checks if the position starts an "else" followed by "if" without a space between them.
	 *
	 * @param sourceCode the source code
	 * @param position the position
	 * @return true if this is "elseif" needing separation
	 */
	private static boolean isElseIfSequence(String sourceCode, int position)
	{
		if (position + 6 > sourceCode.length())
			return false;

		String candidate = sourceCode.substring(position, Math.min(position + 6, sourceCode.length()));
		return candidate.equals("elseif");
	}

	/**
	 * Fixes "elseif" by inserting a space between "else" and "if".
	 *
	 * @param source the source code string builder
	 * @param position the position of 'e' in "elseif"
	 */
	private static void fixElseIfSpacing(StringBuilder source, int position)
	{
		// Insert space after "else"
		source.insert(position + 4, ' ');
	}

	/**
	 * Checks if the position starts the "new" keyword.
	 *
	 * @param sourceCode the source code
	 * @param position the position
	 * @return true if this is the start of "new" keyword
	 */
	private static boolean isNewKeywordStart(String sourceCode, int position)
	{
		if (position + 3 > sourceCode.length())
			return false;

		String candidate = sourceCode.substring(position, position + 3);
		if (!candidate.equals("new"))
			return false;

		// Check word boundaries
		if (position > 0)
		{
			char prev = sourceCode.charAt(position - 1);
			if (Character.isLetterOrDigit(prev) || prev == '_')
				return false;
		}
		if (position + 3 < sourceCode.length())
		{
			char next = sourceCode.charAt(position + 3);
			if (Character.isLetterOrDigit(next) || next == '_')
				return false;
		}

		return true;
	}

	/**
	 * Fixes spacing after the "new" keyword - normalizes to exactly one space.
	 *
	 * @param source the source code string builder
	 * @param position the position of 'n' in "new"
	 */
	private static void fixNewKeywordSpacing(StringBuilder source, int position)
	{
		int afterNew = position + 3;

		// Remove all spaces after "new"
		while (afterNew < source.length() && source.charAt(afterNew) == ' ')
		{
			source.deleteCharAt(afterNew);
		}

		// Add exactly one space
		if (afterNew < source.length() && source.charAt(afterNew) != '(')
		{
			source.insert(afterNew, ' ');
		}
	}
}
