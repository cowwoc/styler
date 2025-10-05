package io.github.cowwoc.styler.cli.security;

import io.github.cowwoc.styler.cli.security.exceptions.FileSizeExceededException;
import io.github.cowwoc.styler.cli.security.exceptions.FileTypeNotAllowedException;
import io.github.cowwoc.styler.cli.security.exceptions.SecurityException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

/**
 * Validates Java source files against security constraints including file size limits,
 * extension whitelist, and readability checks.
 *
 * <p>This validator is thread-safe and stateless (after construction), allowing concurrent
 * validation of multiple files in parallel processing scenarios.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * SecurityConfig config = SecurityConfig.defaults();
 * FileValidator validator = new FileValidator(
 *     config.maxFileSizeBytes(),
 *     config.allowedExtensions()
 * );
 *
 * try {
 *     validator.validateFile(Paths.get("MyClass.java"));
 *     // File is safe to process
 * } catch (FileSizeExceededException e) {
 *     System.err.println("File too large: " + e.getMessage());
 * } catch (FileTypeNotAllowedException e) {
 *     System.err.println("File type not supported: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see SecurityConfig
 * @see FileSizeExceededException
 * @see FileTypeNotAllowedException
 */
public final class FileValidator
{
	private final long maxFileSizeBytes;
	private final Set<String> allowedExtensions;

	/**
	 * Constructs a new file validator with the specified limits.
	 *
	 * @param maxFileSizeBytes maximum file size in bytes, must be positive
	 * @param allowedExtensions set of allowed file extensions (e.g., ".java"), must not be {@code null} or empty
	 * @throws IllegalArgumentException if maxFileSizeBytes is not positive
	 * @throws NullPointerException if allowedExtensions is {@code null}
	 * @throws IllegalArgumentException if allowedExtensions is empty
	 */
	public FileValidator(long maxFileSizeBytes, Set<String> allowedExtensions)
	{
		if (maxFileSizeBytes <= 0)
		{
			throw new IllegalArgumentException(
				"maxFileSizeBytes must be positive: " + maxFileSizeBytes);
		}
		Objects.requireNonNull(allowedExtensions, "allowedExtensions must not be null");
		if (allowedExtensions.isEmpty())
		{
			throw new IllegalArgumentException("allowedExtensions must not be empty");
		}

		this.maxFileSizeBytes = maxFileSizeBytes;
		this.allowedExtensions = Set.copyOf(allowedExtensions);
	}

	/**
	 * Validates that the specified file meets all security requirements.
	 *
	 * <p>Checks performed:
	 * <ul>
	 *   <li>File exists and is readable</li>
	 *   <li>File size is within configured limit</li>
	 *   <li>File extension is in allowed list</li>
	 *   <li>File is a regular file (not directory or special file)</li>
	 * </ul>
	 *
	 * @param filePath the path to validate, must not be {@code null}
	 * @throws SecurityException if file does not exist or is not readable
	 * @throws FileSizeExceededException if file exceeds size limit
	 * @throws FileTypeNotAllowedException if file extension not allowed
	 * @throws IOException if file metadata cannot be accessed
	 * @throws NullPointerException if filePath is {@code null}
	 */
	public void validateFile(Path filePath) throws IOException
	{
		Objects.requireNonNull(filePath, "filePath must not be null");

		validateFileExists(filePath);
		validateFileSize(filePath);
		validateFileExtension(filePath);
		validateRegularFile(filePath);
	}

	/**
	 * Validates that the file exists and is readable.
	 *
	 * @param filePath the file to check
	 * @throws SecurityException if file does not exist or is not readable
	 */
	private void validateFileExists(Path filePath)
	{
		if (!Files.exists(filePath))
		{
			throw new SecurityException("File does not exist: " + filePath);
		}

		if (!Files.isReadable(filePath))
		{
			throw new SecurityException("File is not readable: " + filePath);
		}
	}

	/**
	 * Validates that the file size is within the configured limit.
	 *
	 * @param filePath the file to check
	 * @throws FileSizeExceededException if file exceeds size limit
	 * @throws IOException if file size cannot be determined
	 */
	private void validateFileSize(Path filePath) throws IOException
	{
		long size = Files.size(filePath);
		if (size > maxFileSizeBytes)
		{
			throw new FileSizeExceededException(filePath, size, maxFileSizeBytes);
		}
	}

	/**
	 * Validates that the file has an allowed extension.
	 *
	 * @param filePath the file to check
	 * @throws FileTypeNotAllowedException if file extension is not allowed
	 */
	private void validateFileExtension(Path filePath)
	{
		String fileName = filePath.getFileName().toString();
		String extension = getFileExtension(fileName);

		if (!allowedExtensions.contains(extension))
		{
			throw new FileTypeNotAllowedException(filePath, extension, allowedExtensions);
		}
	}

	/**
	 * Validates that the file is a regular file (not directory or special file).
	 *
	 * @param filePath the file to check
	 * @throws SecurityException if file is not a regular file
	 */
	private void validateRegularFile(Path filePath)
	{
		if (!Files.isRegularFile(filePath))
		{
			throw new SecurityException("Not a regular file: " + filePath);
		}
	}

	/**
	 * Extracts the file extension from a filename (including the dot).
	 *
	 * @param fileName the filename to analyze
	 * @return the extension (e.g., ".java"), or empty string if no extension
	 */
	private String getFileExtension(String fileName)
	{
		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1)
		{
			return "";
		}
		return fileName.substring(lastDotIndex);
	}
}