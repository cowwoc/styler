package io.github.cowwoc.styler.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Builder for constructing {@link Config} instances.
 * <p>
 * This builder provides a fluent API for setting configuration values with sensible defaults.
 * All values are validated when {@link #build()} is called. Jackson TOML deserialization uses
 * this class directly via setter methods.
 * <p>
 * Uses {@code Optional} to track which fields were explicitly set vs defaulted, enabling proper
 * field-level merging in {@link ConfigMerger}.
 */
public final class ConfigBuilder
{
	private Optional<Integer> maxLineLength = Optional.empty();
	private final Map<String, Integer> fieldLineNumbers = new HashMap<>();

	/**
	 * Creates a new builder with default values.
	 * Public constructor required for Jackson deserialization.
	 */
	public ConfigBuilder()
	{
	}

	/**
	 * Sets the maximum line length.
	 *
	 * @param maxLineLength the maximum line length
	 * @return this builder for method chaining
	 */
	@JsonSetter("maxLineLength")
	public ConfigBuilder maxLineLength(int maxLineLength)
	{
		this.maxLineLength = Optional.of(maxLineLength);
		return this;
	}

	/**
	 * Returns the maximum line length if explicitly set.
	 *
	 * @return the maximum line length if set, empty otherwise
	 */
	@JsonGetter("maxLineLength")
	public Optional<Integer> getMaxLineLength()
	{
		return maxLineLength;
	}

	/**
	 * Sets the line number for a specific field.
	 * Used by custom deserializer to track where fields are defined in TOML files.
	 *
	 * @param fieldName the name of the field
	 * @param lineNumber the line number in the TOML file (1-based)
	 * @throws NullPointerException if {@code fieldName} is null
	 */
	public void setFieldLineNumber(String fieldName, int lineNumber)
	{
		requireThat(fieldName, "fieldName").isNotNull();
		fieldLineNumbers.put(fieldName, lineNumber);
	}

	/**
	 * Returns the line number for a specific field.
	 *
	 * @param fieldName the name of the field
	 * @return the line number if tracked, or {@code null} if not tracked
	 * @throws NullPointerException if {@code fieldName} is null
	 */
	public Integer getFieldLineNumber(String fieldName)
	{
		requireThat(fieldName, "fieldName").isNotNull();
		return fieldLineNumbers.get(fieldName);
	}

	/**
	 * Builds a Config instance with the current settings.
	 * Uses defaults for any fields not explicitly set.
	 * <p>
	 * Validates all field values. For example, {@code maxLineLength} must be positive.
	 *
	 * @return a new immutable Config
	 * @throws IllegalArgumentException if any values violate constraints (e.g., {@code maxLineLength} not positive)
	 */
	public Config build()
	{
		int finalMaxLineLength = maxLineLength.orElse(Config.DEFAULT_MAX_LINE_LENGTH);
		return new Config(finalMaxLineLength);
	}
}
