package io.github.cowwoc.styler.pipeline.output.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.github.cowwoc.styler.formatter.FixStrategy;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;
import io.github.cowwoc.styler.pipeline.output.ViolationReport;
import io.github.cowwoc.styler.pipeline.output.ViolationReportRenderer;

/**
 * Renders violation reports in human-readable plain text format.
 * <p>
 * This implementation generates readable terminal output with color codes (ANSI),
 * organized sections, and helpful context. The output is designed for developers
 * reviewing violations during local development and CI log inspection.
 * <p>
 * Output includes:
 * <ul>
 *   <li>File path and summary statistics</li>
 *   <li>Violations grouped by severity (errors, warnings, infos)</li>
 *   <li>Location information and messages</li>
 *   <li>Suggested fixes with auto-fixable indicators</li>
 *   <li>Violation counts for fix ordering guidance</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class HumanViolationRenderer implements ViolationReportRenderer
{
	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_BOLD = "\u001B[1m";

	@Override
	public String render(ViolationReport report)
	{
		return appendHeader(new StringBuilder(1024), report).
			append('\n').
			append(appendSummary(new StringBuilder(256), report)).
			append('\n').
			append(appendViolationsBySeverity(new StringBuilder(512), report)).
			append(appendRuleSummary(new StringBuilder(256), report)).
			toString();
	}

	@Override
	public OutputFormat supportedFormat()
	{
		return OutputFormat.HUMAN;
	}

	/**
	 * Appends the header with file path.
	 *
	 * @param output the output builder
	 * @param report the violation report
	 * @return the output builder for chaining
	 */
	private StringBuilder appendHeader(StringBuilder output, ViolationReport report)
	{
		return output.
			append(ANSI_BOLD).
			append("Violations Report").
			append(ANSI_RESET).
			append('\n').
			append("File: ").
			append(report.filePath()).
			append('\n');
	}

	/**
	 * Appends summary statistics to the output.
	 *
	 * @param output the output builder
	 * @param report the violation report
	 * @return the output builder for chaining
	 */
	private StringBuilder appendSummary(StringBuilder output, ViolationReport report)
	{
		List<FormattingViolation> violations = report.violations();
		Map<ViolationSeverity, List<FormattingViolation>> bySeverity = report.groupedBySeverity();

		return output.
			append(ANSI_BOLD).
			append("Summary").
			append(ANSI_RESET).
			append('\n').
			append("  Total violations: ").
			append(violations.size()).
			append('\n').
			append("  ").
			append(ANSI_RED).
			append("Errors: ").
			append(bySeverity.getOrDefault(ViolationSeverity.ERROR, List.of()).size()).
			append(ANSI_RESET).
			append('\n').
			append("  ").
			append(ANSI_YELLOW).
			append("Warnings: ").
			append(bySeverity.getOrDefault(ViolationSeverity.WARNING, List.of()).size()).
			append(ANSI_RESET).
			append('\n').
			append("  ").
			append(ANSI_BLUE).
			append("Infos: ").
			append(bySeverity.getOrDefault(ViolationSeverity.INFO, List.of()).size()).
			append(ANSI_RESET).
			append('\n');
	}

	/**
	 * Appends violations grouped by severity to the output.
	 *
	 * @param output the output builder
	 * @param report the violation report
	 * @return the output builder for chaining
	 */
	private StringBuilder appendViolationsBySeverity(StringBuilder output, ViolationReport report)
	{
		Map<ViolationSeverity, List<FormattingViolation>> bySeverity = report.groupedBySeverity();
		Map<String, Integer> ruleCounts = report.ruleCounts();

		// Order: ERROR, WARNING, INFO (descending severity)
		for (ViolationSeverity severity : List.of(
			ViolationSeverity.ERROR,
			ViolationSeverity.WARNING,
			ViolationSeverity.INFO))
		{
			List<FormattingViolation> violations = bySeverity.getOrDefault(severity, List.of());
			if (violations.isEmpty())
			{
				continue;
			}

			appendSeveritySection(output, severity, violations, ruleCounts);
		}
		return output;
	}

	/**
	 * Appends a severity section with its violations.
	 *
	 * @param output     the output builder
	 * @param severity   the severity level
	 * @param violations the violations for this severity
	 * @param ruleCounts the violation counts by rule
	 * @return the output builder for chaining
	 */
	private StringBuilder appendSeveritySection(StringBuilder output, ViolationSeverity severity,
		List<FormattingViolation> violations, Map<String, Integer> ruleCounts)
	{
		String colorCode = switch (severity)
		{
			case ERROR -> ANSI_RED;
			case WARNING -> ANSI_YELLOW;
			case INFO -> ANSI_BLUE;
		};

		output.
			append('\n').
			append(colorCode).
			append(ANSI_BOLD).
			append(severity.toString()).
			append('S').
			append(ANSI_RESET).
			append('\n').
			append("-".repeat(40)).
			append('\n');

		// Sort violations by count descending
		List<FormattingViolation> sorted = violations.stream().
			sorted(Comparator.comparing(
				(FormattingViolation v) -> ruleCounts.getOrDefault(v.ruleId(), 0),
				Comparator.reverseOrder())).
			toList();

		for (FormattingViolation violation : sorted)
		{
			appendViolationDetail(output, violation);
		}
		return output;
	}

	/**
	 * Appends details for a single violation to the output.
	 *
	 * @param output    the output builder
	 * @param violation the violation to detail
	 * @return the output builder for chaining
	 */
	private StringBuilder appendViolationDetail(StringBuilder output, FormattingViolation violation)
	{
		output.
			append("  ").
			append(ANSI_BOLD).
			append('[').
			append(violation.ruleId()).
			append(']').
			append(ANSI_RESET).
			append(" at line ").
			append(violation.lineNumber()).
			append(':').
			append(violation.columnNumber()).
			append('\n').
			append("    Message: ").
			append(violation.message()).
			append('\n');

		appendSuggestedFixes(output, violation);
		return output.append('\n');
	}

	/**
	 * Appends suggested fixes for a violation.
	 *
	 * @param output    the output builder
	 * @param violation the violation with fixes
	 * @return the output builder for chaining
	 */
	private StringBuilder appendSuggestedFixes(StringBuilder output, FormattingViolation violation)
	{
		List<FixStrategy> fixes = violation.suggestedFixes();
		if (fixes.isEmpty())
		{
			return output;
		}

		output.append("    Fixes:\n");
		for (FixStrategy fix : fixes)
		{
			output.append("      - ").append(fix.description());
			if (fix.isAutoApplicable())
			{
				output.append(" [auto-fixable]");
			}
			output.append('\n');
		}
		return output;
	}

	/**
	 * Appends a summary of violations grouped by rule to the output.
	 *
	 * @param output the output builder
	 * @param report the violation report
	 * @return the output builder for chaining
	 */
	private StringBuilder appendRuleSummary(StringBuilder output, ViolationReport report)
	{
		Map<String, List<FormattingViolation>> byRule = report.groupedByRule();
		Map<String, Integer> ruleCounts = report.ruleCounts();

		output.
			append(ANSI_BOLD).
			append("By Rule").
			append(ANSI_RESET).
			append('\n').
			append("-".repeat(40)).
			append('\n');

		byRule.entrySet().stream().
			sorted((a, b) -> Integer.compare(
				ruleCounts.getOrDefault(b.getKey(), 0),
				ruleCounts.getOrDefault(a.getKey(), 0))).
			forEach(entry -> output.append(String.format("  %-30s %3d violations%n",
				entry.getKey(), entry.getValue().size())));

		return output.append('\n');
	}
}
