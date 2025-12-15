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

		rule.analyze(null, List.of());
	}

	/**
	 * Tests that format() with null context throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextInFormat()
	{
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		rule.format(null, List.of());
	}

	/**
	 * Tests that analyze() with null configs list throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullConfigsInAnalyze()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		rule.analyze(context, null);
	}

	/**
	 * Tests that format() with null configs list throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullConfigsInFormat()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		rule.format(context, null);
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

		List<FormattingViolation> violations = rule.analyze(context, List.of(config));

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

		String result = rule.format(context, List.of(config));

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
		String result1 = rule.format(context1, List.of(config));

		TestTransformationContext context2 = new TestTransformationContext(result1);
		String result2 = rule.format(context2, List.of(config));

		requireThat(result2, "result2").isEqualTo(result1);
	}
}
