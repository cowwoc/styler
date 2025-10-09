package io.github.cowwoc.styler.config.exception;

import java.io.Serial;

/**
 * Base exception for all configuration-related errors in Styler.
 * <p>
 * This exception is thrown when configuration loading, parsing, or validation fails.
 * Subclasses provide specific error types for syntax errors and validation failures.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public class ConfigurationException extends Exception
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new configuration exception.
	 *
	 * @param message the error message describing what went wrong
	 * @throws NullPointerException if {@code message} is null
	 */
	public ConfigurationException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new configuration exception with a cause.
	 *
	 * @param message the error message describing what went wrong
	 * @param cause   the underlying cause of this exception
	 * @throws NullPointerException if {@code message} or {@code cause} is null
	 */
	public ConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
