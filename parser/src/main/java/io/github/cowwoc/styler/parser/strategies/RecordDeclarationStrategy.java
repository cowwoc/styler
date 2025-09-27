package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.*;

/**
 * Strategy for parsing record declarations introduced in Java 16.
 */
public class RecordDeclarationStrategy implements ParseStrategy {

    @Override
    public boolean canHandle(JavaVersion version, ParseContext context) {
        return version.isAtLeast(JavaVersion.JAVA_16) &&
               context.currentTokenIs(TokenType.RECORD);
    }

    @Override
    public int parseConstruct(ParseContext context) {
        throw new UnsupportedOperationException(
            "Record declaration parsing for Java 16+ is not yet implemented. " +
            "Current parser supports basic Java constructs."
        );
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Record declarations (Java 16+)";
    }
}