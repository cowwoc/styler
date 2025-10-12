package io.github.cowwoc.styler.security.exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when a file exceeds the maximum allowed size.
 * <p>
 * This exception provides context about the file, its actual size, and the configured limit
 * to enable actionable error handling.
 */
public class FileSizeLimitExceededException extends SecurityException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception indicating file size limit violation.
	 *
	 * @param file       path to the oversized file
	 * @param actualSize actual file size in bytes
	 * @param limitSize  configured maximum size in bytes
	 */
	public FileSizeLimitExceededException(Path file, long actualSize, long limitSize)
	{
		super(String.format(
			"File '%s' size (%d bytes) exceeds limit (%d bytes). " +
			"Consider splitting large files or increasing maxFileSizeBytes configuration.",
			file, actualSize, limitSize));
	}
}
