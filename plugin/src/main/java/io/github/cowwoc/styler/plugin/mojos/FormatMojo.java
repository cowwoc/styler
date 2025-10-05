package io.github.cowwoc.styler.plugin.mojos;

import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import io.github.cowwoc.styler.plugin.engine.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Maven Mojo that formats Java source files according to configured style rules.
 * Modifies files in-place to apply formatting.
 * Thread-safe execution enables parallel Maven builds.
 *
 * @see <a href="https://maven.apache.org/guides/plugin/guide-java-plugin-development.html">Maven Plugin
 *  Development</a>
 */
@Mojo(
	name = "format",
	threadSafe = true,
	requiresProject = true)
public class FormatMojo extends AbstractStylerMojo
{
	/**
	 * Execute formatting on configured source files.
	 * Modifies files in-place to apply configured style rules.
	 *
	 * @param config validated plugin configuration
	 * @throws MojoExecutionException if formatting execution fails
	 */
	@Override
	protected void doExecute(PluginConfiguration config) throws MojoExecutionException
	{
		getLog().info("Formatting source files in: " + config.sourceDirectory());

		// Initialize engine components
		SourceFileDiscovery fileDiscovery = new SourceFileDiscovery();
		FormattingRuleLoader ruleLoader = new FormattingRuleLoader();
		FormattingContextBuilder contextBuilder = new FormattingContextBuilder();
		SourceParser parser = new IndexOverlaySourceParser();
		TextEditApplicator editApplicator = new TextEditApplicator();

		// Create formatting strategy (applies edits and writes files)
		FormattingStrategy strategy = new FormattingStrategy(config,
			parser, contextBuilder, ruleLoader, editApplicator);

		try
		{
			// Discover source files matching include/exclude patterns
			List<Path> sourceFiles = fileDiscovery.discoverFiles(
				config.sourceDirectory().toPath(),
				List.of("**/*.java"),
				List.of("**/generated/**", "**/target/**"));

			getLog().info("Found " + sourceFiles.size() + " source files to format");

			// Process each file and apply formatting
			int filesModified = 0;
			int totalEdits = 0;

			for (Path sourceFile : sourceFiles)
			{
				String sourceText = Files.readString(sourceFile);
				FileProcessingStrategy.ProcessingResult result = strategy.process(sourceFile, sourceText);

				if (result.modified())
				{
					++filesModified;
					totalEdits += result.editCount();
					getLog().info("Formatted " + sourceFile + " (" + result.editCount() + " edits)");
				}
			}

			// Report results
			getLog().info("Formatted " + filesModified + " / " + sourceFiles.size() + " files");
			getLog().info("Applied " + totalEdits + " formatting edits");
		}
		catch (IOException e)
		{
			throw new MojoExecutionException("Failed to read/write source files", e);
		}
	}
}
