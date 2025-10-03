package io.github.cowwoc.styler.parser;

/**
 * Immutable token information record.
 * Uses a record for memory efficiency and immutability.
 *
 * @param type the token {@code type}
 * @param startOffset the starting offset in the source
 * @param length the {@code length} of the token in characters
 * @param text the actual {@code text} content of the token
 */
public record TokenInfo(
	TokenType type,
	int startOffset,
	int length,
	String text)
{
	/**
	 * Calculates the end offset of this token in the source text.
	 *
	 * @return the end offset (startOffset + length)
	 */
	public int endOffset()
{
		return startOffset + length;
	}

	/**
	 * Checks if this token matches the specified type.
	 *
	 * @param expectedType the expected token type
	 * @return {@code true} if this token's type matches {@code expectedType}, {@code false} otherwise
	 */
	public boolean is(TokenType expectedType)
{
		return type == expectedType;
	}

	@Override
	public String toString()
{
		return String.format("Token[%s, %d-%d, '%s']",
			type, startOffset, endOffset(), text);
	}
}