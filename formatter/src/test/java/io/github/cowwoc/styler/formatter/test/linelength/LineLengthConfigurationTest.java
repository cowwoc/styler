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
		new LineLengthConfiguration(null, 120, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
	}

	/**
	 * Tests that empty ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectEmptyRuleId()
	{
		new LineLengthConfiguration("", 120, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
	}

	/**
	 * Tests that whitespace-only ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWhitespaceOnlyRuleId()
	{
		new LineLengthConfiguration("   ", 120, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
	}

	/**
	 * Tests that maxLineLength below minimum is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectMaxLineLengthBelowMinimum()
	{
		new LineLengthConfiguration("line-length", 39, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
	}

	/**
	 * Tests that maxLineLength above maximum is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectMaxLineLengthAboveMaximum()
	{
		new LineLengthConfiguration("line-length", 501, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
	}

	/**
	 * Tests that maxLineLength at minimum boundary is accepted.
	 */
	@Test
	public void shouldAcceptMaxLineLengthAtMinimumBoundary()
	{
		LineLengthConfiguration config = new LineLengthConfiguration("line-length", 40, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
		requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(40);
	}

	/**
	 * Tests that maxLineLength at maximum boundary is accepted.
	 */
	@Test
	public void shouldAcceptMaxLineLengthAtMaximumBoundary()
	{
		LineLengthConfiguration config = new LineLengthConfiguration("line-length", 500, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
		requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(500);
	}

	/**
	 * Tests that ruleId is returned correctly.
	 */
	@Test
	public void shouldReturnCorrectRuleId()
	{
		LineLengthConfiguration config = new LineLengthConfiguration("line-length", 120, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
		requireThat(config.ruleId(), "ruleId").isEqualTo("line-length");
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
		new LineLengthConfiguration("line-length", 120, 4, 4,
			null, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
	}
}
