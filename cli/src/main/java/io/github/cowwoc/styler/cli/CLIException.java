package io.github.cowwoc.styler.cli;

import java.io.Serial;

/**
 * Base exception for all CLI-related errors.
 * <p>
 * This exception serves as the parent class for all command-line interface
 * exceptions in the Styler CLI.
 */
public class CLIException extends Exception
{
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * Creates a new CLI exception with the specified message.
	 *
	 * @param message the detail message
	 */
	public CLIException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new CLI exception with the specified message and cause.
	 *
	 * @param message the detail message
	 * @param cause   the cause of this exception
	 */
	public CLIException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
