package io.github.cowwoc.styler.plugin.mojos;

import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.util.List;

/**
 * Base class for all Styler Maven plugin goals.
 * Implements template method pattern for consistent configuration and execution flow.
 * Thread-safe stateless design enables parallel Maven builds.
 */
public abstract class AbstractStylerMojo extends AbstractMojo
{
	/**
	 * Skip plugin execution entirely.
	 */
	@Parameter(property = "styler.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Skip formatting of test source files.
	 */
	@Parameter(property = "styler.skipTests", defaultValue = "false")
	private boolean skipTests;

	/**
	 * Path to styler configuration file (optional).
	 * If not specified, plugin will search for .styler.yml in project and parent directories.
	 */
	@Parameter(property = "styler.configFile")
	private File configFile;

	/**
	 * Main source directory to format.
	 */
	@Parameter(property = "styler.sourceDirectory", defaultValue = "${project.build.sourceDirectory}",
		required = true)
	private File sourceDirectory;

	/**
	 * Test source directory to format.
	 */
	@Parameter(property = "styler.testSourceDirectory",
		defaultValue = "${project.build.testSourceDirectory}")
	private File testSourceDirectory;

	/**
	 * Source file encoding.
	 */
	@Parameter(property = "styler.encoding", defaultValue = "${project.build.sourceEncoding}",
		required = true)
	private String encoding;

	/**
	 * File inclusion patterns (Ant-style globs).
	 */
	@Parameter(property = "styler.includes")
	private List<String> includes;

	/**
	 * File exclusion patterns (Ant-style globs).
	 */
	@Parameter(property = "styler.excludes")
	private List<String> excludes;

	/**
	 * Fail build on formatting violations.
	 */
	@Parameter(property = "styler.failOnViolation", defaultValue = "true")
	private boolean failOnViolation;

	/**
	 * Maven project instance.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	/**
	 * Template method: Execute plugin goal with configuration validation.
	 * Subclasses implement {@link #doExecute(PluginConfiguration)} for goal-specific logic.
	 *
	 * @throws MojoExecutionException if plugin execution fails
	 * @throws MojoFailureException   if formatting violations detected (when failOnViolation=true)
	 */
	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException
	{
		if (skip)
		{
			getLog().info("Skipping styler execution (skip=true)");
			return;
		}

		PluginConfiguration config = buildConfiguration();
		validateConfiguration(config);
		doExecute(config);
	}

	/**
	 * Execute goal-specific logic with validated configuration.
	 * Subclasses must implement this method.
	 *
	 * @param config validated plugin configuration
	 * @throws MojoExecutionException if execution fails
	 * @throws MojoFailureException   if validation fails
	 */
	protected abstract void doExecute(PluginConfiguration config)
		throws MojoExecutionException, MojoFailureException;

	/**
	 * Build immutable configuration from Maven parameters.
	 *
	 * @return plugin configuration
	 */
	private PluginConfiguration buildConfiguration()
	{
		String finalEncoding = encoding;
		if (finalEncoding == null)
		{
			finalEncoding = PluginConfiguration.Builder.DEFAULT_ENCODING;
		}
		List<String> finalIncludes = includes;
		if (finalIncludes == null)
		{
			finalIncludes = PluginConfiguration.Builder.DEFAULT_INCLUDES;
		}
		List<String> finalExcludes = excludes;
		if (finalExcludes == null)
		{
			finalExcludes = PluginConfiguration.Builder.DEFAULT_EXCLUDES;
		}

		return PluginConfiguration.builder().
			project(project).
			sourceDirectory(sourceDirectory).
			testSourceDirectory(testSourceDirectory).
			configFile(configFile).
			encoding(finalEncoding).
			includes(finalIncludes).
			excludes(finalExcludes).
			skip(skip).
			skipTests(skipTests).
			failOnViolation(failOnViolation).
			build();
	}

	/**
	 * Validate configuration parameters.
	 * Fail-fast validation per code-quality-auditor requirements.
	 *
	 * @param config configuration to validate
	 * @throws MojoExecutionException if configuration is invalid
	 */
	private void validateConfiguration(PluginConfiguration config) throws MojoExecutionException
	{
		if (!config.sourceDirectory().exists())
		{
			throw new MojoExecutionException(
				"Source directory does not exist: " + config.sourceDirectory() +
					"\nVerify project structure and ${project.build.sourceDirectory} property");
		}

		if (config.encoding() == null || config.encoding().isBlank())
		{
			throw new MojoExecutionException(
				"Source encoding not specified\n" +
					"Set ${project.build.sourceEncoding} or use -Dstyler.encoding=UTF-8");
		}
	}
}
