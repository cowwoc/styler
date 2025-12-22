package io.github.cowwoc.styler.maven;

import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import io.github.cowwoc.styler.config.Config;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.maven.internal.MavenResultHandler;
import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.PipelineResult;

/**
 * Maven goal that validates Java source file formatting without modifying files.
 * <p>
 * This goal analyzes source files and reports any formatting violations found.
 * It does not modify any files, making it safe for use in CI/CD pipelines
 * to enforce code style standards.
 * <p>
 * <b>Default Phase</b>: {@code verify}
 * <p>
 * <b>Usage Example</b>:
 * <pre>
 * &lt;plugin&gt;
 *     &lt;groupId&gt;io.github.cowwoc.styler&lt;/groupId&gt;
 *     &lt;artifactId&gt;styler-maven-plugin&lt;/artifactId&gt;
 *     &lt;version&gt;${styler.version}&lt;/version&gt;
 *     &lt;executions&gt;
 *         &lt;execution&gt;
 *             &lt;goals&gt;
 *                 &lt;goal&gt;check&lt;/goal&gt;
 *             &lt;/goals&gt;
 *         &lt;/execution&gt;
 *     &lt;/executions&gt;
 *     &lt;configuration&gt;
 *         &lt;failOnViolation&gt;true&lt;/failOnViolation&gt;
 *     &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 * <p>
 * <b>Command Line</b>:
 * <pre>
 * mvn styler:check
 * mvn styler:check -Dstyler.failOnViolation=false
 * </pre>
 * <p>
 * <b>Thread-safety</b>: This class is not thread-safe. Each Maven execution creates
 * a new instance which is used by a single thread.
 *
 * @see StylerFormatMojo
 * @see AbstractStylerMojo
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class StylerCheckMojo extends AbstractStylerMojo
{
	/**
	 * Executes the check goal by validating source file formatting.
	 * <p>
	 * Discovers all Java source files based on configured directories and patterns,
	 * then analyzes each file for formatting violations without modifying them.
	 * <p>
	 * If {@code failOnViolation} is {@code true} (the default) and violations are found,
	 * the build fails with a {@link MojoFailureException}. Otherwise, violations are
	 * logged as warnings.
	 *
	 * @throws MojoExecutionException if an unexpected error occurs during execution
	 * @throws MojoFailureException   if violations are found and {@code failOnViolation} is {@code true}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		if (shouldSkip())
		{
			return;
		}

		Log log = getLog();

		// Load configuration
		Config config = loadConfiguration();

		// Discover files to process
		List<Path> files = discoverFiles();
		if (files.isEmpty())
		{
			log.info("No Java source files found to check");
			return;
		}

		log.info("Checking " + files.size() + " file(s) for formatting violations");

		// Build pipeline in validation-only mode
		FileProcessingPipeline pipeline = buildPipeline(config, true);

		// Process files and collect violations
		MavenResultHandler resultHandler = new MavenResultHandler(log);
		int totalViolations = 0;
		int filesWithViolations = 0;

		for (Path file : files)
		{
			PipelineResult result = pipeline.processFile(file);

			if (!result.overallSuccess())
			{
				resultHandler.handleProcessingError(file, result);
				continue;
			}

			List<FormattingViolation> violations = result.violations();
			if (!violations.isEmpty())
			{
				++filesWithViolations;
				totalViolations += violations.size();
				resultHandler.reportViolations(file, violations);
			}
		}

		// Report summary
		if (totalViolations > 0)
		{
			String message = String.format(
				"Found %d formatting violation(s) in %d file(s)",
				totalViolations, filesWithViolations);

			if (failOnViolation)
			{
				log.error(message);
				throw new MojoFailureException(message);
			}
			log.warn(message);
		}
		else
		{
			log.info("No formatting violations found");
		}
	}
}
