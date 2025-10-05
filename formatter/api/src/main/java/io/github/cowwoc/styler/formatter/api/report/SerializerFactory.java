package io.github.cowwoc.styler.formatter.api.report;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Factory for creating {@link ViolationSerializer} instances based on format.
 * <p>
 * Provides thread-safe singleton instances of serializers for each supported format.
 * All serializers are stateless and can be safely shared across multiple threads.
 * </p>
 * <p>
 * This factory is immutable and thread-safe.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ViolationSerializer serializer = SerializerFactory.create(SerializationFormat.JSON);
 * String json = serializer.serialize(report);
 * }</pre>
 *
 * @see ViolationSerializer
 * @see SerializationFormat
 */
public final class SerializerFactory
{
	private static final JsonViolationSerializer JSON_SERIALIZER = new JsonViolationSerializer();
	private static final XmlViolationSerializer XML_SERIALIZER = new XmlViolationSerializer();

	/**
	 * Private constructor to prevent instantiation.
	 * <p>
	 * This is a utility class with only static methods.
	 * </p>
	 */
	private SerializerFactory()
	{
	}

	/**
	 * Creates a violation serializer for the specified format.
	 * <p>
	 * Returns a singleton instance for the requested format. All serializers are stateless
	 * and thread-safe, so sharing instances across threads is safe and efficient.
	 * </p>
	 *
	 * @param format the serialization format, must not be {@code null}
	 * @return the serializer for the specified format, never {@code null}
	 * @throws NullPointerException if {@code format} is {@code null}
	 */
	public static ViolationSerializer create(SerializationFormat format)
	{
		requireThat(format, "format").isNotNull();

		return switch (format)
		{
			case JSON -> JSON_SERIALIZER;
			case XML -> XML_SERIALIZER;
		};
	}
}
