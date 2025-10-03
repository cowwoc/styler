package io.github.cowwoc.styler.formatter.api.plugin;

/**
 * Exception thrown when plugin security validation fails.
 * <p>
 * This exception indicates security violations such as unsigned plugins,
 * invalid certificates, resource limit violations, or access control failures.
 * Security exceptions are never recoverable and indicate that a plugin cannot
 * be trusted for execution.
 * <p>
 * <b>Thread Safety:</b> This exception class is thread-safe.
 * <b>Security:</b> Exception messages are sanitized to prevent information leakage
 * about system internals or security mechanisms.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Plugin Framework Team
 */
public class PluginSecurityException extends SecurityException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new plugin security exception with the specified message.
	 *
	 * @param message the error message describing the security violation, never {@code null}
	 */
	public PluginSecurityException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new plugin security exception with the specified message and cause.
	 *
	 * @param message the error message describing the security violation, never {@code null}
	 * @param cause   the underlying cause of the security violation, may be {@code null}
	 */
	public PluginSecurityException(String message, Throwable cause)
	{
		super(message, cause);
	}
}