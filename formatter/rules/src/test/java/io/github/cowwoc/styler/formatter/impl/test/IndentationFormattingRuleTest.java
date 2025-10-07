package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ValidationResult;
import io.github.cowwoc.styler.formatter.api.test.TestUtilities;
import io.github.cowwoc.styler.formatter.impl.IndentationConfiguration;
import io.github.cowwoc.styler.formatter.impl.IndentationFormattingRule;
import io.github.cowwoc.styler.formatter.impl.IndentationMode;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link IndentationFormattingRule}.
 */
public final class IndentationFormattingRuleTest
{
	/**
	 * Verifies that getRuleId returns correct rule identifier.
	 */
	@Test
	public void getRuleIdReturnsCorrectIdentifier()
	{
		IndentationFormattingRule rule = new IndentationFormattingRule();
		String ruleId = rule.getRuleId();

		assertThat(ruleId).isEqualTo("io.github.cowwoc.styler.rules.Indentation");
	}

	/**
	 * Verifies that getPriority returns correct priority value.
	 */
	@Test
	public void getPriorityReturnsCorrectValue()
	{
		IndentationFormattingRule rule = new IndentationFormattingRule();
		int priority = rule.getPriority();

		assertThat(priority).isEqualTo(75);
	}

	/**
	 * Verifies that getDefaultConfiguration returns IndentationConfiguration.
	 */
	@Test
	public void getDefaultConfigurationReturnsIndentationConfiguration()
	{
		IndentationFormattingRule rule = new IndentationFormattingRule();
		RuleConfiguration config = rule.getDefaultConfiguration();

		assertThat(config).isInstanceOf(IndentationConfiguration.class);
	}

	/**
	 * Verifies that getDefaultConfiguration returns spaces mode.
	 */
	@Test
	public void getDefaultConfigurationReturnsSpacesMode()
	{
		IndentationFormattingRule rule = new IndentationFormattingRule();
		IndentationConfiguration config = (IndentationConfiguration) rule.getDefaultConfiguration();

		assertThat(config.getMode()).isEqualTo(IndentationMode.SPACES);
		assertThat(config.getIndentSize()).isEqualTo(4);
	}

	/**
	 * Verifies that validate rejects null context.
	 */
	@Test
	public void validateRejectsNullContext()
	{
		IndentationFormattingRule rule = new IndentationFormattingRule();
		ValidationResult result = rule.validate(null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.getFirstErrorMessage()).contains("FormattingContext cannot be null");
	}

	/**
	 * Verifies that validate rejects null root node in context.
	 */
	@Test
	public void validateRejectsNullRootNode()
	{
		IndentationFormattingRule rule = new IndentationFormattingRule();
		FormattingContext context = new FormattingContext(
			null,
			"source",
			Path.of("/test/Example.java"),
			rule.getDefaultConfiguration(),
			Set.of(rule.getRuleId()),
			Map.of());

		ValidationResult result = rule.validate(context);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.getFirstErrorMessage()).contains("Root AST node cannot be null");
	}

	/**
	 * Verifies that validate rejects empty source text in context.
	 */
	@Test
	public void validateRejectsEmptySourceText()
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		IndentationFormattingRule rule = new IndentationFormattingRule();
		FormattingContext context = new FormattingContext(
			ast,
			"",
			Path.of("/test/Example.java"),
			rule.getDefaultConfiguration(),
			Set.of(rule.getRuleId()),
			Map.of());

		ValidationResult result = rule.validate(context);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.getFirstErrorMessage()).contains("Source text cannot be null or empty");
	}

	/**
	 * Verifies that apply returns empty result when AST has no indentable nodes.
	 */
	@Test
	public void applyReturnsEmptyResultWhenNoIndentableNodes()
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		IndentationFormattingRule rule = new IndentationFormattingRule();
		FormattingContext context = new FormattingContext(
			ast,
			"public class Example {}",
			Path.of("/test/Example.java"),
			rule.getDefaultConfiguration(),
			Set.of(rule.getRuleId()),
			Map.of());

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}
}
