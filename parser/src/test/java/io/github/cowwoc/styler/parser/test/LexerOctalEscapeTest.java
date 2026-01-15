package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for octal escape sequence handling in character and string literals.
 */
public final class LexerOctalEscapeTest
{
	@Test
	public void shouldLexCharLiteralWithSingleDigitOctalNul()
	{
		String source = "'\\0'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\0'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(4);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	@Test
	public void shouldLexCharLiteralWithSingleDigitOctalMax()
	{
		String source = "'\\7'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\7'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(4);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	@Test
	public void shouldLexCharLiteralWithTwoDigitOctal()
	{
		String source = "'\\12'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\12'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(5);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	@Test
	public void shouldLexCharLiteralWithThreeDigitOctal()
	{
		String source = "'\\013'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\013'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(6);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	@Test
	public void shouldLexCharLiteralWithMaxOctalValue()
	{
		String source = "'\\377'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\377'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(6);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	@Test
	public void shouldLexCharLiteralWithHighFirstDigitOctal()
	{
		String source = "'\\47'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\47'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(5);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	@Test
	public void shouldLexStringLiteralWithOctalEscape()
	{
		String source = "\"Hello\\012World\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"Hello\\012World\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	@Test
	public void shouldLexSpringFrameworkOctalPattern()
	{
		String source = "sb.append('\\013')";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(7);
		Token charLiteral = tokens.get(4);
		requireThat(charLiteral.type(), "charLiteral.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(charLiteral.text(), "charLiteral.text()").isEqualTo("'\\013'");
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.RIGHT_PARENTHESIS);
	}

	@Test
	public void shouldLexOctalEscapeFollowedByNonOctalDigit()
	{
		String source = "\"\\09\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\09\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}
}
