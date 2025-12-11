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
 * Tests for indentation formatting in space-only mode.
 */
public final class IndentationSpaceModeTest
{
	private static final String RULE_ID = "indentation";

	/**
	 * Verifies that tabs are converted to spaces with width 4.
	 */
	@Test
	public void shouldConvertTabsToSpacesWithWidth4()
	{
		String sourceCode = "\tint x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Should convert 1 tab to 4 spaces
		requireThat(formatted, "formatted").startsWith("    ");
	}

	/**
	 * Verifies that tabs are converted to spaces with width 2.
	 */
	@Test
	public void shouldConvertTabsToSpacesWithWidth2()
	{
		String sourceCode = "\tint x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 2, 2);

		String formatted = rule.format(context, config);

		// Should convert 1 tab to 2 spaces
		requireThat(formatted, "formatted").startsWith("  ");
	}

	/**
	 * Verifies that multiple tabs are converted to spaces.
	 */
	@Test
	public void shouldConvertMultipleTabsToSpaces()
	{
		String sourceCode = "\t\tint x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Should convert 2 tabs to 8 spaces (2 * 4)
		requireThat(formatted, "formatted").startsWith("        ");
	}

	/**
	 * Verifies that existing space indentation is preserved.
	 */
	@Test
	public void shouldPreserveExistingSpaceIndent()
	{
		String sourceCode = "    int x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		requireThat(formatted, "formatted").startsWith("    ");
	}

	/**
	 * Verifies that incorrect space width is normalized.
	 */
	@Test
	public void shouldNormalizeIncorrectSpaceWidth()
	{
		// 3 spaces is not a valid indent level (width 4), should normalize to 0 (round down)
		String sourceCode = "   int x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// 3 spaces is less than one indent level (4), should normalize to no indentation
		requireThat(formatted, "formatted").isEqualTo("int x = 1;");
	}

	/**
	 * Verifies that tabs are detected as violations in space mode.
	 */
	@Test
	public void shouldRejectTabsInSpaceMode()
	{
		String sourceCode = "\tint x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		List<?> violations = rule.analyze(context, config);

		// Should detect tabs as violations in space mode (at least 1 violation)
		requireThat(violations, "violations").isNotEmpty();
	}

	/**
	 * Verifies that width 8 configuration works correctly.
	 */
	@Test
	public void shouldHandleWidth8Configuration()
	{
		String sourceCode = "\tint x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 8, 8);

		String formatted = rule.format(context, config);

		// Should convert 1 tab to 8 spaces
		requireThat(formatted, "formatted").startsWith("        ");
	}

	/**
	 * Verifies that width 2 configuration works correctly.
	 */
	@Test
	public void shouldHandleWidth2Configuration()
	{
		String sourceCode = "\t\t\tint x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 2, 2);

		String formatted = rule.format(context, config);

		// Should convert 3 tabs to 6 spaces (3 * 2)
		requireThat(formatted, "formatted").startsWith("      ");
	}
}
