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
    private TokenInfo pendingToken = null; // For injected tokens (e.g., splitting >> into > >)

    // Security: Recursion depth protection against stack overflow attacks
    private static final int MAX_RECURSION_DEPTH = 1000;
    private int currentRecursionDepth = 0;

    public ParseContext(List<TokenInfo> tokens, NodeRegistry nodeRegistry, String sourceText) {
        this.tokens = tokens;
        this.nodeRegistry = nodeRegistry;
        this.sourceText = sourceText;
    }

    /**
     * Gets the current token without advancing.
     */
    public TokenInfo getCurrentToken() {
        if (pendingToken != null) {
            return pendingToken;
        }
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
        if (pendingToken != null) {
            pendingToken = null; // Consume the pending token
            return getCurrentToken();
        }
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
            // Usability: Provide helpful error message with source position
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
     * Gets the current token index.
     */
    public int getCurrentTokenIndex() {
        return currentTokenIndex;
    }

    /**
     * Sets the current token index position (for backtracking during lookahead).
     */
    public void setPosition(int tokenIndex) {
        this.currentTokenIndex = tokenIndex;
        this.pendingToken = null; // Clear any pending token
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
     * Peeks at the next token without advancing.
     */
    public TokenInfo peekNextToken() {
        return peekToken(1);
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
     * Injects a virtual token to be returned by the next getCurrentToken() call.
     * Used for splitting compound tokens like >> into > >.
     */
    public void injectToken(TokenInfo token) {
        this.pendingToken = token;
    }

    /**
     * Enters a recursive parsing method. Increments depth and checks for stack overflow.
     *
     * @throws ParseException if recursion depth exceeds maximum allowed
     */
    public void enterRecursion() {
        currentRecursionDepth++;
        if (currentRecursionDepth > MAX_RECURSION_DEPTH) {
            throw new IndexOverlayParser.ParseException(
                "Maximum recursion depth exceeded (" + MAX_RECURSION_DEPTH + "). " +
                "Input may contain excessively nested expressions that could cause stack overflow."
            );
        }
    }

    /**
     * Exits a recursive parsing method. Decrements depth.
     */
    public void exitRecursion() {
        if (currentRecursionDepth > 0) {
            currentRecursionDepth--;
        }
    }

    /**
     * Gets the current recursion depth for monitoring purposes.
     */
    public int getCurrentRecursionDepth() {
        return currentRecursionDepth;
    }

    /**
     * Gets the node registry for creating AST nodes.
     */
    public NodeRegistry getNodeRegistry() {
        return nodeRegistry;
    }
}