package io.github.cowwoc.styler.cli.security.exceptions;

import java.nio.file.Path;

/**
 * Thrown when a file exceeds the configured maximum size limit.
 *
 * <p>This exception includes the file path, actual size, and limit to provide
 * actionable debugging information for users.
 *
 * <h2>Example Error Message:</h2>
 * <pre>
 * File size exceeds maximum limit
 *   File: /path/to/LargeClass.java
 *   Size: 75.3 MB
 *   Limit: 50.0 MB
 *
 *   This file is too large for safe parsing. Consider:
 *   - Splitting into smaller files
 *   - Excluding from formatting
 *   - Increasing limit in configuration
 * </pre>
 */
public final class FileSizeExceededException extends SecurityException
{
	private final Path filePath;
	private final long actualSize;
	private final long maxSize;

	/**
	 * Constructs a new file size exceeded exception.
	 *
	 * @param filePath the path to the file that exceeded the limit
	 * @param actualSize the actual file size in bytes
	 * @param maxSize the configured maximum size in bytes
	 */
	public FileSizeExceededException(Path filePath, long actualSize, long maxSize)
	{
		super(formatMessage(filePath, actualSize, maxSize));
		this.filePath = filePath;
		this.actualSize = actualSize;
		this.maxSize = maxSize;
	}

	private static String formatMessage(Path filePath, long actualSize, long maxSize)
	{
		return String.format(
			"File size exceeds maximum limit%n" +
			"  File: %s%n" +
			"  Size: %.1f MB%n" +
			"  Limit: %.1f MB%n" +
			"%n" +
			"  This file is too large for safe parsing. Consider:%n" +
			"  - Splitting into smaller files%n" +
			"  - Excluding from formatting%n" +
			"  - Increasing limit in configuration",
			filePath,
			actualSize / 1024.0 / 1024.0,
			maxSize / 1024.0 / 1024.0
		);
	}

	/**
	 * Returns the path to the file that exceeded the size limit.
	 *
	 * @return the file path
	 */
	public Path filePath()
	{
		return filePath;
	}

	/**
	 * Returns the actual size of the file in bytes.
	 *
	 * @return the actual file size
	 */
	public long actualSize()
	{
		return actualSize;
	}

	/**
	 * Returns the configured maximum file size in bytes.
	 *
	 * @return the maximum file size
	 */
	public long maxSize()
	{
		return maxSize;
	}
}