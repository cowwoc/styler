package io.github.cowwoc.styler.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.github.cowwoc.styler.config.Config;
import io.github.cowwoc.styler.maven.internal.MavenResultHandler;
import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.PipelineResult;

/**
 * Maven goal that formats Java source files by applying style fixes.
 * <p>
 * This goal analyzes source files, applies formatting fixes, and writes the
 * formatted content back to the original files. Optional backup functionality
 * can preserve original files before modification.
 * <p>
 * <b>Default Phase</b>: {@code process-sources}
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
 *                 &lt;goal&gt;format&lt;/goal&gt;
 *             &lt;/goals&gt;
 *         &lt;/execution&gt;
 *     &lt;/executions&gt;
 *     &lt;configuration&gt;
 *         &lt;dryRun&gt;false&lt;/dryRun&gt;
 *         &lt;backupOriginals&gt;false&lt;/backupOriginals&gt;
 *     &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 * <p>
 * <b>Command Line</b>:
 * <pre>
 * mvn styler:format
 * mvn styler:format -Dstyler.dryRun=true
 * mvn styler:format -Dstyler.backupOriginals=true
 * </pre>
 * <p>
 * <b>Thread-safety</b>: This class is not thread-safe. Each Maven execution creates
 * a new instance which is used by a single thread.
 *
 * @see StylerCheckMojo
 * @see AbstractStylerMojo
 */
@Mojo(name = "format", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class StylerFormatMojo extends AbstractStylerMojo
{
	/**
	 * Whether to perform a dry run without actually modifying files.
	 * When {@code true}, reports what changes would be made without applying them.
	 * Default is {@code false}.
	 */
	@Parameter(property = "styler.dryRun", defaultValue = "false")
	private boolean dryRun;

	/**
	 * Whether to create backup copies of original files before formatting.
	 * Backups are created with a {@code .bak} extension in the same directory.
	 * Default is {@code false}.
	 */
	@Parameter(property = "styler.backupOriginals", defaultValue = "false")
	private boolean backupOriginals;

	/**
	 * Executes the format goal by applying formatting fixes to source files.
	 * <p>
	 * Discovers all Java source files based on configured directories and patterns,
	 * then formats each file by applying style fixes. Files are modified in place
	 * unless {@code dryRun} is {@code true}.
	 * <p>
	 * If {@code backupOriginals} is {@code true}, creates backup copies of files
	 * before modification.
	 *
	 * @throws MojoExecutionException if an unexpected error occurs during execution
	 * @throws MojoFailureException   if formatting fails and {@code failOnViolation} is {@code true}
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
			log.info("No Java source files found to format");
			return;
		}

		if (dryRun)
		{
			log.info("Performing dry run on " + files.size() + " file(s)");
		}
		else
		{
			log.info("Formatting " + files.size() + " file(s)");
		}

		// Build pipeline in format mode
		FileProcessingPipeline pipeline = buildPipeline(config, false);

		// Process files
		MavenResultHandler resultHandler = new MavenResultHandler(log);
		int filesFormatted = 0;
		int filesUnchanged = 0;
		List<Path> failedFiles = new ArrayList<>();

		for (Path file : files)
		{
			try
			{
				String originalContent = Files.readString(file, getEncodingCharset());
				PipelineResult result = pipeline.processFile(file);

				if (!result.overallSuccess())
				{
					resultHandler.handleProcessingError(file, result);
					failedFiles.add(file);
					continue;
				}

				// Get formatted content from the pipeline result
				String formattedContent = extractFormattedContent(result, originalContent);

				if (formattedContent.equals(originalContent))
				{
					++filesUnchanged;
					log.debug("No changes needed: " + file);
					continue;
				}

				if (dryRun)
				{
					log.info("Would format: " + file);
					++filesFormatted;
				}
				else
				{
					// Create backup if requested
					if (backupOriginals)
					{
						createBackup(file, log);
					}

					// Write formatted content
					Files.writeString(file, formattedContent, getEncodingCharset());
					log.info("Formatted: " + file);
					++filesFormatted;
				}
			}
			catch (IOException e)
			{
				log.error("Failed to process file: " + file, e);
				failedFiles.add(file);
			}
		}

		// Report summary
		if (dryRun)
		{
			log.info(String.format(
				"Dry run complete: %d file(s) would be formatted, %d file(s) unchanged",
				filesFormatted, filesUnchanged));
		}
		else
		{
			log.info(String.format(
				"Formatting complete: %d file(s) formatted, %d file(s) unchanged",
				filesFormatted, filesUnchanged));
		}

		if (!failedFiles.isEmpty())
		{
			String message = String.format(
				"Failed to format %d file(s)", failedFiles.size());

			if (failOnViolation)
			{
				throw new MojoFailureException(message);
			}
			log.warn(message);
		}
	}

	/**
	 * Extracts the formatted content from a pipeline result.
	 * <p>
	 * The pipeline produces formatted content during the format stage.
	 * This method extracts that content from the result structure.
	 *
	 * @param result          the pipeline result
	 * @param originalContent the original file content (used as fallback)
	 * @return the formatted content, or original content if extraction fails
	 */
	private String extractFormattedContent(PipelineResult result, String originalContent)
	{
		// The pipeline stores formatted content in the stage results
		// For now, return original content until proper result extraction is implemented
		// The actual implementation would extract from result.stageResults()
		return result.formattedSource().orElse(originalContent);
	}

	/**
	 * Creates a backup copy of a file before modification.
	 *
	 * @param file the file to back up
	 * @param log  the logger for debug output
	 * @throws IOException if backup creation fails
	 */
	private void createBackup(Path file, Log log) throws IOException
	{
		Path backup = file.resolveSibling(file.getFileName() + ".bak");
		Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
		log.debug("Created backup: " + backup);
	}
}
