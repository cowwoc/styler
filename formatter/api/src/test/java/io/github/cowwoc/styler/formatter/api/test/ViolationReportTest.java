package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import io.github.cowwoc.styler.formatter.api.report.PriorityScore;
import io.github.cowwoc.styler.formatter.api.report.ViolationEntry;
import io.github.cowwoc.styler.formatter.api.report.ViolationReport;
import io.github.cowwoc.styler.formatter.api.report.ViolationStatistics;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ViolationReport} builder pattern and aggregation.
 */
public final class ViolationReportTest
{
	/**
	 * Verifies builder creates report with all fields.
	 */
	@Test
	public void builderCreatesReportWithAllFields()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		ViolationEntry entry = new ViolationEntry("test", ViolationSeverity.ERROR, "message",
			location, PriorityScore.of(ViolationSeverity.ERROR, 1), null);
		ViolationStatistics stats = new ViolationStatistics(1,
			Map.of(ViolationSeverity.ERROR, 1), Map.of("test", 1));

		ViolationReport report = ViolationReport.builder().
			addViolation(entry).
			statistics(stats).
			timestampMillis(1000L).
			build();

		assertThat(report.violations()).hasSize(1);
		assertThat(report.statistics()).isEqualTo(stats);
		assertThat(report.timestampMillis()).isEqualTo(1000L);
	}

	/**
	 * Verifies builder with violations() replaces individual adds.
	 */
	@Test
	public void builderViolationsReplacesIndividualAdds()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		ViolationEntry entry1 = new ViolationEntry("rule1", ViolationSeverity.ERROR, "msg1",
			location, PriorityScore.of(ViolationSeverity.ERROR, 1), null);
		ViolationEntry entry2 = new ViolationEntry("rule2", ViolationSeverity.WARNING, "msg2",
			location, PriorityScore.of(ViolationSeverity.WARNING, 1), null);
		ViolationStatistics stats = new ViolationStatistics(2,
			Map.of(ViolationSeverity.ERROR, 1, ViolationSeverity.WARNING, 1),
			Map.of("rule1", 1, "rule2", 1));

		ViolationReport report = ViolationReport.builder().
			violations(List.of(entry1, entry2)).
			statistics(stats).
			build();

		assertThat(report.violations()).containsExactly(entry1, entry2);
	}

	/**
	 * Verifies empty() creates report with no violations.
	 */
	@Test
	public void emptyCreatesReportWithNoViolations()
	{
		ViolationReport report = ViolationReport.empty();

		assertThat(report.violations()).isEmpty();
		assertThat(report.statistics().totalViolations()).isEqualTo(0);
	}

	/**
	 * Verifies violations list is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void violationsListIsImmutable()
	{
		ViolationReport report = ViolationReport.empty();
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		ViolationEntry entry = new ViolationEntry("test", ViolationSeverity.ERROR, "msg",
			location, PriorityScore.of(ViolationSeverity.ERROR, 1), null);

		report.violations().add(entry);
	}

	/**
	 * Verifies builder with null statistics throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void builderNullStatisticsThrows()
	{
		ViolationReport.builder().
			statistics(null).
			build();
	}

	/**
	 * Verifies builder with missing statistics throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void builderMissingStatisticsThrows()
	{
		ViolationReport.builder().build();
	}

	/**
	 * Verifies timestamp defaults to current time if not set.
	 */
	@Test
	public void builderDefaultsTimestampToCurrentTime()
	{
		long before = System.currentTimeMillis();
		ViolationStatistics stats = new ViolationStatistics(0, Map.of(), Map.of());

		ViolationReport report = ViolationReport.builder().
			statistics(stats).
			build();

		long after = System.currentTimeMillis();
		assertThat(report.timestampMillis()).isBetween(before, after);
	}

	/**
	 * Verifies multiple violations can be added via builder.
	 */
	@Test
	public void builderSupportsMultipleAddViolation()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		ViolationEntry entry1 = new ViolationEntry("rule1", ViolationSeverity.ERROR, "msg1",
			location, PriorityScore.of(ViolationSeverity.ERROR, 1), null);
		ViolationEntry entry2 = new ViolationEntry("rule2", ViolationSeverity.INFO, "msg2",
			location, PriorityScore.of(ViolationSeverity.INFO, 1), null);
		ViolationStatistics stats = new ViolationStatistics(2,
			Map.of(ViolationSeverity.ERROR, 1, ViolationSeverity.INFO, 1),
			Map.of("rule1", 1, "rule2", 1));

		ViolationReport report = ViolationReport.builder().
			addViolation(entry1).
			addViolation(entry2).
			statistics(stats).
			build();

		assertThat(report.violations()).containsExactly(entry1, entry2);
	}
}
