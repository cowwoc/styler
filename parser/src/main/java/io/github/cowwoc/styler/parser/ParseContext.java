package io.github.cowwoc.styler.parser;

import java.util.List;

/**
 * Parse context for recursive descent parsing.
 * Manages current position in token stream and provides parsing utilities.
 */
public class ParseContext {
    private final List<TokenInfo> tokens;
    private final NodeRegistry nodeRegistry;
    private final String sourceText;
    private int currentTokenIndex = 0;

    public ParseContext(List<TokenInfo> tokens, NodeRegistry nodeRegistry, String sourceText) {
        this.tokens = tokens;
        this.nodeRegistry = nodeRegistry;
        this.sourceText = sourceText;
    }

    /**
     * Gets the current token without advancing.
     */
    public TokenInfo getCurrentToken() {
        if (currentTokenIndex >= tokens.size()) {
            return tokens.get(tokens.size() - 1); // Return EOF token
        }
        return tokens.get(currentTokenIndex);
    }

    /**
     * Peeks ahead at a token relative to current position.
     */
    public TokenInfo peekToken(int offset) {
        int index = currentTokenIndex + offset;
        if (index < 0 || index >= tokens.size()) {
            return tokens.get(tokens.size() - 1); // Return EOF token
        }
        return tokens.get(index);
    }

    /**
     * Advances to the next token and returns it.
     */
    public TokenInfo advance() {
        if (currentTokenIndex < tokens.size() - 1) {
            currentTokenIndex++;
        }
        return getCurrentToken();
    }

    /**
     * Checks if the current token is of the specified type.
     */
    public boolean currentTokenIs(TokenType type) {
        return getCurrentToken().type() == type;
    }

    /**
     * Expects a specific token type and advances if found.
     * Throws ParseException if not found.
     */
    public TokenInfo expect(TokenType expectedType) {
        TokenInfo current = getCurrentToken();
        if (current.type() != expectedType) {
            throw new IndexOverlayParser.ParseException(
                String.format("Expected %s but found %s at position %d",
                    expectedType, current.type(), current.startOffset()));
        }
        return advance();
    }

    /**
     * Checks if we're at the end of the token stream.
     */
    public boolean isAtEnd() {
        return getCurrentToken().type() == TokenType.EOF;
    }

    /**
     * Gets the current position in the source text.
     */
    public int getCurrentPosition() {
        return getCurrentToken().startOffset();
    }

    /**
     * Updates a node's length (used when end position is determined later).
     */
    public void updateNodeLength(int nodeId, int newLength) {
        nodeRegistry.updateNodeLength(nodeId, newLength);
    }

    /**
     * Gets the source text for debugging/error messages.
     */
    public String getSourceText() {
        return sourceText;
    }

    /**
     * Gets the token at a specific index.
     */
    public TokenInfo getToken(int index) {
        if (index < 0 || index >= tokens.size()) {
            return tokens.get(tokens.size() - 1); // Return EOF token
        }
        return tokens.get(index);
    }

    /**
     * Gets the current token index.
     */
    public int getCurrentTokenIndex() {
        return currentTokenIndex;
    }

    /**
     * Sets the current token index (for backtracking).
     */
    public void setCurrentTokenIndex(int index) {
        this.currentTokenIndex = Math.max(0, Math.min(index, tokens.size() - 1));
    }
}