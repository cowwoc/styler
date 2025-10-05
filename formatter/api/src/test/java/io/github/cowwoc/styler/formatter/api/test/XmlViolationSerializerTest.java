package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import io.github.cowwoc.styler.formatter.api.report.PriorityScore;
import io.github.cowwoc.styler.formatter.api.report.SerializationException;
import io.github.cowwoc.styler.formatter.api.report.ViolationEntry;
import io.github.cowwoc.styler.formatter.api.report.ViolationReport;
import io.github.cowwoc.styler.formatter.api.report.ViolationStatistics;
import io.github.cowwoc.styler.formatter.api.report.XmlViolationSerializer;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XmlViolationSerializer} XML serialization and deserialization.
 */
public final class XmlViolationSerializerTest
{
	private static final XmlViolationSerializer SERIALIZER = new XmlViolationSerializer();

	/**
	 * Verifies empty report serializes to valid XML.
	 */
	@Test
	public void serializeEmptyReportProducesValidXml() throws SerializationException
	{
		ViolationReport report = ViolationReport.empty();

		String xml = SERIALIZER.serialize(report);

		assertThat(xml).contains("<violations");
		assertThat(xml).contains("<statistics>");
		assertThat(xml).contains("<timestampMillis>");
	}

	/**
	 * Verifies report with single violation serializes correctly.
	 */
	@Test
	public void serializeSingleViolationIncludesAllFields() throws SerializationException
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		ViolationEntry entry = new ViolationEntry("test-rule", ViolationSeverity.ERROR, "Test message",
			location, PriorityScore.of(ViolationSeverity.ERROR, 1), "Fix suggestion");
		ViolationStatistics stats = new ViolationStatistics(1,
			Map.of(ViolationSeverity.ERROR, 1), Map.of("test-rule", 1));
		ViolationReport report = ViolationReport.builder().
			addViolation(entry).
			statistics(stats).
			timestampMillis(1000L).
			build();

		String xml = SERIALIZER.serialize(report);

		assertThat(xml).contains("<ruleId>test-rule</ruleId>");
		assertThat(xml).contains("<message>Test message</message>");
		assertThat(xml).contains("<fixSuggestion>Fix suggestion</fixSuggestion>");
		assertThat(xml).contains("<severity>ERROR</severity>");
	}

	/**
	 * Verifies null fix suggestion is omitted from XML.
	 */
	@Test
	public void serializeNullFixSuggestionOmitsField() throws SerializationException
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		ViolationEntry entry = new ViolationEntry("test-rule", ViolationSeverity.INFO, "Test",
			location, PriorityScore.of(ViolationSeverity.INFO, 1), null);
		ViolationStatistics stats = new ViolationStatistics(1,
			Map.of(ViolationSeverity.INFO, 1), Map.of("test-rule", 1));
		ViolationReport report = ViolationReport.builder().
			addViolation(entry).
			statistics(stats).
			build();

		String xml = SERIALIZER.serialize(report);

		assertThat(xml).doesNotContain("fixSuggestion");
	}

	/**
	 * Verifies round-trip serialization preserves all data.
	 */
	@Test
	public void roundTripPreservesAllData() throws SerializationException
	{
		SourceRange location = new SourceRange(new SourcePosition(5, 10), new SourcePosition(5, 20));
		ViolationEntry entry1 = new ViolationEntry("rule-1", ViolationSeverity.ERROR, "Error message",
			location, PriorityScore.of(ViolationSeverity.ERROR, 1), "Fix it");
		ViolationEntry entry2 = new ViolationEntry("rule-2", ViolationSeverity.WARNING, "Warning message",
			location, PriorityScore.of(ViolationSeverity.WARNING, 1), null);
		ViolationStatistics stats = new ViolationStatistics(2,
			Map.of(ViolationSeverity.ERROR, 1, ViolationSeverity.WARNING, 1),
			Map.of("rule-1", 1, "rule-2", 1));
		ViolationReport original = ViolationReport.builder().
			addViolation(entry1).
			addViolation(entry2).
			statistics(stats).
			timestampMillis(123_456_789L).
			build();

		String xml = SERIALIZER.serialize(original);
		ViolationReport restored = SERIALIZER.deserialize(xml);

		assertThat(restored.violations()).hasSize(2);
		assertThat(restored.violations().get(0).ruleId()).isEqualTo("rule-1");
		assertThat(restored.violations().get(1).ruleId()).isEqualTo("rule-2");
		assertThat(restored.statistics().totalViolations()).isEqualTo(2);
		assertThat(restored.timestampMillis()).isEqualTo(123_456_789L);
	}

	/**
	 * Verifies deserialize with null XML throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void deserializeNullXmlThrows() throws SerializationException
	{
		SERIALIZER.deserialize(null);
	}

	/**
	 * Verifies deserialize with blank XML throws IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void deserializeBlankXmlThrows() throws SerializationException
	{
		SERIALIZER.deserialize("   ");
	}

	/**
	 * Verifies deserialize with invalid XML throws SerializationException.
	 */
	@Test(expectedExceptions = SerializationException.class)
	public void deserializeInvalidXmlThrows() throws SerializationException
	{
		SERIALIZER.deserialize("<invalid xml>");
	}

	/**
	 * Verifies serialize with null report throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void serializeNullReportThrows() throws SerializationException
	{
		SERIALIZER.serialize(null);
	}

	/**
	 * Verifies statistics are serialized with correct structure.
	 */
	@Test
	public void serializeStatisticsIncludesAllCounts() throws SerializationException
	{
		ViolationStatistics stats = new ViolationStatistics(5,
			Map.of(ViolationSeverity.ERROR, 3, ViolationSeverity.WARNING, 2),
			Map.of("rule-a", 3, "rule-b", 2));
		ViolationReport report = ViolationReport.builder().
			statistics(stats).
			build();

		String xml = SERIALIZER.serialize(report);

		assertThat(xml).contains("<totalViolations>5</totalViolations>");
		assertThat(xml).contains("<severityCounts>");
		assertThat(xml).contains("<ruleIdCounts>");
		assertThat(xml).contains("<ERROR>");
		assertThat(xml).contains("<WARNING>");
	}
}
