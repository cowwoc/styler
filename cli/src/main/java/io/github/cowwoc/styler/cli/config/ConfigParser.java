package io.github.cowwoc.styler.cli.config;

import tools.jackson.core.JacksonException;
import io.github.cowwoc.styler.cli.config.exceptions.ConfigValidationException;
import io.github.cowwoc.styler.cli.config.exceptions.FileAccessException;
import io.github.cowwoc.styler.formatter.api.GlobalConfiguration;
import tools.jackson.dataformat.toml.TomlMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Parses configuration files in TOML format.
 * Focuses specifically on .styler.toml configuration files as per task requirements.
 */
public final class ConfigParser
{
	private final TomlMapper tomlMapper;

	/**
	 * Creates a new configuration parser with TOML support.
	 */
	public ConfigParser()
	{
		this.tomlMapper = TomlMapper.builder().
			findAndAddModules().  // Enable Java record support
			build();
	}

	/**
	 * Parses a TOML configuration file and returns the configuration object.
	 *
	 * @param configFile the path to the TOML configuration file to parse
	 * @return the parsed global configuration
	 * @throws FileAccessException      if the file cannot be read
	 * @throws ConfigValidationException if the file format is invalid or parsing fails
	 * @throws NullPointerException     if configFile is {@code null}
	 */
	public GlobalConfiguration parse(Path configFile)
	{
		if (configFile == null)
			throw new NullPointerException("configFile cannot be null");

		// Check file accessibility
		if (!Files.isReadable(configFile))
		{
			throw new FileAccessException(configFile, "File is not readable or does not exist");
		}

		// Validate it's a TOML file
		validateTomlFormat(configFile);

		try
		{
			// Read and parse the TOML configuration file
			return tomlMapper.readValue(configFile.toFile(), GlobalConfiguration.class);
		}
		catch (JacksonException e)
		{
			throw new ConfigValidationException(configFile,
				"Failed to parse TOML configuration: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			throw new ConfigValidationException(configFile,
				"Configuration validation failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Validates that the configuration file has a TOML extension.
	 *
	 * @param configFile the configuration file to validate
	 * @throws ConfigValidationException if the file format is not TOML
	 */
	private void validateTomlFormat(Path configFile)
	{
		String filename = configFile.getFileName().toString().toLowerCase(Locale.ENGLISH);

		if (!filename.endsWith(".toml"))
		{
			throw new ConfigValidationException(configFile,
				"Unsupported configuration file format. Expected .toml extension but found: " + filename);
		}
	}
}