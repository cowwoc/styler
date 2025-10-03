package io.github.cowwoc.styler.parser;

/**
 * High-performance Java lexer implementing tokenization for JDK 25 features.
 *
 * This is a simplified implementation focusing on the core architecture.
 * A full implementation would include:
 * - Complete Unicode support
 * - All JDK 25 language features
 * - Comprehensive error recovery
 * - String template processing
 *
 * The design prioritizes performance through:
 * - Single-pass scanning
 * - Minimal object allocation
 * - Efficient character classification
 */
public class JavaLexer
{
	private final String source;
	private final int length;
	private int position;

	/**
	 * Creates a Java lexer for the specified source code.
	 *
	 * @param source the Java source code to tokenize
	 */
	public JavaLexer(String source)
	{
		this.source = source;
		this.length = source.length();
	}

	/**
	 * Returns the next token from the input stream.
	 *
	 * @return the next {@link TokenInfo} object representing the scanned token
	 */
	public TokenInfo nextToken()
	{
		long startTime;
		if (METRICS_ENABLED)
		{
			startTime = System.nanoTime();
		}
		else
		{
			startTime = 0;
		}

		skipWhitespace();

		if (position >= length)
		{
			return createToken(TokenType.EOF, position, 0, "");
		}

		char ch = source.charAt(position);

		TokenInfo token = switch (ch)
		{
			case '(' -> singleCharToken(TokenType.LPAREN);
			case ')' -> singleCharToken(TokenType.RPAREN);
			case '{' -> singleCharToken(TokenType.LBRACE);
			case '}' -> singleCharToken(TokenType.RBRACE);
			case '[' -> singleCharToken(TokenType.LBRACKET);
			case ']' -> singleCharToken(TokenType.RBRACKET);
			case ';' -> singleCharToken(TokenType.SEMICOLON);
			case ',' -> singleCharToken(TokenType.COMMA);
			case '@' -> singleCharToken(TokenType.AT);
			case '?' -> singleCharToken(TokenType.QUESTION);
			case ':' -> scanColon();
			case '~' -> singleCharToken(TokenType.BITWISE_NOT);

			// Multi-character operators and punctuation
			case '.' -> scanDotOrEllipsis();
			case '+' -> scanPlus();
			case '-' -> scanMinus();
			case '*' -> scanStar();
			case '/' -> scanSlashOrComment();
			case '%' -> scanPercent();
			case '=' -> scanEquals();
			case '!' -> scanExclamation();
			case '<' -> scanLessThan();
			case '>' -> scanGreaterThan();
			case '&' -> scanAmpersand();
			case '|' -> scanPipe();
			case '^' -> scanCaret();

			// String literals and text blocks
			case '"' -> scanStringOrTextBlock();
			case '\'' -> scanCharacterLiteral();

			// Numbers
			case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> scanNumber();

			// Identifiers and keywords
			default ->
			{
			    if (Character.isJavaIdentifierStart(ch))
			    {
			        yield scanIdentifierOrKeyword();
			    }
			    else
			    {
			        // Unknown character - create error token
			        ParseMetrics.recordParseError(false);
			        yield singleCharToken(TokenType.ERROR);
			    }
			}
		};

		if (METRICS_ENABLED && startTime > 0)
		{
			ParseMetrics.recordTokenizationTime(System.nanoTime() - startTime, 1);
		}

		return token;
	}

	private void skipWhitespace()
	{
		while (position < length && Character.isWhitespace(source.charAt(position)))
		{
			if (source.charAt(position) == '\n')
			{
			}
			else
			{
			}
			++position;
		}
	}

	private TokenInfo singleCharToken(TokenType type)
	{
		char ch = source.charAt(position);
		++position;
		return createToken(type, position - 1, 1, String.valueOf(ch));
	}

	private TokenInfo scanDotOrEllipsis()
	{
		int start = position;
		++position; // consume '.'

		if (position < length - 1 &&
			source.charAt(position) == '.' &&
			source.charAt(position + 1) == '.')
			{
			position += 2; // consume '..'
			return createToken(TokenType.ELLIPSIS, start, 3, "...");
		}

		return createToken(TokenType.DOT, start, 1, ".");
	}

	private TokenInfo scanPlus()
	{
		int start = position;
		++position; // consume '+'

		if (position < length)
		{
			char next = source.charAt(position);
			if (next == '+')
			{
			    ++position;
			    return createToken(TokenType.INCREMENT, start, 2, "++");
			}
			if (next == '=')
			{
			    ++position;
			    return createToken(TokenType.PLUS_ASSIGN, start, 2, "+=");
			}
		}

		return createToken(TokenType.PLUS, start, 1, "+");
	}

	private TokenInfo scanMinus()
	{
		int start = position;
		++position; // consume '-'

		if (position < length)
		{
			char next = source.charAt(position);
			if (next == '-')
			{
			    ++position;
			    return createToken(TokenType.DECREMENT, start, 2, "--");
			}
			if (next == '=')
			{
			    ++position;
			    return createToken(TokenType.MINUS_ASSIGN, start, 2, "-=");
			}
			if (next == '>')
			{
			    ++position;
			    return createToken(TokenType.ARROW, start, 2, "->");
			}
		}

		return createToken(TokenType.MINUS, start, 1, "-");
	}

	private TokenInfo scanStar()
	{
		int start = position;
		++position; // consume '*'

		if (position < length && source.charAt(position) == '=')
		{
			++position;
			return createToken(TokenType.MULT_ASSIGN, start, 2, "*=");
		}

		return createToken(TokenType.MULT, start, 1, "*");
	}

	private TokenInfo scanSlashOrComment()
	{
		int start = position;
		++position; // consume '/'

		if (position < length)
		{
			char next = source.charAt(position);
			if (next == '/')
			{
			    return scanLineComment(start);
			}
			if (next == '*')
			{
			    return scanBlockComment(start);
			}
			if (next == '=')
			{
			    ++position;
			    return createToken(TokenType.DIV_ASSIGN, start, 2, "/=");
			}
		}

		return createToken(TokenType.DIV, start, 1, "/");
	}

	private TokenInfo scanLineComment(int start)
	{
		// Skip until end of line
		while (position < length && source.charAt(position) != '\n')
		{
			++position;
		}

		String text = source.substring(start, position);
		return createToken(TokenType.LINE_COMMENT, start, text.length(), text);
	}

	private TokenInfo scanBlockComment(int start)
	{
		++position; // consume '*'

		boolean isJavadoc = position < length && source.charAt(position) == '*';

		// Scan until */
		while (position < length - 1)
		{
			if (source.charAt(position) == '*' && source.charAt(position + 1) == '/')
			{
			    position += 2; // consume '*/'
			    break;
			}

			if (source.charAt(position) == '\n')
			{
			}
			else
			{
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
		return createToken(type, start, text.length(), text);
	}

	private TokenInfo scanPercent()
	{
		int start = position;
		++position; // consume '%'

		if (position < length && source.charAt(position) == '=')
		{
			++position;
			return createToken(TokenType.MOD_ASSIGN, start, 2, "%=");
		}

		return createToken(TokenType.MOD, start, 1, "%");
	}

	private TokenInfo scanEquals()
	{
		int start = position;
		++position; // consume '='

		if (position < length && source.charAt(position) == '=')
		{
			++position;
			return createToken(TokenType.EQ, start, 2, "==");
		}

		return createToken(TokenType.ASSIGN, start, 1, "=");
	}

	private TokenInfo scanExclamation()
	{
		int start = position;
		++position; // consume '!'

		if (position < length && source.charAt(position) == '=')
		{
			++position;
			return createToken(TokenType.NE, start, 2, "!=");
		}

		return createToken(TokenType.LOGICAL_NOT, start, 1, "!");
	}

	private TokenInfo scanLessThan()
	{
		int start = position;
		++position; // consume '<'

		if (position < length)
		{
			char next = source.charAt(position);
			if (next == '=')
			{
			    ++position;
			    return createToken(TokenType.LE, start, 2, "<=");
			}
			if (next == '<')
			{
			    ++position;
			    if (position < length && source.charAt(position) == '=')
			    {
			        ++position;
			        return createToken(TokenType.LSHIFT_ASSIGN, start, 3, "<<=");
			    }
			    return createToken(TokenType.LSHIFT, start, 2, "<<");
			}
		}

		return createToken(TokenType.LT, start, 1, "<");
	}

	private TokenInfo scanGreaterThan()
	{
		int start = position;
		++position; // consume '>'

		if (position < length)
		{
			char next = source.charAt(position);
			if (next == '=')
			{
			    ++position;
			    return createToken(TokenType.GE, start, 2, ">=");
			}
			if (next == '>')
			{
			    ++position;
			    if (position < length)
			    {
			        char next2 = source.charAt(position);
			        if (next2 == '>')
			        {
			            ++position;
			            if (position < length && source.charAt(position) == '=')
			            {
			                ++position;
			                return createToken(TokenType.URSHIFT_ASSIGN, start, 4, ">>>=");
			            }
			            return createToken(TokenType.URSHIFT, start, 3, ">>>");
			        }
			        if (next2 == '=')
			        {
			            ++position;
			            return createToken(TokenType.RSHIFT_ASSIGN, start, 3, ">>=");
			        }
			    }
			    return createToken(TokenType.RSHIFT, start, 2, ">>");
			}
		}

		return createToken(TokenType.GT, start, 1, ">");
	}

	private TokenInfo scanAmpersand()
	{
		int start = position;
		++position; // consume '&'

		if (position < length)
		{
			char next = source.charAt(position);
			if (next == '&')
			{
			    ++position;
			    return createToken(TokenType.LOGICAL_AND, start, 2, "&&");
			}
			if (next == '=')
			{
			    ++position;
			    return createToken(TokenType.AND_ASSIGN, start, 2, "&=");
			}
		}

		return createToken(TokenType.BITWISE_AND, start, 1, "&");
	}

	private TokenInfo scanPipe()
	{
		int start = position;
		++position; // consume '|'

		if (position < length)
		{
			char next = source.charAt(position);
			if (next == '|')
			{
			    ++position;
			    return createToken(TokenType.LOGICAL_OR, start, 2, "||");
			}
			if (next == '=')
			{
			    ++position;
			    return createToken(TokenType.OR_ASSIGN, start, 2, "|=");
			}
		}

		return createToken(TokenType.BITWISE_OR, start, 1, "|");
	}

	private TokenInfo scanCaret()
	{
		int start = position;
		++position; // consume '^'

		if (position < length && source.charAt(position) == '=')
		{
			++position;
			return createToken(TokenType.XOR_ASSIGN, start, 2, "^=");
		}

		return createToken(TokenType.BITWISE_XOR, start, 1, "^");
	}

	private TokenInfo scanColon()
	{
		int start = position;
		++position; // consume first ':'

		if (position < length && source.charAt(position) == ':')
		{
			++position; // consume second ':'
			return createToken(TokenType.DOUBLE_COLON, start, 2, "::");
		}

		return createToken(TokenType.COLON, start, 1, ":");
	}

	/**
	 * Scan either a regular string literal or a text block.
	 * Text blocks start with """ and are a JDK 15+ feature.
	 *
	 * @return the scanned string or text block token
	 */
	private TokenInfo scanStringOrTextBlock()
	{
		// Check if this is a text block (""")
		if (position + 2 < length &&
			source.charAt(position) == '"' &&
			source.charAt(position + 1) == '"' &&
			source.charAt(position + 2) == '"')
			{
			return scanTextBlock();
		}
		return scanStringLiteral();
	}

	/**
	 * Scan a text block literal (JDK 15+).
	 *
	 * @return the scanned text block token
	 */
	private TokenInfo scanTextBlock()
	{
		int start = position;
		position += 3; // consume opening """

		StringBuilder sb = new StringBuilder();
		sb.append("\"\"\"");

		while (position < length)
		{
			char ch = source.charAt(position);

			// Check for closing """
			if (ch == '"' && position + 2 < length &&
			    source.charAt(position + 1) == '"' &&
			    source.charAt(position + 2) == '"')
			    {
			    // Consume closing """
			    sb.append("\"\"\"");
			    position += 3;
			    break;
			}

			sb.append(ch);
			if (ch == '\n')
			{
			}
			else if (ch == '\r')
			{
			    // Handle \r\n
			    if (position + 1 < length && source.charAt(position + 1) == '\n')
			    {
			        ++position;
			        sb.append('\n');
			    }
			}
			else
			{
			}
			++position;
		}

		return createToken(TokenType.TEXT_BLOCK_LITERAL, start, sb.length(), sb.toString());
	}

	private TokenInfo scanStringLiteral()
	{
		int start = position;
		++position; // consume opening quote

		StringBuilder sb = new StringBuilder();
		sb.append('"');

		while (position < length)
		{
			char ch = source.charAt(position);

			if (ch == '"')
			{
			    ++position; // consume closing quote
			    sb.append('"');
			    break;
			}
			else if (ch == '\\')
			{
			    // Handle escape sequences
			    sb.append(ch);
			    ++position;
			    if (position < length)
			    {
			        sb.append(source.charAt(position));
			        ++position;
			    }
			}
			else
			{
			    sb.append(ch);
			    if (ch == '\n')
			    {
			    }
			    else
			    {
			    }
			    ++position;
			}
		}

		return createToken(TokenType.STRING_LITERAL, start, sb.length(), sb.toString());
	}

	private TokenInfo scanCharacterLiteral()
	{
		int start = position;
		++position; // consume opening quote

		StringBuilder sb = new StringBuilder();
		sb.append('\'');

		while (position < length && source.charAt(position) != '\'')
		{
			char ch = source.charAt(position);
			sb.append(ch);

			if (ch == '\\' && position + 1 < length)
			{
			    ++position;
			    sb.append(source.charAt(position));
			}

			++position;
		}

		if (position < length)
		{
			++position; // consume closing quote
			sb.append('\'');
		}

		return createToken(TokenType.CHARACTER_LITERAL, start, sb.length(), sb.toString());
	}

	private TokenInfo scanNumber()
	{
		int start = position;
		boolean isFloatingPoint = false;

		// Scan integer part
		while (position < length && Character.isDigit(source.charAt(position)))
		{
			++position;
		}

		// Check for decimal point
		if (position < length && source.charAt(position) == '.' &&
			position + 1 < length && Character.isDigit(source.charAt(position + 1)))
			{
			isFloatingPoint = true;
			++position; // consume '.'

			// Scan fractional part
			while (position < length && Character.isDigit(source.charAt(position)))
			{
			    ++position;
			}
		}

		// Check for scientific notation (e or E)
		if (position < length && (source.charAt(position) == 'e' || source.charAt(position) == 'E'))
		{
			isFloatingPoint = true;
			++position; // consume 'e' or 'E'

			// Check for optional + or -
			if (position < length && (source.charAt(position) == '+' || source.charAt(position) == '-'))
			{
			    ++position;
			}

			// Scan exponent digits
			while (position < length && Character.isDigit(source.charAt(position)))
			{
			    ++position;
			}
		}

		// Check for suffixes
		if (position < length)
		{
			char ch = source.charAt(position);
			if (ch == 'L' || ch == 'l')
			{
			    if (isFloatingPoint)
			    {
			        // Invalid: floating-point with long suffix
			        String text = source.substring(start, position);
			        return createToken(TokenType.DOUBLE_LITERAL, start, text.length(), text);
			    }
			    ++position;
			    String text = source.substring(start, position);
			    return createToken(TokenType.LONG_LITERAL, start, text.length(), text);
			}
			if (ch == 'f' || ch == 'F')
			{
			    isFloatingPoint = true;
			    ++position;
			}
			else if (ch == 'd' || ch == 'D')
			{
			    isFloatingPoint = true;
			    ++position;
			}
		}

		String text = source.substring(start, position);
		if (isFloatingPoint)
		{
			// Determine if it's float or double based on suffix
			if (text.endsWith("f") || text.endsWith("F"))
			{
			    return createToken(TokenType.FLOAT_LITERAL, start, text.length(), text);
			}
			return createToken(TokenType.DOUBLE_LITERAL, start, text.length(), text);
		}
		return createToken(TokenType.INTEGER_LITERAL, start, text.length(), text);
	}

	private TokenInfo scanIdentifierOrKeyword()
	{
		int start = position;

		// Scan identifier characters
		while (position < length && Character.isJavaIdentifierPart(source.charAt(position)))
		{
			++position;
		}

		String text = source.substring(start, position);

		// Check for compound keyword "non-sealed"
		if ("non".equals(text) && position < length - 6 &&
			source.substring(position, position + 7).equals("-sealed"))
		{
			// Consume the "-sealed" part
			position += 7;
			text = "non-sealed";
		}

		TokenType type = getKeywordType(text);

		return createToken(type, start, text.length(), text);
	}

	private TokenType getKeywordType(String text)
	{
		return switch (text)
		{
			case "abstract" -> TokenType.ABSTRACT;
			case "assert" -> TokenType.ASSERT;
			case "boolean" -> TokenType.BOOLEAN;
			case "break" -> TokenType.BREAK;
			case "byte" -> TokenType.BYTE;
			case "case" -> TokenType.CASE;
			case "catch" -> TokenType.CATCH;
			case "char" -> TokenType.CHAR;
			case "class" -> TokenType.CLASS;
			case "const" -> TokenType.CONST;
			case "continue" -> TokenType.CONTINUE;
			case "default" -> TokenType.DEFAULT;
			case "do" -> TokenType.DO;
			case "double" -> TokenType.DOUBLE;
			case "else" -> TokenType.ELSE;
			case "enum" -> TokenType.ENUM;
			case "extends" -> TokenType.EXTENDS;
			case "final" -> TokenType.FINAL;
			case "finally" -> TokenType.FINALLY;
			case "float" -> TokenType.FLOAT;
			case "for" -> TokenType.FOR;
			case "goto" -> TokenType.GOTO;
			case "if" -> TokenType.IF;
			case "implements" -> TokenType.IMPLEMENTS;
			case "import" -> TokenType.IMPORT;
			case "instanceof" -> TokenType.INSTANCEOF;
			case "int" -> TokenType.INT;
			case "interface" -> TokenType.INTERFACE;
			case "long" -> TokenType.LONG;
			case "native" -> TokenType.NATIVE;
			case "new" -> TokenType.NEW;
			case "package" -> TokenType.PACKAGE;
			case "private" -> TokenType.PRIVATE;
			case "protected" -> TokenType.PROTECTED;
			case "public" -> TokenType.PUBLIC;
			case "return" -> TokenType.RETURN;
			case "short" -> TokenType.SHORT;
			case "static" -> TokenType.STATIC;
			case "strictfp" -> TokenType.STRICTFP;
			case "super" -> TokenType.SUPER;
			case "switch" -> TokenType.SWITCH;
			case "synchronized" -> TokenType.SYNCHRONIZED;
			case "this" -> TokenType.THIS;
			case "throw" -> TokenType.THROW;
			case "throws" -> TokenType.THROWS;
			case "transient" -> TokenType.TRANSIENT;
			case "try" -> TokenType.TRY;
			case "void" -> TokenType.VOID;
			case "volatile" -> TokenType.VOLATILE;
			case "while" -> TokenType.WHILE;

			// JDK 9+ module keywords
			case "module" -> TokenType.MODULE;
			case "requires" -> TokenType.REQUIRES;
			case "exports" -> TokenType.EXPORTS;
			case "opens" -> TokenType.OPENS;
			case "to" -> TokenType.TO;
			case "uses" -> TokenType.USES;
			case "provides" -> TokenType.PROVIDES;
			case "with" -> TokenType.WITH;
			case "transitive" -> TokenType.TRANSITIVE;

			// JDK 10+ var (contextual keyword)
			case "var" -> TokenType.VAR;

			// JDK 14+ yield
			case "yield" -> TokenType.YIELD;

			// JDK 16+ record
			case "record" -> TokenType.RECORD;

			// JDK 17+ sealed classes
			case "sealed" -> TokenType.SEALED;
			case "non-sealed" -> TokenType.NON_SEALED;
			case "permits" -> TokenType.PERMITS;

			// JDK 21+ when (pattern matching)
			case "when" -> TokenType.WHEN;

			// Literals
			case "true", "false" -> TokenType.BOOLEAN_LITERAL;
			case "null" -> TokenType.NULL_LITERAL;

			default -> TokenType.IDENTIFIER;
		};
	}

	private TokenInfo createToken(TokenType type, int start, int length, String text)
	{
		return new TokenInfo(type, start, length, text);
	}

	private static final boolean METRICS_ENABLED =
		Boolean.parseBoolean(System.getProperty("styler.metrics.enabled", "false"));
}