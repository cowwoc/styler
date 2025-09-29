package io.github.cowwoc.styler.cli.security;

import io.github.cowwoc.styler.cli.security.exceptions.PathTraversalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Sanitizes and validates file paths to prevent traversal attacks and ensure
 * files are within the expected project scope.
 *
 * <p>This sanitizer follows the single-user tool security model: path traversal
 * patterns are detected and warned about, but not necessarily blocked, since users
 * have filesystem permissions already. However, suspicious patterns indicate
 * potential user mistakes.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * PathSanitizer sanitizer = new PathSanitizer();
 *
 * try {
 *     Path safe = sanitizer.sanitize(Paths.get("../../../etc/passwd"));
 *     // Warning logged but path returned (single-user tool)
 * } catch (PathTraversalException e) {
 *     System.err.println("Suspicious path: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see PathTraversalException
 */
public final class PathSanitizer
{
	private static final Logger logger = LoggerFactory.getLogger(PathSanitizer.class);
	private final Path projectRoot;

	/**
	 * Constructs a new path sanitizer using the current working directory
	 * as the project root.
	 */
	public PathSanitizer()
	{
		this(Path.of("").toAbsolutePath());
	}

	/**
	 * Constructs a new path sanitizer with the specified project root.
	 *
	 * @param projectRoot the project root directory for boundary validation
	 * @throws NullPointerException if projectRoot is null
	 */
	public PathSanitizer(Path projectRoot)
	{
		Objects.requireNonNull(projectRoot, "projectRoot must not be null");
		this.projectRoot = projectRoot.toAbsolutePath().normalize();
	}

	/**
	 * Sanitizes and validates the specified path.
	 *
	 * <p>This method performs:
	 * <ol>
	 *   <li>Path normalization (resolves {@code .} and {@code ..})</li>
	 *   <li>Symbolic link resolution</li>
	 *   <li>Traversal pattern detection</li>
	 *   <li>Project boundary validation (warns if outside project)</li>
	 * </ol>
	 *
	 * @param inputPath the path to sanitize, must not be null
	 * @return the sanitized absolute path
	 * @throws PathTraversalException if path contains suspicious traversal patterns
	 * @throws NullPointerException if inputPath is null
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

		// Step 4: Validate against project boundary (warn but allow)
		validateProjectBoundary(real);

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
		String normalizedString = normalized.toString().toLowerCase();

		if (normalizedString.startsWith("/etc/") ||
			normalizedString.startsWith("/sys/") ||
			normalizedString.startsWith("/proc/") ||
			normalizedString.contains("/windows/system32"))
		{
			logger.warn("WARNING: Attempting to access system directory: {}", normalized);
		}
	}

	/**
	 * Validates that the path is within the project boundary.
	 *
	 * <p>In the single-user tool security model, this logs a warning but does
	 * not block processing, as users have filesystem permissions already.
	 *
	 * @param realPath the resolved real path
	 */
	private void validateProjectBoundary(Path realPath)
	{
		if (!realPath.startsWith(projectRoot))
		{
			logger.warn(
				"WARNING: Processing file outside project directory\n" +
				"  File: {}\n" +
				"  Project Root: {}\n" +
				"  This may indicate a path specification mistake.",
				realPath, projectRoot
			);
		}
	}

	/**
	 * Checks if the specified path is considered safe (within project boundaries).
	 *
	 * @param path the path to check, must not be null
	 * @return true if path is within project root, false otherwise
	 * @throws NullPointerException if path is null
	 */
	public boolean isPathSafe(Path path)
	{
		Objects.requireNonNull(path, "path must not be null");

		try
		{
			Path sanitized = sanitize(path);
			return sanitized.startsWith(projectRoot);
		}
		catch (PathTraversalException e)
		{
			return false;
		}
	}
}