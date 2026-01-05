package io.github.cowwoc.styler.cli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Immutable container for parsed command-line options.
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe and can be shared across multiple threads
 * without synchronization.
 *
 * @param inputPaths list of file/directory paths to process (non-null, non-empty)
 * @param configPath optional configuration file path override
 * @param checkMode  true for validation-only mode, false for fix mode
 * @param fixMode    true for auto-fix mode, false otherwise
 * @param classpathEntries paths to JAR files and directories on classpath for type resolution
 * @param modulepathEntries paths to modules on modulepath for type resolution
 */
public record CLIOptions(List<Path> inputPaths, Optional<Path> configPath,
	boolean checkMode, boolean fixMode, List<Path> classpathEntries,
	List<Path> modulepathEntries)
{
	/**
	 * Compact constructor with validation and defensive copying.
	 *
	 * @throws NullPointerException     if {@code inputPaths}, {@code configPath},
	 *                                  {@code classpathEntries}, or
	 *                                  {@code modulepathEntries} is null
	 * @throws IllegalArgumentException if {@code inputPaths} is empty or if both
	 *                                  {@code checkMode} and {@code fixMode} are true
	 */
	public CLIOptions
	{
		requireThat(inputPaths, "inputPaths").isNotEmpty();
		inputPaths = List.copyOf(inputPaths); // Defensive copy

		requireThat(configPath, "configPath").isNotNull();

		requireThat(classpathEntries, "classpathEntries").isNotNull();
		classpathEntries = List.copyOf(classpathEntries);

		requireThat(modulepathEntries, "modulepathEntries").isNotNull();
		modulepathEntries = List.copyOf(modulepathEntries);

		// Business rule: checkMode and fixMode are mutually exclusive
		if (checkMode && fixMode)
			throw new IllegalArgumentException(
				"Cannot enable both check mode and fix mode simultaneously");
	}

	/**
	 * Builder for constructing {@code CLIOptions} instances.
	 * <p>
	 * This builder provides a fluent interface for creating immutable
	 * {@code CLIOptions} objects with validation.
	 */
	public static class Builder
	{
		private final List<Path> inputPaths = new ArrayList<>();
		private final List<Path> classpathEntries = new ArrayList<>();
		private final List<Path> modulepathEntries = new ArrayList<>();
		private Path configPath;
		private boolean checkMode;
		private boolean fixMode;

		/**
		 * Adds an input path to process.
		 *
		 * @param path the file or directory path to add (non-null)
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code path} is null
		 */
		public Builder addInputPath(Path path)
		{
			requireThat(path, "path").isNotNull();
			this.inputPaths.add(path);
			return this;
		}

		/**
		 * Sets the configuration file path override.
		 *
		 * @param path the configuration file path (may be null). If null, no configuration
		 *             override is specified and the formatter will use its default
		 *             configuration discovery mechanism.
		 * @return this builder for method chaining
		 */
		public Builder setConfigPath(Path path)
		{
			this.configPath = path;
			return this;
		}

		/**
		 * Sets check mode (validation-only).
		 *
		 * @param value true to enable check mode
		 * @return this builder for method chaining
		 */
		public Builder setCheckMode(boolean value)
		{
			this.checkMode = value;
			return this;
		}

		/**
		 * Sets fix mode (auto-fix).
		 *
		 * @param value true to enable fix mode
		 * @return this builder for method chaining
		 */
		public Builder setFixMode(boolean value)
		{
			this.fixMode = value;
			return this;
		}

		/**
		 * Sets the classpath entries for type resolution.
		 *
		 * @param entries the classpath entries (JAR files or directories)
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code entries} is {@code null}
		 */
		public Builder setClasspathEntries(List<Path> entries)
		{
			requireThat(entries, "entries").isNotNull();
			this.classpathEntries.clear();
			this.classpathEntries.addAll(entries);
			return this;
		}

		/**
		 * Sets the modulepath entries for type resolution.
		 *
		 * @param entries the modulepath entries (module JARs or directories)
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code entries} is {@code null}
		 */
		public Builder setModulepathEntries(List<Path> entries)
		{
			requireThat(entries, "entries").isNotNull();
			this.modulepathEntries.clear();
			this.modulepathEntries.addAll(entries);
			return this;
		}

		/**
		 * Builds an immutable {@code CLIOptions} instance.
		 *
		 * @return the constructed options
		 * @throws IllegalArgumentException if validation fails (see
		 *                                  {@link CLIOptions} constructor)
		 */
		public CLIOptions build()
		{
			return new CLIOptions(inputPaths, Optional.ofNullable(configPath), checkMode,
			fixMode, classpathEntries, modulepathEntries);
		}
	}
}
