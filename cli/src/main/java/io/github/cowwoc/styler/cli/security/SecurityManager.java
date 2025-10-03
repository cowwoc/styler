package io.github.cowwoc.styler.cli.security;

import io.github.cowwoc.styler.cli.security.exceptions.SecurityException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Facade for all CLI security operations including file validation, path sanitization,
 * memory monitoring, timeout enforcement, recursion depth tracking, and temporary file management.
 *
 * <p>This manager provides a single entry point for security controls, simplifying
 * integration with the CLI argument parser and file processing pipeline.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Initialize with default configuration
 * SecurityManager manager = new SecurityManager(SecurityConfig.defaults());
 *
 * // Validate files before processing
 * try {
 *     manager.validateFile(Paths.get("MyClass.java"));
 * } catch (SecurityException e) {
 *     System.err.println("Security validation failed: " + e.getMessage());
 * }
 *
 * // Execute with timeout
 * String result = manager.executeWithTimeout(
 *     () -> formatFile(path),
 *     "format-file"
 * );
 *
 * // Monitor memory periodically
 * manager.checkMemoryLimit();
 *
 * // Track recursion depth during AST traversal
 * manager.enterRecursion("MethodDeclaration");
 * try {
 *     // Process nested structure
 * } finally {
 *     manager.exitRecursion();
 * }
 *
 * // Manage temporary files
 * Path tempFile = manager.createTempFile("formatter", ".java");
 * }</pre>
 *
 * @see SecurityConfig
 * @see FileValidator
 * @see PathSanitizer
 * @see MemoryMonitor
 * @see ExecutionTimeoutManager
 * @see RecursionDepthTracker
 * @see TempFileManager
 */
public final class SecurityManager
{
	private final FileValidator fileValidator;
	private final PathSanitizer pathSanitizer;
	private final MemoryMonitor memoryMonitor;
	private final ExecutionTimeoutManager timeoutManager;
	private final RecursionDepthTracker recursionDepthTracker;
	private final TempFileManager tempFileManager;

	/**
	 * Constructs a new security manager with the specified configuration.
	 *
	 * @param config the security configuration, must not be {@code null}
	 * @throws NullPointerException if config is {@code null}
	 */
	public SecurityManager(SecurityConfig config)
	{
		Objects.requireNonNull(config, "config must not be null");

		this.fileValidator = new FileValidator(
			config.maxFileSizeBytes(),
			config.allowedExtensions());
		this.pathSanitizer = new PathSanitizer();
		this.memoryMonitor = new MemoryMonitor(config.maxMemoryBytes());
		this.timeoutManager = new ExecutionTimeoutManager(config.timeoutMillis());
		this.recursionDepthTracker = new RecursionDepthTracker(
			config.maxRecursionDepth(),
			config.warnRecursionDepth());
		this.tempFileManager = new TempFileManager(
			config.maxTempFiles(),
			config.maxTempDiskBytes());
	}

	/**
	 * Validates a single file against all security constraints.
	 *
	 * <p>This method:
	 * <ol>
	 *   <li>Sanitizes the file path</li>
	 *   <li>Validates file size, type, and existence</li>
	 * </ol>
	 *
	 * @param filePath the file to validate, must not be {@code null}
	 * @return the sanitized file path
	 * @throws SecurityException if validation fails
	 * @throws IOException if file metadata cannot be accessed
	 * @throws NullPointerException if filePath is {@code null}
	 */
	public Path validateFile(Path filePath) throws SecurityException, IOException
	{
		Objects.requireNonNull(filePath, "filePath must not be null");

		// Sanitize path first
		Path sanitized = pathSanitizer.sanitize(filePath);

		// Validate file
		fileValidator.validateFile(sanitized);

		return sanitized;
	}

	/**
	 * Validates multiple files against all security constraints.
	 *
	 * <p>This is a convenience method that calls {@link #validateFile(Path)}
	 * for each file in the list.
	 *
	 * @param filePaths the files to validate, must not be {@code null}
	 * @return list of sanitized file paths in same order
	 * @throws SecurityException if any validation fails
	 * @throws IOException if file metadata cannot be accessed
	 * @throws NullPointerException if filePaths is {@code null} or contains {@code null}
	 */
	public List<Path> validateFiles(List<Path> filePaths) throws SecurityException, IOException
	{
		Objects.requireNonNull(filePaths, "filePaths must not be null");

		return filePaths.stream().
			map(path ->
			{
				try
				{
					return validateFile(path);
				}
				catch (IOException e)
				{
					throw new SecurityException("File validation failed: " + path, e);
				}
			}).
			toList();
	}

	/**
	 * Sanitizes and validates a file path without checking file properties.
	 *
	 * <p>Use this when you need path validation but the file may not exist yet.
	 *
	 * @param filePath the path to sanitize, must not be {@code null}
	 * @return the sanitized absolute path
	 * @throws SecurityException if path contains suspicious patterns
	 * @throws NullPointerException if filePath is {@code null}
	 */
	public Path sanitizePath(Path filePath) throws SecurityException
	{
		Objects.requireNonNull(filePath, "filePath must not be null");
		return pathSanitizer.sanitize(filePath);
	}

	/**
	 * Checks current memory usage against configured limits.
	 *
	 * <p>Call this method periodically during batch processing (e.g., every 100 files)
	 * to prevent memory exhaustion.
	 *
	 * @throws io.github.cowwoc.styler.cli.security.exceptions.MemoryLimitExceededException
	 *     if memory usage exceeds limit
	 */
	public void checkMemoryLimit()
	{
		memoryMonitor.checkMemoryLimit();
	}

	/**
	 * Executes an operation with timeout enforcement.
	 *
	 * <p>If the operation completes within the timeout, its result is returned.
	 * If it exceeds the timeout, the operation is interrupted and an exception is thrown.
	 *
	 * @param <T> the result type
	 * @param operation the operation to execute, must not be {@code null}
	 * @param operationName descriptive name for error messages, must not be {@code null}
	 * @return the operation result
	 * @throws io.github.cowwoc.styler.cli.security.exceptions.ExecutionTimeoutException
	 *     if operation exceeds timeout
	 * @throws RuntimeException if operation throws an exception
	 * @throws NullPointerException if operation or operationName is {@code null}
	 */
	public <T> T executeWithTimeout(Callable<T> operation, String operationName)
	{
		Objects.requireNonNull(operation, "operation must not be null");
		Objects.requireNonNull(operationName, "operationName must not be null");

		return timeoutManager.executeWithTimeout(operation, operationName);
	}

	/**
	 * Starts resource monitoring for the current thread.
	 *
	 * <p>This sets up a {@link ResourceContext} for tracking operation metrics.
	 * Always call {@link #stopResourceMonitoring()} in a finally block.
	 *
	 * @param operationName descriptive name for this operation, must not be {@code null}
	 * @throws NullPointerException if operationName is {@code null}
	 */
	public void startResourceMonitoring(String operationName)
	{
		Objects.requireNonNull(operationName, "operationName must not be null");
		ResourceContext.set(new ResourceContext(operationName));
	}

	/**
	 * Stops resource monitoring for the current thread.
	 *
	 * <p>Always call this in a finally block to prevent memory leaks.
	 */
	public void stopResourceMonitoring()
	{
		ResourceContext.clear();
	}

	/**
	 * Returns the current memory usage percentage.
	 *
	 * @return memory usage as percentage of configured limit ({@code 0}.{@code 0} to 100.{@code 0}+)
	 */
	public double getMemoryUsagePercentage()
	{
		return memoryMonitor.getMemoryUsagePercentage();
	}

	/**
	 * Enters a new recursion level with the specified location context.
	 *
	 * <p>Every call to this method must have a matching {@link #exitRecursion()}
	 * in a finally block.
	 *
	 * @param location the location context (e.g., "ClassDeclaration", "MethodCall")
	 * @throws io.github.cowwoc.styler.cli.security.exceptions.RecursionDepthExceededException
	 *     if depth exceeds configured limit
	 * @throws NullPointerException if location is {@code null}
	 */
	public void enterRecursion(String location)
	{
		Objects.requireNonNull(location, "location must not be null");
		recursionDepthTracker.enter(location);
	}

	/**
	 * Exits the current recursion level.
	 *
	 * <p>This method must be called in a finally block to ensure depth
	 * tracking remains consistent even if exceptions occur.
	 *
	 * @throws IllegalStateException if called without matching {@link #enterRecursion(String)}
	 */
	public void exitRecursion()
	{
		recursionDepthTracker.exit();
	}

	/**
	 * Returns the current recursion depth for the calling thread.
	 *
	 * @return current recursion depth ({@code 0} if not inside recursive traversal)
	 */
	public int getCurrentRecursionDepth()
	{
		return recursionDepthTracker.getCurrentDepth();
	}

	/**
	 * Resets the recursion depth tracker for the calling thread.
	 *
	 * <p>This should be called after completing a file processing operation.
	 */
	public void resetRecursionDepth()
	{
		recursionDepthTracker.reset();
	}

	/**
	 * Creates a new temporary file with the specified prefix and suffix.
	 *
	 * <p>The created file is automatically tracked and will be deleted during cleanup.
	 *
	 * @param prefix the file name prefix
	 * @param suffix the file name suffix (e.g., ".java")
	 * @return path to the created temporary file
	 * @throws io.github.cowwoc.styler.cli.security.exceptions.TempFileLimitExceededException
	 *     if file count or disk usage limit exceeded
	 * @throws IOException if file creation fails
	 * @throws NullPointerException if prefix or suffix is {@code null}
	 */
	public Path createTempFile(String prefix, String suffix) throws IOException
	{
		Objects.requireNonNull(prefix, "prefix must not be null");
		Objects.requireNonNull(suffix, "suffix must not be null");
		return tempFileManager.createTempFile(prefix, suffix);
	}

	/**
	 * Updates the disk usage tracking for an existing temporary file.
	 *
	 * <p>This should be called after writing to a tracked temporary file to
	 * ensure disk usage limits remain accurate.
	 *
	 * @param tempFile the temporary file that was modified
	 * @throws io.github.cowwoc.styler.cli.security.exceptions.TempFileLimitExceededException
	 *     if new disk usage exceeds configured limit
	 * @throws IOException if file size cannot be determined
	 * @throws IllegalArgumentException if file is not tracked
	 * @throws NullPointerException if tempFile is {@code null}
	 */
	public void updateTempFileSize(Path tempFile) throws IOException
	{
		Objects.requireNonNull(tempFile, "tempFile must not be null");
		tempFileManager.updateFileSize(tempFile);
	}

	/**
	 * Returns the current number of tracked temporary files.
	 *
	 * @return current file count
	 */
	public int getCurrentTempFileCount()
	{
		return tempFileManager.getCurrentFileCount();
	}

	/**
	 * Returns the current disk usage of tracked temporary files.
	 *
	 * @return current disk usage in bytes
	 */
	public long getCurrentTempDiskUsage()
	{
		return tempFileManager.getCurrentDiskUsage();
	}

	/**
	 * Cleans up all tracked temporary files.
	 *
	 * <p>This method is idempotent and automatically called on JVM shutdown.
	 */
	public void cleanupTempFiles()
	{
		tempFileManager.cleanup();
	}
}