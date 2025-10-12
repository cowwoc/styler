package io.github.cowwoc.styler.security.exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when a path traversal attack is detected.
 * <p>
 * Path traversal attempts (e.g., "../../../etc/passwd") are blocked to prevent
 * unauthorized file access outside the designated working directory.
 */
public class PathTraversalException extends SecurityException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception indicating path traversal attempt.
	 *
	 * @param path           the suspicious path
	 * @param canonicalPath  the canonical (resolved) path
	 * @param allowedRoot    the allowed root directory
	 */
	public PathTraversalException(Path path, Path canonicalPath, Path allowedRoot)
	{
		super(String.format(
			"Path traversal detected: '%s' resolves to '%s' which is outside allowed root '%s'. " +
			"Only paths within the working directory are permitted.",
			path, canonicalPath, allowedRoot));
	}
}
