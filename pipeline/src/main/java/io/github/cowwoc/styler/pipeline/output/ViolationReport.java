package io.github.cowwoc.styler.pipeline.output;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;

/**
 * Immutable aggregation of formatting violations with grouping and counts.
 * <p>
 * This record provides convenience methods for organizing violations by rule, severity,
 * and count for output formatting and analysis.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param filePath the source file path containing violations
 * @param violations the list of all formatting violations
 * @param ruleCounts map of rule IDs to violation counts
 */
public record ViolationReport(
	Path filePath,
	List<FormattingViolation> violations,
	Map<String, Integer> ruleCounts)
{
	/**
	 * Creates a ViolationReport with validation of all parameters.
	 *
	 * @param filePath the source file path containing violations
	 * @param violations the list of all formatting violations
	 * @param ruleCounts map of rule IDs to violation counts
	 * @throws NullPointerException if any argument is null
	 */
	public ViolationReport
	{
		requireThat(filePath, "filePath").isNotNull();
		requireThat(violations, "violations").isNotNull();
		requireThat(ruleCounts, "ruleCounts").isNotNull();

		violations = List.copyOf(violations);
		ruleCounts = Map.copyOf(ruleCounts);
	}

	/**
	 * Returns an immutable list of all violations in this report.
	 *
	 * @return the list of violations
	 */
	public List<FormattingViolation> violations()
	{
		return violations;
	}

	/**
	 * Returns violations grouped by rule identifier.
	 *
	 * @return map from rule ID to list of violations for that rule
	 */
	public Map<String, List<FormattingViolation>> groupedByRule()
	{
		return violations.stream().
			collect(Collectors.groupingBy(FormattingViolation::ruleId,
				Collectors.toUnmodifiableList()));
	}

	/**
	 * Returns violations grouped by severity level.
	 *
	 * @return map from severity level to list of violations with that severity
	 */
	public Map<ViolationSeverity, List<FormattingViolation>> groupedBySeverity()
	{
		return violations.stream().
			collect(Collectors.groupingBy(FormattingViolation::severity,
				Collectors.toUnmodifiableList()));
	}

	/**
	 * Returns the violation counts for each rule.
	 *
	 * @return immutable map from rule ID to violation count
	 */
	public Map<String, Integer> ruleCounts()
	{
		return ruleCounts;
	}

	/**
	 * Returns violations sorted by count in descending order.
	 * Rules with more violations appear first.
	 *
	 * @return list of violations sorted by count descending
	 */
	public List<FormattingViolation> sortedByCount()
	{
		return violations.stream().
			sorted(Comparator.comparing(
				(FormattingViolation v) -> ruleCounts.getOrDefault(v.ruleId(), 0),
				Comparator.reverseOrder())).
			collect(Collectors.toUnmodifiableList());
	}
}
