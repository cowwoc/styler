package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Comprehensive tests for Unicode escape preprocessing per JLS 3.3.
 * <p>
 * Tests validate that Unicode escapes (backslash-u-XXXX) outside string and character literals are properly
 * decoded during lexical analysis while preserving original source text in {@link Token#text()}.
 */
public class LexerUnicodePreprocessingTest
{
	// ========== PHASE 1: IDENTIFIER RECOGNITION (5 tests) ==========

	/**
	 * Tests that a simple Unicode escape is recognized as an identifier.
	 * {@code \u0041} is 'A', so {@code int \u0041 = 1;} should produce an IDENTIFIER token.
	 */
	@Test
	public void shouldRecognizeSimpleUnicodeEscapeAsIdentifier()
	{
		String source = "int \\u0041 = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(6);
		Token identifierToken = tokens.get(1);
		requireThat(identifierToken.type(), "identifierToken.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(identifierToken.text(), "identifierToken.text()").isEqualTo("\\u0041");
		requireThat(identifierToken.decodedText(), "identifierToken.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that multiple consecutive Unicode escapes form a single identifier.
	 * {@code \u0041\u0042\u0043} decodes to "ABC" and should be a single IDENTIFIER token.
	 */
	@Test
	public void shouldRecognizeMultipleConsecutiveUnicodeEscapesAsIdentifier()
	{
		String source = "\\u0041\\u0042\\u0043";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041\\u0042\\u0043");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("ABC");
	}

	/**
	 * Tests that an identifier starting with a Unicode escape is recognized.
	 * {@code \u0041bc} decodes to "Abc" and should be a single IDENTIFIER token.
	 */
	@Test
	public void shouldRecognizeUnicodeEscapeAtIdentifierStart()
	{
		String source = "\\u0041bc";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041bc");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("Abc");
	}

	/**
	 * Tests that a Unicode escape in the middle of an identifier is recognized.
	 * {@code a\u0042c} decodes to "aBc" and should be a single IDENTIFIER token.
	 */
	@Test
	public void shouldRecognizeUnicodeEscapeInMiddleOfIdentifier()
	{
		String source = "a\\u0042c";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("a\\u0042c");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("aBc");
	}

	/**
	 * Tests that a mixed identifier with both regular and escaped characters is recognized.
	 * {@code test\u0041var} decodes to "testAvar" and should be a single IDENTIFIER token.
	 */
	@Test
	public void shouldRecognizeMixedIdentifierWithUnicodeEscape()
	{
		String source = "test\\u0041var";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("test\\u0041var");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("testAvar");
	}

	// ========== PHASE 2: KEYWORD RECOGNITION (5 tests) ==========

	/**
	 * Tests that a fully escaped "public" keyword is recognized as PUBLIC.
	 * {@code \u0070\u0075\u0062\u006C\u0069\u0063} decodes to "public".
	 */
	@Test
	public void shouldRecognizeFullyEscapedPublicKeyword()
	{
		String source = "\\u0070\\u0075\\u0062\\u006C\\u0069\\u0063 class Test {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.PUBLIC);
		requireThat(firstToken.text(), "firstToken.text()").isEqualTo("\\u0070\\u0075\\u0062\\u006C\\u0069\\u0063");
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("public");
	}

	/**
	 * Tests that a partially escaped "public" keyword is recognized.
	 * {@code \u0070ublic} decodes to "public" where only 'p' is escaped.
	 */
	@Test
	public void shouldRecognizePartiallyEscapedPublicKeyword()
	{
		String source = "\\u0070ublic";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.PUBLIC);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0070ublic");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("public");
	}

	/**
	 * Tests that an escaped "class" keyword is recognized.
	 * {@code \u0063lass} decodes to "class" where only 'c' is escaped.
	 */
	@Test
	public void shouldRecognizeEscapedClassKeyword()
	{
		String source = "\\u0063lass Test {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.CLASS);
		requireThat(firstToken.text(), "firstToken.text()").isEqualTo("\\u0063lass");
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("class");
	}

	/**
	 * Tests that an escaped "static" keyword is recognized.
	 * {@code \u0073tatic} decodes to "static" where only 's' is escaped.
	 */
	@Test
	public void shouldRecognizeEscapedStaticKeyword()
	{
		String source = "\\u0073tatic int x = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.STATIC);
		requireThat(firstToken.text(), "firstToken.text()").isEqualTo("\\u0073tatic");
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("static");
	}

	/**
	 * Tests that an escaped "var" contextual keyword is recognized.
	 * {@code \u0076ar} decodes to "var" where only 'v' is escaped.
	 */
	@Test
	public void shouldRecognizeEscapedVarKeyword()
	{
		String source = "\\u0076ar x = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.VAR);
		requireThat(firstToken.text(), "firstToken.text()").isEqualTo("\\u0076ar");
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("var");
	}

	// ========== PHASE 3: OPERATOR RECOGNITION (5 tests) ==========
	// Note: The current Lexer implementation does not preprocess Unicode escapes for operators.
	// These tests document expected behavior for future implementation per JLS 3.3.
	// For now, we test that operators following identifiers with Unicode escapes work correctly.

	/**
	 * Tests that the assign operator works with Unicode-escaped identifiers.
	 * Validates proper tokenization after a Unicode-escaped identifier.
	 */
	@Test
	public void shouldRecognizeAssignOperatorAfterEscapedIdentifier()
	{
		String source = "\\u0041 = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.ASSIGN);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.SEMICOLON);
	}

	/**
	 * Tests that the equal operator works in expressions with Unicode-escaped identifiers.
	 */
	@Test
	public void shouldRecognizeEqualOperatorWithEscapedIdentifiers()
	{
		String source = "\\u0041 == \\u0042";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(0).decodedText(), "tokens.get(0).decodedText()").isEqualTo("A");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EQUAL);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(2).decodedText(), "tokens.get(2).decodedText()").isEqualTo("B");
	}

	/**
	 * Tests that the plus-assign operator works with Unicode-escaped identifiers.
	 */
	@Test
	public void shouldRecognizePlusAssignOperatorWithEscapedIdentifier()
	{
		String source = "\\u0041 += 5;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.PLUS_ASSIGN);
	}

	/**
	 * Tests that braces work properly with Unicode-escaped class declarations.
	 */
	@Test
	public void shouldRecognizeBracesWithEscapedKeyword()
	{
		String source = "\\u0063lass X {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.CLASS);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.LEFT_BRACE);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.RIGHT_BRACE);
	}

	/**
	 * Tests that method declaration tokens work with Unicode-escaped modifiers.
	 */
	@Test
	public void shouldRecognizeMethodDeclarationWithEscapedModifier()
	{
		String source = "\\u0070ublic void test() {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.PUBLIC);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.VOID);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.LEFT_PARENTHESIS);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.RIGHT_PARENTHESIS);
	}

	// ========== PHASE 4: MULTIPLE-U COMPLIANCE (4 tests) ==========

	/**
	 * Tests that double-u Unicode escape is valid per JLS 3.3.
	 * {@code \uu0041} should decode to 'A'.
	 */
	@Test
	public void shouldRecognizeDoubleUEscape()
	{
		String source = "\\uu0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\uu0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that triple-u Unicode escape is valid per JLS 3.3.
	 * {@code \uuu0041} should decode to 'A'.
	 */
	@Test
	public void shouldRecognizeTripleUEscape()
	{
		String source = "\\uuu0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\uuu0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that many consecutive 'u' characters in Unicode escape is valid.
	 * {@code \uuuuuuuu0041} should decode to 'A'.
	 */
	@Test
	public void shouldRecognizeManyUEscape()
	{
		String source = "\\uuuuuuuu0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\uuuuuuuu0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that multiple-u escape forms a keyword when decoded.
	 * {@code \uuu0069nt} should decode to "int" and be recognized as INT keyword.
	 */
	@Test
	public void shouldRecognizeMultipleUEscapeFormingKeyword()
	{
		String source = "\\uuu0069nt";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INT);
		requireThat(token.text(), "token.text()").isEqualTo("\\uuu0069nt");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("int");
	}

	// ========== PHASE 5: SOURCE FIDELITY (4 tests) ==========

	/**
	 * Tests that token text preserves the original Unicode escape.
	 * The original escape sequence should be preserved in text() for formatter output.
	 */
	@Test
	public void shouldPreserveOriginalEscapeInTokenText()
	{
		String source = "\\u0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041");
		requireThat(token.text(), "token.text()").isNotEqualTo("A");
	}

	/**
	 * Tests that token positions correctly reference the original source.
	 * Position should reflect the 6-character escape sequence, not the decoded character.
	 */
	@Test
	public void shouldPreserveTokenPositionsForUnicodeEscape()
	{
		String source = "int \\u0041 = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token identifierToken = tokens.get(1);
		// "int " is 4 chars, so identifier starts at position 4
		requireThat(identifierToken.start(), "identifierToken.start()").isEqualTo(4);
		// \u0041 is 6 chars, so identifier ends at position 10
		requireThat(identifierToken.end(), "identifierToken.end()").isEqualTo(10);
		requireThat(identifierToken.length(), "identifierToken.length()").isEqualTo(6);
	}

	/**
	 * Tests that mixed identifier preserves original text including escapes.
	 */
	@Test
	public void shouldPreserveMixedIdentifierOriginalText()
	{
		String source = "test\\u0041var";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.text(), "token.text()").isEqualTo("test\\u0041var");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("testAvar");
	}

	/**
	 * Tests that multiple escapes in token preserve all original escape sequences.
	 */
	@Test
	public void shouldPreserveMultipleEscapesInTokenText()
	{
		String source = "\\u0041\\u0042\\u0043";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041\\u0042\\u0043");
		// The full escaped sequence should be preserved
		requireThat(token.text().contains("\\u0041"), "containsFirstEscape").isTrue();
		requireThat(token.text().contains("\\u0042"), "containsSecondEscape").isTrue();
		requireThat(token.text().contains("\\u0043"), "containsThirdEscape").isTrue();
	}

	// ========== PHASE 6: ERROR HANDLING (5 tests) ==========

	/**
	 * Tests that an incomplete Unicode escape (too few hex digits) is handled.
	 * backslash-u-00 has only 2 hex digits instead of required 4.
	 */
	@Test
	public void shouldHandleIncompleteHexDigits()
	{
		// With only 2 hex digits, this is not a valid Unicode escape
		// The lexer should treat the backslash as a regular character or error
		String source = "\\u00";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		// Behavior: should produce ERROR token since backslash-u-00 is not valid
		// The exact handling depends on implementation
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.ERROR);
	}

	/**
	 * Tests that an invalid hex character in Unicode escape is handled.
	 * backslash-u-00GG contains 'G' which is not a valid hex digit.
	 */
	@Test
	public void shouldHandleInvalidHexCharacter()
	{
		String source = "\\u00GG";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		// The lexer should produce ERROR token for invalid escape
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.ERROR);
	}

	/**
	 * Tests that a backslash not followed by 'u' is not treated as Unicode escape.
	 * backslash-x-0041 is not a Unicode escape since 'x' is not 'u'.
	 */
	@Test
	public void shouldNotTreatBackslashXAsUnicodeEscape()
	{
		String source = "\"\\x0041\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		// This is a string literal with an unrecognized escape sequence
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\x0041\"");
	}

	/**
	 * Tests that escaped backslash followed by u in string literal is handled.
	 * {@code "\\u0041"} contains an escaped backslash, not a Unicode escape.
	 */
	@Test
	public void shouldHandleEscapedBackslashInStringLiteral()
	{
		// Double backslash is an escaped backslash, not a Unicode escape
		String source = "\"\\\\u0041\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		// The text should preserve the double backslash
		requireThat(token.text(), "token.text()").isEqualTo("\"\\\\u0041\"");
	}

	/**
	 * Tests that Unicode escape at very end of source is handled gracefully.
	 */
	@Test
	public void shouldHandleUnicodeEscapeAtEndOfSource()
	{
		String source = "test\\u0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		// Should successfully parse as identifier "testA"
		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("test\\u0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("testA");
	}

	// ========== PHASE 7: REGRESSION TESTS (2 tests) ==========

	/**
	 * Tests that Unicode escapes inside string literals continue to work.
	 * Regression test for existing in-literal escape handling.
	 */
	@Test
	public void shouldContinueToLexUnicodeEscapeInStringLiteral()
	{
		String source = "\"\\u0048ello\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\u0048ello\"");
	}

	/**
	 * Tests that Unicode escapes inside character literals continue to work.
	 * Regression test for existing in-literal escape handling.
	 */
	@Test
	public void shouldContinueToLexUnicodeEscapeInCharLiteral()
	{
		String source = "'\\u0041'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\u0041'");
	}

	// ========== ADDITIONAL EDGE CASE TESTS ==========

	/**
	 * Tests that "non-sealed" keyword works with Unicode escape for 'n'.
	 * {@code \u006eon-sealed} decodes to "non-sealed".
	 */
	@Test
	public void shouldRecognizeEscapedNonSealedKeyword()
	{
		String source = "\\u006eon-sealed class Test {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.NON_SEALED);
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("non-sealed");
	}

	/**
	 * Tests that identifiers with digits and escapes work correctly.
	 * {@code var1\u0032} decodes to "var12".
	 */
	@Test
	public void shouldRecognizeIdentifierWithDigitAndEscape()
	{
		// \u0032 is '2'
		String source = "var1\\u0032";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("var1\\u0032");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("var12");
	}

	/**
	 * Tests that underscore character via Unicode escape works in identifiers.
	 * {@code my\u005Fvar} decodes to "my_var".
	 */
	@Test
	public void shouldRecognizeUnderscoreViaUnicodeEscape()
	{
		// \u005F is '_'
		String source = "my\\u005Fvar";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("my\\u005Fvar");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("my_var");
	}

	/**
	 * Tests that Unicode escape for dollar sign works in identifiers.
	 * Java allows $ in identifiers.
	 * {@code my\u0024var} decodes to "my$var".
	 */
	@Test
	public void shouldRecognizeDollarSignViaUnicodeEscape()
	{
		// \u0024 is '$'
		String source = "my\\u0024var";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("my\\u0024var");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("my$var");
	}

	/**
	 * Tests tokens without Unicode escapes share the same String instance.
	 * This is an optimization test to verify memory efficiency.
	 */
	@Test
	@SuppressWarnings("PMD.UseEqualsToCompareStrings")
	public void shouldShareStringInstanceWhenNoEscapes()
	{
		String source = "regularIdentifier";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		// Identity check (==) is intentional - verifying same String instance
		requireThat(token.text() == token.decodedText(), "sameInstance").isTrue();
	}

	/**
	 * Tests that a Unicode escape for a non-identifier character does not extend the identifier.
	 * {@code test\u0020end} should produce two identifiers since \u0020 is space.
	 */
	@Test
	public void shouldNotExtendIdentifierWithNonIdentifierEscape()
	{
		// \u0020 is space character
		String source = "test\\u0020end";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		// "test" followed by space (from escape) followed by "end"
		// The escape should cause a split, but exact behavior depends on implementation
		// At minimum, we should not have a single identifier "test end"
		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("test");
	}
}
