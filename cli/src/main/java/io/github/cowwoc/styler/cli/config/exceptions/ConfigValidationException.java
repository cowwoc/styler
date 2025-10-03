package io.github.cowwoc.styler.cli.config.exceptions;

import java.io.Serial;
import java.nio.file.Path;
import java.util.Objects;
/**
 * Exception thrown when a configuration file exists but contains invalid format or content.
 * Provides context about which file failed validation and why.
 */
public class ConfigValidationException extends ConfigDiscoveryException
{
	@Serial
	private static final long serialVersionUID = 1L;
	private final transient Path configFile;

	/**
	 * Creates a new config validation exception.
	 *
	 * @param configFile the path to the configuration file that failed validation
	 * @param message    the error message describing the validation failure
	 * @throws NullPointerException if configFile or message is {@code null}
	 */
	public ConfigValidationException(Path configFile, String message)
	{
		super(buildMessage(
			Objects.requireNonNull(configFile, "configFile must not be null"),
			Objects.requireNonNull(message, "message must not be null")));
		this.configFile = configFile;
	}

	/**
	 * Creates a new config validation exception with a cause.
	 *
	 * @param configFile the path to the configuration file that failed validation
	 * @param message    the error message describing the validation failure
	 * @param cause      the underlying exception that caused the validation failure
	 * @throws NullPointerException if configFile, message, or cause is {@code null}
	 */
	public ConfigValidationException(Path configFile, String message, Throwable cause)
	{
		super(buildMessage(
			Objects.requireNonNull(configFile, "configFile must not be null"),
			Objects.requireNonNull(message, "message must not be null")), cause);
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