package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for Unicode escape sequence handling in character literals, string literals, and text blocks.
 */
public class LexerUnicodeEscapeTest
{
	/**
	 * Tests character literal with valid Unicode escape sequence.
	 * Validates that Unicode escape sequences (backslash-u plus 4 hex digits) in character literals are
	 * recognized as complete token text without interpretation.
	 */
	@Test
	public void shouldLexCharLiteralWithUnicodeEscape()
	{
		String source = "'\\u0041'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\u0041'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(8);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests character literal with Unicode replacement character.
	 * Validates that U+FFFD (replacement character) in escape sequences is properly handled.
	 */
	@Test
	public void shouldLexCharLiteralWithReplacementCharacter()
	{
		String source = "'\\uFFFD'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\uFFFD'");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with single Unicode escape sequence.
	 * Validates that Unicode escapes are preserved as token text in string literals.
	 */
	@Test
	public void shouldLexStringLiteralWithSingleUnicodeEscape()
	{
		String source = "\"\\u0048ello\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\u0048ello\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with multiple consecutive Unicode escape sequences.
	 * Validates that the lexer correctly handles strings containing multiple escapes.
	 */
	@Test
	public void shouldLexStringLiteralWithMultipleUnicodeEscapes()
	{
		String source = "\"\\u0048\\u0065\\u006C\\u006C\\u006F\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\u0048\\u0065\\u006C\\u006C\\u006F\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with Unicode escape at the end of content.
	 * Validates that Unicode escapes at string boundaries are properly tokenized.
	 */
	@Test
	public void shouldLexStringLiteralWithUnicodeAtEnd()
	{
		String source = "\"Hello\\u0021\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"Hello\\u0021\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with both standard and Unicode escape sequences.
	 * Validates that lexer correctly handles mixed escape types in the same token.
	 */
	@Test
	public void shouldLexStringLiteralWithMixedEscapes()
	{
		String source = "\"\\n\\u0041\\t\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\n\\u0041\\t\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests text block with Unicode escape sequences.
	 * Validates that Unicode escapes in text blocks (triple-quoted strings) are preserved.
	 */
	@Test
	public void shouldLexTextBlockWithUnicodeEscape()
	{
		String source = "\"\"\"\n\\u0041\\u0042\\u0043\n\"\"\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\"\"\n\\u0041\\u0042\\u0043\n\"\"\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests text block with mixed standard and Unicode escape sequences.
	 * Validates that text blocks handle both escape types correctly.
	 */
	@Test
	public void shouldLexTextBlockWithMixedEscapes()
	{
		String source = "\"\"\"\nHello\\u0020World\\n\n\"\"\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").contains("\\u0020");
		requireThat(token.text(), "token.text()").contains("\\n");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests that standard escape sequences continue to work with Unicode escape support.
	 * Validates backward compatibility of newline escape in character literals.
	 */
	@Test
	public void shouldPreserveStandardEscapesUnchanged()
	{
		String source = "'\\n'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\n'");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests that standard escape sequences work in string literals.
	 * Validates backward compatibility of tab, carriage return, and newline escapes.
	 */
	@Test
	public void shouldPreserveStringStandardEscapes()
	{
		String source = "\"\\t\\r\\n\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\t\\r\\n\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests character literal with lowercase hexadecimal in Unicode escape.
	 * Validates that both uppercase and lowercase hex digits are correctly recognized.
	 */
	@Test
	public void shouldLexCharLiteralWithLowercaseUnicodeEscape()
	{
		String source = "'\\u004a'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\u004a'");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with consecutive Unicode escapes without separators.
	 * Validates that back-to-back escape sequences are correctly recognized with proper token length.
	 */
	@Test
	public void shouldLexStringWithConsecutiveUnicodeEscapes()
	{
		String source = "\"\\u0041\\u0042\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\u0041\\u0042\"");
		// Token is: quote + 6 chars (\\u0041) + 6 chars (\\u0042) + quote = 14 chars total
		requireThat(token.end() - token.start(), "token.length()").isEqualTo(14);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}
}
