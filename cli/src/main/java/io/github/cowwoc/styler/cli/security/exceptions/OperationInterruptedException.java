package io.github.cowwoc.styler.cli.security.exceptions;

/**
 * Exception thrown when an operation is interrupted before completion.
 * <p>
 * This exception indicates that the operation was cancelled due to thread
 * interruption. The interrupted status of the thread is restored before
 * this exception is thrown.
 */
public final class OperationInterruptedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new operation interrupted exception.
	 *
	 * @param operationName the name of the operation that was interrupted
	 * @param cause the underlying {@code InterruptedException}
	 * @throws NullPointerException if operationName or cause is {@code null}
	 */
	public OperationInterruptedException(String operationName, Throwable cause)
	{
		super(buildMessage(operationName), requireNonNull(cause, "cause must not be null"));
	}

	private static String buildMessage(String operationName)
	{
		if (operationName == null)
		{
			throw new NullPointerException("operationName must not be null");
		}
		return "Operation interrupted: " + operationName;
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
