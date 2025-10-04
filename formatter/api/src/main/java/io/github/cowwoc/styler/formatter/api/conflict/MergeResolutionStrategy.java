package io.github.cowwoc.styler.formatter.api.conflict;

import io.github.cowwoc.styler.formatter.api.TextEdit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves conflicts by attempting to merge compatible modifications.
 * <p>
 * This strategy takes a conservative approach to merging modifications:
 * <ul>
 * <li>If modifications have non-overlapping source ranges, both are applied</li>
 * <li>If modifications overlap but have identical replacement text, both are applied</li>
 * <li>Otherwise, the conflict cannot be resolved and an exception is thrown</li>
 * </ul>
 * <p>
 * This strategy prioritizes correctness over aggressive merging, ensuring that
 * incompatible modifications are never combined in ways that could corrupt the AST.
 * <p>
 * This implementation is stateless and thread-safe.
 */
public final class MergeResolutionStrategy implements ResolutionStrategy
{
	@Override
	public boolean canResolve(Conflict conflict)
	{
		Objects.requireNonNull(conflict, "conflict cannot be null");
		return areModificationsCompatible(conflict.first(), conflict.second());
	}

	@Override
	public ResolutionDecision resolve(Conflict conflict) throws ConflictResolutionException
	{
		Objects.requireNonNull(conflict, "conflict cannot be null");

		if (!canResolve(conflict))
		{
			ConflictReport report = new ConflictReport(
				List.of(conflict),
				Map.of(conflict.first().ruleId(), 1, conflict.second().ruleId(), 1),
				conflict.severity(),
				String.format("Cannot merge %s conflict between rules '%s' and '%s'",
					conflict.severity(), conflict.first().ruleId(), conflict.second().ruleId()));
			throw new ConflictResolutionException(
				String.format("Cannot merge incompatible modifications: rule '%s' and rule '%s' " +
					"have overlapping ranges with different replacement text",
					conflict.first().ruleId(), conflict.second().ruleId()),
				report);
		}

		PendingModification first = conflict.first();
		PendingModification second = conflict.second();

		// If modifications don't actually overlap, keep both
		if (!hasOverlap(first.edit(), second.edit()))
		{
			return new ResolutionDecision(
				List.of(first, second),
				List.of(),
				String.format("Non-overlapping modifications from rules '%s' and '%s' merged successfully",
					first.ruleId(), second.ruleId()));
		}

		// If modifications overlap but have identical replacement text, keep both
		// (This handles cases where multiple rules produce the same fix)
		if (first.edit().getReplacement().equals(second.edit().getReplacement()))
		{
			return new ResolutionDecision(
				List.of(first, second),
				List.of(),
				String.format("Rules '%s' and '%s' produce identical replacement text; both applied",
					first.ruleId(), second.ruleId()));
		}

		// Should not reach here if canResolve() works correctly
		ConflictReport report = new ConflictReport(
			List.of(conflict),
			Map.of(conflict.first().ruleId(), 1, conflict.second().ruleId(), 1),
			conflict.severity(),
			"Internal error: canResolve() logic inconsistency");
		throw new ConflictResolutionException(
			"Internal error: canResolve() returned true but modifications are incompatible",
			report);
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
	 * Checks if two modifications can be safely merged.
	 * <p>
	 * Modifications are compatible if:
	 * <ul>
	 * <li>They have non-overlapping source ranges, OR</li>
	 * <li>They have overlapping ranges but identical replacement text</li>
	 * </ul>
	 *
	 * @param first first pending modification, never {@code null}
	 * @param second second pending modification, never {@code null}
	 * @return true if modifications can be merged, false otherwise
	 */
	private boolean areModificationsCompatible(PendingModification first, PendingModification second)
	{
		// Case 1: Non-overlapping modifications can always be merged
		// Case 2: Overlapping modifications with identical text can be merged (multiple rules producing same fix)
		return !hasOverlap(first.edit(), second.edit()) ||
			first.edit().getReplacement().equals(second.edit().getReplacement());
	}
}
