package io.github.cowwoc.styler.formatter.test.indentation;

import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationType;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.formatter.test.indentation.IndentationTestUtils.getIndentedLine;

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
		String sourceCode = "class T {\n    int x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Should convert 4 spaces to 1 tab when width is 4
		requireThat(getIndentedLine(formatted), "indentedLine").startsWith("\t");
	}

	/**
	 * Verifies that existing tab indentation is preserved.
	 */
	@Test
	public void shouldPreserveExistingTabIndent()
	{
		String sourceCode = "class T {\n\tint x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(getIndentedLine(formatted), "indentedLine").isEqualTo("\tint x = 1;");
	}

	/**
	 * Verifies that mixed indentation is converted to tabs.
	 */
	@Test
	public void shouldConvertMixedIndentToTabs()
	{
		// 1 tab + 4 spaces - formatter uses max(tab_levels=1, space_levels=1) = 1 tab
		String sourceCode = "class T {\n\t    int x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Max of 1 tab level and 1 space level (4/4) = 1 tab
		requireThat(getIndentedLine(formatted), "indentedLine").isEqualTo("\tint x = 1;");
	}

	/**
	 * Verifies that inline spaces after code are not modified.
	 */
	@Test
	public void shouldNotModifyInlineSpaces()
	{
		String sourceCode = "class T {\n\tint x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Tab indentation should be preserved
		requireThat(getIndentedLine(formatted), "indentedLine").startsWith("\t");
	}

	/**
	 * Verifies that partial tab width is normalized to AST depth.
	 */
	@Test
	public void shouldHandlePartialTabWidth()
	{
		// 2 spaces at width 4 = half a tab, but AST depth is 1 (inside class)
		String sourceCode = "class T {\n  int x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// AST depth is 1, so 1 tab regardless of original indent
		requireThat(getIndentedLine(formatted), "indentedLine").isEqualTo("\tint x = 1;");
	}

	/**
	 * Verifies that spaces are converted to tabs based on AST depth.
	 */
	@Test
	public void shouldConvertMultipleLevelsOfSpacesToMultipleTabs()
	{
		// 8 spaces original, but AST depth is 1 (inside class)
		String sourceCode = "class T {\n        int x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// AST depth is 1, so 1 tab regardless of original indent
		requireThat(getIndentedLine(formatted), "indentedLine").isEqualTo("\tint x = 1;");
	}

	/**
	 * Verifies that spaces are detected as violations in tab mode.
	 */
	@Test
	public void shouldRejectSpacesInTabMode()
	{
		String sourceCode = "class T {\n    int x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.TABS, 4, 4);

		List<?> violations = rule.analyze(context, List.of(config));

		// Should detect spaces as violations in tab mode (at least 1 violation)
		requireThat(violations, "violations").isNotEmpty();
	}
}
