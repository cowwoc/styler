package io.github.cowwoc.styler.cli.commands;

import io.github.cowwoc.styler.cli.pipeline.BatchResult;
import io.github.cowwoc.styler.cli.pipeline.FileProcessorPipeline;
import io.github.cowwoc.styler.cli.pipeline.ParallelFileProcessor;
import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.PipelineResult;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;
import io.github.cowwoc.styler.cli.pipeline.progress.ProgressObserver;
import io.github.cowwoc.styler.cli.pipeline.stages.FormatStage;
import io.github.cowwoc.styler.cli.pipeline.stages.ParseStage;
import io.github.cowwoc.styler.cli.pipeline.stages.WriteStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Format command for applying code formatting to Java source files.
 * <p>
 * This command processes input files or directories and applies configured
 * formatting rules, modifying files in-place or outputting to specified
 * locations based on user options.
 */
// CLI command: System.out/err required for user output
@SuppressWarnings("PMD.SystemPrintln")
public final class FormatCommand implements Callable<Integer>
{
	// Standard SLF4J logger naming convention
	@SuppressWarnings("PMD.FieldNamingConventions")
	private static final Logger logger = LoggerFactory.getLogger(FormatCommand.class);

	private final List<Path> inputPaths;
	private final Path configFile;
	private final Path outputDirectory;
	private final boolean dryRun;
	private final List<String> includePatterns;
	private final List<String> excludePatterns;
	private final boolean jsonOutput;
	private final boolean failOnChanges;
	private final int maxConcurrentFiles;

	/**
	 * Private constructor - use fromParseResult() factory method.
	 *
	 * @param inputPaths         input files or directories
	 * @param configFile         configuration file path
	 * @param outputDirectory    output directory
	 * @param dryRun             dry run mode
	 * @param includePatterns    file patterns to include
	 * @param excludePatterns    file patterns to exclude
	 * @param jsonOutput         JSON output mode
	 * @param failOnChanges      fail if changes detected
	 * @param maxConcurrentFiles maximum concurrent files (must be positive)
	 */
	private FormatCommand(List<Path> inputPaths, Path configFile, Path outputDirectory, boolean dryRun,
		List<String> includePatterns, List<String> excludePatterns, boolean jsonOutput, boolean failOnChanges,
		int maxConcurrentFiles)
	{
		if (inputPaths == null)
		{
			this.inputPaths = List.of();
		}
		else
		{
			this.inputPaths = List.copyOf(inputPaths);
		}
		this.configFile = configFile;
		this.outputDirectory = outputDirectory;
		this.dryRun = dryRun;
		if (includePatterns == null)
		{
			this.includePatterns = List.of();
		}
		else
		{
			this.includePatterns = List.copyOf(includePatterns);
		}
		if (excludePatterns == null)
		{
			this.excludePatterns = List.of();
		}
		else
		{
			this.excludePatterns = List.copyOf(excludePatterns);
		}
		this.jsonOutput = jsonOutput;
		this.failOnChanges = failOnChanges;
		this.maxConcurrentFiles = maxConcurrentFiles;
	}

	/**
	 * Creates FormatCommand from ParseResult (reflection-free extraction).
	 *
	 * @param parseResult picocli parse result
	 * @return configured FormatCommand instance
	 */
	public static FormatCommand fromParseResult(picocli.CommandLine.ParseResult parseResult)
	{
		@SuppressWarnings("unchecked")
		List<Path> inputPaths;
		if (parseResult.hasMatchedPositional(0))
		{
			inputPaths = parseResult.matchedPositional(0).getValue();
		}
		else
		{
			inputPaths = List.of();
		}

		Path configFile;
		if (parseResult.hasMatchedOption("--config"))
		{
			configFile = parseResult.matchedOptionValue("--config", null);
		}
		else
		{
			configFile = null;
		}

		Path outputDirectory;
		if (parseResult.hasMatchedOption("--output"))
		{
			outputDirectory = parseResult.matchedOptionValue("--output", null);
		}
		else
		{
			outputDirectory = null;
		}

		boolean dryRun = parseResult.hasMatchedOption("--dry-run");

		@SuppressWarnings("unchecked")
		List<String> includePatterns;
		if (parseResult.hasMatchedOption("--include"))
		{
			includePatterns = parseResult.matchedOptionValue("--include", List.of());
		}
		else
		{
			includePatterns = List.of();
		}

		@SuppressWarnings("unchecked")
		List<String> excludePatterns;
		if (parseResult.hasMatchedOption("--exclude"))
		{
			excludePatterns = parseResult.matchedOptionValue("--exclude", List.of());
		}
		else
		{
			excludePatterns = List.of();
		}

		boolean jsonOutput = parseResult.hasMatchedOption("--json");
		boolean failOnChanges = parseResult.hasMatchedOption("--fail-on-changes");

		int maxConcurrentFiles;
		if (parseResult.hasMatchedOption("--max-concurrent-files"))
		{
			maxConcurrentFiles = parseResult.matchedOptionValue("--max-concurrent-files", 1);
		}
		else
		{
			maxConcurrentFiles = 1;
		}

		return new FormatCommand(inputPaths, configFile, outputDirectory, dryRun,
			includePatterns, excludePatterns, jsonOutput, failOnChanges, maxConcurrentFiles);
	}

	@Override
	public Integer call()
	{
		logger.info("Starting format operation on {} input paths", inputPaths.size());

		try
		{
			// Log configuration
			logConfiguration();

			if (dryRun)
			{
				logger.info("Dry run mode - no files will be modified");
				System.out.println("Dry run mode: would format " + inputPaths.size() + " paths");
				return 0;
			}

			// Process files with appropriate strategy
			BatchResult result;
			if (maxConcurrentFiles > 1)
			{
				result = processFilesInParallel();
			}
			else
			{
				result = processFilesSequentially();
			}

			// Report results
			reportResults(result);

			// Return appropriate exit code
			if (result.isSuccess())
			{
				return 0; // Success
			}
			else if (failOnChanges && result.successCount() > 0)
			{
				return 1; // Files were changed
			}
			else
			{
				return 2; // Errors occurred
			}
		}
		catch (Exception e)
		{
			logger.error("Format operation failed", e);
			System.err.println("Format failed: " + e.getMessage());
			return 2; // Error
		}
	}

	/**
	 * Logs the command configuration.
	 */
	private void logConfiguration()
	{
		Object temp;
		if (configFile != null)
		{
			temp = configFile;
		}
		else
		{
			temp = "auto-discover";
		}
		logger.info("Configuration file: {}", temp);
		Object temp2;
		if (outputDirectory != null)
		{
			temp2 = outputDirectory;
		}
		else
		{
			temp2 = "in-place";
		}
		logger.info("Output directory: {}", temp2);
		logger.info("Include patterns: {}", includePatterns);
		logger.info("Exclude patterns: {}", excludePatterns);
		logger.info("JSON output: {}", jsonOutput);
		logger.info("Max concurrent files: {}", maxConcurrentFiles);
	}

	/**
	 * Processes files in parallel using virtual threads.
	 *
	 * @return the batch result with success/failure statistics
	 * @throws InterruptedException if the processing is interrupted
	 */
	private BatchResult processFilesInParallel() throws InterruptedException
	{
		logger.info("Processing files in parallel with max concurrency: {}", maxConcurrentFiles);

		try (ParallelFileProcessor<Path> processor = ParallelFileProcessor.<Path>builder().
			maxConcurrentFiles(maxConcurrentFiles).
			pipelineFactory(this::createPipeline).
			build())
		{
			return processor.processFiles(inputPaths);
		}
	}

	/**
	 * Processes files sequentially (single-threaded).
	 *
	 * @return the batch result with success/failure statistics
	 */
	private BatchResult processFilesSequentially()
	{
		logger.info("Processing files sequentially");

		int successCount = 0;
		List<PipelineException> errors = new ArrayList<>();

		try (FileProcessorPipeline<Path> pipeline = createPipeline())
		{
			for (Path file : inputPaths)
			{
				ProcessingContext context = ProcessingContext.builder(file).build();
				PipelineResult<Path> result = pipeline.process(file, context);

				if (result.isSuccess())
				{
					++successCount;
				}
				else
				{
					errors.add(result.exception().orElseThrow());
				}
			}
		}
		catch (Exception e)
		{
			// Unexpected error during pipeline closure or processing
			logger.error("Unexpected error during sequential processing", e);
		}

		return new BatchResult(successCount, errors.size(), errors);
	}

	/**
	 * Creates a file processing pipeline with parse, format, and write stages.
	 *
	 * @return a configured pipeline
	 */
	private FileProcessorPipeline<Path> createPipeline()
	{
		return FileProcessorPipeline.<Path>builder().
			addStage(new ParseStage()).
			addStage(new FormatStage()).
			addStage(new WriteStage()).
			progressObserver(ProgressObserver.noOp()).
			build();
	}

	/**
	 * Reports the processing results to the console.
	 *
	 * @param result the batch processing result
	 */
	private void reportResults(BatchResult result)
	{
		if (jsonOutput)
		{
			// JSON output format
			System.out.println("{");
			System.out.println("  \"success\": " + result.isSuccess() + ",");
			System.out.println("  \"filesProcessed\": " + result.totalFiles() + ",");
			System.out.println("  \"filesSucceeded\": " + result.successCount() + ",");
			System.out.println("  \"filesFailed\": " + result.errorCount());
			System.out.println("}");
		}
		else
		{
			// Human-readable output
			System.out.println("Format operation completed:");
			System.out.println("  Total files: " + result.totalFiles());
			System.out.println("  Succeeded: " + result.successCount());
			System.out.println("  Failed: " + result.errorCount());

			if (!result.errors().isEmpty())
			{
				System.err.println("\nErrors:");
				for (PipelineException error : result.errors())
				{
					System.err.println("  " + error.getFilePath() + ": " + error.getMessage());
				}
			}
		}
	}

	/**
	 * Returns the list of input paths to be formatted.
	 *
	 * @return the input paths
	 */
	public List<Path> getInputPaths()
	{
		return inputPaths;
	}

	/**
	 * Returns the configuration file path.
	 *
	 * @return the config file path, or {@code null} for auto-discovery
	 */
	public Path getConfigFile()
	{
		return configFile;
	}

	/**
	 * Returns whether this is a dry run operation.
	 *
	 * @return {@code true} if dry run mode is enabled
	 */
	public boolean isDryRun()
	{
		return dryRun;
	}

	/**
	 * Returns whether JSON output format is requested.
	 *
	 * @return {@code true} if JSON output is enabled
	 */
	public boolean isJsonOutput()
	{
		return jsonOutput;
	}

	/**
	 * Returns the maximum number of files to process concurrently.
	 *
	 * @return the max concurrent files (1 for sequential processing)
	 */
	public int getMaxConcurrentFiles()
	{
		return maxConcurrentFiles;
	}
}