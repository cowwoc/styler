package io.github.cowwoc.styler.formatter.api.conflict;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of conflict detection using pairwise source range comparison.
 * <p>
 * This detector uses an O(n²) algorithm to compare all pairs of pending modifications
 * for overlapping source ranges. While not optimal for very large modification counts,
 * this approach is simple, correct, and sufficient for typical formatting scenarios
 * where n  100.
 * <p>
 * This implementation is stateless and thread-safe, making it suitable for concurrent
 * file processing.
 */
public final class DefaultConflictDetector implements ConflictDetector
{
	/**
	 * Maximum number of pending modifications to analyze before triggering resource limit protection.
	 * <p>
	 * Based on Java Language Specification §14.4 nesting limits and empirical performance testing.
	 * See: <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.4">JLS §14.4</a>
	 */
	private static final int MAX_PENDING_MODIFICATIONS = 10_000;

	/**
	 * Maximum number of conflicts to track before aborting analysis.
	 * <p>
	 * Prevents resource exhaustion when facing pathological conflict scenarios.
	 */
	private static final int MAX_CONFLICTS_TO_ANALYZE = 1_000;

	@Override
	public List<Conflict> detectConflicts(List<PendingModification> modifications)
	{
		Objects.requireNonNull(modifications, "modifications cannot be null");

		// Fast path: 0 or 1 modifications cannot have conflicts
		if (modifications.size() <= 1)
		{
			return List.of();
		}

		// Resource limit protection
		if (modifications.size() > MAX_PENDING_MODIFICATIONS)
		{
			throw new IllegalArgumentException("Too many pending modifications: " +
				modifications.size() + " (maximum: " + MAX_PENDING_MODIFICATIONS + ")");
		}

		// Validate no null elements
		for (int i = 0; i < modifications.size(); ++i)
		{
			if (modifications.get(i) == null)
			{
				throw new NullPointerException("modifications contains null element at index " + i);
			}
		}

		List<Conflict> conflicts = new ArrayList<>();

		// Pairwise comparison: check each modification against all subsequent ones
		for (int i = 0; i < modifications.size(); ++i)
		{
			PendingModification first = modifications.get(i);

			for (int j = i + 1; j < modifications.size(); ++j)
			{
				PendingModification second = modifications.get(j);

				if (hasOverlap(first.edit(), second.edit()))
				{
					ConflictSeverity severity = computeSeverityForEdits(first, second);
					String description = buildConflictDescription(first, second, severity);
					Conflict conflict = new Conflict(first, second, severity, description);
					conflicts.add(conflict);

					// Resource limit protection
					if (conflicts.size() > MAX_CONFLICTS_TO_ANALYZE)
					{
						throw new IllegalStateException("Too many conflicts detected: " +
							conflicts.size() + " (maximum: " + MAX_CONFLICTS_TO_ANALYZE + ")");
					}
				}
			}
		}

		return conflicts;
	}

	@Override
	public ConflictSeverity computeSeverity(Conflict conflict)
	{
		Objects.requireNonNull(conflict, "conflict cannot be null");
		return conflict.severity();
	}

	/**
	 * Checks if two text edits have overlapping source ranges.
	 *
	 * @param first first text edit, never {@code null}
	 * @param second second text edit, never {@code null}
	 * @return true if edits overlap, false otherwise
	 */
	private boolean hasOverlap(TextEdit first, TextEdit second)
	{
		// Two ranges overlap if they share any common positions
		// Range [a, b) overlaps [c, d) if: a < d AND c < b
		return first.getRange().start().compareTo(second.getRange().end()) < 0 &&
			second.getRange().start().compareTo(first.getRange().end()) < 0;
	}

	/**
	 * Computes conflict severity based on modification characteristics.
	 *
	 * @param first first pending modification, never {@code null}
	 * @param second second pending modification, never {@code null}
	 * @return severity classification, never {@code null}
	 */
	private ConflictSeverity computeSeverityForEdits(PendingModification first, PendingModification second)
	{
		// Same rule creating multiple edits at same location: SEVERE
		if (first.ruleId().equals(second.ruleId()))
		{
			return ConflictSeverity.SEVERE;
		}

		// Complete overlap (same range): SEVERE
		if (first.edit().getRange().equals(second.edit().getRange()))
		{
			return ConflictSeverity.SEVERE;
		}

		// Partial overlap: MODERATE
		return ConflictSeverity.MODERATE;
	}

	/**
	 * Builds a human-readable description of the conflict.
	 *
	 * @param first first pending modification, never {@code null}
	 * @param second second pending modification, never {@code null}
	 * @param severity conflict severity, never {@code null}
	 * @return formatted description, never {@code null}
	 */
	private String buildConflictDescription(PendingModification first, PendingModification second,
		ConflictSeverity severity)
	{
		SourcePosition overlapStart;
		if (first.edit().getRange().start().compareTo(second.edit().getRange().start()) > 0)
		{
			overlapStart = first.edit().getRange().start();
		}
		else
		{
			overlapStart = second.edit().getRange().start();
		}

		SourcePosition overlapEnd;
		if (first.edit().getRange().end().compareTo(second.edit().getRange().end()) < 0)
		{
			overlapEnd = first.edit().getRange().end();
		}
		else
		{
			overlapEnd = second.edit().getRange().end();
		}

		return String.format("Conflict (%s): Rule '%s' (priority %d) and rule '%s' (priority %d) " +
				"both modify overlapping range [line %d, col %d] to [line %d, col %d]",
			severity,
			first.ruleId(),
			first.priority(),
			second.ruleId(),
			second.priority(),
			overlapStart.line(),
			overlapStart.column(),
			overlapEnd.line(),
			overlapEnd.column());
	}
}
