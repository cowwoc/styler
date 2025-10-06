package io.github.cowwoc.styler.cli;

import io.github.cowwoc.styler.cli.security.SecurityConfig;
import io.github.cowwoc.styler.cli.security.SecurityManager;
import io.github.cowwoc.styler.cli.security.exceptions.SecurityException;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParseResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Facade for command-line argument parsing using Picocli.
 * <p>
 * This class provides a clean, testable interface for parsing command-line
 * arguments while maintaining compatibility with the existing Picocli-based
 * command structure. It returns immutable ParsedArguments objects containing
 * validated configuration.
 * <p>
 * The parser handles all argument validation, error reporting, and usage
 * generation in a stateless manner that can be easily unit tested.
 */
public class CommandLineParser
{
	private static final String VERSION = "1.0-SNAPSHOT";
	private final SecurityManager securityManager;

	/**
	 * Constructs a new command-line parser with default security configuration.
	 */
	public CommandLineParser()
	{
		this(SecurityConfig.defaults());
	}

	/**
	 * Constructs a new command-line parser with custom security configuration.
	 *
	 * @param securityConfig the security configuration to use
	 */
	public CommandLineParser(SecurityConfig securityConfig)
	{
		this.securityManager = new SecurityManager(securityConfig);
	}

	/**
	 * Parses command-line arguments and returns validated configuration.
	 *
	 * @param args the command-line arguments to parse
	 * @return immutable parsed arguments with validation applied
	 * @throws ArgumentParsingException if parsing fails or validation errors occur
	 */
	// Complex parsing logic, refactoring out of scope
	@SuppressWarnings("PMD.NcssCount")
	public ParsedArguments parse(String[] args) throws ArgumentParsingException
	{
		try
		{
			// Create command specification programmatically (no reflection)
			CommandSpec rootSpec = CommandSpecifications.createRootCommandSpec();
			rootSpec.addSubcommand("format", CommandSpecifications.createFormatCommandSpec());
			rootSpec.addSubcommand("check", CommandSpecifications.createCheckCommandSpec());
			rootSpec.addSubcommand("config", CommandSpecifications.createConfigCommandSpec());
			CommandSpec helpSpec = CommandSpec.create();
		helpSpec.name("help");
		helpSpec.usageMessage().description("Display help information");
		rootSpec.addSubcommand("help", helpSpec);

			CommandLine commandLine = new CommandLine(rootSpec);

			// Parse arguments using programmatic API
			ParseResult parseResult;
			try
			{
				parseResult = commandLine.parseArgs(args);
			}
			catch (CommandLine.ParameterException e)
			{
				throw new ArgumentParsingException(
					e.getMessage(),
					getUsageText(),
					e);
			}

			// Handle version and help requests
			if (parseResult.isVersionHelpRequested())
			{
				return ParsedArguments.of(
					List.of(),
					null,
					null,
					false,
					false,
					false,
					false,
					true,
					ParsedArguments.Command.VERSION,
				null,
					parseResult);
			}

			if (parseResult.isUsageHelpRequested())
			{
				return ParsedArguments.of(
					List.of(),
					null,
					null,
					false,
					false,
					false,
					true,
					false,
					ParsedArguments.Command.HELP,
				null,
					parseResult);
			}

			// Determine which subcommand was invoked
			ParsedArguments.Command command = determineCommand(parseResult);

			// Extract global options from ParseResult (no reflection needed)
			boolean verbose = parseResult.hasMatchedOption("--verbose") ||
				parseResult.hasMatchedOption("-v");
			boolean quiet = parseResult.hasMatchedOption("--quiet") ||
				parseResult.hasMatchedOption("-q");

			// Extract command-specific arguments using ParseResult API
			List<Path> inputFiles = List.of();
			Optional<Path> configPath = Optional.empty();
			Optional<String> rulesFilter = Optional.empty();
			boolean dryRun = false;
			Optional<Path> outputDirectory = Optional.empty();

			if (parseResult.hasSubcommand())
			{
				ParseResult subcommandResult = parseResult.subcommand();

				// Extract values from ParseResult instead of using reflection
				switch (command)
				{
					case FORMAT ->
					{
						// Extract positional parameter (files)
						if (subcommandResult.hasMatchedPositional(0))
						{
							@SuppressWarnings("unchecked")
							List<Path> paths = subcommandResult.matchedPositional(0).getValue();
					List<Path> pathsValue;
				if (paths == null)
				{
					pathsValue = List.of();
				}
				 else
				{
					pathsValue = paths;
				}
							inputFiles = pathsValue;
						}

						// Extract options using option names
						configPath = extractOptionValue(subcommandResult, "--config");
						outputDirectory = extractOptionValue(subcommandResult, "--output");
						dryRun = subcommandResult.hasMatchedOption("--dry-run");
					}
					case CHECK ->
					{
						if (subcommandResult.hasMatchedPositional(0))
						{
							@SuppressWarnings("unchecked")
							List<Path> paths = subcommandResult.matchedPositional(0).getValue();
					List<Path> pathsValue;
				if (paths == null)
				{
					pathsValue = List.of();
				}
				 else
				{
					pathsValue = paths;
				}
							inputFiles = pathsValue;
						}
						configPath = extractOptionValue(subcommandResult, "--config");
					}
					case CONFIG ->
					{
						configPath = extractOptionValue(subcommandResult, "--config");
					}
					default ->
					{
						// HELP and VERSION commands don't need additional extraction
					}
				}
			}

			// Validate argument combinations
			validateArguments(command, inputFiles, verbose, quiet);

			// Security validation: Validate all input files
			if (!inputFiles.isEmpty())
			{
				try
				{
					inputFiles = securityManager.validateFiles(inputFiles);
				}
				catch (SecurityException | IOException e)
				{
					throw new ArgumentParsingException(
						"Security validation failed: " + e.getMessage(),
						getUsageText(),
						e);
				}
			}

			return ParsedArguments.of(
				inputFiles,
				configPath.orElse(null),
				rulesFilter.orElse(null),
				verbose,
				quiet,
				dryRun,
				false,
				false,
				command,
				outputDirectory.orElse(null),
				parseResult);
		}
		catch (IllegalArgumentException e)
		{
			throw new ArgumentParsingException(
				e.getMessage(),
				getUsageText(),
				e);
		}
	}

	/**
	 * Generates usage text for the command-line interface.
	 *
	 * @return formatted usage text
	 */
	public String getUsageText()
	{
		// Create command specification programmatically (no reflection)
		CommandSpec rootSpec = CommandSpecifications.createRootCommandSpec();
		rootSpec.addSubcommand("format", CommandSpecifications.createFormatCommandSpec());
		rootSpec.addSubcommand("check", CommandSpecifications.createCheckCommandSpec());
		rootSpec.addSubcommand("config", CommandSpecifications.createConfigCommandSpec());
		CommandSpec helpSpec = CommandSpec.create();
		helpSpec.name("help");
		helpSpec.usageMessage().description("Display help information");
		rootSpec.addSubcommand("help", helpSpec);

		CommandLine commandLine = new CommandLine(rootSpec);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// CLI output uses system default charset
		@SuppressWarnings("PMD.RelianceOnDefaultCharset")
		PrintWriter writer = new PrintWriter(baos);
		try (writer)
		{
			commandLine.usage(writer);
		}

		// CLI output uses system default charset
		@SuppressWarnings("PMD.RelianceOnDefaultCharset")
		String result = baos.toString();
		return result;
	}

	/**
	 * Gets version information for the application.
	 *
	 * @return version string
	 */
	public String getVersionText()
	{
		return "styler " + VERSION;
	}

	/**
	 * Determines which command was invoked based on the parse result.
	 *
	 * @param parseResult the result of parsing command line arguments
	 * @return the command that was invoked
	 */
	private ParsedArguments.Command determineCommand(CommandLine.ParseResult parseResult)
	{
		if (!parseResult.hasSubcommand())
		{
			return ParsedArguments.Command.HELP;
		}

		CommandLine.ParseResult subcommandResult = parseResult.subcommand();
		String commandName = subcommandResult.commandSpec().name();

		return switch (commandName)
		{
			case "format" -> ParsedArguments.Command.FORMAT;
			case "check" -> ParsedArguments.Command.CHECK;
			case "config" -> ParsedArguments.Command.CONFIG;
			case "help" -> ParsedArguments.Command.HELP;
			default -> ParsedArguments.Command.HELP;
		};
	}

	/**
	 * Validates argument combinations and constraints.
	 *
	 * @param command the command being executed
	 * @param inputFiles the list of input files
	 * @param verbose whether verbose mode is enabled
	 * @param quiet whether quiet mode is enabled
	 */
	private void validateArguments(ParsedArguments.Command command, List<Path> inputFiles,
		boolean verbose, boolean quiet)
	{
		// Check for conflicting verbose/quiet flags
		if (verbose && quiet)
		{
			throw new IllegalArgumentException("Cannot specify both --verbose and --quiet options");
		}

		// Check that commands requiring input files have them
		if ((command == ParsedArguments.Command.FORMAT || command == ParsedArguments.Command.CHECK) &&
			inputFiles.isEmpty())
		{
			String commandName = command.name().toLowerCase(java.util.Locale.ROOT);
		throw new IllegalArgumentException("Input files are required for " + commandName + " command");
		}
	}

	/**
	 * Extracts an option value from ParseResult using the option name.
	 * <p>
	 * This method uses the ParseResult API to extract values without reflection,
	 * providing compile-time type safety and better performance.
	 *
	 * @param parseResult the parse result containing parsed options
	 * @param optionName  the name of the option to extract (e.g., "--config")
	 * @return optional containing the value if present, empty otherwise
	 */
	private Optional<Path> extractOptionValue(ParseResult parseResult, String optionName)
	{
		if (parseResult.hasMatchedOption(optionName))
		{
			Object value = parseResult.matchedOptionValue(optionName, null);
			if (value instanceof Path path)
			{
				return Optional.of(path);
			}
		}
		return Optional.empty();
	}
}
