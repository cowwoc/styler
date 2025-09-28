package io.github.cowwoc.styler.formatter.api.plugin;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * Resource management interface for controlling and monitoring plugin resource usage.
 * <p>
 * The resource manager enforces security and performance constraints on plugin
 * execution including memory limits, execution timeouts, and thread management.
 * These constraints prevent resource exhaustion attacks and ensure system stability.
 * <p>
 * <b>Thread Safety:</b> All resource manager methods are thread-safe and can be
 * called concurrently from multiple plugin threads.
 * <b>Security:</b> Resource limits are enforced automatically and cannot be
 * bypassed by plugins. Violations result in immediate plugin termination.
 *
 * @since 1.0.0
 * @author Plugin Framework Team
 */
public interface ResourceManager
{
	/**
	 * Returns the memory limit configured for this plugin in bytes.
	 * <p>
	 * Memory limits prevent plugins from consuming excessive heap space and
	 * causing system instability. Plugins should monitor their memory usage
	 * and implement appropriate cleanup strategies.
	 *
	 * @return the memory limit in bytes, always positive
	 */
	long getMemoryLimit();

	/**
	 * Returns the current memory usage for this plugin in bytes.
	 * <p>
	 * Memory usage is tracked across all plugin threads and includes both
	 * direct allocations and indirect allocations through framework APIs.
	 * <p>
	 * <b>Performance:</b> Memory tracking has minimal overhead but values
	 * may be approximate due to garbage collection timing.
	 *
	 * @return the current memory usage in bytes, always non-negative
	 */
	long getMemoryUsage();

	/**
	 * Returns the percentage of available memory currently in use.
	 * <p>
	 * This provides a convenient way to check memory usage relative to the
	 * configured limit without manual calculation.
	 *
	 * @return the memory usage percentage (0.0 to 100.0)
	 */
	default double getMemoryUsagePercentage()
	{
		long limit = getMemoryLimit();
		if (limit <= 0) return 0.0;
		return (getMemoryUsage() * 100.0) / limit;
	}

	/**
	 * Returns the execution timeout configured for this plugin.
	 * <p>
	 * Execution timeouts prevent plugins from running indefinitely and
	 * blocking the formatting process. Plugins should design their algorithms
	 * to complete within the configured timeout period.
	 *
	 * @return the execution timeout, never {@code null}
	 */
	Duration getExecutionTimeout();

	/**
	 * Returns the remaining execution time before timeout occurs.
	 * <p>
	 * This allows plugins to implement early termination strategies when
	 * approaching the timeout limit to ensure clean shutdown.
	 *
	 * @return the remaining execution time, never {@code null}
	 */
	Duration getRemainingExecutionTime();

	/**
	 * Checks if the plugin has exceeded its execution timeout.
	 * <p>
	 * Plugins should call this method periodically during long-running
	 * operations to detect timeout conditions and terminate gracefully.
	 *
	 * @throws TimeoutException if the execution timeout has been exceeded
	 */
	void checkTimeout() throws TimeoutException;

	/**
	 * Returns the executor service for plugin thread management.
	 * <p>
	 * Plugins should use this executor for any concurrent operations to ensure
	 * proper resource tracking and timeout enforcement. Direct thread creation
	 * is prohibited and may result in plugin termination.
	 * <p>
	 * <b>Security:</b> The executor service enforces thread limits and monitors
	 * thread activity for resource accounting purposes.
	 *
	 * @return the plugin executor service, never {@code null}
	 */
	ExecutorService getExecutor();

	/**
	 * Returns the maximum number of threads this plugin can create.
	 * <p>
	 * Thread limits prevent plugins from creating excessive numbers of threads
	 * that could impact system performance or stability.
	 *
	 * @return the maximum thread count, always positive
	 */
	int getMaxThreads();

	/**
	 * Returns the current number of active threads for this plugin.
	 * <p>
	 * Active threads include both plugin-created threads and threads currently
	 * executing plugin code through the framework executor service.
	 *
	 * @return the current thread count, always non-negative
	 */
	int getActiveThreadCount();

	/**
	 * Forces immediate garbage collection for this plugin's memory.
	 * <p>
	 * This method attempts to reclaim memory by running garbage collection
	 * specifically for plugin-allocated objects. It should be used sparingly
	 * as it may impact performance.
	 * <p>
	 * <b>Performance:</b> Garbage collection may block execution temporarily
	 * and should not be called frequently.
	 */
	void forceGarbageCollection();

	/**
	 * Returns whether the plugin is currently approaching resource limits.
	 * <p>
	 * This provides an early warning when resource usage is high but not yet
	 * at the enforcement threshold, allowing plugins to implement proactive
	 * resource management strategies.
	 *
	 * @return {@code true} if resource usage is approaching limits, {@code false} otherwise
	 */
	boolean isApproachingLimits();

	/**
	 * Returns the file access policy for this plugin.
	 * <p>
	 * File access policies control which directories and file types the plugin
	 * can read or write, enforcing security boundaries to prevent unauthorized
	 * access to system files or other plugin data.
	 *
	 * @return the file access policy, never {@code null}
	 */
	FileAccessPolicy getFileAccessPolicy();
}