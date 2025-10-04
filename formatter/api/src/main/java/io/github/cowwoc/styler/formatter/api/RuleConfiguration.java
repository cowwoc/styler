package io.github.cowwoc.styler.formatter.api;


/**
 * Base class for all formatting rule configurations.
 * <p>
 * Configuration classes provide type-safe access to rule parameters and support
 * validation, merging, and serialization. All configurations are immutable after
 * construction to ensure thread safety.
 * <p>
 * <b>Security Note:</b> Configuration values are validated to prevent injection
 * attacks and ensure they contain only safe, expected data types.
 */
public abstract class RuleConfiguration
{
	/**
	 * Validates this configuration object.
	 * <p>
	 * This method checks that all configuration parameters are within acceptable
	 * ranges and that the combination of settings is logically consistent.
	 * It should be called after construction and before using the configuration.
	 *
	 * @throws ConfigurationException if the configuration is invalid
	 * @throws SecurityException      if configuration values pose security risks
	 */
	public abstract void validate() throws ConfigurationException;

	/**
	 * Merges this configuration with an override configuration.
	 * <p>
	 * The override configuration takes precedence for any values it specifies,
	 * while this configuration provides defaults for unspecified values.
	 * The result is a new configuration object; neither input is modified.
	 *
	 * @param override the configuration to merge with this one, never {@code null}
	 * @return a new merged configuration, never {@code null}
	 * @throws ConfigurationException if the merged configuration would be invalid
	 * @throws IllegalArgumentException if the override is not compatible with this configuration
	 */

	public abstract RuleConfiguration merge( RuleConfiguration override);

	/**
	 * Returns a human-readable description of this configuration.
	 * <p>
	 * This method is used for debugging and audit logging. It should include
	 * all significant configuration parameters but avoid exposing sensitive data.
	 *
	 * @return a description of the configuration, never {@code null}
	 */

	public abstract String getDescription();

	/**
	 * Checks if this configuration is equivalent to another configuration.
	 * <p>
	 * Two configurations are considered equivalent if they would produce
	 * identical formatting results when applied to the same source code.
	 *
	 * @param other the configuration to compare with, may be {@code null}
	 * @return {@code true} if the configurations are equivalent, {@code false} otherwise
	 */
	@Override
	public abstract boolean equals(Object other);

	/**
	 * Returns the hash code for this configuration.
	 * <p>
	 * The hash code must be consistent with the equals method and should
	 * include all parameters that affect formatting behavior.
	 *
	 * @return the hash code for this configuration
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Validates a configuration parameter value for security and correctness.
	 * <p>
	 * This utility method can be used by subclasses to validate individual
	 * parameters and ensure they meet security requirements.
	 *
	 * @param parameterName  the name of the parameter being validated, never {@code null}
	 * @param value         the value to validate, may be {@code null}
	 * @param expectedType  the expected type of the value, never {@code null}
	 * @param <T>           the type parameter
	 * @return the validated value cast to the expected type
	 * @throws ConfigurationException if the value is invalid
	 * @throws SecurityException      if the value poses security risks
	 */

	protected static <T> T validateParameter( String parameterName,
	                                         Object value,
	                                          Class<T> expectedType)
		throws ConfigurationException
	{
		if (value == null)
		{
			throw new ConfigurationException("Parameter '" + parameterName + "' cannot be null");
		}

		if (!expectedType.isInstance(value))
		{
			throw new ConfigurationException("Parameter '" + parameterName + "' must be of type " +
				expectedType.getSimpleName() + ", but was " + value.getClass().getSimpleName());
		}

		// Additional security validation for string parameters
		if (value instanceof String stringValue)
		{
			validateStringParameter(parameterName, stringValue);
		}

		return expectedType.cast(value);
	}

	/**
	 * Validates string parameters for security issues.
	 *
	 * @param parameterName the name of the parameter
	 * @param value        the string value to validate
	 * @throws SecurityException if the string contains potentially dangerous content
	 */
	private static void validateStringParameter( String parameterName,  String value)
	{
		// Check for potential injection attempts
		if (value.contains("${") || value.contains("#{") || value.contains("<%") || value.contains("<script"))
		{
			throw new SecurityException(
				"Parameter '" + parameterName + "' contains potentially dangerous content: " + value);
		}

		// Check for excessively long strings that could cause DoS
		if (value.length() > 10_000)
		{
			throw new SecurityException("Parameter '" + parameterName + "' exceeds maximum length limit");
		}
	}

	/**
	 * Validates numeric parameters for reasonable ranges.
	 *
	 * @param parameterName the name of the parameter
	 * @param value        the numeric value to validate
	 * @param minValue     the minimum allowed value (inclusive)
	 * @param maxValue     the maximum allowed value (inclusive)
	 * @throws ConfigurationException if the value is outside the allowed range
	 */
	protected static void validateNumericRange( String parameterName,
	                                         int value,
	                                         int minValue,
	                                         int maxValue)
		throws ConfigurationException
	{
		if (value < minValue || value > maxValue)
		{
			throw new ConfigurationException("Parameter '" + parameterName + "' must be between " +
				minValue + " and " + maxValue + ", but was " + value);
		}
	}
}