package io.github.cowwoc.styler.formatter.api.test.conflict;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.EditPriority;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.conflict.Conflict;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictResolutionException;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictSeverity;
import io.github.cowwoc.styler.formatter.api.conflict.PendingModification;
import io.github.cowwoc.styler.formatter.api.conflict.PriorityResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.ResolutionDecision;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PriorityResolutionStrategy}.
 * <p>
 * Validates priority-based conflict resolution with sequence number tiebreaking.
 */
public class PriorityResolutionStrategyTest
{
	/**
	 * Verifies canResolve always returns true for any conflict.
	 */
	@Test
	public void canResolveAlwaysReturnsTrue()
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 10, 1);
		Conflict conflict = new Conflict(first, second, ConflictSeverity.MODERATE, "test");

		assertThat(strategy.canResolve(conflict)).isTrue();
	}

	/**
	 * Verifies canResolve with null conflict throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void canResolveWithNullConflictThrows()
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		strategy.canResolve(null);
	}

	/**
	 * Verifies higher priority modification wins over lower priority.
	 */
	@Test
	public void higherPriorityWins()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification lowPriority = createModification("lowPriorityRule", 1, 10, "text1", 5, 0);
		PendingModification highPriority = createModification("highPriorityRule", 5, 15, "text2", 20, 1);
		Conflict conflict = new Conflict(lowPriority, highPriority, ConflictSeverity.MODERATE,
			"Priority conflict");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactly(highPriority);
		assertThat(decision.toDiscard()).containsExactly(lowPriority);
	}

	/**
	 * Verifies first modification wins when it has higher priority.
	 */
	@Test
	public void firstModificationWinsWhenHigherPriority()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification highPriority = createModification("highPriorityRule", 1, 10, "text1", 30, 0);
		PendingModification lowPriority = createModification("lowPriorityRule", 5, 15, "text2", 10, 1);
		Conflict conflict = new Conflict(highPriority, lowPriority, ConflictSeverity.MODERATE,
			"Priority conflict");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactly(highPriority);
		assertThat(decision.toDiscard()).containsExactly(lowPriority);
	}

	/**
	 * Verifies equal priority with lower sequence number wins.
	 */
	@Test
	public void equalPriorityLowerSequenceWins()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification earlier = createModification("rule1", 1, 10, "text1", 15, 0);
		PendingModification later = createModification("rule2", 5, 15, "text2", 15, 1);
		Conflict conflict = new Conflict(earlier, later, ConflictSeverity.MODERATE, "test");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactly(earlier);
		assertThat(decision.toDiscard()).containsExactly(later);
	}

	/**
	 * Verifies equal priority with second having lower sequence wins.
	 */
	@Test
	public void equalPrioritySecondModificationWins()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification later = createModification("rule1", 1, 10, "text1", 15, 5);
		PendingModification earlier = createModification("rule2", 5, 15, "text2", 15, 2);
		Conflict conflict = new Conflict(later, earlier, ConflictSeverity.MODERATE, "test");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactly(earlier);
		assertThat(decision.toDiscard()).containsExactly(later);
	}

	/**
	 * Verifies rationale includes priority information when priority determines winner.
	 */
	@Test
	public void rationaleIncludesPriorityInformation()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification lowPriority = createModification("formattingRule", 1, 10, "text1", 5, 0);
		PendingModification highPriority = createModification("securityRule", 5, 15, "text2", 25, 1);
		Conflict conflict = new Conflict(lowPriority, highPriority, ConflictSeverity.SEVERE,
			"test");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.rationale()).contains("priority 25");
		assertThat(decision.rationale()).contains("priority 5");
		assertThat(decision.rationale()).contains("formattingRule");
		assertThat(decision.rationale()).contains("securityRule");
	}

	/**
	 * Verifies rationale includes sequence information for equal priority conflicts.
	 */
	@Test
	public void rationaleIncludesSequenceInformationForEqualPriority()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification earlier = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification later = createModification("rule2", 5, 15, "text2", 10, 1);
		Conflict conflict = new Conflict(earlier, later, ConflictSeverity.MODERATE, "test");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.rationale()).contains("equal priority");
		assertThat(decision.rationale()).contains("modification 0");
		assertThat(decision.rationale()).contains("modification 1");
	}

	/**
	 * Verifies resolution works with MINOR severity conflicts.
	 */
	@Test
	public void resolvesMinorSeverityConflicts()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification first = createModification("rule1", 1, 10, "text1", 20, 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 10, 1);
		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR, "test");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactly(first);
		assertThat(decision.toDiscard()).containsExactly(second);
	}

	/**
	 * Verifies resolution works with SEVERE severity conflicts.
	 */
	@Test
	public void resolvesSevereSeverityConflicts()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification first = createModification("rule1", 1, 10, "text1", 5, 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 15, 1);
		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE, "test");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactly(second);
		assertThat(decision.toDiscard()).containsExactly(first);
	}

	/**
	 * Verifies resolve with null conflict throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void resolveWithNullConflictThrows()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		strategy.resolve(null);
	}

	/**
	 * Verifies large priority difference is handled correctly.
	 */
	@Test
	public void largePriorityDifference()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification veryLowPriority = createModification("rule1", 1, 10, "text1", 1, 0);
		PendingModification veryHighPriority = createModification("rule2", 5, 15, "text2", 1000, 1);
		Conflict conflict = new Conflict(veryLowPriority, veryHighPriority,
			ConflictSeverity.MODERATE, "test");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactly(veryHighPriority);
		assertThat(decision.toDiscard()).containsExactly(veryLowPriority);
	}

	/**
	 * Verifies large sequence number difference for equal priority.
	 */
	@Test
	public void largeSequenceNumberDifference()
		throws ConflictResolutionException
	{
		PriorityResolutionStrategy strategy = new PriorityResolutionStrategy();
		PendingModification veryEarly = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification veryLate = createModification("rule2", 5, 15, "text2", 10, 999);
		Conflict conflict = new Conflict(veryEarly, veryLate, ConflictSeverity.MODERATE, "test");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactly(veryEarly);
		assertThat(decision.toDiscard()).containsExactly(veryLate);
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
}
