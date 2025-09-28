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

import java.util.concurrent.Callable;

/**
 * Main entry point for the Styler Java Code Formatter CLI application.
 * <p>
 * This class coordinates command execution, handles global options, and manages
 * the application lifecycle. It uses Picocli for command-line parsing and
 * provides subcommands for different formatting operations.
 * <p>
 * <b>Exit Codes:</b>
 * <ul>
 *   <li>0: Success (no violations found or formatting completed)</li>
 *   <li>1: Violations found (in check mode) or formatting changes applied</li>
 *   <li>2: Error occurred during processing</li>
 * </ul>
 */
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
	}
)
public class StylerCLI implements Callable<Integer>
{
	private static final Logger logger = LoggerFactory.getLogger(StylerCLI.class);

	@Option(names = {"-v", "--verbose"}, description = "Enable verbose logging")
	private boolean verbose = false;

	@Option(names = {"-q", "--quiet"}, description = "Suppress all output except errors")
	private boolean quiet = false;

	@Option(names = {"--version"}, versionHelp = true, description = "Show version information")
	private boolean versionRequested = false;

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
	 * Executes the main command when no subcommand is specified.
	 * Shows help information by default.
	 *
	 * @return exit code (0 for success, non-zero for failure)
	 */
	@Override
	public Integer call()
	{
		// When no subcommand is provided, show help
		CommandLine.usage(this, System.out);
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
	 * @param exception the exception that occurred
	 * @param commandLine the command line instance
	 * @param parseResult the parse result
	 * @return exit code
	 */
	private static int handleExecutionException(Exception exception, CommandLine commandLine, CommandLine.ParseResult parseResult)
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
	 * @param args the command line arguments
	 * @return exit code
	 */
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
	 * @return true if verbose mode is active
	 */
	public boolean isVerbose()
	{
		return verbose;
	}

	/**
	 * Returns whether quiet mode is enabled.
	 *
	 * @return true if quiet mode is active
	 */
	public boolean isQuiet()
	{
		return quiet;
	}
}