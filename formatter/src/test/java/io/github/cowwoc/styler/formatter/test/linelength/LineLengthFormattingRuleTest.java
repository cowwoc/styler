package io.github.cowwoc.styler.formatter.test.linelength;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthFormattingRule;
import io.github.cowwoc.styler.formatter.linelength.WrapStyle;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for LineLengthFormattingRule analyze and format methods.
 */
public class LineLengthFormattingRuleTest
{
	/**
	 * Tests that rule returns correct ID.
	 */
	@Test
	public void shouldReturnCorrectRuleId()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		requireThat(rule.getId(), "getId").isEqualTo("line-length");
	}

	/**
	 * Tests that rule returns correct name.
	 */
	@Test
	public void shouldReturnCorrectRuleName()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		requireThat(rule.getName(), "getName").isEqualTo("Line Length");
	}

	/**
	 * Tests that rule returns correct description.
	 */
	@Test
	public void shouldReturnCorrectRuleDescription()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		requireThat(rule.getDescription(), "getDescription").isNotNull().isNotBlank();
	}

	/**
	 * Tests that rule returns WARNING as default severity.
	 */
	@Test
	public void shouldReturnWarningAsDefaultSeverity()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		requireThat(rule.getDefaultSeverity(), "getDefaultSeverity").isEqualTo(ViolationSeverity.WARNING);
	}

	/**
	 * Tests that null context is rejected in analyze.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInAnalyze()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		rule.analyze(null, null);
	}

	/**
	 * Tests that null context is rejected in format.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInFormat()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		rule.format(null, null);
	}

	/**
	 * Tests that wrong config type is rejected in analyze.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWrongConfigTypeInAnalyze()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		// Pass a non-LineLengthConfiguration
		rule.analyze(context, new WrongConfigType());
	}

	/**
	 * Tests that wrong config type is rejected in format.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWrongConfigTypeInFormat()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		// Pass a non-LineLengthConfiguration
		rule.format(context, new WrongConfigType());
	}

	/**
	 * Tests that analyze returns empty list for short lines.
	 */
	@Test
	public void shouldReturnEmptyListForShortLines()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		List<FormattingViolation> violations = rule.analyze(context, null);
		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests that analyze detects lines exceeding max length.
	 */
	@Test
	public void shouldDetectLinesExceedingMaxLength()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		// Create a line that exceeds default max length of 120
		String longLine = "public class Test { void method() { String s = \"" + "a".repeat(100) + "\"; } }";
		TestTransformationContext context = new TestTransformationContext(longLine);

		List<FormattingViolation> violations = rule.analyze(context, null);
		requireThat(violations, "violations").isNotEmpty();
		requireThat(violations.get(0).severity(), "severity").isEqualTo(ViolationSeverity.WARNING);
	}

	/**
	 * Tests that analyze respects custom max length.
	 */
	@Test
	public void shouldRespectCustomMaxLength()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		// Create a 50-character line
		String source = "class Test { void method() { int x = 1; } } extra";
		TestTransformationContext context = new TestTransformationContext(source);

		// With max length 40, should report violation
		LineLengthConfiguration strictConfig = new LineLengthConfiguration(
			"line-length", 40, 4, 4,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);

		List<FormattingViolation> violations = rule.analyze(context, strictConfig);
		requireThat(violations, "violations").isNotEmpty();
	}

	/**
	 * Tests that format returns unchanged source for short lines.
	 */
	@Test
	public void shouldReturnUnchangedSourceForShortLines()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		String result = rule.format(context, null);
		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests that format handles null config by using defaults.
	 */
	@Test
	public void shouldUseDefaultConfigWhenNull()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		// Should not throw with null config
		String result = rule.format(context, null);
		requireThat(result, "result").isNotNull();
	}

	/**
	 * Tests that format preserves multiple lines.
	 */
	@Test
	public void shouldPreserveMultipleLines()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {\n    void method() {\n    }\n}";
		TestTransformationContext context = new TestTransformationContext(source);

		String result = rule.format(context, null);
		requireThat(result, "result").contains("\n");
	}

	/**
	 * Tests that format preserves trailing newline.
	 */
	@Test
	public void shouldPreserveTrailingNewline()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}\n";
		TestTransformationContext context = new TestTransformationContext(source);

		String result = rule.format(context, null);
		requireThat(result, "result").endsWith("\n");
	}

	/**
	 * A wrong configuration type for testing type validation.
	 */
	private record WrongConfigType() implements FormattingConfiguration
	{
		@Override
		public String ruleId()
		{
			return "wrong-type";
		}
	}
}
