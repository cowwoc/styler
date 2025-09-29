package io.github.cowwoc.styler.cli;

import io.github.cowwoc.styler.cli.commands.CheckCommand;
import io.github.cowwoc.styler.cli.commands.ConfigCommand;
import io.github.cowwoc.styler.cli.commands.FormatCommand;
import io.github.cowwoc.styler.cli.security.SecurityConfig;
import io.github.cowwoc.styler.cli.security.SecurityManager;
import io.github.cowwoc.styler.cli.security.exceptions.SecurityException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

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
	public ParsedArguments parse(String[] args) throws ArgumentParsingException
	{
		try
		{
			// Create a capture command to intercept parsed values
			CaptureCommand captureCommand = new CaptureCommand();
			CommandLine commandLine = new CommandLine(captureCommand);

			// Configure error handling to capture exceptions
			CommandLine.ParseResult parseResult;
			try
			{
				parseResult = commandLine.parseArgs(args);
			}
			catch (CommandLine.ParameterException e)
			{
				throw new ArgumentParsingException(
					e.getMessage(),
					getUsageText(),
					e
				);
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
					null
				);
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
					null
				);
			}

			// Determine which subcommand was invoked
			ParsedArguments.Command command = determineCommand(parseResult);

			// Extract global options
			boolean verbose = captureCommand.verbose;
			boolean quiet = captureCommand.quiet;

			// Extract command-specific arguments based on the command
			List<Path> inputFiles = List.of();
			Optional<Path> configPath = Optional.empty();
			Optional<String> rulesFilter = Optional.empty();
			boolean dryRun = false;
			Optional<Path> outputDirectory = Optional.empty();

			if (parseResult.hasSubcommand())
			{
				CommandLine.ParseResult subcommandResult = parseResult.subcommand();
				Object subcommandObject = subcommandResult.commandSpec().userObject();

				if (subcommandObject instanceof FormatCommand formatCmd)
				{
					inputFiles = extractInputFiles(formatCmd);
					configPath = extractConfigPath(formatCmd);
					dryRun = extractDryRun(formatCmd);
					outputDirectory = extractOutputDirectory(formatCmd);
				}
				else if (subcommandObject instanceof CheckCommand checkCmd)
				{
					inputFiles = extractInputFiles(checkCmd);
					configPath = extractConfigPath(checkCmd);
				}
				else if (subcommandObject instanceof ConfigCommand configCmd)
				{
					configPath = extractConfigPath(configCmd);
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
						e
					);
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
				outputDirectory.orElse(null)
			);
		}
		catch (IllegalArgumentException e)
		{
			throw new ArgumentParsingException(
				e.getMessage(),
				getUsageText(),
				e
			);
		}
	}

	/**
	 * Generates usage text for the command-line interface.
	 *
	 * @return formatted usage text
	 */
	public String getUsageText()
	{
		CaptureCommand captureCommand = new CaptureCommand();
		CommandLine commandLine = new CommandLine(captureCommand);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (PrintWriter writer = new PrintWriter(baos))
		{
			commandLine.usage(writer);
		}

		return baos.toString();
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
			throw new IllegalArgumentException("Input files are required for " + command.name().toLowerCase() + " command");
		}
	}

	/**
	 * Extracts input files using reflection from command objects.
	 */
	private List<Path> extractInputFiles(Object command)
	{
		try
		{
			var field = command.getClass().getDeclaredField("inputPaths");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<Path> paths = (List<Path>) field.get(command);
			return paths != null ? paths : List.of();
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			return List.of();
		}
	}

	/**
	 * Extracts config path using reflection from command objects.
	 */
	private Optional<Path> extractConfigPath(Object command)
	{
		try
		{
			var field = command.getClass().getDeclaredField("configFile");
			field.setAccessible(true);
			Path path = (Path) field.get(command);
			return Optional.ofNullable(path);
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			return Optional.empty();
		}
	}

	/**
	 * Extracts dry run flag using reflection from FormatCommand.
	 */
	private boolean extractDryRun(FormatCommand command)
	{
		try
		{
			var field = command.getClass().getDeclaredField("dryRun");
			field.setAccessible(true);
			Boolean dryRun = (Boolean) field.get(command);
			return dryRun != null && dryRun;
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			return false;
		}
	}

	/**
	 * Extracts output directory using reflection from FormatCommand.
	 */
	private Optional<Path> extractOutputDirectory(FormatCommand command)
	{
		try
		{
			var field = command.getClass().getDeclaredField("outputDirectory");
			field.setAccessible(true);
			Path path = (Path) field.get(command);
			return Optional.ofNullable(path);
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			return Optional.empty();
		}
	}

	/**
	 * Internal command class used to capture global options.
	 */
	@Command(
		name = "styler",
		description = "Java Code Formatter with AI-friendly output",
		version = VERSION,
		mixinStandardHelpOptions = true,
		subcommands = {
			FormatCommand.class,
			CheckCommand.class,
			ConfigCommand.class,
			HelpCommand.class
		}
	)
	private static class CaptureCommand implements Callable<Integer>
	{
		@Option(names = {"-v", "--verbose"}, description = "Enable verbose logging")
		private boolean verbose = false;

		@Option(names = {"-q", "--quiet"}, description = "Suppress all output except errors")
		private boolean quiet = false;

		@Override
		public Integer call()
		{
			return 0;
		}
	}
}