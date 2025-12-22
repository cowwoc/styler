package io.github.cowwoc.styler.formatter.test.linelength;

import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.WrapStyle;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for LineLengthConfiguration validation and default values.
 */
public class LineLengthConfigurationTest
{
	/**
	 * Tests that null ruleId is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullRuleId()
	{
		LineLengthConfiguration.builder().
			ruleId(null).
			build();
	}

	/**
	 * Tests that empty ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectEmptyRuleId()
	{
		LineLengthConfiguration.builder().
			ruleId("").
			build();
	}

	/**
	 * Tests that whitespace-only ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWhitespaceOnlyRuleId()
	{
		LineLengthConfiguration.builder().
			ruleId("   ").
			build();
	}

	/**
	 * Tests that maxLineLength below minimum is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectMaxLineLengthBelowMinimum()
	{
		LineLengthConfiguration.builder().
			maxLineLength(39).
			build();
	}

	/**
	 * Tests that maxLineLength above maximum is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectMaxLineLengthAboveMaximum()
	{
		LineLengthConfiguration.builder().
			maxLineLength(501).
			build();
	}

	/**
	 * Tests that maxLineLength at minimum boundary is accepted.
	 */
	@Test
	public void shouldAcceptMaxLineLengthAtMinimumBoundary()
	{
		LineLengthConfiguration config = LineLengthConfiguration.builder().
			maxLineLength(40).
			build();
		requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(40);
	}

	/**
	 * Tests that maxLineLength at maximum boundary is accepted.
	 */
	@Test
	public void shouldAcceptMaxLineLengthAtMaximumBoundary()
	{
		LineLengthConfiguration config = LineLengthConfiguration.builder().
			maxLineLength(500).
			build();
		requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(500);
	}

	/**
	 * Tests that ruleId is returned correctly.
	 */
	@Test
	public void shouldReturnCorrectRuleId()
	{
		LineLengthConfiguration config = LineLengthConfiguration.builder().
			ruleId("custom-rule").
			build();
		requireThat(config.ruleId(), "ruleId").isEqualTo("custom-rule");
	}

	/**
	 * Tests that default configuration returns expected values.
	 */
	@Test
	public void shouldReturnDefaultValues()
	{
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(120);
		requireThat(config.tabWidth(), "tabWidth").isEqualTo(4);
		requireThat(config.indentContinuationLines(), "indentContinuationLines").isEqualTo(4);
		requireThat(config.wrapLongStrings(), "wrapLongStrings").isTrue();
	}

	/**
	 * Tests that all wrap styles default to AFTER.
	 */
	@Test
	public void shouldDefaultAllWrapStylesToAfter()
	{
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		requireThat(config.methodChainWrap(), "methodChainWrap").isEqualTo(WrapStyle.AFTER);
		requireThat(config.methodArgumentsWrap(), "methodArgumentsWrap").isEqualTo(WrapStyle.AFTER);
		requireThat(config.binaryExpressionWrap(), "binaryExpressionWrap").isEqualTo(WrapStyle.AFTER);
		requireThat(config.methodParametersWrap(), "methodParametersWrap").isEqualTo(WrapStyle.AFTER);
		requireThat(config.ternaryExpressionWrap(), "ternaryExpressionWrap").isEqualTo(WrapStyle.AFTER);
		requireThat(config.arrayInitializerWrap(), "arrayInitializerWrap").isEqualTo(WrapStyle.AFTER);
		requireThat(config.annotationArgumentsWrap(), "annotationArgumentsWrap").isEqualTo(WrapStyle.AFTER);
		requireThat(config.genericTypeArgsWrap(), "genericTypeArgsWrap").isEqualTo(WrapStyle.AFTER);
	}

	/**
	 * Tests that null wrap style is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullWrapStyle()
	{
		LineLengthConfiguration.builder().
			methodChainWrap(null).
			build();
	}
}
