package io.github.cowwoc.styler.cli;

import io.github.cowwoc.styler.cli.security.FileValidator;
import io.github.cowwoc.styler.cli.security.PathSanitizer;
import io.github.cowwoc.styler.cli.security.RecursionDepthTracker;
import io.github.cowwoc.styler.cli.security.exceptions.FileSizeExceededException;
import io.github.cowwoc.styler.cli.security.exceptions.FileTypeNotAllowedException;
import io.github.cowwoc.styler.cli.security.exceptions.PathTraversalException;
import io.github.cowwoc.styler.cli.security.exceptions.RecursionDepthExceededException;
import io.github.cowwoc.styler.cli.security.exceptions.SecurityException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Discovers Java source files with configurable filtering and security controls.
 *
 * <p>This service provides recursive directory traversal with:
 * <ul>
 *   <li>Path sanitization to prevent traversal attacks</li>
 *   <li>File size and type validation</li>
 *   <li>Recursion depth limiting</li>
 *   <li>Fail-safe error handling (warnings for permission errors)</li>
 *   <li>Resource limits (max files, max depth)</li>
 * </ul>
 *
 * <p>This class is thread-safe and stateless. Discovery operations can be executed
 * concurrently from multiple threads.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * SecurityConfig config = SecurityConfig.defaults();
 * PathSanitizer sanitizer = new PathSanitizer();
 * FileValidator validator = new FileValidator(
 *     config.maxFileSizeBytes(),
 *     config.allowedExtensions()
 * );
 * RecursionDepthTracker depthTracker = new RecursionDepthTracker(
 *     config.maxRecursionDepth(),
 *     config.warnRecursionDepth()
 * );
 *
 * FileDiscovery discovery = new FileDiscovery(sanitizer, validator, depthTracker);
 * DiscoveryResult result = discovery.discover(List.of(Paths.get("src")));
 *
 * System.out.println("Found " + result.fileCount() + " files");
 * }</pre>
 *
 * @see DiscoveryResult
 * @see PathSanitizer
 * @see FileValidator
 * @see RecursionDepthTracker
 */
public final class FileDiscovery
{
	/** Maximum number of files to discover (prevents resource exhaustion). */
	private static final int MAX_FILES = 100_000;

	private final PathSanitizer pathSanitizer;
	private final FileValidator fileValidator;
	private final RecursionDepthTracker depthTracker;

	/**
	 * Constructs a new file discovery service.
	 *
	 * @param pathSanitizer sanitizer for path validation, must not be {@code null}
	 * @param fileValidator validator for file security checks, must not be {@code null}
	 * @param depthTracker tracker for recursion depth limiting, must not be {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public FileDiscovery(PathSanitizer pathSanitizer, FileValidator fileValidator,
		RecursionDepthTracker depthTracker)
	{
		this.pathSanitizer = Objects.requireNonNull(pathSanitizer,
			"pathSanitizer must not be null");
		this.fileValidator = Objects.requireNonNull(fileValidator,
			"fileValidator must not be null");
		this.depthTracker = Objects.requireNonNull(depthTracker,
			"depthTracker must not be null");
	}

	/**
	 * Discovers Java source files from the specified paths.
	 *
	 * <p>For each path:
	 * <ul>
	 *   <li>If a regular file: validates and includes if it matches criteria</li>
	 *   <li>If a directory: recursively traverses and discovers files</li>
	 * </ul>
	 *
	 * <p>Security controls applied:
	 * <ul>
	 *   <li>Path sanitization (prevents traversal attacks)</li>
	 *   <li>File validation (size, extension, readability)</li>
	 *   <li>Recursion depth limiting (prevents stack overflow)</li>
	 *   <li>File count limiting (prevents resource exhaustion)</li>
	 * </ul>
	 *
	 * @param paths the paths to discover files from, must not be {@code null} or empty
	 * @return discovery result containing files and warnings
	 * @throws NullPointerException if paths is {@code null}
	 * @throws IllegalArgumentException if paths is empty
	 * @throws PathTraversalException if any path contains suspicious patterns
	 * @throws RecursionDepthExceededException if recursion depth limit exceeded
	 * @throws SecurityException if file count limit exceeded
	 */
	public DiscoveryResult discover(Collection<Path> paths)
	{
		return discover(paths, null);
	}

	/**
	 * Discovers files from the specified paths using optional filtering.
	 *
	 * <p>For each path:
	 * <ul>
	 *   <li>If a regular file: validates and includes if it matches filter</li>
	 *   <li>If a directory: recursively traverses and discovers files</li>
	 * </ul>
	 *
	 * <p>Security controls applied:
	 * <ul>
	 *   <li>Path sanitization (prevents traversal attacks)</li>
	 *   <li>File validation (size, extension, readability)</li>
	 *   <li>Recursion depth limiting (prevents stack overflow)</li>
	 *   <li>File count limiting (prevents resource exhaustion)</li>
	 * </ul>
	 *
	 * @param paths the paths to discover files from, must not be {@code null} or empty
	 * @param filter optional file filter for include/exclude patterns (null to accept all)
	 * @return discovery result containing files and warnings
	 * @throws NullPointerException if paths is {@code null}
	 * @throws IllegalArgumentException if paths is empty
	 * @throws PathTraversalException if any path contains suspicious patterns
	 * @throws RecursionDepthExceededException if recursion depth limit exceeded
	 * @throws SecurityException if file count limit exceeded
	 */
	public DiscoveryResult discover(Collection<Path> paths, FileFilter filter)
	{
		Objects.requireNonNull(paths, "paths must not be null");
		if (paths.isEmpty())
		{
			throw new IllegalArgumentException("paths must not be empty");
		}

		List<Path> discoveredFiles = new ArrayList<>();
		List<DiscoveryWarning> warnings = new ArrayList<>();
		AtomicInteger fileCount = new AtomicInteger(0);

		try
		{
			for (Path path : paths)
			{
				// Sanitize path to prevent traversal attacks
				Path sanitized = pathSanitizer.sanitize(path);

				if (Files.isRegularFile(sanitized))
				{
					// Single file: validate and add
					processFile(sanitized, filter, discoveredFiles, warnings, fileCount);
				}
				else if (Files.isDirectory(sanitized))
				{
					// Directory: recursive traversal
					traverseDirectory(sanitized, filter, discoveredFiles, warnings, fileCount);
				}
				else
				{
					warnings.add(new DiscoveryWarning(sanitized,
						"Path is neither file nor directory"));
				}
			}
		}
		finally
		{
			// Cleanup thread-local state
			depthTracker.reset();
		}

		return new DiscoveryResult(discoveredFiles, warnings);
	}

	/**
	 * Processes a single file: validates and adds to results if valid.
	 *
	 * @param file the file to process
	 * @param filter optional file filter (null to accept all)
	 * @param discoveredFiles the list to add discovered files to
	 * @param warnings the list to add warnings to
	 * @param fileCount the counter for discovered files
	 */
	private void processFile(Path file, FileFilter filter, List<Path> discoveredFiles,
		List<DiscoveryWarning> warnings, AtomicInteger fileCount)
	{
		try
		{
			// Apply filter if provided
			if (filter != null && !filter.matches(file))
			{
				// File filtered out (not a warning, just doesn't match)
				return;
			}

			// Validate file security constraints
			fileValidator.validateFile(file);

			// Check file count limit
			if (fileCount.incrementAndGet() > MAX_FILES)
			{
				throw new SecurityException(
					"File count limit exceeded: " + MAX_FILES + " files");
			}

			// Add to results
			discoveredFiles.add(file);
		}
		catch (FileTypeNotAllowedException e)
		{
			// Fail-safe: file type filtering is non-fatal
			warnings.add(new DiscoveryWarning(file,
				"File type not allowed: " + e.getMessage()));
		}
		catch (FileSizeExceededException e)
		{
			// Fail-safe: oversized files are skipped with warning
			warnings.add(new DiscoveryWarning(file,
				"File size exceeded: " + e.getMessage()));
		}
		catch (AccessDeniedException e)
		{
			// Fail-safe: permission errors are warnings, not failures
			warnings.add(new DiscoveryWarning(file, "Permission denied"));
		}
		catch (IOException e)
		{
			// Fail-safe: I/O errors are warnings
			warnings.add(new DiscoveryWarning(file, "I/O error: " + e.getMessage()));
		}
		catch (SecurityException e)
		{
			// Fail-fast: security violations are thrown
			throw e;
		}
	}

	/**
	 * Recursively traverses a directory and discovers files.
	 *
	 * @param directory the directory to traverse
	 * @param filter optional file filter (null to accept all)
	 * @param discoveredFiles the list to add discovered files to
	 * @param warnings the list to add warnings to
	 * @param fileCount the counter for discovered files
	 */
	private void traverseDirectory(Path directory, FileFilter filter, List<Path> discoveredFiles,
		List<DiscoveryWarning> warnings, AtomicInteger fileCount)
	{
		try
		{
			Files.walkFileTree(directory, new SimpleFileVisitor<>()
			{
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
				{
					// Early pruning: skip excluded directories
					if (filter != null && filter.shouldExcludeDirectory(dir))
					{
						return FileVisitResult.SKIP_SUBTREE;
					}

					// Track recursion depth
					depthTracker.enter("Directory: " + dir);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				{
					processFile(file, filter, discoveredFiles, warnings, fileCount);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc)
				{
					// Fail-safe: collect warning and continue
					if (exc instanceof AccessDeniedException)
					{
						warnings.add(new DiscoveryWarning(file, "Permission denied"));
					}
					else if (exc instanceof NoSuchFileException)
					{
						warnings.add(new DiscoveryWarning(file, "File not found"));
					}
					else
					{
						warnings.add(new DiscoveryWarning(file,
							"I/O error: " + exc.getMessage()));
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc)
				{
					// Exit recursion depth tracking
					depthTracker.exit();

					if (exc != null)
					{
						warnings.add(new DiscoveryWarning(dir,
							"Error processing directory: " + exc.getMessage()));
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException e)
		{
			warnings.add(new DiscoveryWarning(directory,
				"Failed to traverse directory: " + e.getMessage()));
		}
	}
}
