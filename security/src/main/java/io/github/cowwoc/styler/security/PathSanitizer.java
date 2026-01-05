package io.github.cowwoc.styler.security;

import io.github.cowwoc.styler.security.exceptions.PathTraversalException;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Sanitizes and validates file paths to prevent path traversal attacks.
 * <p>
 * This class normalizes paths and ensures they remain within allowed boundaries,
 * protecting against malicious attempts to access files outside the working directory.
 * Thread-safe and stateless.
 */
public final class PathSanitizer
{
	/**
	 * Sanitizes and validates a path against the allowed root directory.
	 *
	 * @param path        path to sanitize
	 * @param allowedRoot root directory that path must remain within
	 * @return canonicalized safe path
	 * @throws PathTraversalException if path attempts to escape allowed root
	 * @throws IOException            if path cannot be canonicalized
	 */
	public Path sanitize(Path path, Path allowedRoot) throws PathTraversalException, IOException
	{
		requireThat(path, "path").isNotNull();
		requireThat(allowedRoot, "allowedRoot").isNotNull();

		Path canonicalPath = path.toRealPath();
		Path canonicalRoot = allowedRoot.toRealPath();

		if (!canonicalPath.startsWith(canonicalRoot))
			throw new PathTraversalException(path, canonicalPath, canonicalRoot);

		return canonicalPath;
	}

	/**
	 * Normalizes a path without checking traversal (use when root validation not required).
	 *
	 * @param path path to normalize
	 * @return normalized path
	 */
	public Path normalize(Path path)
	{
		requireThat(path, "path").isNotNull();
		return path.normalize();
	}
}
