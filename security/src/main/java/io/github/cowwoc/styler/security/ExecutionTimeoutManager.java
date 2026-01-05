package io.github.cowwoc.styler.security;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import io.github.cowwoc.styler.security.exceptions.ExecutionTimeoutException;

/**
 * Manages execution time tracking to enforce timeout limits.
 * <p>
 * This manager uses thread-local storage to track start times and validate
 * against configured timeouts, preventing indefinite hangs from malicious inputs.
 * Thread-safe through thread-local storage.
 */
public final class ExecutionTimeoutManager
{
	private final ThreadLocal<Instant> startTime = new ThreadLocal<>();

	/**
	 * Starts tracking execution time for the current thread.
	 */
	public void startTracking()
	{
		startTime.set(Instant.now());
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

		Instant start = startTime.get();
		if (start == null)
		{
			throw new IllegalStateException(
				"Execution tracking not started. Call startTracking() first.");
		}

		Duration elapsed = Duration.between(start, Instant.now());
		if (elapsed.compareTo(config.executionTimeout()) > 0)
			throw new ExecutionTimeoutException(file, config.executionTimeout());
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
	 * Returns the elapsed time since tracking started.
	 *
	 * @return elapsed duration
	 * @throws IllegalStateException if tracking wasn't started
	 */
	public Duration getElapsedTime()
	{
		Instant start = startTime.get();
		if (start == null)
		{
			throw new IllegalStateException(
				"Execution tracking not started. Call startTracking() first.");
		}
		return Duration.between(start, Instant.now());
	}
}
