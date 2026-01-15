package io.github.cowwoc.styler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Lexical analyzer for Java source code.
 * <p>
 * Tokenizes source code into a stream of tokens, handling keywords, identifiers, literals, operators, and
 * comments. Provides error recovery for malformed input.
 * <p>
 * <b>Unicode Escape Processing</b>: Per
 * <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.3">JLS &#167;3.3</a>, Unicode
 * escapes ({@code &#92;uXXXX}) are decoded during lexical analysis. Tokens preserve the original escape text
 * in {@link Token#text()} while providing decoded characters via {@link Token#decodedText()} for keyword
 * matching and semantic analysis.
 */
public final class Lexer
{
	/**
	 * Maximum safe recursion depth for Unicode escape decoding to prevent stack overflow.
	 */
	private static final int MAX_DECODE_DEPTH = 1000;
	/**
	 * The suffix "-sealed" that follows "non" to form the "non-sealed" keyword.
	 */
	private static final String NON_SEALED_SUFFIX = "-sealed";
	private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
		Map.entry("abstract", TokenType.ABSTRACT),
		Map.entry("assert", TokenType.ASSERT),
		Map.entry("boolean", TokenType.BOOLEAN),
		Map.entry("break", TokenType.BREAK),
		Map.entry("byte", TokenType.BYTE),
		Map.entry("case", TokenType.CASE),
		Map.entry("catch", TokenType.CATCH),
		Map.entry("char", TokenType.CHAR),
		Map.entry("class", TokenType.CLASS),
		Map.entry("const", TokenType.CONST),
		Map.entry("continue", TokenType.CONTINUE),
		Map.entry("default", TokenType.DEFAULT),
		Map.entry("do", TokenType.DO),
		Map.entry("double", TokenType.DOUBLE),
		Map.entry("else", TokenType.ELSE),
		Map.entry("enum", TokenType.ENUM),
		Map.entry("exports", TokenType.EXPORTS),
		Map.entry("extends", TokenType.EXTENDS),
		Map.entry("final", TokenType.FINAL),
		Map.entry("finally", TokenType.FINALLY),
		Map.entry("float", TokenType.FLOAT),
		Map.entry("for", TokenType.FOR),
		Map.entry("goto", TokenType.GOTO),
		Map.entry("if", TokenType.IF),
		Map.entry("implements", TokenType.IMPLEMENTS),
		Map.entry("import", TokenType.IMPORT),
		Map.entry("instanceof", TokenType.INSTANCEOF),
		Map.entry("int", TokenType.INT),
		Map.entry("interface", TokenType.INTERFACE),
		Map.entry("long", TokenType.LONG),
		Map.entry("module", TokenType.MODULE),
		Map.entry("native", TokenType.NATIVE),
		Map.entry("open", TokenType.OPEN),
		Map.entry("opens", TokenType.OPENS),
		Map.entry("new", TokenType.NEW),
		Map.entry("non-sealed", TokenType.NON_SEALED),
		Map.entry("package", TokenType.PACKAGE),
		Map.entry("permits", TokenType.PERMITS),
		Map.entry("private", TokenType.PRIVATE),
		Map.entry("protected", TokenType.PROTECTED),
		Map.entry("provides", TokenType.PROVIDES),
		Map.entry("public", TokenType.PUBLIC),
		Map.entry("record", TokenType.RECORD),
		Map.entry("requires", TokenType.REQUIRES),
		Map.entry("return", TokenType.RETURN),
		Map.entry("sealed", TokenType.SEALED),
		Map.entry("short", TokenType.SHORT),
		Map.entry("static", TokenType.STATIC),
		Map.entry("strictfp", TokenType.STRICTFP),
		Map.entry("super", TokenType.SUPER),
		Map.entry("switch", TokenType.SWITCH),
		Map.entry("synchronized", TokenType.SYNCHRONIZED),
		Map.entry("this", TokenType.THIS),
		Map.entry("throw", TokenType.THROW),
		Map.entry("throws", TokenType.THROWS),
		Map.entry("to", TokenType.TO),
		Map.entry("transient", TokenType.TRANSIENT),
		Map.entry("transitive", TokenType.TRANSITIVE),
		Map.entry("try", TokenType.TRY),
		Map.entry("uses", TokenType.USES),
		Map.entry("var", TokenType.VAR),
		Map.entry("void", TokenType.VOID),
		Map.entry("volatile", TokenType.VOLATILE),
		Map.entry("while", TokenType.WHILE),
		Map.entry("with", TokenType.WITH),
		Map.entry("yield", TokenType.YIELD),
		Map.entry("true", TokenType.BOOLEAN_LITERAL),
		Map.entry("false", TokenType.BOOLEAN_LITERAL),
		Map.entry("null", TokenType.NULL_LITERAL));

	private final String source;
	private int position;

	/**
	 * Creates a new lexer for the specified source code.
	 *
	 * @param source the Java source code to tokenize
	 * @throws NullPointerException if {@code source} is {@code null}
	 */
	public Lexer(String source)
	{
		this.source = requireThat(source, "source").isNotNull().getValue();
	}

	/**
	 * Tokenizes the entire source code.
	 *
	 * @return list of tokens including END_OF_FILE token
	 */
	public List<Token> tokenize()
	{
		List<Token> tokens = new ArrayList<>();
		Token token;
		do
		{
			token = nextToken();
			tokens.add(token);
		}
		while (token.type() != TokenType.END_OF_FILE);
		return tokens;
	}

	/**
	 * Reads and returns the next token from the source.
	 *
	 * @return the next token
	 */
	public Token nextToken()
	{
		skipWhitespace();

		if (position >= source.length())
			return new Token(TokenType.END_OF_FILE, position, position, null);

		int start = position;
		char ch = source.charAt(position);

		// Comments
		if (ch == '/')
		{
			if (peek() == '/')
				return scanLineComment(start);
			if (peek() == '*')
				return scanBlockComment(start);
		}

		// Identifiers and keywords - check both direct characters and Unicode escapes
		if (Character.isJavaIdentifierStart(ch) || isIdentifierStartAtPosition())
			return scanIdentifierOrKeyword(start);

		// Numbers
		if (Character.isDigit(ch))
			return scanNumber(start);

		// Floating-point literals starting with decimal point (e.g., .5, .0025)
		if (ch == '.' && Character.isDigit(peek()))
			return scanFloatingPointStartingWithDot(start);

		// String literals
		if (ch == '"')
			return scanStringLiteral(start);

		// Char literals
		if (ch == '\'')
			return scanCharLiteral(start);

		// Operators and separators
		return scanOperatorOrSeparator(start);
	}

	private void skipWhitespace()
	{
		while (position < source.length() && Character.isWhitespace(source.charAt(position)))
			++position;
	}

	private Token scanLineComment(int start)
	{
		// Skip past "//"
		position += 2;

		// Check for markdown doc comment (/// per JEP 467)
		boolean isMarkdownDoc = position < source.length() && source.charAt(position) == '/';

		while (position < source.length() && source.charAt(position) != '\n')
			++position;
		String text = source.substring(start, position);
		TokenType type;
		if (isMarkdownDoc)
			type = TokenType.MARKDOWN_DOC_COMMENT;
		else
			type = TokenType.LINE_COMMENT;
		return new Token(type, start, position, text);
	}

	private Token scanBlockComment(int start)
	{
		// Skip "/*"
		position += 2;
		boolean isJavadoc = position < source.length() && source.charAt(position) == '*';

		while (position < source.length())
		{
			if (source.charAt(position) == '*' && peek() == '/')
			{
				// Skip "*/"
				position += 2;
				break;
			}
			++position;
		}

		String text = source.substring(start, position);
		TokenType type;
		if (isJavadoc)
			type = TokenType.JAVADOC_COMMENT;
		else
			type = TokenType.BLOCK_COMMENT;
		return new Token(type, start, position, text);
	}

	private Token scanIdentifierOrKeyword(int start)
	{
		boolean containsUnicodeEscape = scanIdentifierChars();

		String text = source.substring(start, position);
		String decodedText = resolveDecodedText(text, containsUnicodeEscape);

		// Special case for "non-sealed" keyword (contains hyphen)
		if (isNonSealedKeyword(decodedText))
		{
			position += NON_SEALED_SUFFIX.length();
			text = source.substring(start, position);
			decodedText = resolveDecodedText(text, containsUnicodeEscape);
			return new Token(TokenType.NON_SEALED, start, position, text, decodedText);
		}

		// Use decoded text for keyword lookup
		TokenType type = KEYWORDS.getOrDefault(decodedText, TokenType.IDENTIFIER);

		// For memory efficiency, use same String instance when no Unicode escapes present
		if (containsUnicodeEscape)
			return new Token(type, start, position, text, decodedText);
		return new Token(type, start, position, text);
	}

	/**
	 * Scans identifier characters including Unicode escapes.
	 *
	 * @return {@code true} if any Unicode escapes were found
	 */
	private boolean scanIdentifierChars()
	{
		boolean containsUnicodeEscape = false;

		while (position < source.length())
		{
			char ch = source.charAt(position);

			// Check for Unicode escape
			if (ch == '\\' && position + 1 < source.length() && source.charAt(position + 1) == 'u')
			{
				int checkpoint = position;
				int decoded = tryParseUnicodeEscape();
				if (decoded >= 0 && Character.isJavaIdentifierPart((char) decoded))
				{
					containsUnicodeEscape = true;
					continue;
				}
				// Not a valid identifier part, rollback and break
				position = checkpoint;
				break;
			}

			if (Character.isJavaIdentifierPart(ch))
				++position;
			else
				break;
		}

		return containsUnicodeEscape;
	}

	/**
	 * Resolves the decoded text, decoding Unicode escapes if present.
	 *
	 * @param text                  the original text
	 * @param containsUnicodeEscape whether the text contains Unicode escapes
	 * @return the decoded text, or the original text if no escapes
	 */
	private String resolveDecodedText(String text, boolean containsUnicodeEscape)
	{
		if (containsUnicodeEscape)
			return decodeUnicodeEscapes(text);
		return text;
	}

	/**
	 * Checks if the decoded identifier represents the "non-sealed" keyword.
	 *
	 * @param decodedText the decoded identifier text
	 * @return {@code true} if followed by "-sealed" to form the non-sealed keyword
	 */
	private boolean isNonSealedKeyword(String decodedText)
	{
		if (!decodedText.equals("non"))
			return false;
		if (position + 7 > source.length())
			return false;
		String remaining = source.substring(position, position + 7);
		return remaining.equals("-sealed");
	}

	private Token scanNumber(int start)
	{
		// Check for binary (0b/0B) or hexadecimal (0x/0X) prefix
		if (source.charAt(position) == '0' && position + 1 < source.length())
		{
			char nextChar = source.charAt(position + 1);
			if (nextChar == 'b' || nextChar == 'B')
				return scanBinaryLiteral(start);
			if (nextChar == 'x' || nextChar == 'X')
				return scanHexLiteral(start);
		}

		// Decimal number scanning - handles integers, longs, floats, doubles
		boolean hasDecimal = false;
		boolean hasExponent = false;

		while (position < source.length())
		{
			char ch = source.charAt(position);

			if (Character.isDigit(ch) || ch == '_')
				++position;
			else if (ch == '.' && !hasDecimal && !hasExponent)
			{
				hasDecimal = true;
				++position;
			}
			else if ((ch == 'e' || ch == 'E') && !hasExponent)
			{
				hasExponent = true;
				hasDecimal = true;
				++position;
				consumeOptionalExponentSign();
			}
			else
			{
				switch (ch)
				{
					case 'L', 'l' -> ++position;
					case 'F', 'f', 'D', 'd' ->
					{
						++position;
						hasDecimal = true;
					}
					default ->
					{
						// No suffix character, done scanning
					}
				}
				break;
			}
		}

		String text = source.substring(start, position);
		TokenType type;

		if (text.endsWith("L") || text.endsWith("l"))
			type = TokenType.LONG_LITERAL;
		else if (text.endsWith("F") || text.endsWith("f"))
			type = TokenType.FLOAT_LITERAL;
		else if (text.endsWith("D") || text.endsWith("d") || hasDecimal)
			type = TokenType.DOUBLE_LITERAL;
		else
			type = TokenType.INTEGER_LITERAL;

		return new Token(type, start, position, text);
	}

	/**
	 * Scans a floating-point literal that starts with a decimal point (e.g., {@code .5}, {@code .0025}).
	 * Per JLS §3.10.2, floating-point literals may omit the integer part when starting with a decimal point.
	 *
	 * @param start the starting position of the literal (at the decimal point)
	 * @return the token for the scanned floating-point literal
	 */
	private Token scanFloatingPointStartingWithDot(int start)
	{
		// Skip the decimal point
		++position;

		// Consume digits and underscores after decimal point
		consumeDigitsAndUnderscores();

		// Check for exponent (e/E)
		if (position < source.length() && (source.charAt(position) == 'e' || source.charAt(position) == 'E'))
		{
			++position;
			consumeOptionalExponentSign();
			consumeDigitsAndUnderscores();
		}

		// Check for type suffix (f/F/d/D)
		TokenType type = TokenType.DOUBLE_LITERAL;
		if (position < source.length())
		{
			char ch = source.charAt(position);
			if (ch == 'f' || ch == 'F')
			{
				type = TokenType.FLOAT_LITERAL;
				++position;
			}
			else if (ch == 'd' || ch == 'D')
			{
				++position;
			}
		}

		String text = source.substring(start, position);
		return new Token(type, start, position, text);
	}

	/**
	 * Consumes consecutive digits and underscores starting at the current position.
	 */
	private void consumeDigitsAndUnderscores()
	{
		while (position < source.length())
		{
			char ch = source.charAt(position);
			if (Character.isDigit(ch) || ch == '_')
				++position;
			else
				break;
		}
	}

	/**
	 * Checks if the given character is a binary digit ({@code 0} or {@code 1}).
	 *
	 * @param ch the character to test
	 * @return true if character is a binary digit, false otherwise
	 */
	private boolean isBinaryDigit(char ch)
	{
		return ch == '0' || ch == '1';
	}


	/**
	 * Scans a binary integer literal starting with {@code 0b} or {@code 0B}.
	 * Handles underscores in binary digits and optional {@code L}/{@code l} suffix.
	 *
	 * @param start the starting position of the literal
	 * @return the token for the scanned binary literal
	 */
	private Token scanBinaryLiteral(int start)
	{
		// Skip "0b" or "0B" prefix
		position += 2;

		// Scan binary digits and underscores
		while (position < source.length())
		{
			char ch = source.charAt(position);
			if (isBinaryDigit(ch) || ch == '_')
				++position;
			else
				break;
		}

		// Check for long suffix
		if (position < source.length() && (source.charAt(position) == 'L' || source.charAt(position) == 'l'))
		{
			++position;
			String text = source.substring(start, position);
			return new Token(TokenType.LONG_LITERAL, start, position, text);
		}

		String text = source.substring(start, position);
		return new Token(TokenType.INTEGER_LITERAL, start, position, text);
	}

	/**
	 * Scans a hexadecimal literal starting with {@code 0x} or {@code 0X}.
	 * Handles hexadecimal digits, underscores, optional floating-point notation with binary exponent,
	 * and suffixes ({@code L}/{@code l}/{@code F}/{@code f}/{@code D}/{@code d}).
	 *
	 * @param start the starting position of the literal
	 * @return the token for the scanned hexadecimal literal
	 */
	private Token scanHexLiteral(int start)
	{
		// Skip "0x" or "0X" prefix
		position += 2;

		// Scan hexadecimal digits and underscores
		while (position < source.length())
		{
			char ch = source.charAt(position);
			if (isHexDigit(ch) || ch == '_')
				++position;
			else
				break;
		}

		// Check for hexadecimal floating-point literal
		if (position < source.length() && source.charAt(position) == '.')
			return scanHexFloatLiteral(start);

		// Check for binary exponent (p or P)
		if (position < source.length() && (source.charAt(position) == 'p' || source.charAt(position) == 'P'))
			return scanHexFloatLiteral(start);

		// Check for long suffix
		if (position < source.length() && (source.charAt(position) == 'L' || source.charAt(position) == 'l'))
		{
			++position;
			String text = source.substring(start, position);
			return new Token(TokenType.LONG_LITERAL, start, position, text);
		}

		String text = source.substring(start, position);
		return new Token(TokenType.INTEGER_LITERAL, start, position, text);
	}

	/**
	 * Scans a hexadecimal floating-point literal.
	 * Handles the optional fractional part after {@code .} and requires a binary exponent ({@code p}/{@code P})
	 * followed by an optional sign and decimal exponent digits. Supports suffixes
	 * ({@code f}/{@code F}/{@code d}/{@code D}).
	 *
	 * @param start the starting position of the literal
	 * @return the token for the scanned hexadecimal floating-point literal
	 */
	private Token scanHexFloatLiteral(int start)
	{
		// Handle fractional part if present
		if (position < source.length() && source.charAt(position) == '.')
		{
			++position;
			// Scan hexadecimal digits and underscores in fractional part
			while (position < source.length())
			{
				char ch = source.charAt(position);
				if (isHexDigit(ch) || ch == '_')
					++position;
				else
					break;
			}
		}

		// Binary exponent is required for hexadecimal floating-point literals
		if (position < source.length() && (source.charAt(position) == 'p' || source.charAt(position) == 'P'))
		{
			++position;

			// Optional sign for exponent
			if (position < source.length() && (source.charAt(position) == '+' || source.charAt(position) == '-'))
				++position;

			// Decimal exponent digits
			while (position < source.length())
			{
				char ch = source.charAt(position);
				if (Character.isDigit(ch) || ch == '_')
					++position;
				else
					break;
			}
		}

		// Check for floating-point suffix (f/F or d/D, or none defaults to double)
		TokenType type = TokenType.DOUBLE_LITERAL;
		if (position < source.length())
		{
			char ch = source.charAt(position);
			if (ch == 'f' || ch == 'F')
			{
				++position;
				type = TokenType.FLOAT_LITERAL;
			}
			else if (ch == 'd' || ch == 'D')
			{
				++position;
				type = TokenType.DOUBLE_LITERAL;
			}
		}

		String text = source.substring(start, position);
		return new Token(type, start, position, text);
	}

	/**
	 * Consumes an optional exponent sign (+ or -) if present at current position.
	 */
	private void consumeOptionalExponentSign()
	{
		if (position < source.length())
		{
			char next = source.charAt(position);
			if (next == '+' || next == '-')
				++position;
		}
	}

	private Token scanStringLiteral(int start)
	{
		++position;

		// Check for text block: """
		if (position + 1 < source.length() &&
			source.charAt(position) == '"' &&
			source.charAt(position + 1) == '"')
			return scanTextBlock(start);

		while (position < source.length())
		{
			char ch = source.charAt(position);
			if (ch == '"')
			{
				++position;
				break;
			}
			if (ch == '\\')
			{
				++position;
				consumeEscapeSequence();
			}
			else
				++position;
		}

		String text = source.substring(start, position);
		return new Token(TokenType.STRING_LITERAL, start, position, text);
	}

	private Token scanTextBlock(int start)
	{
		// Skip the remaining two quotes of opening """
		position += 2;

		// Skip whitespace until newline (text block content starts after newline)
		while (position < source.length() && source.charAt(position) != '\n')
		{
			if (!Character.isWhitespace(source.charAt(position)))
				throw new LexerException(
					"Text block opening delimiter must be followed by line terminator", position);
			++position;
		}

		// Skip the newline
		if (position < source.length())
			++position;

		// Scan until closing """
		while (position + 2 < source.length())
		{
			if (source.charAt(position) == '"' &&
				source.charAt(position + 1) == '"' &&
				source.charAt(position + 2) == '"')
			{
				// Skip closing """
				position += 3;
				String text = source.substring(start, position);
				return new Token(TokenType.STRING_LITERAL, start, position, text);
			}
			if (source.charAt(position) == '\\')
			{
				++position;
				consumeEscapeSequence();
			}
			else
				++position;
		}

		throw new LexerException("Unclosed text block starting at position " + start, start);
	}

	private Token scanCharLiteral(int start)
	{
		++position;

		if (position < source.length())
		{
			if (source.charAt(position) == '\\')
			{
				++position;
				consumeEscapeSequence();
			}
			else
				++position;
		}

		if (position < source.length() && source.charAt(position) == '\'')
			++position;

		String text = source.substring(start, position);
		return new Token(TokenType.CHAR_LITERAL, start, position, text);
	}

	/**
	 * Consumes an escape sequence starting after the backslash.
	 * Handles standard escapes ({@code \n}, {@code \t}, etc.), octal escapes ({@code \0} through
	 * {@code \377}), and Unicode escapes (backslash-u plus 4 hex digits).
	 * The backslash has already been consumed when this method is called.
	 */
	private void consumeEscapeSequence()
	{
		if (position >= source.length())
			return;

		char ch = source.charAt(position);
		if (ch == 'u')
		{
			// Unicode escape: skip all 'u' chars (JLS allows multiple 'u' chars before hex digits)
			while (position < source.length() && source.charAt(position) == 'u')
				++position;
			// Skip up to 4 hex digits
			int hexCount = 0;
			while (position < source.length() && hexCount < 4 && isHexDigit(source.charAt(position)))
			{
				++position;
				++hexCount;
			}
		}
		else if (isOctalDigit(ch))
		{
			// Octal escape: \0 through \377
			++position;
			consumeOctalEscape(ch);
		}
		else
			// Standard escape: skip single character (\n, \t, \r, \', \", \\, etc.)
			++position;
	}

	private boolean isHexDigit(char ch)
	{
		return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
	}

	private boolean isOctalDigit(char ch)
	{
		return ch >= '0' && ch <= '7';
	}

	/**
	 * Consumes an octal escape sequence starting at the first octal digit.
	 * Per JLS 3.10.6, octal escapes are:
	 * <ul>
	 *   <li>{@code \0} through {@code \7} (single digit)</li>
	 *   <li>{@code \00} through {@code \77} (two digits)</li>
	 *   <li>{@code \000} through {@code \377} (three digits, max value 255)</li>
	 * </ul>
	 * The backslash and first octal digit have already been consumed when this method is called.
	 *
	 * @param firstDigit the first octal digit (0-7) already consumed
	 */
	private void consumeOctalEscape(char firstDigit)
	{
		// First digit already consumed by caller
		// Check for second digit
		if (position >= source.length() || !isOctalDigit(source.charAt(position)))
			return;

		char secondDigit = source.charAt(position);
		++position;

		// Check for third digit (only valid if first digit is 0-3 to keep value <= 377 octal = 255 decimal)
		if (position >= source.length() || !isOctalDigit(source.charAt(position)))
			return;

		if (firstDigit <= '3')
			++position;
	}

	/**
	 * Checks if the current position matches the expected character and consumes it if found.
	 * Eliminates duplicate bounds checking and lookahead logic.
	 *
	 * @param expected the expected character to match
	 * @return true if character matched and consumed, false otherwise
	 */
	private boolean matchAndConsume(char expected)
	{
		if (position < source.length() && source.charAt(position) == expected)
		{
			++position;
			return true;
		}
		return false;
	}

	private Token scanOperatorOrSeparator(int start)
	{
		char ch = source.charAt(position);
		++position;

		// After incrementing position, check the character AT current position
		// (not ahead of it, which peek() would do)

		TokenType type = switch (ch)
		{
			case '(' -> TokenType.LEFT_PARENTHESIS;
			case ')' -> TokenType.RIGHT_PARENTHESIS;
			case '{' -> TokenType.LEFT_BRACE;
			case '}' -> TokenType.RIGHT_BRACE;
			case '[' -> TokenType.LEFT_BRACKET;
			case ']' -> TokenType.RIGHT_BRACKET;
			case ';' -> TokenType.SEMICOLON;
			case ',' -> TokenType.COMMA;
			case '@' -> TokenType.AT_SIGN;
			case '~' -> TokenType.TILDE;
			case '?' -> TokenType.QUESTION_MARK;
			case ':' ->
			{
				if (matchAndConsume(':'))
					yield TokenType.DOUBLE_COLON;
				yield TokenType.COLON;
			}
			case '.' ->
			{
				if (matchAndConsume('.') && matchAndConsume('.'))
					yield TokenType.ELLIPSIS;
				yield TokenType.DOT;
			}
			case '=' ->
			{
				if (matchAndConsume('='))
					yield TokenType.EQUAL;
				yield TokenType.ASSIGN;
			}
			case '!' ->
			{
				if (matchAndConsume('='))
					yield TokenType.NOT_EQUAL;
				yield TokenType.NOT;
			}
			case '<' ->
			{
				if (matchAndConsume('='))
					yield TokenType.LESS_THAN_OR_EQUAL;
				if (matchAndConsume('<'))
				{
					if (matchAndConsume('='))
						yield TokenType.LEFT_SHIFT_ASSIGN;
					yield TokenType.LEFT_SHIFT;
				}
				yield TokenType.LESS_THAN;
			}
			case '>' -> scanGreaterThanOperator();
			case '&' ->
			{
				if (matchAndConsume('&'))
					yield TokenType.LOGICAL_AND;
				if (matchAndConsume('='))
					yield TokenType.BITWISE_AND_ASSIGN;
				yield TokenType.BITWISE_AND;
			}
			case '|' ->
			{
				if (matchAndConsume('|'))
					yield TokenType.LOGICAL_OR;
				if (matchAndConsume('='))
					yield TokenType.BITWISE_OR_ASSIGN;
				yield TokenType.BITWISE_OR;
			}
			case '+' ->
			{
				if (matchAndConsume('+'))
					yield TokenType.INCREMENT;
				if (matchAndConsume('='))
					yield TokenType.PLUS_ASSIGN;
				yield TokenType.PLUS;
			}
			case '-' ->
			{
				if (matchAndConsume('-'))
					yield TokenType.DECREMENT;
				if (matchAndConsume('='))
					yield TokenType.MINUS_ASSIGN;
				if (matchAndConsume('>'))
					yield TokenType.ARROW;
				yield TokenType.MINUS;
			}
			case '*' ->
			{
				if (matchAndConsume('='))
					yield TokenType.STAR_ASSIGN;
				yield TokenType.STAR;
			}
			case '/' ->
			{
				if (matchAndConsume('='))
					yield TokenType.DIVIDE_ASSIGN;
				yield TokenType.DIVIDE;
			}
			case '^' ->
			{
				if (matchAndConsume('='))
					yield TokenType.CARET_ASSIGN;
				yield TokenType.CARET;
			}
			case '%' ->
			{
				if (matchAndConsume('='))
					yield TokenType.MODULO_ASSIGN;
				yield TokenType.MODULO;
			}
			default -> TokenType.ERROR;
		};

		String text = source.substring(start, position);
		return new Token(type, start, position, text);
	}

	/**
	 * Scans greater-than related operators: {@code >}, {@code >=}, {@code >>}, {@code >>=},
	 * {@code >>>}, {@code >>>=}.
	 *
	 * @return the token type for the scanned operator
	 */
	private TokenType scanGreaterThanOperator()
	{
		if (matchAndConsume('='))
			return TokenType.GREATER_THAN_OR_EQUAL;
		if (matchAndConsume('>'))
			return scanRightShiftOperator();
		return TokenType.GREATER_THAN;
	}

	/**
	 * Scans right shift operators: {@code >>}, {@code >>=}, {@code >>>}, {@code >>>=}.
	 * Called after the second '>' has been consumed.
	 *
	 * @return the token type for the scanned operator
	 */
	private TokenType scanRightShiftOperator()
	{
		if (matchAndConsume('>'))
			return scanUnsignedRightShiftOperator();
		if (matchAndConsume('='))
			return TokenType.RIGHT_SHIFT_ASSIGN;
		return TokenType.RIGHT_SHIFT;
	}

	/**
	 * Scans unsigned right shift operators: {@code >>>}, {@code >>>=}.
	 * Called after the third '>' has been consumed.
	 *
	 * @return the token type for the scanned operator
	 */
	private TokenType scanUnsignedRightShiftOperator()
	{
		if (matchAndConsume('='))
			return TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN;
		return TokenType.UNSIGNED_RIGHT_SHIFT;
	}

	private char peek()
	{
		int nextPos = position + 1;
		if (nextPos < source.length())
			return source.charAt(nextPos);
		return '\0';
	}

	/**
	 * Attempts to parse a Unicode escape sequence starting at the current position.
	 * <p>
	 * Per <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.3">JLS &#167;3.3</a>,
	 * a Unicode escape has the form {@code &#92;uXXXX} where each {@code X} is a hexadecimal digit. Multiple
	 * {@code u} characters are allowed (e.g., {@code &#92;uuuu0041}).
	 * <p>
	 * Uses checkpoint/rollback pattern: saves position, attempts parse, rolls back on failure.
	 *
	 * @return the decoded character (0-65535), or -1 if no valid Unicode escape at current position
	 */
	private int tryParseUnicodeEscape()
	{
		if (position >= source.length() || source.charAt(position) != '\\')
			return -1;

		// Must have at least 6 characters for backslash-u-XXXX
		if (position + 5 >= source.length())
			return -1;

		int checkpoint = position;
		++position;
		if (source.charAt(position) != 'u')
		{
			position = checkpoint;
			return -1;
		}

		// Skip all consecutive 'u' characters (JLS allows \uuuu0041)
		while (position < source.length() && source.charAt(position) == 'u')
			++position;

		// Must have exactly 4 hex digits
		if (position + 4 > source.length())
		{
			position = checkpoint;
			return -1;
		}

		int value = 0;
		for (int i = 0; i < 4; ++i)
		{
			char ch = source.charAt(position);
			int digit = hexDigitValue(ch);
			if (digit < 0)
			{
				position = checkpoint;
				return -1;
			}
			value = (value << 4) | digit;
			++position;
		}

		return value;
	}

	/**
	 * Returns the numeric value of a hexadecimal digit character.
	 *
	 * @param ch the character to evaluate
	 * @return the value (0-15), or -1 if not a hex digit
	 */
	private int hexDigitValue(char ch)
	{
		if (ch >= '0' && ch <= '9')
			return ch - '0';
		if (ch >= 'a' && ch <= 'f')
			return ch - 'a' + 10;
		if (ch >= 'A' && ch <= 'F')
			return ch - 'A' + 10;
		return -1;
	}

	/**
	 * Decodes all Unicode escapes in the given text.
	 * <p>
	 * Per <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.3">JLS &#167;3.3</a>,
	 * this processes the text as if Unicode escapes were replaced with their corresponding characters.
	 *
	 * @param text the text that may contain Unicode escapes
	 * @return the decoded text with escapes replaced, or the original text if no escapes found
	 */
	private String decodeUnicodeEscapes(String text)
	{
		// Fast path: no backslash means no escapes
		int backslashIndex = text.indexOf('\\');
		if (backslashIndex < 0)
			return text;

		return decodeUnicodeEscapesWithDepth(text, 0);
	}

	/**
	 * Internal decoder with depth tracking to prevent stack overflow.
	 *
	 * @param text  the text to decode
	 * @param depth current recursion depth
	 * @return the decoded text
	 */
	private String decodeUnicodeEscapesWithDepth(String text, int depth)
	{
		if (depth > MAX_DECODE_DEPTH)
			throw new LexerException("Unicode escape nesting too deep at position " + position, position);

		StringBuilder result = new StringBuilder(text.length());
		int[] index = {0};
		while (index[0] < text.length())
		{
			char ch = text.charAt(index[0]);
			if (ch == '\\' && index[0] + 1 < text.length() && text.charAt(index[0] + 1) == 'u')
				decodeEscapeSequence(text, index, result);
			else
			{
				result.append(ch);
				++index[0];
			}
		}

		return result.toString();
	}

	/**
	 * Decodes a single Unicode escape sequence from the text.
	 *
	 * @param text   the source text
	 * @param index  array containing current index (modified by this method)
	 * @param result the result builder to append to
	 */
	private void decodeEscapeSequence(String text, int[] index, StringBuilder result)
	{
		int escapeStart = index[0];
		++index[0];

		// Skip all 'u' characters
		while (index[0] < text.length() && text.charAt(index[0]) == 'u')
			++index[0];

		// Need 4 hex digits
		if (index[0] + 4 > text.length())
		{
			result.append(text, escapeStart, index[0]);
			return;
		}

		int decoded = tryDecodeHexDigits(text, index[0]);
		if (decoded >= 0)
		{
			result.append((char) decoded);
			index[0] += 4;
		}
		else
		{
			result.append(text, escapeStart, index[0]);
		}
	}

	/**
	 * Attempts to decode 4 hexadecimal digits starting at the given position.
	 *
	 * @param text  the source text
	 * @param start the starting position
	 * @return the decoded value (0-65535), or -1 if not valid hex digits
	 */
	private int tryDecodeHexDigits(String text, int start)
	{
		int value = 0;
		for (int j = 0; j < 4; ++j)
		{
			int digit = hexDigitValue(text.charAt(start + j));
			if (digit < 0)
				return -1;
			value = (value << 4) | digit;
		}
		return value;
	}

	/**
	 * Peeks at the character at the current position, decoding a Unicode escape if present.
	 *
	 * @return the character at current position (decoded if Unicode escape), or '\0' if at end
	 */
	private char peekCurrentCharDecoded()
	{
		if (position >= source.length())
			return '\0';

		char ch = source.charAt(position);
		if (ch == '\\' && position + 1 < source.length() && source.charAt(position + 1) == 'u')
		{
			int checkpoint = position;
			int decoded = tryParseUnicodeEscape();
			position = checkpoint;
			if (decoded >= 0)
				return (char) decoded;
		}
		return ch;
	}

	/**
	 * Checks if an identifier could start at the current position, considering Unicode escapes.
	 *
	 * @return {@code true} if an identifier start character is at current position (possibly escaped)
	 */
	private boolean isIdentifierStartAtPosition()
	{
		char ch = peekCurrentCharDecoded();
		return Character.isJavaIdentifierStart(ch);
	}

	/**
	 * Exception thrown when lexical analysis fails.
	 */
	public static final class LexerException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		private final int position;

		/**
		 * Creates a new lexer exception.
		 *
		 * @param message  the error message
		 * @param position the position in source code where error occurred
		 */
		public LexerException(String message, int position)
		{
			super(message);
			this.position = position;
		}

		/**
		 * Returns the position in source code where the error occurred.
		 *
		 * @return the position
		 */
		public int getPosition()
		{
			return position;
		}
	}
}
