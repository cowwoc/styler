package io.github.cowwoc.styler.formatter.api.conflict;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolution strategy that immediately rejects any conflict by throwing an exception.
 * <p>
 * This strategy enforces strict conflict-free formatting by refusing to resolve any
 * conflicts. It is useful in validation modes where overlapping modifications indicate
 * a configuration error or rule incompatibility that must be addressed.
 * <p>
 * Unlike other strategies that attempt to choose between conflicting modifications,
 * this strategy treats all conflicts as unrecoverable errors requiring human intervention.
 * <p>
 * This implementation is stateless and thread-safe.
 */
public final class FailFastResolutionStrategy implements ResolutionStrategy
{
	@Override
	public boolean canResolve(Conflict conflict)
	{
		Objects.requireNonNull(conflict, "conflict cannot be null");
		// Fail-fast strategy cannot resolve any conflicts
		return false;
	}

	@Override
	public ResolutionDecision resolve(Conflict conflict) throws ConflictResolutionException
	{
		Objects.requireNonNull(conflict, "conflict cannot be null");

		ConflictReport report = new ConflictReport(
			List.of(conflict),
			Map.of(conflict.first().ruleId(), 1, conflict.second().ruleId(), 1),
			conflict.severity(),
			String.format("Fail-fast policy: %s", conflict.description()));

		throw new ConflictResolutionException(
			String.format("Fail-fast strategy rejects all conflicts: %s", conflict.description()),
			report);
	}
}
