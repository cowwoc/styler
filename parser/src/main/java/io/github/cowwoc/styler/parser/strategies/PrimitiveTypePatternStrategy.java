package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.*;

/**
 * Strategy for parsing primitive type patterns introduced in Java 25.
 */
public class PrimitiveTypePatternStrategy implements ParseStrategy {

    @Override
    public boolean canHandle(JavaVersion version, ParseContext context) {
        return version.isAtLeast(JavaVersion.JAVA_25) &&
               isPrimitiveTypePattern(context);
    }

    @Override
    public int parseConstruct(ParseContext context) {
        throw new UnsupportedOperationException(
            "Primitive type pattern parsing for Java 25+ is not yet implemented. " +
            "Current parser supports basic Java constructs."
        );
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Primitive type patterns (Java 25+)";
    }

    private boolean isPrimitiveTypePattern(ParseContext context) {
        // Feature not yet implemented - return false to indicate no match
        return false;
    }
}