package io.github.cowwoc.styler.formatter.api.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * XML serializer for violation reports.
 * <p>
 * Serializes and deserializes violation reports to/from XML format using Jackson XML. The output
 * is formatted with pretty-printing enabled for human readability. The root element is named
 * {@code <ViolationReport>} and follows standard XML conventions.
 * </p>
 * <p>
 * This class is thread-safe and stateless. The Jackson {@link XmlMapper} instance is configured
 * once during class initialization and shared across all serialization operations. Multiple threads
 * can safely use the same {@code XmlViolationSerializer} instance concurrently.
 * </p>
 * <h2>XML Output Format</h2>
 * <pre>
 * &lt;ViolationReport&gt;
 *   &lt;violations&gt;
 *     &lt;ViolationEntry&gt;...&lt;/ViolationEntry&gt;
 *   &lt;/violations&gt;
 *   &lt;statistics&gt;
 *     &lt;totalViolations&gt;10&lt;/totalViolations&gt;
 *     &lt;severityCounts&gt;...&lt;/severityCounts&gt;
 *     &lt;ruleIdCounts&gt;...&lt;/ruleIdCounts&gt;
 *   &lt;/statistics&gt;
 *   &lt;timestampMillis&gt;1696435200000&lt;/timestampMillis&gt;
 * &lt;/ViolationReport&gt;
 * </pre>
 * <h2>Thread Safety</h2>
 * <p>
 * This implementation is stateless and thread-safe. The {@link XmlMapper} is immutable after
 * construction and can be safely shared across threads.
 * </p>
 *
 * @see ViolationSerializer
 * @see ViolationReport
 */
public final class XmlViolationSerializer implements ViolationSerializer
{
	private static final XmlMapper XML_MAPPER = createXmlMapper();

	/**
	 * Creates and configures the Jackson XmlMapper.
	 * <p>
	 * Configuration:
	 * </p>
	 * <ul>
	 * <li>Pretty printing enabled for human readability</li>
	 * <li>Standard XML formatting with proper indentation</li>
	 * </ul>
	 *
	 * @return the configured XmlMapper, never {@code null}
	 */
	private static XmlMapper createXmlMapper()
	{
		XmlMapper mapper = new XmlMapper();
		mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
		return mapper;
	}

	/**
	 * Serializes a violation report to XML format.
	 * <p>
	 * Produces pretty-printed XML with 2-space indentation. All required fields are included.
	 * Optional fields (like {@code fixSuggestion}) are omitted if {@code null}.
	 * </p>
	 *
	 * @param report the report to serialize, must not be {@code null}
	 * @return the XML string representation, never {@code null}
	 * @throws SerializationException if XML generation fails
	 * @throws NullPointerException   if {@code report} is {@code null}
	 */
	@Override
	public String serialize(ViolationReport report) throws SerializationException
	{
		requireThat(report, "report").isNotNull();

		try
		{
			return XML_MAPPER.writeValueAsString(report);
		}
		catch (JsonProcessingException e)
		{
			throw new SerializationException("Failed to serialize violation report to XML", e);
		}
	}

	/**
	 * Deserializes XML content back to a violation report.
	 * <p>
	 * The XML must match the structure produced by {@link #serialize(ViolationReport)}.
	 * All required fields must be present. Missing or invalid fields will cause deserialization
	 * to fail.
	 * </p>
	 * <p>
	 * <strong>Round-Trip Guarantee:</strong> For any report {@code r}:
	 * </p>
	 * <pre>{@code
	 * String xml = serialize(r);
	 * ViolationReport restored = deserialize(xml);
	 * assert r.equals(restored);
	 * }</pre>
	 *
	 * @param content the XML content, must not be {@code null} or blank
	 * @return the deserialized violation report, never {@code null}
	 * @throws SerializationException   if XML parsing fails or content is invalid
	 * @throws NullPointerException     if {@code content} is {@code null}
	 * @throws IllegalArgumentException if {@code content} is blank
	 */
	@Override
	public ViolationReport deserialize(String content) throws SerializationException
	{
		requireThat(content, "content").isNotBlank();

		try
		{
			return XML_MAPPER.readValue(content, ViolationReport.class);
		}
		catch (JsonProcessingException e)
		{
			throw new SerializationException("Failed to deserialize XML to violation report", e);
		}
	}
}
