package io.github.cowwoc.styler.formatter.api.conflict;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Comprehensive report of all conflicts detected during formatting rule application.
 * <p>
 * Aggregates conflict information with statistical analysis and formatted output
 * for user consumption and programmatic processing.
 *
 * @param conflicts all detected conflicts, never {@code null}, may be empty
 * @param conflictsByRule conflict count per rule ID, never {@code null}, may be empty
 * @param maxSeverity highest severity level detected, never {@code null}
 * @param summary human-readable summary of conflicts, never {@code null}
 */
public record ConflictReport(List<Conflict> conflicts, Map<String, Integer> conflictsByRule,
	ConflictSeverity maxSeverity, String summary)
	implements Serializable
{
	/**
	 * Compact constructor with validation and defensive copying.
	 *
	 * @throws IllegalArgumentException if {@code conflicts}, {@code conflictsByRule}, {@code maxSeverity}, or
	 *     {@code summary} is null
	 */
	public ConflictReport
	{
		Objects.requireNonNull(conflicts, "conflicts cannot be null");
		Objects.requireNonNull(conflictsByRule, "conflictsByRule cannot be null");
		Objects.requireNonNull(maxSeverity, "maxSeverity cannot be null");
		Objects.requireNonNull(summary, "summary cannot be null");

		// Defensive copying to ensure immutability
		conflicts = List.copyOf(conflicts);
		conflictsByRule = Map.copyOf(conflictsByRule);
	}

	/**
	 * Checks if this report contains any conflicts.
	 *
	 * @return true if one or more conflicts detected, false otherwise
	 */
	public boolean hasConflicts()
	{
		return !conflicts.isEmpty();
	}

	/**
	 * Gets the total number of conflicts in this report.
	 *
	 * @return conflict count, never negative
	 */
	public int getConflictCount()
	{
		return conflicts.size();
	}
}
