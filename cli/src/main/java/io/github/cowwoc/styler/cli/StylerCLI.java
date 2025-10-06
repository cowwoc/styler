package io.github.cowwoc.styler.cli;

import io.github.cowwoc.styler.cli.commands.CheckCommand;
import io.github.cowwoc.styler.cli.commands.ConfigCommand;
import io.github.cowwoc.styler.cli.commands.FormatCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

import java.io.PrintStream;
import java.util.concurrent.Callable;

/**
 * Main entry point for the Styler Java Code Formatter CLI application.
 * <p>
 * This class coordinates command execution, handles global options, and manages the application lifecycle. It
 * uses Picocli for command-line parsing and provides subcommands for different formatting operations.
 * <p>
 * <b>Exit Codes:</b>
 * <ul>
 *   <li>{@code 0}: Success (no violations found or formatting completed)</li>
 *   <li>{@code 1}: Violations found (in check mode) or formatting changes applied</li>
 *   <li>2: Error occurred during processing</li>
 * </ul>
 * <p>
 * Note: Annotations are retained for backward compatibility with tests, but main() uses
 * programmatic API. CommandLineParser uses fully programmatic API with no reflection.
 */
// CLI app: System.out/err required for user output
@SuppressWarnings("PMD.SystemPrintln")
@Command(
	name = "styler",
	description = "Java Code Formatter with AI-friendly output",
	version = "1.0-SNAPSHOT",
	mixinStandardHelpOptions = true,
	subcommands = {
		FormatCommand.class,
		CheckCommand.class,
		ConfigCommand.class,
		HelpCommand.class
	})
public class StylerCLI implements Callable<Integer>
{
	// Standard SLF4J logger naming convention
	@SuppressWarnings("PMD.FieldNamingConventions")
	private static final Logger logger = LoggerFactory.getLogger(StylerCLI.class);

	@Option(names = {"-v", "--verbose"}, description = "Enable verbose logging")
	private boolean verbose;

	@Option(names = {"-q", "--quiet"}, description = "Suppress all output except errors")
	private boolean quiet;

	private PrintStream out;

	/**
	 * Main entry point for the application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args)
	{
		// Configure logging level based on environment
		configureLogging();

		// Parse arguments using CommandLineParser (reflection-free)
		CommandLineParser parser = new CommandLineParser();
		try
		{
			ParsedArguments parsedArgs = parser.parse(args);

			// Handle help/version commands
			if (parsedArgs.helpRequested())
			{
				System.out.println(parser.getUsageText());
				System.exit(0);
				return;
			}
			if (parsedArgs.versionRequested())
			{
				System.out.println(parser.getVersionText());
				System.exit(0);
				return;
			}

			// Execute the parsed command
			int exitCode = executeCommand(parsedArgs);
			System.exit(exitCode);
		}
		catch (ArgumentParsingException e)
		{
			System.err.println("Error: " + e.getMessage());
			System.err.println();
			System.err.println(parser.getUsageText());
			System.exit(2);
		}
	}

	/**
	 * Executes the specified command.
	 *
	 * @param args parsed arguments
	 * @return exit code
	 */
	private static int executeCommand(ParsedArguments args)
	{
		try
		{
			return switch (args.command())
			{
				case FORMAT ->
				{
					FormatCommand command = FormatCommand.fromParseResult(args.parseResult());
					yield command.call();
				}
				case CHECK ->
				{
					CheckCommand command = CheckCommand.fromParseResult(args.parseResult());
					yield command.call();
				}
				case CONFIG ->
				{
					// Handle config command and its subcommands
					if (args.parseResult().hasSubcommand())
					{
						CommandLine.ParseResult configSubcommand = args.parseResult().subcommand();
						String subcommandName = configSubcommand.commandSpec().name();

						yield switch (subcommandName)
						{
							case "init" ->
							{
								ConfigCommand.InitCommand initCmd =
									ConfigCommand.InitCommand.fromParseResult(configSubcommand);
								yield initCmd.call();
							}
							case "validate" ->
							{
								ConfigCommand.ValidateCommand validateCmd =
									ConfigCommand.ValidateCommand.fromParseResult(configSubcommand);
								yield validateCmd.call();
							}
							case "list" ->
							{
								ConfigCommand.ListCommand listCmd =
									ConfigCommand.ListCommand.fromParseResult(configSubcommand);
								yield listCmd.call();
							}
							default ->
							{
								ConfigCommand command = ConfigCommand.fromParseResult(args.parseResult());
								yield command.call();
							}
						};
					}
					else
					{
						ConfigCommand command = ConfigCommand.fromParseResult(args.parseResult());
						yield command.call();
					}
				}
				case HELP, VERSION -> 0; // Already handled in main()
			};
		}
		catch (Exception e)
		{
			logger.error("Command execution failed", e);
			System.err.println("Error: " + e.getMessage());
			return 2;
		}
	}

	/**
	 * Sets the output stream for help and usage messages. Used primarily for testing to redirect output.
	 *
	 * @param out the print stream to use for output
	 */
	public void setOut(PrintStream out)
	{
		this.out = out;
	}

	/**
	 * Executes the main command when no subcommand is specified. Shows help information by default.
	 *
	 * @return exit code ({@code 0} for success, non-zero for failure)
	 */
	@Override
	public Integer call()
	{
		PrintStream stream;
		if (out == null)
			stream = System.out;
		else
			stream = out;

		// Show the help screen
		CommandLine.usage(this, stream);
		return 0;
	}

	/**
	 * Configures logging level based on system properties and environment.
	 */
	private static void configureLogging()
	{
		// Set default logging level if not specified
		if (System.getProperty("logback.configurationFile") == null)
		{
			// Use programmatic configuration for default setup
			ch.qos.logback.classic.Logger rootLogger =
				(ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			rootLogger.setLevel(ch.qos.logback.classic.Level.WARN);
		}
	}

	/**
	 * Returns whether verbose logging is enabled.
	 *
	 * @return {@code true} if verbose mode is active
	 */
	public boolean isVerbose()
	{
		return verbose;
	}

	/**
	 * Returns whether quiet mode is enabled.
	 *
	 * @return {@code true} if quiet mode is active
	 */
	public boolean isQuiet()
	{
		return quiet;
	}
}