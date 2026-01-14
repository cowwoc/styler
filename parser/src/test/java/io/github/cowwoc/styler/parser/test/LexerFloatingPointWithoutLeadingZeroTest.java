package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for floating-point literals without a leading zero (e.g., {@code .5}, {@code .0025}).
 */
public final class LexerFloatingPointWithoutLeadingZeroTest
{
	/**
	 * Verifies that a simple decimal without leading zero is recognized.
	 */
	@Test
	public void simpleDecimal()
	{
		Lexer lexer = new Lexer(".5");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5");
		requireThat(tokens.get(1).type(), "eof").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that a decimal with multiple digits is recognized.
	 */
	@Test
	public void multipleDigits()
	{
		Lexer lexer = new Lexer(".0025");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".0025");
	}

	/**
	 * Verifies that a decimal with exponent is recognized.
	 */
	@Test
	public void withExponent()
	{
		Lexer lexer = new Lexer(".5e10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5e10");
	}

	/**
	 * Verifies that a decimal with negative exponent is recognized.
	 */
	@Test
	public void withNegativeExponent()
	{
		Lexer lexer = new Lexer(".5e-3");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5e-3");
	}

	/**
	 * Verifies that a decimal with float suffix is recognized.
	 */
	@Test
	public void withFloatSuffix()
	{
		Lexer lexer = new Lexer(".5f");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.FLOAT_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5f");
	}

	/**
	 * Verifies that a decimal with double suffix is recognized.
	 */
	@Test
	public void withDoubleSuffix()
	{
		Lexer lexer = new Lexer(".5d");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5d");
	}

	/**
	 * Verifies that underscores are allowed in the fractional part.
	 */
	@Test
	public void withUnderscores()
	{
		Lexer lexer = new Lexer(".123_456");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".123_456");
	}

	/**
	 * Verifies that a DOT not followed by a digit remains a DOT token.
	 */
	@Test
	public void dotNotFollowedByDigit()
	{
		Lexer lexer = new Lexer("a.b");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(4);
		requireThat(tokens.get(0).type(), "a").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(1).type(), "dot").isEqualTo(TokenType.DOT);
		requireThat(tokens.get(2).type(), "b").isEqualTo(TokenType.IDENTIFIER);
	}

	/**
	 * Verifies parsing in a method call context (the original error case).
	 */
	@Test
	public void inMethodCallContext()
	{
		Lexer lexer = new Lexer("setSomeDouble(.0025)");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(5);
		requireThat(tokens.get(0).type(), "method").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(1).type(), "lparen").isEqualTo(TokenType.LEFT_PARENTHESIS);
		requireThat(tokens.get(2).type(), "literal").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(tokens.get(2).text(), "literal.text()").isEqualTo(".0025");
		requireThat(tokens.get(3).type(), "rparen").isEqualTo(TokenType.RIGHT_PARENTHESIS);
	}
}
