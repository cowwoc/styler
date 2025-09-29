package io.github.cowwoc.styler.cli.config.exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when a configuration file exists but contains invalid format or content.
 * Provides context about which file failed validation and why.
 */
public class ConfigValidationException extends ConfigDiscoveryException
{
	private final Path configFile;

	/**
	 * Creates a new config validation exception.
	 *
	 * @param configFile the path to the configuration file that failed validation
	 * @param message    the error message describing the validation failure
	 * @throws NullPointerException if configFile or message is null
	 */
	public ConfigValidationException(Path configFile, String message)
	{
		super(buildMessage(configFile, message));
		this.configFile = configFile;
	}

	/**
	 * Creates a new config validation exception with a cause.
	 *
	 * @param configFile the path to the configuration file that failed validation
	 * @param message    the error message describing the validation failure
	 * @param cause      the underlying exception that caused the validation failure
	 * @throws NullPointerException if configFile, message, or cause is null
	 */
	public ConfigValidationException(Path configFile, String message, Throwable cause)
	{
		super(buildMessage(configFile, message), cause);
		this.configFile = configFile;
	}

	/**
	 * Returns the path to the configuration file that failed validation.
	 *
	 * @return the configuration file path
	 */
	public Path getConfigFile()
	{
		return configFile;
	}

	/**
	 * Builds the error message from the config file and validation error.
	 *
	 * @param configFile the configuration file that failed
	 * @param message    the validation error message
	 * @return a descriptive error message
	 */
	private static String buildMessage(Path configFile, String message)
	{
		return "Configuration file validation failed: " + configFile.toAbsolutePath() +
			"\nReason: " + message +
			"\n\nPlease check the file format and content. Configuration files should be valid TOML or YAML.";
	}
}