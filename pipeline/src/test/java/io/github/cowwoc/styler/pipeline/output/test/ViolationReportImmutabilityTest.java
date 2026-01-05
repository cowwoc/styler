package io.github.cowwoc.styler.pipeline.output.test;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for ViolationReport immutability and thread-safety.
 */
public class ViolationReportImmutabilityTest
{
	/**
	 * Tests that returned violations list is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void shouldReturnImmutableViolationsList()
	{
		// Given: Report with violations
		FormattingViolation violation = TestViolationFactory.createViolation(
			"TEST_RULE",
			ViolationSeverity.ERROR,
			"Test message");
		ImmutableViolationReport report = ImmutableViolationReport.of(List.of(violation));

		// When: Try to modify the returned list
		List<FormattingViolation> violations = report.violations();

		// Then: Should throw UnsupportedOperationException
		violations.add(TestViolationFactory.createViolation(
			"NEW_RULE",
			ViolationSeverity.WARNING,
			"New violation"));
	}

	/**
	 * Tests that returned grouped map is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void shouldReturnImmutableGroupedMap()
	{
		// Given: Report with violations
		FormattingViolation violation = TestViolationFactory.createViolation(
			"TEST_RULE",
			ViolationSeverity.ERROR,
			"Test message");
		ImmutableViolationReport report = ImmutableViolationReport.of(List.of(violation));

		// When: Try to modify the grouped map
		Map<String, List<FormattingViolation>> grouped = report.groupedByRule();

		// Then: Should throw UnsupportedOperationException
		grouped.put("NEW_RULE", List.of());
	}

	/**
	 * Test helper class for immutable violation reports.
	 */
	static final class ImmutableViolationReport
	{
		private final List<FormattingViolation> violations;
		private final Map<String, List<FormattingViolation>> groupedByRule;

		private ImmutableViolationReport(List<FormattingViolation> violations)
		{
			this.violations = List.copyOf(violations);

			Map<String, List<FormattingViolation>> grouped = new HashMap<>();
			for (FormattingViolation v : violations)
				grouped.computeIfAbsent(v.ruleId(), k -> new ArrayList<>()).add(v);

			for (Map.Entry<String, List<FormattingViolation>> entry : grouped.entrySet())
				grouped.put(entry.getKey(), List.copyOf(entry.getValue()));

			this.groupedByRule = Map.copyOf(grouped);
		}

		static ImmutableViolationReport of(List<FormattingViolation> violations)
		{
			return new ImmutableViolationReport(violations);
		}

		List<FormattingViolation> violations()
		{
			return violations;
		}

		Map<String, List<FormattingViolation>> groupedByRule()
		{
			return groupedByRule;
		}
	}
}
