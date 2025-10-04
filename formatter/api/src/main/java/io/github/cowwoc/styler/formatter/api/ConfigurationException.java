package io.github.cowwoc.styler.formatter.api;


import java.io.Serial;
/**
 * Exception thrown when a rule configuration is invalid or inconsistent.
 * <p>
 * This exception indicates that configuration parameters are malformed,
 * out of acceptable ranges, or logically inconsistent with each other.
 * It is a checked exception to force explicit handling of configuration errors.
 */
public class ConfigurationException extends Exception
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new configuration exception with the specified message.
	 *
	 * @param message the error message describing the configuration problem, never {@code null}
	 */
	public ConfigurationException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new configuration exception with the specified message and cause.
	 *
	 * @param message the error message describing the configuration problem, never {@code null}
	 * @param cause   the underlying cause of the configuration error, may be {@code null}
	 */
	public ConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}