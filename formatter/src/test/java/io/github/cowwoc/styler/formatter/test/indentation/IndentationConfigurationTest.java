package io.github.cowwoc.styler.formatter.test.indentation;

import io.github.cowwoc.styler.formatter.indentation.IndentationType;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for IndentationFormattingConfiguration validation and creation.
 */
public final class IndentationConfigurationTest
{
	private static final String RULE_ID = "indentation";

	/**
	 * Verifies that null ruleId is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*ruleId.*")
	public void shouldRejectNullRuleId()
	{
		new IndentationFormattingConfiguration(null, IndentationType.SPACES, 4, 4);
	}

	/**
	 * Verifies that empty ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
	public void shouldRejectEmptyRuleId()
	{
		new IndentationFormattingConfiguration("", IndentationType.SPACES, 4, 4);
	}

	/**
	 * Verifies that whitespace-only ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*whitespace.*")
	public void shouldRejectWhitespaceOnlyRuleId()
	{
		new IndentationFormattingConfiguration("   ", IndentationType.SPACES, 4, 4);
	}

	/**
	 * Verifies that null indentationType is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*indentationType.*")
	public void shouldRejectNullIndentationType()
	{
		new IndentationFormattingConfiguration(RULE_ID, null, 4, 4);
	}

	/**
	 * Verifies that zero indent width is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*positive.*")
	public void shouldRejectZeroIndentWidth()
	{
		new IndentationFormattingConfiguration(RULE_ID, IndentationType.SPACES, 0, 4);
	}

	/**
	 * Verifies that negative indent width is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*positive.*")
	public void shouldRejectNegativeIndentWidth()
	{
		new IndentationFormattingConfiguration(RULE_ID, IndentationType.SPACES, -4, 4);
	}

	/**
	 * Verifies that valid indent width is accepted.
	 */
	@Test
	public void shouldAcceptValidIndentWidth()
	{
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 8, 4);

		requireThat(config.indentSize(), "indentSize").isEqualTo(8);
	}

	/**
	 * Verifies that valid tab configuration is accepted.
	 */
	@Test
	public void shouldAcceptValidTabConfiguration()
	{
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		requireThat(config, "config").isNotNull();
		requireThat(config.indentationType(), "indentationType").isEqualTo(IndentationType.TABS);
		requireThat(config.indentSize(), "indentSize").isEqualTo(4);
	}

	/**
	 * Verifies that valid space configuration is accepted.
	 */
	@Test
	public void shouldAcceptValidSpaceConfiguration()
	{
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		requireThat(config, "config").isNotNull();
		requireThat(config.indentationType(), "indentationType").isEqualTo(IndentationType.SPACES);
		requireThat(config.indentSize(), "indentSize").isEqualTo(4);
	}

	/**
	 * Verifies that indent width of 2 is accepted.
	 */
	@Test
	public void shouldAcceptWidth2()
	{
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 2, 2);

		requireThat(config.indentSize(), "indentSize").isEqualTo(2);
	}

	/**
	 * Verifies that indent width of 8 is accepted.
	 */
	@Test
	public void shouldAcceptWidth8()
	{
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 8, 8);

		requireThat(config.indentSize(), "indentSize").isEqualTo(8);
	}
}
