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
		String sourceCode = "class T {\n\tint x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Should convert 1 tab to 4 spaces
		requireThat(getIndentedLine(formatted), "indentedLine").startsWith("    ");
	}

	/**
	 * Verifies that tabs are converted to spaces with width 2.
	 */
	@Test
	public void shouldConvertTabsToSpacesWithWidth2()
	{
		String sourceCode = "class T {\n\tint x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 2, 2);

		String formatted = rule.format(context, List.of(config));

		// Should convert 1 tab to 2 spaces
		requireThat(getIndentedLine(formatted), "indentedLine").startsWith("  ");
	}

	/**
	 * Verifies that multiple tabs are converted to spaces based on AST depth.
	 */
	@Test
	public void shouldConvertMultipleTabsToSpaces()
	{
		String sourceCode = "class T {\n\t\tint x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// AST depth is 1 (inside class), so 4 spaces regardless of original tabs
		requireThat(getIndentedLine(formatted), "indentedLine").startsWith("    ");
	}

	/**
	 * Verifies that existing space indentation is preserved.
	 */
	@Test
	public void shouldPreserveExistingSpaceIndent()
	{
		String sourceCode = "class T {\n    int x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(getIndentedLine(formatted), "indentedLine").startsWith("    ");
	}

	/**
	 * Verifies that incorrect space width is normalized to AST depth.
	 */
	@Test
	public void shouldNormalizeIncorrectSpaceWidth()
	{
		// 3 spaces is not a valid indent level, but AST depth is 1 (inside class)
		String sourceCode = "class T {\n   int x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// AST depth is 1, so 4 spaces regardless of original indent
		requireThat(getIndentedLine(formatted), "indentedLine").isEqualTo("    int x = 1;");
	}

	/**
	 * Verifies that tabs are detected as violations in space mode.
	 */
	@Test
	public void shouldRejectTabsInSpaceMode()
	{
		String sourceCode = "class T {\n\tint x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		List<?> violations = rule.analyze(context, List.of(config));

		// Should detect tabs as violations in space mode (at least 1 violation)
		requireThat(violations, "violations").isNotEmpty();
	}

	/**
	 * Verifies that width 8 configuration works correctly.
	 */
	@Test
	public void shouldHandleWidth8Configuration()
	{
		String sourceCode = "class T {\n\tint x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 8, 8);

		String formatted = rule.format(context, List.of(config));

		// Should convert 1 tab to 8 spaces
		requireThat(getIndentedLine(formatted), "indentedLine").startsWith("        ");
	}

	/**
	 * Verifies that width 2 configuration works correctly with AST depth.
	 */
	@Test
	public void shouldHandleWidth2Configuration()
	{
		String sourceCode = "class T {\n\t\t\tint x = 1;\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 2, 2);

		String formatted = rule.format(context, List.of(config));

		// AST depth is 1 (inside class), so 2 spaces at width 2
		requireThat(getIndentedLine(formatted), "indentedLine").startsWith("  ");
	}
}
