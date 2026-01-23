package io.github.cowwoc.styler.cli;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.github.cowwoc.styler.config.Config;
import io.github.cowwoc.styler.config.ConfigurationLoader;
import io.github.cowwoc.styler.config.exception.ConfigurationException;
import io.github.cowwoc.styler.errorcatalog.Audience;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerConfiguration;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerFormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthFormattingRule;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.PipelineResult;
import io.github.cowwoc.styler.pipeline.parallel.BatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.BatchResult;
import io.github.cowwoc.styler.pipeline.parallel.DefaultBatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.ErrorStrategy;
import io.github.cowwoc.styler.pipeline.parallel.ParallelProcessingConfig;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Entry point and orchestration facade for the Styler CLI application.
 * <p>
 * CliMain coordinates the complete workflow for formatting Java source files:
 * <ol>
 *   <li>Parse command-line arguments into CLIOptions</li>
 *   <li>Load configuration from TOML file</li>
 *   <li>Build the file processing pipeline</li>
 *   <li>Process input files in parallel using virtual threads</li>
 *   <li>Output results in appropriate format</li>
 *   <li>Return exit code indicating success or error</li>
 * </ol>
 * <p>
 * The facade pattern isolates complexity: callers only interact with CliMain and exit codes,
 * while the implementation coordinates multiple subsystems (config, pipeline, output, errors).
 * <p>
 * <b>Thread Safety:</b> This class is stateless and thread-safe. Each invocation is independent.
 * <p>
 * Example usage:
 * <pre>
 * int exitCode = new CliMain().run(args);
 * System.exit(exitCode);
 * </pre>
 */
public final class CliMain
{
	/**
	 * Main entry point for the Styler CLI.
	 * <p>
	 * This method is invoked by the Java runtime with command-line arguments.
	 * The exit code is communicated to the operating system for scripting/CI integration.
	 *
	 * @param args command-line arguments (typically from {@code String[] args} in main method)
	 */
	public static void main(String[] args)
	{
		int exitCode = new CliMain().run(args);
		System.exit(exitCode);
	}

	/**
	 * Executes the CLI workflow and returns exit code.
	 * <p>
	 * This method is the main entry point for testing and programmatic invocation.
	 * It handles all exceptions and maps them to appropriate exit codes.
	 *
	 * @param args command-line arguments
	 * @return exit code from {@link ExitCode}:
	 *         {@link ExitCode#SUCCESS} or {@link ExitCode#HELP},
	 *         {@link ExitCode#VIOLATIONS_FOUND} (check mode),
	 *         {@link ExitCode#USAGE_ERROR},
	 *         {@link ExitCode#CONFIG_ERROR},
	 *         {@link ExitCode#SECURITY_ERROR},
	 *         {@link ExitCode#IO_ERROR},
	 *         {@link ExitCode#INTERNAL_ERROR}
	 * @see ExitCode
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public int run(String[] args)
	{
		requireThat(args, "args").isNotNull();

		try
		{
			// Step 1: Parse command-line arguments
			ArgumentParser parser = new ArgumentParser();
			CLIOptions options = parser.parse(args);

			// Step 2: Load configuration
			Config config = loadConfiguration(options);

			// Step 3: Build pipeline
			FileProcessingPipeline pipeline = buildPipeline(config, options);

			// Step 4: Validate input paths exist
			validateInputPaths(options.inputPaths());

			// Step 5: Process files in parallel
			BatchResult batchResult = processFilesInParallel(pipeline, options);

			// Step 6: Output violations
			outputViolations(batchResult, options);

			// Step 7: Report errors and determine exit code
			reportErrors(batchResult);
			return determineExitCode(batchResult, options);
		}
		catch (HelpRequestedException e)
		{
			// Help or version was requested - not an error
			return ExitCode.HELP.code();
		}
		catch (UsageException e)
		{
			// Invalid CLI arguments
			System.err.println("Usage error: " + e.getMessage());
			return ExitCode.USAGE_ERROR.code();
		}
		catch (ConfigurationException e)
		{
			// Configuration file parsing or validation error
			System.err.println("Configuration error: " + e.getMessage());
			return ExitCode.CONFIG_ERROR.code();
		}
		catch (IllegalArgumentException e)
		{
			// Invalid input - file not readable, directory not supported, etc.
			System.err.println("Error: " + e.getMessage());
			return ExitCode.USAGE_ERROR.code();
		}
		catch (InterruptedException e)
		{
			// Processing was interrupted
			Thread.currentThread().interrupt();
			System.err.println("Processing interrupted");
			return ExitCode.INTERNAL_ERROR.code();
		}
		catch (Exception e)
		{
			// Unexpected internal error - could be security, I/O, or other
			String errorMsg = e.getMessage();
			if (errorMsg != null && errorMsg.contains("Security"))
			{
				System.err.println("Security error: " + errorMsg);
				return ExitCode.SECURITY_ERROR.code();
			}
			System.err.println("Internal error: " + errorMsg);
			e.printStackTrace(System.err);
			return ExitCode.INTERNAL_ERROR.code();
		}
	}

	/**
	 * Loads configuration from file.
	 * <p>
	 * Configuration discovery and loading follows these steps:
	 * <ol>
	 *   <li>If --config is specified, load from that path</li>
	 *   <li>Otherwise, discover configuration from current directory upward</li>
	 *   <li>Apply configuration parsing and validation</li>
	 * </ol>
	 *
	 * @param options parsed CLI options
	 * @return the loaded and validated configuration
	 * @throws ConfigurationException if configuration cannot be loaded or validated
	 * @throws NullPointerException if options is null
	 */
	private Config loadConfiguration(CLIOptions options) throws ConfigurationException
	{
		requireThat(options, "options").isNotNull();

		Path startDir;
		if (options.configPath().isPresent())
		{
			// Use parent directory of specified config file
			Path configPath = options.configPath().get();
			startDir = configPath.getParent();
			// Config file in current directory - use CWD
			if (startDir == null)
				startDir = Path.of(System.getProperty("user.dir"));
		}
		else if (options.inputPaths().isEmpty())
			// Use current working directory
			startDir = Path.of(System.getProperty("user.dir"));
		else
		{
			// Use parent directory of first input file
			Path firstPath = options.inputPaths().get(0);
			startDir = firstPath.getParent();
			// File in current directory - use CWD
			if (startDir == null)
				startDir = Path.of(System.getProperty("user.dir"));
		}

		ConfigurationLoader loader = new ConfigurationLoader();
		return loader.load(startDir);
	}

	/**
	 * Builds the file processing pipeline.
	 * <p>
	 * The pipeline is configured with:
	 * <ul>
	 *   <li>Security configuration (from SecurityConfig.DEFAULT)</li>
	 *   <li>Formatting rules (extracted from config)</li>
	 *   <li>Formatting configurations (list of configs for all rules)</li>
	 *   <li>Validation mode flag (from CLI options)</li>
	 * </ul>
	 *
	 * @param config the configuration containing rule settings
	 * @param options CLI options specifying operational mode
	 * @return the built pipeline ready to process files
	 * @throws NullPointerException if any argument is null
	 */
	private FileProcessingPipeline buildPipeline(Config config, CLIOptions options)
	{
		requireThat(config, "config").isNotNull();
		requireThat(options, "options").isNotNull();

		List<FormattingRule> rules = createFormattingRules(config);
		List<FormattingConfiguration> formattingConfigs = createFormattingConfigurations(config);

		return FileProcessingPipeline.builder().
			securityConfig(SecurityConfig.DEFAULT).
			formattingRules(rules).
			formattingConfigs(formattingConfigs).
			validationOnly(options.checkMode()).
			build();
	}

	/**
	 * Validates that all input paths exist and are files (not directories).
	 * <p>
	 * Rejects non-existent paths and directories with clear error messages.
	 * This catches user errors (typos, wrong paths) before processing begins.
	 *
	 * @param inputPaths the paths to validate
	 * @throws IllegalArgumentException if any path does not exist or is a directory
	 * @throws NullPointerException     if inputPaths is null
	 */
	private void validateInputPaths(List<Path> inputPaths)
	{
		requireThat(inputPaths, "inputPaths").isNotNull();

		for (Path path : inputPaths)
		{
			if (!Files.exists(path))
			{
				throw new IllegalArgumentException(
					"Path does not exist: " + path);
			}
			if (Files.isDirectory(path))
			{
				throw new IllegalArgumentException(
					"Directory processing not yet supported: " + path +
					". Use explicit file paths. File discovery will be implemented in a future release.");
			}
		}
	}

	/**
	 * Creates formatting rules based on configuration.
	 * <p>
	 * Currently creates rules for line length checking and import organization.
	 * Additional rules will be added as formatting implementations become available.
	 *
	 * @param config the configuration
	 * @return list of formatting rules
	 * @throws NullPointerException if config is null
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
	 * <p>
	 * Creates configuration objects for each formatting rule, translating
	 * configuration values (e.g., maxLineLength) into rule-specific configurations.
	 *
	 * @param config the loaded configuration
	 * @return list of formatting configurations for all rules
	 * @throws NullPointerException if config is null
	 */
	private List<FormattingConfiguration> createFormattingConfigurations(Config config)
	{
		requireThat(config, "config").isNotNull();

		// Create configuration for each rule
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
	 * Processes files in parallel using the batch processor.
	 * <p>
	 * Configures parallel processing based on CLI options and executes the batch.
	 *
	 * @param pipeline the configured file processing pipeline
	 * @param options  CLI options including max concurrency setting
	 * @return the batch result containing all processing outcomes
	 * @throws InterruptedException if processing is interrupted
	 * @throws NullPointerException if any argument is null
	 */
	private BatchResult processFilesInParallel(FileProcessingPipeline pipeline, CLIOptions options)
		throws InterruptedException
	{
		requireThat(pipeline, "pipeline").isNotNull();
		requireThat(options, "options").isNotNull();

		int maxConcurrency = options.maxConcurrency().
			orElseGet(ParallelProcessingConfig::calculateDefaultMaxConcurrency);

		ParallelProcessingConfig parallelConfig = ParallelProcessingConfig.builder().
			maxConcurrency(maxConcurrency).
			errorStrategy(ErrorStrategy.CONTINUE).
			build();

		try (BatchProcessor batchProcessor = new DefaultBatchProcessor(pipeline, parallelConfig))
		{
			return batchProcessor.processFiles(options.inputPaths());
		}
	}

	/**
	 * Outputs violations from batch processing results.
	 * <p>
	 * Determines the effective max violations limit based on CLI options and audience detection.
	 * When AI environment is detected and no explicit limit is set, uses a default limit of 50.
	 *
	 * @param batchResult the batch result containing violations
	 * @param options     CLI options including max violations setting
	 * @throws NullPointerException if any argument is null
	 */
	private void outputViolations(BatchResult batchResult, CLIOptions options)
	{
		requireThat(batchResult, "batchResult").isNotNull();
		requireThat(options, "options").isNotNull();

		OutputHandler outputHandler = new OutputHandler();

		// Determine effective max violations
		int effectiveMaxViolations;
		if (options.maxViolations().isPresent())
			effectiveMaxViolations = options.maxViolations().getAsInt();
		else
		{
			// Auto-detect: use default limits based on audience
			Audience audience = Audience.detect();
			if (audience == Audience.AI)
				effectiveMaxViolations = OutputHandler.DEFAULT_AI_MAX_VIOLATIONS;
			else
				effectiveMaxViolations = OutputHandler.DEFAULT_HUMAN_MAX_VIOLATIONS;
		}

		// Render violations
		outputHandler.render(
			batchResult.results(),
			outputHandler.detectOutputFormat(),
			effectiveMaxViolations);
	}

	/**
	 * Reports errors from batch processing to stderr.
	 *
	 * @param batchResult the batch result containing any errors
	 * @throws NullPointerException if batchResult is null
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	private void reportErrors(BatchResult batchResult)
	{
		requireThat(batchResult, "batchResult").isNotNull();

		for (Map.Entry<Path, String> error : batchResult.errors().entrySet())
			System.err.println("Error processing " + error.getKey() + ": " + error.getValue());
	}

	/**
	 * Determines the exit code based on batch processing results and mode.
	 * <p>
	 * Exit code determination follows these rules:
	 * <ul>
	 *   <li>If any file failed to process: return IO_ERROR (4)</li>
	 *   <li>In check mode with violations: return VIOLATIONS_FOUND (1)</li>
	 *   <li>In fix mode or check mode without violations: return SUCCESS (0)</li>
	 * </ul>
	 *
	 * @param batchResult the batch processing result
	 * @param options     CLI options specifying mode
	 * @return appropriate exit code
	 * @throws NullPointerException if any argument is null
	 */
	private int determineExitCode(BatchResult batchResult, CLIOptions options)
	{
		requireThat(batchResult, "batchResult").isNotNull();
		requireThat(options, "options").isNotNull();

		// Check for processing failures
		if (batchResult.hasFailed())
			return ExitCode.IO_ERROR.code();

		// Check for violations in check mode
		if (options.checkMode())
		{
			for (PipelineResult result : batchResult.results())
				if (!result.violations().isEmpty())
					return ExitCode.VIOLATIONS_FOUND.code();
		}

		// Success: no errors or all in fix mode
		return ExitCode.SUCCESS.code();
	}
}
