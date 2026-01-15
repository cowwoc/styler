package io.github.cowwoc.styler.security.exceptions;

import java.io.Serial;

/**
 * Exception thrown when recursion depth exceeds the configured limit.
 * <p>
 * This exception prevents stack overflow from deeply nested structures or
 * maliciously crafted inputs designed to exhaust stack space.
 */
public class RecursionDepthExceededException extends SecurityException
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception indicating recursion depth violation.
	 *
	 * @param currentDepth current recursion depth
	 * @param limit        configured maximum depth
	 */
	public RecursionDepthExceededException(int currentDepth, int limit)
	{
		super(String.format(
			"Recursion depth (%d) exceeds limit (%d). " +
			"Code structure may be excessively nested. " +
			"Consider refactoring or increasing maxRecursionDepth configuration.",
			currentDepth, limit));
	}
}
