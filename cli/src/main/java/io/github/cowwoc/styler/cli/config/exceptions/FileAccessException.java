package io.github.cowwoc.styler.cli.config.exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when a configuration file exists but cannot be accessed due to permission issues.
 * Provides context about which file cannot be accessed and the access issue.
 */
public class FileAccessException extends ConfigDiscoveryException
{
	private final Path configFile;

	/**
	 * Creates a new file access exception.
	 *
	 * @param configFile the path to the configuration file that cannot be accessed
	 * @param message    the error message describing the access issue
	 * @throws NullPointerException if configFile or message is null
	 */
	public FileAccessException(Path configFile, String message)
	{
		super(buildMessage(configFile, message));
		this.configFile = configFile;
	}

	/**
	 * Creates a new file access exception with a cause.
	 *
	 * @param configFile the path to the configuration file that cannot be accessed
	 * @param message    the error message describing the access issue
	 * @param cause      the underlying exception that caused the access failure
	 * @throws NullPointerException if configFile, message, or cause is null
	 */
	public FileAccessException(Path configFile, String message, Throwable cause)
	{
		super(buildMessage(configFile, message), cause);
		this.configFile = configFile;
	}

	/**
	 * Returns the path to the configuration file that cannot be accessed.
	 *
	 * @return the configuration file path
	 */
	public Path getConfigFile()
	{
		return configFile;
	}

	/**
	 * Builds the error message from the config file and access error.
	 *
	 * @param configFile the configuration file that cannot be accessed
	 * @param message    the access error message
	 * @return a descriptive error message
	 */
	private static String buildMessage(Path configFile, String message)
	{
		return "Cannot access configuration file: " + configFile.toAbsolutePath() +
			"\nReason: " + message +
			"\n\nPlease check file permissions and ensure the file is readable.";
	}
}