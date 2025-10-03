package io.github.cowwoc.styler.cli.security;

import io.github.cowwoc.styler.cli.security.exceptions.PathTraversalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Sanitizes and validates file paths to prevent traversal attacks.
 *
 * <p>This sanitizer detects suspicious path traversal patterns and normalizes
 * paths for safe processing.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * PathSanitizer sanitizer = new PathSanitizer();
 *
 * try {
 *     Path safe = sanitizer.sanitize(Paths.get("src/main/java/Example.java"));
 *     // Returns normalized absolute path
 * } catch (PathTraversalException e) {
 *     System.err.println("Suspicious path: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see PathTraversalException
 */
public final class PathSanitizer
{
	private final Logger log = LoggerFactory.getLogger(PathSanitizer.class);

	/**
	 * Constructs a new path sanitizer.
	 */
	public PathSanitizer()
	{
	}

	/**
	 * Sanitizes and validates the specified path.
	 *
	 * <p>This method performs:
	 * <ol>
	 *   <li>Path normalization (resolves {@code .} and {@code ..})</li>
	 *   <li>Symbolic link resolution</li>
	 *   <li>Traversal pattern detection</li>
	 * </ol>
	 *
	 * @param inputPath the path to sanitize, must not be {@code null}
	 * @return the sanitized absolute path
	 * @throws PathTraversalException if path contains suspicious traversal patterns
	 * @throws NullPointerException if inputPath is {@code null}
	 */
	public Path sanitize(Path inputPath) throws PathTraversalException
	{
		Objects.requireNonNull(inputPath, "inputPath must not be null");

		// Step 1: Normalize to resolve . and .. sequences
		Path normalized = inputPath.normalize().toAbsolutePath();

		// Step 2: Resolve symbolic links to real path
		Path real;
		try
		{
			real = normalized.toRealPath();
		}
		catch (IOException e)
		{
			// Path doesn't exist yet or cannot be resolved
			// Use normalized path but continue validation
			real = normalized;
		}

		// Step 3: Detect suspicious traversal patterns in original input
		detectTraversalPatterns(inputPath);

		return real;
	}

	/**
	 * Detects suspicious path traversal patterns in the input path.
	 *
	 * <p>Patterns detected:
	 * <ul>
	 *   <li>Multiple consecutive {@code ..} sequences</li>
	 *   <li>Paths attempting to access system directories</li>
	 * </ul>
	 *
	 * @param inputPath the original input path
	 * @throws PathTraversalException if suspicious patterns detected
	 */
	private void detectTraversalPatterns(Path inputPath) throws PathTraversalException
	{
		String pathString = inputPath.toString();

		// Detect multiple consecutive .. sequences (e.g., ../../../)
		if (pathString.matches(".*\\.\\..*\\.\\..*\\.\\..*"))
		{
			throw new PathTraversalException(
				"Path contains suspicious traversal pattern: " + inputPath);
		}

		// Detect attempts to access common system directories
		Path normalized = inputPath.normalize().toAbsolutePath();
		String normalizedString = normalized.toString().toLowerCase(java.util.Locale.ROOT);

		if (normalizedString.startsWith("/etc/") ||
			normalizedString.startsWith("/sys/") ||
			normalizedString.startsWith("/proc/") ||
			normalizedString.contains("/windows/system32"))
		{
			log.warn("WARNING: Attempting to access system directory: {}", normalized);
		}
	}

	/**
	 * Checks if the specified path can be sanitized without throwing exceptions.
	 *
	 * @param path the path to check, must not be {@code null}
	 * @return {@code true} if path can be sanitized, {@code false} if it contains suspicious patterns
	 * @throws NullPointerException if path is {@code null}
	 */
	public boolean isPathSafe(Path path)
	{
		Objects.requireNonNull(path, "path must not be null");

		try
		{
			sanitize(path);
			return true;
		}
		catch (PathTraversalException e)
		{
			return false;
		}
	}
}