package io.github.cowwoc.styler.security;

import io.github.cowwoc.styler.security.exceptions.ExecutionTimeoutException;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;

/**
 * Manages execution time tracking to enforce timeout limits.
 * <p>
 * This manager uses thread-local storage to track start times and validate
 * against configured timeouts, preventing indefinite hangs from malicious inputs.
 * Thread-safe through thread-local storage.
 */
public final class ExecutionTimeoutManager
{
	private final ThreadLocal<Long> startTime = new ThreadLocal<>();

	/**
	 * Starts tracking execution time for the current thread.
	 */
	public void startTracking()
	{
		startTime.set(System.currentTimeMillis());
	}

	/**
	 * Checks if execution time has exceeded the configured timeout.
	 *
	 * @param file   file being processed (for error reporting)
	 * @param config security configuration defining timeout
	 * @throws ExecutionTimeoutException if timeout exceeded
	 * @throws IllegalStateException     if tracking wasn't started
	 */
	public void checkTimeout(Path file, SecurityConfig config) throws ExecutionTimeoutException
	{
		requireThat(file, "file").isNotNull();
		requireThat(config, "config").isNotNull();

		Long start = startTime.get();
		if (start == null)
		{
			throw new IllegalStateException(
				"Execution tracking not started. Call startTracking() first.");
		}

		long elapsed = System.currentTimeMillis() - start;
		if (elapsed > config.executionTimeoutMs())
		{
			throw new ExecutionTimeoutException(file, config.executionTimeoutMs());
		}
	}

	/**
	 * Stops tracking and cleans up thread-local storage for current thread.
	 * <p>
	 * IMPORTANT: Always call this method in a finally block to prevent memory leaks
	 * in thread-pool environments.
	 */
	public void stopTracking()
	{
		startTime.remove();
	}

	/**
	 * Gets the elapsed time since tracking started.
	 *
	 * @return elapsed milliseconds
	 * @throws IllegalStateException if tracking wasn't started
	 */
	public long getElapsedTime()
	{
		Long start = startTime.get();
		if (start == null)
		{
			throw new IllegalStateException(
				"Execution tracking not started. Call startTracking() first.");
		}
		return System.currentTimeMillis() - start;
	}
}
