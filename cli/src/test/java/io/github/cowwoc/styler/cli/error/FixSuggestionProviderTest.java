package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for FixSuggestionProvider functionality.
 * Validates fix suggestion generation for different error types.
 */
public class FixSuggestionProviderTest
{
	/**
	 * Verifies that suggestions for missing semicolon parse errors are generated correctly.
	 */
	@Test
	public void generateParseSuggestionMissingSemicolon()
	{
		ErrorContext context = ErrorContext.parseError(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 5)),
			"int x = 1",
			"missing ';' at end of statement");

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("semicolon"), "suggestionContainsSemicolon").isTrue();
		requireThat(suggestion.contains(";"), "suggestionContainsSemicolonChar").isTrue();
	}

	/**
	 * Verifies that suggestions for missing opening brace parse errors are generated correctly.
	 */
	@Test
	public void generateParseSuggestionMissingBrace()
	{
		ErrorContext context = ErrorContext.parseError(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 5)),
			"public class Test",
			"missing '{' to start block");

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("opening brace"), "suggestionContainsOpeningBrace").isTrue();
		requireThat(suggestion.contains("{"), "suggestionContainsBraceChar").isTrue();
	}

	/**
	 * Verifies that suggestions for unknown configuration key errors are generated correctly.
	 */
	@Test
	public void generateConfigSuggestionUnknownKey()
	{
		ErrorContext context = ErrorContext.configError(
			Paths.get("config.toml"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10)),
			"invalid_key = value",
			"unknown configuration key 'invalid_key'",
			null);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("configuration documentation"), "suggestionContainsConfigDocs").isTrue();
	}

	/**
	 * Verifies that suggestions for line length violations are generated correctly.
	 */
	@Test
	public void generateFormatSuggestionLineLength()
	{
		ErrorContext context = ErrorContext.formatViolation(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 130)),
			"very long line of code that exceeds maximum length",
			"LineLength",
			"Line too long: 130 characters",
			ErrorSeverity.WARNING,
			null);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("Break long lines") || suggestion.contains("line length"),
			"suggestionContainsLineLengthAdvice").isTrue();
	}

	/**
	 * Verifies that suggestions for indentation violations are generated correctly.
	 */
	@Test
	public void generateFormatSuggestionIndentation()
	{
		ErrorContext context = ErrorContext.formatViolation(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(2, 1), new SourcePosition(2, 5)),
			"   int x = 1;",
			"Indentation",
			"Incorrect indentation: expected 4 spaces",
			ErrorSeverity.WARNING,
			null);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("indentation") || suggestion.contains("consistent"),
			"suggestionContainsIndentationAdvice").isTrue();
	}

	/**
	 * Verifies that suggestions for permission denied system errors are generated correctly.
	 */
	@Test
	public void generateSystemSuggestionPermission()
	{
		ErrorContext context = ErrorContext.systemError(
			Paths.get("Test.java"),
			"Permission denied accessing file Test.java");

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("permission") || suggestion.contains("access"),
			"suggestionContainsPermissionAdvice").isTrue();
	}

	/**
	 * Verifies that suggestions for file not found system errors are generated correctly.
	 */
	@Test
	public void generateSystemSuggestionFileNotFound()
	{
		ErrorContext context = ErrorContext.systemError(
			Paths.get("Missing.java"),
			"File not found: Missing.java");

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("file path") || suggestion.contains("exists"),
			"suggestionContainsFileAdvice").isTrue();
	}

	/**
	 * Verifies that contextual suggestions include both the fix and the context information.
	 */
	@Test
	public void generateContextualSuggestion()
	{
		ErrorContext context = ErrorContext.parseError(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 5)),
			"int x = 1",
			"missing ';' at end of statement");

		String suggestion = FixSuggestionProvider.generateContextualSuggestion(
			context, "inside method declaration");

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("semicolon"), "suggestionContainsSemicolon").isTrue();
		requireThat(suggestion.contains("Context:"), "suggestionContainsContext").isTrue();
		requireThat(suggestion.contains("inside method declaration"), "suggestionContainsMethodContext").isTrue();
	}

	/**
	 * Verifies that contextual suggestions for errors at column one include position information.
	 */
	@Test
	public void generateContextualSuggestionAtColumnOne()
	{
		ErrorContext context = ErrorContext.parseError(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(5, 1), new SourcePosition(5, 1)),
			"invalid statement",
			"Syntax error at beginning of line");

		String suggestion = FixSuggestionProvider.generateContextualSuggestion(context, null);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("beginning of line"), "suggestionContainsLineBeginning").isTrue();
	}

	/**
	 * Verifies that suggestions are available for all error categories.
	 */
	@Test
	public void hasSuggestionForAllCategories()
	{
		requireThat(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.PARSE), "hasSuggestionForParse").isTrue();
		requireThat(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.CONFIG), "hasSuggestionForConfig").isTrue();
		requireThat(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.FORMAT), "hasSuggestionForFormat").isTrue();
		requireThat(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.VALIDATE), "hasSuggestionForValidate").isTrue();
		requireThat(FixSuggestionProvider.hasSuggestionFor(ErrorCategory.SYSTEM), "hasSuggestionForSystem").isTrue();
	}

	/**
	 * Verifies that suggestions for validation errors are generated correctly.
	 */
	@Test
	public void generateValidationSuggestion()
	{
		ErrorContext context = new ErrorContext(
			Paths.get("Test.java"),
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 5)),
			"code",
			ErrorCategory.VALIDATE,
			ErrorSeverity.WARNING,
			"VALIDATE-0001",
			"Constraint violation: value exceeds limit",
			null);

		String suggestion = FixSuggestionProvider.generateSuggestion(context);

		requireThat(suggestion, "suggestion").isNotNull();
		requireThat(suggestion.contains("constraint") || suggestion.contains("configuration"),
			"suggestionContainsConstraintAdvice").isTrue();
	}

	/**
	 * Verifies that passing a null error context to generateSuggestion throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullErrorContextThrows()
	{
		FixSuggestionProvider.generateSuggestion(null);
	}

	/**
	 * Verifies that passing a null category to hasSuggestionFor throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullCategoryThrows()
	{
		FixSuggestionProvider.hasSuggestionFor(null);
	}

	/**
	 * Verifies that passing a null error context to generateContextualSuggestion throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullContextualErrorContextThrows()
	{
		FixSuggestionProvider.generateContextualSuggestion(null, "context");
	}
}