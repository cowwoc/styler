package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for binary literal tokenization.
 */
public final class LexerBinaryLiteralTest
{
	/**
	 * Verifies that lowercase binary literals are recognized.
	 * Validates that the 0b prefix (lowercase) is correctly identified and the
	 * entire literal including all binary digits is tokenized as a single INTEGER_LITERAL.
	 */
	@Test
	public void testBinaryLiteralLowercase()
	{
		Lexer lexer = new Lexer("0b1010");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // binary literal + END_OF_FILE
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1010");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that uppercase binary literals are recognized.
	 * Validates that the 0B prefix (uppercase) is correctly identified and tokenized
	 * as an INTEGER_LITERAL, ensuring case-insensitive prefix handling.
	 */
	@Test
	public void testBinaryLiteralUppercase()
	{
		Lexer lexer = new Lexer("0B1111");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // binary literal + END_OF_FILE
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0B1111");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that binary literals with underscores are recognized.
	 * Validates that numeric separators (underscores) within binary literals are preserved
	 * and the entire literal including separators is tokenized as a single INTEGER_LITERAL.
	 * This tests Java 7+ feature for improved readability of large binary numbers.
	 */
	@Test
	public void testBinaryLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0b1010_1100");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // binary literal + END_OF_FILE
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1010_1100");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that binary long literals with L suffix are recognized.
	 * Validates that the L suffix is correctly identified and the entire binary literal
	 * including the suffix is tokenized as a single LONG_LITERAL.
	 */
	@Test
	public void testBinaryLongLiteral()
	{
		Lexer lexer = new Lexer("0b1L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // binary literal + END_OF_FILE
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1L");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that binary literals with multiple digit groups and underscores are recognized.
	 * Validates comprehensive support for binary literals with underscores in various positions,
	 * testing realistic usage patterns for large binary constants.
	 */
	@Test
	public void testBinaryLiteralMultipleUnderscores()
	{
		Lexer lexer = new Lexer("0b1111_0000_1010_1100");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // binary literal + END_OF_FILE
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1111_0000_1010_1100");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that binary long literals with underscores and L suffix are recognized.
	 * Validates combined support for numeric separators and the long suffix in binary literals.
	 */
	@Test
	public void testBinaryLongLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0b1010_1100L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // binary literal + END_OF_FILE
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1010_1100L");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that single-bit binary literals are recognized.
	 * Validates edge case handling for minimal binary literals containing a single bit value.
	 */
	@Test
	public void testBinaryLiteralMinimal()
	{
		Lexer lexer = new Lexer("0b0");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // binary literal + END_OF_FILE
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b0");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that all-ones binary literals are recognized.
	 * Validates edge case handling for binary literals containing only 1 bits.
	 */
	@Test
	public void testBinaryLiteralAllOnes()
	{
		Lexer lexer = new Lexer("0b1111111");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // binary literal + END_OF_FILE
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1111111");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}
}
