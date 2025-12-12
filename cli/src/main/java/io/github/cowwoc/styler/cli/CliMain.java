package io.github.cowwoc.styler.cli;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.cowwoc.styler.config.Config;
import io.github.cowwoc.styler.config.ConfigurationLoader;
import io.github.cowwoc.styler.config.exception.ConfigurationException;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.WrapStyle;
import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.PipelineResult;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Entry point and orchestration facade for the Styler CLI application.
 * <p>
 * CliMain coordinates the complete workflow for formatting Java source files:
 * <ol>
 *   <li>Parse command-line arguments into CLIOptions</li>
 *   <li>Load configuration from TOML file</li>
 *   <li>Build the file processing pipeline</li>
 *   <li>Process input files sequentially</li>
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

			// Step 4: Process files
			List<PipelineResult> results = new ArrayList<>();
			for (Path inputPath : options.inputPaths())
			{
				PipelineResult result = processFile(pipeline, inputPath);
				results.add(result);
			}

			// Step 5: Determine and return exit code
			return determineExitCode(results, options);
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
			if (startDir == null)
			{
				// Config file in current directory - use CWD
				startDir = Path.of(System.getProperty("user.dir"));
			}
		}
		else if (options.inputPaths().isEmpty())
		{
			// Use current working directory
			startDir = Path.of(System.getProperty("user.dir"));
		}
		else
		{
			// Use parent directory of first input file
			Path firstPath = options.inputPaths().get(0);
			startDir = firstPath.getParent();
			if (startDir == null)
			{
				// File in current directory - use CWD
				startDir = Path.of(System.getProperty("user.dir"));
			}
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
	 *   <li>Formatting configuration (from config)</li>
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
		FormattingConfiguration formattingConfig = createFormattingConfiguration(config);

		return FileProcessingPipeline.builder().
			securityConfig(SecurityConfig.DEFAULT).
			formattingRules(rules).
			formattingConfig(formattingConfig).
			validationOnly(options.checkMode()).
			build();
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
			new io.github.cowwoc.styler.formatter.linelength.LineLengthFormattingRule(),
			new io.github.cowwoc.styler.formatter.importorg.ImportOrganizerFormattingRule());
	}

	/**
	 * Creates formatting configuration from loaded config.
	 * <p>
	 * Translates configuration values (e.g., maxLineLength) into formatter-specific configuration.
	 *
	 * @param config the loaded configuration
	 * @return formatting configuration for the pipeline
	 * @throws NullPointerException if config is null
	 */
	private FormattingConfiguration createFormattingConfiguration(Config config)
	{
		requireThat(config, "config").isNotNull();

		// Create LineLengthConfiguration based on config maxLineLength
		// Use defaults for other settings until they're configurable
		return new LineLengthConfiguration(
			"line-length",
			config.maxLineLength(),
			4,  // tabWidth
			4,  // indentContinuationLines
			WrapStyle.AFTER,  // methodChainWrap
			WrapStyle.AFTER,  // methodArgumentsWrap
			WrapStyle.AFTER,  // binaryExpressionWrap
			WrapStyle.AFTER,  // methodParametersWrap
			WrapStyle.AFTER,  // ternaryExpressionWrap
			WrapStyle.AFTER,  // arrayInitializerWrap
			WrapStyle.AFTER,  // annotationArgumentsWrap
			WrapStyle.AFTER,  // genericTypeArgsWrap
			true);  // wrapLongStrings
	}

	/**
	 * Processes a single file through the pipeline.
	 * <p>
	 * This method handles file-level errors and returns a PipelineResult.
	 * Callers must use try-with-resources to ensure proper cleanup.
	 *
	 * @param pipeline the configured file processing pipeline
	 * @param filePath the path to process
	 * @return the pipeline result (may indicate success or failure)
	 * @throws SecurityException if file is rejected by security validation
	 * @throws IOException if file cannot be read
	 * @throws NullPointerException if any argument is null
	 */
	private PipelineResult processFile(FileProcessingPipeline pipeline, Path filePath)
	{
		requireThat(pipeline, "pipeline").isNotNull();
		requireThat(filePath, "filePath").isNotNull();

		// Check if path is a directory - reject with clear error
		if (Files.isDirectory(filePath))
		{
			throw new IllegalArgumentException(
				"Directory processing not yet supported: " + filePath +
				". Use explicit file paths. File discovery will be implemented in a future release.");
		}

		// Check if file is readable
		if (!Files.isReadable(filePath))
		{
			throw new IllegalArgumentException(
				"File is not readable: " + filePath);
		}

		return pipeline.processFile(filePath);
	}

	/**
	 * Determines the exit code based on processing results and mode.
	 * <p>
	 * Exit code determination follows these rules:
	 * <ul>
	 *   <li>If any file failed to process: return error code (4, 5, etc.)</li>
	 *   <li>In check mode with violations: return VIOLATIONS_FOUND (1)</li>
	 *   <li>In fix mode or check mode without violations: return SUCCESS (0)</li>
	 * </ul>
	 *
	 * @param results list of processing results
	 * @param options CLI options specifying mode
	 * @return appropriate exit code
	 */
	private int determineExitCode(List<PipelineResult> results, CLIOptions options)
	{
		requireThat(results, "results").isNotNull();
		requireThat(options, "options").isNotNull();

		// Check for processing failures
		for (PipelineResult result : results)
		{
			if (!result.overallSuccess())
			{
				// Processing failed for at least one file
				// Error details are in result.stageResults()
				// For now, return IO_ERROR as a generic failure code
				return ExitCode.IO_ERROR.code();
			}
		}

		// Check for violations in check mode
		if (options.checkMode())
		{
			for (PipelineResult result : results)
			{
				if (!result.violations().isEmpty())
				{
					return ExitCode.VIOLATIONS_FOUND.code();
				}
			}
		}

		// Success: no errors or all in fix mode
		return ExitCode.SUCCESS.code();
	}
}
