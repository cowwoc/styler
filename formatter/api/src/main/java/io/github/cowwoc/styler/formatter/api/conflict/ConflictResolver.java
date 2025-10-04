package io.github.cowwoc.styler.formatter.api.conflict;

import java.util.List;

/**
 * Orchestrates conflict detection and resolution for pending AST modifications.
 * <p>
 * A conflict resolver coordinates between a {@link ConflictDetector} to identify
 * overlapping modifications and a {@link ResolutionStrategy} to determine which
 * modifications should be applied. The complete workflow:
 * <ol>
 * <li>Detection: Analyze modifications for conflicts using configured detector</li>
 * <li>Resolution: Apply configured strategy to each detected conflict</li>
 * <li>Decision: Return final decision listing modifications to apply/discard</li>
 * </ol>
 * <p>
 * Implementations must be stateless and thread-safe to support concurrent file
 * processing during parallel formatting operations.
 *
 * @see ConflictDetector
 * @see ResolutionStrategy
 * @see ResolutionDecision
 */
public interface ConflictResolver
{
	/**
	 * Resolves conflicts between pending modifications using configured strategy.
	 * <p>
	 * Orchestrates the complete resolution workflow:
	 * <ol>
	 * <li>Detects conflicts using configured {@link ConflictDetector}</li>
	 * <li>Applies configured {@link ResolutionStrategy} to each conflict</li>
	 * <li>Builds final {@link ResolutionDecision} with modifications to apply/discard</li>
	 * </ol>
	 * <p>
	 * The returned decision includes:
	 * <ul>
	 * <li>{@code toApply}: Modifications that should be applied to AST</li>
	 * <li>{@code toDiscard}: Modifications that lost conflict resolution</li>
	 * <li>{@code rationale}: Human-readable explanation of resolution choices</li>
	 * </ul>
	 *
	 * @param modifications the modifications to analyze for conflicts, never {@code null}
	 * @return resolution decision indicating which modifications to apply/discard, never {@code null}
	 * @throws ConflictResolutionException if conflicts cannot be resolved by configured strategy
	 * @throws IllegalArgumentException if {@code modifications} is null or contains null elements
	 * @throws IllegalStateException if resource limits exceeded (MAX_PENDING_MODIFICATIONS)
	 */
	ResolutionDecision resolve(List<PendingModification> modifications)
		throws ConflictResolutionException;

	/**
	 * Returns the conflict detector used by this resolver.
	 *
	 * @return the configured conflict detector, never {@code null}
	 */
	ConflictDetector getDetector();

	/**
	 * Returns the resolution strategy used by this resolver.
	 *
	 * @return the configured resolution strategy, never {@code null}
	 */
	ResolutionStrategy getStrategy();
}
