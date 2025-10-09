package io.github.cowwoc.styler.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Discovers configuration files using hierarchical search strategy.
 * <p>
 * Search order:
 * <ol>
 * <li>Current directory → parent directories (stops at .git boundary)</li>
 * <li>~/.styler.toml (user home directory)</li>
 * <li>/etc/styler.toml (system-wide configuration)</li>
 * </ol>
 * <p>
 * The .git boundary prevents configuration from leaking across repository boundaries
 * (e.g., when working in a nested git repository, don't read parent repo's config).
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public final class ConfigDiscovery
{
	private static final String CONFIG_FILENAME = ".styler.toml";
	private static final String GIT_DIR = ".git";
	/**
	 * Maximum directory traversal depth (100 levels) to prevent performance issues.
	 */
	private static final int MAX_TRAVERSAL_DEPTH = 100;

	/**
	 * Discovers all configuration files for the given starting directory.
	 * <p>
	 * Returns files in order of precedence: nearest (current dir) to farthest (system-wide).
	 * This order is used by {@link ConfigMerger} where nearest config wins for conflicting fields.
	 *
	 * @param startDir the directory to start searching from (typically current working directory)
	 * @return list of configuration file paths, ordered by precedence (nearest first)
	 * @throws NullPointerException if {@code startDir} is null
	 */
	public List<Path> discover(Path startDir)
	{
		requireThat(startDir, "startDir").isNotNull();
		List<Path> configs = new ArrayList<>();

		// Search current directory and parents (until .git boundary)
		Optional<Path> current = Optional.of(startDir.toAbsolutePath().normalize());
		int depth = 0;
		while (current.isPresent())
		{
			// Prevent excessive traversal (security)
			if (depth >= MAX_TRAVERSAL_DEPTH)
				break;

			Path dir = current.get();

			// Check for .styler.toml in current directory
			Path configFile = dir.resolve(CONFIG_FILENAME);
			if (Files.exists(configFile) && Files.isRegularFile(configFile))
				configs.add(configFile);

			// Stop at .git boundary (prevents cross-repo pollution)
			Path gitDir = dir.resolve(GIT_DIR);
			if (Files.exists(gitDir) && Files.isDirectory(gitDir))
				break;

			// Move to parent directory
			current = Optional.ofNullable(dir.getParent());
			depth++;
		}

		// Check user home directory
		String userHome = System.getProperty("user.home");
		if (userHome != null)
		{
			Path userConfig = Path.of(userHome, CONFIG_FILENAME);
			if (Files.exists(userConfig) && Files.isRegularFile(userConfig))
				configs.add(userConfig);
		}

		// Check system-wide configuration
		Path systemConfig = Path.of("/etc", CONFIG_FILENAME);
		if (Files.exists(systemConfig) && Files.isRegularFile(systemConfig))
			configs.add(systemConfig);

		return configs;
	}
}
