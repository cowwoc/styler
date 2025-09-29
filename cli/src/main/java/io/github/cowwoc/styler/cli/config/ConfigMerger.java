package io.github.cowwoc.styler.cli.config;

import io.github.cowwoc.styler.formatter.api.GlobalConfiguration;
import io.github.cowwoc.styler.formatter.api.GlobalConfiguration.IndentationType;

import java.util.List;
import java.util.Map;

/**
 * Handles merging of multiple configuration sources with proper precedence rules.
 * Higher precedence configurations override lower precedence ones.
 * Precedence order: CLI overrides > project config > parent config > home config > global config
 */
public final class ConfigMerger
{
	/**
	 * Merges multiple configuration objects according to precedence rules.
	 * Configurations are processed in order, with later configurations taking precedence over earlier ones.
	 *
	 * @param configs the list of configurations to merge, ordered from lowest to highest precedence
	 * @return the merged configuration, or a default configuration if the list is empty
	 * @throws NullPointerException if configs is null
	 */
	public GlobalConfiguration merge(List<GlobalConfiguration> configs)
	{
		if (configs == null)
			throw new NullPointerException("configs cannot be null");

		if (configs.isEmpty())
			return new GlobalConfiguration();

		// Start with the first config as the base
		GlobalConfiguration result = configs.get(0);

		// Merge each subsequent config, with later configs taking precedence
		for (int i = 1; i < configs.size(); i++)
		{
			result = mergeTwo(result, configs.get(i));
		}

		return result;
	}

	/**
	 * Applies CLI command-line overrides to a base configuration.
	 * CLI overrides always take the highest precedence.
	 *
	 * @param base        the base configuration to apply overrides to
	 * @param cliOverrides map of CLI override values keyed by configuration property names
	 * @return the configuration with CLI overrides applied
	 * @throws NullPointerException if base or cliOverrides is null
	 */
	public GlobalConfiguration applyOverrides(GlobalConfiguration base, Map<String, Object> cliOverrides)
	{
		if (base == null)
			throw new NullPointerException("base cannot be null");
		if (cliOverrides == null)
			throw new NullPointerException("cliOverrides cannot be null");

		if (cliOverrides.isEmpty())
			return base;

		// Apply CLI overrides using the withXxx methods available on GlobalConfiguration
		GlobalConfiguration result = base;

		for (Map.Entry<String, Object> override : cliOverrides.entrySet())
		{
			String key = override.getKey();
			Object value = override.getValue();

			if (value != null)
			{
				result = applyOverride(result, key, value);
			}
		}

		return result;
	}

	/**
	 * Merges two configurations, with the second taking precedence over the first.
	 * Uses available getter and withXxx methods to implement proper field-level merging.
	 *
	 * @param lower  the lower precedence configuration
	 * @param higher the higher precedence configuration
	 * @return the merged configuration with higher precedence values overriding lower
	 */
	private GlobalConfiguration mergeTwo(GlobalConfiguration lower, GlobalConfiguration higher)
	{
		// Start with the lower precedence configuration
		GlobalConfiguration result = lower;

		// Apply higher precedence values for fields with available withXxx methods
		// Only override if the higher config has non-default values

		// Merge max line length if different from default
		if (higher.getMaxLineLength() != lower.getMaxLineLength())
		{
			result = result.withMaxLineLength(higher.getMaxLineLength());
		}

		// Merge indentation if different from lower
		if (higher.getIndentationType() != lower.getIndentationType() ||
		    higher.getIndentationSize() != lower.getIndentationSize())
		{
			result = result.withIndentation(higher.getIndentationType(), higher.getIndentationSize());
		}

		// For fields without withXxx methods, we need to check if they differ
		// GlobalConfiguration only provides withMaxLineLength() and withIndentation() methods
		// For other fields (charset, lineEnding, insertFinalNewline, trimTrailingWhitespace, tabWidth)
		// we must use the entire higher precedence configuration when they differ
		if (!fieldsEqual(lower, higher))
		{
			// Fields without withXxx methods differ, so we preserve all higher precedence values
			// but apply the merged maxLineLength and indentation from our previous merging
			// This ensures field-level merging for available withXxx methods while preserving
			// the atomic nature of fields that can't be individually merged
			GlobalConfiguration higherWithMergedFields = higher;

			// Apply merged maxLineLength if it was modified
			if (result.getMaxLineLength() != higher.getMaxLineLength())
			{
				higherWithMergedFields = higherWithMergedFields.withMaxLineLength(result.getMaxLineLength());
			}

			// Apply merged indentation if it was modified
			if (result.getIndentationType() != higher.getIndentationType() ||
			    result.getIndentationSize() != higher.getIndentationSize())
			{
				higherWithMergedFields = higherWithMergedFields.withIndentation(
					result.getIndentationType(), result.getIndentationSize());
			}

			return higherWithMergedFields;
		}

		return result;
	}

	/**
	 * Checks if two configurations have equal field values for fields without withXxx methods.
	 *
	 * @param config1 first configuration
	 * @param config2 second configuration
	 * @return true if all non-mergeable fields are equal
	 */
	private boolean fieldsEqual(GlobalConfiguration config1, GlobalConfiguration config2)
	{
		return config1.getLineEnding().equals(config2.getLineEnding()) &&
		       config1.getCharset().equals(config2.getCharset()) &&
		       config1.isInsertFinalNewline() == config2.isInsertFinalNewline() &&
		       config1.isTrimTrailingWhitespace() == config2.isTrimTrailingWhitespace() &&
		       config1.getTabWidth() == config2.getTabWidth();
	}

	/**
	 * Applies a single CLI override to the configuration.
	 *
	 * @param config the configuration to modify
	 * @param key    the configuration property name
	 * @param value  the override value
	 * @return the modified configuration
	 */
	private GlobalConfiguration applyOverride(GlobalConfiguration config, String key, Object value)
	{
		// Apply the override based on the key using available withXxx methods
		return switch (key)
		{
			case "maxLineLength" -> {
				if (value instanceof Integer intValue)
					yield config.withMaxLineLength(intValue);
				else
					yield config;
			}
			case "indentationSize" -> {
				if (value instanceof Integer intValue)
					yield config.withIndentation(config.getIndentationType(), intValue);
				else
					yield config;
			}
			case "indentationType" -> {
				if (value instanceof String stringValue)
				{
					try
					{
						IndentationType indentType = IndentationType.valueOf(stringValue.toUpperCase());
						yield config.withIndentation(indentType, config.getIndentationSize());
					}
					catch (IllegalArgumentException e)
					{
						// Invalid indentation type, ignore
						yield config;
					}
				}
				else
					yield config;
			}
			// Note: Other properties (charset, lineEnding, etc.) don't have withXxx methods yet
			// These would need to be supported when GlobalConfiguration API expands
			default -> config; // Ignore unknown properties for now
		};
	}
}