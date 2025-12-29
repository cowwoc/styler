package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for hexadecimal integer literal tokenization.
 */
public final class LexerHexLiteralTest
{
	/**
	 * Verifies that lowercase hexadecimal literals are recognized.
	 * Validates that the 0x prefix (lowercase) is correctly identified and the
	 * entire literal with lowercase hex digits is tokenized as a single INTEGER_LITERAL.
	 */
	@Test
	public void testHexLiteralLowercase()
	{
		Lexer lexer = new Lexer("0xdead");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xdead");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that uppercase hexadecimal literals are recognized.
	 * Validates that the 0X prefix (uppercase) is correctly identified and tokenized
	 * as an INTEGER_LITERAL, ensuring case-insensitive prefix handling.
	 */
	@Test
	public void testHexLiteralUppercase()
	{
		Lexer lexer = new Lexer("0XBEEF");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0XBEEF");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal literals with underscores are recognized.
	 * Validates that numeric separators (underscores) within hex literals are preserved
	 * and the entire literal including separators is tokenized as a single INTEGER_LITERAL.
	 * This tests Java 7+ feature for improved readability of large hex numbers.
	 */
	@Test
	public void testHexLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0xDEAD_BEEF");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xDEAD_BEEF");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal long literals with L suffix are recognized.
	 * Validates that the L suffix is correctly identified and the entire hex literal
	 * including the suffix is tokenized as a single LONG_LITERAL.
	 */
	@Test
	public void testHexLongLiteral()
	{
		Lexer lexer = new Lexer("0xFFL");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xFFL");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal literals with mixed case hex digits are recognized.
	 * Validates that lexer preserves the original case of hex digits (a-f)
	 * when mixed with uppercase and lowercase prefix variations.
	 */
	@Test
	public void testHexLiteralMixedCase()
	{
		Lexer lexer = new Lexer("0xAbCdEf");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xAbCdEf");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal literals with multiple underscore groups are recognized.
	 * Validates comprehensive support for hex literals with underscores in various positions,
	 * testing realistic usage patterns for large hex constants.
	 */
	@Test
	public void testHexLiteralMultipleUnderscores()
	{
		Lexer lexer = new Lexer("0xFF_FF_FF_FF");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xFF_FF_FF_FF");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal long literals with underscores and L suffix are recognized.
	 * Validates combined support for numeric separators and the long suffix in hex literals.
	 */
	@Test
	public void testHexLongLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0xDEAD_BEEFL");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xDEAD_BEEFL");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that single-digit hexadecimal literals are recognized.
	 * Validates edge case handling for minimal hex literals containing a single hex digit.
	 */
	@Test
	public void testHexLiteralMinimal()
	{
		Lexer lexer = new Lexer("0x0");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x0");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal literals with all F digits are recognized.
	 * Validates edge case handling for hex literals containing maximum digit values.
	 */
	@Test
	public void testHexLiteralAllFs()
	{
		Lexer lexer = new Lexer("0xFFFFFFFF");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xFFFFFFFF");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal literals with only decimal digits are recognized.
	 * Validates that hex digits composed of only 0-9 (without a-f) are correctly identified.
	 */
	@Test
	public void testHexLiteralDecimalDigitsOnly()
	{
		Lexer lexer = new Lexer("0x123");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x123");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}
}
