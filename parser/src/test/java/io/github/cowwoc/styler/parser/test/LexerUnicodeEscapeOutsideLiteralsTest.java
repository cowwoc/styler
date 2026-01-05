package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for Unicode escape sequence handling outside string and character literals.
 * Per JLS §3.3, Unicode escapes are processed before lexical analysis.
 */
public class LexerUnicodeEscapeOutsideLiteralsTest
{
	/**
	 * Tests Unicode escape in identifier position.
	 * {@code \u0041} is 'A', so {@code int \u0041 = 1;} should parse as {@code int A = 1;}.
	 */
	@Test
	public void shouldLexUnicodeEscapeAsIdentifier()
	{
		String source = "int \\u0041 = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(6);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.INT);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.ASSIGN);
	}
}
