package io.github.cowwoc.styler.parser.converter;

/**
 * Exception thrown when attempting to perform Arena node conversion after the Arena has been closed.
 * <p>
 * The Arena lifecycle must be managed carefully:
 * <ul>
 * <li>Arena must remain alive during entire conversion process</li>
 * <li>Close Arena only after all AST nodes have been extracted</li>
 * <li>Do not reuse closed ArenaNodeStorage instances</li>
 * </ul>
 * </p>
 * <h2>Resolution</h2>
 * Ensure the Arena is not closed before conversion completes. Use try-with-resources
 * to guarantee proper Arena lifecycle management.
 *
 * @since 1.0
 */
public final class ArenaClosedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	/**
	 * Creates an ArenaClosedException.
	 *
	 * @param message detailed error message
	 */
	public ArenaClosedException(String message)
{
		super(message);
	}

	/**
	 * Creates an ArenaClosedException with cause.
	 *
	 * @param message detailed error message
	 * @param cause the underlying cause
	 */
	public ArenaClosedException(String message, Throwable cause)
{
		super(message, cause);
	}
}
