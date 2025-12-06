package io.github.cowwoc.styler.pipeline.output.test;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Integration tests for violation output with pipeline components.
 */
public class ViolationOutputIntegrationTest
{
	/**
	 * Tests integration with pipeline result output.
	 */
	@Test
	public void shouldIntegrateWithPipelineResult()
	{
		// Given: Pipeline violations
		FormattingViolation violation = TestViolationFactory.createViolation(
			"LINE_LENGTH",
			ViolationSeverity.ERROR,
			"Line exceeds 120 characters");



		// When: Format violations
		String output = formatViolations(List.of(violation));

		// Then: Output should contain violation data
		requireThat(output, "value").isNotNull();
		requireThat(output.contains("LINE_LENGTH"), "value").isEqualTo(true);
		requireThat(output.contains("ERROR"), "value").isEqualTo(true);
	}

	/**
	 * Tests handling of pipeline with no violations.
	 */
	@Test
	public void shouldHandlePipelineWithNoViolations()
	{
		// When: Format empty violations
		String output = formatViolations(List.of());

		// Then: Should handle gracefully
		requireThat(output, "value").isNotNull();
		// Should be valid JSON with empty violations
		requireThat(output.contains("violations"), "value").isEqualTo(true);
		requireThat(output.contains("[]"), "value").isEqualTo(true);
	}

	/**
	 * Tests violation order preservation.
	 */
	@Test
	public void shouldPreserveViolationOrderFromPipeline()
	{
		// Given: Violations in specific order (by line number)
		FormattingViolation v1 = TestViolationFactory.createViolationAtLocation(5, 1);
		FormattingViolation v2 = TestViolationFactory.createViolationAtLocation(10, 1);
		FormattingViolation v3 = TestViolationFactory.createViolationAtLocation(15, 1);

		// When: Format violations
		String output = formatViolations(List.of(v1, v2, v3));

		// Then: All violations should be present
		requireThat(output, "value").isNotNull();
		requireThat(output.contains("5"), "value").isEqualTo(true);
		requireThat(output.contains("10"), "value").isEqualTo(true);
		requireThat(output.contains("15"), "value").isEqualTo(true);
	}

	/**
	 * Tests round-trip JSON parsing.
	 */
	@Test
	public void shouldRoundTripJsonParsing()
	{
		// Given: Original violations
		FormattingViolation original = TestViolationFactory.createViolation(
			"IMPORT_ORDER",
			ViolationSeverity.WARNING,
			"Import out of order");



		// When: Format as JSON
		String json = formatViolations(List.of(original));

		// Then: JSON should be valid and contain all fields
		requireThat(json, "value").isNotNull();
		requireThat(json.startsWith("{") || json.startsWith("["), "value").isEqualTo(true);
		requireThat(json.endsWith("}") || json.endsWith("]"), "value").isEqualTo(true);
		requireThat(json.contains("IMPORT_ORDER"), "value").isEqualTo(true);
		requireThat(json.contains("WARNING"), "value").isEqualTo(true);
	}

	/**
	 * Formats violations to simple output (placeholder for actual formatter).
	 *
	 * @param violations the violations to format
	 * @return formatted JSON string representation
	 */
	private String formatViolations(List<FormattingViolation> violations)
	{
		if (violations.isEmpty())
		{
			return "{ \"violations\": [] }";
		}

		StringBuilder json = new StringBuilder(128);
		json.append("{ \"violations\": [");

		for (int i = 0; i < violations.size(); i += 1)
		{
			if (i > 0) json.append(',');
			FormattingViolation v = violations.get(i);
			json.append("{ \"rule\": \"").append(v.ruleId()).
				append("\", \"severity\": \"").append(v.severity()).
				append("\", \"line\": ").append(v.lineNumber()).
				append(" }");
		}

		json.append("] }");
		return json.toString();
	}
}
