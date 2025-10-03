package io.github.cowwoc.styler.cli.security.exceptions;


import java.io.Serial;
/**
 * Thrown when an operation exceeds the configured execution timeout.
 *
 * <p>This prevents infinite loops or pathological inputs from hanging the CLI tool.
 *
 * <h2>Example Error Message:</h2>
 * <pre>
 * File processing timeout
 *   Operation: format-file
 *   Elapsed: 32.5 seconds
 *   Timeout: 30.{@code 0} seconds
 *
 *   This file took too long to process. Possible causes:
 *   - Extremely deep nesting (>100 levels)
 *   - Pathological syntax structure
 *   - Parser infinite loop (please report bug)
 * </pre>
 */
public final class ExecutionTimeoutException extends SecurityException
{
	@Serial
	private static final long serialVersionUID = 1L;
	private final String operationName;
	private final long elapsedMillis;
	private final long timeoutMillis;

	/**
	 * Constructs a new execution timeout exception.
	 *
	 * @param operationName the name of the operation that timed out
	 * @param elapsedMillis the elapsed time in milliseconds
	 * @param timeoutMillis the configured timeout in milliseconds
	 */
	public ExecutionTimeoutException(String operationName, long elapsedMillis, long timeoutMillis)
	{
		super(formatMessage(operationName, elapsedMillis, timeoutMillis));
		this.operationName = operationName;
		this.elapsedMillis = elapsedMillis;
		this.timeoutMillis = timeoutMillis;
	}

	private static String formatMessage(String operationName, long elapsedMillis, long timeoutMillis)
	{
		return String.format(
			"Operation timeout%n" +
			"  Operation: %s%n" +
			"  Elapsed: %.1f seconds%n" +
			"  Timeout: %.1f seconds%n" +
			"%n" +
			"  This operation took too long. Possible causes:%n" +
			"  - Extremely deep nesting%n" +
			"  - Pathological syntax structure%n" +
			"  - Infinite loop (please report bug)",
			operationName,
			elapsedMillis / 1000.0,
			timeoutMillis / 1000.0);
	}

	/**
	 * Returns the name of the operation that timed out.
	 *
	 * @return the operation name
	 */
	public String operationName()
	{
		return operationName;
	}

	/**
	 * Returns the elapsed time in milliseconds.
	 *
	 * @return the elapsed time
	 */
	public long elapsedMillis()
	{
		return elapsedMillis;
	}

	/**
	 * Returns the configured timeout in milliseconds.
	 *
	 * @return the timeout limit
	 */
	public long timeoutMillis()
	{
		return timeoutMillis;
	}
}