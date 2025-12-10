package io.github.cowwoc.styler.formatter.test.brace;

import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceStyle;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for BraceFormattingConfiguration validation.
 */
public class BraceFormattingConfigurationTest
{
	/**
	 * Tests that null ruleId is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullRuleId()
	{
		new BraceFormattingConfiguration(null, BraceStyle.NEW_LINE);
	}

	/**
	 * Tests that empty ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectEmptyRuleId()
	{
		new BraceFormattingConfiguration("", BraceStyle.NEW_LINE);
	}

	/**
	 * Tests that whitespace-only ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWhitespaceOnlyRuleId()
	{
		new BraceFormattingConfiguration("   ", BraceStyle.NEW_LINE);
	}

	/**
	 * Tests that null braceStyle is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullBraceStyle()
	{
		new BraceFormattingConfiguration("brace-style", null);
	}

	/**
	 * Tests that valid configuration is accepted.
	 */
	@Test
	public void shouldAcceptValidConfiguration()
	{
		BraceFormattingConfiguration config = new BraceFormattingConfiguration("test-rule",
			BraceStyle.SAME_LINE);

		requireThat(config.ruleId(), "ruleId").isEqualTo("test-rule");
		requireThat(config.braceStyle(), "braceStyle").isEqualTo(BraceStyle.SAME_LINE);
	}

	/**
	 * Tests that default configuration uses correct values.
	 */
	@Test
	public void shouldReturnCorrectDefaultConfig()
	{
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		requireThat(config.ruleId(), "ruleId").isEqualTo("brace-style");
		requireThat(config.braceStyle(), "braceStyle").isEqualTo(BraceStyle.NEW_LINE);
	}

	/**
	 * Tests that each brace style can be configured.
	 */
	@Test
	public void shouldAllowAllBraceStyles()
	{
		for (BraceStyle style : BraceStyle.values())
		{
			BraceFormattingConfiguration config = new BraceFormattingConfiguration("test", style);
			requireThat(config.braceStyle(), "braceStyle").isEqualTo(style);
		}
	}
}
