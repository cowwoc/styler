package io.github.cowwoc.styler.formatter.api.plugin;

/**
 * Enumeration of plugin health status values.
 * <p>
 * Plugin health status is used by the formatter engine to monitor plugin
 * operational state and automatically disable problematic plugins to maintain
 * system stability. Plugins should report their status accurately based on
 * recent performance and error conditions.
 * <p>
 * <b>Thread Safety:</b> This enum is immutable and thread-safe.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Plugin Framework Team
 */
public enum PluginHealthStatus
{
	/**
	 * Plugin is operating normally with no detected issues.
	 * <p>
	 * This status indicates the plugin is functioning within expected parameters
	 * for execution time, memory usage, and error rates.
	 */
	HEALTHY,

	/**
	 * Plugin is experiencing minor issues but remains functional.
	 * <p>
	 * This status indicates elevated error rates, longer execution times, or
	 * increased memory usage that doesn't yet require intervention but should
	 * be monitored closely.
	 */
	DEGRADED,

	/**
	 * Plugin is experiencing significant issues and may require intervention.
	 * <p>
	 * This status indicates high error rates, timeout occurrences, or resource
	 * exhaustion that impacts plugin reliability. The formatter engine may
	 * reduce plugin usage or disable it temporarily.
	 */
	UNHEALTHY,

	/**
	 * Plugin has encountered critical errors and should be disabled.
	 * <p>
	 * This status indicates security violations, repeated failures, or resource
	 * exhaustion that makes the plugin unsafe for continued use. The formatter
	 * engine will disable the plugin until manual intervention occurs.
	 */
	FAILED,

	/**
	 * Plugin status cannot be determined due to monitoring failures.
	 * <p>
	 * This status indicates that health monitoring itself has failed, possibly
	 * due to plugin unresponsiveness or internal errors in the monitoring
	 * infrastructure.
	 */
	UNKNOWN
}