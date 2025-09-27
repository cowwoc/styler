package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.*;

/**
 * Strategy for parsing switch expressions introduced in Java 14.
 */
public class SwitchExpressionStrategy implements ParseStrategy {

    @Override
    public boolean canHandle(JavaVersion version, ParseContext context) {
        // Switch expressions are available from Java 14+
        if (!version.isAtLeast(JavaVersion.JAVA_14)) {
            return false;
        }

        // Check if we're at a switch keyword in an expression context
        return context.currentTokenIs(TokenType.SWITCH) && isExpressionContext(context);
    }

    @Override
    public int parseConstruct(ParseContext context) {
        int startPos = context.getCurrentPosition();

        context.expect(TokenType.SWITCH);
        context.expect(TokenType.LPAREN);

        // Parse switch expression - basic implementation for Java 14+ switch expressions

        context.expect(TokenType.RPAREN);
        context.expect(TokenType.LBRACE);

        // Parse switch expression body
        while (!context.currentTokenIs(TokenType.RBRACE) && !context.isAtEnd()) {
            parseSwitchExpressionCase(context);
        }

        context.expect(TokenType.RBRACE);

        int endPos = context.getCurrentPosition();

        // Use NodeRegistry to create the node (assuming access to registry)
        // In a full implementation, this would be properly integrated
        return context.getNodeRegistry().allocateNode(NodeType.SWITCH_EXPRESSION, startPos, endPos - startPos, -1);
    }

    @Override
    public int getPriority() {
        return 10; // High priority for Java 14+ features
    }

    @Override
    public String getDescription() {
        return "Switch expressions (Java 14+)";
    }

    /**
     * Determines if we're in an expression context where switch expressions are valid.
     */
    private boolean isExpressionContext(ParseContext context) {
        // This is a simplified check - in practice, would need more sophisticated context analysis
        TokenInfo previous = context.peekToken(-1);
        return previous != null && (
            previous.type() == TokenType.ASSIGN ||
            previous.type() == TokenType.LPAREN ||
            previous.type() == TokenType.RETURN ||
            previous.type() == TokenType.COMMA
        );
    }

    /**
     * Parses a single case in a switch expression.
     */
    private void parseSwitchExpressionCase(ParseContext context) {
        // Simplified case parsing - would need full implementation
        if (context.currentTokenIs(TokenType.CASE)) {
            context.advance();
            // Parse case pattern/value
            while (!context.currentTokenIs(TokenType.ARROW) && !context.currentTokenIs(TokenType.COLON)) {
                context.advance();
            }
            context.advance(); // consume -> or :
            // Parse case body
            if (!context.currentTokenIs(TokenType.CASE) && !context.currentTokenIs(TokenType.DEFAULT)) {
                context.advance(); // consume expression or statement
            }
        } else if (context.currentTokenIs(TokenType.DEFAULT)) {
            context.advance();
            context.advance(); // consume -> or :
            context.advance(); // consume expression or statement
        }
    }
}