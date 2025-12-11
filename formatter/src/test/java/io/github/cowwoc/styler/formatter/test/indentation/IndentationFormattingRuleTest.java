package io.github.cowwoc.styler.formatter.test.indentation;

import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationType;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Integration tests for IndentationFormattingRule interface.
 */
public final class IndentationFormattingRuleTest
{
	private static final String RULE_ID = "indentation";

	/**
	 * Verifies that the rule provides required metadata.
	 */
	@Test
	public void shouldProvideRuleMetadata()
	{
		FormattingRule rule = new IndentationFormattingRule();

		requireThat(rule.getId(), "id").isNotNull().isNotEmpty();
		requireThat(rule.getName(), "name").isNotNull().isNotEmpty();
		requireThat(rule.getDescription(), "description").isNotNull().isNotEmpty();
		requireThat(rule.getDefaultSeverity(), "severity").isNotNull();
	}

	/**
	 * Verifies that properly indented code is analyzed without violations.
	 */
	@Test
	public void shouldAnalyzeProperlyIndentedCode()
	{
		String sourceCode = """
			class Test {
			    int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		List<?> violations = rule.analyze(context, config);

		// Properly indented code should have no violations
		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Verifies that code can be formatted.
	 */
	@Test
	public void shouldFormatCode()
	{
		String sourceCode = """
			class Test {
			int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    int x = 1;
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that an empty file can be analyzed.
	 */
	@Test
	public void shouldAnalyzeEmptyFile()
	{
		String sourceCode = "";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		List<?> violations = rule.analyze(context, config);

		// Empty file should have no violations
		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Verifies that an empty file can be formatted.
	 */
	@Test
	public void shouldFormatEmptyFile()
	{
		String sourceCode = "";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		requireThat(formatted, "formatted").isEqualTo("");
	}

	/**
	 * Verifies that null context is rejected for analyze.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextForAnalyze()
	{
		FormattingRule rule = new IndentationFormattingRule();
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		rule.analyze(null, config);
	}

	/**
	 * Verifies that null context is rejected for format.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextForFormat()
	{
		FormattingRule rule = new IndentationFormattingRule();
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		rule.format(null, config);
	}

	/**
	 * Verifies that null config is handled with defaults.
	 */
	@Test
	public void shouldHandleNullConfigWithDefaults()
	{
		String sourceCode = "class Test {}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);

		// Should not crash when config is null - uses default configuration
		List<?> violations = rule.analyze(context, null);
		requireThat(violations, "violations").isNotNull();

		String formatted = rule.format(context, null);
		requireThat(formatted, "formatted").isEqualTo("class Test {}");
	}
}
