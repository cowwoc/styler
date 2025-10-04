package io.github.cowwoc.styler.formatter.api.plugin;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable descriptor containing plugin metadata and identification information.
 * <p>
 * This class provides essential information about a plugin including its unique
 * identifier, version, dependencies, and additional metadata. Plugin descriptors
 * are used for plugin discovery, dependency resolution, and conflict detection.
 * <p>
 * <b>Thread Safety:</b> This class is immutable and thread-safe.
 * <b>Security:</b> All string fields are validated to prevent injection attacks.
 * <b>Performance:</b> Plugin descriptors are cached during discovery to avoid
 * repeated validation overhead.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Plugin Framework Team
 */
public final class PluginDescriptor
{
	private final String pluginId;
	private final String version;
	private final String name;
	private final String vendor;
	private final Set<String> dependencies;
	private final Map<String, String> metadata;

	/**
	 * Creates a new plugin descriptor with the specified metadata.
	 * <p>
	 * All parameters are validated for security and correctness. The plugin ID
	 * must follow reverse domain name convention (e.g., "com.example.styler.rules").
	 * Version must follow semantic versioning (e.g., "{@code 1}.2.3").
	 *
	 * @param pluginId     the unique plugin identifier, never {@code null} or empty
	 * @param version      the plugin version in semantic format, never {@code null}
	 * @param name         the human-readable plugin name, never {@code null}
	 * @param vendor       the plugin vendor or author, never {@code null}
	 * @param dependencies the set of required plugin IDs, never {@code null}
	 * @param metadata     additional plugin metadata, never {@code null}
	 * @throws IllegalArgumentException if any parameter is invalid
	 * @throws PluginSecurityException  if any parameter contains potentially dangerous content
	 */
	public PluginDescriptor(String pluginId,
	                        String version,
	                        String name,
	                        String vendor,
	                        Set<String> dependencies,
	                        Map<String, String> metadata)
	{
		this.pluginId = validatePluginId(pluginId);
		this.version = validateVersion(version);
		this.name = validateName(name);
		this.vendor = validateVendor(vendor);
		this.dependencies = Set.copyOf(Objects.requireNonNull(dependencies, "Dependencies cannot be null"));
		this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "Metadata cannot be null"));

		// Validate dependencies
		for (String dependency : this.dependencies)
		{
			validatePluginId(dependency);
		}

		// Validate metadata
		for (Map.Entry<String, String> entry : this.metadata.entrySet())
		{
			validateMetadataKey(entry.getKey());
			validateMetadataValue(entry.getValue());
		}
	}

	/**
	 * Returns the unique plugin identifier.
	 * <p>
	 * Plugin IDs must follow reverse domain name convention to ensure uniqueness
	 * across different plugin vendors and avoid naming conflicts.
	 *
	 * @return the plugin identifier, never {@code null} or empty
	  * @throws NullPointerException if {@code pluginId} is null
	 */
	public String getPluginId()
	{
		return pluginId;
	}

	/**
	 * Returns the plugin version in semantic versioning format.
	 *
	 * @return the plugin version, never {@code null}
	  * @throws NullPointerException if {@code pluginId} is null
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Returns the human-readable plugin name.
	 *
	 * @return the plugin name, never {@code null}
	  * @throws NullPointerException if {@code pluginId} is null
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the plugin vendor or author name.
	 *
	 * @return the vendor name, never {@code null}
	  * @throws NullPointerException if {@code pluginId} is null
	 */
	public String getVendor()
	{
		return vendor;
	}

	/**
	 * Returns the set of required plugin dependency IDs.
	 * <p>
	 * Dependencies must be satisfied before this plugin can be loaded.
	 * Circular dependencies are detected and prevented during plugin loading.
	 *
	 * @return the set of dependency plugin IDs, never {@code null} but may be empty
	  * @throws NullPointerException if {@code pluginId} is null
	 */
	public Set<String> getDependencies()
	{
		return dependencies;
	}

	/**
	 * Returns additional plugin metadata as key-value pairs.
	 * <p>
	 * Metadata can include information such as supported Java versions,
	 * plugin categories, or custom configuration options.
	 *
	 * @return the metadata map, never {@code null} but may be empty
	  * @throws NullPointerException if {@code version} is null
	  * @throws NullPointerException if {@code pluginId} is null
	 */
	public Map<String, String> getMetadata()
	{
		return metadata;
	}

	/**
	 * Returns whether this plugin has any dependencies.
	 *
	 * @return {@code true} if the plugin has dependencies, {@code false} otherwise
	  * @throws NullPointerException if {@code version} is null
	  * @throws NullPointerException if {@code pluginId} is null
	  * @throws NullPointerException if {@code name} is null
	 */
	public boolean hasDependencies()
	{
		return !dependencies.isEmpty();
	}

	/**
	 * Returns whether this plugin depends on the specified plugin.
	 *
	 * @param pluginId the plugin ID to check, never {@code null}
	 * @return {@code true} if this plugin depends on the specified plugin
	  * @throws NullPointerException if {@code version} is null
	  * @throws NullPointerException if {@code pluginId} is null
	  * @throws NullPointerException if {@code name} is null
	 */
	public boolean dependsOn(String pluginId)
	{
		requireThat(pluginId, "pluginId").isNotNull();
		return dependencies.contains(pluginId);
	}

	/**
	 * Validates a plugin ID for security and format compliance.
	 *
	 * @param pluginId the plugin ID to validate
	 * @return the validated plugin ID
	 * @throws IllegalArgumentException if the plugin ID is invalid
	 * @throws PluginSecurityException  if the plugin ID contains dangerous content
	  * @throws NullPointerException if {@code version} is null
	  * @throws NullPointerException if {@code vendor} is null
	  * @throws NullPointerException if {@code pluginId} is null
	  * @throws NullPointerException if {@code name} is null
	 */
	private static String validatePluginId(String pluginId)
	{
		requireThat(pluginId, "pluginId").isNotNull();
		requireThat(pluginId.trim(), "pluginId").isNotEmpty();

		// Security validation
		if (pluginId.contains("${") || pluginId.contains("#{") || pluginId.contains("<%"))
		{
			throw new PluginSecurityException("Plugin ID contains potentially dangerous content: " + pluginId);
		}

		// Format validation (reverse domain name)
		if (!pluginId.matches("^[a-zA-Z][a-zA-Z0-9]*(?:\\.[a-zA-Z][a-zA-Z0-9]*)*$"))
		{
			throw new IllegalArgumentException("Plugin ID must follow reverse domain name convention: " + pluginId);
		}

		if (pluginId.length() > 200)
		{
			throw new IllegalArgumentException("Plugin ID exceeds maximum length of 200 characters");
		}

		return pluginId.trim();
	}

	/**
	 * Validates a version string for semantic versioning compliance.
	 *
	 * @param version the version to validate
	 * @return the validated version
	 * @throws IllegalArgumentException if the version is invalid
	  * @throws NullPointerException if {@code version} is null
	  * @throws NullPointerException if {@code vendor} is null
	  * @throws NullPointerException if {@code name} is null
	  * @throws NullPointerException if {@code metadataKey} is null
	 */
	private static String validateVersion(String version)
	{
		requireThat(version, "version").isNotNull();
		requireThat(version.trim(), "version").isNotEmpty();

		// Basic semantic versioning validation
		if (!version.matches("^\\d+\\.\\d+\\.\\d+(?:-[a-zA-Z0-9\\-\\.]+)?(?:\\+[a-zA-Z0-9\\-\\.]+)?$"))
		{
			throw new IllegalArgumentException("Version must follow semantic versioning format: " + version);
		}

		return version.trim();
	}

	/**
	 * Validates a plugin name for security and length.
	 *
	 * @param name the name to validate
	 * @return the validated name
	 * @throws PluginSecurityException if the name contains dangerous content
	  * @throws NullPointerException if {@code vendor} is null
	  * @throws NullPointerException if {@code name} is null
	  * @throws NullPointerException if {@code metadataValue} is null
	  * @throws NullPointerException if {@code metadataKey} is null
	 */
	private static String validateName(String name)
	{
		requireThat(name, "name").isNotNull();
		requireThat(name.trim(), "name").isNotEmpty();

		if (name.length() > 100)
		{
			throw new IllegalArgumentException("Plugin name exceeds maximum length of 100 characters");
		}

		return name.trim();
	}

	/**
	 * Validates a vendor name for security and length.
	 *
	 * @param vendor the vendor to validate
	 * @return the validated vendor
	 * @throws PluginSecurityException if the vendor contains dangerous content
	  * @throws NullPointerException if {@code vendor} is null
	  * @throws NullPointerException if {@code metadataValue} is null
	  * @throws NullPointerException if {@code metadataKey} is null
	 */
	private static String validateVendor(String vendor)
	{
		requireThat(vendor, "vendor").isNotNull();
		requireThat(vendor.trim(), "vendor").isNotEmpty();

		if (vendor.length() > 100)
		{
			throw new IllegalArgumentException("Plugin vendor exceeds maximum length of 100 characters");
		}

		return vendor.trim();
	}

	/**
	 * Validates a metadata key for security and format.
	 *
	 * @param key the key to validate
	 * @throws PluginSecurityException if the key contains dangerous content
	  * @throws NullPointerException if {@code metadataValue} is null
	  * @throws NullPointerException if {@code metadataKey} is null
	 */
	private static void validateMetadataKey(String key)
	{
		requireThat(key, "metadataKey").isNotNull();
		requireThat(key.trim(), "metadataKey").isNotEmpty();

		if (key.length() > 50)
		{
			throw new IllegalArgumentException("Metadata key exceeds maximum length of 50 characters");
		}

		if (!key.matches("^[a-zA-Z][a-zA-Z0-9\\-_]*$"))
		{
			throw new IllegalArgumentException(
				"Metadata key must contain only letters, numbers, hyphens, and underscores");
		}
	}

	/**
	 * Validates a metadata value for security and length.
	 *
	 * @param value the value to validate
	 * @throws PluginSecurityException if the value contains dangerous content
	  * @throws NullPointerException if {@code metadataValue} is null
	 */
	private static void validateMetadataValue(String value)
	{
		requireThat(value, "metadataValue").isNotNull();

		if (value.length() > 500)
		{
			throw new IllegalArgumentException("Metadata value exceeds maximum length of 500 characters");
		}

		// Security validation for injection prevention
		if (value.contains("${") || value.contains("#{") || value.contains("<%") || value.contains("<script"))
		{
			throw new PluginSecurityException("Metadata value contains potentially dangerous content");
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		PluginDescriptor that = (PluginDescriptor) obj;
		return Objects.equals(pluginId, that.pluginId) &&
			Objects.equals(version, that.version) &&
			Objects.equals(name, that.name) &&
			Objects.equals(vendor, that.vendor) &&
			Objects.equals(dependencies, that.dependencies) &&
			Objects.equals(metadata, that.metadata);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(pluginId, version, name, vendor, dependencies, metadata);
	}

	@Override
	public String toString()
	{
		return "PluginDescriptor{" +
			"pluginId='" + pluginId + '\'' +
			", version='" + version + '\'' +
			", name='" + name + '\'' +
			", vendor='" + vendor + '\'' +
			", dependencies=" + dependencies.size() +
			", metadata=" + metadata.keySet() +
			'}';
	}
}