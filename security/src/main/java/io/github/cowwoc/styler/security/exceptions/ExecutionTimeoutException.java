package io.github.cowwoc.styler.security.exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when file processing exceeds the configured timeout.
 * <p>
 * This exception prevents infinite loops or excessively slow operations from
 * blocking the system indefinitely.
 */
public class ExecutionTimeoutException extends SecurityException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception indicating execution timeout.
	 *
	 * @param file      file being processed
	 * @param timeoutMs configured timeout in milliseconds
	 */
	public ExecutionTimeoutException(Path file, long timeoutMs)
	{
		super(String.format(
			"Processing file '%s' exceeded timeout of %d ms. " +
			"File may be too complex or contain pathological patterns. " +
			"Consider increasing executionTimeoutMs or excluding this file.",
			file, timeoutMs));
	}
}
