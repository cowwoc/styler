package io.github.cowwoc.styler.pipeline.output.test;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.pipeline.output.PriorityCalculator;
import io.github.cowwoc.styler.pipeline.output.internal.DefaultPriorityCalculator;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for PriorityCalculator violation counting algorithm.
 */
public class PriorityCalculatorTest
{
	private final PriorityCalculator calculator = new DefaultPriorityCalculator();

	/**
	 * Tests that empty map is returned for empty violations list.
	 */
	@Test
	public void shouldReturnEmptyMapForEmptyViolations()
	{
		// Given: Empty violations list
		List<FormattingViolation> violations = List.of();

		// When: Count by rule
		Map<String, Integer> counts = calculator.countByRule(violations);

		// Then: Map should be empty
		requireThat(counts, "counts").isEmpty();
	}

	/**
	 * Tests that single violation returns count of 1.
	 */
	@Test
	public void shouldCountSingleViolation()
	{
		// Given: Single violation
		FormattingViolation violation = TestViolationFactory.createViolationWithSeverity(
			ViolationSeverity.ERROR);
		List<FormattingViolation> violations = List.of(violation);

		// When: Count by rule
		Map<String, Integer> counts = calculator.countByRule(violations);

		// Then: Count should be 1
		requireThat(counts.get(violation.ruleId()), "count").isEqualTo(1);
	}

	/**
	 * Tests that multiple violations of same rule returns correct count.
	 */
	@Test
	public void shouldCountMultipleViolationsOfSameRule()
	{
		// Given: 3 violations for same rule
		FormattingViolation v1 = TestViolationFactory.createViolation("LINE_LENGTH",
			ViolationSeverity.WARNING, "msg1");
		FormattingViolation v2 = TestViolationFactory.createViolation("LINE_LENGTH",
			ViolationSeverity.WARNING, "msg2");
		FormattingViolation v3 = TestViolationFactory.createViolation("LINE_LENGTH",
			ViolationSeverity.WARNING, "msg3");
		List<FormattingViolation> violations = List.of(v1, v2, v3);

		// When: Count by rule
		Map<String, Integer> counts = calculator.countByRule(violations);

		// Then: Count should be 3
		requireThat(counts.get("LINE_LENGTH"), "count").isEqualTo(3);
	}

	/**
	 * Tests that multiple rules return correct counts for each.
	 */
	@Test
	public void shouldCountMultipleRulesCorrectly()
	{
		// Given: 2 violations for RULE_A, 3 for RULE_B, 1 for RULE_C
		FormattingViolation a1 = TestViolationFactory.createViolation("RULE_A",
			ViolationSeverity.ERROR, "msg1");
		FormattingViolation a2 = TestViolationFactory.createViolation("RULE_A",
			ViolationSeverity.ERROR, "msg2");
		FormattingViolation b1 = TestViolationFactory.createViolation("RULE_B",
			ViolationSeverity.WARNING, "msg3");
		FormattingViolation b2 = TestViolationFactory.createViolation("RULE_B",
			ViolationSeverity.WARNING, "msg4");
		FormattingViolation b3 = TestViolationFactory.createViolation("RULE_B",
			ViolationSeverity.WARNING, "msg5");
		FormattingViolation c1 = TestViolationFactory.createViolation("RULE_C",
			ViolationSeverity.INFO, "msg6");

		List<FormattingViolation> violations = List.of(a1, a2, b1, b2, b3, c1);

		// When: Count by rule
		Map<String, Integer> counts = calculator.countByRule(violations);

		// Then: Counts should be correct
		requireThat(counts.get("RULE_A"), "RULE_A_count").isEqualTo(2);
		requireThat(counts.get("RULE_B"), "RULE_B_count").isEqualTo(3);
		requireThat(counts.get("RULE_C"), "RULE_C_count").isEqualTo(1);
		requireThat(counts.size(), "totalRules").isEqualTo(3);
	}

	/**
	 * Tests that severity does not affect count (count is just frequency).
	 */
	@Test
	public void shouldCountRegardlessOfSeverity()
	{
		// Given: Mix of severities for same rule
		FormattingViolation error = TestViolationFactory.createViolation("MIXED_RULE",
			ViolationSeverity.ERROR, "error msg");
		FormattingViolation warning = TestViolationFactory.createViolation("MIXED_RULE",
			ViolationSeverity.WARNING, "warning msg");
		FormattingViolation info = TestViolationFactory.createViolation("MIXED_RULE",
			ViolationSeverity.INFO, "info msg");

		List<FormattingViolation> violations = List.of(error, warning, info);

		// When: Count by rule
		Map<String, Integer> counts = calculator.countByRule(violations);

		// Then: Count should be 3 regardless of severity
		requireThat(counts.get("MIXED_RULE"), "count").isEqualTo(3);
	}
}
