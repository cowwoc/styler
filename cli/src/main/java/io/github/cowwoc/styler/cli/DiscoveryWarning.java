package io.github.cowwoc.styler.cli;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a non-fatal warning encountered during file discovery.
 *
 * <p>Warnings are collected during file system traversal when non-critical errors
 * occur (permission denied, broken symlinks, etc.) allowing discovery to continue
 * while providing diagnostic information to the caller.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * DiscoveryWarning warning = new DiscoveryWarning(
 *     Paths.get("/restricted/file.java"),
 *     "Permission denied"
 * );
 * System.err.println("WARNING: " + warning.message() + " for " + warning.path());
 * }</pre>
 *
 * @param path the file or directory that triggered the warning
 * @param message the warning message describing the issue
 * @see DiscoveryResult
 */
public record DiscoveryWarning(Path path, String message)
{
	/**
	 * Compact constructor with validation.
	 *
	 * @throws NullPointerException if path or message is {@code null}
	 * @throws IllegalArgumentException if message is blank
	 */
	public DiscoveryWarning
	{
		Objects.requireNonNull(path, "path must not be null");
		Objects.requireNonNull(message, "message must not be null");
		if (message.isBlank())
		{
			throw new IllegalArgumentException("message must not be blank");
		}
	}
}
