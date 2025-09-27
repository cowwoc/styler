package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.JavaLexer;
import io.github.cowwoc.styler.parser.TokenInfo;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for scientific notation lexing bugs discovered during enum parsing.
 */
public class ScientificNotationLexerTest {

    @Test(description = "Scientific notation should be tokenized as DOUBLE_LITERAL, not DOUBLE keyword")
    public void testScientificNotationLexing() {
        // Minimal test case that reproduces the exact bug
        String source = "3.303e+23";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token = lexer.nextToken();

        // Bug: lexer produces DOUBLE instead of DOUBLE_LITERAL
        assertEquals(token.type(), TokenType.DOUBLE_LITERAL,
            "Scientific notation should be lexed as DOUBLE_LITERAL");
        assertEquals(token.text(), "3.303e+23",
            "Token text should match the scientific notation");
    }

    @Test(description = "Floating point literals with decimal should be DOUBLE_LITERAL")
    public void testDecimalLiterals() {
        String source = "3.303";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token = lexer.nextToken();

        assertEquals(token.type(), TokenType.DOUBLE_LITERAL);
        assertEquals(token.text(), "3.303");
    }

    @Test(description = "Float literals with f suffix should be FLOAT_LITERAL")
    public void testFloatSuffix() {
        String source = "3.303f";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token = lexer.nextToken();

        assertEquals(token.type(), TokenType.FLOAT_LITERAL);
        assertEquals(token.text(), "3.303f");
    }
}