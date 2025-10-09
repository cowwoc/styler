package io.github.cowwoc.styler.config.exception;

import java.io.Serial;

/**
 * Exception thrown when configuration values fail semantic validation.
 * <p>
 * This exception is thrown when TOML syntax is valid but configuration values violate
 * business rules (e.g., negative line length, invalid enum values, out-of-range numbers).
 * Error messages include the field name, actual value received, and expected constraints
 * to provide actionable guidance for fixing the configuration.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public final class ConfigurationValidationException extends ConfigurationException
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new configuration validation exception.
	 *
	 * @param message the error message describing the validation failure, including field name,
	 *                actual value, and expected constraints
	 * @throws NullPointerException if {@code message} is null
	 */
	public ConfigurationValidationException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new configuration validation exception with a cause.
	 *
	 * @param message the error message describing the validation failure
	 * @param cause   the underlying cause of the validation failure
	 * @throws NullPointerException if {@code message} or {@code cause} is null
	 */
	public ConfigurationValidationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
