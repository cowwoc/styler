package io.github.cowwoc.styler.formatter.api.plugin;

import io.github.cowwoc.styler.formatter.api.FormattingRule;

import java.util.List;

/**
 * Main interface that all formatter plugins must implement.
 * <p>
 * This interface defines the contract for plugin lifecycle management, rule
 * creation, and metadata access. Plugins are discovered using Java's ServiceLoader
 * mechanism and must provide a no-argument constructor for instantiation.
 * <p>
 * <b>Thread Safety:</b> All implementations must be thread-safe for concurrent
 * plugin discovery and initialization operations.
 * <b>Security:</b> Plugin implementations are loaded in isolated ClassLoaders
 * with restricted permissions to prevent interference with the formatter engine.
 * <b>Performance:</b> Plugin initialization should complete within 10 seconds
 * to avoid blocking the discovery process.
 * <p>
 * <b>Example Usage:</b>
 * <pre>{@code
 * public class MyFormatterPlugin implements FormatterPlugin {
 *     private static final PluginDescriptor DESCRIPTOR = new PluginDescriptor(
 *         "com.example.myformatter",
 *         "{@code 1}.{@code 0}.{@code 0}",
 *         "My Formatter Plugin",
 *         "Example Corp",
 *         Set.of(),
 *         Map.of("category", "formatting")
 *     );
 *
 *     // Override
 *     public PluginDescriptor getDescriptor() {
 *         return DESCRIPTOR;
 *     }
 *
 *     // Override
 *     public List<FormattingRule> createRules(PluginContext context) {
 *         return List.of(new MyFormattingRule());
 *     }
 * }
 * }</pre>
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Plugin Framework Team
 * @see PluginDescriptor
 * @see PluginContext
 * @see FormattingRule
 */
public interface FormatterPlugin
{
	/**
	 * Returns the plugin descriptor containing metadata and identification information.
	 * <p>
	 * The descriptor provides essential information for plugin discovery, dependency
	 * resolution, and conflict detection. This method must return the same descriptor
	 * instance across multiple calls for consistency.
	 *
	 * @return the plugin descriptor, never {@code null}
	 */
	PluginDescriptor getDescriptor();

	/**
	 * Creates the formatting rules provided by this plugin.
	 * <p>
	 * This method is called during plugin initialization to obtain the formatting
	 * rules that will be registered with the formatter engine. Rules are created
	 * with access to the plugin context for logging, configuration, and resource
	 * management.
	 * <p>
	 * <b>Security:</b> Rule creation must complete within the configured timeout
	 * and respect memory limits to prevent resource exhaustion attacks.
	 *
	 * @param context the plugin execution context, never {@code null}
	 * @return the list of formatting rules provided by this plugin, never {@code null}
	 * @throws PluginException if rule creation fails due to configuration or resource issues
	 */
	List<FormattingRule> createRules(PluginContext context) throws PluginException;

	/**
	 * Initializes the plugin with the provided context.
	 * <p>
	 * This method is called once after plugin discovery and before rule creation.
	 * Plugins can use this opportunity to validate their environment, load
	 * configuration, and prepare resources needed for formatting operations.
	 * <p>
	 * <b>Performance:</b> Initialization should be lightweight and complete quickly
	 * to avoid blocking the plugin loading process.
	 * <b>Security:</b> Initialization must respect resource limits and timeout constraints.
	 *
	 * @param context the plugin execution context, never {@code null}
	 * @throws PluginException if initialization fails and the plugin cannot be used
	 */
	default void initialize(PluginContext context) throws PluginException
	{
		// Default implementation does nothing - plugins can override if needed
	}

	/**
	 * Shuts down the plugin and releases any resources.
	 * <p>
	 * This method is called when the plugin is being unloaded or the formatter
	 * engine is shutting down. Plugins should clean up resources such as file
	 * handles, network connections, or cached data.
	 * <p>
	 * <b>Performance:</b> Shutdown should complete quickly to avoid delays during
	 * application termination.
	 *
	 * @throws PluginException if shutdown encounters errors (logged but not fatal)
	 */
	default void shutdown() throws PluginException
	{
		// Default implementation does nothing - plugins can override if needed
	}

	/**
	 * Returns the current health status of the plugin.
	 * <p>
	 * This method allows the formatter engine to monitor plugin health and
	 * disable problematic plugins automatically. Plugins should return their
	 * current operational status based on recent activity and resource usage.
	 *
	 * @return the plugin health status, never {@code null}
	 */
	default PluginHealthStatus getHealthStatus()
	{
		return PluginHealthStatus.HEALTHY;
	}

	/**
	 * Returns whether this plugin supports the specified Java language version.
	 * <p>
	 * This method allows the formatter engine to filter plugins based on the
	 * Java version of the source code being formatted. Plugins that don't
	 * support a particular language version are skipped to avoid errors.
	 *
	 * @param javaVersion the Java language version (e.g., 8, 11, 17, 21)
	 * @return {@code true} if the plugin supports the specified version, {@code false} otherwise
	 */
	default boolean supportsJavaVersion(int javaVersion)
	{
		return true; // Default: support all Java versions
	}

	/**
	 * Returns the set of plugin capabilities supported by this implementation.
	 * <p>
	 * Capabilities allow the formatter engine to query plugin functionality
	 * without instantiating rules. This can be used for performance optimization
	 * and conflict resolution.
	 *
	 * @return the set of capability identifiers, never {@code null} but may be empty
	 */
	default java.util.Set<String> getCapabilities()
	{
		return java.util.Set.of();
	}
}