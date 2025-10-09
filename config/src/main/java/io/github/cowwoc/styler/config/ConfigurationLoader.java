package io.github.cowwoc.styler.config;

import io.github.cowwoc.styler.config.exception.ConfigurationException;
import io.github.cowwoc.styler.config.exception.ConfigurationSyntaxException;
import io.github.cowwoc.styler.config.exception.ConfigurationValidationException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Main public API for loading Styler configurations.
 * <p>
 * This facade integrates discovery, parsing, and merging to provide a simple configuration
 * loading system. Configurations are discovered hierarchically, parsed from TOML files,
 * validated individually, and merged with field-level precedence.
 * <p>
 * Example usage:
 * <pre>{@code
 * ConfigurationLoader loader = new ConfigurationLoader();
 * Config config = loader.load(Path.of("."));  // Load from current directory
 * }</pre>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ConfigurationLoader
{
	private final ConfigDiscovery discovery;
	private final ConfigParser parser;
	private final ConfigMerger merger;

	/**
	 * Creates a new configuration loader with default components.
	 */
	public ConfigurationLoader()
	{
		this.discovery = new ConfigDiscovery();
		this.parser = new ConfigParser();
		this.merger = new ConfigMerger();
	}

	/**
	 * Creates a new configuration loader with custom components.
	 *
	 * @param discovery the discovery strategy to use
	 * @param parser    the parser to use for TOML files
	 * @param merger    the merger to use for combining configurations
	 * @throws NullPointerException if any parameter is null
	 */
	public ConfigurationLoader(ConfigDiscovery discovery, ConfigParser parser, ConfigMerger merger)
	{
		requireThat(discovery, "discovery").isNotNull();
		requireThat(parser, "parser").isNotNull();
		requireThat(merger, "merger").isNotNull();
		this.discovery = discovery;
		this.parser = parser;
		this.merger = merger;
	}

	/**
	 * Loads configuration for the given starting directory.
	 * <p>
	 * This method performs the complete load workflow:
	 * <ol>
	 * <li>Discover config files (current → parents → ~/.styler.toml → /etc/.styler.toml)</li>
	 * <li>Parse and validate each file individually</li>
	 * <li>Merge configs with field-level precedence (nearest wins)</li>
	 * <li>Return final merged configuration</li>
	 * </ol>
	 * <p>
	 * If no configuration files are found, returns default configuration.
	 * <p>
	 * Each configuration file is validated before merging. If validation fails, the error message
	 * includes the file path and line number where the invalid value was defined (format:
	 * {@code path@lineNumber: message}).
	 *
	 * @param startDir the directory to start searching from (typically current working directory)
	 * @return the merged configuration
	 * @throws NullPointerException                if {@code startDir} is null
	 * @throws ConfigurationSyntaxException       if any TOML file has syntax errors
	 * @throws ConfigurationValidationException if any configuration values violate business rules
	 */
	public Config load(Path startDir) throws ConfigurationException
	{
		requireThat(startDir, "startDir").isNotNull();

		// 1. Discover configuration files
		List<Path> configPaths = discovery.discover(startDir);

		// 2. Parse and validate each file individually
		List<ConfigBuilder> builders = new ArrayList<>();

		for (Path configPath : configPaths)
		{
			ConfigBuilder builder = parser.parse(configPath);

			// Validate this config file before adding to merge list
			try
			{
				builder.build();
			}
			catch (IllegalArgumentException e)
			{
				// Extract field name from exception message (if available)
				// For now, assume maxLineLength is the only validated field
				Integer lineNumber = builder.getFieldLineNumber("maxLineLength");

				// Format location with line number if available (>= 1)
				String location;
				if (lineNumber != null && lineNumber >= 1)
				{
					location = configPath + "@" + lineNumber;
				}
				else
				{
					// Line number not available or invalid - show just file path
					location = configPath.toString();
				}

				throw new ConfigurationValidationException(
					location + ": " + e.getMessage(), e);
			}

			builders.add(builder);
		}

		// 3. Merge validated configurations with field-level precedence
		return merger.merge(builders);
	}

	/**
	 * Loads configuration and returns it as a builder for further customization.
	 * <p>
	 * Useful when you want to load base configuration from files and then override
	 * specific values programmatically.
	 *
	 * @param startDir the directory to start searching from
	 * @return a builder with merged configuration values
	 * @throws NullPointerException                if {@code startDir} is null
	 * @throws ConfigurationSyntaxException       if any TOML file has syntax errors
	 * @throws ConfigurationValidationException if any configuration values violate business rules
	 */
	public ConfigBuilder loadAsBuilder(Path startDir) throws ConfigurationException
	{
		requireThat(startDir, "startDir").isNotNull();

		// Load merged configuration
		Config merged = load(startDir);

		// Convert final merged Config to ConfigBuilder for further customization
		ConfigBuilder builder = new ConfigBuilder();
		if (merged.maxLineLength() != Config.DEFAULT_MAX_LINE_LENGTH)
			builder.maxLineLength(merged.maxLineLength());

		return builder;
	}
}
