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
 */
@SuppressWarnings("PMD.SystemPrintln") // CLI app: System.out/err required for user output
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
	@SuppressWarnings("PMD.FieldNamingConventions") // Standard SLF4J logger naming convention
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

		// Create command line with custom configuration
		CommandLine commandLine = new CommandLine(new StylerCLI());
		commandLine.setExecutionExceptionHandler(StylerCLI::handleExecutionException);
		commandLine.setParameterExceptionHandler(StylerCLI::handleParameterException);

		// Execute command and exit with appropriate code
		int exitCode = commandLine.execute(args);
		System.exit(exitCode);
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
		@SuppressWarnings("PMD.CloseResource")
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
	 * Handles execution exceptions with user-friendly error messages.
	 *
	 * @param exception   the exception that occurred
	 * @param commandLine the command line instance
	 * @param parseResult the parse result
	 * @return exit code
	 */
	@SuppressWarnings("PMD.UnusedFormalParameter") // Required by Picocli handler signature
	private static int handleExecutionException(Exception exception, CommandLine commandLine,
		CommandLine.ParseResult parseResult)
	{
		logger.error("Command execution failed", exception);

		// Print user-friendly error message
		System.err.println("Error: " + exception.getMessage());

		// Show stack trace only in verbose mode
		if (parseResult.hasMatchedOption("--verbose"))
		{
			exception.printStackTrace(System.err);
		}
		else
		{
			System.err.println("Use --verbose flag for detailed error information");
		}

		return 2; // Error exit code
	}

	/**
	 * Handles parameter parsing exceptions with helpful suggestions.
	 *
	 * @param exception the parameter exception
	 * @param args      the command line arguments
	 * @return exit code
	 */
	@SuppressWarnings("PMD.UnusedFormalParameter") // Required by Picocli handler signature
	private static int handleParameterException(CommandLine.ParameterException exception, String[] args)
	{
		CommandLine commandLine = exception.getCommandLine();

		// Print the error message
		System.err.println("Error: " + exception.getMessage());

		// Show usage for the failing command
		System.err.println();
		System.err.println("Usage:");
		commandLine.usage(System.err);

		return 2; // Error exit code
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