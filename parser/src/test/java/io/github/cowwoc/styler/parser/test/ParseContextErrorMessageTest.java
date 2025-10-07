package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.TokenInfo;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;
import java.util.List;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ParseContext error message formatting with line:column information.
 */
public class ParseContextErrorMessageTest
{
	/**
	 * Verifies that ParseException error messages use line:column format even for EOF token.
	 * EOF token has startOffset() == sourceText.length(), which is a boundary case.
	 */
	@Test(expectedExceptions = IndexOverlayParser.ParseException.class)
	public void expectWithEOFTokenIncludesLineAndColumn()
	{
		String source = "class Test {}";
		List<TokenInfo> tokens = List.of(
			new TokenInfo(TokenType.CLASS, 0, 5, "class"),
			new TokenInfo(TokenType.IDENTIFIER, 6, 4, "Test"),
			new TokenInfo(TokenType.LBRACE, 11, 1, "{"),
			new TokenInfo(TokenType.RBRACE, 12, 1, "}"),
			new TokenInfo(TokenType.EOF, 13, 0, ""));  // offset == sourceText.length()

		ParseContext context = createTestContext(tokens, source);

		// Navigate to EOF
		context.advance();  // CLASS
		context.advance();  // IDENTIFIER
		context.advance();  // LBRACE
		context.advance();  // RBRACE
		// Now at EOF

		try
		{
			context.expect(TokenType.SEMICOLON);  // Force error at EOF
		}
		catch (IndexOverlayParser.ParseException e)
		{
			// Validate format before re-throwing
			String message = e.getMessage();
			requireThat(message, "message").contains("line");
			requireThat(message, "message").contains("column");
			requireThat(message.contains("position 13"), "containsPosition13").isFalse();
			throw e;  // Re-throw for @Test(expectedExceptions)
		}
	}

	/**
	 * Verifies error message format for error at first character of source.
	 */
	@Test(expectedExceptions = IndexOverlayParser.ParseException.class)
	public void expectAtFirstCharacterReportsLineOneColumnOne()
	{
		String source = "invalid syntax";
		List<TokenInfo> tokens = List.of(
			new TokenInfo(TokenType.IDENTIFIER, 0, 7, "invalid"),
			new TokenInfo(TokenType.IDENTIFIER, 8, 6, "syntax"),
			new TokenInfo(TokenType.EOF, 14, 0, ""));

		ParseContext context = createTestContext(tokens, source);

		try
		{
			context.expect(TokenType.CLASS);  // Expect error at position 0
		}
		catch (IndexOverlayParser.ParseException e)
		{
			String message = e.getMessage();
			requireThat(message, "message").contains("line 1");
			requireThat(message, "message").contains("column 1");
			throw e;
		}
	}

	/**
	 * Verifies error message shows correct line number for multiline source code.
	 */
	@Test(expectedExceptions = IndexOverlayParser.ParseException.class)
	public void expectOnMultilineSourceReportsCorrectLine()
	{
		String source = "class Test {\n    int x = 5\n    int y = 10;\n}";

		// Calculate offset for line 2 error (after "int x = 5")
		// Line 1: "class Test {\n" = 13 chars
		// Line 2 up to after 5: "    int x = 5" = 13 chars
		// Total offset: 13 + 13 = 26
		int errorOffset = "class Test {\n    int x = 5".length();

		List<TokenInfo> tokens = List.of(
			new TokenInfo(TokenType.CLASS, 0, 5, "class"),
			new TokenInfo(TokenType.IDENTIFIER, 6, 4, "Test"),
			new TokenInfo(TokenType.LBRACE, 11, 1, "{"),
			new TokenInfo(TokenType.INT, 17, 3, "int"),
			new TokenInfo(TokenType.IDENTIFIER, 21, 1, "x"),
			new TokenInfo(TokenType.ASSIGN, 23, 1, "="),
			new TokenInfo(TokenType.INTEGER_LITERAL, 25, 1, "5"),
			new TokenInfo(TokenType.INT, errorOffset + 1, 3, "int"),  // Line 3 starts
			new TokenInfo(TokenType.EOF, source.length(), 0, ""));

		ParseContext context = createTestContext(tokens, source);

		// Navigate to token after "5" (where semicolon should be)
		for (int i = 0; i < 7; ++i)
		{
			context.advance();
		}

		try
		{
			context.expect(TokenType.SEMICOLON);
		}
		catch (IndexOverlayParser.ParseException e)
		{
			String message = e.getMessage();
			requireThat(message, "message").contains("line 3");
			requireThat(message.contains("position"), "containsPosition").isFalse();
			throw e;
		}
	}

	/**
	 * Verifies line counting works correctly with CRLF line endings (Windows).
	 */
	@Test(expectedExceptions = IndexOverlayParser.ParseException.class)
	public void expectWithCRLFLineEndingsReportsCorrectLine()
	{
		String source = "class Test {\r\n    int invalid\r\n}";

		// Calculate offset: "class Test {\r\n    int " = 22 chars
		int invalidOffset = "class Test {\r\n    int ".length();
		int afterInvalid = invalidOffset + 7;  // After "invalid"

		List<TokenInfo> tokens = List.of(
			new TokenInfo(TokenType.CLASS, 0, 5, "class"),
			new TokenInfo(TokenType.IDENTIFIER, 6, 4, "Test"),
			new TokenInfo(TokenType.LBRACE, 11, 1, "{"),
			new TokenInfo(TokenType.INT, 18, 3, "int"),
			new TokenInfo(TokenType.IDENTIFIER, invalidOffset, 7, "invalid"),
			new TokenInfo(TokenType.RBRACE, afterInvalid + 2, 1, "}"),
			new TokenInfo(TokenType.EOF, source.length(), 0, ""));

		ParseContext context = createTestContext(tokens, source);

		// Navigate to after "invalid" token
		for (int i = 0; i < 5; ++i)
		{
			context.advance();
		}

		try
		{
			context.expect(TokenType.SEMICOLON);
		}
		catch (IndexOverlayParser.ParseException e)
		{
			String message = e.getMessage();
			requireThat(message, "message").contains("line 3");  // RBRACE is on line 3
			requireThat(message, "message").contains("column");
			throw e;
		}
	}

	/**
	 * Verifies error message format for error at end of first line.
	 */
	@Test(expectedExceptions = IndexOverlayParser.ParseException.class)
	public void expectAtEndOfLineReportsCorrectPosition()
	{
		String source = "int x = 5\nint y = 10;";

		// Token at start of line 2 (position 10)
		List<TokenInfo> tokens = List.of(
			new TokenInfo(TokenType.INT, 0, 3, "int"),
			new TokenInfo(TokenType.IDENTIFIER, 4, 1, "x"),
			new TokenInfo(TokenType.ASSIGN, 6, 1, "="),
			new TokenInfo(TokenType.INTEGER_LITERAL, 8, 1, "5"),
			new TokenInfo(TokenType.INT, 10, 3, "int"),  // Line 2 starts here
			new TokenInfo(TokenType.EOF, source.length(), 0, ""));

		ParseContext context = createTestContext(tokens, source);

		// Navigate to position after "5" (end of line 1)
		for (int i = 0; i < 4; ++i)
		{
			context.advance();
		}

		try
		{
			context.expect(TokenType.SEMICOLON);  // Error at line 2, column 1
		}
		catch (IndexOverlayParser.ParseException e)
		{
			String message = e.getMessage();
			requireThat(message, "message").contains("line 2");
			requireThat(message, "message").contains("column");
			throw e;
		}
	}

	/**
	 * Verifies error messages contain expected format components.
	 */
	@Test
	public void expectErrorMessageContainsExpectedAndFoundTypes()
	{
		String source = "class Test { }";
		List<TokenInfo> tokens = List.of(
			new TokenInfo(TokenType.CLASS, 0, 5, "class"),
			new TokenInfo(TokenType.IDENTIFIER, 6, 4, "Test"),
			new TokenInfo(TokenType.LBRACE, 11, 1, "{"),
			new TokenInfo(TokenType.RBRACE, 13, 1, "}"),
			new TokenInfo(TokenType.EOF, 14, 0, ""));

		ParseContext context = createTestContext(tokens, source);

		try
		{
			context.expect(TokenType.INT);  // Wrong type
		}
		catch (IndexOverlayParser.ParseException e)
		{
			String message = e.getMessage();
			requireThat(message, "message").contains("Expected");
			requireThat(message, "message").contains("INT");
			requireThat(message, "message").contains("found");
			requireThat(message, "message").contains("CLASS");
			requireThat(message, "message").contains("line");
			requireThat(message, "message").contains("column");
			// Verify old format is gone
			requireThat(message.contains("position 0"), "containsPosition0").isFalse();
		}
	}

	/**
	 * Verifies error message format matches expected pattern.
	 */
	@Test
	public void expectErrorMessageMatchesFormatPattern()
	{
		String source = "class Test {}";
		List<TokenInfo> tokens = List.of(
			new TokenInfo(TokenType.CLASS, 0, 5, "class"),
			new TokenInfo(TokenType.EOF, 13, 0, ""));

		ParseContext context = createTestContext(tokens, source);

		try
		{
			context.expect(TokenType.PUBLIC);
		}
		catch (IndexOverlayParser.ParseException e)
		{
			String message = e.getMessage();
			requireThat(message.matches(".*line \\d+, column \\d+.*"), "matchesPattern").isTrue();
		}
	}

	/**
	 * Verifies empty source file reports line 1, column 1 for EOF error.
	 */
	@Test(expectedExceptions = IndexOverlayParser.ParseException.class)
	public void expectWithEmptySourceReportsLineOneColumnOne()
	{
		String source = "";
		List<TokenInfo> tokens = List.of(
			new TokenInfo(TokenType.EOF, 0, 0, ""));

		ParseContext context = createTestContext(tokens, source);

		try
		{
			context.expect(TokenType.CLASS);
		}
		catch (IndexOverlayParser.ParseException e)
		{
			String message = e.getMessage();
			requireThat(message, "message").contains("line 1");
			requireThat(message, "message").contains("column 1");
			throw e;
		}
	}

	// Static helper methods for test data creation

	/**
	 * Creates a test ParseContext with the given tokens and source.
	 *
	 * @param tokens the token list
	 * @param source the source text
	 * @return a new ParseContext for testing
	 */
	private static ParseContext createTestContext(List<TokenInfo> tokens, String source)
	{
		ArenaNodeStorage storage = ArenaNodeStorage.create(tokens.size() * 2);
		return new ParseContext(tokens, storage, source);
	}
}
