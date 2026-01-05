package io.github.cowwoc.styler.security;

import io.github.cowwoc.styler.security.exceptions.FileSizeLimitExceededException;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Validates files against security policies before processing.
 * <p>
 * This validator ensures files meet size, type, and accessibility requirements
 * to prevent resource exhaustion and malicious input exploitation.
 * Thread-safe and stateless.
 */
public final class FileValidator
{
	private static final String JAVA_EXTENSION = ".java";

	/**
	 * Validates a file against all security policies.
	 *
	 * @param file   file to validate
	 * @param config security configuration defining limits
	 * @throws FileSizeLimitExceededException if file exceeds size limit
	 * @throws IllegalArgumentException       if file doesn't exist, isn't readable, or wrong type
	 * @throws IOException                    if file attributes cannot be read
	 */
	public void validate(Path file, SecurityConfig config)
		throws FileSizeLimitExceededException, IOException
	{
		requireThat(file, "file").isNotNull();
		requireThat(config, "config").isNotNull();

		validateExists(file);
		validateReadable(file);
		validateFileType(file);
		validateFileSize(file, config);
	}

	/**
	 * Validates that the file exists.
	 *
	 * @param file file to check
	 * @throws IllegalArgumentException if file doesn't exist
	 */
	private void validateExists(Path file)
	{
		if (!Files.exists(file))
			throw new IllegalArgumentException("File does not exist: " + file);
		if (!Files.isRegularFile(file))
			throw new IllegalArgumentException("Path is not a regular file: " + file);
	}

	/**
	 * Validates that the file is readable.
	 *
	 * @param file file to check
	 * @throws IllegalArgumentException if file isn't readable
	 */
	private void validateReadable(Path file)
	{
		if (!Files.isReadable(file))
			throw new IllegalArgumentException("File is not readable: " + file);
	}

	/**
	 * Validates that the file has the correct extension.
	 *
	 * @param file file to check
	 * @throws IllegalArgumentException if file doesn't have .java extension
	 */
	private void validateFileType(Path file)
	{
		String fileName = file.getFileName().toString();
		if (!fileName.endsWith(JAVA_EXTENSION))
			throw new IllegalArgumentException(
				String.format("File must have %s extension: %s", JAVA_EXTENSION, file));
	}

	/**
	 * Validates that the file size doesn't exceed the configured limit.
	 *
	 * @param file   file to check
	 * @param config security configuration
	 * @throws FileSizeLimitExceededException if file is too large
	 * @throws IOException                    if file size cannot be determined
	 */
	private void validateFileSize(Path file, SecurityConfig config)
		throws FileSizeLimitExceededException, IOException
	{
		long fileSize = Files.size(file);
		if (fileSize > config.maxFileSizeBytes())
			throw new FileSizeLimitExceededException(file, fileSize, config.maxFileSizeBytes());
	}
}
