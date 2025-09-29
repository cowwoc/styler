package io.github.cowwoc.styler.cli.security;

import java.util.Objects;

/**
 * Thread-local context for tracking resource usage per operation.
 *
 * <p>This context provides per-thread isolation for resource monitoring,
 * enabling safe concurrent processing of multiple files without race conditions.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Start resource monitoring for current thread
 * ResourceContext.set(new ResourceContext("format-file"));
 * try {
 *     processFile(path);
 *
 *     // Check elapsed time
 *     ResourceContext context = ResourceContext.get();
 *     long elapsed = context.getElapsedMillis();
 * } finally {
 *     // Always clear context
 *     ResourceContext.clear();
 * }
 * }</pre>
 *
 * <p><strong>Important:</strong> Always call {@link #clear()} in a finally block
 * to prevent memory leaks.
 */
public final class ResourceContext
{
	private static final ThreadLocal<ResourceContext> CONTEXT = new ThreadLocal<>();

	private final String operationName;
	private final long startTime;
	private final long initialMemory;

	/**
	 * Constructs a new resource context for the current operation.
	 *
	 * @param operationName descriptive name for this operation, must not be null
	 * @throws NullPointerException if operationName is null
	 */
	public ResourceContext(String operationName)
	{
		Objects.requireNonNull(operationName, "operationName must not be null");
		this.operationName = operationName;
		this.startTime = System.currentTimeMillis();

		// Capture initial memory usage
		Runtime runtime = Runtime.getRuntime();
		this.initialMemory = runtime.totalMemory() - runtime.freeMemory();
	}

	/**
	 * Returns the resource context for the current thread.
	 *
	 * @return the current context, or null if not set
	 */
	public static ResourceContext get()
	{
		return CONTEXT.get();
	}

	/**
	 * Sets the resource context for the current thread.
	 *
	 * @param context the context to set, must not be null
	 * @throws NullPointerException if context is null
	 */
	public static void set(ResourceContext context)
	{
		Objects.requireNonNull(context, "context must not be null");
		CONTEXT.set(context);
	}

	/**
	 * Clears the resource context for the current thread.
	 *
	 * <p>Always call this method in a finally block to prevent memory leaks.
	 */
	public static void clear()
	{
		CONTEXT.remove();
	}

	/**
	 * Returns the operation name for this context.
	 *
	 * @return the operation name
	 */
	public String getOperationName()
	{
		return operationName;
	}

	/**
	 * Returns the start time of this operation in milliseconds since epoch.
	 *
	 * @return the start time
	 */
	public long getStartTime()
	{
		return startTime;
	}

	/**
	 * Returns the elapsed time since operation start in milliseconds.
	 *
	 * @return elapsed time in milliseconds
	 */
	public long getElapsedMillis()
	{
		return System.currentTimeMillis() - startTime;
	}

	/**
	 * Returns the initial memory usage when this context was created.
	 *
	 * @return initial memory in bytes
	 */
	public long getInitialMemory()
	{
		return initialMemory;
	}
}