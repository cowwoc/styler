package io.github.cowwoc.styler.formatter.test.whitespace;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for WhitespaceFormattingRule interface compliance.
 */
public class WhitespaceFormattingRuleTest
{
	/**
	 * Dummy configuration type for testing wrong config type rejection.
	 */
	private static final class WrongConfigType implements FormattingConfiguration
	{
		@Override
		public String ruleId()
		{
			return "wrong";
		}
	}
	/**
	 * Tests that the rule implements FormattingRule.
	 */
	@Test
	public void shouldImplementFormattingRule()
	{
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		requireThat(rule, "rule").isInstanceOf(FormattingRule.class);
	}

	/**
	 * Tests that the rule has the correct ID.
	 */
	@Test
	public void shouldHaveCorrectRuleId()
	{
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		requireThat(rule.getId(), "ruleId").isEqualTo("whitespace");
	}

	/**
	 * Tests that the rule has a name.
	 */
	@Test
	public void shouldHaveName()
	{
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		requireThat(rule.getName(), "name").isNotNull().isNotEmpty();
	}

	/**
	 * Tests that the rule has a description.
	 */
	@Test
	public void shouldHaveDescription()
	{
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		requireThat(rule.getDescription(), "description").isNotNull().isNotEmpty();
	}

	/**
	 * Tests that analyze() with null context throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInAnalyze()
	{
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		rule.analyze(null, config);
	}

	/**
	 * Tests that format() with null context throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInFormat()
	{
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		rule.format(null, config);
	}

	/**
	 * Tests that analyze() with wrong config type throws IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWrongConfigTypeInAnalyze()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		rule.analyze(context, new WrongConfigType());
	}

	/**
	 * Tests that format() with wrong config type throws IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWrongConfigTypeInFormat()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		rule.format(context, new WrongConfigType());
	}

	/**
	 * Tests that analyze() handles empty source code.
	 */
	@Test
	public void shouldHandleEmptySource()
	{
		TestTransformationContext context = new TestTransformationContext("");
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		List<FormattingViolation> violations = rule.analyze(context, config);

		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests that format() returns empty string for empty source.
	 */
	@Test
	public void shouldReturnEmptyStringForEmptySource()
	{
		TestTransformationContext context = new TestTransformationContext("");
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, config);

		requireThat(result, "result").isEmpty();
	}

	/**
	 * Tests that formatting is idempotent.
	 */
	@Test
	public void shouldBeIdempotent()
	{
		String source = "class Test { int x = a+b; }";
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		TestTransformationContext context1 = new TestTransformationContext(source);
		String result1 = rule.format(context1, config);

		TestTransformationContext context2 = new TestTransformationContext(result1);
		String result2 = rule.format(context2, config);

		requireThat(result2, "result2").isEqualTo(result1);
	}
}
