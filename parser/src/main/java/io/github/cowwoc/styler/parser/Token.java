package io.github.cowwoc.styler.parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Immutable token representing a lexical element in Java source code.
 * Tokens are lightweight and arena-independent, containing only essential information.
 * <p>
 * When the token contains Unicode escapes (per
 * <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.3">JLS &#167;3.3</a>),
 * {@code text} preserves the original escape sequences while {@code decodedText} contains the decoded
 * characters. For tokens without Unicode escapes, both fields reference the same {@code String} instance.
 *
 * @param type        the type of this token
 * @param start       the start position in source code (inclusive)
 * @param end         the end position in source code (exclusive)
 * @param text        the original text content of this token (may be {@code null} for structural tokens)
 * @param decodedText the decoded text content with Unicode escapes resolved (may be {@code null} for
 *                    structural tokens)
 */
public record Token(TokenType type, int start, int end, String text, String decodedText)
{
	/**
	 * Creates a new token with matching {@code text} and {@code decodedText}.
	 * <p>
	 * Use this constructor when the token contains no Unicode escapes. Both {@code text()} and
	 * {@code decodedText()} will return the same {@code String} instance.
	 *
	 * @param type  the type of this token
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @param text  the text content of this token (may be {@code null} for structural tokens)
	 * @throws NullPointerException     if {@code type} is {@code null}
	 * @throws IllegalArgumentException if {@code start} is negative or {@code end} is less than {@code start}
	 */
	public Token(TokenType type, int start, int end, String text)
	{
		this(type, start, end, text, text);
	}

	/**
	 * Creates a new token.
	 *
	 * @param type        the type of this token
	 * @param start       the start position in source code (inclusive)
	 * @param end         the end position in source code (exclusive)
	 * @param text        the original text content of this token (may be {@code null} for structural tokens)
	 * @param decodedText the decoded text content with Unicode escapes resolved (may be {@code null} for
	 *                    structural tokens)
	 * @throws NullPointerException     if {@code type} is {@code null}
	 * @throws IllegalArgumentException if {@code start} is negative or {@code end} is less than {@code start}
	 */
	public Token
	{
		requireThat(type, "type").isNotNull();
		if (start < 0)
		{
			throw new IllegalArgumentException("start must be non-negative, got: " + start);
		}
		if (end < start)
		{
			throw new IllegalArgumentException("end must be >= start, got: start=" + start + ", end=" + end);
		}
	}

	/**
	 * Returns the length of this token in characters.
	 *
	 * @return the token length
	 */
	public int length()
	{
		return end - start;
	}

	/**
	 * Checks if this token is of the specified type.
	 *
	 * @param tokenType the type to check against
	 * @return true if this token's type matches the specified type
	 */
	public boolean is(TokenType tokenType)
	{
		return type == tokenType;
	}

	/**
	 * Checks if this token is a keyword.
	 *
	 * @return true if this token is a Java keyword
	 */
	public boolean isKeyword()
	{
		return type.ordinal() >= TokenType.ABSTRACT.ordinal() &&
			type.ordinal() <= TokenType.WHILE.ordinal();
	}

	/**
	 * Checks if this token is a literal.
	 *
	 * @return true if this token is a literal value
	 */
	public boolean isLiteral()
	{
		return type.ordinal() >= TokenType.INTEGER_LITERAL.ordinal() &&
			type.ordinal() <= TokenType.NULL_LITERAL.ordinal();
	}

	/**
	 * Checks if this token is an operator.
	 *
	 * @return true if this token is an operator
	 */
	public boolean isOperator()
	{
		return type.ordinal() >= TokenType.ASSIGN.ordinal() &&
			type.ordinal() <= TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN.ordinal();
	}

	@Override
	public String toString()
	{
		if (text != null)
		{
			return type + "[" + start + ":" + end + ", \"" + text + "\"]";
		}
		return type + "[" + start + ":" + end + "]";
	}
}
