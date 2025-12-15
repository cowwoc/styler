package io.github.cowwoc.styler.formatter.test.whitespace;

import java.util.List;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

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
	 * Tests that configuration can be customized.
	 */
	@Test
	public void shouldSupportCustomConfiguration()
	{
		WhitespaceFormattingConfiguration config = new WhitespaceFormattingConfiguration(
			"whitespace",
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true);

		requireThat(config, "config").isNotNull();
		requireThat(config.spaceAroundBinaryOperators(), "spaceAroundBinaryOperators").
			isEqualTo(true);
	}

	/**
	 * Tests that spacing can be disabled via config.
	 */
	@Test
	public void shouldRespectConfigurationDisable()
	{
		String source = "int x = a+b;";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		// Create config with spaceAroundBinaryOperators disabled
		WhitespaceFormattingConfiguration config = new WhitespaceFormattingConfiguration(
			"whitespace",
			false,   // spaceAroundBinaryOperators = false
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true);

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing since binary operators are disabled
		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests that comma spacing can be disabled.
	 */
	@Test
	public void shouldRespectCommaSpacingConfig()
	{
		String source = "method(a,b,c)";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		// Create config with spaceAfterComma disabled
		WhitespaceFormattingConfiguration config = new WhitespaceFormattingConfiguration(
			"whitespace",
			true,
			true,
			false,   // spaceAfterComma = false
			true,
			true,
			true,
			true,
			true,
			true);

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing
		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests that keyword spacing can be disabled.
	 */
	@Test
	public void shouldRespectKeywordSpacingConfig()
	{
		String source = "if(x){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		// Create config with spaceAfterControlKeywords disabled
		WhitespaceFormattingConfiguration config = new WhitespaceFormattingConfiguration(
			"whitespace",
			true,
			true,
			true,
			true,
			false,   // spaceAfterControlKeywords = false
			true,
			true,
			true,
			true);

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing
		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests that method reference spacing behavior is configurable.
	 */
	@Test
	public void shouldRespectMethodReferenceConfig()
	{
		String source = "String :: valueOf";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		// Create config with noSpaceAroundMethodReference enabled
		WhitespaceFormattingConfiguration config = new WhitespaceFormattingConfiguration(
			"whitespace",
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true);   // noSpaceAroundMethodReference = true

		String result = rule.format(context, List.of(config));

		// Should have no spaces around ::
		requireThat(result, "result").contains("String::valueOf");
	}

	/**
	 * Tests that lambda arrow spacing can be disabled.
	 */
	@Test
	public void shouldRespectLambdaArrowConfig()
	{
		String source = "x  ->  y";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		// Create config with spaceAroundArrowInLambda disabled
		WhitespaceFormattingConfiguration config = new WhitespaceFormattingConfiguration(
			"whitespace",
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			false,   // spaceAroundArrowInLambda = false
			true);

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing
		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests that enhanced for colon spacing can be disabled.
	 */
	@Test
	public void shouldRespectEnhancedForColonConfig()
	{
		String source = "for(String s : list){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		// Create config with spaceAroundColonInEnhancedFor disabled
		WhitespaceFormattingConfiguration config = new WhitespaceFormattingConfiguration(
			"whitespace",
			true,
			true,
			true,
			true,
			true,
			true,
			false,   // spaceAroundColonInEnhancedFor = false
			true,
			true);

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
		String source = "x=1;";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();

		// Create config with spaceAroundAssignmentOperators disabled
		WhitespaceFormattingConfiguration config = new WhitespaceFormattingConfiguration(
			"whitespace",
			true,
			false,   // spaceAroundAssignmentOperators = false
			true,
			true,
			true,
			true,
			true,
			true,
			true);

		String result = rule.format(context, List.of(config));

		// Should preserve original spacing
		requireThat(result, "result").isEqualTo(source);
	}
}
