package io.github.cowwoc.styler.cli.config.exceptions;

import java.util.Objects;

/**
 * Base exception for all configuration discovery related errors.
 * Provides context about the configuration discovery operation that failed.
 */
public class ConfigDiscoveryException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	/**
	 * Creates a new configuration discovery exception.
	 *
	 * @param message the error message describing what went wrong
	 * @throws NullPointerException if message is {@code null}
	 */
	public ConfigDiscoveryException(String message)
	{
		super(Objects.requireNonNull(message, "message must not be null"));
	}

	/**
	 * Creates a new configuration discovery exception with a cause.
	 *
	 * @param message the error message describing what went wrong
	 * @param cause   the underlying exception that caused this error
	 * @throws NullPointerException if message or cause is {@code null}
	 */
	public ConfigDiscoveryException(String message, Throwable cause)
	{
		super(Objects.requireNonNull(message, "message must not be null"),
			Objects.requireNonNull(cause, "cause must not be null"));
	}
}