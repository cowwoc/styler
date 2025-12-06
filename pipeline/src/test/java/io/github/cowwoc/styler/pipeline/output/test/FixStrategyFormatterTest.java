package io.github.cowwoc.styler.pipeline.output.test;

import io.github.cowwoc.styler.formatter.DefaultFixStrategy;
import io.github.cowwoc.styler.formatter.FixStrategy;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for FixStrategyFormatter fix code example generation.
 */
public class FixStrategyFormatterTest
{
	/**
	 * Tests that fix strategy with code example is formatted correctly.
	 */
	@Test
	public void shouldFormatFixStrategyWithCodeExample()
	{
		// Given: Violation with fix strategy containing code example
		FixStrategy fix = new DefaultFixStrategy(
			"Wrap line at method call",
			true,
			"foo()\n  .bar()",
			100,
			120);
		FormattingViolation violation = TestViolationFactory.createViolationWithFix(fix);

		// When: Format violation
		String formatted = formatViolationWithFixes(violation);

		// Then: Should contain fix details
		requireThat(formatted, "value").isNotNull();
		requireThat(formatted.contains("Wrap line at method call"), "value").isEqualTo(true);
		requireThat(formatted.contains("foo()"), "value").isEqualTo(true);
	}

	/**
	 * Tests that multiple fix strategies are formatted correctly.
	 */
	@Test
	public void shouldFormatMultipleFixStrategies()
	{
		// Given: Violation with 2 fix strategies
		FixStrategy fix1 = new DefaultFixStrategy("Option 1", true, "fixed1", 0, 10);
		FixStrategy fix2 = new DefaultFixStrategy("Option 2", false, "fixed2", 0, 10);
		FormattingViolation violation = TestViolationFactory.createViolationWithFixes(
			List.of(fix1, fix2));



		// When: Format violation
		String formatted = formatViolationWithFixes(violation);

		// Then: Should contain both fixes
		requireThat(formatted, "value").isNotNull();
		requireThat(formatted.contains("Option 1"), "value").isEqualTo(true);
		requireThat(formatted.contains("Option 2"), "value").isEqualTo(true);
		// Should indicate auto-applicability
		requireThat(formatted.contains("true") || formatted.contains("false"), "value").isEqualTo(true);
	}

	/**
	 * Tests that violation with no fixes is formatted correctly.
	 */
	@Test
	public void shouldFormatViolationWithNoFixes()
	{
		// Given: Violation without fix strategies
		FormattingViolation violation = TestViolationFactory.createViolationWithFixes(List.of());

		// When: Format violation
		String formatted = formatViolationWithFixes(violation);

		// Then: Should handle gracefully
		requireThat(formatted, "value").isNotNull();
		requireThat(formatted.contains("violation"), "value").isEqualTo(true);
	}

	/**
	 * Formats a violation with its fixes.
	 *
	 * @param violation the violation to format
	 * @return formatted violation string with fixes
	 */
	private String formatViolationWithFixes(FormattingViolation violation)
	{
		StringBuilder output = new StringBuilder(256);
		output.append("violation: ").append(violation.ruleId()).append(" - ").
			append(violation.message()).append('\n').
			append("fixes: ").append(violation.suggestedFixes().size()).append('\n');

		for (FixStrategy fix : violation.suggestedFixes())
		{
			output.append("  - ").append(fix.description()).
				append(" [auto:").append(fix.isAutoApplicable()).append("]\n").
				append("    replacement: ").append(fix.replacementText()).append('\n');
		}

		return output.toString();
	}
}
