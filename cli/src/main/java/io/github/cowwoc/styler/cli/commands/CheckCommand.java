package io.github.cowwoc.styler.cli.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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
@Command(
	name = "check",
	description = "Check Java source files for formatting violations",
	mixinStandardHelpOptions = true)
public class CheckCommand implements Callable<Integer>
{
	@SuppressWarnings("PMD.FieldNamingConventions") // Standard SLF4J logger naming convention
	private static final Logger logger = LoggerFactory.getLogger(CheckCommand.class);

	@Parameters(
		paramLabel = "<files>",
		description = "Java source files or directories to check",
		arity = "1..*")
	private List<Path> inputPaths;

	@Option(
		names = {"-c", "--config"},
		description = "Configuration file path (default: auto-discover)")
	private Path configFile;

	@Option(
		names = {"--include"},
		description = "File patterns to include (default: **/*.java)",
		paramLabel = "<pattern>")
	private List<String> includePatterns;

	@Option(
		names = {"--exclude"},
		description = "File patterns to exclude",
		paramLabel = "<pattern>")
	private List<String> excludePatterns;

	@Option(
		names = {"--json"},
		description = "Output results in JSON format for machine processing")
	private boolean jsonOutput;

	@Option(
		names = {"--fail-fast"},
		description = "Stop checking on first violation found")
	private boolean failFast;

	@Option(
		names = {"--max-violations"},
		description = "Maximum number of violations to report (default: unlimited)",
		paramLabel = "<count>")
	private Integer maxViolations;

	@SuppressWarnings("PMD.ImmutableField") // Picocli sets this via reflection
	@Option(
		names = {"--severity"},
		description = "Minimum severity level to report: ${COMPLETION-CANDIDATES} (default: INFO)",
		paramLabel = "<level>")
	private SeverityLevel severityLevel = SeverityLevel.INFO;

	/**
	 * Severity levels for violation reporting.
	 */
	public enum SeverityLevel
	{
		ERROR, WARN, INFO, DEBUG
	}

	@Override
	public Integer call()
	{
		logger.info("Starting check operation on {} input paths", inputPaths.size());

		try
		{
			// TODO Implement check logic
			// This will be implemented when the formatter API is integrated

			// For now, just log the configuration
			String configFileDisplay;
			if (configFile != null)
				{
				configFileDisplay = configFile.toString();
				}
			else
				{
				configFileDisplay = "auto-discover";
				}
			logger.info("Configuration file: {}", configFileDisplay);
			logger.info("Include patterns: {}", includePatterns);
			logger.info("Exclude patterns: {}", excludePatterns);
			logger.info("JSON output: {}", jsonOutput);
			logger.info("Fail fast: {}", failFast);
			String maxViolationsDisplay;
			if (maxViolations != null)
				{
				maxViolationsDisplay = maxViolations.toString();
				}
			else
				{
				maxViolationsDisplay = "unlimited";
				}
			logger.info("Max violations: {}", maxViolationsDisplay);
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