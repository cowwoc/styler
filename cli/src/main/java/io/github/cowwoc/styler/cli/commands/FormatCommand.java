package io.github.cowwoc.styler.cli.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
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
	// Placeholder for fail-on-changes logic
	@SuppressWarnings("PMD.UnusedPrivateField")
	private final boolean failOnChanges;

	/**
	 * Private constructor - use fromParseResult() factory method.
	 *
	 * @param inputPaths      input files or directories
	 * @param configFile      configuration file path
	 * @param outputDirectory output directory
	 * @param dryRun          dry run mode
	 * @param includePatterns file patterns to include
	 * @param excludePatterns file patterns to exclude
	 * @param jsonOutput      JSON output mode
	 * @param failOnChanges   fail if changes detected
	 */
	private FormatCommand(List<Path> inputPaths, Path configFile, Path outputDirectory, boolean dryRun,
		List<String> includePatterns, List<String> excludePatterns, boolean jsonOutput, boolean failOnChanges)
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

		return new FormatCommand(inputPaths, configFile, outputDirectory, dryRun,
			includePatterns, excludePatterns, jsonOutput, failOnChanges);
	}

	@Override
	public Integer call()
	{
		logger.info("Starting format operation on {} input paths", inputPaths.size());

		try
		{
			// TODO Implement formatting logic
			// This will be implemented when the formatter API is integrated

			if (dryRun)
			{
				logger.info("Dry run mode - no files will be modified");
				System.out.println("Dry run mode: would format " + inputPaths.size() + " paths");
				return 0;
			}

			// For now, just log the configuration
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

			System.out.println("Format command executed successfully");
			System.out.println("Input paths: " + inputPaths);

			return 0; // Success
		}
		catch (Exception e)
		{
			logger.error("Format operation failed", e);
			System.err.println("Format failed: " + e.getMessage());
			return 2; // Error
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
}