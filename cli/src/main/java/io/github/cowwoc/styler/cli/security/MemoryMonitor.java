package io.github.cowwoc.styler.cli.security;

import io.github.cowwoc.styler.cli.security.exceptions.MemoryLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors JVM heap usage and enforces memory limits to prevent
 * OutOfMemoryError during file processing.
 *
 * <p>This monitor uses periodic sampling rather than continuous monitoring
 * to minimize performance overhead. It triggers garbage collection when
 * memory usage approaches the warning threshold.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * SecurityConfig config = SecurityConfig.defaults();
 * MemoryMonitor monitor = new MemoryMonitor(config.maxMemoryBytes());
 *
 * // Check memory periodically during processing
 * for (Path file : files) {
 *     monitor.checkMemoryLimit();  // Throws if limit exceeded
 *     processFile(file);
 * }
 * }</pre>
 *
 * @see MemoryLimitExceededException
 */
public final class MemoryMonitor
{
	private static final Logger logger = LoggerFactory.getLogger(MemoryMonitor.class);

	/** Warning threshold at 80% of max memory */
	private static final double WARNING_THRESHOLD = 0.80;

	private final long maxMemoryBytes;
	private final Runtime runtime;

	/**
	 * Constructs a new memory monitor with the specified limit.
	 *
	 * @param maxMemoryBytes maximum memory usage in bytes, must be positive
	 * @throws IllegalArgumentException if maxMemoryBytes is not positive
	 */
	public MemoryMonitor(long maxMemoryBytes)
	{
		if (maxMemoryBytes <= 0)
		{
			throw new IllegalArgumentException(
				"maxMemoryBytes must be positive: " + maxMemoryBytes);
		}
		this.maxMemoryBytes = maxMemoryBytes;
		this.runtime = Runtime.getRuntime();
	}

	/**
	 * Checks current memory usage against the configured limit.
	 *
	 * <p>If memory usage exceeds the warning threshold (80%), triggers
	 * garbage collection. If usage exceeds the maximum limit, throws
	 * an exception to prevent OutOfMemoryError.
	 *
	 * @throws MemoryLimitExceededException if memory usage exceeds limit
	 */
	public void checkMemoryLimit() throws MemoryLimitExceededException
	{
		long usedMemory = getCurrentHeapUsage();

		// Check if limit exceeded
		if (usedMemory > maxMemoryBytes)
		{
			throw new MemoryLimitExceededException(usedMemory, maxMemoryBytes);
		}

		// Check warning threshold
		long warningThreshold = (long) (maxMemoryBytes * WARNING_THRESHOLD);
		if (usedMemory > warningThreshold)
		{
			logger.warn(
				"Memory usage approaching limit: {} MB / {} MB ({}%). Triggering garbage collection.",
				usedMemory / 1024 / 1024,
				maxMemoryBytes / 1024 / 1024,
				(int) (usedMemory * 100.0 / maxMemoryBytes)
			);
			// Suggest GC (JVM may ignore)
			runtime.gc();
		}
	}

	/**
	 * Returns the current heap usage in bytes.
	 *
	 * <p>Calculation: {@code totalMemory() - freeMemory()}
	 *
	 * @return current heap usage in bytes
	 */
	public long getCurrentHeapUsage()
	{
		return runtime.totalMemory() - runtime.freeMemory();
	}

	/**
	 * Returns the maximum heap size configured for the JVM.
	 *
	 * @return maximum heap size in bytes
	 */
	public long getMaxHeapSize()
	{
		return runtime.maxMemory();
	}

	/**
	 * Returns the current memory usage as a percentage of the configured limit.
	 *
	 * @return memory usage percentage (0.0 to 100.0+)
	 */
	public double getMemoryUsagePercentage()
	{
		long usedMemory = getCurrentHeapUsage();
		return (usedMemory * 100.0) / maxMemoryBytes;
	}
}