package io.github.cowwoc.styler.formatter.api.test.conflict;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.EditPriority;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictDetector;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictResolver;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictResolutionException;
import io.github.cowwoc.styler.formatter.api.conflict.DefaultConflictDetector;
import io.github.cowwoc.styler.formatter.api.conflict.DefaultConflictResolver;
import io.github.cowwoc.styler.formatter.api.conflict.FailFastResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.MergeResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.PendingModification;
import io.github.cowwoc.styler.formatter.api.conflict.PriorityResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.ResolutionDecision;
import io.github.cowwoc.styler.formatter.api.conflict.ResolutionStrategy;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract tests for {@link ConflictResolver} interface implementations.
 * <p>
 * Validates that all ConflictResolver implementations properly integrate
 * ConflictDetector and ResolutionStrategy components.
 */
public class ConflictResolverTest
{
	/**
	 * Verifies that resolver returns configured detector instance.
	 */
	@Test
	public void getDetectorReturnsConfiguredDetector()
	{
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new PriorityResolutionStrategy();

		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);

		assertThat(resolver.getDetector()).isSameAs(detector);
	}

	/**
	 * Verifies that resolver returns configured strategy instance.
	 */
	@Test
	public void getStrategyReturnsConfiguredStrategy()
	{
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new MergeResolutionStrategy();

		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);

		assertThat(resolver.getStrategy()).isSameAs(strategy);
	}

	/**
	 * Verifies that resolver with priority strategy correctly integrates detector and strategy.
	 */
	@Test
	public void resolveWithPriorityStrategyIntegratesComponents()
		throws ConflictResolutionException
	{
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new PriorityResolutionStrategy();
		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);

		PendingModification highPriority = createModification("highRule", 1, 10, "high", 100, 0);
		PendingModification lowPriority = createModification("lowRule", 5, 15, "low", 10, 1);

		ResolutionDecision decision = resolver.resolve(List.of(highPriority, lowPriority));

		assertThat(decision.toApply()).containsExactly(highPriority);
		assertThat(decision.toDiscard()).containsExactly(lowPriority);
	}

	/**
	 * Verifies that resolver with merge strategy correctly merges compatible modifications.
	 */
	@Test
	public void resolveWithMergeStrategyIntegratesComponents()
		throws ConflictResolutionException
	{
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new MergeResolutionStrategy();
		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);

		PendingModification first = createModification("rule1", 1, 5, "text1", 10, 0);
		PendingModification second = createModification("rule2", 10, 15, "text2", 10, 1);

		ResolutionDecision decision = resolver.resolve(List.of(first, second));

		assertThat(decision.toApply()).containsExactlyInAnyOrder(first, second);
		assertThat(decision.toDiscard()).isEmpty();
	}

	/**
	 * Verifies that resolver with fail-fast strategy throws on any conflict.
	 */
	@Test(expectedExceptions = ConflictResolutionException.class)
	public void resolveWithFailFastStrategyThrowsOnConflict()
		throws ConflictResolutionException
	{
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new FailFastResolutionStrategy();
		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);

		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 10, 1);

		resolver.resolve(List.of(first, second));
	}

	/**
	 * Verifies that resolver correctly handles empty modification list.
	 */
	@Test
	public void resolveEmptyListReturnsEmptyDecision()
		throws ConflictResolutionException
	{
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new PriorityResolutionStrategy();
		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);

		ResolutionDecision decision = resolver.resolve(List.of());

		assertThat(decision.toApply()).isEmpty();
		assertThat(decision.toDiscard()).isEmpty();
	}

	/**
	 * Verifies that resolver correctly handles single modification (no conflicts).
	 */
	@Test
	public void resolveSingleModificationAppliesIt()
		throws ConflictResolutionException
	{
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new PriorityResolutionStrategy();
		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);

		PendingModification single = createModification("rule1", 1, 10, "text", 10, 0);

		ResolutionDecision decision = resolver.resolve(List.of(single));

		assertThat(decision.toApply()).containsExactly(single);
		assertThat(decision.toDiscard()).isEmpty();
	}

	/**
	 * Verifies that resolver can switch strategies and maintain correct behavior.
	 */
	@Test
	public void switchingStrategiesChangesResolutionBehavior()
		throws ConflictResolutionException
	{
		ConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 10, 1);
		List<PendingModification> modifications = List.of(first, second);

		// With merge strategy, incompatible modifications throw exception
		ConflictResolver mergeResolver = new DefaultConflictResolver(detector,
			new MergeResolutionStrategy());
		try
		{
			mergeResolver.resolve(modifications);
			throw new AssertionError("Expected ConflictResolutionException with merge strategy");
		}
		catch (ConflictResolutionException e)
		{
			// Expected: merge strategy should reject different replacement text
			assertThat(e.getReport()).isNotNull();
		}

		// With priority strategy, higher priority wins (both have equal priority, sequence breaks tie)
		ConflictResolver priorityResolver = new DefaultConflictResolver(detector,
			new PriorityResolutionStrategy());
		ResolutionDecision decision = priorityResolver.resolve(modifications);
		assertThat(decision.toApply()).hasSize(1);
		assertThat(decision.toDiscard()).hasSize(1);
	}

	/**
	 * Verifies that detector and strategy are accessible after construction.
	 */
	@Test
	public void resolverExposesDetectorAndStrategy()
	{
		ConflictDetector customDetector = new DefaultConflictDetector();
		ResolutionStrategy customStrategy = new MergeResolutionStrategy();

		ConflictResolver resolver = new DefaultConflictResolver(customDetector, customStrategy);

		assertThat(resolver.getDetector()).isNotNull();
		assertThat(resolver.getStrategy()).isNotNull();
		assertThat(resolver.getDetector()).isInstanceOf(ConflictDetector.class);
		assertThat(resolver.getStrategy()).isInstanceOf(ResolutionStrategy.class);
	}

	/**
	 * Verifies that resolver correctly delegates detection to detector.
	 */
	@Test
	public void resolverDelegatesToDetector()
		throws ConflictResolutionException
	{
		// Overlapping modifications should be detected by detector
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new PriorityResolutionStrategy();
		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);

		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 10, 1);

		ResolutionDecision decision = resolver.resolve(List.of(first, second));

		// If detector didn't find conflict, both would be applied
		// If detector found conflict, only one is applied (priority or sequence wins)
		int appliedCount = decision.toApply().size();
		int discardedCount = decision.toDiscard().size();

		assertThat(appliedCount + discardedCount).isEqualTo(2);
		assertThat(appliedCount).isEqualTo(1); // One wins due to conflict
	}

	/**
	 * Creates a test PendingModification with specified parameters.
	 *
	 * @param ruleId the rule identifier
	 * @param startColumn starting column position
	 * @param endColumn ending column position
	 * @param replacement replacement text
	 * @param priority modification priority
	 * @param sequence sequence number
	 * @return a configured PendingModification for testing
	 */
	private static PendingModification createModification(String ruleId, int startColumn,
		int endColumn, String replacement, int priority, int sequence)
	{
		SourcePosition start = new SourcePosition(1, startColumn);
		SourcePosition end = new SourcePosition(1, endColumn);
		SourceRange range = new SourceRange(start, end);
		TextEdit edit = new TextEdit(range, replacement, ruleId,
			EditPriority.NORMAL);
		return new PendingModification(edit, ruleId, priority, sequence);
	}
}
