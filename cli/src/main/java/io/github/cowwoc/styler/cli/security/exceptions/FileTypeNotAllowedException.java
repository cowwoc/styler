package io.github.cowwoc.styler.cli.security.exceptions;

import java.io.Serial;
import java.nio.file.Path;
import java.util.Set;
/**
 * Thrown when a file has an extension that is not in the allowed list.
 *
 * <p>This prevents accidental processing of binary files or unsupported file types.
 *
 * <h2>Example Error Message:</h2>
 * <pre>
 * File type not allowed
 *   File: /path/to/file.class
 *   Extension: .class
 *   Allowed: [.java]
 *
 *   Binary and executable files are not supported.
 *   Only Java source files (.java) can be formatted.
 * </pre>
 */
public final class FileTypeNotAllowedException extends SecurityException
{
	@Serial
	private static final long serialVersionUID = 1L;
	private final transient Path filePath;
	private final String extension;
	private final transient Set<String> allowedExtensions;

	/**
	 * Constructs a new file type not allowed exception.
	 *
	 * @param filePath the path to the file with disallowed extension
	 * @param extension the actual {@code extension} of the file
	 * @param allowedExtensions the set of allowed extensions
	 */
	public FileTypeNotAllowedException(Path filePath, String extension,
		Set<String> allowedExtensions)
	{
		super(formatMessage(filePath, extension, allowedExtensions));
		this.filePath = filePath;
		this.extension = extension;
		this.allowedExtensions = Set.copyOf(allowedExtensions);
	}

	private static String formatMessage(Path filePath, String extension,
		Set<String> allowedExtensions)
	{
		String extensionDisplay;
		if (extension.isEmpty())
			{
			extensionDisplay = "(none)";
			}
		else
			{
			extensionDisplay = extension;
			}
		return String.format(
			"File type not allowed%n" +
			"  File: %s%n" +
			"  Extension: %s%n" +
			"  Allowed: %s%n" +
			"%n" +
			"  Only Java source files can be formatted.%n" +
			"  Binary and executable files are not supported.",
			filePath,
			extensionDisplay,
			allowedExtensions);
	}

	/**
	 * Returns the path to the file with disallowed extension.
	 *
	 * @return the file path
	 */
	public Path filePath()
	{
		return filePath;
	}

	/**
	 * Returns the actual extension of the file.
	 *
	 * @return the file extension (may be empty if file has no extension)
	 */
	public String extension()
	{
		return extension;
	}

	/**
	 * Returns the set of allowed file extensions.
	 *
	 * @return unmodifiable set of allowed extensions
	 */
	public Set<String> allowedExtensions()
	{
		return allowedExtensions;
	}
}