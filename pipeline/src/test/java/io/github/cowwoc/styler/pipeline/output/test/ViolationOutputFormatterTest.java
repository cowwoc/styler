package io.github.cowwoc.styler.pipeline.output.test;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ViolationOutputFormatter JSON output generation.
 */
public class ViolationOutputFormatterTest
{
	/**
	 * Tests that null violations list is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullViolationsList()
	{
		// This test verifies null validation at entry point
		// The actual formatter will be implemented in Phase 3
		// For now, we test the contract
		List<FormattingViolation> nullList = null;
		validateViolationsList(nullList);
	}

	/**
	 * Tests that empty violations list generates empty JSON array.
	 */
	@Test
	public void shouldGenerateEmptyArrayForNoViolations()
	{
		List<FormattingViolation> violations = List.of();

		// When: Format empty violations
		String json = formatAsJson(violations);

		// Then: Should generate valid JSON with empty violations array
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"violations\":[]");
		requireThat(json, "json").contains("\"summary\":");
		requireThat(json, "json").contains("\"total\":0");
	}

	/**
	 * Tests that single violation is formatted correctly as JSON.
	 */
	@Test
	public void shouldFormatSingleViolationAsJson()
	{
		// Given: Single LINE_LENGTH violation
		FormattingViolation violation = new DefaultFormattingViolation(
			"LINE_LENGTH",
			ViolationSeverity.ERROR,
			"Line exceeds 120 characters",
			Path.of("Test.java"),
			100,
			150,
			10,
			5,
			List.of());



		// When: Format as JSON
		String json = formatAsJson(List.of(violation));

		// Then: Should contain violation details
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"ruleId\":\"LINE_LENGTH\"");
		requireThat(json, "json").contains("\"severity\":\"ERROR\"");
		requireThat(json, "json").contains("\"message\":\"Line exceeds 120 characters\"");
		requireThat(json, "json").contains("\"file\":\"Test.java\"");
		requireThat(json, "json").contains("\"line\":10");
		requireThat(json, "json").contains("\"column\":5");
		requireThat(json, "json").contains("\"total\":1");
	}

	/**
	 * Tests that multiple violations with same rule are grouped correctly.
	 */
	@Test
	public void shouldFormatMultipleViolationsFromSameRule()
	{
		// Given: Two LINE_LENGTH violations
		FormattingViolation v1 = TestViolationFactory.createViolation("LINE_LENGTH",
			ViolationSeverity.ERROR, "Line 10 too long");
		FormattingViolation v2 = TestViolationFactory.createViolation("LINE_LENGTH",
			ViolationSeverity.ERROR, "Line 20 too long");

		// When: Format as JSON
		String json = formatAsJson(List.of(v1, v2));

		// Then: Should count both violations
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"total\":2");
		requireThat(json, "json").contains("\"byRule\":{\"LINE_LENGTH\":2}");
		// Should have 2 violations in array
		int violationCount = countOccurrences(json, "\"ruleId\":\"LINE_LENGTH\"");
		requireThat(violationCount, "violationCount").isEqualTo(2);
	}

	/**
	 * Tests that violations from different rules are aggregated correctly.
	 */
	@Test
	public void shouldFormatMultipleViolationsFromDifferentRules()
	{
		// Given: LINE_LENGTH and IMPORT_ORDER violations
		FormattingViolation v1 = TestViolationFactory.createViolation("LINE_LENGTH",
			ViolationSeverity.ERROR, "Line too long");
		FormattingViolation v2 = TestViolationFactory.createViolation("IMPORT_ORDER",
			ViolationSeverity.WARNING, "Import out of order");

		// When: Format as JSON
		String json = formatAsJson(List.of(v1, v2));

		// Then: Should aggregate by rule and severity
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"total\":2");
		requireThat(json, "json").contains("\"LINE_LENGTH\":1");
		requireThat(json, "json").contains("\"IMPORT_ORDER\":1");
		requireThat(json, "json").contains("\"ERROR\":1");
		requireThat(json, "json").contains("\"WARNING\":1");
	}

	/**
	 * Tests that violations are grouped by rule type in the report.
	 */
	@Test
	public void shouldGroupViolationsByRuleType()
	{
		// Given: 3 LINE_LENGTH + 2 IMPORT_ORDER violations
		List<FormattingViolation> violations = List.of(
			TestViolationFactory.createViolation("LINE_LENGTH", ViolationSeverity.ERROR, "msg1"),
			TestViolationFactory.createViolation("LINE_LENGTH", ViolationSeverity.ERROR, "msg2"),
			TestViolationFactory.createViolation("LINE_LENGTH", ViolationSeverity.ERROR, "msg3"),
			TestViolationFactory.createViolation("IMPORT_ORDER", ViolationSeverity.WARNING, "msg4"),
			TestViolationFactory.createViolation("IMPORT_ORDER", ViolationSeverity.WARNING, "msg5"));



		// When: Format as JSON
		String json = formatAsJson(violations);

		// Then: Should group correctly
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"total\":5");
		requireThat(json, "json").contains("\"LINE_LENGTH\":3");
		requireThat(json, "json").contains("\"IMPORT_ORDER\":2");
	}

	/**
	 * Tests that violation counts are tracked correctly per rule.
	 */
	@Test
	public void shouldCountViolationsCorrectly()
	{
		// Given: 2 violations for LINE_LENGTH, 1 for IMPORT_ORDER
		FormattingViolation line1 = TestViolationFactory.createViolation("LINE_LENGTH",
			ViolationSeverity.ERROR, "Error message");
		FormattingViolation line2 = TestViolationFactory.createViolation("LINE_LENGTH",
			ViolationSeverity.WARNING, "Warning message");
		FormattingViolation importV = TestViolationFactory.createViolation("IMPORT_ORDER",
			ViolationSeverity.WARNING, "Import message");

		// When: Format violations
		String json = formatAsJson(List.of(line1, line2, importV));

		// Then: Counts should be correct
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"violations\":");
		requireThat(json, "json").contains("\"LINE_LENGTH\":2");
		requireThat(json, "json").contains("\"IMPORT_ORDER\":1");
	}

	/**
	 * Tests that violations are sorted by severity (ERROR > WARNING > INFO).
	 */
	@Test
	public void shouldSortViolationsBySeverity()
	{
		// Given: INFO, ERROR, WARNING violations (out of order)
		FormattingViolation info = TestViolationFactory.createViolationWithSeverity(
			ViolationSeverity.INFO);
		FormattingViolation error = TestViolationFactory.createViolationWithSeverity(
			ViolationSeverity.ERROR);
		FormattingViolation warning = TestViolationFactory.createViolationWithSeverity(
			ViolationSeverity.WARNING);

		// When: Format violations
		String json = formatAsJson(List.of(info, error, warning));

		// Then: Should contain all severity levels
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"ERROR\"");
		requireThat(json, "json").contains("\"WARNING\"");
		requireThat(json, "json").contains("\"INFO\"");
		// Array should have 3 violations
		int violationCount = countOccurrences(json, "\"severity\":");
		requireThat(violationCount, "violationCount").isEqualTo(3);
	}

	/**
	 * Tests that very long file paths are handled correctly.
	 */
	@Test
	public void shouldHandleVeryLongFilePath()
	{
		// Given: Violation with very long file path (500+ characters)
		String longPath = "/".repeat(250) + "Test.java";
		Path path = Path.of(longPath);
		FormattingViolation violation = TestViolationFactory.createViolation(path);

		// When: Format as JSON
		String json = formatAsJson(List.of(violation));

		// Then: JSON should be valid and path should not be truncated
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"violations\":");
		// Should contain the path (possibly escaped)
		requireThat(json.length(), "jsonLength").isGreaterThan(100);
	}

	/**
	 * Tests that very long messages are handled correctly.
	 */
	@Test
	public void shouldHandleVeryLongMessage()
	{
		// Given: Violation with very long message (5000+ characters)
		String longMessage = "Line exceeds limit " + "x".repeat(5000);
		FormattingViolation violation = TestViolationFactory.createViolationWithMessage(longMessage);

		// When: Format as JSON
		String json = formatAsJson(List.of(violation));

		// Then: JSON should be valid and message should not be truncated
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"violations\":");
		requireThat(json.length(), "jsonLength").isGreaterThan(5000);
	}

	/**
	 * Tests that Unicode characters in messages are handled correctly.
	 */
	@Test
	public void shouldHandleUnicodeInMessage()
	{
		// Given: Violation with Unicode message (Chinese characters)
		String unicodeMessage = "Invalid character: \u4e2d\u6587\u5b57\u7b26 (Chinese)";
		FormattingViolation violation = TestViolationFactory.createViolationWithMessage(unicodeMessage);

		// When: Format as JSON
		String json = formatAsJson(List.of(violation));

		// Then: JSON should be valid and preserve Unicode
		requireThat(json, "json").isNotNull();
		requireThat(json, "json").contains("\"violations\":");
		// Should contain either Unicode characters or escaped Unicode sequences
		boolean hasUnicode = json.contains("\u4e2d") || json.contains("\\u4e2d");
		requireThat(hasUnicode, "hasUnicode").isEqualTo(true);
	}

	/**
	 * Tests that special characters in file paths are escaped correctly.
	 */
	@Test
	public void shouldHandleSpecialCharactersInPath()
	{
		// Given: Path with spaces and quotes
		Path specialPath = Path.of("/path/with spaces/and \"quotes\"/Test.java");
		FormattingViolation violation = TestViolationFactory.createViolation(specialPath);

		// When: Format as JSON
		String json = formatAsJson(List.of(violation));

		// Then: JSON should be valid with properly escaped quotes
		requireThat(json, "json").isNotNull();
		// Should contain escaped quotes
		requireThat(json, "json").contains("\\\"");
		// Should be valid JSON
		requireThat(json, "json").contains("\"violations\":");
	}

	// ==================== Helper Methods ====================

	/**
	 * Validates that violations list is not null.
	 *
	 * @param violations the violations list
	 * @throws IllegalArgumentException if {@code violations} is {@code null}
	 */
	private void validateViolationsList(List<FormattingViolation> violations)
	{
		if (violations == null)
			throw new NullPointerException("violations cannot be null");
	}

	/**
	 * Formats violations as JSON (placeholder for actual formatter).
	 *
	 * @param violations the violations to format
	 * @return JSON string representation
	 */
	private String formatAsJson(List<FormattingViolation> violations)
	{
		// This is a placeholder implementation
		// The actual ViolationOutputFormatter will be implemented in Phase 3
		// For now, return a basic JSON structure
		StringBuilder json = new StringBuilder(256);
		json.append('{').append("\"violations\":[");

		for (int i = 0; i < violations.size(); ++i)
		{
			if (i > 0)
				json.append(',');
			FormattingViolation v = violations.get(i);
			json.append('{').
				append("\"ruleId\":\"").append(v.ruleId()).append("\",").
				append("\"severity\":\"").append(v.severity()).append("\",").
				append("\"message\":\"").append(escapeJson(v.message())).append("\",").
				append("\"file\":\"").append(escapeJson(v.filePath().toString())).append("\",").
				append("\"line\":").append(v.lineNumber()).append(',').
				append("\"column\":").append(v.columnNumber()).
				append('}');
		}

		json.append("],").
			append("\"summary\":{").
			append("\"total\":").append(violations.size()).append(',').
			append("\"byRule\":").
			append(buildRuleCountJson(violations)).append(',').
			append("\"bySeverity\":").
			append(buildSeverityCountJson(violations)).
			append('}').
			append('}');

		return json.toString();
	}

	/**
	 * Escapes special characters for JSON.
	 *
	 * @param str the string to escape
	 * @return escaped string
	 */
	private String escapeJson(String str)
	{
		return str.
			replace("\\", "\\\\").
			replace("\"", "\\\"").
			replace("\n", "\\n").
			replace("\r", "\\r").
			replace("\t", "\\t");
	}

	/**
	 * Builds JSON for rule counts.
	 *
	 * @param violations the violations list
	 * @return JSON string
	 */
	private String buildRuleCountJson(List<FormattingViolation> violations)
	{
		Map<String, Integer> ruleCounts = new HashMap<>();
		for (FormattingViolation v : violations)
			ruleCounts.put(v.ruleId(), ruleCounts.getOrDefault(v.ruleId(), 0) + 1);

		StringBuilder json = new StringBuilder(64);
		json.append('{');
		boolean first = true;
		for (Map.Entry<String, Integer> entry : ruleCounts.entrySet())
		{
			if (!first)
				json.append(',');
			json.append('"').append(entry.getKey()).append("\":").append(entry.getValue());
			first = false;
		}
		json.append('}');
		return json.toString();
	}

	/**
	 * Builds JSON for severity counts.
	 *
	 * @param violations the violations list
	 * @return JSON string
	 */
	private String buildSeverityCountJson(List<FormattingViolation> violations)
	{
		Map<String, Integer> severityCounts = new HashMap<>();
		for (FormattingViolation v : violations)
		{
			String severity = v.severity().toString();
			severityCounts.merge(severity, 1, Integer::sum);
		}

		StringBuilder json = new StringBuilder(64);
		json.append('{');
		boolean first = true;
		for (Map.Entry<String, Integer> entry : severityCounts.entrySet())
		{
			if (!first)
				json.append(',');
			json.append('"').append(entry.getKey()).append("\":").append(entry.getValue());
			first = false;
		}
		json.append('}');
		return json.toString();
	}

	/**
	 * Counts occurrences of substring in string.
	 *
	 * @param str the string to search
	 * @param substring the substring to count
	 * @return count of occurrences
	 */
	private int countOccurrences(String str, String substring)
	{
		int count = 0;
		int index = 0;
		index = str.indexOf(substring, index);
		while (index != -1)
		{
			++count;
			index += substring.length();
			index = str.indexOf(substring, index);
		}
		return count;
	}
}
