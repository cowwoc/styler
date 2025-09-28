package io.github.cowwoc.styler.formatter.api.plugin;

import java.util.Map;

/**
 * Runtime information interface providing access to formatter engine and environment details.
 * <p>
 * Runtime information includes formatter version, Java runtime details, system
 * properties, and performance characteristics that may influence plugin behavior.
 * This information is read-only and provides environmental context for plugin
 * decision-making.
 * <p>
 * <b>Thread Safety:</b> All runtime information methods are thread-safe and
 * return immutable data structures.
 * <b>Security:</b> Sensitive system information is filtered to prevent information
 * disclosure while providing useful environmental context.
 *
 * @since 1.0.0
 * @author Plugin Framework Team
 */
public interface RuntimeInfo
{
	/**
	 * Returns the formatter engine version.
	 * <p>
	 * The formatter version follows semantic versioning and can be used by
	 * plugins to adapt behavior based on available engine features or API changes.
	 *
	 * @return the formatter version string, never {@code null}
	 */
	String getFormatterVersion();

	/**
	 * Returns the Java runtime version being used.
	 * <p>
	 * Java version information helps plugins determine available language
	 * features and adapt their formatting behavior accordingly.
	 *
	 * @return the Java version string, never {@code null}
	 */
	String getJavaVersion();

	/**
	 * Returns the Java runtime major version number.
	 * <p>
	 * This provides the major version number (e.g., 8, 11, 17, 21) for
	 * convenient version comparisons without string parsing.
	 *
	 * @return the Java major version number
	 */
	int getJavaMajorVersion();

	/**
	 * Returns the operating system name.
	 * <p>
	 * Operating system information can be used by plugins to adapt file
	 * path handling or enable platform-specific features.
	 *
	 * @return the operating system name, never {@code null}
	 */
	String getOperatingSystem();

	/**
	 * Returns the system architecture (e.g., x86_64, aarch64).
	 * <p>
	 * Architecture information may be relevant for plugins that interact
	 * with native libraries or have architecture-specific optimizations.
	 *
	 * @return the system architecture, never {@code null}
	 */
	String getArchitecture();

	/**
	 * Returns the total available memory for the JVM in bytes.
	 * <p>
	 * Memory information helps plugins understand system capacity and
	 * adjust their memory usage patterns accordingly.
	 *
	 * @return the total available memory in bytes
	 */
	long getTotalMemory();

	/**
	 * Returns the maximum memory that the JVM can use in bytes.
	 * <p>
	 * Maximum memory represents the -Xmx setting and provides the upper
	 * bound for memory allocation across all plugins and the formatter engine.
	 *
	 * @return the maximum memory in bytes
	 */
	long getMaxMemory();

	/**
	 * Returns the number of available processor cores.
	 * <p>
	 * Processor core count helps plugins determine optimal parallelism
	 * levels for multi-threaded operations.
	 *
	 * @return the number of available processor cores
	 */
	int getAvailableProcessors();

	/**
	 * Returns the formatter engine uptime in milliseconds.
	 * <p>
	 * Uptime information can be used for performance monitoring and
	 * debugging long-running formatter instances.
	 *
	 * @return the engine uptime in milliseconds
	 */
	long getUptimeMillis();

	/**
	 * Returns whether the formatter is running in debug mode.
	 * <p>
	 * Debug mode indicates enhanced logging, validation, and diagnostic
	 * features are enabled, which may impact performance but provide
	 * valuable troubleshooting information.
	 *
	 * @return {@code true} if debug mode is enabled, {@code false} otherwise
	 */
	boolean isDebugMode();

	/**
	 * Returns whether the formatter is running in development mode.
	 * <p>
	 * Development mode may enable additional features like hot-reloading,
	 * relaxed validation, or experimental functionality not suitable for
	 * production environments.
	 *
	 * @return {@code true} if development mode is enabled, {@code false} otherwise
	 */
	boolean isDevelopmentMode();

	/**
	 * Returns the working directory for the current formatting operation.
	 * <p>
	 * The working directory provides context for resolving relative paths
	 * and understanding the project structure being formatted.
	 *
	 * @return the working directory path, never {@code null}
	 */
	java.nio.file.Path getWorkingDirectory();

	/**
	 * Returns environment variables relevant to formatter operation.
	 * <p>
	 * Environment variables are filtered to include only formatter-specific
	 * and generally safe variables to prevent information disclosure while
	 * providing useful configuration context.
	 *
	 * @return a map of environment variables, never {@code null} but may be empty
	 */
	Map<String, String> getEnvironmentVariables();

	/**
	 * Returns system properties relevant to formatter operation.
	 * <p>
	 * System properties are filtered to include only formatter-specific
	 * and generally safe properties to prevent information disclosure while
	 * providing useful configuration context.
	 *
	 * @return a map of system properties, never {@code null} but may be empty
	 */
	Map<String, String> getSystemProperties();

	/**
	 * Returns the temporary directory available for plugin use.
	 * <p>
	 * The temporary directory provides a location for plugins to store
	 * short-lived files during formatting operations. Files in this directory
	 * may be cleaned up automatically by the formatter engine.
	 *
	 * @return the temporary directory path, never {@code null}
	 */
	java.nio.file.Path getTempDirectory();

	/**
	 * Returns the character encoding used for source file processing.
	 * <p>
	 * Encoding information helps plugins handle text processing consistently
	 * with the formatter engine's file reading behavior.
	 *
	 * @return the default character encoding, never {@code null}
	 */
	java.nio.charset.Charset getDefaultEncoding();
}