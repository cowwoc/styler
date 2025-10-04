package io.github.cowwoc.styler.formatter.api.test.conflict;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.EditPriority;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.conflict.Conflict;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictSeverity;
import io.github.cowwoc.styler.formatter.api.conflict.DefaultConflictDetector;
import io.github.cowwoc.styler.formatter.api.conflict.PendingModification;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultConflictDetector}.
 * <p>
 * Validates pairwise conflict detection algorithm, severity classification, and resource limit enforcement.
 */
public class DefaultConflictDetectorTest
{
	/**
	 * Verifies empty modification list produces no conflicts.
	 */
	@Test
	public void emptyListProducesNoConflicts()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		List<Conflict> conflicts = detector.detectConflicts(List.of());

		assertThat(conflicts).isEmpty();
	}

	/**
	 * Verifies single modification produces no conflicts.
	 */
	@Test
	public void singleModificationProducesNoConflicts()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification mod = createModification("rule1", 1, 10, "text", 10, 0);

		List<Conflict> conflicts = detector.detectConflicts(List.of(mod));

		assertThat(conflicts).isEmpty();
	}

	/**
	 * Verifies non-overlapping modifications produce no conflicts.
	 */
	@Test
	public void nonOverlappingModificationsProduceNoConflicts()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 20, 30, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts).isEmpty();
	}

	/**
	 * Verifies adjacent modifications (touching boundaries) produce no conflicts.
	 */
	@Test
	public void adjacentModificationsProduceNoConflicts()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		// Range [1,10) and [10,20) are adjacent, not overlapping
		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 10, 20, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts).isEmpty();
	}

	/**
	 * Verifies overlapping modifications produce one conflict.
	 */
	@Test
	public void overlappingModificationsProduceConflict()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("rule1", 1, 15, "text1", 10, 0);
		PendingModification second = createModification("rule2", 10, 25, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts).hasSize(1);
		assertThat(conflicts.get(0).first()).isEqualTo(first);
		assertThat(conflicts.get(0).second()).isEqualTo(second);
	}

	/**
	 * Verifies complete containment (one range inside another) produces conflict.
	 */
	@Test
	public void containedModificationProducesConflict()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		// [1,30) completely contains [10,20)
		PendingModification outer = createModification("rule1", 1, 30, "text1", 10, 0);
		PendingModification inner = createModification("rule2", 10, 20, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(outer, inner));

		assertThat(conflicts).hasSize(1);
	}

	/**
	 * Verifies identical ranges produce conflict.
	 */
	@Test
	public void identicalRangesProduceConflict()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 1, 10, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts).hasSize(1);
	}

	/**
	 * Verifies single-position overlap is detected.
	 */
	@Test
	public void singlePositionOverlapDetected()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		// [1,10) and [9,15) overlap at position 9
		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 9, 15, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts).hasSize(1);
	}

	/**
	 * Verifies same rule ID conflict is classified as SEVERE.
	 */
	@Test
	public void sameRuleIdProducesSevereConflict()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("sameRule", 1, 15, "text1", 10, 0);
		PendingModification second = createModification("sameRule", 10, 25, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts).hasSize(1);
		assertThat(conflicts.get(0).severity()).isEqualTo(ConflictSeverity.SEVERE);
	}

	/**
	 * Verifies identical range conflict is classified as SEVERE.
	 */
	@Test
	public void identicalRangeProducesSevereConflict()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 1, 10, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts).hasSize(1);
		assertThat(conflicts.get(0).severity()).isEqualTo(ConflictSeverity.SEVERE);
	}

	/**
	 * Verifies partial overlap with different rules is classified as MODERATE.
	 */
	@Test
	public void partialOverlapProducesModerateConflict()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("rule1", 1, 15, "text1", 10, 0);
		PendingModification second = createModification("rule2", 10, 25, "text2", 10, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts).hasSize(1);
		assertThat(conflicts.get(0).severity()).isEqualTo(ConflictSeverity.MODERATE);
	}

	/**
	 * Verifies conflict description includes rule IDs and overlap range.
	 */
	@Test
	public void conflictDescriptionIncludesRuleIdsAndRange()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("formattingRule", 1, 15, "text1", 10, 0);
		PendingModification second = createModification("indentationRule", 10, 25, "text2", 15, 1);

		List<Conflict> conflicts = detector.detectConflicts(List.of(first, second));

		assertThat(conflicts.get(0).description()).contains("formattingRule");
		assertThat(conflicts.get(0).description()).contains("indentationRule");
		assertThat(conflicts.get(0).description()).contains("priority 10");
		assertThat(conflicts.get(0).description()).contains("priority 15");
	}

	/**
	 * Verifies multiple overlapping pairs produce multiple conflicts.
	 */
	@Test
	public void threeModificationsWithTwoConflicts()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		// mod1 overlaps mod2, mod2 overlaps mod3, but mod1 doesn't overlap mod3
		PendingModification mod1 = createModification("rule1", 1, 15, "text1", 10, 0);
		PendingModification mod2 = createModification("rule2", 10, 25, "text2", 10, 1);
		PendingModification mod3 = createModification("rule3", 20, 35, "text3", 10, 2);

		List<Conflict> conflicts = detector.detectConflicts(List.of(mod1, mod2, mod3));

		assertThat(conflicts).hasSize(2);
	}

	/**
	 * Verifies null modifications list throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullModificationsListThrows()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		detector.detectConflicts(null);
	}

	/**
	 * Verifies null element in modifications list throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullElementInListThrows()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		List<PendingModification> mods = new ArrayList<>();
		mods.add(createModification("rule1", 1, 10, "text", 10, 0));
		mods.add(null);
		mods.add(createModification("rule2", 20, 30, "text", 10, 1));

		detector.detectConflicts(mods);
	}

	/**
	 * Verifies exceeding MAX_PENDING_MODIFICATIONS throws IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void tooManyPendingModificationsThrows()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		List<PendingModification> mods = new ArrayList<>();

		// Create 10,001 modifications (exceeds limit of 10,000)
		for (int i = 0; i < 10_001; ++i)
		{
			mods.add(createModification("rule" + i, i * 100, i * 100 + 10, "text", 10, i));
		}

		detector.detectConflicts(mods);
	}

	/**
	 * Verifies exceeding MAX_CONFLICTS_TO_ANALYZE throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void tooManyConflictsThrows()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		List<PendingModification> mods = new ArrayList<>();

		// Create modifications where every pair overlaps to generate many conflicts
		// With n modifications all overlapping at same position, we get n*(n-1)/2 conflicts
		// To exceed 1000 conflicts, we need approximately 45 modifications
		for (int i = 0; i < 50; ++i)
		{
			// All modifications overlap the range [1,100)
			mods.add(createModification("rule" + i, 1, 100, "text" + i, 10, i));
		}

		detector.detectConflicts(mods);
	}

	/**
	 * Verifies computeSeverity returns conflict's severity.
	 */
	@Test
	public void computeSeverityReturnsConflictSeverity()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		PendingModification first = createModification("rule1", 1, 10, "text1", 10, 0);
		PendingModification second = createModification("rule2", 1, 10, "text2", 10, 1);
		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE, "test");

		ConflictSeverity severity = detector.computeSeverity(conflict);

		assertThat(severity).isEqualTo(ConflictSeverity.SEVERE);
	}

	/**
	 * Verifies computeSeverity with null conflict throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void computeSeverityWithNullConflictThrows()
	{
		DefaultConflictDetector detector = new DefaultConflictDetector();
		detector.computeSeverity(null);
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
