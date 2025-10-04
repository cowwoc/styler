package io.github.cowwoc.styler.formatter.api.test.conflict;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.EditPriority;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.conflict.Conflict;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictResolutionException;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictSeverity;
import io.github.cowwoc.styler.formatter.api.conflict.MergeResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.PendingModification;
import io.github.cowwoc.styler.formatter.api.conflict.ResolutionDecision;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MergeResolutionStrategy}.
 * <p>
 * Validates conservative merging strategy for compatible modifications:
 * non-overlapping ranges, or overlapping ranges with identical replacement text.
 */
public class MergeResolutionStrategyTest
{
	/**
	 * Verifies that non-overlapping modifications are considered compatible and mergeable.
	 */
	@Test
	public void canResolveNonOverlappingModifications()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 5, "text1", 0);
		PendingModification second = createModification("rule2", 10, 15, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR, "test");

		assertThat(strategy.canResolve(conflict)).isTrue();
	}

	/**
	 * Verifies that overlapping modifications with identical replacement text are compatible.
	 */
	@Test
	public void canResolveOverlappingModificationsWithIdenticalText()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "sameText", 0);
		PendingModification second = createModification("rule2", 5, 15, "sameText", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR, "test");

		assertThat(strategy.canResolve(conflict)).isTrue();
	}

	/**
	 * Verifies that overlapping modifications with different replacement text are incompatible.
	 */
	@Test
	public void cannotResolveOverlappingModificationsWithDifferentText()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "text1", 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE, "test");

		assertThat(strategy.canResolve(conflict)).isFalse();
	}

	/**
	 * Verifies that adjacent (touching) modifications are considered non-overlapping and compatible.
	 */
	@Test
	public void canResolveAdjacentModifications()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "text1", 0);
		PendingModification second = createModification("rule2", 10, 20, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR, "test");

		assertThat(strategy.canResolve(conflict)).isTrue();
	}

	/**
	 * Verifies that modifications with identical ranges and identical text are compatible.
	 */
	@Test
	public void canResolveIdenticalRangesWithIdenticalText()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 5, 10, "identical", 0);
		PendingModification second = createModification("rule2", 5, 10, "identical", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR, "test");

		assertThat(strategy.canResolve(conflict)).isTrue();
	}

	/**
	 * Verifies that modifications with identical ranges but different text are incompatible.
	 */
	@Test
	public void cannotResolveIdenticalRangesWithDifferentText()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 5, 10, "different1", 0);
		PendingModification second = createModification("rule2", 5, 10, "different2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE, "test");

		assertThat(strategy.canResolve(conflict)).isFalse();
	}

	/**
	 * Verifies that null conflict parameter throws NullPointerException in canResolve.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void canResolveWithNullConflictThrows()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();
		strategy.canResolve(null);
	}

	/**
	 * Verifies that non-overlapping modifications result in both being applied.
	 */
	@Test
	public void resolveNonOverlappingModificationsAppliesBoth()
		throws ConflictResolutionException
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 5, "text1", 0);
		PendingModification second = createModification("rule2", 10, 15, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Non-overlapping modifications");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactlyInAnyOrder(first, second);
		assertThat(decision.toDiscard()).isEmpty();
		assertThat(decision.rationale()).contains("Non-overlapping modifications");
		assertThat(decision.rationale()).contains("rule1");
		assertThat(decision.rationale()).contains("rule2");
	}

	/**
	 * Verifies that overlapping modifications with identical text result in both being applied.
	 */
	@Test
	public void resolveOverlappingIdenticalTextAppliesBoth()
		throws ConflictResolutionException
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "identicalFix", 0);
		PendingModification second = createModification("rule2", 5, 15, "identicalFix", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Rules produce same fix");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactlyInAnyOrder(first, second);
		assertThat(decision.toDiscard()).isEmpty();
		assertThat(decision.rationale()).contains("identical replacement text");
		assertThat(decision.rationale()).contains("rule1");
		assertThat(decision.rationale()).contains("rule2");
	}

	/**
	 * Verifies that overlapping modifications with different text throw ConflictResolutionException.
	 */
	@Test(expectedExceptions = ConflictResolutionException.class)
	public void resolveOverlappingDifferentTextThrows()
		throws ConflictResolutionException
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "fix1", 0);
		PendingModification second = createModification("rule2", 5, 15, "fix2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE,
			"Incompatible overlapping modifications");

		strategy.resolve(conflict);
	}

	/**
	 * Verifies that ConflictResolutionException contains correct conflict report for incompatible modifications.
	 */
	@Test
	public void resolveIncompatibleModificationsIncludesConflictReport()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("formattingRule", 1, 10, "fix1", 0);
		PendingModification second = createModification("indentationRule", 5, 15, "fix2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE,
			"Overlapping with different replacements");

		try
		{
			strategy.resolve(conflict);
			throw new AssertionError("Expected ConflictResolutionException");
		}
		catch (ConflictResolutionException e)
		{
			assertThat(e.getReport()).isNotNull();
			assertThat(e.getReport().conflicts()).contains(conflict);
			assertThat(e.getReport().maxSeverity()).isEqualTo(ConflictSeverity.SEVERE);
			assertThat(e.getMessage()).contains("formattingRule");
			assertThat(e.getMessage()).contains("indentationRule");
		}
	}

	/**
	 * Verifies that adjacent modifications result in both being applied.
	 */
	@Test
	public void resolveAdjacentModificationsAppliesBoth()
		throws ConflictResolutionException
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "text1", 0);
		PendingModification second = createModification("rule2", 10, 20, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Adjacent modifications");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactlyInAnyOrder(first, second);
		assertThat(decision.toDiscard()).isEmpty();
	}

	/**
	 * Verifies that modifications with empty replacement text can be merged when non-overlapping.
	 */
	@Test
	public void resolveNonOverlappingWithEmptyTextAppliesBoth()
		throws ConflictResolutionException
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 5, "", 0);
		PendingModification second = createModification("rule2", 10, 15, "text", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Non-overlapping with empty text");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactlyInAnyOrder(first, second);
		assertThat(decision.toDiscard()).isEmpty();
	}

	/**
	 * Verifies that overlapping modifications with both having empty replacement text are merged.
	 */
	@Test
	public void resolveOverlappingBothEmptyTextAppliesBoth()
		throws ConflictResolutionException
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "", 0);
		PendingModification second = createModification("rule2", 5, 15, "", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Overlapping deletions");

		ResolutionDecision decision = strategy.resolve(conflict);

		assertThat(decision.toApply()).containsExactlyInAnyOrder(first, second);
		assertThat(decision.toDiscard()).isEmpty();
	}

	/**
	 * Verifies that overlapping modifications with one empty and one non-empty text throw exception.
	 */
	@Test(expectedExceptions = ConflictResolutionException.class)
	public void resolveOverlappingMixedEmptyTextThrows()
		throws ConflictResolutionException
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "", 0);
		PendingModification second = createModification("rule2", 5, 15, "text", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE,
			"Overlapping with mixed empty/non-empty text");

		strategy.resolve(conflict);
	}

	/**
	 * Verifies that null conflict parameter throws NullPointerException in resolve.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void resolveWithNullConflictThrows()
		throws ConflictResolutionException
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();
		strategy.resolve(null);
	}

	/**
	 * Verifies that single-position overlap is detected correctly.
	 */
	@Test
	public void cannotResolveSinglePositionOverlapWithDifferentText()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		// Ranges [1,10) and [9,15) overlap at position 9
		PendingModification first = createModification("rule1", 1, 10, "text1", 0);
		PendingModification second = createModification("rule2", 9, 15, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MODERATE, "test");

		assertThat(strategy.canResolve(conflict)).isFalse();
	}

	/**
	 * Verifies that complete containment overlap (one range fully contains another) is detected.
	 */
	@Test
	public void cannotResolveContainmentOverlapWithDifferentText()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		// Range [1,20) contains [5,10)
		PendingModification first = createModification("rule1", 1, 20, "outerText", 0);
		PendingModification second = createModification("rule2", 5, 10, "innerText", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE, "test");

		assertThat(strategy.canResolve(conflict)).isFalse();
	}

	/**
	 * Verifies that complete containment with identical text can be resolved.
	 */
	@Test
	public void canResolveContainmentOverlapWithIdenticalText()
	{
		MergeResolutionStrategy strategy = new MergeResolutionStrategy();

		// Range [1,20) contains [5,10), both have identical text
		PendingModification first = createModification("rule1", 1, 20, "sameText", 0);
		PendingModification second = createModification("rule2", 5, 10, "sameText", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR, "test");

		assertThat(strategy.canResolve(conflict)).isTrue();
	}

	/**
	 * Creates a test PendingModification with specified range and replacement text.
	 *
	 * @param ruleId the rule identifier
	 * @param startColumn starting column position
	 * @param endColumn ending column position
	 * @param replacement replacement text
	 * @param sequence sequence number
	 * @return a configured PendingModification for testing
	 */
	private static PendingModification createModification(String ruleId, int startColumn,
		int endColumn, String replacement, int sequence)
	{
		SourcePosition start = new SourcePosition(1, startColumn);
		SourcePosition end = new SourcePosition(1, endColumn);
		SourceRange range = new SourceRange(start, end);
		TextEdit edit = new TextEdit(range, replacement, ruleId,
			EditPriority.NORMAL);
		return new PendingModification(edit, ruleId, 10, sequence);
	}
}
