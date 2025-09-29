package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.testng.Assert.*;

/**
 * Unit tests for FixSuggestionProvider functionality.
 * Validates fix suggestion generation for different error types.
 */
public class FixSuggestionProviderTest
{
	@Test
	public void testGenerateParseSuggestionMissingSemicolon()
	{
		ErrorContext context = ErrorContext.parseError(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 5)),
			"int x = 1",
			"missing ';' at end of statement"
		);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("semicolon"));
		assertTrue(suggestion.contains(";"));
	}

	@Test
	public void testGenerateParseSuggestionMissingBrace()
	{
		ErrorContext context = ErrorContext.parseError(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 5)),
			"public class Test",
			"missing '{' to start block"
		);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("opening brace"));
		assertTrue(suggestion.contains("{"));
	}

	@Test
	public void testGenerateConfigSuggestionUnknownKey()
	{
		ErrorContext context = ErrorContext.configError(
			Paths.get("config.toml"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10)),
			"invalid_key = value",
			"unknown configuration key 'invalid_key'",
			null
		);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("configuration documentation"));
	}

	@Test
	public void testGenerateFormatSuggestionLineLength()
	{
		ErrorContext context = ErrorContext.formatViolation(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 130)),
			"very long line of code that exceeds maximum length",
			"LineLength",
			"Line too long: 130 characters",
			ErrorSeverity.WARNING,
			null
		);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("Break long lines") || suggestion.contains("line length"));
	}

	@Test
	public void testGenerateFormatSuggestionIndentation()
	{
		ErrorContext context = ErrorContext.formatViolation(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(2, 1), new SourcePosition(2, 5)),
			"   int x = 1;",
			"Indentation",
			"Incorrect indentation: expected 4 spaces",
			ErrorSeverity.WARNING,
			null
		);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("indentation") || suggestion.contains("consistent"));
	}

	@Test
	public void testGenerateSystemSuggestionPermission()
	{
		ErrorContext context = ErrorContext.systemError(
			Paths.get("Test.java"),
			"Permission denied accessing file Test.java"
		);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("permission") || suggestion.contains("access"));
	}

	@Test
	public void testGenerateSystemSuggestionFileNotFound()
	{
		ErrorContext context = ErrorContext.systemError(
			Paths.get("Missing.java"),
			"File not found: Missing.java"
		);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("file path") || suggestion.contains("exists"));
	}

	@Test
	public void testGenerateContextualSuggestion()
	{
		ErrorContext context = ErrorContext.parseError(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 5)),
			"int x = 1",
			"missing ';' at end of statement"
		);

		String suggestion = FixSuggestionProvider.generateContextualSuggestion(
			context, "inside method declaration");

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("semicolon"));
		assertTrue(suggestion.contains("Context:"));
		assertTrue(suggestion.contains("inside method declaration"));
	}

	@Test
	public void testGenerateContextualSuggestionAtColumnOne()
	{
		ErrorContext context = ErrorContext.parseError(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(5, 1), new SourcePosition(5, 1)),
			"invalid statement",
			"Syntax error at beginning of line"
		);

		String suggestion = FixSuggestionProvider.generateContextualSuggestion(context, null);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("beginning of line"));
	}

	@Test
	public void testHasSuggestionForAllCategories()
	{
		assertTrue(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.PARSE));
		assertTrue(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.CONFIG));
		assertTrue(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.FORMAT));
		assertTrue(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.VALIDATE));
		assertTrue(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.SYSTEM));
	}

	@Test
	public void testGenerateValidationSuggestion()
	{
		ErrorContext context = new ErrorContext(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 5)),
			"code",
			ErrorCategory.VALIDATE,
			ErrorSeverity.WARNING,
			"VALIDATE-0001",
			"Constraint violation: value exceeds limit",
			null
		);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		assertNotNull(suggestion);
		assertTrue(suggestion.contains("constraint") || suggestion.contains("configuration"));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullErrorContextThrows()
	{
		FixSuggestionProvider.generateSuggestion(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullCategoryThrows()
	{
		FixSuggestionProvider.hasSuggestionFor(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullContextualErrorContextThrows()
	{
		FixSuggestionProvider.generateContextualSuggestion(null, "context");
	}
}