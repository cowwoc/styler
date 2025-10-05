package io.github.cowwoc.styler.formatter.api.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * JSON serializer for violation reports.
 * <p>
 * Serializes and deserializes violation reports to/from JSON format using Jackson. The output
 * is formatted with pretty-printing enabled for human readability. Timestamps are represented
 * as milliseconds since Unix epoch (not ISO-8601 strings).
 * </p>
 * <p>
 * This class is thread-safe and stateless. The Jackson {@link ObjectMapper} instance is
 * configured once during class initialization and shared across all serialization operations.
 * Multiple threads can safely use the same {@code JsonViolationSerializer} instance concurrently.
 * </p>
 * <h2>JSON Output Format</h2>
 * <pre>{@code
 * {
 *   "violations" : [ ... ],
 *   "statistics" : {
 *     "totalViolations" : 10,
 *     "severityCounts" : { ... },
 *     "ruleIdCounts" : { ... }
 *   },
 *   "timestampMillis" : 1696435200000
 * }
 * }</pre>
 * <h2>Thread Safety</h2>
 * <p>
 * This implementation is stateless and thread-safe. The {@link ObjectMapper} is immutable after
 * construction and can be safely shared across threads.
 * </p>
 *
 * @see ViolationSerializer
 * @see ViolationReport
 */
public final class JsonViolationSerializer implements ViolationSerializer
{
	private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

	/**
	 * Creates and configures the Jackson ObjectMapper.
	 * <p>
	 * Configuration:
	 * </p>
	 * <ul>
	 * <li>Pretty printing enabled for human readability</li>
	 * <li>Timestamps as milliseconds (not ISO-8601 strings)</li>
	 * <li>Null values omitted from output</li>
	 * <li>Auto-discover and register modules (enables Java record support)</li>
	 * </ul>
	 *
	 * @return the configured ObjectMapper, never {@code null}
	 */
	private static ObjectMapper createObjectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();  // Enable Java record support
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}

	/**
	 * Serializes a violation report to JSON format.
	 * <p>
	 * Produces pretty-printed JSON with 2-space indentation. All required fields are included.
	 * Optional fields (like {@code fixSuggestion}) are omitted if {@code null}.
	 * </p>
	 *
	 * @param report the report to serialize, must not be {@code null}
	 * @return the JSON string representation, never {@code null}
	 * @throws SerializationException if JSON generation fails
	 * @throws NullPointerException   if {@code report} is {@code null}
	 */
	@Override
	public String serialize(ViolationReport report) throws SerializationException
	{
		requireThat(report, "report").isNotNull();

		try
		{
			return OBJECT_MAPPER.writeValueAsString(report);
		}
		catch (JsonProcessingException e)
		{
			throw new SerializationException("Failed to serialize violation report to JSON", e);
		}
	}

	/**
	 * Deserializes JSON content back to a violation report.
	 * <p>
	 * The JSON must match the structure produced by {@link #serialize(ViolationReport)}.
	 * All required fields must be present. Missing or invalid fields will cause deserialization
	 * to fail.
	 * </p>
	 * <p>
	 * <strong>Round-Trip Guarantee:</strong> For any report {@code r}:
	 * </p>
	 * <pre>{@code
	 * String json = serialize(r);
	 * ViolationReport restored = deserialize(json);
	 * assert r.equals(restored);
	 * }</pre>
	 *
	 * @param content the JSON content, must not be {@code null} or blank
	 * @return the deserialized violation report, never {@code null}
	 * @throws SerializationException   if JSON parsing fails or content is invalid
	 * @throws NullPointerException     if {@code content} is {@code null}
	 * @throws IllegalArgumentException if {@code content} is blank
	 */
	@Override
	public ViolationReport deserialize(String content) throws SerializationException
	{
		requireThat(content, "content").isNotBlank();

		try
		{
			return OBJECT_MAPPER.readValue(content, ViolationReport.class);
		}
		catch (JsonProcessingException e)
		{
			throw new SerializationException("Failed to deserialize JSON to violation report", e);
		}
	}
}
