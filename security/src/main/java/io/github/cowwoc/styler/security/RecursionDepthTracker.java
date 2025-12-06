package io.github.cowwoc.styler.security;

import io.github.cowwoc.styler.security.exceptions.RecursionDepthExceededException;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tracks recursion depth to prevent stack overflow from deeply nested structures.
 * <p>
 * This tracker uses thread-local storage to maintain depth counters and validate
 * against configured limits, protecting against malicious inputs designed to exhaust
 * stack space. Thread-safe through thread-local storage.
 */
public final class RecursionDepthTracker
{
	private final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);

	/**
	 * Increments recursion depth for the current thread.
	 *
	 * @param config security configuration defining depth limit
	 * @throws RecursionDepthExceededException if depth exceeds limit
	 */
	public void enter(SecurityConfig config) throws RecursionDepthExceededException
	{
		requireThat(config, "config").isNotNull();

		int currentDepth = depth.get() + 1;
		depth.set(currentDepth);

		if (currentDepth > config.maxRecursionDepth())
		{
			throw new RecursionDepthExceededException(currentDepth, config.maxRecursionDepth());
		}
	}

	/**
	 * Decrements recursion depth for the current thread.
	 * <p>
	 * IMPORTANT: Always call this method in a finally block to maintain accurate depth tracking.
	 *
	 * @throws IllegalStateException if depth is already zero
	 */
	public void exit()
	{
		int currentDepth = depth.get();
		if (currentDepth <= 0)
		{
			throw new IllegalStateException("Recursion depth is already zero. Mismatched enter/exit calls.");
		}
		depth.set(currentDepth - 1);
	}

	/**
	 * Returns the current recursion depth for the current thread.
	 *
	 * @return current depth
	 */
	public int getCurrentDepth()
	{
		return depth.get();
	}

	/**
	 * Resets recursion depth to zero for the current thread.
	 * <p>
	 * Use this method at the start of each independent operation to ensure clean state.
	 */
	public void reset()
	{
		depth.set(0);
	}
}
