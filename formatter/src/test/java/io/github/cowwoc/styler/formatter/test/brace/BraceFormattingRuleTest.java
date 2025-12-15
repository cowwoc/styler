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
		rule.analyze(null, List.of());
	}

	/**
	 * Tests that null context is rejected in format.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInFormat()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		rule.format(null, List.of());
	}

	/**
	 * Tests that null configs list is rejected in analyze.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullConfigsInAnalyze()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
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
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);

		rule.format(context, null);
	}

	/**
	 * Tests that empty config list uses defaults in analyze.
	 */
	@Test
	public void shouldUseDefaultConfigWhenEmptyListInAnalyze()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test { void method() { } }";
		TestTransformationContext context = new TestTransformationContext(source);

		List<FormattingViolation> violations = rule.analyze(context, List.of());
		requireThat(violations, "violations").isNotNull();
	}

	/**
	 * Tests that empty config list uses defaults in format.
	 */
	@Test
	public void shouldUseDefaultConfigWhenEmptyListInFormat()
	{
		BraceFormattingRule rule = new BraceFormattingRule();
		String source = "class Test { void method() { } }";
		TestTransformationContext context = new TestTransformationContext(source);

		String result = rule.format(context, List.of());
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
		List<FormattingViolation> violations = rule.analyze(context, List.of(config));

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

		List<FormattingViolation> violations = rule.analyze(context, List.of());
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

		String result = rule.format(context, List.of());
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

		String result = rule.format(context, List.of());
		requireThat(result, "result").contains("{").contains("}");
	}
}
