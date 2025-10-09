package io.github.cowwoc.styler.config;

import io.github.cowwoc.styler.config.exception.ConfigurationSyntaxException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Cache for parsed configuration files.
 * <p>
 * Uses canonical paths as cache keys to handle symbolic links correctly. Multiple threads
 * can safely load and access configurations concurrently without synchronization overhead
 * beyond the internal ConcurrentHashMap mechanisms.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ConfigurationCache
{
	private final ConcurrentMap<Path, ConfigBuilder> cache = new ConcurrentHashMap<>();
	private final ConfigParser parser;

	/**
	 * Creates a new configuration cache.
	 *
	 * @param parser the parser to use for loading configurations
	 * @throws NullPointerException if {@code parser} is null
	 */
	public ConfigurationCache(ConfigParser parser)
	{
		requireThat(parser, "parser").isNotNull();
		this.parser = parser;
	}

	/**
	 * Gets a configuration builder from cache or parses it if not cached.
	 * <p>
	 * Uses canonical paths for cache keys to correctly handle symbolic links and path
	 * variations (e.g., "./config.toml" and "config.toml" map to the same cache entry).
	 *
	 * @param path the path to the configuration file
	 * @return the cached or newly parsed configuration builder
	 * @throws NullPointerException         if {@code path} is null
	 * @throws ConfigurationSyntaxException if parsing fails
	 */
	public ConfigBuilder get(Path path) throws ConfigurationSyntaxException
	{
		requireThat(path, "path").isNotNull();

		try
		{
			// Use canonical path as cache key (handles symlinks correctly)
			Path canonicalPath = path.toRealPath();

			// computeIfAbsent is atomic - only one thread will parse for a given path
			return cache.computeIfAbsent(canonicalPath, p ->
			{
				try
				{
					return parser.parse(p);
				}
				catch (ConfigurationSyntaxException e)
				{
					// Wrap checked exception as unchecked for lambda compatibility
					throw new RuntimeException(e);
				}
			});
		}
		catch (RuntimeException e)
		{
			// Unwrap and rethrow checked exception
			if (e.getCause() instanceof ConfigurationSyntaxException cse)
				throw cse;
			throw e;
		}
		catch (IOException e)
		{
			// toRealPath() failed - file doesn't exist or isn't accessible
			// Fall back to parsing directly (will throw appropriate exception)
			return parser.parse(path);
		}
	}
}
