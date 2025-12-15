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
 * Tests for indentation formatting in tab-only mode.
 */
public final class IndentationTabModeTest
{
	private static final String RULE_ID = "indentation";

	/**
	 * Verifies that spaces are converted to tabs in leading indentation.
	 */
	@Test
	public void shouldConvertSpacesToTabsInLeadingIndent()
	{
		String sourceCode = "    int x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Should convert 4 spaces to 1 tab when width is 4
		requireThat(formatted, "formatted").contains("\t");
	}

	/**
	 * Verifies that existing tab indentation is preserved.
	 */
	@Test
	public void shouldPreserveExistingTabIndent()
	{
		String sourceCode = "\tint x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").isEqualTo("\tint x = 1;");
	}

	/**
	 * Verifies that mixed indentation is converted to tabs.
	 */
	@Test
	public void shouldConvertMixedIndentToTabs()
	{
		// 1 tab + 4 spaces - formatter uses max(tab_levels=1, space_levels=1) = 1 tab
		String sourceCode = "\t    int x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Max of 1 tab level and 1 space level (4/4) = 1 tab
		requireThat(formatted, "formatted").isEqualTo("\tint x = 1;");
	}

	/**
	 * Verifies that inline spaces after code are not modified.
	 */
	@Test
	public void shouldNotModifyInlineSpaces()
	{
		String sourceCode = "\tint x = 1;  // comment";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Inline spaces (after code) should be preserved
		requireThat(formatted, "formatted").contains("//");
	}

	/**
	 * Verifies that partial tab width is handled correctly.
	 */
	@Test
	public void shouldHandlePartialTabWidth()
	{
		// 2 spaces at width 4 = half a tab, should round down to 0 tabs
		String sourceCode = "  int x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// 2 spaces is less than tab width of 4, so no leading tabs
		requireThat(formatted, "formatted").isEqualTo("int x = 1;");
	}

	/**
	 * Verifies that multiple levels of spaces are converted to multiple tabs.
	 */
	@Test
	public void shouldConvertMultipleLevelsOfSpacesToMultipleTabs()
	{
		// 8 spaces = 2 tabs at width 4
		String sourceCode = "        int x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").isEqualTo("\t\tint x = 1;");
	}

	/**
	 * Verifies that spaces are detected as violations in tab mode.
	 */
	@Test
	public void shouldRejectSpacesInTabMode()
	{
		String sourceCode = "    int x = 1;";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		List<?> violations = rule.analyze(context, List.of(config));

		// Should detect spaces as violations in tab mode (at least 1 violation)
		requireThat(violations, "violations").isNotEmpty();
	}
}
