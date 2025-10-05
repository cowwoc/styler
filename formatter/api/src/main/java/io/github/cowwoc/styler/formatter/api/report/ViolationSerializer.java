package io.github.cowwoc.styler.formatter.api.report;

/**
 * Strategy interface for serializing and deserializing violation reports.
 * <p>
 * Implementations provide format-specific serialization (JSON, XML, etc.) for violation reports.
 * All implementations must be stateless and thread-safe to support concurrent usage in the
 * multi-threaded formatter engine.
 * </p>
 * <p>
 * Implementations must guarantee round-trip fidelity: deserializing a serialized report must
 * produce a report equal to the original (as defined by {@link ViolationReport#equals(Object)}).
 * </p>
 * <h2>Thread Safety</h2>
 * <p>
 * All implementations MUST be thread-safe. Multiple threads may use the same serializer instance
 * concurrently to serialize different reports. Implementations should be stateless (zero mutable
 * instance fields) to ensure thread safety.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ViolationSerializer serializer = new JsonViolationSerializer();
 * String json = serializer.serialize(report);
 * ViolationReport restored = serializer.deserialize(json);
 * assert report.equals(restored); // Round-trip fidelity
 * }</pre>
 *
 * @see ViolationReport
 * @see SerializationException
 */
public interface ViolationSerializer
{
	/**
	 * Serializes a violation report to a string representation.
	 * <p>
	 * The format of the output depends on the implementation (JSON, XML, etc.). The output
	 * should be human-readable when possible and must be deserializable back to an equivalent
	 * report using {@link #deserialize(String)}.
	 * </p>
	 *
	 * @param report the report to serialize, must not be {@code null}
	 * @return the serialized string representation, never {@code null}
	 * @throws SerializationException if serialization fails (e.g., encoding issues, invalid data)
	 * @throws NullPointerException   if {@code report} is {@code null}
	 */
	String serialize(ViolationReport report) throws SerializationException;

	/**
	 * Deserializes a string representation back to a violation report.
	 * <p>
	 * The input must be in the same format produced by {@link #serialize(ViolationReport)}.
	 * Deserialization must preserve all report data including violations, statistics, and
	 * timestamp.
	 * </p>
	 * <p>
	 * <strong>Round-Trip Guarantee:</strong> For any report {@code r}, the following must be true:
	 * </p>
	 * <pre>{@code
	 * ViolationReport r = ...;
	 * String serialized = serialize(r);
	 * ViolationReport deserialized = deserialize(serialized);
	 * assert r.equals(deserialized);
	 * }</pre>
	 *
	 * @param content the serialized content, must not be {@code null} or blank
	 * @return the deserialized violation report, never {@code null}
	 * @throws SerializationException if deserialization fails (e.g., invalid format, corrupted data)
	 * @throws NullPointerException   if {@code content} is {@code null}
	 * @throws IllegalArgumentException if {@code content} is blank
	 */
	ViolationReport deserialize(String content) throws SerializationException;
}
