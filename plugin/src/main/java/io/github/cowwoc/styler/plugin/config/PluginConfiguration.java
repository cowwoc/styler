package io.github.cowwoc.styler.plugin.config;

import org.apache.maven.project.MavenProject;
import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration for Styler Maven plugin.
 * Holds all Maven-specific parameters and provides defensive copying.
 *
 * @param project              Maven project instance
 * @param sourceDirectory      main source directory
 * @param testSourceDirectory  test source directory (optional)
 * @param configFile           configuration file path (optional)
 * @param encoding             source file encoding
 * @param includes             file inclusion patterns
 * @param excludes             file exclusion patterns
 * @param skip                 skip plugin execution flag
 * @param skipTests            skip test source formatting flag
 * @param failOnViolation      fail build on violations flag
 */
public record PluginConfiguration(
	MavenProject project,
	File sourceDirectory,
	File testSourceDirectory,
	File configFile,
	String encoding,
	List<String> includes,
	List<String> excludes,
	boolean skip,
	boolean skipTests,
	boolean failOnViolation)
{
	/**
	 * Creates an immutable plugin configuration with defensive copying of collections.
	 *
	 * @param project              Maven project instance
	 * @param sourceDirectory      main source directory
	 * @param testSourceDirectory  test source directory
	 * @param configFile           optional configuration file path
	 * @param encoding             source file encoding
	 * @param includes             file inclusion patterns
	 * @param excludes             file exclusion patterns
	 * @param skip                 skip plugin execution
	 * @param skipTests            skip test source formatting
	 * @param failOnViolation      fail build on formatting violations
	 * @throws NullPointerException if required parameters are null
	 */
	public PluginConfiguration
	{
		// Defensive copying per code-quality-auditor requirements
		if (includes != null)
		{
			includes = List.copyOf(includes);
		}
		else
		{
			includes = List.of();
		}

		if (excludes != null)
		{
			excludes = List.copyOf(excludes);
		}
		else
		{
			excludes = List.of();
		}

		// Validation per code-quality-auditor requirements
		Objects.requireNonNull(project, "project cannot be null");
		Objects.requireNonNull(encoding, "encoding cannot be null");
		Objects.requireNonNull(sourceDirectory, "sourceDirectory cannot be null");
	}

	/**
	 * Builder for PluginConfiguration.
	 *
	 * @return new builder instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Builder for fluent configuration construction.
	 */
	public static class Builder
	{
		/** Default character encoding for source files. */
		public static final String DEFAULT_ENCODING = "UTF-8";

		/** Default file inclusion patterns. */
		public static final List<String> DEFAULT_INCLUDES = List.of("**/*.java");

		/** Default file exclusion patterns. */
		public static final List<String> DEFAULT_EXCLUDES = List.of();

		private MavenProject project;
		private File sourceDirectory;
		private File testSourceDirectory;
		private File configFile;
		private String encoding = DEFAULT_ENCODING;
		private List<String> includes = DEFAULT_INCLUDES;
		private List<String> excludes = DEFAULT_EXCLUDES;
		private boolean skip;
		private boolean skipTests;
		private boolean failOnViolation = true;

		/**
		 * Sets the Maven project instance.
		 *
		 * @param project Maven project
		 * @return this builder
		 */
		public Builder project(MavenProject project)
		{
			this.project = project;
			return this;
		}

		/**
		 * Sets the main source directory.
		 *
		 * @param sourceDirectory source directory
		 * @return this builder
		 */
		public Builder sourceDirectory(File sourceDirectory)
		{
			this.sourceDirectory = sourceDirectory;
			return this;
		}

		/**
		 * Sets the test source directory.
		 *
		 * @param testSourceDirectory test source directory
		 * @return this builder
		 */
		public Builder testSourceDirectory(File testSourceDirectory)
		{
			this.testSourceDirectory = testSourceDirectory;
			return this;
		}

		/**
		 * Sets the configuration file path.
		 *
		 * @param configFile configuration file
		 * @return this builder
		 */
		public Builder configFile(File configFile)
		{
			this.configFile = configFile;
			return this;
		}

		/**
		 * Sets the source file encoding.
		 *
		 * @param encoding file encoding
		 * @return this builder
		 */
		public Builder encoding(String encoding)
		{
			this.encoding = encoding;
			return this;
		}

		/**
		 * Sets file inclusion patterns.
		 *
		 * @param includes inclusion patterns
		 * @return this builder
		 */
		public Builder includes(List<String> includes)
		{
			this.includes = includes;
			return this;
		}

		/**
		 * Sets file exclusion patterns.
		 *
		 * @param excludes exclusion patterns
		 * @return this builder
		 */
		public Builder excludes(List<String> excludes)
		{
			this.excludes = excludes;
			return this;
		}

		/**
		 * Sets whether to skip plugin execution.
		 *
		 * @param skip skip flag
		 * @return this builder
		 */
		public Builder skip(boolean skip)
		{
			this.skip = skip;
			return this;
		}

		/**
		 * Sets whether to skip test source formatting.
		 *
		 * @param skipTests skip tests flag
		 * @return this builder
		 */
		public Builder skipTests(boolean skipTests)
		{
			this.skipTests = skipTests;
			return this;
		}

		/**
		 * Sets whether to fail build on violations.
		 *
		 * @param failOnViolation fail on violation flag
		 * @return this builder
		 */
		public Builder failOnViolation(boolean failOnViolation)
		{
			this.failOnViolation = failOnViolation;
			return this;
		}

		/**
		 * Builds the immutable configuration.
		 *
		 * @return plugin configuration
		 */
		public PluginConfiguration build()
		{
			return new PluginConfiguration(
				project,
				sourceDirectory,
				testSourceDirectory,
				configFile,
				encoding,
				includes,
				excludes,
				skip,
				skipTests,
				failOnViolation);
		}
	}
}
