package io.github.cowwoc.styler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Lexical analyzer for Java source code.
 * Tokenizes source code into a stream of tokens, handling keywords, identifiers,
 * literals, operators, and comments. Provides error recovery for malformed input.
 */
public final class Lexer
{
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
		Map.entry("native", TokenType.NATIVE),
		Map.entry("new", TokenType.NEW),
		Map.entry("non-sealed", TokenType.NON_SEALED),
		Map.entry("package", TokenType.PACKAGE),
		Map.entry("permits", TokenType.PERMITS),
		Map.entry("private", TokenType.PRIVATE),
		Map.entry("protected", TokenType.PROTECTED),
		Map.entry("public", TokenType.PUBLIC),
		Map.entry("record", TokenType.RECORD),
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
		Map.entry("transient", TokenType.TRANSIENT),
		Map.entry("try", TokenType.TRY),
		Map.entry("var", TokenType.VAR),
		Map.entry("void", TokenType.VOID),
		Map.entry("volatile", TokenType.VOLATILE),
		Map.entry("while", TokenType.WHILE),
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
	 * @return list of tokens including EOF token
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
		while (token.type() != TokenType.EOF);
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
		{
			return new Token(TokenType.EOF, position, position, null);
		}

		int start = position;
		char ch = source.charAt(position);

		// Comments
		if (ch == '/')
		{
			if (peek() == '/')
			{
				return scanLineComment(start);
			}
			if (peek() == '*')
			{
				return scanBlockComment(start);
			}
		}

		// Identifiers and keywords
		if (Character.isJavaIdentifierStart(ch))
		{
			return scanIdentifierOrKeyword(start);
		}

		// Numbers
		if (Character.isDigit(ch))
		{
			return scanNumber(start);
		}

		// String literals
		if (ch == '"')
		{
			return scanStringLiteral(start);
		}

		// Char literals
		if (ch == '\'')
		{
			return scanCharLiteral(start);
		}

		// Operators and separators
		return scanOperatorOrSeparator(start);
	}

	private void skipWhitespace()
	{
		while (position < source.length() && Character.isWhitespace(source.charAt(position)))
		{
			++position;
		}
	}

	private Token scanLineComment(int start)
	{
		// Skip past "//"
		position += 2;

		// Check for markdown doc comment (/// per JEP 467)
		boolean isMarkdownDoc = position < source.length() && source.charAt(position) == '/';

		while (position < source.length() && source.charAt(position) != '\n')
		{
			++position;
		}
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
		position += 2;
		boolean isJavadoc = position < source.length() && source.charAt(position) == '*';

		while (position < source.length())
		{
			if (source.charAt(position) == '*' && peek() == '/')
			{
				position += 2;
				break;
			}
			++position;
		}

		String text = source.substring(start, position);
		TokenType type;
		if (isJavadoc)
		{
			type = TokenType.JAVADOC_COMMENT;
		}
		else
		{
			type = TokenType.BLOCK_COMMENT;
		}
		return new Token(type, start, position, text);
	}

	private Token scanIdentifierOrKeyword(int start)
	{
		while (position < source.length() && Character.isJavaIdentifierPart(source.charAt(position)))
		{
			++position;
		}

		String text = source.substring(start, position);

		// Special case for "non-sealed" keyword (contains hyphen)
		if (text.equals("non") && position + 7 <= source.length())
		{
			String remaining = source.substring(position, position + 7);
			if (remaining.equals("-sealed"))
			{
				position += 7;
				text = "non-sealed";
				return new Token(TokenType.NON_SEALED, start, position, text);
			}
		}

		TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
		return new Token(type, start, position, text);
	}

	private Token scanNumber(int start)
	{
		// Check for binary (0b/0B) or hexadecimal (0x/0X) prefix
		if (source.charAt(position) == '0' && position + 1 < source.length())
		{
			char nextChar = source.charAt(position + 1);
			if (nextChar == 'b' || nextChar == 'B')
			{
				return scanBinaryLiteral(start);
			}
			if (nextChar == 'x' || nextChar == 'X')
			{
				return scanHexLiteral(start);
			}
		}

		// Decimal number scanning - handles integers, longs, floats, doubles
		boolean hasDecimal = false;
		boolean hasExponent = false;

		while (position < source.length())
		{
			char ch = source.charAt(position);

			if (Character.isDigit(ch) || ch == '_')
			{
				++position;
			}
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
					case 'L', 'l' ->
					{
						++position;
					}
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
		{
			type = TokenType.LONG_LITERAL;
		}
		else if (text.endsWith("F") || text.endsWith("f"))
		{
			type = TokenType.FLOAT_LITERAL;
		}
		else if (text.endsWith("D") || text.endsWith("d") || hasDecimal)
		{
			type = TokenType.DOUBLE_LITERAL;
		}
		else
		{
			type = TokenType.INTEGER_LITERAL;
		}

		return new Token(type, start, position, text);
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
			{
				++position;
			}
			else
			{
				break;
			}
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
			{
				++position;
			}
			else
			{
				break;
			}
		}

		// Check for hexadecimal floating-point literal
		if (position < source.length() && source.charAt(position) == '.')
		{
			return scanHexFloatLiteral(start);
		}

		// Check for binary exponent (p or P)
		if (position < source.length() && (source.charAt(position) == 'p' || source.charAt(position) == 'P'))
		{
			return scanHexFloatLiteral(start);
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
				{
					++position;
				}
				else
				{
					break;
				}
			}
		}

		// Binary exponent is required for hexadecimal floating-point literals
		if (position < source.length() && (source.charAt(position) == 'p' || source.charAt(position) == 'P'))
		{
			++position;

			// Optional sign for exponent
			if (position < source.length() && (source.charAt(position) == '+' || source.charAt(position) == '-'))
			{
				++position;
			}

			// Decimal exponent digits
			while (position < source.length())
			{
				char ch = source.charAt(position);
				if (Character.isDigit(ch) || ch == '_')
				{
					++position;
				}
				else
				{
					break;
				}
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
		{
			return scanTextBlock(start);
		}

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
			{
				++position;
			}
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
			{
				throw new LexerException(
					"Text block opening delimiter must be followed by line terminator", position);
			}
			++position;
		}

		// Skip the newline
		if (position < source.length())
		{
			++position;
		}

		// Scan until closing """
		while (position + 2 < source.length())
		{
			if (source.charAt(position) == '"' &&
				source.charAt(position + 1) == '"' &&
				source.charAt(position + 2) == '"')
			{
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
			{
				++position;
			}
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
			{
				++position;
			}
		}

		if (position < source.length() && source.charAt(position) == '\'')
		{
			++position;
		}

		String text = source.substring(start, position);
		return new Token(TokenType.CHAR_LITERAL, start, position, text);
	}

	/**
	 * Consumes an escape sequence starting after the backslash.
	 * Handles standard escapes ({@code \n}, {@code \t}, etc.) and Unicode escapes (backslash-u plus 4 hex
	 * digits). The backslash has already been consumed when this method is called.
	 */
	private void consumeEscapeSequence()
	{
		if (position >= source.length())
			return;

		if (source.charAt(position) == 'u')
		{
			// Unicode escape: skip all 'u' chars (JLS allows multiple 'u' chars before hex digits)
			while (position < source.length() && source.charAt(position) == 'u')
			{
				++position;
			}
			// Skip up to 4 hex digits
			int hexCount = 0;
			while (position < source.length() && hexCount < 4 && isHexDigit(source.charAt(position)))
			{
				++position;
				++hexCount;
			}
		}
		else
		{
			// Standard escape: skip single character
			++position;
		}
	}

	private boolean isHexDigit(char ch)
	{
		return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
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
			case '(' -> TokenType.LPAREN;
			case ')' -> TokenType.RPAREN;
			case '{' -> TokenType.LBRACE;
			case '}' -> TokenType.RBRACE;
			case '[' -> TokenType.LBRACKET;
			case ']' -> TokenType.RBRACKET;
			case ';' -> TokenType.SEMICOLON;
			case ',' -> TokenType.COMMA;
			case '@' -> TokenType.AT;
			case '~' -> TokenType.TILDE;
			case '?' -> TokenType.QUESTION;
			case ':' ->
			{
				if (matchAndConsume(':'))
				{
					yield TokenType.DOUBLE_COLON;
				}
				yield TokenType.COLON;
			}
			case '.' ->
			{
				if (matchAndConsume('.') && matchAndConsume('.'))
				{
					yield TokenType.ELLIPSIS;
				}
				yield TokenType.DOT;
			}
			case '=' ->
			{
				if (matchAndConsume('='))
				{
					yield TokenType.EQ;
				}
				yield TokenType.ASSIGN;
			}
			case '!' ->
			{
				if (matchAndConsume('='))
				{
					yield TokenType.NE;
				}
				yield TokenType.NOT;
			}
			case '<' ->
			{
				if (matchAndConsume('='))
				{
					yield TokenType.LE;
				}
				if (matchAndConsume('<'))
				{
					if (matchAndConsume('='))
					{
						yield TokenType.LSHIFTASSIGN;
					}
					yield TokenType.LSHIFT;
				}
				yield TokenType.LT;
			}
			case '>' ->
			{
				yield scanGreaterThanOperator();
			}
			case '&' ->
			{
				if (matchAndConsume('&'))
				{
					yield TokenType.AND;
				}
				if (matchAndConsume('='))
				{
					yield TokenType.BITANDASSIGN;
				}
				yield TokenType.BITAND;
			}
			case '|' ->
			{
				if (matchAndConsume('|'))
				{
					yield TokenType.OR;
				}
				if (matchAndConsume('='))
				{
					yield TokenType.BITORASSIGN;
				}
				yield TokenType.BITOR;
			}
			case '+' ->
			{
				if (matchAndConsume('+'))
				{
					yield TokenType.INC;
				}
				if (matchAndConsume('='))
				{
					yield TokenType.PLUSASSIGN;
				}
				yield TokenType.PLUS;
			}
			case '-' ->
			{
				if (matchAndConsume('-'))
				{
					yield TokenType.DEC;
				}
				if (matchAndConsume('='))
				{
					yield TokenType.MINUSASSIGN;
				}
				if (matchAndConsume('>'))
				{
					yield TokenType.ARROW;
				}
				yield TokenType.MINUS;
			}
			case '*' ->
			{
				if (matchAndConsume('='))
				{
					yield TokenType.STARASSIGN;
				}
				yield TokenType.STAR;
			}
			case '/' ->
			{
				if (matchAndConsume('='))
				{
					yield TokenType.DIVASSIGN;
				}
				yield TokenType.DIV;
			}
			case '^' ->
			{
				if (matchAndConsume('='))
				{
					yield TokenType.CARETASSIGN;
				}
				yield TokenType.CARET;
			}
			case '%' ->
			{
				if (matchAndConsume('='))
				{
					yield TokenType.MODASSIGN;
				}
				yield TokenType.MOD;
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
			return TokenType.GE;
		if (matchAndConsume('>'))
			return scanRightShiftOperator();
		return TokenType.GT;
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
			return TokenType.RSHIFTASSIGN;
		return TokenType.RSHIFT;
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
			return TokenType.URSHIFTASSIGN;
		return TokenType.URSHIFT;
	}

	private char peek()
	{
		int nextPos = position + 1;
		if (nextPos < source.length())
		{
			return source.charAt(nextPos);
		}
		return '\0';
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
