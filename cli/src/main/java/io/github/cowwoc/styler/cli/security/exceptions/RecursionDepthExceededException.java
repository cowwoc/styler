package io.github.cowwoc.styler.cli.security.exceptions;

/**
 * Exception thrown when recursion depth exceeds configured limits during AST traversal.
 *
 * <p>This exception prevents stack overflow errors from processing deeply nested
 * Java code structures such as nested blocks, method calls, or class definitions.
 *
 * <p>Error messages include the current depth, configured limit, and location
 * context to assist with debugging.
 */
public final class RecursionDepthExceededException extends SecurityException
{
	private static final long serialVersionUID = 1L;
	private final int currentDepth;
	private final int maxDepth;
	private final String location;

	/**
	 * Creates a new recursion depth exceeded exception.
	 *
	 * @param currentDepth the current recursion depth
	 * @param maxDepth the configured maximum depth
	 * @param location the location context (e.g., file name, AST node type)
	 * @throws IllegalArgumentException if currentDepth or maxDepth are not positive
	 * @throws NullPointerException if location is {@code null}
	 */
	public RecursionDepthExceededException(int currentDepth, int maxDepth, String location)
	{
		super(formatMessage(currentDepth, maxDepth, location));
		if (currentDepth <= 0)
		{
			throw new IllegalArgumentException("currentDepth must be positive: " + currentDepth);
		}
		if (maxDepth <= 0)
		{
			throw new IllegalArgumentException("maxDepth must be positive: " + maxDepth);
		}
		if (location == null)
		{
			throw new NullPointerException("location must not be null");
		}
		this.currentDepth = currentDepth;
		this.maxDepth = maxDepth;
		this.location = location;
	}

	/**
	 * Returns the current recursion depth that exceeded the limit.
	 *
	 * @return current recursion depth
	 */
	public int getCurrentDepth()
	{
		return currentDepth;
	}

	/**
	 * Returns the configured maximum recursion depth.
	 *
	 * @return maximum recursion depth
	 */
	public int getMaxDepth()
	{
		return maxDepth;
	}

	/**
	 * Returns the location context where the limit was exceeded.
	 *
	 * @return location context
	 */
	public String getLocation()
	{
		return location;
	}

	/**
	 * Formats a detailed, actionable error message.
	 *
	 * @param currentDepth the current recursion depth
	 * @param maxDepth the maximum allowed recursion depth
	 * @param location the location where the recursion limit was exceeded
	 * @return the formatted error message
	 */
	private static String formatMessage(int currentDepth, int maxDepth, String location)
	{
		return String.format(
			"Recursion depth exceeds maximum limit%n" +
			"  Location: %s%n" +
			"  Current depth: %d levels%n" +
			"  Maximum depth: %d levels%n" +
			"%n" +
			"  This code structure has excessive nesting that risks stack overflow.%n" +
			"  Consider:%n" +
			"  - Refactoring deeply nested code into separate methods%n" +
			"  - Simplifying complex nested control structures%n" +
			"  - Increasing recursion limit in configuration (if justified)",
			location,
			currentDepth,
			maxDepth);
	}
}