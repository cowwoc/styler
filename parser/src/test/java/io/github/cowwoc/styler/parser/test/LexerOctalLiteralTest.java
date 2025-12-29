package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for octal literal tokenization.
 */
public final class LexerOctalLiteralTest
{
	/**
	 * Verifies that octal literals are recognized.
	 * Validates that numeric values prefixed with 0 and containing only octal digits (0-7)
	 * are correctly identified and tokenized as a single INTEGER_LITERAL.
	 * This tests traditional octal literal format from Java.
	 */
	@Test
	public void testOctalLiteral()
	{
		Lexer lexer = new Lexer("0755");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0755");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that octal literals with underscores are recognized.
	 * Validates that numeric separators (underscores) within octal literals are preserved
	 * and the entire literal including separators is tokenized as a single INTEGER_LITERAL.
	 * This tests Java 7+ feature for improved readability of large octal numbers.
	 */
	@Test
	public void testOctalLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0_77");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0_77");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that octal long literals with L suffix are recognized.
	 * Validates that the L suffix is correctly identified and the entire octal literal
	 * including the suffix is tokenized as a single LONG_LITERAL.
	 */
	@Test
	public void testOctalLongLiteral()
	{
		Lexer lexer = new Lexer("0777L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal long literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0777L");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that octal literals with multiple underscore groups are recognized.
	 * Validates comprehensive support for octal literals with underscores in various positions,
	 * testing realistic usage patterns for large octal constants.
	 */
	@Test
	public void testOctalLiteralMultipleUnderscores()
	{
		Lexer lexer = new Lexer("0_777_777_777");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0_777_777_777");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that octal long literals with underscores and L suffix are recognized.
	 * Validates combined support for numeric separators and the long suffix in octal literals.
	 */
	@Test
	public void testOctalLongLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0_777_777L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal long literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0_777_777L");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that single-zero octal literals are recognized.
	 * Validates edge case handling for minimal octal literals.
	 */
	@Test
	public void testOctalLiteralZero()
	{
		Lexer lexer = new Lexer("0");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that maximum octal literals with all 7 digits are recognized.
	 * Validates edge case handling for octal literals containing maximum octal digit values.
	 */
	@Test
	public void testOctalLiteralAll7s()
	{
		Lexer lexer = new Lexer("07777777");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("07777777");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that octal literals with underscores after leading zero are recognized.
	 * Validates correct parsing of 0_... pattern with underscores immediately following the leading zero.
	 */
	@Test
	public void testOctalLiteralUnderscoreAfterZero()
	{
		Lexer lexer = new Lexer("0_1");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0_1");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that various octal digit combinations are recognized.
	 * Validates support for octal literals using different combinations of valid octal digits (0-7).
	 */
	@Test
	public void testOctalLiteralVariousDigits()
	{
		Lexer lexer = new Lexer("01234567");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // octal literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("01234567");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}
}
