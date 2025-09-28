package io.github.cowwoc.styler.formatter.api.plugin;

/**
 * Base exception for all plugin-related operations.
 * <p>
 * This exception hierarchy provides structured error handling for plugin framework
 * operations including discovery, loading, configuration, and execution. All plugin
 * exceptions include context information to assist with troubleshooting and debugging.
 * <p>
 * <b>Thread Safety:</b> This exception class is thread-safe.
 * <b>Security:</b> Exception messages are sanitized to prevent information disclosure.
 *
 * @since 1.0.0
 * @author Plugin Framework Team
 */
public class PluginException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new plugin exception with the specified message.
	 *
	 * @param message the error message describing the plugin problem, never {@code null}
	 */
	public PluginException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new plugin exception with the specified message and cause.
	 *
	 * @param message the error message describing the plugin problem, never {@code null}
	 * @param cause   the underlying cause of the plugin error, may be {@code null}
	 */
	public PluginException(String message, Throwable cause)
	{
		super(message, cause);
	}
}