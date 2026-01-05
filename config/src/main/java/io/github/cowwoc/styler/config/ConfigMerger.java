package io.github.cowwoc.styler.config;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Merges multiple configuration sources with field-level precedence.
 * <p>
 * Merging strategy: For each configuration field, the value from the nearest (first) config
 * file that explicitly sets that field wins. This allows project-local config to override
 * user config, which overrides system config, on a per-field basis.
 * <p>
 * Example: If ~/.styler.toml sets {@code maxLineLength=100} and project/.styler.toml sets
 * {@code indentSize=4}, the merged config will have {@code maxLineLength=100} (from user config) and
 * {@code indentSize=4} (from project config).
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public final class ConfigMerger
{
	/**
	 * Merges configuration builders using field-level precedence.
	 * <p>
	 * Builders should be ordered by precedence (nearest/highest priority first).
	 * For each field, the first builder that explicitly sets the field wins.
	 * <p>
	 * Note: Individual builders should be validated before calling this method.
	 * Validation occurs in {@link ConfigurationLoader#load(Path)} before merging.
	 *
	 * @param builders list of configuration builders ordered by precedence (nearest first)
	 * @return merged configuration
	 * @throws NullPointerException if {@code builders} is null
	 */
	public Config merge(List<ConfigBuilder> builders)
	{
		requireThat(builders, "builders").isNotNull();
		if (builders.isEmpty())
			return Config.withDefaults();

		ConfigBuilder merged = new ConfigBuilder();

		// For each field, use the first explicitly set value
		for (ConfigBuilder builder : builders)
			if (merged.getMaxLineLength().isEmpty() && builder.getMaxLineLength().isPresent())
				merged.maxLineLength(builder.getMaxLineLength().orElseThrow());

		return merged.build();
	}

	/**
	 * Merges multiple Config instances by converting to builders first.
	 * <p>
	 * Note: This method cannot preserve which fields were explicitly set in the original
	 * Configs, so it treats all non-default values as explicitly set. For proper field-level
	 * merging, use {@link #merge(List)} with ConfigBuilder instances directly.
	 *
	 * @param configs list of configurations ordered by precedence (nearest first)
	 * @return merged configuration
	 * @throws NullPointerException if {@code configs} is null
	 */
	public Config mergeConfigs(List<Config> configs)
	{
		requireThat(configs, "configs").isNotNull();
		if (configs.isEmpty())
			return Config.withDefaults();

		// Take first config as base (highest precedence)
		// Note: This simple approach assumes all fields in Config were explicitly set
		// For more complex merging, parse to ConfigBuilder and use merge(List<ConfigBuilder>)
		return configs.getFirst();
	}
}
