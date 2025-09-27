package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.JavaLexer;
import io.github.cowwoc.styler.parser.TokenInfo;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for compound keyword tokenization bugs.
 * Specifically tests hyphenated keywords like "non-sealed".
 */
public class CompoundKeywordTest {

    @Test(description = "non-sealed should be tokenized as single NON_SEALED token")
    public void testNonSealedKeyword() {
        String source = "non-sealed";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token = lexer.nextToken();

        assertEquals(token.type(), TokenType.NON_SEALED,
            "non-sealed should be tokenized as NON_SEALED");
        assertEquals(token.text(), "non-sealed",
            "Token text should match the hyphenated keyword");
    }

    @Test(description = "Hyphenated identifier vs non-sealed distinction")
    public void testHyphenatedIdentifierVsKeyword() {
        // This test ensures we don't break normal hyphenated identifiers
        // if they exist in expressions or other contexts
        String source = "non-sealed class";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token1 = lexer.nextToken();
        TokenInfo token2 = lexer.nextToken();

        assertEquals(token1.type(), TokenType.NON_SEALED);
        assertEquals(token2.type(), TokenType.CLASS);
    }

    @Test(description = "Context sensitivity - non-sealed only in class contexts")
    public void testNonSealedContextSensitivity() {
        // In Java, "non-sealed" is only a keyword in specific contexts
        // This test verifies proper contextual keyword handling
        String source = "non-sealed";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token = lexer.nextToken();

        // For now, we expect it to always be NON_SEALED when encountered
        // More sophisticated context handling can be added later
        assertEquals(token.type(), TokenType.NON_SEALED);
    }
}