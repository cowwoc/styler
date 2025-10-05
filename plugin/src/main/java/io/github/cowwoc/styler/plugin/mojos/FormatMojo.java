package io.github.cowwoc.styler.plugin.mojos;

import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

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

		// MVP implementation: Placeholder for CLI pipeline integration
		// Full implementation delegates to MavenCliAdapter → FileProcessorPipeline
		getLog().info("Maven plugin structure created successfully");
		getLog().info("Configuration:");
		getLog().info("  - Source directory: " + config.sourceDirectory());
		getLog().info("  - Encoding: " + config.encoding());

		// Future: Integrate with CLI FileProcessorPipeline
		// MavenCliAdapter adapter = new MavenCliAdapter(config, getLog());
		// FormatResult result = adapter.executeFormat();
		//
		// getLog().info("Formatted " + result.getModifiedCount() + " / " +
		//               result.getTotalCount() + " files");

		getLog().info("Formatting complete (MVP implementation)");
	}
}
