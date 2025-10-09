package io.github.cowwoc.styler.config.exception;

import java.io.Serial;

/**
 * Exception thrown when TOML syntax errors are detected during configuration parsing.
 * <p>
 * This exception includes file location information (path, line, column) to help users
 * identify and fix syntax errors in their <a href="https://toml.io/en/v1.0.0">TOML</a>
 * configuration files.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public final class ConfigurationSyntaxException extends ConfigurationException
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new configuration syntax exception.
	 *
	 * @param message the error message describing the syntax error, including file location
	 * @throws NullPointerException if {@code message} is null
	 */
	public ConfigurationSyntaxException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new configuration syntax exception with a cause.
	 *
	 * @param message the error message describing the syntax error, including file location
	 * @param cause   the underlying parsing exception from the TOML library
	 * @throws NullPointerException if {@code message} or {@code cause} is null
	 */
	public ConfigurationSyntaxException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
