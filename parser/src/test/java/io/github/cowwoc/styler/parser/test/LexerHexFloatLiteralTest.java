package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for hexadecimal floating-point literal tokenization.
 */
public final class LexerHexFloatLiteralTest
{
	/**
	 * Verifies that basic hexadecimal float literals with exponent are recognized.
	 * Validates that hex float literals with the p/P exponent notation are correctly
	 * identified and tokenized as DOUBLE_LITERAL (default float type).
	 * This tests Java's hex float format: 0x{hex-digits}p{exponent}.
	 */
	@Test
	public void testHexFloatLiteral()
	{
		Lexer lexer = new Lexer("0x1p10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p10");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with float suffix are recognized.
	 * Validates that the f/F suffix correctly identifies hex float literals
	 * as FLOAT_LITERAL rather than DOUBLE_LITERAL.
	 */
	@Test
	public void testHexFloatLiteralWithFloatSuffix()
	{
		Lexer lexer = new Lexer("0x1p1f");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.FLOAT_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p1f");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with negative exponent are recognized.
	 * Validates that the p notation with minus sign in the exponent is correctly handled
	 * and tokenized as a single DOUBLE_LITERAL token.
	 */
	@Test
	public void testHexFloatLiteralNegativeExponent()
	{
		Lexer lexer = new Lexer("0x1p-10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p-10");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with positive exponent are recognized.
	 * Validates that the p notation with plus sign in the exponent is correctly handled
	 * and tokenized as a single DOUBLE_LITERAL token.
	 */
	@Test
	public void testHexFloatLiteralPositiveExponent()
	{
		Lexer lexer = new Lexer("0x1p+10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p+10");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with fractional part are recognized.
	 * Validates that hex floats with decimal point separating integer and fractional
	 * hex digits are correctly tokenized as DOUBLE_LITERAL.
	 */
	@Test
	public void testHexFloatLiteralWithFraction()
	{
		Lexer lexer = new Lexer("0x1.8p1");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1.8p1");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that uppercase P exponent notation is recognized in hex float literals.
	 * Validates that both lowercase p and uppercase P are accepted for the exponent indicator.
	 */
	@Test
	public void testHexFloatLiteralUppercaseP()
	{
		Lexer lexer = new Lexer("0x1P10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1P10");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with uppercase D suffix are recognized.
	 * Validates that the D suffix explicitly marks the literal as DOUBLE_LITERAL.
	 */
	@Test
	public void testHexFloatLiteralWithDoubleSuffix()
	{
		Lexer lexer = new Lexer("0x1p1d");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p1d");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that complex hexadecimal float literals with multiple fractional digits are recognized.
	 * Validates comprehensive support for hex floats with extended fractional parts.
	 */
	@Test
	public void testHexFloatLiteralComplexFraction()
	{
		Lexer lexer = new Lexer("0xA.Bp5");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xA.Bp5");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with zero exponent are recognized.
	 * Validates edge case handling for hex floats with p0 exponent.
	 */
	@Test
	public void testHexFloatLiteralZeroExponent()
	{
		Lexer lexer = new Lexer("0x1p0");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p0");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with only fractional part (no integer part) are recognized.
	 * Validates edge case handling for hex floats like 0x.1p1.
	 */
	@Test
	public void testHexFloatLiteralOnlyFractional()
	{
		Lexer lexer = new Lexer("0x.1p1");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x.1p1");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with uppercase hex digits are recognized.
	 * Validates correct handling of uppercase A-F digits in hex float literals.
	 */
	@Test
	public void testHexFloatLiteralUppercaseHexDigits()
	{
		Lexer lexer = new Lexer("0xF.Cp2");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xF.Cp2");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Verifies that hexadecimal float literals with mixed case hex digits are recognized.
	 * Validates correct handling of mixed uppercase and lowercase hex digits in float literals.
	 */
	@Test
	public void testHexFloatLiteralMixedCase()
	{
		Lexer lexer = new Lexer("0xaB.cDp3");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // hex float literal + EOF
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xaB.cDp3");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}
}
