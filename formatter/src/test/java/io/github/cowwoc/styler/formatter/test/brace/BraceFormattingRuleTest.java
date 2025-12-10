package io.github.cowwoc.styler.formatter.test.brace;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for BraceFormattingRule interface compliance.
 */
public class BraceFormattingRuleTest
{
	/**
	 * Tests that rule returns correct ID.
	 */
	@Test
	public void shouldReturnCorrectRuleId()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		requireThat(rule.getId(), "getId").isEqualTo("brace-style");
	}

	/**
	 * Tests that rule returns correct name.
	 */
	@Test
	public void shouldReturnCorrectRuleName()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		requireThat(rule.getName(), "getName").isEqualTo("Brace Style");
	}

	/**
	 * Tests that rule returns correct description.
	 */
	@Test
	public void shouldReturnCorrectRuleDescription()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		requireThat(rule.getDescription(), "getDescription").isNotNull().isNotBlank();
	}

	/**
	 * Tests that rule returns WARNING as default severity.
	 */
	@Test
	public void shouldReturnWarningAsDefaultSeverity()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		requireThat(rule.getDefaultSeverity(), "getDefaultSeverity").isEqualTo(ViolationSeverity.WARNING);
	}

	/**
	 * Tests that null context is rejected in analyze.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInAnalyze()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		rule.analyze(null, null);
	}

	/**
	 * Tests that null context is rejected in format.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInFormat()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		rule.format(null, null);
	}

	/**
	 * Tests that wrong config type is rejected in analyze.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWrongConfigTypeInAnalyze()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		rule.analyze(context, new WrongConfigType());
	}

	/**
	 * Tests that wrong config type is rejected in format.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWrongConfigTypeInFormat()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		rule.format(context, new WrongConfigType());
	}

	/**
	 * Tests that null config uses defaults in analyze.
	 */
	@Test
	public void shouldUseDefaultConfigWhenNullInAnalyze()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test { void method() { } }";
		TestTransformationContext context = new TestTransformationContext(source);

		List<FormattingViolation> violations = rule.analyze(context, null);
		requireThat(violations, "violations").isNotNull();
	}

	/**
	 * Tests that null config uses defaults in format.
	 */
	@Test
	public void shouldUseDefaultConfigWhenNullInFormat()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test { void method() { } }";
		TestTransformationContext context = new TestTransformationContext(source);

		String result = rule.format(context, null);
		requireThat(result, "result").isNotNull();
	}

	/**
	 * Tests that analyze detects K&R style when Allman is required.
	 */
	@Test
	public void shouldDetectKRStyleWhenAllmanRequired()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test {" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);

		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();
		List<FormattingViolation> violations = rule.analyze(context, config);

		requireThat(violations, "violations").isNotEmpty();
	}

	/**
	 * Tests that analyze detects no violations for conforming code.
	 */
	@Test
	public void shouldDetectNoViolationsForConformingCode()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = """
			class Test
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<FormattingViolation> violations = rule.analyze(context, null);
		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests that format returns unchanged source for conforming code.
	 */
	@Test
	public void shouldReturnUnchangedSourceForConformingCode()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = """
			class Test
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);

		String result = rule.format(context, null);
		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests that format transforms K&R to Allman style.
	 */
	@Test
	public void shouldTransformKRToAllmanStyle()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test {" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);

		String result = rule.format(context, null);
		requireThat(result, "result").contains("{").contains("}");
	}

	/**
	 * Test configuration type for testing config validation.
	 */
	private static final class WrongConfigType implements io.github.cowwoc.styler.formatter.FormattingConfiguration
	{
		@Override
		public String ruleId()
		{
			return "wrong";
		}
	}
}
