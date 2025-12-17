package io.github.cowwoc.styler.formatter.test.indentation;

import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationType;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for preservation of string content during indentation formatting.
 */
public final class IndentationPreservationTest
{
	private static final String RULE_ID = "indentation";

	/**
	 * Verifies that string literal content is preserved during formatting.
	 */
	@Test
	public void shouldPreserveStringLiteralContent()
	{
		String sourceCode = """
			class Test {
			    String s = "  leading spaces  ";
			}""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("leading spaces");
	}

	/**
	 * Verifies that escape sequences are preserved during formatting.
	 */
	@Test
	public void shouldPreserveEscapeSequences()
	{
		String sourceCode = """
			class Test {
			    String s = "line1\\nline2\\ttab";
			}""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("\\n").contains("\\t");
	}
}
