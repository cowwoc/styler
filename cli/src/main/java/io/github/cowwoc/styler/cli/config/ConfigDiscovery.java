package io.github.cowwoc.styler.cli.config;

import io.github.cowwoc.styler.cli.config.exceptions.ConfigNotFoundException;
import io.github.cowwoc.styler.formatter.api.GlobalConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main orchestrator for configuration discovery and loading.
 * Searches for configuration files across multiple locations and merges them with proper precedence.
 *
 * This class uses the Builder pattern to provide flexible configuration of the discovery process.
 * It coordinates ConfigSearchPath, ConfigParser, and ConfigMerger to provide a complete solution.
 */
public final class ConfigDiscovery
{
	private final ConfigSearchPath searchPath;
	private final ConfigParser parser;
	private final ConfigMerger merger;

	// Simple cache for configuration discovery results (thread-safe)
	private static final Map<String, GlobalConfiguration> configCache = new ConcurrentHashMap<>();

	/**
	 * Creates a new ConfigDiscovery instance with default components.
	 */
	public ConfigDiscovery()
	{
		this.searchPath = new ConfigSearchPath();
		this.parser = new ConfigParser();
		this.merger = new ConfigMerger();
	}

	/**
	 * Discovers and loads configuration from multiple sources with proper precedence.
	 * Search order: explicit config → current directory → parent directories → home → global
	 * Results are cached for performance (target: <50ms discovery time).
	 *
	 * @param startingPath   the directory to start searching from
	 * @param explicitConfig optional explicit configuration file path (takes highest precedence)
	 * @return the merged global configuration
	 * @throws ConfigNotFoundException if no configuration files can be found and no defaults are available
	 * @throws NullPointerException   if startingPath is null
	 */
	public GlobalConfiguration discover(Path startingPath, Path explicitConfig)
	{
		return discoverWithLocations(startingPath, explicitConfig).getConfiguration();
	}

	/**
	 * Discovers and loads configuration from multiple sources with proper precedence,
	 * returning both the configuration and the discovered file locations.
	 *
	 * @param startingPath   the directory to start searching from
	 * @param explicitConfig optional explicit configuration file path (takes highest precedence)
	 * @return the discovery result containing both configuration and discovered locations
	 * @throws ConfigNotFoundException if no configuration files can be found and no defaults are available
	 * @throws NullPointerException   if startingPath is null
	 */
	public DiscoveryResult discoverWithLocations(Path startingPath, Path explicitConfig)
	{
		if (startingPath == null)
			throw new NullPointerException("startingPath cannot be null");

		// Create cache key based on starting path and explicit config
		String cacheKey = startingPath.toAbsolutePath().normalize().toString() +
		                  (explicitConfig != null ? "|" + explicitConfig.toAbsolutePath().normalize().toString() : "");

		// Check cache first for performance (target: <50ms)
		GlobalConfiguration cached = configCache.get(cacheKey);
		if (cached != null)
		{
			// For cached results, we need to re-discover locations since we don't cache them
			// This is acceptable since location discovery is fast compared to parsing
			List<Path> cachedDiscoveredLocations = new ArrayList<>();
			if (explicitConfig != null)
			{
				cachedDiscoveredLocations.add(explicitConfig);
			}
			else
			{
				List<Path> searchPaths = searchPath.buildSearchPath(startingPath);
				List<Path> configFiles = searchPath.discoverConfigFiles(searchPaths);
				cachedDiscoveredLocations.addAll(configFiles);
			}
			return new DiscoveryResult(cached, cachedDiscoveredLocations);
		}

		List<Path> currentDiscoveredLocations = new ArrayList<>();
		List<GlobalConfiguration> configurations = new ArrayList<>();

		// Handle explicit config file if provided
		if (explicitConfig != null)
		{
			GlobalConfiguration explicitConfiguration = parser.parse(explicitConfig);
			configurations.add(explicitConfiguration);
			currentDiscoveredLocations.add(explicitConfig);
		}
		else
		{
			// Build search path and discover config files
			List<Path> searchPaths = searchPath.buildSearchPath(startingPath);
			List<Path> configFiles = searchPath.discoverConfigFiles(searchPaths);

			if (configFiles.isEmpty())
			{
				throw new ConfigNotFoundException(searchPaths);
			}

			// Parse discovered configurations in reverse order (lowest to highest precedence)
			for (int i = configFiles.size() - 1; i >= 0; i--)
			{
				Path configFile = configFiles.get(i);
				GlobalConfiguration config = parser.parse(configFile);
				configurations.add(config);
				currentDiscoveredLocations.add(configFile);
			}
		}

		// If no configurations found, provide default
		GlobalConfiguration result;
		if (configurations.isEmpty())
		{
			result = new GlobalConfiguration();
		}
		else
		{
			// Merge all configurations with proper precedence
			result = merger.merge(configurations);
		}

		// Cache the result for performance (target: <50ms for subsequent calls)
		configCache.put(cacheKey, result);

		return new DiscoveryResult(result, currentDiscoveredLocations);
	}

	/**
	 * Discovers configuration and applies CLI overrides.
	 * This is the main entry point for CLI usage.
	 *
	 * @param startingPath   the directory to start searching from
	 * @param explicitConfig optional explicit configuration file path
	 * @param cliOverrides   map of CLI override values
	 * @return the final configuration with all sources merged and CLI overrides applied
	 * @throws ConfigNotFoundException if no configuration files can be found
	 * @throws NullPointerException   if startingPath or cliOverrides is null
	 */
	public GlobalConfiguration discoverWithOverrides(Path startingPath, Path explicitConfig,
	                                                 Map<String, Object> cliOverrides)
	{
		if (cliOverrides == null)
			throw new NullPointerException("cliOverrides cannot be null");

		// First discover base configuration
		GlobalConfiguration baseConfig = discover(startingPath, explicitConfig);

		// Apply CLI overrides (highest precedence)
		return merger.applyOverrides(baseConfig, cliOverrides);
	}

	/**
	 * Represents the result of a configuration discovery operation.
	 * Contains both the merged configuration and the list of discovered file locations.
	 */
	public static final class DiscoveryResult
	{
		private final GlobalConfiguration configuration;
		private final List<Path> discoveredLocations;

		public DiscoveryResult(GlobalConfiguration configuration, List<Path> discoveredLocations)
		{
			this.configuration = configuration;
			this.discoveredLocations = List.copyOf(discoveredLocations);
		}

		/**
		 * Returns the merged global configuration.
		 *
		 * @return the configuration result
		 */
		public GlobalConfiguration getConfiguration()
		{
			return configuration;
		}

		/**
		 * Returns the list of configuration file paths that were discovered.
		 * This is useful for debugging and diagnostics.
		 *
		 * @return an immutable list of discovered configuration file paths
		 */
		public List<Path> getDiscoveredLocations()
		{
			return discoveredLocations;
		}
	}

	/**
	 * Creates a new Builder for configuring ConfigDiscovery.
	 *
	 * @return a new Builder instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Builder for configuring ConfigDiscovery instances.
	 * Provides a fluent API for setting up discovery parameters.
	 */
	public static final class Builder
	{
		private final Map<String, Object> cliOverrides;

		private Builder()
		{
			this.cliOverrides = new HashMap<>();
		}

		/**
		 * Adds a CLI override value for a configuration property.
		 *
		 * @param key   the configuration property name
		 * @param value the override value
		 * @return this builder for method chaining
		 * @throws NullPointerException if key is null
		 */
		public Builder withOverride(String key, Object value)
		{
			if (key == null)
				throw new NullPointerException("key cannot be null");

			cliOverrides.put(key, value);
			return this;
		}

		/**
		 * Adds multiple CLI override values.
		 *
		 * @param overrides map of configuration property names to override values
		 * @return this builder for method chaining
		 * @throws NullPointerException if overrides is null
		 */
		public Builder withOverrides(Map<String, Object> overrides)
		{
			if (overrides == null)
				throw new NullPointerException("overrides cannot be null");

			cliOverrides.putAll(overrides);
			return this;
		}

		/**
		 * Builds the ConfigDiscovery instance and performs configuration discovery.
		 *
		 * @param startingPath   the directory to start searching from
		 * @param explicitConfig optional explicit configuration file path
		 * @return the discovered and merged configuration
		 */
		public GlobalConfiguration build(Path startingPath, Path explicitConfig)
		{
			ConfigDiscovery discovery = new ConfigDiscovery();
			return discovery.discoverWithOverrides(startingPath, explicitConfig, cliOverrides);
		}

		/**
		 * Builds the ConfigDiscovery instance and performs configuration discovery from current directory.
		 *
		 * @return the discovered and merged configuration
		 */
		public GlobalConfiguration build()
		{
			return build(Paths.get("."), null);
		}
	}
}