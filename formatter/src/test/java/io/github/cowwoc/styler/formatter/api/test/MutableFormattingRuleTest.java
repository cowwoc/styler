package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.*;
import io.github.cowwoc.styler.formatter.api.plugin.MutableFormattingRule;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the MutableFormattingRule interface.
 */
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.CommentRequired"})
public class MutableFormattingRuleTest
{
	@Test
	public void format_withValidContext_executesWithoutError()
	{
		TestMutableFormattingRule rule = new TestMutableFormattingRule();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		MutableFormattingContext context = TestUtilities.createMutableTestContext(ast,
			"public class Example {}");

		rule.format(context);

		assertThat(context.getRootNode()).isNotNull();
	}

	@Test
	public void mutableRule_extendsFormattingRule_inheritsBaseMethods()
	{
		TestMutableFormattingRule rule = new TestMutableFormattingRule();

		// Verify inherited FormattingRule methods
		assertThat(rule.getRuleId()).isNotNull();
		assertThat(rule.getPriority()).isGreaterThanOrEqualTo(0);
		assertThat(rule.getDefaultConfiguration()).isNotNull();
	}

	/**
	 * Test implementation of MutableFormattingRule.
	 */
	private static final class TestMutableFormattingRule implements MutableFormattingRule
	{
		@Override
		public void format(MutableFormattingContext context)
		{
			// Test implementation: no-op formatting
		}

		@Override
		public String getRuleId()
		{
			return "test.mutable.rule";
		}

		@Override
		public int getPriority()
		{
			return 100;
		}

		@Override
		public RuleConfiguration getDefaultConfiguration()
		{
			return TestUtilities.createTestConfiguration();
		}

		@Override
		public ValidationResult validate(FormattingContext context)
		{
			return ValidationResult.success();
		}

		@Override
		public FormattingResult apply(FormattingContext context)
		{
			return FormattingResult.empty();
		}
	}
}
