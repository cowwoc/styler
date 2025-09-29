package io.github.cowwoc.styler.cli.security.exceptions;

import java.nio.file.Path;

/**
 * Thrown when a file path contains suspicious traversal patterns that could
 * access files outside the intended project directory.
 *
 * <p>In the single-user tool security model, this is typically a warning rather
 * than a hard block, as users have filesystem permissions already. However, it
 * indicates a potential mistake in path specification.
 *
 * <h2>Example Error Message:</h2>
 * <pre>
 * Path contains suspicious traversal pattern
 *   Attempted: ../../../etc/passwd
 *   Normalized: /etc/passwd
 *   Project Root: /workspace/project
 *
 *   This path attempts to access files outside your project directory.
 *   Use relative paths within the project or explicit absolute paths.
 * </pre>
 */
public final class PathTraversalException extends SecurityException
{
	private final Path attemptedPath;
	private final Path normalizedPath;

	/**
	 * Constructs a new path traversal exception.
	 *
	 * @param attemptedPath the original path provided by the user
	 * @param normalizedPath the resolved absolute path
	 */
	public PathTraversalException(Path attemptedPath, Path normalizedPath)
	{
		super(formatMessage(attemptedPath, normalizedPath));
		this.attemptedPath = attemptedPath;
		this.normalizedPath = normalizedPath;
	}

	/**
	 * Constructs a new path traversal exception with a custom message.
	 *
	 * @param message the custom error message
	 */
	public PathTraversalException(String message)
	{
		super(message);
		this.attemptedPath = null;
		this.normalizedPath = null;
	}

	/**
	 * Constructs a new path traversal exception with a custom message and cause.
	 *
	 * @param message the custom error message
	 * @param cause the underlying cause
	 */
	public PathTraversalException(String message, Throwable cause)
	{
		super(message, cause);
		this.attemptedPath = null;
		this.normalizedPath = null;
	}

	private static String formatMessage(Path attemptedPath, Path normalizedPath)
	{
		return String.format(
			"Path contains suspicious traversal pattern%n" +
			"  Attempted: %s%n" +
			"  Normalized: %s%n" +
			"%n" +
			"  This path attempts to access files outside your project directory.%n" +
			"  Use relative paths within the project or explicit absolute paths.",
			attemptedPath,
			normalizedPath
		);
	}

	/**
	 * Returns the original path provided by the user.
	 *
	 * @return the attempted path, or null if constructed with custom message
	 */
	public Path attemptedPath()
	{
		return attemptedPath;
	}

	/**
	 * Returns the resolved absolute path.
	 *
	 * @return the normalized path, or null if constructed with custom message
	 */
	public Path normalizedPath()
	{
		return normalizedPath;
	}
}