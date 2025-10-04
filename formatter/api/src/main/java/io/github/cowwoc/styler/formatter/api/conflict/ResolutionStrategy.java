package io.github.cowwoc.styler.formatter.api.conflict;

/**
 * Strategy for resolving conflicts between pending AST modifications.
 * <p>
 * When multiple formatting rules attempt to modify overlapping source regions,
 * a resolution strategy determines which modifications should be applied and which
 * should be discarded. Different strategies implement different resolution policies:
 * <ul>
 * <li>Priority-based: Use rule priority ordering</li>
 * <li>Merge: Combine compatible modifications</li>
 * <li>Fail-fast: Throw exception on any conflict</li>
 * </ul>
 * <p>
 * Implementations must be stateless and thread-safe to support concurrent file processing.
 */
public interface ResolutionStrategy
{
	/**
	 * Checks if this strategy can resolve the given conflict.
	 * <p>
	 * Some strategies may only be applicable to certain types of conflicts.
	 * For example, a merge strategy cannot resolve conflicts between incompatible
	 * modifications.
	 *
	 * @param conflict the conflict to check, never {@code null}
	 * @return true if this strategy can resolve the conflict, false otherwise
	 * @throws IllegalArgumentException if {@code conflict} is null
	 */
	boolean canResolve(Conflict conflict);

	/**
	 * Resolves a conflict according to this strategy's rules.
	 * <p>
	 * Determines which of the conflicting modifications should be applied and which
	 * should be discarded. The resolution decision includes a rationale explaining
	 * why the strategy made its choice.
	 *
	 * @param conflict the conflict to resolve, never {@code null}
	 * @return resolution decision indicating which modifications to apply/discard, never {@code null}
	 * @throws ConflictResolutionException if the conflict cannot be resolved by this strategy
	 * @throws IllegalArgumentException if {@code conflict} is null
	 */
	ResolutionDecision resolve(Conflict conflict) throws ConflictResolutionException;
}
