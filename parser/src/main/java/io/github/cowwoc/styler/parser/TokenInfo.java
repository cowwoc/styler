package io.github.cowwoc.styler.parser;

/**
 * Immutable token information record.
 * Uses a record for memory efficiency and immutability.
 */
public record TokenInfo(
    TokenType type,
    int startOffset,
    int length,
    String text
) {
    public int endOffset() {
        return startOffset + length;
    }

    public boolean is(TokenType expectedType) {
        return type == expectedType;
    }

    @Override
    public String toString() {
        return String.format("Token[%s, %d-%d, '%s']",
            type, startOffset, endOffset(), text);
    }
}