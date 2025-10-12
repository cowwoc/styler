package io.github.cowwoc.styler.security.exceptions;

/**
 * Base exception for all security-related violations.
 * <p>
 * This checked exception indicates that a security policy has been violated,
 * requiring explicit handling by calling code.
 */
public class SecurityException extends Exception
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
