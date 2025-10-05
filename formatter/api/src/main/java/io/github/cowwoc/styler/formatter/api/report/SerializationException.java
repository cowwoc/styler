package io.github.cowwoc.styler.formatter.api.report;

/**
 * Thrown when violation report serialization or deserialization fails.
 * <p>
 * This exception is thrown by {@link ViolationSerializer} implementations when they cannot
 * serialize a report to a specific format or cannot deserialize content back to a report.
 * Common causes include invalid JSON/XML structure, encoding issues, or unsupported data types.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * try {
 *     String json = serializer.serialize(report);
 * } catch (SerializationException e) {
 *     logger.error("Failed to serialize report", e);
 * }
 * }</pre>
 *
 * @see ViolationSerializer
 */
public final class SerializationException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new serialization exception with the specified message.
	 *
	 * @param message the error message, may be {@code null}
	 */
	public SerializationException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new serialization exception with the specified message and cause.
	 * <p>
	 * This constructor should be used when wrapping lower-level exceptions (e.g.,
	 * {@code JsonProcessingException}, {@code IOException}) to preserve the full error context.
	 * </p>
	 *
	 * @param message the error message, may be {@code null}
	 * @param cause   the underlying cause, may be {@code null}
	 */
	public SerializationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Creates a new serialization exception with the specified cause.
	 * <p>
	 * The message is initialized to {@code cause.toString()} if cause is non-null.
	 * </p>
	 *
	 * @param cause the underlying cause, may be {@code null}
	 */
	public SerializationException(Throwable cause)
	{
		super(cause);
	}
}
