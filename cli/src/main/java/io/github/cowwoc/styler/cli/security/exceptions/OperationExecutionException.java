package io.github.cowwoc.styler.cli.security.exceptions;


import java.io.Serial;
/**
 * Exception thrown when an operation fails during execution.
 * <p>
 * This exception wraps the underlying cause from a failed operation,
 * providing context about which operation failed. The original exception
 * is preserved as the cause.
 */
public final class OperationExecutionException extends RuntimeException
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new operation execution exception.
	 *
	 * @param operationName the name of the operation that failed
	 * @param cause the underlying exception that caused the failure
	 * @throws NullPointerException if operationName or cause is {@code null}
	 */
	public OperationExecutionException(String operationName, Throwable cause)
	{
		super(buildMessage(operationName), requireNonNull(cause, "cause must not be null"));
	}

	private static String buildMessage(String operationName)
	{
		if (operationName == null)
		{
			throw new NullPointerException("operationName must not be null");
		}
		return "Operation failed: " + operationName;
	}

	private static Throwable requireNonNull(Throwable cause, String message)
	{
		if (cause == null)
		{
			throw new NullPointerException(message);
		}
		return cause;
	}
}
