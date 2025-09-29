package io.github.cowwoc.styler.cli.security.exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when temporary file creation exceeds configured limits.
 *
 * <p>This exception prevents disk space exhaustion from runaway temporary file
 * creation during file processing operations.
 *
 * <p>Error messages include current usage, configured limits, and resolution
 * steps to assist with debugging and recovery.
 */
public final class TempFileLimitExceededException extends SecurityException
{
	private final int currentFiles;
	private final int maxFiles;
	private final long currentDiskBytes;
	private final long maxDiskBytes;
	private final Path tempDirectory;

	/**
	 * Creates a new temporary file limit exceeded exception for file count.
	 *
	 * @param currentFiles current number of temporary files
	 * @param maxFiles maximum allowed temporary files
	 * @param tempDirectory the temporary directory
	 * @throws IllegalArgumentException if currentFiles or maxFiles are not positive
	 * @throws NullPointerException if tempDirectory is null
	 */
	public TempFileLimitExceededException(int currentFiles, int maxFiles, Path tempDirectory)
	{
		super(formatFileCountMessage(currentFiles, maxFiles, tempDirectory));
		if (currentFiles <= 0)
		{
			throw new IllegalArgumentException("currentFiles must be positive: " + currentFiles);
		}
		if (maxFiles <= 0)
		{
			throw new IllegalArgumentException("maxFiles must be positive: " + maxFiles);
		}
		if (tempDirectory == null)
		{
			throw new NullPointerException("tempDirectory must not be null");
		}
		this.currentFiles = currentFiles;
		this.maxFiles = maxFiles;
		this.currentDiskBytes = 0;
		this.maxDiskBytes = 0;
		this.tempDirectory = tempDirectory;
	}

	/**
	 * Creates a new temporary file limit exceeded exception for disk usage.
	 *
	 * @param currentDiskBytes current temporary disk usage in bytes
	 * @param maxDiskBytes maximum allowed temporary disk usage in bytes
	 * @param tempDirectory the temporary directory
	 * @throws IllegalArgumentException if currentDiskBytes or maxDiskBytes are not positive
	 * @throws NullPointerException if tempDirectory is null
	 */
	public TempFileLimitExceededException(long currentDiskBytes, long maxDiskBytes, Path tempDirectory)
	{
		super(formatDiskUsageMessage(currentDiskBytes, maxDiskBytes, tempDirectory));
		if (currentDiskBytes <= 0)
		{
			throw new IllegalArgumentException("currentDiskBytes must be positive: " + currentDiskBytes);
		}
		if (maxDiskBytes <= 0)
		{
			throw new IllegalArgumentException("maxDiskBytes must be positive: " + maxDiskBytes);
		}
		if (tempDirectory == null)
		{
			throw new NullPointerException("tempDirectory must not be null");
		}
		this.currentFiles = 0;
		this.maxFiles = 0;
		this.currentDiskBytes = currentDiskBytes;
		this.maxDiskBytes = maxDiskBytes;
		this.tempDirectory = tempDirectory;
	}

	/**
	 * Returns the current number of temporary files.
	 *
	 * @return current temporary file count (0 if limit was disk usage)
	 */
	public int getCurrentFiles()
	{
		return currentFiles;
	}

	/**
	 * Returns the maximum allowed temporary files.
	 *
	 * @return maximum temporary files (0 if limit was disk usage)
	 */
	public int getMaxFiles()
	{
		return maxFiles;
	}

	/**
	 * Returns the current temporary disk usage in bytes.
	 *
	 * @return current disk usage (0 if limit was file count)
	 */
	public long getCurrentDiskBytes()
	{
		return currentDiskBytes;
	}

	/**
	 * Returns the maximum allowed temporary disk usage in bytes.
	 *
	 * @return maximum disk usage (0 if limit was file count)
	 */
	public long getMaxDiskBytes()
	{
		return maxDiskBytes;
	}

	/**
	 * Returns the temporary directory path.
	 *
	 * @return temporary directory
	 */
	public Path getTempDirectory()
	{
		return tempDirectory;
	}

	/**
	 * Formats an error message for file count limit.
	 */
	private static String formatFileCountMessage(int currentFiles, int maxFiles, Path tempDirectory)
	{
		return String.format(
			"Temporary file count exceeds maximum limit%n" +
			"  Directory: %s%n" +
			"  Files: %d%n" +
			"  Limit: %d files%n" +
			"%n" +
			"  Too many temporary files created during processing.%n" +
			"  Consider:%n" +
			"  - Processing files in smaller batches%n" +
			"  - Cleaning up temporary files more frequently%n" +
			"  - Increasing limit in configuration",
			tempDirectory,
			currentFiles,
			maxFiles
		);
	}

	/**
	 * Formats an error message for disk usage limit.
	 */
	private static String formatDiskUsageMessage(long currentBytes, long maxBytes, Path tempDirectory)
	{
		return String.format(
			"Temporary disk usage exceeds maximum limit%n" +
			"  Directory: %s%n" +
			"  Usage: %.1f MB%n" +
			"  Limit: %.1f MB%n" +
			"%n" +
			"  Temporary files are consuming too much disk space.%n" +
			"  Consider:%n" +
			"  - Processing files in smaller batches%n" +
			"  - Reducing temporary file sizes%n" +
			"  - Increasing limit in configuration",
			tempDirectory,
			currentBytes / 1024.0 / 1024.0,
			maxBytes / 1024.0 / 1024.0
		);
	}
}