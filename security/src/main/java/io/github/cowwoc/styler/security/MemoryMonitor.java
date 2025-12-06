package io.github.cowwoc.styler.security;

import io.github.cowwoc.styler.security.exceptions.MemoryLimitExceededException;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Monitors JVM heap memory usage to prevent memory exhaustion.
 * <p>
 * This monitor checks current heap usage against configured limits and throws
 * exceptions when thresholds are exceeded, enabling proactive resource management.
 * Thread-safe and stateless (uses injected Runtime for testability).
 */
public final class MemoryMonitor
{
	private final Runtime runtime;

	/**
	 * Creates a monitor using the default JVM runtime.
	 */
	public MemoryMonitor()
	{
		this(Runtime.getRuntime());
	}

	/**
	 * Creates a monitor with an injected runtime (for testing).
	 *
	 * @param runtime JVM runtime to query for memory information
	 */
	MemoryMonitor(Runtime runtime)
	{
		this.runtime = requireThat(runtime, "runtime").isNotNull().getValue();
	}

	/**
	 * Checks current heap usage against the configured limit.
	 *
	 * @param config security configuration defining memory limit
	 * @throws MemoryLimitExceededException if heap usage exceeds limit
	 */
	public void checkMemoryUsage(SecurityConfig config) throws MemoryLimitExceededException
	{
		requireThat(config, "config").isNotNull();

		long usedMemory = runtime.totalMemory() - runtime.freeMemory();
		if (usedMemory > config.maxHeapBytes())
		{
			throw new MemoryLimitExceededException(usedMemory, config.maxHeapBytes());
		}
	}

	/**
	 * Returns the current heap memory usage in bytes.
	 *
	 * @return current heap usage
	 */
	public long getCurrentUsage()
	{
		return runtime.totalMemory() - runtime.freeMemory();
	}
}
