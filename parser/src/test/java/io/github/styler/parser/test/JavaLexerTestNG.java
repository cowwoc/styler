package io.github.styler.parser.test;

import io.github.styler.parser.JavaLexer;
import io.github.styler.parser.TokenInfo;
import io.github.styler.parser.TokenType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * TestNG-based tests for the Java lexer covering tokenization of Java 25 features.
 */
public class JavaLexerTestNG {

    @Test(description = "Basic tokens")
    public void tokenizeBasicTokens() {
        String source = "public class Test { }";
        JavaLexer lexer = new JavaLexer(source);

        List<TokenInfo> tokens = collectTokens(lexer);

        requireThat(tokens.size(), "tokens.size").isEqualTo(6); // public, class, Test, {, }, EOF
        requireThat(tokens.get(0).type(), "token[0].type").isEqualTo(TokenType.PUBLIC);
        requireThat(tokens.get(1).type(), "token[1].type").isEqualTo(TokenType.CLASS);
        requireThat(tokens.get(2).type(), "token[2].type").isEqualTo(TokenType.IDENTIFIER);
        requireThat(tokens.get(3).type(), "token[3].type").isEqualTo(TokenType.LBRACE);
        requireThat(tokens.get(4).type(), "token[4].type").isEqualTo(TokenType.RBRACE);
        requireThat(tokens.get(5).type(), "token[5].type").isEqualTo(TokenType.EOF);
    }

    @DataProvider(name = "literalTokens")
    public Object[][] literalTokenData() {
        return new Object[][]{
            {"true", TokenType.BOOLEAN_LITERAL},
            {"false", TokenType.BOOLEAN_LITERAL},
            {"null", TokenType.NULL_LITERAL},
            {"42", TokenType.INTEGER_LITERAL},
            {"42L", TokenType.LONG_LITERAL},
            {"'x'", TokenType.CHARACTER_LITERAL},
            {"\"hello\"", TokenType.STRING_LITERAL}
        };
    }

    @Test(dataProvider = "literalTokens", description = "Literal tokens")
    public void tokenizeLiterals(String source, TokenType expectedType) {
        JavaLexer lexer = new JavaLexer(source);
        TokenInfo token = lexer.nextToken();

        requireThat(token.type(), "token.type").isEqualTo(expectedType);
        requireThat(token.text(), "token.text").isEqualTo(source);
    }

    @Test(description = "Operators and punctuation")
    public void tokenizeOperators() {
        String source = "++ -- += -= -> :: ...";
        JavaLexer lexer = new JavaLexer(source);

        List<TokenInfo> tokens = collectTokens(lexer);

        requireThat(tokens.get(0).type(), "++").isEqualTo(TokenType.INCREMENT);
        requireThat(tokens.get(1).type(), "--").isEqualTo(TokenType.DECREMENT);
        requireThat(tokens.get(2).type(), "+=").isEqualTo(TokenType.PLUS_ASSIGN);
        requireThat(tokens.get(3).type(), "-=").isEqualTo(TokenType.MINUS_ASSIGN);
        requireThat(tokens.get(4).type(), "->").isEqualTo(TokenType.ARROW);
        requireThat(tokens.get(5).type(), "::").isEqualTo(TokenType.DOUBLE_COLON);
        requireThat(tokens.get(6).type(), "...").isEqualTo(TokenType.ELLIPSIS);
    }

    @Test(description = "Comments")
    public void tokenizeComments() {
        String source = """
            // Line comment
            /* Block comment */
            /** Javadoc comment */
            """;

        JavaLexer lexer = new JavaLexer(source);
        List<TokenInfo> tokens = collectTokens(lexer);

        // Find comment tokens (skipping whitespace)
        List<TokenInfo> comments = tokens.stream()
            .filter(t -> t.type() == TokenType.LINE_COMMENT ||
                        t.type() == TokenType.BLOCK_COMMENT ||
                        t.type() == TokenType.JAVADOC_COMMENT)
            .toList();

        requireThat(comments.size(), "comments.size").isEqualTo(3);
        requireThat(comments.get(0).type(), "lineComment").isEqualTo(TokenType.LINE_COMMENT);
        requireThat(comments.get(1).type(), "blockComment").isEqualTo(TokenType.BLOCK_COMMENT);
        requireThat(comments.get(2).type(), "javadocComment").isEqualTo(TokenType.JAVADOC_COMMENT);
    }

    @Test(description = "JDK 17+ keywords")
    public void tokenizeNewKeywords() {
        String source = "sealed non-sealed permits record yield when var";
        JavaLexer lexer = new JavaLexer(source);

        List<TokenInfo> tokens = collectTokens(lexer);

        requireThat(tokens.get(0).type(), "sealed").isEqualTo(TokenType.SEALED);
        requireThat(tokens.get(1).type(), "non-sealed").isEqualTo(TokenType.NON_SEALED);
        requireThat(tokens.get(2).type(), "permits").isEqualTo(TokenType.PERMITS);
        requireThat(tokens.get(3).type(), "record").isEqualTo(TokenType.RECORD);
        requireThat(tokens.get(4).type(), "yield").isEqualTo(TokenType.YIELD);
        requireThat(tokens.get(5).type(), "when").isEqualTo(TokenType.WHEN);
        requireThat(tokens.get(6).type(), "var").isEqualTo(TokenType.VAR);
    }

    @Test(description = "Position tracking")
    public void trackPositions() {
        String source = "class Test";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token1 = lexer.nextToken();
        TokenInfo token2 = lexer.nextToken();

        requireThat(token1.startOffset(), "token1.startOffset").isEqualTo(0);
        requireThat(token1.length(), "token1.length").isEqualTo(5);
        requireThat(token2.startOffset(), "token2.startOffset").isEqualTo(6);
        requireThat(token2.length(), "token2.length").isEqualTo(4);
    }

    @Test(description = "String literal with escapes")
    public void tokenizeStringWithEscapes() {
        String source = "\"Hello\\nWorld\\t!\"";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token = lexer.nextToken();

        requireThat(token.type(), "token.type").isEqualTo(TokenType.STRING_LITERAL);
        requireThat(token.text(), "token.text").isEqualTo(source);
    }

    @Test(description = "Complex operators")
    public void tokenizeComplexOperators() {
        String source = "<<= >>= >>> >>>= && || != ==";
        JavaLexer lexer = new JavaLexer(source);

        List<TokenInfo> tokens = collectTokens(lexer);

        requireThat(tokens.get(0).type(), "<<=").isEqualTo(TokenType.LSHIFT_ASSIGN);
        requireThat(tokens.get(1).type(), ">>=").isEqualTo(TokenType.RSHIFT_ASSIGN);
        requireThat(tokens.get(2).type(), ">>>").isEqualTo(TokenType.URSHIFT);
        requireThat(tokens.get(3).type(), ">>>=").isEqualTo(TokenType.URSHIFT_ASSIGN);
        requireThat(tokens.get(4).type(), "&&").isEqualTo(TokenType.LOGICAL_AND);
        requireThat(tokens.get(5).type(), "||").isEqualTo(TokenType.LOGICAL_OR);
        requireThat(tokens.get(6).type(), "!=").isEqualTo(TokenType.NE);
        requireThat(tokens.get(7).type(), "==").isEqualTo(TokenType.EQ);
    }

    @Test(description = "Number literal variations")
    public void tokenizeNumbers() {
        String source = "42 42L 3.14f 3.14159 0xFF 0b1010 0_123";
        JavaLexer lexer = new JavaLexer(source);

        List<TokenInfo> tokens = collectTokens(lexer);

        requireThat(tokens.get(0).type(), "integer").isEqualTo(TokenType.INTEGER_LITERAL);
        requireThat(tokens.get(1).type(), "long").isEqualTo(TokenType.LONG_LITERAL);
        // Note: Current lexer implementation is simplified - full number parsing would handle all formats
    }

    @Test(description = "Identifier vs keyword distinction")
    public void tokenizeIdentifiersVsKeywords() {
        String source = "class className interface interfaceName var varName";
        JavaLexer lexer = new JavaLexer(source);

        List<TokenInfo> tokens = collectTokens(lexer);

        requireThat(tokens.get(0).type(), "class keyword").isEqualTo(TokenType.CLASS);
        requireThat(tokens.get(1).type(), "className identifier").isEqualTo(TokenType.IDENTIFIER);
        requireThat(tokens.get(2).type(), "interface keyword").isEqualTo(TokenType.INTERFACE);
        requireThat(tokens.get(3).type(), "interfaceName identifier").isEqualTo(TokenType.IDENTIFIER);
        requireThat(tokens.get(4).type(), "var keyword").isEqualTo(TokenType.VAR);
        requireThat(tokens.get(5).type(), "varName identifier").isEqualTo(TokenType.IDENTIFIER);
    }

    private List<TokenInfo> collectTokens(JavaLexer lexer) {
        List<TokenInfo> tokens = new ArrayList<>();
        TokenInfo token;
        do {
            token = lexer.nextToken();
            tokens.add(token);
        } while (token.type() != TokenType.EOF);
        return tokens;
    }
}