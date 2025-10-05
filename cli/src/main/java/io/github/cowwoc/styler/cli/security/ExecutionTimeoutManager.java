package io.github.cowwoc.styler.cli.security;

import io.github.cowwoc.styler.cli.security.exceptions.ExecutionTimeoutException;
import io.github.cowwoc.styler.cli.security.exceptions.OperationExecutionException;
import io.github.cowwoc.styler.cli.security.exceptions.OperationInterruptedException;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Enforces execution timeouts for operations to prevent infinite loops and pathological inputs from hanging
 * the CLI tool.
 *
 * <p>This manager uses thread interruption to cancel operations that
 * exceed the configured timeout. Operations are executed in a separate thread pool to enable clean
 * cancellation.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * SecurityConfig config = SecurityConfig.defaults();
 * ExecutionTimeoutManager manager = new ExecutionTimeoutManager(config.timeoutMillis());
 *
 * try {
 *     String result = manager.executeWithTimeout(
 *         () -> processFile(path),
 *         "format-file"
 *     );
 * } catch (ExecutionTimeoutException e) {
 *     System.err.println("Operation timed out: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see ExecutionTimeoutException
 */
public final class ExecutionTimeoutManager
{
	private final long timeoutMillis;

	/**
	 * Constructs a new execution timeout manager with the specified timeout.
	 *
	 * @param timeoutMillis timeout duration in milliseconds, must be positive
	 * @throws IllegalArgumentException if timeoutMillis is not positive
	 */
	public ExecutionTimeoutManager(long timeoutMillis)
	{
		if (timeoutMillis <= 0)
		{
			throw new IllegalArgumentException(
				"timeoutMillis must be positive: " + timeoutMillis);
		}
		this.timeoutMillis = timeoutMillis;
	}

	/**
	 * Executes the specified operation with timeout enforcement.
	 *
	 * <p>If the operation completes within the timeout, its result is returned.
	 * If the operation exceeds the timeout, it is interrupted and an exception is thrown.
	 *
	 * @param <T>           the result type
	 * @param operation     the operation to execute, must not be {@code null}
	 * @param operationName descriptive name for error messages, must not be {@code null}
	 * @return the operation result
	 * @throws NullPointerException          if operation or operationName is {@code null}
	 * @throws ExecutionTimeoutException     if operation exceeds timeout
	 * @throws OperationInterruptedException if operation is interrupted before completion
	 * @throws OperationExecutionException   if operation throws a checked exception
	 * @throws RuntimeException              if operation throws a runtime exception (rethrown as-is)
	 */
	public <T> T executeWithTimeout(Callable<T> operation, String operationName)
	{
		Objects.requireNonNull(operation, "operation must not be null");
		Objects.requireNonNull(operationName, "operationName must not be null");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<T> future = executor.submit(operation);

		long startTime = System.currentTimeMillis();

		try
		{
			return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
		}
		catch (TimeoutException e)
		{
			// Operation exceeded timeout - cancel it
			future.cancel(true);

			long elapsedTime = System.currentTimeMillis() - startTime;
			throw new ExecutionTimeoutException(operationName, elapsedTime, timeoutMillis);
		}
		catch (InterruptedException e)
		{
			// Current thread interrupted - cancel operation and restore flag
			future.cancel(true);
			Thread.currentThread().interrupt();
			throw new OperationInterruptedException(operationName, e);
		}
		catch (ExecutionException e)
		{
			// Operation threw an exception - unwrap and rethrow
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException runtimeException)
			{
				throw runtimeException;
			}
			throw new OperationExecutionException(operationName, cause);
		}
		finally
		{
			// Always shut down executor
			executor.shutdownNow();
		}
	}

	/**
	 * Returns the configured timeout in milliseconds.
	 *
	 * @return timeout duration
	 */
	public long getTimeoutMillis()
	{
		return timeoutMillis;
	}
}