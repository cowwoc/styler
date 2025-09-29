package io.github.cowwoc.styler.cli.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages the search hierarchy for configuration files across different directory levels.
 * Provides platform-aware resolution of configuration file locations following the precedence:
 * current directory → parent directories → home directory → global directory.
 */
public final class ConfigSearchPath
{
	private static final String[] CONFIG_FILENAMES = {".styler.toml"};

	/**
	 * Builds an ordered search path starting from the specified directory.
	 * The search traverses upward through parent directories until it reaches the filesystem root
	 * or finds a project boundary marker (.git directory).
	 *
	 * @param startDir the directory to start searching from
	 * @return an ordered list of directories to search for configuration files
	 * @throws NullPointerException if startDir is null
	 */
	public List<Path> buildSearchPath(Path startDir)
	{
		if (startDir == null)
			throw new NullPointerException("startDir cannot be null");

		List<Path> searchPath = new ArrayList<>();

		// Add current and parent directories up to project root
		Path current = startDir.toAbsolutePath().normalize();
		while (current != null)
		{
			searchPath.add(current);

			// Stop at project boundary (git repository root)
			if (Files.exists(current.resolve(".git")))
				break;

			Path parent = current.getParent();
			if (parent == null || parent.equals(current))
				break; // Reached filesystem root

			current = parent;
		}

		// Add user home directory
		getUserConfigPath().ifPresent(searchPath::add);

		// Add global system config directory
		getGlobalConfigPath().ifPresent(searchPath::add);

		return searchPath;
	}

	/**
	 * Returns the platform-specific user configuration directory.
	 * On Linux: ~/.config/styler/
	 * On Windows: %APPDATA%/styler/
	 * On macOS: ~/Library/Application Support/styler/
	 *
	 * @return the user configuration directory, or empty if cannot be determined
	 */
	public Optional<Path> getUserConfigPath()
	{
		String homeDir = System.getProperty("user.home");
		if (homeDir == null || homeDir.isEmpty())
			return Optional.empty();

		String osName = System.getProperty("os.name", "").toLowerCase();
		Path configDir;

		if (osName.contains("win"))
		{
			// Windows: %APPDATA%/styler/
			String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isEmpty())
				configDir = Paths.get(appData, "styler");
			else
				configDir = Paths.get(homeDir, "AppData", "Roaming", "styler");
		}
		else if (osName.contains("mac"))
		{
			// macOS: ~/Library/Application Support/styler/
			configDir = Paths.get(homeDir, "Library", "Application Support", "styler");
		}
		else
		{
			// Linux and other Unix-like: ~/.config/styler/
			String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
			if (xdgConfigHome != null && !xdgConfigHome.isEmpty())
				configDir = Paths.get(xdgConfigHome, "styler");
			else
				configDir = Paths.get(homeDir, ".config", "styler");
		}

		return Files.isDirectory(configDir) ? Optional.of(configDir) : Optional.empty();
	}

	/**
	 * Returns the global system configuration directory.
	 * On Linux/macOS: /etc/styler/
	 * On Windows: %PROGRAMDATA%/styler/
	 *
	 * @return the global configuration directory, or empty if cannot be determined or doesn't exist
	 */
	public Optional<Path> getGlobalConfigPath()
	{
		String osName = System.getProperty("os.name", "").toLowerCase();
		Path globalDir;

		if (osName.contains("win"))
		{
			// Windows: %PROGRAMDATA%/styler/
			String programData = System.getenv("PROGRAMDATA");
			if (programData != null && !programData.isEmpty())
				globalDir = Paths.get(programData, "styler");
			else
				return Optional.empty();
		}
		else
		{
			// Linux/macOS: /etc/styler/
			globalDir = Paths.get("/etc", "styler");
		}

		return Files.isDirectory(globalDir) ? Optional.of(globalDir) : Optional.empty();
	}

	/**
	 * Discovers all existing configuration files in the specified search directories.
	 * Searches for both .styler.toml and .styler.yaml files in each directory.
	 *
	 * @param searchPaths the directories to search for configuration files
	 * @return a list of discovered configuration file paths, ordered by precedence
	 * @throws NullPointerException if searchPaths is null
	 */
	public List<Path> discoverConfigFiles(List<Path> searchPaths)
	{
		if (searchPaths == null)
			throw new NullPointerException("searchPaths cannot be null");

		List<Path> configFiles = new ArrayList<>();

		for (Path directory : searchPaths)
		{
			for (String filename : CONFIG_FILENAMES)
			{
				Path configFile = directory.resolve(filename);
				if (Files.isRegularFile(configFile) && Files.isReadable(configFile))
				{
					configFiles.add(configFile);
					// Only take the first config file found in each directory (TOML preferred)
					break;
				}
			}
		}

		return configFiles;
	}
}