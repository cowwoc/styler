package io.github.cowwoc.styler.formatter.api.conflict;

import java.util.List;
import java.util.Objects;

/**
 * Immutable result of conflict resolution indicating which modifications to apply or discard.
 * <p>
 * After detecting conflicts between pending modifications, a resolution strategy
 * decides which modifications should be applied to the AST and which should be discarded.
 *
 * @param toApply modifications that should be applied to the AST, never {@code null}, may be empty
 * @param toDiscard modifications that should not be applied due to conflicts, never {@code null}, may be empty
 * @param rationale human-readable explanation of the resolution decision, never {@code null}
 */
public record ResolutionDecision(
	List<PendingModification> toApply,
	List<PendingModification> toDiscard,
	String rationale)
{
	/**
	 * Compact constructor with validation and defensive copying.
	 *
	 * @throws IllegalArgumentException if {@code toApply}, {@code toDiscard}, or {@code rationale} is null
	 */
	public ResolutionDecision
	{
		Objects.requireNonNull(toApply, "toApply cannot be null");
		Objects.requireNonNull(toDiscard, "toDiscard cannot be null");
		Objects.requireNonNull(rationale, "rationale cannot be null");

		// Defensive copying to ensure immutability
		toApply = List.copyOf(toApply);
		toDiscard = List.copyOf(toDiscard);
	}

	/**
	 * Gets the total number of modifications in this decision.
	 *
	 * @return sum of toApply and toDiscard counts, never negative
	 */
	public int getTotalModifications()
	{
		return toApply.size() + toDiscard.size();
	}
}
