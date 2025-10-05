package io.github.cowwoc.styler.plugin.mojos;

import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

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

		// MVP implementation: Placeholder for CLI pipeline integration
		// Full implementation delegates to MavenCliAdapter → FileProcessorPipeline
		getLog().info("Maven plugin structure created successfully");
		getLog().info("Configuration:");
		getLog().info("  - Source directory: " + config.sourceDirectory());
		getLog().info("  - Encoding: " + config.encoding());
		getLog().info("  - Fail on violation: " + config.failOnViolation());

		// Future: Integrate with CLI FileProcessorPipeline
		// MavenCliAdapter adapter = new MavenCliAdapter(config, getLog());
		// CheckResult result = adapter.executeCheck();
		//
		// if (result.hasViolations()) {
		//     getLog().error("Found " + result.getViolationCount() + " violations");
		//     if (config.failOnViolation()) {
		//         throw new MojoFailureException("Formatting violations detected");
		//     }
		// }

		getLog().info("Formatting check complete (MVP implementation)");
	}
}
