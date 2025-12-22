package io.github.cowwoc.styler.formatter.test.whitespace;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.formatter.test.whitespace.WhitespaceTestUtils.wrapInMethod;

/**
 * Tests for WhitespaceFormattingConfiguration validation and behavior.
 */
public class WhitespaceConfigurationTest
{
	/**
	 * Tests that default configuration is created.
	 */
	@Test
	public void shouldCreateDefaultConfiguration()
	{
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		requireThat(config, "config").isNotNull();
	}

	/**
	 * Tests that default config has rule ID.
	 */
	@Test
	public void shouldHaveRuleIdInConfig()
	{
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		requireThat(config.ruleId(), "ruleId").isNotNull();
	}

	/**
	 * Tests that configuration can be customized using the builder.
	 */
	@Test
	public void shouldSupportCustomConfiguration()
	{
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.builder().
			spaceAroundBinaryOperator(true).
			build();

		requireThat(config, "config").isNotNull();
		requireThat(config.spaceAroundBinaryOperator(), "spaceAroundBinaryOperator").
			isEqualTo(true);
	}

	/**
	 * Tests that spacing can be disabled via config.
	 */
	@Test
	public void shouldRespectConfigurationDisable()
	{
		String source = wrapInMethod("int x = a+b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.builder().
			spaceAroundBinaryOperator(false).
			build();

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing since binary operators are disabled
		requireThat(result, "result").contains("a+b");
	}

	/**
	 * Tests that comma spacing can be disabled.
	 */
	@Test
	public void shouldRespectCommaSpacingConfig()
	{
		String source = wrapInMethod("method(a,b,c);");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.builder().
			spaceAfterComma(false).
			build();

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing
		requireThat(result, "result").contains("method(a,b,c)");
	}

	/**
	 * Tests that keyword spacing can be disabled.
	 */
	@Test
	public void shouldRespectKeywordSpacingConfig()
	{
		String source = wrapInMethod("if(x){}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.builder().
			spaceAfterControlKeyword(false).
			build();

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing
		requireThat(result, "result").contains("if(x)");
	}

	/**
	 * Tests that lambda arrow spacing can be disabled.
	 */
	@Test
	public void shouldRespectLambdaArrowConfig()
	{
		String source = wrapInMethod("Object o = x->y;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.builder().
			spaceAroundArrowInLambda(false).
			build();

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing (no spaces around arrow)
		requireThat(result, "result").contains("x->y");
	}

	/**
	 * Tests that enhanced for colon spacing can be disabled.
	 */
	@Test
	public void shouldRespectEnhancedForColonConfig()
	{
		String source = wrapInMethod("for(String s : list){}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.builder().
			spaceAroundColonInEnhancedForLoop(false).
			build();

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing (no changes to colons)
		requireThat(result, "result").contains("for (String s : list)");
	}

	/**
	 * Tests that assignment operator spacing can be disabled.
	 */
	@Test
	public void shouldRespectAssignmentOperatorConfig()
	{
		String source = wrapInMethod("x=1;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.builder().
			spaceAroundAssignmentOperators(false).
			build();

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing
		requireThat(result, "result").contains("x=1");
	}
}
