package io.github.cowwoc.styler.plugin.mojos;

import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import io.github.cowwoc.styler.plugin.engine.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Maven Mojo that validates Java source files conform to configured style rules.
 * Checks formatting without modifying files.
 * Fails build when violations detected and failOnViolation=true.
 * Thread-safe execution enables parallel Maven builds.
 *
 * @see <a href="https://maven.apache.org/guides/plugin/guide-java-plugin-development.html">Maven Plugin
 *  Development</a>
 */
@Mojo(
	name = "check",
	defaultPhase = LifecyclePhase.VERIFY,
	threadSafe = true,
	requiresProject = true)
public class CheckMojo extends AbstractStylerMojo
{
	/**
	 * Execute formatting validation on configured source files.
	 * Reports violations to Maven log without modifying files.
	 *
	 * @param config validated plugin configuration
	 * @throws MojoExecutionException if validation execution fails
	 * @throws MojoFailureException   if formatting violations detected when failOnViolation=true
	 */
	@Override
	protected void doExecute(PluginConfiguration config)
		throws MojoExecutionException, MojoFailureException
	{
		getLog().info("Checking source formatting in: " + config.sourceDirectory());

		// Initialize engine components
		SourceFileDiscovery fileDiscovery = new SourceFileDiscovery();
		FormattingRuleLoader ruleLoader = new FormattingRuleLoader();
		FormattingContextBuilder contextBuilder = new FormattingContextBuilder();
		SourceParser parser = new IndexOverlaySourceParser();

		// Create validation strategy (check-only, no modifications)
		ValidationStrategy strategy = new ValidationStrategy(config, parser, contextBuilder, ruleLoader);

		try
		{
			// Discover source files matching include/exclude patterns
			List<Path> sourceFiles = fileDiscovery.discoverFiles(
				config.sourceDirectory().toPath(),
				List.of("**/*.java"),
				List.of("**/generated/**", "**/target/**"));

			getLog().info("Found " + sourceFiles.size() + " source files to check");

			// Process each file and collect violations
			int totalViolations = 0;
			int filesWithViolations = 0;

			for (Path sourceFile : sourceFiles)
			{
				String sourceText = Files.readString(sourceFile);
				FileProcessingStrategy.ProcessingResult result = strategy.process(sourceFile, sourceText);

				if (result.violationCount() > 0)
				{
					++filesWithViolations;
					totalViolations += result.violationCount();

					getLog().warn("Violations in " + sourceFile + ":");
					for (String violation : result.violations())
					{
						getLog().warn("  " + violation);
					}
				}
			}

			// Report results
			if (totalViolations > 0)
			{
				getLog().error("Found " + totalViolations + " violations in " +
					filesWithViolations + " files");

				if (config.failOnViolation())
				{
					throw new MojoFailureException("Formatting violations detected");
				}
			}
			else
			{
				getLog().info("No formatting violations found");
			}
		}
		catch (IOException e)
		{
			throw new MojoExecutionException("Failed to read source files", e);
		}
	}
}
