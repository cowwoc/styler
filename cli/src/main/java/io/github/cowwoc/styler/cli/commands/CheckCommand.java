package io.github.cowwoc.styler.cli.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Check command for validating Java source files against formatting rules.
 * <p>
 * This command analyzes files and reports violations without making any
 * modifications. It's designed for CI/CD integration and pre-commit hooks
 * to validate code style compliance.
 */
@SuppressWarnings("PMD.SystemPrintln") // CLI command: System.out/err required for user output
public class CheckCommand implements Callable<Integer>
{
	@SuppressWarnings("PMD.FieldNamingConventions") // Standard SLF4J logger naming convention
	private static final Logger logger = LoggerFactory.getLogger(CheckCommand.class);

	private final List<Path> inputPaths;
	private final Path configFile;
	private final List<String> includePatterns;
	private final List<String> excludePatterns;
	private final boolean jsonOutput;
	private final boolean failFast;
	private final Integer maxViolations;
	private final SeverityLevel severityLevel;

	/**
	 * Severity levels for violation reporting.
	 */
	public enum SeverityLevel
	{
		ERROR, WARN, INFO, DEBUG
	}

	/**
	 * Private constructor - use fromParseResult() factory method.
	 *
	 * @param inputPaths      input files or directories
	 * @param configFile      configuration file path
	 * @param includePatterns file patterns to include
	 * @param excludePatterns file patterns to exclude
	 * @param jsonOutput      JSON output mode
	 * @param failFast        fail-fast mode
	 * @param maxViolations   maximum violations to report
	 * @param severityLevel   minimum severity level
	 */
	private CheckCommand(List<Path> inputPaths, Path configFile, List<String> includePatterns,
		List<String> excludePatterns, boolean jsonOutput, boolean failFast, Integer maxViolations,
		SeverityLevel severityLevel)
	{
		this.inputPaths = inputPaths != null ? List.copyOf(inputPaths) : List.of();
		this.configFile = configFile;
		this.includePatterns = includePatterns != null ? List.copyOf(includePatterns) : List.of();
		this.excludePatterns = excludePatterns != null ? List.copyOf(excludePatterns) : List.of();
		this.jsonOutput = jsonOutput;
		this.failFast = failFast;
		this.maxViolations = maxViolations;
		this.severityLevel = severityLevel != null ? severityLevel : SeverityLevel.INFO;
	}

	/**
	 * Creates CheckCommand from ParseResult (reflection-free extraction).
	 *
	 * @param parseResult picocli parse result
	 * @return configured CheckCommand instance
	 */
	public static CheckCommand fromParseResult(picocli.CommandLine.ParseResult parseResult)
	{
		@SuppressWarnings("unchecked")
		List<Path> inputPaths = parseResult.hasMatchedPositional(0) ?
			parseResult.matchedPositional(0).getValue() : List.of();

		Path configFile = parseResult.hasMatchedOption("--config") ?
			parseResult.matchedOptionValue("--config", null) : null;

		@SuppressWarnings("unchecked")
		List<String> includePatterns = parseResult.hasMatchedOption("--include") ?
			parseResult.matchedOptionValue("--include", List.of()) : List.of();

		@SuppressWarnings("unchecked")
		List<String> excludePatterns = parseResult.hasMatchedOption("--exclude") ?
			parseResult.matchedOptionValue("--exclude", List.of()) : List.of();

		boolean jsonOutput = parseResult.hasMatchedOption("--json");
		boolean failFast = parseResult.hasMatchedOption("--fail-fast");

		Integer maxViolations = parseResult.hasMatchedOption("--max-violations") ?
			parseResult.matchedOptionValue("--max-violations", null) : null;

		SeverityLevel severityLevel = parseResult.hasMatchedOption("--severity") ?
			parseResult.matchedOptionValue("--severity", SeverityLevel.INFO) : SeverityLevel.INFO;

		return new CheckCommand(inputPaths, configFile, includePatterns, excludePatterns,
			jsonOutput, failFast, maxViolations, severityLevel);
	}

	@Override
	public Integer call()
	{
		logger.info("Starting check operation on {} input paths", inputPaths.size());

		try
		{
			// Log configuration for now - full implementation pending formatter API integration
			logger.info("Configuration file: {}", configFile != null ? configFile : "auto-discover");
			logger.info("Include patterns: {}", includePatterns);
			logger.info("Exclude patterns: {}", excludePatterns);
			logger.info("JSON output: {}", jsonOutput);
			logger.info("Fail fast: {}", failFast);
			logger.info("Max violations: {}", maxViolations != null ? maxViolations : "unlimited");
			logger.info("Severity level: {}", severityLevel);

			System.out.println("Check command executed successfully");
			System.out.println("Input paths: " + inputPaths);
			System.out.println("No violations found (placeholder)");

			return 0; // Success - no violations
		}
		catch (Exception e)
		{
			logger.error("Check operation failed", e);
			System.err.println("Check failed: " + e.getMessage());
			return 2; // Error
		}
	}

	/**
	 * Returns the list of input paths to be checked.
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
	 * Returns whether JSON output format is requested.
	 *
	 * @return {@code true} if JSON output is enabled
	 */
	public boolean isJsonOutput()
	{
		return jsonOutput;
	}

	/**
	 * Returns whether fail-fast mode is enabled.
	 *
	 * @return {@code true} if fail-fast is enabled
	 */
	public boolean isFailFast()
	{
		return failFast;
	}

	/**
	 * Returns the minimum severity level for reporting.
	 *
	 * @return the severity level
	 */
	public SeverityLevel getSeverityLevel()
	{
		return severityLevel;
	}
}
