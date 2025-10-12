package io.github.cowwoc.styler.security.exceptions;

/**
 * Exception thrown when heap memory usage exceeds the configured limit.
 * <p>
 * This exception indicates potential memory exhaustion and triggers cleanup or
 * throttling to prevent OutOfMemoryError.
 */
public class MemoryLimitExceededException extends SecurityException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception indicating memory limit violation.
	 *
	 * @param currentUsage current heap usage in bytes
	 * @param limit        configured memory limit in bytes
	 */
	public MemoryLimitExceededException(long currentUsage, long limit)
	{
		super(String.format(
			"Heap memory usage (%d bytes) exceeds limit (%d bytes). " +
			"Reduce concurrent file processing or increase maxHeapBytes configuration.",
			currentUsage, limit));
	}
}
