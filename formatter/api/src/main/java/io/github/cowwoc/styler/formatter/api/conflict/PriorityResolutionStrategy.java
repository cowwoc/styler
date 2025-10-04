package io.github.cowwoc.styler.formatter.api.conflict;

import java.util.List;
import java.util.Objects;

/**
 * Resolves conflicts using rule priority ordering.
 * <p>
 * When two modifications conflict, the modification from the rule with higher priority
 * is applied, and the lower priority modification is discarded. If priorities are equal,
 * the modification with the lower sequence number (created first) wins.
 * <p>
 * This strategy is deterministic and always applicable, making it suitable as a default
 * resolution mechanism for production use.
 * <p>
 * This implementation is stateless and thread-safe.
 */
public final class PriorityResolutionStrategy implements ResolutionStrategy
{
	@Override
	public boolean canResolve(Conflict conflict)
	{
		Objects.requireNonNull(conflict, "conflict cannot be null");
		// Priority strategy can always resolve conflicts
		return true;
	}

	@Override
	public ResolutionDecision resolve(Conflict conflict) throws ConflictResolutionException
	{
		Objects.requireNonNull(conflict, "conflict cannot be null");

		PendingModification first = conflict.first();
		PendingModification second = conflict.second();

		// Compare priorities (higher priority wins)
		if (first.priority() > second.priority())
		{
			return new ResolutionDecision(
				List.of(first),
				List.of(second),
				String.format("Rule '%s' (priority %d) takes precedence over rule '%s' (priority %d)",
					first.ruleId(), first.priority(), second.ruleId(), second.priority()));
		}
		if (second.priority() > first.priority())
		{
			return new ResolutionDecision(
				List.of(second),
				List.of(first),
				String.format("Rule '%s' (priority %d) takes precedence over rule '%s' (priority %d)",
					second.ruleId(), second.priority(), first.ruleId(), first.priority()));
		}

		// Equal priority: use sequence number as tiebreaker (earlier modification wins)
		if (first.sequenceNumber() < second.sequenceNumber())
		{
			return new ResolutionDecision(
				List.of(first),
				List.of(second),
				String.format("Rules have equal priority (%d); modification %d applied before modification %d",
					first.priority(), first.sequenceNumber(), second.sequenceNumber()));
		}

		return new ResolutionDecision(
			List.of(second),
			List.of(first),
			String.format("Rules have equal priority (%d); modification %d applied before modification %d",
				second.priority(), second.sequenceNumber(), first.sequenceNumber()));
	}
}
