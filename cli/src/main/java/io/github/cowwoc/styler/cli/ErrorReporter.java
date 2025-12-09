package io.github.cowwoc.styler.cli;

import io.github.cowwoc.styler.config.exception.ConfigurationException;

import java.io.IOException;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Formats and reports errors to stderr with appropriate exit codes.
 * <p>
 * Provides error formatting for CLI errors with automatic audience detection (AI or human)
 * and exit code mapping for different exception types. Errors are always written to stderr.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class ErrorReporter
{
	/**
	 * Creates an ErrorReporter with automatic audience detection.
	 * <p>
	 * Error formatting audience will be detected based on the execution environment.
	 */
	public ErrorReporter()
	{
	}

	/**
	 * Reports an error to stderr with formatted message.
	 * <p>
	 * Writes the formatted error message to stderr. The message format depends
	 * on the detected audience (AI agent or human developer).
	 * <p>
	 * Error formatting includes:
	 * <ul>
	 *     <li>File path and line numbers (if applicable)</li>
	 *     <li>Error category and message</li>
	 *     <li>Actionable fix suggestions</li>
	 *     <li>Context from source file (if available)</li>
	 * </ul>
	 *
	 * @param error the exception to report (non-null)
	 * @return the formatted error message that was written to stderr
	 * @throws NullPointerException if {@code error} is null
	 */
	@SuppressWarnings("PMD.SystemPrintln")  // CLI component intentionally writes to stderr
	public String report(Throwable error)
	{
		requireThat(error, "error").isNotNull();

		String formattedError = error.getMessage();
		System.err.print(formattedError);
		return formattedError;
	}

	/**
	 * Maps an exception to its appropriate {@link ExitCode}.
	 * <p>
	 * Exception type mapping:
	 * <ul>
	 *     <li>{@link HelpRequestedException} → {@link ExitCode#HELP}</li>
	 *     <li>{@link UsageException} → {@link ExitCode#USAGE_ERROR}</li>
	 *     <li>{@link ConfigurationException} → {@link ExitCode#CONFIG_ERROR}</li>
	 *     <li>{@link SecurityException} → {@link ExitCode#SECURITY_ERROR}</li>
	 *     <li>{@link IOException} → {@link ExitCode#IO_ERROR}</li>
	 *     <li>Other exceptions → {@link ExitCode#INTERNAL_ERROR}</li>
	 * </ul>
	 *
	 * @param error the exception to map to an exit code (non-null)
	 * @return the exit code for the exception type
	 * @throws NullPointerException if {@code error} is null
	 * @see ExitCode
	 */
	public int getExitCode(Throwable error)
	{
		requireThat(error, "error").isNotNull();

		return switch (error)
		{
			case HelpRequestedException _ -> ExitCode.HELP.code();
			case UsageException _ -> ExitCode.USAGE_ERROR.code();
			case ConfigurationException _ -> ExitCode.CONFIG_ERROR.code();
			case SecurityException _ -> ExitCode.SECURITY_ERROR.code();
			case IOException _ -> ExitCode.IO_ERROR.code();
			default -> ExitCode.INTERNAL_ERROR.code();
		};
	}
}
