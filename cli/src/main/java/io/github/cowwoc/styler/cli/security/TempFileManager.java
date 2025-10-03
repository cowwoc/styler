package io.github.cowwoc.styler.cli.security;

import io.github.cowwoc.styler.cli.security.exceptions.TempFileLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages temporary file creation and cleanup with enforced resource limits.
 *
 * <p>This component tracks temporary files created during formatting operations
 * and enforces limits on file count and disk usage to prevent resource exhaustion.
 * A shutdown hook ensures cleanup even if the JVM exits unexpectedly.
 *
 * <p>Usage pattern:
 * <pre>{@code
 * TempFileManager manager = new TempFileManager(1000, 1024 * 1024 * 1024);
 *
 * Path tempFile = manager.createTempFile("formatter", ".java");
 * try {
 *     // Use temporary file
 * } finally {
 *     manager.cleanup(); // Cleanup all tracked files
 * }
 * }</pre>
 *
 * @see SecurityConfig#maxTempFiles()
 * @see SecurityConfig#maxTempDiskBytes()
 */
@SuppressWarnings("PMD.AvoidSynchronizedStatement") // synchronized(lock) is optimal for this straightforward tracker
public final class TempFileManager
{
	private final Logger log = LoggerFactory.getLogger(TempFileManager.class);

	private final Object lock = new Object();
	private final int maxFiles;
	private final long maxDiskBytes;
	private final Path tempDirectory;
	private final List<Path> trackedFiles;
	private final Map<Path, Long> fileSizes;
	private long currentDiskUsage;

	/**
	 * Creates a new temporary file manager with the specified limits.
	 *
	 * <p>Automatically registers a shutdown hook to ensure cleanup on JVM exit.
	 *
	 * @param maxFiles maximum number of temporary files
	 * @param maxDiskBytes maximum disk usage for temporary files in bytes
	 * @throws IllegalArgumentException if maxFiles or maxDiskBytes are not positive
	 */
	public TempFileManager(int maxFiles, long maxDiskBytes)
	{
		if (maxFiles <= 0)
		{
			throw new IllegalArgumentException("maxFiles must be positive: " + maxFiles);
		}
		if (maxDiskBytes <= 0)
		{
			throw new IllegalArgumentException("maxDiskBytes must be positive: " + maxDiskBytes);
		}

		this.maxFiles = maxFiles;
		this.maxDiskBytes = maxDiskBytes;
		this.tempDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
		this.trackedFiles = new ArrayList<>();
		this.fileSizes = new HashMap<>();
		this.currentDiskUsage = 0;

		// Register shutdown hook for cleanup
		Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup, "TempFileCleanup"));
	}

	/**
	 * Creates a new temporary file with the specified prefix and suffix.
	 *
	 * <p>The created file is automatically tracked and will be deleted during cleanup.
	 *
	 * @param prefix the file name prefix
	 * @param suffix the file name suffix (e.g., ".java")
	 * @return path to the created temporary file
	 * @throws TempFileLimitExceededException if file count or disk usage limit exceeded
	 * @throws IOException if file creation fails
	 * @throws NullPointerException if prefix or suffix is {@code null}
	 */
	public Path createTempFile(String prefix, String suffix) throws IOException
	{
		if (prefix == null)
		{
			throw new NullPointerException("prefix must not be null");
		}
		if (suffix == null)
		{
			throw new NullPointerException("suffix must not be null");
		}

		synchronized (lock)
		{
			// Check file count limit
			if (trackedFiles.size() >= maxFiles)
			{
				throw new TempFileLimitExceededException(
					trackedFiles.size() + 1,
					maxFiles,
					tempDirectory);
			}

			// Create the temporary file
			Path tempFile = Files.createTempFile(prefix, suffix);
			long fileSize = Files.size(tempFile);

			// Check disk usage limit
			if (currentDiskUsage + fileSize > maxDiskBytes)
			{
				// Delete the file we just created since we can't track it
				try
				{
					Files.deleteIfExists(tempFile);
				}
				catch (IOException e)
				{
					log.warn("Failed to delete temporary file after limit exceeded: {}", tempFile, e);
				}

				throw new TempFileLimitExceededException(
					currentDiskUsage + fileSize,
					maxDiskBytes,
					tempDirectory);
			}

			// Track the file
			trackedFiles.add(tempFile);
			fileSizes.put(tempFile, fileSize);
			currentDiskUsage += fileSize;

			log.debug("Created temporary file: {} (size: {} bytes, total: {} files, {} MB)",
				tempFile, fileSize, trackedFiles.size(), currentDiskUsage / 1024 / 1024);

			return tempFile;
		}
	}

	/**
	 * Updates the disk usage tracking for an existing temporary file.
	 *
	 * <p>This should be called after writing to a tracked temporary file to
	 * ensure disk usage limits remain accurate.
	 *
	 * @param tempFile the temporary file that was modified
	 * @throws TempFileLimitExceededException if new disk usage exceeds limit
	 * @throws IOException if file size cannot be determined
	 * @throws IllegalArgumentException if file is not tracked
	 */
	public void updateFileSize(Path tempFile) throws IOException
	{
		synchronized (lock)
		{
			if (!trackedFiles.contains(tempFile))
			{
				throw new IllegalArgumentException("File is not tracked: " + tempFile);
			}

			// Get the previously recorded size for this file
			long oldSize = fileSizes.getOrDefault(tempFile, 0L);
			long newSize = Files.size(tempFile);

			// Calculate the size delta
			long sizeDelta = newSize - oldSize;
			long newTotal = currentDiskUsage + sizeDelta;

			if (newTotal > maxDiskBytes)
			{
				throw new TempFileLimitExceededException(newTotal, maxDiskBytes, tempDirectory);
			}

			// Update tracking
			currentDiskUsage = newTotal;
			fileSizes.put(tempFile, newSize);
		}
	}

	/**
	 * Returns the current number of tracked temporary files.
	 *
	 * @return current file count
	 */
	public int getCurrentFileCount()
	{
		synchronized (lock)
		{
			return trackedFiles.size();
		}
	}

	/**
	 * Returns the current disk usage of tracked temporary files in bytes.
	 *
	 * @return current disk usage in bytes
	 */
	public long getCurrentDiskUsage()
	{
		synchronized (lock)
		{
			return currentDiskUsage;
		}
	}

	/**
	 * Cleans up all tracked temporary files.
	 *
	 * <p>This method is idempotent and can be called multiple times safely.
	 * It is automatically called by the shutdown hook.
	 */
	public void cleanup()
	{
		synchronized (lock)
		{
			int deletedCount = 0;
			int failedCount = 0;

			for (Path file : trackedFiles)
			{
				try
				{
					if (Files.deleteIfExists(file))
					{
						++deletedCount;
					}
				}
				catch (IOException e)
				{
					++failedCount;
					log.warn("Failed to delete temporary file: {}", file, e);
				}
			}

			if (deletedCount > 0 || failedCount > 0)
			{
				log.info("Temporary file cleanup: {} deleted, {} failed",
					deletedCount, failedCount);
			}

			trackedFiles.clear();
			fileSizes.clear();
			currentDiskUsage = 0;
		}
	}
}