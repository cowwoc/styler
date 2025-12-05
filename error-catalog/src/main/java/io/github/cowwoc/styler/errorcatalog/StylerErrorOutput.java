package io.github.cowwoc.styler.errorcatalog;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unified error output for CLI and API consumers.
 * <p>
 * Provides formatted error messages with automatic audience detection.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class StylerErrorOutput
{
	private final Audience audience;
	private final ErrorFormatter formatter;

	/**
	 * Creates error output with automatic audience detection.
	 */
	public StylerErrorOutput()
	{
		this(Audience.detect());
	}

	/**
	 * Creates error output for specific audience.
	 *
	 * @param audience target audience (AI or HUMAN)
	 * @throws NullPointerException if audience is {@code null}
	 */
	public StylerErrorOutput(Audience audience)
	{
		requireThat(audience, "audience").isNotNull();
		this.audience = audience;
		this.formatter = new ErrorFormatter();
	}

	/**
	 * Formats an error for the configured audience.
	 *
	 * @param error the exception to format
	 * @return formatted error message
	 * @throws NullPointerException  if error is {@code null}
	 * @throws IllegalStateException if error doesn't implement ContextualException
	 */
	public String formatError(Throwable error)
	{
		requireThat(error, "error").isNotNull();
		return switch (audience)
		{
			case AI -> formatter.formatForAi(error);
			case HUMAN -> formatter.formatForHuman(error);
		};
	}

	/**
	 * Gets fix suggestion for an error.
	 *
	 * @param error the exception to analyze
	 * @return fix suggestion, or {@code null} if not available
	 * @throws NullPointerException  if error is {@code null}
	 * @throws IllegalStateException if error doesn't implement ContextualException
	 */
	public FixSuggestion getFixSuggestion(Throwable error)
	{
		requireThat(error, "error").isNotNull();
		return formatter.getFixSuggestion(error);
	}

	/**
	 * Gets the configured audience.
	 *
	 * @return current audience setting
	 */
	public Audience getAudience()
	{
		return audience;
	}
}
