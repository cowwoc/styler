package io.github.cowwoc.styler.formatter.api;

import io.github.cowwoc.styler.formatter.api.report.ViolationReport;
import io.github.cowwoc.styler.formatter.api.report.ViolationReportGenerator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the result of applying a formatting rule to source code.
 * <p>
 * This immutable value object contains the text edits that should be applied,
 * any violations found, and performance metrics from the rule execution.
 */
public final class FormattingResult
{
	private static final FormattingResult EMPTY = new FormattingResult(List.of(), List.of(), Map.of());

	private final List<TextEdit> edits;
	private final List<FormattingViolation> violations;
	private final Map<String, Object> metrics;

	/**
	 * Creates a new formatting result.
	 *
	 * @param edits      the list of text edits to apply, never {@code null}
	 * @param violations the list of formatting violations found, never {@code null}
	 * @param metrics    performance and diagnostic metrics, never {@code null}
	 */
	public FormattingResult(List<TextEdit> edits,
	                        List<FormattingViolation> violations,
	                        Map<String, Object> metrics)
	{
		this.edits = List.copyOf(Objects.requireNonNull(edits, "Edits cannot be null"));
		this.violations = List.copyOf(Objects.requireNonNull(violations, "Violations cannot be null"));
		this.metrics = Map.copyOf(Objects.requireNonNull(metrics, "Metrics cannot be null"));
	}

	/**
	 * Creates an empty formatting result with no edits or violations.
	 *
	 * @return an empty formatting result, never {@code null}
	 */
		public static FormattingResult empty()
	{
		return EMPTY;
	}

	/**
	 * Creates a formatting result with only text edits.
	 *
	 * @param edits the list of text edits, never {@code null}
	 * @return a formatting result with the specified edits, never {@code null}
	 */
		public static FormattingResult withEdits(List<TextEdit> edits)
	{
		return new FormattingResult(edits, List.of(), Map.of());
	}

	/**
	 * Creates a formatting result with only violations (no edits).
	 *
	 * @param violations the list of formatting violations, never {@code null}
	 * @return a formatting result with the specified violations, never {@code null}
	 */
		public static FormattingResult withViolations(List<FormattingViolation> violations)
	{
		return new FormattingResult(List.of(), violations, Map.of());
	}

	/**
	 * Returns the list of text edits that should be applied to the source code.
	 * <p>
	 * These edits represent the changes needed to conform to the formatting rule.
	 * They are ordered by position in the source file and should be applied
	 * in reverse order to maintain correct positions.
	 *
	 * @return the list of text edits, never {@code null} but may be empty
	 */
		public List<TextEdit> getEdits()
	{
		return edits;
	}

	/**
	 * Returns the list of formatting violations found by the rule.
	 * <p>
	 * Violations represent issues that were detected but could not be
	 * automatically fixed, or rules configured to only report issues.
	 *
	 * @return the list of violations, never {@code null} but may be empty
	 */
		public List<FormattingViolation> getViolations()
	{
		return violations;
	}

	/**
	 * Returns performance and diagnostic metrics from the rule execution.
	 * <p>
	 * Metrics can include execution time, memory usage, number of AST nodes
	 * processed, or other diagnostic information useful for monitoring and
	 * optimization.
	 *
	 * @return the metrics map, never {@code null} but may be empty
	 */
		public Map<String, Object> getMetrics()
	{
		return metrics;
	}

	/**
	 * Returns whether this result contains any text edits.
	 *
	 * @return {@code true} if there are text edits, {@code false} otherwise
	 */
	public boolean hasEdits()
	{
		return !edits.isEmpty();
	}

	/**
	 * Returns whether this result contains any violations.
	 *
	 * @return {@code true} if there are violations, {@code false} otherwise
	 */
	public boolean hasViolations()
	{
		return !violations.isEmpty();
	}

	/**
	 * Generates a structured violation report from this formatting result.
	 * <p>
	 * Creates a {@link ViolationReport} containing all violations with priority scores, statistics,
	 * and metadata. The report can be serialized to JSON or XML for AI agent consumption. If this
	 * result has no violations, returns an empty report.
	 * </p>
	 * <h2>Usage Example</h2>
	 * <pre>{@code
	 * FormattingResult result = ...;
	 * ViolationReport report = result.generateReport();
	 * String json = new JsonViolationSerializer().serialize(report);
	 * }</pre>
	 *
	 * @return the violation report, never {@code null}
	 * @see ViolationReport
	 * @see io.github.cowwoc.styler.formatter.api.report.JsonViolationSerializer
	 * @see io.github.cowwoc.styler.formatter.api.report.XmlViolationSerializer
	 */
	public ViolationReport generateReport()
	{
		return ViolationReportGenerator.generate(violations);
	}

	/**
	 * Returns whether this result is empty (no edits, violations, or metrics).
	 *
	 * @return {@code true} if the result is empty, {@code false} otherwise
	 */
	public boolean isEmpty()
	{
		return edits.isEmpty() && violations.isEmpty() && metrics.isEmpty();
	}

	/**
	 * Retrieves a metric value with type safety.
	 *
	 * @param key  the metric key, never {@code null}
	 * @param type the expected type of the metric value, never {@code null}
	 * @param <T>  the type parameter
	 * @return the metric value cast to the specified type, or {@code null} if not present
	 * @throws ClassCastException if the value cannot be cast to the specified type
	 */
	public <T> T getMetric(String key, Class<T> type)
	{
		Object value = metrics.get(key);
		if (value != null)
		{
			return type.cast(value);
		}
		return null;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		FormattingResult that = (FormattingResult) obj;
		return Objects.equals(edits, that.edits) &&
			Objects.equals(violations, that.violations) &&
			Objects.equals(metrics, that.metrics);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(edits, violations, metrics);
	}

	@Override
	public String toString()
	{
		return "FormattingResult{" +
			"edits=" + edits.size() +
			", violations=" + violations.size() +
			", metrics=" + metrics.keySet() +
			'}';
	}
}