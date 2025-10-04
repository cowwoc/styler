package io.github.cowwoc.styler.formatter.api.plugin;

import java.nio.file.Path;

/**
 * File access policy interface for controlling plugin file system access.
 * <p>
 * File access policies enforce security boundaries by restricting which
 * directories and file types plugins can access. This prevents unauthorized
 * access to system files, other plugin data, or sensitive configuration files.
 * <p>
 * <b>Thread Safety:</b> All policy methods are thread-safe and can be called
 * concurrently from multiple plugin threads.
 * <b>Security:</b> Policy violations result in SecurityException and may
 * trigger plugin termination for repeated offenses.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Plugin Framework Team
 */
public interface FileAccessPolicy
{
	/**
	 * Checks if the plugin can read from the specified path.
	 * <p>
	 * Read access is typically granted for source files being formatted,
	 * the plugin's data directory, and explicitly configured additional
	 * directories.
	 *
	 * @param path the path to check, never {@code null}
	 * @return {@code true} if read access is allowed, {@code false} otherwise
	 */
	boolean canRead(Path path);

	/**
	 * Checks if the plugin can write to the specified path.
	 * <p>
	 * Write access is typically restricted to the plugin's data directory
	 * and temporary file locations to prevent system file modification.
	 *
	 * @param path the path to check, never {@code null}
	 * @return {@code true} if write access is allowed, {@code false} otherwise
	 */
	boolean canWrite(Path path);

	/**
	 * Checks if the plugin can execute files at the specified path.
	 * <p>
	 * Execute access is typically denied for security reasons unless
	 * explicitly configured for specific plugin requirements.
	 *
	 * @param path the path to check, never {@code null}
	 * @return {@code true} if execute access is allowed, {@code false} otherwise
	 */
	boolean canExecute(Path path);

	/**
	 * Validates file access and throws SecurityException if denied.
	 * <p>
	 * This method provides a convenient way to enforce access policies
	 * with automatic exception throwing for denied access attempts.
	 *
	 * @param path       the path to validate, never {@code null}
	 * @param accessType the type of access being requested, never {@code null}
	 * @throws SecurityException if access is denied
	 */
	void validateAccess(Path path, FileAccessType accessType);

	/**
	 * Returns the set of allowed file extensions for plugin access.
	 * <p>
	 * File extension restrictions provide an additional security layer
	 * by limiting plugins to specific file types such as .java, .properties,
	 * or .xml files.
	 *
	 * @return the set of allowed file extensions (including the dot), never {@code null}
	 */
	java.util.Set<String> getAllowedExtensions();

	/**
	 * Returns the set of base directories where plugin file access is permitted.
	 * <p>
	 * Base directories define the root paths within which plugins can access
	 * files, typically including the source directory and plugin data directory.
	 *
	 * @return the set of allowed base directories, never {@code null}
	 */
	java.util.Set<Path> getAllowedDirectories();

	/**
	 * Enumeration of file access types for policy validation.
	 */
	enum FileAccessType
	{
		/** Read access to file contents. */
		READ,

		/** Write access to modify or create files. */
		WRITE,

		/** Execute access to run files as programs. */
		EXECUTE,

		/** Access to file metadata (size, modification time, etc.). */
		METADATA
	}
}