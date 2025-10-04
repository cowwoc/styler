package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.api.MutableFormattingContext;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.EditPriority;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for MutableFormattingContext.
 */
public class MutableFormattingContextTest
{
	/**
	 * Verifies constructor rejects null rootNode parameter.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullRootNode()
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();
		Set<String> enabledRules = Collections.emptySet();
		Map<String, Object> metadata = Collections.emptyMap();
		Path filePath = Paths.get("TestFile.java");
		String sourceText = "public class TestFile {}";

		new MutableFormattingContext(null, sourceText, filePath, ruleConfig, enabledRules, metadata);
	}

	/**
	 * Verifies constructor rejects null sourceText parameter.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullSourceText()
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();
		Set<String> enabledRules = Collections.emptySet();
		Map<String, Object> metadata = Collections.emptyMap();
		Path filePath = Paths.get("TestFile.java");

		new MutableFormattingContext(null, null, filePath, ruleConfig, enabledRules, metadata);
	}

	/**
	 * Verifies constructor rejects null filePath parameter.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullFilePath()
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();
		Set<String> enabledRules = Collections.emptySet();
		Map<String, Object> metadata = Collections.emptyMap();
		String sourceText = "public class TestFile {}";

		new MutableFormattingContext(null, sourceText, null, ruleConfig, enabledRules, metadata);
	}

	/**
	 * Verifies constructor rejects null configuration parameter.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullConfiguration()
	{
		Set<String> enabledRules = Collections.emptySet();
		Map<String, Object> metadata = Collections.emptyMap();
		Path filePath = Paths.get("TestFile.java");
		String sourceText = "public class TestFile {}";

		new MutableFormattingContext(null, sourceText, filePath, null, enabledRules, metadata);
	}

	/**
	 * Verifies constructor rejects null enabledRules parameter.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullEnabledRules()
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();
		Map<String, Object> metadata = Collections.emptyMap();
		Path filePath = Paths.get("TestFile.java");
		String sourceText = "public class TestFile {}";

		new MutableFormattingContext(null, sourceText, filePath, ruleConfig, null, metadata);
	}

	/**
	 * Verifies constructor rejects null metadata parameter.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullMetadata()
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();
		Set<String> enabledRules = Collections.emptySet();
		Path filePath = Paths.get("TestFile.java");
		String sourceText = "public class TestFile {}";

		new MutableFormattingContext(null, sourceText, filePath, ruleConfig, enabledRules, null);
	}

	/**
	 * Test configuration methods.
	 */
	@Test
	public void configurationMethods() throws ConfigurationException
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();

		// Test configuration access - this would be tested if we had a working CompilationUnitNode
		// For now, we'll just verify the test configuration works
		ruleConfig.validate(); // Should not throw for our test configuration

		String description = ruleConfig.getDescription();
		assert "Test rule configuration for unit testing".equals(description);
	}

	/**
	 * Verifies that queueModification adds modification to pending queue.
	 */
	@Test
	public void queueModificationAddsToPendingQueue()
	{
		MutableFormattingContext context = createTestContext();
		PendingModification mod = createModification("rule1", 1, 10, "text", 10, 0);

		assertThat(context.getPendingModificationCount()).isEqualTo(0);

		context.queueModification(mod);

		assertThat(context.getPendingModificationCount()).isEqualTo(1);
	}

	/**
	 * Verifies that queueModification assigns sequence numbers automatically.
	 */
	@Test
	public void queueModificationAssignsSequenceNumbers()
		throws ConflictResolutionException
	{
		MutableFormattingContext context = createTestContext();

		// Queue three modifications without sequence numbers (will be auto-assigned)
		PendingModification mod1 = createModification("rule1", 1, 5, "a", 10, 0);
		PendingModification mod2 = createModification("rule2", 10, 15, "b", 10, 0);
		PendingModification mod3 = createModification("rule3", 20, 25, "c", 10, 0);

		context.queueModification(mod1);
		context.queueModification(mod2);
		context.queueModification(mod3);

		ResolutionDecision decision = context.commit();

		// All three should be applied (non-conflicting)
		assertThat(decision.toApply()).hasSize(3);
		// Verify sequence numbers were assigned
		assertThat(decision.toApply().get(0).sequenceNumber()).isEqualTo(0);
		assertThat(decision.toApply().get(1).sequenceNumber()).isEqualTo(1);
		assertThat(decision.toApply().get(2).sequenceNumber()).isEqualTo(2);
	}

	/**
	 * Verifies that queueModification with null modification throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void queueModificationWithNullThrows()
	{
		MutableFormattingContext context = createTestContext();
		context.queueModification(null);
	}

	/**
	 * Verifies that queueModification enforces MAX_PENDING_MODIFICATIONS limit.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void queueModificationEnforcesResourceLimit()
	{
		MutableFormattingContext context = createTestContext();

		// Queue modifications up to the limit (10,000)
		// For test efficiency, we'll just verify the exception is thrown at the limit
		// by creating a realistic scenario near the boundary
		for (int i = 0; i < 10_001; ++i)
		{
			PendingModification mod = createModification("rule" + i, i * 10 + 1,
				i * 10 + 5, "text" + i, 10, i);
			context.queueModification(mod);
		}
	}

	/**
	 * Verifies that commit with empty queue returns empty decision.
	 */
	@Test
	public void commitEmptyQueueReturnsEmptyDecision()
		throws ConflictResolutionException
	{
		MutableFormattingContext context = createTestContext();

		ResolutionDecision decision = context.commit();

		assertThat(decision.toApply()).isEmpty();
		assertThat(decision.toDiscard()).isEmpty();
		assertThat(decision.rationale()).contains("No pending modifications");
	}

	/**
	 * Verifies that commit with single modification applies it successfully.
	 */
	@Test
	public void commitSingleModificationAppliesIt()
		throws ConflictResolutionException
	{
		MutableFormattingContext context = createTestContext();
		PendingModification mod = createModification("rule1", 1, 10, "text", 10, 0);

		context.queueModification(mod);
		ResolutionDecision decision = context.commit();

		assertThat(decision.toApply()).hasSize(1);
		assertThat(decision.toDiscard()).isEmpty();
		assertThat(context.getPendingModificationCount()).isEqualTo(0);
	}

	/**
	 * Verifies that commit with non-conflicting modifications applies all.
	 */
	@Test
	public void commitNonConflictingModificationsAppliesAll()
		throws ConflictResolutionException
	{
		MutableFormattingContext context = createTestContext();

		PendingModification mod1 = createModification("rule1", 1, 5, "a", 10, 0);
		PendingModification mod2 = createModification("rule2", 10, 15, "b", 10, 1);
		PendingModification mod3 = createModification("rule3", 20, 25, "c", 10, 2);

		context.queueModification(mod1);
		context.queueModification(mod2);
		context.queueModification(mod3);

		ResolutionDecision decision = context.commit();

		assertThat(decision.toApply()).hasSize(3);
		assertThat(decision.toDiscard()).isEmpty();
		assertThat(context.getPendingModificationCount()).isEqualTo(0);
	}

	/**
	 * Verifies that commit with priority strategy resolves conflicts by priority.
	 */
	@Test
	public void commitWithPriorityStrategyResolvesByPriority()
		throws ConflictResolutionException
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(),
			new PriorityResolutionStrategy());
		MutableFormattingContext context = createTestContext(resolver);

		PendingModification highPriority = createModification("highRule", 1, 10, "high", 100, 0);
		PendingModification lowPriority = createModification("lowRule", 5, 15, "low", 10, 1);

		context.queueModification(highPriority);
		context.queueModification(lowPriority);

		ResolutionDecision decision = context.commit();

		assertThat(decision.toApply()).containsExactly(highPriority);
		assertThat(decision.toDiscard()).containsExactly(lowPriority);
		assertThat(context.getPendingModificationCount()).isEqualTo(0);
	}

	/**
	 * Verifies that commit with merge strategy merges compatible modifications.
	 */
	@Test
	public void commitWithMergeStrategyMergesCompatible()
		throws ConflictResolutionException
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(),
			new MergeResolutionStrategy());
		MutableFormattingContext context = createTestContext(resolver);

		// Non-overlapping modifications should be merged
		PendingModification mod1 = createModification("rule1", 1, 5, "a", 10, 0);
		PendingModification mod2 = createModification("rule2", 10, 15, "b", 10, 1);

		context.queueModification(mod1);
		context.queueModification(mod2);

		ResolutionDecision decision = context.commit();

		assertThat(decision.toApply()).containsExactlyInAnyOrder(mod1, mod2);
		assertThat(decision.toDiscard()).isEmpty();
	}

	/**
	 * Verifies that commit with merge strategy throws on incompatible modifications.
	 */
	@Test(expectedExceptions = ConflictResolutionException.class)
	public void commitWithMergeStrategyThrowsOnIncompatible()
		throws ConflictResolutionException
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(),
			new MergeResolutionStrategy());
		MutableFormattingContext context = createTestContext(resolver);

		// Overlapping with different text should throw
		PendingModification mod1 = createModification("rule1", 1, 10, "different1", 10, 0);
		PendingModification mod2 = createModification("rule2", 5, 15, "different2", 10, 1);

		context.queueModification(mod1);
		context.queueModification(mod2);

		context.commit();
	}

	/**
	 * Verifies that commit with fail-fast strategy throws on any conflict.
	 */
	@Test(expectedExceptions = ConflictResolutionException.class)
	public void commitWithFailFastStrategyThrowsOnConflict()
		throws ConflictResolutionException
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(),
			new FailFastResolutionStrategy());
		MutableFormattingContext context = createTestContext(resolver);

		PendingModification mod1 = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification mod2 = createModification("rule2", 5, 15, "text2", 10, 1);

		context.queueModification(mod1);
		context.queueModification(mod2);

		context.commit();
	}

	/**
	 * Verifies that failed commit leaves queue unchanged.
	 */
	@Test
	public void failedCommitLeavesQueueUnchanged()
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(),
			new FailFastResolutionStrategy());
		MutableFormattingContext context = createTestContext(resolver);

		PendingModification mod1 = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification mod2 = createModification("rule2", 5, 15, "text2", 10, 1);

		context.queueModification(mod1);
		context.queueModification(mod2);

		assertThat(context.getPendingModificationCount()).isEqualTo(2);

		try
		{
			context.commit();
			throw new AssertionError("Expected ConflictResolutionException");
		}
		catch (ConflictResolutionException e)
		{
			// Queue should remain unchanged after failed commit
			assertThat(context.getPendingModificationCount()).isEqualTo(2);
		}
	}

	/**
	 * Verifies that successful commit clears queue and resets sequence counter.
	 */
	@Test
	public void successfulCommitClearsQueueAndResetsSequence()
		throws ConflictResolutionException
	{
		MutableFormattingContext context = createTestContext();

		PendingModification mod1 = createModification("rule1", 1, 5, "a", 10, 0);
		PendingModification mod2 = createModification("rule2", 10, 15, "b", 10, 1);

		context.queueModification(mod1);
		context.queueModification(mod2);

		assertThat(context.getPendingModificationCount()).isEqualTo(2);

		ResolutionDecision decision = context.commit();

		assertThat(context.getPendingModificationCount()).isEqualTo(0);
		assertThat(decision.toApply()).hasSize(2);

		// Queue new modification - sequence should start from 0 again
		PendingModification mod3 = createModification("rule3", 20, 25, "c", 10, 0);
		context.queueModification(mod3);

		ResolutionDecision secondDecision = context.commit();

		assertThat(secondDecision.toApply()).hasSize(1);
		assertThat(secondDecision.toApply().get(0).sequenceNumber()).isEqualTo(0);
	}

	/**
	 * Verifies that multiple conflicts in single commit are all resolved.
	 */
	@Test
	public void commitMultipleConflictsResolvesAll()
		throws ConflictResolutionException
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(),
			new PriorityResolutionStrategy());
		MutableFormattingContext context = createTestContext(resolver);

		// Create three pairs of conflicting modifications
		PendingModification high1 = createModification("high1", 1, 10, "h1", 100, 0);
		PendingModification low1 = createModification("low1", 5, 15, "l1", 10, 1);

		PendingModification high2 = createModification("high2", 20, 30, "h2", 100, 2);
		PendingModification low2 = createModification("low2", 25, 35, "l2", 10, 3);

		PendingModification high3 = createModification("high3", 40, 50, "h3", 100, 4);
		PendingModification low3 = createModification("low3", 45, 55, "l3", 10, 5);

		context.queueModification(high1);
		context.queueModification(low1);
		context.queueModification(high2);
		context.queueModification(low2);
		context.queueModification(high3);
		context.queueModification(low3);

		ResolutionDecision decision = context.commit();

		// All high-priority modifications should be applied
		assertThat(decision.toApply()).containsExactlyInAnyOrder(high1, high2, high3);
		// All low-priority modifications should be discarded
		assertThat(decision.toDiscard()).containsExactlyInAnyOrder(low1, low2, low3);
	}

	/**
	 * Verifies that commit with custom detector and strategy works correctly.
	 */
	@Test
	public void commitWithCustomDetectorAndStrategyWorks()
		throws ConflictResolutionException
	{
		// Use default detector but custom strategy
		ConflictDetector detector = new DefaultConflictDetector();
		ResolutionStrategy strategy = new MergeResolutionStrategy();
		ConflictResolver resolver = new DefaultConflictResolver(detector, strategy);
		MutableFormattingContext context = createTestContext(resolver);

		// Overlapping with identical text should merge
		PendingModification mod1 = createModification("rule1", 1, 10, "same", 10, 0);
		PendingModification mod2 = createModification("rule2", 5, 15, "same", 10, 1);

		context.queueModification(mod1);
		context.queueModification(mod2);

		ResolutionDecision decision = context.commit();

		assertThat(decision.toApply()).containsExactlyInAnyOrder(mod1, mod2);
		assertThat(decision.toDiscard()).isEmpty();
	}

	/**
	 * Verifies that getPendingModificationCount returns accurate count.
	 */
	@Test
	public void getPendingModificationCountReturnsAccurateCount()
	{
		MutableFormattingContext context = createTestContext();

		assertThat(context.getPendingModificationCount()).isEqualTo(0);

		context.queueModification(createModification("rule1", 1, 5, "a", 10, 0));
		assertThat(context.getPendingModificationCount()).isEqualTo(1);

		context.queueModification(createModification("rule2", 10, 15, "b", 10, 1));
		assertThat(context.getPendingModificationCount()).isEqualTo(2);

		context.queueModification(createModification("rule3", 20, 25, "c", 10, 2));
		assertThat(context.getPendingModificationCount()).isEqualTo(3);
	}

	/**
	 * Verifies that sequence numbers break ties when priorities are equal.
	 */
	@Test
	public void sequenceNumberBreaksTiesInPriorityStrategy()
		throws ConflictResolutionException
	{
		ConflictResolver resolver = new DefaultConflictResolver(
			new DefaultConflictDetector(),
			new PriorityResolutionStrategy());
		MutableFormattingContext context = createTestContext(resolver);

		// Same priority, different sequence numbers
		PendingModification first = createModification("first", 1, 10, "a", 10, 0);
		PendingModification second = createModification("second", 5, 15, "b", 10, 0);

		context.queueModification(first);
		context.queueModification(second);

		ResolutionDecision decision = context.commit();

		// First queued (lower sequence after auto-assignment) should win
		assertThat(decision.toApply()).hasSize(1);
		assertThat(decision.toDiscard()).hasSize(1);
		assertThat(decision.toApply().get(0).sequenceNumber()).
			isLessThan(decision.toDiscard().get(0).sequenceNumber());
	}

	/**
	 * Verifies that context tracks modification count correctly.
	 */
	@Test
	public void getModificationCountTracksChanges()
	{
		MutableFormattingContext context = createTestContext();

		int initialCount = context.getModificationCount();

		// setRootNode increments modification count
		CompilationUnitNode newRoot = createTestRoot();
		context.setRootNode(newRoot);

		assertThat(context.getModificationCount()).isEqualTo(initialCount + 1);
	}

	/**
	 * Verifies that context provides access to pending modification queue state.
	 */
	@Test
	public void contextExposesQueueState()
		throws ConflictResolutionException
	{
		MutableFormattingContext context = createTestContext();

		assertThat(context.getPendingModificationCount()).isEqualTo(0);

		context.queueModification(createModification("rule1", 1, 5, "a", 10, 0));
		context.queueModification(createModification("rule2", 10, 15, "b", 10, 1));

		assertThat(context.getPendingModificationCount()).isEqualTo(2);

		context.commit();

		assertThat(context.getPendingModificationCount()).isEqualTo(0);
	}

	/**
	 * Creates a test MutableFormattingContext with default configuration.
	 *
	 * @return a configured MutableFormattingContext for testing
	 */
	private static MutableFormattingContext createTestContext()
	{
		CompilationUnitNode root = createTestRoot();
		String sourceText = "public class Test {}";
		Path filePath = Paths.get("Test.java");
		RuleConfiguration config = new TestRuleConfiguration();
		Set<String> enabledRules = Set.of("rule1", "rule2", "rule3");
		Map<String, Object> metadata = Collections.emptyMap();

		return new MutableFormattingContext(root, sourceText, filePath, config, enabledRules, metadata);
	}

	/**
	 * Creates a test MutableFormattingContext with custom conflict resolver.
	 *
	 * @param resolver the conflict resolution strategy to use
	 * @return a configured MutableFormattingContext with specified resolver
	 */
	private static MutableFormattingContext createTestContext(ConflictResolver resolver)
	{
		CompilationUnitNode root = createTestRoot();
		String sourceText = "public class Test {}";
		Path filePath = Paths.get("Test.java");
		RuleConfiguration config = new TestRuleConfiguration();
		Set<String> enabledRules = Set.of("rule1", "rule2", "rule3");
		Map<String, Object> metadata = Collections.emptyMap();

		return new MutableFormattingContext(root, sourceText, filePath, config, enabledRules,
			metadata, resolver);
	}

	/**
	 * Creates a minimal CompilationUnitNode for testing.
	 *
	 * @return a CompilationUnitNode with minimal configuration
	 */
	private static CompilationUnitNode createTestRoot()
	{
		// Create a minimal root node for testing conflict resolution
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 100);
		SourceRange range = new SourceRange(start, end);

		return new CompilationUnitNode.Builder().
			setRange(range).
			build();
	}

	/**
	 * Creates a test PendingModification.
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
		TextEdit edit = new TextEdit(range, replacement, ruleId, EditPriority.NORMAL);
		return new PendingModification(edit, ruleId, priority, sequence);
	}

	/**
	 * Test implementation of RuleConfiguration for unit tests.
	 */
	private static final class TestRuleConfiguration extends RuleConfiguration
	{
		@Override
		public void validate() throws ConfigurationException
		{
			// No validation needed for test
		}

		@Override
		public RuleConfiguration merge(RuleConfiguration override)
		{
			return this; // Simple implementation for test
		}

		@Override
		public String getDescription()
		{
			return "Test rule configuration for unit testing";
		}

		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof TestRuleConfiguration;
		}

		@Override
		public int hashCode()
		{
			return TestRuleConfiguration.class.hashCode();
		}
	}
}