package io.github.cowwoc.styler.cli.config.exceptions;

import java.nio.file.Path;
import java.util.List;

/**
 * Exception thrown when no configuration files can be found in any of the search locations.
 * Provides context about which locations were searched.
 */
public class ConfigNotFoundException extends ConfigDiscoveryException
{
	private final List<Path> searchedPaths;

	/**
	 * Creates a new config not found exception.
	 *
	 * @param searchedPaths the list of paths that were searched for configuration files
	 * @throws NullPointerException if searchedPaths is null
	 */
	public ConfigNotFoundException(List<Path> searchedPaths)
	{
		super(buildMessage(searchedPaths));
		this.searchedPaths = List.copyOf(searchedPaths);
	}

	/**
	 * Returns the list of paths that were searched for configuration files.
	 *
	 * @return an immutable list of searched paths
	 */
	public List<Path> getSearchedPaths()
	{
		return searchedPaths;
	}

	/**
	 * Builds the error message from the searched paths.
	 *
	 * @param searchedPaths the paths that were searched
	 * @return a descriptive error message
	 */
	private static String buildMessage(List<Path> searchedPaths)
	{
		if (searchedPaths.isEmpty())
		{
			return "No configuration files found - no search paths were provided";
		}

		StringBuilder message = new StringBuilder("Configuration files (.styler.toml, .styler.yaml) not found in any of the searched locations:");
		for (Path path : searchedPaths)
		{
			message.append("\n  - ").append(path.toAbsolutePath());
		}
		message.append("\n\nTo resolve this, create a .styler.toml file in your project directory or specify a configuration file with --config");
		return message.toString();
	}
}