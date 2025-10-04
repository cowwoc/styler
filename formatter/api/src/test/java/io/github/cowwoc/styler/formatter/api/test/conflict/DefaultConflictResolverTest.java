package io.github.cowwoc.styler.formatter.api.test.conflict;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.EditPriority;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictResolutionException;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictResolver;
import io.github.cowwoc.styler.formatter.api.conflict.DefaultConflictDetector;
import io.github.cowwoc.styler.formatter.api.conflict.DefaultConflictResolver;
import io.github.cowwoc.styler.formatter.api.conflict.FailFastResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.MergeResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.PendingModification;
import io.github.cowwoc.styler.formatter.api.conflict.PriorityResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.ResolutionDecision;
import io.github.cowwoc.styler.formatter.api.conflict.ResolutionStrategy;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link DefaultConflictResolver} validating orchestration of detection and strategy application.
 */
public class DefaultConflictResolverTest
{
	/**
	 * Verifies that resolver with no modifications returns empty decision.
	 */
	@Test
	public void resolveWithEmptyListReturnsNoConflicts() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();

		ResolutionDecision decision = resolver.resolve(List.of());

		assertNotNull(decision);
		assertTrue(decision.toApply().isEmpty());
		assertTrue(decision.toDiscard().isEmpty());
		assertTrue(decision.rationale().contains("No conflicts"));
	}

	/**
	 * Verifies that resolver with single modification returns it unchanged.
	 */
	@Test
	public void resolveWithSingleModificationReturnsItUnchanged() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();
		PendingModification mod = createModification("rule1", 1, 1, 5, 1, 10);

		ResolutionDecision decision = resolver.resolve(List.of(mod));

		assertNotNull(decision);
		assertEquals(decision.toApply().size(), 1);
		assertEquals(decision.toApply().get(0), mod);
		assertTrue(decision.toDiscard().isEmpty());
	}

	/**
	 * Verifies that resolver with two non-overlapping modifications returns both.
	 */
	@Test
	public void resolveWithNonOverlappingModificationsReturnsBoth() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();
		PendingModification mod1 = createModification("rule1", 1, 1, 5, 1, 10);
		PendingModification mod2 = createModification("rule2", 2, 2, 5, 2, 10);

		ResolutionDecision decision = resolver.resolve(List.of(mod1, mod2));

		assertNotNull(decision);
		assertEquals(decision.toApply().size(), 2);
		assertTrue(decision.toDiscard().isEmpty());
	}

	/**
	 * Verifies that resolver with overlapping modifications uses priority strategy.
	 */
	@Test
	public void resolveWithOverlappingModificationsUsesPriority() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();
		PendingModification highPriority = createModification("rule1", 10, 1, 5, 1, 15);
		PendingModification lowPriority = createModification("rule2", 1, 1, 10, 1, 20);

		ResolutionDecision decision = resolver.resolve(List.of(highPriority, lowPriority));

		assertNotNull(decision);
		assertEquals(decision.toApply().size(), 1);
		assertEquals(decision.toDiscard().size(), 1);
		assertTrue(decision.toApply().contains(highPriority));
		assertTrue(decision.toDiscard().contains(lowPriority));
	}

	/**
	 * Verifies that resolver validates null modifications list.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void resolveWithNullModificationsThrowsException() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();
		resolver.resolve(null);
	}

	/**
	 * Verifies that resolver validates null elements in modifications list.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void resolveWithNullElementThrowsException() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();
		List<PendingModification> mods = new ArrayList<>();
		mods.add(createModification("rule1", 1, 1, 5, 1, 10));
		mods.add(null);
		resolver.resolve(mods);
	}

	/**
	 * Verifies that resolver enforces maximum pending modifications limit.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void resolveWithTooManyModificationsThrowsException() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();
		List<PendingModification> mods = new ArrayList<>();
		for (int i = 1; i <= 10_001; ++i)
		{
			mods.add(createModification("rule" + i, 1, i, 1, i, 5));
		}
		resolver.resolve(mods);
	}

	/**
	 * Verifies that resolver returns configured detector.
	 */
	@Test
	public void getDetectorReturnsConfiguredDetector()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		ConflictResolver resolver = new DefaultConflictResolver(detector, new PriorityResolutionStrategy());

		assertEquals(resolver.getDetector(), detector);
	}

	/**
	 * Verifies that resolver returns configured strategy.
	 */
	@Test
	public void getStrategyReturnsConfiguredStrategy()
	{
		ResolutionStrategy strategy = new PriorityResolutionStrategy();
		ConflictResolver resolver = new DefaultConflictResolver(new DefaultConflictDetector(), strategy);

		assertEquals(resolver.getStrategy(), strategy);
	}

	/**
	 * Verifies that resolver validates null detector in constructor.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorWithNullDetectorThrowsException()
	{
		new DefaultConflictResolver(null, new PriorityResolutionStrategy());
	}

	/**
	 * Verifies that resolver validates null strategy in constructor.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorWithNullStrategyThrowsException()
	{
		new DefaultConflictResolver(new DefaultConflictDetector(), null);
	}

	/**
	 * Verifies that resolver handles multiple overlapping conflicts.
	 */
	@Test
	public void resolveWithMultipleConflictsResolvesAll() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();
		PendingModification mod1 = createModification("rule1", 10, 1, 1, 1, 10);
		PendingModification mod2 = createModification("rule2", 5, 1, 5, 1, 15);
		PendingModification mod3 = createModification("rule3", 1, 1, 12, 1, 20);

		ResolutionDecision decision = resolver.resolve(List.of(mod1, mod2, mod3));

		assertNotNull(decision);
		assertFalse(decision.toApply().isEmpty());
		assertTrue(decision.rationale().contains("conflict"));
	}

	/**
	 * Verifies that resolver with fail-fast strategy throws on conflict.
	 */
	@Test(expectedExceptions = ConflictResolutionException.class)
	public void resolveWithFailFastStrategyThrowsOnConflict() throws ConflictResolutionException
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(), new FailFastResolutionStrategy());
		PendingModification mod1 = createModification("rule1", 1, 1, 5, 1, 15);
		PendingModification mod2 = createModification("rule2", 1, 1, 10, 1, 20);

		resolver.resolve(List.of(mod1, mod2));
	}

	/**
	 * Verifies that resolver with merge strategy merges compatible modifications.
	 */
	@Test
	public void resolveWithMergeStrategyMergesCompatibleModifications() throws ConflictResolutionException
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(), new MergeResolutionStrategy());
		PendingModification mod1 = createModification("rule1", 1, 1, 5, 1, 10);
		PendingModification mod2 = createModification("rule2", 1, 2, 5, 2, 10);

		ResolutionDecision decision = resolver.resolve(List.of(mod1, mod2));

		assertNotNull(decision);
		assertEquals(decision.toApply().size(), 2);
		assertTrue(decision.toDiscard().isEmpty());
	}

	/**
	 * Verifies that resolver handles equal priority with sequence number tiebreaking.
	 */
	@Test
	public void resolveWithEqualPriorityUsesSequenceNumber() throws ConflictResolutionException
	{
		ConflictResolver resolver = createDefaultResolver();
		PendingModification first = new PendingModification(
			createTextEdit(1, 5, 1, 15, "replacement"),
			"rule1", 5, 0);
		PendingModification second = new PendingModification(
			createTextEdit(1, 10, 1, 20, "other"),
			"rule2", 5, 1);

		ResolutionDecision decision = resolver.resolve(List.of(first, second));

		assertNotNull(decision);
		assertTrue(decision.toApply().contains(first));
		assertTrue(decision.toDiscard().contains(second));
	}

	// Helper methods

	private static ConflictResolver createDefaultResolver()
	{
		return new DefaultConflictResolver(new DefaultConflictDetector(), new PriorityResolutionStrategy());
	}

	private static PendingModification createModification(String ruleId, int priority,
	                                                      int startLine, int startCol,
	                                                      int endLine, int endCol)
	{
		TextEdit edit = createTextEdit(startLine, startCol, endLine, endCol, "replacement");
		return new PendingModification(edit, ruleId, priority, 0);
	}

	private static TextEdit createTextEdit(int startLine, int startCol, int endLine, int endCol, String replacement)
	{
		SourceRange range = new SourceRange(
			new SourcePosition(startLine, startCol),
			new SourcePosition(endLine, endCol));
		return new TextEdit(range, replacement, "test-rule", EditPriority.NORMAL);
	}
}
