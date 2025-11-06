package io.github.cowwoc.styler.cli;

import java.io.Serial;

/**
 * Exception thrown when command-line arguments are invalid or malformed.
 */
public final class UsageException extends CLIException
{
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * Creates a new usage exception with the specified message.
	 *
	 * @param message the detail message describing the usage error
	 */
	public UsageException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new usage exception with the specified message and cause.
	 *
	 * @param message the detail message describing the usage error
	 * @param cause   the underlying cause of the error
	 */
	public UsageException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
