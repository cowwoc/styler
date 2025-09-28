package io.github.cowwoc.styler.formatter.api.plugin;

import java.time.Duration;
import java.time.Instant;

/**
 * Immutable resource usage statistics for plugin performance monitoring and analysis.
 * <p>
 * Resource usage statistics provide detailed information about plugin resource
 * consumption over time, including peak usage, averages, and trend analysis.
 * These statistics are essential for capacity planning, performance optimization,
 * and identifying resource-intensive plugins.
 * <p>
 * <b>Thread Safety:</b> This class is immutable and thread-safe.
 * <b>Performance:</b> Statistics collection is optimized for minimal overhead
 * during plugin execution.
 *
 * @since 1.0.0
 * @author Plugin Framework Team
 */
public final class ResourceUsageStats
{
	private final long peakMemoryUsage;
	private final long averageMemoryUsage;
	private final long currentMemoryUsage;
	private final int peakThreadCount;
	private final double averageThreadCount;
	private final int currentThreadCount;
	private final Duration totalExecutionTime;
	private final Duration longestOperationTime;
	private final Instant lastResetTime;
	private final long totalOperations;
	private final long memoryAllocations;
	private final long memoryDeallocations;

	/**
	 * Creates a new resource usage statistics instance.
	 *
	 * @param peakMemoryUsage       the peak memory usage in bytes
	 * @param averageMemoryUsage    the average memory usage in bytes
	 * @param currentMemoryUsage    the current memory usage in bytes
	 * @param peakThreadCount       the peak thread count
	 * @param averageThreadCount    the average thread count
	 * @param currentThreadCount    the current thread count
	 * @param totalExecutionTime    the total execution time
	 * @param longestOperationTime  the longest single operation time
	 * @param lastResetTime         the time when statistics were last reset
	 * @param totalOperations       the total number of operations performed
	 * @param memoryAllocations     the total number of memory allocations
	 * @param memoryDeallocations   the total number of memory deallocations
	 */
	public ResourceUsageStats(long peakMemoryUsage, long averageMemoryUsage, long currentMemoryUsage,
	                          int peakThreadCount, double averageThreadCount, int currentThreadCount,
	                          Duration totalExecutionTime, Duration longestOperationTime, Instant lastResetTime,
	                          long totalOperations, long memoryAllocations, long memoryDeallocations)
	{
		this.peakMemoryUsage = peakMemoryUsage;
		this.averageMemoryUsage = averageMemoryUsage;
		this.currentMemoryUsage = currentMemoryUsage;
		this.peakThreadCount = peakThreadCount;
		this.averageThreadCount = averageThreadCount;
		this.currentThreadCount = currentThreadCount;
		this.totalExecutionTime = totalExecutionTime;
		this.longestOperationTime = longestOperationTime;
		this.lastResetTime = lastResetTime;
		this.totalOperations = totalOperations;
		this.memoryAllocations = memoryAllocations;
		this.memoryDeallocations = memoryDeallocations;
	}

	/**
	 * Returns the peak memory usage observed since statistics were last reset.
	 *
	 * @return the peak memory usage in bytes
	 */
	public long getPeakMemoryUsage()
	{
		return peakMemoryUsage;
	}

	/**
	 * Returns the average memory usage since statistics were last reset.
	 *
	 * @return the average memory usage in bytes
	 */
	public long getAverageMemoryUsage()
	{
		return averageMemoryUsage;
	}

	/**
	 * Returns the current memory usage.
	 *
	 * @return the current memory usage in bytes
	 */
	public long getCurrentMemoryUsage()
	{
		return currentMemoryUsage;
	}

	/**
	 * Returns the peak thread count observed since statistics were last reset.
	 *
	 * @return the peak thread count
	 */
	public int getPeakThreadCount()
	{
		return peakThreadCount;
	}

	/**
	 * Returns the average thread count since statistics were last reset.
	 *
	 * @return the average thread count
	 */
	public double getAverageThreadCount()
	{
		return averageThreadCount;
	}

	/**
	 * Returns the current thread count.
	 *
	 * @return the current thread count
	 */
	public int getCurrentThreadCount()
	{
		return currentThreadCount;
	}

	/**
	 * Returns the total execution time since statistics were last reset.
	 *
	 * @return the total execution time
	 */
	public Duration getTotalExecutionTime()
	{
		return totalExecutionTime;
	}

	/**
	 * Returns the longest single operation time since statistics were last reset.
	 *
	 * @return the longest operation time
	 */
	public Duration getLongestOperationTime()
	{
		return longestOperationTime;
	}

	/**
	 * Returns the time when these statistics were last reset.
	 *
	 * @return the last reset time
	 */
	public Instant getLastResetTime()
	{
		return lastResetTime;
	}

	/**
	 * Returns the total number of operations performed since statistics were last reset.
	 *
	 * @return the total operation count
	 */
	public long getTotalOperations()
	{
		return totalOperations;
	}

	/**
	 * Returns the total number of memory allocations since statistics were last reset.
	 *
	 * @return the total allocation count
	 */
	public long getMemoryAllocations()
	{
		return memoryAllocations;
	}

	/**
	 * Returns the total number of memory deallocations since statistics were last reset.
	 *
	 * @return the total deallocation count
	 */
	public long getMemoryDeallocations()
	{
		return memoryDeallocations;
	}

	/**
	 * Returns the memory efficiency ratio (deallocations / allocations).
	 * <p>
	 * Values close to 1.0 indicate efficient memory management with proper cleanup.
	 * Values significantly less than 1.0 may indicate memory leaks.
	 *
	 * @return the memory efficiency ratio (0.0 to 1.0)
	 */
	public double getMemoryEfficiencyRatio()
	{
		if (memoryAllocations == 0) return 1.0;
		return Math.min(1.0, (double) memoryDeallocations / memoryAllocations);
	}

	/**
	 * Returns the average operations per second since statistics were last reset.
	 *
	 * @return the operations per second rate
	 */
	public double getOperationsPerSecond()
	{
		long totalSeconds = totalExecutionTime.getSeconds();
		if (totalSeconds == 0) return 0.0;
		return (double) totalOperations / totalSeconds;
	}

	/**
	 * Returns whether the plugin appears to have memory management issues.
	 * <p>
	 * This is determined by analyzing the memory efficiency ratio and
	 * comparing allocation patterns to typical efficient implementations.
	 *
	 * @return {@code true} if memory issues are suspected, {@code false} otherwise
	 */
	public boolean hasMemoryIssues()
	{
		return getMemoryEfficiencyRatio() < 0.8 ||
		       (peakMemoryUsage > averageMemoryUsage * 3);
	}

	/**
	 * Returns whether the plugin appears to have performance issues.
	 * <p>
	 * This is determined by analyzing operation times and resource utilization
	 * patterns compared to typical efficient implementations.
	 *
	 * @return {@code true} if performance issues are suspected, {@code false} otherwise
	 */
	public boolean hasPerformanceIssues()
	{
		return longestOperationTime.getSeconds() > 30 ||
		       getOperationsPerSecond() < 1.0;
	}

	@Override
	public String toString()
	{
		return String.format("ResourceUsageStats{" +
		                     "peakMemory=%d bytes, " +
		                     "avgMemory=%d bytes, " +
		                     "currentMemory=%d bytes, " +
		                     "peakThreads=%d, " +
		                     "avgThreads=%.1f, " +
		                     "currentThreads=%d, " +
		                     "totalTime=%s, " +
		                     "longestOp=%s, " +
		                     "operations=%d, " +
		                     "opsPerSec=%.2f" +
		                     "}",
		                     peakMemoryUsage, averageMemoryUsage, currentMemoryUsage,
		                     peakThreadCount, averageThreadCount, currentThreadCount,
		                     totalExecutionTime, longestOperationTime,
		                     totalOperations, getOperationsPerSecond());
	}
}