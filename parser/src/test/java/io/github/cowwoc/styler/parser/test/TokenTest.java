package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.assertThrows;

/**
 * Thread-safe tests for Token.
 */
public class TokenTest
{
	/**
	 * Verifies that Token correctly stores all components (type, start, end, text) and
	 * calculates length as the difference between end and start positions.
	 */
	@Test
	public void testValidToken()
	{
		Token token = new Token(TokenType.IDENTIFIER, 0, 5, "hello");

		requireThat(token.type(), "type").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.start(), "start").isEqualTo(0);
		requireThat(token.end(), "end").isEqualTo(5);
		requireThat(token.text(), "text").isEqualTo("hello");
		requireThat(token.length(), "length").isEqualTo(5);
	}

	/**
	 * Validates that tokens can be created without text content (null text).
	 * This is necessary for punctuation and symbols where the TokenType alone
	 * conveys all semantic meaning (e.g., semicolons, braces, operators).
	 */
	@Test
	public void testTokenWithoutText()
	{
		Token token = new Token(TokenType.SEMICOLON, 10, 11, null);

		requireThat(token.type(), "type").isEqualTo(TokenType.SEMICOLON);
		requireThat(token.text(), "text").isNull();
	}

	/**
	 * Ensures that null TokenType is rejected with NullPointerException.
	 * Every token must have a valid type for the lexer and parser to function correctly.
	 */
	@Test
	public void testNullType()
	{
		assertThrows(NullPointerException.class,
			() -> new Token(null, 0, 5, "test"));
	}

	/**
	 * Validates that negative start positions are rejected with IllegalArgumentException.
	 * Token positions must be non-negative to represent valid source code locations.
	 */
	@Test
	public void testNegativeStart()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new Token(TokenType.IDENTIFIER, -1, 5, "test"));
	}

	/**
	 * Ensures that end position before start position is rejected with IllegalArgumentException.
	 * This prevents invalid token spans that would represent negative or backwards ranges.
	 */
	@Test
	public void testEndBeforeStart()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new Token(TokenType.IDENTIFIER, 10, 5, "test"));
	}

	/**
	 * Validates that zero-length tokens are supported (start equals end).
	 * This is necessary for END_OF_FILE tokens which represent a position but contain no characters.
	 */
	@Test
	public void testZeroLengthToken()
	{
		Token token = new Token(TokenType.END_OF_FILE, 10, 10, null);

		requireThat(token.length(), "length").isEqualTo(0);
	}

	/**
	 * Verifies the is() convenience method for type comparison.
	 * This method provides cleaner syntax than direct TokenType equality checks.
	 */
	@Test
	public void testIsMethod()
	{
		Token token = new Token(TokenType.PUBLIC, 0, 6, "public");

		requireThat(token.is(TokenType.PUBLIC), "is(PUBLIC)").isTrue();
		requireThat(token.is(TokenType.PRIVATE), "is(PRIVATE)").isFalse();
	}

	/**
	 * Tests isKeyword() classification for Java keywords.
	 * Validates that keyword tokens (public, static, etc.) are correctly identified
	 * while non-keyword tokens (identifiers) return false.
	 */
	@Test
	public void testIsKeyword()
	{
		Token publicToken = new Token(TokenType.PUBLIC, 0, 6, "public");
		Token staticToken = new Token(TokenType.STATIC, 0, 6, "static");
		Token identifierToken = new Token(TokenType.IDENTIFIER, 0, 5, "myVar");

		requireThat(publicToken.isKeyword(), "publicToken.isKeyword()").isTrue();
		requireThat(staticToken.isKeyword(), "staticToken.isKeyword()").isTrue();
		requireThat(identifierToken.isKeyword(), "identifierToken.isKeyword()").isFalse();
	}

	/**
	 * Tests isLiteral() classification for various literal types.
	 * Validates recognition of integer, string, and boolean literals,
	 * while ensuring identifiers are not classified as literals.
	 */
	@Test
	public void testIsLiteral()
	{
		Token intToken = new Token(TokenType.INTEGER_LITERAL, 0, 2, "42");
		Token stringToken = new Token(TokenType.STRING_LITERAL, 0, 7, "\"hello\"");
		Token boolToken = new Token(TokenType.BOOLEAN_LITERAL, 0, 4, "true");
		Token identifierToken = new Token(TokenType.IDENTIFIER, 0, 5, "myVar");

		requireThat(intToken.isLiteral(), "intToken.isLiteral()").isTrue();
		requireThat(stringToken.isLiteral(), "stringToken.isLiteral()").isTrue();
		requireThat(boolToken.isLiteral(), "boolToken.isLiteral()").isTrue();
		requireThat(identifierToken.isLiteral(), "identifierToken.isLiteral()").isFalse();
	}

	/**
	 * Tests isOperator() classification for Java operators.
	 * Validates recognition of arithmetic operators (+), assignment (=),
	 * and logical operators (&&), while ensuring identifiers return false.
	 */
	@Test
	public void testIsOperator()
	{
		Token plusToken = new Token(TokenType.PLUS, 0, 1, "+");
		Token assignToken = new Token(TokenType.ASSIGN, 0, 1, "=");
		Token andToken = new Token(TokenType.LOGICAL_AND, 0, 2, "&&");
		Token identifierToken = new Token(TokenType.IDENTIFIER, 0, 5, "myVar");

		requireThat(plusToken.isOperator(), "plusToken.isOperator()").isTrue();
		requireThat(assignToken.isOperator(), "assignToken.isOperator()").isTrue();
		requireThat(andToken.isOperator(), "andToken.isOperator()").isTrue();
		requireThat(identifierToken.isOperator(), "identifierToken.isOperator()").isFalse();
	}

	/**
	 * Verifies toString() format when token has text content.
	 * Format: TYPE[start:end, "text"] for debugging and error messages.
	 */
	@Test
	public void testToStringWithText()
	{
		Token token = new Token(TokenType.IDENTIFIER, 10, 15, "hello");

		requireThat(token.toString(), "toString()").isEqualTo("IDENTIFIER[10:15, \"hello\"]");
	}

	/**
	 * Verifies toString() format when token has no text content.
	 * Format: TYPE[start:end] without text portion, used for punctuation/symbols.
	 */
	@Test
	public void testToStringWithoutText()
	{
		Token token = new Token(TokenType.SEMICOLON, 20, 21, null);

		requireThat(token.toString(), "toString()").isEqualTo("SEMICOLON[20:21]");
	}

	/**
	 * Tests record equality semantics - tokens with identical components are equal.
	 * This is essential for token comparison in parser logic and testing.
	 */
	@Test
	public void testEquality()
	{
		Token token1 = new Token(TokenType.IDENTIFIER, 0, 5, "hello");
		Token token2 = new Token(TokenType.IDENTIFIER, 0, 5, "hello");

		requireThat(token1, "token1").isEqualTo(token2);
	}

	/**
	 * Validates that equal tokens produce equal hash codes.
	 * This ensures Token can be used correctly in hash-based collections.
	 */
	@Test
	public void testHashCode()
	{
		Token token1 = new Token(TokenType.IDENTIFIER, 0, 5, "hello");
		Token token2 = new Token(TokenType.IDENTIFIER, 0, 5, "hello");

		requireThat(token1.hashCode(), "token1.hashCode()").isEqualTo(token2.hashCode());
	}
}
