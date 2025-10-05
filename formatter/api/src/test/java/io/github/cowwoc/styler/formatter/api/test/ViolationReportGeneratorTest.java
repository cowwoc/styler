package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.FormattingViolation;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import io.github.cowwoc.styler.formatter.api.report.PriorityScore;
import io.github.cowwoc.styler.formatter.api.report.ViolationEntry;
import io.github.cowwoc.styler.formatter.api.report.ViolationReport;
import io.github.cowwoc.styler.formatter.api.report.ViolationReportGenerator;
import io.github.cowwoc.styler.formatter.api.report.ViolationStatistics;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ViolationReportGenerator} report generation workflow.
 */
public final class ViolationReportGeneratorTest
{
	/**
	 * Creates a test source range.
	 *
	 * @return a source range for testing
	 */
	private static SourceRange createTestRange()
	{
		return new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
	}

	/**
	 * Verifies empty violation list produces empty report with zero statistics.
	 */
	@Test
	public void generateWithEmptyListReturnsEmptyReport()
	{
		List<FormattingViolation> violations = List.of();

		ViolationReport report = ViolationReportGenerator.generate(violations);

		assertThat(report.violations()).isEmpty();
		assertThat(report.statistics().totalViolations()).isEqualTo(0);
		assertThat(report.statistics().severityCounts()).isEmpty();
		assertThat(report.statistics().ruleIdCounts()).isEmpty();
	}

	/**
	 * Verifies single violation generates report with correct statistics.
	 */
	@Test
	public void generateWithSingleViolationCalculatesCorrectStatistics()
	{
		FormattingViolation violation = new FormattingViolation("test-rule", createTestRange(),
			"Test violation", ViolationSeverity.ERROR, null);
		List<FormattingViolation> violations = List.of(violation);

		ViolationReport report = ViolationReportGenerator.generate(violations);

		assertThat(report.violations()).hasSize(1);
		assertThat(report.statistics().totalViolations()).isEqualTo(1);
		assertThat(report.statistics().severityCounts().get(ViolationSeverity.ERROR)).isEqualTo(1);
		assertThat(report.statistics().ruleIdCounts().get("test-rule")).isEqualTo(1);
	}

	/**
	 * Verifies multiple violations are sorted by priority descending.
	 */
	@Test
	public void generateSortsViolationsByPriorityDescending()
	{
		SourceRange location = createTestRange();
		FormattingViolation info = new FormattingViolation("info-rule", location, "Info",
			ViolationSeverity.INFO, null);
		FormattingViolation warning = new FormattingViolation("warning-rule", location, "Warning",
			ViolationSeverity.WARNING, null);
		FormattingViolation error = new FormattingViolation("error-rule", location, "Error",
			ViolationSeverity.ERROR, null);
		List<FormattingViolation> violations = List.of(info, warning, error);

		ViolationReport report = ViolationReportGenerator.generate(violations);

		List<ViolationEntry> entries = report.violations();
		assertThat(entries).hasSize(3);
		assertThat(entries.get(0).severity()).isEqualTo(ViolationSeverity.ERROR);
		assertThat(entries.get(1).severity()).isEqualTo(ViolationSeverity.WARNING);
		assertThat(entries.get(2).severity()).isEqualTo(ViolationSeverity.INFO);
	}

	/**
	 * Verifies priority scores are calculated with frequency 1.
	 */
	@Test
	public void generateAssignsPriorityWithFrequency1()
	{
		FormattingViolation violation = new FormattingViolation("test-rule", createTestRange(), "Test",
			ViolationSeverity.ERROR, null);
		List<FormattingViolation> violations = List.of(violation);

		ViolationReport report = ViolationReportGenerator.generate(violations);

		ViolationEntry entry = report.violations().get(0);
		PriorityScore expected = PriorityScore.of(ViolationSeverity.ERROR, 1);
		assertThat(entry.priorityScore()).isEqualTo(expected);
	}

	/**
	 * Verifies violations with suggested fixes preserve fix suggestions.
	 */
	@Test
	public void generatePreservesSuggestedFixes()
	{
		FormattingViolation violation = new FormattingViolation("test-rule", createTestRange(), "Test",
			ViolationSeverity.WARNING, "Apply this fix");
		List<FormattingViolation> violations = List.of(violation);

		ViolationReport report = ViolationReportGenerator.generate(violations);

		ViolationEntry entry = report.violations().get(0);
		assertThat(entry.fixSuggestion()).isEqualTo("Apply this fix");
	}

	/**
	 * Verifies violations without suggested fixes have null fix suggestion.
	 */
	@Test
	public void generateWithoutFixSuggestionSetsNullFix()
	{
		FormattingViolation violation = FormattingViolation.create("test-rule", createTestRange(), "Test",
			ViolationSeverity.INFO);
		List<FormattingViolation> violations = List.of(violation);

		ViolationReport report = ViolationReportGenerator.generate(violations);

		ViolationEntry entry = report.violations().get(0);
		assertThat(entry.fixSuggestion()).isNull();
	}

	/**
	 * Verifies timestamp is set to current time.
	 */
	@Test
	public void generateSetsTimestampToCurrentTime()
	{
		long before = System.currentTimeMillis();
		List<FormattingViolation> violations = List.of();

		ViolationReport report = ViolationReportGenerator.generate(violations);

		long after = System.currentTimeMillis();
		assertThat(report.timestampMillis()).isBetween(before, after);
	}

	/**
	 * Verifies severity counts aggregate correctly for mixed violations.
	 */
	@Test
	public void generateAggregatesSeverityCountsCorrectly()
	{
		SourceRange location = createTestRange();
		List<FormattingViolation> violations = new ArrayList<>();

		for (int i = 0; i < 3; ++i)
		{
			violations.add(new FormattingViolation("rule-" + i, location, "Error " + i,
				ViolationSeverity.ERROR, null));
		}

		for (int i = 0; i < 2; ++i)
		{
			violations.add(new FormattingViolation("rule-" + i, location, "Warning " + i,
				ViolationSeverity.WARNING, null));
		}

		ViolationReport report = ViolationReportGenerator.generate(violations);

		ViolationStatistics stats = report.statistics();
		assertThat(stats.totalViolations()).isEqualTo(5);
		assertThat(stats.severityCounts().get(ViolationSeverity.ERROR)).isEqualTo(3);
		assertThat(stats.severityCounts().get(ViolationSeverity.WARNING)).isEqualTo(2);
	}

	/**
	 * Verifies rule ID counts aggregate correctly for repeated rules.
	 */
	@Test
	public void generateAggregatesRuleIdCountsCorrectly()
	{
		SourceRange location = createTestRange();
		List<FormattingViolation> violations = List.of(
			new FormattingViolation("rule-a", location, "Error 1", ViolationSeverity.ERROR, null),
			new FormattingViolation("rule-a", location, "Error 2", ViolationSeverity.ERROR, null),
			new FormattingViolation("rule-b", location, "Warning", ViolationSeverity.WARNING, null));

		ViolationReport report = ViolationReportGenerator.generate(violations);

		ViolationStatistics stats = report.statistics();
		assertThat(stats.ruleIdCounts().get("rule-a")).isEqualTo(2);
		assertThat(stats.ruleIdCounts().get("rule-b")).isEqualTo(1);
	}

	/**
	 * Verifies null violations list throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void generateWithNullViolationsThrows()
	{
		ViolationReportGenerator.generate(null);
	}
}
