package io.github.cowwoc.styler.formatter.api.conflict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of conflict resolution orchestrating detection and strategy application.
 * <p>
 * This resolver coordinates the complete resolution workflow:
 * <ol>
 * <li>Validates input modifications and enforces resource limits</li>
 * <li>Detects conflicts using configured {@link ConflictDetector}</li>
 * <li>Applies configured {@link ResolutionStrategy} to each detected conflict</li>
 * <li>Aggregates individual decisions into final {@link ResolutionDecision}</li>
 * </ol>
 * <p>
 * The resolver maintains no mutable state, making it stateless and thread-safe for concurrent
 * file processing operations.
 */
public final class DefaultConflictResolver implements ConflictResolver
{
	/**
	 * Maximum number of pending modifications to analyze before triggering resource limit protection.
	 * <p>
	 * Based on Java Language Specification §14.4 nesting limits and empirical performance testing.
	 * See: <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.4">JLS §14.4</a>
	 */
	private static final int MAX_PENDING_MODIFICATIONS = 10_000;

	private final ConflictDetector detector;
	private final ResolutionStrategy strategy;

	/**
	 * Creates a conflict resolver with specified detector and resolution strategy.
	 *
	 * @param detector the conflict detector to use for identifying overlapping modifications,
	 *                 never {@code null}
	 * @param strategy the resolution strategy to apply to detected conflicts, never {@code null}
	 * @throws NullPointerException if {@code detector} or {@code strategy} is null
	 */
	public DefaultConflictResolver(ConflictDetector detector, ResolutionStrategy strategy)
	{
		this.detector = Objects.requireNonNull(detector, "detector cannot be null");
		this.strategy = Objects.requireNonNull(strategy, "strategy cannot be null");
	}

	@Override
	public ResolutionDecision resolve(List<PendingModification> modifications)
		throws ConflictResolutionException
	{
		Objects.requireNonNull(modifications, "modifications cannot be null");

		// Fast path: 0 or 1 modifications have no conflicts
		if (modifications.size() <= 1)
		{
			return new ResolutionDecision(
				List.copyOf(modifications),
				List.of(),
				"No conflicts: " + modifications.size() + " modification(s)");
		}

		// Resource limit protection
		if (modifications.size() > MAX_PENDING_MODIFICATIONS)
		{
			throw new IllegalStateException("Too many pending modifications: " +
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

		// Step 1: Detect conflicts using configured detector
		List<Conflict> conflicts = detector.detectConflicts(modifications);

		// Fast path: No conflicts detected
		if (conflicts.isEmpty())
		{
			return new ResolutionDecision(
				List.copyOf(modifications),
				List.of(),
				"No conflicts detected among " + modifications.size() + " modifications");
		}

		// Step 2: Apply resolution strategy to each conflict
		List<PendingModification> toApply = new ArrayList<>(modifications);
		List<PendingModification> toDiscard = new ArrayList<>();
		StringBuilder rationale = new StringBuilder(256);
		rationale.append("Resolved ").append(conflicts.size()).append(" conflict(s):").append('\n');

		for (Conflict conflict : conflicts)
		{
			// Check if strategy can resolve this conflict
			if (!strategy.canResolve(conflict))
			{
				Map<String, Integer> conflictsByRule = new HashMap<>();
				conflictsByRule.put(conflict.first().ruleId(),
					conflictsByRule.getOrDefault(conflict.first().ruleId(), 0) + 1);
				conflictsByRule.put(conflict.second().ruleId(),
					conflictsByRule.getOrDefault(conflict.second().ruleId(), 0) + 1);

				ConflictReport report = new ConflictReport(
					List.of(conflict),
					conflictsByRule,
					conflict.severity(),
					String.format("Strategy %s cannot resolve %s conflict: %s",
						strategy.getClass().getSimpleName(), conflict.severity(), conflict.description()));

				throw new ConflictResolutionException(
					"Strategy " + strategy.getClass().getSimpleName() +
					" cannot resolve conflict: " + conflict.description(),
					report);
			}

			// Apply strategy to get resolution decision
			ResolutionDecision decision = strategy.resolve(conflict);

			// Track which modifications to discard from this conflict
			Set<PendingModification> discardedFromConflict = new HashSet<>(decision.toDiscard());

			// Update aggregate lists
			for (PendingModification mod : discardedFromConflict)
			{
				if (toApply.contains(mod))
				{
					toApply.remove(mod);
					if (!toDiscard.contains(mod))
					{
						toDiscard.add(mod);
					}
				}
			}

			// Append conflict-specific rationale
			rationale.append("  - ").append(decision.rationale()).append('\n');
		}

		// Step 3: Build final resolution decision
		return new ResolutionDecision(
			List.copyOf(toApply),
			List.copyOf(toDiscard),
			rationale.toString().trim());
	}

	@Override
	public ConflictDetector getDetector()
	{
		return detector;
	}

	@Override
	public ResolutionStrategy getStrategy()
	{
		return strategy;
	}
}
