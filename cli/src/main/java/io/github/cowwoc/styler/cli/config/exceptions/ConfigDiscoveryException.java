package io.github.cowwoc.styler.cli.config.exceptions;

/**
 * Base exception for all configuration discovery related errors.
 * Provides context about the configuration discovery operation that failed.
 */
public class ConfigDiscoveryException extends RuntimeException
{
	/**
	 * Creates a new configuration discovery exception.
	 *
	 * @param message the error message describing what went wrong
	 * @throws NullPointerException if message is null
	 */
	public ConfigDiscoveryException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new configuration discovery exception with a cause.
	 *
	 * @param message the error message describing what went wrong
	 * @param cause   the underlying exception that caused this error
	 * @throws NullPointerException if message or cause is null
	 */
	public ConfigDiscoveryException(String message, Throwable cause)
	{
		super(message, cause);
	}
}