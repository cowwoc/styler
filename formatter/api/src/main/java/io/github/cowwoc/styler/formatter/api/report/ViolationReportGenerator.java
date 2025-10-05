package io.github.cowwoc.styler.formatter.api.report;

import io.github.cowwoc.styler.formatter.api.FormattingViolation;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Generates structured violation reports from formatting violations.
 * <p>
 * This adapter converts a list of {@link FormattingViolation} objects into a {@link ViolationReport}
 * with priority scores, statistics, and metadata. The conversion is non-invasive and does not modify
 * existing violation data.
 * </p>
 * <p>
 * This class is stateless and thread-safe. All state is passed via method parameters. Multiple threads
 * can safely use the same instance concurrently.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * List<FormattingViolation> violations = ...;
 * ViolationReport report = ViolationReportGenerator.generate(violations);
 * String json = new JsonViolationSerializer().serialize(report);
 * }</pre>
 *
 * @see ViolationReport
 * @see FormattingViolation
 */
public final class ViolationReportGenerator
{
	/**
	 * Private constructor to prevent instantiation.
	 * <p>
	 * This is a utility class with only static methods.
	 * </p>
	 */
	private ViolationReportGenerator()
	{
	}

	/**
	 * Generates a violation report from a list of formatting violations.
	 * <p>
	 * Converts each {@link FormattingViolation} to a {@link ViolationEntry}, calculates priority
	 * scores, aggregates statistics, and creates a timestamped report. Violations in the report
	 * are sorted by priority (highest first).
	 * </p>
	 *
	 * @param violations the formatting violations to include, must not be {@code null}
	 * @return the generated violation report, never {@code null}
	 * @throws NullPointerException if {@code violations} is {@code null}
	 */
	public static ViolationReport generate(List<FormattingViolation> violations)
	{
		requireThat(violations, "violations").isNotNull();

		if (violations.isEmpty())
		{
			return ViolationReport.empty();
		}

		List<ViolationEntry> entries = convertViolations(violations);
		ViolationStatistics stats = calculateStatistics(violations);
		long timestamp = System.currentTimeMillis();

		return ViolationReport.builder().
			violations(entries).
			statistics(stats).
			timestampMillis(timestamp).
			build();
	}

	/**
	 * Converts formatting violations to violation entries with priority scores.
	 * <p>
	 * Each violation is assigned a priority based on its severity (ERROR=100, WARNING=10, INFO=1)
	 * with frequency=1 (single occurrence). Entries are sorted by priority in descending order.
	 * </p>
	 *
	 * @param violations the violations to convert, never {@code null}
	 * @return the list of violation entries, never {@code null}
	 */
	private static List<ViolationEntry> convertViolations(List<FormattingViolation> violations)
	{
		return violations.stream().
			map(ViolationReportGenerator::convertSingle).
			sorted((e1, e2) -> e1.priorityScore().compareTo(e2.priorityScore())).
			toList();
	}

	/**
	 * Converts a single formatting violation to a violation entry.
	 * <p>
	 * Priority is calculated with frequency=1 (single violation). Fix suggestion is preserved
	 * from the original violation if available.
	 * </p>
	 *
	 * @param violation the violation to convert, never {@code null}
	 * @return the violation entry, never {@code null}
	 */
	private static ViolationEntry convertSingle(FormattingViolation violation)
	{
		PriorityScore priority = PriorityScore.of(violation.getSeverity(), 1);
		String fixSuggestion;
		if (violation.hasSuggestedFix())
		{
			fixSuggestion = violation.getSuggestedFix();
		}
		else
		{
			fixSuggestion = null;
		}

		return new ViolationEntry(violation.getRuleId(), violation.getSeverity(), violation.getMessage(),
			violation.getLocation(), priority, fixSuggestion);
	}

	/**
	 * Calculates aggregated statistics from formatting violations.
	 * <p>
	 * Computes total violation count, counts by severity level, and counts by rule ID.
	 * </p>
	 *
	 * @param violations the violations to aggregate, never {@code null}
	 * @return the violation statistics, never {@code null}
	 */
	private static ViolationStatistics calculateStatistics(List<FormattingViolation> violations)
	{
		int totalViolations = violations.size();

		Map<ViolationSeverity, Integer> severityCounts = new EnumMap<>(ViolationSeverity.class);
		Map<String, Integer> ruleIdCounts = new HashMap<>();

		for (FormattingViolation violation : violations)
		{
			// Count by severity
			ViolationSeverity severity = violation.getSeverity();
			severityCounts.merge(severity, 1, Integer::sum);

			// Count by rule ID
			String ruleId = violation.getRuleId();
			ruleIdCounts.merge(ruleId, 1, Integer::sum);
		}

		return new ViolationStatistics(totalViolations, severityCounts, ruleIdCounts);
	}
}
