package io.github.cowwoc.styler.formatter.api.test.conflict;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.EditPriority;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.conflict.Conflict;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictResolutionException;
import io.github.cowwoc.styler.formatter.api.conflict.ConflictSeverity;
import io.github.cowwoc.styler.formatter.api.conflict.FailFastResolutionStrategy;
import io.github.cowwoc.styler.formatter.api.conflict.PendingModification;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FailFastResolutionStrategy}.
 * <p>
 * Validates strict conflict rejection strategy that treats all conflicts as unrecoverable errors.
 */
public class FailFastResolutionStrategyTest
{
	/**
	 * Verifies that fail-fast strategy cannot resolve any conflict.
	 */
	@Test
	public void canResolveAlwaysReturnsFalse()
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "text1", 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Any conflict description");

		assertThat(strategy.canResolve(conflict)).isFalse();
	}

	/**
	 * Verifies that fail-fast strategy rejects even non-overlapping modifications.
	 */
	@Test
	public void canResolveReturnsFalseForNonOverlappingModifications()
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 5, "text1", 0);
		PendingModification second = createModification("rule2", 10, 15, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Non-overlapping modifications");

		assertThat(strategy.canResolve(conflict)).isFalse();
	}

	/**
	 * Verifies that fail-fast strategy rejects conflicts with identical replacement text.
	 */
	@Test
	public void canResolveReturnsFalseForIdenticalReplacementText()
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "sameText", 0);
		PendingModification second = createModification("rule2", 5, 15, "sameText", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Identical replacement text");

		assertThat(strategy.canResolve(conflict)).isFalse();
	}

	/**
	 * Verifies that canResolve with null conflict throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void canResolveWithNullConflictThrows()
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();
		strategy.canResolve(null);
	}

	/**
	 * Verifies that resolve always throws ConflictResolutionException for any conflict.
	 */
	@Test(expectedExceptions = ConflictResolutionException.class)
	public void resolveAlwaysThrowsException()
		throws ConflictResolutionException
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();

		PendingModification first = createModification("rule1", 1, 10, "text1", 0);
		PendingModification second = createModification("rule2", 5, 15, "text2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Overlapping modifications");

		strategy.resolve(conflict);
	}

	/**
	 * Verifies that exception message includes conflict description for minor conflicts.
	 */
	@Test
	public void resolveMinorConflictIncludesDescriptionInException()
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();

		PendingModification first = createModification("whitespaceRule", 1, 10, "  ", 0);
		PendingModification second = createModification("indentRule", 1, 10, "\t", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MINOR,
			"Whitespace vs tab indentation");

		try
		{
			strategy.resolve(conflict);
			throw new AssertionError("Expected ConflictResolutionException");
		}
		catch (ConflictResolutionException e)
		{
			assertThat(e.getMessage()).contains("Whitespace vs tab indentation");
			assertThat(e.getMessage()).contains("Fail-fast strategy");
		}
	}

	/**
	 * Verifies that exception includes ConflictReport with correct severity for severe conflicts.
	 */
	@Test
	public void resolveSevereConflictIncludesConflictReport()
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();

		PendingModification first = createModification("formattingRule", 1, 20, "formatted", 0);
		PendingModification second = createModification("refactorRule", 5, 25, "refactored", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.SEVERE,
			"Formatting vs refactoring conflict");

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
			assertThat(e.getReport().summary()).contains("Fail-fast policy");
			assertThat(e.getReport().summary()).contains("Formatting vs refactoring conflict");
		}
	}

	/**
	 * Verifies that ConflictReport includes both rule IDs in conflictsByRule map.
	 */
	@Test
	public void resolveConflictReportIncludesBothRuleIds()
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();

		PendingModification first = createModification("lineWrapRule", 10, 20, "wrapped", 0);
		PendingModification second = createModification("columnLimitRule", 15, 25, "limited", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MODERATE,
			"Line wrap vs column limit");

		try
		{
			strategy.resolve(conflict);
			throw new AssertionError("Expected ConflictResolutionException");
		}
		catch (ConflictResolutionException e)
		{
			assertThat(e.getReport().conflictsByRule()).containsKeys("lineWrapRule", "columnLimitRule");
			assertThat(e.getReport().conflictsByRule().get("lineWrapRule")).isEqualTo(1);
			assertThat(e.getReport().conflictsByRule().get("columnLimitRule")).isEqualTo(1);
		}
	}

	/**
	 * Verifies that resolve with null conflict throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void resolveWithNullConflictThrows()
		throws ConflictResolutionException
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();
		strategy.resolve(null);
	}

	/**
	 * Verifies that fail-fast strategy rejects conflicts regardless of severity level.
	 */
	@Test(expectedExceptions = ConflictResolutionException.class)
	public void resolveRejectsModerateConflict()
		throws ConflictResolutionException
	{
		FailFastResolutionStrategy strategy = new FailFastResolutionStrategy();

		PendingModification first = createModification("rule1", 5, 15, "fix1", 0);
		PendingModification second = createModification("rule2", 10, 20, "fix2", 1);

		Conflict conflict = new Conflict(first, second, ConflictSeverity.MODERATE,
			"Moderate severity conflict");

		strategy.resolve(conflict);
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
