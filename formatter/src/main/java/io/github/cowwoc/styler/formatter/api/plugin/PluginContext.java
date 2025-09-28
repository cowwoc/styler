package io.github.cowwoc.styler.formatter.api.plugin;

import java.nio.file.Path;
import org.slf4j.Logger;

/**
 * Execution context provided to plugins for resource access and framework integration.
 * <p>
 * The plugin context provides controlled access to formatter engine services
 * including logging, resource management, and plugin-specific data storage.
 * Context instances are created per plugin and provide isolated environments
 * to prevent plugin interference.
 * <p>
 * <b>Thread Safety:</b> All context methods are thread-safe and can be called
 * concurrently from multiple plugin threads.
 * <b>Security:</b> Resource access is controlled through the context to enforce
 * security boundaries and prevent unauthorized system access.
 * <b>Performance:</b> Context operations are optimized for frequent access
 * during formatting operations.
 *
 * @since 1.0.0
 * @author Plugin Framework Team
 */
public interface PluginContext
{
	/**
	 * Returns the plugin descriptor for the context owner.
	 * <p>
	 * This provides access to plugin metadata for logging and identification
	 * purposes within the plugin implementation.
	 *
	 * @return the plugin descriptor, never {@code null}
	 */
	PluginDescriptor getPluginDescriptor();

	/**
	 * Returns a logger instance configured for this plugin.
	 * <p>
	 * The logger is pre-configured with the plugin ID for automatic message
	 * prefixing and filtering. All plugin log messages are automatically
	 * attributed to the plugin for debugging and monitoring purposes.
	 * <p>
	 * <b>Security:</b> Log messages are automatically sanitized to prevent
	 * information disclosure and injection attacks.
	 *
	 * @return the plugin logger, never {@code null}
	 */
	Logger getLogger();

	/**
	 * Returns the plugin-specific data directory for persistent storage.
	 * <p>
	 * Each plugin receives an isolated directory for storing configuration files,
	 * cache data, or other persistent information. The directory is created
	 * automatically if it doesn't exist.
	 * <p>
	 * <b>Security:</b> File access is restricted to the plugin's data directory
	 * to prevent unauthorized access to system files or other plugin data.
	 *
	 * @return the plugin data directory path, never {@code null}
	 * @throws PluginException if the data directory cannot be created or accessed
	 */
	Path getDataDirectory() throws PluginException;

	/**
	 * Returns the resource manager for monitoring and controlling plugin resource usage.
	 * <p>
	 * The resource manager provides access to memory limits, execution timeouts,
	 * and other resource constraints configured for this plugin. Plugins should
	 * check resource limits before performing expensive operations.
	 *
	 * @return the resource manager, never {@code null}
	 */
	ResourceManager getResourceManager();

	/**
	 * Returns runtime information about the formatter engine and environment.
	 * <p>
	 * Runtime information includes formatter version, Java version, available
	 * memory, and other environmental details that may influence plugin behavior.
	 *
	 * @return the runtime information, never {@code null}
	 */
	RuntimeInfo getRuntimeInfo();

	/**
	 * Returns whether the plugin is running in debug mode.
	 * <p>
	 * Debug mode enables additional logging, validation, and diagnostic features
	 * that may impact performance but provide valuable troubleshooting information.
	 *
	 * @return {@code true} if debug mode is enabled, {@code false} otherwise
	 */
	boolean isDebugMode();

	/**
	 * Records a metric value for plugin performance monitoring.
	 * <p>
	 * Metrics are collected by the formatter engine for performance analysis,
	 * health monitoring, and optimization. Plugins should record relevant
	 * metrics such as processing time, memory usage, or operation counts.
	 * <p>
	 * <b>Performance:</b> Metric recording is optimized for frequent calls
	 * and should not significantly impact plugin performance.
	 *
	 * @param metricName the metric name, never {@code null} or empty
	 * @param value      the metric value
	 * @throws IllegalArgumentException if the metric name is invalid
	 */
	void recordMetric(String metricName, double value);

	/**
	 * Records a metric value with additional context tags.
	 * <p>
	 * Tagged metrics allow for more detailed analysis by grouping values
	 * by context such as file type, rule category, or processing phase.
	 *
	 * @param metricName the metric name, never {@code null} or empty
	 * @param value      the metric value
	 * @param tags       the context tags as key-value pairs, never {@code null}
	 * @throws IllegalArgumentException if the metric name or tags are invalid
	 */
	void recordMetric(String metricName, double value, java.util.Map<String, String> tags);
}