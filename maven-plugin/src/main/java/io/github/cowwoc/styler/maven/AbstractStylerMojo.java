package io.github.cowwoc.styler.maven;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.github.cowwoc.styler.config.Config;
import io.github.cowwoc.styler.config.ConfigurationLoader;
import io.github.cowwoc.styler.config.exception.ConfigurationException;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerConfiguration;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerFormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthFormattingRule;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.maven.internal.MavenConfigAdapter;
import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Base class for Styler Maven goals providing shared configuration and pipeline building.
 * <p>
 * This abstract class provides common functionality for both the check and format goals:
 * <ul>
 *   <li>Configuration file discovery and loading</li>
 *   <li>Source directory discovery from Maven project model</li>
 *   <li>File filtering based on include/exclude patterns</li>
 *   <li>Pipeline construction with appropriate formatting rules</li>
 * </ul>
 * <p>
 * Subclasses implement the specific execution behavior for checking or formatting.
 * <p>
 * <b>Thread-safety</b>: This class is not thread-safe. Each Maven execution creates
 * a new instance which is used by a single thread.
 */
public abstract class AbstractStylerMojo extends AbstractMojo
{
	/**
	 * The Maven project.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	/**
	 * Path to the Styler configuration file. If not specified, the plugin searches for
	 * {@code styler.toml} starting from the project's base directory.
	 */
	@Parameter(property = "styler.configFile")
	protected File configFile;

	/**
	 * List of source directories to process. If not specified, uses the project's
	 * compile source roots.
	 */
	@Parameter(property = "styler.sourceDirectories")
	protected List<File> sourceDirectories;

	/**
	 * List of test source directories to process. If not specified, uses the project's
	 * test compile source roots.
	 */
	@Parameter(property = "styler.testSourceDirectories")
	protected List<File> testSourceDirectories;

	/**
	 * List of file patterns to include. Uses Ant-style glob patterns.
	 * Default is {@code **\/*.java}.
	 */
	@Parameter(property = "styler.includes", defaultValue = "**/*.java")
	protected List<String> includes;

	/**
	 * List of file patterns to exclude. Uses Ant-style glob patterns.
	 */
	@Parameter(property = "styler.excludes")
	protected List<String> excludes;

	/**
	 * Whether to fail the build if formatting violations are found.
	 * Default is {@code true}.
	 */
	@Parameter(property = "styler.failOnViolation", defaultValue = "true")
	protected boolean failOnViolation;

	/**
	 * Whether to skip plugin execution.
	 * Default is {@code false}.
	 */
	@Parameter(property = "styler.skip", defaultValue = "false")
	protected boolean skip;

	/**
	 * The character encoding to use when reading/writing source files.
	 * Default is UTF-8.
	 */
	@Parameter(property = "styler.encoding", defaultValue = "UTF-8")
	protected String encoding;

	/**
	 * Determines whether plugin execution should be skipped.
	 * <p>
	 * Checks the {@code skip} parameter and logs a message if skipping.
	 *
	 * @return {@code true} if execution should be skipped, {@code false} otherwise
	 */
	protected boolean shouldSkip()
	{
		if (skip)
		{
			getLog().info("Styler plugin execution skipped");
			return true;
		}
		return false;
	}

	/**
	 * Discovers all Java source files to process based on configured directories and patterns.
	 * <p>
	 * If no source directories are explicitly configured, the method uses the Maven project's
	 * compile source roots and test compile source roots.
	 *
	 * @return list of Java source file paths to process
	 * @throws MojoExecutionException if file discovery fails
	 */
	protected List<Path> discoverFiles() throws MojoExecutionException
	{
		List<Path> files = new ArrayList<>();

		// Collect source directories
		List<Path> sourceDirs = collectSourceDirectories();

		// Discover files in each directory
		for (Path sourceDir : sourceDirs)
		{
			if (Files.isDirectory(sourceDir))
			{
				try
				{
					discoverFilesInDirectory(sourceDir, files);
				}
				catch (IOException e)
				{
					throw new MojoExecutionException("Failed to discover files in " + sourceDir, e);
				}
			}
		}

		getLog().debug("Discovered " + files.size() + " files to process");
		return files;
	}

	/**
	 * Collects all source directories to process.
	 *
	 * @return list of source directory paths
	 */
	private List<Path> collectSourceDirectories()
	{
		List<Path> dirs = new ArrayList<>();

		// Add explicitly configured source directories
		if (sourceDirectories != null && !sourceDirectories.isEmpty())
		{
			for (File dir : sourceDirectories)
			{
				dirs.add(dir.toPath());
			}
		}
		else
		{
			// Use Maven project's compile source roots
			for (String root : project.getCompileSourceRoots())
			{
				dirs.add(Path.of(root));
			}
		}

		// Add explicitly configured test source directories
		if (testSourceDirectories != null && !testSourceDirectories.isEmpty())
		{
			for (File dir : testSourceDirectories)
			{
				dirs.add(dir.toPath());
			}
		}
		else
		{
			// Use Maven project's test compile source roots
			for (String root : project.getTestCompileSourceRoots())
			{
				dirs.add(Path.of(root));
			}
		}

		return dirs;
	}

	/**
	 * Discovers Java files in a directory matching the include/exclude patterns.
	 *
	 * @param directory the directory to search
	 * @param files     the list to add discovered files to
	 * @throws IOException if file discovery fails
	 */
	private void discoverFilesInDirectory(Path directory, List<Path> files) throws IOException
	{
		requireThat(directory, "directory").isNotNull();
		requireThat(files, "files").isNotNull();

		List<String> effectiveIncludes;
		if (includes != null)
		{
			effectiveIncludes = includes;
		}
		else
		{
			effectiveIncludes = List.of("**/*.java");
		}

		List<String> effectiveExcludes;
		if (excludes != null)
		{
			effectiveExcludes = excludes;
		}
		else
		{
			effectiveExcludes = List.of();
		}

		MavenConfigAdapter adapter = new MavenConfigAdapter(effectiveIncludes, effectiveExcludes);

		Files.walkFileTree(directory, new SimpleFileVisitor<>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			{
				if (adapter.matches(directory, file))
				{
					files.add(file);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
			{
				getLog().warn("Failed to access file: " + file);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Loads the Styler configuration from file or default.
	 * <p>
	 * If a configuration file is explicitly specified, loads from that path.
	 * Otherwise, discovers configuration from the project's base directory upward.
	 *
	 * @return the loaded configuration
	 * @throws MojoExecutionException if configuration loading fails
	 */
	protected Config loadConfiguration() throws MojoExecutionException
	{
		try
		{
			Path startDir;
			if (configFile != null)
			{
				Path configPath = configFile.toPath();
				if (!Files.exists(configPath))
				{
					throw new MojoExecutionException("Configuration file not found: " + configPath);
				}
				startDir = configPath.getParent();
				if (startDir == null)
				{
					startDir = project.getBasedir().toPath();
				}
			}
			else
			{
				startDir = project.getBasedir().toPath();
			}

			ConfigurationLoader loader = new ConfigurationLoader();
			return loader.load(startDir);
		}
		catch (ConfigurationException e)
		{
			throw new MojoExecutionException("Failed to load Styler configuration", e);
		}
	}

	/**
	 * Builds the file processing pipeline with the loaded configuration.
	 * <p>
	 * Creates a pipeline configured with:
	 * <ul>
	 *   <li>Security configuration from {@link SecurityConfig#DEFAULT}</li>
	 *   <li>Formatting rules extracted from configuration</li>
	 *   <li>Validation-only mode based on subclass requirement</li>
	 * </ul>
	 *
	 * @param config         the loaded Styler configuration
	 * @param validationOnly {@code true} for check mode, {@code false} for format mode
	 * @return the configured pipeline ready to process files
	 */
	protected FileProcessingPipeline buildPipeline(Config config, boolean validationOnly)
	{
		requireThat(config, "config").isNotNull();

		List<FormattingRule> rules = createFormattingRules(config);
		List<FormattingConfiguration> formattingConfigs = createFormattingConfigurations(config);

		return FileProcessingPipeline.builder().
			securityConfig(SecurityConfig.DEFAULT).
			formattingRules(rules).
			formattingConfigs(formattingConfigs).
			validationOnly(validationOnly).
			build();
	}

	/**
	 * Creates the list of formatting rules based on configuration.
	 *
	 * @param config the Styler configuration
	 * @return list of formatting rules to apply
	 */
	private List<FormattingRule> createFormattingRules(Config config)
	{
		requireThat(config, "config").isNotNull();

		return List.of(
			new LineLengthFormattingRule(),
			new ImportOrganizerFormattingRule());
	}

	/**
	 * Creates the list of formatting configurations from loaded config.
	 *
	 * @param config the loaded configuration
	 * @return list of formatting configurations for all rules
	 */
	private List<FormattingConfiguration> createFormattingConfigurations(Config config)
	{
		requireThat(config, "config").isNotNull();

		LineLengthConfiguration lineLengthConfig = LineLengthConfiguration.builder().
			maxLineLength(config.maxLineLength()).
			build();

		ImportOrganizerConfiguration importConfig = ImportOrganizerConfiguration.defaultConfig();
		BraceFormattingConfiguration braceConfig = BraceFormattingConfiguration.defaultConfig();
		WhitespaceFormattingConfiguration whitespaceConfig = WhitespaceFormattingConfiguration.defaultConfig();
		IndentationFormattingConfiguration indentationConfig = IndentationFormattingConfiguration.defaultConfig();

		return List.of(lineLengthConfig, importConfig, braceConfig, whitespaceConfig, indentationConfig);
	}

	/**
	 * Returns the configured file encoding as a Charset.
	 *
	 * @return the file encoding charset
	 */
	protected Charset getEncodingCharset()
	{
		return Charset.forName(encoding);
	}
}
