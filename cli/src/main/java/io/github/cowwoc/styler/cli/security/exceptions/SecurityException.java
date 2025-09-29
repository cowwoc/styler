package io.github.cowwoc.styler.cli.security.exceptions;

/**
 * Base exception for all security-related violations in the CLI.
 *
 * <p>This exception and its subclasses follow the single-user tool security model,
 * providing detailed error messages that prioritize debugging assistance over
 * information hiding.
 *
 * @see FileSizeExceededException
 * @see FileTypeNotAllowedException
 * @see PathTraversalException
 */
public class SecurityException extends RuntimeException
{
	/**
	 * Constructs a new security exception with the specified detail message.
	 *
	 * @param message the detail message with actionable debugging information
	 */
	public SecurityException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new security exception with the specified detail message and cause.
	 *
	 * @param message the detail message with actionable debugging information
	 * @param cause the cause of this exception
	 */
	public SecurityException(String message, Throwable cause)
	{
		super(message, cause);
	}
}