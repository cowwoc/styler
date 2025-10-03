package io.github.cowwoc.styler.cli.security.exceptions;


import java.io.Serial;
/**
 * Thrown when memory usage exceeds the configured limit during processing.
 *
 * <p>This prevents JVM heap exhaustion and system instability when processing
 * large codebases or memory-intensive files.
 *
 * <h2>Example Error Message:</h2>
 * <pre>
 * Memory limit exceeded during processing
 *   Current: 520 MB
 *   Limit: 512 MB
 *
 *   To resolve:
 *   - Process files in smaller batches
 *   - Increase heap limit: java -Xmx1024m
 *   - Exclude large files from processing
 * </pre>
 */
public final class MemoryLimitExceededException extends SecurityException
{
	@Serial
	private static final long serialVersionUID = 1L;
	private final long currentMemory;
	private final long maxMemory;

	/**
	 * Constructs a new memory limit exceeded exception.
	 *
	 * @param currentMemory the current memory usage in bytes
	 * @param maxMemory the configured maximum memory in bytes
	 */
	public MemoryLimitExceededException(long currentMemory, long maxMemory)
	{
		super(formatMessage(currentMemory, maxMemory));
		this.currentMemory = currentMemory;
		this.maxMemory = maxMemory;
	}

	private static String formatMessage(long currentMemory, long maxMemory)
	{
		return String.format(
			"Memory limit exceeded during processing%n" +
			"  Current: %d MB%n" +
			"  Limit: %d MB%n" +
			"%n" +
			"  To resolve:%n" +
			"  - Process files in smaller batches%n" +
			"  - Increase heap limit: java -Xmx%dm%n" +
			"  - Exclude large files from processing",
			currentMemory / 1024 / 1024,
			maxMemory / 1024 / 1024,
			// Suggest 2x current limit
			(maxMemory / 1024 / 1024) * 2);
	}

	/**
	 * Returns the current memory usage in bytes.
	 *
	 * @return the current memory usage
	 */
	public long currentMemory()
	{
		return currentMemory;
	}

	/**
	 * Returns the configured maximum memory in bytes.
	 *
	 * @return the maximum memory limit
	 */
	public long maxMemory()
	{
		return maxMemory;
	}
}