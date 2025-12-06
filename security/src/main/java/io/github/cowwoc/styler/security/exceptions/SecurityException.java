package io.github.cowwoc.styler.security.exceptions;

/**
 * Base exception for all security-related violations.
 * <p>
 * This unchecked exception indicates that a security policy has been violated.
 * Security violations typically represent unrecoverable conditions (resource exhaustion,
 * execution timeouts) that should propagate without requiring explicit handling at every call site.
 */
public class SecurityException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a security exception with a detailed message.
	 *
	 * @param message description of the security violation
	 */
	public SecurityException(String message)
	{
		super(message);
	}

	/**
	 * Creates a security exception with a message and cause.
	 *
	 * @param message description of the security violation
	 * @param cause   underlying cause of the violation
	 */
	public SecurityException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
