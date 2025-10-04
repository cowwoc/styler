package io.github.cowwoc.styler.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Detects symbolic link cycles during directory traversal.
 *
 * <p>This detector tracks visited canonical paths using thread-local storage,
 * allowing safe concurrent file discovery operations. It prevents infinite loops
 * caused by circular symbolic links.
 *
 * <p>Thread safety: Each thread maintains its own visited path set, enabling
 * parallel file discovery without synchronization overhead.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * SymlinkCycleDetector detector = new SymlinkCycleDetector(50);
 *
 * try {
 *     if (detector.isCycle(symlinkPath)) {
 *         System.err.println("Cycle detected: " + symlinkPath);
 *     } else {
 *         // Process symlink target
 *     }
 * } finally {
 *     detector.reset();
 * }
 * }</pre>
 *
 * @see RecursionDepthTracker
 */
public final class SymlinkCycleDetector
{
	private static final Logger log = LoggerFactory.getLogger(SymlinkCycleDetector.class);

	/** Maximum symlink depth to prevent excessive following. */
	private final int maxSymlinkDepth;

	/** Thread-local set of visited canonical paths. */
	private final ThreadLocal<VisitedPaths> visitedPaths =
		ThreadLocal.withInitial(VisitedPaths::new);

	/**
	 * Creates a new symlink cycle detector.
	 *
	 * @param maxSymlinkDepth maximum symlink depth to follow
	 * @throws IllegalArgumentException if maxSymlinkDepth is not positive
	 */
	public SymlinkCycleDetector(int maxSymlinkDepth)
	{
		if (maxSymlinkDepth <= 0)
		{
			throw new IllegalArgumentException(
				"maxSymlinkDepth must be positive: " + maxSymlinkDepth);
		}
		this.maxSymlinkDepth = maxSymlinkDepth;
	}

	/**
	 * Checks if following the specified path would create a cycle.
	 *
	 * <p>This method:
	 * <ol>
	 *   <li>Resolves the path to its canonical form</li>
	 *   <li>Checks if canonical path was already visited</li>
	 *   <li>Adds canonical path to visited set if not a cycle</li>
	 *   <li>Enforces maximum symlink depth limit</li>
	 * </ol>
	 *
	 * @param path the path to check (may be a symlink), must not be {@code null}
	 * @return {@code true} if following this path would create a cycle
	 * @throws NullPointerException if path is {@code null}
	 * @throws IllegalStateException if symlink depth limit exceeded
	 */
	public boolean isCycle(Path path)
	{
		Objects.requireNonNull(path, "path must not be null");

		VisitedPaths context = visitedPaths.get();

		// Check symlink depth limit
		if (context.symlinkDepth >= maxSymlinkDepth)
		{
			throw new IllegalStateException(
				"Symlink depth limit exceeded: " + maxSymlinkDepth +
				" levels at " + path);
		}

		// Skip non-symlinks
		if (!Files.isSymbolicLink(path))
		{
			return false;
		}

		// Resolve to canonical path
		Path canonical;
		try
		{
			canonical = path.toRealPath();
		}
		catch (IOException e)
		{
			log.warn("Failed to resolve symlink: {} - {}", path, e.getMessage());
			return false;  // Treat unresolvable symlinks as non-cycles
		}

		// Check for cycle
		if (context.visited.contains(canonical))
		{
			log.warn("Symlink cycle detected at: {}", path);
			return true;
		}

		// Track this path
		context.visited.add(canonical);
		++context.symlinkDepth;

		return false;
	}

	/**
	 * Resets the visited path set for the current thread.
	 *
	 * <p>This should be called after completing a file discovery operation
	 * to clean up thread-local state.
	 */
	public void reset()
	{
		VisitedPaths context = visitedPaths.get();
		context.visited.clear();
		context.symlinkDepth = 0;
	}

	/**
	 * Clears the thread-local context, preventing memory leaks.
	 *
	 * <p>This must be called when the thread will no longer use this detector.
	 * Typically called in a finally block or shutdown hook.
	 */
	public void clear()
	{
		visitedPaths.remove();
	}

	/**
	 * Returns the current symlink depth for the calling thread.
	 *
	 * @return current symlink depth
	 */
	public int getCurrentSymlinkDepth()
	{
		return visitedPaths.get().symlinkDepth;
	}

	/**
	 * Thread-local context for tracking visited paths.
	 */
	private static final class VisitedPaths
	{
		private final Set<Path> visited = new HashSet<>();
		private int symlinkDepth;
	}
}
