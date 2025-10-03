package io.github.cowwoc.styler.cli.security;

import io.github.cowwoc.styler.cli.security.exceptions.RecursionDepthExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks recursion depth during AST traversal to prevent stack overflow errors.
 *
 * <p>This component monitors nesting depth during tree-based traversal operations
 * and enforces configured depth limits. It uses thread-local tracking to support
 * concurrent processing of multiple files.
 *
 * <p>Usage pattern:
 * <pre>{@code
 * RecursionDepthTracker tracker = new RecursionDepthTracker(1000, 500);
 *
 * void visitNode(ASTNode node) {
 *     tracker.enter("ASTNode:" + node.getType());
 *     try {
 *         // Process node
 *         for (ASTNode child : node.getChildren()) {
 *             visitNode(child);
 *         }
 *     } finally {
 *         tracker.exit();
 *     }
 * }
 * }</pre>
 *
 * @see SecurityConfig#maxRecursionDepth()
 * @see SecurityConfig#warnRecursionDepth()
 */
public final class RecursionDepthTracker
{
	private final Logger log = LoggerFactory.getLogger(RecursionDepthTracker.class);

	private final int maxDepth;
	private final int warnDepth;
	private final ThreadLocal<DepthContext> context = ThreadLocal.withInitial(DepthContext::new);

	/**
	 * Creates a new recursion depth tracker.
	 *
	 * @param maxDepth maximum allowed recursion depth
	 * @param warnDepth warning threshold (logs warning when exceeded)
	 * @throws IllegalArgumentException if maxDepth or warnDepth are invalid
	 */
	public RecursionDepthTracker(int maxDepth, int warnDepth)
	{
		if (maxDepth <= 0)
		{
			throw new IllegalArgumentException("maxDepth must be positive: " + maxDepth);
		}
		if (warnDepth <= 0 || warnDepth >= maxDepth)
		{
			throw new IllegalArgumentException(
				"warnDepth must be positive and less than maxDepth: " +
				warnDepth + " (max: " + maxDepth + ")");
		}
		this.maxDepth = maxDepth;
		this.warnDepth = warnDepth;
	}

	/**
	 * Enters a new recursion level with the specified location context.
	 *
	 * <p>This method must be called before processing nested structures.
	 * Every call to {@code enter} must have a matching {@code exit} in
	 * a finally block.
	 *
	 * @param location the location context (e.g., "ClassDeclaration", "MethodCall")
	 * @throws RecursionDepthExceededException if depth exceeds maximum limit
	 * @throws NullPointerException if location is {@code null}
	 */
	public void enter(String location)
	{
		if (location == null)
		{
			throw new NullPointerException("location must not be null");
		}

		DepthContext ctx = context.get();
		++ctx.depth;

		// Check warning threshold
		if (ctx.depth == warnDepth)
		{
			log.warn("Recursion depth approaching limit: {} levels at {}",
				ctx.depth, location);
		}

		// Enforce maximum depth
		if (ctx.depth > maxDepth)
		{
			throw new RecursionDepthExceededException(ctx.depth, maxDepth, location);
		}
	}

	/**
	 * Exits the current recursion level.
	 *
	 * <p>This method must be called in a finally block to ensure depth
	 * tracking remains consistent even if exceptions occur.
	 *
	 * @throws IllegalStateException if called without matching {@code enter}
	 */
	public void exit()
	{
		DepthContext ctx = context.get();
		if (ctx.depth <= 0)
		{
			throw new IllegalStateException("exit() called without matching enter()");
		}
		--ctx.depth;
	}

	/**
	 * Returns the current recursion depth for the calling thread.
	 *
	 * @return current recursion depth ({@code 0} if not inside recursive traversal)
	 */
	public int getCurrentDepth()
	{
		return context.get().depth;
	}

	/**
	 * Resets the recursion depth tracker for the calling thread.
	 *
	 * <p>This should be called after completing a file processing operation
	 * to ensure thread-local state is cleaned up properly.
	 */
	public void reset()
	{
		DepthContext ctx = context.get();
		ctx.depth = 0;
	}

	/**
	 * Clears the thread-local context, preventing memory leaks.
	 *
	 * <p>This must be called when the thread will no longer use this tracker.
	 */
	public void clear()
	{
		context.remove();
	}

	/**
	 * Thread-local context for tracking recursion depth.
	 */
	private static final class DepthContext
	{
		private int depth;
	}
}