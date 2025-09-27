package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.*;

/**
 * Strategy for parsing sealed class declarations introduced in Java 17.
 */
public class SealedClassStrategy implements ParseStrategy {

    @Override
    public boolean canHandle(JavaVersion version, ParseContext context) {
        return version.isAtLeast(JavaVersion.JAVA_17) &&
               context.currentTokenIs(TokenType.SEALED);
    }

    @Override
    public int parseConstruct(ParseContext context) {
        throw new UnsupportedOperationException(
            "Sealed class parsing for Java 17+ is not yet implemented. " +
            "Current parser supports basic Java constructs."
        );
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Sealed classes (Java 17+)";
    }
}