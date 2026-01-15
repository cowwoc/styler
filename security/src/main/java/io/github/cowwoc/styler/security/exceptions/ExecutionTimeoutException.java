package io.github.cowwoc.styler.security.exceptions;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.Serial;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Exception thrown when file processing exceeds the configured timeout.
 * <p>
 * This exception prevents infinite loops or excessively slow operations from
 * blocking the system indefinitely.
 */
public class ExecutionTimeoutException extends SecurityException
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception indicating execution timeout.
	 *
	 * @param file    file being processed
	 * @param timeout configured timeout duration
	 * @throws NullPointerException if any argument is null
	 */
	public ExecutionTimeoutException(Path file, Duration timeout)
	{
		super(String.format(
			"Processing file '%s' exceeded timeout of %d ms. " +
			"File may be too complex or contain pathological patterns. " +
			"Consider increasing executionTimeout or excluding this file.",
			requireThat(file, "file").isNotNull().getValue(),
			requireThat(timeout, "timeout").isNotNull().getValue().toMillis()));
	}
}
