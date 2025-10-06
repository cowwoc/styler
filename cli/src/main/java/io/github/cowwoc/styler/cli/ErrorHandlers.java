package io.github.cowwoc.styler.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

/**
 * Utility class providing centralized error handling for the Styler CLI.
 * <p>
 * This class encapsulates exception handling logic for both parameter parsing errors
 * and command execution errors, providing consistent error messages and exit codes
 * across all commands.
 * <p>
 * <b>Exit Codes</b>:
 * <ul>
 *   <li>{@code 0}: Success</li>
 *   <li>{@code 1}: Violations found (check mode) or formatting changes applied</li>
 *   <li>{@code 2}: Error occurred during processing</li>
 * </ul>
 */
// CLI utility: System.out/err required for user output
@SuppressWarnings("PMD.SystemPrintln")
public final class ErrorHandlers
{
	// Standard SLF4J logger naming convention
	@SuppressWarnings("PMD.FieldNamingConventions")
	private static final Logger logger = LoggerFactory.getLogger(ErrorHandlers.class);

	/**
	 * Prevents instantiation of utility class.
	 */
	private ErrorHandlers()
	{
	}

	/**
	 * Handles execution exceptions with user-friendly error messages.
	 * <p>
	 * This handler is invoked when a command's execution method throws an exception.
	 * It logs the error, displays a user-friendly message, and optionally shows
	 * the stack trace if verbose mode is enabled.
	 *
	 * @param exception   the exception that occurred during command execution
	 * @param commandLine the command line instance that was executing
	 * @param parseResult the parse result containing parsed options and parameters
	 * @return exit code ({@code 2} for error)
	 */
	public static int handleExecutionException(Exception exception, CommandLine commandLine,
		ParseResult parseResult)
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
	 * <p>
	 * This handler is invoked when command-line argument parsing fails due to
	 * invalid options, missing required parameters, or type conversion errors.
	 * It displays the error message and usage information for the failing command.
	 *
	 * @param exception the parameter exception that occurred
	 * @param args      the command line arguments that were being parsed
	 * @return exit code ({@code 2} for error)
	 */
	public static int handleParameterException(CommandLine.ParameterException exception, String[] args)
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
}
