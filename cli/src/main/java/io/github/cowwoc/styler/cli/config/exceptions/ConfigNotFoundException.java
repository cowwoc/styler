package io.github.cowwoc.styler.cli.config.exceptions;

import java.io.Serial;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
/**
 * Exception thrown when no configuration files can be found in any of the search locations.
 * Provides context about which locations were searched.
 */
public class ConfigNotFoundException extends ConfigDiscoveryException
{
	@Serial
	private static final long serialVersionUID = 1L;
	private final transient List<Path> searchedPaths;

	/**
	 * Creates a new config not found exception.
	 *
	 * @param searchedPaths the list of paths that were searched for configuration files
	 * @throws NullPointerException if searchedPaths is {@code null}
	 */
	public ConfigNotFoundException(List<Path> searchedPaths)
	{
		super(buildMessage(Objects.requireNonNull(searchedPaths, "searchedPaths must not be null")));
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

		StringBuilder message = new StringBuilder(512);
	message.append("Configuration files (.styler.toml, .styler.yaml) ").
		append("not found in any of the searched locations:");
		for (Path path : searchedPaths)
		{
			message.append("\n  - ").append(path.toAbsolutePath());
		}
		message.append("\n\nTo resolve this, create a .styler.toml file ").
		append("in your project directory or specify a configuration file with --config");
		return message.toString();
	}
}