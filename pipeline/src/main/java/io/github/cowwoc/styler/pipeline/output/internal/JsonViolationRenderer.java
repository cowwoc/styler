package io.github.cowwoc.styler.pipeline.output.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.cowwoc.styler.formatter.FixStrategy;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;
import io.github.cowwoc.styler.pipeline.output.ViolationReport;
import io.github.cowwoc.styler.pipeline.output.ViolationReportRenderer;

/**
 * Renders violation reports as JSON for AI agent consumption.
 * <p>
 * This implementation generates machine-readable JSON output with complete violation details,
 * severity summaries, and rule-based grouping. The JSON structure is designed
 * for reliable parsing by AI systems and CI/CD pipelines.
 * <p>
 * Uses simple StringBuilder-based JSON construction without external dependencies.
 * <p>
 * <b>Output Schema</b>:
 * <pre>
 * {
 *   "version": "1.0",
 *   "file": "path/to/File.java",
 *   "timestamp": "2025-12-05T10:30:00Z",
 *   "summary": {
 *     "totalViolations": 5,
 *     "errorCount": 1,
 *     "warningCount": 3,
 *     "infoCount": 1
 *   },
 *   "violations": [
 *     {
 *       "ruleId": "line-length",
 *       "severity": "WARNING",
 *       "message": "Line exceeds maximum length",
 *       "location": {
 *         "line": 42,
 *         "column": 121,
 *         "startPosition": 1234,
 *         "endPosition": 1378
 *       },
 *       "fixes": [
 *         {
 *           "description": "Wrap at method call boundary",
 *           "autoFixable": true
 *         }
 *       ]
 *     }
 *   ],
 *   "groupedByRule": {
 *     "line-length": {
 *       "count": 3
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class JsonViolationRenderer implements ViolationReportRenderer
{
	private static final String VERSION = "1.0";
	private static final int INDENT = 2;

	@Override
	public String render(ViolationReport report)
	{
		return new StringBuilder(2048).
			append("{\n").
			append(indent(INDENT)).
			append("\"version\": \"").append(VERSION).append("\",\n").
			append(indent(INDENT)).
			append("\"file\": \"").append(escapeJson(report.filePath().toString())).append("\",\n").
			append(indent(INDENT)).
			append("\"timestamp\": \"").append(ZonedDateTime.now(ZoneId.of("UTC"))).append("\",\n").
			append(indent(INDENT)).
			append("\"summary\": ").
			append(appendSummary(new StringBuilder(256), report, INDENT + 2)).
			append(",\n").
			append(indent(INDENT)).
			append("\"violations\": ").
			append(appendViolations(new StringBuilder(1024), report, INDENT + 2)).
			append(",\n").
			append(indent(INDENT)).
			append("\"groupedByRule\": ").
			append(appendGroupedByRule(new StringBuilder(512), report, INDENT + 2)).
			append('\n').
			append('}').
			toString();
	}

	@Override
	public OutputFormat supportedFormat()
	{
		return OutputFormat.JSON;
	}

	/**
	 * Returns an indentation string.
	 *
	 * @param spaces the number of spaces
	 * @return the indentation string
	 */
	private String indent(int spaces)
	{
		return " ".repeat(spaces);
	}

	/**
	 * Appends the summary statistics section.
	 *
	 * @param json   the output builder
	 * @param report the violation report
	 * @param spaces the indentation level
	 * @return the output builder for chaining
	 */
	private StringBuilder appendSummary(StringBuilder json, ViolationReport report, int spaces)
	{
		Map<ViolationSeverity, List<FormattingViolation>> bySeverity = report.groupedBySeverity();
		int errorCount = bySeverity.getOrDefault(ViolationSeverity.ERROR, List.of()).size();
		int warningCount = bySeverity.getOrDefault(ViolationSeverity.WARNING, List.of()).size();
		int infoCount = bySeverity.getOrDefault(ViolationSeverity.INFO, List.of()).size();

		return json.
			append("{\n").
			append(indent(spaces)).
			append("\"totalViolations\": ").append(report.violations().size()).append(",\n").
			append(indent(spaces)).
			append("\"errorCount\": ").append(errorCount).append(",\n").
			append(indent(spaces)).
			append("\"warningCount\": ").append(warningCount).append(",\n").
			append(indent(spaces)).
			append("\"infoCount\": ").append(infoCount).append('\n').
			append(indent(spaces - 2)).
			append('}');
	}

	/**
	 * Appends the violations array.
	 *
	 * @param json   the output builder
	 * @param report the violation report
	 * @param spaces the indentation level
	 * @return the output builder for chaining
	 */
	private StringBuilder appendViolations(StringBuilder json, ViolationReport report, int spaces)
	{
		json.append("[\n");
		List<FormattingViolation> violations = report.violations();
		for (int i = 0; i < violations.size(); ++i)
		{
			json.append(indent(spaces)).append("{\n");
			appendViolation(json, violations.get(i), spaces + 2);
			json.append(indent(spaces)).append('}');
			if (i < violations.size() - 1)
				json.append(',');
			json.append('\n');
		}
		return json.append(indent(spaces - 2)).append(']');
	}

	/**
	 * Appends a single violation object.
	 *
	 * @param json      the output builder
	 * @param violation the violation
	 * @param spaces    the indentation level
	 * @return the output builder for chaining
	 */
	private StringBuilder appendViolation(StringBuilder json, FormattingViolation violation, int spaces)
	{
		json.
			append(indent(spaces)).
			append("\"ruleId\": \"").append(escapeJson(violation.ruleId())).append("\",\n").
			append(indent(spaces)).
			append("\"severity\": \"").append(violation.severity().toString()).append("\",\n").
			append(indent(spaces)).
			append("\"message\": \"").append(escapeJson(violation.message())).append("\",\n");

		appendLocation(json, violation, spaces);
		appendFixes(json, violation, spaces);
		return json;
	}

	/**
	 * Appends the location object.
	 *
	 * @param json      the output builder
	 * @param violation the violation
	 * @param spaces    the indentation level
	 * @return the output builder for chaining
	 */
	private StringBuilder appendLocation(StringBuilder json, FormattingViolation violation, int spaces)
	{
		return json.
			append(indent(spaces)).
			append("\"location\": {\n").
			append(indent(spaces + 2)).
			append("\"line\": ").append(violation.lineNumber()).append(",\n").
			append(indent(spaces + 2)).
			append("\"column\": ").append(violation.columnNumber()).append(",\n").
			append(indent(spaces + 2)).
			append("\"startPosition\": ").append(violation.startPosition()).append(",\n").
			append(indent(spaces + 2)).
			append("\"endPosition\": ").append(violation.endPosition()).append('\n').
			append(indent(spaces)).
			append("},\n");
	}

	/**
	 * Appends the fixes array.
	 *
	 * @param json      the output builder
	 * @param violation the violation
	 * @param spaces    the indentation level
	 * @return the output builder for chaining
	 */
	private StringBuilder appendFixes(StringBuilder json, FormattingViolation violation, int spaces)
	{
		json.append(indent(spaces)).append("\"fixes\": [\n");
		List<FixStrategy> fixes = violation.suggestedFixes();
		for (int i = 0; i < fixes.size(); ++i)
		{
			FixStrategy fix = fixes.get(i);
			json.
				append(indent(spaces + 2)).
				append("{\n").
				append(indent(spaces + 4)).
				append("\"description\": \"").append(escapeJson(fix.description())).append("\",\n").
				append(indent(spaces + 4)).
				append("\"autoFixable\": ").append(fix.isAutoApplicable()).append('\n').
				append(indent(spaces + 2)).
				append('}');
			if (i < fixes.size() - 1)
				json.append(',');
			json.append('\n');
		}
		return json.append(indent(spaces)).append("]\n");
	}

	/**
	 * Appends violations grouped by rule.
	 *
	 * @param json   the output builder
	 * @param report the violation report
	 * @param spaces the indentation level
	 * @return the output builder for chaining
	 */
	private StringBuilder appendGroupedByRule(StringBuilder json, ViolationReport report, int spaces)
	{
		Map<String, List<FormattingViolation>> byRule = report.groupedByRule();
		Map<String, Integer> ruleCounts = report.ruleCounts();

		json.append("{\n");
		List<String> sortedRules = byRule.keySet().stream().
			sorted((a, b) -> Integer.compare(
				ruleCounts.getOrDefault(b, 0),
				ruleCounts.getOrDefault(a, 0))).
			collect(Collectors.toList());

		for (int i = 0; i < sortedRules.size(); ++i)
		{
			String ruleId = sortedRules.get(i);
			int count = byRule.get(ruleId).size();

			json.
				append(indent(spaces)).
				append('"').append(escapeJson(ruleId)).append("\": {\n").
				append(indent(spaces + 2)).
				append("\"count\": ").append(count).append('\n').
				append(indent(spaces)).
				append('}');
			if (i < sortedRules.size() - 1)
				json.append(',');
			json.append('\n');
		}
		return json.append(indent(spaces - 2)).append('}');
	}

	/**
	 * Escapes special characters in JSON strings.
	 *
	 * @param str the string to escape
	 * @return the escaped string
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
}
