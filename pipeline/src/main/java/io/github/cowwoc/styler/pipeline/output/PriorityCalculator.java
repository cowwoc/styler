package io.github.cowwoc.styler.pipeline.output;

import java.util.List;
import java.util.Map;

import io.github.cowwoc.styler.formatter.FormattingViolation;

/**
 * Interface for counting violations grouped by rule.
 * <p>
 * Violation counts help identify which rules have the most violations,
 * allowing AI agents to prioritize fixes that address the largest number of issues.
 * <p>
 * <b>Thread-safety</b>: Implementations must be immutable and thread-safe.
 */
@FunctionalInterface
public interface PriorityCalculator
{
	/**
	 * Counts violations grouped by rule.
	 *
	 * @param violations the formatting violations to count
	 * @return map from rule ID to violation count, or empty map if no violations
	 * @throws NullPointerException if {@code violations} is {@code null}
	 */
	Map<String, Integer> countByRule(List<FormattingViolation> violations);
}
