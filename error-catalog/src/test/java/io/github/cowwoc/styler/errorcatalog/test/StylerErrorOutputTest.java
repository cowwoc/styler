package io.github.cowwoc.styler.errorcatalog.test;

import io.github.cowwoc.styler.errorcatalog.Audience;
import io.github.cowwoc.styler.errorcatalog.ContextualException;
import io.github.cowwoc.styler.errorcatalog.ErrorContext;
import io.github.cowwoc.styler.errorcatalog.FixSuggestion;
import io.github.cowwoc.styler.errorcatalog.SourceLocation;
import io.github.cowwoc.styler.errorcatalog.StylerErrorOutput;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link StylerErrorOutput}.
 */
public final class StylerErrorOutputTest
{
	/**
	 * Tests creation with default audience detection.
	 */
	@Test
	void shouldCreateWithDefaultAudienceDetection()
	{
		StylerErrorOutput output = new StylerErrorOutput();

		Audience audience = output.getAudience();
		requireThat(audience, "audience").isNotNull();
		assertTrue(audience == Audience.AI || audience == Audience.HUMAN,
			"audience must be AI or HUMAN");
	}

	/**
	 * Tests creation with explicit AI audience.
	 */
	@Test
	void shouldCreateWithExplicitAiAudience()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.AI);

		requireThat(output.getAudience(), "audience").isEqualTo(Audience.AI);
	}

	/**
	 * Tests creation with explicit HUMAN audience.
	 */
	@Test
	void shouldCreateWithExplicitHumanAudience()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.HUMAN);

		requireThat(output.getAudience(), "audience").isEqualTo(Audience.HUMAN);
	}

	/**
	 * Tests that null audience is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullAudience()
	{
		new StylerErrorOutput(null);
	}

	/**
	 * Tests formatting error for AI audience.
	 */
	@Test
	void shouldFormatErrorForAi()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.AI);
		TestException error = new TestException("Invalid input");

		String formatted = output.formatError(error);

		requireThat(formatted, "formatted").contains("TestException");
		requireThat(formatted, "formatted").contains("Invalid input");
		requireThat(formatted, "formatted").contains("\"file\":");
		requireThat(formatted, "formatted").contains("\"line\":");
	}

	/**
	 * Tests formatting error for HUMAN audience.
	 */
	@Test
	void shouldFormatErrorForHuman()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.HUMAN);
		TestException error = new TestException("Invalid input");

		String formatted = output.formatError(error);

		requireThat(formatted, "formatted").contains("TestException");
		requireThat(formatted, "formatted").contains("Invalid input");
		requireThat(formatted, "formatted").contains("File:");
		requireThat(formatted, "formatted").contains("Line:");
	}

	/**
	 * Tests that null error is rejected during formatting.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullErrorDuringFormatting()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.AI);
		output.formatError(null);
	}

	/**
	 * Tests that non-ContextualException throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	void shouldRejectNonContextualException()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.AI);
		Exception error = new Exception("Plain exception");
		output.formatError(error);
	}

	/**
	 * Tests getting fix suggestion for error with specific fix.
	 */
	@Test
	void shouldGetFixSuggestionForError()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.AI);
		TestException error = new TestException("Test error");

		FixSuggestion suggestion = output.getFixSuggestion(error);
		requireThat(suggestion.description(), "description").isEqualTo("Add missing semicolon");
	}

	/**
	 * Tests getting null fix suggestion when no fix available.
	 */
	@Test
	void shouldReturnNullWhenNoFixAvailable()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.AI);
		TestExceptionNoFix error = new TestExceptionNoFix("No fix available");

		FixSuggestion suggestion = output.getFixSuggestion(error);

		requireThat(suggestion, "suggestion").isNull();
	}

	/**
	 * Tests that null error is rejected when getting fix suggestion.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullErrorWhenGettingFixSuggestion()
	{
		StylerErrorOutput output = new StylerErrorOutput(Audience.AI);
		output.getFixSuggestion(null);
	}

	/**
	 * Tests that different audiences produce different formats for same error.
	 */
	@Test
	void shouldProduceDifferentFormatsForDifferentAudiences()
	{
		TestException error = new TestException("State error");
		StylerErrorOutput aiOutput = new StylerErrorOutput(Audience.AI);
		StylerErrorOutput humanOutput = new StylerErrorOutput(Audience.HUMAN);

		String aiFormat = aiOutput.formatError(error);
		String humanFormat = humanOutput.formatError(error);

		// Different formats should have different structure
		requireThat(aiFormat, "aiFormat").isNotEqualTo(humanFormat);
		// Both should contain the error message
		requireThat(aiFormat, "aiFormat").contains("State error");
		requireThat(humanFormat, "humanFormat").contains("State error");
	}

	/**
	 * Test exception that implements ContextualException.
	 */
	private static final class TestException extends RuntimeException implements ContextualException
	{
		private static final long serialVersionUID = 1L;
		private final transient ErrorContext context;

		TestException(String message)
		{
			super(message);
			this.context = new ErrorContext(
				new SourceLocation("test.java", 10, 5, 10),
				"int x = 42",
				"semicolon",
				"newline",
				"Add missing semicolon");
		}

		@Override
		public ErrorContext getErrorContext()
		{
			return context;
		}
	}

	/**
	 * Test exception with no fix suggestion.
	 */
	private static final class TestExceptionNoFix extends RuntimeException implements ContextualException
	{
		private static final long serialVersionUID = 1L;
		private final transient ErrorContext context;

		TestExceptionNoFix(String message)
		{
			super(message);
			this.context = new ErrorContext(
				new SourceLocation("test.java", 10, 5, 10),
				"",
				"",
				"",
				"");
		}

		@Override
		public ErrorContext getErrorContext()
		{
			return context;
		}
	}
}
