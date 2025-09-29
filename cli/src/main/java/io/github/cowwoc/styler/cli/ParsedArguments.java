package io.github.cowwoc.styler.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Immutable record containing parsed and validated command-line arguments.
 * <p>
 * This record encapsulates all command-line options and parameters in a
 * type-safe, immutable structure that can be safely passed through the
 * application pipeline without risk of modification.
 *
 * @param inputFiles the list of input Java files or directories to process
 * @param configPath optional path to configuration file
 * @param rulesFilter optional filter for which rules to apply
 * @param verbose whether verbose logging is enabled
 * @param quiet whether quiet mode is enabled (suppresses non-error output)
 * @param dryRun whether to show changes without applying them
 * @param helpRequested whether help information was requested
 * @param versionRequested whether version information was requested
 * @param command the specific command to execute (format, check, config)
 * @param outputDirectory optional output directory for formatted files
 */
public record ParsedArguments(
	List<Path> inputFiles,
	Optional<Path> configPath,
	Optional<String> rulesFilter,
	boolean verbose,
	boolean quiet,
	boolean dryRun,
	boolean helpRequested,
	boolean versionRequested,
	Command command,
	Optional<Path> outputDirectory
)
{
	/**
	 * Static factory method for creating ParsedArguments with raw parameter types.
	 */
	public static ParsedArguments of(
		List<Path> inputFiles,
		Path configPath,
		String rulesFilter,
		boolean verbose,
		boolean quiet,
		boolean dryRun,
		boolean helpRequested,
		boolean versionRequested,
		Command command,
		Path outputDirectory
	)
	{
		// Convert raw parameters to Optional types
		Optional<Path> configPathOpt = Optional.ofNullable(configPath);
		Optional<String> rulesFilterOpt = Optional.ofNullable(rulesFilter)
			.filter(s -> !s.trim().isEmpty())
			.map(String::trim);
		Optional<Path> outputDirectoryOpt = Optional.ofNullable(outputDirectory);

		// Validate defensive copying
		List<Path> inputFilesCopy = (inputFiles == null) ? List.of() : List.copyOf(inputFiles);

		// Validate that verbose and quiet are not both enabled
		if (verbose && quiet)
		{
			throw new IllegalArgumentException("Cannot enable both verbose and quiet modes");
		}

		// Ensure command is specified
		Command commandSafe = (command == null) ? Command.HELP : command;

		return new ParsedArguments(
			inputFilesCopy,
			configPathOpt,
			rulesFilterOpt,
			verbose,
			quiet,
			dryRun,
			helpRequested,
			versionRequested,
			commandSafe,
			outputDirectoryOpt
		);
	}
	/**
	 * Enumeration of available commands.
	 */
	public enum Command
	{
		/** Format Java source files */
		FORMAT,
		/** Check formatting without making changes */
		CHECK,
		/** Show or validate configuration */
		CONFIG,
		/** Show help information */
		HELP,
		/** Show version information */
		VERSION
	}


	/**
	 * Checks if any informational request was made (help or version).
	 *
	 * @return true if help or version was requested
	 */
	public boolean isInformationalRequest()
	{
		return helpRequested || versionRequested ||
			command == Command.HELP || command == Command.VERSION;
	}

	/**
	 * Checks if processing mode requires input files.
	 *
	 * @return true if the command requires input files to be specified
	 */
	public boolean requiresInputFiles()
	{
		return command == Command.FORMAT || command == Command.CHECK;
	}

	/**
	 * Gets the effective logging level based on verbose and quiet flags.
	 *
	 * @return the logging level to use
	 */
	public LogLevel getLogLevel()
	{
		if (verbose)
		{
			return LogLevel.VERBOSE;
		}
		else if (quiet)
		{
			return LogLevel.QUIET;
		}
		else
		{
			return LogLevel.NORMAL;
		}
	}

	/**
	 * Enumeration of logging levels.
	 */
	public enum LogLevel
	{
		/** Only error messages */
		QUIET,
		/** Normal informational messages */
		NORMAL,
		/** Detailed debug information */
		VERBOSE
	}
}