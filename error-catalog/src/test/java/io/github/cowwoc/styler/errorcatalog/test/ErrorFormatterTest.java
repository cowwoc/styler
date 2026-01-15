package io.github.cowwoc.styler.errorcatalog.test;

import io.github.cowwoc.styler.errorcatalog.ContextualException;
import io.github.cowwoc.styler.errorcatalog.ErrorContext;
import io.github.cowwoc.styler.errorcatalog.ErrorFormatter;
import io.github.cowwoc.styler.errorcatalog.FixSuggestion;
import io.github.cowwoc.styler.errorcatalog.SourceLocation;
import org.testng.annotations.Test;

import java.io.Serial;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link ErrorFormatter}.
 */
public final class ErrorFormatterTest
{
	/**
	 * Tests AI format contains JSON structure.
	 */
	@Test
	void shouldFormatForAiAsJson()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		TestException error = new TestException("Unexpected token");

		String formatted = formatter.formatForAi(error);

		requireThat(formatted, "formatted").startsWith("{");
		requireThat(formatted, "formatted").endsWith("}");
		requireThat(formatted, "formatted").contains("\"exception\": \"TestException\"");
		requireThat(formatted, "formatted").contains("\"message\": \"Unexpected token\"");
		requireThat(formatted, "formatted").contains("\"location\":");
		requireThat(formatted, "formatted").contains("\"file\": \"test.java\"");
		requireThat(formatted, "formatted").contains("\"line\": 10");
	}

	/**
	 * Tests human format contains readable text.
	 */
	@Test
	void shouldFormatForHumanAsNarrative()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		TestException error = new TestException("Unexpected token");

		String formatted = formatter.formatForHuman(error);

		requireThat(formatted, "formatted").contains("TestException");
		requireThat(formatted, "formatted").contains("Unexpected token");
		requireThat(formatted, "formatted").contains("File: test.java");
		requireThat(formatted, "formatted").contains("Line: 10");
		requireThat(formatted, "formatted").contains("Expected: valid token");
		requireThat(formatted, "formatted").contains("Found: invalid");
		requireThat(formatted, "formatted").contains("Fix: Replace with valid token");
	}

	/**
	 * Tests that null error is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullErrorForAi()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		formatter.formatForAi(null);
	}

	/**
	 * Tests that null error is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullErrorForHuman()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		formatter.formatForHuman(null);
	}

	/**
	 * Tests that null error is rejected for fix suggestion.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullErrorForFixSuggestion()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		formatter.getFixSuggestion(null);
	}

	/**
	 * Tests that non-ContextualException throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	void shouldRejectNonContextualException()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		Exception error = new Exception("Plain exception");
		formatter.formatForAi(error);
	}

	/**
	 * Tests fix suggestion is extracted from context.
	 */
	@Test
	void shouldGetFixSuggestionFromContext()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		TestException error = new TestException("Test error");

		FixSuggestion suggestion = formatter.getFixSuggestion(error);

		requireThat(suggestion.steps().size(), "steps.size").isEqualTo(1);
		requireThat(suggestion.steps().getFirst(), "steps[0]").isEqualTo("Replace with valid token");
	}

	/**
	 * Tests null returned when no fix available.
	 */
	@Test
	void shouldReturnNullWhenNoFix()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		TestExceptionNoFix error = new TestExceptionNoFix("No fix");

		FixSuggestion suggestion = formatter.getFixSuggestion(error);

		requireThat(suggestion, "suggestion").isNull();
	}

	/**
	 * Tests special characters are escaped in AI format.
	 */
	@Test
	void shouldEscapeSpecialCharactersInAiFormat()
	{
		ErrorFormatter formatter = new ErrorFormatter();
		TestExceptionWithSpecialChars error = new TestExceptionWithSpecialChars();

		String formatted = formatter.formatForAi(error);

		requireThat(formatted, "formatted").contains("\\\"");
		requireThat(formatted, "formatted").contains("\\n");
		requireThat(formatted, "formatted").contains("\\t");
	}

	/**
	 * Test exception that implements ContextualException.
	 */
	private static final class TestException extends RuntimeException implements ContextualException
	{
		@Serial
		private static final long serialVersionUID = 1L;
		private final transient ErrorContext context;

		TestException(String message)
		{
			super(message);
			this.context = new ErrorContext(
				new SourceLocation("test.java", 10, 5, 15),
				"let x = @invalid",
				"valid token",
				"invalid",
				"Replace with valid token");
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
		@Serial
		private static final long serialVersionUID = 1L;
		private final transient ErrorContext context;

		TestExceptionNoFix(String message)
		{
			super(message);
			this.context = new ErrorContext(
				new SourceLocation("test.java", 1, 1),
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

	/**
	 * Test exception with special characters in message.
	 */
	private static final class TestExceptionWithSpecialChars extends RuntimeException
		implements ContextualException
	{
		@Serial
		private static final long serialVersionUID = 1L;
		private final transient ErrorContext context;

		TestExceptionWithSpecialChars()
		{
			super("Error with \"quotes\" and\nnewlines\tand\ttabs");
			this.context = new ErrorContext(
				new SourceLocation("test.java", 1, 1),
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
