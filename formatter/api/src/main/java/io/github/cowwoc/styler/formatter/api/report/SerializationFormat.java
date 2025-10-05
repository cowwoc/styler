package io.github.cowwoc.styler.formatter.api.report;

import java.util.Locale;

/**
 * Supported serialization formats for violation reports.
 * <p>
 * Defines the output formats available for structured violation reports. Each format has
 * associated file extensions and MIME types for proper content handling.
 * </p>
 * <h2>Format Characteristics</h2>
 * <ul>
 * <li><strong>JSON</strong>: Lightweight, widely supported, best for programmatic consumption</li>
 * <li><strong>XML</strong>: Structured, self-describing, best for enterprise integration</li>
 * </ul>
 *
 * @see ViolationSerializer
 */
public enum SerializationFormat
{
	/**
	 * JavaScript Object Notation (JSON) format.
	 * <p>
	 * Produces compact, human-readable output with standard JSON conventions.
	 * File extension: {@code .json}, MIME type: {@code application/json}.
	 * </p>
	 */
	JSON("json", "application/json"),

	/**
	 * Extensible Markup Language (XML) format.
	 * <p>
	 * Produces structured, self-describing output with XML schema support.
	 * File extension: {@code .xml}, MIME type: {@code application/xml}.
	 * </p>
	 */
	XML("xml", "application/xml");

	private final String fileExtension;
	private final String mimeType;

	/**
	 * Creates a serialization format with the specified properties.
	 *
	 * @param fileExtension the file extension without leading dot, never {@code null}
	 * @param mimeType      the MIME type for this format, never {@code null}
	 */
	SerializationFormat(String fileExtension, String mimeType)
	{
		this.fileExtension = fileExtension;
		this.mimeType = mimeType;
	}

	/**
	 * Returns the file extension for this format.
	 * <p>
	 * The extension is returned without a leading dot (e.g., "json", not ".json").
	 * </p>
	 *
	 * @return the file extension, never {@code null}
	 */
	public String fileExtension()
	{
		return fileExtension;
	}

	/**
	 * Returns the MIME type for this format.
	 * <p>
	 * MIME types follow standard conventions: {@code application/json}, {@code application/xml}.
	 * </p>
	 *
	 * @return the MIME type, never {@code null}
	 */
	public String mimeType()
	{
		return mimeType;
	}

	/**
	 * Returns the format name in lowercase.
	 * <p>
	 * Equivalent to {@code name().toLowerCase()}.
	 * </p>
	 *
	 * @return the lowercase format name, never {@code null}
	 */
	public String formatName()
	{
		return name().toLowerCase(Locale.ROOT);
	}
}
