package io.github.cowwoc.styler.formatter.test;

import io.github.cowwoc.styler.formatter.DefaultFixStrategy;
import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FixStrategy;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for FormattingViolation with suggested fixes.
 */
public class FormattingViolationWithFixesTest
{
	private static final Path TEST_FILE = Path.of("Test.java");

	/**
	 * Tests that null suggested fixes are rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullSuggestedFixes()
	{
		new DefaultFormattingViolation(
			"TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, null);
	}

	/**
	 * Tests that empty fixes list is accepted.
	 */
	@Test
	public void shouldAcceptEmptyFixesList()
	{
		FormattingViolation violation = new DefaultFormattingViolation(
			"TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, List.of());

		requireThat(violation.suggestedFixes(), "suggestedFixes").isEmpty();
	}

	/**
	 * Tests that a single fix is accepted.
	 */
	@Test
	public void shouldAcceptSingleFix()
	{
		FixStrategy fix = new DefaultFixStrategy("Test fix", true, "fixed", 0, 5);

		FormattingViolation violation = new DefaultFormattingViolation(
			"TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, List.of(fix));

		requireThat(violation.suggestedFixes().size(), "suggestedFixes.size").isEqualTo(1);
	}

	/**
	 * Tests that multiple fixes are accepted.
	 */
	@Test
	public void shouldAcceptMultipleFixes()
	{
		FixStrategy fix1 = new DefaultFixStrategy("Fix 1", true, "fixed1", 0, 5);
		FixStrategy fix2 = new DefaultFixStrategy("Fix 2", true, "fixed2", 0, 5);

		FormattingViolation violation = new DefaultFormattingViolation(
			"TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, List.of(fix1, fix2));

		requireThat(violation.suggestedFixes().size(), "suggestedFixes.size").isEqualTo(2);
	}

	/**
	 * Tests that the fixes list is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void shouldReturnImmutableFixesList()
	{
		FixStrategy fix = new DefaultFixStrategy("Test", true, "fixed", 0, 5);
		FormattingViolation violation = new DefaultFormattingViolation(
			"TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, List.of(fix));

		violation.suggestedFixes().add(new DefaultFixStrategy("New", true, "new", 0, 5));
	}
}
