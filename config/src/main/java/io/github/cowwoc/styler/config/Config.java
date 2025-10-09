package io.github.cowwoc.styler.config;

import io.github.cowwoc.styler.config.exception.ConfigurationValidationException;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Immutable configuration for Styler formatting rules.
 * <p>
 * This record encapsulates all formatting settings loaded from TOML files. Values are validated
 * on construction to ensure they meet business rule constraints (e.g., positive line lengths,
 * valid enum values). Use {@link ConfigBuilder} to construct instances.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public record Config(int maxLineLength)
{
	/**
	 * Default maximum line length (120 characters).
	 */
	public static final int DEFAULT_MAX_LINE_LENGTH = 120;

	/**
	 * Creates a new configuration with validated values.
	 * <p>
	 * Note: Validation is performed, but exceptions are thrown as unchecked since
	 * record canonical constructors cannot declare checked exceptions. Use {@link ConfigBuilder#build()}
	 * for checked exception handling.
	 *
	 * @param maxLineLength the maximum line length (must be positive)
	 * @throws IllegalArgumentException if {@code maxLineLength} is not positive
	 */
	public Config
	{
		requireThat(maxLineLength, "maxLineLength").isPositive();
	}

	/**
	 * Creates a configuration with default values.
	 *
	 * @return a new Config with all default settings
	 */
	public static Config withDefaults()
	{
		return new Config(DEFAULT_MAX_LINE_LENGTH);
	}

	/**
	 * Creates a builder for constructing Config instances.
	 *
	 * @return a new ConfigBuilder initialized with default values
	 */
	public static ConfigBuilder builder()
	{
		return new ConfigBuilder();
	}
}
