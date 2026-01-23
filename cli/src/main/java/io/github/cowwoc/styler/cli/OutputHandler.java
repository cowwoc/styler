package io.github.cowwoc.styler.cli;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.cowwoc.styler.errorcatalog.Audience;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.pipeline.PipelineResult;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;
import io.github.cowwoc.styler.pipeline.output.ViolationReport;
import io.github.cowwoc.styler.pipeline.output.ViolationReportRenderer;

/**
 * Formats and displays pipeline processing results to stdout.
 * <p>
 * Renders pipeline results in the appropriate format (JSON for AI, human-readable for
 * TTY). Results are written to stdout for display to end users.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class OutputHandler
{
	/**
	 * Default maximum violations for AI audience context limiting.
	 */
	public static final int DEFAULT_AI_MAX_VIOLATIONS = 50;
	/**
	 * Default maximum violations for human audience.
	 */
	public static final int DEFAULT_HUMAN_MAX_VIOLATIONS = 100;

	/**
	 * Result of limiting violations to a maximum count.
	 *
	 * @param limitedReports    the reports with limited violations
	 * @param remainingSummary  summary of omitted violations, or null if none
	 */
	public record ViolationLimitResult(
		List<ViolationReport> limitedReports,
		ViolationSummary remainingSummary)
	{
	}

	/**
	 * Summary of violations that were omitted due to limit.
	 *
	 * @param totalRemaining  total number of omitted violations
	 * @param bySeverity      count of omitted violations by severity
	 * @param byRule          count of omitted violations by rule ID
	 */
	public record ViolationSummary(
		int totalRemaining,
		Map<ViolationSeverity, Integer> bySeverity,
		Map<String, Integer> byRule)
	{
	}

	/**
	 * Internal record to track violations with their source file.
	 *
	 * @param violation the formatting violation
	 * @param filePath  the source file path
	 */
	private record ViolationWithSource(FormattingViolation violation, Path filePath)
	{
	}

	/**
	 * Renders pipeline results in the detected output format.
	 * <p>
	 * Aggregates violations from all pipeline results and renders them using
	 * the appropriate renderer based on the output format. Writes to stdout.
	 *
	 * @param results       the list of pipeline results to render (non-null, may be empty)
	 * @param format        the output format to use: {@code JSON} or {@code HUMAN}
	 * @param maxViolations maximum violations to show: 0 = summary only (no individual violations),
	 *                      positive = show up to that many, {@code Integer.MAX_VALUE} = unlimited
	 * @throws NullPointerException     if any of the arguments are {@code null}
	 * @throws IllegalArgumentException if {@code maxViolations} is negative
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public void render(List<PipelineResult> results, OutputFormat format, int maxViolations)
	{
		requireThat(results, "results").isNotNull();
		requireThat(format, "format").isNotNull();
		requireThat(maxViolations, "maxViolations").isNotNegative();

		// Build initial reports from results
		List<ViolationReport> allReports = new ArrayList<>();
		for (PipelineResult result : results)
		{
			Map<String, Integer> ruleCounts = new HashMap<>();
			result.violations().forEach(violation ->
				ruleCounts.merge(violation.ruleId(), 1, Integer::sum));

			allReports.add(new ViolationReport(
				result.filePath(),
				result.violations(),
				ruleCounts));
		}

		// Apply violation limiting
		ViolationLimitResult limited = limitViolations(allReports, maxViolations);

		// Render limited reports (skip if maxViolations == 0, summary only mode)
		if (maxViolations > 0)
		{
			ViolationReportRenderer renderer = ViolationReportRenderer.create(format);
			for (ViolationReport report : limited.limitedReports())
			{
				String output = renderer.render(report);
				System.out.println(output);
			}
		}

		// Render summary if violations were truncated or in summary-only mode
		if (limited.remainingSummary() != null)
			renderRemainingSummary(limited.remainingSummary(), format);
	}

	/**
	 * Limits violations to maxViolations, prioritizing by severity (ERROR > WARNING > INFO).
	 * <p>
	 * Returns the limited violations and a summary of what was omitted.
	 *
	 * @param reports       the violation reports to limit
	 * @param maxViolations maximum violations to include: 0 = summary only (all violations in summary),
	 *                      positive = show up to that many, {@code Integer.MAX_VALUE} = unlimited
	 * @return the limited result with optional summary
	 */
	public ViolationLimitResult limitViolations(List<ViolationReport> reports, int maxViolations)
	{
		// Collect all violations across all reports with their source
		List<ViolationWithSource> allViolations = new ArrayList<>();
		for (ViolationReport report : reports)
			for (FormattingViolation violation : report.violations())
				allViolations.add(new ViolationWithSource(violation, report.filePath()));

		// Handle summary-only mode (maxViolations == 0)
		if (maxViolations == 0)
		{
			if (allViolations.isEmpty())
				return new ViolationLimitResult(List.of(), null);

			// All violations go to summary
			Map<ViolationSeverity, Integer> bySeverity = allViolations.stream().
				collect(Collectors.groupingBy(
					v -> v.violation().severity(),
					Collectors.summingInt(v -> 1)));
			Map<String, Integer> byRule = allViolations.stream().
				collect(Collectors.groupingBy(
					v -> v.violation().ruleId(),
					Collectors.summingInt(v -> 1)));

			ViolationSummary summary = new ViolationSummary(allViolations.size(), bySeverity, byRule);
			return new ViolationLimitResult(List.of(), summary);
		}

		// If total is within limit, return as-is
		if (allViolations.size() <= maxViolations)
			return new ViolationLimitResult(reports, null);

		// Sort by severity (ERROR first), then by rule for consistency
		allViolations.sort(Comparator.
			comparing((ViolationWithSource v) -> v.violation().severity().weight()).reversed().
			thenComparing(v -> v.violation().ruleId()));

		// Split into included and remaining
		List<ViolationWithSource> included = allViolations.subList(0, maxViolations);
		List<ViolationWithSource> remaining = allViolations.subList(maxViolations, allViolations.size());

		// Rebuild reports from included violations
		Map<Path, List<FormattingViolation>> byPath = included.stream().
			collect(Collectors.groupingBy(
				ViolationWithSource::filePath,
				Collectors.mapping(ViolationWithSource::violation, Collectors.toList())));

		List<ViolationReport> limitedReports = reports.stream().
			filter(report -> byPath.containsKey(report.filePath())).
			map(report ->
			{
				List<FormattingViolation> limitedViolations = byPath.get(report.filePath());
				Map<String, Integer> ruleCounts = new HashMap<>();
				limitedViolations.forEach(v -> ruleCounts.merge(v.ruleId(), 1, Integer::sum));
				return new ViolationReport(report.filePath(), limitedViolations, ruleCounts);
			}).
			toList();

		// Create summary for remaining
		Map<ViolationSeverity, Integer> bySeverity = remaining.stream().
			collect(Collectors.groupingBy(
				v -> v.violation().severity(),
				Collectors.summingInt(v -> 1)));
		Map<String, Integer> byRule = remaining.stream().
			collect(Collectors.groupingBy(
				v -> v.violation().ruleId(),
				Collectors.summingInt(v -> 1)));

		ViolationSummary summary = new ViolationSummary(remaining.size(), bySeverity, byRule);
		return new ViolationLimitResult(limitedReports, summary);
	}

	/**
	 * Renders a summary of omitted violations.
	 *
	 * @param summary the summary of remaining violations
	 * @param format  the output format
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	private void renderRemainingSummary(ViolationSummary summary, OutputFormat format)
	{
		if (format == OutputFormat.JSON)
		{
			// JSON: output truncation info as separate JSON object
			StringBuilder json = new StringBuilder(256).
				append("{\n").
				append("  \"truncated\": {\n").
				append("    \"remaining\": ").append(summary.totalRemaining()).append(",\n").
				append("    \"bySeverity\": {");
			boolean first = true;
			for (Map.Entry<ViolationSeverity, Integer> entry : summary.bySeverity().entrySet())
			{
				if (!first)
					json.append(',');
				json.append("\n      \"").append(entry.getKey()).append("\": ").append(entry.getValue());
				first = false;
			}
			json.append("\n    },\n").
				append("    \"byRule\": {");
			first = true;
			// Sort by count descending for top rules
			List<Map.Entry<String, Integer>> sortedRules = summary.byRule().entrySet().stream().
				sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).
				limit(5).
				toList();
			for (Map.Entry<String, Integer> entry : sortedRules)
			{
				if (!first)
					json.append(',');
				json.append("\n      \"").append(entry.getKey()).append("\": ").append(entry.getValue());
				first = false;
			}
			json.append("\n    }\n").
				append("  }\n").
				append('}');
			System.out.println(json);
		}
		else
		{
			// Human format: terse summary line
			int errorCount = summary.bySeverity().getOrDefault(ViolationSeverity.ERROR, 0);
			int warningCount = summary.bySeverity().getOrDefault(ViolationSeverity.WARNING, 0);
			int infoCount = summary.bySeverity().getOrDefault(ViolationSeverity.INFO, 0);

			List<String> parts = new ArrayList<>();
			if (errorCount > 0)
				parts.add(errorCount + " errors");
			if (warningCount > 0)
				parts.add(warningCount + " warnings");
			if (infoCount > 0)
				parts.add(infoCount + " info");

			StringBuilder output = new StringBuilder(128).
				append("\n... and ").append(summary.totalRemaining()).append(" more violations (").
				append(String.join(", ", parts)).append(")\n");

			// Top omitted rules
			List<Map.Entry<String, Integer>> topRules = summary.byRule().entrySet().stream().
				sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).
				limit(3).
				toList();

			if (!topRules.isEmpty())
			{
				List<String> ruleStrings = topRules.stream().
					map(entry -> entry.getKey() + " (" + entry.getValue() + ")").
					toList();
				output.append("Top omitted rules: ").append(String.join(", ", ruleStrings));
			}

			System.out.println(output);
		}
	}

	/**
	 * Detects the output format based on the execution environment.
	 * <p>
	 * Determines if the target audience is AI or human by checking:
	 * <ul>
	 *     <li>CLAUDE_SESSION_ID environment variable (AI indicator)</li>
	 *     <li>AI_AGENT_MODE environment variable</li>
	 *     <li>Whether stdout is a TTY (human interactive terminal)</li>
	 * </ul>
	 * <p>
	 * Returns appropriate OutputFormat:
	 * <ul>
	 *     <li>AI audience → JSON format</li>
	 *     <li>Human audience → HUMAN format</li>
	 * </ul>
	 *
	 * @return the detected output format
	 */
	public OutputFormat detectOutputFormat()
	{
		Audience audience = Audience.detect();
		return switch (audience)
		{
			case AI -> OutputFormat.JSON;
			case HUMAN -> OutputFormat.HUMAN;
		};
	}
}
