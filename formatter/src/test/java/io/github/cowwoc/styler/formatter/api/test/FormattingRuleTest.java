package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the FormattingRule interface, validating core interface contracts.
 */
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.CommentRequired"})
public class FormattingRuleTest
{
	@Test
	public void getRuleId_withValidRule_returnsRuleIdentifier()
	{
		FormattingRule rule = new TestFormattingRule("test.rule", 100);

		String ruleId = rule.getRuleId();

		assertThat(ruleId).isNotNull().isEqualTo("test.rule");
	}

	@Test
	public void getPriority_withValidRule_returnsConfiguredPriority()
	{
		FormattingRule rule = new TestFormattingRule("test.rule", 100);

		int priority = rule.getPriority();

		assertThat(priority).isEqualTo(100);
	}

	@Test
	public void getDefaultConfiguration_withValidRule_returnsNonNullConfiguration()
	{
		FormattingRule rule = new TestFormattingRule("test.rule", 100);

		RuleConfiguration config = rule.getDefaultConfiguration();

		assertThat(config).isNotNull();
	}

	@Test
	public void validate_withNullContext_returnsFailure()
	{
		FormattingRule rule = new TestFormattingRule("test.rule", 100);

		ValidationResult result = rule.validate(null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.getErrorMessages()).isNotEmpty();
	}

	@Test
	public void validate_withValidContext_returnsSuccess()
	{
		FormattingRule rule = new TestFormattingRule("test.rule", 100);
		CompilationUnitNode ast = TestUtilities.createTestAST();
		FormattingContext context = TestUtilities.createTestContext(ast, "public class Example {}");

		ValidationResult result = rule.validate(context);

		assertThat(result.isValid()).isTrue();
	}

	@Test
	public void apply_withValidContext_returnsNonNullResult()
	{
		FormattingRule rule = new TestFormattingRule("test.rule", 100);
		CompilationUnitNode ast = TestUtilities.createTestAST();
		FormattingContext context = TestUtilities.createTestContext(ast, "public class Example {}");

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
		assertThat(result.getEdits()).isNotNull();
		assertThat(result.getViolations()).isNotNull();
	}
}
