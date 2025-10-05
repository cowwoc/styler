package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import io.github.cowwoc.styler.formatter.api.report.PriorityScore;
import io.github.cowwoc.styler.formatter.api.report.ViolationEntry;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ViolationEntry} record validation and equality.
 */
public final class ViolationEntryTest
{
	/**
	 * Verifies record stores all fields correctly.
	 */
	@Test
	public void recordStoresAllFields()
	{
		SourceRange location = new SourceRange(new SourcePosition(5, 10), new SourcePosition(5, 20));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);

		ViolationEntry entry = new ViolationEntry("test-rule", ViolationSeverity.ERROR,
			"Test message", location, priority, "Fix suggestion");

		assertThat(entry.ruleId()).isEqualTo("test-rule");
		assertThat(entry.severity()).isEqualTo(ViolationSeverity.ERROR);
		assertThat(entry.message()).isEqualTo("Test message");
		assertThat(entry.sourceRange()).isEqualTo(location);
		assertThat(entry.priorityScore()).isEqualTo(priority);
		assertThat(entry.fixSuggestion()).isEqualTo("Fix suggestion");
	}

	/**
	 * Verifies null fix suggestion is allowed.
	 */
	@Test
	public void nullFixSuggestionIsAllowed()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.INFO, 1);

		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.INFO,
			"message", location, priority, null);

		assertThat(entry.fixSuggestion()).isNull();
	}

	/**
	 * Verifies null rule ID throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullRuleIdThrows()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);

		new ViolationEntry(null, ViolationSeverity.ERROR, "message", location, priority, null);
	}

	/**
	 * Verifies null severity throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullSeverityThrows()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);

		new ViolationEntry("rule", null, "message", location, priority, null);
	}

	/**
	 * Verifies null message throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullMessageThrows()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);

		new ViolationEntry("rule", ViolationSeverity.ERROR, null, location, priority, null);
	}

	/**
	 * Verifies null source range throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullSourceRangeThrows()
	{
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);

		new ViolationEntry("rule", ViolationSeverity.ERROR, "message", null, priority, null);
	}

	/**
	 * Verifies null priority score throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullPriorityScoreThrows()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));

		new ViolationEntry("rule", ViolationSeverity.ERROR, "message", location, null, null);
	}

	/**
	 * Verifies blank message throws IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void blankMessageThrows()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);

		new ViolationEntry("rule", ViolationSeverity.ERROR, "   ", location, priority, null);
	}

	/**
	 * Verifies blank rule ID throws IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void blankRuleIdThrows()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);

		new ViolationEntry("   ", ViolationSeverity.ERROR, "message", location, priority, null);
	}

	/**
	 * Verifies records with same values are equal.
	 */
	@Test
	public void equalityWorksForSameValues()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);

		ViolationEntry entry1 = new ViolationEntry("rule", ViolationSeverity.ERROR,
			"message", location, priority, "fix");
		ViolationEntry entry2 = new ViolationEntry("rule", ViolationSeverity.ERROR,
			"message", location, priority, "fix");

		assertThat(entry1).isEqualTo(entry2);
		assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
	}

	/**
	 * Verifies hasFixSuggestion returns true for non-blank fix suggestion.
	 */
	@Test
	public void hasFixSuggestionReturnsTrueForNonBlankSuggestion()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.ERROR,
			"message", location, priority, "Fix this");

		assertThat(entry.hasFixSuggestion()).isTrue();
	}

	/**
	 * Verifies hasFixSuggestion returns false for null fix suggestion.
	 */
	@Test
	public void hasFixSuggestionReturnsFalseForNullSuggestion()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.ERROR,
			"message", location, priority, null);

		assertThat(entry.hasFixSuggestion()).isFalse();
	}

	/**
	 * Verifies hasFixSuggestion returns false for blank fix suggestion.
	 */
	@Test
	public void hasFixSuggestionReturnsFalseForBlankSuggestion()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.ERROR,
			"message", location, priority, "   ");

		assertThat(entry.hasFixSuggestion()).isFalse();
	}

	/**
	 * Verifies isError returns true for ERROR severity.
	 */
	@Test
	public void isErrorReturnsTrueForErrorSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.ERROR,
			"message", location, priority, null);

		assertThat(entry.isError()).isTrue();
	}

	/**
	 * Verifies isError returns false for WARNING severity.
	 */
	@Test
	public void isErrorReturnsFalseForWarningSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.WARNING, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.WARNING,
			"message", location, priority, null);

		assertThat(entry.isError()).isFalse();
	}

	/**
	 * Verifies isError returns false for INFO severity.
	 */
	@Test
	public void isErrorReturnsFalseForInfoSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.INFO, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.INFO,
			"message", location, priority, null);

		assertThat(entry.isError()).isFalse();
	}

	/**
	 * Verifies isWarning returns true for WARNING severity.
	 */
	@Test
	public void isWarningReturnsTrueForWarningSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.WARNING, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.WARNING,
			"message", location, priority, null);

		assertThat(entry.isWarning()).isTrue();
	}

	/**
	 * Verifies isWarning returns false for ERROR severity.
	 */
	@Test
	public void isWarningReturnsFalseForErrorSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.ERROR,
			"message", location, priority, null);

		assertThat(entry.isWarning()).isFalse();
	}

	/**
	 * Verifies isWarning returns false for INFO severity.
	 */
	@Test
	public void isWarningReturnsFalseForInfoSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.INFO, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.INFO,
			"message", location, priority, null);

		assertThat(entry.isWarning()).isFalse();
	}

	/**
	 * Verifies isInfo returns true for INFO severity.
	 */
	@Test
	public void isInfoReturnsTrueForInfoSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.INFO, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.INFO,
			"message", location, priority, null);

		assertThat(entry.isInfo()).isTrue();
	}

	/**
	 * Verifies isInfo returns false for ERROR severity.
	 */
	@Test
	public void isInfoReturnsFalseForErrorSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.ERROR,
			"message", location, priority, null);

		assertThat(entry.isInfo()).isFalse();
	}

	/**
	 * Verifies isInfo returns false for WARNING severity.
	 */
	@Test
	public void isInfoReturnsFalseForWarningSeverity()
	{
		SourceRange location = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		PriorityScore priority = PriorityScore.of(ViolationSeverity.WARNING, 1);
		ViolationEntry entry = new ViolationEntry("rule", ViolationSeverity.WARNING,
			"message", location, priority, null);

		assertThat(entry.isInfo()).isFalse();
	}
}
