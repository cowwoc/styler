package io.github.cowwoc.styler.formatter.test.linelength;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthFormattingRule;
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
		rule.analyze(null, List.of());
	}

	/**
	 * Tests that null context is rejected in format.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInFormat()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		rule.format(null, List.of());
	}

	/**
	 * Tests that null configs list is rejected in analyze.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullConfigsInAnalyze()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		rule.analyze(context, null);
	}

	/**
	 * Tests that null configs list is rejected in format.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullConfigsInFormat()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		rule.format(context, null);
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

		List<FormattingViolation> violations = rule.analyze(context, List.of());
		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests that analyze detects lines exceeding max length.
	 */
	@Test
	public void shouldDetectLinesExceedingMaxLength()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		// Create a source where one line exceeds default max length of 120 (string literal makes it > 120)
		String longLine = """
			class Test
			{
				String s = \"""" + "a".repeat(110) + """
			";
			}""";
		TestTransformationContext context = new TestTransformationContext(longLine);

		List<FormattingViolation> violations = rule.analyze(context, List.of());
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
		// Create a source where one line exceeds 40 characters
		String source = """
			class Test
			{
				void aMethodNameThatMakesThisLineExceedFortyCharacters()
				{
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		// With max length 40, should report violation
		LineLengthConfiguration strictConfig = LineLengthConfiguration.builder().
			maxLineLength(40).
			build();

		List<FormattingViolation> violations = rule.analyze(context, List.of(strictConfig));
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

		String result = rule.format(context, List.of());
		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests that format handles empty config list by using defaults.
	 */
	@Test
	public void shouldUseDefaultConfigWhenEmptyList()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		// Should not throw with empty config list
		String result = rule.format(context, List.of());
		requireThat(result, "result").isNotNull();
	}

	/**
	 * Tests that format preserves multiple lines.
	 */
	@Test
	public void shouldPreserveMultipleLines()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = """
			class Test
			{
				void method()
				{
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		String result = rule.format(context, List.of());
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

		String result = rule.format(context, List.of());
		requireThat(result, "result").endsWith("\n");
	}
}
